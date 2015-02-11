<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyHandlerHelper,
                 com.globalsight.everest.costing.Currency,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.request.reimport.ActivePageReimporter,
                 java.util.Collection,
                 java.util.Iterator,
                 java.util.ResourceBundle"
         session="true" %>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%!
    private static final String CHECKED  = "CHECKED";
    private static final String SELECTED = "SELECTED";
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String title = bundle.getString("lb_system_parameters");
String lbSave = bundle.getString("lb_save");
String lbCancel = bundle.getString("lb_cancel");

boolean isLocale = ((String)request.getAttribute(
    SystemConfigParamNames.EXPORT_DIR_NAME_STYLE)).
      compareToIgnoreCase("locale") == 0;
boolean isLanguage = ((String)request.getAttribute(
    SystemConfigParamNames.EXPORT_DIR_NAME_STYLE)).
      compareToIgnoreCase("language") == 0;

boolean isCorpusStoringNativeFormatDocs =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT)).booleanValue();
boolean isCorpusShowingAllTms =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS)).booleanValue();
boolean isTermAutoReplace =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.AUTO_REPLACE_TERMS)).booleanValue();
boolean isAllowAnonymousTermbases =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.ANONYMOUS_TERMBASES)).booleanValue();

boolean isAllowTranslatorMt =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.MT_SHOW_IN_EDITOR)).booleanValue();

boolean isEnableMyJobsDaysRetrieved = false;

boolean colorOverride =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.HYPERLINK_COLOR_OVERRIDE)).booleanValue();
boolean isCostingEnabled =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.COSTING_ENABLED)).booleanValue();
boolean isRevenueEnabled =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.REVENUE_ENABLED)).booleanValue();
String sortComments =
    (String)request.getAttribute(SystemConfigParamNames.COMMENTS_SORTING);
boolean isEmailAuthenticationEnabled =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.EMAIL_AUTHENTICATION_ENABLED)).booleanValue();

boolean isPmEmailNotificationed =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.PM_EMAIL_NOTIFICATION)).booleanValue();
boolean isReportsCheckAccess =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.REPORTS_CHECK_ACCESS)).booleanValue();
boolean isExportRemoveInfoEnabled =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.EXPORT_REMOVE_INFO_ENABLED)).booleanValue();
boolean isDupOjbAllowed =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED)).booleanValue();
boolean isJobSearchAllowed =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED)).booleanValue();
boolean isImportSuggestJobName =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME)).booleanValue();
boolean isCostingLockDown =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.COSTING_LOCKDOWN)).booleanValue();
boolean isDell =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.IS_DELL)).booleanValue();
// PPT master layer translate switch
boolean isPPTMasterTranslated =
	Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.PPT_MASTER_TRANSLATE)).booleanValue();

 // For "Additional functionality quotation" issue
 String perFileCharge01 = (String)request.getAttribute(
      SystemConfigParamNames.PER_FILE_CHARGE01_KEY);
 String perFileCharge02 = (String)request.getAttribute(
      SystemConfigParamNames.PER_FILE_CHARGE02_KEY);
 String perJobCharge = (String)request.getAttribute(
      SystemConfigParamNames.PER_JOB_CHARGE_KEY);
      
 boolean perFileCharge01Enable = (perFileCharge01.equals("#")) ? false:true;
 String perFileCharge01Value = perFileCharge01Enable ? perFileCharge01:"0.0";
 
 boolean perFileCharge02Enable = (perFileCharge02.equals("#")) ? false:true;
 String perFileCharge02Value = perFileCharge02Enable ? perFileCharge02:"0.0";
 
 boolean perJobChargeEnable = (perJobCharge.equals("#")) ? false:true;
 String perJobChargeValue = perJobChargeEnable ? perJobCharge:"0.0";
 
boolean enableSSO =
	Boolean.valueOf((String)request.getAttribute(
       SystemConfigParamNames.ENABLE_SSO)).booleanValue(); 

//String analyzeInterval = request.getAttribute(SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL).toString();
//int jselect2 = 12;
//available currencies
Collection currencies = CurrencyHandlerHelper.getAllCurrencies();
%>

