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

package com.globalsight.cxe.entity.filterconfiguration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;

public class InternalItem
{
    static private final Logger s_logger = Logger
            .getLogger(InternalItem.class);

    private String content;
    private boolean isRegex = true;
    private boolean isSelected = false;

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public boolean getIsRegex()
    {
        return isRegex;
    }

    public void setIsRegex(boolean isRegex)
    {
        this.isRegex = isRegex;
    }

    public boolean getIsSelected()
    {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
}
