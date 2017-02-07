package com.globalsight.connector.blaise.util;

/**
 * @author David Yan 2017/2/7
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
