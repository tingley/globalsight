<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.awt.Color,
                 java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.Iterator,
                 java.util.Collection,
                 javax.swing.table.TableModel,
                 javax.swing.table.DefaultTableModel,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.WorkflowTableModel,
                 com.globalsight.reports.Constants,
                 com.globalsight.reports.datawrap.TaskDurationReportDataWrap,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.webapp.WebAppConstants
                 " session="true"
%>
<%ResourceBundle bundle = PageHandler.getBundle(session); %>
<HTML>
  <HEAD>

    <META content="text/html; charset=UTF-8" http-equiv="Content-Type">
    <META content="no-cache" http-equiv="Cache-Control">
    <META content="no-cache" http-equiv="Pragma">
    <META NAME="Created" CONTENT="2002-03-06 17:35:12">
    <META NAME="Modified" CONTENT="2002-03-09 11:39:24">
    
    <STYLE type="text/css">
      SPAN.left { text-align: left }
      SPAN.center { text-align: center }
      SPAN.right { text-align: right }
      .highlighted { color: magenta; background-color: lightgray}
      .toolbar { text-align: center; font-family: Verdana,Arial, Helvetica, sans-serif; font-size: 11px }
        #divToolBar {position: absolute; left:20px; top:0px; z-index: 2;}
        #divArea0_0 {position: absolute; top: 43px; left: 595px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_1 {position: absolute; top: 44px; left: 99px;  width: 171px;  z-index: 1}
            #divArea0_2 {position: absolute; top: 86px; left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 14pt; z-index: 1}
            #divArea0_3 {position: absolute; top: 110px; left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
            #divArea0_4 {position: absolute; top: 156px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_5 {position: absolute; top: 470px; left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
            #divArea0_6 {position: absolute; top: 205px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_7 {left: 97px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_8 {left: 545px;  width: 180px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}

            td.size9 { Font-Size: 9pt}
            td.size14 { Font-Size: 14pt}
            #divFooter {position: absolute; top: 858px; z-index: 1;TGTG}
            #divEntire {position: absolute; left:0px; top:0px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}
    </STYLE>
    <TITLE><%=bundle.getString("task_duration_report")%></TITLE>
  </HEAD>
  
  <BODY bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onAfterAction()">
        <% 
        String url = (String)request.getAttribute("image");
          SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
          HashMap map = (HashMap)sessionManager.getReportAttribute(Constants.TASK_DURATION_REPORT_KEY);
          TaskDurationReportDataWrap reportDataObject = (TaskDurationReportDataWrap)map.get(Constants.TASK_DURATION_REPORT_KEY+Constants.REPORT_DATA_WRAP);
          ArrayList tableHeadList = (ArrayList)reportDataObject.getTableHeadList();
          ArrayList pageDataList = (ArrayList)request.getAttribute(Constants.TASK_DURATION_REPORT_CURRENT_PAGE_LIST);
        %>
        
            <script>
  function onsubmit() {
      if(event.keyCode == 13) {
          CustomForm.submit();
      }
  }
  function onAfterAction() {
     if(<%=reportDataObject.getTotalPageNum()%> <= 1) {
        FirstForm.First.disabled = true;
        PreviousForm.Previous.disabled = true;
        NextForm.Next.disabled = true;
        LastForm.Last.disabled = true;
     } else {
        if(<%=reportDataObject.getCurrentPageNum()%> == 1) {
            FirstForm.First.disabled = true;
            PreviousForm.Previous.disabled = true;
            NextForm.Next.disabled = false;
            LastForm.Last.disabled = false;
        } else if(<%=reportDataObject.getCurrentPageNum()%> == <%=reportDataObject.getTotalPageNum()%>) {
            FirstForm.First.disabled = false;
            PreviousForm.Previous.disabled = false;
            NextForm.Next.disabled = true;
            LastForm.Last.disabled = true;
        } else {
            FirstForm.First.disabled = false;
            PreviousForm.Previous.disabled = false;
            NextForm.Next.disabled = false;
            LastForm.Last.disabled = false;
        }
     }
  }
