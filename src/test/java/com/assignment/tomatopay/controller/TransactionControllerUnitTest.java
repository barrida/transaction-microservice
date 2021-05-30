package com.assignment.tomatopay.controller;

import com.assignment.tomatopay.entity.Account;
import com.assignment.tomatopay.entity.Transaction;
import com.assignment.tomatopay.entity.Type;
import com.assignment.tomatopay.repository.AccountRepository;
import com.assignment.tomatopay.repository.TransactionRepository;
import com.assignment.tomatopay.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exception.TransactionNotFound;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.RequestResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Suleyman Yildirim
 */
@RunWith(SpringRunner.class)
@WebMvcTest(value = TransactionController.class)
public class TransactionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionRepository transactionRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transactionRepository.deleteAll();
    }

    final Transaction transactionCredit = Transaction.builder()
            .id(1l)
            .accountId(1l)
            .currency("GBP")
            .amount(250)
            .description("Tesco Holborn Station")
            .type(Type.CREDIT)
            .build();

    final Transaction transactionDebit = Transaction.builder()
            .id(2l)
            .accountId(1l)
            .currency("GBP")
            .amount(250)
            .description("Tesco Holborn Station")
            .type(Type.DEBIT)
            .build();

    final Account account = Account.builder()
            .id(1l)
            .balance(300)
            .transactions(List.of(transactionCredit, transactionDebit))
            .build();

    @Test
    public void retrieveTransactions() throws Exception {
        when(transactionService.retrieveTransactions())
                .thenReturn(List.of(transactionCredit, transactionDebit));

        mockMvc.perform(get("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].type").value(Type.CREDIT.toString()))
                .andExpect(jsonPath("$[1].type").value(Type.DEBIT.toString()))
                .andReturn();
    }

    @Test
    public void should_throw_exception_when_retrieveTransactions() throws Exception {
        when(transactionService.retrieveTransactions())
                .thenThrow(new TransactionNotFound("Transactions not found"));

        mockMvc.perform(get("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }


    @Test
    public void retrieveTransaction() throws Exception {
        when(transactionService.retrieveTransaction(2L))
                .thenReturn(java.util.Optional.ofNullable(transactionDebit));

        mockMvc.perform(get("/transactions/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transactionDebit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void should_throw_exception_when_retrieveTransaction() throws Exception {
        when(transactionService.retrieveTransaction(any(Long.class)))
                .thenThrow(new TransactionNotFound("Transactions not found"));

        mockMvc.perform(get("/transactions/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transactionDebit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
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

        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            assertEquals(transaction, arg0);
            return CompletableFuture.supplyAsync(() -> transaction);
        }).when(transactionService).createTransaction(transaction);

        CompletableFuture<Transaction> completableFuture = transactionService.createTransaction(transaction);
        assertEquals(transaction,completableFuture.get());
        when(transactionService.updateTransaction(any(Transaction.class))).thenReturn(transactionUpdated);

        mockMvc.perform(put("/transactions/{id}",1)
                .content(asJsonString(transactionUpdated))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountId").value(1L))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.amount").value((100)))
                .andReturn();
    }

    @Test
    public void should_throw_exception_when_updateTransaction() throws Exception {
        Transaction transactionUpdated = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(100)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionService.updateTransaction(any(Transaction.class)))
                .thenThrow(new TransactionNotFound("Transaction id: " + transactionUpdated.getId() + " not found"));

        mockMvc.perform(put("/transactions/{id}",1)
                .content(asJsonString(transactionUpdated))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void deleteTransaction() throws Exception {
        Transaction transactionDebit = Transaction.builder()
                .id(2l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();
        when(transactionService.deleteTransaction(2L)).thenReturn(transactionDebit);
        mockMvc.perform(delete("/transactions/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transactionDebit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void should_throw_exception_when_deleteTransaction() throws Exception {
        Transaction transactionDebit = Transaction.builder()
                .id(2l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        when(transactionService.deleteTransaction(any(Long.class)))
                .thenThrow(new TransactionNotFound("Transaction id: " + transactionDebit.getId() + " not found"));

        mockMvc.perform(delete("/transactions/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transactionDebit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @Ignore
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

        CompletableFuture<Transaction> completableFuture = transactionService.createTransaction(transaction);
        assertEquals(transaction,completableFuture.get());

        MvcResult mvcResult = mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transactionCredit))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andDo(MockMvcResultHandlers.log())
                .andReturn();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());

    }


    /**
     * Serializing LocalDate to JSON
     *
     * @param obj
     * @return
     */
    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}