/* Frame API Library header fapi.h */

#ifndef FAPI_H
#define FAPI_H

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

#include "f_types.h"
#include "fapidefs.h"

#ifdef MACINTOSH
#pragma options align=mac68k
#endif

#define METRIC(a) ((MetricT)(a) << 16)   /* no fractions, please */

#ifndef FAPI_HACK

/*! MUST MATCH api_to_fm.x !!! */


typedef struct {
	UIntT len;
	StringT *val;
} F_StringsT;
typedef struct {
	UIntT len;
	UByteT *val;
} F_UBytesT;
typedef struct {
	UIntT len;
	UIntT *val;
} F_UIntsT;
typedef struct {
	MetricT x;
	UCharT type;
	StringT leader;
	UCharT decimal;
} F_TabT;
typedef struct {
	UIntT len;
	IntT *val;
} F_IntsT;
typedef struct {
	UIntT len;
	MetricT *val;
} F_MetricsT;
typedef struct {
	StringT name;
	BoolT required;
#ifdef FAPI_5_BEHAVIOR
	BoolT readOnly;
#else
	UIntT flags;
#endif
	IntT attrType;
	F_StringsT choices;
	F_StringsT defValues;
	StringT rangeMin;
	StringT rangeMax;
} F_AttributeDefT;
typedef struct {
	UIntT len;
	F_AttributeDefT *val;
} F_AttributeDefsT;
typedef struct {
	StringT name;
	F_StringsT values;
	UByteT valflags; /* Read-Only */
	UByteT allow;
} F_AttributeT;
typedef struct {
	UIntT len;
	F_AttributeT *val;
} F_AttributesT;
typedef struct {
	F_ObjHandleT objId;
	UIntT flags;
} F_ElementCatalogEntryT;
typedef struct {
	UIntT len;
	F_ElementCatalogEntryT *val;
} F_ElementCatalogEntriesT;
typedef struct
	{
	 F_ObjHandleT objId;
	 IntT offset;
	} F_TextLocT;
typedef struct {
	 F_TextLocT beg;
	 F_TextLocT end;
	} F_TextRangeT;
typedef struct
	{
	 F_ObjHandleT parentId;
	 F_ObjHandleT childId;
	 IntT offset;
	} F_ElementLocT;
typedef struct
	{
	 F_ElementLocT beg;
	 F_ElementLocT end;
	} F_ElementRangeT;
typedef struct {
	UIntT len;
	F_TabT *val;
} F_TabsT;
typedef struct {
	UIntT len;
	F_PointT *val;
} F_PointsT;
typedef struct {
	UIntT family;
	UIntT variation;
	UIntT weight;
	UIntT angle;
} F_FontT;
typedef struct {
	UIntT len;
	F_FontT *val;
} F_FontsT;
typedef struct {
	F_ObjHandleT combinedFont;
	UIntT variation;
	UIntT weight;
	UIntT angle;
} F_CombinedFontT;
typedef struct {
	UIntT len;
	F_CombinedFontT *val;
} F_CombinedFontsT;
#endif /* FAPI_HACK */
/*! MUST MATCH fm_geometry.h !!! */
typedef struct 
	{MetricT x,y,w,h;
	} F_RectT;
#ifndef FAPI_HACK

/*
 * Note: The following union must not exceed 16 bytes in length if we are to
 * maintain compatibility with clients written to work with FM 4.0.
 */
typedef union {
	StringT sval;
	F_StringsT ssval;
	F_MetricsT msval;
	F_PointsT psval;
	F_TabsT tsval;
	F_TextLocT tlval;
	F_TextRangeT trval;
	F_ElementCatalogEntriesT csval;
	F_IntsT isval;
	F_UIntsT uisval;
	F_UBytesT ubsval;
	IntT ival;
	F_AttributeDefsT adsval;
	F_AttributesT asval;
	F_ElementRangeT *erng;
} F_ValT;

typedef struct {
	IntT valType;
	F_ValT u;
} F_TypedValT;

/* This intentionally does not match api_to_fm.x !!! */
#ifndef DONT_DEFINE_PROPIDENT	/* To avoid problems because of mismatch */
typedef struct {
	IntT num;
	StringT name;
} F_PropIdentT;
#endif

#if !F_PROPVAL_T_DECLARED
#define F_PROPVAL_T_DECLARED
typedef struct F_PropValT F_PropValT;
#endif
struct F_PropValT {
	F_PropIdentT propIdent;
	F_TypedValT propVal;
};

#if !F_PROPVALS_T_DECLARED
#define F_PROPVALS_T_DECLARED
typedef struct F_PropValsT F_PropValsT;
#endif
struct F_PropValsT {
	UIntT len;
	F_PropValT *val;
};

#endif /* FAPI_HACK */


/* MUST MATCH aplib.h */
typedef struct {
	 F_ObjHandleT sumId;
	 F_ObjHandleT compId;
	} F_CompareRetT;

/*! MUST MATCH api_to_fm.h !!! */
/*! MUST MATCH fm_api.h !!! */
/* MUST MATCH F_TextLocT in api_to_fm.x !!! */
typedef struct 
	{
	 IntT offset;
#if FDK_PTR_SIZE == 8
	/*
	 * FrameMaker declares this structure incompatibly; to make
	 * things line up, we insert this shim.  (Specifically, not
	 * 64-bit machines we need the dataType field to be at
	 * structure offset 8, and the u.sdata field to be at
	 * offset 16).
	 */
	 IntT _shim;
#endif
	 IntT dataType;
	 union {
		StringT sdata;
		IntT idata;
		} u;
	} F_TextItemT;

/*! MUST MATCH api_to_fm.h !!! */
typedef struct {
	UIntT len;
	F_TextItemT *val;
} F_TextItemsT;

