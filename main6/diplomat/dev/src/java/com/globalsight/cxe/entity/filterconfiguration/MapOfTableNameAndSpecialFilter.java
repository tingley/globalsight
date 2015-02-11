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

import java.util.Collection;
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
        TABLENAME_FILTER.put(FilterConstants.INDD_TABLENAME, new InddFilter());
        TABLENAME_FILTER.put(FilterConstants.OPENOFFICE_TABLENAME,
                new OpenOfficeFilter());
        TABLENAME_FILTER.put(FilterConstants.MSOFFICEPPT_TABLENAME,
                new MSOfficePPTFilter());
        TABLENAME_FILTER.put(FilterConstants.OFFICE2010_TABLENAME,
                new MSOffice2010Filter());
        TABLENAME_FILTER.put(FilterConstants.PO_TABLENAME, new POFilter());
        TABLENAME_FILTER.put(FilterConstants.BASE_TABLENAME, new BaseFilter());
        TABLENAME_FILTER.put(FilterConstants.FM_TABLENAME, new FMFilter());
        TABLENAME_FILTER.put(FilterConstants.PLAINTEXT_TABLENAME,
                new PlainTextFilter());
        TABLENAME_FILTER.put(FilterConstants.QA_TABLENAME, new QAFilter());
    }

    public static Filter getFilterInstance(String tableName)
    {
        return TABLENAME_FILTER.get(tableName);
    }

    public static Collection<String> getAllFilter()
    {
        Collection<String> c = TABLENAME_FILTER.keySet();
        return c;
    }
}
