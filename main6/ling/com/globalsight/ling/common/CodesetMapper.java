/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.common;

import java.util.*;

/**
 * <p>Maps IANA codesets to Java codesets and vice versa.
 * See {@link <A href="ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets">IANA Character Sets</A>}.</p>
 */
public class CodesetMapper
{
    private static Hashtable IanaCharset = mapIanaCharset();
    private static Hashtable JavaEncoding = mapJavaEncoding();


    /**
     * Map Java encoding to IANA charset
     * @param javaEncoding Java encoding name
     * @return IANA charset name. If the charset name cannot be
     * determined, null is returned.
     */
    public static String getIanaCharset(String javaEncoding)
    {
        return (String)JavaEncoding.get(javaEncoding);
    }

    /**
     * Map IANA charset to Java encoding
     * @param ianaCharset charset name registerd with IANA
     * @return Java encoding name. If the encoding name cannot be
     * determined, null is returned.
     */
    public static String getJavaEncoding(String ianaCharset)
    {
        String lower_cased = ianaCharset.toLowerCase();
        return (String)IanaCharset.get(lower_cased);
    }

    /**
     * Map Windows charset code to Java encoding.
     */
    public static String getJavaEncodingFromCharset(int code)
    {
        switch (code)
        {
        case   0: return "Cp1252";                // ANSI
                                                  // 1 = DEFAULT
                                                  // 2 = SYMBOL
                                                  // 3 = INVALID
        case  77: return "MacRoman";              // MAC
        case 128: return "MS932";                 // Shift Jis
        case 129: return "Cp949";                 // Hangul (Korean)
        case 130: return "Johab";                 // Johab (Johab)
        case 134: return "GBK";                   // GB2312
        case 136: return "Big5";                  // Big5
        case 161: return "Cp1253";                // Greek
        case 162: return "Cp1254";                // Turkish
        case 163: return "Cp1258";                // Vietnamese
        case 177: return "Cp1255";                // Hebrew
        case 178: return "Cp1256";                // Arabic
                                                  // 180 = Arabic User
                                                  // 181 = Hebrew User
        case 186: return "Cp1257";                // Baltic
        case 204: return "Cp1251";                // Russian
        case 222: return "Cp874"; // or TIS620 ?? // Thai
        case 238: return "Cp1250";                // Eastern European
        case 254: return "Cp437";                 // PC 437
                                                  // 255 = OEM
        default:
            return null;
        }
    }

