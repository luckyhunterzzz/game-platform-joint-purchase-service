package com.gameplatform.jointpurchaseservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gameplatform.jointpurchaseservice.kafka.event.TrustScoreCalculatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, TrustScoreCalculatedEvent> trustScoreCalculatedEventConsumerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ObjectMapper kafkaObjectMapper = new ObjectMapper();
        kafkaObjectMapper.registerModule(new JavaTimeModule());
        kafkaObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonDeserializer<TrustScoreCalculatedEvent> valueDeserializer =
                new JsonDeserializer<>(TrustScoreCalculatedEvent.class, kafkaObjectMapper, false);

        valueDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean(name = "trustScoreCalculatedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, TrustScoreCalculatedEvent> trustScoreCalculatedKafkaListenerContainerFactory(
            ConsumerFactory<String, TrustScoreCalculatedEvent> trustScoreCalculatedEventConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TrustScoreCalculatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(trustScoreCalculatedEventConsumerFactory);

        return factory;
    }
}