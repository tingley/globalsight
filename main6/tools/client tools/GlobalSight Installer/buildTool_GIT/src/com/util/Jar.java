/**
 * 
 */
package com.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Jar
{
    private static Logger log = Logger.getLogger(Jar.class);

    private String buildPath;
    private String newPath;
    private List<String> snippets = new ArrayList<String>();

    public Jar(String buildPath, String newPath, String... snippets)
    {
        this.buildPath = buildPath;
        this.newPath = newPath;
        for (String snippet : snippets)
        {
            this.snippets.add(snippet);
        }
    }

    public boolean accept(String path)
    {
        for (String snippet : snippets)
        {
            if (path.indexOf(snippet) > 0)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public String getName()
    {
        return new File(buildPath).getName();
    }

    public void add()
    {
        try
        {
            FileUtil.copyFile(new File(buildPath), new File(newPath));
            log("Add: " + getName());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }

    private void log(String msg)
    {
        System.out.println(msg);
        log.info(msg);
    }
}
