<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean, 
            com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
            com.globalsight.everest.webapp.pagehandler.administration.users.Modify2Handler"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="setRate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<jsp:useBean id="setSource" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
   ResourceBundle bundle = PageHandler.getBundle(session);
   String doneURL = done.getPageURL() + "&" + WebAppConstants.USER_ACTION +
                    "=" + WebAppConstants.USER_ACTION_MODIFY_LOCALES;
   String cancelURL = cancel.getPageURL() + "&" + WebAppConstants.USER_ACTION +
                    "=" + WebAppConstants.USER_ACTION_CANCEL_FROM_ACTIVITIES;
   String setRateURL = setRate.getPageURL() + "&" + WebAppConstants.USER_ACTION +
                    "=" + WebAppConstants.USER_ACTION_SET_RATE;
   //set Source
   String setSourceURL = setSource.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" +WebAppConstants.USER_ACTION_SET_SOURCE;

   String onClickString = "setTargets(selectedIndex)";
   String initTargetOptions = (String) request.getAttribute("optionPadding");

   StringBuffer initSourceBuf = new StringBuffer();
   initSourceBuf.append((String) request.getAttribute("allSourceLocales"));
   String initSourceOptions = initSourceBuf.toString();

   String jsmsgTargetLocale = bundle.getString("jsmsg_users_target_locale");
   String lbChoose = bundle.getString("lb_choose");
   String title = new String();
   String lbCancel = bundle.getString("lb_cancel");
   String lbDone = bundle.getString("lb_done");
   String warningInfo = (request.getAttribute("warningInfo") == null? "" :
                        (String)request.getAttribute("warningInfo"));
   String sourceLocaleStr = (String)request.getAttribute(Modify2Handler.SOURCE_LOCALE);
   String targetLocaleStr = (String)request.getAttribute(Modify2Handler.TARGET_LOCALE);
   String selectedCompanyId = (String)request.getAttribute(Modify2Handler.COMPANY_ID);
   String sourceLocale = "";
   String targetLocale = "";
   if(sourceLocaleStr != null && sourceLocaleStr.length() != 0 &&
      targetLocaleStr != null && targetLocaleStr.length() != 0 )
   {
       sourceLocale =
           sourceLocaleStr.substring(sourceLocaleStr.indexOf("[") + 1,
                                      sourceLocaleStr.indexOf("]"));

       targetLocale =
           targetLocaleStr.substring(targetLocaleStr.indexOf("[") + 1,
                                     targetLocaleStr.indexOf("]"));
   }
   boolean isJobCosting = ((Boolean)request.getAttribute(
       SystemConfigParamNames.COSTING_ENABLED)).booleanValue();
   String isJobCostingStr = (new Boolean(isJobCosting)).toString();
   Boolean isModify = (Boolean) request.getAttribute("isModify");
   if (isModify != null && isModify.booleanValue())
   {
        onClickString = "";
        initTargetOptions = (String) request.getAttribute("targetLocale");
        initSourceOptions = (String) request.getAttribute("sourceLocale");
        title = bundle.getString("lb_edit_roles");
   }
   else
   {
      title= bundle.getString("lb_new_role");
   }
   
   SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
   String userName = wrapper.getUserName();
%>
<HTML>
<!-- This JSP is envoy/administration/users/modify3.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_user") %>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_users_roles_new")%>";
var selSourceFirst = "<%=bundle.getString("lb_new_roles_selSourceFirst")%>";

var theForm;
var dependsInfo = '';
var validating = false;
var needSubmit = false;

function submitForm()
{
    if (document.layers) 
    {
        theForm = document.contentLayer.document.userForm;
    }
    else 
    {
        theForm = document.all.userForm;
    }
    
    if (!validating)
    {
        needSubmit = false;
        
        if(confirmForm(theForm))
        {
            theForm.submit();  
       }
    }
    else
    {
        needSubmit = true;
    }
}

function setSources()
{
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   }
   else
   {
      theForm = document.all.userForm;
   }

   theForm.action = "<%= setSourceURL %>";
   theForm.submit();
}
function setRate()
{
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   } else
   {
      theForm = document.all.userForm;
   }

   // Added for create roles in batches.
   var user_role_slTarget = document.getElementById("selectTargetLocale");
   if(user_role_slTarget!=null)
   {
	   var selectedNum = 0;
	   for(var i=0;i<user_role_slTarget.length;i++)
   	   {
	     if(user_role_slTarget[i].selected) 
	     {
		     selectedNum++;
	     }
	     if(selectedNum>1)
	     {
			return;
	     }
   	   }
   }   
   
   if (checkTargetLocale(theForm))
   {
       theForm.action = "<%= setRateURL %>";
       theForm.submit();
   }
}

