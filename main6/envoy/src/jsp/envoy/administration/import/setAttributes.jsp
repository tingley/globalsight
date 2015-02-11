<%@page import="com.globalsight.everest.servlet.util.SessionManager"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
    com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.util.GlobalSightLocale,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.everest.jobhandler.Job,
    com.globalsight.everest.page.TargetPage,com.globalsight.everest.permission.Permission,
    com.globalsight.everest.util.comparator.DefinedAttributeComparator,
    com.globalsight.everest.permission.PermissionSet,
    com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.cxe.entity.customAttribute.Condition,
    com.globalsight.cxe.entity.customAttribute.SelectOption,
    com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
    com.globalsight.cxe.entity.customAttribute.JobAttribute,
    com.globalsight.everest.company.CompanyWrapper,
    com.globalsight.cxe.entity.customAttribute.ListCondition,
    com.globalsight.everest.jobhandler.JobImpl,
    com.globalsight.cxe.entity.customAttribute.Attribute,
    com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeConstant,
    com.globalsight.cxe.entity.customAttribute.IntCondition,
    com.globalsight.cxe.entity.customAttribute.FloatCondition,
    com.globalsight.cxe.entity.customAttribute.FileCondition,
    com.globalsight.cxe.entity.customAttribute.TextCondition,
    com.globalsight.cxe.entity.customAttribute.DateCondition,
    com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeFileManager,
    com.globalsight.everest.webapp.pagehandler.administration.imp.SetAttributeHandler,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
    com.globalsight.everest.webapp.WebAppConstants,
    java.util.Locale,java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobName" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="previous" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="rssCancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cvsCancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
//hasAttributes
    boolean hasAttributes = (Boolean)request.getAttribute("hasAttributes");
    // has no attributes
	if (!hasAttributes)
	{
	    // Not from "Enter job name" UI, then go to "Enter job name".
	    if (request.getParameter("fromJobName") == null)
	    {
	        response.sendRedirect(jobName.getPageURL());
	    }
	    // From "Enter job name" UI by clicking "Previous", 
	    // so redirect to "Map selected files to file profiles" UI.
	    else
	    {
		    response.sendRedirect(previous.getPageURL());        
	    }
	}
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms = (PermissionSet) session
            .getAttribute(WebAppConstants.PERMISSIONS);

    String selfUrl = self.getPageURL();
    String editListUrl = selfUrl + "&action=" + AttributeConstant.EDIT_LIST;
    String editIntUrl = selfUrl + "&action=" + AttributeConstant.EDIT_INT;
    String editFloatUrl = selfUrl + "&action=" + AttributeConstant.EDIT_FLOAT;
    String editTextUrl = selfUrl + "&action=" + AttributeConstant.EDIT_TEXT;
    String editDateUrl = selfUrl + "&action=" + AttributeConstant.EDIT_DATE;
    String uploadFileUrl = selfUrl + "&action=" + AttributeConstant.EDIT_FILE;
    String getFilesUrl = selfUrl + "&action=" + AttributeConstant.GET_FILES;
    String deleteFilesUrl = selfUrl + "&action=" + AttributeConstant.DELETE_FILES;
    String cancelUrl = cancel.getPageURL() + "&pageAction=" + WebAppConstants.CANCEL;
    String nextUrl = selfUrl + "&action=" + AttributeConstant.NEXT;
    
    //Added by Vincent Yan, 2010/04/21
    //To cancel the job creation when it is RSS job
    String jobType = (String)sessionMgr.getAttribute("jobType");
    if (jobType != null && "rssJob".equals(jobType)) {
    	cancelUrl = rssCancel.getPageURL();
    } else if (jobType != null && "cvsJob".equals(jobType)) {
    	cancelUrl = cvsCancel.getPageURL();
    }
    
    String title = bundle.getString("lb_job_attributes");
    
    Map<String, JobAttribute> jobAttribues = (Map<String, JobAttribute>)sessionMgr.getAttribute(SetAttributeHandler.JOB_ATTRIBUTES);

    
    String uuid = (String) sessionMgr.getAttribute("uuid");
    String root = JobAttributeFileManager.getStorePath(uuid);
    
    boolean isSuperAtt = false;
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(
            WebAppConstants.PERMISSIONS);
