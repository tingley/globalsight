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
%>
<html xmlns:gs>
<?IMPORT namespace="gs" implementation="/globalsight/includes/languageBox.htc"/>

<HEAD>
<TITLE><%=lb_column_mapping%></TITLE>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<STYLE>
#idType 
{ 
    behavior:url(/globalsight/includes/SmartSelect.htc); 
}
</STYLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/administration/projects/importObjects_js.jsp"></SCRIPT>
<SCRIPT language="Javascript">
var args;

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

    var dom = args.importOptions;
    var nodes = $(dom).find("importOptions columnOptions column");
    var node,attrValue;
    for (i = 0; i < nodes.length; ++i)
    {
  	  attrValue = $(nodes[i]).attr("id");
  	  if(attrValue == args.id){
  	 	node = nodes[i];
  	  }
    }
    $(node).find("name").text(idName.value);
	$(node).find("type").text(type);
	$(node).find("subtype").text(getSubType());
	
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
  var oFieldType = aFieldTypes[index];
  return oFieldType.type;
}

function getSubType()
{
  try
  {
    return idSubType.options[idSubType.selectedIndex].value;
  }
  catch (ex)
  {
    return "";
  }
}

function setType(type)
{
  for (i = 0; i < aFieldTypes.length; ++i)
  {
    var oFieldType = aFieldTypes[i];

    if (oFieldType.type == type)
    {
      idType.selectedIndex = i;

      return;
    }
  }
}

function selectType(subtype)
{
  var index = idType.options[idType.selectedIndex].value;
  var oFieldType = aFieldTypes[index];

  if (oFieldType.type == "startdate" ||
      oFieldType.type == "enddate")
  {
    rSubType.disabled = false;
    idSubType.disabled = false;

    fillAttributes(idSubType, oFieldType.subtype.split(","), subtype);
  }
  else
  {
    clearSelect(idSubType);

    rSubType.disabled = true;
    idSubType.disabled = true;
  }
}

function initTypes()
{
  var oOption, oFieldType;
  for (i = 0; i < aFieldTypes.length; ++i)
  {
    oFieldType = aFieldTypes[i];

    oOption = document.createElement("OPTION");
    oOption.text = oFieldType.name;
    oOption.value = i;

    idType.add(oOption);
  }
}

function clearSelect(select)
{
    var options = select.options;
    for (var i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }
}

function fillAttributes(select, values, defaultValue)
{
    clearSelect(select);

    var options = select.options;

    for (var i = 0; i < values.length; i++)
    {
        var value = Trim(values[i]);

        var option = document.createElement('OPTION');
        option.text = value + '\u00a0';
        option.value = value;

        if (value == defaultValue)
        {
          option.selected = true;
        }

        options.add(option);
    }
}

function fillAttributesWithDummy(select)
{
    clearSelect(select);

    var options = select.options;
    var option = document.createElement('OPTION');
    option.text = "none ";
    options.add(option);
}

function doLoad()
{
  args = window.dialogArguments;

  initTypes();

  var dom = args.importOptions;
  var nodes = $(dom).find("importOptions columnOptions column");
  var node,attrValue;
  for(var i = 0; i < nodes.length; ++i)
  {
	  node = nodes[i];
	  attrValue = $(node).attr("id");
	  if(attrValue == args.id){
		  idName.value = $(node).find("name").text();
		  setType($(node).find("type").text());
		  selectType($(node).find("subtype").text());
	  }
  }
  idName.focus();
}
</script>
</head>

<body onload="doLoad()" LEFTMARGIN="20" RIGHTMARGIN="20" TOPMARGIN="20">
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
  <TR id="rSubType">
    <TD>
      <LABEL FOR="idSubType">Column format</LABEL>
    </TD>
    <TD>
      <SELECT id="idSubType"></SELECT>
    </TD>
  </TR>
</TABLE>
<P>
</DIV>

<P>
<DIV ALIGN="CENTER" STYLE="position: relative; Z-INDEX: 9;">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel")%>" TABINDEX="0" ONCLICK="doClose(false);">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_ok")%>" TABINDEX="0" ONCLICK="doClose(true);">
</DIV>

</BODY>
</HTML>
