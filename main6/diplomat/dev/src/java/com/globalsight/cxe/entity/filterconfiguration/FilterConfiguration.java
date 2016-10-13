package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;

public class FilterConfiguration
{
    private int id;
    private String filterName;
    private String knownFormatId;
    private String filterTableName;
    private String filterDescription;
    private long companyId;

    // private ArrayList<Filter> specialFilters;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return filterName;
    }

    public void setName(String name)
    {
        this.filterName = name;
    }

    public String getKnownFormatId()
    {
        return knownFormatId;
    }

    public void setKnownFormatId(String knownFormatId)
    {
        this.knownFormatId = knownFormatId;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription)
    {
        this.filterDescription = filterDescription;
    }

    public ArrayList<Filter> getSpecialFilters()
    {
        return MapOfTableNameAndSpecialFilter
                .getFilterInstance(filterTableName).getFilters(companyId);
    }

    public String toJSON()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"").append(
                FilterHelper.escape(filterName)).append("\"").append(",");
        sb.append("\"knownFormatId\":").append("\"").append(
                FilterHelper.escape(knownFormatId)).append("\"").append(",");
        sb.append("\"filterTableName\":").append("\"").append(
                FilterHelper.escape(filterTableName)).append("\"").append(",");
        sb.append("\"filterDescription\":").append("\"").append(
                FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"specialFilters\":");
        sb.append("[");
        ArrayList<Filter> specialFilters = getSpecialFilters();
        for (int i = 0; i < specialFilters.size(); i++)
        {
            sb.append(specialFilters.get(i).toJSON(companyId));
            if (i != specialFilters.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args)
    {
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        filterConfiguration.setId(1);
        filterConfiguration.setName("Java Properties Filter");
        filterConfiguration
                .setFilterDescription("The fil't\\er\" for java properties file.");
        filterConfiguration.setFilterTableName("java_properties_filter");
        // filterConfiguration.setKnownFormatId(4);
        // filterConfiguration.setSpecialFilters();
        // System.out.println(filterConfiguration.getSpecialFilters());
        System.out.println(filterConfiguration.toJSON());
        // JSONObject o = JSONObject.fromObject(filterConfiguration);
        // System.out.println(o);

    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }
}
