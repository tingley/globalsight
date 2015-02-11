package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class HtmlFilter implements Filter
{
    static private final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(HtmlFilter.class);

    private long id;
    private String filterName;
    private String filterDescription;
    private String defaultEmbeddableTags;
    private String embeddableTags;
    private String placeHolderTrim;
    private long companyId;
    private boolean convertHtmlEntry;
    private boolean ignoreInvalideHtmlTags;
    private String jsFunctionText;
    private String defaultPairedTags;
    private String pairedTags;
    private String defaultUnpairedTags;
    private String unpairedTags;
    private String defaultSwitchTagMaps;
    private String switchTagMaps;
    private String defaultInternalTagMaps = "";
    private String internalTagMaps = "";
    private String defaultWhitePreservingTags;
    private String whitePreservingTags;
    private String defaultNonTranslatableMetaAttributes;
    private String nonTranslatableMetaAttributes;
    private String defaultTranslatableAttributes;
    private String translatableAttributes;
    private String defaultLocalizableAttributeMaps = "";
    private String localizableAttributeMaps = "";

    public String getDefaultPairedTags()
    {
        return defaultPairedTags;
    }

    public void setDefaultPairedTags(String defaultPairedTags)
    {
        this.defaultPairedTags = defaultPairedTags;
    }

    public String getPairedTags()
    {
        return pairedTags;
    }

    public void setPairedTags(String pairedTags)
    {
        this.pairedTags = pairedTags;
    }

    public String getDefaultUnpairedTags()
    {
        return defaultUnpairedTags;
    }

    public void setDefaultUnpairedTags(String defaultUnpairedTags)
    {
        this.defaultUnpairedTags = defaultUnpairedTags;
    }

    public String getUnpairedTags()
    {
        return unpairedTags;
    }

    public void setUnpairedTags(String unpairedTags)
    {
        this.unpairedTags = unpairedTags;
    }

    public String getDefaultSwitchTagMaps()
    {
        return defaultSwitchTagMaps;
    }

    public void setDefaultSwitchTagMaps(String defaultSwitchTagMaps)
    {
        this.defaultSwitchTagMaps = defaultSwitchTagMaps;
    }

    public String getSwitchTagMaps()
    {
        return switchTagMaps;
    }

    public void setSwitchTagMaps(String switchTagMaps)
    {
        this.switchTagMaps = switchTagMaps;
    }

    public String getDefaultWhitePreservingTags()
    {
        return defaultWhitePreservingTags;
    }

    public void setDefaultWhitePreservingTags(String defaultWhitePreservingTags)
    {
        this.defaultWhitePreservingTags = defaultWhitePreservingTags;
    }

    public String getWhitePreservingTags()
    {
        return whitePreservingTags;
    }

    public void setWhitePreservingTags(String whitePreservingTags)
    {
        this.whitePreservingTags = whitePreservingTags;
    }

    public String getDefaultNonTranslatableMetaAttributes()
    {
        return defaultNonTranslatableMetaAttributes;
    }

    public void setDefaultNonTranslatableMetaAttributes(
            String defaultNonTranslatableMetaAttributes)
    {
        this.defaultNonTranslatableMetaAttributes = defaultNonTranslatableMetaAttributes;
    }

    public String getNonTranslatableMetaAttributes()
    {
        return nonTranslatableMetaAttributes;
    }

    public void setNonTranslatableMetaAttributes(
            String nonTranslatableMetaAttributes)
    {
        this.nonTranslatableMetaAttributes = nonTranslatableMetaAttributes;
    }

    public String getDefaultTranslatableAttributes()
    {
        return defaultTranslatableAttributes;
    }

    public void setDefaultTranslatableAttributes(
            String defaultTranslatableAttributes)
    {
        this.defaultTranslatableAttributes = defaultTranslatableAttributes;
    }

    public String getTranslatableAttributes()
    {
        return translatableAttributes;
    }

    public void setTranslatableAttributes(String translatableAttributes)
    {
        this.translatableAttributes = translatableAttributes;
    }

    public String getDefaultLocalizableAttributeMaps()
    {
        return defaultLocalizableAttributeMaps;
    }

    public void setDefaultLocalizableAttributeMaps(
            String defaultLocalizableAttributeMaps)
    {
        this.defaultLocalizableAttributeMaps = defaultLocalizableAttributeMaps;
    }

    public String getLocalizableAttributeMaps()
    {
        return localizableAttributeMaps;
    }

    public void setLocalizableAttributeMaps(String localizableAttributeMaps)
    {
        // localizableAttributeMaps is unusable now.
        // this.localizableAttributeMaps = localizableAttributeMaps;
    }

    public String getJsFunctionText()
    {
        return jsFunctionText;
    }

    public void setJsFunctionText(String jsFunctionText)
    {
        this.jsFunctionText = jsFunctionText;
    }

    public String getDefaultEmbeddableTags()
    {
        return defaultEmbeddableTags;
    }

    public void setDefaultEmbeddableTags(String defaultEmbeddableTags)
    {
        this.defaultEmbeddableTags = defaultEmbeddableTags;
    }

    public boolean isConvertHtmlEntry()
    {
        return convertHtmlEntry;
    }

    public void setConvertHtmlEntry(boolean convertHtmlEntry)
    {
        this.convertHtmlEntry = convertHtmlEntry;
    }

    public boolean isIgnoreInvalideHtmlTags()
    {
        return ignoreInvalideHtmlTags;
    }

    public void setIgnoreInvalideHtmlTags(boolean ignoreInvalideHtmlTags)
    {
        this.ignoreInvalideHtmlTags = ignoreInvalideHtmlTags;
    }

    public HtmlFilter(String filterName, String filterDescription,
            String defaultEmbeddableTags, String embeddableTags,
            String placeHolderTrim, long companyId, boolean convertHtmlEntry,
            boolean ignoreInvalideHtmlTags, String jsFunctionText,
            String defaultPairedTags, String pairedTags,
            String defaultUnpairedTags, String unpairedTags,
            String defaultSwitchTagMaps, String switchTagMaps,
            String defaultWhitePreservingTags, String whitePreservingTags,
            String defaultNonTranslatableMetaAttributes,
            String nonTranslatableMetaAttributes,
            String defaultTranslatableAttributes, String translatableAttributes)
    {
        super();
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.defaultEmbeddableTags = defaultEmbeddableTags;
        this.embeddableTags = embeddableTags;
        this.placeHolderTrim = placeHolderTrim;
        this.companyId = companyId;
        this.convertHtmlEntry = convertHtmlEntry;
        this.ignoreInvalideHtmlTags = ignoreInvalideHtmlTags;
        this.jsFunctionText = jsFunctionText;
        this.defaultPairedTags = defaultPairedTags;
        this.pairedTags = pairedTags;
        this.defaultUnpairedTags = defaultUnpairedTags;
        this.unpairedTags = unpairedTags;
        this.defaultSwitchTagMaps = defaultSwitchTagMaps;
        this.switchTagMaps = switchTagMaps;
        this.defaultWhitePreservingTags = defaultWhitePreservingTags;
        this.whitePreservingTags = whitePreservingTags;
        this.defaultNonTranslatableMetaAttributes = defaultNonTranslatableMetaAttributes;
        this.nonTranslatableMetaAttributes = nonTranslatableMetaAttributes;
        this.defaultTranslatableAttributes = defaultTranslatableAttributes;
        this.translatableAttributes = translatableAttributes;
    }

    public HtmlFilter(long id, String filterName, String filterDescription,
            String defaultEmbeddableTags, String embeddableTags,
            String placeHolderTrim, long companyId, boolean convertHtmlEntry,
            boolean ignoreInvalideHtmlTags, String jsFunctionText,
            String defaultPairedTags, String pairedTags,
            String defaultUnpairedTags, String unpairedTags,
            String defaultSwitchTagMaps, String switchTagMaps,
            String defaultWhitePreservingTags, String whitePreservingTags,
            String defaultNonTranslatableMetaAttributes,
            String nonTranslatableMetaAttributes,
            String defaultTranslatableAttributes,
            String translatableAttributes,
            String defaultLocalizableAttributeMaps,
            String localizableAttributeMaps)
    {
        super();
        this.id = id;
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.defaultEmbeddableTags = defaultEmbeddableTags;
        this.embeddableTags = embeddableTags;
        this.placeHolderTrim = placeHolderTrim;
        this.companyId = companyId;
        this.convertHtmlEntry = convertHtmlEntry;
        this.ignoreInvalideHtmlTags = ignoreInvalideHtmlTags;
        this.jsFunctionText = jsFunctionText;
        this.defaultPairedTags = defaultPairedTags;
        this.pairedTags = pairedTags;
        this.defaultUnpairedTags = defaultUnpairedTags;
        this.unpairedTags = unpairedTags;
        this.defaultSwitchTagMaps = defaultSwitchTagMaps;
        this.switchTagMaps = switchTagMaps;
        this.defaultWhitePreservingTags = defaultWhitePreservingTags;
        this.whitePreservingTags = whitePreservingTags;
        this.defaultNonTranslatableMetaAttributes = defaultNonTranslatableMetaAttributes;
        this.nonTranslatableMetaAttributes = nonTranslatableMetaAttributes;
        this.defaultTranslatableAttributes = defaultTranslatableAttributes;
        this.translatableAttributes = translatableAttributes;
        this.defaultLocalizableAttributeMaps = defaultLocalizableAttributeMaps;
        this.localizableAttributeMaps = localizableAttributeMaps;
    }

    public HtmlFilter()
    {
        super();
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription)
    {
        this.filterDescription = filterDescription;
    }

    public String getEmbeddableTags()
    {
        return embeddableTags;
    }

    public void setEmbeddableTags(String embeddableTags)
    {
        this.embeddableTags = embeddableTags;
    }

    public String getPlaceHolderTrim()
    {
        return placeHolderTrim;
    }

    public void setPlaceHolderTrim(String placeHolderTrim)
    {
        this.placeHolderTrim = placeHolderTrim;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getFilterTableName()
    {
        return FilterConstants.HTML_TABLENAME;
    }

    public boolean checkExists(String filterName, long companyId)
    {
        String hql = "from HtmlFilter hf where hf.filterName =:filterName and hf.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from HtmlFilter hf where hf.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        return filters;
    }

    public long getId()
    {
        return id;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":").append(
                "\"" + getFilterTableName() + "\"").append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"").append(
                FilterHelper.escape(filterName)).append("\"").append(",");
        sb.append("\"filterDescription\":").append("\"").append(
                FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"placeHolderTrim\":").append("\"").append(
                FilterHelper.escape(placeHolderTrim)).append("\"").append(",");
        sb.append("\"embeddableTags\":").append("\"").append(
                FilterHelper.escape(embeddableTags)).append("\"").append(",");
        sb.append("\"defaultEmbeddableTags\":").append("\"").append(
                FilterHelper.escape(defaultEmbeddableTags)).append("\"")
                .append(",");
        sb.append("\"jsFunctionText\":").append("\"").append(
                FilterHelper.escape(jsFunctionText)).append("\"").append(",");
        sb.append("\"defaultPairedTags\":").append("\"").append(
                FilterHelper.escape(defaultPairedTags)).append("\"")
                .append(",");
        sb.append("\"pairedTags\":").append("\"").append(
                FilterHelper.escape(pairedTags)).append("\"").append(",");
        sb.append("\"defaultUnpairedTags\":").append("\"").append(
                FilterHelper.escape(defaultUnpairedTags)).append("\"").append(
                ",");
        sb.append("\"unpairedTags\":").append("\"").append(
                FilterHelper.escape(unpairedTags)).append("\"").append(",");
        sb.append("\"defaultSwitchTagMaps\":").append("\"").append(
                FilterHelper.escape(defaultSwitchTagMaps)).append("\"").append(
                ",");
        sb.append("\"switchTagMaps\":").append("\"").append(
                FilterHelper.escape(switchTagMaps)).append("\"").append(",");
        sb.append("\"defaultInternalTag\":").append("\"").append(
                FilterHelper.escape(getDefaultInternalTagMaps())).append("\"")
                .append(",");
        sb.append("\"internalTag\":").append("\"").append(
                FilterHelper.escape(getInternalTagMaps())).append("\"").append(
                ",");
        sb.append("\"defaultWhitePreservingTags\":").append("\"").append(
                FilterHelper.escape(defaultWhitePreservingTags)).append("\"")
                .append(",");
        sb.append("\"whitePreservingTags\":").append("\"").append(
                FilterHelper.escape(whitePreservingTags)).append("\"").append(
                ",");
        sb.append("\"defaultNonTranslatableMetaAttributes\":").append("\"")
                .append(
                        FilterHelper
                                .escape(defaultNonTranslatableMetaAttributes))
                .append("\"").append(",");
        sb.append("\"nonTranslatableMetaAttributes\":").append("\"").append(
                FilterHelper.escape(nonTranslatableMetaAttributes))
                .append("\"").append(",");
        sb.append("\"defaultTranslatableAttributes\":").append("\"").append(
                FilterHelper.escape(defaultTranslatableAttributes))
                .append("\"").append(",");
        sb.append("\"translatableAttributes\":").append("\"").append(
                FilterHelper.escape(translatableAttributes)).append("\"")
                .append(",");
        sb.append("\"defaultLocalizableAttributeMaps\":").append("\"").append(
                FilterHelper.escape(defaultLocalizableAttributeMaps)).append(
                "\"").append(",");
        sb.append("\"localizableAttributeMaps\":").append("\"").append(
                FilterHelper.escape(localizableAttributeMaps)).append("\"")
                .append(",");
        sb.append("\"convertHtmlEntry\":").append(convertHtmlEntry).append(",");
        sb.append("\"ignoreInvalideHtmlTags\":").append(ignoreInvalideHtmlTags);
        sb.append("}");
        return sb.toString();
    }
    
    public List<HtmlInternalTag> stringToTags(String sTags)
    {
        List<HtmlInternalTag> internalTags = new ArrayList<HtmlInternalTag>();
        String[] tags = sTags.split(",");
        for (String tag : tags)
        {
            tag = tag.trim();
            if (tag.length() > 0)
            {
                try
                {
                    internalTags.add(HtmlInternalTag.string2tag(tag));
                }
                catch (InternalTagException e)
                {
                    s_logger.warn(e);
                }
            }
        }
        return internalTags;
    }

    public List<HtmlInternalTag> getInternalTags()
    {        
        return stringToTags(internalTagMaps);
    }

    public String getDefaultInternalTagMaps()
    {
        return defaultInternalTagMaps;
    }

    public void setDefaultInternalTagMaps(String defaultInternalTagMaps)
    {
        if (defaultInternalTagMaps.indexOf("<") > 0)
        {
            defaultInternalTagMaps = format(defaultInternalTagMaps);
        }
        this.defaultInternalTagMaps = defaultInternalTagMaps;
    }

    public String getInternalTagMaps()
    {
        return internalTagMaps;
    }
    
    public String format(String internTags)
    {
        List<HtmlInternalTag> tags = stringToTags(internTags);
        StringBuffer s = new StringBuffer();
        for (HtmlInternalTag tag : tags)
        {
            if (s.length() > 0)
                s.append(",");

            s.append(tag.toString());
        }

        return s.toString();
    }

    public void setInternalTagMaps(String internalTagMaps)
    {
        if (internalTagMaps.indexOf("<") > 0)
        {
            internalTagMaps = format(internalTagMaps);
        }
        
        this.internalTagMaps = internalTagMaps;
    }
    
    public void setTranslateAlt(boolean isTranslateAlt)
    {
        if (translatableAttributes == null)
        {
            translatableAttributes = "";
        }
        
        boolean hasContent = false;
        StringBuffer newRule = new StringBuffer();
        if (isTranslateAlt)
        {
            newRule.append("alt");
            hasContent = true;
        }
        
        for (String s : translatableAttributes.split(","))
        {
            if (!"alt".equalsIgnoreCase(s))
            {
            	if (hasContent)
                {
                    newRule.append(",");
                }
                else
                {
                    hasContent = true;
                }
            	
                newRule.append(s);
            }
        }
        
        translatableAttributes = newRule.toString();
    }
}
