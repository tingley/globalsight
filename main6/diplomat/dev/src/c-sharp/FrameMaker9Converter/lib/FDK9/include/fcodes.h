#ifndef FCODES_H
#define FCODES_H
/*++**********************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
* Copyright 2009 Adobe Systems Incorporated
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
***********************************************************************--*/

/* WARNING - COMMANDS GREATER THAN 4K CANNOT BE PUT INTO COMMAND TABLES.
* THEY HAVE A 4K LIMIT.
*/


/*********************************************
*
* Keyboard commands and their default keyboard assignments follow.
*
* ! is the Escape key.  Shortcuts which use !  are platform-independent.
*
* Modifiers for x-windows keyboard assignments are:
* 	^ = Control
*	~ = Meta
*	! = Escape
*	s = Shift
*/

#define NULLINPUT	-1
#define KBD_INPUT	 1

/* Please reserve 0xCX00 commands for X_WINDOWS */
/* These commands CANNOT be put into command tables since */
/* they are greater than 4k */

/* These appear in macros delimiting modal-dialog keystrokes */
#define  START_DIALOG 0xC100
#define  END_DIALOG   0xC200

/* These are used internally by Maker to delimit pushing the */
/*    focus to windows and must NOT appear in macros         */
#define  START_WINDOW 0xC400
#define  END_WINDOW   0xC500

/* This is used to get out of a document, but no map is coming */
#define  LEAVE_DOC    0xC600

/* FrameServer only - this acts like an interactive ^c */
/*    It must be the first & only command in a frameserver call */
#define  SERVER_CANCEL      0xC001

/* FrameServer only - this just returns a status code from maker */
/*    It must be the first & only command in a frameserver call */
#define  SERVER_QUERY       0xC002

/* To send a KBD_ABORT to a random dialog (over an unknown doc)  */
/*    use a docp=0 and send the KBD_ABORT as the first command   */
/*    More KBD_ABORTs may follow                               */

/* The following are for Menu animation. */
/* Note that Menu_cmdWAIT means wait for a map event afterwards */

#define  MENU_BARSTART      0xC301
#define  MENU_WAITBIT       0x0010
#define  MENU_DOWNWAIT      0xC312
#define  MENU_RIGHTWAIT     0xC313
#define  MENU_DOWN          0xC304
#define  MENU_RIGHT         0xC305
#define  MENU_DONE          0xC30F



/* start of "gobbled" functions. All function codes between
* KBD_GBL_START and KBD_GBL_END are gobbled. To gobble means
* that, if one such function is found in the input stream,
* then all immediately following such functions are thrown away.
*/
#define	  KBD_GBL_START 	0x100

/* Cursor (insert point) moving commands */
/* There are more cursor moving commands defined later */
#define CSR_HOME  		0x100	/* ~z, +[Home], - Top of column  */
#define CSR_UP    		0x101	/* ^p, [Up] */
#define CSR_DOWN  		0x102	/* ^n, [Down], */
#define CSR_RIGHT 		0x103	/* ^f, [Right] */
#define CSR_LEFT  		0x104	/* ^b, [Left] */
#define CSR_BOL   		0x105	/* ^a - Beginning of line */
#define CSR_EOL   		0x106	/* ^e - End of line       */
#define CSR_BOW			0x107	/* ~b - Previous word start */
#define CSR_EOW			0x108	/* ~f - Next word end */
#define CSR_BOS     	0x109  	/* ~a - Previous sentence start */
#define CSR_EOS     	0x10A	/* ~e - Next sentence end */
#define CSR_BOP			0x10B	/* ~[ - Previous paragraph start */
#define CSR_EOP			0x10C	/* ~] - Next paragraph end */
#define CSR_TOTR		0x10D	/* UNUSED  RECYCLE THIS VALUE */
#define CSR_BOTR		0x10E	/* ~Z, +[End] - Bottom of column */
#define CSR_TOP			0x10F	/* ~{ - Beginning of flow */
#define CSR_BOT			0x110	/* ~} - End of flow */

/* Deletion commands */
/* There are more deletion commands defined later */
#define	DEL_CHARBWD		0x112	/* [Delete], [BackSpace], ^h - Delete */
/* backward one character */
#define DEL_CHARFWD		0x113	/* ^d - Delete forward one character */
#define DEL_BOW			0x114	/* ~[Delete], ~[BackSpace], ~^h - Delete */
/* backward to previous word end */
#define DEL_EOW			0x115	/* ~d - Delete forward to next word start */
#define DEL_EOL     	0x116	/* ^k - Delete forward to end of line */
#define DEL_EOS			0x117	/* ~k - Delete forward to next sentence end */
#define DEL_SEL			0x118	/* see DEL_CHARBWD, Clear shortcut */
#define DEL_BOL			0x119	/*  ^u - Delete backward to start of line */

/* Kerning commands */
#define KBD_KERNUP 		0x11A	/* ^[Up]    - Move 1 point up */
#define KBD_KERNDOWN 	0x11B	/* ^[Down]  - Move 1 point down */
#define KBD_KERNLEFT 	0x11C	/* ^[Left]  - Move 1 point left */
#define KBD_KERNRIGHT	0x11D	/* ^[Right] - Move 1 point right*/
#define KBD_KERNHOME 	0x11E	/* ^[Home]  - Move back to baseline */

#define KBD_KERNUP6     0x121   /* +^[Up]    - Move 6 point up     */
#define KBD_KERNDOWN6   0x122   /* +^[Down]  - Move 6 point down   */
#define KBD_KERNLEFT6   0x123   /* +^[Left]  - Move 6 point left   */
#define KBD_KERNRIGHT6  0x124   /* +^[Right] - Move 6 point right  */

/* Object moving commands, just like kerning versions, but
require an object selection. No key assignments needed. */
#define KBD_OBJUP 		0x125	/* - Move 1 point up */
#define KBD_OBJDOWN 	0x126	/* - Move 1 point down */
#define KBD_OBJLEFT 	0x127	/* - Move 1 point left */
#define KBD_OBJRIGHT	0x128	/* - Move 1 point right*/

/* Cursor (insert point) moving commands */
/* There are more cursor moving commands defined earlier */
#define CSR_NEXT_BOW	0x140	/* !bw - Next word start */
#define CSR_NEXT_BOS   	0x141  	/* !bs - Next sentence start */
#define CSR_NEXT_BOP	0x142	/* !bp - Next paragraph start */
#define CSR_FIRST_COL	0x143	/* !bf - First text column on current page */
#define CSR_NEXT_COL	0x144	/* !bn - Next text column on current page */
#define CSR_NEXT_ELEMENT 	0x145 /* !sD - Next element/snippet start or end*/
#define CSR_PREV_ELEMENT 	0x146 /* !sU - Previous element/snippet start or end*/
#define CSR_BOE				0x149	/* !sS - Beginning of element */
#define CSR_EOE				0x14A	/* !sE - End of element */
#define CSR_BEFORE_ELEMENT	0x14B	/* !sB - Before beginning of element */
#define CSR_INTO_CHILD		0x14D	/* !sN - Into child */

/*sa-added*/
#define CSR_PREV_EOS		0x937		/* Previous Sentence End*/
#define CSR_PREV_EOW		0x938		/*Previous Word End*/

/* Deletion commands */
/* There are more deletion commands defined earlier */
#define DEL_WORD_START	0x160	/* !kb - Delete backward to previous word start */
#define DEL_WORD_END	0x161	/* !kf - Delete forward to next word end */
#define DEL_NEXT_SS		0x162	/* !ks - Delete forward to next sentence start */
#define DEL_BOS			0x163	/* !ka - Delete backward to previous sentence end */

#define KBD_NUMLOCK		0x170	/* NumLock key - change keypad between function pad and keypad. */

#define	  KBD_GBL_END   	0x1FF

/* Misc. editing commands */
#define KBD_BACKTAB		0x220	/* +[Tab] - Tab backwards in a dialog */
#define KBD_SHFTSPACE	0x221	/* +[Space] - Shift Space */
#define KBD_FIRSTTAB	0x222	/* ^Tab - First tab (as in dialogs) */
/*		- First text column flow in doc */
#define KBD_TABLE_TAB	0x223	/* ![Tab] - Tab for table cells */
#define KBD_TAB			0x219	/* ^i, [Tab] - Tab, just normal tab */

#define KBD_XCHARS      0x224	/* ^t, ~t - transpose characters (eXchange) */

#define KBD_SOFTHYPHEN	0x225	/* !-D, ^- - discretionary hyphen */
#define KBD_DONTHYPHEN  0x226   /* !ns, ~_ - suppress hyphenation */
#define KBD_HARDHYPHEN	0x227	/* !-h, ~- - nonbreaking hyphen */

#define KBD_HARDSPACE	0x228	/* ![Space]h, ^[Space] - hard space (not word delimeter) */
#define KBD_HARDRETURN	0x229	/* ~[Return] - Hard return */

#define KBD_NUMSPACE	0x22A	/* ![Space]1 - number space */
#define KBD_THINSPACE	0x22B	/* ![Space]t - thin space = 1/12 em */
#define KBD_EMSPACE		0x22C	/* ![Space]m - em space = 1 em */
#define KBD_ENSPACE		0x22D	/* ![Space]n - en space = 1/2 em */

#define KBD_OPENLINE	0x22E	/* ^o - Open line */

#define KBD_RETURN		0x22F	/* [Return] - normal return */

/* Search and Replace commands */
#define KBD_FPREV		0x230	/* ~r, !fip - Search backward */
#define KBD_FNEXT		0x231	/* ~s, !fin - Search forward */
#define KBD_RONCE		0x232	/* ^%, !ro  - Change*/
#define KBD_RGLOBAL		0x233	/*     !rg  - Change all*/
#define KBD_RANDF  		0x234	/*     !ra  - Replace and find again */
#define KBD_SETSEARCH	0x235   /* 	   !fis - Display Set Search dialog */

/* Highlighting commands */
/* There are more highlighting commands defined later */
#define HIGH_CHAR       0x240	/* !hc - Highlight next character */
#define HIGH_WORD       0x241	/* !hw - Highlight next word */
#define HIGH_LINE       0x242	/* !hl - Highlight next line */
#define HIGH_SENT       0x243	/* !hs - Highlight next sentence */
#define HIGH_PGF        0x244	/* !hp - Highlight next paragraph */
#define HIGH_SHL        0x245	/* !hb - Shift highlighting left 1 char */
#define HIGH_SHR        0x246	/* !hf - Shift highlighting right 1 char */
#define HIGH_CLEAR		0x247	/* !h0 - Clear Selection */
#define HIGH_FLOW		0x248	/* !ea - Select all in flow/page/table/frame */
#define HIGH_ELEMENT	0x249	/* !hE - Highlight next element */

