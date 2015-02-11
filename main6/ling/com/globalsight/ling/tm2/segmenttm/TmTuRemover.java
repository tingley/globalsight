package com.globalsight.ling.tm2.segmenttm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.sql.Connection;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TuvSorter;
import com.globalsight.ling.tm2.TuvSorter.TuvGroup;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;

/**
 * This is code that initially came from TmCoreManagerLocal and now
 * is called from Tm2SegmentTmInfo.
 */
public class TmTuRemover {

    public void deleteTus(Connection pConnection, 
                Collection<SegmentTmTu> p_tus) throws Exception {
        
        SegmentTmPersistence stPersistence
        = new SegmentTmPersistence(pConnection);

        // get lock on Gold Tm tables
        stPersistence.lockSegmentTmTables();
        
        try {
            // sort Tus by localizability
            List<SegmentTmTu> trTus = new ArrayList<SegmentTmTu>();
            List<SegmentTmTu> loTus = new ArrayList<SegmentTmTu>();
            for (SegmentTmTu tu : p_tus) {
                if(tu.isTranslatable())
                {
                    trTus.add(tu);
                }
                else
                {
                    loTus.add(tu);
                }
            }
        
            // delete translatable tuvs
            doDeleteSegmentTmTuvs(
                stPersistence.retrieveTuvsByTuId(trTus, true),
                stPersistence);
            // delete localizable tuvs
            doDeleteSegmentTmTuvs(
                stPersistence.retrieveTuvsByTuId(loTus, false),
                stPersistence);
            
            // delete translatable tus
            stPersistence.removeTus(trTus, true);
            // delete localizable tus
            stPersistence.removeTus(loTus, false);
        }
        finally {
            DbUtil.unlockTables(pConnection);
        }
    }
    
    public void deleteTuvs(Connection pConnection, 
                Collection<SegmentTmTuv> p_tuvs) throws Exception {     
        SegmentTmPersistence stPersistence
            = new SegmentTmPersistence(pConnection);
        // get lock on Gold Tm tables
        stPersistence.lockSegmentTmTables();
        try {          
            doDeleteSegmentTmTuvs(p_tuvs, stPersistence);
        }
        finally {
            DbUtil.unlockTables(pConnection);
        }
    }
    
    private void doDeleteSegmentTmTuvs(
            Collection<SegmentTmTuv> p_tuvs, SegmentTmPersistence p_stPersistence)
            throws Exception
    {
        TuvSorter tuvSorter = new TuvSorter(p_tuvs);
        for(Iterator it = tuvSorter.iterator(); it.hasNext();)
        {
            TuvGroup tuvGroup = (TuvGroup)it.next();

            // remove TUVs
            p_stPersistence.removeTuvs(tuvGroup.getTuvs(),
                tuvGroup.isTranslatable(), tuvGroup.getLocale(),
                tuvGroup.getTmId());
        }
    }


}
