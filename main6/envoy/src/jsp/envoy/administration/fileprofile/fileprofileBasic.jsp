<%@page import="com.globalsight.cxe.entity.fileprofile.FileProfileImpl"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.cxe.entity.fileextension.FileExtension,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile,
            com.globalsight.cxe.entity.knownformattype.KnownFormatType,
            com.globalsight.util.collections.HashtableValueOrderWalker,
            com.globalsight.everest.localemgr.CodeSet,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.comparator.StringComparator,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.FormUtil,
            com.globalsight.util.SortUtil,
            com.globalsight.util.GeneralException,
            com.globalsight.cxe.entity.xmldtd.XmlDtdImpl,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.everest.company.CompanyThreadLocal,
            java.util.*,
            com.globalsight.cxe.entity.filterconfiguration.Filter,
            com.globalsight.everest.foundation.L10nProfile,
            com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper,
            java.lang.*,
            java.io.*,
            java.util.Map,
            java.util.ArrayList"
    session="true"
%>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session); 
    // bring in "state" from session
    SessionManager sessionMgr =
      (SessionManager) request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);

    String saveURL = save.getPageURL();
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String selfURL = self.getPageURL()  + "&action=verify";

    String lbCancel = bundle.getString("lb_cancel");
    String lbSave = bundle.getString("lb_save");

    boolean edit = false;
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_file_profile");
        saveURL += "&action=edit";
    }
    else
    {
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_file_profile");
        saveURL += "&action=create";
    }

    // Data
    HashtableValueOrderWalker locProfiles =
      (HashtableValueOrderWalker)request.getAttribute("locProfiles");
    ArrayList names = (ArrayList) request.getAttribute("names");
    Collection formatTypes = (Collection) request.getAttribute("formatTypes");
    Collection encodings = (Collection) request.getAttribute("encodings");
    Collection xmlRules = (Collection) request.getAttribute("xmlRules");
    Collection extensions = (Collection) request.getAttribute("extensions");
    List<XmlDtdImpl> xmlDtds = (List<XmlDtdImpl>)request.getAttribute("xmlDtds");
    Map<String,ArrayList<Filter>> mapOfFormatTypeFilter =
    	(Map<String,ArrayList<Filter>>)request.getAttribute("filters");
    ArrayList<Filter> jpFilters = mapOfFormatTypeFilter.get("javaprop");
    ArrayList<Filter> jsFilters = mapOfFormatTypeFilter.get("javascript");
    ArrayList<Filter> docFilters = mapOfFormatTypeFilter.get("word-html");
    ArrayList<Filter> xrFilters = mapOfFormatTypeFilter.get("xml");
    ArrayList<Filter> htmlFilters = mapOfFormatTypeFilter.get("html");
    ArrayList<Filter> jspFilters = mapOfFormatTypeFilter.get("jsp");
    ArrayList<Filter> excelFilters = mapOfFormatTypeFilter.get("excel-html");
    ArrayList<Filter> inddFilters = mapOfFormatTypeFilter.get("indd_cs4");
    ArrayList<Filter> ooFilters = mapOfFormatTypeFilter.get("openoffice-xml");
    ArrayList<Filter> pptFilters = mapOfFormatTypeFilter.get("powerpoint-html");
    ArrayList<Filter> o2010Filters = mapOfFormatTypeFilter.get("office-xml");
    ArrayList<Filter> poFilters = mapOfFormatTypeFilter.get("po");
    ArrayList<Filter> fmFilters = mapOfFormatTypeFilter.get("mif");
    ArrayList<Filter> intxtFilters = mapOfFormatTypeFilter.get("plaintext");
    List<Filter> qaFilters = (List<Filter>) request.getAttribute("qaFilters");
    
	FileProfile fp = (FileProfile) sessionMgr.getAttribute("fileprofile");
    String formatType = (String)sessionMgr.getAttribute("formatType");
    String fpName = "";
    String desc = "";
    Long lpId = new Long(-1);
    long formatId = -1;
    String encoding = "";
    long xmlRule = -1;
    long xmlDtdId = -1;
    Hashtable extensionHash = new Hashtable();
    boolean export = false;
    String scriptOnExport = "";
    String scriptOnImport = "";
    String isSupportSid = "";
    String isUnicodeEscape = "";
    String isHeaderTranslate = "";
    String jsFilter = "";
	long filterId = -1L;
	long qaFilterId = -1L;
	ArrayList<Filter> specialFilters = new ArrayList<Filter>();
	String xslFile = "";
	String xslRelativePath = "";
	int terminologyApproval=0;
	int populateSourceToTarget = 0;
	int BOMType = 0;

    if (fp != null)
    {
        fpName = fp.getName();
        desc = fp.getDescription();
        if (desc == null) desc = "";
        lpId = new Long(fp.getL10nProfileId());
        //check if the localization profile's project use termbase
        L10nProfile lp = LocProfileHandlerHelper.getL10nProfile(lpId);
        
        formatId = fp.getKnownFormatTypeId();
        if(formatType != null){
        	specialFilters = mapOfFormatTypeFilter.get(formatType);
        }
        filterId = fp.getFilterId();
        qaFilterId = fp.getQaFilterId();
        encoding = fp.getCodeSet();
        xmlRule = fp.getXmlRuleId();
        xmlDtdId = fp.getXmlDtdId();
        Vector extensionList = fp.getFileExtensionIds();
        for (int i = 0; i < extensionList.size(); i++)
        {
            Long e = (Long)extensionList.get(i);
            extensionHash.put(e, e);
        }
        export = fp.byDefaultExportStf();
        scriptOnImport = fp.getScriptOnImport() == null ? "" : fp.getScriptOnImport();
        scriptOnExport = fp.getScriptOnExport() == null ? "" : fp.getScriptOnExport();
        isSupportSid = fp.supportsSid() ? "checked" : "";
        isUnicodeEscape = fp.supportsUnicodeEscape() ? "checked" : "";
        isHeaderTranslate = fp.translateHeader() ? "checked" : "";
        jsFilter = fp.getJavascriptFilterRegex() == null ? "" : fp.getJavascriptFilterRegex();
        
        //Check whether XSL exists    	
       String docRoot = AmbFileStoragePathUtils.getXslDir().getPath();
        
       String xslRelativeParent = new StringBuffer("/")
                                     .append(fp.getId())
		                             .append("/")
		                             .toString();
        
       StringBuffer xslPath = new StringBuffer(docRoot).append(xslRelativeParent);
        
       File xslParent = new File(xslPath.toString());

       if (xslParent.exists())
       {
	   		File[] files = xslParent.listFiles();
			if (files.length > 0)
			{
				String fileName = files[0].getName();
				if (fileName.toLowerCase().endsWith("xsl")
					    || fileName.toLowerCase().endsWith("xml")
						|| fileName.toLowerCase().endsWith("xslt"))	
				{
					xslFile = fileName;
					xslRelativePath = xslRelativeParent + fileName;
				}
			}	
       }
       
       terminologyApproval = fp.getTerminologyApproval();

       populateSourceToTarget = fp.getXlfSourceAsUnTranslatedTarget();

       BOMType = fp.getBOMType();
    }
    

