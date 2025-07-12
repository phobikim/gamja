package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByUseFlagTrueOrderByOrderNumAsc();
}
