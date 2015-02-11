<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ArrayList,
        java.util.Locale,
        java.util.ResourceBundle,
        com.globalsight.util.GlobalSightLocale,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.cxe.entity.knownformattype.KnownFormatType,
        com.globalsight.everest.aligner.AlignerPackageOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

AlignerPackageOptions gapOptions = (AlignerPackageOptions)
  sessionMgr.getAttribute(WebAppConstants.GAP_OPTIONS);

ArrayList formatTypes =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_FORMATTYPES);
ArrayList rules =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_RULES);
ArrayList locales =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_LOCALES);
ArrayList encodings =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_ENCODINGS);
ArrayList extensions =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_EXTENSIONS);
ArrayList packageNames =
	  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_PACKAGE_NAMES);

String error = (String)sessionMgr.getAttribute(WebAppConstants.GAP_ERROR);

String urlNext = next.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = bundle.getString("lb_aligner_package_create");
String lb_helptext = bundle.getString("helper_text_aligner_package_create");
%>
<HTML XMLNS:gs>
<HEAD>
<!-- JSP file: createPackage.jsp -->
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "<%=bundle.getString("lb_aligner_package")%>";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_createPackage1")%>";
var xmlStr = "<%=gapOptions.getXml()%>";

function Result(message, element, dom)
{
    this.message = message;
    this.element = element;
    this.dom = dom;
}

function doCancel()
{
   if (confirmJump())
   {
      window.navigate("<%=urlCancel%>");
   }
   else
   {
      return false;
   }
}

function doNext()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
        alert(result.message);
        result.element.focus();
    }
    else
    {
        url = "<%=urlNext%>&<%=WebAppConstants.GAP_ACTION%>=" +
          "<%=WebAppConstants.GAP_ACTION_SELECTFILES%>";

        oForm.action = url;   
             
        oForm.gapoptions.value = getDomString(result.dom);
        
        oForm.submit();
    }
}

function buildOptions()
{
    var result = new Result("", null, null);
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlStr);

    var node;

    node = $(dom).find("alignerPackageOptions");

    var name = form.idPackageName.value;

    if (!name)
    {
      return new Result("<%=bundle.getString("jsmsg_aligner_package_create_enter_name")%>", form.idPackageName, null);
    }

    // Do not allow "\",  "/", ":" and other characters in the name
    // that are not valid in Windows (or Unix) filenames.
    var regex = /[\\/:;\*\?\|\"\'<>]/;
    if (regex.test(name))
    {
      return new Result("<%=bundle.getString("jsmsg_aligner_package_create_invalid_name") %> \\/:;*?|\"<>\'.",
        form.idPackageName, null);
    }

    <% for (int i = 0; i < packageNames.size(); i++) 
    {%>
    	if (name == "<%=(String)packageNames.get(i)%>") 
        {
        	return new Result("<%=bundle.getString("jsmsg_aligner_package_existing_name")%>", 
                	form.idPackageName, null);
        }
    <%}%>

    var sel = form.idFileFormat;

    $(node).find("packageName").text(name);
    $(node).find("extractionOptions formatType").text(sel.options[sel.selectedIndex].value);
    
    var sel = form.idRules;
    if (sel.disabled)
    {
    	$(node).find("extractionOptions rules").text('');
    }
    else
    {
    	$(node).find("extractionOptions rules").text(sel.options[sel.selectedIndex].value);
    }

    var sel = form.idSourceLocale;  
    $(node).find("extractionOptions sourceLocale").text(sel.options[sel.selectedIndex].value);
    
    var sel = form.idTargetLocale;
    $(node).find("extractionOptions targetLocale").text(sel.options[sel.selectedIndex].value);
    
    var sel = form.idSourceEncoding;
    $(node).find("extractionOptions sourceEncoding").text(sel.options[sel.selectedIndex].value);

    var sel = form.idTargetEncoding;
    $(node).find("extractionOptions targetEncoding").text(sel.options[sel.selectedIndex].value);
    
    var exts = $(node).find("extractionOptions extensions");
    var len = exts.children().length;
    while(len){
    		exts.children().eq(0).remove();
    		len = exts.children().length;
    }
    
    var haveExtension = false;

    if (form.idAllExtensions.checked)
    {
        // leave extension list empty
        haveExtension = true;
    }
    else
    {
        var sel = form.idExtensions;
        var opts = sel.options;
        for (var i = 0, max = opts.length; i < max; i++)
        {
          var opt = opts.item(i);

          if (opt.selected)
          {
            haveExtension = true;

            var ext = dom.createElement("extension");
            exts.append(ext);
            $(exts).find("extension").text(opt.value);
          }
        }
    }

    if (!haveExtension)
    {
        return new Result("<%=bundle.getString("jsmsg_aligner_package_create_need_ext") %>", form.idExtensions, null);
    }
    result.dom = dom;
    return result;
}

