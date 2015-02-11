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

package com.globalsight.persistence.jobcreation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.PersistenceCommand;


/**
 * Given a source page deletes all dependancies on the source page BUT
 * not the source page itself.  This is done by a different command that
 * deletes the source page and request. 
 * This includes TUs, TUVs, Leverage groups, Target Pages,...
 * but does not include any tasks or task_tuvs.  This assumes that the page
 * has not been through any of the translation process.  This is to be
 * used right after importing.
 */
public class DeleteSourcePageDependenciesCommand
	extends PersistenceCommand
{
    private static GlobalSightCategory c_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
			DeleteSourcePageDependenciesCommand.class.getName());                                          
                            
    // query for all the TU ids associated with a source page
    private static final String QUERY_TU_IDS_SQL = 
            "select id from translation_unit where leverage_group_id in " +
                "(select lg_id from source_page_leverage_group where sp_id = ?)";

    // delete the leverage matches associated with this source page
    private static final String DELETE_LEVERAGE_MATCH_SQL = 
        "delete from leverage_match where source_page_id = ?";

    // delete all the TUVs associated with the source and all target pages
    private static final String DELETE_TUVS_SQL = 
        "delete from translation_unit_variant where tu_id in ( :tuIds )";

    // delete all TUs associated with the source (and all target pages)
    private static final String DELETE_TUS_SQL =
        "delete from translation_unit where id in ( :tuIds )";

    // delete the target page association to the page's leverage group
    private static final String DELETE_TP_LG_ASSOCIATION_SQL =
        "delete from target_page_leverage_group where lg_id in " + 
         "(select lg_id from source_page_leverage_group where sp_id = ?)";

    // delete the target pages
    private static final String DELETE_TARGET_PAGES_SQL = 
        "delete from target_page where source_page_id = ?";

    // delete the source page association to the page's leverage group
    private static final String DELETE_SP_LG_ASSOCIATION_SQL = 
        "delete from source_page_leverage_group where sp_id = ?";

    // delete all the leverage groups associated with the source any target pages
    // tbd - private static final String m_deleteLeverageGroups = 


    // delete template parts
    private static final String DELETE_TEMPLATE_PARTS_SQL = 
        "delete from template_part where template_id in " + 
            "(select id from template where source_page_id = ?)";

    // delete templates
    private static final String DELETE_TEMPLATES_SQL = 
        "delete from template where source_page_id = ?";          

    private PreparedStatement m_psTargetPages;
    private PreparedStatement m_psLeverageMatches;
    private PreparedStatement m_psTuvs;
    private PreparedStatement m_psTus;
    private PreparedStatement m_psSourceLGs;
    private PreparedStatement m_psTargetLGs;
    private PreparedStatement m_psTemplateParts;
    private PreparedStatement m_psTemplates;
    private PreparedStatement m_psTuIds;
    private long m_sourcePageId;
    
    //
    // CONSTRUCTOR
    //
    public DeleteSourcePageDependenciesCommand(long p_sourcePageId)
    {
        m_sourcePageId = p_sourcePageId;
    }

    //
    // INTERFACE METHODS
    //

    /**
     * Overwrites PersistenceObject.persistObjects and adds cleanup calls.
     */
    public void persistObjects(Connection p_connection)
		throws PersistenceException 
    {
        try
        {
            super.persistObjects(p_connection);
        }
        finally
        {
            try 
            {
                if (m_psTargetPages != null) m_psTargetPages.close();
                if (m_psLeverageMatches != null) m_psLeverageMatches.close();
                if (m_psTuvs != null) m_psTuvs.close();
                if (m_psTus != null)  m_psTus.close();
                if (m_psSourceLGs != null) m_psSourceLGs.close();
                if (m_psTargetLGs != null) m_psTargetLGs.close();
                if (m_psTemplateParts != null) m_psTemplateParts.close();
                if (m_psTemplates != null) m_psTemplates.close();
                if (m_psTuIds != null) m_psTuIds.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Create all the prepared statements. Set up the SQL.
     */
    public void createPreparedStatement(Connection p_connection) 
		throws Exception 
    {   
        m_psTargetPages = p_connection.prepareStatement(DELETE_TARGET_PAGES_SQL);
        m_psLeverageMatches = p_connection.prepareStatement(DELETE_LEVERAGE_MATCH_SQL);
        
        m_psTuIds = p_connection.prepareStatement(QUERY_TU_IDS_SQL);
        m_psTuIds.setLong(1, m_sourcePageId);
        ResultSet rs = m_psTuIds.executeQuery();
        StringBuilder ids = new StringBuilder();
        ids.append("''");
        
        while (rs.next()) {
        	ids.append(", '" + rs.getLong(1) + "'");
        }
        
        m_psTuvs = p_connection.prepareStatement(DELETE_TUVS_SQL.replaceAll(":tuIds", ids.toString()));
        m_psTus = p_connection.prepareStatement(DELETE_TUS_SQL.replaceAll(":tuIds", ids.toString()));
        
        m_psSourceLGs = p_connection.prepareStatement(DELETE_SP_LG_ASSOCIATION_SQL);
        m_psTargetLGs = p_connection.prepareStatement(DELETE_TP_LG_ASSOCIATION_SQL);

        m_psTemplateParts = p_connection.prepareStatement(DELETE_TEMPLATE_PARTS_SQL);
        m_psTemplates = p_connection.prepareStatement(DELETE_TEMPLATES_SQL);
    }

    /**
     * Set the data in all the SQL prepared statements.
     */
    public void setData() 
		throws Exception                                      
    {
        m_psTargetPages.setLong(1, m_sourcePageId);    
        m_psLeverageMatches.setLong(1, m_sourcePageId);
        m_psTargetLGs.setLong(1, m_sourcePageId);
        m_psSourceLGs.setLong(1, m_sourcePageId);
        
        m_psTemplateParts.setLong(1, m_sourcePageId);
        m_psTemplates.setLong(1, m_sourcePageId);    
    }

    /**
     * Execute all the statments in the correct order.
     */
    public void batchStatements() 
		throws Exception 
    {
        m_psLeverageMatches.executeUpdate();
        m_psTuvs.executeUpdate();
        m_psTus.executeUpdate();

        m_psTargetLGs.executeUpdate();
        m_psSourceLGs.executeUpdate();

        m_psTargetPages.executeUpdate();
        
        m_psTemplateParts.executeUpdate();
        m_psTemplates.executeUpdate();
    }
}
