<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            java.util.HashMap" session="false" %>

<jsp:useBean id="dummyNavigationBean" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<jsp:useBean id="companyNames" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="pass" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
  String[] supportedLocales = (String[])
      request.getAttribute(SystemConfigParamNames.UI_LOCALES);
  String defaultLocale = (String)
      request.getAttribute(SystemConfigParamNames.DEFAULT_UI_LOCALE);
  
  HashMap map = new HashMap(5);
  map.put("en_US", "0");
  map.put("0", "English");
  
  // links to be displayed... If only one locale, don't display any.
  int numOfLocales = supportedLocales == null ? 0 : supportedLocales.length;
  String uiLinks = "";
  String spacer = "&nbsp;&nbsp;&nbsp;&nbsp;";
  
  if (numOfLocales > 1)
  {      
     StringBuffer links = new StringBuffer();
     links.append("<P><HR SIZE=1 WIDTH=300>");
     int added = 0;
     for (int i=0; i<numOfLocales; i++)
     {
         Object value = map.get(supportedLocales[i]);
         if (value != null)
         {
            added++;
            if (i != 0)
            {
                links.append(spacer);
            }
            links.append("<A CLASS=\"standardHref\"  HREF=\"javascript:changeLanguage("+value+");\" onFocus=\"this.blur();\">");
            links.append(map.get(value));
            links.append("</A>");
         }                  
     }
     if (added < 2)
        uiLinks = "";
     else
        uiLinks = links.toString();
  }
  
  int defaultLocaleIndex = -1;
  try
  {
      String str = (String)map.get(defaultLocale);
      if (str != null)
      {
         defaultLocaleIndex = Integer.parseInt(str);
      }
  }
  catch(Exception e)
  {
  }
  
  //en_US
  String en_loginHeader = null;
  String en_header = null;
  String en_aboutUrl = "About GlobalSight"; 
  
  // locale independent variables
  String title= "GlobalSight";
  String aboutUrl = "/globalsight/envoy/about/about.jsp";
  String headerStyle = null;
       
  en_loginHeader = "Choosing Company For Super PM";
  en_header = "Welcome to GlobalSight";  
  headerStyle = "standardTextBoldLarge";

String logoImage = skin.getProperty("skin.banner.logoImage");
String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
boolean useOneLogoImage = false;
if (logoImage.equals(logoBackgroundImage))
    useOneLogoImage = true;
%>
                
