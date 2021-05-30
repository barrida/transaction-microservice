package com.assignment.tomatopay.service;

import com.assignment.tomatopay.entity.Account;
import com.assignment.tomatopay.entity.Transaction;
import com.assignment.tomatopay.repository.AccountRepository;
import com.assignment.tomatopay.repository.TransactionRepository;
import exception.TransactionExists;
import exception.TransactionNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Suleyman Yildirim
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<Transaction> retrieveTransactions() throws TransactionNotFound {
        List<Transaction> transactionList = transactionRepository.findAll();
        if (transactionList != null) {
            return transactionList;
        } else {
            throw new TransactionNotFound("Transactions not found");
        }
    }

    /**
     * Once the transaction is created, we require the endpoint to start a new thread that asynchronously does the following:
     * <p>
     * <p>
     * •	Add or subtract the amount (depending on the type) for the transaction, from the total running balance, the initial balance being 0.
     *
     * @param transaction
     * @return
     */
    @Async
    public CompletableFuture<Transaction> createTransaction(Transaction transaction) throws InterruptedException, TransactionExists {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transaction.getId());
        if (optionalTransaction.isEmpty()) {
            transactionRepository.save(transaction);
            CompletableFuture.runAsync(() -> processTransactionAsynchronously(transaction));
            return CompletableFuture.completedFuture(transaction);
        } else {
            throw new TransactionExists("Transaction exists");
        }
    }

    public synchronized Account processTransactionAsynchronously(Transaction transaction) {
        Account account = null;
        final Optional<Account> accountOptional = accountRepository.findById(transaction.getAccountId());
        if (accountOptional.filter(account1 -> account1.getBalance() >= 0).isPresent()) {
            account = accountOptional.get();
            double balance = account.getBalance();
            String transactionType = transaction.getType().toString();
            if ("CREDIT".equalsIgnoreCase(transactionType)) {
                balance += transaction.getAmount();
            } else if ("DEBIT".equalsIgnoreCase(transactionType)) {
                balance -= transaction.getAmount();
            }
            account.setBalance(balance);
            accountRepository.save(account);
        }
        return account;
    }

    public Optional<Transaction> retrieveTransaction(Long transactionId) throws TransactionNotFound {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isPresent()) {
            return optionalTransaction;
        }
        throw new TransactionNotFound("Transaction id: " + transactionId + " not found");
    }


    public Transaction deleteTransaction(Long transactionId) throws TransactionNotFound {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isPresent()) {
            transactionRepository.deleteById(transactionId);
            return optionalTransaction.get();
        } else {
            throw new TransactionNotFound("Transaction id: " + transactionId + " not found");
        }
    }

    public Transaction updateTransaction(Transaction transaction) throws TransactionNotFound {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transaction.getId());
        if (optionalTransaction.isPresent()) {
            return transactionRepository.save(transaction);
        } else {
            throw new TransactionNotFound("Transaction id: " + transaction.getId() + " not found");
        }
    }

}
