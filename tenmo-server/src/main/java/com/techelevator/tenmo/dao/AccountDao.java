package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {
    BigDecimal getBalance(int userId);
    Account getAccountByUserId(int userId);
    Account updateBalance(Account account);
    void addToBalance(int userId, BigDecimal amount);

}
