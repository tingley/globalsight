package com.globalsight.ling.docproc.extractor.xml;

import org.junit.Test;

import java.io.StringReader;

public class XmlFilterCheckerTest {

    @Test
    public void testSystemId() throws Exception {
        // The existence of the system id (which both lacks a proper
        // URL protocol and also refers to a non-existent file) should
        // not prevent the check from running.
        check("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
              "<!DOCTYPE foo PUBLIC \"-//Dummy//EN\" \"../foo.dtd\">" + 
              "<foo>test</foo>");
    }
    
    @Test
    public void testNBSP() throws Exception {
        // The existence of the system id (which both lacks a proper
        // URL protocol and also refers to a non-existent file) should
        // not prevent the check from running.
        check("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
              "<!DOCTYPE foo PUBLIC \"-//Dummy//EN\" \"../foo.dtd\">" + 
              "<foo>this is &nbsp; test</foo>");
    }

    private void check(String xmlContent) throws Exception {
        XmlFilterChecker.checkWellFormed(xmlContent);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("com.globalsight.ling.docproc.extractor.xml.XmlFilterCheckerTest");
    }
}
