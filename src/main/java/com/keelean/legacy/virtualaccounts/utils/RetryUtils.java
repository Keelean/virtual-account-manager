package com.keelean.legacy.customeraccounts.utils;

import com.keelean.legacy.customeraccounts.config.ApplicationProperties;
import com.keelean.legacy.customeraccounts.constants.VirtualAccountConstants;
import com.keelean.legacy.rqueue.annotation.RqueueListener;
import com.keelean.legacy.rqueue.core.RqueueMessage;
import com.keelean.legacy.rqueue.core.RqueueMessageEnqueuer;
import com.keelean.legacy.rqueue.core.support.RqueueMessageUtils;
import com.keelean.legacy.rqueue.utils.backoff.ExponentialTaskExecutionBackOff;
import com.example.platform.core.pojo.transaction.TransactionDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryUtils {

    @Autowired
    RqueueMessageEnqueuer messageEnqueuer;

    @Autowired
    ApplicationProperties.RetryProperties properties;


    private void sendMessage(String queue, TransactionDetailsDto txnDetails) {
        RqueueMessage rqueueMessage = RqueueMessageUtils
                .buildMessage(queue, txnDetails, properties.getCount(), properties.getDelay(),
                        new ExponentialTaskExecutionBackOff(properties.getInitialInterval(),
                                properties.getMaxInterval(), properties.getIntervalMultiplier(),
                                properties.getCount()));
        String messageId =
                messageEnqueuer.enqueueInWithRetry(
                        queue, rqueueMessage, properties.getCount(), properties.getDelay());
        log.info("CreateAccount : TxnDetails {} with customerMsisdn {} queued for retry with message id {}", txnDetails,
                txnDetails.getPayeeAccountNo(), messageId);
    }

    public void retryCreateAccount(TransactionDetailsDto txnDetails){
        log.info("create account failed. retrying create account for transactionId : {} and customerMsisdn {}",
                txnDetails.getExternalId(), txnDetails.getPayeeAccountNo());
        sendMessage(VirtualAccountConstants.RETRY_ACC_CREATION_QUEUE, txnDetails);
    }

    @RqueueListener(value= VirtualAccountConstants.RETRY_ACC_CREATION_QUEUE, deadLetterQueue = "false")
    public void processRetryCreateAccount(TransactionDetailsDto txnDetails){
        log.info("Received retryCreateAccount with txnDetails payload : {} and customerMsisdn {}",
                txnDetails, txnDetails.getPayeeAccountNo());
    }
}