%>
<HTML>
<!-- This is /envoy/administration/fileprofile/fileprofileBasic.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.9.1.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/StringBuffer.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/xmlextras.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "<%= bundle.getString("lb_file_profile") %>";
var guideNode = "fileProfiles";
var helpFile = "<%=bundle.getString("help_file_profiles_basic_info")%>";
var xmlHttp = XmlHttp.create();
var toUploadXsl = false;
var companyId = "<%=CompanyThreadLocal.getInstance().getValue()%>";

function confirmFile(fieldName)
{
   var fieldObj = document.getElementsByName(fieldName)[0];
   var textFieldValue = fieldObj.value;
   if(isEmptyString(textFieldValue) || textFieldValue == undefined)
   {
       alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_script_empty"))%>");
       fieldObj.focus();
       return;
   }
   var url = "<%=selfURL%>" + "&path=" + textFieldValue;
   var flagI = false;
   xmlHttp.open("POST", url, true);
   var handleFunction = updatePage(fieldObj);
   xmlHttp.onreadystatechange = handleFunction;
   xmlHttp.send(null);
}

function checkRemve()
{
	var fileBox = document.getElementById('xslFile');
	var isDisabled = fileBox.disabled;
	if (isDisabled)
		fileBox.disabled=false;
	else
		fileBox.disabled=true;
}

function updatePage(obj)
{
    return function ()
    {
       if(xmlHttp.readyState == 4)
       {
           if(xmlHttp.status == 200)
           {
               var responseValue = xmlHttp.responseText;
               if(responseValue == "<%=FileProfileConstants.IS_DIRECTORY%>")
               {
                  alert("<%=EditUtil.toJavascript(bundle.getString("msg_verfy_fileprofile_script_dir"))%>");
                  obj.focus();
               }
               else if(responseValue == "<%=FileProfileConstants.FILE_NOT_EXIST%>")
               {
                  alert("<%=EditUtil.toJavascript(bundle.getString("msg_verfy_fileprofile_script_not_exist"))%>");
                  obj.focus();
               }
               else if(responseValue == "<%=FileProfileConstants.FILE_OK%>")
               {
                  alert("<%=EditUtil.toJavascript(bundle.getString("msg_verfy_fileprofile_script_ok"))%>");
               }
           }
       }
    }
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
     
        fpForm.reset();
        fpForm.action = "<%=cancelURL%>";
        fpForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm())
        {  
           if (toUploadXsl)
           {      
           	   fpForm.encoding = "multipart/form-data";
           	   uploadFile("XSL", "idFpForm", "uploadFile", "uploadFileCallback");
           }
           else
           {
	           fpForm.action = "<%=saveURL%>";
	           enableFieldsBeforeSubmit();
	           fpForm.submit();
           }
       
        }
    }
}

function uploadFileCallback(data)
{
    if (data == "error")
    {
      if(!confirm("Failed to upload XSL file. Do you want to continue saving the file profile? \n\n You can choose to upload XSL file later."))
      {
      	return;
      } 
    }

    fpForm.encoding = "application/x-www-form-urlencoded";
    fpForm.target = "";
    
    fpForm.tmpXslPath.value = data;

        
    fpForm.action = "<%=saveURL%>";
    enableFieldsBeforeSubmit();
    fpForm.submit();
}

function sendAjax(obj, method, callback)
{
	$.ajax(
	{
		url:"AjaxService?action=" + method,
		data:obj,
		dataType: "text", 
		success:function(data){
			eval(callback+'(data)');
		},
		error:function(error)
		{
			alert(error.message);
		}
	});
}

function uploadFile(fileType,formId, method, callback) {	
    var upload_url = "AjaxService?action=" + method + "&fileType=" + fileType;	     
    $("#"+formId+"").submit(function () {   
        $("#"+formId+"").ajaxSubmit({
            type: "post",
            url:   upload_url,
            success: function (data) 
            {
            	var str = data.replace(/<[^>]+>/g,"");
            	eval(callback+'(str)');
            },
            error: function(error)
            {
            	alert("Failed to upload file, please try later.");      	
            }
        });
           return false;
});
        $("#"+formId+"").submit();  
}


