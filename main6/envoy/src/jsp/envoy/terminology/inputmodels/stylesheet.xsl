<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet is outdated as of Thu Feb 19 01:01:38 2004.
  It was superceded by code in entry.js that performs the same
  XML to HTML mapping this this stylesheet used to do.
-->
<!--<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- DEBUGGING: mark non-matched nodes with yellow background -->
<xsl:template match="*">
  <SPAN STYLE="background-color:yellow">
    <xsl:attribute name="title">&lt;<xsl:value-of select="name()"/>&gt;</xsl:attribute>
    <xsl:apply-templates/>
  </SPAN>
</xsl:template>

<xsl:template match="text()"><xsl:value-of select="."/></xsl:template>

<xsl:template match="transacGrp">
  <SPAN class="transacGrp">
    <xsl:attribute name="type"><xsl:value-of select="transac/@type"/></xsl:attribute>
    <xsl:attribute name="author"><xsl:value-of select="transac"/></xsl:attribute>
    <xsl:attribute name="date"><xsl:value-of select="date"/></xsl:attribute>
    <SPAN CLASS="transaclabel"><xsl:value-of select="./transac/@type"/></SPAN>
    <SPAN CLASS="transacvalue"><xsl:value-of select="date"/> 
    (<xsl:value-of select="transac"/>)</SPAN>
  </SPAN>
</xsl:template>

<xsl:template match="noteGrp">
  <DIV class="fieldGrp" ondblClick="doEdit(this);">
    <xsl:apply-templates select="note"/>
  </DIV>
</xsl:template>

<xsl:template match="note">
  <SPAN CLASS="fieldlabel" unselectable="on" type="Note">Note</SPAN>
  <xsl:text> </xsl:text>
  <SPAN CLASS="fieldvalue"><xsl:apply-templates/></SPAN>
</xsl:template>

<xsl:template match="sourceGrp">
  <DIV CLASS="fieldGrp" ondblClick="doEdit(this);">
    <xsl:apply-templates select="source"/>
    <xsl:apply-templates select="noteGrp"/>
  </DIV>
</xsl:template>

<xsl:template match="source">
  <SPAN CLASS="fieldlabel" unselectable="on" type="Source">Source</SPAN>
  <xsl:text> </xsl:text>
  <SPAN CLASS="fieldvalue"><xsl:value-of select="."/></SPAN>
</xsl:template>

<xsl:template match="descripGrp">
  <DIV CLASS="fieldGrp" ondblclick="doEdit(this);">
    <xsl:apply-templates select="descrip"/>
    <xsl:apply-templates select="sourceGrp"/>
    <xsl:apply-templates select="noteGrp"/>
  </DIV>
</xsl:template>

<xsl:template match="descrip">
  <SPAN CLASS="fieldlabel" unselectable="on"><xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute><xsl:value-of select="@type"/></SPAN>
  <xsl:text> </xsl:text>
  <SPAN CLASS="fieldvalue"><xsl:apply-templates/></SPAN>
</xsl:template>

<xsl:template match="language">
  <SPAN class="languagelabel">Language</SPAN>
  <xsl:text> </xsl:text>
  <SPAN class="language" unselectable="on"><xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute><xsl:value-of select="@name"/></SPAN>
</xsl:template>

<xsl:template match="languageGrp">
  <DIV class="languageGrp">
    <SPAN class="fakeLanguageGrp">
      <xsl:apply-templates select="language"/>
    </SPAN>
    
    <xsl:apply-templates select="descripGrp"/>
    <xsl:apply-templates select="sourceGrp"/>
    <xsl:apply-templates select="noteGrp"/>
    
    <xsl:apply-templates select="termGrp"/>
  </DIV>
</xsl:template>

<xsl:template match="termGrp">
  <DIV class="termGrp">
    <DIV class="fakeTermGrp" ondblclick="doEdit(this);">
      <xsl:apply-templates select="term"/>
    </DIV>
    
    <xsl:apply-templates select="descripGrp"/>
    <xsl:apply-templates select="sourceGrp"/>
    <xsl:apply-templates select="noteGrp"/>
  </DIV>
</xsl:template>

<xsl:template match="term">
  <xsl:choose>
    <xsl:when test="position()=1">
      <SPAN class="termlabel">Main Term</SPAN>
    </xsl:when>
    <xsl:otherwise>
      <SPAN class="termlabel">Synonym</SPAN>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text> </xsl:text>
  <SPAN class="term"><xsl:value-of select="."/></SPAN>
</xsl:template>

<xsl:template match="conceptGrp">
  <DIV class="conceptGrp">
    <SPAN class="fakeConceptGrp">
      <SPAN class="conceptlabel">Input Model</SPAN>
      <xsl:text> </xsl:text>
      <SPAN class="concept"></SPAN>
    </SPAN>
    
    <xsl:apply-templates select="transacGrp"/>
    <xsl:apply-templates select="descripGrp"/>
    <xsl:apply-templates select="sourceGrp"/>
    <xsl:apply-templates select="noteGrp"/>
    
    <xsl:apply-templates select="languageGrp"/>
  </DIV>
</xsl:template>

</xsl:stylesheet>
