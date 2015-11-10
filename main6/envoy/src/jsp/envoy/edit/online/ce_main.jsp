<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.comment.Issue,
            com.globalsight.everest.comment.IssueHistory,
            com.globalsight.everest.comment.IssueOptions,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper,
            com.globalsight.everest.foundation.User,
            com.globalsight.util.gxml.GxmlElement,
            com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            com.globalsight.everest.edit.online.SegmentView,
            java.io.File,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="imageUploader" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>

<%
    ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();
String str_userName =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserName();

CommentView view =
  (CommentView)sessionMgr.getAttribute(WebAppConstants.COMMENTVIEW);

String url_self = self.getPageURL();

long tuId  = view.getTuId();
long tuvId = view.getTuvId();
long subId = view.getSubId();

Issue issue = view.getComment();
boolean b_create = (issue == null);
boolean b_edit = false;
boolean b_editSegmentTitle = true;

String cmtTitle = "";
String cmtStatus = "";
String cmtPriority = "";
String cmtCategory = "";
String cmtCreatorUserName = "";
String cmtComment = "";
List histories = null;
String share = "";
String overwrite = "disabled";

if (issue != null)
{
    cmtTitle = issue.getTitle();
    cmtStatus   = issue.getStatus();
    cmtPriority = issue.getPriority();
    cmtCategory = issue.getCategory();
    cmtCreatorUserName  = UserUtil.getUserNameById(issue.getCreatorId());
    histories   = issue.getHistory();
    
    if (issue.isOverwrite())
    {
        overwrite = "checked";
    }
    else
    {
        overwrite = "";
    }
    
    if (issue.isShare())
    {
        share = "checked";
    }
    else
    {
        overwrite += " disabled";
    }

    IssueHistory last = (IssueHistory)histories.get(0);
    if (str_userId.equals(last.reportedBy()))
    {
        // User can edit the last comment he's entered
        b_edit = true;
        cmtComment = last.getComment();
    }

    //user cannot edit the title unless he reported it
    IssueHistory first = (IssueHistory)histories.get(histories.size() - 1);
    if (str_userId.equals(first.reportedBy()) == false)
    {
        b_editSegmentTitle = false;
    }
}

    if(request.getAttribute("uploaderTitle")!=null) {
       cmtTitle = (String)request.getAttribute("uploaderTitle");
    }

    if(request.getAttribute("uploaderStatus")!=null) {
       cmtStatus = (String)request.getAttribute("uploaderStatus");
    }
    
    if(request.getAttribute("uploaderPriority")!=null) {
       cmtPriority = (String)request.getAttribute("uploaderPriority");
    }
    
    if(request.getAttribute("uploaderCategory")!=null) {
       cmtCategory = (String)request.getAttribute("uploaderCategory");
    }
    
    if(request.getAttribute("uploaderComment")!=null) {
        cmtComment = (String)request.getAttribute("uploaderComment");
    }
 
String lb_heading = b_create ? bundle.getString("lb_editor_create_segment_comment") :
   b_edit ? bundle.getString("lb_editor_edit_segment_comment") : bundle.getString("lb_editor_add_segment_comment");
String lb_saveTheChanges = bundle.getString("jsmsg_editor_pls_save_comment");

String uploadUrl = imageUploader.getPageURL() + "&commentUpload=true";

SegmentView segmentView = state.getEditorManager().getSegmentView(
            tuId, tuvId, "" + subId, state.getTargetPageId(),
            state.getSourceLocale().getId(),
            state.getTargetLocale().getId(), state.getTmNames(),
            state.getDefaultTermbaseName());

boolean b_rtlLocale = EditUtil.isRTLLocale(state.getTargetLocale());

GxmlElement srcGxml = segmentView.getSourceSegment();
GxmlElement tgtGxml = segmentView.getTargetSegment();

String sourceSegment = srcGxml.getTextValue();
String targetSegment = tgtGxml.getTextValue();
String str_sid = segmentView.getTargetTuv().getSid();
if (str_sid == null || str_sid.trim().length()==0)
{
    str_sid = "N/A";
} 

OnlineTagHelper applet = new OnlineTagHelper();
try
{
    String srcseg = GxmlUtil.getInnerXml(srcGxml);
    String tgtseg = GxmlUtil.getInnerXml(tgtGxml);
    applet.setDataType(segmentView.getDataType());
    applet.setInputSegment(srcseg, "", segmentView.getDataType());
    if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
    {
        applet.getVerbose();
        srcseg = applet.makeVerboseColoredPtags(srcseg);
    }
    else
    {
        applet.getCompact();
        srcseg = applet.makeCompactColoredPtags(srcseg);
    }
    
    applet.setInputSegment(tgtseg, "", segmentView.getDataType());
    if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
    {
        applet.getVerbose();
        tgtseg = applet.makeVerboseColoredPtags(tgtseg);
    }
    else
    {
        applet.getCompact();
        tgtseg = applet.makeCompactColoredPtags(tgtseg);
    }
    
    sourceSegment = srcseg;
    targetSegment = tgtseg;
    
}
catch (Exception e)
{
    // ignore this exceptiopn
}


