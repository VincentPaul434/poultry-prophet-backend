package com.poultryprophet.bird;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BirdRepository extends JpaRepository<Bird, Long> {

    boolean existsByBatchIdAndBandNumberIgnoreCase(Long batchId, String bandNumber);

    List<Bird> findByBatchIdOrderByBandNumberAsc(Long batchId);

    Optional<Bird> findByIdAndBatchId(Long id, Long batchId);
}