function selectValue(select, value)
{
  for (var i = 0; i < select.options.length; ++i)
  {
    if (select.options[i].value == value)
    {
      select.selectedIndex = i;
      return;
    }
  }
}

function selectMultipleValue(select, value)
{
  var options = select.options;
  for (var i = 0; i < options.length; ++i)
  {
    var option = options.item(i);

    if (option.value == value)
    {
      option.selected = true;
      return;
    }
  }
}

function changeFileFormat()
{
    var form = document.oDummyForm;
    var sel = form.idFileFormat;
    var format = sel.options[sel.selectedIndex]./*value*/text;

    if (formatUsesXmlRuleFile(format))
    {
      form.idRules.disabled = false;
    }
    else
    {
      form.idRules.disabled = true;
    }
}

function formatUsesXmlRuleFile(p_format)
{
    var format = p_format.toLowerCase();

    if (format == "xml" || format == "quark" ||
        format == "frame5" || format == "frame6" || format == "frame7" ||
        format == "ebay prj")
    {
        return true;
    }

    return false;
}

function parseOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlStr);
    var nodes, node;
    var packageName, formatType, rules, srcLocale, trgLocale;
    var srcEncoding, trgEncoding;
    
    node = $(dom).find("alignerPackageOptions");
    packageName = $(node).find("packageName").text();

    node = $(dom).find("extractionOptions");
    formatType = $(node).find("formatType").text();
    rules = $(node).find("rules").text();
    srcLocale = $(node).find("sourceLocale").text();
    trgLocale = $(node).find("targetLocale").text();
    srcEncoding = $(node).find("sourceEncoding").text();
    trgEncoding = $(node).find("targetEncoding").text();

	document.getElementById("idPackageName").value=packageName;//form.idPackageName.value = packageName;
    selectValue(form.idFileFormat, formatType);
    selectValue(form.idRules, rules);
    selectValue(form.idSourceLocale, srcLocale);
    selectValue(form.idTargetLocale, trgLocale);
    selectValue(form.idSourceEncoding, srcEncoding);
    selectValue(form.idTargetEncoding, trgEncoding);

    var nodes = $(dom).find("extractionOptions extension");

    if (nodes.length == 0)
    {
      form.idAllExtensions.checked = true;
    }
    else
    {
      for (var i = 0; i < nodes.length; i++)
      {
        //var ext = nodes.item(i);
        var ext = nodes[i];

        selectMultipleValue(form.idExtensions, ext.text());
      }
    }
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    parseOptions();
    changeFileFormat();

    document.all.idPackageName.focus();
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0"
        TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<XML id="oOptions"><%=gapOptions.getXml()%></XML>

<DIV ID="contentLayer"
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538>
    <%=lb_helptext %>
    </TD>
  </TR>
</TABLE>
<BR>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="gapoptions" VALUE="Options XML goes here">
</FORM>

