package com.globalsight.everest.integration.ling.tm2;

import org.junit.*;
import static org.junit.Assert.*;

import com.globalsight.util.GlobalSightLocale;

public class LeverageMatchTest
{

    @Test
    public void testHashCodeWithLargeTuvId()
    {
        LeverageMatch lm = new LeverageMatch();
        lm.setOriginalSourceTuv(10000000);
        lm.setSubId("1");
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(41);
        lm.setTargetLocale(locale);
        lm.setOrderNum((short)1);
        lm.hashCode();
    }

    @Test
    public void testHashCodeWithNullSubId()
    {
        LeverageMatch lm = new LeverageMatch();
        lm.setOriginalSourceTuv(1000);
        lm.setSubId(null);
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(41);
        lm.setTargetLocale(locale);
        lm.setOrderNum((short)1);
        lm.hashCode();
    }

    @Test
    public void testHashCodeWithNullTargetLocale()
    {
        LeverageMatch lm = new LeverageMatch();
        lm.setOriginalSourceTuv(1000);
        lm.setSubId(null);
        lm.setTargetLocale(null);
        lm.setOrderNum((short)1);
        lm.hashCode();
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.main("com.globalsight.everest.integration.ling.tm2.LeverageMatchTest");
    }
}
