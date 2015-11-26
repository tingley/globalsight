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
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.foundation.SearchCriteriaParameters,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.cxe.entity.xmldtd.XmlDtdImpl,
         java.util.ArrayList,
         java.util.Locale, java.util.HashMap, java.util.ResourceBundle,
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
  SessionManager sessionMgr =
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
  //String advsearchUrl = advsearch.getPageURL() + "&" + action + "=" + FileProfileConstants.ADV_SEARCH_ACTION;
  String searchUrl = search.getPageURL() + "&" + action + "=" + FileProfileConstants.SEARCH_ACTION;
    
  String confirmRemove = bundle.getString("msg_confirm_file_profile_removal");
 // String preReqData = (String)request.getAttribute("preReqData");
  //Hashtable l10nprofiles = (Hashtable) request.getAttribute("l10nprofiles");
  HashMap<Long,String> idViewExtensions = (HashMap<Long,String>) request.getAttribute("idViewExtensions");
  ArrayList<CVSFileProfile> existCVSFPs = (ArrayList<CVSFileProfile>) request.getAttribute("existCVSFPs");

  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
  	
  String uNameFilter = (String) sessionMgr.getAttribute("uNameFilter");
  String uLPFilter = (String) sessionMgr.getAttribute("uLPFilter");
  String uFNFilter = (String) sessionMgr.getAttribute("uFNFilter");
  String uSourceFileFormatFilter = (String) sessionMgr.getAttribute("uSourceFileFormatFilter");
  String uCompanyFilter = (String) sessionMgr.getAttribute("uCompanyFilter");
  uNameFilter = uNameFilter == null ? "" : uNameFilter;
  uLPFilter = uLPFilter == null ? "" : uLPFilter;
  uFNFilter = uFNFilter == null ? "" : uFNFilter;
  uSourceFileFormatFilter = uSourceFileFormatFilter == null ? "" : uSourceFileFormatFilter;
  uCompanyFilter = uCompanyFilter == null ? "" : uCompanyFilter;
  
  // messages                           
  String removeWarning = bundle.getString("jsmsg_wf_template_remove");    
  FileProfileSearchParameters fromSearch =
      (FileProfileSearchParameters)sessionMgr.getAttribute("fromSearch");
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
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "fileProfiles";
var helpFile = "<%=bundle.getString("help_file_profiles_main_screen")%>";

$(
		function(){
			$("#fpForm").keydown(function(e){
				if(e.keyCode==13)
				
				{
					submitForm("search")
				
				}
				
				});
		}		
	)

function submitForm(button)
{
   if (button == "New")
	    {
	        fpForm.action = "<%=newURL%>";
	    }
   else if (button == "search")
   {
       fpForm.action = "<%=searchUrl%>";
   }
   else if(fpForm.radioBtn != null) 
	    {
	        	var radio = document.getElementsByName("radioBtn");
	        	if(radio.length)
		        {
				    var rv="";
				    $(":checkbox:checked").each(
				        function(i){
				        	rv+=$(this).val()+" ";
				        }		
				    )
				    $(":checkbox:checked").each(
				        function(i){
				        	$(this).val(rv);
				        }		
				    )
		        	 value = getRadioValue(fpForm.radioBtn);
		        	 varray = value.split(" ");
		        	 if (button == "Remove") 
			        {	        	
		        		for(var i=0;i<varray.length-1;i++){
		        			array=varray[i].split(",");
				        if (array[1] == "1") {
					        alert('The file profile is referred by CVS file profile. Please remove referred CVS file profile first.');
					        return false;
				        } 
		        		}
				            if (!confirm('<%=confirmRemove%>'))
				            {
				            	 return false;
				            }	
				            fpForm.action = "<%=removeURL%>";
				        
		        		}
			        }
		        }
	          
    fpForm.submit();
    return;

}