    /**
     * Map from IANA to JAVA charset.
     */
    private static Hashtable mapIanaCharset()
    {
        Hashtable table = new Hashtable();

        // US-ASCII (preferred MIME name)
        table.put("ansi_x3.4-1968", "ASCII");
        table.put("iso-ir-6", "ASCII");
        table.put("ansi_x3.4-1986", "ASCII");
        table.put("iso_646.irv:1991", "ASCII");
        table.put("ascii", "ASCII");
        table.put("iso646-us", "ASCII");
        table.put("us-ascii", "ASCII");
        table.put("us", "ASCII");
        table.put("ibm367", "ASCII");
        table.put("cp367", "ASCII");

        // Korean KSC
        table.put("ks_c_5601-1987", "Cp949");
        table.put("iso-ir-149", "Cp949");
        table.put("ks_c_5601-1989", "Cp949");
        table.put("ksc_5601", "Cp949");
        table.put("korean", "Cp949");

        // ISO-2022-KR  (preferred MIME name)
        table.put("iso-2022-kr", "ISO2022KR");

        // EUC-KR  (preferred MIME name)
        table.put("euc-kr", "EUC_KR");

        // EUC-CN
        table.put("euc-cn", "EUC_CN");

        // EUC-TW
        table.put("euc-tw", "EUC_TW");

        // ISO-2022-JP  (preferred MIME name)
        table.put("iso-2022-jp", "ISO2022JP");

        // ISO-2022-CN
        table.put("iso-2022-cn", "ISO2022CN");

        // JIS_X0208-1983
        table.put("jis_c6226-1983", "JIS0208");
        table.put("iso-ir-87", "JIS0208");
        table.put("x0208", "JIS0208");
        table.put("jis_x0208-1983", "JIS0208");

        // ISO-8859-1 (preferred MIME name)
        table.put("iso_8859-1:1987", "ISO8859_1");
        table.put("iso-ir-100", "ISO8859_1");
        table.put("iso_8859-1", "ISO8859_1");
        table.put("iso-8859-1", "ISO8859_1");
        table.put("latin1", "ISO8859_1");
        table.put("l1", "ISO8859_1");
        table.put("ibm819", "ISO8859_1");
        table.put("cp819", "ISO8859_1");

        // ISO-8859-2 (preferred MIME name)
        table.put("iso_8859-2:1987", "ISO8859_2");
        table.put("iso-ir-101", "ISO8859_2");
        table.put("iso_8859-2", "ISO8859_2");
        table.put("iso-8859-2", "ISO8859_2");
        table.put("latin2", "ISO8859_2");
        table.put("l2", "ISO8859_2");

        // ISO-8859-3 (preferred MIME name)
        table.put("iso_8859-3:1988", "ISO8859_3");
        table.put("iso-ir-109", "ISO8859_3");
        table.put("iso_8859-3", "ISO8859_3");
        table.put("iso-8859-3", "ISO8859_3");
        table.put("latin3", "ISO8859_3");
        table.put("l3", "ISO8859_3");

        // ISO-8859-4 (preferred MIME name)
        table.put("iso_8859-4:1988", "ISO8859_4");
        table.put("iso-ir-110", "ISO8859_4");
        table.put("iso_8859-4", "ISO8859_4");
        table.put("iso-8859-4", "ISO8859_4");
        table.put("latin4", "ISO8859_4");
        table.put("l4", "ISO8859_4");

        // ISO-8859-5 (preferred MIME name)
        table.put("iso_8859-5:1988", "ISO8859_5");
        table.put("iso-ir-144", "ISO8859_5");
        table.put("iso_8859-5", "ISO8859_5");
        table.put("iso-8859-5", "ISO8859_5");
        table.put("cyrillic", "ISO8859_5");

        // ISO-8859-6 (preferred MIME name)
        table.put("iso_8859-6:1987", "ISO8859_6");
        table.put("iso-ir-127", "ISO8859_6");
        table.put("iso_8859-6", "ISO8859_6");
        table.put("iso-8859-6", "ISO8859_6");
        table.put("ecma-114", "ISO8859_6");
        table.put("asmo-708", "ISO8859_6");
        table.put("arabic", "ISO8859_6");

        // ISO-8859-7 (preferred MIME name)
        table.put("iso_8859-7:1987", "ISO8859_7");
        table.put("iso-ir-126", "ISO8859_7");
        table.put("iso_8859-7", "ISO8859_7");
        table.put("iso-8859-7", "ISO8859_7");
        table.put("elot_928", "ISO8859_7");
        table.put("ecma-118", "ISO8859_7");
        table.put("greek", "ISO8859_7");
        table.put("greek8", "ISO8859_7");

        // ISO-8859-8 (preferred MIME name)
        table.put("iso_8859-8:1988", "ISO8859_8");
        table.put("iso-ir-138", "ISO8859_8");
        table.put("iso_8859-8", "ISO8859_8");
        table.put("iso-8859-8", "ISO8859_8");
        table.put("hebrew", "ISO8859_8");

        // ISO-8859-9 (preferred MIME name)
        table.put("iso_8859-9:1989", "ISO8859_9");
        table.put("iso-ir-148", "ISO8859_9");
        table.put("iso_8859-9", "ISO8859_9");
        table.put("iso-8859-9", "ISO8859_9");
        table.put("latin5", "ISO8859_9");
        table.put("l5", "ISO8859_9");

        // ISO_8859-15
        table.put("iso-8859-15", "ISO8859_15_FDIS");

        // JIS_X0212-1990
        table.put("jis_x0212-1990", "JIS0212");
        table.put("x0212", "JIS0212");
        table.put("iso-ir-159", "JIS0212");

        // JIS_X0201
        table.put("jis_x0201", "JIS0201");
        table.put("x0201", "JIS0201");

        // cp037
        table.put("ibm037", "Cp037");
        table.put("cp037", "Cp037");
        table.put("ebcdic-cp-us", "Cp037");
        table.put("ebcdic-cp-ca", "Cp037");
        table.put("ebcdic-cp-wt", "Cp037");
        table.put("ebcdic-cp-nl", "Cp037");

        // cp273
        table.put("ibm273", "Cp273");
        table.put("cp273", "Cp273");

        // cp277
        table.put("ibm277", "Cp277");
        table.put("ebcdic-cp-dk", "Cp277");
        table.put("ebcdic-cp-no", "Cp277");

        // cp278
        table.put("ibm278", "Cp278");
        table.put("cp278", "Cp278");
        table.put("ebcdic-cp-fi", "Cp278");
        table.put("ebcdic-cp-se", "Cp278");

        // cp280
        table.put("ibm280", "Cp280");
        table.put("cp280", "Cp280");
        table.put("ebcdic-cp-it", "Cp280");

        // cp284
        table.put("ibm284", "Cp284");
        table.put("cp284", "Cp284");
        table.put("ebcdic-cp-es", "Cp284");

        // cp285
        table.put("ibm285", "Cp285");
        table.put("cp285", "Cp285");
        table.put("ebcdic-cp-gb", "Cp285");

        // cp297
        table.put("ibm297", "Cp297");
        table.put("cp297", "Cp297");
        table.put("ebcdic-cp-fr", "Cp297");

        // cp420
        table.put("ibm420", "Cp420");
        table.put("cp420", "Cp420");
        table.put("ebcdic-cp-ar1", "Cp420");

        // cp424
        table.put("ibm424", "Cp424");
        table.put("cp424", "Cp424");
        table.put("ebcdic-cp-he", "Cp424");

        // cp437
        table.put("ibm437", "Cp437");
        table.put("cp437", "Cp437");
        table.put("437", "Cp437");

        // cp500
        table.put("ibm500", "Cp500");
        table.put("cp500", "Cp500");
        table.put("ebcdic-cp-be", "Cp500");
        table.put("ebcdic-cp-ch", "Cp500");

        // cp775
        table.put("ibm775", "Cp775");
        table.put("cp775", "Cp775");

        // cp850
        table.put("ibm850", "Cp850");
        table.put("cp850", "Cp850");
        table.put("850", "Cp850");

        // cp852
        table.put("ibm852", "Cp852");
        table.put("cp852", "Cp852");
        table.put("852", "Cp852");

        // cp855
        table.put("ibm855", "Cp855");
        table.put("cp855", "Cp855");
        table.put("855", "Cp855");

        // cp857
        table.put("ibm857", "Cp857");
        table.put("cp857", "Cp857");
        table.put("857", "Cp857");

        // cp860
        table.put("ibm860", "Cp860");
        table.put("cp860", "Cp860");
        table.put("860", "Cp860");

        // cp861
        table.put("ibm861", "Cp861");
        table.put("cp861", "Cp861");
        table.put("861", "Cp861");
        table.put("cp-is", "Cp861");

        // cp862
        table.put("ibm862", "Cp862");
        table.put("cp862", "Cp862");
        table.put("862", "Cp862");

        // cp863
        table.put("ibm863", "Cp863");
        table.put("cp863", "Cp863");
        table.put("863", "Cp863");

        // cp864
        table.put("ibm864", "Cp864");
        table.put("cp864", "Cp864");

        // cp865
        table.put("ibm865", "Cp865");
        table.put("cp865", "Cp865");
        table.put("865", "Cp865");

        // cp866
        table.put("ibm866", "Cp866");
        table.put("cp866", "Cp866");
        table.put("866", "Cp866");

        // cp868
        table.put("ibm868", "Cp868");
        table.put("cp868", "Cp868");
        table.put("cp-ar", "Cp868");

        // cp869
        table.put("ibm869", "Cp869");
        table.put("cp869", "Cp869");
        table.put("869", "Cp869");
        table.put("cp-gr", "Cp869");

        // cp870
        table.put("ibm870", "Cp870");
        table.put("cp870", "Cp870");
        table.put("ebcdic-cp-roece", "Cp870");
        table.put("ebcdic-cp-yu", "Cp870");

        // cp871
        table.put("ibm871", "Cp871");
        table.put("cp871", "Cp871");
        table.put("ebcdic-cp-is", "Cp871");

        // cp918
        table.put("ibm918", "Cp918");
        table.put("cp918", "Cp918");
        table.put("ebcdic-cp-ar2", "Cp918");

        // cp1026
        table.put("ibm1026", "Cp1026");
        table.put("cp1026", "Cp1026");

        // KOI8-R  (preferred MIME name)
        table.put("koi8-r", "KOI8_R");

        // UTF-8
        table.put("utf-8", "UTF8");

        // JIS_Encoding (ISO-2022-JP)
        table.put("jis_encoding", "ISO2022JP");

        // Shift_JIS  (preferred MIME name)
        table.put("shift_jis", "MS932");
        table.put("ms_kanji ", "MS932");
        table.put("x-sjis", "MS932");

        // EUC-JP  (preferred MIME name)
        table.put("extended_unix_code_packed_format_for_japanese", "EUC_JP");
        table.put("euc-jp", "EUC_JP");

        // IBM-Thai
        table.put("ibm-thai", "Cp874");

        // GB2312  (preferred MIME name)
        table.put("gb2312", "GBK");

        // Big5  (preferred MIME name)
        table.put("big5", "Big5");

        // Johab
        table.put("johab", "Johab");

        // Windows-874
        table.put("windows-874", "MS874");

        // Windows-932
        table.put("windows-932", "MS932");

        // Windows-936
        table.put("windows-936", "MS936");

        // Windows-949
        table.put("windows-949", "MS949");

        // Windows-950
        table.put("windows-950", "MS950");

        // windows-1250 Windows 3.1 Eastern European
        table.put("windows-1250", "Cp1250");

        // windows-1251 Windows 3.1 Cyrillic
        table.put("windows-1251", "Cp1251");

        // windows-1252 Western-European
        table.put("windows-1252", "Cp1252");
        table.put("cp1252", "Cp1252");
        table.put("ibm1252", "Cp1252");

        // windows-1253 Greek
        table.put("windows-1253", "Cp1253");

        // windows-1254 Turkish
        table.put("windows-1254", "Cp1254");

        // windows-1255 Hebrew
        table.put("windows-1255", "Cp1255");

        // windows-1256 Arabic
        table.put("windows-1256", "Cp1256");

        // windows-1257 Baltic
        table.put("windows-1257", "Cp1257");

        // windows-1258 Vietnamese
        table.put("windows-1258", "Cp1258");

        // TIS-620 (Thai)
        table.put("tis-620", "TIS620");

        // CvdL: this may be a hack but Java seems to know about it
        table.put("unicode", "UnicodeLittle");

        table.put("utf-16be", "UnicodeBig");
        table.put("utf-16le", "UnicodeLittle");

        // Macintosh encodings
        table.put("macarabic", "MacArabic");
        table.put("maccentraleurope", "MacCentralEurope");
        table.put("maccroatian", "MacCroatian");
        table.put("maccyrillic", "MacCyrillic");
        table.put("macdingbat", "MacDingbat");
        table.put("macgreek", "MacGreek");
        table.put("machebrew", "MacHebrew");
        table.put("maciceland", "MacIceland");
        table.put("macroman", "MacRoman");
        table.put("macromania", "MacRomania");
        table.put("macsymbol", "MacSymbol");
        table.put("macthai", "MacThai");
        table.put("macturkish", "MacTurkish");
        table.put("macukraine", "MacUkraine");

        return table;
    }

