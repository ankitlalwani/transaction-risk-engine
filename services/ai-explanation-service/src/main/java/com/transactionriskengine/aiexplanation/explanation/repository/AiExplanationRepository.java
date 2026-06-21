package com.transactionriskengine.aiexplanation.explanation.repository;

import com.transactionriskengine.aiexplanation.explanation.domain.AiExplanation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiExplanationRepository extends JpaRepository<AiExplanation, UUID> {

    boolean existsByTransactionId(UUID transactionId);

    Optional<AiExplanation> findByTransactionId(UUID transactionId);

    Optional<AiExplanation> findByRiskDecisionId(UUID riskDecisionId);
}