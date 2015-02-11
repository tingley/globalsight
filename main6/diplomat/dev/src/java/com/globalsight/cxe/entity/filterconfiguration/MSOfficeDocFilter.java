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

public class MSOfficeDocFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private boolean headerTranslate = true;
    private boolean altTranslate = false;
    private boolean tocTranslate = false;
    private long companyId;
    private long contentPostFilterId = -2;
    private String contentPostFilterTableName = null;

    private List<String> unextractableWordParagraphStyles = new ArrayList<String>();
    private List<String> unextractableWordCharacterStyles = new ArrayList<String>();
    private List<String> allParagraphStyles = new ArrayList<String>();
    private List<String> allCharacterStyles = new ArrayList<String>();
    private List<String> allInternalTextStyles = new ArrayList<String>();
    private List<String> selectedInternalTextStyles = new ArrayList<String>();

    private final String ENTIEY_START = "<entities>";
    private final String ENTIEY_END = "</entities>";
    private final String STYLE = "<entity checked=\"{0}\">{1}</entity>";
    private final String SELECTED_STYLE = "<entity checked=\"true\">(.*?)</entity>";
    private final String ALL_STYLE = "<entity checked=\".*?\">(.*?)</entity>";

    public MSOfficeDocFilter()
    {
        allParagraphStyles.add("DONOTTRANSLATE_para");
        allParagraphStyles.add("tw4winExternal");

        allCharacterStyles.add("DONOTTRANSLATE_char");
        allCharacterStyles.add("tw4winExternal");

        allInternalTextStyles.add("tw4winInternal");
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from MSOfficeDocFilter ms where ms.filterName =:filterName and ms.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName, long companyId)
    {
        String hql = "from MSOfficeDocFilter ms where ms.id<>:filterId and ms.filterName =:filterName and ms.companyId=:companyId";
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

    public String getUnextractableWordParagraphStyles()
    {
        return toString(unextractableWordParagraphStyles);
    }

    public String getUnextractableWordCharacterStyles()
    {
        return toString(unextractableWordCharacterStyles);
    }

    public String getInternalTextStyles()
    {
        return buildToXml(selectedInternalTextStyles, allInternalTextStyles);
    }

    public void setInternalTextStyles(String styles)
    {
        selectedInternalTextStyles = getSelectedStyles(styles);
        allInternalTextStyles = getAllStyles(styles);
        FilterHelper.sort(selectedInternalTextStyles);
        FilterHelper.sort(allInternalTextStyles);
    }

    public void setInTextStyles(String selectedStyles, String allStyles)
    {
        selectedInternalTextStyles = toList(selectedStyles);
        allInternalTextStyles = toList(allStyles);
    }

    public String getSelectedInternalTextStyles()
    {
        return toString(selectedInternalTextStyles);
    }

    private String toString(List<String> styles)
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

    private ArrayList<String> toList(String s)
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
        String hql = "from MSOfficeDocFilter ms where ms.companyId="
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
                .append("\"" + FilterConstants.MSOFFICEDOC_TABLENAME + "\"")
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
        sb.append("\"selectedInternalTextStyles\":")
                .append("\"")
                .append(FilterHelper
                        .escape(toString(selectedInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"allInternalTextStyles\":").append("\"")
                .append(FilterHelper.escape(toString(allInternalTextStyles)))
                .append("\"").append(",");
        sb.append("\"headerTranslate\":").append(headerTranslate).append(",");
        sb.append("\"altTranslate\":").append(altTranslate).append(",");
        sb.append("\"TOCTranslate\":").append(tocTranslate).append(",");
        sb.append("\"contentPostFilterId\":").append(contentPostFilterId)
                .append(",");
        sb.append("\"contentPostFilterTableName\":").append("\"")
                .append(FilterHelper.escape(contentPostFilterTableName))
                .append("\",");
        sb.append("\"baseFilterId\":")
                .append("\"")
                .append(BaseFilterManager.getBaseFilterIdByMapping(id,
                        FilterConstants.MSOFFICEDOC_TABLENAME)).append("\"");
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

    public boolean isAltTranslate()
    {
        return altTranslate;
    }

    public void setAltTranslate(boolean altTranslate)
    {
        this.altTranslate = altTranslate;
    }

    public boolean isTocTranslate()
    {
        return tocTranslate;
    }

    public boolean getTocTranslate()
    {
        return tocTranslate;
    }

    public void setTocTranslate(boolean tocTranslate)
    {
        this.tocTranslate = tocTranslate;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getFilterTableName()
    {
        return FilterConstants.MSOFFICEDOC_TABLENAME;
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
}