%>
<!-- This JSP is: envoy/edit/online/ce_main.jsp -->
<html>
<head>
<title><%=lb_heading%></title>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Ajax.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<style>

@import url(/globalsight/dijit/themes/tundra/attribute.css);

BODY, #idTable, #idTable2 { font-family: verdana; font-size: 10pt; }
BODY { margin: 0; }
#idToolbar { padding: 10px 10px 10px 10px; font-family: verdana; font-size: 9pt;  }
#idHelp { float: right; font-size: 8pt; cursor: hand; cursor:pointer; text-decoration: underline; }
#idTable { margin-left: 10px; margin-right: 10px; }
#idTitle, #idComment { width: 394px; }
#idOldComments { overflow: auto; height: 90px; width: 394px;
                 border: 1px solid black;
               }
.clickable  { cursor: hand; cursor:pointer; }
.label      { font-weight: bold; }
#idOldComments .commentBy   { font-weight: bold; }
#idOldComments .commentDate { font-style: italic; font-size: smaller; }
#idOldComments .comment     { margin-left: 20px; word-wrap: break-word; }
.lineStyle {margin-top:5px; margin-bottom: -10px; width:500px; text-align:right; border-top:1px solid grey}
</style>
<script>
var EditorType = 'comment';

var g_tuId  = "<%=tuId%>";
var g_tuvId = "<%=tuvId%>";
var g_subId = "<%=subId%>";
var g_currentUserName = "<%=str_userName%>";

var g_cmt_title    = "<%=EditUtil.toJavascript(cmtTitle)%>";
var g_cmt_status   = "<%=cmtStatus%>";
var g_cmt_priority = "<%=cmtPriority%>";
var g_cmt_category = "<%=cmtCategory%>";
var g_cmt_creatorUserName  = "<%=EditUtil.toJavascript(cmtCreatorUserName)%>";
var g_cmt_comment  = "<%=EditUtil.toJavascript(cmtComment)%>";

var g_create = eval("<%=b_create%>");
var g_edit   = eval("<%=b_edit%>");

var g_dirty = false;

var helpFile = "<%=bundle.getString("help_create_edit_segment_comments")%>";

function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function CanClose()
{
    return !g_dirty;
}

function setDirty()
{
    g_dirty = true;
}

function RaiseEditor()
{
    window.focus();
    idOk.focus();
    alert("<%=EditUtil.toJavascript(lb_saveTheChanges)%>");
}

function syncMainEditor()
{
    try
    {
        window.opener.HighlightSegment(g_tuId, g_tuvId, g_subId);
    }
    catch (ignore) {}
}

function updateState()
{
	dojo.byId("overwrite").disabled = !dojo.byId("share").checked;
}

function doOk()
{
    if (Trim(document.getElementById("idTitle").value) == '')
    {
      // alert("<%=bundle.getString("jsmsg_editor_enter_title") %>");
       idTitle.value = 'Empty Title';
       //idTitle.focus();
       //return;
    }

    if (Trim(document.getElementById("idComment").value) == '')
    {
       alert("<%=bundle.getString("jsmsg_editor_enter_comment") %>");
       idComment.value = '';
       idComment.focus();
       return;
    }

    if (getUTF8Len(document.getElementById("idTitle").value) >= 200)
    {
       alert("<%=bundle.getString("jsmsg_editor_title_too_long") %>");
       idTitle.value = truncateUTF8Len(idTitle.value, 200);
       idTitle.focus();
       return;
    }

    if (getUTF8Len(document.getElementById("idComment").value) >= 4000)
    {
       alert("<%=bundle.getString("jsmsg_editor_comment_too_long") %>");
       idComment.value = truncateUTF8Len(idComment.value, 4000);
       idComment.focus();
       return;
    }

    var action, title, comment, priority, status;

    if (g_create)
    {
        action = 'create';
    }
    else if (g_edit)
    {
        action = 'edit';
    }
    else
    {
        action = 'add';
    }

    title = document.getElementById("idTitle").value;
    comment = document.getElementById("idComment").value;
    priority = getSelectedValue(document.getElementById("idPriority"));
    status = getSelectedValue(document.getElementById("idStatus"));
    category = getSelectedValue(document.getElementById("idCategory"));

    var rootWindow =  window.opener.parent.parent.parent.frames["review"];
    if (rootWindow)
    {
        rootWindow = window.opener;
    }
    var share = document.getElementById("share").checked;
    var overwrite = document.getElementById("overwrite").checked;
   // var main=rootWindow.parent.parent.parent.parent;
   	//main.localData=null;
	try {rootWindow.SaveComment2(g_tuId, g_tuvId, g_subId, action, title, comment, priority, status, category, share, overwrite);
 } catch (ex) {  window.opener.SaveComment2(g_tuId, g_tuvId, g_subId,  action, title, comment, priority, status, category, share, overwrite);
 }

    //rootWindow.SaveComment2(g_tuId, g_tuvId, g_subId,
      //action, title, comment, priority, status, category, share, overwrite);

    window.close();
}

