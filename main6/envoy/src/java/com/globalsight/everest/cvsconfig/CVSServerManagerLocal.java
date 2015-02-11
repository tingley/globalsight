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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class CVSServerManagerLocal
{
    private static final Logger c_logger = Logger
            .getLogger(CostingEngineLocal.class.getName());

    public void addServer(CVSServer p_server) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_server.setId(CVSConstants.DEFAULT_AUTO_ID);
            p_server.setCompanyId(CompanyWrapper.getCurrentCompanyIdAsLong());
            session.save(p_server);

            CVSUtil.createFolder(p_server.getSandbox());

            transaction.commit();
        }
        catch (PersistenceException e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_server.getName() }, e);
        }
        catch (IOException ioe)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_server.getName() }, ioe);
        }
    }

    public void updateServer(CVSServer p_server) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSServer oldSrv = getServer(p_server.getId());
            if (oldSrv != null)
            {
                if (!oldSrv.getHostIP().equals(p_server.getHostIP())
                        || !oldSrv.getSandbox().equals(p_server.getSandbox())
                        || !oldSrv.getRepository().equals(
                                p_server.getRepository()))
                {
                    Set<CVSModule> modules = oldSrv.getModuleSet();
                    for (CVSModule m : modules)
                    {
                        if (m.isActive())
                        {
                            m.setLastCheckout(null);
                            session.saveOrUpdate(m);
                        }
                    }
                    FileUtils.deleteAllFilesSilently(CVSUtil.getBaseDocRoot()
                            .concat(oldSrv.getSandbox()));
                    CVSUtil.createFolder(p_server.getSandbox());
                }
                oldSrv.setName(p_server.getName());
                oldSrv.setProtocol(p_server.getProtocol());
                oldSrv.setHostIP(p_server.getHostIP());
                oldSrv.setHostPort(p_server.getHostPort());
                oldSrv.setRepository(p_server.getRepository());
                oldSrv.setLoginUser(p_server.getLoginUser());
                oldSrv.setLoginPwd(p_server.getLoginPwd());
                oldSrv.setSandbox(p_server.getSandbox());
                oldSrv.setCVSRootEnv(p_server.getCVSRootEnv());
            }
            session.saveOrUpdate(oldSrv);
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
        }

    }

    public void removeServer(long p_id) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSServer oldSrv = getServer(p_id);
            if (oldSrv != null)
            {
                oldSrv.setIsActive(false);
                session.saveOrUpdate(oldSrv);

                Set<CVSModule> modules = oldSrv.getModuleSet();
                for (CVSModule m : modules)
                {
                    removeModule(m.getId());
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
        }

    }

    public void removeRepository(long p_id) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSRepository oldSrv = getRepository(p_id);
            if (oldSrv != null)
            {
                oldSrv.setIsActive(false);
                session.saveOrUpdate(oldSrv);

                for (CVSModule m : oldSrv.getModuleSet())
                    removeModule(m.getId());
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
        }

    }

    public void removeModule(long p_id) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSModule oldSrv = getModule(p_id);
            if (oldSrv != null)
            {
                oldSrv.setIsActive(false);
                session.saveOrUpdate(oldSrv);
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
        }
    }

    public CVSServer getServer(long p_id)
    {
        CVSServer server = null;
        try
        {
            String hql = "from CVSServer c where c.isActive='Y' and c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            server = i.hasNext() ? (CVSServer) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_id, pe);
        }
        return server;
    }

    public CVSServer getServerByName(String p_name)
    {
        CVSServer server = null;
        try
        {
            String hql = "from CVSServer c where c.isActive='Y' and c.name = :name";
            Map map = new HashMap();
            map.put("name", p_name);
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and c.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            server = i.hasNext() ? (CVSServer) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_name, pe);
        }
        return server;
    }

    public Collection getAllServer()
    {
        Collection servers = null;
        try
        {
            String hql = "from CVSServer a where a.isActive='Y' ";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            servers = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving CVS servers.", pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }

        return servers;
    }

    public Collection getAllRepository()
    {
        Collection repositories = null;
        try
        {
            String hql = "from CVSRepository a where a.isActive='Y' ";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.server.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            repositories = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving CVS servers.", pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }

        return repositories;
    }

    public CVSRepository getRepository(long p_id)
    {
        CVSRepository repository = null;
        try
        {
            String hql = "from CVSRepository c where c.isActive='Y' and c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            repository = i.hasNext() ? (CVSRepository) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_id, pe);
        }

        return repository;
    }

    public CVSRepository getRepositoryByName(String p_name)
    {
        CVSRepository repository = null;
        try
        {
            String hql = "from CVSRepository c where c.isActive='Y' and c.name = :name";
            Map map = new HashMap();
            map.put("name", p_name);
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            repository = i.hasNext() ? (CVSRepository) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_name, pe);
        }

        return repository;
    }

    public void addRepository(CVSRepository p_repository)
            throws RemoteException, CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_repository.setId(CVSConstants.DEFAULT_AUTO_ID);
            session.save(p_repository);

            CVSUtil.createFolder(p_repository.getServer().getSandbox()
                    .concat(File.separator)
                    .concat(p_repository.getFolderName()));

            transaction.commit();
        }
        catch (PersistenceException e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }

            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_repository.getName() }, e);
        }
        catch (IOException ioe)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_repository.getName() }, ioe);
        }
    }

    public void updateRepository(CVSRepository p_repository)
            throws RemoteException, CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSRepository oldRepository = getRepository(p_repository.getId());

            if (oldRepository != null)
            {
                oldRepository.setName(p_repository.getName());
                oldRepository.setFolderName(p_repository.getFolderName());
                oldRepository.setRepository(p_repository.getRepository());
                oldRepository.setServer(p_repository.getServer());
                oldRepository.setLoginUser(p_repository.getLoginUser());
                oldRepository.setLoginPwd(p_repository.getLoginPwd());
                oldRepository.setCVSRootEnv(p_repository.getCVSRootEnv());

                session.saveOrUpdate(oldRepository);
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
        }
    }

    public Collection getAllModule()
    {
        Collection modules = null;
        try
        {
            String hql = "from CVSModule a where a.isActive='Y' ";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and a.server.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            modules = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving CVS servers.", pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }

        return modules;
    }

    public void addModule(CVSModule p_module) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_module.setId(CVSConstants.DEFAULT_AUTO_ID);

            StringBuilder folderPath = new StringBuilder();
            folderPath.append(p_module.getServer().getSandbox()).append(
                    File.separator);
            folderPath.append(p_module.getName());
            p_module.setRealPath(CVSUtil.getBaseDocRoot().concat(
                    folderPath.toString().replace("\\", "/")));
            session.save(p_module);

            CVSUtil.createFolder(folderPath.toString());

            transaction.commit();
        }
        catch (PersistenceException e)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }

            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_module.getName() }, e);
        }
        catch (IOException ioe)
        {
            try
            {
                transaction.rollback();
            }
            catch (Exception e2)
            {
            }
            throw new CVSConfigException(
                    CVSConfigException.MSG_FAILED_TO_CREATE_CVS_SERVER,
                    new String[]
                    { p_module.getName() }, ioe);
        }
    }

    public void updateModule(CVSModule p_module) throws RemoteException,
            CVSConfigException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            CVSModule oldModule = getModule(p_module.getId());

            if (oldModule != null)
            {
                oldModule.setName(p_module.getName());
                oldModule.setModulename(p_module.getModulename());
                oldModule.setBranch(p_module.getBranch());
                oldModule.setServer(p_module.getServer());

                StringBuilder folderPath = new StringBuilder();
                folderPath.append(p_module.getServer().getSandbox()).append(
                        File.separator);
                folderPath.append(p_module.getName());
                String path = folderPath.toString();
                File fPath = new File(path);
                if (!fPath.exists() || fPath.isFile())
                    CVSUtil.createFolder(path);
                oldModule.setRealPath(CVSUtil.getBaseDocRoot().concat(
                        folderPath.toString().replace("\\", "/")));
                oldModule.setLastCheckout(p_module.getLastCheckout());

                session.saveOrUpdate(oldModule);
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
        }
    }

    public CVSModule getModule(long p_id)
    {
        CVSModule module = null;
        try
        {
            String hql = "from CVSModule c where c.isActive='Y' and c.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            module = i.hasNext() ? (CVSModule) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_id, pe);
        }

        return module;
    }

    public CVSModule getModuleByName(String p_name)
    {
        CVSModule module = null;
        try
        {
            String hql = "from CVSModule c where c.isActive='Y' and c.name = :name";
            Map map = new HashMap();
            map.put("name", p_name);
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and c.server.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            module = i.hasNext() ? (CVSModule) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS server "
                    + p_name, pe);
        }

        return module;
    }

    public CVSModule getModuleByRepository(long p_repository_id)
    {
        CVSModule module = null;
        try
        {
            String hql = "from CVSModule c where c.isActive='Y' and c.cvsRepository = :id";
            CVSRepository cr = getRepository(p_repository_id);
            Map map = new HashMap();
            map.put("id", cr);
            Collection repositories = HibernateUtil.search(hql, map);
            Iterator i = repositories.iterator();
            module = i.hasNext() ? (CVSModule) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving CVS module "
                    + p_repository_id, pe);
        }

        return module;
    }

    public boolean isServerExist(String p_name)
    {
        return getServerByName(p_name) != null;
    }

    public boolean isRepositoryExist(String p_name)
    {
        return getRepositoryByName(p_name) != null;
    }

    public boolean isModuleExist(String p_name)
    {
        return getModuleByName(p_name) != null;
    }

    public Vector<String> getAllServerSandbox()
    {
        Vector<String> sandBoxs = new Vector<String>();
        ArrayList<CVSServer> servers = (ArrayList<CVSServer>) getAllServer();
        for (CVSServer s : servers)
        {
            sandBoxs.add(s.getSandbox());
        }
        return sandBoxs;
    }
}
