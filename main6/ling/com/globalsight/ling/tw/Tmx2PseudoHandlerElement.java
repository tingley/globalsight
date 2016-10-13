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
package com.globalsight.ling.tw;

import java.util.Vector;

/**
 * NOTE: Last minute extraction of inner class - Will comment later.
 */
public class Tmx2PseudoHandlerElement
{
    public int type = Tmx2PseudoHandler.UNSET;
    public StringBuffer text = new StringBuffer();
    public Vector subTags = new Vector();
    public String tagName = null;

    /**
     * Tmx2PseudoHandlerElement constructor comment.
     */
    public Tmx2PseudoHandlerElement()
    {
        super();
    }

    public final void setText(String s)
    {
        text.setLength(0);
        text.append(s);
    }

    public final String getText()
    {
        return text.toString();
    }

    public final void append(String s)
    {
        text.append(s);
    }

    public final void append(char c)
    {
        text.append(c);
    }

}
