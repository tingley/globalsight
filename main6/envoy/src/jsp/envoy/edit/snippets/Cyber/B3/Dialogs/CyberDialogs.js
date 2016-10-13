
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
	<minor>8</minor>
</version>
}}b3-meta*/

////////////////////////////////////////////////////////////////////////////////

//debugger

FB3_AttachBehaviorsDeep( null, true );

//
//
//

if(FCyberAnchoredEdges_AttachElements)
	FCyberAnchoredEdges_AttachElements();


//
//
//

if( FB3_InsertActiveXTrace() && !document.getElementById("CyberActiveXLog") ) {

	document.body.insertAdjacentHTML( 'BeforeEnd', '<object b3SkipOnSave classid=\"clsid:3f703800-9607-11d4-99ba-006097f86d7f\" id=CyberActiveXLog></object>' );
}


//
//
//

if( FB3_InsertTrace() && !document.getElementById("CyberLog") ) {

	var path = "Cyber/B3/Log/CyberLog.htc";

	if( typeof( gB3AppPath ) != "undefined" )
		path = gB3AppPath + "/Dialogs/" + path ;

	var html = '<div id=CyberLog b3SkipOnSave style="position:absolute;display:hidden;behavior:url(\'' + path + '\');"></div>';

	//alert(html);

	document.body.insertAdjacentHTML( 'BeforeEnd', html );
}


//
//
//

if( FB3_InCDialog() || 
    FB3_InEditor(/*undefined*/) || 
	FB3_InPalette(/*undefined*/) || 
	FB3_InHelper(/*undefined*/) ) {

	document.body.insertAdjacentHTML( 'BeforeEnd', '<object b3SkipOnSave classid=\"clsid:D1CFC025-6EB1-11d4-99B2-006097F86D7F\" id=CyberDialog></object>' );
}

if( !document.body.b3TempDisableB3Behaviors ) {

	if(!FB3_InCDialog()) {

		// Note that this is called from the C++ runtime for .cdlg files

		FB3_AddToEventHandler( document.body, "onload", "FB3_AttachBehaviorsDeep();" );
	}
}

if( !FB3_InEditor(/*undefined*/) && !FB3_InPalette(/*undefined*/) ) {

	if( !FB3_InCDialog() ) {

		document.body.insertAdjacentHTML( 'BeforeEnd', '<p b3SkipOnSave id=CyberDefaults type=hidden style="display: none; behavior:url(#default#userData);"></p>' );

		FB3_AddToEventHandler( document.body, "onload", "FB3_OnLoad();" );
		FB3_AddToEventHandler( document.body, "onresize", "FB3_WindowResized();" );
		FB3_AddToEventHandler( document.body, "onbeforeunload", "FB3_OnBeforeUnload();" );
		
	}

	FB3_AddToEventHandler( document.body, "onkeyup", "FB3_HandleDefaultControl();" );

	FB3_InitControls( document.body );

	FB3_SetupFramesForOnFrameLoaded();
}


gB3HasInit = true;




