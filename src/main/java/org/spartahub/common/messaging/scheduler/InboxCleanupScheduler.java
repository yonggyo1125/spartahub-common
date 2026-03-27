package org.spartahub.common.messaging.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spartahub.common.domain.Inbox;
import org.spartahub.common.domain.InboxRepository;
import org.spartahub.common.domain.QInbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class InboxCleanupScheduler {
    private final InboxRepository inboxRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 정리 작업 진행
    public void cleanupOldMessage() {
        // 7일 이전 목록 조회
        List<Inbox> items = (List<Inbox>) inboxRepository.findAll(QInbox.inbox.processedAt.before(LocalDateTime.now().minusWeeks(1L)));

        inboxRepository.deleteAll(items);
    }
}
