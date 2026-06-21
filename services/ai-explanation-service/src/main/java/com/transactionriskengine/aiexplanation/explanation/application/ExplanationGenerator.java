package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;

public interface ExplanationGenerator {

    GeneratedExplanation generate(RiskEvaluatedEvent event);
}