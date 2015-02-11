<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.progress.ProcessStatus,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.tm.searchreplace.SearchReplaceManager,
            com.globalsight.everest.tm.searchreplace.SearchReplaceManagerLocal,
            com.globalsight.everest.util.comparator.LocalePairComparator,
            com.globalsight.everest.util.comparator.StringComparator,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.StringUtil,
            com.globalsight.util.SortUtil,
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper,
            java.util.ArrayList,
            java.util.Arrays,            
            java.util.Iterator,
            java.util.Locale,
            java.util.ResourceBundle,
            java.util.Vector,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.foundation.LocalePair,
            com.globalsight.everest.corpus.CorpusDoc,            
            com.globalsight.everest.corpus.CorpusDocGroup,
            com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            java.sql.Connection,
            com.globalsight.ling.tm.ExactMatchedSegments,
            com.globalsight.ling.tm.LeveragingLocales,
            com.globalsight.ling.tm2.*,
            com.globalsight.ling.tm2.BaseTmTu,
            com.globalsight.ling.tm2.BaseTmTuv,
            com.globalsight.ling.tm2.SegmentTmTu,
            com.globalsight.ling.tm2.SegmentTmTuv,
            com.globalsight.ling.tm2.leverage.*,
            com.globalsight.ling.tm2.persistence.*,
            com.globalsight.everest.tm.searchreplace.TmConcordanceResult,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.OverridableLeverageOptions,
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.everest.webapp.pagehandler.tm.maintenance.TableMaker
            "
    session="true"
%><%@page import="com.globalsight.ling.common.URLEncoder"%>
<%!
private String makeJavaScriptArray(List p_vector)
{
    StringBuffer sb = new StringBuffer();
    for (int i = 0, max = p_vector.size(); i < max; i++)
    {
        sb.append("\"");
        sb.append(p_vector.get(i));
        sb.append("\"");

        if (i < max - 1)
        {
            sb.append(",");
        }
    }

    return sb.toString();
}

private List makeTargetLangVector(List p_pairVector)
{
    List result = new ArrayList(p_pairVector.size());
    for (int i = 0; i < p_pairVector.size(); i++)
    {
        LocalePair lp = (LocalePair)p_pairVector.get(i);
        result.add(lp.getTarget().getLocale());
    }

    return result;
}
%><%
String searchType = request.getParameter("searchType");
if (searchType == null) searchType = "fullTextSearch";

SessionManager sessionMgr =
  (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

ProcessStatus m_status =
  (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TM_TM_STATUS);
int counter    = 0;
int percentage = -1;
String message = "";

if (m_status != null)
{
  counter = m_status.getCounter();
  percentage = m_status.getPercentage();
  message = m_status.getMessage();
}

if (! "fullTextSearch".equals(searchType))
{
    //pretend we're done to avoid the use of progress bar for the other two
    //types of searches for now
    percentage=100;
}

String lb_search_msg = bundle.getString("lb_search_msg");
String lbSource = bundle.getString("lb_tm_search_source_locale") +
    bundle.getString("lb_colon");
String lbTarget = bundle.getString("lb_tm_search_target_locale") +
    bundle.getString("lb_colon");
String lb_help = bundle.getString("lb_help");

String action = "/globalsight/ControlServlet?activityName=browseCorpus&pagename=CTMB&" +
  WebAppConstants.TM_SEARCH_STATE_PARAM + "=";
String stateNormal = WebAppConstants.TM_SEARCH_STATE_NORMAL;
String stateShowNext = WebAppConstants.TM_SEARCH_STATE_NEXT;
String stateShowPrev = WebAppConstants.TM_SEARCH_STATE_PREV;
String stateRefresh = WebAppConstants.TM_ACTION_REFRESH;

//get request values
boolean fromEditor = (Boolean)request.getAttribute("fromEditor");
boolean showAllTms = (Boolean)request.getAttribute("showAllTms");
boolean enableTMAccessControl = (Boolean)request.getAttribute("enableTMAccessControl");

String refreshUrl = action + stateRefresh;
refreshUrl += "&fromEditor=" + fromEditor;
String refreshMetaTag = PageHandler.getRefreshMetaTagForProgressBar(refreshUrl);

boolean isFresh = false;
if (request.getAttribute("isFresh") != null)
{
    isFresh = ((Boolean)request.getAttribute("isFresh")).booleanValue();
}
String queryText = (String)request.getAttribute("queryText"); //already UTF-8 and cleaned
if (queryText == null)
{
    queryText = "";
}

ArrayList tmNames = (ArrayList) request.getAttribute("tmNames");
ArrayList tmListOfUser = (ArrayList)sessionMgr.getAttribute("tmListOfUser");
if(session.getAttribute("tmNames") != null)
{
	tmNames = (ArrayList) session.getAttribute("tmNames");
}
if(enableTMAccessControl)
{
    if(fromEditor)
    {
        //From segment editor
        ArrayList tms = new ArrayList();
        Iterator it = tmNames.iterator();
        while(it.hasNext())
        {
            String tm = (String)it.next();
            if(tmListOfUser!=null&&tmListOfUser.contains(tm))
            {
                tms.add(tm);
            }
        }
        tmNames = tms;
    }
    else
    {
        if(tmListOfUser!=null)
        {
            tmNames = tmListOfUser;   
        }
    }
}   
String tableRows = (String) request.getAttribute("tableRows");
// get actual concordance results on the session manager
TmConcordanceResult searchResults =
    (TmConcordanceResult)sessionMgr.getAttribute("results");

int numRecords = 0;
int min = 0;
int max = 0;
if (!isFresh && searchResults != null)
{
    numRecords = searchResults.getTotal();
    min = searchResults.getMin();
    max = searchResults.getMax();
}

GlobalSightLocale sourceLocale =
  (GlobalSightLocale)request.getAttribute("sourceLocale");
GlobalSightLocale targetLocale =
  (GlobalSightLocale)request.getAttribute("targetLocale");
LocalePair localePair = (LocalePair)request.getAttribute("localePair");

int tmIndex = 0;
String[] tmIndexParams = null;
if ("fuzzySearch".equals(searchType) || "fullTextSearch".equals(searchType))
{
//    tmIndexParams = (String[]) request.getParameterValues("tmIndex");
 //   if (tmIndexParams == null)
        tmIndexParams = (String[]) request.getAttribute("tmIndex");
 	  if(tmIndexParams == null)
 	  {
 		 tmIndexParams = (String[]) request.getParameterValues("tmIndex");
 	  }
 	  if(tmIndexParams == null)
 	  {
 		 tmIndexParams = (String[]) sessionMgr.getAttribute("tmIndex");
 	  }
}
else
{
    String tmIndexParam = (String) request.getParameter("tmIndex");
    if (tmIndexParam == null)
        tmIndexParam = (String) request.getAttribute("tmIndex");
    if (tmIndexParam != null)
    {
        tmIndex = Integer.valueOf(tmIndexParam).intValue();
    }
}

Collection corpusDocs = (Collection) request.getAttribute("corpusDocs");
Integer fuzzyOverride = (Integer) request.getAttribute("fuzzyOverride");
String companyId = null;
List pairs = WorkflowTemplateHandlerHelper.getAllLocalePairs(uiLocale);
// remove the duplicated pairs for super admin
Map<String, LocalePair> pairsMap = new HashMap<String, LocalePair>();
for (int i=0;i < pairs.size(); i++) {
    LocalePair lp = (LocalePair) pairs.get(i);
    if (companyId == null)
    {
    	companyId = String.valueOf(lp.getCompanyId());
    }
    String key = lp.getSource().getId() + "-" + lp.getTarget().getId();
    pairsMap.put(key, lp);
}    
pairs = new ArrayList();
Iterator pairsIter = pairsMap.keySet().iterator();
while (pairsIter != null && pairsIter.hasNext()) {
	Object key = pairsIter.next();
	LocalePair lp = pairsMap.get(key);
	pairs.add(lp);
}
SortUtil.sort(pairs, new LocalePairComparator(uiLocale));
Vector leverageDisp =
  (Vector)sessionMgr.getAttribute(WorkflowTemplateConstants.LEVERAGE_DISP);
Vector leverageObjs =
  (Vector)sessionMgr.getAttribute(WorkflowTemplateConstants.LEVERAGE_OBJ);

String[] chosenLeveragesParam =
  (String[])request.getParameterValues("chosenLeverages");
Vector chosenLeverages = null;
if (chosenLeveragesParam != null)
{
    chosenLeverages = new Vector(Arrays.asList(chosenLeveragesParam));
}
// Create a string representation of leverageObjs, leverageDisp, and pairs
// so we can turn them into javascript arrays
String leverageObjsString = makeJavaScriptArray(leverageObjs);
String leverageDispString = makeJavaScriptArray(leverageDisp);
String targetLangString = makeJavaScriptArray(makeTargetLangVector(pairs));

//get fuzzy search results
LeverageDataCenter leverageDataCenter =
  (LeverageDataCenter)request.getAttribute("leverageResults");
%><%@page import="com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;"%>
<HTML>
<HEAD>
<!-- This JSP is envoy/tm/corpus/corpusBrowser.jsp -->
<%if (percentage > -1 && percentage < 100) { 
%>
<%=refreshMetaTag%>
<% } %>
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<TITLE><%=bundle.getString("lb_corpus_browser")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<STYLE>
#idProgressContainer { border: solid 1px ; z-index: 1; 
                 position: absolute; top: 450; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px ; 
                 position: absolute; top: 450; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 480; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 540; left: 20; height: 80; width: 400; }

