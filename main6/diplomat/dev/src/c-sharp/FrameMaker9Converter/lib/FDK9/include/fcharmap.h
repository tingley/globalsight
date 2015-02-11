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

#ifndef FCHARMAP_H
#define FCHARMAP_H

/*
 * Useful character codes.
 * These are rendered in FrameMaker's internal character encoding,
 * which differs from ASCII at several points, e.g., FC_EOL (newline)
 * is 0x09 rather than the usual 0x0A.
 */

/**************************************************************
* 
* These are NON-UNICODE versions of the special characters
* and should be avoided as far as possible.
* The unicode versions are specified later in the file
* 
***************************************************************/



/* Breaks */
#define	FC_UTILITY	0x01	/* Used by search and index */
#define FC_DBREAK	0x02	/* Discretionary break */
#define FC_NBREAK	0x03	/* Suppress this break */
#define	FC_DHYPHEN	0x04	/* Discretionary hyphen */
#define	FC_NHYPHEN	0x05	/* Suppress this h-point */
#define	FC_HYPHEN	0x06	/* Temporary hyphen */

/* Control-character codes */
#define	FC_TAB		0x08  
#define	FC_EOL		0x09	/* Hard return */
#define	FC_EOP		0x0A 	/* End of para */
#define	FC_EOD		0x0B	/* End of flow */

/* Unstretchable spaces */
#define FC_SPACE_NUMBER	0x10	/* Number space */
#define	FC_SPACE_HARD	0x11	/* Hard space   */
#define FC_SPACE_THIN	0x12	/* Thin == 1/12 em */
#define FC_SPACE_EN	0x13	/* En   == 1/2 em */
#define FC_SPACE_EM	0x14	/* Em   == 1 em */

#define FC_HYPHEN_HARD	0x15	/* Unbreakable explicit hyphen */

/* Block sentinels */
#define	FC_ESC		0x1B   /* sentinel code for cblocks */
#define	FC_SCH		0x1C   /* sentinel code for sblocks */

/* Convenient names */
#define	FC_SPACE		0x20   /* ' ' regular space */
#define FC_QUOTEDBL		0x22   /* '"' straight double quote */
#define FC_QUOTESINGLE		0x27   /* '\'' straight single quote */
#define FC_BACKSLASH		0x5c   /* '\\' Backslash             */

#define FC_GUILLEMOTLEFT	0xc7   /* guillemotleft */
#define FC_GUILLEMOTRIGHT	0xc8   /* guillemotright */
#define FC_QUOTEDBLLEFT		0xd2   /* curly double left quote    */
#define FC_QUOTEDBLRIGHT	0xd3   /* curly double right quote   */
#define FC_QUOTELEFT		0xd4   /* curly single left quote    */
#define FC_QUOTERIGHT		0xd5   /* curly single right quote   */
#define FC_GUILSINGLLEFT	0xdc   /* guillemotleft */
#define FC_GUILSINGLRIGHT	0xdd   /* guillemotright */
#define FC_QUOTESINGLBASE	0xe2   /* quotesinglbase */
#define FC_QUOTEDBLBASE		0xe3   /* quotedblbase */

/* Useful metachar display code names */
#define	FC_CENT		0xa2
#define	FC_POUND	0xa3
#define	FC_YEN		0xb4
#define	FC_ENDASH	0xd0
#define	FC_DAGGER	0xa0
#define	FC_DAGGERDBL	0xe0
#define	FC_BULLET	0xa5
#define	FC_EMDASH	0xd1
#ifndef FC_META
#define FC_META		0x80
#endif









#define FC_CONTROL	0x001
#define FC_UPPERCASE	0x002
#define	FC_LOWERCASE	0x004
#define FC_ALPHABETIC	0x008
#define FC_NUMERIC	0x010
#define FC_HEXADECIMAL	0x020
#define FC_INVISIBLE  0x040
#define FC_WORDBREAK	0x080
#define	FC_INWORD	0x100
#define FC_ENDOFLINE	0x200
#define FC_ENDOFSATZ	0x400
#define FC_INDEXABLE	0x800
#define FC_BREAK	0x1000
#define FC_BEFORELQ	0x2000
#define FC_CONSONANT	0x4000

#define F_CharIsUpper(c)	(FdeCharProps[(UCharT)(c)]&FC_UPPERCASE)
#define F_CharIsLower(c)	(FdeCharProps[(UCharT)(c)]&FC_LOWERCASE)
#define F_CharIsEol(c)		(FdeCharProps[(UCharT)(c)]&FC_ENDOFLINE)
#define F_CharIsControl(c)	(FdeCharProps[(UCharT)(c)]&FC_CONTROL)
#define F_CharIsAlphabetic(c)	(FdeCharProps[(UCharT)(c)]&FC_ALPHABETIC)
#define F_CharIsNumeric(c)	(FdeCharProps[(UCharT)(c)]&FC_NUMERIC)
#define F_CharIsAlphaNumeric(c)	(FdeCharProps[(UCharT)(c)]&(FC_NUMERIC|FC_ALPHABETIC))
#define F_CharIsHexadecimal(c)	(FdeCharProps[(UCharT)(c)]&FC_HEXADECIMAL)
#define F_CharIsInWord(c)       (FdeCharProps[(UCharT) (c)]&FC_INWORD)

