package com.riskpulse.transactioningestion.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionCreatedTopic(
            @Value("${riskpulse.kafka.topics.transaction-created}") String topicName
    ) {
        return new NewTopic(topicName, 3, (short) 1);
    }
}
