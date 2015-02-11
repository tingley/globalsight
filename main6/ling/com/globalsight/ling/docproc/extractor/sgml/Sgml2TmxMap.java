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

import com.globalsight.ling.docproc.extractor.html.Html2TmxMap;
import com.globalsight.ling.docproc.extractor.sgml.SgmlObjects;

import com.globalsight.ling.docproc.TmxTagGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class Sgml2TmxMap
{
    //
    // Member Variable Section
    //

    /**
     * <p>Segment-internal match counter (TMX i attribute).  Must be
     * reset after each segment by calling {@link #resetCounter()
     * resetCounter()}.</p>
     */
    private int m_nNextInternalID = 0;
    /** <p>Segment-external match counter (TMX x attribute).*/
    private int m_nNextExternalID = 0;

    /**
     * <p>Hashtable to keep track of the occurences and assigned ids
     * for tags.  Ids are tracked for both intra-segment assignments
     * (i), and inter-segment assignments (&lt;it x&gt;).
     */
    private HashMap m_hLastInternalID = new HashMap();
    private HashMap m_hLastExternalID = new HashMap();
    
    private static Map<String, Integer[]> TMX_TAGS = new HashMap<String, Integer[]>();
    static
    {
        TMX_TAGS.put("b", new Integer[]{TmxTagGenerator.X_BOLD, TmxTagGenerator.BOLD});
        TMX_TAGS.put("strong", new Integer[]{TmxTagGenerator.X_STRONG, TmxTagGenerator.STRONG});
        TMX_TAGS.put("i", new Integer[]{TmxTagGenerator.X_ITALIC, TmxTagGenerator.ITALIC});
        TMX_TAGS.put("em", new Integer[]{TmxTagGenerator.X_EM, TmxTagGenerator.EM});
        TMX_TAGS.put("u", new Integer[]{TmxTagGenerator.X_UNDERLINE, TmxTagGenerator.UNDERLINE});
        TMX_TAGS.put("a", new Integer[]{TmxTagGenerator.LINK});
        TMX_TAGS.put("strike", new Integer[]{TmxTagGenerator.STRIKE});
        TMX_TAGS.put("sub", new Integer[]{TmxTagGenerator.SUBSCRIPT});
        TMX_TAGS.put("sup", new Integer[]{TmxTagGenerator.SUPERSCRIPT});
        TMX_TAGS.put("font", new Integer[]{TmxTagGenerator.FONTCHANGE});
        
        TMX_TAGS.put("B", new Integer[]{TmxTagGenerator.C_X_BOLD, TmxTagGenerator.C_BOLD});
        TMX_TAGS.put("STRONG", new Integer[]{TmxTagGenerator.C_X_STRONG, TmxTagGenerator.C_STRONG});
        TMX_TAGS.put("I", new Integer[]{TmxTagGenerator.C_X_ITALIC, TmxTagGenerator.C_ITALIC});
        TMX_TAGS.put("EM", new Integer[]{TmxTagGenerator.C_X_EM, TmxTagGenerator.C_EM});
        TMX_TAGS.put("U", new Integer[]{TmxTagGenerator.C_X_UNDERLINE, TmxTagGenerator.C_UNDERLINE});
        TMX_TAGS.put("A", new Integer[]{TmxTagGenerator.C_LINK});
        TMX_TAGS.put("STRIKE", new Integer[]{TmxTagGenerator.C_STRIKE});
        TMX_TAGS.put("SUB", new Integer[]{TmxTagGenerator.C_SUBSCRIPT});
        TMX_TAGS.put("SUP", new Integer[]{TmxTagGenerator.C_SUPERSCRIPT});
        TMX_TAGS.put("FONT", new Integer[]{TmxTagGenerator.C_FONTCHANGE});
    }


    //
    // Constructor Section
    //

    public Sgml2TmxMap()
    {
    }


    //
    // Public Methods Section
    //

    /** <p>resets the segment-internal match counter. */
    public void resetCounter()
    {
        m_nNextInternalID = 0;
    }


    /** <p>resets the segment-internal match counter and id context. */
    public void reset()
    {
        resetCounter();
        m_nNextExternalID = 0;
        m_hLastInternalID.clear();
        m_hLastExternalID.clear();
    }


    /**
     * <p>Creates a TMX tag (a TmxTagGenerator object to be exact)
     * that represents a &lt;bpt&gt;, an &lt;ept&gt;, or &lt;ph&gt;
     * tag depending on whether the tag is a start or end tag and
     * whether it appears isolated or not.</p>
     */
    public TmxTagGenerator getPairedTmxTag(SgmlObjects.Tag p_tag,
        boolean p_bStart, boolean p_bIsolated)
    {
        boolean hasAttributes = (p_tag.attributes.size() > 0);

        return getPairedTmxTag(p_tag.tag, p_bStart, p_bIsolated,
            hasAttributes);
    }

    public TmxTagGenerator getPairedTmxTag(SgmlObjects.EndTag p_tag,
        boolean p_bStart, boolean p_bIsolated)
    {
        return getPairedTmxTag(p_tag.tag, p_bStart, p_bIsolated, false);
    }

    /**
     * <p>Creates a TMX tag (a TmxTagGenerator object to be exact)
     * that represents a &lt;bpt&gt;, an &lt;ept&gt;, or &lt;ph&gt;
     * tag depending on whether the tag is a start or end tag and
     * whether it appears isolated or not.</p>
     */
    private TmxTagGenerator getPairedTmxTag(String p_tagName,
        boolean p_bStart, boolean p_bIsolated, boolean p_hasAttributes)
    {
        TmxTagGenerator tmxTag;
        
        if (p_tagName != null && p_tagName.length() > 0)
        {
            if (Character.isLowerCase(p_tagName.charAt(0)))
            {
                p_tagName = p_tagName.toLowerCase();
            }
            else
            {
                p_tagName = p_tagName.toUpperCase();
            }
        }
        
        Integer[] args = TMX_TAGS.get(p_tagName);
        if (args != null)
        {
            if (args.length == 2)
            {
                if (p_hasAttributes)
                {
                    tmxTag = getPairedTag(p_bStart, args[0], p_bIsolated);
                    tmxTag.setErasable(false);
                }
                else
                {
                    tmxTag = getPairedTag(p_bStart, args[1], p_bIsolated);
                    tmxTag.setErasable(true);
                }
            }
            else
            {
                tmxTag = getPairedTag(p_bStart, args[0], p_bIsolated);
            }
        }
        else
        {
            tmxTag = getPairedTag(p_bStart, p_tagName, p_bIsolated);
        }

        tmxTag.makeTags();

        return tmxTag;
    }


    /**
     * <p>Creates a TMX placeholder tag (returned as TmxTagGenerator
     * object).</p>
     */
    public TmxTagGenerator getPlaceholderTmxTag(String p_strTagName,
        boolean p_bStart)
    {
        TmxTagGenerator tmxTag;

        if (p_strTagName.equalsIgnoreCase("basefont"))
        {
            tmxTag = getPlaceHolderTag(p_bStart, "font");
        }
        else
        {

            if (p_bStart)
            {
                tmxTag = getPlaceHolderTag(p_bStart,
                    "x-" + p_strTagName.toLowerCase());
            }
            else
            {
                tmxTag = getPlaceHolderTag(p_bStart,
                    "xe-" + p_strTagName.toLowerCase());
            }
        }

        tmxTag.makeTags();

        return tmxTag;
    }


    //
    // Protected and Private Methods Section
    //

    protected TmxTagGenerator getPairedTag(boolean p_bStart, int p_nType,
        boolean p_bIsolated)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            if (p_bIsolated)
            {
                id = pushExternalId("" + p_nType);
                tmxTag.setExternalMatching(id);
                tmxTag.setPosition(TmxTagGenerator.POS_BEGIN);
                tmxTag.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = pushInternalId(p_nType);
                tmxTag.setInternalMatching(id);
                tmxTag.setTagType(TmxTagGenerator.BPT);
            }
        }
        else
        {
            if (p_bIsolated)
            {
                id = popExternalId("" + p_nType);
                tmxTag.setExternalMatching(id);
                tmxTag.setPosition(TmxTagGenerator.POS_END);
                tmxTag.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = popInternalId("" + p_nType);
                tmxTag.setInternalMatching(id);
                tmxTag.setTagType(TmxTagGenerator.EPT);
            }
        }

        tmxTag.setInlineType(p_nType);

        return tmxTag;
    }


    protected TmxTagGenerator getPairedTag(boolean p_bStart,
        String p_strType, boolean p_bIsolated)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            if (p_bIsolated)
            {
                id = pushExternalId(p_strType);
                tmxTag.setExternalMatching(id);
                tmxTag.setPosition(TmxTagGenerator.POS_BEGIN);
                tmxTag.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = pushInternalId(p_strType);
                tmxTag.setInternalMatching(id);
                tmxTag.setTagType(TmxTagGenerator.BPT);
            }
        }
        else
        {
            if (p_bIsolated)
            {
                id = popExternalId(p_strType);
                tmxTag.setExternalMatching(id);
                tmxTag.setPosition(TmxTagGenerator.POS_END);
                tmxTag.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = popInternalId(p_strType);
                tmxTag.setInternalMatching(id);
                tmxTag.setTagType(TmxTagGenerator.EPT);
            }
        }

        tmxTag.setInlineType("x-" + p_strType.toLowerCase());

        return tmxTag;
    }


    protected TmxTagGenerator getPlaceHolderTag(boolean p_bStart,
        String p_strType)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            id = pushInternalId(p_strType);
            tmxTag.setInternalMatching(id);
            tmxTag.setTagType(TmxTagGenerator.PH);
        }
        else
        {
            id = popInternalId(p_strType);
            tmxTag.setInternalMatching(id);
            tmxTag.setTagType(TmxTagGenerator.PH);
        }

        tmxTag.setInlineType(p_strType);

        return tmxTag;
    }


    /**
     * <p>Allocates the next internal match id (i) and pushes it on
     * the context stack for the tag with the well-known type
     * p_nType.</p>
     *
     * <p>Note that B/I/U which have attributes are printed as type
     * "x-bold" (not as "bold"), but they are treated on the stack as
     * their original type. This is so that the corresponding end tag
     * can be associated with the correct start tag.</p>
     *
     * <p>Forwards to pushInternalId(String).</p>
     */
    protected int pushInternalId(int p_nType)
    {        
        return pushInternalId(Html2TmxMap.getType(p_nType));
    }

    /**
     * <p>Allocate the next internal match id (i) and push it on the
     * context stack for the tag p_strType.
     */
    protected int pushInternalId(String p_strType)
    {
        ++m_nNextInternalID;

        String strKey = Html2TmxMap.getType(p_strType.toLowerCase());
        Stack s = (Stack)m_hLastInternalID.get(strKey);
        if (s == null)
        {
            s = new Stack ();
            s.push(new Integer(m_nNextInternalID));
            m_hLastInternalID.put(strKey, s);
        }
        else
        {
            s.push(new Integer(m_nNextInternalID));
        }

        return m_nNextInternalID;
    }


    /**
     * <p>Pop the internal match id (i) from the context stack of the
     * tag p_strType.
     */
    protected int popInternalId(String p_strType)
    {
        String strKey = Html2TmxMap.getType(p_strType.toLowerCase());
        Stack s = (Stack)m_hLastInternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }

        Integer o_id = (Integer)s.pop();

        return o_id.intValue();
    }

    protected int pushExternalId(String p_strType)
    {
        ++m_nNextExternalID;

        String strKey = p_strType.toLowerCase();
        Stack s = (Stack)m_hLastExternalID.get(strKey);
        if (s == null)
        {
            s = new Stack ();
            s.push(new Integer(m_nNextExternalID));
            m_hLastExternalID.put(strKey, s);
        }
        else
        {
            s.push(new Integer(m_nNextExternalID));
        }

        return m_nNextExternalID;
    }

    protected int popExternalId(String p_strType)
    {
        String strKey = p_strType.toLowerCase();
        Stack s = (Stack)m_hLastExternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }
        Integer o_id = (Integer)s.pop();
        return o_id.intValue();
    }

    protected int peekExternalId(String p_strType)
    {
        String strKey = p_strType.toLowerCase();
        Stack s = (Stack)m_hLastExternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }
        Integer o_id = (Integer)s.peek();
        return o_id.intValue();
    }
}
