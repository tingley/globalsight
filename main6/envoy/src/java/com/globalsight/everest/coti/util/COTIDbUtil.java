/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.coti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIJob;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Database UTIL class for COTI jobs
 * 
 */
public class COTIDbUtil
{

    private static final Logger logger = Logger.getLogger(COTIDbUtil.class);
    private static HashMap<Class, Map<String, Object>> m_cache = null;
    private static Object m_cacheLocker1 = new Object();

    /**
     * cache object for using
     * @param id
     * @param obj
     */
    public static void cacheObject(long id, Object obj)
    {
        if (m_cache == null)
        {
            synchronized (m_cacheLocker1)
            {
                if (m_cache == null)
                {
                    m_cache = new HashMap<Class, Map<String, Object>>();
                }
            }
        }

        Class c = obj.getClass();
        Map<String, Object> map = m_cache.get(c);
        if (map == null)
        {
            synchronized (c)
            {
                if (map == null)
                {
                    map = new HashMap<String, Object>();
                    m_cache.put(c, map);
                }
            }
        }

        map.put("" + id, obj);
    }

    /**
     * Get cached object
     * @param id
     * @param cl
     * @return
     */
    public static Object getCacheObject(long id, Class cl)
    {
        if (m_cache == null)
        {
            return null;
        }

        Map<String, Object> map = m_cache.get(cl);
        if (map == null)
        {
            return null;
        }

        return map.get("" + id);
    }

    /**
     * Get COTI package by its id
     * 
     * @param id
     * @return
     */
    public static COTIPackage getCOTIPackage(long id)
    {
        COTIPackage cd = HibernateUtil.get(COTIPackage.class, id);

        if (cd == null)
        {
            cd = (COTIPackage) getCacheObject(id, COTIPackage.class);
        }
        
        return cd;
    }

    /**
     * Get COTI package by id
     * 
     * @param id
     * @return
     */
    public static COTIPackage getCOTIPackage(String id)
    {
        if (id == null || id.length() == 0)
        {
            return null;
        }

        long idd = Long.parseLong(id);
        return getCOTIPackage(idd);
    }

    /**
     * Get COTI project by id
     * 
     * @param id
     * @return
     */
    public static COTIProject getCOTIProject(long id)
    {
        COTIProject cd = HibernateUtil.get(COTIProject.class, id);

        if (cd == null)
        {
            cd = (COTIProject) getCacheObject(id, COTIProject.class);
        }
        
        return cd;
    }

    /**
     * Get COTI project by id
     * 
     * @param id
     * @return
     */
    public static COTIProject getCOTIProject(String id)
    {
        if (id == null || id.length() == 0)
        {
            return null;
        }

        long idd = Long.parseLong(id);
        return getCOTIProject(idd);
    }

    /**
     * Get COTI documents by its project id
     * 
     * @param pid
     * @return
     */
    public static List<COTIDocument> getCOTIDocumentsByProjectId(long projectId)
    {
        String hql = "from COTIDocument d where d.projectId = " + projectId;
        List<COTIDocument> cds = (List<COTIDocument>) HibernateUtil.search(hql);

        return cds;
    }

    /**
     * Get COTI project by GlobalSight Job Id
     * 
     * @param jid
     * @return
     */
    public static COTIProject getCOTIProjectByGlobalSightJobId(long jid)
    {
        String hql = "from COTIProject d where d.globalsightJobId = " + jid;
        List<COTIProject> ppps = (List<COTIProject>) HibernateUtil.search(hql);

        return (ppps != null && ppps.size() > 0) ? ppps.get(0) : null;
    }

    /**
     * Get COTI document by its id
     * 
     * @param id
     * @return
     */
    public static COTIDocument getCOTIDocument(long id)
    {
        COTIDocument cd = HibernateUtil.get(COTIDocument.class, id);

        if (cd == null)
        {
            cd = (COTIDocument) getCacheObject(id, COTIDocument.class);
        }
        
        return cd;
    }

