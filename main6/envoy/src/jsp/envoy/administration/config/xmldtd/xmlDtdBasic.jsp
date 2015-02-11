<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdConstant,
        com.globalsight.cxe.entity.xmldtd.XmlDtdImpl,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.DtdFileManager,
        com.globalsight.everest.util.comparator.XmlDtdFileComparator,
        java.util.*"
    session="true"
%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    String saveURL = save.getPageURL() + "&action=" + XmlDtdConstant.SAVE;
    String cancelURL = cancel.getPageURL() + "&action=" + XmlDtdConstant.CANCEL;
    String uploadURL = upload.getPageURL() + "&action=" + XmlDtdConstant.UPLOAD;
    String removeURl = remove.getPageURL() + "&action=" + XmlDtdConstant.REMOVE;
    String viewFileURl = self.getPageURL() + "&action=" + XmlDtdConstant.VIEW;
    String validateNameURL = self.getPageURL() + "&action=" + XmlDtdConstant.VALIDATE_NAME;
    
    XmlDtdImpl dtd = (XmlDtdImpl) request.getAttribute(XmlDtdConstant.DTD);   
    
    String title = null;
    boolean edit = false;
    if (dtd != null) {
        edit = true;
        title = bundle.getString("lb_edit_xml_dtd");
    } else {
        title = bundle.getString("lb_new_xml_dtd");
    }

    String dtdName = "";
    String desc = "";
    String dtdId = "-1";
    String isAddComment = "checked";
    String isSendEmail = "checked";
    String otherUrl = null;
    
    if (dtd != null) {
        
        dtdName = dtd.getName();
        if (dtdName == null){
            dtdName = "";
        }
            
        desc = dtd.getDescription();
        if (desc == null){
            desc = "";
        }
        
        dtdId = Long.toString(dtd.getId());
        
        isAddComment = dtd.isAddComment() ? "checked" : "";
        isSendEmail = dtd.isSendEmail() ? "checked" : "";
        otherUrl = "id=" + dtd.getId();
        validateNameURL += "&id=" + dtd.getId();
    }
    
    String helperText = bundle.getString("helper_text_xml_dtd");
%>
<html>
<!-- This is envoy\administration\config\xmldtd\xmlDtdBasic.jsp -->
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_xml_dtd")%>";
var guideNode="xmlDtds";
var helpFile = "<%=bundle.getString("help_xml_dtd_basic_screen")%>";
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        xmlForm.action = "<%=cancelURL%>";
        xmlForm.submit();
    }

    if (formAction == "save")
    {
        if (confirmForm())
        {
            validateName(true);
        }
    }
}

function validateName(isForm)
{
    dojo.xhrPost(
    {
        url:"<%=validateNameURL%>" + "&name=" + encodeURI(xmlForm.name.value),
        handleAs: "text", 
        load:function(data)
        {
            if (data=="")
            {
                if (isForm)
                {
                    xmlForm.action = "<%=saveURL%>";
                    xmlForm.submit();
                }
            }
            else
            {
                if (isForm)
                {
                    alert(data);
                }
                else
                {
                    document.getElementById("nameMsg").innerText=data;
                }
            }
        },
        error:function(error)
        {
            alert(error.message);
        }
    });
}

function confirmForm()
{
    if (isEmptyString(xmlForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_xmlrulefile_empty"))%>");
        xmlForm.name.value = "";
        xmlForm.name.focus();
        return false;
    }

    if (hasSpecialChars(xmlForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%> " +
              "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        xmlForm.name.focus();
        return false;
    }

    return true;
}

function isReady(form)
{
    var field = form.filename;
    var filename = field.value;

    if (filename == null || filename == "")
    {
        alert("<%=bundle.getString("msg_file_none")%>");
        field.focus();
        return false;
    }
    else
    {
        var names = document.getElementsByName("fName");

        var typeIndex = filename.lastIndexOf('.');
        var type = filename.substring(typeIndex + 1, filename.length);
        if (type != "dtd" && type != "zip")
        {
            alert('<%=bundle.getString("msg_support_dtd")%>');
            field.focus();
            return false;
        }
        
        var index = Math.max(filename.lastIndexOf('/'),filename.lastIndexOf('\\') );
        var fName = filename.substring(index + 1, filename.length);

        for (var i = 0; i < names.length; i++)
        {
            if (names[i].firstChild.nodeValue == fName)
            {
                alert("The file (" + fName + ") already exists.");
                field.focus();
                return false;
            }
        }
    
        return true;
    }
}

function loadPage()
{
    loadGuides();
    if (<%=!edit%>)
    {
        document.getElementById("fileListDiv").style.display = "none";
    }

    if(isFirefox)
    {
    	document.getElementById("filename").style.backgroundColor = "#ddd";//"F0EDED";
    }
}

function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = dtdFileForm.selectFileNames;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                {
                    selectedIndex.push(i);
                }
            }
        } 
        else 
        {
            if (boxes.checked) 
            {
                selectedIndex.push(0);
            }
        }
    }
    
    if (selectedIndex.length > 0)
    {
        document.getElementById("idRemoveSubmit").disabled = false;
    }
    else
    {
        document.getElementById("idRemoveSubmit").disabled = true;      
    }
}

function initNameMsg()
{
    document.getElementById("nameMsg").innerText="";
}

