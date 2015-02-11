<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="com.globalsight.everest.webapp.pagehandler.administration.reports.ActivityDurationReportGenerator,
        com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
        com.globalsight.everest.company.CompanyThreadLocal,
        com.globalsight.everest.usermgr.UserLdapHelper, 
        com.globalsight.everest.webapp.WebAppConstants,
        java.io.File" 
        session="true"
%><%
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
ActivityDurationReportGenerator activityDurationReportGenerator = new ActivityDurationReportGenerator();
File[] reports = activityDurationReportGenerator.generateReport( request, response );
ReportHelper.sendFiles(reports, null, response);
out.clear();out = pageContext.pushBody();
%>
