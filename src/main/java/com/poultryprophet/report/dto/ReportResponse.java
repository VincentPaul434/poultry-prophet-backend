package com.poultryprophet.report.dto;

public record ReportResponse(
        Long reportId,
        ReportPayload payload
) {
}