</script>

  <DIV ID="divToolBar" aligh=left>
    <table><tr height=40px>
        <td>
        <form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=turnpage" method=post>
            <input type=hidden name=taskDurationReportPageNum value="1">
            <input type=Submit name=First value=<%=bundle.getString("lb_first")%>>
        </form>
    </td>
        <td>
        <form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=turnpage" method=post>
            <input type=hidden name=taskDurationReportPageNum value=<%=((Integer)request.getAttribute("taskDurationReportPageNum")).intValue()-1%> style="width:30px">
            <input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>>
        </form>
    </td>
        <td>
        <form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=turnpage" method=post>
            <input type=text name=taskDurationReportPageNum size=4 value=<%=(Integer)request.getAttribute("taskDurationReportPageNum")%>>
        </form>
    </td>
        <td>
        <form name=NextForm Action="/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=turnpage" method=post>
            <input type=hidden name=taskDurationReportPageNum value=<%=((Integer)request.getAttribute("taskDurationReportPageNum")).intValue()+1%> style="width:30px">
            <input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
        </form>
    </td>
        <td>
        <form name=LastForm Action="/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=turnpage" method=post>
            <input type=hidden name=taskDurationReportPageNum value=<%=reportDataObject.getTotalPageNum()%> style="width:30px">
            <input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
        </form>
    </td>
        </tr></table>
</DIV>

  <div id="divEntire">
    <TABLE valign="top">
        <TR>
        <TD>
          <div id="divarea0_0" align=right><font color="#000000" face="times"><%=reportDataObject.getTxtDate()%></font></div>
          <div id="divarea0_1"><img src="/globalsight/images/reports.gif" border=0></div>
        </TD>
        </TR>
        
        <TR>
        <TD>
          <DIV ID="divArea0_2" align=center>
            <FONT COLOR="#000000" FACE="Arial"><B><%=reportDataObject.getReportTitle()%></B></FONT>
              <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
          </DIV>
        </TD>
        </TR>
      
        <TR>
            <TD>
              <DIV ID="divArea0_3" align=left>
                <FONT COLOR="#000000" FACE="Arial"><B><%=reportDataObject.getDescription()%></B></FONT>
                  <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
              </DIV>
            </TD>
        </TR>
        <TR>
        <TR>
        
        <TD>
          <DIV ID="divArea0_4">
          <TABLE height="738" valign="top" border="0">
            <TR>
            <TD colspan="2" valign="top">
          <% if (pageDataList != null && pageDataList.size() > 0) { %>
              <TABLE id="TmRecordTable" CELLSPACING=0 CELLPADDING="2" WIDTH="680" style="border: 1px solid #000">
              <TR>
              <% for(int tableHeadColumn = 0; tableHeadColumn < tableHeadList.size(); tableHeadColumn++) { %>
                <TD class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE align=center HEIGHT=30 style="border: 1px solid #000">
                  <span class=center width=500><FONT COLOR="#ffffff" FACE="Arial"><B><%=tableHeadList.get(tableHeadColumn)%></B></FONT></span>
                </TD>
              <% } %>
              </TR>
              <% for(int rowDataIndex = 0; rowDataIndex < pageDataList.size(); rowDataIndex++) {
                  ArrayList rowDataList = (ArrayList) pageDataList.get(rowDataIndex);
              %>
              <TR>
                <% for(int colDataIndex = 0; colDataIndex < rowDataList.size(); colDataIndex++) { %>
                <TD class=size9 VALIGN=MIDDLE WIDTH="100" HEIGHT=30 style="border: 1px solid #000">
                  <span class=left><FONT COLOR="#000000" FACE="Arial"><B><%=rowDataList.get(colDataIndex)%></B></FONT></span>
                </TD>
                <% } %>
              </TR>
              <% } %>
              
            </TABLE>
        <% } %>
        </TD>
        </TR>
        <tr><td colspan="2" height="20">&nbsp;</td></tr>
        <TR height="10">
          <TD>
                    <DIV ID="divArea0_7">
                    <FONT COLOR="#000000" FACE="Times"><%=reportDataObject.getTxtFooter()%></FONT>
                    <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
                    </DIV>
                </TD>
                <TD align="right">
                    <DIV ID="divArea0_8" align=right>
                    <FONT COLOR="#000000" FACE="Times"><%=reportDataObject.getTxtPageNumber()%></FONT>
                    </DIV>
                <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
                </TD>

            </TR>
            <tr><td colspan="2" height="20">&nbsp;</td></tr>
        </TABLE>

      </DIV>

    </TD></TR>
    <div id="divFooter">&nbsp;</div>
  </TABLE>
</div>
</BODY>
</HTML>
