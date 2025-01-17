package org.ayle.transaction.management.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;

import java.io.Serializable;

@Data
public class TransactionListRequest implements Serializable{

    private TransactionType type;

    private TransactionCategory category;

    private TransactionStatus status;

    @NotNull(message = "PageSize cannot be null")
    @Positive
    private int pageSize;

    @NotNull(message = "pageNo cannot be null")
    @Positive
    private int pageNo;

    public String generateCacheKey() {
        String key = this.pageNo + "-" + this.pageSize;
        if (this.type != null){
            key += "-" + this.type.name();
        }
        if (this.category != null){
            key += "-" + this.category.name();
        }
        if (this.status != null){
            key += "-" + this.status.name();
        }
        return key;
    }
}
