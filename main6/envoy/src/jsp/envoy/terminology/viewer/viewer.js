
/* TODO: - add source and target lang defaults for translator */

var bAnonymous;

var strTermbase;
var strBaseUrl;

var nCid;
var nTid;

var objDoc = null;
var bInitialized = false;

var objDefinition = null;
var objHitlistStylesheet = null;
var objHitlistStylesheetRtl = null;
var objHitlist = null;
var objEntry = null;

var g_searching = false;
var g_searchingXref = false;
var g_loading = false;
var g_inputmodel = null;      // XML Document
var g_getInputModelOver = false;

// For locking/unlocking/deleting stuff
var protocolState = null;

var g_namespaces = ' xmlns:m="http://www.w3.org/1998/Math/MathML" ';


function doOnload(ba,tb,nc,nt)
{
    // initialize application
    objDoc = window.document;
    bAnonymous = ba;
    strTermbase = tb;
    strBaseUrl = "/globalsight/envoy/terminology/viewer";

    ClearHitlist();
    ClearEntry();
    InitLanguages();
    InitQuery();
    InitTermbase();

    loadUserPreferences();
    window.onresize = resizeWindow;

    bInitialized = true;

    nCid = parseInt(nc);
    nTid = parseInt(nt);

    if (nCid != 0)
    {
        GetEntry(nCid, nTid);
    }
}

function doOnunload()
{
    // hook for saving the page state
    saveUserPreferences();
}

function loadUserPreferences()
{
    try
    {
        var idPreferences = document.getElementById("idPreferences");
        
        idPreferences.load('TermbaseViewer');

        try
        {
            setSourceLanguage(idPreferences.getAttribute('sourceLanguage'));
        }
        catch (ex)
        {
        }

        try
        {
            setTargetLanguage(idPreferences.getAttribute('targetLanguage'));
        }
        catch (ex)
        {
        }
    }
    catch (ex)
    {
        // Preferences may not be available on some machines for
        // unknown reasons.
    }
}

function saveUserPreferences()
{
    var idPreferences = document.getElementById("idPreferences");
    
    try
    {
        try
        {
            idPreferences.setAttribute('sourceLanguage', getSourceLanguage());
        }
        catch (ex)
        {
        }

        try
        {
            idPreferences.setAttribute('targetLanguage', getTargetLanguage());
        }
        catch (ex)
        {
        }

        idPreferences.save('TermbaseViewer');
    }
    catch (ex)
    {
        // Preferences may not be available on some machines for
        // unknown reasons.
    }
}

function isRTLLocale(locale)
{
  if (locale.indexOf('ar') == 0 || locale.indexOf('he') == 0 ||
      locale.indexOf('fa') == 0 || locale.indexOf('ur') == 0)
  {
    return true;
  }

  return false;
}

function LoadXML(strXML)
{
    var objDom = XmlDocument.create();
    objDom.async = false;
    if (objDom.loadXML('<?xml version="1.0" encoding="unicode"?>' + strXML))
    {
        return objDom;
    }
    else
    {
        return null;
    }
}

function LoadXMLfile(strFilename)
{
    var objDom = XmlDocument.create();
    objDom.async = false;
    if (objDom.load(strFilename))
    {
        return objDom;
    }
    else
    {
        return null;
    }
}

function SourceAddLanguage(strLanguage, strLocale, bSelected)
{
    var idSource = document.getElementById("idSource");
    var oOption = new Option(strLanguage,strLanguage);
    oOption.selected = bSelected;
    oOption.locale = strLocale;
    idSource.options.add(oOption);
}

function TargetAddLanguage(strLanguage, strLocale, bSelected)
{ 
    var idTarget = document.getElementById("idTarget");
    var oOption = new Option(strLanguage,strLanguage);
    oOption.selected = bSelected;
    oOption.locale = strLocale;
    idTarget.options.add(oOption);
}

function ClearSource()
{
    var colOptions = document.getElementById("idSource").options;
    for (i = colOptions.length; i >= 1; --i)
    {
        colOptions.remove(i-1);
    }
}

function ClearTarget()
{
    var idTarget = document.getElementById("idTarget");
    var colOptions = idTarget.options;
    for (i = colOptions.length; i >= 1; --i)
    {
        colOptions.remove(i-1);
    }
}

function getSourceLocale()
{
    var idSource = document.getElementById("idSource");
    return idSource.options[idSource.selectedIndex].locale;
}

function getSourceLanguage()
{
    var idSource = document.getElementById("idSource");
    
    if(idSource.options.length == 0){ 
        return "";
    }

    return idSource.options[idSource.selectedIndex].text;
}

function setSourceLanguage(language)
{
    var idSource = document.getElementById("idSource");
    var options = idSource.options;
    for (var i = 0; i < options.length; ++i)
    {
        if (options[i].text == language)
        {
            idSource.selectedIndex = i;
            break;
        }
    }
}

function getTargetLocale()
{
    var idTarget = document.getElementById("idTarget");
    if(idTarget.options.length == 0){ 
        return "";
    }
    else {
        return idTarget.options(idTarget.selectedIndex).locale;
    }
}

function getTargetLanguage()
{
    var idTarget = document.getElementById("idTarget");
    
    if(idTarget.options.length == 0) { 
        return "";
    }
    else {
        return idTarget.options[idTarget.selectedIndex].text;
    }
}

