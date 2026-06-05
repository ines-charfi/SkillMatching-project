package com.ines.skillmatch_auth_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import jakarta.annotation.PostConstruct;
import com.ines.skillmatch_auth_service.model.Notification;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.ines.skillmatch_auth_service.repository.mongodb")
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;

    @PostConstruct
    public void initIndicesAfterStartup() {
        IndexOperations indexOps = mongoTemplate.indexOps(Notification.class);
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
        resolver.resolveIndexFor(Notification.class).forEach(indexOps::ensureIndex);
    }
}
