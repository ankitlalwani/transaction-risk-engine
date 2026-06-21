package com.transactionriskengine.aiexplanation.explanation.api;

import com.transactionriskengine.aiexplanation.explanation.api.response.AiExplanationResponse;
import com.transactionriskengine.aiexplanation.explanation.application.AiExplanationQueryService;
import com.transactionriskengine.aiexplanation.explanation.mapper.AiExplanationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-explanations")
@RequiredArgsConstructor
public class AiExplanationController {

    private final AiExplanationQueryService aiExplanationQueryService;
    private final AiExplanationMapper aiExplanationMapper;

    @GetMapping("/transaction/{transactionId}")
    public AiExplanationResponse getByTransactionId(@PathVariable UUID transactionId) {
        return aiExplanationMapper.toResponse(
                aiExplanationQueryService.getByTransactionId(transactionId)
        );
    }

    @GetMapping("/risk-decision/{riskDecisionId}")
    public AiExplanationResponse getByRiskDecisionId(@PathVariable UUID riskDecisionId) {
        return aiExplanationMapper.toResponse(
                aiExplanationQueryService.getByRiskDecisionId(riskDecisionId)
        );
    }
}