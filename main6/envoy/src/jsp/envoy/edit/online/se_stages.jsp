<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.edit.online.SegmentVersion,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.ling.common.Text,
            java.util.Iterator,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_segmentVersions = bundle.getString("lb_segment_versions");
String lb_clickToCopy     = bundle.getString("action_click_copy");
String lb_noVersions      = bundle.getString("lb_no_segment_versions");
String lb_task_unknown    = bundle.getString("lb_task_unknown");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

List stages = view.getSegmentVersions();

StringBuffer stb_segments = new StringBuffer();

if (stages != null)
{
    int i = 0;
    String locale = "en-US";

    if (!view.isLocalizable())
    {
      locale = EditUtil.toRFC1766(state.getTargetLocale());
    }

    for (Iterator it = stages.iterator(); it.hasNext(); )
    {
        SegmentVersion v = (SegmentVersion)it.next();

        stb_segments.append("a_segments[");
        stb_segments.append(i++);
        stb_segments.append("] = { data: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
           v.getSegment())));
        stb_segments.append("\", text: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            EditUtil.stripTags(v.getSegment()))));
        stb_segments.append("\", label: \"");

        String lb_stage = v.getTaskName();
        if (lb_stage.length() == 0) lb_stage = lb_task_unknown;

        stb_segments.append(lb_stage);
        stb_segments.append("\", lang: \"");
        stb_segments.append(locale);
        stb_segments.append("\", dir: \"");
        if(EditUtil.isRTLLocale(state.getTargetLocale()))
        {
            stb_segments.append(
                Text.containsBidiChar(v.getSegment()) ? "RTL" : "LTR");
        }
        else
        {
            stb_segments.append("LTR");
        }
        stb_segments.append("\" };\n");
    }
}

String str_segments = stb_segments.toString();
%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none; }
.clickable { font-family:Arial, Helvetica, sans-serif; font-size: 9pt; 
	     color: blue; cursor: hand; cursor:pointer; }
.label     { font-family:Arial, Helvetica, sans-serif; font-size: 9pt;
	     font-weight: bold; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript">
var a_segments = new Array();
<%=str_segments%>

var value;
var index = 0;

function copyToTarget(value)
{
    parent.parent.SetSegment(value);
}

function initMatches()
{
  try
  {
    if (a_segments.length > 0)
    {
      showData(0);
    }
  }
  catch (e)
  {
  }
}

function showData(index)
{
  var o = a_segments[index];

  idLabel.innerText = o.label;
  idText.innerHTML  = o.text;
  if (o.lang)
  {
    idText.lang = o.lang;
  }
  if (o.dir)
  {
    idText.dir = o.dir;
  }

  if (index == 0)
  {
    idStagesPrev.style.visibility = 'hidden';
  }
  else
  {
    idStagesPrev.style.visibility = 'visible';
  }

  if (index == a_segments.length - 1)
  {
    idStagesNext.style.visibility = 'hidden';
  }
  else
  {
    idStagesNext.style.visibility = 'visible';
  }
}

function goLeft()
{
  if (a_segments.length > 0)
  {
    if (index == 0)
    {
      index = a_segments.length - 1;
    }
    else
    {
      --index;
    }
  }
  showData(index);
}

function goRight()
{
  if (a_segments.length > 0)
  {
    if (index == (a_segments.length - 1))
    {
      index = 0;  
    }
    else
    {
      ++index;
    }
  }
  showData(index);
}

function doClick()
{
  copyToTarget(a_segments[index].data);
}

function fnInit()
{
	if (document.recalc)
	{
   sourceBoxTitle.style.setExpression("pixelWidth", "document.body.clientWidth");
   sourceBoxTitle.style.setExpression("pixelHeight", "document.body.clientHeight - 35");
	}

   initMatches();
}

window.onload = fnInit;
</SCRIPT>
</HEAD>
<BODY VLINK="#0000FF">
<HR COLOR="#0C1476" WIDTH="95%">
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
  <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="5" HEIGHT="1"></TD>
  <TD WIDTH="100%">
    <SPAN CLASS="standardTextBold"><%=lb_segmentVersions%></SPAN>
    <SPAN CLASS="standardText"><%=lb_clickToCopy%></SPAN>
  </TD>
</TR>
</TABLE>
<DIV ID="sourceBoxTitle"
     STYLE="position: absolute; top: 35px; left: 0px; overflow: auto;">
<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="10" HEIGHT="1"></TD>
<%
if (stages != null && stages.size() > 0)
{
%>
    <TD VALIGN="TOP" ALIGN="LEFT" WIDTH="100%">
      <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0>
	<TR VALIGN=TOP>
	  <TD>
	    <IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idStagesPrev"
	    class="clickable" onclick="goLeft();return false;">
	  </TD>
	  <TD>
	    <SPAN id="idLabel" class="label" onclick="return false;"></SPAN>
	  </TD>
	  <TD>
	    <IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idStagesNext"
	    class="clickable" onclick="goRight();return false;">
	  </TD>
	  <TD id="idText" class="clickable" onclick="doClick()"></TD>
	</TR>
      </TABLE>
    </TD>
<%
}
else
{
%>
    <TD VALIGN="TOP" ALIGN="LEFT" WIDTH="100%"
      class="standardTextItalic"><%=lb_noVersions%></TD>
<%
}
%>
  </TR>
</TABLE>
</DIV>
</BODY>
</HTML>
