<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.terminology.scheduler.CronExpression,
        com.globalsight.util.GeneralException,
        java.util.*"
    session="true"
%>
<jsp:useBean id="schedule" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="unschedule" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
    WebAppConstants.SESSION_MANAGER);

String urlSchedule = schedule.getPageURL();
String urlUnschedule = unschedule.getPageURL();

String str_event = (String)sessionMgr.getAttribute(WebAppConstants.CRON_EVENT);
String str_objectname = (String)sessionMgr.getAttribute(WebAppConstants.CRON_OBJECT_NAME);
Long l_objectid = (Long)sessionMgr.getAttribute(WebAppConstants.CRON_OBJECT_ID);
String str_backptr = (String)sessionMgr.getAttribute(WebAppConstants.CRON_BACKPOINTER);
CronExpression expr = (CronExpression)sessionMgr.getAttribute(WebAppConstants.CRON_SCHEDULE);

Throwable exception =
  (Throwable)sessionMgr.getAttribute(WebAppConstants.CRON_ERROR);
sessionMgr.removeElement(WebAppConstants.CRON_ERROR);

String title = bundle.getString("lb_schedule_job_title");
String lb_calendar_title = bundle.getString("lb_calendar_title");
%>

<%@page import="java.text.MessageFormat"%>
<HTML>
<!-- This is envoy\scheduler\ScheduleMain.jsp -->
<HEAD>
<TITLE><%=title %></TITLE>
<link type="text/css" rel="stylesheet" href="/globalsight/includes/tabpane.css">
<STYLE type="text/css">
.dynamic-tab-pane-control .tab-page {
    height:     180px;
    width:      400px;
}

.dynamic-tab-pane-control .tab-page .dynamic-tab-pane-control .tab-page {
    height:     100px;
}

.dynamic-tab-pane-control h2 {
    text-align: center;
    width:      auto;
}

.dynamic-tab-pane-control h2 a {
    display:    inline;
    width:      auto;
}

.dynamic-tab-pane-control a:hover {
    background: transparent;
}

.item        { width: 60px; }

FORM         { display: inline; }
.clickable   { color: blue; cursor: pointer; }
.calendar    { width:16px; height:15px; cursor:pointer; vertical-align:middle; }

@import "/globalsight/dojo/resources/dojo.css";
</STYLE>
<link id="themeStyles" rel="stylesheet" href="/globalsight/dijit/themes/tundra/tundra.css">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<script src="/globalsight/includes/tabpane.js"></script>
<SCRIPT type="text/javascript" src="/globalsight/includes/report/calendar2.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/dojo/dojo.js"></script>
<SCRIPT>

//dojo.require("dijit.layout.TabContainer");
//dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.form.NumberSpinner");
dojo.require("dojo.date.locale");
dojo.require("dojox.validate._base");
dojo.require("dojo.number");

var needWarning = true;
var objectName = "Schedule";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_scheduler")%>";

var objectid = "<%=l_objectid%>";
var objectname = "<%=str_objectname%>";

var g_tabpane = null;

var g_debug = false;
var g_debug1 = false;
var g_edit        = eval('<%= (expr != null)%>');
var g_minutes     = '<%= (expr != null ? expr.getMinutes() : "*")%>';
var g_hours       = '<%= (expr != null ? expr.getHours()   : "*")%>';
var g_daysOfMonth = '<%= (expr != null ? expr.getDaysOfMonth() : "*")%>';
var g_months      = '<%= (expr != null ? expr.getMonths() : "*")%>';
var g_daysOfWeek  = '<%= (expr != null ? expr.getDaysOfWeek() : "*")%>';
var g_dayOfYear   = '<%= (expr != null ? expr.getDayOfYear() : "*")%>';
var g_weekOfMonth = '<%= (expr != null ? expr.getWeekOfMonth() : "*")%>';
var g_weekOfYear  = '<%= (expr != null ? expr.getWeekOfYear() : "*")%>';
var g_year        = '<%= (expr != null ? expr.getYear() : "*")%>';
var min0 = 0;
var min1 = 1;
var maxDayInYear = 366;
var maxDayInMon = 31;
var maxWeekInYear = 53;
var maxMin = 59;
var maxHour = 23;
var dateErrorMsg = "<%=bundle.getString("jsmsg_schedule_dateError")%>";//"Specified position is outside the current limits";

function debug()
{
  alert(
  "minutes     \t " + g_minutes + "\n" +
  "hours       \t " + g_hours + "\n" +
  "daysOfMonth \t " + g_daysOfMonth + "\n" +
  "months      \t " + g_months + "\n" +
  "daysOfWeek  \t " + g_daysOfWeek + "\n" +
  "dayOfYear   \t " + g_dayOfYear + "\n" +
  "weekOfMonth \t " + g_weekOfMonth + "\n" +
  "weekOfYear  \t " + g_weekOfYear + "\n" +
  "year        \t\t " + g_year);
}

