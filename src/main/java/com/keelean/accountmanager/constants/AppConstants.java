package com.keelean.accountmanager.constants;

import java.util.Set;

public class AppConstants {

    private AppConstants() {}

    // Customer controller url path
    public static final String CUSTOMER_BASE_URL = "/customers";
    public static final String CUSTOMER_CONFIG_PATH = "/config/customers";
    public static final String GET_CUSTOMER_BY_MSISDN = "/{msisdn}";
    public static final String NOTIFY_CUSTOMER_API_PATH = "/notify/{partnerCode}";
    public static final String NOTIFY_CUSTOMER_V2_API_PATH = "/{product}/notify/{partnerCode}";
    public static final String NOTIFY_CUSTOMER_V3_API_PATH = "/{product}/v3/notify";

    // Account controller url path
    public static final String ACCOUNT_BASE_URL = "/accounts";
    public static final String ACCOUNT_V2_BASE_URL = "/v2/accounts";
    public static final String ACCOUNT_V3_BASE_URL = "/v3/accounts";
    public static final String GET_ACCOUNT_LIST = "/{product}";
    public static final String LINK_ACCOUNT = "/link";
    public static final String BI_LINK_ACCOUNT = "/link/{partnerCode}";
    public static final String SEND_OTP_API_PATH = "/{intend}/send-otp/{partnerCode}";
    public static final String UPDATE_ACCOUNT = "/{accountId}";
    public static final String ACCOUNT_CALLBACK = "/update/{partnerCode}";
    public static final String ACCOUNT_CLOSURE = "/request-closure/{partnerCode}";
    public static final String BI_ACCOUNT_CLOSURE = "/closure/{partnerCode}";
    public static final String GET_BALANCE = "/balance";
    public static final String GET_ACCOUNT_STATEMENT = "/account-statement";
    public static final String GET_MINI_STATEMENT = "/mini-statement";
    public static final String UPGRADE_PROFILE_URL = "/profile-upgrade";
    public static final String VALIDATE_FOR_LINKAGE = "/validate-for-linkage";

    // Transaction controller url path
    public static final String TXN_BASE_URL = "/txn";
    public static final String DEPOSIT_URL = "/{product}/deposit";
    public static final String WITHDRAWAL_URL = "/{product}/withdrawal";
    public static final String FUND_TRANSFER_URL = "/fund-transfer/{partnerCode}";

    // Reports controller url path
    public static final String REPORT_BASE_URL = "/reports";
    public static final String OPTIN_DETAILS = "/optin-details";

    //Kafka
    public static final String KAFKA_PRODUCER_ACK = "all";
    public static final Integer KAFKA_PRODUCER_RETRY = 1000;
    public static final String KAFKA_TOPIC_KEY = "kafka_topic";
    public static final String CREATE_CUS_ACC_TOPIC = "transaction_store";

    //Biller Api names
    public static final String ACCOUNT_CREATION_API_NAME = "CreateAccount";
    public static final String BALANCE_INQUIRY_API_NAME = "BalanceInquiry";
    public static final String ACCOUNT_CLOSURE_API_NAME = "AccountDeletion";
    public static final String ACCOUNT_STATEMENT_API_NAME = "FullAccountStatement";
    public static final String MINI_STATEMENT_API_NAME = "MiniStatement";
    public static final String UPGRADE_PROFILE_API_NAME = "UpgradeProfile";

    //Savings
    public static final String API_KEY = "api";

    //LLM Api Names
    public static final String USER_OPTIN_API_NAME = "UserOptin";
    public static final String USER_OPTOUT_API_NAME = "UserOptout";

    public static final String LLM_OPTIN_TOPIC_NAME = "llm-user-optin";

    //gateway
    public static final String X_SERVICE_ID = "x-service-id";
    public static final String X_APP_VERSION = "x-app-version";

