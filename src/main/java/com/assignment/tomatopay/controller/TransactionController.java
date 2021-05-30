package com.assignment.tomatopay.controller;

import com.assignment.tomatopay.entity.Transaction;
import com.assignment.tomatopay.service.TransactionService;
import exception.TransactionExists;
import exception.TransactionNotFound;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Suleyman Yildirim
 */
@RestController
@AllArgsConstructor
@RequestMapping(value = "/")
public class TransactionController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TransactionService transactionService;

    /**
     * /transactions (GET/POST)
     *
     * @return
     */
    @GetMapping(value = "transactions")
    public ResponseEntity<List<Transaction>> retrieveTransactions() throws TransactionNotFound{
        try {
            List<Transaction> transactionList = transactionService.retrieveTransactions();
            return ResponseEntity.ok(transactionList);
        } catch (TransactionNotFound e){
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) throws TransactionExists,InterruptedException,ExecutionException  {
        try {
            CompletableFuture<Transaction> completableFuture = transactionService.createTransaction(transaction);
            return new ResponseEntity<>( completableFuture.get(), HttpStatus.CREATED);
        } catch (TransactionExists e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * /transactions/{transactionId} (GET/PUT/DELETE)
     */

    @GetMapping(value = "transactions/{transactionId}")
    public ResponseEntity<Transaction> retrieveTransaction(@PathVariable Long transactionId) throws TransactionNotFound {
        try {
            Optional<Transaction> optionalTransaction = transactionService.retrieveTransaction(transactionId);
            return new ResponseEntity<>(optionalTransaction.get(), HttpStatus.OK);
        } catch (TransactionNotFound e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "transactions/{transactionId}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long transactionId, @RequestBody Transaction transaction) throws TransactionNotFound{
        try {
            transaction.setId(transactionId);
            Transaction optionalTransaction = transactionService.updateTransaction(transaction);
            return new ResponseEntity<>(optionalTransaction, HttpStatus.OK);
        } catch (TransactionNotFound e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "transactions/{transactionId}")
    public ResponseEntity<Transaction> deleteTransaction(@PathVariable Long transactionId) throws TransactionNotFound {
        try {
            Transaction transaction = transactionService.deleteTransaction(transactionId);
            return new ResponseEntity<>(transaction, HttpStatus.NO_CONTENT);
        } catch (TransactionNotFound e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
