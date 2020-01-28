package com.meemaw.model.beacon;

public enum BeaconEventType {

    LOAD(0), RESIZE(1);

    private final int value;

    BeaconEventType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
