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

package com.globalsight.persistence.pageupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * @deprecated
 * @author YorkJin
 *
 */
public class DeleteTuPersistenceCommand extends PersistenceCommand
{
    static private Logger s_logger = Logger
            .getLogger(DeleteTuPersistenceCommand.class);

    /*
     * Too complicated for now to delete *all* garbage data.
     * 
     * static private final String s_deleteTuCommand =
     * "delete from translation_unit where id=?";
     * 
     * static private final String s_deleteTuvCommand =
     * "delete from translation_unit_variant where tu_id=?";
     * 
     * static private final String s_deleteLevMatchCommand =
     * "delete from leverage_match where original_source_tuv_id=? " +
     * "and target_locale_id=?";
     * 
     * static private final String s_deleteTaskTuvCommand =
     * "delete from task_tuv where ";
     * 
     * static private final String s_deleteTemplatePartCommand =
     * "delete from template_part where tu_id=?";
     */

    // Mark the TUs as orphans instead of deleting them.
    static private final String s_UPDATE_TU = "update " + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " set leverage_group_id=0 where id=?";
    // Getting paranoid, set orphaned TUVs to OUT_OF_DATE.
    static private final String s_UPDATE_TUV = "update " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " set state='OUT_OF_DATE' where id=?";

    private PreparedStatementBatch m_psBatch1 = null;
    private PreparedStatementBatch m_psBatch2 = null;
    private List m_tus;
    private long jobId = -1;

    //
    // Constructor
    //

    public DeleteTuPersistenceCommand(List p_tus)
    {
        m_tus = p_tus;
    }

    public void setJobId(long p_jobId)
    {
        jobId = p_jobId;
    }

    public void persistObjects(Connection p_connection, String p_companyId)
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
            close(m_psBatch1);
            close(m_psBatch2);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        String tuTableName = BigTableUtil.getTuTableJobDataInByJobId(jobId);
        String sql1 = s_UPDATE_TU.replace(
                TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName);

        String tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(jobId);
        String sql2 = s_UPDATE_TUV.replace(
                TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTableName);
        m_psBatch1 = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection, sql1,
                true);
        m_psBatch2 = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection, sql2,
                true);
    }

    public void setData() throws Exception
    {
        for (int i = 0, max = m_tus.size(); i < max; i++)
        {
            TuImpl tu = (TuImpl) m_tus.get(i);
            PreparedStatement ps1 = m_psBatch1.getNextPreparedStatement();
            ps1.setLong(1, tu.getId());
            ps1.addBatch();

            for (Iterator it = tu.getTuvs(true, jobId).iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();

                PreparedStatement ps2 = m_psBatch2.getNextPreparedStatement();
                ps2.setLong(1, tuv.getId());
                ps2.addBatch();
            }
        }
    }

    public void batchStatements() throws Exception
    {
        m_psBatch1.executeBatches();
        m_psBatch2.executeBatches();
    }
}
