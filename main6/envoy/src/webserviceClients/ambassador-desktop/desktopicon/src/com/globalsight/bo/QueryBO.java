package com.globalsight.bo;

import java.sql.Connection;

import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class QueryBO
{
	public static String q_fileprofile = "fileprofile";

	public static String q_getStatus = "getStatus"; 

	public static String q_getLocalizedDocs = "getLocalizedDocs";
	
	public static String q_getAllPermissionsByUser = "getAllPermissionsByUser";

	public static String q_getSourceLocales = "getSourceLocales";

	public static String q_getTargetLocales = "getTargetLocales";
	
	public static String q_getConnection = "getConnection";
	
	public static String q_getPriorityByID = "getPriorityByID";
	
	public String query(String parameter, String accessToken, String jobName)
			throws Exception
	{
		String result = "";

		Ambassador ambassador = WebClientHelper.getAmbassador();
		if (q_fileprofile.equals(parameter))
		{
			result = ambassador.getFileProfileInfoEx(accessToken);
		}

		if (q_getStatus.equals(parameter))
		{
			result = ambassador.getStatus(accessToken, jobName);
		}

		if (q_getLocalizedDocs.equals(parameter))
		{
			result = ambassador.getLocalizedDocuments(accessToken, jobName);
		}
		
		if (q_getAllPermissionsByUser.equalsIgnoreCase(parameter))
		{
			result = ambassador.getAllPermissionsByUser(accessToken);
		}
		//getAllPermissionsByUser
		if (q_getSourceLocales.equalsIgnoreCase(parameter))
		{
			result = ambassador.getSourceLocales(accessToken);
		}
		if (q_getTargetLocales.equalsIgnoreCase(parameter))
		{
			result = ambassador.getTargetLocales(accessToken, jobName);
		}
		if (q_getConnection.equalsIgnoreCase(parameter))
		{
			result = ambassador.getConnection(accessToken);
		}
		if (q_getPriorityByID.equalsIgnoreCase(parameter))
		{
			result = ambassador.getPriorityByID(accessToken, jobName);
		}

		return result;
	}
	
}
