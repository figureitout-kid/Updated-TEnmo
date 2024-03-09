package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface AccountDao {
    BigDecimal getBalance(int userId);
    Account getAccountByUserId(int userId);
    int getUserIdByAccountId(int accountId);
    @Transactional
    Account updateBalance(Account account);
    @Transactional
    void addToBalance(int userId, BigDecimal amount);
    @Transactional
    void subtractFromBalance(int userId, BigDecimal amount);

}
