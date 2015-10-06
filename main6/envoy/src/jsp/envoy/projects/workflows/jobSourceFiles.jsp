<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.jobhandler.Job,com.globalsight.everest.permission.Permission,
    com.globalsight.cxe.entity.fileprofile.FileProfile,
    com.globalsight.cxe.entity.fileprofile.FileProfileUtil,
    com.globalsight.everest.servlet.util.ServerProxy,
    com.globalsight.everest.permission.PermissionSet,com.globalsight.everest.webapp.WebAppConstants,
    com.globalsight.everest.util.system.SystemConfigParamNames,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.AddSourceHandler,
    com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator,
    com.globalsight.everest.util.system.SystemConfiguration,
    com.globalsight.everest.company.CompanyThreadLocal,
    com.globalsight.everest.page.JobSourcePageDisplay,
    com.globalsight.everest.foundation.User,
    com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
    java.text.MessageFormat,java.util.*"
    session="true"
%>
<jsp:useBean id="jobDetails" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetailsPDFs" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobComments" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="neweditor" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="sourceEditor" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="allStatus" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editSourcePageWc" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="pageSearch" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="searchText" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="incontextreiview" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	//jobSummary child page needed started.
	ResourceBundle bundle = PageHandler.getBundle(session);
	String jobCommentsURL = jobComments.getPageURL() + "&jobId="
			+ request.getAttribute("jobId");
	//jobSummary child page needed end.
	String lb_filter_text = bundle.getString("lb_source_file_filter");// used by the pageSearch include
	String thisFileSearch = (String) request
			.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
	if (thisFileSearch == null)
	{
		thisFileSearch = "";
	}
	
	String url_incontextreview = incontextreiview.getPageURL();
	
	String searchTextUrl = searchText.getPageURL()
			+ "&action=searchText" + "&jobId="
			+ request.getAttribute("jobId");
	Map<Long, String> targetLocaleMap = (Map<Long, String>) request
			.getAttribute("targetLocaleMap");

	String thisFileSearchText = (String) request
			.getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT);
	if (thisFileSearchText == null)
	{
		thisFileSearchText = "";
	}
	String thisSearchLocale = (String) request
			.getAttribute(JobManagementHandler.PAGE_SEARCH_LOCALE);
	if (thisSearchLocale == null)
	{
		thisSearchLocale = "sourceLocale";
	}
	String thisTargetLocaleId = (String) request
			.getAttribute(JobManagementHandler.PAGE_TARGET_LOCAL);
	if (thisTargetLocaleId == null || thisTargetLocaleId.equals("null"))
	{
		thisTargetLocaleId = "-1";
	}

	String pageSearchURL = pageSearch.getPageURL() + "&jobId="
			+ request.getAttribute("jobId");
	String editSourcePageWcURL = editSourcePageWc.getPageURL()
			+ "&jobId=" + request.getAttribute("jobId");
	String jobSourceFilesURL = jobSourceFiles.getPageURL() + "&jobId="
			+ request.getAttribute("jobId");
	String addSourceFilesURL = addSourceFiles.getPageURL() + "&jobId="
			+ request.getAttribute("jobId");
	String checkPageExistURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.CHECK_PAGE_EXIST;
	String beforeAddDeleteSourceURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.CAN_ADD_DELETE_SOURCE_FILES;
	String beforeDeleteSourceURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.BEFORE_DELETE_SOURCE_FILES;
	String deleteSourceURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.DELETE_SOURCE_FILES;
	String showDeleteProgressURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.SHOW_DELETE_PROGRESS;
	String downloadSourceURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.DOWNLOAD_SOURCE_FILES;
	String uploadSourceURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.UPLOAD_SOURCE_FILES;
	String showUpdateProgressURL = addSourceFilesURL + "&action="
			+ AddSourceHandler.SHOW_UPDATE_PROGRESS;

	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

	Job jobImpl = (Job) request.getAttribute("Job");
	boolean isIE = request.getHeader("User-Agent").indexOf("MSIE") != -1;
	
	List<JobSourcePageDisplay> jobSourcePageDisplayList = (List<JobSourcePageDisplay>)request.getAttribute("JobSourcePageDisplayList");

	SystemConfiguration sysConfig = SystemConfiguration.getInstance();
	boolean useSSL = sysConfig
			.getBooleanParameter(SystemConfigParamNames.USE_SSL);
	String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	if (useSSL == true)
	{
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
	}
	else
	{
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	}

	StringBuffer appletcontent = new StringBuffer();
	if (isIE)
	{
		appletcontent
				.append("<OBJECT classid=\"clsid:CAFEEFAC-0018-0000-0045-ABCDEFFEDCBA\" width=\"920\" height=\"500\" ");
		appletcontent.append("NAME = \"FSV\" codebase=\"");
		appletcontent.append(httpProtocolToUse);
		appletcontent
				.append("://javadl.sun.com/webapps/download/AutoDL?BundleId=107109\"> ");
		appletcontent
				.append("<PARAM NAME = \"code\" VALUE = \"com.globalsight.EditSourceApplet\" > ");
	}
	else
	{
		appletcontent
				.append("<APPLET style=\"display:inline\" type=\"application/x-java-applet;jpi-version=1.8.0_45\" width=\"920\" height=\"500\" code=\"com.globalsight.EditSourceApplet\" ");
		appletcontent
				.append("pluginspage=\"");
		appletcontent.append(httpProtocolToUse);
		appletcontent
				.append("://www.java.com/en/download/manual.jsp\"> ");
	}
	appletcontent
			.append("<PARAM NAME = \"cache_option\" VALUE = \"Plugin\" > ");
	appletcontent
			.append("<PARAM NAME = \"cache_archive\" VALUE = \"applet/lib/SelectFilesApplet.jar, applet/lib/commons-codec-1.3.jar, applet/lib/commons-httpclient-3.0-rc2.jar, applet/lib/commons-logging.jar, applet/lib/jaxrpc.jar, applet/lib/axis.jar, applet/lib/commons-discovery.jar, applet/lib/wsdl4j.jar, applet/lib/webServiceClient.jar\">");
	appletcontent.append("<PARAM NAME = NAME VALUE = \"FSV\"> ");
	appletcontent
			.append("<PARAM NAME = \"scriptable\" VALUE=\"true\"> ");
	appletcontent.append("<PARAM NAME = \"jobId\" value=\""
			+ jobImpl.getJobId() + "\"> ");
	appletcontent.append("<PARAM NAME = \"companyId\" value=\""
			+ CompanyThreadLocal.getInstance().getValue() + "\"> ");
	appletcontent.append("<PARAM NAME = \"pageLocale\" value=\""
			+ bundle.getLocale() + "\"> ");
	appletcontent.append("<PARAM NAME = \"projectId\" value=\""
			+ jobImpl.getProjectId() + "\"> ");
	appletcontent.append("<PARAM NAME = \"l10nProfileId\" value=\""
			+ jobImpl.getL10nProfileId() + "\"> ");
	appletcontent.append("<PARAM NAME = \"userName\" value=\""
			+ user.getUserName() + "\"> ");
	appletcontent.append("<PARAM NAME = \"password\" value=\""
			+ user.getPassword() + "\"> ");
	appletcontent
			.append("<PARAM NAME = \"addToApplet\" value=\"MainAppletWillAddThis\"> ");

	if (isIE)
	{
		appletcontent.append(" </OBJECT>");
	}
	else
	{
		appletcontent.append(" </APPLET>");
	}
	
	boolean okForInContextReviewXml = PreviewPDFHelper.isXMLEnabled("" + jobImpl.getCompanyId());
	boolean okForInContextReviewIndd = PreviewPDFHelper.isInDesignEnabled("" + jobImpl.getCompanyId());
	boolean okForInContextReviewOffice = PreviewPDFHelper.isOfficeEnabled("" + jobImpl.getCompanyId());
