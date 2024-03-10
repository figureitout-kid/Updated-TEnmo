package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable int userId) {
        BigDecimal balance = accountDao.getBalance(userId);
        if (balance != null) {
            return new ResponseEntity<>(balance, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @PutMapping("/{userId}/balance")
    public ResponseEntity<Account> updateAccountBalance(@PathVariable int userId, @RequestBody BigDecimal newBalance) {
        Account account = accountDao.getAccountByUserId(userId);
        if (account != null) {
            account.setBalance(newBalance);
            accountDao.updateBalance(account);
            return new ResponseEntity<>(account, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{accountId}/account-to-userId")
    public ResponseEntity<Integer> getUserIdByAccountId(@PathVariable int accountId) {
        int userId = accountDao.getUserIdByAccountId(accountId);
        if (userId != 0) {
            return new ResponseEntity<>(userId, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/account")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Account> getAccountByUserId(@PathVariable int userId) {
        Account account = accountDao.getAccountByUserId(userId);
        if (account != null) {
            return new ResponseEntity<>(account, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private int findUserIdByUsername(String username) {
        User user = userDao.getUserByUsername(username);
        if (user != null) return user.getId();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + "not found.");
    }
}





