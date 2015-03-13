package com.globalsight.connector.git;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorCacheFile;
import com.globalsight.persistence.hibernate.HibernateUtil;


public class GitConnectorPushThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorPushThread.class);
    
    public GitConnectorPushThread()
    {
        super();
    }

    private void startPush() throws Exception
    {
        try
        {
           List<GitConnectorCacheFile> cacheFiles = (List<GitConnectorCacheFile>) 
           		GitConnectorManagerLocal.getAllCacheFiles();
           if(cacheFiles != null && cacheFiles.size() > 0)
           {
        	   for(GitConnectorCacheFile cacheFile: cacheFiles)
        	   {
        		   GitConnector gc = GitConnectorManagerLocal
        		   		.getGitConnectorById(cacheFile.getGitConnectorId());
        		   GitConnectorHelper helper = new GitConnectorHelper(gc);
        		   helper.gitConnectorPush(cacheFile.getFilePath());
        		   HibernateUtil.delete(cacheFile);
        	   }
        	   startPush();
           }
           else
           {
        	   Thread.sleep(60 * 1000);
        	   startPush();
           }
        }
        catch (Exception e)
        {
            logger.error("Push git file failed.", e);
        }
    }

    @Override
    public void run()
    {
        try
        {
            startPush();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
}