/* Misc. control commands, some are also on menus */
#define KBD_ABORT  		0x250	/* ^c  - Abort a long process like import */
#define KBD_CAPTURE		0x251	/* !ftP - Capture portion of screen, not used  */
#define KBD_ECAPTURE	0x256	/* !ftp - Capture portion of screen, compressed*/
#define KBD_RECORD 		0x252	/* ^]  - Record keystrokes */
#define KBD_GETTRIGGER	0x253	/* ^]  - Get trigger for recorded keystrokes  */
#define KBD_ABORT_DLGS	0x254	/*  Special for X and FrameServer */
#define KBD_WAIT_DLGS	0x255	/*  Special for X and FrameServer */

/* Character attribute commands */
#define TXT_BOLD        0x260	/* !cb, +[F2] - Set chars to bold */
#define TXT_ITALIC      0x261	/* !ci, +[F3] - Set chars to italic */
#define TXT_UNDERLINE   0x262	/* !cu, +[F4] - Set chars to underline */
#define TXT_PLAIN       0x263	/* !cp, +[F1] - Set chars to plain */
#define TXT_SUPER       0x264	/* !c+ - Set chars to superscript */
#define TXT_SUB         0x265	/* !c- - Set chars to subscript */
#define TXT_NORMAL      0x266	/* !c= - Set chars to normal */
#define TXT_INCSIZE     0x267	/* !c], !+s - Increment text size */
#define TXT_DECSIZE     0x268	/* !c[, !-s - Decrement text size */
#define TXT_SELINCSIZE  0x11F	/* - Increment text size, sel only. */
#define TXT_SELDECSIZE  0x120	/* - Decrement text size, sel only.*/
#define TXT_SQUEEZE		0x269	/* ![D, !c[Left]  - Squeeze spacing 20% of an em */
#define TXT_SPREAD 		0x26A	/* ![C, !c[Right] - Spread spacing 20% of an em */
#define TXT_NOSTRETCH	0x130	/* ![n Set textstretch to 100% */
#define TXT_LESSSTRETCH	0x131	/* ![c Condense textstretch by 5 %pts */
#define TXT_MORESTRETCH	0x132	/* ![e Extend textstretch by 5 %pts */
#define TXT_BAM         0x26B	/* !cc - Set to font dialog values, no dialog */
/* 		 Used to repeat the last keyboard command */
#define TXT_STRIKEOUT	0x26C	/* !cs, +[F5] - Set chars to strikethrough */
#define TXT_DEFAULT		0x26D	/* !ocp - Set chars to default pgf font */
#define TXT_OVERLINE	0x26E	/* !co - Set chars to overline */
#define TXT_CHANGEBAR	0x26F	/* !ch - Set chars to change bar */
#define TXT_KERN		0x270	/* !ck - Set chars to kerned */
#define TXT_OUTLINE		0x271	/*       Set chars to outline */
#define TXT_SHADOW		0x272	/*       Set chars to shadow */
#define TXT_MINICAPS	0x273	/* !cm - Set chars to small caps */
#define TXT_INITCAPS	0x274	/* ~c - Set chars to initial caps */
#define TXT_UPPERCASE	0x275	/* ~u - Set chars to upper case */
#define TXT_LOWERCASE	0x276	/* ~l - Set chars to lower case */
#define TXT_NORMALCASE	0x277	/*    - Set chars to as-typed case */
#define TXT_DBLUNDERLINE	0x278	/* !cd - Set chars to double underline */
#define TXT_NUMUNDERLINE	0x279	/* !c2 - Set chars to numeric underline */
#define TXT_TSUME		0x2A5	/* !ct - Set chars to tsume */

/* these versions require a selection. No KBD needed. */
#define TXT_SELBOLD        0x27A	/* - Set chars to bold */
#define TXT_SELITALIC      0x27B	/* - Set chars to italic */
#define TXT_SELUNDERLINE   0x27C	/* - Set chars to underline */
#define TXT_SELPLAIN       0x27D	/* - Set chars to plain */

/* Ad hoc */
#define TXT_7		0xC00	/* Set text to 7 point */
#define TXT_9		0xC01	/* Set text to 9 point */
#define TXT_10		0xC02	/* Set text to 10 point */
#define TXT_12		0xC03	/* Set text to 12 point */
#define TXT_14		0xC04	/* Set text to 14 point */
#define TXT_18		0xC05	/* Set text to 18 point */
#define TXT_24		0xC06	/* Set text to 24 point */
#define TXT_USIZE	0xC07	/* Set text to the last size (initially 36) */
#define TXT_OTHERSIZE	0xC08	/* Set text size with dialog */
#define TXT_FAMILY_AND_VARIATION	0xC09	/* Set text family and variation */
#define TXT_FAMILY_AND_VARIATION_PLATFORM	0xC0A /* Platform specific set text family and variation */

#define PGF_APPLY_TAG	0xC10	/* Set pgf tag from menu */
#define CHAR_APPLY_TAG	0xC11	/* Set char tag from menu */
#define MENU_EXPOSE_WIN 0xC12   /* Expose window from menu */
#define MRU_SELECT_FILE 0xC13	/* Select a most recently visited */
#define MENU_IMPORT_FILE 0xC14  /* Import file from menu */
#define KBD_EXPORT_GRAPHIC 0xC15 /* Export Graphic from menu */
#define KBD_EXPORT_DOCUMENT 0xC16  /* Export document from menu*/
#define TXT_FONT	0xC17 /* Platform specific set text family and variation */

#define PGF_APPLY_CAT_TO_SEL	0xF3D
#define CHAR_APPLY_CAT_TO_SEL	0xF3E

/* Paragraph attributes and justification commands */
#define PGF_HYPHENON	0x27E	/* !jh - Turn hyphenation on */
#define PGF_HYPHENOFF	0x27F	/* !jn - Turn hyphenation off */
#define PGF_INCLINE     0x280	/* !j+, !+1 - Increment line spacing */
#define PGF_DECLINE     0x281	/* !j-, !-1 - Decrement line spacing */
#define PGF_CENTER      0x282	/* !jc - Center paragraph */
#define PGF_LEFT        0x283	/* !jl - Left justify paragraph */
#define PGF_RIGHT       0x284	/* !jr - Right justify paragraph */
#define PGF_FULL        0x285	/* !jf - Full justify paragraph */
#define PGF_BAM         0x286	/* !jj - Set to pgf dialog values, no dialog */
/* 		 Used to repeat the last keyboard command */
#define PGF_LINEFIX		0x287	/* !jx - Fixed line spacing */
#define PGF_LINEFLOAT	0x288	/* !jo - Floating line spacing */
#define PGF_UNIFY		0x289	/* !jU - Make all pgfs with current */
/* pgf's tag match current pgf's fmt */
#define PGF_PAGETOP		0x28A	/* !jP - Start at top of page */
#define PGF_COLTOP		0x28B	/* !jC - Start at top of column */
#define PGF_LEFTTOP		0x28C	/* !jL - Start at top of left page */
#define PGF_RIGHTTOP	0x28D	/* !jR - Start at top of right page */
#define PGF_ANYPLACE	0x28E	/* !jA - Start anywhere */
/* See below for PGF_KBD_ALIGN* since there's no more fcodes near 28E. */

/* Alignment commands */
#define KBD_ALIGN_TOP		0x290	/* !jt - Top align */
#define KBD_ALIGN_MIDDLE	0x291	/* !jm - Top/bottom (middle) align */
#define KBD_ALIGN_BOTTOM	0x292	/* !jb - Bottom align */

/* versions requiring an object selection. No KBD needed: */
#define KBD_OBALIGN_TOP		0x129	/* - Top align */
#define KBD_OBALIGN_MIDDLE	0x12A	/* - Top/bottom (middle) align */
#define KBD_OBALIGN_BOTTOM	0x12B	/* - Bottom align */
#define KBD_OBALIGN_CENTER  0x12C	/* - Center paragraph */
#define KBD_OBALIGN_LEFT    0x12D	/* - Left justify paragraph */
#define KBD_OBALIGN_RIGHT   0x12E	/* - Right justify paragraph */

/* Line spacing commands */
#define PGF_SINGLE_SPACE	0x293		/* !j1 - Single space paragraphs */
#define PGF_ONEANDAHALF_SPACE	0x294	/* !j/ - 1 1/2 space paragraphs */
#define PGF_DOUBLE_SPACE	0x295		/* !j2 - Double space paragraphs */
#define PGF_SPACE_BETWEEN	0x296 /* !jw  - Space Between dialog */
#define PGF_LINE_SPACE		0x297 /* !ju  - Line Space dialog */
#define PGF_UPDATE_ALL		0x298 /* !opu - Update Pgf Format dialog */
#define PGF_NEW_FORMAT		0x299 /* !opn - New Pgf Format dialog */

/* help commands for all platforms */
#define KBD_HELP_INDEX  	0x2A0  /*     - Help Index */
#define KBD_HELP_KEYS		0x2A1  /* !?k - Keyboard Shortcut Help */
#define KBD_HELP_SAMPLES	0x2A2  /*     - Samples and Clip Art Help */
#define KBD_HELP_OVERVIEW	0x2A3  /*     - FrameMaker Overview */
#define KBD_HELP_ONLINEMANUALS	0x2A4  /* - FrameMaker Online Manuals */
#define KBD_HELP_WEBWORKS		0x2A6  /*     - Help about WebWorks */

#define KBD_CUSTOMNEW		0x2FF  /*     - Custom Blank Paper dialog */

/*  Main window commands, some are also on menus   */
#define KBD_NEW         0x300	/*       !fn */
#define KBD_NEWBOOK     0x308   /*       !fN */
#define KBD_OPEN        0x301	/* ^x^f, !fo */
#define KBD_HEROIC_OPEN 0x306	/*       !oH */
#define KBD_SILENT_OPEN 0x307	/*       !oS */
#define KBD_HELP        0x302	/*       !fh */
#define KBD_INFO        0x303	/*       ^[F1] */
#define KBD_CSHELPMODE  0x304	/* ~?,   [F1] */
#define KBD_ABOUTPRODUCT	0x305

/* Book kit file menu commands */
#define KBD_BOOKADDFILE 			0x30A	/* !ff - Add file to book*/
#define KBD_BOOKEDITDEFINE 			0x30B	/* !fd - Set up generated file*/
#define KBD_BOOKRENAMEFILE			0x30C
#define KBD_BOOKDISPLAYFILENAME		0x30D
#define KBD_BOOKDISPLAYTEXT			0x30E
#define	KBD_BOOKCOMP_EXCLUDE		0x4E0

/* Book Kit toolbar commands */
#define KBD_BOOKADDFOLDER		0x601
#define KBD_BOOKCOMP_FILENAME	0x607
#define KBD_BOOKCOMP_TEXT		0x608
#define KBD_BOOKADDGROUP		0x609

/* Composite document operations. */
#define KBD_MOVE_UP					0x70A
#define KBD_MOVE_DOWN				0x70B
#define KBD_MOVE_LEFT				0x70C
#define KBD_MOVE_RIGHT				0x70D
#define KBD_OPEN_COMPONENTS			0x3EA
#define KBD_CLOSE_COMPONENTS		0x3EB
#define KBD_PRINT_COMPONENTS		0x3EC
#define KBD_SAVE_COMPONENTS			0x3ED
#define KBD_COMPONENT_PROPERTIES	0x3EE
#define KBD_DELETE_FILE				0x30F
#define KBD_COLLAPSE_ALL			0x3FA
#define KBD_EXPAND_ALL				0x3FB

