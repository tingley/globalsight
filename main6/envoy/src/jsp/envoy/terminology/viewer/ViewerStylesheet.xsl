<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet is outdated as of Thu Feb 19 01:01:38 2004.
  It was superceded by code in entry.js that performs the same
  XML to HTML mapping this this stylesheet used to do.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">

<xsl:script language="JScript"><![CDATA[
// ViewerStylesheet.xsl 

// Only this script section needs to be localized. 
// The rest of the stylesheet pulls l10n strings from this script.

// Display strings for languages, if desired. No-op for English.
function mapLanguage(node)
{
  var lang = node.attributes.getNamedItem("name").text;

  return lang;

  if (lang == "Danish")  return "Danish";
  if (lang == "German")  return "Deutsch";
  if (lang == "Greek")   return "Ellenike";
  if (lang == "English") return "English";
  if (lang == "Spanish") return "Espanol";
  if (lang == "Finnish") return "Suomi";
  if (lang == "French")  return "Francais";
  if (lang == "Italian") return "Italian";
  if (lang == "Latin")   return "Latin";
  if (lang == "Dutch")   return "Nederlands";
  if (lang == "Portuguese") return "Portugues";
  if (lang == "Swedish") return "Swedish";

  return lang;
}

function mapField(node)
{
  var type = node.attributes.getNamedItem("type").text;

  // Keep this list in sync with objects.js.

  var t = type.toLowerCase();

  if (t == 'status') return "Status";
  if (t == 'domain') return "Domain";
  if (t == 'project') return "Project";
  if (t == 'definition') return "Definition";
  if (t == 'usage') return "Usage";
  if (t == 'type') return "Term Type";
  if (t == 'pos') return "Part Of Speech";
  if (t == 'gender') return "Gender";
  if (t == 'number') return "Number";
  if (t == 'example') return "Example";

  return type;
}

function mapTransac(node)
{
  var type = node.selectSingleNode('transac').attributes.getNamedItem("type").text;

  if (type == 'origination')  return "Creation Date";
  if (type == 'modification') return "Modification Date";

  return type;
}

function mapEntry()
{
  return "Entry";
}

function mapNote()
{
  return "Note";
}

function mapSource()
{
  return "Source";
}

]]></xsl:script>
  
<xsl:template>
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

    <xsl:template match="transacGrp">
      <SPAN class="vtransacGrp">
        <xsl:attribute name="type"><xsl:value-of select="transac/@type"/></xsl:attribute>
        <xsl:attribute name="author"><xsl:value-of select="transac"/></xsl:attribute>
        <xsl:attribute name="date"><xsl:value-of select="date"/></xsl:attribute>
        <SPAN CLASS="vtransaclabel"><xsl:eval>mapTransac(this)</xsl:eval></SPAN>
        <SPAN CLASS="vtransacvalue"><xsl:value-of select="date"/> 
         (<xsl:value-of select="transac"/>)</SPAN>
      </SPAN>
    </xsl:template>

    <xsl:template match="noteGrp">
      <DIV class="vfieldGrp">
        <xsl:apply-templates select="note"/>
      </DIV>
    </xsl:template>

    <xsl:template match="note">
      <SPAN CLASS="vfieldlabel" unselectable="on" type="note"><xsl:eval>mapNote()</xsl:eval></SPAN>
      <SPAN CLASS="vfieldvalue"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="sourceGrp">
      <DIV CLASS="vfieldGrp">
        <xsl:apply-templates select="source"/>
        <xsl:apply-templates select="noteGrp"/>
      </DIV>
    </xsl:template>

    <xsl:template match="source">
      <SPAN CLASS="vfieldlabel" unselectable="on" type="source"><xsl:eval>mapSource()</xsl:eval></SPAN>
      <SPAN CLASS="vfieldvalue"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="descripGrp">
      <DIV CLASS="vfieldGrp">
        <xsl:apply-templates select="descrip"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>
      </DIV>
    </xsl:template>

    <xsl:template match="descrip">
      <SPAN CLASS="vfieldlabel" unselectable="on"><xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute><xsl:eval>mapField(this)</xsl:eval></SPAN>
      <SPAN CLASS="vfieldvalue"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="language">
      <SPAN class="vlanguagelabel">Language</SPAN>
      <SPAN class="vlanguage" unselectable="on"><xsl:attribute name="locale"><xsl:value-of select="@locale"/></xsl:attribute><xsl:value-of select="@name"/></SPAN>
    </xsl:template>

    <xsl:template match="languageGrp">
      <DIV> <!--class="vlanguageGrp"-->
        <xsl:choose>
          <xsl:when test="language[@source-lang]">
            <xsl:attribute name="class">vsourceLanguageGrp</xsl:attribute>
          </xsl:when>
          <xsl:when test="language[@target-lang]">
            <xsl:attribute name="class">vtargetLanguageGrp</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">vlanguageGrp</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <SPAN class="vfakeLanguageGrp">
          <xsl:apply-templates select="language"/>
        </SPAN>

        <xsl:apply-templates select="descripGrp"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>

        <xsl:apply-templates select="termGrp[term/@search-term]"/>
        <xsl:apply-templates select="termGrp[not(term/@search-term)]"/>
      </DIV>
    </xsl:template>

    <xsl:template match="termGrp">
      <DIV class="vtermGrp">
        <DIV class="vfakeTermGrp">
          <xsl:apply-templates select="term"/>
        </DIV>

        <xsl:apply-templates select="descripGrp"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>
      </DIV>
    </xsl:template>

    <xsl:template match="term[@search-term]">
      <SPAN class="vtermlabel">Term</SPAN>
      <SPAN class="vsearchterm"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="term[not(@search-term)]">
      <SPAN class="vtermlabel">Term</SPAN>
      <SPAN class="vterm"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="conceptGrp">
      <DIV class="vconceptGrp">
        <SPAN class="vfakeConceptGrp">
          <SPAN class="vconceptlabel"><xsl:eval>mapEntry()</xsl:eval></SPAN>
          <SPAN class="vconcept"><xsl:value-of select="concept"/></SPAN>
        </SPAN>

        <xsl:apply-templates select="transacGrp"/>
        <xsl:apply-templates select="descripGrp"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>

        <xsl:apply-templates select="languageGrp[language/@source-lang]"/>
        <xsl:apply-templates select="languageGrp[language/@target-lang]"/>
        <xsl:apply-templates select="languageGrp[not(language/@source-lang) and not(language/@target-lang)]"/>
      </DIV>
    </xsl:template>

  </xsl:apply-templates>
  </xsl:template>
</xsl:stylesheet>
