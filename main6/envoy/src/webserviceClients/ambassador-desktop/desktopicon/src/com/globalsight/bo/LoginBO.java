package com.globalsight.bo;

import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class LoginBO
{

	public String login(String userName, String password) throws Exception
	{
		Ambassador abmassador = WebClientHelper.getAmbassador();
		String accessToken = abmassador.login(userName, password);
		return accessToken;
	}
}
