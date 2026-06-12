INSERT INTO risk_rules (
    rule_code,
    rule_name,
    rule_description,
    rule_type,
    score_impact,
    active
)
VALUES
    (
        'HIGH_AMOUNT_TRANSACTION',
        'High Amount Transaction',
        'Transaction amount is greater than 10000.',
        'AMOUNT',
        40,
        TRUE
    ),
    (
        'MEDIUM_AMOUNT_TRANSACTION',
        'Medium Amount Transaction',
        'Transaction amount is between 5000 and 10000.',
        'AMOUNT',
        25,
        TRUE
    ),
    (
        'WIRE_TRANSFER_RISK',
        'Wire Transfer Risk',
        'Wire transfer payment channel has elevated risk.',
        'PAYMENT_CHANNEL',
        20,
        TRUE
    ),
    (
        'INTERNATIONAL_TRANSACTION',
        'International Transaction',
        'Merchant country is outside the United States.',
        'LOCATION',
        25,
        TRUE
    ),
    (
        'HIGH_RISK_MERCHANT_CATEGORY',
        'High Risk Merchant Category',
        'Merchant category belongs to a configured high-risk category.',
        'MERCHANT_CATEGORY',
        30,
        TRUE
    ),
    (
        'MISSING_DEVICE_ID',
        'Missing Device ID',
        'Transaction does not include device identifier information.',
        'DEVICE',
        10,
        TRUE
    ),
    (
        'MISSING_SOURCE_IP',
        'Missing Source IP',
        'Transaction does not include source IP information.',
        'DEVICE',
        10,
        TRUE
    ),
    (
        'HIGH_AMOUNT_UNUSUAL_CHANNEL',
        'High Amount Unusual Channel',
        'Transaction amount is greater than 5000 and was initiated through ATM or mobile app.',
        'COMPOSITE',
        20,
        TRUE
    )
    ON CONFLICT (rule_code) DO NOTHING;