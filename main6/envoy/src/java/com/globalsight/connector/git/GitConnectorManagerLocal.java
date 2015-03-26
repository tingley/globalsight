package com.globalsight.connector.git;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorCacheFile;
import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;
import com.globalsight.cxe.entity.gitconnector.GitConnectorJob;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class GitConnectorManagerLocal
{
    private static final Logger logger = Logger
            .getLogger(GitConnectorManagerLocal.class);

    public static List<?> getAllConnectors()
    {
        String hql = "from GitConnector e where e.isActive = 'Y' ";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and e.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    public static GitConnector getGitConnectorById(long gitcId)
    {
        return HibernateUtil.get(GitConnector.class, gitcId);
    }
    
    public static GitConnectorJob getGitConnectorJobByJobId(long jobId)
    {
    	String hql = "from GitConnectorJob e where e.jobId = " + jobId;
    	return (GitConnectorJob) HibernateUtil.getFirst(hql);
    }
    
    public static GitConnectorFileMapping getGitConnectorFileMappingById(long gcfmId)
    {
    	 return HibernateUtil.get(GitConnectorFileMapping.class, gcfmId);
    }
    
    public static List<?> getAllFileMappings(long gitConnectorId)
    {
        String hql = "from GitConnectorFileMapping e where e.isActive = 'Y' and " +
        		" e.gitConnectorId = :gitConnectorId and e.parentId = :parentId ";
        HashMap<String, Long> map = new HashMap<String, Long>();
        map.put("gitConnectorId", gitConnectorId);
        map.put("parentId", new Long(-1));

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and e.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }
    
    public static List<?> getAllFileMappings(long gitConnectorId,
    		String sourceLocale, String targetLocale)
    {
    	String hql = "from GitConnectorFileMapping e where e.isActive = 'Y' and " +
				" e.gitConnectorId = :gitConnectorId and e.sourceLocale =  '" + sourceLocale + "' " +
				" and e.targetLocale = '" + targetLocale + "' ";
		HashMap<String, Long> map = new HashMap<String, Long>();
		map.put("gitConnectorId", gitConnectorId);
		String currentId = CompanyThreadLocal.getInstance().getValue();
		if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
		{
		    hql += " and e.companyId = :companyId";
		    map.put("companyId", Long.parseLong(currentId));
		}
		return HibernateUtil.search(hql, map);
    }
    
    public static List<?> getAllFileMapping(long gitConnectorId,
    		String sourceLocale, Set<String> targetLocales)
    {
    	String hql = "from GitConnectorFileMapping e where e.isActive = 'Y' and " +
				" e.gitConnectorId = :gitConnectorId and e.sourceLocale =  '" + sourceLocale + "' " +
				" and e.targetLocale in ( ";
		HashMap map = new HashMap();
		map.put("gitConnectorId", gitConnectorId);
		String targetLoclestr = "";
		for(String targetLocale: targetLocales)
		{
			targetLoclestr = targetLoclestr + "'" + targetLocale + "',";
		}
		hql += targetLoclestr.substring(0, targetLoclestr.length() -1) + ") ";
		String currentId = CompanyThreadLocal.getInstance().getValue();
		if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
		{
		    hql += " and e.companyId = :companyId";
		    map.put("companyId", Long.parseLong(currentId));
		}
		return HibernateUtil.search(hql, map);
    }
    
    public static List<?> getAllSonFileMappings(long parentId)
    {
        String hql = "from GitConnectorFileMapping e where e.isActive = 'Y' and " +
        		" e.parentId = :parentId ";
        HashMap<String, Long> map = new HashMap<String, Long>();
        map.put("parentId", parentId);

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and e.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }
    
    public static GitConnectorFileMapping isFileMappingExist(GitConnectorFileMapping gcfm)
    {
    	try
        {
            String hql = "from GitConnectorFileMapping c where c.isActive='Y' and c.sourceLocale=:srcLocale and c.targetLocale=:tarLocale and "
                    + "c.sourceMappingPath=:srcMappingPath and c.companyId=:companyId and id!=:Id and c.gitConnectorId = :gcId";
            Map map = new HashMap();
            map.put("srcLocale", gcfm.getSourceLocale());
            map.put("tarLocale", gcfm.getTargetLocale());
            map.put("srcMappingPath", gcfm.getSourceMappingPath());
            // map.put("tarModule", p_mm.getTargetModule());
            map.put("companyId", gcfm.getCompanyId());
            map.put("Id", gcfm.getId());
            map.put("gcId",gcfm.getGitConnectorId());

            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            return i.hasNext() ? (GitConnectorFileMapping) i.next() : null;
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when verify if exist Git Connector file mapping "
                            + gcfm.getSourceLocale() + ","
                            + gcfm.getTargetLocale() + ","
                            + gcfm.getSourceMappingPath() + ","
                            + gcfm.getTargetMappingPath(), pe);
            return null;
        }
    }
}