%>
<html>
<head>
<title><%=bundle.getString("lb_SourceFiles")%></title>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<title><%=bundle.getString("lb_workflows")%></title>
<style>
#sourceFilesTbody td {
	padding-top:3px;
}

#fullbg {
    background-color: Gray;
    display:none;
    z-index:1;
    position:absolute;
    left:0px;
    top:0px;
    filter:Alpha(Opacity=30);
    /* IE */
    -moz-opacity:0.4;
    /* Moz + FF */
    opacity: 0.4;
}

#container {
    position:absolute;
    display: none;
    z-index: 2;
}
</style>
<script type="text/javascript">
var pageNames = new Array();
var incontextReviewPDFs = new Array();

function contorlTargetLocale(){
	var localeSelect = document.getElementById("<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>");
    var locale = localeSelect.options[localeSelect.selectedIndex].value;
    
    if(locale == "targetLocale"){
    	targetLocaleTable.style.display='block';
    	 if(isFirefox=navigator.userAgent.indexOf("Firefox")>0){
    		 targetLocaleTable.style.paddingTop='6px';
    	 } 
    	pageSearchTextTd.style.width='30%';
    }else if(locale == "sourceLocale"){
    	targetLocaleTable.style.display='none';
    	pageSearchTextTd.style.width='55%';
    }
}
function searchPages(){
	var iChars = "#,%,^,&,+,\\,\',\",<,>.";
	var localesSelect = document.getElementById("<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>");
 	var index = localesSelect.selectedIndex;
    var locale = localesSelect.options[index].value;
    var searchText = document.getElementById("pageSearchText").value;
    searchText = ATrim(searchText);
    var targetLocaleId = null;
    if(locale == "targetLocale"){
    	var targetLocaleSelect = document.getElementById("targetLocale");
    	targetLocaleId = targetLocaleSelect.options[targetLocaleSelect.selectedIndex].value;
    }
    if(checkSomeSpecialChars(searchText)){
    	 alert("<%= bundle.getString("lb_tm_search_text") %>" + "<%= bundle.getString("msg_invalid_entry4") %>" + iChars);
         return false;
    }
    var url = "<%=searchTextUrl%>" + "&pageSearchLocale="+locale+"&pageSearchText="+encodeURI(encodeURI(searchText))+"&targetLocale="+targetLocaleId;
    pageSearchTextForm.action = url;
    pageSearchTextForm.submit();
}
</script>

