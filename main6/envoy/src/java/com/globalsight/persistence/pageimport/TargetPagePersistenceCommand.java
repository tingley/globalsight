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
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;

/**
 * @deprecated
 */
public class TargetPagePersistenceCommand extends PersistenceCommand
{
    //
    // Public Static Members
    //
    public static final String TARGET_PAGE_SEQ_NAME = "TARGET_PAGE_SEQ";

    //
    // Private Static Members
    //
    private static final Logger c_logger = Logger
            .getLogger(TargetPagePersistenceCommand.class);

    private static final String m_insertTargetPageWithExtractedFileCommand = "insert into target_page (ID, STATE, SOURCE_PAGE_ID, TIMESTAMP, INTERNAL_BASE_HREF, EXTERNAL_BASE_HREF, GXML_VERSION) "
            + "values(null,?,?,?,?,?,?)";
    private static final String m_insertTargetPageWithUnextractedFileCommand =
        "insert into target_page (ID, STATE, SOURCE_PAGE_ID, TIMESTAMP, " +
        "TOTAL_WORD_COUNT, SUB_LEV_MATCH_WORD_COUNT, SUB_LEV_REPETITION_WORD_COUNT, " +
        "FUZZY_LOW_WORD_COUNT, FUZZY_MED_WORD_COUNT, FUZZY_MED_HI_WORD_COUNT, " + 
        "FUZZY_HI_WORD_COUNT, EXACT_CONTEXT_WORD_COUNT, EXACT_SEGMENT_TM_WORD_COUNT, IN_CONTEXT_MATCH_WORD_COUNT " + 
        "NO_MATCH_WORD_COUNT,NO_USE_IC_MATCH_WORD_COUNT,NO_USE_EXACT_MATCH_WORD_COUNT,IS_DEFAULT_CONTEXT_MATCH, storage_path, modifier_user_id, last_modified, file_length) " +
        " values(null,?,?,?,0,0,0,0,0,0,0,0,0,0,0,0,0,'N',?,?,?,?)";

    // private static final String m_updateImportError =
    // "update target_page set exception_xml = empty_clob() where id = ? ";

    private static final String m_updateImportError = "update target_page set exception_xml = ? where id = ? ";
    private String m_selectForUpdate = "select exception_xml from target_page where id = ? for update";

    // this gets set to one of the SQL strings above according to what type
    // of primary file the page contains
    private String m_insertTargetPageCommand = null;

    //
    // Private Members
    //

    // this is set to specify if the TargetPages contain an
    // "extracted" file (true) or an "unextracted" file (false)
    private boolean m_extractedFile = true;
    private PreparedStatement m_ps;
    private PreparedStatement m_psImportError1;
    private PreparedStatement m_psImportError2;
    private List m_targetPages;
//    private HashMap m_sequenceMap;

    //
    // Constructor
    //
    public TargetPagePersistenceCommand(List p_targetPages,
            HashMap p_sequenceMap)
    {
        m_targetPages = p_targetPages;
//        m_sequenceMap = p_sequenceMap;

        // set the correct SQL according to what primary file is
        // associated with a target page. The file type is associated
        // with the entire collection so only need to look at the
        // first one.
        if (m_targetPages.size() > 0)
        {
            TargetPage tp = (TargetPage) p_targetPages.get(0);
            if (tp.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                m_extractedFile = true;
                m_insertTargetPageCommand = m_insertTargetPageWithExtractedFileCommand;
            }
            else
            {
                m_extractedFile = false;
                m_insertTargetPageCommand = m_insertTargetPageWithUnextractedFileCommand;
            }
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
                if (m_ps != null)
                    m_ps.close();
                if (m_psImportError1 != null)
                    m_psImportError1.close();
                if (m_psImportError2 != null)
                    m_psImportError2.close();
            }
            catch (Throwable ignore)
            { /* ignore */
            }
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps = p_connection.prepareStatement(m_insertTargetPageCommand);
        m_psImportError1 = p_connection.prepareStatement(m_updateImportError);
        m_psImportError2 = p_connection.prepareStatement(m_selectForUpdate);
    }

