package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferStatus;
import com.techelevator.tenmo.model.TransferType;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;
    public JdbcTransferDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }


    @Override
    public Transfer getTransferById(int transferId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                     "FROM transfer " +
                     "WHERE transfer_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
        Transfer transfer = null;
        if (results.next())
        {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }


    @Override
    public List<Transfer> getAllTransfersByUserId(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                     "FROM transfer t " +
                     "JOIN account a ON a.account_id = t.account_from OR a.account_id = t.account_to " +
                     "WHERE a.user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        while (results.next())
        {
            transfers.add(mapRowToTransfer(results));
        }
        return transfers;
    }

    @Override
    public Transfer createTransfer(Transfer transfer) {

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";

        Integer newId = jdbcTemplate.queryForObject(sql, new Object[]{
                transfer.getTransferType().getValue(),
                transfer.getTransferStatus().getValue(),
                transfer.getAccountFrom(),
                transfer.getAccountTo(),
                transfer.getAmount(),
        }, Integer.class);

        if (newId != null)
        {
            return getTransferById(newId);
        }
        else
        {
            throw new DaoException("Create Transfer failed: No ID returned.");
        }
    }

    @Override
    public Transfer updateTransfer(Transfer transfer) {
        String sql = "UPDATE transfer " +
                     "SET transfer_type_id = ?, transfer_status_id = ?, account_from = ?, account_to = ?, amount = ? " +
                     "WHERE transfer_id = ?;";

        int rowsAffected = jdbcTemplate.update(sql,
                transfer.getTransferType().getValue(),
                transfer.getTransferStatus().getValue(),
                transfer.getAccountFrom(),
                transfer.getAccountTo(),
                transfer.getAmount(),
                transfer.getTransferId());
        if (rowsAffected == 1)
        {
            return getTransferById(transfer.getTransferId());
        }
        else
        {
            throw new DaoException("Update Transfer failed: No rows affected.");
        }
    }

    @Override
    public Transfer updateTransferStatus(int transferId, TransferStatus newStatus) {
        String sql = "UPDATE transfer " +
                     "SET transfer_status_id = ? " +
                     "WHERE transfer_id = ?;";

        int rowsAffected = jdbcTemplate.update(sql, newStatus.getValue(), transferId);

        if (rowsAffected == 1)
        {
            return getTransferById(transferId);
        }
        else
        {
            throw new DaoException("Update Transfer failed: No rows affected.");
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferType(TransferType.fromValue(rs.getInt("transfer_type_id")));
        transfer.setTransferStatus(TransferStatus.fromValue(rs.getInt("transfer_status_id")));
        transfer.setAccountFrom(rs.getInt("account_from"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        return transfer;
    }

}
