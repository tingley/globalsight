<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.tm.exporter.ExportOptions,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.tm.util.Tmx"
    session="true"
%>
<%--
State machinery for export:

export --> exportFileOptions --> exportOutputOptions --> exportProgress

export.jsp sets <selectOptions>
exportFileOptions sets <fileOptions>
exportOutputOptions.jsp sets <outputOptions>
exportProgress.jsp runs the export and allows to download result file

--%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_DEFINITION);
String xmlExportOptions =
  ((String)sessionMgr.getAttribute(WebAppConstants.TM_EXPORT_OPTIONS)).replaceAll("\"","&quot;");
String xmlProject = 
    (String)sessionMgr.getAttribute(WebAppConstants.TM_PROJECT);
String xmlAttribute = 
	(String)sessionMgr.getAttribute(WebAppConstants.TM_ATTRIBUTE);
String termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);

String selectLanguage = bundle.getString("jsmsg_tm_select_language");
String propTypeTitle = bundle.getString("lb_prop_type");
String[] propTypes = new String[]{
        Tmx.VAL_TU_LOCALIZABLE,
        Tmx.VAL_TU_TRANSLATABLE,
        Tmx.PROP_SOURCE_TM_NAME,
        Tmx.PROP_UPDATED_BY_PROJECT,
        Tmx.PROP_CREATION_PROJECT};

String urlNext   = next.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = bundle.getString("lb_export_tm");
String lb_calendar_title = bundle.getString("lb_calendar_title");
%>
<HTML>
<!-- This is envoy\tm\management\export.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_tm_export")%></TITLE>
<STYLE>
{ font: Tahoma Verdana Arial 10pt; }
INPUT, SELECT { font: Tahoma Verdana Arial 10pt; }

LEGEND        { font-size: smaller; font-weight: bold; }
.link         { color: blue; cursor: hand; }
.calendar     { width:16px; height:15px; cursor:pointer; vertical-align:middle; }
.row1 {
  background-color: #DFE3EE;
}

.row2 {
  background-color: #FFFFFF;
}
</STYLE>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/includes/report/calendar.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/jquery/jquery.xmlext.js"></SCRIPT>
<script type="text/javascript" SRC="/globalsight/includes/utilityScripts.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_export")%>";
var xmlProject = "<%=xmlProject.replace("\n","").replace("\r","").trim()%>";
var xmlDefinition = "<%=xmlDefinition.replace("\n","").replace("\r","").trim()%>";
var xmlExportOptions = "<%=xmlExportOptions.replace("\n","").replace("\r","").trim()%>";
var xmlAttribute = "<%=xmlAttribute.replace("\n","").replace("\r","").trim()%>";

function Result(message, description, element, dom)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = dom;
}

