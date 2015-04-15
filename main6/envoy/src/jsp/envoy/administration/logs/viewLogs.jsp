<%@page import="com.globalsight.everest.servlet.util.SessionManager"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.logs.ViewLogsMainHandler,
                com.globalsight.util.j2ee.AppServerWrapperFactory,
                com.globalsight.util.j2ee.AppServerWrapper,                
                java.util.ResourceBundle,
				java.util.ArrayList"
	session="true"%>
<jsp:useBean id="packageLogs" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String message = (String)sessionMgr.getAttribute("message");
    
    String title= bundle.getString("lb_logs");
    String docsDir = (String) request.getAttribute(ViewLogsMainHandler.CXE_DOCS_DIR);
    String j2eeVendor = AppServerWrapperFactory.getAppServerWrapper().getJ2EEServerName();
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String today = sdf.format(now.getTime());
    String packageLogsUrl = packageLogs.getPageURL() + "&action=packageLogs";
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<STYLE>
body {
  font-size:11px;
}

li {
 list-style-type: none;
 padding-lef: 0px;
 padding-bottom: 5px;
}

TR.standardText {
	vertical-align: top;
}

TR.trodd {
	background-color: #eee;
}
</STYLE>

<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>

<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "logs";
    var helpFile = "<%=bundle.getString("help_log_files")%>";

	$(function() {
		$("#list tr:nth-child(even)").addClass("trodd");
		
        $("#selectedAll").click(function() {
            if ($("#selectedAll").is(":checked")) 
                $("#list :checkbox").attr("checked", true);
            else
                $("#list :checkbox").attr("checked", false);
        });
		
		$("#list :checkbox:not(#selectedAll)").click(function() {
		    $("#selectedAll").attr("checked", false);
		});
		
		var dates = $( "#fromDate, #toDate" ).datepicker({
			defaultDate: "+0w",
			changeMonth: true,
			dateFormat: "yy-mm-dd",
			numberOfMonths: 2,
			onSelect: function( selectedDate ) {
				var option = this.id == "fromDate" ? "minDate" : "maxDate",
					instance = $( this ).data( "datepicker" ),
					date = $.datepicker.parseDate(
						instance.settings.dateFormat ||
						$.datepicker._defaults.dateFormat,
						selectedDate, instance.settings );
				dates.not( this ).datepicker( "option", option, date );
			}
		});
	});

	function submitForm() {
		var startDate = $("#fromDate").val();
		var endDate = $("#toDate").val();
		if ($("#partDownload").is(":checked")) {
			if (startDate == "" || endDate == "") {
				alert("Please input correct start/end date.");
				return false;
			}
			var today = "<%=today %>";
			if (startDate > today || endDate > today) {
				alert("<%=bundle.getString("msg_log_wrong_time") %>");
				return false;
		    }		
		    
			if (startDate == endDate) {
			  var startHour = $("#fromHour").val();
			  var endHour = $("#toHour").val();
			  if (startHour > endHour || (startHour==endHour && $("#fromMinute").val() >= $("#toMinute").val())) {
				  alert("<%=bundle.getString("msg_log_wrong_time_1") %>");
				  return false;
			  }
			}
        }
        if ($("#daysDownload").is(":checked")) {
        	if (!isAllDigits($("#days").val()) || $("#days").val()>60 || $("#days").val()==0) {
        		alert("<%=bundle.getString("msg_log_wrong_days_1") %>");
        		return false;
        	}
        }
        
		var selected = false;
		$("#list :checkbox:not(#selectedAll)").each(function(index) {
			if ($(this).is(":checked"))
			  selected = true;
		});
		if (!selected) {
		  alert("<%=bundle.getString("msg_log_not_select") %>");
		  return false;
		}
		logForm.action = "/globalsight/envoy/administration/logs/downloadLogs.jsp";
		logForm.submit();
	}
	
	function fullDown() {
	  if ($("#fullDownload").is(":checked")) {
	    $("#fromDate").attr("disabled", true);
		$("#fromHour").attr("disabled", true);
		$("#fromMinute").attr("disabled", true);
	    $("#toDate").attr("disabled", true);
		$("#toHour").attr("disabled", true);
		$("#toMinute").attr("disabled", true);
		
		$("#days").attr("disabled", true);
	  }
	}
	
	function partDown() {
	  if ($("#partDownload").is(":checked")) {
	    $("#fromDate").attr("disabled", false);
		$("#fromHour").attr("disabled", false);
		$("#fromMinute").attr("disabled", false);
	    $("#toDate").attr("disabled", false);
		$("#toHour").attr("disabled", false);
		$("#toMinute").attr("disabled", false);
	  }
	}
	
	function daysDown() {
		partDown();
		
		$("#days").attr("disabled", false);
	}
	
	function packageLogs() {
		if ($("#zipLogs").is(":checked")) {
			$("#logDays").attr("disabled", false);
			$("#zipLogsBtn").attr("disabled", false);
		}
	}
	
	function zipLogFiles() {
		if (!isAllDigits($("#logDays").val())) {
            alert("<%=bundle.getString("msg_log_wrong_days") %>");
            return false;
        }
		var selected = false;
		$("#list :checkbox:not(#selectedAll)").each(function(index) {
			if ($(this).is(":checked"))
			  selected = true;
		});
		if (!selected) {
		  alert("<%=bundle.getString("msg_log_not_select") %>");
		  return false;
		}
        logForm.action = "<%=packageLogsUrl %>";
        logForm.submit();
	}
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" ONLOAD="loadGuides()">
	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<DIV ID="contentLayer"
		STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

		<SPAN CLASS="mainHeading"> <%=title%> </SPAN>

		<P>
		<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
			<TR>
				<TD WIDTH=538><%=bundle.getString("helper_text_logs")%></TD>
			</TR>
		</TABLE>
		<P>
		<form id="logForm" name="logForm" method="post">
		<div>
  	     <ul class="standardText">
           <li>
             <%
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss [zzzz]");
             %>
             &nbsp;&nbsp;&nbsp;&nbsp;<b><i><%=bundle.getString("log_server_time") %> <%=dateFormat.format(now.getTime()) %></i></b>
           </li>
		   <li>
		     <input type="radio" id="fullDownload" name="downloadOption" value="full" checked onclick="fullDown()" /><%=bundle.getString("log_full_files") %>
		   </li>
		   <li>
		    <input type="radio" id="partDownload" name="downloadOption" value="part" onclick="partDown()" /><%=bundle.getString("log_part_files") %> &nbsp;&nbsp;
			<label for="fromDate">From</label>
			<input type="text" id="fromDate" name="fromDate" disabled=true>
		      <select id="fromHour" name="fromHour" disabled=true>
		        <%
				String tmp = "";
		        for (int i = 0; i < 24; i++) {
					if (i<10)
						tmp = "0" + i;
					else
						tmp = "" + i;
		            out.println("<option value='" + tmp + "'>" + tmp + "</option>");
				}
		        %>
		      </select>
		      <select id="fromMinute" name="fromMinute" disabled=true>
		        <%
		        for (int i = 0; i < 60; i++) {
					if (i<10)
					  tmp = "0"+i;
					else
					  tmp = String.valueOf(i);
		            out.println("<option value='" + tmp + "'>" + tmp + "</option>");
				}
		        %>
		      </select>
			  &nbsp;
		    <label for="toDate">To</label>
            <input type="text" id="toDate" name="toDate" maxlength="10" disabled=true>
              <select id="toHour" name="toHour" disabled=true>
                <%
                for (int i = 0; i < 23; i++) {
				  if (i<10)
				    tmp = "0" + i;
				  else
				    tmp = String.valueOf(i);
                    out.println("<option value='" + tmp + "'>" + tmp + "</option>");
				}
                out.println("<option value='23' selected>23</option>");
                %>
              </select>
              <select id="toMinute" name="toMinute" disabled=true>
                <%
                for (int i = 0; i < 59; i++) {
				  if (i<10)
				    tmp = "0" + i;
				  else
				    tmp = String.valueOf(i);
                    out.println("<option value='" + tmp + "'>" + tmp + "</option>");
				}
                out.println("<option value='59' selected>59</option>");
                %>
              </select>
			</li>
            <li>
              <input type="radio" id="daysDownload" name="downloadOption" value="days" onclick="daysDown()" /><%=bundle.getString("log_since_files") %>&nbsp;&nbsp;
              <input type="text" id="days" name="days" size="10" value="3"/> <%=bundle.getString("lb_days") %>
            </li>
			<li>
			</li>
			<li>
				<input type="button" id="download" name="download" value="<%=bundle.getString("lb_download")%>" onclick="submitForm()"/>&nbsp;&nbsp;
			</li>
			<!-- 
            <li>
            </li>
            <li>
              <input type="radio" id="zipLogs" name="downloadOption" value="zipLogs" onclick="packageLogs()"/><%=bundle.getString("log_zip_files") %>&nbsp;&nbsp;
              <input type="text" id="logDays" name="logDays" size="10" value="3" disabled /> <%=bundle.getString("lb_days") %>
            </li>
            <li>
                <input type="button" id="zipLogsBtn" name="zipLogsBtn" value="Package Log files" disabled onclick="zipLogFiles()" />
            </li>
             -->
		</ul>
		</div>
		<p>
		<TABLE id="list" BORDER="0" CELLPADDING="4" CELLSPACING="0" WIDTH="700"	CLASS="standardText">
			<TR>
			    <td class="tableHeadingBasic">
			      <input type="checkbox" id="selectedAll" name="selectedAll" />
			    </td>
				<TD CLASS="tableHeadingBasic"><%=bundle.getString("log_table_header1")%></TD>
				<TD CLASS="tableHeadingBasic"><%=bundle.getString("log_table_header2")%></TD>
			</TR>
			<TR>
			    <td>
			      <input type="checkbox" id="globalsight" name="globalsight" value="GlobalSight.log" />
			    </td>
				<TD><%=bundle.getString("log_cap_file") %></TD>
				<TD><%=bundle.getString("log_cap")%></TD>
			</TR>
            <TR>
                <td>
                  <input type="checkbox" id="activity" name="activity" value="activity.log" />
                </td>
                <TD><%=bundle.getString("log_activity_file") %></TD>
                <TD><%=bundle.getString("log_activity")%></TD>
            </TR>
            <TR>
                <td>
                  <input type="checkbox" id="operation" name="operation" value="operation.log" />
                </td>
                <TD><%=bundle.getString("log_operation_file") %></TD>
                <TD><%=bundle.getString("log_operation")%></TD>
            </TR>
            <TR>
                <td>
                  <input type="checkbox" id="webservices" name="webservices" value="webservices.log" />
                </td>
                <TD><%=bundle.getString("log_webservices_file") %></TD>
                <TD><%=bundle.getString("log_webservices")%></TD>
            </TR>
<!-- 
            <TR>
                <td>
                  <input type="checkbox" id="jboss" name="jboss" value="JBoss_GlobalSight.log" />
                </td>
                <TD><%=bundle.getString("log_jboss_wrapper_file") %></TD>
                <TD><%=bundle.getString("log_jboss_wrapper")%></TD>
            </TR>
			<TR>
                <td>
                  <input type="checkbox" id="termAudit" name="termAudit" value="1" />
                </td>
				<TD><%=bundle.getString("log_term_audit_file") %></TD>
				<TD><%=bundle.getString("log_tb_audit")%></TD>
			</TR>
-->
		</TABLE>
        </form>
</BODY>
</HTML>

