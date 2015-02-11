<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.JobAttributeReportHelper,
         com.globalsight.everest.company.CompanyThreadLocal,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.everest.usermgr.UserLdapHelper,
         com.globalsight.everest.webapp.WebAppConstants"
         session="true"
%>
<%
response.setHeader("Content-Disposition","attachment; filename=JobAttributes.xlsx" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
response.setContentType("application/x-excel");
JobAttributeReportHelper helper = new JobAttributeReportHelper(request,response);
helper.generateReport();
out.clear();out = pageContext.pushBody();
%>