package org.ayle.transaction.management.service;

import org.ayle.transaction.management.Exception.ErrorCode;
import org.ayle.transaction.management.Exception.TransactionException;
import org.ayle.transaction.management.entity.Transaction;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.model.TransactionListRequest;
import org.ayle.transaction.management.model.TransactionRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final ConcurrentSkipListMap<String, Transaction> transactions = new ConcurrentSkipListMap<>();

    public List<Transaction> listTransactions(TransactionListRequest request) {
        Map<String, Transaction> transactionsTail = transactions.tailMap(request.getLastId(), false);

        return transactionsTail.values().stream().filter(t -> t.getStatus() != TransactionStatus.DELETED)
                .filter(t -> request.getType() == null || t.getType() == request.getType())
                .filter(t -> request.getCategory() == null || t.getCategory() == request.getCategory())
                .filter(t -> request.getStatus() == null || t.getStatus() == request.getStatus())
                .limit(request.getSize()).collect(Collectors.toList());
    }

    public String createTransaction(TransactionRequest request) {
        if (request.getId() != null && transactions.containsKey(request.getId())) {
            throw new TransactionException(ErrorCode.TRANSACTION_ALREADY_EXISTS);
        }
        validateTransaction(request);
        Transaction transaction = new Transaction();
        transaction.setId(request.getId() != null ? request.getId() : UUID.randomUUID().toString());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setStatus(request.getStatus());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setDescription(request.getDescription());
        transaction.setPrimaryAccount(request.getPrimaryAccount());
        transaction.setCounterpartyAccount(request.getCounterpartyAccount());
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        transactions.put(transaction.getId(), transaction);
        return transaction.getId();
    }

    public String updateTransaction(TransactionRequest request) {
        if (!transactions.containsKey(request.getId())) {
            throw new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        validateTransaction(request);
        Transaction transaction = transactions.get(request.getId());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setStatus(request.getStatus());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setDescription(request.getDescription());
        transaction.setPrimaryAccount(request.getPrimaryAccount());
        transaction.setCounterpartyAccount(request.getCounterpartyAccount());
        transaction.setUpdateTime(LocalDateTime.now());
        return request.getId();
    }

    public Transaction getTransaction(String id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null || transaction.getStatus().equals(TransactionStatus.DELETED)) {
            throw new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        return transaction;
    }

    public void deleteTransaction(String id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            throw new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        transaction.setStatus(TransactionStatus.DELETED);
        transaction.setUpdateTime(LocalDateTime.now());
    }

    private void validateTransaction(TransactionRequest request) {
        if (!request.getType().isValidCategory(request.getCategory())) {
            throw new TransactionException(ErrorCode.INVALID_TRANSACTION_CATEGORY);
        }

        if ((request.getCategory() == TransactionCategory.TRANSFER_IN ||
                request.getCategory() == TransactionCategory.TRANSFER_OUT) && request.getCounterpartyAccount() == null) {
            throw new TransactionException(ErrorCode.COUNTERPARTY_ACCOUNT_REQUIRED);
        }
    }
}