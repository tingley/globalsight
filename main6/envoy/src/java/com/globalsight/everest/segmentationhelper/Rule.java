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
package com.globalsight.everest.segmentationhelper;

import java.util.*;

/**
 * Rule class represents rule element in segmentation rule file writen in xml
 * format.
 * 
 * @author holden.cai
 * 
 */
public class Rule
{
    /**
     * The break attribute in a rule element.
     */
    private boolean m_break;
    /**
     * The text of a rule element's before child element.
     */
    private String m_beforebreak;
    /**
     * The text of a rule element's after child element.
     */
    private String m_afterbreak;

    public Rule()
    {
    }

    public Rule(String before, String after, boolean isBreak)
    {
        m_break = isBreak;
        m_beforebreak = before;
        m_afterbreak = after;
    }

    /**
     * Get the text of afterbreak element. The text should be regular
     * expression.
     * 
     * @return
     */
    public String getAfterBreak()
    {
        return m_afterbreak;
    }

    /**
     * Set the text of afterbreak element. The text should be regular
     * expression.
     * 
     * @param p_afterbreak
     */
    public void setAfterBreak(String p_afterbreak)
    {
        m_afterbreak = p_afterbreak;
    }

    /**
     * Get the text of beforebreak element. The text should be regular
     * expression.
     * 
     * @return
     */
    public String getBeforeBreak()
    {
        return m_beforebreak;
    }

    /**
     * Set the text of beforebreak element. The text should be regular
     * expression.
     * 
     * @param p_beforebreak
     */
    public void setBeforeBreak(String p_beforebreak)
    {
        m_beforebreak = p_beforebreak;
    }

    /**
     * Get break attribute of rule element, if "yes", return false, otherwise
     * return true.
     * 
     * @return
     * @deprecated Use {@link #isBreak()} instead
     */
    public boolean getBreak()
    {
        return isBreak();
    }

    /**
     * Get break attribute of rule element, if "yes", return false, otherwise
     * return true.
     * 
     * @return
     */
    public boolean isBreak()
    {
        return m_break;
    }

    /**
     * @deprecated Use {@link #isBreak(boolean)} instead
     */
    public void setBreak(boolean p_break)
    {
        isBreak(p_break);
    }

    /**
     * Set break attribute.
     * 
     * @param p_break
     */
    public void isBreak(boolean p_break)
    {
        m_break = p_break;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("m_isBreak: ");
        sb.append(this.isBreak() + "\n");
        sb.append("m_beforebreak: ");
        sb.append(m_beforebreak + "\n");
        sb.append("m_afterbreak: ");
        sb.append(m_afterbreak + "\n");
        return sb.toString();
    }

}
