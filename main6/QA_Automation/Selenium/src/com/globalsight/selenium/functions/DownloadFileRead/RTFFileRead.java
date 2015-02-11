package com.globalsight.selenium.functions.DownloadFileRead;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * Read RTF content
 * 
 * @author leon
 * 
 */
public class RTFFileRead
{

    private File file = null;

    public RTFFileRead(File p_file)
    {
        file = p_file;
    }

    /**
     * Read the content with ref editor
     * 
     * @return content
     */
    public String extractWithRTFEditorKit()
    {
        String result = null;
        try
        {
            DefaultStyledDocument styledDoc = new DefaultStyledDocument();
            InputStream is = new FileInputStream(file);
            new RTFEditorKit().read(is, styledDoc, 0);
            result = new String(styledDoc.getText(0, styledDoc.getLength())
                    .getBytes("ISO8859_1"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Read the content with read line
     * 
     * @return content
     */
    public String extractWithBufferedReader()
    {
        String result = "";
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.read() != -1)
            {
                result = result + br.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}