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
package com.globalsight.ling.aligner.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.globalsight.BuildVersion;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.UTC;


/**
 * TmxWriter writes TMX file
 */

public class TmxWriter
{
    private PrintWriter m_output;
    

    public TmxWriter(PrintWriter p_output)
    {
        m_output = p_output;
    }


    /**
     * Write TMX file.
     *
     * @param p_tus List of BaseTmTu objects
     * @param p_sourceLocale Source Locale
     * @param p_fileType Original file type (html, xml, doc, etc)
     */
    public void write(List p_tus,
        GlobalSightLocale p_sourceLocale, String p_fileType)
        throws Exception
    {
        writeHeader(p_sourceLocale, p_fileType);
        
        Iterator it = p_tus.iterator();
        while(it.hasNext())
        {
            BaseTmTu tu = (BaseTmTu)it.next();

            writeTuHeader(tu);
            
            Iterator itLocales = tu.getAllTuvLocales().iterator();
            while(itLocales.hasNext())
            {
                GlobalSightLocale locale = (GlobalSightLocale)itLocales.next();
                
                Iterator itTuvs = tu.getTuvList(locale).iterator();
                while(itTuvs.hasNext())
                {
                    BaseTmTuv tuv = (BaseTmTuv)itTuvs.next();
                    
                    writeTuv(tuv);
                }
            }
            
            writeTuTrailer();
        }
        
        writeTrailer();
    }
            

    public void writeHeader(
        GlobalSightLocale p_sourceLocale, String p_fileType)
        throws IOException
    {
        m_output.println("<!DOCTYPE tmx SYSTEM \"tmx-gs.dtd\">");
        m_output.println("<tmx version=\"1.0 GS\">");
        m_output.println("  <header creationtool=\"GlobalSight "
            + BuildVersion.PRODUCT + "\" creationtoolversion=\""
            + BuildVersion.VERSION + "\" segtype=\"sentence\" "
            + "o-tmf=\"gxml\" adminlang=\"en_US\" srclang=\""
            + p_sourceLocale.toString() + "\" datatype=\""
            + p_fileType + "\" creationdate=\""
            + UTC.valueOfNoSeparators(new Date()) + "\" />");
        m_output.println("  <body>");

        checkError();
    }
    

    public void writeTrailer()
        throws IOException
    {
        m_output.println("  </body>");
        m_output.println("</tmx>");

        checkError();
    }


    public void writeTuHeader(BaseTmTu p_tu)
        throws IOException
    {
        m_output.println("    <tu tuid=\"" + p_tu.getId()
            + "\" datatype=\"" + p_tu.getFormat() + "\">");
        m_output.println("      <prop type=\"" + Tmx.PROP_SEGMENTTYPE
            + "\">" + p_tu.getType() + "</prop>");

        checkError();
    }
    

    public void writeTuTrailer()
        throws IOException
    {
        m_output.println("    </tu>");

        checkError();
    }
    

    public void writeTuv(BaseTmTuv p_tuv)
        throws Exception
    {
        m_output.println("      <tuv xml:lang=\""
            + p_tuv.getLocale().toString() + "\">");
        m_output.println(
            "        <seg>" + p_tuv.getSegmentNoTopTag() + "</seg>");
        m_output.println("      </tuv>");
        
        checkError();
    }


    private void checkError()
        throws IOException
    {
        if (m_output.checkError())
        {
            throw new IOException("PrintWriter write error");
        }
    }


}
