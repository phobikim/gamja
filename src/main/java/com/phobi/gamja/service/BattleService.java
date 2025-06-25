package com.phobi.gamja.service;

import com.phobi.gamja.dto.battle.DropItemDto;
import com.phobi.gamja.dto.battle.SkillResultDto;
import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.dex.DexSkillDto;
import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.item.ItemDto;
import com.phobi.gamja.dto.user.UserEquipment;
import com.phobi.gamja.entity.battle.BattleSkill;
import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterDrop;
import com.phobi.gamja.entity.battle.MonsterMap;
import com.phobi.gamja.entity.dex.*;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemPotionEffect;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.battle.MonsterDropRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.battle.MonsterMapRepository;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.dex.DexSkillImageRepository;
import com.phobi.gamja.repository.dex.DexSkillRepository;
import com.phobi.gamja.repository.item.ItemPotionEffectRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.UserDexStatRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserEquipmentRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterMapRepository monsterMapRepository;
    private final ItemRepository itemRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final ItemPotionEffectRepository itemPotionEffectRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final DexSkillRepository dexSkillRepository;
    private final DexSkillImageRepository dexSkillImageRepository;
    private final MonsterDropRepository monsterDropRepository;

    public GamJaResponse getMapList(HttpServletRequest request) {
        List<MonsterMap> maps = monsterMapRepository.findAll().stream()
                .filter(MonsterMap::isEnabled)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (MonsterMap map : maps) {
            List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(map);
            List<MonsterDto> monsterDtos = monsters.stream()
                    .map(this::toMonsterDtoWithDropItems)
                    .collect(Collectors.toList());

            List<Map<String, Object>> monsterList = monsters.stream()
                    .sorted(Comparator.comparingInt(Monster::getMonsterPower))
                    .map(monster -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", monster.getId());
                        m.put("name", monster.getName());
                        m.put("rank", monster.getRank());
                        m.put("monsterImg", monster.getImagePath());
                        m.put("desc", monster.getDesc());
                        return m;
                    })
                    .collect(Collectors.toList());

            List<DropItemDto> uniqueDrops = collectUniqueDropItems(monsterDtos);
            uniqueDrops.sort(Comparator.comparing(DropItemDto::getRarity));

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("id", map.getId());
            mapData.put("name", map.getName());
            mapData.put("desc", map.getDesc());
            mapData.put("imagePath", map.getBackgroundImagePath());
            mapData.put("recommendedLevel", map.getRecommendedLevel());
            mapData.put("monsters", monsterList);
            mapData.put("rewards", uniqueDrops);

            result.add(mapData);
        }

        return GamJaResponse.success("맵 리스트 조회 성공", result);
    }

    public GamJaResponse getUserBattleStat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return GamJaResponse.fail("장착한 캐릭터가 없습니다.");
        }

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat userDexStat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        BattleStatDto userBattleDto = statCalculator.calculateBattleStat(userId);

        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", userDtl.getUser().getUsername());
        userInfo.put("charImage", userDtl.getCharacterImage());
        userInfo.put("dexName", userDexStat.getDex().getName());
        userInfo.put("lv", userDexStat.getLevel());
        userInfo.put("xp", userDexStat.getXp());
        userInfo.put("maxExp", userDexStat.getMaxExp());
        DexAttribute attr = userDexStat.getDex().getAttribute();
        userInfo.put("attribute", attr != null ? attr.getName() : null);
        userInfo.put("attributeIconPath", attr != null ? attr.getIconPath() : null);
        userInfo.put("power", userBattleDto.getPower().getTotal());
        userInfo.put("hp", userBattleDto.getHp().getTotal());
        userInfo.put("speed", userBattleDto.getSpeed().getTotal());

        // 포션 아이템 정보
        ItemDto potionItem = null;
        int potionCount = 0;
        int bonusPower = 0;
        int bonusHp = 0;
        int durationTurns = 0;

        if (userBattleDto.getEquippedItems() != null) {
            potionItem = userBattleDto.getEquippedItems().stream()
                    .filter(item -> "POTION".equals(item.getEquipSlot()))
                    .findFirst()
                    .orElse(null);

            if (potionItem != null) {
                // 수량 조회
                potionCount = userInventoryRepository.findByUserIdAndItemId(userId, potionItem.getId())
                        .map(UserInventory::getQuantity)
                        .orElse(0);

                // 효과 조회
                ItemPotionEffect effect = itemPotionEffectRepository.findById(potionItem.getId())
                        .orElse(null);
                if (effect != null) {
                    bonusPower = effect.getBonusPower();
                    bonusHp = effect.getHealHp();
                    durationTurns = effect.getDurationTurns();
                }
            }
        }

        Map<String, Object> potionInfo = new HashMap<>();
        if (potionItem != null) {
            potionInfo.put("itemName", potionItem.getName());
            potionInfo.put("itemPath", potionItem.getIconPath());
            potionInfo.put("quantity", potionCount);
            potionInfo.put("bonusPower", bonusPower);
            potionInfo.put("bonusHp", bonusHp);
            potionInfo.put("durationTurns", durationTurns);
        } else {
            potionInfo.put("quantity", 0); // 없으면 기본값
        }

        userInfo.put("potion", potionInfo);

        return GamJaResponse.success("유저 스탯 조회 성공", userInfo);
    }

    public GamJaResponse getMonstersByMap(Long mapId) {
        Optional<MonsterMap> mapOpt = monsterMapRepository.findById(mapId);
        if (mapOpt.isEmpty()) {
            return GamJaResponse.fail("맵을 찾을 수 없습니다.");
        }

        List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(mapOpt.get());
        List<MonsterDto> monsterDtos = monsters.stream()
                .map(this::toMonsterDtoWithDropItems)
                .collect(Collectors.toList());

        return GamJaResponse.success("몬스터 전체 조회 성공", monsterDtos);
    }

    private List<DropItemDto> collectUniqueDropItems(List<MonsterDto> monsters) {
        return monsters.stream()
                .flatMap(m -> m.getDropItems().stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(DropItemDto::getItemId, Function.identity(), (a, b) -> a),
                        m -> new ArrayList<>(m.values())
                ));
    }

    public MonsterDto toMonsterDtoWithDropItems(Monster monster) {
        MonsterDto dto = new MonsterDto();
        dto.setId(monster.getId());
        dto.setName(monster.getName());
        dto.setDesc(monster.getDesc());
        dto.setRank(monster.getRank());
        dto.setImagePath(monster.getImagePath());
        dto.setMonsterPower(monster.getMonsterPower());
        dto.setMonsterHp(monster.getMonsterHp());
        dto.setMonsterXp(monster.getMonsterXp());

        // DropItemDto 리스트 구성
        List<DropItemDto> dropDtos = monsterDropRepository.findByMonster(monster).stream()
                .map(drop -> {
                    Item item = drop.getItem();
                    return DropItemDto.builder()
                            .itemId(item.getId())
                            .name(item.getName())
                            .iconPath(item.getIconPath())
                            .rarity(item.getRarity())
                            .itemType(item.getItemType())
                            .dropRate(drop.getDropRate())
                            .minCount(drop.getMinCount())
                            .maxCount(drop.getMaxCount())
                            .build();
                })
                .collect(Collectors.toList());

        dto.setDropItems(dropDtos);
        return dto;
    }

    @Transactional
    public GamJaResponse usePotion(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 현재 장착중인 POTION 아이템 ID 가져오기
        Optional<Long> potionItemIdOpt = userEquipmentRepository
                .findByUserIdAndSlot(userId, EquipmentSlot.POTION)
                .map(UserEquipment::getItemId);

        if (potionItemIdOpt.isEmpty()) {
            return GamJaResponse.fail("장착된 물약이 없습니다.");
        }

        Long potionItemId = potionItemIdOpt.get();

        // 인벤토리 수량 가져오기
        Optional<UserInventory> inventoryOpt = userInventoryRepository.findByUserIdAndItemId(userId, potionItemId);
        if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() <= 0) {
            return GamJaResponse.fail("보유한 물약이 없습니다.");
        }

        // 수량 감소 처리
        UserInventory inventory = inventoryOpt.get();
        inventory.setQuantity(inventory.getQuantity() - 1);
        userInventoryRepository.save(inventory);

        int remaining = Math.max(0, inventory.getQuantity());

        return GamJaResponse.success("물약 사용 완료", Map.of("quantity", remaining));
    }


    /**
     * attribute 속성, 스킬 타입(type)에 맞는 스킬 목록을 DTO로 변환하여 반환
     */
    public List<DexSkillDto> getSkills(String attribute, BattleSkill.Type type) {
        return dexSkillRepository
                .findByDexAttributeAndSkillType(attribute, type)
                .stream()
                .map(skill -> {
                    List<String> images = dexSkillImageRepository
                            .findBySkillId(skill.getId())
                            .stream()
                            .map(DexSkillImage::getImagePath)
                            .collect(Collectors.toList());

                    return DexSkillDto.builder()
                            .id(skill.getId())
                            .name(skill.getName())
                            .description(skill.getDescription())
                            .powerRatio(skill.getPowerRatio())
                            .effect(skill.getEffect().name())
                            .effectValue(skill.getEffectValue())
                            .target(skill.getTarget().name())
                            .mpCost(skill.getMpCost())
                            .cooldown(skill.getCooldown())
                            .images(images)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 스킬 사용 로직
     */
    public SkillResultDto useSkill(Long skillId) {
        // 1) 스킬 조회
        DexSkill skill = dexSkillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스킬입니다. id=" + skillId));

        // 2) 유저 파워 등 가져오기 (예시 하드코딩, 실제로는 인증된 유저 ID로 조회)
        int playerPower = 10; // TODO: UserStatService로 대체

        // 3) 효과별 계산
        Integer damage = null, heal = null, buff = null;
        switch (skill.getEffect()) {
            case DAMAGE:
                damage = Math.round(playerPower * skill.getPowerRatio());
                break;
            case HEAL:
                heal = skill.getEffectValue();
                break;
            case BUFF:
                buff = skill.getEffectValue();
                break;
        }

        return SkillResultDto.builder()
                .damage(damage)
                .heal(heal)
                .buff(buff)
                .build();
    }

}