function checkTargetLocale(theForm)
{     
    if (theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == "-1" ||
            theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == "" || 
            theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == null)
    {
        alert("<%= jsmsgTargetLocale %>");
        return false;
    }
    else 
    {
        return true;
    }
}

function confirmForm(formSent)
{
    activityChecked = false;
    for (i=0; i < formSent.length; i++)
    {
        if ((formSent.elements[i].type == "checkbox") &&
            (formSent.elements[i].checked == true) && (formSent.elements[i].name !== "selectAll"))
        {
            activityChecked = true;
            break;
        }
    }

    var confirmFlag = true;

    if (!activityChecked)
    {
        alert("<%= bundle.getString("jsmsg_users_activities") %>");
        confirmFlag = false;
    }
    else if (!isSelectionMade(formSent.selectSourceLocale)) 
    {
        alert("<%= bundle.getString("jsmsg_users_source_locale") %>");
        confirmFlag = false;
    }
    else if (!isSelectionMade(formSent.selectTargetLocale)) 
    {
        alert("<%= bundle.getString("jsmsg_users_target_locale") %>");     
        confirmFlag = false;
    }

    if (!confirmFlag)
    {
        needSubmit = false;
        var doneButton = document.getElementById("doneButton");
        doneButton.disabled = false;
    }
    
    return confirmFlag;
}
</SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
{
  var targetArrayText0 = new Array(selSourceFirst," ");
  var targetArrayValue0 = new Array("-1","-1");

  <%= (String) request.getAttribute("jsArrays") %>
}

function setTargets(selectedIndexSent)
{
	var i,j,textArr,valueArr,selLen;	
	
    if (document.layers)
    {
        for (i=0; i < (eval("targetArrayText" + selectedIndexSent)).length; i++)
        {
            document.contentLayer.document.userForm.selectTargetLocale.options[i].text = (eval("targetArrayText" + selectedIndexSent))[i];
            document.contentLayer.document.userForm.selectTargetLocale.options[i].value = (eval("targetArrayValue" + selectedIndexSent))[i];
        }
        for (i=(eval("targetArrayText" + selectedIndexSent)).length;
             i < document.contentLayer.document.userForm.selectTargetLocale.length; i++)
        {
            document.contentLayer.document.userForm.selectTargetLocale.options[i].text = "";
            document.contentLayer.document.userForm.selectTargetLocale.options[i].value = "";
        }
    }
    else
    {
		var select = document.userForm.selectTargetLocale;
    	if (selectedIndexSent==0)
    	{
    		select.size=1;
    		select.multiple="";
			select.onblur = "";
    		select.options.length = 1;    
    		select.options[0].text = selSourceFirst;
    		
    		return;
    	}
    	
    	textArr = eval("targetArrayText" + selectedIndexSent);
    	valueArr = eval("targetArrayValue" + selectedIndexSent);
    	selLen = textArr.length;
    	select.size = textArr.length;    	
    	select.options.length = 0;
    	select.options.length = selLen;
    	select.onblur = setRate;
    	select.multiple="multiple";

    	for (i=0,j=0; i < selLen;j++)
        {
			if(valueArr[j]==-1)
			{
				select.size--;
				select.options.length--;
				selLen--;
			}
			else
			{
				//IE11 does not support such an approach
    			option = document.createElement("option");
				option.text =  textArr[j];
				option.value = valueArr[j];
    			select.options[i] = option;
    			i++;
			}
        }
    	
        /*for (i=0; i < (eval("targetArrayText" + selectedIndexSent)).length; i++)
        {
            document.userForm.selectTargetLocale.options[i].text = (eval("targetArrayText" + selectedIndexSent))[i];
            document.userForm.selectTargetLocale.options[i].value = (eval("targetArrayValue" + selectedIndexSent))[i];
        }
        for (i=(eval("targetArrayText" + selectedIndexSent)).length;
             i < document.userForm.selectTargetLocale.length; i++)
        {
            document.userForm.selectTargetLocale.options[i].text = "";
            document.userForm.selectTargetLocale.options[i].value = "";
        }*/
    }
}

//for GBS-2599
$(document).ready(function(){
	$("#selectAll").click(function(){
		$(":checkbox[name!='selectAll']").each(function(){
			if($("#selectAll").attr("checked")){
				$(this).attr("checked",true);
			}else{
				$(this).attr("checked",false);
			}
		});      
	});
});
</SCRIPT>
<STYLE type="text/css">
.list {
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
    padding: 0px;
}
</STYLE>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides();">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

    <DIV CLASS="mainHeading"><%=title%></DIV>

