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
package com.globalsight.ling.docproc.extractor.po;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.ling.docproc.ExtractorException;

/**
 * <p>
 * Parser for PO File.
 * </p>
 */
public class Parser
{

    private BufferedReader m_bufferedReader;

    /**
     * <p>
     * Constructor - initializes the parser with an input stream.
     * </p>
     * 
     * @param: p_InputReader - the parser's input stream.
     */
    public Parser(Reader p_InputReader)
    {
        // super();

        try
        {
            m_bufferedReader = new BufferedReader(p_InputReader);
        }
        catch (Exception e)
        {
        }
    }

    public List<String> getLineList()
    {

        List<String> list = new ArrayList<String>();

        try
        {

            String strLine;
            // Read File Line By Line
            while ((strLine = m_bufferedReader.readLine()) != null)
            {

                list.add(strLine);
            }

            if (m_bufferedReader != null)
                m_bufferedReader.close();

        }
        catch (Exception e)
        {
            // System.err.println("Error: " + e.getMessage());
            throw new ExtractorException(e);
        }

        return list;
    }
}
