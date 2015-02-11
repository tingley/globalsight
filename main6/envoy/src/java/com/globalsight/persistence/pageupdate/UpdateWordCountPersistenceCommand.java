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
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.PersistenceCommand;

/**
 * Performs an in-place update of existing source page data: modified TUs and
 * TUVs are updated, unmodified but reordered TUs and TUVs are ordered into
 * their new place, and template parts are re-created.
 */
public class UpdateWordCountPersistenceCommand extends PersistenceCommand
{
    private static Logger s_logger = Logger
            .getLogger(UpdateWordCountPersistenceCommand.class);

    // private static final String s_UPDATE_SOURCEPAGE =
    // "update source_page set " +
    // "overriden_word_count=null, word_count=?, contains_gs_tag=?, " +
    // "timestamp=SYSDATE, last_modified=SYSDATE where id=?";
    private static final String s_UPDATE_SOURCEPAGE = "update source_page set "
            + "overriden_word_count=null, word_count=?, contains_gs_tag=?, "
            + "timestamp=current_timestamp, last_modified=current_timestamp where id=?";

    private static final String s_UPDATE_TARGETPAGE = "update target_page set "
            + "total_word_count=?, sub_lev_match_word_count=?, sub_lev_repetition_word_count=?, "
            + "exact_context_word_count=?, exact_segment_tm_word_count=?, "
            + "fuzzy_low_word_count=?, fuzzy_mid_word_count=?, fuzzy_mid_hi_word_count=?, "
            + "fuzzy_hi_word_count=?, no_match_word_count=?, "
            + "repetition_word_count=?, timestamp=CURRENT_TIMESTAMP, "
            + "last_modified=CURRENT_TIMESTAMP where id=?";
    // private static final String s_UPDATE_TARGETPAGE =
    // "update target_page set " +
    // "total_word_count=?, sub_lev_match_word_count=?, sub_lev_repetition_word_count=?, "
    // +
    // "exact_context_word_count=?, exact_segment_tm_word_count=?, " +
    // "fuzzy_low_word_count=?, fuzzy_mid_word_count=?, fuzzy_mid_hi_word_count=?, "
    // +
    // "fuzzy_hi_word_count=?, no_match_word_count=?, " +
    // "repetition_word_count=?, timestamp=SYSDATE, " +
    // "last_modified=SYSDATE where id=?";

    private static final String s_UPDATE_JOB = "update job set is_wordcount_reached='N', "
            + "overriden_word_count=?, timestamp=CURRENT_TIMESTAMP where id=?";
    // private static final String s_UPDATE_JOB =
    // "update job set is_wordcount_reached='N', " +
    // "overriden_word_count=?, timestamp=SYSDATE where id=?";

    private static final String s_UPDATE_WORKFLOW = "update workflow set "
            + "total_word_count=?, sub_lev_match_word_count=?, sub_lev_repetition_word_count=?, "
            + "exact_context_word_count=?, exact_segment_tm_word_count=?, "
            + "fuzzy_low_word_count=?, fuzzy_mid_word_count=?, fuzzy_mid_hi_word_count=?, "
            + "fuzzy_hi_word_count=?, no_match_word_count=?, "
            + "repetition_word_count=?, timestamp=CURRENT_TIMESTAMP "
            + "where iflow_instance_id=?";
    // final String s_UPDATE_WORKFLOW =
    // "update workflow set " +
    // "total_word_count=?, sub_lev_match_word_count=?, sub_lev_repetition_word_count=?, "
    // +
    // "exact_context_word_count=?, exact_segment_tm_word_count=?, " +
    // "fuzzy_low_word_count=?, fuzzy_mid_word_count=?, fuzzy_mid_hi_word_count=?, "
    // +
    // "fuzzy_hi_word_count=?, no_match_word_count=?, " +
    // "repetition_word_count=?, timestamp=SYSDATE " +
    // "where iflow_instance_id=?";

    //
    // Members
    //

    private SourcePage m_sourcePage;
    private ArrayList m_targetPages;
    private Job m_job;
    private ArrayList m_workflows;

    private int m_totalWordCount;
    private boolean m_hasGsTags;

    private PreparedStatement m_ps_sp;
    private PreparedStatement m_ps_tp;
    private PreparedStatement m_ps_job;
    private PreparedStatement m_ps_wf;

    //
    // Constructor
    //

    /**
     *
     */
    public UpdateWordCountPersistenceCommand(SourcePage p_sourcePage,
            ArrayList p_targetPages, Job p_job, ArrayList p_workflows,
            int p_totalWordCount, boolean p_hasGsTags)
    {
        m_sourcePage = p_sourcePage;
        m_targetPages = p_targetPages;
        m_job = p_job;
        m_workflows = p_workflows;

        m_totalWordCount = p_totalWordCount;
        m_hasGsTags = p_hasGsTags;
    }

