package com.qlda.notificationservice.notification.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDlqPublisher implements DlqPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDlqPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String dlqTopic;

    public KafkaDlqPublisher(
        ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
        @Value("${app.kafka.notification-dlq-topic:notification-events-dlq}") String dlqTopic
    ) {
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        this.dlqTopic = dlqTopic;
    }

    @Override
    public void publish(String payload, String reason) {
        if (kafkaTemplate == null) {
            log.error("KafkaTemplate is unavailable, cannot publish DLQ. reason={}", reason);
            return;
        }
        kafkaTemplate.send(dlqTopic, payload);
    }
}