function checkName()
{
    validateName(false);
}

//for GBS-2599
function handleSelectAll() {
	if (dtdFileForm && dtdFileForm.selectAll) {
		if (dtdFileForm.selectAll.checked) {
			checkAllWithName('dtdFileForm', 'selectFileNames'); 
			setButtonState();
	    }
	    else {
			clearAll('dtdFileForm'); 
			setButtonState();
	    }
	}
}
</script>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadPage();">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>"/>
<span class=errorMsg></span>
 
<div style="float:left"> 
<FORM name="xmlForm" method="post" action="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">

    <tr>
        <td>
        <input type="hidden" name="id" value="<%=dtdId%>"> 
        <table border="0" cellspacing="2" cellpadding="2" class="standardText">
            <tr valign="top">
                <td>
                <table border="0" class="standardText" cellpadding="2">
                     <tr>
                        <td>&nbsp;</td>
                        <td class="errorMsg" id="nameMsg">&nbsp;</td>
                    </tr>
                    <tr>
                        <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
                        <td>
                            <input type="text" name="name" maxlength="40" value="<%=dtdName%>" style="width: 160px;" onfocus="initNameMsg()" onblur="checkName()">
                        </td>
                    </tr>
                    <tr>
                        <td><%=bundle.getString("lb_dtdTestFailed")%></td>
                        <td><input id="isAddComment" type="checkbox" name="addComment" value="true" <%=isAddComment %>><%=bundle.getString("lb_addComment")%></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td><input id="isSendEmail" type="checkbox" name="sendEmail" value="true" <%=isSendEmail %>><%=bundle.getString("lb_sendEmail")%></td>
                    </tr>
                    <tr>
                        <td valign="top"><%=bundle.getString("lb_description")%>:</td>
                        <td><textarea rows="8" style="width: 300px;" name="description"><%=desc%></textarea></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2"><input type="button" name="cancelBtn" value="<%=bundle.getString("lb_return")%>" onclick="submitForm('cancel')"> <input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="submitForm('save')"></td>
                    </tr>
                </table>
                </td>
            </tr>
        </table>
        </td>
    </tr>
</table>
</FORM>
</div>

<div id="fileListDiv" class="standardText" style="float:left; text-align:right; margin-left:20px;">

&nbsp;<amb:tableNav bean="<%=XmlDtdConstant.XMLDTD_File_LIST%>" key="<%=XmlDtdConstant.XMLDTDFILE_KEY%>" pageUrl="self" otherUrl="<%=otherUrl%>"/>

<form name="dtdFileForm" method="post" action="<%=removeURl%>">
    <input type="hidden" name="id" value="<%=dtdId%>"> 

    <amb:table bean="<%=XmlDtdConstant.XMLDTD_File_LIST%>" id="dtdFile" 
            key="<%=XmlDtdConstant.XMLDTDFILE_KEY%>" 
            dataClass="java.io.File"
            pageUrl="self"
            emptyTableMsg="msg_no_xmldtdfile" 
            width='560px"'
            otherUrl="<%=otherUrl%>"
            >
            
            <amb:column label="checkbox" width="1px;" align="center">
            <%String path = DtdFileManager.getDisplayPath(Long.parseLong(dtdId), dtdFile); %>
              <input type="checkbox" name="selectFileNames" value="<%=path%>" onclick="setButtonState();">
            </amb:column>      
            <amb:column label="lb_file_name" align="left" sortBy="<%=XmlDtdFileComparator.NAME%>" width="89%">
                <%
                String path = DtdFileManager.getDisplayPath(Long.parseLong(dtdId), dtdFile); 
                String fileUrl = viewFileURl + "&id=" + dtdId + "&fileName=" + path;%>
                <A name='fName' class='standardHREF' target='_blank' href="<%=fileUrl%>"><%=EditUtil.encodeHtmlEntities(path)%></A>        
            </amb:column>
            <amb:column label="">
              &nbsp;
            </amb:column>
    </amb:table>
    
    <!-- for GBS-2599
	DIV ID="CheckAllLayer" style="float: left; margin-left:10px;">
        <A CLASS="standardHREF"
           HREF="javascript:checkAllWithName('dtdFileForm', 'selectFileNames'); setButtonState()"><%=bundle.getString("lb_check_all")%></A> |
        <A CLASS="standardHREF"
           HREF="javascript:clearAll('dtdFileForm'); setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
    </DIV-->
    <div style="width:560px;*width:400px;">
    <INPUT id="idRemoveSubmit" TYPE="submit" VALUE="<%=bundle.getString("lb_remove")%>" style="margin-top:1px;" disabled="true">
    </div>
</form>

<div>
<FORM NAME="uploadForm" METHOD="POST" ACTION="<%=uploadURL%>"
        ENCTYPE="multipart/form-data" onSubmit="return isReady(this)"
        CLASS="standardText">
    <input type="hidden" name="id" value="<%=dtdId%>"> 
    <SPAN class="standardTextBold"><%=bundle.getString("lb_select_file")%></SPAN>
    <INPUT TYPE="file" SIZE="40" NAME="filename" id="filename" style="height:22px;">
    <INPUT TYPE="submit" VALUE="<%=bundle.getString("lb_upload")%>" name="idSubmit"  style="height:22px">
</FORM>
</div>
</div>

</div>
</body>
</html>
