<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
			com.globalsight.util.GlobalSightLocale,
			com.globalsight.util.edit.EditUtil,
			com.globalsight.everest.servlet.util.ServerProxy,
			com.globalsight.ling.common.Text,
	        com.globalsight.everest.webapp.WebAppConstants"
	session="true"%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String saveUrl = self.getPageURL()+"&action="+WebAppConstants.TM_ACTION_SAVE_ENTRY;

    String sourceLocaleStr = (String) request.getAttribute("sourceLocale");
    String targetLocaleStr = (String) request.getAttribute("targetLocale");
    GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager().getLocaleByString(sourceLocaleStr);
    GlobalSightLocale targetLocale = ServerProxy.getLocaleManager().getLocaleByString(targetLocaleStr);
    String srcSegment =(String)request.getAttribute("srcSegment");
	String trgSegment =(String)request.getAttribute("trgSegment");

	String srcDir="LTR";
	if (EditUtil.isRTLLocale(sourceLocale) && Text.containsBidiChar(srcSegment))
	{
		srcDir = "RTL";
	}
    String trgDir="LTR";
	if (EditUtil.isRTLLocale(targetLocale) && Text.containsBidiChar(trgSegment))
	{
		trgDir = "RTL";
	}
%>
<html>
<head>
<title>Translation Memory - Edit Entry</title>
<style>
.table_left{
background: none repeat scroll 0 0 #7B8EB5;color: white;font-weight: bold;
}
.table_right{
background: none repeat scroll 0 0 #DEE3ED;color: #000000;
}
td.coolButton {
    font: menu;
    height: 16px;
    width: 16px;
}
.coolButton {
    border: 1px solid buttonface;
    cursor: default;
    padding: 1px;
    text-align: center;
}

.ptagDivPop {
margin-bottom: 3px;
display: none;
position: absolute;
background:#DEE3ED;
border:solid 1px #6e8bde;
}
#mask {
    display:none;
    z-index:9998;
    position:absolute;
    left:0px;
    top:0px;
    filter:Alpha(Opacity=30);
    /* IE */
    -moz-opacity:0.4;
    /* Moz + FF */
    opacity: 0.4;
}

.link {
    color: blue;
    cursor: pointer;
}
</style>

<script SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/spellcheck.js"></script>
<script SRC="/globalsight/spellchecker/jsp/spellcheck.js"></script>
<script SRC="/globalsight/includes/filter/StringBuffer.js"></script>
<script SRC="/globalsight/xdespellchecker/noapplet/SpellCheckNoApplet.js"></script>
<SCRIPT SRC="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/envoy/edit/online/stringbuilder.js"></script>
<script src="/globalsight/envoy/edit/online/richedit.js"></script>
<script src="/globalsight/envoy/edit/online/coolbuttons.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dnd/DragAndDrop.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript">
var srcDir = "<%=srcDir%>";
var trgDir = "<%=trgDir%>";
var entryInfo;
var tuId;
var sourceTuvId;
var targetTuvId;
var sourceLocale;
var targetLocale;
var tmId;
var textboxSource;
var textboxTarget;
var userId;
var uiLocale;
var ptagsSource;
var ptagsTarget;
var sourceInternals = new Array();
var targetInternals = new Array();
var controlChars = "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009\u000A\u000B\u000C\u000D\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F";
var hexChars = "0123456789ABCDEF";

function spellCheck(tLocale, edit)
{
	var g_SC_GSA = new SC_GSA_Parameters();
	var w_scwin = null;
	var sc_customDict = null;
	var sc_dict;
	if (!sc_customDict)
    {
       sc_dict = g_SC_GSA.getSystemDict(tLocale);
       sc_customDict = g_SC_GSA.getCustomDict(userId, tLocale);
    }

    w_scwin = scSpell(this, edit+'&typectrl=richedit', tLocale, uiLocale, sc_dict, sc_customDict);
}

