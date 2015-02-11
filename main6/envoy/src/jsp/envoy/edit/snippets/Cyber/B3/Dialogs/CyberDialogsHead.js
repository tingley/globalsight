
// {{B3}} //////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2001, [B3] - [Buckland, Buchman and Backlund] /////////////////
//
//
// WARNING: DO NOT EDIT OR DELETE THIS FILE!
// IT MAY BE UPDATED BY B3 CYBER DIALOGS AT ANY TIME
//
//

////////////////////////////////////////////////////////////////////////////////

/*b3-meta{{
<version>
	<major>7</major>
	<minor>11</minor>
</version>
}}b3-meta*/

////////////////////////////////////////////////////////////////////////////////

// b3-block{{ id=tracing ///////////////////////////////////////////////////////
//
//

var gB3HasInit = false;
var gB3LogEnabled = 0;
var gB3InsertLog = false;	// false <- March 14, 2002

function FB3_Debugging()
{
	if(typeof(gB3Debugging) == "undefined")
		return false;

	return gB3Debugging;
}

function FB3_DebuggingDetails()
{
	if(typeof(gB3DebuggingDetails) == "undefined")
		return false;

	return gB3DebuggingDetails;
}

function FB3_TraceEnabled()
{
	if(typeof(gB3LogEnabled) == "undefined")
		return FB3_Debugging();

	return gB3LogEnabled > 0;
}

function FB3_InsertActiveXTrace()
{
	if(typeof(gB3InsertActiveXLog) == "undefined")
		return false;

	return gB3InsertActiveXLog;
}

function FB3_InsertTrace()
{
	if( FB3_TraceEnabled() ) return true;

	if( document.body && 
	    FB3_Bool(document.body.b3InsertLogObject) ) {

		return true;
	}

	if(typeof(gB3InsertLog) == "undefined")
		return false;

	return gB3InsertLog;
}


function FB3_StartTracing()
{
	gB3LogEnabled++;
}

function FB3_StopTracing()
{
	gB3LogEnabled--;

	if( gB3LogEnabled < 0 ) gB3LogEnabled = 0; // saftey
}

//
//
//

function FB3_FormatText( format, args )
{
	if(!format) return "";
	
	var itemText = "";

	var i;
	var size = format.length;
	var argc = 1;

	for(i=0; i<size; i++) {

		var c = format.charAt(i);
		
		if(c == '%') {
		
			var c2 = format.charAt(i+1);
			
			if(c2 == 's') {
			
				var arg = args[argc++];

				if(typeof(arg) == "undefined")	// Added Sep 6, 2001
					itemText += "undefined";
				else itemText += arg.toString();

				i++;
				
				continue;		
			}
		}

		itemText += c;
	}

	return itemText;
}


//
//
//

var gB3LogReady = false;

function FB3_Trace( format )
{	
	if( !FB3_TraceEnabled() ) return;

	if( typeof( CyberActiveXLog ) != "undefined" ) {

		CyberActiveXLog.Trace( FB3_FormatText(format, arguments) );
	}

	if( gB3HasInit && 
		typeof( CyberLog ) != "undefined" &&
		gB3LogReady  ) {

		CyberLog.formatv( format, arguments );
	}
}


//
//
//

function FB3_TraceX( format )
{	
	if( !FB3_TraceEnabled() ) return;

	if( typeof( CyberActiveXLog ) != "undefined" ) {

		CyberActiveXLog.Trace( FB3_FormatText(format, arguments) );
	}
}


function FB3_TraceLine( format )
{	
	if( !FB3_TraceEnabled() ) return;

	if( typeof( CyberActiveXLog ) != "undefined" ) {

		CyberActiveXLog.Trace( FB3_FormatText(format, arguments) + "\n" );
	}

	if( gB3HasInit && 
		typeof( CyberLog ) != "undefined" &&
		gB3LogReady ) {

		CyberLog.formatLinev( format, arguments );
	}
}

function FB3_TraceLineX( format )
{	
	if( !FB3_TraceEnabled() ) return;

	if( typeof( CyberActiveXLog ) != "undefined" ) {

		CyberActiveXLog.Trace( FB3_FormatText(format, arguments) + "\n" );
	}
}

function FB3_LogReady( logId )
{
	gB3LogReady = true;
}

// }}b3-block //////////////////////////////////////////////////////////////////


// b3-block{{ id=utils /////////////////////////////////////////////////////////
//
//

//
//
//

function FB3_GetItem( id )
{
	if( typeof(id) != "string" ) return id;

	return document.getElementById(id);
}



//
//
//

function FB3_GetExtension( url )
{
	var last = url.lastIndexOf( "." );

	if(last == -1) return /*undefined*/;

	var ext = url.substring( last+1 );

	return ext;
}




//
//
//

function FB3_Bool( val )
{
	if( typeof(val) == "boolean" ) return val;

	if( typeof(val) == "number" ) return val != 0;
	
	if( val == null ) return false;
	
	var lower = val.toString().toLowerCase();

	if( lower == "false" ) return false;
	if( lower == "off" ) return false;
	if( lower == "0" ) return false;
	if( lower == "no" ) return false;
	if( lower == "" ) return false;

	return true;
}



// -FIX- A smarter way to really determine if
// we're running in the CyberDialog runtime
//

var gB3InCDialog;
var gB3InHTA;
var gB3InHTP;
var gB3InHTPFrame;

function FB3_InCDialog()
{
	if( typeof(gB3InCDialog) != "undefined" ) return gB3InCDialog;

	if( typeof( gB3PresetInCDialog ) != "undefined" ) {

		gB3InCDialog = gB3PresetInCDialog;
		return gB3InCDialog;
	}

	var ext = FB3_GetExtension( document.location.pathname );

	if(!ext) {
		gB3InCDialog = false;
		return gB3InCDialog;
	}

	ext = ext.toLowerCase();

	if(	ext == "cdlg" || 
		ext == "cdlgp" || 
		ext == "cdlgt" ) {

		gB3InCDialog = true;
		return gB3InCDialog;
	}

	gB3InCDialog = false;
	return gB3InCDialog;
}



//
//
//

function FB3_InRealHTA()
{
	if( typeof(gB3InHTA) != "undefined" ) return gB3InHTA;

	var ext = FB3_GetExtension( document.location.pathname );

	if(!ext) {
		gB3InHTA = false;
		return gB3InHTA;
	}

	ext = ext.toLowerCase();

	if(	ext == "hta" ) {

		gB3InHTA = true;
		return gB3InHTA;
	}

	gB3InHTA = false;
	return gB3InHTA;
}



//
//
//

function FB3_InHTP()
{
	if( typeof(gB3InHTP) != "undefined" ) {
		return gB3InHTP;
	}

	if( typeof(window.frameElement) != "undefined" ) {

		gB3InHTP = false;
		return gB3InHTP;
	}

	gB3InHTP = FB3_InHTPFrame()
	return gB3InHTP;
}



//
//
//

function FB3_InHTPFrame()
{
	if( typeof(gB3InHTPFrame) != "undefined" ) {
		return gB3InHTPFrame;
	}

	if( FB3_InRealHTA() ) {

		gB3InHTPFrame = true;
		return gB3InHTPFrame;
	}

	gB3InHTPFrame = FB3_GetItem( "thepage" ) != null;	// -HACK-
	return gB3InHTPFrame;
}




//
//
//

function FB3_InNonDialog()
{
	if( FB3_InCDialog() ) {
		return false;
	}

	if( window.dialogArguments ) {

		return false;
	}

	return true;
}



//
//
//

function FB3_GetArgument( prop, def )
{
	// HTML dialogs

	if( window.dialogArguments ) {

		if( typeof(window.dialogArguments.Item) != "undefined" ) {

			var x = window.dialogArguments.Item(prop);

			if( typeof(x) == "undefined" ) return def;

			return x;
		}
	}


	// Cyber Dialogs

	var args = document.body.getAttribute( "b3TempArgumentArray" );

	if(!args) {

		args = new ActiveXObject( "Scripting.Dictionary" );
		document.body.setAttribute( "b3TempArgumentArray", args );

		var argsSpec = document.body.getAttribute( "b3TempArguments" );

		// -FIX- Handle escaped ; and = ...

		if( argsSpec && typeof(argsSpec) == "string" ) {

			var array = argsSpec.split(";"); 

			var i;
			var size = array.length;

			for(i=0; i<size; i++) {

				var x = array[i];
				if(!x) continue;

				var itemSpec = x.split("=");
				var setprop = itemSpec[0];
				var setval = itemSpec[1];

				args.Item(setprop) = setval; 

			}
		}
	}

	var x = args.Item(prop);

	if( typeof(x) == "undefined" ) return def;

	return x;
}




//
//
//

function FB3_SetArgument( prop, value )
{
	if( window.dialogArguments ) {

		if( typeof(window.dialogArguments.Item) != "undefined" ) {

			window.dialogArguments.Item(prop) = value;
			return true;
		}
	}

	return /*undefined*/;
}




//
//
//

function FB3_RootDocument( element )
{
	// There might be a better trick than this?

	if( typeof(element) == "object" && element != null ) {

		if( typeof(element.document.parentWindow.top.document ) == "object" ) {
			
			return element.document.parentWindow.top.document;
		}
	}

	return document;
}

