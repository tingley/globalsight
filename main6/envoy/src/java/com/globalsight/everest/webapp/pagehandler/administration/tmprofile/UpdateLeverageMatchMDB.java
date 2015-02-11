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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * For GBS-613, there are some performance problem while updating tm profile,
 * because updating leverage match cost some time. So we split the updating
 * leverage match, and use jms to do it.
 */
public class UpdateLeverageMatchMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;
    private static Logger CATEGORY = Logger
            .getLogger(UpdateLeverageMatchMDB.class.getName());
    private static final String SQL = "update leverage_match set "
            + "project_tm_index = ? where tm_id = ? and tm_profile_id=?";

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
            Vector<LeverageProjectTM> tms = (Vector<LeverageProjectTM>) msg
                    .get(0);
            Long tmprofileId = Long.parseLong((String) msg.get(1));

            Connection connection = null;
            PreparedStatement ps = null;
            try
            {
                connection = DbUtil.getConnection();
                ps = connection.prepareStatement(SQL);

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
                
                if (connection != null)
                {
                    try
                    {
                        DbUtil.returnConnection(connection);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error("Can not close the connection");
                    }
                }
            }
        }
        catch (JMSException e)
        {
            CATEGORY.error(e);
        }
    }
}
