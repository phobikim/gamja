package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserItemAlchemyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserItemAlchemyOptionRepository extends JpaRepository<UserItemAlchemyOption, Long> {
    List<UserItemAlchemyOption> findByUserIdAndItemId(Long userId, Long itemId);
    List<UserItemAlchemyOption> findByUserIdAndItemIdIn(Long userId, Collection<Long> itemIds);
    void deleteByUserIdAndItemId(Long userId, Long itemId);
}