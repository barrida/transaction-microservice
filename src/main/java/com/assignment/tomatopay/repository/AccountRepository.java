package com.assignment.tomatopay.repository;

import com.assignment.tomatopay.entity.Account;
import com.assignment.tomatopay.entity.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Suleyman Yildirim
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
}
