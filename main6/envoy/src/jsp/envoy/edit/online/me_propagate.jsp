<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
String url_self = self.getPageURL();

ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
    WebAppConstants.SESSION_MANAGER);
EditorState state =
    (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String lb_title = bundle.getString("lb_automatic_propagate_options");

String tpId = (String) request.getAttribute("targetPageId");
String needRefreshOpener = (String) request.getAttribute("needRefreshOpener");
String previousTuScope = (String) request.getAttribute("tuScope");
String previousTuvScope = (String) request.getAttribute("tuvScope");
String previousPickup = (String) request.getAttribute("pickup");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery.progressbar.js"></script>

<SCRIPT>
$(document).ready(function() {
	if ('<%=needRefreshOpener%>' == 'Yes') {
		showHourglass(true);
		$("#propagateProgressBar").progressBar();
		$("#propagateProgressBar").show();
		setTimeout(udpatePropagateProgress, 500);
	}
});

var getPercentageURL = '<%=url_self%>' + "&action=getPercentage";
var percentage = 0;
function udpatePropagateProgress()
{
   	// On IE, it the url is no change, seems it will not execute the url really,
    // so add a fake parameter "percentage" to cheat it in IE.
	percentage += 1;
   	getPercentageURL = getPercentageURL + "&percentage=" + percentage;
   	$.post(getPercentageURL, function(data) {
   	    var dataObj = eval('(' + data + ')');
   	    var per = dataObj.propagatePercentage;
   	    $("#propagateProgressBar").progressBar(parseInt(per));
   	    if (per < 100) {
   	     	setTimeout(udpatePropagateProgress,1000);
   	    } else {
   	        // Enable buttons when propagate is done.
            showHourglass(false);
        }
    });
}

function showHourglass(flag)
{
	if (true == flag) {
        form.propagateBtn.disabled = true;
        form.closeBtn.disabled = true;
        idBody.style.cursor = "wait";
	} else {
		form.propagateBtn.disabled = false;
        form.closeBtn.disabled = false;
        idBody.style.cursor = "default";
	}
}

function updateMutiLocOption()
{
	if (document.getElementById('id_specifiedTus').checked) {
		document.getElementById('id_latest').disabled = true;
		document.getElementById('id_oldest').disabled = true;
	} else {
		document.getElementById('id_latest').disabled = false;
		document.getElementById('id_oldest').disabled = false;
	}
}

function checkForm()
{
    showHourglass(true);
    var url = '<%=url_self%>' + "&action=propagate";
    // Tu Scope
    if (document.getElementById('id_currentFile').checked){
    	url += "&tuScope=currentFile";
    }
//////Reserve these codes for later use.////////
//    else if (document.getElementById('id_specifiedTus').checked){
//    	url += "&tuScope=specifiedTus";
//      var specTuIds = document.getElementById("id_specifiedTusField").value;
//	  if (specTuIds == null || specTuIds.length == 0){
//	    alert("Specified TuIds can't be empty.");
//        return false;
//      }
//      var tuIdsArr = specTuIds.split(",");
//      for (i=0; i<tuIdsArr.length; i++){
//    	var tuId = tuIdsArr[i];
//    	if(isNaN(tuId) || parseInt(tuId) != tuId){
//		  alert("TuID is not a valid number: " + tuId);
//		  return false;
//		} else if (parseInt(tuId) < 1) {
//          alert("TuID should not less than 1: " + tuId);
//		  return false;
//		}
//   	  }
//    }
	else {
    	url += "&tuScope=allFiles";
    }
    
    // Tuv Scope
    if (document.getElementById("id_all").checked){
    	url += "&tuvScope=all";
    } else if (document.getElementById("id_localized").checked){
    	url += "&tuvScope=localizedOnly";
    } else {
    	url += "&tuvScope=unlocalizedOnly";
    }
    
    // Pick up
    if (document.getElementById("id_oldest").checked){
    	url += "&pickup=oldest";
    } else {
    	url += "&pickup=latest";
    }

    form.action = url;
	form.submit();
}

function closeThis()
{
	if ('Yes' == '<%=needRefreshOpener%>') {
        window.opener.refresh(0);
	}
    try { window.close(); } catch (e) {}
}

</SCRIPT>
</HEAD>
<BODY style="margin:1ex" id="idBody">
<SPAN CLASS="mainHeading"><%=lb_title%></SPAN>

<FORM ACTION="" METHOD="post" name="form">
  <input type="hidden" name="targetPageId" id="targetPageId" value="<%=tpId %>" />
<TABLE>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=bundle.getString("lb_propagate_tu_scope")%>:</SPAN></TD>
    <TD class="standardText">
    <%
      String tuScopeChecked = "";
      if (previousTuScope == null || "allFiles".equals(previousTuScope)) {
          tuScopeChecked = "checked";
      }
    %>
      <INPUT type="radio" name="tuScope" id="id_allFiles" <%=tuScopeChecked%> onclick="updateMutiLocOption();"/>
      <LABEL for="id_allFiles"><%=bundle.getString("lb_propagate_to_all_job_files")%></LABEL><BR>
    <%
      tuScopeChecked = "";
      if ("currentFile".equals(previousTuScope)) {
          tuScopeChecked = "checked";
      }
    %>
      <INPUT type="radio" name="tuScope" id="id_currentFile" <%=tuScopeChecked%> onclick="updateMutiLocOption();"/>
      <LABEL for="id_currentFile"><%=bundle.getString("lb_propagate_in_current_file")%></LABEL><BR>
    <%
      tuScopeChecked = "";
      if ("specifiedTus".equals(previousTuScope)) {
          tuScopeChecked = "checked";
      }
    %>
      <!-- 
      <INPUT type="radio" name="tuScope" id="id_specifiedTus" <%=tuScopeChecked%> onclick="updateMutiLocOption();";/>
      <LABEL for="id_specifiedTus"><%=bundle.getString("lb_propagate_specified_tus")%>:</LABEL>
      <INPUT type="text" size="25" maxlength="100" name="specifiedTusField" id="id_specifiedTusField" /><%=bundle.getString("lb_propagate_eg")%><BR>
       -->
    </TD>
  </TR>
  <TR><TD colspan="2">&nbsp;</TD></TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=bundle.getString("lb_propagate_tuv_scope")%>:</SPAN></TD>
    <TD class="standardText">
    <%
      String tuvScopeChecked = "";
      if (previousTuvScope == null || "all".equals(previousTuvScope)) {
          tuvScopeChecked = "checked";
      }
    %>
      <INPUT type="radio" name="tuvScope" id="id_all" <%=tuvScopeChecked%>/>
      <LABEL for="id_all"><%=bundle.getString("lb_propagate_all_segments")%></LABEL><BR>
    <%
      tuvScopeChecked = "";
      if ("unlocalizedOnly".equals(previousTuvScope)) {
          tuvScopeChecked = "checked";
      }
    %>      
      <INPUT type="radio" name="tuvScope" id="id_unlocalized" <%=tuvScopeChecked%>/>
      <LABEL for="id_unlocalized"><%=bundle.getString("lb_propagate_unlocalized_segments_only")%></LABEL><BR>
    <%
      tuvScopeChecked = "";
      if ("localizedOnly".equals(previousTuvScope)) {
          tuvScopeChecked = "checked";
      }
    %>
      <INPUT type="radio" name="tuvScope" id="id_localized" <%=tuvScopeChecked%>/>
      <LABEL for="id_localized"><%=bundle.getString("lb_propagate_localized_segments_only")%></LABEL>
    </TD>
  </TR>
  <TR><TD colspan="2">&nbsp;</TD></TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=bundle.getString("lb_multiple_localized_segments")%>:</SPAN></TD>
    <TD class="standardText">
    <%
      String pickupChecked = "";
      if (previousPickup == null || "latest".equals(previousPickup)) {
          pickupChecked = "checked";
      }
    %>
      <INPUT type="radio" name="multipleLocalizedSegments" id="id_latest" <%=pickupChecked %>/>
      <LABEL for="id_latest"><%=bundle.getString("lb_use_latest")%></LABEL><BR>
    <%
      pickupChecked = "";
      if ("oldest".equals(previousPickup)) {
          pickupChecked = "checked";
      }
    %>
      <INPUT type="radio" name="multipleLocalizedSegments" id="id_oldest" <%=pickupChecked %>/>
      <LABEL for="id_oldest"><%=bundle.getString("lb_use_oldest")%></LABEL><BR>
    </TD>
  </TR>
  <TR><TD colspan="2">&nbsp;</TD></TR>
  <TR>
    <TD colspan="2" align="center">
      <INPUT type="button" value="<%=bundle.getString("lb_propagate")%>" name="propagateBtn" 
        onclick="javascript:checkForm();"> &nbsp;&nbsp;&nbsp;
      <INPUT type="button" onclick="closeThis()" value="<%=bundle.getString("lb_close")%>" name="closeBtn">
    </TD>
  </TR>
  <TR><TD colspan="2">&nbsp;</TD></TR>
  <TR>
    <TD colspan="2" align="center">&nbsp;&nbsp; <span id="propagateProgressBar"></span></TD>
  </TR>
</TABLE>

</FORM>
</BODY>
</HTML>
