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

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * This class represents an emoji.
 */
public class Emoji
{
    private final String description;
    private final List<String> aliases;
    private final List<String> tags;
    private final byte[] bytes;
    private final String htmlDec;
    private final String htmlHex;

    public Emoji(String description, List<String> aliases, List<String> tags,
            int htmlCode, byte... bytes)
    {
        this.description = description;
        this.aliases = aliases;
        this.tags = tags;
        this.htmlDec = "&#" + htmlCode + ";";
        this.htmlHex = "&#x" + Integer.toHexString(htmlCode) + ";";
        this.bytes = bytes;
    }

    public String getDescription()
    {
        return this.description;
    }

    public List<String> getAliases()
    {
        return this.aliases;
    }

    public List<String> getTags()
    {
        return this.tags;
    }

    public String getUnicode()
    {
        try
        {
            return new String(this.bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String getHtml()
    {
        return this.getHtmlDecimal();
    }

    public String getHtmlDecimal()
    {
        return this.htmlDec;
    }

    public String getHtmlHexidecimal()
    {
        return this.htmlHex;
    }

    @Override
    public String toString()
    {
        return "Emoji{" + "description='" + description + '\'' + ", aliases="
                + aliases + ", tags=" + tags + ", unicode=" + this.getUnicode()
                + ", htmlDec='" + htmlDec + '\'' + ", htmlHex='" + htmlHex
                + '\'' + '}';
    }
}
