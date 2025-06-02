package com.keelean.accountmanager.enums;

import lombok.Getter;

@Getter
public enum AccountCapacity {
    MILLION_100(8, 100000000, "100 Million"),
    MILLION_10(7, 10000000, "10 Million"),
    MILLION_1(6, 1000000, "1 Million"),
    THOUSAND_100(5, 100000, "100 Thousand"),
    THOUSAND_10(4, 10000, "10 Thousand"),
    SHARED_POOL_1(MILLION_1.reusableDigits, MILLION_1.capacity, "1 Million Shared Pool"),
    SHARED_POOL_10(MILLION_10.reusableDigits, MILLION_10.capacity, "10 Million Shared Pool");

    //create a contructor to contain totalNumOfReusableDigits
    //define property to control how many SHARED_POOL capacity is for dynamic account different from static account.
    //shared-pool.dynamic-account=10,000,000
    //shared-pool.static-account=1,000,000

    private int reusableDigits;
    private int capacity;
    private String desc;

    AccountCapacity(int reusableDigits, int capacity, String desc) {
        this.reusableDigits = reusableDigits;
        this.capacity = capacity;
        this.desc = desc;
    }


    public static int getStartPrefixWidth(AccountCapacity capacity) {
        switch (capacity) {
            case MILLION_10:
                return 10 - MILLION_10.getReusableDigits() - 2;
            case MILLION_1:
                return 10 - MILLION_1.getReusableDigits() - 2;
            case SHARED_POOL_1:
                return 10 - SHARED_POOL_1.getReusableDigits() - 2;
            case SHARED_POOL_10:
                return 10 - SHARED_POOL_10.getReusableDigits() - 2;
        }
        return 0;
    }


}