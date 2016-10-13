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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;

public class PageImportQuery
{
    private PreparedStatement m_psSourcePage = null;
    private PreparedStatement m_psSourcePage1 = null;
    private PreparedStatement m_psLGIds = null;
    // get the latest version of a particular source page
    // that is an EXTRACTED file
    private static final String LATEST_SOURCE_PAGE_SQL = " SELECT SP.* FROM SOURCE_PAGE SP, REQUEST R, L10N_PROFILE LP WHERE"
            + " SP.EXTERNAL_PAGE_ID= ?"
            + " AND DATA_TYPE is not null "
            + " AND LP.SOURCE_LOCALE_ID = ?"
            + " AND SP.COMPANY_ID = ?"
            + " AND R.L10N_PROFILE_ID=LP.ID AND R.PAGE_ID=SP.ID "
            + " AND SP.STATE != '" + PageState.IMPORT_FAIL + "' "
            + " AND R.TYPE != 'REQUEST_WITH_IMPORT_ERROR' "
            + " AND R.JOB_ID IS NOT NULL "
            + " ORDER BY SP.ID DESC limit 1";
    private static final String SOURCE_PAGE_BY_ID = "select * from source_page where id = ? ";
    private static final String LEVERAGE_GROUP_IDS = "select * from source_page_leverage_group where SP_ID = ?";

    public PageImportQuery()
    {
    }

    public SourcePage getLatestVersionOfSourcePage(RequestImpl p_request)
            throws PersistenceException
    {
        Connection connection = null;
        SourcePage sourcePage = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            m_psSourcePage = connection
                    .prepareStatement(LATEST_SOURCE_PAGE_SQL);
            m_psSourcePage.setString(1, p_request.getExternalPageId());
            m_psSourcePage.setLong(2, p_request.getL10nProfile()
                    .getSourceLocale().getId());
            m_psSourcePage.setLong(3, p_request.getCompanyId());
            rs = m_psSourcePage.executeQuery();
            sourcePage = processResultSet(rs);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(m_psSourcePage);
            DbUtil.silentReturnConnection(connection);
        }

        return sourcePage;
    }

    public SourcePage getSourcePageById(long p_id) throws PersistenceException
    {
        Connection connection = null;
        SourcePage sourcePage = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            m_psSourcePage1 = connection.prepareStatement(SOURCE_PAGE_BY_ID);
            m_psSourcePage1.setLong(1, p_id);
            rs = m_psSourcePage1.executeQuery();
            sourcePage = processResultSet(rs);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(m_psSourcePage1);
            DbUtil.silentReturnConnection(connection);
        }

        return sourcePage;
    }

    public List<Long> getLeverageGroupIds(long p_sourcePageId)
            throws PersistenceException
    {
        Connection connection = null;
        ResultSet rs = null;
        ArrayList<Long> list = null;
        try
        {
            connection = DbUtil.getConnection();
            m_psLGIds = connection.prepareStatement(LEVERAGE_GROUP_IDS);
            m_psLGIds.setLong(1, p_sourcePageId);
            rs = m_psLGIds.executeQuery();
            int i = 0;
            while (rs.next())
            {
                i++;
                if (i == 1)
                {
                    list = new ArrayList<Long>();
                }
                long lgId = rs.getLong(1);
                list.add(new Long(lgId));
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(m_psLGIds);
            DbUtil.silentReturnConnection(connection);
        }

        return list;
    }

    private SourcePage processResultSet(ResultSet p_resultSet) throws Exception
    {
        ResultSet rs = p_resultSet;
        SourcePage sourcePage = null;
        while (rs.next())
        {
            // tbd - assumes this is an extracted file
            sourcePage = new SourcePage(ExtractedSourceFile.EXTRACTED_FILE);
            ExtractedSourceFile esf = (ExtractedSourceFile) sourcePage
                    .getPrimaryFile();
            sourcePage.setId(rs.getLong(1));
            sourcePage.setExternalPageId(rs.getString(2));
            sourcePage.setWordCount(rs.getInt(3));
            esf.setOriginalCodeSet(rs.getString(4));
            sourcePage.setPageState(rs.getString(5));
            sourcePage.setDataSourceType(rs.getString(6));
            esf.setDataType(rs.getString(7));
            esf.setInternalBaseHref(rs.getString(8));
            esf.setExternalBaseHref(rs.getString(9));
            sourcePage.setPreviousPageId(rs.getLong(10));
            // 11 - timestamp field which isn't loaded into the object
            if (rs.getString(12).equals("Y"))
            {
                esf.containGsTags(true);
            }
            else
            {
                esf.containGsTags(false);
            }
        }

        return sourcePage;
    }
}
