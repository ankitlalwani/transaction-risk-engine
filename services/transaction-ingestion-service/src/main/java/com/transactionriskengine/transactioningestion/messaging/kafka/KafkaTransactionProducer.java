package com.transactionriskengine.transactioningestion.messaging.kafka;

import com.transactionriskengine.transactioningestion.messaging.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topicName, String key, String payload) {
        kafkaTemplate.send(topicName, key, payload);
    }
}
