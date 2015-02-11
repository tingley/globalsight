<%@ page contentType="application/vnd.ms-excel"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.CharacterCountXlsReport"
         session="true"
%><%
response.setHeader("Content-Disposition","attachment; filename=CharacterCountReport.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");

CharacterCountXlsReport characterCountXlsReport = 
		new CharacterCountXlsReport(request, response);
characterCountXlsReport.generateReport();
%>