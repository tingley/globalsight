package com.globalsight.ling.inprogresstm.persistence;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.ClassUtil;

public class IndexPersistenceTest
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
            IndexPersistence indexPersistence = new IndexPersistence(null);
            
            Set<String> tokenStrings = new HashSet();
            tokenStrings.add("Junit");
            tokenStrings.add("Tests");
            
            Set<Long> jobIds = new HashSet();
            jobIds.add(new Long(1000));
            
            Set<Long> tmIds = new HashSet();
            String sql = (String) ClassUtil.testMethod(indexPersistence,
                    "getQuerySQL", tokenStrings, jobIds, tmIds);
            String expected1 = "SELECT TOKEN, SRC_ID, JOB_ID, REPETITION, TOTAL_TOKEN_COUNT FROM IP_TM_INDEX WHERE TOKEN IN ('Junit', 'Tests') AND LOCALE_ID = ?  AND JOB_ID IN (1000)";
            String expected2 = "SELECT TOKEN, SRC_ID, JOB_ID, REPETITION, TOTAL_TOKEN_COUNT FROM IP_TM_INDEX WHERE TOKEN IN ('Tests', 'Junit') AND LOCALE_ID = ?  AND JOB_ID IN (1000)";
            boolean result = false;
            if (expected1.equals(sql) || expected2.equals(sql)) {
                result = true;
            }
            assertTrue(result);
            
            tokenStrings.remove("Tests");
            tmIds.add(new Long(2000));
            String sql2 = (String) ClassUtil.testMethod(indexPersistence,
                    "getQuerySQL", tokenStrings, jobIds, tmIds);
            String expected3 = "SELECT TOKEN, SRC_ID, JOB_ID, REPETITION, TOTAL_TOKEN_COUNT FROM IP_TM_INDEX WHERE TOKEN IN ('Junit') AND LOCALE_ID = ?  AND JOB_ID IN (1000) UNION SELECT TOKEN, SRC_ID, JOB_ID, REPETITION, TOTAL_TOKEN_COUNT FROM IP_TM_INDEX WHERE TOKEN IN ('Junit') AND LOCALE_ID = ? AND POPULATION_TM_ID IN (2000)";
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
