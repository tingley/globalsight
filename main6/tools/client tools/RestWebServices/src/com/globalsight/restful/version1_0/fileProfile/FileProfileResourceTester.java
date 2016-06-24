package com.globalsight.restful.version1_0.fileProfile;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;

public class FileProfileResourceTester extends RestfulApiTestHelper
{
	private String userName = null;
	private String password = null;

	public FileProfileResourceTester(String userName, String password)
	{
		this.userName = userName;
		this.password = password;
	}

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{
	 * companyName}/fileProfiles/getFileProfiles
	 * */
	public String testGetFileProfiles()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		try
		{
			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/fileProfiles/getFileProfiles");
			HttpGet httpGet = getHttpGet(url.toString(), userName, password);

			httpResponse = httpClient.execute(httpGet);

			return printHttpResponse(httpResponse);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consumeQuietly(httpResponse);
		}
		return null;
	}

	public static void main(String[] args)
	{
		FileProfileResourceTester tester = new FileProfileResourceTester(
				"allieadmin", "password");
		try
		{
			tester.testGetFileProfiles();
		}
		finally
		{
			tester.shutdownHttpClient();
		}
	}
}