typedef VoidT (*F_FdFuncT)FARGS((IntT, IntT)); /* Unix only */

#ifdef MACINTOSH
#pragma options align=reset
#endif

#ifdef __cplusplus
extern "C" {
#endif

extern IntT FA_bailout; /* True when FM has told us to quit */
extern IntT FA_errno; /* Like Unix's errno; see fapidefs.h for values */
#ifdef UNIX
extern IntT FA_clientno; /* client number; don't ever write here */
extern IntT FA_dynalink; /* True iff we're a dynamically-linked client */
extern IntT FA_SelectMask; /* From select() call in F_ApiService() */
#endif

extern IntT F_ApiGetPropIndex FARGS((const F_PropValsT *pvp, IntT propNum));

/* Called by FM, and expected to be defined in API program */
extern VoidT F_ApiInitialize FARGS((IntT init));
#ifdef FAPI_4_BEHAVIOR
extern VoidT F_ApiNotify FARGS((IntT notification, F_ObjHandleT docId, StringT filename));
#else
extern VoidT F_ApiNotify FARGS((IntT notification, F_ObjHandleT docId, StringT sparm, IntT iparm));
#endif
extern VoidT F_ApiCommand FARGS((IntT command)); /* From Menu or Keyboard Shortcut */
extern VoidT F_ApiMessage FARGS((StringT message, F_ObjHandleT docId,
	F_ObjHandleT objId)); /* From Hypertext marker */
extern VoidT F_ApiEmergency FARGS((VoidT)); /* FM went away */
extern VoidT F_ApiDialogEvent FARGS((IntT dlgNum, IntT item, IntT modifiers));
extern VoidT F_ApiNetLibSetAuthFunction FARGS((NetLib_AuthFunction func));

/* Called by API program */
extern IntT F_ApiClientNumber FARGS((VoidT));
extern StringT F_ApiClientName FARGS((VoidT));
extern StringT F_ApiClientDir FARGS((VoidT));
#ifdef UNIX
extern IntT F_ApiSetClientDir FARGS((ConStringT dirName));
#endif
extern IntT F_ApiNotification FARGS((IntT notification, IntT state));
	 /* Request that we do/don't get a particular notification */
extern IntT F_ApiReturnValue FARGS((IntT retval));
extern IntT F_ApiAddMenu
	FARGS((ConStringT toMenu, ConStringT menu, ConStringT label));
extern IntT F_ApiRemoveMenu FARGS((ConStringT fromMenu, ConStringT label));
extern IntT F_ApiAddCommand FARGS((IntT cmd, ConStringT toMenu, ConStringT tag,
	ConStringT label, ConStringT shortcut)); /* Add a menu item */
extern IntT F_ApiMenuExists FARGS((ConStringT menu)); /* Does a menu exist. */
extern IntT F_ApiShortcutExists FARGS((ConStringT shortcut));
	/* Does a shortcut exist? */
extern F_ObjHandleT F_ApiDefineCommand FARGS((IntT cmd, ConStringT tag,
	ConStringT label, ConStringT shortcut)); /* Define a menu item */
extern F_ObjHandleT F_ApiDefineMenu FARGS((ConStringT tag, ConStringT label));
	/* Define a Menu */
extern IntT F_ApiAddCommandToMenu FARGS((F_ObjHandleT toMenuId,
	F_ObjHandleT commandId)); /* Add a command to a menu */
extern IntT F_ApiAddMenuToMenu FARGS((F_ObjHandleT toMenuId,
	F_ObjHandleT menuId)); /* Add a menu to a menu */
extern F_ObjHandleT F_ApiDefineAndAddCommand FARGS((IntT cmd,
	F_ObjHandleT toMenuId, ConStringT tag, ConStringT label,
	ConStringT shortcut));
	/* Define a command and add it to a menu */
extern F_ObjHandleT F_ApiDefineAndAddMenu FARGS((F_ObjHandleT toMenuId,
	ConStringT tag, ConStringT label)); /* Define a menu and add it to a menu */
extern IntT F_ApiLoadMenuCustomizationFile FARGS((ConStringT filename,
	BoolT silent)); /* Load menu customization file */
extern IntT F_ApiAnimateMenu FARGS((F_ObjHandleT menuId,
	F_ObjHandleT menuitemId, IntT sleep, ConStringT realLabel,
	F_ObjHandleT docOrBookId, BoolT action));
	/* Pull down a menu & select menu item */
extern F_ObjHandleT F_ApiMenuItemInMenu FARGS((F_ObjHandleT menuId,
	F_ObjHandleT menuitemId, BoolT recursive));
	/* Return the menu in which a menu item resides */

extern IntT F_ApiCallClient FARGS((ConStringT clname, ConStringT arg));
	/* Cross-client call */
extern IntT F_ApiHypertextCommand FARGS((F_ObjHandleT docId,
	ConStringT hypertext));
extern IntT F_ApiApplyPageLayout FARGS((F_ObjHandleT docId,
	F_ObjHandleT destPage, F_ObjHandleT srcPage));
extern F_FontsT F_ApiFamilyFonts FARGS((IntT family));
extern F_CombinedFontsT F_ApiCombinedFamilyFonts FARGS((F_ObjHandleT combinedFontId));
extern IntT F_ApiGetFontFamilyValue FARGS((ConStringT familyName));
extern IntT F_ApiGetFontVariationValue FARGS((ConStringT variationName));
extern IntT F_ApiGetFontWeightValue FARGS((ConStringT weightName));
extern IntT F_ApiGetFontAngleValue FARGS((ConStringT angleName));
extern StatusT F_ApiMakeGhostFont FARGS((const F_FontT *ghostFont, 
	ConStringT ghostPlatformFontName, ConStringT ghostPostScriptFontName, 
	ConStringT ghostPanoseName, ConStringT ghostEncodingName, 
	const F_FontT *surrogateFont));

extern VoidT F_ApiUndoStartCheckPoint FARGS((F_ObjHandleT docId, 
	ConStringT description));
extern VoidT F_ApiUndoEndCheckPoint FARGS((F_ObjHandleT docId));
extern VoidT F_ApiUndoCancel FARGS((F_ObjHandleT docId));
extern VoidT F_ApiEnableUnicode FARGS((BoolT enable));
extern F_StringsT F_ApiGetFontDirsList FARGS((VoidT));

/*
 * We want to go away, but we will be restarted restarted later if
 * there's a message or command for us
 */
extern VoidT F_ApiBailOut FARGS((VoidT));

#if defined(UNIX) || defined(WIN_FRAME)
/*
 * Tell FM we're done processing an F_ApiCommand, so the interactive user
 * gets control back, but we can keep on doing some other (presumably slow)
 * stuff in the API program
 */
extern IntT F_ApiService FARGS((IntT *imaskp));
extern IntT F_ApiAlive FARGS((VoidT));
extern IntT F_ApiRun FARGS((VoidT));
extern ConStringT F_ApiStartUp FARGS((F_FdFuncT fd_func));
extern VoidT F_ApiShutDown FARGS((VoidT));
extern VoidT F_ApiErr FARGS((ConStringT message));
extern IntT F_ApiDisconnectFromSession FARGS((VoidT));
#endif

#ifdef UNIX
extern IntT F_ApiDoneCommand FARGS((VoidT));
extern IntT F_ApiTakeControl FARGS((VoidT)); /* Asynchronous calls coming */
extern F_FdFuncT F_ApiSetFdFunc FARGS((F_FdFuncT newval));
extern IntT F_ApiConnectToSession FARGS((ConStringT clientName,
	ConStringT hostname, IntT prognum));
extern IntT F_ApiFindXSessionAddress FARGS((ConStringT displayname,
	ConStringT propname, StringT *hostnamep, IntT *prognump));
#endif	/* UNIX */

#ifdef WIN_FRAME
extern IntT F_ApiWinConnectSession FARGS((const F_PropValsT *connectProps,
	ConStringT hostname, const struct _GUID *service));
VoidT F_ApiWinInstallDefaultMessageFilter(VoidT);
#endif

/* Doc open/close/etc. for API program */
extern F_PropValsT F_ApiGetImportDefaultParams FARGS((VoidT));
extern F_ObjHandleT F_ApiImport FARGS((F_ObjHandleT enclosingDocId,
	const F_TextLocT *textLocp, ConStringT fileName,
	const F_PropValsT *importParamsp, F_PropValsT **importReturnParamspp));
extern IntT F_ApiUpdateTextInset FARGS((F_ObjHandleT docId,
	F_ObjHandleT insetId));
extern IntT F_ApiDeleteTextInsetContents FARGS((F_ObjHandleT docId,
	F_ObjHandleT insetId));
extern F_ObjHandleT F_ApiOpen FARGS((ConStringT fileName,
	const F_PropValsT *openParamsp, F_PropValsT **openReturnParamspp));
extern F_ObjHandleT F_ApiSimpleOpen FARGS((ConStringT fileName,
	IntT interactive));
extern F_ObjHandleT F_ApiSimpleNewDoc FARGS((ConStringT templateName,
	IntT interactive));
extern F_PropValsT F_ApiGetOpenDefaultParams FARGS((VoidT));
extern VoidT F_ApiPrintOpenStatus FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintOpenStatusToChannel FARGS((const F_PropValsT *p, ChannelT channel));
extern VoidT F_ApiPrintSaveStatus FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintSaveStatusToChannel FARGS((const F_PropValsT *p, ChannelT channel));
extern VoidT F_ApiPrintImportStatus FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintImportStatusToChannel FARGS((const F_PropValsT *p, ChannelT channel));
extern VoidT F_ApiPrintUpdateBookStatus FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintUpdateBookStatusToChannel FARGS((const F_PropValsT *p, ChannelT channel));
extern IntT F_ApiExport FARGS((F_ObjHandleT enclosingDocId, ConStringT fileName,
	const F_PropValsT *exportParamsp, F_PropValsT **exportReturnParamspp));
extern F_PropValsT F_ApiGetExportDefaultParams FARGS((VoidT));
extern VoidT F_ApiPrintExportStatus FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintExportStatusToChannel FARGS((const F_PropValsT *p, ChannelT channel));
extern VoidT F_ApiPrintFAErrno FARGS((VoidT));
extern VoidT F_ApiPrintFAErrnoToChannel FARGS((ChannelT channel));
extern ConStringT F_ApiErrorName FARGS((IntT i));
extern VoidT F_ApiPrintErrno FARGS((IntT i));
extern VoidT F_ApiPrintErrnoToChannel FARGS((IntT i, ChannelT channel));
extern ConStringT F_ApiInitializationName FARGS((IntT i));
extern VoidT F_ApiPrintInitialization FARGS((IntT i));
extern VoidT F_ApiPrintInitializationToChannel FARGS((IntT i, ChannelT channel));
extern ConStringT F_ApiNotificationName FARGS((IntT i));
extern VoidT F_ApiPrintNotification FARGS((IntT i));
extern VoidT F_ApiPrintNotificationToChannel FARGS((IntT i, ChannelT Channel));
extern F_ObjHandleT F_ApiSave FARGS((F_ObjHandleT Id, ConStringT saveAsName,
	const F_PropValsT *saveParamsp, F_PropValsT **saveReturnParamspp));
extern F_ObjHandleT F_ApiSimpleSave FARGS((F_ObjHandleT docId,
	ConStringT saveAsName, IntT interactive));
extern F_PropValsT F_ApiGetSaveDefaultParams FARGS((VoidT));
extern IntT F_ApiCheckStatus FARGS((const F_PropValsT *p, IntT propNum));
extern F_PropValsT F_ApiAllocatePropVals FARGS((IntT numProps));
extern VoidT F_ApiAppendVal FARGS((F_PropValsT *pv, IntT prop, const F_TypedValT *val));
extern VoidT F_ApiAppendStringProp FARGS((F_PropValsT *pv, IntT prop, ConStringT val));
extern VoidT F_ApiAppendIntProp FARGS((F_PropValsT *pv, IntT prop, IntT val));
extern VoidT F_ApiAppendMetricProp FARGS((F_PropValsT *pv, IntT prop, MetricT val));
extern VoidT F_ApiAppendStringsProp FARGS((F_PropValsT *pv, IntT prop, const F_StringsT * val));

extern VoidT F_ApiDeallocateVal FARGS((F_TypedValT *p));
extern VoidT F_ApiDeallocateStrings FARGS((F_StringsT *stringsp));
extern VoidT F_ApiDeallocateUBytes FARGS((F_UBytesT *ubytesp));
extern VoidT F_ApiDeallocateMetrics FARGS((F_MetricsT *metricsp));
extern VoidT F_ApiDeallocateInts FARGS((F_IntsT *intsp));
extern VoidT F_ApiDeallocateUInts FARGS((F_UIntsT *uintsp));
extern VoidT F_ApiDeallocatePoints FARGS((F_PointsT *pointsp));
extern VoidT F_ApiDeallocateElementCatalogEntries
	FARGS((F_ElementCatalogEntriesT *ecep));
extern VoidT F_ApiDeallocateTab FARGS((F_TabT *tabp));
extern VoidT F_ApiDeallocateTabs FARGS((F_TabsT *tabsp));
extern VoidT F_ApiDeallocateAttributeDef FARGS((F_AttributeDefT *adp));
extern VoidT F_ApiDeallocateAttributeDefs FARGS((F_AttributeDefsT *adsp));
extern VoidT F_ApiDeallocateAttribute FARGS((F_AttributeT *ap));
extern VoidT F_ApiDeallocateAttributes FARGS((F_AttributesT *ap));
extern VoidT F_ApiDeallocatePropVal FARGS((F_PropValT *propp));
extern VoidT F_ApiDeallocatePropVals FARGS((F_PropValsT *pvp));
extern VoidT F_ApiDeallocateTextItem FARGS((F_TextItemT *itemp));
extern VoidT F_ApiDeallocateTextItems FARGS((F_TextItemsT *itemsp));
extern VoidT F_ApiDeallocateString FARGS((StringT *s));
extern VoidT F_ApiDeallocateFonts FARGS((F_FontsT *fontsp));
extern VoidT F_ApiDeallocateCombinedFonts FARGS((F_CombinedFontsT *fontsp));

extern VoidT F_ApiPrintTextItem FARGS((const F_TextItemT *p));
extern VoidT F_ApiPrintTextItemToChannel FARGS((const F_TextItemT *p, ChannelT channel));
extern VoidT F_ApiPrintTextItems FARGS((const F_TextItemsT *p));
extern VoidT F_ApiPrintTextItemsToChannel FARGS((const F_TextItemsT *p, ChannelT channel));
extern VoidT F_ApiPrintPropVal FARGS((const F_PropValT *p));
extern VoidT F_ApiPrintPropValToChannel FARGS((const F_PropValT *p, ChannelT channel));
extern VoidT F_ApiPrintPropVals FARGS((const F_PropValsT *p));
extern VoidT F_ApiPrintPropValsToChannel FARGS((const F_PropValsT *p,  ChannelT channel));
extern IntT F_ApiSimpleImportElementDefs FARGS((F_ObjHandleT docOrBookId,
	F_ObjHandleT fromDocOrBookId, IntT importFlags));
extern IntT F_ApiSimpleImportFormats FARGS((F_ObjHandleT bookId,
	F_ObjHandleT fromDocId, IntT formatFlags));
extern IntT F_ApiSimpleGenerate FARGS((F_ObjHandleT bookId, IntT interactive,
	IntT makeVisible));
extern ErrorT F_ApiUpdateBook FARGS((F_ObjHandleT bookId,
	const F_PropValsT *updateBookParamsp,
	F_PropValsT **updateBookReturnParamspp));
extern F_PropValsT F_ApiGetUpdateBookDefaultParams FARGS((VoidT));
extern IntT F_ApiSilentPrintDoc FARGS((F_ObjHandleT docId));
extern F_ObjHandleT F_ApiCustomDoc FARGS((MetricT width, MetricT height,
	IntT numCols, MetricT columnGap, MetricT topMargin, MetricT botMargin,
	MetricT leftinsideMargin, MetricT rightoutsideMargin, IntT sidedness,
	BoolT makeVisible));
extern IntT F_ApiClose FARGS((F_ObjHandleT docId, IntT closeDocFlags));
extern F_CompareRetT F_ApiCompare FARGS((F_ObjHandleT olderId,
	F_ObjHandleT newerId, IntT flags, ConStringT insertCondTag,
	ConStringT deleteCondTag, ConStringT replaceText, IntT compareThreshold));
extern IntT F_ApiCut FARGS((F_ObjHandleT docId, IntT flags));
extern IntT F_ApiClear FARGS((F_ObjHandleT docId, IntT flags));
extern IntT F_ApiCopy FARGS((F_ObjHandleT docId, IntT flags));
extern IntT F_ApiPaste FARGS((F_ObjHandleT docId, IntT flags));
extern IntT F_ApiRestartPgfNumbering FARGS((F_ObjHandleT docId));
extern IntT F_ApiResetReferenceFrames FARGS((F_ObjHandleT docId));
extern IntT F_ApiRehyphenate FARGS((F_ObjHandleT docId));
extern IntT F_ApiClearAllChangebars FARGS((F_ObjHandleT docId));
extern IntT F_ApiResetEqnSettings FARGS((F_ObjHandleT docId));
extern IntT F_ApiUpdateVariables FARGS((F_ObjHandleT docId));
extern IntT F_ApiUpdateXRefs FARGS((F_ObjHandleT docId, IntT updateXRefFlags));
extern IntT F_ApiUpdateXRef FARGS((F_ObjHandleT destDocId, F_ObjHandleT srcDocId, F_ObjHandleT xrefId));
extern IntT F_ApiReformat FARGS((F_ObjHandleT docId));
extern IntT F_ApiRedisplay FARGS((F_ObjHandleT docId));
extern IntT F_ApiScrollToText FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep));
extern IntT F_ApiCenterOnText FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep));

