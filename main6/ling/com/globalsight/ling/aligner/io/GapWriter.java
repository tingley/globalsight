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

import com.globalsight.ling.aligner.AlignmentProject;
import com.globalsight.ling.aligner.AlignmentUnit;
import com.globalsight.util.GlobalSightLocale;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/**
 * GapWriter writes GlobalSight Alignment Package (GAP) file.
 */

public class GapWriter
    implements GapConstants
{
    private PrintWriter m_output;
    

    public GapWriter(PrintWriter p_output)
    {
        m_output = p_output;
    }
    

    public void write(AlignmentProject p_alignmentProject)
        throws Exception
    {
        writeHeader(p_alignmentProject.getSourceLocale(),
            p_alignmentProject.getTargetLocale());

        Iterator it = p_alignmentProject.getAlignmentUnits().iterator();
        while(it.hasNext())
        {
            AlignmentUnit alignmentUnit = (AlignmentUnit)it.next();
            
            if(alignmentUnit.getState().equals(AlignmentUnit.PRE_ALIGNED))
            {
                writeFiles(alignmentUnit);
            }
        }

        writeTrailer();
    }
    
    private void writeHeader(GlobalSightLocale p_sourceLocale,
        GlobalSightLocale p_targetLocale)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        // DOCTYPE for DTD validation
        sb.append("<!DOCTYPE ").append(ELEM_GAP)
            .append(" SYSTEM \"gap.dtd\">\n");

        // gap root element
        sb.append("<").append(ELEM_GAP).append(" ");
        sb.append(ATT_VERSION).append("=\"1.0\" ");
        sb.append(ATT_SOURCE_LOCALE).append("=\"")
            .append(p_sourceLocale.toString()).append("\" ");
        sb.append(ATT_TARGET_LOCALE).append("=\"")
            .append(p_targetLocale.toString()).append("\"");
        sb.append(">");
        
        m_output.println(sb.toString());

        checkError();
    }
    

    private void writeTrailer()
        throws IOException
    {
        m_output.println("</" + ELEM_GAP + ">");
        checkError();
    }


    private void writeFiles(AlignmentUnit p_alignmentUnit)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();
        sb.append("  <").append(ELEM_FILES).append(" ");
        sb.append(ATT_ORIGINAL_SOURCE_FILE).append("=\"")
            .append(p_alignmentUnit.getOriginalSourceFileName()).append("\" ");
        sb.append(ATT_ORIGINAL_TARGET_FILE).append("=\"")
            .append(p_alignmentUnit.getOriginalTargetFileName()).append("\" ");
        sb.append(ATT_SOURCE_CUV_ID).append("=\"")
            .append(p_alignmentUnit.getSourceCorpusDoc().getId())
            .append("\" ");
        sb.append(ATT_TARGET_CUV_ID).append("=\"")
            .append(p_alignmentUnit.getTargetCorpusDoc().getId())
            .append("\" ");
        sb.append(ATT_SOURCE_TMX).append("=\"")
            .append(p_alignmentUnit.getSourceTmxFileName()).append("\" ");
        sb.append(ATT_TARGET_TMX).append("=\"")
            .append(p_alignmentUnit.getTargetTmxFileName()).append("\" ");
        sb.append(ATT_GAM).append("=\"")
            .append(p_alignmentUnit.getGamFileName()).append("\" ");
        sb.append(ATT_STATE).append("=\"")
            .append(p_alignmentUnit.getState()).append("\" ");
        sb.append("/>");
        
        m_output.println(sb.toString());
        
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
