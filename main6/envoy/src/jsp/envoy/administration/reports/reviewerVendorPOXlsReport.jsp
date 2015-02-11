<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.ReviewerVendorPoXlsReportHelper,
         com.globalsight.everest.company.CompanyThreadLocal,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.everest.usermgr.UserLdapHelper,
         com.globalsight.everest.webapp.WebAppConstants"
         session="true"
%><%!
    public ReviewerVendorPoXlsReportHelper reviewerVendorPoXlsReportHelper = null;
%><%
response.setHeader("Content-Disposition","attachment; filename=ReviewerVendorPO.xlsx" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
response.setContentType("application/x-excel");
//Multi-Company: get current user's company from the session
HttpSession userSession = request.getSession(false);
String companyName = (String)userSession.getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
if (UserUtil.isBlank(companyName))
{
    companyName = (String)userSession.getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
}
if (companyName != null)
{
    CompanyThreadLocal.getInstance().setValue(companyName);
}
reviewerVendorPoXlsReportHelper = new ReviewerVendorPoXlsReportHelper(request,response);
reviewerVendorPoXlsReportHelper.generateReport();
%>