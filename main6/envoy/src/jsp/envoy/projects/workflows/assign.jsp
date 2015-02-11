<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.taskmanager.Task,com.globalsight.everest.taskmanager.TaskAssignee,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.webapp.javabean.NavigationBean,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,com.globalsight.everest.usermgr.UserInfo,com.globalsight.everest.util.system.SystemConfigParamNames,com.globalsight.everest.util.system.SystemConfiguration,java.util.*"
	session="true"%>
<jsp:useBean id="done" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
	
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);

	boolean b_isDell = false;
	try {
		SystemConfiguration sc = SystemConfiguration.getInstance();
		b_isDell = sc
				.getBooleanParameter(SystemConfigParamNames.IS_DELL);
	} catch (Exception ge) {
	}
	
	String title = bundle.getString("lb_reassign_full");
	if (b_isDell)
		title = bundle.getString("lb_reassign_revonly");

	//Urls of the links on this page
	String doneUrl = done.getPageURL() + "&"
			+ JobManagementHandler.ASSIGN_PARAM + "=saveAssign";
	String cancelUrl = done.getPageURL() + "&"
            + JobManagementHandler.ASSIGN_PARAM + "=cancelAssign";
	String jobId = (String)request.getAttribute(WebAppConstants.JOB_ID);
	if(jobId != null && jobId != ""){
		doneUrl += "&"+WebAppConstants.JOB_ID+"="+jobId;
		cancelUrl  += "&"+WebAppConstants.JOB_ID+"="+jobId;
	}

	//Data
	List<Task> keys = (List<Task>) sessionMgr.getAttribute("tasks");
	Hashtable taskUserHash = (Hashtable) sessionMgr
			.getAttribute("taskUserHash");
	Hashtable taskSelectedUserHash = (Hashtable) sessionMgr
			.getAttribute("taskSelectedUserHash");
	Enumeration tasks = taskUserHash.keys();
%>

<HTML>
<!-- This JSP is: envoy/projects/workflows/assign.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT type="text/javascript">
var needWarning = true;
var objectName = "re-assign operation";
var taskIds = new Array();
var helpFile = "<%=bundle.getString("help_job_reassign")%>";
var guideNode = "myJobs";
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

function getSelectRadioBtn()
{
	var selectedRadioBtn =  new Array();
	 if (assignForm.RadioBtn.length)
	 {
		 for (i = 0; i < assignForm.RadioBtn.length; i++) 
	     {
			 if(assignForm.RadioBtn[i].checked == true)
			 {
				 selectedRadioBtn.push(taskIds[i+1]);
			 }
	     } 
	 }
	 else
	 {
		 if(assignForm.RadioBtn.checked == true)
		 {
			 selectedRadioBtn.push(taskIds[1]);
		 }
	 }
	 return selectedRadioBtn;
}
function getChangedTasks(selectedRadioBtn)
{
	var tasks = "";
	var index = 1;
	if(selectedRadioBtn.length > 0){
		for(var n = 0;n < selectedRadioBtn.length; n++)
		{
			<%
			for (Task task: keys)
			{
				long taskId = task.getId();
				%>
				var id = <%=taskId%>;
				if( id == selectedRadioBtn[n])
				{
					var selectAll = document.getElementById("selectAll"+id).checked;
				    var values = "";
				    var selectUserSize = 0;
				    var selectElem = document.getElementById("task"+id);
				    
				    var isChanged = false;
				    for (var i = 0; i < selectElem.length; i++)
				    {
				        if (selectAll || selectElem.options[i].selected == true)
				        {
				        	selectUserSize++;     	
				        	var userId = selectElem.options[i].getAttribute("userId");     	
				        	var uId = "t_" + id + "_" + userId;
				        	var userDiv = document.getElementById(uId);
				        	if (!userDiv)
				        	{
				        		isChanged = true;
				        	    break;
				        	}
				        }        
				    }
				    if(isChanged){
				    	
				    	tasks += ' ' + index + '. <%=task.getTaskDisplayName()%>\n';
				    }else{
				    	
					    var tLength = document.getElementById('t_l_<%=taskId%>');
					    if(tLength!=null)
					    {
					    	if(selectUserSize == tLength.getAttribute("value"))
					    	{
					    		tasks += ' ' + index + '. <%=task.getTaskDisplayName()%>---(No Change)\n';
					    	}
					    	else
					    	{
					    		tasks += ' ' + index + '. <%=task.getTaskDisplayName()%>\n';
					    	}		
					    	index++;
					    }
				    }
				}
				<%
			}
			%>
		}
	}

return tasks;
}

