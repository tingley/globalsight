<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet is outdated as of Thu Feb 19 01:01:38 2004.
  It was superceded by code in entry.js that performs the same
  XML to HTML mapping this this stylesheet used to do.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">
  
<xsl:template>
  <DIV STYLE="margin-top:1em; margin-bottom:1em;">

  <xsl:apply-templates select=".">

    <xsl:template><xsl:apply-templates/></xsl:template>

    <!-- DEBUGGING: mark non-matched nodes with yellow background -->
    <xsl:template match="*">
       <SPAN STYLE="background-color:yellow">
         <xsl:attribute name="title">&lt;<xsl:node-name/>&gt;</xsl:attribute>
         <xsl:apply-templates/>
       </SPAN>
    </xsl:template>

    <xsl:template match="text()"><xsl:value-of/></xsl:template>

    <xsl:template match="xref">
      <A STYLE="color:forestgreen; cursor:hand" onclick='showlink(this)'>
      <xsl:choose>
        <xsl:when test="@Clink">
          <xsl:attribute name="class">Clink</xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="@Clink"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@Tlink">
          <xsl:attribute name="class">Tlink</xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="@Tlink"/>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:value-of/>
      </A>
    </xsl:template>

    <xsl:template match="date">
      <SPAN CLASS="date">Date:</SPAN>
      <SPAN CLASS="dateVal"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="note">
      <SPAN CLASS="note">Note:</SPAN>
      <SPAN CLASS="noteVal"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="transacGrp">
      <DIV>
        <xsl:apply-templates select="transac"/>
        <SPAN STYLE="margin-left:10pt;">
          <xsl:apply-templates select="date"/>
        </SPAN>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="note"/>
        </DIV>
      </DIV>
    </xsl:template>

    <xsl:template match="transac">
      <SPAN CLASS="transac">
        <xsl:choose>
          <xsl:when test="@type[. = 'origination']">
            Created By:
          </xsl:when>
          <xsl:when test="@type[. = 'modification']">
            Modified By:
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@type"/>
          </xsl:otherwise>
        </xsl:choose>
      </SPAN>
      <SPAN CLASS="transacVal"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="sourceGrp">
      <DIV>
        <xsl:apply-templates select="source"/>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="note"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="transacGrp"/>
        </DIV>
      </DIV>
    </xsl:template>

    <xsl:template match="source">
      <SPAN CLASS="source">Source:</SPAN>
      <SPAN CLASS="sourceVal"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="descripGrp">
      <DIV>
        <xsl:apply-templates select="descrip"/>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="note"/>
        </DIV>
        <DIV STYLE="margin-left:15pt;">
          <xsl:apply-templates select="source|sourceGrp"/>
        </DIV>
        <DIV STYLE="margin-left:15pt;">
          <xsl:apply-templates select="transacGrp"/>
        </DIV>
      </DIV>
    </xsl:template>

    <xsl:template match="descrip">
      <SPAN CLASS="descrip"><xsl:value-of select="@type"/>:</SPAN>
      <SPAN CLASS="descripVal"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="languageGrp">
      <DIV STYLE="margin-left:10pt; margin-top: 4pt; ">
        <xsl:choose>
          <xsl:when test="language[@source-lang]">
            <xsl:attribute name="class">sourceLang</xsl:attribute>
          </xsl:when>
          <xsl:when test="language[@target-lang]">
            <xsl:attribute name="class">targetLang</xsl:attribute>
          </xsl:when>
        </xsl:choose>

        <DIV CLASS="lang"><xsl:value-of select="language/@name"/></DIV>

        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="transacGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="descrip|descripGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="source|sourceGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="note"/>
        </DIV>

        <xsl:apply-templates select="termGrp[term/@search-term]"/>
        <xsl:apply-templates select="termGrp[not(term/@search-term)]"/>
      </DIV>
    </xsl:template>

    <xsl:template match="termGrp">
      <DIV STYLE="margin-left:10pt;">
        <xsl:apply-templates select="term"/>

        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="transacGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="descrip|descripGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="source|sourceGrp"/>
        </DIV>
        <DIV STYLE="margin-left:10pt;">
          <xsl:apply-templates select="note"/>
        </DIV>
      </DIV>
    </xsl:template>

    <xsl:template match="term[@search-term]">
      <SPAN CLASS="searchterm"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="term[not(@search-term)]">
      <SPAN CLASS="term"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="conceptGrp">
      <DIV STYLE="margin-left:10pt; margin-bottom: 5pt;">
        <SPAN CLASS="concept">
          Entry Number: <xsl:value-of select="concept"/>
        </SPAN>
      </DIV>

      <DIV STYLE="margin-left:10pt; margin-bottom: 2pt;">
        <xsl:apply-templates select="transacGrp"/>
      </DIV>
      <DIV STYLE="margin-left:10pt;">
        <xsl:apply-templates select="descrip|descripGrp"/>
      </DIV>
      <DIV STYLE="margin-left:10pt;">
        <xsl:apply-templates select="source|sourceGrp"/>
      </DIV>
      <DIV STYLE="margin-left:10pt;">
        <xsl:apply-templates select="note"/>
      </DIV>

      <xsl:apply-templates select="languageGrp[language/@source-lang]"/>
      <xsl:apply-templates select="languageGrp[language/@target-lang]"/>
      <xsl:apply-templates select="languageGrp[not(language/@source-lang) and not(language/@target-lang)]"/>
    </xsl:template>

  </xsl:apply-templates>

  </DIV>
  </xsl:template>
</xsl:stylesheet>
