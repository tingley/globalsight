<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

<xsl:template match="/ | @* | node()">
  <xsl:copy><xsl:apply-templates select="@* | node()"/></xsl:copy>
</xsl:template>

<xsl:template match="text()"><xsl:value-of disable-output-escaping="yes" select="."/></xsl:template>

</xsl:stylesheet>