function parseExportOptions()
{
  var form = document.oDummyForm;
  var node;
  var createdAfter, createdBefore,modifyAfter,modifyBefore,lastUsageAfter,lastUsageBefore,createUser,modifyUser,tuId,sId,isRegex,jobId;
  var selectMode, selectLanguage, selectProjectName, duplicateHandling, fileType, fileEncoding, selectChangeCreationId;

  var $xml = $( $.parseXML( xmlExportOptions ) );

  node = $xml.find("exportOptions > selectOptions");
  selectMode = node.find("selectMode").text();

  selectFilter = node.find("selectFilter").text();
  selectChangeCreationId = node.find("selectChangeCreationId").text();
  
  node = $xml.find("exportOptions > filterOptions");
  createdAfter = node.find("createdafter").text();
  createdBefore = node.find("createdbefore").text();
  modifyAfter = node.find("modifiedafter").text();
  modifyBefore = node.find("modifiedbefore").text();
  //GBS-3907
  selectLanguage = node.find("language").text();
  selectProjectName = node.find("projectName").text();
  
  lastUsageAfter = node.find("lastusageafter").text();
  lastUsageBefore = node.find("lastusagebefore").text();
  jobId = node.find("jobId").text();
  
  createUser = node.find("createdby").text();
  modifyUser = node.find("modifiedby").text();
  tuId = node.find("tuId").text();
  sId = node.find("stringId").text();
  isRegex = node.find("regex").text();
  
  node = $xml.find("exportOptions > fileOptions");
  fileType = node.find("fileType").text();
  fileEncoding = node.find("fileEncoding").text();

  if (selectMode == "<%=ExportOptions.SELECT_FILTERED%>")
  {
    form.oEntries[1].checked = true;
    form.oEntryLang.disabled = false;
  }

  form.fltCreatedAfter.value = createdAfter;
  form.fltCreatedBefore.value = createdBefore;
  form.fltModifiedAfter.value = modifyAfter;
  form.fltModifiedBefore.value = modifyBefore;
  form.fltCreatedUser.value = createUser;
  form.fltModifiedUser.value = modifyUser;
  form.fltTuId.value = tuId;
  form.fltSID.value = sId;
  
  form.fltLastUsageAfter.value = lastUsageAfter;
  form.fltLastUsageBefore.value = lastUsageBefore;
  form.fltJobId.value = jobId;
  
  if(isRegex == "true"){
	  form.fltIsRegex.checked = true;
  }

  selectValue(form.oEntryLang, selectLanguage);
  selectValue(form.oEntryPropType, selectProjectName);
  selectValue(form.oEncoding, fileEncoding);

  if (fileType == "<%=ExportOptions.TYPE_XML%>")
  {
    form.oType[0].click();
  }
  else if (fileType == "<%=ExportOptions.TYPE_TMX1%>")
  {
    form.oType[1].click();
  }
  else if (fileType == "<%=ExportOptions.TYPE_TMX2%>")
  {
    form.oType[2].click();
  }
  else if (fileType == "<%=ExportOptions.TYPE_TTMX%>")
  {
    form.oType[3].click();
  }

  if (selectChangeCreationId == "true") {
	  form.oChangeCreationId.checked = true;
  }

  $xml.find("exportOptions > attributes").children().each(function(){
	  var key = $(this).find("key").text();
	  var value= $(this).find("value").text();
	  opt = document.createElement("OPTION");
	  opt.text = opt.value = opt.title = key + ":" + value;
	  oDummyForm.allItems.add(opt);	
  });
  setOptionColor();
}

