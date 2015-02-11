<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
		    com.globalsight.everest.servlet.util.SessionManager,
		    com.globalsight.everest.servlet.util.ServerProxy,
		    com.globalsight.everest.webapp.WebAppConstants,
		    com.globalsight.everest.webapp.javabean.NavigationBean,
		    com.globalsight.everest.webapp.pagehandler.PageHandler,
		    com.globalsight.everest.comment.CommentUpload,
		    com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
		    com.globalsight.everest.comment.CommentFile,
		    com.globalsight.everest.foundation.WorkObject,
		    com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
		    com.globalsight.everest.comment.Comment,
		    com.globalsight.everest.foundation.User,
		    com.globalsight.util.resourcebundle.ResourceBundleConstants,
		    com.globalsight.util.resourcebundle.SystemResourceBundle,
		    com.globalsight.util.GlobalSightLocale,
		    com.globalsight.everest.jobhandler.Job,
		    com.globalsight.everest.taskmanager.Task,
		    com.globalsight.everest.workflowmanager.Workflow,
		    com.globalsight.util.AmbFileStoragePathUtils,
		    java.util.Locale,java.util.ResourceBundle,
		    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
		    com.globalsight.util.FormUtil"
		    session="true"
%>
<jsp:useBean id="referenceUpload" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobCommentsDone" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobCommentsCancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="delete" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>


