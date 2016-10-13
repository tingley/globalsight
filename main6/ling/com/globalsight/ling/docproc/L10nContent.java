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
package com.globalsight.ling.docproc;

import com.globalsight.ling.common.Transcoder;
import com.globalsight.ling.common.TranscoderException;

public class L10nContent
{
    StringBuffer m_l10nContent;

    /**
     * Constructor.
     */
    public L10nContent()
    {
        super();

        m_l10nContent = new StringBuffer();
    }

    /**
     * Constructor.
     */
    public L10nContent(String p_l10nContent)
    {
        super();

        m_l10nContent = new StringBuffer(p_l10nContent);
    }

    public void addContent(String p_content)
    {
        m_l10nContent.append(p_content);
    }

    /**
     * Returns a String that represents the value of this object.
     */
    public String getL10nContent()
    {
        return m_l10nContent.toString();
    }

    public byte[] getTranscodedL10nContent(String p_codeset)
        throws TranscoderException
    {
        byte[] tmp = null;

        Transcoder transcoder = new Transcoder();
        tmp = transcoder.transcode(m_l10nContent.toString(), p_codeset);

        return tmp;
    }

    public void setL10nContent(String p_l10nContent)
    {
        m_l10nContent.setLength(0);
        m_l10nContent.append(p_l10nContent);
    }

    /**
     * Returns the last char in the output string.
     */
    public char getLastChar()
    {
        int len = m_l10nContent.length();

        if (len == 0)
        {
            return 0;
        }
        else
        {
            return m_l10nContent.charAt(len - 1);
        }
    }
}
