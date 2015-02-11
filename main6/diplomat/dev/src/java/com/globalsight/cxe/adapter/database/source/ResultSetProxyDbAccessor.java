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
package com.globalsight.cxe.adapter.database.source;

import com.globalsight.diplomat.util.Logger;

import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.diplomat.util.database.DbAccessException;
import com.globalsight.diplomat.util.database.RecordProfile;
import com.globalsight.diplomat.util.database.SqlParameterSubstituter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * Concrete extension of the DbAccessor class.
 * <p>
 * Adds functionality to execute acquisition sql for a given record profile
 * with supplied parameters substituted into the sql string.  The result set,
 * if any, is returned in the form of a ResultSetProxy.
 */
public class ResultSetProxyDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE VARIABLES
    //
    private static Logger s_logger = Logger.getLogger();

    //
    // PUBLIC STATIC METHODS
    //
    /**
     * Read the result set corresponding to the acquisition SQL from the 
     * given RecordProfile, with parameters all substituted.  Return a
     * proxy representing the result set.
     *
     * @param p_rp the record profile to use.
     * @param p_params a string of delimited parameters to substitute.
     *
     * @return the result set proxy.
     *
     * @throws DbAccessException if any database problem occurs.
     */
    public static ResultSetProxy readResultSetProxy(RecordProfile p_rp, String p_params)
    throws DbAccessException
    {
        ResultSetProxy rsp = null;
        try
        {
            String sql = constructSql(p_rp.getAcquisitionSql(), p_params);
            s_logger.println(Logger.DEBUG_B, "Acquisition SQL:\n" + sql);

            Connection conn = getConnection(p_rp.getAcquisitionConnectionId());
            PreparedStatement st = conn.prepareStatement(sql);
            rsp = new ResultSetProxy(sql, st.executeQuery());
            s_logger.println(Logger.DEBUG_D, "ResultSetProxyDbAccessor: got " + rsp);
            st.close();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to read acquisition sql for record profile, " +
                                        ", id=" + p_rp.getId(), e);

        }
        return rsp;
    }

    //
    // PRIVATE STATIC SUPPORT METHODS
    //
    /* Construct executable SQL by merging the parameters into the template */
    private static String constructSql(String p_template, String p_params)
    {
        return SqlParameterSubstituter.substitute(p_template, p_params);
    }
}

