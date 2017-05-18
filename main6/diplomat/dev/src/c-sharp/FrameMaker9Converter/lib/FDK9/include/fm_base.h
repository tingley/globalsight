/****************
 * THIS FILE IS AN INSTALLED COPY OF
 * .\fm_base.h
 ****************/

/* DON'T EDIT THIS FILE DIRECTLY! */
#ifndef fm_base_h
#define fm_base_h
#ifdef __cplusplus
extern "C" {
#endif
/*<fm_base.c<*/
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

#include "fdetypes.h"
#include "fapi.h"

/*
 * Error code data type.
 */

typedef IntT SrwErrorT;
extern SrwErrorT SRW_errno;


/*
 * FM properties which can be bound to attributes.
 */

typedef enum {

	SRW_PROP_UNDEFINED = 0,

	/* table properties */
	SRW_PROP_COLUMNS,
	SRW_PROP_COLUMN_WIDTHS,
	SRW_PROP_TABLE_FORMAT,
	SRW_PROP_COLUMN_FORMATS,
	SRW_PROP_PGF_FORMAT,
	SRW_PROP_MINIMUM_HEIGHT,
	SRW_PROP_MAXIMUM_HEIGHT,
	SRW_PROP_HSTRAD,
	SRW_PROP_VSTRAD,
	SRW_PROP_MORE_ROWS,
	SRW_PROP_TABLE_BORDER,
	SRW_PROP_ROW_TYPE,
	SRW_PROP_VSTRAD_START,
	SRW_PROP_VSTRAD_END_AT,
	SRW_PROP_VSTRAD_END_BEFORE,
	SRW_PROP_PAGE_WIDE,
	SRW_PROP_ROTATE,
	SRW_PROP_INSERT_TITLE,
	SRW_PROP_INSERT_HEADING,
	SRW_PROP_INSERT_FOOTING,

	/* colspec/spanspec properties */
	SRW_PROP_COL_NAME,
	SRW_PROP_COL_NUM,
	SRW_PROP_COL_WIDTH,
	SRW_PROP_SPAN_NAME,
	SRW_PROP_NAME_START,
	SRW_PROP_NAME_END,
	SRW_PROP_COL_RULING,
	SRW_PROP_ROW_RULING,
	SRW_PROP_ALIGN_TYPE, 
	SRW_PROP_ALIGN_CHAR,
	SRW_PROP_ALIGN_OFFSET,

	/* graphics properties */
	SRW_PROP_ENTITY,
	SRW_PROP_FILE,
	SRW_PROP_DPI,
	SRW_PROP_IMPORT_SIZE,
	SRW_PROP_REF_OR_COPY,
	SRW_PROP_SIDEWAYS,
	SRW_PROP_IMPORT_ANGLE,
	SRW_PROP_HOR_OFFSET,
	SRW_PROP_VER_OFFSET,
	SRW_PROP_POSITION,
	SRW_PROP_ALIGNMENT,
	SRW_PROP_CROPPED,
	SRW_PROP_FLOATING,
	SRW_PROP_WIDTH,
	SRW_PROP_HEIGHT,
	SRW_PROP_ANGLE,
	SRW_PROP_BLOFFSET,
	SRW_PROP_NSOFFSET,
	SRW_PROP_ALT_TEXT,
	SRW_PROP_RASTERDPI,
	SRW_PROP_INSETDATA,

	/*XML Export graphic properties */
	SRW_PROP_XML_LINK,
	SRW_PROP_XML_HREF,
	SRW_PROP_XML_SHOW,
	SRW_PROP_XML_ACTUATE,

	/* cross reference properties */
	SRW_PROP_XREF_ID,
	SRW_PROP_XREF_FORMAT,
	/* MoonLight -s */
	SRW_PROP_XREF_SRCFILE,
	/* MoonLight -e */

	/* marker properties */
	SRW_PROP_MARKER_TYPE,
	SRW_PROP_MARKER_TEXT,

	SRW_PROP_MAX_VALUE		/* max value for error checking */

} SrwFmPropertyT;


/*
 * FM property values which can be bound to attribute values
 */
typedef enum {
	/* Aframe align */
	SRW_PVAL_UNDEFINED = 0,
	SRW_PVAL_ALEFT,
	SRW_PVAL_ARIGHT,
	SRW_PVAL_ACENTER,
	SRW_PVAL_AINSIDE,
	SRW_PVAL_AOUTSIDE,
	/* Aframe position */
	SRW_PVAL_INLINE,
	SRW_PVAL_TOP,
	SRW_PVAL_BELOW,
	SRW_PVAL_BOTTOM,
	SRW_PVAL_SC_LEFT,
	SRW_PVAL_SC_RIGHT,
	SRW_PVAL_SC_NEAREST,
	SRW_PVAL_SC_FARTHEST,
	SRW_PVAL_SC_INSIDE,
	SRW_PVAL_SC_OUTSIDE,
	SRW_PVAL_TF_LEFT,
	SRW_PVAL_TF_RIGHT,
	SRW_PVAL_TF_NEAREST,
	SRW_PVAL_TF_FARTHEST,
	SRW_PVAL_TF_INSIDE,
	SRW_PVAL_TF_OUTSIDE,
	SRW_PVAL_RUN_INTO_PGF,
	/* Import by ref/copy */
	SRW_PVAL_REFERENCE,
	SRW_PVAL_COPY,
	/* Table border ruling */
	SRW_PVAL_TOPBOT,
	SRW_PVAL_SIDES,
	SRW_PVAL_LEFT,
	SRW_PVAL_RIGHT,
	SRW_PVAL_ALL,
	SRW_PVAL_NONE,
	/* Table row type */
	SRW_PVAL_HEADING,
	SRW_PVAL_BODY,
	SRW_PVAL_FOOTING,
	SRW_PVAL_MAX_VALUE		/* max value for error checking */
} SrwFmPropValT;


/*
 * Size specification units for Structured export
 */
typedef enum {
	SRW_SZ_DPI = 1,
	SRW_SZ_CC,
	SRW_SZ_CM,
	SRW_SZ_DD,
	SRW_SZ_IN,
	SRW_SZ_MM,
	SRW_SZ_PI,
	SRW_SZ_PT,
	SRW_SZ_MAX_VALUE		/* max value for error checking */
} SrwGfxSizeSpecT;

/*
 * element processing flags
 */
#define SRW_UNWRAP_ELEMENT			0x01
#define SRW_DROP_ELEMENT_CONTENT	0x02

typedef enum {
	SRW_TABLE_UNDEFINED = 0,
	SRW_TABLE_TITLE,
	SRW_TABLE_HEADING,
	SRW_TABLE_BODY,
	SRW_TABLE_FOOTING,
	SRW_TABLE_PART_MAX_VALUE
} SrwTablePartTypeT;

#define SRW_TABLE_NUM_PARTS 4

/*
 * Open straddles.
 */
typedef struct {
	F_ObjHandleT  cellId;
	StringT       straddleName;
} SrwStraddleT;

typedef struct {
	IntT          len;
	SrwStraddleT *val;
} SrwStraddlesT;

/*
 * Colspecs and spanspecs.
 */
typedef struct {
	IntT          colnum;
	StringT       colname;
	StringT       colwidth;
	StringT 	  alignType;
	StringT       alignChar;
	StringT       alignOffset;
	BoolT         colsep;
	BoolT         rowsep;
	/* Not all fields in a colspec have assigned values */
	UIntT		  valueSet;
#define SRW_COLSPEC_COLNUM		(1<<0)
#define SRW_COLSPEC_COLNAME		(1<<1)
#define SRW_COLSPEC_COLWIDTH	(1<<2)
#define SRW_COLSPEC_ALIGN		(1<<3)
#define SRW_COLSPEC_CHAROFF		(1<<4)
#define SRW_COLSPEC_CHAR		(1<<5)
#define SRW_COLSPEC_COLSEP		(1<<6)
#define SRW_COLSPEC_ROWSEP		(1<<7)
} SrwColSpecT;

typedef struct {
	IntT          len;
	SrwColSpecT  *val;
} SrwColSpecsT;

typedef struct {
	StringT       spanname;
	StringT	      namest;
	StringT	      nameend;
	StringT 	  alignType;
	StringT       alignChar;
	StringT	      alignOffset;
	BoolT         colsep;
	BoolT         rowsep;
	/* Not all fields in a spanspec have assigned values */
	UIntT		  valueSet;
#define SRW_SPANSPEC_SPANNAME	(1<<0)
#define SRW_SPANSPEC_NAMEST		(1<<1)
#define SRW_SPANSPEC_NAMEEND	(1<<2)
#define SRW_SPANSPEC_ALIGN    	(1<<3)
#define SRW_SPANSPEC_CHAROFF	(1<<4)
#define SRW_SPANSPEC_CHAR		(1<<5)
#define SRW_SPANSPEC_COLSEP		(1<<6)
#define SRW_SPANSPEC_ROWSEP		(1<<7)
} SrwSpanSpecT;

typedef struct {
	IntT		   len;
	SrwSpanSpecT  *val;
} SrwSpanSpecsT;

/*
 * Error codes common to reader/writer
 */

enum SrwErrors {
	SRW_E_SUCCESS = 0,
	SRW_E_FAILURE,					/* Failure - generic code */
	SRW_E_INVALID_CONV_OBJ,			/* Invaid object pointer */
	SRW_E_WRONG_OBJ_TYPE,			/* Wrong object type */
	SRW_E_OBJ_HAS_NO_SUCH_PROP,		/* No such property for obj */
	SRW_E_NO_SUCH_ATTR,				/* No such attribute for obj */
	SRW_E_NOT_CUR_DOC_ID,			/* Wrong document ID */
	SRW_E_BAD_OBJ_HANDLE,
	SRW_E_NO_TEMPLATE,
	SRW_E_BAD_VALUE,
	SRW_E_NOT_BOOK_COMP,
	SRW_E_INVALID_TEXT_ITEM_TYPE,
	SRW_E_BAD_ROW_SCAN_ORDER,
	SRW_E_WRITE_INST,
	SRW_E_WRITE_DTDS,
	SRW_E_NOT_ENOUGH_MEM              /* we need this for the Docbook client */
};

/*
 * FM Property specification.
 */
typedef struct {
	SrwFmPropertyT prop;
	StringT value;
} SrwPropValT;

typedef struct {
	UIntT len;
	SrwPropValT *val;
} SrwPropValsT;

/*
 * Enumerated Types for Writing to the Log File
 */

typedef enum {
	SRW_LOGD_FILEPATH = 1,
	SRW_LOGD_ID,
	SRW_LOGD_MAX_VALUE
} SrwLogDocT;

typedef enum {
	SRW_LOGL_NONE = 1,
	SRW_LOGL_ID,
	SRW_LOGL_LINE,
	SRW_LOGL_MAX_VALUE
} SrwLogLocT;

typedef struct {
	SrwLogDocT docIdentType;
	union {
		FilePathT *fp;
		F_ObjHandleT docId;
	} doc_u;

	SrwLogLocT locType;
	union {
		F_ObjHandleT targetId;
		IntT line;
	} loc_u;
} SrwLogMessageLocationT;

/*>fm_base.c>*/
/* --- */
/*<srwuser.c<*/
extern IntT SRW_errno;
extern FilePathT *Srw_GetSgmlDocFilePath FARGS((VoidT));
extern FilePathT *Srw_GetStructuredDocFilePath FARGS((VoidT));
extern F_ObjHandleT Srw_GetMainDocBookId FARGS((VoidT));
extern VoidT Srw_DeallocatePropVals FARGS((SrwPropValsT *propVals));
extern VoidT Srw_DeallocatePropVal FARGS((SrwPropValT *propVal));
extern SrwPropValsT Srw_CopyPropVals FARGS((SrwPropValsT *propVals));
extern SrwPropValT Srw_CopyPropVal FARGS((SrwPropValT *propVal));
extern VoidT Srw_LogMessage FARGS((SrwLogMessageLocationT *location,
	StringT message));
extern FilePathT *Srw_GetRulesDocFilePath FARGS((VoidT));
extern FilePathT *Srw_GetImportTemplateFilePath FARGS((VoidT));
extern FilePathT *Srw_GetExportDtdFilePath FARGS((VoidT));
extern FilePathT *Srw_GetExportSchemaFilePath FARGS((VoidT));
extern FilePathT *Srw_GetSgmlDeclarationFilePath FARGS((VoidT));
extern FilePathT *Srw_GetStructuredDeclarationFilePath FARGS((VoidT));
extern SrwColSpecT Srw_GetColSpecByName FARGS((SrwColSpecsT *listp,
	StringT name));
extern SrwColSpecT Srw_GetColSpecByColNum FARGS((SrwColSpecsT *listp,
	IntT colnum));
extern SrwErrorT Srw_SetColSpec FARGS((SrwColSpecsT *listp,
	SrwColSpecT *colspecp));
extern SrwColSpecT Srw_CopyColSpec FARGS((SrwColSpecT *colspecp));
extern SrwColSpecsT Srw_CopyColSpecs FARGS((SrwColSpecsT *listp));
extern VoidT Srw_DeallocateColSpecs FARGS((SrwColSpecsT *listp));
extern VoidT Srw_DeallocateColSpec FARGS((SrwColSpecT *colspecp));
extern SrwErrorT Srw_SetSpanSpec FARGS((SrwSpanSpecsT *listp,
	SrwSpanSpecT *spanspecp));
extern SrwSpanSpecsT Srw_CopySpanSpecs FARGS((SrwSpanSpecsT *listp));
extern VoidT Srw_DeallocateSpanSpecs FARGS((SrwSpanSpecsT *listp));
extern VoidT Srw_DeallocateSpanSpec FARGS((SrwSpanSpecT *spanspecp));
extern SrwSpanSpecT Srw_GetSpanSpecByName FARGS((SrwSpanSpecsT *listp,
	StringT name));
extern SrwSpanSpecT Srw_CopySpanSpec FARGS((SrwSpanSpecT *spanspecp));
extern SrwErrorT Srw_SetStraddle FARGS((SrwStraddlesT *listp,
	SrwStraddleT *stradp));
extern VoidT Srw_DeallocateStraddles FARGS((SrwStraddlesT *listp));
extern SrwStraddlesT Srw_CopyStraddles FARGS((SrwStraddlesT *listp));
extern SrwStraddleT Srw_CopyStraddle FARGS((SrwStraddleT *stradp));
extern VoidT Srw_DeallocateStraddle FARGS((SrwStraddleT *stradp));
extern VoidT Srw_DeleteStraddlesByName FARGS((SrwStraddlesT *listp,
	StringT name));
extern StringT Srw_ConvertTextToUTF8 FARGS((ConStringT text));
/*>srwuser.c>*/
#ifdef __cplusplus /* end */
}
#endif
#endif /* fm_base_h */

/* Installed: .\fm_base.h */
