package com.globalsight.everest.edit.offline.ttx;

import static org.junit.Assert.fail;

import java.io.File;

import junitx.framework.FileAssert;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import com.globalsight.everest.unittest.util.FileUtil;

public class TTXParserTest
{
    private static String sourceRoot = FileUtil.getResourcePath(
            TTXParserTest.class, "source");
    private static String expectedRoot = FileUtil.getResourcePath(
            TTXParserTest.class, "expected");
    private static String resultRoot = FileUtil.getResourcePath(
            TTXParserTest.class, "result");

    /**
     * When off-line upload TTX file,need parse to get the content in another
     * style for further use. This is also for GBS-1820.
     */
    @Test
    public void testParseToTxt()
    {
        sourceRoot += File.separator + "source.ttx";
        expectedRoot += File.separator + "expected.txt";
        resultRoot += File.separator + "result.txt";

        File sourceFile = new File(sourceRoot);
        File expectedFile = new File(expectedRoot);
        File resultFile = new File(resultRoot);

        if (sourceFile.exists())
        {
            try
            {
                // Parse source file to get result content.
                SAXReader reader = new SAXReader();
                Document doc = reader.read(sourceFile);
                TTXParser parser = new TTXParser();
                boolean isParsingTTXForGS = true;
                String ttxContent = parser.parseToTxt(doc, isParsingTTXForGS);

                // Write result into result.txt
                FileUtil.generateFile(resultFile, ttxContent, "UTF-8");

                // Compare the result file and answer file.
                FileAssert.assertEquals(expectedFile, resultFile);
            }
            catch (Exception e)
            {
                fail("TTX upload parsing error. " + e.getMessage());
            }
        }
    }

}
