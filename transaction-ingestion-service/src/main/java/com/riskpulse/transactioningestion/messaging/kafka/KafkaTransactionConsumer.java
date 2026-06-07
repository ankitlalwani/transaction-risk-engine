package com.riskpulse.transactioningestion.messaging.kafka;

import com.riskpulse.transactioningestion.messaging.event.TransactionEventPayload;
import com.riskpulse.transactioningestion.messaging.transactionEventAudit.TransactionEvent;
import com.riskpulse.transactioningestion.messaging.transactionEventAudit.TransactionEventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private static final String CONSUMER_NAME = "transaction-ingestion-service";

    private final TransactionEventAuditRepository transactionEventAuditRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${riskpulse.kafka.topics.transaction-created}",
            groupId = "transaction-ingestion-service"
    )
    @Transactional
    public void consumeTransactionCreatedEvent(ConsumerRecord<String, String> record) {
        log.info("Consuming transaction event from topic {}", record);
        String payload = record.value();
        TransactionEventPayload transactionEventPayload =
                objectMapper.readValue(payload, TransactionEventPayload.class);

        TransactionEvent transactionEvent = new TransactionEvent(
                transactionEventPayload.eventId(),
                transactionEventPayload.transactionId(),
                record.topic(),
                transactionEventPayload.eventType(),
                CONSUMER_NAME
        );
        transactionEvent.markProcessed();
        transactionEventAuditRepository.save(transactionEvent);

        log.info(
                "Consumed event {} for transaction {} from topic {}",
                transactionEventPayload.eventId(),
                transactionEventPayload.transactionId(),
                record.topic()
        );
    }
}
