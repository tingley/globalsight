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
package com.globalsight.ling.docproc.extractor.xptag;

import com.globalsight.ling.docproc.extractor.html.Html2TmxMap;
import com.globalsight.ling.docproc.extractor.xptag.XPTagObjects;

import com.globalsight.ling.docproc.TmxTagGenerator;

import java.util.HashMap;
import java.util.Stack;

/**
 * Helper class to map Quark XPress tags to TMX tags.
 */
public class Tag2TmxMap
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


    //
    // Constructor Section
    //

    public Tag2TmxMap()
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
    public TmxTagGenerator getPairedTmxTag(XPTagObjects.CharTag p_tag,
        boolean p_bStart, boolean p_bIsolated)
    {
        return getPairedTmxTag(p_tag.tag, p_bStart, p_bIsolated);
    }

    /**
     * <p>Creates a TMX tag (a TmxTagGenerator object to be exact)
     * that represents a &lt;bpt&gt;, an &lt;ept&gt;, or &lt;ph&gt;
     * tag depending on whether the tag is a start or end tag and
     * whether it appears isolated or not.</p>
     */
    private TmxTagGenerator getPairedTmxTag(String p_tagName,
        boolean p_bStart, boolean p_bIsolated)
    {
        TmxTagGenerator result;

        String type = p_tagName.substring(1, p_tagName.length() - 1);

        // Well-known tag types
        if (type.equals("b"))
        {
            result = getPairedTag(p_bStart, TmxTagGenerator.BOLD, p_bIsolated);
            result.setErasable(true);
        }
        else if (type.equals("i"))
        {
            result = getPairedTag(p_bStart, TmxTagGenerator.ITALIC, p_bIsolated);
            result.setErasable(true);
        }
        else
        {
            result = getPairedTag(p_bStart, type, p_bIsolated);
        }

        result.makeTags();

        return result;
    }


    /**
     * <p>Creates a TMX placeholder tag (returned as TmxTagGenerator
     * object).</p>
     */
    public TmxTagGenerator getPlaceholderTmxTag(String p_tagName,
        boolean p_bStart)
    {
        TmxTagGenerator result;

        String type = p_tagName.substring(1, p_tagName.length() - 1);

        if (type.equals("\\n"))
        {
            result = getPlaceHolderTag(p_bStart, "x-br");
            result.setErasable(true);
        }
        else
        {
            result = getPlaceHolderTag(p_bStart, "x-xptag");
        }

        result.makeTags();

        return result;
    }


    //
    // Protected and Private Methods Section
    //

    protected TmxTagGenerator getPairedTag(boolean p_bStart, int p_nType,
        boolean p_bIsolated)
    {
        TmxTagGenerator result = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            if (p_bIsolated)
            {
                id = pushExternalId("" + p_nType);
                result.setExternalMatching(id);
                result.setPosition(TmxTagGenerator.POS_BEGIN);
                result.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = pushInternalId(p_nType);
                result.setInternalMatching(id);
                result.setTagType(TmxTagGenerator.BPT);
            }
        }
        else
        {
            if (p_bIsolated)
            {
                id = popExternalId("" + p_nType);
                result.setExternalMatching(id);
                result.setPosition(TmxTagGenerator.POS_END);
                result.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = popInternalId("" + p_nType);
                result.setInternalMatching(id);
                result.setTagType(TmxTagGenerator.EPT);
            }
        }

        result.setInlineType(p_nType);

        return result;
    }


    protected TmxTagGenerator getPairedTag(boolean p_bStart,
        String p_strType, boolean p_bIsolated)
    {
        TmxTagGenerator result = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            if (p_bIsolated)
            {
                id = pushExternalId(p_strType);
                result.setExternalMatching(id);
                result.setPosition(TmxTagGenerator.POS_BEGIN);
                result.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = pushInternalId(p_strType);
                result.setInternalMatching(id);
                result.setTagType(TmxTagGenerator.BPT);
            }
        }
        else
        {
            if (p_bIsolated)
            {
                id = popExternalId(p_strType);
                result.setExternalMatching(id);
                result.setPosition(TmxTagGenerator.POS_END);
                result.setTagType(TmxTagGenerator.IT);
            }
            else
            {
                id = popInternalId(p_strType);
                result.setInternalMatching(id);
                result.setTagType(TmxTagGenerator.EPT);
            }
        }

        result.setInlineType("x-xptag");

        return result;
    }


    protected TmxTagGenerator getPlaceHolderTag(boolean p_bStart,
        String p_strType)
    {
        TmxTagGenerator result = new TmxTagGenerator();
        int id;

        if (p_bStart)
        {
            id = pushInternalId(p_strType);
            result.setInternalMatching(id);
            result.setTagType(TmxTagGenerator.PH);
        }
        else
        {
            id = popInternalId(p_strType);
            result.setInternalMatching(id);
            result.setTagType(TmxTagGenerator.PH);
        }

        result.setInlineType(p_strType);

        return result;
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