</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="load()"; id="idBody" onunload="unload()" class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/pageSort.jspIncl"%>
<div id="contentLayer"  class="standardText" style="position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">
<div id="includeSummaryTabs">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<div style="clear:both;padding-top:1em" ></div>
<div id="sourceFiles" style="width:900px;">
	<%@ include file="/envoy/projects/workflows/pageSearch.jspIncl" %>
		    <FORM name="pageSearchTextForm"  id="pageSearchTextForm" method="post" action=""  ENCTYPE="multipart/form-data">
				<table  CELLSPACING="0" CELLPADDING="0" BORDER="0" style="border:solid 1px slategray;background:#DEE3ED;width:900;height:40">
				  <tr valign="middle">
				  	  <td class="standardText"><%=bundle.getString("lb_search_in")%>:</td>
					  <td  class="standardText">
					  		<select class="standardText" id="<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>" name="<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>" onchange="contorlTargetLocale(this.value);">
					  			<option value="sourceLocale" <%=thisSearchLocale.equals("sourceLocale") ? "selected" : ""%>><%=bundle.getString("lb_tm_search_source_locale")%></option>
					  			<option value="targetLocale" <%=thisSearchLocale.equals("targetLocale") ? "selected" : ""%>><%=bundle.getString("lb_tm_search_target_locale")%></option>
					  		</select>
					  </td>
					  <td class="standardText" style="display:none" id="targetLocaleTable">
					  		<table BORDER="0" class="standardText">
					  			<tr>
					  				<td class="standardText">Target Locale:</td>
					  				<td class="standardText" id="targetLocaleTd">
					  					<select class="standardText" id="<%=JobManagementHandler.PAGE_TARGET_LOCAL%>">
								  		<%
								  			if (!targetLocaleMap.isEmpty())
								  			{
								  				Set<Long> keySet = targetLocaleMap.keySet();
								  				Iterator<Long> it = keySet.iterator();
								  				while (it.hasNext())
								  				{
								  					long localeId = it.next();
								  					String locale = targetLocaleMap.get(localeId);
								  		%>
								  				<option value="<%=localeId%>" <%=Long.parseLong(thisTargetLocaleId) == localeId ? "selected": ""%>> <%=locale%></option>
									   <%
								  			}
								  			}
								  		%>
								  		</select>
					  				</td>
					  			</tr>
					  		</table>
					  </td>
				    <td class="standardText" ><%=bundle.getString("lb_search_for")%>:</td>
				    <td class="standardText" id="pageSearchTextTd" style="width:55%">
				    	<input type="text" maxlength="200" style="width:100%"  id = "<%=JobManagementHandler.PAGE_SEARCH_TEXT%>" 
				       	name="<%=JobManagementHandler.PAGE_SEARCH_TEXT%>" value="<%=thisFileSearchText%>"/>
				    </td>
				    <td class="standardText">
				    	<input type="submit" value="<%=bundle.getString("lb_search")%>" onclick ="searchPages();"/>
				    </td>
				  </tr>
				</table> 
			</FORM>
	<table class="standardText" cellpadding="3" cellspacing="0" style="width:900px;border:solid 1px slategray;">
		<thead class="scroll">
			<tr class="tableHeadingBasic" valign="bottom" width="100%">
				<td class="scroll" style="padding-left: 0px; padding-top: 2px; padding-bottom: 2px;width:60%;height:30px;text-align:left;">
					<c:if test="${addCheckBox}"><input type="checkbox"  name="selectAll" id="selectAll" onclick="selectAll()"/></c:if>
					<a class="sortHREFWhite" href="<%=jobSourceFilesURL%>&pageSearchParam=${pageSearchParam}&pageSort=0"><%=bundle.getString("lb_primary_source_files")%>
						<%=pageNameSortArrow%>
					</a>
				</td>
				<td class="scroll" style="padding:2 0;width:20%;text-align:left;white-space:nowrap"  >
					<%=bundle.getString("lb_file_profile")%>
				</td>
				<td class="scroll" style="padding:2 0;width:12%;text-align:center;white-space:nowrap">
					<a class="sortHREFWhite" href="<%=jobSourceFilesURL%>&pageSearchParam=${pageSearchParam}&pageSort=2"><%=bundle.getString("lb_source_word_count")%>
						<%=wordCountSortArrow%>
					</a>
				</td>
				<td class="scroll" style="padding:2 0;width:8%;text-align:center;white-space:nowrap">
					<%=bundle.getString("lb_source")%>
				</td>
			</tr>
	    </thead>
	    <tbody id="sourceFilesTbody">
	    	<!-- SourcePages -->
		    <c:forEach items="${JobSourcePageDisplayList}" var="item" varStatus="status">
		    	<tr>
		    		<td style="word-wrap: break-word;word-break:break-all;text-align:left;">
			    		<c:if test="${addCheckBox}">
			    			<input class="checkSourceFiles" type="checkbox" name="pageIds" value="${item.sourcePage.id}"/>
			    		</c:if>
			    		<c:choose>
				    		<c:when test="${item.sourcePage.primaryFileType == 2}">
								<img src="/globalsight/images/file_unextracted.gif" title="Unextracted File" width="13" height="15"/>
							</c:when>
							<c:otherwise>
								<img src="/globalsight/images/file_extracted.gif" title="Extracted File" width="13" height="15"/>
							</c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${item.sourcePage.pageState == 'IMPORT_FAIL' || cancelledWorkflow}">
								<span 
									<c:if test="${item.sourcePage.pageState == 'IMPORT_FAIL'}">
										class="warningText"
									</c:if>
								>
									${item.sourcePage.displayPageName}
								</span>
							</c:when>
							<c:otherwise>
								<amb:permission  name="<%=Permission.JOB_FILES_EDIT%>" >
									<c:choose>
							    		<c:when test="${item.sourcePage.primaryFileType == 2}">
											<a class="standardHREF" href="${item.pageUrl}" target="_blank" title="${item.sourcePage.displayPageName}">
										</c:when>
										<c:otherwise>
											<a class="standardHREF" href="#" onclick="openViewerWindow('${item.pageUrl}');return false;" oncontextmenu="contextForPage('${item.pageUrl}',event, '${ status.index}')" onfocus="this.blur();" title="${item.sourcePage.displayPageName}">
										</c:otherwise>
									</c:choose>
								</amb:permission>
									<c:if test="${shortOrFullPageNameDisplay == 'full'}">
										${item.sourcePage.displayPageName}
									</c:if>
									<c:if test="${shortOrFullPageNameDisplay == 'short'}">
										${item.sourcePage.shortPageName}
									</c:if>
								<amb:permission  name="<%=Permission.JOB_FILES_EDIT%>" >
									</a>
								</amb:permission>
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align:left">
						${item.dataSourceName}
					</td>
					<td style="text-align:center">
						<span <c:if test="${item.isWordCountOverriden}">style="font-style:oblique;font-weight:bold;"
							      <c:set value="true" var="sourcePageWordCountOverriden" scope="page"></c:set>
							  </c:if>>${item.sourcePage.wordCount}
						</span>
					</td>
					<td style="text-align:center">
						<a href="${item.sourceLink}" target="_blank"><%=bundle.getString("lb_click_to_view")%></a>
					</td>
		    	</tr>
		    </c:forEach>
		    
		   	<!-- AddingPages -->
		    <c:forEach items="${JobAddingSourcePageList}" var="item">
		    	<tr style="color:gray">
		    		<td style="word-wrap: break-word;word-break:break-all;text-align:left;">
			    		<c:if test="${addCheckBox}">
			    			<input type="checkbox" name="notCheck" value="${item.id}" disabled="disabled"/>
			    		</c:if>
						<img src="/globalsight/images/file_update.gif" title="<%=bundle.getString("lb_file_adding")%>" width="13" height="15"/>
						<c:if test="${shortOrFullPageNameDisplay == 'full'}">
							${item.displayPageName}
						</c:if>
						<c:if test="${shortOrFullPageNameDisplay == 'short'}">
							${item.shortPageName}
						</c:if>
					</td>
					<td style="text-align:left">
						${item.dataSource}
					</td>
					<td style="text-align:center">
						--
					</td>
					<td style="text-align:center">
						<%=bundle.getString("lb_adding_file_status")%>
					</td>
		    	</tr>
		    </c:forEach>
		    
		   	<!-- UpdatedPages -->
		    <c:forEach items="${JobUpdatedSourcePageList}" var="item">
		    	<tr valign="top" style="color:gray">
		    		<td style="word-wrap: break-word;word-break:break-all;text-align:left;">
			    		<c:if test="${addCheckBox}">
			    			<input type="checkbox" name="notCheck" value="${item.id}" disabled="disabled"/>
			    		</c:if>
						<img src="/globalsight/images/file_update.gif" title="<%=bundle.getString("lb_file_adding")%>" width="13" height="15"/>
						<c:if test="${shortOrFullPageNameDisplay == 'full'}">
							${item.displayPageName}
						</c:if>
						<c:if test="${shortOrFullPageNameDisplay == 'short'}">
							${item.shortPageName}
						</c:if>
					</td>
					<td style="text-align:left">
						${item.dataSource}
					</td>
					<td style="text-align:center">
						--
					</td>
					<td style="text-align:center">
						<%=bundle.getString("lb_updating_file_status")%>
					</td>
		    	</tr>
		    </c:forEach>
		    
	    </tbody>
	    <c:if test="${atLeastOneError}">
	    	<tr>
	    		<td colspan="2" align="right">
	    			<input class="standardText" type="button" name="PageError" value="<%=bundle.getString("action_view_errors")%>..." onclick="submitForm()">
	    		</td>
	    	</tr>
	    </c:if>
	    <amb:permission  name="<%=Permission.JOB_FILES_DOWNLOAD%>" >
	    	<tr valign="top">
	    		<td colspan="3"></td>
	    		<td align="right">
	    			<input type="button" <c:if test="${sourcePagesSize == 0}">disabled="disabled"</c:if> 
	    				value="<%=bundle.getString("lb_download_files_in_job_detail")%>" onclick="location.href='<%=jobSourceFilesURL%>&action=downloadSourcePages'"/>
	    		</td>
	    	</tr>
		</amb:permission>
		<tr style="padding:3 0;">
			<td height="1" bgcolor="000000" colspan="4" style="padding:0;">
			</td>
		</tr>
		<tr valign="top">
			<td style="word-wrap: break-word;word-break:break-all;padding:0 3px">
				<%=bundle.getString("lb_source_word_count_total")%>
			</td>
			<td></td>
			<td style="text-align:center;">
				<span <c:if test="${wordCountOverridenAtAll || sourcePageWordCountOverriden}">style="font-style:oblique;font-weight:bold;"</c:if>>
					${Job.wordCount}
				</span>
					<c:if test="${canModifyWordCount}">
						<amb:permission  name="<%=Permission.JOB_FILES_EDIT%>" >
							<amb:permission  name="<%=Permission.JOB_SOURCE_WORDCOUNT_TOTAL%>" >
								(<a href="<%=editSourcePageWcURL%>" class="standardHREFDetail"><%=bundle.getString("lb_edit")%></a>)
							</amb:permission>
						</amb:permission>
					</c:if>
			</td>
			<td></td>
		</tr>
		<tr id="fileCounts"">
			<td style="word-wrap: break-word;word-break:break-all;padding:0 3px;">
				<span class="standardtext"><%=bundle.getString("lb_primary_source_files_number")%></span>
			</td>
			<td></td>
			<td style="text-align:center;">
				${sourcePagesSize}
			</td>
			<td></td>
		</tr>
		<!-- text explainging about the bold and italics -->
		<c:if test="${wordCountOverridenAtAll || sourcePageWordCountOverriden}">
			<p>
			<tr>
				<td>
					<span class="smallTextGray" style="font-style:oblique;font-weight:bold;"><%=bundle.getString("helper_text_override_word_count")%></span>
				</td>
				<td></td>
			</tr>
		</c:if>
		<tr>
			<td colspan="4">
				<amb:permission  name="<%=Permission.ADD_SOURCE_FILES%>" >
					<input class="standardText" type="button" name="Add Files" value="<%=bundle.getString("lb_add_files")%>" onclick="addSourceFiles()">
				</amb:permission>
				<amb:permission  name="<%=Permission.DELETE_SOURCE_FILES%>" >
					<input class="standardText" type="button" name="remove Files" value="<%=bundle.getString("lb_remove_files")%>" onclick="removeSourceFiles()">
				</amb:permission>
				<amb:permission  name="<%=Permission.EDIT_SOURCE_FILES%>" >
					<input class="standardText" type="button" name="download Files" value="<%=bundle.getString("lb_download_edit")%>" onclick="downloadFiles()">
					<input class="standardText" type="button" name="upload Files" value="<%=bundle.getString("lb_upload_edit")%>" onclick="openUploadFile();">
				</amb:permission> 
			</td>
	   </tr>	    
	</table>
	
	<!-- popup jquery dialog instead of dojo -->
	<div id="fullbg"></div>
		<div id="container">
		<div id="updateWordCountsProgressBar"></div>
	</div>
	
	<div id="addSourceDiv" title="<%=bundle.getString("title_add_source_file")%>" style="display:none">
		<div id="appletDiv" style="padding: 0px; margin: 0px; width:920px; height:500px;"></div>
	</div>
	
	<div id="uploadFormDiv" title="<%=bundle.getString("title_upload_source_file")%>" style="display:none" class="standardtext">
		<form name="uploadForm" method="post" action="<%=uploadSourceURL%>" enctype="multipart/form-data" id="uploadForm"  target="ajaxUpload">
			<input type="hidden" id="jobId" name="jobId" value="${jobId}">
			<table style="width: 650px;" class="standardText">
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" align="center" valign="middle"
							style="width: 600px;" class="standardText">
						<table class="standardText" style="font-size:10pt">
							<tr>
								<td><%=bundle.getString("lb_file")%>:</td>
								<td valign="middle"><input type="file" name="uploadFile" 
									style="width: 380px; height: 27px;" size="25"
									id="fileUploadDialog"></td>
								<td valign="middle" style="padding-left:15px">
									<button type="button" onclick="uploadFileDialog()"><%=bundle.getString("lb_upload")%></button>
									<button type="button" onclick="$('#uploadFormDiv').dialog('close')"><%=bundle.getString("lb_close")%></button>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
			</table>
		</form>
	</div>
	<!-- for uploading file asynchronous -->
	<iframe id="ajaxUpload" name="ajaxUpload" style="display:none"></iframe>