function showPTags(type)
{
	var ptags;
	var textbox;
	if("source"==type)
	{
		ptags = ptagsSource;
		textbox = textboxSource;
	}
	else
	{
		ptags = ptagsTarget;
		textbox = textboxTarget;
	}
	var data = ptags.split(",");
  	if (ptags.length > 0 )
  	{
  		var bufTags = new Array();
  		bufTags.push("<table cellpadding=4 cellspacing=0>");
  		bufTags.push("<tr class='tableHeadingBasic'><td colspan=2 align='left'>Select PTag:</td></tr>");
    	for (var i = 0; i < data.length; i += 2)
    	{
    	  bufTags.push("<tr><td>");
      	  bufTags.push("<span class=\"link\" style=\"font-family:Courier; font-size:12;\" onclick=\"doPtagClick('"+type+"','"+data[i]+"')\">"+data[i]+"</span>");
      	  bufTags.push("</td>");
      	  if(i+1<data.length)
      	  {
      		bufTags.push("<td value='"+data[i+1]+"'>");
        	bufTags.push("<span class=\"link\" style=\"font-family:Courier; font-size:12;\" onclick=\"doPtagClick('"+type+"','"+data[i+1]+"')\">"+data[i+1]+"</span>");
        	bufTags.push("</td>");
      	  }
      	  else
      	  {
      		bufTags.push("<td></td>");
      	  }
      	  bufTags.push("</tr>");
    	}
    	bufTags.push("<tr><td colspan=2 align='center'><BUTTON ONCLICK=\"hideDiv();\">Close</BUTTON></td></tr>");
    	bufTags.push("</table>");
    	$("#selectPtagDiv").html(bufTags.join(""));
  	}
  	else
  	{
  		var buf = new Array();
  		buf.push("<table cellpadding=4 cellspacing=0>");
  		buf.push("<tr class='tableHeadingBasic'><td colspan=2 align='left'>Select PTag:</td></tr>");
  		buf.push("<tr><td>");
  		buf.push("No tags");
  		buf.push("</td>");
  		buf.push("</tr>");
  		buf.push("<tr><td colspan=2 align='center'><BUTTON ONCLICK=\"hideDiv();\">Close</BUTTON></td></tr>");
  		buf.push("</table>");
  		$("#selectPtagDiv").html(buf.join(""));
  	}
  	popupDiv(type);  	
}

function popupDiv(type)
{
  var winWidth = $(window).width();
  var winHeight = $(window).height();
  
  var leftL = 250;
  var topL = 150;
  if(!("source"==type))
  {
	  topL = 350;
  }
  
  $("#selectPtagDiv").css({"position": "absolute", "z-index": "9999", "left": leftL, "top": topL});
  $("#selectPtagDiv").show();
  $("#mask").width(winWidth).height(winHeight).show();
}

function hideDiv()
{
  $("#selectPtagDiv").hide();
  $("#mask").hide();
}	

function doPtagClick(type, tag)
{
  if("source"==type)
  {
	  InsertPTag(textboxSource, tag);
  }
  else
  {
	  InsertPTag(textboxTarget, tag);
  }
  hideDiv();
}
   		
function addLre(textbox)
{
  textbox.addLre();
  textbox.frameWindow.focus();
}

function addPdf(textbox)
{
  textbox.addPdf();
  textbox.frameWindow.focus();
}

function addCr(textbox)
{
  textbox.addCR();
  textbox.frameWindow.focus();
}

function InsertPTag(textbox, tag)
{
  textbox.insertPTag(tag);
}

