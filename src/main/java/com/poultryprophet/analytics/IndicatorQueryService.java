package com.poultryprophet.analytics;

import com.poultryprophet.analytics.dto.IndicatorResponse;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.common.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-side for indicators (SDD 2.1 gauge/sparkline data), with farm scoping. */
@Service
public class IndicatorQueryService {

    private final IndicatorRepository indicatorRepository;
    private final BatchService batchService;

    public IndicatorQueryService(IndicatorRepository indicatorRepository, BatchService batchService) {
        this.indicatorRepository = indicatorRepository;
        this.batchService = batchService;
    }

    @Transactional(readOnly = true)
    public IndicatorResponse latest(Long batchId, Long farmId) {
        batchService.requireBatch(batchId, farmId);
        return indicatorRepository.findFirstByBatchIdOrderByComputedAtDesc(batchId)
                .map(IndicatorResponse::from)
                .orElseThrow(() -> new NotFoundException("No indicators computed yet for batch " + batchId));
    }

    @Transactional(readOnly = true)
    public List<IndicatorResponse> recent(Long batchId, Long farmId, int limit) {
        batchService.requireBatch(batchId, farmId);
        return indicatorRepository
                .findByBatchIdOrderByComputedAtDesc(batchId, PageRequest.of(0, limit))
                .stream()
                .map(IndicatorResponse::from)
                .toList();
    }
}
