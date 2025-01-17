package org.ayle.transaction.management.service;

import org.ayle.transaction.management.Exception.ErrorCode;
import org.ayle.transaction.management.Exception.TransactionException;
import org.ayle.transaction.management.entity.Transaction;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.model.TransactionListRequest;
import org.ayle.transaction.management.model.TransactionRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Service class for managing transactions.
 * Provides methods to list, create, update, and delete transactions.
 */
@Service
public class TransactionService {

    /**
     * In-memory storage for transactions.
     */
    private final ConcurrentSkipListMap<String, Transaction> transactions = new ConcurrentSkipListMap<>();

    /**
     * Retrieves a list of transactions based on the provided request parameters.
     *
     * @param request The request object containing filtering criteria.
     * @return A list of transactions matching the criteria.
     */
    @Cacheable(value = "transactionsCache", key = "#request.generateCacheKey()", unless = "#result == null")
    public List<Transaction> listTransactions(TransactionListRequest request) {

        return transactions.values().stream().filter(t -> t.getStatus() != TransactionStatus.DELETED)
                .filter(t -> request.getType() == null || t.getType() == request.getType())
                .filter(t -> request.getCategory() == null || t.getCategory() == request.getCategory())
                .filter(t -> request.getStatus() == null || t.getStatus() == request.getStatus())
                .skip((long) request.getPageSize() * (request.getPageNo() - 1))
                .limit(request.getPageSize()).collect(Collectors.toList());
    }

    /**
     * Creates a new transaction.
     *
     * @param request The request object containing transaction details.
     * @return The ID of the newly created transaction.
     * @throws TransactionException If the transaction already exists or validation fails.
     */
    @CacheEvict(value = "transactionsCache", allEntries = true)
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

    /**
     * Updates an existing transaction.
     *
     * @param request The request object containing updated transaction details.
     * @return The ID of the updated transaction.
     * @throws TransactionException If the transaction is not found or validation fails.
     */
    @CacheEvict(value = "transactionsCache", allEntries = true)
    public String updateTransaction(TransactionRequest request) {
        if (request.getId() == null || !transactions.containsKey(request.getId())) {
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

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return The transaction object.
     * @throws TransactionException If the transaction is not found.
     */
    public Transaction getTransaction(String id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null || transaction.getStatus().equals(TransactionStatus.DELETED)) {
            throw new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        return transaction;
    }

    /**
     * Deletes a transaction by marking it as deleted.
     *
     * @param id The ID of the transaction to delete.
     * @throws TransactionException If the transaction is not found.
     */
    @CacheEvict(value = "transactionsCache", allEntries = true)
    public void deleteTransaction(String id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            throw new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        transaction.setStatus(TransactionStatus.DELETED);
        transaction.setUpdateTime(LocalDateTime.now());
    }

    /**
     * Validates the transaction request.
     *
     * @param request The request object containing transaction details.
     * @throws TransactionException If validation fails.
     */
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