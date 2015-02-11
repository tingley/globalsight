<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.util.comparator.TermbaseInfoComparator,
        com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
        com.globalsight.util.GlobalSightLocale,
        com.globalsight.terminology.TermbaseInfo,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.ServerProxy,
        java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="searchterm" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <!--  all valid term base list (TermbaseInfo) 
<jsp:useBean id="namelist" scope="request" class="java.util.ArrayList" />
-->
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	
	String title = bundle.getString("lb_termbase_search_title");
	String lbShow = bundle.getString("lb_show").toLowerCase(Locale.ENGLISH);
	String lbHidden = bundle.getString("lb_hidden").toLowerCase(Locale.ENGLISH);
	String lbGlossary = bundle.getString("lb_glossary");
	String lbSrcTerm = bundle.getString("lb_source_term");
	String lbTgtTerm = bundle.getString("lb_target_term");
	
	//all valid locale pairs info <GlobalSightLocale>
	Vector locales = (Vector) request.getAttribute(LocalePairConstants.LOCALES);

	List listSelectedTBNames = (List) request.getAttribute("selectedTBNamesList");
	List namelists = new ArrayList();
	List allTbName = (List) request.getAttribute("namelist");
	if(sessionMgr.getAttribute("tbListOfUser")!=null)
	{
	    ArrayList tmListOfUsers = (ArrayList)sessionMgr.getAttribute("tbListOfUser");
	    Iterator it = allTbName.iterator();
	    while(it.hasNext())
	    {
	        TermbaseInfo tbi = (TermbaseInfo)it.next();
	        if(tmListOfUsers.contains(tbi.getTermbaseId()))
	        {
	           namelists.add(tbi);
	        }
	    } 
	}
	else
	{
	    namelists = allTbName;
	}
	
	String sourceLangName = (String) request.getAttribute("sourceLangName");
	String targetLangName = (String) request.getAttribute("targetLangName");
	String searchstr = (String) request.getAttribute("searchstr");
	String matchtype = (String) request.getAttribute("matchtype");
	String action = (String) request.getAttribute("action");
	
	String sortBy = (String) request.getAttribute("sortBy");
	int _sortBy = 0;
	if ( sortBy != null && !"".equals(sortBy) && !"null".equals(sortBy) ){
		_sortBy = (new Integer(sortBy)).intValue();
	}
	
	int totalNum = ((Integer) request.getAttribute("totalNum")).intValue();
	int curPageNum = ((Integer) request.getAttribute("curPageNum")).intValue();
	int num_on_per_page = 15;
	
	int _rest = totalNum % num_on_per_page;
	int _round = Math.round(totalNum / num_on_per_page);
	int allPageNums = (_rest==0?_round:(_round+1));
	boolean navBtnVisible = true;
	boolean preBtnEnabled = true;
	boolean nextBtnEnabled = true;
	if ( allPageNums == 0 ){
		navBtnVisible = false;
	} else if ( allPageNums == 1 ){
		preBtnEnabled = false;
		nextBtnEnabled = false;
	} else if ( allPageNums > 1) {
		if ( curPageNum == 1 ){
			preBtnEnabled = false;
			nextBtnEnabled = true;
		} else if ( curPageNum == allPageNums) {
			preBtnEnabled = true;
			nextBtnEnabled = false;
		} else {
			preBtnEnabled = true;
			nextBtnEnabled = true;
		}
	}

	List listResults = (List) request.getAttribute("listResults");
	
	String urlSearchTerm = searchterm.getPageURL();
	String tbnames = "";
	if ( listSelectedTBNames != null && listSelectedTBNames.size()>0) {
		for (int i=0;i<listSelectedTBNames.size(); i++){
			if ("".equals(tbnames)){
				tbnames = (String) listSelectedTBNames.get(i);
			} else {
				tbnames += "," + (String) listSelectedTBNames.get(i);
			}
		}
	}

	String searchURL = urlSearchTerm + "&" + WebAppConstants.TERMBASE_ACTION + "=" + WebAppConstants.TERMBASE_ACTION_SEARCH_TERM;
	String searchURLForSort = searchURL + "&tbnames=" + tbnames + "&sourcelocale=" + sourceLangName +
								"&targetlocale=" + targetLangName + "&searchstr=" + searchstr + "&matchtype=" + matchtype +
								"&curPageNum=" + curPageNum + "&operation=sort";
