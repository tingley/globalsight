<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Formats GXML version 1.0 and 2.0 nicely on the screen.
To use, include this instruction in your GXML file.
<?xml-stylesheet type='text/xsl' href='diplomat.xsl'?>
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:SYM="mailto:nils@globalsight.com">
<xsl:output method="html" version="4.0"/>

<!-- <SYM:W>Ö</SYM:W> <SYM:W>Õ</SYM:W> -->

  <!-- unmatched tags catcher -->
  <xsl:template match="*">
    <SPAN STYLE="background-color:yellow">
    <xsl:attribute name="title">&lt;<xsl:value-of select="name()"/>&gt;
    </xsl:attribute>
    <xsl:apply-templates/>
    </SPAN>
  </xsl:template>

  <xsl:template match="/"><xsl:apply-templates/></xsl:template>
	
  <xsl:template match="text()"><xsl:value-of select="."
     disable-output-escaping="no" /></xsl:template>

  <xsl:template match="diplomat">
    <html xmlns:SYM="mailto:nils@globalsight.com">
    <head>
      <STYLE><![CDATA[
      @media all 
      {
      BODY              { font: verdana; }
      .clsSkeleton	{ color: black; }

      .clsTranslatable	          { background-color: dodgerblue; }
      .clsTranslatable .clsTMX	  { background-color: slategray; }
      .clsTranslatable .clsTMXtag { background-color: black; color: white; }
      .clsTranslatable .clsTMXsub { background-color: cyan; }
      .clsTranslatable .clsTMXsubLocalizable  
      		{ 
		background-color: orange; 
		}
      .clsTranslatable .clsTMXsubTranslatable 
      		{ 
		background-color: navy; color: white; 
		}
      .clsTMXisolated { background-color: red; color: white; }

      .clsLocalizable	          { background-color: gold; }
      .clsLocalizable .clsTMX	  { background-color: slategray; }
      .clsLocalizable .clsTMXtag  { background-color: black; color: white; }
      .clsLocalizable .clsTMXsub  { background-color: cyan; }

      .clsPresentation	{ background-color: yellow; }
      .clsGSA		{ background-color: palegreen; }

      #colordemo        { border: 1pt solid black; 
                          padding-left: 0.75ex; padding-right: 0.75ex; }
      .clsError         { background-color: red; }

      SYM\:W            { font-family: 'wingdings'; border: none; }
      SYM\:W3           { font-family: 'wingdings 3'; border: none; }
      }
      ]]></STYLE>
    </head>
    <body>
      <H2>Diplomat file</H2>
      <P>
      <INPUT TYPE="BUTTON" VALUE="View XML Source" style="float:right"
       onClick="window.location='view-source:' + window.location.href" />
      Version: 
         <xsl:choose>
	  <xsl:when test="@version">
	    <xsl:value-of select="@version"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <span class="clsError">[required but missing]</span>
	  </xsl:otherwise>
	 </xsl:choose><BR/>
         Locale:
         <xsl:choose>
	  <xsl:when test="@locale">
	    <xsl:value-of select="@locale"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <span class="clsError">[required but missing]</span>
	  </xsl:otherwise>
	 </xsl:choose><BR/>
         Datatype:
         <xsl:choose>
	  <xsl:when test="@datatype">
	    <xsl:value-of select="@datatype"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <span class="clsError">[required but missing]</span>
	  </xsl:otherwise>
	 </xsl:choose><BR/>
         Target encoding:
         <xsl:choose>
	  <xsl:when test="@targetEncoding">
	    <xsl:value-of select="@targetEncoding"/>
	  </xsl:when>
	  <xsl:otherwise>[N/A]
	  </xsl:otherwise>
	 </xsl:choose><BR/>
         Word count:
         <xsl:choose>
	  <xsl:when test="@wordcount">
	    <xsl:value-of select="@wordcount"/>
	  </xsl:when>
	  <xsl:otherwise>[N/A]
	  </xsl:otherwise>
	 </xsl:choose><BR/>
      </P>

      <P>Color codes:</P>
      <SPAN class="clsSkeleton"     id="colordemo">Skeleton</SPAN>
      <SPAN class="clsTranslatable" id="colordemo">Translatable text</SPAN> 
      <SPAN class="clsLocalizable"  id="colordemo">Localizable text</SPAN> 

      <SPAN class="clsTranslatable">
       <SPAN class="clsTMXtag" id="colordemo">TMX tag</SPAN>
       <SPAN class="clsTMX"    id="colordemo">TMX tag content</SPAN>
       <BR/>
       <SPAN class="clsTMXsub"  id="colordemo">Subflow</SPAN>
       <SPAN class="clsTMXsubTranslatable" id="colordemo">Translatable 
       subflow</SPAN>
       <SPAN class="clsTMXsubLocalizable"  id="colordemo">Localizable 
       subflow</SPAN>
       <SPAN class="clsTMXisolated" id="colordemo">Isolated tag</SPAN>
      </SPAN>

      <SPAN class="clsGSA" id="colordemo">GSA tag</SPAN> 

      <UL>
      <xsl:apply-templates/>
      </UL>
    </body>
    </html>
  </xsl:template>

  <xsl:template match="skeleton">
    <LI><PRE class="clsSkeleton"><xsl:apply-templates/></PRE></LI>
  </xsl:template>

  <xsl:template match="sub/skeleton">
    <SPAN class="clsSkeleton"><xsl:apply-templates/></SPAN>
  </xsl:template>

  <xsl:template match="translatable">
    <!-- @wordcount -->
    <LI><DIV class="clsTranslatable"><xsl:attribute name="title">Block <xsl:value-of select="@blockId"/><xsl:if test="@wordcount">, <xsl:value-of select="@wordcount"/> <xsl:choose>
      <xsl:when test="@wordcount = 1"> word</xsl:when>
      <xsl:otherwise> words</xsl:otherwise>
      </xsl:choose></xsl:if></xsl:attribute><xsl:apply-templates/></DIV></LI>
  </xsl:template>

  <xsl:template match="localizable">
    <!-- @datatype -->
    <LI><PRE class="clsLocalizable"><xsl:attribute name="title">Block <xsl:value-of select="@blockId"/><xsl:if test="@type">, type=<xsl:value-of select="@type"/></xsl:if></xsl:attribute><xsl:apply-templates/></PRE></LI>
  </xsl:template>

  <xsl:template match="presentation">
    <LI><DIV class="clsPresentation"><xsl:apply-templates/></DIV></LI>
  </xsl:template>

  <xsl:template match="gsa">
    <LI><DIV class="clsGSA"><SYM:W3>&#186;</SYM:W3> GSA <xsl:if test="@add">add="<xsl:value-of select="@add"/>"</xsl:if><xsl:if test="@delete"> delete="<xsl:value-of select="@delete"/>"</xsl:if><xsl:if test="@extract"> extract="<xsl:value-of select="@extract"/>"</xsl:if><xsl:if test="@added"> added="<xsl:value-of select="@added"/>"</xsl:if><xsl:if test="@deleted"> deleted="<xsl:value-of select="@deleted"/>"</xsl:if>
