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

package com.globalsight.everest.tm.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * An implementation of EntityResolver that finds the various TMX DTDs.
 */
public class DtdResolver implements EntityResolver
{
    static private final REProgram DTD_SEARCH_PROGRAM = createSearchProgram("tmx\\d+\\.dtd");

    static private final REProgram GS_DTD_SEARCH_PROGRAM = createSearchProgram(Tmx.TMX_DTD_GS);

    private static REProgram createSearchProgram(String p_pattern)
    {
        REProgram result = null;

        try
        {
            RECompiler compiler = new RECompiler();
            result = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }

    /**
     * A static resolver instance that can be reused by all threads.
     */
    static private DtdResolver s_instance = new DtdResolver();

    //
    // Constructors
    //

    // Singleton class, private constructor.
    private DtdResolver()
    {
    }

    //
    // Public Methods
    //

    static public DtdResolver getInstance()
    {
        return s_instance;
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
    {
        InputSource result = null;

        RE matcher = new RE(DTD_SEARCH_PROGRAM, RE.MATCH_SINGLELINE);

        if (matcher.match(systemId))
        {
            InputStream stream = DtdResolver.class
                    .getResourceAsStream("/resources/" + matcher.getParen(0));

            if (stream != null)
            {
                result = new InputSource(stream);
            }
        }

        if (result == null)
        {
            matcher = new RE(GS_DTD_SEARCH_PROGRAM, RE.MATCH_SINGLELINE);

            if (matcher.match(systemId))
            {
                InputStream stream = DtdResolver.class
                        .getResourceAsStream("/resources/"
                                + matcher.getParen(0));

                if (stream != null)
                {
                    result = new InputSource(stream);
                }
            }
        }

        if (result == null)
        {
            result = new InputSource(new ByteArrayInputStream(new byte[0]));
        }

        return result;
    }
}
