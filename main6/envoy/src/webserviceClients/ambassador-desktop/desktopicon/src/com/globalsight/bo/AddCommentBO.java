package com.globalsight.bo;

import com.globalsight.util.WebClientHelper;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;
import com.globalsight.www.webservices.Ambassador;

public class AddCommentBO
{

	public String addComment(String accessToken, String jobID, String comment,
			byte[] filebytes, String jobName) throws Exception
	{
		String result = null;
		Ambassador abmassador = WebClientHelper.getAmbassador();
		long jobId = Long.parseLong(jobID);
		String username = CacheUtil.getInstance().getCurrentUser().getName();

		/*
         * p_objectType : 1 for job, 3 for task p_accessList: access
         * specification of attachment file Restricted = Only the Project
         * Manager can view this file. General = All Participants of the Task
         * can view this file.
         */

		result = abmassador.addComment(accessToken, jobId, 1, username,
				comment, filebytes, jobName, null);

		return (result != null) ? result : "";

	}

    public String addJobComment(String accessToken, String jobName,
            String comment, byte[] filebytes, String fileName) throws Exception
    {
        String result = null;
        Ambassador abmassador = WebClientHelper.getAmbassador();
        // long jobId = Long.parseLong(jobID);
        String username = CacheUtil.getInstance().getCurrentUser().getName();

        /*
         * p_objectType : 1 for job, 3 for task p_accessList: access
         * specification of attachment file Restricted = Only the Project
         * Manager can view this file. General = All Participants of the Task
         * can view this file.
         */
        result = abmassador.addJobComment(accessToken, jobName, username,
                comment, filebytes, fileName, null);

        return (result != null) ? result : "";

    }
}
