<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    String okURL = ok.getPageURL() + "&action=ok";
    String pagetitle = bundle.getString("lb_view_vendor") + " - " +
                       bundle.getString("lb_details");
    
    //Labels
    String lbOK = bundle.getString("lb_ok");    

%>
<html>
<head>
<title><%=pagetitle%></title>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script>
var needWarning = false;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var helpFile = "<%=bundle.getString("help_vendors_vendor_details")%>";
var guideNode = "vendors";

function submitForm(btnName)
{
    if (btnName == "ok")
    {
        vendorForm.action = "<%=okURL %>";
    }
    vendorForm.submit();
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; top: 108px; left: 20px; right: 20px;">

<span class="mainHeading">
<%=pagetitle%>
</span>
<jsp:include page="viewBasic.jsp"/>
<br>
<jsp:include page="viewContact.jsp"/>
<br>
<jsp:include page="viewRoles.jsp"/>
<br>
<jsp:include page="viewProjects.jsp"/>
<br>
<jsp:include page="viewCV.jsp"/>
<br>
<jsp:include page="viewCustom.jsp"/>

<p>
<input type="button" name="<%=lbOK%>" value="<%=lbOK%>"
            onclick="location.replace('<%=okURL%>')">
</div>
</body>
</html>
