<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.util.List,
         		 java.util.HashMap,	
         		 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.datawrap.TermAuditReportDataWrap,
                 com.globalsight.reports.Constants,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.webapp.WebAppConstants" session="true"
%>
<%
	SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	HashMap map = (HashMap)sessionManager.getReportAttribute(Constants.TERMAUDIT_REPORT_KEY);
	TermAuditReportDataWrap reportDataWrap = (TermAuditReportDataWrap)map.get(Constants.TERMAUDIT_REPORT_KEY+Constants.REPORT_DATA_WRAP);
	List tableHeadList = reportDataWrap.getTableHeadList();
	List tableContentList = (List)request.getAttribute(Constants.DATARESOURCE);
	List formLabel = (List)reportDataWrap.getCriteriaFormLabel();
	ResourceBundle bundle = PageHandler.getBundle(session);
%>
<HTML>
<HEAD>

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

<STYLE type="text/css">
SPAN.left { text-align: left }
SPAN.center { text-align: center }
SPAN.right { text-align: right }
#divArea0_0 {position: absolute; top: 43px; left: 595px;  width: 130px;  Font-Family: Times; Font-Size: 9pt;; z-index: 1}
#divArea0_1 {position: absolute; top: 44px; left: 99px;  width: 171px; ; z-index: 1}
#divArea0_2 {position: absolute; top: 86px; left: 279px;  width: 266px;  Font-Family: Arial; Font-Size: 14pt;; z-index: 1}
#divArea0_3 {position: absolute; top: 126px; left: 97px;  width: 297px;  Font-Family: Arial; Font-Size: 9pt;; z-index: 1}
#divArea0_4 {position: absolute; top: 143px; left: 97px;  width: 306px;  Font-Family: Arial; Font-Size: 9pt;; z-index: 1}
#divArea0_5 {position: absolute; top: 160px; left: 97px;  width: 122px;  Font-Family: Arial; Font-Size: 9pt;; z-index: 1}
#divArea0_6 {position: absolute; top: 173px; left: 97px;  width: 632px; ; z-index: 2}
#divArea0_7 {position: absolute; top: 182px; left: 99px;  width: 630px;  Font-Family: Arial; Font-Size: 9pt;; z-index: 1}
#divArea0_8 {position: absolute; top: 818px; left: 97px;  width: 131px;  Font-Family: Times; Font-Size: 9pt;; z-index: 1}
#divArea0_9 {position: absolute; top: 818px; left: 651px;  width: 62px;  Font-Family: Times; Font-Size: 9pt;; z-index: 1}
TD.size6 { Font-Size: 6pt}
TD.size9 { Font-Size: 9pt}
TD.size14 { Font-Size: 14pt}
#divToolBar {position: absolute; left:20px; top:0px; z-index: 2;}
#divFooter {position: absolute; top: 858px; z-index: 1;TGTG}
#divEntire {position: absolute; left:0px; top:24px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}
</STYLE>
<TITLE><%=bundle.getString("term_audit")%></TITLE>
</HEAD>

<BODY bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onAfterAction()">

<TABLE BACKGROUND=""><TR><TD>

<DIV ID="divToolBar" aligh=left>
	<table><tr height=40px><td>
		<form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=TermAudit&act=turnpage" method=post>
			<input type=hidden name=termauditReportPageNum value="1">
			<input type=Submit name=First value=<%=bundle.getString("lb_first")%>> 
		</form>
	</td><td>
		<form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=TermAudit&act=turnpage" method=post>
			<input type=hidden name=termauditReportPageNum value=<%=reportDataWrap.getCurrentPageNum()-1%>>
			<input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>> 
		</form>
	</td><td>
		<form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=TermAudit&act=turnpage" method=post>
			<input type=text name=termauditReportPageNum size=4 value=<%=reportDataWrap.getCurrentPageNum()%>>
		</form>
	</td><td>
		<form name=NextForm Action="/globalsight/TranswareReports?reportPageName=TermAudit&act=turnpage" method=post>
			<input type=hidden name=termauditReportPageNum value=<%=reportDataWrap.getCurrentPageNum()+1%> style="width:30px">
			<input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
		</form>
	</td><td>
	 	<form name=LastForm Action="/globalsight/TranswareReports?reportPageName=TermAudit&act=turnpage" method=post>
			<input type=hidden name=termauditReportPageNum value=<%=reportDataWrap.getTotalPageNum()%> style="width:30px">
			<input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
		</form>
	</td></tr></table>
