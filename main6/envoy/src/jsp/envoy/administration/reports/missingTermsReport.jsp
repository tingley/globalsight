<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.util.ArrayList,
         java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.datawrap.MissingTermsDataWrap,
                 com.globalsight.reports.Constants" session="true"
%>
<%
	MissingTermsDataWrap missingtermsDataWrap = (MissingTermsDataWrap)request.getAttribute(Constants.MISSINGTERMS_REPORT_DATA);
	ArrayList missingItems = missingtermsDataWrap.getMissingItems();
	ArrayList criteriaFormLabel = missingtermsDataWrap.getCriteriaFormLabel();
	ArrayList criteriaFormValue = missingtermsDataWrap.getCriteriaFormValue();
	String pageFooter = missingtermsDataWrap.getPageFooter();
	String currentPageOfAll =  missingtermsDataWrap.getTxtPageNumber();
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
  	 if(<%=missingtermsDataWrap.getTotalPageNum()%> <= 1) {
  	 	FirstForm.First.disabled = true;
  	 	PreviousForm.Previous.disabled = true;
  	 	NextForm.Next.disabled = true;
  	 	LastForm.Last.disabled = true;
  	 } else {
  	 	if(<%=missingtermsDataWrap.getCurrentPageNum()%> == 1) {
  	 		FirstForm.First.disabled = true;
	  	 	PreviousForm.Previous.disabled = true;
	  	 	NextForm.Next.disabled = false;
	  	 	LastForm.Last.disabled = false;
  	 	} else if(<%=missingtermsDataWrap.getCurrentPageNum()%> == <%=missingtermsDataWrap.getTotalPageNum()%>) {
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
#divToolBar {position: absolute; left:20px; top:0px; z-index: 2;}
#divArea0_0 {position: absolute; top: 43px; left: 595px;  width: 130px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
#divArea0_1 {position: absolute; top: 44px; left: 99px;  width: 171px;   z-index: 1}
#divArea0_2 {position: absolute; top: 86px; left: 300px;  width: 227px;  Font-Family: Arial; Font-Size: 14pt; z-index: 1}
#divArea0_3 {position: absolute; top: 126px; left: 97px;  width: 632px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
#divArea0_4 {position: absolute; top: 139px; left: 97px;  width: 632px;  z-index: 2}
#divArea0_5 {position: absolute; top: 149px; left: 97px;  width: 404px;  Font-Family: Arial; Font-Size: 9pt; z-index: 1}
#divArea0_6 {position: absolute; top: 1045px; left: 97px;  width: 190px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
#divArea0_7 {position: absolute; top: 1045px; left: 400px;  width: 150px;  Font-Family: Times; Font-Size: 9pt; z-index: 1}
TD.size9 { Font-Size: 9pt}
TD.size14 { Font-Size: 14pt}
#divFooter {position: absolute; top: 990px; z-index: 1;TGTG}
#divEntire {position: absolute; left:0px; top:0px ; z-index:1;background-color:#ffffff;layer-background-color:#ffffff;}

</STYLE>

<TITLE><%=bundle.getString("missing_terms")%></TITLE>
</HEAD>

<BODY bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="onAfterAction()">

<TABLE BACKGROUND="">
<TR><TD>

<DIV ID="divToolBar" aligh=left>
	<table><tr height=40px><td>
		<form name=FirstForm Action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=turnpage" method=post>
			<input type=hidden name=pageId value="1">
			<input type=Submit name=First value=<%=bundle.getString("lb_first")%>> 
		</form>
	</td><td>
		<form name=PreviousForm Action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=turnpage" method=post>
			<input type=hidden name=pageId value=<%=missingtermsDataWrap.getCurrentPageNum()-1%>>
			<input type=Submit name=Previous value=<%=bundle.getString("lb_Prev")%>> 
		</form>
	</td><td>
		<form name=CustomForm Action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=turnpage" method=post>
			<input type=text name=pageId size=4 value=<%=missingtermsDataWrap.getCurrentPageNum()%>>
		</form>
	</td><td>
		<form name=NextForm Action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=turnpage" method=post>
			<input type=hidden name=pageId value=<%=missingtermsDataWrap.getCurrentPageNum()+1%> style="width:30px">
			<input type=Submit name=Next value=<%=bundle.getString("lb_next")%>>
		</form>
	</td><td>
	 	<form name=LastForm Action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=turnpage" method=post>
			<input type=hidden name=pageId value=<%=missingtermsDataWrap.getTotalPageNum()%> style="width:30px">
			<input type=Submit name=Last value=<%=bundle.getString("lb_last")%>> 
		</form>
	</td></tr></table>
</DIV>

<div id="divEntire">

<DIV ID="divArea0_0" align=right><FONT COLOR="#000000" FACE="Times"><%=missingtermsDataWrap.getTxtDate()%></FONT>
</DIV>

<DIV ID="divArea0_1"><IMG SRC="/globalsight/images/reports.gif" BORDER=0>
</DIV>

<DIV ID="divArea0_2" align=center><FONT COLOR="#000000" FACE="Arial"><B><%=missingtermsDataWrap.getReportTitle()%></B></FONT>
</DIV>

<DIV ID="divArea0_3"><FONT COLOR="#000000" FACE="Arial"><B><%=criteriaFormLabel.get(0)%><%=criteriaFormValue.get(0)%></B></FONT>
</DIV>

<DIV ID="divArea0_4"><HR NOSHADE SIZE="1" WIDTH="100%" >
</DIV>

<DIV ID="divArea0_5">

	<DIV ID=""><FONT COLOR="#000000" FACE="Arial"><B><%=missingtermsDataWrap.getPageHeader()%></B></FONT>
	</DIV>
	<DIV id="" style="position:relative; height:10px"></DIV>
	<DIV ID="">
		<%
			for(int i=0; i < missingItems.size(); i++) {
		%>
			<%= missingItems.get(i)%><br>
		<%
			}
		%>
	</DIV>
	
	<DIV ID=""><FONT COLOR="#000000" FACE="Arial"><B><%=missingtermsDataWrap.getPageFooter()%></B></FONT>
	</DIV>
	<DIV id="" style="position:relative; height:30px">
	</DIV>
</DIV>

<DIV ID="divArea0_6"><FONT COLOR="#000000" FACE="Times"><%=missingtermsDataWrap.getTxtFooter()%></FONT>
</DIV>

<DIV ID="divArea0_7" align=right><FONT COLOR="#000000" FACE="Times"><%=currentPageOfAll%></FONT>
</DIV>

<div id="divFooter">&nbsp;</div>
</TD></TR>

</TABLE>

</BODY>
</HTML>
