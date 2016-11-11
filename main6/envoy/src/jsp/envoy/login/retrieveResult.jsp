<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.HashMap,
            com.globalsight.everest.webapp.WebAppConstants" session="false" %>

<jsp:useBean id="dummyNavigationBean" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<% 
  // this object will be null for a login.  Otherwise, it's a login failed page.
  String failed = (String)request.getAttribute("header");
  String isJustUiLocale = request.getParameter("isJustChangeUILocale");
  if ("True".equalsIgnoreCase(isJustUiLocale))
  {
      failed = null;
  }

  String[] supportedLocales = (String[])
      request.getAttribute(SystemConfigParamNames.UI_LOCALES);
  String defaultLocale = (String)
      request.getAttribute(SystemConfigParamNames.DEFAULT_UI_LOCALE);
  // get last uilocale from cookie 
  String cookieUiLocale = "";
  Cookie[] cookies = request.getCookies();
  if (cookies != null)
  {
	  for(int i=0; i<cookies.length; i++)
	  {
	      Cookie cookie = cookies[i];
	      if ("localelang".equals(cookie.getName()))
	          cookieUiLocale = cookie.getValue();
	  }
  }
  if (supportedLocales!=null 
		  &&!Arrays.asList(supportedLocales).contains(cookieUiLocale))
      cookieUiLocale = defaultLocale;
  if (cookieUiLocale.equals(""))
      cookieUiLocale = defaultLocale;
  
  ResourceBundle bundle = PageHandler.getBundleByLocale(cookieUiLocale);
  // For "login directly" issue
  String userId = "";
  String forwardUrl = "";
  String from = request.getParameter(WebAppConstants.LOGIN_FROM) != null ? request.getParameter(WebAppConstants.LOGIN_FROM) : "";
  if(from != null && from.length() > 0)
  {
     userId = request.getParameter("nameField") != null ? request.getParameter("nameField") : "";
     forwardUrl = request.getParameter("forwardUrl") != null ? request.getParameter("forwardUrl") : "";
  }
  
  HashMap<String,String> map = new HashMap<String,String>(5);
  int numOfLocales = supportedLocales == null ? 0 : supportedLocales.length;
  if (numOfLocales >= 1)
  {
      for (int i=0; i<numOfLocales; i++)
      {
          String value = supportedLocales[i];
          Locale l = com.globalsight.util.GlobalSightLocale.makeLocaleFromString(value);
          String longname = l.getDisplayName(l);
          int indexOfMrow = longname.indexOf("(");
          map.put(value, longname);
      }
  }
  
  // links to be displayed... If only one locale, don't display any.
  String uiLinks = "";
  String spacer = "&nbsp;&nbsp;&nbsp;&nbsp;";
  
  if (numOfLocales > 1)
  {      
     StringBuffer links = new StringBuffer();
     links.append("<P><HR SIZE=1 WIDTH=300>");
     int added = 0;
     for (int i=0; i<numOfLocales; i++)
     {
         Object value = supportedLocales[i];
         if (value != null)
         {
            added++;
            if (i != 0)
            {
                links.append(spacer);
            }
            links.append("<A CLASS=\"standardHref\"  HREF=\"javascript:changeLanguage('"+value+"');\" onFocus=\"this.blur();\">");
            links.append(map.get(value));
            links.append("</A>");
         }
     }
     if (added < 2)
        uiLinks = "";
     else
        uiLinks = links.toString();
  }
  
  String res_loginHeader = null;
  String res_header   = bundle.getString("msg_welcome_1");
  String res_aboutUrl = bundle.getString("lb_about_globalsight_system4");
  String res_userName = bundle.getString("lb_user_name");
  String res_pwd      = bundle.getString("lb_password");
  String res_login    = bundle.getString("lb_login");
  
  // locale independent variables
  String title= "GlobalSight";
  String aboutUrl = "/globalsight/envoy/about/about.jsp";
  String headerStyle = "standardTextBoldLarge";
  
  /*if (failed == null)
  {       
     res_loginHeader = bundle.getString("lb_login");
     res_header = bundle.getString("msg_welcome_1");     
     headerStyle = "standardTextBoldLarge";
  }
  else
  {
      res_loginHeader = bundle.getString("lb_login") + " " + bundle.getString("msg_failed");            
      headerStyle = "warningText";

      if (failed.equals("invalidUser")) 
      {
          res_header = bundle.getString("msg_login_fail");
      }
      else if ("generalFail".equals(failed))// general failure
      {
          // this is for general failure or duplicate concurrent log ins.
          res_header = bundle.getString("msg_login_fail_general");
      }
      else // invalid customer installation key
      {
         res_header = failed;
      }
  }*/

String logoImage = skin.getProperty("skin.banner.logoImage");
String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
boolean useOneLogoImage = false;
if (logoImage.equals(logoBackgroundImage))
    useOneLogoImage = true;

