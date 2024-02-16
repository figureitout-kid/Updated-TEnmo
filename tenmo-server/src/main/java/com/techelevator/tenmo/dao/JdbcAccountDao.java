package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/* Potential issue-- currently using AccountId for checking account balance/updating balance
will need to ensure this is best practice, or change to userId potentially later when sendBucks implemented
*/

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
        if(row.next())
        {
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
        if (result.next())
        {
            account = mapRowToAccount(result);
        }
        return account;

    }

    @Override
    public Account updateBalance(Account account) {
        Account updatedAccount = null;
        String sql = "UPDATE account" +
                     "SET balance = ? " +
                     "WHERE user_id = ?";
        try
        {
            int rowsAffected = jdbcTemplate.update(sql, account.getBalance(), account.getUserId());
            if (rowsAffected == 0)
            {
                throw new DaoException("Zero rows affects, expected at least one");
            }
            updatedAccount = getAccountByUserId(account.getUserId());
        }
        catch (CannotGetJdbcConnectionException e)
        {
            throw new DaoException("Unable to connect to server or database", e);
        }
        catch (DataIntegrityViolationException e)
        {
            throw new DaoException("Data integrity violation, e");
        }
        return updatedAccount;
    }


    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
