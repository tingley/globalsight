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
package com.globalsight.everest.cvsconfig;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class CVSFileProfileManagerLocal
{
    private static final Logger c_logger = Logger
            .getLogger(CVSFileProfileManagerLocal.class.getName());

    public void add(CVSFileProfile p_obj) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_obj.setId(CVSConstants.DEFAULT_AUTO_ID);
            p_obj.setCompanyId(CompanyWrapper.getCurrentCompanyIdAsLong());
            session.save(p_obj);
            transaction.commit();
        }
        catch (PersistenceException e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void add(ArrayList<CVSFileProfile> p_objs)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            if (p_objs == null || p_objs.size() == 0)
                return;
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            for (CVSFileProfile cvsfp : p_objs)
            {
                if (!isExist(cvsfp))
                {
                    cvsfp.setId(CVSConstants.DEFAULT_AUTO_ID);
                    session.save(cvsfp);
                }
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void update(CVSFileProfile p_obj)
    {
        CVSFileProfile oldfp = null;
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            oldfp = getCVSFileProfile(p_obj.getId());

            if (oldfp != null)
            {
                oldfp.setModule(p_obj.getModule());
                oldfp.setSourceLocale(p_obj.getSourceLocale());
                oldfp.setFileExt(p_obj.getFileExt());
                oldfp.setFileProfile(p_obj.getFileProfile());
            }
            session.saveOrUpdate(oldfp);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    public void remove(long p_id)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.delete(getCVSFileProfile(p_id));
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    public CVSFileProfile getCVSFileProfile(long p_id)
    {
        CVSFileProfile cvsfp = null;
        try
        {
            String hql = "from CVSFileProfile c where c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            cvsfp = i.hasNext() ? (CVSFileProfile) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Persistence Exception when retrieving CVS file profile "
                            + p_id, pe);
        }
        return cvsfp;
    }

    public Collection getCVSFileProfilesByFileProfileId(long p_id)
    {
        Collection data = null;
        try
        {
            String hql = "from CVSFileProfile c where c.fileProfile.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            data = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Persistence Exception when retrieving CVS file profile "
                            + p_id, pe);
        }
        return data;
    }

    public void removeByFileProfileId(long p_id)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            ArrayList<CVSFileProfile> data = (ArrayList<CVSFileProfile>) getCVSFileProfilesByFileProfileId(p_id);
            if (data != null)
            {
                for (CVSFileProfile fp : data)
                {
                    session.delete(fp);
                }
                transaction.commit();
            }
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    public Collection getAllCVSFileProfiles()
    {
        Collection data = null;
        try
        {
            String hql = "from CVSFileProfile c where 1=1";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and c.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            data = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving CVS file profiles.",
                    pe);
        }
        return data;
    }

    public Collection getAllCVSFileProfiles(HashMap<String, String> p_params)
    {
        if (p_params == null || p_params.size() == 0)
            return getAllCVSFileProfiles();

        Collection data = null;
        try
        {
            String hql = "from CVSFileProfile c where 1=1";
            HashMap map = new HashMap();
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and c.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            String project = "", module = "", sourceLocale = "", fileExt = "";
            project = p_params.get("project");
            module = p_params.get("module");
            sourceLocale = p_params.get("sourceLocale");
            fileExt = p_params.get("fileExt");
            if (project != null && !project.trim().equals("-1"))
            {
                hql += " and c.project.id=:project";
                map.put("project", Long.parseLong(project));
            }
            if (module != null && !module.trim().equals("-1"))
            {
                hql += " and c.module.id=:module";
                map.put("module", Long.parseLong(module));
            }
            if (sourceLocale != null && !sourceLocale.trim().equals("-1"))
            {
                hql += " and c.sourceLocale=:sourceLocale";
                map.put("sourceLocale", sourceLocale);
            }
            if (fileExt != null && !fileExt.trim().equals(""))
            {
                hql += " and c.fileExt=:fileExt";
                map.put("fileExt", fileExt);
            }

            data = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving CVS file profiles.",
                    pe);
        }
        return data;
    }

    public Collection getFileExtensions(String p_projectId)
    {
        Collection data = null;
        try
        {
            String hql = "from FileProfileExtension c where c.fileProfile.companyId=:companyId and c.fileProfile.isActive='Y'";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentId));
            hql += " and c.fileProfile.l10nProfileId in (select b.id from BasicL10nProfile b where b.isActive='Y' and b.project.id=:projectId)";
            map.put("projectId",
                    p_projectId == null ? -1 : Long.parseLong(p_projectId));
            hql += " order by c.extension.name asc";
            data = HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            c_logger.error("Exception found in getFileExtensions.", e);
        }
        return data;
    }

    public boolean isExist(CVSFileProfile p_obj)
    {
        Collection data = null;
        try
        {
            String hql = "from CVSFileProfile c where 1=1";
            HashMap map = new HashMap();
            if (p_obj.getProject() != null)
            {
                hql += " and c.project.id=:projectId";
                map.put("projectId", p_obj.getProject().getId());
            }
            if (p_obj.getModule() != null)
            {
                hql += " and c.module.id=:moduleId";
                map.put("moduleId", p_obj.getModule().getId());
            }
            if (!p_obj.getSourceLocale().equals(""))
            {
                hql += " and c.sourceLocale=:srcLocale";
                map.put("srcLocale", p_obj.getSourceLocale());
            }
            if (!p_obj.getFileExt().equals(""))
            {
                hql += " and c.fileExt=:fileExt";
                map.put("fileExt", p_obj.getFileExt());
            }
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            return i.hasNext() ? true : false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
