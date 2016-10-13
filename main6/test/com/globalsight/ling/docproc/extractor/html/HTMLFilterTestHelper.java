package com.globalsight.ling.docproc.extractor.html;

import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class HTMLFilterTestHelper
{
    public static final String DEFAULT_EMBEDDABLE_TAGS = "a,abbr,acronym,b,basefont,bdo,big,blink,cite,code,del,dfn,em,font,i,img,ins,kbd,nobr,q,s,samp,small,span,strike,strong,sub,sup,tt,u,var,wbr";
    public static final String DEFAULT_PAIRED_TAGS = "a,abbr,acronym,b,bdo,big,blink,button,cite,code,del,dfn,em,font,i,ins,kbd,label,nobr,plaintext,q,ruby,s,samp,select,small,span,strike,strong,sub,sup,textarea,tt,u,var,xmp";
    public static final String DEFAULT_SWITCH_TAG_MAPS = "script:javascript,style:css-styles,xml:xml";
    public static final String DEFAULT_TRANSLATABLE_ATTRIBUTES = "abbr,accesskey,alt,char,label,prompt,standby,summary,title";
    public static final String DEFAULT_UNPAIRED_TAGS = "br,hr,img,input,rt,wbr";
    public static final String DEFAULT_WHITE_PRESERVING_TAGS = "listing,pre";
    public static final String DEFAULT_NONTRANSLATABLE_META_ATTRIBUTES = "expires,generator,originator,progid,robots,template";
    public static final String PLACEHOLDER_TRIM = "embeddable_tags";
    public static final String JSFUNCTION_TEXT = "l10n";
        
    /**
     * Create a new HTML Filter
     * 
     * @param p_companyID
     *            company id
     * @param p_des
     *            Short description for HTML Filter name and description
     * @param p_isIgnoreInvalideHtmlTags
     *            HTML Filter option
     * @return
     */
    public static HtmlFilter addHtmlFilter(long p_companyID, String p_des, 
            boolean p_isIgnoreInvalideHtmlTags)
    {
        HtmlFilter hFilter = new HtmlFilter();
        hFilter.setFilterName("JUnit Test " + p_des);
        hFilter.setFilterDescription("Added For JUnit Test " + p_des);
        hFilter.setCompanyId(p_companyID);
        hFilter.setIgnoreInvalideHtmlTags(p_isIgnoreInvalideHtmlTags);
        hFilter.setConvertHtmlEntry(true);
        hFilter.setEmbeddableTags(DEFAULT_EMBEDDABLE_TAGS);
        hFilter.setPairedTags(DEFAULT_PAIRED_TAGS);
        hFilter.setSwitchTagMaps(DEFAULT_SWITCH_TAG_MAPS);
        hFilter.setTranslatableAttributes(DEFAULT_TRANSLATABLE_ATTRIBUTES);
        hFilter.setUnpairedTags(DEFAULT_UNPAIRED_TAGS);
        hFilter.setWhitePreservingTags(DEFAULT_WHITE_PRESERVING_TAGS);
        hFilter.setNonTranslatableMetaAttributes(DEFAULT_NONTRANSLATABLE_META_ATTRIBUTES);
        hFilter.setDefaultEmbeddableTags(DEFAULT_EMBEDDABLE_TAGS);
        hFilter.setDefaultPairedTags(DEFAULT_PAIRED_TAGS);
        hFilter.setDefaultSwitchTagMaps(DEFAULT_SWITCH_TAG_MAPS);
        hFilter.setDefaultTranslatableAttributes(DEFAULT_TRANSLATABLE_ATTRIBUTES);
        hFilter.setDefaultUnpairedTags(DEFAULT_UNPAIRED_TAGS);
        hFilter.setDefaultWhitePreservingTags(DEFAULT_WHITE_PRESERVING_TAGS);
        hFilter.setDefaultNonTranslatableMetaAttributes(DEFAULT_NONTRANSLATABLE_META_ATTRIBUTES);
        hFilter.setPlaceHolderTrim(PLACEHOLDER_TRIM);
        hFilter.setJsFunctionText(JSFUNCTION_TEXT);
        
        try
        {
            HibernateUtil.save(hFilter);
            return hFilter;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Delete the HtmlFilter By ID.
     */
    public static void delHtmlFilterByID(HtmlFilter p_hFilter)
    {
        try
        {
            if (p_hFilter != null)
            {
                HibernateUtil.delete(p_hFilter);
                System.out.println("DELETE HtmlFilter with ID is " + p_hFilter.getId());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