</DIV></LI>
  <xsl:apply-templates/>
  <LI><DIV class="clsGSA"><SYM:W3>&#185;</SYM:W3> GSA</DIV></LI>
  </xsl:template>

<!--  <xsl:template match="segment"><SYM:W>Ö</SYM:W><xsl:apply-templates/><SYM:W>Õ</SYM:W></xsl:template> -->

  <xsl:template match="segment"><SYM:W><xsl:attribute name="TITLE">Segment <xsl:if test="@segmentId"><xsl:value-of select="@segmentId"/></xsl:if>
      <xsl:if test="@wordcount">, <xsl:value-of select="@wordcount"/> 
      <xsl:choose>
      <xsl:when test="@wordcount = 1"> word</xsl:when>
      <xsl:otherwise> words</xsl:otherwise>
      </xsl:choose></xsl:if>
    </xsl:attribute>Ö</SYM:W><xsl:apply-templates/><SYM:W>Õ</SYM:W></xsl:template>

  <xsl:template match="sub">
    <SPAN class="clsTMXtag">&lt;sub<xsl:if test="@locType"> locType="<xsl:value-of select="@locType"/>"</xsl:if><xsl:if test="@type"> type="<B><xsl:value-of select="@type"/></B>"</xsl:if><xsl:if test="@datatype"> datatype="<B><xsl:value-of select="@datatype"/></B>"</xsl:if><xsl:if test="@i"> i="<xsl:value-of select="@i"/>"</xsl:if>&gt;</SPAN>
    <SPAN>
    <xsl:attribute name="class">
      <xsl:choose>
      <xsl:when test="@locType='translatable'">clsTMXsubTranslatable</xsl:when>
      <xsl:when test="@locType='localizable'">clsTMXsubLocalizable</xsl:when>
      <xsl:otherwise>clsTMXsub</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/sub&gt;</SPAN>
  </xsl:template>

  <xsl:template match="ut">
    <SPAN class="clsTMXtag">&lt;ut<xsl:if test="@x"> x="<xsl:value-of select="@x"/>"</xsl:if>&gt;</SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/ut&gt;</SPAN>
  </xsl:template>

  <xsl:template match="it">
    <BR/><SPAN class="clsTMXisolated">&lt;it<xsl:if test="@type"> type="<xsl:value-of select="@type"/>"</xsl:if><xsl:if test="@pos"> pos="<xsl:value-of select="@pos"/>"</xsl:if><xsl:if test="@x"> x="<xsl:value-of select="@x"/>"</xsl:if><xsl:if test="@i"> i="<xsl:value-of select="@i"/></xsl:if>&gt;</SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXisolated">&lt;/it&gt;</SPAN>
  </xsl:template>

  <xsl:template match="ph">
    <BR/><SPAN class="clsTMXtag">&lt;ph<xsl:if test="@type"> type="<xsl:value-of select="@type"/>"</xsl:if><xsl:if test="@i"> i="<xsl:value-of select="@i"/>"</xsl:if><xsl:if test="@x"> x="<xsl:value-of select="@x"/>"</xsl:if>&gt;</SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/ph&gt;</SPAN>
  </xsl:template>

