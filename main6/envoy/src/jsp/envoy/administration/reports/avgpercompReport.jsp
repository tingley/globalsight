<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.Iterator,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.WorkflowTableModel,
                 com.globalsight.reports.Constants,
                 com.globalsight.reports.datawrap.AvgPerCompReportDataWrap" session="true"
%>
<% AvgPerCompReportDataWrap reportDataWrap = (AvgPerCompReportDataWrap)request.getAttribute(Constants.AVGPERCOMP_REPORT_DATA);

              int jobNum = reportDataWrap.getTotalJobNum().intValue();
              ArrayList criteriaFormLabel = reportDataWrap.getCriteriaFormLabel();
              ArrayList criteriaFormValue = reportDataWrap.getCriteriaFormValue();
              HashMap job2WorkFlowMap = reportDataWrap.getAvgPerCompMap();
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
            #divToolBar {position: absolute; left:20px; top:0px; z-index: 2;}
            #divArea0_0 {position: absolute; top: 43px; left: 595px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_1 {position: absolute; top: 44px; left: 99px;  width: 171px;  z-index: 1}
            #divArea0_2 {position: absolute; top: 86px; left: 126px;  width: 575px;  Font-Family: Arial; Font-Size: 14pt;; z-index: 1}
            #divArea0_3 {position: absolute; top: 126px; left: 99px;  width: 692px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_4 {position: absolute; top: 174px; left: 324px;  width: 177px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
			<%
             	if( reportDataWrap.getCurrentPageNum() == 1) {
            %>
            	#divArea0_5 {position: absolute; top: 205px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
			<%
				} else {
			%>
				#divArea0_5 {position: absolute; top: 86px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
			<%
				}
			%>
            td.size9 { Font-Size: 9pt}
            td.size14 { Font-Size: 14pt}
            #divArea0_6 {position: absolute; top: 818px; left: 97px;  width: 131px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
			#divArea0_7 {position: absolute; top: 818px; left: 651px;  width: 62px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divFooter {position: absolute; top: 858px; z-index: 1;TGTG}
            #divEntire {position: absolute; left:0px; top:0px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}
        </style>
        <title><%=bundle.getString("avg_per_compAvg")%></title>
    </head>
    <body onload="onAfterAction()">
        <div id="divEntire">
        	<DIV ID="divToolBar" aligh=left>
				<table><tr height=40px><td>
				<form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=AvgPerComp&act=turnpage" method=post>
					<input type=hidden name=pageId value="1">
					<input type=Submit name=First value=<%=bundle.getString("lb_first")%>> 
				</form>
				</td><td>
				<form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=AvgPerComp&act=turnpage" method=post>
					<input type=hidden name=pageId value=<%=reportDataWrap.getCurrentPageNum()-1%>>
					<input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>> 
				</form>
				</td><td>
				<form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=AvgPerComp&act=turnpage" method=post>
					<input type=text name=pageId size=4 value=<%=reportDataWrap.getCurrentPageNum()%>>
				</form>
				</td><td>
				<form name=NextForm Action="/globalsight/TranswareReports?reportPageName=AvgPerComp&act=turnpage" method=post>
					<input type=hidden name=pageId value=<%=reportDataWrap.getCurrentPageNum()+1%> style="width:30px">
					<input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
				</form>
				</td><td>
	 			<form name=LastForm Action="/globalsight/TranswareReports?reportPageName=AvgPerComp&act=turnpage" method=post>
					<input type=hidden name=pageId value=<%=reportDataWrap.getTotalPageNum()%> style="width:30px">
					<input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
				</form>
				</td></tr></table>
			</DIV>
            <table>
                <DIV>
                <tr><td>
                <div id="divarea0_0" align=right><font color="#000000" face="times"><%=reportDataWrap.getTxtDate()%></font>
                </div>
                <div id="divarea0_1"><img src="/globalsight/images/reports.gif" border=0>
                </div>
                
                <%
                	if( reportDataWrap.getCurrentPageNum() == 1) {
                %>
                
                <div id="divarea0_2" align=center>
                <font color="#000000" face="arial"><b><%=reportDataWrap.getReportTitle()%></b></font>
                </div>
                
                <!--Report Head-->
                
                <div id="divarea0_3">
                    <table cellspacing=0 cellpadding=0 width=630>
                    <tr>
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="155" height=18>
                    <span class=right><font face="Arial"><b><%=criteriaFormLabel.get(0)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="162" height=18>
                    <span class=left><font face="arial"><%=criteriaFormValue.get(0)%></font> 
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="153" height=18>
                    <span class=right><font face="Arial"><b><%=criteriaFormLabel.get(1)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="155" height=18>
                    <span class=left><font face="Arial"><%=criteriaFormValue.get(1)%></font>
                    </span>
                    </td>
                    </tr>
                    </table>
                    <hr noshade size="1" width="100%" >
                </div>
                
                <!-- Job Num -->
                
                <div id="divArea0_4" align=center><font color="#000000" face="Arial">
                    <b><%=bundle.getString("total_number_of_jobs")%>&nbsp;<%=jobNum%></b></font>
                </div>
                </DIV>
                
                <% } %>
                
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
                
                <!--Job infor-->
                
                <div id=""><table cellspacing=0 cellpadding=0 width=627.75>
                <hr noshade size="1" width="100%" >
                    <tr>
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="116.10000000000001" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="184.95000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="183.60000000000002" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="143.10000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    </tr>
                    
                    <tr>
                    <td  style="border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="116.10000000000001" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  Style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="184.95000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="183.60000000000002" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="143.10000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    </tr>
                    
                    <tr>
                    <td  style="border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="116.10000000000001" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="184.95000000000002" height=18>
                    <span class=left><FONT FACE="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="183.60000000000002" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="143.10000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    </tr>
                    
                    <tr>
                    <td  style="border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="116.10000000000001" height=18>
                    <span class=right><font facE="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="184.95000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom align=right nowrap width="183.60000000000002" height=18>
                    <span class=right><font face="Arial"><b><%=jobFormLabels.get(index)%></b></font>
                    </span>
                    </td>
                    
                    <td  style="border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 valign=bottom nowrap width="143.10000000000002" height=18>
                    <span class=left><font face="Arial"><%=jobFormValues.get(index++)%></font>
                    </span>
                    </td>
                    </tr>
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
                    <td  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE align=center WIDTH="417.15000000000003" HEIGHT=16>
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
                    <td style="border-bottom-style:solid; border-bottom-color:#000000; border-bottom-width:1; border-left-style:solid; border-left-color:#000000; border-left-width:1; border-right-style:solid; border-right-color:#000000; border-right-width:1;" class=size9 valign=middle width="417" height=16>
                    <span class=left><font color="#000000" face="Arial"><%=wfTableModel.getValueAt(i,subColumns[j]).toString()%></font>
                    </span>
                    </td>
                    <% } else {
                    %>
                    <td style="border-bottom-style:solid; border-bottom-color:#000000; border-bottom-width:1; border-right-style:solid; border-right-color:#000000; border-right-width:1;" class=size9 valign=middle width="207" height=16>
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