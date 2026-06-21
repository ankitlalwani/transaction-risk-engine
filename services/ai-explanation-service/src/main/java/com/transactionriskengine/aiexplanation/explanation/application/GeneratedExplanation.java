package com.transactionriskengine.aiexplanation.explanation.application;

public record GeneratedExplanation(
        String explanationText,
        String recommendedAction,
        String analystSummary,
        String modelProvider,
        String modelName,
        String promptVersion
) {
}