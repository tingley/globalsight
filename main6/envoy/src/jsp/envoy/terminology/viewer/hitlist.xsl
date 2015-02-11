<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="hit">
    <LI>
      <SPAN CLASS="clsTerm">
        <xsl:attribute name="onclick">
          onHitClick(<xsl:value-of select="./conceptid"/>,<xsl:value-of select="./termid"/>)
        </xsl:attribute>
        <xsl:attribute name="cid">
          <xsl:value-of select="./conceptid"/>
        </xsl:attribute>
        <xsl:attribute name="tid">
          <xsl:value-of select="./termid"/>
        </xsl:attribute>
        <xsl:value-of select="./term"/>
      </SPAN>
      <!-- 
      <xsl:if test="./score[. != '100']">
        <SPAN CLASS="clsScore">(<xsl:value-of select="./score"/>%)</SPAN>
      </xsl:if>
       -->
    </LI>
    </xsl:template>

    <xsl:template match="hitlist">
      <UL>
      <xsl:apply-templates select="hits/hit"/>
      </UL>
    </xsl:template>


</xsl:stylesheet>
