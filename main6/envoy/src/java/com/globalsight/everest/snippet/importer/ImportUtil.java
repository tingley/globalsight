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

package com.globalsight.everest.snippet.importer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.Output;

/**
 * Helper methods for snippet import
 */
public class ImportUtil
{
    private static final Logger CATEGORY = Logger
            .getLogger(ImportUtil.class.getName());

    //
    // Constructors
    //

    /** Static class, private constructor */
    private ImportUtil()
    {
    }

    //
    // Public Methods
    //

    public static boolean isEmptyLine(String p_line)
    {
        return p_line.trim().length() == 0;
    }

    /**
     * Extracts a (snippet) file using the HTML extractor and the given
     * encoding, and returns the intermediary Output structure instead of a Gxml
     * string.
     * 
     * @see com.globalsight.ling.docproc.Output
     */
    public static Output extractFile(String p_url, String p_encoding)
            throws Exception
    {
        DiplomatAPI diplomat = new DiplomatAPI();
        diplomat.setSourceFile(p_url);
        diplomat.setInputFormat(DiplomatAPI.FORMAT_HTML);
        diplomat.setEncoding(p_encoding);
        diplomat.setLocale(Locale.US);

        // Turn off segmentation since snippets get converted back to
        // plain text anyway.
        diplomat.setSentenceSegmentation(false);

        String gxml = diplomat.extract();

        // Also allow the side-product to be garbage-collected.
        gxml = null;

        return diplomat.getOutput();
    }
}
