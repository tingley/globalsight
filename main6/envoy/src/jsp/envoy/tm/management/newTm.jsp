<%@page import="com.globalsight.cxe.entity.customAttribute.TMAttributeCons"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.gsedition.GSEdition,
        com.globalsight.everest.projecthandler.ProjectTM,
        com.globalsight.util.FormUtil"
    session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
    WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_DEFINITION);
sessionMgr.removeElement(WebAppConstants.TM_DEFINITION);

List allGSEditions = (List) sessionMgr.getAttribute("allGSEdition");

ProjectTM modifyProjectTM = (ProjectTM) sessionMgr.getAttribute("modifyProjectTM");
sessionMgr.removeElement("modifyProjectTM");

Map remoteFpIdNames = (HashMap) sessionMgr.getAttribute("remoteFpIdNames");
sessionMgr.removeElement("remoteFpIdNames");

boolean modifiedFPStillExist = false;
if (remoteFpIdNames != null && remoteFpIdNames.size() > 0 && modifyProjectTM != null)
{
	modifyProjectTM.getRemoteTmProfileId();
	Set allFpIds = remoteFpIdNames.keySet();
	if (allFpIds.contains(modifyProjectTM.getRemoteTmProfileId()))
	{
		modifiedFPStillExist = true;
	}
}

String str_tmid =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_ID);
String tmAvailableAtts = (String)request.getAttribute(WebAppConstants.TM_AVAILABLE_ATTS);
String tmTMAtts = (String)request.getAttribute(WebAppConstants.TM_TM_ATTS);

String urlCancel = cancel.getPageURL();
String urlOK     = ok.getPageURL();

// Perform error handling, then clear out session attribute.
String str_error =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML XMLNS:gs>
<!-- This is envoy/tm/management/newTm.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_tm")%></TITLE>
<link rel="stylesheet" type="text/css" href="/globalsight/envoy/tm/management/tm.css"/>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<script SRC="/globalsight/includes/Ajax.js"></script>
<script SRC="/globalsight/includes/dojo.js"></script>
<script SRC="/globalsight/includes/json2.js"></script>
<script SRC="/globalsight/includes/json2.js"></script>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<!-- for jQuery -->
<link rel="stylesheet" type="text/css" href="/globalsight/includes/jquery-ui-custom.css"/>
<script src="/globalsight/jquery/jquery-1.6.4.min.js" type="text/javascript"></script>
<script src="/globalsight/includes/jquery-ui-custom.min.js" type="text/javascript"></script>
<script src="/globalsight/includes/Array.js" type="text/javascript"></script>
<script src="/globalsight/includes/filter/StringBuffer.js" type="text/javascript"></script>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "tm";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_create_modify")%>";

</SCRIPT>
<SCRIPT language="Javascript">
var str_error = "<%=str_error == null ? "" : str_error%>";
var tmid = "<%=str_tmid%>";
var isModify = false;

var strAvailableAttnames = "<%=tmAvailableAtts %>";
var strTMAtts = "<%=tmTMAtts %>";

var arrayAvailableAttnames = new Array();
var arrayTMAtts = new Array();

if (strAvailableAttnames != null)
{
	arrayAvailableAttnames = strAvailableAttnames.split(",");
}

if (strTMAtts != null)
{
	var temparray = strTMAtts.split(",");
	for (var i = 0; i < temparray.length; i++)
	{
		var ttt = temparray[i].split(":");
		var tmAtt = new Object();
		tmAtt.attributename = ttt[0];
		tmAtt.settype = ttt[1];

		arrayTMAtts[arrayTMAtts.length] = tmAtt;
	}
}

function doAddAttribute()
{
	var attname = document.getElementById("attname").value;
	var settype = document.getElementById("attsettype").value;

	//alert(attname);

	if (attname == "")
	{
		return;
	}

	arrayAvailableAttnames.removeData(attname);
	var tmAtt = new Object();
	tmAtt.attributename = attname;
	tmAtt.settype = settype;
	arrayTMAtts[arrayTMAtts.length] = tmAtt;

	initAttbutesUI();
}

function removeTMAttribute(attName)
{
	for (var i = 0; i < arrayTMAtts.length; i++)
	{
		var tmatt = arrayTMAtts[i];

		if (tmatt.attributename == attName)
		{
			arrayAvailableAttnames.appendUniqueObj(attName);
			arrayTMAtts.splice(i, 1);
			break;
		}
	}

	initAttbutesUI();
}