    public static final String SUCCESS = "Success";
    public static final String ENABLED_STATUS = "ENABLED";
    public static final String MSISDN_KEY = "msisdn";
    public static final String PARTNER_CODE_KEY = "partnerCode";
    public static final String PRODUCT = "product";
    public static final String X_PRODUCT = "x-product";
    public static final String MESSAGE = "message";
    public static final String BALANCE_KEY = "balance";
    public static final String EMAIL_ID_KEY = "emailId";
    public static final String X_CHANNEL_NAME = "x-channel-name";
    public static final String KYC = "kyc";
    public static final String ADD_INFO = "additionalInfo";
    public static final String X_API_CLIENT = "X-Api-Client";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String ISO_LOCAL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+SSSS";
    public static final String ACCOUNT_LIST = "accounts";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String DATE_OF_BIRTH = "dob";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,6}$";
    public static final String EMAIL_ERROR_MESSAGE = "Invalid email address";
    public static final String NATIONALITY = "nationality";
    public static final String ID_TYPE = "id_type";
    public static final String ID_NUMBER = "id_number";
    public static final String IDTYPE = "idType";
    public static final String IDNUMBER = "idNumber";
    public static final String WEB_JARS_PATH = "/webjars/**";
    public static final String CUSTOMER_NOTIFICATION_LOG_MSG = "Customer {} notified successfully";
    public static final String LLM_SWITCH_FAILURE_LOG_MSG = "Non-success response from LLM Switch API for apiName : {}";
    public static final String BILLER_SWITCH_FAILURE_LOG_MSG = "Non-success response from Biller Switch API for apiName : {}";
    public static final String KYC_FAILURE_LOG_MSG = "Non-success response from KYC API for apiName : {}";
    public static final Set<String> CUSTOMER_PROFILE_KEYS = Set.of(AppConstants.FIRSTNAME,
            AppConstants.DATE_OF_BIRTH, AppConstants.NATIONALITY, AppConstants.IDTYPE, AppConstants.IDNUMBER);
    public static final String CURRENCY_NODE = "currencyCode";
    public static final String TRANSACTION_DETAILS = "transactionDetails";
    public static final String EXTRA_DATA = "extraData";
    public static final String ASP_REGION = "asp-region";
    public static final String X_CLIENT_ID = "x-client-id";
    public static final String KYC_AM_X_CLIENT_ID_VALUE = "BILLER";
    public static final String RESPONSE = "response";
    public static final String REDIS_KYC_DETAILS_AM = "Kyc-Details-Am";
    public static final String VALIDATE_FOR_LINKAGE_SUCCESS_RESPONSE = "Validation Success";
    public static final String VALIDATE_FOR_LINKAGE_FAILURE_RESPONSE = "Validation Failure";
    public static final String MULTIPLE_ACCOUNT_FLOW_FLAG = "multipleAccountFlow";

    //Retry Config
    public static final String RETRY_BASE_URL = "/retry-config";
    public static final String RETRY_CONFIG_CACHE = "retryConfigCache";

    public static final Set<String> CUSTOMER_PROFILE_AM_KEYS = Set.of(AppConstants.FIRST_NAME,
            AppConstants.DATE_OF_BIRTH, AppConstants.ID_TYPE, AppConstants.ID_NUMBER);
    public static final String CUSTOM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.s";
    public static final String SUCCESS_LINKAGE_MSG = "account linkage successful";
    public static final String SUCCESS_DELINKAGE_MSG = "account delinkage successful";
    public static final String FAILURE_LINKAGE_MSG = "Account linkage failure due to invalid KYC, SMS sent to subscriber";

    public static final String NOTIFICATION_CONFIG = "/v1/notifications";
    public static final String CREATE_CONFIG = "/config/create";
    public static final String UPDATE_CONFIG = "/config/update/{id}";
    public static final String GET_CONFIGS = "/configs";
    public static final String DELETE_CONFIG = "/config/delete/{id}";
    public static final String LANG_EN = "en";
    public static final String ACCOUNT_API_NAME = "AccountLinking";
    public static final String ACCOUNT_DELINKING_API_NAME = "BankInitiatedAccountDeLinking";
    public static final String VALIDATE_ACTIVE_AM_USER = "/validateActiveAMUser";
    public static final String USER_BARRED_KEY = "user_barred";
    public static final String DATA = "data";
    public static final String FROM_DATE = "from_date";
    public static final String TO_DATE = "to_date";

    public static final String VIRTUAL_ACCOUNT_BASE_URL = "/v1/virtualAccounts";
    public static final String ACCOUNT_POOL_BASE_URL = "/v1/account-pool";

    public static final String WLS_TOPIC_NAME = "wls_requests";

    public static final String VIRTUAL_ACCOUNT_KEY_ID = "virtualAccountId";
    public static final String ACCEPTED_SETTLEMENT_COMPLETED = "ACCEPTED_SETTLEMENT_COMPLETED";

    public static final String VIRTUAL_ACCOUNT_PARTNER_CONFIG = "/v1/va/configs";
    public static final String HIBERNATE_SESSION_FACTORY = "HibernateSessionFactory";

    public static String RETRY_ACC_CREATION_QUEUE = "retry-acc-creation-queue";
    public static String RETRY_USER_OPTIN_QUEUE = "retry-user-optin-queue";
}
