package com.poultryprophet.selection.dto;

import java.util.List;

/**
 * The ranked month-5 selection view for a batch: birds ordered by CRS desc, with the suggested
 * advancement cut-line (Blueprint Module 3 / 6).
 */
public record SelectionViewResponse(
        Long batchId,
        double cutLineCrs,
        List<SelectionRowResponse> rows
) {
}
