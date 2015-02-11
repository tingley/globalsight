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

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

/**
 * @deprecated Hibernate supports all its functionality.
 */
public class InsertRequestCommand extends PersistenceCommand
{
    // static private final String s_insertRequestCommand =
    // "insert into request(id, l10n_profile_id, type, event_flow_xml,
    // exception_xml, data_source_id, is_page_cxe_previewable, batch_id,
    // batch_page_count, batch_page_number,batch_doc_page_count,
    // batch_doc_page_number, batch_job_name, base_href, timestamp, company_id)"
    // +
    // "values(?,?,?,empty_clob(),empty_clob(),?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * @deprecated we do not insert id for records now and the class is
     *             deprecated also. Hibernate supports all its functionality.
     */
    static private final String s_insertRequestCommand = "insert into request(id, l10n_profile_id, type, event_flow_xml, exception_xml, data_source_id, is_page_cxe_previewable, batch_id, batch_page_count, batch_page_number,batch_doc_page_count, batch_doc_page_number, batch_job_name, base_href, timestamp, company_id)"
            + "values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    static private final String s_selectRequestCommand = "select * from request where id = ? for update";

    private PreparedStatement m_ps;
    private PreparedStatement m_ps2;
    private RequestImpl m_request;
//    private SequenceStore m_seqStore;

    public InsertRequestCommand(RequestImpl p_request, SequenceStore p_seqStore)
    {
        m_request = p_request;
//        m_seqStore = p_seqStore;
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
        catch (Exception ex)
        {
            throw new PersistenceException(ex);
        }
        finally
        {
            try
            {
                if (m_ps != null)
                    m_ps.close();
                if (m_ps2 != null)
                    m_ps2.close();
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(s_insertRequestCommand);
        m_ps2 = p_connection.prepareStatement(s_selectRequestCommand);
    }

    public void setData() throws Exception
    {
//        long primaryKey = allocateSequenceNumberRange(m_seqStore);

        // s_logger.debug("The value of primaryKey is " + primaryKey);

//        m_ps.setLong(1, primaryKey);
        m_ps.setLong(1, m_request.getL10nProfile().getId());

        int type = m_request.getType();

        // s_logger.debug("The value of type is" + type);

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

        // Insert text type into MySql as String.
        m_ps.setString(3, m_request.getEventFlowXml());
        if (m_request.getExceptionAsString().length() > 0)
        {
            m_ps.setString(4, m_request.getExceptionAsString());
        }
        else
        {
            m_ps.setString(4, null);
        }

        // s_logger.debug("The value of dsid is" + m_request.getDataSourceId());

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

        BatchInfo bi = m_request.getBatchInfo();
        if (bi != null)
        {
            String batch_id = bi.getBatchId();

            // s_logger.debug("The batch id is" + batch_id);

            if (batch_id == null || batch_id.length() == 0)
            {
                m_ps.setNull(7, Types.VARCHAR);
            }
            else
            {
                m_ps.setString(7, batch_id);
            }

            long batchPageCount = bi.getPageCount();
            m_ps.setLong(8, batchPageCount);
            long batchPageNumber = bi.getPageNumber();
            m_ps.setLong(9, batchPageNumber);
            long batchDocPageCount = bi.getDocPageCount();
            m_ps.setLong(10, batchDocPageCount);
            long batchDocPageNumber = bi.getDocPageNumber();
            m_ps.setLong(11, batchDocPageNumber);

            String batchJobName = bi.getJobPrefixName();
            if (batchJobName == null || batchJobName.length() == 0)
            {
                m_ps.setNull(12, Types.VARCHAR);
            }
            else
            {
                m_ps.setString(12, batchJobName);
            }
        }

        String baseRef = m_request.getBaseHref();
        if (baseRef == null || baseRef.length() == 0)
        {
            m_ps.setNull(13, Types.VARCHAR);
        }
        else
        {
            m_ps.setString(13, baseRef);
        }

        m_ps.setDate(14, new Date(System.currentTimeMillis()));

        m_ps.setLong(15, new Long(m_request.getCompanyId()).longValue());

//        m_request.setId(primaryKey);
    }

    public void batchStatements() throws Exception
    {
        m_ps.executeUpdate();
    }

    // public void batchStatements()
    // throws Exception
    // {
    // int ret = m_ps.executeUpdate();
    // Clob clobEventFlow = null;
    // CLOB oclobEventFlow = null;
    // Clob clobException = null;
    // CLOB oclobException = null;
    // Writer writer = null;
    // Writer writer1 = null;
    // ResultSet rs = null;
    //
    // try
    // {
    // m_ps2.setLong(1, m_request.getId());
    // rs = m_ps2.executeQuery();
    // if (rs.next())
    // {
    // clobEventFlow = rs.getClob(4);
    // }
    //
    // oclobEventFlow = (CLOB)clobEventFlow;
    // writer = oclobEventFlow.getCharacterOutputStream();
    // StringReader sr = new StringReader(m_request.getEventFlowXml());
    // char[] buffer = new char[oclobEventFlow.getChunkSize()];
    // int charsRead = 0;
    // while ((charsRead = sr.read(buffer)) != EOF)
    // {
    // writer.write(buffer, 0, charsRead);
    // }
    //
    // if (m_request.getExceptionAsString().length() > 0)
    // {
    // clobException = rs.getClob(5);
    // oclobException = (CLOB)clobException;
    // writer1 = oclobException.getCharacterOutputStream();
    // StringReader sr1 = new StringReader(m_request.getExceptionAsString());
    // char[] buffer1 = new char[oclobException.getChunkSize()];
    // int charsRead1 = 0;
    // while ((charsRead1 = sr1.read(buffer1)) != EOF)
    // {
    // writer1.write(buffer1, 0, charsRead1);
    // }
    // }
    // }
    // finally
    // {
    // close(writer);
    // close(writer1);
    // close(rs);
    // }
    // }
}
