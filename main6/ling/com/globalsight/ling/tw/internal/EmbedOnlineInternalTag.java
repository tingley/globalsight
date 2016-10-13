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

package com.globalsight.ling.tw.internal;

import com.globalsight.ling.common.DiplomatBasicParserException;

public class EmbedOnlineInternalTag implements InternalTag
{
    @Override
    public String getInternalTag(String internalText, String allText,
            InternalTexts texts)
    {
        InternalTextUtil util = new InternalTextUtil(new EmbedOnlineInternalTag());
        try
        {
            String segment;
            while (true)
            {
                InternalTexts result = util.preProcessInternalText(internalText);
                segment = result.getSegment();
                if (segment.equals(internalText))
                {
                    break;
                }
                
                internalText = segment;
            }
            
        }
        catch (DiplomatBasicParserException e)
        {
            e.printStackTrace();
        }
        
        return internalText;
    }
}
