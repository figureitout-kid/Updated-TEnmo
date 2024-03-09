package com.techelevator.tenmo.model;

public enum TransferStatus {
    PENDING(1),
    APPROVED(2),
    REJECTED(3);

    private final int value;

    TransferStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static TransferStatus fromValue(int value) {
        for (TransferStatus status : TransferStatus.values())
        {
            if (status.getValue() == value) return status;

        }
        throw new IllegalArgumentException("Unknown TransferStatus value: " + value);
    }
}
