<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
	        com.globalsight.everest.edit.online.SegmentView,
	        com.globalsight.everest.edit.online.SegmentMatchResult,
	        com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.webapp.WebAppConstants,
	        com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
	        com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.ling.common.Text,
            com.globalsight.ling.tm.TuvBasicInfo,
            com.globalsight.everest.tuv.Tuv,
            com.globalsight.everest.tuv.TuvManager,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.date.DateHelper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.util.Iterator,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            java.text.NumberFormat"
    session="true"
%><%

ResourceBundle bundle = PageHandler.getBundle(session);

String lb_matchResults = bundle.getString("lb_match_results");
String lb_clickToCopy  = bundle.getString("action_click_copy");
String lb_noSegments   = bundle.getString("lb_no_match_results");
String lb_SourceName = bundle.getString("lb_match_source");
String lb_TargetName = bundle.getString("lb_match_target");
String lb_details = bundle.getString("lb_details");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String str_sourceSegment = GxmlUtil.getInnerXml(view.getSourceSegment());
NumberFormat percent = NumberFormat.getPercentInstance(
  (Locale)session.getAttribute(WebAppConstants.UILOCALE));
percent.setMinimumFractionDigits(2);

StringBuffer stb_segments = new StringBuffer();

List tmMatches = view.getTmMatchResults();
if (tmMatches != null)
{
    int i = 0;
    String locale = "en-US";

    if (!view.isLocalizable())
    {
      locale = EditUtil.toRFC1766(state.getTargetLocale());
    }

    for (Iterator it = tmMatches.iterator(); it.hasNext(); )
    {
        SegmentMatchResult p = (SegmentMatchResult)it.next();

        stb_segments.append("a_segments[");
        stb_segments.append(i++);
        stb_segments.append("] = { data: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            p.getMatchContent())));
        stb_segments.append("\", text: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            EditUtil.stripTags(p.getMatchContent()))));
        stb_segments.append("\", matchedSource: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            p.getMatchContentSource())));
		stb_segments.append("\", label: \"");
        stb_segments.append(percent.format(Math.floor(p.getMatchPercentage()*100)/10000.0));
        stb_segments.append("(");
        stb_segments.append(p.getTmName());
        stb_segments.append(")");
        stb_segments.append("\", lang: \"");
        stb_segments.append(locale);
        stb_segments.append("\", dir: \"");
        if(EditUtil.isRTLLocale(state.getTargetLocale()))
        {
            stb_segments.append(
                Text.containsBidiChar(p.getMatchContent()) ? "RTL" : "LTR");
        }
        else
        {
            stb_segments.append("LTR");
        }
        stb_segments.append("\", srcDir: \"");
        if (EditUtil.isRTLLocale(state.getSourceLocale()))
        {
            if(!(p.getMatchContentSource().equals("")))
            {
                stb_segments.append(
                        Text.containsBidiChar(p.getMatchContentSource()) ? "RTL" : "LTR");
            }
            else
            {
                stb_segments.append(
                        Text.containsBidiChar(str_sourceSegment) ? "RTL" : "LTR");
            }
            
        }
        else
        {
            stb_segments.append("LTR");
        }
        
        stb_segments.append("\", sid: \"");
        //Get TUV SID by ID
        String sid = p.getSid();
        if (null == sid) {
        	sid = "N/A";
        }
        stb_segments.append(EditUtil.encodeXmlEntities(sid));
        
        TuvBasicInfo matchedTuvBasicInfo = p.getMatchedTuvBasicInfo();
        String matchedTuvJobName = p.getMatchedTuvJobName()==null?"N/A":p.getMatchedTuvJobName();
        String creationUser = (matchedTuvBasicInfo==null)?"N/A":EditUtil.encodeXmlEntities(UserUtil.getUserNameById(matchedTuvBasicInfo.getCreationUser()));
        String creationDate  = (matchedTuvBasicInfo==null)?"N/A":DateHelper.getFormattedDateAndTime(matchedTuvBasicInfo.getCreationDate());
        String modifyUser = (matchedTuvBasicInfo==null||matchedTuvBasicInfo.getModifyUser()==null)?"N/A":EditUtil.encodeXmlEntities(UserUtil.getUserNameById(matchedTuvBasicInfo.getModifyUser()));
        String modifyDate  = (matchedTuvBasicInfo==null||modifyUser=="N/A")?"N/A":DateHelper.getFormattedDateAndTime(matchedTuvBasicInfo.getModifyDate());
        String lmMatchType = (p.getMatchType()==null?"N/A":p.getMatchType());

        stb_segments.append("\", creationDate: \"");
        stb_segments.append(creationDate);
        stb_segments.append("\", creationUser: \"");
        stb_segments.append(creationUser);
        stb_segments.append("\", modifyDate: \"");
        stb_segments.append(modifyDate);
        stb_segments.append("\", modifyUser: \"");
        stb_segments.append(modifyUser);
        stb_segments.append("\", matchedTuvJobName: \"");
        stb_segments.append(matchedTuvJobName);
        stb_segments.append("\", tmName: \"");
        stb_segments.append(p.getTmName());
        stb_segments.append("\", matchType: \"");
        stb_segments.append(lmMatchType);

        stb_segments.append("\" };\n");
    }
}

String str_segments = stb_segments.toString();
%>
<HTML>
<HEAD>
<link type="text/css" rel="StyleSheet" id="cssPtag"
  href="/globalsight/envoy/edit/online2/ptag.css">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none; }
.clickable { font-family:Arial, Helvetica, sans-serif; font-size: 9pt; 
	     cursor: hand;cursor:pointer; }
.source { font-family:Arial, Helvetica, sans-serif; font-size: 9pt;}
.label     { font-family:Arial, Helvetica, sans-serif; font-size: 9pt;
	     font-weight: bold; }
.link      { color: blue; cursor: hand; cursor:pointer;}
</STYLE>
<SCRIPT LANGUAGE="JavaScript">
var a_segments = new Array();
<%=str_segments%>

var value;
var index = 0;
var g_ptagsverbose =
  "<%=state.getPTagFormat()%>" == "<%=EditorConstants.PTAGS_VERBOSE%>";

function copyToTarget(value)
{
    parent.parent.SetSegment(value);
}

function PostLoad()
{
  convertMatches();
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

function markDiff(s1, s2, diff) {
  var splitChar = "";
  var ori = s1;
  
  if (s1 == "") {
    diff.innerHTML = s2;
    return;
  } else if (s2 == "") {
    diff.innerHTML = s1;
    return;
  }
  s1 = s1.replace(/<[^>].*?>/g,"").replace(/\[(x|ph)\d+\]|&(gt|lt)?;|[,.=\-:%~\|<>\?()\'\"]/g, " ");
  s2 = s2.replace(/<[^>].*?>/g,"").replace(/\[(x|ph)\d+\]|&(gt|lt)?;|[,.=\-:%~\|<>\?()\'\"]/g, " ");
  
  s1 = s1.replace(/\[/g, "").replace(/\]/g, "");
  s2 = s2.replace(/\[/g, "").replace(/\]/g, "");
  
  var a1 = s1.split(" ");
  var a2 = s2.split(" ");
  var length = a1.length;
  
  var i = 0;
  /**
  if (length < 3) {
    //Used to process string like zh-CN, which don't be splitted by space.
    for (i=0;i<s1.length;i++)
        a1[i] = s1.charAt(i);
    for (i=0;i<s2.length;i++)
        a2[i] = s2.charAt(i);
  } else {
    splitChar = " ";
  }
  length = a1.length;
  */
  var diffStr = "";
  for (var i=0;i<length;i++) {
    if (a1[i] != "" && s2.indexOf(a1[i]) == -1) {
        ori = replaceString(ori, a1[i], "<span style='background-color:yellow;'>" + a1[i] + "</span>");
    }
  }
  diff.innerHTML = ori;
}

function replaceString(s1, s2, s3) {
  if (s1.indexOf(s2) == -1)
    return s1;
  if (s2 == "" || s3 == "")
    return s1;
    
  var re = new RegExp("\\b" + s2 + "\\b", "g");
  var arr = s1.split(re);
  var len = arr.length;
  var tmp = "";
  //There is a problem that len will be 1 if there is no matching string or 
  //the s2 includes special characters such as @, $, *, # etc.
  //So it needs to use validateContent to identify if there includes special
  //characters
  if (len <= 1) {
    if (!validateContent(s2)) {
        var fi = 0;
        var index = 0;
        while ((fi = s1.indexOf(s2, index)) != -1 && index < s1.length) {
          bs = s1.substring(index, fi);
          tmp += bs + s3;
          index = fi + s2.length;
        }
        tmp += s1.substring(index);
    } else {
        fi = s1.indexOf(s2);
        bs = s1.substring(0, fi);
        es = s1.substring(fi + s2.length);
        tmp = bs + s3 + es;
    }
  } else {
    tmp = arr[0];
    for (i=1;i<arr.length;i++) {
      tmp += s3 + arr[i];
    }
  }
  return tmp;
}

function validateContent(str) {
  var disallowChars = "!@#$&*";
  if (str == null || str.length == 0)
    return true;
  for (var i=0;i<str.length;i++) {
    if (disallowChars.indexOf(str.charAt(i)) != -1) {
        return false;
    }
  }
  return true;
}

function showData(index)
{
  try { parent.parent.match_details.close(); } catch (ignore) {}
  var o = a_segments[index];
  idLabel.innerHTML = o.label;
  sourceName.innerHTML = "<%=lb_SourceName%>";
  idSourceText.innerHTML = o.contentSource;
  targetName.innerHTML = "<%=lb_TargetName%>";
  idText.innerHTML  = o.contentTarget?o.contentTarget:o.text;
  
  var sourceCell = parent.source.document.getElementById("idSourceCell");
  if (sourceCell) {
    markDiff(idSourceText.innerHTML, sourceCell.innerHTML, idSourceText);
    //markDiff(sourceCell.innerHTML, idSourceText.innerHTML, sourceCell);
  }
  

  if (o.lang)
  {
    idText.lang = o.lang;
  }
  if (o.dir)
  {
    idText.dir = o.dir;
  }

  if(o.srcDir)
  {
	  idSourceText.dir = o.srcDir;
  }

  if (index == 0)
  {
    idMatchesPrev.style.visibility = 'hidden';
  }
  else
  {
    idMatchesPrev.style.visibility = 'visible';
  }

  if (index == a_segments.length - 1)
  {
    idMatchesNext.style.visibility = 'hidden';
  }
  else
  {
    idMatchesNext.style.visibility = 'visible';
  }
  
  creationDate.value=o.creationDate;
  creationUser.value=o.creationUser;
  modifyDate.value=o.modifyDate;
  modifyUser.value=o.modifyUser;
  matchedTuvJobName.value=o.matchedTuvJobName;
  tmName.value=o.tmName;
  sid.value=o.sid;
  matchType.value=o.matchType;
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

function convertMatches()
{
 try
 {
	for(var i = 0; i < (a_segments.length);i++)
    {
	 var match = a_segments[i];
	 //convert matches, tags to [X1]...
     var format = g_ptagsverbose ? "<%=EditorConstants.PTAGS_VERBOSE%>" :
        "<%=EditorConstants.PTAGS_COMPACT%>";

     if(parent.parent.GetPTagString(match.matchedSource,format))
     {   
       match.contentSource = parent.parent.GetPTagString(match.matchedSource,format);
     }
     else
     {
	    //if matched source is ""(for MT, Xliff..), add source content to this matched source 
        match.contentSource = parent.parent.GetPTagString(parent.parent.source_segment,format);
     }

     match.contentTarget = parent.parent.GetPTagString(match.data, format);
    }
 }
 finally
 {
	 parent.parent.EndGetPTagStrings();
 }
}
function showMatchdetailInfo()
{
    parent.parent.match_details = window.open("envoy/edit/online/se_match_details.jsp","MatchDetail","resizable,scrollbars=yes,width=400,height=400");
}
</SCRIPT>
</HEAD>
<BODY VLINK="#0000FF">
<HR COLOR="#0C1476" WIDTH="95%">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" width="95%">
<TR>
  <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="5" HEIGHT="1"></TD>
  <TD>
    <SPAN CLASS="standardTextBold"><%=lb_matchResults%></SPAN>
    <SPAN CLASS="standardText"><%=lb_clickToCopy%></SPAN>
  </TD>
</TR>
</TABLE>
<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="95%">
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="10" HEIGHT="1"></TD>
<%
if (tmMatches != null && tmMatches.size() > 0)
{
%>
    <TD VALIGN="TOP" ALIGN="LEFT">
      <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 ALIGN="LEFT">
			<TR VALIGN=TOP>
			  <TD width="8px">
				<IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idMatchesPrev"
				class="clickable" onclick="goLeft();return false;">
			  </TD>
			  <TD nowrap width="1%">
				<SPAN id="idLabel" class="label" onclick="return false;"></SPAN>
			  </TD>
			  <TD>
				<IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idMatchesNext"
				  class="clickable" onclick="goRight();return false;">
			  </TD>
			  <TD VALIGN="TOP" align="right" class="standardTextBold" >
			  <!-- <span class="label"><%=bundle.getString("lb_sid")%>: </span>
			   <SPAN id="idSID" class="standardText"></SPAN>
			  -->
			  <SPAN class="link" TITLE="Click to see match detail info" 
			   onclick="showMatchdetailInfo()" oncontextmenu="showMatchdetailInfo()">
			   <font style="text-decoration:underline;"><%=lb_details%></font>
			  </SPAN>
			  </TD>
			</TR>
			<TR>
			  <TD></TD>
			  <TD COLSPAN=3>
				  <TABLE>
					  <TR>
						<TD id="sourceName" class="label" valign = "top" width="50px"></TD>
						<TD id="idSourceText" class="source"></TD>
					  </TR>
					  <TR>
						<TD id="targetName" class="label" valign = "top"></TD>
						<TD id="idText" class="clickable" onclick="doClick()"></TD>
					  </TR>
				  </TABLE>
			  </TD>
			</TR>
    </TABLE>
    </TD>
<%
}
else
{
%>
    <TD VALIGN="TOP" ALIGN="LEFT" WIDTH="100%"
      class="standardTextItalic"><%=lb_noSegments%>
    </TD>
<%
}
%>
  </TR>
</TABLE>
<!-- Used for save match details info -->
<input type= 'hidden' id='creationDate'/> 
<input type= 'hidden' id='creationUser'/> 
<input type= 'hidden' id='modifyDate'/> 
<input type= 'hidden' id='modifyUser'/> 
<input type= 'hidden' id='matchedTuvJobName'/> 
<input type= 'hidden' id='tmName'/> 
<input type= 'hidden' id='sid'/>
<input type= 'hidden' id='matchType'/>
</BODY>
</HTML>
