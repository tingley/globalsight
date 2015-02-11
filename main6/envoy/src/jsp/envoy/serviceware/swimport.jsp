<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,    
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.ResourceBundle,
            java.util.Enumeration,
            java.util.Collection,
            java.util.Iterator,
            java.net.*,
            java.util.Hashtable,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.cxe.adapter.serviceware.ServiceWareAPI,
            com.globalsight.cxe.entity.fileprofile.FileProfile"
    session="true" %>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Hashtable ht = null;
    try {
        if (request.getAttribute("submittedImport") == null)
        {
            //connect up to serviceware and query all the knowledge objects
            String sessionId = ServiceWareAPI.connect();
            ht = ServiceWareAPI.getKnowledgeObjects(sessionId);
            ServiceWareAPI.disconnect(sessionId);
        }
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=bundle.getString("lb_serviceware_import")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "import";
  var helpFile = "<%=bundle.getString("help_import_main_screen")%>";

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=bundle.getString("lb_serviceware_import_heading")%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<% if (request.getAttribute("submittedImport") != null) {%>
<TR><TD><%=bundle.getString("lb_serviceware_subjob")%> <%=request.getAttribute("submittedJobName")%>
<TR><TD>&nbsp;</TD></TR>
<TR><TD><button name="Previous" onClick="javascript:location.href='<%=serviceWareUrl%>'"><%=bundle.getString("lb_previous")%></button></TD></TR>
</TD></TR>
<% } else { %>
<TR>
<TD WIDTH=500>
<%=bundle.getString("lb_serviceware_sel")%>
</TD>
</TR>
</TABLE>

<FORM ACTION="/globalsight/ControlServlet?activityName=swimport&startImport=true" METHOD="POST">
<TABLE CELLPADDING=1 CELLSPACING=1 BORDER=0 CLASS=standardText>
<TR><TD><%=bundle.getString("lb_serviceware_ko")%>: </TD>
<TD>
<SELECT NAME="KOID">
<% 
  Enumeration keys = ht.keys();
  while (keys.hasMoreElements())
  {
      String id = (String)keys.nextElement();
      String name = (String) ht.get(id);
      %>
<OPTION VALUE="<%=id%>"><%=name%></OPTION>
<% } %>
</SELECT>
</TD> <TR>
<TR><TD><%=bundle.getString("lb_serviceware_fp")%>: </TD>
<TD>
<SELECT NAME="FPID">
<%
Collection c = ServerProxy.getFileProfilePersistenceManager().getAllFileProfiles();
Iterator iter = c.iterator();
while (iter.hasNext())
{
    FileProfile fp = (FileProfile) iter.next();
    %>
<OPTION VALUE="<%=fp.getId()%>"><%=fp.getName()%></OPTION>
<%
}
%>
</SELECT>
</TD></TR>
<TR><TD>Job Name: </TD><TD><INPUT TYPE="TEXT" NAME="jobname"></INPUT></TD></TR>
<TR><TD><input TYPE="SUBMIT" VALUE="Import"></INPUT></TD></TR>
<%}%>
</TABLE>
</FORM>
<P>
</DIV>
</BODY>
</HTML>

