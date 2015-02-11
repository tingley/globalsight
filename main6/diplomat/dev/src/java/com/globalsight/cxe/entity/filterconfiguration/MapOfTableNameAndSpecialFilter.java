package com.globalsight.cxe.entity.filterconfiguration;

import java.util.HashMap;
import java.util.Map;

public class MapOfTableNameAndSpecialFilter
{
    private final static Map<String, Filter> TABLENAME_FILTER = new HashMap<String, Filter>();
    static
    {
        TABLENAME_FILTER.put(FilterConstants.JAVAPROPERTIES_TABLENAME,
                new JavaPropertiesFilter());
        TABLENAME_FILTER.put(FilterConstants.JAVASCRIPT_TABLENAME,
                new JavaScriptFilter());
        TABLENAME_FILTER.put(FilterConstants.MSOFFICEDOC_TABLENAME,
                new MSOfficeDocFilter());
        TABLENAME_FILTER.put(FilterConstants.XMLRULE_TABLENAME,
                new XMLRuleFilter());
        TABLENAME_FILTER.put(FilterConstants.HTML_TABLENAME, new HtmlFilter());
        TABLENAME_FILTER.put(FilterConstants.JSP_TABLENAME, new JSPFilter());
        TABLENAME_FILTER.put(FilterConstants.MSOFFICEEXCEL_TABLENAME, 
                             new MSOfficeExcelFilter());
        TABLENAME_FILTER.put(FilterConstants.INDD_TABLENAME, 
                new InddFilter());
        TABLENAME_FILTER.put(FilterConstants.OPENOFFICE_TABLENAME, 
                new OpenOfficeFilter());
        TABLENAME_FILTER.put(FilterConstants.MSOFFICEPPT_TABLENAME, 
                new MSOfficePPTFilter());
        TABLENAME_FILTER.put(FilterConstants.OFFICE2010_TABLENAME, 
                new MSOffice2010Filter());
        TABLENAME_FILTER.put(FilterConstants.PO_TABLENAME, 
                new POFilter());
    }

    public static Filter getFilterInstance(String tableName)
    {
        return TABLENAME_FILTER.get(tableName);
    }
}
