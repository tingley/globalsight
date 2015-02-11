package com.globalsight.action;

import com.globalsight.bo.GetVersionBO;
import com.globalsight.bo.LoginBO;
import com.globalsight.bo.ConfigureBO;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.ConfigureHelper;

public class GetVersionAction extends Action
{

	public String execute(String[] args) throws Exception
	{
        GetVersionBO getVersionBO = new GetVersionBO();
		return getVersionBO.query(accessToken);
	}
}
