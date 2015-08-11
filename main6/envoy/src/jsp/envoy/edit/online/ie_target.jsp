<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String url_upload = upload.getPageURL();

String lb_title = bundle.getString("lb_image_upload");
String lb_imageUrl = bundle.getString("lb_url")+bundle.getString("lb_colon");
String lb_target = bundle.getString("lb_target_image");
String lb_view = bundle.getString("lb_view");
String lb_upload = bundle.getString("lb_upload");
String lb_replaceImage = bundle.getString("lb_replace_image");
String lb_pleaseEnterData = bundle.getString("msg_replace_image");

String str_targetSegment = GxmlUtil.getInnerXml(view.getTargetSegment());
String url_image = view.getTargetImageUrl();

// Don't need language/direction because localizables are ASCII.
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
var o_textbox = null;
var b_changed = false;

// Callback for se_main.jsp.
function HilitePtags(bright)
{
}

// Callback for onload() of se_main.jsp.
function PostLoad()
{
}

function init()
{
    o_textbox = document.targetForm.targetText;
    o_textbox.focus();
    o_textbox.select();
}

function SetSegment(s, changed, whitepreserving)
{
    o_textbox.value = s;
    o_textbox.focus();
    o_textbox.select();

    if (changed)
    {
	SetChanged();
    }
    else
    {
        SetUnchanged();
    }
}

function GetSegment()
{
    return o_textbox.value;
}

function SetFocus()
{
    o_textbox.focus();
}

function IsChanged()
{
    var result;

    // IE5.0 only fires onchanged before onblur when the element has focus
    o_textbox.focus();
    o_textbox.blur();
    o_textbox.focus();
    result = b_changed;

    return result;
}
      
function SetChanged()
{
    b_changed = true;
}

function SetUnchanged()
{
    b_changed = false;
}

function CanClose()
{
    return !IsChanged();
}

function imageError()
{
  idTargetImage.alt = 'Image failed to load.';
  getEvent().returnValue = true;
}

function isUploadReady(form)
{
    var field = form.filename;
    var filename = field.value;

    form.targetImageURL.value = o_textbox.value;

    if (filename == null || filename == "")
    {
        alert("<%=lb_pleaseEnterData%>");
        field.focus();
        return false;
    }
    else
    {
        return true;
    }
}

function trapKeys()
{
	var event = getEvent();
    var key = event.keyCode || event.charCode;
    if (event.ctrlKey && !event.altKey)
    {
	if (key == 81) // "Q" 
	{
	    event.cancelBubble = true;
    	    event.returnValue = false;
	    parent.parent.doClose();
	    return;
        }
        else if (key == 83) // "S" 
	{
	    event.cancelBubble = true;
    	    event.returnValue = false;
	    parent.parent.doRefresh(0, true);
	    return;
        }
        else if (key == 33) // PAGE UP
        {
	    event.cancelBubble = true;
            event.returnValue = false;
	    parent.parent.doRefresh(-1, false);
	    return;
        }
        else if (key == 34) // PAGE DOWN
        {
	    event.cancelBubble = true;
            event.returnValue = false;
	    parent.parent.doRefresh(1, false);
	    return;
        }
    }
}
</SCRIPT>
</HEAD>
<BODY onkeydown="trapKeys()">
<HR COLOR="#0C1476" WIDTH="95%">
<DIV ID="targetLayer" STYLE="position: absolute; top: 15px; left: 0px;">
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="5" HEIGHT="1"></TD>
    <TD WIDTH="100%">
      <SPAN CLASS="standardTextBold"><%=lb_target%></SPAN>
    </TD>
  </TR>
</TABLE>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
  <FORM NAME="targetForm" style="display:inline" onsubmit="return false;"> 
  <TR>
    <TD ROWSPAN="5"><IMG SRC="/globalsight/images/spacer.gif" WIDTH="10" HEIGHT="1"></TD>
    <TD NOWRAP><SPAN class="standardText"><%=lb_imageUrl%></SPAN>
      <INPUT TYPE="text" CLASS="standardText" SIZE="45"
      VALUE="<%=str_targetSegment%>" NAME="targetText" ID="textarea"
      onchange="SetChanged()" onchange="trapKeys()"></INPUT>
    </TD>
  </TR>
  </FORM>
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="1" HEIGHT="2"></TD>
  </TR>
  <FORM NAME="uploadForm" METHOD="POST" ACTION="<%=url_upload%>" target="_top"
  ENCTYPE="multipart/form-data" onSubmit="return isUploadReady(this)">
  <TR>
    <TD WIDTH="100%" NOWRAP>
      <SPAN class="standardText"><%=lb_replaceImage%></SPAN>
      <INPUT TYPE="file" SIZE="20" class="standardText" NAME="filename">
      <INPUT TYPE="hidden" class="standardText" NAME="targetImageURL">
      <INPUT TYPE="submit" VALUE="<%=lb_upload%>" class="standardText">
    </TD>
  </TR>
  </FORM>
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="1" HEIGHT="4"></TD>
  </TR>
  <TR>
    <TD WIDTH="100%"><IMG SRC="<%=url_image%>" id="idTargetImage" 
      style="border: 1px solid black;" onerror="imageError()"></TD>
  </TR>
</TABLE>
</DIV>
</BODY>
<script>
init();
parent.parent.finishFrame();
</script>

</HTML>
