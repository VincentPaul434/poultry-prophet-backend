package com.poultryprophet.report.dto;

public record ExportResult(
        String filename,
        String contentType,
        byte[] content
) {
}
