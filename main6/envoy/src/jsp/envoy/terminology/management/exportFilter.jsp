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

String lb_title = bundle.getString("lb_termbase_advanced_export_filters");
String lb_cancel = bundle.getString("lb_cancel");
String lb_ok = bundle.getString("lb_ok");
%>
<html xmlns:gs>
<?IMPORT namespace="gs" implementation="/globalsight/includes/languageBox.htc" ?>
<HEAD>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<TITLE><%=lb_title%></TITLE>
<STYLE>

*{font-family: Arial, Helvetica, sans-serif;
  font-size:12px;}

#idType
{
    behavior:url(/globalsight/includes/SmartSelect.htc);
}

#idFilters {
    background-color: #ffffff;
    table-layout: fixed;
    behavior: url(/globalsight/includes/rowover.htc);
    ro--selected-background: #738EB5;
    ro--selected-color: black;
}

#idAdd, #idEdit, #idRemove { width: 80px; }


</STYLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT>
var g_args;
var g_definedFields;
var g_displayedFields;
var g_oldLevel;

var g_selectedRow ;

// Operators for reference
var s_contains = '\u2248';
var s_containsNot = '\u2249';
var s_equals = '\u003D';
var s_equalsNot = '\u2260';
var s_lessthan = '<';
var s_greaterthan = '>';
var s_exists = '\u2203';
var s_existsNot = '\u2204';

var s_arrow = '\u2192';

function doKeypress(event)
{
  //var key = event.keyCode;
  var key = window.event ? event.keyCode:event.which;

  if (key == 27) // Escape
  {
    doClose(false);
  }
  /*
  else if (key == 13) // Return
  {
    doClose(true);
  }
  */
}

function initLevels()
{
  var langs = g_args.getLanguages();

  for (var i = 0; i < langs.length; i++)
  {
    var lang = langs[i];

    var option = document.createElement("OPTION");
    option.text = lang.name;
    option.value = lang.name;
    // Yes, a language in this context describes the term-level fields.
    option.level = 'term';

    idLevel.add(option);
  }
}

function initFields()
{
  g_definedFields = g_args.getFields();

  setFields('concept');
}

function initFilterConditions(filters)
{
  if (!filters || filters.length == 0)
  {
    return;
  }

  for (var i = 0; i < filters.length; i++)
  {
    var filter = filters[i];

    var level = filter.getLevel();
    var field = filter.getField();
    var operator = filter.getOperator();
    var value = filter.getValue();
    var matchcase = filter.getMatchCase();

    switch (operator)
    {
      case 'contains':    operator = s_contains; break;
      case 'containsnot': operator = s_containsNot; break;
      case 'equals':      operator = s_equals; break;
      case 'equalsnot':   operator = s_equalsNot; break;
      case 'lessthan':    operator = s_lessthan; break;
      case 'greaterthan': operator = s_greaterthan; break;
      case 'exists':      operator = s_exists; break;
      case 'existsnot':   operator = s_existsNot; break;
      default:            operator = s_equals; break;
    }

    addFilterRow(level, field, operator, value, matchcase);
  }
}

function getFilterConditions()
{
  var result = new Array();

  var rows = idTBody.rows;
  for (var i = 0; i < rows.length; i++)
  {
    var row = rows[i];

    var level = getLevelFromRow(row);
    var field = getFieldFromRow(row);
    var operator = getOperatorStringFromRow(row);
    var value = getValueFromRow(row);
    var matchcase = getMatchCaseFromRow(row);

    result.push(new FilterCondition(level, field,
      operator, value, matchcase));
  }

  return result;
}

