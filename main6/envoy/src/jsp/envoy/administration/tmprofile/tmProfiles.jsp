<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.util.comparator.TMProfileComparator,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.util.GeneralException,
            java.text.MessageFormat,
            java.util.Date,
            java.util.Vector,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.foundation.BasicL10nProfile"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="mt_edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="tda_edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="tmProfiles" class="java.util.ArrayList" scope="request"/>

<%@ include file="/envoy/common/header.jspIncl" %>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String title= bundle.getString("lb_tm_profiles");
    String helperText = bundle.getString("helper_text_tm_profile_main");
    //Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String mtEdit = bundle.getString("lb_mt_edit");
    //Urls of the links on this page
    String action = TMProfileConstants.ACTION;
    String selfUrl = self.getPageURL();
    String newUrl = new1.getPageURL() + "&" + action + "=" + TMProfileConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL()+ "&" + action + "=" + TMProfileConstants.EDIT_ACTION;
    String removeUrl = remove.getPageURL() + "&" + action + "=" + TMProfileConstants.REMOVE_ACTION;
    String mtEditUrl = mt_edit.getPageURL() + "&" + action + "=" + TMProfileConstants.MT_EDIT_ACTION;
    String tdaEditUrl = tda_edit.getPageURL() + "&" + action + "=" + TMProfileConstants.MT_EDIT_ACTION;

    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    Collection l10nProfiles = null;
    try {
    	l10nProfiles = ServerProxy.getProjectHandler().getAllL10nProfiles();
    } catch (Exception ex) {}

    
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_tmProfile") %>";
var guideNode = "tmProfiles";
var helpFile = "<%=bundle.getString("help_tmprofile")%>";

    function enableButtons()
    {
        if (TMProfileForm.editBtn) {
            TMProfileForm.editBtn.disabled = false;
        }
        if (TMProfileForm.removeBtn) {
			TMProfileForm.removeBtn.disabled = false;
        }
        if (TMProfileForm.mtEditBtn) {
			      TMProfileForm.mtEditBtn.disabled = false;
        }
        
        if (TMProfileForm.tdaEditBtn) {
			      TMProfileForm.tdaEditBtn.disabled = false;
        }
    }

    function submitForm(selectedButton)
    {
       var checked = false;
       var selectedRadioBtn = null;
       if (TMProfileForm.radioBtn != null)
       {
          // If more than one radio button is displayed, the length attribute of the
          // radio button array will be non-zero, so find which
          // one is checked
          if (TMProfileForm.radioBtn.length)
          {
              for (i = 0; !checked && i < TMProfileForm.radioBtn.length; i++)
              {
                  if (TMProfileForm.radioBtn[i].checked == true)
                  {
                      checked = true;
                      selectedRadioBtn = TMProfileForm.radioBtn[i].value;
                  }
              }
          }
          // If only one is displayed, there is no radio button array, so
          // just check if the single radio button is checked
          else
          {
              if (TMProfileForm.radioBtn.checked == true)
              {
                  checked = true;
                  selectedRadioBtn = TMProfileForm.radioBtn.value;
              }
          }
       }

       // otherwise do the following
       if (selectedButton != 'New' && !checked)
       {
           alert("<%= bundle.getString("jsmsg_tm_profile_select") %>");
           return false;
       }
       else
       {
          if (selectedButton=='Edit')
          {
             TMProfileForm.action = "<%=modifyUrl%>";
             TMProfileForm.submit();
          }
          else if ( selectedButton == 'New' )
          {
             TMProfileForm.action = "<%=newUrl%>";
             TMProfileForm.submit();
          }
          else if ( selectedButton == 'Remove' )
          {
             var rtnMsg = ifCanBeRemoved(selectedRadioBtn);
             if ( rtnMsg == "" ) {
    			 TMProfileForm.action = "<%=removeUrl%>";
    	         TMProfileForm.submit();
             } else {
				alert(rtnMsg);
				return false;
             }
          }
          else if ( selectedButton == 'MTEdit' )
          {
			        TMProfileForm.action = "<%=mtEditUrl%>";
			        TMProfileForm.submit();
          }
          else if(selectedButton == 'TDAEdit') {
              TMProfileForm.action = "<%=tdaEditUrl%>";
			        TMProfileForm.submit();
			    }
       }
    }

    function ifCanBeRemoved(selectedRadioBtn)
    {
        var rtnMsg = "";
		<%
		if ( l10nProfiles != null && l10nProfiles.size() > 0 ) 
        {
            for (Iterator iter = l10nProfiles.iterator(); iter.hasNext();)
            {
            	BasicL10nProfile l10nProfile = (BasicL10nProfile)iter.next(); 
            	TranslationMemoryProfile tmProfile = l10nProfile.getTranslationMemoryProfile();
            	long tmProfileId = -1;
            	if (tmProfile != null) {
            		tmProfileId = tmProfile.getId();
            	}
            	%>
            	if ( '<%=tmProfileId%>' == selectedRadioBtn ) 
            	{
					if ( rtnMsg == "" ) {
						rtnMsg = "<%=bundle.getString("msg_tm_remove_tmp_lp") %>";
						rtnMsg = rtnMsg + "\n   " + '<%=l10nProfile.getName()%>';
					} else {
						rtnMsg = rtnMsg + "\n   " + '<%=l10nProfile.getName()%>';
					}
            	}
       	<%
            }
        }
        %>

        return rtnMsg;
    }
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<FORM NAME="TMProfileForm" METHOD="POST">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
      <tr valign="top">
        <td align="right">
            <amb:tableNav bean="tmProfiles" key="<%=TMProfileConstants.TMP_KEY%>"
                 pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="tmProfiles" id="tmProfile" key="<%=TMProfileConstants.TMP_KEY%>"
                 dataClass="com.globalsight.everest.projecthandler.TranslationMemoryProfile"
                 pageUrl="self"
                 emptyTableMsg="msg_no_tm_profiles" >
            <amb:column label="" width="20px">
                <input type="radio" name="radioBtn" value="<%=tmProfile.getId()%>"
                    onclick="enableButtons()">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=TMProfileComparator.NAME%>">
                <%=tmProfile.getName()%>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=TMProfileComparator.DESCRIPTION%>"
                width="200px">
                <% out.print(tmProfile.getDescription() == null ? "" : tmProfile.getDescription()); %>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" width="200px" sortBy="<%=TMProfileComparator.ASC_COMPANY%>">
                <%
                    String companyId = ServerProxy.getProjectHandler().getProjectTMById(tmProfile.getProjectTmIdForSave(), false).getCompanyId();
                    String companyName = ServerProxy.getJobHandler().getCompanyById(Long.parseLong(companyId)).getCompanyName();
                    out.print(companyName);
                %>
            </amb:column>
            <% } %>
          </amb:table>
        </td>
    </tr>
</div>
<tr><td>&nbsp;</td></tr>

<TR>
<TD>
<DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: visible">
    <P>
<amb:permission name="<%=Permission.TMP_EDIT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');"
        name="editBtn" disabled>
</amb:permission>
<amb:permission name="<%=Permission.TMP_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
</amb:permission>
<amb:permission name="<%=Permission.TMP_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>..." onClick="submitForm('Remove');"
        name="removeBtn" disabled>
</amb:permission>
<amb:permission name="<%=Permission.TMP_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=mtEdit%>..." onClick="submitForm('MTEdit');"
        name="mtEditBtn" disabled>
</amb:permission>
<amb:permission name="<%=Permission.TMP_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_tda_edit")%>..." onClick="submitForm('TDAEdit');"
        name="tdaEditBtn" disabled>
</amb:permission>
</DIV>
</TD>
</TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
