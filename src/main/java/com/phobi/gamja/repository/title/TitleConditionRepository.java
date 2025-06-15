package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.TitleCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TitleConditionRepository extends JpaRepository<TitleCondition, Long> {
    List<TitleCondition> findByTitle_Id(Long titleId);
}
