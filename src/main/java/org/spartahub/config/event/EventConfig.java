package org.spartahub.config.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.spartahub.common.domain.InboxRepository;
import org.spartahub.common.domain.OutboxRepository;
import org.spartahub.common.event.Events;
import org.spartahub.common.event.OutboxEventListener;
import org.spartahub.common.event.OutboxRelay;
import org.spartahub.common.messaging.advice.InboxAdvice;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class EventConfig implements AsyncConfigurer {

    @Bean
    public InitializingBean eventsInitializer(KafkaTemplate<String, Object> kafkaTemplate, ApplicationEventPublisher eventPublisher) {
        return () -> Events.setDependency(kafkaTemplate, eventPublisher);
    }

    // 비동기 적용시 생성될 스레드 풀 설정
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // 기본 스레드 수
        executor.setMaxPoolSize(50);        // 최대 스레드 수
        executor.setQueueCapacity(100);     // 대기 큐 용량
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 접두사
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    @Bean
    public OutboxEventListener outboxEventListener(OutboxRepository outboxRepository, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        return new OutboxEventListener(outboxRepository, kafkaTemplate, objectMapper);
    }

    @Bean
    public OutboxRelay outboxRelay(OutboxRepository outboxRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        return new OutboxRelay(outboxRepository, kafkaTemplate);
    }

    @Bean
    public InboxAdvice inboxAdvice(InboxRepository inboxRepository) {
        return new InboxAdvice(inboxRepository);
    }
}
