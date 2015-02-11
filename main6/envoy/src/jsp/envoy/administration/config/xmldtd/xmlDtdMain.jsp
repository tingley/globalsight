<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdConstant,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.cxe.entity.xmldtd.XmlDtdImpl,
            com.globalsight.everest.util.comparator.XmlDtdComparator,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.company.CompanyWrapper,java.util.*"
    session="true"
%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dup" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rem" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL() + "&action=" + XmlDtdConstant.NEW;
  String editURL = edit.getPageURL() + "&action=" + XmlDtdConstant.EDIT;
  String remURL = rem.getPageURL() + "&action=" + XmlDtdConstant.REMOVE;
  String title= bundle.getString("lb_xml_dtds");
  String helperText = bundle.getString("helper_text_xml_dtds");
  String confirmRemove = bundle.getString("msg_remove_xml_dtd");
  String invalid = (String)request.getAttribute("invalid"); 
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<!-- xmlrulefile/xmlDtdMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "xmlDtds";
var helpFile = "<%=bundle.getString("help_xml_dtds_main_screen")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        xmlForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(xmlForm.selectXmlDtdIds);

        if (button == "Edit")
        {
            xmlForm.action = "<%=editURL%>" + "&id=" + value;
        }
        else if (button == "Remove")
        {
            var referencedDtdNames = "";
            var dtds = document.getElementsByName("selectXmlDtdIds");
            
            for (var i = 0; i < dtds.length; i++)
            {
                if (dtds[i].checked)
                {
                    var dtdID = dtds[i].value;
                    var isReferenced_dtdname = document.getElementById(dtdID).value;
                    
                    var isReferenced = false;
                    var dtdName = "";
                    if (isReferenced_dtdname.indexOf("true") > -1)
                    {
                    	isReferenced = true;
                    	dtdName = isReferenced_dtdname.substring(isReferenced_dtdname.indexOf("true")+5);
                    }

                    if (isReferenced == true)
                    {
                        if (referencedDtdNames.length > 0)
                        {
                            referencedDtdNames = referencedDtdNames.concat(", ");
                        }
                        
                        referencedDtdNames = referencedDtdNames.concat(dtdName);
                    }
                }
            }
            
            if (referencedDtdNames.length > 0)
            {
                alert("<%=bundle.getString("lb_dtdRemoveWarning")%>\n".concat(referencedDtdNames));
                isOk = false;
            }
            else if (!confirm('<%=confirmRemove%>'))
            {
                isOk = false;
            }
            xmlForm.action = "<%=remURL%>";
        }
    }

    if (isOk)
    {
        xmlForm.submit();
    }
}

function enableButtons()
{
    if (xmlForm.editBtn)
        xmlForm.editBtn.disabled = false;
    if (xmlForm.dupBtn)
        xmlForm.dupBtn.disabled = false;
    if (xmlForm.remBtn)
        xmlForm.remBtn.disabled = false;    
}


function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = xmlForm.selectXmlDtdIds;
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
    
    if (xmlForm.editBtn)
    {
        if (selectedIndex.length != 1)
        {
            xmlForm.editBtn.disabled = true;
        }
        else
        {
            xmlForm.editBtn.disabled = false;
        }
    }
    
    if (xmlForm.remBtn)
    {
        if (selectedIndex.length > 0)
        {
            xmlForm.remBtn.disabled = false;
        }
        else
        {
            xmlForm.remBtn.disabled = true;
        }
    }
}

//for GBS-2599
function handleSelectAll() {
	if (xmlForm && xmlForm.selectAll) {
		if (xmlForm.selectAll.checked) {
			checkAllWithName('xmlForm', 'selectXmlDtdIds');
			setButtonState();
	    }
	    else {
			clearAll('xmlForm'); 
			setButtonState();
	    }
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<span class=errorMsg><%
    if (invalid != null && invalid.length() > 0) out.print(invalid);
%></span>

<form name="xmlForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="xmlDtds" key="<%=XmlDtdConstant.XMLDTD_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="xmlDtds" id="xmlDtd"
       key="<%=XmlDtdConstant.XMLDTD_KEY%>"
       dataClass="com.globalsight.cxe.entity.xmldtd.XmlDtdImpl"
       pageUrl="self"
       emptyTableMsg="msg_no_xmldtdfile" >
        <amb:column label="checkbox">
          <input type="checkbox" name="selectXmlDtdIds" value="<%=xmlDtd.getId()%>" onclick="setButtonState()">
           <%
             boolean isReferenced = xmlDtd.referenced();
             String dtdName = xmlDtd.getName();
             String hiddenStr = "" + isReferenced + "_" + dtdName;
           %>
           <input type="hidden" id="<%=xmlDtd.getId()%>" value="<%=hiddenStr%>"/>
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=XmlDtdComparator.NAME%>">
            <%String url = editURL + "&id=" + xmlDtd.getId();%>          
             <amb:permission name="<%=Permission.XMLDTD_EDIT%>" >
                <A name='nameLink' class='standardHREF' href="<%=url%>">
             </amb:permission>
            <%=xmlDtd.getName()%>
             <amb:permission name="<%=Permission.XMLDTD_EDIT%>" >
                </A> 
             </amb:permission>           
        </amb:column>
        <amb:column label="lb_file_number" align="center" sortBy="<%=XmlDtdComparator.FILE_NUMBER%>" width="80px;">
              <%
                  int n = xmlDtd.getFileNumber();
                  if (n == 0){
                      out.print("<span class=\"asterisk\">" + n + "</span>");   
                  } else {
                      out.print(Integer.toString(n));   
                  }             
              %>
        </amb:column>      
        <amb:column label="lb_description" sortBy="<%=XmlDtdComparator.DESC%>"
         width="400px">
          <%
            out.print(xmlDtd.getDescription() == null ? "" :
                      xmlDtd.getDescription());
          %>
        </amb:column>
        <%
            if (isSuperAdmin) {
        %>
        <amb:column label="lb_company_name" sortBy="<%=XmlDtdComparator.ASC_COMPANY%>">
            <%=CompanyWrapper.getCompanyNameById(xmlDtd.getCompanyId())%>
        </amb:column>
        <% } %>
      </amb:table>
      
      <!--for gbs-2599
	  DIV ID="CheckAllLayer" style="float: left; margin-left:10px;">
        <A CLASS="standardHREF"
           HREF="javascript:checkAllWithName('xmlForm', 'selectXmlDtdIds'); setButtonState()"><%=bundle.getString("lb_check_all")%></A> |
        <A CLASS="standardHREF"
           HREF="javascript:clearAll('xmlForm'); setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
    </DIV-->
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.XMLDTD_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="remBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLDTD_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
      name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLDTD_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</BODY>
</HTML>