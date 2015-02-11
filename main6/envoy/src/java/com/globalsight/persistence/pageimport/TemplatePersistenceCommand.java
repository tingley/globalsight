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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.SequenceStore;

/**
 * @deprecated
 */
public class TemplatePersistenceCommand extends PersistenceCommand
{
    private String m_insertCommand = "insert into Template values(?,?,?)";
    private static String TEMPLATE_SEQ = "PAGE_TEMPLATE_SEQ";
    private PreparedStatement m_ps;
    private SourcePage m_sourcePage;
    private Map m_templates;
    private HashMap m_sequenceMap;
    private HashMap m_typeConversion;

    public TemplatePersistenceCommand(SourcePage p_sourcePage,
                                      HashMap p_sequenceMap)
    {
        m_sourcePage = p_sourcePage;
        ExtractedFile ef = (ExtractedFile)m_sourcePage.getPrimaryFile();
        m_templates =  ef.getTemplateMap();
        
        m_sequenceMap = p_sequenceMap;
        m_typeConversion = new HashMap();
        m_typeConversion.put("1", "EXP");
        m_typeConversion.put("2", "STD");
        m_typeConversion.put("3", "DTL");
        m_typeConversion.put("4", "PRV");
    }
    public String getInsertCommand()
    {
        return m_insertCommand;
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
        catch(Exception sqle)
        {
            throw new PersistenceException(sqle);
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
            catch (Exception e)
            {
            }
        }
    }
    public void createPreparedStatement(Connection p_connection)
        throws Exception
    {
        m_ps = p_connection.prepareStatement(m_insertCommand);
    }

    public void setData()  throws Exception
    {
        Iterator it = m_templates.values().iterator();
        SequenceStore seqStore = (SequenceStore)m_sequenceMap.get(TEMPLATE_SEQ);
        long primaryKey = allocateSequenceNumberRange(seqStore);
        while (it.hasNext())
        {
            PageTemplate pageTemplate = (PageTemplate)it.next();
            m_ps.setLong(1,primaryKey);
            m_ps.setLong(2,m_sourcePage.getId());
            long type = pageTemplate.getType();
            String  pType = new Long(type).toString();
            pType = (String)m_typeConversion.get(pType);
            m_ps.setString(3,pType);
            m_ps.addBatch();
            pageTemplate.setId(primaryKey);
            primaryKey++;
        }

    }
    public void batchStatements() throws Exception
    {
        m_ps.executeBatch();
    }
}
