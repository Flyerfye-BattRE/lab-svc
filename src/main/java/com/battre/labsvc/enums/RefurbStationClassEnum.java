package com.battre.labsvc.enums;

import com.battre.stubs.services.RefurbStationClass;

public enum RefurbStationClassEnum {
    UNKNOWN_REFURB(0, "UNKNOWNREFURB", RefurbStationClass.UNKNOWN_REFURB),
    RESOLDER(1, "RESOLDER", RefurbStationClass.RESOLDER),
    REPACK(2, "REPACK", RefurbStationClass.REPACK),
    PROCESSOR_SWAP(3, "PROCESSOR SWAP", RefurbStationClass.PROCESSOR_SWAP),
    CAPACITOR_SWAP(4, "CAPACITOR SWAP", RefurbStationClass.CAPACITOR_SWAP);

    private final int classCode;
    private final String classDescription;
    private final RefurbStationClass grpcClass;

    RefurbStationClassEnum(int classCode, String classDescription, RefurbStationClass grpcClass) {
        this.classCode = classCode;
        this.classDescription = classDescription;
        this.grpcClass = grpcClass;
    }

    public static RefurbStationClassEnum fromClassCode(int classCode) {
        for (RefurbStationClassEnum classValue : values()) {
            if (classValue.classCode == classCode) {
                return classValue;
            }
        }
        return UNKNOWN_REFURB;
    }

    public static RefurbStationClassEnum fromClassDescription(String classDescription) {
        for (RefurbStationClassEnum classValue : values()) {
            if (classValue.classDescription.equals(classDescription)) {
                return classValue;
            }
        }
        return UNKNOWN_REFURB;
    }

    public static RefurbStationClassEnum fromGrpcClass(RefurbStationClass grpcClass) {
        for (RefurbStationClassEnum classValue : values()) {
            if (classValue.grpcClass.equals(grpcClass)) {
                return classValue;
            }
        }
        return UNKNOWN_REFURB;
    }

    public int getClassCode() {
        return this.classCode;
    }

    public String getClassDescription() {
        return this.classDescription;
    }

    public RefurbStationClass getGrpcClass() {
        return this.grpcClass;
    }
}
