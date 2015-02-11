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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class InddFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private long companyId;
    private boolean translateHiddenLayer = false;
    private boolean translateMasterLayer = true;
    private boolean translateFileInfo = false;
    private boolean translateHyperlinks = false;
    private boolean translateHiddenCondText = true;
    private boolean skipTrackingKerning = false;
    private boolean extractLineBreak = true;
    private boolean replaceNonbreakingSpace = false;

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from InddFilter infl where infl.filterName =:filterName and infl.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from InddFilter infl where infl.id<>:filterId and infl.filterName =:filterName and infl.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from InddFilter infl where infl.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.INDD_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"translateHiddenLayer\":").append(translateHiddenLayer)
                .append(",");
        sb.append("\"translateMasterLayer\":").append(translateMasterLayer)
                .append(",");
        sb.append("\"translateFileInfo\":").append(translateFileInfo)
                .append(",");
        sb.append("\"translateHyperlinks\":").append(translateHyperlinks)
                .append(",");
        sb.append("\"translateHiddenCondText\":")
                .append(translateHiddenCondText).append(",");
        sb.append("\"skipTrackingKerning\":").append(skipTrackingKerning)
                .append(",");
        sb.append("\"extractLineBreak\":").append(extractLineBreak).append(",");
        sb.append("\"replaceNonbreakingSpace\":").append(
                replaceNonbreakingSpace);
        sb.append("}");
        return sb.toString();
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
        return FilterConstants.INDD_TABLENAME;
    }

    public boolean getTranslateHiddenLayer()
    {
        return translateHiddenLayer;
    }

    public void setTranslateHiddenLayer(boolean translateHiddenLayer)
    {
        this.translateHiddenLayer = translateHiddenLayer;
    }

    public boolean getTranslateMasterLayer()
    {
        return translateMasterLayer;
    }

    public void setTranslateMasterLayer(boolean translateMasterLayer)
    {
        this.translateMasterLayer = translateMasterLayer;
    }

    public boolean getTranslateFileInfo()
    {
        return translateFileInfo;
    }

    public void setTranslateFileInfo(boolean translateFileInfo)
    {
        this.translateFileInfo = translateFileInfo;
    }

    public boolean getTranslateHyperlinks()
    {
        return translateHyperlinks;
    }

    public void setTranslateHyperlinks(boolean translateHyperlinks)
    {
        this.translateHyperlinks = translateHyperlinks;
    }

    public boolean getExtractLineBreak()
    {
        return extractLineBreak;
    }

    public void setExtractLineBreak(boolean extractLineBreak)
    {
        this.extractLineBreak = extractLineBreak;
    }

    public boolean isReplaceNonbreakingSpace()
    {
        return replaceNonbreakingSpace;
    }

    public void setReplaceNonbreakingSpace(boolean replaceNonbreakingSpace)
    {
        this.replaceNonbreakingSpace = replaceNonbreakingSpace;
    }

    public boolean getTranslateHiddenCondText()
    {
        return translateHiddenCondText;
    }

    public void setTranslateHiddenCondText(boolean translateHiddenCondText)
    {
        this.translateHiddenCondText = translateHiddenCondText;
    }

    public boolean getSkipTrackingKerning()
    {
        return skipTrackingKerning;
    }

    public void setSkipTrackingKerning(boolean skipTrackingKerning)
    {
        this.skipTrackingKerning = skipTrackingKerning;
    }
}
