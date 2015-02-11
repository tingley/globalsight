package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.File;

import com.globalsight.util.FileUtil;

public class FileSystemEntity
{
    private String fileId;
    private String parentFileId;
    private String fileName;
    private String filePath;
    
    public FileSystemEntity(File file)
    {
        this.fileId = FileUtil.getFileNo(file.getPath());
        this.parentFileId = FileUtil.getFileNo(file.getParentFile().getPath());
        this.fileName = file.getName();
    }
    
    public FileSystemEntity(File file, String path)
    {
        this.fileId = FileUtil.getFileNo(file.getPath());
        this.fileName = file.getName();
        this.filePath = file.getPath().replace("\\", "\\\\");
        if (file.getParent().equals(path))
        {
            this.parentFileId = "0";
        }
        else 
        {
            this.parentFileId = FileUtil.getFileNo(file.getParentFile().getPath());
        }
    }
    
    public String getFileId()
    {
        return fileId;
    }

    public void setFileId(String fileId)
    {
        this.fileId = fileId;
    }

    public String getParentFileId()
    {
        return parentFileId;
    }

    public void setParentFileId(String parentFileId)
    {
        this.parentFileId = parentFileId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    
}
