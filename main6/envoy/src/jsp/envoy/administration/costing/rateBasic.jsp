<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.util.edit.EditUtil,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.costing.rate.RateConstants,
                  com.globalsight.everest.foundation.LocalePair,
                  com.globalsight.everest.costing.Currency,
                  com.globalsight.everest.costing.Rate,
                  com.globalsight.everest.costing.Currency,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.workflow.Activity,
                  com.globalsight.util.FormUtil,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.Vector,
                  java.util.ArrayList,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.text.NumberFormat"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    
    //for bug GBS-2574,by fan
    NumberFormat numberFormat = NumberFormat.getInstance();
    numberFormat.setGroupingUsed(false);
    numberFormat.setMaximumFractionDigits(5);

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + RateConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_rate");
    }
    else
    {
        saveURL +=  "&action=" + RateConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_rate");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + RateConstants.CANCEL;

    // Data
    ArrayList<Currency> currencies = (ArrayList<Currency>)request.getAttribute(RateConstants.CURRENCIES);
    ArrayList rates = (ArrayList)request.getAttribute(RateConstants.RATES);
    ArrayList rateNames = (ArrayList)request.getAttribute(RateConstants.RATE_NAMES);
    String pivot = (String)request.getAttribute("pivot");

    Vector activities = null;
    Vector localePairs = null;
    Rate theRate = null;
    String currencyName = "";
    String rateName = "";
    int rateType = 4;
    float fixed = 0;
    float hourly = 0;
    float pageRate = 0;
    float inContextExact = 0, inContextExactPer = 0;
    float exact = 0, exactPer = 0;
    float band1 = 0, band1Per = 0;
    float band2 = 0, band2Per = 0;
    float band3 = 0, band3Per = 0;
    float band4 = 0, band4Per = 0;
    float nomatch = 0;
    float repetition = 0, repetitionPer = 0;
    if (edit)
    {
        theRate = (Rate)sessionMgr.getAttribute(RateConstants.RATE);
        rateName = theRate.getName();
        pivot = theRate.getCurrency().getDisplayName(uiLocale);
        rateType = theRate.getRateType().intValue();
        if (rateType == 1)
            fixed = theRate.getUnitRate();
        else if (rateType == 2)
            hourly = theRate.getUnitRate();
        else if (rateType == 3)
            pageRate = theRate.getUnitRate();
        else if (rateType == 4)
        {
            inContextExact = theRate.getInContextMatchRate();
            exact = theRate.getSegmentTmRate();
            band1 = theRate.getHiFuzzyMatchRate();
            band2 = theRate.getMedHiFuzzyMatchRate();
            band3 = theRate.getMedFuzzyMatchRate();
            band4 = theRate.getLowFuzzyMatchRate();
            nomatch = theRate.getNoMatchRate();
            repetition = theRate.getRepetitionRate();
        }
        else
        {
            inContextExact = theRate.getInContextMatchRate();
            exact = theRate.getSegmentTmRate();
            band1 = theRate.getHiFuzzyMatchRate();
            band2 = theRate.getMedHiFuzzyMatchRate();
            band3 = theRate.getMedFuzzyMatchRate();
            band4 = theRate.getLowFuzzyMatchRate();
            nomatch = theRate.getNoMatchRate();
            repetition = theRate.getRepetitionRate();

            inContextExactPer = theRate.getInContextMatchRatePer(); 
            exactPer = theRate.getSegmentTmRatePer(); 
            band1Per = theRate.getHiFuzzyMatchRatePer();
            band2Per = theRate.getMedHiFuzzyMatchRatePer();
            band3Per = theRate.getMedFuzzyMatchRatePer();
            band4Per = theRate.getLowFuzzyMatchRatePer();
            repetitionPer = theRate.getRepetitionRatePer();
        }
    }
    else
    {
        activities = (Vector)request.getAttribute(RateConstants.ACTIVITIES);
        localePairs = (Vector)request.getAttribute(RateConstants.LPS);
    }

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_rate")%>";
var guideNode="rate";
var helpFile = "<%=bundle.getString("help_rate_basic_screen")%>"; 

function $(id){
	return document.getElementById(id).value;
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        rateForm.action = "<%=cancelURL%>";
        rateForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            rateForm.action = "<%=saveURL%>";
            rateForm.submit();
        }
    }
}

