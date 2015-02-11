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
package com.globalsight.ling.jtidy;

import java.util.HashMap;
import java.util.Map;


/**
 * Maps between Java and IANA character encoding names. Also handles encoding alias used in tidy c.
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 * @see http://www.iana.org/assignments/character-sets
 */
public abstract class EncodingNameMapper
{

    /**
     * Map containing uppercase alias - {standard iana, standard java}.
     */
    private static Map encodingNameMap = new HashMap();

    static
    {
        encodingNameMap.put("ISO-8859-1", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("ISO8859_1", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("ISO-IR-100", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("LATIN1", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("CSISOLATIN1", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("L1", new String[]{"ISO-8859-1", "ISO8859_1"});
        encodingNameMap.put("819", new String[]{"ISO-8859-1", "ISO8859_1"});

        encodingNameMap.put("US-ASCII", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("ASCII", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("ISO-IR-6", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("CSASCII", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("ISO646-US", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("US", new String[]{"US-ASCII", "ASCII"});
        encodingNameMap.put("367", new String[]{"US-ASCII", "ASCII"});

        encodingNameMap.put("UTF-8", new String[]{"UTF-8", "UTF8"});
        encodingNameMap.put("UTF8", new String[]{"UTF-8", "UTF8"});
        encodingNameMap.put("UTF-16", new String[]{"UTF-16", "Unicode"});
        encodingNameMap.put("UNICODE", new String[]{"UTF-16", "Unicode"});
        encodingNameMap.put("UTF16", new String[]{"UTF-16", "Unicode"});
        encodingNameMap.put("UTF16", new String[]{"UTF-16", "Unicode"}); // tidy

        encodingNameMap.put("UTF-16BE", new String[]{"UTF-16BE", "UnicodeBig"});
        encodingNameMap.put("UNICODEBIG", new String[]{"UTF-16BE", "UnicodeBig"});
        encodingNameMap.put("UTF16-BE", new String[]{"UTF-16BE", "UnicodeBig"});
        encodingNameMap.put("UTF-16LE", new String[]{"UTF-16LE", "UnicodeLittle"});
        encodingNameMap.put("UNICODELITTLE", new String[]{"UTF-16LE", "UnicodeLittle"});
        encodingNameMap.put("UTF16-LE", new String[]{"UTF-16LE", "UnicodeLittle"});
        encodingNameMap.put("UTF16BE", new String[]{"UTF-16BE", "UnicodeBig"}); // tidy
        encodingNameMap.put("UTF16LE", new String[]{"UTF-16LE", "UnicodeLittle"}); // tidy

        encodingNameMap.put("BIG5", new String[]{"BIG5", "Big5"});
        encodingNameMap.put("CSBIG5", new String[]{"BIG5", "Big5"});

        encodingNameMap.put("SJIS", new String[]{"SHIFT_JIS", "SJIS"});
        encodingNameMap.put("SHIFT_JIS", new String[]{"SHIFT_JIS", "SJIS"});
        encodingNameMap.put("CSSHIFTJIS", new String[]{"CSSHIFTJIS", "SJIS"});
        encodingNameMap.put("MS_KANJI", new String[]{"MS_KANJI", "SJIS"});
        encodingNameMap.put("SHIFTJIS", new String[]{"SHIFT_JIS", "SJIS"}); // tidy

        encodingNameMap.put("JIS", new String[]{"ISO-2022-JP", "JIS"});
        encodingNameMap.put("ISO-2022-JP", new String[]{"ISO-2022-JP", "JIS"});
        encodingNameMap.put("CSISO2022JP", new String[]{"CSISO2022JP", "JIS"});
        encodingNameMap.put("ISO2022", new String[]{"ISO-2022-JP", "JIS"}); // tidy

        encodingNameMap.put("ISO2022KR", new String[]{"ISO-2022-KR", "ISO2022KR"});
        encodingNameMap.put("ISO-2022-KR", new String[]{"ISO-2022-KR", "ISO2022KR"});
        encodingNameMap.put("CSISO2022KR", new String[]{"CSISO2022KR", "ISO2022KR"});
        encodingNameMap.put("ISO-2022-CN", new String[]{"ISO-2022-CN", "ISO2022CN"});
        encodingNameMap.put("ISO2022CN", new String[]{"ISO-2022-CN", "ISO2022CN"});

        encodingNameMap.put("MACROMAN", new String[]{"macintosh", "MacRoman"}); // tidy
        encodingNameMap.put("MACINTOSH", new String[]{"macintosh", "MacRoman"});
        encodingNameMap.put("MACINTOSH ROMAN", new String[]{"macintosh", "MacRoman"});

        encodingNameMap.put("37", new String[]{"IBM037", "CP037"});
        encodingNameMap.put("273", new String[]{"IBM273", "CP273"});
        encodingNameMap.put("277", new String[]{"IBM277", "CP277"});
        encodingNameMap.put("278", new String[]{"IBM278", "CP278"});
        encodingNameMap.put("280", new String[]{"IBM280", "CP280"});
        encodingNameMap.put("284", new String[]{"IBM284", "CP284"});
        encodingNameMap.put("285", new String[]{"IBM285", "CP285"});
        encodingNameMap.put("290", new String[]{"IBM290", "CP290"});
        encodingNameMap.put("297", new String[]{"IBM297", "CP297"});
        encodingNameMap.put("420", new String[]{"IBM420", "CP420"});
        encodingNameMap.put("424", new String[]{"IBM424", "CP424"});
        encodingNameMap.put("437", new String[]{"IBM437", "CP437"});
        encodingNameMap.put("500", new String[]{"IBM500", "CP500"});
        encodingNameMap.put("775", new String[]{"IBM775", "CP775"});
        encodingNameMap.put("850", new String[]{"IBM850", "CP850"});
        encodingNameMap.put("852", new String[]{"IBM852", "CP852"});
        encodingNameMap.put("CSPCP852", new String[]{"IBM852", "CP852"});
        encodingNameMap.put("855", new String[]{"IBM855", "CP855"});
        encodingNameMap.put("857", new String[]{"IBM857", "CP857"});
        encodingNameMap.put("858", new String[]{"IBM00858", "Cp858"});
        encodingNameMap.put("0858", new String[]{"IBM00858", "Cp858"});
        encodingNameMap.put("860", new String[]{"IBM860", "CP860"});
        encodingNameMap.put("861", new String[]{"IBM861", "CP861"});
        encodingNameMap.put("IS", new String[]{"IBM861", "CP861"});
        encodingNameMap.put("862", new String[]{"IBM862", "CP862"});
        encodingNameMap.put("863", new String[]{"IBM863", "CP863"});
        encodingNameMap.put("864", new String[]{"IBM864", "CP864"});
        encodingNameMap.put("865", new String[]{"IBM865", "CP865"});
        encodingNameMap.put("866", new String[]{"IBM866", "CP866"});
        encodingNameMap.put("868", new String[]{"IBM868", "CP868"});
        encodingNameMap.put("AR", new String[]{"IBM868", "CP868"});
        encodingNameMap.put("869", new String[]{"IBM869", "CP869"});
        encodingNameMap.put("GR", new String[]{"IBM869", "CP869"});
        encodingNameMap.put("870", new String[]{"IBM870", "CP870"});
        encodingNameMap.put("871", new String[]{"IBM871", "CP871"});
        encodingNameMap.put("EBCDIC-CP-IS", new String[]{"IBM871", "CP871"});
        encodingNameMap.put("918", new String[]{"CP918", "CP918"});
        encodingNameMap.put("924", new String[]{"IBM00924", "CP924"});
        encodingNameMap.put("0924", new String[]{"IBM00924", "CP924"});
        encodingNameMap.put("1026", new String[]{"IBM1026", "CP1026"});
        encodingNameMap.put("1047", new String[]{"IBM1047", "Cp1047"});
        encodingNameMap.put("1140", new String[]{"IBM01140", "Cp1140"});
        encodingNameMap.put("1141", new String[]{"IBM01141", "Cp1141"});
        encodingNameMap.put("1142", new String[]{"IBM01142", "Cp1142"});
        encodingNameMap.put("1143", new String[]{"IBM01143", "Cp1143"});
        encodingNameMap.put("1144", new String[]{"IBM01144", "Cp1144"});
        encodingNameMap.put("1145", new String[]{"IBM01145", "Cp1145"});
        encodingNameMap.put("1146", new String[]{"IBM01146", "Cp1146"});
        encodingNameMap.put("1147", new String[]{"IBM01147", "Cp1147"});
        encodingNameMap.put("1148", new String[]{"IBM01148", "Cp1148"});
        encodingNameMap.put("1149", new String[]{"IBM01149", "Cp1149"});
        encodingNameMap.put("1250", new String[]{"WINDOWS-1250", "Cp1250"});
        encodingNameMap.put("1251", new String[]{"WINDOWS-1251", "Cp1251"});
        encodingNameMap.put("1252", new String[]{"WINDOWS-1252", "Cp1252"});
        encodingNameMap.put("WIN1252", new String[]{"WINDOWS-1252", "Cp1252"}); // tidy
        encodingNameMap.put("1253", new String[]{"WINDOWS-1253", "Cp1253"});
        encodingNameMap.put("1254", new String[]{"WINDOWS-1254", "Cp1254"});
        encodingNameMap.put("1255", new String[]{"WINDOWS-1255", "Cp1255"});
        encodingNameMap.put("1256", new String[]{"WINDOWS-1256", "Cp1256"});
        encodingNameMap.put("1257", new String[]{"WINDOWS-1257", "Cp1257"});
        encodingNameMap.put("1258", new String[]{"WINDOWS-1258", "Cp1258"});

        encodingNameMap.put("EUC-JP", new String[]{"EUC-JP", "EUCJIS"});
        encodingNameMap.put("EUCJIS", new String[]{"EUC-JP", "EUCJIS"});
        encodingNameMap.put("EUC-KR", new String[]{"EUC-KR", "KSC5601"});
        encodingNameMap.put("KSC5601", new String[]{"EUC-KR", "KSC5601"});
        encodingNameMap.put("GB2312", new String[]{"GB2312", "GB2312"});
        encodingNameMap.put("CSGB2312", new String[]{"GB2312", "GB2312"});
        encodingNameMap.put("X0201", new String[]{"X0201", "JIS0201"});
        encodingNameMap.put("JIS0201", new String[]{"X0201", "JIS0201"});
        encodingNameMap.put("X0208", new String[]{"X0208", "JIS0208"});
        encodingNameMap.put("JIS0208", new String[]{"X0208", "JIS0208"});
        encodingNameMap.put("ISO-IR-87", new String[]{"ISO-IR-87", "JIS0208"});
        encodingNameMap.put("JIS0208", new String[]{"ISO-IR-87", "JIS0208"});
        encodingNameMap.put("X0212", new String[]{"X0212", "JIS0212"});
        encodingNameMap.put("JIS0212", new String[]{"X0212", "JIS0212"});
        encodingNameMap.put("ISO-IR-159", new String[]{"X0212", "JIS0212"});
        encodingNameMap.put("GB18030", new String[]{"GB18030", "GB18030"});

        encodingNameMap.put("936", new String[]{"GBK", "GBK"});
        encodingNameMap.put("MS936", new String[]{"GBK", "GBK"});

        encodingNameMap.put("MS932", new String[]{"WINDOWS-31J", "MS932"});
        encodingNameMap.put("WINDOWS-31J", new String[]{"WINDOWS-31J", "MS932"});
        encodingNameMap.put("CSWINDOWS31J", new String[]{"WINDOWS-31J", "MS932"});
        encodingNameMap.put("TIS-620", new String[]{"TIS-620", "TIS620"});
        encodingNameMap.put("TIS620", new String[]{"TIS-620", "TIS620"});

        encodingNameMap.put("ISO-8859-2", new String[]{"ISO-8859-2", "ISO8859_2"});
        encodingNameMap.put("ISO8859_2", new String[]{"ISO-8859-2", "ISO8859_2"});
        encodingNameMap.put("ISO-IR-101", new String[]{"ISO-8859-2", "ISO8859_2"});
        encodingNameMap.put("LATIN2", new String[]{"ISO-8859-2", "ISO8859_2"});
        encodingNameMap.put("L2", new String[]{"ISO-8859-2", "ISO8859_2"});

        encodingNameMap.put("ISO-8859-3", new String[]{"ISO-8859-3", "ISO8859_3"});
        encodingNameMap.put("ISO8859_3", new String[]{"ISO-8859-3", "ISO8859_3"});
        encodingNameMap.put("ISO-IR-109", new String[]{"ISO-8859-3", "ISO8859_3"});
        encodingNameMap.put("LATIN3", new String[]{"ISO-8859-3", "ISO8859_3"});
        encodingNameMap.put("L3", new String[]{"ISO-8859-3", "ISO8859_3"});

        encodingNameMap.put("ISO-8859-4", new String[]{"ISO-8859-4", "ISO8859_4"});
        encodingNameMap.put("ISO8859_4", new String[]{"ISO-8859-4", "ISO8859_4"});
        encodingNameMap.put("ISO-IR-110", new String[]{"ISO-8859-4", "ISO8859_4"});
        encodingNameMap.put("ISO-IR-110", new String[]{"ISO-8859-4", "ISO8859_4"});
        encodingNameMap.put("L4", new String[]{"ISO-8859-4", "ISO8859_4"});

        encodingNameMap.put("ISO-8859-5", new String[]{"ISO-8859-5", "ISO8859_5"});
        encodingNameMap.put("ISO8859_5", new String[]{"ISO-8859-5", "ISO8859_5"});
        encodingNameMap.put("ISO-IR-144", new String[]{"ISO-8859-5", "ISO8859_5"});
        encodingNameMap.put("CYRILLIC", new String[]{"ISO-8859-5", "ISO8859_5"});

        encodingNameMap.put("ISO-8859-6", new String[]{"ISO-8859-6", "ISO8859_6"});
        encodingNameMap.put("ISO8859_6", new String[]{"ISO-8859-6", "ISO8859_6"});
        encodingNameMap.put("ISO-IR-127", new String[]{"ISO-8859-6", "ISO8859_6"});
        encodingNameMap.put("ARABIC", new String[]{"ISO-8859-6", "ISO8859_6"});

        encodingNameMap.put("ISO-8859-7", new String[]{"ISO-8859-7", "ISO8859_7"});
        encodingNameMap.put("ISO8859_7", new String[]{"ISO-8859-7", "ISO8859_7"});
        encodingNameMap.put("ISO-IR-126", new String[]{"ISO-8859-7", "ISO8859_7"});
        encodingNameMap.put("GREEK", new String[]{"ISO-8859-7", "ISO8859_7"});

        encodingNameMap.put("ISO-8859-8", new String[]{"ISO-8859-8", "ISO8859_8"});
        encodingNameMap.put("ISO8859_8", new String[]{"ISO-8859-8", "ISO8859_8"});
        encodingNameMap.put("ISO-8859-8-I", new String[]{"ISO-8859-8", "ISO8859_8"});
        encodingNameMap.put("ISO-IR-138", new String[]{"ISO-8859-8", "ISO8859_8"});
        encodingNameMap.put("HEBREW", new String[]{"ISO-8859-8", "ISO8859_8"});

        encodingNameMap.put("ISO-8859-9", new String[]{"ISO-8859-9", "ISO8859_8"});
        encodingNameMap.put("ISO8859_8", new String[]{"ISO-8859-9", "ISO8859_8"});
        encodingNameMap.put("CSISOLATINHEBREW", new String[]{"ISO-8859-9", "ISO8859_9"});
        encodingNameMap.put("ISO-IR-148", new String[]{"ISO-8859-9", "ISO8859_9"});
        encodingNameMap.put("LATIN5", new String[]{"ISO-8859-9", "ISO8859_9"});
        encodingNameMap.put("CSISOLATIN5", new String[]{"ISO-8859-9", "ISO8859_9"});
        encodingNameMap.put("L5", new String[]{"ISO-8859-9", "ISO8859_9"});

        encodingNameMap.put("ISO-8859-15", new String[]{"ISO-8859-15", "ISO8859_15"});
        encodingNameMap.put("ISO8859_15", new String[]{"ISO-8859-15", "ISO8859_15"});

        encodingNameMap.put("KOI8-R", new String[]{"KOI8-R", "KOI8_R"});
        encodingNameMap.put("KOI8_R", new String[]{"CSKOI8R", "KOI8_R"});
        encodingNameMap.put("CSKOI8R", new String[]{"CSKOI8R", "KOI8_R"});
    }

    /**
     * Convert a Java character encoding name to its IANA equivalent.
     * @param encoding java encoding name or alias
     * @return iana equivalent or null if no match is found.
     */
    public static String toIana(String encoding)
    {
        if (encoding == null)
        {
            return null;
        }

        String[] values = (String[]) encodingNameMap.get(handlecommonAlias(encoding));
        if (values != null)
        {
            return values[0];
        }

        return null;
    }

    /**
     * "Fix" the name for common alias to reduce the number of entries needed in the hashmap. It actually removes CSIBM,
     * CCSID, IBM-, IBM0, CP-0, IBM, CP0, CP-, CP, WINDOWS- prefixes from given name.
     * @param encoding encoding name
     * @return "fixed" encoding.
     */
    private static String handlecommonAlias(String encoding)
    {
        String key = encoding.toUpperCase();

        // handle common alias
        if (key.startsWith("CSIBM") || key.startsWith("CCSID"))
        {
            key = key.substring(5);
        }
        else if (key.startsWith("IBM-") || key.startsWith("IBM0") || key.startsWith("CP-0"))
        {
            key = key.substring(4);
        }
        else if (key.startsWith("IBM") || key.startsWith("CP0") || key.startsWith("CP-"))
        {
            key = key.substring(3);
        }
        else if (key.startsWith("CP"))
        {
            key = key.substring(2);
        }
        else if (key.startsWith("WINDOWS-"))
        {
            key = key.substring(8);
        }
        else if (key.startsWith("ISO_"))
        {
            key = "ISO-" + key.substring(4);
        }

        return key;
    }

    /**
     * Converts an encoding name to the standard java name. Handles IANA names, legacy names used in tidy and different
     * java encoding alias. See http://www.iana.org/assignments/character-sets.
     * @param encoding IANA encoding name or alias
     * @return java equivalent or null if no match is found.
     */
    public static String toJava(String encoding)
    {
        if (encoding == null)
        {
            return null;
        }

        String[] values = (String[]) encodingNameMap.get(handlecommonAlias(encoding));
        if (values != null)
        {
            return values[1];
        }

        return null;
    }
}
