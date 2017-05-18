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

/*
 * Data structures used inside of MIF structures.
 */
typedef struct MifWHStruct {
	RealT	H;
	RealT	W;
} MifWHStructT;

typedef struct MifXYStruct {
	RealT	X;
	RealT	Y;
} MifXYStructT;

typedef struct MifLTRBStruct {
	RealT	L;
	RealT	T;
	RealT	R;
	RealT	B;
} MifLTRBStructT;

typedef struct MifLTWHStruct {
	RealT	L;
	RealT	T;
	RealT	W;
	RealT	H;
} MifLTWHStructT;


typedef struct MifXYWHStruct {
	IntT	X;
	IntT	Y;
	IntT	W;
	IntT 	H;
} MifXYWHStructT;

typedef struct MifFacetStruct {
	struct MifFacetStruct 	*next;
	CharT		*name;
	IntT		datalen;
	IntT		datasizealloced;
	UCharT		*data;
} MifFacetStructT;


/* enum type of units. MIFUend marks the end of the enum list */
typedef enum {
	MIFUnitDef=0, MIFUnitIn=1, MIFUnitCm, MIFUnitMm=3, MIFUnitPica=4,
	MIFUnitPt=5, MIFUnitDd=6, MIFUnitCc=7, MIFUnitEnd=8
} MifUnitT;

/****************************************************************************
  decimal precision
*/
#define MIFPrecision	4


/*
 * define macros for the structures 
 */

#define MifWHGetW(x) ((x)->W)
#define MifWHGetH(x) ((x)->H)

#define MifXYGetX(x) ((x)->X)
#define MifXYGetY(x) ((x)->Y)

#define MifLTRBGetL(x) ((x)->L)
#define MifLTRBGetT(x) ((x)->T)
#define MifLTRBGetB(x) ((x)->B)
#define MifLTRBGetR(x) ((x)->R)

#define MifLTWHGetL(x) ((x)->L)
#define MifLTWHGetT(x) ((x)->T)
#define MifLTWHGetW(x) ((x)->W)
#define MifLTWHGetH(x) ((x)->H)

#define MifXYWHGetX(x) ((x)->X)
#define MifXYWHGetY(x) ((x)->Y)
#define MifXYWHGetW(x) ((x)->W)
#define MifXYWHGetH(x) ((x)->H)

#define MifWHSetW(x, y) ((x)->W = (y))
#define MifWHSetH(x, y) ((x)->H = (y))

#define MifXYSetX(x, y) ((x)->X = (y))
#define MifXYSetY(x, y) ((x)->Y = (y))

#define MifLTRBSetL(x, y) ((x)->L = (y))
#define MifLTRBSetT(x, y) ((x)->T = (y))
#define MifLTRBSetB(x, y) ((x)->B = (y))
#define MifLTRBSetR(x, y) ((x)->R = (y))

#define MifLTWHSetL(x, y) ((x)->L = (y))
#define MifLTWHSetT(x, y) ((x)->T = (y))
#define MifLTWHSetW(x, y) ((x)->W = (y))
#define MifLTWHSetH(x, y) ((x)->H = (y))

#define MifXYWHSetX(x, y) ((x)->X = (y))
#define MifXYWHSetY(x, y) ((x)->Y = (y))
#define MifXYWHSetW(x, y) ((x)->W = (y))
#define MifXYWHSetH(x, y) ((x)->H = (y))

/*
 * Define all the MIF tokens. 
 * These values are defined to match the array of mif string values
 */ 
#define MIFNo                       0
#define MIFYes                      1
#define MIFCShown                   2
#define MIFChidden                  3
#define MIFCAsIs                    4
#define MIFCUnderline               5
#define MIFCStrike                  6
#define MIFCOverline                7
#define MIFLeft                     8
#define MIFCenter                   9
#define MIFRight                    10
#define MIFDecimal                  11
#define MIFLeftRight                12
#define MIFAnywhere                 13
#define MIFColumnTop                14
#define MIFPageTop                  15
#define MIFLPageTop                 16
#define MIFRPageTop                 17
#define MIFFixed                    18
#define MIFProportional             19
#define MIFFloating                 20
#define MIFUSEnglish                21
#define MIFUKEnglish                22
#define MIFFrench                   23
#define MIFDutch                    24
#define MIFGerman                   25
#define MIFItalian                  26
#define MIFSpanish                  27
#define MIFSwedish                  28
#define MIFNorwegian                29
#define MIFPortuguese               30
#define MIFBrazilian                31
#define MIFDanish                   32
#define MIFFinnish                  33
#define MIFNoLanguage               34
#define MIFUin                      35
#define MIFUcm                      36
#define MIFUmm                      37
#define MIFUpica                    38
#define MIFUpt                      39
#define MIFUdd                      40
#define MIFUcc                      41
#define MIFFloat                    42
#define MIFInHeader                 43
#define MIFInFooter                 44
#define MIFNone                     45
#define MIFFirstLeft                46
#define MIFFirstRight               47
#define MIFDeleteEmptyPages         48
#define MIFMakePageCountEven        49
#define MIFMakePageCountOdd         50
#define MIFDontChangePageCount      51
#define MIFPerPage                  52
#define MIFPerFlow                  53
#define MIFArabic                   54
#define MIFUCRoman                  55
#define MIFLCRoman                  56
#define MIFUCAlpha                  57
#define MIFLCAlpha                  58
#define MIFCustom                   59
#define MIFFNSuperscript            60
#define MIFFDBaseline               61
#define MIFFNSubscript              62
#define MIFLeftOfCol                63
#define MIFRightOfCol               64
#define MIFNearestEdge              65
#define MIFFurthestEdge             66
#define MIFTOC                      67
#define MIFLOF                      68
#define MIFLOT                      69
#define MIFLOP                      70
#define MIFLOM                      71
#define MIFAML                      72
#define MIFAPL                      73
#define MIFIDX                      74
#define MIFIOA                      75
#define MIFIOS                      76
#define MIFIOM                      77
#define MIFLR                       78
#define MIFStick                    79
#define MIFHollow                   80
#define MIFFilled                   81
#define MIFArrowHead                82
#define MIFButt                     83
#define MIFRound                    84
#define MIFSquare                   85
#define MIFTab                      86
#define MIFHardSpace                87
#define MIFSoftHyphen               88
#define MIFDiscHyphen               89
#define MIFNoHyphen                 90
#define MIFMathLarge                91
#define MIFMathMedium               92
#define MIFMathSmall                93
#define MIFNotAnchored              94
#define MIFInline                   95
#define MIFTop                      96
#define MIFMiddle                   97
#define MIFBelow                    98
#define MIFBottom                   99
#define MIFNear                     100
#define MIFFar                      101
#define MIFLeftMasterPage           102
#define MIFRightMasterPage          103
#define MIFOtherMasterPage          104
#define MIFReferencePage            105
#define MIFBodyPage                 106
#define MIFHiddenPage               107
#define MIFPortrait                 108
#define MIFLandscape                109
#define MIFCent                     110
#define MIFPound                    111
#define MIFYen                      112
#define MIFEnDash                   113
#define MIFEmDash                   114
#define MIFDagger                   115
#define MIFDoubleDagger             116
#define MIFBullet                   117
#define MIFHardReturn               118
#define MIFEndOfPara                119
#define MIFEndOfFlow                120
#define MIFNumberSpace              121
#define MIFThinSpace                122
#define MIFEnSpace                  123
#define MIFEmSpace                  124
#define MIFHardHyphen               125
#define MIFCanadianFrench	126	
#define MIFSwissGerman		127
#define MIFCatalan		128
#define MIFNynorsk		129