extern IntT F_ApiStringLen FARGS((ConStringT s));
extern StringT F_ApiCopyString FARGS((ConStringT s));
extern F_StringsT F_ApiCopyStrings FARGS((const F_StringsT *fromstrings));
extern F_UBytesT F_ApiCopyUBytes FARGS((const F_UBytesT *fromubytes));
extern F_MetricsT F_ApiCopyMetrics FARGS((const F_MetricsT *frommetrics));
extern F_IntsT F_ApiCopyInts FARGS((const F_IntsT *fromints));
extern F_UIntsT F_ApiCopyUInts FARGS((const F_UIntsT *fromuints));
extern F_PointsT F_ApiCopyPoints FARGS((const F_PointsT *frompoints));
extern F_ElementCatalogEntriesT F_ApiCopyElementCatalogEntries
	FARGS((const F_ElementCatalogEntriesT *fromelementcatents));
extern F_TabT F_ApiCopyTab FARGS((const F_TabT *fromtab));
extern F_TabsT F_ApiCopyTabs FARGS((const F_TabsT *fromtabs));
extern F_AttributeDefT F_ApiCopyAttributeDef
	FARGS((const F_AttributeDefT *fromattributedef));
extern F_AttributeDefsT F_ApiCopyAttributeDefs
	FARGS((const F_AttributeDefsT *fromattributedefs));