#define	SWITCH_TO_RESOURCEMANAGER	0x9EA
#define	SWITCH_TO_DOCUMENTVIEW		0x9EB

/* File menu commands */
/*		KBD_NEW			defined in main window commands !fn */
/*		KBD_OPEN		defined in main window commands !fo */
#define KBD_SAVE        0x310	/* ^x^s, !fs */
#define KBD_SAVEAS      0x311	/* ^x^w, !fa */
#define KBD_SAVEASPDFREVIEW   0x95A
#define KBD_SAVEASPDFREVIEW2   0x95B
#define KBD_SAVEASPDF   0x950
#define KBD_SAVEASXML   0x951
#define KBD_MANCOND		0x952
#define KBD_CONDINDICATOR 0x953
#define KBD_ATTRCOND	0x954
/*TRACK CHANGES*/
#define KBD_FINDNEXT		0x955
#define KBD_FINDPREV		0x956
#define KBD_TRACKCHANGE		0x957
#define KBD_ACCEPTCHANGE	0x958
#define KBD_REJECTCHANGE	0x959
#define KBD_ACCEPTALLCHANGE	0x960
#define KBD_REJECTALLCHANGE	0x961
#define KBD_PREVIEW_ACCEPTALL	0x962
#define KBD_PREVIEW_REJECTALL	0x963
#define KBD_PREVIEW_OFF		0x964
/*TRACK CHANGES*/

/* AMTLib HelpMenu Commands */
#define AMT_REGISRATION 0x965
#define AMT_DEACTIVATION 0x967
#define AMT_UPDATES 0x968
/* AMTLib HelpMenu Commands */

#define KBD_REVERT		0x312	/*       !fr */
#define KBD_DOCINFO		0x3DA	/*       !fI - File Info */
#define KBD_PRINT       0x313	/*       !fp */
#define KBD_IMPORT      0x314	/*       !fif - Import File */
#define KBD_GENERATE    0x3E1   /*       !eU */
#define KBD_USEFMTFROM  0x316	/* 		 !fio - Import Formats */
#define KBD_KBMACRO		0x317	/* 		 !ftk */
/*		KBD_CAPTURE		defined in misc. control commands !ftp */
#define KBD_SESSION		0x318	/*       !fP - Session preferences */
/*		KBD_QUITWIN		defined in window menu commands !fq */

#define KBD_PAGESETUP 	0x319   /* page setup for Mac */

/* File menu commands access by shift */
#define KBD_OPENALL		0x31A	/* !fO - Open all */
#define KBD_QUITALL		0x31B	/* !fQ - Quit all */
#define KBD_SAVEALL		0x31C	/* !fS - Save all */
#define KBD_REPEATNEW	0x31D	/*     - Repeat last new command */
/*      KBD_CUSTOMNEW   0x2FF            Custom Blank Paper dialog */
#define KBD_PODLOCATION 0x31E
#define KBD_COMPARE		0x31F   /* !ftc - Compare docs or books */

/* Edit menu commands */
#define KBD_UNDO        0x320	/*     !eu, [Undo] */

/*	The codes of these should have been greater than 0x94F
because these should fall outside the range
[ KBD_EMBEDDED00 & KBD_EMBEDDEDMAX ] - dhaundy
*/
#define KBD_REDO        0x935	/*     !er, [Redo] */
#define KBD_HIST        0x936	/*     !eh, [Hist] */

#define KBD_CUT         0x321	/* ^w, !ex     */
#define KBD_COPY        0x322	/* ~w, !ec     */
#define KBD_PASTE       0x323	/*     !ep	   */
#define KBD_CLEAR       0x324   /*     !eb     */
#define KBD_COPYFONT	0x325	/* 	   !eyc - Copy font properties */
#define KBD_COPYPGF		0x326	/* 	   !eyp - Copy pgf  properties */
/* See below for KBD_COPY{COND,CELLFMT,COLW} since there are no fcodes left near 0x320. */
#define KBD_SELECTALL   0x327	/*     !ea     */
#define KBD_STUFF       0x328	/* ~y, !ii	   */
#define KBD_SEARCH      0x329	/* ^s, !ef     */
#define KBD_SPELLING	0x32A	/*	   !es     */
#define KBD_CAPITAL		0x32B	/*     !eC     */
#define KBD_YANK        0x32C   /* ^y  !eY - pastes kill buffer */
#define KBD_SELECT_GENERATED_FILES		0x32D /* */
#define KBD_SELECT_NON_GENERATED_FILES	0x32E /* */
#define KBD_SELECT_FM_FILES				0x315 /* */
#define KBD_SELECT_PRINTABLE_FILES		0x9E2 /* */
#define KBD_SELECT_EXCLUDED_FILE		0x9D1 /* !eAE */
#define KBD_SELECT_NONEXCLUDED_FILE		0x9D2 /* !eNE */
#define	KBD_SELECT_CHAPTER_COMPONENTS	0x9D3 /* !cl */
#define	KBD_SELECT_SECTION_COMPONENTS	0x9D4 /* !sl */
#define	KBD_SELECT_SUBSECTION_COMPONENTS	0x9D5 /* !ssl */

#define KBD_ALLCAP		0x33A	/* ~u - convert selected text to cap */
#define KBD_ALLLOWER	0x33B	/* ~l - convert selected text to lower case */
#define KBD_INITCAP		0x33C	/* ~c - convert selected text to initial caps */
/* these 3 are provided just for the command palette, no KB needed: */
#define KBD_ALLCAPH		0x369	/* like ALLCAP, but keep text selected */
#define KBD_ALLLOWERH	0x36A	/* like ALLLOWER, but keep text selected */
#define KBD_INITCAPH	0x36B	/* like INITCAP, but keep text selected */

#define KBD_THESAURUS	0x3D0	/* !et - lookup selected word in Thesaurus */
#define KBD_THESAURUS_REPLACE	0x3D3 /* !Tr - replace active selection with 
word from thesaurus */
#define KBD_CREATE_PUBLISHER	0x3D5	/* Mac only */
#define KBD_SUBSCRIBE_TO		0x3D6	/* Mac only */
#define KBD_LINK_BOUNDARIES		0x3D7  /* Turn link borders on and off.
* Currenlty only effect Publishers.
*/
/* Format menu commands */
#define KBD_FONTDESIGN 		0x330	/* !ocd - Character Format Designer */
#define KBD_PGFDESIGN  		0x331	/* !opd - Paragraph Format Designer */
#define KBD_RUBIPROPS       0x3DD

/* Page Layout Menu commands */
#define KBD_COLLAYOUT   	0x348	/* !ocl */ 
#define KBD_LINELAYOUT     	0x332	/* !oll */
#define KBD_PAGESIZE		0x349	/* !ops	*/
#define KBD_PAGINATION      0xA40	
#define KBD_PAGEBACK 		0x34A	/* !omu */
#define KBD_NEWMASTER		0xA0A	/* !omp	*/
#define KBD_REORDERMASTER	0xA0B	/* !omr - Reorder Master Pages */
#define KBD_PAGEUPDATE		0xA00	/* !oup	*/

/* Customize Page Layout Menu commands */
#define KBD_CUST_TEXT_FRAME		0xA01	/* !ocf - Customize text frame	*/
#define KBD_CONNECT_TEXT_FRAME	0x35B	/* !CC -Connect text frames */
#define KBD_CUTHEAD				0x35C	/* !CP - Cut Previous */
#define KBD_CURTAIL				0x35D	/* !CN - Cut Next */
#define KBD_CUTBOTH				0xA04	/* !CB - Cut Both */
#define KBD_SPLIT				0x35A	/* !CS - Split column below IP */
#define KBD_SPLITR				0xA02	/* OBSOLETE */
#define KBD_SPLITL				0xA03	/* OBSOLETE */
#define KBD_ROTPAGE_PLUS		0x34E	/* !pO - Rotate page clockwise */
#define KBD_ROTPAGE_MINUS		0x34F	/* !po - Rotate page CounterC */
#define KBD_ROTPAGE_NORM		0xA05	/* !pU - Un-rotate Page */

/* Document Menu commands */
#define KBD_NUMBERING   	0x333	/* !odn - Doc Numbering, !en Book */
#define KBD_CBARPRO	    	0x334	/* !ob  - Change Bars */
#define KBD_FOOTNOTEPRO    	0x335	/* !of  - Footnote Properties */
#define KBD_TEXT_OPTIONS   	0x337	/* !oto - Text Options */
#define KBD_COMBINED_FONTS 	0x338	/* !ocf - Combined Fonts */
#define KBD_ACROBAT_SETUP	0x36C	/* !oda - PDF Setup */
/* Other Page commands */
#define KBD_FIRSTPAGE   0x340	/* !pf, ~<, +[F6] */
#define KBD_LASTPAGE    0x341	/* !pl, ~>, +[F7] */
#define KBD_BODYPAGE	0x342	/* !vB */
#define KBD_MASTERPAGE	0x343	/* !vM */
#define KBD_REFPAGE		0x344	/* !vR */
#define KBD_GOTOPAGE    0x345	/* !vp, ^g*/
#define KBD_ADDPAGE     0x346	/* !spa */
#define KBD_DELETEPAGE  0x347	/* !spd */
#define KBD_FREEZE		0x34B   /* !pz */
#define KBD_TEXTCOLPRO  0x339   /* !jpS - Toggle Sideheads On/Off  */
#define KBD_PREVPAGE    0x34C	/* !pp, ~v, F6 */
#define KBD_NEXTPAGE    0x34D	/* !pn, ^v, F7 */
#define KBD_GOTOPAGEN	0x96E
#define KBD_GOTOIP		0x96F
/* toggle structure window left anchor when scrolling */
#define KBD_STRWIN_LEFTANCHOR  0x3DF   /* !sva */

/* Special menu commands */
#define KBD_PAGEBREAK	0x32F	/* !spb */
#define KBD_ANCHOR      0x350	/* !sa */
#define KBD_FOOTNOTE	0x351	/* !sf */
#define KBD_REFERENCE	0x352	/* !sc */
#define KBD_VARIABLE	0x353	/* !sv */
#define KBD_INSET		0x354	/* !si */
#define KBD_HYPERTEXT	0x359	/* !sh */
#define KBD_MARKERS     0x355	/* !sm */
#define KBD_EQUATION    0x336   /* !pe - Equation Sizes*/
#define KBD_CONDTEXT 	0x357	/* !sC */