    /**
     * Get GlobalSight job by COTI project
     * 
     * @param cproject
     * @return
     */
    public static Job getGlobalSightJob(COTIProject cproject)
    {
        if (cproject == null)
        {
            return null;
        }

        long id = cproject.getGlobalsightJobId();
        if (id <= 0)
        {
            return null;
        }

        return HibernateUtil.get(JobImpl.class, id);
    }

    /**
     * Get COTI document by id
     * 
     * @param id
     * @return
     */
    public static COTIDocument getCOTIDocument(String id)
    {
        if (id == null || id.length() == 0)
        {
            return null;
        }

        long idd = Long.parseLong(id);
        return getCOTIDocument(idd);
    }

    /**
     * Get COTI document id by its project id, file type, fileRef
     * 
     * @param projectId
     * @param fileType
     *            translation or reference
     * @param fileRef
     * @return
     */
    public static String getDocumentId(String projectId, String fileType,
            String fileRef)
    {
        String isTrans = COTIConstants.fileType_translation.equals(fileType) ? "Y"
                : "N";

        String hql = "from COTIDocument d where d.projectId = " + projectId
                + " and d.isTranslation = '" + isTrans + "' and d.fileRef = '"
                + fileRef + "'";

        COTIDocument cd = (COTIDocument) HibernateUtil.getFirst(hql);

        if (cd == null)
        {
            Map<String, Object> map = m_cache.get(COTIDocument.class);
            if (map != null)
            {
                Collection values = map.values();
                for (Object obj : values)
                {
                    COTIDocument cdTemp = (COTIDocument) obj;
                    String isTr = cdTemp.getIsTranslation() ? "Y" : "N";
                    String cdProjectId = cdTemp.getProjectId() + "";
                    String cdFileRef = cdTemp.getFileRef();

                    if (projectId.equals(cdProjectId)
                            && isTrans.equals(isTr)
                            && fileRef.equals(cdFileRef))
                    {
                        cd = cdTemp;
                        break;
                    }
                }
            }
        }

        return "" + cd.getId();
    }

    /**
     * Get COTI job list by status list
     * 
     * @param p_stateList
     * @return
     */
    public static List<COTIJob> getCotiJobsByStateList(Vector p_stateList)
    {
        try
        {
            String sql = jobsByStateListQuery(p_stateList);

            return HibernateUtil.searchWithSql(COTIJob.class, sql);
        }
        catch (Exception e)
        {
            logger.error("COTIDbUtil::getCotiJobsByStateList", e);
            String[] args = new String[1];
            args[0] = p_stateList.toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    args, e);
        }
    }

    private static String jobsByStateListQuery(Vector p_listOfStates)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT DISTINCT j.* ");
        sb.append("FROM JOB j");
        sb.append(" WHERE ");
        addStateClause(sb, p_listOfStates, "j");

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            sb.append(" AND j.COMPANY_ID = ");
            sb.append(Long.parseLong(currentId));
        }

        logger.debug("The query is " + sb.toString());

        return sb.toString();
    }

    private static void addStateClause(StringBuffer p_sb,
            Vector p_listOfStates, String p_tableName)
    {
        p_sb.append(p_tableName);
        p_sb.append(".STATE in (");

        for (int i = 0; i < p_listOfStates.size(); i++)
        {
            p_sb.append("'");
            p_sb.append(p_listOfStates.elementAt(i));
            p_sb.append("'");

            if (i < p_listOfStates.size() - 1)
            {
                p_sb.append(", ");
            }
        }

        p_sb.append(")");
    }
    
    public static void deleteCotiProjects(long[] ids)
    {
        List<COTIProject> ps = new ArrayList<COTIProject>();
        
        for (int i = 0; i < ids.length; i++)
        {
            long id = ids[i];
            COTIProject cp = getCOTIProject(id);
            cp.setStatus(COTIConstants.project_status_deleted);
            ps.add(cp);
        }
        
        HibernateUtil.update(ps);
    }

    /**
     * Update one Object in database
     * 
     * @param obj
     */
    public static void update(Object obj)
    {
        HibernateUtil.update(obj);
    }
}