function init()
{
	entryInfo = $.parseJSON('${entryInfo}');
	tuId = '${tuId}';
	sourceTuvId = '${sourceTuvId}';
	targetTuvId = '${targetTuvId}';
	sourceLocale = '${sourceLocale}';
	targetLocale = '${targetLocale}';
	tmId = '${tmId}';
	uiLocale = '${uiLocale}';
	userId = '${userId}';
	ptagsSource = entryInfo.ptagsSource;
	ptagsTarget = entryInfo.ptagsTarget;
	
	var sourceInternalsTemp = entryInfo.sourceInternals;
	if (sourceInternalsTemp.length > 0)
	{
		sourceInternals = sourceInternalsTemp.split("_g_s_");
	}
	
	var targetInternalsTemp = entryInfo.targetInternals;
	if (targetInternalsTemp.length > 0)
	{
		targetInternals = targetInternalsTemp.split("_g_s_");
	}
	
	$("#tmName").html(entryInfo.tmName);
	$("#createdBy").html(entryInfo.createdBy);	
	$("#createdOn").html(entryInfo.createdOn);	
	$("#modifiedBy").html(entryInfo.modifiedBy);	
	$("#modifiedOn").html(entryInfo.modifiedOn);	
	$("#sourceLocale").html(entryInfo.sourceLocale);	
	$("#targetLocale").html(entryInfo.targetLocale);
	$("#sid").val(entryInfo.sid);
	$("#jobId").html(entryInfo.jobId);	
	$("#jobName").html(entryInfo.jobName);
	$("#lastUsageDate").html(entryInfo.lastUsageDate);
	
	var tuAttributes =entryInfo.tuAttributes; 
	var bufTUAttributes = new Array();
	for(var i=0;i<tuAttributes.length;i++)
	{
		bufTUAttributes.push(tuAttributes[i]);
		if(i!=tuAttributes.length-1)
		{
			bufTUAttributes.push("<br>");
		}
	}
	$("#tuAttributes").html(bufTUAttributes.join(""));
	
	if(document.recalc)
	{
		var all = document.all;
	}
	else
	{
		var all = document.getElementsByTagName("*");
	}
    
    var l = all.length;
    for (var i = 0; i < l; i++)
    {
      var o = all[i];

      if (o.tagName != "INPUT" && o.tagName != "TEXTAREA" && o.tagName != "IFRAME" )
      {
        o.unselectable = "on";
      }
    }
	
	textboxSource = document.getElementById("editSource");
	textboxTarget = document.getElementById("editTarget");
	
	textboxSource.dir=srcDir;
	textboxTarget.dir=trgDir;
	
	setSegment(textboxSource, entryInfo.source);
	setSegment(textboxTarget, entryInfo.target);
	$("#idSourceSegment").html(entryInfo.source);
	$("#idTargetSegment").html(entryInfo.target);
	textboxTarget.focus();
}

function setSegment(textbox, s)
{
  initRichEdit(textbox);
  if(textbox)
  {
	  textbox.setHTML(s);
      textbox.focus();
  }
}