function FB3_InEditor( element )
{
	if( typeof( gB3Mode ) == "string" && gB3Mode == "editing" ) {

		return true;
	}

	var doc = FB3_RootDocument(element);

	return (	typeof( FB3LER_SendResized ) != "undefined" &&
				typeof( doc.body.b3TempMode ) == "string" &&
		  		doc.body.b3TempMode == "editing" );

}

function FB3_InPalette( element )
{
	if( typeof( gB3Mode ) == "string" && gB3Mode == "palette" ) {

		return true;
	}

	var doc = FB3_RootDocument(element);

	return (	typeof( FB3LER_SendResized ) != "undefined" &&
				typeof( doc.body.b3TempMode ) == "string" &&
		  		doc.body.b3TempMode == "palette" );
}

function FB3_InHelper(element)
{
	if( typeof( gB3Mode ) == "string" && gB3Mode == "helper" ) {

		return true;
	}

	var doc = FB3_RootDocument(element);

	return ( typeof( doc.body.b3TempMode ) == "string" &&
		  	 doc.body.b3TempMode == "helper" );
}

//
//
//

function FB3_Implements( el, proto )
{

	var protos = el.b3TempProtocols;

	if(!protos) return false;

	var i;
	for(i=0; i<protos.length; i++) {

		var p = protos[i];

		if(p == proto) return true;
	}

	return false;
}




//
//
//

function FB3_LoadProtocols( el, protos )
{

	el.b3TempProtocols = protos.split(";");
}




//
//
//

function FB3_AddToEventHandler( el, event, someScript )
{

	var existingHandler = el.getAttribute( event );
	var oldScript = "";

	if( existingHandler ) {

		var fnDef = existingHandler.toString();

		bodyStart = fnDef.indexOf( "{" );

		if( bodyStart == -1 ) {

			// malformed js
			return;
		}

		bodyEnd = fnDef.lastIndexOf( "}" );

		if( bodyEnd == -1 ) {

			// malformed js
			return;
		}

		bodyString = fnDef.substr(	bodyStart + 1, 
									bodyEnd - bodyStart - 2 );

		oldScript = bodyString;

	} else {

		var sc = FB3_FindScriptHandler( el, event ); // -NEW-
		if(sc) oldScript = oldScript = script.text;
	}

	if( !oldScript && typeof(someScript) == "function" ) {

		el.setAttribute( event, someScript );
		
		return;
	}

	//

	var newScript = oldScript;

	if( typeof(someScript) == "function" ) {

		var tmp = someScript.toString().split(" ");
		var tmp = tmp[1].split("(");
		fnname = tmp[0];

		someScript = fnname + ".apply(this)";
	}

	if( newScript.length == 0 ) {

		newScript = someScript;

	} else {

		newScript += "; " ;
		newScript += someScript;
	}

	var insertScript;
	if( el == document.body )
		insertScript = "{ ";
	else insertScript = "with(" + el.uniqueID + ") { ";

	insertScript += newScript;
	insertScript += "}";

	el.setAttribute( event, new Function( insertScript ) );
}



//
//
//

function FB3_HandleDefaultControl()
{ 

	if( window.event.srcElement.isContentEditable &&
		window.event.srcElement.id ) {

		FB3_SendOnChange( window.event.srcElement.id );
	}

	if( window.event.keyCode != 13 ) {
		return true;
	}

	if( window.event.srcElement.tagName == "TEXTAREA" ) {
		return true;
	}

	if( FB3_Implements( window.event.srcElement, "RichText") ) {

		// not 100% correct yet - can't do multiple lines...

		if( window.event.srcElement.isContentEditable ) {

			document.execCommand( "insertParagraph" );
			return true;
		}
	}

	var def = document.body.getAttribute( "b3DefaultControlId" );
	if(!def) {

		def = FB3_ComputeDefaultControlId();
	}

	var el = FB3_GetItem(def);

	if(el && !el.disabled) {

 
		event.cancelBubble = true;

		el.click();

		return false;
	}

	return true;
}



//
//
//

function FB3_ComputeDefaultControlId()
{
	var slideshow = FB3_LocateSlideShow();

	if( !slideshow ) return "ok";

	if( !slideshow.isAtEnd() ) {

		return "next";
	}

	return "ok";
}



//
//
//

function FB3_HandleFirstFocus()
{

	var def = document.body.getAttribute( "b3FirstFocusId" );
	if(!def) def = "ok";

	var el = FB3_GetItem(def);

	if( el && !el.disabled ) {

		if( window == window.top ) {

			window.focus();

			el.focus();

			if( !(el.tagName == "INPUT" && el.type == "button" ) ) {

				// -FIX- case

				if( typeof( el.select ) != "undefined" ) {
					el.select();
				}
			}
		}

		return;
	}
	
	window.focus();
}



//
//
//

function FB3_FindScriptHandler( el, event )
{
	var cachename = "b3TempScript_" + event;
	var cache = el.getAttribute( cachename );
	return cache;
}


//
//
//

function FB3_CacheScriptHandlers()
{
	var sc = document.scripts.length;
	var i;

	for( i=0; i<sc; i++ ) {

		var script = document.scripts[i];

		if( script.htmlFor != "" && 
			script.event != "" ) {

			var el = document.getElementById( script.htmlFor );
			if(el) {
				var cachename = "b3TempScript_" + script.event;
				el.setAttribute( cachename, script );
			}
		}
	}
}



//
//
//

var gB3Applying = 0;
var gB3SrcElement = null;

function FB3_EvalHandler( el, event )
{

	var handler = el.getAttribute( event );
	var ok = true;


	if( handler ) {

		with( el ) {

			gB3SrcElement = el;

			if( typeof(handler) != "function" ) {	// 13 March, 2002
				handler = new Function( "with( " + element.uniqueID + ") " + handler );
				el.setAttribute( event, handler );
			}

			ok = element.onchange.apply(element);

			//ok = eval(handler); // can't use return here!

			gB3SrcElement = null;
		}

	} else if( el.id ) {

		var script = FB3_FindScriptHandler( el, event );
	
		if(script) {

			var fncache = script.getAttribute( "b3TempFunction" );
			if( typeof(fncache) != "function" ) {
						
				fncache = new Function( "el", "with(el) { " + script.text + "}" );
				script.setAttribute( "b3TempFunction", fncache );
			}

			gB3SrcElement = el;
			ok = fncache(el);
			gB3SrcElement = null;
		
			/* can't do this since we want users to be able to 'return'

			with( el ) {
				gB3SrcElement = el;
				ok = eval(script.text);
				gB3SrcElement = null;
			}
			*/

		} else {

			if( event == "oncyberdialoginit" ||
			    event == "oncyberdialogapply" ) {

				var autosync = el.currentStyle.getAttribute( "b3-autosync" );
				
				if( autosync && autosync != "false" ) {
				
					FB3_SyncSource( "undefined", el );
				}
			}
		}
	}

	if( typeof(ok) == "undefined" ) ok = true;

	return FB3_Bool(ok);
}


//
//
//

function FB3_InitiateOnCyberDialogLoaded()
{

	FB3_CacheScriptHandlers();

	if( !FB3_InCDialog() ) {

		var caption = FB3_GetArgument( "b3Title" );

		if(caption) {
			document.title = caption; // this doesn't work in IE modal dialogs (must be a IE bug)
		}
	}
	
	FB3_UpdateSlideShowControls();

	FB3_InitiateEvent( document.body, "oncyberdialogloaded" );	
}

function FB3_InitiateOnCyberDialogInit()
{

	FB3_InitiateEvent( document.body, "oncyberdialoginit" );
}

function FB3_InitiateOnCyberDialogApply()
{

	gB3Applying++;
	FB3_InitiateEvent( document.body, "oncyberdialogapply" );
	gB3Applying--;
}

function FB3_InitiateOnCyberDialogValidate()
{

	return FB3_InitiateEvent( document.body, "oncyberdialogvalidate", true );
}

//
//
//

var gB3CurrentEventName = null;

function FB3_InitiateEvent( el, event, exitOnFalse )
{
	gB3CurrentEventName = event;
	var r = FB3_InitiateEvent1(el, event, exitOnFalse);
	gB3CurrentEventName = /*undefined*/ null;
	return r;
}


//
//
//

function FB3_InitiateEvent1( el, event, exitOnFalse )
{
	var ok;
	
	ok = FB3_EvalHandler( el, event );

	if(exitOnFalse && !ok) {

		
		return false;
	}

	var i;
	for(i=0; i<el.children.length; i++) {

		ok = FB3_InitiateEvent1( el.children[i], event, exitOnFalse );

		if(exitOnFalse && !ok) return false;
	}

	return true;
}



//
//
//

function FB3_ParseLength( d )
{
	// -LATER-, parse other lengths than px

	px = parseInt( d );
	if(isNaN(px)) return 0;

	return px;
}



//
//
//
 
function FB3_GetHTP( prop )
{
	if( FB3_InRealHTA() ) {

		var htm = FB3_GetItem( "theapp" );

		if(!htm) return /*undefined*/;

		return htm.getAttribute( prop );
	}

	var htm = FB3_GetItem( "thepage" );

	if(!htm) return /*undefined*/;

	return htm.getAttribute( prop );
}





//
//
//

