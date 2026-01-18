package com.prog3.security.Configurations;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Configuración personalizada de MongoDB para manejar conexiones SSL/TLS y mejorar la estabilidad
 * con MongoDB Atlas en entornos cloud
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * Cliente MongoDB configurado con opciones SSL mejoradas y timeouts adecuados
     */
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                // Configuración de timeouts más generosa para conexiones cloud
                .applyToSocketSettings(builder -> builder.connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS))
                // Configuración de cluster y selección de servidor
                .applyToClusterSettings(
                        builder -> builder.serverSelectionTimeout(60, TimeUnit.SECONDS))
                // Pool de conexiones optimizado
                .applyToConnectionPoolSettings(
                        builder -> builder.maxSize(50).minSize(10).maxWaitTime(60, TimeUnit.SECONDS)
                                .maxConnectionIdleTime(60, TimeUnit.SECONDS))
                // Retry de escrituras habilitado
                .retryWrites(true).build();

        return MongoClients.create(settings);
    }

    /**
     * Template de MongoDB usando el cliente configurado
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "vercy_motos");
    }
}
