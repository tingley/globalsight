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

#ifndef FMIFSTMT_H
#define FMIFSTMT_H

#include "fmifdata.h"

#ifdef  __cplusplus
extern "C" {
#endif

extern VoidT F_MifBegin FARGS((StringT tokenText));
extern VoidT F_MifEnd FARGS((StringT tokenText));
extern VoidT F_MifMIFFile FARGS((PRealT version));
extern VoidT F_MifString FARGS((StringT string));
extern VoidT F_MifDMargins FARGS((PRealT l, PRealT t, PRealT r, PRealT b, IntT unit));
extern VoidT F_MifDColumns FARGS((IntT DColumns));
extern VoidT F_MifDColumnGap FARGS((PRealT DColumnGap, IntT unit));
extern VoidT F_MifDPageSize FARGS((PRealT w, PRealT h, IntT unit));
extern VoidT F_MifDStartPage FARGS((IntT DStartPage));
extern VoidT F_MifDTwoSides FARGS((BoolT DTwoSides));
extern VoidT F_MifDParity FARGS((IntT DParity));
extern VoidT F_MifDPageNumStyle FARGS((IntT DPageNumStyle));
extern VoidT F_MifDFNotePgfTag FARGS((StringT DFNotePgfTag));
extern VoidT F_MifDFNoteMaxH FARGS((PRealT DFNoteMaxH, IntT unit));
extern VoidT F_MifDFNoteNumStyle FARGS((IntT DFNoteNumStyle));
extern VoidT F_MifDFNoteLabels FARGS((StringT DFNoteLabels));
extern VoidT F_MifDFNotesNumbering FARGS((IntT DFNotesNumbering));
extern VoidT F_MifDFNoteNumberPos FARGS((IntT DFNoteNumberPos));
extern VoidT F_MifDPageRounding FARGS((IntT DPageRounding));
extern VoidT F_MifDLinebreakChars FARGS((StringT DLinebreakChars));
extern VoidT F_MifPgfTag FARGS((StringT tagStyleName));
extern VoidT F_MifPgfUseNextTag FARGS((BoolT pgfUseNextTag));
extern VoidT F_MifPgfNextTag FARGS((StringT pgfNextTag));
extern VoidT F_MifPgfLIndent FARGS((PRealT pgfLIndent, IntT unit));
extern VoidT F_MifPgfFIndent FARGS((PRealT pgfFIndent, IntT unit));
extern VoidT F_MifPgfRIndent FARGS((PRealT pgfRIndent, IntT unit));
extern VoidT F_MifPgfAlignment FARGS((IntT pgfAlignment));
extern VoidT F_MifPgfTopSeparator FARGS((StringT pgfTopSeparator));
extern VoidT F_MifPgfBotSeparator FARGS((StringT pgfBotSeparator));
extern VoidT F_MifPgfPlacement FARGS((IntT pgfPlacement));
extern VoidT F_MifPgfSpBefore FARGS((PRealT pgfSpBefore, IntT unit));
extern VoidT F_MifPgfSpAfter FARGS((PRealT pgfSpAfter, IntT unit));
extern VoidT F_MifPgfWithNext FARGS((BoolT pgfWithNext));
extern VoidT F_MifPgfBlockSize FARGS((IntT pgfBlockSize));
extern VoidT F_MifPgfAutoNum FARGS((BoolT pgfAutoNum));
extern VoidT F_MifPgfNumberFont FARGS((StringT fTag));
extern VoidT F_MifPgfNumFormat FARGS((StringT pgfNumFormat));
extern VoidT F_MifPgfNumString FARGS((StringT pgfNumString));
extern VoidT F_MifPgfLineSpacing FARGS((IntT pgfLineSpacing));
extern VoidT F_MifPgfLeading FARGS((PRealT pgfLeading, IntT unit));
extern VoidT F_MifPgfNumTabs FARGS((IntT pgfNumTabs));
extern VoidT F_MifTSX FARGS((PRealT tsx, IntT unit));
extern VoidT F_MifTSType FARGS((IntT tsType));
extern VoidT F_MifTSLeaderStr FARGS((StringT tsLeader));
extern VoidT F_MifTSDecimalChar FARGS((IntT decimalChar));
extern VoidT F_MifPgfHyphenate FARGS((BoolT pgfHyphenate));
extern VoidT F_MifHyphenMaxLines FARGS((IntT hyphenMaxLines));
extern VoidT F_MifHyphenMinPrefix FARGS((IntT hyphenMinPrefix));
extern VoidT F_MifHyphenMinSuffix FARGS((IntT hyphenMinSuffix));
extern VoidT F_MifHyphenMinWord FARGS((IntT hyphenMinWord));
extern VoidT F_MifHyphenQuality FARGS((StringT hyphenQuality));
extern VoidT F_MifPgfLetterSpace FARGS((BoolT pgfLetterSpace));
extern VoidT F_MifPgfMinWordSpace FARGS((IntT pgfMinWordSpace));
extern VoidT F_MifPgfOptWordSpace FARGS((IntT pgfOptWordSpace));
extern VoidT F_MifPgfMaxWordSpace FARGS((IntT pgfMaxWordSpace));
extern VoidT F_MifPgfLanguage FARGS((IntT pgfLanguage));
extern VoidT F_MifPgfCellAlignment FARGS((IntT valign));
extern VoidT F_MifPgfCellMargins FARGS((PRealT l, PRealT t, PRealT r, PRealT b, IntT unit));
extern VoidT F_MifFTag FARGS((StringT fTag));
extern VoidT F_MifFFamily FARGS((StringT fFamily));
extern VoidT F_MifFVar FARGS((StringT fVar));
extern VoidT F_MifFWeight FARGS((StringT fWeight));
extern VoidT F_MifFAngle FARGS((StringT fAngle));
extern VoidT F_MifFSize FARGS((PRealT fSize, IntT unit));
extern VoidT F_MifFUnderline FARGS((BoolT fUnderline));
extern VoidT F_MifFStrike FARGS((BoolT fstrike));
extern VoidT F_MifFSupScript FARGS((BoolT fSupScript));
extern VoidT F_MifFSubScript FARGS((BoolT fSubScript));
extern VoidT F_MifFSmallCaps FARGS((BoolT fsmallcaps));
extern VoidT F_MifFCaps FARGS((BoolT fcaps));
extern VoidT F_MifFChangeBar FARGS((BoolT fChangeBar));
extern VoidT F_MifFOutline FARGS((BoolT foutline));
extern VoidT F_MifFShadow FARGS((BoolT fshadow));
extern VoidT F_MifFPairKern FARGS((BoolT fPairKern));
extern VoidT F_MifFPlain FARGS((BoolT fPlain));
extern VoidT F_MifFBold FARGS((BoolT fbold));
extern VoidT F_MifFItalic FARGS((BoolT fitalic));
extern VoidT F_MifFDX FARGS((PRealT fDX, IntT unit));
extern VoidT F_MifFDY FARGS((PRealT fDY, IntT unit));
extern VoidT F_MifFDW FARGS((PRealT fDW, IntT unit));
extern VoidT F_MifFLocked FARGS((BoolT fLocked));
extern VoidT F_MifFSeparation FARGS((IntT fSeparation));
extern VoidT F_MifFlipLR FARGS((BoolT flipLR));
extern VoidT F_MifPen FARGS((IntT pen));
extern VoidT F_MifFill FARGS((IntT fill));
extern VoidT F_MifPenWidth FARGS((PRealT penWidth));
extern VoidT F_MifSeparation FARGS((IntT layer));
extern VoidT F_MifAngle FARGS((IntT angle));
extern VoidT F_MifBRect FARGS((PRealT l, PRealT t, PRealT w, PRealT h, IntT unit));
extern VoidT F_MifBeginTextFlow FARGS((StringT flowTag, BoolT autoConnect));
extern VoidT F_MifEndTextFlow FARGS((VoidT));
extern VoidT F_MifTextRectID FARGS((IntT textRectID));
extern VoidT F_MifID FARGS((IntT id));
extern VoidT F_MifPageSize FARGS((PRealT w, PRealT h, IntT unit));
extern VoidT F_MifPageType FARGS((IntT pageType));
extern VoidT F_MifPageTag FARGS((StringT pageTag));
extern VoidT F_MifPageOrientation FARGS((IntT pageOrientation));
extern VoidT F_MifPageNum FARGS((IntT pageNum));
extern VoidT F_MifPageBackground FARGS((StringT pageTag));
extern VoidT F_MifTag FARGS((StringT tag));
extern VoidT F_MifFrameType FARGS((IntT frameType));
extern VoidT F_MifHLine FARGS((IntT pen, PRealT penWidth, PRealT x, PRealT y, PRealT length));
extern VoidT F_MifNumPoints FARGS((IntT n));
extern VoidT F_MifPoint FARGS((PRealT x, PRealT y, IntT unit));
extern VoidT F_MifTLOrigin FARGS((PRealT x, PRealT y, IntT unit));
extern VoidT F_MifTLAlignment FARGS((IntT align));
extern VoidT F_MifChar FARGS((IntT text));
extern VoidT F_MifVariable FARGS((StringT text));
extern VoidT F_MifUnits FARGS((IntT u));
extern VoidT F_MifVerbose FARGS((BoolT verbose));
extern VoidT F_MifSmoothed FARGS((BoolT smoothed));
extern StringT F_MifGetUnit FARGS((IntT u));
extern StringT F_MifGetValueString FARGS((IntT u));
extern ChannelT F_MifSetOutputChannel FARGS((ChannelT chan));
extern ChannelT  F_MifGetOutputChannel FARGS((VoidT));
extern VoidT F_MifSetIndent FARGS((IntT indent));
extern IntT F_MifGetIndent FARGS((VoidT));
extern VoidT F_MifText FARGS((StringT textPtr));
extern VoidT F_MifSpace FARGS((VoidT));
extern VoidT F_MifTab FARGS((VoidT));
extern VoidT F_MifNewLine FARGS((VoidT));
extern VoidT F_MifInteger FARGS((IntT n));
extern VoidT F_MifDecimal FARGS((PRealT d, IntT n, IntT u));
extern VoidT F_MifTextString FARGS((StringT string));
extern VoidT F_MifComment FARGS((StringT textPtr));
extern VoidT F_MifIndent FARGS((VoidT));
extern IntT F_MifIndentInc FARGS((VoidT));
extern IntT F_MifIndentDec FARGS((VoidT));

#ifdef  __cplusplus
}
#endif

#endif  /* FMIFSTMT_H */
