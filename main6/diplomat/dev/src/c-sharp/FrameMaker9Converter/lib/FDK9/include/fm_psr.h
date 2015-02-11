/****************
 * THIS FILE IS AN INSTALLED COPY OF
 * .\fm_psr.h
 ****************/

/* DON'T EDIT THIS FILE DIRECTLY! */
#ifndef fm_psr_h
#define fm_psr_h
#ifdef __cplusplus
extern "C" {
#endif
/*<sgml_psr.c<*/
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

#include "fm_base.h"

/*
 * Quantity limits defined in the SGML standard.
 */
typedef enum {
	STRUCTURED_QTY_ATTCNT = 1,
	STRUCTURED_QTY_ATTSPLEN,
	STRUCTURED_QTY_BSEQLEN,
	STRUCTURED_QTY_DTAGLEN,
	STRUCTURED_QTY_DTEMPLEN,
	STRUCTURED_QTY_ENTLVL,
	STRUCTURED_QTY_GRPCNT,
	STRUCTURED_QTY_GRPGTCNT,
	STRUCTURED_QTY_GRPLVL,
	STRUCTURED_QTY_LITLEN,
	STRUCTURED_QTY_NAMELEN,
	STRUCTURED_QTY_NORMSEP,
	STRUCTURED_QTY_PILEN,
	STRUCTURED_QTY_TAGLEN,
	STRUCTURED_QTY_TAGLVL,
	STRUCTURED_QTY_MAX_VALUE
} StructuredQuantityTypeT;


/* Deprecated type and values */
#define  SGML_QTY_ATTCNT      STRUCTURED_QTY_ATTCNT
#define  SGML_QTY_ATTSPLEN    STRUCTURED_QTY_ATTSPLEN
#define  SGML_QTY_BSEQLEN     STRUCTURED_QTY_BSEQLEN
#define  SGML_QTY_DTAGLEN     STRUCTURED_QTY_DTAGLEN
#define  SGML_QTY_DTEMPLEN    STRUCTURED_QTY_DTEMPLEN
#define  SGML_QTY_ENTLVL      STRUCTURED_QTY_ENTLVL
#define  SGML_QTY_GRPCNT      STRUCTURED_QTY_GRPCNT
#define  SGML_QTY_GRPGTCNT    STRUCTURED_QTY_GRPGTCNT
#define  SGML_QTY_GRPLVL      STRUCTURED_QTY_GRPLVL
#define  SGML_QTY_LITLEN      STRUCTURED_QTY_LITLEN
#define  SGML_QTY_NAMELEN     STRUCTURED_QTY_NAMELEN
#define  SGML_QTY_NORMSEP     STRUCTURED_QTY_NORMSEP
#define  SGML_QTY_PILEN       STRUCTURED_QTY_PILEN
#define  SGML_QTY_TAGLEN      STRUCTURED_QTY_TAGLEN
#define  SGML_QTY_TAGLVL      STRUCTURED_QTY_TAGLVL
#define  SGML_QTY_MAX_VALUE   STRUCTURED_QTY_MAX_VALUE

#define SgmlQuantityTypeT StructuredQuantityTypeT

/*
 * Delimiter roles defined in SGML.
 */
typedef enum {
	STRUCTURED_DE_AND = 1,
	STRUCTURED_DE_COM,
	STRUCTURED_DE_CRO,
	STRUCTURED_DE_DSC,
	STRUCTURED_DE_DSO,
	STRUCTURED_DE_DTGC,
	STRUCTURED_DE_DTGO,
	STRUCTURED_DE_ERO,
	STRUCTURED_DE_ETAGO,
	STRUCTURED_DE_GRPC,
	STRUCTURED_DE_GRPO,
	STRUCTURED_DE_LIT,
	STRUCTURED_DE_LITA,
	STRUCTURED_DE_MDC,
	STRUCTURED_DE_MDO,
	STRUCTURED_DE_MINUS,
	STRUCTURED_DE_MSC,
	STRUCTURED_DE_NET,
	STRUCTURED_DE_OPT,
	STRUCTURED_DE_OR,
	STRUCTURED_DE_PERO,
	STRUCTURED_DE_PIC,
	STRUCTURED_DE_PIO,
	STRUCTURED_DE_PLUS,
	STRUCTURED_DE_REFC,
	STRUCTURED_DE_REP,
	STRUCTURED_DE_RNI,
	STRUCTURED_DE_SEQ,
	STRUCTURED_DE_STAGO,
	STRUCTURED_DE_TAGC,
	STRUCTURED_DE_VI,
	STRUCTURED_DE_MAX_VALUE,
	STRUCTURED_DE_XMLNET,
	STRUCTURED_DE_HCRO
} StructuredDelimiterTypeT;


/* Deprecated type and values */
#define SGML_DE_AND         STRUCTURED_DE_AND
#define SGML_DE_COM         STRUCTURED_DE_COM
#define SGML_DE_CRO         STRUCTURED_DE_CRO
#define SGML_DE_DSC         STRUCTURED_DE_DSC
#define SGML_DE_DSO         STRUCTURED_DE_DSO
#define SGML_DE_DTGC        STRUCTURED_DE_DTGC
#define SGML_DE_DTGO        STRUCTURED_DE_DTGO
#define SGML_DE_ERO         STRUCTURED_DE_ERO
#define SGML_DE_ETAGO       STRUCTURED_DE_ETAGO
#define SGML_DE_GRPC        STRUCTURED_DE_GRPC
#define SGML_DE_GRPO        STRUCTURED_DE_GRPO
#define SGML_DE_LIT         STRUCTURED_DE_LIT
#define SGML_DE_LITA        STRUCTURED_DE_LITA
#define SGML_DE_MDC         STRUCTURED_DE_MDC
#define SGML_DE_MDO         STRUCTURED_DE_MDO
#define SGML_DE_MINUS       STRUCTURED_DE_MINUS
#define SGML_DE_MSC         STRUCTURED_DE_MSC
#define SGML_DE_NET         STRUCTURED_DE_NET
#define SGML_DE_OPT         STRUCTURED_DE_OPT
#define SGML_DE_OR          STRUCTURED_DE_OR
#define SGML_DE_PERO        STRUCTURED_DE_PERO
#define SGML_DE_PIC         STRUCTURED_DE_PIC
#define SGML_DE_PIO         STRUCTURED_DE_PIO
#define SGML_DE_PLUS        STRUCTURED_DE_PLUS
#define SGML_DE_REFC        STRUCTURED_DE_REFC
#define SGML_DE_REP         STRUCTURED_DE_REP
#define SGML_DE_RNI         STRUCTURED_DE_RNI
#define SGML_DE_SEQ         STRUCTURED_DE_SEQ
#define SGML_DE_STAGO       STRUCTURED_DE_STAGO
#define SGML_DE_TAGC        STRUCTURED_DE_TAGC
#define SGML_DE_VI          STRUCTURED_DE_VI
#define SGML_DE_MAX_VALUE   STRUCTURED_DE_MAX_VALUE
#define XML_DE_NET          STRUCTURED_DE_XMLNET
#define XML_DE_HCRO         STRUCTURED_DE_HCRO

#define SgmlDelimiterTypeT  StructuredDelimiterTypeT 

/*
 * SGML and XML reserved names.
 */
typedef enum {
	STRUCTURED_RN_ANY = 1,
	STRUCTURED_RN_ATTLIST,
	STRUCTURED_RN_CDATA,
	STRUCTURED_RN_CONREF,
	STRUCTURED_RN_CURRENT,
	STRUCTURED_RN_DEFAULT,
	STRUCTURED_RN_DOCTYPE,
	STRUCTURED_RN_ELEMENT,
	STRUCTURED_RN_EMPTY,
	STRUCTURED_RN_ENDTAG,
	STRUCTURED_RN_ENTITIES,
	STRUCTURED_RN_ENTITY,
	STRUCTURED_RN_FIXED,
	STRUCTURED_RN_ID,
	STRUCTURED_RN_IDLINK,
	STRUCTURED_RN_IDREF,
	STRUCTURED_RN_IDREFS,
	STRUCTURED_RN_IGNORE,
	STRUCTURED_RN_IMPLIED,
	STRUCTURED_RN_INCLUDE,
	STRUCTURED_RN_INITIAL,
	STRUCTURED_RN_LINK,
	STRUCTURED_RN_LINKTYPE,
	STRUCTURED_RN_MD,
	STRUCTURED_RN_MS,
	STRUCTURED_RN_NAME,
	STRUCTURED_RN_NAMES,
	STRUCTURED_RN_NDATA,
	STRUCTURED_RN_NMTOKEN,
	STRUCTURED_RN_NMTOKENS,
	STRUCTURED_RN_NOTATION,
	STRUCTURED_RN_NUMBER,
	STRUCTURED_RN_NUMBERS,
	STRUCTURED_RN_NUTOKEN,
	STRUCTURED_RN_NUTOKENS,
	STRUCTURED_RN_O,
	STRUCTURED_RN_PCDATA,
	STRUCTURED_RN_PI,
	STRUCTURED_RN_POSTLINK,
	STRUCTURED_RN_PUBLIC,
	STRUCTURED_RN_RCDATA,
	STRUCTURED_RN_RE,
	STRUCTURED_RN_REQUIRED,
	STRUCTURED_RN_RESTORE,
	STRUCTURED_RN_RS,
	STRUCTURED_RN_SDATA,
	STRUCTURED_RN_SHORTREF,
	STRUCTURED_RN_SIMPLE,
	STRUCTURED_RN_SPACE,
	STRUCTURED_RN_STARTTAG,
	STRUCTURED_RN_SUBDOC,
	STRUCTURED_RN_SYSTEM,
	STRUCTURED_RN_TEMP,
	STRUCTURED_RN_USELINK,
	STRUCTURED_RN_USEMAP,
	STRUCTURED_RN_MAX_VALUE
} StructuredReservedNameT;


/* Deprecated type and values */
#define SGML_RN_ANY        STRUCTURED_RN_ANY
#define SGML_RN_ATTLIST    STRUCTURED_RN_ATTLIST
#define SGML_RN_CDATA      STRUCTURED_RN_CDATA
#define SGML_RN_CONREF     STRUCTURED_RN_CONREF
#define SGML_RN_CURRENT    STRUCTURED_RN_CURRENT
#define SGML_RN_DEFAULT    STRUCTURED_RN_DEFAULT
#define SGML_RN_DOCTYPE    STRUCTURED_RN_DOCTYPE
#define SGML_RN_ELEMENT    STRUCTURED_RN_ELEMENT
#define SGML_RN_EMPTY      STRUCTURED_RN_EMPTY
#define SGML_RN_ENDTAG     STRUCTURED_RN_ENDTAG
#define SGML_RN_ENTITIES   STRUCTURED_RN_ENTITIES
#define SGML_RN_ENTITY     STRUCTURED_RN_ENTITY
#define SGML_RN_FIXED      STRUCTURED_RN_FIXED
#define SGML_RN_ID         STRUCTURED_RN_ID
#define SGML_RN_IDLINK     STRUCTURED_RN_IDLINK
#define SGML_RN_IDREF      STRUCTURED_RN_IDREF
#define SGML_RN_IDREFS     STRUCTURED_RN_IDREFS
#define SGML_RN_IGNORE     STRUCTURED_RN_IGNORE
#define SGML_RN_IMPLIED    STRUCTURED_RN_IMPLIED
#define SGML_RN_INCLUDE    STRUCTURED_RN_INCLUDE
#define SGML_RN_INITIAL    STRUCTURED_RN_INITIAL
#define SGML_RN_LINK       STRUCTURED_RN_LINK
#define SGML_RN_LINKTYPE   STRUCTURED_RN_LINKTYPE
#define SGML_RN_MD         STRUCTURED_RN_MD
#define SGML_RN_MS         STRUCTURED_RN_MS
#define SGML_RN_NAME       STRUCTURED_RN_NAME
#define SGML_RN_NAMES      STRUCTURED_RN_NAMES
#define SGML_RN_NDATA      STRUCTURED_RN_NDATA
#define SGML_RN_NMTOKEN    STRUCTURED_RN_NMTOKEN
#define SGML_RN_NMTOKENS   STRUCTURED_RN_NMTOKENS
#define SGML_RN_NOTATION   STRUCTURED_RN_NOTATION
#define SGML_RN_NUMBER     STRUCTURED_RN_NUMBER
#define SGML_RN_NUMBERS    STRUCTURED_RN_NUMBERS
#define SGML_RN_NUTOKEN    STRUCTURED_RN_NUTOKEN
#define SGML_RN_NUTOKENS   STRUCTURED_RN_NUTOKENS
#define SGML_RN_O          STRUCTURED_RN_O
#define SGML_RN_PCDATA     STRUCTURED_RN_PCDATA
#define SGML_RN_PI         STRUCTURED_RN_PI
#define SGML_RN_POSTLINK   STRUCTURED_RN_POSTLINK
#define SGML_RN_PUBLIC     STRUCTURED_RN_PUBLIC
#define SGML_RN_RCDATA     STRUCTURED_RN_RCDATA
#define SGML_RN_RE         STRUCTURED_RN_RE
#define SGML_RN_REQUIRED   STRUCTURED_RN_REQUIRED
#define SGML_RN_RESTORE    STRUCTURED_RN_RESTORE
#define SGML_RN_RS         STRUCTURED_RN_RS
#define SGML_RN_SDATA      STRUCTURED_RN_SDATA
#define SGML_RN_SHORTREF   STRUCTURED_RN_SHORTREF
#define SGML_RN_SIMPLE     STRUCTURED_RN_SIMPLE
#define SGML_RN_SPACE      STRUCTURED_RN_SPACE
#define SGML_RN_STARTTAG   STRUCTURED_RN_STARTTAG
#define SGML_RN_SUBDOC     STRUCTURED_RN_SUBDOC
#define SGML_RN_SYSTEM     STRUCTURED_RN_SYSTEM
#define SGML_RN_TEMP       STRUCTURED_RN_TEMP
#define SGML_RN_USELINK    STRUCTURED_RN_USELINK
#define SGML_RN_USEMAP     STRUCTURED_RN_USEMAP
#define SGML_RN_MAX_VALUE  STRUCTURED_RN_MAX_VALUE

#define SgmlReservedNameT  StructuredReservedNameT 


/*
 * SGML optional features
 */
typedef enum {
	STRUCTURED_FEAT_SHORTREF = 1,
	STRUCTURED_FEAT_DATATAG,
	STRUCTURED_FEAT_OMITTAG,
	STRUCTURED_FEAT_RANK,
	STRUCTURED_FEAT_SHORTTAG,
	STRUCTURED_FEAT_SIMPLE_LINK,
	STRUCTURED_FEAT_IMPLICIT_LINK,
	STRUCTURED_FEAT_EXPLICIT_LINK,
	STRUCTURED_FEAT_CONCUR,
	STRUCTURED_FEAT_SUBDOC,
	STRUCTURED_FEAT_FORMAL,
	STRUCTURED_FEAT_MAX_VALUE
} StructuredFeatureTypeT;


/* Deprecated type and values */
#define SGML_FEAT_SHORTREF       STRUCTURED_FEAT_SHORTREF
#define SGML_FEAT_DATATAG        STRUCTURED_FEAT_DATATAG
#define SGML_FEAT_OMITTAG        STRUCTURED_FEAT_OMITTAG
#define SGML_FEAT_RANK           STRUCTURED_FEAT_RANK
#define SGML_FEAT_SHORTTAG       STRUCTURED_FEAT_SHORTTAG
#define SGML_FEAT_SIMPLE_LINK    STRUCTURED_FEAT_SIMPLE_LINK
#define SGML_FEAT_IMPLICIT_LINK  STRUCTURED_FEAT_IMPLICIT_LINK
#define SGML_FEAT_EXPLICIT_LINK  STRUCTURED_FEAT_EXPLICIT_LINK
#define SGML_FEAT_CONCUR         STRUCTURED_FEAT_CONCUR
#define SGML_FEAT_SUBDOC         STRUCTURED_FEAT_SUBDOC
#define SGML_FEAT_FORMAL         STRUCTURED_FEAT_FORMAL
#define SGML_FEAT_MAX_VALUE      STRUCTURED_FEAT_MAX_VALUE

#define SgmlFeatureTypeT  StructuredFeatureTypeT 

/*
 * entity name is name of a paramter entity without the initial pero
 * delimiter, or the name of a general entity.
 */
typedef enum {
	STRUCTURED_ES_GENERAL = 1,
	STRUCTURED_ES_PARAMETER,
	STRUCTURED_ES_MAX_VALUE
} StructuredEntityScopeT ;


/* Deprecated type and values */
#define	SGML_ES_GENERAL    STRUCTURED_ES_GENERAL
#define	SGML_ES_PARAMETER  STRUCTURED_ES_PARAMETER
#define	SGML_ES_MAX_VALUE  STRUCTURED_ES_MAX_VALUE

#define SgmlEntityScopeT StructuredEntityScopeT

/*
 * STRUCTURED_ET_TEXT identifies an entity that is not declared with bracketed
 * text and is not a data entity or subdocument entity. Other values
 * distinguish various types of bracketed text, data entities, and 
 * subdocument entities.
 */
typedef enum {
	STRUCTURED_ET_UNDEFINED = 0,
	STRUCTURED_ET_TEXT,
	STRUCTURED_ET_CDATA,
	STRUCTURED_ET_SDATA,
	STRUCTURED_ET_NDATA,
	STRUCTURED_ET_PI,
	STRUCTURED_ET_STARTTAG,
	STRUCTURED_ET_ENDTAG,
	STRUCTURED_ET_MS,
	STRUCTURED_ET_MD,
	STRUCTURED_ET_SUBDOC,
	STRUCTURED_ET_MAX_VALUE
} StructuredEntityTypeT;

/* Deprecated type and values */
#define SGML_ET_UNDEFINED   STRUCTURED_ET_UNDEFINED
#define	SGML_ET_TEXT        STRUCTURED_ET_TEXT
#define	SGML_ET_CDATA       STRUCTURED_ET_CDATA
#define	SGML_ET_SDATA       STRUCTURED_ET_SDATA
#define	SGML_ET_NDATA       STRUCTURED_ET_NDATA
#define	SGML_ET_PI          STRUCTURED_ET_PI
#define	SGML_ET_STARTTAG    STRUCTURED_ET_STARTTAG
#define	SGML_ET_ENDTAG      STRUCTURED_ET_ENDTAG
#define	SGML_ET_MS          STRUCTURED_ET_MS
#define	SGML_ET_MD          STRUCTURED_ET_MD
#define	SGML_ET_SUBDOC      STRUCTURED_ET_SUBDOC
#define	SGML_ET_MAX_VALUE   STRUCTURED_ET_MAX_VALUE

#define SgmlEntityTypeT  StructuredEntityTypeT

/*
 * The declared value of an attribute
 */
typedef enum {
	STRUCTURED_AT_UNDEFINED = 0,
	STRUCTURED_AT_CDATA,
	STRUCTURED_AT_ENTITY,
	STRUCTURED_AT_ENTITIES,
	STRUCTURED_AT_ID,
	STRUCTURED_AT_IDREF,
	STRUCTURED_AT_IDREFS,
	STRUCTURED_AT_NAME,
	STRUCTURED_AT_NAMES,
	STRUCTURED_AT_NMTOKEN,
	STRUCTURED_AT_NMTOKENS,
	STRUCTURED_AT_NOTATION,
	STRUCTURED_AT_NUMBER,
	STRUCTURED_AT_NUMBERS,
	STRUCTURED_AT_NUTOKEN,
	STRUCTURED_AT_NUTOKENS,
	STRUCTURED_AT_NMTOKENGRP,
	STRUCTURED_AT_INTEGER,
	STRUCTURED_AT_FLOAT,
	STRUCTURED_AT_MAX_VALUE
} StructuredAttrDeclaredValueT;


/* Deprecated type and values */
#define SGML_AT_UNDEFINED    STRUCTURED_AT_UNDEFINED
#define SGML_AT_CDATA        STRUCTURED_AT_CDATA
#define SGML_AT_ENTITY       STRUCTURED_AT_ENTITY
#define SGML_AT_ENTITIES     STRUCTURED_AT_ENTITIES
#define SGML_AT_ID           STRUCTURED_AT_ID
#define SGML_AT_IDREF        STRUCTURED_AT_IDREF
#define SGML_AT_IDREFS       STRUCTURED_AT_IDREFS
#define SGML_AT_NAME         STRUCTURED_AT_NAME
#define SGML_AT_NAMES        STRUCTURED_AT_NAMES
#define SGML_AT_NMTOKEN      STRUCTURED_AT_NMTOKEN
#define SGML_AT_NMTOKENS     STRUCTURED_AT_NMTOKENS
#define SGML_AT_NOTATION     STRUCTURED_AT_NOTATION
#define SGML_AT_NUMBER       STRUCTURED_AT_NUMBER
#define SGML_AT_NUMBERS      STRUCTURED_AT_NUMBERS
#define SGML_AT_NUTOKEN      STRUCTURED_AT_NUTOKEN
#define SGML_AT_NUTOKENS     STRUCTURED_AT_NUTOKENS
#define SGML_AT_NMTOKENGRP   STRUCTURED_AT_NMTOKENGRP
#define SGML_AT_INTEGER      STRUCTURED_AT_INTEGER
#define SGML_AT_FLOAT        STRUCTURED_AT_FLOAT
#define SGML_AT_MAX_VALUE    STRUCTURED_AT_MAX_VALUE

#define SgmlAttrDeclaredValueT  StructuredAttrDeclaredValueT 

/*
 * Default value type of an SGML attribute.
 */
typedef enum {
	STRUCTURED_AV_FIXED = 1,
	STRUCTURED_AV_REQUIRED,
	STRUCTURED_AV_CURRENT,
	STRUCTURED_AV_CONREF,
	STRUCTURED_AV_IMPLIED,
	STRUCTURED_AV_DEFAULT,
	STRUCTURED_AV_MAX_VALUE
} StructuredAttrDefaultValueT;

/* Deprecated type and values */
#define SGML_AV_FIXED      STRUCTURED_AV_FIXED
#define SGML_AV_REQUIRED   STRUCTURED_AV_REQUIRED
#define SGML_AV_CURRENT    STRUCTURED_AV_CURRENT
#define SGML_AV_CONREF     STRUCTURED_AV_CONREF
#define SGML_AV_IMPLIED    STRUCTURED_AV_IMPLIED
#define SGML_AV_DEFAULT    STRUCTURED_AV_DEFAULT
#define SGML_AV_MAX_VALUE  STRUCTURED_AV_MAX_VALUE

#define SgmlAttrDefaultValueT StructuredAttrDefaultValueT

/*
 * Whether element definition is a model group, ANY, or declared content.
 */
typedef enum {
	STRUCTURED_EC_MODEL_GROUP = 1,
	STRUCTURED_EC_ANY,
	STRUCTURED_EC_EMPTY,
	STRUCTURED_EC_CDATA,
	STRUCTURED_EC_RCDATA,
	STRUCTURED_EC_MAX_VALUE
} StructuredContentTypeT;

/* Deprecated type and values */
#define SGML_EC_MODEL_GROUP  STRUCTURED_EC_MODEL_GROUP
#define SGML_EC_ANY          STRUCTURED_EC_ANY
#define SGML_EC_EMPTY        STRUCTURED_EC_EMPTY
#define SGML_EC_CDATA        STRUCTURED_EC_CDATA
#define SGML_EC_RCDATA       STRUCTURED_EC_RCDATA
#define SGML_EC_MAX_VALUE    STRUCTURED_EC_MAX_VALUE

#define SgmlContentTypeT StructuredContentTypeT 

/*
 * Tokens in a model group
 */
typedef enum {
	STRUCTURED_CT_GRPO = 1,
	STRUCTURED_CT_GRPC,
	STRUCTURED_CT_PCDATA,
	STRUCTURED_CT_REP,
	STRUCTURED_CT_PLUS,
	STRUCTURED_CT_OPT,
	STRUCTURED_CT_SEQ,
	STRUCTURED_CT_AND,
	STRUCTURED_CT_OR,
	STRUCTURED_CT_NAME,
	STRUCTURED_CT_MAX_VALUE
} StructuredContentTokenT;

/* Deprecated type and values */
#define SGML_CT_GRPO      STRUCTURED_CT_GRPO 
#define SGML_CT_GRPC      STRUCTURED_CT_GRPC
#define SGML_CT_PCDATA    STRUCTURED_CT_PCDATA
#define SGML_CT_REP       STRUCTURED_CT_REP
#define SGML_CT_PLUS      STRUCTURED_CT_PLUS
#define SGML_CT_OPT       STRUCTURED_CT_OPT
#define SGML_CT_SEQ       STRUCTURED_CT_SEQ
#define SGML_CT_AND       STRUCTURED_CT_AND
#define SGML_CT_OR        STRUCTURED_CT_OR
#define SGML_CT_NAME      STRUCTURED_CT_NAME
#define SGML_CT_MAX_VALUE STRUCTURED_CT_MAX_VALUE

#define SgmlContentTokenT StructuredContentTokenT 

/********************************************************************
 * DATA STRUCTURES
 ********************************************************************/

/*
 * Attribute name/value pair
 */
typedef struct {
	StringT sgmlAttrName;
	StringListT sgmlAttrVal;

	IntT sgmlAttrFlags;

#define STRUCTURED_ATTR_VAL_SPECIFIED	  0x0001
#define STRUCTURED_ATTR_IS_CDATA	  0x0002
#define STRUCTURED_ATTR_IS_ID		  0x0004
#define STRUCTURED_ATTR_IS_FIXED	  0x0008
#define STRUCTURED_ATTR_IS_IDREF	  0x0010
#define STRUCTURED_ATTR_IS_NAME_TOKEN	  0x0020

} StructuredAttrValT;


#define SGML_ATTR_VAL_SPECIFIED	  STRUCTURED_ATTR_VAL_SPECIFIED	
#define SGML_ATTR_IS_CDATA	  STRUCTURED_ATTR_IS_CDATA
#define SGML_ATTR_IS_ID		  STRUCTURED_ATTR_IS_ID
#define SGML_ATTR_IS_FIXED	  STRUCTURED_ATTR_IS_FIXED
#define SGML_ATTR_IS_IDREF	  STRUCTURED_ATTR_IS_IDREF
#define SGML_ATTR_IS_NAME_TOKEN	  STRUCTURED_ATTR_IS_NAME_TOKEN

#define SgmlAttrValT StructuredAttrValT

#define Sgml_IsAttrValSpecified(attrValp)		\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_VAL_SPECIFIED) 

#define Structured_IsAttrValSpecified(attrValp)		\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_VAL_SPECIFIED) 

#define Sgml_IsAttrCDATA(attrValp)		\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_CDATA) 

#define Structured_IsAttrCDATA(attrValp)		\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_CDATA)

#define Sgml_IsAttrNameToken(attrValp)			\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_NAME_TOKEN)

#define Structured_IsAttrNameToken(attrValp)			\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_NAME_TOKEN)

#define Sgml_IsIdAttr(attrValp)					\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_ID)

#define Structured_IsIdAttr(attrValp)					\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_ID)

#define Sgml_IsIdRefAttr(attrValp)					\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_IDREF)

#define Structured_IsIdRefAttr(attrValp)					\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_IDREF)

#define Sgml_IsAttrFixed(attrValp)				\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_FIXED)

#define Structured_IsAttrFixed(attrValp)				\
		(((StructuredAttrValT *)attrValp)->sgmlAttrFlags & STRUCTURED_ATTR_IS_FIXED)


/*
 * List of attribute values associated with a start tag
 */
typedef struct {
	UIntT len;
	StructuredAttrValT *val;
} StructuredAttrValsT;

#define SgmlAttrValsT StructuredAttrValsT 

/*
 * Entity declaration information
 */
typedef struct {
	StringT ename;
	StringT etext;
	StructuredEntityScopeT escope;
	StructuredEntityTypeT etype;
	BoolT external;
	StringT pubid;
	StringT sysid;
	StringT nname;
	StructuredAttrValsT dataAttrVals;
	FilePathT *fp;
} StructuredEntityDefT;

#define SgmlEntityDefT StructuredEntityDefT 

/*
 * Attribute definition information.
 */
typedef struct {
	StringT attrName;
	StructuredAttrDeclaredValueT declVal;
	StructuredAttrDefaultValueT defValType;
	StringListT tokenGroup;
	StringListT defVal;
	StringT minVal;
	StringT maxVal;
} StructuredAttrDefT ;

#define SgmlAttrDefT StructuredAttrDefT 

typedef struct {
	UIntT len;
	StructuredAttrDefT *val;
} StructuredAttrDefsT;

#define SgmlAttrDefsT StructuredAttrDefsT
/*
 * Notation definition
 */
typedef struct {
	StringT nname;
	StringT pubid;
	StringT sysid;
	StructuredAttrDefsT dataAttrs;
} StructuredNotationDefT;

#define SgmlNotationDefT StructuredNotationDefT 

/*
 * Element definition
 */
typedef struct {
	StringT gi;
	StructuredContentTypeT conType;
	UCharT *modelGroup;
	StringListT inclusions, exclusions;
	StructuredAttrDefsT attrs;

	IntT tagOmission;
#define OMIT_START_TAG  0x0001
#define OMIT_END_TAG    0x0002

	IntT reserved;
	StringT type;
	StringT minval;
	StringT maxval;
} StructuredElementDefT;

#define SgmlElementDefT StructuredElementDefT 

typedef struct {
	UIntT len;
	StructuredElementDefT *val;
} StructuredElementDefsT;

#define StartTagOmissible(edefp)    ((edefp)->tagOmission & OMIT_START_TAG)
#define EndTagOmissible(edefp)      ((edefp)->tagOmission & OMIT_END_TAG)



/*>sgml_psr.c>*/
/* --- */
/*<spuser.c<*/
extern ConStringT Sgml_GetLcnmstrt FARGS((VoidT));
extern ConStringT Structured_GetLcnmstrt FARGS((VoidT));
extern ConStringT Sgml_GetUcnmstrt FARGS((VoidT));
extern ConStringT Structured_GetUcnmstrt FARGS((VoidT));
extern ConStringT Sgml_GetLcnmchar FARGS((VoidT));
extern ConStringT Structured_GetLcnmchar FARGS((VoidT));
extern ConStringT Sgml_GetUcnmchar FARGS((VoidT));
extern ConStringT Structured_GetUcnmchar FARGS((VoidT));
extern BoolT Sgml_GetEntityNamecase FARGS((VoidT));
extern BoolT Structured_GetEntityNamecase FARGS((VoidT));
extern BoolT Sgml_GetGeneralNamecase FARGS((VoidT));
extern BoolT Structured_GetGeneralNamecase FARGS((VoidT));
extern UIntT Sgml_GetQuantity FARGS((SgmlQuantityTypeT quantity));
extern UIntT Structured_GetQuantity FARGS((
	StructuredQuantityTypeT quantity));
extern ConStringT Sgml_GetDelimiterString FARGS((
	SgmlDelimiterTypeT deltype));
extern ConStringT Structured_GetDelimiterString FARGS((
	StructuredDelimiterTypeT deltype));
extern ConStringT Sgml_GetReservedName FARGS((SgmlReservedNameT rn));
extern ConStringT Structured_GetReservedName FARGS((
	StructuredReservedNameT rn));
extern IntT Sgml_GetOptionalFeature FARGS((SgmlFeatureTypeT feature));
extern IntT Structured_GetOptionalFeature FARGS((
	StructuredFeatureTypeT feature));
extern ConStringT Sgml_GetAppinfo FARGS((VoidT));
extern ConStringT Structured_GetAppinfo FARGS((VoidT));
extern ConStringT Sgml_GetFirstEntityName FARGS((
	SgmlEntityScopeT scope));
extern ConStringT Structured_GetFirstEntityName FARGS((
	StructuredEntityScopeT scope));
extern ConStringT Sgml_GetNextEntityName FARGS((SgmlEntityScopeT scope,
	StringT ename));
extern ConStringT Structured_GetNextEntityName FARGS((
	StructuredEntityScopeT scope, StringT ename));
extern const SgmlEntityDefT *Sgml_GetEntityDef FARGS((
	SgmlEntityScopeT scope, StringT ename));
extern const StructuredEntityDefT *Structured_GetEntityDef FARGS((
	StructuredEntityScopeT scope, StringT ename));
extern const SgmlEntityDefT *Sgml_GetDefaultEntityDef FARGS((VoidT));
extern const StructuredEntityDefT *Structured_GetDefaultEntityDef FARGS((VoidT));
extern ConStringT Sgml_GetFirstNotationName FARGS((VoidT));
extern ConStringT Structured_GetFirstNotationName FARGS((VoidT));
extern ConStringT Sgml_GetNextNotationName FARGS((StringT nname));
extern ConStringT Structured_GetNextNotationName FARGS((StringT nname));
extern const SgmlNotationDefT *Sgml_GetNotationDef FARGS((
	StringT nname));
extern const StructuredNotationDefT *Structured_GetNotationDef FARGS((
	StringT nname));
extern ConStringT Sgml_GetDocTypeName FARGS((VoidT));
extern ConStringT Structured_GetDocTypeName FARGS((VoidT));
extern ConStringT Sgml_GetFirstElementName FARGS((VoidT));
extern ConStringT Structured_GetFirstElementName FARGS((VoidT));
extern ConStringT Sgml_GetNextElementName FARGS((StringT etag));
extern ConStringT Structured_GetNextElementName FARGS((StringT etag));
extern const SgmlElementDefT *Sgml_GetElementDef FARGS((StringT etag));
extern const StructuredElementDefT *Structured_GetElementDef FARGS((
	StringT etag));
extern VoidT Sgml_GetLoc FARGS((UIntT *linep, FilePathT **fpp));
extern VoidT Structured_GetLoc FARGS((UIntT *linep, FilePathT **fpp));
extern SgmlAttrValT Sgml_CopyAttrVal FARGS((SgmlAttrValT *attrVal));
extern SgmlAttrValT Structured_CopyAttrVal FARGS((
	StructuredAttrValT *attrVal));
extern SgmlAttrValsT Sgml_CopyAttrVals FARGS((SgmlAttrValsT *attrVals));
extern SgmlAttrValsT Structured_CopyAttrVals FARGS((
	StructuredAttrValsT *attrVals));
extern VoidT Sgml_DeallocateAttrVal FARGS((SgmlAttrValT *attrVal));
extern VoidT Structured_DeallocateAttrVal FARGS((
	StructuredAttrValT *attrVal));
extern VoidT Sgml_DeallocateAttrVals FARGS((SgmlAttrValsT *attrVals));
extern VoidT Structured_DeallocateAttrVals FARGS((
	StructuredAttrValsT *attrVals));
/*>spuser.c>*/
#ifdef __cplusplus /* end */
}
#endif
#endif /* fm_psr_h */

/* Installed: .\fm_psr.h */
