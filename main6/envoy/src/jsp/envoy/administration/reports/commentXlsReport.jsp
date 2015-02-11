<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.CommentXlsReportHelper"
         session="true"
%><%!
    public CommentXlsReportHelper commentXlsReportHelper = null;
%><%   
response.setHeader("Content-Disposition","attachment; filename=SegmentComments.xlsx" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
response.setContentType("application/x-excel");
commentXlsReportHelper = new CommentXlsReportHelper();
commentXlsReportHelper.generateReport(request,response);
%>