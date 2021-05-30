package com.assignment.tomatopay.service;

import com.assignment.tomatopay.entity.Account;
import com.assignment.tomatopay.entity.Transaction;
import com.assignment.tomatopay.entity.Type;
import com.assignment.tomatopay.repository.AccountRepository;
import com.assignment.tomatopay.repository.TransactionRepository;
import exception.TransactionExists;
import exception.TransactionNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Suleyman Yildirim
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @AfterEach
    public void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }
    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void whenTypeIsCreditThenProcessTransactionAsynchronously() throws InterruptedException {
        Transaction transactionCredit = Transaction.builder()
                .id(Long.parseLong("1"))
                .accountId(Long.parseLong("1"))
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        Account account = Account.builder()
                .id(Long.parseLong("1"))
                .balance(300)
                .transactions(List.of(transactionCredit, transactionDebit))
                .build();

        when(accountRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(account));

        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(() -> transactionService.processTransactionAsynchronously(transactionCredit));
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertEquals(550, accountRepository.findById(1L).get().getBalance());
    }

    @Test
    public void whenTypeIsDebitThenProcessTransactionAsynchronously() throws InterruptedException {
        Transaction transactionCredit = Transaction.builder()
                .id(Long.parseLong("1"))
                .accountId(Long.parseLong("1"))
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        Account account = Account.builder()
                .id(Long.parseLong("1"))
                .balance(300)
                .transactions(List.of(transactionCredit, transactionDebit))
                .build();

        when(accountRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(account));

        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(() -> transactionService.processTransactionAsynchronously(transactionDebit));
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertEquals(50, accountRepository.findById(2L).get().getBalance());
    }

    @Test
    public void retrieveTransaction() throws Exception {
        Transaction transactionCredit = Transaction.builder()
                .id(Long.parseLong("1"))
                .accountId(Long.parseLong("1"))
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(transactionCredit));
        Transaction transaction1 = transactionService.retrieveTransaction(1L).get();
        assertEquals(transaction1, transactionCredit);

        when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(transactionDebit));
        Transaction transaction2 = transactionService.retrieveTransaction(2L).get();
        assertEquals(transaction2, transactionDebit);
    }

    @Test(expected = TransactionNotFound.class)
    public void should_throw_exception_when_retrieveTransaction() throws Exception {
        Transaction transactionCredit = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        transactionService.retrieveTransaction(1L);
    }

    @Test
    public void retrieveTransactions() throws TransactionNotFound {
        Transaction transactionCredit = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        when(transactionRepository.findAll()).thenReturn(List.of(transactionCredit, transactionDebit));
        List<Transaction> transactionList = transactionService.retrieveTransactions();
        assertNotNull(transactionList);
        assertEquals(transactionCredit, transactionList.get(0));
        assertEquals(transactionDebit, transactionList.get(1));
    }

    @Test(expected = TransactionNotFound.class)
    public void should_throw_exception_when_retrieveTransactions() throws TransactionNotFound {
        when(transactionRepository.findAll()).thenReturn(null);
        transactionService.retrieveTransactions();
    }

    @Test
    public void createTransaction() throws InterruptedException {
        Transaction transactionCredit = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        Account account = Account.builder()
                .id(1L)
                .balance(300)
                .transactions(List.of(transactionCredit))
                .build();
        when(accountRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(account));
        //when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(transactionDebit));

        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(() -> {
            try {
                transactionService.createTransaction(transactionDebit);
            } catch (InterruptedException | TransactionExists e) {
                e.printStackTrace();
            }
        });
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        assertEquals(50, accountRepository.findById(any(Long.class)).get().getBalance());
    }

    @Test(expected = TransactionExists.class)
    public void  should_throw_exception_when_transaction_exist() throws TransactionExists, InterruptedException {
        Transaction transactionDebit = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.DEBIT)
                .build();

        when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(transactionDebit));
        transactionService.createTransaction(transactionDebit);
    }

    @Test
    public void deleteTransaction() throws TransactionNotFound {
        Transaction transactionCredit = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();
        when(transactionRepository.findById(transactionCredit.getId())).thenReturn(Optional.of(transactionCredit));
        transactionService.deleteTransaction(transactionCredit.getId());
        verify(transactionRepository).deleteById(transactionCredit.getId());
    }

    @Test(expected = TransactionNotFound.class)
    public void should_throw_exception_when_deleteTransaction() throws TransactionNotFound {
        Transaction transactionCredit = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();
        when(transactionRepository.findById(transactionCredit.getId())).thenReturn(Optional.empty());
        transactionService.deleteTransaction(transactionCredit.getId());
    }

    @Test
    public void updateTransaction() throws TransactionExists, InterruptedException, ExecutionException, TransactionNotFound {
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

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transactionUpdated)).thenReturn(transactionUpdated);
        Transaction optionalTransaction = transactionService.updateTransaction(transactionUpdated);
        assertEquals(100.0, optionalTransaction.getAmount());
    }

    @Test(expected = TransactionNotFound.class)
    public void should_throw_exception_when_updateTransaction() throws TransactionExists, InterruptedException, ExecutionException, TransactionNotFound {
        Transaction transaction = Transaction.builder()
                .id(1l)
                .accountId(1l)
                .currency("GBP")
                .amount(250)
                .description("Tesco Holborn Station")
                .type(Type.CREDIT)
                .build();

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        transactionService.updateTransaction(transaction);
    }

}