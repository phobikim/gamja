package com.phobi.gamja.repository.achievement;

import com.phobi.gamja.entity.achievement.AchievementEntry;
import com.phobi.gamja.entity.achievement.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AchievementEntryRepository extends JpaRepository<AchievementEntry, Long> {

    List<AchievementEntry> findByAchievementIdOrderByOrderInSeries(Long achievementId);

    boolean existsByAchievementIdAndRequirementTypeAndCharacterIdAndRequirementValue(
            Long achievementId, RequirementType requirementType, Long characterId, Integer requirementValue);

    List<AchievementEntry> findByIdIn(Collection<Long> ids);

    /**
     * 시리즈 내 엔트리 + 리워드 + 특정 유저 진행상태를 평탄화로 가져오는 쿼리
     * (서비스에서 groupBy(entryId) 후 DTO로 조립)
     */
    @Query("""
        select new com.phobi.gamja.dto.achievement.EntryFlatRow(
            e.id, e.description, e.requirementType, e.requirementValue,
            e.characterId, e.monsterId, e.itemId, e.orderInSeries, e.enabled,
            r.id, r.rewardType, r.amount, r.rewardKey, r.rewardRefId,
            ua.status, ua.progressCount, ua.completedAt, ua.rewardedAt
        )
        from AchievementEntry e
        left join AchievementReward r on r.entry = e
        left join UserAchievement ua on ua.entry = e and ua.userId = :userId
        where e.achievement.id = :achievementId and e.enabled = true
        order by e.orderInSeries asc, r.id asc
        """)
    List<com.phobi.gamja.dto.achievement.EntryFlatRow> findEntryFlatRows(Long achievementId, Long userId);

    // [레벨 달성] 같은 캐릭터 + 요구레벨 이하(=달성 가능한) 엔트리
    List<AchievementEntry> findByRequirementTypeAndCharacterIdAndRequirementValueLessThanEqualOrderByRequirementValueAsc(
            RequirementType requirementType, Long characterId, Integer currentLevel);


    // [몬스터 킬] 같은 몬스터 대상 엔트리 (누적 비교는 서비스에서)
    List<AchievementEntry> findByRequirementTypeAndMonsterId(
            RequirementType requirementType, Long monsterId);
}