function initAttbutesUI()
{
	var objAttnameSelect = document.getElementById("attname");
	var divAtts = document.getElementById("divAtts");

	if (objAttnameSelect && divAtts)
	{
	objAttnameSelect.options.length = 0;
	for(var i = 0; i < arrayAvailableAttnames.length; i++)
	{
		var attname = arrayAvailableAttnames[i];
		var varItem = new Option(attname, attname);
		objAttnameSelect.options.add(varItem);
	}

	var ccc = new StringBuffer("<table style='width:100%'>");
	ccc.append("<tr class='thead_tr'><td class='thead_td'>Attribute Internal Name</td><td class='thead_td'>Set during TM update</td><td class='thead_td'>Delete</td></tr>");
	for(var i = 0; i < arrayTMAtts.length; i++)
	{
		var tmatt = arrayTMAtts[i];
		if (tmatt.settype)
		{
			var backgroundColor = "#C7CEE0";
			if(i % 2 == 0)
			{
				backgroundColor = "#DFE3EE";
			}
			ccc.append("<tr style='background-color:");
			ccc.append(backgroundColor);
			ccc.append("'><td class='standardText'>");
			ccc.append(tmatt.attributename);
			ccc.append("</td><td class='standardText'>");
			ccc.append(getSetTypeStr(tmatt.settype));
			ccc.append("</td><td class='standardText' align='center'><a href='#' onclick='removeTMAttribute(\"");
			ccc.append(tmatt.attributename);
			ccc.append("\")'>X</a></td></tr>");
		}
	}
	ccc.append("</table>");

	divAtts.innerHTML = ccc.toString();
	}
}

function getSetTypeStr(setType)
{
	if ("<%=TMAttributeCons.SET_NOT %>" == setType)
	{
		return "Not set";
	}

	if ("<%=TMAttributeCons.SET_FROM_JOBATT %>" == setType)
	{
		return "From Job Attribute of same name";
	}

	if ("<%=TMAttributeCons.SET_FROM_WFATT %>" == setType)
	{
		return "From Workflow Attribute of same name";
	}
}

