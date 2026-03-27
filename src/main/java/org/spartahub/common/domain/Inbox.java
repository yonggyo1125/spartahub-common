package org.spartahub.common.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@Access(AccessType.FIELD)
@Table(name = "P_INBOX", indexes = {
        @Index(name = "idx_inbox_message_group", columnList = "messageGroup"),
        @Index(name = "idx_inbox_processed_at", columnList = "processedAt")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Inbox {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(length=36, name="message_id")
    protected UUID id; // Outbox에 등록된 메세지 ID와 동일하게 유지

    @Column(length=50)
    protected String messageGroup;

    @CreatedDate
    @Column(updatable=false)
    protected LocalDateTime processedAt;
}
