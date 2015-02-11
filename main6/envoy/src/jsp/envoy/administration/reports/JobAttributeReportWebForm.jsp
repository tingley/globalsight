<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="java.util.*,
	       com.globalsight.everest.foundation.User,
	       com.globalsight.everest.webapp.pagehandler.administration.reports.AttributeItem,
	       com.globalsight.everest.servlet.util.SessionManager,
	       com.globalsight.everest.projecthandler.ProjectImpl,
	       com.globalsight.everest.webapp.WebAppConstants,
	       com.globalsight.everest.workflowmanager.Workflow,
	       com.globalsight.everest.webapp.javabean.NavigationBean,
	       com.globalsight.everest.webapp.pagehandler.PageHandler,
	       com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
	       com.globalsight.everest.util.comparator.UserComparator,
	       com.globalsight.everest.util.comparator.JobComparator,
	       com.globalsight.everest.util.comparator.LocaleComparator,
	       com.globalsight.everest.jobhandler.Job,
	       com.globalsight.everest.servlet.util.ServerProxy,
	       com.globalsight.util.GlobalSightLocale,
	       com.globalsight.util.SortUtil,
	       java.util.Locale,java.util.ResourceBundle"
	session="true"%>
<%
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	if (uiLocale == null)
	{
		uiLocale = Locale.ENGLISH;
	}

    String self = "/globalsight/ControlServlet?activityName=jobAttributeReport";
    String isRegexUrl = self + "&action=isJaveRegex";
    List<ProjectImpl> projects = (List)request.getAttribute("project");
    Vector<User> users = (Vector<User>)request.getAttribute("users");
    SortUtil.sort(users, new UserComparator(Locale.getDefault()));
    
    List<AttributeItem> attributes = (List)request.getAttribute("attributes");
    
    StringBuffer attIds = new StringBuffer();
    for (AttributeItem n : attributes){
        if (attIds.length() > 0)
        {
            attIds.append(",");
        }
        attIds.append(n.getId());
    }
    
	ResourceBundle bundle = PageHandler.getBundle(session);
%>

<head>
<title><%=bundle.getString("lb_job_attributes")%></title>
<style type="text/css">
@import url(/globalsight/includes/attribute.css);
@import url(/globalsight/dijit/themes/nihilo/nihilo.css);
@import url(/globalsight/dojo/resources/dojo.css);
@import url(/globalsight/includes/dojo.css);

.regex {
  color: #0000ff;
}

</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js"></SCRIPT>
<script language="JavaScript" src="/globalsight/includes/report/calendar.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>
<script>

dojo.require("dojo.dnd.Container");
dojo.require("dojo.dnd.Manager");
dojo.require("dojo.dnd.Source");
dojo.require("dijit.Menu");

function orderItem(id)
{
	var orderId = document.getElementById("orderItem").value;
	if (orderId && orderId.length > 0){
		var item = document.getElementById("classDiv" + orderId);
		item.style.backgroundColor = "";
		dijit.byId("orderAsc" + orderId).setDisabled(false);
		dijit.byId("orderDesc" + orderId).setDisabled(false);
		dijit.byId("notOrder" + orderId).setDisabled(true);
	}
	
	var item = document.getElementById("classDiv" + id);
	item.style.backgroundColor = "#EEEE00";

	document.getElementById("orderItem").value = id;
}

function orderItemAsc(id)
{
	orderItem(id);	
	document.getElementById("order").value = "asc";	

	dijit.byId("orderAsc" + id).setDisabled(true);
	dijit.byId("orderDesc" + id).setDisabled(false);
	dijit.byId("notOrder" + id).setDisabled(false);
}

function orderItemDesc(id)
{
	orderItem(id);	
	document.getElementById("order").value = "desc";	

	dijit.byId("orderAsc" + id).setDisabled(false);
	dijit.byId("orderDesc" + id).setDisabled(true);
	dijit.byId("notOrder" + id).setDisabled(false);
}

function notOrderItem(id)
{
	var item = document.getElementById("classDiv" + id);
	item.style.backgroundColor = "#fff";

	document.getElementById("orderItem").value = "";

	dijit.byId("orderAsc" + id).setDisabled(false);
	dijit.byId("orderDesc" + id).setDisabled(false);
	dijit.byId("notOrder" + id).setDisabled(true);
}

