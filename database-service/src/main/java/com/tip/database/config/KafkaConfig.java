package com.tip.database.config;

import com.tip.database.model.EnrichedIoc;
import com.tip.database.model.ValidatedIoc;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "database-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    @Bean
    public ConsumerFactory<String, ValidatedIoc> validatedIocConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(ValidatedIoc.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ValidatedIoc> validatedIocListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ValidatedIoc> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(validatedIocConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EnrichedIoc> enrichedIocConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(EnrichedIoc.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EnrichedIoc> enrichedIocListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EnrichedIoc> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(enrichedIocConsumerFactory());
        return factory;
    }
}
