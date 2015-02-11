<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="com.globalsight.everest.company.CompanyThreadLocal,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,                
        com.globalsight.everest.usermgr.UserLdapHelper,
        com.globalsight.everest.webapp.pagehandler.administration.reports.OnlineRevStatusXlsReportGenerator,
        com.globalsight.everest.webapp.WebAppConstants" 
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
OnlineRevStatusXlsReportGenerator onlineRevStatusXlsReportGenerator = new OnlineRevStatusXlsReportGenerator();

response.setHeader("Content-Disposition","attachment; filename=OnlineReviewStatus.xlsx" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public"); 
response.setContentType("application/x-excel");
onlineRevStatusXlsReportGenerator.generateReport(request,response);
out.clear();out = pageContext.pushBody();
%>
