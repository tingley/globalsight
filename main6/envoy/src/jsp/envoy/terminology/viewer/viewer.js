
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
        if (options(i).text == language)
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
        if (options(i).text == language)
        {
            idTarget.selectedIndex = i;
            break;
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
            var strLanguage = lang.selectSingleNode('name').text;
            var strLocale   = lang.selectSingleNode('locale').text;
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
            var strLanguage = lang.selectSingleNode('name').text;
            var strLocale   = lang.selectSingleNode('locale').text;
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
            var objLanguages = objDefinition.selectNodes('//language');
            var langs = new Array();
            
            if(!document.all){
                langs = objLanguages;
            }
            else {
                for (i = 0; i < objLanguages.length; i++)
                {
                    langs.push(objLanguages.item(i));
                }
            }

            langs.sort(compareLanguages);
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
                    // alert("Loaded entry " + objEntry.xml);

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
                var hits = objHitlist.selectNodes('//hit');

                if (hits.length == 0)
                {
                    if(changePage) {
                        if(direction == 0) {
                            alert("This has been the firstpage.");
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

                    if (isRTLLocale(locale))
                    {
                        strHTML = objHitlist.transformNode(objHitlistStylesheetRtl);
                    }
                    else
                    {
                        //strHTML = objHitlist.transformNode(objHitlistStylesheet);
                        //var sss = (new XMLSerializer()).serializeToString(objHitlist);
                        var xlsFile = "/globalsight/envoy/terminology/viewer/hitlist.xsl";
                
                        strHTML = getHtml(objHitlist,xlsFile);
                    }

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

    idBody.style.cursor = 'wait';

    // Quote special characters in term. Characters with a value
    // greater than 255 are stored using the %uXXXX format. To work
    // around different interpretations of IE and WebLogic and
    // Weblogic's behavior of not decoding *any* escapes if %uXXXX is
    // encountered, we escape twice.

    var xmlDoc = loadXML(strBaseUrl + '/search.jsp?' +
        'SOURCE=' + escape(escape(source)) + '&target=' + escape(escape(target)) + '&QUERY=' + escape(escape(query))); 

    SetHitlist(xmlDoc);
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

function Browse(source, target, start, direction)
{
    if (g_searching) return;

    g_searching = true;

    ClearEntry();
    //ClearHitlist(lb_searching);
    document.getElementById("idBody").style.cursor = 'wait';

    if (direction != 0 && direction != 1)
    {
        direction = 1;
    }

    // Quote special characters in term. Characters with a value
    // greater than 255 are stored using the %uXXXX format. To work
    // around different interpretations of IE and WebLogic and
    // Weblogic's behavior of not decoding *any* escapes if %uXXXX is
    // encountered, we escape twice.

    var url = strBaseUrl + '/browse.jsp?' +
        'SOURCE=' + escape(escape(source)) +
        '&target=' + escape(escape(target)) +
        '&QUERY=' + escape(escape(start)) +
        '&DIRECTION=' + direction;

    var xmlDoc = loadXML(url); 

    SetHitlist(xmlDoc, 'true', direction);
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
 
    var urlEntry = strBaseUrl + '/entry.jsp?' +
        'CONCEPTID=' + nConceptId + '&TERMID=' + nTermId +
        '&SOURCE=' + escape(escape(getSourceLanguage())) +
        '&TARGET=' + escape(escape(getTargetLanguage()));

    var xmlDoc = loadXML(urlEntry);
    SetEntry(xmlDoc);
}

function GetEntryForEditor(nConceptId)
{
    // alert("GetEntryForEditor loading " + nConceptId);

    var xmlhttp = XmlHttp.create();
    if (!xmlhttp)
    {
        TermbaseError("Fatal Error: XMLHTTP object not found", true);
    }

    var async = false;
    xmlhttp.open('POST', strBaseUrl + '/entry.jsp?' +
        'CONCEPTID=' + nConceptId + '&TERMID=0', async);
    xmlhttp.send();

    return xmlhttp.responseXML;
}

function GetInputModel()
{
    // alert("GetInputModel loading default model");

    var xmlhttp = XmlHttp.create();
    if (!xmlhttp)
    {
        TermbaseError("Fatal Error: XMLHTTP object not found", true);
    }

    var async = false;
    xmlhttp.open('POST', strBaseUrl + '/inputmodel.jsp', async);
    xmlhttp.send();

    return xmlhttp.responseXML;
}

function ReloadEntry()
{
    if (g_loading) return;

    if (nCid != 0 && nTid != 0)
    {
        g_loading = true;

        ClearEntry(lb_redisplaying);
        idBody.style.cursor = 'wait';

        xmlEntry.src = strBaseUrl + '/entry.jsp?' +
            'CONCEPTID=' + nCid + '&TERMID=' + nTid +
            '&SOURCE=' + escape(escape(getSourceLanguage())) +
            '&TARGET=' + escape(escape(getTargetLanguage()));
    }
}

function ShowStatistics()
{
    idBody.style.cursor = 'wait';
    var s = GetStatistics();

    window.showModalDialog('/globalsight/envoy/terminology/viewer/Statistics.html', s,
        'menubar:no;location:no;resizable:yes;center:yes;toolbar:no;' +
        'status:no;dialogHeight:400px;dialogWidth:400px;');
    idBody.style.cursor = 'auto';
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

function GetStatistics()
{
    idBody.style.cursor = 'wait';
    var xml = '<statistics></statistics>';
    var response = sendRequest(xml);
    idBody.style.cursor = 'auto';

    return response;
}

function LockEntryInEditor(conceptId, steal)
{
    idBody.style.cursor = 'wait';

    var xml = '<lock steal="' + steal + '">' + conceptId + '</lock>';

    var response = sendRequest(xml);

    if (response.documentElement.nodeName == 'exception')
    {
        TermbaseError(response.documentElement.text, false);
        idBody.style.cursor = 'auto';
        return null;
    }

    // alert(response.xml);

    idBody.style.cursor = 'auto';

    return response;
}

function UnlockEntryEditor(conceptId, lock)
{
    idBody.style.cursor = 'wait';

    var xml = '<unlock conceptid="' + conceptId + '">' + lock + '</unlock>';

    var response = sendRequest(xml);

    // Result is an exception if we don't own the lock (anymore).
    // Ignore it.

    // alert(response.xml);

    idBody.style.cursor = 'auto';

    return response;
}

function editorDeleteEntry(conceptId)
{
    idBody.style.cursor = 'wait';

    xml = '<delete conceptid="' + conceptId + '"></delete>';

    var response = sendRequest(xml);

    if (response.documentElement.nodeName == 'exception')
    {
        TermbaseError(response.documentElement.text, false);
        idBody.style.cursor = 'auto';
        return null;
    }

    // If we currently show this entry, display a message
    if (conceptId == ConceptId)
    {
        SetEntryFeedback("Entry deleted");
    }

    // alert(response.xml);

    idBody.style.cursor = 'auto';

    return response;
}

function ViewSaveEntry(conceptId, xml, lock)
{
    idBody.style.cursor = 'wait';

    xml = '<update ' + g_namespaces + ' conceptid="' + conceptId + '">' +
        lock + xml + '</update>';

    var response = sendRequest(xml);

    if (response.documentElement.nodeName == 'exception')
    {
        TermbaseError(response.documentElement.text, false);
        idBody.style.cursor = 'auto';
        return null;
    }

    // alert(response.xml);

    idBody.style.cursor = 'auto';

    return response;
}

function CreateEntry(xml)
{
    idBody.style.cursor = 'wait';

    xml = '<create ' + g_namespaces + ' >' + xml + '</create>';

    var response = sendRequest(xml);
    

    if (response.documentElement.nodeName == 'exception')
    {
        TermbaseError(response.documentElement.text, false);
        idBody.style.cursor = 'auto';
        return null;
    }

    idBody.style.cursor = 'auto';
    var conceptId = response.documentElement.text;

    return conceptId;
}

function ValidateEntryEditor(conceptId, xml)
{
    idBody.style.cursor = 'wait';

    xml = '<validate ' + g_namespaces + ' conceptid="' + conceptId + '">' +
        xml + '</validate>';

    var response = sendRequest(xml);

    if (response.documentElement.nodeName == 'exception')
    {
        TermbaseError(response.documentElement.text, false);
        idBody.style.cursor = 'auto';
        return null;
    }

    // alert(response.xml);

    idBody.style.cursor = 'auto';

    return response;
}

//for firefox

loadXML = function(fileRoute){
    var xmlDoc=null;
    var xmlhttp = new window.XMLHttpRequest();
    xmlhttp.open("GET",fileRoute,false);
    xmlhttp.send(null);
    xmlDoc = xmlhttp.responseXML.documentElement;
    
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
    
    if(document.all) {
        source = idSource.options(idSource.selectedIndex).text;
        target = idTarget.options(idTarget.selectedIndex).text;
    }
    else {
        source = idSource.options[idSource.selectedIndex].textContent;
        target = idTarget.options[idTarget.selectedIndex].textContent;
    }

    Search(source, target, idQuery.value);
    idQuery.focus();  
}

function getHtml(xmlDoc, xsltFile){
    var text;
 
    if(typeof(window.ActiveXObject) != 'undefined'){
        //IE
        try{
            var xslDoc = loadXML(xsltFile);
            text = xmlDoc.transformNode(xslDoc);
        }catch(e){
            alert(e.name + ": " + e.message);          
        }
        
    }else if(document.implementation && document.implementation.createDocument){  
        // Mozilla
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

function viewHitsPre()
{
    var source = idSource.options[idSource.selectedIndex].text;
    var target = idTarget.options[idTarget.selectedIndex].text;
    var start = '';

    try
    {
        if(idHitList.children.length > 0) {
            if(idHitList.children[0].children.length > 0) {
                start = idHitList.children[0].children[0].innerText;
            }
            else {
                return;
            }
        }
        else {
            return;
        }

    }
    catch (ex)
    {
        // probably no hitlist, use empty string
    }

    Browse(source, target, start, 0);

    window.event.cancelBubble = true;
    window.event.returnValue = false;
}

function viewHitsNext()
{
    var source = idSource.options[idSource.selectedIndex].text;
    var target = idTarget.options[idTarget.selectedIndex].text;
    var start = '';

    if(idHitList.children.length > 0) {
        if(idHitList.children[0].children.length > 0) {
            start = idHitList.children[0].children[idHitList.children[0].children.length - 1].innerText;
        }
        else {
            return;
        }
    }
    else {
        return;
    }

    Browse(source, target, start, 1);

    window.event.cancelBubble = true;
    window.event.returnValue = false;
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