extern F_AttributeT F_ApiCopyAttribute
	FARGS((const F_AttributeT *fromattribute));
extern F_AttributesT F_ApiCopyAttributes
	FARGS((const F_AttributesT *fromattributes));
extern F_TypedValT F_ApiCopyVal FARGS((const F_TypedValT *fromvalp));
extern F_TextItemT F_ApiCopyTextItem FARGS((const F_TextItemT *fromtip));
extern F_TextItemsT F_ApiAllocateTextItems FARGS((IntT numTextItems));
extern F_PropValT F_ApiCopyPropVal FARGS((const F_PropValT *frompvp));
extern F_PropValsT F_ApiCopyPropVals FARGS((const F_PropValsT *frompvp));
extern F_TextItemsT F_ApiCopyTextItems FARGS((const F_TextItemsT *fromip));
extern F_FontsT F_ApiCopyFonts FARGS((const F_FontsT *fromfonts));
extern F_CombinedFontsT F_ApiCopyCombinedFonts FARGS((const F_CombinedFontsT *fromfonts));

/*
 * This structure, rather than a StringT, is passed to F_ApiNotify()
 * if the API client is a "FileToFile" filter.
 * Do not modify or reallocate the contained strings.
 */
typedef struct F_FilterArgsT {
	ConStringT infile;
	ConStringT outfile;
	/*
	 * Following fields valid only if the notification was
	 * FA_Note_FilterFileToFile.
	 */
	ConStringT clname;		/* name by which client was registered */
	ConStringT informat;	/* InFormat, from client registration */
	ConStringT outformat;	/* OutFormat, from client registration */
	ConStringT binname;		/* filename of client/dll/plugin */
	/* reserved */
	F_StringsT args;
} F_FilterArgsT;

