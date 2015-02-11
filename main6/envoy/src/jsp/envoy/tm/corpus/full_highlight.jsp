<%@ page contentType="text/html; charset=UTF-8"session="false" 
%><%
//this stylesheet can show either list mode or plain text mode
long tuid = Long.valueOf(request.getParameter("tuid")).longValue();
boolean textMode = true;
if ("list".equals(request.getParameter("view")))
    textMode = false; //use list mode
%><?xml version="1.0" encoding="utf-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- outputs as a nice XHTML page -->
<xsl:output method="html" indent="yes" />
<xsl:template match="diplomat">
	<!-- match the root document -->
	<html>
	<head>
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache"></META>
    <META HTTP-EQUIV="Expires" CONTENT="0"></META>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8"></META>
	</head>
	<body>
    <% if (textMode) { %>
    <xsl:apply-templates/>
    <% } else {%>
    <OL><xsl:apply-templates/></OL>
    <% } %>
    </body>
    </html>    
</xsl:template>    

<xsl:template match="translatable">
    <xsl:choose>
		<xsl:when test="./segment[@tuid='<%=tuid%>']">
        <% if (textMode) { %>
            <a NAME="focus"></a><P><xsl:apply-templates/></P>
        <% } else { %>
            <BR></BR><a NAME="focus"></a><xsl:apply-templates/>
        <% } %>
		</xsl:when>
		<xsl:otherwise>
        <% if (textMode) { %>
    		<P><xsl:apply-templates/></P>
        <% } else { %>
    		<BR></BR><xsl:apply-templates/>
        <% } %>
		</xsl:otherwise>
     </xsl:choose>
</xsl:template>

<xsl:template match="segment">
		<xsl:choose>
		<xsl:when test="./@tuid='<%=tuid%>'">
        <% if (textMode) { %>
            <SPAN STYLE="background-color: yellow"><xsl:apply-templates/></SPAN>
        <% } else { %>
            <LI><SPAN STYLE="background-color: yellow"><xsl:apply-templates/></SPAN></LI>
        <% } %>
		</xsl:when>
		<xsl:otherwise>
        <% if (textMode) { %>
    		<font color="black"><xsl:apply-templates/></font>
        <% } else { %>
    		<LI><font color="black"><xsl:apply-templates/></font></LI>
        <% } %>
		</xsl:otherwise>
		</xsl:choose>		
</xsl:template>

<!-- skip btp and ept tags -->
<xsl:template match="bpt" />
<xsl:template match="ept" />
<xsl:template match="ph[@type='x-nbspace']">&#x00a0;</xsl:template>
<xsl:template match="ph[@type='x-img']"><img src="/globalsight/images/img.gif" align="absmiddle"/></xsl:template>
<xsl:template match="ph[@type='image']"><img src="/globalsight/images/img.gif" align="absmiddle"/></xsl:template>
<xsl:template match="ph" />
<xsl:template match="it" />
<xsl:template match="skeleton" />
<xsl:template match="localizable" />
</xsl:stylesheet>

