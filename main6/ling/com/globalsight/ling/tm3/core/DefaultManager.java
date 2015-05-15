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
package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Top-level interface for interacting with TM3.
 * 
 */
public class DefaultManager implements TM3Manager
{

    private DefaultManager()
    {
    }

    public static TM3Manager create()
    {
        return new DefaultManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TM3Data> List<TM3Tm<T>> getAllTms(
            TM3DataFactory<T> factory) throws TM3Exception
    {
        Session session = HibernateUtil.getSession();
        try
        {
            List<? extends TM3Tm> tms = session.createCriteria(BaseTm.class)
                    .list();
            for (TM3Tm t : tms)
            {
                BaseTm<T> tm = (BaseTm<T>) t;
                injectTm(tm, factory);
            }
            return (List<TM3Tm<T>>) tms;
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TM3Data> TM3Tm<T> getTm(TM3DataFactory<T> factory, long id)
            throws TM3Exception
    {
        try
        {
            BaseTm<T> tm = (BaseTm<T>) HibernateUtil.get(BaseTm.class, id,
                    false);
            if (tm != null)
            {
                injectTm(tm, factory);
            }
            return tm;
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    public <T extends TM3Data> TM3SharedTm<T> createMultilingualSharedTm(
            TM3DataFactory<T> factory, Set<TM3Attribute> inlineAttributes,
            long sharedStorageId) throws TM3Exception
    {

        try
        {
            return init(new MultilingualSharedTm<T>(sharedStorageId, factory),
                    inlineAttributes);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    /**
     * Remove a tm.
     * 
     * @param tm
     * @throws TM3Exception
     */
    public <T extends TM3Data> void removeTm(TM3Tm<T> tm) throws TM3Exception
    {
        try
        {
            StorageInfo<T> storage = ((BaseTm<T>) tm).getStorageInfo();
            storage.destroy();
            HibernateUtil.delete(tm);
        }
        catch (Exception e)
        {
            throw new TM3Exception(e);
        }
    }

    private <T extends TM3Data, K extends BaseTm<T>> K init(K tm,
            Set<TM3Attribute> inlineAttributes) throws SQLException,
            HibernateException
    {
        tm.setManager(this);
        try
        {
            HibernateUtil.save(tm);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        for (TM3Attribute attr : inlineAttributes)
        {
            attr.setTm(tm);
            tm.addAttribute(attr);
        }
        tm.getStorageInfo().create();
        return tm;
    }

    private <T extends TM3Data, V extends BaseTm<T>> V injectTm(V tm,
            TM3DataFactory<T> factory)
    {
        tm.setDataFactory(factory);
        tm.setManager(this);
        return tm;
    }

    @Override
    public boolean createStoragePool(Connection conn, long id,
            Set<TM3Attribute> inlineAttributes) throws TM3Exception
    {
        try
        {
            return new SharedStorageTables(conn, id).create(inlineAttributes);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public boolean removeStoragePool(Connection conn, long id)
            throws TM3Exception
    {
        try
        {
            return new SharedStorageTables(conn, id).destroy();
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

}
