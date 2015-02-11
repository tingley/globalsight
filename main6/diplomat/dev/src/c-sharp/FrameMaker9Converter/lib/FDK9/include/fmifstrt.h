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

typedef struct MifConditionStruct {
	StringT				CTag;
	IntT				CState;
	IntT				CStyle;
	IntT				CSeparation;
} MifConditionStructT;

typedef struct MifConditionCatalogStruct {
	MifConditionStructT		*Condition;
} MifConditionCatalogStructT;

typedef struct MifFontStruct {
	StringT				FTag;
	StringT				FFamily;
	StringT				FVar;
	StringT				FWeight;
	StringT				FAngle;
	RealT				FSize;
	IntT				FUnderline;
	IntT				FOverline;
	IntT				FStrike;
	IntT				FSupScript;
	IntT				FSubScript;
	IntT				FChangeBar;
	IntT				FOutline;
	IntT				FShadow;
	IntT				FPairKern;
	IntT				FDoubleUnderline;
	IntT				FNumericUnderline;
	RealT				FDX;
	RealT				FDY;
	RealT				FDW;
	IntT				FSeparation;
	IntT				FPlain;
	IntT				FBold;
	IntT				FItalic;
	StringT				FColor;
} MifFontStructT;

typedef struct MifPgfFontStruct {
	StringT				FTag;
	StringT				FFamily;
	StringT				FVar;
	StringT				FWeight;
	StringT				FAngle;
	RealT				FSize;
	IntT				FUnderline;
	IntT				FOverline;
	IntT				FStrike;
	IntT				FSupScript;
	IntT				FSubScript;
	IntT				FChangeBar;
	IntT				FOutline;
	IntT				FShadow;
	IntT				FPairKern;
	IntT				FDoubleUnderline;
	IntT				FNumericUnderline;
	RealT				FDX;
	RealT				FDY;
	RealT				FDW;
	IntT				FSeparation;
	IntT				FPlain;
	IntT				FBold;
	IntT				FItalic;
	StringT				FColor;
} MifPgfFontStructT;

typedef struct MifXRefStruct {
	StringT				XRefName;
	StringT				XRefSrcText;
	StringT				XRefSrcFile;
} MifXRefStructT;

typedef struct MifMarkerStruct {
	IntT				MType;
	StringT				MText;
	IntT				MCurrPage;
} MifMarkerStructT;

typedef struct MifConditionalStruct {
	struct MifInConditionStructL	*InCondition;
} MifConditionalStructT;

typedef struct MifInConditionStructL {
	struct MifInConditionStructL	*Next;
	void				*Prev;
	StringT				*InCondition;
} MifInConditionStructLT;

typedef struct MifVariableStruct {
	StringT				VariableName;
} MifVariableStructT;

typedef struct MifParaLineStruct {
	IntT				TextRectID;
	IntT				SpclHyphenation;
	void				*LineItems;
} MifParaLineStructT;

typedef struct MifUnconditionalStructL {
	struct MifUnconditionalStructL	*Next;
	void				*Prev;
} MifUnconditionalStructLT;

typedef struct MifStringStructL {
	struct MifStringStructL		*Next;
	void				*Prev;
	StringT				*String;
} MifStringStructLT;

typedef struct MifCharStructL {
	struct MifCharStructL		*Next;
	void				*Prev;
	IntT				Char;
} MifCharStructLT;

typedef struct MifATblStructL {
	struct MifATblStructL		*Next;
	void				*Prev;
	IntT				ATbl;
} MifATblStructLT;

typedef struct MifAFrameStructL {
	struct MifAFrameStructL		*Next;
	void				*Prev;
	IntT				AFrame;
} MifAFrameStructLT;

typedef struct MifFNoteStructL {
	struct MifFNoteStructL		*Next;
	void				*Prev;
	IntT				FNote;
} MifFNoteStructLT;

typedef struct MifXRefEndStructL {
	struct MifXRefEndStructL	*Next;
	void				*Prev;
} MifXRefEndStructLT;

typedef struct MifTabStopStruct {
	RealT				TSX;
	IntT				TSType;
	StringT				TSLeaderStr;
	IntT				TSDecimalChar;
} MifTabStopStructT;

