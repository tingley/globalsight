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
<jsp:useBean id="subscribe" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="showInRight" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/> 
<jsp:useBean id="showItems" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean"/> 
<jsp:useBean id="allFeeds" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_rss_reader");
    String helperText = bundle.getString("helper_text_rss_reader");
    String lb_add_channel = bundle.getString("lb_rss_channel_url");
    String lb_rss_subscribe = bundle.getString("lb_rss_subscribe");
    
	SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	//All rss feeds that have been subscribed (List:Feed)
	allFeeds = (ArrayList) sessionMgr.getAttribute(RSSConstants.ALL_FEED);
	String returnMsg = (String)sessionMgr.getAttribute(RSSConstants.RETURN_MESSAGE);
	sessionMgr.setAttribute(RSSConstants.RETURN_MESSAGE, "");
	
    String url_subscribe = subscribe.getPageURL();
    String url_showInRight = showInRight.getPageURL();
    String url_showItems = showItems.getPageURL();
%>

<HTML>

<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "Rss Reader";
var helpFile = "<%=bundle.getString("help_rss")%>";

function IsURL(str_url){ 
	 var strRegex = /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/  
//	    var re=new RegExp(strRegex);  
	 //re.test() 
	    if (strRegex.test(str_url)){ 
	          return true;  
	    }else{  
	          return false;  
	      } 
}

function submitForm(button)
{
	if (button == "subscribe") {
        var val = document.getElementById("rssUrlId").value;
        if (val == "") {
            return false;
        }
        if (!IsURL(val)) {
            alert("<%=bundle.getString("jsmsg_rss_invalidUrl")%>");
            return false;
        }
		
        addForm.action = "<%=url_subscribe%>&action=subscribe";
        addForm.submit();
		return true;		
	} else {
    	value = getRadioValue(addForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return false;
        }
        if (button == "unsubscribe") {
        	if (!confirm("<%=bundle.getString("jsmsg_rss_unsubscribe_confirm") %>"))
                return false;
            addForm.action = "<%=url_subscribe%>&action=unsubscribe&id=" + value;
        } else
            return false;
        addForm.submit();
        return true;
    }
    return false;
}

function getItems(rssUrl)
{
    if (rssUrl == null) {
        return;
    }

    var url = document.getElementById("idRssUrl");
    url.value = rssUrl;
    itemsForm.action = "<%=url_showInRight%>&action=showInRight";
    alert("itemsForm.action :" + itemsForm.action);
    
    itemsForm.submit();
    
    return;
}

function openOrHideDiv(itemId)
{
    var item = document.getElementById(itemId);
    if (item.style.display == 'none') {
      item.style.display = 'block';
    } else {
    	item.style.display = 'none';
    }

}

function enableButtons()
{
    if (addForm.unsubBtn)
        addForm.unsubBtn.disabled = false;
    if (addForm.refreshBtn)
        addForm.refreshBtn.disabled = false;
}

function showItems(id) {
	if (id != "") {
		addForm.action = "<%=url_showItems%>&channelId=" + id;
		addForm.submit();
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="addForm" method="post">
<div class="standardText"><%=lb_add_channel%>
  <INPUT TYPE="text" name="rssUrlName" id="rssUrlId" size="60" maxlength="140"/>&nbsp;&nbsp;
  <!-- 
  <input type="button" value="<%=bundle.getString("lb_search") %>" name="rssSearch" id="rssSearch" onclick="submitForm('search');"/>&nbsp;&nbsp;
   -->
  <INPUT TYPE="BUTTON" VALUE="<%=lb_rss_subscribe%>" onclick="submitForm('subscribe');"/>
  <input type="hidden" name="channelId" id="channelId"/>
</div><br/>

<table cellspacing="0" cellpadding="6" border="0" class="listborder" width="100%">
  <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
    <td align="center"></td>
    <td align="left"><b><%=bundle.getString("lb_title") %></b></td>
    <td align="left"><b><%=bundle.getString("lb_description") %></b></td>
    <td align="left"><b><%=bundle.getString("lb_rss_channel_url") %></b></td>
    <td align="left"><b><%=bundle.getString("lb_language") %></b></td>
  </tr>
  <%
  Feed feed = null;
  for (int i=0;i<allFeeds.size();i++) {
	  feed = (Feed)allFeeds.get(i);
	  %>
	  <tr class="<%=i%2 == 0 ? "tableRowOdd" : "tableRowEven" %>">
	    <td align="center" width="1%"><input type="radio" name="radioBtn" value="<%=feed.getId() %>" onclick="enableButtons();"/></td>
        <td class="standardText" width="30%"><a href="#" onclick="showItems('<%=feed.getId() %>');"><%=feed.getChannelTitle() %></a></td>
	    <td class="standardText" width="40%"><%=feed.getChannelDescription() %></td>
        <td class="standardText" width="20%"><%=feed.getRssUrl() %></td>
	    <td class="standardText" width="5%" align="center"><%=feed.getChannelLanguage() %></td>
	  </tr>
	  <%
  }
  %>
</table>
<br>
   <amb:permission name="<%=Permission.RSS_READER%>" >
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_rss_unsubscribe")%>"
      name="unsubBtn" onclick="submitForm('unsubscribe');" disabled>
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
