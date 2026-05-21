package com.muyulu.mboard.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic albumRatingTopic(KafkaTopicsProperties topics) {
        return TopicBuilder.name(topics.getAlbumRating()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic songRatingTopic(KafkaTopicsProperties topics) {
        return TopicBuilder.name(topics.getSongRating()).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic albumCommentTopic(KafkaTopicsProperties topics) {
        return TopicBuilder.name(topics.getAlbumComment()).partitions(3).replicas(1).build();
    }
}
