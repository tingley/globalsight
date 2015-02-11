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

import java.util.HashMap;
import java.util.Map;

public class InternalTexts
{
    private String segment;
    private HashMap<String, String> internalTexts = new HashMap<String, String>();
    // key - original internal tag string; value - wrapped internal tag string
    private Map<String, String> wrappedInternalTexts = new HashMap<String, String>();

    public HashMap<String, String> getInternalTexts()
    {
        return internalTexts;
    }

    public void setInternalTexts(HashMap<String, String> internalTexts)
    {
        this.internalTexts = internalTexts;
    }

    public String getSegment()
    {
        return segment;
    }

    public void setSegment(String segment)
    {
        this.segment = segment;
    }

    public void addInternalTags(String tag, String segment)
    {
        internalTexts.put(tag, segment);
    }

    public void addWrappedInternalTags(String oriTag, String wrappedTag)
    {
        wrappedInternalTexts.put(oriTag, wrappedTag);
    }

    public Map<String, String> getWrappedInternalTexts()
    {
        return wrappedInternalTexts;
    }
}
