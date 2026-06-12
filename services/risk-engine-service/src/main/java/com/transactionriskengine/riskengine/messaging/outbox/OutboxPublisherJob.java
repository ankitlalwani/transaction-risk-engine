package com.transactionriskengine.riskengine.messaging.outbox;

import com.transactionriskengine.riskengine.messaging.kafka.KafkaRiskEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "transaction-risk-engine.outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisherJob {

    private static final int MAXIMUM_RETRIES = 3;

    private final RiskOutboxEventRepository riskOutboxEventRepository;
    private final KafkaRiskEventProducer kafkaRiskEventProducer;

    @Scheduled(
            fixedDelayString =
                    "${transaction-risk-engine.outbox.publisher.fixed-delay-ms:5000}"
    )
    @Transactional
    public void publishPendingEvents() {
        List<RiskOutboxEvent> pendingEvents =
                riskOutboxEventRepository.findTop20ByEventStatusOrderByCreatedAtAsc(
                        RiskOutboxEventStatus.PENDING
                );

        for (RiskOutboxEvent event : pendingEvents) {
            try {
                kafkaRiskEventProducer.publish(
                        event.getTopicName(),
                        event.getAggregateId().toString(),
                        event.getPayload()
                );
                event.markPublished(Instant.now());

                log.info(
                        "Published risk outbox event {} to topic {}",
                        event.getId(),
                        event.getTopicName()
                );
            } catch (Exception exception) {
                event.recordFailure(exception.getMessage(), MAXIMUM_RETRIES);
                log.error(
                        "Failed to publish risk outbox event {}",
                        event.getId(),
                        exception
                );
            }
        }
    }
}
