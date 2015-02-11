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

import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.Output;
import java.util.*;

/**
 * Utility class used in Extractor
 */
class TranslatableWriter extends OutputWriter
{
    private StringBuffer outputBuffer = new StringBuffer();
    private Output output = null;
    private Map xilffTransPart = null;

    public TranslatableWriter(Output output)
    {
        this.output = output;
    }

    /**
     * Flush data into output
     * 
     * For xliff source file,target content may be blank.In this case,
     * the blank target content should be added to translatable instead of skeleton.
     */
    public void flush()
    {
        if (outputBuffer.length() > 0)
        {
            String toAdd = outputBuffer.toString();

            if (Text.isBlank(toAdd))
            {
            	if (this.getBlankTextAsSkeleton() == true) {
                    output.addSkeleton(toAdd);            		
            	} else {
            		//Special handling for xliff blank target content
            		output.addTranslatableTmx(toAdd, getSid(), xilffTransPart);
            	}
            }
            else if (isXmlCommentsOnly(toAdd))
            {
                output.addSkeletonTmx(removePhs(toAdd));
            }
            else
            {
                output.addTranslatableTmx(toAdd, getSid(), xilffTransPart);
            }
        }
    }
    
    public void append(String content)
    {
        outputBuffer.append(content);
    }

    public int getOutputType()
    {
        return TRANSLATABLE;
    }
    
    public void setXliffTransPart(Map attributes) {
        xilffTransPart = attributes;
    }
}