function setTargetLanguage(language)
{
    var idTarget = document.getElementById("idTarget");
    var options = idTarget.options;
    for (var i = 0; i < options.length; ++i)
    {
        if (options[i].text == language)
        {
            idTarget.selectedIndex = i;
            break;
        }
    }
}

// Set the source language with input value, and keep target different with source.
function setSrcAndTrgLanguage(srcLanguage)
{
	setSourceLanguage(srcLanguage);
	if(srcLanguage == getTargetLanguage())
	{
		var idTarget = document.getElementById("idTarget");
		var options = idTarget.options;
		for (var i = 0; i < options.length; ++i)
	    {
	        if (options[i].text != srcLanguage)
	        {
	            idTarget.selectedIndex = i;
	            break;
	        }
	    }
	}
}

function compareLanguages(a,b)
{
  var aname = a.selectSingleNode('name').text;
  var bname = b.selectSingleNode('name').text;
  if (aname > bname) return 1;
  if (aname < bname) return -1;
  return 0;
}

function InitSource(objLanguages)
{
    if (objLanguages != null)
    {
        ClearSource();

        for (i = 0; i < objLanguages.length; i++)
        {
            var lang = objLanguages[i];
            var strLanguage = $(lang).find('name').text();
            var strLocale   = $(lang).find('locale').text();
            
            SourceAddLanguage(strLanguage, strLocale, i == 0 ? true : false);
        }
    }
}

function InitTarget(objLanguages)
{
    if (objLanguages != null)
    {
        ClearTarget();

        for (i = 0; i < objLanguages.length; i++)
        {
            var lang = objLanguages[i];
            var strLanguage = $(lang).find('name').text();
            var strLocale   = $(lang).find('locale').text();
            TargetAddLanguage(strLanguage, strLocale, i == 1 ? true : false);
        }
    }
}

function InitLanguages()
{
    if (objDefinition != null)
    {
        try
        {
            
            // For natural languages only: "//language[hasterms='true']"
            //var objLanguages = objDefinition.selectNodes('//language');
            var objLanguages = $(objDefinition).find("language");
            var langs = new Array();
            
            if(!document.all){
                langs = objLanguages;
            }
            else {
                for (i = 0; i < objLanguages.length; i++)
                {
                    langs.push(objLanguages[i]);
                }
            }
            InitSource(langs);
            InitTarget(langs);
        }
        catch (ex)
        {
            TermbaseError("Cannot initialize db " + strTermbase +
                ":\n" + ex + ".\n" + ex.description, true);
        }
    }
}

function InitQuery()
{
    idQuery.focus();
}

function InitHitlist(message)
{
    if (typeof(message) != 'undefined' && message != null)
    {
        document.getElementById("idHitList").innerHTML = message + "";
    }
    else
    {
        document.getElementById("idHitList").innerHTML = '';
    }
}

function SetHitlistFeedback(message)
{
    if (typeof(message) != 'undefined' && message != null)
    {
        document.getElementById("idHitList").innerHTML =
            '<SPAN class="feedback">' + message + '</SPAN>';
    }
    else
    {
        document.getElementById("idHitList").innerHTML = '';
    }
}

function InitEntry(message)
{
    if (typeof(message) != 'undefined' && message != null)
    {
        idViewerEntry.innerHTML = message;
    }
    else
    {
        idViewerEntry.innerHTML = '';
    }

    if (bInitialized)
    {
        updateViewerMenu(true);
    }
}

function SetEntryFeedback(message)
{
    if (typeof(message) != 'undefined' && message != null)
    {
        idViewerEntry.innerHTML =
            '<SPAN class="feedback">' + message + '</SPAN>';
    }
    else
    {
        idViewerEntry.innerHTML = '';
    }
}

function SetDefinition()
{
    objDefinition = loadXML('/globalsight/envoy/terminology/viewer/definition.jsp');

    if (bInitialized){
        InitLanguages(); 
    }

    setTermbaseLanguages(objDefinition);
    setTermbaseFields(objDefinition);
}

function getLanguageOver(data) {
    //XmlDocument doc = new XmlDocument();  
    //var aaa = doc.loadXML(data);
}

function ClearEntry(feedback)
{
    ConceptId = 0;
    TermId = 0;

    idEditButton.disabled = true;
    idPrintViewerButton.disabled = true;
    
    SetEntryFeedback(feedback);
}

function SetEntry(obj)
{
    try
    {
        idBody.style.cursor = 'auto'; 
        objEntry = obj;

        g_loading = false;

        if (objEntry.nodeName == 'exception')
        {
            TermbaseError(objEntry.documentElement.text, false);
            ClearEntry(lb_no_result);
            return;
        }

        if (objEntry.nodeName == 'noresult')
        {
            ClearEntry("Entry has been deleted.");
            idViewerHistory.RemoveEntry(nCid, nTid);
            return;
        }

        if (bInitialized)
        {
            try
            {
                var strHTML = XmlToHtml(objEntry, new MappingContext(
                    g_termbaseFields, aFieldTypes, "viewer"));

                ConceptId = nCid;
                TermId = nTid;
   
                AddEntry(nCid, nTid);

                InitEntry(strHTML);
            }
            catch (ex)
            {
                TermbaseError("Cannot format entry: " + ex + ".\n" +
                    ex.description, false);
                return;
            }
        }
    }
    catch (ex) {}
    finally
    {
        g_loading = false;
    }

}

