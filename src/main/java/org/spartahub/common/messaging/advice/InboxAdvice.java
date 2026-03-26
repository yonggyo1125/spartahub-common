package org.spartahub.common.messaging.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.spartahub.common.domain.Inbox;
import org.spartahub.common.domain.InboxRepository;
import org.spartahub.common.messaging.annotation.IdempotentConsumer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class InboxAdvice {
    private final InboxRepository inboxRepository;

    @Around("@annotation(idempotentConsumer)")
    @Transactional(rollbackFor = Exception.class)
    public Object handle(ProceedingJoinPoint joinPoint, IdempotentConsumer idempotentConsumer) throws Throwable {
        UUID messageId = extractMessageId(joinPoint.getArgs());

        if (messageId == null) {
            log.warn("Message ID가 없는 메시지입니다. 멱등성 체크를 진행하지 않습니다.");
            return joinPoint.proceed();
        }

        // 이미 처리된 메시지인지 Inbox 확인
        if (inboxRepository.existsById(messageId)) {
            log.info("이미 처리된 중복 메시지입니다.: {}", messageId);
            return null;
        }

        try {
            // 메서드 호출
            Object result = joinPoint.proceed();

            // 메서드 실행 성공 시 Inbox에 기록 (messageId는 Outbox에서 발행된 ID이며 동일하게 저장합니다.)
            Inbox inbox = Inbox.builder()
                    .id(messageId)
                    .messageGroup(idempotentConsumer.messageGroup())
                    .build();
            inboxRepository.save(inbox);

            return result;

        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈가 발생하여 중복 저장을 하게 되는 경우 예외 처리
            log.warn("동시성 이슈로 인한 중복 메시지 처리 차단: {}", messageId);
            return null;
        }
    }

    private UUID extractMessageId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ConsumerRecord<?, ?> record) {
                Header header = record.headers().lastHeader("message_id");
                if (header != null) {
                    return UUID.fromString(new String(header.value()));
                }
            }
        }
        return null;
    }
}
