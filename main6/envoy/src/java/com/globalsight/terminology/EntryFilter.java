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

package com.globalsight.terminology;

import org.apache.log4j.Logger;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;

import com.globalsight.terminology.exporter.ExportOptions;
import com.globalsight.terminology.exporter.ExportOptions.FilterCondition;

import com.globalsight.terminology.util.SqlUtil;
import org.dom4j.Element;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Holds constraints to filter termbase entries according to criteria
 * that can be applied in the database itself (e.g.  creation user,
 * modification users, dates, domain, project), or in software ("entry
 * contains term in language X").
 */
public class EntryFilter
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            EntryFilter.class);

    /** DB field: creation user */
    private String m_createdBy = "";
    /** DB field: modification user */
    private String m_modifiedBy = "";

    /**
     * DB field: creation/modification date ranges. The format is
     * currently determined by the UI to be DD/MM/YYYY. The backend
     * will decode the dates.
     */
    private String m_createdAfter = "";
    private String m_createdBefore = "";
    private String m_modifiedAfter = "";
    private String m_modifiedBefore = "";

    /** DB field: entry status: proposed, reviewed, approved. */
    private String m_status = "";
    private String m_domain = "";
    private String m_project = "";
    private HashMap queryMap = new HashMap();

    /**
     * Field constraints to be evaluated in software, contains
     * ExportOptions.FilterCondition objects.
     */
    private ArrayList m_conditions = new ArrayList();

    //
    // Constructors
    //

    public EntryFilter()
    {
    }

    public EntryFilter(ExportOptions.FilterOptions p_options)
    {
        m_createdBy = p_options.m_createdBy;
        m_modifiedBy = p_options.m_modifiedBy;
        m_createdAfter = p_options.m_createdAfter;
        m_createdBefore = p_options.m_createdBefore;
        m_modifiedAfter = p_options.m_modifiedAfter;
        m_modifiedBefore = p_options.m_modifiedBefore;
        m_status = p_options.m_status;
        m_domain = p_options.m_domain;
        m_project = p_options.m_project;

        m_conditions.clear();
        m_conditions.addAll(p_options.m_conditions);
    }

    //
    // Public Methods
    //

    public String getCreatedBy()
    {
        return m_createdBy;
    }

    public boolean isCreatedBySet()
    {
        return isSet(m_createdBy);
    }

    public String getModifiedBy()
    {
        return m_modifiedBy;
    }

    public boolean isModifiedBySet()
    {
        return isSet(m_modifiedBy);
    }

    public String getCreatedAfter()
    {
        return m_createdAfter;
    }

    public boolean isCreatedAfterSet()
    {
        return isSet(m_createdAfter);
    }

    public String getCreatedBefore()
    {
        return m_createdBefore;
    }

    public boolean isCreatedBeforeSet()
    {
        return isSet(m_createdBefore);
    }

    public String getModifiedAfter()
    {
        return m_modifiedAfter;
    }

    public boolean isModifiedAfterSet()
    {
        return isSet(m_modifiedAfter);
    }

    public String getModifiedBefore()
    {
        return m_modifiedBefore;
    }

    public boolean isModifiedBeforeSet()
    {
        return isSet(m_modifiedBefore);
    }

    public String getStatus()
    {
        return m_status;
    }

    public boolean isStatusSet()
    {
        return isSet(m_status);
    }

    public String getDomain()
    {
        return m_domain;
    }

    public boolean isDomainSet()
    {
        return isSet(m_domain);
    }

    public String getProject()
    {
        return m_project;
    }

    public boolean isProjectSet()
    {
        return isSet(m_project);
    }

    // field constraints

    public ArrayList getConditions()
    {
        return m_conditions;
    }
    
    public HashMap getQueryMap()
    {
        return this.queryMap;
    }

    /**
     * Returns true if this filter can be applied in the DB, that is
     * in SQL select statements.
     */
    public boolean isDbFiltering()
    {
        if (isSet(m_createdBy) || isSet(m_modifiedBy) ||
            isSet(m_createdAfter) || isSet(m_createdBefore) ||
            isSet(m_modifiedAfter) || isSet(m_modifiedBefore) ||
            isSet(m_status) || isSet(m_domain) || isSet(m_project))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns true if this filter contains constraints that need to
     * be evaluated in software.
     */
    public boolean isSwFiltering()
    {
        return m_conditions.size() > 0;
    }

    /**
     * Returns a string of SQL expressions that apply an entry filter
     * in a SQL query.
     *
     * Example: " and C.Created_by='cvdl'"
     */
    public String getSqlExpression(String p_table, boolean isHibenate)
    {
        StringBuffer result = new StringBuffer();

        if (isCreatedBySet())
        {
            result.append(" and ");
            if (isHibenate)
            {
                result.append(p_table).append(".creationBy=:creationBy ");
                queryMap.put("creationBy", getCreatedBy());
            }
            else
            {
                result.append(p_table).append(".Created_By='");
                result.append(getCreatedBy()).append("'");
            }
        }

        if (isModifiedBySet())
        {
            result.append(" and ");
            if (isHibenate)
            {
                result.append(p_table).append(".modifyBy=:modifyBy");
                queryMap.put("modifyBy", getModifiedBy());
            }
            else
            {
                result.append(p_table).append(".Modified_By='");
                result.append(getModifiedBy()).append("'");
            }
        }
        
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        try
        {
            if (isCreatedAfterSet())
            {
                result.append(" and ");
                if (isHibenate)
                {
                    result.append(p_table).append(".creationDate>=:cdate1");
                    Timestamp ts = new Timestamp(format
                            .parse(getCreatedAfter()).getTime());
                    queryMap.put("cdate1", ts);
                }
                else
                {
                    result.append(p_table).append(".Created_On >= '");
                    result.append(getCreatedAfter()).append("'");
                }
            }

            if (isCreatedBeforeSet())
            {
                result.append(" and ");
                if (isHibenate)
                {
                    result.append(p_table).append(".creationDate<=:cdate2");
                    Timestamp ts = new Timestamp(format.parse(
                            getCreatedBefore()).getTime());
                    queryMap.put("cdate2", ts);
                }
                else
                {
                    result.append(p_table).append(".Created_On <= '");
                    result.append(getCreatedBefore()).append("'");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (isModifiedAfterSet())
            {
                result.append(" and ");
                if (isHibenate)
                {
                    result.append(p_table).append(".modifyDate>=:mdate1");
                    Timestamp ts = new Timestamp(format.parse(
                            getModifiedAfter()).getTime());
                    queryMap.put("mdate1", ts);
                }
                else
                {
                    result.append(p_table).append(".Modified_On >= '");
                    result.append(getModifiedAfter()).append("'");
                }
            }

            if (isModifiedBeforeSet())
            {
                result.append(" and ");
                if (isHibenate)
                {
                    result.append(p_table).append(".modifyDate<=:mdate2");
                    Timestamp ts = new Timestamp(format.parse(
                            getModifiedBefore()).getTime());
                    queryMap.put("mdate2", ts);
                }
                else
                {
                    result.append(p_table).append(".Modified_On <= '");
                    result.append(getModifiedBefore()).append("'");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (isStatusSet())
        {
            StringTokenizer toks =
                new StringTokenizer(getStatus(), ",");

            result.append(" and (");

            boolean first = true;
            while (toks.hasMoreElements())
            {
                String tok = toks.nextToken().trim();

                if (tok.length() == 0)
                {
                    break;
                }

                if (first)
                {
                    first = false;
                }
                else
                {
                    result.append(" or ");
                }

                result.append(p_table).append(".status='");
                result.append(tok).append("'");
            }

            result.append(") ");
        }

        if (isDomainSet())
        {
            result.append(" and ");
            result.append(p_table).append(".domain='");
            result.append(SqlUtil.quote(getDomain())).append("'");
        }

        if (isProjectSet())
        {
            result.append(" and ");
            result.append(p_table).append(".project='");
            result.append(SqlUtil.quote(getProject())).append("'");
        }

        return result.toString();
    }
    
    public String getSqlExpression(String p_table) {
        return getSqlExpression(p_table, false);
    }


    //
    // FILTERING METHODS
    //

    /**
     * Applies the db filter conditions to an in-memory Entry object.
     */
    public boolean evaluateDbFilter(Entry p_entry)
    {
        // TODO.
        return true;
    }

    /**
     * Applies the software filter conditions to an in-memory Entry object.
     */
    public boolean evaluateSwFilter(Entry p_entry)
        throws Exception
    {
        Element root = p_entry.getDom().getRootElement();

        // Mon Jan 24 15:43:22 2005 CvdL (Ambassador 6.3): all
        // conditions are connected with AND.

        for (int i = 0, max = m_conditions.size(); i < max; i++)
        {
            FilterCondition condition = (FilterCondition)m_conditions.get(i);

            boolean res = evaluateCondition(root, condition);
            if (!res)
            {
                return false;
            }
        }

        return true;
    }

    // TODO: comparisons at language level should lowercase using the
    // language's locale.
    private boolean evaluateCondition(Element p_entry,
        FilterCondition p_condition)
        //throws Exception
    {
        String level = p_condition.getLevel();
        String field = p_condition.getField();
        String operator = p_condition.getOperator();
        String value = p_condition.getValue();
        boolean matchCase = p_condition.isMatchCase();

        if (matchCase)
        {
            value = value.toLowerCase();
        }

        StringBuffer xpath = new StringBuffer();

        // TODO: identify concept level with an unambiguous token like
        // "_concept_".
        if (level.equalsIgnoreCase("concept"))
        {
            xpath.append("/conceptGrp/");
        }
        else
        {
            xpath.append("/conceptGrp/languageGrp[language/@name='");
            xpath.append(level);
            xpath.append("']/termGrp/");
        }

        if (field.equals("source"))
        {
            xpath.append("sourceGrp/source");
        }
        else if (field.equals("note"))
        {
            xpath.append("noteGrp/note");
        }
        else
        {
            xpath.append("descripGrp/descrip[@type='");
            xpath.append(field);
            xpath.append("']");
        }

        List nodes = p_entry.selectNodes(xpath.toString());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("xpath " + xpath + " returned " +
                nodes.size() + " nodes");
        }

        if (operator.equals(FilterCondition.OP_CONTAINS))
        {
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                Element node = (Element)nodes.get(i);
                String nodeVal = EntryUtils.getInnerText(node);

                if (matchCase)
                {
                    if (nodeVal.indexOf(value) >= 0)
                    {
                        return true;
                    }
                }
                else
                {
                    if (nodeVal.toLowerCase().indexOf(value) >= 0)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        else if (operator.equals(FilterCondition.OP_CONTAINSNOT))
        {
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                Element node = (Element)nodes.get(i);
                String nodeVal = EntryUtils.getInnerText(node);

                if (matchCase)
                {
                    if (nodeVal.indexOf(value) >= 0)
                    {
                        return false;
                    }
                }
                else
                {
                    if (nodeVal.toLowerCase().indexOf(value) >= 0)
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else if (operator.equals(FilterCondition.OP_EQUALS))
        {
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                Element node = (Element)nodes.get(i);
                String nodeVal = EntryUtils.getInnerText(node);

                if (matchCase)
                {
                    if (nodeVal.equals(value))
                    {
                        return true;
                    }
                }
                else
                {
                    if (nodeVal.toLowerCase().equals(value))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        else if (operator.equals(FilterCondition.OP_EQUALSNOT))
        {
            for (int i = 0, max = nodes.size(); i < max; i++)
            {
                Element node = (Element)nodes.get(i);
                String nodeVal = EntryUtils.getInnerText(node);

                if (matchCase)
                {
                    if (nodeVal.equals(value))
                    {
                        return false;
                    }
                }
                else
                {
                    if (nodeVal.toLowerCase().equals(value))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else if (operator.equals(FilterCondition.OP_LESSTHAN))
        {
            // TODO
            return true;
        }
        else if (operator.equals(FilterCondition.OP_GREATERTHAN))
        {
            // TODO
            return true;
        }
        else if (operator.equals(FilterCondition.OP_EXISTS))
        {
            return nodes.size() > 0;
        }
        else if (operator.equals(FilterCondition.OP_EXISTSNOT))
        {
            return nodes.size() == 0;
        }

        return false;
    }

    //
    // PRIVATE METHODS
    //

    private boolean isSet(String p_arg)
    {
        if (p_arg != null && p_arg.length() > 0)
        {
            return true;
        }

        return false;
    }
}