function SetHitlistStylesheet(obj)
{
    if (obj.readyState == 'complete')
    {
        objHitlistStylesheet = obj.XMLDocument;
    }
}

function SetHitlistStylesheetRtl(obj)
{
    if (obj.readyState == 'complete')
    {
        objHitlistStylesheetRtl = obj.XMLDocument;
    }
}

function ClearHitlist(feedback)
{
    SetHitlistFeedback(feedback);
}

function SetHitlist(obj, changePage, direction)
{
   try
   {
        idBody.style.cursor = 'auto';
        objHitlist = obj;

        if (objHitlist.nodeName == 'exception')
        {
            TermbaseError(objHitlist.text, false);
            ClearHitlist(lb_no_result);
            return;
        }

        if (bInitialized)
        {
            try
            {
                //var hits = objHitlist.selectNodes('//hit');
            	var hits = $(objHitlist).find('hit');

                if (hits.length == 0)
                {
                    if(changePage) {
                        if(direction == 0) {
                            alert("This has been the first page.");
                        }
                        else {
                            alert("This has been the last page.");
                        }
                        
                        return;
                    }
                    else {
                        ClearHitlist(lb_no_result);
                    }
                }
                else
                {
                    var strHTML;
                    var locale = getSourceLocale();
                       
                    //strHTML = objHitlist.transformNode(objHitlistStylesheet);
                    //var sss = (new XMLSerializer()).serializeToString(objHitlist);
                    var xlsFile = "/globalsight/envoy/terminology/viewer/hitlist.xsl";

                    strHTML = getHtml(objHitlist,xlsFile);

                    InitHitlist(strHTML);
                }

                if (g_searchingXref && hits.length >= 1)
                {
                    var firsthit = hits[0];
                    var cid = firsthit.selectSingleNode('conceptid').text;
                    var tid = firsthit.selectSingleNode('termid').text;

                    nCid = parseInt(cid);
                    nTid = parseInt(tid);

                    if (nCid != 0)
                    {
                        GetEntry(nCid, nTid);
                    }
                }
            }
            catch (ex)
            {
                TermbaseError("Cannot format hitlist: " + ex + ".\n" +
                    ex.description, false);
                return;
            }
        }
    }
    catch (ex) {}
    finally
    {
        g_searching = false;
        g_searchingXref = false;
    }
}


function InitTermbase() {
    if(document.all) {//IE
        document.getElementById("idTermbase").innerText = strTermbase;
    }
    else {//FireFox
        document.getElementById("idTermbase").textContent = strTermbase;
    }
}

function TermbaseError(strMessage, bFatal)
{
    ShowError(strMessage);

    if (bFatal)
    {
        ClearSource();
        ClearTarget();
        // reset all elements
        ClearHitlist();
        ClearEntry();
        alert(lb_close_window);
    }
}

// ==========================================================

function Search(source, target, query, isXref)
{
    if (g_searching) return;

    g_searching = true;
    g_searchingXref = isXref;

    if (!g_searchingXref)
    {
        ClearEntry();
    }

    ClearHitlist(lb_searching);

    searchHitList(source, target, 
        query, document.getElementById("searchType").value, "current");
}

function viewHitsPre()
{
    searchHitList('', '', '', document.getElementById("searchType").value, 'pre');
}

function viewHitsNext()
{
    searchHitList('', '', '', document.getElementById("searchType").value, 'next');
}

function searchHitList(source, target, query, type, direction)
{
    idBody.style.cursor = 'wait';
    
    var urlString = searchItem +
        '&SOURCE=' + encodeURIComponent(source) 
        + '&target=' + encodeURIComponent(target) 
        + '&QUERY=' + encodeURIComponent(query)
        + '&type=' + type
        + '&direction=' + direction;

    dojo.xhrPost(
    {
       url:urlString,
       handleAs: "text",
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   if(returnData.hitlist=="isLast") {
        	       alert("The page is last page!")
        	       idBody.style.cursor = 'auto';
        	       g_searching = false;
        	   }
        	   else if(returnData.hitlist =="isFirst") {
        	       alert("The page is first page!")
        	       idBody.style.cursor = 'auto';
        	       g_searching = false;
        	   }
        	   else {
        	       var rData = StrToXML(returnData.hitlist);
        	       SetHitlist(rData);
        	       idBody.style.cursor = 'auto';
        	   }
           }
       },
       error:function(error)
       {
           ClearHitlist(lb_no_result);
           idBody.style.cursor = 'auto';
       }
   });
}

function ShowXref(xref)
{
    var language = xref.language;
    var term = xref.term;
    var cid = xref.cid;
    var tid = xref.tid;

    //alert("Showing xref for [" + language + ":" + term + "]");

    // Perform an exact match search; if only one result, the entry
    // gets loaded immediately.
    Search(language, '!' + term, true);
}

function searchEntry(source, target, CONCEPTID, TERMID)
{
    var urlString = searchEntryURL +
        '&SOURCE=' + encodeURIComponent(source) 
        + '&TARGET=' + encodeURIComponent(target) 
        + '&CONCEPTID=' + CONCEPTID + '&TERMID=' + TERMID;
    
    dojo.xhrPost(
    {
       url:urlString,
       handleAs: "text",
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   var rData = StrToXML(returnData.entry);
        	   SetEntry(rData);
           }
       },
       error:function(error)
       {
       }
   });
}

