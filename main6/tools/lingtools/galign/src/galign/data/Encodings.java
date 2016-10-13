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

package galign.data;

import java.util.Vector;

/**
 * Provides a list of known encodings for handling files, for example
 * exporting TMX files or importing documents for alignment.
 */
public class Encodings
{
    static public String[] s_encodingNames =
        {
        "UTF-8",
        "Windows-1252",
        "UTF-16LE",
        "UTF-16BE",
        "ASCII",
        "Big5",
        "EUC-CN",
        "EUC-JP",
        "EUC-KR",
        "EUC-TW",
        "GB2312",
        "ISO-2022-CN",
        "ISO-2022-JP",
        "ISO-2022-KR",
        "ISO-8859-1",
        "ISO-8859-2",
        "ISO-8859-3",
        "ISO-8859-4",
        "ISO-8859-5",
        "ISO-8859-6",
        "ISO-8859-7",
        "ISO-8859-8",
        "ISO-8859-9",
        "ISO-8859-15",
        "Johab",
        "KOI8-R",
        "Shift_JIS",
        "TIS-620",
        "Windows-874",
        "Windows-932",
        "Windows-936",
        "Windows-949",
        "Windows-950",
        "Windows-1250",
        "Windows-1251",
        "Windows-1253",
        "Windows-1254",
        "Windows-1255",
        "Windows-1256",
        "Windows-1257",
        "Windows-1258",
        };

    public static Vector getEncodingNames()
    {
        Vector result = new Vector();

        for (int i = 0; i < s_encodingNames.length; i++)
        {
            result.add(s_encodingNames[i]);
        }

        return result;
    }
}
