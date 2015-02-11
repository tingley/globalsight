<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
      com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteServerConstants,
      com.globalsight.everest.util.comparator.TeamSiteServerComparator,
      com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl,
	  com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.util.resourcebundle.ResourceBundleConstants,
      com.globalsight.util.resourcebundle.SystemResourceBundle,
      com.globalsight.util.GlobalSightLocale,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="add" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="delete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
// Use this information to create a temporary directory.
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
Vector contentStores = new Vector();
String deleteUrl  = delete.getPageURL() + "&"
                 + TeamSiteServerConstants.ACTION 
                 + "=" + TeamSiteServerConstants.REMOVE_ACTION;
String addUrl = add.getPageURL() + "&"
                 + TeamSiteServerConstants.ACTION 
                 + "=" + TeamSiteServerConstants.ADD_ACTION;
String doneUrl = done.getPageURL() + "&" 
                 + TeamSiteServerConstants.ACTION 
                 + "=" + TeamSiteServerConstants.SAVE_ACTION;
String cancelUrl = cancel.getPageURL() + "&" 
                 + TeamSiteServerConstants.ACTION 
                 + "=" + TeamSiteServerConstants.CANCEL_ACTION;

String lb_title = bundle.getString("lb_add_another_content_store");
String lb_help = bundle.getString("lb_help");
String lb_cancel = bundle.getString("lb_cancel");
String lb_done = bundle.getString("lb_done");
String lb_remove = bundle.getString("lb_remove");
String lb_add = bundle.getString("lb_add");
String lb_store_name = bundle.getString("lb_store_name");
String lb_store = TeamSiteServerConstants.STORE_NAME;
String lb_store_not_selected = bundle.getString("lb_store_not_selected");
String msg_one_store_name = bundle.getString("msg_one_store_name");
int storeSize = 0;

String lb_pleaseEnterData = bundle.getString("msg_enter_store_name");
try
{
    contentStores = (Vector)sessionMgr.getAttribute(TeamSiteServerConstants.CONTENT_STORES);
}
catch(Exception  e)
{
    // do nothing
}
if(contentStores != null)
{
    storeSize = contentStores.size();
}
else
{
    storeSize = 0;
}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "<%= bundle.getString("lb_teamsite") %>";
var guideNode = "teamsiteServers";
var helpFile = '<%=bundle.getString("help_teamsite_add_another_store")%>';

function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow','resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

var reload = false;
function prepToClose()
{
  if (!reload)
  {
    window.close();
  }
}

function doOnload()
{
    self.focus();
}

function doOnunload()
{
    prepToClose();
}

function isReady()
{
    value = addStoreForm.<%=lb_store%>.value;
    if (value == null || value == "")
    {
        alert("<%=lb_pleaseEnterData%>");
        addStoreForm.<%=lb_store%>.focus();
        return false;
    }
    return true;
}


function saveForm(p_action)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.addStoreForm;
    }
    else
    {
        theForm = document.all.addStoreForm;
    }
	if (p_action == "<%=TeamSiteServerConstants.ADD_ACTION%>")
	{
        if (!isReady())
        {
            return false;
        }
        theForm.action = "<%=addUrl%>" + "&<%=lb_store%>=" + theForm.<%=lb_store%>.value;
	}
	if (p_action == "<%=TeamSiteServerConstants.SAVE_ACTION%>")
	{
       if(<%=storeSize%> > 0)
       {
          theForm.action = "<%=doneUrl%>";
       }
       else
       {
         alert("<%=msg_one_store_name%>");
         return false;
       }
	}
	if (p_action == "<%=TeamSiteServerConstants.CANCEL_ACTION%>")
	{
       theForm.action = "<%=cancelUrl%>";
	}
	theForm.submit();
}

function submitForm()
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.deleteForm;
    }
    else
    {
        theForm = document.all.deleteForm;
    }
    if(optionTest(theForm))
    {
       theForm.submit();
    }
}

