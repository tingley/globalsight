<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
	String lbnext = bundle.getString("lb_next");
    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL += "&action=" + CompanyConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_company");
    }
    else
    {
        saveURL += "&action=" + CompanyConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_company");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CompanyConstants.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(CompanyConstants.NAMES);
    Company company = (Company)sessionMgr.getAttribute(CompanyConstants.COMPANY);
    String companyName = "";
    String email = (String)request.getAttribute(CompanyConstants.EMAIL);
    String desc = "";
    String checked = "checked";//default
    String tmAccessControl = "";//default
    String tbAccessControl = "";//default
    String ssoChecked = "";//default
    String isSsoChecked = "false";//default
    String ssoIdpUrl = "";
    String sessionTime = "";
    boolean isReviewOnly = false;
    String useSeparateTablesPerJobChecked = "";
    String qaChecks = "";
    String enableDitaChecksChecked = "";
    String enableWorkflowStatePosts = "";
    String enableBlankTmSearch = "";
    
    String inCtxRvKeyIndd = (String) request.getAttribute("incontext_review_key_indd");
    String inCtxRvKeyOffice = (String) request.getAttribute("incontext_review_key_office");
    String inCtxRvKeyXML = (String) request.getAttribute("incontext_review_key_xml");
    String inCtxRvKeyHTML = (String) request.getAttribute("incontext_review_key_html");
    
    String enableInCtxRvToolIndd = "true".equals(inCtxRvKeyIndd) ? "checked" : "";
    String enableInCtxRvToolOffice = "true".equals(inCtxRvKeyOffice) ? "checked" : "";
    String enableInCtxRvToolXML = "true".equals(inCtxRvKeyXML) ? "checked" : "";
    String enableInCtxRvToolHTML = "true".equals(inCtxRvKeyHTML) ? "checked" : "";
    
    boolean isInDesignEnabled = PreviewPDFHelper.isInDesignEnabled();
    boolean isOfficeEnabled = PreviewPDFHelper.isOfficeEnabled();
    boolean isXMLEnabled = PreviewPDFHelper.isXMLEnabled();
    boolean isHTMLEnabled = PreviewPDFHelper.isHTMLEnabled();
    boolean showInContextReivew = (isInDesignEnabled ||  isOfficeEnabled || isXMLEnabled || isHTMLEnabled);
    
    String defaultCompanyFluency = "";
    String defaultCompanyAdequacy = "";
    
    if (company != null)
    {
        companyName = company.getName();
        desc = company.getDescription();
        email = company.getEmail();
        sessionTime = company.getSessionTime();
        
        if (desc == null) desc = "";
        if (email == null) email = "";
        if (sessionTime==null) sessionTime="";
        
        boolean enableIPFilte = company.getEnableIPFilter();
        if (enableIPFilte==false) {
        	checked = "";
        }
        
        boolean enableTMAcessControl = company.getEnableTMAccessControl();
        if (enableTMAcessControl) {
            tmAccessControl = "checked";
        }
        
        boolean enableTBAcessControl = company.getEnableTBAccessControl();
        if (enableTBAcessControl) {
            tbAccessControl = "checked";
        }
        
        boolean enableQAChecks = company.getEnableQAChecks();
        if (enableQAChecks) {
            qaChecks = "checked";
        }
          
        if (company.getEnableSSOLogin())
        {
            ssoChecked = "checked";
            isSsoChecked = "true";
        }
        
        ssoIdpUrl = company.getSsoIdpUrl();
        ssoIdpUrl = ssoIdpUrl == null ? "" : ssoIdpUrl;

        if (company.getBigDataStoreLevel() == 2) {
            useSeparateTablesPerJobChecked = "checked";
        }

        if (company.getEnableDitaChecks()) {
            enableDitaChecksChecked = "checked";
        }
        
        if(company.getEnableWorkflowStatePosts()){
            enableWorkflowStatePosts = "checked";
        }
        
        if(company.getEnableBlankTmSearch()){
            enableBlankTmSearch = "checked";
        }
        
        defaultCompanyFluency = company.getDefaultFluency();
        defaultCompanyAdequacy = company.getDefaultAdequacy();
    }
%>
<html>
<head>
<title><%=title%></title>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="companyForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
            <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
            <td>
                <% if (edit) { %>
                    <%=companyName%>
                <% } else { %>
                    <input type="text" name="<%=CompanyConstants.NAME%>" maxlength="40" size="30" value="<%=companyName%>">
                <% } %>
            </td>
            <td valign="center">
                <% if (!edit) { %>
                    <%=bundle.getString("lb_valid_name")%>
                <% } %>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td colspan="2">
                <textarea rows="6" cols="40" style="width:350px;" name="<%=CompanyConstants.DESC%>"><%=desc%></textarea>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_email")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.EMAIL%>" id="emailId" value="<%=email%>">
            </td>
        </tr>
        
        <tr>
        	<td valign="top"><%=bundle.getString("lb_session_timeout")%>&nbsp;(<%=bundle.getString("lb_minutes")%>):</td>
        	<td colspan="2">
                <input type="text" name="<%=CompanyConstants.SESSIONTIME%>" maxlength="3" size="20" value="<%=sessionTime%>">&nbsp;(30-480)
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableIPFilter")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableIPFilterId" name="<%=CompanyConstants.ENABLE_IP_FILTER%>" <%=checked%>/>
            </td>
        </tr>
        
        <tr id="ssoCheck">
            <td valign="top"><%=bundle.getString("lb_sso_enableSSO")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableSsoLogonId" onclick="onEnableSSOSwitch()" name="<%=CompanyConstants.ENABLE_SSO_LOGON%>" <%=ssoChecked%>/>
            </td>
        </tr>
        <tr id="ssoIdpUrlCC">
            <td valign="top"><%=bundle.getString("lb_sso_IdpUrl")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.SSO_IDP_URL%>" maxlength="256" value="<%=ssoIdpUrl%>">
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTMAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTMAccessControl" name="<%=CompanyConstants.ENABLE_TM_ACCESS_CONTROL%>" <%=tmAccessControl%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTBAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTBAccessControl" name="<%=CompanyConstants.ENABLE_TB_ACCESS_CONTROL%>" <%=tbAccessControl%>/>
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_use_separate_tables_per_job")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" name="<%=CompanyConstants.BIG_DATA_STORE_LEVEL%>" <%=useSeparateTablesPerJobChecked%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_company_enable_qachecks")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableQAChecks" name="<%=CompanyConstants.ENABLE_QA_CHECKS%>" <%=qaChecks%>/>
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_enable_dita_checks")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" name="<%=CompanyConstants.ENABLE_DITA_CHECKS%>" <%=enableDitaChecksChecked%>/>
            </td>
        </tr>
        
        <tr>
        <td valign="top"><%=bundle.getString("lb_enable_workflow_state_posts") %>:</td>
        <td>
        	<input class="standardText" type="checkbox" name="<%=CompanyConstants.ENABLE_WORKFLOW_STATE_POSTS%>" <%=enableWorkflowStatePosts%>/>
        </td>
        </tr>
        
        <tr>
        <td valign="top"><%=bundle.getString("lb_company_enable_blank_tm_search") %>:</td>
        <td>
        	<input class="standardText" type="checkbox" name="<%=CompanyConstants.ENABLE_BLANK_TM_SEARCH%>" <%=enableBlankTmSearch%>/>
        </td>
        </tr>
        
        <tr id="inctxrvCheck" <% if (!showInContextReivew) {%>style="display:none;" <%}%> >
            <td valign="top"><%=bundle.getString("lb_incontext_review")%>:</td>
            <td colspan="2">
            </td>
        </tr>
        
        <tr id="inctxrvCheckIndd" <% if (!isInDesignEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_indesign")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolInddId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_INDD%>" <%=enableInCtxRvToolIndd%>/>
            </td>
        </tr>
        
        <tr id="inctxrvCheckOffice" <% if (!isOfficeEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_office2010")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolOfficeId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_OFFICE%>" <%=enableInCtxRvToolOffice%>/>
            </td>
        </tr>
        
        <tr id="inctxrvCheckXML" <% if (!isXMLEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_xml")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolXMLId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_XML%>" <%=enableInCtxRvToolXML%>/>
            </td>
        </tr>
        
        <tr id="inctxrvCheckHTML" <% if (!isHTMLEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_html")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolHTMLId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_HTML%>" <%=enableInCtxRvToolHTML%>/>
            </td>
        </tr>

        <tr valign="top">
    		<td colspan=3>
                 <br/>        
               <div id="toShowSegmentComment" style="cursor:pointer;font-weight:bold;display:inline-block;">
                <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                Segment Comment Category 
                </div>
    			<br/><div class="standardText"><c:out value="${helpMsg}" escapeXml="false"/></div>
                <div id="segmentCommentPanel" style="display:none;">
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="segmentCommentFrom" name="segmentCommentFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allSegmentCommentCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('segmentCommentFrom','segmentCommentTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('segmentCommentTo','segmentCommentFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="segmentCommentTo" name="segmentCommentTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${segmentCommentCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="segmentCommentCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('segmentComment')">&nbsp;
						<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('segmentComment')">
        			</td>
        		</tr>
      			</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowScorecard" style="cursor:pointer;display:inline-block;">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b>Scorecard Category</b>
                    <div class="standardText"><c:out value="${scorecardHelpMsg}" escapeXml="false"/></div>
                </div>
                <div id="scorecardPanel" style="display:none;">
    			<br/>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="scorecardFrom" name="scorecardFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allScorecardCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('scorecardFrom','scorecardTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('scorecardTo','scorecardFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="scorecardTo" name="scorecardTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${scorecardCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="scorecardCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('scorecard')">&nbsp;
						<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('scorecard')">
        			</td>
        		</tr>
      			</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                 <br/>        
               <div id="toShowQuality" style="cursor:pointer;display:inline-block;">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b>Quality Category </b>
                    <div class="standardText"><c:out value="${qualityHelpMsg}" escapeXml="false"/></div>
                </div>
                <div id="qualityPanel" style="display:none;">
    			<br/>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="qualityFrom" name="qualityFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allQualityCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('qualityFrom','qualityTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('qualityTo','qualityFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="qualityTo" name="qualityTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${qualityCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="qualityCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('quality')">&nbsp;
						<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('quality')">
        			</td>
        		</tr>
      			</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowMarket" style="cursor:pointer;display:inline-block;">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b>Market Category</b>
                    <div class="standardText"><c:out value="${marketHelpMsg}" escapeXml="false"/></div>
                </div>
                <div id="marketPanel" style="display:none;">
    			<br/>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="marketFrom" name="marketFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allMarketCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('marketFrom','marketTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('marketTo','marketFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="marketTo" name="marketTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${marketCategories}">
	      					<option title="${op.name}" value="${op.name}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="marketCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('market')">&nbsp;
						<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('market')">
        			</td>
        		</tr>
      			</table>
                </div>
    		</td>
  		</tr>
        <tr valign="top">
            <td colspan=3>
                 <br/>        
               <div id="toShowDQF" style="cursor:pointer;display:inline-block;">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b>DQF Category </b>
                    <div class="standardText">Define <b>Fluency and Adequacy</b> categories for DQF (Dynamic Quality Framework)</div>
                </div>
                <div id="dqfPanel" style="display:none;">
                <br/>
                <table border="0" class="standardText" cellpadding="2">
                    <tr>
                        <td colspan="3"><div class="standardText"><c:out value="${fluencyHelpMsg}" escapeXml="false"/>:</div></td>
                    </tr>
                    <tr>
                        <td>
                            <span><c:out value="${labelForLeftTable}"/>
                        </td>
                        <td>&nbsp;</td>
                        <td>
                            <span><c:out value="${labelForRightTable}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <select id="fluencyFrom" name="fluencyFrom" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${allFluencyCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                        <td>
                            <table>
                            <tr>
                            <td>
                                <input type="button" name="addButton" value=" >> "
                                onclick="move('fluencyFrom','fluencyTo')"><br>
                            </td>
                            </tr>
                            <tr><td>&nbsp;</td></tr>
                            <tr>
                                <td>
                                <input type="button" name="removedButton" value=" << "
                                onclick="move('fluencyTo','fluencyFrom')">
                                </td>
                            </tr>
                            </table>
                        </td>
                        <td>
                            <select id="fluencyTo" name="fluencyTo" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${fluencyCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=3>
                            <table border="0" class="standardText" cellpadding="2">
                                <tr>
                                    <td>
                                        <span><c:out value="${label}"/></span> :
                                    </td>
                                    <td>
                                        <input id="fluencyCategory" size="40" maxlength="100">
                                        <input style="display:none">
                                    </td>
                                    <td>
                                        <input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('fluency')">&nbsp;
										<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('fluency')">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr><td colspan="3">&nbsp;</td></tr>
                    <tr>
                        <td colspan="3"><div class="standardText"><c:out value="${adequacyHelpMsg}" escapeXml="false"/>:</div></td>
                    </tr>
                    <tr>
                        <td>
                            <span><c:out value="${labelForLeftTable}"/>
                        </td>
                        <td>&nbsp;</td>
                        <td>
                            <span><c:out value="${labelForRightTable}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <select id="adequacyFrom" name="adequacyFrom" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${allAdequacyCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                        <td>
                            <table>
                            <tr>
                            <td>
                                <input type="button" name="addButton" value=" >> "
                                onclick="move('adequacyFrom','adequacyTo')"><br>
                            </td>
                            </tr>
                            <tr><td>&nbsp;</td></tr>
                            <tr>
                                <td>
                                <input type="button" name="removedButton" value=" << "
                                onclick="move('adequacyTo','adequacyFrom')">
                                </td>
                            </tr>
                            </table>
                        </td>
                        <td>
                            <select id="adequacyTo" name="adequacyTo" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${adequacyCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=3>
                            <table border="0" class="standardText" cellpadding="2">
                                <tr>
                                    <td>
                                        <span><c:out value="${label}"/></span> :
                                    </td>
                                    <td>
                                        <input id="adequacyCategory" size="40" maxlength="100">
                                        <input style="display:none">
                                    </td>
                                    <td>
                                        <input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('adequacy')">&nbsp;
										<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('adequacy')">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr><td colspan="3">&nbsp;</td></tr>
                  </table>
                </div>
            </td>
        </tr>
        <tr valign="top">
            <td colspan=3>
                <br/>        
                <div id="toShowSeverity" style="cursor:pointer;display:inline-block;">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_dqf_severity_category") %></b>
                    <div class="standardText"><c:out value="${severityHelpMsg}" escapeXml="false"/></div>
                </div>
                <div id="severityPanel" style="display:none;">
                    <br>
                    <table border="0" class="standardText" cellpadding="2">
                    <tr>
                        <td>
                            <span><c:out value="${labelForLeftTable}"/>
                        </td>
                        <td>&nbsp;</td>
                        <td>
                            <span><c:out value="${labelForRightTable}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <select id="severityFrom" name="severityFrom" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${allSeverityCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                        <td>
                            <table>
                            <tr>
                            <td>
                                <input type="button" name="addButton" value=" >> "
                                onclick="move('severityFrom','severityTo')"><br>
                            </td>
                            </tr>
                            <tr><td>&nbsp;</td></tr>
                            <tr>
                                <td>
                                <input type="button" name="removedButton" value=" << "
                                onclick="move('severityTo','severityFrom')">
                                </td>
                            </tr>
                            </table>
                        </td>
                        <td>
                            <select id="severityTo" name="severityTo" multiple class="standardText" size="10" style="width:250">
                            <c:forEach var="op" items="${severityCategories}">
                                <option title="${op.name}" value="${op.name}">${op.name}</option>
                            </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <table class="standardText" colspan=3>
                            <tr>
                        <td>
                            <span><c:out value="${label}"/></span> :
                        </td>
                        <td>
                            <input id="severityCategory" size="40" maxlength="100">
                            <input style="display:none">
                        </td>
                        <td>
                            <input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addCategory('severity')">&nbsp;
							<input type="button" name="remove" value="<%=bundle.getString("lb_remove") %>" onclick="removeCategory('severity')">
                        </td>
                            </tr>
                        </table>
                    </tr>
                    </table>
                </div>
            </td>
        </tr>
        </div>
        
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
                <input type="button" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
            </td>
        </tr>

      </table>
    </td>
  </tr>
  
</table>
</form>
</div>
</body>
<script SRC="/globalsight/jquery/jquery-1.11.3.min.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script>
$().ready(function() {
    initCategoryShow();
});

function initCategoryShow() {
    $("#toShowSegmentComment").click(function() {
       if ($("#segmentCommentPanel").css("display") == "none") {
           $("#segmentCommentPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#segmentCommentPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
    $("#toShowScorecard").click(function() {
       if ($("#scorecardPanel").css("display") == "none") {
           $("#scorecardPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#scorecardPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
    $("#toShowQuality").click(function() {
       if ($("#qualityPanel").css("display") == "none") {
           $("#qualityPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#qualityPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
    $("#toShowMarket").click(function() {
       if ($("#marketPanel").css("display") == "none") {
           $("#marketPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#marketPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
    $("#toShowDQF").click(function() {
       if ($("#dqfPanel").css("display") == "none") {
           $("#dqfPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#dqfPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
    $("#toShowSeverity").click(function() {
       if ($("#severityPanel").css("display") == "none") {
           $("#severityPanel").css("display", "block");
           $(this).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
       }
       else {
           $("#severityPanel").css("display", "none");
           $(this).find("img").attr("src", "/globalsight/images/enlarge.jpg");
       }
    });
}

//Move option from f to t
function move(f,t) {
	var $from = $("#" + f + " option:selected");
	var $to = $("#" + t);
	if ($from.length>0) {
		$from.each(function() {
			$to.append("<option value='" + $(this).val() + "'>" + $(this).text()+"</option>");
			$(this).remove();
		});
	}
}

//Sort options
function SortD(box){
	var $box = $("#" + box);
    $box.find("option").sort(function(a,b) {
        var aText = $(a).text().toUpperCase();
        var bText = $(b).text().toUpperCase();
        if (aText>bText) return 1;
        if (aText<bText) return -1;
        return 0;
    }).appendTo($box);
}

function isLetterAndNumber(str){
	var reg = new RegExp("^[A-Za-z0-9 _,.-]+$");
	return (reg.test(str));
}

function isChinese(str){
	return str.match(/[\u4e00-\u9fa5]/g);
}

//Add new category
function addCategory(key) {
	var newCategoryName = $("#" + key + "Category").val().trim();
	if (newCategoryName == "") {
        alert("<%=bundle.getString("msg_company_category_invalid") %>");
        return;
	}
    if (!isLetterAndNumber(newCategoryName) && !isChinese(newCategoryName))
    {
        alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
        return false;
    }
    if (!checkNewCategory(key, newCategoryName)) {
        var tmp = "<option value='" + newCategoryName + "'>" + newCategoryName + "</option>";
        var keyElement = key + "To";
        var $to = $("#" + keyElement);
        $to.append(tmp);
        
        //SortD(keyElement);
    } else {
    	alert("There is existing category with the name already. Please using another name.");
    	return;
    }
}

//Add new category
function removeCategory(key) {
	var $from = $("#" + key + "From" + " option:selected");
	if ($from.length>0) {
		$from.each(function() {
			$(this).remove();
		});
	} else {
		alert("<%=bundle.getString("msg_category_remove_alert") %>");
	}
}


function checkNewCategory(key, name) {
	var tmp = "";
    var exist = false;
    
    $("select[id^='" + key + "'] option").each(function() {
        if ($(this).val().toUpperCase() == name.toUpperCase()) {
            exist = true;
            return false;
        }
    });
    
    return exist;
}

var needWarning = true;
var objectName = "<%=bundle.getString("lb_company")%>";
var guideNode="companies";
var helpFile = "<%=bundle.getString("help_companies_basic_screen")%>";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        companyForm.action = "<%=cancelURL%>";
        companyForm.submit();
    }
    else if (formAction == "save")
    {
    	if (confirmForm() && confirmTime())
		{
            $("select option").each(function() {
                $(this).attr("selected", true);
            });
            
        	companyForm.action = "<%=saveURL%>";
            companyForm.submit();
		}
    }
}

//
// Check required fields(SSO, email, name).
// Check duplicate activity name.
//
function confirmForm()
{
	// check sso
	var ssoLogonElem = companyForm.enableSsoLogonField;
	if(ssoLogonElem!=null && ssoLogonElem.checked)
    {
        var idpUrl = companyForm.ssoIdpUrlField.value;
        if (isEmptyString(idpUrl))
        {
        	alert("<%=bundle.getString("msg_sso_input_valid_idpurl")%>");
            return false;
        }
    }
	
	// Check Email Field
	var emailElem = document.getElementById("emailId");
	var sysNotificationEnable = "<%=request.getAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED)%>";
    if("true" == sysNotificationEnable)
    {
    	var email = stripBlanks(emailElem.value);
    	if(email.length > 0 && !validEmail(email))
    	{
    		alert("<%=bundle.getString("jsmsg_email_invalid")%>");
            return false;
    	}
    }
	
	// check name
    if (!companyForm.nameField) 
    {
        // can't change name on edit
        return true;
    }
    if (isEmptyString(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_company_name"))%>");
        companyForm.nameField.value = "";
        companyForm.nameField.focus();
        return false;
    }   
    
    //Check if the company name is one of key words
    var companyName = ATrim(companyForm.nameField.value).toLowerCase();
	var words = new Array("com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "con", "prn", "aux", "nul", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");
	var tmp = "", tmpPrefix = "";
	for (x in words) {
	  tmp = words[x];
	  tmpPrefix = tmp + ".";
	  if (companyName == tmp || companyName.indexOf(tmpPrefix) == 0) {
		alert("<%=EditUtil.toJavascript(bundle.getString("msg_invalid_company_name"))%>");
		return false;
	  }
	}
    
    if (hasSpecialChars(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String comName = (String)names.get(i);
%>
            if ("<%=comName%>".toLowerCase() == companyForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company"))%>");
                return false;
            }
<%
        }
    }
%>

	return true;
}

function confirmTime()
{
	var sessionTime = companyForm.sessionTimeField.value;
	{
		if (sessionTime!='')
		{
			if(isNumeric(sessionTime))
			{
				sessionTime = parseInt(sessionTime)
				if (sessionTime > 480 || sessionTime < 30)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
					return false;
				}
			}
			else
			{
				alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
				return false;
			}
		}
	}
    return true;
}

function isNumeric(str){
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}

function onEnableSSOSwitch()
{
	onEnableSSO(companyForm.enableSsoLogonField.checked);
}

function onEnableSSO(checked)
{
	var ele = document.getElementById("ssoIdpUrlCC");
	var display = checked ? "" : "none";
	ele.style.display = display;
}

function doOnload()
{
    loadGuides();

    var edit = eval("<%=edit%>");
    if (edit)
    {
        companyForm.<%=CompanyConstants.DESC%>.focus();
    }
    else
    {
        companyForm.<%=CompanyConstants.NAME%>.focus();
    }

    var enableSSO = <%=request.getAttribute(SystemConfigParamNames.ENABLE_SSO)%>;
    if(!enableSSO)
    {
		document.getElementById("ssoCheck").style.display = "none";
		document.getElementById("ssoIdpUrlCC").style.display = "none";
    }
    else
    {
    	onEnableSSO(eval("<%=isSsoChecked%>"));
    }
}

function Trim(str)
{
	if(str=="") return str;
	var newStr = ""+str;
	RegularExp = /^\s+|\s+$/gi;
	return newStr.replace( RegularExp,"" );
}
</script>
</html>
