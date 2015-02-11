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

import com.globalsight.ling.aligner.AlignmentResult;
import com.globalsight.ling.aligner.AlignedSegments;
import com.globalsight.ling.tm2.BaseTmTuv;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/**
 * GamWriter writes GlobalSight Alignment Mapping (GAM) file.
 */

public class GamWriter
    implements GamConstants
{
    private PrintWriter m_output;
    

    public GamWriter(PrintWriter p_output)
    {
        m_output = p_output;
    }
    

    public void write(AlignmentResult p_alignmentResult)
        throws Exception
    {
        writeHeader(p_alignmentResult.getSourceTmx(),
            p_alignmentResult.getTargetTmx());

        Iterator it = p_alignmentResult.getAlignedSegments().iterator();
        while(it.hasNext())
        {
            AlignedSegments alignedSegment = (AlignedSegments)it.next();
            
            writeAlign(alignedSegment.getSourceSegments(),
                alignedSegment.getTargetSegments());
        }

        // isolate stuff is gone.
//         it = p_alignmentResult.getSourceIsolatedSegments().iterator();
//         while(it.hasNext())
//         {
//             BaseTmTuv tuv = (BaseTmTuv)it.next();
//             writeIsolate(tuv, true);
//         }
        
//         it = p_alignmentResult.getTargetIsolatedSegments().iterator();
//         while(it.hasNext())
//         {
//             BaseTmTuv tuv = (BaseTmTuv)it.next();
//             writeIsolate(tuv, false);
//         }

        writeTrailer();
    }
    
    private void writeHeader(String p_sourceTmxName, String p_targetTmxName)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        // DOCTYPE for DTD validation
        sb.append("<!DOCTYPE ").append(ELEM_GAM)
            .append(" SYSTEM \"gam.dtd\">\n");

        // gam root element
        sb.append("<").append(ELEM_GAM).append(" ");
        sb.append(ATT_VERSION).append("=\"1.0\" ");
        sb.append(ATT_SOURCE_TMX).append("=\"")
            .append(p_sourceTmxName).append("\" ");
        sb.append(ATT_TARGET_TMX).append("=\"")
            .append(p_targetTmxName).append("\"");
        sb.append(">");

        m_output.println(sb.toString());

        checkError();
    }
    

    private void writeTrailer()
        throws IOException
    {
        m_output.println("</" + ELEM_GAM + ">");
        checkError();
    }


    private void writeAlign(List p_sourceTuvs, List p_targetTuvs)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();
        sb.append("  <").append(ELEM_ALIGN).append(" ");
        sb.append(ATT_SOURCE).append("=\"")
            .append(getIdsString(p_sourceTuvs)).append("\" ");
        sb.append(ATT_TARGET).append("=\"")
            .append(getIdsString(p_targetTuvs)).append("\" ");
        sb.append(ATT_APPROVED).append("=\"").append(VALUE_N).append("\"");
        sb.append("/>");

        m_output.println(sb.toString());

        checkError();
    }
        
    /*        
    private void writeIsolate(BaseTmTuv p_segment, boolean p_source)
        throws IOException
    {
        m_output.println("  <isolate id=\"" + p_segment.getId()
            + "\" locale=\"" + (p_source ? "S" : "T")
            + "\" approved=\"N\" />");
        checkError();
    }
        
    */

    private String getIdsString(List p_tuvs)
    {
        StringBuffer sb = new StringBuffer();
        
        Iterator it = p_tuvs.iterator();
        while(it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv)it.next();
            sb.append(tuv.getId());
            
            if(it.hasNext())
            {
                sb.append(",");
            }
        }
        
        return sb.toString();
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
