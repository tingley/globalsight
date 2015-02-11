<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
        java.io.IOException,
        com.globalsight.everest.localemgr.LocaleManagerWLRemote,
        com.globalsight.everest.servlet.util.ServerProxy,
        com.globalsight.util.GlobalSightLocale,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.util.SortUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        javax.servlet.jsp.JspWriter"
    session="true"
%>
<%!

public void printArray(JspWriter out, 
				String p_locale, String p_language, 
				String p_localCountry, String p_languageCountry)
    throws IOException
{
  out.print("g_languages[i++] = new pair('");
  out.print(p_locale);
  out.print("','");
  out.print(p_language);
  out.print("','");
  out.print(p_localCountry);
  out.print("','");
  out.print(p_languageCountry);
  out.print("');\n");
}

public void printSortOrder(JspWriter out, Locale p_uiLocale, GlobalSightLocale p_order)
    throws IOException
{
  String language = p_order.getDisplayLanguage(p_uiLocale);
  String locale = p_order.getLanguage();
  String localCountry = p_order.toString();
  String languageCountry = p_order.getDisplayName();
  printArray(out, locale, language, localCountry, languageCountry); 
  
}

// Special case for Chinese: print out "zh_XX"
public void printSpecialSortOrder(JspWriter out, Locale p_uiLocale, Locale p_order)
    throws IOException
{
  String language = p_order.getDisplayName(p_uiLocale);
  String locale = p_order.toString();
  String localCountry = locale;
  String languageCountry = p_order.getDisplayName();
  printArray(out, locale, language, localCountry, languageCountry);
}

%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
%>
<html>
	<!-- This is envoy\terminology\management\language.jsp -->
<head>
<title><%=bundle.getString("lb_language_dialog")%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT language="Javascript">
var isModify = false;
var bLanguageExists = false;
var islocalCountry = false;

function pair(locale, language, localCountry, languageCountry)
{
  this.locale = locale;
  this.language = language;
  this.localCountry = localCountry;
  this.languageCountry = languageCountry;
}

function compareLanguages(p_a, p_b)
{
  var aname = p_a.language;
  var bname = p_b.language;

  // Only in JScript 5.5
  try
  {
    return aname.localeCompare(bname);
  }
  catch (e)
  {
    if (aname == bname) return 0;
    if (aname > bname) return 1;
    return -1;
  }
}

var i = 0;
var g_languages = new Array();
<%

Set<String> languages = new HashSet<String>();
LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
Vector<GlobalSightLocale> sources = localeMgr.getAvailableLocales();
/*
List<Locale> realLocales = new ArrayList<Locale>();
for (GlobalSightLocale locale : sources)
{
    languages.add(locale.getLocale().getLanguage());
}

languages.remove("zh");

for (String language : languages)
{
    realLocales.add(new Locale(language, ""));
}

SortUtil.sort(realLocales, new Comparator<Locale>() {
    public int compare(Locale o1, Locale o2) {
        return  o1.getDisplayName(Locale.US).compareToIgnoreCase(o2.getDisplayName(Locale.US));
    }
});

for (Locale locale : realLocales)
{
  printSortOrder(out, uiLocale, locale);
}	

printSpecialSortOrder(out, uiLocale, Locale.SIMPLIFIED_CHINESE);
printSpecialSortOrder(out, uiLocale, Locale.TRADITIONAL_CHINESE);
*/

SortUtil.sort(sources, new Comparator() {
	public int compare(Object o1, Object o2) {
		return ((GlobalSightLocale) o1).getDisplayName(Locale.US).compareToIgnoreCase(((GlobalSightLocale) o2).getDisplayName(Locale.US));
	}
});
for (int i = 0; i < sources.size(); i++)
{
    GlobalSightLocale locale = sources.elementAt(i);
  	printSortOrder(out, uiLocale, locale);
}	
%>

function initLocales()
{
  g_languages.sort(compareLanguages);
  var lastLang = "";

  for (i = 0; i < g_languages.length; ++i)
  {
    var lang = g_languages[i].language;
    var locale = g_languages[i].locale;    
    var languageCountry = g_languages[i].languageCountry;
    var localCountry 	= g_languages[i].localCountry;

    if(lastLang!=lang)
    {
    	var oOption = document.createElement("OPTION");
    	oOption.text = lang;
    	oOption.value = locale;
    }

    var oOption2 = document.createElement("OPTION");
    oOption2.text = languageCountry;
    oOption2.value = localCountry;

    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
		if(lastLang!=lang)
		{
			idLocale.add(oOption);
		}
		idLocaleCountry.add(oOption2);
    } 
    else 
    {
    	if(lastLang!=lang)
		{
    		idLocale.appendChild(oOption);
		}
    	idLocaleCountry.appendChild(oOption2);
    }

    lastLang = lang;
  }
}