function confirmForm()
{
    fpForm.fpName.value = ATrim(fpForm.fpName.value);

    if (isEmptyString(fpForm.fpName.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_name"))%>");
        fpForm.fpName.value = "";
        fpForm.fpName.focus();
        return false;
    }
    if (!isEmptyString(fpForm.scriptOnImport.value) && isEmptyString(fpForm.scriptOnExport.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_script"))%>");
        fpForm.scriptOnExport.value = "";
        fpForm.scriptOnExport.focus();
        return false;
    }
    if (!isEmptyString(fpForm.scriptOnExport.value) && isEmptyString(fpForm.scriptOnImport.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_script"))%>");
        fpForm.scriptOnImport.value = "";
        fpForm.scriptOnImport.focus();
        return false;
    }
    if (hasSpecialChars(fpForm.fpName.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
              "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    <% 
      
        if (names != null) 
        {
            for (int i = 0; i < names.size(); i++)
            {
                FileProfile fpi = (FileProfile)names.get(i);
                String name = fpi.getName();
    %>
                if (fpForm.fpName.value.toLowerCase() == "<%=name%>".toLowerCase() &&
                    fpForm.fpName.value != "<%=fpName%>")
                {
                    alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_file_profile"))%>");
                    fpForm.fpName.focus();
                    return false;
                }
    <%
            }
        }
    %>
    if (!isNotLongerThan(fpForm.desc.value, 256))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_description"))%>");
        fpForm.desc.focus();
        return false;
    }
    if (fpForm.locProfileId.selectedIndex == 0) 
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_lp"))%>");
        fpForm.locProfileId.focus();
        return false;
    }
    if (fpForm.formatInfo.selectedIndex == 0) 
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_sff"))%>");
        fpForm.formatInfo.focus();
        return false;
    }
    if (fpForm.codeSet.selectedIndex == 0) 
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_sfe"))%>");
        fpForm.codeSet.focus();
        return false;
    }
    if (fpForm.extGroup[0].checked == true) 
    {
        var values = "";
        for (var i = 0; i < fpForm.extension.length; i++)
        {
            if (fpForm.extension.options[i].selected == true)
            {
                values += fpForm.extension.options[i].value + ",";
            }
        }
        fpForm.extensions.value = values;
        if (fpForm.extensions.value == "")
        {
            alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_fe"))%>");
            return false;
        }
    }
    
    if (isSpecialFormat("XML") || isSpecialFormat("RESX"))
    {
        toUploadXsl = false;
        var fileBox = document.getElementById('xslFile');
        var isDisabled = fileBox.disabled;
        if (!isDisabled)
        {
            var path = fpForm.xslFile.value;
            if(path != null && path != "")
            {
                if(!isXslFile(path))
                {
                    alert("XSL file extension should be xsl, xslt or xml.");
                    return false;
                }
                else
                {
                    toUploadXsl = true;
                }
            }
        }
    }
    else
    {
    	toUploadXsl = false;
    }
    
    return true;
}

function enableFieldsBeforeSubmit()
{
    fpForm.codeSet.disabled = false;

    fpForm.exportFiles[0].disabled = false;
    fpForm.exportFiles[1].disabled = false;
}

function getFormat()
{
    var value = fpForm.formatInfo.options[fpForm.formatInfo.selectedIndex].value;
    var idx = value.indexOf(",");
    return value.substring(idx + 1);
}

