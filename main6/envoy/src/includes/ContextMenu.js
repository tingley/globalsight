ContextMenu.intializeContextMenu=function()
{
    isIE = (navigator.userAgent.indexOf("MSIE")>-1);
    if( !isIE )
    {
    var ifr = document.createElement("iframe");
    ifr.setAttribute("id", "WebFX_PopUp");
    ifr.setAttribute("scrolling", "no");
    ifr.setAttribute("src", "/");
    ifr.setAttribute("class", "WebFX-ContextMenu");
    ifr.setAttribute("marginwidth", "0");
    ifr.setAttribute("marginheight", "0");
    ifr.setAttribute("style", "position:absolute;display:none;z-index:50000000;");

    document.getElementsByTagName("body")[0].appendChild(ifr);
    }
    else
    {
    document.body.insertAdjacentHTML("BeforeEnd", '<iframe scrolling="no" src="/" class="WebFX-ContextMenu" marginwidth="0" marginheight="0" frameborder="0" style="position:absolute;display:none;z-index:50000000;" id="WebFX_PopUp"></iframe>');
    }
    WebFX_PopUp    = document.getElementById("WebFX_PopUp");
    WebFX_PopUpcss = document.getElementById("WebFX_PopUp");
    WebFX_PopUpcss.onfocus = function(){WebFX_PopUpcss.style.display="inline";};
    WebFX_PopUpcss.onblur  = function(){WebFX_PopUpcss.style.display="none";};
    if(isIE)
    {
    document.body.attachEvent("onmousedown", function(){WebFX_PopUpcss.style.display="none";});
    self.attachEvent("onblur", function(){WebFX_PopUpcss.style.display="none";});
    }
    else
    {
    document.body.onmousedown = function(){WebFX_PopUpcss.style.display="none";};
    }
}


function ContextSeperator(){}
function ContextMenu(){}

ContextMenu.showPopup=function()
{
    WebFX_PopUpcss.style.display = "block";
}

ContextMenu.display=function(popupoptions, e)
{   
    var eobj,x,y;
    eobj = (e)?e:window.event;
    x    = (eobj.x)?eobj.x:eobj.clientX;
    y    = (eobj.y)?eobj.y:eobj.clientY;
    /*
      not really sure why I had to pass window here
      it appears that an iframe inside a frames page
      will think that its parent is the frameset as
      opposed to the page it was created in...
    */
    ContextMenu.populatePopup(popupoptions,window);

    ContextMenu.showPopup();
    ContextMenu.fixSize(popupoptions.length);
    ContextMenu.fixPos(x,y);
    if(eobj instanceof Object)
    {
	    eobj.preventDefault();
	    eobj.stopPropagation();
    }
    else
    {
	    eobj.returnValue  = false;
	    eobj.cancelBubble = true
    }
}

//TODO
ContextMenu.getScrollTop=function()
{
    return document.body.scrollTop;
    //window.pageXOffset and window.pageYOffset for moz
}

ContextMenu.getScrollLeft=function()
{
    return document.body.scrollLeft;
}

ContextMenu.fixPos=function(x,y)
{
    var docheight,docwidth,dh,dw;
    docheight = document.body.clientHeight;
    docwidth  = document.body.clientWidth;
    dh = (WebFX_PopUpcss.offsetHeight+y) - docheight;
    dw = (WebFX_PopUpcss.offsetWidth+x)  - docwidth;
    if(dw>0)
    {
        WebFX_PopUpcss.style.left = (x - dw) + ContextMenu.getScrollLeft() + "px";
    }
    else
    {
        WebFX_PopUpcss.style.left = x + ContextMenu.getScrollLeft();
    }
    if(dh>0)
    {
        WebFX_PopUpcss.style.top = (y - dh) + ContextMenu.getScrollTop() + "px";
    }
    else
    {
        WebFX_PopUpcss.style.top  = y + ContextMenu.getScrollTop();
    }
}

ContextMenu.fixSize=function(p_size)
{
    var body,h,w;
    WebFX_PopUpcss.style.width = "10px";
    WebFX_PopUpcss.style.height = "100000px";
    body = (WebFX_PopUp.document)?WebFX_PopUp.document.body:WebFX_PopUp.contentDocument.body;
    // check offsetHeight twice... fixes a bug where scrollHeight
    // is not valid because the visual state is undefined
    var dummy = WebFX_PopUpcss.offsetHeight + " dummy";
    h = body.scrollHeight + body.offsetHeight - body.clientHeight;
    w = body.scrollWidth + body.offsetWidth - body.clientWidth;
    //WebFX_PopUpcss.style.height = h + "px";
    //WebFX_PopUpcss.style.width = w + "px";
    //h and w are not different in Firefox and IE, so I use a hard number, waiting for solution.
    WebFX_PopUpcss.style.height = 17 * p_size + "pt";
    WebFX_PopUpcss.style.width = 190 + "px";
    //use document.height for moz
}

ContextMenu.populatePopup=function(arr,win)
{
    var alen,i,tmpobj,doc,height,htmstr;
    alen = arr.length;
    doc  = (WebFX_PopUp.document)?WebFX_PopUp.document:WebFX_PopUp.contentDocument;
    while (doc.body != null) {
    	doc.body.innerHTML  = "";
    	break;
    }

    doc.open();
    doc.write('<html><head><link rel="StyleSheet" type="text/css" href="/globalsight/includes/ContextMenu.css"></head><body></body></html>');
    doc.close();

    for (i = 0; i < alen; i++)
    {
        if (arr[i].constructor==ContextItem)
        {
            tmpobj=doc.createElement("DIV");
            tmpobj.noWrap = true;
            tmpobj.className = "WebFX-ContextMenu-Item";
            if(arr[i].disabled)
            {
                htmstr = '<span class="WebFX-ContextMenu-DisabledContainer">' +
                    '<span class="WebFX-ContextMenu-DisabledContainer">';
                htmstr += arr[i].text + '</span></span>';
                tmpobj.innerHTML = htmstr;
                tmpobj.className = "WebFX-ContextMenu-Disabled";
                tmpobj.onmouseover = function(){
                    this.className = "WebFX-ContextMenu-Disabled-Over";
                };
                tmpobj.onmouseout  = function(){
                    this.className = "WebFX-ContextMenu-Disabled";
                };
            }
            else
            {
                tmpobj.innerHTML = arr[i].text;
                tmpobj.onclick = (function (f)
                {
                    return function () {
                        win.WebFX_PopUpcss.style.display='none';
                        if (typeof(f) == "function"){ f(); }
                    };
                })(arr[i].action);

                tmpobj.onmouseover = function(){
                    this.className="WebFX-ContextMenu-Over";
                };
                tmpobj.onmouseout  = function(){
                    this.className="WebFX-ContextMenu-Item";
                };
            }

            doc.body.appendChild(tmpobj);
        }
        else
        {
            doc.body.appendChild(doc.createElement("DIV")).className =
                "WebFX-ContextMenu-Separator";
        }
    }

    doc.body.className  = "WebFX-ContextMenu-Body" ;
    doc.body.onselectstart = function(){return false;};
}

function ContextItem(str,fnc,disabled)
{
    this.text     = str;
    this.action   = fnc;
    this.disabled = disabled || false;
}