function buildExportOptions()
{
  var result = new Result("", "", null, null);
  var form = document.oDummyForm;
  var node;
  var sel;
  
  var $xml = $( $.parseXML( xmlExportOptions ) );

  //SELECT OPTIONS
  node = $xml.find("exportOptions > selectOptions");
  node.find("selectMode").text("<%=ExportOptions.SELECT_ALL%>");

  

  sel = form.oChangeCreationId;
  if (sel.checked) 
  {
	  node.find("selectChangeCreationId").text("true");
  } 
  else 
  {
	  node.find("selectChangeCreationId").text("false");
  }

  // FILTER OPTIONS
  node = $xml.find("exportOptions > filterOptions");
  //Create Date
  node.find("createdafter").text(form.fltCreatedAfter.value);
  node.find("createdbefore").text(form.fltCreatedBefore.value);
  //Modify Date
  node.find("modifiedafter").text(form.fltModifiedAfter.value);
  node.find("modifiedbefore").text(form.fltModifiedBefore.value);
  //Last Usage Date
  node.find("lastusageafter").text(form.fltLastUsageAfter.value);
  node.find("lastusagebefore").text(form.fltLastUsageBefore.value);
  
 if(checkSomeSpecialChar(form.fltJobId.value))
 {
	 alert("<%=EditUtil.toJavascript(bundle.getString("lb_job_id"))%> " +
              "<%=EditUtil.toJavascript(bundle
				.getString("msg_invalid_entry8"))%>");
	 return false;
 }
 else
 {
	 if(form.fltJobId.value.trim() != "" && form.fltJobId.value.trim() != null)
	 {
		 var temp=/^\d+(\.\d+)?$/;
		 var jobIdArr = form.fltJobId.value.split(",");
		 for(var i =0;i<jobIdArr.length;i++)
		 {
			 if(temp.test(jobIdArr[i])==false)
			 {
				 alert("<%=EditUtil.toJavascript(bundle.getString("lb_job_id"))%> " +
			              "<%=EditUtil.toJavascript(bundle
							.getString("msg_invalid_entry8"))%>");
				 return false;
			 }
		 }
	 }	 
	  //Job id
	  node.find("jobId").text(form.fltJobId.value);
 }
  //Create User
  node.find("createdby").text(form.fltCreatedUser.value);
  //Modify User
  node.find("modifiedby").text(form.fltModifiedUser.value);
  //TU ID
  node.find("tuId").text(form.fltTuId.value);
  //SID
  node.find("stringId").text(form.fltSID.value);
 //regex fltIsRegex
 if(form.fltIsRegex.checked){
	 node.find("regex").text("true");
 }else{
	 node.find("regex").text("false");
 }
 
 sel = form.oEntryLang;
 if (sel.options.length > 0)
 {
	  var language="";
	  var count = 0;
	  var selectOption = false;
	  for(var i = 0;i < sel.options.length;i++ )
	  {
	  	 if(sel.options[i].selected)
	  	 {
			 count++;
	  		language += sel.options[i].value+","
	  	 }  
	  }
	 language = language.substring(0,language.lastIndexOf(","));
     node.find("language").text(language);
 }
 
 sel = form.oEntryPropType;
 if(sel.options.length > 0)
 {
   optionsList = selectMulti(sel);    
   node.find("projectName").text(optionsList.toString());
 }
 
  // FILE OPTIONS
  node = $xml.find("exportOptions > fileOptions");
  if (form.oType[0].checked)
  {
    node.find("fileType").text("<%=ExportOptions.TYPE_XML%>");
  }
  else if (form.oType[1].checked)
  {
    node.find("fileType").text("<%=ExportOptions.TYPE_TMX1%>");
  }
  else if (form.oType[2].checked)
  {
    node.find("fileType").text("<%=ExportOptions.TYPE_TMX2%>");
  }
  else if (form.oType[3].checked)
  {
    node.find("fileType").text("<%=ExportOptions.TYPE_TTMX%>");
  }

  var sel = form.oEncoding;
  node.find("fileEncoding").text(sel.options[sel.selectedIndex].value);

  $xml.find("exportOptions > attributes").remove();
  $xml.find("exportOptions").appendXml("<attributes></attributes>");
  	$("#allItems").children("option").each(function()
	{
		var keyAndValue=$(this).val();
		var key = keyAndValue.substring(0,keyAndValue.indexOf(":"));
		var value = keyAndValue.substring(keyAndValue.indexOf(":") + 1);
		value = value.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("'","&apos;").replaceAll('"',"&quot;");
		$xml.find("exportOptions > attributes").appendXml("<attribute><key>"+key+"</key><value>"+value+"</value></attribute>");
	});
  result.dom = $xml;
  return result;
}

String.prototype.replaceAll = function(s1, s2) {      
    return this.replace(new RegExp(s1, "gm"), s2);     
} 

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function selectMulti(selects){
    var optionsList = new Array();
    for( var loop = 0; loop < selects.options.length; loop ++){
        if (selects.options[loop].selected == true) {
            optionsList[optionsList.length] = selects.options[loop].text;
        }
    }
    return optionsList;
}

