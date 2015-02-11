<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
    		com.globalsight.everest.webapp.javabean.NavigationBean,
    		com.globalsight.machineTranslation.asiaOnline.AsiaOnlineMtInvoker,
    		com.globalsight.everest.webapp.pagehandler.PageHandler,
    		com.globalsight.everest.servlet.util.SessionManager,
    		com.globalsight.everest.webapp.WebAppConstants,
    		com.globalsight.everest.projecthandler.TranslationMemoryProfile,
    		com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
    		com.globalsight.machineTranslation.asiaOnline.DomainCombination,
    		com.globalsight.everest.projecthandler.AsiaOnlineLP2DomainInfo,
    		java.util.HashMap,
    		java.util.Iterator,
    		java.util.ResourceBundle"
    session="true" %>
<%@ include file="/envoy/common/header.jspIncl" %>

<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<%
     ResourceBundle bundle = PageHandler.getBundle(session);
     String title = bundle.getString("lb_tm_options_edit");

     SessionManager sessionMgr = (SessionManager) session
             .getAttribute(WebAppConstants.SESSION_MANAGER);
     TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr
             .getAttribute("changedTmProfile");
     if (tmProfile == null)
     {
         tmProfile = (TranslationMemoryProfile) 
                 sessionMgr.getAttribute(TMProfileConstants.TM_PROFILE);
     }
     Set aoInfoSet = tmProfile.getTmProfileAoInfoSet();
     HashMap aoSupportedLocalePairs = (HashMap) request
             .getAttribute("aoSupportedLocalePairs");
     HashMap domainCombinations = (HashMap) request
             .getAttribute("domainCombinationMap");

     //Urls of the links on this page
     String saveUrl = save.getPageURL();
     String previousUrl = previous.getPageURL() + "&operation=previous";
     
     String exceptionInfo = (String) request.getAttribute("ExceptionInfo");
     int exLength = 0;
     if (exceptionInfo != null && exceptionInfo.length() > 0) {
  	   exLength = exceptionInfo.length();	   
     }
     String onloadFunc = "";
     if (exLength > 0) 
     {
  	   onloadFunc = "isPrevious();loadGuides();";
     }
     else 
     {
  	   onloadFunc = "loadGuides();";
     }

 %>

<HTML>
<HEAD>
	<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<TITLE><%=title%></TITLE>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
	<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
	<%@ include file="/envoy/common/warning.jspIncl" %>
	
<SCRIPT language="JavaScript">
	var needWarning = false;
	var objectName = "Asia Online Settings";
	var guideNode = "tmProfiles";
	var helpFile = "<%=bundle.getString("help_tmprofile_asia_online_domain_modify")%>"; 

	function submitForm(formAction)
	{
		aoMtOptionsForm.formAction.value = formAction;
		if (formAction == "saveMTOptions") {
			aoMtOptionsForm.action = '<%=saveUrl%>';
			aoMtOptionsForm.submit();
		} else if (formAction == "previous") {
			aoMtOptionsForm.action = '<%=previousUrl%>';
			aoMtOptionsForm.submit();
		} else {
			return false;
		}
	}

	function isPrevious() 
	{
        if ('<%=exLength%>' > 0 ) 
        {
        	aoMtOptionsForm.action = '<%=previousUrl%>';
        	aoMtOptionsForm.submit();
        }
	}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="<%=onloadFunc%>">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px;
      POSITION: absolute; WIDTH: 800px; TOP: 108px">

<DIV CLASS="mainHeading" id="idHeading">
    <%=bundle.getString("lb_tm_ao_mt_options")%>&nbsp;"<%=tmProfile.getName()%>"</DIV>

<FORM NAME="aoMtOptionsForm" METHOD="POST" action="">
    <input type="hidden" name="formAction" value=""/>
    <input type="hidden" name="<%=TMProfileConstants.MT_ENGINE%>" value="<%=tmProfile.getMtEngine()%>"/>
    <input TYPE="HIDDEN" name="ExceptionInfo" value="<%=exceptionInfo==null?"":exceptionInfo%>" />
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
    <THEAD>
        <COL align="right" valign="top" CLASS="standardText">
        <COL align="left"  valign="top" CLASS="standardText">
    </THEAD>
    <TR>
        <TD align="left"><%=bundle.getString("lb_tm_mt_engine")%>: </TD>
        <TD><%=tmProfile.getMtEngine() == null ? "" : tmProfile.getMtEngine()%></TD>
    </TR>
    <tr>
        <td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_url")%>: </td>
        <td><%=tmProfile.getAoMtUrl() == null ? "" : tmProfile.getAoMtUrl()%></td>
    </tr>
    <tr><td colspan="2">&nbsp;</td></tr>

    <tr>
        <td align="left"><b><%=bundle.getString("lb_tm_ao_mt_locale_pair_name")%></b></td>
        <td><b><%=bundle.getString("lb_tm_ao_mt_domain_combination")%></b></td>
    </tr>
<%
    if (domainCombinations != null && domainCombinations.size() > 0)
    {
        Iterator dcIter = domainCombinations.entrySet().iterator();
        while (dcIter.hasNext())
        {
            Map.Entry entry = (Map.Entry) dcIter.next();
            String lpCode = (String) entry.getKey();
            // Find the domain combination code if it has been setup.
            String dcCodeAlreadySet = null;
            if (aoInfoSet != null && aoInfoSet.size() > 0)
            {
                Iterator aoInfoItr = aoInfoSet.iterator();
                while (aoInfoItr.hasNext())
                {
                    AsiaOnlineLP2DomainInfo aoLP2DC = 
                        (AsiaOnlineLP2DomainInfo) aoInfoItr.next();
                    if (String.valueOf(aoLP2DC.getLanguagePairCode()).equals(lpCode))
                    {
                        dcCodeAlreadySet = 
                            String.valueOf(aoLP2DC.getDomainCombinationCode());
                    }
                }
            }
            
            List val = (List) entry.getValue();
            DomainCombination firstDC = (DomainCombination) val.get(0);
            String lpName = firstDC.getSourceLanguage() + "-"
                    + firstDC.getTargetLanguage();
%>
    <tr>
        <td align="left"><%=lpName%></td>
        <td>
            <select name="<%=lpCode%>">
<%
            for (int i=0; i<val.size(); i++)
            {
                DomainCombination dc = (DomainCombination) val.get(i);
                String dcName = dc.getDomainCombinationName();
                String dcCode = dc.getCode();
                if (dcCodeAlreadySet != null && dcCodeAlreadySet.equals(dcCode))
                {
%>
                <option value="<%=dcCode%>" selected><%=dcName%></option>
<%
                }
                else
                {
%>
                <option value="<%=dcCode%>"><%=dcName%></option>
<%                  
                }
            }
%>
            </select>
        </td>
    </tr>
<%
        }
    }
%>

</TABLE>

</FORM>

	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous")%>" ID="previous" onclick="submitForm('previous');">
	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="OK" onclick="submitForm('saveMTOptions');">

</DIV>

</BODY>
</HTML>
