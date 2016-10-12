<!--
Title: Tigra Calendar
URL: http://www.softcomplex.com/products/tigra_calendar/
Version: 3.2
Date: 10/14/2002 (mm/dd/yyyy)
Note: Permission given to use this script in ANY kind of applications if
   header lines are left unchanged.
Note: Script consists of two files: calendar?.js and calendar.html
-->
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
         com.globalsight.everest.webapp.pagehandler.PageHandler"
         session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<head>
<title><%=bundle.getString("calendar_title")%> </title>
<style>
	td {font-family: Tahoma, Verdana, sans-serif; font-size: 12px;}
</style>
<script language="JavaScript">

// months as they appear in the calendar's title
var ARR_MONTHS = ["<%=bundle.getString("lb_january")%>", "<%=bundle.getString("lb_february")%>", 
                  "<%=bundle.getString("lb_march")%>", "<%=bundle.getString("lb_april")%>", 
                  "<%=bundle.getString("lb_may")%>", "<%=bundle.getString("lb_june")%>",
		"<%=bundle.getString("lb_july")%>", "<%=bundle.getString("lb_august")%>", 
		"<%=bundle.getString("lb_september")%>", "<%=bundle.getString("lb_october")%>", 
		"<%=bundle.getString("lb_november")%>", "<%=bundle.getString("lb_december")%>"];
// week day titles as they appear on the calendar
var ARR_WEEKDAYS = ["<%=bundle.getString("su")%>", "<%=bundle.getString("mo")%>", 
                    "<%=bundle.getString("tu")%>", "<%=bundle.getString("we")%>", 
                    "<%=bundle.getString("th")%>", "<%=bundle.getString("fr")%>", 
                    "<%=bundle.getString("sa")%>"];
// day week starts from (normally 0-Su or 1-Mo)
var NUM_WEEKSTART = 1;
// path to the directory where calendar images are stored. trailing slash req.
var STR_ICONPATH = 'img/';

var re_url = new RegExp('datetime=(\\-?\\d+)');
var dt_current = (re_url.exec(String(window.location))
	? new Date(new Number(RegExp.$1)) : new Date());
var re_id = new RegExp('id=(\\d+)');
var num_id = (re_id.exec(String(window.location))
	? new Number(RegExp.$1) : 0);
var obj_caller = (window.opener ? window.opener.calendars[num_id] : null);

if (obj_caller && obj_caller.year_scroll) {
	// get same date in the previous year
	var dt_prev_year = new Date(dt_current);
	dt_prev_year.setFullYear(dt_prev_year.getFullYear() - 1);
	if (dt_prev_year.getDate() != dt_current.getDate())
		dt_prev_year.setDate(0);
	
	// get same date in the next year
	var dt_next_year = new Date(dt_current);
	dt_next_year.setFullYear(dt_next_year.getFullYear() + 1);
	if (dt_next_year.getDate() != dt_current.getDate())
		dt_next_year.setDate(0);
}

// get same date in the previous month
var dt_prev_month = new Date(dt_current);
dt_prev_month.setMonth(dt_prev_month.getMonth() - 1);
if (dt_prev_month.getDate() != dt_current.getDate())
	dt_prev_month.setDate(0);

// get same date in the next month
var dt_next_month = new Date(dt_current);
dt_next_month.setMonth(dt_next_month.getMonth() + 1);
if (dt_next_month.getDate() != dt_current.getDate())
	dt_next_month.setDate(0);

// get first day to display in the grid for current month
var dt_firstday = new Date(dt_current);
dt_firstday.setDate(1);
dt_firstday.setDate(1 - (7 + dt_firstday.getDay() - NUM_WEEKSTART) % 7);

// function passing selected date to calling window
function set_datetime(n_datetime, b_close) {
	if (!obj_caller) return;

	var dt_datetime = obj_caller.prs_time(
		(document.cal ? document.cal.time.value : ''),
		new Date(n_datetime)
	);

	if (!dt_datetime) return;
	if (b_close) {
		window.close();
		obj_caller.target.value = (document.cal
			? obj_caller.gen_tsmp(dt_datetime)
			: obj_caller.gen_date(dt_datetime)
		);

		if (obj_caller.afterObj)
		{
			eval("obj_caller.afterObj." + obj_caller.afterMethod);
		}
	}
	else obj_caller.popup(dt_datetime.valueOf());
}

