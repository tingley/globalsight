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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.RemoveInfo.FilterInfos;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class FilterHelper
{
    public static String escape(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\"", "\\\"")
                .replace("'", "\\\'");
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<FilterConfiguration> getAllFilterConfiguration(
            long companyId)
    {
        String hql = "from FilterConfiguration fc where fc.companyId="
                + companyId + " order by fc.name";
        return (ArrayList<FilterConfiguration>) HibernateUtil.search(hql);
    }

    public static String filterConfigurationsToJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        ArrayList<FilterConfiguration> filterConfigurations = getAllFilterConfiguration(companyId);
        ArrayList<FilterConfiguration> sorted = new ArrayList<FilterConfiguration>();

        for (int i = 0; i < filterConfigurations.size(); i++)
        {
            FilterConfiguration fc = filterConfigurations.get(i);

            if ("base_filter".equals(fc.getFilterTableName()))
            {
                sorted.add(0, fc);
            }
            else
            {
                sorted.add(fc);
            }
        }

        for (int i = 0; i < sorted.size(); i++)
        {
            sb.append(sorted.get(i).toJSON());
            if (i != sorted.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String allXmlRulesToJSON(long companyId)
    {
        Collection allXmlRules = XMLRuleFilter.getAllXmlRuleFiles();
        Iterator xmlRules = allXmlRules.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
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
        sb.append("]");
        return sb.toString();
    }

    public static boolean checkExistNew(String filterTableName,
            String filterName, long companyId)
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName);
        return filter.checkExistsNew(filterName, companyId);
    }

    public static boolean checkExistEdit(long filterId, String filterTableName,
            String filterName, long companyId)
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName);
        return filter.checkExistsEdit(filterId, filterName, companyId);
    }

    public static long saveJavaPropertiesFilter(String filterName,
            String filterDesc, boolean isSupportSid, boolean isUnicodeEscape,
            boolean isPreserveSpaces, long companyId, long secondFilterId,
            String secondFilterTableName, JSONArray internalTexts)
    {
        JavaPropertiesFilter filter = new JavaPropertiesFilter();
        filter.setCompanyId(companyId);
        filter.setEnableSidSupport(isSupportSid);
        filter.setEnableUnicodeEscape(isUnicodeEscape);
        filter.setFilterDescription(filterDesc);
        filter.setFilterName(filterName);
        filter.setEnablePreserveSpaces(isPreserveSpaces);
        filter.setSecondFilterId(secondFilterId);
        filter.setSecondFilterTableName(secondFilterTableName);
        filter.setInternalTextJson(internalTexts);

        HibernateUtil.saveOrUpdate(filter);
        return filter.getId();
    }

    public static long updateJavaPropertiesFilter(long fId, String filterName,
            String filterDesc, boolean isSupportSid, boolean isUnicodeEscape,
            boolean isPreserveSpaces, long companyId, long secondFilterId,
            String secondFilterTableName, JSONArray internalTexts)
    {
        JavaPropertiesFilter filter = null;
        String hql = "from JavaPropertiesFilter jp where jp.id='" + fId + "'";
        if (HibernateUtil.search(hql).size() > 0)
        {
            filter = (JavaPropertiesFilter) HibernateUtil.search(hql).get(0);
            filter.setCompanyId(companyId);
            filter.setEnableSidSupport(isSupportSid);
            filter.setEnableUnicodeEscape(isUnicodeEscape);
            filter.setEnablePreserveSpaces(isPreserveSpaces);
            filter.setFilterDescription(filterDesc);
            filter.setFilterName(filterName);
            filter.setSecondFilterId(secondFilterId);
            filter.setSecondFilterTableName(secondFilterTableName);
            filter.setInternalTextJson(internalTexts);
            HibernateUtil.update(filter);
        }

        return filter != null ? filter.getId() : -2;
    }

    public static long saveMSOfficeExcelFilter(String filterName,
            String filterDesc, long companyId, boolean altTranslate,
            boolean tabNamesTranslate, long contentPostFilterId,
            String contentPostFilterTableName)
    {
        MSOfficeExcelFilter filter = new MSOfficeExcelFilter();
        filter.setCompanyId(companyId);
        filter.setFilterDescription(filterDesc);
        filter.setFilterName(filterName);
        filter.setAltTranslate(altTranslate);
        filter.setTabNamesTranslate(tabNamesTranslate);
        filter.setContentPostFilterId(contentPostFilterId);
        filter.setContentPostFilterTableName(contentPostFilterTableName);

        HibernateUtil.saveOrUpdate(filter);
        return filter.getId();
    }

    public static long updateMSOfficeExcelFilter(long fId, String filterName,
            String filterDesc, long companyId, boolean altTranslate,
            boolean tabNamesTranslate, long contentPostFilterId,
            String contentPostFilterTableName)
    {
        String hql = "from MSOfficeExcelFilter me where me.id='" + fId + "'";
        if (HibernateUtil.search(hql).size() > 0)
        {
            MSOfficeExcelFilter filter = (MSOfficeExcelFilter) HibernateUtil
                    .search(hql).get(0);
            filter.setCompanyId(companyId);
            filter.setFilterDescription(filterDesc);
            filter.setFilterName(filterName);
            filter.setAltTranslate(altTranslate);
            filter.setTabNamesTranslate(tabNamesTranslate);
            filter.setContentPostFilterId(contentPostFilterId);
            filter.setContentPostFilterTableName(contentPostFilterTableName);
            HibernateUtil.update(filter);
            return filter.getId();
        }
        return -2;
    }

    public static long saveJavaScriptFilter(String filterName,
            String filterDesc, String jsFunctionText, long companyId,
            boolean enableUnicodeEscape)
    {
        JavaScriptFilter filter = new JavaScriptFilter();
        filter.setCompanyId(companyId);
        filter.setFilterDescription(filterDesc);
        filter.setFilterName(filterName);
        filter.setJsFunctionText(jsFunctionText);
        filter.setEnableUnicodeEscape(enableUnicodeEscape);
        HibernateUtil.saveOrUpdate(filter);
        return filter.getId();
    }

    public static void updateJavaScriptFilter(long filterId, String filterName,
            String filterDesc, String jsFunctionText, long companyId,
            boolean enableUnicodeEscape)
    {
        String hql = "from JavaScriptFilter js where js.id='" + filterId + "'";
        if (HibernateUtil.search(hql).size() > 0)
        {
            JavaScriptFilter filter = (JavaScriptFilter) HibernateUtil.search(
                    hql).get(0);
            filter.setCompanyId(companyId);
            filter.setFilterDescription(filterDesc);
            filter.setFilterName(filterName);
            filter.setJsFunctionText(jsFunctionText);
            filter.setEnableUnicodeEscape(enableUnicodeEscape);
            HibernateUtil.update(filter);
        }
    }

    public static boolean isFilterExist(String filterTableName, long filterId)
    {
        if (filterTableName == null || "".equals(filterTableName.trim()))
        {
            return false;
        }

        boolean isExist = false;

        try
        {
            String sql = "select id from " + filterTableName + " where id="
                    + filterId;
            if (HibernateUtil.searchWithSql(sql, null).size() > 0)
            {
                isExist = true;
            }
        }
        catch (Exception ex)
        {
        }

        return isExist;
    }

    public static Filter getFilter(String filterTableName, long filterId)
            throws Exception
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName);
        if (filter == null)
        {
            return null;
        }

        return HibernateUtil.get(filter.getClass(), filterId);
    }

    public static void deleteFilter(String filterTableName, long filterId, String m_userId )
            throws Exception
    {
        // Filter filter = MapOfTableNameAndSpecialFilter
        // .getFilterInstance(filterTableName);
        List<Filter> list = getFilterByMapping(filterId, filterTableName);
        Filter filter = list.get(0);
        String sql = "delete from " + filterTableName + " where id=" + filterId;
        HibernateUtil.executeSql(sql);
        OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                OperationLog.COMPONET_FILTER_CONFIGURATION,
                filter.getFilterName());
    }

    private static List<Filter> getFilterByMapping(long filterId,
            String filterTableName)
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName);
        String sql = "select * from " + filterTableName + " where id = ?";

        return (List<Filter>) HibernateUtil.searchWithSql(filter.getClass(),
                sql, filterId);
    }

    public static void deleteFilters(
            ArrayList<SpecialFilterToDelete> specialFilterToDeletes, String m_userId)
            throws Exception
    {
        for (int i = 0; i < specialFilterToDeletes.size(); i++)
        {
            String filterTableName = specialFilterToDeletes.get(i)
                    .getFilterTableName();
            long filterId = specialFilterToDeletes.get(i).getSpecialFilterId();
            deleteFilter(filterTableName, filterId, m_userId);
            BaseFilterManager
                    .deleteBaseFilterMapping(filterId, filterTableName);
        }
    }

    @SuppressWarnings("unchecked")
    public static String getFilterTableNameByKnownFormatId(long knownFormatId,
            long companyId)
    {
        String sql = "select filter_table_name from filter_configuration where company_id="
                + companyId
                + " and known_format_id like '%|"
                + knownFormatId
                + "|%'";
        List list = HibernateUtil.searchWithSql(sql, null);
        if (list.size() > 0)
        {
            return (String) list.get(0);
        }
        else
        {
            return null;
        }
    }

    public static ArrayList<Filter> getFiltersByKnownFormatId(
            long knownFormatId, long companyId)
    {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        String filterTableName = getFilterTableNameByKnownFormatId(
                knownFormatId, companyId);
        if (filterTableName != null)
        {
            Filter filter = MapOfTableNameAndSpecialFilter
                    .getFilterInstance(filterTableName);
            filters = filter.getFilters(companyId);
        }
        return filters;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<Filter>> getKnownFormatFilterMap(
            Collection allFormatTypes, long companyId)
    {
        Map<String, ArrayList<Filter>> map = new HashMap<String, ArrayList<Filter>>();
        Iterator<KnownFormatTypeImpl> it = allFormatTypes.iterator();
        while (it.hasNext())
        {
            KnownFormatTypeImpl formatType = it.next();
            ArrayList<Filter> filters = getFiltersByKnownFormatId(
                    formatType.getId(), companyId);
            map.put(formatType.getFormatType(), filters);
        }
        return map;
    }

    public static Filter getFilterById(ArrayList<Filter> filters, long filterId)
    {
        Filter filter = null;
        for (int i = 0; i < filters.size(); i++)
        {
            if (filters.get(i).getId() == filterId)
            {
                filter = filters.get(i);
            }
        }
        return filter;
    }

    public static boolean checkExistInFileProfile(Filter filter)
    {
        if (filter == null)
        {
            return false;
        }
        String hql = "from FileProfileImpl fp where fp.isActive = 'Y' "
                + "and fp.filterId = " + filter.getId()
                + " and fp.filterTableName='" + filter.getFilterTableName()
                + "'";
        return HibernateUtil.search(hql).size() > 0;
    }

    private static Set<JobImpl> getAllUsedJobs(Long filterId, String tableName)
    {
        Set<JobImpl> jobs = new HashSet<JobImpl>();

        String hql = "from FileProfileImpl fp where ";
        if (FilterConstants.QA_TABLENAME.equals(tableName))
        {
            hql += "fp.qaFilter.id = " + filterId;
        }
        else
        {
            hql += "fp.filterId = " + filterId + " and fp.filterTableName='"
                    + tableName + "'";
        }
        List<FileProfileImpl> fps = (List<FileProfileImpl>) HibernateUtil
                .search(hql);
        for (FileProfileImpl f : fps)
        {
            String hql2 = "select distinct r.job from RequestImpl r "
                    + "where r.job.state != 'CANCELLEd' and r.dataSourceId = "
                    + f.getId();
            jobs.addAll((Collection<JobImpl>) HibernateUtil.search(hql2));
        }

        return jobs;
    }

    public static RemoveInfo checkExistInFileProfile(
            ArrayList<SpecialFilterToDelete> specialFilterToDeletes)
    {
        StringBuilder sql = new StringBuilder(
                "select filter_id, filter_table_name, name, qa_filter_id from file_profile where is_active!='N'");
        SortUtil.sort(specialFilterToDeletes);

        // normal filters to delete
        boolean hasFilters = false;
        for (String tableName : FilterConstants.ALL_FILTER_TABLE_NAMES)
        {
            boolean added = false;
            StringBuilder filterConditions = new StringBuilder();
            if (!hasFilters)
            {
                filterConditions.append(" and (filter_table_name = '");
                filterConditions.append(tableName);
                filterConditions.append("' and filter_id in (");
            }
            else
            {
                filterConditions.append(" || (filter_table_name = '");
                filterConditions.append(tableName);
                filterConditions.append("' and filter_id in (");
            }
            for (int i = 0; i < specialFilterToDeletes.size(); i++)
            {
                SpecialFilterToDelete specialFilterToDelete = specialFilterToDeletes
                        .get(i);
                if (tableName
                        .equals(specialFilterToDelete.getFilterTableName()))
                {
                    added = true;
                    filterConditions.append(specialFilterToDelete
                            .getSpecialFilterId());
                    filterConditions.append(",");
                }
            }
            filterConditions = filterConditions.deleteCharAt(filterConditions
                    .length() - 1);
            filterConditions.append("))");
            if (added)
            {
                hasFilters = true;
                sql.append(filterConditions);
            }
        }
        // GBS-3697, qaFilter to delete
        boolean hasQAFilter = false;
        StringBuilder qaFilterConditions = new StringBuilder();
        if (hasFilters)
        {
            qaFilterConditions.append(" || qa_filter_id in (");
        }
        else
        {
            qaFilterConditions.append(" and qa_filter_id in (");
        }
        for (SpecialFilterToDelete specialFilterToDelete : specialFilterToDeletes)
        {
            if (FilterConstants.QA_TABLENAME.equals(specialFilterToDelete
                    .getFilterTableName()))
            {
                hasQAFilter = true;
                qaFilterConditions.append(specialFilterToDelete
                        .getSpecialFilterId());
                qaFilterConditions.append(",");
            }
        }
        if (hasQAFilter)
        {
            qaFilterConditions = qaFilterConditions
                    .deleteCharAt(qaFilterConditions.length() - 1);
            qaFilterConditions.append(")");
            sql.append(qaFilterConditions.toString());
        }

        List list = HibernateUtil.searchWithSql(sql.toString(), null);
        boolean isExistInFileProfile = list.size() > 0;
        RemoveInfo removeInfo = new RemoveInfo();
        removeInfo.setExistInFileProfile(isExistInFileProfile);

        List<FilterInfos> filterInfos = new ArrayList<FilterInfos>();
        for (int i = 0; i < list.size(); i++)
        {
            Object[] contents = (Object[]) list.get(i);
            FilterInfos filterInfo = null;
            if (hasQAFilter && contents[3] != null)
            {
                // qa_filter_id
                filterInfo = removeInfo.new FilterInfos(
                        Long.parseLong(contents[3].toString()),
                        FilterConstants.QA_TABLENAME, contents[2].toString());
            }
            else
            {
                // normal filter
                filterInfo = removeInfo.new FilterInfos(
                        Long.parseLong(contents[0].toString()),
                        contents[1].toString(), contents[2].toString());
            }
            filterInfos.add(filterInfo);
        }
        removeInfo.setFilterInfos(filterInfos);
        return removeInfo;
    }

    public static void checkUsedInJob(
            ArrayList<SpecialFilterToDelete> p_specialFilterToDeletes,
            RemoveInfo p_removeInfo, Long p_companyId)
    {
        Map<SpecialFilterToDelete, Set<JobImpl>> result = new HashMap<SpecialFilterToDelete, Set<JobImpl>>();
        for (SpecialFilterToDelete sf : p_specialFilterToDeletes)
        {
            Set<JobImpl> jobs = getAllUsedJobs(sf.getSpecialFilterId(),
                    sf.getFilterTableName());
            if (jobs.size() > 0)
            {
                result.put(sf, jobs);
            }
        }

        p_removeInfo.addFilterInJobs(result);
    }

    /*
     * Check the filter whether is used by other filters.
     */
    public static void checkExistInFiters(
            ArrayList<SpecialFilterToDelete> p_specialFilterToDeletes,
            RemoveInfo p_removeInfo, Long p_companyId)
    {
        // 1. Separates the filter by type.
        List<Long> htmlIDList = new ArrayList<Long>();
        List<Long> xmlIDList = new ArrayList<Long>();
        List<Long> baseIdList = new ArrayList<Long>();
        for (SpecialFilterToDelete f : p_specialFilterToDeletes)
        {
            if (FilterConstants.HTML_TABLENAME.equals(f.getFilterTableName()))
            {
                htmlIDList.add(f.getSpecialFilterId());
            }
            else if (FilterConstants.XMLRULE_TABLENAME.equals(f
                    .getFilterTableName()))
            {
                xmlIDList.add(f.getSpecialFilterId());
            }
            else if (FilterConstants.BASE_TABLENAME.equals(f
                    .getFilterTableName()))
            {
                baseIdList.add(f.getSpecialFilterId());
            }
        }

        // 2. Checks the filter list after separate.
        checkHTMLFilterIsUsedByFiter(FilterConstants.JAVAPROPERTIES_TABLENAME,
                htmlIDList, p_removeInfo, p_companyId);
        checkHTMLFilterIsUsedByFiter(FilterConstants.MSOFFICEDOC_TABLENAME,
                htmlIDList, p_removeInfo, p_companyId);
        checkHTMLFilterIsUsedByFiter(FilterConstants.MSOFFICEEXCEL_TABLENAME,
                htmlIDList, p_removeInfo, p_companyId);
        checkHTMLFilterIsUsedByFiter(FilterConstants.MSOFFICEPPT_TABLENAME,
                htmlIDList, p_removeInfo, p_companyId);
        checkHTMLFilterIsUsedByFiter(FilterConstants.XMLRULE_TABLENAME,
                htmlIDList, p_removeInfo, p_companyId);
        checkHTMLFilterIsUsedByFiter(FilterConstants.PO_TABLENAME, htmlIDList,
                p_removeInfo, p_companyId);

        checkXMLFilterIsUsedByFiter(FilterConstants.PO_TABLENAME, xmlIDList,
                p_removeInfo, p_companyId);

        checkBaseFilterIsUsedByFiter(baseIdList, p_removeInfo, p_companyId);
    }

    public static void checkHTMLFilterIsUsedByFiter(
            String p_usedfilterTableName, List<Long> p_htmlFilteIDList,
            RemoveInfo p_removeInfo, Long p_companyId)
    {
        if (p_htmlFilteIDList == null || p_htmlFilteIDList.size() < 1)
            return;

        if (p_usedfilterTableName.equals(FilterConstants.XMLRULE_TABLENAME))
        {
            // Gets all XML filter, and then check one by one.
            List<Filter> list = getFiltersByTableName(p_usedfilterTableName,
                    p_companyId);
            for (int i = 0; i < list.size(); i++)
            {
                XMLRuleFilter filter = (XMLRuleFilter) list.get(i);
                Map<String, String> nodeMap = new HashMap<String, String>();

                // Checks Element pose_filter
                nodeMap = filter.getElementPostFilter();
                Long tableID = null;
                if (nodeMap.get(FilterConstants.TABLEID) != null
                        && !nodeMap.get(FilterConstants.TABLEID).equals(""))
                {
                    tableID = Long
                            .valueOf(nodeMap.get(FilterConstants.TABLEID));
                }
                String tableName = (String) nodeMap
                        .get(FilterConstants.TABLENAME);
                if (tableID != null && !tableID.equals("") && tableID > 0
                        && tableName != null && tableName.length() > 0)
                {
                    HtmlFilter htmlFilter = getHtmlFilter(Long.valueOf(tableID));
                    if (p_htmlFilteIDList.contains(tableID)
                            && htmlFilter != null
                            && tableName
                                    .equals(htmlFilter.getFilterTableName()))
                    {
                        if (!p_removeInfo.isUsedByFilters())
                            p_removeInfo.setUsedByFilters(true);
                        FilterInfos filterInfo = p_removeInfo.new FilterInfos(
                                Long.valueOf(tableID), tableName,
                                String.valueOf(filter.getId()),
                                p_usedfilterTableName);
                        p_removeInfo.addUsedFilters(filterInfo);
                    }
                }

                // Checks CDATA pose_filter
                nodeMap = filter.getCdataPostFilter();
                Long cdataPostFilterTableID = null;
                if (nodeMap.get(FilterConstants.TABLEID) != null
                        && !nodeMap.get(FilterConstants.TABLEID).equals(""))
                {
                    cdataPostFilterTableID = Long.valueOf(nodeMap
                            .get(FilterConstants.TABLEID));
                }
                String cdataPostFilterTableName = (String) nodeMap
                        .get(FilterConstants.TABLENAME);
                if (tableID != null && !tableID.equals("")
                        && cdataPostFilterTableID != null
                        && !cdataPostFilterTableID.equals("")
                        && tableID.equals(cdataPostFilterTableID)
                        && tableName.equalsIgnoreCase(cdataPostFilterTableName))
                {
                    break;
                }
                if (tableID != null && !tableID.equals("") && tableID > 0
                        && tableName != null && tableName.length() > 0)
                {
                    HtmlFilter htmlFilter = getHtmlFilter(Long.valueOf(tableID));
                    if (p_htmlFilteIDList.contains(tableID)
                            && htmlFilter != null
                            && tableName
                                    .equals(htmlFilter.getFilterTableName()))
                    {
                        if (!p_removeInfo.isUsedByFilters())
                            p_removeInfo.setUsedByFilters(true);
                        FilterInfos filterInfo = p_removeInfo.new FilterInfos(
                                Long.valueOf(cdataPostFilterTableID),
                                cdataPostFilterTableName, String.valueOf(filter
                                        .getId()), p_usedfilterTableName);
                        p_removeInfo.addUsedFilters(filterInfo);
                    }
                }
            }
        }
        else
        {
            for (Long hID : p_htmlFilteIDList)
            {
                String sql = "select id, filter_name from "
                        + p_usedfilterTableName
                        + " where SECOND_FILTER_ID = :SFID"
                        + " and COMPANY_ID = :CID";
                if (p_usedfilterTableName
                        .equals(FilterConstants.MSOFFICEDOC_TABLENAME)
                        || p_usedfilterTableName
                                .equals(FilterConstants.MSOFFICEEXCEL_TABLENAME)
                        || p_usedfilterTableName
                                .equals(FilterConstants.MSOFFICEPPT_TABLENAME))
                {
                    sql = "select id, filter_name from "
                            + p_usedfilterTableName
                            + " where CONTENT_POST_FILTER_ID = :SFID"
                            + " and CONTENT_POST_FILTER_TABLE_NAME = \""
                            + FilterConstants.HTML_TABLENAME
                            + "\" and COMPANY_ID = :CID";
                }

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("SFID", hID);
                map.put("CID", p_companyId);
                List<?> list = HibernateUtil.searchWithSql(sql, map);
                boolean isExist = list.size() > 0;
                if (isExist)
                {
                    if (!p_removeInfo.isUsedByFilters())
                        p_removeInfo.setUsedByFilters(isExist);
                    FilterInfos filterInfo = null;
                    for (int i = 0; i < list.size(); i++)
                    {
                        Object[] contents = (Object[]) list.get(i);
                        filterInfo = p_removeInfo.new FilterInfos(hID,
                                FilterConstants.HTML_TABLENAME,
                                contents[0].toString(), p_usedfilterTableName);
                        p_removeInfo.addUsedFilters(filterInfo);
                    }
                }
            }
        }
    }

    public static void checkXMLFilterIsUsedByFiter(
            String p_usedfilterTableName, List<Long> p_filteIDList,
            RemoveInfo p_removeInfo, Long p_companyId)
    {
        if (p_filteIDList == null || p_filteIDList.size() < 1)
            return;

        for (Long hID : p_filteIDList)
        {
            String sql = "select id, filter_name from " + p_usedfilterTableName
                    + " where SECOND_FILTER_ID = :SFID"
                    + " and COMPANY_ID = :CID";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("SFID", hID);
            map.put("CID", p_companyId);
            List<?> list = HibernateUtil.searchWithSql(sql, map);
            boolean isExist = list.size() > 0;
            if (isExist)
            {
                if (!p_removeInfo.isUsedByFilters())
                    p_removeInfo.setUsedByFilters(isExist);
                FilterInfos filterInfo = null;
                for (int i = 0; i < list.size(); i++)
                {
                    Object[] contents = (Object[]) list.get(i);
                    filterInfo = p_removeInfo.new FilterInfos(hID,
                            FilterConstants.XMLRULE_TABLENAME,
                            contents[0].toString(), p_usedfilterTableName);
                    p_removeInfo.addUsedFilters(filterInfo);
                }
            }
        }
    }

    public static void checkBaseFilterIsUsedByFiter(List<Long> p_filteIDList,
            RemoveInfo p_removeInfo, Long p_companyId)
    {
        if (p_filteIDList == null || p_filteIDList.size() < 1)
            return;

        for (Long hID : p_filteIDList)
        {
            List<BaseFilterMapping> mappings = BaseFilterManager
                    .getBaseFilterMapping(hID);
            if (mappings != null && mappings.size() > 0)
            {
                FilterInfos filterInfo = null;
                List<FilterInfos> usedFilters = new ArrayList<FilterInfos>();
                for (int i = 0; i < mappings.size(); i++)
                {
                    BaseFilterMapping mapping = mappings.get(i);
                    long filterId = mapping.getFilterId();
                    String filterTableName = mapping.getFilterTableName();
                    if (isFilterExist(filterTableName, filterId))
                    {
                        filterInfo = p_removeInfo.new FilterInfos(hID,
                                FilterConstants.BASE_TABLENAME, ""
                                        + mapping.getFilterId(),
                                mapping.getFilterTableName());
                        usedFilters.add(filterInfo);
                    }
                    else
                    {
                        BaseFilterManager.deleteBaseFilterMapping(filterId,
                                filterTableName);
                    }
                }

                if (usedFilters.size() > 0)
                {
                    if (!p_removeInfo.isUsedByFilters())
                        p_removeInfo.setUsedByFilters(true);

                    p_removeInfo.addUsedFilters(usedFilters);
                }
            }
        }
    }

    public static long saveXmlRuleFilter(XMLRuleFilter filter)
    {
        HibernateUtil.saveOrUpdate(filter);
        return filter.getId();
    }

    public static void updateFilter(Filter filter)
    {
        HibernateUtil.update(filter);
    }

    public static HtmlFilter getHtmlFilter(long filterId)
    {
        return HibernateUtil.get(HtmlFilter.class, filterId);
    }

    public static XMLRuleFilter getXmlFilter(long filterId)
    {
        return HibernateUtil.get(XMLRuleFilter.class, filterId);
    }

    public static PlainTextFilter getPlainTextFilter(long filterId)
    {
        return HibernateUtil.get(PlainTextFilter.class, filterId);
    }

    public static long saveFilter(Filter filter)
    {
        HibernateUtil.saveOrUpdate(filter);
        return filter.getId();
    }

    public static String removeComma(String parameter)
    {
        if (parameter == null)
        {
            return null;
        }
        if ("".equals(parameter.trim()))
        {
            return "";
        }
        parameter = parameter.trim().replaceAll(",+", ",");
        if (parameter.startsWith(","))
        {
            parameter = parameter.substring(1);
        }
        if (parameter.endsWith(","))
        {
            parameter = parameter.substring(0, parameter.lastIndexOf(','));
        }
        return parameter;
    }

    public static void main(String[] args)
    {
        // System.out.println(filterConfigurationsToJSON(1000));
        System.out.println(removeComma(",,,,a,,,,,,br,,,,,"));
    }

    public static List getXmlRuleFilters(String id, String companyId)
    {
        String hql = "from XMLRuleFilter xr where xr.xmlRuleId=" + id
                + " and xr.companyId=" + companyId;
        return HibernateUtil.search(hql);
    }

    @SuppressWarnings("unchecked")
    public static List<Filter> getFiltersByTableName(String filterTableName,
            long companyId)
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName);
        String sql = "select * from " + filterTableName
                + " where company_id = ?";

        return (List<Filter>) HibernateUtil.searchWithSql(filter.getClass(),
                sql, companyId);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getallFiltersLikeName(String param)
    {
        Map<String, String> filters = new HashMap<String, String>();
        Collection<String> c = MapOfTableNameAndSpecialFilter.getAllFilter();
        for (String filterTableName : c)
        {
            Filter filter = MapOfTableNameAndSpecialFilter
                    .getFilterInstance(filterTableName);
            String sql = "select * from " + filterTableName
                    + " where filter_name like '%" + param + "%'";

            List<Filter> fList = (List<Filter>) HibernateUtil.getSession()
                    .createSQLQuery(sql).addEntity(filter.getClass()).list();
            for (Filter f : fList)
            {
                filters.put(filterTableName + f.getId(), f.getFilterName());
            }
        }

        return filters;
    }

    public static String getFiltersJson(String filterTableName, long companyId)
    {
        List<Filter> filters = getFiltersByTableName(filterTableName, companyId);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < filters.size(); i++)
        {
            Filter filter = filters.get(i);
            sb.append(filter.toJSON(companyId));
            if (i != filters.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String isFilterValid(HttpServletRequest request,
            String filterTableName)
    {
        HttpSession session = request.getSession(false);
        ResourceBundle bundle = PageHandler.getBundle(session);
        String result = "true";

        // xml_rule_filter
        if (filterTableName.equals(FilterConstants.XMLRULE_TABLENAME))
        {
            String extendedWhitespaceChars = request
                    .getParameter("extendedWhitespaceChars");
            if (extendedWhitespaceChars != null
                    && !"".equals(extendedWhitespaceChars.trim()))
            {
                extendedWhitespaceChars = extendedWhitespaceChars.trim();
                String[] chars = extendedWhitespaceChars.split("\\s+");
                for (String ch : chars)
                {
                    if (ch.length() > 1)
                    {
                        result = MessageFormat
                                .format(bundle
                                        .getString("lb_filter_msg_invalid_extended_space_char"),
                                        ch);
                        break;
                    }
                }
            }

            return result;
        }

        // TODO check: is filter valid

        return "true";
    }

    /**
     * Sort method
     * 
     * @param list
     */
    public static void sort(List<String> list)
    {
        SortUtil.sort(list, new StringComparator(Locale.getDefault()));
    }
}
