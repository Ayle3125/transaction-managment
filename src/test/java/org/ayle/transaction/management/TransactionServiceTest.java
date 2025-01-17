package org.ayle.transaction.management;

import jakarta.annotation.Resource;
import org.ayle.transaction.management.Exception.ErrorCode;
import org.ayle.transaction.management.Exception.TransactionException;
import org.ayle.transaction.management.entity.Transaction;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;
import org.ayle.transaction.management.model.TransactionListRequest;
import org.ayle.transaction.management.model.TransactionRequest;
import org.ayle.transaction.management.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.ayle.transaction.management.Exception.ErrorCode.COUNTERPARTY_ACCOUNT_REQUIRED;
import static org.ayle.transaction.management.Exception.ErrorCode.INVALID_TRANSACTION_CATEGORY;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    @Resource
    private TransactionService transactionService;

    @Test
    public void testCreateTransactionWithValidData() {
        TransactionRequest request = new TransactionRequest();
        request.setId(null);
        request.setType(TransactionType.DEPOSIT);
        request.setCategory(TransactionCategory.CASH);
        request.setStatus(TransactionStatus.PENDING);
        request.setAmount(100.0);
        request.setDescription("Deposit Cash");
        request.setPrimaryAccount("12345");

        String transactionId = transactionService.createTransaction(request);

        assertNotNull(transactionId);

        Transaction transaction = transactionService.getTransaction(transactionId);
        assertNotNull(transaction);
    }

    @Test
    public void testCreateTransactionWithDuplicateId() {
        // create a transaction
        TransactionRequest requestPre = new TransactionRequest();
        requestPre.setType(TransactionType.DEPOSIT);
        requestPre.setCategory(TransactionCategory.CASH);
        requestPre.setStatus(TransactionStatus.PENDING);
        requestPre.setAmount(100.0);
        requestPre.setDescription("Test deposit");
        requestPre.setPrimaryAccount("12345");

        String existingId = transactionService.createTransaction(requestPre);

        TransactionRequest request = new TransactionRequest();
        request.setId(existingId);
        request.setType(TransactionType.DEPOSIT);
        request.setCategory(TransactionCategory.CASH);
        request.setStatus(TransactionStatus.PENDING);
        request.setAmount(50.0);
        request.setDescription("Another deposit");
        request.setPrimaryAccount("67890");

        Exception exception = assertThrows(TransactionException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals(ErrorCode.TRANSACTION_ALREADY_EXISTS.getMessage(), exception.getMessage());
    }

    @Test
    public void testInvalidTransactionTypeCategory() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setCategory(TransactionCategory.TRANSFER_IN);

        Exception exception = assertThrows(TransactionException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals(INVALID_TRANSACTION_CATEGORY.getMessage(), exception.getMessage());
    }

    @Test
    public void testInvalidTransactionAccount() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setCategory(TransactionCategory.TRANSFER_OUT);

        Exception exception = assertThrows(TransactionException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals(COUNTERPARTY_ACCOUNT_REQUIRED.getMessage(), exception.getMessage());
    }

    @Test
    public void testDeleteTransaction() {
        // create a transaction
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DEPOSIT);
        request.setCategory(TransactionCategory.CASH);
        request.setStatus(TransactionStatus.PENDING);
        request.setAmount(100.0);
        request.setDescription("Test deposit");
        request.setPrimaryAccount("12345");

        String transactionId = transactionService.createTransaction(request);

        // delete the transaction
        transactionService.deleteTransaction(transactionId);

        // query the transaction
        Exception exception = assertThrows(TransactionException.class, () -> {
            transactionService.getTransaction(transactionId);
        });
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    public void testListTransactionsWithPaging() {
        // Create transactions with specific IDs in random order
        TransactionRequest request1 = new TransactionRequest();
        request1.setId("3");
        request1.setType(TransactionType.DEPOSIT);
        request1.setCategory(TransactionCategory.CASH);
        request1.setStatus(TransactionStatus.PENDING);
        request1.setAmount(100.0);
        request1.setDescription("Deposit Cash");
        request1.setPrimaryAccount("12345");

        TransactionRequest request2 = new TransactionRequest();
        request2.setId("1");
        request2.setType(TransactionType.WITHDRAWAL);
        request2.setCategory(TransactionCategory.PAYMENT);
        request2.setStatus(TransactionStatus.PENDING);
        request2.setAmount(50.0);
        request2.setDescription("Payment");
        request2.setPrimaryAccount("67890");

        TransactionRequest request3 = new TransactionRequest();
        request3.setId("2");
        request3.setType(TransactionType.DEPOSIT);
        request3.setCategory(TransactionCategory.CASH);
        request3.setStatus(TransactionStatus.PENDING);
        request3.setAmount(200.0);
        request3.setDescription("Deposit Cash");
        request3.setPrimaryAccount("54321");

        transactionService.createTransaction(request1);
        transactionService.createTransaction(request2);
        transactionService.createTransaction(request3);

        TransactionListRequest listRequest = new TransactionListRequest();
        listRequest.setSize(2);

        // Get the first page of transactions
        List<Transaction> transactionsPage1 = transactionService.listTransactions(listRequest);

        // Ensure the list is sorted by ID in ascending order
        assertEquals(2, transactionsPage1.size());
        assertEquals("1", transactionsPage1.get(0).getId());
        assertEquals("2", transactionsPage1.get(1).getId());

        // Check that the next page returns the remaining transactions and no duplicates
        listRequest.setLastId(transactionsPage1.get(transactionsPage1.size() - 1).getId());  // Set the lastId to the last transaction ID from page 1
        List<Transaction> transactionsPage2 = transactionService.listTransactions(listRequest);

        assertEquals(1, transactionsPage2.size());
        assertEquals("3", transactionsPage2.get(0).getId());

        // Verify no duplicate entries between pages
        assertNotEquals(transactionsPage1.get(1).getId(), transactionsPage2.get(0).getId());
    }

}
