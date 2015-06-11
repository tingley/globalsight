<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,com.globalsight.util.resourcebundle.ResourceBundleConstants,com.globalsight.util.resourcebundle.SystemResourceBundle,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.util.system.SystemConfiguration,com.globalsight.everest.util.system.SystemConfigParamNames,com.globalsight.everest.webapp.pagehandler.PageHandler,java.util.Locale,java.util.ResourceBundle"
	session="true"%>

<jsp:useBean id="next3"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="cancel3"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="pre3"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String next3URL = next3.getPageURL();
	String cancel3URL = cancel3.getPageURL();
	String pre3URL = pre3.getPageURL();
	String title = bundle.getString("lb_db_column_profiles");

	SystemConfiguration sysConfig = SystemConfiguration.getInstance();
	boolean useSSL = sysConfig
			.getBooleanParameter(SystemConfigParamNames.USE_SSL);
	String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	if (useSSL == true) {
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
	} else {
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	}
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_db_profile")%>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_columns")%>";

function cancelForm()
{
   profileCancel.submit();
}
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" ONLOAD="loadGuides()">
	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<DIV ID="contentLayer"
		STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px">

		<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
			<TR>
				<TD ALIGN="<%=gridAlignment%>"><SPAN CLASS="mainHeading">
						<%=title%> </SPAN>

					<P>
					<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
						<TR>
							<TD WIDTH=538><%=bundle.getString("helper_text_db_profile_column")%>
							</TD>
						</TR>
					</TABLE>
					<P>
					<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
						<TR>
							<TD>
								<%
									boolean isIE = request.getHeader("User-Agent").indexOf("MSIE") != -1;
									boolean isFirefox = request.getHeader("User-Agent").indexOf(
											"Firefox") != -1;
								%> <%
 	if (isIE) {
 %> <OBJECT
									classid="clsid:CAFEEFAC-0018-0000-0045-ABCDEFFEDCBA" WIDTH="538"
									HEIGHT="385" NAME="GPUI" id="GPUI"
									codebase="<%=httpProtocolToUse%>://javadl.sun.com/webapps/download/AutoDL?BundleId=107109">
									<%
										} else {
									%>
									<%=bundle.getString("applet_need_java_support")%>
									<BR>
									<APPLET type="application/x-java-applet;jpi-version=1.8.0_45"
										id="GPUI" height="385" width="538"
										pluginspage="<%=httpProtocolToUse%>://www.java.com/en/download/manual.jsp">
										<%
											}
										%>
										<PARAM NAME=CODE
											VALUE="com.globalsight.everest.webapp.applet.admin.dbprofile.DBColumnApplet.class">
										<!--PARAM NAME = CODEBASE VALUE = "classes/"-->

										<PARAM NAME="cache_option" VALUE="Plugin">
										<PARAM NAME="cache_archive"
											VALUE="/globalsight/applet/lib/dbProfile.jar">
										<param name="rand"
											value=<%=session.getAttribute("UID_" + session.getId())%>>
										<param name="servletUrl"
											value="/globalsight/ControlServlet?linkName=next2&pageName=DBPN1&applet=true&rand=">
										<param name="grid"
											value="com.globalsight.everest.webapp.applet.admin.dbprofile.DBColumnPanel">
										<param name=next3URL
											value="<%=next3URL%>&applet=yes&initial=true&rand=">
										<param name=cancel3URL
											value="<%=cancel3URL%>&Cancel=Cancel&applet=yes&initial=true&rand=">
										<param name=pre3URL
											value="<%=pre3URL%>&applet=yes&initial=true&rand=">
										<%
											if (isIE) {
										%>
									
								</OBJECT> <%
 	} else {
 %> </APPLET> <%
 	}
 %>
							</TD>
						</TR>
					</TABLE></TD>
			</TR>
		</TABLE>
	</DIV>
</BODY>
</HTML>

