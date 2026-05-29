package com.poultryprophet.ranging.dto;

import com.poultryprophet.ranging.HealthEventSeverity;
import com.poultryprophet.ranging.QualityRating;
import com.poultryprophet.ranging.RangingRecord;

import java.time.LocalDate;

public record RangingRecordResponse(
        Long id,
        Long birdId,
        LocalDate recordDate,
        double weightG,
        HealthEventSeverity healthEvent,
        String temperamentNotes,
        QualityRating qualityRating
) {
    public static RangingRecordResponse from(RangingRecord record) {
        return new RangingRecordResponse(
                record.getId(),
                record.getBird().getId(),
                record.getRecordDate(),
                record.getWeightG(),
                record.getHealthEvent(),
                record.getTemperamentNotes(),
                record.getQualityRating());
    }
}