</div>
</div>

//load script and sytle after DOM loaded
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<script src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js" type="text/javascript"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var w_viewer = null;
var w_addSourceFileWindow = null;
var helpFile = "<%=bundle.getString("help_job_sourcefiles")%>";

function load(){
	ContextMenu.intializeContextMenu();
	loadGuides();
	var thisTargetLocaleId = "<%=thisTargetLocaleId%>";
	if(thisTargetLocaleId != "-1"){
		targetLocaleTable.style.display='block';
		 if(isFirefox=navigator.userAgent.indexOf("Firefox")>0){
    		 targetLocaleTable.style.paddingTop='6px';
    	 } 
		pageSearchTextTd.style.width='30%';
	}
}

function unload(){
    if (w_viewer != null && !w_viewer.closed)
    {
        w_viewer.close();
    }
    w_viewer = null;
    
    if (w_addSourceFileWindow != null && !w_addSourceFileWindow.closed)
    {
    	w_addSourceFileWindow.close();
    }
    w_addSourceFileWindow = null;
}

function selectAll(){
	var selectAll = $("#selectAll").is(":checked");
	if (selectAll) {
	   $(".checkSourceFiles").attr("checked","true");
	}else{
	   $(".checkSourceFiles").removeAttr("checked");
	}
}

function openInContextReview(url)
{
    document.getElementById("idBody").focus();
	
	var ajaxUrl = "<%=checkPageExistURL%>&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>")) +"&targetLocale="+"<%=thisTargetLocaleId%>"+ url;
	$.get(ajaxUrl,function(data){
		if(data==""){
        	if (w_viewer != null && !w_viewer.closed)
            {
                w_viewer.focus();
                return;
            }

            var style = "resizable=yes,top=0,left=0,height=" + (screen.availHeight - 60) + ",width=" + (screen.availWidth - 20);
            w_viewer = window.open('<%= url_incontextreview %>' + url, 'Viewer', style);
		}else{
			alert(data);
		}
	});
}

