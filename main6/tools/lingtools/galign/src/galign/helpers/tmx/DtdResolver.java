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

package galign.helpers.tmx;

import galign.helpers.tmx.TmxHeader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * An implementation of EntityResolver that finds various TMX DTDs in
 * the application's /resources directory.
 *
 * (Copied from com.globalsight.everest.tm.util.)
 */
public class DtdResolver
    implements EntityResolver
{
    static private final Pattern DTD_SEARCH_PROGRAM =
        Pattern.compile("tmx\\d+\\.dtd");

    static private final Pattern GS_DTD_SEARCH_PROGRAM =
        Pattern.compile(TmxHeader.TMX_DTD_GS);

    /**
     * A static resolver instance that can be reused by all threads.
     */
    static private DtdResolver s_instance = new DtdResolver();

    //
    // Constructors
    //

    // Singleton class, private constructor.
    private DtdResolver () {}

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

        Matcher matcher = DTD_SEARCH_PROGRAM.matcher(systemId);

        if (matcher.find())
        {
            InputStream stream = DtdResolver.class.getResourceAsStream(
                "/resources/" + matcher.group());

            if (stream != null)
            {
                result = new InputSource(stream);
            }
        }

        if (result == null)
        {
            matcher = GS_DTD_SEARCH_PROGRAM.matcher(systemId);

            if (matcher.find())
            {
                InputStream stream = DtdResolver.class.getResourceAsStream(
                    "/resources/" + matcher.group(0));

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
