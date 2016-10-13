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
package com.globalsight.ling.docproc.extractor.rtf;

import com.globalsight.ling.docproc.TmxTagGenerator;
import com.globalsight.ling.docproc.extractor.html.Html2TmxMap;

import java.util.HashMap;
import java.util.Stack;

public class Rtf2TmxMap
{
    //
    // Member Variables
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
    // Constructor
    //

    public Rtf2TmxMap()
    {
    }


    //
    // Public Methods
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
    public TmxTagGenerator getPairedTmxTag(String p_name,
        boolean p_start, boolean p_isolated)
    {
        TmxTagGenerator tmxTag;

        // Well-known tag types
        if (p_name.equals("b"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.BOLD, p_isolated);
            tmxTag.setErasable(true);
        }
        else if (p_name.equals("i"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.ITALIC, p_isolated);
            tmxTag.setErasable(true);
        }
        else if (p_name.equals("ul"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.UNDERLINE, p_isolated);
            tmxTag.setErasable(true);
        }
        else if (p_name.equals("uldb"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.DOUBLEUNDERLINE, p_isolated);
            tmxTag.setErasable(true);
        }
        else if (p_name.equals("strike"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.STRIKE, p_isolated);
        }
        /*
        else if (p_name.equals("striked"))        // double strike
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.STRIKE, p_isolated);
        }
        */
        else if (p_name.equals("sub"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.SUBSCRIPT, p_isolated);
        }
        else if (p_name.equals("super"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.SUPERSCRIPT, p_isolated);
        }
        else if (p_name.equals("f"))
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.FONTCHANGE, p_isolated);
        }
        else if (p_name.equals("cf"))             // color foreground
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.COLORCHANGE, p_isolated);
        }
        /*
        else if (p_name.equals("cb"))             // color background
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.COLORCHANGE, p_isolated);
        }
        */
        else if (p_name.equals("link"))           // fake HYPERLINK field
        {
            tmxTag = getPairedTag(p_start, TmxTagGenerator.LINK, p_isolated);
        }
        else
        {
            tmxTag = getPairedTag(p_start, p_name, p_isolated);
        }

        tmxTag.makeTags();

        return tmxTag;
    }


    /**
     * <p>Creates a TMX placeholder tag (returned as TmxTagGenerator
     * object).</p>
     */
    public TmxTagGenerator getPlaceholderTmxTag(String p_name)
    {
        TmxTagGenerator tmxTag;

        tmxTag = getPlaceHolderTag("x-" + p_name);
        tmxTag.makeTags();

        return tmxTag;
    }


    //
    // Protected and Private Methods Section
    //

    protected TmxTagGenerator getPairedTag(boolean p_start, int p_nType,
        boolean p_isolated)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        if (p_start)
        {
            if (p_isolated)
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
            if (p_isolated)
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


    protected TmxTagGenerator getPairedTag(boolean p_start,
        String p_strType, boolean p_isolated)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        if (p_start)
        {
            if (p_isolated)
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
            if (p_isolated)
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

        tmxTag.setInlineType("x-" + p_strType);

        return tmxTag;
    }


    protected TmxTagGenerator getPlaceHolderTag(String p_strType)
    {
        TmxTagGenerator tmxTag = new TmxTagGenerator();
        int id;

        /*
        if (p_start)
        {
            id = pushInternalId(p_strType);
            tmxTag.setInternalMatching(id);
        */
            tmxTag.setTagType(TmxTagGenerator.PH);
            /*
        }
        else
        {
            id = popInternalId(p_strType);
            tmxTag.setInternalMatching(id);
            tmxTag.setTagType(TmxTagGenerator.PH);
        }
            */

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

        String strKey = Html2TmxMap.getType(p_strType);
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
        String strKey = Html2TmxMap.getType(p_strType);
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

        String strKey = p_strType;
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
        String strKey = p_strType;
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
        String strKey = p_strType;
        Stack s = (Stack)m_hLastExternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }
        Integer o_id = (Integer)s.peek();
        return o_id.intValue();
    }
}
