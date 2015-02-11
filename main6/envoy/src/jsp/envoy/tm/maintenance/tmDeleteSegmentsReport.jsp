<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.ling.tm2.BaseTmTuv,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.ling.tw.PseudoConstants,
            com.globalsight.ling.tw.PseudoData,
            com.globalsight.ling.tw.TmxPseudo,
            com.globalsight.ling.tw.PtagStringFormatter,
            com.globalsight.ling.common.Text,
            com.globalsight.ling.tm2.SegmentTmTu,
            java.util.Collection,
            java.util.ResourceBundle" 
    session="true"
%>
<jsp:useBean id="search" scope="request" 
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request" 
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // links   
    String doneUrl = done.getPageURL();
    String searchUrl = search.getPageURL();

    // labels    
    String pagetitle= bundle.getString("lb_globalsight")
                      + bundle.getString("lb_colon") + " "
                      + bundle.getString("lb_tm_delete_results_title");
    String title = bundle.getString("lb_tm_delete_results_title");   
    String lbDoneBtn = bundle.getString("lb_done");
    String lbSearchAndReplaceBtn = bundle.getString("lb_search_and_replace") + 
        bundle.getString("lb_dots");

    String lbId = bundle.getString("lb_id");
    String lbSource = bundle.getString("lb_tm_search_source_locale") +
        bundle.getString("lb_colon");
    String lbTarget = bundle.getString("lb_tm_search_target_locale") +
        bundle.getString("lb_colon");

    // control name
    String stateParam = WebAppConstants.TM_SEARCH_STATE_PARAM;
    String stateNormal = WebAppConstants.TM_SEARCH_STATE_NORMAL;

    GlobalSightLocale sourceSearchLocale =
        (GlobalSightLocale)sessionMgr.getAttribute(
            WebAppConstants.TM_SOURCE_SEARCH_LOCALE);

    String sourceSearchLocaleDisplayName =
        sourceSearchLocale != null ?
        sourceSearchLocale.getDisplayName(uiLocale) : "null";

    GlobalSightLocale targetSearchLocale =
        (GlobalSightLocale)sessionMgr.getAttribute(
            WebAppConstants.TM_TARGET_SEARCH_LOCALE);

    String targetSearchLocaleDisplayName =
        targetSearchLocale != null ?
        targetSearchLocale.getDisplayName(uiLocale) : "null";

    String action  = request.getParameter(WebAppConstants.TM_ACTION);

    ArrayList deletedSegments = (ArrayList)request.getAttribute(
      WebAppConstants.TM_DELETED_SEGMENTS);

    ArrayList notDeletedSegments = (ArrayList)request.getAttribute(
      WebAppConstants.TM_NOT_DELETED_SEGMENTS);
%>
<%!
    public String getTableMsg(
        int p_table, String p_action, ResourceBundle p_bundle)
    {
        String msg = null;
        if(p_table == 0)
        {
            if(p_action.equals(WebAppConstants.TM_ACTION_DELETE_TUV))
            {
                msg = p_bundle.getString("msg_delete_tuv_successful");
            }
            else
            {
                msg = p_bundle.getString("msg_delete_tu_successful");
            }
        }
        else
        {
            msg = p_bundle.getString("msg_delete_tuv_failed");
        }

        return msg;
    }

    public String makePtagString(BaseTmTuv p_tuv)
        throws Exception
    {
        String gxml = GxmlUtil.stripRootTag(p_tuv.getSegment());

        PseudoData pTagData = new PseudoData();
        TmxPseudo convertor = new TmxPseudo();

        pTagData.setMode(PseudoConstants.PSEUDO_COMPACT);
        pTagData.setAddables(p_tuv.getTu().getFormat());

        convertor.tmx2Pseudo(gxml, pTagData);

        String rawPtagString = pTagData.getPTagSourceString();

        // color code Ptag string
        PtagStringFormatter format = new PtagStringFormatter();

        return format.htmlLtrPtags(
            EditUtil.encodeHtmlEntities(rawPtagString), null, false,
            p_tuv.getLocale());
    }

    public String getDirTag(String p_ptagStr, GlobalSightLocale p_locale)
    {
        String tag = "";

        if (EditUtil.isRTLLocale(p_locale)
            && Text.containsBidiChar(p_ptagStr))
        {
            tag = "DIR=\"RTL\"";
        }

        return tag;
    }

    public String getStyleTag(int p_table)
    {
        String tag = "";

        if(p_table == 1)
        {
            tag = "style=\"color : red;\"";
        }

        return tag;
    }

