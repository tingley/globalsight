/****************
 * THIS FILE IS AN INSTALLED COPY OF
 * .\fm_rdr.h
 ****************/

/* DON'T EDIT THIS FILE DIRECTLY! */
#ifndef fm_rdr_h
#define fm_rdr_h
#ifdef __cplusplus
extern "C" {
#endif
/*<fm_rdr.c<*/
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

#include "fm_psr.h"

/*
 * SGML Parser Related Events:
 */
typedef enum {
	SR_EVT_BEGIN_READER = 1,
	SR_EVT_END_READER,
	SR_EVT_BEGIN_BOOK,
	SR_EVT_END_BOOK,
	SR_EVT_BEGIN_BOOK_COMP,
	SR_EVT_END_BOOK_COMP,
	SR_EVT_BEGIN_DOC,
	SR_EVT_END_DOC,
	SR_EVT_BEGIN_ELEM,
	SR_EVT_END_ELEM,
	SR_EVT_BEGIN_ENTITY,
	SR_EVT_END_ENTITY,
	SR_EVT_RE,
	SR_EVT_PI,
	SR_EVT_CDATA,
	SR_EVT_BEGIN_CDATASECTION,
	SR_EVT_END_CDATASECTION,
	SR_EVT_COMMENT,  /* Jackpot -s -e*/
	SR_EVT_MAX_VALUE
} SrEventTypeT;


typedef struct {
	StringT gi;			   /* SGML element name */
	StructuredAttrValsT sgmlAttrVals;  /* attribute values */
} SrTagT;


typedef struct {
	SrEventTypeT evtype;
	union {
		SrTagT tag;
		StringT cdata;
		StringT entname;
		StringT pi;
		StringT comment; /* Jackpot -s -e */
	} u;
} SrEventT;

typedef PtrT SrConvObjT;

/*
 * Reader Object Types
 */
typedef enum {
	SR_OBJ_UNDEFINED = 0,
	SR_OBJ_SESSION,
	SR_OBJ_BOOK,
	SR_OBJ_BOOK_COMP,
	SR_OBJ_DOC,
	SR_OBJ_ELEM,
	SR_OBJ_TABLE,
	SR_OBJ_TABLE_TITLE,
	SR_OBJ_TABLE_HEADING,
	SR_OBJ_TABLE_BODY,
	SR_OBJ_TABLE_FOOTING,
	SR_OBJ_TABLE_ROW,
	SR_OBJ_TABLE_CELL,
	SR_OBJ_COLSPEC,
	SR_OBJ_SPANSPEC,
	SR_OBJ_GRAPHIC,
	SR_OBJ_EQUATION,
	SR_OBJ_VARIABLE,
	SR_OBJ_MARKER,
	SR_OBJ_XREF,
	SR_OBJ_FOOTNOTE,
	SR_OBJ_TEXT,
	SR_OBJ_SPECIAL_CHAR,
	SR_OBJ_TEXT_INSET,
	SR_OBJ_REF_ELEM,
	SR_OBJ_RUBI_GROUP,
	SR_OBJ_RUBI,
	SR_OBJ_ENTITY,
	SR_OBJ_CDATASECTION,
	SR_OBJ_MAX_VALUE
} SrObjTypeT;


/*
 * Insertion location definition.
 */
typedef enum {
	SR_LOC_UNDEFINED = 0,
	SR_LOC_FLOW,
	SR_LOC_BOOK,
	SR_LOC_ELEMENT,
	SR_LOC_MARKER_TEXT,
	SR_LOC_TEXT_INSET,
	SR_LOC_CDATASECTION,
	SR_LOC_MAX_VALUE
} SrLocationT;

typedef struct {
	SrLocationT pos;
	union
	{
		F_ObjHandleT flowId;	/* pos == SR_LOC_FLOW */
		F_ObjHandleT mkrId;		/* pos == SR_LOC_MARKER_TEXT */
		F_ObjHandleT tiId;		/* pos == SR_LOC_TEXT_INSET */
		F_ElementLocT elemLoc;	/* pos == SR_LOC_ELEMENT */
	} u;
} SrInsertLocT;


/*
 * Line break flags
 */
#define SR_LB_SPACE			1
#define SR_LB_FORCED_RETURN	2
#define SR_LB_HARD_RETURN	SR_LB_FORCED_RETURN


/*
 * Reader session properties
 */
typedef struct
{
	BoolT	isBatchMode;		/* read-only */	
	StringT tableRulingStyle;	/* default style to use in tables */
	BoolT	overwriteFiles;		/* True/False */
} SrSessionPropsT;


extern IntT Sr_Convert FARGS((SrEventT *eventp, SrConvObjT srObj));

/*>fm_rdr.c>*/
/* --- */
/*<srevent.c<*/
extern SrwErrorT Sr_EventHandler FARGS((SrEventT *eventp,
	SrConvObjT srObj));
/*>srevent.c>*/
/*<sruser.c<*/
extern SrEventT *Sr_GetAssociatedEvent FARGS((SrConvObjT srObj));
extern PtrT Sr_GetPrivateData FARGS((SrConvObjT srObj));
extern VoidT Sr_SetPrivateData FARGS((SrConvObjT srObj, PtrT privDatap));
extern SrConvObjT Sr_GetCurConvObjOfType FARGS((SrObjTypeT objtype));
extern SrConvObjT Sr_GetCurConvObj FARGS((VoidT));
extern SrConvObjT Sr_GetParentConvObj FARGS((SrConvObjT srObj));
extern SrConvObjT Sr_GetChildConvObj FARGS((SrConvObjT srObj));
extern F_ObjHandleT Sr_GetTemplateDocId FARGS((VoidT));
extern FilePathT *Sr_GetExtEntityFilePath FARGS((StringT entname));
extern VoidT Sr_CancelOperation FARGS((VoidT));
extern VoidT Sr_CancelCurBatchFile FARGS((VoidT));
extern SrObjTypeT Sr_GetObjType FARGS((SrConvObjT srObj));
extern SrInsertLocT Sr_GetInsertLoc FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetInsertLoc FARGS((SrConvObjT srObj,
	SrInsertLocT *insertLoc));
extern StringT Sr_GetPrevSgmlGi FARGS((SrConvObjT srObj));
extern StringT Sr_GetPrevStructuredGi FARGS((SrConvObjT srObj));
extern F_ObjHandleT Sr_GetBookId FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetBookId FARGS((SrConvObjT srObj,
	F_ObjHandleT bookId));
extern FilePathT *Sr_GetBookFilePath FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetBookFilePath FARGS((SrConvObjT srObj,
	FilePathT *fp));
extern F_ObjHandleT Sr_GetDocId FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetDocId FARGS((SrConvObjT srObj,
	F_ObjHandleT docId));
extern F_ObjHandleT Sr_GetFlowId FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetFlowId FARGS((SrConvObjT srObj,
	F_ObjHandleT flowId));
extern FilePathT *Sr_GetBookCompFilePath FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetBookCompFilePath FARGS((SrConvObjT srObj,
	FilePathT *fp));
extern F_ObjHandleT Sr_GetFmElemId FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_UseFmElemId FARGS((SrConvObjT srObj,
	F_ObjHandleT elemId));
extern StringT Sr_GetFmElemTag FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetFmElemTag FARGS((SrConvObjT srObj,
	StringT fm_tag));
extern IntT Sr_GetProcessingFlags FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetProcessingFlags FARGS((SrConvObjT srObj,
	IntT flags));
extern F_AttributesT Sr_GetAttrVals FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetAttrVals FARGS((SrConvObjT srObj,
	F_AttributesT *attVals));
extern F_AttributeT Sr_GetAttrVal FARGS((SrConvObjT srObj,
	StringT fmAttrName));
extern SrwErrorT Sr_SetAttrVal FARGS((SrConvObjT srObj,
	F_AttributeT *attVal));
extern IntT Sr_GetLineBrkInfo FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetLineBrkInfo FARGS((SrConvObjT srObj,
	IntT lineBrkInfo));
extern StringT Sr_GetFmText FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetFmText FARGS((SrConvObjT srObj, StringT fmText));
extern SrwPropValsT Sr_GetPropVals FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetPropVals FARGS((SrConvObjT srObj,
	SrwPropValsT *propVals));
extern StringT Sr_GetPropVal FARGS((SrConvObjT srObj,
	SrwFmPropertyT prop));
extern SrwErrorT Sr_SetPropVal FARGS((SrConvObjT srObj,
	SrwFmPropertyT prop, StringT propVal));
extern F_ObjHandleT Sr_GetFmObjId FARGS((SrConvObjT srObj));
extern StringT Sr_GetInsertedTablePartElementName FARGS((
	SrConvObjT srObj, SrwTablePartTypeT part));
extern VoidT Sr_SetInsertedTablePartElementName FARGS((
	SrConvObjT srObj, SrwTablePartTypeT part, StringT name));
extern VoidT Sr_SetTableRowUsed FARGS((F_ObjHandleT docId,
	F_ObjHandleT rowId));
extern BoolT Sr_RowInUse FARGS((F_ObjHandleT docId, F_ObjHandleT rowId));
extern SrwErrorT Sr_AddTableRows FARGS((F_ObjHandleT docId,
	F_ObjHandleT refRowId, IntT direction, IntT cnt));
extern VoidT Sr_SetTableCellUsed FARGS((F_ObjHandleT docId,
	F_ObjHandleT cellId));
extern BoolT Sr_CellInUse FARGS((F_ObjHandleT docId,
	F_ObjHandleT cellId));
extern VoidT Sr_SetTableCellStartsNewRow FARGS((SrConvObjT srObj,
	BoolT startNewRow));
extern BoolT Sr_GetTableCellStartsNewRow FARGS((SrConvObjT srObj));
extern StringT Sr_GetVariableName FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetVariableName FARGS((SrConvObjT srObj,
	StringT varName));
extern UCharT Sr_GetFmChar FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetFmChar FARGS((SrConvObjT srObj,
	PUCharT fm_charcode));
extern StringT Sr_GetCharFmt FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetCharFmt FARGS((SrConvObjT srObj,
	StringT fm_chartag));
extern SrwColSpecsT Sr_GetColSpecs FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetColSpecs FARGS((SrConvObjT srObj,
	SrwColSpecsT *listp));
extern SrwColSpecT Sr_GetCurColSpecByName FARGS((StringT name));
extern SrwColSpecT Sr_GetCurColSpecByColNum FARGS((IntT colnum));
extern SrwSpanSpecsT Sr_GetSpanSpecs FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetSpanSpecs FARGS((SrConvObjT srObj,
	SrwSpanSpecsT *listp));
extern SrwSpanSpecT Sr_GetCurSpanSpecByName FARGS((StringT name));
extern SrwStraddlesT Sr_GetStraddles FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetStraddles FARGS((SrConvObjT srObj,
	SrwStraddlesT *listp));
extern StringT Sr_GetRefElemTag FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetRefElemTag FARGS((SrConvObjT srObj,
	StringT fm_reftag));
extern FilePathT *Sr_GetTextInsetFilePath FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetTextInsetFilePath FARGS((SrConvObjT srObj,
	FilePathT *fp));
extern IntT Sr_GetTextInsetPageSpace FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetTextInsetPageSpace FARGS((SrConvObjT srObj,
	IntT pageSpace));
extern IntT Sr_GetTextInsetFormatting FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetTextInsetFormatting FARGS((SrConvObjT srObj,
	IntT tiFmt));
extern StringT Sr_GetTextInsetFlowTag FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetTextInsetFlowTag FARGS((SrConvObjT srObj,
	StringT flowTag));
extern StringT Sr_GetImportFileHint FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetImportFileHint FARGS((SrConvObjT srObj,
	StringT fileHint));
extern SrSessionPropsT Sr_GetSessionProps FARGS((SrConvObjT srObj));
extern SrwErrorT Sr_SetSessionProps FARGS((SrConvObjT srObj,
	SrSessionPropsT *sProps));
extern VoidT Sr_DeallocateSessionProps FARGS((SrSessionPropsT *sProps));
/*>sruser.c>*/
#ifdef __cplusplus /* end */
}
#endif
#endif /* fm_rdr_h */

/* Installed: .\fm_rdr.h */
