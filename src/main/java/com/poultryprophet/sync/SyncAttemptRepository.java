package com.poultryprophet.sync;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncAttemptRepository extends JpaRepository<SyncAttempt, Long> {
}
