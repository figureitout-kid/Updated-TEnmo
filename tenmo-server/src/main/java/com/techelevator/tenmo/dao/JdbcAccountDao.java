package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcAccountDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override
    public BigDecimal getBalance (int accountId) {
        String sql = "SELECT balance " +
                "FROM account " +
                "WHERE account_id = ?;";

        SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountId);

        BigDecimal balance = null;
        if(row.next())
        {
            balance = row.getBigDecimal("balance");
        }
        return balance;
    }
 //TODO
    @Override
    public Account getAccountByUserId(int userId) {
        return null;
    }

    @Override
    public Account updateBalance(Account account) {
        return null;
    }


}
