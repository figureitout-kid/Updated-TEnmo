package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.TransferType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTransferTypeDao implements TransferTypeDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcTransferTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TransferType getTransferTypeById(int transferTypeId) {
        String sql = "SELECT transfer_type_id, transfer_type_desc " +
                     "FROM transfer_type" +
                     "WHERE transfer_type_id = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferTypeId);
        if (results.next())
        {
            int id = results.getInt("transfer_type_id");
            String description = results.getString("transfer_type_desc");

            return TransferType.valueOf(description.toUpperCase());
        }
        else
        {
            throw new DaoException("Transfer type not found for id " + transferTypeId);
        }
    }

    /* did not utilize a mapper here as we used an enum, but in the future could be added
    for any potential changes to how the transfer status/type are used. */
}
