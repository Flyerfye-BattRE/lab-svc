package com.battre.labsvc.enums;

public enum RefurbStationClass {
    // Corresponds to values defined in the sql data init file under resources/initdb
    NO_REFURB(0),
    RESOLDER(1),
    REPACK(2),
    PROCESSOR_SWAP(3),
    CAPACITOR_SWAP(4);

    private final int statusCode;

    RefurbStationClass(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