function doNext()
{
    var form = document.oDummyForm;
    //Create Date
    if (form.fltCreatedAfter.value != "") {
        if (form.fltCreatedBefore.value == "") {
            alert("Please select the end of creation date");
            return;
        }
    } else {
    	if (form.fltCreatedBefore.value != "") {
            alert("Please select the start of creation date");
            return;
    	}
    }
	
    //Modify Date
    if (form.fltModifiedAfter.value != "") {
        if (form.fltModifiedBefore.value == "") {
            alert("Please select the end of modify date");
            return;
        }
    } else {
    	if (form.fltModifiedBefore.value != "") {
            alert("Please select the start of modify date");
            return;
    	}
    }
    
    //Create User
    if(hasSpecialChars(form.fltCreatedUser.value))
    {
    	alert("<%= bundle.getString("lb_export_create_user") %>" + "<%=bundle.getString("msg_invalid_entry")%>");
    	return false;	
    }
    //Modify User
    if(hasSpecialChars(form.fltModifiedUser.value))
    {
    	alert("<%= bundle.getString("lb_export_modify_user") %>" + "<%=bundle.getString("msg_invalid_entry")%>");
    	return false;	
    }
    
    //tu id
    var tuIds = form.fltTuId.value;
    if(tuIds != "" && tuIds != null)
    {
	    if (checkSpecialChars(tuIds))
	    {
	        alert("TU ID" + "<%= bundle.getString("msg_invalid_entry5") %>");
	        return false;
	    }
	    
	    var tuIdArr = tuIds.split(",");
	    if(tuIdArr.length > 0)
	    {
	    	for(var i = 0;i < tuIdArr.length; i++)
	    	{
	    		var tuId = tuIdArr[i];
	    		if(tuId.indexOf("-") > -1)
	    		{
	    			var startIndex = tuId.indexOf("-");
	    			var lastIndex = tuId.lastIndexOf("-");
	    			if(startIndex != lastIndex)
	    			{
	    				alert("TU ID" + "<%=bundle.getString("msg_invalid_entry6")%>");
	    				return false;
	    			}
	    			else
	    			{
	    				var idArr = tuId.split("-");
	    				for(var j=0;j<idArr.length;j++)
	    				{
	    					if(!isInteger(idArr[j]))
	    	    			{
	    	    				alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
	    	    				return false;
	    	    			}
	    				}
	    				if(idArr.length == 2 && isInteger(idArr[0]) > isInteger(idArr[1]))
	    				{
	    					alert("TU ID" + "<%=bundle.getString("msg_invalid_entry7")%>");
    	    				return false;
	    				}
	    			}
	    		}
	    		else
	    		{
	    			if(!isInteger(tuId))
	    			{
	    				alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
	    				return false;
	    			}
	    		}
	    	}
	    }
    }
    
    var result = buildExportOptions();
    if(result == ""){
        return;
    }
    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        var url = "<%=urlNext +
            "&" + WebAppConstants.TM_ACTION +
            "=" + WebAppConstants.TM_ACTION_ANALYZE_TM%>";

        oForm.action = url;
        oForm.exportoptions.value = xmlObjToString(result.dom);

        oForm.submit();
    }
}

function isInteger(obj)
{
    return Math.floor(obj) == obj;
}

function doByEntry()
{
  document.oDummyForm.oEntryLang.disabled = true;
  document.oDummyForm.oEntryPropType.disabled = true;
}


function doTypeChanged()
{
/*
  var form = document.oDummyForm;
  var select = form.oEncoding;

  // XML, TMX, TTX use UTF-8. Everything else can select encoding.
  if (form.oType[0].checked ||
      form.oType[1].checked ||
      form.oType[2].checked ||
      form.oType[3].checked)
  {
    selectValue(select, "UTF-8")
  }
*/
}

function selectValue(select, value)
{
	var  values = value.split(",");
	if(values != null && values.length > 0)
	{
		 for(var j=0;j< values.length ;j++)
		 {
			 for (i = 0; i < select.options.length; ++i)
			 {
				 if (select.options[i].value == values[j])
				    {
				      //select.selectedIndex = i;
				      select.options[i].selected = true;
				      break;
				    }
			 }
		 }
	}
	else
	{
		 for (i = 0; i < select.options.length; ++i)
		 {
			 if (select.options[i].value == value)
			    {
			      select.selectedIndex = i;
			      return;
			    }
		 }
	}
}

function fillLanguages()
{
  var $xml = $( $.parseXML( xmlDefinition ) );
  $xml.find("statistics > languages > language").each(function(){
	var name   = $(this).find("name").text();
	var locale = $(this).find("locale").text();
	if (locale == "iw_IL")
    {
        locale = "he_IL";
    }
    oOption = document.createElement("OPTION");
    oOption.text = name;
    oOption.value = locale;
    document.oDummyForm.oEntryLang.add(oOption);
  });

  var tuCount = $xml.find("statistics > tus").text();
  if (parseInt(tuCount) == 0)
  {
    idWarning.style.display = '';
  }
}

function fillProjects(){
    var $xml = $( $.parseXML( xmlProject ) );
    $xml.find("statistics > projects > project").each(function(){
    	var name   = $(this).find("name").text();
    	opt = document.createElement("OPTION");
        opt.text = opt.value = name;
        oDummyForm.oEntryPropType.add(opt);
    });
}

