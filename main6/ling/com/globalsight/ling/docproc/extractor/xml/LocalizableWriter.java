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
package com.globalsight.ling.docproc.extractor.xml;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.Output;

/**
 * Utility class used in the XML Extractor.
 */
class LocalizableWriter
    extends OutputWriter
{
    private StringBuffer outputBuffer = new StringBuffer();
    private Output output = null;

    public LocalizableWriter(Output output)
    {
        this.output = output;
    }

    public void flush()
    {
        if (outputBuffer.length() > 0)
        {
            String toAdd = outputBuffer.toString();
            if (Text.isBlank(toAdd) || 
                    (getXmlFilterHelper() != null && getXmlFilterHelper().isBlankOrExblank(toAdd)))
            {
                output.addSkeleton(toAdd);
            }
            else if(isXmlCommentsOnly(toAdd))
            {
                output.addSkeletonTmx(removePhs(toAdd));
            }
            else
            {
                output.addLocalizableTmx(toAdd);
            }
        }
    }

    public void append(String content)
    {
        outputBuffer.append(content);
    }

    public int getOutputType()
    {
        return LOCALIZABLE;
    }
}


