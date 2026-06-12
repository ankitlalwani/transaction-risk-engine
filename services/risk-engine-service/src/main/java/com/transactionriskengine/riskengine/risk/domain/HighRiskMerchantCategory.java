package com.transactionriskengine.riskengine.risk.domain;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum HighRiskMerchantCategory {
    CRYPTO,
    GAMBLING,
    WIRE_TRANSFER,
    MONEY_TRANSFER,
    LUXURY_GOODS;

    private static final Set<String> CATEGORY_NAMES = Stream.of(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean contains(String category) {
        return category != null
                && CATEGORY_NAMES.contains(category.trim().toUpperCase(Locale.ROOT));
    }
}
