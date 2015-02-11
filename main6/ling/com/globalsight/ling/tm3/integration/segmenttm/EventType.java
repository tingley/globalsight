package com.globalsight.ling.tm3.integration.segmenttm;

public enum EventType {
    TM_IMPORT(0),
    LEGACY_MIGRATE(1),
    SEGMENT_SAVE(2),
    TUV_DELETE(3),
    TUV_MODIFY(4);
    EventType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    private int value;
}
