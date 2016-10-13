package com.globalsight.everest.unittest.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;

import junitx.framework.FileAssert;

public class FileUtil
{
    /**
     * Get simple file MD5 code
     */
    public static String getFileMD5(File file)
    {
        if (!file.isFile())
        {
            return null;
        }
        
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
    
    /**
     * Compare the content of two files based on MD5.
     * @param File A
     * @param File B
     * @return Returns true if file A is equal to file B,
     * else return false
     */
    public static boolean fileCompare(File a, File b)
    {
        String codeOfA = FileUtil.getFileMD5(a);
        String codeOfB = FileUtil.getFileMD5(b);

        if (codeOfA.equals(codeOfB))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Used to get resource path relative to current test class.
     */
    public static String getResourcePath(Class c, String relativePath)
    {
        return c.getResource(relativePath).getFile();
    }
    
    /**
     * Write the content to file with specified encoding.
     */
    public static void generateFile(File file, String content, String encoding)
            throws IOException
    {
        Writer fw = null;
        BufferedWriter bw = null;
        try
        {
            fw = new OutputStreamWriter(new FileOutputStream(file), encoding);
            bw = new BufferedWriter(fw);
            bw.write(content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bw != null)
                bw.close();
            if (fw != null)
                fw.close();
        }
    }
    
    /**
     * As FileAssert.assertEquals(file1,file2) is based on Reader.readline(),it
     * has no end lining problem. So if want to ignore end line to compare file
     * content, suggest to use this method.
     */
    public static boolean fileCompareNoCareEndLining(File expected, File actual)
    {
        try
        {
            FileAssert.assertEquals(expected, actual);
        }
        catch (Exception e)
        {
            return false;
        }
        
        return true;
    }

    /**
     * Read xml rule from file for testing
     * @param fileName
     * @param charsetName
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readRuleFile(String fileName, String charsetName)
            throws FileNotFoundException, IOException
    {
        if (fileName == null || "".equals(fileName))
        {
            return "";
        }

        InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), charsetName);
        StringBuffer buf = new StringBuffer();
        int ch;
        while ((ch = reader.read()) != -1)
        {
            buf.append((char) ch);
        }

        return buf.toString();
    }
}
