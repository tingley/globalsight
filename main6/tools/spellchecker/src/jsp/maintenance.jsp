<%@page language="java"
  contentType="text/html; charset=UTF-8"
  import="spell.*,java.util.*,"
%>
<%
ArrayList words = null;

String action = request.getParameter("action");
String dictionaryName = request.getParameter("dictionaryName");
String addword = request.getParameter("addword");
String deleteword = request.getParameter("deleteword");

if (action == null)
{
    action = "";
}

////// out.println("action=" + action);

if (action.equals("Load") && dictionaryName != null)
{
    SpellIndex index = SpellIndexList.getSpellIndex(dictionaryName);
    words = index.getWords();
}
else if (action.equals("Delete") && dictionaryName != null)
{
    SpellIndexList.deleteSpellIndex(dictionaryName);
    dictionaryName = "";
}
else if(action.equals("Add") && dictionaryName != null && addword != null)
{
    SpellIndex index = SpellIndexList.getSpellIndex(dictionaryName);
    index.addWord(addword);
    words = index.getWords();
}
else if(action.equals("Delete") && dictionaryName != null && deleteword != null)
{
    SpellIndex index = SpellIndexList.getSpellIndex(dictionaryName);
    index.removeWord(deleteword);
    words = index.getWords();
}
else if(action.equals("Refresh"))
{
  // do nothing, refresh spell index list
}
%>
<html>
<HEAD>
<TITLE>Dictionary Maintenance</TITLE>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<style type="text/css">
body {
	font-family: Arial Unicode MS, Verdana, Arial, Helvetica, sans-serif;
	font-size: 10pt;
	font-style: normal;
	background-color: white;
}
</style>
<script>
function changeDictionary()
{
  var dict = form1.select.options[form1.select.selectedIndex].value;
  form1.dictionaryName.value = dict;
}

function selectDictionary(dict)
{
  var options = form1.select.options;
  for (i = 0; i < options.length; ++i)
  {
    if (options(i).value == dict)
    {
      form1.select.selectedIndex = i;
      break;
    }
  }
}


function init()
{
  try
  {
    if (eval("<%=dictionaryName%>" != ""))
    {
      selectDictionary("<%=dictionaryName%>");
    }
    else
    {
      changeDictionary();
    }
  }
  catch (exception)
  {
  }
}
</script>
</HEAD>

<body onload="init()">

<P><a href="../index.html">Go back to index</a></P>

<form name="form1" method="post" action="maintenance.jsp">
<table width="100%" border="0">
  <tr>
    <th width="35%" align="right" valign="top">
      Known Dictionaries:
    </th>
    <td align="left" valign="top">
      <select name="select" onchange="changeDictionary()">
      <%
      ArrayList indexes = SpellIndexList.getSpellIndexes();
      Collections.sort(indexes);
      for (int i = 0, max = indexes.size(); i < max; i++)
      {
        String name = (String)indexes.get(i);
	%>
	<option value="<%=name%>"><%=name%></option>
	<%
      }
      %>
      </select>
      &nbsp;
      <input type="submit" name="action" value="Refresh">
    </td>
  </tr>
  <tr> 
    <th align="right" valign="top">
      Dictionary Name:
    </th>
    <td align="left" valign="top"> 
      <input type="text" name="dictionaryName" maxlength="25"
      value="<%=dictionaryName != null ? dictionaryName : ""%>">
      <input type="submit" name="action" value="Load">
      &nbsp;
      <input type="submit" name="action" value="Delete">
      <BR>
      If a non-existent dictionary is loaded, it will be created.
    </td>
  </tr>
  <tr> 
    <th align="right" valign="top">
      Dictionary Words:
    </th>
    <td align="left" valign="top"> 
      <textarea cols="30" rows="15"
      ><%
      if (words != null)
      {
        for (int i = 0, max = words.size(); i < max; i++)
        {
          String temp = (String)words.get(i);
          out.println(temp);
        }
      }
      %></textarea>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">
      Add Word:
    </th>
    <td align="left" valign="top">
      <input type="text" name="addword" width="100" value="">
      <input type="submit" name="action" value="Add">
    </td>
  </tr>
  <tr>
    <th align="right" valign="top">
      Delete Word:
    </th>
    <td align="left" valign="top">
      <input type="text" name="deleteword" width="100" value="">
      <input type="submit" name="action" value="Delete">
    </td>
  </tr>
</table>
</form>
</body>   
</html>

