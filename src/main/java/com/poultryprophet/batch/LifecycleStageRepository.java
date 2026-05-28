package com.poultryprophet.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LifecycleStageRepository extends JpaRepository<LifecycleStage, Long> {

    Optional<LifecycleStage> findByNameIgnoreCase(String name);

    List<LifecycleStage> findAllByOrderByOrderIndexAsc();
}
