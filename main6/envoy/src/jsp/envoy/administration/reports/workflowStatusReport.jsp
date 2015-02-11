<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.LinkedHashMap,
                 java.util.Iterator,
                 java.util.ResourceBundle,
                 com.globalsight.reports.WorkflowTableModel,
                 com.globalsight.reports.Constants,
                 com.globalsight.reports.datawrap.WorkflowStatusReportDataWrap,
                 com.globalsight.everest.util.system.SystemConfiguration,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.util.system.SystemConfigParamNames" session="true"
%>
<% 
      WorkflowStatusReportDataWrap reportDataWrap = (WorkflowStatusReportDataWrap)request.getAttribute(Constants.WORKFLOW_REPORT_DATA);
      int jobNum = reportDataWrap.getTotalJobNum().intValue();
      ArrayList criteriaFormLabel = reportDataWrap.getCriteriaFormLabel();
      ArrayList criteriaFormValue = reportDataWrap.getCriteriaFormValue();
      LinkedHashMap job2WorkFlowMap = reportDataWrap.getJob2WorkFlowMap();
      ArrayList jobFormLabels = null;
      ArrayList jobFormValues = null;
      WorkflowTableModel wfTableModel = null;
      ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
    <head>
    <script>
      function onsubmit() {
          if(event.keyCode == 13) {
              CustomForm.submit();
          }
      }
      function onAfterAction() {
         if(<%=reportDataWrap.getTotalPageNum()%> <= 1) {
            FirstForm.First.disabled = true;
            PreviousForm.Previous.disabled = true;
            NextForm.Next.disabled = true;
            LastForm.Last.disabled = true;
         } else {
            if(<%=reportDataWrap.getCurrentPageNum()%> == 1) {
                FirstForm.First.disabled = true;
                PreviousForm.Previous.disabled = true;
                NextForm.Next.disabled = false;
                LastForm.Last.disabled = false;
            } else if(<%=reportDataWrap.getCurrentPageNum()%> == <%=reportDataWrap.getTotalPageNum()%>) {
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
        <style type="text/css">
            span.left { text-align: left }
            span.center { text-align: center }
            span.right { text-align: right }
            #divEntire { padding-left:100px; width: 800px;}
            #divToolBar {}
            #logo_pic {text-align:left; width: 200px;}
            #txt_date {text-align:right; width: 350px;  Font-Family: Times; Font-Size: 9pt;}
            #report_title {text-align:center; Font-Family: Arial; Font-Size: 14pt;}
            #divArea0_3 {Font-Family: Times; Font-Size: 9pt;}
            #total_jobs_num {Font-Family: Arial; Font-Size: 9pt; padding:5px;}
            #divArea0_5 {Font-Family: Times; Font-Size: 9pt;}
            #divArea0_6 {position: absolute; top: 818px; left: 97px;  width: 131px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_7 {position: absolute; top: 818px; left: 651px;  width: 62px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            td.size9 { Font-Size: 9pt}
            td.size14 { Font-Size: 14pt}
            #divFooter {position: absolute; top: 858px; z-index: 1;}
        </style>
        <title><%=bundle.getString("workflow_status_title")%></title>
    </head>
    <body onload="onAfterAction()" style="padding: 0;margin: 0">
        <table><tr><td>
        <form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=turnpage" method=post>
            <input type=hidden name=pageId value="1">
            <input type=Submit name=First value=<%=bundle.getString("lb_first")%>> 
        </form>
        </td><td>
        <form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=turnpage" method=post>
            <input type=hidden name=pageId value=<%=reportDataWrap.getCurrentPageNum()-1%>>
            <input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>> 
        </form>
        </td><td>
        <form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=turnpage" method=post>
            <input type=text name=pageId size=4 value=<%=reportDataWrap.getCurrentPageNum()%>>
        </form>
        </td><td>
        <form name=NextForm Action="/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=turnpage" method=post>
            <input type=hidden name=pageId value=<%=reportDataWrap.getCurrentPageNum()+1%> style="width:30px">
            <input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
        </form>
        </td><td>
        <form name=LastForm Action="/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=turnpage" method=post>
            <input type=hidden name=pageId value=<%=reportDataWrap.getTotalPageNum()%> style="width:30px">
            <input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
        </form>
        </td></tr></table>

        <div id="divEntire">
            <table>
                <tr><td>
                    <table>
                        <tr>
                            <td id="logo_pic"><img src="/globalsight/images/reports.gif" border=0></td>
                            <td id="txt_date"><font color="#000000" face="times"><%=reportDataWrap.getTxtDate()%></font></td>
                        </tr>
                    </table>
                </td></tr>
                <%
                    if( reportDataWrap.getCurrentPageNum() == 1) {
                %>
                <tr><td align="center" id="report_title">
                    <font color="#000000" face="arial"><b><%=reportDataWrap.getReportTitle()%></b></font>
                </td></tr>
                <tr><td>
                    <table id="divarea0_3" cellspacing=0 cellpadding=0>
                        <tr height=18>
                        <td class="size9" valign=bottom align=right>
                        <span class=right><font face="Arial"><b><%=criteriaFormLabel.get(0)%></b></font>
                        </span>
                        <span class=left><font face="arial"><%=criteriaFormValue.get(0)%></font>
                        </span>
                        </td>
                        
                        <td class="size9" valign=bottom align=right style="padding-left:10px">
                        <span class=right><font face="Arial"><b><%=criteriaFormLabel.get(1)%></b></font>
                        </span>
                        <span class=left><font face="Arial"><%=criteriaFormValue.get(1)%></font>
                        </span>
                        </td>
                        
                        <% if(SystemConfiguration.getInstance().getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED)) { %>
                        <td class="size9" valign=bottom align=right style="padding-left:10px">
                        <span class=right><font face="Arial"><b><%=criteriaFormLabel.get(2)%></b></font>
                        </span>
                        <span class=left><font face="Arial"><%=criteriaFormValue.get(2)%></font>
                        </span>
                        </td>
                        <% } %>
                        </tr>
                    </table>
                    <hr noshade size="1" width="100%" >
                    <div id="total_jobs_num" align=center>
                        <font color="#000000" face="Arial"><b><%=bundle.getString("total_number_of_jobs")%>&nbsp;<%=jobNum%></b></font>
                    </div>
                <% } %>
                
                </td></tr>
                <tr><td>
                
                <div id="divarea0_5">
                <%
                    Iterator keySet = job2WorkFlowMap.keySet().iterator();
                    Long jobId = null;
                    int index = 0;
                    int wfRow = 0;
                    int[] subColumns = null;
                    while(keySet.hasNext()) {
                        jobId = (Long) keySet.next();
                        jobFormLabels = reportDataWrap.getJobFormLabel(jobId);
                        jobFormValues = reportDataWrap.getJobFormValue(jobId);
                        subColumns = reportDataWrap.getWorkflowTableSubColumns(jobId);
                        wfTableModel = reportDataWrap.getWorkflowTableModel(jobId);
                        index = 0;
                        if(wfTableModel != null) {
                            wfRow = wfTableModel.getRowCount();
                        }
                %>
                <div id="">
                <hr noshade size="1" width="100%" >
                <table border="0" cellspacing="0" cellpadding="1">
                  <% for (index = 0; index < jobFormLabels.size(); index++) { %>
                    <tr>
                    <td class="size9" valign=bottom align=right nowrap width="20%" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    
                    </td>
                    <td class="size9" valign=bottom nowrap width="30%" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index)%></font>
                    </span>
                    </td>
                    <% if (++index < jobFormLabels.size()) { %>
                    <td class="size9" valign=bottom align=right nowrap height=18 width="25%">
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    
                    </td>
                    <td class="size9" valign=bottom nowrap height=18 width="25%">
                    <span class=left><font face="Arial"><%=jobFormValues.get(index)%></font>
                    </span>
                    
                    </td>
                    <% } else { %>
                    <td class="size9" valign=bottom align=right nowrap height=18>
                    </td>
                    <td class="size9" valign=bottom nowrap height=18>
                    </td>
                    <% } %>
                    </tr>
                  <% } %>
                    <tr>
                    </table>
                </div>  
                <div id="" style = "position:relative; height:50px"></div>
                <div id="">
                    <table cellspacing=0 cellpadding=0 width=630.45>
                    <!--*************workflow table header***************-->
                    <tr>
                    <% 
                        for(int i=0; i<subColumns.length; i++) {
                    %>
                    <td  style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding:1px;" class="size9" bgcolor="#0c1476" valign=middle align=center width="63.45" height=58>
                    <span class=center><font color="#ffffff" face="Arial"><%=wfTableModel.getColumnName(subColumns[i])%></font>
                    </span>
                    </td>
                    <% } %>
                    </tr>
                    <!--*************workflow table content***************-->
                    <% 
                        for(int i=0; i<wfRow; i++) {
                    %>
                    <tr>
                    <%
                        for(int j=0; j<subColumns.length; j++) {
                            if(j == 0) {
                    %>
                    <td style="border-bottom-style:solid; border-bottom-color:#000000; border-bottom-width:1; border-left-style:solid; border-left-color:#000000; border-left-width:1; border-right-style:solid; border-right-color:#000000; border-right-width:1;" class="size9" valign=middle width="63.45" height=44>
                    <span class=left><font color="#000000" face="Arial"><%=wfTableModel.getValueAt(i,subColumns[j]).toString()%></font>
                    </span>
                    </td>
                    <% } else {
                    %>
                    <td style="border-bottom-style:solid; border-bottom-color:#000000; border-bottom-width:1; border-right-style:solid; border-right-color:#000000; border-right-width:1;" class="size9" valign=middle width="63.45" height=44>
                    <span class=left><font color="#000000" face="Arial"><%=wfTableModel.getValueAt(i,subColumns[j]).toString()%></font>
                    </span>
                    </td>
                    <% } }%>    
                    </tr>
                    <% } %>
                    </table>
                    </div>
                    <div id="" style = "position:relative; height:50px"></div>
                    <% } %>
                </div>
                <DIV id = "divArea0_6">
                    <FONT COLOR="#000000" FACE="Times"><%=reportDataWrap.getTxtFooter()%></FONT>
                </DIV>
                <DIV id = "divArea0_7">
                    <FONT COLOR="#000000" FACE="Times"><%=reportDataWrap.getTxtPageNumber()%></FONT>
                </DIV>
                </div>
                
                </td></tr>
                </table>
                <div id="divFooter">&nbsp;</div>
        </div>
    </body>
</html>