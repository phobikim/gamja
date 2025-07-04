package com.phobi.gamja.repository.chronicle;

import com.phobi.gamja.entity.chronicle.Chronicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChronicleRepository extends JpaRepository<Chronicle, Long> {
    List<Chronicle> findByMapIdAndUseFlagTrue(Long mapId);
    Optional<Chronicle> findByTargetTypeAndTargetId(Chronicle.ChronicleTargetType targetType, Long targetId);
}
