package com.globalsight.ling.tm3.core;

import java.util.Iterator;
import java.util.List;

/**
 * A data handle is an interface to a slice of data in the TM that
 * can be accessed efficiently in various ways.  A data handle
 * should generally load its data as needed, rather than upon 
 * creation.  However, the handle may operate on a snapshot of the
 * data, rather than a completely live view.
 * <p>
 * A data handle always refers to a set of TM3Tu objects.  
 */
public interface TM3Handle<T extends TM3Data> extends Iterable<TM3Tu<T>> {

    /**
     * Get the number of TUs represented by this handle.
     * @return number of tus referred to by this data handle
     */
    public long getCount() throws TM3Exception;
    
    public long getAllTuCount() throws TM3Exception;

    public long getTuCountByLocale(Long localeId) throws TM3Exception;

    public long getTuvCountByLocale(List<TM3Locale> localeList) throws TM3Exception;
    /**
     * Get the number of TUVs represented by this handle.
     * @return number of tuvs referred to by this data handle.
     */
    public long getTuvCount() throws TM3Exception;

	/**
     * Get an iterator to the data.
     * @return iterator to the TU referred to by this handle
     */
    public Iterator<TM3Tu<T>> iterator() throws TM3Exception;
    
    /**
     * Purge all TUs identified by this handle from its 
     * associated translation memory.
     */
    public void purge() throws TM3Exception; 
    
    /**
     * Purge all TUs identified by this handle from its 
     * associated translation memory.
     */
    public void purgeWithoutLock() throws TM3Exception; 
}
