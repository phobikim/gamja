package com.phobi.gamja.service;

import com.phobi.gamja.dto.battle.BossPatternMapper;
import com.phobi.gamja.dto.battle.PatternDTO;
import com.phobi.gamja.dto.user.UserDexXpDto;
import com.phobi.gamja.entity.battle.*;
import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.dex.DexSkill;
import com.phobi.gamja.entity.dex.DexSkillImage;
import com.phobi.gamja.entity.item.ItemReward;
import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserDex;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.battle.MonsterBossPatternRepository;
import com.phobi.gamja.repository.battle.MonsterMapRepository;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.dex.DexSkillImageRepository;
import com.phobi.gamja.repository.dex.DexSkillRepository;
import com.phobi.gamja.repository.user.UserDexRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BossBattleService {

    private static final String ATTR_ALLY_FLAGS  = "bossAllyFlags";   // Map<String,Boolean>
    private static final String ATTR_ALLY_IMAGES = "bossAllyImages";  // Map<String,String> (1회용이긴 하지만 유지해도 무방)

    // 역할 키(한 번씩만 발동)
    private static final String KEY_HELP_HEAL    = "ALLY_HEAL_USED";     // 플레이어 HP<30% → 전부 회복
    private static final String KEY_HELP_RESTORE = "ALLY_RESTORE_USED";  // 디버프로 떨어진 공격력 정상화
    private static final String KEY_HELP_ATTACK  = "ALLY_ATTACK_USED";   // 보스 HP<20% → 1회 대리공격


    private static final String KEY_RAW   = "RAW_HELPED";    // 생감자: 체력 전부 회복(플레이어 HP<30%)
    private static final String KEY_BAKED = "BAKED_HELPED";  // 구운감자: 디버프로 낮아진 공격력을 정상화
    private static final String KEY_FRIED = "FRIED_HELPED";  // 튀김감자: 보스 HP<20%일 때 1회 공격(플레이어 공격력의 절반)

    // 게임 내 속성명(네가 쓰는 명칭에 맞춰 조정)
    private static final String ATTR_RAW_NAME   = "생감자";
    private static final String ATTR_BAKED_NAME = "구운감자";
    private static final String ATTR_FRIED_NAME = "튀김감자";

    private static final String ATTR_BATTLE_SESSION   = "battleSession";
    private static final String ATTR_BOSS_PHASE       = "bossPhase";
    private static final String ATTR_BOSS_PATTERNS    = "bossPatterns";
    private static final String ATTR_BOSS_COOLDOWNS   = "bossCooldowns";
    private static final String ATTR_BOSS_TURN        = "bossTurn";
    private static final String ATTR_BOSS_CURR_SKILL  = "bossCurrentSkill";
    private static final String ATTR_PLAYER_META      = "playerMeta";
    private static final String ATTR_BOSS_META        = "bossMeta";
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;

    private final MonsterMapRepository monsterMapRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterBossPatternRepository bossPatternRepository;

    private final DexSkillRepository dexSkillRepository;
    private final DexSkillImageRepository dexSkillImageRepository;
    private final UserDexRepository userDexRepository;
    private final CommonUtil commonUtil;
    private final LogService logService;
    private final UserLogService userLogService;
    private final CorpsTierService corpsTierService;
    private final StatCalculator statCalculator;
    private final ActionService actionService;
    private final LevelService levelService;
    private final BattleService battleService;
    private final BossPatternMapper bossPatternMapper;
    String skillImagePath = null;
    private PatternDTO toDto(MonsterBossPattern p) {
        return bossPatternMapper.toDto(p);
    }
    private Optional<PatternDTO> pickRandomPatternForPhase(
            Monster boss,
            int phase,
            Map<Long, Integer> cooldowns
    ) {
        List<MonsterBossPattern> raw =
                bossPatternRepository.findByMonsterAndPhaseOrderByPhaseOrderAsc(boss, phase);

        List<PatternDTO> candidates = raw.stream()
                .filter(MonsterBossPattern::isEnabled)
                .map(this::toDto)
                .filter(p -> cooldowns.getOrDefault(p.getId(), 0) == 0) // 쿨다운만 체크
                .toList();

        if (candidates.isEmpty()) return Optional.empty();
        return Optional.of(candidates.get((int)(Math.random() * candidates.size())));
    }

    @Transactional(readOnly = true)
    public MonsterMap getBossMap() {
        return monsterMapRepository.findAll().stream()
                .filter(map -> map.getMapDifficulty() == MonsterMap.MapDifficulty.BOSS)
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Monster> getBossMonsters(Long mapId) {
        return monsterRepository.findByMapIdAndEnabledIsTrue(mapId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBossPatterns(Monster monster) {
        Map<String, Object> result = new HashMap<>();

        // 1. 기본 반복 패턴 (phase = 0, is_repeatable = true)
        List<MonsterBossPattern> basicPatterns = bossPatternRepository
                .findByMonsterAndPhaseOrderByPhaseOrderAsc(monster, 0)
                .stream()
                .filter(MonsterBossPattern::isRepeatable)
                .collect(Collectors.toList());

        // 2. 페이즈별 패턴 (phase > 0)
        List<MonsterBossPattern> phasePatterns = bossPatternRepository
                .findByMonsterAndPhaseGreaterThanOrderByPhaseAscPhaseOrderAsc(monster, 0);

        // 3. 페이즈별로 그룹핑
        Map<Integer, List<Map<String, Object>>> phasePatternMap = new LinkedHashMap<>();
        for (MonsterBossPattern pattern : phasePatterns) {
            int phase = pattern.getPhase();
            phasePatternMap.computeIfAbsent(phase, k -> new ArrayList<>())
                    .add(convertPatternToMap(pattern));
        }

        result.put("basicPatterns", basicPatterns.stream().map(this::convertPatternToMap).collect(Collectors.toList()));
        result.put("phasePatterns", phasePatternMap);
        return result;
    }
    private int calcPhaseByHp(int currentHp, int maxHp) {
        if (maxHp <= 0) return 1;
        double pct = (currentHp * 100.0) / maxHp;
        if (pct > 70.0) return 1;
        if (pct > 40.0) return 2;
        return 3;
    }

    private Map<String, Object> convertPatternToMap(MonsterBossPattern pattern) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dialogue", pattern.getDialogue());
        map.put("type", pattern.getPatternType().name());
        map.put("value", pattern.getPatternValue());
        map.put("repeatable", pattern.isRepeatable());
        map.put("cooldown", pattern.getCooldown());
        return map;
    }

    @Transactional
    public GamJaResponse startBossBattle(HttpSession session) {
        Long userId = commonUtil.getUserId(session);

        // 이미 있으면 유지
        BattleSession cached = (BattleSession) session.getAttribute(ATTR_BATTLE_SESSION);
        Map<String, Object> cachedPatterns = (Map<String, Object>) session.getAttribute(ATTR_BOSS_PATTERNS);
        Integer cachedPhase = (Integer) session.getAttribute(ATTR_BOSS_PHASE);
        if (cached != null && cachedPatterns != null && cachedPhase != null) {
            Map<String, Object> cachedPotion = new HashMap<>();
            cachedPotion.put("bonusHp",    cached.getPlayerPotionHp());
            cachedPotion.put("bonusPower", cached.getPlayerPotionPower());
            cachedPotion.put("quantity",   cached.getPlayerPotionQuantity());
            cachedPotion.put("itemPath",   cached.getPlayerPotionItemPath());

            return GamJaResponse.success("보스 전투 상태 유지",
                    buildStartPayload(session, cached, cachedPatterns, cachedPotion, cachedPhase));
        }
        // 1) 보스 맵/몬스터
        MonsterMap bossMap = getBossMap();
        if (bossMap == null) return GamJaResponse.fail("보스 맵이 존재하지 않습니다.");

        List<Monster> monsters = getBossMonsters(bossMap.getId());
        if (monsters.isEmpty()) return GamJaResponse.fail("보스 몬스터가 존재하지 않습니다.");
        Monster boss = monsters.get(0);

        // 2) 유저 전투 스탯
        GamJaResponse userRes = battleService.getUserBattleStat(userId);
        if (!userRes.isSuccess()) return userRes;
        Map<String, Object> userInfo = (Map<String, Object>) userRes.getData();
        // 유저 스킬 이미지 (BASIC 타입 1개)
        String attribute = (String) userInfo.get("attribute");
        DexSkill dexSkill = dexSkillRepository.findByDexAttributeAndSkillType(attribute, BattleSkill.Type.BASIC)
                .stream().findFirst().orElse(null);

        if (dexSkill != null) {
            skillImagePath = dexSkillImageRepository.findBySkillId(dexSkill.getId())
                    .stream()
                    .map(DexSkillImage::getImagePath)
                    .findFirst().orElse(null);
        }
        // ===== (A) 플레이어/보스 메타 세팅 =====
        Map<String, Object> playerMeta = new HashMap<>();
        playerMeta.put("attribute", userInfo.get("attribute"));
        playerMeta.put("dexName",   userInfo.get("dexName"));
        playerMeta.put("charImage", userInfo.get("charImage"));

        session.setAttribute(ATTR_PLAYER_META, playerMeta);

        Map<String, Object> bossMeta = new HashMap<>();
        bossMeta.put("imagePath", boss.getImagePath());
        bossMeta.put("name",      boss.getName());
        session.setAttribute(ATTR_BOSS_META, bossMeta);
        // ================================================

        // 3) BattleSession 세팅
        BattleSession bs = new BattleSession();
        bs.setUserId(userId);
        bs.setPlayerHp((Integer) userInfo.get("hp"));
        bs.setPlayerMaxHp((Integer) userInfo.get("hp"));
        bs.setPlayerXp((Integer) userInfo.get("xp"));
        bs.setPlayerLevel((Integer) userInfo.get("lv"));
        bs.setPlayerPower((Integer) userInfo.get("power"));
        bs.setPlayerBasePower((Integer) userInfo.get("power"));
        bs.setBonusApplied(false);

        Map<String, Object> potion = (Map<String, Object>) userInfo.getOrDefault("potion", Map.of());
        bs.setPlayerPotionHp((Integer) potion.getOrDefault("bonusHp", 0));
        bs.setPlayerPotionPower((Integer) potion.getOrDefault("bonusPower", 0));
        bs.setPlayerPotionQuantity((Integer) potion.getOrDefault("quantity", 0));
        bs.setPlayerPotionItemPath((String) potion.getOrDefault("itemPath", ""));

        Map<String, Double> special = (Map<String, Double>) userInfo.getOrDefault("specialOptions", Map.of());
        bs.setCritRate(special.getOrDefault("CRIT_RATE", 0.0));
        bs.setCritDmg(special.getOrDefault("CRIT_DMG", 0.0));
        bs.setExpGain(special.getOrDefault("EXP_GAIN", 0.0));
        bs.setGoldGain(special.getOrDefault("GOLD_GAIN", 0.0));
        bs.setDefense((Integer) userInfo.getOrDefault("defense", 0));

        bs.setMonsterId(boss.getId());
        bs.setMonsterName(boss.getName());
        bs.setMonsterHp(boss.getMonsterHp());
        bs.setMonsterMaxHp(boss.getMonsterHp());
        bs.setMonsterPower(boss.getMonsterPower());
        bs.setMonsterXp(boss.getMonsterXp());
        bs.setPlayerTurn(true);

        int phase = calcPhaseByHp(bs.getMonsterHp(), bs.getMonsterMaxHp());
        Map<String, Object> patterns = getBossPatterns(boss);

        // 세션 저장
        session.setAttribute(ATTR_BATTLE_SESSION, bs);
        session.setAttribute(ATTR_BOSS_PHASE, phase);
        session.setAttribute(ATTR_BOSS_PATTERNS, patterns);
        session.setAttribute(ATTR_BOSS_COOLDOWNS, new HashMap<Long, Integer>());
        session.setAttribute(ATTR_BOSS_TURN, 0);

        // 페이즈0 후보에서 쿨다운 0인 패턴 랜덤 선택
        Map<Long, Integer> cooldowns = (Map<Long, Integer>) session.getAttribute(ATTR_BOSS_COOLDOWNS);
        Optional<PatternDTO> initialSkillOpt = pickRandomPatternForPhase(boss, 0, cooldowns);

        if (initialSkillOpt.isPresent()) {
            PatternDTO skill = initialSkillOpt.get();
            // 세션에는 패턴 DTO를 그대로 저장 (지연 정보 없음)
            session.setAttribute(ATTR_BOSS_CURR_SKILL, skill);
        } else {
            session.removeAttribute(ATTR_BOSS_CURR_SKILL);
        }
        //조력 플래그 초기화
        getAllyFlags(session);
        getAllyImages(session);
        return GamJaResponse.success("보스 전투 시작",
                buildStartPayload(session, bs, patterns, potion, phase));
    }


    private Map<String, Object> buildStartPayload(
            HttpSession session,
            BattleSession bs,
            Map<String, Object> patterns,
            Map<String, Object> potion,
            int phase
    ) {
        Map<String, Object> player = new HashMap<>();
        player.put("hp", bs.getPlayerHp());
        player.put("maxHp", bs.getPlayerMaxHp());
        player.put("power", bs.getPlayerPower());
        player.put("lv", bs.getPlayerLevel());
        player.put("xp", bs.getPlayerXp());
        player.put("defense", bs.getDefense());
        player.put("skillImagePath", skillImagePath);
        player.put("potion", potion);

        Map<String, Object> playerMeta = (Map<String, Object>) session.getAttribute(ATTR_PLAYER_META);
        if (playerMeta != null) player.putAll(playerMeta);

        Map<String, Object> monster = new HashMap<>();
        monster.put("id", bs.getMonsterId());
        monster.put("name", bs.getMonsterName());
        monster.put("hp", bs.getMonsterHp());
        monster.put("maxHp", bs.getMonsterMaxHp());
        monster.put("power", bs.getMonsterPower());
        monster.put("xp", bs.getMonsterXp());

        Map<String, Object> bossMeta = (Map<String, Object>) session.getAttribute(ATTR_BOSS_META);
        if (bossMeta != null) monster.putAll(bossMeta);

        Map<String, Object> result = new HashMap<>();
        result.put("player", player);
        result.put("monster", monster);
        result.put("phase", phase);
        result.put("patterns", patterns);

        // 초기 선택 스킬(딜레이 정보 없이 DTO 그대로)
        PatternDTO curr = (PatternDTO) session.getAttribute(ATTR_BOSS_CURR_SKILL);
        if (curr != null) {
            result.put("initialBossSkill", curr);
        }

        return result;
    }

    @Transactional
    public GamJaResponse playerAttack(HttpSession session) {
        BattleSession bs = (BattleSession) session.getAttribute("battleSession");
        if (bs == null) return GamJaResponse.fail("전투 중이 아닙니다.");
        if (!bs.isPlayerTurn()) return GamJaResponse.fail("지금은 플레이어 턴이 아닙니다.");

        // 크리티컬 여부
        boolean isCritical = Math.random() < (bs.getCritRate() / 100.0);
        int power = bs.getPlayerPower();
        double critMultiplier = (bs.getCritDmg() + 10) / 100.0;
        int damage = isCritical ? (int) (power * (1 + critMultiplier)) : power;

        int newHp = Math.max(0, bs.getMonsterHp() - damage);
        bs.setMonsterHp(newHp);
        bs.setPlayerTurn(false); // 턴 전환
        bs.setPlayerPower(bs.getPlayerBasePower());
        session.setAttribute("battleSession", bs);

        boolean victory = newHp <= 0;

        Map<String, Object> data = new HashMap<>();
        data.put("monster", Map.of("hp", newHp, "maxHp", bs.getMonsterMaxHp()));
        data.put("playerAttack", Map.of("damage", damage, "isCritical", isCritical));
        data.put("victory", victory);

        // === 조력: 보스 HP 20% 미만이면 1회 대리공격 ===
        Map<String, Boolean> flags = getAllyFlags(session);
        List<Map<String, Object>> allyAssists = new ArrayList<>();
        
        return GamJaResponse.success("보스에게 공격 완료", data);
    }

    @Transactional
    public GamJaResponse bossTurn(HttpSession session) {
        BattleSession bs = (BattleSession) session.getAttribute(ATTR_BATTLE_SESSION);
        if (bs == null) return GamJaResponse.fail("전투 중이 아닙니다.");
        if (bs.isPlayerTurn()) return GamJaResponse.fail("지금은 보스 턴이 아닙니다.");

        @SuppressWarnings("unchecked")
        Map<Long, Integer> cooldowns = (Map<Long, Integer>) session.getAttribute(ATTR_BOSS_COOLDOWNS);
        if (cooldowns == null) {
            cooldowns = new HashMap<>();
            session.setAttribute(ATTR_BOSS_COOLDOWNS, cooldowns);
        }

        // 1) 턴 시작: 기존 쿨다운 전체 -1
        if (!cooldowns.isEmpty()) {
            Map<Long, Integer> updated = new HashMap<>();
            for (Map.Entry<Long,Integer> e : cooldowns.entrySet()) {
                int left = Math.max(0, e.getValue() - 1);
                updated.put(e.getKey(), left);
            }
            cooldowns.clear();
            cooldowns.putAll(updated);
        }

        // 2) 현재 선택된 스킬 읽기 (없으면 현재 페이즈에서 즉시 픽)
        PatternDTO skill = (PatternDTO) session.getAttribute(ATTR_BOSS_CURR_SKILL);
        if (skill == null) {
            Monster boss = new Monster();
            boss.setId(bs.getMonsterId()); // 레포에서 다시 가져오는 게 정석이지만 ID만 세팅해도 JPA 파라미터 매칭 OK
            int currPhase = calcPhaseByHp(bs.getMonsterHp(), bs.getMonsterMaxHp());

            // 2-1) 현재 페이즈에서 '쿨다운 체크'하며 선택
            Optional<PatternDTO> picked = pickRandomPatternForPhase(boss, currPhase, cooldowns);

            // 2-2) 없으면 → '쿨타임 무시'하고 phase=0에서 선택 (기본 스킬 강제)
            if (picked.isEmpty()) {
                picked = pickEnabledPatternIgnoringCooldown(boss, 0);
            }

            // 2-3) 그래도 없으면 스킵, 있으면 세션 셋
            if (picked.isPresent()) {
                skill = picked.get();
                session.setAttribute(ATTR_BOSS_CURR_SKILL, skill);
            } else {
                // 정말 후보가 하나도 없으면 보스턴 스킵
                bs.setPlayerTurn(true);
                session.setAttribute(ATTR_BATTLE_SESSION, bs);
                Map<String,Object> data = Map.of(
                        "skipped", true,
                        "reason", "사용 가능한 보스 패턴이 없습니다.",
                        "player", Map.of("hp", bs.getPlayerHp(), "power", bs.getPlayerPower()),
                        "monster", Map.of("hp", bs.getMonsterHp(), "maxHp", bs.getMonsterMaxHp())
                );
                return GamJaResponse.success("보스 턴 스킵", data);
            }
        }
        // 3) 스킬 실행 (대사와 동시에)
        String type = skill.getType();
        int value = skill.getValue() != 0 ? skill.getValue() : 0;

        int dealtDamage = 0;
        int healed = 0;
        int debuffedPower = 0;

        switch (type) {
            case "DAMAGE_TO_PLAYER" -> {
                int raw = value;
                int reduced = Math.max(1, raw - bs.getDefense()); // 방어력 적용
                dealtDamage = Math.min(reduced, bs.getPlayerHp());
                bs.setPlayerHp(Math.max(0, bs.getPlayerHp() - dealtDamage));
            }
            case "HEAL_SELF" -> {
                int before = bs.getMonsterHp();
                bs.setMonsterHp(Math.min(bs.getMonsterMaxHp(), bs.getMonsterHp() + value));
                healed = bs.getMonsterHp() - before;
            }
            case "DEBUFF_PLAYER" -> {
                // 다음 플레이어 공격력 일시 하향 (기본공격력을 기준으로 적용)
                int newPower = Math.max(1, bs.getPlayerBasePower() - value);
                debuffedPower = Math.max(0, bs.getPlayerPower() - newPower);
                bs.setPlayerPower(newPower);
            }
            default -> {
                // 정의되지 않은 타입이면 아무 것도 안 함
            }
        }

        // 4) 실행한 스킬에 쿨다운 부여 (턴 시작에 이미 전체-1 했으므로 지금 세팅)
        Integer cd = skill.getCooldown();
        if (cd != null && cd > 0) {
            cooldowns.put(skill.getId(), cd);
        }

        // 5) 보스 HP 기준 새 페이즈 계산 → 다음 스킬 픽
        int newPhase = calcPhaseByHp(bs.getMonsterHp(), bs.getMonsterMaxHp());

        // boss 엔티티 필요: 레포에서 다시 조회 (권장)
        Monster bossEntity = monsterRepository.findById(bs.getMonsterId())
                .orElse(null);

        PatternDTO nextSkill = null;
        if (bossEntity != null) {
            nextSkill = pickRandomPatternForPhase(bossEntity, newPhase, cooldowns).orElse(null);
        }
        if (nextSkill != null) {
            session.setAttribute(ATTR_BOSS_CURR_SKILL, nextSkill);
        } else {
            session.removeAttribute(ATTR_BOSS_CURR_SKILL);
        }

        // 6) 턴 종료: 플레이어 턴으로 전환
        bs.setPlayerTurn(true);
        session.setAttribute(ATTR_BATTLE_SESSION, bs);
        session.setAttribute(ATTR_BOSS_PHASE, newPhase);
        session.setAttribute(ATTR_BOSS_COOLDOWNS, cooldowns);

        boolean defeat = bs.getPlayerHp() <= 0;

        // 7) 응답 페이로드
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dialogue", skill.getDialogue());
        payload.put("skill", skill); // PatternDTO 그대로 내려줌
        payload.put("effects", Map.of(
                "damageToPlayer", dealtDamage,
                "healToBoss", healed,
                "debuffPlayerPower", debuffedPower
        ));
        payload.put("player", Map.of(
                "hp", bs.getPlayerHp(),
                "power", bs.getPlayerPower(),
                "defeat", defeat
        ));
        payload.put("monster", Map.of(
                "hp", bs.getMonsterHp(),
                "maxHp", bs.getMonsterMaxHp()
        ));
        payload.put("phase", newPhase);
        if (nextSkill != null) {
            payload.put("nextBossSkill", nextSkill); // 다음 턴에 쓸 예정 스킬 미리 제공
        }

        return GamJaResponse.success("보스 턴 처리 완료", payload);
    }

    private Optional<PatternDTO> pickEnabledPatternIgnoringCooldown(Monster boss, int phase) {
        List<MonsterBossPattern> raw =
                bossPatternRepository.findByMonsterAndPhaseOrderByPhaseOrderAsc(boss, phase);

        List<PatternDTO> enabled = raw.stream()
                .filter(MonsterBossPattern::isEnabled)   // 사용 가능만
                .map(this::toDto)                         // 쿨타임은 완전 무시
                .toList();

        if (enabled.isEmpty()) return Optional.empty();
        return Optional.of(enabled.get((int)(Math.random() * enabled.size())));
    }

    @Transactional
    public GamJaResponse endBossBattle(HttpSession session, String outcomeParam) {
        BattleSession bs = (BattleSession) session.getAttribute(ATTR_BATTLE_SESSION);
        if (bs == null) {
            clearBossSession(session);
            return GamJaResponse.success("전투 세션이 없어 초기화만 수행", Map.of("outcome", "none"));
        }
        boolean victory = bs.getMonsterHp() <= 0;
        boolean defeat  = bs.getPlayerHp()  <= 0;
        boolean escape  = "escape".equalsIgnoreCase(outcomeParam);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("player", Map.of("hp", bs.getPlayerHp(), "maxHp", bs.getPlayerMaxHp()));
        result.put("monster", Map.of("id", bs.getMonsterId(), "hp", bs.getMonsterHp(), "maxHp", bs.getMonsterMaxHp()));

        if (escape || defeat) {
            result.put("outcome", escape ? "escape" : "defeat");
            clearBossSession(session);
            return GamJaResponse.success("보스 전투 종료", result);
        }

        if (victory) {
            Long userId = bs.getUserId();
            if (logService.hasKilledBossToday(userId, bs.getMonsterId())) {
                clearBossSession(session);
                result.put("outcome", "already_cleared_today");
                result.put("message", "오늘은 이미 이 보스를 토벌했습니다. 내일 00:00에 초기화됩니다.");
                return GamJaResponse.success("보스 전투 종료(이미 토벌)", result);
            }

            Long dexId = userDtlRepository.findById(userId)
                    .map(dtl -> dtl.getCharacterDexId())
                    .orElse(null);

            if (dexId == null) {
                clearBossSession(session);
                return GamJaResponse.fail("장착한 캐릭터가 없습니다.");
            }

            Dex dex = dexRepository.findById(dexId)
                    .orElseThrow(() -> new IllegalArgumentException("캐릭터 정보 없음"));

            int beforeXp    = bs.getPlayerXp();
            int beforeLevel = bs.getPlayerLevel();

            // 경험치(보너스 포함)
            int baseXp = bs.getMonsterXp();
            int bonusXp = (int) Math.floor(baseXp * (bs.getExpGain() / 100.0));
            int gainedXp = baseXp + bonusXp;

            UserDexXpDto xpDto = levelService.updateCharacterExp(userId, dexId, gainedXp);

            // 골드/드랍
            double goldGainPercent = bs.getGoldGain();
            BattleService.DropResult dropResult = battleService.getDropResult(bs.getMonsterId(), userId, goldGainPercent);
            List<Map<String, Object>> visibleDrops = dropResult.visibleRewards();
            List<ItemReward> internalRewards = dropResult.internalRewards();
            battleService.processItemRewards(userId, internalRewards, goldGainPercent);

            // 활동 로그
            logService.recordCounter(userId, CounterType.MONSTER_KILL, bs.getMonsterId());
            userLogService.recordDailyMonster(userId, bs.getMonsterId());
            corpsTierService.updateCorpsXp(userId, 100);

            // 응답 페이로드(보상 블록 + 루팅)
            result.put("dexName", dex.getName());
            result.put("charImage", dex.getImage());
            result.put("beforeLevel", beforeLevel);
            result.put("afterLevel", xpDto.getLevel());
            result.put("beforeXp", beforeXp);
            result.put("afterXp", xpDto.getXp());
            result.put("maxExp", xpDto.getMaxExp());
            result.put("gainedXp", gainedXp);
            result.put("items", visibleDrops);
            result.put("outcome", "victory");
        }

        clearBossSession(session);
        return GamJaResponse.success("보스 전투 종료", result);
    }

    private void clearBossSession(HttpSession session) {
        session.removeAttribute(ATTR_BATTLE_SESSION);
        session.removeAttribute(ATTR_BOSS_PHASE);
        session.removeAttribute(ATTR_BOSS_PATTERNS);
        session.removeAttribute(ATTR_BOSS_COOLDOWNS);
        session.removeAttribute(ATTR_BOSS_TURN);
        session.removeAttribute(ATTR_BOSS_CURR_SKILL);
        session.removeAttribute(ATTR_PLAYER_META);
        session.removeAttribute(ATTR_BOSS_META);
    }


    private int calcEffectivePlayerPower(BattleSession bs) {
        int base = bs.getPlayerBasePower();
        if (bs.isBonusApplied()) base += bs.getPlayerPotionPower();
        return Math.max(1, base);
    }

    private Optional<Dex> pickRandomOwnedDex(Long userId) {
        List<Long> dexIds = userDexRepository.findByUserId(userId)
                .stream()
                .map(UserDex::getId)
                .toList();

        if (dexIds.isEmpty()) return Optional.empty();

        List<Dex> dexList = dexRepository.findUsableVisibleByIds(dexIds);
        if (dexList.isEmpty()) return Optional.empty();

        return Optional.of(dexList.get(ThreadLocalRandom.current().nextInt(dexList.size())));
    }
    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getAllyFlags(HttpSession session) {
        Map<String, Boolean> flags = (Map<String, Boolean>) session.getAttribute(ATTR_ALLY_FLAGS);
        if (flags == null) {
            flags = new HashMap<>();
            flags.put(KEY_HELP_HEAL, false);
            flags.put(KEY_HELP_RESTORE, false);
            flags.put(KEY_HELP_ATTACK, false);
            session.setAttribute(ATTR_ALLY_FLAGS, flags);
        }
        return flags;
    }
    @SuppressWarnings("unchecked")
    private Map<String, String> getAllyImages(HttpSession session) {
        Map<String, String> imgs = (Map<String, String>) session.getAttribute(ATTR_ALLY_IMAGES);
        if (imgs == null) {
            imgs = new HashMap<>();
            session.setAttribute(ATTR_ALLY_IMAGES, imgs);
        }
        return imgs;
    }

}
