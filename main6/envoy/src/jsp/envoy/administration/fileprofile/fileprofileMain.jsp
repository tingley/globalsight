
<%@page import="com.globalsight.everest.cvsconfig.CVSFileProfile"%><%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission, 
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants,
         com.globalsight.everest.util.comparator.FileProfileComparator,
         com.globalsight.everest.foundation.SearchCriteriaParameters,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.cxe.entity.xmldtd.XmlDtdImpl,
         java.util.ArrayList,
         java.util.Locale, java.util.Hashtable, java.util.ResourceBundle,
         com.globalsight.everest.projecthandler.FileProfileSearchParameters"
         session="true" %>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="fileprofiles" scope="request"
 class="java.util.ArrayList" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="advsearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="templates" scope="request" class="java.util.ArrayList" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER); 
  String newURL = new1.getPageURL() + "&action=" + FileProfileConstants.CREATE;
  String editURL = edit.getPageURL() + "&action=" + FileProfileConstants.EDIT;
  String removeURL = remove.getPageURL() + "&action=" + FileProfileConstants.REMOVE;
  String selfURL = self.getPageURL();
  String title= bundle.getString("lb_file_profiles");
  String helperText = bundle.getString("helper_text_file_profile");
  String action = FileProfileConstants.ACTION;
  //Button names
  String searchButton = bundle.getString("lb_search");

  //Urls of the links on this page
  String advsearchUrl = advsearch.getPageURL() + "&" + action + "=" + FileProfileConstants.ADV_SEARCH_ACTION;
  String searchUrl = search.getPageURL() + "&" + action + "=" + FileProfileConstants.SEARCH_ACTION;
    
  String confirmRemove = bundle.getString("msg_confirm_file_profile_removal");
  String preReqData = (String)request.getAttribute("preReqData");
  Hashtable l10nprofiles = (Hashtable) request.getAttribute("l10nprofiles");
  ArrayList<CVSFileProfile> existCVSFPs = (ArrayList<CVSFileProfile>) request.getAttribute("existCVSFPs");

  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
  
 
  
  // messages                           
    String removeWarning = bundle.getString("jsmsg_wf_template_remove");    
    FileProfileSearchParameters fromSearch =
      (FileProfileSearchParameters)sessionManager.getAttribute("fromSearch");
    String emptyMsg = "msg_no_file_profiles";
    if (fromSearch != null)
    {
        emptyMsg = "msg_no_file_profiles";
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "fileProfiles";
var helpFile = "<%=bundle.getString("help_file_profiles_main_screen")%>";

function submitForm(button)
{
   if (button == "New")
	    {
<%
	        if (preReqData != null)
	        {
%>	            
				alert("<%=preReqData%>");
	            return;
<%	
	        }
%>	       	
	        fpForm.action = "<%=newURL%>";
	    }
      else if (button == "Search") 
	        {
	       	 	fpForm.action = "<%=searchUrl%>"
	        }
	  else if(fpForm.radioBtn != null) 
	        {
	        	var radio = document.getElementsByName("radioBtn");
	        	if(radio.length)
		        {
		        	 value = getRadioValue(fpForm.radioBtn);
		        	 varray = value.split(",");
		        	 if (button == "Edit")
				        {
				           fpForm.action = "<%=editURL%>";
				        }
			        else if (button == "Remove") 
			        {
				        if (varray[1] == "1") {
					        alert('The file profile is referred by CVS file profile. Please remove referred CVS file profile first.');
					        return false;
				        } else {
				            if (!confirm('<%=confirmRemove%>'))
				            {
				            	 return false;
				            }
				        } 
				        fpForm.action = "<%=removeURL%>";
			        }
		        }
	        }
    fpForm.submit();
    return;

}

function enableButtons()
{
    if (fpForm.removeBtn)
        fpForm.removeBtn.disabled = false;
    if (fpForm.editBtn)
        fpForm.editBtn.disabled = false;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="fpForm" method="post">

 <table border="0" class="standardText" cellpadding="2">
  <tr>
    <td class="standardText">
      <%=bundle.getString("lb_name")%>:
    </td>
    <td class="standardText">
      <select name="nameOptions">
	<option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'>
	  <%= bundle.getString("lb_begins_with") %>
	</option>
	<option value='<%=SearchCriteriaParameters.ENDS_WITH%>'>
	  <%= bundle.getString("lb_ends_with") %>
	</option>
	<option value='<%=SearchCriteriaParameters.CONTAINS%>'>
	  <%= bundle.getString("lb_contains") %>
	</option>
      </select>
      <input type="text" size="30" name="nameField" value=""/>
    </td>
    <td>
      <input type="button" value="<%=searchButton%>..." onclick="submitForm('Search');"/>
    </td>
    <td class="standardText" style="padding-bottom: 2px">
      <a class="standardHREF" href="<%=advsearchUrl%>"><%=bundle.getString("lb_advanced_search") %></a>
    </td>
  </tr>
 </table>
 <p>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="fileprofiles" key="<%=FileProfileConstants.FILEPROFILE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="fileprofiles" id="fp"
                     key="<%=FileProfileConstants.FILEPROFILE_KEY%>"
                     dataClass="com.globalsight.cxe.entity.fileprofile.FileProfileImpl"
                     pageUrl="self"
                     emptyTableMsg="<%=emptyMsg%>" >
                <amb:column label="">
                    <input type="radio" id="radioBtn" name="radioBtn" value="<%=fp.getId()%>,<%=existCVSFPs.contains(String.valueOf(fp.getId())) ? "1" : "0" %>"
                        onclick="enableButtons()">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=FileProfileComparator.NAME%>"
                    width="150px">
                    <%= fp.getName() %>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=FileProfileComparator.DESC%>"
                    width="260px">
                     <% out.print(fp.getDescription() == null ?
                         "" : fp.getDescription()); %>
                </amb:column>
                <amb:column label="lb_loc_profile" sortBy="<%=FileProfileComparator.LP%>" width="120px">
                     <% 
                        long id = fp.getL10nProfileId();
                        out.print(l10nprofiles.get(new Long(id)));
                     %> 
                </amb:column>
                <amb:column label="lb_loc_filter_name" sortBy="<%=FileProfileComparator.FILTER_NAME%>" width="120px">
                     <% 
                        String filterName = fp.getFilterName();
                        out.print(filterName);
                     %> 
                </amb:column>
                <amb:column label="lb_loc_xml_dtd_name" width="120px">
                     <% 
                        XmlDtdImpl xmlDtd = fp.getXmlDtd();
                        if (xmlDtd != null)
                        {
                    	    out.print(xmlDtd.getName());
                        }
                     %> 
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=FileProfileComparator.ASC_COMPANY%>">
                    <%=CompanyWrapper.getCompanyNameById(fp.getCompanyId())%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.FILE_PROFILES_REMOVE%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
            name="removeBtn" disabled onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.FILE_PROFILES_EDIT%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" disabled onClick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.FILE_PROFILES_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
             onClick="submitForm('New');">
    </amb:permission> 
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