function saveEntry()
{
	var newSourceText = textboxSource.getText();
	var newTargetText = textboxTarget.getText();
	var originalSourceText = $("#idSourceSegment").text()
	var originalTargetText = $("#idTargetSegment").text()
	var sid = $("#sid").val();
	
	if(newSourceText=="")
	{
		alert("${msg_tm_search_add_source_null}");
		return;
	}
	
	if(newTargetText=="")
	{
		alert("${msg_tm_search_add_target_null}");
		return;
	}
	var sourceNoChanged = (originalSourceText==newSourceText);
	var targetNoChanged = (originalTargetText==newTargetText&&sid==entryInfo.sid);
	if(sourceNoChanged&&targetNoChanged)
	{
		alert("${msg_tm_search_no_changed}");
		self.close();
	}
	else
	{
		// check control chars
		for(var chari = 0; chari < controlChars.length; chari ++)
		{
			var charHere = controlChars.charAt(chari);
			var charCode = controlChars.charCodeAt(chari);
			
			var index = newSourceText.indexOf(charHere);
			var msg;
			
			var charCodeStr;
			if(charCode < 16)
			{
				charCodeStr = "000" + hexChars.charAt(charCode);
			}
			else if (charCode >= 16)
			{
				charCodeStr = "001" + hexChars.charAt(charCode - 16);
			}
			
			if (index != -1)
			{
				msg = "You are saving a string with a non-visible character in source segment. Character is u+" 
				+ charCodeStr + ", and is at column " + (index + 1) + " in the string.";
			}
			else
			{
			index = newTargetText.indexOf(charHere);
			if (index != -1)
			{
				msg = "You are saving a string with a non-visible character in target segment. Character is u+" 
				+ charCodeStr + ", and is at column " + (index + 1) + " in the string.";
			}
			}
			
			if (index != -1)
			{
				var isOkControlChar = confirm(msg);
				
				if (!isOkControlChar)
				{
					return;
				}
			}
		}
		
		// check internal tag
		if (sourceInternals.length > 0)
		{
			var missedInternals = new Array();
			for(var i = 0; i < sourceInternals.length; i++)
			{
				var internal = sourceInternals[i];
				
				if (newSourceText.indexOf(internal) == -1)
				{
					missedInternals[missedInternals.length] = internal;
				}
				
			}
			
			if (missedInternals.length > 0)
			{
				var confirmmsg = "${msg_internal_moved_continue}".replace(/target segment/g, "source segment") + "\r\n\r\n" + missedInternals.toString();
				var isOkInternal = confirm(confirmmsg);
				
				if (!isOkInternal)
				{
					return;
				}
			}
		}
		
		if (targetInternals.length > 0)
		{
			var missedInternals = new Array();
			for(var i = 0; i < targetInternals.length; i++)
			{
				var internal = targetInternals[i];
				
				if (newTargetText.indexOf(internal) == -1)
				{
					missedInternals[missedInternals.length] = internal;
				}
				
			}
			
			if (missedInternals.length > 0)
			{
				var confirmmsg = "${msg_internal_moved_continue}" + "\r\n\r\n" + missedInternals.toString();
				var isOkInternal = confirm(confirmmsg);
				
				if (!isOkInternal)
				{
					return;
				}
			}
		}
		
		var searchParams
		if(sid=="")
		{
			searchParams = {"tuId":tuId,
	                "sourceTuvId":sourceTuvId,
	                "targetTuvId":targetTuvId,
	                "tmId": tmId,
	                "sourceLocale": sourceLocale,
	                "targetLocale": targetLocale,
	                "source": newSourceText,
	                "target": newTargetText,
	                "sourceNoChanged":sourceNoChanged,
	                "targetNoChanged":targetNoChanged
	                };
		}
		else
		{
			searchParams = {"tuId":tuId,
                "sourceTuvId":sourceTuvId,
                "targetTuvId":targetTuvId,
                "tmId": tmId,
                "sourceLocale": sourceLocale,
                "targetLocale": targetLocale,
                "source": newSourceText,
                "target": newTargetText,
                "sid": sid,
                "sourceNoChanged":sourceNoChanged,
                "targetNoChanged":targetNoChanged
                };
			
		}
		$.ajax({
			   type: "POST",
			   url: "<%=saveUrl%>",
			   dataType:'text',
			   cache:false,
			   data: searchParams,
			   success: function(text){
				   if("update"==text)
				   {
					 window.opener.location = "javascript:searchClick()";
					 self.close();
				   }
				   else
				   {
					 alert(text);   
				   }
				}
		});
	}
}

function revert(type)
{
	var textbox;
	var segment;
	if("source"==type)
	{
		textbox = textboxSource;
		segment = entryInfo.source;
	}
	else
	{
		textbox = textboxTarget;
		segment = entryInfo.target;
	}
	textbox.setHTML(segment);
    textbox.focus();
}

