<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
         com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.WorkflowInfos,
         com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.FormUtil,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.everest.webapp.webnavigation.LinkHelper,
         java.util.Date,
         java.util.Vector,
         java.util.List,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         java.util.Locale,
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>
<jsp:useBean id="new5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="modify5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    // bring in "state" from session
    SessionManager sessionMgr = (SessionManager) request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
    String sourceCol = bundle.getString("lb_source_locale");
    String targetCol = bundle.getString("lb_target_locale");
    String nameCol = bundle.getString("lb_workflow_name");
    String workflowTypeCol = bundle.getString("lb_workflow_type");
    String title= bundle.getString("lb_add_edit_workflows");
    //String modify5URL = modify5.getPageURL();
    String saveURL = save.getPageURL() + "&action=save";
    String cancel5URL = cancel5.getPageURL();
    String pre5URL = pre5.getPageURL();
    String removeWorkflowURL = remove.getPageURL()+"&action=remove";
    String wizardTitle = bundle.getString("msg_loc_profiles_wizard_6_title");
    String lbPrevious = bundle.getString("lb_previous");  
    String lbCancel = bundle.getString("lb_cancel");  
    String lbSave = bundle.getString("lb_save");  
    String lbEdit = bundle.getString("lb_edit");  
    String lbAdd = bundle.getString("lb_add");  
    String lbRemove = bundle.getString("lb_remove");
    String modify5URL = modify5.getPageURL() + "&"
                    + LocProfileStateConstants.ACTION 
                    + "=" + LocProfileStateConstants.MODIFY_PROFILE_ACTION;
    String new5URL = new5.getPageURL() + "&"
                    + LocProfileStateConstants.ACTION 
                    + "=" + LocProfileStateConstants.MODIFY_PROFILE_ACTION;
    // Get information from the handler
    String source =  (String)sessionMgr.getAttribute(LocProfileStateConstants.SOURCE_LOCALE);
    Vector targets =  (Vector)sessionMgr.getAttribute(LocProfileStateConstants.TARGET_LOCALES);
    Vector names =  (Vector)sessionMgr.getAttribute(LocProfileStateConstants.WORKFLOW_NAMES);
    Vector targetObjs =  (Vector)sessionMgr.getAttribute(LocProfileStateConstants.TARGET_OBJECTS);
    Vector<WorkflowInfos> workflowInfos = (Vector<WorkflowInfos>)sessionMgr.getAttribute(LocProfileStateConstants.WORKFLOW_INFOS);
    Boolean isSamePM = (Boolean)sessionMgr.getAttribute(
      LocProfileStateConstants.IS_SAME_PROJECT_MANAGER);
    Boolean isSameProject = (Boolean)sessionMgr.getAttribute(
      LocProfileStateConstants.IS_SAME_PROJECT);
    
    // table width setting
    int tableWidth = 510;
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = true;
    var cancelName = "<%= bundle.getString("lb_loc_profile") %>";
    var previousName = "<%= bundle.getString("lb_workflows") %>";
    var objectName = cancelName;
    var guideNode = "locProfiles";
    var modifyUrls = new Array();
    var helpFile = "<%=bundle.getString("help_localized_workflow_add_edit")%>";

    function cancel()
    {
       //  Warn before really cancelling
       //  (Warning turned on/off based on needWarning flag above)
       //
       if (confirmJump())
       {
          if (document.layers) document.contentLayer.document.profileCancel.submit();
          else profileCancel.submit();
       }
    }
    function previous()
    {       
    objectName = previousName;
       if (confirmJump())
       {
          if (document.layers) document.contentLayer.document.profilePrev.submit();
          else profilePrev.submit();
       }
     else
     {
        objectName = cancelName;
     }
    }

    function sameProject()
    {
<%
       if (!isSameProject.booleanValue()) 
       {
%>
          alert("<%= bundle.getString("jsmsg_wf_template_add") %>");
          return false;
<%     }
       else
       {
%>
       return true;
<%
       }
%>
    }

    function samePM()
    {
       <%
       if (!isSamePM.booleanValue()) 
       {%>
          alert("<%= bundle.getString("jsmsg_wf_template_add") %>");
          return false;
     <%}%>
     
       return true;
    }
    
    function checkDisplayedTRCount()
    {
    	var check = false;
    	var count = 0;
    	for(var i = 0; i < <%=workflowInfos.size()%>; i++)
    	{
    		var trObj = document.getElementById("tr"+i);
    		if(trObj && trObj.style.display != "none")
    		{
    			count ++;
    		}
    	}
    	if(count > 1)
    	{
    		check = true;
    	}
    	return check;
    }
    
	function getWfidOfSelectedRadio()
	{
		var obj = new Object();
		if(WorkflowList.RadioBtn)
		{
			if(WorkflowList.RadioBtn.length > 1 && checkDisplayedTRCount())
			{
				for(var i = 0; i < WorkflowList.RadioBtn.length; i++)
				{
					if(WorkflowList.RadioBtn[i].checked == true)
					{	
              obj.checked = true;
              obj.wfid = WorkflowList.RadioBtn[i].getAttribute("wfid");
              return obj;
					}
				}
				
				obj.checked = false;
			  return null;
			}
			else
			{
				obj.checked = false;
				return obj;
			}
		}
	}
	
    function submitForm(selectedButton) 
    {
       var checked = false;        
       var selectedRadioBtn = null;
       if (WorkflowList.RadioBtn != null) 
       {
          // If more than one radio button is displayed, the length attribute of the 
          // radio button array will be non-zero, so find which 
          // one is checked
          if (WorkflowList.RadioBtn.length)
          {
              for (i = 0; !checked && i < WorkflowList.RadioBtn.length; i++) 
              {
                  if (WorkflowList.RadioBtn[i].checked == true) 
                  {
                      checked = true;
                      selectedRadioBtn = WorkflowList.RadioBtn[i].value;
                  }
              }
          }
          // If only one is displayed, there is no radio button array, so
          // just check if the single radio button is checked
          else 
          {
              if (WorkflowList.RadioBtn.checked == true)
              {
                  checked = true;
                  selectedRadioBtn = WorkflowList.RadioBtn.value;
              }
          }
       }
       
       // otherwise do the following
       if (selectedButton == 'modify' && !checked) 
       {
           alert("<%= bundle.getString("jsmsg_wf_template_select") %>");
           return false;
       }
       
      if (selectedButton=='modify')
      {
         WorkflowList.action = modifyUrls[selectedRadioBtn];
         WorkflowList.submit();
         return;
      }
      if (selectedButton=='new')
      {
         WorkflowList.action = "<%=new5URL%>";
         WorkflowList.submit();
         return;
      }
      var obj = getWfidOfSelectedRadio();
      if ( obj != null )
      {
          if(obj.checked && selectedButton=='remove')
          { 
      	     WorkflowList.action = "<%=removeWorkflowURL%>&wfTemplateId="+obj.wfid;
      	     WorkflowList.submit();
          }
          else
          {
        	 alert("<%=bundle.getString("jsmsg_l10n_profile_remove")%>");
         	 return;
          }
      } 
      else 
      {
          alert("<%= bundle.getString("jsmsg_wf_template_select") %>");
          return false;
      }
	}
    
    </SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">

