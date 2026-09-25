package com.keelean.accountmanager.enums;

public enum AccountMode {
    // Static account for a single invoice; closes once the invoice is paid
    STATIC_INVOICE_CLOSED,
    // Static invoice account that stays open for further payments (not assigned at creation)
    STATIC_INVOICED_EXTENDED,
    // Static account without an invoice
    STATIC_NORMAL,
    DYNAMIC
}