%>
<!-- This is envoy\administration\import\setAttributes.jsp -->
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<style type="text/css">
@import url(/globalsight/dojo/resources/dojo.css);
@import url(/globalsight/dojox/form/resources/FileUploader.css);
@import url(/globalsight/dijit/themes/tundra/attribute.css);


.tundra .dijitButtonText {
    width:100%;
    height:20px;
	text-align: center;
	padding: 0 0.3em;
}
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script language="JavaScript" src="/globalsight/includes/report/calendar.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var guideNode = "import";
    var objectName = "";
    var helpFile = "<%=bundle.getString("help_set_attribute_screen")%>";
    dojo.require("dijit.Dialog");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.MultiSelect");
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dijit.form.TextBox");
    dojo.require("dojo.io.iframe");

	dojo.addOnLoad(
		function(){    
			dojo.byId("contentTable").style.display="";
		}	   	  
	);

    function editListValue(obj)
    {
    	var jsonOjb = eval("(" + obj + ")");

        dojo.xhrPost(
        {
           url:"<%=editListUrl%>",
           handleAs: "text", 
           content:jsonOjb,
           load:function(data)
           {
               var returnData = eval(data);
               if (returnData.error)
               {
            	   alert(returnData.error);
               }
               else
               {
            	   dijit.byId("selected" + jsonOjb.attributeName).setLabel(returnData.label);
               }
           },
           error:function(error)
           {
               alert(error.message);
           }
       });
    }

    function editIntValue(obj)
    {
       	var jsonOjb = eval("(" + obj + ")");

        dojo.xhrPost(
        {
           url:"<%=editIntUrl%>",
           handleAs: "text", 
           content:jsonOjb,
           load:function(data)
           {
               var returnData = eval(data);
               if (returnData.error)
               {
            	   alert(returnData.error);
               }
               else
               {
            	   dijit.byId("int" + jsonOjb.attributeName).setLabel(returnData.value);
               }
           },
           error:function(error)
           {
               alert("error4:" + error.message);
           }
       });
    }

    function editFloatValue(obj)
    {
       	var jsonOjb = eval("(" + obj + ")");

        dojo.xhrPost(
        {
           url:"<%=editFloatUrl%>",
           handleAs: "text", 
           content:jsonOjb,
           load:function(data)
           {
               var returnData = eval(data);
               if (returnData.error)
               {
            	   alert(returnData.error);
               }
               else
               {
            	   dijit.byId("float" + jsonOjb.attributeName).setLabel(returnData.value);
               }
           },
           error:function(error)
           {
               alert(error.message);
           }
       });
    }

    function editDateValue(obj)
    {
       	var jsonOjb = eval("(" + obj + ")");
       	jsonOjb.dateValue = dojo.byId("dateValue"+jsonOjb.attributeName).value;

        dojo.xhrPost(
        {
           url:"<%=editDateUrl%>",
           handleAs: "text", 
           content:jsonOjb,
           load:function(data)
           {
               var returnData = eval(data);
               if (returnData.error)
               {
            	   alert(returnData.error);
               }
               else
               {
            	   dijit.byId("date" + jsonOjb.attributeName).setLabel(returnData.value);
               }
           },
           error:function(error)
           {
               alert(error.message);
           }
       });
    }

    function editTextValue(obj)
    {
       	var jsonOjb = eval("(" + obj + ")");

        dojo.xhrPost(
        {
           url:"<%=editTextUrl%>",
           handleAs: "text", 
           content:jsonOjb,
           load:function(data)
           {
               var returnData = eval(data);
               if (returnData.error)
               {
            	   alert(returnData.error);
               }
               else
               {
            	   dijit.byId("text" + jsonOjb.attributeName).setLabel(returnData.value);
               }
           },
           error:function(error)
           {
               alert(error.message);
           }
       });
    }
    
    function uploadFile() 
    {
        dojo.io.iframe.send({
   		form: dojo.byId("uploadForm"),
   		url:  "<%=uploadFileUrl%>", 
           method: 'POST', 
           contentType: "multipart/form-data",
   		handleAs: "text",
   		handle: function(response, ioArgs){
   			if(response instanceof Error){
   				alert("Failed to upload file, please try later.");
   			}else{
   				var id = dojo.byId("attributeName").value;
   				var returnData = eval(response);
                if (returnData.error)
                {
             	   alert(returnData.error);
                }
                else
                {
             	    dojo.byId("file" + id).innerHTML = returnData.label;
             	    updateFiles(returnData.files);
                }
   			}
   		}
   	});
   }

   function showUploadfileDialog(attributeName)
   {
	   
	   var jsonOjb = {
		  attributeName : attributeName
	   }

	   dojo.xhrPost(
       {
          url:"<%=getFilesUrl%>",
          handleAs: "text", 
          content:jsonOjb,
          load:function(data)
          {
              var returnData = eval(data);
              if (returnData.error)
              {
           	      alert(returnData.error);
              }
              else
              {
            	  initFileDialog(attributeName, returnData);
	        	  dijit.byId('uploadFormDiv').show();
              }
          },
          error:function(error)
          {
              alert(error.message);
          }
      });
   }

   function updateFiles(files)
   {
	   var selectBox = dojo.byId("allFiles");
	   var options =  selectBox.options;
	   for (var i = options.length-1; i>=0; i--)
	   {
	       selectBox.remove(i);
	   }

	   for (var i = 0; i < files.length; i++)
	   {
		   addFile(files[i]);
	   }

	   setOptionColor();
	}

	function updateFilesLabel(files)
	{
		var label = "";
	    for (var i = 0; i < files.length; i++)
	    {
		    if (label.lenght > 0)
		    {
		    	label.concat("<br>");
			}
		    label.concat(files[i]);
	    }
	}
   
   function initFileDialog(attributeName, data)
   {
       dojo.byId("attributeName").value = attributeName;
       updateFiles(data.files);
   }

   function addFile(file)
   {
       var option = document.createElement("option");
       option.appendChild(document.createTextNode(file));
       option.setAttribute("value", file);
       dojo.byId("allFiles").appendChild(option);
   }

   function deleteSelectFiles()
   {
	   var selectFiles = new Array();
	   var selectBox = dojo.byId("allFiles");
	   var options = selectBox.options;
	   
	   for (var i = options.length-1; i>=0; i--)
	   {
		   if (options[i].selected)
		   {
			   selectFiles.push(options[i].value);
		   }
	   }

	   if (selectFiles.length < 1)
	   {
		   return;
	   }

	   var attributeName = dojo.byId("attributeName").value;
	   
	   var jsonOjb = {
		  attributeName : attributeName,
		  deleteFiles : selectFiles
	   }

       dojo.xhrPost(
       {
          url:"<%=deleteFilesUrl%>",
          handleAs: "text", 
          content:jsonOjb,
          load:function(data)
          {
              var returnData = eval(data);
              if (returnData.error)
              {
           	      alert(returnData.error);
              }
              else
              {
            	  updateFiles(returnData.files);
            	  dojo.byId("file" + attributeName).innerHTML = returnData.label;
              }
          },
          error:function(error)
          {
              alert(error.message);
          }
      });
   }

   function setOptionColor()
   {
   	   var options = dojo.byId("allFiles").options;
       var flag = true;
       for (var i = 0; i<options.length; i++)
       {
	   		if (flag)
	   		{
	   		    options[i].className="row1";
	   			flag = false;
	   		}
	           else
	   		{
	   			options[i].className="row2";
	   			flag = true;
	   		}
       }
   }

   function showCalendar1(attId) {
       var cal1 = new calendar2(dojo.byId("dateValue" + attId), dijit.byId("date" + attId), "openDropDown()");
       cal1.year_scroll = true;
       cal1.time_comp = true;
       cal1.popup();
   }

   function submitForm(action) 
   {    
      if (action == 'next') 
      {  
    	  CommentForm.action = "<%=jobName.getPageURL()%>";
    	  nextPage();
      }       
      else if (action == 'cancel')
      {
    	  CommentForm.action = "<%=cancelUrl %>";
    	  CommentForm.submit();
      }
      else if (action == 'previous') 
      {
    	  CommentForm.action = "<%=previous.getPageURL()%>";
    	  CommentForm.submit();
      }
   }

   function nextPage()
   {
	   var jsonOjb = {
				  action : "nextPage"
		};
	   dojo.xhrPost(
       {
          url:"<%=nextUrl%>",
          handleAs: "text", 
          content:jsonOjb,
          load:function(data)
          {
              if (data == "")
              {
            	  CommentForm.submit();
              }
              else
              {
            	  var returnData = eval(data);
                  if (returnData.error)
                  {
               	      alert(returnData.error);
                  }
              }
          },
          error:function(error)
          {
              alert(error.message);
          }
      });
   }