function openViewerWindow(url)
{
	document.getElementById("idBody").focus();
	
	var ajaxUrl = "<%=checkPageExistURL%>&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>")) +"&targetLocale="+"<%=thisTargetLocaleId%>"+ url;
	$.get(ajaxUrl,function(data){
		if(data==""){
        	if (w_viewer != null && !w_viewer.closed)
            {
                w_viewer.focus();
                return;
            }

            var style = "resizable=yes,top=0,left=0,height=" + (screen.availHeight - 60) + ",width=" + (screen.availWidth - 20);
            w_viewer = window.open('${editor.pageURL}' + url, 'Viewer', style);
		}else{
			alert(data);
		}
	});
}

function openNewViewerWindow(url)
{
	document.getElementById("idBody").focus();
	
	var ajaxUrl = "<%=checkPageExistURL%>&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>")) +"&targetLocale="+"<%=thisTargetLocaleId%>"+ url;
	$.get(ajaxUrl,function(data){
		if(data==""){
        	if (w_viewer != null && !w_viewer.closed)
            {
                w_viewer.focus();
                return;
            }

            var style = "resizable=yes,top=0,left=0,height=" + (screen.availHeight - 60) + ",width=" + (screen.availWidth - 20);
            w_viewer = window.open('${neweditor.pageURL}' + url, 'Viewer', style);
		}else{
			alert(data);
		}
	});
}

