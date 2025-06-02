package com.keelean.accountmanager.config;

import com.keelean.accountmanager.constants.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
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
    
    @Data
    @ConfigurationProperties("payments")
    @Component
    public class PaymentsProperties{
      private String baseUrl;
      private String processTxnPath;

    }

    @Data
    @ConfigurationProperties("technical")
    @Component
    public class TechnicalProperties {

        private List<Integer> errorCodes = Arrays
                .asList(408, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511);
    }

    @Data
    @ConfigurationProperties("gateway")
    @Component
    public class GatewayProperties{
        private String baseUrl;
        private String kycPath;
        private String appVersion;
        private String serviceId;
    }

    @Data
    @ConfigurationProperties("kyc.source")
    @Component
    public class KYCSourceProperties{
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
    public class KafkaProperties {
        private String bootstrapServer;
        private String producerAcks = AppConstants.KAFKA_PRODUCER_ACK;
        private int producerRetries = AppConstants.KAFKA_PRODUCER_RETRY;
        private String defaultTopic;
        private String notifyTopic;
    }

    @Data
    @ConfigurationProperties("biller")
    @Component
    public class BillerProperties{
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
    public class LLMProperties {
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
    public class SavingsProperties {
        private List<String> createSavingsAccountTxnType;
        private List<String> createSavingsAccountPartner;
    }

    @Data
    @ConfigurationProperties("retry")
    @Component
    public class RetryProperties {
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
    public class KafkaConsumerProperties {
        private int kafkaConcurrentSize;
        private String bootstrapServer;
    }

    @Data
    @ConfigurationProperties("bus")
    @Component
    public class BusProperties {
        private String amProfileBaseUrl;
        private String amTransactionBaseUrl;
        private String amLastTransactionApiUrl;
        private String xClientId;
        private long validateUserlastTransactionMaxTimeInEpochTime=2629800;
    }

    private String secretKey;

    private List<String> encryptFields;

}
