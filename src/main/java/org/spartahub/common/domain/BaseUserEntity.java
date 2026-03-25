package org.spartahub.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.spartahub.common.util.SecurityUtil;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@Access(AccessType.FIELD)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUserEntity extends BaseEntity {
    @CreatedBy
    @Column(length=45, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(length=45, insertable = false)
    protected String modifiedBy;

    @Column(length=45, insertable = false)
    protected String deletedBy;

    protected void delete() {
        deletedBy = SecurityUtil.getCurrentUsername().orElse(null);
        deletedAt = LocalDateTime.now();
    }
}
