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
package com.globalsight.ling.common;

import java.util.*;

/**
 * Disable <a> tag in HTML file.
 */
public class DisableLink
{
    final static protected ArrayList m_tagList = createTagList();

    public static String doDisable(String p_snippet)
    {
        StringBuffer ret = new StringBuffer(p_snippet);

        for (int i = 0; i < p_snippet.length(); i++)
        {
            if (p_snippet.charAt(i) == '<')
            {
                for (Iterator it = m_tagList.iterator(); it.hasNext(); )
                {
                    String s = (String)it.next();

                    if (p_snippet.regionMatches(true, i + 1, s, 0, s.length())
                       && Character.isWhitespace(
                           p_snippet.charAt(i + 1 + s.length())))
                    {
                        ret.append('<');
                        ret.append(s);
                        ret.append(" onClick=\"return false;\"");

                        i += s.length() + 1;

                        break;
                    }
                }
            }

            ret.append(p_snippet.charAt(i));
        }

        return ret.toString();
    }

    private static ArrayList createTagList()
    {
        ArrayList v = new ArrayList();

        v.add("a");
        v.add("area");
        v.add("input");

        return v;
    }

    public static void main(String[] arg)
    {
        System.out.print(DisableLink.doDisable(arg[0]));
    }
}