/* Dialogs for the API program */
extern IntT F_ApiAlert FARGS((ConStringT message, IntT type));
extern IntT F_ApiPromptInt FARGS((IntT *intp, ConStringT message,
	ConStringT stuffVal));
extern IntT F_ApiPromptMetric FARGS((MetricT *metricp, ConStringT message,
	ConStringT stuffVal, MetricT defaultunit));
extern IntT F_ApiPromptString FARGS((StringT *stringp, ConStringT message,
	ConStringT stuffVal));
extern IntT F_ApiFileScrollBox FARGS((StringT *stringp, ConStringT title,
	ConStringT directory));
extern IntT F_ApiScrollBox FARGS((IntT *selectedp, ConStringT title,
	const F_StringsT *list, IntT first));
extern IntT F_ApiChooseFile FARGS((StringT *stringp, ConStringT title,
	ConStringT directory, ConStringT stuffVal, IntT mode, ConStringT helpLink));
extern IntT F_ApiChooseFileEx FARGS((StringT *stringp, ConStringT title,
	ConStringT directory, ConStringT stuffVal, IntT mode, ConStringT helpLink,
	ConStringT formatStr));
extern IntT F_ApiChooseFileAndTag FARGS((StringT *valp, UIntT *selp, 
	ConStringT message, ConStringT directory, ConStringT stuffVal, IntT mode, 
	ConStringT helpLink, ConStringT tagLabel, const F_StringsT *listOfTags, 
	UIntT first));
extern F_PointT F_ApiPromptPoint FARGS((F_ObjHandleT docId,
	F_ObjHandleT frameId, MetricT snap));
extern F_RectT F_ApiPromptRect FARGS((F_ObjHandleT docId,
	F_ObjHandleT frameId, MetricT snap));