//res_header = "Forgot Password";
//String strRetriverId = "If you have forgotten your username you can retrieve it";
//String strHere = "here";
%>
                
<HTML>
<HEAD>

    <style type="text/css">
	<!--
		body{ background: url(images/page_bg.png) no-repeat; }
		span{ padding:10px; white-space:nowrap }
		
		.darkGreenText
		{
    		font-family: Arial, Helvetica, sans-serif;
    		font-size: 10pt;
    		color:darkgreen;
		}
	-->
	</style>

    <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
    <TITLE><%=title %></TITLE>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
    <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
    <SCRIPT type="text/javascript">
    function getLangCookie() 
    {
       if (document.cookie.length > 0)
       {
           offset = document.cookie.indexOf("localelang");
           if (offset != -1)
           {
               offset += 11;
               end = document.cookie.indexOf(";", offset);
               if (end == -1)
               {
                   end = document.cookie.length;
               }
               var languageSelected = unescape(document.cookie.substring(offset, end));
           }
       }
       if (languageSelected == null) return "-1";
       else return languageSelected;
     }
      
    function setLangCookie(langSent) {
        var today = new Date();
        var expires = new Date(today.getTime() + (365 * 86400000));
        document.cookie = "localelang=" + langSent + ";EXPIRES=" + expires.toGMTString() + ";PATH=" + escape("/");
    }

    function confirmForm(formSent) {
       var locale = formSent.uiLocale.value;
       // Make sure a name has been given
       if (isEmptyString(formSent.nameField.value))
       {
          displayNameMessage();
          formSent.nameField.value = "";
          formSent.nameField.focus();
          return false;
       }

       // Make sure the password has been entered
       if (isEmptyString(formSent.passwordField.value))
       {
          displayPwdMessage();
          formSent.passwordField.value = "";
          formSent.passwordField.focus();
          return false;
       }

       return true;
    }

  function displayNameMessage()
  {
     alert("<%=bundle.getString("jsmsg_login_name") %>");
     
     return true;
  }

  function displayPwdMessage()
  {
     alert("<%=bundle.getString("jsmsg_login_password") %>");
        
     return true;
  }

  function changeLanguage(strLocale)
  {
	  setLangCookie(strLocale);
	  loginForm.uiLocale.value = strLocale;
	  loginForm.isJustChangeUILocale.value = "True";
	  loginForm.submit();
  }
  
  function init()
  {
    var selectedLang = getLangCookie();
    var cookieUiLocaleFromJava = "<%=cookieUiLocale %>";
    
    if( selectedLang != cookieUiLocaleFromJava )
    {
        selectedLang=cookieUiLocaleFromJava;
        setLangCookie(selectedLang);
    }
  }
  
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
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>

    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        onLoad="init();">

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
            <TD ALIGN="RIGHT"></TD>
        </TR>
        <TR>
            <TD COLSPAN="3" CLASS="header2" HEIGHT="20" ALIGN="RIGHT"><A CLASS="header2" HREF="#" onClick="javascript:aboutWindow = window.open('<%=aboutUrl%>','about','HEIGHT=350,WIDTH=460,scrollbars'); return false;">
                <%= res_aboutUrl %></A>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        </TABLE>
    </DIV>
  
    <!-- "Login Form" info -->
    <DIV ALIGN="CENTER" ID="contentLayer1" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 150px; LEFT: 0px;">
     <FORM NAME="loginForm" ACTION="<%= dummyNavigationBean.getPageURL() %>" METHOD="post" onSubmit="return confirmForm(this)">
      <input type="hidden" name="uiLocale" value="<%=cookieUiLocale %>" />
      
      <TABLE NAME="loginTable" id="loginTable" CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
       <TR>
       <TD ALIGN="CENTER">
        <SPAN CLASS="<%= headerStyle %>">
        <%=res_header%>
        </SPAN>
       </TD>
       </TR>
       <TR><TD>&nbsp;</TD></TR>
       
       <TR>
        <TD ALIGN="center">    
        <TABLE CELLPADDING="0" CELLSPACING="2" BORDER="0"> 
        <TR align="left" style="height:20px;">
        	<SPAN CLASS="darkGreenText">
        	<%=request.getAttribute("mess") %><br/>
        	</SPAN>
        </TR>  
        <TR>&nbsp;</TR>
        
        </TABLE>
        
        </TD>
       </TR>
      </TABLE>
     </FORM>

	 <P CLASS="standardText"><A CLASS="standardHREF" HREF="/globalsight">Click here to return to GlobalSight.</A></P>

    </DIV>
    
    <DIV ID="restart" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 260px">
        <%@ include file="/envoy/common/restartBanner.jspIncl" %>    
    </DIV>
    <DIV ID="shutdown" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 260px">
<%@ include file="/envoy/common/shutdownBanner.jspIncl" %>    
    </DIV>   
    </BODY>
</HTML>
