<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">

<xsl:template>
  <xsl:apply-templates select=".">

    <xsl:template><xsl:apply-templates/></xsl:template>

    <xsl:template match="hit">
    <LI>
      <SPAN CLASS="clsTerm" DIR="rtl">
        <xsl:attribute name="cid">
          <xsl:value-of select="./conceptid"/>
        </xsl:attribute>
        <xsl:attribute name="tid">
          <xsl:value-of select="./termid"/>
        </xsl:attribute>
        <xsl:value-of select="./term"/>
      </SPAN>
      <xsl:if test="./score[. != '100']">
        <SPAN CLASS="clsScore">(<xsl:value-of select="./score"/>%)</SPAN>
      </xsl:if>
    </LI>
    </xsl:template>

    <xsl:template match="hitlist">
      <UL>
      <xsl:apply-templates select="hits/hit"/>
      </UL>
    </xsl:template>

  </xsl:apply-templates>

  </xsl:template>
</xsl:stylesheet>
