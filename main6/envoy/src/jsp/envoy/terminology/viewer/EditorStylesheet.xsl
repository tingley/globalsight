<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet is outdated as of Thu Feb 19 01:01:38 2004.
  It was superceded by code in entry.js that performs the same
  XML to HTML mapping this this stylesheet used to do.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">

<xsl:script language="JScript"><![CDATA[
// EditorStylesheet.xsl

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
  var type = node.selectSingleNode('transac').attributes.getNamedItem('type').text;

  if (type == 'origination')  return "Creation Date";
  if (type == 'modification') return "Modification Date";

  return type;
}

function mapEntry()
{
  return "Entry";
}

function mapNewEntry()
{
  return "New Entry";
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
      <SPAN class="transacGrp">
        <xsl:attribute name="type"><xsl:value-of select="transac/@type"/></xsl:attribute>
        <xsl:attribute name="author"><xsl:value-of select="transac"/></xsl:attribute>
        <xsl:attribute name="date"><xsl:value-of select="date"/></xsl:attribute>
        <SPAN CLASS="transaclabel"><xsl:eval>mapTransac(this)</xsl:eval></SPAN>
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
      <SPAN CLASS="fieldlabel" unselectable="on" type="note"><xsl:eval>mapNote()</xsl:eval></SPAN>
      <SPAN CLASS="fieldvalue"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="sourceGrp">
      <DIV CLASS="fieldGrp" ondblClick="doEdit(this);">
        <xsl:apply-templates select="source"/>
        <xsl:apply-templates select="noteGrp"/>
      </DIV>
    </xsl:template>

    <xsl:template match="source">
      <SPAN CLASS="fieldlabel" unselectable="on" type="source"><xsl:eval>mapSource()</xsl:eval></SPAN>
      <SPAN CLASS="fieldvalue"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="descripGrp">
      <DIV CLASS="fieldGrp" ondblclick="doEdit(this);">
        <xsl:apply-templates select="descrip"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>
      </DIV>
    </xsl:template>

    <xsl:template match="descrip">
      <SPAN CLASS="fieldlabel" unselectable="on"><xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute><xsl:eval>mapField(this)</xsl:eval></SPAN>
      <SPAN CLASS="fieldvalue"><xsl:apply-templates/></SPAN>
    </xsl:template>

    <xsl:template match="language">
      <SPAN class="languagelabel">Language</SPAN>
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
      <SPAN class="termlabel">Term</SPAN>
      <SPAN class="term"><xsl:value-of/></SPAN>
    </xsl:template>

    <xsl:template match="conceptGrp">
      <DIV class="conceptGrp">
        <SPAN class="fakeConceptGrp">
          <xsl:choose>
            <xsl:when test="concept[. &gt; 0]">
              <SPAN class="conceptlabel"><xsl:eval>mapEntry()</xsl:eval></SPAN>
              <SPAN class="concept"><xsl:value-of select="concept"/></SPAN>
            </xsl:when>
            <xsl:otherwise>
              <SPAN class="conceptlabel"><xsl:eval>mapNewEntry()</xsl:eval></SPAN>
              <SPAN class="concept"></SPAN>
            </xsl:otherwise>
          </xsl:choose>
        </SPAN>

        <xsl:apply-templates select="transacGrp"/>
        <xsl:apply-templates select="descripGrp"/>
        <xsl:apply-templates select="sourceGrp"/>
        <xsl:apply-templates select="noteGrp"/>

        <xsl:apply-templates select="languageGrp"/>
      </DIV>
    </xsl:template>

  </xsl:apply-templates>
  </xsl:template>
</xsl:stylesheet>
