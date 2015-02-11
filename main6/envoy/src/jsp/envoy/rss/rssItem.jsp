<%@page import="com.globalsight.everest.webapp.pagehandler.rss.RSSUtil"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.rss.Feed,
            com.globalsight.everest.webapp.pagehandler.rss.Item,
            com.globalsight.everest.webapp.pagehandler.rss.RSSConstants,
            java.util.ResourceBundle,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration"
    session="true"%>
<jsp:useBean id="tosave" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="toback" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/> 
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    
	SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	
	String returnMsg = (String)sessionMgr.getAttribute(RSSConstants.RETURN_MESSAGE);
	sessionMgr.setAttribute(RSSConstants.RETURN_MESSAGE, "");
	
	Feed feed = (Feed)sessionMgr.getAttribute(RSSConstants.FEED);
	
	//All rss feeds that have been subscribed (List:Feed)
	List allItems = (List) sessionMgr.getAttribute(RSSConstants.ALL_ITEM);
	int[] pageParams = (int[])sessionMgr.getAttribute("Page_Params");
	
    String url_toSave = tosave.getPageURL();
    String url_toBack = toback.getPageURL();
    String url_refresh = refresh.getPageURL();
%>

<HTML>

<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE>RSS Viewer</TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "Rss Reader";
var helpFile = "<%=bundle.getString("help_rss_item")%>";

function submitForm(button)
{
	if (button == "refresh") {
		form1.action = "<%=url_refresh%>&action=refresh";
		form1.submit();
	}
    return;
}

function createXmlHttpRequest(){   
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	}
}   
   
function afterMarkRead(){   
    if(xmlHttpRequest.readyState == 4 && xmlHttpRequest.status == 200){   
        var b = xmlHttpRequest.responseText;
		b = trim(b);
        if (b != "") {
            var item = "title_" + b;
            document.getElementById(item).style.fontWeight = "normal";
        }
    }   
}  

function  trim(str){
    var len = str.length;
    var result = "";
    for (var i=0;i<len;i++) {
        if (str.charAt(i) >= "0" && str.charAt(i) <= "9")
            result += str.charAt(i);
    }
    return result;
} 

function markRead(itemId) {
	var url = "/globalsight/envoy/rss/ajaxMarkRead.jsp?id=" + itemId;
    xmlHttpRequest = createXmlHttpRequest();   
    xmlHttpRequest.onreadystatechange = afterMarkRead;   
    xmlHttpRequest.open("GET",url,true);   
    xmlHttpRequest.send(null);     
}

function openOrHide(itemId)
{
    var item = document.getElementById(itemId);
	var pitem = document.getElementById("p" + itemId);
    if (item.style.display == 'none') {
      item.style.display = 'block';
	  pitem.style.borderStyle = "solid";
	  pitem.style.borderWidth = "2px";
	  pitem.style.borderColor = "#99CCFF";
      markRead(itemId);
    } else {
    	item.style.display = 'none';
		pitem.style.borderStyle = "none";
	    pitem.style.borderWidth = "0px";
	    pitem.style.borderColor = "#000000";
    }

}

function toTranslate(itemId) {
    if (confirm("<%=bundle.getString("jsmsg_rss_confirm") %>")) {
		form1.action = "<%=url_toSave%>&action=translate&itemid=" + itemId;
		form1.submit();
	}
}

