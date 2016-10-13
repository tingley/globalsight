package com.globalsight.everest.cvsconfig.modulemapping;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class ModuleMappingManagerLocal
{
    private static final Logger logger = Logger
            .getLogger(ModuleMappingManagerLocal.class.getName());

    public void addModuleMapping(ModuleMapping p_mm) throws RemoteException,
            ModuleMappingException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_mm.setId(-1L);
            p_mm.setCompanyId(CompanyWrapper.getCurrentCompanyIdAsLong());
            session.save(p_mm);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
            throw new ModuleMappingException(e.getMessage());
        }
    }

    public void addModuleMapping(ArrayList<ModuleMapping> p_mm)
            throws RemoteException, ModuleMappingException
    {
        if (p_mm == null || p_mm.size() == 0)
            return;

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            for (ModuleMapping mm : p_mm)
            {
                // if (isModuleMappingExist(mm) != null)
                // continue;
                mm.setId(-1L);
                session.save(mm);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
            throw new ModuleMappingException(e.getMessage());
        }
    }

    public void updateModuleMapping(ModuleMapping p_mm) throws RemoteException,
            ModuleMappingException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            ModuleMapping old = getModuleMapping(p_mm.getId());
            if (old != null)
            {
                old.setSourceLocale(p_mm.getSourceLocale());
                old.setSourceLocaleLong(p_mm.getSourceLocaleLong());
                old.setSourceModule(p_mm.getSourceModule());
                old.setTargetLocale(p_mm.getTargetLocale());
                old.setTargetLocaleLong(p_mm.getTargetLocaleLong());
                old.setTargetModule(p_mm.getTargetModule());
                session.saveOrUpdate(old);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
        }
    }

    public void updateModuleMapping(ArrayList<ModuleMapping> p_mms)
            throws RemoteException, ModuleMappingException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            ModuleMapping m = p_mms.get(0);
            ModuleMapping km = getModuleMapping(m.getId());
            String newSubFolder = m.getSubFolderMapped();
            String oldSubFolder = km.getSubFolderMapped();
            if (newSubFolder.equals("0"))
            {
                ArrayList<ModuleMapping> mms = (ArrayList<ModuleMapping>) getAllWithSubFolder();
                for (ModuleMapping tm : mms)
                {
                    if (tm.getId() != m.getId())
                    {
                        if (tm.getCompanyId() == m.getCompanyId()
                                && tm.getSourceLocale().equals(
                                        km.getSourceLocale())
                                && tm.getTargetLocale().equals(
                                        km.getTargetLocale())
                                && tm.getSourceModule().startsWith(
                                        km.getSourceModule())
                                && tm.getTargetModule().startsWith(
                                        km.getTargetModule()))
                        {
                            tm.setIsActive(false);
                            session.saveOrUpdate(tm);
                        }
                    }
                }
            }

            km.setSourceLocale(m.getSourceLocale());
            km.setSourceLocaleLong(m.getSourceLocaleLong());
            km.setSourceModule(m.getSourceModule());
            km.setTargetLocale(m.getTargetLocale());
            km.setTargetLocaleLong(m.getTargetLocaleLong());
            km.setTargetModule(m.getTargetModule());
            km.setSubFolderMapped(m.getSubFolderMapped());
            session.update(km);
            if (p_mms.size() > 1)
            {
                p_mms.remove(0);

                for (ModuleMapping mm : p_mms)
                {
                    if ((km = isModuleMappingExist(mm)) != null)
                    {
                        km.setIsActive(true);
                        session.saveOrUpdate(km);
                    }
                    else
                    {
                        if (mm.getId() < 0)
                            mm.setId(-1L);
                        session.saveOrUpdate(mm);
                    }
                }
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
        }
    }

    public void removeModuleMapping(long id)
    {
        ModuleMapping mm = null;
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            mm = getModuleMapping(id);
            Set<ModuleMappingRename> mmrs = mm.getFileRenames();
            for (ModuleMappingRename mmr : mmrs)
            {
                mmr.setModuleMappingId("");
                session.delete(mmr);
                mm.getFileRenames().remove(mmr);
                // session.delete(mmr);
            }
            ArrayList<ModuleMapping> mms = (ArrayList<ModuleMapping>) getAllWithSubFolder();
            for (ModuleMapping m : mms)
            {
                if (m.getId() != id)
                {
                    if (m.getCompanyId() == mm.getCompanyId()
                            && m.getSourceLocale().equals(mm.getSourceLocale())
                            && m.getTargetLocale().equals(mm.getTargetLocale())
                            && m.getSourceModule().startsWith(
                                    mm.getSourceModule())
                            && m.getTargetModule().startsWith(
                                    mm.getTargetModule()))
                    {
                        m.setIsActive(false);
                        session.saveOrUpdate(m);
                    }
                }
            }
            mm.setIsActive(false);
            session.update(mm);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception se)
            {
            }
            logger.error(e.getMessage(), e);
        }
    }

    public ModuleMapping getModuleMapping(long p_id)
    {
        ModuleMapping result = null;
        try
        {
            String hql = "from ModuleMapping c where c.isActive='Y' and c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            result = i.hasNext() ? (ModuleMapping) i.next() : null;
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when retrieving CVS module mapping "
                            + p_id, pe);
        }
        return result;
    }

    public ModuleMapping getModuleMapping(String p_srcLocale,
            String p_tarLocale, String p_srcModule, String p_tarModule,
            String p_company, String p_flag)
    {
        ModuleMapping result = null, kmm = null;
        try
        {
            String hql = "from ModuleMapping c where c.isActive='Y' and c.subFolderMapped='"
                    + (p_flag.equals("2") ? "1" : p_flag)
                    + "' and c.sourceLocale=:srcLocale and c.targetLocale=:tarLocale and c.companyId=:companyId";
            Map map = new HashMap();
            map.put("srcLocale", p_srcLocale);
            map.put("tarLocale", p_tarLocale);
            map.put("companyId", Long.parseLong(p_company));
            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            while (i.hasNext())
            {
                kmm = (ModuleMapping) i.next();
                if (p_srcModule.startsWith(kmm.getSourceModule())
                        && p_tarModule.startsWith(kmm.getTargetModule()))
                    result = kmm;
            }
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when retrieving CVS module mapping "
                            + p_srcLocale + ", " + p_srcModule, pe);
        }
        return result;
    }

    public boolean hasModuleMapping(long p_id)
    {
        boolean result = false;
        try
        {
            String hql = "from ModuleMapping c where c.isActive='Y' and c.moduleId = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            result = i.hasNext() ? true : false;
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when retrieving CVS module mapping "
                            + p_id, pe);
        }
        return result;
    }

    /**
     * Get all module mappings in current company
     * 
     * @return All module mappings
     */
    public Collection getAll()
    {
        Collection result = null;
        try
        {
            String hql = "from ModuleMapping a where a.isActive='Y' and a.subFolderMapped != '2'";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            result = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            logger.error("PersistenceException while retrieving CVS servers.",
                    pe);
            throw new ModuleMappingException(
                    ModuleMappingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES,
                    null, pe);
        }

        return result;
    }

    public Collection getAllWithSubFolder()
    {
        Collection result = null;
        try
        {
            String hql = "from ModuleMapping a where a.isActive='Y'";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            result = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            logger.error("PersistenceException while retrieving CVS servers.",
                    pe);
            throw new ModuleMappingException(
                    ModuleMappingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES,
                    null, pe);
        }

        return result;
    }

    /**
     * Get all module mappings in current company
     * 
     * @return All module mappings
     */
    public Collection getModuleMappings(HashMap<String, String> p_params)
    {
        Collection result = null;
        try
        {
            String hql = "from ModuleMapping a where a.isActive='Y' and a.subFolderMapped!='2'";
            HashMap map = new HashMap();
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            if (p_params != null)
            {
                String srcLocale = p_params.get("sourceLocale");
                String tarLocale = p_params.get("targetLocale");
                String moduleName = p_params.get("moduleName");
                if (!srcLocale.equals("-1"))
                {
                    hql += " and a.sourceLocale = :srcLocale";
                    map.put("srcLocale", srcLocale);
                }
                if (!tarLocale.equals("-1"))
                {
                    hql += " and a.targetLocale = :tarLocale";
                    map.put("tarLocale", tarLocale);
                }
                if (!"".equals(moduleName))
                {
                    hql += " and (a.sourceModule like :moduleName or a.targetModule like :moduleName)";
                    map.put("moduleName", "%" + moduleName + "%");
                }
            }

            result = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            logger.error("PersistenceException while retrieving CVS servers.",
                    pe);
            throw new ModuleMappingException(
                    ModuleMappingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES,
                    null, pe);
        }

        return result;
    }

    private String getLocale(String p)
    {
        if (p != null)
            return p.substring(p.lastIndexOf("[") + 1, p.lastIndexOf("]"));
        else
            return "";
    }

    public void removeRename(ModuleMappingRename p_mmr)
    {
        try
        {
            HibernateUtil.delete(p_mmr);
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to remove target file rename " + p_mmr.toString(),
                    e);
        }
    }

    public ModuleMappingRename getModuleMappingRename(long p_id)
    {
        ModuleMappingRename result = null;
        try
        {
            String hql = "from ModuleMappingRename c where c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            result = i.hasNext() ? (ModuleMappingRename) i.next() : null;
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when retrieving CVS module mapping "
                            + p_id, pe);
        }
        return result;
    }

    public void addModuleMappingRename(ModuleMappingRename p_mmr)
            throws RemoteException, ModuleMappingException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_mmr.setId(-1L);

            session.save(p_mmr);
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
            throw new ModuleMappingException(e.getMessage());
        }
    }

    public void updateModuleMappingRename(ModuleMappingRename p_mmr)
            throws RemoteException, ModuleMappingException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            ModuleMappingRename old = getModuleMappingRename(p_mmr.getId());
            if (old != null)
            {
                old.setSourceName(p_mmr.getSourceName());
                old.setTargetName(p_mmr.getTargetName());
                session.saveOrUpdate(old);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            logger.error(e.getMessage(), e);
        }
    }

    public ModuleMapping isModuleMappingExist(ModuleMapping p_mm)
    {
        try
        {
            String hql = "from ModuleMapping c where c.isActive='Y' and c.sourceLocale=:srcLocale and c.targetLocale=:tarLocale and "
                    + "c.sourceModule=:srcModule and c.companyId=:companyId and id!=:Id";
            Map map = new HashMap();
            map.put("srcLocale", p_mm.getSourceLocale());
            map.put("tarLocale", p_mm.getTargetLocale());
            map.put("srcModule", p_mm.getSourceModule());
            // map.put("tarModule", p_mm.getTargetModule());
            map.put("companyId", p_mm.getCompanyId());
            map.put("Id", p_mm.getId());

            Collection mms = HibernateUtil.search(hql, map);
            Iterator i = mms.iterator();
            return i.hasNext() ? (ModuleMapping) i.next() : null;
        }
        catch (Exception pe)
        {
            logger.error(
                    "Persistence Exception when verify if exist CVS module mapping "
                            + p_mm.getSourceLocale() + ","
                            + p_mm.getTargetLocale() + ","
                            + p_mm.getSourceModule() + ","
                            + p_mm.getTargetModule(), pe);
            return null;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
