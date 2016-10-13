<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Fri May 18 11:53:22 2001 CvdL
       Stylesheet for displaying and hyperlinking EnvoyConfig.xml. -->

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <body bgcolor="#FFFFFF">
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="text()"><xsl:value-of select="."
     disable-output-escaping="no" /></xsl:template>

  <xsl:template match="WebApplication">
    <H2>Web Application <xsl:value-of select="@webAppName"/></H2>
    <INPUT TYPE="BUTTON" VALUE="View XML Source" style="float:right"
      onClick="window.location='view-source:' + window.location.href" />
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="WebModule">
    <H3>Module: <xsl:value-of select="@webModuleName"/></H3>
    <xsl:apply-templates/>
    <HR/>
  </xsl:template>

  <xsl:template match="WebActivity">
    <TABLE width="80%">
      <TR>
        <TD COLSPAN="2">
          <U>Activity: <B><xsl:value-of select="@webActivityName"/></B></U>
        </TD>
        <TD></TD>
      </TR>
      <xsl:apply-templates/>
    </TABLE>
  </xsl:template>

  <xsl:template match="WebPage">
    <TR><TD width="20"></TD><TD>
    <TABLE>
      <TR>
        <TD COLSPAN="4">
          <A>
            <xsl:attribute name="name">
              <xsl:value-of select="@pageName"/>
            </xsl:attribute>
          </A>
          Page: <B><xsl:value-of select="@pageName"/></B>
        </TD>
        <TD/><TD/><TD/>
      </TR>
      <xsl:apply-templates/>
    </TABLE>
    </TD></TR>
  </xsl:template>

  <xsl:template match="PageLink">
    <TR>
      <TD width="20"></TD>
      <TD width="120"><xsl:value-of select="@linkName"/></TD>
      <TD>&#x2192;</TD>
      <TD>
        <A>
          <xsl:attribute name="href">
            #<xsl:value-of select="@destinationPageName"/>
          </xsl:attribute>
          <B><xsl:value-of select="@destinationPageName"/></B>
        </A>
      </TD>
    </TR>
  </xsl:template>
  
</xsl:stylesheet>
