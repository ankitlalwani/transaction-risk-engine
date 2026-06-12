package com.transactionriskengine.riskengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.flyway.enabled=false",
        "transaction-risk-engine.outbox.publisher.enabled=false"
})
class RiskEngineServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
