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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.everest.webapp.pagehandler.administration.users.ExportUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class ExportFilterHelper
{
    private static final Logger logger = Logger.getLogger(ExportFilterHelper.class);

    private static final String SQL_SELECT_XML_FILTER = "select * from "
            + FilterConstants.XMLRULE_TABLENAME + " where id = ? and company_id = ?";
    private static final String SQL_SELECT_XML_RULE = "select * from xml_rule "
            + " where id = ? and company_id = ?";
    private static final String SQL_SELECT_BASE_FILTER_MAPPING = "select * from base_filter_mapping "
            + " where filter_table_name = ? and filter_id = ?";
    private static final String SQL_SELECT_BASE_FILTER_MAPPING_DATA = "select * from base_filter_mapping "
            + " where id = ?";
    private final static String NEW_LINE = "\r\n";
    private static EditUtil editUtil = new EditUtil();

    /**
     * export filters
     * 
     * @param List
     *            <SpecialFilterToDelete>
     * @param companyId
     * */
    public static void exportFilters(String userName, HttpServletResponse response,
            List<SpecialFilterToExport> specialFilterToexportList, Long companyId)
    {
        // create property file
        File propertyFile = createPropertyFile(userName, companyId);
        // get property file name
        String fileName = propertyFile.getName();

        // get all need download filters
        Set<String> filterSet = findAllNeedDownloadFilter(specialFilterToexportList, companyId);
        Iterator it = filterSet.iterator();
        while (it.hasNext())
        {
            String tableNameAndID = (String) it.next();
            String[] tableNameAndIDArr = tableNameAndID.split("\\.");
            String tableName = tableNameAndIDArr[0];
            Long filterId = Long.parseLong(tableNameAndIDArr[1]);

            if (FilterConstants.FM_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputFrameMakerFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.HTML_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputHtmlFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.INDD_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputInDesignFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.BASE_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputInternalFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.JAVAPROPERTIES_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and interanlTextFilter
                propertiesInputJavaPropertiesFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.JAVASCRIPT_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputJavaScriptFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.JSP_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputJspFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.OFFICE2010_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and interanlTextFilter
                propertiesInputOffice2010Filter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.MSOFFICEDOC_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and interanlTextFilter
                propertiesInputMsOfficeDocFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.MSOFFICEEXCEL_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and interanlTextFilter
                propertiesInputMsOfficeExcelFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.MSOFFICEPPT_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and interanlTextFilter
                propertiesInputMsOfficePptFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.OPENOFFICE_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInpuOpenOfficeFiltert(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.PLAINTEXT_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use Internal filter
                propertiesInputPlainTextFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.PO_TABLENAME.equalsIgnoreCase(tableName))
            {
                // Determine whether to use htmlFilter and xmlRulefilter
                propertiesInputPOFilter(propertyFile, tableName, filterId, companyId);
            }
            else if (FilterConstants.XMLRULE_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputxmlRuleFilter(propertyFile, tableName, filterId, companyId);
            }
            else if ("base_filter_mapping".equalsIgnoreCase(tableName))
            {
                propertiesInputBaseFilterMapping(propertyFile, filterId);
            }
            else if ("xml_rule".equalsIgnoreCase(tableName))
            {
                propertiesInputxmlRule(propertyFile, filterId, companyId);
            }
            else if (FilterConstants.QA_TABLENAME.equalsIgnoreCase(tableName))
            {
                propertiesInputQAFilter(propertyFile, tableName, filterId, companyId);
            }
        }

        ExportUtil.writeToResponse(response, propertyFile, fileName);
    }

    /**
     * Create property file
     * 
     * @param userName
     * @param companyId
     * @return File
     * */
    private static File createPropertyFile(String userName, Long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath()).append(File.separator)
                .append("GlobalSight").append(File.separator).append("FilterConfigurations")
                .append(File.separator).append("export");
        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "FilterConfiguration_" + userName + "_" + companyId + "_"
                + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Find all you need to download the filter
     * 
     * @param specialFilterToexportList
     * @return List<SpecialFilterToExport>
     * */
    private static Set<String> findAllNeedDownloadFilter(
            List<SpecialFilterToExport> specialFilterToexportList, Long companyId)
    {
        Set<String> filterSet = new HashSet<String>();
        Filter filter = null;
        BaseFilterMapping baseFilterMapping = null;
        String idAndTableName = null;
        for (SpecialFilterToExport ftr : specialFilterToexportList)
        {
            String filterTableName = ftr.getFilterTableName();
            Long specialFilterId = ftr.getSpecialFilterId();

            idAndTableName = filterTableName + "." + specialFilterId;
            filterSet.add(idAndTableName);

            filter = selectFilterDataFromDataBase(ftr.getFilterTableName(),
                    ftr.getSpecialFilterId(), companyId);
            baseFilterMapping = checkInternalFilterIsUsedByFilter(ftr.getFilterTableName(),
                    ftr.getSpecialFilterId());

            if (filterTableName.equalsIgnoreCase(FilterConstants.JAVAPROPERTIES_TABLENAME))
            {
                JavaPropertiesFilter javaPropertiesFilter = (JavaPropertiesFilter) filter;
                if (javaPropertiesFilter.getSecondFilterTableName() != null
                        && !"".equals(javaPropertiesFilter.getSecondFilterTableName()))
                {
                    filterSet.add(javaPropertiesFilter.getSecondFilterTableName() + "."
                            + javaPropertiesFilter.getSecondFilterId());

                    // Judge whether the html_filter reference base_filter
                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                            javaPropertiesFilter.getSecondFilterTableName(),
                            javaPropertiesFilter.getSecondFilterId());

                    if (bfm != null)
                    {
                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                        filterSet.add(FilterConstants.BASE_TABLENAME + "." + bfm.getBaseFilterId());
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.PLAINTEXT_TABLENAME))
            {
                try
                {
                    PlainTextFilter plainTextFilter = (PlainTextFilter) filter;
                    PlainTextFilterParser parser = new PlainTextFilterParser(plainTextFilter);
                    parser.parserXml();
                    String postFilterTableName = parser.getElementPostFilterTableName();
                    String postFilterId = parser.getElementPostFilterId();

                    if (!StringUtil.isEmpty(postFilterTableName)
                            && !StringUtil.isEmpty(postFilterId))
                    {
                        filterSet.add(postFilterTableName + "." + postFilterId);
                        if (postFilterTableName.equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                        {
                            BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                    postFilterTableName, Long.parseLong(postFilterId));

                            if (bfm != null)
                            {
                                filterSet.add("base_filter_mapping" + "." + bfm.getId());
                                filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                        + bfm.getBaseFilterId());
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("An error occurred while processing plain text filter.");
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.OFFICE2010_TABLENAME))
            {
                MSOffice2010Filter msOffice2010Filter = (MSOffice2010Filter) filter;
                if (msOffice2010Filter.getContentPostFilterTableName() != null)
                {
                    filterSet.add(msOffice2010Filter.getContentPostFilterTableName() + "."
                            + msOffice2010Filter.getContentPostFilterId());

                    // Judge whether the html_filter reference base_filter
                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                            msOffice2010Filter.getContentPostFilterTableName(),
                            msOffice2010Filter.getContentPostFilterId());

                    if (bfm != null)
                    {
                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                        filterSet.add(FilterConstants.BASE_TABLENAME + "." + bfm.getBaseFilterId());
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.MSOFFICEDOC_TABLENAME))
            {
                MSOfficeDocFilter msOfficeDocFilter = (MSOfficeDocFilter) filter;
                if (msOfficeDocFilter.getContentPostFilterTableName() != null)
                {
                    filterSet.add(msOfficeDocFilter.getContentPostFilterTableName() + "."
                            + msOfficeDocFilter.getContentPostFilterId());

                    // Judge whether the html_filter reference base_filter
                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                            msOfficeDocFilter.getContentPostFilterTableName(),
                            msOfficeDocFilter.getContentPostFilterId());

                    if (bfm != null)
                    {
                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                        filterSet.add(FilterConstants.BASE_TABLENAME + "." + bfm.getBaseFilterId());
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.MSOFFICEEXCEL_TABLENAME))
            {
                MSOfficeExcelFilter msOfficeExcelFilter = (MSOfficeExcelFilter) filter;
                if (msOfficeExcelFilter.getContentPostFilterTableName() != null)
                {
                    filterSet.add(msOfficeExcelFilter.getContentPostFilterTableName() + "."
                            + msOfficeExcelFilter.getContentPostFilterId());

                    // Judge whether the html_filter reference base_filter
                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                            msOfficeExcelFilter.getContentPostFilterTableName(),
                            msOfficeExcelFilter.getContentPostFilterId());

                    if (bfm != null)
                    {
                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                        filterSet.add(FilterConstants.BASE_TABLENAME + "." + bfm.getBaseFilterId());
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.MSOFFICEPPT_TABLENAME))
            {
                MSOfficePPTFilter msOfficePPTFilter = (MSOfficePPTFilter) filter;
                if (msOfficePPTFilter.getContentPostFilterTableName() != null)
                {
                    filterSet.add(msOfficePPTFilter.getContentPostFilterTableName() + "."
                            + msOfficePPTFilter.getContentPostFilterId());

                    // Judge whether the html_filter reference base_filter
                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                            msOfficePPTFilter.getContentPostFilterTableName(),
                            msOfficePPTFilter.getContentPostFilterId());

                    if (bfm != null)
                    {
                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                        filterSet.add(FilterConstants.BASE_TABLENAME + "." + bfm.getBaseFilterId());
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.PO_TABLENAME))
            {
                POFilter poFilter = (POFilter) filter;
                if (poFilter.getSecondFilterTableName() != null
                        && !"".equals(poFilter.getSecondFilterTableName()))
                {
                    filterSet.add(poFilter.getSecondFilterTableName() + "."
                            + poFilter.getSecondFilterId());

                    // Judge whether the po_filter reference html_filter
                    if (poFilter.getSecondFilterTableName().equalsIgnoreCase(
                            FilterConstants.HTML_TABLENAME))
                    {
                        // Judge whether the html_filter reference base_filter
                        BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                poFilter.getSecondFilterTableName(), poFilter.getSecondFilterId());

                        if (bfm != null)
                        {
                            filterSet.add("base_filter_mapping" + "." + bfm.getId());
                            filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                    + bfm.getBaseFilterId());
                        }
                    }
                    // Judge whether the po_filter reference xml_rule_filter
                    else if (poFilter.getSecondFilterTableName().equalsIgnoreCase(
                            FilterConstants.XMLRULE_TABLENAME))
                    {
                        // Judge whether the xml_rule_filter reference
                        // base_filter
                        BaseFilterMapping bfmXmlRuleFilter = checkInternalFilterIsUsedByFilter(
                                poFilter.getSecondFilterTableName(), poFilter.getSecondFilterId());
                        if (bfmXmlRuleFilter != null)
                        {
                            filterSet.add("base_filter_mapping" + "." + bfmXmlRuleFilter.getId());
                            filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                    + bfmXmlRuleFilter.getBaseFilterId());
                        }

                        XMLRuleFilter xmlRuleFilter = selectXmlRuleFilterDataFromDatabase(
                                poFilter.getSecondFilterId(), companyId);

                        if (xmlRuleFilter.getXmlRuleId() != -1)
                        {
                            filterSet.add("xml_rule" + "." + xmlRuleFilter.getXmlRuleId());
                        }

                        if (xmlRuleFilter.getConfigXml() != null
                                && !xmlRuleFilter.getConfigXml().equals(""))
                        {
                            try
                            {
                                String configXmlStr = xmlRuleFilter.getConfigXml();
                                XmlFilterConfigParser xmlFilterConfigParser = new XmlFilterConfigParser(
                                        configXmlStr);
                                xmlFilterConfigParser.parserXml();
                                String postFilterTableName = xmlFilterConfigParser
                                        .getElementPostFilterTableName();
                                String postFilterTableID = xmlFilterConfigParser
                                        .getElementPostFilterId();

                                if (postFilterTableName != String.valueOf(-1)
                                        && postFilterTableID != String.valueOf(-1))
                                {
                                    filterSet.add(postFilterTableName + "." + postFilterTableID);
                                    if (postFilterTableName
                                            .equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                                    {
                                        // Judge whether the html_filter
                                        // reference base_filter
                                        BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                                postFilterTableName,
                                                Long.parseLong(postFilterTableID));

                                        if (bfm != null)
                                        {
                                            filterSet
                                                    .add("base_filter_mapping" + "." + bfm.getId());
                                            filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                                    + bfm.getBaseFilterId());
                                        }
                                    }
                                }

                                String cdataPostFilterTableName = xmlFilterConfigParser
                                        .getCdataPostFilterTableName();
                                String cdataPostFilterId = xmlFilterConfigParser
                                        .getCdataPostFilterId();

                                if (cdataPostFilterTableName != String.valueOf(-1)
                                        && cdataPostFilterId != String.valueOf(-1))
                                {
                                    filterSet.add(cdataPostFilterTableName + "."
                                            + cdataPostFilterId);

                                    if (cdataPostFilterTableName
                                            .equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                                    {
                                        // Judge whether the html_filter
                                        // reference base_filter
                                        BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                                cdataPostFilterTableName,
                                                Long.parseLong(cdataPostFilterId));

                                        if (bfm != null)
                                        {
                                            filterSet
                                                    .add("base_filter_mapping" + "." + bfm.getId());
                                            filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                                    + bfm.getBaseFilterId());
                                        }
                                    }
                                }
                                List<String> list = xmlFilterConfigParser.getPostFilterIdAndName();
                                if (list != null && list.size() > 0)
                                {
                                    for (int i = 0; i < list.size(); i++)
                                    {
                                        String postFilterIdAndName = list.get(i);
                                        String[] postFilterIdAndNameArr = postFilterIdAndName
                                                .split(",");
                                        String postFilterId = postFilterIdAndNameArr[0];
                                        String postFilterName = postFilterIdAndNameArr[1];
                                        filterSet.add(postFilterName + "." + postFilterId);

                                        if (postFilterName
                                                .equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                                        {
                                            // Judge whether the html_filter
                                            // reference base_filter
                                            BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                                    postFilterName, Long.parseLong(postFilterId));

                                            if (bfm != null)
                                            {
                                                filterSet.add("base_filter_mapping" + "."
                                                        + bfm.getId());
                                                filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                                        + bfm.getBaseFilterId());
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                logger.error("An error occurred while processing xml filter.");
                            }
                        }
                    }
                }
            }
            else if (filterTableName.equalsIgnoreCase(FilterConstants.XMLRULE_TABLENAME))
            {
                XMLRuleFilter xmlRuleFilter = (XMLRuleFilter) filter;
                if (xmlRuleFilter.getXmlRuleId() != -1)
                {
                    filterSet.add("xml_rule" + "." + xmlRuleFilter.getXmlRuleId());
                }

                if (xmlRuleFilter.getConfigXml() != null
                        && !xmlRuleFilter.getConfigXml().equals(""))
                {
                    try
                    {
                        String configXmlStr = xmlRuleFilter.getConfigXml();
                        XmlFilterConfigParser xmlFilterConfigParser = new XmlFilterConfigParser(
                                configXmlStr);
                        xmlFilterConfigParser.parserXml();
                        String postFilterTableName = xmlFilterConfigParser
                                .getElementPostFilterTableName();
                        String postFilterTableID = xmlFilterConfigParser.getElementPostFilterId();

                        if (postFilterTableName != null && postFilterTableID != null)
                        {
                            filterSet.add(postFilterTableName + "." + postFilterTableID);
                            if (postFilterTableName
                                    .equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                            {
                                // Judge whether the html_filter
                                // reference base_filter
                                BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                        postFilterTableName, Long.parseLong(postFilterTableID));

                                if (bfm != null)
                                {
                                    filterSet.add("base_filter_mapping" + "." + bfm.getId());
                                    filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                            + bfm.getBaseFilterId());
                                }
                            }
                        }

                        String cdataPostFilterTableName = xmlFilterConfigParser
                                .getCdataPostFilterTableName();
                        String cdataPostFilterId = xmlFilterConfigParser.getCdataPostFilterId();

                        if (cdataPostFilterTableName != String.valueOf(-1)
                                && cdataPostFilterId != String.valueOf(-1))
                        {
                            filterSet.add(cdataPostFilterTableName + "." + cdataPostFilterId);

                            if (cdataPostFilterTableName
                                    .equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                            {
                                // Judge whether the html_filter
                                // reference base_filter
                                BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                        cdataPostFilterTableName, Long.parseLong(cdataPostFilterId));

                                if (bfm != null)
                                {
                                    filterSet.add("base_filter_mapping" + "." + bfm.getId());
                                    filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                            + bfm.getBaseFilterId());
                                }
                            }
                        }
                        List<String> list = xmlFilterConfigParser.getPostFilterIdAndName();
                        if (list != null && list.size() > 0)
                        {
                            for (int i = 0; i < list.size(); i++)
                            {
                                String postFilterIdAndName = list.get(i);
                                String[] postFilterIdAndNameArr = postFilterIdAndName.split(",");
                                String postFilterId = postFilterIdAndNameArr[0];
                                String postFilterName = postFilterIdAndNameArr[1];
                                filterSet.add(postFilterName + "." + postFilterId);

                                if (postFilterName.equalsIgnoreCase(FilterConstants.HTML_TABLENAME))
                                {
                                    // Judge whether the html_filter
                                    // reference base_filter
                                    BaseFilterMapping bfm = checkInternalFilterIsUsedByFilter(
                                            postFilterName, Long.parseLong(postFilterId));

                                    if (bfm != null)
                                    {
                                        filterSet.add("base_filter_mapping" + "." + bfm.getId());
                                        filterSet.add(FilterConstants.BASE_TABLENAME + "."
                                                + bfm.getBaseFilterId());
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("An error occurred while processing xml filter.");
                    }
                }
            }
            // Judge whether the filter reference base_filter
            if (baseFilterMapping != null)
            {
                filterSet.add("base_filter_mapping" + "." + baseFilterMapping.getId());
                filterSet.add(FilterConstants.BASE_TABLENAME + "."
                        + baseFilterMapping.getBaseFilterId());
            }
        }
        return filterSet;
    }

    /**
     * Write openOfficeFilter the properties file
     * 
     * @param prop
     * @param filter
     * */
    private static void propertiesInpuOpenOfficeFiltert(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            OpenOfficeFilter openOfficeFilter = (OpenOfficeFilter) filter;

            buffer.append("##openoffice_filter.").append(openOfficeFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId()).append(".ID = ")
                    .append(openOfficeFilter.getId()).append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".COMPANY_ID = ").append(openOfficeFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(openOfficeFilter.getFilterName())).append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(openOfficeFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".IS_HEADER_TRANSLATE = ").append(openOfficeFilter.isHeaderTranslate())
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".IS_FILEINFO_TRANSLATE = ")
                    .append(openOfficeFilter.isFileinfoTranslate()).append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".UNEXTRACTABLE_WORD_PARAGRAPH_STYLES = ")
                    .append(editUtil.removeCRLF(openOfficeFilter.getParagraphStyles()))
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".UNEXTRACTABLE_WORD_CHARACTER_STYLES = ")
                    .append(editUtil.removeCRLF(openOfficeFilter.getCharacterStyles()))
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".XML_FILTER_ID = ").append(openOfficeFilter.getXmlFilterId())
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".SECOND_FILTER_ID = ").append(openOfficeFilter.getSecondFilterId())
                    .append(NEW_LINE);
            buffer.append("openoffice_filter.").append(openOfficeFilter.getId())
                    .append(".SECOND_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(openOfficeFilter.getSecondFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##openoffice_filter.").append(openOfficeFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write jspFilter the properties file
     * 
     * @param prop
     * @param filter
     * */
    private static void propertiesInputJspFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            JSPFilter jspFilter = (JSPFilter) filter;

            buffer.append("##jsp_filter.").append(jspFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId()).append(".ID = ")
                    .append(jspFilter.getId()).append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId()).append(".COMPANY_ID = ")
                    .append(jspFilter.getCompanyId()).append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId()).append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(jspFilter.getFilterName())).append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId()).append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(jspFilter.getFilterDescription())).append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId())
                    .append(".IS_ADDITIONAL_HEAD_ADDED = ")
                    .append(jspFilter.getAddAdditionalHead()).append(NEW_LINE);
            buffer.append("jsp_filter.").append(jspFilter.getId()).append(".IS_ESCAPE_ENTITY = ")
                    .append(jspFilter.getEnableEscapeEntity()).append(NEW_LINE);
            buffer.append("##jsp_filter.").append(jspFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write javaScriptFilter the properties file
     * 
     * @param prop
     * @param filter
     * */
    private static void propertiesInputJavaScriptFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            JavaScriptFilter javaScriptFilter = (JavaScriptFilter) filter;

            buffer.append("##java_script_filter.").append(javaScriptFilter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId()).append(".ID = ")
                    .append(javaScriptFilter.getId()).append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId())
                    .append(".COMPANY_ID = ").append(javaScriptFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(javaScriptFilter.getFilterName())).append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(javaScriptFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId())
                    .append(".JS_FUNCTION_FILTER = ")
                    .append(editUtil.removeCRLF(javaScriptFilter.getJsFunctionText()))
                    .append(NEW_LINE);
            buffer.append("java_script_filter.").append(javaScriptFilter.getId())
                    .append(".ENABLE_UNICODE_ESCAPE = ")
                    .append(javaScriptFilter.getEnableUnicodeEscape()).append(NEW_LINE);
            buffer.append("##java_script_filter.").append(javaScriptFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write frameMakerFilter the properties file
     * 
     * @param prop
     * @param filter
     * */
    private static void propertiesInputInDesignFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            InddFilter inddFilter = (InddFilter) filter;

            buffer.append("##indd_filter.").append(inddFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId()).append(".ID = ")
                    .append(inddFilter.getId()).append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId()).append(".COMPANY_ID = ")
                    .append(inddFilter.getCompanyId()).append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId()).append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(inddFilter.getFilterName())).append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(inddFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".TRANSLATE_HIDDEN_LAYER = ")
                    .append(inddFilter.getTranslateHiddenLayer()).append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".TRANSLATE_MASTER_LAYER = ")
                    .append(inddFilter.getTranslateMasterLayer()).append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".TRANSLATE_FILE_INFO = ").append(inddFilter.getTranslateFileInfo())
                    .append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".TRANSLATE_HYPERLINKS = ").append(inddFilter.getTranslateHyperlinks())
                    .append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".EXTRACT_LINE_BREAK = ").append(inddFilter.getExtractLineBreak())
                    .append(NEW_LINE);
            buffer.append("indd_filter.").append(inddFilter.getId())
                    .append(".REPLACE_NONBREAKING_SPACE = ")
                    .append(inddFilter.isReplaceNonbreakingSpace()).append(NEW_LINE);
            buffer.append("##indd_filter.").append(inddFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write frameMakerFilter the properties file
     * 
     * @param prop
     * @param filter
     * */
    private static void propertiesInputFrameMakerFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            FMFilter fmFilter = (FMFilter) filter;

            buffer.append("##frame_maker_filter.").append(fmFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId()).append(".ID = ")
                    .append(fmFilter.getId()).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId()).append(".COMPANY_ID = ")
                    .append(fmFilter.getCompanyId()).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId()).append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(fmFilter.getFilterName())).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(fmFilter.getFilterDescription())).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".TRANSLATE_FOOT_NOTE = ").append(fmFilter.isExposeFootNote())
                    .append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".TRANSLATE_LEFT_MASTER_PAGE = ")
                    .append(fmFilter.isExposeLeftMasterPage()).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".TRANSLATE_RIGHT_MASTER_PAGE = ")
                    .append(fmFilter.isExposeRightMasterPage()).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".TRANSLATE_OTHER_MASTER_PAGE = ")
                    .append(fmFilter.isExposeOtherMasterPage()).append(NEW_LINE);
            buffer.append("frame_maker_filter.").append(fmFilter.getId())
                    .append(".IS_TOC_TRANSLATE = ").append(fmFilter.isTableOfContentTranslate())
                    .append(NEW_LINE);
            buffer.append("##frame_maker_filter.").append(fmFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write msOffice2010Filter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputOffice2010Filter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            MSOffice2010Filter msOffice2010Filter = (MSOffice2010Filter) filter;

            buffer.append("##office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId()).append(".ID = ")
                    .append(msOffice2010Filter.getId()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getFilterName()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".UNEXTRACTABLE_WORD_PARAGRAPH_STYLES = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getParagraphStyles()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".UNEXTRACTABLE_WORD_CHARACTER_STYLES = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getCharacterStyles()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".UNEXTRACTABLE_EXCEL_CELL_STYLES = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getExcelCellStyles()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".WORD_INTERNAL_TEXT_STYLES = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getWordInternalTextStyles()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".EXCEL_INTERNAL_TEXT_STYLES = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getExcelInternalTextStyles()))
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_HEADER_TRANSLATE = ")
                    .append(msOffice2010Filter.isHeaderTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_FOOTENDNOTE_TRANSLATE = ")
                    .append(msOffice2010Filter.isFootendnoteTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_MASTER_TRANSLATE = ")
                    .append(msOffice2010Filter.isMasterTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_FILEINFO_TRANSLATE = ")
                    .append(msOffice2010Filter.isFileinfoTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_NOTES_TRANSLATE = ").append(msOffice2010Filter.isNotesTranslate())
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_PPTLAYOUT_TRANSLATE = ")
                    .append(msOffice2010Filter.isPptlayoutTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_NOTEMASTER_TRANSLATE = ")
                    .append(msOffice2010Filter.isNotemasterTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_HANDOUTMASTER_TRANSLATE = ")
                    .append(msOffice2010Filter.isHandoutmasterTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_EXCEL_TAB_NAMES_TRANSLATE = ")
                    .append(msOffice2010Filter.isExcelTabNamesTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_TOOLTIPS_TRANSLATE = ")
                    .append(msOffice2010Filter.isToolTipsTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_HIDDENTEXT_TRANSLATE = ")
                    .append(msOffice2010Filter.isHiddenTextTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_URL_TRANSLATE = ").append(msOffice2010Filter.isUrlTranslate())
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".IS_TOC_TRANSLATE = ")
                    .append(msOffice2010Filter.isTableOfContentTranslate()).append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".COMPANY_ID = ").append(msOffice2010Filter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".XML_FILTER_ID = ").append(msOffice2010Filter.getXmlFilterId())
                    .append(NEW_LINE);
            buffer.append("office2010_filter.").append(msOffice2010Filter.getId())
                    .append(".CONTENT_POST_FILTER_ID = ")
                    .append(msOffice2010Filter.getContentPostFilterId()).append(NEW_LINE);
            buffer.append("office2010_filter.")
                    .append(msOffice2010Filter.getId())
                    .append(".CONTENT_POST_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(msOffice2010Filter.getContentPostFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##office2010_filter.").append(msOffice2010Filter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write msOfficeDocFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputMsOfficeDocFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            MSOfficeDocFilter msOfficeDocFilter = (MSOfficeDocFilter) filter;

            buffer.append("##ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".ID = ").append(msOfficeDocFilter.getId()).append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getFilterName()))
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".UNEXTRACTABLE_WORD_PARAGRAPH_STYLES = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getParagraphStyles()))
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".UNEXTRACTABLE_WORD_CHARACTER_STYLES = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getCharacterStyles()))
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".INTERNAL_TEXT_CHARACTER_STYLES = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getInternalTextStyles()))
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".IS_HEADER_TRANSLATE = ")
                    .append(msOfficeDocFilter.isHeaderTranslate()).append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".IS_ALT_TRANSLATE = ").append(msOfficeDocFilter.isAltTranslate())
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".IS_TOC_TRANSLATE = ").append(msOfficeDocFilter.isTocTranslate())
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".COMPANY_ID = ").append(msOfficeDocFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".CONTENT_POST_FILTER_ID = ")
                    .append(msOfficeDocFilter.getContentPostFilterId()).append(NEW_LINE);
            buffer.append("ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".CONTENT_POST_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(msOfficeDocFilter.getContentPostFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##ms_office_doc_filter.").append(msOfficeDocFilter.getId())
                    .append(".end").append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write msOfficeDocFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputMsOfficeExcelFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            MSOfficeExcelFilter msOfficeExcelFilter = (MSOfficeExcelFilter) filter;

            buffer.append("##ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".ID = ").append(msOfficeExcelFilter.getId()).append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(msOfficeExcelFilter.getFilterName()))
                    .append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(msOfficeExcelFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".COMPANY_ID = ").append(msOfficeExcelFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".IS_ALT_TRANSLATE = ").append(msOfficeExcelFilter.isAltTranslate())
                    .append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".IS_TAB_NAMES_TRANSLATE = ")
                    .append(msOfficeExcelFilter.isTabNamesTranslate()).append(NEW_LINE);
            buffer.append("ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".CONTENT_POST_FILTER_ID = ")
                    .append(msOfficeExcelFilter.getContentPostFilterId()).append(NEW_LINE);
            buffer.append("ms_office_excel_filter.")
                    .append(msOfficeExcelFilter.getId())
                    .append(".CONTENT_POST_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(msOfficeExcelFilter.getContentPostFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##ms_office_excel_filter.").append(msOfficeExcelFilter.getId())
                    .append(".end").append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write msOfficePptFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputMsOfficePptFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            MSOfficePPTFilter msOfficePPTFilter = (MSOfficePPTFilter) filter;

            buffer.append("##ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".ID = ").append(msOfficePPTFilter.getId()).append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(msOfficePPTFilter.getFilterName()))
                    .append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(msOfficePPTFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".IS_ALT_TRANSLATE = ").append(msOfficePPTFilter.isAltTranslate())
                    .append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".IS_NOTES_TRANSLATE = ").append(msOfficePPTFilter.isNotesTranslate())
                    .append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".COMPANY_ID = ").append(msOfficePPTFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".CONTENT_POST_FILTER_ID = ")
                    .append(msOfficePPTFilter.getContentPostFilterId()).append(NEW_LINE);
            buffer.append("ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".CONTENT_POST_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(msOfficePPTFilter.getContentPostFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##ms_office_ppt_filter.").append(msOfficePPTFilter.getId())
                    .append(".end").append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write poFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputPOFilter(File propertyFile, String tableName, Long filterId,
            Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            POFilter poFilter = (POFilter) filter;

            buffer.append("##po_filter.").append(poFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId()).append(".ID = ")
                    .append(poFilter.getId()).append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId()).append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(poFilter.getFilterName())).append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId()).append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(poFilter.getFilterDescription())).append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId()).append(".COMPANY_ID = ")
                    .append(Long.toString(poFilter.getCompanyId())).append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId()).append(".SECOND_FILTER_ID = ")
                    .append(poFilter.getSecondFilterId()).append(NEW_LINE);
            buffer.append("po_filter.").append(poFilter.getId())
                    .append(".SECOND_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(poFilter.getSecondFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("##po_filter.").append(poFilter.getId()).append(".end").append(NEW_LINE)
                    .append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write msOfficePptFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputPlainTextFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            PlainTextFilter plainTextFilter = (PlainTextFilter) filter;

            buffer.append("##plain_text_filter.").append(plainTextFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("plain_text_filter.").append(plainTextFilter.getId()).append(".ID = ")
                    .append(plainTextFilter.getId()).append(NEW_LINE);
            buffer.append("plain_text_filter.").append(plainTextFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(plainTextFilter.getFilterName())).append(NEW_LINE);
            buffer.append("plain_text_filter.").append(plainTextFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(plainTextFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("plain_text_filter.").append(plainTextFilter.getId())
                    .append(".COMPANY_ID = ").append(plainTextFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("plain_text_filter.").append(plainTextFilter.getId())
                    .append(".CONFIG_XML = ")
                    .append(checkSpecialChar(plainTextFilter.getConfigXml())).append(NEW_LINE);
            buffer.append("##plain_text_filter.").append(plainTextFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write javaPropertiesFilter the properties file
     * 
     * @param prop
     * @param filter
     * @param baseFilterMapping
     * */
    private static void propertiesInputJavaPropertiesFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            JavaPropertiesFilter javaPropertiesFilter = (JavaPropertiesFilter) filter;

            buffer.append("##java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".begin").append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".ID = ").append(javaPropertiesFilter.getId()).append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(javaPropertiesFilter.getFilterName()))
                    .append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(javaPropertiesFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".ENABLE_SID_SUPPORT = ")
                    .append(javaPropertiesFilter.getEnableSidSupport()).append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".ENABLE_UNICODE_ESCAPE = ")
                    .append(javaPropertiesFilter.getEnableUnicodeEscape()).append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".ENABLE_TRIM_SEGMENT = ")
                    .append(javaPropertiesFilter.getEnablePreserveSpaces()).append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".SECOND_FILTER_ID = ")
                    .append(javaPropertiesFilter.getSecondFilterId()).append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".SECOND_FILTER_TABLE_NAME = ")
                    .append(editUtil.removeCRLF(javaPropertiesFilter.getSecondFilterTableName()))
                    .append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".INTERNAL_TEXTS = ")
                    .append(editUtil.removeCRLF(javaPropertiesFilter.getInternalText()))
                    .append(NEW_LINE);
            buffer.append("java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".COMPANY_ID = ").append(javaPropertiesFilter.getCompanyId())
                    .append(NEW_LINE);
            buffer.append("##java_properties_filter.").append(javaPropertiesFilter.getId())
                    .append(".end").append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    private static void propertiesInputBaseFilterMapping(File propertyFile, Long id)
    {
        StringBuffer buffer = new StringBuffer();
        BaseFilterMapping baseFilterMapping = selectBaseFilterMappingData(id);
        if (baseFilterMapping == null)
            return;

        buffer.append("##base_filter_mapping.").append(baseFilterMapping.getId()).append(".begin")
                .append(NEW_LINE);
        buffer.append("base_filter_mapping.").append(baseFilterMapping.getId()).append(".ID = ")
                .append(baseFilterMapping.getId()).append(NEW_LINE);
        buffer.append("base_filter_mapping.").append(baseFilterMapping.getId())
                .append(".BASE_FILTER_ID = ").append(baseFilterMapping.getBaseFilterId())
                .append(NEW_LINE);
        buffer.append("base_filter_mapping.").append(baseFilterMapping.getId())
                .append(".FILTER_TABLE_NAME = ").append(baseFilterMapping.getFilterTableName())
                .append(NEW_LINE);
        buffer.append("base_filter_mapping.").append(baseFilterMapping.getId())
                .append(".FILTER_ID = ").append(Long.toString(baseFilterMapping.getFilterId()))
                .append(NEW_LINE);
        buffer.append("##base_filter_mapping.").append(baseFilterMapping.getId()).append(".end")
                .append(NEW_LINE).append(NEW_LINE);

        writeToFile(propertyFile, buffer.toString().getBytes());
    }

    /**
     * Writes QA Filter to properties file.
     */
    private static void propertiesInputQAFilter(File propertyFile, String tableName, Long filterId,
            Long companyId)
    {
        StringBuilder sb = new StringBuilder();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            QAFilter qaFilter = (QAFilter) filter;

            sb.append("##qa_filter.").append(qaFilter.getId()).append(".begin").append(NEW_LINE);
            sb.append("qa_filter.").append(qaFilter.getId()).append(".ID = ")
                    .append(Long.toString(qaFilter.getId())).append(NEW_LINE);
            sb.append("qa_filter.").append(qaFilter.getId()).append(".FILTER_NAME = ")
                    .append(qaFilter.getFilterName()).append(NEW_LINE);
            sb.append("qa_filter.").append(qaFilter.getId()).append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(qaFilter.getFilterDescription())).append(NEW_LINE);
            sb.append("qa_filter.").append(qaFilter.getId()).append(".COMPANY_ID = ")
                    .append(Long.toString(qaFilter.getCompanyId())).append(NEW_LINE);
            sb.append("qa_filter.").append(qaFilter.getId()).append(".CONFIG_XML = ")
                    .append(checkSpecialChar(qaFilter.getConfigXml())).append(NEW_LINE);
            sb.append("##qa_filter.").append(qaFilter.getId()).append(".end").append(NEW_LINE)
                    .append(NEW_LINE);

            writeToFile(propertyFile, sb.toString().getBytes());
        }
    }

    /**
     * Write internalFilter the properties file
     * 
     * @param prop
     * @param filterId
     * @param companyId
     * */
    private static void propertiesInputInternalFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);
        if (filter != null)
        {
            BaseFilter baseFilter = (BaseFilter) filter;

            buffer.append("##base_filter.").append(baseFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("base_filter.").append(baseFilter.getId()).append(".ID = ")
                    .append(Long.toString(baseFilter.getId())).append(NEW_LINE);
            buffer.append("base_filter.").append(baseFilter.getId()).append(".FILTER_NAME = ")
                    .append(baseFilter.getFilterName()).append(NEW_LINE);
            buffer.append("base_filter.").append(baseFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(baseFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("base_filter.").append(baseFilter.getId()).append(".COMPANY_ID = ")
                    .append(Long.toString(baseFilter.getCompanyId())).append(NEW_LINE);
            buffer.append("base_filter.").append(baseFilter.getId()).append(".CONFIG_XML = ")
                    .append(checkSpecialChar(baseFilter.getConfigXml())).append(NEW_LINE);
            buffer.append("##base_filter.").append(baseFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    /**
     * Write htmlFilter the properties file
     * 
     * @param prop
     * @param filterId
     * @param companyId
     * */
    private static void propertiesInputHtmlFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            HtmlFilter htmlFilter = (HtmlFilter) filter;

            buffer.append("##html_filter.").append(htmlFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".ID = ")
                    .append(htmlFilter.getId()).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(htmlFilter.getFilterName())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(htmlFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".EMBEDDABLE_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getEmbeddableTags())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".PLACEHOLD_TRIMMING = ")
                    .append(editUtil.removeCRLF(htmlFilter.getPlaceHolderTrim())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".COMPANY_ID = ")
                    .append(Long.toString(htmlFilter.getCompanyId())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_EMBEDDABLE_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultEmbeddableTags()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".CONVERT_HTML_ENTRY = ").append(htmlFilter.isConvertHtmlEntry())
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".IGNORE_INVALIDE_HTML_TAGS = ")
                    .append(htmlFilter.isIgnoreInvalideHtmlTags()).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".ADD_RTL_DIRECTIONALITY = ")
                    .append(htmlFilter.isAddRtlDirectionality()).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".WHITESPACE_PRESERVE = ").append(htmlFilter.getWhitespacePreserve())
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".JS_FUNCTION_FILTER = ")
                    .append(editUtil.removeCRLF(htmlFilter.getJsFunctionText())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_PAIRED_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultPairedTags()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".PAIRED_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getPairedTags())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_UNPAIRED_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultUnpairedTags()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".UNPAIRED_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getUnpairedTags())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_SWITCH_TAG_MAPS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultSwitchTagMaps()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".SWITCH_TAG_MAPS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getSwitchTagMaps())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_WHITE_PRESERVING_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultWhitePreservingTags()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_INTERNAL_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultInternalTagMaps()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId()).append(".INTERNAL_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getInternalTagMaps())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".WHITE_PRESERVING_TAGS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getWhitePreservingTags()))
                    .append(NEW_LINE);
            buffer.append("html_filter.")
                    .append(htmlFilter.getId())
                    .append(".DEFAULT_NON_TRANSLATABLE_META_ATTRIBUTES = ")
                    .append(editUtil.removeCRLF(htmlFilter
                            .getDefaultNonTranslatableMetaAttributes())).append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".NON_TRANSLATABLE_META_ATTRIBUTES = ")
                    .append(editUtil.removeCRLF(htmlFilter.getNonTranslatableMetaAttributes()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_TRANSLATABLE_ATTRIBUTES = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultTranslatableAttributes()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".TRANSLATABLE_ATTRIBUTES = ")
                    .append(editUtil.removeCRLF(htmlFilter.getTranslatableAttributes()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".DEFAULT_LOCALIZABLE_ATTRIBUTE_MAPS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getDefaultLocalizableAttributeMaps()))
                    .append(NEW_LINE);
            buffer.append("html_filter.").append(htmlFilter.getId())
                    .append(".LOCALIZABLE_ATTRIBUTE_MAPS = ")
                    .append(editUtil.removeCRLF(htmlFilter.getLocalizableAttributeMaps()))
                    .append(NEW_LINE);
            buffer.append("##html_filter.").append(htmlFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    private static void propertiesInputxmlRuleFilter(File propertyFile, String tableName,
            Long filterId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        Filter filter = selectFilterDataFromDataBase(tableName, filterId, companyId);

        if (filter != null)
        {
            XMLRuleFilter xmlRuleFilter = (XMLRuleFilter) filter;

            buffer.append("##xml_rule_filter.").append(xmlRuleFilter.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId()).append(".ID = ")
                    .append(xmlRuleFilter.getId()).append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".COMPANY_ID = ").append(xmlRuleFilter.getCompanyId()).append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".FILTER_NAME = ")
                    .append(editUtil.removeCRLF(xmlRuleFilter.getFilterName())).append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".FILTER_DESCRIPTION = ")
                    .append(editUtil.removeCRLF(xmlRuleFilter.getFilterDescription()))
                    .append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".XML_RULE_ID = ").append(xmlRuleFilter.getXmlRuleId())
                    .append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".ENABLE_CONVERT_HTML_ENTITY = ")
                    .append(xmlRuleFilter.isConvertHtmlEntity()).append(NEW_LINE);
            // buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
            // .append(".SECOND_FILTER_ID = ")
            // .append(xmlRuleFilter.getSecondFilterId()).append(NEW_LINE);
            // buffer.append("xml_rule_filter.")
            // .append(xmlRuleFilter.getId())
            // .append(".SECOND_FILTER_TABLE_NAME = ")
            // .append(deleteNewline(xmlRuleFilter
            // .getSecondFilterTableName())).append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".USE_XML_RULE = ").append(xmlRuleFilter.isUseXmlRule())
                    .append(NEW_LINE);
            buffer.append("xml_rule_filter.").append(xmlRuleFilter.getId())
                    .append(".CONFIG_XML = ")
                    .append(editUtil.removeCRLF(xmlRuleFilter.getConfigXml())).append(NEW_LINE);
            buffer.append("##xml_rule_filter.").append(xmlRuleFilter.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    private static void propertiesInputxmlRule(File propertyFile, Long ruleId, Long companyId)
    {
        StringBuffer buffer = new StringBuffer();
        XmlRuleFileImpl xmlRuleFileImpl = selectXmlRuleFileImplFromDatabase(ruleId, companyId);
        if (xmlRuleFileImpl != null)
        {
            buffer.append("##xml_rule.").append(xmlRuleFileImpl.getId()).append(".begin")
                    .append(NEW_LINE);
            buffer.append("xml_rule.").append(xmlRuleFileImpl.getId()).append(".ID=")
                    .append(xmlRuleFileImpl.getId()).append(NEW_LINE);
            buffer.append("xml_rule.").append(xmlRuleFileImpl.getId()).append(".NAME=")
                    .append(xmlRuleFileImpl.getName()).append(NEW_LINE);
            buffer.append("xml_rule.").append(xmlRuleFileImpl.getId()).append(".COMPANY_ID=")
                    .append(xmlRuleFileImpl.getCompanyId()).append(NEW_LINE);
            buffer.append("xml_rule.").append(xmlRuleFileImpl.getId()).append(".DESCRIPTION=")
                    .append(editUtil.removeCRLF(xmlRuleFileImpl.getDescription())).append(NEW_LINE);
            buffer.append("xml_rule.").append(xmlRuleFileImpl.getId()).append(".RULE_TEXT=")
                    .append(editUtil.removeCRLF(xmlRuleFileImpl.getRuleText())).append(NEW_LINE);
            buffer.append("##xml_rule.").append(xmlRuleFileImpl.getId()).append(".end")
                    .append(NEW_LINE).append(NEW_LINE);

            writeToFile(propertyFile, buffer.toString().getBytes());
        }
    }

    private static XmlRuleFileImpl selectXmlRuleFileImplFromDatabase(Long ruleId, Long companyId)
    {
        List<XmlRuleFileImpl> xmlRuleFileImplList = (List<XmlRuleFileImpl>) HibernateUtil
                .searchWithSql(XmlRuleFileImpl.class, SQL_SELECT_XML_RULE, ruleId, companyId);

        if (xmlRuleFileImplList != null && xmlRuleFileImplList.size() > 0)
            return xmlRuleFileImplList.get(0);
        else
            return null;
    }

    /**
     * Find xmlRuleFilter data according to filterId and companyId
     * 
     * @param filterId
     * @param companyId
     * @return HtmlFilter
     * */
    private static XMLRuleFilter selectXmlRuleFilterDataFromDatabase(Long filterId, Long companyId)
    {
        Filter filter = MapOfTableNameAndSpecialFilter
                .getFilterInstance(FilterConstants.XMLRULE_TABLENAME);

        List<XMLRuleFilter> list = (List<XMLRuleFilter>) HibernateUtil.searchWithSql(
                filter.getClass(), SQL_SELECT_XML_FILTER, filterId, companyId);

        if (list != null && list.size() > 0)
            return list.get(0);
        else
            return null;
    }

    public static Filter selectFilterDataFromDataBase(String tableName, Long filterId,
            Long companyId)
    {

        Filter filter = MapOfTableNameAndSpecialFilter.getFilterInstance(tableName);
        String sql = "select * from " + tableName + " where id = ? and company_id = ?";

        List<Filter> list = (List<Filter>) HibernateUtil.searchWithSql(filter.getClass(), sql,
                filterId, companyId);
        if (list != null && list.size() > 0)
            return list.get(0);
        else
            return null;
    }

    public static BaseFilterMapping checkInternalFilterIsUsedByFilter(String filterTableName,
            Long filterId)
    {
        List<BaseFilterMapping> baseFilterMappingList = (List<BaseFilterMapping>) HibernateUtil
                .searchWithSql(BaseFilterMapping.class, SQL_SELECT_BASE_FILTER_MAPPING,
                        filterTableName, filterId);
        if (baseFilterMappingList != null && baseFilterMappingList.size() > 0)
            return baseFilterMappingList.get(0);
        else
            return null;
    }

    public static BaseFilterMapping selectBaseFilterMappingData(Long id)
    {
        List<BaseFilterMapping> baseFilterMappingList = (List<BaseFilterMapping>) HibernateUtil
                .searchWithSql(BaseFilterMapping.class, SQL_SELECT_BASE_FILTER_MAPPING_DATA, id);
        if (baseFilterMappingList != null && baseFilterMappingList.size() > 0)
            return baseFilterMappingList.get(0);
        else
            return null;
    }

    private static void writeToFile(File writeInFile, byte[] bytes)
    {
        writeInFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(writeInFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }

    private static String checkSpecialChar(String str)
    {
        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < chars.length; i++)
        {
            char ch = chars[i];
            if (ch == '\\')
            {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        return buffer.toString();
    }
}