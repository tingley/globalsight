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
package com.globalsight.cxe.adapter.quarkframe;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Element;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.CxeMessage;

/**
* The FrameHelper handles converting Frame to and from XML.
*/
public class FrameHelper extends DesktopAppHelper
{
    private static final String PROPERTY_FILE =
        "/properties/frameAdapter.properties";

    private String m_formatName = null;

    /**
     * Constructs a FrameHelper
     *
     * @param p_workingDir -- the main working directory where the
     * conversion server looks for files
     * @param p_eventFlowXml -- the EventFlowXml
     * @param p_content -- the content (whether GXML or Native)
     */
    public FrameHelper(String p_workingDir, CxeMessage p_cxeMessage,
        org.apache.log4j.Logger p_logger)
    {
        super(p_workingDir, p_cxeMessage, p_logger);
    }

    /**************************************************/
    /*** Methods over-riding the super class methods ***/
    /**************************************************/

    /**
     * Returns the format name that this desktop app helper supports.
     *
     * @return "frame5" or "frame6" or "frame7"
     */
    public String getFormatName()
    {
        if (m_formatName == null)
        {
            setFormatName();
        }

        return m_formatName;
    }

    /**
     * Returns the event to use as the post merge event
     * so that after the merger merges the GXML to XML,
     * the XML will come to the DesktopApplicationAdapter
     *
     * @return post merge event name
     */
    protected CxeMessageType getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(
            CxeMessageType.FRAME_LOCALIZED_EVENT);
    }

    /**
     * Returns the name of the property file for this helper
     *
     * @return
     */
    protected String getPropertyFileName()
    {
        return PROPERTY_FILE;
    }

    /**
     * Looks up the known format type associated with the file profile
     * to see if it is Frame5 or Frame6 or Frame7 or Frame9. This was the
     * only way to do this as a patch without changing the
     * configuration in Active.
     */
    private void setFormatName()
    {
        Connection c = null;
        PreparedStatement query = null;
        ResultSet results = null;

        try
        {
            c = ConnectionPool.getConnection();
            StringBuffer sql = new StringBuffer ("SELECT KNOWN_FORMAT_TYPE.NAME");
            sql.append(" FROM KNOWN_FORMAT_TYPE, FILE_PROFILE ");
            sql.append(" WHERE FILE_PROFILE.ID=?");
            sql.append(" AND KNOWN_FORMAT_TYPE.ID=FILE_PROFILE.KNOWN_FORMAT_TYPE_ID");
            query = c.prepareStatement(sql.toString());
            query.setString(1, m_parser.getSourceDataSourceId());

            results = query.executeQuery();

            if (results.next())
            {
                m_formatName = FRAME9;
                
                String knownFormatName = results.getString(1);
                if (knownFormatName.equalsIgnoreCase(FRAME5))
                {
                    m_formatName = FRAME5;
                }
                else if (knownFormatName.equalsIgnoreCase(FRAME6))
                {
                    m_formatName = FRAME6;
                }
                else if (knownFormatName.equalsIgnoreCase(FRAME7))
                {
                    m_formatName = FRAME7;
                }
                else if (knownFormatName.equalsIgnoreCase(FRAME9))
                {
                    m_formatName = FRAME9;
                }
            }
            else
            {
                m_logger.error("Could not determine FrameMaker format because the file profile could not be queried. Using frame9");
                m_formatName = FRAME9;
            }
        }
        catch (Exception e)
        {
            m_formatName = FRAME9;
            m_logger.error("Could not determine FrameMaker format, using format" + m_formatName, e);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(c);
        }
    }
}

