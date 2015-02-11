//for firefox, because the style.width and other value always have "px" and not a number.
function turnPXStringTInt(str) {
    var newStr = str.replace("px", "");
    return parseInt(newStr);
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

//for firefox center window
function centerWindow(xrange, yrange){
    var xMax = screen.width;
    var yMax = screen.height;

    window.moveTo(xMax/2 - xrange, yMax/2 - yrange);
}