function contextForPage(url, e, displayName)
{
    if(e instanceof Object)
    {
	    e.preventDefault();
	    e.stopPropagation();
    }

    var popupoptions;

    var allowEditSource = eval('${allowEditSourcePage}');
    var canEditSource = eval('${canEditSourcePage}');
    var incontextReviewPDF = incontextReviewPDFs[displayName];
    displayName = pageNames[displayName];
    
    var fileName = displayName;
    if (fileName.match(/\)$/))
    {
    	fileName = displayName.substr(0, displayName.lastIndexOf("("));
    	if (fileName.match(/ $/))
    	{
    		fileName = fileName.substr(0, fileName.length - 1);
    	}
    }
    
    var showInContextReview = (1 == incontextReviewPDF);
    var inctxTitle = "Open In Context Review";

    if (allowEditSource)
    {
       popupoptions = [
         new ContextItem("<B><%=bundle.getString("lb_context_item_view_trans_status")%></B>",
           function(){ openViewerWindow(url);}),
         new ContextItem("<%=bundle.getString("lb_context_item_post_review_editor")%>",
           function(){ openNewViewerWindow(url);}),
         new ContextItem("<%=bundle.getString("lb_context_item_edit_src_page")%>",
           function(){ openGxmlEditor(url,"${sourceEditor.pageURL}");}, !canEditSource)
       ];
       
       if (showInContextReview)
       {
    	   popupoptions[popupoptions.length] = new ContextItem(inctxTitle,
       	        function(){ openInContextReview(url);});
       }
    }
    else
    {
       popupoptions = [
         new ContextItem("<B><%=bundle.getString("lb_context_item_view_trans_status")%></B>",
           function(){ openViewerWindow(url);}),
         new ContextItem("<%=bundle.getString("lb_context_item_post_review_editor")%>",
           function(){ openNewViewerWindow(url);})
       ];
       
       if (showInContextReview)
       {
    	   popupoptions[popupoptions.length] = new ContextItem(inctxTitle,
       	        function(){ openInContextReview(url);});
       }
    }
    
    ContextMenu.display(popupoptions, e);
}

