package com.phobi.gamja.service;

import com.phobi.gamja.entity.achievement.AchievementEntry;
import com.phobi.gamja.entity.achievement.AchievementStatus;
import com.phobi.gamja.entity.achievement.RequirementType;
import com.phobi.gamja.entity.user.UserAchievement;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.achievement.AchievementEntryRepository;
import com.phobi.gamja.repository.user.UserAchievementRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementEventService {
    private final CommonUtil commonUtil;
    private final AchievementEntryRepository entryRepo;
    private final UserAchievementRepository userAchRepo;

    /**
     * 레벨업 이벤트: 특정 캐릭터의 현재 레벨 전달
     * - 해당 조건을 만족하는(<= currentLevel) 모든 REACH_LEVEL 엔트리를 완료 처리
     */
    @Transactional
    public GamJaResponse onLevelUp(Long characterId, Integer currentLevel, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        if (characterId == null || currentLevel == null) {
            return GamJaResponse.fail("characterId, currentLevel가 필요합니다.");
        }

        // 달성 가능한 모든 엔트리 조회
        List<AchievementEntry> targets = entryRepo
                .findByRequirementTypeAndCharacterIdAndRequirementValueLessThanEqualOrderByRequirementValueAsc(
                        RequirementType.REACH_LEVEL, characterId, currentLevel);

        int completed = 0;

        for (AchievementEntry entry : targets) {
            // 유저-엔트리 행을 잠금으로 확보(없으면 생성)
            UserAchievement ua = userAchRepo
                    .findByUserIdAndEntryIdForUpdate(userId, entry.getId())
                    .orElseGet(() -> {
                        UserAchievement n = new UserAchievement();
                        n.setUserId(userId);
                        n.setEntry(entry);
                        n.setStatus(AchievementStatus.IN_PROGRESS);
                        n.setProgressCount(0);
                        return userAchRepo.save(n);
                    });

            // 진행도: 레벨은 "현재레벨"로 set (더 큰 값이면 갱신)
            if (ua.getProgressCount() == null || ua.getProgressCount() < currentLevel) {
                ua.setProgressCount(currentLevel);
            }

            // 이미 완료/보상 상태면 스킵
            if (ua.getStatus() == AchievementStatus.COMPLETED || ua.getStatus() == AchievementStatus.REWARDED) {
                continue;
            }

            // 요구치 충족 시 완료 처리
            if (currentLevel >= entry.getRequirementValue()) {
                ua.setStatus(AchievementStatus.COMPLETED);
                ua.setCompletedAt(LocalDateTime.now());
                completed++;
            }
        }

        return GamJaResponse.success("레벨업 이벤트 처리 완료",
                String.format("완료 %d건 / 검사 %d건", completed, targets.size()));
    }

    /**
     * 몬스터 처치 이벤트: 특정 몬스터를 count만큼 처치(누적 증가)
     * - 해당 MONSTER_KILL 엔트리에 누적 progress를 올리고, 요구치 이상이면 완료 처리
     */
    @Transactional
    public GamJaResponse onMonsterKill(Long monsterId, Integer count, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        if (monsterId == null || count == null || count <= 0) {
            return GamJaResponse.fail("monsterId, count가 필요합니다.");
        }

        List<AchievementEntry> targets = entryRepo
                .findByRequirementTypeAndMonsterId(RequirementType.MONSTER_KILL, monsterId);

        int completed = 0;

        for (AchievementEntry entry : targets) {
            UserAchievement ua = userAchRepo
                    .findByUserIdAndEntryIdForUpdate(userId, entry.getId())
                    .orElseGet(() -> {
                        UserAchievement n = new UserAchievement();
                        n.setUserId(userId);
                        n.setEntry(entry);
                        n.setStatus(AchievementStatus.IN_PROGRESS);
                        n.setProgressCount(0);
                        return userAchRepo.save(n);
                    });

            // 이미 보상까지 끝났으면 스킵
            if (ua.getStatus() == AchievementStatus.REWARDED) {
                continue;
            }

            // 누적 증가
            int now = (ua.getProgressCount() == null ? 0 : ua.getProgressCount());
            ua.setProgressCount(now + count);

            // 완료/보상 전이
            if (ua.getStatus() == AchievementStatus.IN_PROGRESS &&
                    ua.getProgressCount() >= entry.getRequirementValue()) {
                ua.setStatus(AchievementStatus.COMPLETED);
                ua.setCompletedAt(LocalDateTime.now());
                completed++;
            }
        }

        return GamJaResponse.success("몬스터 처치 이벤트 처리 완료",
                String.format("완료 %d건 / 검사 %d건", completed, targets.size()));
    }
}