typedef struct MifPgfStruct {
	StringT				PgfTag;
	IntT				PgfUseNextTag;
	StringT				PgfNextTag;
	IntT				PgfAlignment;
	RealT				PgfFIndent;
	RealT				PgfLIndent;
	RealT				PgfRIndent;
	StringT				PgfTopSeparator;
	StringT				PgfBotSeparator;
	IntT				PgfPlacement;
	RealT				PgfSpBefore;
	RealT				PgfSpAfter;
	IntT				PgfWithPrev;
	IntT				PgfWithNext;
	IntT				PgfBlockSize;
	MifPgfFontStructT		*PgfFont;
	IntT				PgfLineSpacing;
	RealT				PgfLeading;
	IntT				PgfAutoNum;
	StringT				PgfNumFormat;
	StringT				PgfNumberFont;
	IntT				PgfNumAtEnd;
	IntT				PgfNumTabs;
	MifTabStopStructT		*TabStop;
	IntT				PgfHyphenate;
	IntT				HyphenMaxLines;
	IntT				HyphenMinPrefix;
	IntT				HyphenMinSuffix;
	IntT				HyphenMinWord;
	IntT				PgfLetterSpace;
	IntT				PgfMinWordSpace;
	IntT				PgfOptWordSpace;
	IntT				PgfMaxWordSpace;
	IntT				PgfLanguage;
	IntT				PgfCellAlignment;
	MifLTRBStructT			PgfCellMargins;
	IntT				PgfCellLMarginFixed;
	IntT				PgfCellTMarginFixed;
	IntT				PgfCellRMarginFixed;
	IntT				PgfCellBMarginFixed;
} MifPgfStructT;

typedef struct MifPgfCatalogStruct {
	IntT				Units;
	IntT				Verbose;
	MifPgfStructT			*Pgf;
} MifPgfCatalogStructT;

typedef struct MifFontCatalogStruct {
	IntT				Units;
	IntT				Verbose;
	MifFontStructT			*Font;
} MifFontCatalogStructT;

typedef struct MifRulingStruct {
	StringT				RulingTag;
	RealT				RulingPenWidth;
	RealT				RulingGap;
	IntT				RulingSeparation;
	IntT				RulingPen;
	IntT				RulingLines;
} MifRulingStructT;

typedef struct MifRulingCatalogStruct {
	MifRulingStructT		*Ruling;
} MifRulingCatalogStructT;

typedef struct MifTblColumnHStruct {
	StringT				PgfTag;
	MifPgfStructT			*Pgf;
} MifTblColumnHStructT;

typedef struct MifTblColumnBodyStruct {
	StringT				PgfTag;
	MifPgfStructT			*Pgf;
} MifTblColumnBodyStructT;

typedef struct MifTblColumnFStruct {
	StringT				PgfTag;
	MifPgfStructT			*Pgf;
} MifTblColumnFStructT;

typedef struct MifTblColumnStruct {
	IntT				TblColumnNum;
	RealT				TblColumnWidth;
	MifTblColumnHStructT		*TblColumnH;
	MifTblColumnBodyStructT		*TblColumnBody;
	MifTblColumnFStructT		*TblColumnF;
} MifTblColumnStructT;

typedef struct MifTblTitlePgf1Struct {
	StringT				PgfTag;
	MifPgfStructT			*Pgf;
} MifTblTitlePgf1StructT;

typedef struct MifTblFormatStruct {
	StringT				TblTag;
	RealT				TblWidth;
	MifTblColumnStructT		*TblColumn;
	MifLTRBStructT			TblCellMargins;
	RealT				TblLIndent;
	RealT				TblRIndent;
	IntT				TblAlignment;
	IntT				TblPlacement;
	RealT				TblSpBefore;
	RealT				TblSpAfter;
	IntT				TblBlockSize;
	IntT				TblHFFill;
	IntT				TblHFSeparation;
	IntT				TblBodyFill;
	IntT				TblBodySeparation;
	IntT				TblShadeByColumn;
	IntT				TblShadePeriod;
	IntT				TblXFill;
	IntT				TblXSeparation;
	IntT				TblAltShadePeriod;
	StringT				TblLRuling;
	StringT				TblBRuling;
	StringT				TblRRuling;
	StringT				TblTRuling;
	StringT				TblColumnRuling;
	IntT				TblXColumnNum;
	StringT				TblXColumnRuling;
	StringT				TblBodyRowRuling;
	StringT				TblXRowRuling;
	IntT				TblRulingPeriod;
	StringT				TblHFRowRuling;
	StringT				TblSeparatorRuling;
	IntT				TblLastBRuling;
	IntT				TblTitlePlacement;
	MifTblTitlePgf1StructT		*TblTitlePgf1;
	RealT				TblTitleGap;
	IntT				TblInitNumColumns;
	IntT				TblInitNumHRows;
	IntT				TblInitNumBodyRows;
	IntT				TblInitNumFRows;
	IntT				TblNumByColumn;
} MifTblFormatStructT;