<FORM NAME="oDummyForm">
<DIV>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <THEAD>
    <col valign="top" align="right">
    <col valign="top" align="left">
    <col valign="top" align="right">
    <col valign="top" align="left">
  </THEAD>
  <TBODY>
    <TR VALIGN="TOP">
      <TD colspan=4 align=left>
	<%=bundle.getString("lb_package_name") %>:
	<input id="idPackageName" name="idPackageName" type=text width=50>
      </TD>
    </TR>
    <TR><TD colspan=4>&nbsp;</TD></TR>
    <TR>
      <TD><%=bundle.getString("lb_select_files_type") %>:</TD>
      <TD>
	<select id="idFileFormat" name="idFileFormat"
	  onchange="changeFileFormat()">
	  <%
	  for (int i = 0, max = formatTypes.size(); i < max; i++)
	  {
	    KnownFormatType f = (KnownFormatType)formatTypes.get(i);
	    String type = f.getFormatType();

	    if (type.equalsIgnoreCase("unextracted"))
	    {
	      continue;
	    }
	  
	    out.print("<OPTION VALUE='");
	    out.print(f.getId());
	    out.print("'");
	    if (type.equalsIgnoreCase("html"))
	    {
	      out.print(" SELECTED");
	    }
	    out.print(">");
	    out.print(f.getName());
	    out.println("</OPTION>");
	  }
	  %>
	</select>
	<BR>
	<SPAN><%=bundle.getString("lb_xml_rules") %>:
	<select id="idRules" name="idRules" DISABLED style="width:14em">
	  <%
	  if (rules.size() > 0)
	  {
	    for (int i = 0, max = rules.size(); i < max; i++)
	    {
	      String s = (String)rules.get(i);
	      out.print("<OPTION VALUE='");
	      out.print(s);
	      out.print("'>");
	      out.print(s);
	      out.println("</OPTION>");
	    }
	  }
	  else
	  {
	      out.print("<OPTION VALUE=''>");
	      out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
	      out.println("</OPTION>");
	  }
	  %>
	</select>
	</SPAN>
      </TD>
      <TD><%=bundle.getString("lb_extension") %>:</TD>
      <TD>
	<select id="idExtensions" name="idExtensions" size="4" MULTIPLE
	  style="width:7em" align="top">
	  <%
	  for (int i = 0, max = extensions.size(); i < max; i++)
	  {
	    String s = (String)extensions.get(i);
	    out.print("<OPTION VALUE='");
	    out.print(s);
	    out.print("'>");
	    out.print(s);
	    out.println("</OPTION>");
	  }
	  %>
	</select>
	<input id="idAllExtensions" name="idAllExtensions" type="checkbox">
	<label for="idAllExtensions"><%=bundle.getString("lb_extensions_all") %></label>
      </TD>
    </TR>
    <TR>
      <TD><%=bundle.getString("lb_source_locale") %>:</TD>
      <TD>
	<select id="idSourceLocale" name="idSourceLocale">
	  <%
	  for (int i = 0, max = locales.size(); i < max; i++)
	  {
	    GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
	    out.print("<OPTION VALUE='");
	    out.print(locale.toString());
	    out.print("'>");
	    out.print(locale.getDisplayName(uiLocale));
	    out.println("</OPTION>");
	  }
	  %>
	</select>
      </TD>
      <TD><%=bundle.getString("lb_encoding") %>:</TD>
      <TD>
	<select id="idSourceEncoding" name="idSourceEncoding">
	  <%
	  for (int i = 0, max = encodings.size(); i < max; i++)
	  {
	    String s = (String)encodings.get(i);
	    out.print("<OPTION VALUE='");
	    out.print(s);
	    out.print("'>");
	    out.print(s);
	    out.println("</OPTION>");
	  }
	  %>
	</select>
      </TD>
    </TR>
    <TR>
      <TD><%=bundle.getString("lb_target_locale") %>:</TD>
      <TD>
	<select id="idTargetLocale" name="idTargetLocale">
	  <%
	  for (int i = 0, max = locales.size(); i < max; i++)
	  {
	    GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
	    out.print("<OPTION VALUE='");
	    out.print(locale.toString());
	    out.print("'>");
	    out.print(locale.getDisplayName(uiLocale));
	    out.println("</OPTION>");
	  }
	  %>
	</select>
      </TD>
      <TD><%=bundle.getString("lb_encoding") %>:</TD>
      <TD>
	<select id="idTargetEncoding" name="idTargetEncoding">
	  <%
	  for (int i = 0, max = encodings.size(); i < max; i++)
	  {
	    String s = (String)encodings.get(i);
	    out.print("<OPTION VALUE='");
	    out.print(s);
	    out.print("'>");
	    out.print(s);
	    out.println("</OPTION>");
	  }
	  %>
	</select>
      </TD>
    </TR>
  </TBODY>
</TABLE>
</DIV>
</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
</DIV>

</DIV>

</BODY>
</HTML>
