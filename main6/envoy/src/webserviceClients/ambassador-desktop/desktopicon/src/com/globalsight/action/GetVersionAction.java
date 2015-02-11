package com.globalsight.action;

import com.globalsight.bo.GetVersionBO;

public class GetVersionAction extends Action
{

	public String execute(String[] args) throws Exception
	{
        GetVersionBO getVersionBO = new GetVersionBO();
		return getVersionBO.query(accessToken);
	}
}