<!--
  <xsl:template match="ph">
    <SPAN class="clsTMXtag"><SYM:W TITLE="&lt;PH&gt;">Ö</SYM:W></SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag"><SYM:W TITLE="&lt;/PH&gt;">Õ</SYM:W></SPAN>
  </xsl:template>
-->

  <xsl:template match="bpt">
    <BR/><SPAN class="clsTMXtag">&lt;bpt<xsl:if test="@type"> type="<xsl:value-of select="@type"/>"</xsl:if><xsl:if test="@i"> i="<xsl:value-of select="@i"/>"</xsl:if><xsl:if test="@x"> x="<xsl:value-of select="@x"/>"</xsl:if><xsl:if test="@erasable"> erasable="<xsl:value-of select="@erasable"/>"</xsl:if>&gt;</SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/bpt&gt;</SPAN>
  </xsl:template>

  <xsl:template match="ept">
    <BR/><SPAN class="clsTMXtag">&lt;ept<xsl:if test="@type"> type="<xsl:value-of select="@type"/>"</xsl:if><xsl:if test="@i"> i="<xsl:value-of select="@i"/>"</xsl:if><xsl:if test="@x"> x="<xsl:value-of select="@x"/>"</xsl:if>&gt;</SPAN>
    <SPAN class="clsTMX"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/ept&gt;</SPAN>
  </xsl:template>

  <xsl:template match="hi">
    <SPAN class="clsTMXtag">&lt;hi&gt;</SPAN>
    <SPAN style="color:red"><xsl:apply-templates/></SPAN>
    <SPAN class="clsTMXtag">&lt;/hi&gt;</SPAN>
  </xsl:template>

</xsl:stylesheet>
