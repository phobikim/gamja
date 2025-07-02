package com.phobi.gamja.service;

import com.phobi.gamja.dto.battle.DropItemDto;
import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.item.ItemDto;
import com.phobi.gamja.dto.user.UserDexXpDto;
import com.phobi.gamja.dto.user.UserEquipment;
import com.phobi.gamja.entity.battle.*;
import com.phobi.gamja.entity.dex.*;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemPotionEffect;
import com.phobi.gamja.entity.item.ItemReward;
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
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final CommonUtil commonUtil;
    private final LogService logService;
    private final UserLogService userLogService;
    private final CorpsTierService corpsTierService;
    private final StatCalculator statCalculator;
    private final ActionService actionService;
    private final LevelService levelService;


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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public GamJaResponse getUserBattleStat(Long userId) {
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public GamJaResponse startBattle(Map<String, Object> request, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        Long mapId = ((Number) request.get("mapId")).longValue();

        // 몬스터 랜덤 선택
        MonsterMap map = monsterMapRepository.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("맵을 찾을 수 없습니다."));
        List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(map);
        if (monsters.isEmpty()) return GamJaResponse.fail("해당 맵에 몬스터가 없습니다.");

        Monster monster = monsters.get(new Random().nextInt(monsters.size()));

        // 유저 스탯 및 포션 정보
        GamJaResponse userRes = getUserBattleStat(userId);
        if (!"SUCCESS".equals(userRes.getCode())) return userRes;
        Map<String, Object> userInfo = (Map<String, Object>) userRes.getData();

        // 유저 스킬 이미지 (BASIC 타입 1개)
        String attribute = (String) userInfo.get("attribute");
        DexSkill skill = dexSkillRepository.findByDexAttributeAndSkillType(attribute, BattleSkill.Type.BASIC)
                .stream().findFirst().orElse(null);
        String skillImagePath = null;
        if (skill != null) {
            skillImagePath = dexSkillImageRepository.findBySkillId(skill.getId())
                    .stream()
                    .map(DexSkillImage::getImagePath)
                    .findFirst().orElse(null);
        }

        // 드롭 정보 (세션 저장용)
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
                }).collect(Collectors.toList());

        //
        BattleSession battleSession = new BattleSession();
        battleSession.setUserId(userId);
        battleSession.setPlayerHp((Integer) userInfo.get("hp"));
        battleSession.setPlayerMaxHp((Integer) userInfo.get("hp"));
        battleSession.setPlayerSpeed((Integer) userInfo.get("speed"));
        battleSession.setPlayerXp((Integer) userInfo.get("xp"));
        battleSession.setPlayerLevel((Integer) userInfo.get("lv"));
        // 포션으로 인한 공격력 증가 조절 (1회만 가능하도록)
        battleSession.setPlayerPower((Integer) userInfo.get("power"));
        battleSession.setPlayerBasePower((Integer) userInfo.get("power"));
        battleSession.setBonusApplied(false);

        Map<String, Object> potion = (Map<String, Object>) userInfo.get("potion");
        battleSession.setPlayerPotionHp((Integer) potion.getOrDefault("bonusHp", 0));
        battleSession.setPlayerPotionPower((Integer) potion.getOrDefault("bonusPower", 0));
        battleSession.setPlayerPotionQuantity((Integer) potion.getOrDefault("quantity", 0));

        battleSession.setMonsterId(monster.getId());
        battleSession.setMonsterName(monster.getName());
        battleSession.setMonsterHp(monster.getMonsterHp());
        battleSession.setMonsterMaxHp(monster.getMonsterHp());
        battleSession.setMonsterPower(monster.getMonsterPower());
        battleSession.setMonsterXp(monster.getMonsterXp());
        battleSession.setMonsterDrops(dropDtos);

        session.setAttribute("battleSession", battleSession);


        // 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("player", Map.of(
                "dexName", userInfo.get("dexName"),
                "attribute", attribute,
                "hp", userInfo.get("hp"),
                "maxHp", userInfo.get("hp"),
                "power", userInfo.get("power"),
                "lv", userInfo.get("lv"),
                "xp", userInfo.get("xp"),
                "charImage", userInfo.get("charImage"),
                "potion", potion,
                "skillImagePath", skillImagePath
        ));
        result.put("monster", Map.of(
                "name", monster.getName(),
                "rank", monster.getRank(),
                "hp", monster.getMonsterHp(),
                "maxHp", monster.getMonsterHp(),
                "power", monster.getMonsterPower(),
                "xp", monster.getMonsterXp(),
                "imagePath", monster.getImagePath()
        ));

        return GamJaResponse.success("전투 시작", result);
    }

    @Transactional
    public GamJaResponse playerAttack(HttpSession session) {
        BattleSession bs = (BattleSession) session.getAttribute("battleSession");
        if (bs == null) return GamJaResponse.fail("전투 중이 아닙니다.");
        if (!bs.isPlayerTurn()) {
            return GamJaResponse.fail("지금은 플레이어 턴이 아닙니다.");
        }
        // === 유저 공격 처리 ===
        boolean isCritical = Math.random() < 0.3;
        int playerRawPower = bs.getPlayerPower();
        int playerDamage = isCritical ? (int) Math.round(playerRawPower * 1.5) : playerRawPower;

        int monsterHp = Math.max(0, bs.getMonsterHp() - playerDamage);
        bs.setMonsterHp(monsterHp);

        // ✅ 턴 종료 → 몬스터 턴으로 전환
        bs.setPlayerTurn(false);
        // ✅ 공격력 원복
        bs.setPlayerPower(bs.getPlayerBasePower());
        bs.setBonusApplied(false);

        session.setAttribute("battleSession", bs);

        boolean victory = (monsterHp <= 0);

        Map<String, Object> result = new HashMap<>();
        result.put("monster", Map.of("hp", monsterHp, "maxHp", bs.getMonsterMaxHp()));
        result.put("playerAttack", Map.of("damage", playerDamage, "isCritical", isCritical));
        result.put("victory", victory);

        return GamJaResponse.success("플레이어 공격 완료", result);
    }

    @Transactional
    public GamJaResponse monsterAttack(HttpSession session) {
        BattleSession bs = (BattleSession) session.getAttribute("battleSession");
        if (bs == null) return GamJaResponse.fail("전투 중이 아닙니다.");

        int playerHp = bs.getPlayerHp();
        double ratio = 0.2 + Math.random() * 0.8;
        int monsterDamage = Math.max(1, (int) Math.round(bs.getMonsterPower() * ratio));

        int newHp = Math.max(0, playerHp - monsterDamage);
        bs.setPlayerHp(newHp);

        bs.setPlayerTurn(true);
        session.setAttribute("battleSession", bs);

        boolean defeat = (newHp <= 0);

        Map<String, Object> result = new HashMap<>();
        result.put("player", Map.of(
                "hp", bs.getPlayerHp(),
                "maxHp", bs.getPlayerMaxHp()
        ));
        result.put("monsterAttack", Map.of(
                "damage", monsterDamage
        ));
        result.put("defeat", defeat);

        return GamJaResponse.success("몬스터 반격 완료", result);
    }

    public record DropResult(List<Map<String, Object>> visibleRewards, List<ItemReward> internalRewards) {}

    @Transactional
    public GamJaResponse endBattle(HttpSession session) {
        BattleSession bs = (BattleSession) session.getAttribute("battleSession");
        if (bs == null) return GamJaResponse.success("전투 중이 아닙니다.", null);

        boolean victory = bs.getMonsterHp() <= 0;
        boolean defeat = bs.getPlayerHp() <= 0;

        Map<String, Object> result = new HashMap<>();

        if (victory) {
            Long userId = bs.getUserId();
            Long dexId = userDtlRepository.findById(userId)
                    .map(dtl -> dtl.getCharacterDexId())
                    .orElse(null);

            if (dexId == null) {
                return GamJaResponse.fail("장착한 캐릭터가 없습니다.");
            }

            // 캐릭터 기본 정보
            Dex dex = dexRepository.findById(dexId)
                    .orElseThrow(() -> new IllegalArgumentException("캐릭터 정보 없음"));

            int beforeXp = bs.getPlayerXp();
            int beforeLevel = bs.getPlayerLevel();
            int gainedXp = bs.getMonsterXp();

            // XP 처리
            UserDexXpDto xpDto = levelService.updateCharacterExp(userId, dexId, gainedXp);

            // 드랍 아이템
            DropResult dropResult = getDropResult(bs.getMonsterId());
            List<Map<String, Object>> visibleDrops = dropResult.visibleRewards();
            List<ItemReward> internalRewards = dropResult.internalRewards();
            processItemRewards(userId, internalRewards);

            result.put("dexName", dex.getName());
            result.put("charImage", dex.getImage());
            result.put("beforeLevel", beforeLevel);
            result.put("afterLevel", xpDto.getLevel());
            result.put("beforeXp", beforeXp);
            result.put("afterXp", xpDto.getXp());
            result.put("maxExp", xpDto.getMaxExp());
            result.put("gainedXp", gainedXp);
            result.put("items", visibleDrops);

            // 활동 로그 등록
            logService.recordCounter(userId, CounterType.MONSTER_KILL, bs.getMonsterId());
            userLogService.recordDailyMonster(userId, bs.getMonsterId());
            corpsTierService.updateCorpsXp(userId, 1);
        }

        // 세션 제거
        session.removeAttribute("battleSession");

        return GamJaResponse.success("전투 종료", result);
    }
    private void processItemRewards(Long userId, List<ItemReward> items) {
        for (ItemReward reward : items) {
            Long itemId = reward.getItem().getId();
            int count = reward.getCount();

            UserInventory inv = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                    .orElseGet(() -> new UserInventory(userId, itemId, 0));
            inv.setQuantity(inv.getQuantity() + count);
            userInventoryRepository.save(inv);
        }
    }

    public DropResult getDropResult(Long monsterId) {
        List<MonsterDrop> drops = monsterDropRepository.findByMonsterId(monsterId);
        List<Map<String, Object>> visible = new ArrayList<>();
        List<ItemReward> internal = new ArrayList<>();

        for (MonsterDrop drop : drops) {
            if (Math.random() * 100 <= drop.getDropRate()) {
                int count = drop.getMinCount() + new Random().nextInt(drop.getMaxCount() - drop.getMinCount() + 1);
                Item item = drop.getItem();

                // 클라이언트용
                visible.add(Map.of(
                        "name", item.getName(),
                        "iconPath", item.getIconPath(),
                        "rarity", item.getRarity().name(),
                        "count", count
                ));

                // 내부 처리용
                internal.add(new ItemReward(item, count));
            }
        }

        return new DropResult(visible, internal);
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
        Optional<UserInventory> inventoryOpt = userInventoryRepository.findByUserIdAndItemId(userId, potionItemId);

        if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() <= 0) {
            return GamJaResponse.fail("보유한 물약이 없습니다.");
        }

        // ✅ 세션에서 전투 상태 가져오기
        BattleSession bs = (BattleSession) request.getSession().getAttribute("battleSession");
        if (bs == null) {
            return GamJaResponse.fail("전투 중이 아닙니다.");
        }
        //  수량 감소 처리
        UserInventory inventory = inventoryOpt.get();
        inventory.setQuantity(inventory.getQuantity() - 1);
        userInventoryRepository.save(inventory);
        int remaining = Math.max(0, inventory.getQuantity());

        //  포션 효과 적용
        int bonusHp = bs.getPlayerPotionHp();
        int bonusPower = bs.getPlayerPotionPower();
        int healedHp = Math.min(bs.getPlayerMaxHp(), bs.getPlayerHp() + bonusHp);
        bs.setPlayerHp(healedHp);
        if (!bs.isBonusApplied() && bonusPower > 0) {
            bs.setPlayerPower(bs.getPlayerBasePower() + bonusPower);
            bs.setBonusApplied(true); // ✅ 중복 방지
        }
        bs.setPlayerPotionQuantity(remaining);

        request.getSession().setAttribute("battleSession", bs);

        return GamJaResponse.success("물약 사용 완료", Map.of(
                "playerHp", healedHp,
                "maxHp", bs.getPlayerMaxHp(),
                "bonusHp", bonusHp,
                "bonusPower", bonusPower,
                "quantity", remaining
        ));
    }

}
