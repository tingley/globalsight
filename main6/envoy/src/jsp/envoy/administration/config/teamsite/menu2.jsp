<%@ page contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteBranchMainHandler,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
	    com.globalsight.everest.webapp.pagehandler.PageHandler, 
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new3a" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);

// bring in "state" from session
SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(
  WebAppConstants.SESSION_MANAGER);

String newURL = (String)sessionMgr.getAttribute(
  TeamSiteBranchMainHandler.NEW_URL);

String title = bundle.getString("lb_select_target_branch");

Vector TargetLocales = new Vector();
TargetLocales = (Vector)sessionMgr.getAttribute(
  TeamSiteBranchMainHandler.TARGET_LOCALES);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<HTML>
<HEAD>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE>The Joust Outliner - The Menu</TITLE>
<META NAME="ROBOTS" CONTENT="NOINDEX,NOFOLLOW">

<STYLE ID="JoustStyles" TYPE="text/css">
.menuItem {position:absolute; visibility:hidden; left:0px;}
.menuItem BR { clear: both; }
.node { color: black;
	font-family : "Helvetica", "Arial", "MS Sans Serif", sans-serif;
	font-size : 9pt;}
.node A:link { color: blue; text-decoration: underline; }
.node A:visited { color: blue; text-decoration: underline; }
.node A:active { color: red; text-decoration: underline; }
.node A:hover { color: blue; text-decoration: underline; }
.leaf { color: black;
	font-family : "Helvetica", "Arial", "MS Sans Serif", sans-serif;
	font-size : 9pt;}
.leaf A:link { color: blue; text-decoration: underline;}
.leaf A:visited { color: blue; text-decoration: underline; }
.leaf A:active { color: red; text-decoration: underline; }
.leaf A:hover { color: blue; text-decoration: underline; }
</STYLE>

<!--
Joust Outliner Version 2.5.3
(c) Copyright 1996-2001, MITEM (Europe) Ltd. All rights reserved.
This code may be freely copied and distributed provided that it is accompanied by this
header.  For full details of the Joust license, as well as documentation and help, go
to http://www.ivanpeters.com/.

Do not modify anything between here and the "End of Joust" marker unless you know what you
are doing.
-->
<script language="JavaScript">
<!--
var theMenuRef = "parent.theMenu";
var theMenu = eval(theMenuRef);
var theBrowser = parent.theBrowser;
var belowMenu = null;
var menuStart = 0;

if (parent.theBrowser) {
	if (parent.theBrowser.canOnError) {window.onerror = parent.defOnError;}
}

if (theMenu) {
	theMenu.amBusy = true;
	if (theBrowser.hasDHTML) {
		if (document.all) {
			with (document.styleSheets["JoustStyles"]) {
				addRule ("#menuTop", "position:absolute");
				addRule ("#menuBottom", "position:absolute");
				addRule ("#menuBottom", "visibility:hidden");
				addRule ("#statusMsgDiv", "position:absolute");
			}
		} else {
			if (document.layers) {
				document.ids.menuTop.position = "absolute";
				document.ids.menuBottom.position = "absolute";
				document.ids.menuBottom.visibility = "hidden";
				document.ids.statusMsgDiv.position = "absolute";
			} else {
				if (theBrowser.hasW3CDOM) {
					var styleSheetElement = document.styleSheets[0];
    				var styleSheetLength = styleSheetElement.cssRules.length;
					styleSheetElement.insertRule("#menuTop { position:absolute } ", styleSheetLength++);
					styleSheetElement.insertRule("#menuBottom { position:absolute } ", styleSheetLength++);
					styleSheetElement.insertRule("#menuBottom { visibility:hidden } ", styleSheetLength++);
					styleSheetElement.insertRule("#statusMsgDiv { position:absolute } ", styleSheetLength++);
				}
			}
		}
	}
}
function getDHTMLObj(objName) {
	if (theBrowser.hasW3CDOM) {
		return document.getElementById(objName).style;
	} else {
		return eval('document' + theBrowser.DHTMLRange + '.' + objName + theBrowser.DHTMLStyleObj);
	}
}
function getDHTMLObjHeight(objName) {
	if (theBrowser.hasW3CDOM) {
		return document.getElementById(objName).offsetHeight;
	} else {
		return eval('document' + theBrowser.DHTMLRange + '.' + objName + theBrowser.DHTMLDivHeight);
	}
}
function myVoid() { ; }
function setMenuHeight(theHeight) {
	getDHTMLObj('menuBottom').top = theHeight;
}
function drawStatusMsg() {
	if (document.layers) {
		document.ids.statusMsgDiv.top = menuStart;
	} else{
		if (document.all) {
			document.styleSheets["JoustStyles"].addRule ("#statusMsgDiv", "top:" + menuStart);
		}
	}
	document.writeln('<DIV ID="statusMsgDiv"><CENTER>Re-building Menu...</CENTER></DIV>');
}
function drawLimitMarker() {
	var b = theBrowser;
	if (theMenu && b.hasDHTML && b.needLM) {
		var limitPos = theMenu.maxHeight + menuStart + getDHTMLObjHeight('menuBottom');
		if (b.code == 'NS') {
			document.ids.limitMarker.position = "absolute";
			document.ids.limitMarker.visibility = "hidden";
			document.ids.limitMarker.top = limitPos;
		}
		if (b.code == 'MSIE') {
			with (document.styleSheets["JoustStyles"]) {
				addRule ("#limitMarker", "position:absolute");
				addRule ("#limitMarker", "visibility:hidden");
				addRule ("#limitMarker", "top:" + limitPos + "px");
			}
		}
		document.writeln('<DIV ID="limitMarker">&nbsp;</DIV>');
	}
}
function setTop() {
	if (theMenu && theBrowser.hasDHTML) {
		if (getDHTMLObj('menuTop')) {
			drawStatusMsg();
			menuStart = getDHTMLObjHeight("menuTop");
		} else {
			theBrowser.hasDHTML = false;
		}
	}
}
function setBottom() {
	if (theMenu) {
		if (theBrowser.hasDHTML) {
			var mb = getDHTMLObj('menuBottom');
			if (mb) {
				drawLimitMarker();
				getDHTMLObj("statusMsgDiv").visibility = 'hidden';
				menuStart = getDHTMLObjHeight("menuTop");
				theMenu.refreshDHTML();
				if (theMenu.autoScrolling) {theMenu.scrollTo(theMenu.lastPMClicked);}
				mb.visibility = 'visible';
			} else {
				theBrowser.hasDHTML = false;
				self.location.reload();
			}
		}
		theMenu.amBusy = false;
	}
}

