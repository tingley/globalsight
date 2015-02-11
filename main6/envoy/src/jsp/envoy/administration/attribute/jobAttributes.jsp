<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
    com.globalsight.cxe.entity.customAttribute.AttributeClone,
    com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeConstant,
    com.globalsight.cxe.entity.customAttribute.IntCondition,
    com.globalsight.cxe.entity.customAttribute.FloatCondition,
    com.globalsight.cxe.entity.customAttribute.FileCondition,
    com.globalsight.cxe.entity.customAttribute.TextCondition,
    com.globalsight.cxe.entity.customAttribute.DateCondition,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
    com.globalsight.everest.webapp.WebAppConstants,
    java.text.MessageFormat,
    java.util.Locale,java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms = (PermissionSet) session
            .getAttribute(WebAppConstants.PERMISSIONS);

    String otherUrl = JobManagementHandler.JOB_ID + "="
            + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String selfUrl = self.getPageURL();
    String selfIdUrl = selfUrl + "&" + otherUrl;
    String jobDetailsURL = jobDetails.getPageURL() + "&" + otherUrl;
    String editListUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_LIST;
    String editIntUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_INT;
    String editFloatUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_FLOAT;
    String editTextUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_TEXT;
    String editDateUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_DATE;
    String uploadFileUrl = selfIdUrl + "&action=" + AttributeConstant.EDIT_FILE;
    String getFilesUrl = selfIdUrl + "&action=" + AttributeConstant.GET_FILES;
    String deleteFilesUrl = selfIdUrl + "&action=" + AttributeConstant.DELETE_FILES;
    String downloadFilesUrl = selfIdUrl + "&action=" + AttributeConstant.DOWNLOAD_FILES;
    
    String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");
	String jobReportsURL = jobReports.getPageURL() 
			+ "&" + JobManagementHandler.JOB_ID 
			+ "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    
    String title = bundle.getString("lb_job_attributes");

    // tab labels
    String labelDetails = bundle.getString("lb_details");
    String labelComments = bundle.getString("lb_comments");

    String labelJobName = bundle.getString("lb_job") + bundle.getString("lb_colon");
    JobImpl job =  (JobImpl)request.getAttribute("Job");
    Map<AttributeClone, JobAttribute> attributeMap = job.getAttriguteMap();

    boolean isSuperAtt = false;
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(
            WebAppConstants.PERMISSIONS);
    boolean editPerm = userPermissions.getPermissionFor(Permission.JOB_ATTRIBUTE_EDIT);
    boolean editable = false;
%>
<!-- This is envoy\administration\attribute\jobAttributes.jsp -->
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<style type="text/css">
@import url(/globalsight/dijit/themes/tundra/attribute.css);
@import url(/globalsight/dojox/form/resources/FileUploader.css);
@import url(/globalsight/dojo/resources/dojo.css);

