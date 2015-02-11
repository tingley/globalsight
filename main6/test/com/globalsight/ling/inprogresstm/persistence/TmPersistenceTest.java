package com.globalsight.ling.inprogresstm.persistence;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.ClassUtil;

public class TmPersistenceTest
{
    /**
     * Method: private String getQuerySQL(...)
     * For GBS-1740
     */
    @Test
    public void testGetQuerySQL() throws Exception
    {
        Connection conn = null;
        try
        {
//            conn = DbUtil.getConnection();
            TmPersistence tmPersistence = new TmPersistence(null);
            Set<Long> jobIds = new HashSet();
            jobIds.add(new Long(1000));
            jobIds.add(new Long(1001));
            
            Set<Long> tmIds = new HashSet();
            String sql = (String) ClassUtil.testMethod(tmPersistence,
                    "getQuerySQL", jobIds, tmIds);
            String expected1 = "SELECT SRC.ID, SRC.JOB_ID, SRC.TYPE, SRC.SEGMENT_STRING, SRC.SEGMENT_CLOB, TRG.ID, TRG.SEGMENT_STRING, TRG.SEGMENT_CLOB, TRG.TU_ID FROM IP_TM_TRG_L TRG, IP_TM_SRC_L SRC  WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.LOCALE_ID = ? AND SRC.EXACT_MATCH_KEY = ? AND SRC.JOB_ID IN (1001, 1000)";
            String expected2 = "SELECT SRC.ID, SRC.JOB_ID, SRC.TYPE, SRC.SEGMENT_STRING, SRC.SEGMENT_CLOB, TRG.ID, TRG.SEGMENT_STRING, TRG.SEGMENT_CLOB, TRG.TU_ID FROM IP_TM_TRG_L TRG, IP_TM_SRC_L SRC  WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.LOCALE_ID = ? AND SRC.EXACT_MATCH_KEY = ? AND SRC.JOB_ID IN (1000, 1001)";
            boolean result = false;
            if (expected1.equals(sql) || expected2.equals(sql)) {
                result = true;
            }
            assertTrue(result);
            
            jobIds.remove(new Long(1001));
            tmIds.add(new Long(2000));
            String sql2 = (String) ClassUtil.testMethod(tmPersistence,
                    "getQuerySQL", jobIds, tmIds);
            String expected3 = "SELECT SRC.ID, SRC.JOB_ID, SRC.TYPE, SRC.SEGMENT_STRING, SRC.SEGMENT_CLOB, TRG.ID, TRG.SEGMENT_STRING, TRG.SEGMENT_CLOB, TRG.TU_ID FROM IP_TM_TRG_L TRG, IP_TM_SRC_L SRC  WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.LOCALE_ID = ? AND SRC.EXACT_MATCH_KEY = ? AND SRC.JOB_ID IN (1000) UNION SELECT SRC.ID, SRC.JOB_ID, SRC.TYPE, SRC.SEGMENT_STRING, SRC.SEGMENT_CLOB, TRG.ID, TRG.SEGMENT_STRING, TRG.SEGMENT_CLOB, TRG.TU_ID FROM IP_TM_TRG_L TRG, IP_TM_SRC_L SRC  WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.LOCALE_ID = ? AND SRC.EXACT_MATCH_KEY = ? AND SRC.POPULATION_TM_ID IN (2000)";
            assertEquals("Fail when tmIds parameter is not empty.", expected3, sql2);
        }
        finally
        {
            if(conn != null)
            {
                DbUtil.returnConnection(conn);
            }
        }
    }

}
