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
<jsp:useBean id="jobDetailsPDFs" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
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
.fileDiv,.listDiv,.intDiv,.floatDiv,.textDiv,.dateDiv{
    width:auto !important;
	min-width:50px;
	display:inline-block;
 	min-height:20px;
	cursor:pointer;
	border-width: 1px;
	border-style: solid;
	border-color: #C0C0C0 #C0C0C0 #9B9B9B;
	-moz-border-top-colors: none;
	-moz-border-right-colors: none;
	-moz-border-bottom-colors: none;
	-moz-border-left-colors: none;
	border-image: none;
	border-radius: 2px;
	padding: 0.1em 0.2em 0.2em;
	background: #FFF url("dijit/themes/tundra/images/buttonEnabled.png") repeat-x scroll left bottom;
 }
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script language="JavaScript" src="/globalsight/includes/report/calendar.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var guideNode = "myJobs";
    var objectName = "";
    var helpFile = "<%=bundle.getString("help_job_attribute_screen")%>";
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
    <table cellpadding=2 cellspacing=2 border=0 id="contentTable" class="standardText" width="750px;">
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
                        String listLabel2=listLabel.replaceAll("&lt;br&gt;",",");
    					if (editable)
    					{
    					    ListCondition listCondition = (ListCondition)condition;
						    List<SelectOption> allOptions = listCondition.getSortedAllOptions();
						    String isMultiple = listCondition.isMultiple() ? "multiple='true' size='5' " : "size='5'";
                        %>
              				<div class="listDiv" id="selected<%=attribute.getId()%>"  style="text-align:left;"  onclick = "disPlayListInput('uptadeSelectedDiv<%=attribute.getId()%>');">
              					<% for(String label : listLabel2.split(",")){
              					%>
              							<%= label%> <br>
              				    <%	
              					   }
              					 %>
	              				<div class="standardText" title="Select Values" id="uptadeSelectedDiv<%=attribute.getId()%>" style="display:none;">
		                        	<table>
		                        		<tr valign="middle">
		                        			<td>
		                        				<input type="hidden" name="attributeId<%=attribute.getId()%>" id="attributeId<%=attribute.getId()%>"  value="<%=attribute.getId()%>">
		    					      	    	<input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId<%=attribute.getId()%>" value="<%=jobAtt.getId()%>">
			                        			<select  name="selectOption<%=attribute.getId()%>" id = "selectOption<%=attribute.getId()%>" hasDownArrow="true" style="width:100%;" <%= isMultiple%>>
					    						  <%
					    			                 for (SelectOption option : allOptions){
					    			                     String selected = selectOptions.contains(option) ? "selected" : "";%>
					    			                    <option value="<%=option.getId()%>" <%=selected%>><%=EditUtil.encodeTohtml(option.getValue())%></option>
					    						  <%}%>
					    						</select>
		                        			</td>
		                        		</tr>
	                        			<tr>
		                        			<td>
		                        				<input type="button" name = "selectedButtonClose" id = "selectedButtonClose" onclick = "$('#uptadeSelectedDiv<%=attribute.getId()%>').dialog('close')" value = "<%=bundle.getString("lb_close") %>">
		                        				<input type="button" name = "selectedButtonSave" id = "selectedButtonSave"  onclick = "editListValue(<%=attribute.getId()%>);" value = "<%=bundle.getString("lb_save") %>">
		                        			</td>
	                        			</tr>
		                        	</table>
	              				</div>
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
                        <div class="intDiv" id="intValue<%=attribute.getId()%>"  style="text-align:left;"  onclick = "disPlayInput('uptadeIntDiv<%=attribute.getId()%>');"><%=label%>
	              				<div title="Input Integer Value" id="uptadeIntDiv<%=attribute.getId()%>" style="display:none;">
		                        	<table>
		                        		<tr valign="middle">
		                        			<td>
		                        				<input type="hidden" name="attributeId<%=attribute.getId()%>" id="attributeId<%=attribute.getId()%>"  value="<%=attribute.getId()%>">
		    					      	    	<input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId<%=attribute.getId()%>" value="<%=jobAtt.getId()%>">
			                        			<input style="width:180px; height:25px;" name ="updateIntValue<%=attribute.getId()%>" id ="updateIntValue<%=attribute.getId()%>" value="<%=label%>">
		                        			</td>
		                        			<td>
		                        				<input type="button" name = "intButtonClose" id = "intButtonClose" onclick = "$('#uptadeIntDiv<%=attribute.getId()%>').dialog('close')" value = "<%=bundle.getString("lb_close") %>">
		                        				<input type="button" name = "intButtonSave" id = "intButtonSave"  onclick = "editIntValue(<%=attribute.getId()%>);" value = "<%=bundle.getString("lb_save") %>">
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
                        <div class="floatDiv" id="floatValue<%=attribute.getId()%>"  style="text-align:left;"  onclick = "disPlayInput('uptadeFloatDiv<%=attribute.getId()%>');"><%=label%>
	              				<div title="Input Float Value" id="uptadeFloatDiv<%=attribute.getId()%>" style="display:none;">
		                        	<table>
		                        		<tr valign="middle">
		                        			<td>
		                        				<input type="hidden" name="attributeId<%=attribute.getId()%>" id="attributeId<%=attribute.getId()%>"  value="<%=attribute.getId()%>">
		    					      	    	<input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId<%=attribute.getId()%>" value="<%=jobAtt.getId()%>">
			                        			<input style="width:180px; height:25px;" name ="updateFloatValue<%=attribute.getId()%>" id ="updateFloatValue<%=attribute.getId()%>" value="<%=label%>">
		                        			</td>
		                        			<td>
		                        				<input type="button" name = "floatButtonClose" id = "floatButtonClose" onclick = "$('#uptadeFloatDiv<%=attribute.getId()%>').dialog('close')" value = "<%=bundle.getString("lb_close") %>">
		                        				<input type="button" name = "floatButtonSave" id = "floatButtonSave"  onclick = "editFloatValue(<%=attribute.getId()%>);" value = "<%=bundle.getString("lb_save") %>">
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
                        	<div class="fileDiv"  id="file<%=attribute.getId()%>" onclick="showUploadfileDialog('<%=attribute.getId()%>')" >
                         		<%=filesLabel%>
                             </div>
                       <%}
    					else
    					{
    					    out.println(filesLabel);
    					}
                    }
                    else if (condition instanceof TextCondition)
                    {
                        String label = EditUtil.encodeHtmlEntities(jobAtt.getTextLabel());
                        if (editable)
    					{
                        %>
                        <div class="textDiv" id="textValue<%=attribute.getId()%>"  style="text-align:left;"  onclick = "disPlayInput('uptadeTextDiv<%=attribute.getId()%>');"><%=label%>
	              				<div title="Input Text Value" id="uptadeTextDiv<%=attribute.getId()%>" style="display:none;">
		                        	<table>
		                        		<tr valign="middle">
		                        			<td>
		                        				<input type="hidden" name="attributeId<%=attribute.getId()%>" id="attributeId<%=attribute.getId()%>"  value="<%=attribute.getId()%>">
		    					      	    	<input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId<%=attribute.getId()%>" value="<%=jobAtt.getId()%>">
			                        			<input style="width:180px; height:25px;" name ="updateTextValue" id ="updateTextValue<%=attribute.getId()%>" value="<%=label%>">
		                        			</td>
		                        			<td>
		                        				<input type="button" name = "textButtonClose" id = "textButtonClose" onclick = "$('#uptadeTextDiv<%=attribute.getId()%>').dialog('close')" value = "<%=bundle.getString("lb_close") %>">
		                        				<input type="button" name = "textButtonSave" id = "textButtonSave"  onclick = "editTextValue(<%=attribute.getId()%>);" value = "<%=bundle.getString("lb_save") %>">
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
                    else if (condition instanceof DateCondition)
                    {
                        String label = jobAtt.getDateLabel();
                        if (editable)
    					{
                        %>
                        <div class="dateDiv" id="dateValue<%=attribute.getId()%>"  style="text-align:left;width:100px;"  onclick = "disPlayInput('uptadeDateDiv<%=attribute.getId()%>');"><%=label%>
	              				<div title="Input Date Value" id="uptadeDateDiv<%=attribute.getId()%>" style="display:none;">
		                        	<table>
		                        		<tr valign="middle">
		                        			<td>
		                        				<input type="hidden" name="attributeId<%=attribute.getId()%>" id="attributeId<%=attribute.getId()%>"  value="<%=attribute.getId()%>">
		    					      	    	<input type="hidden" id="jobAtt<%=attribute.getId()%>" name="jobAttributeId<%=attribute.getId()%>" value="<%=jobAtt.getId()%>">
			                        			<input style="width:150px; height:25px;" name ="updateDateValue<%=attribute.getId()%>" id ="updateDateValue<%=attribute.getId()%>" value = "<%=label%>">
			                        			<IMG style='cursor:hand' align=top border=0 src="/globalsight/includes/Calendar.gif"  onclick="showCalendar1('updateDateValue<%=attribute.getId()%>')">
		                        			</td>
		                        			<td>
		                        				<input type="button" name = "dateButtonClose" id = "dateButtonClose" onclick = "$('#uptadeDateDiv<%=attribute.getId()%>').dialog('close')" value = "<%=bundle.getString("lb_close") %>">
		                        				<input type="button" name = "dateButtonSave" id = "dateButtonSave"  onclick = "editDateValue(<%=attribute.getId()%>);" value = "<%=bundle.getString("lb_save") %>">
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

<div id="uploadFormDiv" title="<%=bundle.getString("lb_update_job_attributes") %>"  execute="" style="display:none;">
  <form name="uploadForm" method="post" action="<%=uploadFileUrl%>" enctype="multipart/form-data" id="uploadForm"  target="ajaxUpload">
	  <input type="hidden" id="attributeId" name="attributeId" value="-1">
	  <input type="hidden" id="jobAttributeId" name="jobAttributeId" value="-1">
	  <table id = "uploadFormTable" style="width:97%;" class="standardText">
	    <tr>
	      <td colspan="2">
	          <%=bundle.getString("lb_all_files") %>:
	          <select name="allFiles" multiple="multiple" id="allFiles" size="15" style="width: 100%;">
			  </select>
			  <div align="right">
			  <input type="button" name="downloadFiles" id="downloadFiles" onclick="downloadSelectFiles()" value="<%=bundle.getString("lb_download")%>">
			  <input type="button" name="deleteFiles" id="deleteFiles" onclick="deleteSelectFiles()" value = "<%=bundle.getString("lb_delete")%>">
			  </div>
		  </td>
	    </tr>
	    <tr>
	      <td colspan="2">&nbsp;</td>     
	    </tr>
	    <tr>
	    	<td align="left" valign="middle">
	    		<%=bundle.getString("lb_file")%>:
	          <input type="file" name="uploadFile" size="60" id="fileUploadDialog" style="height:24px;width:300px">
	    	</td>
	      <td align="right" valign="middle">
	          <input type="button" onclick="uploadFileMethod()" value="<%=bundle.getString("lb_upload")%>">
	          <input type="button" onclick="$('#uploadFormDiv').dialog('close')" value="<%=bundle.getString("lb_close")%>">
	      </td>
	    </tr>
	  </table>
  </form>
</div>
<!-- for uploading file asynchronous -->
<iframe id="ajaxUpload" name="ajaxUpload" style="display:none"></iframe>

<Form name="downLoadForm" method="post" action="<%=downloadFilesUrl%>">
  <input type="hidden" id="jobAttributeId2" name="jobAttributeId" value="-1">
  <div style="display:none" id="downloadFiles">
      
  </div>
</Form>
</body>
<SCRIPT LANGUAGE="JavaScript">
function showUploadfileDialog(attributeId)
{
	   var jobAttributeId = document.getElementById("jobAtt" + attributeId).value
	   
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=getFilesUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	        	  initFileDialog(attributeId, jobAttributeId, returnData);
	        	  $("#uploadFormDiv").dialog({width:600, height:400,resizable:true});
	          }
		   },
	   	   error:function(error)
	       {
          	alert(error.message);
           }
		});
	   
}

