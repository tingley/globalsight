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
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;

/**
 * Utility class used in Extractor
 */
class TranslatableWriter extends OutputWriter
{
    private StringBuffer outputBuffer = new StringBuffer();
    private Output output = null;
    private boolean isOfficeXml = false;
    private boolean isPhConsolidate = false;

    public TranslatableWriter(Output output)
    {
        this.output = output;

        try
        {
            isOfficeXml = ExtractorRegistry.FORMAT_OFFICE_XML
                    .equals(this.output.getDataFormat());
        }
        catch (Exception e)
        {
            // ignore
        }
    }

    public void flush()
    {
        if (outputBuffer.length() > 0)
        {
            String toAdd = outputBuffer.toString();

            if (Text.isBlank(toAdd)
                    || (getXmlFilterHelper() != null && getXmlFilterHelper()
                            .isBlankOrExblank(toAdd)))
            {
                output.addSkeleton(toAdd);
            }
            else if (isXmlCommentsOnly(toAdd))
            {
                output.addSkeletonTmx(removePhs(toAdd));
            }
            else if (isTmxTagsOnly(toAdd))
            {
                output.addSkeletonTmx(removeTags(toAdd));
            }
            else
            {
                // For prefix and suffix spaces, output them to skeleton so that
                // there will be no line break or space in translatable segment.
                String preBlank = getPrefixBlank(toAdd);
                String sufBlank = getSuffixBlank(toAdd);
                if (preBlank != null && preBlank.length() > 0)
                {
                    output.addSkeleton(preBlank);
                }

                output.addTranslatableTmx(toAdd.trim(), getSid(),
                        isPreserveWhiteSpace(), output.getDataFormat());

                if (sufBlank != null && sufBlank.length() > 0)
                {
                    output.addSkeleton(sufBlank);
                }
            }
        }
    }

    private String removeTags(String content)
    {
        StringBuffer sb = new StringBuffer(content);
        int st = sb.indexOf("<");
        int ed = sb.indexOf(">", st);

        while (st > -1 && ed > st)
        {
            String tag = sb.toString().substring(st, ed + 1);
            if (tag.contains(OfficeXmlContentPostFilter.IS_FROM_OFFICE_CONTENT))
            {
                sb.insert(
                        ed + 1,
                        OfficeXmlContentPostFilter.SKELETON_OFFICE_CONTENT_START);
                sb.delete(st, ed + 1);
                st = sb.indexOf("<");
                ed = sb.indexOf(">", st);
                sb.insert(st,
                        OfficeXmlContentPostFilter.SKELETON_OFFICE_CONTENT_END);
                st += OfficeXmlContentPostFilter.SKELETON_OFFICE_CONTENT_END
                        .length();
                ed += OfficeXmlContentPostFilter.SKELETON_OFFICE_CONTENT_END
                        .length();

                continue;
            }
            sb.delete(st, ed + 1);

            st = sb.indexOf("<");
            ed = sb.indexOf(">", st);
        }

        return sb.toString();
    }

    public void append(String content)
    {
        boolean handled = false;

        if (!handled)
        {
            outputBuffer.append(content);
        }
    }

    public int getOutputType()
    {
        return TRANSLATABLE;
    }
}
