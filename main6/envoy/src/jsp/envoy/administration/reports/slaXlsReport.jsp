<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.SlaXlsReportHelper"
         session="true"
%><%!
    public SlaXlsReportHelper slaXlsReportHelper = null;
%><%
response.setHeader("Content-Disposition","attachment; filename=TranslationSlaPerformance.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
slaXlsReportHelper = new SlaXlsReportHelper(request,response);
slaXlsReportHelper.generateReport();
%>