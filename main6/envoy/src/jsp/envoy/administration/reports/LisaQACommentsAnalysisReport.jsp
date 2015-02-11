<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.ReviewerLisaQAXlsReportHelper"
         session="true"
%><%
response.setHeader("Content-Disposition","attachment; filename=CommentsAnalysis.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");

ReviewerLisaQAXlsReportHelper reviewerLisaQAXlsReportHelper = 
		new ReviewerLisaQAXlsReportHelper(request, response, ReviewerLisaQAXlsReportHelper.COMMENTS_ANALYSIS);
reviewerLisaQAXlsReportHelper.generateReport();
%>