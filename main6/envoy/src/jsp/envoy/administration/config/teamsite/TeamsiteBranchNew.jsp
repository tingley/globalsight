<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
      com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteServerConstants,
      com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer,
      com.globalsight.cxe.entity.cms.teamsite.store.BackingStore,
      com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl,
      com.globalsight.everest.servlet.EnvoyServletException,
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.util.resourcebundle.ResourceBundleConstants,
      com.globalsight.util.resourcebundle.SystemResourceBundle,
      com.globalsight.util.edit.EditUtil,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="news" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
// Use this information to create a temporary directory.
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
String nextUrl = news.getPageURL() + "&"
                 + TeamSiteServerConstants.ACTION
                 + "=" + TeamSiteServerConstants.NEXT_ACTION;
String cancelUrl = cancel.getPageURL() + "&"
                 + TeamSiteServerConstants.ACTION
                 + "=" + TeamSiteServerConstants.CANCEL_ACTION;

String lb_title = bundle.getString("lb_select_teamsite_server_and_store");
String lb_help = bundle.getString("lb_help");
String lb_cancel = bundle.getString("lb_cancel");
String lb_next = bundle.getString("lb_next");
String lb_server = bundle.getString("lb_teamsite_server") +  "*" +
                   bundle.getString("lb_colon");
String lb_store = bundle.getString("lb_teamsite_content_store") + "*" +
                  bundle.getString("lb_colon");

// Let's get all the TeamSite Servers
Vector servers = null;
try
{
  servers = new Vector((Collection)ServerProxy
                    .getTeamSiteServerPersistenceManager()
                    .getAllTeamSiteServers());
}
catch(Exception e)
{
  throw new EnvoyServletException(e);
}

%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "profile";
var guideNode = "teamsiteBranches";
var helpFile = "<%=bundle.getString("help_teamsite_select_server")%>";

var groups = <%=servers != null ? servers.size() : 0%>;
var group = new Array(groups);
for (var i = 0; i < groups; i++)
{
  group[i] = new Array();
}

<%
if (servers != null)
{
   for (int i = 0; i < servers.size(); i++)
   {
       Vector stores = new Vector();
       TeamSiteServer tss = (TeamSiteServer)servers.get(i);
       stores = new Vector(tss.getBackingStoreIds());
       for(int j=0; j<stores.size(); j++)
       {
           Long l = (Long) stores.get(j);
           long bsId = l.longValue();
           BackingStore bs = (BackingStore)ServerProxy
                              .getTeamSiteServerPersistenceManager()
                              .readBackingStore(bsId);
           String bsName = bs.getName();
           out.print("group[");
           out.print(i);
           out.print("][");
           out.print(j);
           out.print("]=new Option('");
           out.print(EditUtil.toJavascript(bsName));
           out.print("','");
           out.print(bsId);
           out.print("');\n");
       }
   }
}
%>

var g_stores = null;

function doOnload()
{
  g_stores = document.serverstore.stores;

  loadGuides();
}

function redirect(x)
{
  for (var m = g_stores.options.length-1; m > 0; m--)
  {
      g_stores.options[m] = null;
  }

  for (var i = 0; i < group[x].length; i++)
  {
    g_stores.options[i] = new Option(group[x][i].text, group[x][i].value);
  }

  g_stores.options[0].selected = true;
}

function sendIt(p_action)
{
    var form;

    if (document.layers)
    {
        form = document.contentLayer.document.serverstore;
    }
    else
    {
        form = document.all.serverstore;
    }

    var server = form.servers.value;
    var store = form.stores.value;
    if (p_action == "<%=TeamSiteServerConstants.NEXT_ACTION%>")
    {
       form.action = "<%=nextUrl%>" + "&" +
          "<%=TeamSiteServerConstants.SERVERS%>=" + server + "&" +
          "<%=TeamSiteServerConstants.STORES%>=" + store;
    }
    else if (p_action == "<%=TeamSiteServerConstants.CANCEL_ACTION%>")
    {
       form.action = "<%=cancelUrl%>";
    }

    if (confirmForm(form))
    {
       form.submit();
    }
}

function confirmForm(form)
{
    // check if teamsite servers are selected
    if (!isSelectionMade(form.servers))
    {
      alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_teamsite_server"))%>");
      form.servers.focus();
      return false;
    }

    // check if teamsite stores are selected
    if (!isSelectionMade(form.stores))
    {
      alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_teamsite_store"))%>");
      form.stores.focus();
      return false;
    }

    return true;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
 onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<BR>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD><SPAN CLASS="mainHeading"><%=lb_title%></SPAN></TD>
  </TR>
  <TR>
    <TD WIDTH=500>
      <%=bundle.getString("helper_text_select_server_and_store")%>
    </TD>
  </TR>
</TABLE>
<BR>

<FORM NAME="serverstore" METHOD="POST" CLASS="standardText"
 ENCTYPE="multipart/form-data">
<p><%=lb_server%> &nbsp;
<select name="servers" size="1" onchange="redirect(this.options.selectedIndex)">
<%
if (servers != null)
{
  for (int i = 0; i < servers.size(); i++)
  {
    Vector stores = new Vector();
    TeamSiteServer tss = (TeamSiteServer)servers.get(i);
    long tsId = tss.getId();
    String tsName = tss.getName();
    out.print(" <option value='");
    out.print(tsId);
    out.print("'>");
    out.print(tsName);
    out.print("</option>\n");
  }
}
out.print("</select>");
%>
<BR>
<%=lb_store%> &nbsp;
<select name="stores" size="1">
<%
if (servers != null)
{
        Vector stores = new Vector();
        TeamSiteServer tss = (TeamSiteServer)servers.get(0);
        stores = new Vector(tss.getBackingStoreIds());
        for(int j=0; j<stores.size(); j++)
        {
           Long l = (Long) stores.get(j);
           long bsId = l.longValue();
           BackingStore bs = (BackingStore)ServerProxy
                              .getTeamSiteServerPersistenceManager()
                              .readBackingStore(bsId);
           String bsName = bs.getName();
           out.print(" <option value='");
           out.print(bsId);
           out.print("'>");
           out.print(bsName);
           out.print("</option>");
        }
}
%>
</select>
</p>
<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>"
 onclick="sendIt('<%=TeamSiteServerConstants.CANCEL_ACTION%>');">
<INPUT TYPE="SUBMIT" VALUE="<%=lb_next%>" name="lb_next"
 onclick="sendIt('<%=TeamSiteServerConstants.NEXT_ACTION%>');">
</FORM>
</DIV>
</BODY>
</HTML>