function fillAttributes()
{
    $("#choiceListAttributeValue").hide();
    $("#addAttributeButton").hide();
    
	var $xml = $( $.parseXML( xmlAttribute ) );
    $xml.find("attributes > attribute").each(function(){
    	var name   = $(this).find("name").text();
    	opt = document.createElement("OPTION");
        opt.text = opt.value = name;
        oDummyForm.oEntryAttribute.add(opt);
    });
}
 
function getAttributeOptions(value)
{
	if(value == "")
	{
		$("#choiceListAttributeValue").hide();
	    $("#addAttributeButton").hide();
	}
	else
	{
		$("#addAttributeButton").show();
		
		var $xml = $( $.parseXML( xmlAttribute ) );
	    $xml.find("attributes > attribute").each(function()
	   	{
	    	var name  = $(this).find("name").text();
	    	if(name == value)
	    	{
		    		document.getElementById("choiceListAttributeValue").options.length=0; 
		    		$(this).find("values > value").each(function()
				    {
		    			opt = document.createElement("OPTION");
		    	        opt.text = opt.value = $(this).text();
		    	        oDummyForm.choiceListAttributeValue.add(opt);
			    	});
		    		$("#choiceListAttributeValue").show();
		    	    $("#textAttributeValue").hide();
	    	}
	    });
 
	}
}
 
function addAttribute()
{
	var key = $("#idAttributeList").val();
	var value = $("#choiceListAttributeValue").val();
	if(value == '' || value == null)
	{
		alert("Please select attribute value.");
		return;
	}

	opt = document.createElement("OPTION");
    opt.text = opt.value = opt.title = key + ":" + value;
    var added = false;
    $("#allItems").children("option").each(function()
	{
		if($(this).val() == key + ":" + value)
		{
			added = true;
			alert("You have added this attribute.");
		}
	});
	if(!added)
	{
    	oDummyForm.allItems.add(opt);	
    	setOptionColor();
	}
}

function removeAddedAttribute()
{
	var selectBox = document.getElementById("allItems");
    var options =  selectBox.options;
    for (var i = options.length-1; i>=0; i--)
    {
        if (options[i].selected)
        {
        	selectBox.remove(i);
        }
    }
    
    setOptionColor();
}

function setOptionColor()
{
	var options =  document.getElementById("allItems").options;
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

function fillEncodings()
{
    var form = document.oDummyForm;
    var options = form.oEncoding.options;
    for (i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }

    var option = document.createElement("OPTION");
    option.text = option.value = "UTF-8";
    options.add(option);

    var option = document.createElement("OPTION");
    option.text = option.value = "UTF-16LE";
    options.add(option);

    var option = document.createElement("OPTION");
    option.text = option.value = "UTF-16BE";
    options.add(option);
}

function fillPropTypes(){
    var form = document.oDummyForm;
    var options = form.oEntryPropType.options;
    var option = document.createElement("OPTION");
    option.text = option.value = "<%=propTypes[0]%>";
    options.add(option);
    
    var option = document.createElement("OPTION");
    option.text = option.value = "<%=propTypes[1]%>";
    options.add(option);
    
    var option = document.createElement("OPTION");
    option.text = option.value = "<%=propTypes[2]%>";
    options.add(option);
    
    var option = document.createElement("OPTION");
    option.text = option.value = "<%=propTypes[3]%>";
    options.add(option);
    
    var option = document.createElement("OPTION");
    option.text = option.value = "<%=propTypes[4]%>";
    options.add(option);
}

function checkEmptyTM()
{ 
  var $xml = $( $.parseXML( xmlDefinition ) );  
  var tuCount = $xml.find("statistics > tus").text();

  if (parseInt(tuCount) == 0)
  {
    idWarning.style.display = '';
  }
}

function doOnLoad()
{
	$("#idCos").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idCoe").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#idCoe").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idCos").datepicker( "option", "maxDate", selectedDate );
		}
	});
	
	$("#idMos").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idMoe").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#idMoe").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idMos").datepicker( "option", "maxDate", selectedDate );
		}
	});
	
	$("#idLtUgs").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idLtUge").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#idLtUge").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#idLtUgs").datepicker( "option", "maxDate", selectedDate );
		}
	});
   // Load the Guides
   loadGuides();

   fillLanguages();
   fillEncodings();
   //fillPropTypes();
   
   fillProjects();
   selectValue(document.oDummyForm.oEncoding, "UTF-8");

   fillAttributes();

   parseExportOptions();

   checkEmptyTM();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>