typedef struct MifTblCatalogStruct {
	MifTblFormatStructT		*TblFormat;
} MifTblCatalogStructT;

typedef struct MifVariableFormatStruct {
	StringT				VariableName;
	StringT				VariableDef;
} MifVariableFormatStructT;

typedef struct MifVariableFormatsStruct {
	MifVariableFormatStructT	*VariableFormat;
} MifVariableFormatsStructT;

typedef struct MifXRefFormatStruct {
	StringT				XRefName;
	StringT				XRefDef;
} MifXRefFormatStructT;

typedef struct MifXRefFormatsStruct {
	MifXRefFormatStructT		*XRefFormat;
} MifXRefFormatsStructT;

typedef struct MifDocumentStruct {
	MifXYWHStructT			DWindowRect;
	MifWHStructT			DPageSize;
	IntT				DStartPage;
	IntT				DPageNumStyle;
	IntT				DPagePointStyle;
	IntT				DTwoSides;
	IntT				DParity;
	IntT				DPageRounding;
	IntT				DFrozenPages;
	IntT				DSmartQuotesOn;
	IntT				DSmartSpacesOn;
	StringT				DLinebreakChars;
	RealT				DMaxInterLine;
	RealT				DMaxInterPgf;
	IntT				DShowAllConditions;
	IntT				DDisplayOverrides;
	StringT				DFNoteTag;
	RealT				DFNoteMaxH;
	IntT				DFNoteRestart;
	IntT				FNoteStartNum;
	IntT				DFNoteNumStyle;
	StringT				DFNoteLabels;
	IntT				DFNoteAnchorPos;
	IntT				DFNoteNumberPos;
	StringT				DFNoteAnchorPrefix;
	StringT				DFNoteAnchorSuffix;
	StringT				DFNoteNumberPrefix;
	StringT				DFNoteNumberSuffix;
	StringT				DTblFNoteTag;
	StringT				DTblFNoteLabels;
	IntT				DTblFNoteNumStyle;
	IntT				DTblFNoteAnchorPos;
	IntT				DTblFNoteNumberPos;
	StringT				DTblFNoteAnchorPrefix;
	StringT				DTblFNoteAnchorSuffix;
	StringT				DTblFNoteNumberPrefix;
	StringT				DTblFNoteNumberSuffix;
	RealT				DChBarGap;
	RealT				DChBarWidth;
	IntT				DChBarPosition;
	IntT				DAutoChBars;
	IntT				DGridOn;
	IntT				DRulersOn;
	IntT				DBordersOn;
	IntT				DSymbolsOn;
	IntT				DViewOnly;
	IntT				DGraphicsOff;
	IntT				DLanguage;
	IntT				DMathItalicFunctionName;
	IntT				DMathItalicOtherText;
	StringT				DMathAlphaCharFontFamily;
	RealT				DMathSmallIntegral;
	RealT				DMathMediumIntegral;
	RealT				DMathLargeIntegral;
	RealT				DMathSmallSigma;
	RealT				DMathMediumSigma;
	RealT				DMathLargeSigma;
	RealT				DMathSmallLevel1;
	RealT				DMathMediumLevel1;
	RealT				DMathLargeLevel1;
	RealT				DMathSmallLevel2;
	RealT				DMathMediumLevel2;
	RealT				DMathLargeLevel2;
	RealT				DMathSmallLevel3;
	RealT				DMathMediumLevel3;
	RealT				DMathLargeLevel3;
} MifDocumentStructT;

typedef struct MifBookComponentStruct {
	StringT				FileName;
	StringT				FileNameSuffix;
	IntT				DeriveType;
	StringT				DeriveTag;
} MifBookComponentStructT;

typedef struct MifParaStruct {
	StringT				PgfTag;
	MifPgfStructT			*Pgf;
	StringT				PgfNumString;
	IntT				PgfEndCond;
	IntT				PgfCondFullPgf;
	MifParaLineStructT		*ParaLine;
	IntT				Units;
	IntT				Verbose;
} MifParaStructT;

typedef struct MifFNoteStruct {
	IntT				ID;
	MifParaStructT			*Para;
} MifFNoteStructT;