function addOption(box, name, value, className)
{
    var option = document.createElement("option");
    var selectBox = document.getElementById(box);
    option.appendChild(document.createTextNode(name));
    option.setAttribute("value", value);
    if (className)
    {
    	option.className = className;
    }
    selectBox.appendChild(option);
}

    function showCalendar1() {
        var cal1 = new calendar2(document.forms['AttributeForm'].elements['startDate']);
        cal1.year_scroll = true;
        cal1.time_comp = true;
        cal1.popup();
    }
    
    function showCalendar2() {
        var cal2 = new calendar2(document.forms['AttributeForm'].elements['endDate']);
        cal2.year_scroll = true;
        cal2.time_comp = true;
        cal2.popup();
    }

    function setIsRegex()
    {
        var box = document.getElementById("isRegex");
    	if (box.checked)
		{
    		document.getElementById("option").className="regex";
		}
		else
		{
			document.getElementById("option").className="";
		}
    }

    function isJaveRegex(value)
    {
        var obj = 
        {
        	value : value
        };
        
    	dojo.xhrPost(
	   {
	       url:"<%=isRegexUrl%>",
			handleAs :"text",
			content :obj,
			load : function(data) {
				if (data) {
					alert(data);
				} else {
					addOption("regexItems", value, value, null);
					addOption("allItems", value, value, "regex");
					document.getElementById("option").value = "";
				}
			},
			error : function(error) {
				alert(error.message);
			}
		});
	}

    function updateAttribute(srcId, trgId)
    {
    	var allItems = document.getElementById(trgId);
        var options =  allItems.options;
        for (var i = options.length-1; i>=0; i--)
        {
        	allItems.remove(i);       
        }
        
        var selectedDiv = document.getElementById(srcId);
        var nodes = selectedDiv.childNodes;
        var ids = "";
        var n = 0;
        for (var i=0; i < nodes.length; i++)
        {
            var nodeId = nodes[i].id;
            if (nodeId)
            {
                var option = document.createElement("option");
                option.appendChild(document.createTextNode(nodeId));
                option.setAttribute("value", nodeId);
                option.setAttribute("selected", true);
                allItems.appendChild(option);
                n++;
            }
        }

        return n;
    }
    
    function submitForm()
    {
    	var n = updateAttribute("trgAttribute1", "normalAtts");
    	n += updateAttribute("trgAttribute2", "totalAtts");

    	if (n == 0)
    	{
        	if (!confirm("<%=bundle.getString("msg_report_no_attribute")%>"))
        	{
            	return;
            }
        }

    	AttributeForm.submit();;
    }
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" class="nihilo">
<table border="0" cellspacing="0" cellpadding="5" height="452" width="100%">
	<tr bgcolor="#ABB0D3" valign="top">
		<td height="40" colspan="2"><b><font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("report")%></font></b>: <font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("lb_job_attributes")%></font></td>
	</tr>
	<tr bgcolor="ccffff">
		<td height="6" colspan="2"></td>
	</tr>
	<tr>
		<td height="452" valign="top" align="left" width="20%" background="/globalsight/images/parambar.jpg"><font face="Verdana, Arial, Helvetica, sans-serif" size="2"></font></td>
		<td height="452" valign="top" align="left" bgcolor="#E9E9E9"><font face="Verdana, Arial, Helvetica, sans-serif" size="3">
		<FORM name="AttributeForm" action="/globalsight/envoy/administration/reports/JobAttributeReport.jsp" method=POST>
		<CENTER>
		<TABLE BORDER=0 CELLSPACING=10>
			<TR>
				<TD ALIGN=RIGHT width="20%"><%=bundle.getString("lb_start_date")%>:</TD>
				<TD valign="middle" colspan="2"><INPUT type=text name="startDate" value="" READONLY>&nbsp;<IMG style='cursor: hand' align=middle border=0 src="/globalsight/includes/Calendar.gif"
					onclick=showCalendar1();></TD>
			</TR>
			<TR>
				<TD ALIGN=RIGHT><%=bundle.getString("lb_end_date")%>:</TD>
				<TD valign="middle" colspan="2"><INPUT type=text name="endDate" value="" READONLY>&nbsp;<IMG style='cursor: hand' align=middle border=0 src="/globalsight/includes/Calendar.gif"
					onclick=showCalendar2();></TD>
			</TR>
			<TR>
				<TD ALIGN=RIGHT valign="top"><%=bundle.getString("lb_prop_type")%>:</TD>
				<TD colspan="2"><SELECT name="selectedProjects" size="5" multiple="multiple">
					<%for(ProjectImpl p : projects){ %>
					<OPTION value="<%=p.getId() %>" selected><%=p.getName()%></option>
					<%}%>
				</SELECT></TD>
			</TR>
			<TR>
				<TD ALIGN=RIGHT valign="top"><%=bundle.getString("lb_job_status")%>:</TD>
				<TD colspan="2"><SELECT name="selectedStatus" size="6" multiple="multiple">
					<OPTION value="READY_TO_BE_DISPATCHED" selected><%=bundle.getString("lb_ready")%></option>
					<OPTION value="DISPATCHED" selected><%=bundle.getString("lb_inprogress")%></option>
					<OPTION value="LOCALIZED" selected><%=bundle.getString("lb_localized")%></option>
				    <OPTION value="EXPORTED" selected><%=bundle.getString("lb_exported")%></option>
				    <OPTION value="EXPORT_FAILED" selected><%=bundle.getString("lb_exported_failed")%></option>
				    <OPTION value="ARCHIVED" selected><%=bundle.getString("lb_archived")%></option>
				</SELECT></TD>
			</TR>
			
			<TR>
				<TD ALIGN=RIGHT valign="top"><%=bundle.getString("lb_submitters")%>:</TD>
				<TD colspan="2"><SELECT name="selectedSubmitters" size="5" multiple="multiple">
					<%for(User u : users){ %>
					<OPTION value="<%=u.getUserId()%>" selected><%=u.getDisplayName(uiLocale)%></option>
					<%}%>
				</SELECT></TD>
			</TR>
			
			<tr>
			<td ALIGN=RIGHT valign="top" ><%=bundle.getString("lb_target_locales")%>:</td>
			<td colspan="2">
			<select name="targetLocalesList" size="5" multiple="true">
			<%
			         ArrayList targetLocales = new ArrayList( ServerProxy.getLocaleManager().getAllTargetLocales() );
			         SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
                     for( int i=0; i < targetLocales.size(); i++)
			         {
			             GlobalSightLocale gsLocale = (GlobalSightLocale) targetLocales.get(i);
			%>
			<option VALUE="<%=gsLocale.toString()%>" selected><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
			<%
			         }
			%>
			</select>
			</td>
			</tr>
			
			<TR>
				<TD ALIGN=RIGHT valign="top"><div style="padding-top: 7px;"><%=bundle.getString("lb_attributes")%>:</div></TD>
				<td colspan="2">
				<table CELLSPACING=0>
				<tr>
				<td>
				<div style="padding-top: 7px;"><%=bundle.getString("lb_unselected_attributes")%>:</div>
				<div style="font-size: 9;"><i>(<%=bundle.getString("lb_drag_from_right")%>)</i></div>
				<div class="wrap2">
				<div dojoType="dojo.dnd.Source" id="srcAttribute" class="dndcontainer1" accept="number, text">
				<%for (AttributeItem n : attributes){%>
				<div class="dojoDndItem" dndType="<%=n.getDndType()%>" id='<%=n.getId()%>'>
				<div id="classDiv<%=n.getId()%>" class='<%=n.isFromSuper() ? "superAttribute" : "" %>'>
				<%=n.getName()%> 
				</div>
				</div>
				<%}%>
				</div>
				</div>
				</td>
				<td width="10px;">&nbsp;</td>
				<td align="left">
				<div style="clear:both;">
				<div><%=bundle.getString("lb_normal_attributes")%>:</div>
				<div style="font-size: 9;"><i>(<%=bundle.getString("lb_drag_from_left")%>)</i></div>
				<div class="wrap4">
				<div dojoType="dojo.dnd.Source" id="trgAttribute1" accept="number, text" class="dndcontainer2"></div>
				</div>
				</div>

				<div style="clear:both;">
				<div ><%=bundle.getString("lb_statistical_attributes")%>:</div>
				<div style="font-size: 9;"><i>(<%=bundle.getString("lb_drag_from_left")%>)</i></div>
				<div class="wrap4">
				<div dojoType="dojo.dnd.Source" id="trgAttribute2" accept="number" class="dndcontainer2"></div>
				</div>
				</div>
				</TD>
				</tr>
				</table>
				</td>				
			</TR>

			<TR>
			    <TD ALIGN=RIGHT></TD>
				<TD colspan=2>
				<input type=button value="<%=bundle.getString("lb_shutdownSubmit")%>" onclick="submitForm();"> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <input type=button value="<%=bundle.getString("lb_cancel")%>"
					onclick=window.close();>
				</TD>
			</TR>
		</TABLE>
		</CENTER>
