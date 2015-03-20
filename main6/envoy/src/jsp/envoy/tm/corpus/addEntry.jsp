<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
	        com.globalsight.everest.webapp.WebAppConstants"
	session="true"%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String saveUrl = self.getPageURL()+"&action="+WebAppConstants.TM_ACTION_SAVE_ENTRY;
%>
<html>
<head>
<title>Translation Memory - Add Entry</title>
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
#sourceLocale, #targetLocale,#tms
{
 width:270px;
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
var textboxSource;
var textboxTarget;
var userId;
var uiLocale;

function spellCheck(type, edit)
{
	var g_SC_GSA = new SC_GSA_Parameters();
	var w_scwin = null;
	var sc_customDict = null;
	var sc_dict;
	var tLocale;
	if("source"==type)
	{
		var sourceText = textboxSource.getText();
		if(""==sourceText)
		{
			alert("${msg_tm_search_add_source_null}");
			return;
		}
		var sourceLocaleText=$("#sourceLocale").find("option:selected").text(); 
		if(""==sourceLocaleText)
		{
			alert("${msg_tm_search_source}");
			return;
		}
		tLocale = sourceLocaleText.substring(sourceLocaleText.indexOf("[")+1, sourceLocaleText.indexOf("]"));
	}
	else
	{
		var targetText = textboxTarget.getText();
		if(""==targetText)
		{
			alert("${msg_tm_search_add_target_null}");
			return;
		}
		var targetLocaleText=$("#targetLocale").find("option:selected").text(); 
		if(""==targetLocaleText)
		{
			alert("${msg_tm_search_target}");
			return;
		}
		tLocale = targetLocaleText.substring(targetLocaleText.indexOf("[")+1, targetLocaleText.indexOf("]"));
	}
        
	if (!sc_customDict)
    {
       sc_dict = g_SC_GSA.getSystemDict(tLocale);
       sc_customDict = g_SC_GSA.getCustomDict(userId, tLocale);
    }

    w_scwin = scSpell(this, edit+'&typectrl=richedit', tLocale, uiLocale, sc_dict, sc_customDict);
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

function init()
{
	var locales = $.parseJSON('${locales}');
	var sourceLocaleId = '${sourceLocaleId}';
	var targetLocaleId = '${targetLocaleId}';
	var tmsList = $.parseJSON('${tms}');
	uiLocale = '${uiLocale}';
	userId = '${userId}';
	
	var bufLocales = new Array();
	bufLocales.push('<option value="-1"></option>');
	for(var i=0;i<locales.length;i++)
	{
	  var obj = locales[i];
	  var contentHtml='<option value="'+obj.id+'">'+obj.displayName+'</option>';
	  bufLocales.push(contentHtml);
	}
	$("#sourceLocale").html(bufLocales.join(""));
	$("#sourceLocale").attr("value", sourceLocaleId);
	$("#targetLocale").html(bufLocales.join(""));
	$("#targetLocale").attr("value", targetLocaleId);
	
	var bufTMs = new Array();
	bufTMs.push('<option value="-1">&nbsp;</option>');
	for(var i=0;i<tmsList.length;i++)
	{
	  var obj = tmsList[i];
	  var contentHtml='<option value="'+obj.id+'">'+obj.name+'</option>';
	  bufTMs.push(contentHtml);
	}
	$("#tms").html(bufTMs.join(""));
	
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
	
    textboxSource = document.getElementById("addSource");
	textboxTarget = document.getElementById("addTarget");

	setSegment(textboxSource, "");
	setSegment(textboxTarget, "");
	textboxSource.focus();
}

function setSegment(textbox, s)
{
  initRichEdit(textbox);
  if(textbox)
  {
	  textbox.setHTML(s);
  }
}


function saveEntry()
{
	var sourceText = textboxSource.getText();
	var targetText = textboxTarget.getText();
	var sourceLocale = $("#sourceLocale").val();
	var targetLocale = $("#targetLocale").val();
	var sid = $("#sid").val();
	var tmId = $("#tms").val();
	if(sourceText=="")
	{
		alert("${msg_tm_search_add_source_null}");
		return;
	}
	
	if(targetText=="")
	{
		alert("${msg_tm_search_add_target_null}");
		return;
	}
	
	if(sourceLocale==-1)
    {
		 alert("${msg_tm_search_source}");
		 return;
    }
	
	if(targetLocale==-1)
	{
	  alert("${msg_tm_search_target}");
	  return;
	}
	
	if(tmId==-1)
	{
	  alert("${msg_tm_search_tm}");
	  return;
	}
	
	var searchParams = {"tmId": tmId,
			            "sourceLocale": sourceLocale,
			            "targetLocale": targetLocale,
			            "source": sourceText,
			            "target": targetText,
			            "sid": sid
			            };
	$.ajax({
			type: "POST",
			url: "<%=saveUrl%>",
			dataType:'text',
			cache:false,
			data: searchParams,
			success: function(text){
			 if("save"==text)
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

$(document).ready(function(){
  init();
  $("#ok").click(function(){
	  saveEntry();
  });
})
</script>
</head>
<body>
<div id="contentLayer">
<SPAN class="mainHeading">${lb_tm_add_entry}</SPAN>
<br>
<br>
<span class="standardTextNew">${lb_tm_add_entry_description}</span>
<br>
<br>
<table class="standardTextNew" width="100%" cellspacing="1" cellpadding="3" border="0">
  <tr>
    <td class="table_left">${lb_source_locale}:</td>
    <td class="table_right">
      <select id="sourceLocale"></select>
	</td>
  </tr>
  <tr>
    <td class="table_left">
      ${lb_source}:
    </td>
    <td class="table_right">
      <table>
       <tr>
       <td> 
           <div STYLE="position: relative;">
           <table cellspacing="0" class="standardTextNew" style="background: none repeat scroll 0 0 #FFFFFF;width:98%">
           <tbody>
           <tr>
           <td id="idSpellCheck" class="coolButton" onclick="spellCheck('source', 'addSource')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;">
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
           <td id="idBrackets" class="coolButton" onclick="textboxSource.addBrackets(); textboxSource.frameWindow.focus();"> &nbsp;[[]]&nbsp; </td>
           <td id="idButCr" class="coolButton" onclick="addCr(textboxSource)"> &nbsp;[cr]&nbsp; </td>
           <td id="idButLtr" class="coolButton" onclick="addLre(textboxSource)"> &nbsp;[LRE]&nbsp; </td>
           <td id="idButRtl" class="coolButton" onclick="addPdf(textboxSource)"> &nbsp;[PDF]&nbsp; </td>
           </tr>
           </tbody>
           </table>
           <br>
           <iframe frameborder="0" id="addSource" src="about:blank" class="richEdit" align="center" usebr="true" onblur="return false" style="background:white;width:100%;height:120" ></iframe>
           </div>
       </td>
       </tr>
     </table>
    </td>
  </tr>
  <tr>
    <td class="table_left">${lb_target_locale}:</td>
    <td class="table_right">
      <select id="targetLocale"></select>
	</td>
  </tr>
  <tr>
    <td class="table_left">
      ${lb_target}:
    </td>
    <td class="table_right">
       <table>
       <tr>
       <td> 
           <div STYLE="position: relative;">
           <table cellspacing="0" class="standardTextNew" style="background: none repeat scroll 0 0 #FFFFFF;width:98%">
           <tbody>
           <tr>
           <td id="idSpellCheck" class="coolButton" onclick="spellCheck('target', 'addTarget')" style="border-left: 1px solid buttonshadow; border-right: 1px solid buttonshadow; border-width: 1px; border-style: solid; border-color: buttonshadow; padding: 1px;">
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
           <td id="idBrackets" class="coolButton" onclick="textboxTarget.addBrackets(); textbox.frameWindow.focus();"> &nbsp;[[]]&nbsp; </td>
           <td id="idButCr" class="coolButton" onclick="addCr(textboxTarget)"> &nbsp;[cr]&nbsp; </td>
           <td id="idButLtr" class="coolButton" onclick="addLre(textboxTarget)"> &nbsp;[LRE]&nbsp; </td>
           <td id="idButRtl" class="coolButton" onclick="addPdf(textboxTarget)"> &nbsp;[PDF]&nbsp; </td>
           </tr>
           </tbody>
           </table>
           <br>
           <iframe frameborder="0" id="addTarget" src="about:blank" class="richEdit" align="center" usebr="true" onblur="return false" style="background:white;width:100%;height:120" ></iframe>
           </div>
       </td>
       </tr>
       </table>
    </td>
  </tr>
  <tr>
    <td class="table_left">${lb_sid}:</td>
    <td class="table_right">
      <textarea rows="2" cols="50" id="sid" class="standardTextNew"></textarea>
	</td>
  </tr>
  <tr>
    <td class="table_left">${lb_tm}:</td>
    <td class="table_right">
      <select id="tms"></select>
	</td>
  </tr>
</table>
<input id="ok" type="button" name="ok" value="${lb_save}">&nbsp;
<input id="cancel" type="button" name="cancel" onclick="self.close();" value="${lb_close}">
</div>
</body>
</html>