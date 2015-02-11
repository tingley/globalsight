<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.ling.common.URLDecoder,
            com.globalsight.util.Base64,
            com.globalsight.util.FormUtil,
            java.util.HashMap,
            com.globalsight.everest.webapp.WebAppConstants"
    session="false"%>
<jsp:useBean id="dummyNavigationBean" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<%
	// this object will be null for a login.  Otherwise, it's a login failed page.
  String failed = (String)request.getAttribute("header");
  String isJustUiLocale = request.getParameter("isJustChangeUILocale");
  if ("True".equalsIgnoreCase(isJustUiLocale))
  {
      failed = null;
  }

  String autoLoginCookie = "";
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
	      String cookieName = cookie.getName();
	      if ("localelang".equals(cookieName))
	          cookieUiLocale = cookie.getValue();
	      if("autoLogin".equals(cookieName))
	          autoLoginCookie = cookie.getValue();
	  }
  }
  if (!Arrays.asList(supportedLocales).contains(cookieUiLocale))
      cookieUiLocale = defaultLocale;
  if (cookieUiLocale.equals(""))
      cookieUiLocale = defaultLocale;
  
  ResourceBundle bundle = PageHandler.getBundleByLocale(cookieUiLocale);
  
  String userName = "";
  String passWord = "";
  boolean isSSO = false;
  
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
  String res_header = null;
  String res_aboutUrl = bundle.getString("lb_about_globalsight_system4");
  String res_userName = bundle.getString("lb_user_name");
  String res_pwd      = bundle.getString("lb_password");
  String res_login    = bundle.getString("lb_login");
  String res_forgPass = bundle.getString("lb_login_link_resetPass").trim();
  String res_forgUser = bundle.getString("lb_login_link_retrieveUsername").trim();
  String res_isSso    = bundle.getString("lb_sso_IsSSOAccount");
  String res_ssoIdpUrl= bundle.getString("lb_sso_IdpUrl");
 
  // locale independent variables
  String title= "GlobalSight";
  String aboutUrl = "/globalsight/envoy/about/about.jsp";
  String headerStyle = null;
  
  if (failed == null)
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
      else if ("notALogin".equals(failed)) // wasn't a login attempt, just loading page
      {
          res_header = bundle.getString("msg_login_1");
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
  }

String logoImage = skin.getProperty("skin.banner.logoImage");
String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
boolean useOneLogoImage = false;
if (logoImage.equals(logoBackgroundImage))
    useOneLogoImage = true;

if(autoLoginCookie != null && autoLoginCookie.trim().length() > 0)
{
    int index = autoLoginCookie.indexOf("|");
    if(index > 0)
    {
    	userName = autoLoginCookie.substring(0, index);
    	passWord = autoLoginCookie.substring(index+1);
    	passWord = URLDecoder.decode(passWord);
    	passWord = Base64.decodeToString(passWord);
    }
}

String forwardUrl = "";
String from = request.getParameter(WebAppConstants.LOGIN_FROM) != null ? request.getParameter(WebAppConstants.LOGIN_FROM) : "";
if(from != null && from.length() > 0)
{
   userName = request.getParameter("nameField") != null ? request.getParameter("nameField") : "";
   forwardUrl = request.getParameter("forwardUrl") != null ? request.getParameter("forwardUrl") : "";
   String enableSso = request.getParameter("isSSO");
   if (enableSso != null && enableSso.equalsIgnoreCase("on"))
   {
       isSSO = true;
   }
}