function isFloat(field)
{
    var j = 0;
    for (var i = 0; i < field.length; i++)
    {
        if ((field.charAt(i) < "0" || field.charAt(i) > "9") && field.charAt(i) != ".")
        {
            return false;
        }
        if  (field.charAt(i) == ".") {
            if (j ++ > 0) {
                return false;
            }    
        } 
    }
    return true;
}

var showing = "wcSection";
function updateRateType(obj)
{
    if (obj.selectedIndex == 0)
    {
        document.getElementById("fixedSection").style.display = "";
        document.getElementById(showing).style.display = "none";
        showing = "fixedSection";
    }
    else if (obj.selectedIndex == 1)
    {
        document.getElementById("hourlySection").style.display = "";
        document.getElementById(showing).style.display = "none";
        showing = "hourlySection";
    }
    else if (obj.selectedIndex == 2)
    {
        document.getElementById("pageSection").style.display = "";
        document.getElementById(showing).style.display = "none";
        showing = "pageSection";
    }
    else if (obj.selectedIndex == 3)
    {
        document.getElementById("wcSection").style.display = "";
        document.getElementById(showing).style.display = "none";
        showing = "wcSection";
    }
    else
    {
        document.getElementById("wcperSection").style.display = "";
        document.getElementById(showing).style.display = "none";
        showing = "wcperSection";
    }
        
}

