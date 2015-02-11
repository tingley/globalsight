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
package com.globalsight.ling.docproc.extractor.xliff;

import java.util.*;
/**
 * <p>Utility class used by the XML Extractor.  Basically it manages
 * index numbers for &lt;bpt&gt;'s <code>i</code> attribute.</p>
 *
 * <p>Index numbers must be incremented monotonically and not reset to
 * 1, or the XML Extractor/Segmenter will produce wrong isolated tags
 * when segment-breaking tags occur inside a tag.</p>
 */
class ExtractorAdmin
{
    private int bptIndex = 1;
    private OutputWriter outputWriter;

    public ExtractorAdmin(OutputWriter outputWriter)
    {
        this.outputWriter = outputWriter;
    }

    public void reset(OutputWriter outputWriter)
    {
        if (this.outputWriter != null)
        {
            this.outputWriter.flush();
        }

        this.outputWriter = outputWriter;
    }
    
    public void reset(OutputWriter outputWriter, boolean blankTextAsSkeleton)
    {
        if (this.outputWriter != null)
        {
            this.outputWriter.flush();
        }

        this.outputWriter = outputWriter;
    	this.outputWriter.setBlankTextAsSkeleton(blankTextAsSkeleton);
    }

    public int getBptIndex()
    {
        return bptIndex;
    }

    public int incrementBptIndex()
    {
        return bptIndex++;
    }
    
    public void setBptIndex(int index)
    {
        bptIndex = index;
    }

    public void addContent(String content)
    {
        outputWriter.append(content);
    }

    public int getOutputType()
    {
        int ret = outputWriter.NO_OUTPUT;
        if (outputWriter != null)
        {
            ret = outputWriter.getOutputType();
        }
        return ret;
    }
    
    public void setSid(String sid)
    {
        outputWriter.setSid(sid);
    }
    
    public void setXliffTransPart(Map Attributes)
    {
        outputWriter.setXliffTransPart(Attributes);
    }
}

