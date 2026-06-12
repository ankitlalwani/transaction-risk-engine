package com.transactionriskengine.riskengine.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaRiskEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topicName, String key, String payload) {
        kafkaTemplate.send(topicName, key, payload).join();
    }
}