function frameResized() {if (theBrowser.hasDHTML) {theMenu.refreshDHTML();}}

//	############################   End of Joust   ############################

//if (self.name != 'menu') { self.location.href = 'index.htm'; }
//-->
</script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
function getElement()
{
	if (document.layers)
	{
		theElement = document.fileListForm.<%= TeamSiteBranchMainHandler.TEAMSITE_TARGET_LANGUAGE %>;
	}
	else
	{
		theElement = menuTop.all.<%= TeamSiteBranchMainHandler.TEAMSITE_TARGET_LANGUAGE %>;
	}
	return theElement;
}

function setSelection()
{
	parent.targetSelectionIndex = getElement().selectedIndex;
}

function resetSelection()
{
	getElement().selectedIndex = parent.targetSelectionIndex;
}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="20" BGCOLOR="#FFFFFF" LINK="#000000" onResize="frameResized();" onLoad="resetSelection();">
<DIV ID="menuTop">
<!-- Place anything you want to appear before the menu between these DIV tags. -->
<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_teamsite_target")%>
</TD>
</TR>
</TABLE>
<P>

<INPUT TYPE="BUTTON" NAME="Expand All" VALUE="Expand All" ONCLICK="parent.theMenu.openAll()"> 
<INPUT TYPE="BUTTON" NAME="Collapse All" VALUE="Collapse All" ONCLICK="parent.theMenu.closeAll()"> 

 <FORM NAME="fileListForm" ACTION="<%= newURL %>" TARGET="_parent" METHOD="post"><INPUT TYPE="HIDDEN" NAME="<%= TeamSiteBranchMainHandler.TEAMSITE_TARGET_BRANCH %>">
<P>
<SPAN CLASS="standardText"><%= bundle.getString("lb_target_locale")+bundle.getString("lb_colon")+" " %></SPAN>
<SPAN CLASS="standardText">
  <SELECT name="<%= TeamSiteBranchMainHandler.TEAMSITE_TARGET_LANGUAGE %>" onChange="setSelection();">
  <OPTION VALUE="-1"><%= bundle.getString("lb_choose") %></OPTION>
<%
    String locale_id;
    String locale_info;
    if (TargetLocales != null)
    {
        int size = TargetLocales.size();
        for (int i = 0; i < size; i += 2)
        {
            locale_id = (String)TargetLocales.elementAt(i);
            locale_info = (String)TargetLocales.elementAt(i+1);
%>
    <OPTION VALUE="<%= locale_id %>"><%= locale_info %></OPTION>
<%
        }
    }
%>
  </SELECT>
</SPAN>
</FORM>
</P>
</DIV>
<SCRIPT LANGUAGE="JavaScript">
<!--
setTop();
//-->
</SCRIPT>

<!-- Set up any font's, colours, etc. that should apply to the menu here -->
<SCRIPT LANGUAGE="JavaScript">
<!--
if (theMenu) {
	parent.DrawMenu(theMenu);
}
//-->
</SCRIPT>
<!-- Close any tags you set up for the menu here -->
<DIV ID="menuBottom">
<!-- Place anything you want to appear after the menu between these DIV tags. -->
</FORM>
</DIV>

<SCRIPT LANGUAGE="JavaScript">
<!--
setBottom();
//-->
</SCRIPT>
</BODY>
</HTML>