<%
	int partialComment = WebAppConstants.PARTIAL_COMMENT_LENGTH;
	ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale) session
			.getAttribute(WebAppConstants.UILOCALE);
	// Use this information to create a temporary directory.
	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	User userWelcome = (User) sessionMgr.getAttribute(WebAppConstants.USER);
	String userId = userWelcome.getUserId();
	String lbJobName = bundle.getString("lb_job_name");
	//get comments
	String comments = (String) sessionMgr.getAttribute(WebAppConstants.TASK_COMMENT);
	String sampleComments = "";
	if (comments == null) {
		comments = request
				.getParameter(WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT);
		//request.setAttribute(
			//	WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT,	comments);
	}
	if (comments.length() < partialComment) {
		sampleComments = comments;
	} else {
		sampleComments = comments.substring(0, partialComment - 1)
				+ "...";
	}
	Comment comment = (Comment) sessionMgr.getAttribute("comment");
	WorkObject wo = (WorkObject) TaskHelper.retrieveObject(session,
			WebAppConstants.WORK_OBJECT);
	String wid = "";
	String doneUrl = "";
	String cancelUrl = "";
	String jobName = "";
	ArrayList<CommentFile> commentReferences = (ArrayList<CommentFile>) sessionMgr
			.getAttribute("commentReferences");
	if (commentReferences == null) {
		commentReferences = new ArrayList<CommentFile>();
	}
	boolean isTaskComment = true;
	String saveCommStatus = (String) request.getParameter(CommentConstants.SAVE_COMMENT_STATUS);
    if(saveCommStatus!=null)
    {
    	isTaskComment = false;
    }
    String url_upload = referenceUpload.getPageURL() + "&"
			+ CommentConstants.DELETE + "="
			+ WebAppConstants.COMMENT_REFERENCE_NO_DELETE + "&"
			+ WebAppConstants.COMMENT_REFERENCE_RESTRICTED + "="
			+ WebAppConstants.COMMENT_REFERENCE_FALSE + "&"
			+ CommentConstants.SAVE_COMMENT_STATUS + "=" + saveCommStatus + "&";

	String url_upload_true = referenceUpload.getPageURL() + "&"
			+ CommentConstants.DELETE + "="
			+ WebAppConstants.COMMENT_REFERENCE_NO_DELETE + "&"
			+ WebAppConstants.COMMENT_REFERENCE_RESTRICTED + "="
			+ WebAppConstants.COMMENT_REFERENCE_TRUE+ "&"
			+ CommentConstants.SAVE_COMMENT_STATUS + "=" + saveCommStatus + "&";
	
	String url_delete = delete.getPageURL() + "&"
			+ CommentConstants.DELETE + "="
			+ WebAppConstants.COMMENT_REFERENCE_DELETE+"&"
			+ CommentConstants.SAVE_COMMENT_STATUS + "=" + saveCommStatus;

	if (wo != null) {
		long companyId = -1;
		if (wo instanceof Task) {
			Task task = (Task) wo;
			wid = (new Long(task.getId())).toString();
			doneUrl = done.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					+ WebAppConstants.TASK_ACTION_SAVECOMMENT + "&"
					+ CommentConstants.SAVE_COMMENT_STATUS + "=" + saveCommStatus 
					//GBS 2913 add taskID and taskState
					+ "&" + WebAppConstants.TASK_ID
					+ "=" + task.getId()
					+ "&" + WebAppConstants.TASK_STATE
					+ "=" + task.getState()
					+ "&" + WebAppConstants.TASK_COMMENT
					+ "=" + comments
					+ "&toTask=ture";
			
			cancelUrl = cancel.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					+ WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL
					//GBS 2913 add taskID and taskState
					+ "&" + WebAppConstants.TASK_ID
					+ "=" + task.getId()
					+ "&" + WebAppConstants.TASK_STATE
					+ "=" + task.getState()
					+ "&" + WebAppConstants.TASK_COMMENT
					+ "=" + comments
					+ "&toTask=ture";
			//GBS 2913 add taskID and taskState
			url_upload +=  WebAppConstants.TASK_ID
						+ "=" + task.getId()
						+ "&" + WebAppConstants.TASK_STATE
						+ "=" + task.getState()
						+ "&" + WebAppConstants.TASK_COMMENT
						+ "=" + comments
						+ "&toTask=ture&";
			//GBS 2913 add taskID and taskState
			url_upload_true += WebAppConstants.TASK_ID
								+ "=" + task.getId()
								+ "&" + WebAppConstants.TASK_STATE
								+ "=" + task.getState()
								+ "&" + WebAppConstants.TASK_COMMENT
								+ "=" + comments
								+ "&toTask=ture&";
			//GBS 2913 add taskID and taskState
			url_delete += "&" + WebAppConstants.TASK_ID
						+ "=" + task.getId()
						+ "&" + WebAppConstants.TASK_STATE
						+ "=" + task.getState()
						+ "&" + WebAppConstants.TASK_COMMENT
						+ "=" + comments
						+ "&toTask=ture";
			if(comment != null){
				doneUrl += "&commentId="+comment.getId();
				cancelUrl += "&commentId="+comment.getId();
				url_upload += "&commentId="+comment.getId();
				url_upload_true += "&commentId="+comment.getId();
				url_delete += "&commentId="+comment.getId();
			}
			companyId = ((Task) wo).getCompanyId();
			jobName = ((Task) wo).getJobName();
		} else if (wo instanceof Job) {
			Job job = (Job) wo;
			wid = (new Long(job.getId())).toString();
			doneUrl = jobCommentsDone.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					//GBS 2913 add jobID
					+ WebAppConstants.TASK_ACTION_SAVECOMMENT + "&"
					+ WebAppConstants.JOB_ID + "="
					+ job.getId()
					+ "&" + WebAppConstants.TASK_COMMENT
					+ "=" + comments
					+ "&toJob=ture";
			
			cancelUrl = jobCommentsCancel.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					+ WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL+ "&"
					//GBS 2913 add jobID
					+ WebAppConstants.JOB_ID + "="
					+ job.getId()
					+ "&" + WebAppConstants.TASK_COMMENT
					+ "=" + comments
					+ "&toJob=ture";
			//GBS 2913 add jobID
			url_upload +=  WebAppConstants.JOB_ID + "="
						+ job.getId()
						+ "&" + WebAppConstants.TASK_COMMENT
						+ "=" + comments
						+ "&toJob=ture";
			//GBS 2913 add jobID
			url_upload_true += WebAppConstants.JOB_ID + "="
								+ job.getId()
								+ "&" + WebAppConstants.TASK_COMMENT
								+ "=" + comments
								+ "&toJob=ture";
			//GBS 2913 add jobID
			url_delete += "&" + WebAppConstants.JOB_ID + "="
						+ job.getId()
						+ "&" + WebAppConstants.TASK_COMMENT
						+ "=" + comments
						+ "&toJob=ture";
			
			if(comment != null){
				doneUrl += "&commentId="+comment.getId();
				cancelUrl += "&commentId="+comment.getId();
				url_upload += "&commentId="+comment.getId();
				url_upload_true += "&commentId="+comment.getId();
				url_delete += "&commentId="+comment.getId();
			}
			companyId = ((Job) wo).getCompanyId();
			jobName = ((Job) wo).getJobName();
		} else if (wo instanceof Workflow) {
			Workflow wf = (Workflow) wo;
			wid = (new Long(wf.getId())).toString();
			doneUrl = done.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					+ WebAppConstants.TASK_ACTION_SAVECOMMENT;
			cancelUrl = cancel.getPageURL() + "&"
					+ WebAppConstants.TASK_ACTION + "="
					+ WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL;

			companyId = ((Workflow) wo).getCompanyId();
			jobName = ((Workflow) wo).getJob().getJobName();
		}

		CompanyThreadLocal.getInstance().setIdValue(String.valueOf(companyId));
	}
	String tmpDir = WebAppConstants.COMMENT_REFERENCE_TEMP_DIR + wid
			+ userId;
	
	String lb_title = bundle.getString("lb_upload_comment_reference");

	String lb_help = bundle.getString("lb_help");
	String lb_cancel = bundle.getString("lb_cancel");
	String lb_done = bundle.getString("lb_done");
	String lb_attach = bundle.getString("lb_attach");
	String lb_fileName = bundle.getString("lb_filename");
	String lb_selectFile = bundle.getString("lb_select_file");
	String lb_fileNotSelected = bundle
			.getString("jsmsg_file_not_selected");
	String lb_duuudeDoYouWantToDoThis = bundle
			.getString("msg_remove_comment_reference_file");
	String lb_remove = bundle.getString("lb_remove");
	String lb_comments = bundle.getString("lb_comments");
	String lb_upload = bundle.getString("lb_upload");

	// msg_replace_image = "Please enter a file name."
	String lb_pleaseEnterData = bundle.getString("msg_replace_image");