</FORM>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<DIV id="idWarning" style="color: red; display: none;">
<%=bundle.getString("lb_tm_export_note_tm_is_empty")%>
<BR><BR>
</DIV>

<FORM NAME="oDummyForm" ACTION="" METHOD="post">
<!-- 
<div style="margin-bottom:10px">
<%=bundle.getString("lb_select_entries_to_export")%>:<BR>
  <div style="margin-left: 40px">
      <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 CLASS="standardText">
          <tr align="left" valign="center">
            <td colspan=2><input type="radio" name="oEntries" id="idEntries1" CHECKED onclick="doByEntry()">
              <label for="idEntries1"><%= bundle.getString("lb_entire_tm")%></label>
            </td>
          </tr>
          <tr>
	    	<td>
	       		<input type="radio" name="oEntries" id="idEntries2"
                 onclick="idLanguageList.disabled = false;idPropTypeList.disabled = true;idLanguageList.focus();">
               <label for="idEntries2"><%= bundle.getString("lb_by_language")%></label>
            </td>
            <td><select name="oEntryLang" id="idLanguageList" disabled onchange="doByLanguage()"  MULTIPLE  size="6"></select></td>
          </tr>
          <tr>
            <td><input type="radio" name="oEntries" id="idEntries3"
                onclick="idPropTypeList.disabled = false;idLanguageList.disabled = true;idPropTypeList.focus();">
                <label for="idEntries3"><%= propTypeTitle%></label>
            </td>
            <td><select name="oEntryPropType" id="idPropTypeList" disabled onchange="doByProject()" MULTIPLE></select></td>
          </tr>
        </TABLE>
    <br/>
  </div>
</div>
 -->
 
<BR>
 
<div style="margin-bottom:10px">
 <%=bundle.getString("lb_select_entries_to_export")%>: <BR> 
 <div style="margin-left: 40px">
  <TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
   <thead>
    <col align="right" valign="baseline" class="standardText">
    <col align="left"  valign="baseline" class="standardText">
   </thead>
   <!-- language -->
   <tr>
   	 <td valign="middle"><%= bundle.getString("lb_by_language")%>:</td>
     <td><select name="oEntryLang" id="idLanguageList" style="width:203px;" MULTIPLE  size="6"></select></td>
   </tr>
   <!-- project -->
   <tr>
        <td valign="middle"><%= propTypeTitle%>:</td>
        <td><select name="oEntryPropType" id="idPropTypeList" style="width:203px;" MULTIPLE></select></td>
   </tr>
   <%--TU ID --%>
   <tr>
 	<td><%=bundle.getString("lb_export_tu_id") %>:</td>
    <td>
      <input name="fltTuId" id="fltTuId" type="text" size="30">
    </td>
   </tr>
   <%--JOB ID --%>
   <tr>
 	<td><%=bundle.getString("lb_job_id") %>:</td>
    <td>
      <input name="fltJobId" id="fltJobId" type="text" size="30">
    </td>
   </tr>
   <%-- SID --%>
   <tr>
 	<td><%=bundle.getString("lb_export_sid") %>:</td>
    <td>
      <input name="fltSID" id="fltSID" type="text" size="30">
      <input name="fltIsRegex" id="fltIsRegex" type="checkbox"><%=bundle.getString("lb_export_regex") %>
    </td>
   </tr>
    <%--Create User --%>
   <tr>
 	<td><%=bundle.getString("lb_export_create_user") %> :</td>
    <td>
      <input name="fltCreatedUser" id="fltCreatedUser" type="text" size="30">
    </td>
   </tr>
    <%--Modify User --%>
   <tr>
 	<td><%=bundle.getString("lb_export_modify_user") %>:</td>
    <td>
      <input name="fltModifiedUser" id="fltModifiedUser" type="text" size="30">
    </td>
   </tr>
   <%-- Creation date --%>
   <tr>
 	<td><%=bundle.getString("lb_creation_date") %>:</td>
    <td>
      <%=bundle.getString("lb_start").toLowerCase() %>: 
      <input name="fltCreatedAfter" id="idCos" type="text" size="15" >
      <%=bundle.getString("lb_end").toLowerCase() %>: 
      <input name="fltCreatedBefore" id="idCoe" type="text" size="15">
      <span class='info'>(MM/DD/YYYY)</span>
    </td>
   </tr>
   <%-- Modify date --%>
   <tr>
 	<td><%=bundle.getString("lb_modify_date") %>:</td>
    <td>
      <%=bundle.getString("lb_start").toLowerCase() %>: 
      <input name="fltModifiedAfter" id="idMos" type="text" size="15">
      <%=bundle.getString("lb_end").toLowerCase() %>: 
      <input name="fltModifiedBefore" id="idMoe" type="text" size="15">
      <span class='info'>(MM/DD/YYYY)</span>
    </td>
   </tr>
   <%-- last usage date --%>
   <tr>
 	<td><%=bundle.getString("lb_last_usage_date") %>:</td>
    <td>
      <%=bundle.getString("lb_start").toLowerCase() %>: 
      <input name="fltLastUsageAfter" id="idLtUgs" type="text" size="15">
      <%=bundle.getString("lb_end").toLowerCase() %>: 
      <input name="fltLastUsageBefore" id="idLtUge" type="text" size="15">
      <span class='info'>(MM/DD/YYYY)</span>
    </td>
   </tr>
  </TABLE>
 </div>
 <div style="margin-bottom:10px">
 <div style="margin-left: 40px">
  <TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <tr>
  	<td valign="top">
  		TM Attribute Name:
  		<select name="oEntryAttribute" id="idAttributeList" style="width:200px" onchange="getAttributeOptions(this.value)">
	   	<option value="">Choose...</option>
	    </select>
   </td>
   <td valign="top">
   		<select name="choiceListAttributeValue" id="choiceListAttributeValue" style="width:200px"></select>
   </td>
   <td valign="top">
   		<input name="addAttributeButton" id="addAttributeButton" type="button" value="Add" onclick="addAttribute()">
   <td>
  </tr>
  <tr>
  <td valign="top" align="right">
  	<select id="allItems" style="width: 200px" size="5" name="allItems">
	</select>
  </td>
  <td valign="top" colspan="2">
  	<input name="remove" type="button" value="Remove" onclick="removeAddedAttribute()">
  </td>
  </tr>
  </TABLE>
 </div>
