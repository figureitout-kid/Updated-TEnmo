package com.techelevator.tenmo.model;

public enum TransferType {
    REQUEST(1, "Request"),
    SEND(2, "SEND");

    private final int value;
    private final String description;

    TransferType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TransferType fromValue(int value) {
        for (TransferType type : values())
        {
            if (type.getValue() == value) return type;

        }
        throw new IllegalArgumentException("Unknown TransferType value: " + value);
    }
}
