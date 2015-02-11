<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.io.PrintWriter,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

ITermbase termbase = (ITermbase)session.getAttribute(
  WebAppConstants.TERMBASE);

String stylesheet =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
"" +
"<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/TR/WD-xsl\">" +
"  " +
"<!-- Start at the first element (should be root) -->" +
"<xsl:template>" +
"  <DIV STYLE=\"margin-bottom:2em\">" +
"" +
"  <xsl:apply-templates select=\".\">" +
"" +
"    <xsl:template><xsl:apply-templates/></xsl:template>" +
"" +
"    <!-- DEBUGGING: mark non-matched nodes with yellow background -->" +
"    <xsl:template match=\"*\">" +
"       <SPAN STYLE=\"background-color:yellow\">" +
"         <xsl:attribute name=\"title\">&lt;<xsl:node-name/>&gt;</xsl:attribute>" +
"         <xsl:apply-templates/>" +
"       </SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"text()\"><xsl:value-of/></xsl:template>" +
"" +
"    <xsl:template match=\"xref\"><A STYLE=\"color:green;cursor:hand\" onclick='showlink(this)'>" +
"      <xsl:choose>" +
"        <xsl:when test=\"@Clink\">" +
"          <xsl:attribute name=\"class\">Clink</xsl:attribute>" +
"          <xsl:attribute name=\"target\"><xsl:value-of select=\"@Clink\"/></xsl:attribute>" +
"        </xsl:when>" +
"        <xsl:when test=\"@Tlink\">" +
"          <xsl:attribute name=\"class\">Tlink</xsl:attribute>" +
"          <xsl:attribute name=\"target\"><xsl:value-of select=\"@Tlink\"/></xsl:attribute>" +
"        </xsl:when>" +
"      </xsl:choose>" +
"      <xsl:value-of/>" +
"      </A>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"date\">" +
"      <SPAN STYLE=\"font-weight:bolder;\">Date:</SPAN>" +
"      <SPAN STYLE=\"\"><xsl:value-of/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"note\">" +
"      <DIV>" +
"      <SPAN STYLE=\"font-weight:bolder;\">Note:</SPAN>" +
"      <SPAN STYLE=\"\"><xsl:apply-templates/></SPAN>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"transacGrp\">" +
"      <DIV STYLE=\"color:black;\">" +
"        <xsl:apply-templates select=\"transac\"/>" +
"        <SPAN STYLE=\"margin-left:10pt;color:gray\">" +
"          <xsl:apply-templates select=\"date\"/>" +
"        </SPAN>" +
"        <DIV STYLE=\"margin-left:10pt;\">" +
"          <xsl:apply-templates select=\"note\"/>" +
"        </DIV>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"transac\">" +
"      <SPAN STYLE=\"font-weight:bolder\"><xsl:value-of select=\"@type\"/></SPAN>" +
"      <SPAN STYLE=\"\"><xsl:value-of/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"sourceGrp\">" +
"      <DIV STYLE=\"color:black;\">" +
"        <xsl:apply-templates select=\"source\"/>" +
"        <DIV STYLE=\"margin-left:10pt;\">" +
"          <xsl:apply-templates select=\"note\"/>" +
"        </DIV>" +
"        <DIV STYLE=\"margin-left:10pt;\">" +
"          <xsl:apply-templates select=\"transacGrp\"/>" +
"        </DIV>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"source\">" +
"      <SPAN STYLE=\"font-weight:bolder\">Source:</SPAN>" +
"      <SPAN STYLE=\"\"><xsl:value-of/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"descripGrp\">" +
"      <DIV STYLE=\"color:black\">" +
"        <xsl:apply-templates select=\"descrip\"/>" +
"        <DIV STYLE=\"margin-left:10pt;\">" +
"          <xsl:apply-templates select=\"note\"/>" +
"        </DIV>" +
"        <DIV STYLE=\"margin-left:15pt;\">" +
"          <xsl:apply-templates select=\"source|sourceGrp\"/>" +
"        </DIV>" +
"        <DIV STYLE=\"margin-left:15pt;font-size:8pt;\">" +
"          <xsl:apply-templates select=\"transacGrp\"/>" +
"        </DIV>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"descrip\">" +
"      <SPAN STYLE=\"font-weight:bolder\"><xsl:value-of select=\"@type\"/></SPAN>" +
"      <SPAN><xsl:apply-templates/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"languageGrp\">" +
"      <xsl:apply-templates />" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"language\">" +
"    <!-- mark source and target language in different colors --> " +
"    <!-- (short of displaying the terms big and in colors)   -->" +
"      <xsl:choose>" +
"        <xsl:when test=\".[@source-lang]\">" +
"	  <DIV STYLE=\"font-size:12pt;font-weight:bold;color:blue;\">" +
"            <xsl:value-of select=\"@name\" />" +
"	  </DIV>" +
"	</xsl:when>" +
"        <xsl:when test=\".[@target-lang]\">" +
"	  <DIV STYLE=\"font-size:12pt;font-weight:bold;color:red;\">" +
"            <xsl:value-of select=\"@name\" />" +
"	  </DIV>" +
"	</xsl:when>" +
"	<xsl:otherwise>" +
"	  <DIV STYLE=\"font-size:10pt;font-weight:bold;color:brown;\">" +
"            <xsl:value-of select=\"@name\" />" +
"	  </DIV>" +
"	</xsl:otherwise>" +
"      </xsl:choose>" +
"" +
"      <DIV STYLE=\"margin-left:10pt;font-size:8pt;\">" +
"        <xsl:apply-templates select=\"transacGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"descrip|descripGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"source|sourceGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"note\"/>" +
"      </DIV>" +
"" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"termGrp[term/@search-term]\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"termGrp[not(term/@search-term)]\"/>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"termGrp\">" +
"      <xsl:apply-templates select=\"term\"/>" +
"      <DIV STYLE=\"margin-left:10pt;font-size:8pt;\">" +
"        <xsl:apply-templates select=\"transacGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"descrip|descripGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"source|sourceGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"note\"/>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"term[@search-term]\">" +
"      <SPAN STYLE=\"font-size:10pt; color:gray; font-weight:bolder\">Term:</SPAN>" +
"      <SPAN STYLE=\"font-size:16pt; color:blue\"><xsl:value-of/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"term[not(@search-term)]\">" +
"      <SPAN STYLE=\"font-size:10pt; color:gray; font-weight:bolder\">Term:</SPAN>" +
"      <SPAN STYLE=\"font-size:14pt; color:green\"><xsl:value-of/></SPAN>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"conceptGrp\">" +
"      <!-- can't get this one to work... -->" +
"      <DIV STYLE=\"font-size:14pt\"><BR/>Entry " +
"      <xsl:value-of select=\"concept\"/>:<BR></BR>" +
"      </DIV>" +
"" +
"      <DIV STYLE=\"margin-left:10pt;font-size:8pt;\">" +
"        <xsl:apply-templates select=\"transacGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"descrip|descripGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"source|sourceGrp\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <xsl:apply-templates select=\"note\"/>" +
"      </DIV>" +
"      <DIV STYLE=\"margin-left:10pt;\">" +
"        <P></P>" +
"        <xsl:apply-templates select=\"languageGrp\"/>" +
"      </DIV>" +
"    </xsl:template>" +
"" +
"    <xsl:template match=\"entry\"><xsl:apply-templates/></xsl:template>" +
"" +
"  </xsl:apply-templates>" +
"" +
"  </DIV>" +
"  </xsl:template>" +
"</xsl:stylesheet>";


PrintWriter writer = response.getWriter();
writer.write(stylesheet);
%>