#define KBD_NEWMARKER	0x356	/* !mk - Insert new marker */
#define KBD_EDITMARKERTYPE	0x409   /* !emt - Edit Marker Types */
#define KBD_DELMARKERTYPE	0x358	/* ?? - Delete Marker Type by name */
#define KBD_RENAMEMARKERTYPE	0x35F	/* ?? - Rename Marker Type */
#define KBD_ADDMARKERTYPE       0x41D   /* ?? - Add Marker Type by name */
#define KBD_DELETEMARKER       0x969   /* ?? - Add Marker Type by name */
#define KBD_MARKERSPOD			0x96A   /* ?? - Add Marker Type by name */
#define KBD_XREFSPOD			0x96B /* Cross-References Pod */
#define KBD_FONTPOD				0x966 /*font pod*/
#define KBD_FONTREPLACEPOD				0x95C /*font replace pod*/
#define KBD_NEWAFRAME      0x96D 
#define KBD_NEWHYPERTEXT 0x35E	/* !mh - Insert new hypertext */
#define KBD_VALIDATE_HYPERTEXT  0xF13   /* !vh - Validate hypertext makers */
#define MENU_HYPERTEXT  0xC18   /* fcode for hypertext-in-menu-items */
#define KBD_HYPRTXT_SHTCUT  0x41E
#define WEB_GOTOADOBE	0xC19	/* !www - access web services */
#define WEB_PREFERENCES	0xC1A
#define WEB_TOPISSUES	0xC1B
#define WEB_ADOBEHELP	0xC22
#define WEB_COMPLETEHELP 0xD1F
/*#define WEB_DOWNLOADABLES	0xC1C*/
#define WEB_CORPORATENEWS	0xC1D
#define WEB_REGISTRATION	0xC1E
#define WEB_FRAME_BOOKMARKS	0xC1F
#define WEB_ADOBE_BOOKMARKS	0xC21
#define KBD_DUMPHYPERTEXT 0xC20 /* !dh - Dump h-text selection to file */
#define KBD_VARCURPG    0xA06   /* !ohp - Insert current page # variable */
#define KBD_VARPGCOUNT  0xA07   /* !ohc - Insert page count variable */
#define KBD_VARCURDATE  0xA08	/* !ohd - Insert current date # variable */
#define KBD_VAROTHER    0xA09	/* !oho - Insert current date # variable */
#define KBD_EDITVARIABLE	0xA0C   /*Edit Variable pod directly*/

#define KBD_TOC					0x2B0
#define KBD_LIST_FIGURE			0x2B1
#define KBD_LIST_TABLE			0x2B2
#define KBD_LIST_PGF			0x2B3
#define KBD_LIST_PGF_ALPHA		0x2B4
#define KBD_LIST_MARKER			0x2B5
#define KBD_LIST_MARKER_ALPHA	0x2B6
#define KBD_LIST_REFERENCES		0x2B7
#define KBD_INDEX_STANDARD		0x2C0
#define KBD_INDEX_AUTHOR		0x2C1
#define KBD_INDEX_SUBJECT		0x2C2
#define KBD_INDEX_MARKER		0x2C3
#define KBD_INDEX_REFERENCES	0x2C4

/* View menu commands */
#define KBD_OPTIONS		0x360	/* !vo */
#define KBD_BORDERS     0x361	/* !vb */
#define KBD_SYMBOLS     0x362	/* !vt */
#define KBD_RULERS     	0x363	/* !vr */
#define KBD_GRID     	0x364	/* !vg */
#define KBD_SEPARATIONS 0x365	/* !vcv */
#define KBD_COLOR 		0x39F	/* !vcd */
#define KBD_CONDVISIBILITY	0x367 /* !vC */
#define KBD_CONDTOGGLEOVERR 0x368 /* !vO */

#define KBD_TOGGLEDRAW	0x366	/* !vv - Toggle draw/don't draw preference */

#define KBD_VIEWSEP1    0x36D	/* !v1 */ 
#define KBD_VIEWSEP2    0x36E	/* !v2 */
#define KBD_VIEWSEP3    0x36F	/* !v3 */
#define KBD_VIEWSEP4    0x37D	/* !v4 */ 
#define KBD_VIEWSEP5    0x37E	/* !v5 */
#define KBD_VIEWSEP6    0x39E	/* !v6 */

#define KBD_MENUCOMPLETE	0x33D	/* !vmc   */
#define KBD_MENUQUICK		0x33E	/* !vmq   */
#define KBD_MENUCUSTOM		0x33F	/* !vmu   */
#define KBD_MENUMODIFY		0x3D9	/* !vmm	  */
#define KBD_MENURESET		0x3D8	/* !vmr   */

#define KBD_SHOW_BORDERS		0x3F1
#define KBD_SHOW_SYMBOLS		0x3F2
#define KBD_SHOW_RULERS			0x3F3
#define KBD_SHOW_GRID			0x3F4
#define KBD_SHOW_LINK_BOUNDARIES	0x3F5
#define KBD_SHOW_ELEM_BORDER		0x3F6
#define KBD_SHOW_ELEM_TAGS		0x3F7
#define KBD_SHOW_GRAPHICS       0x3F8
#define KBD_SHOW_COND_IND       0x3F9

#define KBD_HIDE_BORDERS		0x4F1
#define KBD_HIDE_SYMBOLS		0x4F2
#define KBD_HIDE_RULERS			0x4F3
#define KBD_HIDE_GRID			0x4F4
#define KBD_HIDE_LINK_BOUNDARIES	0x4F5
#define KBD_HIDE_ELEM_BORDER		0x4F6
#define KBD_HIDE_GRAPHICS       0x4F8
#define KBD_HIDE_COND_IND       0x4F9
#define KBD_INSETPOD			0x994

/* Graphics menu commands */
#define KBD_FLIPUD			0x370	/* !gv */
#define KBD_FLIPLR			0x371	/* !gh */
#define KBD_ROTATE_CCW		0x372	/* !gt - rotate 90 counterclockwise*/
#define KBD_ROT_PLUS		0x372	/* !gt - rotate 90 counterclockwise*/
#define KBD_ROTATE_CCW_SMALL 0x38A	/*     - rotate 15 or 90 counterclockwise*/
#define KBD_SCALE       	0x373	/* !gz */
#define KBD_SMOOTH      	0x374	/* !gs */
#define KBD_UNSMOOTH    	0x375	/* !gm */
#define KBD_RESHAPE     	0x376	/* !gr */
#define KBD_JOINCURVES    	0x377	/* !gj */
#define KBD_SETSIDES    	0x378	/* !gn */
#define KBD_CONSTRAIN   	0x379
#define KBD_SNAP        	0x37A	/* !gp */
#define KBD_GRAVITY     	0x37B	/* !gy */
#define KBD_KEEPTOOL    	0x37C   /* !gk */
#define KBD_OVERPRINT 		0x3D2	/* Obsolete? */
#define KBD_OVERPRINT_NONE  0x3D4	/* Obsolete? */
#define KBD_RUN_OFF			0x670	/* !gq - Turn off runarounds */
#define KBD_RUN_CONTOUR		0x668	/* !gw - Contour runaround */
#define KBD_RUN_BBOX		0x669	/* !gW - Bounding box runaround */
#define KBD_RUN_GAP			0x671	/* none */
#define KBD_RUN_PROPS       0x667
#define KBD_SETRUN_PROPS    0x96C
#define KBD_FRONT       	0x380	/* !gf */
#define KBD_BACK        	0x381	/* !gb */
#define KBD_GROUP       	0x382	/* !gg */
#define KBD_UNGROUP     	0x383	/* !gu */
#define KBD_ALIGN       	0x384	/* !ga */
#define KBD_DISTRIBUTE  	0x385	/* !gd */
#define KBD_ROTATE_CW		0x386	/* !g+ - rotate 90 clockwise */
#define KBD_ROT_MINUS		0x386	/* !g+ - rotate 90 clockwise */
#define KBD_ROTATE_CW_SMALL	0x389	/*     - rotate 15 or 90 clockwise */
#define KBD_OBJPROPS		0x387	/* !go */
#define KBD_PICKOBJPROPS	0x388	/* !gO */
#define	KBD_MUTATE			0x37F	/* obsolete? */

#define	KBD_ROTATE				0x38B	/* !gt */
#define	KBD_ROTATE_AGAIN		0x38C	/* !gx */
#define	KBD_ROTATE_NATURAL		0x38D	/* !g0 */
#define	KBD_REROTATE			0x38E	/* !g1 */
#define	KBD_ROTATE_RESET0		0x38F	/* !g9 */

#define	KBD_SETALIGN_PROPS		0x992	/* Modeless Align Dialog */
#define	KBD_SETDISTRIBUTE_PROPS	0x993	/* Modeless Distribute Dialog */


/* Window menu commands  */
#define KBD_CLOSEWIN    0x390   /* !wc       */
#define KBD_OPENWIN     0x391	/* !wo       */
#define KBD_CLOPWIN     0x392   /* Close/Open Window */  
#define KBD_MOVEWIN     0x393   /* !wm       */
/* 0x394 UNUSED */
#define KBD_EXPOSEWIN   0x395   /* !we       */
#define KBD_HIDEWIN     0x396   /* !wh       */
#define KBD_HISHWIN     0x397	/* Hide/show toggel */
#define KBD_REFRESHWIN  0x398   /* !wr, ^l   */
#define KBD_QUITWIN     0x399   /* !fq, !fc, ^x^c */

/* Only used internally to resize document toggling */
/* between lock and unlock. */
#define KBD_RESIZELOCK		0x39A
#define KBD_RESIZEUNLOCK	0x39B

/* Only used internally */  
#define KBD_QUIETCLOSEWIN   0x39C

/* Bring up the Popup Menu (ala right button), in X-Motif port */
#define KBD_POPUP_MENU      0x39D

/* Tools window tools */
#define KBD_LINETOOL    0x3A0   /* !1l  */
#define KBD_RECTTOOL    0x3A1   /* !1r  */
#define KBD_POLYGTOOL   0x3A2   /* !1pg */
#define KBD_POLYLTOOL   0x3A3   /* !1pl */
#define KBD_ARCTOOL     0x3A4   /* !1a  */
#define KBD_ROUNDRECT   0x3A5   /* !1R  */
#define KBD_OVALTOOL    0x3A6   /* !1e   ("ellipse") */
#define KBD_TEXTLTOOL   0x3A7   /* !1tl */
#define KBD_TEXTRTOOL   0x3A8   /* !1tf */
#define KBD_FREETOOL    0x3A9   /* !1f  */
#define KBD_FRAMETOOL   0x3AA   /* !1m  */
#define KBD_LASTTOOL    0x3AB   /* !11  select last-used tool  */

/* Line width commands */
#define KBD_WIDTH0      0x3AC   /* !0w - Set to thinnest width  */
#define KBD_WIDTH1      0x3AD   /* !9w - Set to thickest width   */
#define KBD_INCWIDTH    0x3AE   /* !+w - Increment line width   */
#define KBD_DECWIDTH    0x3AF   /* !-w - Decrement line width   */

/* Pen pattern commands */
#define KBD_PEN0        0x3B0   /* !0p - Set to "first" pen pat */
#define KBD_PEN1        0x3B1   /* !9p - Set to last pen  pattern*/
#define KBD_INCPEN      0x3B2   /* !+p - Increment pen pattern  */
#define KBD_DECPEN      0x3B3   /* !-p - Decrement pen pattern  */