function enforceEncodingAndTargetFileExportIfNeeded()
{
  	var format = getFormat();
	var jpFilters = new Array();
	var jsFilters = new Array();
	var docFilters = new Array();
	var xrFilters = new Array();
	var htmlFilters = new Array();
	var jspFilters = new Array();
	var excelFilters = new Array();
	var inddFilters = new Array();
	var ooFilters = new Array();
	var pptFilters = new Array();
	var o2010Filters = new Array();
	var poFilters = new Array();
	var fmFilters = new Array();
	var intxtFilters = new Array();
  
	<%
		for(int i = 0; i < jpFilters.size(); i++)
		{
			Filter filter = jpFilters.get(i);
			%>
			var jpFilter = new Object();
			jpFilter.id = "<%=filter.getId()%>";
			jpFilter.filterName = "<%=filter.getFilterName()%>";
			jpFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			jpFilters.push(jpFilter);
			<%
		}
	%>
	<%
		for(int i = 0; i < excelFilters.size(); i++)
		{
			Filter filter = excelFilters.get(i);
			%>
			var excelFilter = new Object();
			excelFilter.id = "<%=filter.getId()%>";
			excelFilter.filterName = "<%=filter.getFilterName()%>";
			excelFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			excelFilters.push(excelFilter);
			<%
		}
	%>
	<%
		for(int i = 0; i < jsFilters.size(); i++)
		{
			Filter filter = jsFilters.get(i);
			%>
			var jsFilter = new Object();
			jsFilter.id = "<%=filter.getId()%>";
			jsFilter.filterName = "<%=filter.getFilterName()%>";
			jsFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			jsFilters.push(jsFilter);
			<%
		}
	%>
	<%
		for(int i = 0; i < docFilters.size(); i++)
		{
			Filter filter = docFilters.get(i);
			%>
			var docFilter = new Object();
			docFilter.id = "<%=filter.getId()%>";
			docFilter.filterName = "<%=filter.getFilterName()%>";
			docFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			docFilters.push(docFilter);
			<%
		}
	%>
	
	<%
		for(int i = 0; i < xrFilters.size(); i++)
		{
			Filter filter = xrFilters.get(i);
			%>
			var xrFilter = new Object();
			xrFilter.id = "<%=filter.getId()%>";
			xrFilter.filterName = "<%=filter.getFilterName()%>";
			xrFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			xrFilters.push(xrFilter);
			<%
		}
	%>
	
	<%
		for(int i = 0; i < htmlFilters.size(); i++)
		{
			Filter filter = htmlFilters.get(i);
			%>
			var htmlFilter = new Object();
			htmlFilter.id = "<%=filter.getId()%>";
			htmlFilter.filterName = "<%=filter.getFilterName()%>";
			htmlFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			htmlFilters.push(htmlFilter);
			<%
		}
	%>
	
	<%
		for(int i = 0; i < jspFilters.size(); i++)
		{
			Filter filter = jspFilters.get(i);
			%>
			var jspFilter = new Object();
			jspFilter.id = "<%=filter.getId()%>";
			jspFilter.filterName = "<%=filter.getFilterName()%>";
			jspFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			jspFilters.push(jspFilter);
			<%
		}      
	%>
	
	<%
		for(int i = 0; i < fmFilters.size(); i++)
		{
			Filter filter = fmFilters.get(i);
			%>
			var fmFilter = new Object();
			fmFilter.id = "<%=filter.getId()%>";
			fmFilter.filterName = "<%=filter.getFilterName()%>";
			fmFilter.filterTableName = "<%=filter.getFilterTableName()%>";
			fmFilters.push(fmFilter);
			<%
		}      
	%>

	<%
	for(int i = 0; i < inddFilters.size(); i++)
	{
		Filter filter = inddFilters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		inddFilters.push(filter);
		<%
	}      
	%>

	<%
	for(int i = 0; i < ooFilters.size(); i++)
	{
		Filter filter = ooFilters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		ooFilters.push(filter);
		<%
	}      
	%>

	<%
	for(int i = 0; i < o2010Filters.size(); i++)
	{
		Filter filter = o2010Filters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		o2010Filters.push(filter);
		<%
	}      
	%>
	
	<%
	for(int i = 0; i < pptFilters.size(); i++)
	{
		Filter filter = pptFilters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		pptFilters.push(filter);
		<%
	} 
	%>
	
	<%	
	for(int i = 0; i < poFilters.size(); i++)
	{
		Filter filter = poFilters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		poFilters.push(filter);
		<%
	}
	
	%>
	
	<%	
	for(int i = 0; i < intxtFilters.size(); i++)
	{
		Filter filter = intxtFilters.get(i);
		%>
		var filter = new Object();
		filter.id = "<%=filter.getId()%>";
		filter.filterName = "<%=filter.getFilterName()%>";
		filter.filterTableName = "<%=filter.getFilterTableName()%>";
		intxtFilters.push(filter);
		<%
	}
	
	%>

    if (isXml(format))
    {
        fpForm.rule.disabled = false;
        generateFilters(xrFilters);
    }
    else
    {
        fpForm.rule.disabled = true;
    }

    hideBOMType();
    
    if (thisFormatMustUseUTF8(format))
    {
        //re-set the encoding to UTF-8 and make it non-changeable
        fpForm.codeSet.selectedIndex = 1;
        fpForm.codeSet.disabled = true;
        
        // enable the checkbox to let the user choose which one they want
        fpForm.exportFiles[0].disabled = false;
        fpForm.exportFiles[1].disabled = false;
        if(isWordDoc())
        {
            generateFilters(docFilters);
        }
        else if(isExcels())
        {
            generateFilters(excelFilters);
        }
        else if(isPPT())
        {
            generateFilters(pptFilters);
        }
        else if (isOpenOffice(format))
        {
        	generateFilters(ooFilters);
        }
        else if (isOffice2010(format))
        {
        	generateFilters(o2010Filters);
        }
        else if (isFM9(format))
        {
        	generateFilters(fmFilters);
        }
        else
        {
            generateEmptyFilters();
        }
    }
    else if (noEncodingNeededForFormat(format))
    {
        //re-set the encoding to UTF-8 and make it non-changeable
        fpForm.codeSet.selectedIndex = 1;
        fpForm.codeSet.disabled = true;

        // select the Primary Target File for export and
        // disable the radios so the user can't change the choice
        fpForm.exportFiles[0].checked = true;
        fpForm.exportFiles[0].disabled = true;
        fpForm.exportFiles[1].disabled = true;
        generateEmptyFilters();
    }
    else if(isJavaProperties())
    {
    	generateFilters(jpFilters);
    }
    else if(isJavaScript())
    {
    	generateFilters(jsFilters);
    }
    else if(isWordDoc())
    {
    	generateFilters(docFilters);
    }
    else if(isHtml())
    {
    	generateFilters(htmlFilters);
    }
    else if (isJSP())
    {
    	generateFilters(jspFilters);
    }
    else if (isIndesign(format))
    {
    	generateFilters(inddFilters);
    }
    else if (isPO(format))
    {
    	generateFilters(poFilters);
    }
    else
    {
        // make sure that the encoding and target file export is changeable
        fpForm.codeSet.disabled = false;

        fpForm.exportFiles[0].disabled = false;
        fpForm.exportFiles[1].disabled = false;
        generateEmptyFilters();
		if(isXml(format))
		{
			generateFilters(xrFilters);
		}
    }
    
    if(isPlainText(format))
	{
		generateFilters(intxtFilters);
	}
    
    if(isHtml() || format == "XML" || format == "RESX" || format == "AuthorIT XML")
    {
    	showBOMType();
    }
    
    isSupportSid();
    isSupportXsl();
    isHeaderTranslate();
    isShowLocalizeFunction();
    isShowXmlDtd();
    hideXmlRule();
    showOrHideXlfSourceAsTarget();
}

function setForXLZ() {
    var text = getFormat();
    if (text.indexOf("XLZ") != -1) {
    	enforceEncodingAndTargetFileExportIfNeeded();
    }
}

function generateEmptyFilters()
{
	var str = new StringBuffer("<select id='filterContent' name='filterInfo' class='standardText'>");
	str.append("<option value='-1,'><%=bundle.getString("lb_choose")%></option>");
	str.append("</select>");
	insertFilterHtml(str.toString());
}

