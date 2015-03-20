package com.globalsight.connector.git;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

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

    public void startPush() throws Exception
    {
		try 
		{
			while (true) 
			{
				Transaction tx = HibernateUtil.getTransaction();
				String hql = " from GitConnectorCacheFile";
				List<GitConnectorCacheFile> cacheFiles = (List<GitConnectorCacheFile>)
						HibernateUtil.search(hql);
				if (cacheFiles != null && cacheFiles.size() > 0) 
				{
					for (GitConnectorCacheFile cacheFile : cacheFiles) 
					{
						GitConnector gc = GitConnectorManagerLocal
								.getGitConnectorById(cacheFile.getGitConnectorId());
						GitConnectorHelper helper = new GitConnectorHelper(gc);
						helper.gitConnectorPush(cacheFile.getFilePath());
						HibernateUtil.delete(cacheFile);
					}
				}
				HibernateUtil.commit(tx);
				
				Thread.sleep(60 * 1000);
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

