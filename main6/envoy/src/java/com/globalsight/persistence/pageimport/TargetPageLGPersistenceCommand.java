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
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.persistence.PersistenceCommand;

/**
 * @deprecated
 */
public class TargetPageLGPersistenceCommand
    extends PersistenceCommand
{
    static private final String INSERTTARGETLGCOMMAND =
        "insert into target_page_leverage_group values(?,?)";

    private List m_targetPages;
    private SourcePage m_sourcePage;
    private PreparedStatement m_ps;


    public TargetPageLGPersistenceCommand(SourcePage p_sp,
        List p_targetPages)
    {
        m_sourcePage = p_sp;
        m_targetPages = p_targetPages;
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
                {
                    m_ps.close();
                }
            }
            catch (Throwable ignore)
            { /* ignore */ }
        }
    }


    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_ps = p_connection.prepareStatement(INSERTTARGETLGCOMMAND);
    }


    public void setData()
        throws Exception
    {
        // assumes this is an extracted file
        List lgList = ((ExtractedFile)m_sourcePage.getPrimaryFile()).
                            getLeverageGroups();

        for (Iterator it = lgList.iterator(); it.hasNext(); )
        {
            LeverageGroupImpl lg = (LeverageGroupImpl)it.next();

            Iterator iTargetPages = m_targetPages.iterator();
            while (iTargetPages.hasNext())
            {
                TargetPage targetPage = (TargetPage)iTargetPages.next();

                m_ps.setLong(1, lg.getId());
                m_ps.setLong(2, targetPage.getId());

                m_ps.addBatch();
            }
        }
    }


    public void batchStatements()
        throws Exception
    {
        m_ps.executeBatch();
    }
}