function doCancel()
{
    window.close();
}

function doOnUnload()
{
}

function selectValue(select, value)
{
    for (var i = 0; i < select.length; ++i)
    {
        if (select.options[i].value == value)
        {
            select.selectedIndex = i;
            return;
        }
    }
}
//Fix for GBS-2383, add no-existing option user uploaded
function selectCategory(select, value)
{
	for (var i = 0; i < select.length; ++i)
	{
		if (select.options[i].value == value)
		{
			select.selectedIndex = i;
			return;
		}
	}	
	select.options.add(new Option(value, value));
	select.selectedIndex = i;
}


function getSelectedValue(select)
{
    return select.options[select.selectedIndex].value;
}

function cancelEvent()
{
  event.returnValue = false;
  event.cancelBubble = true;
  return false;
}

function doKeypress(event)
{
  var key = event.keyCode;

  setDirty();

  if (key == 27) // Escape
  {
    doCancel();
  }
}

function doKeydown(event)
{
  var key = event.keyCode;

  setDirty();

  if (event.ctrlKey && key == 13) // ^enter
  {
    doOk();
    return cancelEvent();
  }
}

function doOnLoad()
{
    document.getElementById("idSegment").innerHTML = g_tuId;

    if (g_create)
    {
        document.getElementById("idUser").innerHTML = g_currentUserName;

        document.getElementById("idTitle").focus();
    }
    else
    {
        document.getElementById("idUser").innerHTML = g_cmt_creatorUserName;
    }
        selectValue(document.getElementById("idStatus"), g_cmt_status);
        selectValue(document.getElementById("idPriority"), g_cmt_priority);
        selectCategory(document.getElementById("idCategory"), g_cmt_category);
        document.getElementById("idTitle").value = g_cmt_title;

        if (g_edit || g_create)
        {
            document.getElementById("idComment").value = g_cmt_comment;
        }

        document.getElementById("idComment").focus();
        document.getElementById("idComment").select();


    syncMainEditor();

    g_dirty = false;
}
</script>
</head>
<body onload="doOnLoad()" onunload="doOnUnload()"
 onkeypress="doKeypress(event)" onkeydown="doKeydown(event)">
<div id="idToolbar" class="tableHeadingBasic">
<span id="idHelp" onclick="helpSwitch()"><%=bundle.getString("lb_help") %></span>
<%=lb_heading%>
</div>    

<table id="idTable" width="100%" border=0  style="border-collapse: collapse;">

  <tr>
    <td colspan="2">
      <table id="idTable2" cellpadding="3" cellspacing="0">
	<tr>
	  <td>
	    <span class="label"><%=bundle.getString("lb_segment") %>:</span> <span id="idSegment"></span>
	  </td>
	  <td>&nbsp;</td>
	  <td>
	    <span class="label"><%=bundle.getString("lb_created_by") %>:</span> <span id="idUser"></span>
	  </td>
	</tr>
	<tr>
      <td colspan="3">
         <div class = "lineStyle">
            &nbsp;
         </div>
      </td>
   </tr>
	<tr>
	  <td>
	    <span class="label"><%=bundle.getString("lb_status") %>:</span>
	    <select id="idStatus" name="idStatus" onchange="setDirty()">
<%
          List statusList = IssueOptions.getAllStatus();
          for (int i = 0 ; i < statusList.size() ; i++)
          {
              String status = (String)statusList.get(i);
%>
            <option value="<%=status%>">
                    <%=bundle.getString("issue.status." + status)%></option>
<%
          }
%>
	    </select>
	  </td>

	  <td>&nbsp;</td>
	  <td>
	    <span class="label"><%=bundle.getString("lb_priority") %>:</span>
	    <select id="idPriority" name="idPriority" onchange="setDirty()">
        
<%
          Map priorities = IssueOptions.getAllPriorities();
          for (int j = 0 ; j < priorities.size() ; j++) 
          {
                // get them in order
                String priority = (String)priorities.get(new Integer(j+1));
                if (Issue.PRI_MEDIUM.equals(priority))
                {
%>                  <option value="<%=priority%>" selected>
<%
                }
                else 
                {
%>                
                    <option value="<%=priority%>">
<%
                }
%>
                <%=bundle.getString("issue.priority." +priority)%></option>
<%
          }
