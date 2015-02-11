<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String title = bundle.getString("lb_term_input_model_edit_field_constraints");
%>
<html>
<head>
<title><%=title %></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/viewer/objects.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/management/FireFox.js"></script>
<style>
/* TO BE REMOVED AND REPLACED BY SetStylesheet.js */
BODY,
LABEL,
#idType,
#idDescription {
    font-family: Verdana, Helvetica, sans-serif;
    font-size: 10pt;
}
#idType {
    font-weight: bold;
}
#idDescription {
    height: 5em;
    width: 100%;
    overflow: auto;
    padding: 1px 3px 1px 1px;
    background-color: lightblue;
    word-wrap: break-word;
}
</style>
<SCRIPT language="Javascript">
var g_args;

var g_currentFields;
var g_currentField;

function setup(level, type, value, definedFields)
{
  g_currentFields = new Array().concat(definedFields);

  if (g_currentFields.length == 0)
  {
    if (level == 'concept')
    {
      g_currentFields = g_currentFields.concat(g_conceptFields);
    }
    else if (level == 'language')
    {
      g_currentFields = g_currentFields.concat(g_languageFields);
    }
    else if (level == 'term')
    {
      g_currentFields = g_currentFields.concat(g_termFields);
    }
    else if (level == 'field')
    {
      g_currentFields = g_currentFields.concat(g_fieldFields);
    }
    else if (level == 'source')
    {
      g_currentFields = g_currentFields.concat(g_sourceFields);
    }
  }

  // override field and source level fields
  if (level == 'field')
  {
    g_currentFields = g_fieldFields;
  }
  else if (level == 'source')
  {
    g_currentFields = g_sourceFields;
  }

  setType(g_currentFields, level, type);
  setValue(g_currentField, value);
}

function clearSelect(select)
{
    var options = select.options;
    for (var i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }
}

function setType(values, level, type)
{
    for (var i = 0; i < values.length; i++)
    {
        var value = values[i];

        if (value.type == type)
        {
          var options = idType.options;
          var option = document.createElement('OPTION');
          option.text = value.getDisplayName() + ' ';
          options.add(option);
          idType.disabled = true;
          idDescription.innerText = value.getDescription();

          g_currentField = value;

          return;
        }
    }

    // Not a predefined field, treat as user-defined.
    g_currentField = getUserDefinedField(level, type)

    idType.innerText = g_currentField.getDisplayName();
    idDescription.innerText = g_currentField.getDescription();
}

function fillAttributes(select, values, defaultValue)
{
    clearSelect(select);

    var options = select.options;

    for (var i = 0; i < values.length; i++)
    {
        var value = Trim(values[i]);

        var option = document.createElement('OPTION');
        option.text = value;
        option.value = i;

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
    option.text = "text field ";
    options.add(option);
}

function setValue(field, value)
{
/*
  if (field.isAttribute())
  {
    fillAttributes(idAttrValue, field.values.split(","), value);
    idAttrValue.focus();

    idAttrValue.disabled = false;
    idAttrValueLabel.disabled = false;
    idValue.disabled = true;
    idValueLabel.disabled = true;
  }
  else
  {
    fillAttributesWithDummy(idAttrValue);

    idValue.value = value;
    idValue.focus();

    idAttrValue.disabled = true;
    idAttrValueLabel.disabled = true;
    idValue.disabled = false;
    idValueLabel.disabled = false;
  }
*/
  if (value.indexOf("required") >= 0)
  {
    idRequired.checked = true;
    idOptional.focus();
  }
  else
  {
    idOptional.checked = true;
    idRequired.focus();
  }

  if (value.indexOf("multiple") >= 0)
  {
    idMultiple.checked = true;
  }
}

function doClose(ok)
{
  if (ok == true)
  {
    var field = g_currentField;
    var value;

/*
    if (field.isAttribute())
    {
      value = idAttrValue.options[idAttrValue.selectedIndex].text;
    }
    else
    {
      value = Trim(idValue.value);
    }
*/
    if (idRequired.checked)
    {
      value = "required";
    }
    else
    {
      value = "optional";
    }

    if (idMultiple.checked)
    {
      value += ", multiple";
    }

    g_args.setValue(value);

    window.returnValue = g_args;
  }
  else
  {
    window.returnValue = null;
  }

  window.close();
}

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

function doLoad()
{
  g_args = window.dialogArguments;

  var level = g_args.getLevel();
  var type = g_args.getType();
  var value = g_args.getValue();

  idLevel.innerText = Trim(level);

  var definedFields = g_args.getDefinedFields();
  setup(level, type, value, definedFields);
}
</script>
</head>

<body onload="doLoad()" onkeypress="doKeypress()">

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;">
<SPAN ID="idHeading" CLASS="mainHeading">
<%=bundle.getString("lb_edit") %> <span id="idLevel"></span> - <%=bundle.getString("lb_term_input_model_edit_level_field") %></SPAN>
<BR><BR>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
  <COL WIDTH="25%">
  <COL WIDTH="75%">
  <TBODY>
  <TR>
    <TD>
      <LABEL><%=bundle.getString("lb_the_field") %></LABEL>
    </TD>
    <TD>
      <select id="idType" TABINDEX="-1"></select>
    </TD>
  </TR>
<!--
  <TR>
    <TD valign="top">
      <LABEL id="idAttrValueLabel" FOR="idAttrValue">Attribute Value:</LABEL>
    </TD>
    <TD valign="top">
      <SELECT id="idAttrValue" TABINDEX="-1"></SELECT>
    </TD>
  </TR>
  <TR>
    <TD valign="top">
      <LABEL id="idValueLabel" FOR="idValue">Text Value:</LABEL>
    </TD>
    <TD valign="top">
      <TEXTAREA id="idValue" TABINDEX="2" rows="5" cols="27"></TEXTAREA>
    </TD>
  </TR>
-->
  <TR><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>
  <TR>
    <TD valign="top"><LABEL><%=bundle.getString("lb_term_input_model_explanation") %>:</LABEL></TD>
    <TD valign="top">
      <P id="idDescription" style="width:200"></P>
    </TD>
  </TR>
  <TR>
    <TD valign="top" rowspan=2><%=bundle.getString("lb_term_input_model_is") %>:</TD>
    <TD>
      <INPUT TYPE="radio" name="m" id="idRequired" TABINDEX="2" checked>
      <LABEL FOR="idRequired"><%=bundle.getString("lb_editor_required") %></LABEL>
      <INPUT TYPE="radio" name="m" id="idOptional" TABINDEX="3">
      <LABEL FOR="idOptional"><%=bundle.getString("lb_editor_optional") %></LABEL>
    </TD>
  </TR>
  <TR>
    <TD colspan=2>
      <INPUT TYPE="checkbox" id="idMultiple" TABINDEX="4">
      <LABEL FOR="idMultiple"><%=bundle.getString("lb_term_input_model_occur_multi_times") %></LABEL>
    </TD>
  </TR>
  </TBODY>
</TABLE>

<BR>

<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE=" <%=bundle.getString("lb_ok") %> " TABINDEX="5" onclick="doClose(true);">
<INPUT id="idCancel" TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel") %>" TABINDEX="6" onclick="doClose(false);">
</DIV>

</DIV>
</body>
</html>