extern F_ObjHandleT F_ApiOpenResource FARGS((IntT objType, ConStringT name));
extern F_ObjHandleT F_ApiDialogItemId FARGS((F_ObjHandleT dlgId,
	IntT itemNum));
extern IntT F_ApiModelessDialog FARGS((IntT dlgNum, F_ObjHandleT dlgId));
extern IntT F_ApiModalDialog FARGS((IntT dlgNum, F_ObjHandleT dlgId));

/* Fcodes */
extern IntT F_ApiFcodes FARGS((IntT len, const IntT *vec));
	/* Send a vector of fcodes to FM */

/* Create/Find/Delete */
extern F_ObjHandleT F_ApiGetNamedObject FARGS((F_ObjHandleT docId,
	IntT objType, ConStringT name));
extern F_ObjHandleT F_ApiGetUniqueObject FARGS((F_ObjHandleT docId,
	IntT objType, IntT unique));
extern F_ObjHandleT F_ApiNewGraphicObject FARGS((F_ObjHandleT docId,
	IntT objType, F_ObjHandleT parentId));
extern F_ObjHandleT F_ApiNewNamedObject FARGS((F_ObjHandleT docId,
	IntT objType, ConStringT name));
extern F_ObjHandleT F_ApiNewAnchoredObject FARGS((F_ObjHandleT docId,
	IntT objType, const F_TextLocT *textLocp));
extern F_ObjHandleT F_ApiNewAnchoredFormattedObject FARGS((F_ObjHandleT docId,
	IntT objType, ConStringT format, const F_TextLocT *textLocp));
extern F_ObjHandleT F_ApiNewElement FARGS((F_ObjHandleT docId,
	F_ObjHandleT elemDefId, const F_TextLocT *textLocp));
extern F_ObjHandleT F_ApiNewElementInHierarchy FARGS((F_ObjHandleT docId,
	F_ObjHandleT elemDefId, const F_ElementLocT *elemLocp));
extern F_ObjHandleT F_ApiNewSubObject FARGS((F_ObjHandleT docId,
	F_ObjHandleT parentId, IntT propNum));
extern F_ObjHandleT F_ApiNewBookComponentInHierarchy FARGS((F_ObjHandleT bookId,
	ConStringT compName, const F_ElementLocT *elemLocp));
extern F_ObjHandleT F_ApiNewBookComponentOfTypeInHierarchy FARGS((F_ObjHandleT bookId,
	ConStringT compName, IntT compType, const F_ElementLocT *elemLocp));
extern VoidT F_ApiMoveComponent FARGS((F_ObjHandleT cdId, F_ObjHandleT compId, IntT moveAction));
extern StatusT F_ApiSilentOpenAllBookComponents FARGS((F_ObjHandleT bookId, 
					const F_StringsT *compList, const F_StringsT *openedCompList));
extern F_ObjHandleT F_ApiSilentOpenBookComponent FARGS((F_ObjHandleT bookId, F_ObjHandleT compId));
extern StatusT F_ApiApplyAttributeExpression FARGS((F_ObjHandleT docId, F_ObjHandleT objId));
extern StatusT F_ApiFixUpDitamapResourceManager FARGS((F_ObjHandleT docId));
extern StatusT F_ApiRepopulateDitamapResourceManager FARGS((F_ObjHandleT docId));
extern F_ObjHandleT F_ApiNewSeriesObject FARGS((F_ObjHandleT docId,
	IntT objType, F_ObjHandleT prevId));
extern IntT F_ApiDelete FARGS((F_ObjHandleT docId, F_ObjHandleT objId));
extern IntT F_ApiObjectValid FARGS((F_ObjHandleT docId, F_ObjHandleT objId));
extern F_TextLocT F_ApiAddText FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp, ConStringT text));
extern IntT F_ApiDeleteText FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep));


/* Table operations */
extern IntT F_ApiAddRows FARGS((F_ObjHandleT docId, F_ObjHandleT refRowId,
	IntT direction, IntT numNewRows));
extern IntT F_ApiAddCols FARGS((F_ObjHandleT docId, F_ObjHandleT tableId,
	IntT refColNum, IntT direction, IntT numNewCols));
extern IntT F_ApiDeleteRows FARGS((F_ObjHandleT docId, F_ObjHandleT tableId,
	F_ObjHandleT delRowId, IntT numDelRows));
extern IntT F_ApiDeleteCols FARGS((F_ObjHandleT docId, F_ObjHandleT tableId,
	IntT delColNum, IntT numDelCols));
extern IntT F_ApiMakeTblSelection FARGS((F_ObjHandleT docId,
	F_ObjHandleT tableId, IntT topRow, IntT bottomRow,
	IntT leftCol, IntT rightCol));
extern IntT F_ApiStraddleCells FARGS((F_ObjHandleT docId,
	F_ObjHandleT cellId, IntT heightInRows, IntT widthInCols));
extern IntT F_ApiUnStraddleCells FARGS((F_ObjHandleT docId,
	F_ObjHandleT cellId, IntT heightInRows, IntT widthInCols));
extern F_ObjHandleT F_ApiNewTable FARGS((F_ObjHandleT docId, ConStringT format,
	IntT numCols, IntT numBodyRows, IntT numHeaderRows, IntT numFooterRows,
	const F_TextLocT *textLocp));

/* Structure */
extern VoidT F_ApiMergeIntoFirst FARGS((F_ObjHandleT docId));
extern VoidT F_ApiMergeIntoLast FARGS((F_ObjHandleT docId));
extern VoidT F_ApiPromoteElement FARGS((F_ObjHandleT docId));
extern VoidT F_ApiDemoteElement FARGS((F_ObjHandleT docId));
extern VoidT F_ApiSplitElement FARGS((F_ObjHandleT docId));
extern VoidT F_ApiWrapElement FARGS((F_ObjHandleT docId, F_ObjHandleT edefId));
extern VoidT F_ApiUnWrapElement FARGS((F_ObjHandleT docId));
extern F_ElementRangeT F_ApiGetElementRange FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetElementRange FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_ElementRangeT *setVal));
extern F_TextLocT F_ApiElementLocToTextLoc FARGS((F_ObjHandleT docId,
	const F_ElementLocT *elocp));
