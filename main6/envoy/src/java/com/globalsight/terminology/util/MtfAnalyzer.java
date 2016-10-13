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
package com.globalsight.terminology.util;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;

/**
 * Reads MultiTerm MTF files and counts how many entries they contain.
 */
public class MtfAnalyzer
{
    private int m_entryCount;

    //
    // Constructors
    //

    public MtfAnalyzer ()
    {
    }

    public void log(String p_message)
    {
        System.out.println(p_message);
    }

    public void analyze(String p_url)
        throws Exception
    {
        m_entryCount = 0;

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        System.err.println("Analyzing document: " + p_url);

        // enable element complete notifications to conserve memory
        reader.addHandler("/mtf/conceptGrp",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                        ++m_entryCount;

                        if (m_entryCount % 200 == 0)
                        {
                            log("Entry " + m_entryCount);
                        }
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        // prune the current element to reduce memory
                        element.detach();

                        element = null;
                    }
                }
            );

        Document document = reader.read(p_url);

        log("Total entries: " + m_entryCount);

        // all done
    }

    static public void main(String[] argv)
        throws Exception
    {
        MtfAnalyzer a = new MtfAnalyzer();

        a.analyze(argv[0]);
    }
}