<HTML>
<!-- This JSP is envoy/administration/config/configMain.jsp -->
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "systemParameter";
var helpFile = "<%=bundle.getString("help_system_parameters")%>";
<!--For "Additional functionality quotation" issue-->
var perSurchargeDefaultValue = "#";
function submitForm()
{
    if (document.layers)
    {
    }
    else
    {
        //if(!verifyOverdueNotificationInput())
        //{
        //        return;
        //}
        //if (confirm("<%= bundle.getString("jsmsg_system_parameters") %>"))
        //{
            profileForm.submit();
        //}
    }
}

function doCancel()
{
	window.location.href="/globalsight/ControlServlet";
}

<!--For "Additional functionality quotation" issue-->
function setElementVisible(id,visible)
{
        var obj=document.getElementById(id);
        if(obj != null && obj != "undefine")
        {
          if(visible == true)
          {
               obj.style.visibility= "";
          }
          else
          {
               obj.style.visibility = "hidden";
          }
        }
}
  
function confirmPositiveNum(obj,initValue)
{
		var reg = /^(([\d]+\.?[\d]+)|([\d]*))$/;
        if(obj != null && obj != "undefine" && obj != "")
        {
             var value = obj.value;
             if(!reg.test(value))
             {                
                 alert("<%= bundle.getString("jsmsg_system_parameters_wrong_number") %>");
                 obj.value=initValue;
                 obj.select();
                 obj.focus();
				 return false;
             }

        }
  }

  // For verify the overdue notification days, insure the "overdue notification to PM" > "overdue notification to user" 
  function verifyOverdueNotificationInput()
  {
      var overdueToPmDays = document.getElementById("<%=SystemConfigParamNames.OVERDUE_PM_DAY%>").value;
      var overdueToUserDays = document.getElementById("<%=SystemConfigParamNames.OVERDUE_USER_DAY%>").value;
	  if(overdueToUserDays - overdueToPmDays > 0 )
      {
          alert("<%= bundle.getString("jsmsg_system_parameters_notification_days") %>");
          return false;
      }
      return true;
  }
<!--For "Additional functionality quotation" issue-->  
function setPerSurchargeTextInputValue(id,value)
{
	
	var obj = document.getElementById(id);
	if(obj != null && obj != "undefine" && obj != "")
	{	
		obj.value = value;	
	}
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_system_paramters")%>
</TD>
</TR>
</TABLE>
<P>

<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0">
<COL WIDTH=250>
<COL>
  <FORM name="profileForm" action="<%=save.getPageURL()%>" method="post">
  <INPUT TYPE="hidden" NAME="<%=SystemConfigParamNames.ADD_LANG_META_TAG%>" VALUE="true">
  <TR VALIGN="TOP">
    <TD ><SPAN CLASS="standardText"><%=bundle.getString("lb_export_directory_name_style")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <SELECT NAME="<%=SystemConfigParamNames.EXPORT_DIR_NAME_STYLE%>" CLASS="standardText">
        <OPTION <%if (isLocale) out.print(SELECTED);%>><%=bundle.getString("lb_locale")%>
        <OPTION <%if (isLanguage) out.print(SELECTED);%>><%=bundle.getString("lb_language")%>
        <OPTION <%if ((!isLanguage)&&(!isLocale)) out.print(SELECTED);%>><%=bundle.getString("lb_export")%>
      </SELECT></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_web_server_admin_email_address")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.WEB_SERVER_ADMIN_EMAIL_ADDRESS%>"
      SIZE="40" MAXLENGTH="1000" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.WEB_SERVER_ADMIN_EMAIL_ADDRESS)%>"></SPAN>
    </TD>
  </TR>  
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_auto_replace_terms")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isTermAutoReplace) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.AUTO_REPLACE_TERMS%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isTermAutoReplace) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.AUTO_REPLACE_TERMS%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_allow_anonymous_termbases")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isAllowAnonymousTermbases) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.ANONYMOUS_TERMBASES%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isAllowAnonymousTermbases) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.ANONYMOUS_TERMBASES%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>

