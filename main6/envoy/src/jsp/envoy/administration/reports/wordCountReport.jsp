<%@ page
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.WordCountProcessor,
         		com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsProcessor"
         session="true"
%><%!
	public WordCountProcessor processor = null;
%><%
response.setHeader("Content-Disposition","attachment; filename=WordCount.csv" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
//response.setContentType("application/octet-stream");
response.setContentType("text/csv");
//response.setHeader("content-type","text/csv;charset=UTF8");
processor = new WordCountProcessor();
processor.generateReport(request, response);
%>