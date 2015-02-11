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
package com.globalsight.ling.docproc.extractor.html;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeExcelFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficePPTFilter;
import com.globalsight.cxe.entity.filterconfiguration.POFilter;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.msoffice.DynamicExcelRules;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * <p>
 * Keeps tag information and extraction rules that guide the extraction of
 * translatable and localizable attributes in mixed file formats.
 * </p>
 * <p>
 * Information is pulled from the file Tags.properties.
 * </p>
 */
public class ExtractionRules
{
    private boolean s_debug = false;

    private Map<String, Object> s_mapTranslatableAttrs = new HashMap<String, Object>();
    private Map<String, Object> s_mapWhitePreservingTags = new HashMap<String, Object>();
    private Map<String, Object> s_mapNonTranslatableMetaAttrs = new HashMap<String, Object>();
    private Map<String, Object> s_mapTranslatableJspParams = new HashMap<String, Object>();
    private Map<String, Object> s_mapUnpairedTags = new HashMap<String, Object>();
    private Map<String, Object> s_mapPairedTags = new HashMap<String, Object>();
    private Map<String, Object> s_mapInlineTags = new HashMap<String, Object>();
    private Map<String, String> s_mapSwitchTags = new HashMap<String, String>();

    private String s_spacerGif = "";
    private boolean s_extractSSIInclude = true;
    private String fileProfileId;
    private long filterId = -1;

    private boolean useContentPostFilter = false;
    private boolean useInternalTextFilter = false;
    private Map<String, Object> contentPostFilter_mapTranslatableAttrs = new HashMap<String, Object>();
    private Map<String, Object> contentPostFilter_mapInlineTags = new HashMap<String, Object>();
    private Map<String, Object> contentPostFilter_mapPairedTags = new HashMap<String, Object>();
    private List<InternalText> internalTextList = new ArrayList<InternalText>();

    // for testing purpose
    void setContentPostFilterTranslableAttrMap(Map<String, Object> map)
    {
        contentPostFilter_mapTranslatableAttrs = map;
    }

    // for testing purpose
    void setContentPostFilterInlineTagMap(Map<String, Object> map)
    {
        contentPostFilter_mapInlineTags = map;
    }

    // for testing purpose
    void setInternalTextList(List<InternalText> list)
    {
        internalTextList = list;
    }

    void setSpacerGif(ResourceBundle res, String key)
    {
        String value = res.getString(key);

        s_spacerGif = value.toLowerCase().trim();
    }

    void setDebugFlag(ResourceBundle res, String key)
    {
        String value = res.getString(key);

        if (value.equalsIgnoreCase("true"))
        {
            s_debug = true;
        }
        else
        {
            s_debug = false;
        }
    }

    void setSSIInclude(ResourceBundle res, String key)
    {
        String value = res.getString(key);

        if (value.equalsIgnoreCase("true"))
        {
            s_extractSSIInclude = true;
        }
        else
        {
            s_extractSSIInclude = false;
        }
    }

    void fillBooleanMap(ResourceBundle res, String key, Map<String, Object> map)
    {
        String value = res.getString(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String tag = tok.nextToken().trim().toLowerCase();
            map.put(tag, null);
        }
    }

