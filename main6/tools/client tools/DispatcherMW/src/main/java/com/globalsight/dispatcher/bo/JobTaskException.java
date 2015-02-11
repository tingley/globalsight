package com.globalsight.dispatcher.bo;

import com.globalsight.dispatcher.dao.DispatcherDAOFactory;

public class JobTaskException extends Exception
{
    private static final long serialVersionUID = 1L;
    JobBO job;
    String errorMsg;
    
    public JobTaskException(String p_message, JobBO p_job)
    {
        super(p_message);
        errorMsg = p_message;
        job = p_job;
    }

    public String getInfo()
    {
        StringBuilder msg = new StringBuilder();
        MTPLanguage lang = DispatcherDAOFactory.getMTPLanguagesDAO().getMTPLanguage(job.getMtpLanguageID());
        msg.append("Do Machine Translation Failed. The Job Info are:\n")
           .append("JobID:").append(job.getJobID()).append("\n")
           .append("SourceLanguage:").append(job.getSourceLanguage()).append("\n")
           .append("TargetLanguage:").append(job.getTargetLanguage()).append("\n")
           .append("SrcFile:").append(job.getSrcFile()).append("\n")
           .append("Language Name:").append(lang.getName()).append("\n")
           .append("Error Message:").append(errorMsg);
        return msg.toString();
    }

}