<% if (b_corpus) { %>  
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_corpus_cfg_nf")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isCorpusStoringNativeFormatDocs) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isCorpusStoringNativeFormatDocs) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_corpus_cfg_showTms")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isCorpusShowingAllTms) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isCorpusShowingAllTms) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.CORPUS_SHOW_ALL_TMS_TO_LPS%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  
<% } %>  

    <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText">Allow Localization Participants to use MT in Editor:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isAllowTranslatorMt) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.MT_SHOW_IN_EDITOR%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isAllowTranslatorMt) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.MT_SHOW_IN_EDITOR%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>

  <%
  if (isCostingEnabled)
  {
  %>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_pivot_currency")%>:</SPAN></TD>
    <TD>
      <SELECT NAME="<%=SystemConfigParamNames.PIVOT_CURRENCY%>" CLASS="standardText">
        <%
        if (currencies != null)
        {
          String defaultCode =
          (String)request.getAttribute(SystemConfigParamNames.PIVOT_CURRENCY);
          Map<String, String> currencyMap = new HashMap<String, String>();

          for (Iterator it = currencies.iterator(); it.hasNext(); )
          {
            Currency c = (Currency)it.next();
            currencyMap.put(c.getIsoCode(), c.getDisplayName());
          }
          
          for (Iterator it = currencyMap.keySet().iterator(); it.hasNext();) {
        	  String isoCode = (String)it.next();
        	  String currencyName = currencyMap.get(isoCode);
              if (isoCode.equals(defaultCode))
              {
                out.println("<option value=\"" + isoCode + "\" SELECTED>" +
                		currencyName + "</option>");
              }
              else
              {
                out.println("<option value=\"" + isoCode + "\">" +
                		currencyName + "</option>");
              }
          }
        }
        %>
      </SELECT>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_costing_options")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isRevenueEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.REVENUE_ENABLED%>" VALUE="true">
      <%=bundle.getString("lb_yes_revenue_and_expenses") %>
      <INPUT TYPE="radio" <%if (!isRevenueEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.REVENUE_ENABLED%>" VALUE="false">
      <%=bundle.getString("lb_yes_expenses_only") %>
      <!--
      <INPUT TYPE="radio" <%if (!isCostingEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COSTING_ENABLED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
      -->
    </TD>
  </TR>
  <%
  }
  %>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_comment_sort_order")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if ("desc".equals(sortComments)) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COMMENTS_SORTING%>" VALUE="desc">
      <%=bundle.getString("lb_comments_descending") %>
      <INPUT TYPE="radio" <%if ("asc".equals(sortComments)) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COMMENTS_SORTING%>" VALUE="asc">
      <%=bundle.getString("lb_comments_ascending") %>
      <INPUT TYPE="radio" <%if ("default".equals(sortComments)) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COMMENTS_SORTING%>" VALUE="default">
      <%=bundle.getString("lb_comments_default") %>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_email_authentication_enabled")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isEmailAuthenticationEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.EMAIL_AUTHENTICATION_ENABLED%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isEmailAuthenticationEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.EMAIL_AUTHENTICATION_ENABLED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_account_username")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.ACCOUNT_USERNAME%>"
      SIZE="10" MAXLENGTH="1000" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.ACCOUNT_USERNAME)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_account_password")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.ACCOUNT_PASSWORD%>"
      SIZE="10" MAXLENGTH="1000" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.ACCOUNT_PASSWORD)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_records_per_page_jobs")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.RECORDS_PER_PAGE_JOBS%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.RECORDS_PER_PAGE_JOBS)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_records_per_page_tasks")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.RECORDS_PER_PAGE_TASKS%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.RECORDS_PER_PAGE_TASKS)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_server_instance_id")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.SERVER_INSTANCE_ID%>"
      SIZE="50" MAXLENGTH="50" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.SERVER_INSTANCE_ID)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD COLSPAN="2"></TD>
  </TR>
  <TR VALIGN="TOP">
    <TD COLSPAN="2"><B>new parameters</B><hr></TD>
  </TR>

  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_formatType_handleImportFailure")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.HANDLE_IMPORT_FAILURE%>"
      SIZE="10" MAXLENGTH="10" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.HANDLE_IMPORT_FAILURE)%>"></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_reimport_option")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <SELECT NAME="<%=SystemConfigParamNames.REIMPORT_OPTION%>" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.REIMPORT_OPTION)%>">
        <OPTION value="0" <%if (request.getAttribute(SystemConfigParamNames.REIMPORT_OPTION).equals("0")) 
            out.print(SELECTED);%>><%=bundle.getString("lb_reimport_option_0")%>
            </OPTION>
        <OPTION value="1" <%if (request.getAttribute(SystemConfigParamNames.REIMPORT_OPTION).equals("1")) 
            out.print(SELECTED);%>><%=bundle.getString("lb_reimport_option_1")%>
            </OPTION>
        <OPTION value="2" <%if (request.getAttribute(SystemConfigParamNames.REIMPORT_OPTION).equals("2")) 
            out.print(SELECTED);%>><%=bundle.getString("lb_reimport_option_2")%>
            </OPTION>
      </SELECT></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
  <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_reports_activity")%>:</SPAN></TD>
  <TD><SPAN CLASS="standardText">
    <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.REPORTS_ACTIVITY%>"
    SIZE="20" MAXLENGTH="64" CLASS="standardText"
    VALUE="<%=request.getAttribute(SystemConfigParamNames.REPORTS_ACTIVITY)%>"></SPAN>
  </TD>
</TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_reimport_delay_milliseconds")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.REIMPORT_DELAY_MILLISECONDS%>"
      SIZE="8" MAXLENGTH="8" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.REIMPORT_DELAY_MILLISECONDS)%>"></SPAN>
      <INPUT TYPE="hidden" NAME="<%=SystemConfigParamNames.DEFAULT_PM_EMAIL%>" VALUE="<%=request.getAttribute(SystemConfigParamNames.DEFAULT_PM_EMAIL)%>">
    </TD>
  </TR>
  <% if (isEnableMyJobsDaysRetrieved) { %>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_myJobs_daysRetrieved")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED)%>"></SPAN>
    </TD>
  </TR>
  <% } %>
  
  <!--For "Enhancement of Overdue notification" issue
        SystemConfigParamNames.OVERDUE_PM_DAY = "overdue.pm.day";
        SystemConfigParamNames.OVERDUE_USER_DAY = "overdue.user.day";
   -->
   <!-- 
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_overdue_PM")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.OVERDUE_PM_DAY%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText" style="text-align:right"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.OVERDUE_PM_DAY)%>" onblur="confirmPositiveNum(this,<%=request.getAttribute(SystemConfigParamNames.OVERDUE_PM_DAY)%>)"/> <%=bundle.getString("lb_days")%></SPAN>
    </TD>
  </TR>

  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_overdue_user")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.OVERDUE_USER_DAY%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText" style="text-align:right"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.OVERDUE_USER_DAY)%>" onblur="confirmPositiveNum(this,<%=request.getAttribute(SystemConfigParamNames.OVERDUE_USER_DAY)%>)"/> <%=bundle.getString("lb_days")%></SPAN>
    </TD>
  </TR>
 -->
  <!-- End For "Enhancement of Overdue notification" issue -->

      
  <INPUT TYPE="hidden" NAME="<%=SystemConfigParamNames.PM_EMAIL_NOTIFICATION%>" VALUE="false">
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_reports_checkAccess")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isReportsCheckAccess) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.REPORTS_CHECK_ACCESS%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isReportsCheckAccess) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.REPORTS_CHECK_ACCESS%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_export_remove_info_enabled")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isExportRemoveInfoEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.EXPORT_REMOVE_INFO_ENABLED%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isExportRemoveInfoEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.EXPORT_REMOVE_INFO_ENABLED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR><TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_duplicationOfObjects_allowed")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isDupOjbAllowed) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isDupOjbAllowed) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_jobSearchReplace_allowed")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isJobSearchAllowed) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isJobSearchAllowed) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_import_suggestJobName")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isImportSuggestJobName) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isImportSuggestJobName) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_jobCosting_lockFinishedCost")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isCostingLockDown) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COSTING_LOCKDOWN%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isCostingLockDown) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.COSTING_LOCKDOWN%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_customerAccessGroup_isDell")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isDell) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.IS_DELL%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isDell) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.IS_DELL%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <!--Add parameter for PPT master translate switch-->
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_ppt_master_translate")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isPPTMasterTranslated) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.PPT_MASTER_TRANSLATE%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isPPTMasterTranslated) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.PPT_MASTER_TRANSLATE%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <!--For "Additional functionality quotation" issue-->
  <tr>
  	<td><SPAN CLASS="standardText"><%=bundle.getString("lb_additional_charges")%></SPAN></td>
  	<td></td>
  </tr>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_per_file_charge_01")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (perFileCharge01Enable) out.print(CHECKED);%>
      NAME="perFileCharge01Enable" VALUE="true" onclick='setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_FILE_CHARGE01_KEY%>","<%=perFileCharge01Value%>");setElementVisible("file_charge_1",true);'/>
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!perFileCharge01Enable) out.print(CHECKED);%>
      NAME="perFileCharge01Enable" VALUE="false" onclick='setElementVisible("file_charge_1",false);setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_FILE_CHARGE01_KEY%>",perSurchargeDefaultValue);'/>
      <%=bundle.getString("lb_no") %>&nbsp;&nbsp;&nbsp;
      <%
        if(perFileCharge01Enable)
           {
      %>
              <span id="file_charge_1" style="visibility:visible">$
      <%
           }else{
      %>
              <span id="file_charge_1" style="visibility:hidden">$
      <%
              }
      %>
          <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.PER_FILE_CHARGE01_KEY%>" id="<%=SystemConfigParamNames.PER_FILE_CHARGE01_KEY%>"
          SIZE="8" MAXLENGTH="8" CLASS="standardText"
          VALUE="<%=perFileCharge01%>" onBlur='confirmPositiveNum(this,"<%=perFileCharge01Value%>")'/>
        </span>
      </SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_per_file_charge_02")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (perFileCharge02Enable) out.print(CHECKED);%>
      NAME="perFileCharge02Enable" VALUE="true" onclick='setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_FILE_CHARGE02_KEY%>","<%=perFileCharge02Value%>");setElementVisible("file_charge_2",true)'/>
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!perFileCharge02Enable) out.print(CHECKED);%>
      NAME="perFileCharge02Enable" VALUE="false" onclick='setElementVisible("file_charge_2",false);setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_FILE_CHARGE02_KEY%>",perSurchargeDefaultValue);'/>
      <%=bundle.getString("lb_no") %>&nbsp;&nbsp;&nbsp;
       <%
        if(perFileCharge02Enable)
           {
      %>
              <span id="file_charge_2" style="visibility:visible">$
      <%
           }else{
      %>
              <span id="file_charge_2" style="visibility:hidden">$
      <%
              }
      %>
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.PER_FILE_CHARGE02_KEY%>" id="<%=SystemConfigParamNames.PER_FILE_CHARGE02_KEY%>"
      SIZE="8" MAXLENGTH="8" CLASS="standardText"
      VALUE="<%=perFileCharge02%>" onBlur='confirmPositiveNum(this,"<%=perFileCharge02Value%>")'/>
      </span>
    </SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_per_job_charge")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (perJobChargeEnable) out.print(CHECKED);%>
      NAME="perJobChargeEnable" VALUE="true" onclick='setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_JOB_CHARGE_KEY%>","<%=perJobChargeValue%>");setElementVisible("job_charge",true);'/>
      <%=bundle.getString("lb_yes")%>
      <INPUT TYPE="radio" <%if (!perJobChargeEnable) out.print(CHECKED);%>
      NAME="perJobChargeEnable" VALUE="false" onclick='setElementVisible("job_charge",false);setPerSurchargeTextInputValue("<%=SystemConfigParamNames.PER_JOB_CHARGE_KEY%>",perSurchargeDefaultValue);'/>
      <%=bundle.getString("lb_no")%>&nbsp;&nbsp;&nbsp;
       <%
        if(perJobChargeEnable)
           {
      %>
              <span id="job_charge" style="visibility:visible">$
      <%
           }else{
      %>
              <span id="job_charge" style="visibility:hidden">$
      <%
              }
      %>
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.PER_JOB_CHARGE_KEY%>" id="<%=SystemConfigParamNames.PER_JOB_CHARGE_KEY%>"
      SIZE="8" MAXLENGTH="8" CLASS="standardText"
      VALUE="<%=perJobCharge%>" onBlur='confirmPositiveNum(this,"<%=perJobChargeValue%>")'/>
      </span>
      </SPAN>
    </TD>
  </TR>
  <!-- Enable Single sign-on -->
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_sso_enableSSO")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (enableSSO) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.ENABLE_SSO%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!enableSSO) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.ENABLE_SSO%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  <!-- Delay time issue-->
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_download_dealy_time")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText" style="text-align:right"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME)%>" onblur="confirmPositiveNum(this,<%=request.getAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME)%>)"/> seconds</SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_task_complete_dealy_time")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME%>"
      SIZE="4" MAXLENGTH="4" CLASS="standardText" style="text-align:right"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME)%>" onblur="confirmPositiveNum(this,<%=request.getAttribute(SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME)%>)"/> seconds</SPAN>
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD COLSPAN="2"><hr></TD>
  </TR>
  <TR>
    <TD COLSPAN="2" ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" onclick="doCancel();"/>
      <INPUT TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>" onclick="submitForm();"/>
    </TD>
  </TR>
  </FORM>
</TABLE>
</DIV>
</BODY>
</HTML>
