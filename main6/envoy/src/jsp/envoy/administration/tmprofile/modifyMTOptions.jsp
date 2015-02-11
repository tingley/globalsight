<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
    		com.globalsight.everest.webapp.javabean.NavigationBean,
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

<jsp:useBean id="saveMTOptions" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelMTOptions" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextAsiaOnline" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String lbNext = bundle.getString("lb_next");
    String lbSave = bundle.getString("lb_save");
    String title = bundle.getString("lb_tm_options_edit");

    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    Long tmProfileId = (Long) sessionMgr
            .getAttribute(TMProfileConstants.TM_PROFILE_ID);
    TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr
            .getAttribute(TMProfileConstants.TM_PROFILE);
    TranslationMemoryProfile changedTmProfile = (TranslationMemoryProfile) sessionMgr
            .getAttribute("changedTmProfile");
    //If return back from promt next page, refer to the changedTmProfile.
    if (changedTmProfile != null)
    {
        tmProfile = changedTmProfile;
    }

    //List of TM Profile values
    String current_engine = tmProfile.getMtEngine();
    boolean current_override_non_exact_matches = tmProfile
            .getOverrideNonExactMatches();
    boolean current_auto_commit_to_tm = tmProfile.getAutoCommitToTM();
    boolean current_show_in_editor = tmProfile.getShowInEditor();

    //Urls of the links on this page
    String saveMTOptionsUrl = saveMTOptions.getPageURL();
    String cancelMTOptionsUrl = cancelMTOptions.getPageURL();
    String nextUrlForPromt = next.getPageURL();
    String nextUrlForAsiaOnline = nextAsiaOnline.getPageURL();
    String testUrl = self.getPageURL() + "&operation=testMSHost";
    String testAOUrl = self.getPageURL() + "&operation=testAOHost";

    // For "MS_Translator" and "Asia_Online",need check if connection is available.
    String operation = (String) request.getAttribute("operation");
    String exceptionInfo = (String) request.getAttribute("ExceptionInfo");
    String isCanSave = "true";
    if (operation != null && operation.startsWith("test")
            && exceptionInfo != null)
    {
        isCanSave = "false";
    }
    String url_flag = request.getAttribute("URL_flag") == null ? tmProfile
            .getMsMTUrlFlag() : (String) request
            .getAttribute("URL_flag");
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
	var needWarning = true;
	var objectName = "MT Options";
	var guideNode = "tmProfiles";
	var helpFile = "<%=bundle.getString("help_tmprofile_mt_options")%>"; 

	function submitForm(formAction)
	{
		MTOptionsForm.formAction.value = formAction;
		if (formAction == "cancelMTOptions") 
		{
			if (confirmJump())
			{
				MTOptionsForm.action = '<%=cancelMTOptionsUrl%>';
				MTOptionsForm.submit();
			}
			else 
			{
				return false;
			}
		}
		else if (formAction == "saveMTOptions") 
		{
			var userMT = document.getElementById("override_non_exact");
			var va = document.getElementById("auto_commit_to_tm");
			var mt = document.getElementById("mtLeveraging");
			var mtPenalty = document.getElementById('mtSensitivePenalty').value;

			if (mtPenalty == null || trim(mtPenalty)== "" ) 
			{
				alert("<%=bundle.getString("msg_tm_mt_penalty_null")%>");
			}
			else if (!isAllDigits(mtPenalty)) 
			{
				alert("<%=bundle.getString("msg_tm_mt_penalty_invalid")%>");
				return false;
			}
			else if (mt.checked && !checkIsVaildPercent(mtPenalty)) 
			{
			   return false;
			}
			else if (userMT.checked && !va.checked && !mt.checked) 
			{
				alert("<%=bundle.getString("msg_tm_mt_select_notice")%>");
			}
			else 
			{
				var engine_name = document.getElementById('mtEngine').value;
				if (engine_name != null && engine_name.toLowerCase() == "promt") 
				{
					var ptsurl = document.getElementById('ptsurl').value;
					var ptsusername = document.getElementById('username').value;
					var ptspassword = document.getElementById('password').value;

					MTOptionsForm.action = '<%=nextUrlForPromt%>';
					if (ptsurl != null && trim(ptsurl) != "")
					{
						var httpIndex = ptsurl.indexOf('http');
						if (httpIndex == 0) 
						{
							MTOptionsForm.submit();
						}
						else 
						{
							alert("<%=bundle.getString("msg_tm_mt_url_format")%>");
							return false;
						}
					}
					else 
					{
						alert("<%=bundle.getString("msg_tm_mt_url_empty")%>");
						return false;
					}
				}
				else if (engine_name != null && engine_name.toLowerCase() == "ms_translator") 
				{
				    var msMTCanSave = document.getElementById('idCanSave').value;
					var msTranslatorUrl = document.getElementById('idMsMtUrl').value;
					var msTranslatorAppId = document.getElementById('idMsMtAppid').value;
					if (msTranslatorUrl == null || trim(msTranslatorUrl) == "" ) 
					{
						alert("<%=bundle.getString("msg_tm_mt_url_empty")%>");
						return false;
				    }
				    else if (msMTCanSave != null && trim(msMTCanSave) == 'false') 
					{
				        alert("<%=bundle.getString("lb_tm_mt_ms_engine_cannot_save")%>");
				        return false;
					}
					else 
					{
						MTOptionsForm.action = '<%=saveMTOptionsUrl%>';
						MTOptionsForm.submit();
					}
				}
				else if (engine_name != null && engine_name.toLowerCase() == "asia_online")
				{
					var canSave = document.getElementById('idCanSave').value;
					var canGoOn = checkAoOptions();
					if (!canGoOn)
					{
                        return false;
					}
					else
					{
						MTOptionsForm.action = '<%=nextUrlForAsiaOnline%>';
						MTOptionsForm.submit();
					}
				} 
				else 
				{
					MTOptionsForm.action = '<%=saveMTOptionsUrl%>';
					MTOptionsForm.submit();
				}
			}
		}
		else if (formAction == "testMSMT")
		{
			var engine_name = document.getElementById('mtEngine').value;
			if (engine_name != null && engine_name.toLowerCase() == "ms_translator") 
			{
				var msTranslatorUrl = document.getElementById('idMsMtUrl').value;
				if (msTranslatorUrl == null || trim(msTranslatorUrl) == "" ) 
				{
					alert("<%=bundle.getString("msg_tm_mt_url_empty")%>");
					return false;
				}
				else 
				{
					MTOptionsForm.action = '<%=testUrl%>';
					MTOptionsForm.submit();
				}
			}
		}
		else if (formAction == "testAOHost")
		{
			var engine_name = document.getElementById('mtEngine').value;
			if (engine_name != null && engine_name.toLowerCase() == "asia_online")
			{
				var canGoOn = checkAoOptions();
				if (canGoOn == true)
				{
                	MTOptionsForm.action = '<%=testAOUrl%>';
					MTOptionsForm.submit();
                }
			}
		}
		else 
		{
			return false;
		}
	}

	function checkIsVaildPercent(percent){
	    var submit = false;
		var i_percent = parseInt(percent);
		if(i_percent > 100 || i_percent < 1){
			alert("<%=bundle.getString("msg_tm_leverage_number_1")%>");
			submit = false;
		}else{
			submit = true;
		}	
		return submit;
	}
	
	function changeStatus(Obj)
	{
		var va = document.getElementById("auto_commit_to_tm");
		var mt = document.getElementById("mtLeveraging");
		if (Obj.checked) {
            va.disabled = false;
            mt.disabled = false;
		} else {
		    va.disabled = true;
		    mt.disabled = true;
		    
		    va.checked = false;
		    mt.checked = false;
		}
	}

	function changeRadioStatus(Obj)
	{
		var va = document.getElementById("auto_commit_to_tm");
		var mt = document.getElementById("mtLeveraging");

		va.checked = false;
		mt.checked = false;

		Obj.checked = true;
	}

	function contorlMTOptionShowing()
	{
        //1.Control MT DIVs display
        var ptsDiv = document.getElementById("ptsDiv");
        var msMtDiv = document.getElementById("msMtDiv");
        var aoMtDiv = document.getElementById("aoMtDiv");
        
        var engineSelect = document.getElementById("mtEngine");
        var selectedEngineName = engineSelect.options[engineSelect.selectedIndex].value;
        // hide them all first
        ptsDiv.style.display='none';
        msMtDiv.style.display='none';
        aoMtDiv.style.display='none';
        // display corresponding div by selected engine name.
	    if (selectedEngineName.toLowerCase() == "google") 
		{
            //do nothing
	    }
	    else if (selectedEngineName.toLowerCase() == "promt") 
		{
            ptsDiv.style.display='block';
	    }
	    else if (selectedEngineName.toLowerCase() == "ms_translator") 
		{
            msMtDiv.style.display='block';
	    }
	    else if(selectedEngineName.toLowerCase() == "asia_online")
	    {
            aoMtDiv.style.display='block';
	    }

	    //2.Display "Save" button or "Next" button
        var okBtn = document.getElementById("OK");	    
	    if (selectedEngineName.toLowerCase() == "google"
		    || selectedEngineName.toLowerCase() == "ms_translator")
	    {
	    	okBtn.value = "<%=lbSave%>";
	    }
	    else
	    {
	    	okBtn.value = "<%=lbNext%>";
	    }

	    //3.When engine is MS tranlator,set it as "false".
        var msMTCanSave = document.getElementById('idCanSave');
        msMTCanSave.value = 'true';
        if (selectedEngineName.toLowerCase() == "ms_translator"
            || selectedEngineName.toLowerCase() == "asia_online" ) 
		{
            msMTCanSave.value = 'false';
		}
	}

	function checkAoOptions()
	{
        var aoMtUrl = document.getElementById('idAoMtUrl').value;
        var aoMtPort = document.getElementById('idAoMtPort').value;
        var aoMtUserName = document.getElementById('idAoMtUsername').value;
        var aoMtPassword = document.getElementById('idAoMtPassword').value;
        var aoMtAccountNumber = document.getElementById('idAoMtAccountNumber').value;

        //var jobNameRegex = /[\\/:;\*\?\|\"\'<>&%]/;
   	    //if (jobNameRegex.test(customerForm.jobName.value))

        if (aoMtUrl == null || trim(aoMtUrl) == "")
        {
            alert("<%=bundle.getString("lb_tm_ao_mt_url_empty")%>");
            return false;
        }
        else if (aoMtPort == null || trim(aoMtPort) == "" || !isAllDigits(trim(aoMtPort)))
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_port_empty_invalid")%>");
            return false;
        }
        else if (aoMtUserName == null || trim(aoMtUserName) == "")
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_username_empty")%>");
            return false;
        }
        else if (aoMtPassword == null || trim(aoMtPassword) == "")
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_password_empty")%>");
            return false;
        }
        else if (aoMtAccountNumber == null || trim(aoMtAccountNumber) == "" 
            || !isAllDigits(trim(aoMtAccountNumber)))
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_account_number_empty_invalid")%>");            
            return false;
        }

        return true;
	}
	
	//remove all whitespace on left and right
	function trim(str)
	{
	     return str.replace(/(^\s*)|(\s*$)/g, '');
	}

	function ltrim(str)
	{
	     return str.replace(/(^\s*)/g,'');
	}

	function rtrim(str)
	{
	     return str.replace(/(\s*$)/g,'');
	}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute; WIDTH: 800px; TOP: 108px">

