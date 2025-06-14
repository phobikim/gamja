package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.Title;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleRepository extends JpaRepository<Title, Long> {
}
