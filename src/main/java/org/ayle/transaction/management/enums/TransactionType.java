package org.ayle.transaction.management.enums;

import java.util.Set;

public enum TransactionType {
    DEPOSIT(Set.of(TransactionCategory.TRANSFER_IN, TransactionCategory.CASH, TransactionCategory.REPAYMENT_REFUND)),
    WITHDRAWAL(Set.of(TransactionCategory.TRANSFER_OUT, TransactionCategory.CASH, TransactionCategory.PAYMENT));

    private final Set<TransactionCategory> validCategories;

    TransactionType(Set<TransactionCategory> validCategories) {
        this.validCategories = validCategories;
    }

    public boolean isValidCategory(TransactionCategory category) {
        return validCategories.contains(category);
    }
}
