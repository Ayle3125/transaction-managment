package org.ayle.transaction.management;

import org.ayle.transaction.management.Exception.ErrorCode;
import org.ayle.transaction.management.Exception.TransactionException;
import org.ayle.transaction.management.controller.TransactionController;
import org.ayle.transaction.management.entity.Transaction;
import org.ayle.transaction.management.enums.TransactionCategory;
import org.ayle.transaction.management.enums.TransactionStatus;
import org.ayle.transaction.management.enums.TransactionType;
import org.ayle.transaction.management.model.TransactionListRequest;
import org.ayle.transaction.management.model.TransactionRequest;
import org.ayle.transaction.management.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;



import java.util.List;

import static org.ayle.transaction.management.Exception.ErrorCode.INVALID_TRANSACTION_CATEGORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    @Autowired
    private TransactionController transactionController;

    @Test
    public void testCreateTransactionSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DEPOSIT);
        request.setCategory(TransactionCategory.CASH);
        request.setStatus(TransactionStatus.PENDING);
        request.setAmount(100.0);
        request.setDescription("Deposit Cash");
        request.setPrimaryAccount("12345");

        Transaction transaction = new Transaction();
        transaction.setId("1");
        Mockito.when(transactionService.createTransaction(request)).thenReturn("1");

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testCreateTransactionFailure() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DEPOSIT);
        request.setCategory(TransactionCategory.TRANSFER_OUT);
        request.setStatus(TransactionStatus.PENDING);
        request.setAmount(100.0);
        request.setDescription("Deposit Cash");
        request.setPrimaryAccount("12345");

        Mockito.when(transactionService.createTransaction(request)).thenThrow(new TransactionException(INVALID_TRANSACTION_CATEGORY));

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(INVALID_TRANSACTION_CATEGORY.getMessage(), response.getBody());
    }

    @Test
    public void testCreateTransactionWithInvalidAmount() throws Exception {
        String jsonRequest = "{\"type\":\"DEPOSIT\"," +
                "\"category\":\"CASH\"," +
                "\"status\":\"PENDING\"," +
                "\"amount\":-100.0," +
                "\"description\":\"Deposit Cash\"," +
                "\"primaryAccount\":\"12345\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("Invalid request content.", result.getResponse().getErrorMessage());

    }

    @Test
    public void testListTransactionsSuccess() {
        TransactionListRequest request = new TransactionListRequest();

        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        Mockito.when(transactionService.listTransactions(request)).thenReturn(List.of(transaction1, transaction2));

        ResponseEntity<List<Transaction>> response = transactionController.listTransactions(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testListTransactionsEmpty() {
        TransactionListRequest request = new TransactionListRequest();

        Mockito.when(transactionService.listTransactions(request)).thenReturn(List.of());

        ResponseEntity<List<Transaction>> response = transactionController.listTransactions(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void testListTransactionsWithInvalidPageNo() throws Exception {
        String jsonRequest = "{\"type\":\"DEPOSIT\"," +
                "\"category\":\"CASH\"," +
                "\"status\":\"PENDING\"," +
                "\"pageSize\":10," +
                "\"pageNo\":-1}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("Invalid request content.", result.getResponse().getErrorMessage());
    }

    @Test
    public void testUpdateTransactionSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setId("1");
        request.setType(TransactionType.WITHDRAWAL);
        request.setCategory(TransactionCategory.CASH);
        request.setStatus(TransactionStatus.COMPLETED);
        request.setAmount(50.0);
        request.setDescription("Withdrawal Card");
        request.setPrimaryAccount("67890");

        Mockito.when(transactionService.updateTransaction(request)).thenReturn("1");

        ResponseEntity<String> response = transactionController.updateTransaction(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody());
    }

    @Test
    public void testUpdateTransactionFailure() {
        TransactionRequest request = new TransactionRequest();

        Mockito.when(transactionService.updateTransaction(request)).thenThrow(new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND));

        ResponseEntity<String> response = transactionController.updateTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND.getMessage(), response.getBody());
    }

    @Test
    public void testDeleteTransactionSuccess() {
        String transactionId = "1";

        Mockito.doNothing().when(transactionService).deleteTransaction(transactionId);

        ResponseEntity<String> response = transactionController.deleteTransaction(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction deleted successfully.", response.getBody());
    }

    @Test
    public void testDeleteTransactionFailure() {
        String transactionId = "1";

        Mockito.doThrow(new TransactionException(ErrorCode.TRANSACTION_NOT_FOUND)).when(transactionService).deleteTransaction(transactionId);

        ResponseEntity<String> response = transactionController.deleteTransaction(transactionId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND.getMessage(), response.getBody());
    }


}