function setFields(level)
{
  var fields;

  // Custom fields do not carry level information. For now,
  // if any fields are defined, show them all.
  if (g_definedFields && g_definedFields.length > 0)
  {
    g_displayedFields = g_definedFields;
  }
  else
  {
    if (level == 'concept')
    {
      g_displayedFields = g_conceptFields;
    }
    else if (level == 'language')
    {
      g_displayedFields = g_languageFields;
    }
    else if (level == 'term')
    {
      g_displayedFields = g_termFields;
    }
  }

  // refill the field list only if it has changed
  if (!g_oldLevel || (g_oldLevel != level &&
     (!g_definedFields || g_definedFields.length == 0)))
  {
    g_oldLevel = level;

    var idField = document.getElementById("idField");
    var options = idField.options;
    for (var i = options.length; i >= 1; --i)
    {
    	if(window.navigator.userAgent.indexOf("MSIE")>0)
        {
            options.remove(i-1);
        }else{
        	idField.remove(i-1);
        }
    }

    for (var i = 0; i < g_displayedFields.length; i++)
    {
      var field = g_displayedFields[i];
      var option = document.createElement("OPTION");

      option.text  = field.getDisplayName();
      option.value = field.getType();

      options.add(option);
    }
  }
}

function changeLevel()
{
  var level = idLevel.options[idLevel.selectedIndex].level;

  setFields(level);
}

function selectLevel(lang)
{
  var options = idLevel.options;
  for (var i = 0; i < options.length; i++)
  {
    var option = options[i];

    if (option.text == lang)
    {
      option.selected = true;
    }
  }
}

function getSelectedLevel()
{
  return idLevel.options[idLevel.selectedIndex].text;
}

function selectField(field)
{
  var options = idField.options;
  for (var i = 0; i < options.length; i++)
  {
    var option = options[i];

    if (option.value == field)
    {
      option.selected = true;
    }
  }
}

function getSelectedField()
{
  return idField.options[idField.selectedIndex].value;
}

function selectOperator(char)
{
  var options = idOperator.options;
  for (var i = 0; i < options.length; i++)
  {
    var option = options[i];

    if (option.char == char)
    {
      option.selected = true;
      break;
    }
  }

  changeOperator();
}

function changeOperator()
{
  var operator = getSelectedOperator();

  if (operator == s_exists || operator == s_existsNot)
  {
    idValue.value = '';
    idValue.disabled = true;
    idCase.checked = false;
    idCase.disabled = true;
  }
  else
  {
    idValue.disabled = false;
    idCase.disabled = false;
  }
}

function getSelectedOperator()
{
	//return idOperator.options[idOperator.selectedIndex].char;
	return idOperator.options[idOperator.selectedIndex].getAttribute("char");
}

function getOperatorImage(operator)
{
  var result = '<IMG SRC="/globalsight/images/U';

  var op = operator.charCodeAt(0).toString(16);

  if      (op.length == 3) result += '0';
  else if (op.length == 2) result += '00';
  else if (op.length == 1) result += '000';

  result += op;

  result += '.gif" width="20" height="20" op="';

  switch (operator)
  {
    case s_lessthan:    result += '&lt;'; break;
    case s_greaterthan: result += '&gt;'; break;
    default:            result += operator;
  }

  result += '">';

  return result;
}

function getCaseImage(matchcase)
{
  if (matchcase == true || matchcase == 'true')
  {
    return '<img src="/globalsight/images/checkmark.gif">';
  }

  return '\u00a0';
}

function selectCase(arg)
{
  idCase.checked = arg;
}

function getCase(s)
{
  // check for the checkmark.gif image
  return s.indexOf('<IMG') >= 0;
}

function getLevelFromRow(row)
{
  return row.cells[0].level;
}

function getFieldFromRow(row)
{
  return row.cells[0].field;
}

function getOperatorFromRow(row)
{
  return row.cells[1].firstChild.getAttribute("op");
}

function getOperatorStringFromRow(row)
{
  var op = getOperatorFromRow(row);

  switch (op)
  {
    case s_contains: return 'contains';
    case s_containsNot: return 'containsnot';
    case s_equals: return 'equals';
    case s_equalsNot: return 'equalsnot';
    case s_lessthan: return 'lessthan';
    case s_greaterthan: return 'greaterthan';
    case s_exists: return 'exists';
    case s_existsNot: return 'existsnot';
    default: throw "unknown operator " + op;
  }
}

function getValueFromRow(row)
{
  return row.cells[2].innerText;
}

function getMatchCaseFromRow(row)
{
  return getCase(row.cells[3].innerHTML);
}

