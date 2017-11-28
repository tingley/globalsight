
<%@page import="com.globalsight.everest.webapp.pagehandler.rss.RSSUtil"%>
<%@page import="com.globalsight.everest.company.CompanyWrapper"%>
<%@page import="com.globalsight.util.AmbFileStoragePathUtils"%>
<%@page import="com.globalsight.everest.cvsconfig.CVSModule"%><%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*, java.util.*" %>
<%
String id = request.getParameter("id");
if (id == null || id.trim().equals(""))
	id = "0";
String root = RSSUtil.getStorageRoot();
String rootPath = new File(root).getCanonicalPath();
StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>");
try {
	File rootFile = null;
	if ("0".equals(id)) {
		rootFile = new File(root);
		sb.append("<tree id=\"0\">");
	} else {
		rootFile = new File(id);
		String path = rootFile.getCanonicalPath();
		if (!path.startsWith(rootPath)) {
		  	response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Cache-Control", "no-cache");
			response.getWriter().write("<Error>Bad pathname.</Error>");
			response.getWriter().close();
		  	 return;
	    }
		
		sb.append("<tree id=\"").append(id).append("\">");
	}
	String[] files = rootFile.list();
	if (files != null && files.length>0) {
		File file = null;
		String baseFileName = rootFile.getAbsolutePath() + File.separator;
		String fileName = "";
		for (int i=0;i<files.length;i++) {
			if (files[i].equals("CVS"))
				continue;
			fileName = baseFileName + files[i];
			//fileName = files[i];
			file = new File(fileName);
			if (file.isFile()) {
				sb.append("<item text=\"").append(files[i]).append("\" id=\"").append(fileName).append("\"");
				sb.append(" im0=\"book.gif\" im1=\"books_open.gif\" im2=\"books_close.gif\" child=\"0\" />");
			} else if (file.isDirectory()) {
				sb.append("<item text=\"").append(files[i]).append("\" id=\"").append(fileName).append("\"");
				sb.append(" im0=\"book.gif\" im1=\"books_open.gif\" im2=\"books_close.gif\" child=\"1\" />");
			}
		}
	}
	sb.append("</tree>");
	response.setContentType("text/xml");
	response.setCharacterEncoding("UTF-8");
	response.setHeader("Cache-Control", "no-cache");

	response.getWriter().write(sb.toString());
	response.getWriter().close();
} catch (Exception e) {
  System.out.println("Error ==== " + e.toString());
  //e.printStackTrace();
}
//System.out.println("XML==" + sb.toString());
%>
    