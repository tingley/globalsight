<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
	    	com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
	    	com.globalsight.everest.webapp.pagehandler.PageHandler,
	    	com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.foundation.LocalePair,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.localemgr.CodeSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.projecthandler.ProjectTM,
            com.globalsight.everest.projecthandler.LeverageProjectTM,
            com.globalsight.everest.util.comparator.TmComparator,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl,
            com.globalsight.everest.util.comparator.SegmentationRuleFileComparator,
            com.globalsight.everest.projecthandler.ProMTInfo,
            java.util.Collections,
            java.util.List,
            java.util.Locale,
            java.util.HashMap,
            java.util.Vector,
            java.util.Iterator,
            java.util.Enumeration,
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

   SessionManager sessionMgr = 
       (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
   TranslationMemoryProfile tmProfile = 
       (TranslationMemoryProfile) sessionMgr.getAttribute("changedTmProfile");
   List dirList = 
       (List) request.getAttribute(TMProfileConstants.MT_LocalPairs_List);
   HashMap dirToTplMap = 
       (HashMap) request.getAttribute(TMProfileConstants.MT_DIRECTION_TOPICTEMPLATE_MAP);
   
   //List of TM Profile values
   String tmprofile_value = tmProfile.getName();
   String mt_engine_value = tmProfile.getMtEngine();
   
   //Urls of the links on this page
   String saveUrl = save.getPageURL();
   String previousUrl = previous.getPageURL() + "&operation=previous";

   String exceptionInfo = (String) request.getAttribute("ExceptionInfo");
   int exLength = 0;
   if (exceptionInfo != null && exceptionInfo.length() > 0) {
	   exLength = exceptionInfo.length();	   
   }
   String onloadFunc = "";
   if (exLength > 0) {
	   onloadFunc = "isPrevious();loadGuides();";
   } else {
	   onloadFunc = "loadGuides();";
   }
   
%>
<HTML>
<HEAD>
	<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<TITLE><%=title %></TITLE>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
	<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
	<%@ include file="/envoy/common/warning.jspIncl" %>
	
<SCRIPT language="JavaScript">
	var needWarning = false;
	var objectName = "ProMT Settings";
	var guideNode = "tmProfiles";
	var helpFile = "<%=bundle.getString("help_tmprofile_create_modify")%>"; 

	function submitForm(formAction)
	{
		PromtOptionsForm.formAction.value = formAction;
		if (formAction == "saveMTOptions") {
			PromtOptionsForm.action = '<%=saveUrl%>';
			PromtOptionsForm.submit();
		} else if (formAction == "previous") {
			PromtOptionsForm.action = '<%=previousUrl%>';
			PromtOptionsForm.submit();
		} else {
			return false;
		}
	}

	function isPrevious() {
        if ('<%=exLength%>' > 0 ) {
        	PromtOptionsForm.action = '<%=previousUrl%>';
			PromtOptionsForm.submit();
        }
	}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="<%=onloadFunc%>">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px;
      POSITION: absolute; WIDTH: 800px; TOP: 108px">

<DIV CLASS="mainHeading" id="idHeading"><%=bundle.getString("lb_tm_options_edit_for") %>&nbsp;"<%=tmprofile_value%>"</DIV>

<FORM NAME="PromtOptionsForm" METHOD="POST" action="">

	<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="" />
	<INPUT TYPE="HIDDEN" NAME="radioBtn" VALUE="<%=tmProfile.getId()%>" />

	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_ENGINE%>" VALUE="<%=tmProfile.getMtEngine()%>"/>
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_OVERRIDE_MATCHES%>" 
			VALUE="<%=tmProfile.getOverrideNonExactMatches()==true?"on":null%>"/>
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_AUTOCOMMIT_TO_TM%>" 
			VALUE="<%=tmProfile.getAutoCommitToTM()==true?"on":null%>"/>
	<INPUT TYPE="HIDDEN" NAME="mtLeveraging" 
			VALUE="<%=tmProfile.getIsMTSensitiveLeveraging()==true?"on":null%>"/>
	<INPUT TYPE="HIDDEN" NAME="mtSensitivePenalty" 
			VALUE="<%=tmProfile.getMtSensitivePenalty()%>"/>			
			
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_SHOW_IN_EDITOR%>" 
			VALUE="<%=tmProfile.getShowInEditor()==true?"on":null%>"/>
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_PTSURL%>" 
			VALUE="<%=tmProfile.getPtsurl()==null?"":tmProfile.getPtsurl()%>"/>
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_PTS_USERNAME%>"
		    VALUE="<%=tmProfile.getPtsUsername()==null?"":tmProfile.getPtsUsername()%>" />
	<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_PTS_PASSWORD%>"
		    VALUE="<%=tmProfile.getPtsPassword()==null?"":tmProfile.getPtsPassword()%>" />
    <INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.MT_PTS_URL_FLAG%>"
		    VALUE="<%=tmProfile.getPtsUrlFlag()==null?"":tmProfile.getPtsUrlFlag()%>" />
	<INPUT TYPE="HIDDEN" NAME="ExceptionInfo"
		    VALUE="<%=exceptionInfo==null?"":exceptionInfo%>" />
		    
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
  <THEAD>
    <COL align="right" valign="top" CLASS="standardText">
    <COL align="left"  valign="top" CLASS="standardText">
  </THEAD>
  <TR>
    <TD align="left"><%=bundle.getString("lb_tm_mt_engine") %>: </TD>
    <TD><%=mt_engine_value==null?"":mt_engine_value%></TD>
  </TR>
  <tr>
     <td ALIGN="LEFT"><%=bundle.getString("lb_tm_ptsurl") %>: </td>
     <td><%=tmProfile.getPtsurl()==null?"":tmProfile.getPtsurl() %></td>
  </tr>
  <tr><td colspan="2">&nbsp;</td></tr>

  <tr>
     <td align="left"><b><%=bundle.getString("lb_tm_locale_pair_name") %></b></td>
     <td><b><%=bundle.getString("lb_tm_topic_template") %></b>(<%=bundle.getString("lb_tm_topic_template_comments") %>)</td>
  </tr>
      <%
      Vector promtInfoVector = tmProfile.getPromtInfos();

      if (dirList != null && dirList.size()>0 )
      {
    	  for (int i=0; i<dirList.size(); i++)
    	  {
        	  String dirName = (String) dirList.get(i);
           	  String topicTemplateId = null;
              if (promtInfoVector != null && promtInfoVector.size()>0 ) 
              {
                  Iterator promtIt = promtInfoVector.iterator();
                  while (promtIt != null && promtIt.hasNext()) 
                  {
                      ProMTInfo promtInfo = (ProMTInfo) promtIt.next();
                      if (promtInfo.getDirName().equals(dirName)) 
                      {
                    	  topicTemplateId = promtInfo.getTopicTemplateId();
                   	  }
                  }
              }
      %>
      <tr>
        <td align="left"><%=dirName%></td>
        <td>
          <select name="<%=dirName%>">
          <% List dir2tplidList = (List) dirToTplMap.get(dirName);
             if (dir2tplidList != null && dir2tplidList.size() > 0) 
             {
                 Iterator dir2tplidIt = dir2tplidList.iterator();
                 while (dir2tplidIt.hasNext())
                 {
                	 String tplid = (String) dir2tplidIt.next();
                	 if (topicTemplateId != null) {
		  %>
			             <option value="<%=tplid%>" <%=topicTemplateId.equals(tplid)?"selected":""%>><%=tplid%></option>
		  <%       	 } else { %>
			             <option value="<%=tplid%>" <%="General".equalsIgnoreCase(tplid)?"selected":""%>><%=tplid%></option>
		  <%
		  			 }
                 }
             }
          %>

          </select>
        </td>
      </tr>
<% } } %>
</TABLE>

</FORM>

	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous") %>" ID="previous" onclick="submitForm('previous');">
<%  if (exceptionInfo != null && !"".equals(exceptionInfo)) { %>
	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save") %>" ID="OK" onclick="submitForm('saveMTOptions');" disabled>
<%  } else { %>
	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save") %>" ID="OK" onclick="submitForm('saveMTOptions');">
<%  } %>
</DIV>

</BODY>
</HTML>
