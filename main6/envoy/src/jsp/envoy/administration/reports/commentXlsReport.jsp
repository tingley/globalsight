<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.CommentXlsReportHelper"
         session="true"
%><%!
    public CommentXlsReportHelper commentXlsReportHelper = new CommentXlsReportHelper();
%><%    
response.setHeader("Content-Disposition","attachment; filename=SegmentComments.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
commentXlsReportHelper.generateReport(request,response);
%>