request.setAttribute("forwardUrl", forwardUrl);
request.setAttribute("userName", userName);
request.setAttribute("password", passWord);
request.setAttribute("cookieUiLocale", cookieUiLocale);
request.setAttribute("from", from);
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
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT TYPE="text/javascript">
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
     alert("<%=bundle.getString("jsmsg_login_name")%>");
     
     return true;
  }

  function displayPwdMessage()
  {
     alert("<%=bundle.getString("jsmsg_login_password")%>");

		return true;
	}

	function changeLanguage(strLocale) {
		setLangCookie(strLocale);
		loginForm.uiLocale.value = strLocale;
		loginForm.isJustChangeUILocale.value = "True";
		loginForm.submit();
	}

	function init() {
		var selectedLang = getLangCookie();
		var cookieUiLocaleFromJava = "<c:out value="${cookieUiLocale}"/>";

		if (selectedLang != cookieUiLocaleFromJava) {
			selectedLang = cookieUiLocaleFromJava;
			setLangCookie(selectedLang);
		}
		document.loginForm.isJustChangeUILocale.value = "false";
		var elemSSO = document.loginForm.isSSO;
		if (elemSSO != null) {
			elemSSO.checked =<%=isSSO%>;
		}
	}

	function switchIsSSO() {
		var ele = document.getElementById("ssoIdpUrlCC");
		var display = document.loginForm.isSSO.checked ? "" : "none";
		ele.style.display = display;
	}
	
	function onChangeName() {
		if(loginForm.nameField.value!='<c:out value="${userName}"/>'){
			document.getElementById('<%=WebAppConstants.LOGIN_FROM%>').value=''
		}
		else{
			document.getElementById('<%=WebAppConstants.LOGIN_FROM%>').value = '<c:out value="${from}"/>'
		}
	}

	$(document).ready(function() {
		var width = $(window).width();

		$("#logoTable").css("width", width);
		$("#navigationTable").css("width", width);
		$("#loginTable").css("width", width);
		$(window).resize(function(e) {
			width = $(window).width();
			$("#logoTable").css("width", width);
			$("#navigationTable").css("width", width);
			$("#loginTable").css("width", width);
		});

	});
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onLoad="init();">
    <!-- Header info -->
    <DIV ID="header0" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
        <TABLE NAME="logoTable" id="logoTable" WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR CLASS="header1">
                <%
                	if (useOneLogoImage == true) {
                %>
                <TD WIDTH="960">
                    <IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="960">
                </TD>
                <%
                	} else {
                %>
                <TD WIDTH="285">
                    <IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="285">
                </TD>
                <TD>
                    <IMG SRC="<%=logoBackgroundImage%>" HEIGHT="68" WIDTH="675">
                </TD>
                <%
                	}
                %>
            </TR>
        </TABLE>
    </DIV>
    <DIV ID="" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 68px; LEFT: 0px;">
        <TABLE id="navigationTable" WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR>
                <TD CLASS="header2" HEIGHT="20" ALIGN="RIGHT">
                    <A CLASS="header2" HREF="#" onClick="javascript:aboutWindow = window.open('<%=aboutUrl%>','about','HEIGHT=350,WIDTH=460,scrollbars'); return false;">
                        <%=res_aboutUrl%>
                    </A>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </TD>
            </TR>
        </TABLE>
    </DIV>
    <!-- "Login Form" info -->
    <DIV ALIGN="CENTER" ID="contentLayer1" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 150px; LEFT: 0px;">
        <FORM NAME="loginForm" ACTION="<%=dummyNavigationBean.getPageURL()%>" METHOD="post" onSubmit="return confirmForm(this)">
            <TABLE NAME="loginTable" id="loginTable" CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
                <TR>
                    <TD ALIGN="CENTER">
                        <SPAN CLASS="<%=headerStyle%>">
                            <%=res_header%>
                        </SPAN>
                    </TD>
                </TR>
                <TR>
                    <TD>&nbsp;</TD>
                </TR>
                <TR>
                    <TD ALIGN="CENTER">
                        <TABLE CELLPADDING="0" CELLSPACING="2" BORDER="0">
                            <TR>
                                <TD ALIGN="RIGHT">
                                    <SPAN CLASS="standardText">
                                        <%=res_userName%>:
                                    </SPAN>
                                </TD>
                                <TD>
                                    <SPAN CLASS="standardText">
                                        <input type="text" name="nameField" size="18" tabIndex="1" style="width: 145px" value="<c:out value="${userName}"/>"
                                            onBlur="onChangeName()" />
                                    </SPAN>
                                    <input type="hidden" name="uiLocale" value="<c:out value="${cookieUiLocale}"/>" />
                                    <input type="hidden" name="isJustChangeUILocale" value="False" />
                                </TD>
                            </TR>
                            <TR>
                                <TD ALIGN="RIGHT">
                                    <SPAN CLASS="standardText">
                                        <%=res_pwd%>:
                                    </SPAN>
                                </TD>
                                <TD>
                                    <SPAN CLASS="standardText">
                                        <input autocomplete="off" type="password" name="passwordField" size="18" tabIndex="2" style="width: 145px" value="<c:out value="${password}"/>" />
                                    </SPAN>
                                </TD>
                            </TR>
                            <TR id="ssoIdpUrlCC" style="display: none">
                                <TD ALIGN="RIGHT">
                                    <SPAN CLASS="standardText">
                                        <%=res_ssoIdpUrl%>:
                                    </SPAN>
                                </TD>
                                <TD>
                                    <SPAN CLASS="standardText">
                                        <input type="text" name="ssoIdpUrlField" size="18" tabIndex="2" style="width: 145px" value="<c:out value="${passWord}"/>" />
                                    </SPAN>
                                </TD>
                            </TR>
                        </TABLE>
                        <input type="hidden" id="<%=WebAppConstants.LOGIN_FROM%>" name="<%=WebAppConstants.LOGIN_FROM%>" value="<c:out value="${from}"/>" />
                        <input type="hidden" id="forwardUrl" name="forwardUrl" value="<c:out value="${forwardUrl}"/>" />
                        <P></P>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <SPAN CLASS="standardText">
                            <INPUT type="SUBMIT" VALUE="<%=res_login%>" tabIndex="3" NAME="login0" />
                        </SPAN>
                        &nbsp;
                        <%
                        	if (Boolean.valueOf(request.getAttribute(
                        			SystemConfigParamNames.ENABLE_SSO).toString())) {
                        %>
                        <SPAN CLASS="standardText">
                            <input type="checkbox" name="isSSO" tabIndex="3" onclick="switchIsSSO()" />
                        </SPAN>
                        <SPAN CLASS="standardText">
                            <%=res_isSso%>
                        </SPAN>
                        <%
                        	}
                        %>
                        <P></P>
                        <input type="hidden" id="retrieveType">
                        <a href="/globalsight/ControlServlet?linkName=resetPass&pageName=LOG1" tabIndex="4" class="standardHref"><%=res_forgPass%></a>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <a href="/globalsight/ControlServlet?linkName=retrieveUsername&pageName=LOG1" tabIndex="5" class="standardHref"><%=res_forgUser%></a>
                        <P></P>
                        <%=uiLinks%>
                    </TD>
                </TR>
            </TABLE>
            <input type="hidden" name="token.login" value="<%=request.getAttribute("token.login")%>" />
        </FORM>
    </DIV>
    <DIV ID="shutdown" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 260px">
        <%@ include file="/envoy/common/shutdownBanner.jspIncl"%>
    </DIV>
    <link rel="shortcut icon" href="/globalsight/images/GlobalSight_Icon.ico">
</BODY>
</HTML>