$(document).ready(function(){
  init();
  $("#ok").click(function(){
	  saveEntry();
  });
  $("#revertSource").click(function(){
	  revert("source");
  });
  
  $("#revertTarget").click(function(){
	  revert("target");
  });
})
</script>
</head>
<body>
<div id="contentLayer">
<SPAN class="mainHeading">${lb_tm_edit_entry}</SPAN>
<br>
<br>
<span class="standardTextNew">${lb_tm_edit_entry_description}</span>
<br>
<br>
<table class="standardTextNew" width="100%" cellspacing="1" cellpadding="3" border="0">
  <tr>
    <td class="table_left">
      ${lb_source}:<br>
      <span id="sourceLocale"></span>
      <td class="table_right">
       <table>
       <tr>
       <td> 
           <div id="idEditorSource" STYLE="position: relative;">
           <table cellspacing="0" class="standardTextNew" style="background: none repeat scroll 0 0 #FFFFFF;width:98%">
           <tbody>
           <tr>
           <td id="idSpellCheck" class="coolButton" onclick="spellCheck(sourceLocale, 'editSource')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;">
           <img src="/globalsight/envoy/edit/online2/Spellcheck2.gif">
           </td>
           <td id="idBold" class="coolButton" onclick="textboxSource.makeBold(); textboxSource.frameWindow.focus();">
           <b>[bold]</b>
           </td>
           <td id="idItalic" class="coolButton" onclick="textboxSource.makeItalic(); textboxSource.frameWindow.focus();">
           <i>[italic]</i>
           </td>
           <td id="idUnderline" class="coolButton" onclick="textboxSource.makeUnderline(); textboxSource.frameWindow.focus();">
           <u>[underline]</u>
           </td>
           <td id="idBr" class="coolButton" onclick="textboxSource.addBr(); textboxSource.frameWindow.focus();"> &nbsp;[br]&nbsp; </td>
           <td id="idNbsp" class="coolButton" onclick="textboxSource.addNbsp(); textboxSource.frameWindow.focus();"> &nbsp;[nbsp]&nbsp; </td>
           <td id="idPtag" class="coolButton" onclick="showPTags('source')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;"> &nbsp;[ptag...]&nbsp; </td>
           <td id="idBrackets" class="coolButton" onclick="textboxSource.addBrackets(); textboxSource.frameWindow.focus();"> &nbsp;[[]]&nbsp; </td>
           <td id="idButCr" class="coolButton" onclick="addCr(textboxSource)"> &nbsp;[cr]&nbsp; </td>
           <td id="idButLtr" class="coolButton" onclick="addLre(textboxSource)"> &nbsp;[LRE]&nbsp; </td>
           <td id="idButRtl" class="coolButton" onclick="addPdf(textboxSource)"> &nbsp;[PDF]&nbsp; </td>
           <td id="revertSource" class="coolButton" title="Revert Source">&nbsp;[Revert]&nbsp; </td>
           </tr>
           </tbody>
           </table>
           <br>
           <iframe frameborder="0" id="editSource" src="about:blank" class="richEdit" align="center" usebr="true" onblur="return false" style="background:white;width:100%;height:120" ></iframe>
           </div>
       </td>
       </tr>
       </table>
    </td>
  </tr>
  <tr>
    <td class="table_left">
      ${lb_target}:<br>
      <span id="targetLocale"></span>
    </td>
    <td class="table_right">
       <table>
       <tr>
       <td> 
           <div id="idEditorTarget" STYLE="position: relative;">
           <table cellspacing="0" class="standardTextNew" style="background: none repeat scroll 0 0 #FFFFFF;width:98%">
           <tbody>
           <tr>
           <td id="idSpellCheck" class="coolButton" onclick="spellCheck(targetLocale, 'editTarget')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;">
           <img src="/globalsight/envoy/edit/online2/Spellcheck2.gif">
           </td>
           <td id="idBold" class="coolButton" onclick="textboxTarget.makeBold(); textboxTarget.frameWindow.focus();">
           <b>[bold]</b>
           </td>
           <td id="idItalic" class="coolButton" onclick="textboxTarget.makeItalic(); textboxTarget.frameWindow.focus();">
           <i>[italic]</i>
           </td>
           <td id="idUnderline" class="coolButton" onclick="textboxTarget.makeUnderline(); textboxTarget.frameWindow.focus();">
           <u>[underline]</u>
           </td>
           <td id="idBr" class="coolButton" onclick="textboxTarget.addBr(); textboxTarget.frameWindow.focus();"> &nbsp;[br]&nbsp; </td>
           <td id="idNbsp" class="coolButton" onclick="textboxTarget.addNbsp(); textboxTarget.frameWindow.focus();"> &nbsp;[nbsp]&nbsp; </td>
           <td id="idPtag" class="coolButton" onclick="showPTags('target')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;"> &nbsp;[ptag...]&nbsp; </td>
           <td id="idBrackets" class="coolButton" onclick="textboxTarget.addBrackets(); textbox.frameWindow.focus();"> &nbsp;[[]]&nbsp; </td>
           <td id="idButCr" class="coolButton" onclick="addCr(textboxTarget)"> &nbsp;[cr]&nbsp; </td>
           <td id="idButLtr" class="coolButton" onclick="addLre(textboxTarget)"> &nbsp;[LRE]&nbsp; </td>
           <td id="idButRtl" class="coolButton" onclick="addPdf(textboxTarget)"> &nbsp;[PDF]&nbsp; </td>
           <td id="revertTarget" class="coolButton" title="Revert Target">&nbsp;[Revert]&nbsp; </td>
           </tr>
           </tbody>
           </table>
           <br>
           <iframe frameborder="0" id="editTarget" src="about:blank" class="richEdit" align="center" usebr="true" onblur="return false" style="background:white;width:100%;height:120" ></iframe>
           </div>
       </td>
       </tr>
       </table>
    </td>
  </tr>
  <tr>
    <td class="table_left">${lb_sid}:</td>
    <td class="table_right">
      <textarea rows="2" cols="60" id="sid" class="standardTextNew"></textarea>
	</td>
  </tr>
  <tr valign="top">
    <td class="table_left">${lb_tm_search_sys_attrs}:</td>
    <td class="table_right">
      <table class="standardTextNew" cellspacing="0" cellpadding="1" border="0">
        <tr>
		  <td>${lb_tm_name}:</td>
          <td><span id="tmName"></span></td>
		</tr>
	    <tr>
		  <td>${lb_created_by}:</td>
          <td><span id="createdBy"></span></td>
		</tr>
		<tr>
		  <td>${lb_created_on}:</td>
          <td><span id="createdOn"></span></td>
		</tr>
		<tr>
		  <td>${lb_modified_by}:</td>
          <td><span id="modifiedBy"></span></td>
		</tr>
		<tr>
		  <td>${lb_modified_on}:</td>
          <td><span id="modifiedOn"></span></td>
		</tr>
	    <tr>
		  <td>${lb_last_usage_date}:</td>
          <td><span id="lastUsageDate"></span></td>
		</tr>
		<tr>
		  <td>${job_id}:</td>
          <td><span id="jobId"></span></td>
		</tr>
		<tr>
		  <td>${job_name}:</td>
          <td><span id="jobName"></span></td>
		</tr>
		<tr>
		  <td valign="top">${lb_tm_search_tu_attributes}:</td>
		  <td><span id="tuAttributes"></span></td>
		</tr>
	  </table>
	</td>
  </tr>
</table>
<div id="selectPtagDiv" class="ptagDivPop"></div>
<div id='mask'></div>
<input id="ok" type="button" name="ok" value="${lb_save}">&nbsp;
<input id="cancel" type="button" name="cancel" onclick="self.close();" value="${lb_close}">
<div id="idSourceSegment" style="display:none"></div>
<div id="idTargetSegment" style="display:none"></div>
</div>
</body>
</html>