/* Fill pattern commands */
#define KBD_FILL0       0x3B4   /* !0f - Set to "first" fill pat*/
#define KBD_FILL1       0x3B5   /* !9f - Set to last fill pattern*/
#define KBD_INCFILL     0x3B6   /* !+f - Increment fill pattern */
#define KBD_DECFILL     0x3B7   /* !-f - Decrement fill pattern */

/* This cmds are never issued by keyboard only by graphics palette */
#define KBD_SETFILL      0x3B8
#define KBD_SETPEN       0x3B9
#define KBD_SETWIDTH     0x3BA
#define KBD_SETCAP       0x3BB
#define KBD_SETSEP       0x3BC
#define KBD_SETSEP_KEEP  0x3BE
#define KBD_SETSEP_ALL   0x3BF
#define KBD_SETSEP_RESET_TINT_OVERPRINT 0x3D1

#define KBD_SETFILL_0     0x430
#define KBD_SETFILL_1     0x431
#define KBD_SETFILL_2     0x432
#define KBD_SETFILL_3     0x433
#define KBD_SETFILL_4     0x434
#define KBD_SETFILL_5     0x435
#define KBD_SETFILL_6     0x436
#define KBD_SETFILL_7     0x437
#define KBD_SETFILL_8     0x438
#define KBD_SETFILL_9     0x439
#define KBD_SETFILL_A     0x43A
#define KBD_SETFILL_B     0x43B
#define KBD_SETFILL_C     0x43C
#define KBD_SETFILL_D     0x43D
#define KBD_SETFILL_E     0x43E
#define KBD_SETFILL_F     0x43F

#define KBD_SETPEN_0     0x440
#define KBD_SETPEN_1     0x441
#define KBD_SETPEN_2     0x442
#define KBD_SETPEN_3     0x443
#define KBD_SETPEN_4     0x444
#define KBD_SETPEN_5     0x445
#define KBD_SETPEN_6     0x446
#define KBD_SETPEN_7     0x447
#define KBD_SETPEN_8     0x448
#define KBD_SETPEN_9     0x449
#define KBD_SETPEN_A     0x44A
#define KBD_SETPEN_B     0x44B
#define KBD_SETPEN_C     0x44C
#define KBD_SETPEN_D     0x44D
#define KBD_SETPEN_E     0x44E
#define KBD_SETPEN_F     0x44F

#define KBD_SETWIDTH_0      0x450
#define KBD_SETWIDTH_1      0x451
#define KBD_SETWIDTH_2      0x452
#define KBD_SETWIDTH_3      0x453

#define KBD_SETWIDTH_SLIDE 0x45E /* Only used internally */ 
#define KBD_SETWIDTH_OPTION 0x45F

#define KBD_SETCAP_0       0x460
#define KBD_SETCAP_1       0x461
#define KBD_SETCAP_2       0x462
#define KBD_SETCAP_3       0x463
#define KBD_SETCAP_OPTION  0x46F

#define KBD_SETDASH_0     0x470
#define KBD_SETDASH_1     0x471
#define KBD_SETDASH_2     0x472
#define KBD_SETDASH_3     0x473
#define KBD_SETDASH_4     0x474
#define KBD_SETDASH_5     0x475
#define KBD_SETDASH_6     0x476
#define KBD_SETDASH_7     0x477
#define KBD_SETDASH_8     0x478
#define KBD_SETDASH_OPTION 0x47F

#define KBD_SNAP_0        	0x480
#define KBD_SNAP_1        	0x481
#define KBD_GRAVITY_0     	0x482
#define KBD_GRAVITY_1     	0x483

#define KBD_SETKNOCKOUT    0x48A
#define KBD_SETOVERPRINT   0x48B
#define KBD_SETFROMCOLOR   0x48C
#define KBD_SETTINT        0x48D

/* Dashed Line commands */
#define	KBD_SETSOLID	0x402	/* !1ds	- set solid line */
#define	KBD_SETDASH		0x403	/* !1dd	- set dashed line */
#define	KBD_DASH0		0x404	/* !0d	- select first dashed pattern */
#define	KBD_DASH1		0x405	/* !9d	- select last dashed pattern */
#define	KBD_INCDASH		0x406	/* !-d	- select next dashed pattern */
#define	KBD_DECDASH		0x407	/* !+d	- select previous dashed pattern */
#define	KBD_DASHOPTION	0x408	/* !1di - bring up dashed options dialog */

/* Spelling checker commands */
#define KBD_CHECKSEL	0x3C0	/* !ls  - check selection		*/
#define KBD_CHECKDOC	0x3C1	/* !le  - check entire doc	*/
#define KBD_CORRECT		0x3C2	/* !lcw - correct word		*/
#define KBD_ADDUSRDICT	0x3C3	/* !lap - add to personal dict	*/
#define KBD_ADDDOCDICT	0x3C4	/* !lad - add to document dict	*/
#define KBD_ADDAUTOCORR 0x3C5	/* !lac - add to auto corrections */
#define KBD_DELUSRDICT	0x3C6	/* !lxp - del from personal dict	*/
#define KBD_DELDOCDICT	0x3C7	/* !lxd - del from document dict	*/
#define KBD_CLEARAUTO	0x3C8   /* !lca - clear auto corrections  */
#define KBD_CHANGEDICT	0x3C9	/* !lcd - change dictionaries	*/
#define KBD_SPELLRESET  0x3CA	/* !lr  - reset checked pgfs	*/
#define KBD_CHECKPAGE	0x3CB	/* !lp  - check page		*/
#define KBD_SPOPTIONS	0x3CC   /* !lo  - spell check options */
#define KBD_HYPHENATE	0x3CD   /* !l-  - hyphenate word */
#define KBD_CHECKBATCH  0x3CE   /* !lb  - batch spell check */
#define KBD_REFORMATDOC 0x3CF   /* !lR  - reformat entire document */

/* Text Inset commands */
#define TEXT_INSET_PROPS 0x3E0  /* !ei - Text inset properties */
#define UPDATE_INSETS    0x3E1  /* !eU - Update References */
#define SUPPRESS_INSETS  0x3E2  /* !eS - Suppress auto reference updating */

#define KBD_MENUBARFOCUS 0x3F0  /* simulates Menubar Focus for Bookkit */

/* Thesaurus commands */

/* Smart quotes */
#define KBD_SINGLE_QUOTE    0x400   /* '    */
#define KBD_DOUBLE_QUOTE    0x401   /* "    */

/* Highlighting commands */
/* There are more highlighting commands defined earlier */
#define HIGH_CHAR_PREV  	0x410	/* !HC - Move active end of selection back 1 char */
#define HIGH_WORD_PREV  	0x411	/* !HW - Move active end back 1 word */
#define HIGH_LINE_PREV  	0x412	/* !HL - Move active end back 1 line (select a whole line) */
#define HIGH_SENT_PREV  	0x413	/* !HS - Move active end back 1 sentence */
#define HIGH_PGF_PREV   	0x414	/* !HP - Move active end back 1 paragraph */

#define HIGH_LINE_UP   		0x415	/* !hu - Extend one line up */
#define HIGH_LINE_DOWN   	0x416	/* !hd - Extend one line down*/
#define HIGH_COL_TOP   		0x417	/* !ht - Extend to top of column */
#define HIGH_COL_BOT   		0x418	/* !hm - Extend to bottom of column */
#define HIGH_FLOW_BEG   	0x419	/* !hg - Extend to beginning of flow */
#define HIGH_FLOW_END		0x41A	/* !hn - Extend to end of flow */
#define HIGH_LINE_BEG		0x41B	/* Windows specific - Select line to the beginning */
#define HIGH_LINE_END		0x41C	/* Windows specific - Select line to the end */

#define HIGH_ELEMENT_PREV	0x420   /* !hP - Move active end back 1 element */
#define HIGH_ELEMENT_NEXT	0x421	/* !hN - Move active end forward 1 element */
#define HIGH_SIBLINGS		0x422	/* !hS - Extend to all of parent's contents */
#define HIGH_PARENT			0x423	/* !heP - Extend to all of parent */

#define KBD_DFN_WEB_obsolete	0x500	/* NeXT-specific (obsoleted by NextStep 2.0). */
#define KBD_DFN_LIB_obsolete	0x501	/* NeXT-specific (obsoleted by NextStep 2.0). */
#define KBD_PASTE_RTF 		0x502	/* NeXT-specific */
#define KBD_PAGELAYOUT 		0x503	/* NeXT-specific */
#define KBD_COPYRIGHT 		0x504	/* NeXT-specific */

/* Added for NeXT sbs 911122. */
#define KBD_RESTOREFONT		0x505	/* restore IP font */
#define KBD_SYMFONT			0x506	/* temporarily set IP font to Symbol */

#define HIGH_CHAR_NEXT  	0x510	/* !Hc - Move active end of selection fwd 1 char */
#define HIGH_WORD_NEXT  	0x511	/* !Hw - Move active end forward 1 word */
#define HIGH_LINE_NEXT  	0x512	/* !Hl - Move active end forward 1 line (select a whole line) */
#define HIGH_SENT_NEXT  	0x513	/* !Hs - Move active end forward 1 sentence */
#define HIGH_PGF_NEXT   	0x514	/* !Hp - Move active end forward 1 paragraph */
#define HIGH_SAMECB			0x515	/* !hF - Select text with the same char fmt */
#define HIGH_SAMECOND		0x516	/* !hC - Select text with the same conditions */
#define HIGH_HYPERTEXT		0x517	/* used internally for hypertext highlighting */
#define SEARCH_CB			0x518	/* used internally by findcblocks */
#define SEARCH_CBTAG		0x519	/* used internally by findcblocks */
#define SEARCH_COND			0x51A	/* used internally by findcblocks */

/* Object selection commands */
#define OBJ_SEL_FIRST		0x600	/* !of - Select first object on cur page */
#define OBJ_SEL_NEXT		0x601	/* !on - Select next object on cur page */
#define OBJ_SEL_EXTEND_NEXT	0x602	/* !oe - Extend object selection to */
/* next object on current page */
#define OBJ_SEL_PREV		0x603	/* Select prev object on cur page */
#define OBJ_SEL_NEXT_WRAP	0x604	/* wrap to beginning if at the end */
#define OBJ_SEL_PREV_WRAP	0x605	/* wrap to end if at the beginning */
#define OBJ_SEL_LAST		0x606	/* Select last object on cur page */

