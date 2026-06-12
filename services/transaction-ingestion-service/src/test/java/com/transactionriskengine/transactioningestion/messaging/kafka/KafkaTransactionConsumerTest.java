package com.transactionriskengine.transactioningestion.messaging.kafka;

import com.transactionriskengine.transactioningestion.messaging.transactionEventAudit.TransactionEvent;
import com.transactionriskengine.transactioningestion.messaging.transactionEventAudit.TransactionEventAuditRepository;
import com.transactionriskengine.transactioningestion.messaging.transactionEventAudit.TransactionEventProcessingStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionConsumerTest {

    private static final UUID EVENT_ID =
            UUID.fromString("97c1efc8-c8ba-4eb6-8282-fe1e84113451");
    private static final UUID TRANSACTION_ID =
            UUID.fromString("11111111-aaaa-bbbb-cccc-222222222222");

    @Mock
    private TransactionEventAuditRepository transactionEventAuditRepository;

    private KafkaTransactionConsumer kafkaTransactionConsumer;

    @BeforeEach
    void setUp() {
        kafkaTransactionConsumer = new KafkaTransactionConsumer(
                transactionEventAuditRepository,
                new ObjectMapper()
        );
    }

    @Test
    void consumeTransactionCreatedEventSavesProcessedAuditRow() {
        String payload = """
                {
                  "eventId": "97c1efc8-c8ba-4eb6-8282-fe1e84113451",
                  "transactionId": "11111111-aaaa-bbbb-cccc-222222222222",
                  "transactionReference": "TXN-123",
                  "eventType": "TransactionReceived",
                  "status": "RECEIVED"
                }
                """;
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("transaction.created.v1", 0, 0L, TRANSACTION_ID.toString(), payload);

        kafkaTransactionConsumer.consumeTransactionCreatedEvent(record);

        ArgumentCaptor<TransactionEvent> eventCaptor =
                ArgumentCaptor.forClass(TransactionEvent.class);
        verify(transactionEventAuditRepository).save(eventCaptor.capture());

        TransactionEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getEventId()).isEqualTo(EVENT_ID);
        assertThat(savedEvent.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedEvent.getTopicName()).isEqualTo("transaction.created.v1");
        assertThat(savedEvent.getEventType()).isEqualTo("TransactionReceived");
        assertThat(savedEvent.getConsumerName()).isEqualTo("transaction-ingestion-service");
        assertThat(savedEvent.getProcessingStatus())
                .isEqualTo(TransactionEventProcessingStatus.PROCESSED);
        assertThat(savedEvent.getReceivedAt()).isNotNull();
        assertThat(savedEvent.getProcessedAt()).isNotNull();
    }
}
