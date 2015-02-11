<%@page import="com.globalsight.everest.webapp.pagehandler.tm.management.Tm3ConvertProcess"%>
<%@page import="com.globalsight.util.StringUtil"%>
<%@page import="com.globalsight.everest.servlet.util.ServerProxy"%>
<%@page import="com.globalsight.everest.projecthandler.ProjectTM"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.tm.management.Tm3ConvertHelper"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,java.util.ResourceBundle,java.text.MessageFormat,com.globalsight.util.edit.EditUtil,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.servlet.util.SessionManager,java.io.IOException"
    session="true"%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    
    Tm3ConvertProcess convertProcess = Tm3ConvertProcess.getInstance();
    String returnString = "";
    String tm2name = "", tm3name = "";
    int convertRate = 0;
    boolean hasConversion = false;
    long tm2Id = 0;

    tm2Id = convertProcess.getTm2Id();
    if (tm2Id > 0) {
        convertRate = convertProcess.getConvertedRate();
        returnString = convertProcess.getTm3Id() + "," + convertRate + "," + convertProcess.getStatus() + "," + convertProcess.getTm3Name() + ",<i><font color='red'>" + convertProcess.getTm2Name()
                + " " + bundle.getString("msg_tm_converting") + " " + convertProcess.getTm3Name() + " (" + convertRate + "%)</font>&nbsp;&nbsp;";
        if (convertProcess.getConvertHelper() != null)
            returnString += "<a href='#' title='" + bundle.getString("msg_tm_convert_cancel") + "' onclick='cancelConvert()'>" + bundle.getString("lb_cancel") + "</a>";
        returnString += "</font></i>";
    }
    out.print(returnString);
%>
