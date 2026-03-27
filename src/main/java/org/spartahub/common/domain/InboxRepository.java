package org.spartahub.common.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.UUID;

public interface InboxRepository extends JpaRepository<Inbox, UUID>, QuerydslPredicateExecutor<Inbox> {
}
