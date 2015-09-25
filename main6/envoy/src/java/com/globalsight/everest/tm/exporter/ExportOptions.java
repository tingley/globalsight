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

package com.globalsight.everest.tm.exporter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.dom4j.Element;

import com.globalsight.exporter.ExporterException;
import com.globalsight.util.edit.EditUtil;
/**
 * This class collects all the options related to exporting
 * TM databases to files. There are the following sets of options:
 *
 * SelectOptions - selects entries from the termbase (all, by language);
 * OutputOptions - determines how entries are output to the file.
 */
public class ExportOptions
    extends com.globalsight.exporter.ExportOptions
{
    private static final Logger CATEGORY = Logger
            .getLogger(ExportOptions.class);

    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_XML = "xml";  // native format
    public static final String TYPE_TMX1 = "tmx1";  // TMX (level 1)
    public static final String TYPE_TMX2 = "tmx2";  // TMX (level 2)
    public static final String TYPE_TTMX = "ttmx";  // Trados TTX

    public static final String SELECT_ALL      = "all";
    public static final String SELECT_FILTERED = "filter";
    public static final String SELECT_FILTER_PROP_TYPE = "filterPropType";

    private String identifyKey = null;
    //
    // Private Classes
    //

    /**
     * SelectOptions specify which entries to select from the termbase
     * for export, and how duplicate entries are handled (reached
     * through synonymous terms).
     */
    public class SelectOptions
    {
        /** One of "all", "filter" */
        public String m_selectMode = "";

        /** If selectMode=filter, the filter spec */
        public String m_selectFilter = "";

        /** 
         * If true and TM data is from machine translation engine,
         * all "creationId" of exported tmx will be changed to "MT!"(GBS-832),
         * e.g. Original in db is "Google_MT",now should be "MT!";
         * If false,no changes. 
         */
        public boolean m_selectChangeCreationId = false;

		public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<selectOptions>");
            result.append("<selectMode>");
            result.append(EditUtil.encodeXmlEntities(m_selectMode));
            result.append("</selectMode>");
            result.append("<selectFilter>");
            result.append(EditUtil.encodeXmlEntities(m_selectFilter));
            result.append("</selectFilter>");
            result.append("<selectChangeCreationId>");
            result.append(EditUtil.encodeXmlEntities(Boolean.toString(m_selectChangeCreationId)));
            result.append("</selectChangeCreationId>");
            result.append("</selectOptions>");

            return result.toString();
        }
    }
    /**
     * Extended filter conditions that are not applied in the
     * database but evaluated in memory.
     *
     * Example: "English-Definition contains 'text'".
     *
     * m_operator can be one of the OP_XXX constants.
     */
    public class FilterCondition
    {
        static public final String OP_CONTAINS = "contains";
        static public final String OP_CONTAINSNOT = "containsnot";
        static public final String OP_EQUALS = "equals";
        static public final String OP_EQUALSNOT = "equalsnot";
        static public final String OP_LESSTHAN = "lessthan";
        static public final String OP_GREATERTHAN = "greaterthan";
        static public final String OP_EXISTS = "exists";
        static public final String OP_EXISTSNOT = "existsnot";

        public String m_level;
        public String m_field;
        public String m_operator;
        public String m_value;
        public String m_matchcase;

        public String getLevel()
        {
            return m_level;
        }

        public String getField()
        {
            return m_field;
        }

        public String getOperator()
        {
            return m_operator;
        }

        public String getValue()
        {
            return m_value;
        }

        public boolean isMatchCase()
        {
            return m_matchcase.equalsIgnoreCase("true");
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<condition>");
            result.append("<level>");
            result.append(EditUtil.encodeXmlEntities(m_level));
            result.append("</level>");
            result.append("<field>");
            result.append(EditUtil.encodeXmlEntities(m_field));
            result.append("</field>");
            result.append("<operator>");
            result.append(EditUtil.encodeXmlEntities(m_operator));
            result.append("</operator>");
            result.append("<value>");
            result.append(EditUtil.encodeXmlEntities(m_value));
            result.append("</value>");
            result.append("<matchcase>");
            result.append(EditUtil.encodeXmlEntities(m_matchcase));
            result.append("</matchcase>");
            result.append("</condition>");

            return result.toString();
        }
    }


    /**
     * FilterOptions specify how to filter the raw entry list selected
     * in SelectOptions.
     */
    public class FilterOptions
    {
        /** Creation/modification user */
        public String m_createdBy = "";
        public String m_modifiedBy = "";

        /**
         * Creation/modification date ranges. The format is currently
         * determined by the UI to be DD/MM/YYYY. The backend will
         * decode the dates.
         */
        public String m_createdAfter = "";
        public String m_createdBefore = "";
        public String m_modifiedAfter = "";
        public String m_modifiedBefore = "";
        public String m_lastUsageAfter = "";
        public String m_lastUsageBefore = "";
        public String m_language = "";
        public String m_projectName = "";
        
        /** Entry status: proposed, reviewed, approved. */
        public String m_status = "";
        public String m_domain = "";
        public String m_identifyKey = "";
        //tu id 
        public String m_tuId = "";
        //sid
        public String m_sid = "";
        //is regex
        public String m_regex = "";
        //job id
        public String m_jobId = "";
        /**
         * List of FilterCondition objects that describe filters that
         * are not applicable in the database but need to be evaluated
         * in memory.
         *
         * Example: "English-Definition contains 'text'".
         */
        public ArrayList m_conditions = new ArrayList();

        // TODO: field-specific filters based on termbase definition

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<filterOptions>");
            result.append("<createdby>");
            result.append(EditUtil.encodeXmlEntities(m_createdBy));
            result.append("</createdby>");
            result.append("<modifiedby>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedBy));
            result.append("</modifiedby>");
            result.append("<createdafter>");
            result.append(EditUtil.encodeXmlEntities(m_createdAfter));
            result.append("</createdafter>");
            result.append("<createdbefore>");
            result.append(EditUtil.encodeXmlEntities(m_createdBefore));
            result.append("</createdbefore>");
            result.append("<modifiedafter>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedAfter));
            result.append("</modifiedafter>");
            result.append("<modifiedbefore>");
            result.append(EditUtil.encodeXmlEntities(m_modifiedBefore));
            result.append("</modifiedbefore>");
            result.append("<lastusageafter>");
            result.append(EditUtil.encodeXmlEntities(m_lastUsageAfter));
            result.append("</lastusageafter>");
            result.append("<lastusagebefore>");
            result.append(EditUtil.encodeXmlEntities(m_lastUsageBefore));
            result.append("</lastusagebefore>");
            result.append("<tuId>");
            result.append(EditUtil.encodeXmlEntities(m_tuId));
            result.append("</tuId>");
            result.append("<stringId>");
			String sid = EditUtil.encodeXmlEntities(m_sid);
			if (sid.indexOf("\\") != -1)
			{
				sid = sid.replace("\\", "\\\\");
			}
            result.append(sid);
            result.append("</stringId>");
            result.append("<regex>");
            result.append(EditUtil.encodeXmlEntities(m_regex));
            result.append("</regex>");
            result.append("<jobId>");
            result.append(EditUtil.encodeXmlEntities(m_jobId));
            result.append("</jobId>");
            //GBS-3907
            result.append("<language>");
            result.append(EditUtil.encodeXmlEntities(m_language));
            result.append("</language>");
            result.append("<projectName>");
            result.append(EditUtil.encodeXmlEntities(m_projectName));
            result.append("</projectName>");
            
            result.append("<status>");
            result.append(EditUtil.encodeXmlEntities(m_status));
            result.append("</status>");
            result.append("<domain>");
            result.append(EditUtil.encodeXmlEntities(m_domain));
            result.append("</domain>");
//            result.append("<project>");
//            result.append(EditUtil.encodeXmlEntities(m_project));
//            result.append("</project>");
            result.append("<identifyKey>");
            result.append(EditUtil.encodeXmlEntities(m_identifyKey));
            result.append("</identifyKey>");
            result.append("<conditions>");

            for (int i = 0, max = m_conditions.size(); i < max; i++)
            {
                result.append(((FilterCondition)m_conditions.get(i)).asXML());
            }

            result.append("</conditions>");

            result.append("</filterOptions>");

            return result.toString();
        }
    }
    

    /**
     * Specifies how entries are output to the file, e.g. field
     * mappings, field suppression, field addition.
     */
    public class OutputOptions
    {
        /**
         * For native export, export system fields or not. Allowed
         * values: "true", "false".
         */
        public String m_systemFields = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<outputOptions>");
            result.append("<systemFields>");
            result.append(EditUtil.encodeXmlEntities(m_systemFields));
            result.append("</systemFields>");
            result.append("</outputOptions>");

            return result.toString();
        }
    }
    
    public class JobAttributeOptions
    {
    	public Set<String> jobAttributeSet = new HashSet<String>();
    	
    	public String asXML()
    	{
    		StringBuffer result = new StringBuffer();
    		
    		result.append("<attributes>");
    		for(String keyAndValue : jobAttributeSet)
    		{
    			String key = keyAndValue.substring(0,keyAndValue.indexOf(":"));
    			String value = keyAndValue.substring(keyAndValue.indexOf(":") + 1);
    			result.append("<attribute>");
    			result.append("<key>");
    			result.append(EditUtil.encodeXmlEntities(key));
    			result.append("</key>");
    			result.append("<value>");
    			result.append(EditUtil.encodeXmlEntities(value));
    			result.append("</value>");
    			result.append("</attribute>");
    		}
    		result.append("</attributes>");
    		return result.toString();
    	}
    }

    //
    // Private Members
    //
    private SelectOptions m_selectOptions = new SelectOptions();
    private OutputOptions m_outputOptions = new OutputOptions();
    private FilterOptions m_filterOptions = new FilterOptions();
    private JobAttributeOptions m_jobAttributeOptions = new JobAttributeOptions();

    //
    // Public Methods
    //
    public SelectOptions getSelectOptions()
    {
        return m_selectOptions;
    }

    public OutputOptions getOutputOptions()
    {
        return m_outputOptions;
    }
    public FilterOptions getFilterOptions()
    {
        return m_filterOptions;
    }
    public JobAttributeOptions getJobAttributeOptions()
    {
    	return m_jobAttributeOptions;
    }

    public String getSelectMode()
    {
        return m_selectOptions.m_selectMode;
    }

    public void setSelectMode(String p_mode)
    {
        m_selectOptions.m_selectMode = p_mode;
    }

    public void setSelectChangeCreationId(boolean m_selectChangeCreationId)
    {
        m_selectOptions.m_selectChangeCreationId = m_selectChangeCreationId;
    }

    public boolean getSelectChangeCreationId()
    {
        return m_selectOptions.m_selectChangeCreationId;
    }

    public String getSelectFilter()
    {
        return m_selectOptions.m_selectFilter;
    }

    public void setSelectFilter(String p_filter)
    {
        m_selectOptions.m_selectFilter = p_filter;
    }

    //
    // Overwritten Abstract Methods
    //

    /**
     * Returns an ExportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    protected String getOtherXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append(m_selectOptions.asXML());
        result.append(m_filterOptions.asXML());
        result.append(m_outputOptions.asXML());
        result.append(m_jobAttributeOptions.asXML());

        return result.toString();
    }

    /**
     * Reads and validates a ExportOptions XML string.
     */
    protected void initOther(Element p_root)
        throws ExporterException
    {
        try
        {
            Element elem = (Element)p_root.selectSingleNode("//selectOptions");
            m_selectOptions.m_selectMode = elem.elementText("selectMode");
            m_selectOptions.m_selectFilter = elem.elementText("selectFilter");
            m_selectOptions.m_selectChangeCreationId = 
                    Boolean.parseBoolean(elem.elementText("selectChangeCreationId"));
            
            elem = (Element)p_root.selectSingleNode("//filterOptions");
            m_filterOptions.m_createdAfter = elem.elementText("createdafter");
            m_filterOptions.m_createdBefore = elem.elementText("createdbefore");
            m_filterOptions.m_modifiedAfter = elem.elementText("modifiedafter");
            m_filterOptions.m_modifiedBefore = elem.elementText("modifiedbefore");
            m_filterOptions.m_lastUsageAfter = elem.elementText("lastusageafter");
            m_filterOptions.m_lastUsageBefore = elem.elementText("lastusagebefore");
            m_filterOptions.m_createdBy = elem.elementText("createdby");
            m_filterOptions.m_modifiedBy = elem.elementText("modifiedby");
            m_filterOptions.m_tuId = elem.elementText("tuId");
            m_filterOptions.m_sid = elem.elementText("stringId");
            m_filterOptions.m_regex = elem.elementText("regex");
            m_filterOptions.m_identifyKey = elem.elementText("identifyKey");
            m_filterOptions.m_jobId = elem.elementText("jobId");
            m_filterOptions.m_language = elem.elementText("language");
            m_filterOptions.m_projectName = elem.elementText("projectName");
            m_filterOptions.m_conditions.clear();
            

            elem = (Element)p_root.selectSingleNode("//outputOptions");
            m_outputOptions.m_systemFields = elem.elementText("systemFields");
            
            List<Element> attributeNodes = p_root.selectNodes("//attribute");
            Set<String> attributeSet = new HashSet<String>();
            if(attributeNodes!= null && attributeNodes.size() > 0)
            {
            	for(Element attributeElem: attributeNodes)
            	{
            		attributeSet.add(attributeElem.elementText("key") + ":" +
            				attributeElem.elementText("value"));
            	}
            }
            m_jobAttributeOptions.jobAttributeSet = attributeSet;
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }

    public String getIdentifyKey()
    {
        return identifyKey;
    }

    public void setIdentifyKey(String identifyKey)
    {
        this.identifyKey = identifyKey;
    }
}
