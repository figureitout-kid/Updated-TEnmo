package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.TransferStatus;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTransferStatusDao implements TransferStatusDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcTransferStatusDao (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //todo created this early, but didn't use it, can be used potentially in printing pending transfers?
    @Override
    public TransferStatus getTransferStatusById(int transferStatusId) {
        String sql = "SELECT transfer_status_id, transfer_status_desc, " +
                     "FROM transfer_status " +
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

    /* did not utilize a mapper here as we used an enum, but in the future could be added
    for any potential changes to how the transfer status/type are used. */
