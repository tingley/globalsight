package com.globalsight.cxe.entity.filterconfiguration;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class BaseFilterParserTest
{
    private BaseFilterParser p;
    
    @Test
    public void testGetInternalTexts() throws Exception
    {
        String configXml = "<BaseFilterConfig><internalTexts>" +
        "<array><aName>test</aName><enable>true</enable><isRE>true</isRE><itemid>1309986121465</itemid></array>" +
        "<array><aName>ttt</aName><enable>false</enable><isRE>false</isRE><itemid>1310003730361</itemid></array>" +
        "</internalTexts></BaseFilterConfig>";
        p = new BaseFilterParser(configXml);
        p.parserXml();
        List<InternalText> its = p.getInternalTexts();
        assertTrue("Size is not 1. ", its.size() == 1);
        assertTrue(its.get(0).getName().equals("test"));
        assertTrue(its.get(0).isRE());
    }
    
    @Test
    public void testGetInternalTextsEmpty() throws Exception
    {
        String configXml = null;
        p = new BaseFilterParser(configXml);
        p.parserXml();
        List<InternalText> its = p.getInternalTexts();
        assertTrue("Size is not 0. ", its.size() == 0);
    }
}