.help         {
                font-size: 10pt; 
                color: blue;
                cursor: hand;
				cursor: pointer;
                text-decoration: underline;
              }

.clickable    {
                cursor: hand;
				cursor: pointer;
              }
</STYLE>
<SCRIPT LANGUAGE="JavaScript">
var isIE 		= window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox 	= window.navigator.userAgent.indexOf("Firefox")>0;
var helpFile = "<%=bundle.getString("help_corpus_browser")%>";
function helpSwitch() 
{  
  // The variable helpFile must be defined in each JSP.
  helpWindow = window.open(helpFile, 'helpWindow',
    'resizable=yes,scrollbars=yes,WIDTH=730,HEIGHT=400');
  helpWindow.focus();
}

var WIDTH = 400;
var corpuswins= new Array();
var numcorpuswins = 0;
function showCorpus(p_tuvId, p_srcLocaleId)
{
   var url = "/globalsight/ControlServlet?activityName=viewCorpusMatches&tuvId=" +
     p_tuvId + "&localeDbId=" + p_srcLocaleId;
   var name = "corpus" + numcorpuswins;
   corpuswins[numcorpuswins++] = window.open(url, name,
     'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
}

function doOnload()
{
   window.focus();
   <% if ("docNameSearch".equals(searchType)) { %>
      setupDocNameSearch();
   <% } else if ("fuzzySearch".equals(searchType)) { %>
      setupFuzzySearch();
   <% } else { %>
      setupFullTextSearch();
   <% } %>

   document.searchForm.queryText.focus();
}

function doOnUnload()
{
   for (var i=0; i < numcorpuswins; i++) {
      try { corpuswins[i].close(); } catch (ignore) {}
   }
}

function setupFullTextSearch()
{
<% if (percentage == -1 || percentage == 100) { %>
   hideProgressBar();
<% } %>

<% if (!fromEditor) {%>
   document.searchForm.localePair.disabled = false;
   document.getElementById("divLocalePair").style.display = 'block';
<% } %>
   document.searchForm.tmIndex.disabled = false;
   document.searchForm.fuzzyOverride.disabled = true;
   document.searchForm.tmProfileId.disabled = true;
   document.searchForm.chosenLeverages.disabled = true;
   document.searchForm.tmIndex.multiple = true;
   for (var i = 0; i < document.searchForm.tmIndex.options.length; i++)
   {
       if (tmIndexes[i] != undefined)
           document.searchForm.tmIndex.options[i].selected = true;
       else
           document.searchForm.tmIndex.options[i].selected = false;
   }
   var msg = document.getElementById("labelQueryText");
   msg.childNodes.item(0).nodeValue = "<%=bundle.getString("lb_conc_search_text")%>" +":";

   document.getElementById("divTmIndex").style.display = 'block';
   document.getElementById("divFuzzy").style.display = 'none';
}

function setupFuzzySearch()
{
   hideProgressBar();
   populateLeverage();

<% if (!fromEditor) {%>
   document.searchForm.localePair.disabled = false;
   document.getElementById("divLocalePair").style.display = 'block';
<% } %>
   document.searchForm.tmIndex.disabled = false;
   document.searchForm.fuzzyOverride.disabled = false;
   document.searchForm.tmProfileId.disabled = false;
   document.searchForm.chosenLeverages.disabled = false;
   document.searchForm.tmIndex.multiple = true;
   for (var i = 0; i < document.searchForm.tmIndex.options.length; i++)
   {
       if (tmIndexes[i] != undefined)
           document.searchForm.tmIndex.options[i].selected = true;
       else
           document.searchForm.tmIndex.options[i].selected = false;
   }
   var msg = document.getElementById("labelQueryText");
   msg.childNodes.item(0).nodeValue = "<%=bundle.getString("lb_conc_search_text")%>"  +":";

   document.getElementById("divTmIndex").style.display = 'none';
   document.getElementById("divFuzzy").style.display = 'block';
   document.getElementById("fuzzythreshold").style.display = "none";
}

function setupDocNameSearch()
{
   hideProgressBar();
<% if (!fromEditor) {%>
   document.searchForm.localePair.disabled = true;
   document.getElementById("divLocalePair").style.display = 'none';
<% } %>
   document.searchForm.tmIndex.disabled = true;
   document.searchForm.fuzzyOverride.disabled = true;
   document.searchForm.tmProfileId.disabled = true;
   document.searchForm.chosenLeverages.disabled = true;
   document.searchForm.tmIndex.multiple = false;
   var msg = document.getElementById("labelQueryText");
   msg.childNodes.item(0).nodeValue = "<%=bundle.getString("lb_corpus_doc_name")%>"  +":";

   document.getElementById("divTmIndex").style.display = 'none';
   document.getElementById("divFuzzy").style.display = 'none';
}

var wins = new Array();
var numwins = 0;

function deleteCorpusDoc(cuvId, link)
{
   if (link.disabled == true)
      return false;

   var url = "/globalsight/envoy/tm/corpus/deleteCorpus.jsp?cuvId=" + cuvId;
   var name = "delete" + numwins;
   var agree = confirm('<%=bundle.getString("jsmsg_corpus_deldoc")%>');
   if (agree)
   {
      window.showModalDialog(url, null, "center:yes; help:no; resizable:no; status:no; dialogWidth: 300px; dialogHeight: 180px; ");
      link.disabled = true;
      return false;
   }
   else
   {
      return true;
   }
}

//Added for save ppt file in IE,refer GBS-1128. 
String.prototype.endWith=function(oString){  
	var reg=new RegExp(oString+"$");  
	return reg.test(this);    
}

function isSpecialCondition(url)
{
	if(isIE && (url.endWith("pptx") || url.endWith("ppt")))
	{
		return true;
	}
}

function showCorpusDoc(url)
{
   var name = "corpusDoc" + numwins;
   var newurl = "/globalsight" + url;

   //Modify for save ppt file in IE,refer GBS-1128.
   if(isSpecialCondition(newurl))
   {
	   wins[numwins++] = window.location = newurl;
   }
   else
   {
	   wins[numwins++] = window.open(newurl, name, 'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
   }
   
}

function showCorpusDocText(gxmlUrl,docName)
{
   var name = "corpusDocText" + numwins;
   var _gxmlUrl = encodeURIComponent(gxmlUrl);
   var _docName = encodeURIComponent(docName);
   var url = "/globalsight/envoy/tm/corpus/justShowText.jsp?gxmlUrl=/globalsight" + _gxmlUrl + "&docName=" + _docName;
   wins[numwins++] = window.open(url, name, 'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}

var possibleLevLocales = new Array(<%=leverageObjsString%>);
var possibleLevLocalesDisplay = new Array(<%=leverageDispString%>);
var targetLangs = new Array(<%=targetLangString%>);
var chosenLeveragesArray = new Array();
<%
for (int i=0; chosenLeverages != null && i < chosenLeverages.size(); i++) { 
%>
chosenLeveragesArray["<%=chosenLeverages.get(i)%>"] = "<%=chosenLeverages.get(i)%>";
<%}%>
var tmIndexes = new Array();
<%
if (("fuzzySearch".equals(searchType) || "fullTextSearch".equals(searchType))
      && tmIndexParams != null) {
    for (int i=0; i < tmIndexParams.length; i++) { 
%>
        tmIndexes[<%=tmIndexParams[i]%>] = "<%=tmIndexParams[i]%>";
<%  }
}
%>

function populateLeverage()
{
<% if (fromEditor) { %>
    populateLeverageFromFixedLocalePair();
<% } else { %>
    populateLeverageFromList(document.searchForm.localePair);
<% } %>
}

function populateLeverageFromList(localePairComboBox)
{
   var targetLang = null;
   var targetLoc = null;
   var options_string = "";

   // The target lang that was selected
   for (var i = 0; i < localePairComboBox.options.length; i++)
   {
      if (localePairComboBox.options[i].selected == true)
      {
         targetLang = localePairComboBox.options[i].id;
         break;
      }
   }

   // Populate the "Leverage From:" multi-select box
   searchForm.chosenLeverages.length = 0; // Clear the multi-select box
   var count = 0;
   for (var i = 0; i < possibleLevLocales.length; i++)
   {
      var loc = possibleLevLocales[i];
     
     if (possibleLevLocales[i].indexOf(targetLang) != -1)
      {
         searchForm.chosenLeverages.options[count] = 
            new Option(possibleLevLocalesDisplay[i], possibleLevLocales[i]);
         
         if (chosenLeveragesArray[possibleLevLocales[i]] != null ||
             loc == targetLoc)
         {
           searchForm.chosenLeverages.options[count].selected=true;
         }
         count++;
      }
   }

}

function populateLeverageFromFixedLocalePair()
{
   var targetLang = null;
   var targetLoc = null;
   var options_string = "";

   // The target lang that was selected
   <% if (fromEditor) {%>
   targetLoc = "<%=targetLocale.getLocale()%>";
   targetLang = "<%=targetLocale.getLocale().getLanguage()%>";
   <% } %>

   // Populate the "Leverage From:" multi-select box
   searchForm.chosenLeverages.length = 0; // Clear the multi-select box
   var count = 0;
   for (var i = 0; i < possibleLevLocales.length; i++)
   {
      var loc = possibleLevLocales[i];
      var langCode = loc.substring(0,2);
      if (langCode == targetLang)
      {
         searchForm.chosenLeverages.options[count] = 
            new Option(possibleLevLocalesDisplay[i], possibleLevLocales[i]);
         
         if (chosenLeveragesArray[possibleLevLocales[i]] != null ||
             loc == targetLoc)
         {
           searchForm.chosenLeverages.options[count].selected=true;
           searchForm.chosenLeverages.selectedIndex = count;
         }
         count++;
      }
   }

}

function scrollLeverageLocalesBox()
{
   var cbo = document.all('chosenLeverages');
   cbo[cbo.selectedIndex].selected = cbo[cbo.selectedIndex].selected;
}

function buildParams()
{
	var orderedTM = "&orderedTM=";
	var projectTmsSelectBox = searchForm.tmIndex;
	for(var i = 0; i < projectTmsSelectBox.options.length; i++)
	{
		var projectTm = projectTmsSelectBox.options[i];
		if(projectTm.selected)
		{
			if(document.recalc)
			{
				orderedTM += projectTm.innerHTML;
			}
			else
			{
				orderedTM += projectTm.text;
			}
			if(i != projectTmsSelectBox.options.length - 1)
			{
				orderedTM += ":";
			}
		}
		
	}
	if(projectTmsSelectBox.options.length == 0)
	{
		orderedTM = "";
	}

	return orderedTM;
}

function submitForm(buttonClicked)
{
	var params = buildParams();
    if (buttonClicked=="search")
    {
    	if(verifyBeforeSearch()){
	  		searchForm.action = "<%=action%>" + "<%=stateNormal%>" + params;
		} else {
	  		alert("<%=bundle.getString("jsmsg_tm_select_one") %>");
	  		return;
		}
    }
    else if (buttonClicked=="prev")
    {
        searchForm.action = "<%=action%>" + "<%=stateShowPrev%>" + params;
    }
    else if (buttonClicked=="next")
    {
        searchForm.action = "<%=action%>" + "<%=stateShowNext%>" + params;
    }
    else if (buttonClicked=="refresh")
    {
        searchForm.action = "<%=action%>" + "<%=stateRefresh%>" + params;
    }
    searchForm.submit();
}

/**
 * Make sure at least one tm is selected.
 */
function verifyBeforeSearch() {
  var flag = false;
  var tms = document.getElementById('tmIndex');
  if( ! document.getElementById("rFullText").checked)
  {
      return true;
  }
  for(var i = 0; i < tms.options.length;i++){
    if(tms.options[i].selected){
      /* the option is selected ,return true*/
      flag = true;
      break;
    }
  }
  return flag;

}

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerText = message;
    idMessages.appendChild(div);

    if (idMessages.style.pixelHeight < 80)
    {
      idMessages.style.pixelHeight = 80;
    }

    idMessages.style.pixelHeight += 40;

    if (idMessages.style.pixelHeight > 200)
    {
      idMessages.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
      showProgressBar();
   if (percentage > 99)
   {
      hideProgressBar(percentage);
   }
   else
   {
      idProgress.innerText = percentage.toString(10) + "%";

      idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);

      if (message != null && message != "")
      {
        showMessage(message);
      }
   }
}

function hideProgressBar(percentage)
{
  idProgressContainer.style.top = -500;
  idProgressBar.style.top = -500;
  idProgress.style.top = -500;
  idMessagesHeader.style.top = -500;
  idMessages.style.top = -500;
}

function showProgressBar()
{

  idProgressContainer.style.top = 450;
  idProgressBar.style.top = 450;
  idProgress.style.top = 450;
  idMessagesHeader.style.top = 480;
  idMessages.style.top = 540;
}

function arrangeTm()
{

	var projectTmArray = new Array();
	<%
	for(int i = 0; i < tmNames.size(); i++)
	{
	%>
		var projectTm = new Object();
		projectTm.tmName = "<%=tmNames.get(i)%>";
		projectTmArray[projectTmArray.length] = projectTm;
	<%
	}
	%>
	var projectTmsSelectBox = searchForm.tmIndex;
	var selectedProjectTms = new Array();
	for(var i = 0; i < projectTmsSelectBox.length; i++)
	{
		var selectedProjectTm = projectTmsSelectBox.options[i];
		var mapOfTmIdAndTmName = new Object();
		if(selectedProjectTm.selected)
		{
			mapOfTmIdAndTmName.tmName = selectedProjectTm.innerHTML;
			selectedProjectTms[selectedProjectTms.length] = mapOfTmIdAndTmName;
		}
	}
	if(selectedProjectTms.length <= 0)
	{
		alert("<%=bundle.getString("msg_tm_select_tms_first") %>");
		return;
	}
	var dialogWidth = getWidth(selectedProjectTms);
	arrangeTm.dialogWidth = dialogWidth;

	showConfigDialog(selectedProjectTms, projectTmArray);
	showConfigDialog.projectTmArray = projectTmArray;
	showConfigDialog.selectedProjectTms = selectedProjectTms;
	var unSelectedProjectTms = getUnSelectedProjectTms(projectTmArray, selectedProjectTms);
	showConfigDialog.unSelectedProjectTms = unSelectedProjectTms;
	onlyChooseThis.index = 0;
	
}

function getWidth(selectedProjectTms)
{
	var width = 0; 
	var maxTmNameLength = selectedProjectTms[0].tmName.length;
	for(var i = 0; i < selectedProjectTms.length; i++){
		var projectTm = selectedProjectTms[i];
		var tmNameLength = projectTm.tmName.length;
		if(tmNameLength > maxTmNameLength)
		{
			maxTmNameLength = tmNameLength;
		}
	}	
	var offsetWidth = (maxTmNameLength - 15)*150/15;
	width = (offsetWidth > 0) ? offsetWidth : 0;
	return width;
}

function getUnSelectedProjectTms(projectTmArray, selectedProjectTms)
{
	var unSelectedProjectTms = new Array();
	for(var i = 0; i < projectTmArray.length; i++)
	{
		var projectTm = projectTmArray[i];
		
		for(var j = 0; j < selectedProjectTms.length; j++)
		{
			var selectedProjectTm = selectedProjectTms[j];
			if(ATrim(projectTm.tmName) == ATrim(selectedProjectTm.tmName))
			{
				projectTmArray[i] = "0";
			}
		}
	}
	for(var i = 0; i < projectTmArray.length; i++)
	{
		if(projectTmArray[i] != "0")
		{
			unSelectedProjectTms[unSelectedProjectTms.length] = projectTmArray[i];
		}
	}
	return unSelectedProjectTms;
}

function buildOptions(selectedProjectTms, selectedIndex, notChooseMulti)
{
	var popupContentInnerHtml = "";
	for(var i = 0; i < selectedProjectTms.length; i++)
	{	
		var isSelected;
		var tm = selectedProjectTms[i];
		if(i == selectedIndex)
		{
			isSelected = "selected";
		}
		else
		{
			isSelected = "";
		}
		
		if(selectedIndex == -1)
		{
		//all the options in "selectedProjectTms" will not be selected.
			isSelected = "";
		}
		if(selectedIndex == -2)
		{
		//all the options in "selectedProjectTms" will be selected.
			isSelected = "selected";
		}
		
		var noMulti;
		if(notChooseMulti)
		{
			noMulti = "";
		}
		else
		{
			noMulti = "";
		}
		popupContentInnerHtml += "<option value='" + i + "'" + isSelected + " " + noMulti + ">" + tm.tmName + "</option>";
		
	}
	return popupContentInnerHtml;
}

function onlyChooseThis(currentOption, event)
{

	var selectIndex = currentOption.selectedIndex;
	var allSelectIndexes = getAllSelectedIndexes(currentOption);
	var arrangeTmSelectBox = document.getElementById("arrangeTmSelect");
	for(var i = 0; i < arrangeTmSelectBox.options.length; i++)
	{
		arrangeTmSelectBox.options[i].selected = false;	
	}
	var index = -1;
	
	if(onlyChooseThis.index == allSelectIndexes[0])
	{
		index = allSelectIndexes[allSelectIndexes.length - 1];
	}
	if(onlyChooseThis.index == allSelectIndexes[allSelectIndexes.length - 1])
	{
		index = allSelectIndexes[0];
	}
	arrangeTmSelectBox.options[index].selected = true;
	onlyChooseThis.index = index;
}

function buildSelectBoxByArray(/*array*/ selectedProjectTms, selectedIndex)
{
	var wide = 150 + arrangeTm.dialogWidth;
	var popupContentInnerHtml = "<select id='arrangeTmSelect' onkeydown='onlyChooseThis(this)'  style='float:left;margin-left:10px;margin-top:2px;width:" + wide + "px;height:70px' multiple>";
	popupContentInnerHtml += buildOptions(selectedProjectTms, selectedIndex);
	popupContentInnerHtml += "</select>";
	return popupContentInnerHtml;
}

function showConfigDialog(selectedProjectTms, projectTmArray)
{
	var popupContentInnerHtml = "<span id='arrangeTmSelectBoxContent'>";
	popupContentInnerHtml += buildSelectBoxByArray(selectedProjectTms, 0);
	popupContentInnerHtml += "</span>";
	popupContentInnerHtml += "<div style='float:left;width:20px'>"
	popupContentInnerHtml += "<img src='/globalsight/images/sort-up(big).gif' style='margin-bottom:43px' onclick='sortUp()'></img>";
	popupContentInnerHtml += "<img src='/globalsight/images/sort-down(big).gif' onclick='sortDown()'></img>";
	popupContentInnerHtml += "</div>"
	document.getElementById("popupContent").innerHTML = popupContentInnerHtml;
	showArrangeTmDialog();
}

function check()
{
	var selectedCount = 0;
	var arrangeSelectBoxObj = document.getElementById("arrangeTmSelect");
	for(var i = 0; i < arrangeSelectBoxObj.options.length; i++)
	{
		var arrangeSelectBoxOption = arrangeSelectBoxObj.options[i];
		if(arrangeSelectBoxOption.selected)
		{
			selectedCount ++;
		}
	}
	if(selectedCount != 1)
	{
		return false;
	}
	else
	{
		return true;
	}
}

function sortUp()
{
	if(! check())
	{
		alert("<%=bundle.getString("msg_tm_select_one_sort_up") %>");
		return;
	}
	var arrangeSelectBoxObj = document.getElementById("arrangeTmSelect");
	var currentSelectedIndex = getCurrentSelectedIndex(arrangeSelectBoxObj);
	if(currentSelectedIndex <= 0)
	{
		//alert("The option you have selected is the top option, can not be sort up!");
		return;
	}
	
	sortUpSelectedProjectTms(currentSelectedIndex);
	currentSelectedIndex--;
	
	var arrangeTmSelectBoxContentInnerHtml = buildSelectBoxByArray(showConfigDialog.selectedProjectTms, currentSelectedIndex);
	var arrangeTmSelectBoxContentObj = document.getElementById("arrangeTmSelectBoxContent");
	arrangeTmSelectBoxContentObj.innerHTML = arrangeTmSelectBoxContentInnerHtml;
	onlyChooseThis.index = currentSelectedIndex;
}

function sortUpSelectedProjectTms(currentSelectedIndex)
{
	var tmpProjectTm = showConfigDialog.selectedProjectTms[currentSelectedIndex - 1];
	showConfigDialog.selectedProjectTms[currentSelectedIndex - 1] = showConfigDialog.selectedProjectTms[currentSelectedIndex];
	showConfigDialog.selectedProjectTms[currentSelectedIndex] = tmpProjectTm;
}

function sortDownSelectedProjectTms(currentSelectedIndex)
{
	var tmpProjectTm = showConfigDialog.selectedProjectTms[currentSelectedIndex + 1];
	showConfigDialog.selectedProjectTms[currentSelectedIndex + 1] = showConfigDialog.selectedProjectTms[currentSelectedIndex];
	showConfigDialog.selectedProjectTms[currentSelectedIndex] = tmpProjectTm;
}

function getAllSelectedIndexes(/*SelectBox*/selectBoxObj)
{
	var allIndexes = new Array();
	for(var i = 0; i < selectBoxObj.options.length; i++)
	{
		if(selectBoxObj.options[i].selected)
		{
			allIndexes.push(i);
		}
	}
	return allIndexes;
}

function getCurrentSelectedIndex(/*selectBoxObj*/ arrangeSelectBoxObj)
{
	for(var i = 0; i < arrangeSelectBoxObj.options.length; i++)
	{
		if(arrangeSelectBoxObj.options[i].selected)
		{
			return i;
		}
	}
	return -1;
}

function sortDown()
{
	if(! check())
	{
		alert("<%=bundle.getString("msg_tm_select_one_sort_down") %>");
		return;
	}
	var arrangeSelectBoxObj = document.getElementById("arrangeTmSelect");
	var currentSelectedIndex = getCurrentSelectedIndex(arrangeSelectBoxObj);
	
	if(currentSelectedIndex >= arrangeSelectBoxObj.options.length - 1)
	{
		//alert("The option you have selected is the bottom option, can not be sort down!");
		return;
	}
	sortDownSelectedProjectTms(currentSelectedIndex);
	currentSelectedIndex ++;
	
	var arrangeTmSelectBoxContentInnerHtml = buildSelectBoxByArray(showConfigDialog.selectedProjectTms, currentSelectedIndex);
	var arrangeTmSelectBoxContentObj = document.getElementById("arrangeTmSelectBoxContent");
	arrangeTmSelectBoxContentObj.innerHTML = arrangeTmSelectBoxContentInnerHtml;
	onlyChooseThis.index = currentSelectedIndex;
}

function isIndexInArray(indexOfSelectBox, indexArray)
{
	for(var i = 0; i < indexArray.length; i++)
	{
		if(indexOfSelectBox == indexArray[i])
		{
			return true;
		}
	}
	return false;
}

function showArrangeTmDialog()
{
	var arrangeTmDialogObj = document.getElementById("arrangeTmDialog");
	arrangeTmDialogObj.style.width = 300 + arrangeTm.dialogWidth;
	document.getElementById("div_button_arrangeTm").style.marginLeft = (300 + arrangeTm.dialogWidth - 100) / 2;
	arrangeTmDialogObj.style.display = "block";
}

function closePopupDialog()
{
	document.getElementById("arrangeTmDialog").style.display = "none";
}

function arrangeTms()
{
	<%
		String leverageTMSelectBoxInnerHtml = "<SELECT NAME='tmIndex' id='tmIndex' size='4' MULTIPLE>";
	%>
		var leverageTMSelectBoxInnerHtml = "<%=leverageTMSelectBoxInnerHtml%>";
		leverageTMSelectBoxInnerHtml += buildOptions(showConfigDialog.selectedProjectTms, -2);
		leverageTMSelectBoxInnerHtml += buildOptions(showConfigDialog.unSelectedProjectTms, -1);
		leverageTMSelectBoxInnerHtml += "</select>";
		document.getElementById("tmIndexSelectContent").innerHTML = leverageTMSelectBoxInnerHtml;
		closePopupDialog();
}


</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="10" RIGHTMARGIN="10" TOPMARGIN="10" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnload()" onunload="doOnUnload()">
       <div id='arrangeTmDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:180px;position:absolute;top:100px;z-index:100'>
		<div style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%'>
			<label class='whiteBold'><%=bundle.getString("lb_tm_arrange_tm_order") %></label>
		</div>
		<div id='popupContent' style='margin:20px;margin-top:20px;margin-bottom:40px;margin-left:40px'>
			<!-- Generate By Program -->
			<label style='float:left;margin-left:50px;margin-top:2px'><%=bundle.getString("lb_tm_name") %>:</label>
			<select id='position' style='width:100px'>
				<option value='tmId'><%=bundle.getString("lb_tm_index") %></option>
			</select>	
			
        </div>
			<div id="div_button_arrangeTm" style="float:left;margin-left:100px;margin-top:20px">
			<center><input type='submit' value='<%=bundle.getString("lb_ok") %>' onclick='arrangeTms()'/>
			<input id='exit' style='margin-left:5px' type='submit' value='<%=bundle.getString("lb_cancel") %>' onclick='closePopupDialog()'/>
			</center>
			</div>
		</div>
 
<CENTER>
<BR>
<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<SPAN class="help" onclick="helpSwitch()" style="float:right"><%=lb_help%></SPAN>

<H3><%=bundle.getString("lb_corpus_browser")%></H3>
<% if (fromEditor) { %>
<%=bundle.getString("lb_source_locale")%>: <%=request.getAttribute("sourceLocale")%><BR>
<%=bundle.getString("lb_target_locale")%>: <%=request.getAttribute("targetLocale")%><BR>
<%}%>
<FORM NAME="searchForm" METHOD="POST"
 ACTION="/globalsight/ControlServlet?activityName=browseCorpus&pagename=CTMB" >
<INPUT TYPE="HIDDEN" NAME="fromEditor" VALUE="<%=fromEditor%>">
<BR>
<% 
String radio1 = "";
String radio2 = "";
String radio3 = "";
String checked = "CHECKED";

if ("fuzzySearch".equals(searchType))
    radio2 = checked;
else if ("docNameSearch".equals(searchType))
    radio3 = checked;
else
    radio1 = checked;
%>
<BR>
<DIV id="idMessagesHeader" class="header"><%=lb_search_msg%>:</DIV>
<DIV id="idMessages"></DIV>
<INPUT TYPE="RADIO" NAME="searchType" VALUE="fullTextSearch" <%=radio1%>
 onclick="setupFullTextSearch()" id="rFullText">
<label for="rFullText"><%=bundle.getString("lb_corpus_searchFT")%></label>
<% if (!fromEditor || (fromEditor && showAllTms)) {
   //do not enable fuzzy/exact for translators searching unless they can search on all TMs
%>
<INPUT TYPE="RADIO" NAME="searchType" VALUE="fuzzySearch" <%=radio2%>
 onclick="setupFuzzySearch()" id="rFuzzy">
<label for="rFuzzy"><%=bundle.getString("lb_corpus_searchFZ")%></label>
<% } %>
<INPUT TYPE="RADIO" NAME="searchType" VALUE="docNameSearch" <%=radio3%>
 onclick="setupDocNameSearch()" id="rDocument">
<label for="rDocument"><%=bundle.getString("lb_corpus_searchDN")%></label>

<BR>
<LABEL for="queryText" id="labelQueryText"><%=bundle.getString("lb_conc_search_text")%></LABEL>
<% if (!isFresh) { %>
<INPUT NAME="queryText" TYPE="TEXT" VALUE="<%=queryText%>" onkeydown="if (event.keyCode==13){return false;}">
<% } else { %>
<INPUT NAME="queryText" TYPE="TEXT" VALUE="" onkeydown="if (event.keyCode==13){return false;}">
<% } %>
<BR>
<% if (!fromEditor) { 
    //user must select a source locale and target locale then
%>

<DIV ID="divLocalePair">
<BR>
<LABEL for="localePair" id="labelLocalePair"><%=bundle.getString("lb_locale_pair")%>:</LABEL>
<SELECT NAME="localePair" ONCHANGE="populateLeverage()">
<% 
Iterator iter = pairs.iterator();
while (iter != null && iter.hasNext()) {
	LocalePair lp = (LocalePair) iter.next();
    String isSelected = "";
    if (localePair != null && localePair.getId() == lp.getId())
        isSelected = " SELECTED ";
    %>
    <OPTION ID="<%=lp.getTarget().getLanguageCode()%>"  VALUE="<%=lp.getId()%>" <%=isSelected%>>
    <%=lp.getSource().getDisplayName(uiLocale)%> --> <%=lp.getTarget().getDisplayName(uiLocale)%>
    </OPTION>
<% } %>
</SELECT>
<%}/*endif*/%>
</DIV>
<DIV ID="divTmIndex">
<BR>
<table>
<tr>
<td>
<table>
<tr>
<td>
<LABEL for="tmIndex"><%=bundle.getString("lb_conc_tm")%>:</LABEL>
</td>
</tr>
<tr>
<td>
<input type="button" id="changeTmPosition" value="<%=bundle.getString("lb_tm_arrange_order") %>" onclick="arrangeTm()"/>
</td>
</tr>
</table>
</td>
<td>
<span id="tmIndexSelectContent">
<SELECT NAME="tmIndex" id = "tmIndex" size=4 MULTIPLE>
<% 
Set<String> tmSet = new HashSet<String>();
for (int j=0; j < tmNames.size(); j++) { 
    String isSelected = "";
    String tmName = (String)tmNames.get(j);
    if (tmSet.contains(tmName)) {
    	continue;
    }
    tmSet.add(tmName);
    if (!"fuzzySearch".equals(searchType)
     && !"fullTextSearch".equals(searchType))
    {
        if (j==tmIndex)
            isSelected = " SELECTED ";
    }
%>
    <OPTION VALUE="<%=j%>" <%=isSelected%>> <%=tmName%></OPTION>
<% } %>
</SELECT>
</span>
</td>
</table>

</DIV>
<DIV ID="divFuzzy">
<BR><LABEL for="tmProfileId"><%=bundle.getString("lb_corpus_tmprofile")%>:</LABEL>
<SELECT ID="tmProfile" NAME="tmProfileId">
<% 
ArrayList tmProfiles = (ArrayList) request.getAttribute("tmProfiles");
Long tmProfileId = (Long) request.getAttribute("tmProfileId");
for (int i=0; i < tmProfiles.size(); i++) { 
    TranslationMemoryProfile tmp = (TranslationMemoryProfile) tmProfiles.get(i);
    String isSelected = "";
    if (tmp.getIdAsLong().equals(tmProfileId))
        isSelected = " SELECTED ";
%>
<OPTION VALUE="<%=tmp.getId()%>" <%=isSelected%>><%=tmp.getName()%></OPTION>
<% } %>
</SELECT>

<BR>
<span id="fuzzythreshold">
<LABEL for="fuzzyOverride"><%=bundle.getString("lb_corpus_fuzzyover")%>:</LABEL>
<SELECT NAME="fuzzyOverride">
<% for (int i=0;i < 96; i=i+5) {
    String isSelected="";
    if ((fuzzyOverride == null && i==75) || 
        (fuzzyOverride != null && fuzzyOverride.intValue()==i))
        isSelected=" SELECTED ";
%>
<OPTION VALUE="<%=i%>" <%=isSelected%>><%=i%>%</OPTION>
<% } %>
<% for (int i=96;i < 101; i=i+1) {%>
<OPTION VALUE="<%=i%>"><%=i%>%</OPTION>
<% } %>
</SELECT>
</span>
<BR>
<%
  javax.servlet.jsp.JspWriter m_out = null;
  out.flush();
  m_out = out;

  // Initiate server-side search and respond to status events.
  if(m_status != null && "fullTextSearch".equals(searchType))
  {
      m_out.print("<SCRIPT>");
      m_out.print("showProgress(" + counter + "," + percentage + ",'");
      if(message != null)
      {
          m_out.print(EditUtil.toJavascript(message));
      }
      m_out.println("');");
      m_out.print("</SCRIPT>");
      m_out.flush();
  }
%>
<LABEL for="chosenLeverages"><%=bundle.getString("lb_target_cross_locale_leverage")%>:</LABEL>
<SELECT NAME="chosenLeverages" CLASS="standardText" MULTIPLE size=4>
</SELECT>
<BR>
</DIV>
<BR>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_search")%>" onclick="submitForm('search')">&nbsp;&nbsp;&nbsp;<INPUT TYPE="BUTTON" ONCLICK="window.close()" VALUE="<%=bundle.getString("lb_close")%>">
<% if(!isFresh && percentage < 100) { %>
<INPUT TYPE="BUTTON" NAME="REFRESH" VALUE="<%=bundle.getString("lb_refresh")%>" onclick="submitForm('refresh')"> 
<%}%>
</FORM>
</CENTER>
<HR>
  <% if (!isFresh) { %>
    <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0>
  <% } %>
  <% if(percentage > 99)
  {
    if ("fullTextSearch".equals(searchType) && !isFresh && tableRows != null &&
      tableRows.length() > 1 ) {
       if (max != 0 ) { %>
    <TR><TD>       
      <!-- Top Utility bar -->
     <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" BGCOLOR="#DFDFDF">
      <TR>
       <TD HEIGHT=30>
       <TD ALIGN="center">
         <P id="statusMessageTop" CLASS="standardText">
         <IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idLeft"
         <% if (min > 0) { %>
         class="clickable" onclick="submitForm('prev');"
         <%}%>
         >
         <%=min + 1%>-<%=max%> of <%=numRecords%>
         <IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idRight"
         <% if (max < numRecords) { %>
         class="clickable" onclick="submitForm('next');"
         <%}%>
         >
         </P>
       </TD>
      </TR>
     </TABLE>
      <!-- End Top Utility bar -->
     </TD></TR>
     <%}%>
    <TR><TD>
 <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0"
    STYLE="border: solid 1px black">
  <THEAD>
  <COL VALIGN="top"
       ALIGN="left" STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
  <COL VALIGN="top"> <!-- Source -->
  <COL VALIGN="top"> <!-- Target -->
  <TR CLASS="tableHeadingBasic">
   <TD HEIGHT="20"><%=bundle.getString("lb_corpus_context")%></TD>
   <TD HEIGHT="20"><%=lbSource%> <%=sourceLocale.getDisplayName()%></TD>
   <TD HEIGHT="20"><%=lbTarget%> <%=targetLocale.getDisplayName()%></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_tm_name") %></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_sid") %></TD>
  </TR>
  </THEAD>
  <TBODY>
  <%= tableRows %>
  </TBODY>
  </TABLE>
     </TD></TR>
<% } else if ("docNameSearch".equals(searchType) && !isFresh && corpusDocs!= null && corpusDocs.size() > 0) {
    %>
    <TR><TD>
 <TABLE CELLPADDING="1" CELLSPACING="0" BORDER="0"
    STYLE="border: solid 1px black">
  <THEAD>
  <COL VALIGN="top"
       ALIGN="left" STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
  <COL VALIGN="top"> <!-- Source -->
  <COL VALIGN="top"> <!-- Target -->
  <TR CLASS="tableHeadingBasic">
   <TD HEIGHT="20"><%=bundle.getString("lb_locale")%></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_corpus_doc_name")%></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_actions")%></TD>
  </TR>
  </THEAD>
  <TBODY>
  <%
    Iterator iter = corpusDocs.iterator();
    int rownum = 0;
    String rowColor = null;
    while (iter.hasNext())
    {
        CorpusDoc doc = (CorpusDoc) iter.next();
        CorpusDocGroup group = doc.getCorpusDocGroup();
        rowColor = (rownum++ % 2 == 0) ? "#FFFFFF" : "#DDDDDD";
        %>
        <TR BGCOLOR="<%=rowColor%>">        
        <TD CLASS="standardText"><%=doc.getLocale()%>&nbsp;&nbsp;</TD>
        <TD CLASS="standardText"><%=group.getCorpusName()%>&nbsp;&nbsp;</TD>
        <TD CLASS="standardText">
        
        <%
        String nativeFormatPath = doc.getNativeFormatPath();
        String fileStoragePath = AmbFileStoragePathUtils.getFileStorageDirPath();
        String fullPath = fileStoragePath + nativeFormatPath;
        java.io.File file = new java.io.File(fullPath);
        if (file.exists()) 
           {
        %>
        <A HREF="#" onclick="showCorpusDoc('<%=URLEncoder.encodeUrlStr(doc.getNativeFormatPath())%>')"><%=bundle.getString("lb_corpus_action_vnf")%></A>        	
        <% } else {  %>
        <%=bundle.getString("lb_corpus_action_vnf")%>
        <% } 
        
        String gxmlPath = doc.getGxmlPath();
        fullPath = fileStoragePath + gxmlPath;
        file = new java.io.File(fullPath);
        if (file.exists()) 
           {
        %>
        --<A HREF="#" onclick="showCorpusDocText('<%=URLEncoder.encodeUrlStr(doc.getGxmlPath())%>','<%=group.getCorpusName()%>')">
        <%=bundle.getString("lb_corpus_action_vt")%></A>
        <% } else {  %>
        --<%=bundle.getString("lb_corpus_action_vt")%>
        <% } %>
       
        <% if (!fromEditor) { %>
        --<A HREF="#" NAME="del<%=doc.getId()%>" id="del<%=doc.getId()%>" onclick="deleteCorpusDoc(<%=doc.getId()%>,this)">
        <%=bundle.getString("lb_remove")%></A>
        <% } %>
        </TD>
        </TR>
        <%
    }
    %>
    </TBODY>
    </TABLE>
     </TD></TR>
<% } else if ("fuzzySearch".equals(searchType) && !isFresh && leverageDataCenter != null ) {
    %>
     <TR><TD>
 <TABLE CELLPADDING="1" CELLSPACING="0" BORDER="0"
    STYLE="border: solid 1px black">
  <THEAD>
  <COL VALIGN="top"
       ALIGN="left" STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
  <COL VALIGN="top"> <!-- Source -->
  <COL VALIGN="top"> <!-- Target -->
  <TR CLASS="tableHeadingBasic">
  
   <TD HEIGHT="20"><%=bundle.getString("lb_corpus_context")%></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_percentage")%>&nbsp;&nbsp;</TD>   
   <TD HEIGHT="20"><%=lbSource%> <%=sourceLocale.getDisplayName()%></TD>
   <TD HEIGHT="20"><%=lbTarget%> <%=targetLocale.getDisplayName()%></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_sid") %></TD>
   <TD HEIGHT="20"><%=bundle.getString("lb_tm_name") %></TD>

  </TR>
  </THEAD>
  <TBODY>
  <%
   int rownum = 0;
   String rowColor = null;
   Iterator itLeverageMatches = leverageDataCenter.leverageResultIterator();
   TableMaker tableMaker = new TableMaker(true);
   while(itLeverageMatches.hasNext())
   {
       LeverageMatches levMatches = (LeverageMatches)itLeverageMatches.next();
       // walk through all target locales in the LeverageMatches
       Iterator itLocales = levMatches.targetLocaleIterator(-1);
       while(itLocales.hasNext())
       {
           GlobalSightLocale tLocale = (GlobalSightLocale)itLocales.next();
           // walk through all matches in the locale
           Iterator itMatch = levMatches.matchIterator(tLocale, -1);
           while(itMatch.hasNext())
           {
               rowColor = (rownum++ % 2 == 0) ? "#FFFFFF" : "#DDDDDD";
               LeveragedTuv matchedTuv = (LeveragedTuv)itMatch.next();
               String sourceContent = matchedTuv.getSourceTuv().getSegment();
               String targetContent = matchedTuv.getSegment();
               long tuvId = matchedTuv.getId();
               Long srcLocaleId = matchedTuv.getSourceTuv().getLocale().getIdAsLong();
               float score = matchedTuv.getScore();
               
               String sid = matchedTuv.getSid();
               
               if (null == sid) {
            	   sid = "N/A"; 
               }
               
        %>
        <TR BGCOLOR="<%=rowColor%>"> 
        <TD CLASS="clickable" STYLE="padding-top: 2px"><img src="/globalsight/images/corpus_icon.jpg" onclick="showCorpus(<%=tuvId%>,<%=srcLocaleId%>)"></img></TD>
        <TD CLASS="standardText"><%=StringUtil.formatPCT(score)%>&nbsp;&nbsp;</TD>
        <TD CLASS="results\">
        <TABLE CLASS="standardText">
        <%=tableMaker.getFormattedCell(EditUtil.encodeHtmlEntities(queryText), false, matchedTuv.getSourceTuv())%>
        </TABLE>
        </TD>
        <TD CLASS="results\">
        <TABLE CLASS="standardText">
        <%=tableMaker.getFormattedCell(null, false, matchedTuv)%>        
        </TABLE>
        </TD>
        <TD CLASS="standardText">
        <%= sid %>&nbsp;&nbsp;
        </TD>
		<TD CLASS="results\">
        <TABLE CLASS="standardText">
        <%=tableMaker.makeTMName(matchedTuv.getTu().getTmId(), (HashMap)request.getAttribute("mapTmIdName"))%>        
        </TABLE>
        </TD>

        <% } //endwhile%>
        </TD>
        </TR>
<%        
       }//endwhile
   }//endwhile
%>
    </TBODY>
    </TABLE>    
     </TD></TR>
<% }  else if (!isFresh){ %>
     <TR><TD>
    <EM><%=bundle.getString("msg_search_results_nothing_found")%></EM>
     </TD></TR>
<% } %>
<%}%>

</BODY>
</HTML>