function fnCheckbox(i)
{
	var eLocale 	 = document.getElementById("idLocale");
	var eLocaleText  = document.getElementById("idLocaleText");
	var eLocaleC	 = document.getElementById("idLocaleCountry");
	var eLocaleCText = document.getElementById("idLocaleCountryText");
	
	if(i==1)
	{
		eLocale.disabled 		= false;
		eLocale.style.color  	= "";
		eLocaleText.style.color	= "";
		eLocaleC.disabled		= true;
		eLocaleC.style.color  	= "gray";
		eLocaleCText.style.color= "gray";
		eLocale.focus();
		islocalCountry = false;
	}
	else if(i==2)
	{
		eLocale.disabled 		= true;
		eLocale.style.color  	= "gray";
		eLocaleText.style.color = "gray";
		eLocaleC.disabled		= false;
		eLocaleC.style.color  	= "";
		eLocaleCText.style.color= "";
		eLocaleC.focus();
		islocalCountry = true;
	}
}

function selectLocale(value)
{
  	if(value!==null&&value.length>2)
  	{
	  	local = document.getElementById("idLocaleCountry");
	  	islocalCountry = true;
  	}
  	else
  	{
	  	local = document.getElementById("idLocale");
	  	islocalCountry = false;
  	}
  	fnCheckbox(islocalCountry?2:1);
    
  	for (i = 0; i < local.options.length; ++i)
  	{
    	if (local.options[i].value == value)
    	{
    		local.selectedIndex = i;
      		return;
    	}
  	}
}

<%--
// Could use this to fill the Name with the selected language name.
// But it's awkward, if you select a first sort order then change
// your mind, the name won't get updated.
function fillLanguage()
{
  if (idName.value == "")
  {
    idName.value = idLocale.options(idLocale.selectedIndex).text;
  }
}
--%>

function doClose(ok)
{
  if (ok == true)
  {
    var hasterms = true;
    var name = Trim(idName.value);
    var locale,seLocale;
    var exists = bLanguageExists;

    if(islocalCountry)
    {
    	seLocale = document.getElementById("idLocaleCountry");
  	}
    else
    {
    	seLocale = document.getElementById("idLocale");
    }
    locale = seLocale.options[seLocale.selectedIndex].value;

    if (name == "")
    {
      idName.focus();
      alert("<%=EditUtil.toJavascript(bundle.getString("lb_enter_language_name"))%>");
      return;
    }

    if (locale == "")
    {
      seLocale.focus();
      alert("<%=EditUtil.toJavascript(bundle.getString("lb_select_sort_order"))%>");
      return;
    }

    window.returnValue = new Language(name, locale, hasterms, exists);
  }
  else
  {
    window.returnValue = null;
  }

  window.close();
}

function doLoad()
{
  initLocales();

  var arg = window.dialogArguments;

  if (arg != null)
  {
    isModify = true;
    idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("lb_modify_language"))%>";
    idName.value = arg.name;
    selectLocale(arg.locale);
    bLanguageExists = arg.exists;
  }
  else
  {
    idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("lb_add_language"))%>";
  }
}
</script>
</head>

<body onload="doLoad()">

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 20px; LEFT: 20px; RIGHT: 20px;">
<SPAN ID="idHeading" CLASS="mainHeading"></SPAN>
<BR>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
  <TR>
    <TD></TD>
    <TD>
      <LABEL FOR="idName"><%=bundle.getString("lb_language_name")%></LABEL>
    </TD>
    <TD>
      <INPUT id="idName" TABINDEX="1" type="text" size="25"></INPUT>
    </TD>
  </TR>
  <TR>
    <TD><input type="radio" name="radioBtn" onclick="fnCheckbox(1)" checked ></TD>
    <TD>
      <LABEL FOR="idLocale" id="idLocaleText">
      		<%=bundle.getString("lb_sort_order")%>
      </LABEL>
    </TD>
    <TD>
      <select id="idLocale" TABINDEX="2">
		<option value=""><%=bundle.getString("lb_select")%></option>
      </select>
    </TD>
  </TR>
  
  <TR>
  	<TD><input type="radio" name="radioBtn" onclick="fnCheckbox(2)"></input></TD>
    <TD>
      <LABEL FOR="idLocaleCountry" id="idLocaleCountryText" style="color:grey;">
      		<%=bundle.getString("lb_sort_order_withCountry")%>
      </LABEL>
    </TD>
    <TD>
      <select id="idLocaleCountry" TABINDEX="3" disabled>
		<option value=""><%=bundle.getString("lb_select")%></option>
      </select>
    </TD>
  </TR>
</TABLE>
<P>

<DIV ALIGN="CENTER">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel")%>" TABINDEX="3" onclick="doClose(false);">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_ok")%>" TABINDEX="3" onclick="doClose(true);">
</DIV>

</DIV>

</body>
</html>
