<%@ page contentType="application/vnd.ms-excel"
	errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.company.CompanyThreadLocal,
	        com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
	        com.globalsight.everest.usermgr.UserLdapHelper,
	        com.globalsight.everest.webapp.WebAppConstants,
	        com.globalsight.everest.webapp.pagehandler.administration.reports.FileListXlsReport"
	session="true"%>
<%
    //Multi-Company: get current user's company from the session
    HttpSession userSession = request.getSession(false);
    String companyName = (String) userSession
            .getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
    if (UserUtil.isBlank(companyName))
    {
        companyName = (String) userSession
                .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
    }
    if (companyName != null)
    {
        CompanyThreadLocal.getInstance().setValue(companyName);
    }
    String exportFormat = request.getParameter("exportFormat");
    if("xls".equals(exportFormat))
    {
        response.setHeader("Content-Disposition",
        "attachment; filename=DetailedWordCountByJob.xls");
    }
    else
    {
        response.setHeader("Content-Disposition",
        "attachment; filename=DetailedWordCountByJob.csv");
    }
    response.setHeader("Expires", "0");
    response.setHeader("Cache-Control",
            "must-revalidate, post-check=0,pre-check=0");
    response.setHeader("Pragma", "public");
    FileListXlsReport flxr = new FileListXlsReport();
    flxr.generateReport(request, response);
%>