<DIV CLASS="mainHeading" id="idHeading"><%=title%><%=tmProfile.getName() == null ? "" : (" : " + tmProfile
                            .getName())%></DIV>

<FORM NAME="MTOptionsForm" METHOD="POST" action="">
<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
<INPUT TYPE="HIDDEN" NAME="radioBtn" VALUE="<%=tmProfile.getId()%>"/>
<INPUT TYPE="HIDDEN" NAME="canSave" id="idCanSave" VALUE="<%=isCanSave%>"/>
<INPUT TYPE="HIDDEN" NAME="ms_mt_url_flag" id="idURL_flag" VALUE="<%=url_flag%>"/>

	<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
	  <THEAD>
	    <COL align="right" valign="top" CLASS="standardText">
	    <COL align="left"  valign="top" CLASS="standardText">
	  </THEAD>
	  
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_tm_mt_engine")%>: </TD>
	    <TD>
	      <SELECT CLASS="standardText" ID="mtEngine" NAME="<%=TMProfileConstants.MT_ENGINE%>"
			onchange="contorlMTOptionShowing(this.value)">
			<%
			    String[] engines =
			    { "Google", "ProMT", "MS_Translator", "Asia_Online" };
			    for (int i = 0; i < engines.length; i++)
			    {
			        String _engine = engines[i];
			        String isSelected = "";
			        if (_engine.equalsIgnoreCase(current_engine))
			        {
			            isSelected = " selected";
			        }
			%>
			<OPTION VALUE="<%=_engine%>" <%=isSelected%>><%=_engine%></OPTION>
			<%
			    }
			%>
		</SELECT>
	    </TD>
	  </TR>
	  
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_tm_override_non_exact")%>:</TD>
	    <TD>
	    	<%
	    	    String isChecked = "";
	    	    if (current_override_non_exact_matches)
	    	    {
	    	        isChecked = "CHECKED";
	    	    }
	    	%>
	    	<INPUT CLASS="standardText" ID="override_non_exact" NAME="<%=TMProfileConstants.MT_OVERRIDE_MATCHES%>" 
	    	    TYPE="checkbox" <%=isChecked%> onclick="changeStatus(this);" />
	    </TD>
	  </TR>
	  
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_tm_auto_commit_to_tm")%>:</TD>
	    <TD>
	    	<%
	    	    String autoCommitRadioChecked = "";
	    	    if (current_auto_commit_to_tm)
	    	    {
	    	        autoCommitRadioChecked = "CHECKED";
	    	    }
	    	    if (current_override_non_exact_matches == false)
	    	    {
	    	        autoCommitRadioChecked += " disabled";
	    	    }
	    	%>
	    	<INPUT CLASS="standardText" ID="auto_commit_to_tm" NAME="<%=TMProfileConstants.MT_AUTOCOMMIT_TO_TM%>" 
	    	    TYPE="radio" <%=autoCommitRadioChecked%> onclick="changeRadioStatus(this);"/>
	    </TD>
	  </TR>
	  
      <TR>
        <TD ALIGN="LEFT" STYLE="vertical-align: middle"><%=bundle.getString("lb_tm_mt_sensitive_leveraging")%>:</TD>
        <TD>
            <%
                String mtSensitiveRadioChecked = "";
                if (tmProfile.getIsMTSensitiveLeveraging())
                {
                    mtSensitiveRadioChecked = "CHECKED";
                }
                if (current_override_non_exact_matches == false)
                {
                    mtSensitiveRadioChecked += " disabled";
                }
            %>
            <INPUT TYPE="radio" ID="mtLeveraging" NAME="mtLeveraging" <%=mtSensitiveRadioChecked%> onclick="changeRadioStatus(this);"/>
            <%=bundle.getString("msg_penalty")%>: <INPUT ID="mtSensitivePenalty" NAME="mtSensitivePenalty" SIZE="1" MAXLENGTH="3" VALUE="<%=tmProfile.getMtSensitivePenalty()%>">
        </TD>
      </TR>
      
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_show_in_editor")%>:</TD>
	    <TD>
	    	<%
	    	    if (current_show_in_editor)
	    	    {
	    	%>
	    	<INPUT CLASS="standardText" NAME="<%=TMProfileConstants.MT_SHOW_IN_EDITOR%>" TYPE="checkbox" CHECKED/>
	    	<%
	    	    }
	    	    else
	    	    {
	    	%>
	    	<INPUT CLASS="standardText" NAME="<%=TMProfileConstants.MT_SHOW_IN_EDITOR%>" TYPE="checkbox" />
	    	<%
	    	    }
	    	%>
	    </TD>
	  </TR>

	</TABLE>

    <!-- **************** Promt MT Options : Start ************************* -->
	<%
	    if ("ProMT".equalsIgnoreCase(current_engine))
	    {
	%>
		<div id="ptsDiv" style="display:block;">
	<%
	    }
	    else
	    {
	%>
		<div id="ptsDiv" style="display:none;">
	<%
	    }
	%>
		<p>
		<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="88%">
	      <tr>
	        <td colspan="2"><b><%=bundle.getString("lb_tm_pts_title")%>: http://&lt;server_name:port&gt;/pts8/services/ptservice.asmx(?wsdl) </b></td>
	      </tr>
	      <tr>
	        <td ALIGN="LEFT"><%=bundle.getString("lb_tm_pts_url")%><font color="red">*</font>: </td>
	        <td>
	        	<INPUT CLASS="standardText" ID="ptsurl" NAME="<%=TMProfileConstants.MT_PTSURL%>" 
	        	    value="<%=tmProfile.getPtsurl() == null ? "" : tmProfile
                    .getPtsurl()%>" TYPE="text" MAXLENGTH="99" SIZE="99" />
	        </td>
	      </tr>
	      <tr>
	        <td ALIGN="LEFT"><%=bundle.getString("lb_user_name")%>: </td>
	        <td>
	        	<INPUT CLASS="standardText" ID="username" NAME="<%=TMProfileConstants.MT_PTS_USERNAME%>" 
	        	    value="<%=tmProfile.getPtsUsername() == null ? "" : tmProfile
                    .getPtsUsername()%>" TYPE="text" MAXLENGTH="99" SIZE="20" />
	        	&nbsp;(<%=bundle.getString("msg_tm_mt_anonymous_username")%>)
	        </td>
	      </tr>
	      <tr>	
	        <td ALIGN="LEFT"><%=bundle.getString("lb_password")%>: </td>
	        <td>
	        	<INPUT CLASS="standardText" ID="password" NAME="<%=TMProfileConstants.MT_PTS_PASSWORD%>" 
	        	    value="<%=tmProfile.getPtsPassword() == null ? "" : tmProfile
                    .getPtsPassword()%>" TYPE="password" MAXLENGTH="99" SIZE="20" />
	        	&nbsp;(<%=bundle.getString("msg_tm_mt_anonymous_pwd")%>)
	      </td>
	    </tr>

        <%
            if (exceptionInfo != null && !"".equals(exceptionInfo.trim())
                    && operation != null && "previous".equals(operation))
            {
        %>
        <tr><td colspan="2">&nbsp;</td></tr>
	    <tr><td align="left" colspan="2"><font color="red"><%=bundle.getString("msg_tm_mt_exception_title")%> :&nbsp;<b><%=exceptionInfo%></b></font></td></tr>
		<%
		    if (exceptionInfo.indexOf("404") != -1)
		        {
		%>
		<tr><td align="left" colspan="2"><%=bundle.getString("msg_tm_mt_exception_404")%></td></tr>
		<%
		    }
		        if (exceptionInfo.indexOf("401") != -1)
		        {
		%>
        <tr><td align="left" colspan="2"><%=bundle.getString("msg_tm_mt_exception_401")%></td></tr>
        <%
            }
            }
        %>

	  </TABLE></p></div>
    <!-- **************** Promt MT Options : End *************************** -->

    <!-- **************** MS Translator MT Options : Start ***************** -->
 	 <%
 	     if ("MS_Translator".equalsIgnoreCase(current_engine))
 	     {
 	 %>
	    <div id="msMtDiv" style="display:block;">
 	 <%
 	     }
 	     else
 	     {
 	 %>
	    <div id="msMtDiv" style="display:none;">
	 <%
	     }
	 %>
	 <p>
	 <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="88%">
	      <tr><td colspan="3"><b><%=bundle.getString("lb_tm_ms_mt_title")%></b></td></tr>
	      <tr>
	        <td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_url")%><font color="red">*</font>: </td>
	        <td>
	        	<INPUT CLASS="standardText" ID="idMsMtUrl" NAME="<%=TMProfileConstants.MT_MS_URL%>" 
	        			value="<%=tmProfile.getMsMTUrl() == null ? "http://api.microsofttranslator.com/V2/Soap.svc" : tmProfile
                            .getMsMTUrl()%>" TYPE="text" MAXLENGTH="99" SIZE="90" />
	        </td>
			<td ALIGN="LEFT"><INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_test_host")%>" ID="test" onclick="submitForm('testMSMT');"/></td>
	      </tr>
	      <tr>
	      	<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_appid")%>: </td>
	      	<td>
	      		<INPUT CLASS="standardText" ID="idMsMtAppid" NAME="<%=TMProfileConstants.MT_MS_APPID%>" 
	        			value="<%=tmProfile.getMsMTAppID() == null ? "" : tmProfile
                    .getMsMTAppID()%>" TYPE="text" MAXLENGTH="50" SIZE="60" />
	        			<a href="http://www.bing.com/developers/appids.aspx" target="_blank"><%=bundle.getString("lb_tm_ms_mt_appid_tag")%></a>
	      	</td>
	      </tr>
	      <%
	          if (operation != null && "testMSHost".equals(operation))
	          {
	              if (exceptionInfo != null && !"".equals(exceptionInfo.trim()))
	              {
	      %>
                <tr><td colspan="3">&nbsp;</td></tr>
    	        <tr><td align="left" colspan="2"><font color="red">
    	            <%=bundle.getString("lb_tm_mt_ms_engine_error")%> :&nbsp;<b><%=exceptionInfo%></b></font></td></tr>
          <%
              }
                  else
                  {
          %>
                <tr><td colspan="3">&nbsp;</td></tr>
                <tr><td align="left" colspan="3"><font color="green"><b><%=bundle.getString("lb_tm_mt_engine_work_well")%></b></font></td></tr>
          <%
              }
              }
          %>
	</TABLE>
	</p></div>
    <!-- **************** MS Translator MT Options : End ******************* -->

    <!-- **************** Asia Online MT Options : Start ****************** --> 