function GetEntryXml()
{
    return objEntry;
}

function GetEntry(nConceptId, nTermId)
{
    if (g_loading) return;
    g_loading = true;

    nCid = nConceptId;
    nTid = nTermId;

    ClearEntry(lb_loading);
    idBody.style.cursor = 'wait';
    
    searchEntry(getSourceLanguage(), 
        getTargetLanguage(),nConceptId, nTermId);
}


function GetInputModel()
{
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"getDefaultModel"},
       load:function(data)
       {
          var returnData = eval(data);

           if (returnData.error)
           {
        	     alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     if (result == 'error')
               {
                   TermbaseError("Get defaulted failed", false);
               }
               else {
                   g_inputmodel = StrToXML(result);
                   g_getInputModelOver = true;
               }
        	 }
       },
       error:function(error)
       {
       }
   }); 
}

function sendRequest(xml)
{
    // window.document.recalc(true);
    var xmlDoc;
    
    if (window.ActiveXObject){
        xmlDoc = new ActiveXObject('Msxml2.DOMDocument');
    }
    else {
        xmlDoc = document.implementation.createDocument("", "", null);
    }
    
    xmlDoc.loadXML(xml);

/*
    if (xmlDoc.parseError.errorCode != 0)
    {
        TermbaseError("Internal xml error: " + xmlTemp.parseError.reason,
            false);
        idBody.style.cursor = 'auto';
        return '';
    }
*/
    var xmlhttp = XmlHttp.create();

    if (!xmlhttp)
    {
        TermbaseError("Fatal Error: XMLHTTP object not found", true);
    }

    xmlhttp.open("POST", strBaseUrl + '/protocol.jsp', false);
    xmlhttp.send(xmlDoc);

    // alert("Response = " + xmlhttp.responseXML.xml);

    return xmlhttp.responseXML;
}

function ShowStatistics()
{
    idBody.style.cursor = 'wait';
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"GetStatistics"},
       load:function(data)
       {
          var returnData = eval(data);

           if (returnData.error)
           {
        	     alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     if (result == 'error')
               {
                   TermbaseError("lock entry failed", false);
               }
               else {
        	         window.showModalDialog('/globalsight/envoy/terminology/viewer/Statistics.html', 
        	         StrToXML(result),
                   'menubar:no;location:no;resizable:yes;center:yes;toolbar:no;' +
                   'status:no;dialogHeight:400px;dialogWidth:400px;');
               }
               
               idBody.style.cursor = 'auto';
        	 }
       },
       error:function(error)
       {
       }
   }); 
}

function LockEntryInEditor(conceptId, steal)
{
    idBody.style.cursor = 'wait';
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"LockEntry", steal:steal, conceptId:conceptId},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     
        	     if (result == 'error')
               {
                   TermbaseError("lock entry failed", false);
               }
               
               idBody.style.cursor = 'auto';
               g_lock = StrToXML(result);
           }
       },
       error:function(error)
       {
       }
   });    
    
}

function DeleteEntry()
{
    if (g_conceptId == g_NEWENTRY)
    {
        alert("The current entry has not been saved to the termbase yet\n" +
            "and cannot be deleted.");
        return;
    }

    if (!confirm("Do you really want to delete this entry?"))
    {
        return;
    }

    StopEditingForDelete();

    editorDeleteEntry(g_conceptId);
}

function editorDeleteEntry(p_conceptId)
{
    idBody.style.cursor = 'wait';
        
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"deleteEntry",conceptId:p_conceptId},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     idBody.style.cursor = 'auto';
        	     
        	     if (result == 'error')
               {
                   TermbaseError("Delete entry failed", false);
               }
               //set the concept id is "g_NEWENTRY", when click the hitlist, in stopEdition() method don't setEntry again.
               g_conceptId = g_NEWENTRY;
               
               if(idQuery.value != "") {
                   execute();
               }
               SetEntryFeedback("Entry deleted");
               idBody.style.cursor = 'auto';
           }
       },
       error:function(error)
       {
       }
   });
}

function ViewSaveEntry(conceptId, xml, lock, isReIndex)
{
    idBody.style.cursor = 'wait';
    
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"updateEntry",conceptId:conceptId, entryXML:xml, lock:lock, isReIndex:isReIndex},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	     alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     idBody.style.cursor = 'auto';
        	     
        	     if (result == 'error')
               {
                   TermbaseError("update entry failed", false);
               }

               alert(msg_entry_modify_sucessfully);
               GetEntry(conceptId, 0);
               idBody.style.cursor = 'auto';
               StopEditing(false);
           }
       },
       error:function(error)
       {
       }
   });
}

function SaveEntry(confirmStr)
{
	var langs = getLanguageNamesInEntry();
    if (langs.length == 0)
    {
        alert("The entry does not contain any terms.\n" +
            "Please add at least one term.");
        return;
    }
    
    //idSaving.style.display = '';
	
    var res = window.showModalDialog(
    		  "/globalsight/envoy/terminology/viewer/SaveEntryReIndex.html", confirmStr,
    		  "dialogWidth:410px; dialogHeight:200px; center:yes; resizable:no; status:no; help:no;"); 
    
    if("yes" == res)
    {
    	SaveEntry2(true);
    }
    else if("no" == res)
    {
    	SaveEntry2(false);
    }
}

