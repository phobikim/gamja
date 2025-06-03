package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.ActionCardEventDrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionCardEventDropRepository extends JpaRepository<ActionCardEventDrop, Long> {
    List<ActionCardEventDrop> findByDropGroupId(Long dropGroupId);
}