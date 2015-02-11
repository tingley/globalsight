/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

var Delimitor      = new Array();
Delimitor['tab']   = 0;
Delimitor[';']     = 1;
Delimitor[',']     = 2;
Delimitor['space'] = 3;
Delimitor['other'] = 4;

var Encodings = new Array();
Encodings['ASCII'] = 0;
Encodings['Big5'] = 1;
Encodings['EUC-CN'] = 2;
Encodings['EUC-JP'] = 3;
Encodings['EUC-KR'] = 4;
Encodings['EUC-TW'] = 5;
Encodings['GB2312'] = 6;
Encodings['ISO-2022-CN'] = 7;
Encodings['ISO-2022-JP'] = 8;
Encodings['ISO-2022-KR'] = 9;
Encodings['ISO-8859-1'] = 10;
Encodings['ISO-8859-15'] = 11;
Encodings['ISO-8859-2'] = 12;
Encodings['ISO-8859-3'] = 13;
Encodings['ISO-8859-4'] = 14;
Encodings['ISO-8859-5'] = 15;
Encodings['ISO-8859-6'] = 16;
Encodings['ISO-8859-7'] = 17;
Encodings['ISO-8859-8'] = 18;
Encodings['ISO-8859-9'] = 19;
Encodings['Johab'] = 20;
Encodings['KOI8-R'] = 21;
Encodings['Shift_JIS'] = 22;
Encodings['TIS-620'] = 23;
Encodings['UTF-16BE'] = 24;
Encodings['UTF-16LE'] = 25;
Encodings['UTF-8'] = 26;
Encodings['Unicode'] = 27;
Encodings['Windows-1250'] = 28;
Encodings['Windows-1251'] = 29;
Encodings['Windows-1252'] = 30;
Encodings['Windows-1253'] = 31;
Encodings['Windows-1254'] = 32;
Encodings['Windows-1255'] = 33;
Encodings['Windows-1256'] = 34;
Encodings['Windows-1257'] = 35;
Encodings['Windows-1258'] = 36;
Encodings['Windows-874'] = 37;
Encodings['Windows-932'] = 38;
Encodings['Windows-936'] = 39;
Encodings['Windows-949'] = 40;
Encodings['Windows-950'] = 41;
