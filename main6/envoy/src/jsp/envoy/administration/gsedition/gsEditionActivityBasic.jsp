<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.gsedition.*,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*,
                 com.globalsight.cxe.entity.fileprofile.FileProfile"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=modify" + "&GSEditionID=" + request.getParameter("gsID");
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_gsedition_action");
    }
    else
    {
        saveURL +=  "&action=create" + "&GSEditionID=" + request.getParameter("gsID");
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_gsedition_action");
    }

    String cancelURL = cancel.getPageURL() + "&action=cancel" 
                       + "&GSEditionID=" + request.getParameter("gsID");
    ArrayList names = (ArrayList)request.getAttribute("allGSEditionNames");
    GSEditionActivity gsEditionAcitivty = (GSEditionActivity)request.getAttribute("gsEditionActivity");
    String name = "",description = "";
    long  fileProfile = 0;
    int sourceFile = 0;
    long id = 0;
    long geEditionID = 0;

    if (edit)
    {
   	    id = gsEditionAcitivty.getId();
    	geEditionID = gsEditionAcitivty.getGsEdition().getId();
        name = gsEditionAcitivty.getName();
        fileProfile = gsEditionAcitivty.getFileProfile();
        sourceFile = gsEditionAcitivty.getSourceFileReference();
        description = gsEditionAcitivty.getDescription();
        saveURL =  saveURL + "&gsEditionActivityID=" + id;
    }
  
    ArrayList allGSEdition = (ArrayList)request.getAttribute("allGSEdition");
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script SRC="/globalsight/includes/Ajax.js"></script>
<script SRC="/globalsight/includes/dojo.js"></script>
<script SRC="/globalsight/includes/json2.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "";
var guideNode = "gsEditionAction";
var helpFile = "<%=bundle.getString("help_gsEdition_actionBasic")%>";

var FileProfileNameArray =new Array();

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
    	actionsForm.action = "<%=cancelURL%>";
    	actionsForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            actionsForm.fileprofileName.value = FileProfileNameArray[actionsForm.fileProfile.selectedIndex];
        	actionsForm.action = "<%=saveURL%>";
        	actionsForm.submit();
        }
    }
}

