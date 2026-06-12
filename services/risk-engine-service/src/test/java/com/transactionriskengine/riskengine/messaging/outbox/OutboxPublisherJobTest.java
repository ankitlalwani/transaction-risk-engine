package com.transactionriskengine.riskengine.messaging.outbox;

import com.transactionriskengine.riskengine.messaging.kafka.KafkaRiskEventProducer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherJobTest {

    private final RiskOutboxEventRepository repository =
            mock(RiskOutboxEventRepository.class);
    private final KafkaRiskEventProducer producer =
            mock(KafkaRiskEventProducer.class);
    private final OutboxPublisherJob publisherJob =
            new OutboxPublisherJob(repository, producer);

    @Test
    void publishPendingEventsMarksEventAsPublishedAfterKafkaAcknowledgement() {
        RiskOutboxEvent event = pendingEvent();
        when(repository.findTop20ByEventStatusOrderByCreatedAtAsc(
                RiskOutboxEventStatus.PENDING
        )).thenReturn(List.of(event));

        publisherJob.publishPendingEvents();

        verify(producer).publish(
                "risk.evaluated.v1",
                event.getAggregateId().toString(),
                event.getPayload()
        );
        assertThat(event.getEventStatus()).isEqualTo(RiskOutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.getLastError()).isNull();
    }

    @Test
    void publishPendingEventsMarksEventFailedAfterThreeAttempts() {
        RiskOutboxEvent event = pendingEvent();
        when(repository.findTop20ByEventStatusOrderByCreatedAtAsc(
                RiskOutboxEventStatus.PENDING
        )).thenReturn(List.of(event));
        doThrow(new IllegalStateException("Kafka unavailable"))
                .when(producer)
                .publish(
                        "risk.evaluated.v1",
                        event.getAggregateId().toString(),
                        event.getPayload()
                );

        publisherJob.publishPendingEvents();
        publisherJob.publishPendingEvents();
        publisherJob.publishPendingEvents();

        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getEventStatus()).isEqualTo(RiskOutboxEventStatus.FAILED);
        assertThat(event.getLastError()).isEqualTo("Kafka unavailable");
    }

    private RiskOutboxEvent pendingEvent() {
        return new RiskOutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RISK_EVALUATED",
                "risk.evaluated.v1",
                "{\"eventType\":\"RISK_EVALUATED\"}"
        );
    }
}
