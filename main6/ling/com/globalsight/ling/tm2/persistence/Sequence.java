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
package com.globalsight.ling.tm2.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * <code>Sequence</Code> class is used to keep the unique of id.
 * <p>
 * For example:<br>
 * Insert 5 entities into PAGE_TM, you can use this class like following.
 * <p>
 * 
 * long id = Sequence.allocateIds(Sequence.PAGE_TM_SEQ, 5); <br>
 * then you can use id, id+1, id+2, id+3, id+4 as id value of there 5 entities.
 */
public class Sequence
{
    private static final Logger c_logger = Logger
            .getLogger(Sequence.class);

    private final String tableName;
    private long value;

    private final long DEFAULT_VALUE = 1000;
    private final String LOAD_VALUE_SQL = "select max(id) from ";

    public static final String PAGE_TM_SEQ = "PAGE_TM_SEQ";

    public static final String PAGE_TM_TU_T_SEQ = "PAGE_TM_TU_T_SEQ";
    public static final String PAGE_TM_TUV_T_SEQ = "PAGE_TM_TUV_T_SEQ";
    public static final String PAGE_TM_TU_L_SEQ = "PAGE_TM_TU_L_SEQ";
    public static final String PAGE_TM_TUV_L_SEQ = "PAGE_TM_TUV_L_SEQ";
    public static final String SEGMENT_TM_TU_T_SEQ = "SEGMENT_TM_TU_T_SEQ";
    public static final String SEGMENT_TM_TUV_T_SEQ = "SEGMENT_TM_TUV_T_SEQ";
    public static final String SEGMENT_TM_TU_L_SEQ = "SEGMENT_TM_TU_L_SEQ";
    public static final String SEGMENT_TM_TUV_L_SEQ = "SEGMENT_TM_TUV_L_SEQ";

    public static final String SRC_T_SEQ = "IP_TM_SRC_T_SEQ";
    public static final String SRC_L_SEQ = "IP_TM_SRC_L_SEQ";
    public static final String TRG_T_SEQ = "IP_TM_TRG_T_SEQ";
    public static final String TRG_L_SEQ = "IP_TM_TRG_L_SEQ";

    private static Map<String, Sequence> SEQUENCE;
    static
    {
        SEQUENCE = new HashMap<String, Sequence>();
        SEQUENCE.put(SRC_T_SEQ, new Sequence("IP_TM_SRC_T"));
        SEQUENCE.put(SRC_L_SEQ, new Sequence("IP_TM_SRC_L"));
        SEQUENCE.put(TRG_T_SEQ, new Sequence("IP_TM_TRG_T"));
        SEQUENCE.put(TRG_L_SEQ, new Sequence("IP_TM_TRG_L"));

        SEQUENCE.put(PAGE_TM_SEQ, new Sequence("PAGE_TM"));
        SEQUENCE.put(PAGE_TM_TU_T_SEQ, new Sequence("PAGE_TM_TU_T"));
        SEQUENCE.put(PAGE_TM_TU_L_SEQ, new Sequence("PAGE_TM_TU_L"));
        SEQUENCE.put(PAGE_TM_TUV_T_SEQ, new Sequence("PAGE_TM_TUV_T"));
        SEQUENCE.put(PAGE_TM_TUV_L_SEQ, new Sequence("PAGE_TM_TUV_L"));
        SEQUENCE.put(SEGMENT_TM_TU_T_SEQ, new Sequence("PROJECT_TM_TU_T"));
        SEQUENCE.put(SEGMENT_TM_TU_L_SEQ, new Sequence("PROJECT_TM_TU_L"));

        SEQUENCE.put(SEGMENT_TM_TUV_T_SEQ, new Sequence("PROJECT_TM_TUV_T"));
        SEQUENCE.put(SEGMENT_TM_TUV_L_SEQ, new Sequence("PROJECT_TM_TUV_L"));
    }

    private Sequence(String tableName)
    {
        super();
        this.tableName = tableName;
        try
        {
            loadValue();
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }

    private void loadValue() throws Exception
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.prepareStatement(LOAD_VALUE_SQL + this.tableName);
            rs = stmt.executeQuery();

            long n = DEFAULT_VALUE;
            while (rs.next())
            {
                n = rs.getLong(1);
            }
            setValue(n + 1);
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            DbUtil.returnConnection(conn);
        }
    }

    private void setValue(long value)
    {
        if (value == 1)
        {
            value = DEFAULT_VALUE;
        }

        this.value = value;
    }

    private void addValue(long value)
    {
        this.value += value;
    }

    /**
     * Gets some unique ids.
     * 
     * @param p_seqName
     * @param p_allocationUnit
     * @return
     */
    static public synchronized long allocateIds(String p_seqName,
            long p_allocationUnit)
    {
        Sequence sequence = SEQUENCE.get(p_seqName);
        long seqId = sequence.value;
        sequence.addValue(p_allocationUnit);
        return seqId;
    }
    
    /**
     * Load values.
     */
    public static void init()
    {
        // do nothing, just load the class.
    }
}
