package com.globalsight.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.junit.*;

import com.globalsight.util.file.XliffFileUtil;

public class XliffFileUtilTest
{
    private File filename = null;
    private FileUtilTest fileUtilTest = new FileUtilTest();
    
    @Test
    public void testIsXliffFile() throws IOException {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertTrue(XliffFileUtil.isXliffFile(filename.getAbsolutePath()));
        filename = fileUtilTest.getTestFile("testdata/xliff/demo_header.txt");
        assertFalse(XliffFileUtil.isXliffFile(filename.getAbsolutePath()));
    }
    
    @Test
    public void testIsMultipleFileTags() throws IOException {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertFalse(XliffFileUtil.isMultipleFileTags(filename.getAbsolutePath()));
        filename = fileUtilTest.getTestFile("testdata/xliff/demo_multi.xlf");
        assertTrue(XliffFileUtil.isMultipleFileTags(filename.getAbsolutePath()));
    }
    
    @Test
    public void testContainFileTag() throws IOException {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertTrue(XliffFileUtil.containsFileTag(filename.getAbsolutePath()));
    }
}
