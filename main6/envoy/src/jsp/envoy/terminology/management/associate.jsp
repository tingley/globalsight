<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_associate_with_column = bundle.getString("lb_associate_with_column");
String lb_column_mapping = bundle.getString("lb_column_mapping");
String lb_column_name = bundle.getString("lb_column_name");
String lb_column_properties = bundle.getString("lb_column_properties");
String lb_enter_column_name = bundle.getString("lb_enter_column_name");
String lb_map_column_to_type = bundle.getString("lb_map_column_to_type");
String lb_mapping_options = bundle.getString("lb_mapping_options");
String lb_term_language = bundle.getString("lb_term_language");
String lb_select_field_type = bundle.getString("lb_select_field_type");
String lb_cancel = bundle.getString("lb_cancel");
String lb_ok = bundle.getString("lb_ok");
%>
<html xmlns:gs>
<?IMPORT namespace="gs" implementation="/globalsight/includes/languageBox.htc"/>

<HEAD>
<TITLE><%=lb_column_mapping%></TITLE>
<%-- for debugging <META HTTP-EQUIV="EXPIRES" CONTENT="0"> --%>
<STYLE>
#idType 
{ 
    behavior:url(/globalsight/includes/SmartSelect.htc); 
}
</STYLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/importObjects_js.jsp"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT>
var args;

function doKeypress()
{
  var key = event.keyCode;

  if (key == 27) // Escape
  {
    doClose(false);
  }
  else if (key == 13) // Return
  {
    doClose(true);
  }
}

function doClose(ok)
{
  if (ok == true)
  {
    var name = idName.value;
    var type = getType();

    if (name == "")
    {
      idName.focus();
      alert("<%=EditUtil.toJavascript(lb_enter_column_name)%>");
      return;
    }

    if (type == null || type == "")
    {
      idType.focus();
      alert("<%=EditUtil.toJavascript(lb_select_field_type)%>");
      return;
    }

    var node;
    var nodes = $(args.importOptions).find("importOptions columnOptions column");
    for (i = 0; i < nodes.length; ++i)
    {
    	var attrValue = $(nodes[i]).attr("id");
    	if(attrValue == args.id){
    		node = nodes[i];
    	}
    }

    $(node).find("name").text(idName.value);
    $(node).find("type").text(type);
    $(node).find("termLanguage").text(idLanguage.options[idLanguage.selectedIndex].value);
	
    args.name				= idName.value;
    args.type 			  	= type;
    args.termLanguage   = idLanguage.options[idLanguage.selectedIndex].value;

    if (idColumn.options.length > 0)
    {
        if (type == "term" || type == "skip" || type.startsWith("concept"))
        {
            $(node).find("associatedColumn").text("-1");
			args.associatedColumn 	= "-1";
        }
        else
        {
            $(node).find("associatedColumn").text(idColumn.options[idColumn.selectedIndex].value);
            args.associatedColumn 	= 
            	idColumn.options[idColumn.selectedIndex].value;
        }
    }
    window.returnValue = args;
  }
  else
  {
    window.returnValue = null;
  }

  window.close();
}

function getType()
{
  var index = idType.options[idType.selectedIndex].value;
  var oImportFieldType = aImportFieldTypes[index];
  return oImportFieldType.type;
}

function setType(type)
{
  for (i = 0; i < aImportFieldTypes.length; ++i)
  {
    var oImportFieldType = aImportFieldTypes[i];

    if (oImportFieldType.type == type)
    {
      idType.selectedIndex = i;
      return;
    }
  }
}

function setLanguage(lang)
{
  for (i = 0; i < idLanguage.options.length; ++i)
  {
    if (idLanguage.options[i].text == lang)
    {
      idLanguage.selectedIndex = i;
      break;
    }
  }
}

function setColumn(col)
{
  for (i = 0; i < idColumn.options.length; ++i)
  {
    if (idColumn.options[i].text == col)
    {
      idColumn.selectedIndex = i;
      break;
    }
  }
}

function selectType()
{
  var index = idType.options[idType.selectedIndex].value;
  var oImportFieldType = aImportFieldTypes[index];

  if (oImportFieldType.type == "term")
  {
    rLanguage.disabled = false;
    idLanguage.disabled = false;
    rColumn.disabled = true;
    idColumn.disabled = true;
    rLanguageText.color="black";
    rColumnText.color="lightgrey";
  }
  else
  {
    rLanguage.disabled = true;
    idLanguage.disabled = true;
    rLanguageText.color="lightgrey";
    
    if (oImportFieldType.type.startsWith("term") ||
        oImportFieldType.type == "source")
    {
      rColumn.disabled = false;
      idColumn.disabled = false;
      rColumnText.color="black";
    }
    else
    {
      rColumn.disabled = true;
      idColumn.disabled = true;
      rColumnText.color="lightgrey";
    }
  }
}

