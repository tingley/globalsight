package com.globalsight.connector.eloqua.models;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.globalsight.util.FileUtil;


public class LandingPage extends EloquaObject
{
    static private final Logger logger = Logger.getLogger(LandingPage.class);
    
    @Override
    public String getDisplayId()
    {
        return "p" + getId();
    }
   
    public void saveToFile(File f)
    {
        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append(html);
            FileUtil.writeFile(f, sb.toString(), "utf-8");
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    /**
     * Update the translated name, subject and html body
     * @param f
     */
    public void updateFromFile(File f)
    {
        String content;
        try
        {
            content = FileUtil.readFile(f, "utf-8");
            setHtml(content);
        }
        catch (IOException e)
        {
            logger.error(e);
        }        
    }
}