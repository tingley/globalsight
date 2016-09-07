/**
 *  Copyright 2009 Welocalize, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.globalsight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class FileUtilTest
{

    @Test
    public void testXmlFileEncoding() throws Exception
    {
        // Assume UTF-8 if we can't find a BOM or a header
        assertEquals("UTF-8", getEncoding("testdata/utf8_default.xml"));
        // Read the BOM
        assertEquals("UTF-16BE", getEncoding("testdata/utf16be_bom.xml"));
        // Read the header
        assertEquals("UTF-16", getEncoding("testdata/utf16.xml"));
    }

    protected String getEncoding(String filename) throws IOException
    {
        return FileUtil.getEncodingOfXml(getTestFile(filename));
    }

    public File getTestFile(String filename) throws IOException
    {
        String fullname = getClass().getResource(filename).getFile();
        return new File(fullname);
    }

    public File getTestFile(Class className, String filename) throws IOException
    {
        String filePath = className.getResource(filename).getFile();
        return new File(filePath);
    }

    @Test
    public void testDeleteTempFile() throws Exception
    {
        File tmpFile;

        tmpFile = File.createTempFile("GSTestDelete", null);
        FileUtil.deleteTempFile(tmpFile);
        assertFalse(tmpFile.exists());

        // It's ok if already deleted.
        tmpFile = File.createTempFile("GSTestDelete", null);
        assertTrue(tmpFile.delete());
        assertFalse(tmpFile.exists());
        FileUtil.deleteTempFile(tmpFile);
        assertFalse(tmpFile.exists());

        // Create a non-empty directory to simulate an un-deletable file.
        // It's ok if can't be deleted; there should be a warning but that's
        // hard to test for.
        tmpFile = File.createTempFile("GSTestDelete", null);
        assertTrue(tmpFile.delete());
        assertFalse(tmpFile.exists());
        assertTrue(tmpFile.mkdir());
        File child = new File(tmpFile, "foo");
        assertTrue(child.createNewFile());
        assertFalse(tmpFile.delete());
        FileUtil.deleteTempFile(tmpFile);
        assertTrue(tmpFile.exists());
        // clean up
        assertTrue(child.delete());
        assertTrue(tmpFile.delete());
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.main("com.globalsight.util.FileUtilTest");
    }

    @Test
    public void testIsUTFFormat()
    {
        assertTrue(FileUtil.isUTFFormat("UTF-8"));
        assertTrue(FileUtil.isUTFFormat("UTF-16LE"));
        assertTrue(FileUtil.isUTFFormat("UTF-16BE"));
        assertTrue(FileUtil.isUTFFormat("UTF-16"));
        assertFalse(FileUtil.isUTFFormat("GBK"));
    }

    @Test
    public void testGuessEncoding() throws IOException
    {
        assertEquals(null, FileUtil.guessEncoding(getTestFile("testdata/utf8_default.xml")));
        assertEquals(FileUtil.UTF8, FileUtil.guessEncoding(getTestFile("testdata/demo_utf8.html")));
        assertEquals(FileUtil.UTF16LE,
                FileUtil.guessEncoding(getTestFile("testdata/demo_utf16.html")));
        assertEquals(FileUtil.UTF16BE,
                FileUtil.guessEncoding(getTestFile("testdata/demo_utf16BE.html")));
    }

    @Test
    public void testIsNeedBOMProcessing()
    {
        assertTrue(FileUtil.isNeedBOMProcessing("test.html"));
        assertTrue(FileUtil.isNeedBOMProcessing("test.htm"));
        assertTrue(FileUtil.isNeedBOMProcessing("test.HTML"));
        assertTrue(FileUtil.isNeedBOMProcessing("test.xml"));
        assertFalse(FileUtil.isNeedBOMProcessing("test.pdf"));
    }

    @Test
    public void testReadWriteFile() throws Exception
    {
        File tmpFile;

        tmpFile = File.createTempFile("GSTestReadWrite", null);
        FileUtil.writeFile(tmpFile, "hello world", "US-ASCII");
        assertEquals("hello world", FileUtil.readFile(tmpFile, "US-ASCII"));
        FileUtil.deleteTempFile(tmpFile);

        // Not easy to test the atomic part
        tmpFile = new File(
                System.getProperty("java.io.tmpdir") + File.separator + "GSTestReadWrite.tmp");
        FileUtil.writeFileAtomically(tmpFile, "hello world", "US-ASCII");
        assertEquals("hello world", FileUtil.readFile(tmpFile, "US-ASCII"));
        FileUtil.deleteTempFile(tmpFile);
    }

    @Test
    public void testIsWindowsReturnMethod() throws Exception
    {
        String filename = "";
        filename = getTestFile("testdata/test_w.resx").getAbsolutePath();
        assertTrue(FileUtil.isWindowsReturnMethod(filename));
        filename = getTestFile("testdata/test_u.resx").getAbsolutePath();
        assertTrue(FileUtil.isWindowsReturnMethod(filename));
    }
}
