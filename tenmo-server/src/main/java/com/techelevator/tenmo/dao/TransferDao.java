package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {
    Transfer getTransferById(int transferId);
    List<Transfer> getAllTransfersByUserId(int userId);
    Transfer createTransfer(Transfer transfer);
    Transfer updateTransfer(Transfer transfer);

}
