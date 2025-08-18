package com.phobi.gamja.service;

import com.phobi.gamja.dto.achievement.AchievementEntryDto;
import com.phobi.gamja.dto.achievement.AchievementRewardDto;
import com.phobi.gamja.dto.achievement.AchievementSeriesDto;
import com.phobi.gamja.dto.achievement.EntryFlatRow;
import com.phobi.gamja.dto.user.UserAchievementDto;
import com.phobi.gamja.entity.achievement.Achievement;
import com.phobi.gamja.entity.achievement.AchievementCategory;
import com.phobi.gamja.entity.user.UserAchievement;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.achievement.AchievementEntryRepository;
import com.phobi.gamja.repository.achievement.AchievementRepository;
import com.phobi.gamja.repository.user.UserAchievementRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final CommonUtil commonUtil;
    private final AchievementRepository achievementRepository;
    private final AchievementEntryRepository achievementEntryRepository;
    private final UserAchievementRepository userAchievementRepository;

    /*
    * 카테고리별 대표 업적 메타 리스트
    * */
    @Transactional(readOnly = true)
    public GamJaResponse listSeriesByCategory(String category, HttpSession session) {
        Long userId = commonUtil.getUserId(session);

        List<Achievement> metas;
        if (category == null || category.isBlank()) {
            metas = achievementRepository.findAll()
                    .stream().filter(Achievement::isEnabled)
                    .sorted(Comparator.comparing(Achievement::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } else {
            AchievementCategory cat = AchievementCategory.valueOf(category.toUpperCase());
            metas = achievementRepository.findByCategoryAndEnabledTrueOrderByCreatedAtDesc(cat);
        }

        List<Map<String, Object>> result = metas.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("description", m.getDescription());
            map.put("seriesKey", m.getSeriesKey());
            map.put("category", m.getCategory());
            map.put("enabled", m.isEnabled());
            map.put("createdAt", m.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return GamJaResponse.success("업적 시리즈 목록", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getSeries(String seriesKey, HttpSession session) {
        Long userId = commonUtil.getUserId(session);

        Achievement meta = achievementRepository.findBySeriesKeyAndEnabledTrue(seriesKey)
                .orElse(null);
        if (meta == null) {
            return GamJaResponse.fail("해당 시리즈를 찾을 수 없습니다.");
        }

        // 평탄화 로우 조회
        List<EntryFlatRow> rows = achievementEntryRepository.findEntryFlatRows(meta.getId(), userId);

        // entryId 기준 그룹핑 후 DTO 조립
        Map<Long, List<EntryFlatRow>> byEntry = rows.stream()
                .collect(Collectors.groupingBy(r -> r.entryId));

        List<AchievementEntryDto> entryDtos = new ArrayList<>();
        for (Map.Entry<Long, List<EntryFlatRow>> e : byEntry.entrySet()) {
            List<EntryFlatRow> group = e.getValue();
            EntryFlatRow head = group.get(0);

            AchievementEntryDto entryDto = new AchievementEntryDto();
            entryDto.id = head.entryId;
            entryDto.description = head.description;
            entryDto.requirementType = head.requirementType;
            entryDto.requirementValue = head.requirementValue;
            entryDto.characterId = head.characterId;
            entryDto.monsterId   = head.monsterId;
            entryDto.itemId      = head.itemId;
            entryDto.orderInSeries = head.orderInSeries;
            entryDto.enabled = Boolean.TRUE.equals(head.entryEnabled);

            // rewards
            List<AchievementRewardDto> rewards = group.stream()
                    .filter(r -> r.rewardId != null)
                    .map(r -> {
                        AchievementRewardDto rd = new AchievementRewardDto();
                        rd.id = r.rewardId;
                        rd.rewardType = r.rewardType;
                        rd.amount = r.rewardAmount;
                        rd.rewardKey = r.rewardKey;
                        rd.rewardRefId = r.rewardRefId;
                        return rd;
                    })
                    .collect(Collectors.toList());
            entryDto.rewards = rewards;

            // user progress (있을 때만)
            if (head.userStatus != null) {
                UserAchievementDto ua = new UserAchievementDto();
                ua.status = head.userStatus;
                ua.progressCount = head.userProgressCount;
                ua.completedAt = head.userCompletedAt;
                ua.rewardedAt = head.userRewardedAt;
                entryDto.user = ua;
            }

            entryDtos.add(entryDto);
        }

        // 정렬 보정(혹시 쿼리 외적인 재정렬 필요 시)
        entryDtos.sort(Comparator.comparingInt(d -> Optional.ofNullable(d.orderInSeries).orElse(0)));

        // series dto
        AchievementSeriesDto seriesDto = new AchievementSeriesDto();
        seriesDto.id = meta.getId();
        seriesDto.name = meta.getName();
        seriesDto.description = meta.getDescription();
        seriesDto.seriesKey = meta.getSeriesKey();
        seriesDto.category = meta.getCategory();
        seriesDto.enabled = meta.isEnabled();
        seriesDto.entries = entryDtos;

        return GamJaResponse.success("업적 시리즈", seriesDto);
    }
}
