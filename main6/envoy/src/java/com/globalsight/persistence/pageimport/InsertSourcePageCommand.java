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

// globalsight
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.UnextractedFile;

// java
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;

/**
 * @deprecated
 * Persists a source page in the database - whether it has un-extraced
 * or extraced file content.
 */
public class InsertSourcePageCommand
    extends PersistenceCommand
{
    private static final GlobalSightCategory s_insertLogger =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            InsertSourcePageCommand.class);

    //this gets set to one of the SQLs below depending on the request type
    private String m_insertSourcePage;

    private static final String m_insertSourcePageWithExtractedFile =
        "insert into source_page(id,external_page_id, state, data_source_type, original_encoding, data_type, " +
        "previous_page_id, internal_base_href, external_base_href, timestamp, contains_gs_tag, gxml_version, company_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String m_insertSourcePageWithUnextractedFile =
        "insert into source_page(id, external_page_id, state, data_source_type, timestamp, storage_path, modifier_user_id, last_modified, file_length, company_id)" +
        " values(?,?,?,?,?,?,?,?,?,?)";

    private String m_updateRequest = "update request set page_id = ? where id = ?";
    private Request m_request;
    private SourcePage m_sourcePage;
    private PreparedStatement m_ps1;
    private PreparedStatement m_ps2;
    private SequenceStore     m_seqStore;
    // specifies if this is an extracted file import or an un-extracted
    // set to "true" for extracted file import

    public InsertSourcePageCommand(Request p_request,
        SourcePage p_sourcePage,
        SequenceStore p_seqStore)
    {
        m_request = p_request;
        m_sourcePage = p_sourcePage;
        m_seqStore = p_seqStore;

        if (p_sourcePage.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            m_insertSourcePage = m_insertSourcePageWithExtractedFile;
        }
        else // un-extracted file
        {
            m_insertSourcePage = m_insertSourcePageWithUnextractedFile;
        }
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
                if (m_ps1 != null)
                {
                    m_ps1.close();
                }
                if (m_ps2 != null)
                {
                    m_ps2.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_ps1 = p_connection.prepareStatement(m_insertSourcePage);
        m_ps2 = p_connection.prepareStatement(m_updateRequest);

    }

    public void setData()
        throws Exception
    {
        // set things common to the both and inserted in the same order
        long primaryKey = allocateSequenceNumberRange(m_seqStore);
        m_ps1.setLong(1,primaryKey);
        m_ps1.setString(2, m_sourcePage.getExternalPageId());
        m_ps1.setString(3, m_sourcePage.getPageState());
        m_ps1.setString(4, m_sourcePage.getDataSourceType());

        // set the things specific to the different types
        if (m_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
        {
            setDataForExtractedFileImport();
        }
        else
        {
            //tbd - will errors fall into there?
            setDataForUnextractedFileImport();
        }

        // set the data in the second command to set the request to
        // source page relationship
        m_sourcePage.setId(primaryKey);
        m_ps2.setLong(1,primaryKey);
        m_ps2.setLong(2,m_request.getId());
        m_request.setSourcePage(m_sourcePage);
        m_sourcePage.setRequest(m_request);
    }

    private void setDataForExtractedFileImport()
        throws Exception
    {
        ExtractedSourceFile esf =
            (ExtractedSourceFile)m_sourcePage.getPrimaryFile();

        m_ps1.setString(5, esf.getOriginalCodeSet());
        m_ps1.setString(6, esf.getDataType());

        long previousPageId = m_sourcePage.getPreviousPageId();

        if (s_insertLogger.isDebugEnabled())
        {
            s_insertLogger.debug("The value of ppId is" + previousPageId);
        }

        if (previousPageId == 0)
        {
            m_ps1.setNull(7, Types.NUMERIC);
        }
        else
        {
            m_ps1.setLong(7, previousPageId);
        }
        String internalPageRef = esf.getInternalBaseHref();
        if (internalPageRef == null)
        {
            m_ps1.setNull(8, Types.VARCHAR);
        }
        else
        {
            m_ps1.setString(8, internalPageRef);
        }
        String externalPageRef = esf.getExternalBaseHref();
        if (externalPageRef == null)
        {
            m_ps1.setNull(9, Types.VARCHAR);
        }
        else
        {
            m_ps1.setString(9, externalPageRef);
        }

        m_ps1.setDate(10, new Date(System.currentTimeMillis()));

        if (esf.containGsTags())
        {
            m_ps1.setString(11, "Y");
        }
        else
        {
            m_ps1.setString(11, "N");
        }

        String gxmlVersion = esf.getGxmlVersion();
        m_ps1.setString(12, gxmlVersion);
        m_ps1.setLong(13, new Long(m_request.getCompanyId()).longValue());
    }

    private void setDataForUnextractedFileImport()
        throws Exception
    {
        UnextractedFile uf = (UnextractedFile)m_sourcePage.getPrimaryFile();

        m_ps1.setDate(5, new Date(System.currentTimeMillis())); //timestamp
        m_ps1.setString(6, uf.getStoragePath());

        if (uf.getLastModifiedBy() != null)
        {
            m_ps1.setString(7, uf.getLastModifiedBy());
        }
        else
        {
            m_ps1.setNull(7, java.sql.Types.VARCHAR);
        }

        if (uf.getLastModifiedDate() != null)
        {
            m_ps1.setDate(8, new Date(uf.getLastModifiedDate().getTime()));
        }
        else
        {
            m_ps1.setNull(8, java.sql.Types.DATE);
        }

        m_ps1.setLong(9, uf.getLength());
        m_ps1.setLong(10, new Long(m_request.getCompanyId()).longValue());
    }

    public void batchStatements() throws Exception
    {
        int ret1 = m_ps1.executeUpdate();
        int ret2 = m_ps2.executeUpdate();
    }

    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }
}