%>
                   
	    </select>
	  </td>
	</tr>
	<tr class="row1">		 
	  <td  colspan="3" >
	  <span class="label"><%=bundle.getString("lb_category") %>:</span>
	    <select id="idCategory" name="idCategory" onchange="setDirty()" style="width:300">
			<c:forEach var="op" items="${toList}">
				<option title="${op.value}" value="${op.key}">${op.value}</option>
			</c:forEach>
	    </select>
	  </td>
	</tr>
	<tr>		 
	  <td  colspan="2" >
	     <span class="label"><%=bundle.getString("lb_shareOtherLocales")%> :</span>
	  </td>
     <td>
	     <input type="checkbox" id="share" name="shareOtherLocales" value="true" <%=share%> onclick="updateState()">
	  </td>
	</tr>
	<tr class="row1">		 
	  <td  colspan="2" >
	     <span class="label"><%=bundle.getString("lb_overwriteExistComment")%> :</span>
     </td>
     <td>
	     <input type="checkbox" id="overwrite" name="overwriteExistComment" value="true" <%=overwrite%>>
	  </td>
	</tr>
      </table>  
    </td>
  </tr>
  
  <tr >
      <td colspan="4">
         <div class = "lineStyle">
            &nbsp;
         </div>
      </td>
  </tr>
  <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_title") %>:</span></td>
    <td>
     <% if (b_editSegmentTitle) { %>
      <input id="idTitle" name="idTitle" type="text" size="60" MAXLENGTH="200"
      onchange="setDirty()">
      <% } else { %>
      <input id="idTitle" name="idTitle" type="hidden" size="60" MAXLENGTH="200" VALUE="<%=EditUtil.toJavascript(cmtTitle)%>">
      <i><%=cmtTitle%></i>
      <% } %>
    </td>
  </tr>
  <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_comment") %>:</span></td>
    <td>
      <textarea id="idComment" name="idComment" rows="4" cols="47" onchange="setDirty()"></textarea>
    </td>
  </tr>
  <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_source") %>:</span></td>
    <td class="standardtext" >
      <div style="width: 394px;max-height: 60px;overflow: auto; "><%=sourceSegment %></div>
    </td>
  </tr>
  <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_target") %>:</span></td>
   <% 
   	if(b_rtlLocale)
   	{
    %>
  	 <td class="standardtext">
      <div style="width: 394px;max-height: 60px;overflow: auto;" dir="rtl"><%=targetSegment %></div>
    </td>
    <%		
   	}
   	else
   	{
    %>
    <td class="standardtext">
      <div style="width: 394px;max-height: 60px;overflow: auto;"><%=targetSegment %></div>
    </td>
    <%
   	}
    %>
  </tr>
    <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_sid") %>:</span></td>
    <td class="standardtext">
      <div style="width: 394px;max-height: 60px;overflow: auto;"><%=str_sid%></div>
    </td>
  </tr>
  <tr>
    <td valign="top"><span class="label"><%=bundle.getString("lb_comment_log") %>:</span></td>
    <td width="100%">
      <div id="idOldComments" name="idOldComments">
<%
     if (histories != null)
     {
       for (int j = 0, maxj = histories.size(); j < maxj; j++)
       {
         IssueHistory history = (IssueHistory)histories.get(j);
%>
    <DIV>
    <SPAN class="commentBy"><%=EditUtil.encodeHtmlEntities(UserUtil.getUserNameById(history.reportedBy()))%></SPAN>
    <SPAN class="commentDate"><%=history.dateReported()%></SPAN>
    <DIV class="comment"><%=EditUtil.encodeHtmlEntities(history.getComment())%></DIV>
    </DIV>
<%
       }
     }
%>
      </div>
    </td>
  </tr>
  
  <tr >
  <td colSpan=4>
    <IFRAME ID=IFrame1 FRAMEBORDER=0 SCROLLING=NO SRC="<%=uploadUrl%>" width="100%" height="60"></IFRAME>
  <td>
  </tr>

  <tr >
      <td colspan="4">
         <div class = "lineStyle">
            &nbsp;
         </div>
      </td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <input type="button" id="idCancel" value=<%=bundle.getString("lb_cancel") %>
      onclick="doCancel()" tabindex="1">
      &nbsp;&nbsp;
      <input type="button" id="idOk" value="&nbsp;&nbsp;<%=bundle.getString("lb_ok") %>&nbsp;&nbsp;"
      onclick="doOk();" tabindex="2">
    </td>
  </tr>
  
</table>

</body>
</html>