</DIV>

<DIV id="divEntire">
	<DIV ID="divArea0_0" align=right><FONT COLOR="#000000" FACE="Times"><%=reportDataWrap.getTxtDate()%></FONT>
	</DIV>
	<DIV ID="divArea0_1"><IMG SRC="/globalsight/images/reports.gif" BORDER=0>
	</DIV>
	<DIV id="divArea0_2" align=center><FONT COLOR="#000000" FACE="Arial"><B><%=reportDataWrap.getReportTitle()%></B></FONT>
	</DIV>
	<DIV ID="divArea0_3"><FONT COLOR="#000000" FACE="Arial"><B><%=(String)formLabel.get(0)%></B></FONT>
	</DIV>
	<DIV ID="divArea0_4"><FONT COLOR="#000000" FACE="Arial"><B><%=(String)formLabel.get(1)%></B></FONT>
	</DIV>
	<DIV ID="divArea0_5"><FONT COLOR="#000000" FACE="Arial"><B><%=(String)formLabel.get(2)%></B></FONT>
	</DIV>
	<DIV ID="divArea0_6"><HR NOSHADE SIZE="1" WIDTH="100%" >
	</DIV>
	<DIV ID="divArea0_7">
		<TABLE CELLSPACING=0 CELLPADDING=0 WIDTH=630.45><TR>
		
		<!-- table header-->
		
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="58.050000000000004" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(0)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="162.0" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(1)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="85.05000000000001" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(2)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="64.80000000000001" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(3)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="94.5" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(4)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="62.1" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(5)%></B></FONT>
			</span>
		</TD>
		<TD  Style="border-top-style: solid ; border-top-color: #000000;  border-top-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size9 BGCOLOR="#0c1476" VALIGN=MIDDLE WIDTH="89.10000000000001" HEIGHT=32>
			<span class=left><FONT COLOR="#ffffff" FACE="Arial"><B><%=(String)tableHeadList.get(6)%></B></FONT>
			</span>
		</TD>
		</TR>
		
		<!-- table content -->
		
		<%
			List singleRowDataList = null;
			for(int i=0; i<tableContentList.size(); i++) {
				singleRowDataList = (List)tableContentList.get(i);
		%>
		<TR>
		<TD  Style="border-left-style: solid ; border-left-color: #000000;  border-left-width: 1;border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="58.050000000000004" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(0)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="162.0" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(1)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="85.05000000000001" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(2)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="64.80000000000001" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(3)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="94.5" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(4)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="62.1" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(5)%></FONT>
			</span>
		</TD>
		<TD  Style="border-bottom-style: solid ; border-bottom-color: #000000;  border-bottom-width: 1;border-right-style: solid ; border-right-color: #000000;  border-right-width: 1;padding-top: 1;padding-bottom: 1;padding-left: 1;padding-right: 1;" class=size6 VALIGN=MIDDLE WIDTH="89.10000000000001" HEIGHT=20>
			<span class=left><FONT COLOR="#000000" FACE="Arial"><%=(String)singleRowDataList.get(6)%></FONT>
			</span>
		</TD>
		</TR>
		<%
			}
		%>
		</TABLE>
	</DIV>
	
	<DIV ID="divArea0_8"><FONT COLOR="#000000" FACE="Times"><%=reportDataWrap.getTxtFooter()%></FONT>
	</DIV>
	<DIV ID="divArea0_9" align=right><FONT COLOR="#000000" FACE="Times"><%=reportDataWrap.getTxtPageNumber()%></FONT>
	</DIV>
        <div id="divFooter">&nbsp;</div>
</div>
</TD></TR></TABLE>

</BODY>
</HTML>