function insertFilterHtml(str)
{
	document.getElementById("filterSelectBox").innerHTML = str;
}
function generateFilters(filters)
{
	var str = new StringBuffer("<select id='filterContent' name='filterInfo' class='standardText'>");
	str.append("<option value='-1,'><%=bundle.getString("lb_choose")%></option>");
	for(var i = 0; i < filters.length; i++)
	{
		var filter = filters[i];
		str.append("<option value='"+filter.id+","+filter.filterTableName+"'>");
		str.append(filter.filterName);
		str.append("</option>");
	}
	str.append("</select>");
	insertFilterHtml(str.toString());
}

function isJSP()
{
	return isSpecialFormat("JSP");
}

function isHtml()
{
	return isSpecialFormat("HTML");
}

function thisFormatMustUseUTF8(format)
{
    if (format == "Word2003" ||
        format == "PowerPoint2003" ||
        format == "Excel2003" ||
        format == "Word2007" ||
        format == "PowerPoint2007" ||
        format == "Excel2007" ||
        format == "PDF" || 
        format == "OpenOffice document" || 
        format == "Office2010 document" ||
        format == "(Beta) New Office 2010 Filter (DOCX only)" ||
        format == "MIF 9" ||
        format == "FrameMaker9" ||
        format == "Passolo 2011" ||
        format == "Windows Portable Executable")
    {
        return true;
    }

    if (format.indexOf("Office 2010") >= 0)
    {
        return true;
    }

    return false;
}

function noEncodingNeededForFormat(format)
{
    if (format == "Un-extracted")
    {
        return true;
    }

    return false;
}

function isXml(format)
{
    if (format == "XML" || 
    	format == "RESX" ||
        format == "Quark (WIN)" ||
        format == "Frame5" ||
        format == "Frame6" ||
        format == "Frame7" ||
        format == "AuthorIT XML"
        )
    {
        return true;
    }

    return false;
}

function isIndesign(format)
{
    if (format == "INDD (CS2)" ||
        format == "INDD (CS3)" ||
        format == "INDD (CS4)" ||
        format == "INDD (CS5)" ||
        format == "INDD (CS5.5)" ||
        format == "INX (CS2)" ||
        format == "INX (CS3)"  ||
        format == "InDesign Markup (IDML)" )
    {
        return true;
    }

    return false;
}

function isOpenOffice(format)
{
    if (format == "OpenOffice document")
    {
        return true;
    }

    return false;
}

function isOffice2010(format)
{
    
    if (format == "Office2010 document" || format.indexOf("Office 2010") >= 0)
    {
        return true;
    }

    return false;
}

function isPlainText(format)
{
    if (format == "PlainText")
    {
        return true;
    }

    return false;
}

function isFM9(format)
{
	if (format == "MIF 9" || format == "FrameMaker9")
    {
        return true;
    }

    return false;
}

function doOnload()
{
    loadGuides();

    // enable/disable fields
    enforceEncodingAndTargetFileExportIfNeeded();
    selectFilter();
    // <% if (extensionHash.size() == 0 && fp != null) { %>
    fpForm.extGroup[1].checked = true;
    // <% } else { %>
    fpForm.extGroup[0].checked = true;
    // <% } %>

    // <% if (export) { %>
    fpForm.exportFiles[1].checked = true;
    // <% } else { %>
    fpForm.exportFiles[0].checked = true;
    // <% } %>
    isSupportSid();
    isSupportXsl();
    isHeaderTranslate();
    isShowLocalizeFunction();
    isShowXmlDtd();
    hideXmlRule();
}

function selectFilter()
{
	var filterId = "<%=filterId%>";
	var filterSelectBox = document.getElementById("filterContent");
	for(var i = 0; i < filterSelectBox.options.length; i++)
	{
		var opt = filterSelectBox.options[i];
		if(opt.value.split(",") && opt.value.split(",")[0] == filterId)
		{
			opt.selected = true;
		}
	}
}

function showHeaderTranslate()
{
    document.getElementById("tr_isHeaderTranslate").style.display = "block";
}

function hideHeaderTranslate()
{
    document.getElementById("tr_isHeaderTranslate").style.display = "none";
}

function isHeaderTranslate()
{
    var selector = document.getElementById("formatSelector");
    var text = getFormat();
    if (text.search("Word") != -1)
    {
       //showHeaderTranslate();
       hideHeaderTranslate();
    }
    else
    {
       hideHeaderTranslate();
    }
    
}

function showSupportSID()
{
    document.getElementById("tr_isSupportedSID").style.display = "block";
    document.getElementById("tr_isUnicodeEscape").style.display = "block";
}

function hideSupportSID()
{
    document.getElementById("tr_isSupportedSID").style.display = "none";
    document.getElementById("tr_isUnicodeEscape").style.display = "none";
}

function showBOMType() {
	document.getElementById("lb_bomType").style.display = "block";
	document.getElementById("td_bomType").style.display = "block";
}

function hideBOMType() {
    document.getElementById("lb_bomType").style.display = "none";
	document.getElementById("td_bomType").style.display = "none";
}

function isSpecialFormat(specialFormatType)
{
	var selector = document.getElementById("formatSelector");
    var text = getFormat();
    
    if(text.search(specialFormatType) != -1)
    {
    	return true;
    }
    else
    {
		return false;    
    }
}
function isJavaProperties()
{
	return isSpecialFormat("JavaProperties");
}

function isExcels()
{
	return isSpecialFormat("Excel");
}

function isPPT()
{
	return isSpecialFormat("PowerPoint");
}

function isPO()
{
	return isSpecialFormat("Portable Object");
}

function isJavaScript()
{
	return isSpecialFormat("Javascript");
}

function isWordDoc()
{
	return isSpecialFormat("Word");
}

function isXlfOrXlz()
{
    return isSpecialFormat("Xliff") || isSpecialFormat("XLZ");
}

