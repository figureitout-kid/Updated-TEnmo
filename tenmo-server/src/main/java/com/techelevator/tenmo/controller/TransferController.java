package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.TransferStatusDao;
import com.techelevator.tenmo.dao.TransferTypeDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferDao transferDao;
    private final TransferStatusDao transferStatusDao;
    private final TransferTypeDao transferTypeDao;

    public TransferController (TransferDao transferDao,TransferStatusDao transferStatusDao, TransferTypeDao transferTypeDao) {
        this.transferDao = transferDao;
        this.transferStatusDao = transferStatusDao;
        this.transferTypeDao = transferTypeDao;
    }

    //TODO need to take care of error handling here--------------------------------

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer) {
        Transfer newTransfer = transferDao.createTransfer(transfer);
        if (newTransfer != null)
        {
            return new ResponseEntity<>(newTransfer, HttpStatus.CREATED);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransferById(@PathVariable int id) {
        Transfer transfer = transferDao.getTransferById(id);
        if (transfer != null)
        {
            return new ResponseEntity<>(transfer, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Transfer>> getAllTransfersForUser(@PathVariable int userId) {
        List<Transfer> transfers = transferDao.getAllTransfersByUserId(userId);
        return new ResponseEntity<>(transfers, HttpStatus.OK);

        //TODO if/else logic
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transfer> updateTransfer(@PathVariable int id, @RequestBody Transfer transfer) {
        if (transfer.getTransferId() != id)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Transfer updatedTransfer = transferDao.updateTransfer(transfer);
        if (updatedTransfer != null)
        {
            return new ResponseEntity<>(updatedTransfer, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}