%>

<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT type="text/javascript">
	var needWarning = false;
	var objectName = "Termbase";
	var guideNode = "terminology";
	var helpFile = "<%=bundle.getString("help_termbase_search_term")%>";
</SCRIPT>
<SCRIPT type="text/javascript">
function search(navDirection){
    var sourcelocale = document.getElementById("id_sourcelocale").value;
    if ( sourcelocale == null || sourcelocale == '-1' ) {
		alert("<%=bundle.getString("jsmsg_tb_maintenance_search_srclocale_empty") %>");
		document.getElementById("id_sourcelocale").focus();
		return false;
    }
    
    var targetlocale = document.getElementById("id_targetlocale").value;
    if ( targetlocale == null || targetlocale == '-1' ) {
		alert("<%=bundle.getString("jsmsg_tb_maintenance_search_tgtlocale_empty") %>");
		document.getElementById("id_targetlocale").focus();
		return false;
    }
    
    var matchtype = document.getElementById("id_matchtype").value;
    if ( matchtype == null ) {
		alert("<%=bundle.getString("jsmsg_tb_maintenance_search_matchtype_empty") %>");
		document.getElementById("id_matchtype").focus();
		return false;
    }
    
    var searchstr = document.getElementById("id_searchstr").value;
    if ( searchstr == null || trim(searchstr) == "" ) {
		alert("<%=bundle.getString("jsmsg_tb_maintenance_search_string_empty") %>");
		document.getElementById("id_searchstr").focus();
		return false;
    }
    
    var selectTBs = document.getElementById('id_tbnames');
    var select=true;
    for (var loop=0; loop < selectTBs.options.length; loop++)
    {
	    if (selectTBs.options[loop].selected == true)
		{
			select=false;
			break;
		}
	}
	if(select)
	{
		alert("<%=bundle.getString("jsmsg_tb_maintenance_search_tm_empty")%>");
		return false;
	}

	navagationTo(navDirection);
	var operation;
	if ( navDirection != null && (navDirection == 'pre' || navDirection == 'next') ) {
		operation = "nav";
	} else if ( navDirection == 'current' ) {
		operation = "search";
	} else {
		operation = "sort";
	}
//	alert("operation : " + operation);
	searchForm.action = "<%=searchURL%>&operation=" + operation;
	
    searchForm.submit();

    return true;
}

function navagationTo(navDirection) {
	var current_page_num = <%=(new Integer(curPageNum)).intValue()%>; //document.getElementById("id_current_page_num").value;
	current_page_num = parseInt(current_page_num);
	if ( navDirection != null && navDirection == 'pre' ) {
		current_page_num = current_page_num - 1;
	} else if ( navDirection == 'next') {
		current_page_num = current_page_num + 1;
	}
	document.getElementById("id_current_page_num").value = current_page_num;
}

function hiddenorShowDiv(){
	var searchDivLayer = document.getElementById("searchDiv");
	var hiddenOrShow = document.getElementById("hiddenOrShow");
	var btnValue = hiddenOrShow.value;
	if ( btnValue == "<%=lbShow %>" ){
		searchDivLayer.style.display='block';
		hiddenOrShow.value = "<%=lbHidden %>";
	} else {
		searchDivLayer.style.display='none';
		hiddenOrShow.value = "<%=lbShow %>";
	}
}

function Ltrim(str) {
	if( str.charAt(0) == " ") {
		str = str.substring(1, str.length);
		str = Ltrim(str);
	}  
    return str;
}  
 
function Rtrim(str) {
	if(str.charAt(str.length-1)==" "){
		str = str.substring(0,str.length-1);
		str = Rtrim(str);
	}
	return str;  
}
 