function FB3_InitHTP()
{
	if(!FB3_InHTP()) {
		return;
	}

	var normalSize = true;
	var sizeIsInner = true;
	var useSize = true;

	if( FB3_InRealHTA() ) {

		normalSize = ( FB3_GetHTP( "windowstate" ) == "normal" );

		sizeIsInner = FB3_Bool( FB3_GetHTP( "b3SizeIsInner" ) ); // for the future...if needed
	
	} else {

		useSize = FB3_Bool( FB3_GetHTP( "b3UseSize" ) );
	}

	if(!useSize) {
		return;
	}

	window.b3TmpInResize = false;
	window.b3TmpSizeIsInner = sizeIsInner;
	window.b3TmpHasInit = false;

	if( typeof(window.b3Width) == "undefined" ) {

		var remember = FB3_GetHTP( "b3RememberSize" );

		window.b3Width = FB3_ParseLength( FB3_GetHTP( "b3Width" ) );
		window.b3Height = FB3_ParseLength( FB3_GetHTP( "b3Height" ) );

		// Hack to determine margins

		if(sizeIsInner) {

			var bodyw = document.body.clientWidth;
			var bodyh = document.body.clientHeight;

			window.b3TmpInResize = true;
			window.resizeTo( bodyw, bodyh );
			window.b3TmpInResize = false;

			var bodyw2 = document.body.clientWidth;
			var bodyh2 = document.body.clientHeight;

			window.b3HorizMargin = bodyw - bodyw2;
			window.b3VertMargin = bodyh - bodyh2;
		}

		var lastwidth = FB3_ParseLength( FB3_GetItemRegistryValue( "b3LastWidth" ) );
		var lastheight = FB3_ParseLength( FB3_GetItemRegistryValue( "b3LastHeight" ) );

		var savedWidth = FB3_ParseLength( FB3_GetItemRegistryValue( "b3LastDefinedWidth" ) );
		var savedHeight = FB3_ParseLength( FB3_GetItemRegistryValue( "b3LastDefinedHeight" ) );

		if( FB3_Bool(remember) && 
			window.b3Width == savedWidth && 
			window.b3Height == savedHeight ) {
					
			if( lastwidth && lastwidth != 0 )
				window.b3Width = lastwidth;
			
			if( lastheight && lastheight != 0 )
				window.b3Height = lastheight;
		}
	}

	if( normalSize && window.b3Width != 0 ) {

		var w = window.b3Width;
		var h = window.b3Height;

		if(sizeIsInner) {
			w += window.b3HorizMargin;
			h += window.b3VertMargin;
		}

		window.b3TmpInResize = true;
		window.resizeTo( w, h );
		window.b3TmpInResize = false;

		if(!sizeIsInner) {

			var neww = document.body.clientWidth;
			var newh = document.body.clientHeight;

			window.b3HorizMargin = window.b3Width - neww;
			window.b3VertMargin = window.b3Height - newh;
		}
	}

	if( normalSize && typeof(window.b3Left) == "undefined" ) {

		var remember = FB3_Bool( FB3_GetHTP( "b3RememberPosition" ) );

		var left =  FB3_GetHTP( "b3Left" );
		var top =  FB3_GetHTP( "b3Top" );
		var center =  FB3_Bool( FB3_GetHTP( "b3Center" ) );

		if( center ) {

			var w = window.b3Width;
			var h = window.b3Height;

			if(sizeIsInner) {
				w += window.b3HorizMargin;
				h += window.b3VertMargin;
			}

			window.b3Left = (window.screen.availWidth / 2) - (w / 2);
			window.b3Top = (window.screen.availHeight / 2) - (h / 2);
			
			window.moveTo( window.b3Left, window.b3Top );

		}

		else if( left != "auto" && left != "" ) {

			window.b3Left = FB3_ParseLength( left );
			window.b3Top = FB3_ParseLength( top );

			var lastleftstr = FB3_GetItemRegistryValue( "b3LastLeft" );
			var lasttopstr = FB3_GetItemRegistryValue( "b3LastTop" );

			var lastleft = FB3_ParseLength( lastleftstr );
			var lasttop = FB3_ParseLength( lasttopstr );

			if( remember && lastleftstr != "" ) {
						
				window.b3Left = lastleft;
				window.b3Top = lasttop;
			}

			window.moveTo( window.b3Left, window.b3Top );
		}
	}

	window.b3TmpHasInit = true;

}



//
//
//

function FB3_WindowResized()
{

	if( window.b3TmpInResize == true ) {

		//FB3_TraceLine( "\t window.b3TmpInResize" );
		
		return;
	}

	if( FB3_InCDialog() ) {
		return;
	}

	var maxwidth = 0;
	var minwidth = 0;
	var width = 0;

	var maxheight = 0;
	var minheight = 0;
	var height = 0;

	if( FB3_InHTP() && window.b3TmpHasInit ) {

		var resize = false;

		maxwidth = FB3_ParseLength( FB3_GetHTP( "b3MaxWidth") );
		minwidth = FB3_ParseLength( FB3_GetHTP( "b3MinWidth") );

		if( window.b3TmpSizeIsInner )
			width = document.body.clientWidth;
		else width = document.body.clientWidth + window.b3HorizMargin;

		maxheight = FB3_ParseLength( FB3_GetHTP( "b3MaxHeight") );
		minheight = FB3_ParseLength( FB3_GetHTP( "b3MinHeight") );

		if( window.b3TmpSizeIsInner )
			height = document.body.clientHeight;
		else height = document.body.clientHeight + window.b3VertMargin;
		
		window.b3Width = width;
		window.b3Height = height;

		if(width > maxwidth && maxwidth > 0 ) {
			window.b3Width = maxwidth;	
			resize = true;
		}

		if(width < minwidth ) {
			window.b3Width = minwidth;	
			resize = true;
		}

		if(height > maxheight && maxheight > 0 ) {
			window.b3Height = maxheight;	
			resize = true;
		}

		if(height < minheight ) {
			window.b3Height = minheight;	
			resize = true;
		}
		
		if(resize) {

			var w = window.b3Width;
			var h = window.b3Height;

			if(window.b3TmpSizeIsInner) {
				w += window.b3HorizMargin;
				h += window.b3VertMargin;
			}

			window.b3TmpInResize = true;
			window.resizeTo( w, h );
			window.b3TmpInResize = false;

		}

		return;
	}

	maxwidth = FB3_ParseLength( FB3_GetArgument( "b3DialogMaxWidth") );
	minwidth = FB3_ParseLength( FB3_GetArgument( "b3DialogMinWidth") );
	width = FB3_ParseLength( window.dialogWidth );

	maxheight = FB3_ParseLength( FB3_GetArgument( "b3DialogMaxHeight") );
	minheight = FB3_ParseLength( FB3_GetArgument( "b3DialogMinHeight") );
	height = FB3_ParseLength( window.dialogHeight );

	if(width > maxwidth && maxwidth > 0 ) {
		window.dialogWidth = maxwidth + "px";	
	}

	if(width < minwidth ) {
		window.dialogWidth = minwidth + "px";	
	}

	if(height > maxheight && maxheight > 0 ) {
		window.dialogHeight = maxheight + "px";	
	}

	if(height < minheight ) {
		window.dialogHeight = minheight + "px";	
	}
}



//
//
//

function FB3_SaveHTPSize()
{
	if( FB3_InHTP() ) {

		var rem = FB3_GetHTP( "b3RememberSize" );

		if( FB3_Bool( rem ) ) {


			FB3_SetItemRegistryValue( "b3LastDefinedWidth", FB3_GetHTP( "b3Width") );
			FB3_SetItemRegistryValue( "b3LastDefinedHeight", FB3_GetHTP( "b3Height") );

			if( window.b3Width ) {
			
				var w = window.b3Width;
				var h = window.b3Height;

				FB3_SetItemRegistryValue( "b3LastWidth", w );
				FB3_SetItemRegistryValue( "b3LastHeight", h );
			}
			
			//

			var realleft = window.screenLeft - (window.b3HorizMargin / 2.0);
			var realtop = window.screenTop - ((window.b3HorizMargin / 2.0) + ( window.b3VertMargin - window.b3HorizMargin ));

			if( window.b3TmpSizeIsInner ) {
				realleft = window.screenLeft;
				realtop = window.screenTop;
			}


			FB3_SetItemRegistryValue( "b3LastLeft", realleft );
			FB3_SetItemRegistryValue( "b3LastTop", realtop );

			FB3_CommitRegistry();
		}

		return;
	}

}



// }}b3-block //////////////////////////////////////////////////////////////////

//
//
//

function FB3_AutoEnableButtons(flag)
{

	if( gB3CurrentEventName == "oncyberdialogloaded" ) return;
	if( gB3CurrentEventName == "oncyberdialoginit" ) return;
	if( gB3CurrentEventName == "oncyberdialogapply" ) return;

	if(!FB3_InCDialog()) {

		var apply = FB3_GetItem( "apply" );
		if(apply) apply.disabled = flag;

		var ok = FB3_GetItem( "ok" );
		if(ok) ok.disabled = flag;

		return;
	}
	
	FB3_SendOnChange( "_unknown_" );
}


//
//
//

