
if (navigator.appVersion.indexOf("Macintosh") == -1)
{
	if (navigator.appVersion.indexOf("MSIE") == -1)
	{
      self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/gsVignette/Includes/stylesNN.css">');
	}
	else
	{
      self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/gsVignette/Includes/stylesIE.css">');
	}
}
else 
{
	self.document.write('<LINK REL=STYLESHEET TYPE="text/css" HREF="/gsVignette/Includes/stylesMac.css">');
}
