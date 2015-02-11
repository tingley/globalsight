<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.LinkedHashMap,
                 java.util.Iterator,
                 java.util.Collection,
                 javax.swing.table.TableModel,
                 javax.swing.table.DefaultTableModel,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.WorkflowTableModel,
                 com.globalsight.reports.Constants,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.reports.datawrap.JobDetailsReportDataWrap,
                 com.globalsight.everest.webapp.WebAppConstants
                 " session="true"
%>
    <%
    ResourceBundle bundle = PageHandler.getBundle(session);
    %>
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

            #divArea0_4 {position: absolute; top: 126px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_5 {position: absolute; top: 174px; left: 99px;  width: 628px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
            #divArea0_6 {position: absolute; top: 205px; left: 99px;  width: 628px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            
            #divArea0_7 {left: 99px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
            #divArea0_8 {left: 545px;  width: 180px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}

            td.size9 { Font-Size: 9pt}
            td.size14 { Font-Size: 14pt}
            #divFooter {position: absolute; top: 858px; z-index: 1;TGTG}
            #divEntire {position: absolute; left:0px; top:0px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}
        </style>
        <title><%=bundle.getString("lb_job_details")%></title>
    </head>
    <BODY bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onAfterAction()">
        <%
          SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
          HashMap map = (HashMap)sessionManager.getReportAttribute(Constants.JOBDETAILS_REPORT_KEY);
          JobDetailsReportDataWrap reportDataObject = (JobDetailsReportDataWrap)map.get(Constants.JOBDETAILS_REPORT_KEY+Constants.REPORT_DATA_WRAP);
          int currentPageNum = reportDataObject.getCurrentPageNum();
          LinkedHashMap curPageLinkMap = reportDataObject.gainCurrentPageData(currentPageNum);
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
		<form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=JobDetails&act=turnpage" method=post>
			<input type=hidden name=jobDetailsReportPageNum value="1">
			<input type=Submit name=First value=<%=bundle.getString("lb_first")%>>
		</form>
	</td>
        <td>
		<form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=JobDetails&act=turnpage" method=post>
			<input type=hidden name=jobDetailsReportPageNum value=<%=currentPageNum - 1 %> style="width:30px">
			<input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>>
		</form>
	</td>
        <td>
		<form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=JobDetails&act=turnpage" method=post>
			<input type=text name=jobDetailsReportPageNum size=4 value=<%=currentPageNum%>>
		</form>
	</td>
        <td>
		<form name=NextForm Action="/globalsight/TranswareReports?reportPageName=JobDetails&act=turnpage" method=post>
			<input type=hidden name=jobDetailsReportPageNum value=<%=currentPageNum + 1%> style="width:30px">
			<input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
		</form>
	</td>
        <td>
	 	<form name=LastForm Action="/globalsight/TranswareReports?reportPageName=JobDetails&act=turnpage" method=post>
			<input type=hidden name=jobDetailsReportPageNum value=<%=reportDataObject.getTotalPageNum()%> style="width:30px">
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
      
      <TABLE height="738" valign="top" border="0">
            <TR>
            <TD colspan="2" valign="top">

    <%
      if(Constants.FIRST_PAGE_NUM == currentPageNum )
      {
    %> 
        <DIV ID="" align=center>
          <FONT COLOR="#000000" FACE="Arial"><B><%=reportDataObject.getReportTitle()%></B></FONT>
          <table border=0 width=100% cellpadding=0 cellspacing=0><tr><td></td></tr></table>
        </DIV>
        <div id="" style = "position:relative; height:25px"></div>
    <%
      } // end if(Constants.FIRST_PAGE_NUM) 
      for(Iterator iter = (curPageLinkMap.keySet()).iterator(); iter.hasNext();)
      {
        String keyName = (String)iter.next();
        if( keyName.startsWith(Constants.CRITERIA_FORM) )
        {
          ArrayList criteriaFormLabel = reportDataObject.getCriteriaFormLabel();
          ArrayList criteriaFormValue = reportDataObject.getCriteriaFormValue();
    %>

        <DIV ID="">
        <TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=628>
          <TR>
        <%
            for(int indexCriteria = 0; indexCriteria < criteriaFormLabel.size(); indexCriteria++)
            {
        %>
            <TD Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-left-style: none ; border-left-color: #ffffff;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=BOTTOM align=right nowrap WIDTH="88.0" HEIGHT=18>
                <span class=right><FONT FACE="Arial"><B><%=criteriaFormLabel.get(indexCriteria)%> </B></FONT></span>
            </TD>
            <TD Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #ffffff;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=BOTTOM nowrap WIDTH="98.0" HEIGHT=18>
                <span class=left><FONT FACE="Arial"><%=criteriaFormValue.get(indexCriteria)%></FONT></span>
            </TD>
        <%   } // end for %>
            </TR>
          </TABLE>
        </DIV>
        <DIV ID="" align=center><HR NOSHADE SIZE="1" WIDTH=628 ></DIV>
      <%
          } // end if(Constants.CRITERIA_FORM)
          if( keyName.startsWith(Constants.JOB_FORM) )
          {
            ArrayList jobFormLabel = reportDataObject.getJobFormLabel();
            ArrayList jobFormValue = reportDataObject.getJobFormValue();
      %>
        <DIV id="">
          <TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=627.75>
        <%
            if(jobFormLabel == null)
            {
              jobFormLabel = new ArrayList();
            }
            for(int indexJobForm = 0; indexJobForm < jobFormLabel.size(); indexJobForm++)
            {
        %>
            <TR>
              <TD Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-left-style: none ; border-left-color: #000000;  border-left-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #000000;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 WIDTH="194.4" HEIGHT=16>
                <span class=left><FONT FACE="Arial"><B><%=jobFormLabel.get(indexJobForm)%></B></FONT></span>
              </TD>
              <TD  Style="border-top-style: none ; border-top-color: #000000;  border-top-width: 0;border-bottom-style: none ; border-bottom-color: #000000;  border-bottom-width: 0;border-right-style: none ; border-right-color: #000000;  border-right-width: 0;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 WIDTH="433.35" HEIGHT=16>
                <span class=left><FONT FACE="Times"><%=jobFormValue.get(indexJobForm)%></FONT></span>
              </TD>
            </TR>
        <%  } // end for %>
          </TABLE>
        </DIV>
        <div id="" style = "position:relative; height:50px"></div>
        
      <% 
          }  // end if(Constants.JOB_FORM)
          if( keyName.startsWith(Constants.CONTENT_TYPE_LABEL) )
          {
      %>
        <DIV ID=""><HR NOSHADE SIZE="1" WIDTH="100%" ></DIV>
        <DIV ID="">
          <FONT COLOR="#000000" FACE="Arial" Size="3"><B><%=curPageLinkMap.get(keyName)%></B></FONT>
        </DIV>
        <div id="" style = "position:relative; height:20px"></div>
      <% 
          }  // end if(Constants.CONTENT_TYPE_LABEL)
          if( keyName.startsWith(Constants.CONTENT_TYPE_TABLE_MODEL) )
          {
            TableModel wtm = (TableModel)curPageLinkMap.get(keyName);
            int[] subCols = (int[])curPageLinkMap.get(Constants.CONTENT_TYPE_INTETER_ARRAY);
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
            <% } // end for  %>
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
          <%   } // end for   %>
            </tr>
          <% }  // end for %>
          </table>
        </DIV>
        
        <%
          } // end if(Constants.CONTENT_TYPE_TABLE_MODEL)
          if( keyName.startsWith(Constants.CONTENT_TYPE_TITLE) )
          {
      %>
        <DIV ID="" align=center>
          <FONT COLOR="#000000" FACE="Arial" size="2"><B><%=curPageLinkMap.get(keyName)%></B></FONT>
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
          %>
            <TR>
          <%
                 for(int indexColumns2 = 1; indexColumns2 < tableColumns; indexColumns2++)
                 {
          %>
              <TD Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 VALIGN=MIDDLE WIDTH="205.20000000000002" HEIGHT=30>
                   <span class=left><FONT COLOR="#000000" FACE="Arial"><%=activityTm.getValueAt(indexRow,indexColumns2)%></FONT></span>
              </TD>
          <%  } // end for %>
            </TR>
          <% } // end for %>
          </TABLE>
        </DIV>
        <div id="" style = "position:relative; height:30px"></div>
        <DIV ID=""><HR NOSHADE SIZE="1" WIDTH="100%" ></DIV>
        <%
          } // end if(Constants.ACTIVITY_TABLE)
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
          }  // end if(Constants.PAGENAME_TABLE)
        }// end for(curPageLinkMap.keySet())
    %>
        
       
       
      	</TD>
	    </TR>
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
      



      
    </TD></TR>
    <div id="divFooter">&nbsp;</div>
  </TABLE>
</div>

</BODY>
</HTML>
