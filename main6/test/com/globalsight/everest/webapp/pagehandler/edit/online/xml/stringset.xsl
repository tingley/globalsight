<?xml version="1.0"?>
<?xml-stylesheet type='text/xsl' href='stringset.xsl'?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" encoding="UTF-8" indent="no"/>
<xsl:template match="/">
<html>
<head><title>StringSet Viewer</title>
</head>
<body>
<table width="100%" border="1">
  <THEAD>
	<TR>
 	   <TD width="20%"><B>ID</B></TD>
 	   <TD width="80%"><B>String</B></TD>
	</TR>
  </THEAD>
  <TBODY>
	<xsl:for-each select="stringset/string">
	<TR>
	   <TD width="20%" style="bgcolor: gray"><xsl:value-of select="@id" /></TD>
	   <TD width="80%"><xsl:apply-templates /></TD>
	</TR>
	</xsl:for-each>
  </TBODY>
</table>
</body>
</html>
</xsl:template>
</xsl:stylesheet>