<FORM NAME="userForm" ACTION="<%= doneURL %>" METHOD="post">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
<INPUT TYPE="hidden" NAME="<%=Modify2Handler.SOURCE_LOCALE%>" VALUE ="<%=sourceLocale%>" />
<INPUT TYPE="hidden" NAME="<%=Modify2Handler.TARGET_LOCALE%>" VALUE = "<%=targetLocale%>"/>
<INPUT TYPE="hidden" NAME="<%=Modify2Handler.COMPANY_ID%>" VALUE = "<%=selectedCompanyId%>"/>
<TR><%=warningInfo%></TR> <br>
  <TR>
   <TD>
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
	 <TR>
	  <TD>
	      <SPAN CLASS="standardText"><%= bundle.getString("lb_user_name") %>:&nbsp;<%= userName %></SPAN>
	  </TD>
	  <TD CLASS="standardText">
              
	  </TD>
	  <TD></TD>
	 </TR>
	 <TR>
	  <TD>
	      <SPAN CLASS="standardText"><%= bundle.getString("lb_role_companyname") %>:&nbsp;<%= (String) request.getAttribute("allRoleCompanyNames") %></SPAN>
	  </TD>
	  <TD>
             
	  </TD>
	  <TD></TD>
	 </TR>

  	 <TR>
	 	  <TD valign="top">
	 	     <table>
	 	   	  <tr>
	 	  		<div CLASS="standardText"><%= bundle.getString("lb_source_locale") %></div>
				<span CLASS="standardText">
			 	<SELECT NAME="selectSourceLocale" SIZE="1" CLASS="standardText" 
			 		onChange="<%=onClickString%>">
              		<OPTION VALUE="-1" SELECTED><%= lbChoose %></OPTION>
              		<%= initSourceOptions %>
             	</SELECT>
            	</span>
              </tr>
              <tr style="height:2px;"></tr>
              
              <!-- Activity Table -->
              <tr>                          
               <TD>
	  			<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="list">
				 	<TR CLASS=tableHeadingBasic>
		  					<TD><input type="checkbox" name="selectAll" id="selectAll"/><%=bundle.getString("lb_activity_types")%></TD>
		  				
		  				<% if(isJobCosting) { %>
		  				    <TD><%=bundle.getString("lb_expense")%></TD>
		  				<% } %>
					</TR>
					<TR>
		  				<TD>
							<%=(String)request.getAttribute("activities")%>
		  				</TD>
					</TR>
	  			</TABLE>
			   </TD>            
             </tr>
            </table>
          </TD>
          
		  <TD valign="top">
		  	<DIV CLASS="standardText">
		  		<%= bundle.getString("lb_target_locale") %>
		  		<% if(isModify == null || !isModify.booleanValue())
		  		   {
		  			 out.print(bundle.getString("lb_role_noteForExpense"));
				   }
				%>
		  	</DIV>
			<DIV CLASS="standardText">
			 <% if(isModify != null && isModify.booleanValue()){%>
			 	<SELECT NAME="selectTargetLocale" CLASS="standardText" size="1"
			 		ONCHANGE="javascript:setRate();" id="selectTargetLocale">
            		<%= initTargetOptions %>
             	</SELECT>
             <% }else{ %>
             	<SELECT NAME="selectTargetLocale" id="selectTargetLocale" CLASS="standardText" 
             			 onBlur="javascript:setRate();" size="1" >
            		<%= initTargetOptions %>
             	</SELECT>
             <%} %>
            </DIV>
          </TD>
	 </TR>
    </TABLE>
   </TD>
  </TR>
  <TR>
	
  </TR>
  <TR>
	<TD COLSPAN="2">&nbsp;</TD>
  </TR>
</TR>

</TABLE>
</FORM>
<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
    ONCLICK="location.replace('<%=cancelURL%>')">
<INPUT ID="doneButton" TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>"
    ONCLICK="this.disabled=true; submitForm();">


</DIV>
</BODY>
</HTML>

<SCRIPT LANGUAGE = "JavaScript">
var xmlHttp = false;
try {
  xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
} catch (e) {
  try {
    xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
  } catch (e2) {
    xmlHttp = false;
  }
}

if (!xmlHttp && typeof XMLHttpRequest != 'undefined') {
  xmlHttp = new XMLHttpRequest();
}
</SCRIPT>
