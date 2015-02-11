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
package com.globalsight.ling.docproc.extractor.plaintext;

/**
 * <p>A rule which describes the desired tagging behavior for a given
 * character.<p>
 */
public class PTTmxControllerRule
    implements PTTmxControllerConstants
{
    //
    // Public Member Variables
    //

    /**
     * The target character.
     */
    public char m_cTrgChar;

    /**
     * Identifies the inline type for target character - a
     * TmxTagGenerator constant.
     */
    public int m_nInlineType;

    /**
     * Enable encoding for target character when leading a string.
     */
    public boolean m_bOnLead = true;

    /**
     * Enable erasable attribute for target character when leading a
     * string.
     */
    public boolean m_bOnLeadErasable = true;

    /**
     * Enable encoding for target character when embedded in a string.
     */
    public boolean m_bOnMid = true;

    /**
     * Enable erasable attribute for target character when embedded in
     * a string.
     */
    public boolean m_bOnMidErasable = true;

    /**
     * Enable encoding for target character when trialing a string.
     */
    public boolean m_bOnTrail = true;

    /**
     * Enable erasable attribute for target character when trailing in
     * a string.
     */
    public boolean m_bOnTrailErasable = true;

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    public PTTmxControllerRule()
    {
        super();
    }

    /**
     * Constructor.
     * @param: p_char the target character this rule will apply to.
     * @param: p_InlineType the inline tag type - a TmxTagGenerator
     * constant.
     */
    public PTTmxControllerRule(char p_char, int p_nInlineType)
    {
        super();

        m_cTrgChar = p_char;
        m_nInlineType = p_nInlineType;
        m_bOnLead = true;
        m_bOnLeadErasable = true;
        m_bOnMid = true;
        m_bOnMidErasable = true;
        m_bOnTrail = true;
        m_bOnTrailErasable = true;
    }

    /**
     * Constructor.
     * @param: p_char - the character the rule applies to.
     * @param: p_InlineType - a TmxTagGenerator constant.
     * @param: p_bOnLead - Enable tag when it leads on a line.
     * @param: p_bOnLeadErasable - Make a leading occurance erasable.
     * @param: p_bOnMid - Enable tag when its embedded within a line.
     * @param: p_bOnLeadErasable - Make a Mid occurance erasable.
     * @param: p_bOnTrail - Enable tag when it trails on a line.
     * @param: p_bOnLeadErasable - Make a trailing occurance erasable.
     */
    public PTTmxControllerRule( char p_char,int p_nInlineType,
      boolean p_bOnlead, boolean p_bOnleadErasable,
      boolean p_bOnMid, boolean p_bOnMidErasable,
      boolean p_bOnTrail, boolean p_bOnTrailErasable)
    {
        super();

        m_cTrgChar = p_char;
        m_nInlineType = p_nInlineType;
        m_bOnLead = p_bOnlead;
        m_bOnLeadErasable = p_bOnleadErasable;
        m_bOnMid = p_bOnMid;
        m_bOnMidErasable = p_bOnMidErasable;
        m_bOnTrail = p_bOnTrail;
        m_bOnTrailErasable = p_bOnTrailErasable;
    }
}