function trim(str) {
	var result = Ltrim(Rtrim(str));
	return result;
}

function setTermbasesState(trueOrfalse) {
//	alert("trueOrfalse :" + trueOrfalse);
	selectObj = document.getElementById('id_tbnames');
	for (var loop=0; loop < selectObj.options.length; loop++) {
		if (trueOrfalse=='true') {
			selectObj.options[loop].selected = true;
		} else {
			selectObj.options[loop].selected = false;
		}
	}
}

</SCRIPT>
</HEAD>

<BODY onload="" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
	<span class="mainHeading"><%= title %></span><br/>
	<span class="standardText">&nbsp;<%=bundle.getString("helper_text_tb_search_main") %></span>
	&nbsp;<input type="button" name="hiddenOrShow" id="hiddenOrShow" value="<%=lbHidden %>" onClick="hiddenorShowDiv();">
	
	<form name="searchForm" method="post" action="<%=searchURL%>">

	<input type="hidden" name="totalNum" id="id_total_num" value="<%=totalNum%>" />
	<input type="hidden" name="num_on_per_page" id="id_num_on_per_page" value="<%=num_on_per_page%>"/>
	<input type="hidden" name="curPageNum" id="id_current_page_num" value="<%=curPageNum%>" />
	<input type="hidden" name="sortBy" id="id_sortBy" value="<%=sortBy%>" />
	<input type="hidden" name="isShowSearchConditions" id="id_isShowSearchConditions" value="javascript:return getConditionStatus();">
	
	<div id="searchDiv" style="display:block;">
	<p>
	<table border="0" cellspacing="2" cellpadding="2" class="standardText">
		<tr>
			<td class="standardText" align="left" valign="top"><%=bundle.getString("lb_source_locale") %><SPAN CLASS="asterisk">*</SPAN>: </td>
			<td class="standardText" align="left" valign="top">
				<select name="sourcelocale" id="id_sourcelocale" class="standardText">
            <%
                out.println("<option value=\"-1\">&nbsp;</option>");
                boolean getSelected = false;
                for (int i = 0; i < locales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                    String localeDisplayName = locale.getDisplayName(uiLocale);
                    String lang_code = locale.getLanguageCode().toLowerCase().trim();
                    String country_code = locale.getCountryCode().toUpperCase().trim();
                    if ( sourceLangName != null && localeDisplayName.equals(sourceLangName)) {
                    	out.println("<option value=\"" + localeDisplayName + "\" selected>" + localeDisplayName + "</option>");
                    	getSelected = true;
                    } else if ( !getSelected && lang_code.equals("en") && country_code.equals("US") ){
                    	out.println("<option value=\"" + localeDisplayName + "\" selected>" + localeDisplayName + "</option>");
                    } else {
                        out.println("<option value=\"" + localeDisplayName + "\">" + localeDisplayName + "</option>");
                    }
                }
            %>
	            </select>
			</td>

			<td class="standardText" align="left" valign="top"><%=bundle.getString("lb_target_locale") %><SPAN CLASS="asterisk">*</SPAN>: </td>
			<td class="standardTest" align="left" valign="top">
		        <select name="targetlocale" id="id_targetlocale" class="standardText">
            <%
                out.println("<option value=\"-1\">&nbsp;</option>");
                for (int i = 0; i < locales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                    
                    String localeDisplayName = locale.getDisplayName(uiLocale);
                    if ( targetLangName != null && localeDisplayName.equals(targetLangName) ) {
                        out.println("<option value=\"" + localeDisplayName + "\" selected>" + localeDisplayName + "</option>");                    	
                    } else {
                    	out.println("<option value=\"" + localeDisplayName + "\">" + localeDisplayName + "</option>");
                    }
                }
            %>
            </select>
			</td>

		    <td class="standardText" align="left" valign="top"><%=bundle.getString("lb_termbase_select_tms") %>: </td>
			<td class="standardText" align="left" valign="top" rowspan="2">
				<select name="tbnames" id="id_tbnames" size="5" multiple>
					<%
