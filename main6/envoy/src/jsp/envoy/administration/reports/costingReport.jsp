<%@ page contentType="text/html; charset=UTF-8" 
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.Map,
                 java.util.LinkedHashMap,
                 java.util.Iterator,
                 java.util.Collection,
                 java.util.Locale,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 javax.swing.table.TableModel,
                 javax.swing.table.DefaultTableModel,
                 com.globalsight.reports.WorkflowTableModel,
                 com.globalsight.reports.WorkflowTableModel2,
                 com.globalsight.reports.Constants,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.reports.datawrap.CostingReportDataWrap,
                 com.globalsight.everest.webapp.WebAppConstants
                 " session="true"
%>
<%ResourceBundle bundle = PageHandler.getBundle(session); %>
<html>
    <head>
        <META content="text/html; charset=UTF-8" http-equiv="Content-Type">
        <META content="no-cache" http-equiv="Cache-Control">
        <META content="no-cache" http-equiv="Pragma">

        <style type="text/css">
            SPAN.left { text-align: left }
            SPAN.center { text-align: center }
            SPAN.right { text-align: right }
            #divToolBar {position: absolute; top: 0px;  left: 20px; z-index: 2;}
            #divArea0_0 {position: absolute; top: 43px; left: 595px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_1 {position: absolute; top: 44px; left: 99px;  width: 171px;  z-index: 1}
            #divArea0_2 {position: absolute; top: 86px; left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 14pt; z-index: 1}

            #divArea0_4 {left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_5 {left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
            #divArea0_6 {left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            
            #divArea0_7 {left: 99px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_8 {left: 545px;  width: 180px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}

            td.size9 { Font-Size: 9pt}
            td.size14 { Font-Size: 14pt}
            #divFooter {position: absolute; top: 858px; z-index: 1;TGTG}
            #divEntire {position: absolute; left:0px; top:0px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}
        </style>
        <title>Costing</title>
    </head>
    
    <BODY bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onAfterAction()">
        <% 
          SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
          HashMap map = (HashMap)sessionManager.getReportAttribute(Constants.COSTING_REPORT_KEY);
          CostingReportDataWrap reportDataObject = (CostingReportDataWrap)map.get(Constants.COSTING_REPORT_KEY+Constants.REPORT_DATA_WRAP);
          int currentPageNum = reportDataObject.getCurrentPageNum();
          LinkedHashMap curPageLinkMap = reportDataObject.gainCurrentPageData(currentPageNum);
	  Map lineInGreepMap= (HashMap)reportDataObject.getLineInGreenMap();
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
		<form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=CostingReport&act=turnpage" method=post>
			<input type=hidden name=costingReportPageNum value="1">
			<input type=Submit name=First value=<%=bundle.getString("lb_first")%>>
		</form>
	</td>
        <td>
		<form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=CostingReport&act=turnpage" method=post>
			<input type=hidden name=costingReportPageNum value=<%=currentPageNum - 1 %> style="width:30px">
			<input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>>
		</form>
	</td>
        <td>
		<form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=CostingReport&act=turnpage" method=post>
			<input type=text name=costingReportPageNum size=4 value=<%=currentPageNum%>>
		</form>
	</td>
        <td>
		<form name=NextForm Action="/globalsight/TranswareReports?reportPageName=CostingReport&act=turnpage" method=post>
			<input type=hidden name=costingReportPageNum value=<%=currentPageNum + 1%> style="width:30px">
			<input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
		</form>
	</td>
        <td>
	 	<form name=LastForm Action="/globalsight/TranswareReports?reportPageName=CostingReport&act=turnpage" method=post>
			<input type=hidden name=costingReportPageNum value=<%=reportDataObject.getTotalPageNum()%> style="width:30px">
			<input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
		</form>
	</td>
        </tr></table>
</DIV>

