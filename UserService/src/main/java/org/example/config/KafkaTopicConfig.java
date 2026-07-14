package org.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userDeletionTopic(){
        return TopicBuilder.name("userDeletionTopic").build();
    }

    @Bean
    public NewTopic userCreationTopic(){
        return TopicBuilder.name("userCreationTopic").build();
    }
}