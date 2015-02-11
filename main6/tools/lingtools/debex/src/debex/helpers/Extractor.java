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
package debex.helpers;

import debex.data.ExtractorSettings;

import com.globalsight.ling.docproc.DiplomatAPI;

import java.io.*;
import java.util.regex.*;

public class Extractor
{
    //
    // Members
    //
    private DiplomatAPI m_api = new DiplomatAPI();
    private ExtractorSettings m_settings = null;
    private String m_result = null;

    //
    // Constructor
    //
    public Extractor()
    {
    }

    public void setSettings(ExtractorSettings p_settings)
        throws Exception
    {
        m_settings = p_settings;
        m_result = null;
        m_settings.m_resultFileName = null;

        m_api.reset();

        m_api.setSourceFile(m_settings.m_fileName);
        m_api.setInputFormat(m_settings.m_type);
        m_api.setEncoding(m_settings.m_encoding);
        m_api.setLocale(m_settings.m_locale);
        m_api.setSentenceSegmentation(m_settings.m_sentenceSegmentation);

        String rules = m_settings.m_rulesFileName;
        if (rules != null && rules.length() > 0)
        {
            m_api.setRuleFile(rules);
        }
    }

    public void extract()
        throws Exception
    {
        try
        {
            m_result = m_api.extract();
        }
        // catch (Throwable ex)
        finally
        {
            m_settings.m_resultFileName = null;
        }
    }

    public String saveResult(String p_stylesheet)
        throws IOException
    {
        if (m_result != null)
        {
            File file = File.createTempFile("gxml", ".xml");
            file.deleteOnExit();

            Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            if (p_stylesheet != null)
            {
                Pattern r = Pattern.compile("<\\?xml[^>]*>");
                Matcher m = r.matcher(m_result);
                if (m.find())
                {
                    StringBuffer res = new StringBuffer(
                        m_result.substring(m.start(), m.end()));
                    res.append("\n<?xml-stylesheet type='text/xsl' href='");
                    res.append(p_stylesheet);
                    res.append("'?>\n");
                    res.append(m_result.substring(m.end()));

                    m_result = res.toString();
                }
            }

            writer.write(m_result);
            writer.close();

            m_result = null;
            return file.getAbsolutePath();
        }

        return null;
    }
}
