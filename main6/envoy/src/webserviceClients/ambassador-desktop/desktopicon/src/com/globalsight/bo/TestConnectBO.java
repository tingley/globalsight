package com.globalsight.bo;

import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class TestConnectBO
{

	public String test(String hostName, String port, boolean useSSL) throws Exception
	{
		String testResult;
		Ambassador abmassador = WebClientHelper.getAmbassador(hostName, port, useSSL);
		testResult = abmassador.helloWorld();
		return testResult;
	}
}
