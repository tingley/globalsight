package com.globalsight.ling.tm2;

import static java.lang.Math.min;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.tm.TmManagerException;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;

/**
 * Handles paging of Tu results.
 * Note that this page does not hold the current page results, as SegmentTmTu are
 * not serializable.
 */
public class TuQueryResult implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_PAGESIZE = 10;

    private List<TMidTUid> m_tuIds;
    private int m_first, m_last, m_pageSize;  // m_last exclusive
    
    /**
     * Initialize at the first page with the default page size.
     */
    public TuQueryResult(List<TMidTUid> tuIds) {
        this(tuIds, DEFAULT_PAGESIZE);
    }
    /**
     * Initialize at the first page.
     */
    public TuQueryResult(List<TMidTUid> tuIds, int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("Invalid page size (< 0)");
        }
        m_tuIds = tuIds;
        m_pageSize = pageSize;
        m_first = 0;
        m_last = min(m_first + m_pageSize, m_tuIds.size());
    }
    
    /**
     * Get the number of results.
     * @return result count
     */
    public int getCount() {
        return m_tuIds.size();
    }

    /**
     * Get the start index of the current page, inclusive.
     * @return start index
     */
    public int getFirst() {
        return m_first;
    }
    
    /**
     * Get the end index of the current page, exclusive.
     * @return end index
     */
    public int getLast() {
        return m_last;
    }
    
    /**
     * Return the results for the current page.
     */  
    public List<SegmentTmTu> getPageResults() throws TmManagerException {
        try {
            return LingServerProxy.getTmCoreManager().getSegmentsById(
                    m_tuIds.subList(m_first, m_last));
        }
        catch (Exception e) {
            throw new TmManagerException(e);
        }
    }

    /**
     * Load the next page's worth of results.  Stays put if there are no
     * results past the current page.  (Needed because the UI allows
     * double-clicks on the next button.)
     */
    public void loadNextPage() 
            throws TmManagerException {
        if (m_last == m_tuIds.size()) {
            return;
        }
        m_first += m_pageSize;
        m_last = min(m_first + m_pageSize, m_tuIds.size());
    }

    /**
     * Load the previous page's worth of results.  Stays put if the current
     * page is the first.
     */
    public void loadPreviousPage() 
            throws TmManagerException {
        if (m_first == 0) {
            return;
        }
        m_first -= m_pageSize;
        m_last = m_first + m_pageSize;
    }
}