%>
<!-- This JSP is: /envoy/administration/comment/addAnotherFile.jsp -->
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var helpFile = "<%=bundle.getString("help_job_comment_attach_files")%>";
var needWarning = false;
var guideNode = "myActivitiesComments";
var allFileNames = new Array;

reload = false;
function prepToClose()
{
  if (!reload)
  {
    window.close();
  }
}

function isReady(form)
{
    var field = form.filename;
    var filename = field.value;

    if (filename == null || filename == "")
    {
        alert("<%=lb_pleaseEnterData%>");
        field.focus();
        return false;
    }
    else
    {
        var names = document.getElementsByName("fName");
        var index = Math.max(filename.lastIndexOf('/'),filename.lastIndexOf('\\') );
        var fName = filename.substring(index + 1, filename.length);

        for (var i = 0; i < names.length; i++)
        {
            if (names[i].firstChild.nodeValue == fName)
            {
                alert("The file name(" + fName + ") already exists.");
                field.focus();
                return false;
            }
        }
    
        if(uploadForm.restricted.checked==true)
        {
           uploadForm.action = "<%=url_upload_true%>";
        }else
        {
           uploadForm.action = "<%=url_upload%>";
        }
        idBody.style.cursor = "wait";
        uploadForm.idSubmit.style.cursor = "wait";
        return true;
    }
}

