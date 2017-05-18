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

#ifndef FENCODE_H
#define FENCODE_H

#ifdef  __cplusplus
extern "C" {
#endif

typedef UIntT FontEncIdT;

/* This has some relationship to F_FontEncodingT internal to FM. */
typedef struct {
	UCharT firstBytes[256]; /* First byte character information */
	UCharT secondBytes[256]; /* Second byte character information */
	StringT name; /* The name of the encoding */
} F_FontEncT;

typedef enum {                     /* For use with F_StrConvertEnc()*/
        F_EncMakerRoman = 0,       /* EncodingRoman fdeFrameRomanEncodingId*/
        F_EncISOLatin1,            /* EncodingRoman*/
        F_EncASCII,                /* EncodingRoman*/
        F_EncANSI,                 /* EncodingRoman*/
        F_EncMacASCII,             /* EncodingRoman*/
		F_EncJIS7,                 /* EncodingJapanese*/
        F_EncShiftJIS,             /* EncodingJapanese fdeJISX0208_ShiftJISEncodingId*/
		F_EncJIS8_EUC,             /* EncodingJapanese*/
        F_EncBig5,                 /* EncodingTraditionalChinese fdeBIG5EncodingId*/
        F_EncCNS_EUC,              /* EncodingTraditionalChinese*/
        F_EncGB8_EUC,              /* EncodingSimplifiedChinese fdeGB2312_80_EUCEncodingId*/
        F_EncHZ,                   /* EncodingSimplifiedChinese*/
        F_EncKSC8_EUC,             /* EncodingKorean fdeKSC5601_1992EncodingId*/
        F_EncSpecialSymbol,        /* Symbol - only used as a from encoding*/
        F_EncSpecialZapfDingbats,  /* Dingbat - only used as a from encoding*/
        F_EncSpecialWingdings,     /* Wingdings - only used as a from encoding*/
        F_EncUTF8,                 /* Unicode - 8 */
        F_EncUTF16,                /* Unicode - 16*/
		F_EncUnknown
} FTextEncodingT;




extern F_FontEncT *fdeFontEncs[];

#define fdeFrameRomanEncodingId			0
#define fdeJISX0208_ShiftJISEncodingId	1
#define fdeKSC5601_1992EncodingId		2
#define fdeBIG5EncodingId				3
#define fdeGB2312_80_EUCEncodingId		4
#define fdeUTF8EncodingId				5
#define NUM_ENCODINGS 6
#define fdeLastEncodingId				(NUM_ENCODINGS-1)

extern FontEncIdT fdeDialogEncodingId;


#define fdeDialogEncodingIsFrameRoman (fdeDialogEncodingId == fdeFrameRomanEncodingId)

#define F_CharIsDoubleByteFirst(c, feId) (fdeFontEncs[feId]->firstBytes[c])
#define F_CharIsDoubleByteSecond(c, feId) (fdeFontEncs[feId]->secondBytes[c])
#define F_CharIsDoubleByte(c1, c2, feId) \
	(F_CharIsDoubleByteFirst(c1, feId) && F_CharIsDoubleByteSecond(c2, feId))

extern FontEncIdT F_FdeInitFontEncs FARGS((ConStringT fontEncName));
extern BoolT F_FdeEncodingsInitialized FARGS((VoidT));
extern ConStringT F_FontEncName FARGS((FontEncIdT fontEncId));
extern FontEncIdT F_FontEncId FARGS((ConStringT fontEncName));
extern StringT F_StrChrEnc FARGS((StringT s, PUCharT first, PUCharT second, FontEncIdT feId));
extern StringT F_StrRChrEnc FARGS((StringT s, PUCharT first, PUCharT second, FontEncIdT feId));
extern IntT F_StrStrEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern BoolT F_StrIEqualEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern BoolT F_StrIEqualNEnc FARGS((ConStringT s1, ConStringT s2, IntT n, FontEncIdT feId));
extern IntT F_StrICmpEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern IntT F_StrMCmpEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern IntT F_StrCmpNEnc FARGS((ConStringT s1, ConStringT s2, IntT n, FontEncIdT feId));
extern IntT F_StrICmpNEnc FARGS((ConStringT s1, ConStringT s2, IntT n, FontEncIdT feId));
extern NativeIntT F_StrQsortCmpEnc FARGS((ConStringT *s1, ConStringT *s2, FontEncIdT feId));
extern VoidT F_StrTruncEnc FARGS((StringT s, IntT n, FontEncIdT feId));
extern IntT F_StrLenEnc FARGS((ConStringT s, FontEncIdT feId));
extern IntT F_StrCatDblCharNEnc FARGS((StringT s, PUCharT first, PUCharT second, IntT n, FontEncIdT feId));
extern BoolT F_StrIPrefixEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern BoolT F_StrISuffixEnc FARGS((ConStringT s1, ConStringT s2, FontEncIdT feId));
extern IntT F_StrCatNEnc FARGS((StringT s1, ConStringT s2, IntT n, FontEncIdT feId));
extern IntT F_StrNCatNEnc FARGS((StringT s1, ConStringT s2, IntT m, IntT n, FontEncIdT feId));
extern IntT F_StrCpyNEnc FARGS((StringT s1, ConStringT s2, IntT n, FontEncIdT feId));

extern UCharT *F_StrRChrUTF8 FARGS((StringT s, const UCharT *c));
extern UCharT *F_StrChrUTF8 FARGS((StringT s, const UCharT *c));
extern IntT F_StrCmpUTF8 FARGS(( ConStringT s1,  ConStringT s2));
extern IntT F_StrMCmpUTF8 FARGS(( ConStringT s1,  ConStringT s2));
extern NativeIntT F_StrQsortCmpUTF8 FARGS((ConStringT *sp1, ConStringT *sp2));
extern IntT F_StrICmpNUTF8Char FARGS(( ConStringT s1,  ConStringT s2, IntT n));
extern BoolT F_StrIEqualNUTF8Char FARGS(( ConStringT s1,  ConStringT s2,  IntT n));
extern IntT F_StrCatUTF8CharNByte FARGS(( StringT s, const UCharT *c,  IntT n));
extern IntT F_StrNCatNUTF8Char FARGS(( StringT s1,  ConStringT s2,  IntT n,  IntT m));
extern IntT F_StrCpyNUTF8Char FARGS((StringT s1,ConStringT s2,IntT n));
extern VoidT F_StrReverseUTF8Char FARGS((StringT s, IntT l));
extern VoidT F_CharToLowerUTF8 FARGS((const UCharT *c, UCharT *newchar));
extern VoidT F_CharToUpperUTF8 FARGS((const UCharT *c, UCharT *newchar));
extern BoolT F_CharIsLowerUTF8 FARGS((const UCharT *c));
extern BoolT F_CharIsUpperUTF8 FARGS((const UCharT *c));
extern BoolT F_CharIsAlphaUTF8 FARGS((const UCharT *c));
extern BoolT F_CharIsNumericUTF8 FARGS((const UCharT *c));
extern BoolT F_CharIsAlphaNumericUTF8 FARGS((const UCharT *c));
extern BoolT F_CharIsHexadecimalUTF8 FARGS((const UCharT *c));
extern IntT F_UTF8CharSize FARGS((UCharT c));
extern const UCharT * F_UTF8NextChar FARGS((const UCharT **c));
extern IntT F_UTF16CharSize FARGS((UChar16T c));
extern const UChar16T * F_UTF16NextChar FARGS((const UChar16T **c));
extern BoolT F_IsValidUTF8 FARGS((ConStringT s));
extern IntT F_CharUTF32ToUTF8 FARGS((UChar32T src, UCharT *dest));
extern UChar32T F_CharUTF8ToUTF32 FARGS((const UCharT *src));
extern IntT F_CharUTF16ToUTF8 FARGS((const UChar16T *src, UCharT *dest));
extern IntT F_CharUTF8ToUTF16 FARGS((const UCharT *src, UChar16T *dest));
extern IntT F_CharUTF32ToUTF16 FARGS((UChar32T src, UChar16T *dest));
extern UChar32T F_CharUTF16ToUTF32 FARGS((const UChar16T *src));
extern IntT F_StrCmpUTF8Locale FARGS((ConStringT s1, ConStringT s2));
extern IntT F_StrCmpIUTF8Locale FARGS((ConStringT s1, ConStringT s2));
extern StringT F_StrConvertEnc FARGS((ConStringT in, FTextEncodingT from, FTextEncodingT to));
extern StringT F_StrConvertEnc_IgnoreControlChars FARGS((ConStringT in, FTextEncodingT from, FTextEncodingT to));
extern StringT F_StrConvertEnc_ConvertControlChars FARGS((ConStringT in, FTextEncodingT from, FTextEncodingT to));
extern VoidT F_StrStripUTF8Chars FARGS((StringT s, ConStringT strip));
extern VoidT F_StrStripUTF8String FARGS((StringT s, ConStringT strip));
extern VoidT F_StrStripUTF8Strings FARGS((StringT s, StringListT sstrip));
extern StringT F_StrBrkUTF8 FARGS((StringT s1, ConStringT s2));
extern StringT F_StrTokUTF8 FARGS((StringT s1, ConStringT s2));
extern BoolT F_IsDigit FARGS((UCharT *s));
extern IntT F_DigitValue FARGS((UCharT *s));
extern IntT F_StrAlphaToIntUnicode FARGS((ConStringT string));
extern PRealT F_StrAlphaToRealUnicode FARGS((ConStringT string));
extern IntT F_StrLenUTF16 FARGS((const UChar16T *string));
extern BoolT F_IsTrailUTF8Byte FARGS((UCharT c));
extern BoolT F_IsLeadUTF8Byte FARGS((UCharT c));
extern BoolT F_IsSingleUTF8Byte FARGS((UCharT c));
extern IntT F_TruncateToValidUTF8 FARGS((StringT str_utf8));

extern VoidT F_FdeInitUnicode FARGS((ConStringT path));

extern VoidT F_SetICUDataDir FARGS((ConStringT path));
extern ConStringT F_GetICUDataDir FARGS((VoidT));
extern FTextEncodingT F_FontEncToTextEnc FARGS((FontEncIdT feId));
extern FontEncIdT F_TextEncToFontEnc FARGS((FTextEncodingT textEnc));



#define F_CharIsEolUTF8(c)		(F_CharIsEol(*c))
#define F_CharIsControlUTF8(c)	(F_CharIsControl(*c))

#ifdef  __cplusplus
}
#endif

#endif /* FENCODE_H */
