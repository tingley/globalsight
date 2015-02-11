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

package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * @deprecated
 * @since 8.3 
 * When save TM profile and reference TMs order is changed, system will 
 * synchronize "project_tm_index" of leverage match DB store, there is 
 * performance problem when there are large data in leverage match DB store. 
 * So change to abandon the "synchronize" execution.
 */
public class UpdateLeverageMatchMDB extends GenericQueueMDB implements
        TuvQueryConstants
{
    private static final long serialVersionUID = 1L;
    private static Logger CATEGORY = Logger
            .getLogger(UpdateLeverageMatchMDB.class.getName());
    
    private static final String SQL = "update "
            + LM_TABLE_PLACEHOLDER + " "
            + "set project_tm_index = ? where tm_id = ? and tm_profile_id=?";

    public UpdateLeverageMatchMDB()
    {
        super(CATEGORY);
    }

    @Override
    public void onMessage(Message p_message)
    {
        try
        {
            if (p_message.getJMSRedelivered())
            {
                CATEGORY.warn("Ignoring duplicate JMS message.");
                return;
            }

            ArrayList<?> msg = (ArrayList<?>) ((ObjectMessage) p_message)
                    .getObject();
            @SuppressWarnings("unchecked")
            Vector<LeverageProjectTM> tms = (Vector<LeverageProjectTM>) msg.get(0);
            Long tmprofileId = Long.parseLong((String) msg.get(1));
            String currentCompanyId = (String) msg.get(2);

            Connection connection = null;
            PreparedStatement ps = null;
            try
            {
                connection = DbUtil.getConnection();
                String lmTableName = SegmentTuTuvCacheManager
                        .getLeverageMatchTableName(currentCompanyId);
                String sql = SQL.replace(LM_TABLE_PLACEHOLDER, lmTableName);
                ps = connection.prepareStatement(sql);

                for (LeverageProjectTM leverageProjectTM : tms)
                {
                    ps.setInt(1, leverageProjectTM.getProjectTmIndex());
                    ps.setLong(2, leverageProjectTM.getProjectTmId());
                    ps.setLong(3, tmprofileId);
                    ps.addBatch();
                }

                ps.executeBatch();
            }
            catch (Exception ex)
            {
                CATEGORY.error("database error", ex);
                throw new LingManagerException(ex);
            }
            finally
            {
                DbUtil.silentClose(ps);
                DbUtil.silentReturnConnection(connection);
            }
        }
        catch (JMSException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }
}
