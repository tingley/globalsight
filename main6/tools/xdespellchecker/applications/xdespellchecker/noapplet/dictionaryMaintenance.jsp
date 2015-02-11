<%@page language="java"
  contentType="text/html; charset=UTF-8"
  import="net.xde.SpellChecker.*,java.util.*,net.xde.xdeutil.*"
%>
<!--
$RCSfile: dictionaryMaintenance.jsp,v $
$Revision: 1.1 $
$Date: 2009/04/14 15:42:52 $
-->
<html>
<HEAD>
<TITLE>XDE Spell Checker Dictionary Maintenance</TITLE>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<style type="text/css">
body {
	font-family: Arial Unicode MS, Verdana, Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-style: normal;
	background-color: #FFFFFF;
	border-top-width: thin;
	border-right-width: thin;
	border-bottom-width: thin;
	border-left-width: thin;
}
</style>
</HEAD>

<body>
<%
String action = null;
SpellChecker spl = new SpellChecker();
String defaultFilePath = null;
String dictionaryName = null;
defaultFilePath = request.getParameter("defaultFilePath");
if (defaultFilePath == null)
{
    defaultFilePath = spl.getSettings().getUserDir();
}

if (defaultFilePath == null)
{
    defaultFilePath = System.getProperty("user.dir");
}
if (defaultFilePath.indexOf("WEB-INF")>-1)
{
	response.getWriter().println("WEB-INF, Invalid Action!");
	return;
}
action = request.getParameter("action");
dictionaryName = request.getParameter("dictionaryName");
if (dictionaryName == null)
{
    dictionaryName = "generic";
}

String dictionaryWords = request.getParameter("dictionaryWords");
if (dictionaryWords == null || dictionaryWords.equals(""))
{
    dictionaryWords = "SampleWord1";
}
spl.getSettings().setUserDir(defaultFilePath);
if (action == null)
{
    action = "Load";
}

if (action.equals("Load"))
{
    dictionary dic = spl.getSettings().getDictionaryPool().getDictionary(dictionaryName);
    if (dic != null)
    {
      dictionaryWords = dic.dictionaryWords();
    }
    else
    {
      dictionaryWords = "DICT DOES NOT EXIST";
    }
}
else if(action.equals("Save"))
{
    dictionary dic = spl.getSettings().getDictionaryPool().getDictionary(dictionaryName);
    dic.addWords(dictionaryWords,"\r\n");
    dic.save();
}
else
{
    response.getWriter().println("Invalid Action!");
    return;
}
%>
<%=spl.getSettings().getUserDir()%>
<form name="form1" method="post" action="dictionaryMaintenance.jsp">
<table width="100%" border="0">
  <tr> 
    <th width="17%" nowrap align="right" valign="top">Dictionary File Path :</th>
    <td width="83%" align="left" valign="top"> 
      <input type="text" name="defaultFilePath" size="80" maxlength="500"
      value="<%=defaultFilePath%>">
    </td>
  </tr>
  <tr> 
    <th width="17%" nowrap align="right" valign="top">Known Dictionaries:</th>
    <td width="83%" align="left" valign="top">
      <!-- Full list of available dictionaries as of Thu Dec 16 22:21:51 2004.
      This list is for testing only, we cannot ship all of them. -->
      
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">africansouth</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">afrikaan</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">algae</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">arabic</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">argentina</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">australian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">bolivian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">bolivian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">brazilianportuguese</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">canadian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">canadianfrench</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">chilean</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">chinese</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">colombian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">colombian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">croatian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">czech</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">czech2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">danish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">danish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">dutch</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">dutch2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ecuadoran</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">estonian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">estonian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">finnish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">finnish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">french</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">french2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">german</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">greek</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">greek2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">hungarian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">hungarian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">italian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">italian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japanese2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japaneseenglish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japaneseroman</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japanesesanscript</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">mexican</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">norwegian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">norwegian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">paraguayan</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">peruvian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">polish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">portuguese</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">russian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">spanish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">spanish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">swahili</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">swedish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">swiss2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">turkey2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">turkish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ukenglish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ukenglish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ukrainian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglishfirstname</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglishlastname</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">venezuelan2002</span>

<!-- Thu Dec 16 22:20:37 2004
 These are the ones we are entitled to ship according to the licence agreement:

	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">africansouth</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">arabic</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">argentina</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">australian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">bolivian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">brazilianportuguese</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">canadian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">canadianfrench</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">chinese</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">colombian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">czech</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">danish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">dutch</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">dutch2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">estonian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">finnish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">french</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">french2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">german</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">german2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">greek</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">italian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">italian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japaneseroman</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">japanesesanscript</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">norwegian</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">polish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">portuguese</span>
 	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">russian</span>
 	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">russian2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">spanish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">spanish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">swedish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ukenglish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">ukenglish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglish</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglish2002</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglishfirstname</span>
	<span onclick="dictionaryName.value = this.innerText" style="cursor:hand">usenglishlastname</span>
-->
    </td>
  </tr>
  <tr> 
    <th width="17%" nowrap align="right" valign="top">Dictionary Name :</th>
    <td width="83%" align="left" valign="top"> 
      <input type="text" name="dictionaryName" maxlength="25"
      value="<%=dictionaryName%>">
      <input type="submit" name="action" value="Load">
    </td>
  </tr>
  <tr> 
    <th width="17%" nowrap align="right" valign="top" height="243">
      Dictionary Words:
    </th>
    <td width="83%" align="left" valign="top" height="243"> 
      <textarea name="dictionaryWords" cols="30" rows="15"
      ><%=dictionaryWords%></textarea>
    </td>
  </tr>
  <tr>
    <th width="17%" nowrap align="right" valign="top">
      <input type="submit" name="action" value="Save">
    </th>
    <td width="83%" align="left" valign="top">&nbsp;</td>
  </tr>
</table>
</form>
</body>   
</html>

