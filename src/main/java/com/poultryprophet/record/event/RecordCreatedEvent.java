package com.poultryprophet.record.event;

/**
 * Published after a daily record is committed (SDD 1.1 sequence: DailyRecordService ->
 * AnalyticsService enqueue). The analytics worker consumes it to (re)compute indicators.
 */
public record RecordCreatedEvent(Long recordId, Long batchId) {
}
