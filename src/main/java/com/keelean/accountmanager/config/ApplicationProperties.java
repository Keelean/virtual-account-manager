package com.keelean.accountmanager.config;

import com.keelean.accountmanager.constants.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("account")
@Component
@RefreshScope
public class ApplicationProperties {

    @NotNull
    private String switchName;

    // Include exception details in error responses (devErrorMessage); keep off outside development
    private boolean injectDevErrorMessage;
    

    @Data
    @ConfigurationProperties("technical")
    @Component
    public static class TechnicalProperties {

        private List<Integer> errorCodes = Arrays
                .asList(408, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511);
    }


    @Data
    @ConfigurationProperties("kyc.source")
    @Component
    public static class KYCSourceProperties{
        @NotNull
        private String baseUrl;
        @NotNull
        private String kyc1Path;
        @NotNull
        private String amPath;
        @NotNull
        private String appVersion;
        @NotNull
        private String serviceId;
    }

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
    @ConfigurationProperties("biller")
    @Component
    public static class BillerProperties{
        private String baseUrl;
        private String createAccount;
        private String deleteAccount;
        private String balanceInquiry;
        private String miniStatement;
        private String fullStatement;
        private String upgradeProfile;
    }

    @Data
    @ConfigurationProperties("llm")
    @Component
    public static class LLMProperties {
        private String baseUrl;
        private String userOptin;
        private String userOptout;
        private String balanceInquiry;
        private String miniStatement;
        private List<String> createCustomerAccountTxnType;
        private String optinCallback;
        private String preRuleValidationErrorCode = "SWITCH4011";
    }

    @Data
    @ConfigurationProperties("notify")
    @Component
    public static class NotificationProperties {
        private String notificationClientKey;
        private String lang;
        private String deLinkingProcessName;
        private String linkingProcessName;
        private String kycValidationProcessName;
    }

    @Data
    @ConfigurationProperties("notify.template.name.account")
    @Component
    public static class AccountNotifyTemplateNameProperties {
        //Create account
        private String createFailure;
        private String createSuccess;
        private String createCallback;

        //Get balance
        private String balanceFailure;
        private String balanceSuccess;

        //Get Mini-statement
        private String miniStatementFailure;
        private String miniStatementSuccess;

        //Upgrade Profile
        private String upgradeProfileFailure;
        private String upgradeProfileSuccess;

        //Get statement
        private String statementFailure;
        private String statementSuccess;

        //delete account
        private String closureFailure;
        private String closureSuccess;
        private String closureCallback;
    }

    @Data
    @ConfigurationProperties("notify.template.name.customer")
    @Component
    public static class CustomerNotifyTemplateNameProperties {

        private String notifyCallback;
    }

    @Data
    @ConfigurationProperties("savings")
    @Component
    public static class SavingsProperties {
        private List<String> createSavingsAccountTxnType;
        private List<String> createSavingsAccountPartner;
    }

    @Data
    @ConfigurationProperties("retry")
    @Component
    public static class RetryProperties {
        private int count;
        private long delay;
        private long initialInterval;
        private int intervalMultiplier;
        private long maxInterval;

        @PostConstruct
        private void init() throws IllegalStateException {
            if(this.initialInterval > this.maxInterval) {
                throw new IllegalStateException("Please check configuration for RetryProperties. Initial Interval cannot be greater than Max Interval");
            }
        }
    }

    @Data
    @ConfigurationProperties("spring.kafka.consumer")
    @Component
    public static class KafkaConsumerProperties {
        private int kafkaConcurrentSize;
        private String bootstrapServer;
    }


    private String secretKey;

    private List<String> encryptFields;

}
