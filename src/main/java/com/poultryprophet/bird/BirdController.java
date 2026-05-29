package com.poultryprophet.bird;

import com.poultryprophet.bird.dto.BirdResponse;
import com.poultryprophet.bird.dto.CreateBirdRequest;
import com.poultryprophet.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batches/{batchId}/birds")
public class BirdController {

    private final BirdService birdService;

    public BirdController(BirdService birdService) {
        this.birdService = birdService;
    }

    @PostMapping
    public ResponseEntity<BirdResponse> band(@PathVariable Long batchId,
                                             @Valid @RequestBody CreateBirdRequest request,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        BirdResponse response = birdService.band(batchId, principal.getFarmId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BirdResponse> list(@PathVariable Long batchId,
                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return birdService.listForBatch(batchId, principal.getFarmId());
    }
}
