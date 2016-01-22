<%@ page contentType="text/css; charset=UTF-8"
errorPage="/envoy/common/error.jsp" session="false" %>
<jsp:useBean id="skin" class="com.globalsight.everest.webapp.javabean.SkinBean" scope="application"/>
.asterisk
{
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    color:red;
}

.bigText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12pt
}

.navPoint {
    font-size: 18px;
    font-weight: bolder;
}

.confidential {
    color: #0C1476;
    background-color: #FFFFFF;
}

.divider
{
    background: #A6B8CE;
}

.detailText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
}

.editorStandardText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt
}

.editorText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

.editorId {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: normal;*/
}

.editorSegment, .editorSegmentRepetition {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

/* Class added in 6.2SP4. */
.editorSegmentUpdated {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

.editorSegmentExact {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

.editorSegmentFuzzy {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

/* Class added in 6.2SP4. */
.editorSegmentLocked {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

.editorSegmentExcluded {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

/* Class added in 6.3. */
.editorSegmentUnverified {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}

.errorMsg
{
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    color:red;
}

.glossaryDateSize {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-style: italic
}

.greenText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    color: #009966
}

.header1 {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.header1.color")%>;
    background: <%=skin.getProperty("skin.header1.bgColor")%>;
}

.header2 {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.header2.color")%>;
    background: <%=skin.getProperty("skin.header2.bgColor")%>;
}

.headingError {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    font-weight: bold;
    color: red;
}

.listborder {
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}

.welcomePageMainHeading {
    font-family: Arial, Helvetica, sans-serif;
    color: rgb(147, 165, 179);
    font-size: 16pt;
    font-weight: bold;
}

.mainHeading {
    font-family: Arial, Helvetica, sans-serif;
    color: #0C1476;
    font-size: 11pt;
    font-weight: bold;
}

.welcomePageContentLayer {
    position: absolute;
    z-index: 10;
    top: 168px;
    left: 20px;
    right: 20px;
    margin-left: 80px;
}

.helloText {
    font-family: Arial,Helvetica,sans-serif;
    font-weight:bold;
    font-size: 11pt;
}

.helloTextDetail {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12pt;
    color: #555;
}

.smallText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 7pt;
}

.smallTextGray {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 7pt;
    color: gray;
}

.smallWhiteItalic {
    font-family: Arial, Helvetica, sans-serif;
    font-style: italic;
    font-size: 7pt;
    color: white;
}

.smallSilverItalicBold {
    font-family: Arial, Helvetica, sans-serif;
    font-style: italic;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.welcomeMsg.color")%>;
}

.welcomePageLink {
    font-family: Arial, Helvetica, sans-serif;
    color: rgb(95,139,42);
    font-size: 13px;
    text-decoration: none;
}

.standardHREF {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
}

.standardHREF:visited {
    text-decoration: underline;
}

.standardHREFDetail {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
}

.standardHREFDetail:visited {
    text-decoration: underline;
}

.standardHREFSmall {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 7pt;
}

.standardHREFSmall:visited {
    text-decoration: underline;
}

.standardText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
}

.standardTextNew {
    font-family:Verdana,Arial;
    font-size: 9pt;
}

.standardTextBold {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
}

.standardTextBoldLarge {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12pt;
    font-weight: bold;
}

.standardTextItalic {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-style: italic
}

.standardTextWhite {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    color: #FFFFFF
}

.standardTextGray {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    color: gray
}


.startHere {
    font-family: Verdana, sans-serif;
    font-size: 7pt;
    font-weight: bold;
    color: white;
    background: red;
    text-decoration: none;
}

.startHere: hover
{
    text-decoration: underline;
}


.sourceTerm {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt
}

.targetTerm {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    margin-left: 10pt;
    /*color: blue;
    cursor: hand;*/
}

.tableBodyHome {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    vertical-align: top;
}

.welcomePageTableHeadingBasic {
    font-family: Arial,Helvetica, sans-serif; 
    font-weight:bold;
    font-size: 9pt;
    color: rgb(37,34,101);
}

.tableHeadingBasic {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.tableHeadingBasic.fgColor")%>;
    background: <%=skin.getProperty("skin.tableHeadingBasic.bgColor")%>;
}

.welcomePageTableHeadingBasicBlack {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    font-weight: bold;
    color: #555;
}

.tableHeadingBasicBlack {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: black;
    background: #EEEEEE;
}


.tableHeadingGray {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: white;
    background: <%=skin.getProperty("skin.tableHeading.bgColor")%>;
}

.tableHeadingListOff {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.tableHeading.fgColor")%>;
    background: <%=skin.getProperty("skin.tableHeading.bgColor")%>;
}

.tableHeadingListOn {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.tableHeadingBasicOn.fgColor")%>;
    background: <%=skin.getProperty("skin.tableHeadingBasicOn.bgColor")%>;vertical-align: middle;
}

.tableHeadingWhite {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12pt;
    font-weight: bold;
    color: #FFFFFF
}

.tableHeadingFilter {
    height:10px;
    background: #738EB5;
}

.tableRowEven {
    background: #FFFFFF;
}

.tableRowOdd {
    background: #EEEEEE;
}

.warningHREF {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    color: red;
    text-decoration: underline;
}

.warningHREF:visited {
    text-decoration: underline;
}

.warningText {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    color: red;
}

.whiteBold {
    color: <%=skin.getProperty("skin.whiteBold.color")%>;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
}

.whiteHREF {
    color: white;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
}

.wizardCell {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    text-align: center;
    background: #EEEEEE;
    padding-left: 2px;
    padding-right: 2px;
    height: 15px;
    width: 7%;			/*width: 80px;*/
}

.wizardCellArrow {
    text-align: center;
    height: 15px;
    background: #EEEEEE;
}

.wizardCellArrowLinks
{
    font-family: arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: silver;
}

.wizardCellArrowLinks:hover
{
    text-decoration: underline;
    color: navy;
}

.wizardCellClose {
    text-align: center;
    border: 1px solid silver;
    background: #EEEEEE;
    padding-left: 2px;
    padding-right: 2px;
    height: 10px;
}

.wizardCellCloseLinks
{
    font-family: arial, Helvetica, sans-serif;
    font-size: 7pt;
    font-weight: normal;
    color: silver;
}

.wizardCellCloseLinks:hover
{
    text-decoration: none;
    color: navy;
}

.wizardCellQuestion {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: white;
    text-align: left;
    vertical-align: top;
    background: <%=skin.getProperty("skin.tableHeading.bgColor")%>;
}

.wizardCellStartFinish {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    text-align: center;
    background: #EEEEEE;
    padding-left: 2px;
    padding-right: 2px;
    height: 15px;
    width: 40px;
}

.wizardLinks
{
    font-family: verdana, sans-serif;
    font-size: 7pt;
    font-weight: bold;
    color: gray;
    text-decoration: none;
}

.wizardLinks:hover
{
    text-decoration: underline;
    color: navy;
}

.wizardLinks:visited
{
    text-decoration: none;
}

.wordCountHeading {
    font-family: Verdana, Helvetica, sans-serif;
    font-size: 7pt;
    font-weight: bold;
    color: white;
    background: <%=skin.getProperty("skin.tableHeading.bgColor")%>;
}

.wordCountHeadingBlack {
    font-family: Verdana, Helvetica, sans-serif;
    font-size: 7pt;
    color: black;
    font-weight: bold;
    background: #EEEEEE;
}

.wordCountHeadingWhite {
    font-family: Verdana, Helvetica, sans-serif;
    font-size: 7pt;
    color: <%=skin.getProperty("skin.wordCountHeadingWhite.color")%>;
    font-weight: bold;
    background: <%=skin.getProperty("skin.wordCountHeading.bgColor")%>;
}

.HREFBold {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: #0C1476;
}

a:visited
{
    text-decoration: none;
}


a.HREFBold {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-weight: bold;
    color: #0C1476;
}

a.HREFBold:hover {
    text-decoration: underline;
}

a.HREFBold:link {
    text-decoration: underline;
}

a.HREFBold:visited {
    text-decoration: underline;
}


a.HREFBoldSmall {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: #0C1476;
}

a.HREFBoldSmall:hover {
    text-decoration: underline;
}

a.HREFBoldSmall:link {
    text-decoration: none;
}

a.HREFBoldSmall:visited {
    text-decoration: none;
}

a.HREFBoldWhite {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.HREFBoldWhite.color")%>
}

a.HREFBoldWhite:hover {
    text-decoration: underline;
}

a.HREFBoldWhite:link {
    text-decoration: underline;
}

a.HREFBoldWhite:visited {
    text-decoration: underline;
}

a.header1 {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: white;
}

a.header1:hover {
    text-decoration: underline;
}

a.header1:link {
    text-decoration: none;
}


a.header2 {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: white;
}

a.header2:hover {
    text-decoration: underline;
}

a.header2:link {
    text-decoration: none;
}

a.sortHREFWhite {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.sortHREFWhite.color")%>;
}

a.sortHREFWhite:hover {
    text-decoration: underline;
}

a.sortHREFWhite:link {
    text-decoration: none;
}

a.wordCountLinks {
    font-family: Verdana, sans-serif;
    font-size: 7pt;
    font-weight: bold;
    color: <%=skin.getProperty("skin.wordCountLinks.color")%>
}

a.wordCountLinks:hover {
    text-decoration: underline;
}

a.wordCountLinks:link {
    text-decoration: none;
}

.activityDashboardText{
    font-family: Arial, Helvetica, sans-serif;
    color: rgb(95,139,42);
    font-size: 13px;
    text-decoration: none;
}

.activityDashboardNumber{
    font-family: Arial, Helvetica, sans-serif;
    color: rgb(37,34,101);
    font-size: 13px;
    font-weight: bold;
    text-decoration: none;
}

.editorSegmentMT {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    /*word-break: break-all;*/
    word-wrap: break-word;
}
.tip{
    position:absolute;
    border:1px solid #000000;
    display:none;
    background:#FFFFE1;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
}

.tableHeadingBasicTM {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
    color: white;
    background: #0C1476;
    height:30px;
}

.tableRowEvenTM {
	height:30px;
    background: #FFFFFF
}

.tableRowOddTM {
	height:30px;
    background: #EEEEEE
}
