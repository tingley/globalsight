<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.text.MessageFormat,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.everest.foundation.User,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_title = bundle.getString("lb_term_input_model_editor");

String lb_initializing = bundle.getString("lb_initializing");
String lb_info = bundle.getString("lb_info");
String lb_help = bundle.getString("lb_help");
String lb_close_window = bundle.getString("lb_close_this_window");

/* This is a dialog receiving its input from the caller
-------------------------------------------------------
String str_type = (String)session.getAttribute(
  WebAppConstants.TERMBASE_OBJECT_TYPE);
String str_username = (String)session.getAttribute(
  WebAppConstants.TERMBASE_OBJECT_USER);
String str_name = (String)session.getAttribute(
  WebAppConstants.TERMBASE_OBJECT_NAME);
String str_value = (String)session.getAttribute(
  WebAppConstants.TERMBASE_OBJECT_VALUE);
*/

String str_tbname = (String)session.getAttribute(
  WebAppConstants.TERMBASE_TB_NAME);

boolean b_anonymous = true;
boolean b_admin = false;
if (PageHandler.getUser(session) != null)
{
  b_anonymous = false;
  // TODO: need to distinguish normal users and admins.
  b_admin = true;
}

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns:ie>
<head>
<TITLE><%=lb_title%></TITLE>
<STYLE type="text/css" MEDIA="all">
@import url("/globalsight/includes/coolbutton2.css");
@import url("/globalsight/envoy/terminology/inputmodels/editor.css");
</STYLE>
<script src="/globalsight/includes/library.js"></script>
<script src="/globalsight/includes/xmlextras.js"></script>
<script src="/globalsight/includes/coolbutton2.js"></script>
<script src="/globalsight/envoy/terminology/management/objects_js.jsp"></script>
<script src="/globalsight/envoy/terminology/viewer/error.js"></script>
<script LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/management/FireFox.js"></script>
<script src="/globalsight/envoy/terminology/viewer/entry.js" ></script>

<script src="/globalsight/envoy/terminology/viewer/SelectableFields.js"></script>
<script src="/globalsight/envoy/terminology/viewer/editor.js" ></script>
<script src="/globalsight/envoy/terminology/inputmodels/editor_js.jsp"></script>

<script>
var helpFileEditor = "<%=bundle.getString("help_termbase_create_inputmodel")%>";
window.onresize = resizeWindow;

function resizeWindow() {
    idEditor.style.width = document.body.clientWidth;
    idEditorMenu.style.left = 
        document.body.clientWidth - turnPXStringTInt(idEditorMenu.style.width);
    idEditorMenu.style.height = 
        document.body.clientHeight - turnPXStringTInt(idEditorHeader.style.height);
    idEditorArea.style.left = 0;
    idEditorArea.style.height = idEditorMenu.style.height;
    idEditorArea.style.width = idEditorMenu.style.left;
    idEditorEntry.style.height = 
        turnPXStringTInt(idEditorArea.style.height) - 
            turnPXStringTInt(idEditorEntryHeader.style.height);
}