function Result(message, errorFlag, element)
{
    this.message = message;
    this.error   = errorFlag;
    this.element = element;
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doOK()
{
    if (hasSpecialChars(form.<%=WebAppConstants.TM_TM_NAME%>.value))
    {
        alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    if (hasSpecialChars(form.<%=WebAppConstants.TM_TM_DOMAIN%>.value))
    {
        alert("<%= bundle.getString("lb_tm_domain") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    if (hasSpecialChars(form.<%=WebAppConstants.TM_TM_ORGANIZATION%>.value))
    {
        alert("<%= bundle.getString("lb_tm_organization") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    var isRemoteTm = document.getElementById('idRemoteTm');
    if (isRemoteTm.checked==true)
    {
    	var selectedGSEditionId = getSelectedGsEdition();
    	if (selectedGSEditionId == -1)
    	{
        	alert("<%= bundle.getString("lb_tm_gs_edition_not_selected") %>");
        	return false;
        }
    	var selectedRemoteTmProfile = getSeletedRemoteTmProfile();
        if (selectedRemoteTmProfile == "")
        {
        	alert("<%= bundle.getString("lb_tm_remote_tmprofile_not_selected") %>");
        	return false;
        }
    }

    // attibutes
    var objtmAttributes = document.getElementById("tmAttributes");
    var tmattstr = new StringBuffer("");
    if (arrayTMAtts != null && arrayTMAtts.length > 0)
    {
    	for (var i = 0; i < arrayTMAtts.length; i++)
    	{
    		var tmatt = arrayTMAtts[i];
    		if (tmatt.settype)
    		{
	    		tmattstr.append(tmatt.attributename);
	    		tmattstr.append(":");
	    		tmattstr.append(tmatt.settype);
	    		tmattstr.append(",");
    		}
    	}
    }
    objtmAttributes.value = tmattstr.toString();

    var result = buildDefinition();
    if (result.error == 0)
    {
        if (validName()) {
            form.<%=WebAppConstants.TM_TM_NAME%>.disabled = false;
            form.submit();
        }
    }
    else
    {
        if (result.element != null)
        {
            result.element.focus();
        }

        alert(result.message);
    }
}

function validName() {
	if (!isModify) {
	    var name = allTrim(form.<%=WebAppConstants.TM_TM_NAME%>.value);
	    var existNames = "<%=(String) sessionMgr.getAttribute(WebAppConstants.TM_EXIST_NAMES) %>";

	    var lowerName = name.toLowerCase();
	    existNames = existNames.toLowerCase();

	    if (existNames.indexOf("," + lowerName + ",") != -1) {
	        alert(name + " is exist. Please input another new translation memory name.");
	        form.<%=WebAppConstants.TM_TM_NAME%>.focus();
	        return false;
	    }
	    else
	    	form.<%=WebAppConstants.TM_TM_NAME%>.value = name;
	        return true;
	} else 
		return true;
}

function buildDefinition()
{
    var name = Trim(form.<%=WebAppConstants.TM_TM_NAME%>.value);
    if (name == "")
    {
      return new Result("Please enter a name.", 1,
        form.<%=WebAppConstants.TM_TM_NAME%>);
    }

    return new Result('', 0, null);
}

function parseDefinition()
{
    var xmlStr = "<%=xmlDefinition.replace("\n","").replace("\r","").trim()%>";
    var $xml = $( $.parseXML( xmlStr ) );

    var nameNode = $xml.find("tm > name");
    if (nameNode != null && nameNode.text() != "")
    {
      // for update, set ID, for clone don't
      form.<%=WebAppConstants.TM_TM_ID%>.value = $xml.find("tm").attr("id");
      isModify = true;
    }
    form.<%=WebAppConstants.TM_TM_NAME%>.value = $xml.find("tm > name").text();
    form.<%=WebAppConstants.TM_TM_DOMAIN%>.value = $xml.find("tm > domain").text();
    form.<%=WebAppConstants.TM_TM_ORGANIZATION%>.value = $xml.find("tm > organization").text();

	//for bug GBS-2547,by fan
	var desc = $xml.find("tm > description").text().replace(new RegExp("<br/>","gm"),"\n");
    form.<%=WebAppConstants.TM_TM_DESCRIPTION%>.value = desc;

    var indexTarget = $xml.find("tm > indexTarget").text();
    var objIndexTarget = document.getElementById('idIndexTarget');
    if (indexTarget == "true")
    {
    	objIndexTarget.checked = true;
    }
    else
   	{
    	objIndexTarget.checked = false;
   	}
    
    var isRemoteTm = $xml.find("tm > isRemoteTm").text();
    var objIsRemoteTm = document.getElementById('idRemoteTm');
	var divRemoteTmSetting = document.getElementById('idRemoteTmSetting');
    if (isRemoteTm == "true")
    {
    	objIsRemoteTm.checked = true;
    	divRemoteTmSetting.style.display = 'block';
    }
    else
    {
    	objIsRemoteTm.checked = false;
    	divRemoteTmSetting.style.display = 'none';
    }
}

function doOnLoad()
{
    // This loads the guides in guides.js and the
    loadGuides();

    if (str_error)
    {
      showError(new Error("Server Error", str_error));
    }

    parseDefinition();
    if (isModify)
    {
      tmName = form.<%=WebAppConstants.TM_TM_NAME%>.value;
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("lb_edit") + " " + bundle.getString("lb_tm"))%>" + " " + tmName;

      // can't rename for now
      form.<%=WebAppConstants.TM_TM_NAME%>.disabled = true;
      form.<%=WebAppConstants.TM_TM_DOMAIN%>.select();
      form.<%=WebAppConstants.TM_TM_DOMAIN%>.focus();
    }
    else
    {
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("lb_new") + " " + bundle.getString("lb_tm"))%>";

      form.<%=WebAppConstants.TM_TM_NAME%>.select();
      form.<%=WebAppConstants.TM_TM_NAME%>.focus();
    }

    initAttbutesUI();
}

function showOrHideEditions(object)
{
    var isChecked = object.checked;
    var divRemoteTmSetting = document.getElementById('idRemoteTmSetting');
    if (isChecked == true)
    {
    	divRemoteTmSetting.style.display = 'block';
    }
    else
    {
    	divRemoteTmSetting.style.display = 'none';
    }
}

//get all tm profiles from specified gs edition server
function getTmProfiles()
{
    var remoteTmProfile = document.getElementById("idRemoteTmProfile");
    remoteTmProfile.options.length = 0;
    remoteTmProfile.disabled = true;

	var selectedGSEditionId = getSelectedGsEdition();
    if (selectedGSEditionId != -1)
    {
        var obj = {id:selectedGSEditionId};
        sendAjax(obj, "getAllRemoteTmProfiles", "getAllRemoteTmProfiles");
    }
}

//return selected gsEdition Id
function getSelectedGsEdition()
{
    var selectedGSEditionId = -1;
	var GSEditions = document.getElementById("idGsEdition");
    if(GSEditions.length > 1)
    {
        for (var i=0; i<GSEditions.options.length; i++)
        {
            if (GSEditions.options[i].selected == true)
            {
                selectedGSEditionId = GSEditions.options[i].value;
            }
        }
    }

    return selectedGSEditionId;
}

//return selected remote tm profile
function getSeletedRemoteTmProfile()
{
    var selectedRemoteTmProfileIdName = "";
    var remoteTmProfiles = document.getElementById("idRemoteTmProfile");
	if (remoteTmProfiles.length > 0)
	{
        for (var i=0;i<remoteTmProfiles.options.length; i++)
        {
            if (remoteTmProfiles.options[i].selected == true)
            {
                selectedRemoteTmProfileIdName = remoteTmProfiles.options[i].value;
            }
        }
	}

	return selectedRemoteTmProfileIdName;
}

function getAllRemoteTmProfiles(data)
{
    allRemoteTmProfiles = eval(data);

    var remoteTmProfile = document.getElementById("idRemoteTmProfile");
    remoteTmProfile.options.length = 0;

    if (allRemoteTmProfiles != null)
    {
        for(var i = 0; i < allRemoteTmProfiles.length; i++)
        {
            var oOption = document.createElement("OPTION");
            //need save remote tm profile id and name both,so put them into 'value' together.
            oOption.value = allRemoteTmProfiles[i].tmProfileId + "_" + allRemoteTmProfiles[i].tmProfileName;
            oOption.text = allRemoteTmProfiles[i].tmProfileName;
            remoteTmProfile.options.add(oOption);
        }
    }

    remoteTmProfile.disabled = false;
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
      TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: absolute; Z-INDEX: 0; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" id="idHeading">&nbsp;</DIV>

<FORM NAME="form" METHOD="POST" action="<%=urlOK%>">
<INPUT TYPE="hidden" NAME="<%=WebAppConstants.TM_ACTION%>"
 VALUE="<%=WebAppConstants.TM_ACTION_SAVE%>">
<INPUT TYPE="hidden" NAME="<%=WebAppConstants.TM_TM_ID%>" VALUE="">
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0">
  <THEAD>
    <COL align="right" valign="top" CLASS="standardText">
    <COL align="left"  valign="top" CLASS="standardText">
  </THEAD>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_name")%><span class="asterisk">*</span>: </TD>
    <TD CLASS="standardText"><INPUT NAME="<%=WebAppConstants.TM_TM_NAME%>"
      TYPE="text" MAXLENGTH="60" SIZE="20"></TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_tm_domain")%></TD>
    <TD CLASS="standardText"><INPUT NAME="<%=WebAppConstants.TM_TM_DOMAIN%>"
      TYPE="text" MAXLENGTH="500" SIZE="40"></TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_tm_organization")%></TD>
    <TD CLASS="standardText"><INPUT NAME="<%=WebAppConstants.TM_TM_ORGANIZATION%>"
      TYPE="text" MAXLENGTH="400" SIZE="40"></TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_tm_description")%></TD>
    <TD CLASS="standardText"><TEXTAREA NAME="<%=WebAppConstants.TM_TM_DESCRIPTION%>"
      COLS="40" ROWS="3"></TEXTAREA></TD>
  </TR>
  <TR>
    <TD CLASS="standardText">Index Target:</TD>
    <TD CLASS="standardText"><input type="checkbox" ID="idIndexTarget" NAME="indexTarget"/></TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_tm_remote_tm")%></TD>
    <TD CLASS="standardText">
        <input type="checkbox" id="idRemoteTm" NAME="<%=WebAppConstants.TM_TM_REMOTE_TM%>" onclick="showOrHideEditions(this)"/>
    </TD>
  </TR>
  <amb:permission name="<%=Permission.TM_ENABLE_TM_ATTRIBUTES%>" >
  <TR>
    <TD CLASS="standardText" valign="top">TU Attributes:</TD>
    <TD CLASS="standardText">
        <div id="divAtts" style='width:100%'></div>
        <br />
        <span CLASS="standardText">(Only the attributes of Text and Choice List type are available.)</span>
        <br />
        <span CLASS="standardText">
		<select id="attname" CLASS="standardText">
		</select>
		</span>
		&nbsp;
		<span CLASS="standardText">
		<select id="attsettype" CLASS="standardText">
		<option value="<%=TMAttributeCons.SET_NOT %>">Not set</option>
		<option value="<%=TMAttributeCons.SET_FROM_JOBATT %>">From Job Attribute of same name</option>
		<!-- <option value="<%=TMAttributeCons.SET_FROM_WFATT %>">From Workflow Attribute of same name</option> -->
		</select>
		</span>
		&nbsp;
		<INPUT TYPE="BUTTON" VALUE="Add" ID="addAttribute" onclick="doAddAttribute()" />
    </TD>
  </TR>
 </amb:permission>
</TABLE>

<div id="idRemoteTmSetting" style="display:none;">
<p>
    <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="">
        <tr><td colspan="2"><b><%=bundle.getString("lb_tm_set_tmprofile") %></b></td></tr>
        <TR>
            <TD CLASS="standardText"><%=bundle.getString("lb_tm_gs_editions")%></TD>
            <TD CLASS="standardText" align="left">
                <select id="idGsEdition" class="standardText" NAME="<%=WebAppConstants.TM_TM_GS_EDITON%>" onchange="getTmProfiles()">
                <% if (allGSEditions != null && allGSEditions.size() > 0) { %>
                      <option value="-1"> </option>
                <%    for (int i=0; i<allGSEditions.size(); i++)
                      {
                	       String selected = "";
                           GSEdition edition = (GSEdition) allGSEditions.get(i);
                           long modifyGSEditionId = -1;
                           if (modifyProjectTM != null) {
                               modifyGSEditionId = modifyProjectTM.getGsEditionId();
                           }
                           if (modifyGSEditionId == edition.getId()) {
                        		  selected = "selected";
                           }
                %>
                           <option value="<%=edition.getId()%>" <%=selected%>> <%=edition.getName() %></option>
                <%    }
                   }%>

                </select>
            </TD>
        </TR>
        <TR>
            <TD CLASS="standardText"><%=bundle.getString("lb_tm_remote_tm_profile")%></TD>
            <TD CLASS="standardText" align="left">

                <select id="idRemoteTmProfile" class="standardText" NAME="<%=WebAppConstants.TM_TM_REMOTE_TM_PROFILE%>" >
                <%
                    if (modifyProjectTM != null)
                    {
                        long modifyRemoteTmProfileId = -1;
                        String modifyRemoteTmProfileName = "";
                    	modifyRemoteTmProfileId = modifyProjectTM.getRemoteTmProfileId();
                    	modifyRemoteTmProfileName = modifyProjectTM.getRemoteTmProfileName();
                        if (modifyRemoteTmProfileId != -1 && modifiedFPStillExist == true)
                        {
                %>
                   	<option value="<%=modifyRemoteTmProfileId + "_" + modifyRemoteTmProfileName%>" selected><%=modifyRemoteTmProfileName%></option>
                <%      }
                        if (remoteFpIdNames != null && remoteFpIdNames.size()>0)
                        {
                        	Iterator fpIdNameIter = remoteFpIdNames.entrySet().iterator();
                        	while (fpIdNameIter.hasNext())
                        	{
                        		Map.Entry entry = (Map.Entry )fpIdNameIter.next();
                        		long id = ((Long)entry.getKey()).longValue();
                        		String name = (String) entry.getValue();
                        		if (id != modifyRemoteTmProfileId)
                        		{
                %>
                    <option value="<%=id + "_" + name%>"><%=name%></option>
                <%
                        		}
                        	}
                        }
                    } %>
                </select>

            </TD>
        </TR>
	</TABLE>
</p></div>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_TRANSLATION_MEMORY); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
<input type="hidden" name="tmAttributes" id="tmAttributes" value="" />

</FORM>

<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="Cancel" onclick="doCancel();">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="OK" onclick="doOK();">

</DIV>

</BODY>
</HTML>
