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

        Exception exception = assertThrows(TransactionException.class, () -> transactionService.createTransaction(request)
        );

        assertEquals(ErrorCode.TRANSACTION_ALREADY_EXISTS.getMessage(), exception.getMessage());
    }

    @Test
    public void testInvalidTransactionTypeCategory() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setCategory(TransactionCategory.TRANSFER_IN);

        Exception exception = assertThrows(TransactionException.class, () -> transactionService.createTransaction(request));

        assertEquals(INVALID_TRANSACTION_CATEGORY.getMessage(), exception.getMessage());
    }

    @Test
    public void testInvalidTransactionAccount() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setCategory(TransactionCategory.TRANSFER_OUT);

        Exception exception = assertThrows(TransactionException.class, () -> transactionService.createTransaction(request));

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
        Exception exception = assertThrows(TransactionException.class, () -> transactionService.getTransaction(transactionId));
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND.getMessage(), exception.getMessage());
    }


    @Test
    public void testListTransactionsWithPaging() {
        // only for memory storage, no data before
        // Create transactions with random 16-digit IDs based on timestamp
        Set<String> transactionIds = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < 200; i++) {
            long timestamp = System.currentTimeMillis();
            String id = String.format("%016d", (timestamp + random.nextInt(10000)) % 10000000000000000L);

            while (transactionIds.contains(id)) {
                timestamp = System.currentTimeMillis();
                id = String.format("%016d", (timestamp + random.nextInt(10000)) % 10000000000000000L);
            }

            transactionIds.add(id);

            TransactionRequest request = new TransactionRequest();
            request.setId(id);
            request.setType(TransactionType.DEPOSIT);
            request.setCategory(TransactionCategory.CASH);
            request.setStatus(TransactionStatus.PENDING);
            request.setAmount(100.0);
            request.setDescription("Deposit Cash " + i);
            request.setPrimaryAccount("12345");

            transactionService.createTransaction(request);
        }

        // Convert the set of IDs to a sorted list
        List<String> sortedTransactionIds = new ArrayList<>(transactionIds);
        Collections.sort(sortedTransactionIds);

        TransactionListRequest listRequest = new TransactionListRequest();
        int PAGE_SIZE = 10;
        listRequest.setPageSize(PAGE_SIZE);
        listRequest.setPageNo(1);

        // Get the first page of transactions
        List<Transaction> transactionsPage1 = transactionService.listTransactions(listRequest);

        // Ensure the list is sorted by ID in ascending order
        assertEquals(PAGE_SIZE, transactionsPage1.size());
        for (int i = 0; i < PAGE_SIZE; i++) {
            assertEquals(sortedTransactionIds.get(i), transactionsPage1.get(i).getId());
        }

        // Check that the next page returns the next set of transactions and no duplicates
        listRequest.setPageNo(2);
        List<Transaction> transactionsPage2 = transactionService.listTransactions(listRequest);

        assertEquals(PAGE_SIZE, transactionsPage2.size());
        for (int i = 0; i < PAGE_SIZE; i++) {
            assertEquals(sortedTransactionIds.get(i + PAGE_SIZE), transactionsPage2.get(i).getId());
        }

        // Verify no duplicate entries between pages
        for (int i = 0; i < PAGE_SIZE; i++) {
            assertNotEquals(transactionsPage1.get(i).getId(), transactionsPage2.get(i).getId());
        }

        // Continue checking
        for (int page = 3; page <= 20; page++) {
            listRequest.setPageNo(page);
            List<Transaction> transactionsPage = transactionService.listTransactions(listRequest);

            assertEquals(10, transactionsPage.size());
            for (int i = 0; i < 10; i++) {
                assertEquals(sortedTransactionIds.get((page - 1) * 10 + i), transactionsPage.get(i).getId());
            }
        }
    }

}