function confirmForm()
{
    var selectedRadioBtn=getSelectRadioBtn();
	if (selectedRadioBtn.length > 0)
	{
		var changedTasks = getChangedTasks(selectedRadioBtn);
		var msg = "<%=bundle.getString("lb_activities_modified")%>\n\n" 
			+ changedTasks
			+ "\n<%=bundle.getString("lb_confirm_reassign")%>";
		
		if(!confirm(msg))
		{
			return false;
		}
    }
    else
    {
        alert("No activity is selected.");
        return false;
    }
	if(selectedRadioBtn.length > 0 )
	{
		for(var n = 0;n < selectedRadioBtn.length; n++)
		{
			<% if (taskUserHash.size() > 0)
		    {
		        while (tasks.hasMoreElements())
		        {
		            Task task = (Task)tasks.nextElement();
		            long taskId = task.getId();
		            %>
		            var id = <%=taskId%>;
		            if(selectedRadioBtn[n] == id)
		            {
			            <%
			            Hashtable userInfos = (Hashtable)taskUserHash.get(task);
			            if(userInfos != null)
			            {
			            %>
			    	       var selectAll = document.getElementById("selectAll"+id).checked;
			    	       var selectElem = document.getElementById("task"+id);        
			               var values = "";
			               
			               for (var i = 0; i < selectElem.length; i++)
			               {
			                   if (selectAll || selectElem.options[i].selected == true)
			                   {
			                       values += selectElem.options[i].value + ":";
			                   }
			               }
			               
			               if(values == "")
			               {
			                   alert('<%=bundle.getString("jsmsg_customer_select_user")%>');
			                   document.getElementById(id).click();
			                   return false;
			               }
			               assignForm.users<%=taskId%>.value = values;
			     <% }%>
		            }
		        <%
		        } 
		    } 
		    %>
		}
	}
    return true;
}

function confirmCancel()
{
	var selectedRadioBtn=getSelectRadioBtn();
	var changedTasks = getChangedTasks(selectedRadioBtn);
	if (changedTasks.length > 1)
	{
		return confirm("By leaving this page you will lose all of the work on this " + objectName + "\n\nIs this OK?");
    }
    
    return true;
}

function submitPage(button)
{
	if (button == "save")
	{
	   saveChangedUser();
	   
	   if(confirmForm())
	   {
	      assignForm.action = "<%=doneUrl%>";
	      assignForm.submit();
	   }
	}
	else
	{
	    if(confirmCancel())
	    {	    	
		    assignForm.action = "<%=cancelUrl%>";
		    assignForm.submit();	    
	    }
	}
}

function getUsersByActivityId(id)
{
	return document.getElementById('hiddenUsers' + id);
}

function saveChangedUser()
{
	var active = document.getElementById('active');
	var aid;
	
	aid = parseInt(active.getAttribute("activeId"));
	
	if (aid != -1)
	{
		getUsersByActivityId(aid).innerHTML = document.getElementById('showedUsers').innerHTML;
	}
	
}

// Transport selected value in select tag, for firefox bug.
function fnTransportSelectedValue(div1, div2)
{
	//if(!isFirefox) return;
	
	var sel1, sel2, check1, check2;
	
	sel1 = div1.getElementsByTagName("select")[0];
	sel2 = div2.getElementsByTagName("select")[0];
	for(var i=0; i<sel1.options.length; i++)
	{
		sel1.options[i].selected = sel2.options[i].selected;
	}	

	check1 = div1.getElementsByTagName("input")[0];
	check2 = div2.getElementsByTagName("input")[0];
	check1.checked = check2.checked;
}

function changeActive(currentActive)
{
	saveChangedUser();

	var users = document.getElementById('showedUsers');
	var currentUsers = getUsersByActivityId(currentActive.id);
	var originalID,originalDiv;
	
	originalID = users.getAttribute("originalID");
	if(originalID != "")
	{
		originalDiv = getUsersByActivityId(originalID);
		originalDiv.innerHTML = users.innerHTML;
		fnTransportSelectedValue(originalDiv,users);
	}
	users.setAttribute("originalID", currentActive.id);
	users.innerHTML = currentUsers.innerHTML;
	fnTransportSelectedValue(users,currentUsers);
	currentUsers.innerHTML = "";
	
	var active = document.getElementById('active');
	var aid;
	
	aid = parseInt(active.getAttribute("activeId"));
	
	if (aid != -1)
	{
		var prevActive = document.getElementById(aid);
		prevActive.className = "normal";
		prevActive.selected = false;
	}
	
	//currentActive.className = "selected";
	//currentActive.selected = true;
	
  	var attrActiveId = active.getAttribute("activeId");
  	attrActiveId = "" + currentActive.id;
}

function disable(name, isDisable)
{
	var obs = document.getElementsByName(name);
	for (var i = 0; i < obs.length; i++)
	{
		obs[i].disabled= isDisable;
	}
}
</SCRIPT>
<style>

.normal
{
 text-align : center;
 width: 150px;
 color: #000;
 text-decoration: none;
 border: 1px solid #CCC;
 cursor: pointer;
}

