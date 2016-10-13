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

import java.util.Hashtable;

/**
 * <p>Represents an entry in the PseudoOverrideMap.</p>
 * @see PseudoData
 */
public class PseudoOverrideMapItem
{
    /** True indicates the name is always used with a paired type (bpt/ept) */
    public boolean m_bPaired = false;
    /** The verbose name for this tag. */
    public String m_strVerbose = "";
    /** The compact name for this tag. */
    public String m_strCompact= "";
    /** The native tmx type that this name is associated with.*/
    public String m_strTmx = "";
    /** When false, this tag name will become unnumbered.*/
    public boolean m_bNumbered = false;
    /** When NOT null, this tag name can be added by the user.*/
    public Hashtable m_hAttributes = null;

    //
    // Class constructor
    //

    /**
     * <p>Represents an entry in the PseudoOverrideMap.</p>
     * @see PseudoData
     *
     * @param p_strTmxType - The native tmx type that this entry is
     * associated with.<p>
     * @param p_bPaired - True indicates that the name is always used
     * with a paired type (bpt/ept)<p>
     * @param p_strVerbose - The verbose name for the resulting PTag.
     * @param p_strCompact - The compact name for the resulting PTag.
     * @param p_bNumbered - When false, this type will become
     * unnumbered.
     * @param p_hAddableAttributes - The attribute list from which to
     * generate TMX content.  See PseudoData.mapPseudoOverrides().
     */
    public PseudoOverrideMapItem(String p_strTmxType, boolean p_bPaired,
      String p_strVerbose, String p_strCompact, boolean p_bNumbered,
      Hashtable p_hAddableAttributes)
        throws PseudoOverrideItemException
    {

        /**
         * Rule: Addable MUST be Unnumbered, and likewise: Unnumbered
         * MUST be Addable"); This allows the add process to create
         * the TMX for ALL non-unique PTag names (no matter if they
         * were added or existing in the source) while at the same
         * time create and map unique "i" attributes for all paired
         * types.
         */
        if (((p_bNumbered == false) && (p_hAddableAttributes == null)) ||
          ((p_hAddableAttributes != null) && (p_bNumbered == true)))
        {
            throw new PseudoOverrideItemException(
                "Rule: Addable MUST be Unnumbered, likewise: " +
                "Unnumbered MUST be Addable");
        }


        // All string must be defined.
        if (p_strTmxType == null || p_strTmxType.length() == 0 ||
            p_strVerbose == null || p_strVerbose.length() == 0 ||
            p_strCompact == null || p_strCompact.length() == 0)
        {
            throw new PseudoOverrideItemException(
                "Must define all strings in the override item.");
        }


        // If addable, attributes must be defined.
        if (p_hAddableAttributes != null)
        {
            if ((p_hAddableAttributes.get(
                PseudoConstants.ADDABLE_TMX_TAG) == null ) ||
                (p_hAddableAttributes.get(
                    PseudoConstants.ADDABLE_TMX_TYPE) == null ) ||
                (p_hAddableAttributes.get(
                    PseudoConstants.ADDABLE_ATTR_ERASABLE) == null ) ||
                (p_hAddableAttributes.get(
                    PseudoConstants.ADDABLE_HTML_CONTENT) == null ) )
            {
                throw new PseudoOverrideItemException("Missing attributes.");
            }

            if (p_bPaired == true)
            {
                if ((p_hAddableAttributes.get(
                    PseudoConstants.ADDABLE_TMX_ENDPAIRTAG) == null) ||
                    (p_hAddableAttributes.get(
                        PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT) == null))
                {
                    throw new PseudoOverrideItemException(
                        "Missing end paired attributes.");
                }
            }
        }

        m_strTmx = p_strTmxType;
        m_bPaired = p_bPaired;
        m_strVerbose = p_strVerbose;
        m_strCompact = p_strCompact;
        m_bNumbered = p_bNumbered;
        m_hAttributes = p_hAddableAttributes;
    }
}
