/*************************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
* Copyright 1986 - 2009 Adobe Systems Incorporated
* All Rights Reserved.
*
* NOTICE:  All information contained herein is, and remains the property
* of Adobe Systems Incorporated and its suppliers, if any. The intellectual
* and technical concepts contained herein are proprietary to Adobe Systems
* Incorporated and its suppliers and may be covered by U.S. and Foreign
* Patents, patents in process, and are protected by trade secret or
* copyright law. Dissemination of this information or reproduction of this
* material is strictly forbidden unless prior written permission is
* obtained from Adobe Systems Incorporated.
**************************************************************************/

/*
 * Identifies the current version of the FDK libaries
 *             MMmmppbb: MM: major, mm: minor, pp: point, bb: build
 */
#define FDK_LIBRARY_VERSION  0x09000000

/* Settings for FA_errno */
#define FE_Success            0   /* All's well */ 
#define FE_Transport          -1   /* Communications is falling apart */ 
#define FE_BadDocId           -2   /* Illegal Document or Book */ 
#define FE_BadObjId           -3   /* Illegal Object */ 
#define FE_BadPropNum         -4   /* Current object doesn't have this property */ 
#define FE_BadPropType        -5   /* Property's type different than requested */ 
#define FE_ReadOnly           -6   /* Can't write into this property */ 
#define FE_OutOfRange         -7   /* Value not in legal range for property */ 
#define FE_DocModified        -8   /* Closing modified doc w/o FF_CLOSE_MODIFIED */ 
#define FE_GroupSelect        -9   /* Can't select/deselect object in group */ 
#define FE_WithinFrame        -10   /* Must implicitly move between frames first */ 
#define FE_NotGraphic         -11   /* Value must be an id of a Graphic object */ 
#define FE_NotFrame           -12   /* Value must be an id of a Frame object */ 
#define FE_NotGroup           -13   /* Value must be an id of a Group object */ 
#define FE_BadNewFrame        -14   /* Can't move given object to this Frame */ 
#define FE_BadNewGroup        -15   /* Can't move given object to this Group */ 
#define FE_BadNewSibling      -16   /* Can't make this prev/next connection */ 
#define FE_BadDelete          -17   /* Can't delete this kind of object */ 
#define FE_BadPageDelete      -18   /* Can't delete this page */ 
#define FE_TypeUnNamed        -19   /* Can't GetNamedObject of this type */ 
#define FE_NameNotFound       -20   /* Can't find object with requested name */ 
#define FE_OffsetNotFound     -21   /* Can't find requested offset */ 
#define FE_SomeUnresolved     -22   /* Some XRefs or Text Insets were  unresolved */ 
#define FE_BadNew             -23   /* Illegal NewFrameMakerObject call */ 
#define FE_NotBodyPage        -24   /* Expecting id of a Body Page object */ 
#define FE_NotPgf             -25   /* Expecting id of a Pgf object */ 
#define FE_NotBookComponent   -26   /* Expecting id of a Book Component object */ 
#define FE_BadOperation       -27   
#define FE_BadElementDefId    -28   /* Expecting id of an ElementDef */ 
#define FE_BadElementId       -29   /* Expecting id of an Element */ 
#define FE_BadNotificationNum -30   /* Bad Notification number */
#define	FE_BadContainer       -104   /* Bad Folder/Group book component. */
#define	FE_BadTemplatePath    -105	 /* Bad Template path for book component. */
#define	FE_BadXmlApplication  -106   /* Bad Application for XML file. */	


#define FE_DupName       -32 /* A same-type item of this name exists */
#define FE_BadName       -33 /* Trying to give an object an illegal name */
#define FE_CompareTypes  -34 /* Can only compare book to book or doc to doc */
#define FE_BadCompare    -35 /* Compare operation failed */
#define FE_BadRange      -36 /* Two ends of range not in same flow or hidden */
#define FE_PageFrame     -37 /* PageFrames can't be moved or selected */
#define FE_CantSmooth    -38 /* Can't smooth/unsmooth this object */
#define FE_NotTextFrame  -39 /* Value must be an id of a TextFrame object */ 
#define FE_HiddenPage    -40 /* Value must be an id of a non-hidden page */ 
#define FE_NotTextObject -41 /* Expecting id of a FO_Pgf, FO_TextLine,
                              * FO_Flow, FO_Cell, FO_TextFrame, FO_SubCol,
                              * FO_Fn, FO_Element, FO_XRef, FO_Var, FO_TiFlow,
                              * FO_TiText, FO_TiTextTable, FO_TiApiClient
                              */ 

/*
 * Possible values FA_errno may have after a call to F_ApiOpen() or F_ApiSave()
 * FE_Success indicates the document was opened/saved.
 */
#define FE_SystemError    -42 /* Unable to open the document due to system 
                               * error.  Check errno. 
                               */
#define FE_BadParameter   -43 /* Parameter passed to an API function was 
                               * invalid.
                               */
#define FE_Canceled       -44 /* User canceled operation. */
#define FE_FailedState    -45 /* Document was in an inconsistent state.*/
#define FE_WantsCustom    -46 /* User selected custom from the 
                               * template browser.
                               */
#define FE_WantsLandscape -47 /* User selected landscape from the
                               * template browser.
                               */
#define FE_WantsPortrait  -48 /* User selected portrait from the
                               * template browser.
                               */

/*
 * In addition FA_errno may this value have after a call to F_ApiSave() 
 */
#define FE_ViewOnly       -49  /* OBSOLETE in 5.0 */
/* In 5.0 and beyond FA_errno is  FE_WrongProduct */

/*
 * Other values FA_errno may have after call to F_ApiSilentPrintDoc()
 * FE_Success indicates the document was printed.
 */
#define FE_BadSaveFileName -50

/* 
 * Error values from structure operations
 * IMPORTANT: General errors must be defined in the same
 * order as PARSE_E_ errors in validation/fm_parser.h as
 * api code assumes this.
 */
#define FE_GenRuleItemExpected          -51 /* general rule has syntax error */
#define FE_GenRuleMixedConnectors       -52 /* general rule has syntax error */
#define FE_GenRuleLeftBracketExpected   -53 /* general rule has syntax error */
#define FE_GenRuleRightBracketExpected  -54 /* general rule has syntax error */
#define FE_GenRuleAmbiguous             -55 /* general rule has syntax error */
#define FE_GenRuleSyntaxError           -56 /* general rule has syntax error */
#define FE_GenRuleConnectorExpected     -57 /* general rule has syntax error */

#define FE_InvalidString                -58 /* spec has syntax error */

#define FE_BadSelectionForOperation     -59 /* selection in doc not valid for 
                                             * operation 
                                             */

#define FE_WrongProduct                 -60 /* Can't access this object 
                                             * type in current product
                                             */

/* Insert Element */
#define FE_BookStructured   -61  /* UNUSED */
#define FE_BadRefFlowId     -62  /* UNUSED */
#define FE_FlowStructured   -63  /* UNUSED */
#define FE_BadRefElementId  -64  /* UNUSED */

#define FE_BadInsertPos     -65  /* bad insertion position */
#define FE_BadBookId        -66  /* bad book id specified */
#define FE_BookUnStructured -67  /* book is unstructured */
#define FE_BadCompPath      -68  /* bad book component path specified */

/* For set element selection */
#define FE_BadElementSelection -69 /* bad element selection specified */

#define FE_FileClosedByClient  -70  /* File was closed by an apiclient when
                                     * it processed a notification.
                                     */
#define FE_NotPgfOrFlow             -71   /* Expecting id of a Pgf or Flow */ 
/*
 * ConfigUI Error Messages
 */
#define FE_NotMenu          -72  /* Expecting id of a FO_Menu */
#define FE_NotCommand       -73  /* Expecting id of a FO_Command */
#define FE_NotApiCommand    -74  /* Expecting id of a FO_Command defined
                                  * by an api client
                                  */
#define FE_NotInMenu        -75  /* Menu item (FO_Command or FO_Menu) 
                                  * is not in menu */
#define FE_BadShortcut      -76  /* Expecting a valid keyboard shortcut */
#define FE_BadMenuBar       -77  /* Expecting a menu to contain menus only*/


#define FE_PropNotSet       -78  /* prop not set on FmtChangeList */
#define FE_InvAttributeDef  -79  /* F_AttributeDefT values bad */
#define FE_InvAttribute     -80  /* F_AttributeT values bad */

/* F_ApiImport() Error Messages */
/* FE_Success, FE_Canceled,  FE_SystemError, FE_BadParameter, FE_FailedState,
 * FE_FileClosedByClient, FE_CanceledByClient
 */
#define FE_CircularReference     -81 /* Importing document would cause
                                      * a circular reference.
                                      */
#define FE_NoSuchFlow            -82 /* Requested flow did not exist
                                      * in the source document.
                                      */
#define FE_BadFileType           -83 /* The type of the file on disk 
                                      * was not the type of file
                                      * the import operation expected.
                                      * Or the type F_ApiUpateTextInset()
                                      * expected based on the inset.
                                      */
#define FE_MissingFile           -84 /* The file no longer exists on disk */

/* F_ApiUpdateTextInset() Error Messages */
/* Everything returned by F_ApiImport() */
#define FE_CantUpdateMacEdition  -85 /* The Inset is a Mac Edition, but
                                      * we aren't running on a Mac.
                                      */
#define FE_CanceledByClient      -86 /* An API Client canceled the operation */
#define FE_EmptyTextObject       -87 /* Object has no text in it */
#define FE_Busy                  -88 /* FM not in safe state for
                                      * asynchronous invocation
                                      */
#define FE_FilterFailed          -89
#define FE_AsianSystemRequired   -90 /* Asian capable system required */
#define FE_TintedColor           -91 /* Can't change tinted color this way */
#define FE_NoColorFamily         -92 /* Can't Set Ink Name without 
                                      * Color Family 
                                      */
#define FE_StringTooLong         -93 /* String exceeds max length for
                                      * property, truncated 
                                      */
#define FE_InternalErrorFailedToWriteInsets		-94		
									 /* Internal code to move Graphic Inset data
									  * from current document to a file failed,
									  * leaving user with a file with missing data.
									  * This is an incomplete, unsuccessful Save.
									  */
#define FE_NotFound				-95  /*Returned by F_ApiFind.*/
/* NB Used by experimental interfaces. */
#define FE_LanguageNotAvailable -96  /* Return by F_ApiThesaurus */
/* End experimental */

#define FE_BadBaseColor         -97  /* Can't set a color's base color to be a tint or the color itself */
#define FE_BadFamilyName        -98  /* No color library available with specified name. */
#define FE_BadInkName           -99  /* No ink with specified name found in specified color library */
#define FE_ReservedColor       -100  /* Cannot change most properties of a reserved color */
#define FE_TableInLockedTi	   -101  /* Table is in locked text inset. Hence it cannot be edited. */

#define FE_XRefUnresolved      -102  /* The XRef cannot be resolved. */ 
#define FE_BadXRefSrcDocId     -103  /* Illegal Document or Book specified as the xref source.*/
#define FE_InvalidAttrExpr	   -107  /* The FBA Expression supplied to apply to the document, is invalid */

/* Don't forget to add your new error code to F_ApiErrorName dee*/

#ifdef FAPI_4_BEHAVIOR
#define FE_NotTextCol    FE_NotTextFrame
#endif


/* Values clients can return to FrameMaker*/
#define FR_DialogStayUp         -10000	/* How a client lets FM know that
										 * it wants its dialog to stay up
										 * following a dialog event.
										 */
#define FR_ModalStayUp	FR_DialogStayUp	/* These should all be converted 
										 * FR_DialogStayUp as time permits.
										 */
#define FR_CancelOperation		-10001	/* How a client lets FM know that
										 * the upcoming event should be 
										 * cancelled. Only sent in response to
										 * a prefunction notification.
										 */
#define FR_DisplayedTiDialog	-10002	/* How a client lets FM know that
										 * it displayed an APIClient version
										 * of the text inset properties
										 * dialog.
										 */
#define FR_ECMImportSuccess	    -10003  /* ECM client
										 * Frame Internal Use Only
										 */

#define FR_SkipStep				-10004	/* Client wants to skip the next step
										 * in a process without cancelling the
										 * entire operation. Behavior is specific
										 * to a particular notification.
										 */
#define FR_YesOperation			-10005  /* How a client lets FM know that the
										 * upcoming  alert should be
										 * "Yesed/OKed"
										 * (experimental interface)
										 */

#define FR_NoOperation			-10006  /* How a client lets FM know that the
										 * upcoming alert should be "Noed".
										 * (experimental interface)
										 */

#define FR_DisplayedXRefDialog	-10007	/* How a client lets FM know that
										 * it displayed an APIClient version
										 * of the XRef properties dialog.
										 */

#define FR_ClosedXRefDialog		-10008	/* How a client lets FM know that
										 * it closed an APIClient version
										 * of the XRef properties dialog,
										 * if it was open.
										 */

#define FR_DisplayedModelessDialogForNonContainerElem	-10009	
										/* How a client lets FM know that
										 * it displayed the modeless dialog (if any)
										 * required to get the properties while 
										 * inserting the non-container element.
										 */

#define FR_CancelInsertElementOperation			-10010
										/* If the client performs an undo operation in post-insert element
										then it will set this return value.
										*/

/* Values for FA_Note_Palette (experimental interface) */
#define FV_ACCESSBAR_OPEN	1
#define FV_ACCESSBAR_CLOSE	2
#define FV_MATH				3
#define FV_FORMATBAR_OPEN	4
#define FV_FORMATBAR_CLOSE	5
#define FV_CHAR_CATALOG		6
#define FV_PGF_CATALOG		7
#define FV_ELEMENT_CATALOG	8
#define FV_GRAPHIC			9
#define FV_THESAURUS		10
#define FV_TEMPLATE_BROWSER	11
#define FV_HIST				12

/* Values for FA_Note_Help (experimental interface) */
#define FV_HELP_INDEX					0
#define FV_HELP_KEYS					1
#define FV_HELP_SAMPLES					2
#define FV_HELP_OVERVIEW				3
#define FV_HELP_ONLINE_MANUALS			4
#define FV_HELP_CONTEXT					5
#define FV_HELP_INIT_CONTEXT			6
#define FV_HELP_HYPERTEXT				7
#define FV_HELP_WEBWORKS				8
#ifdef FAPI_6_BEHAVIOR
#define FV_HELP_KYLE					FV_HELP_WEBWORKS
#endif /* FAPI_6_BEHAVIOR */

/* FO_Alert */
/* #define FP_Unique	*/
/*FO_Alert(experimental interface) */
#define FP_AlertClientUnique 2290	/* R/W IntT */
#define FP_AlertClientName   2291   /* R/W StringT*/
#define FP_AlertString	     2292	/* R/O StringT*/
#define FP_AlertType	     2293	/* R/O IntT enumerated below: */

/* Kind of Alert in a FA_Note_Alert notification, and in FO_Alert object.*/
#define FV_AlertOneButton	  1
#define FV_AlertTwoButton	  2
#define FV_AlertThreeButton	  3

/* Use to place your alert id in the high order 24 bytes of
 * the integer parameter to F_ApiAlert(). It will be used
 * for the value of FP_AlertClientUnique. Your client
 * name is placed in FP_AlertClientName.
 * NB experimental interface
 */
#define PackAlertParam(alertType, alertId) ((UIntT)(alertType)+(((UIntT)(alertId))<<8))

/* Flags for F_ApiAlert() */
#define FF_ALERT_OK_DEFAULT                0   
#define FF_ALERT_CANCEL_DEFAULT            1   
#define FF_ALERT_CONTINUE_NOTE             2   
#define FF_ALERT_CONTINUE_WARN             3   
#define FF_ALERT_YES_DEFAULT               4  
#define FF_ALERT_NO_DEFAULT            	   5   
#define FF_ALERT_YES_NO_CANCEL             6   

/* Flags for F_ApiUpdateXrefs() */	
#define FF_XRUI_INTERNAL             ((IntT)   0x01)
#define FF_XRUI_OPEN_DOCS            ((IntT)   0x02)
#define FF_XRUI_CLOSED_DOCS          ((IntT)   0x04)
#define FF_XRUI_FORCE_UPDATE          ((IntT)  0x08) 
#define FF_XRUI_EVERYTHING 	 (FF_XRUI_INTERNAL|FF_XRUI_OPEN_DOCS|FF_XRUI_CLOSED_DOCS)

/* Flags for F_ApiCustomDoc() */
#define FF_Custom_SingleSided     0   
#define FF_Custom_FirstPageRight  1   
#define FF_Custom_FirstPageLeft   2   

/* Flags for F_ApiClose () */
#define FF_CLOSE_MODIFIED         1   /* OK to exit doc even if it's modified */ 

/* Flags for F_ApiCompare */
#define FF_CMP_SUMMARY_ONLY 0x01    /* Generate summary doc, but not composite */
#define FF_CMP_CHANGE_BARS  0x02    /* Turn on change bars in composite document */
#define FF_CMP_HYPERLINKS  0x04    /* Put hypertext links in summary document */
#define FF_CMP_SUMKIT      0x08    /* Put Summary doc into a kit (API, not UI) */
#define FF_CMP_COMPKIT     0x10    /* Put Comp doc into a kit (API, not UI) */

/* Flags for F_ApiImageGraphic (experimental interface) */
#define FF_IMAGE_BACKGROUND			0x0001	/* image background if page frame */
#define FF_IMAGE_MASTER				0x0002	/* image master page if page frame */
#define FF_IMAGE_CLIP				0x0004	/* clip to boundery if frame */
#define FF_IMAGE_NOTRIM				0x0008	/* special magic */
#define FF_IMAGE_PAGEFRAME			0x000F	/* standard flags for drawing page frame */

/* Values for F_ApiUserInteractObject (experimental interface) */
#define FV_INTERACT_ROTATE				1	/* Rotates object unconstrained */ 
#define FV_INTERACT_ROTATE_CONSTRAIN	2	/* Rotates object snapped to 45 degree intervals */

/* Flags for F_ApiAttachDocToWindowEx (experimental interface) */
#define FF_ATTACH_BORDER_STATUS		0x0001	/* Status line in bottom border */
#define FF_ATTACH_BORDER_PAGENUM	0x0002	/* Page number in bottom border */
#define FF_ATTACH_BORDER_ZOOM		0x0004	/* Zoom buttons in bottom border */
#define FF_ATTACH_BORDER_PAGEUPDOWN	0x0008	/* Page up/down buttons in bottom border */
#define FF_ATTACH_BORDER_RIGHTICONS	0x0010	/* Icon buttons in right border */
#define FF_ATTACH_ALL	(FF_ATTACH_BORDER_STATUS | FF_ATTACH_BORDER_PAGENUM | FF_ATTACH_BORDER_ZOOM | FF_ATTACH_BORDER_PAGEUPDOWN | FF_ATTACH_BORDER_RIGHTICONS )

/* Flags for F_ApiSimpleImportFormats */
#define FF_UFF_PGF					0x0001
#define FF_UFF_FONT					0x0002
#define FF_UFF_PAGE					0x0004
#define FF_UFF_TABLE				0x0008
#define FF_UFF_COND					0x0010
#define FF_UFF_REFPAGE				0x0020
#define FF_UFF_VAR					0x0040
#define FF_UFF_XREF					0x0080
#define FF_UFF_COLOR				0x0100
#define FF_UFF_MATH					0x0200
#define FF_UFF_DOCUMENT_PROPS		0x0400
#define FF_UFF_COMBINED_FONTS		0x0800
#define FF_UFF_REMOVE_PAGE_BREAKS	0x4000 
#define FF_UFF_REMOVE_EXCEPTIONS	0x8000

/* Flags for F_ApiCut(), F_ApiPaste(), F_ApiCopy(), F_ApiClear() 
 * A flag of 0 defaults to not interactive, leave table cells empty
 * insert left of current column, insert above current row, delete hidden
 * text, selected object doesn't have to be visible on screen.
 */
#define FF_INTERACTIVE   			0x0001 /* Display alerts/dialogs. 		*/
#define FF_CUT_TBL_CELLS			0x0002 /* Cut selected cells from table.*/
#define FF_DONT_DELETE_HIDDEN_TEXT	0x0004 /* Don't cut hidden text.		*/
#define FF_INSERT_BELOW_RIGHT 		0x0008 /* Paste below/right selected 
											* row/col.		
											*/
#define FF_VISIBLE_ONLY				0x0010 /* Selection must be visible on 
											* screen.			
											*/
#define FF_REPLACE_CELLS			0x0020 /* Replace selected cells		 */
#define FF_DONT_APPLY_ALL_ROWS		0x0040 /* Don't apply Condition setting 
											* to all rows (whole table 
											* is selected - cancel paste).
		                                   	*/
#define FF_STRIP_HYPERTEXT			0x0080 /* Strip hypertext markers when
											* copying from a view-only doc
		                                   	*/
/*Flags for F_ApiMakeTableSelection*/
#define FF_SELECT_WHOLE_TABLE		0x80000000 /* Pass this value in for the 
												* topRow parameter.
												*/
/*Flags for the mouse action and key modifiers which accompany the FA_NotePreMouseCommand notification.*/
/* Mouse Actions */
#define FF_CLEAR_SEL		 0  /* remove any existing selection */
#define FF_TEXT_SEL		 1  /* start selecting text */
#define FF_TEXT_EXT	 	 2  /* extend a text selection */
#define FF_TEXT_Q_COPY   	 3  /* quick-copy text */
#define FF_CONTEXT_DRAGTXT  4	/* do a context sensitive popup menu for text */
#define FF_PASTE_SEL_TEXT	11  /* paste the selection (from another window) */
#define FF_PASTE_CLIP_TEXT	12  /* paste from the clipboard into text */
#define FF_COPY_TO_CLIP	13  /* copy the current sel to the clipboard */
#define FF_CELL_SEL        21  /* select a cell as an object */
#define FF_CELL_EXT        22  /* extend a Cell to a Cell */
#define FF_TABLE_SELALL    23  /* select a whole table */
#define FF_CELL_RES        24  /* resize a cell, changing the table size */
#define FF_CELLS_RES       25  /* resize a cell, changing the next cell's size */
#define FF_VIEWER_MENU		32  /* put up the viewer popup menu */
#define FF_CONTEXT_MENU	33	/* put up a context sensitive popup menu, no dragging */
#define FF_STRUCTURE_MENU	34  /* put up the structured maker popup menu */
#define FF_HYPERTEXT		41	/* do a hypertext command */

/* SFM Structure View Commands */
#define FF_ELEMENT_CONTEXT 47  /* context menu on element selection */
#define FF_ELEMENT_EXT_CONTEXT 48 /* context menu after extend element seln */
#define FF_BUBBLE_CONTEXT  49  /* context menu on bubble selection, no dragging */
#define FF_BUBBLE_EXT_CONTEXT  50 /* context menu after extend bubble selection, no dragging */
#define FF_ELEMENT_SEL		51	/* Start selecting element */
#define FF_ELEMENT_EXT		52	/* extend element selection */
#define FF_BUBBLE_SEL		53	/* bubble selection or move */
#define FF_BUBBLE_EXT		54	/* bubble extend */
#define FF_BUBBLE_COPY		55	/* bubble copy */
#define FF_COLLAPSE_ELEM	56  /* collapse element */
#define FF_COLLAPSE_ALL	57  /* collapse all siblings */
#define FF_OPEN_BOOK_COMP	58  /* open book file component */

#define FF_PASTE_CLIP_OBJ	61  /* paste from the clipboard into non-text */
#define FF_OBJ_SEL			62  /* select an object */
#define FF_OBJ_EXT			63  /* extend an object selection, drag does a move-obj */
#define FF_OBJ_EXT_BOR		64  /* extend an object selection, force border if drag */
#define FF_OBJ_Q_COPY		65  /* quick copy an object */
#define FF_OBJ_ROTATE      66  /* rotate an object */
#define FF_OBJ_CROTATE     67  /* rotate an object, constrained */
#define FF_BOR_SEL		 	71  /* start a selection border */
#define FF_BOR_EXT		 	72  /* start an extension border */
#define FF_CONTEXT_DRAG 	73	/* do a context sensitive popup menu or drag with popup */
#define FF_CONTEXT_DRAG_EXT 74	/* ditto, extending selection */

/* Reshape regions are anywhere on a reshapable object's boundary- not just on
 *    handles (so can add a reshape vertex).  For the following 2, if not in a
 *    a handle, they change the mode to object and try again.
 */
#define FF_RES_MOVE		81  /* move a reshape handle */
#define FF_RES_MOVE1		82  /* do a 1-sided move of a reshape handle */
#define FF_RES_VERTEX		83  /* add/delete a reshape vertex */
#define FF_RES_ADD			84  /* add a reshape vertex */
#define FF_RES_DEL			85  /* delete a reshape vertex */

/* Key modifiers */
#define	FF_SHIFT_KEY	0x0001 /* All platforms.*/
#define	FF_CONTROL_KEY	0x0002 /* All Platforms.*/
#define	FF_ALT_KEY 		0x0004 /* Alt key UNIX/Win, option key	Mac*/
#define	FF_CMD_KEY		0x0010 /* Command key, mac only	*/		

/*
 * More structure view commands
 */
#define FF_ATTRDISP		86	/* cycles through attribute display mode */
#define FF_ATTRDISP_ALL	87	/* cycles through attribute display mode for all */
#define FF_EDIT_ATTRIBUTE	88	/* quick edit of one attribute */
#define FF_ATTR_SEL		89	/* select attribute */

/* Parameters for F_ApiOpen() */
#define FS_ShowBrowser					1	/* Boolean Default False  Shared I */ 
#define FS_AlertUserAboutFailure		2	/* Boolean Default False Shared I/S */
#define FS_DontNotifyAPIClients			3	/* Boolean Default False Shared I/S */
#define FS_UpdateBrowserDirectory		4	/* Boolean Default False  */ 
#define FS_MakeVisible					5	/* Boolean Default True  */
#define FS_MakeIconic					6	/* Boolean Default False  */
#define FS_DisallowMIF					7	/* Boolean Default False Shared I */
#define FS_DisallowDoc					8	/* Boolean Default False Shared I */
#define FS_DisallowBookMIF				9	/* Boolean Default False */
#define FS_DisallowBookDoc				10	/* Boolean Default False */
#define FS_DisallowFilterTypes			11	/* Boolean Default False Shared I */
#define FS_DisallowPlainText			12 	/* Boolean Default False Shared I */

#define FS_ForceOpenAsText 				13	/* Boolean Default False  If set to True
											 * value of FS_FileIsText is used to determine
											 * how to handle end of lines.
											 */

