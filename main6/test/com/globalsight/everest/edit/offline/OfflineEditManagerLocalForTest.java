package com.globalsight.everest.edit.offline;

import java.io.File;
import java.util.Vector;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.taskmanager.Task;

public class OfflineEditManagerLocalForTest extends OfflineEditManagerLocal
{

    private static Vector<String> fileNames = new Vector<String>();
    
    public Vector<String> getFileNames()
    {
        return fileNames;
    }

    public void setFileNames(Vector<String> fileNames)
    {
        this.fileNames = fileNames;
    }

    @Override
    public String runProcessUploadPage(File p_tmpFile, User p_user,
            Task p_task, String p_fileName) throws AmbassadorDwUpException
    {
        try
        {
            Thread.sleep(1000);
            fileNames.add(p_fileName);
            this.getStatus().setPercentage(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return "";
    }
}