function initLanguages(dom)
{
  var oOption, oLang;
  var nodes = $(dom).find("definition languages language name");

  for (i = 0; i < nodes.length; ++i)
  {
	var node = nodes[i];
    oLang = $(node).text();//oLang = nodes.item(i).text;
    oOption = document.createElement("OPTION");
    oOption.text = oLang;
    oOption.value = oLang;

    idLanguage.add(oOption);
  }
}

function initTypes()
{
  var oOption, oImportFieldType;
  for (i = 0; i < aImportFieldTypes.length; ++i)
  {
    oImportFieldType = aImportFieldTypes[i];

    oOption = document.createElement("OPTION");
    oOption.text = oImportFieldType.name;
    oOption.value = i;

    idType.add(oOption);
  }
}

function initColumns(dom)
{
 	var oOption, oColumn;
 	var nodes = $(dom).find("importOptions columnOptions column name");
	for (i = 0; i < nodes.length; ++i)
    {
		var node = nodes[i];
		var attrValue = $(node).parent().attr("id");
		if(attrValue != args.id){
    		oColumn = $(node).text();//oColumn = nodes.item(i).text;
    		oOption = document.createElement("OPTION");
    		oOption.text = oColumn;
    		oOption.value = attrValue;
    		idColumn.add(oOption);
		}
    }
}

function doLoad()
{
  args = window.dialogArguments;
  
  addCustomFields(args.definition);

  initTypes();
  initLanguages(args.definition);
  initColumns(args.importOptions);

  var attrValue,nodes,node;
  var dom = args.importOptions;
  nodes = $(dom).find("importOptions columnOptions column");
  for (i = 0; i < nodes.length; ++i)
  {
	  node = nodes[i];
	  attrValue = $(node).attr("id");
	  if(attrValue == args.id){
		  idName.value = $(node).find("name").text();
		  setType($(node).find("type").text());
		  setLanguage($(node).find("termLanguage").text());
		  setColumn($(node).find("associatedColumn").text());
	  }
  }

  selectType();

  idName.focus();
}
</script>
</head>

<body LEFTMARGIN="20" RIGHTMARGIN="20" TOPMARGIN="20"
      onload="doLoad()" onkeypress="doKeypress()">
<DIV ID="contentLayer">
<SPAN CLASS="mainHeading"><%=lb_column_mapping%></SPAN>
<P>
<SPAN CLASS="standardTextBold"><%=lb_column_properties%></SPAN>
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER=0 CLASS="standardText">
  <TR>
    <TD>
      <LABEL FOR="idName"><%=lb_column_name%></LABEL>
    </TD>
    <TD>
      <INPUT  TYPE="text" id="idName"></INPUT>
    </TD>
  </TR>
  <TR>
    <TD>
      <LABEL FOR="idType" ACCESSKEY="T"><%=lb_map_column_to_type%></LABEL>
    </TD>
    <TD>
      <SELECT id="idType" onchange="selectType()"></SELECT>
    </TD>
  </TR>
</TABLE>
<P>

<SPAN CLASS="standardTextBold"><%=lb_mapping_options%></SPAN>
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER=0 CLASS="standardText">
  <TR id="rLanguage">
    <TD>
      <font id=rLanguageText color="black"><LABEL FOR="idLanguage" ACCESSKEY="L"><%=lb_term_language%></LABEL></font>
    </TD>
    <TD>
      <SELECT id="idLanguage"></SELECT>
    </TD>
  </TR>
  <TR id="rColumn">
    <TD>
      <font id=rColumnText color="black"><LABEL FOR="idColumn" ACCESSKEY="C"><%=lb_associate_with_column%></LABEL></font>
    </TD>
    <TD>
      <SELECT id="idColumn"></SELECT>
    </TD>
  </TR>
</TABLE>
<P>
</DIV>

<P>
<DIV ALIGN="CENTER" STYLE="position: relative; Z-INDEX: 9;">
<INPUT TYPE=BUTTON VALUE="<%=lb_cancel%>" TABINDEX="8" ONCLICK="doClose(false);">
<INPUT TYPE=BUTTON VALUE="<%=lb_ok%>" TABINDEX="9" ONCLICK="doClose(true);">
</DIV>

</BODY>
</HTML>
