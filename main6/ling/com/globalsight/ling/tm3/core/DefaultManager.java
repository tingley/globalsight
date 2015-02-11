package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Top-level interface for interacting with TM3.
 * 
 */
public class DefaultManager implements TM3Manager {
    
    private DefaultManager() {
    }
        
    public static TM3Manager create() {
        return new DefaultManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TM3Data> List<TM3Tm<T>> getAllTms(Session session, 
            TM3DataFactory<T> factory) throws TM3Exception {
        try {
            List<? extends TM3Tm> tms = session.createCriteria(BaseTm.class).list();
            for (TM3Tm t : tms) {
                BaseTm<T> tm = (BaseTm<T>)t;
                injectTm(session, tm, factory);
            }
            return (List<TM3Tm<T>>) tms;
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }

    
    @SuppressWarnings("unchecked")
    public <T extends TM3Data> TM3Tm<T> getTm(Session session, 
            TM3DataFactory<T> factory, long id) throws TM3Exception {
        try {
            BaseTm<T> tm = (BaseTm<T>)session.get(BaseTm.class, id);
            if (tm != null) {
                injectTm(session, tm, factory);
            }
            return tm;
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TM3Data> TM3BilingualTm<T> getBilingualTm(
            Session session, TM3DataFactory<T> factory, long id) 
            throws TM3Exception {
        TM3Tm<T> tm = getTm(session, factory, id);
        if (tm == null) {
            return null;
        }
        if (!(tm instanceof TM3BilingualTm)) {
            return null;
        }
        return (TM3BilingualTm<T>)tm;
    }
    
    
    /**
     * Create a new bilingual tm.
     * 
     * @param session
     * @param factory
     * @param srcLocale
     * @param tgtLocale
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> TM3BilingualTm<T> createBilingualTm(
            Session session, TM3DataFactory<T> factory, 
            Set<TM3Attribute> inlineAttributes,
            TM3Locale srcLocale, TM3Locale tgtLocale) 
            throws TM3Exception {
        
        if (srcLocale == null) {
            throw new IllegalArgumentException("Invalid source locale");
        }
        if (tgtLocale == null) {
            throw new IllegalArgumentException("Invalid target locale");
        }
        
        try {
            return init(new BilingualTm<T>(factory, srcLocale, tgtLocale),
                        session, inlineAttributes);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }
    
    public <T extends TM3Data> TM3Tm<T> createMultilingualTm(Session session,
            TM3DataFactory<T> factory, Set<TM3Attribute> inlineAttributes)
            throws TM3Exception {
        
        try {
            return init(new MultilingualTm<T>(factory), session,
                        inlineAttributes);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }

    public <T extends TM3Data> TM3SharedTm<T> createMultilingualSharedTm(
            Session session, TM3DataFactory<T> factory,
            Set<TM3Attribute> inlineAttributes, long sharedStorageId) 
            throws TM3Exception {
    
        try {
            return init(new MultilingualSharedTm<T>(sharedStorageId, factory),
                        session, inlineAttributes);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }
    
    /**
     * Remove a tm.
     * @param session
     * @param tm
     * @throws TM3Exception
     */
    public <T extends TM3Data> void removeTm(Session session, TM3Tm<T> tm) 
                        throws TM3Exception {
        try {
            StorageInfo<T> storage = ((BaseTm<T>)tm).getStorageInfo();
            storage.destroy();
            session.delete(tm);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    private <T extends TM3Data, K extends BaseTm<T>> K init(K tm, 
            Session session, Set<TM3Attribute> inlineAttributes)
            throws SQLException, HibernateException {
        tm.setManager(this);
        tm.setSession(session);
        session.persist(tm);
        for (TM3Attribute attr : inlineAttributes) {
            attr.setTm(tm);
            tm.addAttribute(attr);
        }
        session.flush(); // Sync the object to get an ID
        tm.getStorageInfo().create();
        return tm;
    }
    
    private <T extends TM3Data, V extends BaseTm<T>> V injectTm(
                    Session session, V tm, TM3DataFactory<T> factory) {
        tm.setDataFactory(factory);
        tm.setSession(session);
        tm.setManager(this);
        return tm;
    }

    @Override
    public boolean createStoragePool(Connection conn, long id,
            Set<TM3Attribute> inlineAttributes) throws TM3Exception {
        try {
            return new SharedStorageTables(conn, id).create(inlineAttributes);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public boolean removeStoragePool(Connection conn, long id) throws TM3Exception {
        try {
            return new SharedStorageTables(conn, id).destroy();
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

}
