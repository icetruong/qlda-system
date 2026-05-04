package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);
    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public EmailNotificationSender(
        JavaMailSender javaMailSender,
        @Value("${app.notification.email.from:${spring.mail.username:}}") String fromAddress
    ) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public String channel() {
        return "EMAIL";
    }

    @Override
    public void send(NotificationEvent event, NotificationResponse notification) {
        List<String> recipients = extractRecipients(event.metadata());
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Missing recipient email in event metadata");
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        if (StringUtils.hasText(fromAddress)) {
            mail.setFrom(fromAddress);
        }
        mail.setTo(recipients.toArray(String[]::new));
        mail.setSubject(event.tieuDe());
        mail.setText(buildBody(event, notification));
        javaMailSender.send(mail);
        log.info("Sent outlook email for eventId={}, notificationId={}, recipients={}",
            event.eventId(), notification.id(), recipients);
    }

    private List<String> extractRecipients(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return List.of();
        }
        List<String> recipients = new ArrayList<>();
        Object many = metadata.get("recipientEmails");
        if (many instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String value && StringUtils.hasText(value)) {
                    recipients.add(value.trim());
                }
            }
        }

        Object one = metadata.get("recipientEmail");
        if (one instanceof String value && StringUtils.hasText(value)) {
            recipients.add(value.trim());
        }
        return recipients.stream().distinct().toList();
    }

    private String buildBody(NotificationEvent event, NotificationResponse notification) {
        return """
            %s

            Loai thong bao: %s
            Event ID: %s
            Notification ID: %s
            """.formatted(
            event.noiDung(),
            event.loaiThongBao(),
            event.eventId(),
            notification.id()
        );
    }
}
