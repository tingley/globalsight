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

import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;
import com.globalsight.log.GlobalSightCategory;

public class InternalItem
{
    static private final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
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

    public String handleString(String s, List<Integer> index)
    {
        if (!isSelected)
            return s;
        
        int n = index.get(0);
        if (!isRegex)
        {
            JPTmxEncoder tmx = new JPTmxEncoder();
            String internalText = tmx.encode(content);
            int i = s.indexOf(internalText);
            if (i > -1)
            {
                String first = s.substring(0, i);
                String end = s.substring(i + internalText.length());
                index.remove(0);
                index.add(n + 1);
                s = handleString(first, index) + "<bpt internal=\"yes\" i=\""
                        + n + "\"></bpt>" + internalText + "<ept i=\"" + n
                        + "\"></ept>" + handleString(end, index);
            }
        }
        else
        {
            try
            {
                Pattern p = Pattern.compile(content);
                Matcher m = p.matcher(s);
                if (m.find())
                {
                    String token = m.group();
                    int i = s.indexOf(token);
                    String first = s.substring(0, i);
                    String end = s.substring(i + token.length());
                    index.remove(0);
                    index.add(n + 1);
                    s = handleString(first, index)
                            + "<bpt internal=\"yes\" i=\"" + n + "\"></bpt>"
                            + token + "<ept i=\"" + n + "\"></ept>"
                            + handleString(end, index);
                }
            }
            catch (Exception e)
            {
                s_logger.error(e);
            }
        }

        return s;
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
