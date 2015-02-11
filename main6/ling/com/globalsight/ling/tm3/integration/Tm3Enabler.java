package com.globalsight.ling.tm3.integration;

import java.sql.Connection;

import com.globalsight.everest.company.Company;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.util.progress.ProgressReporter;

/**
 * Class to enable TM3 for a company.
 */
public class Tm3Enabler {

    private Company company;
    private Connection conn;
    
    public Tm3Enabler(Connection conn, Company company) {
        this.conn = conn;
        this.company = company;
    }
    
    /**
     * Enable TM3 for the company.  This code must be run inside an
     * active transaction.
     * @param progress progress reporter
     */
    public void enable(ProgressReporter progress) throws LingManagerException {
        // Create TM3 storage
        progress.setMessageKey("lb_tm_enable_tm3", 
                "Enabling TM3 for company " + company.getId());
        DefaultManager.create().createStoragePool(conn, company.getId(),
                SegmentTmAttribute.inlineAttributes());
        progress.setPercentage(50);
        // Enable
        company.setTmVersion(TmVersion.TM3);
         
        progress.setPercentage(100);
        progress.setMessageKey("lb_done", "Done");
    }
}