    /**
     * Map from JAVA to IANA charset.
     */
    private static Hashtable mapJavaEncoding()
    {
        Hashtable table = new Hashtable();

        table.put("ASCII", "US-ASCII");
        table.put("Cp949", "KSC_5601");
        table.put("MS949", "Windows-949");
        table.put("ISO2022KR", "ISO-2022-KR");
        table.put("EUC_KR", "EUC-KR");
        table.put("EUC_CN", "EUC-CN");
        table.put("EUC_TW", "EUC-TW");
        table.put("ISO2022JP", "ISO-2022-JP");
        table.put("ISO2022CN", "ISO-2022-CN");
        table.put("JIS0208", "x0208");
        table.put("ISO8859_1", "ISO-8859-1");
        table.put("ISO8859_2", "ISO-8859-2");
        table.put("ISO8859_3", "ISO-8859-3");
        table.put("ISO8859_4", "ISO-8859-4");
        table.put("ISO8859_5", "ISO-8859-5");
        table.put("ISO8859_6", "ISO-8859-6");
        table.put("ISO8859_7", "ISO-8859-7");
        table.put("ISO8859_8", "ISO-8859-8");
        table.put("ISO8859_9", "ISO-8859-9");
        table.put("ISO8859_15_FDIS", "ISO-8859-15");
        table.put("JIS0212", "x0212");
        table.put("JIS0201", "x0201");
        table.put("Cp037", "CP037");
        table.put("Cp273", "CP273");
        table.put("Cp277", "IBM277");
        table.put("Cp278", "CP278");
        table.put("Cp280", "CP280");
        table.put("Cp284", "CP284");
        table.put("Cp285", "CP285");
        table.put("Cp297", "CP297");
        table.put("Cp420", "CP420");
        table.put("Cp424", "CP424");
        table.put("Cp437", "CP437");
        table.put("Cp500", "CP500");
        table.put("Cp775", "CP775");
        table.put("Cp850", "CP850");
        table.put("Cp852", "CP852");
        table.put("Cp855", "CP855");
        table.put("Cp857", "CP857");
        table.put("Cp860", "CP860");
        table.put("Cp861", "CP861");
        table.put("Cp862", "CP862");
        table.put("Cp863", "CP863");
        table.put("Cp864", "CP864");
        table.put("Cp865", "CP865");
        table.put("Cp866", "CP866");
        table.put("Cp868", "CP868");
        table.put("Cp869", "CP869");
        table.put("Cp870", "CP870");
        table.put("Cp871", "CP871");
        table.put("Cp918", "CP918");
        table.put("Cp1026", "CP1026");
        table.put("KOI8_R", "KOI8-R");
        table.put("UTF8", "UTF-8");
        table.put("MS932", "Shift_JIS");
        table.put("SJIS", "Shift_JIS");
        table.put("EUC_JP", "EUC-JP");
        table.put("Cp874", "IBM-Thai");
        table.put("MS874", "Windows-874");
        table.put("MS936", "Windows-936");
        table.put("MS950", "Windows-950");
        table.put("GBK", "GB2312");
        table.put("Big5", "Big5");
        table.put("Johab", "Johab");
        table.put("Cp1250", "Windows-1250");
        table.put("Cp1251", "Windows-1251");
        table.put("Cp1252", "Windows-1252");
        table.put("Cp1253", "Windows-1253");
        table.put("Cp1254", "Windows-1254");
        table.put("Cp1255", "Windows-1255");
        table.put("Cp1256", "Windows-1256");
        table.put("Cp1257", "Windows-1257");
        table.put("Cp1258", "Windows-1258");
        table.put("TIS620", "TIS-620");
        table.put("UnicodeBig", "UTF-16BE");
        table.put("UnicodeLittle", "UTF-16LE");
        table.put("MacArabic", "MacArabic");
        table.put("MacCentralEurope", "MacCentralEurope");
        table.put("MacCroatian", "MacCroatian");
        table.put("MacCyrillic", "MacCyrillic");
        table.put("MacDingbat", "MacDingbat");
        table.put("MacGreek", "MacGreek");
        table.put("MacHebrew", "MacHebrew");
        table.put("MacIceland", "MacIceland");
        table.put("MacRoman", "MacRoman");
        table.put("MacRomania", "MacRomania");
        table.put("MacSymbol", "MacSymbol");
        table.put("MacThai", "MacThai");
        table.put("MacTurkish", "MacTurkish");
        table.put("MacUkraine", "MacUkraine");

        return table;
    }
}
