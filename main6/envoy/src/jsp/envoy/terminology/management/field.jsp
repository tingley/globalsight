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

String lb_define_field = bundle.getString("lb_define_field");
String lb_enter_field_name = bundle.getString("lb_enter_field_name");
String lb_select_field_type = bundle.getString("lb_select_field_type");
String lb_field_name = bundle.getString("lb_field_name");
String lb_this_field_is_system = bundle.getString("lb_system_field");
String lb_field_properties = bundle.getString("lb_field_properties");
String lb_field_type = bundle.getString("lb_field_type");
String lb_field_format = bundle.getString("lb_field_format");
String lb_explanation_of_field_type = bundle.getString("lb_explanation_of_field_type");
String lb_allowed_values_for_attribute_fields =
  bundle.getString("lb_allowed_values_for_attribute_fields");
String lb_suggested_attribute_values =
  bundle.getString("lb_suggested_attribute_values");
String lb_tbfields_click_to_copy =
  bundle.getString("lb_tbfields_click_to_copy");
String lb_ok = bundle.getString("lb_ok");
String lb_cancel = bundle.getString("lb_cancel");
%>
<html xmlns:gs>
<?IMPORT namespace="gs" implementation="/globalsight/includes/languageBox.htc"/>

<HEAD>
<TITLE><%=lb_define_field%></TITLE>
<!-- <META HTTP-EQUIV="EXPIRES" CONTENT="0"> -->
<STYLE>
BODY    
{ 
    color: buttontext; 
    background-color: buttonface;
    font-family: arial;
    font-size: 9pt; 
    margin: 2px;
}
.link   { cursor: pointer; color: blue; }

LEGEND  
{ 
    font-size: 9pt; 
}
#idType { behavior:url(/globalsight/includes/SmartSelect.htc); }

.textBox 
{ 
    border-style: inset; 
    border-width: 2px;
    border-color: white;
    background-color: gainsboro; 
    overflow: auto;
    text-align: justify;
    margin-top: 1px; padding: 2px;
}

LABEL 
{
    font-size: 9pt;
}

TD 
{
    font-size: 9pt;
}

</STYLE>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT>
var g_isModify = false;
var g_definedFields;
var g_currentField;

