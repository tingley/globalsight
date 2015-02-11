<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
      com.globalsight.util.edit.EditUtil,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.everest.glossaries.GlossaryUpload,
      com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryConstants,
      com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
      com.globalsight.util.resourcebundle.ResourceBundleConstants,
      com.globalsight.util.resourcebundle.SystemResourceBundle,
      com.globalsight.util.GlobalSightLocale,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

GlossaryState state =
  (GlossaryState)session.getAttribute(WebAppConstants.GLOSSARYSTATE);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

String url_upload = upload.getPageURL();

String lb_title = bundle.getString("lb_upload_glossary");
String lb_sourceLocale = bundle.getString("lb_source_locale");
String lb_targetLocale = bundle.getString("lb_target_locale");
String lb_select_any_source_locale = bundle.getString("lb_select_any_source_locale");
String lb_select_any_target_locale = bundle.getString("lb_select_any_target_locale");

String lb_uploadSuccessful = bundle.getString("lb_upload_successful");
String lb_uploadFailed = bundle.getString("lb_upload_failed");
String lb_help = bundle.getString("lb_help");
String lb_close = bundle.getString("lb_close");

String lb_upload = bundle.getString("lb_upload");
String lb_selectFile = bundle.getString("lb_select_file");
String lb_selectLanguages = bundle.getString("lb_select_languages");

// msg_replace_image = "Please enter a file name."
String lb_pleaseEnterData = bundle.getString("msg_replace_image");

String str_message = state.getMessage();
state.setMessage(null);

if (str_message != null)
{
    if (str_message.length() == 0)
    {
        str_message = "<P><span class='standardText'>" +
            lb_uploadSuccessful + "</span><P>";
    }
    else
    {
        str_message = "<P><span class='warningText'>" +
            lb_uploadFailed + " " + str_message + "</span><P>";
    }
}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/menuparams.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/menuengine.js"></SCRIPT>
<SCRIPT>
var reload = false;

function prepToClose()
{
  if (!reload)
  {
    opener.modalFlag = false;
    window.close();
  }
}

var helpFile = "<%=bundle.getString("help_support_files_upload")%>";
function helpSwitch() 
{  
   // The variable helpFile is defined in each JSP
   helpWindow = window.open(helpFile,'helpWindow','resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
   helpWindow.focus();
}

function isReady(form)
{
    var field = form.filename;
    var filename = field.value;

    if (filename == null || filename == "")
    {
        alert("<%=lb_pleaseEnterData%>");
        field.focus();
        reload = false;
        return false;
    }
    else
    {
        idBody.style.cursor = "wait";
        uploadForm.idSubmit.style.cursor = "wait";
        return true;
    }
}

function showMessage(message)
{
    if (message != "")
    {
        window.showModalDialog("/globalsight/envoy/administration/glossaries/uploadMessage.jsp",
            message,
            "center:yes; help:no; resizable:yes; status:no; " +
            "dialogWidth: 300px; dialogHeight: 180px; ");
    }
}

function doOnload()
{
    if (opener) opener.blockEvents();
    self.focus();
    showMessage('<%=str_message != null ? EditUtil.toJavascript(str_message) : ""%>');
}

function doOnunload()
{
    if (opener && opener.modalFlag) opener.unblockEvents();
    prepToClose();
    if (!reload) opener.location = opener.location;
}
</SCRIPT>
</HEAD>
<BODY id="idBody" bgcolor="#FFFFFF" onload="doOnload()" onunload="doOnunload()">
<DIV ID="targetLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 20px; LEFT: 20px; RIGHT: 20px;">
<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 WIDTH="100%">
  <TR>
    <TD><SPAN CLASS="mainHeading"><%=lb_title%></SPAN></TD>
    <TD ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lb_close%>" VALUE="<%=lb_close%>" 
      onclick="prepToClose()">   
      <INPUT TYPE="BUTTON" NAME="<%=lb_help%>" VALUE="<%=lb_help%>" 
      onclick="helpSwitch()">  
    </TD>
  </TR>
</TABLE>

<P>

<FORM NAME="uploadForm" METHOD="POST" ACTION="<%=url_upload%>"
 ENCTYPE="multipart/form-data" onSubmit="reload=true; return isReady(this)"
 CLASS="standardText">

<SPAN class="standardTextBold"><%=lb_selectLanguages%></SPAN>
<BR>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" class="standardText">
    <TR>
        <TD>
            <%=lb_sourceLocale%>:
            <BR>
            <SELECT NAME="<%=GlossaryUpload.KEY_SOURCE_LOCALE%>">
            <OPTION SELECTED VALUE="<%=GlossaryUpload.KEY_ANY_SOURCE_LOCALE%>"><%=lb_select_any_source_locale%></OPTION>
<%
  Collection col;
  Iterator it;

  if ((col = state.getAllSourceLocales()) != null)
  {
    for (it = col.iterator(); it.hasNext(); )
    {
      GlobalSightLocale loc = (GlobalSightLocale)it.next();
%>
  <OPTION VALUE="<%=loc.toString()%>"><%=loc.getDisplayName(uiLocale)%></OPTION>
<%
    }
  }
%>
            </SELECT>
        </TD>
        <TD WIDTH="30">&nbsp;</TD>
        <TD>
            <%=lb_targetLocale%>:
            <BR>
            <SELECT  NAME="<%=GlossaryUpload.KEY_TARGET_LOCALE%>">
            <OPTION SELECTED VALUE="<%=GlossaryUpload.KEY_ANY_TARGET_LOCALE%>"><%=lb_select_any_target_locale%></OPTION>
<%
  if ((col = state.getAllTargetLocales()) != null)
  {
    for (it = col.iterator(); it.hasNext(); )
    {
      GlobalSightLocale loc = (GlobalSightLocale)it.next();
%>
  <OPTION VALUE="<%=loc.toString()%>"><%=loc.getDisplayName(uiLocale)%></OPTION>
<%
    }
  }
%>
            </SELECT>
        </TD>
    </TR>
</TABLE>

<P>
      <SPAN class="standardTextBold"><%=lb_selectFile%></SPAN>
<BR>
      <INPUT TYPE="file" SIZE="60" NAME="filename">
      <INPUT TYPE="submit" VALUE="<%=lb_upload%>" name="idSubmit">
</P>
</FORM>
<P></P>
</DIV>
</BODY>
</HTML>