#define F_CharToUpper(c)	(FdeMToUpper[(UCharT)(c)])
#define F_CharToLower(c)	(FdeMToLower[(UCharT)(c)])

#define F_Hex2AtoD(msn9,lsn9)\
((FdeHexAtoD[(UCharT) (msn9)]<<4) | FdeHexAtoD[(UCharT) (lsn9)])
#define F_HexDto2A(i9,msn9,lsn9)\
((msn9)=FdeHexDtoA[((UCharT)(i9))>>4], (lsn9)=FdeHexDtoA[((UCharT)(i9))&0xF])

#ifdef __cplusplus
extern "C" {
#endif

extern const UCharT *FdeIdentMap;
extern const UCharT *FdeMToUpper;
extern const UCharT *FdeMToLower;
extern const UCharT *FdeHexDtoA;
extern const UCharT *FdeHexAtoD;
extern const UShortT *FdeCharProps;
extern const UCharT *FdeMToAnMap;
extern const UCharT *FdeAnToMMap;

#ifdef __cplusplus
}
#endif






/**************************************************************
* 
* These are the Unicode versions of the special characters.
* From FrameMaker 8.0 onwards, these should be used instead of 
* the non unicode versions specified earlier in the file
* 
***************************************************************/


/* Breaks */
#define	FC_UTILITY_U	0x01	/* Used by search and index */
#define FC_DBREAK_U		0x02	/* Discretionary break */
#define FC_NBREAK_U		0x03	/* Suppress this break */
#define	FC_DHYPHEN_U	0x04	/* Discretionary hyphen */
#define	FC_NHYPHEN_U	0x05	/* Suppress this h-point */
#define	FC_HYPHEN_U		0x06	/* Temporary hyphen */

/* Control-character codes */
#define	FC_TAB_U		0x08  
#define	FC_EOL_U		0x09	/* Hard return */
#define	FC_EOP_U		0x0A 	/* End of para */
#define	FC_EOD_U		0x0B	/* End of flow */

/* Unstretchable spaces */
#define FC_SPACE_NUMBER_U	0x10	/* Number space */
#define	FC_SPACE_HARD_U		0x11	/* Hard space   */
#define FC_SPACE_THIN_U		0x12	/* Thin == 1/12 em */
#define FC_SPACE_EN_U		0x13	/* En   == 1/2 em */
#define FC_SPACE_EM_U		0x14	/* Em   == 1 em */

#define FC_HYPHEN_HARD_U	0x15	/* Unbreakable explicit hyphen */

/* Block sentinels */
#define	FC_ESC_U		0x1B   /* sentinel code for cblocks */
#define	FC_SCH_U		0x1C   /* sentinel code for sblocks */

/* Convenient names */
#define	FC_SPACE_U			0x20   /* ' ' regular space */
#define FC_QUOTEDBL_U		0x22   /* '"' straight double quote */
#define FC_QUOTESINGLE_U	0x27   /* '\'' straight single quote */
#define FC_BACKSLASH_U		0x5c   /* '\\' Backslash             */



#define FC_GUILLEMOTLEFT_U		0x00AB   /* guillemotleft */
#define FC_GUILLEMOTRIGHT_U		0x00BB   /* guillemotright */
#define FC_QUOTELEFT_U          0x2018   /* curly single left quote    */
#define FC_QUOTERIGHT_U         0x2019   /* curly single right quote   */
#define FC_QUOTEDBLLEFT_U       0x201C   /* curly double left quote    */
#define FC_QUOTEDBLRIGHT_U      0x201D   /* curly double right quote   */
#define FC_GUILSINGLLEFT_U		0x2039   /* guillemotleft */
#define FC_GUILSINGLRIGHT_U		0x203A   /* guillemotright */
#define FC_QUOTESINGLBASE_U		0x201A   /* quotesinglbase */
#define FC_QUOTEDBLBASE_U		0x201E   /* quotedblbase */



/* Useful metachar display code names */
#define	FC_CENT_U		0x00A2
#define	FC_POUND_U		0x00A3
#define	FC_YEN_U		0x00A5
#define	FC_ENDASH_U		0x2013
#define	FC_DAGGER_U		0x2020
#define	FC_DAGGERDBL_U	0x2021
#define	FC_BULLET_U		0x2022
#define	FC_EMDASH_U		0x2014
#ifndef FC_META_U
#define FC_META_U		0x80
#endif




#endif /* FCHARMAP_H */
