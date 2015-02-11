package com.globalsight.ling.tm3.core;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import java.sql.Connection;

/**
 * Top-level interface for interacting with TM3.
 * 
 */
public interface TM3Manager {
    
    /**
     * Create a new shared pool for storing shared TMs.
     * <p>  
     * <b>Note that this method is non-transactional, because table creation 
     * is involved.</b>
     * @param conn Active JDBC connection.  Unlike other TM3 methods, a connection
     *          is used here, because only table creation is performed.
     * @param id ID of this pool.  Note that these IDs must be externally managed.
     * @return true if the pool was created.  False if it already existed.
     * @throws TM3Exception if an error occurred
     */
    public boolean createStoragePool(Connection conn, long id,
            Set<TM3Attribute> inlineAttributes) throws TM3Exception;
    
    /**
     * Permanentaly remove a shared pool and any TMs stored in it.
     * <p>  
     * <b>Note that this method is non-transactional, because table removal 
     * is involved.</b>
     * @param conn Active JDBC connection.  Unlike other TM3 methods, a connection
     *          is used here, because only table deletion is performed.
     * @param id ID of this pool
     * @return true if the pool was removed.  False if it did not exist.
     * @throws TM3Exception if an error occurred
     */
    public boolean removeStoragePool(Connection conn, long id) throws TM3Exception;
    
    /**
     * Get a list of all TMs in the database.  This method should be used with
     * care, since it will attach a single data factory to every instance. 
     * @param session
     * @param factory
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> List<TM3Tm<T>> getAllTms(Session session, 
                TM3DataFactory<T> factory) throws TM3Exception;
    
    /**
     * Load a TM from the database. 
     * @param session Hibernate Session to be used for all database interaction
     * @param factory factory object to deserialize TUV data 
     * @param id TM id
     * @return TM, or null if no TM with that id exists
     * @throws TM3Exception If an error occurs while the TM is being loaded.
     */    
    public <T extends TM3Data> TM3Tm<T> getTm(Session session, 
                TM3DataFactory<T> factory, long id) throws TM3Exception;

    /**
     * Load a bilingual TM from the database.  This is equivalent to casting
     * the result of getTm(), except that null is returned if the TM requested
     * is not bilingual.
     *  
     * @param session Hibernate Session to be used for all database interaction
     * @param factory factory object to deserialize TUV data 
     * @param id TM id
     * @return TM, or null if no TM with that id exists (or if the TM is not
     *          bilingual)
     * @throws TM3Exception If an error occurs while the TM is being loaded.
     */
    public <T extends TM3Data> TM3BilingualTm<T> getBilingualTm(Session session, 
                TM3DataFactory<T> factory, long id) throws TM3Exception;
    
    
    /**
     * Create a new bilingual TM with its own dedicated storage space.
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
                throws TM3Exception;

    /**
     * Create a new multilingual TM with its own dedicate storage 
     * space.
     * 
     * @param session
     * @param factory
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> TM3Tm<T> createMultilingualTm(Session session,
            TM3DataFactory<T> factory, Set<TM3Attribute> inlineAttributes)
            throws TM3Exception;

    /**
     * Create a new multilingual TM, using shared storage
     * identified by <tt>sharedStorageId</tt>.
     * 
     * @param session
     * @param factory
     * @param sharedStorageId
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> TM3SharedTm<T> createMultilingualSharedTm(
            Session session, TM3DataFactory<T> factory,
            Set<TM3Attribute> inlineAttributes, long sharedStorageId) 
            throws TM3Exception;

    
    /**
     * Remove a tm and all associated metadata, including segment (TU/TUV)
     * data, attributes, and event history.
     * 
     * @param session
     * @param tm
     * @throws TM3Exception
     */
    public <T extends TM3Data> void removeTm(Session session, TM3Tm<T> tm) 
            throws TM3Exception;

}