function FB3_SendButtonClicked(id)
{

	var el = FB3_GetItem(id);
	var elclass = "";
	if(el) elclass = el.className;

	if(!FB3_InCDialog()) {

		if( id == "apply" ) {

			if( FB3_InitiateOnCyberDialogValidate() == false  )
				return;

			FB3_InitiateOnCyberDialogApply();
			FB3_SaveItemsToRegistry();

			return;
		}

		else if( id == "ok" ) {

			if( FB3_InitiateOnCyberDialogValidate() == false  )
				return;

			FB3_InitiateOnCyberDialogApply();
			FB3_SaveItemsToRegistry();

			if( window.frameElement ) {		// Added 13 Sep, 2001
				
				if( typeof(window.frameElement.oncloseframe) == "function" ) {

					window.frameElement.oncloseframe(true, window.frameElement, document);
					return;
				}
			}

			window.returnValue = true;
			window.close();

			return true;
		}

		else if( id == "cancel" ) {

			FB3_SaveItemsToRegistry(true);	// Added 24 August, 2001

			if( window.frameElement ) {		// Added 13 Sep, 2001
				
				if( typeof(window.frameElement.oncloseframe) == "function" ) {

					window.frameElement.oncloseframe(false, window.frameElement, document);
					return;
				}
			}

			window.returnValue = false;
			window.close();
			return true;
		}

		//

		else if( id == "next" || elclass == "gotonext" ) {

			FB3_GotoNext();
			return true;
		}

		else if( id == "previous"  || elclass == "gotoprevious" ) {

			FB3_GotoPrevious();
			return true;
		}

		else if( id == "last"  || elclass == "gotolast" ) {

			FB3_GotoLast();
			return true;
		}

		else if( id == "first"  || elclass == "gotofirst" ) {

			FB3_GotoFirst();
			return true;

		}

		else if( id == "startanimation"  || elclass == "startanimation" ) {

			FB3_Animate(true);
			return true;
		}

		else if( id == "stopanimation"  || elclass == "stopanimation" ) {

			FB3_Animate(false);
			return true;
		}

		return false;
	}

	// 

	if( id == "next" || elclass == "gotonext" ) {				// Added 20 April, 2001

		if( FB3_GotoNext() )
			return true;
	}

	else if( id == "previous" || elclass == "gotoprevious"  ) {	// Added 20 April, 2001

		if( FB3_GotoPrevious() )
			return true;
	}

	else if( id == "last" || elclass == "gotolast" ) {			// Added 20 April, 2001

		if( FB3_GotoLast() )
			return true;
	}

	else if( id == "first" || elclass == "gotofirst" ) {		// Added 20 April, 2001

		if( FB3_GotoFirst() )
			return true;
	}

	else if( id == "startanimation"  || elclass == "startanimation" ) {

		if( FB3_Animate(true) )
			return true;
	}

	else if( id == "stopanimation"  || elclass == "stopanimation" ) {

		if( FB3_Animate(false) )
			return true;
	}

	//

	CyberDialog.SendButtonClicked(id);

	return true;
}



//
//
//

function FB3_SendItemSelected(id)
{
	if(!FB3_InCDialog()) return false;


	var el = FB3_GetItem(id);

	var index = el.getAttribute( "selectedIndex" );

	if( typeof(CyberDialog) != "undefined" ) {

		if(typeof(index) == "undefined") // Added Sep 6, 2001
			CyberDialog.SendItemMessage( "_b3ItemSelected", id, 0 );
		else CyberDialog.SendItemMessage( "_b3ItemSelected", id, index.toString() );
	}
}



//
//
//

function FB3_SendOnChange(id)
{
	if(!FB3_InCDialog()) return false;


	if( typeof(CyberDialog) != "undefined" )
		CyberDialog.SendItemMessage( "_b3OnChange", id, "" );
}



//
//
//

function FB3_SendOnReady(id)
{

	if( typeof(CyberDialog) != "undefined" )
		CyberDialog.SendItemMessage( "_b3OnReady", id, "" );
}



//
//
//

function FB3_SendMessage( senderId, msg, arg )
{

	if( typeof(CyberDialog) != "undefined" )
		CyberDialog.SendItemMessage( msg, senderId, arg );
}



//
//
//

function FB3_Run( dialog, args )
{
	if( typeof(CyberDialog) != "undefined" ) {

		document.body.removeAttribute( "b3TempDialogValue" );

		CyberDialog.SendItemMessage( "runDialog", dialog, args );

		return document.body.getAttribute( "b3TempDialogValue" );
	}

	return /*undefined*/;
}


//
//
//

function FB3_SetReturnValue( value )
{

	window.returnValue = value; 
}


//
//
//

function FB3_GetReturnValue()
{

	return window.returnValue; 
}



/* -FUTURE-
//
//
//

function FB3_RunColorDialog( colorDef, reserved )
{
	CyberDialog.SendItemMessage( "_b3RunColorDialog", colorDef.toString(), reserved.toString() );

	return body.getAttribute( "b3TempDialogValue" );
}
*/




//
//
//

function FB3_InitControls( el )
{

	if( el.tagName == "INPUT" ||
		el.tagName == "SELECT" ) {

		var handler = el.getAttribute( "onclick", 0 );
		var id = el.getAttribute( "id" );
		var handlerStr = "";

		if( typeof(handler) == "function" )
			handlerStr = handler.toString();

		/*
		if( id && 
			handlerStr.indexOf( "FB3_SendButtonClicked" ) == -1 &&
			handlerStr.indexOf( "CyberDialog" ) == -1 ) {

			FB3_AddToEventHandler( el, "onclick", "FB3_SendButtonClicked(id);" );
		}
		*/

		if( id && handlerStr.length == 0 ) {

			FB3_AddToEventHandler( el, "onclick", "FB3_SendButtonClicked(id);" );
		}
	}
	
	if( el.tagName == "SELECT" ) {

		var handler = el.getAttribute( "onchange", 0 );
		var id = el.getAttribute( "id" );
		var handlerStr = "";

		if( typeof(handler) == "function" )
			handlerStr = handler.toString();

		/*
		if( id && 
			handlerStr.indexOf( "FB3_SendItemSelected" ) == -1 ) {

			FB3_AddToEventHandler( el, "onchange", "FB3_SendItemSelected(id);" );
		}
		*/
		
		if( id && handlerStr.length == 0 ) {

			FB3_AddToEventHandler( el, "onchange", "FB3_SendOnChange(id); FB3_SendItemSelected(id);" );
		}
	}

	if( el.tagName == "INPUT" ||
		el.tagName == "TEXTAREA" ) {

		var handler = el.getAttribute( "onchange", 0 );
		var id = el.getAttribute( "id" );
		var handlerStr = "";

		if( typeof(handler) == "function" )
			handlerStr = handler.toString();

		/*
		if( id && 
			handlerStr.indexOf( "FB3_SendOnChange" ) == -1 &&
			handlerStr.indexOf( "CyberDialog" ) == -1 ) {

			FB3_AddToEventHandler( el, "onchange", "FB3_SendOnChange(id);" );
		}
		*/

		if( id && handlerStr.length == 0 ) {

			FB3_AddToEventHandler( el, "onchange", "FB3_SendOnChange(id);" );
		}
	}

	var i;
	var size = el.children.length;

	for(i=0; i<size; i++) {

		var child = el.children[i];

		FB3_InitControls( child );
	}
}




//
//
//

function FB3_LoadFromRegistry( el )
{

	if( el.id && el.tagName ) {

		var doSave = el.style.getAttribute( "b3-save-in-registry" );

		if( doSave && FB3_Bool(doSave) ) {

			var saved = FB3_GetItemRegistryValue( el.id );

			if(typeof(saved) != "undefined") {

				if( FB3_Implements( el, "Registry") ) {

					el.setAttribute( "b3TempRegistryValue", saved );

					el.loadFromRegistry();

				} else {

					FB3_Set( el, saved );

				}
			}
		}
	}

	// -FIX- add text area too

	var i;
	var size = el.children.length;

	for(i=0; i<size; i++) {

		FB3_LoadFromRegistry( el.children[i] );
	}
}



//
//
//

function FB3_SaveToRegistry( el )
{
	var doSave = el.style.getAttribute( "b3-save-in-registry" );

	if( doSave && FB3_Bool(doSave) ) {
	
		if( FB3_Implements( el, "Registry") ) {

			el.saveToRegistry();

			var saved = el.getAttribute( "b3TempRegistryValue" );
		
			if(typeof(saved) != "undefined") {
				
				FB3_SetItemRegistryValue( el.id, saved );

			}

		} else {

			FB3_SetItemRegistryValue( el.id, FB3_Get( el ) );
		}
	}

	var i;
	var size = el.children.length;

	for(i=0; i<size; i++) {

		FB3_SaveToRegistry( el.children[i] );
	}
}





//
//
//

function FB3_OnLoad()
{
	FB3_InitRegistry(); 
	FB3_InitHTP(); 
	FB3_InitiateOnCyberDialogLoaded(); 
	FB3_InitiateOnCyberDialogInit(); 
	FB3_InitItemsFromRegistry();

	FB3_HandleFirstFocus();
}




//
//
//

function FB3_OnBeforeUnload()
{

	if( FB3_InNonDialog() )
		FB3_SaveItemsToRegistry();

	FB3_SaveHTPSize();
}



//
//
//

var gB3RegName;

