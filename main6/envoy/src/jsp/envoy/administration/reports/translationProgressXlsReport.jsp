<%@ page
	errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.webapp.pagehandler.administration.reports.TranslationProgressReportHelper"
	session="true"%><%
	response.reset();
	response.setContentType("application/vnd.ms-excel; charset=UTF-8");
	response.setHeader("Content-Disposition",
			"attachment; filename=Translation_Progress_Report.xlsx");
	response.setHeader("Expires", "0");
	response.setHeader("Cache-Control",
			"must-revalidate, post-check=0,pre-check=0");
	response.setHeader("Pragma", "public");
	response.setContentType("application/x-excel");

	TranslationProgressReportHelper transProgressReportHelper = new TranslationProgressReportHelper();
	transProgressReportHelper.generateReport(request, response);
%>