#define FS_UseRecoverFile				14  /* Enum Default FV_DoCancel Also allowable: 
											 * FV_DoShowDialog, 
											 * FV_DoNo, FV_DoYes
											 */
#define FS_UseAutoSaveFile				15	/* Enum Default FV_DoCancel Also allowable: 
											 * FV_DoShowDialog, 
											 * FV_DoNo, FV_DoYes
											 */
#define FV_DoCancel 0					
#define FV_DoOK		1
#define FV_DoYes	2
#define FV_DoNo		3
#define FV_DoShowDialog	4
#define FV_OpenViewOnly	5

#define FS_FileIsText				16	/* Enum Default FV_TextFile_EOLisEOP also 
										 * allowable: FV_TextFile_EOLisNotEOP, 
										 * FV_DoCancel, FV_DoShowDialog,
										 */
#define FV_TextFile_EOLisEOP 	12
#define FV_TextFile_EOLisNotEOP 13

#define FS_FileIsInUse		17	/* Enum Default FV_OpenViewOnly
								 * FV_OpenEditableCopy, FV_DoShowDialog,
								 * FV_ResetLockAndContinue, 
								 * FV_DoCancel,  
								 */
#define FV_ResetLockAndContinue 7
#define FV_OpenEditableCopy     8

#define FS_BookIsInUse			18 /* Enum Default FV_DoCancel also
									* allowable FV_DoShowDialog, 
									* FV_ResetLockAndContinue
									*/
#define FS_LockCantBeReset		19  /* Enum Default FV_DoCancel Also allowable:
									 * FV_DoShowDialog, FV_DoOK
									 */
#define FS_FileIsOldVersion		20	/* Enum Default FV_DoCancel Also allowable: 
										 * FV_DoShowDialog, FV_DoOK,
										 */

#define FS_FileIsStructured		21	/* Enum Default FV_OpenViewOnly 
									 * Also allowable: FV_DoShowDialog,
									 * FV_DoCancel, FV_StripStructureAndOpen
									 */
#define FV_StripStructureAndOpen 10

#define FS_FontNotFoundInDoc		22 /* Enum Default FV_DoCancel Also allowable: 
										*  FV_DoOK, FV_DoShowDialog
										*/

#define FS_FontChangedMetric		23 /* Enum Default FV_DoCancel Also allowable: 
										*  FV_DoOK, FV_DoShowDialog
										*/

#define FS_RefFileNotFound	24 /* Enum Default: FV_DoCancel, 
										* also allowable: FV_AllowAllRefFilesUnFindable, 
										* FV_DoShowDialog
										*/
#define FV_AllowAllRefFilesUnFindable 11


#define FS_LanguageNotAvailable		25	/* Enum Default FV_DoCancel, Also allowable:
										 * FV_DoOK, FV_DoShowDialog
										 */

#define FS_OpenInNewWindow			26	/*Boolean Default True. */
#define FS_OpenId					27  /* Id. If FS_OpenInNewWindow is False,
										   then FS_OpenId contains the Id of the
										   document currently using the window you
										   want the new document opened into.*/

#define FS_FontNotFoundInCatalog	28 /* Enum Default FV_DoCancel Also allowable: 
										*  FV_DoOK, FV_DoShowDialog
										*/


#define FS_NewDoc					29  /* Boolean default False */
#define FS_OpenDocViewOnly			30  /* Boolean default False */
#define FS_NameStripe				31  /* StringT default NULL */
#define FS_BeefyDoc					32	/* Enum Default FV_DoCancel Also allowable:
										 * FV_DoOK, FV_DoShowDialog
										 */
#define FS_DisallowSgml				33 	/* Boolean Default False Shared I */
#define FS_FileTypeHint				34  /* String to autorecognize file Shared I */
#define FS_UpdateTextReferences		35  /* Enum default FV_DoUserPreference
										 * Also allowable, FV_DoYes FV_DoNo
										 */
#define FS_UpdateXRefs				36  /* Enum default FV_DoUserPreference
										 * Also allowable, FV_DoYes FV_DoNo
										 */
#define FS_OpenDocFluid				37  /* Boolean default False */

#define FV_DoUserPreference 12

#define FS_StructuredOpenApplication	38
#ifdef FAPI_6_BEHAVIOR
#define FS_SgmlOpenApplication			FS_StructuredOpenApplication
#endif /* FAPI_6_BEHAVIOR */

#define FS_SgmlBookFileName				39	/* String default NULL */
#define FS_OpenFileNotWritable  		40	/* Enum Default FV_Cancel.
										* Also allowable:
										*  FV_DoShowDialog, FV_DoOK
										*/

#define FS_OpenAsType				41  /* Enum Default FV_AUTORECOGNIZE */ 
#define FV_AUTORECOGNIZE					0x00
#define FV_TYPE_BINARY						0x01
#define FV_TYPE_MIF							0x02
#define FV_TYPE_TEXT						0x03
#define FV_TYPE_SGML						0x04
#define FV_TYPE_XML							0x05
#define FV_TYPE_FILTER						0xFF

#define FS_OpenBookViewOnly			42 /* Boolean default False */
#define FS_DisallowXml				43 /* Boolean Default False Shared I */
#define FS_NoStructuredErrorLog		44
#define FS_NumOpenParams       		FS_NoStructuredErrorLog 
#define FS_PDFPageNum				45


/* Return parameters from F_ApiOpen() */
#define FS_OpenedFileName						1   /* String */
#define FS_OpenNativeError						2   /* Int*/
#define FS_OpenStatus							3	/* Bit Field */
#define FS_NumOpenReturnParams				    FS_OpenStatus
#define FV_NumOpenStatusFields					5   /* Size of OpenStatus */

/* FA_errno is FE_Success, FE_Canceled FE_CanceledByClient or FE_FailedState*/
#define FV_LockWasReset			0
#define FV_LockNotReset 		1
#define FV_LockCouldntBeReset	2
#define FV_FileWasInUse			3
#define FV_FileIsViewOnly		4
#define FV_LockWasInvalid		5
#define FV_FileIsNotWritable	6
#define FV_FileModDateChanged	7

/* Something opened. FA_errno() is FE_Success */
#define FV_FileHasNewName			32
#define FV_RecoverFileUsed			33
#define FV_AutoSaveFileUsed			34
#define FV_FileWasFiltered			35
#define FV_FontsWereMapped			36
#define FV_FontMetricsChanged		37
#define FV_FontsMappedInCatalog		38
#define FV_LanguagesWerentFound		39
#define FV_BeefyDoc					40
#define FV_FileIsOldVersion			41
#define FV_FileStructureStripped	42
#define FV_FileIsText				43
#define FV_OpenedViewOnly			44
#define FV_EditableCopyOpened		45
#define FV_BadFileRefsWereMapped	46
#define FV_ReferencedFilesWerentFound	47
#define FV_FileAlreadyOpen			48
#define FV_UnresolvedXRefs			49
#define FV_UnresolvedTextInsets		50
#define FV_OpenedFluid				51
/* _CouldNotConvertTextToTable purposely not added. */
#define FV_FontsWithUnavailableEncodingsUsed	53

/* Nothing opened. FA_errno is FE_BadParameter */
#define FV_FileHadStructure			64
#define FV_FileAlreadyOpenThisSession	65
#define FV_BadFileType				66
#define FV_BadFileName				67
#define FV_CantNewBooks				68
#define FV_CantOpenBooksViewOnly	69			/* This is now OBSOLETE */
#define FV_BadScriptValue			70
#define FV_MissingScript			71
#define FV_CantForceOpenAsText		72 
#define FV_DisallowedType			73
#define FV_DocDamagedByTextFilter	74
#define FV_DocHeadersDamaged		75
#define FV_DocWrongSize				76
#define FV_ChecksumDamage			77
#define FV_CantOpenBooksFluid		78

/* Nothing opened. FA_errno is FE_Canceled*/
#define FV_CancelUseRecoverFile		96
#define FV_CancelUseAutoSaveFile	97
#define FV_CancelFileIsText			98
#define FV_CancelFileIsInUse		99
#define FV_CancelFileHasStructure	100
#define FV_CancelReferencedFilesNotFound	101
#define FV_CancelLanguagesNotFound			102
#define FV_CancelFontsMapped				103
#define FV_CancelFontMetricsChanged			104
#define FV_CancelFontsMappedInCatalog		105
#define FV_CancelFileIsDoc					106
#define FV_CancelFileIsMIF					107
#define FV_CancelBook						108
#define FV_CancelBookMIF					109
#define FV_CancelFileIsFilterable			110
#define FV_CancelFileIsOldVersion			111
#define FV_UserCanceled						112
#define FV_CancelFileBrowser				113
#define FV_CancelBeefyDoc					114
#define FV_CancelFileIsSgml					115
#define FV_CancelFontsWithUnavailableEncodings 116
#define FV_CancelOpenFileNotWritable		117
#define FV_CancelTempDiskFull				118		/* NB obsolete */
#define FV_CancelFileIsXml					119


/* Nothing opened FA_errno FE_SystemError */
#define FV_TooManyWindows	128
#define FV_BadTemplate		129
#define FV_FileNotReadable  130

/* Parameters for F_ApiSave()*/

#define FS_FileType 					1	/* Enum Default FV_SaveFmtBinary
											 * Also allowable: 
											 * FV_SaveFmtInterchange
											 * FV_SaveFmtStationery, 
											 * FV_SaveFmtViewOnly,
											 * FV_SaveFmtText, 
											 * FV_SaveFmtSgml,
											 * FV_SaveFmtPdf,
											 */
#define FV_SaveFmtBinary		0
#define FV_SaveFmtInterchange	1
#define FV_SaveFmtStationery 	3
#define FV_SaveFmtViewOnly 	 	4
#define FV_SaveFmtText			6
#define FV_SaveFmtSgml			7
#define FV_SaveFmtFilter		8
#define FV_SaveFmtPdf		 	9
#define FV_SaveFmtXml			10
#define FV_SaveFmtBinary60		11	
#define FV_SaveFmtBinary70		12	
#define FV_SaveFmtBinary80		13
#define FV_SaveFmtInterchange70	14
#define FV_SaveFmtInterchange80	15
#define FV_SaveFmtBinary90		16		/* pranav - added for fm9 */
#define FV_SaveFmtInterchange90	17		/* pranav - added for fm9 */
#define FV_SaveFmtCompositeDoc	18
#define FV_SaveFmtBookWithXml	19
#define FV_SaveFmtBookWithFm	20

/*#define FS_AlertUserAboutFailure		2   Same as defined for F_ApiOpen() */
/*#define FS_DontNotifyAPIClients		3	Same as defined for F_ApiOpen() */
#define FS_SaveMode						4	/* Enum Default value FV_ModeSaveAs. Also 
											 * allowable FV_ModeSave.
											 */
#define FV_ModeSave 			0
#define FV_ModeSaveAs 			1

#define FS_SaveAsModeName				5 /* Enum Default FV_SaveAsNameProvided. Also 
										   * allowable: FV_SaveAsNameAskUser, 
										   * FV_SaveAsUseFileName
										   */

#define FV_SaveAsNameAskUser	0
#define FV_SaveAsUseFileName	1
#define FV_SaveAsNameProvided	2

#define FS_AutoBackupOnSave				6 /* Enum Default FV_SaveUserPrefAutoBackup. Also
										   * allowable: FV_SaveYesAutoBackup, 
										   * FV_SaveNoAutoBackup, 
										   */
#define FV_SaveYesAutoBackup	0
#define FV_SaveNoAutoBackup		1
#define FV_SaveUserPrefAutoBackup 2

#define FS_MakePageCount				7 /* Enum Default FV_UseCurrentSetting
										   */
#define FV_UseCurrentSetting 	0
#define FV_DontChangePageCount	1
#define FV_MakePageCountEven	2
#define FV_MakePageCountOdd		3
#define FV_DeleteEmptyPages		4

#define FS_ShowSaveTextDialog			8
#define FS_SaveTextTblSetting			9

#define FV_SaveTblUserPref		0
#define FV_SaveTblRowsAsPgfs	1
#define FV_SaveTblColsAsPgfs	2
#define FV_SaveSkipTbls			3

#define FS_SaveTextExtraBlankLineAtEOP  10 /* Boolean Default False */
#define FS_UseDefaultUNIXpermissions	11 /* Boolean Default True */
#define FS_RetainNameStripe				12 /* Boolean Default False */
#define FS_UNIXpermissions				13 /* Int 0666 */
#define FS_UpdateFRVList				14 /* Boolean Default False */
#define FS_SaveFileTypeHint				16 /* Same format as string used by Open/Import*/
/*#define FS_FileIsInUse				17 Enum Default FV_DoCancel. Also allowable:
											FV_ResetLockAndContinue, FV_DoShowDialog 
											*/
#define FS_StructuredSaveApplication	18 /*String default NULL */
#ifdef FAPI_6_BEHAVIOR
#define FS_SgmlSaveApplication			FS_StructuredSaveApplication
#endif /* FAPI_6_BEHAVIOR */

/*#define FS_LockCantBeReset			19 Enum Default FV_DoCancel. Also allowable:
											FV_DoShowDialog, FV_DoOK
											*/
#define FS_SaveFileNotWritable			20 /* Enum Default FV_Cancel. Also allowable:
											FV_DoShowDialog
											*/
#define FS_ModDateChanged				21 /* Enum Default FV_DoCancel. Also allowable:
											* FV_DoShowDialog, FV_DoOK
											*/
#define FS_DitavalFile					22	/*Added For DitaVal file option for ditamap files -- richa*/
#define FS_NumSaveParams				22	/* Note that 15 is now skipped since it's obsolete */

/* Return parameters from F_ApiSave */
#define FS_SavedFileName			1 /* StringT */
#define FS_SaveNativeError			2	/* IntT */
#define FS_SaveStatus				3
#define FS_NumSaveReturnParams  FS_SaveStatus

#define FV_NumSaveStatusFields		2   /* Size of SaveStatus */

#define FV_ProductIsViewer		32
#define FV_FileNotWritable		33
#define FV_BadSaveFileName		34
#define FV_BadFileId			35
#define FV_BadSaveScriptValue	36
#define FV_NonPortableSaveName  37
#define FV_NonPortableFileRefs  38
#define FV_ProductIsMaker		39
#define FV_BadSaveObjectId		40		/* Not currently used */
#define FV_Unstructured			41
#define FV_InvalidSaveFilter	42

#define FV_UserCanceledSave		48
#define FV_FileWasExported		49
#define FV_CancelSaveFileIsInUse		50
#define FV_CancelSaveFileNotWritable	51
#define FV_CancelSaveModDateChanged	52
/* Also these errors defined above for open */
/* #define FV_LockWasReset			0 */
/* #define FV_LockNotReset 			1 */
/* #define FV_LockCouldntBeReset	2 */
/* #define FV_FileWasInUse			3 */
/* #define FV_LockWasInvalid		5 */
/* #define FV_FileIsNotWritable		6 */
/* #define FV_FileModDateChanged	7 */


/* Parameters for F_ApiImport() */
/* General Controls for all types of imports: */

/* #define FS_ShowBrowser 			    1 Same as defined for F_ApiOpen() */
/* #define FS_AlertUserAboutFailure		2 Same as defined for F_ApiOpen() */
/* #define FS_DontNotifyAPIClients		3 Same as defined for F_ApiOpen() */
#define FS_HowToImport					4 /* Enum Default FV_DoByRef Also
										   * allowable FV_DoByCopy, and
										   * FV_DoUserChoice
										   */
#define FV_DoByRef		5
#define FV_DoByCopy		6
#define FV_DoUserChoice 7


/* Text insets only */
#define FS_ManualUpdate					5 /* Boolean Default False */
#define FS_TextInsetName				6 /* String, name of text inset */

/* What to disallow */
/* #define FS_DisallowMIF				7 Defined above for F_ApiOpen() */
/* #define FS_DisallowDoc				8  Defined above for F_ApiOpen() */
#define FS_DisallowGraphicTypes			9 /* Boolean Default False */
#define FS_DisallowMacEditions          10 /* Boolean,  Default False Mac Only */
/* #define FS_DisallowFilterTypes		11 Defined above for F_ApiOpen() */
/* #define FS_DisallowPlainText			12 Defined above for F_ApiOpen() */
/* When importing a graphic the following apply */

#define FS_FileIsGraphic				13 /* Enum Default FV_DoOK also:
											* FV_DoCancel, FV_DoShowDialog
											*/
#define FS_FitGraphicInSelectedRect		14 /* Boolean Default True */
#define FS_GraphicDpi					15 /* Int Dpi for imported graphic.*/
/* When Importing Text Files the following applies */
/*#define FS_FileIsText			16	  Enum Default FV_TextFile_EOLisEOP also 
									  allowable: FV_TextFile_EOLisNotEOP, 
									  FV_DoCancel, FV_DoShowDialog,
									  FV_DoImportAsTable
*/
#define FV_DoImportAsTable	8
#define FS_ForceImportAsText 		17 /* Boolean default False*/

/* When Importing FASL/MIF Files the following applies */
#define FS_FileIsMakerDoc				18 /* Enum Default FV_DoOK. Also
											* allowable FV_DoCancel,
											* FV_DoShowDialog
											*/
#define FS_UseMainFlow					19 /* Enum Default True */
#define FS_ImportFlowTag				20 /* String, name of flow */
#define FS_ImportFlowPageSpace			21 /* Enum Default FV_BodyPage,
											* FV_ReferencePage
											*/
#define FV_ReferencePage			9
#define FV_BodyPage					10
#define FS_FormatImportedText			22 /* Enum Default FV_EnclosingDoc
											* also: FV_SourceDoc, FV_PlainText
											*/
#define FV_SourceDoc				0
#define FV_EnclosingDoc				1
#define FV_PlainText				2

/* When formatting comes from the Enclosing document.*/
#define FS_RemoveManualPageBreaks		23  /* Boolean Default True */
#define FS_RemoveOverrides				24  /* Boolean Default True */

/* When importing text as a table.*/
#define FS_ImportTblTag					25 /* String Default Format A*/
#define FS_TblNumHeadingRows			26 /* Int Boolean 1 */
#define FS_LeaveHeadingRowsEmpty		27 /* Boolean Default False */
#define FS_TreatParaAsRow				28 /* Boolean Default True */

/* When FS_TreatParaAsRow is True: */
#define FS_CellSeparator				29 /* String Default Tabs */
#define FS_NumCellSeparators			30 /* Int Default 1 */

/*When FS_TreatParaAsRow is False */
#define FS_NumColumns					31 /* Int Default 1 */

#define FS_FileIsSgmlDoc				32 /* Enum Default FV_DoOK. Also
											* allowable FV_DoCancel,
											* FV_DoShowDialog
											*/
/* #define FS_DisallowSgml				33 	* Same as defined by F_ApiOpen() */
/* #define FS_FileTypeHint				34  * Same as defined by F_ApiOpen() */

#define FS_StructuredImportApplication	35 	/* String default NULL */
#ifdef FAPI_6_BEHAVIOR
#define FS_SgmlImportApplication		FS_StructuredImportApplication
#endif /* FAPI_6_BEHAVIOR */

#define FS_ImportAsType					36  /* Enum values same as for FS_OpenAsType */ 
/* #define FS_DisallowXml				43 	* Same as defined by F_ApiOpen() */
#define FS_FileIsXmlDoc				37     /* Enum Default FV_DoOK. Also
											* allowable FV_DoCancel,
											* FV_DoShowDialog
											*/
#define FS_RasterDpi				38	   /* Metric Dpi for Raster filters */
#define FS_RasterImageWidth			39	   /* Metric Image width for */
										   /* Raster Filters */
#define FS_RasterImageHeight		40	   /* Metric Image width for */
										   /* Raster Filters */
#define FS_ShowRasterDpiDialog		41	   /* IntT for showing rasterdpi dialog */
#define FS_NumImportParams			45 /* Note that 43 is not contiguous since it's inherited from F_ApiOpen */
											/*note num incremented by 1 to 44 due to FS_InsetData*/
											/* note num incremented by 1 to 45 due to FS_UseHTTP*/
#define FS_InsetData                50 /* this new prop has been assigned a unique value */
#define FS_UseHTTP					51

/* Return paramters from F_ApiImport() */
#define FS_ImportedFileName				1 /* String */
#define FS_ImportNativeError			2 /* Int */
#define FS_ImportStatus				    3 /* Bit Field */
#define FS_NumImportReturnParams		FS_ImportStatus
#define FV_NumImportStatusFields		2 /* Size of ImportStatus */

/* FA_errno is FE_Success */
#define FV_ImportedByCopy				0
#define FV_ImportedText					1
#define FV_ImportedTextTable			2
#define FV_ImportedMIF					3
#define FV_ImportedMakerDoc				4
#define FV_ImportedFilteredFile			5
#define FV_ImportedGraphicFile			6
#define FV_ImportedMacEdition			7			
#define FV_ImportedSgmlDoc				8			
#define FV_ImportedXmlDoc				9			

/* Nothing Imported FA_errno is FE_BadParameter, FE_BadFileType, 
 * FE_MissingFile or FE_FailedState
 */
#define FV_BadImportFileName				16
#define FV_BadImportFileType				17
#define FV_BadImportScriptValue			18
#define FV_MissingImportScript			19
#define FV_CantForceImportAsText		20
#define FV_DisallowedImportType			21
#define FV_NoMainFlow						22
#define FV_NoFlowWithSpecifiedName		23
#define FV_InsertionPointNotInText		24
#define FV_InsertionPointInTableCell	25
#define FV_InsertionPointInFootnote	26
#define FV_InsufficientMemory			27
#define FV_BadEnclosingDocId				28
#define FV_BadTextFileTypeHint			29
#define FV_FlowUnstructured				30

/* Nothing Imported FA_errno is FE_Canceled*/
#define FV_CancelFileText				32
#define FV_CancelFileDoc				33
#define FV_CancelFileMIF				34
#define FV_CancelFileFilterable			35
#define FV_CancelFileGraphic			36
#define FV_UserCanceledImport			37
#define FV_CancelImportBrowser			38
#define FV_CancelFileIsMacEdition		39 /* Mac only */
#define FV_CancelFileSgml				40
#define FV_CancelFileXml				41
/* Nothing Imported FA_errno is FE_SystemError*/
#define FV_ImportFileNotReadable		48

/* Parameters for F_ApiExport() */
/*#define FS_ShowBrowser 			    1 Same as defined for F_ApiOpen()*/
/*#define FS_AlertUserAboutFailure		2 Same as defined for F_ApiOpen()*/
/*#define FS_DontNotifyAPIClients		3 Same as defined for F_ApiOpen()*/
#define FS_Export						4  
#define FV_ExportDocument		0
#define FV_ExportObject			1
#define FS_ExportType 					5 /* Enum Default FV_SaveFmtFilter,
										   * Also allowable: 
										   * FV_SaveFmtInterchange
										   * FV_SaveFmtStationery, 
										   * FV_SaveFmtViewOnly,
										   * FV_SaveFmtText, 
										   * FV_SaveFmtSgml,
										   * FV_SaveFmtBinary
										   */
#define FS_ExportTextRange				6  /* When FS_Export is FV_ExportDocument*/
#define FS_ExportObjectId				7 /* FS_Export is FS_ExportObject*/
/*#define FS_ShowSaveTextDialog	        8 Same as defined for F_ApiSave()*/
/*#define FS_SaveTextTblSetting		    9 Same as defined for F_ApiSave()*/
/*#define FS_SaveTextExtraBlankLineAtEOP 10  Same as defined for F_ApiSave()*/
/*#define FS_UseDefaultUNIXpermissions	11  Same as defined for F_ApiSave()*/
#define FS_ExportObjectNeedsDpi			12 /*Enum default FV_DoOK also:
											* FV_DoCancel, FV_DoShowDialog
											*/

/*#define FS_UNIXpermissions			13  Same as defined for F_ApiSave()*/
#define FS_ExportFileTypeHint				14  
/*#define FS_GraphicDpi					15 Same as defined for F_ApiImport()*/

#define FS_StructuredExportApplication	16 	/*String default NULL */
#ifdef FAPI_6_BEHAVIOR
#define FS_SgmlExportApplication		FS_StructuredExportApplication
#endif /* FAPI_6_BEHAVIOR */

#define FS_ImportExportVersion			17		
#define FS_NumExportParams				FS_ImportExportVersion

/* Return parameters from F_ApiExport */
#define FS_ExportFileName			1 /* StringT */
#define FS_ExportNativeError		2	/* IntT */
#define FS_ExportStatus				3
#define FV_NumExportStatusFields	2   /* Size of ExportStatus */
#define FS_NumExportReturnParams  FS_ExportStatus

/* FA_errno is FE_Success */
#define FV_ExportFileHasNewName			0
/* FA_errno is FE_WrongProduct*/
#define FV_ExportProductIsViewer		1
#define FV_ExportFileNotWritable		2
#define FV_InsufficientMem				3
/* FA_errno is FE_BadParameter*/
#define FV_BadExportFileName			16
#define FV_MissingExportScript			17
#define FV_BadExportObjectId			18
#define FV_InvalidTextRange				19
#define FV_BadExportDocId				20
#define FV_BadExportScriptValue			21
#define FV_FilterFailed					22
#define FV_InvalidExportFilter			23
/* FA_errno is FE_Canceled*/
#define FV_UserCanceledExport			32
#define FV_CanceledExportObjectNeedsDpi	33
#define FV_ApiClientCanceledExport		34

/* Parameters for F_ApiUpdateBook() */
#define FS_AllowNonFMFiles              1 /* Enum default FV_DoOK Also
										   * allowable, FV_DoCancel
										   * FV_DoShowDialog
										   */
/* #define FS_AlertUserAboutFailure      2 Same as defined for F_ApiOpen() */
#define FS_AllowViewOnlyFiles           3 /* Enum default FV_DoOK Also
										   * allowable, FV_DoCancel
										   * FV_DoShowDialog
										   */
#define FS_ShowBookErrorLog             4 /* Boolean default False */
/* #define FS_MakeVisible				5	Same as defined for F_ApiOpen() */
#define FS_AllowInconsistentNumProps    6 /* Enum default FV_DoOK Also
										   * allowable, FV_DoCancel
										   * FV_DoShowDialog
										   */
#define FS_UpdateBookGeneratedFiles     7 /* Boolean default True */
#define FS_UpdateBookNumbering          8 /* Boolean default True */
#define FS_UpdateBookOleLinks           9 /* Boolean default True */
#define FS_UpdateBookTextReferences     10 /* Boolean default True */
#define FS_UpdateBookXRefs              11 /* Boolean default True */
#define FS_UpdateBookMasterPages        12 /* Boolean default True */
#define FS_NumUpdateBookParams			FS_UpdateBookMasterPages

