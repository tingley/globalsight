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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterMapping;
import com.globalsight.cxe.entity.filterconfiguration.FMFilter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.GlobalExclusionFilter;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InddFilter;
import com.globalsight.cxe.entity.filterconfiguration.JSPFilter;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.JavaScriptFilter;
import com.globalsight.cxe.entity.filterconfiguration.JsonFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeExcelFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficePPTFilter;
import com.globalsight.cxe.entity.filterconfiguration.OpenOfficeFilter;
import com.globalsight.cxe.entity.filterconfiguration.POFilter;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilter;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilterParser;
import com.globalsight.cxe.entity.filterconfiguration.QAFilter;
import com.globalsight.cxe.entity.filterconfiguration.SidFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Import filter configuration.
 */
public class FilterConfigImport implements ConfigConstants
{
    private String companyId;
    private String sessionId;
    private String importToCompId;
    private String userId;
    // map store the old id and the new id
    private Map<Long, Long> baseFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> htmlFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> javaScriptFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> xmlRuleFileImplIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> xmlRuleFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> javaPropertiesFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> msOffice2010FilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> msOfficeDocFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> msOfficeExcelFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> msOfficePPTFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> plainTextFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> poFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> jsonFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> sidFilterIdMap = new HashMap<Long, Long>();
    private Map<Long, Long> exclusionSidFilterIdMap = new HashMap<Long, Long>();
    private static final Logger logger = Logger.getLogger(FilterConfigImport.class);

    public FilterConfigImport(String sessionId, String userId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.companyId = companyId;
        this.userId = userId;
        this.importToCompId = importToCompId;
    }

    /**
     * Analysis and imports upload file.
     */
    @SuppressWarnings("rawtypes")
    public void analysisAndImport(File uploadedFile)
    {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        Map<String, List> dataMap = new HashMap<String, List>();
        try
        {
            String[] keyArr = null;
            String key = null;
            String strKey = null;
            String strValue = null;
            InputStream is = new FileInputStream(uploadedFile);
            Properties prop = new Properties();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            prop.load(bf);
            Enumeration enum1 = prop.propertyNames();
            while (enum1.hasMoreElements())
            {
                strKey = (String) enum1.nextElement();
                key = strKey.substring(0, strKey.lastIndexOf('.'));
                keyArr = strKey.split("\\.");
                strValue = prop.getProperty(strKey);
                Set<String> keySet = map.keySet();
                if (keySet.contains(key))
                {
                    Map<String, String> valueMap = map.get(key);
                    Set<String> valueKey = valueMap.keySet();
                    if (!valueKey.contains(keyArr[2]))
                    {
                        valueMap.put(keyArr[2], strValue);
                    }
                }
                else
                {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put(keyArr[2], strValue);
                    map.put(key, valueMap);
                }

            }
            // Data analysis
            dataMap = analysisData(map);
        }
        catch (Exception e)
        {
            logger.error("Failed to parse data.", e);
            addToError("Upload failed because of incorrect data.");
        }
        // Storing data
        storeDataToDatabase(dataMap);
    }