typedef struct MifNotesStruct {
	MifFNoteStructT			*FNote;
} MifNotesStructT;

typedef struct MifTextFlowStruct {
	StringT				TFTag;
	IntT				TFAutoConnect;
	IntT				TFPostScript;
	IntT				TFFeather;
	IntT				TFSynchronized;
	RealT				TFLineSpacing;
	RealT				TFMinHangHeight;
	MifNotesStructT			*Notes;
	MifParaStructT			*Para;
	IntT				Units;
	IntT				Verbose;
} MifTextFlowStructT;

typedef struct MifCellContentStruct {
	MifNotesStructT			*Notes;
	MifParaStructT			*Para;
} MifCellContentStructT;

typedef struct MifCellStruct {
	IntT				CellFill;
	IntT				CellSeparation;
	StringT				CellLRuling;
	StringT				CellBRuling;
	StringT				CellRRuling;
	StringT				CellTRuling;
	IntT				CellColumns;
	IntT				CellRows;
	IntT				CellAffectsColumnWidthA;
	IntT				CellAngle;
	MifCellContentStructT		*CellContent;
} MifCellStructT;

typedef struct MifRowStruct {
	MifConditionalStructT		*Conditional;
	IntT				RowWithNext;
	IntT				RowWithPrev;
	RealT				RowMinHeight;
	RealT				RowMaxHeight;
	RealT				RowHeight;
	IntT				RowPlacement;
	MifCellStructT			*Cell;
} MifRowStructT;

typedef struct MifTblHStruct {
	MifRowStructT			*Row;
} MifTblHStructT;

typedef struct MifTblBodyStruct {
	MifRowStructT			*Row;
} MifTblBodyStructT;

typedef struct MifTblFStruct {
	MifRowStructT			*Row;
} MifTblFStructT;

typedef struct MifTblTitleContentStruct {
	MifNotesStructT			*Notes;
	MifParaStructT			*Para;
} MifTblTitleContentStructT;

typedef struct MifTblStruct {
	IntT				TblID;
	StringT				TblTag;
	MifTblFormatStructT		*TblFormat;
	IntT				TblNumColumns;
	struct MifTblColumnWidthStructL	*TblColumnWidth;
	MifTblTitleContentStructT	*TblTitleContent;
	MifTblHStructT			*TblH;
	MifTblBodyStructT		*TblBody;
	MifTblFStructT			*TblF;
} MifTblStructT;

typedef struct MifTblColumnWidthStructL {
	struct MifTblColumnWidthStructL	*Next;
	void				*Prev;
	RealT				TblColumnWidth;
} MifTblColumnWidthStructLT;

typedef struct MifTblsStruct {
	MifTblStructT			*Tbl;
} MifTblsStructT;

typedef struct MifArrowStyleStruct {
	IntT				TipAngle;
	IntT				BaseAngle;
	RealT				Length;
	IntT				HeadType;
	IntT				ScaleHead;
	RealT				ScaleFactor;
} MifArrowStyleStructT;

typedef struct MifGenericObjectStruct {
	IntT				ID;
	IntT				GroupID;
	IntT				Pen;
	IntT				Fill;
	RealT				PenWidth;
	IntT				Separation;
	IntT				Units;
	IntT				Verbose;
	IntT				ObAngle;
	IntT				ReRotateAngle;
	StringT				ObColor;
	IntT				Overprint;
} MifGenericObjectStructT;

typedef struct MifArcStruct {
	MifGenericObjectStructT		*GenericObject;
	IntT				HeadCap;
	IntT				TailCap;
	MifArrowStyleStructT		*ArrowStyle;
	MifLTWHStructT			ArcRect;
	IntT				ArcTheta;
	IntT				ArcDTheta;
} MifArcStructT;

typedef struct MifEllipseStruct {
	MifGenericObjectStructT		*GenericObject;
	MifLTWHStructT			BRect;
} MifEllipseStructT;

typedef struct MifPolygonStruct {
	MifGenericObjectStructT		*GenericObject;
	IntT				Smoothed;
	IntT				NumPoints;
	struct MifPointStructL		*Point;
} MifPolygonStructT;

typedef struct MifPointStructL {
	struct MifPointStructL		*Next;
	void				*Prev;
	MifXYStructT			Point;
} MifPointStructLT;

