/**
 *  Created by Wendy 2008-1-22
 */
var ie5 = (document.all && document.getElementsByTagName);

/**
 *  set progress bar
 */
function setSBByTip(v, el, inforEl, message)
{
   if(ie5 || document.readyState == "complete")
   {
       filterEl = el.children[0];
       if (filterEl.style.pixelWidth > 0)
       {
           var filterBackup = filterEl.style.filter;
           filterEl.style.filter = "";
           filterEl.style.filter = filterBackup;
       }
       filterEl.style.width = v + "%";
       inforEl.innerText = message;
   }
}

/**
 *  init progress bar
 */
function fakeProgressByTip(v, el, inforObj, tooltipMsg)
{
    if (v >= 101)
    {
        v = 0;
        setSBByTip(v, el, inforObj, tooltipMsg);
    }
    setSBByTip(v, el, inforObj, tooltipMsg);
    window.setTimeout("fakeProgressByTip(" + (v + 1) + ", document.all['" + el.id + "'], document.all['" + inforObj.id + "'], \"" + tooltipMsg + "\")", 20);
}

/**
 * Tooltip.js: simple CSS tool tips with drop shadows.
 *
 * This module defines a Tooltip class. Create a Tooltip object with the
 * Tooltip() constructor. Then make it visible with the show() method.
 * When done, hide it with the hide() method.
 *
 * Note that this module must be used with appropriate CSS class definitions
 * to display correctly. The following are examples:
 *
 *   .tooltipShadow {
 *      background: url(shadow.png);  /* translucent shadow * /
 *   }
 *
 *   .tooltipContent {
 *      left: -4px; top: -4px;        /* how much of the shadow shows * /
 *      background-color: #ff0;       /* yellow background * /
 *      border: solid black 1px;      /* thin black border * /
 *      padding: 5px;                 /* spacing between text and border * /
 *      font: bold 10pt sans-serif;   /* small bold font * /
 *   }
 *
 * In browsers that support translucent PNG images, it is possible to display
 * translucent drop shadows. Other browsers must use a solid color or
 * simulate transparency with a dithered GIF image that alternates solid and
 * transparent pixels.
 */
function Tooltip() {  // The constructor function for the Tooltip class
    this.tooltip = document.createElement("div"); // create div for shadow
    this.tooltip.style.position = "absolute";     // absolutely positioned
    this.tooltip.style.visibility = "hidden";     // starts off hidden
    this.tooltip.className = "tooltipShadow";     // so we can style it

    this.content = document.createElement("div"); // create div for content
    this.content.style.position = "relative";     // relatively positioned
    this.content.className = "tooltipContent";    // so we can style it

    this.tooltip.appendChild(this.content); 
    
    var divObj = document.createElement("<DIV align=center>");
    var divInfoObj = document.createElement("<DIV id=infor style='FONT-SIZE: 11px; WIDTH: 100%; COLOR: #999999; FONT-FAMILY: arial; POSITION: relative; HEIGHT: 14px; TEXT-ALIGN: center'>");
    var divSbObj = document.createElement("<DIV id=sb style='BORDER-RIGHT: #ffffff 1px solid; BORDER-TOP: #ffffff 1px solid; BACKGROUND: #DCDCDC; WIDTH: 300px; BORDER-BOTTOM: #cccccc 1px solid; HEIGHT: 14px; TEXT-ALIGN: left'>");
    var divSbChildObj = document.createElement("<DIV id=sbChild1 style='FILTER: Alpha(Opacity=0, FinishOpacity=80, Style=1, StartX=0, StartY=0, FinishX=100, FinishY=0); OVERFLOW: hidden; WIDTH: 100%; POSITION: absolute; HEIGHT: 12px'>");
    var divSbGrandChildObj = document.createElement("<DIV style='BACKGROUND: #000000; WIDTH: 100%' hidden overflow: height:12px;>");
    divSbChildObj.appendChild(divSbGrandChildObj);
    divSbObj.appendChild(divSbChildObj);
    divObj.appendChild(divSbObj);
    divObj.appendChild(divInfoObj);
    //add progress bar to tooltip
    this.tooltip.appendChild(divObj);
}

// Set the content and position of the tool tip and display it
Tooltip.prototype.show = function(text, x, y) {
    this.content.innerHTML = text;             // Set the text of the tool tip.
    this.tooltip.style.left = x + "px";        // Set the position.
    this.tooltip.style.top = y + "px";
    this.tooltip.style.visibility = "visible"; // Make it visible.

    // Add the tool tip to the document if it has not been added before
    if (this.tooltip.parentNode != document.body)
        document.body.appendChild(this.tooltip);
};

// Hide the tool tip
Tooltip.prototype.hide = function() {
    this.tooltip.style.visibility = "hidden";  // Make it invisible.
};

//whethre tool tip is visible
Tooltip.prototype.isVisible = function()
{
     return this.tooltip.style.visibility == "visible";
}