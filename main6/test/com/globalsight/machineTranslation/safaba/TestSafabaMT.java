package com.globalsight.machineTranslation.safaba;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSafabaMT
{

    private String hostName = null;
    private int port = 0;
    private String endClient = null;
    private String password = null;
    private String endClientLSP = null;

    @Before
    public void setUp()
    {
        hostName = "boon2.safaba.com";
        port = 8110;
        endClient = "Test";
        password = "test#PWD$5";
        endClientLSP = "welocalize";
    }

    @Test
    public void testTranslation()
    {
        String text = "Hello world.";
        String langPair = "enus-dede";
        try
        {
            String translation = SafabaTranslateUtil.translate(hostName, port,
                    endClient, password, endClientLSP, langPair, text, 30);
            Assert.assertNotNull(translation);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

//    @Test
//    public void testBatchTranslation()
//    {
//        String langPair = "enus-dede";
//        String[] segments = new String[2];
//        segments[0] = "Nice to see you.";
//        segments[1] = "How are you?";
//
//        try
//        {
//            String[] translations = SafabaTranslateUtil.batchTranslate(
//                    hostName, port, endClient, password, endClientLSP,
//                    langPair, segments, 30);
//            Assert.assertNotNull(translations);
//            Assert.assertEquals(translations.length, segments.length);
//        }
//        catch (Exception e)
//        {
//            fail(e.getMessage());
//        }
//    }

    @After
    public void tearDown()
    {
        hostName = null;
        port = 0;
        endClient = null;
        password = null;
        endClientLSP = null;
    }

}
