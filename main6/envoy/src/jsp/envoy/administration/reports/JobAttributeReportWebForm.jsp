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

.regex {
  color: #0000ff;
}
#srcAttribute div, #trgAttribute1 div, #trgAttribute2 div { margin: 2px; padding: 2px;cursor:pointer;}
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js"></SCRIPT>
<script language="JavaScript" src="/globalsight/includes/report/calendar.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script src="/globalsight/includes/ContextMenu.js"></script>
<script>
$(document).ready(function(){
	$("#startDate").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#endDate").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#endDate").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#startDate").datepicker( "option", "maxDate", selectedDate );
		}
	});
});

function orderItem(id)
{
	
	var orderId = document.getElementById("orderItem").value;

	if (orderId && orderId.length > 0){
		var item = document.getElementById(orderId);
		item.style.backgroundColor = "";

	}
	
	var item = document.getElementById(id);
	item.style.backgroundColor = "#EEEE00";
	document.getElementById("orderItem").value = id;
}

function orderItemAsc(id)
{
	orderItem(id);	
	document.getElementById("order").value = "asc";	
}

function orderItemDesc(id)
{
	orderItem(id);	
	document.getElementById("order").value = "desc";	
}

function notOrderItem(id)
{
	var item = document.getElementById(id);
	item.style.backgroundColor = "#fff";

	document.getElementById("orderItem").value = "";
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
    	if($("select[name='selectedProjects']").children().length==0){
    		alert("no projects!");
    		return;
    	}
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
    
    $(function() {
    	$( "#srcAttribute" ).sortable({
    		connectWith: "div"
    	});

    	$( "#trgAttribute1").sortable({
    		connectWith: "div",
    		dropOnEmpty: true
    	});

    	$( "#trgAttribute2" ).sortable({
    		connectWith: "div",
    		dropOnEmpty: true,
    		receive:function( event, ui){
    			var dndType = ui.item.context.getAttribute("dndType");
    			if(dndType == "text"){
    				var id = ui.sender.context.id;
    				$("#"+id).sortable( 'cancel' );
    			}
    		}
    	});
    	$( "#srcAttribute , #trgAttribute1, #trgAttribute2" ).disableSelection();
    });
    
    
    function contextForPage(id, e)
    {
        if(e instanceof Object)
        {
    	    e.preventDefault();
    	    e.stopPropagation();
        }
        var popupoptions;
         popupoptions = [
           new ContextItem("<div id='orderAsc'><%=bundle.getString("lb_orderAsc")%></div>",
             function(){orderItemAsc(id); }),
           new ContextItem("<div id='orderDesc'><%=bundle.getString("lb_orderDesc")%></div>",
             function(){orderItemDesc(id);  }),
             new ContextItem("<div id='cancelOrder' disabled = true><%=bundle.getString("lb_cancelOrder")%></div>",
               function(){ notOrderItem(id); })
         ];
        ContextMenu.display(popupoptions, e);
    }
    function load(){
    	ContextMenu.intializeContextMenu();
    }
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" class="nihilo" onload="load()">
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
				<td valign="middle" colspan="2" VALIGN="BOTTOM"><input type="text" id="startDate" name="startDate"></td>
			</TR>
			<TR>
				<TD ALIGN=RIGHT><%=bundle.getString("lb_end_date")%>:</TD>
				<td valign="middle" colspan="2" VALIGN="BOTTOM"><input type="text" id="endDate" name="endDate"></td>
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
			<option VALUE="<%=gsLocale.getId()%>" selected><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
			<%
			         }
			%>
			</select>
			</td>
			</tr>
			
			<tr>
				<TD ALIGN=RIGHT valign="top"><div style="padding-top: 7px;"><%=bundle.getString("lb_attributes")%>:</div></TD>
				<td colspan="2">
					<table CELLSPACING=0>
						<tr>
							<td>
								<div style="padding-top: 7px;"><%=bundle.getString("lb_unselected_attributes")%>:</div>
								<div style="font-size: 9;"><i>(<%=bundle.getString("lb_drag_from_right")%>)</i></div>
								<div class="wrap2">
									<div id="srcAttribute"  class="dndcontainer1">
										<%for (AttributeItem n : attributes){%>
											<div 
											 dndType="<%=n.getDndType()%>" id='<%=n.getId()%>'  class='<%=n.isFromSuper() ? "superAttribute" : "" %>'
											 oncontextmenu="contextForPage('<%=n.getId()%>',event)" ><%=n.getName()%>	</div>
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
										<div id="trgAttribute1" class="dndcontainer2"></div>
									</div>
								</div>

								<div style="clear:both;">
									<div ><%=bundle.getString("lb_statistical_attributes")%>:</div>
									<div style="font-size: 9;"><i>(<%=bundle.getString("lb_drag_from_left")%>)</i></div>
									<div class="wrap4">
										<div id="trgAttribute2"  class="dndcontainer2"></div>
									</div>
								</div>
							</TD>
						</tr>
					</table>
				</td>				
			</tr>
			
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
				
</body>
</HTML>