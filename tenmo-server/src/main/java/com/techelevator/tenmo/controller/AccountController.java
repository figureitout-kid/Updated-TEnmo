package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{userId}/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable int userId) {
        BigDecimal balance = accountDao.getBalance(userId);
        if (balance != null)
        {
            return new ResponseEntity<>(balance, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //TODO this will need to be tied into creating a transaction once that is up and running? Keeping in mind-- the amount to transfer will be deducted from sender
    //TODO and added to the receiver balances.
//    @PutMapping("/{userId}/update")
//    public ResponseEntity<Account> updateAccountBalance(@PathVariable int userId, @RequestBody BigDecimal amount) {
//        try
//        {
//            Account accountToUpdate = accountDao.getAccountByUserId(userId);
//            if (accountToUpdate != null)
//            {
//                accountToUpdate.setBalance();
//            }
//        }
//    }


    private int findUserIdByUsername(String username) {
        User user = userDao.getUserByUsername(username);
        if (user != null) return user.getId();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + "not found.");
    }
}