function SaveEntry2(isReIndex)
{
    try
    {
        var langs = getLanguageNamesInEntry();
        if (langs.length == 0)
        {
            alert("The entry does not contain any terms.\n" +
                "Please add at least one term.");
            return;
        }

        var xml = HtmlToXml(g_entry);
        if (g_inputmodel)
        {
            var validator = new InputModelValidator(xml, g_entry,
                g_inputmodel, g_termbaseFields);
            var res = validator.validate();
            if (res)
            {
                var node = res.getHtml();
                fileClick(node);
                node.scrollIntoView(false);

                alert(res.getMessage());

                return;
            }
        }

        if (g_lock == null)
        {
            CreateEntry(xml, isReIndex);
        }
        else
        {
            ViewSaveEntry(g_conceptId, xml, g_lock.xml, isReIndex);
        }

        g_dirty = false;
        
    }
    catch (ex)
    {
        // throw ex; // need not through here?
    }
    finally
    {
        // hide feedback
        idSaving.style.display = 'none';
    }
}

function CreateEntry(xml, isReIndex)
{
    idBody.style.cursor = 'wait';
    var conceptId;
        
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"addEntry", entryXML:xml, isReIndex:isReIndex},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     conceptId = returnData.result;
        	     idBody.style.cursor = 'auto';
        	     
        	     if (conceptId == null)
               {
                   // Entry could not be created, message has been shown.
                   return;
               }
    
                g_conceptId = parseInt(conceptId);
                g_termId = 0;
                alert(msg_entry_add_sucessfully);
                GetEntry(g_conceptId, 0);
                StopEditing(false);
           }
       },
       error:function(error)
       {
       }
   });
   
   return conceptId;
}

//
// Operational methods (save, cancel, validate)
//

var VlalidateParams;

function ValidateEntry()
{
    var xml = HtmlToXml(g_entry);
    var emptyData = "<conceptGrp><concept></concept></conceptGrp>";
    if(xml==null || xml.replace(/\n/g,'')==emptyData)
    {
        return;
    }
    
    idBody.style.cursor = 'wait';
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"valadateEntry", entryXML:xml},
       load:function(data)
       {
           var returnData = eval(data);
           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   var result = returnData.result;
        	     
        	   if (result == 'error')
               {
                   TermbaseError("valadate entry failed", false);
               }
               
               VlalidateParams = new ValidationParameters(StrToXML(result));
               /*
               g_validationWindow = window.showModalDialog(
                  '/globalsight/envoy/terminology/viewer/Validation.html',
                  params, "dialogHeight:400px; dialogWidth:600px; center:yes; " +
                  "resizable:yes; status:no;");
                  */
                               
               window.open ("/globalsight/envoy/terminology/viewer/Validation.html", 
               "newwindow", "height=400, width=600, toolbar =no, menubar=no, location=no, status=no");
               idBody.style.cursor = 'auto';
           }
       },
       error:function(error)
       {
       }
   });
}

function ReIndexEntry()
{
    idReIndexing.style.display = '';
    idBody.style.cursor = 'wait';

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"ReIndexEntry"},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;

        	     if (result == 'indexing')
               {
                   alert(reindexing_warning);
               }

               idBody.style.cursor = 'auto';
               idReIndexing.style.display = 'None';
           }
       },
       error:function(error)
       {
       }
   });
}

//for firefox

loadXML = function(fileRoute){
    var xmlDoc=null;
    var xmlhttp = new window.XMLHttpRequest();
    xmlhttp.open("GET",fileRoute,false);
    xmlhttp.send(null);
    //xmlDoc = xmlhttp.responseXML.documentElement;
    if(xmlhttp.responseText != null){
    	if(window.navigator.userAgent.indexOf("MSIE")>0)
        {
    		xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
    		xmlDoc.async="false";
    		xmlDoc.loadXML(xmlhttp.responseText);
        }
        else if(window.DOMParser)
        { 
          var parser = new DOMParser();
          xmlDoc = parser.parseFromString(xmlhttp.responseText,"text/xml");
        }
    }
    return xmlDoc;
}

