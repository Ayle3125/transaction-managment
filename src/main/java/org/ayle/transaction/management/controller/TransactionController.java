package org.ayle.transaction.management.controller;

import org.ayle.transaction.management.Exception.TransactionException;
import org.ayle.transaction.management.entity.Transaction;
import org.ayle.transaction.management.model.TransactionListRequest;
import org.ayle.transaction.management.model.TransactionRequest;
import org.ayle.transaction.management.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing transactions.
 * Provides endpoints to list, create, update, and delete transactions.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Lists all transactions based on the provided request parameters.
     *
     * @param request The request object containing filtering criteria.
     * @return A list of transactions matching the criteria.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<List<Transaction>> listTransactions(@Validated TransactionListRequest request) {
        return ResponseEntity.ok(transactionService.listTransactions(request));
    }

    /**
     * Creates a new transaction.
     *
     * @param request The request object containing transaction details.
     * @return A success message or an error response.
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> createTransaction(@Validated @RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.ok(transactionService.createTransaction(request));
        } catch (TransactionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates an existing transaction.
     *
     * @param request The request object containing updated transaction details.
     * @return A success message or an error response.
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<String> updateTransaction(@Validated @RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.ok(transactionService.updateTransaction(request));
        } catch (TransactionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id The ID of the transaction to delete.
     * @return A success message or an error response.
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> deleteTransaction(@PathVariable String id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.ok("Transaction deleted successfully.");
        } catch (TransactionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}