function FB3_InitRegistry()
{
	// Added August 24, 2001 to support HTML Help

	var strurl = document.URLUnencoded;
	if (typeof(strurl) != "undefined" &&
	    strurl.substring( 0, 3 ) == 'mk:' ) 
	{
		return; // Need this in IE5.5 - test removing in IE6 
	}
	
	//

	if( !FB3_InCDialog() ) {

		var regversion = "";

		if( FB3_InNonDialog() && 
		    !FB3_InHTPFrame() ) {

			return;
		}

		var disableReg;

		if( FB3_InNonDialog() )
			disableReg = FB3_GetHTP( "b3DisableRegistry" );
		else disableReg = FB3_GetArgument( "b3DisableRegistry" );

		if( FB3_Bool(disableReg) ) {
			return;
		}

		if( FB3_InNonDialog() )
			regversion = FB3_GetHTP( "b3RegistryVersion" );
		else regversion = FB3_GetArgument( "b3RegistryVersion" );

		//

		var regname = "";

		if( FB3_InNonDialog() )
			regname = FB3_GetHTP( "b3RegistryName" );
		else regname = FB3_GetArgument( "b3RegistryName" );

		if(!regname) {

			regname = "b3Defaults_" + document.URLUnencoded;

			regname = regname.replace(/\\/g, "_");   
			regname = regname.replace(/\//g, "_");     
			regname = regname.replace(/\:/g, "_");     
		}

		if(regversion) regname += regversion;

		gB3RegName = regname;
		CyberDefaults.load( gB3RegName );
	}
}


//
//
//

function FB3_InitItemsFromRegistry()
{

	FB3_LoadFromRegistry( document.body );
}


//
//
//

function FB3_SaveItemsToRegistry( skipItems )
{

	if( skipItems != false )	// Added 24 August, 2001
		FB3_SaveToRegistry( document.body );

	if( !FB3_InCDialog() ) {

		if( !FB3_InNonDialog() ) {

			FB3_SetArgument( "b3OutDialogWidth", window.dialogWidth );
			FB3_SetArgument( "b3OutDialogHeight", window.dialogHeight );
			FB3_SetArgument( "b3OutDialogLeft", window.dialogLeft );
			FB3_SetArgument( "b3OutDialogTop", window.dialogTop );
		}

		FB3_CommitRegistry();
	}
}


//
//
//

function FB3_CommitRegistry()
{

	// Added August 24, 2001 to suppoer HTML Help

	var strurl = document.URLUnencoded;
	if (typeof(strurl) != "undefined" &&
		strurl.substring( 0, 3 ) == 'mk:' ) 
	{
		return; // Need this in IE5.5 - test removing in IE6 
	}

	if( !FB3_InCDialog() ) {
		
		if(gB3RegName) {
			
			var e;
			var reporterror = false;

			if( FB3_InNonDialog() )
				reporterror = FB3_Bool( FB3_GetHTP( "b3ReportRegistryErrors" ) );
			else reporterror = FB3_Bool( FB3_GetArgument( "b3ReportRegistryErrors" ) );

			window.status = "";

			try {
				CyberDefaults.save( gB3RegName );
			}
			catch(e) {

				if(reporterror) {

					var msg = "Could not save persistent data.\n";
					msg += "IE persistent storage might be full.\n";

					/*
					msg += "[IE error string: '";
					msg += e.description;
					msg += "'].\n";
					msg += "[IE error number: ";
					msg += e.code;
					msg += "].\n";
					*/

					alert( msg );
				}

				window.status = "IE persistent storage is full";

			}
			
		}
	}
}



//
//
//

function FB3_GetItemRegistryValue( id )
{

	if(!FB3_InCDialog()) {

		saved =  CyberDefaults.getAttribute( id );
 

		return saved;
	}

	CyberDialog.SendItemMessage( "_b3GetItemRegistryValue", id, "" );

	var saved = document.body.getAttribute( "b3TempRegistryValue" );


	return saved;
}



//
//
//

function FB3_SetItemRegistryValue( id, value )
{

	if( typeof(value) == "undefined" ) return;
	
	if(!FB3_InCDialog()) {

		CyberDefaults.setAttribute( id, value.toString() );
		return;
	}

	CyberDialog.SendItemMessage( "_b3SetItemRegistryValue", id, value.toString() );
}




//
//
//

function FB3_GetSourceValue( id )
{
	if(!FB3_InCDialog()) {

		return FB3_GetArgument(id);
	}

	CyberDialog.SendItemMessage( "_b3GetSourceValue", id, "" );

	var saved = document.body.getAttribute( "b3TempSourceValue" );


	return saved;
}


//
//
//

function FB3_SetSourceValue( id, value )
{
	if(!FB3_InCDialog()) {

		return FB3_SetArgument(id, value);
	}

	if( typeof(value) == "undefined" ) // Added Sep 6, 2001
		CyberDialog.SendItemMessage( "_b3SetSourceValue", id, "undefined" );
	else CyberDialog.SendItemMessage( "_b3SetSourceValue", id, value.toString() );
}


//
//
//

function FB3_SyncSource( val, el )
{
	if(!el) el = gB3SrcElement;

	if(!el) {
		// error
		return null;
	}

	var id = el.id;


	if( gB3Applying > 0 ) {

		if(typeof(val) == "undefined" || val == "undefined") {
			val = FB3_Get( el );
		}

		FB3_SetSourceValue( id, val );

		return;
	}

	var sval = FB3_GetSourceValue( id );

	if( typeof(sval) == "undefined" ) {

		if( FB3_Implements( el, "3State" ) ) {	// added 21 Aug, 2001

			FB3_Set( el, sval );
		}

		return sval;
	}

	FB3_Set( el, sval );
	
	return sval;
}


//
//
//

function FB3_Set1( el, sval )	// 13 March, 2002
{
	return FB3_Set( el, document.body.b3TempSetValue );
}


//
//
//

function FB3_Set( el, sval )
{

	if( typeof(el) == "string" ) {
		el = FB3_GetItem(el);
	}

	if(!el) {
		return false;
	}

	if( el.tagName == "INPUT" &&
		( el.type == "checkbox" ||
		  el.type == "radio" ) ) {

		el.checked = FB3_Bool(sval);

		return true;
	}

	if( el.tagName == "IFRAME" ) {					// 4 Aug, 2001

		el.src = sval;

		return true;
	}

	if( FB3_Implements( el, "ValueFunctions") ) {	// 13 March, 2002

		el.put_value(sval);

		return true;
	}

	el.value = sval;
	
	return true;
}




//
//
//

function FB3_Get( el, defValue )
{

	if( typeof(el) == "string" ) {
		el = FB3_GetItem(el);
	}

	if(!el) {
		return defValue;
	}

	if( el.tagName == "INPUT" &&
		( el.type == "checkbox" ||
		  el.type == "radio" ) ) {

		return el.checked;
	}
		
	if( el.tagName == "IFRAME" ) {					// 4 Aug, 2001

		return el.src;
	}

	if( FB3_Implements( el, "ValueFunctions") ) {	// 13 March, 2002

		return el.get_value();
	}

	return el.value;
}


////////////////////////////////////////////////////////////////////////////////

//
//
//

function FB3_SetGroup( groupId, idValue )
{

	FB3_SetGroup1( document.body, groupId, idValue );
}


//
//
//

function FB3_SetGroup1( el, groupId, idValue )
{

	if( el.name && el.name == groupId ) {

		if( el.value == idValue ) {

			el.checked = true;
		}
	}

	var i;
	var size = el.children.length;

	for(i=0; i<size; i++) {

		var child = el.children[i];

		FB3_SetGroup1( child, groupId, idValue );
	}
}



//
//
//

function FB3_GetGroup( groupId )
{

	return FB3_GetGroup1( document.body, groupId );
}



//
//
//

function FB3_GetGroup1( el, groupId )
{

	if( el.name && el.name == groupId ) {

		if( el.checked ) {

			return el.value;
		}
	}

	var i;
	var size = el.children.length;

	for(i=0; i<size; i++) {

		var child = el.children[i];

		var childValue = FB3_GetGroup1( child, groupId );

		if( childValue != null ) return childValue;
	}

	return null;
}




////////////////////////////////////////////////////////////////////////////////



//
//
//

function FB3_LoadList( id, items )
{

	var el = FB3_GetItem(id);
	if(!el) return; // exception!

	var array = items;

	if( typeof(items) == "string" )
		array = items.split( "\n" );

	var i;
	var size = array.length;

	if( el.tagName == "SELECT" ) {

		el.innerHTML = "";

		for(i=0; i<array.length; i++) {

			var str = array[i];

			var opt = str.split("\t");
			var caption = opt[0];
			var value = opt[1];

			if(!value) value = caption;
	
			var newOption = document.createElement( "OPTION" );

			newOption.value = value;
			newOption.text = caption;
			
			el.options.add( newOption );
		}

		return;
	}

	if( FB3_Implements( el, "List") ) {

		el.loadList( array );
	}

}



//
//
//

function FB3_AddList( id, items )
{

	var el = FB3_GetItem(id);
	if(!el) return; // exception!

	var array = items.split( "\n" );

	var i;

	if( el.tagName == "SELECT" ) {

		for(i=0; i<array.length; i++) {

			var str = array[i];
			var old = null;

			var j;

			var opt = str.split("\t");
			var caption = opt[0];
			var value = opt[1];

			if(!value) value = caption;

			for(j=0; j<el.options.length; j++) {

				var option = el.options[j];
		
				if( option.value == value ) {
					old = option;
					break;
				}
			}

			if( !old ) {
	
				var newOption = document.createElement( "OPTION" );

				newOption.value = value;
				newOption.text = caption;
				
				el.options.add( newOption );
			}
		}

		return;
	}

	if( FB3_Implements( el, "List") ) {

		el.addList( array );
	}
}




//
//
//

function FB3_CheckListItem( id, value, flag )
{

	var el = FB3_GetItem(id);

	if( FB3_Implements( el, "List") ) {

		el.checkItem( value, flag );

	}
}





//
//
//

function FB3_ExportList( id )
{

	var el = FB3_GetItem(id);
	if(!el) return; // exception!

	var string = "";

	var i;

	if( el.tagName == "SELECT" ) {

		for(i=0; i<el.options.length; i++) {

			var opt = el.options[i];

			var caption = opt.innerText;
			var value = opt.value;

			if(!value) value = caption;
	
			if( string.length > 0 ) {
				string += "\n";
			}

			string += caption;
			string += "\t";
			string += value;
		}

		return string;
	}

	if( FB3_Implements( el, "List") ) {

		string = el.exportList();

	}

	return string;
}


////////////////////////////////////////////////////////////////////////////////

//
//
//

function FB3_FindSlideShowUpwards( el )
{
	if(!el) return null;

	if( FB3_Implements( el, "SlideShow") ) return el;

	return FB3_FindSlideShowUpwards( el.parentElement );
}



//
//
//

function FB3_LocateSlideShow( slideshow )
{

	var argshow = slideshow;

	if( slideshow ) {

		slideshow = FB3_GetItem( slideshow );

		if( slideshow && !FB3_Implements( slideshow, "SlideShow") ) {

			if( slideshow.slideshow )
				slideshow = FB3_GetItem( slideshow.slideshow );
			else slideshow = null;

		}
	}

	if( !slideshow && argshow ) slideshow = FB3_FindSlideShowUpwards(argshow);

	if( !slideshow && !argshow && window.event ) slideshow = FB3_FindSlideShowUpwards(window.event.srcElement);

	if( !slideshow ) slideshow = FB3_GetItem( "slideshow" );

	if( !document.body.b3TempFoundNoSlideshows ) {

		if( !slideshow ) {
			slideshow = FB3_FindSlideShow(document.body);
		}

		if( !slideshow ) {
			document.body.b3TempFoundNoSlideshows = true;
		}
	}

	if( !slideshow ) return null;

	if( !FB3_Implements( slideshow, "SlideShow") ) return null;

	return slideshow;
}




//
//
//

function FB3_FindSlideShow(el)
{

	var i;
	var size = el.children.length;

	for( i=0; i<size; i++ ) {

		var child = el.children[i];

		if( FB3_Implements( child, "SlideShow" ) ) {

			return child;
		}

		var sub = FB3_FindSlideShow(child);

		if( sub ) {
			return sub;
		}
	}

	return null;
}




//
//
//

function FB3_UpdateSlideShowControls( slideshow )
{

	//

	if( typeof( FB3_InEditor ) == "function" && FB3_InEditor(slideshow) ) {
		
		return false;
	}

	if( typeof( FB3_InPalette ) == "function" && FB3_InPalette(slideshow) ) {
		
		return false;
	}

	//

	if(slideshow) {
		document.body.b3TempFoundNoSlideshows = false;
	}

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return;


	var i;
	var size = document.body.all.length;

	for(i=0; i<size; i++) {
	
		var child = document.body.all[i];

		if(!child.tagName) continue;

		if(	child.tagName == "INPUT" ||
			child.tagName == "BUTTON" ) {

			var ss = FB3_LocateSlideShow(child);

			if(ss && ss == slideshow) {

				if( child.id == "next" ||
					child.className == "gotonext" ) {

					child.disabled = ss.isAtEnd();
					continue;
				}

				if( child.id == "previous" ||
					child.className == "gotoprevious" ) {

					child.disabled = ss.isAtStart();
					continue;
				}
				
				if( child.id == "first" ||
					child.className == "gotofirst" ) {

					child.disabled = ss.isAtStart();
					continue;
				}

				if( child.id == "last" ||
					child.className == "gotolast" ) {

					child.disabled = ss.isAtEnd();
					continue;
				}

				if( child.id == "stopanimation" ||
					child.className == "stopanimation" ) {

					child.disabled = !ss.b3TempRunning; // isRunning()
					continue;
				}

				if( child.id == "startanimation" ||
					child.className == "startanimation" ) {

					child.disabled = ss.b3TempRunning; // isRunning()
					continue;
				}
			}
		}
	}

	return true;
}




//
//
//

function FB3_GotoNext( slideshow )
{

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return false;

	slideshow.gotoNext();
	return true;
}


//
//
//

function FB3_GotoPrevious( slideshow )
{

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return false;

	slideshow.gotoPrevious();
	return true;
}



//
//
//

function FB3_GotoLast( slideshow )
{

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return false;

	slideshow.gotoLast();
	return true;
}


//
//
//

function FB3_GotoFirst( slideshow )
{

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return false;

	slideshow.gotoFirst();
	return true;
}



//
//
//

function FB3_Animate( flag, slideshow )
{

	slideshow = FB3_LocateSlideShow(slideshow);
	if( !slideshow ) return false;

	if(flag) slideshow.animate();
	else slideshow.stop();

	return true;
}


//////////////////////////////////////////////////////////////////////////////////////
// Include: CyberAnchoredEdges
// Speed isues force us to include CyberAnchoredEdges here
// and not use the CyberAnchoredEdges.htc file
//

//
//
//

function FCyberAnchoredEdges_AttachElements(root)
{
	if(!root) root = document.body;

	var coll = root.children;
	var i;
	var size = coll.length;

	FCyberAnchoredEdges_FSetup(root);

	for(i=0; i<size; i++) {

		var element = coll[i];

		if(!element.tagName) continue;

		FCyberAnchoredEdges_AttachElements(element);
	}
}




//
//
//

function FCyberAnchoredEdges_DisableExpressions(flag, root)
{
	if(!root) root = document.body;

	var coll = root.children;
	var i;
	var size = coll.length;

	if( flag ) {

		FCyberAnchoredEdges_FDetach(root);

	} else {

		FCyberAnchoredEdges_FSetup(root);
	}

	for(i=0; i<size; i++) {

		var element = coll[i];

		if(!element.tagName) continue;

		FCyberAnchoredEdges_DisableExpressions( element );

	}
}






//
//
//

function FCyberAnchoredEdges_FDetach(element)
{

	if( typeof(element.b3TempAnchorSetup) == "undefined" ) return;

	element.style.removeExpression( "width" );
	element.style.removeExpression( "height" );
	element.style.removeExpression( "left" );
	element.style.removeExpression( "top" );

	element.removeAttribute( "b3TempAnchorSetup" );
	element.removeAttribute( "b3TempOrigOffsetWidth" );
	element.removeAttribute( "b3TempOrigOffsetHeight" );
	element.removeAttribute( "b3TempOrigOffsetLeft" );
	element.removeAttribute( "b3TempOrigOffsetTop" );
}


// Needed because offsetWidth et al is not known when we setup expressions
//
//

function FCyberAnchoredEdges_FOffsetWidth(element)
{
	if(element.offsetWidth == 0) return 0;

	if(element.b3TempOrigOffsetWidth == null) 
		element.b3TempOrigOffsetWidth = element.offsetWidth + element.b3TempOrigOffsetWidthEXTRA;

	return element.b3TempOrigOffsetWidth;
}

function FCyberAnchoredEdges_FOffsetHeight(element)
{
	if(element.offsetHeight == 0) return 0;

	if(element.b3TempOrigOffsetHeight == null) 
		element.b3TempOrigOffsetHeight = element.offsetHeight + element.b3TempOrigOffsetHeightEXTRA;

	return element.b3TempOrigOffsetHeight;
}

function FCyberAnchoredEdges_FOffsetLeft(element)
{
	if(element.offsetLeft == 0) return 0;

	if(element.b3TempOrigOffsetLeft == null) 
		element.b3TempOrigOffsetLeft = element.offsetLeft + element.b3TempOrigOffsetLeftEXTRA;

	return element.b3TempOrigOffsetLeft;
}

function FCyberAnchoredEdges_FOffsetTop(element)
{
	if(element.offsetHeight == 0) return 0;

	if(element.b3TempOrigOffsetTop == null) 
		element.b3TempOrigOffsetTop = element.offsetTop + element.b3TempOrigOffsetTopEXTRA;

	return element.b3TempOrigOffsetTop;
}


//
//
//

function FCyberAnchoredEdges_FSetup(element)
{

	if( typeof(gB3LERAnchorsDisabled) != "undefined" ) {

		if(gB3LERAnchorsDisabled > 0) return;
	}

	if( typeof(element.b3TempAnchorSetup) != "undefined" ) {

		return;	// Already setup!
	}

	var right = element.style.getAttribute("b3-right");
	var bottom = element.style.getAttribute("b3-bottom");
	if( !right && !bottom ) {

		return;
	}

	//
	
	element.style.removeExpression( "width" );
	element.style.removeExpression( "height" );
	element.style.removeExpression( "left" );
	element.style.removeExpression( "top" );
	
	//

	var spec = FCyberAnchoredEdges_FGetSpec(element);
	if(!spec) {
		return;
	}

	var anchoredRight = FCyberAnchoredEdges_FInCollection( spec, "right" );
	var anchoredLeft = FCyberAnchoredEdges_FInCollection( spec, "left" );
	var anchoredBottom = FCyberAnchoredEdges_FInCollection( spec, "bottom" );
	var anchoredTop = FCyberAnchoredEdges_FInCollection( spec, "top" );
	var anchoredWidth = FCyberAnchoredEdges_FInCollection( spec, "width" );
	var anchoredHeight = FCyberAnchoredEdges_FInCollection( spec, "height" );

	if( !anchoredRight &&
		!anchoredLeft &&
		!anchoredBottom &&
		!anchoredTop &&
		!anchoredWidth &&
		!anchoredHeight ) {

		return;
	}

	if( 1 ) {

		element.b3TempAnchorSetup = true;
		
		//debugger

		element.b3TempOrigStyleWidth = element.style.width;
		element.b3TempOrigStyleHeight = element.style.height;

		if( element.style.pixelHeight < 0 )
			element.b3TempOrigStyleHeight = "0px";

		if( element.style.pixelWidth < 0 )
			element.b3TempOrigStyleWidth = "0px";
		
		var b3Right = FCyberAnchoredEdges_FGetExtraStyleInfo( element, "b3-right" );
		element.b3TempOrigOffsetRight = b3Right;
		
		var b3Bottom = FCyberAnchoredEdges_FGetExtraStyleInfo( element, "b3-bottom" );
		element.b3TempOrigOffsetBottom = b3Bottom;

		element.b3TempPositionLeftOffset = 0;
		element.b3TempPositionRightOffset = 0;

		element.b3TempOrigOffsetWidthEXTRA = 0;
		element.b3TempOrigOffsetHeightEXTRA = 0;

		element.b3TempOrigOffsetLeftEXTRA = 0;
		element.b3TempOrigOffsetTopEXTRA = 0;

		// This needs check in -IE6-
		// Not needed in IE6 - check 5.5 again

		if( 0 && element.tagName == "IFRAME" ) {
		
			var lm = FCyberAnchoredEdges_FParseLength( element.style.borderLeftWidth );
			var rm = FCyberAnchoredEdges_FParseLength( element.style.borderRightWidth );
			var tm = FCyberAnchoredEdges_FParseLength( element.style.borderTopWidth );
			var bm = FCyberAnchoredEdges_FParseLength( element.style.borderBottomWidth );
		
			element.b3TempPositionLeftOffset = lm;
			element.b3TempPositionRightOffset = tm;

			element.b3TempOrigOffsetWidthEXTRA = (lm + rm);
			element.b3TempOrigOffsetHeightEXTRA = (tm + bm);

			element.b3TempOrigOffsetLeftEXTRA = (lm);
			element.b3TempOrigOffsetRight += (rm);

			element.b3TempOrigOffsetTopEXTRA = (tm);
			element.b3TempOrigOffsetBottom += (bm);
		}
	}

	//

	if( anchoredLeft && anchoredRight ) {
		element.style.setExpression( "width", "this.offsetParent.clientWidth - (FCyberAnchoredEdges_FOffsetLeft(this)+this.b3TempOrigOffsetRight)" );
	
	} else

	if( !anchoredLeft && !anchoredWidth && anchoredRight ) {
		element.style.width = element.b3TempOrigStyleWidth;
		element.style.setExpression( "left", "this.offsetParent.clientWidth - (FCyberAnchoredEdges_FOffsetWidth(this) + this.b3TempOrigOffsetRight - this.b3TempPositionLeftOffset)" );
	
	} else

	if( anchoredWidth ) {
		element.style.width = element.b3TempOrigStyleWidth;
		element.style.setExpression( "left", "(this.offsetParent.clientWidth/2) - (FCyberAnchoredEdges_FOffsetWidth(this) / 2)" );
	}

	//

	if( anchoredTop && anchoredBottom ) {
		element.style.setExpression( "height", "this.offsetParent.clientHeight - (FCyberAnchoredEdges_FOffsetTop(this) + this.b3TempOrigOffsetBottom)" );
	
	} else

	if( !anchoredTop && !anchoredHeight && anchoredBottom ) {
		element.style.height = element.b3TempOrigStyleHeight;
		element.style.setExpression( "top", "this.offsetParent.clientHeight - (FCyberAnchoredEdges_FOffsetHeight(this) + this.b3TempOrigOffsetBottom - this.b3TempPositionLeftOffset)" );
	
	} else

	if( anchoredHeight ) {
		element.style.height = element.b3TempOrigStyleHeight;
		element.style.setExpression( "top", "(this.offsetParent.clientHeight/2) - (FCyberAnchoredEdges_FOffsetHeight(this) / 2)" );
	}


}



////////////////////////////////////////////////////////////////////////////////                          
// Helpers
//

//
//
//

function FCyberAnchoredEdges_FGetSpec( el )
{
	var spec = el.style.getAttribute("b3-anchored-edges");

	if(!spec) return null;

	return spec.split(" ");
}


//
//
//

function FCyberAnchoredEdges_FParseLength( d )
{
	px = parseInt( d );
	if(isNaN(px)) return 0;

	return px;
}



//
//
//

function FCyberAnchoredEdges_FInCollection( coll, str )
{
	if( coll == null ) return false;

	var i;
	var size = coll.length;

	for( i=0; i<size; i++ ) {
		var cstr = coll[i];
		if( cstr == str ) return true;
	}

	return false;
}




//
//
//

function FCyberAnchoredEdges_FGetExtraStyleInfo( element, prop )
{
	var value = element.style.getAttribute(prop);

	return FCyberAnchoredEdges_FParseLength(value);
}



//
//
//

function FCyberAnchoredEdges_FDebugging()
{
	if( typeof(gB3Debugging) != "undefined" ) 
		return gB3Debugging;

	return false;
}


//
//
//

function FCyberAnchoredEdges_FDebuggingDetails()
{
	if( typeof(gB3DebuggingDetails) != "undefined" ) 
		return gB3DebuggingDetails;

	return false;
}


//////////////////////////////////////////////////////////////////////////////////////////
// [B3 Behaviors]: style="b3-behavior: url('Fuu.b3htc');"
//


//
//
//

function FB3_SetExtension( url, extension )
{
	var last = url.lastIndexOf( "." );

	if(last == -1) return url + "." + extension;

	var left = url.substring( 0, last );

	return left + "." + extension;
}


//
//
//

function FB3_GetName( url )
{
	var name = url;
	
	last = name.lastIndexOf( "/" );
	if(last != -1) name = name.substring( last+1 );

	var last = name.lastIndexOf( "." );
	if(last != -1) name = name.substring( 0, last );

	return name;
}





//
//
//

function FB3_ExtractCSSURL(url)
{
	if(!url) return url;

	if(url.substr( 0, 4 ) != "url(" ) return url;

	url = url.substr(4, url.length-5);
	
	if(url.charAt(0) == "'" ) 
		return url.substr(1, url.length-2);
	
	if(url.charAt(0) == "\"" ) 
		return url.substr(1, url.length-2);

	return url;
}




//
//
//

function FB3_CombinePaths( base, path )
{
	path = FB3_ExtractCSSURL(path);

	var service = path.substring( 0, 5 );

	if(service == "file:" || service == "http:" ) {

		return path;
	}

	if( base.charAt(base.length-1) == "/" )
		return base + path;

	return base + "/" + path;
}




// -FIX- There must be a better way
//
//

function FB3_EqualPaths( p1, p2 )
{
	re = /%20/g;        // -HACK- This just solved the problem with spaces - fix more!     
	p1 = p1.replace(re, " ");   
	p2 = p2.replace(re, " ");   

	if( p1 == p2 ) return true;

	if(!p1) return false;
	if(!p2) return false;

	var test;
	
	test = p1.substring( 0, 8 );
	if(test == "file:///"  ) 
		p1 = "file://" + p1.substring( 8, p1.length );
	
	test = p2.substring( 0, 8 );
	if(test == "file:///"  ) 
		p2 = "file://" + p2.substring( 8, p2.length );

	return p1 == p2;
}




//
//
//

var gB3BaseURL = null;
function FB3_GetBaseURL()
{
	if(!gB3BaseURL)
		gB3BaseURL = FB3_GetBaseURL1();

	return gB3BaseURL;
}


function FB3_GetBaseURL1()
{
	var b_IE50 = false;

	var base = "";
	var strurl = document.URLUnencoded;
	if (typeof(strurl) == "undefined")
	{
		// Aaargh, this is not implemented (*&*&%^%@^*@
		// strurl = document.URL; // IE5.0
		strurl = window.location.href;
		b_IE50 = true;
	}
	var last = strurl.lastIndexOf( "\\" ); // file:
	var last2 = strurl.lastIndexOf( '/' );
	if(last2 > last) last = last2;

	if( last >= 0 ) {

		var i;
		var len = strurl.length;
		var tmp = "";

		for(i=0; i<len; i++) {
			var cc = strurl.charAt(i);
			if( cc == '\\' )
				tmp += "/";
			else tmp += cc;
		}

		strurl = tmp;
	}

	if(last >= 0) {
		strurl = strurl.substring( 0, last );

		base = strurl;
		base += "/";
	}

    if (b_IE50)
	{
		base += "envoy/edit/snippets/";
	}

	return base;
}


//
//
//

function FB3_AttachBehaviorsDeep( root, preload )
{
	if( root == null ) root = document.body;

	FB3_AttachBehaviors(root, preload);

	var i;
	var size = root.children.length;
	for(i=0; i<size; i++) {
		
		var child = root.children[i];

		if(child.style == null) continue;

		FB3_AttachBehaviorsDeep(child, preload);
	}
}


//
//
//

function FB3_ParseCSSUrls( str )
{
	var array = new Array();
	var size = str.length;
	var i;
	var u = "";

	for(i=0; i<size; i++){
		
		var cc = str.charAt(i);

		if(cc == ')') {

			u += cc;
			
			array[array.length] = u;
			u = "";
			continue;
		}
		
		if( u == "" && cc == ' ') continue;
		
		u += cc;	
	}

	return array;
}


//
//
//

var gB3ScriptLoader = null; // -NEW-
function FB3_LoadScript( src )
{
	if(!gB3ScriptLoader ) {

		var size = document.scripts.length;
		var i;

		for(i=0; i<size; i++) {

			var script = document.scripts[i];

			if(script.src != "") {
				gB3ScriptLoader = script;
				break;
			}
		}

		if(!gB3ScriptLoader) {
			alert( "fatal Error; Can't locate Script Loader!" );
			return false;
		}
	}

	gB3ScriptLoader.src = src;
}


//
//
//

var goB3Preloaded = new Array();

function FB3_AttachBehaviors( element, preload )
{
	var size;
	var i;
	var load = !preload;

	if(!element && this == document) {

		element = document.scripts[document.scripts.length-1].previousSibling;
		load = false;
	}

	if(!element) return false;

	var urls = element.currentStyle.getAttribute( "b3-behavior" );
	if(!urls) urls = element.style.getAttribute( "b3-behavior" );

	if(!urls) return false;
	
	//debugger

	var array = FB3_ParseCSSUrls(urls);
	size = array.length;

	for(i=0; i<size; i++) {
	
		var url = array[i];
		
		if(!url) break;

		url = FB3_ExtractCSSURL(url);

		var absurl;

		var service = url.substring( 0, 5 );
		if(service == "http:" || 
		   service == "file:" ) {
	
			absurl = url;

		} else {
		
			absurl = FB3_GetBaseURL() + url;

		}

		var jsurl = FB3_SetExtension( absurl, "js" );
		var name = FB3_GetName( url );

		var attachfnname = "F" + name + "_Attach";
		var attachfntest = "b3TempHTC_" + name + "_Attached";
		var attachfn;

		if( element.getAttribute( attachfntest ) ) continue;

		//alert(attachfnname);

		//debugger

		var e;

		try {

			attachfn = eval(attachfnname);

			//alert( "Loaded!" );

		} catch(e) {

			if(load)
				FB3_LoadScript( jsurl );

		}

		if(preload) {

			if(!attachfn) {

				dowrite = true;
				var j;
				var jsize = goB3Preloaded.length;

				for(j=0; j<jsize; j++) {
					if( goB3Preloaded[j] == jsurl ) {
						dowrite = false;
						break;
					}
				}

				//alert("preload");
				if(dowrite) {
					goB3Preloaded[goB3Preloaded.length] = jsurl;
				 	document.writeln( "<script language=JavaScript src='" + jsurl + "'></script>" );
				}
			}

			continue;
		}

		if(load) {

			var e;

			try {

				attachfn = eval(attachfnname);

			} catch(e) {

				alert( "Failed to load b3htc " + absurl );

			}
		}

		if( attachfn ) {

			var args = new Array();
			args[0] = element;
			args[1] = absurl;

			//debugger
			attachfn.apply( element, args );

			element.setAttribute( attachfntest, true );
		}
	}
}




//
//
//

function FB3_FireEvent( element, event, evObj )
{
	if( event == "onchange" ) {

		return FB3_EvalHandler( element, event );
	}

	return element.fireEvent( event, evObj );
}




//
//
//

function FB3_GetLocalFunction( element, fn )
{
	if(typeof(fn) == "function") {
		fn = fn.toString();
		fn = fn.split(" ")[1];
		fn = fn.split("(")[0];
	}

	var cache = "b3TempHTC_" + fn;
	var old = element.getAttribute( cache );
	if(old && typeof(old) == "function") return old;

	var fnblock = new Function( fn + "(" + element.uniqueID + ")" );

	element.setAttribute( cache, fnblock );

	return fnblock;
}




//
//
//

function FB3_AttachEvent( element, eventname, fn )
{
	var fnblock = FB3_GetLocalFunction( element, fn );

	return element.attachEvent( eventname, fnblock );
}



//
//
//

function FB3_DetachEvent( element, eventname, fn )
{
	var fnblock = FB3_GetLocalFunction( element, fn );

	if(!fnblock) return false;

	return element.detachEvent( eventname, fnblock );
}


//
//
//

var goB3_OnFrameLoaded = new Array();

function FB3_SetupFramesForOnFrameLoaded()
{
	var i;
	var size = document.frames.length;
	for(i=0; i<size; i++) { 

		var frw = document.frames[i];		
		var fr = frw.frameElement;

		FB3_AddToEventHandler( fr, "onreadystatechange", "FB3_OnFrameReadyStateChange()" );
		//fr.onreadystatechange = FB3_OnFrameReadyStateChange;
	}
}

function FB3_RegisterForOnFrameLoaded(el)
{
	goB3_OnFrameLoaded[ goB3_OnFrameLoaded.length ] = el;
}


function FB3_UnregisterForOnFrameLoaded(el)
{
	var i;
	var size = goB3_OnFrameLoaded.length;

	for(i=0; i<size; i++) {

		var r = goB3_OnFrameLoaded[i];

		if(el == r) {
			goB3_OnFrameLoaded[i] = null;
			return true;
		}
	}

	return false;
}

function FB3_OnFrameLoaded(frame)
{
	var i;
	var size = goB3_OnFrameLoaded.length;

	for(i=0; i<size; i++) {

		var el = goB3_OnFrameLoaded[i];
		if(!el) continue;

		if(FB3_Implements(el, "OnFrameLoaded")) {
			el.onFrameLoaded(frame);
		}
	}
}

function FB3_OnFrameBeginDownload(frame)
{
	var i;
	var size = goB3_OnFrameLoaded.length;

	for(i=0; i<size; i++) {

		var el = goB3_OnFrameLoaded[i];
		if(!el) continue;

		if(FB3_Implements(el, "OnFrameLoaded")) {
			el.onFrameBeginDownload(frame);
		}
	}
}


function FB3_OnFrameReadyStateChange()
{
	var frame = window.event.srcElement;

	//alert(frame.readyState);

	if(frame.readyState == "loading") {

		FB3_OnFrameBeginDownload(frame);
		return;
	}

	if(frame.readyState == "complete") {

		FB3_OnFrameLoaded(frame);	
		return;
	}	
}


//
//
//

var goB3_TOCs = new Array();

function FB3_GetTOCContainer( src )
{
	var i;
	var size = goB3_TOCs.length;
	var x;

	for(i=0; i<size; i++) {
	
		x = goB3_TOCs[i];
		
		if(!x) continue;

		if(x.src == src ) {

			return x;
		}
	}

	//debugger
	
	x = document.createElement( "xml" );
	x.b3SkipOnSave = true;
	document.body.insertAdjacentElement( "beforeEnd", x ); 

	x.XMLDocument.async = false;
	x.XMLDocument.load( src );
	if(x.XMLDocument.firstChild == null) {
        return null;
    }

	//x.src = src;

	goB3_TOCs[ goB3_TOCs.length ] = x;

	return x;
}


//
//
//

function FB3_GetTOC( src, report )
{
	var xml = src;
	
	if( typeof(src) == "string" ) {
		xml = FB3_GetTOCContainer(src);
	}

	if(!xml || !xml.tagName || xml.tagName != "xml" ) {

		if(report) alert( "TOC: Missing XML data!" );
		return null;
	}

	var firstChild = xml.XMLDocument.firstChild;
	while( firstChild && firstChild.nodeType != 1 ) 
		firstChild = firstChild.nextSibling;

	// -FIX- Handle "doc type" as the first node too!

	if(firstChild && firstChild.nodeName == "html" ) {

		var coll = firstChild.getElementsByTagName("xml");

		firstChild = null;

		if(coll) {

			var subxml = coll[0];
			if(subxml) {

				firstChild = subxml.firstChild;

			}
		}
	}

	if(!firstChild || firstChild.nodeName != "toc" ) {

		if(report) alert( "Missing TOC root!" );
		return null;
	}

	return firstChild;
}




//
//
//

function FB3_GetTOCChapter( toc, src )
{
	if( typeof(toc) == "string" ) toc = FB3_GetTOC(toc);

	if(!toc) return null;

	if(toc.nodeType != 1) return null;

	var hrefNode = toc.attributes.getNamedItem( "href" );
	if(hrefNode) {

		var base = FB3_GetBaseURL();
		var href = FB3_CombinePaths( base, hrefNode.text );

		if( FB3_EqualPaths(src,href) ) return toc;	
	}

	var i;
	var length = toc.childNodes.length;

	for(i=0; i<length; i++) {

		var child = toc.childNodes.item(i);

		var found = FB3_GetTOCChapter( child, src );

		if( found ) return found;
	}

	if( toc.parentNode && toc.parentNode.nodeName == "toc" ) {

		for(i=0; i<length; i++) {

			var child = toc.childNodes.item(i);

			if(child.nodeName == "chapter") return child;
		}
	}

	return null;
}



// Try to find a loaded XML chapter for a url
//
//

function FB3_FindTOCChapter( src )
{
	var i;
	var size = goB3_TOCs.length;
	var xmlNode;

	for(i=0; i<size; i++) {
	
		xmlNode = goB3_TOCs[i];
		
		if(!xmlNode) continue;

		var toc = FB3_GetTOC( xmlNode, false );

		var found = FB3_GetTOCChapter( toc, src );

		if( found ) return found;
	}

	return null;
}


