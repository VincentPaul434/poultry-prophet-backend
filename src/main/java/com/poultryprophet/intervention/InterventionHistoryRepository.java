package com.poultryprophet.intervention;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionHistoryRepository extends JpaRepository<InterventionHistory, Long> {

    List<InterventionHistory> findByInterventionIdOrderByCreatedAtAsc(Long interventionId);
}
