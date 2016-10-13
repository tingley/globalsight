<%@page import="com.globalsight.everest.webapp.pagehandler.rss.Item"%>
<%@page import="com.globalsight.everest.servlet.util.ServerProxy"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.rss.RSSPersistenceManager"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%
String id = request.getParameter("id");
if (id == null || id.trim().equals(""))
	out.print("");
else {
	try {
		RSSPersistenceManager rssManager = ServerProxy.getRSSPersistenceManager();
		Item item = rssManager.getItem(Long.parseLong(id));
		if (rssManager.markRead(item))
			out.print(id);
		else
			out.print("");
	} catch (Exception e) {
		out.print("");
	}
}


%>