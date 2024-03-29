package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class JdbcAccountDao implements AccountDao {

    private final JdbcTemplate jdbcTemplate;
    public JdbcAccountDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }


    @Override
    public BigDecimal getBalance (int userId) {
        String sql = "SELECT balance " +
                     "FROM account " +
                     "WHERE user_id = ?;";

        SqlRowSet row = jdbcTemplate.queryForRowSet(sql, userId);

        BigDecimal balance = null;
        if(row.next()) {
            balance = row.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public Account getAccountByUserId(int userId) {
        String sql = "SELECT account_id, user_id, balance " +
                     "FROM account WHERE user_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
        Account account = null;
        if (result.next()) {
            account = mapRowToAccount(result);
        }
        return account;
    }

    @Override
    public int getUserIdByAccountId(int accountId) {
        String sql = "SELECT user_id " +
                     "FROM account " +
                     "WHERE account_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, accountId);

        if (result.next()) {
            return result.getInt("user_id");
        }
        else {
            throw new RuntimeException("Account not found for accountId: " + accountId);
        }
    }

    @Override
    public Account updateBalance(Account account) {
        Account updatedAccount = null;
        String sql = "UPDATE account " +
                     "SET balance = ? " +
                     "WHERE user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, account.getBalance(), account.getUserId());
            if (rowsAffected == 0) {
                throw new DaoException("Zero rows affects, expected at least one");
            }
            updatedAccount = getAccountByUserId(account.getUserId());
        }
        catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation, e");
        }
        return updatedAccount;
    }

    @Override
    public void addToBalance(int userId, BigDecimal amount) {
        //get current account
        Account account = getAccountByUserId(userId);
        if (account != null) {
            BigDecimal newBalance = account.getBalance().add(amount);
            //use existing updateBalance method to persis the new balance
            account.setBalance(newBalance);
            updateBalance(account);
        }
        else {
            throw new DaoException("Account not found for userId: " + userId);
        }
    }

    @Override
    public void subtractFromBalance(int userId, BigDecimal amount) {
        //get current account
        Account account = getAccountByUserId(userId);
        if (account != null) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            //check for overdraft-- may want to implement this elsewhere, covering it here for now
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new DaoException("Insufficient funds for userId: " + userId);
            }

            account.setBalance(newBalance);
            updateBalance(account);
        }
        else {
            throw new DaoException("Account not found for userId: " + userId);
        }
    }


    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
