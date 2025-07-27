package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserItemAlchemyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserItemAlchemyOptionRepository extends JpaRepository<UserItemAlchemyOption, Long> {
    List<UserItemAlchemyOption> findByUserIdAndUserItemId(Long userId, Long userItemId);

    // 해당 유저가 특정 장비에 부여한 옵션 전체 삭제 (전체 리롤용)
    void deleteByUserIdAndUserItemId(Long userId, Long userItemId);

    // 일부 리롤용
    void deleteByUserIdAndUserItemIdAndOptionIndex(Long userId, Long userItemId, int optionIndex);
}