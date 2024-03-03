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
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable int userId) {
//        String username = principal.getName();
//        User user = userDao.getUserByUsername(username);

        BigDecimal balance = accountDao.getBalance(userId);
        if (balance != null) {
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
/* commenting this out to create the addTo subtractFrom for balance updating?? 02/27: updating the balance in sendBucks saves the receivers new balance as the senders balance,
currently trying to fix this by breaking the transactions down. updateAccountBalance does work on it's own, worth noting.
 */
//    //TODO this will need to be tied into creating a transaction once that is up and running? Keeping in mind-- the amount to transfer will be deducted from sender
//    //TODO and added to the receiver balances.
    @Transactional
    @PutMapping("/{userId}/balance")
    public ResponseEntity<Account> updateAccountBalance(@PathVariable int userId, @RequestBody BigDecimal newBalance) {
        Account account = accountDao.getAccountByUserId(userId);
        if (account != null) {
            account.setBalance(newBalance);
            accountDao.updateBalance(account);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PutMapping("/userId/add")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<Void> addToBalance(@PathVariable int userId, @RequestBody BigDecimal amount) {
//        try
//        {
//            accountDao.addToBalance(userId, amount);
//            return new ResponseEntity<>(HttpStatus.OK);
//        }
//        catch (DaoException e)
//        {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }

//    @PutMapping("/userId/subtract")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<Void> subtractFromBalance(@PathVariable int userId, @RequestBody BigDecimal amount) {
//        try
//        {
//            accountDao.subtractFromBalance(userId, amount);
//            return new ResponseEntity<>(HttpStatus.OK);
//        }
//        catch (DaoException e)
//        {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }

    @GetMapping("/user/{userId}/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Account> getAccountByUserId(@PathVariable int userId) {
        Account account = accountDao.getAccountByUserId(userId);
        if (account != null)
        {
            return new ResponseEntity<>(account, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    private int findUserIdByUsername(String username) {
        User user = userDao.getUserByUsername(username);
        if (user != null) return user.getId();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + "not found.");
    }
}





