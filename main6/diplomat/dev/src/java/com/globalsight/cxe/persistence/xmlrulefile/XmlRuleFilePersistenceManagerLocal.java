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
package com.globalsight.cxe.persistence.xmlrulefile;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implements the service interface for performing CRUD operations for
 * XmlRuleFiles *
 */
public class XmlRuleFilePersistenceManagerLocal implements
        XmlRuleFilePersistenceManager
{
    /**
     * Default Constructor TODO: remove throws clause
     */
    public XmlRuleFilePersistenceManagerLocal()
            throws XmlRuleFileEntityException, RemoteException
    {
        super();
    }

    /**
     * Creates a new XmlRuleFile object in the data store
     * 
     * @return the newly created object
     */
    public XmlRuleFile createXmlRuleFile(XmlRuleFile p_xmlRuleFile)
            throws XmlRuleFileEntityException, RemoteException
    {
        try
        {
            p_xmlRuleFile.setCompanyId(Long.parseLong(CompanyThreadLocal
                    .getInstance().getValue()));
            HibernateUtil.save((XmlRuleFileImpl) p_xmlRuleFile);
            return readXmlRuleFile(p_xmlRuleFile.getId());
        }
        catch (Exception e)
        {
            throw new XmlRuleFileEntityException(e);
        }
    }

    /**
     * Reads the XmlRuleFile object from the datastore
     * 
     * @return XmlRuleFile with the given id
     */
    public XmlRuleFile readXmlRuleFile(long p_id)
            throws XmlRuleFileEntityException, RemoteException
    {
        try
        {
            return (XmlRuleFileImpl) HibernateUtil.get(XmlRuleFileImpl.class,
                    p_id);
        }
        catch (Exception e)
        {
            throw new XmlRuleFileEntityException(e);
        }
    }

    /**
     * Deletes an XML Rule File from the datastore
     */
    public void deleteXmlRuleFile(XmlRuleFile p_xmlRuleFile)
            throws XmlRuleFileEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete((XmlRuleFileImpl) p_xmlRuleFile);
        }
        catch (Exception e)
        {
            throw new XmlRuleFileEntityException(e);
        }
    }

    /**
     * Update the XMLRuleFile object in the datastore
     * 
     * @return the updated XmlRuleFile
     */
    public XmlRuleFile updateXmlRuleFile(XmlRuleFile p_xmlRuleFile)
            throws XmlRuleFileEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update((XmlRuleFileImpl) p_xmlRuleFile);
        }
        catch (Exception e)
        {
            throw new XmlRuleFileEntityException(e);
        }
        return p_xmlRuleFile;
    }

    /**
     * Get a list of all existing XMLRuleFile objects in the datastore; make
     * them editable.
     * 
     * @return a vector of the XmlRuleFile objects
     */
    public Collection getAllXmlRuleFiles() throws XmlRuleFileEntityException,
            RemoteException
    {
        try
        {
            String hql = "from XmlRuleFileImpl x";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " where x.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            hql += " order by x.name";

            return HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new XmlRuleFileEntityException(e);
        }
    }
}
