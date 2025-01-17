package org.ayle.transaction.management.model;

import lombok.Data;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;

@Data
public class TransactionListRequest {
    private TransactionType type;
    private TransactionCategory category;
    private TransactionStatus status;
    private String lastId = "0"; // last id of the last list request
    private int size;
}
