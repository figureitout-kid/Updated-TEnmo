package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.TransferStatus;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class JdbcTransferStatusDao implements TransferStatusDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcTransferStatusDao (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TransferStatus getTransferStatusById(int transferStatusId) {
        String sql = "SELECT transfer_status_id, transfer_status_desc, " +
                     "FROM transfer_status" +
                     "WHERE transfer_status_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferStatusId);
        if (results.next())
        {
            int id = results.getInt("transfer_status_id");
            String description = results.getString("transfer_status_desc");

            return TransferStatus.valueOf(description.toUpperCase());
        }
        else
        {
            throw new DaoException("Transfer status not found for id " + transferStatusId);
        }
    }
}
