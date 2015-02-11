<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.ReviewerLisaQAXlsReportHelper"
         session="true"
%><%
String isLSOExtStr = (String)request.getParameter("isLSOExt");
boolean isLSOExt = isLSOExtStr == null? false: new Boolean(isLSOExtStr).booleanValue();
if(isLSOExt)
{
    response.setHeader("Content-Disposition","attachment; filename=ImplementedCommentsCheck.xls" );
}else
{
    response.setHeader("Content-Disposition","attachment; filename=ReviewerCommentReport.xls" );
}
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
ReviewerLisaQAXlsReportHelper reviewerLisaQAXlsReportHelper = 
		new ReviewerLisaQAXlsReportHelper(request, response, ReviewerLisaQAXlsReportHelper.LANGUAGE_SIGN_OFF);
reviewerLisaQAXlsReportHelper.generateReport();
%>