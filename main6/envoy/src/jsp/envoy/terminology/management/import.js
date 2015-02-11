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
Encodings['UTF-8'] = 0;
Encodings['Windows-1252'] = 1;
Encodings['Unicode'] = 2;
Encodings['UTF-16LE'] = 3;
Encodings['UTF-16BE'] = 4;
Encodings['ASCII'] = 5;
Encodings['Big5'] = 6;
Encodings['EUC-CN'] = 7;
Encodings['EUC-JP'] = 8;
Encodings['EUC-KR'] = 9;
Encodings['EUC-TW'] = 10;
Encodings['GB2312'] = 11;
Encodings['ISO-2022-CN'] = 12;
Encodings['ISO-2022-JP'] = 13;
Encodings['ISO-2022-KR'] = 14;
Encodings['ISO-8859-1'] = 15;
Encodings['ISO-8859-2'] = 16;
Encodings['ISO-8859-3'] = 17;
Encodings['ISO-8859-4'] = 18;
Encodings['ISO-8859-5'] = 19;
Encodings['ISO-8859-6'] = 20;
Encodings['ISO-8859-7'] = 21;
Encodings['ISO-8859-8'] = 22;
Encodings['ISO-8859-9'] = 23;
Encodings['ISO-8859-15'] = 24;
Encodings['Johab'] = 25;
Encodings['KOI8-R'] = 26;
Encodings['Shift_JIS'] = 27;
Encodings['TIS-620'] = 28;
Encodings['Windows-874'] = 29;
Encodings['Windows-932'] = 30;
Encodings['Windows-936'] = 31;
Encodings['Windows-949'] = 32;
Encodings['Windows-950'] = 33;
Encodings['Windows-1250'] = 34;
Encodings['Windows-1251'] = 35;
Encodings['Windows-1253'] = 36;
Encodings['Windows-1254'] = 37;
Encodings['Windows-1255'] = 38;
Encodings['Windows-1256'] = 39;
Encodings['Windows-1257'] = 40;
Encodings['Windows-1258'] = 41;
