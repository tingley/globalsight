<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="com.globalsight.everest.webapp.pagehandler.administration.reports.JobOnlineXlsReport,
		com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
		com.globalsight.everest.company.CompanyThreadLocal,
		com.globalsight.everest.usermgr.UserLdapHelper,
		com.globalsight.everest.webapp.WebAppConstants" session="true" 
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

response.setHeader("Content-Disposition","attachment; filename=OnlineJobs.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
JobOnlineXlsReport onlineReport = new JobOnlineXlsReport(request,response);
%>