function showMessage(message)
{
    if (message != "")
    {
        window.showModalDialog("/globalsight/envoy/administration/comment/commentUploadMessage.html",
            message,
            "center:yes; help:no; resizable:yes; status:no; " +
            "dialogWidth: 300px; dialogHeight: 180px; ");
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

function saveForm(p_action)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.uploadForm;
    }
    else
    {
        theForm = document.all.uploadForm;
    }
	if (p_action == "<%=WebAppConstants.COMMENT_REFERENCE_ACTION_DONE%>")
	{
       if (validateForm() == false)
           return;          

       uploadForm.action = "<%=doneUrl%>";
	}
	if (p_action == "<%=WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL%>")
	{
       uploadForm.action = "<%=cancelUrl%>";
	}
	theForm.submit();
}

function validateForm()
{
    if (theForm.filename.value != "")
    {
        msg = "\"" + theForm.filename.value + "\" " +  "<%=bundle.getString("jsmsg_file_not_uploaded")%>";
        if (!confirm(msg))
            return false;
    }
    return true;
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

    if (optionTest(theForm))
    {
        theForm.submit();
    }
}

function optionTest(formSent)
{
    var pageChecked = false;

    if (formSent.<%=CommentConstants.FILE_CHECKBOXES%>.value)
    {
        if (formSent.<%=CommentConstants.FILE_CHECKBOXES%>.checked)
        {
            pageChecked = true;
        }
    }
    else
    { 
        for (var i = 0;
             i < formSent.<%=CommentConstants.FILE_CHECKBOXES%>.length; i++)
        {
            if (formSent.<%=CommentConstants.FILE_CHECKBOXES%>[i].checked == true)
            {
                pageChecked = true;
                break;
            }
        }
    }

    if (pageChecked)
    {
        return confirm("<%=lb_duuudeDoYouWantToDoThis%>");
        
    }

    return(true);
}

function compareTrs(o1, o2)
{
var s1 = o1.cells[0].firstChild.firstChild.nodeValue;
var s2 = o2.cells[0].firstChild.firstChild.nodeValue;

return s1.localeCompare(s2);
}

function sort()
{
var otable = document.getElementById("allFiles");
var obody = otable.tBodies[0];
var orows = obody.rows;
var trs = new Array;
for(var i=0; i < orows.length; i++)
{
    trs.push(orows[i]);
}
trs.sort(compareTrs);

var of = document.createDocumentFragment();
for (var i=0; i < trs.length; i++)
{
    of.appendChild(trs[i]);
}

obody.appendChild(of);
}

</SCRIPT>

<SCRIPT LANGUAGE="JavaScript">
    function helpSwitch() 
    {  
       // The variable helpFile is defined in each JSP
       helpWindow = window.open(helpFile,'helpWindow','resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
       helpWindow.focus();
    }
</SCRIPT>
</HEAD>


<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    id="idBody" onLoad="loadGuides(); doOnload()" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=lbJobName%>: <%=jobName%>
<%
	if (wo instanceof Task) {
%>
  <br><br>
<%
	out.println(bundle.getString("lb_target_language"));
%>: 
<%
	out.println(((Task) wo).getTargetLocale().getDisplayName());
	}
%>
</SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
    <TR>
        <TD><SPAN CLASS="mainHeading"><%=lb_title%></SPAN></TD>
    </TR>
    <TR>
        <TD WIDTH=500>
            <%=bundle.getString("helper_text_comment_reference_upload")%>
        </TD>
    </TR>
</TABLE>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
    <TD><B><%=lb_comments%>:</B><BR><%=sampleComments%></TD>
</TR>
</TABLE>
<FORM action="<%=url_delete%>" METHOD="post" NAME="deleteForm">

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.EDIT_COMMENT); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
<!-- Table to align the Remove button -->
                                 