//					out.println("<option value=\"-1\">&nbsp;</option>");
//					out.println("<option value=\"all\">all termbases</option>");
					for (int i = 0; i<namelists.size(); i++) {
						TermbaseInfo tbi = (TermbaseInfo) namelists.get(i);
						if (listSelectedTBNames.size() > 0 && listSelectedTBNames.contains(tbi.getName())) {
							out.println("<option value=\"" + tbi.getName() + "\" selected>" + tbi.getName() + "</option>");
						} else {
							out.println("<option value=\"" + tbi.getName() + "\">" + tbi.getName() + "</option>");
						}
					}
					%>
		        </select>
			</td>
		</tr>
		<tr>
			<td class="standardText" align="left"><%=bundle.getString("lb_match_type") %><SPAN CLASS="asterisk">*</SPAN>: </td>
			<td  class="standardText">
				<select name="matchtype" id="id_matchtype">
				<%
				String fmatch = bundle.getString("lb_fuzzy_match");
				String ematch = bundle.getString("lb_exact_match");
				if ( matchtype != null && "1".equals(matchtype) ) {
					out.println("<option value=\"1\" selected>" + ematch + "</option>");
					out.println("<option value=\"2\">" + fmatch + "</option>");
				} else {
					out.println("<option value=\"1\">" + ematch + "</option>");
					out.println("<option value=\"2\" selected>" + fmatch + "</option>");
				}
				%>
				</select>
			</td>
						
			<td class="standardText" align="left"><%=bundle.getString("lb_search_for") %><SPAN CLASS="asterisk">*</SPAN>: </td>
			<td class="standardTest"><input type="text" size="35" name="searchstr" id="id_searchstr" value="<%= searchstr == null?"":searchstr%>" onkeypress="if(event.keyCode==13||event.which==13){return false;}"></td>
			<td><input type="button" name="Submit" value="<%=bundle.getString("lb_search") %>" onClick="return search('current');"></td>
			<td>&nbsp;</td>
		</tr>
		
		<tr><td colspan="6" align="right" height="10">
			<A class="standardHREF" HREF="javascript:setTermbasesState('false');"><%=bundle.getString("lb_unselect_all") %></A> | <A class="standardHREF" HREF="javascript:setTermbasesState('true');"><%=bundle.getString("lb_select_all") %></A>
			</td>
		</tr>
		
	</table>
	</form>
	</p>
	</div>
	

	<table BORDER="0" CELLPADDING="0" CELLSPACING="0" CLASS="list" width="80%">
		<%
		if ( listResults != null && listResults.size() > 0) 
		{ %>
			<tr>
				<td colspan="3" class="standardText" align="right">
					<%=bundle.getString("lb_total_records") %>: <%=totalNum%> | <%=bundle.getString("lb_total_pages") %>: <%=allPageNums %> | <%=bundle.getString("lb_current_page") %>: <%=curPageNum %>
				</td>
			</tr>
			<tr class="tableHeadingBasic" VALIGN="middle">
				<td height="22" width="40%" class="headerCell">
					<A class="sortHREFWhite" href="<%=searchURLForSort%>&sortBy=<%=_sortBy==5?6:5%>"><%=lbSrcTerm %></A>
					<% if (_sortBy == 5 ) { %>
						<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>					
					<% } else if (_sortBy == 6) { %>
						<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>
					<% } %>
				</td>
	   			<td height="22" width="40%" class="headerCell">
	   				<A class="sortHREFWhite" href="<%=searchURLForSort%>&sortBy=<%=_sortBy==9?10:9%>"><%=lbTgtTerm %></A>
	   				<% if (_sortBy == 9 ) { %>
						<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>					
					<% } else if (_sortBy == 10) { %>
						<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>
					<% } %>
	   			</td>
	   			<td height="22" width="20%" class="headerCell">
	   				<A class="sortHREFWhite" href="<%=searchURLForSort%>&sortBy=<%=_sortBy==1?2:1%>"><%=lbGlossary %></A>
	   				<% if (_sortBy == 1 ) { %>
						<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>					
					<% } else if (_sortBy == 2) { %>
						<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>
					<% } %>
	   			</td>
			</tr>
		<%
			int count = 0;
			for (int i=0; i<listResults.size(); i++ ) {
				HashMap map = (HashMap) listResults.get(i);
				count++;
				if ( count % 2 == 1 ) {
					out.println("<tr VALIGN=\"TOP\" STYLE=\"padding-top: 5px; padding-bottom: 5px;\" BGCOLOR=\"#FFFFFF\">");
				} else {
					out.println("<tr VALIGN=\"TOP\" STYLE=\"padding-top: 5px; padding-bottom: 5px;\" BGCOLOR=\"#EEEEEE\">");
				}
			
				if ( i == (listResults.size()-1) ) {
					out.println("  <td width=\"40%\" style=\"BORDER-left:#0c1476 1px solid;border-bottom:#0c1476 1px solid;\" class=\"standardText\">" + map.get("src_term") + "</td>");
					out.println("  <td width=\"40%\" class=\"standardText\" style=\"border-bottom:#0c1476 1px solid;\">" + map.get("target_term") + "</td>");
					out.println("  <td width=\"20%\" style=\"BORDER-right:#0c1476 1px solid;border-bottom:#0c1476 1px solid;\" class=\"standardText\">" + map.get("tbname") + "</td>");
					out.println("</tr>");
				} else {
					out.println("  <td width=\"40%\" style=\"BORDER-left:#0c1476 1px solid;\" class=\"standardText\">" + map.get("src_term") + "</td>");
					out.println("  <td width=\"40%\" class=\"standardText\">" + map.get("target_term") + "</td>");
					out.println("  <td width=\"20%\" style=\"BORDER-right:#0c1476 1px solid;\" class=\"standardText\">" + map.get("tbname") + "</td>");
					out.println("</tr>");
				}
			}
		} else {
			if ( action != null && "searchterm".equals(action.toLowerCase()) ) {
				out.println("<tr class=\"tableHeadingBasic\" VALIGN=\"middle\">");
				out.println("  <td height=\"22\" width=\"40%\" class=\"headerCell\">" + lbSrcTerm + "</td>");
				out.println("  <td height=\"22\" width=\"40%\" class=\"headerCell\">" + lbTgtTerm + "</td>");
	   			out.println("  <td height=\"22\" width=\"20%\" class=\"headerCell\">"+ lbGlossary + "</td>");
				out.println("<tr>");
				out.println("<tr VALIGN=\"TOP\" STYLE=\"padding-top: 1px; padding-bottom: 1px;\" BGCOLOR=\"#FFFFFF\">");
				out.println("  <td colspan=\"3\" align=\"left\" style=\"BORDER-left:#0c1476 1px solid;border-bottom:#0c1476 1px solid;BORDER-right:#0c1476 1px solid;\">" + bundle.getString("lb_no_termbase_data_matches") + "</td>");
				out.println("</tr>");				
			}
		}
		%>
		
		<% if ( navBtnVisible == true ) { 
			out.println("<tr>");
			out.println("<td colspan=\"3\" class=\"standardText\" align=\"right\">");
			
			String lbPre = bundle.getString("lb_previous");
			String lbNext = bundle.getString("lb_next");
			if ( preBtnEnabled == true) {
				out.println(" <input type=\"button\" name=\"pre\" value=\"" + lbPre + "\" onClick=\"return search('pre');\" enabled>");
			} else {
				out.println(" <input type=\"button\" name=\"pre\" value=\"" + lbPre + "\" onClick=\"return search('pre');\" disabled>");
			}
			if ( nextBtnEnabled == true ) {
				out.println("<input type=\"button\" name=\"next\" value=\"" + lbNext + "\" onClick=\"return search('next');\" enabled>");
			} else {
				out.println("<input type=\"button\" name=\"next\" value=\"" + lbNext + "\" onClick=\"return search('next');\" disabled>");
			}
			out.println("</td>");
			out.println("</tr>");
		  } %>
		
    </table>

</DIV>
</body>
</html>
