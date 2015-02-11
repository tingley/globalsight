/**
var drag = new Drag();
function Drag()
{
    this.obj = null;
}

Drag.prototype.init = function(id)
{
	var obj = document.getElementById(id); 
	//start to drag the object
	obj.onmousedown = drag.start; 
}

Drag.prototype.start = function(e) 
{
	var obj = drag.obj = this; 
	//the position of monse
	obj.lastMouseX = drag.getEvent(e).x; 
	obj.lastMouseY = drag.getEvent(e).y; 
	//move the object 
	document.onmousemove = drag.move; 
	//drop the object
	document.onmouseup = drag.end; 
}

Drag.prototype.move = function(e)
{
	var obj = drag.obj; 
	//dx, dy denote the distance between drag and drop
	var dx = drag.getEvent(e).x - obj.lastMouseX; 
	var dy = drag.getEvent(e).y - obj.lastMouseY; 
	
	var left = parseInt(obj.style.left) + dx ; 
	var top = parseInt(obj.style.top) + dy; 
	obj.style.left = left; 
	obj.style.top = top; 
	obj.lastMouseX = drag.getEvent(e).x; 
	obj.lastMouseY = drag.getEvent(e).y; 
}

Drag.prototype.end = function(e)
{
    document.onmousemove = null; 
	document.onMouseup = null; 
	drag.obj = null; 
}

Drag.prototype.getEvent = function(e)
{
    if (typeof e == 'undefined'){ 
        e = window.event; 
    } 
    if(typeof e.x == 'undefined'){ 
        e.x = e.layerX; 
    } 
    if(typeof e.y == 'undefined'){ 
        e.y = e.layerY; 
    } 
    return e; 
    } 
}
*/
var zindex = 21;
var Drag = { //The object to be dragged
	draggedObj: null, 
	//initialize
	init: function(id, zIndex){ 
		var obj = document.getElementById(id); 
		//start to drag the object
		obj.onmousedown = Drag.start; 
		zindex = Number(zIndex);
		window.status = zindex;
	}, 
	start: function(e){ 
		var obj = Drag.draggedObj = this; 
		//the position of monse
		obj.lastMouseX = Drag.getEvent(e).x; 
		obj.lastMouseY = Drag.getEvent(e).y; 
		//move the object 
		window.status=obj.tagName;
		document.onmousemove = Drag.move; 
		//drop the object
		document.onmouseup = Drag.end; 
		document.getElementById("navigation").style.zIndex = 1;
	}, 
	move: function(e){ 
		 var   srcElement   =   e.srcElement   ||   e.target; 
         var obj = Drag.draggedObj; 
		//dx, dy denote the distance between drag and drop
		var dx = Drag.getEvent(e).x - obj.lastMouseX; 
		var dy = Drag.getEvent(e).y - obj.lastMouseY; 
		var left = parseInt(obj.style.left) + dx ; 
		var top = parseInt(obj.style.top) + dy ; 
		obj.style.left = left; 
		obj.style.top = top; 
		obj.lastMouseX = Drag.getEvent(e).x; 
		obj.lastMouseY = Drag.getEvent(e).y; 
	}, 
	end: function(e){ 
		document.onmousemove = null; 
		document.onMouseup = null; 
		if(Drag.draggedObj)
		{
		    Drag.draggedObj.style.zIndex = zindex;
		}
		
		Drag.draggedObj = null; 
		document.getElementById("navigation").style.zIndex = 1;
	}, 
	release: function()
	{
		if(Drag.draggedObj)
		{
		   Drag.draggedObj.onmousedown = null;
		}

	    document.onmousedown = null;
	},
	
	//The event model
	getEvent: function(e){
	    if (typeof e == 'undefined'){ 
	        e = window.event; 
	    } 
	    if(typeof e.x == 'undefined'){ 
	        e.x = e.layerX; 
	    } 
	    if(typeof e.y == 'undefined'){ 
	        e.y = e.layerY; 
	    } 

	    return e; 
    } 

}; 


// For fireFox and IE
function DragAndDrop(o, rootElement) {
    o.onmousedown=function(a) {
        var d = document;
        
        if(!a)a = window.event;  
        var x = a.layerX ? a.layerX : a.offsetX;
        var y = a.layerY ? a.layerY : a.offsetY;
        x = x + parseInt(rootElement.style.left); 
    		y = y + parseInt(rootElement.style.top); 
 
        if(o.setCapture)  
            o.setCapture();  
        else if(window.captureEvents)  
            window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  

        d.onmousemove = function(a){
            if(!a) a = window.event;  
            if(!a.pageX) a.pageX = a.clientX;  
            if(!a.pageY)a.pageY = a.clientY;  
               
            var tx = a.pageX - x , ty = a.pageY - y;  
            o.style.left = tx;
            o.style.top = ty;
        };  
   
        d.onmouseup=function(){
            if(o.releaseCapture)  
                o.releaseCapture();  
            else if(window.captureEvents)  
                window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);  
            d.onmousemove=null;  
            d.onmouseup=null; 
            o.onmousedown = null; 
        };  
    };  
}  
