package com.keelean.accountmanager.constants;

public class AppConstants {

    // Minimum time a dynamic account stays open for payment (24 hours)
    public static final long DYNAMIC_PAYMENT_WINDOW_MINS = 1440;
    public static final long DYNAMIC_ACCOUNT_EXPIRY_DAYS = 90;

    private AppConstants() {}

    //Kafka
    public static final String KAFKA_PRODUCER_ACK = "all";
    public static final Integer KAFKA_PRODUCER_RETRY = 1000;

    public static final String VIRTUAL_ACCOUNT_BASE_URL = "/v1/virtualAccounts";
    public static final String ACCOUNT_POOL_BASE_URL = "/v1/account-pool";

    public static final String WLS_TOPIC_NAME = "wls_requests";

    public static final String VIRTUAL_ACCOUNT_KEY_ID = "virtualAccountId";
    public static final String ACCEPTED_SETTLEMENT_COMPLETED = "ACCEPTED_SETTLEMENT_COMPLETED";

    public static final String VIRTUAL_ACCOUNT_PARTNER_CONFIG = "/v1/va/configs";
    public static final String HIBERNATE_SESSION_FACTORY = "HibernateSessionFactory";

}
