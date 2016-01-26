<%@page import="com.globalsight.cxe.entity.customAttribute.TMAttributeCons"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.foundation.LocalePair,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.localemgr.CodeSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.projecthandler.ProjectTM,
            com.globalsight.everest.util.comparator.TmComparator,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.SortUtil,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl,
            com.globalsight.everest.util.comparator.SegmentationRuleFileComparator,
            com.globalsight.everest.permission.Permission,
            java.util.List,
            java.util.Locale,
            java.util.HashMap,
            java.util.Vector,
            java.util.Iterator,
            java.util.Enumeration,
            java.util.ResourceBundle,
            java.util.Collections,
            java.util.ArrayList"
    session="true" %>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
   ResourceBundle bundle = PageHandler.getBundle(session);
   SessionManager sessionMgr = 
       (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

   Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

   String nameField = TMProfileConstants.NAME_FIELD;
   String descriptionField = TMProfileConstants.DESCRIPTION_FIELD;
   String projectTmIdToSave = TMProfileConstants.PROJECT_TM_ID_TO_SAVE;
   String isSaveToProjectTm = TMProfileConstants.SAVE_UNLOC_SEGS_TO_PROJECT_TM;
   String isSaveAllUnlocToProjectTm = TMProfileConstants.SAVE_ALL_UNLOC_SEGS_TO_PROJECT_TM;
   String isSaveUnlocToProjectTm = TMProfileConstants.SAVE_UN_LOC_SEGS_TO_PROJECT_TM;
   String isSaveLocToProjectTm = TMProfileConstants.SAVE_LOC_SEGS_TO_PROJECT_TM;
   String isSaveApprovedToProjectTm = TMProfileConstants.SAVE_APPROVED_SEGS_TO_PROJECT_TM;
   String isSaveExactMatchToProjectTm = TMProfileConstants.SAVE_EXACT_MATCH_SEGS_TO_PROJECT_TM;
   String isSaveToPageTm = TMProfileConstants.SAVE_UNLOC_SEGS_TO_PAGE_TM;
   String isSaveWhollyInternalTextTm = TMProfileConstants.SAVE_WHOLLY_INTERNAL_TEXT_TM;
   String leverageExcludeType = TMProfileConstants.LEVERAGE_EXCLUDE_TYPES;
   String levLocalizable      = TMProfileConstants.LEVERAGE_LOCALIZABLES;
   String levExactMatches =  TMProfileConstants.LEVERAGE_EXACT_MATCH_ONLY;
   String leveragePTM = TMProfileConstants.LEVERAGE_FROM_PROJECT_TM;
   String typeSensitiveLeveraging = TMProfileConstants.TYPE_SENSITIVE_LEVERAGING;
   String typeDiffPenalty = TMProfileConstants.TYPE_DIFFERENCE_PENALTY;
   String caseSensitiveLeveraging = TMProfileConstants.CASE_SENSITIVE_LEVERAGING;
   String caseDiffPenalty = TMProfileConstants.CASE_DIFFERENCE_PENALTY;
   String whitespaceSensitiveLeveraging = TMProfileConstants.WHITESPACE_SENSITIVE_LEVERAGING;
   String whiteDiffPenalty = TMProfileConstants.WHITESPACE_DIFFERENCE_PENALTY;
   String codeSensitiveLeveraging = TMProfileConstants.CODE_SENSITIVE_LEVERAGING;
   String codeDiffPenalty = TMProfileConstants.CODE_DIFFERENCE_PENALTY;
   String multiLingualLeveraging = TMProfileConstants.MULTILINGUAL_LEVERAGING;
   String autoRepair = TMProfileConstants.AUTO_REPAIR;
   String multEM = TMProfileConstants.MULTIPLE_EXACT_MATCHES;
   String multDiffPenalty = TMProfileConstants.MULTIPLE_EXACT_MATCH_PENALTY;
   String fuzzyMatchThreshold = TMProfileConstants.FUZZY_MATCH_THRESHOLD;
   String numberOfMatches = TMProfileConstants.MATCHES_RETURNED;
   String latestMatchForReimport = TMProfileConstants.LATEST_MATCH_FOR_REIMPORT;
   String typeSensitiveLeveragingReimport = TMProfileConstants.TYPE_SENSITIVE_LEVERAGING_REIMPORT;
   String typeDiffPenaltyReimport = TMProfileConstants.TYPE_DIFFERENCE_PENALTY_REIMPORT;
   String multLGEM = TMProfileConstants.MULTIPLE_EXACT_MATCHES_REIMPORT;
   String multMatchesPenaltyReimport = TMProfileConstants.MULTIPLE_EXACT_MATCHES_PENALTY_REIMPORT;
   String dynLevGold = TMProfileConstants.DYN_LEV_GOLD;
   String dynLevStopSearch = TMProfileConstants.DYN_LEV_STOP_SEARCH;
   String dynLevInProgress = TMProfileConstants.DYN_LEV_IN_PROGRESS;
   String dynLevPopulation = TMProfileConstants.DYN_LEV_POPULATION;
   String dynLevReference = TMProfileConstants.DYN_LEV_REFERENCE;
   String excludeItemTypesLongList = TMProfileConstants.EXCLUDE_ITEM_TYPES_LONG_LIST;
   String selectedSR = TMProfileConstants.SELECTED_SR;
   String matchingPercentage = TMProfileConstants.MATCH_PERCENTAGE; 
   String tmProcendence = TMProfileConstants.TM_PROCENDENCE;
   
   //labels
   String labelName = bundle.getString("lb_name");
   String labelDescription = bundle.getString("lb_description");
   String basicInfo = bundle.getString("msg_basic_info");
   String lbCreateTmProfile = bundle.getString("msg_create_tmprofile");
   String lbtmPopulationOptions = bundle.getString("msg_tm_population_options");
   String lbsaveToProjectTmId = bundle.getString("msg_save_to_project_tm");
   String lbisSaveToProjectTm = bundle.getString("msg_is_save_to_project_tm");
   String lbsaveAllUnlSegToTM = bundle.getString("msg_save_all_unlocalized_sgments_to_tm");
   String lbonlySaveApprovedSegToTM = bundle.getString("msg_save_approved_segments_to_tm_only");
   String lbsaveUnlSegToTm =  bundle.getString("msg_save_unlocalized_segments_to_tm");
   String lbsavelocSegToTm =  bundle.getString("msg_save_localized_segments_to_tm");
   String lbsaveApprovedSegToTM = bundle.getString("msg_save_approved_segments_to_tm");
   String lbisSaveExactMatchToProjectTm = bundle.getString("msg_is_save_exact_match_to_project_tm");
   String lbisSaveToPageTm = bundle.getString("msg_is_save_to_page_tm");
   String lbisSaveWhollyInternalTextTm = bundle.getString("msg_is_save_wholly_internal_text_tm");
   String lbgeneralLeverageOptions = bundle.getString("msg_general_leverage_options");
   String lbexcludeItemTypes = bundle.getString("msg_exclude_item_types");
   String lblevLocalizable = bundle.getString("msg_lev_localizable");
   String lblevExactMatches = bundle.getString("msg_lev_exact_matches");
   String levContextMatches = bundle.getString("msg_lev_context_matches");
   String lblevOptionsFreshImport = bundle.getString("msg_lev_options_fresh_import");
   String lblevProjectTm = bundle.getString("msg_lev_project_tm");
   String lbtypeSensitiveLeveraging = bundle.getString("msg_type_sensitive_leveraging");
   String lbcaseSensitiveLeveraging = bundle.getString("msg_case_sensitive_leveraging");
   String lbwsSensitiveLeveraging = bundle.getString("msg_ws_sensitive_leveraging");
   String lbcodeSensitiveLeveraging = bundle.getString("msg_code_sensitive_leveraging");
   String lbmultLingLeveraging = bundle.getString("msg_multilingual_leveraging");
   String lbautoRepair = bundle.getString("msg_auto_repair");
   String lbmultExactMatches = bundle.getString("msg_mult_exact_matches");
   String lblatest = bundle.getString("msg_latest");
   String lboldest = bundle.getString("msg_oldest");
   String lbdemoted = bundle.getString("msg_demoted");
   String lbpenalty = bundle.getString("msg_penalty");
   String lbfuzzyMatches = bundle.getString("lb_leverage_match_threshold");
   String lbnumMatchesReturned = bundle.getString("msg_num_matches_returned");
   String lbreimportOptions = bundle.getString("msg_reimport_options");
   String lbchooseLatestMatch = bundle.getString("msg_choose_latest_match");
   String lbnoMultExacts = bundle.getString("msg_no_multiple_exact_matches");
   String latestValue = TranslationMemoryProfile.LATEST_EXACT_MATCH;
   String oldestValue = TranslationMemoryProfile.OLDEST_EXACT_MATCH;
   String demotedValue = TranslationMemoryProfile.DEMOTED_EXACT_MATCH;
   String jsmsg = "";
   String msgDuplicateName = bundle.getString("msg_duplicate_tm_profile_name");
   String lbDynLevOptions = bundle.getString("lb_dynLevOptions");
   String lbDynLevGoldTm = bundle.getString("lb_dynLevGoldTm");
   String lbDynLevStopSearch = bundle.getString("lb_dynLevStopSearch");
   String lbDynLevInProgressTm = bundle.getString("lb_dynLevInProgressTm");
   String lbDynLevPopulationTm = bundle.getString("lb_dynLevPopulationTm");
   String lbDynLevReferenceTm = bundle.getString("lb_dynLevReferenceTm");
   String lbRelatedSRX = bundle.getString("msg_relate_with_srx");

   Long tmProfileId = (Long)request.getAttribute(TMProfileConstants.TM_PROFILE_ID);
   
   boolean isAdmin = (Boolean)sessionMgr.getAttribute("isAdmin");
   boolean isSuperAdmin = (Boolean)sessionMgr.getAttribute("isSuperAdmin");
   boolean enableTMAccessControl = (Boolean)sessionMgr.getAttribute("enableTMAccessControl");
   List tmsOfUser = (List)sessionMgr.getAttribute("tmsOfUser");
   
   List projectTms = null;
   int cnt=0;
   if(enableTMAccessControl)
   {
       //TM Access Control is enable
       if(isAdmin||isSuperAdmin)
       {
           try
           {
                projectTms = new ArrayList(ServerProxy.getProjectHandler().
                                         getAllProjectTMs());
           }
           catch(Exception e)
           {
               throw new EnvoyServletException(e);
           }
       }
       else
       {
           projectTms = tmsOfUser;
       } 
   }
   else
   {
       //TM Access Control is disable
       try
       {
            projectTms = new ArrayList(ServerProxy.getProjectHandler().
                                     getAllProjectTMs());
       }
       catch(Exception e)
       {
           throw new EnvoyServletException(e);
       }
   }
   
   TmComparator tmComp = new TmComparator(TmComparator.NAME, uiLocale);
   SortUtil.sort(projectTms,tmComp);
   
   List segmentationRules = null;
   try
   {
	   segmentationRules = new ArrayList(ServerProxy
				.getSegmentationRuleFilePersistenceManager()
				.getAllSegmentationRuleFiles());
   }
   catch(Exception e)
   {
       throw new EnvoyServletException(e);
   }
   SegmentationRuleFileComparator srComp = 
	   new SegmentationRuleFileComparator(SegmentationRuleFileComparator.NAME, uiLocale);
   SortUtil.sort(segmentationRules, srComp);

   List tmProfiles =  null;
   try
   {
       tmProfiles = new ArrayList(ServerProxy.getProjectHandler().getAllTMProfiles());
   }
   catch (Exception e)
   {
       throw new EnvoyServletException(e);
   }
   if (tmProfiles != null)
       {
           for(int i=0; i<tmProfiles.size(); i++)
           {
              TranslationMemoryProfile tmProfile = (TranslationMemoryProfile)tmProfiles.get(i);
              jsmsg += "if(basicTMProfileForm." + nameField + ".value == \"" + tmProfile.getName() + "\")\n" +
              "   {\n" +
              "      alert('" + msgDuplicateName + "');\n" +
              "      return false;\n" +
              "   }\n";
           }
       }
   // links for the next and cancel buttons
   String saveURL = save.getPageURL() + (tmProfileId == null ? "" :
                     ("&" + TMProfileConstants.TM_PROFILE_ID + "="
                     + tmProfileId));
   String cancelURL = cancel.getPageURL() + "&"
                     + TMProfileConstants.ACTION
                     + "=" + TMProfileConstants.CANCEL_ACTION;
   // Titles
   String newTitle = bundle.getString("msg_tm_profile_title1");
   String lbCancel = bundle.getString("lb_cancel");
   String lbSave = bundle.getString("lb_save");

   String tmpAvailableAtts = (String)request.getAttribute(WebAppConstants.TMP_AVAILABLE_ATTS);
   String tmpAtts = (String)request.getAttribute(WebAppConstants.TMP_TMP_ATTS);
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= newTitle %></TITLE>
<link rel="stylesheet" type="text/css" href="/globalsight/envoy/tm/management/tm.css"/>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<script SRC="/globalsight/includes/dojo.js"></script>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<!-- for jQuery -->
<link rel="stylesheet" type="text/css" href="/globalsight/includes/jquery-ui-custom.css"/>
<script src="/globalsight/jquery/jquery-1.6.4.min.js" type="text/javascript"></script>
<script src="/globalsight/includes/jquery-ui-custom.min.js" type="text/javascript"></script>
<script src="/globalsight/includes/Array.js" type="text/javascript"></script>
<script src="/globalsight/includes/filter/StringBuffer.js" type="text/javascript"></script>
<script src="/globalsight/envoy/administration/tmprofile/TMPAttribute.js" type="text/javascript"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_tmProfile") %>";
var guideNode = "tmProfiles";
var helpFile = "<%=bundle.getString("help_tmprofile_create_modify")%>";

var names = new Array();
function Result(message, errorFlag, element)
{
    this.message = message;
    this.error   = errorFlag;
    this.element = element;
}

var strAvailableAttnames = "<%=tmpAvailableAtts %>";
var strTMPAtts = "<%=tmpAtts %>";
var maxOrder = 0;

var arrayAvailableAttnames = new Array();
var arrayTMPAtts = new Array();
var msgAlertDigit = "<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>";

if (strAvailableAttnames != null)
{
	arrayAvailableAttnames = strAvailableAttnames.split(",");
}

if (strTMPAtts != null)
{
	var temparray = strTMPAtts.split(",");
	for (var i = 0; i < temparray.length; i++)
	{
		var ttt = temparray[i].split(":");
		var tmpAtt = new Object();
		tmpAtt.itemid = newId();
		tmpAtt.attributename = ttt[0];
		tmpAtt.operator = ttt[1];
		tmpAtt.valueType = ttt[2];
		tmpAtt.valueData = ttt[3];
		tmpAtt.order = ttt[4];
		tmpAtt.andOr = ttt[5];
		
		arrayTMPAtts[arrayTMPAtts.length] = tmpAtt;
		if (maxOrder < tmpAtt.order)
		{
			maxOrder = tmpAtt.order;
		}
	}
}

function checkRefTms(refTms,selectTm){
    refTms = "," + refTms;
	for (var loop2 = 0;loop2 < selectTm.options.length; loop2++)
    {
         if (selectTm.options[loop2].selected == true)
         {
             var value = "," + selectTm.options[loop2].value + ",";
             if(refTms.indexOf(value) == -1)
             {
            	  return false;
			 }
         }
	}
	return true;
}

function buildDefinition()
{
    var name = Trim(basicTMProfileForm.<%=TMProfileConstants.NAME_FIELD%>.value);
    if (name == "")
    {
      return new Result("Please enter a name.", 1,
        basicTMProfileForm.<%=TMProfileConstants.NAME_FIELD%>);
    }

    return new Result('', 0, null);
}
function checkForDuplicateName()
{
   <%=jsmsg%>
   return true;
}
function submitForm(formAction)
{
   basicTMProfileForm.formAction.value = formAction;
   if (formAction == "cancel")
   {
      if (confirmJump())
       {
           basicTMProfileForm.submit();
       }
       else
       {
          return false;
       }

   }
   else if (formAction == "save")
   {
      if(!checkForDuplicateName())
       {
          return false;
       }

      if (confirmForm(basicTMProfileForm))
       {
         var result = buildDefinition();
         if (result.error == 0)
           {
              basicTMProfileForm.<%=WebAppConstants.TM_TM_NAME%>.disabled = false;
              // Prepare the leveragedLocales param
              var options_string = "";
              var options_string1 = "";
              var the_select = basicTMProfileForm.leveragePTM;
              for (loop=0; loop < the_select.options.length; loop++)
               {
                if (the_select.options[loop].selected == true)
                {
                   options_string += the_select.options[loop].value + ",";
                   options_string1 += loop + ",";
                }
               }
              basicTMProfileForm.leveragedProjects.value = options_string;
	          basicTMProfileForm.action += "&indexes=" + options_string1;
	          
			  var options_string2 = "";
              var options_string3 = "";
              if(basicTMProfileForm.isRefTm.checked==true){
	              var the_select2 = basicTMProfileForm.selectleveragedRefProjects;
	              for (var loop2=0; loop2 < the_select2.options.length; loop2++)
	              {
	                if (the_select2.options[loop2].selected == true)
	                {
	                   options_string2 += the_select2.options[loop2].value + ",";
	                   options_string3 += loop + ",";
	                }
	              }
			      
			      basicTMProfileForm.leveragedRefProjects.value = options_string2;
          
        	      if(!checkRefTms(options_string,the_select2)){
    			     alert("<%=bundle.getString("msg_tm_reference_tm_above") %>");
    			     return false;
    	          }
             }
		  
		  // attributes
		  setTMPAttributes();
		  
          basicTMProfileForm.submit();
         }
         else
         {
            if (result.element != null)
            {
              result.element.focus();
            }
            alert(result.message);
         }
      }
      else
       {
          return false;
       }
   }
}

function checkIsVaildPercent(percent){
    var submit = false;
	var i_percent = parseInt(percent);
	if(i_percent > 100 || i_percent < 0){
		alert("<%=bundle.getString("msg_tm_number_scope_0_100") %>");
		submit = false;
	}else{
		submit = true;
	}
	return submit;
}

function checkIsVaildPercent2(percent){
    var submit = false;
	var i_percent = parseInt(percent);
	if(i_percent > 100 || i_percent < 1){
		alert("<%=bundle.getString("msg_tm_number_scope_1_100") %>");
		submit = false;
	}else{
		submit = true;
	}
	return submit;
}

function confirmForm(formSent) {

    var theName = formSent.<%=TMProfileConstants.NAME_FIELD%>.value;
	theName = stripBlanks (theName);

	if (isEmptyString(formSent.<%=TMProfileConstants.NAME_FIELD%>.value))
    {
		alert("<%= bundle.getString("jsmsg_tm_profile_name") %>");
		formSent.<%=TMProfileConstants.NAME_FIELD%>.value = "";
		formSent.<%=TMProfileConstants.NAME_FIELD%>.focus();
		return false;
	}
    if (hasSpecialChars(theName))
    {
        alert("<%= labelName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    if (!isNotLongerThan(formSent.<%=TMProfileConstants.DESCRIPTION_FIELD%>.value, 256)) 
    {
		alert("<%= bundle.getString("jsmsg_description") %>");
		formSent.<%=TMProfileConstants.DESCRIPTION_FIELD%>.focus();
		return false;
	}

    if (!isSelectionMade(formSent.<%=TMProfileConstants.SELECTED_SR%>))
    {
		alert("<%= bundle.getString("jsmsg_tm_profiles_sr") %>");
		return false;
	}
    if (!isSelectionMade(formSent.<%=TMProfileConstants.PROJECT_TM_ID_TO_SAVE%>))
    {
		alert("<%= bundle.getString("jsmsg_tm_profiles_project") %>");
		return false;
	}
    if (formSent.<%=TMProfileConstants.LEVERAGE_FROM_PROJECT_TM%>.selectedIndex < 0)
    {
		alert("<%= bundle.getString("jsmsg_tm_profiles_leverage_ptm") %>");
		return false;
	}

    if (isEmptyString(formSent.multDiffPenalty.value))
    {
       alert("<%= bundle.getString("jsmsg_tm_mult_diff_penalty") %>");
       return false;
    }
    if (!isAllDigits(formSent.multDiffPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (isEmptyString(formSent.fuzzyMatchThreshold.value))
    {
       alert("<%= bundle.getString("jsmsg_tm_fuzzy_match_threshold") %>");
       return false;
    }
    if (!isAllDigits(formSent.fuzzyMatchThreshold.value))
    {
       alert("<%=lbfuzzyMatches%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (isEmptyString(formSent.numberOfMatches.value))
    {
       alert("<%= bundle.getString("jsmsg_tm_num_of_matches") %>");
       return false;
    }
    if (!isAllDigits(formSent.numberOfMatches.value))
    {
       alert("<%=lbnumMatchesReturned%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.typeDiffPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.caseDiffPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.whiteDiffPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.codeDiffPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.typeDiffPenaltyReimport.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (!isAllDigits(formSent.multMatchesPenaltyReimport.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
	if (!isAllDigits(formSent.refTmPenalty.value))
    {
       alert("<%=lbpenalty%>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
    if (formSent.tuAttNotMatchPenalty && !isAllDigits(formSent.tuAttNotMatchPenalty.value))
    {
       alert("<%= bundle.getString("msg_tu_attribute_penalty") %>" + "<%= bundle.getString("jsmsg_numeric") %>");
       return false;
    }
	
	//check penalty between 0 and 100;whiteDiffPenalty
	if (!checkIsVaildPercent(formSent.refTmPenalty.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.multDiffPenalty.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.fuzzyMatchThreshold.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.numberOfMatches.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.typeDiffPenalty.value)){
	   return false;
	} 
	if (!checkIsVaildPercent(formSent.caseDiffPenalty.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.whiteDiffPenalty.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.codeDiffPenalty.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.typeDiffPenaltyReimport.value)){
	   return false;
	}
	if (!checkIsVaildPercent(formSent.multMatchesPenaltyReimport.value)){
	   return false;
	}
	//check penalty between 1 and 100
	if (formSent.tuAttNotMatchPenalty 
			&& !checkIsVaildPercent2(formSent.tuAttNotMatchPenalty.value))
	{
	   return false;
	}
	
    return true;
}

function dynCheckCtrl()
{
    var disable = new Boolean();
    if(basicTMProfileForm.<%=dynLevInProgress%>.checked == true)
    {
        disable = false;
    }
    else
    {
        disable = true;
    }

    basicTMProfileForm.<%=dynLevPopulation%>.disabled = disable;
    basicTMProfileForm.<%=dynLevReference%>.disabled = disable;
}

function arrangeTm()
{
	var projectTmArray = new Array();
	<%
	for(int i = 0; i < projectTms.size(); i++)
	{
	%>
		var projectTm = new Object();
		projectTm.tmId = "<%=((ProjectTM)projectTms.get(i)).getId()%>";
		projectTm.tmName = "<%=((ProjectTM)projectTms.get(i)).getName()%>";
		projectTmArray[projectTmArray.length] = projectTm;
	<%
	}
	%>
	var projectTmsSelectBox = basicTMProfileForm.leveragePTM;
	var selectedProjectTms = new Array();
	for(var i = 0; i < projectTmsSelectBox.length; i++)
	{
		var selectedProjectTm = projectTmsSelectBox.options[i];
		var mapOfTmIdAndTmName = new Object();
		if(selectedProjectTm.selected)
		{
			mapOfTmIdAndTmName.tmId = selectedProjectTm.value;
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
			if(projectTm.tmId == selectedProjectTm.tmId)
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
		popupContentInnerHtml += "<option value='" + tm.tmId + "'" + isSelected + " " + noMulti + ">" + tm.tmName + "</option>";
		
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

function checkSelectedCount()
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
	if(! checkSelectedCount())
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
	if(! checkSelectedCount())
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

function checkSameIndex()
{
	var flag = false;
	var projectTmArray = showConfigDialog.projectTmArray;
	var indexArray = new Array();
	for(var i = 0; i < projectTmArray.length; i++)
	{
		var tm = projectTmArray[i];
		var selectBoxObj = document.getElementById(tm.tmId);
		var indexOfSelectBox = selectBoxObj.options[selectBoxObj.selectedIndex].value;
		
		if(isIndexInArray(indexOfSelectBox, indexArray))
		{
			flag = true;
			break;
		}
		indexArray[indexArray.length] = indexOfSelectBox;
	}
	return flag;
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
		String leverageTMSelectBoxInnerHtml = "<SELECT NAME='" + leveragePTM +"' CLASS=standardText MULTIPLE SIZE=4>";
	%>
		var leverageTMSelectBoxInnerHtml = "<%=leverageTMSelectBoxInnerHtml%>";
		leverageTMSelectBoxInnerHtml += buildOptions(showConfigDialog.selectedProjectTms, -2);
		leverageTMSelectBoxInnerHtml += buildOptions(showConfigDialog.unSelectedProjectTms, -1);
		leverageTMSelectBoxInnerHtml += "</select>";
		document.getElementById("leverageTms").innerHTML = leverageTMSelectBoxInnerHtml;
		closePopupDialog();
}

function check(obj)
{
	document.getElementById("percentage").checked = false;
	document.getElementById("procendence").checked = false;
	obj.checked = true;
}

function checkLeverageMatchOption(obj)
{
	var exact = document.getElementById("idIsLevEMChecked");
	var incontext = document.getElementById("idLevContextMatches");
	var icePromotionRule1 = document.getElementById("idIcePromotionRules1");
	var icePromotionRule2 = document.getElementById("idIcePromotionRules2");
	var icePromotionRule3 = document.getElementById("idIcePromotionRules3");	

	exact.checked = false;
	incontext.checked = false;
	icePromotionRule1.disabled = true;
	icePromotionRule2.disabled = true;
	icePromotionRule3.disabled = true;

	obj.checked = true;
	if (incontext.checked == true)
	{
		icePromotionRule1.disabled = false;
		icePromotionRule2.disabled = false;
		icePromotionRule3.disabled = false;
	}
}

function doOnLoad()
{
	loadGuides();
	initAttbutesUI();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doOnLoad()">
   <%@ include file="/envoy/common/header.jspIncl" %>

   <%@ include file="/envoy/common/navigation.jspIncl" %>

   <%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute; WIDTH: 880px; TOP: 108px">
    <SPAN CLASS="mainHeading">
      <%= lbCreateTmProfile%>
    </SPAN>

    <FORM NAME="basicTMProfileForm" ACTION= "<%=saveURL%>" METHOD="post">
        <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
        <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
            <TR ALIGN="LEFT" VALIGN="TOP">
                <TD VALIGN="TOP" WIDTH="420">
                    <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
                        <TR>
                            <TD><B><%=basicInfo%></B>
                                <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
                                    <TR>
                                        <TD><%=labelName%>
                                            <SPAN CLASS="asterisk">
                                                *
                                            </SPAN>
                                            :</TD>
                                        <TD><INPUT CLASS="standardText" MAXLENGTH="25" NAME="<%=nameField%>"></TD>
                                    </TR>
                                    <TR>
                                        <TD valign="top"><%=labelDescription%>:</TD>
                                        <TD><TEXTAREA CLASS="standardText" NAME="<%=descriptionField%>"ROWS="3" COLS="40"></TEXTAREA></TD>
                                    </TR>
                                </TABLE>
                                <BR><BR>
                            </TD>
                        </TR>
                        <TR>
                        <TD><B><%=lbRelatedSRX%>
                            <SPAN CLASS="asterisk">
                                *
                            </SPAN>
                            </B>
                            <SELECT NAME="<%=selectedSR%>" CLASS="standardText">
                                <OPTION VALUE="-1" SELECTED><%=bundle.getString("lb_choose_1")%>
                     <%
                       Iterator it_s = segmentationRules.iterator();
                       while (it_s.hasNext())
                       {
                           SegmentationRuleFileImpl sr = (SegmentationRuleFileImpl)it_s.next();
                           String srName = sr.getName() + (sr.getIsDefault() ? " (Default)" : "");
                           long id  = sr.getId();
                           %> <OPTION VALUE = "<%=id%>" ><%=srName%><%
                       }%>

                                </SELECT>
                                <BR><BR>
                            </TD>
                        </TR>
                        <TR>
                            <TD><B><%=lbtmPopulationOptions%></b>
                                <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
                                    <TR ALIGN="LEFT">
                                        <TD><%=lbsaveToProjectTmId%>
                                            <SPAN CLASS="asterisk">*</SPAN>:
                                         </TD>
                                         <TD>
                                            <SELECT NAME="<%=projectTmIdToSave%>" CLASS="standardText">
                                                <OPTION VALUE="-1" SELECTED><%=bundle.getString("lb_choose_1")%>
                                 <%
                                   if(projectTms!=null)
                                   {
                                       Iterator it = projectTms.iterator();
                                       while (it.hasNext())
                                       {
                                           ProjectTM projTm = (ProjectTM)it.next();
                                           if (projTm.getIsRemoteTm()==false)
                                           {
                                               String projName = projTm.getName();
                                               long id  = projTm.getId();
                                     %> 
                                           <OPTION VALUE = "<%=id%>"><%=projName%>
                                     <%    }
                                       }
                                   }%>
                                           </SELECT>
                                        </TD>
                                    </TR>
                                    <TR ALIGN="LEFT">
                                        <TD COLSPAN=2>
                                        	<INPUT TYPE="checkbox"  NAME="<%=isSaveUnlocToProjectTm%>" VALUE="true"><%=lbsaveUnlSegToTm%>
                                        </TD>
                                    </TR>
                                    <TR ALIGN="LEFT">
		                               <TD COLSPAN=2>
			                               <INPUT TYPE="checkbox" NAME="<%=isSaveLocToProjectTm%>" VALUE="true" CHECKED><%=lbsavelocSegToTm%>
			                           </TD>
			                        </TR>
                                    <TR ALIGN="LEFT">
                                        <TD COLSPAN=2><INPUT TYPE="checkbox" NAME="<%=isSaveWhollyInternalTextTm%>" VALUE="true"><%=lbisSaveWhollyInternalTextTm%></TD>
                                    </TR>
                                    <TR ALIGN="LEFT">
                                        <TD COLSPAN=2><INPUT TYPE="checkbox" NAME="<%=isSaveExactMatchToProjectTm%>" VALUE="true" CHECKED><%=lbisSaveExactMatchToProjectTm%></TD>
                                    </TR>
			                        <TR ALIGN="LEFT">
			                           <TD COLSPAN=2>
			                           <INPUT TYPE="checkbox" NAME="<%=isSaveApprovedToProjectTm%>"  VALUE="true" CHECKED><%=lbsaveApprovedSegToTM%>
			                           </TD>
			                        </TR>
                                    <TR ALIGN="LEFT">
                                        <TD COLSPAN=2><INPUT TYPE="checkbox" NAME="<%=isSaveToPageTm%>" VALUE="true" CHECKED><%=lbisSaveToPageTm%></TD>
                                    </TR>
                                </TABLE>
                                <BR>
                                <BR>
                            </TD>
                        </TR>
                        <TR>
                            <TD>
                                <b><%=lbgeneralLeverageOptions%></b><BR>
                                <%=lbexcludeItemTypes%>:<BR>
                                <TEXTAREA CLASS="standardText" NAME="<%=leverageExcludeType%>" ROWS="5" COLS="50"><%=excludeItemTypesLongList%></TEXTAREA>
                                <BR>
                                <INPUT TYPE="checkbox" NAME="<%=levLocalizable%>" VALUE="true" CHECKED><%=lblevLocalizable%>
                                <BR>
                                <INPUT id="idIsLevEMChecked" onclick="checkLeverageMatchOption(this);" TYPE="radio" NAME="<%=levExactMatches%>" VALUE="true"><%=lblevExactMatches%>
                                <BR>
                                <INPUT id="idLevContextMatches" onclick="checkLeverageMatchOption(this);" type="radio" name="<%=levContextMatches %>" value ="true" checked><%=levContextMatches%>
                                <BR>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<INPUT TYPE="radio" id="idIcePromotionRules1" NAME="icePromotionRules" VALUE="1"><%=bundle.getString("lb_apply_ice_promotion_rule1")%><BR>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<INPUT TYPE="radio" id="idIcePromotionRules2" NAME="icePromotionRules" VALUE="2"><%=bundle.getString("lb_apply_ice_promotion_rule2")%><BR>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<INPUT TYPE="radio" id="idIcePromotionRules3" NAME="icePromotionRules" VALUE="3" CHECKED><%=bundle.getString("lb_apply_ice_promotion_rule3")%><BR>
                                <BR/>
                            </TD>
                        </TR>
                        <TR><TD>
                               <B><%=lbDynLevOptions%></B>
                        </TD></TR>
                        <TR><TD><!--For "(AMB-108) TM memory profiles to have "Latest" as the default for Multiple Exact Matches" issue-->
                               <INPUT TYPE="checkbox" NAME="<%=dynLevGold%>" VALUE="true" checked/><%=lbDynLevGoldTm%>
                        </TD></TR>
                        <TR><TD>
                               <INPUT TYPE="checkbox" NAME="<%=dynLevInProgress%>" VALUE="true"  onClick="dynCheckCtrl();" CHECKED><%=lbDynLevInProgressTm%>
                        </TD></TR>
                        <TR><TD>
                               &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                               <INPUT TYPE="checkbox" NAME="<%=dynLevPopulation%>" VALUE="true"><%=lbDynLevPopulationTm%>
                        </TD></TR>
                        <TR><TD>
                               &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                               <INPUT TYPE="checkbox" NAME="<%=dynLevReference%>" VALUE="true"><%=lbDynLevReferenceTm%>
                            </TD>
                        </TR>
                        <TR><TD>
                               <INPUT TYPE="checkbox" NAME="<%=dynLevStopSearch%>" VALUE="true"><%=lbDynLevStopSearch%>
                        </TD></TR>
                    </TABLE>
                </TD>
                <TD STYLE="padding: 4px"></TD>
                <TD STYLE="padding: 4px" width="440"><b><%=lblevOptionsFreshImport%></b>
                    <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" valign="top">
                            <%=lblevProjectTm%><SPAN CLASS="asterisk">
                                                *
                                            </SPAN>
                                            :
                             <br/><input type="button" id="changeTmPosition" value="<%=bundle.getString("lb_tm_arrange_order") %>" onclick="arrangeTm()"/>
                             </TD>
                            <TD><INPUT TYPE="HIDDEN" NAME="leveragedProjects" VALUE="">
                            <span id="leverageTms">
                                <SELECT NAME="<%=leveragePTM%>" CLASS="standardText" MULTIPLE size=4>
                                 <%
                                    if(projectTms!=null)
                                    {
                                        Iterator it1 = projectTms.iterator();
                                        while (it1.hasNext())
                                        {
                                            ProjectTM projTm = (ProjectTM)it1.next();
                                            long id = projTm.getId();
                                            String projName = projTm.getName();
                                            %><OPTION VALUE = "<%=id%>"><%=projName%></OPTION>
                                            <%
                                        }
                                    }%>
                                    
							</span>
                                </SELECT>
                            </TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbtypeSensitiveLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox"
                                       NAME="<%=typeSensitiveLeveraging%>"
                                       VALUE="true" CHECKED>
                                                          
                                <SPAN ID="penaltyTypeSensitive">
                                    <%=lbpenalty%>: <INPUT NAME="<%=typeDiffPenalty%>" SIZE="1" MAXLENGTH="3" VALUE="1">%
                                </SPAN>
                             </TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbcaseSensitiveLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox"
                                       NAME="<%=caseSensitiveLeveraging%>"
                                       VALUE="true" CHECKED>
                                <SPAN ID="penaltyCaseSensitive">
                                    <%=lbpenalty%>: <INPUT NAME="<%=caseDiffPenalty%>" SIZE="1" MAXLENGTH="3" VALUE="1">%
                                </SPAN>
                            </TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbwsSensitiveLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%=whitespaceSensitiveLeveraging%>"
                                       VALUE="true" CHECKED>
                                                       
                                <SPAN ID="penaltyWhiteSpaceSensitive">
                                    <%=lbpenalty%>: <INPUT NAME="<%=whiteDiffPenalty%>" SIZE="1" MAXLENGTH="3" VALUE="1">%
                                </SPAN>
                             </TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbcodeSensitiveLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%=codeSensitiveLeveraging%>"
                              VALUE="true" CHECKED>
                              
                                <SPAN ID="penaltyCodeSensitive">
                                    <%=lbpenalty%>: <INPUT NAME="<%=codeDiffPenalty%>" SIZE="1" MAXLENGTH="3" VALUE="1">%
                                </SPAN>
                             </TD>
                        </TR>
						
						
						<TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=bundle.getString("lb_tm_reference_tm") %>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%="isRefTm"%>"
                              VALUE="true" CHECKED>
                              
                                <SPAN ID="penaltyCodeSensitive">
                                    <%=lbpenalty%>: <INPUT NAME="<%="refTmPenalty"%>" SIZE="1" MAXLENGTH="3"      VALUE="1">%
                                </SPAN>
                             </TD>
					    </TR>
						<TR>
                             <TD></TD> 
							 <TD><INPUT TYPE="HIDDEN" NAME="leveragedRefProjects" VALUE="">
                                <SELECT NAME="selectleveragedRefProjects" CLASS="standardText" MULTIPLE size=4>
                                 <%
                                    if(projectTms!=null)
                                    {
                                        Iterator it2 = projectTms.iterator();
                                        while (it2.hasNext())
                                        {
                                            ProjectTM projTm = (ProjectTM)it2.next();
                                            long id = projTm.getId();
                                            String projName = projTm.getName();
                                            %><OPTION VALUE = "<%=id%>"><%=projName%>
                                            <%
                                         }
                                    }%>

                                </SELECT>
                              </TD>
                        </TR>

                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbmultLingLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME=
                              "<%=multiLingualLeveraging%>" VALUE="true"></TD>
                        </TR>
                        <TR>
                           <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                              <%=lbautoRepair%>:
                           </TD>
                           <TD>
                              <INPUT TYPE="checkbox" NAME="<%=autoRepair%>" VALUE="true" checked>
                           </TD>
                        </TR>
                        <TR>
                           <TD ALIGN="LEFT" STYLE="vertical-align: middle">Get Unique from Multiple Exact Matches:</TD>
                           <TD><INPUT TYPE="checkbox" NAME="uniqueFromMultTrans" VALUE="true"/></TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" valign="top">
                            <%=lbmultExactMatches%>:</TD><!--For "(AMB-108) TM memory profiles to have "Latest" as the default for Multiple Exact Matches" issue-->
                            <TD><INPUT TYPE="radio" NAME="<%=multEM%>" VALUE="<%=latestValue%>" checked/><%=lblatest%>
                                <BR/>
                                <INPUT TYPE="radio" NAME="<%=multEM%>" VALUE="<%=oldestValue%>"/><%=lboldest%><BR>
                                <INPUT TYPE="radio" NAME="<%=multEM%>" VALUE="<%=demotedValue%>"/><%=lbdemoted%>
                                <BR/>
                                <%=lbpenalty%>: <INPUT NAME="<%=multDiffPenalty%>" SIZE="1" MAXLENGTH="3" VALUE="1">%</TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbfuzzyMatches%>:</TD>
                            <TD><INPUT NAME="<%=fuzzyMatchThreshold%>" SIZE="1" MAXLENGTH="3" VALUE="75">%</TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbnumMatchesReturned%>:</TD>
                            <TD><INPUT NAME="<%=numberOfMatches%>" SIZE="1" VALUE="5"></TD>
                        </TR>
                        <tr>
	                        <td  ALIGN="LEFT" STYLE="vertical-align: middle">
	                        	<%=bundle.getString("lb_tm_display_tm_natches_by") %>:
	                        </td>
	                        <td>
	                        	<input id="percentage" type="radio" onclick="check(this)" name="<%=matchingPercentage %>" value="true" checked><%=bundle.getString("lb_matching_percentage")%></input>
	                        	<br/>
	                        	<input id="procendence" type="radio" onclick="check(this)" name="<%=tmProcendence %>" value="true"><%=bundle.getString("lb_tm_precedence")%></input>
	                        </td>
                        </tr>
                        
                    </TABLE>
                    <BR><BR>
                    <b><%=lbreimportOptions%></b>
                    <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: left">
                            <%=lbchooseLatestMatch%></TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%=latestMatchForReimport%>" VALUE="true" checked></TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbtypeSensitiveLeveraging%>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%=typeSensitiveLeveragingReimport%>"
                              VALUE="true" CHECKED>
                              
                                <SPAN ID="penaltyTypeSensitiveReimp">
                                    <%=lbpenalty%>: <INPUT NAME="<%=typeDiffPenaltyReimport%>" SIZE="1" MAXLENGTH="3" VALUE="1">%</TD>
                        </TR>
                        <TR ALIGN="LEFT">
                            <TD ALIGN="LEFT" STYLE="vertical-align: middle">
                            <%=lbnoMultExacts%>:</TD>
                            <TD><INPUT TYPE="checkbox" NAME="<%=multLGEM%>" VALUE="true" CHECKED>
                              <SPAN ID="penaltyMultipleMatches">
                                    <%=lbpenalty%>: <INPUT NAME="<%=multMatchesPenaltyReimport%>" SIZE="1" MAXLENGTH="3" VALUE="1">%</TD>
                        </TR>
                    </TABLE>

        <amb:permission name="<%=Permission.TM_ENABLE_TM_ATTRIBUTES%>" >
            <BR><BR>
            <b><%=bundle.getString("lb_attribute_rules") %></b><a id="tuvAttSub" name="tuvAttSub">&nbsp;</a>
            <div id="divAtts" class="standardText" style="width:100%"></div><br/>
			<span class="standardText">
			    <select id="andOr">
			    	<option value="and">and</option>
			    	<option value="or">or</option>
			    </select>
			</span>
            <span CLASS="standardText">
            	<select id="attname"></select>
			</span>
			<span CLASS="standardText">
			<select id="operator">
				<option value="<%=TMAttributeCons.OP_EQUAL%>"><%=TMAttributeCons.OP_EQUAL%></option>
				<option value="<%=TMAttributeCons.OP_CONTAIN%>"><%=TMAttributeCons.OP_CONTAIN%></option>
				<option value="<%=TMAttributeCons.OP_NOT_CONTAIN%>"><%=TMAttributeCons.OP_NOT_CONTAIN%></option>
			</select>
		</span>
		<span CLASS="standardText">
			<select id="valueType" onchange="onValueTypeChange()">
				<option value="<%=TMAttributeCons.VALUE_FROM_JOBATT %>"><%=bundle.getString("lb_value_from_job") %></option>
				<option value="<%=TMAttributeCons.VALUE_INPUT %>"><%=bundle.getString("lb_input_value") %></option>
			</select>
		</span>
		<span id="inputValueField" CLASS="standardText" style="display:none">
			<input type="text" id="valueData" size="8" maxlength="20" value="" />
		</span>
		<input type="BUTTON" id="addRow" value="Add" onclick="doAddAttribute()"/><br/><br/>

		<span class="standardText">
			<input type="radio" name="choiceIfAttNotMatched" value="disregard" /><%=bundle.getString("lb_disregard") %><br/>
			<input type="radio" name="choiceIfAttNotMatched" value="penalize" CHECKED /><%=bundle.getString("lb_penalize") %>
			&nbsp; Penalty:<input type="text" maxlength="3" size="1" name="tuAttNotMatchPenalty" value="1" />%
		</span>
		</amb:permission>

                    </TD>
            </TR>
        </TABLE>
        <input type="hidden" name="tmpAttributes" id="tmpAttributes" value="" />
        <P>
        <INPUT TYPE="button" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" ONCLICK="submitForm('cancel')">
        <INPUT TYPE="button" NAME="<%=lbSave%>" VALUE="<%=lbSave%>" ONCLICK="submitForm('save')">
        </P>
    </FORM>
</DIV>
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

</BODY>
</HTML>
