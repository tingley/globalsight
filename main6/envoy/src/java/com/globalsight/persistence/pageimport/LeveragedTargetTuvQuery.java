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
package com.globalsight.persistence.pageimport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.tuv.TuvImpl;


public class LeveragedTargetTuvQuery
{
    private PreparedStatement m_ps;
    private static final String LEVERAGED_TARGET_TUV_SQL = 
                            "select tuv.* from " +
                            "translation_unit_variant tuv , leverage_match lm " +
                            "where tuv.id = lm.leveraged_target_tuv_id " +
                            " and lm.source_page_id = ? " +
                            " and lm.target_locale_id = ? " ;


    public LeveragedTargetTuvQuery()
    {
    }
    public List getLeveragedTargetTuvs(long p_sourcePageId,
                                       long p_targetLocaleId)
        throws PersistenceException
    {
        Connection connection = null;
        ResultSet rs = null;
        List targetTuvs = null;
        try
        {
            connection = PersistenceService.getInstance().getConnection();
            m_ps = connection.prepareStatement(LEVERAGED_TARGET_TUV_SQL);
            m_ps.setLong(1,p_sourcePageId);
            m_ps.setLong(2,p_targetLocaleId);
            rs = m_ps.executeQuery();
            targetTuvs = processResultSet(rs);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            if (rs != null)
            {
                 try {rs.close();}
                 catch(Exception e){}
            }

            if (m_ps != null)
            {
                 try {m_ps.close();}
                 catch(Exception e){}
            }

            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {

            }
        }

        return targetTuvs;
    }
    private List processResultSet(ResultSet p_rs)
        throws Exception
    {
        List targetTuvs = new ArrayList();
        ResultSet rs = p_rs;
        while (rs.next())
        {
            TuvImpl tuv = new TuvImpl();
            long id = rs.getLong(1);
            tuv.setId(id);
            String segment = rs.getString(7);
            if (segment == null || segment.length() == 0)
            {
                int clobColumn = 6;
                String clobSegment = DbAccessor.readClob(rs,clobColumn);
                tuv.setGxml(clobSegment);
            }
            else 
            {
                tuv.setGxml(segment);
            }
            targetTuvs.add(tuv);
        }
        return targetTuvs;
    }
}