%>
<HTML>
<HEAD>
<TITLE><%=pagetitle%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tmSearch";
var helpFile = "<%=bundle.getString("help_tm_maintenance3")%>";

function submitForm(buttonClicked) 
{
    if (buttonClicked=="search")
    {
        TmBatchReplaceResultsForm.action = "<%=searchUrl%>" + 
            "&" + "<%=stateParam%>" + "=" + "<%=stateNormal%>";
    }

    if (buttonClicked=="done")
    {
        TmBatchReplaceResultsForm.action = "<%=doneUrl%>";                
    }

    TmBatchReplaceResultsForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>                
<P></P>

<!-- Lower table -->
<%
    for(int tables = 0; tables < 2; tables++)
    {
        ArrayList tuList = tables == 0 ? deletedSegments : notDeletedSegments;
        if(tuList.size() > 0)
        {
%>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
  <TR>
    <TD VALIGN="TOP">
      <SPAN CLASS="standardTextBold" <%=getStyleTag(tables)%>><%=getTableMsg(tables, action, bundle)%></SPAN>
    </TD>
  </TR>
</TABLE>
<P></P>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>">
  <THEAD>
    <COL VALIGN="top" ALIGN="right"
    STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
    <COL VALIGN="top"> <!-- Source -->
    <COL VALIGN="top"> <!-- Target -->
    <TR CLASS="tableHeadingBasic">
      <TD HEIGHT="20" ALIGN="center"><%=lbId%></TD>
      <TD HEIGHT="20"><%=lbSource%> <%=sourceSearchLocaleDisplayName%></TD>
      <TD HEIGHT="20"><%=lbTarget%> <%=targetSearchLocaleDisplayName%></TD>
    </TR>
  </THEAD>
  <TBODY>
<%
             for(int i = 0, max = tuList.size(); i < max; i++)
             {
                 String rowColor = (i % 2 == 0) ? "#FFFFFF" : "#EEEEEE";

                 SegmentTmTu tu = (SegmentTmTu)tuList.get(i);
                 BaseTmTuv srcTuv = tu.getFirstTuv(sourceSearchLocale);
                 String tuIdStr = Long.toString(srcTuv.getTu().getId());
                 String srcPtagStr = makePtagString(srcTuv);

                 Collection targetTuvs = tu.getTuvList(targetSearchLocale);
                 int count = 0;
                 for (Iterator it = targetTuvs.iterator(); it.hasNext(); )
                 {
                     String targetRowColor
                         = (count++ % 2 == 0) ? rowColor : "#DDDDDD";

                     BaseTmTuv trgTuv = (BaseTmTuv)it.next();
                     String trgPtagStr = makePtagString(trgTuv);

%>
    <TR VALIGN=TOP BGCOLOR="<%=rowColor%>">
      <TD STYLE="padding-top: 2px"><%=tuIdStr%></TD>
      <TD STYLE="word-wrap: break-word" WIDTH=300 <%=getDirTag(srcPtagStr, srcTuv.getLocale())%>><%=srcPtagStr%></TD>
      <TD STYLE="word-wrap: break-word" WIDTH=300 <%=getDirTag(srcPtagStr, srcTuv.getLocale())%> BGCOLOR="<%=targetRowColor%>"><%=trgPtagStr%></TD>
    </TR>
<%
                    tuIdStr = "&nbsp;";
                    srcPtagStr = "&nbsp;";
                }
            }
%>
  </TBODY>
</TABLE>
<P></P>
<%
        }
    }
%>

<FORM NAME="TmBatchReplaceResultsForm" ACTION="<%=doneUrl%>" METHOD="POST">
<DIV>
<INPUT TYPE="BUTTON" NAME="Search" VALUE="<%= lbSearchAndReplaceBtn %>"
 onClick="submitForm('search');">&nbsp;
<INPUT TYPE="BUTTON" NAME="Search" VALUE="<%= lbDoneBtn %>"
 onClick="submitForm('done');">
</DIV>
</FORM>

</DIV>
</BODY>
</HTML>
