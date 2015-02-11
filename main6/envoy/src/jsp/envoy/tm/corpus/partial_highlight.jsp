<%@ page contentType="text/html; charset=UTF-8"session="false" 
%><%
String tuid = request.getParameter("tuid");
%><?xml version="1.0" encoding="utf-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" />
<!-- output each segment on a line by itself -->
<xsl:template match="segment">
		<xsl:choose>
		<xsl:when test="./@tuid='<%=tuid%>'">
            <SPAN STYLE="background-color: yellow">
            <xsl:apply-templates/>
            </SPAN>
		</xsl:when>
		<xsl:otherwise>
		<font color="black">
        <xsl:apply-templates/>
        </font>
		</xsl:otherwise>
		</xsl:choose>
 </xsl:template>

<!-- skip btp and ept tags -->
<xsl:template match="bpt"/>
<xsl:template match="ept"/>
<xsl:template match="ph[@type='x-nbspace']">&#x00a0;</xsl:template>
<xsl:template match="ph[@type='x-img']"><img src="/globalsight/images/img.gif" align="absmiddle"/></xsl:template>
<xsl:template match="ph[@type='image']"><img src="/globalsight/images/img.gif" align="absmiddle"/></xsl:template>
<xsl:template match="ph" />
<xsl:template match="it" />
</xsl:stylesheet>
