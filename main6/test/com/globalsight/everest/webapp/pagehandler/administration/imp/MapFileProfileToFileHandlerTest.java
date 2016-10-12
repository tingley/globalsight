package com.globalsight.everest.webapp.pagehandler.administration.imp;

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;

import org.junit.Test;

public class MapFileProfileToFileHandlerTest
{
    @Test
    public void testGetFileNames()
    {
        MapFileProfileToFileHandler mp = new MapFileProfileToFileHandler();
        String[] str =
        { "E:\\Hi, Leon.html" };
        String fileNames = SetFileNamesString(str);
        // Call the method I am actually testing
        String[] result = mp.getFileNames(fileNames);
        assertEquals(str.length, result.length);
        for (int i = 0; i < result.length; i++)
        {
            assertEquals(str[i], result[i]);
        }

        String[] str0 =
        { "E:\\Hi, Leon.html", "E:\\I789am.html" };
        fileNames = SetFileNamesString(str0);
        // Call the method I am actually testing
        String[] result0 = mp.getFileNames(fileNames);
        assertEquals(str0.length, result0.length);
        for (int i = 0; i < result0.length; i++)
        {
            assertEquals(str0[i], result0[i]);
        }

        // file path with !@#$%^&*(),.';:"
        String[] str1 =
        { "E:\\Hi\"#, Leon.html", "E:\\path!@#$%^&*(),.\';:\"dasdas asd.html",
                "E:\\I789am,.,.,.dasdsad.html" };
        fileNames = SetFileNamesString(str1);
        // Call the method I am actually testing
        String[] result1 = mp.getFileNames(fileNames);
        assertEquals(str1.length, result1.length);
        for (int i = 0; i < result1.length; i++)
        {
            assertEquals(str1[i], result1[i]);
        }
    }

    /**
     * Help to test GBS-1831
     * 
     * @param files
     * @return
     */
    private static String SetFileNamesString(String[] files)
    {
        String fileString = "";
        if (files != null && files.length > 0)
        {
            fileString += URLEncoder.encode(files[0]);
            for (int i = 1; i < files.length; i++)
            {
                String selfFile = URLEncoder.encode(files[i]);
                fileString += "," + selfFile;
            }
        }
        return fileString;
    }
}