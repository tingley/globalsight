
if (navigator.appVersion.indexOf("Macintosh") == -1)
{
	if (navigator.appVersion.indexOf("MSIE") == -1)
	{
      self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/globalsight/includes/stylesIE.jsp">');
	}
	else
	{
      self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/globalsight/includes/stylesIE.jsp">');
	}
}
else 
{
	self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/globalsight/includes/stylesMac.css">');
}