function initFileDialog(attributeId, jobAttributeId, data)
{
   document.getElementById("attributeId").value = attributeId;
   document.getElementById("jobAttributeId").value = jobAttributeId;
    updateFiles(data.files);
}

function updateFiles(files)
{
	   var selectBox = document.getElementById("allFiles");
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

function update(files)
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

function addFile(file)
{
    var option = document.createElement("option");
    option.appendChild(document.createTextNode(file));
    option.setAttribute("value", file);
    document.getElementById("allFiles").appendChild(option);
}

function setOptionColor()
{
	   var options = document.getElementById("allFiles").options;
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


function uploadFileMethod() 
{
	$("#uploadForm").submit();
	setTimeout("getAllFiles()", 500);
}

function getAllFiles()
{
	   var attributeId = document.getElementById("attributeId").value;
	   var jobAttributeId = document.getElementById("jobAttributeId").value;
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=getFilesUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId,
		   success: function(data){
			  var id = document.getElementById("attributeId").value;
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	        	  document.getElementById("jobAttributeId").value = returnData.jobAttributeId;
	        	  document.getElementById("jobAtt" + id).value = returnData.jobAttributeId;
	        	  document.getElementById("file" + id).innerHTML = returnData.label;
            	  updateFiles(returnData.files);
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
}

function deleteSelectFiles()
{
	   var selectFiles = new Array();
	   var selectBox = document.getElementById("allFiles");
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
	   var attId = document.getElementById("attributeId").value;
	   var jobAttributeId = document.getElementById("jobAttributeId").value;
	   
	   var jsonOjb = {
		  attributeId : attId,
		  jobAttributeId : jobAttributeId,
		  deleteFiles : selectFiles
	   }
	   
	   if (selectFiles.length == 0)
	   {
		   return;
	   }
	
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=deleteFilesUrl%>",
		   traditional: true,
		   data: jsonOjb,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	        	  updateFiles(returnData.files);
	        	  document.getElementById("file" + attId).innerHTML = returnData.label;
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
	   var selectBox = document.getElementById("allFiles");
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

	   var downloadFiles = document.getElementById("downloadFiles");
	   document.getElementById("jobAttributeId2").value = document.getElementById("jobAttributeId").value;

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

function showCalendar1(inputId) {
    var cal1 = new calendar2(document.getElementById(inputId), "openDropDown()");
    cal1.year_scroll = true;
    cal1.time_comp = true;
    cal1.popup();
}

function editTextValue(textAttributeId)
{
	  var attributeId = document.getElementById("attributeId"+textAttributeId).value;
	  var jobAttributeId = document.getElementById("jobAtt"+textAttributeId).value;
	  var textValue = document.getElementById("updateTextValue"+textAttributeId).value;
	  
	  $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=editTextUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId+"&textValue="+textValue,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	   		  	 document.getElementById("textValue"+textAttributeId).innerHTML = returnData.value;
	   		     document.getElementById("jobAtt"+textAttributeId).value = returnData.jobAttributeId;
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
	 $('#uptadeTextDiv'+textAttributeId).dialog('close');
}

function editFloatValue(floatAttributeId)
{
	  var attributeId = document.getElementById("attributeId"+floatAttributeId).value;
	  var jobAttributeId = document.getElementById("jobAtt"+floatAttributeId).value;
	  var floatValue = document.getElementById("updateFloatValue"+floatAttributeId).value;
	  
	  $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=editFloatUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId+"&floatValue="+floatValue,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	   		  	 document.getElementById("floatValue"+floatAttributeId).innerHTML = returnData.value;
	   		     document.getElementById("jobAtt"+floatAttributeId).value = returnData.jobAttributeId;
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
	$('#uptadeFloatDiv'+floatAttributeId).dialog('close');
}

function editIntValue(intAttributeId)
{
	  var attributeId = document.getElementById("attributeId"+intAttributeId).value;
	  var jobAttributeId = document.getElementById("jobAtt"+intAttributeId).value;
	  var intValue = document.getElementById("updateIntValue"+intAttributeId).value;
	  
	  $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=editIntUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId+"&intValue="+intValue,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	   		  	 document.getElementById("intValue"+intAttributeId).innerHTML = returnData.value;
	   		     document.getElementById("jobAtt"+intAttributeId).value = returnData.jobAttributeId;
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
	 $('#uptadeIntDiv'+intAttributeId).dialog('close');
}


function editDateValue(dateAttributeId)
{
	  var attributeId = document.getElementById("attributeId"+dateAttributeId).value;
	  var jobAttributeId = document.getElementById("jobAtt"+dateAttributeId).value;
	  var dateValue = document.getElementById("updateDateValue"+dateAttributeId).value;
	  
	  $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=editDateUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId+"&dateValue="+dateValue,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	   		  	 document.getElementById("dateValue"+dateAttributeId).innerHTML = returnData.value;
	   		     document.getElementById("jobAtt"+dateAttributeId).value = returnData.jobAttributeId;
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
	  $('#uptadeDateDiv'+dateAttributeId).dialog('close');
}

function editListValue(listAttributeId)
{
	  var attributeId = document.getElementById("attributeId"+listAttributeId).value;
	  var jobAttributeId = document.getElementById("jobAtt"+listAttributeId).value;
	  var selectOption = $("#selectOption"+listAttributeId).val();
 
	  $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=editListUrl%>",
		   data: "attributeId="+attributeId+"&jobAttributeId="+jobAttributeId+"&selectOption="+selectOption,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	   		  	 document.getElementById("selected"+listAttributeId).innerHTML = returnData.label;
	   		     document.getElementById("jobAtt"+listAttributeId).value = returnData.jobAttributeId;
	          }
		   },
	   	   error:function(error)
	       {
          alert(error.message);
        }
		});
	$('#uptadeSelectedDiv'+listAttributeId).dialog('close');
}

function disPlayInput(inputId)
{
	   $("#"+inputId).dialog({width: 350, height: 100, resizable:false});
}

function disPlayListInput(inputId)
{
	   $("#"+inputId).dialog({width: 280, height: 190, resizable: true});
}
</SCRIPT>
