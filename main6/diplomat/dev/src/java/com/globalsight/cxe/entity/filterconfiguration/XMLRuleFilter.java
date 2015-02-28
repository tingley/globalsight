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
package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

/**
 * The filter for xml files
 * 
 */
public class XMLRuleFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private long xmlRuleId;
    private long companyId;
    private boolean convertHtmlEntity = false;
    private boolean useXmlRule = true;
    private String configXml = "";

    public XMLRuleFilter()
    {
    }

    public XMLRuleFilter(String filterName, String filterDescription,
            long xmlRuleId, long companyId, boolean convertHtmlEntity)
    {
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.xmlRuleId = xmlRuleId;
        this.companyId = companyId;
        this.convertHtmlEntity = convertHtmlEntity;
    }

    public XMLRuleFilter(long id, String filterName, String filterDescription,
            long xmlRuleId, long companyId, boolean convertHtmlEntity)
    {
        this(filterName, filterDescription, xmlRuleId, companyId,
                convertHtmlEntity);
        this.id = id;
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from XMLRuleFilter xr where xr.filterName =:filterName and xr.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from XMLRuleFilter xr where xr.id<>:filterId and xr.filterName =:filterName and xr.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getFilterTableName()
    {
        return FilterConstants.XMLRULE_TABLENAME;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from XMLRuleFilter xr where xr.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public XMLRuleFilter getFilter(long companyId, String filterName)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from XMLRuleFilter xr where xr.companyId =")
                .append(companyId).append(" and xr.filterName like '%")
                .append(filterName).append("%'");
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (XMLRuleFilter) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public long getBaseFilterId()
    {
        if (id > 0)
        {
            return BaseFilterManager.getBaseFilterIdByMapping(id,
                    FilterConstants.XMLRULE_TABLENAME);
        }
        else
        {
            return -2;
        }
    }

    public String toJSON(long companyId)
    {
        XmlFilterConfigParser parser = new XmlFilterConfigParser(configXml);
        boolean isParsed = false;
        try
        {
            parser.parserXml();
            isParsed = true;
        }
        catch (Exception e)
        {
            CATEGORY.error("configXml : " + configXml, e);
            isParsed = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.XMLRULE_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        // sb.append("\"xmlRuleName\":").append("\"").append(
        // FilterHelper.escape(getXmlRuleNameById(xmlRuleId)))
        // .append("\"").append(",");
        sb.append("\"xmlRules\":");
        sb.append("[");
        Iterator xmlRules = getAllXmlRuleFiles().iterator();
        boolean deleteComm = false;
        while (xmlRules.hasNext())
        {
            deleteComm = true;
            XmlRuleFile xmlRuleFile = (XmlRuleFile) xmlRules.next();
            sb.append("{");
            sb.append("\"xmlRuleId\":").append(xmlRuleFile.getId()).append(",");
            sb.append("\"xmlRuleName\":").append("\"")
                    .append(FilterHelper.escape(xmlRuleFile.getName()))
                    .append("\"");
            sb.append("}");

            sb.append(",");
        }
        if (deleteComm)
        {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("],");
        // base filter list
        // sb.append(BaseFilterManager.getAllBaseFiltersJson()).append(",");
        sb.append("\"xmlRuleId\":").append(xmlRuleId).append(",");
        sb.append("\"convertHtmlEntity\":").append(convertHtmlEntity)
                .append(",");
        sb.append("\"useXmlRule\":").append(useXmlRule).append(",");
        sb.append("\"extendedWhitespaceChars\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getExtendedWhiteSpaceChars()) : "").append("\",");
        sb.append("\"phConsolidationMode\":")
                .append(isParsed ? parser.getPhConsolidationMode()
                        : XmlFilterConstants.PH_CONSOLIDATE_DONOT).append(",");
        sb.append("\"phTrimMode\":")
                .append(isParsed ? parser.getPhTrimMode()
                        : XmlFilterConstants.PH_TRIM_DONOT).append(",");
        sb.append("\"nonasciiAs\":")
                .append(isParsed ? parser.getNonasciiAs()
                        : XmlFilterConstants.NON_ASCII_AS_CHARACTER)
                .append(",");
        sb.append("\"wsHandleMode\":")
                .append(isParsed ? parser.getWhiteSpaceHanldeMode()
                        : XmlFilterConstants.WHITESPACE_HANDLE_PRESERVE)
                .append(",");
        sb.append("\"emptyTagFormat\":")
                .append(isParsed ? parser.getEmptyTagFormat()
                        : XmlFilterConstants.EMPTY_TAG_FORMAT_PRESERVE)
                .append(",");
        sb.append("\"elementPostFilter\":").append("\"")
                .append(isParsed ? parser.getElementPostFilterTableName() : "")
                .append("\",");
        sb.append("\"elementPostFilterId\":").append("\"")
                .append(isParsed ? parser.getElementPostFilterId() : "")
                .append("\",");
        sb.append("\"cdataPostFilter\":").append("\"")
                .append(isParsed ? parser.getCdataPostFilterTableName() : "")
                .append("\",");
        sb.append("\"cdataPostFilterId\":").append("\"")
                .append(isParsed ? parser.getCdataPostFilterId() : "")
                .append("\",");
        sb.append("\"sidSupportTagName\":").append("\"")
                .append(isParsed ? parser.getSidTagName() : "").append("\",");
        sb.append("\"sidSupportAttName\":").append("\"")
                .append(isParsed ? parser.getSidAttrName() : "").append("\",");
        sb.append("\"isCheckWellFormed\":")
                .append(isParsed ? parser.isCheckWellFormed() : "false")
                .append(",");
        sb.append("\"isGerateLangInfo\":")
                .append(isParsed ? parser.isGerateLangInfo() : "false")
                .append(",");
        sb.append("\"preserveWsTags\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getWhiteSpacePreserveTagsJson()) : "[]").append("\",");
        sb.append("\"embTags\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getEmbeddedTagsJson()) : "[]").append("\",");
        sb.append("\"transAttrTags\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getTransAttrTagsJson()) : "[]").append("\",");
        sb.append("\"contentInclTags\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getContentInclTagsJson()) : "[]").append("\",");
        sb.append("\"cdataPostfilterTags\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getCDataPostFilterTagsJson()) : "[]").append("\",");
        sb.append("\"entities\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser.getEntitiesJson())
                        : "[]").append("\",");
        sb.append("\"processIns\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getProcessInsJson()) : "[]").append("\",");
        sb.append("\"internalTag\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getInternalTagJson()) : "[]").append("\",");
        sb.append("\"srcCmtXmlComment\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getSrcCmtXmlCommentJson()) : "[]").append("\",");
        sb.append("\"srcCmtXmlTag\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getSrcCmtXmlTagJson()) : "[]").append("\",");
        sb.append("\"baseFilterId\":").append("\"").append(getBaseFilterId())
                .append("\"");
        sb.append("}");

        return sb.toString();
    }

    public static Collection getAllXmlRuleFiles()
    {
        try
        {
            return ServerProxy.getXmlRuleFilePersistenceManager()
                    .getAllXmlRuleFiles();
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not get all the xml rules");
        }
        return new ArrayList();
    }

    private String getXmlRuleNameById(long ruleId)
    {
        String name = null;
        try
        {
            XmlRuleFile rule = ServerProxy.getXmlRuleFilePersistenceManager()
                    .readXmlRuleFile(ruleId);
            name = rule.getName();
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not get xml rule by id: " + ruleId);
        }
        return name;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription)
    {
        this.filterDescription = filterDescription;
    }

    public long getXmlRuleId()
    {
        return xmlRuleId;
    }

    public void setXmlRuleId(long xmlRuleId)
    {
        this.xmlRuleId = xmlRuleId;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public boolean isConvertHtmlEntity()
    {
        return convertHtmlEntity;
    }

    public void setConvertHtmlEntity(boolean convertHtmlEntity)
    {
        this.convertHtmlEntity = convertHtmlEntity;
    }

    public boolean isUseXmlRule()
    {
        return useXmlRule;
    }

    public void setUseXmlRule(boolean useXmlRule)
    {
        this.useXmlRule = useXmlRule;
    }

    public String getConfigXml()
    {
        return configXml;
    }

    public void setConfigXml(String configXml)
    {
        this.configXml = configXml;
    }

    public Map<String, String> getElementPostFilter()
    {
        return parseConfigXml(XmlFilterConstants.NODE_ELEMENT_POST_FILTER);
    }

    public Map<String, String> getCdataPostFilter()
    {
        return parseConfigXml(XmlFilterConstants.NODE_CDATA_POST_FILTER);
    }

    public Map<String, String> parseConfigXml(String nodeName)
    {
        XmlFilterConfigParser parser = new XmlFilterConfigParser(configXml);
        boolean isParsed = false;
        try
        {
            parser.parserXml();
            isParsed = true;
        }
        catch (Exception e)
        {
            CATEGORY.error("configXml : " + configXml, e);
            isParsed = false;
        }

        Map<String, String> result = new HashMap<String, String>();
        if (isParsed)
        {
            if (nodeName != null && nodeName.length() > 0)
            {
                if (nodeName
                        .equals(XmlFilterConstants.NODE_ELEMENT_POST_FILTER))
                {
                    result.put(FilterConstants.TABLENAME,
                            parser.getElementPostFilterTableName());
                    result.put(FilterConstants.TABLEID,
                            parser.getElementPostFilterId());
                }
                else if (nodeName
                        .equals(XmlFilterConstants.NODE_CDATA_POST_FILTER))
                {
                    result.put(FilterConstants.TABLENAME,
                            parser.getCdataPostFilterTableName());
                    result.put(FilterConstants.TABLEID,
                            parser.getCdataPostFilterId());
                }
            }
        }

        return result;
    }
}
