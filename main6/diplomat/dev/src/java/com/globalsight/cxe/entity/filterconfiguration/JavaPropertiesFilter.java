package com.globalsight.cxe.entity.filterconfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class JavaPropertiesFilter implements Filter
{
    static private final Logger s_logger = Logger
            .getLogger(JavaPropertiesFilter.class);

    @SuppressWarnings("unchecked")
    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from JavaPropertiesFilter jp where jp.companyId="
                + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public static void main(String[] args) throws IllegalArgumentException,
            SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException
    {
        JavaPropertiesFilter f = new JavaPropertiesFilter();
        System.out.println(f.toJSON(1000));
    }

    private long id;
    private String filterName;
    private String filterDescription;
    private boolean enableSidSupport = false;
    private boolean enableUnicodeEscape = false;
    private boolean enablePreserveSpaces = false;
    private long secondFilterId = -2;
    private String secondFilterTableName = null;
    private long companyId;
    private String internalText = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><propertiesInternalText><items><content>\\\\{[^{]*?\\\\}</content><isRegex>true</isRegex></items></propertiesInternalText>";

    public long getId()
    {
        return id;
    }

    public String getFilterTableName()
    {
        return FilterConstants.JAVAPROPERTIES_TABLENAME;
    }

    public String toJSON(long companyId)
    {
        long baseFilterId = BaseFilterManager.getBaseFilterIdByMapping(id,
                getFilterTableName());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.JAVAPROPERTIES_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"enableSidSupport\":").append(enableSidSupport).append(",");
        sb.append("\"enableUnicodeEscape\":").append(enableUnicodeEscape)
                .append(",");
        sb.append("\"enablePreserveSpaces\":").append(enablePreserveSpaces)
                .append(",");
        sb.append("\"internalTexts\":").append(getInternalTextJson())
                .append(",");
        sb.append("\"secondFilterId\":").append(secondFilterId).append(",");
        sb.append("\"secondFilterTableName\":").append("\"")
                .append(FilterHelper.escape(secondFilterTableName))
                .append("\",");
        sb.append("\"baseFilterId\":").append("\"").append(baseFilterId)
                .append("\"");
        sb.append("}");
        return sb.toString();
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

    public boolean getEnableSidSupport()
    {
        return enableSidSupport;
    }

    public void setEnableSidSupport(boolean enableSidSupport)
    {
        this.enableSidSupport = enableSidSupport;
    }

    public boolean getEnableUnicodeEscape()
    {
        return enableUnicodeEscape;
    }

    public void setEnableUnicodeEscape(boolean enableUnicodeEscape)
    {
        this.enableUnicodeEscape = enableUnicodeEscape;
    }

    public void setEnablePreserveSpaces(boolean enablePreserveSpaces)
    {
        this.enablePreserveSpaces = enablePreserveSpaces;
    }

    public boolean getEnablePreserveSpaces()
    {
        return this.enablePreserveSpaces;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public void setSecondFilterId(long secondFilterId)
    {
        this.secondFilterId = secondFilterId;
    }

    public long getSecondFilterId()
    {
        return this.secondFilterId;
    }

    public void setSecondFilterTableName(String secondFilterTableName)
    {
        this.secondFilterTableName = secondFilterTableName;
    }

    public String getSecondFilterTableName()
    {
        return this.secondFilterTableName;
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from JavaPropertiesFilter jp where jp.filterName =:filterName and jp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName, long companyId)
    {
        String hql = "from JavaPropertiesFilter jp where jp.id<>:filterId and jp.filterName =:filterName and jp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }
    
    public String getInternalText()
    {
        return internalText;
    }

    public void setInternalText(String internalText)
    {
        this.internalText = internalText;
    }

    public String getInternalTextJson()
    {
        try
        {
            JSONObject ob = XML.toJSONObject(internalText);
            ob = ob.getJSONObject("propertiesInternalText");
            if (!ob.isNull("items"))
            {
                return ob.get("items").toString();
            }
            return ob.toString();
        }
        catch (JSONException e)
        {
            s_logger.error(e.getMessage(), e);
        }

        return "{}";
    }

    public void setInternalTextJson(JSONArray internalTexts)
    {
        PropertiesInternalText texts = new PropertiesInternalText();
        if (internalTexts != null)
        {
            for (int i = 0; i < internalTexts.length(); i++)
            {
                try
                {
                    JSONObject ob = internalTexts.getJSONObject(i);
                    texts.add(ob);
                }
                catch (JSONException e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }

        this.internalText = XmlUtil.object2String(texts);
    }

    public PropertiesInternalText getInternalRegexs()
    {
        if (internalText != null && internalText.length() > 0)
        {
            return PropertiesInternalText.load(internalText);
        }

        return null;
    }
}