function doClose(ok)
{
  if (ok == true)
  {
    var name = idName.value;
    var type = getType(); 
    var format = idFormat.value;
    var system = idSystem.checked;
    var values = idValues.value;

    if (name == "" || name.match(/^[A-Za-z0-9_. ]+$/) == null)
    {
      idName.focus();
      alert("<%=EditUtil.toJavascript(lb_enter_field_name)%>");
      return;
    }

    if (type == null || type == "")
    {
      idType.focus();
      alert("<%=EditUtil.toJavascript(lb_select_field_type)%>");
      return;
    }

    window.returnValue = new Field(name, type, format, system, values);
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

function setType(type)
{
  var index = 0;

  if (type.startsWith("attr"))
  {
    type = "attr";
  }
  else if (type.startsWith("text"))
  {
    type = "text";
  }

  for (i = 0; i < aFieldTypes.length; ++i)
  {
    var oFieldType = aFieldTypes[i];

    if (oFieldType.getType() == type)
    {
      index = i;
      break;
    }
  }

  var options = idType.options;
  for (i = 0; i < options.length; ++i)
  {
    var option = options[i];

    if (option.value == index)
    {
      idType.selectedIndex = i;
      break;
    }
  }
}

function selectType()
{
  //alert("Function selectType:");
  var index = idType.options[idType.selectedIndex].value;
  var oFieldType = aFieldTypes[index];
  idExplanation.innerHTML = oFieldType.description;

  if (oFieldType.isAttribute())
  {
    idFormat.value = "attribute";
    idDefaultValues.innerHTML = oFieldType.values;
    if (oFieldType.values != "")
        copylink.style.visibility = "visible";
    else
        copylink.style.visibility = "hidden";

    // TODO
    if (oFieldType.isUserDefinedAttribute() ||
        (oFieldType.isAttribute() && (!oFieldType.isSystemField() ||
         oFieldType.systemFieldAllowsValues())))
    {
      idValues.value = "*user-defined*";
      idValues.disabled = false;
      idValues.style.backgroundColor = "white";
    }
    else
    {
      idValues.value = oFieldType.values;
      idValues.disabled = true;
      idValues.style.backgroundColor = "gainsboro";
    }
  }
  else if (oFieldType.isTerm())
  {
    idFormat.value = "term";
    idDefaultValues.innerHTML = "";
    copylink.style.visibility = "hidden";
    idValues.value = "";
    idValues.disabled = true;
    idValues.style.backgroundColor = "gainsboro";
  }
  else
  {
    idFormat.value = "text";
    idDefaultValues.innerHTML = "";
    copylink.style.visibility = "hidden";
    idValues.value = "";
    idValues.disabled = true;
    idValues.style.backgroundColor = "gainsboro";
  }

  idSystem.checked = oFieldType.isSystemField();
}

function initTypes()
{
  var oOption, oFieldType;
  for (i = 0; i < aFieldTypes.length; ++i)
  {
    oFieldType = aFieldTypes[i];

    oOption = document.createElement("OPTION");
    oOption.text = oFieldType.getDisplayName();
    oOption.value = i;
    
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
    idType.add(oOption);
    } 
    else 
    {
    idType.appendChild(oOption);
    }
  }

  selectType();
}

function copyValues()
{
  idValues.value = idDefaultValues.innerHTML;
}

function doLoad()
{
  initTypes();
  idSystem.disabled = true;

  var arg = window.dialogArguments;
  g_definedFields = arg.fields;
  g_currentField = arg.field;

  if (g_currentField != null)
  {
    g_isModify = true;

    idName.value = g_currentField.name; 
    idFormat.value = g_currentField.format;
    setType(g_currentField.type); 
    idValues.value = g_currentField.values;
  }
  else
  {
    idSystem.checked = false;
    idName.focus();
  }
}
</script>
</head>

<body onload="doLoad()">
<TABLE width="100%" align="center">
  <TR>
    <TD valign="top" width="50%">
      <TABLE>
	<TR>
	  <TD>
	    <LABEL FOR="idName" ACCESSKEY="N"
	    style="margin-left: 5px;"><%=lb_field_name%></LABEL>
	  </TD>
	  <TD><INPUT id="idName" type="text"></INPUT></TD>
	</TR>
      </TABLE>
    </TD>
    <TD valign="top" width="50%"></TD>
  </TR>
  <TR>
    <TD COLSPAN="2">
      <FIELDSET>
      <LEGEND id="idLegend"><%=lb_field_properties%></LEGEND>
      <TABLE width="100%">
	<TR>
	  <TD valign="top" width="50%">
	    <TABLE width="95%">
	      <TR>
		<TD>
		  <LABEL FOR="idType" ACCESSKEY="T"><%=lb_field_type%></LABEL>
		</TD>
		<TD>
		  <SELECT id="idType" onchange="selectType()" onkeyup="selectType()"></SELECT>
		</TD>
	      </TR>
	      <TR>
		<TD><%=lb_field_format%></TD>
		<TD><INPUT id="idFormat" type="text" TABINDEX="-1"
		  CONTENTEDITABLE="false" 
		  STYLE="background-color:gainsboro"></INPUT>
		</TD>
	      </TR>
	      <TR>
		<TD>
		  <SPAN><%=lb_this_field_is_system%>:</SPAN>
		</TD>
		<TD>
		  <INPUT TABINDEX="-1" id="idSystem" type="checkbox"
		  style="border: 0px none; padding:0; margin:0;">
		</TD>
	      </TR>
	    </TABLE>
	  </TD>
	  <TD valign="top" width="50%">
	    <%=lb_explanation_of_field_type%>
	    <BR>
	    <DIV class="textBox" ID="idExplanation"
	    STYLE="width: 270px; height: 80px;">
	    </DIV>
	  </TD>
	</TR>
	<TR>
	  <TD valign="top" width="50%">
	    <LABEL FOR="idValues" ACCESSKEY="V"><%=lb_allowed_values_for_attribute_fields%></LABEL>
	    <BR>
	    <TEXTAREA id="idValues" ROWS="3" COLS="30"></TEXTAREA>
	  </TD>
	  <TD valign="top" width="50%">
	    <%=lb_suggested_attribute_values%>
	    <span class="link" id="copylink" onclick="copyValues()"><%=lb_tbfields_click_to_copy%></span>
	    <BR>
	    <DIV class="textBox" ID="idDefaultValues"
	    STYLE="width: 270px; height: 62px;*height: 55px;">
	    </DIV>
	  </TD>
	</TR>
      </TABLE>
      </FIELDSET>
    </TD>
  </TR>
</TABLE>
<DIV align="right" style="margin-top: 3pt">
<button TABINDEX="0" onclick="doClose(true);"><%=lb_ok%></button>
&nbsp;
<button TABINDEX="0" onclick="doClose(false);"><%=lb_cancel%></button>
</DIV>
</BODY>
</HTML>