    void fillBooleanMap(String value, Map<String, Object> map)
    {
        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String tag = tok.nextToken().trim().toLowerCase();
            map.put(tag, null);
        }
    }

    void fillEmbeddableTags(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getEmbeddableTags();
        fillBooleanMap(value, map);
    }

    void fillPairedTags(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getPairedTags();
        fillBooleanMap(value, map);
    }

    void fillUnPairedTags(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getUnpairedTags();
        fillBooleanMap(value, map);
    }

    void fillWhitePreservingTags(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getWhitePreservingTags();
        fillBooleanMap(value, map);
    }

    void fillNonTranslatableMetaAttrs(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getNonTranslatableMetaAttributes();
        fillBooleanMap(value, map);
    }

    void fillTranslatableAttrs(HtmlFilter filter, Map<String, Object> map)
    {
        String value = filter.getTranslatableAttributes();
        fillBooleanMap(value, map);
    }

    void fillLocalizableAttrs(HtmlFilter filter, Map<String, String> map)
    {
        String line = filter.getLocalizableAttributeMaps();
        fillMapMap(line, map);
    }

    void fillSwitchTags(HtmlFilter filter, Map<String, String> map)
    {
        String line = filter.getSwitchTagMaps();
        fillMapMap(line, map);
    }

    void fillMapMap(String line, Map<String, String> map)
    {
        StringTokenizer tok = new StringTokenizer(line, ",");
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken().trim();
            StringTokenizer tok1 = new StringTokenizer(token, ":");
            while (tok1.hasMoreTokens())
            {
                String tag = tok1.nextToken().trim().toLowerCase();
                String val = tok1.nextToken().trim();
                map.put(tag, val);
            }
        }
    }

    void fillMapMap(ResourceBundle res, String key, Map<String, String> map)
    {
        String line = res.getString(key);

        StringTokenizer tok = new StringTokenizer(line, ",");
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken().trim();
            StringTokenizer tok1 = new StringTokenizer(token, ":");
            while (tok1.hasMoreTokens())
            {
                String tag = tok1.nextToken().trim().toLowerCase();
                String val = tok1.nextToken().trim();
                map.put(tag, val);
            }
        }
    }

    void setConfigurationValues()
    {
        try
        {
            Boolean altTranslate = null;
            HtmlFilter htmlFilter = null;
            HtmlFilter contentPostFilter = null;
            BaseFilter bf = null; // internal text post-filter
            if (fileProfileId == null && filterId > -1)
            {
                htmlFilter = FilterHelper.getHtmlFilter(filterId);
            }
            if (fileProfileId != null && !"".equals(fileProfileId)
                    && Long.parseLong(this.fileProfileId) > 0)
            {
                FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class,
                        Long.valueOf(fileProfileId), false);
                long mainFilterId = fp.getFilterId();
                String mainFilterTableName = fp.getFilterTableName();

                if (FilterConstants.HTML_TABLENAME
                        .equalsIgnoreCase(mainFilterTableName))
                {
                    htmlFilter = FilterHelper.getHtmlFilter(mainFilterId);
                    bf = BaseFilterManager.getBaseFilterByMapping(
                            htmlFilter.getId(), FilterConstants.HTML_TABLENAME);
                }
                else if (FilterConstants.MSOFFICEDOC_TABLENAME
                        .equalsIgnoreCase(mainFilterTableName))
                {
                    MSOfficeDocFilter docFilter = (MSOfficeDocFilter) FilterHelper
                            .getFilter(mainFilterTableName, mainFilterId);
                    long contentPostFilterId = docFilter
                            .getContentPostFilterId();
                    if (contentPostFilterId > 0)
                    {
                        contentPostFilter = FilterHelper
                                .getHtmlFilter(contentPostFilterId);
                    }
                    altTranslate = docFilter.isAltTranslate();
                    bf = BaseFilterManager.getBaseFilterByMapping(
                            docFilter.getId(),
                            FilterConstants.MSOFFICEDOC_TABLENAME);
                }
                else if (FilterConstants.MSOFFICEEXCEL_TABLENAME
                        .equalsIgnoreCase(mainFilterTableName))
                {
                    MSOfficeExcelFilter excelFilter = (MSOfficeExcelFilter) FilterHelper
                            .getFilter(mainFilterTableName, mainFilterId);
                    long contentPostFilterId = excelFilter
                            .getContentPostFilterId();
                    if (contentPostFilterId > 0)
                    {
                        contentPostFilter = FilterHelper
                                .getHtmlFilter(contentPostFilterId);
                    }
                    altTranslate = excelFilter.isAltTranslate();
                    bf = BaseFilterManager.getBaseFilterByMapping(
                            excelFilter.getId(),
                            FilterConstants.MSOFFICEEXCEL_TABLENAME);
                }
                else if (FilterConstants.MSOFFICEPPT_TABLENAME
                        .equalsIgnoreCase(mainFilterTableName))
                {
                    MSOfficePPTFilter pptFilter = (MSOfficePPTFilter) FilterHelper
                            .getFilter(mainFilterTableName, mainFilterId);
                    long contentPostFilterId = pptFilter
                            .getContentPostFilterId();
                    if (contentPostFilterId > 0)
                    {
                        contentPostFilter = FilterHelper
                                .getHtmlFilter(contentPostFilterId);
                    }
                    altTranslate = pptFilter.isAltTranslate();
                    bf = BaseFilterManager.getBaseFilterByMapping(
                            pptFilter.getId(),
                            FilterConstants.MSOFFICEPPT_TABLENAME);
                }
                else if (FilterConstants.JAVAPROPERTIES_TABLENAME
                        .equalsIgnoreCase(mainFilterTableName))
                {
                    JavaPropertiesFilter proFilter = (JavaPropertiesFilter) FilterHelper
                            .getFilter(mainFilterTableName, mainFilterId);
                    long secondFilterId = proFilter.getSecondFilterId();
                    if (secondFilterId > 0)
                    {
                        htmlFilter = FilterHelper.getHtmlFilter(secondFilterId);
                    }
                }
                else if (FilterConstants.PO_TABLENAME.equalsIgnoreCase(mainFilterTableName))
                {
                    POFilter poFilter = (POFilter) FilterHelper
                            .getFilter(mainFilterTableName, mainFilterId);
                    long secondFilterId = poFilter.getSecondFilterId();
                    String secondFilterTableName = poFilter.getSecondFilterTableName();
                    if (secondFilterId > 0
                            && FilterConstants.HTML_TABLENAME
                                    .equals(secondFilterTableName))
                    {
                        htmlFilter = FilterHelper.getHtmlFilter(secondFilterId);
                    }
                }
            }

            if (contentPostFilter != null)
            {
                useContentPostFilter = true;
            }
            if (bf != null)
            {
                internalTextList = BaseFilterManager.getInternalTexts(bf);
                if (!internalTextList.isEmpty())
                {
                    useInternalTextFilter = true;
                }
            }

            ResourceBundle res = ResourceBundle.getBundle("properties/Tags",
                    Locale.US);

            Enumeration<String> keys = res.getKeys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                String tmp = key.toLowerCase();

                if (tmp.startsWith("inlinetag"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillEmbeddableTags(htmlFilter, s_mapInlineTags);
                    }
                    else
                    {
                        fillBooleanMap(res, key, s_mapInlineTags);
                    }
                    if (contentPostFilter != null && tmp.endsWith("_html"))
                    {
                        fillEmbeddableTags(contentPostFilter,
                                contentPostFilter_mapInlineTags);
                    }
                }
                else if (tmp.startsWith("pairedtag"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillPairedTags(htmlFilter, s_mapPairedTags);
                    }
                    else
                    {
                        fillBooleanMap(res, key, s_mapPairedTags);
                    }
                    if (contentPostFilter != null && tmp.endsWith("_html"))
                    {
                        fillPairedTags(contentPostFilter,
                                contentPostFilter_mapPairedTags);
                    }
                }
                else if (tmp.startsWith("unpairedtag"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillUnPairedTags(htmlFilter, s_mapUnpairedTags);
                    }
                    else
                    {
                        fillBooleanMap(res, key, s_mapUnpairedTags);
                    }
                }
                else if (tmp.startsWith("whitepreservingtag"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillWhitePreservingTags(htmlFilter,
                                s_mapWhitePreservingTags);
                    }
                    else
                    {
                        fillBooleanMap(res, key, s_mapWhitePreservingTags);
                    }
                }
                else if (tmp.startsWith("nontranslatablemetaattribute"))
                {
                    fillBooleanMap(res, key, s_mapNonTranslatableMetaAttrs);
                }
                else if (tmp.startsWith("translatablejspparam"))
                {
                    fillBooleanMap(res, key, s_mapTranslatableJspParams);
                }
                else if (tmp.startsWith("translatableattribute"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillTranslatableAttrs(htmlFilter,
                                s_mapTranslatableAttrs);
                    }
                    else
                    {
                        fillBooleanMap(res, key, s_mapTranslatableAttrs);
                    }
                    if (contentPostFilter != null && tmp.endsWith("_html"))
                    {
                        fillTranslatableAttrs(contentPostFilter,
                                contentPostFilter_mapTranslatableAttrs);
                    }
                }
                else if (tmp.startsWith("localizableattributemap"))
                {
                    // ignore this option currently
                }
                else if (tmp.startsWith("switchtagmap"))
                {
                    if (htmlFilter != null && tmp.endsWith("_html"))
                    {
                        fillSwitchTags(htmlFilter, s_mapSwitchTags);
                    }
                    else
                    {
                        fillMapMap(res, key, s_mapSwitchTags);
                    }

                }
                else if (tmp.startsWith("spacergif"))
                {
                    setSpacerGif(res, key);
                }
                else if (tmp.equals("extractssi"))
                {
                    setSSIInclude(res, key);
                }
                else if (tmp.equals("debug"))
                {
                    setDebugFlag(res, key);
                }
            }
            if (altTranslate != null)
            {
                if (altTranslate)
                {
                    if (s_mapTranslatableAttrs.get("alt") == null)
                    {
                        s_mapTranslatableAttrs.put("alt", null);
                    }
                }
                else
                {
                    s_mapTranslatableAttrs.remove("alt");
                }
            }
            if (useContentPostFilter)
            {
                // Excel merged cells have br tag in the content.
                // Add br to inline tags for content post-filter process.
                s_mapInlineTags.put("br", null);
            }
            // MS Office Hack: add <o:p>
            s_mapInlineTags.put("o:p", null);
        }
        catch (MissingResourceException e)
        {
            System.err.println("ExtractionRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
        catch (Throwable e)
        {
            System.err.println("ExtractionRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
    }

    public boolean useContentPostFilter()
    {
        return useContentPostFilter;
    }

    public boolean useInternalTextFilter()
    {
        return useInternalTextFilter;
    }

    void setConfigurationValues(HtmlFilter filter)
    {
        ResourceBundle res = ResourceBundle.getBundle("properties/Tags",
                Locale.US);

        Enumeration<String> keys = res.getKeys();
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            String tmp = key.toLowerCase();

            if (tmp.startsWith("inlinetag"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    // Only for pure Html file.
                    fillEmbeddableTags(filter, s_mapInlineTags);
                }
                else
                {
                    fillBooleanMap(res, key, s_mapInlineTags);
                }
            }
            else if (tmp.startsWith("pairedtag"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    fillPairedTags(filter, s_mapPairedTags);
                }
                else
                {
                    fillBooleanMap(res, key, s_mapPairedTags);
                }
            }
            else if (tmp.startsWith("unpairedtag"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    fillUnPairedTags(filter, s_mapUnpairedTags);
                }
                else
                {
                    fillBooleanMap(res, key, s_mapUnpairedTags);
                }
            }
            else if (tmp.startsWith("whitepreservingtag"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    fillWhitePreservingTags(filter, s_mapWhitePreservingTags);
                }
                else
                {
                    fillBooleanMap(res, key, s_mapWhitePreservingTags);
                }
            }
            else if (tmp.startsWith("nontranslatablemetaattribute"))
            {
                fillBooleanMap(res, key, s_mapNonTranslatableMetaAttrs);
            }
            else if (tmp.startsWith("translatablejspparam"))
            {
                fillBooleanMap(res, key, s_mapTranslatableJspParams);
            }
            else if (tmp.startsWith("translatableattribute"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    fillTranslatableAttrs(filter, s_mapTranslatableAttrs);
                }
                else
                {
                    fillBooleanMap(res, key, s_mapTranslatableAttrs);
                }
            }
            else if (tmp.startsWith("localizableattributemap"))
            {

            }
            else if (tmp.startsWith("switchtagmap"))
            {
                if (filter != null && tmp.endsWith("_html"))
                {
                    fillSwitchTags(filter, s_mapSwitchTags);
                }
                else
                {
                    fillMapMap(res, key, s_mapSwitchTags);
                }
            }
            else if (tmp.startsWith("spacergif"))
            {
                setSpacerGif(res, key);
            }
            else if (tmp.equals("extractssi"))
            {
                setSSIInclude(res, key);
            }
            else if (tmp.equals("debug"))
            {
                setDebugFlag(res, key);
            }

            // MS Office Hack: add <o:p>
            s_mapInlineTags.put("o:p", null);
        }
    }

    //
    // Private & Protected Constants
    //

    /**
     * <p>
     * Map that holds exceptional extraction rules.
     * </p>
     */
    private DynamicRules m_rules = new DynamicRules();

    //
    // Constructors
    //
    public ExtractionRules(String fileProfileId, long filterId)
    {
        this.fileProfileId = fileProfileId;
        this.filterId = filterId;
        setConfigurationValues();
    }

    public ExtractionRules(HtmlFilter filter)
    {
        setConfigurationValues(filter);
    }

    public ExtractionRules()
    {

    }

    /**
     * <p>
     * Loads rules to guide extraction process from a string.
     * </p>
     * 
     * <p>
     * String format may be list of: - Extract: TAG.ATTR
     * localizable|translatable item_type - DontExtract: TAG.ATTR - ExtractRule:
     * RULE - DontExtractRule: RULE
     */
    public final void loadRules(String p_rules) throws ExtractorException
    {
        // not implemented yet
    }

    /**
     * <p>
     * Loads rules to guide extraction process from an object.
     * </p>
     */
    public final void loadRules(Object p_rules) throws ExtractorException
    {
        if (p_rules != null && p_rules instanceof DynamicRules)
        {
            m_rules = (DynamicRules) p_rules;
        }
    }

    //
    // Format-specific stuff (MS-Office HTML)
    //

    public boolean doExtractScripts()
    {
        return m_rules.doExtractScripts();
    }

    public boolean doExtractStylesheets()
    {
        return m_rules.doExtractStylesheets();
    }

    public boolean doExtractXml()
    {
        return m_rules.doExtractXml();
    }

    public boolean doExtractNumbers()
    {
        return m_rules.doExtractNumbers();
    }

    public boolean doExtractCharset()
    {
        return m_rules.doExtractCharset();
    }

    public void setExtractCharset(boolean charset)
    {
        m_rules.setExtractCharset(charset);
    }

    //
    // Word-specific stuff
    //

    public boolean canExtractWordParaStyle(String p_style)
    {
        if (m_rules != null)
        {
            return m_rules.canExtractWordParaStyle(p_style);
        }

        return true;
    }

    public boolean canExtractWordCharStyle(String p_style)
    {
        if (m_rules != null)
        {
            return m_rules.canExtractWordCharStyle(p_style);
        }

        return true;
    }

    public boolean isInternalTextCharStyle(String p_style)
    {
        if (m_rules != null)
        {
            return m_rules.isInternalTextCharStyle(p_style);
        }

        return false;
    }

    //
    // Excel-specific stuff
    //

    public boolean canExtractExcelCellStyle(String p_style)
    {
        if (m_rules != null)
        {
            return m_rules.canExtractExcelCellStyle(p_style);
        }

        return true;
    }

    //
    // HTML-specific stuff
    //

    public final boolean doExtractSSIInclude()
    {
        return s_extractSSIInclude;
    }

    public final String getLocalizableAttribType(String p_tag, String p_attr)
    {
        // Exceptional cases from the 3.0 code
        if (p_attr.equalsIgnoreCase("lang"))
        {
            return p_attr.toLowerCase();
        }

        String key = p_tag + "." + p_attr;
        key = key.toLowerCase();
        // String result = (String)s_mapLocalizableAttrs.get(key);
        //
        // if (result == null)
        // {
        // // TODO: Use System 4 Logger
        // if (s_debug)
        // {
        // System.err.println("ExtractionRules.getLocalizableAttribType(): " +
        // "unknown tag.attr combination " + key);
        // }
        // result = key;
        // }
        //
        // return result;
        return key;
    }

    public final boolean isLocalizableAttribute(String p_tag, String p_attr)
    {
        boolean result = false;

        // HtmlFilter.localizableAttributeMaps is unusable now.
        // dynamic rules take precedence
        // if (m_rules.canLocalizeAttribute(p_tag, p_attr))
        // {
        // String key = p_tag + "." + p_attr;
        // key = key.toLowerCase();
        //
        // result = s_mapLocalizableAttrs.containsKey(key);
        //
        // // Handle VALUE attribute special. Customers can extract all
        // // values by adding "value" to Tags.properties.
        // //
        // // We should really make the list of tag.value explicit!!
        // }

        if (!result && p_attr.equalsIgnoreCase("value"))
        {
            result = !(p_tag.equalsIgnoreCase("option") || p_tag
                    .equalsIgnoreCase("button"));
        }

        return result;
    }

    public final List<InternalText> getInternalTextList()
    {
        return internalTextList;
    }

    public final boolean isTranslatableAttribute(String p_tag, String p_attr)
    {
        String key = p_attr.toLowerCase();
        return s_mapTranslatableAttrs.containsKey(key);
    }

    public final boolean isContentTranslatableAttribute(String p_attr)
    {
        String key = p_attr.toLowerCase();
        return contentPostFilter_mapTranslatableAttrs.containsKey(key);
    }

    // Should be handled by a rule
    public final boolean isNonTranslatableMetaAttribute(String p_attr)
    {
        String key = p_attr.toLowerCase();
        return s_mapNonTranslatableMetaAttrs.containsKey(key);
    }

    public final boolean isTranslatableJspParam(String p_attr)
    {
        String key = p_attr.toLowerCase();
        return s_mapTranslatableJspParams.containsKey(key);
    }

    public final boolean isInlineTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return s_mapInlineTags.containsKey(key);
    }

    public final boolean isContentInlineTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return contentPostFilter_mapInlineTags.containsKey(key);
    }

    public final boolean isContentPairedTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return contentPostFilter_mapPairedTags.containsKey(key);
    }

    public final boolean isPairedTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return s_mapPairedTags.containsKey(key);
    }

    public final boolean isUnpairedTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return s_mapUnpairedTags.containsKey(key);
    }

    public final boolean isSwitchTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return s_mapSwitchTags.containsKey(key);
    }

    public final boolean isExcelTractionRules()
    {
        return m_rules instanceof DynamicExcelRules;
    }

    // This is actually not used. Switching is hardcoded to what the
    // tag name says (script, style, xml).
    public final String getSwitchTagFormat(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return (String) s_mapSwitchTags.get(key);
    }

    public final boolean isWhitePreservingTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return s_mapWhitePreservingTags.containsKey(key);
    }

    // XXX to be implemented
    public final boolean isEventHandlerAttribute(String p_attr)
    {
        // <tag onEvent="...">
        if (p_attr.toLowerCase().startsWith("on"))
        {
            return true;
        }

        return false;
    }

    /**
     * <P>
     * ColdFusion tags come in 2 flavors: some look like html tags and follow
     * the same rules for attribute syntax, the others take CFSCRIPT
     * expressions: &lt;CFSET a=mid(someval) & "-xx"&gt;.
     * </P>
     * 
     * <P>
     * CFSCRIPT is based on JavaScript but contains ## expressions that work
     * like eval() or lisp-style backquote `() inside macros. An empty ##
     * resolves to a single # character. ## can appear inside strings but not
     * around arguments inside functions.
     * </P>
     * 
     * <P>
     * Legal example:
     * </P>
     * 
     * <PRE>
     * &lt;CFSET Sentence="The length of the full name
     *        is #Len("#FirstName# #LastName#")#"&gt;
     * </PRE>
     */
    // Synch these tags with the ones hardcoded in gs_html.jj.
    static final String strExpressionTags = "*CFIF*CFELSEIF*CFSET*";

    public static final boolean isCFExpressionTag(String p_strTagName)
    {
        return strExpressionTags
                .indexOf("*" + p_strTagName.toUpperCase() + "*") != -1;
    }

    /**
     * Is this attribute value (of <IMG SRC="...">) the same as the spacer gif
     * URL defined for this system?
     */
    public final boolean isSpacerGif(String p_attr)
    {
        if (s_spacerGif == null || s_spacerGif.length() == 0 || p_attr == null
                || p_attr.length() == 0)
        {
            return false;
        }

        if (p_attr.toLowerCase().indexOf(s_spacerGif) >= 0)
        {
            return true;
        }

        return false;
    }
}