function selectRow(level, field, operator, value, matchcase, row)
{
    /*
  g_selectedRow = event.srcRow.rowIndex;
  var row = idTBody.rows[g_selectedRow];

  var level = getLevelFromRow(row);
  var field = getFieldFromRow(row);
  var operator = getOperatorFromRow(row);
  var value = getValueFromRow(row);
  var matchcase = getMatchCaseFromRow(row);
*/

  if(g_selectedRow != undefined) {
      g_selectedRow.bgColor = '';
  }

  g_selectedRow = row;
  row.bgColor = '#738EB5';
  
  if (operator == s_exists || operator == s_existsNot)
  {
    value = '';
    matchcase = false;
  }

  //alert("level=" + level + " field=" + field +
  // " operator=" + operator + " value=" + value + " case=" + matchcase);

  selectLevel(level);
  changeLevel();
  selectField(field);
  selectOperator(operator);
  selectCase(matchcase);
  idValue.value = value;

  idEdit.disabled = false;
  idRemove.disabled = false;
}

function addFilter()
{
  var level = getSelectedLevel();
  var field = getSelectedField();
  var operator = getSelectedOperator();
  var value = idValue.value;
  var matchcase = idCase.checked;

  if (operator == s_exists || operator == s_existsNot)
  {
    value = '';
    matchcase = false;
  }

  addFilterRow(level, field, operator, value, matchcase);
}

function addFilterRow(level, field, operator, value, matchcase)
{
  var row, cell;
  row = idTBody.insertRow(-1);
  row.onclick=function newClick(){
      selectRow(level, field, operator, value, matchcase, this);
  }
  
  cell = row.insertCell(-1);
  cell.level = level;
  cell.field = field;
  var temp = getFieldNameByType(field, g_displayedFields);
  cell.innerText = (level + ' ' + s_arrow + ' ' + temp);

  cell = row.insertCell(-1);
  cell.innerHTML = getOperatorImage(operator);

  cell = row.insertCell(-1);
  cell.innerText = value;

  cell = row.insertCell(-1);
  cell.innerHTML = getCaseImage(matchcase);
}

function editFilter()
{
  if(g_selectedRow != undefined)
  {
    var level = getSelectedLevel();
    var field = getSelectedField();
    var operator = getSelectedOperator();
    var value = idValue.value;
    var matchcase = idCase.checked;

    if (operator == s_exists || operator == s_existsNot)
    {
      value = '';
      matchcase = false;
    }

    var row = g_selectedRow;

    var temp = getFieldNameByType(field, g_displayedFields);
    row.cells[0].innerText = (level + ' ' + s_arrow + ' ' + temp);
    row.cells[0].level = level;
    row.cells[0].field = field;
    row.cells[1].innerHTML = getOperatorImage(operator);
    row.cells[2].innerText = value;
    row.cells[3].innerHTML = getCaseImage(matchcase);
  }
}

function removeFilter()
{
  if(g_selectedRow != undefined)
  {
    idTBody.deleteRow(g_selectedRow);
  }

  g_selectedRow = -1;

  idEdit.disabled = true;
  idRemove.disabled = true;
}

function doClose(ok)
{
  if (ok == true)
  {
    var result = getFilterConditions();
    g_args.getWindow().SetFilterConditions(result);

    window.returnValue = null;
  }
  else
  {
    window.returnValue = null;
  }

  window.close();
}

function doLoad()
{
  g_args = window.dialogArguments;

  initLevels();
  initFields();

  initFilterConditions(g_args.getFilter());
}
</script>
</head>

<body LEFTMARGIN="8" RIGHTMARGIN="8" TOPMARGIN="8" BOTTOMMARGIN="8"
      onload="doLoad()" onkeypress="doKeypress(event)">

<DIV CLASS="mainHeading"><%=lb_title%></DIV>