.tundra .dijitButtonText {
    width:100%;
    height:20px;
	text-align: center;
	padding: 0 0.3em;
}
</style>
<script src="/globalsight/jquery/jquery-1.6.4.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script language="JavaScript" src="/globalsight/includes/report/calendar.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var guideNode = "myJobs";
    var objectName = "";
    var helpFile = "<%=bundle.getString("help_job_attribute_screen")%>";
    dojo.require("dijit.Dialog");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.MultiSelect");
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dijit.form.TextBox");
    dojo.require("dojo.io.iframe");

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
            	   dijit.byId("selected" + jsonOjb.attributeId).setLabel(returnData.label);
            	   dijit.byId("jobAtt" + jsonOjb.attributeId).setValue(returnData.jobAttributeId);
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
            	   dijit.byId("int" + jsonOjb.attributeId).setLabel(returnData.value);
            	   dijit.byId("jobAtt" + jsonOjb.attributeId).setValue(returnData.jobAttributeId);
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
            	   dijit.byId("float" + jsonOjb.attributeId).setLabel(returnData.value);
            	   dijit.byId("jobAtt" + jsonOjb.attributeId).setValue(returnData.jobAttributeId);
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
       	jsonOjb.dateValue = dojo.byId("dateValue"+jsonOjb.attributeId).value;

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
            	   dijit.byId("date" + jsonOjb.attributeId).setLabel(returnData.value);
            	   dijit.byId("jobAtt" + jsonOjb.attributeId).setValue(returnData.jobAttributeId);
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
            	   dijit.byId("text" + jsonOjb.attributeId).setLabel(returnData.value);
            	   dijit.byId("jobAtt" + jsonOjb.attributeId).setValue(returnData.jobAttributeId);
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
   				var id = dojo.byId("attributeId").value;
   				var returnData = eval(response);
                if (returnData.error)
                {
             	   alert(returnData.error);
                }
                else
                {
                	dojo.byId("jobAttributeId").value = returnData.jobAttributeId;
                    dojo.byId("jobAtt" + id).value = returnData.jobAttributeId;
             	    dojo.byId("file" + id).innerHTML = returnData.label;
             	    
             	    updateFiles(returnData.files);
                }
   			}
   		}
   	});
   }

   function showUploadfileDialog(attributeId)
   {
	   var jobAttributeId = dojo.byId("jobAtt" + attributeId).value
	   var jsonOjb = {
		  attributeId : attributeId,
		  jobAttributeId : jobAttributeId
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
            	  initFileDialog(attributeId, jobAttributeId, returnData);
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
   
   function initFileDialog(attributeId, jobAttributeId, data)
   {
       dojo.byId("attributeId").value = attributeId;
       dojo.byId("jobAttributeId").value = jobAttributeId;
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

	   var attId = dojo.byId("attributeId").value;
	   
	   var jsonOjb = {
		  attributeId : attId,
		  jobAttributeId : dojo.byId("jobAttributeId").value,
		  deleteFiles : selectFiles
	   }

	   if (selectFiles.length == 0)
	   {
		   return;
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
            	  dojo.byId("file" + attId).innerHTML = returnData.label;
              }
          },
          error:function(error)
          {
              alert(error.message);
          }
      });
   }

   function downloadSelectFiles()
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

	   if (selectFiles.length == 0)
	   {
		   return;
	   }

	   var downloadFiles = dojo.byId("downloadFiles");
	   dojo.byId("jobAttributeId2").value = dojo.byId("jobAttributeId").value;

	   downloadFiles.innerHTML = "";
	   
	   for (var i = options.length-1; i>=0; i--)
	   {
		   if (options[i].selected)
		   {
			   var fileBox = document.createElement("input");
			   fileBox.type = "checkbox";
			   fileBox.name = "selectFiles";
			   fileBox.value = options[i].value;
			   downloadFiles.appendChild(fileBox);
			   fileBox.checked = true;
		   }
	   }

	   downLoadForm.submit();
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
   