</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body bgcolor="#FFFFFF" marginheight="5" marginwidth="5" topmargin="5" leftmargin="5" rightmargin="5">
<table class="clsOTable" cellspacing="0" border="0" width="100%">
<tr><td bgcolor="#4682B4">
<table cellspacing="1" cellpadding="3" border="0" width="100%">
<tr><td colspan="7"><table cellspacing="0" cellpadding="0" border="0" width="100%">
<tr>
<script language="JavaScript">
document.write(
'<td>'+(obj_caller&&obj_caller.year_scroll?'<a href="javascript:set_datetime('+dt_prev_year.valueOf()+')"><img src="'+STR_ICONPATH+'prev_year.gif" width="16" height="16" border="0" alt="previous year"></a>&nbsp;':'')+'<a href="javascript:set_datetime('+dt_prev_month.valueOf()+')"><img src="'+STR_ICONPATH+'prev.gif" width="16" height="16" border="0" alt="previous month"></a></td>'+
'<td align="center" width="100%"><font color="#ffffff">'+ARR_MONTHS[dt_current.getMonth()]+' '+dt_current.getFullYear() + '</font></td>'+
'<td><a href="javascript:set_datetime('+dt_next_month.valueOf()+')"><img src="'+STR_ICONPATH+'next.gif" width="16" height="16" border="0" alt="next month"></a>'+(obj_caller && obj_caller.year_scroll?'&nbsp;<a href="javascript:set_datetime('+dt_next_year.valueOf()+')"><img src="'+STR_ICONPATH+'next_year.gif" width="16" height="16" border="0" alt="next year"></a>':'')+'</td>'
);
</script>
</tr>
</table></td></tr>
<tr>
<script language="JavaScript">

// print weekdays titles
for (var n=0; n<7; n++)
	document.write('<td bgcolor="#87cefa" align="center"><font color="#ffffff">'+ARR_WEEKDAYS[(NUM_WEEKSTART+n)%7]+'</font></td>');
document.write('</tr>');

// print calendar table
var dt_current_day = new Date(dt_firstday);
while (dt_current_day.getMonth() == dt_current.getMonth() ||
	dt_current_day.getMonth() == dt_firstday.getMonth()) {
	// print row heder
	document.write('<tr>');
	for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
		if (dt_current_day.getDate() == dt_current.getDate() &&
			dt_current_day.getMonth() == dt_current.getMonth())
			// print current date
			document.write('<td bgcolor="#ffb6c1" align="center" width="14%">');
		else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)
			// weekend days
			document.write('<td bgcolor="#dbeaf5" align="center" width="14%">');
		else
			// print working days of current month
			document.write('<td bgcolor="#ffffff" align="center" width="14%">');

		document.write('<a href="javascript:set_datetime('+dt_current_day.valueOf() +', true);">');

		if (dt_current_day.getMonth() == this.dt_current.getMonth())
			// print days of current month
			document.write('<font color="#000000">');
		else 
			// print days of other months
			document.write('<font color="#606060">');
			
		document.write(dt_current_day.getDate()+'</font></a></td>');
		dt_current_day.setDate(dt_current_day.getDate()+1);
	}
	// print row footer
	document.write('</tr>');
}
if (obj_caller && obj_caller.time_comp)
	document.write('<form onsubmit="javascript:set_datetime('+dt_current.valueOf()+', true)" name="cal"><tr><td colspan="7" bgcolor="#87CEFA"><font color="White" face="tahoma, verdana" size="2"><%=bundle.getString("calendar_time")%>: <input type="text" name="time" value="'+obj_caller.gen_time(this.dt_current)+'" size="8" maxlength="8"></font></td></tr></form>');
</script>
</table></tr></td>
</table>
</body>
</html>

