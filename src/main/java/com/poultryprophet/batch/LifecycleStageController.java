package com.poultryprophet.batch;

import com.poultryprophet.batch.dto.LifecycleStageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lifecycle-stages")
public class LifecycleStageController {

    private final LifecycleStageRepository stageRepository;

    public LifecycleStageController(LifecycleStageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @GetMapping
    public List<LifecycleStageResponse> list() {
        return stageRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(LifecycleStageResponse::from)
                .toList();
    }
}