//jobSummary child page needed started
<amb:permission  name="<%=Permission.JOB_ATTRIBUTE_VIEW%>" >
$(document).ready(function(){
	$("#jobAttributesTab").removeClass("tableHeadingListOff");
	$("#jobAttributesTab").addClass("tableHeadingListOn");
	$("#jobAttributesTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobAttributesTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})
</amb:permission>

//jobSummary child page needed end.
</SCRIPT>
</head>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides(); " class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<div id="includeSummaryTabs">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<form name="CommentForm" method="post">
<!-- Comments data table -->
    <table cellpadding=2 cellspacing=2 border=0 id="contentTable" class="standardText" width="650px;">
        <tr><td>&nbsp;</td></tr>
        <tr>
            <td><b><%=bundle.getString("lb_job_attributes") %>
            </b></td>
        </tr>

        <tr>
          <td align="right">
            <amb:tableNav bean="<%=JobAttributeConstant.JOB_ATTRIBUTE_LIST%>" key="<%=JobAttributeConstant.JOB_ATTRIBUTES_KEY%>"
                 pageUrl="self" otherUrl="<%=otherUrl%>"/>
          </td>
        </tr>
        <tr>
          <td>
          <amb:table bean="<%=JobAttributeConstant.JOB_ATTRIBUTE_LIST%>" id="attribute" key="<%=JobAttributeConstant.JOB_ATTRIBUTES_KEY%>"
             dataClass="com.globalsight.cxe.entity.customAttribute.AttributeClone" pageUrl="self"
             emptyTableMsg="msg_attribute_none_for_job" otherUrl="<%=otherUrl%>">
           <amb:column label=""  width="1%">
                &nbsp;
            </amb:column>
            <amb:column label="lb_name"  width="15%" sortBy="<%=DefinedAttributeComparator.NAME%>">
                <%
                isSuperAtt = CompanyWrapper.SUPER_COMPANY_ID.equals(attribute.getCompanyId());
                editable = editPerm && attribute.getEditable();
                
                if (isSuperAtt) {%>
                <div class="superAttribute">
                <%} %>
                <%=attribute.getDisplayName()%>
                <%if (isSuperAtt) {%>
                </div>
                <%} %>
            </amb:column>
            <amb:column label=""  width="1%">
                &nbsp;
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
            <amb:column label=""  width="1%">
                &nbsp;
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
            <amb:column label=""  width="1%">
                &nbsp;
            </amb:column>
            <amb:column label="lb_values">
                <%
                try
                {
                    JobAttribute jobAtt = attributeMap.get(attribute);
                    if (jobAtt == null)
                    {
                        jobAtt = new JobAttribute();
                    }

                    Condition condition = attribute.getCondition();
                    if (condition instanceof ListCondition)
                    {
                        Set<SelectOption> selectOptions = jobAtt.getOptionValues();
                        String listLabel = EditUtil.encodeHtmlEntities(jobAtt.getListLabel());
    					if (editable)
    					{
    					    ListCondition listCondition = (ListCondition)condition;
						    List<SelectOption> allOptions = listCondition.getSortedAllOptions();
						    String mult = listCondition.isMultiple()? "dijit.form.MultiSelect MULTIPLE size=\"5\"" : "dijit.form.FilteringSelect";
                        %>
	    					<div dojoType="dijit.form.DropDownButton" id="selected<%=attribute.getId()%>" style="width:180px;text-align:left;" label="<%=listLabel%>">
	    					    <div dojoType="dijit.TooltipDialog" title="Select Values" id="tooltipDlg<%=attribute.getId()%>"
	    					    execute="editListValue(dojo.toJson(arguments[0], true));">					         
	    					      	 <table>
	    					      	 <tr>
	    					      	 <td>
	    					      	   <input dojoType=dijit.form.TextBox type="hidden" name="attributeId" value="<%=attribute.getId()%>">
	    					      	   <input dojoType=dijit.form.TextBox type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
	    					      	   <select dojoType=<%=mult%> name="selectOption" hasDownArrow="true" style="width:180px;">
		    							  <%
		    			                    for (SelectOption option : allOptions){
		    			                        String selected = selectOptions.contains(option) ? "selected" : "";%>
		    			                        <option value="<%=option.getId()%>" <%=selected%>><%=EditUtil.encodeTohtml(option.getValue())%></option>
		    							  <%}%>
		    						   </select>
	    					      	 </td>
	    					      	 </tr>
	    					      	 <tr style="padding-top: 10px;">
	    					      	 <td align="center">
	    					      	     <button dojoType=dijit.form.Button type="button" name="close" onclick="dijit.byId('selected<%=attribute.getId()%>').closeDropDown();"><%=bundle.getString("lb_close") %></button>
	    					      	     <button dojoType=dijit.form.Button type="submit" name="submit"><%=bundle.getString("lb_save") %></button>
	    					      	 </td>
	    					      	 </tr>
	    					      	 </table>
	    					   </div>
                       <%}
    					else
    					{
    					    out.println(listLabel);
    					}
                    }
                    else if (condition instanceof IntCondition)
                    {
                        String label = jobAtt.getIntLabel();
                        if (editable)
    					{
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="int<%=attribute.getId()%>" style="width:120px; text-align:left;" label="<%=label%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editIntValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					             <input dojoType=dijit.form.TextBox type="hidden" name="attributeId" value="<%=attribute.getId()%>">
    					      	     <input dojoType=dijit.form.TextBox type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
    					             <input dojoType=dijit.form.TextBox name="intValue" style="width:120px; height:25px;">
    					         </td>
    					         <td>    
    					             <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('int<%=attribute.getId()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>
    					             <button dojoType=dijit.form.Button type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%}
    					else
    					{
    					    out.println(label);
    					}
                    }
                    else if (condition instanceof FloatCondition)
                    {
                        String label = jobAtt.getFloatLabel();
                        if (editable)
    					{
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="float<%=attribute.getId()%>" style="width:120px; text-align:left;" label="<%=label%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editFloatValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					             <input dojoType=dijit.form.TextBox type="hidden" name="attributeId" value="<%=attribute.getId()%>">
    					      	     <input dojoType=dijit.form.TextBox type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
    					             <input dojoType=dijit.form.TextBox trim="true" name="floatValue" style="width:120px; height:25px;">
    					         </td>
    					         <td>   
    					             <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('float<%=attribute.getId()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>
    					             <button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%}
    					else
    					{
    					    out.println(label);
    					}
                    }
                    else if (condition instanceof FileCondition)
                    {
                        String filesLabel = jobAtt.getFilesLabel();
                        if (editable)
    					{
                        %>
  					      <input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
                          <div class="dijitReset dijitInline dijitButtonNode" 
                               onclick="showUploadfileDialog('<%=attribute.getId()%>')"
                               style="margin:3px; text-align:left;">
                              <div id="file<%=attribute.getId()%>">
                          		<%=filesLabel%>
                              </div>
                              <div style="width:30px;">&nbsp;</div>
                          </div>
                          
                          
                       <%}
    					else
    					{
    					    out.println(filesLabel);
    					}
                    }
                    else if (condition instanceof TextCondition)
                    {
                        String lable = EditUtil.encodeHtmlEntities(jobAtt.getTextLabel());
                        if (editable)
    					{
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="text<%=attribute.getId()%>" style="min-width:120px; text-align:left;" label="<%=lable%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editTextValue(dojo.toJson(arguments[0], true));">					         
    					         <table>
    					         <tr valign="middle">
    					         <td valign="middle">
    					            <input dojoType=dijit.form.TextBox type="hidden" name="attributeId" value="<%=attribute.getId()%>">
    					      	    <input dojoType=dijit.form.TextBox type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
    					            <input dojoType=dijit.form.TextBox trim="true" name="textValue" style="width:120px; height:25px; ma">
    					         </td>
    					         <td>
    					            <button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('text<%=attribute.getId()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button>
    					      	    <button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button>
    					         </td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%}
    					else
    					{
    					    out.println(lable);
    					}   					
                    }
                    else if (condition instanceof DateCondition)
                    {
                        String lable = jobAtt.getDateLabel();
                        if (editable)
    					{
                        %>
    					<div dojoType="dijit.form.DropDownButton" id="date<%=attribute.getId()%>" style="width:120px; text-align:left;" label="<%=lable%>">
    					    <div dojoType="dijit.TooltipDialog" title="<%=bundle.getString("lb_update_job_attributes") %>"
    					    execute="editDateValue(dojo.toJson(arguments[0], true));">					         
     					      	 <table>
    					         <tr valign="middle">
    					         <td >
    					            <input dojoType=dijit.form.TextBox type="hidden" name="attributeId" value="<%=attribute.getId()%>">
    					      	    <input dojoType=dijit.form.TextBox type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId" value="<%=jobAtt.getId()%>">
    					      	    <input id="dateValue<%=attribute.getId()%>" name="dateValue" style="width:150px; height:25px;" >
    					         </td>
    					         <td valign="middle"><IMG style='cursor:hand' align=top border=0 src="/globalsight/includes/Calendar.gif"  onclick="showCalendar1('<%=attribute.getId()%>')"></td>
    					         <td align="right"><button dojoType=dijit.form.Button trim="true" type="button" name="button" onclick="dijit.byId('date<%=attribute.getId()%>').closeDropDown()"><%=bundle.getString("lb_close") %></button></td>
    					         <td align="left"><button dojoType=dijit.form.Button trim="true" type="submit" name="submit" ><%=bundle.getString("lb_save") %></button></td>
    					         </tr>
    					         </table>
    					  </div>
    					</div>
                       <%}
    					else
    					{
    					    out.println(lable);
    					}   					
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
</form>

<div dojoType="dijit.Dialog" id="uploadFormDiv" title="<%=bundle.getString("lb_update_job_attributes") %>"
    execute="" style="display:none">
  
  <FORM NAME="uploadForm" METHOD="POST" ACTION="<%=uploadFileUrl%>"
        ENCTYPE="multipart/form-data" id="uploadForm">
  <input type="hidden" id="attributeId" name="attributeId" value="-1">
  <input type="hidden" id="jobAttributeId" name="jobAttributeId" value="-1">
  <table style="width: 650px; ">
    <tr>
      <td colspan="2">
          <%=bundle.getString("lb_all_files") %>:
          <select name="allFiles" multiple="multiple" id="allFiles" size="15" style="width: 100%;">
		  </select>
		  <div align="right">
		  <button type="button" dojoType="dijit.form.Button" name="downloadFiles" id="downloadFiles" onclick="downloadSelectFiles()"><%=bundle.getString("lb_download")%></button>
		  <button type="button" dojoType="dijit.form.Button" name="deleteFiles" id="deleteFiles" onclick="deleteSelectFiles()"><%=bundle.getString("lb_delete")%></button>
		  </div>
	  </td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>     
    </tr>
    <tr>
      <td colspan="2"  align="right" valign="middle">
          <%=bundle.getString("lb_file")%>:
          <input type="file" name="uploadFile" size="55" id="fileUploadDialog" style="height:24px;">
          <button dojoType="dijit.form.Button" type="button" onclick="uploadFile()"><%=bundle.getString("lb_upload")%></button>
          <button dojoType="dijit.form.Button" type="button" onclick="dijit.byId('uploadFormDiv').hide();"><%=bundle.getString("lb_close")%></button>
      </td>
    </tr>
  </table>
  </FORM>
</div>

<Form name="downLoadForm" method="post" action="<%=downloadFilesUrl%>">
  <input type="hidden" id="jobAttributeId2" name="jobAttributeId" value="-1">
  <div style="display:none" id="downloadFiles">
      
  </div>
</Form>
</body>
