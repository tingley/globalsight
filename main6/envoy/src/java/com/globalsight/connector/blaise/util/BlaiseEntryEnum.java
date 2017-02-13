package com.globalsight.connector.blaise.util;

/**
 * Enumeration for Blaise entry
 */
public enum BlaiseEntryEnum
{

    HDU_WORKBOOK("HD_HDU_MANUAL"),
    INSHEET("HD_ISHEETS"),
    OTHERS("Others");

    private String value;

    BlaiseEntryEnum(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
