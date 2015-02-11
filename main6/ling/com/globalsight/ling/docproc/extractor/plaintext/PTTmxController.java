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

import com.globalsight.ling.docproc.TmxTagGenerator;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>Controls the creation of diplomat format tags and atributes
 * according to a predefined set of rules. The controller is currently
 * statically configured via its own private method -
 * mapCharToRule().</p>
 */
public class PTTmxController
    extends TmxTagGenerator
    implements PTTmxControllerConstants
{

    /**
     * A hashtable that maps single characters to unique rules.
     */
    private Hashtable m_hItem = mapCharToRule();

    /**
     * <p>Will enable the conversion of leading spaces into diplomat
     * tags - <b>NOT IMPLIMENTED</b>.</p>
     */
    public boolean m_bUseTmxOnLeadingSpaces;

    public PTTmxController()
    {
        super();
    }

    /**
     * <p>Marks the position of format tokens in relation to TEXT
     * tokens.  Tokens are marked as LEADING, EMBEDDED or
     * TRAILING.</p>
     *
     * <p>Eventually, this method would also control the convertion of
     * leading/trailing spaces into PH tags.</p>
     *
     * @param p_vTokens - A vector of {@see PTTokens}.
     */
    public void applyRules(Vector p_vTokens)
    {
        boolean bInLeading = true;
        PTToken Tok;
        
    
        // First: Walk forward to mark the position of the leading
        // format tokens as leading and initially mark all others as
        // embeded.
        for (Enumeration en = p_vTokens.elements();en.hasMoreElements();)
        {
            Tok = (PTToken)en.nextElement();
            if (Tok.m_nType != PTToken.TEXT && bInLeading)
            {
                Tok.m_nPos = LEADING;
            }
            else
            {
                // ...are we in a TEXT token that consists of nothing
                // but whitespace ?
                if ((Tok.m_strContent.trim().equals("")) && bInLeading)
                {
                    // ...if so, the bInLeading flag remains true
                    // indicating that leading whitespace continues
                    // eventhough this was a TEXT token.
                    Tok.m_nPos = LEADING;
    
                    /* =============
                       TODO: At this point, we could convert this
                       whitespace chunk into some sort of PH tag. I
                       don't think this is important right now for
                       plain text.
    
                       Conversion of spaces to PH tags could be
                       controlled either by a rule (if individual
                       spaces are tokenized) or by one or more flags:
                       m_bUseTmxOnLeadingSpaces etc...
                       ================*/
                }
                else
                {
                    Tok.m_nPos = EMBEDDED;
                    bInLeading = false;
                }
            }
        }
    
        // Second: reverse walk up to first TEXT token to mark
        // trailing tmx as trailing
        int i = p_vTokens.size();
        for (int p = 0; p < i; --i)
        {
            Tok = (PTToken)p_vTokens.get(i-1);
    
            if (Tok.m_nType != PTToken.TEXT)
            {
                Tok.m_nPos = TRAILING;
            }
            else
            {
                break;
            }
        }
    }

    /**
     * <p>Looks up the rule for the given character and builds the tag
     * according to the rule. If no rule exists, returns false.</p>
     *
     * @param p_char - the character to be converted.
     * @param p_nPos - indicates whether the character is leading,
     * middle or trailing.
     * @return boolean - true when character/rule was found. False if
     * no rules exists for the given character.
     */
    public boolean makeTmx(char p_Char, int p_nPos)
    {
        PTTmxControllerRule TmxRule = new PTTmxControllerRule();
    
        // lookup the rule for this character
        TmxRule = (PTTmxControllerRule)m_hItem.get(new Character(p_Char));
    
        if (TmxRule == null)
        {
            return false;                         // no mapping
        }
    
        // set the tag attributes for this character (at the given position)
        // according to the rule.
        setTagType(PH);
        setInlineType(TmxRule.m_nInlineType);
    
        if (p_nPos == LEADING)
        {
            if (TmxRule.m_bOnLead)
            {
                setErasable(TmxRule.m_bOnLeadErasable);
            }
            else
            {
                return false; // do not encode at this position
            }
        }
        else if (p_nPos == EMBEDDED)
        {
            if (TmxRule.m_bOnMid)
            {
                setErasable(TmxRule.m_bOnMidErasable);
            }
            else
            {
                return false;
            }
        }
        else if (p_nPos == TRAILING)
        {
            if (TmxRule.m_bOnTrail)
            {
                setErasable(TmxRule.m_bOnTrailErasable);
            }
            else
            {
                return false;
            }
        }
    
        makeTags();
    
        return true;
    }

    /**
     * <p>Maps a characters to a controller rule.</p>
     *
     * <p>See the rule class for a complete description of the rule
     * parameters.</p>
     *
     * @return Hashtable
     */
    private Hashtable mapCharToRule()
    {
        Hashtable h = new Hashtable();
                                                              //           Const
                                                              //           from              OnLead           OnMid             OnTail
                                                              //  Char   TmxTagGen  OnLead   Erasable  OnMid  Erasable  OnTail  Erasable
        h.put(new Character('\u000c'), new PTTmxControllerRule('\u000c',  FORMFEED,  false,   false,  false,    false,   false,  false   ));
        h.put(new Character('\n'),     new PTTmxControllerRule(    '\n', LINEBREAK,  false,   false,  false,    false,   false,  false  ));
        h.put(new Character('\r'),     new PTTmxControllerRule(    '\r', LINEBREAK,  false,   false,  false,    false,   false,  false  ));
        h.put(new Character('\t'),     new PTTmxControllerRule(    '\t',       TAB,  false,   false,  false,    false,   false,  false   ));
        h.put(new Character('\u00a0'), new PTTmxControllerRule('\u00a0',   NBSPACE,  false,   false,  false,    false,   false,  false   ));
    
        // PREVIOUSLY - some things were on
        // Jim suggested they must be off for segmenter to break.
        //h.put(new Character('\u000c'), new PTTmxControllerRule('\u000c',  FORMFEED,  true,    true,   true,    true,    true,    true   ));
        //h.put(new Character('\n'),     new PTTmxControllerRule(    '\n', LINEBREAK,  true,    false,  true,    false,   true,    false  ));
        //h.put(new Character('\r'),     new PTTmxControllerRule(    '\r', LINEBREAK,  true,    false,  true,    false,   true,    false  ));
        //h.put(new Character('\t'),     new PTTmxControllerRule(    '\t',       TAB,  true,    true,   true,    false,   true,    true   ));
        //h.put(new Character('\u00a0'), new PTTmxControllerRule('\u00a0',   NBSPACE,  true,    true,   true,    true,    true,    true   ));
        
        return h;
    }
}