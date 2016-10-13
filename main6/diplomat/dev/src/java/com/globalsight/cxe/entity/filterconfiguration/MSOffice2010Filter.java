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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class MSOffice2010Filter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private boolean headerTranslate = true;
    private boolean footendnoteTranslate = false;
    private boolean masterTranslate = false;
    private boolean fileinfoTranslate = false;
    private boolean notesTranslate = false;
    private boolean pptlayoutTranslate = false;
    private boolean notemasterTranslate = false;
    private boolean handoutmasterTranslate = false;
    private boolean excelTabNamesTranslate = false;
    private boolean toolTipsTranslate = false;
    private boolean hiddenTextTranslate = false;
    private boolean urlTranslate = false;
    private boolean tableOfContentTranslate = false;
    private boolean commentTranslate = false;
    private long companyId;
    private long xmlFilterId = -2;
    private long contentPostFilterId = -2;
    private String contentPostFilterTableName = null;
    private String excelOrder = "n";

    private List<String> unextractableWordParagraphStyles = new ArrayList<String>();
    private List<String> unextractableWordCharacterStyles = new ArrayList<String>();
    private List<String> unextractableExcelCellStyles = new ArrayList<String>();
    private List<String> allParagraphStyles = new ArrayList<String>();
    private List<String> allCharacterStyles = new ArrayList<String>();
    private List<String> allExcelCellStyles = new ArrayList<String>();
    private List<String> allWordInternalTextStyles = new ArrayList<String>();
    private List<String> selectedWordInternalTextStyles = new ArrayList<String>();
    private List<String> allExcelInternalTextStyles = new ArrayList<String>();
    private List<String> selectedExcelInternalTextStyles = new ArrayList<String>();

    private final String ENTIEY_START = "<entities>";
    private final String ENTIEY_END = "</entities>";
    private final String STYLE = "<entity checked=\"{0}\">{1}</entity>";
    private final String SELECTED_STYLE = "<entity checked=\"true\">(.*?)</entity>";
    private final String ALL_STYLE = "<entity checked=\".*?\">(.*?)</entity>";

    public MSOffice2010Filter()
    {
        allParagraphStyles.add("DONOTTRANSLATE_para");
        allParagraphStyles.add("tw4winExternal");

        allCharacterStyles.add("DONOTTRANSLATE_char");
        allCharacterStyles.add("tw4winExternal");

        allWordInternalTextStyles.add("tw4winInternal");
        allExcelInternalTextStyles.add("tw4winInternal");
        allExcelCellStyles.add("tw4winExternal");
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from MSOffice2010Filter oof where oof.filterName =:filterName and oof.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from MSOffice2010Filter oof where oof.id<>:filterId and oof.filterName =:filterName and oof.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getParagraphStyles()
    {
        return buildToXml(unextractableWordParagraphStyles, allParagraphStyles);
    }

    public void setParagraphStyles(String styles)
    {
        unextractableWordParagraphStyles = getSelectedStyles(styles);
        allParagraphStyles = getAllStyles(styles);
        FilterHelper.sort(unextractableWordParagraphStyles);
        FilterHelper.sort(allParagraphStyles);
    }

    public void setParaStyles(String selectedStyles, String allStyles)
    {
        unextractableWordParagraphStyles = toList(selectedStyles);
        allParagraphStyles = toList(allStyles);
    }

    public String getExcelCellStyles()
    {
        return buildToXml(unextractableExcelCellStyles, allExcelCellStyles);
    }

    public void setExcelCellStyles(String styles)
    {
        unextractableExcelCellStyles = getSelectedStyles(styles);
        allExcelCellStyles = getAllStyles(styles);
        FilterHelper.sort(unextractableExcelCellStyles);
        FilterHelper.sort(allExcelCellStyles);
    }

    public String getCharacterStyles()
    {
        return buildToXml(unextractableWordCharacterStyles, allCharacterStyles);
    }

    public void setCharacterStyles(String styles)
    {
        unextractableWordCharacterStyles = getSelectedStyles(styles);
        allCharacterStyles = getAllStyles(styles);
        FilterHelper.sort(unextractableWordCharacterStyles);
        FilterHelper.sort(allCharacterStyles);
    }

    public void setCharStyles(String selectedStyles, String allStyles)
    {
        unextractableWordCharacterStyles = toList(selectedStyles);
        allCharacterStyles = toList(allStyles);
    }

    public void setExcelCellStyles(String selectedStyles, String allStyles)
    {
        unextractableExcelCellStyles = toList(selectedStyles);
        allExcelCellStyles = toList(allStyles);
    }

    public String getUnextractableWordParagraphStyles()
    {
        return toString(unextractableWordParagraphStyles);
    }

    public String getUnextractableWordCharacterStyles()
    {
        return toString(unextractableWordCharacterStyles);
    }

    public List<String> getUnextractableExcelCellStyles()
    {
        return unextractableExcelCellStyles;
    }

    public String getWordInternalTextStyles()
    {
        return buildToXml(selectedWordInternalTextStyles,
                allWordInternalTextStyles);
    }

    public String getExcelInternalTextStyles()
    {
        return buildToXml(selectedExcelInternalTextStyles,
                allExcelInternalTextStyles);
    }

    public void setWordInternalTextStyles(String styles)
    {
        selectedWordInternalTextStyles = getSelectedStyles(styles);
        allWordInternalTextStyles = getAllStyles(styles);
        FilterHelper.sort(selectedWordInternalTextStyles);
        FilterHelper.sort(allWordInternalTextStyles);
    }

    public void setExcelInternalTextStyles(String styles)
    {
        selectedExcelInternalTextStyles = getSelectedStyles(styles);
        allExcelInternalTextStyles = getAllStyles(styles);
        FilterHelper.sort(selectedExcelInternalTextStyles);
        FilterHelper.sort(allExcelInternalTextStyles);
    }

    public void setWordInTextStyles(String selectedStyles, String allStyles)
    {
        selectedWordInternalTextStyles = toList(selectedStyles);
        allWordInternalTextStyles = toList(allStyles);
    }

    public void setExcelInTextStyles(String selectedStyles, String allStyles)
    {
        selectedExcelInternalTextStyles = toList(selectedStyles);
        allExcelInternalTextStyles = toList(allStyles);
    }

    public String getSelectedWordInternalTextStyles()
    {
        return toString(selectedWordInternalTextStyles);
    }

    public String getSelectedExcelInternalTextStylesAsString()
    {
        return toString(selectedExcelInternalTextStyles);
    }

    public List<String> getSelectedExcelInternalTextStylesAsList()
    {
        return selectedExcelInternalTextStyles;
    }

    public static String toString(List<String> styles)
    {
        if (styles == null)
            return "";

        StringBuilder s = new StringBuilder();
        for (String style : styles)
        {
            style = style.trim();
            if (style.length() > 0)
            {
                if (s.length() > 0)
                {
                    s.append(",");
                }
                s.append(style);
            }
        }

        return s.toString();
    }

    public static ArrayList<String> toList(String s)
    {
        ArrayList<String> list = new ArrayList<String>();

        if (s == null)
            return list;

        for (String w : s.split(","))
        {
            w = w.trim();
            if (w.length() > 0)
            {
                list.add(w);
            }
        }

        return list;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from MSOffice2010Filter oof where oof.companyId="
                + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.OFFICE2010_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"unextractableWordParagraphStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(unextractableWordParagraphStyles)))
                .append("\"").append(",");
        sb.append("\"allParagraphStyles\":").append("\"")
                .append(FilterHelper.escape(toString(allParagraphStyles)))
                .append("\"").append(",");
        sb.append("\"unextractableWordCharacterStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(unextractableWordCharacterStyles)))
                .append("\"").append(",");
        sb.append("\"allCharacterStyles\":").append("\"")
                .append(FilterHelper.escape(toString(allCharacterStyles)))
                .append("\"").append(",");
        sb.append("\"unextractableExcelCellStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(unextractableExcelCellStyles)))
                .append("\"").append(",");
        sb.append("\"allExcelCellStyles\":").append("\"")
                .append(FilterHelper.escape(toString(allExcelCellStyles)))
                .append("\"").append(",");
        sb.append("\"selectedWordInternalTextStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(selectedWordInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"allWordInternalTextStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(allWordInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"selectedExcelInternalTextStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(selectedExcelInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"allExcelInternalTextStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(allExcelInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"headerTranslate\":").append(headerTranslate).append(",");
        sb.append("\"footendnoteTranslate\":").append(footendnoteTranslate)
                .append(",");
        sb.append("\"masterTranslate\":").append(masterTranslate).append(",");
        sb.append("\"fileinfoTranslate\":").append(fileinfoTranslate)
                .append(",");
        sb.append("\"notesTranslate\":").append(notesTranslate).append(",");
        sb.append("\"pptlayoutTranslate\":").append(pptlayoutTranslate)
                .append(",");
        sb.append("\"notemasterTranslate\":").append(notemasterTranslate)
                .append(",");
        sb.append("\"handoutmasterTranslate\":").append(handoutmasterTranslate)
                .append(",");
        sb.append("\"excelTabNamesTranslate\":").append(excelTabNamesTranslate)
                .append(",");
        sb.append("\"hiddenTextTranslate\":").append(hiddenTextTranslate)
                .append(",");
        sb.append("\"tableOfContentTranslate\":")
                .append(tableOfContentTranslate).append(",");
        sb.append("\"commentTranslate\":").append(commentTranslate).append(",");
        sb.append("\"toolTipsTranslate\":").append(toolTipsTranslate)
                .append(",");
        sb.append("\"urlTranslate\":").append(urlTranslate).append(",");
        sb.append("\"xmlFilterId\":").append(xmlFilterId).append(",");
        sb.append("\"contentPostFilterId\":").append(contentPostFilterId)
                .append(",");
        sb.append("\"contentPostFilterTableName\":").append("\"")
                .append(FilterHelper.escape(contentPostFilterTableName))
                .append("\",");
        sb.append("\"excelOrder\":").append("\"")
                .append(FilterHelper.escape(excelOrder)).append("\",");
        sb.append("\"baseFilterId\":")
                .append("\"")
                .append(BaseFilterManager.getBaseFilterIdByMapping(id,
                        FilterConstants.OFFICE2010_TABLENAME)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private List<String> getSelectedStyles(String xmlStyles)
    {
        return getStyles(SELECTED_STYLE, xmlStyles);
    }

    private List<String> getStyles(String regex, String xmlStyles)
    {
        List<String> styles = new ArrayList<String>();

        if (xmlStyles.length() > 0 && !xmlStyles.startsWith(ENTIEY_START))
        {
            styles.add(xmlStyles);
        }
        else
        {
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(xmlStyles);
            while (m.find())
            {
                String style = m.group(1).trim();
                if (style.length() > 0)
                {
                    styles.add(style);
                }
            }
        }

        return styles;
    }

    private List<String> getAllStyles(String xmlStyles)
    {
        return getStyles(ALL_STYLE, xmlStyles);
    }

    private String buildToXml(List<String> checkedStyles, List<String> allStyles)
    {
        SortUtil.sort(allStyles, new StringComparator(Locale.getDefault()));
        StringBuilder xml = new StringBuilder(ENTIEY_START);

        for (String style : allStyles)
        {
            style = style.trim();
            if (style.length() > 0)
            {
                boolean selected = checkedStyles.contains(style);
                xml.append(toXml(selected, style));
            }
        }

        xml.append(ENTIEY_END);

        return xml.toString();
    }

    private String toXml(boolean selected, String style)
    {
        return MessageFormat.format(STYLE, selected, style);
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

    public boolean isHeaderTranslate()
    {
        return headerTranslate;
    }

    public void setHeaderTranslate(boolean headerTranslate)
    {
        this.headerTranslate = headerTranslate;
    }

    public boolean isFootendnoteTranslate()
    {
        return footendnoteTranslate;
    }

    public void setFootendnoteTranslate(boolean footendnoteTranslate)
    {
        this.footendnoteTranslate = footendnoteTranslate;
    }

    public boolean isMasterTranslate()
    {
        return masterTranslate;
    }

    public void setMasterTranslate(boolean masterTranslate)
    {
        this.masterTranslate = masterTranslate;
    }

    public boolean isFileinfoTranslate()
    {
        return fileinfoTranslate;
    }

    public void setFileinfoTranslate(boolean fileinfoTranslate)
    {
        this.fileinfoTranslate = fileinfoTranslate;
    }

    public boolean isTableOfContentTranslate()
    {
        return tableOfContentTranslate;
    }

    public void setTableOfContentTranslate(boolean tableOfContentTranslate)
    {
        this.tableOfContentTranslate = tableOfContentTranslate;
    }

    public boolean isCommentTranslate()
    {
        return commentTranslate;
    }

    public void setCommentTranslate(boolean commentTranslate)
    {
        this.commentTranslate = commentTranslate;
    }

    public boolean isNotesTranslate()
    {
        return notesTranslate;
    }

    public void setNotesTranslate(boolean notesTranslate)
    {
        this.notesTranslate = notesTranslate;
    }

    public boolean isPptlayoutTranslate()
    {
        return pptlayoutTranslate;
    }

    public void setPptlayoutTranslate(boolean pptlayoutTranslate)
    {
        this.pptlayoutTranslate = pptlayoutTranslate;
    }

    public boolean isNotemasterTranslate()
    {
        return notemasterTranslate;
    }

    public void setNotemasterTranslate(boolean notemasterTranslate)
    {
        this.notemasterTranslate = notemasterTranslate;
    }

    public boolean isHandoutmasterTranslate()
    {
        return handoutmasterTranslate;
    }

    public void setHandoutmasterTranslate(boolean handoutmasterTranslate)
    {
        this.handoutmasterTranslate = handoutmasterTranslate;
    }

    public boolean isExcelTabNamesTranslate()
    {
        return excelTabNamesTranslate;
    }

    public void setExcelTabNamesTranslate(boolean excelTabNamesTranslate)
    {
        this.excelTabNamesTranslate = excelTabNamesTranslate;
    }

    public boolean isToolTipsTranslate()
    {
        return toolTipsTranslate;
    }

    public void setToolTipsTranslate(boolean toolTipsTranslate)
    {
        this.toolTipsTranslate = toolTipsTranslate;
    }

    public boolean isUrlTranslate()
    {
        return urlTranslate;
    }

    public void setUrlTranslate(boolean urlTranslate)
    {
        this.urlTranslate = urlTranslate;
    }

    public boolean isHiddenTextTranslate()
    {
        return hiddenTextTranslate;
    }

    public void setHiddenTextTranslate(boolean hiddenTextTranslate)
    {
        this.hiddenTextTranslate = hiddenTextTranslate;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public void setXmlFilterId(long xmlFilterId)
    {
        this.xmlFilterId = xmlFilterId;
    }

    public long getXmlFilterId()
    {
        return this.xmlFilterId;
    }

    public long getContentPostFilterId()
    {
        return contentPostFilterId;
    }

    public void setContentPostFilterId(long contentPostFilterId)
    {
        this.contentPostFilterId = contentPostFilterId;
    }

    public String getContentPostFilterTableName()
    {
        return contentPostFilterTableName;
    }

    public void setContentPostFilterTableName(String contentPostFilterTableName)
    {
        this.contentPostFilterTableName = contentPostFilterTableName;
    }

    public String getFilterTableName()
    {
        return FilterConstants.OFFICE2010_TABLENAME;
    }

    public String getExcelOrder()
    {
        return excelOrder;
    }

    public void setExcelOrder(String excelOrder)
    {
        this.excelOrder = excelOrder;
    }
}