//
// Check required fields.
//
function confirmForm()
{
    var buf = stripBlanks(rateForm.rateName.value);

    // Added for creating rates in batches.
    var selLPNum =0;	// Selected Locale Pair count
    var rate_localePair = document.getElementById("lp");
    if(rate_localePair!=null)
    {
    	var localPairName;
    	var splitStr = /[\[\]]/;
    	var subNameArr;
    	var subName ;
    	var tempRateName;
    	var rateNameArr = new Array();
    	var rateNameArr_index = new Array();
    	for(var i=0;i<rate_localePair.length;i++)
    	{
	      if(rate_localePair[i].selected) 
	      {
			selLPNum++;
			localPairName = rate_localePair[i].text;
			subNameArr = localPairName.split(splitStr);
			subName = "_"+subNameArr[1]+"_"+subNameArr[3];
			tempRateName = buf+subName;
	    	rateNameArr.push(tempRateName);
	    	rateNameArr_index.push(i);//store index of LocalePair
	      }
    	}
    	//alert("selLPNum:"+selLPNum+"\nrateNameArr:\n"+rateNameArr+"\nrateNameArr_index:\n"+rateNameArr_index);
    }

    if (isEmptyString(buf))
    {
        alert(" <%= bundle.getString("jsmsg_rate_name") %>");
        rateForm.rateName.value = "";
        rateForm.rateName.focus();
        return false;
    }
     if (hasSpecialChars(buf))
    {
        alert("<%= bundle.getString("lb_name") %>" +" <%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    
    var nameMaxLen=document.getElementById("rateName").getAttribute("maxlength"); 
    var subNameLen=12;
    //alert("nameMaxLen:"+nameMaxLen+"\nbuf.length:"+buf.length); 
    if (buf.length > nameMaxLen) {
        alert(" <%= bundle.getString("jsmsg_rate_name_invalid_length") %>");
        rateForm.rateName.focus();
        return false;
    }else if(selLPNum>1&&buf.length>(nameMaxLen-subNameLen)){
		alert(" <%= bundle.getString("jsmsg_rate_name_invalid_length_mulLocalePair") %>");
        rateForm.rateName.focus();
        return false;
    }

    var allErrorLocalePair = ""; //the error Locale Pair,due exist rate name.
    <%
    if (rateNames != null)
    {
    	for (int i = 0; i < rateNames.size(); i++)
        {
            String rateName2 = (String)rateNames.get(i);
    %>
    	    if(selLPNum>1) // Added for creating rates in batches
    	    {
				var tempLocalePair;
				var tempLocalePairIndex;
				for(var j=0;j<rateNameArr.length;j++)
				{
					if("<%=rateName2%>".toLowerCase() == rateNameArr[j].toLowerCase())
					{
						tempLocalePairIndex = parseInt(rateNameArr_index[j]);
						tempLocalePair=rate_localePair[tempLocalePairIndex].text;
						allErrorLocalePair=allErrorLocalePair+tempLocalePair+"\n";
					}		
				}
    	    } 
            else if ("<%=rateName2%>".toLowerCase() == buf.toLowerCase() && (!"<%=edit%>" || "<%=rateName2%>" != "<%=rateName%>"))
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_rate_error"))%>");
                return false;
            }
    <%
        }
    }
    %>
    if(allErrorLocalePair!="")//Stop, and display the error Locale Pair
	{
		alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_rate_error_mul"))%>"+allErrorLocalePair);
        return false;
	}

    
    <% if (!edit) { %>
    if (rateForm.activity.selectedIndex == 0)
    {
        alert(" <%= bundle.getString("jsmsg_rate_activity_type") %>");
        rateForm.activity.focus();
        return false;
    }

    if (rateForm.lp.selectedIndex < 0)
    {
        alert(" <%= bundle.getString("jsmsg_rate_locale_pair") %>");
        rateForm.lp.focus();
        return false;
    }
    <% } %>
    if (rateForm.currency.selectedIndex == 0)
    {
        alert(" <%= bundle.getString("jsmsg_rate_currency") %>");
        rateForm.currency.focus();
        return false;
    }
    if (rateForm.rateType.selectedIndex == 0)
    {
        // fixed
        buf = stripBlanks(rateForm.fixed.value);
        if (!isFloat(buf) || isEmptyString(buf))
        {
            alert("<%=bundle.getString("lb_rate")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
        }   
    }
    else if (rateForm.rateType.selectedIndex == 1)
    {
        // hourly
        buf = stripBlanks(rateForm.hourly.value);
        if (!isFloat(buf) || isEmptyString(buf))
        {
            alert("<%=bundle.getString("lb_rate")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
        }
    }
    else if (rateForm.rateType.selectedIndex == 2)
    {
        // page
        buf = stripBlanks(rateForm.page.value);
        if (!isFloat(buf) || isEmptyString(buf))
        {
            alert("<%=bundle.getString("lb_rate")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
        }
    }
    else if (rateForm.rateType.selectedIndex == 3)
    {
        // word count
	//

	if(!isValidRate($('inContextExact'))){
            alert("<%=bundle.getString("lb_in_context")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}


	if(!isValidRate($('exact'))){
            alert("<%=bundle.getString("lb_exact")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('band1'))){
            alert("<%=bundle.getString("lb_95")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}
    

	if(!isValidRate($('band2'))){
            alert("<%=bundle.getString("lb_85")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}
    

	if(!isValidRate($('band3'))){
            alert("<%=bundle.getString("lb_75")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}


	if(!isValidRate($('band4'))){
            alert("<%=bundle.getString("lb_50")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}


	if(!isValidRate($('nomatch'))){
            alert("<%=bundle.getString("lb_no_match")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}


	if(!isValidRate($('repetition'))){
            alert("<%=bundle.getString("lb_repetition_word_cnt")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

    }
    else if (rateForm.rateType.selectedIndex == 4)
    {
        // word count by %

	if(!isValidRate($('inContextExactC'))){
            alert("<%=bundle.getString("lb_in_context")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('inContextExactPer'))){
        alert("<%=bundle.getString("lb_in_context_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
        return false;
	}

	if(!isValidRate($('exactC'))){
            alert("<%=bundle.getString("lb_exact")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('exactPer'))){
        alert("<%=bundle.getString("lb_exact_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
        return false;
	}
	
	if(!isValidRate($('band1C'))){
            alert("<%=bundle.getString("lb_95")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}
    
	
	if(!isValidRate($('band1Per'))){
            alert("<%=bundle.getString("lb_95_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('band2C'))){
            alert("<%=bundle.getString("lb_85")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('band2Per'))){
            alert("<%=bundle.getString("lb_85_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}
    

	if(!isValidRate($('band3C'))){
            alert("<%=bundle.getString("lb_75")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}


	if(!isValidRate($('band3Per'))){
            alert("<%=bundle.getString("lb_75_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('band4C'))){
            alert("<%=bundle.getString("lb_50")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

	if(!isValidRate($('band4Per'))){
        alert("<%=bundle.getString("lb_50_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
        return false;
	}

	if(!isValidRate($('baserate'))){
            alert("<%=bundle.getString("lb_no_match")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
            return false;
	}

    }
    return true;
}


function isValidRate(value) {
    buf = stripBlanks(value);
    if (!isFloat(buf) || isEmptyString(buf)){
      return false;
    }
    else {
      return true;
    }
}

function calculateRate() {
	var baseRate = $("baserate");
	var decimalDigits = $("decimalDigits");
	if (!isValidRate(baseRate)) {
		alert("<%=bundle.getString("lb_no_match")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;
	}
	if(!isValidRate($('inContextExactPer'))){
		alert("<%=bundle.getString("lb_in_context_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;
	}
	if(!isValidRate($('exactPer'))){
		alert("<%=bundle.getString("lb_exact_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
	if(!isValidRate($('band1Per'))){
		alert("<%=bundle.getString("lb_95_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
	if(!isValidRate($('band2Per'))){
		alert("<%=bundle.getString("lb_85_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
	if(!isValidRate($('band3Per'))){
		alert("<%=bundle.getString("lb_75_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
	if(!isValidRate($('band4Per'))){
		alert("<%=bundle.getString("lb_50_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
	if(!isValidRate($('repetitionPer'))){
		alert("<%=bundle.getString("lb_no_match_repetition_per")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
		return false;	
	}
    if(!(decimalDigits==0||decimalDigits==1||decimalDigits==2||decimalDigits==3||decimalDigits==4||decimalDigits==5))
	{
		alert("<%= bundle.getString("lb_rate_decimal_digits_invalid") %>");
		return false;
	}
	var digits = Math.pow(10, decimalDigits);
	var dBaseRate = parseFloat(baseRate);
	document.getElementById("inContextExactC").value = Math.round((dBaseRate*parseFloat($("inContextExactPer"))/100)*digits)/digits;
	document.getElementById("exactC").value = Math.round((dBaseRate*parseFloat($("exactPer"))/100)*digits)/digits;
	document.getElementById("band1C").value = Math.round((dBaseRate*parseFloat($("band1Per"))/100)*digits)/digits;
	document.getElementById("band2C").value = Math.round((dBaseRate*parseFloat($("band2Per"))/100)*digits)/digits;
	document.getElementById("band3C").value = Math.round((dBaseRate*parseFloat($("band3Per"))/100)*digits)/digits;
	document.getElementById("band4C").value = Math.round((dBaseRate*parseFloat($("band4Per"))/100)*digits)/digits;
	document.getElementById("repetitionC").value = Math.round((dBaseRate*parseFloat($("repetitionPer"))/100)*digits)/digits;
}
</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
    <br>
    <br>

<form name="rateForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td width=150px>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" id="rateName" name="rateName" value="<%=rateName%>" maxlength="40">
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_activity_type")%><span class="asterisk">*</span>:
          </td>
          <td>
            <%
            if (edit)
            {
                out.println(theRate.getActivity().getDisplayName());
            } else {
                out.println("<select name=activity>");
                out.println("<option value=-1>" + bundle.getString("lb_choose") + 
                        "</option>");
                for (int i = 0; i < activities.size(); i++)
                {
                    Activity act = (Activity) activities.get(i);
                    out.println("<option value=\"" + act.getActivityName() + "\">" +
                              act.getDisplayName() + "</option>");
                }
                out.println("</select>");
            }
            %>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_locale_pair")%><span class="asterisk">*</span>:
          </td>
          <td>
            <%
            if (edit)
            {
                LocalePair pair = theRate.getLocalePair();
                out.println(pair.getSource().getDisplayName(uiLocale) + "&#x2192;" +
                            pair.getTarget().getDisplayName(uiLocale));
            } else {
            %>
            	<table><tr><td>
                <select name=lp id=lp size=10 multiple>
            <% 
                //out.println("<option value=-1>" + bundle.getString("lb_choose")+"</option>");
                for (int i = 0; i < localePairs.size(); i++)
                {
                    LocalePair lp = (LocalePair) localePairs.get(i);
                    out.println("<option value=" + lp.getId() + ">" +
                              lp.getSource().getDisplayName(uiLocale) + "->" +
                             lp.getTarget().getDisplayName(uiLocale) + "</option>");
                }
             %>
                </select>
                </td><td>
                
                </td><td>
                </td></tr></table>
            <% 
            }	
            %>
          </td>
        </tr>
        <tr>
          <td valign="middle">
            <%=bundle.getString("lb_currency")%><span class="asterisk">*</span>:
          </td>
          <td>
            <%
                out.println("<select name=currency>");
                out.println("<option value=-1>" + bundle.getString("lb_choose") + 
                        "</option>");
                for (int i = 0; i < currencies.size(); i++)
                {
                    Currency curr = currencies.get(i);
                    if (curr.getDisplayName(uiLocale).equals(pivot))
                    {
                        out.println("<option value=\"" + curr.getIsoCode() + "\" selected>" +
                              curr.getDisplayName(uiLocale) + "</option>");
                    } else {
                        out.println("<option value=\"" + curr.getIsoCode() + "\">" +
                              curr.getDisplayName(uiLocale) + "</option>");
                    }
                }
                out.println("</select>");
             %>
          </td>
        </tr>
        <tr>
          <td valign="center">
            <%=bundle.getString("lb_rate_type")%><span class="asterisk">*</span>:
          </td>
          <td>
            <%
                out.println("<select name=rateType onchange=updateRateType(this)>");
                for (int i = 0; i < rates.size(); i++)
                {
                    String type = (String) rates.get(i);
                    int idx = i+1;
                    if (idx == rateType)
                        out.println("<option value=" + idx + " selected>" + type + "</option>");
                    else
                        out.println("<option value=" + idx + " >" + type + "</option>");
                }
                out.println("</select>");
            %>
          </td>
        </tr>
        <tr id="fixedSection" style="display:none">
          <td>
            <%=bundle.getString("lb_rate")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="fixed" value="<%=numberFormat.format(fixed)%>"> <%=bundle.getString("lb_rate_unit_1")%>
          </td>
        </tr>
        <tr id="hourlySection" style="display:none">
          <td>
            <%=bundle.getString("lb_rate")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="hourly" value="<%=numberFormat.format(hourly)%>"> <%=bundle.getString("lb_rate_unit_2")%>
          </td>
        </tr>
        <tr id="pageSection" style="display:none">
          <td>
            <%=bundle.getString("lb_rate")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="page" value="<%=numberFormat.format(pageRate)%>"> <%=bundle.getString("lb_rate_unit_3")%>
          </td>
        </tr>
        <tr id="wcSection">
            <td valign="top" style="padding-top:9px" nowrap>
              <%=bundle.getString("lb_rate_by_match_type")%>:
            </td>
            <td>
              <table>
              <tr>
                <td width=25% class="standardText" nowrap>
                  <%=bundle.getString("lb_in_context")%><span class="asterisk">*</span>:
                </td>
                <td width=25%>
                  <input type="text" name="inContextExact" id="inContextExact" size=5 value="<%=numberFormat.format(inContextExact)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_repetition_word_cnt")%><span class="asterisk">*</span>:
                </td>
                <td>
                  <input type="text" name="repetition" id="repetition" size=5 value="<%=numberFormat.format(repetition)%>">
                </td>
              </tr>
              <tr>
                <td width=25% class="standardText" nowrap>
                  <%=bundle.getString("lb_100")%><span class="asterisk">*</span>:
                </td>
                <td width=25%>
                  <input type="text" name="exact" id="exact" size=5 value="<%=numberFormat.format(exact)%>">
                </td>
              </tr>
              <tr>
                <td class="standardText" nowrap>
                  <%=bundle.getString("lb_95")%>:
                </td>
                <td>
                  <input type="text" name="band1" id="band1" size=5 value="<%=numberFormat.format(band1)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_85")%>:
                </td>
                <td>
                  <input type="text" name="band2" id="band2" size=5 value="<%=numberFormat.format(band2)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_75")%>:
                </td>
                <td>
                  <input type="text" name="band3" id="band3" size=5 value="<%=numberFormat.format(band3)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_50")%>:
                </td>
                <td>
                  <input type="text" name="band4" id="band4" size=5 value="<%=numberFormat.format(band4)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_no_match")%><span class="asterisk">*</span>:
                </td>
                <td>
                  <input type="text" name="nomatch" id="nomatch" size=5 value="<%=numberFormat.format(nomatch)%>">
                </td>
              </tr>
              <tr style="display:none">
                <td style="padding-left:0px" class="standardText">
                  <%=bundle.getString("lb_context_tm")%><span class="asterisk">*</span>:
                </td>
                <td>
                  <input type="text" name="context" id="context" size=5 value="0">
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <!-- Added by Vincent Yan 09/07/10 #537 -->
        <tr id="wcperSection" style="display:none;">
            <td valign="top" style="padding-top:9px" nowrap>
              <%=bundle.getString("lb_rate_by_match_type")%>:
            </td>
            <td>
              <table>
              <tr>
                <td class="standardText" style="white-space:nowrap;width:25%;">
                  <%=bundle.getString("lb_in_context")%><span class="asterisk">*</span>:
                </td>
                <td style="white-space:nowrap;width:25%;">
                  <input type="text" name="inContextExactPer" id="inContextExactPer" size=5 value="<%=inContextExactPer==0.0?"0":numberFormat.format(inContextExactPer)%>" onfocus="this.select()">&nbsp;%&nbsp;&nbsp;
                </td>
                <td width=25%>
                  <input type="text" name="inContextExactC" id="inContextExactC" size=8 value="<%=numberFormat.format(inContextExact)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_repetition_word_cnt")%><span class="asterisk">*</span>:
                </td>
                <td>
                  <input type="text" name="repetitionPer" id="repetitionPer" size=5 value="<%=repetitionPer==0.0?"0":numberFormat.format(repetitionPer)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td>
                  <input type="text" name="repetitionC" id="repetitionC" size=8 value="<%=numberFormat.format(repetition)%>">
                </td>
              </tr>
              <tr>
                <td width=25% class="standardText" nowrap>
                  <%=bundle.getString("lb_100")%><span class="asterisk">*</span>:
                </td>
                <td width=25%>
                  <input type="text" name="exactPer" id="exactPer" size=5 value="<%=exactPer==0.0?"0":numberFormat.format(exactPer)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td width=25%>
                  <input type="text" name="exactC" id="exactC" size=8 value="<%=numberFormat.format(exact)%>">
                </td>
              </tr>
              <tr>
                <td class="standardText" nowrap>
                  <%=bundle.getString("lb_95")%>:
                </td>
                <td>
                  <input type="text" name="band1Per" id="band1Per" size=5 value="<%=band1Per==0.0?"0":numberFormat.format(band1Per)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td>
                  <input type="text" name="band1C" id="band1C" size=8 value="<%=numberFormat.format(band1)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_85")%>:
                </td>
                <td>
                  <input type="text" name="band2Per" id="band2Per" size=5 value="<%=band2Per==0.0?"0":numberFormat.format(band2Per)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td>
                  <input type="text" name="band2C" id="band2C" size=8 value="<%=numberFormat.format(band2)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_75")%>:
                </td>
                <td>
                  <input type="text" name="band3Per" id="band3Per" size=5 value="<%=band3Per==0.0?"0":numberFormat.format(band3Per)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td>
                  <input type="text" name="band3C" id="band3C" size=8 value="<%=numberFormat.format(band3)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" nowrap>
                  <%=bundle.getString("lb_50")%>:
                </td>
                <td>
                  <input type="text" name="band4Per" id="band4Per" size=5 value="<%=band4Per==0.0?"0":numberFormat.format(band4Per)%>" onfocus="this.select()">&nbsp;%
                </td>
                <td>
                  <input type="text" name="band4C" id="band4C" size=8 value="<%=numberFormat.format(band4)%>">
                </td>
              </tr>
              <tr>
                <td style="padding-left:0px" class="standardText" width="35%" nowrap>
                  <%=bundle.getString("lb_rate_base")%><span class="asterisk">*</span>:
                </td>
                <td>
                  <input type="text" name="baserate" id="baserate" size=5 value="<%=nomatch==0.0?"0":numberFormat.format(nomatch)%>" onfocus="this.select()">
                </td>
                <td>
                  <input type="button" name="calculate" id="calculate" value="<%=bundle.getString("lb_rate_calculate")%>" onclick="calculateRate()">
                </td>
              </tr>
			  <tr>
                <td></td>
                <td></td>
                <td class="standardText" nowrap>
                 <%=bundle.getString("lb_rate_decimal_digits")%>: <input type="text" name="decimalDigits" id="decimalDigits" size=5 value="2" onfocus="this.select()">(0-5)
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
  </td>
</tr>
</table>
<script>
// make sure the correct fields are showing for rate
<% if (edit) {  %>
     rateForm.rateType.selectedIndex = "<%=rateType -1%>";
     if (rateForm.rateType.selectedIndex != 3)
     {
        updateRateType(rateForm.rateType);
     }
<% } %>
</script>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_RATE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</form>
</body>
</html>