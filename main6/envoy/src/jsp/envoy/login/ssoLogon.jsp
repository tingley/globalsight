<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,
	com.globalsight.everest.webapp.javabean.NavigationBean,
	com.globalsight.everest.util.system.SystemConfigParamNames,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	java.util.HashMap,com.globalsight.everest.webapp.WebAppConstants,
	java.util.ResourceBundle"
	session="true"%>

<jsp:useBean id="skin" scope="application"
	class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pass" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String title = bundle.getString("lb_sso_gs");
  String ssoIdpUrl = (String) request.getAttribute("ssoIdpUrl");
  boolean sendToIdp = Boolean.parseBoolean((String) request.getAttribute("sentToIdp"));
  String ssoAssertion = (String) request.getAttribute("ssoAssertion");
  
  String logoImage = skin.getProperty("skin.banner.logoImage");
  String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
  boolean useOneLogoImage = false;
  if (logoImage.equals(logoBackgroundImage))
      useOneLogoImage = true;
%>

<HTML>
<HEAD>

<style>
body {
	background: url(images/page_bg.png) no-repeat;
}
</style>

<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<script type="text/javascript"
	src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT type="text/javascript">
function init() {
	<%
	    if (sendToIdp) {
	%>
	var msgEle = document.getElementById("msg");
	msgEle.innerHTML = "<%= bundle.getString("msg_sso_wait_auth") %>";
	//setTimeout("submitDataToIdp()", "1000");
	submitDataToIdp();
	<%
	    }
	%>
	}

function submitDataToIdp() { ssoForm.submit(); }

$(document).ready(function(){
	var width;
	width = $(window).width();
	$("#logoTable").css("width",width);
	$("#loginTable").css("width",width);

	$(window).resize(function(e){
		width = $(window).width();
		$("#logoTable").css("width",width);
		$("#loginTable").css("width",width);
	});
	
  });
</SCRIPT>


</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" onLoad="init();">
	
	<!-- Header info -->
    <DIV ID="header0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
    <TABLE  NAME="logoTable" id="logoTable" WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
        <TR CLASS="header1">
        <% if (useOneLogoImage == true){ %>
            <TD WIDTH="960"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="960"></TD>
            <%} else {%>
            <TD WIDTH="285"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="285"></TD>
            <TD WIDTH="675"><IMG SRC="<%=logoBackgroundImage%>" HEIGHT="68" WIDTH="675"></TD>
            <%}%>            
            <TD ALIGN="RIGHT">
                <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
                    <TR>
                        <TD CLASS="header1" ALIGN="right"></TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD COLSPAN="3" CLASS="header2" HEIGHT="20" ALIGN="RIGHT"></TD>
        </TR>
    </TABLE>
    </DIV>
    
    <DIV ALIGN="CENTER" ID="contentLayer1" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 100px; LEFT: 20px;">
	<span id="msg" CLASS="standardText"></span>
	<FORM NAME="ssoForm" ACTION="<%=ssoIdpUrl%>" METHOD="post">
	<input type="hidden" name="ssoRequestData" value="<%=ssoAssertion%>" />
	</FORM>
	</DIV>
</BODY>
</HTML>
