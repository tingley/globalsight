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

#ifndef FSTRINGS_H
#define FSTRINGS_H

/*
 *	These routines supersede the standard <strings.h> library.
 *	They're all impervious to being called with NULL arguments:
 *
 *  StringT F_StrCopyString(s)       Makes a new copy of s
 *  BoolT   CharIsInString(c,s)	   Searches for a character in a string
 *  IntT    F_StrLen(s)              Replaces strlen directly
 *  VoidT   F_StrCpy(to,from)        Replaces strcpy directly
 *  VoidT   F_StrCpyN(to,from,tolen) Copy on top of existing string
 *  VoidT   F_StrCatN(to,from,tolen) Tack onto the end of a string
 *  VoidT   F_StrCatCharN(to,from,tolen) Tack char onto the end of a string
 *  VoidT   F_StrTrunc(s)            Empty out the string (s[0] = 0)
 *  BoolT   F_StrEqual(s1,s2)        True if both strings the same
 *  BoolT   F_StrPrefix(s1,s2)       True if s2 is a prefix of s1
 *  BoolT   F_StrIPrefix(s1,s2)      ditto, but case-insensitive
 *  IntT    F_StrCmp(s1,s2)          Replaces strcmp directly
 *  IntT    F_StrICmp(s1,s2)         Case-insensitive F_StrCmp
 *  VoidT   F_StrReverse(s,l)	   Reverse the characters in a string
 *  BoolT   F_StrIsEmpty(s)          True if s is NULL or *s is NUL (MACRO)
 *  VoidT   F_StrFree(s)             Gets rid of a CopyString'd thing (MACRO)
 *
 *  Now you can compare strings for equality without having to think
 *  about the strange return values of "strcmp".  Also, any NULL pointer
 *  in a source string is considered to be the same as a pointer to an
 *  empty string; while any NULL pointer for a destination string causes
 *  the operation to be silently skipped.
 * 
 *  The only tricky part is that the Str*N() procedures use a third parameter
 *  that is the total allowable length of the destination string, including the
 *  terminating NUL byte.  These routines guarantee that they'll never write
 *  out of bounds, and they always leave a NUL byte at the end of the string.
 *  (This is different from the strncpy/strncat routines' length parameter,
 *  which gives the maximum size of the source string, kind of.)
 */


/*
 * Offsets of fields in Version 1 hint string. Note that these may change
 * in Version 2 hint strings. To extract fields from a hint string reliably,
 * use the F_GetValueFromHint call below, which will work with all hints. The
 * selector values for use with that call are in fapidefs.h.
 */

#define HINT_VERSION_OFFSET			0
#define HINT_VENDORID_OFFSET		4
#define HINT_FORMATID_OFFSET		8
#define HINT_PLATFORM_OFFSET		12
#define HINT_FILTERVERSION_OFFSET	16
#define HINT_FILTERNAME_OFFSET		20

/* Current Version of the hint string */
#define FILTERHINTVERSION1		(StringT) "0001"

/* Vendor ID's */
#define FM_INTERNALFILTER		(StringT)"FRAM"
#define FM_EXTERNALFILTER		(StringT)"FFLT"
#define IMAGEMARKFILTER			(StringT)"IMAG"
#define ADOBEW4WFILTER			(StringT)"AW4W"
#define XTNDFILTER				(StringT)"XTND"

#ifdef  __cplusplus
extern "C" {
#endif

extern StringT F_GetValueFromHint FARGS((ConStringT hintString, IntT selector));
extern StringT F_CreateVers1HintString FARGS((ConStringT vendorId, ConStringT formatId, 
			ConStringT platformId, ConStringT filterVersion, ConStringT filterName));

extern StringT F_StrNew FARGS((IntT len));
extern StringT F_StrCopyString FARGS((ConStringT s));
extern IntT F_StrLen FARGS((ConStringT s));
extern VoidT F_StrCpy FARGS((StringT s1, ConStringT s2));
extern IntT F_StrCpyN FARGS((StringT s1, ConStringT s2, IntT n));
extern BoolT F_StrEqual FARGS((ConStringT s1, ConStringT s2));
extern BoolT F_StrEqualN FARGS((ConStringT s1, ConStringT s2, IntT n));
extern BoolT F_StrIEqualN FARGS((ConStringT s1, ConStringT s2, IntT n));
extern VoidT F_StrTrunc FARGS((StringT s, IntT len));
extern IntT F_StrCat FARGS((StringT s1, ConStringT s2));
extern IntT F_StrCatN FARGS((StringT s1, ConStringT s2, IntT n));
extern IntT F_StrNCatN FARGS((StringT s1, ConStringT s2, IntT m, IntT n));
extern IntT F_StrCatCharN FARGS((StringT s, PUCharT c, IntT n));
extern IntT F_StrCatIntN FARGS((StringT s, IntT i, IntT n));
extern BoolT F_StrPrefix FARGS((ConStringT s1, ConStringT s2));
extern BoolT F_StrPrefixN FARGS((ConStringT s1, ConStringT s2, IntT n));
extern BoolT F_StrSuffix FARGS((ConStringT s1, ConStringT s2));
extern VoidT F_StrStripTrailingSpaces FARGS((StringT s));
extern VoidT F_StrStripLeadingSpaces FARGS((StringT s));
extern VoidT F_StrReverse FARGS((StringT s, IntT l));
extern UCharT *F_StrChr FARGS((StringT s, PUCharT c));
extern UCharT *F_StrRChr FARGS((StringT s, PUCharT c));
extern BoolT F_StrIEqual FARGS((ConStringT s1, ConStringT s2));
extern BoolT F_StrSIEqual FARGS((ConStringT s1, ConStringT s2));
extern NativeIntT F_StrQsortCmp FARGS((ConStringT *sp1, ConStringT *sp2));
extern BoolT F_StrIPrefix FARGS((ConStringT s1, ConStringT s2));
extern VoidT F_StrStrip FARGS((StringT s, ConStringT strip));
extern IntT F_StrSubString FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrAlphaToInt FARGS((ConStringT string));
extern PRealT F_StrAlphaToReal FARGS((ConStringT string));
extern BoolT F_StrISuffix FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrCmp FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrICmp FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrMCmp FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrICmpN FARGS((ConStringT s1, ConStringT s2, IntT n));
extern IntT F_StrCmpN FARGS((ConStringT s1, ConStringT s2, IntT n));
extern StringT F_StrTok FARGS((StringT s1, ConStringT s2));
extern StringT F_StrBrk FARGS((StringT s1, ConStringT s2));
/*
 * F_LanguageString returns the address of a string literal:
 * don't try to modify or free the string.
 */
extern ConStringT F_LanguageString FARGS((IntT id));
extern IntT F_LanguageNumber FARGS((ConStringT s));

#ifdef  __cplusplus
}
#endif

#define F_StrIsEmpty(s)\
((s) == 0 || *(s) == 0)
#define F_StrFree(s)\
F_Free(s)
#define CharIsInString(c, s)\
(F_StrChr(s, c) != 0)

#endif /* FSTRINGS_H */