<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<STYLE type="text/css">
.list 
{
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
</STYLE>

<FORM NAME="WorkflowList" METHOD="POST">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%= bundle.getString("lb_add_edit_workflows") %>
</SPAN>
<P>       
                
                <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
                <TR>
                <TD WIDTH=538>
                <%= bundle.getString("helper_text_add_edit_workflows") %>
                </TD>
                </TR>
                </TABLE>
                <P>

                <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
                    <TR>
                        <TD>
                        <!-- Data Table -->                
                        <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="5" CLASS="list" WIDTH="100%">
                                <TR CLASS="tableHeadingBasic" VALIGN="BOTTOM" STYLE="padding-bottom: 3px;">
                                    <TD>&nbsp;</TD>
                                    <%
                                    out.println("<TD>"+nameCol+"</TD>");
                                    out.println("<TD>"+sourceCol+"</TD>");
                                    out.println("<TD>"+targetCol+"</TD>");
                                    out.println("<TD>"+workflowTypeCol+"</TD>");
                                    %>
                                </TR>
                    
                                <%
                                   int i;
                                   int javascript_array_index = 0;
                                   String checked = "";
                                   for (i=0; i < targets.size(); i++, javascript_array_index++)
                                   {
                                	  WorkflowInfos workflowInfo = workflowInfos.elementAt(i);
                                      GlobalSightLocale tgt = (GlobalSightLocale)targetObjs.elementAt(i);
                                      String modifyURL = modify5URL + "&" + WorkflowTemplateConstants.TARGET_LOCALE  + "=" + tgt.getId();
                                      String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                                      String target = (String)targets.elementAt(i);
                                      String name = (String)names.elementAt(i);
                                      String display = "";
                                      if( ! workflowInfo.isActive())
                                      {
                                    	  display = "display:none;";
                                      }
                                      out.println("<TR id='tr"+i+"' STYLE='padding-bottom: 5px; padding-top: 5px;"+display+"' VALIGN=TOP BGCOLOR="+color+">");
                                      out.println("<TD><INPUT TYPE=RADIO " + checked + " NAME=RadioBtn wfid='" + workflowInfo.getWfId() + "' VALUE=\""+javascript_array_index+"\"></TD>");
                                 %>
                                  <SCRIPT LANGUAGE="JavaScript1.2">
                                    modifyUrls[<%=javascript_array_index%>] = "<%=modifyURL%>";
                                  </SCRIPT>
                                 <%
                                      out.println("<TD><SPAN CLASS=standardText>"+ name + "</SPAN></TD>");
                                      out.println("<TD><SPAN CLASS=standardText>"+ source + "</SPAN></TD>");
                                      out.println("<TD><SPAN CLASS=standardText>"+ target + "</SPAN></TD>");
                                      String workflowType = "Translation";
                                      out.println("<TD><SPAN CLASS=standardText>"+ workflowType + "</SPAN></TD>");
                                      out.println("</TR>");
                                   }
                                 %>        

                </TABLE>
                <!-- End Data Table -->
                </FORM>
                </TD>
                </TR>
                 <TR>
                     <TD ALIGN="RIGHT">
                        <FORM NAME="profileCancel" ACTION="<%=cancel5URL%>" METHOD="POST">
                            <INPUT TYPE="HIDDEN" NAME="Cancel" VALUE="Cancel">
                        </FORM>
                        <FORM NAME="profilePrev" ACTION="<%=pre5URL%>" METHOD="post">
                            <INPUT TYPE="HIDDEN" NAME="Previous" VALUE="Previous">
                        </FORM>
                        <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
                            ONCLICK="cancel()">  
                        <INPUT TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE= "<%=lbPrevious%>" 
                            ONCLICK="previous()">                            
                        <INPUT TYPE="BUTTON" NAME="<%=lbRemove%>" VALUE="<%=lbRemove%>..." 
                            ONCLICK="submitForm('remove')">
                        <INPUT TYPE="BUTTON" NAME="<%=lbEdit%>" VALUE="<%=lbEdit%>..." 
                            ONCLICK="submitForm('modify')">   
                        <INPUT TYPE="BUTTON" NAME="<%=lbAdd%>" VALUE="<%=lbAdd%>..." 
                            ONCLICK="submitForm('new')">
                        <INPUT TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>" 
                            onclick="if(sameProject() && samePM()){saveEditForm.submit()}">
                        <form name="saveEditForm" action="<%=saveURL%>" method="post">
                            <% String tokenName = FormUtil.getTokenName(FormUtil.Forms.EDIT_LOCALIZATION_PROFILE); %>
                            <input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
                        </form>
                        </TD>
                    </TR>
                </TABLE>
</DIV>
</BODY>
</HTML>
