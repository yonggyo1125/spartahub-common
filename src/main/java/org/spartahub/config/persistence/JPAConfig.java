package org.spartahub.config.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JPAConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    @ConditionalOnMissingBean(JPAQueryFactory.class)
    private JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
