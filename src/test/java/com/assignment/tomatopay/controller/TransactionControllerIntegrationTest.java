package com.assignment.tomatopay.controller;

import com.assignment.tomatopay.entity.Account;
import com.assignment.tomatopay.entity.Transaction;
import com.assignment.tomatopay.entity.Type;
import com.assignment.tomatopay.repository.TransactionRepository;
import com.assignment.tomatopay.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.TransactionExists;
import exception.TransactionNotFound;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Suleyman Yildirim
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionControllerIntegrationTest {

    @Autowired
    private TransactionController transactionController;

    @MockBean
    private TransactionService transactionService;
//
//    @MockBean
//    private TransactionRepository transactionRepository;


    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void retrieveTransaction() throws Exception {
        Transaction transactionCredit = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        when(transactionService.retrieveTransactions()).thenReturn(List.of(transactionCredit, transactionDebit));
        ResponseEntity<List<Transaction>> transactions = transactionController.retrieveTransactions();

        assertNotNull(transactions);
        assertThat(transactions.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(transactions.getBody().get(0), is(equalTo(transactionCredit)));
        assertThat(transactions.getBody().get(1), is(equalTo(transactionDebit)));
    }

    @Test
    public void should_throw_exception_when_retrieveTransactions() throws TransactionNotFound {
        when(transactionService.retrieveTransactions()).thenReturn(null);
        doThrow(new TransactionNotFound("Transactions not found")).when(spy(transactionController)).retrieveTransactions();
    }

    @Test
    public void updateTransaction() throws Exception {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionUpdated = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(100)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionService.createTransaction(transaction)).thenReturn(CompletableFuture.completedFuture(transaction));
        when(transactionService.updateTransaction(any(Transaction.class))).thenReturn(transactionUpdated);

        ResponseEntity<Transaction> updateTransaction = transactionController.updateTransaction(1L, transactionUpdated);

        assertNotNull(updateTransaction);
        assertThat(updateTransaction.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(updateTransaction.getBody(), is(equalTo(transactionUpdated)));
        assertThat(updateTransaction.getBody().getAmount(), is(equalTo(100.0)));
    }

    @Test
    public void should_throw_exception_when_updateTransaction() throws TransactionNotFound {
        Transaction transactionUpdated = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(100)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionService.updateTransaction(any(Transaction.class))).thenReturn(null);

        doThrow(new TransactionNotFound("Transaction id: " + transactionUpdated.getId() + " not found"))
                .when(spy(transactionController))
                .updateTransaction(1L, transactionUpdated);
    }

    @Test
    public void deleteTransaction() throws Exception {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionService.deleteTransaction(any(Long.class))).thenReturn(transaction);
        ResponseEntity<Transaction> responseEntity = transactionController.deleteTransaction(1L);
        assertNotNull(responseEntity);
        assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat(responseEntity.getBody(), is(equalTo(transaction)));
    }

    @Test
    public void should_throw_exception_when_deleteTransaction() throws TransactionNotFound {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionService.deleteTransaction(any(Long.class))).thenReturn(null);

        doThrow(new TransactionNotFound("Transaction id: " + transaction.getId() + " not found"))
                .when(spy(transactionController)).deleteTransaction(any(Long.class));
    }


    @Test
    public void createTransaction() throws Exception {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(transaction, arg0);
            return CompletableFuture.supplyAsync(() -> transaction);
        }).when(transactionService).createTransaction(transaction);

        ResponseEntity<Transaction> transactionResponseEntity = transactionController.createTransaction(transaction);
        assertNotNull(transactionResponseEntity);
        assertThat(transactionResponseEntity.getStatusCode(), is(equalTo(HttpStatus.CREATED)));
        assertThat(transactionResponseEntity.getBody(), is(equalTo(transaction)));
    }

    @Test
    public void should_throw_exception_when_createTransaction() throws TransactionExists, InterruptedException, ExecutionException {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(transaction, arg0);
            return CompletableFuture.supplyAsync(() -> transaction);
        }).when(transactionService).createTransaction(transaction);

        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(transaction, arg0);
            return CompletableFuture.supplyAsync(() -> transaction);
        }).when(transactionService).createTransaction(transaction);

        ResponseEntity<Transaction> transactionResponseEntity = transactionController.createTransaction(transaction);
        //when(transactionController.createTransaction(transaction)).thenThrow(new TransactionExists(""));
        doThrow(new TransactionExists()).when(spy(transactionController)).createTransaction(transactionResponseEntity.getBody());
    }
}