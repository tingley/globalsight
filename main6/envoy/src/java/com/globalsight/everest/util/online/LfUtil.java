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
package com.globalsight.everest.util.online;

import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;

public class LfUtil
{
    public static final String LF = "[LF]";
    public static final String N = "\n";

    /**
     * Replace LF tag to \n.
     * 
     * @param seg
     * @return
     */
    public static String removeLf(String seg)
    {
        return seg.replace(LF, N);
    }

    /**
     * Replace \n to LF tag.
     * 
     * @param seg
     * @return
     */
    public static String addLf(String seg)
    {
        return seg.replace(N, LF);
    }

    /**
     * Replace \n to LF tag.
     * 
     * @param seg
     * @return
     */
    public static String addHtlmLf(String seg)
    {
        return seg.replace(N,
                OnlineTagHelper.PTAG_COLOR_START + LF + OnlineTagHelper.PTAG_COLOR_END);
    }
}
