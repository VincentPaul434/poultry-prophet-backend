package com.poultryprophet.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    boolean existsByFarmIdAndNameIgnoreCase(Long farmId, String name);

    List<Batch> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    Optional<Batch> findByIdAndFarmId(Long id, Long farmId);
}
