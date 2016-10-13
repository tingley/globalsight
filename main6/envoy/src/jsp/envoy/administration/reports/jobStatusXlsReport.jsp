<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.JobStatusXlsReportProcessor,
         		com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsProcessor"
         session="true"
%><%!
	public ReportsProcessor processor = null;
%><%
response.setHeader("Content-Disposition","attachment; filename=JobStatus.xlsx" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
response.setContentType("application/x-excel");

processor = new JobStatusXlsReportProcessor();
processor.generateReport(request, response);
%>