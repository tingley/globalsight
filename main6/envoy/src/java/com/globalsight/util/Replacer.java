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
package com.globalsight.util;

import java.util.regex.Matcher;

/**
<pre>
There is a example for how to use this class. 

String s = "this is &lt;b&gt;a&lt;/b&gt; test";
static Pattern TEST_PATTERN = Pattern.compile("&lt;b&gt;(.*?)&lt;/b&gt;");

If you want to change the paired tag &lt;b&gt; to &lt;i&gt;, you can write a method like updateTags:

    private String updateTags(String s) 
    {
       Matcher m = TEST_PATTERN.matcher(s); 
       StringBuilder output = new StringBuilder(); 
       int start = 0; 
        while (m.find()) 
        {
           output.append(s.substring(start, m.start())); 
           output.append("&lt;i&gt;"); 
           output.append(m.group(1)); 
           output.append("&lt;/i&gt;"); 
           start = m.end();
        } 
        output.append(s.substring(start)); 
        return output.toString(); 
    }
    
Or a method like updateTags2:
    private String updateTags2(String s) 
    {
        s = StringUtil.replaceWithRE(s, TEST_PATTERN, new Replacer() 
        {
            //Override
            public String getReplaceString(Matcher m) 
            {
                return "&lt;i&gt;" + m.group(1) + "&lt;/i&gt;";
            }
        });

        return s; 
    }

Replacer can accept up at most 3 parameters, and you can use them as r1, r2, r3. 
So if you want to set the &lt;i&gt; &lt;/i&gt; as argument. you can write a method like updateTags3.

    private String updateTags3(String s, String start, String end) 
    {
        s = StringUtil.replaceWithRE(s, TEST_PATTERN, new Replacer(start, end) 
        {
            //Override
            public String getReplaceString(Matcher m) 
            {
                return r1 + m.group(1) + r2;
            }
        });

        return s; 
    }
    
    You can call these methods as following:
        String s = "this is &lt;b&gt;a&lt;/b&gt; test";
        System.out.println(updateTags(s));
        System.out.println(updateTags2(s));
        System.out.println(updateTags3(s, "&lt;i&gt;", "&lt;/i&gt;"));       
 */
public abstract class Replacer {

    protected String r1;
    protected String r2;
    protected String r3;
    
    public Replacer(String... rs)
    {
        if (rs.length > 0)
            r1 = rs[0];
        
        if (rs.length > 1)
            r2 = rs[1];
        
        if (rs.length > 2)
            r3 = rs[2];
    }
    
    public abstract String getReplaceString(Matcher m);
}