<table width="100%" cellpadding="0" cellspacing="2" style="margin-top: 6px">
  <col align="left" class="standardText">
  <col align="right" class="standardText">
  <tbody>
    <tr>
      <td>
	<%=bundle.getString("lb_the_field") %>
	<select id="idLevel" align="baseline" TABINDEX="1"
	  onchange="changeLevel()">
	  <option value="concept" level="concept"><%=bundle.getString("lb_concept") %></option>
	</select>
	<B style="font-size:12pt">&#x2192;</B>
	<select id="idField" align="baseline" TABINDEX="2">
	  <option value="_all_">
	</select>
      </td>
      <td><input id="idAdd" type="button" value="<%=bundle.getString("lb_add") %>" TABINDEX="6"
	onclick="addFilter()"></td>
    </tr>
    <tr>
      <td>
	<select id="idOperator" align="middle" TABINDEX="3"
	  onchange="changeOperator()">
	  <option value="contains" char="&#x2248;"><%=bundle.getString("lb_operator_contains") %></option>
	  <option value="containsnot" char="&#x2249;"><%=bundle.getString("lb_operator_donot_contains") %></option>
	  <option value="equals" char="&#x003d;"><%=bundle.getString("lb_operator_equals") %></option>
	  <option value="equalsnot" char="&#x2260;"><%=bundle.getString("lb_operator_donot_equals") %></option>
	  <!--
	  <option value="lessthan" char="&lt;"><%=bundle.getString("lb_operator_less_than") %></option>
	  <option value="greaterthan" char="&gt;"><%=bundle.getString("lb_operator_greater_than") %></option>
	  -->
	  <option value="exists" char="&#x2203;"><%=bundle.getString("lb_operator_exists") %></option>
	  <option value="existsnot" char="&#x2204;"><%=bundle.getString("lb_operator_donot_exist") %></option>
	</select>
	<input id="idValue" style="margin-top:5px" type="text"
	size="30" TABINDEX="4">
      </td>
      <td><input id="idEdit" type="button" value="<%=bundle.getString("lb_update") %>" TABINDEX="7"
	onclick="editFilter()" disabled></td>
    </tr>
    <tr>
      <td><%=bundle.getString("lb_match_case") %>
	<input id="idCase" type="checkbox" TABINDEX="5"></td>
      <td><input id="idRemove" type="button" value="<%=bundle.getString("lb_remove") %>" TABINDEX="8"
	onclick="removeFilter()" disabled></td>
    </tr>
  </tbody>
</table>

<!-- Border table -->
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1" WIDTH="100%" 
  style="margin-top: 10px">
  <TR>
    <TD BGCOLOR="#0C1476" ALIGN="CENTER" width="100%">
      <!-- Data header table -->
      <table BORDER="0" CELLSPACING="0" CELLPADDING="2"	width="100%"
	style="table-layout: fixed">
	<col WIDTH="175" class="tableHeadingBasic">
	<col WIDTH="20"  class="tableHeadingBasic" align="center">
	<col WIDTH="170" class="tableHeadingBasic">
	<col WIDTH="30"  class="tableHeadingBasic" align="left">
	<tbody>
	  <tr>
	    <TD style="color:white"><%=bundle.getString("lb_filter_field") %></TD>
	    <TD style="color:white"><%=bundle.getString("lb_filter_operator") %></TD>
	    <TD style="color:white"><%=bundle.getString("lb_filter_condition") %></TD>
	    <TD style="color:white"><%=bundle.getString("lb_filter_case") %></TD>
	  </tr>
	</tbody>
      </table>
      <!-- End Data header table -->
      <!-- Data table -->
      <div style="overflow: auto; height: 180px; background-color: white">
      <table id="idFilters" BORDER="0" CELLSPACING="0" CELLPADDING="2"
	STRIPED="false" SELECTABLE="true" SELECTION="true" width="100%">
	<col width="210" valign="top" class="standardText">
	<col width="20"  valign="top" align="center">
	<col width="200" valign="top" class="standardText">
	<col width="30"  valign="top" align="center" class="standardText">
	<tbody id="idTBody"></tbody>
      </table>
      </div>
      <!-- End Data table -->
    </td>
  </tr>
</table>
<!-- End Border table -->

<DIV ALIGN="CENTER" STYLE="margin-top: 6px;" class="standardText">
<INPUT TYPE=BUTTON VALUE="<%=lb_cancel%>" TABINDEX="9" ONCLICK="doClose(false);">
<INPUT TYPE=BUTTON VALUE="<%=lb_ok%>" TABINDEX="10" ONCLICK="doClose(true);">
</DIV>

</BODY>
</HTML>
