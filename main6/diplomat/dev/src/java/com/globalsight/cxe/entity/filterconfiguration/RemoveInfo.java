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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.util.SortUtil;

public class RemoveInfo implements JsonSerializable
{
    private boolean isExistInFileProfile; // Referenced By File Profile
    private List<FilterInfos> filterInfos; // Referenced By File Profile
    private String isDeleted = "true";
    private boolean isUsedByFilters; // Referenced By Other Filters
    private List<FilterInfos> usedFilters; // Referenced By Other Filters
    private Map<SpecialFilterToDelete, Set<JobImpl>> filterInJobs;

    public Map<SpecialFilterToDelete, Set<JobImpl>> getFilterInJobs()
    {
        return filterInJobs;
    }

    public void setFilterInJobs(
            Map<SpecialFilterToDelete, Set<JobImpl>> filterInJobs)
    {
        this.filterInJobs = filterInJobs;
    }

    public void addFilterInJobs(
            Map<SpecialFilterToDelete, Set<JobImpl>> filterInJobs)
    {
        if (this.filterInJobs == null)
        {
            this.filterInJobs = filterInJobs;
            return;
        }

        for (SpecialFilterToDelete f : filterInJobs.keySet())
        {
            Set<JobImpl> fps = this.filterInJobs.get(f);
            if (fps == null)
            {
                fps = new HashSet<JobImpl>();
            }

            fps.addAll(filterInJobs.get(f));
            this.filterInJobs.put(f, fps);
        }
    }

    public String isDeleted()
    {
        return isDeleted;
    }

    public void setDeleted(String isDeleted)
    {
        this.isDeleted = isDeleted;
    }

    public RemoveInfo()
    {
    }

    public RemoveInfo(boolean isExistInFileProfile,
            List<FilterInfos> filterInfos)
    {
        this.isExistInFileProfile = isExistInFileProfile;
        this.filterInfos = filterInfos;
    }

    public List<FilterInfos> getFilterInfos()
    {
        return filterInfos;
    }

    public void setFilterInfos(List<FilterInfos> filterInfos)
    {
        this.filterInfos = filterInfos;
    }

    public boolean isExistInFileProfile()
    {
        return isExistInFileProfile;
    }

    public void setExistInFileProfile(boolean isExistInFileProfile)
    {
        this.isExistInFileProfile = isExistInFileProfile;
    }

    public boolean isUsedByFilters()
    {
        return isUsedByFilters;
    }

    public void setUsedByFilters(boolean isUsedByFilters)
    {
        this.isUsedByFilters = isUsedByFilters;
    }

    public List<FilterInfos> getUsedFilters()
    {
        return usedFilters;
    }

    public void setUsedFilters(List<FilterInfos> usedFilters)
    {
        this.usedFilters = usedFilters;
    }

    public void addUsedFilters(FilterInfos p_usedFilter)
    {
        if (usedFilters == null)
            usedFilters = new ArrayList<FilterInfos>();

        usedFilters.add(p_usedFilter);
    }

    public void addUsedFilters(List<FilterInfos> p_usedFilters)
    {
        if (usedFilters == null)
            usedFilters = new ArrayList<FilterInfos>();

        usedFilters.addAll(p_usedFilters);
    }

    public String toJSON()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"isExistInFileProfile\":").append(isExistInFileProfile)
                .append(",");
        sb.append("\"isDeleted\":\"").append(isDeleted).append("\",");
        sb.append("\"filterInfos\":");
        sb.append("[");
        for (int i = 0; i < filterInfos.size(); i++)
        {
            FilterInfos filterInfo = filterInfos.get(i);
            sb.append("{");
            sb.append("\"filterId\":").append(filterInfo.filterId).append(",");
            sb.append("\"fileProfileName\":\"")
                    .append(filterInfo.fileProfileName).append("\"")
                    .append(",");
            sb.append("\"filterTableName\":\"")
                    .append(filterInfo.filterTableName).append("\"");
            sb.append("}");
            if (i != filterInfos.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");

        sb.append(",");
        sb.append("\"isUsedByFilters\":").append(isUsedByFilters).append(",");
        sb.append("\"usedFilters\":");
        sb.append("[");
        for (int i = 0; (usedFilters != null) && i < usedFilters.size(); i++)
        {
            FilterInfos filterInfo = usedFilters.get(i);
            sb.append("{");
            sb.append("\"filterId\":").append(filterInfo.filterId).append(",");
            sb.append("\"filterTableName\":\"")
                    .append(filterInfo.filterTableName).append("\"")
                    .append(",");
            sb.append("\"usedFilterID\":\"").append(filterInfo.usedFilterID)
                    .append("\"").append(",");
            sb.append("\"usedFilterTableName\":\"")
                    .append(filterInfo.usedFilterTableName).append("\"");
            sb.append("}");
            if (i != usedFilters.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");

        List<SpecialFilterToDelete> filters = new ArrayList<SpecialFilterToDelete>();
        filters.addAll(filterInJobs.keySet());
        SortUtil.sort(filters);

        sb.append(",");
        sb.append("\"isUsedInJob\":").append(isUsedInJob()).append(",");
        sb.append("\"usedJobs\":");
        sb.append("[");

        for (SpecialFilterToDelete f : filters)
        {
            Set<JobImpl> jobs = filterInJobs.get(f);

            for (JobImpl j : jobs)
            {
                sb.append("{");
                sb.append("\"filterId\":").append(f.getSpecialFilterId())
                        .append(",");
                sb.append("\"filterTableName\":\"")
                        .append(f.getFilterTableName()).append("\"")
                        .append(",");
                sb.append("\"jobName\":\"").append(j.getName()).append("\"");
                sb.append("}");

                sb.append(",");
            }
        }

        if (sb.charAt(sb.length() - 1) == ',')
        {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("]");

        sb.append("}]");
        return sb.toString();
    }

    class FilterInfos
    {
        private long filterId;
        private String filterTableName;
        private String fileProfileName;
        private String usedFilterID; // Referenced By Other Filters
        private String usedFilterTableName; // Referenced By Other Filters

        public FilterInfos(long filterId, String filterTableName,
                String fileProfileName)
        {
            this.filterId = filterId;
            this.filterTableName = filterTableName;
            this.fileProfileName = fileProfileName;
        }

        public FilterInfos(long filterId, String filterTableName,
                String usedFilterID, String usedFilterTableName)
        {
            this.filterId = filterId;
            this.filterTableName = filterTableName;
            this.usedFilterID = usedFilterID;
            this.usedFilterTableName = usedFilterTableName;
        }

    }

    public boolean isUsedInJob()
    {
        return filterInJobs != null && filterInJobs.keySet().size() > 0;
    }
}
