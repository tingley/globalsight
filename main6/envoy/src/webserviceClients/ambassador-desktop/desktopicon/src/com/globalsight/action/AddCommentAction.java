package com.globalsight.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;

import com.globalsight.bo.AddCommentBO;
import com.globalsight.bo.QueryBO;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.StringHelper;

public class AddCommentAction extends Action
{

	static Logger log = Logger.getLogger(AddCommentAction.class.getName());

	String jobName;
	String textComment;
	String fileAttach;
	byte[] fileBytes = null;
	String fileName = null;
	Exception m_e = null;
    int maxWaitSeconds = 1800;

	public boolean executeWithThread(String args[]) throws Exception
	{
		jobName = args[0];
		textComment = args[1];
		fileAttach = args[2];

		File file = new File(fileAttach);
		long maximalFileSize = Long.parseLong(ConfigureHelper
				.getAttachedFileSize()) * 1024 * 1024;
		if (file.exists() && file.isFile())
		{
			if (file.length() > maximalFileSize)
			{
				AmbOptionPane.showMessageDialog("Can not upload attachment larger than "
								+ ConfigureHelper.getAttachedFileSize()
								+ "M!", "Warning");
			}
            else
			{
				fileName = file.getName();
				int len = (int) file.length();
				fileBytes = new byte[len];
				BufferedInputStream inputStream = new BufferedInputStream(
						new FileInputStream(file));
				inputStream.read(fileBytes, 0, len);
			}
		}
        log.info("Start to add comments for job " + jobName + ".");
        try
        {
            if (!textComment.trim().equals("") || !fileAttach.equals(""))
            {
                AddCommentBO commentBO = new AddCommentBO();
                String result = commentBO.addJobComment(accessToken, jobName,
                        textComment, fileBytes, fileName);
                return (result.indexOf("successful") != -1);
            }
        }
        catch (Exception ex)
        {
            m_e = ex;
        }
        return false;
    }

	public String execute(String[] args) throws Exception
	{
		return null;
	}

	public Exception getException()
	{
		return m_e;
	}
}