function isSupportSid()
{
    var selector = document.getElementById("formatSelector");
    var text = getFormat();

    if (text.search("JavaProperties") != -1)
    {
       //document.getElementById("isSopportSid").disabled=false;
       //showSupportSID();
       hideSupportSID();
    }
    else
    {
       //document.getElementById("isSopportSid").disabled=true;
       hideSupportSID();
    }
}

function isSupportXsl()
{
	if (isSpecialFormat("XML") || isSpecialFormat("RESX"))
	{
	  document.getElementById("lb_isSupportXsl").style.display = "block";
	  document.getElementById("td_isSupportXsl").style.display = "block";
	}
	else
	{
	  document.getElementById("lb_isSupportXsl").style.display = "none";
	  document.getElementById("td_isSupportXsl").style.display = "none";
	}
}

function removeXsl(filePath)
{
   if (filePath != null && filePath != "")
   {
     if (confirm("You're trying to delete the XSL file from server, \n\nare you sure to continue?")) 
     {
     	sendAjax({filePath:filePath}, "removeFile", "removeFileCallback");
     }
   }
}

function removeFileCallback(data)
{
	if (data != null && data == "true")
	{
       alert("Succeeded in deleting the file on server.");
       document.getElementById("idXslLink").style.display = "none";
       document.getElementById("idXslRemoveButton").style.display = "none";
    }
    else
    {
       alert("Failed to delete the file on server.");
    }
    
}

function hideXmlRule()
{
	document.getElementById("xmlrule").style.display = "none";
}

function showOrHideXlfSourceAsTarget()
{
    if (isXlfOrXlz())
    {
        document.getElementById("lb_xlfSrcAsTarget").style.display = "block";
        document.getElementById("td_xlfSrcAsTarget").style.display = "block";
    }
    else
    {
        document.getElementById("lb_xlfSrcAsTarget").style.display = "none";
        document.getElementById("td_xlfSrcAsTarget").style.display = "none";
    }
}

function isShowLocalizeFunction()
{
    var text = getFormat();
    if (text.search("Javascript") != -1)
    {
        document.getElementById("tr_jsFilter").style.display = "none";
    }
    else
    {
        document.getElementById("tr_jsFilter").style.display = "none";
    }
}


function isXslFile(path)
{
  var isXsl = false;
  
  if (path != null && path != "")
  {
    var index = path.lastIndexOf(".");
    if (index < 0)
    {
      return;
    }

    var ext = path.substring(index + 1).toLowerCase();

    if (ext == "xsl" 
        || ext == "xslt"
        || ext == "xml")
    {
        isXsl = true;
    } 
    
  }
  
  return isXsl;
}

function viewXsl(filePath)
{
  document.getElementById('idXslLink').href="/globalsight/envoy/administration/fileprofile/viewXslFile.jsp?companyId="+ companyId + "&filePath=" + filePath;
  document.getElementById('idXslLink').target="_blank";
}

function isShowXmlDtd()
{
    var text = getFormat();
    if (text.search("XML") != -1)
    {
        document.getElementById("tr_xmlDtd").style.display = "";
    }
    else
    {
        document.getElementById("tr_xmlDtd").style.display = "none";
    }
}

function isProjectUseTermbaseCheck() {
    if(document.getElementById("terminologyRadio2").checked == true) {
        var locProfile = fpForm.locProfileId;

        if(locProfile.value == "-1") {
            alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_file_profile_lp"))%>");
        }
        else {
            var obj = {locProfileId:locProfile.value};
            sendAjax(obj, "isProjectUseTerbase", "isProjectUseTermbase");
        }
    }
    else {
        submitForm('save');
    }
}