function toBack() {
	form1.action = "<%=url_toBack%>";
	form1.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">


<form name="form1" method="post">
<input type="hidden" name="channelId" id="channelId" value="<%=feed.getId()%>" />
<div class="mainHeading">
<%=bundle.getString("lb_rss_channel_url") %> : <%=feed.getChannelTitle() %>
</div>
<br/>
   <amb:permission name="<%=Permission.RSS_READER%>" >
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_back")%>"
      name="backBtn" onclick="toBack();">
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
      name="refreshBtn" onclick="submitForm('refresh');">
   </amb:permission>
<br>
<!-- Show items -->
<div align="left" style="width:90%;">
<div align="left" style="width:80%;background-color:#fff;">

<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
<%
int currentPageNum = pageParams[2];
%>
Displaying <B><%=pageParams[3] %> - <%=pageParams[4] %></B> of <B><%=pageParams[0]%></B><BR>
<%
if (currentPageNum == 1) {
%>
<SPAN CLASS=standardTextGray>First</SPAN> | 
<SPAN CLASS=standardTextGray>Previous</SPAN> |
<%
} else {
%>
<A CLASS=standardHREF HREF='<%=url_refresh %>&page=1'>First</A> | 
<A CLASS=standardHREF HREF='<%=url_refresh %>&page=<%=currentPageNum-1 %>'>Previous</A> |
<%
}
for (int i=1;i<=pageParams[1];i++) {
	if (i == currentPageNum) {
		%>
		<SPAN CLASS=standardTextGray><%=i %></SPAN>&nbsp;
		<%
	} else {
		%>
		<A CLASS=standardHREF HREF='<%=url_refresh %>&page=<%=i %>'><%=i %></A>&nbsp;
		<%
	}
}
%> 
| &nbsp;
<%
if (currentPageNum == pageParams[1]) {
%>
<SPAN CLASS=standardTextGray>Next</SPAN> | 
<SPAN CLASS=standardTextGray>Last</SPAN>
<%
} else {
%>
<A CLASS=standardHREF HREF='<%=url_refresh %>&page=<%=currentPageNum+1 %>'>Next</A> | 
<A CLASS=standardHREF HREF='<%=url_refresh %>&page=<%=pageParams[1] %>'>Last</A>
<%
}
%>   
</DIV>

<%
int itemCount = allItems.size();
Item item = null;
String title, description, author, pubDate, link;
long itemId = 0;
int status = 0;
String bgcolor = "#99CCFF";
for (int i = 0; i < itemCount; i++) {
	item = (Item)allItems.get(i);
	itemId = item.getId();
	title = RSSUtil.convertToHtml(item.getTitle());
	description = item.getDescription();
	author = item.getAuthor();
	pubDate = item.getPubDate();
	link = item.getLink();
	status = item.getStatus();
	if (i % 2 == 0)
		bgcolor = "#EEEEEE";
	else
		bgcolor = "#FFFFFF";
	%>
	<div id="p<%=itemId%>" style="background-color:<%=bgcolor%>;margin-top:2px;">
		<a title="Open source website" onclick="this.href='<%=link %>';document.getElementById('<%=itemId%>').style.display='none';" href="<%=link %>" target="_blank" class="standardText" style="text-decoration:underline;">
			<span id="title_<%=itemId%>" style="font-weight:<%= item.getIsRead() == 0 ? "bold" : "normal"%>;"><%=title.length()<75 ? title : title.substring(0, 75) + "..." %></span>
		</a>
		<span onclick="openOrHide('<%=itemId %>');" style="cursor:pointer;" title="Click here to open or hide the item">
		  <div align="right">
			<span class="standardText" title="<%=title %>" style="width:100%;height:15px;">
				<%=pubDate%>
			</span>
		  </div>
		</span>
	  <div id="<%=itemId %>" style="display:none">
	    <div class="standardText"><b>Author: <%=author%></b></div><br>
	    <div style="width:100%;font-size:9pt;height:300px;overflow:auto;"><%=description%></div><br>
	    <div align="right" style="margin-left:10px;margin-bottom:2px;" class="standardText">
	    <!-- Status:<b><%=(status==0)?"Untranslated":bundle.getString("lb_rss_in_translation") %></b>  -->
		&nbsp;&nbsp;<a href="javascript:toTranslate('<%=itemId%>');" class="standardText" title="Translate the entry in GlobalSight"><b><%=bundle.getString("lb_rss_to_translate") %></b></a>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		</div>
	  </div>
	</div> 
	<%
}
%>
</div>
</div>

<!-- End of show items -->
<br>
   <amb:permission name="<%=Permission.RSS_READER%>" >
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_back")%>"
      name="backBtn" onclick="toBack();">
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
      name="refreshBtn" onclick="submitForm('refresh');">
   </amb:permission>
</form>
</DIV>

<script language="javascript">
<%
if (returnMsg != null && !returnMsg.equals(""))
	out.print("alert('" + returnMsg + "');");
%>
</script>
</BODY>
</HTML>