/* Input focus to current document kit and modeless dialogs */
#define FOCUS_INPUT_DOC		0x620	/* !Fid - Current document */
#define FOCUS_INPUT_SEARCH	0x621	/* !Fif - Find */
#define FOCUS_INPUT_MARKER	0x622	/* !Fim - Marker */
#define FOCUS_INPUT_SPELL	0x623	/* !Fis - Spelling */
#define FOCUS_INPUT_HYPERTEXT 0x624  /* FiH - HyperText*/
#define FOCUS_INPUT_PGFFMT	0x626	/* !Fip - Paragraph format */
#define FOCUS_INPUT_FONTFMT	0x627	/* !Fic - Character format */
#define FOCUS_INPUT_COND	0x629	/* !Fio - Conditional text */
#define FOCUS_INPUT_CELLFMT	0x62A	/* Obsolete ? */
#define FOCUS_INPUT_CUSTRS	0x62B	/* !Fir - Custom ruling and shading */
#define FOCUS_INPUT_TBLFMT	0x62C	/* !Fit - Table format */
#define FOCUS_INPUT_STRWIN	0x62D	/* !Fiv - Structure window */
#define FOCUS_INPUT_VALIDATION	0x62E	/* !Fiw - Validation Kit */
#define FOCUS_INPUT_ATTREDITOR	0x62F	/* !Fia - Attribute editor kit */
#define FOCUS_INPUT_ELEM_CTX	0xA32	/* !Fie - Show Element Context kit*/

/* Close current modeless dialog */
#define KBD_CLOSE_SEARCH		0x681	/* !Cf - Search */
#define KBD_CLOSE_MARKER		0x682	/* !Cm - Marker */
#define KBD_CLOSE_SPELL			0x683	/* !Cs - Spelling */
#define KBD_CLOSE_PGFFMT		0x684	/* !Cp - Paragraph format */
#define KBD_CLOSE_FONTFMT		0x685	/* !Cc - Character format */
#define KBD_CLOSE_COND			0x686	/* !Co - Conditional text */
#define KBD_CLOSE_CUSTRS		0x687	/* !Cr - Custom ruling and shading */
#define KBD_CLOSE_TBLFMT		0x688	/* !Ct - Table format */
#define KBD_CLOSE_STRWIN		0x689	/* !Cv - Structure window */
#define KBD_CLOSE_VALIDATION	0x68A	/* !Cw - Validation Kit */
#define KBD_CLOSE_ATTREDITOR	0x68B	/* !Ca - Attribute editor kit */
#define KBD_CLOSE_ELEM_CTX		0x68C	/* !Cx - Close Element Context kit*/
#define KBD_CLOSE_HYPERTEXT		0x68D	/* !Ch - Close Hypertext kit */

/* Commands to make maker sleep for 1, .1 and .01 seconds */
#define KBD_PAUSE_1x00          0x630
#define KBD_PAUSE_0x10          0x631
#define KBD_PAUSE_0x01          0x632
/* Commands to make maker sleep for 1, .1 and .01 seconds   */
/*   before every command, except between characters        */
#define KBD_SLOW_0x00         0x633   /* sets wait-time to 0     */
#define KBD_SLOW_1x00         0x634   /* adds 1 sec to wait time */
#define KBD_SLOW_0x10         0x635   /* adds 1/10 second        */
#define KBD_SLOW_0x01         0x636   /* adds 1/100 second       */

/* Straddles debug stuff */
#define KBD_SET_TEXTFRAME_GRID 0x666 /* calls DEBUG_UiGetNumColsForAllTextFramesInFlow() */

/* Unix debugging aids */
#define KBD_PURIFY_NEW_LEAKS  0x650	/* calls purify_new_leaks() */
#define KBD_PURIFY_ALL_LEAKS  0x651	/* calls purify_all_leaks() */
#define KBD_PURIFY_CLEAR_LEAKS  0x652	/* calls purify_clear_leaks() */

/* sblock count debug aid - mostly for builder use */
#define KBD_COUNT_SBLOCKS	0x653	/* count number of sblocks in doc */

#define KBD_API					0xDF0	/* Dynamic (API clients/config) */
#define KBD_API_SHORTCUT		0xDF1	/* API used only from shortcut */
#define TYPEIN					0xDF2	/* OUTPUT-ONLY: Text type-in */
#define INLINE_TYPEIN 			0xDF3	/* Inline input for Asian text */

#define KBD_BACKSTACK	0xEF0   /* !vP - Backward Link */
#define KBD_UPSTACK		0xEF1   /* !vN - Forward Link */
#define KBD_VIEWER		0xF00	/* !Flk - Toggle view-only document or book */
#define TOGGLE_FLUID_VIEW 0x500 /* !VF - Toggle fluid view */
#define KBD_DSEXIT		0xF01	/* exercise dsexit		*/
#define KBD_MEMFAIL		0xF02	/* exercise mem_fail	*/
#define KBD_SAVEMETA	0xF03	/* !Ftc Toggle mode so Save Text saves meta Chars */ 
#define KBD_MEM_STATS	0xF04	/* print busy/free memory totals */
#define KBD_CACHE_STATS	0xF05	/* print cache statistics */

#define KBD_NEWVAR		0xF06	/* new variable @ ip */
#define KBD_UPDATEREF	0xF07	/* update ref @ ip */
#define KBD_DEREFREF	0xF08	/* dereference ref @ ip */
#define KBD_HEATREF		0xF09	/* heat reference @ ip */

/* Doc report */
#define KBD_DOC_REPORT	0xF10	/* !ftr - Document reports */

/* Document ruler commands */
#define KBD_FULLRULERS 	0x3BD   /* !oa - toggle full/abbreviated rulers */

/* Document right border commands */
#define KBD_ALLSELECT 	0xF20   /* !1s - smart selection */
#define KBD_OBJSELECT 	0xF21   /* !1o - object selection */
#define KBD_TOOLWIN     0xF22   /* !1w, !gT - tools window */
#define KBD_PGFWIN      0xF23   /* !opc - paragraph catalog window */
#define KBD_FONTWIN     0xF24   /* !occ - font catalog window */
#define KBD_RESIZEBOX   0xF25	/* window resize box */
#define KBD_MATHWIN     0xF26   /* !se, !mw - equations window */
#define KBD_RESIZEBOXM  0xF27	/* window resize box using ctrl-middle mouse*/
#define KBD_ELEMENTWIN  0xF28   /* !EC - element catalog window */
#define KBD_SMALLTOOLWIN     0xF29   /* small tools window */
#define KBD_OBJSELECT_NOPREF 0xF2A   /* shifted KBD_OBJSELECT */

/*deprecated in FM 9.0
#define KBD_TCTOOLWIN	0xF2B 
*/

/* Document bottom border commands */
#define KBD_ZOOMIN			0xF30	/* !zi - zoom in */
#define KBD_ZOOMOUT 		0xF31	/* !zo - zoom out */
#define KBD_ZOOM_FIT_PAGE  	0xF32   /* !zp - zoom fit page in window */
#define KBD_ZOOM_FIT_WINDOW 0xF33   /* !zw - zoom fit window to page */
#define KBD_ZOOM			0xF34   /* zoom */
#define KBD_ZOOM100			0xF35	/* !zz - zoom to 100% */
#define KBD_ZOOM_SET		0xF3C	/* !zs - set zoom percentages */
#define KBD_ZOOM_FIT_TEXTFRAME 0xF3F   /* !zt - zoom fit window to textframe */
#define KBD_ZOOM_AUTOFIT_AFTER_ZOOM 0xF1A   /* !zaf - toggle to enable/disable autofit after zoom */

#define KBD_TAGSTATUS       0xF36   /* Use by MS Windows */
#define KBD_PAGESTATUS      0xF37
#define KBD_HSCROLL         0xF38
#define KBD_VSCROLL         0xF39

#define KBD_RENAMEPAGE		0xF3A	/* !pN - rename master/reference page */
#define KBD_RENAMEFRAME		0xF3B	/* not bound currently */

/* Font and paragraph catalog selection quick key. */
#define KBD_FONTQUICK		0xF40	/* !qc, F8, ^8 - Char fmt quick key */
#define KBD_PGFQUICK		0xF41	/* !qp, F9, ^9 - Pgf fmt quick key */
#define KBD_VARQUICK		0xF42	/* !qv,     ^0 - Variable quick key */
#define KBD_CELLFMTQUICK	0xF43	/* obsolete ? */
#define KBD_CONDINQUICK		0xF44	/* !qC - Conditional text "In" quick key*/
#define KBD_CONDNOTINQUICK	0xF45	/* !qD - Conditional text "NotIn" quick key */
#define KBD_UNCOND			0xF46	/* !qU - Conditional Text "Unconditional" key*/
#define KBD_CONDVISONLYQUICK 0xF47  /* !qS Show One Conditional Text Tag */

#define KBD_INSERTQUICK     0xF48   /* !Ei, ^1 - insert element quick key */
#define KBD_WRAPQUICK       0xF49   /* !Ew, ^2 - wrap element quick key */
#define KBD_CHANGEQUICK     0xF4C   /* !Ec, ^3 - change element quick key */
#define KBD_ATTREDITQUICK	0xF4D	/* ^7 - attribute edit quick key */

/* Designer stuffers, take string parameter */
#define PGF_DESIGN_CAT		0xF4E
#define CHAR_DESIGN_CAT		0xF4F
#define TBL_DESIGN_CAT		0xF5F

/* Dialog commands: set all to As Is and reset. */
#define KBD_NOCHANGEDB	0xF4A	/* +[F8] - Set all items to As Is in dialog */
#define KBD_RESETDB		0xF4B	/* +[F9] - Reset dialog */

/* New Equation commands. */
#define KBD_SMEQN       	0xF50	/* !ms - Small equation */
#define KBD_MEDEQN      	0xF51	/* !mm - Medium equation */
#define KBD_LGEQN       	0xF52	/* !ml - Large equation */
#define KBD_PUTINLINE       0xF53	/* !mp - Shrinkwrap */
#define KBD_ANTIPUTINLINE   0xF54	/* !me - Expand (unwrap) */

#define KBD_EVACUATE		0xF60	/* (unbound) Force assertion botch */
#define KBD_VERIFYCONTEXT	0xF61	/* (unbound) Verify context tables */

#define KBD_SAVEASDBRE		0xF70	/* obsolete- Save to dbre, asking for name */
#define KBD_TEST_MODAL		0xF71	/* !dt - Test document as modal dialog. */
#define KBD_TEST_MODELESS	0xF72	/* !dT - Test document as modeless dialog. */
#define KBD_STUFF_ITEM		0xF73	/* !df - Set up stuff item. */
#define KBD_SAVESAS			0xF74   /* so says cmdinit */
#define	KBD_TEST_PRINTDBRE	0xF75	/* !dp - print dialog resource test */
#define KBD_SAVEFMX			0xF76   /* obsolete */
#define KBD_SAVEDBRE		0xF77	/* obsolete- Save to dbre resource file. */
#define	KBD_CAPTURE_LIVE_DIALOG	0xF78	/* !dc - capture (print) live dialog */
#define KBD_RM_MODE		    0xF79   /* !dr - Remove platform dialog */
#define KBD_DRE_MODE_X		0xF7A   /* !dx - make X the current view */
#define KBD_DRE_MODE_W		0xF7B   /* !dw - make Windows the current view */
#define KBD_DRE_MODE_M		0xF7C   /* !dm - make Mac the current view */