<HTML>
<HEAD>
    <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
    <TITLE><%=title %></TITLE>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
    <SCRIPT LANGUAGE="JavaScript">
    {                    
      var currentUI = 0;      
    }

    function getLangCookie() 
    {
       if (document.cookie.length > 0)
       {
           offset = document.cookie.indexOf("lang");
           if (offset != -1)
           {
               offset += 5;
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
        document.cookie = "lang=" + langSent + ";EXPIRES=" + expires.toGMTString() + ";PATH=" + escape("/");
    }

    function confirmFormEN(formSent) {
       var locale = formSent.uiLocale.value;
       // Make sure a name has been given
       if (isEmptyString(formSent.nameField.value))
       {
          displayNameMessage(locale);
          formSent.nameField.value = "";
          formSent.nameField.focus();
          return false;
       }

       // Make sure the password has been entered
       if (isEmptyString(formSent.passwordField.value))
       {
          displayPwdMessage(locale);
          formSent.passwordField.value = "";
          formSent.passwordField.focus();
          return false;
       }

       return true;
    }

  function displayNameMessage(uilocale)
  {
     alert("You must enter a username to log in.");
     return true;
  }

  function displayPwdMessage(uilocale)
  {
     alert("You must enter a password to log in.");
     return true;
  }

  function changeLanguage(IDSent)
  {
    if (document.layers) 
    {
        eval("document.header" + currentUI + ".visibility = 'hide'");
        eval("document.header" + IDSent + ".visibility = 'show'");

        eval("document.contentLayer" + currentUI + ".visibility = 'hide'");
        eval("document.contentLayer" + IDSent + ".visibility = 'show'");
        eval("document.contentLayer" + IDSent + ".document.loginForm.nameField.focus()");        
    }
    else
    {
        eval("header" + currentUI + ".style.visibility = 'hidden'");
        eval("header" + IDSent + ".style.visibility = 'visible'");

        eval("contentLayer" + currentUI + ".style.visibility = 'hidden'");
        eval("contentLayer" + IDSent + ".style.visibility = 'visible'");
//        eval("contentLayer" + IDSent + ".all.nameField.focus()");        
    }
    currentUI = IDSent;
    setLangCookie(IDSent);
  }
  
  function init()
  {
	var selectedLang = getLangCookie();
	if( selectedLang == -1 ) {
		selectedLang=0;
		setLangCookie(0);
	}
    
    // only change it the first time when no cookie was installed
    if (selectedLang == -1)
    {
        selectedLang = <%=defaultLocaleIndex %>;
        changeLanguage(selectedLang);    
    }
    
    //currentUI = selectedLang;
    selectedLang = currentUI;
    
    if (document.layers)
    {
        eval("document.header" + selectedLang + ".visibility = 'show'");

        eval("document.contentLayer" + selectedLang + ".visibility = 'show'");
        eval("document.contentLayer" + selectedLang + ".document.loginForm.nameField.focus()");
    }
    else
    {
        eval("header" + selectedLang + ".style.visibility = 'visible'");
        eval("contentLayer" + selectedLang + ".style.visibility = 'visible'");
//        eval("contentLayer" + selectedLang + ".all.nameField.focus()");
    }    
  }
    </SCRIPT>


</HEAD>

    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onLoad="init();">
        
    <!-- Header info for en_US (0) -->
    <DIV ID="header0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px; VISIBILITY: HIDDEN; width:100%;">
    <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
        <TR CLASS="header1">
        <% if (useOneLogoImage == true){ %>
            <TD WIDTH="704"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="704"></TD>
            <%} else {%>
            <TD WIDTH="253"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="253"></TD>
            <TD WIDTH="451"><IMG SRC="<%=logoBackgroundImage%>" HEIGHT="68" WIDTH="451"></TD>
            <%}%>            
            <TD ALIGN="RIGHT">
                <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
                    <TR>
                        <TD CLASS="header1" ALIGN="right"><%= en_loginHeader %>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD COLSPAN="3" CLASS="header2" HEIGHT="20" ALIGN="RIGHT"><A CLASS="header2" HREF="#" onClick="javascript:aboutWindow = window.open('<%=aboutUrl%>','about','HEIGHT=350,WIDTH=450,scrollbars'); return false;">
                <%= en_aboutUrl %></A>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        </TABLE>
    </DIV>     
    
    <!-- "Login Form" info for en_US (layer 0) -->
    <DIV ALIGN="CENTER" ID="contentLayer0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 150px; LEFT: 0px; VISIBILITY: HIDDEN; width:100%;">
        <!-- <A HREF="javascript:changeLanguage(0);" onFocus="this.blur();"><IMG SRC="/globalsight/images/english.gif" BORDER="0"></A><A HREF="javascript:changeLanguage(3);" onFocus="this.blur();"><IMG SRC="/globalsight/images/spanish.gif" BORDER="0"></A><P> -->

       <FORM NAME="chooseCompanyForm" ACTION="<%= pass.getPageURL() %>" METHOD="post">
       
       <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
       <TR>
       <TD ALIGN="CENTER">    
        <SPAN CLASS="<%= headerStyle %>">
        <%=en_header%>
        </SPAN>
       </TD>
       </TR>
       <TR> <TD>&nbsp; </TD> </TR>
       <TR>
       <TD ALIGN="CENTER">    
       <TABLE CELLPADDING="0" CELLSPACING="2" BORDER="0">    
       <TR>
           <TD>
               <SPAN CLASS="standardText">
               Company List:
               </SPAN>
           </TD>
           <TD>
               <SPAN CLASS="formFields">
               <select name="companyName" tabIndex="1">
<% for (int i = 0; i < companyNames.size(); i++) { %>                  
                 <option value="<%=companyNames.get(i).toString()%>" <%=i==0?"selected":""%> ><%=companyNames.get(i).toString()%></option>
<% } %>                
               </select>
               </SPAN>
               <input type="hidden" name="uiLocale" value="en_US">
           </TD>
       </TR>
    </TABLE>
    <P>
    <SPAN CLASS="standardText">
    <INPUT type="SUBMIT" VALUE="Submit" tabIndex="3" NAME="login0">
    </SPAN>
    <%=uiLinks%>
    </TD>
    </TR>
    </TABLE>
    </FORM>
    </DIV>
    
    
    <DIV ID="shutdown" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 260px">
<%@ include file="/envoy/common/shutdownBanner.jspIncl" %>    
    </DIV>        
    </BODY>
</HTML>
