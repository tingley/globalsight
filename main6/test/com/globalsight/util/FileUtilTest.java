package com.globalsight.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.*;

public class FileUtilTest {

    @Test
    public void testXmlFileEncoding() throws Exception {
        // Assume UTF-8 if we can't find a BOM or a header
        assertEquals("UTF-8", getEncoding("testdata/utf8_default.xml"));
        // Read the BOM
        assertEquals("UTF-16BE", getEncoding("testdata/utf16be_bom.xml"));
        // Read the header
        assertEquals("UTF-16", getEncoding("testdata/utf16.xml"));
    }

    protected String getEncoding(String filename) throws IOException {
        return FileUtil.getEncodingOfXml(getTestFile(filename));
    }
    
    protected File getTestFile(String filename) throws IOException {
        String fullname = getClass().getResource(filename).getFile();
        return new File(fullname);
    }
    
    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("com.globalsight.util.FileUtilTest");
    }
}
