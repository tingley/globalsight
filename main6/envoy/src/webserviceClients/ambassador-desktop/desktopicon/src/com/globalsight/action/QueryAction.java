package com.globalsight.action;

import java.sql.Connection;

import com.globalsight.bo.QueryBO;

public class QueryAction extends Action
{
	public static String q_fileprofile = QueryBO.q_fileprofile; 

	public static String q_getStatus = QueryBO.q_getStatus;

	public static String q_getLocalizedDocs = QueryBO.q_getLocalizedDocs;
	
	public static String q_getAllPermissionsByUser = QueryBO.q_getAllPermissionsByUser;
	
	public static String q_getSourceLocales = QueryBO.q_getSourceLocales;

	public static String q_getTargetLocales = QueryBO.q_getTargetLocales;
	
	public static String q_getConnection = QueryBO.q_getConnection;
	
	public static String q_getPriorityByID = QueryBO.q_getPriorityByID;
	
	public String execute(String args[]) throws Exception
	{
		QueryBO queryBO = new QueryBO();
		return queryBO.query(args[0], accessToken, "");
	}
	
	public String execute(String parameter, String sourceLocale) throws Exception {
		QueryBO queryBO = new QueryBO();
		return queryBO.query(parameter, accessToken, sourceLocale);
	}
	
}
