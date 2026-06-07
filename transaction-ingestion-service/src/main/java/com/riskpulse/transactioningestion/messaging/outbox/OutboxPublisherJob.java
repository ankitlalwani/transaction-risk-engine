package com.riskpulse.transactioningestion.messaging.outbox;

import com.riskpulse.transactioningestion.messaging.kafka.KafkaTransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTransactionProducer kafkaTransactionProducer;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findTop20ByEventStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing pending events");

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTransactionProducer.publish(
                        event.getTopicName(),
                        event.getAggregateId().toString(),
                        event.getPayload()
                );

                event.setEventStatus(OutboxEventStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                event.setLastError(null);

                log.info("published outbox event {} to topic {}", event.getId(), event.getTopicName());

            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());

                if (event.getRetryCount() >= 3) {
                    event.setEventStatus(OutboxEventStatus.FAILED);
                }

                log.error("Error publishing outbox event {}", event, e);
            }
        }
    }

}
