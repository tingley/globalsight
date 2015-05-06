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
						try
						{
							helper.gitConnectorPull();
							helper.gitConnectorPush(cacheFile.getFilePath());
						}
						catch (Exception e)
						{
							logger.error("Push git file failed.Git connector id : " + cacheFile.getGitConnectorId() 
									+ ", file path : " + cacheFile.getFilePath() + ".", e);
						}
						HibernateUtil.delete(cacheFile);
					}
				}
				HibernateUtil.commit(tx);
				
				Thread.sleep(60 * 1000);
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

