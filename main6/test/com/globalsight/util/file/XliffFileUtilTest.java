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
package com.globalsight.util.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.util.ClassUtil;
import com.globalsight.util.FileUtilTest;

import junit.framework.Assert;

public class XliffFileUtilTest
{
    private File filename = null;
    private FileUtilTest fileUtilTest = new FileUtilTest();

    private static String relativeFilename;

    @BeforeClass
    public static void beforeClass()
    {
        // GBS-4469
        relativeFilename = "testdata/xliff/Export3_zh.xlf";
    }

    @Test
    public void testIsXliffFile() throws IOException
    {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertTrue(XliffFileUtil.isXliffFile(filename.getAbsolutePath()));
        filename = fileUtilTest.getTestFile("testdata/xliff/demo_header.txt");
        assertFalse(XliffFileUtil.isXliffFile(filename.getAbsolutePath()));
    }

    @Test
    public void testIsMultipleFileTags() throws IOException
    {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertFalse(XliffFileUtil.isMultipleFileTags(filename.getAbsolutePath()));
        filename = fileUtilTest.getTestFile("testdata/xliff/demo_multi.xlf");
        assertTrue(XliffFileUtil.isMultipleFileTags(filename.getAbsolutePath()));
    }

    @Test
    public void testContainFileTag() throws IOException
    {
        filename = fileUtilTest.getTestFile("testdata/xliff/demo.xlf");
        assertTrue(XliffFileUtil.containsFileTag(filename.getAbsolutePath()));
    }

    // GBS-4469
    @Test
    public void testProcessMultipleFileTags()
    {
        XliffFileUtil xliffFileUtil = new XliffFileUtil();
        MultipleFileTagsXliff multipleFileTagsXliff = new MultipleFileTagsXliff();
        String expectedHeader = "";
        try
        {
            File testFile = fileUtilTest.getTestFile(relativeFilename);
            String filePath = testFile.getAbsolutePath();
            String fileContent = (String) ClassUtil.testMethodWithException(xliffFileUtil,
                    "getFileContent", filePath);
            expectedHeader = getExpectedHeader(fileContent);

            ClassUtil.testMethodWithException(xliffFileUtil, "separateFileContent", new Object[]
            { multipleFileTagsXliff, filePath, fileContent });
        }
        catch (Exception e)
        {
            // the exception happens because the db connection or properties
            // condition used in the tested method is not satisfied, but it does
            // not affect the result we need to test.
            Assert.assertEquals(expectedHeader, multipleFileTagsXliff.getHeader());
        }
    }

    private String getExpectedHeader(String fileContent)
    {
        String header = "";
        int headerBeginWithoutBomIndex = fileContent.indexOf("<");
        int contentBeginIndex = fileContent.indexOf("<file ");
        // Get header of original Xliff file
        if (headerBeginWithoutBomIndex != -1 && contentBeginIndex != -1
                && headerBeginWithoutBomIndex < contentBeginIndex)
        {
            header = fileContent.substring(headerBeginWithoutBomIndex, contentBeginIndex);
        }

        return header;
    }
}
