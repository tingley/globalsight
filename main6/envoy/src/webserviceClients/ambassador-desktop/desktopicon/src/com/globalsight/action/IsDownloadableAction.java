package com.globalsight.action;

import com.globalsight.bo.IsDownloadableBO;

public class IsDownloadableAction extends Action
{

    /**
     * @param jobs the array of jobs name
     * @return xml String
     * <jobs>
     * <job>
     * <name></name>
     * <status>downloadable | create_error | unknown</status>
     * </job>
     * </jobs>
     */
    public String execute(String[] jobs) throws Exception
    {
        IsDownloadableBO isDownloadableBO = new IsDownloadableBO();
        String msg = createMsg(jobs);
        if (msg.equals(""))
        {
            return null;
        }
        else
        {
            return isDownloadableBO.query(accessToken, msg);
        }
    }

    /**
     * Wrapper the msg
     * @param jobs
     * @return xml String
     * <jobs>
     * <job>
     * <name></name>
     * <status>downloadable | create_error | unknown</status>
     * </job>
     * </jobs>
     */
    private String createMsg(String[] jobs)
    {
        StringBuffer msg = new StringBuffer("");
        if (jobs != null && jobs.length != 0)
        {
            msg.append("<jobs>");
            for (int i = 0; i < jobs.length; i++)
            {
                msg.append("<job>");
                msg.append("<name>").append(jobs[i]).append("</name>");
                msg.append("<status>").append("unknown").append("</status>");
                msg.append("</job>");
            }
            msg.append("</jobs>");
        }
        return msg.toString();
    }
}