<div style="display:none">
<select name="normalAtts" multiple="multiple" id="normalAtts">
</select>
<select name="totalAtts" multiple="multiple" id="totalAtts">
</select>
<input type="hidden" id="orderItem" name="orderItem">
<input type="hidden" id="order" name="order" value="asc">
</div>
		</FORM>
		</font></td>
	</tr>
</table>

<%for (AttributeItem n : attributes){%>
<div dojoType="dijit.Menu" contextMenuForWindow="false" style="display: none;" targetNodeIds="<%=n.getId()%>">
  <div dojoType="dijit.MenuItem" id="orderAsc<%=n.getId()%>" onClick="orderItemAsc('<%=n.getId()%>');"><%=bundle.getString("lb_orderAsc")%></div>
  <div dojoType="dijit.MenuItem" id="orderDesc<%=n.getId()%>" onClick="orderItemDesc('<%=n.getId()%>');"><%=bundle.getString("lb_orderDesc")%></div>
  <div dojoType="dijit.MenuSeparator"></div>
  <div dojoType="dijit.MenuItem" id="notOrder<%=n.getId()%>" onClick="notOrderItem('<%=n.getId()%>');" disabled="true"><%=bundle.getString("lb_cancelOrder")%></div>
</div>
<%}%>
				

</body>
</HTML>