if(!document.all){
    //  
    XMLDocument.prototype.loadXML = function(xmlString){  
        var childNodes = this.childNodes;  
        
        for (var i = childNodes.length - 1; i >= 0; i--){  
            this.removeChild(childNodes[i]);  
        }  

        var dp = new DOMParser();  
        var newDOM = dp.parseFromString(xmlString, "text/xml");  
        var newElt = this.importNode(newDOM.documentElement, true);  
        this.appendChild(newElt);  
    }  

     // prototying the XMLDocument  
     XMLDocument.prototype.selectNodes = function(cXPathString, xNode){  
         if( !xNode ) { xNode = this; } 
           
         var oNSResolver = this.createNSResolver(this.documentElement)  ;
         var aItems = this.evaluate(cXPathString, xNode, oNSResolver,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null) ; 
         var aResult = []; 
          
         for( var i = 0; i < aItems.snapshotLength; i++){  
             aResult[i] =   aItems.snapshotItem(i);  
         }
           
         return aResult;  
    }  
    // prototying the Element  
    Element.prototype.selectNodes = function(cXPathString){  
       if(this.ownerDocument.selectNodes){  
           return this.ownerDocument.selectNodes(cXPathString, this);  
       }else{throw "For XML Elements Only";}  
    } 
     
    XMLDocument.prototype.selectSingleNode = function(cXPathString, xNode) {
        if( !xNode ) { xNode = this; }
        
        var xItems = this.selectNodes(cXPathString, xNode);  

        if(xItems.length > 0){  
            return xItems[0];  
        }else{
            return null;  
        }  
    }  
    // prototying the Element  
    Element.prototype.selectSingleNode = function(cXPathString) {  
        if(this.ownerDocument.selectSingleNode){  
            return this.ownerDocument.selectSingleNode(cXPathString, this);  
        }else{throw "For XML Elements Only";}  
    } 
     
    //  
    Element.prototype.__defineGetter__( "text",  function(){  
        return this.textContent;
    }  
    );
    
    Element.prototype.__defineSetter__( "text",  function(s){  
        this.textContent = s;
    }  
    );
    
    HTMLElement.prototype.__defineGetter__( "text",  function(){  
        return this.textContent;
    }  
    );
    
    HTMLElement.prototype.__defineSetter__( "text",  function(s){  
        this.textContent = s;
    }  
    );
    
    Element.prototype.__defineGetter__( "innerText",  function(){  
        return this.textContent;
    }  
    ); 
    
    Element.prototype.__defineSetter__( "innerText",  function(s){
        this.textContent = s;
    }  
    );
    
    HTMLElement.prototype.__defineGetter__("innerText", function(){  
        return this.textContent;  
    });  

    HTMLElement.prototype.__defineSetter__("innerText", function(s){  
        this.textContent = s;  
    });
    
    HTMLElement.prototype.__defineGetter__("parentElement", function(){  
        return this.parentNode;
    }); 
    
    Element.prototype.__defineGetter__("parentElement", function(){  
        return this.parentNode;
    });
}  

function doChange()
{
    window.event.cancelBubble = true;
    window.event.returnValue = false;

    ClearHitlist();
}

function execute(){
    var source;
    var target;
    var idSource = document.getElementById("idSource");
    var idTarget = document.getElementById("idTarget");
    
    var strQuery = LTrim(RTrim(idQuery.value));
    if(idQuery.value == '' || strQuery == '')
    {
        alert(query_no_empty);
        return;
    }
    
    if(document.all) {
        source = idSource.options(idSource.selectedIndex).text;
        target = idTarget.options(idTarget.selectedIndex).text;
    }
    else {
        source = idSource.options[idSource.selectedIndex].textContent;
        target = idTarget.options[idTarget.selectedIndex].textContent;
    }

	if (source == target)
	{
        Search(source, '', idQuery.value);
	}
	else
	{
        Search(source, target, idQuery.value);
	}
    idQuery.focus();  
}

function getHtml(xmlDoc, xsltFile){
    var text;
    var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
    var isChrome = window.navigator.userAgent.indexOf("Chrome")>0;
    if(typeof(window.ActiveXObject) != 'undefined'){
        //IE
        try{
            var xslDoc = loadXML(xsltFile);
            text = xmlDoc.transformNode(xslDoc);
        }catch(e){
            alert(e.name + ": " + e.message);          
        }
        
    }else if(isFirefox){  
        try {
            var oParser = new DOMParser();       
            var xslDoc = document.implementation.createDocument("", "", null);
            xslDoc.async = false;  
            xslDoc.load(xsltFile);

            // define XSLTProcessor object
            var xsltProcessor = new XSLTProcessor();
            xsltProcessor.importStylesheet(xslDoc);  

            var result = xsltProcessor.transformToDocument(xmlDoc);
            var xmls = new XMLSerializer();
            text = xmls.serializeToString(result);
            text = text.replace('<?xml version="1.0" encoding="UTF-8"?>','');
        }
        catch(e)  {
           if (isDebug) alert(e.name + ": " + e.message);
           alert("Unable to do xml/xsl processing");           
        }
    }else if(isChrome){  
	    try {
	    	var xslDoc = loadXML(xsltFile);
	        // define XSLTProcessor object
	        var xsltProcessor = new XSLTProcessor();
	        xsltProcessor.importStylesheet(xslDoc);  
	
	        var result = xsltProcessor.transformToDocument(xmlDoc);
	        var xmls = new XMLSerializer();
	        text = xmls.serializeToString(result);
	        text = text.replace('<?xml version="1.0" encoding="UTF-8"?>','');
	    }
	    catch(e)  {
	       if (isDebug) alert(e.name + ": " + e.message);
	       alert("Unable to do xml/xsl processing");           
	    }
	} 
    
    return text;
}