#define KBD_TABLE_INS       0xF80   /* !ti - Insert Table */
#define KBD_TABLE_FORMAT    0xF81   /* !td - Table Designer */
#define KBD_TABLE_CELLFMT   0xF84   /* Obsolete ? */
#define KBD_TABLE_ROWFMT    0xF85   /* !tr - Row Format */
#define KBD_TABLE_CUSTRS    0xF86   /* !tx - Custom Ruling and Shading */
#define KBD_TABLE_ADDRC     0xF87   /* !ta - Add Rows or Columns */
#define KBD_TABLE_RESIZECOL 0xF88   /* !tz - Resize columns */
#define KBD_TABLE_STRADDLE  0xF89   /* !tl - Straddle/Unstraddle */
#define KBD_TABLE_CONVERT   0xF8A   /* !tv - Convert to Table/Paragraphs */
#define KBD_TABLE_DEBUG     0xF8B
#define KBD_TABLE_RULES     0xF8C   /* !te - Edit Ruling Style */

/* continue at OxFC0 to avoid moving the items below */
#define KBD_TABLE_EXIT_IP      0xFC0 /* !tI - Move IP out of table */

/* These are the table dialog shortcuts, more below */
#define KBD_TBL_DLG_UNIFY_TF     0xF90 /* !tut - Unify Table Formats */
#define KBD_TBL_DLG_UNIFY_CF     0xF91 /* obsolete ? */
#define KBD_TBL_DLG_ADD_ABOVE    0xF92 /* !tRa - Add table rows above */
#define KBD_TBL_DLG_ADD_BELOW    0xF93 /* !tRb - Add table rows below */
#define KBD_TBL_DLG_ADD_LEFT     0xF94 /* !tcl - Add columns to left */
#define KBD_TBL_DLG_ADD_RIGHT    0xF95 /* !tcr - Add columns to right */
#define KBD_TBL_DLG_CLEAR_EMPTY  0xF96 /* !tce - Clear leaving cells empty */
#define KBD_TBL_DLG_CLEAR_X      0xF97 /* !tcx - Clear removing cells */
#define KBD_TBL_DLG_PASTE_REPL   0xF98 /* !tpr - Paste replacing selection */
#define KBD_TBL_DLG_PASTE_BEFORE 0xF99 /* !tpb - Paste table before */
#define KBD_TBL_DLG_PASTE_AFTER  0xF9A /* !tpa - Paste table after */

#define KBD_COPYCELLFMT	0xF9B		/* obsolete ? */
#define KBD_COPYCOLW	0xF9C		/* !eyw - Copy Column Width */
#define KBD_COPYCOND	0xF9D		/* !eyd - Copy Condition Setting */
#define KBD_USEELTDEFSFROM 0xF9E	/* !fie - Import Element Definitions */
#define KBD_COPYATTRS	0xF9F		/* !eA - Copy Element Attributes */

/* Miscellaneous graphics commands. */
#define KBD_ATOMIZE_INSET	    0xFAA   /* !gU - Ungroup FmVect import. */
#define KBD_SWAP_RED_BLUE   	0xFAB   /* !RedBlue - Swap red and blue for 24-bit frameimage. */
#define KBD_REWRAP_INLINE_MATH	0xFAC   /* !rwmath - Shrink-wrap all inline math. */
#define KBD_MODE_ROTATE_TOOL	0xFAD	/* Rotate selected object */
#define KBD_XYZZY		0xFED
#define KBD_XYZZZ		0xEE0
#define KBD_XYZZQ		0xEE1


/* Design kits */
#define PGF_DESIGNKIT_APPLY		 0xAAA
#define CHAR_DESIGNKIT_APPLY	 0xAAB
#define TBL_DESIGNKIT_APPLY		 0xAAC
#define PGF_DESIGNKIT_UPDATEALL	 0xAAD
#define CHAR_DESIGNKIT_UPDATEALL 0xAAE
#define TBL_DESIGNKIT_UPDATEALL	 0xAAF
#define PGF_DESIGNKIT_NEWFORMAT	 0xB00
#define CHAR_DESIGNKIT_NEWFORMAT 0xB01
#define TBL_DESIGNKIT_NEWFORMAT	 0xB02
#define PGF_DESIGNKIT_UPDATEOPTIONS	 0xB03
#define CHAR_DESIGNKIT_UPDATEOPTIONS 0xB04
#define TBL_DESIGNKIT_UPDATEOPTIONS	 0xB05
#define KBD_PGFFMT_DELETE  	0xB06
#define KBD_CHARFMT_DELETE 	0xB07
#define KBD_TBLFMT_DELETE 	0xB08
/*Reset button on the new designer dialogs*/
#define	PGF_DESIGNKIT_RESET	0xABA
#define CHAR_DESIGNKIT_RESET 0xABB
#define TBL_DESIGNKIT_RESET 0xABC

/* Cond text kit */
#define CONDTEXT_KIT_APPLY	0xB09

/*Alternate invocation for WorkSpace*/
#define ALT_TXT_INSET		0xB11
#define ALT_GFX_INSET		0xB12

/* Table selection. */
#define KBD_TBLSEL_CELL     0xFA0	/* !the - Select the current cell, then next. */
#define KBD_TBLSEL_ROW      0xFA1	/* !thr - Select the current row, then next. */
#define KBD_TBLSEL_COL      0xFA2	/* !thc - Select the current col, then next. */
#define KBD_TBLSEL_TABLE    0xFA3	/* !tht - Select the current table. */
#define KBD_TBLSEL_CELLTEXT	0xFA4	/* !tha - Select all text in the cell. */
#define KBD_TBLSEL_COLBODY  0xFA5   /* !thb - Select all body cells in the column. */

/* Table insertion point navigation. */
#define KBD_TBLIP_TOPLEFT   0xFB0   /* !tms - IP to top left cell of table selection. */
#define KBD_TBLIP_RIGHT		0xFB1	/* !tmr - IP to cell on right. */
#define KBD_TBLIP_LEFT		0xFB2	/* !tml - IP to cell on left. */
#define KBD_TBLIP_ABOVE		0xFB3	/* !tmu - IP to cell above. */
#define KBD_TBLIP_BELOW		0xFB4	/* !tmd IP to cell below. */
#define KBD_TBLIP_LEFTMOST	0xFB5	/* !tma IP to left most cell in current row. */
#define KBD_TBLIP_RIGHTMOST	0xFB6	/* !tme - IP to right most cell in current row. */
#define KBD_TBLIP_NEXT		0xFB7	/* !tmn - IP to next logical cell. */
#define KBD_TBLIP_PREV		0xFB8	/* !tmp - IP to next previous cell. */
#define KBD_TBLIP_TOP		0xFB9	/* !tmt - IP to top cell in current column. */
#define KBD_TBLIP_BOTTOM	0xFBA	/* !tmb - IP to bottom cell in current column. */

/* These are part of the PGF_ shortcuts. */
#define PGF_KBD_SIDEBODY 				0xFC9 /* !jpn - Pgf placement normal */
#define PGF_KBD_SIDEHEAD_FIRST_BASELINE 0xFCA /* Align first baseline */
#define PGF_KBD_SIDEHEAD_LAST_BASELINE 	0xFCB /* Align last baseline */
#define PGF_KBD_SIDEHEAD_TOP 			0xFCC /* Align Tops */
#define PGF_KBD_RUN_IN 					0xFCD /* !jpr - Pgf placement run-in */
#define PGF_KBD_FULLSTRADDLE			0xFCE /* !jpT - Pgf placement full straddle */
#define PGF_KBD_BODYSTRADDLE			0xFCF /* !jpt - Pgf placement body straddle */

/* More table dialog shortcuts */
#define KBD_TBL_DLG_SHRINKWRAP   0xFD0  /* !tw - Shrink wrap column width to */
/*       selected cells contents. */
/* structure  */
#define ELEM_INS_CAT_AT_SEL		0xA10	/* Insert from element catalog */
#define ELEM_WRAP_CAT_AT_SEL	0xA11	/* Wrap from element catalog */
#define ELEM_CHANGE_CAT_AT_SEL	0xA12	/* Change from element catalog */

#define ELEM_INSERT_CUSTOM_1	0xA21	/* Insert first custom element */
#define ELEM_INSERT_CUSTOM_2	0xA22	/* Insert second custom element */
#define ELEM_INSERT_CUSTOM_3	0xA23	/* Insert third custom element */
#define ELEM_INSERT_CUSTOM_4	0xA24	/* Insert fourth custom element */
#define ELEM_INSERT_CUSTOM_5	0xA25	/* Insert fifth custom element */
#define ELEM_INSERT_CUSTOM_6	0xA26	/* Insert sixth custom element */
#define ELEM_INSERT_CUSTOM_7	0xA27	/* Insert seventh custom element */
#define ELEM_INSERT_CUSTOM_8	0xA28	/* Insert eighth custom element */
#define ELEM_INSERT_CUSTOM_9	0xA29	/* Insert ninth custom element */
#define ELEM_INSERT_CUSTOM_10	0xA2A	/* Insert tenth custom element */

#define KBD_STRIP_FLOWSTRUCTURE	0xA30	/* !ssf - Remove structure from flow */
#define KBD_SHOW_ELEM_CTX		0xA31	/* !fDE - Show element context */
#define KBD_NORMALIZE_TAGS 		0xA33   /* !ftf - Catalogize pgf and char formats */

#define KBD_STRWIN          0xFD1   /* !EV - Structure View */
#define KBD_ELEM_BORDER 	0xFD2	/* !vE  - Element borders */
#define KBD_ELEM_MERGE_1ST 	0xFD3   /* !Em  - Merge */
#define KBD_ELEM_MERGE_LAST 0xFD4   /* !EM  - Merge into last */
#define KBD_ELEM_SPLIT		0xFD5   /* !Es  - Split  */
#define KBD_ELEM_UNWRAP		0xFD6   /* !Eu  - Unwrap  */
#define KBD_ELEM_CAT_OPTS   0xFD7   /* !EOC - Set available elements */
#define KBD_SETELCATSTRICT  0xFD8   /* !elv - List valid elements working */
/*        from start to end */
#define KBD_SETELCATLOOSE   0xFD9   /* !elu - List valid elements any order */
#define KBD_SETELCATCHILD   0xFDA   /* !elc - List elements allowed */
/*        anywhere in parent */
#define KBD_SETELCATALL     0xFDB   /* !ela - List all elements */
#define KBD_SETELCATFREQ	0xFF7 	/* !elf - List frequently used elements */
#define ELEM_BAM			0xFDC	/* !eer - Repeat last insert/wrap/change */
#define KBD_ELEM_TAGS		0xFDD	/* !vT  - Element boundaries as Tags */
#define KBD_ELEM_PROMOTE	0xFDE	/* !EP  - Promote element */
#define KBD_ELEM_DEMOTE		0xFDF	/* !ED  - Demote element*/