    //
    // Methods
    //

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception sqle)
        {
            throw new PersistenceException(sqle);
        }
        finally
        {
            close(m_ps_sp);
            close(m_ps_tp);
            close(m_ps_job);
            close(m_ps_wf);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        m_ps_sp = p_connection.prepareStatement(s_UPDATE_SOURCEPAGE);
        m_ps_tp = p_connection.prepareStatement(s_UPDATE_TARGETPAGE);
        m_ps_job = p_connection.prepareStatement(s_UPDATE_JOB);
        m_ps_wf = p_connection.prepareStatement(s_UPDATE_WORKFLOW);
    }

    public void setData() throws Exception
    {
        // Timestamp now = new Timestamp(System.currentTimeMillis());

        m_sourcePage.setWordCount(m_totalWordCount);
        m_sourcePage.clearOverridenWordCount();
        ExtractedSourceFile file = (ExtractedSourceFile) m_sourcePage
                .getPrimaryFile();
        file.containGsTags(m_hasGsTags);

        // update source_page set overriden_word_count=null, word_count=?,
        // contains_gs_tag=?
        m_ps_sp.setInt(1, m_totalWordCount);
        m_ps_sp.setString(2, m_hasGsTags ? "Y" : "N");
        m_ps_sp.setLong(3, m_sourcePage.getId());
        m_ps_sp.addBatch();

        if (s_logger.isDebugEnabled())
        {
            String s = s_UPDATE_SOURCEPAGE;
            s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
            s = s.replaceFirst("\\?", m_hasGsTags ? "Y" : "N");
            s = s.replaceFirst("\\?", String.valueOf(m_sourcePage.getId()));
            System.err.println(s);
        }

        if (m_targetPages.size() > 0)
        {
            for (int i = 0, max = m_targetPages.size(); i < max; i++)
            {
                TargetPage tp = (TargetPage) m_targetPages.get(i);

                // update target_page set total_word_count=?,
                // sub_lev_match_word_count=?,
                // sub_lev_repetition_word_count, exact_context_word_count=?,
                // exact_segment_tm_word_count=?, fuzzy_low_word_count=?,
                // fuzzy_mid_word_count=?,
                // fuzzy_mid_hi_word_count=?, fuzzy_hi_word_count=?,
                // no_match_word_count=?, repetition_word_count=?
                m_ps_tp.setInt(1, m_totalWordCount);
                m_ps_tp.setInt(2, 0);
                m_ps_tp.setInt(3, 0);
                m_ps_tp.setInt(4, 0);
                m_ps_tp.setInt(5, 0);
                m_ps_tp.setInt(6, 0);
                m_ps_tp.setInt(7, 0);
                m_ps_tp.setInt(8, 0);
                m_ps_tp.setInt(9, 0);
                m_ps_tp.setInt(10, m_totalWordCount);
                m_ps_tp.setInt(11, 0);
                m_ps_tp.setLong(12, tp.getId());
                m_ps_tp.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_UPDATE_TARGETPAGE;
                    s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(tp.getId()));
                    System.err.println(s);
                }
            }
        }

        // update job set is_wordcount_reached='N',
        // overriden_word_count=?, timestamp=SYSDATE where id=?
        m_ps_job.setLong(1, m_totalWordCount);
        m_ps_job.setLong(2, m_job.getId());
        m_ps_job.addBatch();

        if (s_logger.isDebugEnabled())
        {
            String s = s_UPDATE_JOB;
            s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
            s = s.replaceFirst("\\?", String.valueOf(m_job.getId()));
            System.err.println(s);
        }

        if (m_workflows.size() > 0)
        {
            for (int i = 0, max = m_workflows.size(); i < max; i++)
            {
                Workflow wf = (Workflow) m_workflows.get(i);

                // update workflow set total_word_count=?,
                // sub_lev_match_word_count=?,
                // sub_lev_repetition_word_count=?, exact_context_word_count=?,
                // exact_segment_tm_word_count=?,fuzzy_low_word_count=?,
                // fuzzy_mid_word_count=?,
                // fuzzy_mid_hi_word_count=?, fuzzy_hi_word_count=?,
                // no_match_word_count=?, repetition_word_count=?,
                // timestamp=SYSDATE where iflow_instance_id=?";
                m_ps_wf.setInt(1, m_totalWordCount);
                m_ps_wf.setInt(2, 0);
                m_ps_wf.setInt(3, 0);
                m_ps_wf.setInt(4, 0);
                m_ps_wf.setInt(5, 0);
                m_ps_wf.setInt(6, 0);
                m_ps_wf.setInt(7, 0);
                m_ps_wf.setInt(8, 0);
                m_ps_wf.setInt(9, 0);
                m_ps_wf.setInt(10, m_totalWordCount);
                m_ps_wf.setInt(11, 0);
                m_ps_wf.setLong(12, wf.getId());
                m_ps_wf.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_UPDATE_WORKFLOW;
                    s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(m_totalWordCount));
                    s = s.replaceFirst("\\?", String.valueOf(0));
                    s = s.replaceFirst("\\?", String.valueOf(wf.getId()));
                    System.err.println(s);
                }
            }
        }
    }

    public void batchStatements() throws Exception
    {
        m_ps_sp.executeBatch();
        m_ps_tp.executeBatch();
        m_ps_job.executeBatch();
        m_ps_wf.executeBatch();
    }
}