/* Return parameters from F_ApiUpdateBook() */
#define FS_UpdateBookStatus               1        /* Bit Field */
#define FS_NumUpdateBookReturnParams      FS_UpdateBookStatus
#define FV_NumUpdateBookStatusFields	  4   /* Size of UpdateBookStatus */

/* FA_errno is FE_BadOperation */
#define FV_BookNotSelfConsistent     0
#define FV_DuplicateFileInBook       1
#define FV_NoNonGeneratedFilesInBook 2

/* FA_errno is FE_Canceled or FE_CanceledByClient */
#define FV_CancelNonFMFileInBook     32
#define FV_CancelViewOnlyFileInBook  33
#define FV_CancelInconsistentNumPropsInFileInBook 34
#define FV_UserCanceledUpdateBook    35

/* FA_errno is FE_BadParameter */
#define FV_BadUpdateBookFileId       64
#define FV_BadUpdateBookScriptValue  65

/* FA_errno FE_SystemError */
#define FV_FileInBookNotOpened       96
#define FV_FileInBookNotSaved        97
#define FV_TooManyWindowsUpdateBook  98

/* Values for the "direction" parameter to F_ApiAddRows() */
#define FV_Body				  	  1237
#define FV_Heading				  1238
#define FV_Footing				  1239
#define FV_Above                  1240   
#define FV_Below                  1241   
/* Values for the "direction" parameter to F_ApiAddCols() */
#define FV_Left                   1242   
#define FV_Right                  1243   

/* Values for "mode" parameter to F_ApiChooseFile() */
#define FV_ChooseSelect    0
#define FV_ChooseOpen      1
#define FV_ChooseSave      2
#define FV_ChooseOpenDir   3

/* Initializations */
#define FA_Init_First             1   
#define FA_Init_Subsequent        2
#define FA_Init_TakeControl       3   
#define FA_Init_DocReport 	      4   

/* Notifications */
#define FA_Note_PreOpenDoc        1   
#define FA_Note_PostOpenDoc       2   
#define FA_Note_PreOpenMIF        3   
#define FA_Note_PostOpenMIF       4   
#define FA_Note_PreSaveDoc        5   
#define FA_Note_PostSaveDoc       6   
#define FA_Note_PreSaveMIF        7   
#define FA_Note_PostSaveMIF       8   
#define FA_Note_PreFileType       9   
#define FA_Note_PostFileType      10   
#define FA_Note_PreQuitDoc        11   
#define FA_Note_DirtyDoc          12   
#define FA_Note_ClientCall        13   
#define FA_Note_FilterIn          14   
#define FA_Note_FilterOut         15
#define FA_Note_PreOpenBook		  16
#define FA_Note_PostOpenBook	  17
#define FA_Note_PreOpenBookMIF	  18
#define FA_Note_PostOpenBookMIF	  19
#define FA_Note_PreSaveBook		  20
#define FA_Note_PostSaveBook	  21
#define FA_Note_PreSaveBookMIF	  22
#define FA_Note_PostSaveBookMIF	  23
#define FA_Note_PreQuitBook		  24
#define FA_Note_DirtyBook		  25  
#define FA_Note_PreQuitSession	  26
#define FA_Note_PostQuitSession	  27
#define FA_Note_PreRevertDoc	  28
#define FA_Note_PostRevertDoc     29
#define FA_Note_PreRevertBook     30
#define FA_Note_PostRevertBook    31
#define FA_Note_PreAutoSaveDoc	  32
#define FA_Note_PostAutoSaveDoc   33
#define FA_Note_BackToUser        34
#define FA_Note_DisplayClientTiDialog 35
#define FA_Note_UpdateAllClientTi 36
#define FA_Note_UpdateClientTi 	  37
#define FA_Note_PreImport		  38
#define FA_Note_PostImport		  39
#define FA_Note_PostQuitDoc       40
#define FA_Note_PostQuitBook      41
#define FA_Note_PreFunction       42
#define FA_Note_PostFunction      43
#define FA_Note_PreMouseCommand   44
#define FA_Note_PostMouseCommand  45
#define FA_Note_PreHypertext      46
#define FA_Note_PostHypertext     47
#define FA_Note_PrePrint          48
#define FA_Note_PostPrint         49
#define FA_Note_BodyPageAdded     50
#define FA_Note_BodyPageDeleted   51
#define FA_Note_PreInsertElement  52
#define FA_Note_PostInsertElement 53
#define FA_Note_PreChangeElement  54
#define FA_Note_PostChangeElement 55
#define FA_Note_PreWrapElement	  56
#define FA_Note_PostWrapElement	  57
#define FA_Note_PreDragElement	  58
#define FA_Note_PostDragElement	  59
#define FA_Note_PreCopyElement	  60
#define FA_Note_PostCopyElement	  61
#define FA_Note_PreSetAttrValue	  62
#define FA_Note_PostSetAttrValue  63
#define FA_Note_PreImportElemDefs 64
#define FA_Note_PostImportElemDefs 65
#define FA_Note_ECMInternal	      66    /* Frame Internal Use */
#define FA_Note_PreExport         67
#define FA_Note_PostExport        68
#define FA_Note_PreInlineTypeIn	  69
#define FA_Note_PostInlineTypeIn  70
#define FA_Note_PreSaveAsPDFDialog 71
#define FA_Note_PostSaveAsPDFDialog 72
#define FA_Note_PreDistill		  73
#define FA_Note_PostDistill		  74
	/* FA_Note_FilterIn will be used instead of FA_Note_FilterFileToFile */
	/* on Mac so that existing filters will continue to work.            */
#define FA_Note_FilterFileToFile  75
#define FA_Note_PreBookComponentOpen  76
#define FA_Note_PostBookComponentOpen 77
#define FA_Note_PreGenerate  78
#define FA_Note_PostGenerate 79
#define FA_Note_PreGoToXrefSrc 80
#define FA_Note_PostGoToXrefSrc 81
#define FA_Note_PreOpenSGML	82
#define FA_Note_PostOpenSGML	83
	/* NB experimental interfaces */
#define FA_Note_Dialog		84
#define FA_Note_Alert		85
#define FA_Note_Palette		86
#define FA_Note_ToolBar		87
#define FA_Note_ConsoleMessage	88
#define FA_Note_Help			89
#define FA_Note_URL				90
#define FA_Note_CursorChange	91
#define FA_Note_FontSubstitution 92
#define FA_Note_UndoCheckpoint   93
#define FA_Note_FileOpen		 94
	/* end experimental */
#define FA_Note_PreOpenXML	95
#define FA_Note_PostOpenXML	96
#define FA_Note_PreSaveXML	97
#define FA_Note_PostSaveXML	98
#define FA_Note_PreSaveSGML	99
#define FA_Note_PostSaveSGML	100
#define FA_Note_U3DCommand 101
#define FA_Note_Not_U3DCommand 102
#define FA_Note_Not_RSC_Supported_File 103
#define FA_Note_RSC_Supported_File 104
#define FA_Note_PostActiveDocChange 105
#define FA_Note_PreUpdateXRefs 106
#define FA_Note_PostUpdateXRefs 107
#define FA_Note_DisplayClientXRefDialog 108
#define FA_Note_QuitModelessDialog 109
#define	FA_Note_InsertTopicRef	110 /* Only for FM Internal Use */
#define	FA_Note_InsertConRef	111 /* Only for FM Internal Use */
#define	FA_Note_GenerateFM		112 /* Only for FM Internal Use */
#define	FA_Note_OpenAllTopicrefs	113 /* Only for FM Internal Use */
#define	FA_Note_UpdateRefs		114 /* Only for FM Internal Use */
#define	FA_Note_AssignId		115 /* Only for FM Internal Use */
#define	FA_Note_DITAOptions		116 /* Only for FM Internal Use */
#define FA_Note_NewDitamapFile	117 /* Only for FM Internal Use */
#define FA_Note_NewBookmapFile	118 /* Only for FM Internal Use */
#define FA_Note_NewTopicFile	119 /* Only for FM Internal Use */
#define FA_Note_NewTaskFile		120 /* Only for FM Internal Use */
#define FA_Note_NewConceptFile	121 /* Only for FM Internal Use */
#define FA_Note_NewReferenceFile	122 /* Only for FM Internal Use */
#define FA_Note_NewGlossEntryFile	123 /* Only for FM Internal Use */
#define FA_Note_SWF_File			124 /* Captivate 4.0 Integration*/
#define FA_Note_Not_SWF_File		125 /* Captivate 4.0 Integration*/
#define FA_Note_Enable_Disable_DITA_Menu_Commands	126	/*Added new notification for DITA menu update.*/
#define FA_Note_Dialog_Create	127
#define FA_Note_Num			128	 /* last notify + 1



									 * Don't forget to add
									 * your new notification to
									 * F_ApiNotificationName
									 */


#ifdef FAPI_4_BEHAVIOR
#define FA_Note_QuitDoc FA_Note_PreQuitDoc
#define FA_Note_QuitBook FA_Note_PreQuitBook
#endif /* FAPI_4_BEHAVIOR */