function modifyuser(name){
	
	var url = "<%=editURL%>&&radioBtn=" + name;

	fpForm.action = url;

	fpForm.submit();
				  
			
}
function handleSelectAll() {
	var ch = $("#selectAll").attr("checked");
	if (ch == "checked") {
		$("[name='radioBtn']").attr("checked", true);
	} else {
		$("[name='radioBtn']").attr("checked", false);
	}
	buttonManagement();
}
function buttonManagement()
{
	var count = $("input[name='radioBtn']:checked").length;
	if (count == 0) {
	    	$("#removeBtn").attr("disabled", true);
		} else {
			$("#removeBtn").attr("disabled", false);
		}	
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	fpForm.action = "<%=selfURL%>";
    	fpForm.submit();
    }
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

<form name="fpForm" id="fpForm" method="post">

    <table  cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="fileprofiles" key="<%=FileProfileConstants.FILEPROFILE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="fileprofiles" id="fp" hasFilter="true"
                     key="<%=FileProfileConstants.FILEPROFILE_KEY%>"
                     dataClass="com.globalsight.cxe.entity.fileprofile.FileprofileVo"
                     pageUrl="self"
                     emptyTableMsg="<%=emptyMsg%>" >
                <amb:column label="checkbox"  width="2%">
                	<input type="checkbox" name="radioBtn" id="<%=fp.getId()%>" 
                	value="<%=fp.getId()%>,<%=existCVSFPs.contains(String.valueOf(fp.getId())) ? "1" : "0" %>" onclick="buttonManagement()">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=FileProfileComparator.NAME%>"  filter="uNameFilter" filterValue="<%=uNameFilter%>"  width="150px">
                    <amb:permission name="<%=Permission.FILE_PROFILES_EDIT%>" ><a href='javascript:void(0)' title='Edit FileProfile' onclick="modifyuser('<%= fp.getId() %>')"></amb:permission>
                    <%= fp.getName() %>
                    <amb:permission name="<%=Permission.FILE_PROFILES_EDIT%>" ></a></amb:permission>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=FileProfileComparator.DESC%>"
                    width="260px">
                     <% out.print(fp.getDescription() == null ?
                         "" : fp.getDescription()); %>
                </amb:column>
                <amb:column label="lb_loc_profile" sortBy="<%=FileProfileComparator.LP%>"  filter="uLPFilter" filterValue="<%=uLPFilter%>" width="20%">
                     <% 
                        out.print(fp.getLocName());
                     %> 
                </amb:column>
                  <amb:column label="lb_source_file_format" sortBy="<%=FileProfileComparator.FORMATTYPES_NAME%>" filter="uSourceFileFormatFilter" filterValue="<%=uSourceFileFormatFilter%>" width="15%">
                     <% 
                        String formatTypesName = fp.getFormatName();
                        out.print(formatTypesName);
                     %> 
                </amb:column>
                 <amb:column label="lb_loc_filter_name" sortBy="<%=FileProfileComparator.FILTER_NAME%>" filter="uFNFilter" filterValue="<%=uFNFilter%>" width="20%">
                     <% 
                        String filterName = fp.getFilterName();
                        out.print(filterName);
                     %> 
                </amb:column>
                <amb:column label="lb_source_file_encoding" sortBy="<%=FileProfileComparator.CODE_NAME%>" width="120px">
                     <% 
                        String codeName = fp.getCodeSet();
                        out.print(codeName);
                     %> 
                </amb:column>
                <amb:column label="lb_file_extensions" sortBy="<%=FileProfileComparator.EXTENSIONS_NAME%>" width="120px">
                     <% 
                     String extensionsName =idViewExtensions.get(fp.getId());
                     extensionsName =extensionsName==null?"all":extensionsName;
                     out.print(extensionsName);
                     %> 
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=FileProfileComparator.ASC_COMPANY%>"  filter="uCompanyFilter" filterValue="<%=uCompanyFilter%>">
                    <%=fp.getCompanyName()%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
         
         </TR>
		    <td>
		      <amb:tableNav  bean="fileprofiles" key="<%=FileProfileConstants.FILEPROFILE_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
		    </td>
		  <TR>
		</DIV>
		<TR><TD>&nbsp;</TD></TR>
		
		<TR>
    <td style="padding-top:5px" align="left">
    <amb:permission name="<%=Permission.FILE_PROFILES_REMOVE%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
            name="removeBtn" id="removeBtn" disabled onClick="submitForm('Remove');">
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