function TransHtml(xmlDoc) {
    var text;

    if(typeof(window.ActiveXObject) != 'undefined'){
        xslDoc = new ActiveXObject("Msxml2.DOMDocument.3.0");
        xslDoc.async = false; 
        xslDoc.load("/globalsight/envoy/terminology/viewer/hitlist.xsl");
        text = xmlDoc.transformNode(xslDoc.documentElement);
    }
    else if(document.implementation && document.implementation.createDocument){  
         try {

        var oParser = new DOMParser();      
        var xslDoc = document.implementation.createDocument("", "", null);
         xslDoc.async = false;  
         xslDoc.load("/globalsight/envoy/terminology/viewer/hitlist.xsl");     
         
         // define XSLTProcessor object
       var xsltProcessor = new XSLTProcessor();

       xsltProcessor.importStylesheet(xslDoc);
       
        var result = xsltProcessor.transformToDocument(xmlDoc);
        var xmls = new XMLSerializer();
        text = xmls.serializeToString(result);
      }
      catch(e)  {
        alert(e);
           if (isDebug) alert(e.name + ": " + e.message);
           alert("Unable to do xml/xsl processing");           
      }
    }
    
    return text;
}

function onHitClick(cid, tid)
{
  StopEditing(false);
  GetEntry(cid, tid);
}

//************************************************************
//bhvrhistory:

var g_maxhist = 250;
var g_history = new Array();
var g_current = -1;

function HistoryEntry(cid, tid)
{
    this.cid = cid;
    this.tid = tid;
}

HistoryEntry.prototype.getCid = function ()
{
    return this.cid;
}

HistoryEntry.prototype.getTid = function ()
{
    return this.tid;
}

HistoryEntry.prototype.toString = function ()
{
    return "[History " + this.cid + ":" + this.tid + "]";
}

function updateUI()
{
    var arrowPre = document.getElementById("idHistoryBack");
    var arrowNext = document.getElementById("idHistoryForward");
    
    if (g_history.length > 1)
    {
        if(g_current == 0) {
            arrowPre.style.display = "none";
            arrowNext.style.display = "";
        }
        else if(g_current == g_history.length - 1) {
            arrowNext.style.display = "none";
            arrowPre.style.display = "";
        }
        else {
            arrowPre.style.display = "";
            arrowNext.style.display = "";
        }

    }
    else if(g_history.length == 1) {
        arrowPre.style.display = "none";
        arrowNext.style.display = "none";
    }
    else
    {
        arrowPre.style.display = "";
        arrowNext.style.display = "";
    }
}

function AddEntry(cid, tid)
{
    // alert("history add " + cid + ":" + tid);

    // don't add if we went back in the history (or reload the same entry)
    if (g_current >= 0)
    {
        var entry = g_history[g_current];

        if (entry.cid == cid && entry.tid == tid)
        {
            return;
        }
    }

    // add new entry as top of the stack (at g_current + 1)
    ++g_current;
    g_history[g_current] = new HistoryEntry(cid, tid);

    if (g_current < g_history.length - 1)
    {
        g_history.splice(g_current + 1, g_history.length - g_current - 1);
    }

    // don't store too many entries, remove the first few.
    if (g_history.length > g_maxhist)
    {
        g_history.splice(0, 10);
    }

    g_current = g_history.length - 1;

    updateUI();
}

function RemoveEntry(cid, tid)
{
    for (var i = 0; i < g_history.length; i++)
    {
        var entry = g_history[i];

        if (entry.cid == cid && entry.tid == tid)
        {
            g_history.splice(i, 1);

            if (i <= g_current)
            {
                --g_current;
            }
        }
    }

    if (g_current >= g_history.length)
    {
        g_current = g_history.length - 1;
    }

    updateUI();
}

function IsFirst()
{
    if (g_current == 0) return true;
    return false;
}

function IsLast()
{
    if (g_current == g_history.length - 1) return true;
    return false;
}

function GetNextEntry()
{
    var result = null;

    if (g_current < g_history.length - 1)
    {
        ++g_current;
        result = g_history[g_current];
    }

    updateUI();

    return result;
}

function GetPreviousEntry()
{
    var result = null;

    if (g_current > 0)
    {
        --g_current;
        result = g_history[g_current];
    }

    updateUI();

    return result;
}

function idViewerChange() {
    splitterLeft.style.height = idBody.clientHeight - 35;
    splitterRight.style.height = idBody.clientHeight;

    if(idEditor.style.display == 'none') {
        splitterRight.style.left = idBody.clientWidth;
        idViewer.style.width = idBody.clientWidth;
    }
    else {
        idViewer.style.width = splitterRight.style.left;
    }
    
    idViewerMenuArea.style.width = splitterLeft.style.left; 
    idViewerMenuArea.style.height = splitterLeft.style.height; 
    idViewerMenu.style.width = idViewerMenuArea.style.width;
    
    idHitList.style.top = turnPXStringTInt(idHitListHeader.style.top) + turnPXStringTInt(idHitListHeader.style.height) + 10;
    idHitList.style.height = turnPXStringTInt(idViewerMenuArea.style.height) 
                             - turnPXStringTInt(idViewerMenu.style.height)
                             -30;
    
    idQuery.style.width = turnPXStringTInt(splitterLeft.style.left) - 15;
    idViewerArea.style.left = turnPXStringTInt(splitterLeft.style.left) + turnPXStringTInt(splitterLeft.style.width);
    idViewerArea.style.width = turnPXStringTInt(splitterRight.style.left) 
        - turnPXStringTInt(splitterLeft.style.left) - turnPXStringTInt(splitterLeft.style.width);
    idViewerArea.style.height = document.body.clientHeight - turnPXStringTInt(idViewerHeader.style.height);
    idViewerEntry.style.height = turnPXStringTInt(idViewerArea.style.height) - turnPXStringTInt(idViewerEntryHeader.style.height);
}

