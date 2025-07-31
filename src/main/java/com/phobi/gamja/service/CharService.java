package com.phobi.gamja.service;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.dex.DexOwnedDto;
import com.phobi.gamja.dto.dex.DexOwnedListResponseDto;
import com.phobi.gamja.dto.item.*;
import com.phobi.gamja.dto.user.*;
import com.phobi.gamja.entity.battle.StatBonus;
import com.phobi.gamja.entity.contents.BackgroundImage;
import com.phobi.gamja.entity.contents.CorpsTier;
import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.dex.DexAttribute;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.item.*;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.BackgroundImageRepository;
import com.phobi.gamja.repository.contents.CorpsTierRepository;
import com.phobi.gamja.repository.dex.DexRarityStatRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.item.*;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import com.phobi.gamja.web.config.annotation.SanitizeInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharService {
    @PersistenceContext
    private EntityManager entityManager;
    private final CommonUtil commonUtil;
    private final UtilService utilService;
    private final StatCalculator statCalculator;
    private final LogService logService;
    private final CorpsTierService corpsTierService;

    private final UserDtlRepository userDtlRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final DexRepository dexRepository;
    private final DexRarityStatRepository dexRarityStatRepository;
    private final UserDexRepository userDexRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;
    private final ItemPotionEffectRepository itemPotionEffectRepository;
    private final ItemRepository itemRepository;
    private final UserTitleRepository userTitleRepository;
    private final BackgroundImageRepository backgroundImageRepository;
    private final UserBackgroundRepository userBackgroundRepository;
    private final CorpsTierRepository corpsTierRepository;
    private final UserCorpsRepository userCorpsRepository;
    private final UserEnhancementRepository userEnhancementRepository;
    private final ItemAlchemyOptionRepository itemAlchemyOptionRepository;
    private final UserItemAlchemyOptionRepository userItemAlchemyOptionRepository;


    @Transactional(readOnly = true)
    public GamJaResponse getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return GamJaResponse.fail("착용 중인 캐릭터가 없습니다.");
        }

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage);

        // 칭호 조회
        UserTitle equipped = userTitleRepository.findByIdUserId(userId).stream()
                .filter(UserTitle::isEquipped)
                .findFirst().orElse(null);
        String equippedTitleName = equipped != null ? equipped.getTitle().getName() : null;
        String equippedTitleIcon = equipped != null ? equipped.getTitle().getIconPath() : null;

        // 배경 조회
        BackgroundImage bg = userDtl.getBackgroundImage();
        String backgroundImageUrl = (bg != null) ? bg.getImageUrl() : null;
        String backgroundImageName = (bg != null) ? bg.getName() : null;

        // 감자단 정보
        UserCorps userCorps = userCorpsRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("감자단 정보가 없습니다."));
        StatBonus statBonus = corpsTierService.calculateTierStatBonus(userCorps);
        // ✅ 응답 DTO 구성
        UserCharInfoDto result = new UserCharInfoDto(
                user.getUsername(),
                userDtl, stat, 0, // 기존 필드
                equippedTitleName, equippedTitleIcon, // 칭호
                backgroundImageUrl, backgroundImageName, // 배경
                userCorps// 감자단 정보
                ,statBonus
        );

        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getUserInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Long userId = user.getId();

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return GamJaResponse.fail("착용 중인 캐릭터가 없습니다.");
        }

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage);

        UserTitle equipped = userTitleRepository.findByIdUserId(userId).stream()
                .filter(UserTitle::isEquipped)
                .findFirst().orElse(null);
        String equippedTitleName = equipped != null ? equipped.getTitle().getName() : null;
        String equippedTitleIcon = equipped != null ? equipped.getTitle().getIconPath() : null;

        BackgroundImage bg = userDtl.getBackgroundImage();
        String backgroundImageUrl = (bg != null) ? bg.getImageUrl() : null;
        String backgroundImageName = (bg != null) ? bg.getName() : null;

        UserCorps userCorps = userCorpsRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("감자단 정보가 없습니다."));
        StatBonus statBonus = corpsTierService.calculateTierStatBonus(userCorps);

        UserCharInfoDto result = new UserCharInfoDto(
                user.getUsername(),
                userDtl, stat, 0,
                equippedTitleName, equippedTitleIcon,
                backgroundImageUrl, backgroundImageName,
                userCorps,
                statBonus
        );

        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getBattleInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BattleStatDto result = statCalculator.calculateBattleStat(userId);
        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getBattleInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        BattleStatDto result = statCalculator.calculateBattleStat(user.getId());
        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getLifeInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LifeStatDto result = statCalculator.calculateLifeSkill(userId);
        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getEquipItems(HttpServletRequest request, Map<String, String> payload) {
        Long userId = (Long) request.getAttribute("userId");

        String itemTypeStr = payload.get("itemType");
        String equipSlotStr = payload.get("equipSlot");

        if (itemTypeStr == null || equipSlotStr == null) {
            return GamJaResponse.fail("itemType 또는 equipSlot 누락");
        }

        try {
            Item.ItemType itemType = Item.ItemType.valueOf(itemTypeStr);
            if (equipSlotStr.startsWith("BADGE")) {
                equipSlotStr = "BADGE";
            }
            EquipmentSlot equipSlot = EquipmentSlot.valueOf(equipSlotStr);

            if (equipSlot == EquipmentSlot.POTION) {
                return GamJaResponse.success("정상 조회", handlePotionItems(userId, equipSlot));
            }

            return switch (itemType) {
                case EQUIP_BATTLE -> GamJaResponse.success("정상 조회", handleBattleEquipItems(userId, itemType, equipSlot));
                case EQUIP_GATHER -> GamJaResponse.success("정상 조회", handleGatherEquipItems(userId, itemType, equipSlot));
                default -> GamJaResponse.success("정상 조회", null);
            };

        } catch (IllegalArgumentException e) {
            return GamJaResponse.fail("itemType 또는 equipSlot 값이 잘못되었습니다.");
        }
    }

    private List<EquipPotionDto> handlePotionItems(Long userId, EquipmentSlot equipSlot) {
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);
        List<Long> itemIds = inventoryList.stream().map(UserInventory::getItemId).toList();

        List<Item> items = itemRepository.findByIdInAndEquipSlot(itemIds, equipSlot);
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, i -> i));

        List<ItemPotionEffect> potionEffects = itemPotionEffectRepository.findByItemIdIn(itemMap.keySet());
        Map<Long, ItemPotionEffect> effectMap = potionEffects.stream()
                .collect(Collectors.toMap(ItemPotionEffect::getItemId, e -> e));

        return inventoryList.stream()
                .filter(inv -> itemMap.containsKey(inv.getItemId()) && inv.getQuantity() > 0)
                .map(inv -> {
                    Item item = itemMap.get(inv.getItemId());
                    ItemPotionEffect eff = effectMap.getOrDefault(item.getId(), new ItemPotionEffect());

                    return EquipPotionDto.builder()
                            .itemId(item.getId())
                            .itemName(item.getName())
                            .itemPath(item.getIconPath())
                            .description(item.getDescription())
                            .bonusPower(eff.getBonusPower())
                            .bonusHp(eff.getHealHp())
                            .quantity(inv.getQuantity())
                            .build();
                }).toList();
    }

    private List<EquipBattleDto> handleBattleEquipItems(Long userId, Item.ItemType itemType, EquipmentSlot equipSlot) {
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);
        List<Long> itemIds = inventoryList.stream().map(UserInventory::getItemId).toList();

        List<Item> items = itemRepository.findByIdInAndItemTypeAndEquipSlot(itemIds, itemType, equipSlot);
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, i -> i));

        List<UserEnhancement> enhancements = userEnhancementRepository.findByUserId(userId);
        Map<Long, UserEnhancement> enhancementMap = enhancements.stream()
                .collect(Collectors.toMap(UserEnhancement::getItemId, e -> e));

        List<ItemStatBonus> baseStats = itemStatBonusRepository.findByItemIdIn(itemMap.keySet());
        Map<Long, ItemStatBonus> baseStatMap = baseStats.stream()
                .collect(Collectors.toMap(ItemStatBonus::getItemId, s -> s));

        List<UserItemAlchemyOption> alchemyOptions = userItemAlchemyOptionRepository.findByUserIdAndItemIdIn(userId, itemMap.keySet());
        Map<Long, List<UserItemAlchemyOption>> alchemyMap = alchemyOptions.stream()
                .collect(Collectors.groupingBy(UserItemAlchemyOption::getItemId));

        return inventoryList.stream()
                .filter(inv -> itemMap.containsKey(inv.getItemId()) && inv.getQuantity() > 0)
                .map(inv -> {
                    Item item = itemMap.get(inv.getItemId());
                    Long itemId = item.getId();
                    boolean equipped = userEquipmentRepository.existsByUserIdAndItemId(userId, itemId);

                    int bonusPower = 0, bonusHp = 0, bonusSpeed = 0, level = 0;
                    if (enhancementMap.containsKey(itemId)) {
                        var enh = enhancementMap.get(itemId);
                        bonusPower = enh.getBonusPower();
                        bonusHp = enh.getBonusHp();
                        bonusSpeed = enh.getBonusSpeed();
                        level = enh.getEnhancementLevel();
                    } else {
                        var base = baseStatMap.getOrDefault(itemId, new ItemStatBonus());
                        bonusPower = base.getBonusPower();
                        bonusHp = base.getBonusHp();
                        bonusSpeed = base.getBonusSpeed();
                    }

                    List<AlchemyOptionDto> alchemyOptionList = Optional.ofNullable(alchemyMap.get(itemId))
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(opt -> new AlchemyOptionDto(
                                    opt.getOptionType(),
                                    opt.getValueType(),
                                    opt.getOptionValue(),
                                    opt.getDescription()
                            ))
                            .toList();

                    return EquipBattleDto.builder()
                            .itemId(itemId)
                            .itemName(item.getName())
                            .itemPath(item.getIconPath())
                            .description(item.getDescription())
                            .bonusPower(bonusPower)
                            .bonusHp(bonusHp)
                            .bonusSpeed(bonusSpeed)
                            .enhancementLevel(level)
                            .equipped(equipped)
                            .alchemyOptions(alchemyOptionList)
                            .quantity(inv.getQuantity())
                            .build();
                }).toList();
    }

    private List<EquipGatherDto> handleGatherEquipItems(Long userId, Item.ItemType itemType, EquipmentSlot equipSlot) {
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);
        List<Long> itemIds = inventoryList.stream().map(UserInventory::getItemId).toList();

        List<Item> items = itemRepository.findByIdInAndItemTypeAndEquipSlot(itemIds, itemType, equipSlot);
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, i -> i));

        List<ItemSkillBonus> skillBonuses = itemSkillBonusRepository.findByItemIdIn(itemMap.keySet());
        Map<Long, ItemSkillBonus> skillMap = skillBonuses.stream()
                .collect(Collectors.toMap(ItemSkillBonus::getItemId, s -> s));

        return inventoryList.stream()
                .filter(inv -> itemMap.containsKey(inv.getItemId()) && inv.getQuantity() > 0)
                .map(inv -> {
                    Item item = itemMap.get(inv.getItemId());
                    ItemSkillBonus skill = skillMap.getOrDefault(item.getId(), new ItemSkillBonus());

                    return EquipGatherDto.builder()
                            .itemId(item.getId())
                            .itemName(item.getName())
                            .itemPath(item.getIconPath())
                            .description(item.getDescription())
                            .bonusSkillFish(skill.getFishing())
                            .bonusSkillGathering(skill.getGathering())
                            .bonusSkillWoodCutting(skill.getWoodcutting())
                            .bonusSkillMining(skill.getMining())
                            .bonusSkillMaking(skill.getMaking())
                            .quantity(inv.getQuantity())
                            .build();
                }).toList();
    }


    @Transactional
    public void setEquipItems(HttpServletRequest request, Map<String, String> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long itemId = Long.valueOf(payload.get("itemId"));
        utilService.equipItem(userId,itemId);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse setCharacterImage(HttpServletRequest request, Map<String, Long> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long dexId = payload.get("dexId");

        // 1. 감자 도감 보유 여부 확인
        boolean owned = userDexRepository.existsByUserIdAndDexId(userId, dexId);
        if (!owned) {
            return GamJaResponse.fail("미획득한 감자는 설정할 수 없습니다.");
        }

        // 2. 유저 정보 조회 및 대표 감자 설정
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        userDtl.setCharacterDexId(dexId);
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));
        userDtlRepository.save(userDtl);

        // 3. 스탯 정보 조회
        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalStateException("스탯 정보를 가져올 수 없습니다."));

        return GamJaResponse.success("대표 감자가 설정되었습니다.",null);
    }
    @Transactional
    @SanitizeInput
    public GamJaResponse getGacha(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 1. 인벤토리에서 미감정 감자 확인
        final Long UNAPPRAISED_POTATO_ID = 53L;

        // 1. 미감정 감자 보유 확인 및 수량 차감
        UserInventory unappraised = userInventoryRepository.findByUserIdAndItemId(userId, UNAPPRAISED_POTATO_ID)
                .orElse(null);
        if (unappraised == null || unappraised.getQuantity() <= 0) {
            return GamJaResponse.fail("미감정 감자가 부족합니다.");
        }
        // 수량 차감
        unappraised.setQuantity(unappraised.getQuantity() - 1);
        userInventoryRepository.save(unappraised);

        // 2. 확률 추첨
        DexRarityStat.Rarity selectedRarity = DexRarityStat.Rarity.roll();
        DexRarityStat selectedRarityStat = dexRarityStatRepository.findById(selectedRarity)
                .orElseThrow(() -> new IllegalStateException("해당 rarity가 존재하지 않습니다: " + selectedRarity));


        List<Dex> candidates = dexRepository.getGachaCandidates(selectedRarity);
        if (candidates.isEmpty()) {
            return GamJaResponse.fail("해당 등급의 감자가 없습니다.");
        }
        // 3. 감자 하나 랜덤 선택
        Dex selected = candidates.get(new Random().nextInt(candidates.size()));

        // 4. 중복 여부 확인
        boolean isDuplicate = userDexRepository.existsByUserIdAndDexId(userId, selected.getId());
        int gainedFragments = 0;

        logService.recordCounter(userId, CounterType.CHARACTER_DRAW, selected.getId());
        if (isDuplicate) {
            // 5. 중복 → 친밀도 +1
            UserDexStatId statId = new UserDexStatId(userId, selected.getId());
            UserDexStat stat = userDexStatRepository.findById(statId)
                    .orElseGet(() -> UserDexStat.builder()
                            .id(statId)
                            .user(userRepository.getReferenceById(userId))
                            .dex(dexRepository.getReferenceById(selected.getId()))
                            .level(1)
                            .xp(0)
                            .maxExp(100)
                            .power(0)
                            .hp(0)
                            .speed(0)
                            .affinity(0)
                            .build());

            stat.setAffinity(stat.getAffinity() + 1);
            userDexStatRepository.save(stat);
        } else {
            // 6. 보유 감자에 등록
            UserDex newDex = UserDex.of(userId, selected);
            userDexRepository.save(newDex);
            UserDexStatId statId = new UserDexStatId(userId, selected.getId());
            UserDexStat newStat = UserDexStat.builder()
                    .id(statId)
                    .user(userRepository.getReferenceById(userId))
                    .dex(dexRepository.getReferenceById(selected.getId()))
                    .level(1)
                    .xp(0)
                    .maxExp(100)
                    .power(0)
                    .hp(0)
                    .speed(0)
                    .affinity(0)
                    .build();
            userDexStatRepository.save(newStat);
        }
        // 7. 응답 데이터 구성
        DexAttribute attr = selected.getAttribute();
        Map<String, Object> result = new HashMap<>();
        result.put("resultType", isDuplicate ? "DUPLICATE" : "NEW");
        result.put("dexId", selected.getId());
        result.put("name", selected.getName());
        result.put("rarity", selected.getRarity());
        result.put("image", selected.getImage());
        result.put("attribute", attr.getName());
        result.put("attributeIconPath", attr.getIconPath());
        result.put("desc", selected.getDescription());
        result.put("pieceGained", gainedFragments);

        /* 감자단 경험치 상승 */
        corpsTierService.updateCorpsXp(userId, 10);

        return GamJaResponse.success("감자 뽑기 성공", result);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse getMultiGacha(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        final Long UNAPPRAISED_POTATO_ID = 53L;
        final int GACHA_COUNT = 10;

        // 인벤토리 확인
        UserInventory unappraised = userInventoryRepository.findByUserIdAndItemId(userId, UNAPPRAISED_POTATO_ID)
                .orElse(null);

        if (unappraised == null || unappraised.getQuantity() < GACHA_COUNT) {
            return GamJaResponse.fail("미감정 감자가 10개 이상 필요합니다.");
        }

        // 감자 수량 차감
        unappraised.setQuantity(unappraised.getQuantity() - GACHA_COUNT);
        userInventoryRepository.save(unappraised);

        List<Map<String, Object>> results = new ArrayList<>();

        for (int i = 0; i < GACHA_COUNT; i++) {
            // 확률 추첨
            DexRarityStat.Rarity selectedRarity = DexRarityStat.Rarity.roll();
            DexRarityStat selectedRarityStat = dexRarityStatRepository.findById(selectedRarity)
                    .orElseThrow(() -> new IllegalStateException("해당 rarity가 존재하지 않습니다: " + selectedRarity));

            List<Dex> candidates = dexRepository.getGachaCandidates(selectedRarity);
            if (candidates.isEmpty()) continue; // 없으면 스킵

            Dex selected = candidates.get(new Random().nextInt(candidates.size()));

            boolean isDuplicate = userDexRepository.existsByUserIdAndDexId(userId, selected.getId());
            int gainedFragments = 0;

            logService.recordCounter(userId, CounterType.CHARACTER_DRAW, selected.getId());

            if (isDuplicate) {
                UserDexStatId statId = new UserDexStatId(userId, selected.getId());
                UserDexStat stat = userDexStatRepository.findById(statId)
                        .orElseGet(() -> UserDexStat.builder()
                                .id(statId)
                                .user(userRepository.getReferenceById(userId))
                                .dex(dexRepository.getReferenceById(selected.getId()))
                                .level(1)
                                .xp(0)
                                .maxExp(100)
                                .power(0)
                                .hp(0)
                                .speed(0)
                                .affinity(0)
                                .build());

                stat.setAffinity(stat.getAffinity() + 1);
                userDexStatRepository.save(stat);
            } else {
                UserDex newDex = UserDex.of(userId, selected);
                userDexRepository.save(newDex);

                UserDexStatId statId = new UserDexStatId(userId, selected.getId());
                UserDexStat newStat = UserDexStat.builder()
                        .id(statId)
                        .user(userRepository.getReferenceById(userId))
                        .dex(dexRepository.getReferenceById(selected.getId()))
                        .level(1)
                        .xp(0)
                        .maxExp(100)
                        .power(0)
                        .hp(0)
                        .speed(0)
                        .affinity(0)
                        .build();
                userDexStatRepository.save(newStat);
            }

            // 결과 구성
            DexAttribute attr = selected.getAttribute();
            Map<String, Object> result = new HashMap<>();
            result.put("resultType", isDuplicate ? "DUPLICATE" : "NEW");
            result.put("dexId", selected.getId());
            result.put("name", selected.getName());
            result.put("rarity", selected.getRarity());
            result.put("image", selected.getImage());
            result.put("attribute", attr.getName());
            result.put("attributeIconPath", attr.getIconPath());
            result.put("desc", selected.getDescription());
            result.put("pieceGained", gainedFragments);
            results.add(result);
        }

        // 경험치 10개 → 100 누적
        corpsTierService.updateCorpsXp(userId, GACHA_COUNT * 10);

        return GamJaResponse.success("10개 감정 성공", results);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse ticketCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 1. 인벤토리에서 미감정 감자 확인
        final Long UNAPPRAISED_POTATO_ID = 53L;

        // 1. 미감정 감자 및 미감정 감자 조각 보유 개수 확인
        UserInventory unappraisedCount = userInventoryRepository.findByUserIdAndItemId(userId, UNAPPRAISED_POTATO_ID)
                .orElse(null);

        // 7. 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("unappraisedCount", unappraisedCount != null ? unappraisedCount.getQuantity() : 0);

        return GamJaResponse.success("미감정 감자 조회 성공", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getOwnedDex(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 1. 보유 감자 ID 리스트
        List<UserDex> userDexList = userDexRepository.findByUserId(userId);

        // 2. 대표 감자 ID
        Long selectedDexId = userDtlRepository.findById(userId)
                .map(UserDtl::getCharacterDexId)
                .orElse(null);

        // 3. 각각의 캐릭터에 대해 DTO 생성
        List<DexOwnedDto> ownedDexList = userDexList.stream().map(userDex -> {
                    Dex dex = userDex.getDex();
                    UserDexStat stat = userDexStatRepository
                            .findByUserIdAndDexId(userId, dex.getId())
                            .orElse(null);
                    DexAttribute attr = dex.getAttribute();
                    DexRarityStat rarityStat = dex.getRarity();
                    return DexOwnedDto.builder()
                            .dexId(dex.getId())
                            .dexImage(dex.getImage())
                            .dexName(dex.getName())
                            .attribute(attr != null ? attr.getName() : null)
                            .attributeIconPath(attr != null ? attr.getIconPath() : null)
                            .rarity(rarityStat.getRarity().name())
                            .level(stat != null ? stat.getLevel() : 1)
                            .xp(stat != null ? stat.getXp() : 0)
                            .maxExp(stat != null ? stat.getMaxExp() : 100)
                            .affinity(stat != null ? stat.getAffinity() : 0)
                            .power(stat != null ? stat.getPower() : 0)
                            .hp(stat != null ? stat.getHp() : 0)
                            .speed(stat != null ? stat.getSpeed() : 0)
                            .selected(dex.getId().equals(selectedDexId))
                            .build();
                })
                .sorted((a, b) -> {
                    // 1. selected 우선 정렬 (true가 먼저)
                    int selectedCompare = Boolean.compare(!a.isSelected(), !b.isSelected());
                    if (selectedCompare != 0) return selectedCompare;

                    // 2. rarity 높은 순 정렬
                    return Integer.compare(
                            RARITY_ORDER.getOrDefault(b.getRarity(), 0),
                            RARITY_ORDER.getOrDefault(a.getRarity(), 0)
                    );
                })
                .collect(Collectors.toList());

        // 전체 감자 도감 수
        int totalDexCount = (int) dexRepository.count();
        // 보유 감자 수
        int ownedDexCount = userDexList.size();

        DexOwnedListResponseDto result = DexOwnedListResponseDto.builder()
                .representDex(selectedDexId)
                .totalDexCount(totalDexCount)
                .ownedDexCount(ownedDexCount)
                .ownedDexList(ownedDexList)
                .build();

        return GamJaResponse.success("보유 감자 리스트 조회 완료", result);
    }

    private static final Map<String, Integer> RARITY_ORDER = Map.of(
            "COMMON", 1,
            "UNCOMMON", 2,
            "RARE", 3,
            "EPIC", 4,
            "LEGENDARY", 5
    );

    @Transactional(readOnly = true)
    public GamJaResponse getBackgroundList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<BackgroundImage> all = backgroundImageRepository.findByEnabledTrue();
        List<UserBackground> ownedList = userBackgroundRepository.findByUserId(userId);
        Set<Long> ownedIds = ownedList.stream()
                .map(bg -> bg.getBackgroundImage().getId())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = all.stream()
                .map(bg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", bg.getId());
                    map.put("name", bg.getName());
                    map.put("imageUrl", bg.getImageUrl());
                    map.put("owned", ownedIds.contains(bg.getId()));
                    return map;
                })
                .toList();

        return GamJaResponse.success("배경 이미지 목록", result);
    }

    @Transactional
    public GamJaResponse setBackgroundList(Map<String, Long> payload, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long backgroundId = payload.get("backgroundId");

        // 유효한 배경인지 확인
        BackgroundImage bg = backgroundImageRepository.findById(backgroundId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배경입니다."));

        // 보유한 배경인지 확인
        boolean owned = userBackgroundRepository.findByUserId(userId).stream()
                .anyMatch(ub -> ub.getBackgroundImage().getId().equals(backgroundId));
        if (!owned) {
            return GamJaResponse.fail("해당 배경을 보유하고 있지 않습니다.");
        }

        // 적용
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        userDtl.setBackgroundImage(bg);
        userDtlRepository.save(userDtl);

        return GamJaResponse.success("배경이 변경되었습니다.", null);
    }
    public GamJaResponse tierList(HttpServletRequest request) {
        List<CorpsTier> tiers = corpsTierRepository.findAllByOrderByTierIdAsc();
        return GamJaResponse.success("감자단 랭크 정보 .",tiers);
    }

}
