<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
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

String urlCancel = cancel.getPageURL();
String urlOK     = ok.getPageURL();

// Perform error handling, then clear out session attribute.
String str_error =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=bundle.getString("lb_manage_tm_definition")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<script SRC="/globalsight/includes/Ajax.js"></script>
<script SRC="/globalsight/includes/dojo.js"></script>
<script SRC="/globalsight/includes/json2.js"></script>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
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
    var name = Trim(form.<%=WebAppConstants.TM_TM_NAME%>.value);
<%
    ArrayList names = (ArrayList)request.getAttribute(WebAppConstants.TM_EXIST_NAMES);
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String actname = (String)names.get(i);
%>
            if ("<%=actname%>".toLowerCase() == name.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_tm_name"))%>");
                return false;
            }
<%
        }
    }
%>

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
    var dom;
    //Mozilla compatibility  
    var xmlStr = "<%=xmlDefinition%>";

    if(ie)
    {
      dom = oDefinition.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }

    var nameNode = dom.selectSingleNode("/tm/name");
    if (nameNode != null && nameNode.text != "")
    {
      // for update, set ID, for clone don't
      form.<%=WebAppConstants.TM_TM_ID%>.value =
        dom.selectSingleNode("/tm").getAttribute("id");
    }
    form.<%=WebAppConstants.TM_TM_NAME%>.value =
      dom.selectSingleNode("/tm/name").text;
    form.<%=WebAppConstants.TM_TM_DOMAIN%>.value =
      dom.selectSingleNode("/tm/domain").text;
    form.<%=WebAppConstants.TM_TM_ORGANIZATION%>.value =
      dom.selectSingleNode("/tm/organization").text;
    form.<%=WebAppConstants.TM_TM_DESCRIPTION%>.value =
      dom.selectSingleNode("/tm/description").text;
    
    var isRemoteTm = dom.selectSingleNode("/tm/isRemoteTm").text;
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

    var dom;
    //Mozilla compatibility  
    var xmlStr = "<%=xmlDefinition%>";

    if(ie)
    {
      dom = oDefinition.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }
    
    var nameNode = dom.selectSingleNode("/tm/name");
    if (nameNode != null && nameNode.text != "")
    {
      isModify = true;
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tm_modify"))%>" + " " + nameNode.text;

      // can't rename for now
      form.<%=WebAppConstants.TM_TM_NAME%>.disabled = true;
      form.<%=WebAppConstants.TM_TM_DOMAIN%>.select();
      form.<%=WebAppConstants.TM_TM_DOMAIN%>.focus();
    }
    else
    {
      isModify = false;
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tm_define_new"))%>";

      form.<%=WebAppConstants.TM_TM_NAME%>.select();
      form.<%=WebAppConstants.TM_TM_NAME%>.focus();
    }
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

<XML id="oDefinition" style="display:none"><%=xmlDefinition%></XML>

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
    <TD CLASS="standardText"><%=bundle.getString("lb_tm_remote_tm")%></TD>
    <TD CLASS="standardText">
        <input type="checkbox" id="idRemoteTm" NAME="<%=WebAppConstants.TM_TM_REMOTE_TM%>" onclick="showOrHideEditions(this)"/>
    </TD>
  </TR>
 
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

</FORM>

<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="Cancel" onclick="doCancel();">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="OK" onclick="doOK();">

</DIV>

</BODY>
</HTML>
