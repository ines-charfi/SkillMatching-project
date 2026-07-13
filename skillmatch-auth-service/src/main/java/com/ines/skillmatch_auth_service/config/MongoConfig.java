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

/**
 * MongoDB configuration class for the authentication service.
 * Enables auditing, repository scanning, and ensures database indexes
 * are created automatically at application startup.
 */
@Configuration
// Enables automatic population of @CreatedDate, @LastModifiedDate, etc. in entities.
@EnableMongoAuditing
// Tells Spring Data MongoDB where to scan for repository interfaces.
@EnableMongoRepositories(basePackages = "com.ines.skillmatch_auth_service.repository.mongodb")
@RequiredArgsConstructor
public class MongoConfig {

    // Low-level MongoDB operations – used to create indexes.
    private final MongoTemplate mongoTemplate;
    // Holds metadata about MongoDB entity classes (like Notification).
    private final MongoMappingContext mongoMappingContext;

    /**
     * Runs after the application context is fully initialized.
     * Ensures that all indexes defined via annotations (e.g., @Indexed, @CompoundIndex)
     * in the Notification entity are created in the database.
     */
    @PostConstruct
    public void initIndicesAfterStartup() {
        // Get index operations for the Notification collection
        IndexOperations indexOps = mongoTemplate.indexOps(Notification.class);
        // Create an index resolver that reads index definitions from entity annotations
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
        // Resolve all indexes declared in Notification and apply them to the collection
        resolver.resolveIndexFor(Notification.class).forEach(indexOps::ensureIndex);
    }
}