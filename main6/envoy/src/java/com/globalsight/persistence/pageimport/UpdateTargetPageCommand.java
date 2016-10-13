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

import org.apache.log4j.Logger;


import com.globalsight.persistence.PersistenceCommand;

import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceException;

import java.util.Collection;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class UpdateTargetPageCommand
    extends PersistenceCommand
{
    static private final Logger s_category = Logger
            .getLogger(UpdateTargetPageCommand.class);

    static private final String m_updateNewTargetPage = "update target_page set state = ? where id = ?";

    static private final String m_updateOldTargetPage = "update target_page tp1 inner join (select tp.id from target_page tp, "
            + "workflow w where tp.source_page_id = ? and tp.workflow_iflow_instance_id = "
            + "w.iflow_instance_id and w.target_locale_id = ?) as tp2 on tp1.id = tp2.id set tp1.state = ?";

    private PreparedStatement m_ps;
    private PreparedStatement m_ps1;
    private SourcePage m_sourcePage = null;
    private Collection m_targetPages = null;
    private TargetPage m_targetPage = null;
    private String m_targetPageState;
    private String m_previousTargetPageState;


    /**
     * Constructor to use when updating a collection of new target
     * pages and the old ones.
     */
    public UpdateTargetPageCommand(SourcePage p_sourcePage, Collection p_targetPages)
    {
        m_sourcePage = p_sourcePage;
        m_targetPages = p_targetPages;
    }

    /**
     * Constructor to use when updating a specific target page.
     */
    public UpdateTargetPageCommand(TargetPage p_targetPage)
    {
        m_targetPage = p_targetPage;
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
            close(m_ps);
            close(m_ps1);
        }
    }

    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        if (m_sourcePage != null)
        {
            m_ps = p_connection.prepareStatement(m_updateNewTargetPage);
            m_ps1 = p_connection.prepareStatement(m_updateOldTargetPage);

            s_category.debug("The previous page Id is " +
                m_sourcePage.getPreviousPageId());
        }
        else
        {
            m_ps = p_connection.prepareStatement(m_updateNewTargetPage);
        }
    }

    /**
     * This code takes into consideration import and reimport.
     * For a reimport previous pageId should be greater than 0.
     * The same SQL statement is batched and executed.
     */
    public void setData()
        throws Exception
    {
        if (m_sourcePage != null)
        {
            // update each of the target pages with the new state
            for (Iterator tpi1 = m_targetPages.iterator(); tpi1.hasNext(); )
            {

                TargetPage tp1 = (TargetPage)tpi1.next();
                tp1.setPageState(m_sourcePage.getPageState());
                m_ps.setString(1, m_sourcePage.getPageState());
                m_ps.setLong(2, tp1.getId());
                m_ps.addBatch();
            }

            // update the old target pages if new ones are being imported
            // they are now OUT_OF_DATE since new ones are active
            if (m_sourcePage.getPreviousPageId() > 0 &&
                m_sourcePage.getPageState().equals(PageState.IMPORT_SUCCESS))
            {
                // loop through all target pages and mark the old ones
                // as OUT_OF_DATE
                for (Iterator tpi2 = m_targetPages.iterator(); tpi2.hasNext(); )
                {
                    TargetPage tp2 = (TargetPage) tpi2.next();
                    m_ps1.setLong(1, m_sourcePage.getPreviousPageId());
                    m_ps1.setLong(2, tp2.getLocaleId());
                    m_ps1.setString(3, PageState.OUT_OF_DATE);
                    m_ps1.addBatch();
                }
            }
        }
        else
        {
            m_ps.setString(1, m_targetPage.getPageState());
            m_ps.setLong(2, m_targetPage.getId());
            m_ps.addBatch();
        }
    }

    public void batchStatements()
        throws Exception
    {
        m_ps.executeBatch();
        if (m_ps1 != null)
        {
            m_ps1.executeBatch();
        }
    }
}