typedef struct MifPolyLineStruct {
	MifGenericObjectStructT		*GenericObject;
	IntT				HeadCap;
	IntT				TailCap;
	MifArrowStyleStructT		*ArrowStyle;
	IntT				Smoothed;
	IntT				NumPoints;
	struct MifPointStructL		*Point;
} MifPolyLineStructT;

typedef struct MifRectangleStruct {
	MifGenericObjectStructT		*GenericObject;
	MifLTWHStructT			BRect;
	IntT				Smoothed;
} MifRectangleStructT;

typedef struct MifTextLineStruct {
	MifGenericObjectStructT		*GenericObject;
	MifXYStructT			TLOrigin;
	IntT				TLAlignment;
	IntT				Angle;
	void				*LineItems;
} MifTextLineStructT;

typedef struct MifTextRectStruct {
	MifGenericObjectStructT		*GenericObject;
	IntT				Angle;
	MifLTWHStructT			BRect;
	IntT				TRNext;
} MifTextRectStructT;

typedef struct MifRoundRectStruct {
	MifGenericObjectStructT		*GenericObject;
	MifLTWHStructT			BRect;
	RealT				Radius;
} MifRoundRectStructT;

typedef struct MifMathStruct {
	IntT				GroupID;
	IntT				Separation;
	MifLTWHStructT			BRect;
	StringT				MathFullForm;
	MifXYStructT			MathOrigin;
	IntT				MathAlignment;
	IntT				MathSize;
	IntT				Angle;
} MifMathStructT;

typedef struct MifGroupStruct {
	IntT				GroupID;
	IntT				ID;
	IntT				Angle;
} MifGroupStructT;

typedef struct MifImportObjectStruct {
	MifGenericObjectStructT		*GenericObject;
	StringT				ImportObFile;
	StringT				ImportObFileDI;
	MifLTWHStructT			BRect;
	IntT				BitMapDpi;
	IntT				Angle;
	IntT				FlipLR;
	MifFacetStructT			*Facet;
	StringT				ImportObEditor;
	StringT				ImportObUpdator;
} MifImportObjectStructT;

typedef struct MifFrameStruct {
	MifGenericObjectStructT		*GenericObject;
	MifLTWHStructT			BRect;
	IntT				FrameType;
	StringT				Tag;
	IntT				Float;
	RealT				NSOffset;
	RealT				BLOffset;
	IntT				AnchorAlign;
	IntT				Cropped;
	void				*Objects;
} MifFrameStructT;

typedef struct MifPageStruct {
	IntT				PageType;
	StringT				PageNum;
	StringT				PageTag;
	MifWHStructT			PageSize;
	IntT				PageOrientation;
	StringT				PageBackground;
	IntT				Units;
	IntT				Verbose;
	void				*Objects;
} MifPageStructT;

typedef struct MifDictionaryStruct {
	struct MifOKWordStructL		*OKWord;
} MifDictionaryStructT;

typedef struct MifOKWordStructL {
	struct MifOKWordStructL		*Next;
	void				*Prev;
	StringT				*OKWord;
} MifOKWordStructLT;

typedef struct MifAFramesStruct {
	MifFrameStructT			*Frame;
} MifAFramesStructT;

typedef struct MifMIFTopStruct {
	IntT				MIFFile;
	IntT				Verbose;
	IntT				Units;
	MifConditionCatalogStructT	*ConditionCatalog;
	MifPgfCatalogStructT		*PgfCatalog;
	MifFontCatalogStructT		*FontCatalog;
	MifRulingCatalogStructT		*RulingCatalog;
	MifTblCatalogStructT		*TblCatalog;
	MifVariableFormatsStructT	*VariableFormats;
	MifXRefFormatsStructT		*XRefFormats;
	MifBookComponentStructT		*BookComponent;
	MifDictionaryStructT		*Dictionary;
	MifTblsStructT			*Tbls;
	MifAFramesStructT		*AFrames;
	MifPageStructT			*Page;
	MifTextFlowStructT		*TextFlow;
	MifDocumentStructT		*Document;
} MifMIFTopStructT;

typedef struct MifColorStruct {
	StringT				ColorTag;
	RealT				ColorCyan;
	RealT				ColorMagenta;
	RealT				ColorYellow;
	RealT				ColorBlack;
	StringT				ColorPantoneValue;
	IntT				ColorAttribute;
} MifColorStructT;

typedef struct MifColorCatalogStruct {
	IntT				Units;
	IntT				Verbose;
	MifColorStructT			*Color;
} MifColorCatalogStructT;