function commonPositionChange() {
    idViewerChange();
    
    //The splitterRight init left value is 10000

    if(idEditor.style.display != 'none') {
        idEditor.style.left = turnPXStringTInt(splitterRight.style.left) + turnPXStringTInt(splitterRight.style.width);
        idEditor.style.width = document.body.clientWidth - 
            turnPXStringTInt(splitterRight.style.left) - turnPXStringTInt(splitterRight.style.width);
        idEditorHeader.style.left = 0;
        idEditorHeader.style.width = turnPXStringTInt(idEditor.style.width);
        idEditorArea.style.left = 0;
        idEditorArea.style.width = turnPXStringTInt(idEditor.style.width) 
            - turnPXStringTInt(idEditorMenu.style.width);
        idEditorArea.style.height = document.body.clientHeight - turnPXStringTInt(idEditorHeader.style.height);
        //idEditorEntryHeader.style.width =  idEditorArea.style.width ;
        //idEditorEntryHeader.style.left =  idEditorArea.style.left;  
        idEditorMenu.style.left = turnPXStringTInt(idEditorArea.style.width);

        idEditorMenu.style.height = document.body.clientHeight - turnPXStringTInt(idEditorHeader.style.height);
        //idEditorEntry.style.left =  0; 
        idEditorEntry.style.height = turnPXStringTInt(idEditorArea.style.height) - turnPXStringTInt(idEditorEntryHeader.style.height);
        //idEditorEntry.style.width =  idEditorArea.style.width;
    }
}

function resizeWindow() {
    if(g_editing) {
        splitterLeft.style.left = Math.max(125, idBody.clientWidth / 8);
        splitterRight.style.left = idBody.clientWidth / 2;
    }
    
    commonPositionChange();
}

// for IE and FireFox all can use insertAdjacentHTML() method.
 function insertHtml(where, el, html){      
     where = where.toLowerCase();      
     if(el.insertAdjacentHTML){
      
         switch(where){      
             case "beforebegin":      
                 el.insertAdjacentHTML('BeforeBegin', html);      
                 return el.previousSibling;      
             case "afterbegin":      
                 el.insertAdjacentHTML('AfterBegin', html);      
                 return el.firstChild;      
             case "beforeend":      
                 el.insertAdjacentHTML('BeforeEnd', html);      
                 return el.lastChild;      
             case "afterend":      
                 el.insertAdjacentHTML('AfterEnd', html);      
                 return el.nextSibling;      
         }      
         throw 'Illegal insertion point -> "' + where + '"';      
     }      
                     
     var range = el.ownerDocument.createRange();      
     var frag;      
     switch(where){      
          case "beforebegin":      
             range.setStartBefore(el);      
             frag = range.createContextualFragment(html);      
             el.parentNode.insertBefore(frag, el);      
             return el.previousSibling;      
          case "afterbegin":      
             if(el.firstChild){      
                 range.setStartBefore(el.firstChild);      
                 frag = range.createContextualFragment(html);      
                 el.insertBefore(frag, el.firstChild);      
                 return el.firstChild;      
              }else{      
                 el.innerHTML = html;      
                 return el.firstChild;      
              }      
         case "beforeend":      
             if(el.lastChild){      
                 range.setStartAfter(el.lastChild);      
                 frag = range.createContextualFragment(html);      
                 el.appendChild(frag);      
                 return el.lastChild;      
             }else{      
                 el.innerHTML = html;      
                 return el.lastChild;      
             }      
         case "afterend":      
             range.setStartAfter(el);      
             frag = range.createContextualFragment(html);      
             el.parentNode.insertBefore(frag, el.nextSibling);      
             return el.nextSibling;      
     }      
     throw 'Illegal insertion point -> "' + where + '"';      
 }     

//for firefox, removeNode()
if(!document.all) {
    Element.prototype.removeNode = function removeNode(flag) {
        if(flag == true) {
            this.parentNode.removeChild(this);
        }
    }
    
    HTMLElement.prototype.__defineGetter__("children", 
     function () { 
         var returnValue = new Object(); 
         var number = 0; 
         for (var i=0; i<this.childNodes.length; i++) { 
             if (this.childNodes[i].nodeType == 1) { 
                 returnValue[number] = this.childNodes[i]; 
                 number++; 
             } 
         } 
         returnValue.length = number; 
         return returnValue; 
     } 
 );
}

function fileClick(elem){
    if(document.all){
        elem.click(); 
    }
    else{
        var evt=document.createEvent("MouseEvents");
        evt.initEvent("click",true,true);
        elem.dispatchEvent(evt);
    }
}

function getAtrributeText(nodeAtrribute) {
    if(!document.all){
        return nodeAtrribute.textContent;
    }
    else {
        return nodeAtrribute.text;
    }
}

function historyNext() {
    var history = GetNextEntry();
    
	  if (!history)
	  {
		    return;
	  }

    GetEntry(history.getCid(), history.getTid());
}

function historyBack()
{
    var history = GetPreviousEntry();

    if (!history)
    {
        return;
    }

    GetEntry(history.getCid(), history.getTid());
}

//for firefox, because the style.width and other value always have "px" and not a number.
function turnPXStringTInt(str) {
    var newStr = str.replace("px", "");
    return parseInt(newStr);
}