/* For F_TextItemT */
/* F_TextItemT->dataType:		->data: */
#define FTI_String                 0x00000001   /* StringT */ 
#define FTI_LineBegin              0x00000002   /* Nada */ 
#define FTI_LineEnd                0x00000004   /* See flags below */ 
#define FTI_PgfBegin               0x00000008   /* ID(Pgf) */ 
#define FTI_PgfEnd                 0x00000010   /* ID(Pgf) */ 
#define FTI_FlowBegin              0x00000020   /* ID(Flow) */ 
#define FTI_FlowEnd                0x00000040   /* ID(Flow) */ 
#define FTI_PageBegin              0x00000080   /* ID(Page) */ 
#define FTI_PageEnd                0x00000100   /* ID(Page) */ 
#define FTI_SubColBegin            0x00000200   /* ID(SubCol) */ 
#define FTI_SubColEnd              0x00000400   /* ID(SubCol) */ 
#define FTI_FrameAnchor            0x00000800   /* ID(AnchoredFrame) */ 
#define FTI_FnAnchor               0x00001000   /* ID(Footnote) */ 
#define FTI_TblAnchor              0x00002000   /* ID(Table) */ 
#define FTI_MarkerAnchor           0x00004000   /* ID(Marker) */ 
#define FTI_XRefBegin              0x00008000   /* ID(XRef) */ 
#define FTI_XRefEnd                0x00010000   /* ID(XRef) */ 
#define FTI_VarBegin               0x00020000   /* ID(Var) */ 
#define FTI_VarEnd                 0x00040000   /* ID(Var) */ 
#define FTI_ElementBegin           0x00080000   /* ID(Element) */ 
#define FTI_ElementEnd             0x00100000   /* ID(Element) */ 
#define FTI_CharPropsChange        0x00200000   /* See flags below */ 
#define FTI_TextFrameBegin         0x00400000   /* ID(TextFrame) */ 
#define FTI_TextFrameEnd           0x00800000   /* ID(TextFrame) */ 
#define FTI_TextObjId              0x01000000   
#define FTI_TextInsetBegin         0x02000000   
#define FTI_TextInsetEnd           0x04000000    /* ID(FO_TiText),ID(FO_TiFlow)
												  * ID(FO_TiTextTable) or
												  * ID(FO_TiApiClient.
												  */
#define FTI_ElemPrefixBegin		   0x08000000	/* ID(Element) */
#define FTI_ElemPrefixEnd		   0x10000000	/* ID(Element) */
#define FTI_ElemSuffixBegin		   0x20000000	/* ID(Element) */
#define FTI_ElemSuffixEnd		   0x40000000	/* ID(Element) */

#define FTI2_property(p)		   (((p) & 0xF0000000) == 0x80000000)
#define FTI2_RubiTextBegin         0x80000001	/* ID(Rubi) */
#define FTI2_RubiTextEnd           0x80000002	/* ID(Rubi) */
#define FTI2_RubiCompositeBegin    0x80000004	/* ID(Rubi) */
#define FTI2_RubiCompositeEnd      0x80000008	/* ID(Rubi) */

/* FTI_LineEnd flags: */
#define FTI_HardLineEnd            1
#define FTI_HyphenLineEnd          2

#ifdef FAPI_4_BEHAVIOR
#define FTI_ColBegin     FTI_SubColBegin
#define FTI_ColEnd       FTI_SubColEnd
#endif

/* FTI_CharPropsChange flags: */
#define FTF_FAMILY         0x80000000
#define FTF_VARIATION      0x40000000
#define FTF_WEIGHT         0x20000000
#define FTF_ANGLE          0x10000000
#define FTF_UNDERLINING    0x08000000
#define FTF_STRIKETHROUGH  0x04000000
#define FTF_OVERLINE       0x02000000
#define FTF_CHANGEBAR      0x01000000
#define FTF_OUTLINE        0x00800000
#define FTF_SHADOW         0x00400000
#define FTF_PAIRKERN       0x00200000
#define FTF_SIZE           0x00100000
#define FTF_KERNX          0x00080000
#define FTF_KERNY          0x00040000
#define FTF_SPREAD         0x00020000
#define FTF_COLOR          0x00010000
#define FTF_CHARTAG        0x00008000
#define FTF_CAPITALIZATION 0x00004000
#define FTF_POSITION       0x00002000
#define FTF_STRETCH        0x00001000
#define FTF_LANGUAGE       0x00000800
#define FTF_TSUME          0x00000400
#define FTF_ENCODING       0x00000200
#define FTF_IIF            0x00000002
#define FTF_CONDITIONTAG   0x00000001
#define FTF_ALL			   0xFFFFFE03

/* For any F_TextLocT (or F_TextRangeT) "offset" field */
#define FV_OBJ_END_OFFSET      0x50000000
#define F_ApiOffsetFromEnd(o) (((o) & 0xE0000000) == 0x40000000) /* allow FV_OBJ_END_OFFSET-1 */

/* Property type definitions */
#define FT_Bad               0   /*! Internal use only */ 
#define FT_Integer           1   
#define FT_Metric            2   
#define FT_String            3   
#define FT_Id                4   
#define FT_Metrics           5   
#define FT_Strings           6   
#define FT_Points            7   
#define FT_Tabs              8   
#define FT_TextLoc           9   
#define FT_TextRange         10   
#define oldFT_ElementFmts    11   	/* FB_OBSOLETE */
#define FT_ElementCatalog    12   
#define FT_Ints              13   
#define FT_UBytes		     14
#define FT_UInts		     15
#define FT_FormatBranches	 16
#define FT_AttributeDefs	 17
#define FT_Attributes	 	 18
#define FT_ElementRange		 19
/*
 * IMPORTANT VERSIONING INFORMATION:
 *		New property types must not be returned to down-rev clients.
 *      The client's parameter marshalling and copy/free routines
 *      will not know what to do with these types.
 */
#define FT_Num               20

/*
 * Windows-specific properties to specify a client's registration
 * information at initialization (for F_ApiWinConnectSession())
 */
#define FI_PLUGIN_PRODUCTNAME	1
#define FI_PLUGIN_NAME			2
#define FI_PLUGIN_TYPE			3
#define FI_PLUGIN_DESCRIPTION	4
#define FI_PLUGIN_PRODUCTS		5
#define FI_PLUGIN_FACET			6
#define FI_PLUGIN_FORMATID		7
#define FI_PLUGIN_VENDOR		8
#define FI_PLUGIN_SUFFIX		9
#define FI_PLUGIN_INFORMAT		10
#define FI_PLUGIN_OUTFORMAT		11

/* Object type definitions */
#define FO_Session            0   
#define FO_StringResource     1   /* ! Frame Tech. use only */ 
#define FO_Book               2   
#define FO_BookComponent      3   
#define FO_Doc                4   
#define FO_First_Page         FO_BodyPage
#define FO_BodyPage           5   
#define FO_MasterPage         6   
#define FO_RefPage      	  7   
#define FO_HiddenPage         8   
#define FO_Last_Page          FO_HiddenPage
#define FO_First_Graphic      FO_UnanchoredFrame   /* ! Internal use only */ 
#define FO_UnanchoredFrame    9   
#define FO_Group              10   
#define FO_Arc                11   
#define FO_Rectangle          12   
#define FO_Ellipse            13   
#define FO_RoundRect          14   
#define FO_Polyline           15   
#define FO_Polygon            16   
#define FO_Line               17   
#define FO_TextLine           18   
#define FO_TextFrame          19   
#define FO_Inset              20   
#define FO_Math               21   
#define FO_DBGroup            22           /* ! Frame Tech. use only */ 
#define FO_Last_Graphic       FO_DBGroup   /* ! Internal use only */ 
#define FO_AFrame             23   
#define FO_XLast_Graphic      FO_AFrame    /* ! Internal use only */ 
#define FO_Marker             24   
#define FO_Fn 	              25   
#define FO_XRef               26   
#define FO_XRefFmt            27   
#define FO_Var 	              28   
#define FO_VarFmt 	          29   
#define FO_Flow               30   
#define FO_Pgf                31   
#define FO_PgfFmt             32   
#define FO_CharFmt            33   
#define FO_CondFmt            34   
#define FO_Color              35   
#define FO_Tbl 	              36   
#define FO_TblFmt 	          37   
#define FO_Row                38   
#define FO_Cell               39   
#define FO_RulingFmt          40   
#define FO_ElementDef         41   
#define FO_Element            42   
#define FO_DialogResource     43
#define FO_DlgBox             44
#define FO_DlgButton          45
#define FO_DlgTriBox          46
#define FO_DlgCheckBox        47
#define FO_DlgPopUp           48
#define FO_DlgScrollBox       49
#define FO_DlgEditBox         50
#define FO_DlgRadioButton     51
#define FO_DlgLabel           52
#define FO_DlgImage           53
#define FO_DlgScrollBar       54
#define FO_Menu				  55
#define FO_Command			  56
#define FO_MenuItemSeparator  57
#define FO_FmtChangeList	  58
#define FO_FmtRule		  	  59
#define FO_FmtRuleClause	  60
#define FO_TiFlow			  61
#define FO_TiText			  62
#define FO_TiTextTable		  63
#define FO_TiApiClient		  64
#define FO_SubCol			  65
#define FO_MarkerType		  66
#define FO_CombinedFontDefn	  67
#define FO_Rubi               68
#define FO_Alert			  69
#define FO_CursorResource     70	/* unsupported experimental interface */
#define FO_AttrCondExpr		  71	
/* 
 * When you add a new object here, make sure that it is IN THE SAME
 * RELATIVE LOCATION in the propLists[] data structure in props_api.c
 * That data structure is indexed by these values.
 */
#define FO_Num                72 /* last object number + 1 */

#define FO_First_Internal     FO_Num   /* ! Internal use only */ 
#define FO_Page               (FO_First_Internal+0)  /* ! Internal use only */ 
#define FO_Graphic            (FO_First_Internal+1)  /* ! Internal use only */ 
#define FO_Frame              (FO_First_Internal+2)  /* ! Internal use only */ 
#define FO_Cblock             (FO_First_Internal+3)  /* ! Internal use only */ 
#define FO_SubCond            (FO_First_Internal+4)  /* ! Internal use only */ 
#define FO_SubStyle           (FO_First_Internal+5)  /* ! Internal use only */ 
#define FO_SubDash            (FO_First_Internal+6)  /* ! Internal use only */ 
#define FO_SubCell            (FO_First_Internal+7)  /* ! Internal use only */ 
#define FO_SubTbl             (FO_First_Internal+8)  /* ! Internal use only */ 
#define FO_SubPblock          (FO_First_Internal+9)  /* ! Internal use only */ 
#define FO_SubTextDef         (FO_First_Internal+10) /* ! Internal use only */ 
#define FO_MenuCell		      (FO_First_Internal+11) /* ! Internal use only */ 
#define FO_TextInset		  (FO_First_Internal+12) /* ! Internal use only */
#define FO_DitaMap            (FO_First_Internal+13) /* ! Internal use only */
#define FO_Last_Internal      (FO_First_Internal+14) /* ! Internal use only */ 
#define FO_Bad               255   /* ! Internal use only */ 

#define FO_AnchoredFrame  FO_AFrame
#define FO_UFrame         FO_UnanchoredFrame


#ifdef FAPI_4_BEHAVIOR
#define FO_TextCol        FO_TextFrame
#endif

/* Property definitions */

	/* Object */
#define FP_Name                   20   /* R/W String */ 
#define FP_Unique                 21   /* R/O Integer */ 
#define FP_UserString             22   /* R/W String */ 
#define FP_ObjectAttributes       23   /* R/W Stringlist */ 

	/* Kit */
#define FP_Label	30 /* R/W String */
#define FP_IsIconified	31 /* R/W Boolean */
#define FP_IsInFront	32 /* R/W Boolean */
#define FP_ScreenX      33 /* R/W Integer */
#define FP_ScreenY      34 /* R/W Integer */
#define FP_ScreenWidth  35 /* R/W Integer */ 
#define FP_ScreenHeight 36 /* R/W Integer */ 


	/* Session */
#define FV_SessionId 		  0
#define FP_VersionRevision        41   /* R/O Integer */ 
#define FP_ProductName            42   /* R/O String */ 
#define FP_VersionMajor           43   /* R/O Integer */ 
#define FP_VersionMinor           44   /* R/O Integer */ 
#define FP_Platform               45   /* R/O String */ 
#define FP_OperatingSystem        46   /* R/O String */ 
#define FP_WindowSystem           47   /* R/O String */ 
#define FP_AutoSave               48   /* R/W Boolean */ 
#define FP_AutoSaveSeconds        49   /* R/W Integer (in seconds, not minutes!) */ 
#define FP_AutoBackup             50   /* R/W Boolean */ 
#define FP_ActiveDoc         	  51   /* R/W ID(Document) */ 

/* The following is for INTERNAL use only. Donot publish 
 * this to external world */
#define FP_CurrentDoc			  2401 /* R/O ID */ 

/* This is for INTERNAL use - used for calling PDF Client 
 * through batch mode. DO NOT publish */
#define FP_PDFPageNum			  2402 /* R/O Integer */ 
/*
This would set the fdk undo recording state
*/
#define FP_UndoFDKRecording		  2403

/**This is for the HP Warning preference in pref dialog box - dhaundy */
#define FP_StackWarningLevel		2404
#define FP_NoFlashInPDF				2405  /* R/W Boolean */ 
#define FP_No3DInPDF				2406   /* R/W Boolean */ 
#define FP_InsetURL					2407   /* R/W Boolean */ 
#define FP_DontShowWelcomeScreen	2408   /* R/W Boolean */ 
#define FP_TechSuiteInternal		2409
#define FP_IsTechnicalSuiteLicensed           2410  /* R/O Boolean */
#define FP_TechSuiteInternal2				  2411	/* R/O Integer */
#define FP_IsFMRunningInTrialPeriod		2412  /* R/O Boolean */	

#define FV_WarnNever				0
#define FV_WarnAlways				1
#define FV_WarnOnce					2
#define FV_UndoAll					3
#define FV_RedoAll					4


#define FP_ActiveBook             52   /* R/W ID(Document) */ 
#define FP_FirstOpenDoc      	  53   /* R/O ID(Document) */ 
#define FP_FirstOpenBook          54   /* R/O ID(Document) */ 
#define FP_FontFamilyNames        55   /* R/O Strings */ 
#define FP_FontVariationNames     56   /* R/O Strings */ 
#define FP_FontWeightNames        57   /* R/O Strings */ 
#define FP_FontAngleNames         58   /* R/O Strings */ 
#define FP_MarkerNames            59   /* R/O Strings */ 
#define FP_FontFamilyAttributes	  60   /* R/O Ints */
#define		FV_FAMILY_VISIBLE		0x00000001		/* Family is visible in menu */
#define		FV_FAMILY_SELECTABLE 	0x00000002		/* Family can be selected in menu */
#define		FV_FAMILY_MAPPED		0x00000004		/* Family is always mapped to another family */
#define		FV_FAMILY_SURROGATE		0xFFFF0000		/* High-order 16 bits are the family mapped to */
#define FP_RememberMissingFontNames 61 /* R/W Boolean */
#define FP_Mif8bitOutput		   62  /* R/W Boolean */
#define FP_GreekSize               63  /* R/W Metric */
#define FP_RetainUndoState         64  /* R/W Boolean */
#define FP_ProductIsStructured     65  /* R/O Boolean */
#define FP_ProductIsDemo           66  /* R/O Boolean */

#define FP_DisableAutofitAfterZoom 67  /* R/W Boolean */ 

#define FP_DefaultFontFamily      68   /* R/O Integer */
#define FP_DefaultFontVariation   69   /* R/O Integer */
#define FP_DefaultFontWeight      70   /* R/O Integer */
#define FP_DefaultFontAngle       71   /* R/O Integer */
#define FP_CTFontContext          72   /* R/O Ptr */
#define FP_FontFamilyFullNames    73   /* R/O Strings */ 


#define FP_RpcPropertyName        76   /* R/O String */ 
#define FP_RpcProgramNumber       77   /* R/O Int */ 
#define FP_DisplayName            78   /* R/O String */ 
#define FP_ProcessNumber          79   /* R/O Int */ 
#define FP_OpenDir                80   /* R/W String */ 
#define FP_HostName               81   /* R/O String */ 
#define FP_UserName               82   /* R/O String */ 
#define FP_UserLogin              83   /* R/O String */ 
#define FP_UserHomeDir            84   /* R/O String */ 
#define FP_UserSettingsDir        152  /* R/O String */ 
#define FP_Path                   85   /* R/O String */ 
#define FP_TmpDir                 86   /* R/O String */ 
#define FP_FM_HomeDir             87   /* R/O String */ 
#define FP_FM_BinDir              88   /* R/O String */ 
#define FP_FM_InitDir             89   /* R/O String */ 
#define FP_FM_CurrentDir          90   /* R/O String */ 
#define FP_FM_SgmlDir             100  /* R/O String */
#define FP_FM_StructureDir        2031 /* R/O String */ 
#define FP_FM_XmlDir              2032 /* R/O String */ 

#define FP_Reformatting           91   /* R/W Boolean */ 
#define FP_Displaying             92   /* R/W Boolean */ 
#define FP_ApplyFormatRules		  93   /* R/W Boolean */
#define FP_ApplyEOPRules		2030   /* R/W Boolean */
#define FP_Validating			  94   /* R/W Boolean */

#define FP_ToolBar	  		95   	/* R/W StringList */

#define FP_FirstMenuItemInSession 97   /* R/O ID (MenuCell) */
#define FP_FirstCommandInSession  98   /* R/O ID (MenuCell) */
#define FP_CurrentMenuSet		  99   /* R/W Int */
#define FV_MENU_QUICK  		1
#define FV_MENU_COMPLETE  	2
#define FV_MENU_CUSTOM  	3
/* #define FP_UniquelyEnabledMenuItem 100 DEAD!!! */  /* R/W String */
#define FP_IconBarOn			  101	/* R/W Boolean */
#define FP_HelpPending			  102   /* RO Boolean */
#define FP_FM_HelpDir             103   /* R/O String */ 
#define FP_PercentDone			  104	/* R/W Int */
#define FP_ActiveAlert			  105   /* R/0 ID FO_Alert (experimental) */
#define FP_Snap					  151	/* R/W Boolean */
#define FP_Gravity				  107   /* R/W Boolean */
#define FP_KByteAllocationSize	  106   /* R/W Integer */
#define FP_ImportFilters         2288   /* R/O StringListT */ 
#define FP_ExportFilters         2289   /* R/O StringListT */ 

#define FP_UIColorBackground     2297   /* R/W Metrics (experimental) */
#define FP_UIColorRuler          2298   /* R/W Metrics (experimental) */
#define FP_BIBGetAddressProc     2299   /* R/O Int (Actually a function pointer) */
/* Used by svg filter. This is for FM internal usage. This must not be */
/* published to outside world.										   */
#define FP_RasterFilterDpi		 2400	/* R/O Metric. */

/*
 * Selectors for fields in Hint String
 */
#define FV_Hint_HintVersion		0
#define FV_Hint_VendorId		1
#define FV_Hint_FormatId		2
#define FV_Hint_Platform		3
#define FV_Hint_FilterVersion	4
#define FV_Hint_FilterName		5



	/* Document */
#define FP_StatusLine             108 /* R/W String */
#define FP_TextSelection          109   /* R/W TextRange */ 
#define FP_IsOnScreen               110   /* R/W Boolean */ 
#define FP_NextOpenDocInSession       111   /* R/O ID(Document) */ 
#define FP_FirstGraphicInDoc      112   /* R/O ID(Graphic) */ 
#define FP_FirstPgfInDoc          113   /* R/O ID(Pgf) */ 
#define FP_FirstMarkerInDoc       114   /* R/O ID(Marker) */ 
#define FP_FirstVarInDoc     115   /* R/O ID(Variable) */ 
#define FP_FirstVarFmtInDoc  116   /* R/O ID(VariableFmt) */ 
#define FP_FirstXRefInDoc         117   /* R/O ID(XRef) */ 
#define FP_FirstXRefFmtInDoc      118   /* R/O ID(XRefFmt) */ 
#define FP_FirstFnInDoc     119   /* R/O ID(Footnote) */ 
#define FP_FirstTblInDoc        120   /* R/O ID(Table) */ 
#define FP_FirstFlowInDoc         121   /* R/O ID(Flow) */ 
#define FP_FirstPgfFmtInDoc       122   /* R/O ID(PgfFmt) */ 
#define FP_FirstCharFmtInDoc      123   /* R/O ID(CharFmt) */ 
#define FP_FirstCondFmtInDoc      124   /* R/O ID(CondFmt) */ 
#define FP_FirstTblFmtInDoc     125   /* R/O ID(Table Catalog) */ 
#define FP_FirstRulingFmtInDoc    126   /* R/O ID(RulingFmt) */ 
#define FP_FirstColorInDoc        127   /* R/O ID(Color) */ 
#define FP_FirstSelectedGraphicInDoc   128   /* R/O ID(Graphic) */ 
#define FP_SelectedTbl	          129   /* R/O ID(Table) */
#define FP_FileExtensionOverride  2813  /* R/W StringT */
#define FP_FirstAttrCondExprInDoc 2815  /* R/O ID(AttrCondExpr)*/

#ifdef FAPI_4_BEHAVIOR
#define FP_DocType                FP_DocOpenType
#endif
#define FP_DocOpenType			  130  /* R/O Enum */ 
#define FV_DOC_TYPE_BINARY                 0x01 
#define FV_DOC_TYPE_TEXT                   0x02 
#define FV_DOC_TYPE_MIF                    0x03 
#define FV_DOC_TYPE_FILTER                 0x04 

#define FP_DocIsModified            131   /* R/O Boolean */ 
#define FP_DocIsHelp				132   /* R/O Boolean */
#define FP_DocIsViewOnly			133   /* R/W Boolean */
#define FP_ViewOnlyWinPalette	134   /* R/W Boolean */
#define FP_ViewOnlyWinMenubar	135   /* R/W Boolean */
#define FP_ViewOnlyWinBorders	136   /* R/W Boolean */
#define FP_ViewOnlyWinPopup	137   /* R/W Boolean */
#define FP_ViewOnlyXRef	138   /* R/W Enum */
#define FV_VOX_NOT_ACTIVE       0
#define FV_VOX_GOTO_BEHAVIOR    1
#define FV_VOX_OPEN_BEHAVIOR    2
#define FV_VOX_ALERT            3
#define FP_ViewOnlySelect	139   /* R/W Enum */
#define FV_VOS_USER_ONLY        1
#define FV_VOS_NONE             2
#define FV_VOS_YES              3
#define FP_ViewOnlyDeadCodes	140   /* R/W UInts */
#define FP_FirstTiInDoc			141  /* R/O text inset object.*/
#define FP_FirstSelectedTiInDoc	142
#define FP_DocIsViewOnlyWinPalette	FP_ViewOnlyWinPalette /* backward compatibility */
#define FP_MenuBar				143	/* R/W ID */
#define FP_ViewOnlyMenuBar		144 /* R/W ID */
#define FP_DocSaveType   		145 /* R/O Enum same as FP_DocOpenType*/
#define FP_Untouchable          146 /* R/W Boolean */ 
#define FP_DocFluidFlow			147 /* R/W ID(Flow) */ 
#define FP_FirstMarkerTypeInDoc 148 /* R/O ID(MarkerTypeT) */
#define FP_MarkerTypeNames		149 /* R/O StringListT */
#define FP_AddMarkerTypeToStandardMarkers 150 /* W/O String */

   /* Doc Condition Properties */
#define FP_ShowAll                155   /* R/W Boolean */ 
#define FP_ShowCondIndicators     156   /* R/W Boolean */ 

/* Doc Update Properties */
#define FP_DontUpdateXRefs		159  /* R/W Boolean */
#define FP_DontUpdateTextInsets	160  /* R/W Boolean */

   /* Doc Typography Properties */
#define FP_LineBreakAfter         177   /* R/W String */ 
#define FP_SuperScriptSize        178   /* R/W MetricPercent */ 
#define FP_SuperScriptShift       179   /* R/W MetricPercent */ 
#define FP_SubScriptSize          180   /* R/W MetricPercent */ 
#define FP_SubScriptShift         181   /* R/W MetricPercent */ 
#define FP_SmallCapsSize          182   /* R/W MetricPercent */ 
#define FP_SuperScriptStretch     183   /* R/W MetricPercent */ 
#define FP_SubScriptStretch       184   /* R/W MetricPercent */ 
#define FP_SmallCapsStretch       185   /* R/W MetricPercent */ 

   /* Doc Rubi Typography Properties */
#define FP_RubiSize                186  /* R/W MetricPercent */
#define FP_RubiFixedSize           187  /* R/W Metric */
#define FP_NarrowRubiSpaceForJapanese 188  /* R/W Enum */
#define FP_WideRubiSpaceForJapanese   189  /* R/W Enum */
#define FP_NarrowRubiSpaceForOther 190  /* R/W Enum */
#define FP_WideRubiSpaceForOther   191  /* R/W Enum */
   /* Careful to match FV_{WIDE,NARROW,PROPORTIONAL} with RubiSpacingT */
#define FV_WIDE 0
#define FV_NARROW 1
#define FV_PROPORTIONAL 2
#define FP_RubiOverhang            192  /* R/W Boolean */
#define FP_RubiAlignAtLineBounds   193  /* R/W Boolean */
#define FP_FirstRubiInDoc          194  /* R/O ID(TextRangeT type=TXTRNG_OYAMOJI) */
#define FP_ScreenCaptureDocToFile  195  /* W/O STRING(Generic FilePathT) */

   /* Doc Spell-check/hyphenation Properties */
#define FP_Dictionary             203   /* R/W Strings */ 

   /* Doc Volume Properties */
#define FP_VolNumComputeMethod		211  /* R/W Enum - FV_NUM_RESTART et al */
#define FV_NUM_READ_FROM_FILE	0x00
#define FV_NUM_CONTINUE			0x01
#define FV_NUM_RESTART			0x02
#define FV_NUM_SAME				0x03
/* only for fnote compute method. FV_NUM_SAME is not applicable for fnote. */
#define FV_NUM_PERPAGE			0x03			 
#define FP_VolumeNumber			212  /* R/W Integer */
#define FP_VolumeNumStyle		213  /* R/W Enum */
#define FP_VolumeNumText		214

/* Doc Chapter Properties */
#define FP_ChapNumComputeMethod		215	/* R/W Enum - FV_NUM_RESTART et al */
#define FP_ChapterNumber		216	/* R/W Integer */
#define FP_ChapterNumStyle		217	/* R/W Enum, see FV_NUMSTYLE values */
/* These are identical to the values for
 *   FV_PAGE_NUM_NUMERIC and FV_POINT_PAGE_NUM_NUMERIC
 */
#define FV_NUMSTYLE_NUMERIC		0x00
#define FV_NUMSTYLE_ROMAN_UC	0x01
#define FV_NUMSTYLE_ROMAN_LC	0x02
#define FV_NUMSTYLE_ALPHA_UC	0x03
#define FV_NUMSTYLE_ALPHA_LC	0x04
#define FV_NUMSTYLE_KANJI		0x05
#define FV_NUMSTYLE_ZENKAKU		0x06
#define FV_NUMSTYLE_ZENKAKU_UC 	0x07
#define FV_NUMSTYLE_ZENKAKU_LC 	0x08
#define FV_NUMSTYLE_KANJI_KAZU 	0x09
#define FV_NUMSTYLE_DAIJI		0x0a
#define FV_NUMSTYLE_TEXT		0x0b  /* only for volume/chapter numbers */
#define FV_NUMSTYLE_FULLWIDTH		0x0c
#define FV_NUMSTYLE_FULLWIDTH_UC 	0x0d
#define FV_NUMSTYLE_FULLWIDTH_LC 	0x0e
#define FV_NUMSTYLE_CHINESE_NUMERIC	0x10 /* harshg-FM9: Identical to numberingStyle_Chinese_Numeric(M_CHINESENUMERIC ) in fm_text.h */

#define FP_ChapterNumText		218	/* R/W String */

   /* Doc Page Properties */
#define FP_FirstPageNum			224		/* R/W Integer */ 
#define FP_PageNumStyle			225		/* R/W Enum */
/* These are identical to the _NUMSTYLE_ values above */
#define FV_PAGE_NUM_NUMERIC			0x00
#define FV_PAGE_NUM_ROMAN_UC		0x01
#define FV_PAGE_NUM_ROMAN_LC		0x02 
#define FV_PAGE_NUM_ALPHA_UC		0x03 
#define FV_PAGE_NUM_ALPHA_LC		0x04 
#define FV_PAGE_NUM_KANJI			0x05
#define FV_PAGE_NUM_ZENKAKU			0x06
#define FV_PAGE_NUM_ZENKAKU_UC 		0x07
#define FV_PAGE_NUM_ZENKAKU_LC 		0x08
#define FV_PAGE_NUM_KANJI_KAZU 		0x09
#define FV_PAGE_NUM_DAIJI			0x0a
#define FV_PAGE_NUM_FULLWIDTH		0x0c
#define FV_PAGE_NUM_FULLWIDTH_UC 	0x0d
#define FV_PAGE_NUM_FULLWIDTH_LC 	0x0e
#define FV_PAGE_NUM_CHINESE_NUMERIC	0x10 /* harshg-FM9: Identical to numberingStyle_Chinese_Numeric(M_CHINESENUMERIC ) in fm_text.h */

	
#define FP_DocIsDoubleSided            226   /* R/W Boolean */ 
#define FP_FirstPageVerso          227   /* R/W Boolean */ 

#define FP_PointPageNumStyle      228   /* R/W Enum */
/* These are identical to the _NUMSTYLE_ values above */
#define FV_POINT_PAGE_NUM_NUMERIC		0x00 
#define FV_POINT_PAGE_NUM_ROMAN_UC		0x01 
#define FV_POINT_PAGE_NUM_ROMAN_LC		0x02 
#define FV_POINT_PAGE_NUM_ALPHA_UC		0x03 
#define FV_POINT_PAGE_NUM_ALPHA_LC		0x04 
#define FV_POINT_PAGE_NUM_KANJI			0x05 
#define FV_POINT_PAGE_NUM_ZENKAKU		0x06
#define FV_POINT_PAGE_NUM_ZENKAKU_UC 	0x07
#define FV_POINT_PAGE_NUM_ZENKAKU_LC 	0x08
#define FV_POINT_PAGE_NUM_KANJI_KAZU 	0x09
#define FV_POINT_PAGE_NUM_DAIJI			0x0a
#define FV_POINT_PAGE_FULLWIDTH			0x0c /* harshg-FM9: Identical to numberingStyle_FullWidth(M_FULLWIDTH) in fm_text.h */
#define FV_POINT_PAGE_FULLWIDTH_UC 		0x0d
#define FV_POINT_PAGE_FULLWIDTH_LC 		0x0e
#define FV_POINT_PAGE_CHINESE_NUMERIC	0x10

#define FP_PageRounding           229   /* R/W Enum */ 
#define FV_PR_DEL_EMPTY              0x01 
#define FV_PR_KEEP_NUM_EVEN          0x02 
#define FV_PR_KEEP_NUM_ODD           0x03 
#define FV_PR_DONT_CHANGE            0x04 

#define FP_TopMargin              230   /* R/W Metric */ 
#define FP_BottomMargin           231   /* R/W Metric */ 
#define FP_LeftMargin             232   /* R/W Metric */ 
#define FP_RightMargin            233   /* R/W Metric */ 
#define FP_ColGap                 234   /* R/W Metric */ 
#define FP_NumCols                235   /* R/O Ord */ 
#define FP_CurrentPage            236   /* R/W ID(Page) */

   /*Doc Type-In Properties */
#define FP_SmartQuotes            237   /* R/W Boolean */ 
#define FP_AutoChangeBars         238   /* R/W Boolean */ 
#define FP_SmartSpaces            239   /* R/W Boolean */ 
#define FP_CurrentInset			  240   /* Added for PDF Client - SKT */

   /* Doc Change Bar Look */
#define FP_ChangeBarDistance      259   /* R/W Metric */ 
#define FP_ChangeBarPosition      260   /* R/W Enum */ 
#define FV_CB_COL_LEFT               0x00 
#define FV_CB_COL_RIGHT              0x01 
#define FV_CB_COL_NEAREST                0x02 
#define FV_CB_COL_FURTHEST               0x03 
#define FP_ChangeBarThickness     261   /* R/W Metric */ 
#define FP_ChangeBarColor         262   /* R/W ID(Color) */ 

    /* Doc Foot Note Properties */
#define FP_FnFmt               283   /* R/W String */ 
#define FP_FnFirstNum             284   /* R/W Ordinal */ 
#define FP_FnCustNumString           285   /* R/W String */ 
#define FP_FnRefPrefix      286   /* R/W String */ 
#define FP_FnInstancePosition     287   /* R/W Enum */ 
#define FV_FN_POS_SUPER              0x00 
#define FV_FN_POS_BASELINE               0x01 
#define FV_FN_POS_SUB              0x02 

#define FP_FnInstanceSuffix       288   /* R/W String */ 
#define FP_FnHeightPerCol         289   /* R/W Metric */ 
#define FP_FnNumStyle          290   /* R/W Enum */ 
/* These DIFFER from the _NUMSTYLE_ values at value CUSTOM and up */
#define FV_FN_NUM_NUMERIC		0x00 
#define FV_FN_NUM_ROMAN_UC		0x01 
#define FV_FN_NUM_ROMAN_LC		0x02 
#define FV_FN_NUM_ALPHA_UC		0x03 
#define FV_FN_NUM_ALPHA_LC		0x04 
#define FV_FN_NUM_CUSTOM		0x05 
#define FV_FN_NUM_KANJI			0x06
#define FV_FN_NUM_ZENKAKU		0x07
#define FV_FN_NUM_ZENKAKU_UC 	0x08
#define FV_FN_NUM_ZENKAKU_LC 	0x09
#define FV_FN_NUM_KANJI_KAZU 	0x0A
#define FV_FN_NUM_DAIJI			0x0B
#define FV_FN_NUM_FULL_WIDTH	0x0C
#define FV_FN_NUM_FULL_WIDTH_UC	0x0D
#define FV_FN_NUM_FULL_WIDTH_LC	0x0E
#define FV_FN_NUM_CHINESE_NUMERIC	0x10
/*
 * FP_FnNumberingPerpage is retained for compatibility with old clients.
 * It can not be mapped to any of the new properties in FM6.
 */
#define FP_FnNumberingPerPage	291   /* R/W Boolean */ 
#define FP_FnRefPosition    292   /* R/W Enum *//*See FnInstancePostion */ 
#define FP_FnRefSuffix      293   /* R/W String */ 
#define FP_FnInstancePrefix       294   /* R/W String */ 
#define FP_FnNumComputeMethod	  295 /* R/W Enum - FV_NUM_RESTART,       
									   * FV_NUM_CONTINUE & FV_NUM_PERPAGE */

/* Doc Table Foot Note Properties */
#define FP_TblFnFmt          315   /* R/W String */ 
#define FP_TblFnNumStyle     316   /* R/W Enum */ 
#define FP_TblFnCustNumString      317   /* R/W String */ 
#define FP_TblFnCellPosition    318   /* R/W Enum */ 
#define FP_TblFnCellSuffix      319   /* R/W String */ 
#define FP_TblFnCellPrefix      320   /* R/W String */ 
#define FP_TblFnPosition        321   /* R/W Enum */ 
#define FP_TblFnPrefix          322   /* R/W String */ 
#define FP_TblFnSuffix          323   /* R/W String */ 
#define FP_TblFnNumComputeMethod 324   /* R/W Enum */ 

   /* Doc Equation Properties */
#define FP_Symbols				 334	/* R/W String */
#define FP_SymbolsList			 335    /* R/O StringList */
#define FP_Variables			 336	/* R/W String */
#define FP_Strings			 	 337	/* R/W String */
#define FP_Numbers				 338	/* R/W String */
#define FP_Functions 			 339	/* R/W String */
#define FP_HorizontalSpreadSmall 340	/* R/W MetricPercent */ 
#define FP_HorizontalSpreadMed	 341	/* R/W MetricPercent */ 
#define FP_HorizontalSpreadLarge 342	/* R/W MetricPercent */ 
#define FP_VerticalSpreadSmall	 343	/* R/W MetricPercent */ 
#define FP_VerticalSpreadMed	 344	/* R/W MetricPercent */ 
#define FP_VerticalSpreadLarge	 345	/* R/W MetricPercent */ 
#define FP_EqnIntegralSizeSmall  346   /* R/W Metric */ 
#define FP_EqnIntegralSizeMed    347   /* R/W Metric */ 
#define FP_EqnIntegralSizeLarge 348   /* R/W Metric */ 
#define FP_EqnSigmaSizeSmall 349   /* R/W Metric */ 
#define FP_EqnSigmaSizeMed   350   /* R/W Metric */ 
#define FP_EqnSigmaSizeLarge 351   /* R/W Metric */ 
#define FP_EqnLevel1SizeSmall 352   /* R/W Metric */ 
#define FP_EqnLevel1SizeMed  353   /* R/W Metric */ 
#define FP_EqnLevel1SizeLarge 354   /* R/W Metric */ 
#define FP_EqnLevel2SizeSmall 355   /* R/W Metric */ 
#define FP_EqnLevel2SizeMed  356   /* R/W Metric */ 
#define FP_EqnLevel2SizeLarge 357   /* R/W Metric */ 
#define FP_EqnLevel3SizeSmall 358   /* R/W Metric */ 
#define FP_EqnLevel3SizeMed  359   /* R/W Metric */ 
#define FP_EqnLevel3SizeLarge 360   /* R/W Metric */ 

  /* Doc View Properties */
#define FP_ViewBorders            382   /* R/W Boolean */ 
#define FP_ViewRulers             383   /* R/W Boolean */ 
#define FP_ViewNoGraphics         384   /* R/W Boolean */ 
#define FP_ViewPageScrolling      385   /* R/W Enum */ 
#define FV_SCROLL_VARIABLE                 0 
#define FV_SCROLL_HORIZONTAL               1 
#define FV_SCROLL_VERTICAL                 2 
#define FV_SCROLL_FACING                   3 

#define FP_ViewGridUnits          386   /* R/W Metric */ 
#define FP_Zoom                   387   /* R/W MetricPercent */ 
#define FV_NO_ZOOM_MANGLING			-1
#define FV_ZOOM_TO_FIT_TO_WINDOW	-2
#define FV_FIT_WINDOW_TO_PAGE		-3
#define FP_ViewTextSymbols        388   /* R/W Boolean */ 
#define FP_ViewGrid               389   /* R/W Boolean */ 
#define FP_ViewDisplayUnits       390   /* R/W Metric */


/*Doc Track Changes*/
#define FP_TrackChangesOn		2819	/*R/W Boolean*/
#define FP_PreviewState			2820	/* R/W Enum*/
#define FV_PREVIEW_OFF_TRACK_CHANGE 0
#define FV_PREVIEW_ON_ORIGINAL      1
#define FV_PREVIEW_ON_FINAL         2


/* The valid property values for this property are
 * metric inch, cm, mm, pica, point, didot and cicero.  
 * these are the same values as are displayed in the view options
 * dialog. The defined values follow:
 */
#define FV_METRIC_INCH   ((MetricT)0x480000)	/* 1" == 72 points exactly */
#define FV_METRIC_CM     ((MetricT)0x1c58b1)	/* 1" == 2.54cm exactly */
#define FV_METRIC_MM	 ((MetricT)0x02d5ab)	/* 1" == 25.4mm exactly */
#define FV_METRIC_PICA   ((MetricT)0x0c0000)	/* 1pica == 12 points exactly */
#define FV_METRIC_POINT   ((MetricT)0x010000)	
#define FV_METRIC_DIDOT   ((MetricT)0x011159)	/* 1didot == 0.01483" */
#define FV_METRIC_CICERO  ((MetricT)0x0cd02c)	/* 1cicero == 12didot */

#define FP_ViewRulerUnits       391   /* R/W Metric */
/* The valid property values for this property are
 * metric cm, 1/2 cm mm, pica, 1/8 inch, 1/10 inch, 1/12 inch.
 * these are the same values as are displayed in the view options
 * dialog.
 */

#define FP_SpotColorView          392   /* R/W Ordinal */ 
#define FP_SnapGridUnits          393   /* R/W Metric */
/* The valid property values for this property are
 * metric cm, 2 cm, 1/2 cm inch, 1/3 inch, 1/2 inch, 1/4 inch.
 * these are the same values as are displayed in the view options
 * dialog.
 */

#define FP_SnapAngle              394   /* R/W Metric */ 
#define FP_ViewLinkBoundaries	  395   /* R/W Boolean */
/* The valid property valius for this property ate
 * metric point or Q.
 */
#define FV_METRIC_Q	 ((MetricT)0x00b56a)	/* 1Q = .25mm */

#define FP_ViewFontSizeUnits      396   /* R/W Metric */ 

   /* Doc Pointers */
#define FP_LeftMasterPage         413   /* R/O ID(MasterPage) */ 
#define FP_RightMasterPage        414   /* R/O ID(MasterPage) */ 
#define FP_FirstBodyPageInDoc     415   /* R/O ID(BodyPage) */ 
#define FP_LastBodyPageInDoc      416   /* R/O ID(BodyPage) */ 
#define FP_FirstMasterPageInDoc   417   /* R/O ID(MasterPage) */ 
#define FP_LastMasterPageInDoc    418   /* R/O ID(MasterPage) */ 
#define FP_FirstRefPageInDoc      419   /* R/O ID(ReferencePage) */ 
#define FP_LastRefPageInDoc       420   /* R/O ID(ReferencePage) */ 
#define FP_HiddenPage             421   /* R/O ID(HiddenPage) */ 
#define FP_MainFlowInDoc          422   /* R/O ID(Flow) */ 

#ifdef FAPI_4_BEHAVIOR
#define FP_DocIsOnScreen FP_IsOnScreen
#define FP_BodyHead FP_FirstBodyPageInDoc
#define FP_BodyTail FP_LastBodyPageInDoc
#define FP_MasterHead FP_FirstMasterPageInDoc
#define FP_MasterTail FP_LastMasterPageInDoc
#define FP_RefHead FP_FirstRefPageInDoc
#define FP_RefTail FP_LastRefPageInDoc
#endif /* FAPI_4_BEHAVIOR */

   /* Doc Print Props */
#define FP_PrintStartPageName     439   /* R/W String */
#define FP_PrintEndPageName       440   /* R/W String */
#define FP_PrintCols              441   /* R/W Integer */
#define FP_PrintRows              442   /* R/W Integer */
#define FP_PrintStartPage         443   /* R/W Integer */ 
#define FP_PrintStartPoint        444   /* R/W Integer */ 
#define FP_PrintOddPages          445   /* R/W Boolean */ 
#define FP_PrintCollated          446   /* R/W Boolean */ 
#define FP_PrintLowRes            447   /* R/W Boolean */ 
#define FP_PrintThumbnails        448   /* R/W Boolean */ 
#define FP_PrinterName            449   /* R/W String */ 
#define FP_PrintScale             450   /* R/W IntegerPercent */ 
#define FP_PrintFileName          451   /* R/W String */ 
#define FP_PrintDitavalFileName   2343  /* R/W String */ 
#define FP_PrintScope             452   /* R/W Enum */ 
#define FV_PR_ALL       1
#define FV_PR_RANGE     2
#define FP_PrintEndPage           453   /* R/W Integer */ 
#define FP_PrintEndPoint          454   /* R/W Integer */ 
#define FP_PrintEvenPages         455   /* R/W Boolean */ 
#define FP_PrintLastSheetFirst    456   /* R/W Boolean */ 
#define FP_PrintRegistrationMarks 457   /* R/W Boolean */ 
#define FP_PrintManualFeed        458   /* R/W Boolean */ 
#define FP_PrintNumCopies         459   /* R/W Ordinal */ 
#define FP_PrintToFile            460   /* R/W Boolean */ 
#define FP_PrintPaperWidth        461   /* R/W Metric */ 
#define FP_PrintPaperHeight		  462   /* R/W Metric */ 
#define FP_PrintSeps       		  463   /* R/W Boolean */ 
#define FP_SkipBlankSeps  		  464   /* R/W Boolean */ 
#define FP_PrintImaging        	  465	/* R/W Boolean */ 
#define FV_IMG_POSITIVE 0
#define FV_IMG_NEGATIVE 1
#define FP_PrintEmulsion     	  466   /* R/W Boolean */ 
#define FV_EMUL_UP   0
#define FV_EMUL_DOWN 1
#define FP_PrintBlankPages        467   /* R/W Boolean */ 
#define FP_PrintTomboMarks		  468	/* R/W Boolean - Only used if FP_PrintRegistrationMarks is also true */
#define FP_PrintRegMarkDate       469   /* R/W Boolean */
#define FP_TrapwiseCompatibility  495	/* R/W Boolean - Macintosh only for now */
#define FP_DownloadFonts		  496	/* R/W Enum - Unix only for now */
#define FP_PrintSpotBW            498   /* R/W Boolean - Windows only */
#define FV_PR_DOWNLOAD_NONE						1
#define FV_PR_DOWNLOAD_ALL						2
#define FV_PR_DOWNLOAD_ALL_BUT_STANDARD_13		3
#define FV_PR_DOWNLOAD_ALL_BUT_STANDARD_35		4
#define FP_PrintDownloadAsianFonts	501	/* R/W Boolean - Unix only */
#define FP_PrintDownloadTrueTypeAsType1	502	/* R/W Boolean - Unix only */

			/* Acrobat stuff */
#define FP_GenerateAcrobatInfo	  2250	/* R/W Boolean */
#define FP_AcrobatBookmarkDisplayTags 2251 /* R/W Boolean */
#define FP_DocAcrobatDefaultsChanged 2252 /* R/O Boolean */
#define FP_DocAcrobatElements	  2253	/* R/W Boolean */
#define FP_DocAcrobatElementList  2254	/* R/W StringList */
#define FP_DocAcrobatNoArticleThreads	  2255	/* R/W Boolean */
#define FP_DocAcrobatColumnArticleThreads 2256	/* R/W Boolean */
#define FP_PDFAllNamedDestinations 2257	/* R/W Boolean */
#define FP_PDFDestsMarked  2258	/* R/W Boolean */
#define FP_PDFStructure    2259	/* R/W Boolean */
#define FP_PDFDocInfo  2260  /* R/W StringList, a list of name/value pairs
                              *   in a specific format- see the manual.
                              */
#define FP_PDFBookmark 2261  /* R/W Boolean */
#define FP_FileInfoPacket 2262 /* R/W FileInfo packet string */
	/* PDF Settings related entries. */
#define FP_PDFJobOption 2263                 /* R/W String */
#define FP_PDFOpenPage  2264                 /* R/W String    */
#define FP_PDFZoomType  2265                 /* R/W Int    */
   /* The values for FP_PDFZoomType. */
   /* These values must be in sinc with SettingsZoomOptionsT
    * in pdfsetupui.c
    */
#define FV_PDFZoomNone 0
#define FV_PDFZoomDefault  1
#define FV_PDFZoomPage 2
#define FV_PDFZoomWidth 3
#define FV_PDFZoomHeight 4
#define FV_PDFZoomMaxValue 4
    /* Values for FP_PDFRegistrationMarks */
	/* They must be in sinc with SettingsRegmarksOptionsT in pdfsetupui.c */
							  
#define FV_PDFRegistrationMarksNone 0
#define FV_PDFRegistrationMarksWestern 1
#define FV_PDFRegistrationMarksTombo 2
#define FV_PDFRegistrationMarksMax   2

#define FP_PDFZoomFactor 2266                /* R/W Metric */
#define FP_PDFSeparateFiles 2267             /* R/W flag bit(PDFSETTINGSEPFILES) */
#define FP_PDFRegistrationMarks 2268         /* R/W Int    */
#define FP_PDFPageWidth 2269                 /* R/W Metric */
#define FP_PDFPageHeight 2270                /* R/W Metric */
#define FP_PDFPrintPageRange 2271             /* R/W flag bit(PDFSETTINGSALLPAGES) */
#define FP_PDFStartPage 2272                 /* R/W String    */
#define FP_PDFEndPage 2273                   /* R/W String    */
#define FP_PDFConvertCMYKtoRGB 2274          /* R/W flag bit(PDFSETTINGCMYKTORGB) */
#define FP_PDFBookmarksOpenLevel 2275        /* R/W Int    */
#define FP_PDFDistillerAbsent 2276           /* RO  flag bit(PDFDISTILLERABSENT) */
#define FP_PDFJobOptionsAbsent 2277          /* RO  flag bit(PDFJOBOPTIONSABSENT) */
#define FP_PDFViewPDF 2278                   /* R/W flag bit(PDFSETTINGVIEWPDF) */
#define FP_PDFGenerateForReview 2279         /* R/W flag bit(PDFSETTINGREVIEW) */
  /* Indicies for the FP_PDFBookmarksOpenLevel string values */
  /* They must be in sync with the order in the BookmarksLevelSrs
   * array in pdfsetupui.c
   */
#define FV_PDFBookmarksOpenDefaultLevel -1
#define FV_PDFBookmarksOpenAllLevels    -2
#define FV_PDFBookmarksOpenNoneLevel    -3

							  /* doc fcl limits */
#define FP_MaxFirstIndent		 472	/* R/W Metric */
#define FP_MinFirstIndent		 473	/* R/W Metric */
#define FP_MaxLeftIndent		 474	/* R/W Metric */
#define FP_MinLeftIndent		 475	/* R/W Metric */
#define FP_MaxRightIndent		 476	/* R/W Metric */
#define FP_MinRightIndent		 477	/* R/W Metric */
#define FP_MaxSpaceAbove		 478	/* R/W Metric */
#define FP_MinSpaceAbove		 479	/* R/W Metric */
#define FP_MaxSpaceBelow		 480	/* R/W Metric */
#define FP_MinSpaceBelow		 481	/* R/W Metric */
#define FP_MaxLeading 			 482	/* R/W Metric */
#define FP_MinLeading			 483	/* R/W Metric */
#define FP_MaxFontSize			 484	/* R/W Metric */
#define FP_MinFontSize 			 485	/* R/W Metric */
#define FP_MaxSpread			 486	/* R/W Metric */
#define FP_MinSpread 			 487	/* R/W Metric */
#define FP_MaxTabPosition		 701    /* R/W Metric */
#define FP_MinTabPosition		 702    /* R/W Metric */
#define FP_MaxLeftMargin	 	 703    /* R/W Metric */
#define FP_MinLeftMargin	 	 704    /* R/W Metric */
#define FP_MaxRightMargin	 	 705    /* R/W Metric */
#define FP_MinRightMargin	 	 706    /* R/W Metric */
#define FP_MaxTopMargin		 	 707    /* R/W Metric */
#define FP_MinTopMargin		 	 708    /* R/W Metric */
#define FP_MaxBottomMargin	 	 709    /* R/W Metric */
#define FP_MinBottomMargin	 	 710    /* R/W Metric */
#define FP_MaxStretch	     	 711	/* R/W Metric */
#define FP_MinStretch			 712	/* R/W Metric */

/* Doc Magic Props */
#define FP_MagicMarker            488   /* R/W Ordinal */

/* Doc xml properties */
#define FP_XmlVersion			2800	/* R/W String   */
#define FP_XmlEncoding			2801	/* R/W String   */
#define FP_XmlStandAlone		2802	/* R/W Int      */
#define FV_XML_STANDALONE_YES	1
#define FV_XML_STANDALONE_NO	2
#define FV_XML_STANDALONE_NONE	3
#define FV_XML_STANDALONE_NODEC	4
#define FP_XmlStyleSheet		2803	/* R/W String   */
#define FP_XmlStyleSheetList	2804	/* R/W StrList  */
#define FP_XmlUseBOM		    2805	/* R/W Int      */
#define FV_XML_USEBOM_YES	    1
#define FV_XML_USEBOM_NO	    2
#define FV_XML_USEBOM_UTF8    3
#define FV_XML_USEBOM_UTF16BE 4
#define FV_XML_USEBOM_UTF16LE 5
#define FV_XML_USEBOM_UTF32BE 6
#define FV_XML_USEBOM_UTF32LE 7
#define FP_XmlWellFormed		2806	/* R/W Int      */
#define FV_XML_WELLFORMED_YES	1
#define FV_XML_WELLFORMED_NO	2
#define FP_XmlFileEncoding		2809	/* R/W String   */
#define FP_XmlDocType		    2810	/* R/W String   */
#define FP_XmlPublicId		    2811	/* R/W String   */
#define FP_XmlSystemId		    2812	/* R/W String   */

/* Doc WebDAV properties */
#define FP_ServerUrl			2807	/* R/W String   */
#define FP_ServerState			2808	/* R/W Enum   */
#define FV_URL_CHECKED_IN       1
#define FV_URL_CHECKED_OUT      2


   /* Book */
#define FP_NextOpenBookInSession  490   /* R/O ID(Book) */ 
#define FP_FirstComponentInBook   491   /* R/W ID(BookComponent) */ 
#define FP_BookIsModified         492   /* R/O Boolean */ 
#define FP_FirstSelectedComponentInBook	493 /* R/O ID(BookComponent) */
#define FP_BookDontUpdateReferences 494 /* R/W Boolean */
#define FP_BookIsViewOnly         497   /* R/W Boolean */
#define FP_BookIsSelected		  499	/* R/W Boolean */
#define FP_TypeOfDisplayText      500   /* R/W Enum */
#define FV_BK_FILENAME            1
#define FV_BK_TEXT                2

   /* Book Component */
#define FP_BookParent             515   /* R/O ID(Book) */ 
#define FP_ExtractTags            516   /* R/W Strings */
#define FP_GenerateInclude        517   /* R/W Boolean */ 
#define FP_ImportFmtInclude          518   /* R/W Boolean */ 
#define FP_PrintInclude           519   /* R/W Boolean */ 
#define FP_BookComponentIsGeneratable            520   /* R/O Boolean */ 
/*
 * FP_PagePrefix and FP_PageSuffix are obsolete.  They have been retained for
 * compatibility with old clients and cannot be mapped to any other properties.
 */
#define FP_PagePrefix             521   /* R/W String */ 
#define FP_PageSuffix             522   /* R/W String */ 
#define FP_PageSide               523   /* R/W Enum */ 
#define FV_BK_START_FROM_FILE        0x01 
#define FV_BK_START_NEXT_AVAILABLE   0x02 
#define FV_BK_START_LEFT             0x03 
#define FV_BK_START_RIGHT            0x04 
#define FP_PageNumComputeMethod      524   /* R/W Enum */ 
#define FP_PgfNumComputeMethod       525   /* R/W Enum - for docs, too */

#ifdef FAPI_55_BEHAVIOR
#define FP_PageNumbering          FP_PageNumComputeMethod
#define FP_PgfNumbering           FP_PgfNumComputeMethod
#define FV_BK_CONT_PAGE_NUM       FV_NUM_CONTINUE
#define FV_BK_RESET_PAGE_NUM      FV_NUM_RESTART
#define FV_BK_READ_FROM_FILE 	  FV_NUM_READ_FROM_FILE
#define FV_BK_CONT_PGF_NUM        FV_NUM_CONTINUE
#define FV_BK_RESTART_PGF_NUM     FV_NUM_RESTART
#endif /* FAPI_55_BEHAVIOR */

#define FP_PrevComponentInBook    526   /* R/W ID(BookComponent) */ 
#define FP_NextComponentInBook    527   /* R/W ID(BookComponent) */ 
#define FP_InsertLinks            528   /* R/W Boolean */
#define FP_ComponentIsSelected    529   /* R/W Boolean */
#define FP_NextSelectedComponentInBook	530 /* R/O ID(BookComponent) */
#define FP_BookComponentType            531   /* R/W Enum */ 
#define FV_BK_TOC                 0
#define FV_BK_LIST_FIGURE         1
#define FV_BK_LIST_TABLE          2
#define FV_BK_LIST_PGF            3
#define FV_BK_LIST_MARKER         4
#define FV_BK_LIST_MARKER_ALPHA   5
#define FV_BK_LIST_PGF_ALPHA      6
#define FV_BK_INDEX_STAN      7
#define FV_BK_INDEX_AUTHOR    8
#define FV_BK_INDEX_SUBJECT   9
#define FV_BK_INDEX_MARKER    10
#define FV_BK_LIST_FORMATS    11
#define FV_BK_LIST_REFERENCES 12
#define FV_BK_INDEX_FORMATS   13
#define FV_BK_INDEX_REFERENCES    14
#define FV_BK_NUM_BOOK_LIST   15
#define FV_BK_NOT_GENERATABLE 16
#define FP_ComponentDisplayText  532

#define	FP_FirstComponentInBookComponent	2324	/* R/W ID(BookComponent) */ 
#define FP_BookComponentParent				2325	/* R/O (ID parent book component) */
#define	FP_ExcludeBookComponent				2326	/* R/W Boolean */
#define	FP_BookComponentTemplatePath		2327	/* R/W String. */
#define	FP_BookComponentTitle				2328	/* R/W string. */
#define	FP_ComponentType					2329	/* R/O component type. i.e Folder, Group etc. */
#define	FP_ComponentIsDitaMap				2331
#define	FV_BK_GENERAL						0x1
#define	FV_BK_FOLDER						0x2
#define	FV_BK_BOOK							0x4
#define	FV_BK_FM							0x8
#define	FV_BK_MIF							0x10
#define	FV_BK_XML							0x20
#define	FV_BK_GROUP							0x40
#define	FV_BK_DITAMAP						0x80
#define	FV_BK_BOOKMAP						0x100
#define	FV_BK_FILE							0x200

#define	FP_NextBookComponentInDFSOrder	    2333
#define FP_BookComponentFileType            2339
#define	FP_PrevBookComponentInDFSOrder	    2340

#define FP_BookComponentIsFolderWithTemplate    2341
#define FP_BookComponentIsFolderWithoutTemplate 2342

#define	FP_XmlApplicationForBookComponent	2330


/* Used for actions to be taken in F_ApiMoveBookComponent API. */
#define	FA_COMPONENT_MOVEUP				1
#define	FA_COMPONENT_MOVEDOWN			2
#define	FA_COMPONENT_PROMOTE			3
#define	FA_COMPONENT_DEMOTE				4


   /* Pgf Format */
#define FP_PgfAlignment           547   /* R/W Enum */ 
#define FV_PGF_LEFT                  0x01
#define FV_PGF_RIGHT                 0x02
#define FV_PGF_CENTER                0x03
#define FV_PGF_JUSTIFIED             0x04
#define FP_AutoNumString          548   /* R/W String */ 
#define FP_AutoNumChar            549   /* R/W String */ 
#define FP_LetterSpace            550   /* R/W Boolean */ 
#define FP_KeepWithPrev           551   /* R/W Boolean */ 
#define FP_NextTag                552   /* R/W String */ 
#define FP_SpaceAbove             553   /* R/W Metric */ 
#define FP_TopSeparator           554   /* R/W String */ 
#define FP_LeftIndent             555   /* R/W Metric */ 
#define FP_FirstIndent            556   /* R/W Metric */ 
#define FP_OptSpace               557   /* R/W Metric */ 
#define FP_Leading                558   /* R/W Metric */
#define FP_AdjHyphens             559   /* R/W Ordinal */ 
#define FP_HyphMinSuffix          560   /* R/W Ordinal */ 
#define FP_BlockLines             561   /* R/W Ordinal */ 
#define FP_PgfIsAutoNum                562   /* R/W Boolean */ 
#define FP_NumAtEnd               563   /* R/W Boolean */ 
#define FP_Hyphenate              564   /* R/W Boolean */ 
#define FP_KeepWithNext           565   /* R/W Boolean */ 
#define FP_UseNextTag             566   /* R/W Boolean */ 
#define FP_Start                  567   /* R/W Enum */ 
#define FV_PGF_ANYWHERE              0x00 
#define FV_PGF_TOP_OF_COL            0x01 
#define FV_PGF_TOP_OF_PAGE           0x02 
#define FV_PGF_TOP_OF_LEFT_PAGE         0x03 
#define FV_PGF_TOP_OF_RIGHT_PAGE         0x04 
#define FP_SpaceBelow             568   /* R/W Metric */ 
#define FP_BottomSeparator           569   /* R/W String */ 
#define FP_RightIndent            570   /* R/W Metric */ 
#define FP_MinSpace               571   /* R/W Metric */ 
#define FP_MaxSpace               572   /* R/W Metric */ 
#define FP_NumTabs                573   /* R/O Int */ 
#define FP_Tabs                   574   /* R/W Tabs */ 
#define FP_HyphMinPrefix          575   /* R/W Ordinal */ 
#define FP_HyphMinWord            576   /* R/W Ordinal */ 
#define FP_Language               577   /* R/W Enum */ 
#define FV_LANG_NOLANGUAGE   0x00
#define FV_LANG_ENGLISH      0x01
#define FV_LANG_BRITISH      0x02
#define FV_LANG_GERMAN       0x03
#define FV_LANG_SWISS_GERMAN 0x04
#define FV_LANG_FRENCH       0x05
#define FV_LANG_CANADIAN_FRENCH  0x06
#define FV_LANG_SPANISH      0x07
#define FV_LANG_CATALAN      0x08
#define FV_LANG_ITALIAN      0x09
#define FV_LANG_PORTUGUESE   0x0A
#define FV_LANG_BRAZILIAN    0x0B
#define FV_LANG_DANISH       0x0C
#define FV_LANG_DUTCH        0x0D
#define FV_LANG_NORWEGIAN    0x0E
#define FV_LANG_NYNORSK      0x0F
#define FV_LANG_FINNISH      0x10
#define FV_LANG_SWEDISH      0x11
#define FV_LANG_JAPANESE     0x12
#define FV_LANG_TRADITIONAL_CHINESE 0x13
#define FV_LANG_SIMPLIFIED_CHINESE  0x14
#define FV_LANG_KOREAN       0x15
#define FV_LANG_NEW_GERMAN   0x16
#define FV_LANG_NEW_SWISS_GERMAN    0x17
#define FV_LANG_NEW_DUTCH    0x18
#define FV_LANG_GREEK		 0x19	
#define FV_LANG_RUSSIAN		 0x1A	
#define FV_LANG_CZECH		 0x1B	
#define FV_LANG_POLISH		 0x1C	
#define FV_LANG_HUNGARIAN	 0x1D	
#define FV_LANG_TURKISH		 0x1E	
#define FV_LANG_SLOVAK		 0x1F	
#define FV_LANG_SLOVENIAN	 0x20	
#define FV_LANG_BULGARIAN	 0x21	
#define FV_LANG_CROATIAN	 0x22	
#define FV_LANG_ESTONIAN	 0x23	
#define FV_LANG_LATVIAN		 0x24	
#define FV_LANG_LITHUANIAN	 0x25	
#define FV_LANG_ROMANIAN	 0x26	
#define FV_LANG_NUM          0x27 /* Number of languages */ 

#define FP_RunInSeparator         578   /* R/W String */ 
#define FP_Placement              579   /* R/W Enum */ 
#define FV_PGF_SIDEBODY              0 
#define FV_PGF_SIDEHEAD_TOP          1 
#define FV_PGF_SIDEHEAD_FIRST_BASELINE 2 
#define FV_PGF_SIDEHEAD_LAST_BASELINE 3 
#define FV_PGF_RUN_IN                4 
#define FV_PGF_STRADDLE              5 
#define FV_PGF_STRADDLE_NORMAL_ONLY  6
#define FP_NextPgfFmtInDoc      580   /* R/O ID(PgfFmt) */ 
#define FP_CellTopMargin		581  /* R/W Metric */ 
#define FP_CellBottomMargin		582   /* R/W Metric */ 
#define FP_CellLeftMargin		583   /* R/W Metric */ 
#define FP_CellRightMargin		584   /* R/W Metric */ 
#define FP_CellVAlignment		585   /* R/W Enum */
#define FV_PGF_V_ALIGN_TOP 0
#define FV_PGF_V_ALIGN_MIDDLE 1
#define FV_PGF_V_ALIGN_BOTTOM 2
#define FP_CellMarginsFixed		586   /* R/W Enum */
#define FV_PGF_FIXED_L_MARGIN 0x01
#define FV_PGF_FIXED_B_MARGIN 0x02
#define FV_PGF_FIXED_R_MARGIN 0x04
#define FV_PGF_FIXED_T_MARGIN 0x08
#define FP_LineSpacing			587   /* R/W Enum */
#define FV_PGF_FIXED           0x00
#define FV_PGF_PROPORTIONAL    0x01
#define FV_PGF_FLOATING        0x02
#define FP_Locked			   588      /* R/W Bool.  Is this paragraph
										 * locked against formatting changes?
										 * True when in a text inset whose
										 * formatting comes from the source
										 * document.
										 */
#define FP_AcrobatLevel		   589		/* R/W Int */
#define FP_PDFStructureLevel   600		/* R/W Int */
#define FP_FormatOverride      590		/* R/O Bool */
#define FP_MinJRomSpace		   591		/* R/W MetricT */
#define FP_OptJRomSpace		   592		/* R/W MetricT */
#define FP_MaxJRomSpace		   593		/* R/W MetricT */
#define FP_MinJLetSpace		   594		/* R/W MetricT */
#define FP_OptJLetSpace		   595		/* R/W MetricT */
#define FP_MaxJLetSpace		   596		/* R/W MetricT */
#define FP_YakumonoType		   597		/* R/W Enum */
#define FV_FLOATING_YAKUMONO  0x00
#define FV_MONOSPACE_YAKUMONO 0x01
#define FV_FIXED_YAKUMONO     0x02
#define FP_DialogEncodingName      598		/* R/O StringT */
#define FP_FMInterfaceEncodingName 599		/* R/O StringT */

   /* Char Format */
#define FP_CharTag                602   /* R/W String */ 
#define FP_NextCharFmtInDoc       603   /* R/O ID(CharFmt) */ 
#define FP_FontFamily             604   /* R/W Ordinal */ 
#define FP_FontVariation          605   /* R/W Ordinal */ 
#define FP_FontWeight             606   /* R/W Ordinal */ 
#define FP_FontAngle              607   /* R/W Ordinal */ 
#define FP_Underlining            608   /* R/W Enum */ 
#define FV_CB_NO_UNDERLINE      0
#define FV_CB_SINGLE_UNDERLINE  1
#define FV_CB_DOUBLE_UNDERLINE  2
#define FV_CB_NUMERIC_UNDERLINE 3

#define FP_Strikethrough              609   /* R/W Boolean */ 
#define FP_Overline               610   /* R/W Boolean */ 
#define FP_ChangeBar              611   /* R/W Boolean */ 
#define FP_Outline                612   /* R/W Boolean */ 
#define FP_Shadow                 613   /* R/W Boolean */ 
#define FP_PairKern               614   /* R/W Boolean */ 
#define FP_FontSize               615   /* R/W Metric */ 
#define FP_KernX                  616   /* R/W Metric */ 
#define FP_KernY                  617   /* R/W Metric */ 
#define FP_Spread                 618   /* R/W MetricPercent */ 
#define FP_Capitalization         619   /* R/W Enum */ 
#define FV_CAPITAL_CASE_NORM           0 
#define FV_CAPITAL_CASE_SMALL          1 
#define FV_CAPITAL_CASE_LOWER          2 
#define FV_CAPITAL_CASE_UPPER          3 
#define FP_Position               620   /* R/W Enum */ 
#define FV_POS_NORM            0
#define FV_POS_SUPER           1
#define FV_POS_SUB             2

#define FP_UseFontFamily          621   /* R/W Boolean */ 
#define FP_UseFontVariation       622   /* R/W Boolean */ 
#define FP_UseFontWeight          623   /* R/W Boolean */ 
#define FP_UseFontAngle           624   /* R/W Boolean */ 
#define FP_UseUnderlining         625   /* R/W Boolean */ 
#define FP_UseStrikethrough       626   /* R/W Boolean */ 
#define FP_UseOverline            627   /* R/W Boolean */ 
#define FP_UseChangeBar           628   /* R/W Boolean */ 
#define FP_UseOutline             629   /* R/W Boolean */ 
#define FP_UseShadow              630   /* R/W Boolean */ 
#define FP_UsePairKern            631   /* R/W Boolean */ 
#define FP_UseFontSize            632   /* R/W Boolean */ 
#define FP_UseKernX               633   /* R/W Boolean */ 
#define FP_UseKernY               634   /* R/W Boolean */ 
#define FP_UseSpread              635   /* R/W Boolean */ 
#define FP_UseCapitalization      636   /* R/W Boolean */ 
#define FP_UsePosition            637   /* R/W Boolean */ 
#define FP_UseColor               638   /* R/W Boolean */ 
#define FP_FontPlatformName	  	  639	/* R/W String */
#define FP_FontPostScriptName	  640	/* R/W String */
#define FP_FontPanoseName	  	  641	/* R/W String */
#define FP_FontEncodingName 	  642	/* R/W String */
#define FP_Stretch                643   /* R/W MetricPercent */ 
#define FP_UseStretch             644   /* R/W Boolean */ 
#define FP_UseLanguage			  645   /* R/W Boolean */
#define FP_WesternFontPlatformName	  646	/* R/W String */
#define FP_WesternFontPostScriptName  647	/* R/W String */
#define FP_WesternFontPanoseName	  648	/* R/W String */
#define FP_Tsume				  649	/* R/W Boolean */
#define FP_UseTsume				  650	/* R/W Boolean */
/* FP_Locked #define  R/W Bool.  Is this char format locked against 
 * formatting changes? True when in a text inset whose
 * formatting comes from the source document.
*/
   /* Tabs: For use in setting F_TabT.type */
   /* the xxx_RELATIVE_xxx values are allowed only for tab stops
	* in FO_FmtChangeList objects 
	*/
#define FV_TAB_LEFT                0x1 
#define FV_TAB_CENTER              0x2 
#define FV_TAB_RIGHT               0x3 
#define FV_TAB_DECIMAL             0x4 
#define FV_TAB_RELATIVE_LEFT       0x5 
#define FV_TAB_RELATIVE_CENTER     0x6 
#define FV_TAB_RELATIVE_RIGHT      0x7 
#define FV_TAB_RELATIVE_DECIMAL    0x8 




   /* Body/Master/Reference/Hidden Page */
#define FP_PageNum                687   /* R/O Integer */ 
#define FP_PointPageNum           688   /* R/O Integer */ 
#define FP_PageBackground             689   /* R/W Enum */ 
#define FV_BGD_DEFAULT               0x00 
#define FV_BGD_NONE                  0x01 
#define FV_BGD_OTHER                 0x02 
#define FP_MasterPage             690   /* R/W String */ 
#define FP_PageWidth              691   /* R/O Metric */ 
#define FP_PageHeight             692   /* R/O Metric */ 
#define FP_PagePrev               693   /* R/O ID(Page) */ 
#define FP_PageNext               694   /* R/O ID(Page) */ 
#define FP_PageFrame              695   /* R/O ID(UnanchoredFrame) */ 
#define FP_PageNumString          696   /* R/O String */ 
#define FP_PageIsRecto            697   /* R/O Boolean */ 

   /* Pgf */
#define FP_NextPgfInDoc           716   /* R/O ID(Pgf) */ 
#define FP_NextPgfInFlow          717   /* R/O ID(Pgf) */ 
#define FP_PrevPgfInFlow          718   /* R/O ID(Pgf) */ 
#define FP_PgfNumber              719   /* R/O String */ 
#define FP_PgfSpellChecked        720   /* R/W Boolean */ 
#define FP_PgfSplit               721   /* R/W Boolean */
#define FP_PgfMarkedForNamedDestination  722   /* R/W Boolean */

   /* Graphic Format */
#define FP_BorderWidth            740   /* R/W Metric */ 
#define FP_Fill                   741   /* R/W Enum */ 
#define FV_FILL_BLACK                     0 
#define FV_FILL_WHITE                     7 
#define FV_FILL_CLEAR                     15 
#define FP_Pen                    742   /* R/W Enum *//* See FP_Fill */ 
#define FP_HeadArrow                743   /* R/W Boolean */ 
#define FP_TailArrow                744   /* R/W Boolean */ 
#define FP_ArrowTipAngle          745   /* R/W Int */ 
#define FP_ArrowBaseAngle         746   /* R/W Int */ 
#define FP_ArrowScaleFactor       747   /* R/W Metric (4.4) */ 
#define FP_ArrowLength            748   /* R/W Metric (8.8) */ 
#define FP_ArrowType              749   /* R/W Enum */ 
#define FV_ARROW_STICK                    0x1 
#define FV_ARROW_HOLLOW                   0x2 
#define FV_ARROW_FILLED                   0x3 
#define FP_ArrowScaleHead         750   /* R/W Boolean */ 
#define FP_Color                  751   /* R/W ID(Color) */ 
#define FP_Dash                   752   /* R/W Metrics */ 
#define FP_LineCap                753   /* R/W Enum */ 
#define FV_CAP_BUTT                  0x00 
#define FV_CAP_ROUND                 0x01 
#define FV_CAP_SQUARE                0x02 
#define FP_RunaroundGap           754   /* R/W Metric */
#define FP_TintPercent            755   /* R/W Metric */
#define FP_Overprint              756   /* R/W Enum */
#define FV_KNOCKOUT                  0x00
#define FV_OVERPRINT                 0x01
#define FV_FROMCOLOR                 0x02

   /* Graphic */
#define FP_GraphicIsSelected               771   /* R/W Boolean */ 
#define FP_GraphicCantBeSelected           772   /* R/W Boolean */ 
#define FP_GraphicIsButton				   773   /* R/W Boolean Only applicable to FO_TextFrame*/ 
#define FP_FrameParent           		   774   /* R/W ID(Frame) */ 
#define FP_PrevGraphicInFrame         775   /* R/W ID(Graphic) */ 
#define FP_NextGraphicInFrame         776   /* R/W ID(Graphic) */ 
#define FP_GroupParent            777   /* R/W ID(Group) */ 
#define FP_PrevGraphicInGroup         778   /* R/O ID(Graphic) */ 
#define FP_NextGraphicInGroup         779   /* R/O ID(Graphic) */ 
#define FP_Angle                  780   /* R/W Metric */ 
#define FP_LocX                   781   /* R/W Metric */
#define FP_LocY                   782   /* R/W Metric */
#define FP_Width                  783   /* R/W Metric */ 
#define FP_Height                 784   /* R/W Metric */ 
#define FP_NextSelectedGraphicInDoc    785   /* R/O ID(Graphic) */ 
#define FP_NextGraphicInDoc       786   /* R/O ID(Graphic) */ 
#define FP_Runaround			  787	/* R/W Enum */
#define FV_TR_NONE			0x01
#define FV_TR_CONTOUR		0x02
#define FV_TR_BBOX			0x03
	/* experimental */
#define FP_DesktopX							788	/* R/O IntT */
#define FP_DesktopY							789	/* R/O IntT */
#define FP_DesktopWidth						790	/* R/O IntT */
#define FP_DesktopHeight					791	/* R/O IntT */
	/* end experimental */

   /* Group */
#define FP_FirstGraphicInGroup        807   /* R/O ID(Graphic) */ 
#define FP_LastGraphicInGroup         808   /* R/O ID(Graphic) */ 

   /* Arc */
#define FP_DTheta                 830   /* R/W Metric */ 
#define FP_Theta                  831   /* R/W Metric */ 

   /* Inset */

#define FP_InsetEditor                      850 /* R/W String */
#define FP_InsetUpdater                     851 /* R/W String */

#define FP_InsetFile                        852 /* R/W String */
#define FP_InsetFileOrigName				2814 /* R/W String */

#define FP_InsetDpi                         853 /* R/W Int */
#define FP_InsetIsFixedSize					854 /* R/W Bool */

#define FP_InsetIsFlippedSideways           855 /* R/W Bool */
#define FP_InsetIsInverted                  856 /* R/W Bool */
#define FP_InsetRasterDpi                   857 /* R/O MetricT */
#define FP_INSETinfo						858 /* R/W F_UBYTES --- added by richa*/

   /* Math */
#define FP_MathFullForm			  865   /* R/W String */
#define FP_MathSize				  866   /* R/W Ordinal */
#define FV_MATH_MEDIUM 0
#define FV_MATH_SMALL 1
#define FV_MATH_LARGE 2

   /* Frame */
#define FP_AnchorType             875   /* R/W Enum */ 
#define FV_ANCHOR_INLINE              1 
#define FV_ANCHOR_TOP                 2
#define FV_ANCHOR_BELOW               3
#define FV_ANCHOR_BOTTOM              4
#define FV_ANCHOR_SUBCOL_LEFT         5
#define FV_ANCHOR_SUBCOL_RIGHT        6
#define FV_ANCHOR_SUBCOL_NEAREST      7
#define FV_ANCHOR_SUBCOL_FARTHEST     8
#define FV_ANCHOR_SUBCOL_INSIDE       9
#define FV_ANCHOR_SUBCOL_OUTSIDE     10
#define FV_ANCHOR_TEXTFRAME_LEFT     11
#define FV_ANCHOR_TEXTFRAME_RIGHT    12
#define FV_ANCHOR_TEXTFRAME_NEAREST  13
#define FV_ANCHOR_TEXTFRAME_FARTHEST 14
#define FV_ANCHOR_TEXTFRAME_INSIDE   15
#define FV_ANCHOR_TEXTFRAME_OUTSIDE  16
#define FV_ANCHOR_RUN_INTO_PARAGRAPH   17

#ifdef FAPI_4_BEHAVIOR
#define FV_ANCHOR_LEFT FV_ANCHOR_SUBCOL_LEFT
#define FV_ANCHOR_RIGHT FV_ANCHOR_SUBCOL_RIGHT
#define FV_ANCHOR_NEAREST FV_ANCHOR_SUBCOL_NEAREST
#define FV_ANCHOR_FARTHEST FV_ANCHOR_SUBCOL_FARTHEST
#endif

#define FP_AFrameIsFloating               876   /* R/W Boolean */ 
#define FP_SideOffset             877   /* R/W Metric */ 
#define FP_AFrameIsCropped                878   /* R/W Boolean */ 
#define FP_TextLoc                879   /* R/O TextLoc */ 
#define FP_PageFramePage          880   /* R/O ID(Page) */ 
#define FP_BaselineOffset         881   /* R/W Metric */ 
#define FP_FirstGraphicInFrame    882   /* R/O ID(Graphic) */ 
#define FP_LastGraphicInFrame     883   /* R/O ID(Graphic) */ 
#define FP_PrevAFrame			  884   /* R/O ID(AnchoredFrame) */ 
#define FP_NextAFrame			  885   /* R/O ID(AnchoredFrame) */ 
#define FP_Alignment              886   /* R/W Enum */
#define FV_ALIGN_LEFT         0
#define FV_ALIGN_CENTER       1
#define FV_ALIGN_RIGHT        2
#define FV_ALIGN_INSIDE       3
#define FV_ALIGN_OUTSIDE      4

   /* Poly */
#define FP_PolyIsBezier               906   /* R/W Boolean */
#define FP_NumPoints	              907   /* R/O Ordinal */
#define FP_Points                 908   /* R/W Points */

   /* Rect */
#define FP_RectangleIsSmoothed             929   /* R/W Boolean */ 

   /* Round Rect */
#define FP_Radius                 950   /* R/W Metric */

   /* TextFrame */
#define FP_FirstPgf			      972   /* R/O ID(Pgf) */ 
#define FP_LastPgf			      973   /* R/O ID(Pgf) */ 
#define FP_FirstAFrame			  974   /* R/O ID(AnchoredFrame) */ 
#define FP_LastAFrame			  975   /* R/O ID(AnchoredFrame) */ 
#define FP_FirstFn			      976   /* R/O ID(Footnote) */ 
#define FP_LastFn			      977   /* R/O ID(Footnote) */ 
#define FP_FirstCell			  978   /* R/O ID(Cell) */ 
#define FP_LastCell			      979   /* R/O ID(Cell) */ 
#define FP_PrevTextFrameInFlow    980   /* R/W ID(TextFrame) */ 
#define FP_NextTextFrameInFlow    981   /* R/W ID(TextFrame) */ 
#define FP_Flow                   982   /* R/O ID(Flow) */ 
/* see below where FP_Overflowed is defined 983 is not avaliable.*/
#define FP_NumColumns			  984	/* R/W Short */
#define FP_ColGapWidth			  985	/* R/W Metric */
#define FP_FirstSubCol			  986	/* R/O ID(SubCol) */
#define FP_LastSubCol			  987	/* R/O ID(SubCol) */
#define FP_LineSpacingFactor	  988	/* R/W Metric */
#define FP_PgfSpacingFactor		  989	/* R/W Metric */
#define FP_CellMarginSpacingDelta 990	/* R/W Metric */
#define FP_SideHeadPlacement      1171  /* R/W Enum */ 
#define FV_SH_LEFT				0x00
#define FV_SH_RIGHT				0x01
#define FV_SH_INSIDE			0x02
#define FV_SH_OUTSIDE			0x03
#define FP_SideHeadWidth          1172  /* R/W Metric */ 
#define FP_SideHeadGap            1173  /* R/W Metric */ 
#define FP_ColumnsAreBalanced     1175	/* R/W Boolean */

   /* Footnote */
#define FP_InTextObj        	  1000   /* R/O ID(TextFrame, SubCol, Gline)*/ 
#define FP_InTextFrame        	  1001   /* R/O ID(TextFrame) */ 
#define FP_FnNum         		  1002   /* R/O Int */ 
#define FP_NextFnInDoc      	  1003   /* R/O ID(Footnote) */ 
#define FP_PrevFn					  1004   /* R/O ID(Footnote) */ 
#define FP_NextFn			        1005   /* R/O ID(Footnote) */ 
#define FP_FnAnchorString			1006   /* R/O String */ 

   /* Marker */
#define FP_OldTypeNum             1024   /* R/W Int */ 
#if defined(FAPI_5_BEHAVIOR) || defined(FAPI_4_BEHAVIOR)
#define FP_MarkerType             FP_OldTypeNum
#endif
#define FP_MarkerText             1025   /* R/W String */ 
#define FP_NextMarkerInDoc        1026   /* R/O ID(Marker) */ 
#define FP_MarkerTypeId			  1027	 /* R/W ID(MarkerType) */

   /* MarkerType */
#define FP_NextMarkerTypeInDoc		1028  /* R/W ID(MarkerType) */ 
#define FP_InvariantName			1029  /* R/W String(languageInvariantName) */
#define FP_Public					1030  /* R/W flag bit(MARKERTYPE_PUBLIC) */
#define FP_Transient				1031  /* R/W flag bit(MARKERTYPE_TRANSIENT) */
#define FP_Required					1032  /* R/O flag bit(MARKERTYPE_REQUIRED) */

  /* Variable */
#define FP_NextVarInDoc   1046   /* R/O ID(Variable) */ 
#define FP_VarFmt         1047   /* R/W ID(VariableFmt) */ 

  /* Variable Format */
#define FP_SystemVar         1049   /* R/O Int */ 
#define FV_VAR_USER_VARIABLE             0 
#define FV_VAR_CURRENT_PAGE_NUM          1 
#define FV_VAR_PAGE_COUNT                2 
#define FV_VAR_CURRENT_DATE_LONG         3 
#define FV_VAR_CURRENT_DATE_SHORT        4 
#define FV_VAR_MODIFICATION_DATE_LONG    5 
#define FV_VAR_MODIFICATION_DATE_SHORT   6 
#define FV_VAR_CREATION_DATE_LONG        7 
#define FV_VAR_CREATION_DATE_SHORT       8 
#define FV_VAR_FILE_NAME_LONG            9 
#define FV_VAR_FILE_NAME_SHORT           10 
#define FV_VAR_HEADER_FOOTER_1           11 
#define FV_VAR_HEADER_FOOTER_2           12 
#define FV_VAR_HEADER_FOOTER_3           13 
#define FV_VAR_HEADER_FOOTER_4           14 
#define FV_VAR_TABLE_CONTINUATION        15
#define FV_VAR_TABLE_SHEET               16 
#define FV_VAR_HEADER_FOOTER_5           19 
#define FV_VAR_HEADER_FOOTER_6           20 
#define FV_VAR_HEADER_FOOTER_7           21 
#define FV_VAR_HEADER_FOOTER_8           22 
#define FV_VAR_HEADER_FOOTER_9           23
#define FV_VAR_HEADER_FOOTER_10          24
#define FV_VAR_HEADER_FOOTER_11          25
#define FV_VAR_HEADER_FOOTER_12          26
#define FP_Fmt               1050  /* R/W String */
#define FP_NextVarFmtInDoc   1051   /* R/O ID(VariableFmt) */ 

  /* XRef */
#define FP_XRefFile               1070   /* R/W String */
#define FP_TextRange              1071   /* R/O TextRange */ 
#define FP_NextXRefInDoc		  1072   /* R/O ID(XRef) */ 
#define FP_XRefFmt             	  1073   /* R/W ID(XRefFmt) */ 
#define FP_XRefSrcText			  1075	 /* R/W String */
#define FP_XRefSrcIsElem		  1076	 /* R/W Bool */
#define FP_XRefIsUnresolved       1077   /* R/O Bool */
#define FP_XRefSrcElemNonUniqueId 1152   /* R/W String */
#define FP_XRefAltText            1153   /* R/W String */
#define FP_XRefClientName         1154   /* R/W String */
#define FP_XRefClientType         1155   /* R/W String */

  /* XRef Format */
#define FP_NextXRefFmtInDoc       1074   /* R/O ID(XRefFmt) */ 

 /* TextLine (FO_TextLine) */
#define FP_TextLineType           1095   
#define FV_TEXTLINE_LEFT                0 
#define FV_TEXTLINE_RIGHT               1 
#define FV_TEXTLINE_CENTER              2 
#define FV_TEXTLINE_MATH                3 

#define FP_BasePointX             1116   /* R/W Metric */ 
#define FP_BasePointY             1117   /* R/W Metric */ 

 /* Condition (FO_CondFmt) */
#define FP_NextCondFmtInDoc       1138   /* R/O ID(CondFmt) */ 
#define FP_CondFmtIsShown                  1139   /* R/W Boolean */ 
#define FP_SepOverride     1140   /* R/W ID(Color) */ 
#define FP_UseSepOverride  1141   /* R/W Boolean */ 
#define FP_StyleOverride          1142   /* R/W Enum */
#define FV_CN_NO_OVERRIDE       0   
#define FV_CN_OVERLINE          1   
#define FV_CN_STRIKETHROUGH     2   
#define FV_CN_SINGLE_UNDERLINE  3
#define FV_CN_DOUBLE_UNDERLINE  4
#define FV_CN_CHANGEBAR         5
#define FV_CN_NUMERIC_UNDERLINE 6
#define FV_CN_NMRIC_AND_CHNGBAR 7

/* Attribute Conditional Expression (FO_AttrCondExpr) */
#define FP_NextAttrCondExprInDoc 2816  /* R/O ID(AttrCondExpr)*/
#define FP_AttrCondExprStr	2817	   /* R/W String */	
#define FP_AttrCondExprIsActive 2818   /* R/W Boolean */	

 /* Condition List (in text) */
#define FP_InCond				  1150  /* R/W Conditions */
#define FP_StyleOverrides		  1151  /* R/O Enum */
#define FV_CS_NO_OVERRIDE       0x00
#define FV_CS_OVERLINE          0x01
#define FV_CS_STRIKETHROUGH     0x02
#define FV_CS_SINGLE_UNDERLINE  0x04
#define FV_CS_DOUBLE_UNDERLINE  0x08
#define FV_CS_CHANGEBAR         0x10
#define FV_CS_NUMERIC_UNDERLINE	0x20

   /* Flow */ 
#define FP_NextFlowInDoc			1162   /* R/O ID(Flow) */ 
#define FP_FlowIsSynchronized		1163   /* R/W Boolean */ 
#define FP_MinHang					1164   /* R/W Metric */ 
#define FP_FlowIsAutoConnect		1165   /* R/W Boolean */ 
#define FP_FlowIsFeathered			1166   /* R/W Boolean */ 
#define FP_Spacing					1167   /* R/W Metric */ 
#define FP_FlowIsPostScript			1168   /* R/W Boolean */ 
#define FP_FirstTextFrameInFlow		1169   /* R/O ID(TextFrame) */ 
#define FP_LastTextFrameInFlow		1170   /* R/O ID(TextFrame) */ 
#define FP_SideHeadRoomInFlow		1174	 /* R/W Boolean */
/* used to be Doc Flow Props: */
#define FP_MaxInterlinePadding	  470   /* R/W Metric */
#define FP_MaxInterPgfPadding	  471   /* R/W Metric */

   /* FO_Cell */
#define FP_CellUseOverrideLRuling 1190   /* R/W Boolean */ 
#define FP_CellUseOverrideRRuling 1191   /* R/W Boolean */ 
#define FP_CellUseOverrideTRuling 1192   /* R/W Boolean */ 
#define FP_CellUseOverrideBRuling 1193   /* R/W Boolean */ 
#define FP_CellOverrideShading    1194   /* R/W ID(FO_Color) */ 
#define FP_CellOverrideFill       1195   /* R/W Enum */ 
#define FP_CellUseOverrideFill    1196   /* R/W Boolean */ 
#define FP_CellUseOverrideShading 1197   /* R/W Boolean */ 
#define FP_CellRow                1198   /* R/O ID(FO_Row) */ 
#define FP_NextCellInTbl          1199   /* R/O ID(FO_Cell) */ 
#define FP_CellBelowInTbl         1200   /* R/O ID(FO_Cell) */ 
#define FP_PrevCellInRow          1201   /* R/O ID(FO_Cell) */ 
#define FP_NextCellInRow          1202   /* R/O ID(FO_Cell) */ 
#define FP_CellAboveInCol         1203   /* R/O ID(FO_Cell) */ 
#define FP_CellBelowInCol         1204   /* R/O ID(FO_Cell) */ 
#define FP_CellColNum         	  1205   /* R/O Integer */ 
#define FP_CellIsStraddled        1206   /* R/O Boolean */ 
#define FP_CellNumRowsStraddled   1207   /* R/O Ordinal */ 
#define FP_CellNumColsStraddled   1208   /* R/O Integer */ 
#define FP_CellAngle              1209   /* R/W Integer */ 
#define FP_CellOverrideTopRuling  1210   /* R/W ID(FO_RulingFmt) */ 
#define FP_CellOverrideBottomRuling 1211 /* R/W ID(FO_RulingFmt) */ 
#define FP_CellOverrideLeftRuling 1212   /* R/W ID(FO_RulingFmt) */
#define FP_CellOverrideRightRuling 1213  /* R/W ID(FO_RulingFmt) */ 
#define FP_CellDefaultTopRuling   1214   /* R/O ID(FO_RulingFmt) */ 
#define FP_CellDefaultBottomRuling 1215  /* R/O ID(FO_RulingFmt)*/ 
#define FP_CellDefaultLeftRuling  1216   /* R/O ID(FO_RulingFmt) */ 
#define FP_CellDefaultRightRuling 1217   /* R/O ID(FO_RulingFmt) */ 
#define FP_CellIsShown            1218   /* R/O Boolean */ 
#define FP_PrevCell			      1219   /* R/O ID(Cell) */ 
#define FP_NextCell			      1220   /* R/O ID(Cell) */ 
/* FP_FirstPgf, FP_LastPgf (R/O ID(FO_Pgf) allow you to 
 * get the paragraphs within the cell. 
 */

   /* FO_Row */
#define FP_PrevRowInTbl                1244   /* R/O ID(Row) */ 
#define FP_NextRowInTbl                1245   /* R/O ID(Row) */ 
#define FP_RowTbl               1246   /* R/O ID(Table) */ 
#define FP_FirstCellInRow              1247   /* R/O ID(Cell) */ 
#define FP_RowKeepWithNext        1248   /* R/W Boolean */ 
#define FP_RowKeepWithPrev        1249   /* R/W Boolean */ 
#define FP_RowMaxHeight           1250   /* R/W METRIC */ 
#define FP_RowMinHeight           1251   /* R/W METRIC */ 
#define FP_RowStart               1252   /* R/W Enum */ 
#define FV_ROW_ANYWHERE        0 
#define FV_ROW_TOP_OF_COL      1 
#define FV_ROW_TOP_OF_PAGE     2 
#define FV_ROW_TOP_OF_LEFT_PAGE   3 
#define FV_ROW_TOP_OF_RIGHT_PAGE   4 
#define FP_RowType                1253   /* R/O Enum */ 
#define FV_ROW_HEADING          0 
#define FV_ROW_BODY            1 
#define FV_ROW_FOOTING          2 
#define FP_RowIsShown             1254   /* R/O Boolean */ 

 /* FO_TblFmt
  * FO_Tbl objects have some of thes properties as well.
  * Those properties shared by byoth instances and catalogs
  * are indicated by "Both", those unique to FO_TblFmt are
  * indicated by "FO_TblFmt".  Changing the property associated
  * with the instance constitutes a local override.
  */
/* FO_Tbl, FO_TblFmt */
#define FP_TblTag				1275    /* R/W String */
#define FP_NextTblFmtInDoc      1276   /* R/O ID(FO_TblFmt) *//*FO_TblFmt*/ 
#define FP_TblLeftIndent        1278   /* R/W Metric *//*Both*/ 
#define FP_TblRightIndent       1279   /* R/W Metric *//*Both*/ 
#define FP_TblSpaceAbove        1280   /* R/W Metric *//*Both*/ 
#define FP_TblSpaceBelow        1281   /* R/W Metric *//*Both*/ 
#define FP_TblAlignment         1282   /* R/W Enum *//*Both*/ 
#define FV_ALIGN_TBL_LEFT       0 
#define FV_ALIGN_TBL_CENTER     1 
#define FV_ALIGN_TBL_RIGHT      2 
#define FP_TblPlacement         1283   /* R/W Enum *//*Both*/ 
#define FV_TBL_ANYWHERE			0
#define FV_TBL_TOP_OF_COL 		1
#define FV_TBL_TOP_OF_PAGE 		2
#define FV_TBL_TOP_OF_LEFT_PAGE 3
#define FV_TBL_TOP_OF_RIGHT_PAGE 4
#define FV_TBL_FLOAT			 5
#define FP_TblInitNumCols        1284   /* R/W Ordinal *//*FO_TblFmt*/ 
#define FP_TblInitNumHRows       1285   /* R/W Ordinal *//*FO_TblFmt*/ 
#define FP_TblInitNumBodyRows    1286   /* R/W Ordinal *//*FO_TblFmt*/ 
#define FP_TblInitNumFRows       1287   /* R/W Ordinal *//*FO_TblFmt*/ 
#define FP_TblNumbering          1288   /* R/W Enum *//*Both*/ 
#define FV_TBL_NUM_BY_ROW	  0
#define FV_TBL_NUM_BY_COL 	  1
#define FP_TblTitlePosition       1289   /* R/W Enum *//*Both*/ 
#define FV_TBL_NO_TITLE            0 
#define FV_TBL_TITLE_ABOVE         1 
#define FV_TBL_TITLE_BELOW         2 
#define FP_TblTitleGap            1290   /* R/W Metric *//*Both*/ 
#define FP_OrphanRows             1291   /* R/W Ordinal *//*Both*/ 
#define FP_TblCatalogEntry        1292   /* R/W Boolean */ /* Both, however
										                    * the instance 
															* is RO
															*/
#define FP_TblColRulingPeriod     1293   /* R/W Ordinal *//*Both*/ 
#define FP_TblBodyRowRulingPeriod 1294   /* R/W Ordinal *//*Both*/ 
#define FP_TblLastBodyRuling      1295   /* R/W Boolean *//*Both*/ 
#define FP_TblHFFill              1296   /* R/W Enum *//*Both*/ 
#define FP_TblHFColor             1297   /* R/W ID(FO_Color) *//*Both*/ 
#define FP_TblBodyFirstFill       1298   /* R/W Enum *//*Both*/ 
#define FP_TblBodyFirstColor      1299   /* R/W ID(FO_Color) *//*Both*/ 
#define FP_TblBodyShadeBy         1300   /* R/W Boolean *//*Both*/ 
#define FP_TblBodyFirstPeriod     1301   /* R/W Ordinal */ /*Both*/ 
#define FP_TblBodyNextFill        1302   /* R/W Enum */ /*Both*/ 
#define FP_TblBodyNextPeriod      1303   /* R/W Ordinal *//*Both*/ 
#define FP_TblBodyNextColor       1304   /* R/W ID(FO_Color) *//*Both*/ 
#define FP_TblTopRuling           1305   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblBottomRuling        1306   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblLeftRuling          1307   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblRightRuling         1308   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblColRuling           1309   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblBodyRowRuling       1310   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblHFSeparatorRuling   1311   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblHFRowRuling         1312   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblOtherBodyRowRuling  1313   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblOtherColRuling     1314   /* R/W ID(FO_RulingFmt) *//*Both*/ 
#define FP_TblCellTopMargin		  1315   /* R/W Metric     *//*Both*/ 
#define FP_TblCellBottomMargin	  1316   /* R/W Metric     *//*Both*/ 
#define FP_TblCellLeftMargin	  1317   /* R/W Metric     *//*Both*/ 
#define FP_TblCellRightMargin	  1318   /* R/W Metric     *//*Both*/ 
/* FP_Locked does not serve the purpose if we want to know whether table */
/* is part of locked text inset.										 */
#define FP_TblInLockedTi		  1319   /* R/O Integer */
/* FP_Locked #define  R/W Bool.  Is this table format locked against 
 * formatting changes? True when in a text inset whose
 * formatting comes from the source document.
*/
   /* FO_Tbl */
#define FP_TblNumCols           1335   /* R/O Ordinal */ 
#define FP_TblNumRows           1336   /* R/O Ordinal */ 
#define FP_NextTblInDoc         1337   /* R/O ID(FO_Tbl) */ 
#define FP_FirstRowInTbl        1338   /* R/O ID(FO_Row) */ 
#define FP_LastRowInTbl         1339   /* R/O ID(FO_Row) */ 
#define FP_TblWidth             1340   /* R/O Metric */ 
#define FP_TopRowSelection      1341   /* R/O ID(FO_Row) */ 
#define FP_BottomRowSelection   1342   /* R/O ID(FO_Row) */ 
#define FP_LeftColNum           1343   /* R/O Ordinal */ 
#define FP_RightColNum          1344   /* R/O Ordinal */ 
#define FP_TblColWidths			1345   /* R/W Metrics */ 
#define FP_TblTitleSelected	    1346   /* R/O Bool */
/* FP_FirstPgf, FP_LastPgf allow you to 
 * get the paragraphs in the table title.
 */

   /* FO_RulingFmt */
#define FP_NextRulingFmtInDoc     1430   /* R/O ID(FO_RulingFmt) */ 
#define FP_RulingPenWidth         1432   /* R/W Metric */ 
#define FP_RulingGap              1433   /* R/W Metric */ 
#define FP_RulingSep       		  1434   /* R/W ID */ 
#define FP_RulingLines            1435   /* R/W Ordinal*/ 

   /* FO_Color */
#define FP_NextColorInDoc         1455   /* R/O ID(FO_Color) */ 
#define FP_Pantone                1456   /* R/W String */ 
#define FP_Cyan                   1457   /* R/W Metric */ 
#define FP_Magenta                1458   /* R/W Metric */ 
#define FP_Yellow                 1459   /* R/W Metric */ 
#define FP_Black                  1460   /* R/W Metric */ 
#define FP_ColorViewCtl           1461   /* R/W Int */ 
#define FV_SEP_NORMAL             0x0 
#define FV_SEP_NONE               0x1 
#define FV_SEP_WHITE              0x2 
#define FP_ColorPrintCtl          1462   /* R/W Int */ 
#define FV_PRINT_SPOT                0x0 
#define FV_PRINT_PROCESS             0x1 
#define FV_PRINT_NO                  0x2 
#define FP_ReservedColor          1463   /* R/O Enum */ 
#define FV_COLOR_NOT_RESERVED      0
#define FV_COLOR_CYAN      1
#define FV_COLOR_MAGENTA   2
#define FV_COLOR_YELLOW    3
#define FV_COLOR_BLACK     4
#define FV_COLOR_WHITE     5
#define FV_COLOR_RED       6
#define FV_COLOR_GREEN     7
#define FV_COLOR_BLUE      8
#define FV_COLOR_DARKGREY	9
#define FV_COLOR_PALEGREEN	10
#define FV_COLOR_FORESTGREEN	11
#define FV_COLOR_ROYALBLUE	12
#define FV_COLOR_MAUVE		13
#define FV_COLOR_LIGHTSALMON	14
#define FV_COLOR_DARKYELLOW		15
#define FV_COLOR_SALMON		16

#define FP_FamilyName             1464   /* R/W String */
#define FP_InkName                1465   /* R/W String */
#define FP_ColorTintPercent       1466   /* R/W Metric */
#define FV_COLOR_NOT_TINTED           ((MetricT)0x7fffffff)
#define FP_TintBaseColor          1467   /* R/W ID(FO_Color) */
#define FV_NO_BASE_COLOR              0x00
#define FP_ColorOverprint         1468   /* R/W Enum */
#define FV_COLOR_KNOCKOUT             0x00
#define FV_COLOR_OVERPRINT            0x01

/* Rubi */
#define FP_NextRubiInDoc    1469   /* R/O ID FO_Rubi */ 
#define FP_OyamojiTextRange 1470   /* R/O TEXTRANGE */ 
#define FP_RubiTextRange    1471   /* R/O TEXTRANGE */ 

/* Hypertext Parsing and Validation */
#define FP_HypertextDoValidate			2300	/* R/W Boolean */
#define FP_HypertextCommandText			2301	/* R/W String */
#define FP_HypertextParsedArgs			2302	/* R/O StringList */
#define FP_HypertextParseErr			2303	/* R/O Int */
			/* Possible error values. HypertextParseResultsT */
#define		FV_HypertextSyntaxOK				0
#define		FV_HypertextEmptyCommand			1
#define		FV_HypertextUnrecognizedCommand		2
#define		FV_HypertextMissingArguments		3
#define		FV_HypertextExtraArguments			4
#define		FV_HypertextBadSyntaxPathSpec		10
#define		FV_HypertextUnanchoredPartialPath	11
#define		FV_HypertextHelpDirNotFound			20
#define		FV_HypertextExpectedANumberParam	30
#define FP_HypertextValidateErr			2304	/* R/O Int */
			/* Possible error values. HypertextValidateResultsT. Don't overlap with above. */
#define		FV_HypertextValid					0
#define		FV_HypertextUsesDefaultText			200
#define		FV_HypertextFileNotRegular			210
#define		FV_HypertextFileNotMakerDoc			211
#define		FV_HypertextCantOpenDestFile		212
#define		FV_HypertextDestinationLinkNotFound	220
#define		FV_HypertextDuplicateLinkName		221
#define		FV_HypertextPageNameNotFound		230
#define		FV_HypertextUnrecognizedObjectType	240
#define		FV_HypertextObjectIDNotFound		241
#define		FV_HypertextBadMatrixSize			250
#define		FV_HypertextMatrixCommandInvalid	251
#define		FV_HypertextFlowMissingLines		252
#define		FV_HypertextNoNamedFlow				260
#define		FV_HypertextRecursiveFlow			261
#define		FV_HypertextMissingPopupMarker		270
#define		FV_HypertextMissingPopupLabelItem	271
#define		FV_HypertextEmptyLineInMiddleOfPopup 272
#define		FV_HypertextCommandIllegalWithinPopup 273
#define		FV_HypertextFcodeInvalid			280
#define FP_HypertextParseBadParam		2305	/* R/O Int */
#define FP_HypertextParseErrMsg			2306	/* R/O String */
#define FP_HypertextParsedCmdCode		2307	/* R/O Int */
			/* These are the HypertextCmdTypeT possible values */
#define		FV_CmdNotTyped						0
#define		FV_CmdError							1
#define		FV_CmdUnknown						2
#define		FV_CmdNoop							3		/* buttons can cause this */
			/* Published commands */
#define		FV_CmdAlert							8
#define		FV_CmdAlertTitle					9
#define		FV_CmdExit							10
#define		FV_CmdGoToLink						11
#define		FV_CmdGoToLinkFitWin				12
#define		FV_CmdGoToNew						13
#define		FV_CmdGoToPage						14
#define		FV_CmdGoToObjectId					15
#define		FV_CmdGoToObjectIdFitWin			16
#define		FV_CmdMatrix						17
#define		FV_CmdMessage						18
#define		FV_CmdNewLink						19
#define		FV_CmdNextPage						20
#define		FV_CmdPreviousPage					21
#define		FV_CmdOpenLink						22
#define		FV_CmdOpenLinkFitWin				23
#define		FV_CmdOpenNew						24
#define		FV_CmdOpenObjectId					25
#define		FV_CmdOpenObjectIdFitWin			26
#define		FV_CmdOpenPage						27
#define		FV_CmdPopup							28
#define		FV_CmdPreviousLink					29
#define		FV_CmdPreviousLinkFitWin			30
#define		FV_CmdQuit							31
#define		FV_CmdQuitAll						32
			/* Internal FrameMaker use */
#define		FV_CmdFCodes						256
#define		FV_CmdOutCodes						257
#define		FV_CmdInCodes						258
#define		FV_CmdApplyMathRules				259
#define		FV_CmdThesaurusLookup				260
#define		FV_CmdNative						261
#define		FV_CmdHelpLink						262
#define		FV_CmdBeginRange					263		/* for ApplyMathRules */
#define		FV_CmdEndRange						264		/* for ApplyMathRules */
#define FP_HypertextParsedCmdDest			2308	/* R/O Int */
			/* HypertextDestinationT */
#define		FV_DestNowhere						0
#define		FV_DestMarkerNewLink				1
#define		FV_DestFirstPage					2
#define		FV_DestLastPage						3
#define		FV_DestPageNum						4
#define		FV_DestFluidFlow					5
#define		FV_DestMarker						6
#define		FV_DestObjectId						7
#define		FV_DestXRef							8		/* to element text */
#define FP_HypertextParsedCmdDestObjType	2309	/* R/O Int */
			/* These arethe HypertextDestinationObjTypeT */
			/* These should match SomeUniqTypeT until we kill that typedef */
#define		FV_ObjectUnknown					0
#define		FV_ObjectMarker						1
#define		FV_ObjectPgf						2 
#define		FV_ObjectXref						3 
#define		FV_ObjectGraphic					4
#define	    FV_ObjectElement					5 
#define		FV_ObjectTextInset					6 
#define		FV_ObjectDataLink					7
#define FP_HypertextParsedCmdDestObjID		2310	/* R/O Int */
#define FP_HypertextParsedCmdMatrixRows		2311	/* R/O Int */
#define FP_HypertextParsedCmdMatrixColumns	2312	/* R/O Int */
#define FP_HypertextParsedLinkName		2313	/* R/O String */
#define FP_HypertextParsedPageName		2314	/* R/O String */
#define FP_HypertextParsedFlowName		2315	/* R/O String */
#define FP_HypertextParsedRangeName		2316	/* R/O String */
#define FP_HypertextParsedClientName	2317	/* R/O String */
#define FP_HypertextParsedTitle			2318	/* R/O String */
#define FP_HypertextParsedMessage		2319	/* R/O String */
#define FP_HypertextParsedDIFileName	2320	/* R/O String */



/*------------------- Additional Structure Properties ----------------------*/
/*--------------------------------------------------------------------------*/

				/* Document */

#define FP_FirstElementDefInDoc   1483   /* R/O FO_ElementDef */ 
#define FP_ElementBoundaryDisplay 1484   /* R/W Integer */ 

/* Boolean Conditional Expression is part of document */
#define FP_BooleanConditionExpression   2321	/* R/W String */
#define FP_BooleanConditionState  2322   /* R/W Integer */

/* Resource Manager update is needed in case of Ditamap/Bookmap. */
#define	FP_IsDitamapInResourceManager	2332

/* values for FP_ElementBoundaryDisplay */
#define FV_ELEM_DISP_NONE		0
#define FV_ELEM_DISP_BRACKETS	1
#define FV_ELEM_DISP_TAGS		2

#define FP_ElementCatalogDisplay  1485   /* R/W Integer */ 

/* values for FP_ElementCatalogDisplay */
#define FV_ELCAT_STRICT		0x00
#define FV_ELCAT_LOOSE		0x01
#define FV_ELCAT_CHILDREN	0x02
#define FV_ELCAT_ALL		0x03
#define FV_ELCAT_CUSTOM		0x04

#define FP_DefaultInclusions      1486   /* R/W Strings */ 
#define FP_DefaultExclusions      1487   /* R/W Strings */ 
#define FP_ElementCatalog         1488   /* R/O F_ElementCatalogEntriesT */ 

#define FP_FirstFmtChangeListInDoc  1489	 /* R/O FO_FmtChangeList */
#define FP_NewElemAttrDisplay		1490	 /* R/W Enum */

/* values for FP_NewElemAttrDisplay */
#define FV_ATTR_DISP_NONE			1
#define FV_ATTR_DISP_REQSPEC		2
#define FV_ATTR_DISP_ALL			3

#define FP_NewElemAttrEditing		1491	 /* R/W Enum */

/* values for FP_AttrEditing */
#define FV_ATTR_EDIT_NONE			0
#define FV_ATTR_EDIT_REQUIRED		1
#define FV_ATTR_EDIT_ALWAYS			2

#define FP_UseInitialStructure		1492	/* R/W Boolean */
#define FP_SeparateInclusions		1493	/* R/W Boolean */
#define FP_ElementSelection			1494	/* R/W F_ElementRangeT */
#define FP_StructuredApplication	1495	/* R/W StringT */

#define FP_CustomElementList		1496	/* R/W Strings */
#define FP_StructuredApplicationList	1497
#define FP_StructuredApplicationForOpen	1498

#ifdef FAPI_6_BEHAVIOR
#define FP_SgmlApplication			FP_StructuredApplication
#endif /* FAPI_6_BEHAVIOR */

/* values for F_ElementCatalogEntry->flags */

#define FV_STRICTLY_VALID	0x01
#define FV_LOOSELY_VALID	0x02
#define FV_ALTERNATIVE		0x04
#define FV_INCLUSION		0x08

/* FO_Flow, FO_Books, FO_Cells and FO_Tbl (for tbl title) */
#define FP_HighestLevelElement    1509   /* R/O FO_Element */ 

				/* Book Component */

#define FP_ComponentElement       1551   /* R/O FO_Element */ 
#define FP_ExtractElementTags     1552   /* R/W Strings */

				/* ElementDef */

#define FP_NextElementDefInDoc         1572   /* R/O FO_ElementDef */ 
#define FP_ElementInCatalog       1573   /* R/W Boolean */ 
#define FP_ValidHighestLevel      1574   /* R/W Boolean */ 
#define FP_GeneralRule            1575   /* R/W String */ 
#define FP_Exclusions             1576   /* R/W Strings */ 
#define FP_Inclusions             1577   /* R/W Strings */ 
#define FP_GeneralRuleErrorOffsets 1578  /* R/O Ints */
#define FP_ElementDefType		  1579   /* R/W Integer */ 
#define FP_Comment			      1581   /* R/W String */
#define FP_TextFmtRules	  	  	  1582	 /* R/O FO_FmtRule[] */
#define FP_ObjectFmtRules         1583	 /* R/O FO_FmtRule[] */
#define FP_AttributeDefs		  1584	 /* R/W AttributeDefs */
#define FP_InitStructurePattern   1585   /* R/W String */
#define FP_TableTagging		FP_InitStructurePattern   /* 5.1 compatibility */
#define FP_ElementPgfFormat		  1586   /* R/W String */
#define FP_PrefixRules	  		  1587	 /* R/O FO_FmtRule[] */
#define FP_SuffixRules	  		  1588	 /* R/O FO_FmtRule[] */
#define FP_FirstPgfRules	  	  1589	 /* R/O FO_FmtRule[] */
#define FP_LastPgfRules  	  	  1590	 /* R/O FO_FmtRule[] */
#define FP_AlsoInsert			  1591   /* R/W Strings */
#define FP_ParsedGeneralRule	  1592	 /* R/O Strings */
#define FP_ElementDataType		  1593   /* R/W Integer */
#define FP_AllowedDataRange		  1594   /* R/W Strings */

/* values for FP_ElementDefType - formatting objects   */
#define FV_FO_UNSPECIFIED            0 
#define FV_FO_CONTAINER              1 
#define FV_FO_SYS_VAR              	 2 
#define FV_FO_XREF                   3 
#define FV_FO_MARKER                 4 
#define FV_FO_FOOTNOTE               5 
#define FV_FO_GRAPHIC				 6
#define FV_FO_EQN               	 7 
#define FV_FO_TBL                  	 8 
#define FV_FO_TBL_TITLE				 9
#define FV_FO_TBL_HEADING			10
#define FV_FO_TBL_BODY 				11
#define FV_FO_TBL_FOOTING			12	
#define FV_FO_TBL_ROW				13
#define FV_FO_TBL_CELL 				14
#define FV_FO_RUBI_GROUP 		    15
#define FV_FO_RUBI					16
#define FV_FO_NUMTYPES				17

/* values for FP_ElementDataType - element data type */
#define FV_DT_UNSPECIFIED			 0 
#define FV_DT_INTEGER				 1 
#define FV_DT_FLOAT					 2 
#define FV_DT_NUMTYPES				 3 

/* values for attrType field in F_AttributeDefT structure */
#define FV_AT_STRING         0
#define FV_AT_STRINGS        1
#define FV_AT_CHOICES        2
#define FV_AT_INTEGER        3
#define FV_AT_INTEGERS       4
#define FV_AT_REAL           5
#define FV_AT_REALS          6
#define FV_AT_UNIQUE_ID      7
#define FV_AT_UNIQUE_IDREF   8
#define FV_AT_UNIQUE_IDREFS	 9
#define FV_AT_NUMTYPES      10

/* values for flags bitfield in the F_AttributeDefT structure */
#define FV_AF_READ_ONLY      0x0001
#define FV_AF_HIDDEN         0x0002
#define FV_AF_FIXED			 0x0004


				/* FmtRule */
#define FP_FmtRuleType			2100	/* R/W Enum */
#define FP_CountElements		2101	/* R/W Strings */
#define FP_StopCountingAt		2102	/* R/W String */
#define FP_FmtRuleClauses		2103	/* R/O FO_FmtRuleClauses[] */
#define FP_FmtRuleClause        2104    /* R/O FO_FmtRuleClause */

/* format rule types */
#define FV_CONTEXT_RULE				 0
#define FV_LEVEL_RULE				 1


				/* FmtRuleClause */
#define FP_Specification		2150	/* R/W String */
#define FP_IsTextRange			2151	/* R/W Bool */
#define FP_RuleClauseType		2152	/* R/O Enum */
#define FP_FormatTag			2153	/* R/W String */
#define FP_FmtChangeListTag		2154	/* R/W String */
#define FP_FmtChangeList		2155	/* R/O FO_FmtChangeList */
#define FP_SubFmtRule			2156	/* R/O FO_FmtRule */
#define FP_ContextLabel			2157	/* R/W context label */
#define FP_ElemPrefixSuffix		2158    /* R/W String */
#define FP_FmtRule              2159    /* R/O FO_FmtRule */
#define FP_ParsedSpecification	2160 	/* R/O Strings */
/* FM Internal usage only. Used by CSS generator. Should not be */
/* published.													*/
#define FP_SpecificationForCSS  2161	/* R/O StringList */
/* FM Internal usage only. Used by CSS generator. Should not be */
/* published.													*/
#define FP_ContextForCSS		2162	/* R/O IntList */
#define FP_ParseFullSpecification	2163 	/* R/O Strings */


/* format rule clause types */
#define FV_RC_TAG						 0
#define FV_RC_SUB_FMTRULE				 1
#define FV_RC_CHANGELIST				 2
#define FV_RC_CHANGELIST_TAG			 3

/* format rule clause context tokens */
#define FV_EDT_TEXT        40         /* <TEXT>  */
#define FV_EDT_TEXTONLY    41         /* <TEXTONLY>  */
#define FV_EDT_ANY         42         /* <ANY>  */
#define FV_EDT_ALL         43         /* anywhere */
#define FV_EDT_FIRST       44         /* first  */
#define FV_EDT_MIDDLE      45         /* middle  */
#define FV_EDT_LAST        46         /* last  */
#define FV_EDT_NOTFIRST    47         /* notfirst  */
#define FV_EDT_NOTLAST     48         /* notlast  */
#define FV_EDT_AFTER       49         /* after  */
#define FV_EDT_BEFORE      50		  /* before */ 
#define FV_EDT_BETWEEN     53         /* between */
#define FV_EDT_INSERTPAR   23         /* insert parent separator */
#define FV_EDT_ONLY        62		  /* only */
#define FV_EDT_ATTRGRPO    30
#define FV_EDT_ELEMENT	   25		

				/* Element */

#define FP_ElementDef             1621   /* R/W FO_ElementDef */ 
#define FP_ParentElement          1622   /* R/O FO_Element */ 
#define FP_FirstChildElement      1623   /* R/O FO_Element */ 
#define FP_LastChildElement       1624   /* R/O FO_Element */ 
#define FP_PrevSiblingElement     1625   /* R/O FO_Element */ 
#define FP_NextSiblingElement     1626   /* R/O FO_Element */ 
#define FP_ElementIsCollapsed     1627   /* R/W Boolean */ 
#define FP_BookComponent          1628   /* R/O FO_BookComponent */ 
#define FP_Object				  1630	 /* R/O object ID */
#define FP_MatchingTextClauses	  1631	 /* R/O FO_RuleClauses */
#define FP_MatchingObjectClauses  1632	 /* R/O FO_RuleClauses */
#define FP_Attributes			  1634   /* R/W F_Attributes */
#define FP_AttrDisplay			  1635	 /* R/W Enum */
/*define FP_ContextLabel		  2157	    R/O context label */
#define FP_ElementType			  1636	 /* R/O Integer */
#define FP_IDAttrValue			  1637	 /* R/W String */
#define FP_MatchingFirstPgfClauses 1638  /* R/O FO_RuleClauses */
#define FP_MatchingLastPgfClauses 1639   /* R/O FO_RuleClauses */
#define FP_MatchingPrefixClauses  1640   /* R/O FO_RuleClauses */
#define FP_MatchingSuffixClauses  1641   /* R/O FO_RuleClauses */
#define FP_MatchesContextInUserString	  1642	 /* R/O Boolean */
#define FP_Namespace  			  1643   /* R/W FO_Element */
#define FP_NamespaceScope		  1644   /* R/O FO_Element */
#define FP_NumNamespaces  		  1645   /* R/O Integer */

/*DITAMap related properties of element*/
#define FP_NextElementDFS	2334	/* R/O FO_Element */
#define FP_PrevElementDFS	2335	/* R/O FO_Element */
#define FP_ElementIsTopicRef	2336	/* R/O Bool */
#define FP_ElementIsTopicHead	2337	/* R/O Bool */
#define FP_ElementIsTopicGroup	2338	/* R/O Bool */

				/* Elements bound to formatter objects */
#define FP_Element				2170	/* R/O FO_Element */
#define FP_TblElement			2171	/* R/O FO_Element */
#define FP_TblTitleElement		2172	/* R/O FO_Element */
#define FP_TblHeaderElement		2173	/* R/O FO_Element */
#define FP_TblBodyElement		2174	/* R/O FO_Element */
#define FP_TblFooterElement		2175	/* R/O FO_Element */
#define FP_RubiElement		    2176	/* R/O FO_Element */


				/* Validation properties for Element */

#define FP_AllowAsSpecialCase		1649   /* R/W Boolean */ 

#define FP_ElementIsUndefined       1650   /* R/O Boolean */ 
#define FP_ContentIsStrictlyValid   1651   /* R/O Boolean */ 
#define FP_ContentIsLooselyValid    1652   /* R/O Boolean */ 
#define FP_ContentNeededAtBegin     1653   /* R/O Boolean */ 
#define FP_HoleAtEnd				1654   /* R/O Boolen */
#define FP_ContentNeededAtEnd		FP_HoleAtEnd   /* BACKWARD COMPATIBILITY */
#define FP_ElementIsExcludedInContext      1655   /* R/O Boolean */ 
#define FP_ElementIsInvalidInParent        1656   /* R/O Boolean */ 
#define FP_ElementIsInvalidInPosition      1657   /* R/O Boolean */ 
#define FP_ElementTypeMismatch    1658   /* R/O Boolean */ 
#define FP_HoleBeforeElement      1659   /* R/O Boolean */ 
#define FP_TextIsInvalidInElement 1660   /* R/O Boolean */ 
#define FP_InvalidHighestLevel    1661   /* R/O Boolean */ 
#define FP_BookComponentMissing   1662   /* R/O Boolean */ 
#define FP_ErrorInBookComponent   1663   /* R/O Boolean */ 
#define FP_NextInvalidElement     1664   /* R/O FO_Element */ 
#define FP_ContentMustBeEmpty	  1665	 /* R/O Boolean */
#define FP_AttributeValueRequired 1666	 /* R/O Boolean */
#define FP_AttributeValueInvalid  1667	 /* R/O Boolean */

/* new Iris property to get all properties in one shot */
#define FP_ValidationFlags		  1668	 /* R/O Integer */
#define FP_ElementMarkedForNamedDestination	1669	 /* R/W Boolean */							  

#define FV_ELEM_UNDEFINED				0x0001
#define FV_ELEM_TYPE_MISMATCH			0x0002
#define FV_ELEM_EXCLUDED				0x0004
#define FV_ELEM_INVALID_IN_PARENT		0x0008
#define FV_ELEM_INVALID_AT_POSITION		0x0010
#define FV_ELEM_HAS_TEXT_INVALID		0x0020
#define FV_ELEM_CONTENT_MUST_BE_EMPTY	0x0040
#define FV_ELEM_MISSING_CONTENT_BEFORE	0x0080
#define FV_ELEM_MISSING_CONTENT_AT_BEG	0x0100
#define FV_ELEM_MISSING_CONTENT_AT_END	0x0200
#define FV_ELEM_NOT_VALID_AS_ROOT		0x0400
#define FV_ELEM_BOOK_COMP_MISSING		0x0800
#define FV_ELEM_BOOK_COMP_INVALID		0x1000
#define FV_ELEM_ATTRVAL_REQUIRED		0x2000
#define FV_ELEM_ATTRVAL_INVALID			0x4000
#define FV_ELEM_CONTENT_STRICTLY_VALID	0x10000
#define FV_ELEM_CONTENT_LOOSELY_VALID	0x20000
#define FV_ELEM_INVISIBLE_ATTRVAL_REQUIRED	0x40000
#define FV_ELEM_INVISIBLE_ATTRVAL_INVALID	0x80000

/*
 * valflags for F_AttributeT
 */
#define FV_AV_REQUIRED				1
#define FV_AV_INVALID_CHOICE		2
#define FV_AV_INVALID_FORMAT		3
#define FV_AV_IDREF_UNRESOLVED		4
#define FV_AV_ID_DUPLICATE_IN_DOC	5
#define FV_AV_ID_DUPLICATE_IN_BOOK	6	/* Not supported */
#define FV_AV_TOO_MANY_TOKENS		7
#define FV_AV_UNDEFINED				8
#define FV_AV_OUT_OF_RANGE			9


/*
 * F_ApiDeleteUndefinedAttribute(..)
 * Values for the "scope" parameter
 */
#define FV_Element			0
#define FV_ElementsOfType	1
#define FV_AllElements		2

/*
 * FmtChangeList  - fcl properties not used for other object types 
 */ 
#define FP_NextFmtChangeListInDoc  2000	  /* R/O FO_FmtChangeList */
#define FP_FmtChangeListInCatalog  2021   /* R/W Bool */
#define FP_PgfCatalogReference	   2001   /* R/W String */
#define FP_SpaceAboveChange        2002   /* R/W Metric */ 
#define FP_SpaceBelowChange        2003   /* R/W Metric */ 
#define FP_LeftIndentChange        2004   /* R/W Metric */ 
#define FP_FirstIndentChange       2005   /* R/W Metric */ 
#define FP_RightIndentChange       2006   /* R/W Metric */ 
#define FP_LeadingChange           2007   /* R/W Metric */
#define FP_LineSpacingFixed		   2008	  /* R/W Bool */
#define FP_TopSepAtIndent          2009   /* R/W Bool */ 
#define FP_BottomSepAtIndent       2010   /* R/W Bool */ 
#define FP_FontSizeChange          2012   /* R/W Metric */ 
#define FP_SpreadChange            2013   /* R/W MetricPercent */ 
#define FP_MoveTabs				   2014   /* R/W Metric */
#define FP_CellTopMarginFixed	   2015	  /* R/W Bool */
#define FP_CellBottomMarginFixed   2016	  /* R/W Bool */
#define FP_CellLeftMarginFixed	   2017	  /* R/W Bool */
#define FP_CellRightMarginFixed	   2018	  /* R/W Bool */
#define FP_FirstIndentIsRelative   2019   /* R/W Bool*/ 
#define FP_FirstIndentRelPos       2020   /* R/W Metric */ 
#define FP_CellTopMarginChange	   2022   /* R/W Metric */ 
#define FP_CellBottomMarginChange  2023   /* R/W Metric */ 
#define FP_CellLeftMarginChange	   2024   /* R/W Metric */ 
#define FP_CellRightMarginChange   2025   /* R/W Metric */ 
#define FP_StretchChange           2026   /* R/W MetricPercent */ 


/* Flags for F_ApiSimpleImportElementDefs */
#define FF_IED_REMOVE_OVERRIDES		0x0001
#define FF_IED_REMOVE_BOOK_INFO		0x0002
#define FF_IED_DO_NOT_IMPORT_EDD	0x0004
#define FF_IED_NO_NOTIFY			0x0008
#define FF_IED_DELETE_EMPTY_PAGES	0x0010

/* Dialog item properties */
#define FP_Text			1701	/* R/W String */
#define FP_State		1702	/* R/W Integer */
#define FV_DlgOptNotActive   0
#define FV_DlgOptActive      1
#define FV_DlgOptDontCare    2
#define FP_Labels		1703	/* R/W Strings */
#define FP_NumLines		1704	/* R/O Integer */
#define FP_FirstVis		1705	/* R/W Integer */
#define FP_DoubleClick	1706	/* R/W Bool; R/O for FO_DialogResource */
#define FP_NumItems		1707	/* R/O Integer */
#define FP_Sensitivity	1708    /* R/W Bool */
#define FP_MinVal 		1709    /* R/W Integer */
#define FP_MaxVal 		1710    /* R/W Integer */
#define FP_IncrVal 		1711    /* R/W Integer */
#define FP_Size 		1712    /* R/W Integer */
#define FP_Visibility	1713    /* R/W Bool */
/* The following property is meant for only FM internal usage */
#define FP_PasswordStyle 1714    /* R/W Bool. WebDAV uses it. */
/* The following property is meant for only FM internal usage */
/* getting this property always returns 0. see obj_api.c      */
#define FP_TabStops		1715    /* R/W Ints . WebDAV uses it. */

/* F_ApiDialogEvent events */
#define FV_DlgClose	-1
#define FV_DlgReset	-2
#define FV_DlgNoChange	-3
#define FV_DlgEnter	-4
#define FV_DlgUndo	-5
#define FV_DlgPrevPage	-6
#define FV_DlgNextPage	-7
#define FV_DlgNeedsUpdate -8

/* F_ApiDialogEvent modifier bits */
#define  FV_EvShift		0x0001
#define  FV_EvControl	0x0002
#define  FV_EvMeta		0x0004      /* non-Mac meta or alt key */
#define  FV_EvOption	0x0004      /* Mac Option key   */
#define  FV_EvCaps		0x0008
#define  FV_EvCommand	0x0010      /* Mac Command key  */


/* FO_DBGroup */ /* ! Frame Tech. use only */ 
#define FP_DbItemNum			1800   /* R/W Integer */
#define FP_DbType				1801   /* R/W Enum */
#define FP_DbIdentifier			1802   /* R/W String */
#define FP_DbVarLabelWidth		1803   /* R/W Metric */
#define FP_DbStuffItem			1804   /* R/W Integer */
#define FP_DbSbxNumLines		1805   /* R/W Integer */
#define FP_DbRadioButtonGroup	1806   /* R/W Integer */
#define FP_DbCheckBoxState		1807   /* R/W Integer */
#define FP_DbFirstFocus			1808   /* R/W Integer */
#define FP_DbDefaultButton		1809   /* R/W Integer */
#define FP_DbOKButton			1810   /* R/W Integer */
#define FP_DbCancelButton		1811   /* R/W Integer */
#define FP_DbFbTextBox			1812   /* R/W Integer */
#define FP_DbFbScrollBox		1813   /* R/W Integer */
#define FP_DbFbCurrentDir		1814   /* R/W Integer */
#define FP_DbFbStatus			1815   /* R/W Integer */
#define FP_DbTitleLabel			1816   /* R/W Integer */
#define FP_DbAttributes			1817   /* R/W Strings */
#define FP_DbEditable			1818   /* R/W Integer */
#define FP_DbNoHelp				1819   /* R/W Boolean */

#define FP_LineAscent			1900  /* R/O Metric */
#define FP_LineDescent			1901  /* R/O Metric */
#define FP_LineBaseline			1902  /* R/O Metric */

   /* Menu Cell Properties (FO_Menu & FO_Command) */
#define FP_MenuItemIsEnabled	1922   /* R/O Boolean */
#define FP_NextMenuItemInMenu	1923   /* R/W ID (MenuCell) */
#define FP_PrevMenuItemInMenu	1924   /* R/W ID (MenuCell) */
#define FP_NextMenuItemInSession 1925  /* R/O ID (MenuCell) */
#define FP_MenuType				1926   /* R/O Int */
#define FV_MENU_MENUBAR		1
#define FV_MENU_POPUP		2
#define FV_MENU_ADHOCRULER	3
#define FV_MENU_DEFAULT		4
#define FP_FirstMenuItemInMenu	1927   /* R/W ID (MenuCell) */
#define FP_CommandNum			1928   /* R/W Int */
#define FP_KeyboardShortcutLabel	1929   /* R/W String */
#define FP_KeyboardShortcuts	1930   /* R/W Strings */
#define FP_CanHaveCheckMark		1931   /* R/W Boolean */
#define FP_CheckMarkIsOn		1932   /* R/W Boolean */
#define FP_MenuItemType			1933   /* R/O Int */
#define FV_MENUITEM_FRAME		 1
#define FV_MENUITEM_API			 2
#define FV_MENUITEM_MACRO		 3
#define FV_MENUITEM_EXPANDOMATIC 4
#define FP_ExpandOMaticParent	1934   /* R/O ID (MenuItem) */
#define FP_EnabledWhen			1935   /* R/W Int */
#define FV_ENABLE_ALWAYS_ENABLE	1
#define FV_ENABLE_ALWAYS_DISABLE 2
#define FV_ENABLE_IN_PARA_TEXT	3
#define FV_ENABLE_IN_TEXT_LINE	4
#define FV_ENABLE_IS_TEXT_SEL	5
#define FV_ENABLE_IN_MATH		6
#define FV_ENABLE_IN_TEXT		7
#define FV_ENABLE_OBJ_PROPS		8
#define FV_ENABLE_IN_TABLE		9
#define FV_ENABLE_IN_TABLE_TITLE	10
#define FV_ENABLE_IN_CELL_TEXT	11
#define FV_ENABLE_IS_CELL		12
#define FV_ENABLE_IS_CELLS		13
#define FV_ENABLE_IS_TABLE		14
#define FV_ENABLE_IS_OBJ		15
#define FV_ENABLE_IS_TEXT_FRAME	16
#define FV_ENABLE_IS_OR_IN_FRAME	17
#define FV_ENABLE_IS_AFRAME		18
#define FV_ENABLE_IS_TEXT_INSET	19
#define FV_ENABLE_IS_GRAPHIC_INSET	20
#define FV_ENABLE_IN_FLOW		21
#define FV_ENABLE_COPY			22
#define FV_ENABLE_COPY_FONT		23
#define FV_ENABLE_CAN_PASTE		24
#define FV_ENABLE_IS_VIEW_ONLY	25
#define FV_ENABLE_NEEDS_DOCP_ONLY	26
#define FV_ENABLE_NEEDS_BOOKP_ONLY	27
#define FV_ENABLE_NEEDS_DOCP_OR_BOOKP 28
#define FV_ENABLE_BOOK_HAS_SELECTION 29
#define FV_ENABLE_DOC_OR_BOOK_HAS_SELECTION 30
#define FP_Fcode				1936   /* R/O Int */
#define FP_Fcodes				1937   /* R/O Ints */
#define FP_HelpLink				1938   /* R/W String */
#define FP_HasShiftOrUnshiftCommand 1939   /* R/W Int */
#define FV_ITEM_HAS_SHIFT_COMMAND 				1
#define FV_ITEM_HAS_UNSHIFT_COMMAND 			2
#define FV_ITEM_HAS_NO_SHIFT_OR_UNSHIFT_COMMAND 3
#define FP_ShiftOrUnshiftCommand	1940   /* R/W ID */
#define FP_Mode					1941   /* R/O Int */
#define FV_MODE_MATH			1
#define FV_MODE_NONMATH			2
#define FV_MODE_ALL				3
#define FP_NextCommandInSession	1942  /* R/O ID */
#define FP_Hypertext            1943  /* String */

/* Text Inset Properties  For all types of text insets.*/
/* FP_Name, FP_Unique, FP_TextRange */
/* #define FP_TiIsUnresolved      2075   R/W Boolean for FO_TiApiClient,
                                         R/O for all other types.
										*/
#define FP_NextTiInDoc			2050
#define FP_TiAutomaticUpdate	2051 /* R/W Bool True if the inset
									  * updates automatically 
									  * False otherwise.
									  */
/* These accompany the FA_Note_UpdateAllClientTi notification.
 * FV_UpdateAllAutomaticClientTi means update only those with
 * a FP_TiAutomaticUpdate of True. FV_UpdateAllManualClientTi
 * means update only those with a FP_TiAutomaticUpdate of False.
 * FV_UpdateAllClientTi means update all of the regardless of
 * the FP_TiAutomatic setting.
 */ 
#define FV_UpdateAllClientTi 1
#define FV_UpdateAllAutomaticClientTi 2
#define FV_UpdateAllManualClientTi 3
#define FP_LastUpdate			2052 /* R/W Int when the inset was
									  * last updated, number of
									  * seconds since Jan 1, 1970
									  */
#define FP_TiFile				2053 /* R/W String */

#define FP_TiMacEdition			2054   /* RO Int, when the imported file
										* is a Mac Edition this is the
										* mac edition number.
										*/
#define FP_ImportHint			2055  /* R/W String.  When the imported
									   * file may be hard to type, this
									   * is used as a file type 
									   * hint to the update code.
									   * Currently, on Windows Only.
									   */
#define FP_TiLocked				2056  /* R/W Bool.  Before adding/deleting text
									   * to an inset, it should be unlocked.
									   * It should be relocked when finished.
									   */
#define FP_TiFileModDate        2057  /* RO Int.  The modification time
									   * of the inset's source file.
									   */
#define FP_TiIsNested           2058  /* RO Boolean.  True when the text
									   * inset is nested (lies within another
									   * text inset) false otherwise.
									   */

/* FO_TiFlow objects*/
#define FP_TiMainFlow			2059 /* R/W Bool True if the 
									  *	main flow in the source 
									  * document is used.
									  */
#define FP_TiFlowName			2060 /* R/W String. Flow Name
									  * if the main flow is
									  * not used.
									  */
#define FP_TiFlowPageSpace		2061 /* R/W Flag, either FV_BodyPage,
									  * or FV_ReferencePage.
									  */
#define FV_BODY_PAGE		 0x00
#define FV_REFERENCE_PAGE	 0x02

#define FP_TiFormat				2062 /* R/W Enum, source of the
									  * imported text's formatting.
									  * FV_SourceDoc, FV_EnclosingDoc,
									  * FV_PlainText
									  */
#define FP_TiRemovePageBreaks	2063 /* R/W Bool When the source of
									  * the formatting is the enclosing 
									  * document, should page breaks be
									  * removed during import?
									  */
#define FP_TiRemoveOverrides	2064 /* R/W Bool When the source of
									  * the formatting is the enclosing 
									  * document, should overrides be
									  * removed during import?
									  */
/* FO_TiText and FO_TiTextTable objects */
#define FP_TiTextEncoding    2073   /* R/O Int which identifies the encoding of the
									        * of the text file.
											*/

#define FV_IsoLatin		1
#define FV_ASCII			2
#define FV_ANSI				3
#define FV_MacANSI			4
#define FV_JIS				5
#define FV_Shitf_JIS		6
#define FV_EUC				7
#define FV_BIG5				8
#define FV_EUC_CNS			9
#define FV_GB				10
#define FV_HZ				11
#define FV_Korean			12

/* FO_TiText, objects */
#define FP_TiEOLisEOP			2065	/* R/W Bool, True if EOL in imported
										 * text is treated as  EOP False 
										 * otherwise.
										 */

/* FO_TiTextTable, objects */
#define FP_TiTblTag				2066	/* R/W String name of the table 
										 * tag.
										 */
#define FP_TiNumHeaderRows		2067    /* R/W Int. Number of heading rows.*/

#define FP_TiHeadersEmpty		2068	/* R/W Bool. True if the heading
										 * rows in the table are left empty.
										 */

#define FP_TiByRows				2069	/* R/W Bool. True if each paragraph
										 * in the imported text is treated as
										 * a row, False otherwise.
										 */
#define FP_TiSeparator			2070	/* R/W String. Applies when 
										 * FP_TiByRows is True. String
										 * used as Cell separator.
										 */
#define FP_TiNumSeparators		2071	/* R/W Int. Applies when FP_TiByRows
										 * is True. Number of separators
										 * between Cells.
										 */
#define FP_TiNumCols			2072	/* R/W Int. Applies when FP_TiByRows
										 * Is False. Number of colums in
										 * the table.
										 */
/* FO_TiApiClient */
#define FP_TiIsUnresolved       2075    /* R/W Boolean for FO_TiApiClient*/
#define FP_TiClientName			2076    /* R/W String, the name the
										 * FM uses to know the client.
										 */
#define FP_TiClientSource		2077	/* R/W String client
										 * can use to distinquish source
										 */
#define FP_TiClientType			2078	/* R/W String which is displayed
										 * in the Text Inset Properties
										 * dialog.
										 */
#define FP_TiClientData			2079    /* R/W String for clients use.*/

   /* SubCol */
#define FP_ContentHeight		2219     /* R/O Metric */
#define FP_ParentTextFrame		2200   /* R/O ID(TextFrame) */ 
#define FP_PrevSubCol			2201   /* R/O ID(SubCol) */ 
#define FP_NextSubCol			2202   /* R/O ID(SubCol) */ 
#define FP_Overflowed           983    /* R/O Boolean */ 

/* FO_CombinedFontDefn */
#define FP_FirstCombinedFontDefnInDoc	2280	/* R/O ID(Combined Font) */
#define FP_NextCombinedFontDefnInDoc	2281	/* R/O ID(Combined Font) */
#define FP_BaseFamily					2282	/* R/W IntT */
#define FP_AllowBaseFamilyBoldedAndObliqued	2283	/* R/W BoolT */
#define FP_WesternFamily				2284	/* R/W IntT */
#define FP_WesternSize					2285	/* R/W MetricT */
#define FP_WesternShift					2286	/* R/W MetricT */

/* part of Char Format */
#define FP_CombinedFontFamily			2287    /* R/W ID(Combined Font) */

/* F_ApiFind Items */
#define FS_FindText						1 /* StringT */

#define FS_FindElementTag				2 /* StringT array with 3 items */
#define FV_FindElemTag   0 /* Location of the element tag in above array. */
#define FV_FindAttrName  1 /* Location of the element tag in above array. */
#define FV_FindAttrValue 2 /* Location of the element tag in above array. */
#define FV_NumFindElementItems FV_FindAttrValue + 1

#define FS_FindCharFmt					3 /* ObjHandleT of an FO_CharFmtId */
#define FS_FindPgfTag					4 /* StringT */
#define FS_FindCharTag					5 /* StringT */
#define FS_FindTableTag					6 /* StringT */

#define FS_FindObject					8 /* Enum one of the following:*/
#define FV_FindAnyMarker				0
#define FV_FindAnyXRef				    1
#define FV_FindUnresolvedXRef			2
#define FV_FindAnyTextInset				3
#define FV_FindUnresolvedTextInset		4
#define FV_FindAnyPub					5
#define FV_FindAnyVariable				6	
#define FV_FindAnchoredFrame			7
#define FV_FindFootnote					8
#define FV_FindAnyTable					9
#define FV_FindAutomaticHyphen			10
#define FV_FindAnyRubi					11

#define FS_FindMarkerOfType				9 /* StringT */
#define FS_FindMarkerText				10 /* StringT */
#define FS_FindXRefWithFormat			11 /* StringT */
#define FS_FindNamedVariable			12 /* StringT */
#define FS_FindCondTextInCondTags		13 /* Strings */
#define FS_FindCondTextNotInCondTags	14 /* Strings */
#define FS_FindCustomizationFlags		15 /* IntT the following ored together */
#define FF_FIND_CONSIDER_CASE	((IntT)   0x01)
#define FF_FIND_WHOLE_WORD		((IntT)   0x02)
#define FF_FIND_USE_WILDCARDS	((IntT)   0x04)
#define FF_FIND_BACKWARDS		((IntT)   0x08) 
#define FS_FindWrap                     16 /* Boolean Default True */

/* Parameters for F_ApiSpell (experimental interface) */
#define FS_SpellOptions        1000    /* Spell options, e.g. find repeated
										  words, etc. */
#define FS_TwoInARowString     1001    /* String of all two in a row symbol */
#define FS_TextBeforeString    1002    /* String for text before */
#define FS_TextAfterString     1003    /* String for Text after */
#define FS_WordContainString   1004    /* String of words with in a word */
#define FS_SpellAction         1005
#define FV_CheckDocument                      1
#define FV_CheckCurrentPage                   2 
#define FV_WriteUnknownWordsToFile            3
#define FS_OutputFilePathName  1006
#define FS_SpellWrap           1007    /* Boolean Default True */
#define FS_NumSpellParams      (FS_SpellWrap - FS_SpellOptions + 1)
                               /* Subtract the base (FS_SpellOptions) and add
								* 1 because the base starts at 0.
								*/

/*
 * Flags for FS_SpellOptions (experimental interface)
 */
#define FF_SpellRepeatedWords             ((IntT) 0x0001)
#define FF_SpellUnusualHyphenation        ((IntT) 0x0002)
#define FF_SpellUnusualCapitalization     ((IntT) 0x0004)
#define FF_SpellTwoInARow                 ((IntT) 0x0008)
#define FF_SpellStraightQuotes            ((IntT) 0x0010)
#define FF_SpellExtraSpaces               ((IntT) 0x0020)
#define FF_SpellSpaceBefore               ((IntT) 0x0040)
#define FF_SpellSpaceAfter                ((IntT) 0x0080)
#define FF_SpellSingleCharWords           ((IntT) 0x0100)
#define FF_SpellUpperCaseWords            ((IntT) 0x0200)
#define FF_SpellContainingWords           ((IntT) 0x0400)
#define FF_SpellRomanNum                  ((IntT) 0x0800)
#define FF_SpellWordsWithDigits           ((IntT) 0x1000)

/* Values returned by  F_ApiSpell in the spellError parameter.*/
#define FV_SpellMisspelling	    1
#define FV_SpellCapitalization  2
#define FV_SpellRepeatedWord    3
#define FV_SpellRepeatedLetter  4
#define FV_SpellHyphenation     5
#define FV_SpellExtraSpace      6
#define FV_SpellSpaceAfter      7
#define FV_SpellSpaceBefore     8
#define FV_SpellStraightQuotes  9

/* Values for F_ApiDictionary (experimental interface) */
#define FV_UserDictionary                   1
#define FV_DocumentDictionary               2
/* 3 is reserved for site dictionary support */
#define FV_WriteDictionaryToFile            4
#define FV_MergeDictionaryContents          5 
#define FV_SetDictionaryToNoneDictionary    6
#define FV_ClearDocDictionary               7
#define FV_ChangePersonalDictionary         8
#define FV_GetDictionaryStrings		        9

/* FO_CursorResource  (experimental interface) */
#define FP_CursorData					2294	/* R/O PtrT */
#define FP_CursorTypes					2295	/* R/W IntT */
#define FP_StructureCursorTypes			2296	/* R/W IntT */

#define FP_SecNumComputeMethod	        915
#define FP_SectionNumStyle		        916
#define FP_SectionNumber		        917
#define FP_SectionNumText		        918

#define FP_SubsecNumComputeMethod	    815
#define FP_SubsectionNumStyle		    816
#define FP_SubsectionNumber			    817
#define FP_SubsectionNumText		    818

#define FP_DoNotGenerateErrorLog                819
#define FP_DocOpenClientEncounteredErrors       820
#define FP_OpenAndSaveXmlBookComponentDoc       822
#define FP_XmlIsBook                            823

/* Flags to set Cursors, used in FP_CursorTypes  (experimental interface) */
#define FF_Watch_Cursor						((IntT) 0x00000001)	/* please wait... */
#define FF_Arrow_Cursor						((IntT) 0x00000002)	/* regular arrow */
#define FF_I_Beam_Cursor					((IntT) 0x00000004)	/* Text I-beam */
#define FF_Crosshair_Cursor					((IntT) 0x00000008)	/* Crosshair or reticule for drawing */
#define FF_Top_Left_Cursor					((IntT) 0x00000010)	/* There are 8 cursor shapes for the eight handles around an object */
#define FF_Top_Cursor						((IntT) 0x00000020)	
#define FF_Top_Right_Cursor					((IntT) 0x00000040)
#define FF_Right_Cursor						((IntT) 0x00000080)
#define FF_Bot_Right_Cursor					((IntT) 0x00000100)
#define FF_Bot_Cursor						((IntT) 0x00000200)
#define FF_Bot_Left_Cursor					((IntT) 0x00000400)
#define FF_Left_Cursor						((IntT) 0x00000800) /* end of 8 cursor shapes for handles */
#define FF_Reshape_Cursor					((IntT) 0x00001000)	
#define FF_Dialog_Cursor					((IntT) 0x00002000)
#define FF_Textline_Cursor					((IntT) 0x00004000)
#define FF_Object_Selection_Cursor			((IntT) 0x00008000) /* MOVE_CURSOR_SHAPE */
#define FF_Cells_Resize_Cursor				((IntT) 0x00010000)	
#define FF_Rotate_Cursor					((IntT) 0x00020000)
#define FF_I_Beam_L_Cursor					((IntT) 0x00040000)
#define FF_I_Beam_R_Cursor					((IntT) 0x00080000)
#define FF_I_Beam_90_Cursor					((IntT) 0x00100000)
#define FF_Help_Cursor						((IntT) 0x00200000)
#define FF_Hypertext_Cursor					((IntT) 0x00400000)
/* Flags to set Structure cursors, used in FP_StructureCursorTypes */
#define FF_SW_Promote_Cursor				((IntT) 0x00000001)
#define FF_SW_Demote_Cursor					((IntT) 0x00000002)
#define FF_SW_NudgeUp_Cursor				((IntT) 0x00000004)
#define FF_SW_NudgeDown_Cursor				((IntT) 0x00000008)
#define FF_SW_Drag_Cursor					((IntT) 0x00000010)
#define FF_SW_CopyDrag_Cursor				((IntT) 0x00000020)
#define FF_SW_Right_Cursor					((IntT) 0x00000040)
#define FF_xArrow_Cursor					((IntT) 0x00000080)	/* regular arrow in XOR */
#define FF_xMove_Cursor						((IntT) 0x00000100) /* move cursor in XOR */
#define FF_Button_Cursor					((IntT) 0x00000200)
#define FV_CmdOpenXmlElementId                 821

#ifdef FAPI_4_BEHAVIOR
#define FP_FirstPgfInTextCol	FP_FirstPgf
#define FP_LastPgfInTextCol		FP_LastPgf

#define FP_FirstAFrameInTextCol	FP_FirstAFrame
#define FP_LastAFrameInTextCol	FP_LastAFrame
#define FP_PrevAFrameInTextCol	FP_PrevAFrame
#define FP_NextAFrameInTextCol	FP_NextAFrame

#define FP_FirstFnInTextCol		FP_FirstFn
#define FP_LastFnInTextCol		FP_LastFn
#define FP_PrevFnInTextCol		FP_PrevFn
#define FP_NextFnInTextCol		FP_NextFn

#define FP_FirstCellInTextCol	FP_FirstCell
#define FP_LastCellInTextCol	FP_LastCell
#define FP_PrevCellInTextCol	FP_PrevCell
#define FP_NextCellInTextCol	FP_NextCell

#define FP_FirstTextColInFlow	FP_FirstTextFrameInFlow
#define FP_LastTextColInFlow	FP_LastTextFrameInFlow
#define FP_PrevTextColInFlow	FP_PrevTextFrameInFlow
#define FP_NextTextColInFlow	FP_NextTextFrameInFlow

#define FP_InTextCol			FP_InTextFrame
#endif