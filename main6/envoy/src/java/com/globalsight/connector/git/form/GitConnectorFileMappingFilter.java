package com.globalsight.connector.git.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;

public class GitConnectorFileMappingFilter
{
    private String sourceLocaleFilter;
    private String sourceMappingPathFilter;
    private String targetLocaleFilter;
    private String targetMappingPathFilter;
    private String companyNameFilter;

    public List<GitConnectorFileMapping> filter(List<GitConnectorFileMapping> fileMappings)
    {
        List<GitConnectorFileMapping> result = new ArrayList<GitConnectorFileMapping>();
        
        for (GitConnectorFileMapping fileMapping : fileMappings)
        {
            if (!like(sourceLocaleFilter, fileMapping.getSourceLocale()))
            {
                continue;
            }
            
            if (!like(sourceMappingPathFilter, fileMapping.getSourceMappingPath()))
            {
                continue;
            }
            
            if (!like(targetLocaleFilter, fileMapping.getTargetLocale()))
            {
                continue;
            }
            
            if (!like(targetMappingPathFilter, fileMapping.getTargetMappingPath()))
            {
                continue;
            }
            
            if (!like(companyNameFilter, fileMapping.getCompanyName()))
            {
                continue;
            }
            
            result.add(fileMapping);
        }
        
        return result;
    }

    private boolean like(String filterValue, String candidateValue)
    {
        if (filterValue == null)
            return true;

        filterValue = filterValue.trim();
        if (filterValue.length() == 0)
            return true;

        if (candidateValue == null)
            return false;

        filterValue = filterValue.toLowerCase();
        candidateValue = candidateValue.toLowerCase();

        return candidateValue.indexOf(filterValue) > -1;
    }

	public void setSourceLocaleFilter(String sourceLocaleFilter) {
		this.sourceLocaleFilter = sourceLocaleFilter;
	}

	public String getSourceLocaleFilter() {
		return sourceLocaleFilter;
	}

	public void setSourceMappingPathFilter(String sourceMappingPathFilter) {
		this.sourceMappingPathFilter = sourceMappingPathFilter;
	}

	public String getSourceMappingPathFilter() {
		return sourceMappingPathFilter;
	}

	public void setTargetLocaleFilter(String targetLocaleFilter) {
		this.targetLocaleFilter = targetLocaleFilter;
	}

	public String getTargetLocaleFilter() {
		return targetLocaleFilter;
	}

	public void setTargetMappingPathFilter(String targetMappingPathFilter) {
		this.targetMappingPathFilter = targetMappingPathFilter;
	}

	public String getTargetMappingPathFilter() {
		return targetMappingPathFilter;
	}
	
	public String getCompanyNameFilter()
    {
        return companyNameFilter;
    }

    public void setCompanyNameFilter(String companyNameFilter)
    {
        this.companyNameFilter = companyNameFilter;
    }
}
