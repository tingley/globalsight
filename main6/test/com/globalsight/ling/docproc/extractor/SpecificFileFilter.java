package com.globalsight.ling.docproc.extractor;

import java.io.File;
import java.io.FileFilter;

public class SpecificFileFilter implements FileFilter
{
    private String fileExtension;
    
    public SpecificFileFilter()
    {
        fileExtension = "";
    }
    
    public SpecificFileFilter(String extension)
    {
        fileExtension = extension;
    }
    
    public String getFileExtension()
    {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    public boolean accept(File pathname)
    {
        String filename = pathname.getName().toLowerCase();
        if (fileExtension == null || fileExtension.length() == 0)
        {
            return true;
        }
        else if (filename.endsWith("." + fileExtension.toLowerCase()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