<div id="divEntire">
  <TABLE>
    <TR>
      <TD>
        <div id="divarea0_0" align=right><font color="#000000" face="times"><%=reportDataObject.getTxtDate()%></font></div>
        <div id="divarea0_1"><img src="/globalsight/images/reports.gif" border=0></div>
      </TD>
    </TR>
    <TR>
    <TD>
    <DIV ID="divArea0_2">
    <TABLE  valign="top" border="0" height="738">
            <TR>
            <TD colspan="2" valign="top">
    <TABLE>
    <%
      if(Constants.FIRST_PAGE_NUM == currentPageNum )
      {

        ArrayList jobFormLable = (ArrayList)(curPageLinkMap.get(Constants.CONTENT_TYPE_LABEL));
        ArrayList jobFormValue = (ArrayList)(curPageLinkMap.get(Constants.CONTENT_TYPE_FIELD));
    %>       
    <TR>
      <TD>
        <DIV ID="" align=center>
          <FONT COLOR="#000000" FACE="Arial"><B><%=reportDataObject.getReportTitle()%></B></FONT>
          <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
        </DIV>
      </TD>
    </TR>
            
    <TR>
      <TD>
        <DIV ID="divArea0_4" align=center><HR NOSHADE SIZE="1" WIDTH=628 ></DIV>
        <DIV id="divArea0_5">

        <DIV id="">
           <TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=627.75>
            <%
              for(int indexJobForm = 0; indexJobForm < jobFormLable.size(); indexJobForm++)
              {
            %>
            <TR>
              <TD  Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-left-style: none ; border-left-color: #000000;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #000000;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 WIDTH="194.4" HEIGHT=16>
                <span class=left><FONT FACE="Arial"><B><%=jobFormLable.get(indexJobForm)%></B></FONT></span>
              </TD>
              <TD  Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #000000;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 WIDTH="433.35" HEIGHT=16>
                <span class=left><FONT FACE="Times"><%=jobFormValue.get(indexJobForm)%></FONT></span>
              </TD>
            </TR>
            <% } %>
          </TABLE>
        </DIV>
        <div id="" style = "position:relative; height:50px"></div>
        <DIV ID=""><FONT COLOR="#ff0000" FACE="Arial">
          <B>*<%=curPageLinkMap.get(Constants.CONTENT_TYPE_NOTE)%></B>
        </FONT></DIV>
      </DIV>
      </TD>
    </TR>
    <%
      }
      else
      {
    %>
    <TR>
      <TD>
        <DIV ID="divArea0_4" align=center><HR NOSHADE SIZE="1" WIDTH=628 ></DIV>
        <DIV id="divArea0_5">

        <DIV id="">
    <%  
        for(Iterator iter = (curPageLinkMap.keySet()).iterator(); iter.hasNext();)
        {
          String keyName = (String)iter.next();
          if( keyName.startsWith(Constants.CONTENT_TYPE_LABEL) )
          {
    %>
            <DIV ID=""><FONT COLOR="#000000" FACE="Arial"><B><%=curPageLinkMap.get(keyName)%></B></FONT></DIV>
    <%
          }
          if( keyName.startsWith(Constants.SURCHARGES_TABLE) )
          {
            TableModel tm = (TableModel)curPageLinkMap.get(keyName);
    %>
        <DIV ID="">
          <table border=0 width=100% cellpadding=0 cellspacing=0>
            <tr>
            <%
              for(int indexTableColumns = 0; indexTableColumns < tm.getColumnCount(); indexTableColumns++)
              {
            %>
              <td  style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 bgcolor="#0c1476" valign=middle align=center width="241.65" height=16>
                <span class=center><font color="#ffffff" face="Arial"><%=tm.getColumnName(indexTableColumns)%></font></span>
              </td>
            <% }   %>
            </tr>
            
          <%
            for(int indexRows = 0; indexRows < tm.getRowCount(); indexRows++)
            {
          %>
            <tr>
          <%
              for(int indexTableColumns2 = 0; indexTableColumns2 < tm.getColumnCount(); indexTableColumns2++)
              {
          %>
              <td Style="border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE WIDTH="241.65" HEIGHT=16>
                <span class=center><font color="#000000" face="Arial"><%=tm.getValueAt(indexRows,indexTableColumns2)%></font></span>
              </td>
          <%   }    %>
            </tr>
          <% }  %>
          </table>
        </DIV>
        <DIV ID="" align=center><HR NOSHADE SIZE="1" WIDTH=628 ></DIV>
       <% 
          }  // end if
          if( keyName.startsWith(Constants.CONTENT_TYPE_TABLE_MODEL) )
          {
            TableModel wtm = (TableModel)curPageLinkMap.get(keyName);
            int[] subCols = (int[])curPageLinkMap.get(Constants.CONTENT_TYPE_INTETER_ARRAY);
            String note = (String)curPageLinkMap.get(Constants.CONTENT_TYPE_NOTE);
       %>
        <DIV ID="">
          <table border=0 width=100% cellpadding=0 cellspacing=0>
            <tr>
            <%
              for(int indexWtmCol = 0; indexWtmCol < subCols.length; indexWtmCol++)
              {
            %>
              <td  style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 bgcolor="#0c1476" valign=middle align=center width="55.35" height=72>
                <span class=center><font color="#ffffff" face="Arial"><%=wtm.getColumnName(subCols[indexWtmCol])%></font></span>
              </td>
            <% }   %>
            </tr>
            
          <%
            for(int indexRows = 0; indexRows < wtm.getRowCount(); indexRows++)
            {
          %>
            <tr>
          <%
              for(int indexWtmCol2 = 0; indexWtmCol2 < subCols.length; indexWtmCol2++)
              {
          %>
              <td Style="border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE width="55.35" height=72>
                <span class=center><font color="#000000" face="Arial"><%=wtm.getValueAt(indexRows,subCols[indexWtmCol2])%></font></span>
              </td>
          <%   }    %>
            </tr>
          <% }  %>
          </table>
        </DIV>
        <DIV ID="" align=center><HR NOSHADE SIZE="1" WIDTH=628 ></DIV>
        <DIV ID="" align=left><FONT COLOR="#ff0000" FACE="Arial"><B><%=note%></B></FONT></DIV>
      <%
          } // end if
          if( keyName.startsWith(Constants.CONTENT_TYPE_TITLE) )
          {
      %>
        <DIV ID="" align=center>
          <FONT COLOR="#000000" FACE="Arial"><B><%=curPageLinkMap.get(keyName)%></B></FONT>
        </DIV>
      <% 
          }
          if( keyName.startsWith(Constants.ACTIVITY_TABLE) )
          {
            TableModel activityTm = (TableModel)curPageLinkMap.get(keyName);
      %>
        <DIV ID="">
          <TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=629.1>
            <TR>
          <%
            int tableColumns =  activityTm.getColumnCount();
	    String lineInNum = lineInGreepMap.get(keyName) == null ? null :lineInGreepMap.get(keyName).toString();
	    int lineNumber = lineInNum == null ? -1 : Integer.valueOf(lineInNum).intValue();
            for(int indexColumns = 1; indexColumns < tableColumns; indexColumns++)
            {
          %>
	  
              <TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE align=center WIDTH="75.60000000000001" HEIGHT=30>
                 <span class=center><FONT COLOR="#ffffff" FACE="Arial"><%=activityTm.getColumnName(indexColumns)%></FONT></span>
              </TD>
          <% }  %>
            </TR>
          <%
               int tableRows =  activityTm.getRowCount();
               for(int indexRow = 0; indexRow < tableRows; indexRow++)
               {
	       if(indexRow == lineNumber){
	       //if the indexRow equals to the specified line number, show the line with
	       //the green color
          %>
            <TR>
          <%
                 for(int indexColumns2 = 1; indexColumns2 < tableColumns; indexColumns2++)
                 {
          %>
              <TD Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE WIDTH="205.20000000000002" HEIGHT=30 >
                   <span class=left><FONT COLOR="#009966" FACE="Arial"><%=activityTm.getValueAt(indexRow,indexColumns2)%></FONT></span>
              </TD>
	  <%      } // end for %>
            </TR>
	    <%   }//end if
	    else { %>
            <TR>
          <%
                 for(int indexColumns2 = 1; indexColumns2 < tableColumns; indexColumns2++)
                 {
          %>
              <TD Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE WIDTH="205.20000000000002" HEIGHT=30>
                   <span class=left><FONT COLOR="#000000" FACE="Arial"><%=activityTm.getValueAt(indexRow,indexColumns2)%></FONT></span>
              </TD>
	  <%      } // end for %>
            </TR>

	  <%}// end else%>
          <% } // end for %>
          </TABLE>
        </DIV>
        <div id="" style = "position:relative; height:30px"></div>
        <DIV ID=""><HR NOSHADE SIZE="1" WIDTH="100%" ></DIV>
        <%
          } // end if
          if( keyName.startsWith(Constants.PAGENAME_TABLE) )
          {
            TableModel activityTm = (TableModel)curPageLinkMap.get(keyName);
        %>
        <DIV ID="">
          <TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=629.1>
            <TR>
        <%
            int tableWithPageColumns =  activityTm.getColumnCount();
            for(int indexWithPageColumns = 0; indexWithPageColumns < tableWithPageColumns; indexWithPageColumns++)
            {
        %>
             <TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE align=center WIDTH="176.85000000000002" HEIGHT=16>
               <span class=center><FONT COLOR="#ffffff" FACE="Arial"><%=activityTm.getColumnName(indexWithPageColumns)%></FONT></span>
             </TD>
        <%  } //end for  %>
            </TR>
        <%
              int tableWithPageRows =  activityTm.getRowCount();
              for(int indexWithPageRow = 0; indexWithPageRow < tableWithPageRows; indexWithPageRow++)
              {
        %>
            <TR>
        <%
              for(int indexWithPageColumns2 = 0; indexWithPageColumns2 < tableWithPageColumns; indexWithPageColumns2++)
              {
        %>
              <TD  Style="border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE WIDTH="448.20000000000005" HEIGHT=16>
                 <span class=left><FONT COLOR="#000000" FACE="Arial"><%=activityTm.getValueAt(indexWithPageRow,indexWithPageColumns2)%></FONT></span>
              </TD>
            <%  }//end for  %>
            </TR>
        <% }//end for  %>
          </TABLE>
        </DIV>
    <%
          }  // end if
        }// end for
    %>
      </DIV>
      </TD>
    </TR>
    <%
      }// end else
    %>

    </TABLE>
    </TR></TD>
    <tr><td colspan="2" height="20">&nbsp</td></tr>
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
            <tr><td colspan="2" height="20">&nbsp</td></tr>
            </TABLE>
            </DIV>
           
           </TD>
           </TR>
             <div id="divFooter">&nbsp;</div>
	    </TABLE>

</div>

</BODY>
</HTML>
