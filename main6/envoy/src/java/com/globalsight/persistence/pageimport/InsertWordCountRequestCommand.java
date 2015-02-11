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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

/**
 * @deprecated
 */
public class InsertWordCountRequestCommand extends PersistenceCommand
{
    private PreparedStatement m_ps;
    private PreparedStatement m_ps1;

    private RequestImpl m_request;

    private String m_insertRequestCommand = "insert into request(id, l10n_profile_id, type, event_flow_xml, exception_xml, data_source_id, is_page_cxe_previewable, base_href, timestamp)"
            + "values (null,?,?,?,?,?,?,?,?)";

    private static final Logger s_insertLogger = Logger
            .getLogger(InsertWordCountRequestCommand.class.getName());

    public InsertWordCountRequestCommand(RequestImpl p_request,
            SequenceStore p_seqStore)
    {
        m_request = p_request;
    }

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_ps != null)
                    m_ps.close();
                if (m_ps1 != null)
                    m_ps1.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(m_insertRequestCommand);
    }

    public void setData() throws Exception
    {
        m_ps.setLong(1, m_request.getL10nProfile().getId());
        int type = m_request.getType();
        if (type == RequestImpl.EXTRACTED_LOCALIZATION_REQUEST)
        {
            m_ps.setString(2, "EXTRACTED_LOCALIZATION_REQUEST");
        }
        else if (type == RequestImpl.UNEXTRACTED_LOCALIZATION_REQUEST)
        {
            m_ps.setString(2, "UNEXTRACTED_LOCALIZATION_REQUEST");
        }
        else if (type == RequestImpl.REQUEST_WITH_CXE_ERROR)
        {
            m_ps.setString(2, "REQUEST_WITH_CXE_ERROR");
        }
        else if (type == RequestImpl.REQUEST_WITH_IMPORT_ERROR)
        {
            m_ps.setString(2, "REQUEST_WITH_IMPORT_ERROR");
        }

        if (s_insertLogger.isDebugEnabled())
        {
            s_insertLogger.debug("The value of dsid is"
                    + m_request.getDataSourceId());            
        }

        m_ps.setString(3, m_request.getEventFlowXml());
        if (m_request.getExceptionAsString().length() > 0)
        {
            m_ps.setString(4, m_request.getExceptionAsString());
        }
        else
        {
            m_ps.setString(4, null);
        }
        m_ps.setLong(5, m_request.getDataSourceId());
        boolean isCxePagePreviewable = m_request.isPageCxePreviewable();
        if (isCxePagePreviewable == true)
        {
            m_ps.setString(6, "Y");
        }
        else
        {
            m_ps.setString(6, "N");
        }
        String baseRef = m_request.getBaseHref();
        if (baseRef == null || baseRef.length() == 0)
        {
            m_ps.setNull(7, Types.VARCHAR);
        }
        else
        {
            m_ps.setString(7, baseRef);
        }
        m_ps.setDate(8, new Date(System.currentTimeMillis()));
    }

    public void batchStatements() throws Exception
    {
        m_ps.executeUpdate();
    }
}