</SCRIPT>
</head>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides(); " class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
	
	<SPAN CLASS="mainHeading"><%=bundle.getString("lb_job_attributes")%></SPAN>
	<P></P>
	
	<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
	  <TR>
	    <TD WIDTH=500><%=bundle.getString("helper_text_set_attribute")%></TD>
	  </TR>
	</TABLE>
	<P></P>
<p>

    <table cellpadding=2 cellspacing=2 border=0 class="standardText" id="contentTable" width="650px;" style="display: none">
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td align="right">
            <amb:tableNav bean="<%=JobAttributeConstant.JOB_ATTRIBUTE_LIST%>" key="<%=JobAttributeConstant.JOB_ATTRIBUTES_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
          <amb:table bean="<%=JobAttributeConstant.JOB_ATTRIBUTE_LIST%>" id="attribute" key="<%=JobAttributeConstant.JOB_ATTRIBUTES_KEY%>"
             dataClass="com.globalsight.cxe.entity.customAttribute.Attribute" pageUrl="self"
             emptyTableMsg="msg_attribute_none_for_job">
           <amb:column label=""  width="1%">
                &nbsp;
            </amb:column>
            <amb:column label="lb_name"  width="15%" sortBy="<%=DefinedAttributeComparator.NAME%>">
                <%
                isSuperAtt = 1 == attribute.getCompanyId();
                
                if (isSuperAtt) {%>
                <div class="superAttribute">
                <%} %>
                <%=attribute.getDisplayName()%>
                <%if (isSuperAtt) {%>
                </div>
                <%} %>
            </amb:column>
             
            <amb:column label="lb_type"  width="15%" sortBy="<%=DefinedAttributeComparator.TYPE%>">
                <%if (isSuperAtt) {%>
                <div class="superAttribute">
                <%} %>
                <%=bundle.getString("lb_attribute_type_" + attribute.getType())%>
                <%if (isSuperAtt) {%>
                </div>
                <%} %>
            </amb:column>
            
            <amb:column label="lb_required"  width="15%" sortBy="<%=DefinedAttributeComparator.REQUIRED%>">
                <%if (isSuperAtt) {%>
                <div class="superAttribute">
                <%} 
                String key = attribute.isRequired() ? "lb_yes" : "lb_no";
                out.println(bundle.getString(key));
                if (isSuperAtt) {%>
                </div>
                <%}%>
            </amb:column>
            
            <amb:column label="lb_values">
                <%
                try
                {
                    JobAttribute jobAtt = jobAttribues.get(attribute.getName());
                    Condition condition = attribute.getCondition();
                    if (condition instanceof ListCondition)
                    {
                        Set<SelectOption> selectOptions = jobAtt.getOptionValues();
                        String listLabel = EditUtil.encodeHtmlEntities(jobAtt.getListLabel());
    					ListCondition listCondition = (ListCondition)condition;
						List<SelectOption> allOptions = listCondition.getSortedAllOptions();
						String mult = listCondition.isMultiple()? "dijit.form.MultiSelect MULTIPLE size=\"5\"" : "dijit.form.FilteringSelect";
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="selected<%=attribute.getName()%>" style="width:150px; text-align:left;" label="<%=listLabel%>">
    					    <div dojoType="dijit.TooltipDialog" title="Select Values" id="tooltipDlg<%=attribute.getName()%>"
    					    execute="editListValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					      	 <tr>
    					      	 <td>
    					      	   <input dojoType=dijit.form.TextBox type="hidden" name="attributeName" value="<%=attribute.getName()%>">
    					      	   <select dojoType=<%=mult%> name="selectOption" hasDownArrow="true" style="width:180px;">
	    							  <%
	    			                    for (SelectOption option : allOptions){
	    			                        String selected = selectOptions.contains(option) ? "selected" : "";%>
	    			                        <option value="<%=option.getValue()%>" <%=selected%>><%=EditUtil.encodeTohtml(option.getValue())%></option>
	    							  <%}%>
	    						   </select>
    					      	 </td>
    					      	 </tr>
    					      	 <tr style="padding-top: 10px;">
    					      	 <td align="center">
    					      	    <button dojoType=dijit.form.Button type="button" name="close" onclick="dijit.byId('selected<%=attribute.getName()%>').closeDropDown();"><%=bundle.getString("lb_close") %></button>
    					      	    <button dojoType=dijit.form.Button type="submit" name="submit"><%=bundle.getString("lb_save") %></button>
    					      	 </td>
    					      	 </tr>
    					      	 </table>
    					  </div>
    					</div>
                       <%
                    }
                    else if (condition instanceof IntCondition)
                    {
                        String label = jobAtt.getIntLabel();
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="int<%=attribute.getName()%>" style="width:120px; text-align:left;" label="<%=label%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editIntValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					             <input dojoType=dijit.form.TextBox type="hidden" name="attributeName" value="<%=attribute.getName()%>">
    					             <input dojoType=dijit.form.TextBox name="intValue" style="width:120px; height:25px;">
    					         </td>
    					         <td>    
    					             <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('int<%=attribute.getName()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>
    					             <button dojoType=dijit.form.Button type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%
                    }
                    else if (condition instanceof FloatCondition)
                    {
                        String label = jobAtt.getFloatLabel();
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="float<%=attribute.getName()%>" style="width:120px; text-align:left;" label="<%=label%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editFloatValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					             <input dojoType=dijit.form.TextBox type="hidden" name="attributeName" value="<%=attribute.getName()%>">
    					             <input dojoType=dijit.form.TextBox trim="true" name="floatValue" style="width:120px; height:25px;">
    					         </td>
    					         <td>
    					             <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('float<%=attribute.getName()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>   
    					             <button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%
                    }
                    else if (condition instanceof FileCondition)
                    {
                        String filesLabel = SetAttributeHandler.getFileLabel(root + "/" + attribute.getName());
                        %>
  					      <input type="hidden" id="jobAtt<%=attribute.getName()%>" name="attributeName" value="<%=attribute.getName()%>">
                          <div class="dijitReset dijitInline dijitButtonNode" 
                               onclick="showUploadfileDialog('<%=attribute.getName()%>')"
                               style="margin:3px; text-align:left;">
                              <div id="file<%=attribute.getName()%>">
                          		<%=filesLabel%>
                              </div>
                              <div style="width:35px;">&nbsp;</div>
                          </div>
                       <%
                    }
                    else if (condition instanceof TextCondition)
                    {
                        String lable = EditUtil.encodeHtmlEntities(jobAtt.getTextLabel());
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="text<%=attribute.getName()%>" style="min-width:120px; text-align:left;" label="<%=lable%>">
    					  <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editTextValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					            <input dojoType=dijit.form.TextBox type="hidden" name="attributeName" value="<%=attribute.getName()%>">
    					            <input dojoType=dijit.form.TextBox trim="true" name="textValue" style="width:120px; height:25px;">
    					         </td>
    					         <td>
    					            <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('text<%=attribute.getName()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>
    					      	    <button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%  					
                    }
                    else if (condition instanceof DateCondition)
                    {
                        String lable = jobAtt.getDateLabel();
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="date<%=attribute.getName()%>" style="width:120px; text-align:left;" label="<%=lable%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editDateValue(dojo.toJson(arguments[0], true));">					         
    					      	 <table>
    					         <tr valign="middle">
    					         <td >
    					            <input dojoType=dijit.form.TextBox type="hidden" name="attributeName" value="<%=attribute.getName()%>">
    					      	    <input id="dateValue<%=attribute.getName()%>" name="dateValue" style="width:150px; height:25px;" >
    					         </td>
    					         <td valign="middle"><IMG style='cursor:hand' align=top border=0 src="/globalsight/includes/Calendar.gif"  onclick="showCalendar1('<%=attribute.getName()%>')"></td>
    					         <td align="right"><button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('date<%=attribute.getName()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button></td>
    					         <td align="left"><button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button></td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <% 					
                    }
                }
                catch(Exception e)
                {
                    out.println(e.getMessage());
                }
                
	           %>
            </amb:column>
           </amb:table>
           </td>
         </tr> 
    </table>
    
<form name="CommentForm" method="post">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
<TR>
    <TD ALIGN="RIGHT">
        <INPUT TYPE="BUTTON" NAME="cancel" VALUE="<%=bundle.getString("lb_cancel")%>" 
            ONCLICK="submitForm('cancel')">   
        <INPUT TYPE="BUTTON" NAME="previous" VALUE="<%=bundle.getString("lb_previous")%>"
            ONCLICK="submitForm('previous')">
        <INPUT TYPE="BUTTON" NAME="next" VALUE="<%=bundle.getString("lb_next")%>"
            ONCLICK="submitForm('next')">   
   </TD> 
</TR>
</TABLE>
</form>

<div dojoType="dijit.Dialog" id="uploadFormDiv" title="<%=bundle.getString("lb_update_job_attributes") %>"
    execute="" style="display:none">
  
  <FORM NAME="uploadForm" METHOD="POST" ACTION="<%=uploadFileUrl%>"
        ENCTYPE="multipart/form-data" id="uploadForm">
  <input type="hidden" id="attributeName" name="attributeName" value="-1">
  <table style="width: 650px; ">
    <tr>
      <td colspan="2">
          <%=bundle.getString("lb_all_files") %>:
          <select name="allFiles" multiple="multiple" id="allFiles" size="15" style="width: 100%;">
		  </select>
		  <div align="right">
		  <button type="button" dojoType="dijit.form.Button" name="deleteFiles" id="deleteFiles" onclick="deleteSelectFiles()"><%=bundle.getString("lb_delete")%></button>
		  </div>
	  </td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>     
    </tr>
    <tr>
      <td colspan="2"  align="right">
          <%=bundle.getString("lb_file")%>:
          <input type="file" name="uploadFile" id="fileUploadDialog" size="55" style="height:24px;">
          <button dojoType="dijit.form.Button" type="button" onclick="uploadFile()"><%=bundle.getString("lb_upload")%></button>
          <button dojoType="dijit.form.Button" type="button" onclick="dijit.byId('uploadFormDiv').hide();"><%=bundle.getString("lb_close")%></button>
      </td>
    </tr>
  </table>
  </FORM>
</div>
</body>