<%String commentId = (String) sessionMgr.getAttribute("commentId");
if (commentReferences != null && commentReferences.size() > 0) 
{
%>

<TABLE BORDER="0" CELLPADDING="2" CELLSPACING="0">
<TR>
<!-- Table for the data -->
<TD>
<TABLE id="allFiles" BORDER="0" CELLPADDING="3" CELLSPACING="0" STYLE="border: solid navy 1px">
	<thead>
		<TR CLASS="tableHeadingBasic">	  
		  <TD style="width:355px">
		    <%=lb_fileName%>
		  </TD>
		  <TD><%=bundle.getString("lb_restrict")%></TD>
		  <TD><%=bundle.getString("lb_remove")%></TD>
		</TR>
	</thead>
	<tbody>
<%
	int i = 0;
		for (Iterator it = commentReferences.iterator(); it.hasNext(); ++i) {
			CommentFile file = (CommentFile) it.next();

			out.print("<TR BGCOLOR='");
			if (i % 2 == 0) {
				out.print("#FFFFFF");
			} else {
				out.print("#EEEEEE");
			}
			out.print("'>");

			// Col 1: filename (as link)
			out.print("<TD>");
			out
					.print("<A name='fName' class='standardHREF' target='_blank' href='/globalsight/");
			out.print(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR
					+ "/");
			if (file.isSaved()) {
				out.print(commentId);
			} else {
				out.print(tmpDir);
			}
			out.print("/" + file.getFileAccess() + "/");
			out.print(file.getFilename());
			out.print("'>");
			out.print(EditUtil.encodeHtmlEntities(file.getFilename()));
			out.print("</A>");
			out.println("</TD>");

			// Col 2: Restrict check box.
            String checked = "";
            if (file.getFileAccess().equals(WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS)) 
            {
                checked = "checked";
            }
			out.print("<TD align=\"center\">");
			out.print("<input name=\"restrict\" value=\"" + file.getAbsolutePath().hashCode() + "\" type=\"checkbox\" " + checked + "/> ");
			out.println("</TD>");
			
	        // Col 3: Remove checkbox
            out.print("<TD align=\"center\"><INPUT TYPE='checkbox' CLASS='standardText' ");
            out.print("NAME='");
            out.print(CommentConstants.FILE_CHECKBOXES);
            out.print("' VALUE='");
            out.print(i);
            out.print("'>");
            out.println("</INPUT></TD>");

			out.println("</TR>");
		}
%>
</tbody>

<TR>
<TD COLSPAN=4 ALIGN="RIGHT">
</TR>
</TABLE>



</TD>
</TR>
<TR>
<TD ALIGN="RIGHT">
<INPUT TYPE="BUTTON" NAME="<%=lb_remove%>" VALUE="Save" 
                    ONCLICK="submitForm()"> 
</TD>
</TR>
</TABLE>

<script>
sort();
</script>
<%
	}
%>

</FORM>

<P>

<FORM NAME="uploadForm" METHOD="POST" ACTION="<%=url_upload%>"
        ENCTYPE="multipart/form-data" onSubmit="return isReady(this)"
        CLASS="standardText">

<INPUT TYPE="HIDDEN" NAME="saveCommStatus" VALUE="<%=saveCommStatus%>"></INPUT>
<INPUT TYPE="HIDDEN" NAME="tmpdir" VALUE="<%=tmpDir%>"></INPUT>
<P>
      <SPAN class="standardTextBold"><%=lb_selectFile%></SPAN>
<BR>
      <INPUT TYPE="file" SIZE="60" NAME="filename">
      <BR>
        <INPUT TYPE="CHECKBOX" NAME="restricted" VALUE="true" > <%=bundle.getString("lb_restrict_access")%>
      &nbsp;&nbsp;&nbsp;<INPUT TYPE="submit" VALUE="<%=lb_upload%>" name="idSubmit">
      <BR>
      <BR>
      <INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
        ONCLICK="saveForm('<%=WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL%>');">   
      <INPUT TYPE="BUTTON" NAME="<%=lb_done%>" VALUE="<%=lb_done%>" 
          ONCLICK="saveForm('<%=WebAppConstants.COMMENT_REFERENCE_ACTION_DONE%>');">  

</P>
</FORM>
<P></P>
</DIV>
</BODY>
</HTML>
