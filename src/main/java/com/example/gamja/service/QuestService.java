package com.example.gamja.service;

import com.example.gamja.dto.QuestListResponseDto;
import com.example.gamja.entity.DailyQuest;
import com.example.gamja.entity.UserQuest;
import com.example.gamja.repository.DailyQuestRepository;
import com.example.gamja.repository.UserQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final DailyQuestRepository dailyQuestRepository;
    private final UserQuestRepository userQuestRepository;

    public List<QuestListResponseDto> getTodayQuestList(Long userId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<DailyQuest> quests = dailyQuestRepository.findByDayAndIsActive(DayOfWeek.valueOf(today.name()), true);

        return quests.stream().map(q -> {
            Optional<UserQuest> userQuestOpt = userQuestRepository
                    .findByUserIdAndQuestIdAndQuestDate(userId.intValue(), q.getId(), LocalDate.now());

            boolean isCompleted = userQuestOpt.map(UserQuest::isCompleted).orElse(false);

            return QuestListResponseDto.builder()
                    .questId(q.getId())
                    .title(q.getTitle())
                    .description(q.getDescription())
                    .action(q.getAction().name())
                    .goalCount(q.getGoalCount())
                    .rewardType(q.getRewardType().name())
                    .rewardValue(q.getRewardValue())
                    .difficulty(q.getDifficulty().name())
                    .isCompleted(isCompleted)
                    .build();
        }).collect(Collectors.toList());
    }
}
