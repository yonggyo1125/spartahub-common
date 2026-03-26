package org.spartahub.common.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Events {
    private static KafkaTemplate<String, Object> kafkaTemplate;
    private static ApplicationEventPublisher eventPublisher;

    public static void setDependency(KafkaTemplate<String, Object> kafkaTemplate, ApplicationEventPublisher eventPublisher) {
        Events.kafkaTemplate = kafkaTemplate;
        Events.eventPublisher = eventPublisher;
    }

    public static void trigger(String correlationId, String domainType, String domainId, String eventType, Object payload) {
        if (kafkaTemplate != null && eventPublisher != null) {
            eventPublisher.publishEvent(new OutboxEvent(correlationId, domainType, domainId, eventType, payload));
        }
    }
}
