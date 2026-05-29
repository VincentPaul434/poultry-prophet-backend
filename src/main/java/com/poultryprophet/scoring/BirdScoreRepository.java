package com.poultryprophet.scoring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BirdScoreRepository extends JpaRepository<BirdScore, Long> {

    Optional<BirdScore> findByBirdId(Long birdId);

    List<BirdScore> findByBatchIdOrderByCrsDesc(Long batchId);
}