extern F_ElementLocT F_ApiTextLocToElementLoc FARGS((F_ObjHandleT docId,
	const F_TextLocT *tlocp));

/* Property set/get */

#define F_ApiGetObjectType(docid,objectid) ((UIntT)(objectid)>>24)

#define TEXT_ELEMENTDEF_ID 0
#define F_ApiElementDefIsText(docid,objectid) ((objectid) == TEXT_ELEMENTDEF_ID)

extern IntT F_ApiGetInt FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum));
extern IntT F_ApiGetIntByName FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	ConStringT propName));
extern VoidT F_ApiSetInt FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, IntT setVal));
extern VoidT F_ApiSetIntByName FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	ConStringT propName, IntT setVal));

extern MetricT F_ApiGetMetric FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum));
extern MetricT F_ApiGetMetricByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, ConStringT propName));
extern VoidT F_ApiSetMetric FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, MetricT setVal));
extern VoidT F_ApiSetMetricByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, ConStringT propName, MetricT setVal));

extern StringT F_ApiGetString FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum));
extern VoidT F_ApiSetString FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, ConStringT setVal));

extern F_ObjHandleT F_ApiGetId FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum));
extern VoidT F_ApiSetId FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, F_ObjHandleT setVal));

extern F_StringsT F_ApiGetStrings FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetStrings FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, const F_StringsT *setVal));

extern F_TextLocT F_ApiGetTextLoc FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetTextLoc FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_TextLocT *setVal));

extern F_TextRangeT F_ApiGetTextRange FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetTextRange FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_TextRangeT *setVal));

extern F_MetricsT F_ApiGetMetrics FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetMetrics FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_MetricsT *setVal));

extern F_IntsT F_ApiGetInts FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetInts FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_IntsT *setVal));

extern F_PointsT F_ApiGetPoints FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));
extern VoidT F_ApiSetPoints FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum, const F_PointsT *setVal));

extern F_TabsT F_ApiGetTabs FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum));
extern VoidT F_ApiSetTabs FARGS((F_ObjHandleT docId, F_ObjHandleT objId,
	IntT propNum, const F_TabsT *setVal));

extern F_AttributeDefsT F_ApiGetAttributeDefs FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId));
extern VoidT F_ApiSetAttributeDefs FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, const F_AttributeDefsT *setVal));

extern F_AttributesT F_ApiGetAttributes FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId));
extern VoidT F_ApiSetAttributes FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, const F_AttributesT *setVal));
extern IntT F_ApiDeleteUndefinedAttribute FARGS((F_ObjHandleT docId,
	ConStringT attrName, IntT scope, F_ObjHandleT objId));

extern F_ElementCatalogEntriesT F_ApiGetElementCatalog
	FARGS((F_ObjHandleT docId));

extern F_TypedValT F_ApiGetVal FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId, IntT propNum));
extern F_PropValT F_ApiGetPropVal FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId, IntT propNum));
extern F_PropValsT F_ApiGetProps FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId));
extern VoidT F_ApiSetVal FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId, IntT propNum, const F_TypedValT *setVal));
extern VoidT F_ApiSetPropVal FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId, const F_PropValT *setVal));
extern VoidT F_ApiSetProps FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId, const F_PropValsT *setVal));

extern F_UBytesT F_ApiGetUBytesByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, ConStringT propName));
extern VoidT F_ApiSetUBytesByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, ConStringT propName, const F_UBytesT *setVal));

extern VoidT F_ApiDeletePropByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, ConStringT propName));

extern F_TypedValT F_ApiGetTextVal FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp, IntT propNum));
extern F_PropValT F_ApiGetTextPropVal FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp, IntT propNum));
extern F_PropValsT F_ApiGetTextProps FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp));
extern VoidT F_ApiSetTextVal FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRange, IntT propNum, const F_TypedValT *setVal));
extern VoidT F_ApiSetTextPropVal FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep, const F_PropValT *setVal));
extern VoidT F_ApiSetTextProps FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep, const F_PropValsT *setVal));

extern F_TextItemsT F_ApiGetText FARGS((F_ObjHandleT docId,
	F_ObjHandleT pgfId, UIntT flags));
extern F_TextItemsT F_ApiGetTextForRange FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep, UIntT flags));
extern F_TextItemsT F_ApiGetText2 FARGS((F_ObjHandleT docId,
	F_ObjHandleT pgfId, UIntT flags, UIntT flags2));
extern F_TextItemsT F_ApiGetTextForRange2 FARGS((F_ObjHandleT docId,
	const F_TextRangeT *textRangep, UIntT flags, UIntT flags2));

extern VoidT F_ApiPushClipboard FARGS((VoidT));
extern IntT F_ApiPopClipboard FARGS((VoidT));

extern IntT F_ApiSleep FARGS((IntT seconds));
extern IntT F_ApiUSleep FARGS((IntT microseconds));
extern IntT F_ApiUserCancel FARGS((VoidT));

#ifdef UNIX
extern IntT F_ApiSystemShell FARGS((ConStringT command));
extern IntT F_ApiForkAndExec
	FARGS((ConStringT execname, const F_StringsT *argv));
#endif

extern IntT F_ApiQuickSelect FARGS((F_ObjHandleT docId, ConStringT prompt,
	const F_StringsT *stringlist));

