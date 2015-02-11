<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.tm.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String urlCreate = create.getPageURL();
String urlCancel = cancel.getPageURL();
String lb_createBtn = bundle.getString("lb_create1") + " " + bundle.getString("lb_dtd");
String title = lb_createBtn;
String helper = bundle.getString("lb_sgml_create_help");

%>
<HTML XMLNS:gs>
<!-- This is \envoy\administration\config\sgmlrulefile\create.jsp -->
<HEAD>
<TITLE><%=title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "sgmlRules";
var helpFile = "<%=bundle.getString("help_sgml_rules_create_dtd")%>";

function Result(message, description, element)
{
    this.message = message;
    this.description = description;
    this.element = element;
}

function doCancel()
{
    //window.navigate("<%=urlCancel%>");
    window.location.href = "<%=urlCancel%>";
}

function doCreate()
{
    var result = checkForm();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        oForm.submit();
    }
}

function checkForm()
{
    var result = new Result("", "", null);

    var value = oForm.publicid.value;
    var regex = /[\\:;\*\?\|\"\'<>]/;

    if (value == "")
    {
        return new Result(
          "<%=bundle.getString("lb_sgml_create_invalid") %>",
          "<%=bundle.getString("lb_sgml_create_invalid_empty") %>",
          oForm.publicid);
    }
    // Do not allow "\",  "/", ":" and other characters in the name
    // that are not valid in Windows (or Unix) filenames.
    else if (regex.test(value))
    {
        return new Result(
          "<%=bundle.getString("lb_sgml_create_invalid") %>",
          "<%=bundle.getString("lb_sgml_create_invalid_char") %> \\ : ; * ? | \" < > \'.",
          oForm.publicid);
    }

    return result;
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    oForm.publicid.focus();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading" ID="idHeading"><%=title %></SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=helper %></TD>
  </TR>
</TABLE>
<BR>

<FORM NAME="oForm" ACTION="<%=urlCreate%>&action=create" METHOD="post">

<DIV style="margin-bottom: 12px">
<BR>
<%=bundle.getString("lb_sgml_create_name") %>:
<BR>
<INPUT TYPE="text" NAME="publicid" id="idPublicId" SIZE=40>
<BR><BR>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("lb_sgml_create_example") %>
      <BR><BR>
      <TT style="margin-left:2em">&lt;!DOCTYPE article PUBLIC "<B>-//OASIS//DTD DocBook V4.1//EN</B>"&gt;</TT>
    </TD>
  </TR>
</TABLE>

</DIV>
</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" VALUE="<%=lb_createBtn %>"
 onclick="doCreate()">
</DIV>
</DIV>

</BODY>
</HTML>
