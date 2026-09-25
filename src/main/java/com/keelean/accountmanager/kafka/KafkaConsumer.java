package com.keelean.accountmanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keelean.accountmanager.exception.TechnicalErrorException;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import com.keelean.accountmanager.repo.AccountTransactionRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class KafkaConsumer {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  AccountCustomerRepo accountCustomerRepo;

  @Autowired
  AccountTransactionRepo accountTransactionRepo;

  /*
  @KafkaListener(topics = "${kafka.consumer-llm-user-optin-topic}", clientIdPrefix = "string",
          containerFactory = "kafkaListenerStringContainerFactory", autoStartup = "${kafka.listen.auto.start}")
  public void userOptIn(GenericMessage<Object> message, Acknowledgment ack) {
    try {
      log.info("Received message {}", message.getPayload());
      UserOptinDTO userOptinDTO = objectMapper.readValue(message.getPayload().toString(), UserOptinDTO.class);
      accountService.userOptinFromKafka(userOptinDTO);
    } catch (JsonProcessingException exception) {
      log.info("Received some other message type which is not UserOptinDTO");
    } finally {
      ack.acknowledge();
    }
  }*/

  //@KafkaListener(topics = "${kafka.consumer-topic}")
  /*
  @KafkaListener(topics = "${kafka.consumer-transaction-topic}", clientIdPrefix = "string",
          containerFactory = "kafkaListenerStringContainerFactory", autoStartup = "${kafka.listen.auto.start}")
  public void createCustomerAndAccount(GenericMessage<Object> message, Acknowledgment ack) throws JsonProcessingException {
    TransactionDetailsDto txnDetails = null;
    try{
      txnDetails = objectMapper.readValue(
              message.getPayload().toString(), TransactionDetailsDto.class);
      log.info("Listener || create customer & account : Received message for txn_type: {} and status: {}"
          ,txnDetails.getTxnType(), txnDetails.getStatus());
      String msisdn = txnDetails.getPayeeAccountNo();
      if(TxnProcessStatus.PROCESSED.equals(txnDetails.getStatus())
              && TransactionDirection.PULL.equals(txnDetails.getTransactionDirection())
              && llmProperties.getCreateCustomerAccountTxnType().contains(txnDetails.getTxnType())){

        Map<String, Object> kycResponseData = new HashMap<>();
        log.info("kycResponseData for msisdn: {} is : {}", msisdn, kycResponseData);
        accountService.createCustomerWithAcc(kycResponseData, txnDetails);
        log.info("Customer and Account creation completed for msisdn {}", msisdn);
      }
      if(TxnProcessStatus.PROCESSED.equals(txnDetails.getStatus())
              && TransactionDirection.PULL.equals(txnDetails.getTransactionDirection())
              && savingsProperties.getCreateSavingsAccountTxnType().contains(txnDetails.getTxnType())
              && savingsProperties.getCreateSavingsAccountPartner().contains(txnDetails.getPayer().getCode())){
        log.info("calling createSavingsAccountForCustomer for msisdn : {}", msisdn);
        accountService.createSavingsAccountForCustomer(txnDetails);
      }
    } catch(TechnicalErrorException e){
      log.info("TechnicalErrorException thrown for create account. Pushing to retry queue");
      pushToRetryQueue(txnDetails);
      log.info("Retry create account for transactionId : {} customerMsisdn {}", txnDetails.getExternalId(), txnDetails.getPayeeAccountNo());
    }
    catch (Exception e){
      log.error("Listener || create customer & account : failed with message {}", e.getMessage());
    } finally {
      ack.acknowledge();
    }
  }

  public void pushToRetryQueue(TransactionDetailsDto txnDetails){
    retryUtils.retryCreateAccount(txnDetails);
  }
*/
  /*
  @KafkaListener(topics = AppConstants.WLS_TOPIC_NAME, clientIdPrefix = "string",
          containerFactory = "kafkaListenerStringContainerFactory", autoStartup = "${kafka.listen.auto.start}")
  public void updateVirtualCustomerAccountInfo(GenericMessage<Object> message, Acknowledgment ack) {
    try {
      log.info("Virtual account paymentConfirmationRequests start :: Received Message {}", message.getPayload());
      TransactionDetailsWLSDto txnDetailsDto = objectMapper.convertValue(message.getPayload(), TransactionDetailsWLSDto.class);

      Map<String, Object> payeeParams = txnDetailsDto.getPayee().getCustomerParams();
      if(Objects.nonNull(payeeParams)){
        String virtualAccountId = (String)payeeParams.getOrDefault(AppConstants.VIRTUAL_ACCOUNT_KEY_ID, null);
        if(Objects.nonNull(virtualAccountId)){
          AccountCustomer customer = accountCustomerRepo.getVirtualAccountFromList(virtualAccountId);
          AccountStatus accountStatus;
          if(Objects.nonNull(customer)){
            if(txnDetailsDto.getState().equals(AppConstants.ACCEPTED_SETTLEMENT_COMPLETED)){
              accountStatus = customer.getMeta().getAccountType() == VirtualAccountMode.DYNAMIC? AccountStatus.COMPLETED_UNUSABLE : AccountStatus.COMPLETED_USABLE;

              Transaction vt = accountTransactionRepo.findByTransactionId(txnDetailsDto.getTransactionId());

              if(vt == null){
                Transaction vat = Transaction.builder()
                        .customerId(customer.getId())
                        .actualAmount(txnDetailsDto.getTotalAmount())
                        .transactionId(txnDetailsDto.getTransactionId())
                        .paymentReceivedDate(txnDetailsDto.getInnitiatedTs())
                        .build();
                accountTransactionRepo.save(vat);
              }
            }else{
              accountStatus = customer.getStatus();
            }
            customer.setStatus(accountStatus);
            if(customer.getMeta().getAccountType() == VirtualAccountMode.STATIC){
              customer.setExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.YEARS));
            }
            accountCustomerRepo.save(customer);
          }
        }
      }
      log.info("Virtual Account PaymentConfirmationRequests end :: Received Message");
    } catch (Exception exception) {
      log.error("Virtual Account PaymentConfirmationRequests :: , Exception: {}", exception.getMessage());
    } finally {
      ack.acknowledge();
    }
  }*/

}