    private Map<String, List> analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return null;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<FMFilter> frameMakerFilterList = new ArrayList<FMFilter>();
        List<HtmlFilter> htmlFilterList = new ArrayList<HtmlFilter>();
        List<InddFilter> inndrFilterList = new ArrayList<InddFilter>();
        List<BaseFilter> baseFilterList = new ArrayList<BaseFilter>();
        List<BaseFilterMapping> baseFilterMappingList = new ArrayList<BaseFilterMapping>();
        List<JavaPropertiesFilter> javaPropertiesFilterList = new ArrayList<JavaPropertiesFilter>();
        List<JavaScriptFilter> javaScriptFilterList = new ArrayList<JavaScriptFilter>();
        List<JSPFilter> jspFilterList = new ArrayList<JSPFilter>();
        List<MSOffice2010Filter> office2010FilterList = new ArrayList<MSOffice2010Filter>();
        List<MSOfficeDocFilter> msOfficeDocFilterList = new ArrayList<MSOfficeDocFilter>();
        List<MSOfficeExcelFilter> msOfficeExcelFilterList = new ArrayList<MSOfficeExcelFilter>();
        List<MSOfficePPTFilter> msOfficePptFilterList = new ArrayList<MSOfficePPTFilter>();
        List<OpenOfficeFilter> openofficeFilterList = new ArrayList<OpenOfficeFilter>();
        List<PlainTextFilter> plainTexFilterList = new ArrayList<PlainTextFilter>();
        List<POFilter> poFilterList = new ArrayList<POFilter>();
        List<XMLRuleFilter> xmlRuleFilterList = new ArrayList<XMLRuleFilter>();
        List<XmlRuleFileImpl> xmlRuleList = new ArrayList<XmlRuleFileImpl>();
        List<QAFilter> qaFilterList = new ArrayList<QAFilter>();
        List<JsonFilter> jsonFilterList = new ArrayList<JsonFilter>();
        List<SidFilter> sidFilterList = new ArrayList<SidFilter>();
        List<GlobalExclusionFilter> globalExclusionFilterList = new ArrayList<GlobalExclusionFilter>();

        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("frame_maker_filter"))
                {
                    FMFilter fmFilter = putDataIntoFMFilter(valueMap);
                    frameMakerFilterList.add(fmFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("html_filter"))
                {
                    HtmlFilter htmlFilter = putDataIntoHtmlFilter(valueMap);
                    htmlFilterList.add(htmlFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("indd_filter"))
                {
                    InddFilter inddFilter = putDataIntoInddFilter(valueMap);
                    inndrFilterList.add(inddFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("base_filter"))
                {
                    BaseFilter baseFilter = putDataIntoBaseFilter(valueMap);
                    baseFilterList.add(baseFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("base_filter_mapping"))
                {
                    BaseFilterMapping baseFilterMapping = putDataIntoBaseFilterMapping(valueMap);
                    baseFilterMappingList.add(baseFilterMapping);
                }
                else if (keyArr[0].equalsIgnoreCase("java_properties_filter"))
                {
                    JavaPropertiesFilter javaPropertiesFilter = putDataIntoJavaPropertiesFilter(valueMap);
                    javaPropertiesFilterList.add(javaPropertiesFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("json_filter"))
                {
                    JsonFilter jsonFilter = putDataIntoJsonFilter(valueMap);
                    jsonFilterList.add(jsonFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("java_script_filter"))
                {
                    JavaScriptFilter javaScriptFilter = putDataIntoJavaScriptFilter(valueMap);
                    javaScriptFilterList.add(javaScriptFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("jsp_filter"))
                {
                    JSPFilter jspFilter = putDataIntoJSPFilter(valueMap);
                    jspFilterList.add(jspFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("office2010_filter"))
                {
                    MSOffice2010Filter msOffice2010Filter = putDataIntoMSOffice2010Filter(valueMap);
                    office2010FilterList.add(msOffice2010Filter);
                }
                else if (keyArr[0].equalsIgnoreCase("ms_office_doc_filter"))
                {
                    MSOfficeDocFilter msOfficeDocFilter = putDataIntoMSOfficeDocFilter(valueMap);
                    msOfficeDocFilterList.add(msOfficeDocFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("ms_office_excel_filter"))
                {
                    MSOfficeExcelFilter msOfficeExcelFilter = putDataIntoMSOfficeExcelFilter(valueMap);
                    msOfficeExcelFilterList.add(msOfficeExcelFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("ms_office_ppt_filter"))
                {
                    MSOfficePPTFilter msOfficePPTFilter = putDataIntoMSOfficePPTFilter(valueMap);
                    msOfficePptFilterList.add(msOfficePPTFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("openoffice_filter"))
                {
                    OpenOfficeFilter openOfficeFilter = putDataIntoOpenOfficeFilter(valueMap);
                    openofficeFilterList.add(openOfficeFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("plain_text_filter"))
                {
                    PlainTextFilter plainTextFilter = putDataIntoPlainTextFilter(valueMap);
                    plainTexFilterList.add(plainTextFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("po_filter"))
                {
                    POFilter poFilter = putDataIntoPOFilter(valueMap);
                    poFilterList.add(poFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("xml_rule_filter"))
                {
                    XMLRuleFilter xmlRuleFilter = putDataIntoXMLRuleFilter(valueMap);
                    xmlRuleFilterList.add(xmlRuleFilter);
                }
                else if (keyArr[0].equalsIgnoreCase("xml_rule"))
                {
                    XmlRuleFileImpl xmlRule = putDataIntoXMLRule(valueMap);
                    xmlRuleList.add(xmlRule);
                }
                else if (FilterConstants.QA_TABLENAME.equalsIgnoreCase(keyArr[0]))
                {
                    QAFilter qaFilter = putDataIntoQAFilter(valueMap);
                    qaFilterList.add(qaFilter);
                }
                else if (FilterConstants.SID_TABLENAME.equalsIgnoreCase(keyArr[0]))
                {
                    SidFilter filter = putDataIntoSidFilter(valueMap);
                    sidFilterList.add(filter);
                }
                else if (FilterConstants.GLOBAL_EXCLUSIONS_TABLENAME.equalsIgnoreCase(keyArr[0]))
                {
                    GlobalExclusionFilter filter = putDataIntoGlobalExclusionFilter(valueMap);
                    globalExclusionFilterList.add(filter);
                }
            }
        }
        if (frameMakerFilterList.size() > 0)
            dataMap.put("frame_maker_filter", frameMakerFilterList);

        if (htmlFilterList.size() > 0)
            dataMap.put("html_filter", htmlFilterList);

        if (inndrFilterList.size() > 0)
            dataMap.put("indd_filter", inndrFilterList);

        if (baseFilterList.size() > 0)
            dataMap.put("base_filter", baseFilterList);

        if (baseFilterMappingList.size() > 0)
            dataMap.put("base_filter_mapping", baseFilterMappingList);

        if (javaPropertiesFilterList.size() > 0)
            dataMap.put("java_properties_filter", javaPropertiesFilterList);

        if (jsonFilterList.size() > 0)
            dataMap.put("json_filter", jsonFilterList);
        
        if (globalExclusionFilterList.size() > 0)
            dataMap.put("global_exclusion_filter", globalExclusionFilterList);
        
        if (sidFilterList.size() > 0)
            dataMap.put("sid_filter", sidFilterList);
        

        if (javaScriptFilterList.size() > 0)
            dataMap.put("java_script_filter", javaScriptFilterList);

        if (jspFilterList.size() > 0)
            dataMap.put("jsp_filter", jspFilterList);

        if (office2010FilterList.size() > 0)
            dataMap.put("office2010_filter", office2010FilterList);

        if (msOfficeDocFilterList.size() > 0)
            dataMap.put("ms_office_doc_filter", msOfficeDocFilterList);

        if (msOfficeExcelFilterList.size() > 0)
            dataMap.put("ms_office_excel_filter", msOfficeExcelFilterList);

        if (msOfficePptFilterList.size() > 0)
            dataMap.put("ms_office_ppt_filter", msOfficePptFilterList);

        if (openofficeFilterList.size() > 0)
            dataMap.put("openoffice_filter", openofficeFilterList);

        if (plainTexFilterList.size() > 0)
            dataMap.put("plain_text_filter", plainTexFilterList);

        if (poFilterList.size() > 0)
            dataMap.put("po_filter", poFilterList);

        if (xmlRuleFilterList.size() > 0)
            dataMap.put("xml_rule_filter", xmlRuleFilterList);

        if (xmlRuleList.size() > 0)
            dataMap.put("xml_rule", xmlRuleList);

        if (qaFilterList.size() > 0)
            dataMap.put(FilterConstants.QA_TABLENAME, qaFilterList);

        return dataMap;
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;
        try
        {
            // stores "global_exclusions_filter" data
            if (dataMap.containsKey(FilterConstants.GLOBAL_EXCLUSIONS_TABLENAME))
            {
                storeGlobalExclusionFilterFilterData(dataMap);
            }
            
            // stores "sid_filter" data
            if (dataMap.containsKey(FilterConstants.SID_TABLENAME))
            {
                storeSidFilterData(dataMap);
            }
            
            // stores "base_filter" data to database
            if (dataMap.containsKey("base_filter"))
            {
                storeBaseFilterData(dataMap);
            }
            // stores "html_filter" data to database
            if (dataMap.containsKey("html_filter"))
            {
                storeHtmlFilterData(dataMap);
            }
            // stores "java_script_filter" data to database
            if (dataMap.containsKey("java_script_filter"))
            {
                storeJavaScriptFilterData(dataMap);
            }
            // stores "json_filter" data to database
            if (dataMap.containsKey("json_filter"))
            {
                storeJsonFilterData(dataMap);
            }
            // stores "xml_rule" data to database
            if (dataMap.containsKey("xml_rule"))
            {
                storeXmlRuleFileImplData(dataMap);
            }
            // stores "xml_rule_filter" data to database
            if (dataMap.containsKey("xml_rule_filter"))
            {
                storeXMLRuleFilterData(dataMap);
            }
            // stores "java_properties_filter" data to database
            if (dataMap.containsKey("java_properties_filter"))
            {
                storeJavaPropertiesFilterData(dataMap);
            }
            // stores "office2010_filter" data to database
            if (dataMap.containsKey("office2010_filter"))
            {
                storeMSOffice2010FilterData(dataMap);
            }
            // stores "ms_office_doc_filter" data to database
            if (dataMap.containsKey("ms_office_doc_filter"))
            {
                storeMSOfficeDocFilterData(dataMap);
            }
            // stores "ms_office_excel_filter" data to database
            if (dataMap.containsKey("ms_office_excel_filter"))
            {
                storeMSOfficeExcelFilterData(dataMap);
            }
            // stores "ms_office_ppt_filter" data to database
            if (dataMap.containsKey("ms_office_ppt_filter"))
            {
                storeMSOfficePPTFilterData(dataMap);
            }
            // stores "frame_maker_filter" data
            if (dataMap.containsKey("frame_maker_filter"))
            {
                storeFMFilterData(dataMap);
            }
            // stores "jsp_filter" data
            if (dataMap.containsKey("jsp_filter"))
            {
                storeJSPFilter(dataMap);
            }
            // stores "openoffice_filter" data
            if (dataMap.containsKey("openoffice_filter"))
            {
                storeOpenOfficeFilterData(dataMap);
            }
            // stores "indd_filter" data
            if (dataMap.containsKey("indd_filter"))
            {
                storeInddFilterData(dataMap);
            }
            // stores "po_filter" data
            if (dataMap.containsKey("po_filter"))
            {
                storePOFilterData(dataMap);
            }
            // stores "plain_text_filter" data
            if (dataMap.containsKey("plain_text_filter"))
            {
                storePlainTextFilterData(dataMap);
            }
            // stores "base_filter_mapping" data
            if (dataMap.containsKey("base_filter_mapping"))
            {
                storeBaseFilterMappingData(dataMap);
            }

            // stores "qa_filter" data
            if (dataMap.containsKey(FilterConstants.QA_TABLENAME))
            {
                storeQAFilterData(dataMap);
            }
            
            addMessage("<b>Done importing Filter Configurations.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Filter Configuration.", e);
            addToError(e.getMessage());
        }
    }

    /**
     * Stores qa_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeQAFilterData(Map<String, List> dataMap)
    {
        List<QAFilter> qaFilterList = (List<QAFilter>) dataMap.get(FilterConstants.QA_TABLENAME);
        try
        {
            for (QAFilter qaFilter : qaFilterList)
            {
                String name = qaFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "QAFilter");
                qaFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(qaFilter);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("QA Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload QA Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }
    
    /**
     * Stores sid_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeSidFilterData(Map<String, List> dataMap)
    {
        List<SidFilter> filterList = (List<SidFilter>) dataMap.get(FilterConstants.SID_TABLENAME);
        try
        {
            for (SidFilter f : filterList)
            {
                Long id = f.getId();
                String name = f.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "SidFilter");
                f.setFilterName(newFilterName);
                
                long efId = f.getExclusionFilterId();
                if (efId > 0)
                {
                    Long newExclusionSidFilterId = exclusionSidFilterIdMap.get(efId);
                    if (newExclusionSidFilterId != null)
                    {
                        f.setExclusionFilterId(newExclusionSidFilterId);
                    }
                }
                
                // stores data to database
                HibernateUtil.save(f);
                sidFilterIdMap.put(id, f.getId());
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("SID Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload SID Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }
    
    /**
     * Stores qa_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeGlobalExclusionFilterFilterData(Map<String, List> dataMap)
    {
        List<GlobalExclusionFilter> filterList = (List<GlobalExclusionFilter>) dataMap.get(FilterConstants.GLOBAL_EXCLUSIONS_TABLENAME);
        try
        {
            for (GlobalExclusionFilter f : filterList)
            {
                Long id = f.getId();
                String name = f.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "GlobalExclusionFilter");
                f.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(f);
                
                exclusionSidFilterIdMap.put(id, f.getId());
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Filter Exclusion Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Filter Exclusion Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }


    /**
     * Stores base_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeBaseFilterData(Map<String, List> dataMap)
    {
        BaseFilter baseFilter = null;
        List<BaseFilter> baseFilterList = (List<BaseFilter>) dataMap.get("base_filter");
        try
        {
            for (int i = 0; i < baseFilterList.size(); i++)
            {
                // stores data to database
                baseFilter = baseFilterList.get(i);
                Long id = baseFilter.getId();
                String name = baseFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "BaseFilter");
                baseFilter.setFilterName(newFilterName);
                HibernateUtil.save(baseFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "BaseFilter");
                baseFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Base Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Base Text Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores html_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeHtmlFilterData(Map<String, List> dataMap)
    {
        HtmlFilter htmlFilter = null;
        List<HtmlFilter> htmlFilterList = (List<HtmlFilter>) dataMap.get("html_filter");
        try
        {
            for (int i = 0; i < htmlFilterList.size(); i++)
            {
                htmlFilter = htmlFilterList.get(i);
                Long id = htmlFilter.getId();
                String name = htmlFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "HtmlFilter");
                htmlFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(htmlFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "HtmlFilter");
                htmlFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Html Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Html Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores javascript_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeJavaScriptFilterData(Map<String, List> dataMap)
    {
        JavaScriptFilter javaScriptFilter = null;
        List<JavaScriptFilter> javaScriptFilterList = (List<JavaScriptFilter>) dataMap
                .get("java_script_filter");
        try
        {
            for (int i = 0; i < javaScriptFilterList.size(); i++)
            {
                javaScriptFilter = javaScriptFilterList.get(i);
                Long id = javaScriptFilter.getId();
                String name = javaScriptFilter.getFilterName();
                String newFilterName = checkFilterNameExists(name, "JavaScriptFilter");
                // gets new filter name
                javaScriptFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(javaScriptFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "JavaScriptFilter");
                javaScriptFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("JavaScript Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Java Script Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores XmlRuleFileImpl data.
     */
    @SuppressWarnings("unchecked")
    private void storeXmlRuleFileImplData(Map<String, List> dataMap)
    {
        XmlRuleFileImpl xmlRuleFileImpl = null;
        List<XmlRuleFileImpl> xmlRuleFileImplList = (List<XmlRuleFileImpl>) dataMap.get("xml_rule");
        try
        {
            for (int i = 0; i < xmlRuleFileImplList.size(); i++)
            {
                xmlRuleFileImpl = xmlRuleFileImplList.get(i);
                String name = xmlRuleFileImpl.getName();
                Long xmlRuleId = xmlRuleFileImpl.getId();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "XmlRuleFileImpl");
                XmlRuleFileImpl newXmlRuleFileImpl = getNewXmlRuleFileImpl(xmlRuleFileImpl);
                newXmlRuleFileImpl.setName(newFilterName);
                // stores data to database
                HibernateUtil.save(newXmlRuleFileImpl);
                // gets new id
                Long newId = selectNewId(newFilterName, "XmlRuleFileImpl");
                xmlRuleFileImplIdMap.put(xmlRuleId, newId);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("XML Rule name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Xml Rule data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private XmlRuleFileImpl getNewXmlRuleFileImpl(XmlRuleFileImpl xmlRuleFileImpl)
    {
        XmlRuleFileImpl newXmlRuleFileImpl = new XmlRuleFileImpl();
        newXmlRuleFileImpl.setName(xmlRuleFileImpl.getName());
        newXmlRuleFileImpl.setCompanyId(xmlRuleFileImpl.getCompanyId());
        newXmlRuleFileImpl.setDescription(xmlRuleFileImpl.getDescription());
        newXmlRuleFileImpl.setRuleText(xmlRuleFileImpl.getRuleText());
        return newXmlRuleFileImpl;
    }

    /**
     * Stores XmlRule_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeXMLRuleFilterData(Map<String, List> dataMap)
    {
        XMLRuleFilter xmlRuleFilter = null;
        List<XMLRuleFilter> xmlRuleFilterList = (List<XMLRuleFilter>) dataMap
                .get("xml_rule_filter");
        try
        {
            for (int i = 0; i < xmlRuleFilterList.size(); i++)
            {
                xmlRuleFilter = xmlRuleFilterList.get(i);
                Long id = xmlRuleFilter.getId();
                String name = xmlRuleFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "XMLRuleFilter");
                xmlRuleFilter.setFilterName(newFilterName);
                // Judgments "Xml_Filter" are references "xml_Rule"
                if (xmlRuleFileImplIdMap.containsKey(xmlRuleFilter.getXmlRuleId()))
                {
                    xmlRuleFilter.setXmlRuleId(xmlRuleFileImplIdMap.get(xmlRuleFilter
                            .getXmlRuleId()));
                }

                XmlFilterConfigParser xmlFilterConfigParser = new XmlFilterConfigParser(
                        xmlRuleFilter);
                xmlFilterConfigParser.parserXml();
                
                
                String sidFilterId = xmlFilterConfigParser.getSidFilterId();
                if (sidFilterId != null)
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "sidFilterId", String.valueOf(sidFilterIdMap.get(Long
                                    .parseLong(sidFilterId))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                
                String secondarySidFilter = xmlFilterConfigParser.getSecondarySidFilter();
                if (secondarySidFilter != null)
                {
                    Long newSecondarySidFilter = sidFilterIdMap.get(Long
                            .parseLong(secondarySidFilter));
                    if (newSecondarySidFilter != null)
                    {
                        secondarySidFilter = String.valueOf(newSecondarySidFilter);
                    }

                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "sidFilterSecondarySidFilter", secondarySidFilter);
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                
                //xmlFilterConfigParser.getSidFilterId();
                // elementPostFilterId
                String postFilterTableName = xmlFilterConfigParser
                        .getElementPostFilterTableName();
                String postFilterTableID = xmlFilterConfigParser.getElementPostFilterId();
                // Judgments "Xml_Filter" are references "html_filter"
                if (postFilterTableName.equalsIgnoreCase("html_filter")
                        && htmlFilterIdMap.containsKey(Long.parseLong(postFilterTableID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "elementPostFilterId", String.valueOf(htmlFilterIdMap.get(Long
                                    .parseLong(postFilterTableID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                // Judgments "Xml_Filter" are references "java_script_filter"
                else if (postFilterTableName.equalsIgnoreCase("java_script_filter")
                        && javaScriptFilterIdMap.containsKey(Long.parseLong(postFilterTableID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "elementPostFilterId", String.valueOf(javaScriptFilterIdMap
                                    .get(Long.parseLong(postFilterTableID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                else if (postFilterTableName.equalsIgnoreCase("filter_json")
                        && jsonFilterIdMap.containsKey(Long.parseLong(postFilterTableID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "elementPostFilterId", String.valueOf(jsonFilterIdMap.get(Long
                                    .parseLong(postFilterTableID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }

                // cdataPostFilter
                String cdataPostFilterID = xmlFilterConfigParser.getCdataPostFilterId();
                String cdataPostFilterTableName = xmlFilterConfigParser
                        .getCdataPostFilterTableName();

                if (cdataPostFilterTableName.equalsIgnoreCase("html_filter")
                        && htmlFilterIdMap.containsKey(Long.parseLong(cdataPostFilterID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "cdataPostFilterId", String.valueOf(htmlFilterIdMap.get(Long
                                    .parseLong(cdataPostFilterID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                // Judgments "Xml_Filter" are references "java_script_filter"
                else if (cdataPostFilterTableName.equalsIgnoreCase("java_script_filter")
                        && javaScriptFilterIdMap.containsKey(Long.parseLong(cdataPostFilterID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "cdataPostFilterId", String.valueOf(javaScriptFilterIdMap.get(Long
                                    .parseLong(cdataPostFilterID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }
                else if (cdataPostFilterTableName.equalsIgnoreCase("filter_json")
                        && jsonFilterIdMap.containsKey(Long.parseLong(cdataPostFilterID)))
                {
                    String newconfigXmlStr = xmlFilterConfigParser.getNewConfigXmlStr(
                            "cdataPostFilterId", String.valueOf(jsonFilterIdMap.get(Long
                                    .parseLong(cdataPostFilterID))));
                    xmlRuleFilter.setConfigXml(newconfigXmlStr);
                }

                // cdataPostfilterTags
                String newConfigXmlStr = xmlFilterConfigParser
                        .getNewConfigXmlStr(htmlFilterIdMap);
                xmlRuleFilter.setConfigXml(newConfigXmlStr);

                // stores data to database
                HibernateUtil.save(xmlRuleFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "XMLRuleFilter");
                xmlRuleFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Xml Rule Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Xml Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores json_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeJsonFilterData(Map<String, List> dataMap)
    {
        JsonFilter jsonFilter = null;
        List<JsonFilter> jsonFilterList = (List<JsonFilter>) dataMap.get("json_filter");
        try
        {
            for (int i = 0; i < jsonFilterList.size(); i++)
            {
                jsonFilter = jsonFilterList.get(i);
                Long id = jsonFilter.getId();
                String name = jsonFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "JsonFilter");
                jsonFilter.setFilterName(newFilterName);
                if (baseFilterIdMap.get(jsonFilter.getBaseFilterId()) != null)
                {
                    jsonFilter
                            .setBaseFilterId(baseFilterIdMap.get(jsonFilter.getBaseFilterId()));
                }

                // Judgments "json_Filter" are references "html_filter"
                if (jsonFilter.getElementPostFilterTableName().equalsIgnoreCase("html_filter")
                        && htmlFilterIdMap.containsKey(jsonFilter.getElementPostFilterId()))
                {
                    jsonFilter.setElementPostFilterId(htmlFilterIdMap
                            .get(jsonFilter.getElementPostFilterId()));
                }
                
                SidFilter sf = jsonFilter.getSidFilter();
                if (sf != null)
                {
                    sf = HibernateUtil.get(SidFilter.class, sidFilterIdMap.get(sf.getId()));
                    jsonFilter.setSidFilter(sf);
                }
                
                // stores data to database
                HibernateUtil.save(jsonFilter);
                Long newId = selectNewId(newFilterName, "JsonFilter");
                jsonFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("JSON Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Json Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores javaProperties_filter data.
     */
    @SuppressWarnings("unchecked")
    private void storeJavaPropertiesFilterData(Map<String, List> dataMap)
    {
        JavaPropertiesFilter javaPropertiesFilter = null;
        List<JavaPropertiesFilter> javaPropertiesFilterList = (List<JavaPropertiesFilter>) dataMap
                .get("java_properties_filter");
        try
        {
            for (int i = 0; i < javaPropertiesFilterList.size(); i++)
            {
                javaPropertiesFilter = javaPropertiesFilterList.get(i);
                Long id = javaPropertiesFilter.getId();
                String name = javaPropertiesFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "JavaPropertiesFilter");
                javaPropertiesFilter.setFilterName(newFilterName);

                // Judgments "Xml_Filter" are references "html_filter"
                if (javaPropertiesFilter.getSecondFilterTableName().equalsIgnoreCase(
                        "html_filter")
                        && htmlFilterIdMap
                                .containsKey(javaPropertiesFilter.getSecondFilterId()))
                {
                    javaPropertiesFilter.setSecondFilterId(htmlFilterIdMap
                            .get(javaPropertiesFilter.getSecondFilterId()));
                }
                
                SidFilter sf = javaPropertiesFilter.getSidFilter();
                if (sf != null)
                {
                    sf = HibernateUtil.get(SidFilter.class, sidFilterIdMap.get(sf.getId()));
                    javaPropertiesFilter.setSidFilter(sf);
                }
                
                // stores data to database
                HibernateUtil.save(javaPropertiesFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "JavaPropertiesFilter");
                javaPropertiesFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Java Property Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Java Properties Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores office2010_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeMSOffice2010FilterData(Map<String, List> dataMap)
    {
        MSOffice2010Filter msOffice2010Filter = null;
        List<MSOffice2010Filter> msOffice2010FilterList = (List<MSOffice2010Filter>) dataMap
                .get("office2010_filter");
        try
        {
            for (int i = 0; i < msOffice2010FilterList.size(); i++)
            {
                msOffice2010Filter = msOffice2010FilterList.get(i);
                Long id = msOffice2010Filter.getId();
                String name = msOffice2010Filter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "MSOffice2010Filter");
                msOffice2010Filter.setFilterName(newFilterName);
                
                // Judgments "office2010_filter" are references "html_filter"
                if (msOffice2010Filter.getContentPostFilterTableName().equalsIgnoreCase(
                        "html_filter")
                        && htmlFilterIdMap.containsKey(msOffice2010Filter.getContentPostFilterId()))
                {
                    msOffice2010Filter.setContentPostFilterId(htmlFilterIdMap
                            .get(msOffice2010Filter.getContentPostFilterId()));
                }
                // stores data to database
                HibernateUtil.save(msOffice2010Filter);
                // gets new id
                Long newId = selectNewId(newFilterName, "MSOffice2010Filter");
                msOffice2010FilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("MS Office2010 Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MS Office 2010 Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores ms_office_doc_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeMSOfficeDocFilterData(Map<String, List> dataMap)
    {
        MSOfficeDocFilter msOfficeDocFilter = null;
        List<MSOfficeDocFilter> msOfficeDocFilterList = (List<MSOfficeDocFilter>) dataMap
                .get("ms_office_doc_filter");
        try
        {
            for (int i = 0; i < msOfficeDocFilterList.size(); i++)
            {
                msOfficeDocFilter = msOfficeDocFilterList.get(i);
                Long id = msOfficeDocFilter.getId();
                String name = msOfficeDocFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "MSOfficeDocFilter");
                msOfficeDocFilter.setFilterName(newFilterName);
                // Judgments "ms_office_doc_filter" are references
                // "html_filter"
                if (msOfficeDocFilter.getContentPostFilterTableName().equalsIgnoreCase(
                        "html_filter")
                        && htmlFilterIdMap.containsKey(msOfficeDocFilter.getContentPostFilterId()))
                {
                    msOfficeDocFilter.setContentPostFilterId(htmlFilterIdMap.get(msOfficeDocFilter
                            .getContentPostFilterId()));
                }
                // stores data to database
                HibernateUtil.save(msOfficeDocFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "MSOfficeDocFilter");
                msOfficeDocFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("MS Office Doc Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MS Office Doc Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores ms_office_excel_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeMSOfficeExcelFilterData(Map<String, List> dataMap)
    {
        MSOfficeExcelFilter msOfficeExcelFilter = null;
        List<MSOfficeExcelFilter> msOfficeExcelFilterList = (List<MSOfficeExcelFilter>) dataMap
                .get("ms_office_excel_filter");
        try
        {
            for (int i = 0; i < msOfficeExcelFilterList.size(); i++)
            {
                msOfficeExcelFilter = msOfficeExcelFilterList.get(i);
                Long id = msOfficeExcelFilter.getId();
                String name = msOfficeExcelFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "MSOfficeExcelFilter");
                msOfficeExcelFilter.setFilterName(newFilterName);

                // Judgments "ms_office_excel_filter" are references
                // "html_filter"
                if (msOfficeExcelFilter.getContentPostFilterTableName().equalsIgnoreCase(
                        "html_filter")
                        && htmlFilterIdMap
                                .containsKey(msOfficeExcelFilter.getContentPostFilterId()))
                {
                    msOfficeExcelFilter.setContentPostFilterId(htmlFilterIdMap
                            .get(msOfficeExcelFilter.getContentPostFilterId()));
                }
                // stores data to database
                HibernateUtil.save(msOfficeExcelFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "MSOfficeExcelFilter");
                msOfficeExcelFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("MS Office Excel Filter name <b>" + name
                            + "</b> already exists. <b>" + newFilterName
                            + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MS Office Excel Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores ms_office_ppt_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeMSOfficePPTFilterData(Map<String, List> dataMap)
    {
        MSOfficePPTFilter msOfficePPTFilter = null;
        List<MSOfficePPTFilter> msOfficePPTFilterList = (List<MSOfficePPTFilter>) dataMap
                .get("ms_office_ppt_filter");
        try
        {
            for (int i = 0; i < msOfficePPTFilterList.size(); i++)
            {
                msOfficePPTFilter = msOfficePPTFilterList.get(i);
                Long id = msOfficePPTFilter.getId();
                String name = msOfficePPTFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "MSOfficePPTFilter");
                msOfficePPTFilter.setFilterName(newFilterName);

                // Judgments "ms_office_ppt_filter" are references
                // "html_filter"
                if (msOfficePPTFilter.getContentPostFilterTableName().equalsIgnoreCase(
                        "html_filter")
                        && htmlFilterIdMap.containsKey(msOfficePPTFilter.getContentPostFilterId()))
                {
                    msOfficePPTFilter.setContentPostFilterId(htmlFilterIdMap.get(msOfficePPTFilter
                            .getContentPostFilterId()));
                }
                // stores data to database
                HibernateUtil.save(msOfficePPTFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "MSOfficePPTFilter");
                msOfficePPTFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("MS Office PowerPoint Filter name <b>" + name
                            + "</b> already exists. <b>" + newFilterName
                            + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload MS Office PPT Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores frame_maker_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeFMFilterData(Map<String, List> dataMap)
    {
        FMFilter fmFilter = null;
        List<FMFilter> fmFilterList = (List<FMFilter>) dataMap.get("frame_maker_filter");
        try
        {
            for (int i = 0; i < fmFilterList.size(); i++)
            {
                fmFilter = fmFilterList.get(i);
                String name = fmFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "FMFilter");
                fmFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(fmFilter);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("FM Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload FrameMaker 9/Mif 9 Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores jsp_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeJSPFilter(Map<String, List> dataMap)
    {
        JSPFilter jspFilter = null;
        List<JSPFilter> jspFilterList = (List<JSPFilter>) dataMap.get("jsp_filter");
        try
        {
            for (int i = 0; i < jspFilterList.size(); i++)
            {
                jspFilter = jspFilterList.get(i);
                String name = jspFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "JSPFilter");
                jspFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(jspFilter);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("JSP Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Jsp Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores openoffice_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeOpenOfficeFilterData(Map<String, List> dataMap)
    {
        OpenOfficeFilter openOfficeFilter = null;
        List<OpenOfficeFilter> openOfficeFilterList = (List<OpenOfficeFilter>) dataMap
                .get("openoffice_filter");
        try
        {
            for (int i = 0; i < openOfficeFilterList.size(); i++)
            {
                openOfficeFilter = openOfficeFilterList.get(i);
                String name = openOfficeFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "OpenOfficeFilter");
                openOfficeFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(openOfficeFilter);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Open Office Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload OpenOffice Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores indd_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storeInddFilterData(Map<String, List> dataMap)
    {
        InddFilter inddFilter = null;
        List<InddFilter> inddFilterList = (List<InddFilter>) dataMap.get("indd_filter");
        try
        {
            for (int i = 0; i < inddFilterList.size(); i++)
            {
                inddFilter = inddFilterList.get(i);
                String name = inddFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "InddFilter");
                inddFilter.setFilterName(newFilterName);
                // stores data to database
                HibernateUtil.save(inddFilter);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("InDesign/IDML Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload InDesign/IDML Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores po_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storePOFilterData(Map<String, List> dataMap)
    {
        POFilter poFilter = null;
        List<POFilter> poFilterList = (List<POFilter>) dataMap.get("po_filter");
        try
        {
            for (int i = 0; i < poFilterList.size(); i++)
            {
                poFilter = poFilterList.get(i);
                String name = poFilter.getFilterName();
                Long id = poFilter.getId();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "POFilter");
                poFilter.setFilterName(newFilterName);
                // Judgments "po_filter" are references "html_filter"
                if (poFilter.getSecondFilterTableName().equalsIgnoreCase("html_filter")
                        && htmlFilterIdMap.containsKey(poFilter.getSecondFilterId()))
                {
                    poFilter.setSecondFilterId(htmlFilterIdMap.get(poFilter.getSecondFilterId()));
                }
                // Judgments "po_filter" are references "xml_rule_filter"
                else if (poFilter.getSecondFilterTableName().equalsIgnoreCase("xml_rule_filter")
                        && xmlRuleFilterIdMap.containsKey(poFilter.getSecondFilterId()))
                {
                    poFilter.setSecondFilterId(xmlRuleFilterIdMap.get(poFilter.getSecondFilterId()));
                }
                // stores data to database
                HibernateUtil.save(poFilter);
                // gets new id
                Long newId = selectNewId(newFilterName, "POFilter");
                poFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Portable Object Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Portable Object Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores plain_text_filter data.
     * */
    @SuppressWarnings("unchecked")
    private void storePlainTextFilterData(Map<String, List> dataMap)
    {
        PlainTextFilter plainTextFilter = null;
        List<PlainTextFilter> plainTextFilterList = (List<PlainTextFilter>) dataMap
                .get("plain_text_filter");
        try
        {
            for (int i = 0; i < plainTextFilterList.size(); i++)
            {
                plainTextFilter = plainTextFilterList.get(i);
                Long id = plainTextFilter.getId();
                String name = plainTextFilter.getFilterName();
                // gets new filter name
                String newFilterName = checkFilterNameExists(name, "PlainTextFilter");
                plainTextFilter.setFilterName(newFilterName);
                PlainTextFilterParser parser = new PlainTextFilterParser(plainTextFilter);
                parser.parserXml();
                String postFilterTableName = parser.getElementPostFilterTableName();
                String postFilterId = parser.getElementPostFilterId();
                if (FilterConstants.HTML_TABLENAME.equalsIgnoreCase(postFilterTableName)
                        && htmlFilterIdMap.containsKey(Long.parseLong(postFilterId)))
                {
                    String newconfigXml = parser.getNewConfigXml(
                            PlainTextFilterParser.NODE_ELEMENT_POST_FILTER_ID,
                            String.valueOf(htmlFilterIdMap.get(Long.parseLong(postFilterId))));
                    plainTextFilter.setConfigXml(newconfigXml);
                }
                // stores data to database
                HibernateUtil.save(plainTextFilter);
                long sidFilterId = parser.getSidFilterId();
                if (sidFilterId > 0)
                {
                    Long sidId = sidFilterIdMap.get(sidFilterId);
                    if (sidId != null)
                    {
                        sidFilterId = sidId;
                    }
                    else
                    {
                        // for old data. Need parser again.
                        parser = new PlainTextFilterParser(plainTextFilter);
                        parser.parserXml();
                    }
                    String newconfigXmlStr = parser.getNewConfigXml(
                            "sidFilterId", String.valueOf(sidFilterId));
                    plainTextFilter.setConfigXml(newconfigXmlStr);
                }
                HibernateUtil.update(plainTextFilter);
                
                // gets new id
                Long newId = selectNewId(newFilterName, "PlainTextFilter");
                plainTextFilterIdMap.put(id, newId);
                OperationLog.log(userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_FILTER_CONFIGURATION, newFilterName);
                if (name.equals(newFilterName))
                {
                    addMessage("<b>" + newFilterName + "</b> imported successfully.");
                }
                else
                {
                    addMessage("Plain Text Filter name <b>" + name + "</b> already exists. <b>"
                            + newFilterName + "</b> imported successfully.");
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Plain Text Filter data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Stores base_filter_mapping data.
     * */
    @SuppressWarnings("unchecked")
    private void storeBaseFilterMappingData(Map<String, List> dataMap)
    {
        BaseFilterMapping baseFilterMapping = null;
        List<BaseFilterMapping> baseFilterMappingList = (List<BaseFilterMapping>) dataMap
                .get("base_filter_mapping");
        try
        {
            for (int i = 0; i < baseFilterMappingList.size(); i++)
            {
                baseFilterMapping = baseFilterMappingList.get(i);
                String filterTableName = baseFilterMapping.getFilterTableName();
                Long filterId = baseFilterMapping.getFilterId();
                if ("html_filter".equalsIgnoreCase(filterTableName))
                {
                    if (htmlFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(htmlFilterIdMap.get(filterId));
                    }
                }
                else if ("java_properties_filter".equalsIgnoreCase(filterTableName))
                {
                    if (javaPropertiesFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(javaPropertiesFilterIdMap.get(filterId));
                    }
                }
                else if ("filter_json".equalsIgnoreCase(filterTableName))
                {
                    if (jsonFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(jsonFilterIdMap.get(filterId));
                    }
                }
                else if ("office2010_filter".equalsIgnoreCase(filterTableName))
                {
                    if (msOffice2010FilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(msOffice2010FilterIdMap.get(filterId));
                    }
                }
                else if ("ms_office_doc_filter".equalsIgnoreCase(filterTableName))
                {
                    if (msOfficeDocFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(msOfficeDocFilterIdMap.get(filterId));
                    }
                }
                else if ("ms_office_excel_filter".equalsIgnoreCase(filterTableName))
                {
                    if (msOfficeExcelFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(msOfficeExcelFilterIdMap.get(filterId));
                    }
                }
                else if ("ms_office_ppt_filter".equalsIgnoreCase(filterTableName))
                {
                    if (msOfficePPTFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(msOfficePPTFilterIdMap.get(filterId));
                    }
                }
                else if ("plain_text_filter".equalsIgnoreCase(filterTableName))
                {
                    if (plainTextFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(plainTextFilterIdMap.get(filterId));
                    }
                }
                else if ("xml_rule_filter".equalsIgnoreCase(filterTableName))
                {
                    // xml_rule_filter
                    if (xmlRuleFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(xmlRuleFilterIdMap.get(filterId));
                    }
                }
                else if ("po_filter".equalsIgnoreCase(filterTableName))
                {
                    if (poFilterIdMap.containsKey(filterId))
                    {
                        baseFilterMapping.setFilterId(poFilterIdMap.get(filterId));
                    }
                }
                if (baseFilterIdMap.containsKey(baseFilterMapping.getBaseFilterId()))
                {
                    baseFilterMapping.setBaseFilterId(baseFilterIdMap.get(baseFilterMapping
                            .getBaseFilterId()));
                }
                // stores data to database
                HibernateUtil.save(baseFilterMapping);
            }
        }
        catch (Exception e)
        {
            String msg = "Upload BaseFilterMapping data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private Long selectNewId(String newName, String filterTableName)
    {
        String hql = null;
        Map map = new HashMap();
        map.put("companyId", Long.parseLong(companyId));
        if ("XmlRuleFileImpl".equalsIgnoreCase(filterTableName))
        {
            hql = "select f.id from " + filterTableName
                    + "  f where f.companyId=:companyId and f.name=:name";
            map.put("name", newName);
        }
        else
        {
            hql = "select f.id from " + filterTableName
                    + "  f where f.companyId=:companyId and f.filterName=:filterName";
            map.put("filterName", newName);
        }

        Long id = (Long) HibernateUtil.getFirst(hql, map);

        return id;
    }

    private FMFilter putDataIntoFMFilter(Map<String, String> valueMap)
    {
        FMFilter fmFilter = new FMFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if (keyField.equalsIgnoreCase("ID"))
            {
                fmFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                fmFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                fmFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    fmFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    fmFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_FOOT_NOTE"))
            {
                fmFilter.setExposeFootNote(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_LEFT_MASTER_PAGE"))
            {
                fmFilter.setExposeLeftMasterPage(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_RIGHT_MASTER_PAGE"))
            {
                fmFilter.setExposeRightMasterPage(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_OTHER_MASTER_PAGE"))
            {
                fmFilter.setExposeOtherMasterPage(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_TOC_TRANSLATE"))
            {
                fmFilter.setTableOfContentTranslate(Boolean.parseBoolean(valueField));
            }
        }
        return fmFilter;
    }

    private HtmlFilter putDataIntoHtmlFilter(Map<String, String> valueMap)
    {
        HtmlFilter htmlFilter = new HtmlFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if (keyField.equalsIgnoreCase("ID"))
            {
                htmlFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                htmlFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                htmlFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("EMBEDDABLE_TAGS"))
            {
                htmlFilter.setEmbeddableTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("PLACEHOLD_TRIMMING"))
            {
                htmlFilter.setPlaceHolderTrim(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    htmlFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    htmlFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_EMBEDDABLE_TAGS"))
            {
                htmlFilter.setDefaultEmbeddableTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("CONVERT_HTML_ENTRY"))
            {
                htmlFilter.setConvertHtmlEntry(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IGNORE_INVALIDE_HTML_TAGS"))
            {
                htmlFilter.setIgnoreInvalideHtmlTags(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("ADD_RTL_DIRECTIONALITY"))
            {
                htmlFilter.setAddRtlDirectionality(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("WHITESPACE_PRESERVE"))
            {
                htmlFilter.setWhitespacePreserve(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("JS_FUNCTION_FILTER"))
            {
                htmlFilter.setJsFunctionText(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_PAIRED_TAGS"))
            {
                htmlFilter.setDefaultPairedTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("PAIRED_TAGS"))
            {
                htmlFilter.setPairedTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_UNPAIRED_TAGS"))
            {
                htmlFilter.setDefaultUnpairedTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNPAIRED_TAGS"))
            {
                htmlFilter.setUnpairedTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_SWITCH_TAG_MAPS"))
            {
                htmlFilter.setDefaultSwitchTagMaps(valueField);
            }
            else if (keyField.equalsIgnoreCase("SWITCH_TAG_MAPS"))
            {
                htmlFilter.setSwitchTagMaps(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_WHITE_PRESERVING_TAGS"))
            {
                htmlFilter.setDefaultWhitePreservingTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_INTERNAL_TAGS"))
            {
                htmlFilter.setDefaultInternalTagMaps(valueField);
            }
            else if (keyField.equalsIgnoreCase("INTERNAL_TAGS"))
            {
                htmlFilter.setInternalTagMaps(valueField);
            }
            else if (keyField.equalsIgnoreCase("WHITE_PRESERVING_TAGS"))
            {
                htmlFilter.setWhitePreservingTags(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_NON_TRANSLATABLE_META_ATTRIBUTES"))
            {
                htmlFilter.setDefaultNonTranslatableMetaAttributes(valueField);
            }
            else if (keyField.equalsIgnoreCase("NON_TRANSLATABLE_META_ATTRIBUTES"))
            {
                htmlFilter.setNonTranslatableMetaAttributes(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_TRANSLATABLE_ATTRIBUTES"))
            {
                htmlFilter.setDefaultTranslatableAttributes(valueField);
            }
            else if (keyField.equalsIgnoreCase("TRANSLATABLE_ATTRIBUTES"))
            {
                htmlFilter.setTranslatableAttributes(valueField);
            }
            else if (keyField.equalsIgnoreCase("DEFAULT_LOCALIZABLE_ATTRIBUTE_MAPS"))
            {
                htmlFilter.setDefaultLocalizableAttributeMaps(valueField);
            }
            else if (keyField.equalsIgnoreCase("LOCALIZABLE_ATTRIBUTE_MAPS"))
            {
                htmlFilter.setLocalizableAttributeMaps(valueField);
            }
        }
        return htmlFilter;
    }

    private InddFilter putDataIntoInddFilter(Map<String, String> valueMap)
    {
        InddFilter inddFilter = new InddFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                inddFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                inddFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                inddFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    inddFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    inddFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_HIDDEN_LAYER"))
            {
                inddFilter.setTranslateHiddenLayer(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_MASTER_LAYER"))
            {
                inddFilter.setTranslateMasterLayer(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_FILE_INFO"))
            {
                inddFilter.setTranslateFileInfo(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("EXTRACT_LINE_BREAK"))
            {
                inddFilter.setExtractLineBreak(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("REPLACE_NONBREAKING_SPACE"))
            {
                inddFilter.setReplaceNonbreakingSpace(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("TRANSLATE_HYPERLINKS"))
            {
                inddFilter.setTranslateHyperlinks(Boolean.parseBoolean(valueField));
            }
        }
        return inddFilter;
    }

    private QAFilter putDataIntoQAFilter(Map<String, String> valueMap)
    {
        QAFilter qaFilter = new QAFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                qaFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                qaFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                qaFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    qaFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    qaFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("CONFIG_XML"))
            {
                qaFilter.setConfigXml(valueField);
            }
        }
        return qaFilter;
    }

    private BaseFilter putDataIntoBaseFilter(Map<String, String> valueMap)
    {
        BaseFilter baseFilter = new BaseFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                baseFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                baseFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                baseFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    baseFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    baseFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("CONFIG_XML"))
            {
                baseFilter.setConfigXml(valueField);
            }
        }
        return baseFilter;
    }

    private BaseFilterMapping putDataIntoBaseFilterMapping(Map<String, String> valueMap)
    {
        BaseFilterMapping baseFilterMapping = new BaseFilterMapping();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                baseFilterMapping.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("base_filter_id"))
            {
                baseFilterMapping.setBaseFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("filter_table_name"))
            {
                baseFilterMapping.setFilterTableName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("filter_id"))
            {
                baseFilterMapping.setFilterId(Long.parseLong(valueField));
            }
        }
        return baseFilterMapping;
    }

    private JsonFilter putDataIntoJsonFilter(Map<String, String> valueMap)
    {
        JsonFilter jsonFilter = new JsonFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                jsonFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                jsonFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                jsonFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("ENABLE_SID_SUPPORT"))
            {
                jsonFilter.setEnableSidSupport(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("SID_FILTER_ID"))
            {
                SidFilter sf = new SidFilter();
                sf.setId(Long.parseLong(valueField));
                jsonFilter.setSidFilter(sf);
            }
            else if (keyField.equalsIgnoreCase("BASE_FILTER_ID"))
            {
                jsonFilter.setBaseFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("ELEMENT_POST_FILTER_ID"))
            {
                jsonFilter.setElementPostFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("ELEMENT_POST_FILTER_TABLE_NAME"))
            {
                jsonFilter.setElementPostFilterTableName(valueField);;
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    jsonFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    jsonFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
        }
        return jsonFilter;
    }
    
    private SidFilter putDataIntoSidFilter(Map<String, String> valueMap)
    {
        SidFilter sidFilter = new SidFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                sidFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                sidFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                sidFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("TYPE"))
            {
                sidFilter.setType(Integer.parseInt(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONFIG"))
            {
                sidFilter.setConfigXml(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                sidFilter.setCompanyId(Long.parseLong(companyId));
            }
            else if (keyField.equalsIgnoreCase("EXCLUSION_FILTER_ID"))
            {
                sidFilter.setExclusionFilterId(Long.parseLong(valueField));
            }
        }
        return sidFilter;
    }
    
    private GlobalExclusionFilter putDataIntoGlobalExclusionFilter(Map<String, String> valueMap)
    {
        GlobalExclusionFilter globalExclusionFilter = new GlobalExclusionFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                globalExclusionFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                globalExclusionFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                globalExclusionFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("TYPE"))
            {
                globalExclusionFilter.setType(Integer.parseInt(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONFIG"))
            {
                globalExclusionFilter.setConfigXml(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    globalExclusionFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    globalExclusionFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
        }
        return globalExclusionFilter;
    }

    private JavaPropertiesFilter putDataIntoJavaPropertiesFilter(Map<String, String> valueMap)
    {
        JavaPropertiesFilter javaPropertiesFilter = new JavaPropertiesFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                javaPropertiesFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                javaPropertiesFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                javaPropertiesFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("ENABLE_SID_SUPPORT"))
            {
                javaPropertiesFilter.setEnableSidSupport(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("SID_FILTER_ID"))
            {
                SidFilter sf = new SidFilter();
                sf.setId(Long.parseLong(valueField));
                javaPropertiesFilter.setSidFilter(sf);
            }
            else if (keyField.equalsIgnoreCase("ENABLE_UNICODE_ESCAPE"))
            {
                javaPropertiesFilter.setEnableUnicodeEscape(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("ENABLE_TRIM_SEGMENT"))
            {
                javaPropertiesFilter.setEnablePreserveSpaces(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_ID"))
            {
                javaPropertiesFilter.setSecondFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_TABLE_NAME"))
            {
                javaPropertiesFilter.setSecondFilterTableName(valueField);
            }
            else if (keyField.equalsIgnoreCase("INTERNAL_TEXTS"))
            {
                javaPropertiesFilter.setInternalText(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    javaPropertiesFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    javaPropertiesFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
        }
        return javaPropertiesFilter;
    }

    private JavaScriptFilter putDataIntoJavaScriptFilter(Map<String, String> valueMap)
    {
        JavaScriptFilter javaScriptFilter = new JavaScriptFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                javaScriptFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                javaScriptFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                javaScriptFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("JS_FUNCTION_FILTER"))
            {
                javaScriptFilter.setJsFunctionText(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    javaScriptFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    javaScriptFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("ENABLE_UNICODE_ESCAPE"))
            {
                javaScriptFilter.setEnableUnicodeEscape(Boolean.parseBoolean(valueField));
            }
        }
        return javaScriptFilter;
    }

    private JSPFilter putDataIntoJSPFilter(Map<String, String> valueMap)
    {
        JSPFilter jspFilter = new JSPFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                jspFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                jspFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                jspFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    jspFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    jspFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("IS_ADDITIONAL_HEAD_ADDED"))
            {
                jspFilter.setAddAdditionalHead(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_ESCAPE_ENTITY"))
            {
                jspFilter.setEnableEscapeEntity(Boolean.parseBoolean(valueField));
            }
        }
        return jspFilter;
    }

    private MSOffice2010Filter putDataIntoMSOffice2010Filter(Map<String, String> valueMap)
    {
        MSOffice2010Filter msOffice2010Filter = new MSOffice2010Filter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                msOffice2010Filter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                msOffice2010Filter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                msOffice2010Filter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_PARAGRAPH_STYLES"))
            {
                msOffice2010Filter.setParagraphStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_CHARACTER_STYLES"))
            {
                msOffice2010Filter.setCharacterStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_EXCEL_CELL_STYLES"))
            {
                msOffice2010Filter.setExcelCellStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("WORD_INTERNAL_TEXT_STYLES"))
            {
                msOffice2010Filter.setWordInternalTextStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("EXCEL_INTERNAL_TEXT_STYLES"))
            {
                msOffice2010Filter.setExcelInternalTextStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_HEADER_TRANSLATE"))
            {
                msOffice2010Filter.setHeaderTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_FOOTENDNOTE_TRANSLATE"))
            {
                msOffice2010Filter.setFootendnoteTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_MASTER_TRANSLATE"))
            {
                msOffice2010Filter.setMasterTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_FILEINFO_TRANSLATE"))
            {
                msOffice2010Filter.setFileinfoTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_NOTES_TRANSLATE"))
            {
                msOffice2010Filter.setNotesTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_PPTLAYOUT_TRANSLATE"))
            {
                msOffice2010Filter.setPptlayoutTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_NOTEMASTER_TRANSLATE"))
            {
                msOffice2010Filter.setNotemasterTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_HANDOUTMASTER_TRANSLATE"))
            {
                msOffice2010Filter.setHandoutmasterTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_EXCEL_TAB_NAMES_TRANSLATE"))
            {
                msOffice2010Filter.setExcelTabNamesTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_TOOLTIPS_TRANSLATE"))
            {
                msOffice2010Filter.setToolTipsTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_HIDDENTEXT_TRANSLATE"))
            {
                msOffice2010Filter.setHiddenTextTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_URL_TRANSLATE"))
            {
                msOffice2010Filter.setUrlTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_TOC_TRANSLATE"))
            {
                msOffice2010Filter.setTableOfContentTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    msOffice2010Filter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    msOffice2010Filter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("XML_FILTER_ID"))
            {
                msOffice2010Filter.setXmlFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_ID"))
            {
                msOffice2010Filter.setContentPostFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_TABLE_NAME"))
            {
                msOffice2010Filter.setContentPostFilterTableName(valueField);
            }
        }
        return msOffice2010Filter;
    }

    private MSOfficeDocFilter putDataIntoMSOfficeDocFilter(Map<String, String> valueMap)
    {
        MSOfficeDocFilter msOfficeDocFilter = new MSOfficeDocFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                msOfficeDocFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                msOfficeDocFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                msOfficeDocFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_PARAGRAPH_STYLES"))
            {
                msOfficeDocFilter.setParagraphStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_CHARACTER_STYLES"))
            {
                msOfficeDocFilter.setCharacterStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("INTERNAL_TEXT_CHARACTER_STYLES"))
            {
                msOfficeDocFilter.setInternalTextStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_HEADER_TRANSLATE"))
            {
                msOfficeDocFilter.setHeaderTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_ALT_TRANSLATE"))
            {
                msOfficeDocFilter.setAltTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_TOC_TRANSLATE"))
            {
                msOfficeDocFilter.setTocTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    msOfficeDocFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    msOfficeDocFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_ID"))
            {
                msOfficeDocFilter.setContentPostFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_TABLE_NAME"))
            {
                msOfficeDocFilter.setContentPostFilterTableName(valueField);
            }
        }
        return msOfficeDocFilter;
    }

    private MSOfficeExcelFilter putDataIntoMSOfficeExcelFilter(Map<String, String> valueMap)
    {
        MSOfficeExcelFilter msOfficeExcelFilter = new MSOfficeExcelFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                msOfficeExcelFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                msOfficeExcelFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                msOfficeExcelFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    msOfficeExcelFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    msOfficeExcelFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("IS_ALT_TRANSLATE"))
            {
                msOfficeExcelFilter.setAltTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_TAB_NAMES_TRANSLATE"))
            {
                msOfficeExcelFilter.setTabNamesTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_ID"))
            {
                msOfficeExcelFilter.setContentPostFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_TABLE_NAME"))
            {
                msOfficeExcelFilter.setContentPostFilterTableName(valueField);
            }
        }
        return msOfficeExcelFilter;
    }

    private MSOfficePPTFilter putDataIntoMSOfficePPTFilter(Map<String, String> valueMap)
    {
        MSOfficePPTFilter msOfficePPTFilter = new MSOfficePPTFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                msOfficePPTFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                msOfficePPTFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                msOfficePPTFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_ALT_TRANSLATE"))
            {
                msOfficePPTFilter.setAltTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_NOTES_TRANSLATE"))
            {
                msOfficePPTFilter.setNotesTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    msOfficePPTFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    msOfficePPTFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_ID"))
            {
                msOfficePPTFilter.setContentPostFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONTENT_POST_FILTER_TABLE_NAME"))
            {
                msOfficePPTFilter.setContentPostFilterTableName(valueField);
            }
        }
        return msOfficePPTFilter;
    }

    private OpenOfficeFilter putDataIntoOpenOfficeFilter(Map<String, String> valueMap)
    {
        OpenOfficeFilter openOfficeFilter = new OpenOfficeFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                openOfficeFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                openOfficeFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                openOfficeFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_PARAGRAPH_STYLES"))
            {
                openOfficeFilter.setParagraphStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("UNEXTRACTABLE_WORD_CHARACTER_STYLES"))
            {
                openOfficeFilter.setCharacterStyles(valueField);
            }
            else if (keyField.equalsIgnoreCase("IS_HEADER_TRANSLATE"))
            {
                openOfficeFilter.setHeaderTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("IS_FILEINFO_TRANSLATE"))
            {
                openOfficeFilter.setFileinfoTranslate(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    openOfficeFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    openOfficeFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("XML_FILTER_ID"))
            {
                openOfficeFilter.setXmlFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_ID"))
            {
                openOfficeFilter.setSecondFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_TABLE_NAME"))
            {
                openOfficeFilter.setSecondFilterTableName(valueField);
            }
        }
        return openOfficeFilter;
    }

    private PlainTextFilter putDataIntoPlainTextFilter(Map<String, String> valueMap)
    {
        PlainTextFilter plainTextFilter = new PlainTextFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                plainTextFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                plainTextFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                plainTextFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    plainTextFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    plainTextFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("CONFIG_XML"))
            {
                plainTextFilter.setConfigXml(valueField);
            }
        }
        return plainTextFilter;
    }

    private POFilter putDataIntoPOFilter(Map<String, String> valueMap)
    {
        POFilter poFilter = new POFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                poFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                poFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                poFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    poFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    poFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_ID"))
            {
                poFilter.setSecondFilterId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("SECOND_FILTER_TABLE_NAME"))
            {
                poFilter.setSecondFilterTableName(valueField);
            }
        }
        return poFilter;
    }

    private XMLRuleFilter putDataIntoXMLRuleFilter(Map<String, String> valueMap)
    {
        XMLRuleFilter xmlRuleFilter = new XMLRuleFilter();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);

            if (keyField.equalsIgnoreCase("ID"))
            {
                xmlRuleFilter.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_NAME"))
            {
                xmlRuleFilter.setFilterName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("FILTER_DESCRIPTION"))
            {
                xmlRuleFilter.setFilterDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("XML_RULE_ID"))
            {
                xmlRuleFilter.setXmlRuleId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("ENABLE_CONVERT_HTML_ENTITY"))
            {
                xmlRuleFilter.setConvertHtmlEntity(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    xmlRuleFilter.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    xmlRuleFilter.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("USE_XML_RULE"))
            {
                xmlRuleFilter.setUseXmlRule(Boolean.parseBoolean(valueField));
            }
            else if (keyField.equalsIgnoreCase("CONFIG_XML"))
            {
                xmlRuleFilter.setConfigXml(valueField);
            }
        }
        return xmlRuleFilter;
    }

    private XmlRuleFileImpl putDataIntoXMLRule(Map<String, String> valueMap)
    {
        XmlRuleFileImpl xmlRuleFileImpl = new XmlRuleFileImpl();
        String keyField = null;
        String valueField = null;
        Set<String> valueKey = valueMap.keySet();
        Iterator itor = valueKey.iterator();
        while (itor.hasNext())
        {
            keyField = (String) itor.next();
            valueField = valueMap.get(keyField);
            if (keyField.equalsIgnoreCase("ID"))
            {
                xmlRuleFileImpl.setId(Long.parseLong(valueField));
            }
            else if (keyField.equalsIgnoreCase("NAME"))
            {
                xmlRuleFileImpl.setName(containSpecialChar(valueField));
            }
            else if (keyField.equalsIgnoreCase("COMPANY_ID"))
            {
                if (importToCompId != null && !importToCompId.equals("-1"))
                {
                    xmlRuleFileImpl.setCompanyId(Long.parseLong(importToCompId));
                }
                else
                {
                    xmlRuleFileImpl.setCompanyId(Long.parseLong(companyId));
                }
            }
            else if (keyField.equalsIgnoreCase("DESCRIPTION"))
            {
                xmlRuleFileImpl.setDescription(valueField);
            }
            else if (keyField.equalsIgnoreCase("RULE_TEXT"))
            {
                xmlRuleFileImpl.setRuleText(valueField);
            }
        }
        return xmlRuleFileImpl;
    }
    
    /**
     * If the database does not have the filterName, then put the filterName
     * directly into the database. If the database has filterName, then
     * behind filterName plus _import_xx, and then stored in the database
     * 
     * @param filterName
     * @param filterTableName
     * @param newFilterName
     * */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String checkFilterNameExists(String filterName, String filterTableName)
    {
        String hql = null;
        if ("XmlRuleFileImpl".equalsIgnoreCase(filterTableName))
        {
            hql = "select f.name from " + filterTableName + "  f where f.companyId=:companyId";
        }
        else
        {
            hql = "select f.filterName from " + filterTableName
                    + "  f where f.companyId=:companyId";
        }
        Map map = new HashMap();
        map.put("companyId", Long.parseLong(companyId));
        List itList = HibernateUtil.search(hql, map);

        List<String> existedNames = new ArrayList<String>();
        for (int i = 0; i < itList.size(); i++)
        {
            String name = (String) itList.get(i);
            existedNames.add(name.toLowerCase());
        }

        if (existedNames.contains(filterName.toLowerCase()))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (filterName.contains("_import_"))
                {
                    returnStr = filterName.substring(0, filterName.lastIndexOf('_')) + "_"
                            + num;
                }
                else
                {
                    returnStr = filterName + "_import_" + num;
                }

                if (!existedNames.contains(returnStr.toLowerCase()))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return filterName;
        }
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p style='color:red'>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }

    private String containSpecialChar(String filterName)
    {
        if (filterName.indexOf("<") != -1 || filterName.indexOf(">") != -1
                || filterName.indexOf("'") != -1 || filterName.indexOf("\"") != -1
                || filterName.indexOf("&") != -1)
        {
            filterName = filterName.replaceAll("<", "");
            filterName = filterName.replaceAll(">", "");
            filterName = filterName.replaceAll("&", "");
            filterName = filterName.replaceAll("'", "");
            filterName = filterName.replaceAll("\"", "");
        }
        return filterName;
    }
}
