package org.ayle.transaction.management.Exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class TransactionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    public TransactionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

