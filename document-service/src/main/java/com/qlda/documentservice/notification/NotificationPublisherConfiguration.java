package com.qlda.documentservice.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationPublisherConfiguration {

    @Bean
    @ConditionalOnMissingBean(NotificationEventPublisher.class)
    public NotificationEventPublisher noOpNotificationEventPublisher() {
        return new NoOpNotificationEventPublisher();
    }
}