function optionTest(formSent)
{
    var storeChecked = false;

    if (formSent.<%=TeamSiteServerConstants.STORE_CHECKBOXES%>.value)
    {
        if (formSent.<%=TeamSiteServerConstants.STORE_CHECKBOXES%>.checked)
        {
            storeChecked = true;
        }
    }
    else
    { 
        for (var i = 0;
             i < formSent.<%=TeamSiteServerConstants.STORE_CHECKBOXES%>.length; i++)
        {
            if (formSent.<%=TeamSiteServerConstants.STORE_CHECKBOXES%>[i].checked == true)
            {
                  storeChecked = true;
                  break;
            }
        }
    }

    if (!storeChecked)
    {
        alert("<%=lb_store_not_selected%>");
        return(false);
    }

    return true;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    id="idBody" onLoad="loadGuides(); doOnload()" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD><SPAN CLASS="mainHeading"><%=lb_title%></SPAN></TD>
  </TR>
  <TR>
    <TD WIDTH=500><%=bundle.getString("helper_text_content_store")%></TD>
  </TR>
</TABLE>
<P>
<FORM action="<%=deleteUrl%>" METHOD="post" NAME="deleteForm">

<!-- Table to align the Remove button -->
<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
  <TR>
    <!-- Table for the data -->
    <TD>
      <TABLE BORDER="0" CELLPADDING="3" CELLSPACING="0"
	STYLE="border: solid navy 1px">
	<TR CLASS="tableHeadingBasic">
	  <TD>&nbsp;</TD>
	  <TD><%=lb_store_name%></TD>
	</TR>
<%
Vector branchStores = (Vector)sessionMgr.getAttribute(TeamSiteServerConstants.BRANCH_STORES);
if (contentStores != null)
{
    int i = 0;
    for (i=0; i<contentStores.size(); i++)
    {
        String storeName = (String)contentStores.get(i);
        boolean cannotRemove = false;
        if(branchStores != null)
        {
            for (int j=0; j<branchStores.size(); j++)
            {
                if(storeName.equals((String)branchStores.elementAt(j)))
                {
                    cannotRemove = true;
                    break;
                }
            }
        }
        out.print("<TR BGCOLOR='");
        if (i % 2 == 0)
        {
          out.print("#FFFFFF");
        }
        else
        {
          out.print("#EEEEEE");
        }
        out.print("'>");
        // Col 1: checkbox
        out.print("<TD><INPUT TYPE='checkbox' CLASS='standardText' ");
        out.print("NAME='");
        out.print(TeamSiteServerConstants.STORE_CHECKBOXES);
        out.print("' VALUE='");
        out.print(i);
        out.print("'");
        if(cannotRemove)
        {
            out.print("DISABLED");
        }
        out.print(">");
        out.println("</INPUT></TD>");
        // Col 2: storeName 
        out.print("<TD>");
        out.print(contentStores.get(i));
        out.println("</TD>");

        out.println("</TR>");
    }
}
%>
	<TR>
	  <TD COLSPAN=4 ALIGN="RIGHT"></TD>
	</TR>
      </TABLE>
    </TD>
  </TR>
  <TR>
    <TD ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lb_remove%>" VALUE="<%=lb_remove%>" 
      onclick="submitForm()"> 
    </TD>
  </TR>
</TABLE>
</FORM>
<P>

<FORM NAME="addStoreForm" METHOD="POST" ACTION="<%=addUrl%>"
        onSubmit="return isReady()" CLASS="standardText">
<P>
<SPAN class="standardTextBold"><%=lb_store_name%></SPAN>
<BR>
<INPUT TYPE="text" SIZE="60" NAME="<%=lb_store%>"> </INPUT>
<BR>
<INPUT TYPE="BUTTON" VALUE="<%=lb_add%>" name="idSubmit"
 onclick="saveForm('<%=TeamSiteServerConstants.ADD_ACTION%>');">  </INPUT> 
<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
 onclick="saveForm('<%=TeamSiteServerConstants.CANCEL_ACTION%>');">   </INPUT> 
<INPUT TYPE="BUTTON" NAME="<%=lb_done%>" VALUE="<%=lb_done%>" 
 onclick="saveForm('<%=TeamSiteServerConstants.SAVE_ACTION%>');">  </INPUT> 
</P>
</FORM>
</DIV>
</BODY>
</HTML>
