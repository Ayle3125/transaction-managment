package org.ayle.transaction.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;

@Data
public class TransactionRequest {
    private String id;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    @NotNull(message = "Transaction category cannot be null")
    private TransactionCategory category;

    @NotNull(message = "Transaction status cannot be null")
    private TransactionStatus status;

    @NotNull(message = "Amount cannot be null")
    private Double amount;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Primary account cannot be blank")
    private String primaryAccount;

    private String counterpartyAccount;
}