    public void setData() throws Exception
    {
//        SequenceStore seqStore = (SequenceStore) m_sequenceMap
//                .get(TARGET_PAGE_SEQ_NAME);
//        long primaryKey = allocateSequenceNumberRange(seqStore);

        for (Iterator it = m_targetPages.iterator(); it.hasNext();)
        {
            TargetPage targetPage = (TargetPage) it.next();

//            m_ps.setLong(1, primaryKey);
            m_ps.setString(1, targetPage.getPageState());
            m_ps.setLong(2, targetPage.getSourcePage().getId());
            m_ps.setDate(3, new Date(System.currentTimeMillis()));

            if (m_extractedFile)
            {
                setDataForExtractedFileImport(targetPage);
            }
            else
            {
                setDataForUnextractedFileImport(targetPage);
            }

            m_ps.addBatch();

//            targetPage.setId(primaryKey);

//            ++primaryKey;
        }
    }

    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();

        // now go through all the target pages and the ones that
        // are marked as IMPORT_FAIL should have an exception to write out
        for (int i = 0; i < m_targetPages.size(); i++)
        {
            TargetPage tp = (TargetPage) m_targetPages.get(i);
            if (tp.getPageState().equals(PageState.IMPORT_FAIL))
            {
                try
                {
                    executeImportErrorUpdate(tp);
                }
                catch (Exception e)
                {
                    c_logger.error(
                            "Failed to persist the import error for target page "
                                    + tp.getId(), e);
                }
            }
        }
    }

    private void setDataForExtractedFileImport(TargetPage p_targetPage)
            throws SQLException
    {
        // add an empty string for the base href.
        // this is needed in order to populate the ExtractedTargetFile
        // successfully. Needs atleast one data item from the class
        // to not be null.
        ExtractedFile ef = (ExtractedFile) p_targetPage.getPrimaryFile();
        String internalPageRef = ef.getInternalBaseHref();
        if (internalPageRef == null)
        {
            m_ps.setNull(4, Types.VARCHAR);
        }
        else
        {
            m_ps.setString(4, internalPageRef);
        }

        String externalPageRef = ef.getExternalBaseHref();
        if (externalPageRef == null)
        {
            m_ps.setNull(5, Types.VARCHAR);
        }
        else
        {
            m_ps.setString(5, externalPageRef);
        }

        String gxmlVersion = ef.getGxmlVersion();
        m_ps.setString(6, gxmlVersion);
    }

    private void setDataForUnextractedFileImport(TargetPage p_targetPage)
            throws SQLException
    {
        UnextractedFile uf = (UnextractedFile) p_targetPage.getPrimaryFile();
        m_ps.setString(4, uf.getStoragePath());
        if (uf.getLastModifiedBy() != null)
        {
            m_ps.setString(5, uf.getLastModifiedBy());
        }
        else
        {
            m_ps.setNull(5, java.sql.Types.VARCHAR);
        }
        if (uf.getLastModifiedDate() != null)
        {
            m_ps.setDate(6, new Date(uf.getLastModifiedDate().getTime()));
        }
        else
        {
            m_ps.setNull(6, java.sql.Types.DATE);
        }
        m_ps.setLong(7, uf.getLength());
    }

    /**
     * Store an import error into the CLOB field.
     */
    private void executeImportErrorUpdate(TargetPage p_tp) throws Exception
    {
        if (p_tp.getImportError() != null)
        {
            m_psImportError1.setString(1, p_tp.getErrorAsString());
            m_psImportError1.setLong(2, p_tp.getId());
            m_psImportError1.executeUpdate();

            // m_psImportError1.setLong(1, p_tp.getId());
            // m_psImportError1.executeUpdate();
            // Clob clob = null;
            // CLOB oclob;
            // Writer writer = null;
            // ResultSet rs = null;
            //
            // try
            // {
            // m_psImportError2.setLong(1,p_tp.getId());
            // rs = m_psImportError2.executeQuery();
            // if (rs.next())
            // {
            // clob = rs.getClob(1);
            // }
            // oclob = (CLOB)clob;
            // writer = oclob.getCharacterOutputStream();
            // StringReader sr = new StringReader(p_tp.getErrorAsString());
            // char[] buffer = new char[oclob.getChunkSize()];
            // int charsRead = 0;
            // while ((charsRead = sr.read(buffer)) != EOF)
            // {
            // writer.write(buffer, 0, charsRead);
            // }
            // }
            // finally
            // {
            // close(writer);
            // close(rs);
            // }
        }
    }
}
