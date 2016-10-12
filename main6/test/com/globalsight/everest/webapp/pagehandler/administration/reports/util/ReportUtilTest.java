package com.globalsight.everest.webapp.pagehandler.administration.reports.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ReportUtilTest
{

    @Test
    public void testGetCurrencySymbol()
    {
        String result = ReportUtil.getCurrencySymbol("US Dollar (USD)");
        assertEquals("$", result);

        result = ReportUtil.getCurrencySymbol("Euro (EUR)");
        assertEquals("\u20AC", result);

        result = ReportUtil.getCurrencySymbol("Chinese Renminbi-Yuan (CNY)");
        assertEquals("\u00a5", result);

        result = ReportUtil.getCurrencySymbol("US Dollar");
        assertEquals("", result);

        result = ReportUtil.getCurrencySymbol("");
        assertEquals("", result);

        result = ReportUtil.getCurrencySymbol(null);
        assertEquals("", result);
    }

    @Test
    public void testGetCurrencyName()
    {
        String result = ReportUtil.getCurrencyName("US Dollar (USD)");
        assertEquals("US Dollar", result);

        result = ReportUtil.getCurrencyName("Euro (EUR)");
        assertEquals("Euro", result);

        result = ReportUtil.getCurrencyName("Chinese Renminbi-Yuan (CNY)");
        assertEquals("Chinese Renminbi-Yuan", result);

        result = ReportUtil.getCurrencyName("(asd) asd");
        assertEquals("asd", result);

        result = ReportUtil.getCurrencyName("(a sd) as d");
        assertEquals("as d", result);

        result = ReportUtil.getCurrencyName("(/a sd) as d/");
        assertEquals("as d/", result);

        try
        {
            result = ReportUtil.getCurrencyName("");
            fail();
        }
        catch (IllegalArgumentException ie)
        {
            return;
        }
        catch (Exception e)
        {
            fail();
        }

        try
        {
            result = ReportUtil.getCurrencyName(null);
            fail();
        }
        catch (IllegalArgumentException ie)
        {
            return;
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Test
    public void testToChar()
    {
        String result = ReportUtil.toChar(0);
        assertEquals("A", result);

        result = ReportUtil.toChar(1);
        assertEquals("B", result);

        result = ReportUtil.toChar(25);
        assertEquals("Z", result);

        result = ReportUtil.toChar(26);
        assertEquals("AA", result);

        result = ReportUtil.toChar(51);
        assertEquals("AZ", result);

        result = ReportUtil.toChar(-1);
        assertEquals("", result);
        
        result = ReportUtil.toChar(52);
        assertEquals("", result);    
    }
}
