package org.ayle.transaction.management.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements Serializable {

    private String id;

    private TransactionType type;

    private TransactionCategory category;

    private TransactionStatus status;

    private BigDecimal amount;

    private String description;

    /**
     * Primary account involved in the transaction.
     */
    private String primaryAccount;

    /**
     * Counterparty account involved in the transaction.
     */
    private String counterpartyAccount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