<%
     if ("Asia_Online".equalsIgnoreCase(current_engine))
     {
 %>
    <div id="aoMtDiv" style="display: block;">
<%
    }
    else
    {
%>
    <div id="aoMtDiv" style="display: none;">
<%
    }
%>
<p>
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="88%">
	<tr>
		<td colspan="2"><b><%=bundle.getString("lb_tm_ao_mt_title")%></b></td>
	</tr>
	<tr>
		<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_url")%><font color="red">*</font>:</td>
		<td><INPUT CLASS="standardText" ID="idAoMtUrl"
			NAME="<%=TMProfileConstants.MT_AO_URL%>"
			value="<%=tmProfile.getAoMtUrl() == null ? "" : tmProfile
                    .getAoMtUrl().replaceAll("\"", "")%>"
			TYPE="text" MAXLENGTH="99" SIZE="90" /></td>
	</tr>

	<tr>
		<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_port")%><font color="red">*</font>:</td>
		<td><INPUT CLASS="standardText" ID="idAoMtPort"
			NAME="<%=TMProfileConstants.MT_AO_PORT%>"
			value="<%=tmProfile.getAoMtPort() > -1 ? tmProfile
                    .getAoMtPort() : ""%>"
			TYPE="text" MAXLENGTH="5" SIZE="20" />
		</td>
	</tr>

	<tr>
		<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_username")%><font color="red">*</font>:</td>
		<td><INPUT CLASS="standardText" ID="idAoMtUsername"
			NAME="<%=TMProfileConstants.MT_AO_USERNAME%>"
			value="<%=tmProfile.getAoMtUsername() == null ? "" : tmProfile
                    .getAoMtUsername().replaceAll("\"", "")%>"
			TYPE="text" MAXLENGTH="99" SIZE="20" />
		</td>
	</tr>
	
	<tr>
		<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_password")%><font color="red">*</font>:</td>
		<td><INPUT CLASS="standardText" ID="idAoMtPassword"
			NAME="<%=TMProfileConstants.MT_AO_PASSWORD%>"
			value="<%=tmProfile.getAoMtPassword() == null ? "" : tmProfile
                    .getAoMtPassword().replaceAll("\"", "")%>"
			TYPE="password" MAXLENGTH="99" SIZE="20" />
		</td>
	</tr>

	<tr>
		<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_account_number")%><font color="red">*</font>:</td>
		<td><INPUT CLASS="standardText" ID="idAoMtAccountNumber"
			NAME="<%=TMProfileConstants.MT_AO_ACCOUNT_NUMBER%>"
			value="<%=tmProfile.getAoMtAccountNumber() > -1 ? tmProfile
                    .getAoMtAccountNumber() : ""%>"
			TYPE="text" MAXLENGTH="10" SIZE="20" />
		</td>
	</tr>

<%  if (operation != null && "previous".equals(operation))
    {
        if (exceptionInfo != null && !"".equals(exceptionInfo.trim()))
        {
%>
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr><td align="left" colspan="2"><font color="red"><b><%=exceptionInfo%></b></font></td></tr>
<%      }
        else
        {
%>
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr><td align="left" colspan="3"><font color="green"><b></b></font></td></tr>
<%
        }
    }
%>	
</TABLE>
</p>
</div>
<!-- **************** Asia Online MT Options : End ******************** --></FORM>

<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="Cancel" onclick="submitForm('cancelMTOptions');">
<INPUT TYPE="BUTTON"
	VALUE="<%=("promt".equalsIgnoreCase(current_engine) || "asia_online"
                                    .equalsIgnoreCase(current_engine)) ? lbNext : lbSave%>"
	ID="OK" onclick="submitForm('saveMTOptions');">

</DIV>

</BODY>
</HTML>