function showHelpEditor()
{
    var helpWindow = window.open(helpFileEditor, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

var g_args;

function SetResultValue(value)
{
  var result = g_args;

  result.setName(idEntryName.value);
  result.setValue(value);

  window.returnValue = result;

  window.close();
}

function doLoad()
{
    resizeWindow()
    SetEditiorDefinition();
  // Dialog argument is a UserObject object.
  g_args = window.dialogArguments;

  idEntryName.value = g_args.name;

  if (g_args.value && g_args.value != "")
  {
    StartEditing(g_args.value);
  }
  else
  {
    StartEditing(null);
  }

  //document.recalc(true);
  window.focus();

  idEntryName.focus();
}

centerWindow(300,0);
</script>
</HEAD>

<body id="idBody" style="overflow: hidden"
  onload="doLoad()" onkeydown="isKeydown(event)">

<DIV id="idPreferences" class="preferences"></DIV>

<div id="idViewer" style="display:none;"
 BaseUrl="/globalsight/envoy/terminology/inputmodels" Termbase="<%=str_tbname%>"
 Anonymous="<%=b_anonymous%>" Admin="<%=b_admin%>"></div>

<div id="idEditor" style="position: absolute; top: 0; left: 0;
 height: 100%;background-color:#a6b8ce">

  <div id="idEditorHeader"
   style="overflow: visible; position: absolute;
   top: 0; left: 0; width: 100%; height: 35;
   border-bottom: 1px solid black;">
    <SPAN id="idCloseWindow" onclick="CancelEditing()" 
     style="float: right; margin-right: 7px; margin-top: 7px;"
     TITLE="<%=lb_close_window %>"><img src="/globalsight/images/close.gif" align="baseline"></SPAN>
    <SPAN id="idHelpEditor" onclick="showHelpEditor()" 
     style="float: right; margin-right: 7px; margin-top: 7px;cursor: pointer;font-size: 14;"><%=lb_help%></SPAN>
    <div style="margin-left: 7px; margin-top: 7px; margin-right: 7px;">
      <span class="mainHeading"><%=lb_title %></span>
      <span class="feedback"></span>
    </div>
  </div> <!-- idEditorHeader -->

  <div id="idEditorMenu" UNSELECTABLE="on"
   style="position: absolute; top: 36; width: 180; background-color: #a6b8ce;font-size: 14;">
    <div class="menuHeader"><%=bundle.getString("lb_editor_menu") %></div>

    <span id="idSaveEntry" class="menuItem" onclick="SaveEntry()"
     UNSELECTABLE="on"><%=bundle.getString("lb_editor_save_close") %></span><br>
    <span id="idCancelEditing" class="menuItem" onclick="CancelEditing()"
     UNSELECTABLE="on"><%=bundle.getString("lb_close") %></span>

    <hr width="90%">

    <span id="idAddLanguage" class="menuItem" onclick="AddLanguage()"
     UNSELECTABLE="on"><%=bundle.getString("lb_add_language") %>...</span><br>
    <span id="idRemoveLanguage" class="menuItem" onclick="RemoveLanguage()"
     UNSELECTABLE="on"><%=bundle.getString("lb_remove_language") %></span>

    <hr width="90%">

    <span id="idAddTerm" class="menuItem" onclick="AddTerm()"
     UNSELECTABLE="on"><%=bundle.getString("lb_add_synonym") %>...</span><br>
    <span id="idRemoveTerm" class="menuItem" onclick="RemoveTerm()"
     UNSELECTABLE="on"><%=bundle.getString("lb_remove_synonym") %></span>

    <hr width="90%">

    <div class="menuLabel" UNSELECTABLE="on"><%=bundle.getString("lb_add_field") %>:</div>
      <span id="idAddFieldTo" class="menuSubItem"
       onclick="AddFieldToCurrent()"
       UNSELECTABLE="on"><%=bundle.getString("lb_to_current_field") %>...</span><br>
      <span id="idAddFieldAfter" class="menuSubItem"
       onclick="AddFieldAfterCurrent()"
       UNSELECTABLE="on"><%=bundle.getString("lb_after_current_field") %>...</span><br>
    <span id="idEditField" class="menuItem" onclick="EditField()"
     UNSELECTABLE="on"><%=bundle.getString("lb_edit_field") %></span><br>
    <span id="idRemoveField" class="menuItem" onclick="RemoveField(event)"
     UNSELECTABLE="on"><%=bundle.getString("lb_remove_field") %></span>

    <hr width="90%">
  </div> <!-- idEditorMenu -->

  <div id="idEditorArea" onkeydown="isKeydown(event, this);" 
   style="overflow: auto; position: absolute; top: 36; left: 0;background-color: #f8eec9;font-size: 14;">
    <div id="idEditorEntryHeader" class="menuLabel"
     style="position: absolute; top: 0; left: 0; height: 25; width: 100%;
            padding-top: 3px;">
      <%=bundle.getString("lb_name") %>: <input type=text id="idEntryName"/>
    </div>

    <div id="idEditorEntry"
     style="overflow: auto; position: absolute; top: 25; width: 100%;"></div>

  </div> <!-- idEditorArea -->
</div> <!-- idEditor -->

</body>
</html>
