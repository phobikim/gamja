package com.phobi.gamja.service;

import com.phobi.gamja.dto.user.UserCharInfoDto;
import com.phobi.gamja.dto.user.UserRankDto;
import com.phobi.gamja.entity.title.Title;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.user.UserCorps;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.UserCorpsRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private final UserCorpsRepository userCorpsRepository;
    private final UserRepository userRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserTitleRepository userTitleRepository;

    public GamJaResponse getRank(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserRankDto> rankList = userCorpsRepository.findTopRankList();

        if (userId != null) {
            for (UserRankDto dto : rankList) {
                if (userId.equals(dto.getId())) {
                    dto.setMine(true);
                    break;
                }
            }
        }

        return GamJaResponse.success("정상 조회", rankList);
    }
}
