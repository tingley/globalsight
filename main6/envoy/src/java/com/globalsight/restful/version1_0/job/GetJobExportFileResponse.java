package com.globalsight.restful.version1_0.job;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "JobFiles")
public class GetJobExportFileResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    String requestedJobIds;
    String message;

    String path;
    List<ExportingJob> exportingJobs;

    @XmlElement(name = "requestedJobIds")
    public String getRequestedJobIds()
    {
        return requestedJobIds;
    }

    public void setRequestedJobIds(String requestedJobIds)
    {
        this.requestedJobIds = requestedJobIds;
    }

    @XmlElement(name = "message")
    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @XmlElement(name = "path")
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @XmlElement(name = "exportingJobs")
    public List<ExportingJob> getExportingJobs()
    {
        return exportingJobs;
    }

    public void setExportingJobs(List<ExportingJob> exportingJobs)
    {
        this.exportingJobs = exportingJobs;
    }

    public void addExportingJobs(long jobId, String language)
    {
        ExportingJob exportingJob = new ExportingJob();
        exportingJob.setJobId(jobId);
        exportingJob.setLanguage(language);
        exportingJobs.add(exportingJob);
    }

    private class ExportingJob implements Serializable
    {
        private static final long serialVersionUID = 1L;
        long jobId;
        String language;

        @XmlElement(name = "jobId")
        public long getJobId()
        {
            return jobId;
        }

        public void setJobId(long jobId)
        {
            this.jobId = jobId;
        }

        @XmlElement(name = "language")
        public String getLanguage()
        {
            return language;
        }

        public void setLanguage(String language)
        {
            this.language = language;
        }

    }

}
