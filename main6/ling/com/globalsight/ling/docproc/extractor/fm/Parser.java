/**
 *  Copyright 2010 Welocalize, Inc. 
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
package com.globalsight.ling.docproc.extractor.fm;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.ling.docproc.ExtractorException;

public class Parser
{
    private BufferedReader m_bufferedReader;

    public Parser(Reader p_InputReader)
    {
        try
        {
            m_bufferedReader = new BufferedReader(p_InputReader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getLineList()
    {
        List<String> list = new ArrayList<String>();

        try
        {
            String line;
            // Read File Line By Line
            while ((line = m_bufferedReader.readLine()) != null)
            {
                String result = line.trim();
                list.add(result);
            }
            if (m_bufferedReader != null)
            {
                m_bufferedReader.close();
            }
        }
        catch (Exception e)
        {
            throw new ExtractorException(e);
        }
        return list;
    }
}