#define KBD_VALIDATION		0xFE0   /* !Ev  - Validate */
#define KBD_VAL_ELEM		0xFE1   /* !ve  - Validate element */
#define KBD_VAL_FLOW		0xFE2   /* !vf  - Validate flow */
#define KBD_VAL_DOC			0xFE3   /* !vd  - Validate document */
#define KBD_VAL_IGNORE		0xFE4   /* !vie - Ignore missing elements */
#define KBD_VAL_START		0xFE5   /* !vn  - Start validating */
#define KBD_VAL_ALLOW		0xFE6   /* !vae - Allow as special case */
#define KBD_VAL_CLEAR		0xFE7   /* !vce - Clear special validation cases */

#define SW_CSR_UP			0xFE8
#define SW_CSR_DOWN			0xFE9
#define SW_CSR_RIGHT		0xFEA
#define SW_CSR_LEFT			0xFEB

#define KBD_ATTRIBUTE_EDIT	0xFEC	/* !EA - Edit Attributes */
#define KBD_ATTR_DISP_OPTS	0xFEE   /* !vA - Attribute Display */
#define KBD_FB_EDIT_PREFS	0xFEF   /* EOI - New Element Options */
#define KBD_REMOVE_STRUCT	0xFF0

#define KBD_TOGGLE_INCLUSION	0xFF1 /* !eli - Toggle inclusion grouping */
#define KBD_TOGGLE_COLLAPSE		0xFF2 /* !Ex  - Toggle element collapse */
#define KBD_TOGGLE_COLLAPSE_ALL	0xFF3 /* !EX  - Toggle collapse all siblings */
#define KBD_ELEM_TRANSPOSE_PREV	0xFF4 /* !Et  - Transpose with previous */
#define KBD_ELEM_TRANSPOSE_NEXT	0xFF5 /* !ET  - Transpose with next */
#define KBD_VAL_IGNORE_ATTR		0xFF6 /* !via - Ignore missing attributes */

#define KBD_NAMESPACES		0xFF8	/* !EN - Namespaces */
#define KBD_TOGGLE_STRUCT_AND_DOCWIN		0xFF9	/* !sd - Toggle struct and doc windows */

/* Misc Commands */
#define FM_SEPARATOR	0xF12	/* Menu item Separator */
#define FM_TERMINATE	0xFFF	/* Quit FrameMaker */


/* Currently, the 0x0Dxx range is Mac specific, but not necessarily so. tpl */
#define FM_RPT_CMDS_BY_TAG		0xD00
#define FM_RPT_CMDS_BY_SHORTCUT	0xD01

#define KBD_PASTESPECIAL		0xD10

#define KBD_CLOSE_TOP_PALETTE			0xD20
#define KBD_CLOSE_PARAGRAPH_CATALOG		0xD21
#define KBD_CLOSE_CHARACTER_CATALOG		0xD22
#define KBD_CLOSE_TOOLS_PALETTE			0xD23
#define KBD_CLOSE_EQUATIONS_PALETTE		0xD24
#define KBD_CLOSE_ELEMENT_CATALOG		0xD26


/*deprecated in FM 9.0
#define KBD_CLOSE_COMMAND_PALETTE		0xD27
*/

#define KBD_CLOSE_PARAGRAPH_DESIGNER	0xD28 /* TODO: OUTPUT ONLY */
#define KBD_CLOSE_CHARACTER_DESIGNER	0xD29 /* TODO: OUTPUT ONLY */
#define KBD_CLOSE_TABLE_DESIGNER		0xD2A /* TODO: OUTPUT ONLY */

/*deprecated in FM 9.0
#define	KBD_COMMAND_PALETTE				0xD30 ( !vq - Quick Access Bar )
*/

#define KBD_WINDOWFULL_UP				0xD40 /* !vsp - Scroll prev screen */
#define KBD_WINDOWFULL_DOWN				0xD41 /* !vsn - Scroll next screen */

/* These two temporarily replaced KBD_WINDOWFULL_UP/DOWN during FM5.5 development */
/* #define SCROLL_TO_PREVSCREEN			0xD42 OBSOLETE */
/* #define SCROLL_TO_NEXTSCREEN			0xD43 OBSOLETE */

#define	KBD_VIEWER_MAKE_DOCUMENT		0xD50
#define	KBD_VIEWER_MAKE_PALETTE			0xD51
#define	KBD_VIEWER_MAKE_MODELESSDIALOG	0xD52

#define KBD_Item1stLogical				0xD60
#define KBD_ItemNextLogical				0xD61
#define KBD_ItemPrevLogical				0xD62
#define KBD_ItemNextPhysical			0xD63
#define KBD_ItemPrevPhysical			0xD64
#define KBD_ItemLeft					0xD65
#define KBD_ItemRight					0xD66
#define KBD_ItemUp						0xD67
#define KBD_ItemDown					0xD68
#define KBD_ItemSelect					0xD69
#define KBD_ItemFirstFocus				0xD6A

/* Instantiates a dialog. Similar to START_DIALOG, but
* that fcode will not work in a command table,
* since it is greater than 4k (0xC100).
*/

#define INIT_DIALOG						0xD80

/* Windows specific fcodes...  */

#define KBD_RENAMEORPLAIN   0x900

/*deprecated in FM 9.0
#define KBD_TOOLBAR	       	0x901
#define KBD_RIBBONBAR		0x902
*/

#define KBD_WIN_CASCADE		0x903
#define KBD_WIN_TILE		0x904

/*deprecated in FM 9.0
#define KBD_WIN_ARRANGEICON	0x905
*/

#define KBD_TAB_LEFT		0x906
#define KBD_TAB_CENTER		0x907
#define KBD_TAB_RIGHT		0x908
#define KBD_TAB_DECIMAL		0x909
#define KBD_PRINTSETUP		0x90A

/*deprecated in FM 9.0
#define KBD_TOOLBAR_NEXTPAGE	0x90B
#define KBD_TOOLBAR_PREVPAGE	0x90C
#define KBD_QACCESS_TOGGLE     	0x90D
#define KBD_QAB_HELP			0x90E
*/
#define KBD_MINIMIZE			0x90F
#define KBD_MAXIMIZE			0x910
#define KBD_RESTORE				0x911
#define KBD_EDITLINKS			0x912
#define KBD_INSERTOBJECT		0x913
#define KBD_CONTEXTMENU			0x914
#define KBD_EMBEDDED00			0x920
#define KBD_EMBEDDED01			0x921
#define KBD_EMBEDDED02			0x922
#define KBD_EMBEDDED03			0x923
#define KBD_EMBEDDED04			0x924
#define KBD_EMBEDDED05			0x925
#define KBD_EMBEDDED06			0x926
#define KBD_EMBEDDED07			0x927
#define KBD_EMBEDDED08			0x928
#define KBD_EMBEDDED09			0x92A
#define KBD_EMBEDDED10			0x92B
#define KBD_EMBEDDED11			0x92C
#define KBD_EMBEDDED12			0x92D
#define KBD_EMBEDDED13			0x92E
#define KBD_EMBEDDED14			0x92F
#define KBD_EMBEDDED15			0x930
#define KBD_EMBEDDED16			0x931
#define KBD_EMBEDDED17			0x932
#define KBD_EMBEDDED18			0x933
#define KBD_EMBEDDED19			0x934
#define KBD_EMBEDDEDMAX			0x94F
/* NOTE: Codes through 0x94F to 0x970 reserved for future expansion */

/*NOTE: the range 0x970 to 0x98f will be used for OWL */
#define KBD_GOTOLINEN			0x970 	/* Only used internally */ 
#define KBD_SHOWNEXT			0x971	/*^F6*/		
#define KBD_SHOWPREV			0x972	/*^+F6*/		

#if NOTDEF
/*Theme*/
#define KBD_OWLTHEME_LOAD		0x974	/* Load Owl Theme */
#define KBD_OWLTHEME_SAVE		0x975	/* Save Owl Theme */
#endif 

/*Screen Modes*/
#define KBD_SCREENMODE_TOGGLE			0x978	/* Toogle Screen Mode For a Document*/
#define KBD_SCREENMODE_STANDARD			0x979	/* Standard Screen Mode*/
#define KBD_SCREENMODE_FULLSCREEN_UI	0x97A	/* Full Screen Mode with UI visible*/
#define KBD_SCREENMODE_FULLSCREEN		0x97B	/* Full Screen Mode with only document visible*/

#define KBD_UI_PREFERENCE		0x980	/* Ui Preference Dialog*/

/*ToolBar*/
#define KBD_TOOLBAR_SHOWALL		0x989	/* Show All the toolbars */
#define KBD_TOOLBAR_HIDEALL		0x98A 	/* Hide All the toolbars */
#define KBD_TOOLBAR_BASE		0x98F   /* ToolBar Selected from Menu*/


#define KBD_MATH_BASE	0x1000

#define TEXTSEL_QUICK_COPY		0x0001
#define TEXTSEL_EXTEND			0x0002
#define TEXTSEL_EXTEND_WORD		0x0004
#define TEXTSEL_EXTEND_LINE		0x0008
#define TEXTSEL_EXTEND_PGF		0x0010
#define TEXTSEL_WORD			0x0020
#define TEXTSEL_LINE			0x0040
#define TEXTSEL_PGF				0x0080
#define TEXTSEL_SELECT_ONLY 	0x0100
#define TEXTSEL_EXTEND_SENT 	0x0200
#define TEXTSEL_SENT			0x0400
#define TEXTSEL_EXTEND_ELEMENT	0x0800
#define TEXTSEL_ELEMENT			0x1000
#define TEXTSEL_DRAGGING		0x2000

#define EXTEND_SEL (TEXTSEL_EXTEND | \
	TEXTSEL_EXTEND_WORD | \
	TEXTSEL_EXTEND_LINE | \
	TEXTSEL_EXTEND_PGF | \
	TEXTSEL_EXTEND_ELEMENT)

/*
These are not function codes but rather
are the flags in the MIF Save Options Mask.
These are used by DocServer.
*/

#define MIF_SAVE_TEXT		0x000001
#define MIF_SAVE_TAGS		0x000002
#define MIF_SAVE_FMTS		0x000004
#define MIF_SAVE_FONTS		0x000008
#define MIF_SAVE_MKRS		0x000010
#define MIF_SAVE_AFMS		0x000020
#define MIF_SAVE_LAYT		0x000040
#define MIF_SAVE_MPAGE		0x000080
#define MIF_SAVE_FCAT		0x000100
#define MIF_SAVE_PCAT		0x000200
#define MIF_SAVE_CCAT		0x000400
#define MIF_SAVE_TMPLT		0x000800
#define MIF_SAVE_DICT		0x001000
#define MIF_SAVE_VARS		0x002000
#define MIF_SAVE_TABLECATS	0x004000
#define MIF_SAVE_TABLES		0x008000
#define MIF_SAVE_ECAT		0x010000
#define MIF_SAVE_ELEMENTS	0x020000
#define MIF_SAVE_COLORCAT   0x040000
#define MIF_SAVE_VIEWSET    0x080000
#define MIF_SAVE_DATALINKS	0x100000
#define MIF_SAVE_FPLCAT		0x200000
#define MIF_SAVE_TEXTINSETS	0x400000
#define MIF_SAVE_RUBIS		0x800000
#endif /* FCODES_H */

