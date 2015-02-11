<%@page language="java" import="net.xde.SpellChecker.*,java.util.*,net.xde.xdeutil.*"%>
<%@page contentType="text/html"%>
<%
SpellChecker spl = new SpellChecker();
%>
<!--
$RCSfile: spellCheckStatistics.jsp,v $
$Revision: 1.1 $
$Date: 2009/04/14 15:42:52 $
-->
<html>
<HEAD>
<TITLE>Spell Checker Statistics</TITLE>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
</HEAD>

<body>
Debug Level: <%=spl.getSettings().getDebugLevel()%><br>
Total Spell Checker Requests: <%=spl.getSettings().getTotalRequests()%><br>
Suggestion Cache hit: <%=spl.getSettings().getDictionaryPool().getSuggestionHitRatio()%>%<br>
Correct Word Cache hit: <%=spl.getSettings().getDictionaryPool().getCorrectHitRatio()%>%<br>

Maximum Word Cache Entries per Language: <%=spl.getSettings().getMaxSuggestionCache()%><br>
Strip HTML: <%=spl.getSettings().getStripHTML()%><br>
Ignore Words with numerics: <%=spl.getSettings().getIgnoreNumbers()%><br>
Valid Alphanumeric: <%=spl.getSettings().getValidAlpha()%><br>
Maximum Suggestions to display: <%=spl.getSettings().getMaxSuggestions()%><br>
Attempt Typographical Suggestions: <%=spl.getSettings().getTypographicalSuggestions()%><br>
Attempt Phonetic Suggestions: <%=spl.getSettings().getPhoneticSuggestions()%><br>
Ignore words containing: <%=spl.getSettings().getIgnoreWordsContaining()%><br>
Ignore words in upper case: <%=spl.getSettings().isIgnoreUpperCase()%><br>


Loaded Object Versions: <%=spl.getSettings().getVersion()%><br>

</body>   
</html>
