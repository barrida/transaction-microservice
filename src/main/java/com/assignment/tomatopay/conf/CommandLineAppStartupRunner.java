//package com.assignment.tomatopay.conf;
//
//import com.assignment.tomatopay.entity.Account;
//import com.assignment.tomatopay.entity.Transaction;
//import com.assignment.tomatopay.entity.Type;
//import com.assignment.tomatopay.repository.AccountRepository;
//import com.assignment.tomatopay.repository.TransactionRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @author Suleyman Yildirim
// */
//@Component
//public class CommandLineAppStartupRunner implements CommandLineRunner {
//
//    @Autowired
//    TransactionRepository transactionRepository;
//
//    @Autowired
//    AccountRepository accountRepository;
//
//    private static final Logger LOG =
//            LoggerFactory.getLogger(CommandLineAppStartupRunner.class);
//
//    public static int counter;
//
//    final Transaction transactionCredit = Transaction.builder()
//            .id(1l)
//            .accountId(1l)
//            .currency("GBP")
//            .amount(250)
//            .description("Tesco Holborn Station")
//            .type(Type.CREDIT)
//            .build();
//
//    final Transaction transactionDebit = Transaction.builder()
//            .id(2l)
//            .accountId(1l)
//            .currency("GBP")
//            .amount(250)
//            .description(" asdf  gfhj")
//            .type(Type.DEBIT)
//            .build();
//
//    final Account account = Account.builder()
//            .id(1l)
//            .balance(transactionDebit.getAmount() + transactionCredit.getAmount())
//            .transactions(List.of(transactionCredit, transactionDebit))
//            .build();
//
//    @Override
//    public void run(String...args) throws Exception {
//        LOG.info("Initializing reposiitories");
//        transactionRepository.saveAll(List.of(transactionCredit,transactionDebit));
//        accountRepository.save(account);
//    }
//}