// Expects a Javascript Error object.
function showError(error)
{
    window.showModalDialog("/globalsight/envoy/scheduler/error.jsp",
        error,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}

function doBack()
{
  window.location.href = "<%=str_backptr%>";
}

function doUnschedule()
{
  frmUnschedule.submit();
}

function getTabIndex()
{
  var styleNone = "none";
  if(document.getElementById("idTabDaily").style.display != styleNone)
  {
	return 0;
  }
  else if(document.getElementById("idTabWeekly").style.display != styleNone)
  {
	return 1;
  }
  else if(document.getElementById("idTabMonthly").style.display != styleNone)
  {
	return 2;
  }
  else if(document.getElementById("idTabOnce").style.display != styleNone)
  {
	return 3;
  }
}
function doSchedule()
{
  var tab = getTabIndex();

  switch (tab)
  {
  case 0: if (!scheduleDaily()) return; break;
  case 1: if (!scheduleWeekly()) return; break;
  case 2: if (!scheduleMonthly()) return; break;
  case 3: if (!scheduleOnce()) return; break;
  }

  fillForm();

  if (g_debug)
  {
    document.getElementById("idRunsAt").innerHTML = getCronDisplayString();
    debug();
  }

  frmSchedule.submit();
}

function fillForm()
{
  frmSchedule.<%=WebAppConstants.CRON_MINUTES%>.value = g_minutes;
  frmSchedule.<%=WebAppConstants.CRON_HOURS%>.value = g_hours;
  frmSchedule.<%=WebAppConstants.CRON_DAYSOFMONTH%>.value = g_daysOfMonth;
  frmSchedule.<%=WebAppConstants.CRON_MONTHS%>.value = g_months;
  frmSchedule.<%=WebAppConstants.CRON_DAYSOFWEEK%>.value = g_daysOfWeek;
  frmSchedule.<%=WebAppConstants.CRON_DAYOFYEAR%>.value = g_dayOfYear;
  frmSchedule.<%=WebAppConstants.CRON_WEEKOFMONTH%>.value = g_weekOfMonth;
  frmSchedule.<%=WebAppConstants.CRON_WEEKOFYEAR%>.value = g_weekOfYear;
  frmSchedule.<%=WebAppConstants.CRON_YEAR%>.value = g_year;
}

function scheduleDaily()
{
/*
  var error = validateDate(idStartDate1);
  if (error)
  {
    alert(error);
    return false;
  }
*/

  var schMin = dojo.byId("idTime1.minutes").value;
  var schHour = dojo.byId("idTime1.hours").value;
  if((!fnCheckDate(schMin,"minute")) 
		  || (!fnCheckDate(schHour,"hour"))
		  || (!fnCheckDate(idEveryXDay1.value,"day")))
  {
	return false;
  }

  g_minutes = "" + schMin;
  g_hours = "" + schHour;
  g_daysOfMonth = "*";
  if (idEveryDay1.checked)
  {
    g_daysOfWeek = "*";
    g_dayOfYear = "*";
  }
  else if (idWeekDays1.checked)
  {
    g_daysOfWeek = "mon,tue,wed,thu,fri";
    g_dayOfYear = "*";
  }
  else
  {
    g_daysOfWeek = "*";
	g_dayOfYear = "+" + idEveryXDay1.value;    
  }
  g_months = "*";
  g_weekOfMonth = "*";
  g_weekOfYear = "*";
  g_year = "*";

  return true;
}

function scheduleWeekly()
{
  var error = validateWeekdays();
  if (error)
  {
    alert(error);
    return false;
  }

  var schMin = dojo.byId("idTime2.minutes").value;
  var schHour = dojo.byId("idTime2.hours").value;
  if((!fnCheckDate(schMin,"minute")) 
		  || (!fnCheckDate(schHour,"hour"))
		  || (!fnCheckDate(idEveryXWeek2.value,"week")))
  {
	return false;
  }
  
  g_minutes = "" + schMin;
  g_hours = "" + schHour;
  g_daysOfMonth = "*";
  g_months = "*";
  g_daysOfWeek = getDaysOfWeek();
  g_dayOfYear = "*";
  g_weekOfMonth = "*";
  g_weekOfYear = "+" + idEveryXWeek2.value;
  g_year = "*";

  return true;
}

function scheduleMonthly()
{
  var error = validateMonths();
  if (error)
  {
    alert(error);
    return false;
  }

  var schMin = dojo.byId("idTime3.minutes").value;
  var schHour = dojo.byId("idTime3.hours").value;
  if((!fnCheckDate(schMin,"minute")) 
		  || (!fnCheckDate(schHour,"hour"))
		  || (idDayOfMonth3.checked && !fnCheckDate(idMonthlyDay3.value,"month")))
  {
	return false;
  }

  g_minutes = "" + schMin;
  g_hours = "" + schHour;

  if (idDayOfMonth3.checked)
  {
    g_daysOfMonth = "" + idMonthlyDay3.value;
  }
  else
  {
    g_daysOfMonth = getDayOfMonth();
  }
  
  g_months = getMonths();
  g_daysOfWeek = "*";
  g_dayOfYear = "*";
  g_weekOfMonth = "*";
  g_weekOfYear = "*";
  g_year = "*";

  return true;
}

function scheduleOnce()
{
  var error = validateDate(idStartDate4);
  if (!error)
  {
    error = validateDateTime(idStartDate4, idTime4);
  }
  if (error)
  {
    alert(error);
    return false;
  }

  var schMin = dojo.byId("idTime4.minutes").value;
  var schHour = dojo.byId("idTime4.hours").value;
  if((!fnCheckDate(schMin,"minute")) 
		  || (!fnCheckDate(schHour,"hour")))
  {
	return false;
  }
  
  g_minutes = "" + schMin;
  g_hours = "" + schHour;
  g_daysOfMonth = getDay(idStartDate4);
  g_months = getMonth(idStartDate4);
  g_daysOfWeek = "*";
  g_dayOfYear = "*";
  g_weekOfMonth = "*";
  g_weekOfYear = "*";
  g_year = getYear(idStartDate4);

  return true;
}

function changeTab(_old, _new)
{
  var oldTime, newTime;

  switch (_old)
  {
  case 0: oldTime = idTime1; break;
  case 1: oldTime = idTime2; break;
  case 2: oldTime = idTime3; break;
  case 3: oldTime = idTime4; break;
  }

  switch (_new)
  {
  case 0: newTime = idTime1; break;
  case 1: newTime = idTime2; break;
  case 2: newTime = idTime3; break;
  case 3: newTime = idTime4; break;
  }

  newTime.hours = oldTime.hours;
  newTime.minutes = oldTime.minutes;
}

function initFromNow()
{
  var time;

  switch (g_tabpane.getSelectedIndex())
  {
  case 0: time = idTime1; break;
  case 1: time = idTime2; break;
  case 2: time = idTime3; break;
  case 3: time = idTime4; break;
  }

  var now = new Date();
  time.hours = now.getHours();
  time.minutes = now.getMinutes();

  var val = now.getDate() + "/" + (now.getMonth() + 1) + "/" + now.getYear();
  /*idStartDate1.value =*/ idStartDate4.value = val;
}

function initFromSchedule()
{
  document.getElementById("idRunsAt").innerHTML = "<%=bundle.getString("lb_never")%>";

  if (!g_edit) return;

  idTime1.minutes = idTime2.minutes = idTime3.minutes =
    idTime4.minutes = parseInt(g_minutes, 10);
  idTime1.hours = idTime2.hours = idTime3.hours =
    idTime4.hours = parseInt(g_hours, 10);

  if (g_daysOfWeek != "*")
  {
    idMon.checked = idTue.checked = idWed.checked = idThu.checked =
      idFri.checked = idSat.checked = idSun.checked = false;

    var arr = g_daysOfWeek.split(",");
    for (var i = 0; i < arr.length; i++)
    {
      switch (arr[i])
      {
      case "mon": idMon.checked = true; break;
      case "tue": idTue.checked = true; break;
      case "wed": idWed.checked = true; break;
      case "thu": idThu.checked = true; break;
      case "fri": idFri.checked = true; break;
      case "sat": idSat.checked = true; break;
      case "sun": idSun.checked = true; break;
      }
    }
  }

  if (g_months.match(/jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec/))
  {
    idJan.checked = idFeb.checked = idMar.checked = idApr.checked =
      idMay.checked = idJun.checked = idJul.checked = idAug.checked =
      idSep.checked = idOct.checked = idNov.checked = idDec.checked = false;

    var arr = g_months.split(",");
    for (var i = 0; i < arr.length; i++)
    {
      switch (arr[i])
      {
      case "jan": idJan.checked = true; break;
      case "feb": idFeb.checked = true; break;
      case "mar": idMar.checked = true; break;
      case "apr": idApr.checked = true; break;
      case "may": idMay.checked = true; break;
      case "jun": idJun.checked = true; break;
      case "jul": idJul.checked = true; break;
      case "aug": idAug.checked = true; break;
      case "sep": idSep.checked = true; break;
      case "oct": idOct.checked = true; break;
      case "nov": idNov.checked = true; break;
      case "dec": idDec.checked = true; break;
      }
    }
  }

  if (g_daysOfMonth != '*')
  {
    if (g_daysOfMonth.match(/^[0-9]+$/))
    {
      idDayOfMonth3.checked = true;
	  idMonthlyDay3.value = parseInt(g_daysOfMonth, 10);
    }
    else
    {
      idXDayOfMonth3.checked = true;

      var when = g_daysOfMonth.charAt(0);
      switch (when)
      {
      case '1': idWhichWeek3.selectedIndex = 0; break;
      case '2': idWhichWeek3.selectedIndex = 1; break;
      case '3': idWhichWeek3.selectedIndex = 2; break;
      case '4': idWhichWeek3.selectedIndex = 3; break;
      case '$': idWhichWeek3.selectedIndex = 4; break;
      }

      var day = g_daysOfMonth.substring(1);
      switch(day)
      {
      case 'MO': idWhichDayOfWeek3.selectedIndex = 0; break;
      case 'TU': idWhichDayOfWeek3.selectedIndex = 1; break;
      case 'WE': idWhichDayOfWeek3.selectedIndex = 2; break;
      case 'TH': idWhichDayOfWeek3.selectedIndex = 3; break;
      case 'FR': idWhichDayOfWeek3.selectedIndex = 4; break;
      case 'SA': idWhichDayOfWeek3.selectedIndex = 5; break;
      case 'SU': idWhichDayOfWeek3.selectedIndex = 6; break;
      }
    }
  }

  if (g_weekOfYear.match(/^\+[0-9]+$/))
  {
    
    idEveryXWeek2.value = parseInt(g_weekOfYear.substring(1), 10);
  }

  if (g_daysOfWeek == 'mon,tue,wed,thu,fri')
  {
    idWeekDays1.checked = true;
  }
  else
  {
    idEveryDay1.checked = true;
  }

  if (g_dayOfYear.match(/^\+[0-9]+$/))
  {
    var tmp = parseInt(g_dayOfYear.substring(1), 10);

    if (tmp > 1)
    {
      idEvery1.checked = true;
      //idEveryXDay1.setCurrentPos(tmp);
    }
  }

  if (g_debug1)
  {
    document.getElementById("idRunsAt").innerHTML =
      '<%=expr != null ? expr.getCronExpression().substring(4) : bundle.getString("lb_never")%> %> | ' +
      getCronDisplayString();
  }
  else
  {
    document.getElementById("idRunsAt").innerHTML = getCronDisplayString();
  }
}

function getCronDisplayString()
{
  // TODO: compute strings like "At 02:17 every 2 days, starting 3/5/2005"
  // TODO: initialize the UI from the values. Tough!
  var res = "";

  res += (g_hours.length < 2 ? "0" : "") + g_hours + ":" +
         (g_minutes.length < 2 ? "0" : "") + g_minutes;

  if (g_year != '*')
  {
    // ONCE

    var months = parseInt(g_months, 10) + 1;

    res += " <%=bundle.getString("lb_on")%> " + (g_daysOfMonth.length < 2 ? "0" : "") + g_daysOfMonth + "/" +
           (months < 10 ? "0" : "") + months + "/" + g_year;
  }
  else if (g_daysOfMonth != '*')
  {
    // MONTHLY

    if (g_daysOfMonth.match(/^[0-9]+$/))
    {
      res += " <%=bundle.getString("lb_on_day")%> " + g_daysOfMonth;
    }
    else
    {
      var when = g_daysOfMonth.charAt(0);
      switch (when)
      {
      case '1': res += " <%=bundle.getString("lb_every_first")%>"; break;
      case '2': res += " <%=bundle.getString("lb_every_second")%>"; break;
      case '3': res += " <%=bundle.getString("lb_every_third")%>"; break;
      case '4': res += " <%=bundle.getString("lb_every_fourth")%>"; break;
      case '$': res += " <%=bundle.getString("lb_every_last")%>"; break;
      }

      var day = g_daysOfMonth.substring(1);
      switch(day)
      {
      case 'MO': res += " <%=bundle.getString("lb_monday")%>"; break;
      case 'TU': res += " <%=bundle.getString("lb_tuesday")%>"; break;
      case 'WE': res += " <%=bundle.getString("lb_wednesday")%>"; break;
      case 'TH': res += " <%=bundle.getString("lb_thursday")%>"; break;
      case 'FR': res += " <%=bundle.getString("lb_friday")%>"; break;
      case 'SA': res += " <%=bundle.getString("lb_saturday")%>"; break;
      case 'SU': res += " <%=bundle.getString("lb_sunday")%>"; break;
      }
    }

    if (g_months.match(/^jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec$/))
    {
      res += " <%=bundle.getString("lb_of_every_month")%>";
    }
    else
    {
      var tmp = "";
      if (g_months.match(/jan/)) tmp += ",<%=bundle.getString("lb_jan")%>";
      if (g_months.match(/feb/)) tmp += ",<%=bundle.getString("lb_feb")%>";
      if (g_months.match(/mar/)) tmp += ",<%=bundle.getString("lb_mar")%>";
      if (g_months.match(/apr/)) tmp += ",<%=bundle.getString("lb_apr")%>";
      if (g_months.match(/may/)) tmp += ",<%=bundle.getString("lb_may")%>";
      if (g_months.match(/jun/)) tmp += ",<%=bundle.getString("lb_jun")%>";
      if (g_months.match(/jul/)) tmp += ",<%=bundle.getString("lb_jul")%>";
      if (g_months.match(/aug/)) tmp += ",<%=bundle.getString("lb_aug")%>";
      if (g_months.match(/sep/)) tmp += ",<%=bundle.getString("lb_sep")%>";
      if (g_months.match(/oct/)) tmp += ",<%=bundle.getString("lb_oct")%>";
      if (g_months.match(/nov/)) tmp += ",<%=bundle.getString("lb_nov")%>";
      if (g_months.match(/dec/)) tmp += ",<%=bundle.getString("lb_dec")%>";

      res += " <%=bundle.getString("lb_of_report")%> " + tmp.substring(1);
    }
  }
  else if (g_weekOfYear.match(/^\+[0-9]+$/))
  {
    // WEEKLY

    if (g_daysOfWeek != '*')
    {
      res += " <%=bundle.getString("lb_every_report")%> ";

      var tmp = "";
      if (g_daysOfWeek.match(/mon/)) tmp += ",<%=bundle.getString("lb_mon")%>";
      if (g_daysOfWeek.match(/tue/)) tmp += ",<%=bundle.getString("lb_tue")%>";
      if (g_daysOfWeek.match(/wed/)) tmp += ",<%=bundle.getString("lb_wed")%>";
      if (g_daysOfWeek.match(/thu/)) tmp += ",<%=bundle.getString("lb_thu")%>";
      if (g_daysOfWeek.match(/fri/)) tmp += ",<%=bundle.getString("lb_fri")%>";
      if (g_daysOfWeek.match(/sat/)) tmp += ",<%=bundle.getString("lb_sat")%>";
      if (g_daysOfWeek.match(/sun/)) tmp += ",<%=bundle.getString("lb_sun")%>";

      res += tmp.substring(1);
    }

    if (g_weekOfYear != '+1')
    {
      var tmp = parseInt(g_weekOfYear.substring(1), 10);
      res += " <%=bundle.getString("lb_every_report")%> " + tmp + " <%=bundle.getString("lb_weeks")%>";
    }
    else
    {
      res += " <%=bundle.getString("lb_every_week")%>";
    }
  }
  else
  {
    // DAILY
    if (g_dayOfYear.match(/^\+[0-9]+$/))
    {
      var tmp = parseInt(g_dayOfYear.substring(1), 10);
      res += " <%=bundle.getString("lb_every_report")%> " + tmp + " <%=bundle.getString("lb_days")%>";
    }
    else if (g_daysOfWeek != '*')
    {
      res += " <%=bundle.getString("lb_every_report")%> ";

      var tmp = "";
      if (g_daysOfWeek.match(/mon/)) tmp += ",<%=bundle.getString("lb_mon")%>";
      if (g_daysOfWeek.match(/tue/)) tmp += ",<%=bundle.getString("lb_tue")%>";
      if (g_daysOfWeek.match(/wed/)) tmp += ",<%=bundle.getString("lb_wed")%>";
      if (g_daysOfWeek.match(/thu/)) tmp += ",<%=bundle.getString("lb_thu")%>";
      if (g_daysOfWeek.match(/fri/)) tmp += ",<%=bundle.getString("lb_fri")%>";
      if (g_daysOfWeek.match(/sat/)) tmp += ",<%=bundle.getString("lb_sat")%>";
      if (g_daysOfWeek.match(/sun/)) tmp += ",<%=bundle.getString("lb_sun")%>";

      res += tmp.substring(1);
    }
    else
    {
      res += " <%=bundle.getString("lb_every_day_report")%>";
    }
  }

  return res;
}

function getDay(p_cal)
{
  return "" + parseInt(p_cal.value.split('/')[0], 10);
}

function getMonth(p_cal)
{
  return "" + (parseInt(p_cal.value.split('/')[1], 10) - 1);
}

function getYear(p_cal)
{
  return "" + parseInt(p_cal.value.split('/')[2], 10);
}

function getDaysOfWeek()
{
  var res = "";

  if (idMon.checked) { res += ",mon"; }
  if (idTue.checked) { res += ",tue"; }
  if (idWed.checked) { res += ",wed"; }
  if (idThu.checked) { res += ",thu"; }
  if (idFri.checked) { res += ",fri"; }
  if (idSat.checked) { res += ",sat"; }
  if (idSun.checked) { res += ",sun"; }

  if (!res)
  {
    res = "*";
  }
  else
  {
    // remove initial comma
    res = res.substring(1);
  }

  return res;
}

function getDayOfMonth()
{
  return "" + idWhichWeek3.options[idWhichWeek3.selectedIndex].value +
         idWhichDayOfWeek3.options[idWhichDayOfWeek3.selectedIndex].value;
}

function getMonths()
{
  var res = "";

  if (idJan.checked) { res += ",jan"; }
  if (idFeb.checked) { res += ",feb"; }
  if (idMar.checked) { res += ",mar"; }
  if (idApr.checked) { res += ",apr"; }
  if (idMay.checked) { res += ",may"; }
  if (idJun.checked) { res += ",jun"; }
  if (idJul.checked) { res += ",jul"; }
  if (idAug.checked) { res += ",aug"; }
  if (idSep.checked) { res += ",sep"; }
  if (idOct.checked) { res += ",oct"; }
  if (idNov.checked) { res += ",nov"; }
  if (idDec.checked) { res += ",dec"; }

  if (!res)
  {
    res = "*";
  }
  else
  {
    // remove initial comma
    res = res.substring(1);
  }

  return res;
}

function validateWeekdays()
{
  var res = (idMon.checked || idTue.checked || idWed.checked ||
    idThu.checked || idFri.checked || idSat.checked || idSun.checked);

  if (!res)
  {
    return "<%=bundle.getString("lb_select_at_least_one_weekday")%>";
  }
}

function validateMonths()
{
  var res =
    (idJan.checked || idFeb.checked || idMar.checked || idApr.checked ||
     idMay.checked || idJun.checked || idJul.checked || idAug.checked ||
     idSep.checked || idOct.checked || idNov.checked || idDec.checked);

  if (!res)
  {
    return "<%=bundle.getString("lb_select_at_least_one_month")%>";
  }
}

function l18nFormat(string, arg0)
{
	return string.replace(/\{.*?\}/g, arg0);
}

// Validates a date to be DD/MM/YYYY and nothing else.
function validateDate(control)
{
  var date = control.value;
  if (!date) return;

  var arr = date.split('/');
  if (arr.length != 3)
  {
    return dateError("");
  }

  var val;

  val = parseInt(arr[0], 10);
  if (!(val >= 1 && val <= 31))
  {
	 var msg = "<%=bundle.getString("msg_invalid_day")%>";
     return dateError(l18nFormat(msg, arr[0]));
  }

  var val = parseInt(arr[1], 10);
  if (!(val >= 1 && val <= 12))
  {
	 var msg = "<%=bundle.getString("msg_invalid_month")%>";
     return dateError(l18nFormat(msg, arr[1]));
  }

  var val = parseInt(arr[2], 10);
  if (!(val >= 1900 && val <= 2099))
  {
	 var msg = "<%=bundle.getString("msg_invalid_year")%>";
     return dateError(l18nFormat(msg, arr[2]));
  }

  return null;
}

// Validates the date and time are later than NOW.
// (Server may have a different time.)
function validateDateTime(p_date, p_time)
{
  var min = p_time.minutes;
  var hrs = p_time.hours;
  var day = getDay(p_date);
  var mon = getMonth(p_date);
  var yrs = getYear(p_date);

  var date = new Date(yrs, mon, day, hrs, min);
  var now = new Date();

  if (date.valueOf() < now.valueOf())
  {
    return "<%=bundle.getString("lb_date_is_past")%>";
  }

  return null;
}

function dateError(desc)
{
  return "<%=bundle.getString("msg_invalid_date_format")%>" +
    (desc ? "\n" + desc : "");
}

function setConstraints()
{
  idEveryXDay1.case_sensitive = false;
  idEveryXDay1.integer = true;
  idEveryXDay1.circular = true;
  idEveryXDay1.setLimits(1, 366);
  idEveryXDay1.setStep(1);

  idEveryXWeek2.case_sensitive = false;
  idEveryXWeek2.integer = true;
  idEveryXWeek2.circular = true;
  idEveryXWeek2.setLimits(1, 53);
  idEveryXWeek2.setStep(1);

  idMonthlyDay3.case_sensitive = false;
  idMonthlyDay3.integer = true;
  idMonthlyDay3.circular = true;
  idMonthlyDay3.setLimits(1, 31);
  idMonthlyDay3.setStep(1);
}

function showCalendar(id) {
	var cal1 = new calendar2(document.getElementById(id));
    cal1.year_scroll = true;
    cal1.time_comp = false;
    cal1.popup();
}

// Check the value of Minute/Hour/Week/Month/Day
function fnCheckDate(value,type){
	var flags,result,msg,maxValue,minValue;
	msg = dateErrorMsg;
	
	if(type == "minute")
	{
		maxValue = maxMin; 
		minValue = min0;
	}
	else if (type == "hour")
	{
		maxValue = maxHour; 
		minValue = min0;
	}
	else if (type == "week")
	{
		maxValue = maxWeekInYear; 
		minValue = min1;
	}
	else if (type == "month")
	{
		maxValue = maxDayInMon; 
		minValue = min1;
	}
	else if (type == "day")
	{
		maxValue = maxDayInYear; 
		minValue = min1;
	}

	flags = {max:maxValue, min:minValue};
	result = dojox.validate.isInRange(value,flags);
	if(result)
	{
		return true;
	}
	else
	{
		msg = msg + "(" +minValue + "-" + maxValue + ")";
		alert(msg);
		return false;
	}
}

function isInt(input)
{
	var reg= /^[0-9]+$/ ;
	return reg.test(input); 
}

/**
 * Generate Minute and Hour Spinner
 *
 * id	        --Container/Spinner Id 
 * scheduleMin	--Schedule Minute Value, 
 * scheduleHour	--Schedule Hour Value
 */
function genarateDateTime(id, scheduleHour, scheduleMin)
{
	var elem,spinId,spinTimeMins=".minutes",spinTimeHours=".hours";
	var _style = "width: 54;font: 10pt Courier New; text-align: right;";
	spinId = id;
	elem = dojo.byId(spinId);
	elem.innerHTML = "<span id='"+spinId+spinTimeHours+"'></span>&nbsp;:&nbsp;"
				   + "<span id='"+spinId+spinTimeMins+"'></span>";

    elem.hours = new dijit.form.NumberSpinner({
		value: scheduleHour,
		smallDelta: 1,
		largeDelta: 10,
		constraints: {
			min: 0,
			max: 23,
			places: 0
		},
		style: _style
	},
	spinId+spinTimeHours);

	elem.minutes = new dijit.form.NumberSpinner({
		value: scheduleMin,
		smallDelta: 1,
		largeDelta: 10,
		constraints: {
			min: 0,
			max: 59,
			places: 0
		},
		style: _style
	},
	spinId+spinTimeMins);
	
}

// *******************************************************************************************************************
// *	Start Initial the Date Spinner.		
// *******************************************************************************************************************

dojo.addOnLoad(function() {

	var scheduleHour,scheduleMin,scheduleDay,scheduleWeek,scheduleMonDay; 
	var date = new Date();
	
	if(!g_edit)
	{
		scheduleHour = date.getHours();
		scheduleMin = date.getMinutes(); 
		scheduleDay = 1;
		scheduleWeek = 1;
		scheduleMonDay = 1;
		dojo.byId("idStartDate4").value = 
			dojo.date.locale.format(date, {datePattern: "dd/MM/yyyy", selector: "date"});
		dojo.byId("idEvery1").checked = true;
		dojo.byId("idDayOfMonth3").checked = true;
	}
	else
	{
		scheduleHour = parseInt(g_hours, 10);
		scheduleMin = parseInt(g_minutes, 10); 
		
		if (g_dayOfYear.match(/^\+[0-9]+$/))
		{
		    var tmp = parseInt(g_dayOfYear.substring(1), 10);
		    if (tmp > 1)
		    {
		      idEvery1.checked = true;
		      scheduleDay = tmp;
		    }
		}

		if (g_weekOfYear.match(/^\+[0-9]+$/))
		{
			scheduleWeek = parseInt(g_weekOfYear.substring(1), 10);
		}

		if (g_daysOfMonth != '*')
		{
		    if (g_daysOfMonth.match(/^[0-9]+$/))
		    {
		      idDayOfMonth3.checked = true;
		      scheduleMonDay = parseInt(g_daysOfMonth, 10);
		    }
		    else
		    {
		      idXDayOfMonth3.checked = true;

		      var when = g_daysOfMonth.charAt(0);
		      switch (when)
		      {
		      case '1': idWhichWeek3.selectedIndex = 0; break;
		      case '2': idWhichWeek3.selectedIndex = 1; break;
		      case '3': idWhichWeek3.selectedIndex = 2; break;
		      case '4': idWhichWeek3.selectedIndex = 3; break;
		      case '$': idWhichWeek3.selectedIndex = 4; break;
		      }

		      var day = g_daysOfMonth.substring(1);
		      switch(day)
		      {
		      case 'MO': idWhichDayOfWeek3.selectedIndex = 0; break;
		      case 'TU': idWhichDayOfWeek3.selectedIndex = 1; break;
		      case 'WE': idWhichDayOfWeek3.selectedIndex = 2; break;
		      case 'TH': idWhichDayOfWeek3.selectedIndex = 3; break;
		      case 'FR': idWhichDayOfWeek3.selectedIndex = 4; break;
		      case 'SA': idWhichDayOfWeek3.selectedIndex = 5; break;
		      case 'SU': idWhichDayOfWeek3.selectedIndex = 6; break;
		      }
		    }
		}

		//g_daysOfMonth,g_months,g_year
		var onceDay;
		if(isInt(g_daysOfMonth) && isInt(g_months) && isInt(g_year))
		{
			onceDay = dojo.number.format(g_daysOfMonth, {pattern:"00"}) + "/" +
					  dojo.number.format(parseInt(g_months)+1, {pattern:"00"}) + "/" +
					  dojo.number.format(g_year, {pattern:"0000"});
		}
		else
		{
			onceDay = dojo.date.locale.format(date, {datePattern: "dd/MM/yyyy", selector: "date"});
		}
		dojo.byId("idStartDate4").value = onceDay;
		

		if(scheduleHour==null) scheduleHour = date.getHours();
		if(scheduleMin==null) scheduleMin = date.getMinutes(); 
		if(scheduleDay==null) scheduleDay = 1;
		if(scheduleWeek==null) scheduleWeek = 1;
		if(scheduleMonDay==null) scheduleMonDay = 1;
	}

	genarateDateTime("idTime1", scheduleHour, scheduleMin);
	genarateDateTime("idTime2", scheduleHour, scheduleMin);
	genarateDateTime("idTime3", scheduleHour, scheduleMin);
	genarateDateTime("idTime4", scheduleHour, scheduleMin);
	
	var idEveryXDay1 = new dijit.form.NumberSpinner({
		value: scheduleDay,
        smallDelta: 1,
		largeDelta: 10,
        constraints: {
        	min: 1,
        	max: 366,
        	places: 0
        },
        //id: "idEveryXDay1"+"Input",
        style: "width: 62;font: 10pt Courier New; text-align: right;"
	},
	"idEveryXDay1");

	var idEveryXWeek2 = new dijit.form.NumberSpinner({
		value: scheduleWeek,
        smallDelta: 1,
		largeDelta: 10,
        constraints: {
        	min: 1,
        	max: 53,
        	places: 0
        },
        style: "width: 62;font: 10pt Courier New; text-align: right;"
	},
	"idEveryXWeek2");

	var idMonthlyDay3 = new dijit.form.NumberSpinner({
		value: scheduleMonDay,
        smallDelta: 1,
		largeDelta: 10,
        constraints: {
        	min: 1,
        	max: 31,
        	places: 0
        },
        style: "width: 62;font: 10pt Courier New; text-align: right;"
	},
	"idMonthlyDay3");    

	initFromSchedule();
});
// 	Finish Initial the Date Spinner.	

function doOnLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

  //g_tabpane = new WebFXTabPane(document.getElementById("tabPane"));
  //setConstraints();
  //initFromNow();
  
  //initFromSchedule();

  // <%if (exception != null) { %>
  var error = new Error;
  error.message = '<%=EditUtil.toJavascript(exception.getMessage())%>';
  error.description = '<%=EditUtil.toJavascript(GeneralException.getStackTraceString(exception))%>';
  showError(error);
  // <% } %>
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
      TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: absolute; Z-INDEX: 0; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<P CLASS="mainHeading" id="idHeading"><%=title %></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538>
      <%
      String help = bundle.getString("helper_text_schedule_main");
      String lbSchedule = bundle.getString("lb_schedule");
      String lbUnschedule = bundle.getString("lb_unschedule"); 
      %>
      <%=MessageFormat.format(help, lbSchedule, lbUnschedule)%>
    </TD>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
  <TR>
    <TD WIDTH=538>
      <table CLASS=standardText>
        <tr>
          <td><B><%=bundle.getString("lb_cron_job") %>:</B></td>
          <td>
<%
      if (str_event.equals(WebAppConstants.CRON_EVENT_REINDEX_TERMBASE))
      {
      %><%=bundle.getString("lb_reindex_termbase")%> "<%=str_objectname%>"<%
      }
      else
      {
      %><%=bundle.getString("lb_execute_task_type")%> "<%=str_event%>" <%=bundle.getString("lb_on_object")%> "<%=l_objectid%>"<%
      }
%>
          </td>
        </tr>
        <tr>
          <td><B><%=bundle.getString("lb_run_at") %>:</B></td>
          <td><span id="idRunsAt"></span></td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>
<BR>

<div class="tab-pane" id="tabPane" onchange="changeTab">
  <div class="tab-page" id="idTabDaily" style="display:none">
      <h2 class="tab"><%=bundle.getString("lb_daily") %></h2>

      <DIV><%=bundle.getString("lb_start_time") %>: &nbsp;&nbsp; <span id="idTime1"></span></DIV>
      <BR>
      <DIV>
        <input type="radio" name="radDaily" id="idEveryDay1">
        <label for="idEveryDay1"><%=bundle.getString("lb_every_day") %></label><BR>
        <input type="radio" name="radDaily" id="idWeekDays1">
        <label for="idWeekDays1"><%=bundle.getString("lb_weekdays") %></label><BR>
        <input type="radio" name="radDaily" id="idEvery1">
        <label for="idEvery1"><%=bundle.getString("lb_every") %></label>
        &nbsp;&nbsp;
        <span id="idEveryXDay1"
         style="width: 30; font: 10pt Courier New; text-align: right;"
         spin_onchange="idEvery1.click();"></span>
        &nbsp; <%=bundle.getString("lb_day_s") %>
      </DIV>
      <BR>
<%--
      <DIV>Start date: &nbsp;&nbsp;
        <input id="idStartDate1" type="text" size="10">
        <gs:calendar id="idCalendar1" target="idStartDate1" />
        &nbsp; <span class='info'>(DD/MM/YYYY)</span>
      </DIV>
--%>

  </div>
  <div class="tab-page" id="idTabWeekly" style="display:none">
      <h2 class="tab"><%=bundle.getString("lb_weekly") %></h2>

      <DIV><%=bundle.getString("lb_start_time") %>: &nbsp;&nbsp; <span id="idTime2"></span></DIV>
      <BR>
      <DIV><%=bundle.getString("lb_every") %>
        &nbsp;&nbsp;
        <span id="idEveryXWeek2"
         style="width: 20; font: 10pt Courier New; text-align: right;"></span>
         &nbsp; <%=bundle.getString("lb_week_s") %>
      </DIV>
      <BR>
      <DIV><%=bundle.getString("lb_select_day_of_week") %>:</DIV>
      <DIV> <span class="item">
        <input type="checkbox" id="idMon" checked><label for="idMon"><%=bundle.getString("lb_mon")%></label>
        </span>
        <span class="item">
        <input type="checkbox" id="idTue" checked><label for="idTue"><%=bundle.getString("lb_tue")%></label>
        </span>
        <span class="item">
        <input type="checkbox" id="idWed" checked><label for="idWed"><%=bundle.getString("lb_wed")%></label>
        </span>
        <span class="item">
        <input type="checkbox" id="idThu" checked><label for="idThu"><%=bundle.getString("lb_thu")%></label>
        </span>
        <span class="item">
        <input type="checkbox" id="idFri" checked><label for="idFri"><%=bundle.getString("lb_fri")%></label>
        </span>
        <br>
        <span class="item">
        <input type="checkbox" id="idSat" checked><label for="idSat"><%=bundle.getString("lb_sat")%></label>
        </span>
        <span class="item">
        <input type="checkbox" id="idSun" checked><label for="idSun"><%=bundle.getString("lb_sun")%></label>
        </span>
      </DIV>

  </div>
  <div class="tab-page"  id="idTabMonthly" style="display:none">
    <h2 class="tab"><%=bundle.getString("lb_monthly") %></h2>

      <DIV><%=bundle.getString("lb_start_time") %>: &nbsp;&nbsp; <span id="idTime3"></span></DIV>
      <BR>
      <DIV>
        <input type="radio" name="radMonthly" id="idDayOfMonth3">
        <label for="idDayOfMonth3"><%=bundle.getString("lb_day")%></label>
        &nbsp;&nbsp;
        <span id="idMonthlyDay3"
         style="width: 30; font: 10pt Courier New; text-align: right;"
         spin_onchange="idDayOfMonth3.click()"></span>
        <BR>
        <input type="radio" name="radMonthly" id="idXDayOfMonth3">
        <label for="idXDayOfMonth3"><%=bundle.getString("the")%></label>
        &nbsp;&nbsp;
        <select id="idWhichWeek3" onclick="idXDayOfMonth3.click()">
          <option value="1"><%=bundle.getString("lb_first_report")%>
          <option value="2"><%=bundle.getString("lb_second_report")%>
          <option value="3"><%=bundle.getString("lb_third_report")%>
          <option value="4"><%=bundle.getString("lb_fourth_report")%>
          <option value="$"><%=bundle.getString("lb_last_report")%>
        </select>
        &nbsp;&nbsp;
        <select id="idWhichDayOfWeek3" onclick="idXDayOfMonth3.click()">
          <option value="MO"><%=bundle.getString("lb_monday")%>
          <option value="TU"><%=bundle.getString("lb_tuesday")%>
          <option value="WE"><%=bundle.getString("lb_wednesday")%>
          <option value="TH"><%=bundle.getString("lb_thursday")%>
          <option value="FR"><%=bundle.getString("lb_friday")%>
          <option value="SA"><%=bundle.getString("lb_saturday")%>
          <option value="SU"><%=bundle.getString("lb_sunday")%>
        </select>
      </DIV>
      <BR>
      <DIV><%=bundle.getString("lb_of_the_month")%></DIV>
      <DIV>
      <span class="item">
      <input type="checkbox" id="idJan" checked><label for="idJan"><%=bundle.getString("lb_jan")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idFeb" checked><label for="idFeb"><%=bundle.getString("lb_feb")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idMar" checked><label for="idMar"><%=bundle.getString("lb_mar")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idApr" checked><label for="idApr"><%=bundle.getString("lb_apr")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idMay" checked><label for="idMay"><%=bundle.getString("lb_may")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idJun" checked><label for="idJun"><%=bundle.getString("lb_jun")%></label>
      </span>
      <br>
      <span class="item">
      <input type="checkbox" id="idJul" checked><label for="idJul"><%=bundle.getString("lb_jul")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idAug" checked><label for="idAug"><%=bundle.getString("lb_aug")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idSep" checked><label for="idSep"><%=bundle.getString("lb_sep")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idOct" checked><label for="idOct"><%=bundle.getString("lb_oct")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idNov" checked><label for="idNov"><%=bundle.getString("lb_nov")%></label>
      </span>
      <span class="item">
      <input type="checkbox" id="idDec" checked><label for="idDec"><%=bundle.getString("lb_dec")%></label>
      </span>
      </DIV>

  </div>
  <div class="tab-page" id="idTabOnce" style="display:none">
    <h2 class="tab"><%=bundle.getString("lb_once") %></h2>

      <DIV><%=bundle.getString("lb_start_time") %>: &nbsp;&nbsp; <span id="idTime4"></span></DIV>
      <BR>
      <DIV><%=bundle.getString("lb_start_date") %>: &nbsp;&nbsp;
    	<input id="idStartDate4" type="text" size="10">
        <img id="idCalendar4" src="/globalsight/includes/Calendar.gif" class="calendar" 
        	onclick="showCalendar('idStartDate4')" title="<%=lb_calendar_title %>">
        &nbsp; <span class='info'>(DD/MM/YYYY)</span>
      </DIV>

  </div>
</div>


<BR>
<DIV>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous") %>"
  ID="idBack" onclick="doBack()">
&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_unschedule") %>"
  ID="idUnschedule" onclick="doUnschedule()">
&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_schedule") %>"
  ID="idSchedule" onclick="doSchedule()">
&nbsp;
</DIV>
<BR>

</DIV>

<FORM name="frmSchedule" method="post" action="<%=urlSchedule%>">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_ACTION%>"
 value="<%=WebAppConstants.CRON_ACTION_SCHEDULE%>">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_MINUTES%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_HOURS%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_DAYSOFMONTH%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_MONTHS%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_DAYSOFWEEK%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_DAYOFYEAR%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_WEEKOFMONTH%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_WEEKOFYEAR%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_YEAR%>" value="">
</FORM>

<FORM name="frmUnschedule" method="post" action="<%=urlUnschedule%>">
<INPUT type="hidden" name="<%=WebAppConstants.CRON_ACTION%>"
 value="<%=WebAppConstants.CRON_ACTION_UNSCHEDULE%>">
</FORM>

</BODY>
</HTML>
