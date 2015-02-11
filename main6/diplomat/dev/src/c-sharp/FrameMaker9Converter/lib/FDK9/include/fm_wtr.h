/****************
 * THIS FILE IS AN INSTALLED COPY OF
 * .\fm_wtr.h
 ****************/

/* DON'T EDIT THIS FILE DIRECTLY! */
#ifndef fm_wtr_h
#define fm_wtr_h
#ifdef __cplusplus
extern "C" {
#endif
/*<fm_wtr.c<*/
/*************************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
* Copyright 1986 - 2005 Adobe Systems Incorporated
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

#include "fm_base.h"

/*
 * SGML Writer Related Events:
 */

typedef enum {
	SW_EVT_UNDEFINED = 0,
	SW_EVT_BEGIN_WRITER,
	SW_EVT_END_WRITER,
	SW_EVT_BEGIN_BOOK,
	SW_EVT_END_BOOK,
	SW_EVT_BEGIN_BOOK_COMP,
	SW_EVT_END_BOOK_COMP,
	SW_EVT_BEGIN_DOC,
	SW_EVT_END_DOC,
	SW_EVT_BEGIN_ELEM,
	SW_EVT_END_ELEM,
	SW_EVT_BEGIN_FOOTNOTE,
	SW_EVT_END_FOOTNOTE,
	SW_EVT_BEGIN_TABLE,
	SW_EVT_END_TABLE,
	SW_EVT_BEGIN_TABLE_TITLE,
	SW_EVT_END_TABLE_TITLE,
	SW_EVT_BEGIN_TABLE_HEADING,
	SW_EVT_END_TABLE_HEADING,
	SW_EVT_BEGIN_TABLE_BODY,
	SW_EVT_END_TABLE_BODY,
	SW_EVT_BEGIN_TABLE_FOOTING,
	SW_EVT_END_TABLE_FOOTING,
	SW_EVT_BEGIN_TABLE_ROW,
	SW_EVT_END_TABLE_ROW,
	SW_EVT_BEGIN_TABLE_CELL,
	SW_EVT_END_TABLE_CELL,
	SW_EVT_BEGIN_COLSPEC,
	SW_EVT_END_COLSPEC,
	SW_EVT_BEGIN_RUBI_GROUP,
	SW_EVT_END_RUBI_GROUP,
	SW_EVT_BEGIN_RUBI,
	SW_EVT_END_RUBI,
	SW_EVT_VARIABLE,
	SW_EVT_MARKER,
	SW_EVT_XREF,
	SW_EVT_GRAPHIC,
	SW_EVT_EQUATION,
	SW_EVT_TEXT,
	SW_EVT_SPECIAL_CHAR,
	SW_EVT_TEXT_INSET,
	SW_EVT_REF_ELEM,
	SW_EVT_EOL,				/* end of line */
	SW_EVT_EOP,				/* end of para */
	SW_EVT_CONDITION_CHANGE,
	SW_EVT_BEGIN_CDATASECTION,
	SW_EVT_END_CDATASECTION,
	SW_EVT_MAX_VALUE
} SwEventTypeT;


typedef struct {
	SwEventTypeT evtype;
	F_TextLocT txtloc;			/* location in fm doc */
	F_ObjHandleT fm_elemid;		/* element id */
	F_AttributesT fm_attrs;
	F_ObjHandleT fm_objid;		/* table | marker | variable | fn etc. */
	StringT text;
	UIntT charcode;			/* for special characters */
	StringT chartag;
} SwEventT;


typedef PtrT SwConvObjT; /* Writer conversion object. */


/*
 * Sgml Writer Object Types.
 */
typedef enum {
	SW_OBJ_UNDEFINED = 0,
	SW_OBJ_SESSION,
	SW_OBJ_BOOK,
	SW_OBJ_BOOK_COMP,
	SW_OBJ_DOC,
	SW_OBJ_ELEM,
	SW_OBJ_TABLE,
	SW_OBJ_TABLE_TITLE,
	SW_OBJ_TABLE_HEADING,
	SW_OBJ_TABLE_BODY,
	SW_OBJ_TABLE_FOOTING,
	SW_OBJ_TABLE_ROW,
	SW_OBJ_TABLE_CELL,
	SW_OBJ_COLSPEC,
	SW_OBJ_GRAPHIC,
	SW_OBJ_EQUATION,
	SW_OBJ_VARIABLE,
	SW_OBJ_MARKER,
	SW_OBJ_XREF,
	SW_OBJ_FOOTNOTE,
	SW_OBJ_TEXT,
	SW_OBJ_ENTITY,
	SW_OBJ_PI,
	SW_OBJ_RUBI_GROUP,
	SW_OBJ_RUBI,
	SW_OBJ_RE,	/* SGML Record End (RE) */
	SW_OBJ_UNKNOWNCHAR,
	SW_OBJ_CDATASECTION,
	SW_OBJ_COMMENT, /* Jackpot -s -e */
	SW_OBJ_MAX_VALUE
} SwObjTypeT;


/*
 * Information pertaining to the beginning of an SGML document.
 */
typedef struct {
	BoolT includeDtd;
	BoolT includeSgmlDecl;
	StringT doctypeSysId;
	StringT doctypePubId;
	BoolT overwriteFiles;
} SwSessionPropsT;


/*
 * Types of data items representing a sequence of FM text
 * characters.
 */
typedef enum {
	SW_TEXT_STRING = 1,
	SW_TEXT_NUMERIC_CHAR_REF,
	SW_TEXT_NAMED_CHAR_REF,
	SW_TEXT_ENT_REF,
	SW_TEXT_MAX_VALUE
} SwTextItemTypeT;

/*
 * Array of data items (strings and character references) for
 * character data.
 */
typedef struct {
	SwTextItemTypeT itemType;
	union {
		StringT text;
		UCharT charNum;
		StringT charName;
		StringT entName;
	} u;
} SwTextItemT;

typedef struct {
	UIntT len;
	SwTextItemT *val;
} SwTextItemsT;


/*
 * SGML Writer location
 */
typedef enum {
	SW_LOC_DTD_SUBSET = 1,
	SW_LOC_INSTANCE,
	SW_LOC_MAX_VALUE
} SwLocationT;


extern IntT Sw_Convert FARGS((SwEventT *eventp, SwConvObjT swObj));

/*>fm_wtr.c>*/
/* --- */
/*<swbook.c<*/
extern StringT Sw_GetBookPi FARGS((SwConvObjT swObj));
extern ErrorT Sw_SetBookPi FARGS((SwConvObjT swObj, StringT bookPi));
/*>swbook.c>*/
/*<swdoc.c<*/
extern F_ObjHandleT Sw_GetDocId FARGS((SwConvObjT swObj));
extern FilePathT *Sw_GetBookCompEntityFilePath FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetBookCompEntityFilePath FARGS((SwConvObjT swObj,
	FilePathT *fpEntity));
extern StringT Sw_GetBookCompPi FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetBookCompPi FARGS((SwConvObjT swObj,
	StringT bookCompPi));
/*>swdoc.c>*/
/*<swelem.c<*/
extern StringT Sw_GetSgmlGi FARGS((SwConvObjT swObj));
extern StringT Sw_GetStructuredGi FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetSgmlGi FARGS((SwConvObjT swObj, StringT gi));
extern SrwErrorT Sw_SetStructuredGi FARGS((SwConvObjT swObj,
	StringT gi));
extern IntT Sw_GetProcessingFlags FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetProcessingFlags FARGS((SwConvObjT swObj,
	IntT flags));
extern StructuredAttrValsT Sw_GetAttrVals FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetAttrVals FARGS((SwConvObjT swObj,
	StructuredAttrValsT *attVals));
extern StructuredAttrValT Sw_GetAttrVal FARGS((SwConvObjT swObj,
	StringT sgmlAttrName));
extern SrwErrorT Sw_SetAttrVal FARGS((SwConvObjT swObj,
	StructuredAttrValT *attVal));
/*>swelem.c>*/
/*<swent.c<*/
extern BoolT Sw_IsGeneralEntityNameUsed FARGS((StringT ename));
extern BoolT Sw_IsGeneralEntityDefined FARGS((StringT ename));
extern StringT Sw_GetEntityName FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetEntityName FARGS((SwConvObjT swObj,
	StringT entName));
extern StringT Sw_GetPI FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetPI FARGS((SwConvObjT swObj, StringT pi));
/*>swent.c>*/
/*<swevent.c<*/
extern SrwErrorT Sw_EventHandler FARGS((SwEventT *eventp,
	SwConvObjT swObj));
/*>swevent.c>*/
/*<swglob.c<*/
extern SwEventT *Sw_GetAssociatedEvent FARGS((SwConvObjT swObj));
extern PtrT Sw_GetPrivateData FARGS((SwConvObjT swObj));
extern VoidT Sw_SetPrivateData FARGS((SwConvObjT swObj, PtrT privDatap));
extern SwConvObjT Sw_GetCurConvObjOfType FARGS((SwObjTypeT objtype));
extern SwConvObjT Sw_GetCurConvObj FARGS((VoidT));
extern SwConvObjT Sw_GetParentConvObj FARGS((SwConvObjT convObj));
extern SwConvObjT Sw_GetChildConvObj FARGS((SwConvObjT convObj));
extern VoidT Sw_CancelOperation FARGS((VoidT));
extern VoidT Sw_CancelCurBatchFile FARGS((VoidT));
extern SwObjTypeT Sw_GetObjType FARGS((SwConvObjT swObj));
/*>swglob.c>*/
/*<swgraph.c<*/
extern StringT Sw_GetGfxEntityName FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxEntityName FARGS((SwConvObjT swObj,
	StringT ename));
extern StringT Sw_GetGfxPubId FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxPubId FARGS((SwConvObjT swObj, StringT pubid));
extern StringT Sw_GetGfxSysId FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxSysId FARGS((SwConvObjT swObj, StringT sysid));
extern StructuredEntityTypeT Sw_GetGfxEntityType FARGS((
	SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxEntityType FARGS((SwConvObjT swObj,
	StructuredEntityTypeT type));
extern StringT Sw_GetGfxNotation FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxNotation FARGS((SwConvObjT swObj,
	StringT nname));
extern StructuredAttrValsT Sw_GetGfxDataAttrVals FARGS((
	SwConvObjT swObj));
extern SrwErrorT Sw_SetGfxDataAttrVals FARGS((SwConvObjT swObj,
	StructuredAttrValsT *attVals));
extern StringT Sw_GetExportFileFormat FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetExportFileFormat FARGS((SwConvObjT swObj,
	StringT format));
extern FilePathT *Sw_GetExportFilePath FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetExportFilePath FARGS((SwConvObjT swObj,
	FilePathT *fp));
/*>swgraph.c>*/
/*<swnotify.c<*/
extern VoidT Sw_NotifyStartTag FARGS((StringT gi));
extern VoidT Sw_NotifyEndTag FARGS((StringT gi));
extern VoidT Sw_NotifyGeneralEntityDef FARGS((StringT ename));
/*>swnotify.c>*/
/*<swobj.c<*/
extern SrwPropValsT Sw_GetPropVals FARGS((SwConvObjT swObj));
extern StringT Sw_GetPropVal FARGS((SwConvObjT swObj,
	SrwFmPropertyT fm_prop));
/*>swobj.c>*/
/*<swscan.c<*/
extern SrwErrorT Sw_ScanElem FARGS((F_ObjHandleT docId,
	F_ObjHandleT elemId));
/*>swscan.c>*/
/*<swsessn.c<*/
extern SwSessionPropsT Sw_GetSessionProps FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetSessionProps FARGS((SwConvObjT swObj,
	SwSessionPropsT *sProps));
extern VoidT Sw_DeallocateSessionProps FARGS((SwSessionPropsT *sProps));
extern BoolT Sw_IsExportingToXml FARGS((VoidT));
/*>swsessn.c>*/
/*<swsgml.c<*/
extern VoidT Sw_WriteDelimiter FARGS((SwLocationT loc,
	StructuredDelimiterTypeT deltype));
extern VoidT Sw_WriteReservedName FARGS((SwLocationT loc,
	StructuredReservedNameT rn));
extern VoidT Sw_WriteAttrSpec FARGS((SwLocationT loc, StringT attrName,
	StringT attrVal));
extern VoidT Sw_WriteString FARGS((SwLocationT loc, ConStringT s));
/*>swsgml.c>*/
/*<swtbl.c<*/
extern SrwErrorT Sw_SetTableScanOrder FARGS((SwConvObjT swObj,
	SrwTablePartTypeT *scanOrder));
extern SrwColSpecsT Sw_GetColSpecs FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetColSpecs FARGS((SwConvObjT swObj,
	SrwColSpecsT *listp));
/*>swtbl.c>*/
/*<swtext.c<*/
extern SwTextItemsT Sw_GetSgmlText FARGS((SwConvObjT swObj));
extern SwTextItemsT Sw_GetStructuredText FARGS((SwConvObjT swObj));
extern SrwErrorT Sw_SetSgmlText FARGS((SwConvObjT swObj,
	SwTextItemsT *itemListp));
extern SrwErrorT Sw_SetStructuredText FARGS((SwConvObjT swObj,
	SwTextItemsT *itemListp));
extern VoidT Sw_DeallocateTextItems FARGS((SwTextItemsT *textp));
/*>swtext.c>*/
#ifdef __cplusplus /* end */
}
#endif
#endif /* fm_wtr_h */

/* Installed: .\fm_wtr.h */
