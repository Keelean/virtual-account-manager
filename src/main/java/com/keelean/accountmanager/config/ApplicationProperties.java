package com.keelean.accountmanager.config;

import com.keelean.accountmanager.constants.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@ConfigurationProperties("account")
@Component
public class ApplicationProperties {

    // Include exception details in error responses (devErrorMessage); keep off outside development
    private boolean injectDevErrorMessage;

    @Data
    @ConfigurationProperties("kafka")
    @Component
    public static class KafkaProperties {
        private String bootstrapServer;
        private String producerAcks = AppConstants.KAFKA_PRODUCER_ACK;
        private int producerRetries = AppConstants.KAFKA_PRODUCER_RETRY;
        private String defaultTopic;
        private String notifyTopic;
    }

    @Data
    @ConfigurationProperties("spring.kafka.consumer")
    @Component
    public static class KafkaConsumerProperties {
        private int kafkaConcurrentSize;
        private String bootstrapServer;
    }

}