.selected
{
 text-align : center;
 width: 150px;
 color: #000;
 text-decoration: none;
 background-color: #FFFFCC;
 border: 1px solid #999;
 cursor: pointer;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0">
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer"
	STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />

<FORM NAME="assignForm" METHOD="POST">
	<table cellpadding=2 cellspacing=2 border=0 CLASS="standardText">
		<tr>
			<td><b><%=bundle.getString("lb_job")%>: </b><%=request.getAttribute("jobName")%></td>
		</tr>
		<tr>
			<td><b><%=bundle.getString("lb_target_locale")%>: </b><%=sessionMgr.getAttribute("targDisplayName")%></td>
		</tr>
	</table>
<p>
<div CLASS="standardText" style="width:1000px;float:left;">
<div CLASS="standardText" style="float:left;">
  <%if (taskUserHash.size() == 0) 
    {%>
        <%=bundle.getString("msg_no_activities_reassign")%>
  <%} 
	else 
	{
	    int i = 0;
	    for (Task task: keys)
	    {
			i++;
			long taskId = task.getId();
			Collection<UserInfo> users = null;
			Collection<UserInfo> selectedUsers = null;
			Hashtable userInfos = (Hashtable) taskUserHash.get(task);
			Hashtable selectedUserInfos = (Hashtable) taskSelectedUserHash
					.get(task);
			if (userInfos != null) 
			{
				users = userInfos.values();
			}
			if (selectedUserInfos != null) 
			{
				selectedUsers = selectedUserInfos.values();
			}				
			%>
			
			<div CLASS="standardText" style="width:200px; margin: 2px;">
			    <table CLASS="standardText">
					<tr>
						<td>
							<INPUT TYPE="checkbox" id="<%=taskId%>"  NAME="RadioBtn" VALUE="<%=i%>" onClick="changeActive(this)">
						</td>
						<td>
							<span title="<%=bundle.getString("lb_reassign_the_activity")%>" id="<%=taskId%>"> <%=task.getTaskDisplayName()%></span>
						    <input type="hidden" name="users<%=taskId%>" value="">
						</td>
					</tr>
				</table>
			</div>
			<SCRIPT LANGUAGE="JavaScript1.2">
                taskIds[<%=i%>] = "<%=taskId%>";
			</script>
			
			<div id="hiddenUsers<%=taskId%>" style="display:none;">
			    <table>
			        <tr >
			            <td  CLASS="standardText"><b><%=task.getTaskDisplayName()%></b></td>
			            <td  CLASS="standardText" align="right">
				                <label> <%=bundle.getString("lb_select_all")%></label> 
				                <input id="selectAll<%=taskId%>" type="checkbox" 
				                	onclick = "disable('task<%=taskId%>', this.checked)">
			            </td>
			        </tr>
			        <tr>
			            <td colspan="2">
			                <select  id="task<%=taskId%>" name="task<%=taskId%>" multiple="true">
								<%for (UserInfo userInfo : users) 
								{
									String selected = "";
									if (selectedUsers != null) 
									{
										for (UserInfo selectedUser : selectedUsers) 
										{
											if (selectedUser.getUserId().equals(userInfo.getUserId())) 
											{
												selected = "selected";
												break;
											}
										}
									}
									String fullname = userInfo.getFirstName() + " " 
										+ userInfo.getLastName() + " ("+ userInfo.getUserName() + ")";
									out.println("<option userId = \"" + userInfo.getUserId() +  "\"  value=\"" + userInfo.getUserId() + "," 
										+ userInfo.getUserName() + "\" " + selected + " >" + fullname + "</option>");
								}%>
							</select>
			            </td>
			        </tr>
			    </table>
			</div>
			<%
	      }
	  }%>
</div>
<%if (taskUserHash.size() > 0) {%>
<div id="active" activeId="-1" display="none" style="float:left;"></div>
<div id="showedUsers" originalID="" style="float:left"></div>
<% }%>
</div>
<br><br><br><br><br><br>
<div CLASS="standardText" style="width:200px;">
	<INPUT TYPE="button" name="cancelBtn" VALUE="<%=bundle.getString("lb_cancel")%>" onclick="submitPage('cancel');"> 
    <%if (taskUserHash.size() != 0){%> 
	 	<INPUT TYPE="button" name="saveBtn" VALUE="<%=bundle.getString("lb_save")%>" onclick="submitPage('save');"> 
	<%}%>
</div>
</FORM>


</DIV>

<div style="display: none">
<%
for (Task task: keys)
{
	Hashtable selectedUserInfos = (Hashtable) taskSelectedUserHash.get(task);
	if (selectedUserInfos != null) 
	{
		Collection<UserInfo> users = selectedUserInfos.values();
        %>
		<div id="t_l_<%=task.getId()%>" value="<%=users.size() %>"/>
		<%
		for(UserInfo user: users)
		{
		%>
			<div id="t_<%=task.getId()%>_<%=user.getUserName()%>"/>
		<% 
		}		
	}
}
%>
</div>
</BODY>
</HTML>