#ifdef WIN_FRAME
extern IntT F_ApiSetDdeInstance FARGS((UIntT ddeInstance));
extern StatusT F_ApiSetPrinterState FARGS((HANDLE hDevMode, HANDLE hDevNames));
extern StatusT F_ApiGetPrinterState FARGS((HANDLE *hDevMode,
	HANDLE *hDevNames));
#endif


#ifdef FAPI_4_BEHAVIOR

#define F_TypedValT_u u
#define F_PropIdentT_u u
#define F_StringsT_len len
#define F_StringsT_val val
#define F_UBytesT_len len
#define F_UBytesT_val val
#define F_UIntsT_len len
#define F_UIntsT_val val
#define F_IntsT_len len
#define F_IntsT_val val
#define F_MetricsT_len len
#define F_MetricsT_val val
#define F_AttributeDefsT_len len
#define F_AttributeDefsT_val val
#define F_AttributesT_len len
#define	F_AttributesT_val val
#define F_ElementCatalogEntriesT_len len
#define	F_ElementCatalogEntriesT_val val
#define F_TabsT_len len
#define	F_TabsT_val val
#define F_PointsT_len len
#define F_PointsT_val val
#define F_PropValsT_len len
#define F_PropValsT_val val
#define F_TextItemsT_len len
#define F_TextItemsT_val val

#define F_ApiPromptMetric oldF_ApiPromptMetric
#define F_ApiAllocatePropVals oldF_ApiAllocatePropVals
#define F_ApiCopyPropVals oldF_ApiCopyPropVals
#define F_ApiGetOpenDefaultParams oldF_ApiGetOpenDefaultParams
#define F_ApiGetSaveDefaultParams oldF_ApiGetSaveDefaultParams
#define F_ApiCompare oldF_ApiCompare
#define F_ApiAddText oldF_ApiAddText
#define F_ApiPromptPoint oldF_ApiPromptPoint
#define F_ApiPromptRect oldF_ApiPromptRect
#define F_ApiGetStrings oldF_ApiGetStrings
#define F_ApiGetTextLoc oldF_ApiGetTextLoc
#define F_ApiGetTextRange oldF_ApiGetTextRange
#define F_ApiGetMetrics oldF_ApiGetMetrics
#define F_ApiGetInts oldF_ApiGetInts
#define F_ApiGetPoints oldF_ApiGetPoints
#define F_ApiGetTabs oldF_ApiGetTabs
#define F_ApiGetElementCatalog oldF_ApiGetElementCatalog
#define F_ApiGetProps oldF_ApiGetProps
#define F_ApiGetUBytesByName oldF_ApiGetUBytesByName
#define F_ApiGetTextProps oldF_ApiGetTextProps
#define F_ApiGetText oldF_ApiGetText
#define F_ApiChooseFile oldF_ApiChooseFile
#endif /* FAPI_4_BEHAVIOR */

extern IntT oldF_ApiPromptMetric FARGS((MetricT *metricp,
	StringT message, StringT stuffVal));

extern F_PropValsT *oldF_ApiAllocatePropVals FARGS((IntT numProps));

extern F_PropValsT *oldF_ApiCopyPropVals FARGS((const F_PropValsT *frompvp));

extern F_PropValsT *oldF_ApiGetOpenDefaultParams FARGS((VoidT));

extern F_PropValsT *oldF_ApiGetSaveDefaultParams FARGS((VoidT));

extern F_CompareRetT *oldF_ApiCompare FARGS((F_ObjHandleT olderId,
	F_ObjHandleT newerId, IntT flags, StringT insertCondTag,
	StringT deleteCondTag, StringT replaceText, IntT compareThreshold));

extern F_TextLocT *oldF_ApiAddText FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp, StringT text));

extern F_PointT *oldF_ApiPromptPoint FARGS((F_ObjHandleT docId,
	F_ObjHandleT frameId, MetricT snap));

extern F_RectT *oldF_ApiPromptRect FARGS((F_ObjHandleT docId,
	F_ObjHandleT frameId, MetricT snap));

extern F_StringsT *oldF_ApiGetStrings FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_TextLocT *oldF_ApiGetTextLoc FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_TextRangeT *oldF_ApiGetTextRange FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_MetricsT *oldF_ApiGetMetrics FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_IntsT *oldF_ApiGetInts FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_PointsT *oldF_ApiGetPoints FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_TabsT *oldF_ApiGetTabs FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, IntT propNum));

extern F_ElementCatalogEntriesT *oldF_ApiGetElementCatalog
	FARGS((F_ObjHandleT docId));

extern F_PropValsT *oldF_ApiGetProps FARGS((F_ObjHandleT docId,
	F_ObjHandleT ObjId));

extern F_UBytesT *oldF_ApiGetUBytesByName FARGS((F_ObjHandleT docId,
	F_ObjHandleT objId, StringT propName));

extern F_PropValsT *oldF_ApiGetTextProps FARGS((F_ObjHandleT docId,
	const F_TextLocT *textLocp));

extern F_TextItemsT *oldF_ApiGetText FARGS((F_ObjHandleT docId,
	F_ObjHandleT pgfId, IntT flags));

extern IntT oldF_ApiChooseFile FARGS((StringT *stringp, StringT title,
	StringT directory, StringT stuffVal, IntT mode));

/* Support Double Byte Encodings */
extern BoolT F_ApiIsEncodingSupported FARGS((ConStringT encodingName));
extern F_StringsT F_ApiGetSupportedEncodings FARGS((VoidT));
extern StringT F_ApiGetEncodingForFamily FARGS((IntT family));
extern StringT F_ApiGetEncodingForFont FARGS((const F_FontT *font));

extern F_TextRangeT F_ApiFind FARGS((F_ObjHandleT docId,
									 const F_TextLocT *textLocp,
									 const F_PropValsT *findParamsp));

#ifdef __cplusplus
}
#endif

#endif /* FAPI_H */