function confirmForm()
{
    if (isEmptyString(actionsForm.name.value))
    {
        alert("<%=bundle.getString("jsmsg_gsedition_action_name_warning")%>");
        actionsForm.name.value = "";
        actionsForm.name.focus();
        return false;
    }

    if (hasSpecialChars(actionsForm.name.value))
    {
        alert("<%=bundle.getString("lb_name")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    if (hasSpecialChars(actionsForm.description.value))
    {
        alert("<%=bundle.getString("lb_description")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }    

    //check name
<%
    if (names != null)
    {
    	for (int i = 0; i < names.size(); i++ ) 
    	{
    		String loopName = ((String) names.get(i)).toLowerCase();
  		%>
  		    var currentName = document.getElementById("idName");
  		    if (<%=edit%>) {
  	            if (currentName != null 
  	                    && currentName.value.toLowerCase() == "<%=loopName%>"
  	                    && "<%=name%>" != "" 
  	                    && currentName.value.toLowerCase() != "<%=name.toLowerCase()%>" )
  	            {
  	              	alert("<%=bundle.getString("jsmsg_duplicate_gs_edition_action_name")%>");
  	              	return false;
  	            }
  		    } else {
  	            if (currentName != null 
  	                    && currentName.value.toLowerCase() == "<%=loopName%>")
  	            {
  	              	alert("<%=bundle.getString("jsmsg_duplicate_gs_edition_action_name")%>");
  	              	return false;
  	            }
  		    }
   		<%
    	}
    }
%>
    //check gs edition
    var gsEdition = document.getElementById("idGSEdition");
    var GSEditionID = -1;
    if(gsEdition != null && gsEdition.length > 0) 
    {
        for (var i=0; i<gsEdition.length; i++)
        {
            if (gsEdition.options[i].selected) {
                GSEditionID = gsEdition.options[i].value;
            }
        }
    }
    if (GSEditionID == -1) {
        alert("<%=bundle.getString("lb_tm_gs_edition_not_selected")%>");
        return false;
    }

    //check file profile
    var remoteFP = document.getElementById("idFileProfile");
    var selectedRemoteFPID = -1;
    if (remoteFP != null && remoteFP.length > 0)
    {
        for (var j=0; j<remoteFP.length; j++)
        {
            if (remoteFP.options[j].selected) {
            	selectedRemoteFPID = remoteFP.options[j].value;
            }
        }
    }
    if (selectedRemoteFPID == -1) {
        alert("<%=bundle.getString("lb_tm_file_profile_not_selected")%>");
        return false;
    }

    return true;
}

function doOnload()
{
    loadGuides();
}

</script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>
<form name="actionsForm" method="post" action="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name") %>
            <span class="asterisk">*</span>:
          </td>
          <td>
	        <input type="textfield" id="idName" name="name" maxlength="40" size="30" value="<%=name%>">
          </td>
        </tr>
        
        <tr>
          <td valign="top"><%=bundle.getString("lb_gsedition")%><span class="asterisk">*</span>:
          </td>
          <td>
            <SELECT ID="idGSEdition" NAME="GSEdition" onchange="onChangeGSEditon()">
              <%
                if(allGSEdition != null) 
                {
                    for(int i = 0; i < allGSEdition.size(); i++) 
                    {
                        GSEdition ge = (GSEdition)allGSEdition.get(i);
                        String selected = "";
                        if(ge.getId() == geEditionID) {
                            selected = "selected";
                        }
              %>
			      <OPTION VALUE="<%=ge.getId()%>" <%=selected%>><%=ge.getName()%>
	          <%
			        }
		        }
			  %>
		        <INPUT TYPE=hidden VALUE="" NAME="fileprofileName" id="fileprofileName">
          </td>
        </tr>
        
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_gsEdition_acitivty_fileProfile") %><span class="asterisk">*</span>:
          </td>
          <td>
			    <SELECT ID="idFileProfile" NAME="fileProfile" >
			        <%
			        /*
			        if(request.getAttribute("xliffList") != null) {
			            String fileProfileSelected = "";
    			        HashMap ha = (HashMap)request.getAttribute("xliffList");
    			        Iterator itera = ha.entrySet().iterator();
    			        
    			        while(itera.hasNext()) {
    			            Map.Entry me = (Map.Entry)itera.next();
    			            
    			            if(((Long)me.getKey()) == fileProfile) {
    			                fileProfileSelected = "SELECTED";
    			            }
    			        */
			        %>
			        
			        <%
			        //    }
			        //}
			        %>
			    </SELECT>
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_gsEdition_acitivty_sourcefile") %><span class="asterisk">*</span>:
          </td>
          <td>
            <%
            String checked1="";
            String checked2="";
            if (sourceFile == 0) {
                checked1 = "CHECKED";
            }
            else {
                checked2 = "CHECKED";
            }
            %>
		          <INPUT type=radio name="sourceFileReference" value=0 <%=checked1%>><%=bundle.getString("lb_no")%>
		          <INPUT type=radio name="sourceFileReference" value=1 <%=checked2%>><%=bundle.getString("lb_yes")%>
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_description") %>:
          </td>
          <td>
		    <input type="textfield" name="description" maxlength="40" size="30" value="<%=description%>">
          </td>
        </tr>
        <tr><td colspan="2">&nbsp;</td></tr>
	<tr>
	  <td colspan="2">
	    <input type="button" id="idCancel" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
	    <input type="button" id="idSave" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</table>
</form>
<div id="loadingDiv" style="left:0;top:0; position:absolute">
     <span id="loadingWord" style="position:absolute"><font size="10">Loading...</font></span>
     <img id="my" src="/globalsight/images/loading.gif" border=0>
<div>
</div>
<script>
//set the loading div
loadingDiv.style.width = 400;
loadingDiv.style.height = 300;
loadingDiv.style.background = "#bbb9b9";
loadingDiv.style.opacity = "0.3";   
loadingDiv.style.filter = "Alpha(opacity=30)"; 
my.style.position = 'relative' ;
my.style.left = 100;
my.style.top = 260;
loadingWord.style.left = 140;
loadingWord.style.top = 190;
loadingDiv.style.display="none";

onChangeGSEditon();

function onChangeGSEditon() {
    var fileProfileSelect = document.getElementById("idFileProfile");
    fileProfileSelect.disabled = true;
    //disable "Cancel" and "Save" buttons to avoid clicking before ajax returns result.
    var cancelBtn = document.getElementById("idCancel");
    cancelBtn.disabled = true;
    var saveBtn = document.getElementById("idSave");
    saveBtn.disabled = true;

    var gsEdition = document.getElementById("idGSEdition");
    gsEdition.disabled = true;
    
    loadingDiv.style.display="block";
    
    if(gsEdition.length == 0)
    {
        alert("There is no GS Edition. Please configure it first.");
        loadingDiv.style.display="none";
        submitForm('cancel');
    }
    else 
    {
        var GSEditionID = -1;
        for (var i=0; i<gsEdition.length; i++)
        {
            if (gsEdition.options[i].selected) {
                GSEditionID = gsEdition.options[i].value;
            }
        }
        if (GSEditionID != -1) {
            var obj = {id:GSEditionID};
            sendAjax(obj, "getRemoteFileProfile", "getRemoteFileProfile");
        }
    }
}

function getRemoteFileProfile(data) {
	//Enable "Cancel" and "Save" buttons first.
    var cancelBtn = document.getElementById("idCancel");
    cancelBtn.disabled = false;
    var saveBtn = document.getElementById("idSave");
    saveBtn.disabled = false;
    
    var gsEdition = document.getElementById("idGSEdition");
    gsEdition.disabled = false;
    
    fileprofiles = eval(data);
    
    if(fileprofiles.length == 0) {
        loadingDiv.style.display="none";
        return;
    }
    
    var fileProfileSelect = document.getElementById("idFileProfile");
    
    var noXliffFile = fileprofiles[0].noXliffFile;

    if(noXliffFile == "true") {
        alert("<%=bundle.getString("lb_no_xliff_fileProfile")%>");
        loadingDiv.style.display="none";
        return;
    }
    
    var lowVersion = fileprofiles[0].lowVersion;

    if(lowVersion != null) {
        alert("<%=bundle.getString("lb_error_version_GSEdition")%>");
        fileProfileSelect.options.length = 0;
        loadingDiv.style.display="none";
        return;
    }
    
    var errorInfo = fileprofiles[0].errorInfo;
    
    if(errorInfo != null) {
        alert(errorInfo);
        fileProfileSelect.options.length = 0;
        loadingDiv.style.display="none";
        return;
    }
    
    fileProfileSelect.options.length = 0;

    if (fileProfileSelect != null)
    {
        for(var i = 0; i < fileprofiles.length; i++) 
        {
            var oOption = document.createElement("OPTION");
            
            if(fileprofiles[i].fileprofileID != null) {
                oOption.value = fileprofiles[i].fileprofileID;
                oOption.text = fileprofiles[i].fileprofileName;
                fileProfileSelect.options.add(oOption);
                FileProfileNameArray[i] = oOption.text;
                
                if(<%=fileProfile%> == oOption.value) {
                    oOption.selected  = true;
                }
            }
        }
    }
    
    fileProfileSelect.disabled = false;
    
    loadingDiv.style.display="none";
}
</script>

</body>
</html>
