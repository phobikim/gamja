package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.CorpsTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorpsTierRepository extends JpaRepository<CorpsTier, Long> {
    List<CorpsTier> findAllByOrderByTierIdAsc();
}