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
package com.globalsight.ling.docproc.extractor.sgml;

import com.globalsight.ling.sgml.sgmlrules.SgmlRule;
import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.ling.sgml.GlobalSightEntity;

import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;

import java.util.*;

/**
 * <p>Keeps tag information and extraction rules that guide the
 * extraction of translatable and localizable attributes in SGML
 * files.</p>
 */
public class ExtractionRules
{
    //
    // Private Members
    //

    /**
     * <p>Object that holds DTD-specific extraction rules.</p>
     */
    private SgmlRule m_rule;
    private HashMap m_entities = new HashMap();

    //
    // Constructors
    //
    public ExtractionRules()
    {
    }

    /**
     * <p>Loads rules to guide extraction process from a string.</p>
     *
     * <p>String format may be list of:
     * - Extract: TAG.ATTR localizable|translatable item_type
     * - DontExtract: TAG.ATTR
     * - ExtractRule: RULE
     * - DontExtractRule: RULE
     */
    public final void loadRules(String p_rules)
        throws ExtractorException
    {
        // not implemented yet
    }

    /**
     * <p>Loads rules to guide extraction process from an object.</p>
     */
    public final void loadRules(Object p_rules)
        throws ExtractorException
    {
        if (p_rules != null && p_rules instanceof SgmlRule)
        {
            m_rule = (SgmlRule)p_rules;

            GlobalSightDtd dtd = m_rule.getDtd();
            if (dtd != null)
            {
                ArrayList ents = dtd.getEntities();

                for (int i = 0, max = ents.size(); i < max; i++)
                {
                    GlobalSightEntity ent = (GlobalSightEntity)ents.get(i);
                    m_entities.put(ent.getName(), ent.getValue());
                }
            }
        }
    }

    //
    // SGML-specific methods
    //

    public final String getLocalizableAttribType(
        String p_tag, String p_attr)
    {
        if (m_rule != null)
        {
            SgmlRule.Attribute attr = m_rule.getAttributeRule(p_tag, p_attr);
            return attr.m_type;
        }

        return "UnKnOwN";
    }

    public final boolean isLocalizableAttribute(
        String p_tag, String p_attr)
    {
        if (m_rule != null)
        {
            SgmlRule.Attribute attr = m_rule.getAttributeRule(p_tag, p_attr);

            if (attr != null)
            {
                if (attr.m_translatable.equals("loc"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean isTranslatableAttribute(
        String p_tag, String p_attr)
    {
        if (m_rule != null)
        {
            SgmlRule.Attribute attr = m_rule.getAttributeRule(p_tag, p_attr);

            if (attr != null)
            {
                if (attr.m_translatable.equals("trans"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean isInlineTag(String p_tag)
    {
        if (m_rule != null)
        {
            SgmlRule.Element elem = m_rule.getElementRule(p_tag);

            if (elem != null)
            {
                return elem.m_extract;
            }
        }

        return false;
    }

    public final boolean isPairedTag(String p_tag)
    {
        if (m_rule != null)
        {
            SgmlRule.Element elem = m_rule.getElementRule(p_tag);

            if (elem != null)
            {
                return elem.m_paired;
            }
        }

        return false;
    }

    public final boolean isUnpairedTag(String p_tag)
    {
        return !isPairedTag(p_tag);
    }

    public final boolean isWhitePreservingTag(String p_tag)
    {
        // not implemented
        return false;
    }

    public final boolean isSwitchTag(String p_tag)
    {
        // not implemented
        return false;
    }

    public final boolean isEntity(String p_name)
    {
        return m_entities.get(p_name) != null;
    }

    public final String getEntityValue(String p_name)
    {
        return (String)m_entities.get(p_name);
    }

    public final boolean isSystemEntity(String p_name)
    {
        return SystemEntities.sdataToChar(p_name) != null;
    }

    public final String getSystemEntityValue(String p_name)
    {
        return SystemEntities.sdataToChar(p_name);
    }
}