function isProjectUseTermbase(data) {
    var returnObj =  eval(data);

    if(returnObj[0].isProjectUseTerbase == "false") {
        alert("<%=bundle.getString("lb_no_use_termbase")%>");
    }
    else {
        submitForm('save');
    }
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
  <TR>
    <TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=title%></TD>
  </TR>
</TABLE>
<P>

<form name="fpForm" id="idFpForm" method="post" action="">
<input type="hidden" name="extensions" value="">
      
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="textfield" name="fpName" maxlength="60" size="30"
            value="<%=fpName%>" class="standardText">
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea rows="4" cols="40" name="desc" class="standardText"><%=desc%></textarea>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_loc_profile")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select id="locProfileId" name="locProfileId" onchange="setForXLZ();" class="standardText">
              <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            //fix for GBS-1693
            HashMap<String, Long> locProfilesMap = new HashMap<String, Long>();
            ArrayList<String> locProfilesValueList = new ArrayList<String>();
            for (int i = 0; i < locProfiles.size(); i++)
            {
                Long num = (Long)locProfiles.getKey(i);
                String value = (String)locProfiles.getValue(i);
                locProfilesMap.put(value, num);
                locProfilesValueList.add(value);
            }
            SortUtil.sort(locProfilesValueList, new StringComparator(Locale.getDefault()));
            for (String locProfileValue:locProfilesValueList)
            {
                long num = locProfilesMap.get(locProfileValue);
                if (lpId.equals(num))
                    out.println("<option value=" + num + " selected>" + locProfileValue + "</option>");
                else
                    out.println("<option value=" + num + ">" + locProfileValue + "</option>");
            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_source_file_format")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select id="formatSelector" name="formatInfo"
              onchange="enforceEncodingAndTargetFileExportIfNeeded()" class="standardText">
              <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (Iterator it = formatTypes.iterator(); it.hasNext();)
            {
                KnownFormatType type = (KnownFormatType)it.next();
                if (type.getId() == formatId)
        		{
                    out.println("<option value='" + type.getId() + "," + type.getName() + "' selected>" + type.getName() + "</option>");
        		}
                else
        		{
                    out.println("<option value='" + type.getId() + "," + type.getName() + "'>"          + type.getName() + "</option>");
        		}
            }
%>
            </select>
            
          </td>
        </tr>
        <tr>
          <td valign="top">
            <span id="lb_isSupportXsl" class="standardText"><%=bundle.getString("lb_xsl_file")%>:</span>
          </td>
          <td id="td_isSupportXsl">
            <a id="idXslLink" class="standardHREF" href="" onclick="javascript: viewXsl('<%=xslRelativePath%>')"><%=xslFile%></a>
            <%if(xslFile != null && !xslFile.equals("")) {%>
            <label><%=bundle.getString("lb_remove")%></label>
            <input type="checkbox" id="idXslRemove" name="removeXslFile" value="true" onclick="checkRemve()"></input>
            <input id="xslFile" type="file" name="xslFile" size=40 disabled="disabled"></input>
            <% } else{ %>
		    <input id="xslFile" type="file" name="xslFile" size=40></input>
		    <%} %>
          </td>
        </tr>
        
        <tr id="tr_filter">
        	<td><span class="standardText"><%=bundle.getString("lb_filter")%>:</span></td>
        	<td>
	        	<span id="filterSelectBox">
	        	      <select id="filterContent" name="filterInfo" class="standardText">
	        	      	<option value="-1,"><%=bundle.getString("lb_choose")%></option>
	        	      	<%
	        	      	for(int index = 0; index < specialFilters.size(); index++)
	        	      	{
	        	      		Filter specialFilter = specialFilters.get(index);
	        	      		if(specialFilter.getId() == filterId)
	        	      		{
	        	      			out.println("<option value='" + specialFilter.getId() +","+specialFilter.getFilterTableName()+"' selected>");
	        	      			out.println(specialFilter.getFilterName());
	        	      			out.println("</option>");
	        	      		}
	        	      		else
	        	      		{
	        	      			out.println("<option value='" + specialFilter.getId() +","+specialFilter.getFilterTableName()+"'>");
	        	      			out.println(specialFilter.getFilterName());
	        	      			out.println("</option>");        	      			
	        	      		} 
	        	      	}
	        	      	%>
	        	      </select>
	        	</span>
        	</td>
        </tr>
        
        <tr id="tr_isHeaderTranslate">
          <td></td>
          <td valign="top">
            <input id="isHeaderTranslate" type="checkbox" name="headerTranslate" value="true" <%=isHeaderTranslate %>>Translate Header Information
          </td>
        </tr>
        
        <tr id="tr_isSupportedSID">
          <td></td>
          <td valign="top">
            <input id="isSopportSid" type="checkbox" name="supportSid" value="true" <%=isSupportSid %>>Enable SID Support
          </td>         
        </tr>
        
        <tr id="tr_isUnicodeEscape">
          <td></td>
          <td valign="top">
            <input id="isUnicodeEscape" type="checkbox" name="unicodeEscape" value="true" <%=isUnicodeEscape %>>Enable Unicode Escape
          </td>         
        </tr>
        
        <tr id="tr_jsFilter" class="standardText">
          <td></td>
          <td valign="top">
            <span style="padding-right:10px;"><%=bundle.getString("lb_js_function_filter")%></span>
            <input id="jsFilterText" type="text" name="jsFilter" maxlength="100" value="<%=jsFilter%>" style="width:222px; padding-left:5px;">
          </td>
        </tr>
        
        <tr id="tr_xmlDtd" class="standardText">
          <td>&nbsp;</td>
          <td valign="top">
            <span style="padding-right:10px;"><%=bundle.getString("lb_xml_dtd_config")%></span>
            <select name="dtdIds">
              <option value="-1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
              <%for (XmlDtdImpl xmlDtd : xmlDtds){
                 String selected = xmlDtd.getId() == xmlDtdId? "selected" : "";
              %>
              <option value='<%=xmlDtd.getId()%>' <%=selected %>><%=xmlDtd.getName()%></option>
              <%}%>               
            </select>
          </td>
        </tr>
        
        <tr>
        	<td><span class="standardText"><%=bundle.getString("lb_filter_qafilter")%>:</span></td>
        	<td>
	        	<select id="qaFilterContent" name="qaFilterInfo" class="standardText">
	        	    <option value="-1"><%=bundle.getString("lb_choose")%></option>
	        	    <%
	        	    for(int i = 0; i < qaFilters.size(); i++)
	        	    {
	        	    	String selected = "";
        	      		Filter qaFilter = qaFilters.get(i);
        	      		if(qaFilter.getId() == qaFilterId)
        	      		{
        	      			selected = "selected";
        	      		}
    	      			%>
    	      			<option value="<%=qaFilter.getId()%>" <%=selected%>><%=qaFilter.getFilterName()%></option>
	        	     <%
	        	     }
	        	     %>
	        	 </select>
        	</td>
        </tr>
        
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_source_file_encoding")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="codeSet" class="standardText">
              <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (Iterator it = encodings.iterator(); it.hasNext();)
            {
                CodeSet code = (CodeSet)it.next();
                if (code.getCodeSet().equals(encoding))
        {
                    out.println("<option value='" + code.getCodeSet() + "' selected>" +
                         code.getCodeSet() + "</option>");
        }
                else
        {
                    out.println("<option value='" + code.getCodeSet() + "'>" +
                         code.getCodeSet() + "</option>");
            }
            }
%>
            </select>
          </td>
        </tr>
        
        <!-- BOM processing -->
        <tr>
          <td valign="top">
            <span id="lb_bomType" class="standardText"><%=bundle.getString("lb_utf_bom") %>:<br><%=bundle.getString("lb_utf_bom_usage") %></span>
          </td>
          <td id="td_bomType">
            <select name="bomType" id="bomType" class="standardText">
              <option value="0"><%=bundle.getString("lb_choose") %></option>
              <option value="1" <%=BOMType == FileProfileImpl.UTF_BOM_PRESERVE ? "selected" : "" %>><%=bundle.getString("lb_utf_bom_preserve") %></option>
              <option value="2" <%=BOMType == FileProfileImpl.UTF_BOM_ADD ? "selected" : "" %>><%=bundle.getString("lb_utf_bom_add") %></option>
              <option value="3" <%=BOMType == FileProfileImpl.UTF_BOM_REMOVE ? "selected" : "" %>><%=bundle.getString("lb_utf_bom_remove") %></option>
            </select>
          </td>
        </tr>
   <!--
     Start of adding script on import and export field
   -->
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_script_on_import")%><span class="asterisk"></span>:
          </td>
          <td>
            <input type="textfield" name="scriptOnImport" size="50" value="<%=scriptOnImport%>" class="standardText">
            <input type="button" name="importBatBtn" value="<%=bundle.getString("lb_verify") %>" onclick="confirmFile('scriptOnImport')" class="standardText">
           </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_script_on_export")%><span class="asterisk"></span>:
          </td>
          <td>
             <input type="textfield" name="scriptOnExport" size="50" value="<%=scriptOnExport%>" class="standardText">
             <input type="button" name="exportBatBtn" value="<%=bundle.getString("lb_verify") %>" onclick="confirmFile('scriptOnExport')" class="standardText">
           </td>
        </tr>
   <!--
    End of adding script on import and export field
   -->

        <tr id='xmlrule'>
          <td valign="top">
            <%=bundle.getString("lb_xml_rules")%>:
          </td>
          <td>
            <select name="rule" class="standardText">
              <option value="-1"><%=bundle.getString("lb_none")%></option>
<%
            for (Iterator it = xmlRules.iterator(); it.hasNext();)
            {
                XmlRuleFile rule = (XmlRuleFile)it.next();
                if (rule.getId() == xmlRule)
        {
                    out.println("<option value='" + rule.getId() + "' selected>" +
                         rule.getName() + "</option>");
        }
                else
        {
                    out.println("<option value='" + rule.getId() + "'>" +
                         rule.getName() + "</option>");
        }
            }
%>
            </select>
      </td>
    </tr>

    <tr>
      <td valign="top">
        <%=bundle.getString("lb_file_extensions")%><span class="asterisk">*</span>:
      </td>
      <td>
        <input type="radio" name="extGroup" value="0" id="idExt1">
        <label for="idExt1">
          <%=bundle.getString("lb_file_extensions_select")%>
        </label>
        <input type="radio" name="extGroup" value="1" id="idExt2">
        <label for="idExt2">
          <%=bundle.getString("lb_file_extensions_all")%>
        </label>
      </td>
    </tr>
    <tr>
      <td></td>
      <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <select name="extension" size="4" multiple="true" class="standardText">
<%
            for (Iterator it = extensions.iterator(); it.hasNext();)
            {
                FileExtension ext = (FileExtension)it.next();
                if (extensionHash.get(new Long(ext.getId())) != null)
            {
                    out.println("<option value='" + ext.getId() + "' selected>" +
                             ext.getName() + "</option>");
        }
                else
        {
                    out.println("<option value='" + ext.getId() + "'>" +
                             ext.getName() + "</option>");
        }
            }
%>
            </select>
      </td>
    </tr>
    <tr>
      <td valign="top">
        <%=bundle.getString("lb_default_export")%>:
      </td>
      <td>
        <input type="radio" name="exportFiles" value="1" id="idExp1">
        <label for="idExp1">
          <%=bundle.getString("lb_primary_target_files")%>
        </label>
        <input type="radio" name="exportFiles" value="2" id="idExp2">
        <label for="idExp2">
          <%=bundle.getString("lb_secondary_target_files")%>
        </label>
      </td>
    </tr>

  <tr>
      <td valign="top">
        <%=bundle.getString("lb_terminology_approval")%>:
      </td>
      <td>
        <input type="radio" name="terminologyRadio" value="0" id="terminologyRadio1" <%if(terminologyApproval==0) out.println("checked");%>>
        <label for="terminologyRadio1">
          <%=bundle.getString("lb_no")%>
        </label>
        <input type="radio" name="terminologyRadio" value="1" id="terminologyRadio2" <%if(terminologyApproval==1) out.println("checked");%>">
        <label for="terminologyRadio2">
          <%=bundle.getString("lb_yes")%>
        </label>
      </td>
    </tr>

    <tr>
      <td valign="top">
        <span id="lb_xlfSrcAsTarget" class="standardText" style="display:none">Populate Source Into<br>Un-Translated Target:</span>
      </td>
      <td id="td_xlfSrcAsTarget" style="display:none">
        <input type="radio" name="xlfSrcAsTargetRadio" value="0" id="xlfSrcAsTargetRadio1" <%if(populateSourceToTarget==0) out.println("checked");%>>
            <label for="xlfSrcAsTargetRadio1"><%=bundle.getString("lb_no")%></label>
        <input type="radio" name="xlfSrcAsTargetRadio" value="1" id="xlfSrcAsTargetRadio2" <%if(populateSourceToTarget==1) out.println("checked");%>/>
            <label for="xlfSrcAsTargetRadio2"><%=bundle.getString("lb_yes")%></label>
      </td>
    </tr>

    </table>
    </td>
  </tr>
  <tr>
    <td>
      <input type="button" class="standardText" value="<%=lbCancel%>" onclick="submitForm('cancel')">
      <input type="button" class="standardText" value="<%=lbSave%>"   onclick="isProjectUseTermbaseCheck();">
    </td>
  </tr>
</table>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_FILE_PROFILE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
<input type="hidden" name="tmpXslPath" value="" />

</form>

</body>
</html>
