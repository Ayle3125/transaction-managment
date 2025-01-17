package org.ayle.transaction.management.Exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    TRANSACTION_ALREADY_EXISTS("1001", "Transaction ID already exists"),
    TRANSACTION_NOT_FOUND("1002", "Transaction not found"),
    INVALID_TRANSACTION_CATEGORY("1003", "Invalid category for the specified transaction type"),
    COUNTERPARTY_ACCOUNT_REQUIRED("1004", "Counterparty account is required for transfer transactions")
    ;

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