</div>
 </div>


<BR>
<div style="margin-bottom:10px">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR VALIGN="TOP">
    <TD WIDTH=100><%=bundle.getString("lb_terminology_export_format")%></TD>
    <TD>
      <input type="radio" name="oType" id="idXml" CHECKED="true"
      onclick="doTypeChanged()"><label for="idXml">
      <%= bundle.getString("lb_tm_export_format_gtmx") %></label></input>
      <BR>
      <input type="radio" name="oType" id="idTmx1"
      onclick="doTypeChanged()"><label for="idTmx1">
      <%= bundle.getString("lb_tm_export_format_tmx1") %></label></input>
      <BR>
      <input type="radio" name="oType" id="idTmx2"
      onclick="doTypeChanged()"><label for="idTmx2">
      <%= bundle.getString("lb_tm_export_format_tmx2") %></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmx"
      onclick="doTypeChanged()"><label for="idTtmx">
      <%= bundle.getString("lb_tm_export_format_ttmx") %></label></input>
      <BR>
    </TD>
  </TR>
</TABLE>
</div>

<div style="margin-bottom:10px">
<span style="width:100px"><%=bundle.getString("lb_file_encoding")%></span>
<SELECT name="oEncoding" id="idEncoding"></SELECT><BR/>
</div>

<div style="margin-bottom:10px">
<INPUT type="checkbox" name="oChangeCreationId" id="idChangeCreationId" />
<span style="width:400px"><%=bundle.getString("lb_tm_export_change_creationid_for_mt")%></span>
</div>

</FORM>
<BR>

<DIV id="idButtons" align="left">
<button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
<button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_next")%></button>
</DIV>


<BR>
<TABLE>
<TR><TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD></tr>
<TR><TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR><%=bundle.getString("lb_tmx_logo_text2")%></SPAN></TD></TR>
</TABLE>

</BODY>
</HTML>