function submitForm(){
	var url = "${addSourceFiles.pageURL}&action=canUpdateWorkFlow&jobId=${jobId}&t=" + new Date().getTime();
	$.get(url,function(data){
		if(data==""){
			window.location = "/globalsight/ControlServlet?linkName=error&pageName=WF1&jobId=${jobId}&fromDetails=true";
		}else{
			alert(data);
		}
	});
}
//jobSummary child page needed started
<amb:permission  name="<%=Permission.JOB_FILES_VIEW%>" >
$(document).ready(function(){
	$("#jobSourceFilesTab").removeClass("tableHeadingListOff");
	$("#jobSourceFilesTab").addClass("tableHeadingListOn");
	$("#jobSourceFilesTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobSourceFilesTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})
</amb:permission>

//jobSummary child page needed end.

function addSourceFiles()
{
	$.get("<%=beforeAddDeleteSourceURL%>",function(data){
		if(data==""){
			openAddSourceFilesWindow();
		}else{
			alert(data);
		}
	});
}

function openAddSourceFilesWindow()
{
	$("#addSourceDiv").dialog({width: "auto", resizable:false});
	document.getElementById('addSourceDiv').parentNode.style.display = "inline";
	document.getElementById('addSourceDiv').style.display = "inline";
	document.getElementById('appletDiv').innerHTML = '<%=appletcontent.toString()%>';
}

function removeSourceFiles()
{	
	var pIds = getSelectPageIds();
	
	if (pIds.length == 0)
	{
		alert('<%=bundle.getString("msg_no_file_remove")%>');
		return;
    }

	var obj = {
		pIds : pIds
	}
	
	$.get("<%=beforeDeleteSourceURL%>",obj,function(data){
		if(data==""){
            if (confirm('<%=bundle.getString("msg_confirm_remove")%>')) {
            	doRemoveFiles(pIds);
            }
		}
		else {
          	var returnData = eval(data);
            if (returnData.error)
            {
            	alert(returnData.error);
            }
            else if (returnData.confirm && confirm(returnData.confirm))
            {
            	doRemoveFiles(pIds);
            }
		}
	});
}

function doRemoveFiles(pIds)
{
	var  randomNum = new Date().getTime() + Math.floor(Math.random()*10000+1);
	
	$.get("<%=deleteSourceURL%>&pageIds=" + pIds + "&randomNum=" + randomNum,function(data){
		closeMsg2();
		if(data==""){
			refreshJobPage();
		}else{
          	var returnData = eval(data);
        	if (returnData.discard != null){
        		location.replace('${allStatus.pageURL}');
	        } else {
        		alert(data);
	        }
		}
	});

    showDeleteProgress(0, randomNum);
}

function getSelectPageIds()//from jobDetails.js
{
	var pIds = document.getElementsByName("pageIds");
	var selectIds = "";
	for (var i = 0; i < pIds.length; i++) {
        if (pIds[i].checked){
           if (selectIds.length > 0){
           	selectIds = selectIds.concat(",");
           }
           
           selectIds = selectIds.concat(pIds[i].value);
        }
    }

    return selectIds;
}

function refreshJobPage() {
	try {
		window.location.href = "<%=jobSourceFilesURL%>&jobId=${jobId}";
	} catch(ex) {
		location.reload(true);
	}
}

function closeMsg2() {
	$("#msgDialog2").hide();
}

function showDeleteProgress(num, randomNum)
{
	var obj = {
		number : num,
		randomNum : randomNum
	}
	
	$.get("<%=showDeleteProgressURL%>",obj,function(data){
		if(data==""){
			$("#fullbg").css("display","none");
			$("#container").css("display","none");
		} else {
			var returnData = eval(data);
			var scrollHeight = document.body.scrollHeight-120;
		    var scrollWidth = document.body.scrollWidth-40;
		    var percent = returnData.number/returnData.total;
		    $("#fullbg").css({width:scrollWidth, height:scrollHeight, display:"block"});
		    $("#container").css({top:"400px",left:"600px",display:"block"});
		    if (percent <= 1) {
				$("#updateWordCountsProgressBar").progressBar(percent*100);
				showDeleteProgress(returnData.number, randomNum);
		    } else {
		    	$("#fullbg").css("display","none");
		    	$("#container").css("display","none");
		    }
		}
	});
}

function downloadFiles(){
	var pIds = getSelectPageIds();
	if (pIds.length == 0) {
		alert('<%=bundle.getString("msg_no_file_remove")%>');
		return;
    }
    var obj = {
        pIds : pIds
    }
    
	$.get("<%=beforeDeleteSourceURL%>",obj,function(data){
		if(data==""){
			doDownloadFiles(pIds);
		} else {
        	var returnData = eval(data);
            if (returnData.error)
            {
                alert(returnData.error);
            }
            else 
            {
                doDownloadFiles(pIds);
            }
		}
	});
}

function doDownloadFiles(pIds) {
    window.location.href = '<%=downloadSourceURL%>' + "&pageIds=" + pIds;
}

function openUploadFile()
{
	$.get("<%=beforeAddDeleteSourceURL%>",function(data){
		if(data==""){
			if($.browser.msie){
				$("#uploadFormDiv").dialog({width: 700, height: 260, resizable:false});
			}else {
				$("#uploadFormDiv").dialog({width: 700, height: 150, resizable:false});
			}
		} else {
			alert(data);
		}
	});
}

function uploadFileDialog() 
{
	var  randomNum = new Date().getTime() + Math.floor(Math.random()*10000+1);
	
	var url = "<%=uploadSourceURL%>&randomNum=" + randomNum;
	$("#uploadForm").attr("action",url);
	$("#uploadForm").submit();
	$("#uploadFormDiv").dialog('close');
	showUpdateProgress(0, randomNum);
}

function popupUploadErrorMessage()
{
	var popupDiv = document.getElementById("ajaxUpload").contentWindow.document.getElementById("uploadFileErroInfo");
	if($.browser.msie){
		$(popupDiv).dialog({width:700,height:300,title:'Message',resizable:false,buttons:{'Close':function(){$(this).dialog('close');}}});
	} else {
		$(popupDiv).dialog({width:700,height:250,title:'Message',resizable:false,buttons:{'Close':function(){$(this).dialog('close');}}});
	}
}

function showUpdateProgress(num, randomNum)
{
	var obj = {
		number : num,
		randomNum : randomNum
	}
	
	$.get("<%=showUpdateProgressURL%>",obj,function(data){
		if(data==""){
			$("#fullbg").css("display","none");
			$("#container").css("display","none");
		} else {
			var returnData = eval(data);
			var scrollHeight = document.body.scrollHeight-120;
		    var scrollWidth = document.body.scrollWidth-40;
		    var percent = returnData.number/returnData.total;
		    $("#fullbg").css({width:scrollWidth, height:scrollHeight, display:"block"});
		    $("#container").css({top:"400px",left:"600px",display:"block"});
		    if (percent <= 1) {
				$("#updateWordCountsProgressBar").progressBar(percent*100);
				showUpdateProgress(returnData.number, randomNum);
		    } else {
		    	$("#fullbg").css("display","none");
		    	$("#container").css("display","none");
		    }
		}
	});
}

function closeDialog(){
	document.getElementById('addSourceDiv').parentNode.style.display = "none";
}

<%
JobSourcePageDisplay jobSourcePageDisplay = null;
for (int i = 0; i < jobSourcePageDisplayList.size(); i++)
{
	jobSourcePageDisplay = jobSourcePageDisplayList.get(i);
	String pageName = jobSourcePageDisplay.getSourcePage().getDisplayPageName().replace("\\","/");
	FileProfile fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(jobSourcePageDisplay.getSourcePage().getRequest().getDataSourceId());
	
	String pageNameLow = jobSourcePageDisplay.getSourcePage().getExternalPageId().toLowerCase();
    boolean isXml = pageNameLow.endsWith(".xml");
    boolean isInDesign = pageNameLow.endsWith(".indd") || pageNameLow.endsWith(".idml");
    boolean isOffice = pageNameLow.endsWith(".docx") || pageNameLow.endsWith(".pptx") || pageNameLow.endsWith(".xlsx");
    
    boolean enableInContextReivew = false;
    if (isXml)
    {
        enableInContextReivew = okForInContextReviewXml ? FileProfileUtil.isXmlPreviewPDF(fp) : false;
    }
    if (isInDesign)
    {
        enableInContextReivew = okForInContextReviewIndd;
    }
    if (isOffice)
    {
        enableInContextReivew = okForInContextReviewOffice;
    }
    {%>
       	pageNames[<%=i%>] = "<%=pageName%>";
       	incontextReviewPDFs[<%=i%>] = <%=(enableInContextReivew ? 1 : 0 )%>;
  <%}
}
%>
</script>
</body>
</html>
