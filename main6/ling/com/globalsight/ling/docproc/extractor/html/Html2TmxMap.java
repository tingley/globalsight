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
package com.globalsight.ling.docproc.extractor.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.globalsight.ling.docproc.TmxTagGenerator;

public class Html2TmxMap
{
    //
    // Member Variable Section
    //

    /**
     * <p>
     * Segment-internal match counter (TMX i attribute). Must be reset after
     * each segment by calling {@link #resetCounter() resetCounter()}.
     * </p>
     */
    private int m_nNextInternalID = 0;
    /**
     * <p>
     * Segment-external match counter (TMX x attribute).
     */
    private int m_nNextExternalID = 0;

    /**
     * <p>
     * Hashtable to keep track of the occurences and assigned ids for tags. Ids
     * are tracked for both intra-segment assignments (i), and inter-segment
     * assignments (&lt;it x&gt;).
     */
    private HashMap m_hLastInternalID = new HashMap();
    private HashMap m_hLastExternalID = new HashMap();

    private static Map<String, Integer[]> TMX_TAGS = new HashMap<String, Integer[]>();
    static
    {
        TMX_TAGS.put("b", new Integer[]
        { TmxTagGenerator.X_BOLD, TmxTagGenerator.BOLD });
        TMX_TAGS.put("strong", new Integer[]
        { TmxTagGenerator.X_STRONG, TmxTagGenerator.STRONG });
        TMX_TAGS.put("i", new Integer[]
        { TmxTagGenerator.X_ITALIC, TmxTagGenerator.ITALIC });
        TMX_TAGS.put("em", new Integer[]
        { TmxTagGenerator.X_EM, TmxTagGenerator.EM });
        TMX_TAGS.put("u", new Integer[]
        { TmxTagGenerator.X_UNDERLINE, TmxTagGenerator.UNDERLINE });
        TMX_TAGS.put("a", new Integer[]
        { TmxTagGenerator.LINK });
        TMX_TAGS.put("strike", new Integer[]
        { TmxTagGenerator.STRIKE });
        TMX_TAGS.put("sub", new Integer[]
        { TmxTagGenerator.SUBSCRIPT });
        TMX_TAGS.put("sup", new Integer[]
        { TmxTagGenerator.SUPERSCRIPT });
        TMX_TAGS.put("font", new Integer[]
        { TmxTagGenerator.FONTCHANGE });

        TMX_TAGS.put("B", new Integer[]
        { TmxTagGenerator.C_X_BOLD, TmxTagGenerator.C_BOLD });
        TMX_TAGS.put("STRONG", new Integer[]
        { TmxTagGenerator.C_X_STRONG, TmxTagGenerator.C_STRONG });
        TMX_TAGS.put("I", new Integer[]
        { TmxTagGenerator.C_X_ITALIC, TmxTagGenerator.C_ITALIC });
        TMX_TAGS.put("EM", new Integer[]
        { TmxTagGenerator.C_X_EM, TmxTagGenerator.C_EM });
        TMX_TAGS.put("U", new Integer[]
        { TmxTagGenerator.C_X_UNDERLINE, TmxTagGenerator.C_UNDERLINE });
        TMX_TAGS.put("A", new Integer[]
        { TmxTagGenerator.C_LINK });
        TMX_TAGS.put("STRIKE", new Integer[]
        { TmxTagGenerator.C_STRIKE });
        TMX_TAGS.put("SUB", new Integer[]
        { TmxTagGenerator.C_SUBSCRIPT });
        TMX_TAGS.put("SUP", new Integer[]
        { TmxTagGenerator.C_SUPERSCRIPT });
        TMX_TAGS.put("FONT", new Integer[]
        { TmxTagGenerator.C_FONTCHANGE });
    }

    //
    // Constructor Section
    //

    public Html2TmxMap()
    {
    }

    //
    // Public Methods Section
    //

    /**
     * <p>
     * resets the segment-internal match counter.
     */
    public void resetCounter()
    {
        m_nNextInternalID = 0;
    }

    /**
     * <p>
     * resets the segment-internal match counter and id context.
     */
    public void reset()
    {
        resetCounter();
        m_nNextExternalID = 0;
        m_hLastInternalID.clear();
        m_hLastExternalID.clear();
    }

    public TmxTagGenerator getPairedInternalTmxTag(HtmlObjects.Tag p_tag,
            boolean p_bStart)
    {
        boolean hasAttributes = (p_tag.attributes.size() > 0);
        TmxTagGenerator tmxTag = getPairedTmxTag(p_tag.tag, p_bStart, false,
                hasAttributes);

        if (p_tag.isFromOfficeContent)
        {
            tmxTag.setFromOfficeContent(true);
        }
        tmxTag.setInternalTag(true);
        tmxTag.makeTags();

        return tmxTag;
    }

    public TmxTagGenerator getPairedTagForInternalText(
            HtmlObjects.InternalText internalText)
    {
        TmxTagGenerator tmxTagBpt = getPairedTmxTag(internalText.tag, true,
                false, false);

        if (internalText.isFromOfficeContent)
        {
            tmxTagBpt.setFromOfficeContent(true);
        }
        tmxTagBpt.setInternalTag(true);
        tmxTagBpt.makeTags();

        TmxTagGenerator tmxTagEpt = getPairedTmxTag(internalText.tag, false,
                false, false);
        if (internalText.isFromOfficeContent)
        {
            tmxTagEpt.setFromOfficeContent(true);
        }
        tmxTagEpt.makeTags();

        String newStart = tmxTagBpt.getStart() + tmxTagBpt.getEnd();
        if (newStart != null && newStart.contains(" type=\"x-gs-internal-text\""))
        {
            newStart = newStart.replace(" type=\"x-gs-internal-text\"", "");
        }
        tmxTagBpt.setStart(newStart);
        tmxTagBpt.setEnd(tmxTagEpt.getStart() + tmxTagEpt.getEnd());

        return tmxTagBpt;
    }

    /**
     * <p>
     * Creates a TMX tag (a TmxTagGenerator object to be exact) that represents
     * a &lt;bpt&gt;, an &lt;ept&gt;, or &lt;ph&gt; tag depending on whether the
     * tag is a start or end tag and whether it appears isolated or not.
     * </p>
     */
    public TmxTagGenerator getPairedTmxTag(HtmlObjects.Tag p_tag,
            boolean p_bStart, boolean p_bIsolated)
    {
        boolean hasAttributes = (p_tag.attributes.size() > 0);

        return getBuildedPairedTmxTag(p_tag, p_bStart, p_bIsolated,
                hasAttributes);
    }

    public TmxTagGenerator getPairedTmxTag(HtmlObjects.EndTag p_tag,
            boolean p_bStart, boolean p_bIsolated)
    {
        return getBuildedPairedTmxTag(p_tag, p_bStart, p_bIsolated, false);
    }

    public TmxTagGenerator getPairedTmxTag(HtmlObjects.CFTag p_tag,
            boolean p_bStart, boolean p_bIsolated)
    {
        return getBuildedPairedTmxTag(p_tag.tag, p_bStart, p_bIsolated, false);
    }

    private TmxTagGenerator getBuildedPairedTmxTag(String p_tagName,
            boolean p_bStart, boolean p_bIsolated, boolean p_hasAttributes)
    {
        TmxTagGenerator tmxTag = getPairedTmxTag(p_tagName, p_bStart,
                p_bIsolated, p_hasAttributes);
        tmxTag.makeTags();

        return tmxTag;
    }

    /**
     * Overloaded method of getBuildedPairedTmxTag(String, boolean, boolean)
     */
    private TmxTagGenerator getBuildedPairedTmxTag(
            HtmlObjects.HtmlElement p_tag, boolean p_bStart,
            boolean p_bIsolated, boolean p_hasAttributes)
    {
        String tagName = "";
        if (p_tag instanceof HtmlObjects.Tag)
        {
            tagName = ((HtmlObjects.Tag) p_tag).tag;
        }
        else if (p_tag instanceof HtmlObjects.EndTag)
        {
            tagName = ((HtmlObjects.EndTag) p_tag).tag;
        }

        TmxTagGenerator tmxTag = getPairedTmxTag(tagName, p_bStart,
                p_bIsolated, p_hasAttributes);

        if (p_tag.isFromOfficeContent)
        {
            tmxTag.setFromOfficeContent(true);
        }
        tmxTag.makeTags();

        return tmxTag;
    }

    /**
     * <p>
     * Creates a TMX tag (a TmxTagGenerator object to be exact) that represents
     * a &lt;bpt&gt;, an &lt;ept&gt;, or &lt;ph&gt; tag depending on whether the
     * tag is a start or end tag and whether it appears isolated or not.
     * </p>
     */
    private TmxTagGenerator getPairedTmxTag(String p_tagName, boolean p_bStart,
            boolean p_bIsolated, boolean p_hasAttributes)
    {
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

        TmxTagGenerator tmxTag;

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

        return tmxTag;
    }

    /**
     * <p>
     * Creates a TMX placeholder tag (returned as TmxTagGenerator object).
     * </p>
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

    /**
     * Overloaded method of getPlaceholderTmxTag(String, boolean).
     */
    public TmxTagGenerator getPlaceholderTmxTag(HtmlObjects.Tag p_tag,
            boolean p_bStart)
    {
        String tagName = p_tag.tag;

        TmxTagGenerator tmxTag;

        if (tagName.equalsIgnoreCase("basefont"))
        {
            tmxTag = getPlaceHolderTag(p_bStart, "font");
        }
        else
        {

            if (p_bStart)
            {
                tmxTag = getPlaceHolderTag(p_bStart,
                        "x-" + tagName.toLowerCase());
            }
            else
            {
                tmxTag = getPlaceHolderTag(p_bStart,
                        "xe-" + tagName.toLowerCase());
            }
        }
        if (p_tag.isFromOfficeContent)
        {
            tmxTag.setFromOfficeContent(true);
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

    protected TmxTagGenerator getPairedTag(boolean p_bStart, String p_strType,
            boolean p_bIsolated)
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
     * <p>
     * Allocates the next internal match id (i) and pushes it on the context
     * stack for the tag with the well-known type p_nType.
     * </p>
     * 
     * <p>
     * Note that B/I/U which have attributes are printed as type "x-bold" (not
     * as "bold"), but they are treated on the stack as their original type.
     * This is so that the corresponding end tag can be associated with the
     * correct start tag.
     * </p>
     * 
     * <p>
     * Forwards to pushInternalId(String).
     * </p>
     */
    protected int pushInternalId(int p_nType)
    {
        return pushInternalId(getType(p_nType));
    }

    public static String getType(String p_nType)
    {
        String strType = p_nType;

        int type = 0;
        try
        {
            type = Integer.parseInt(p_nType);
        }
        catch (Exception e)
        {
            return p_nType;
        }

        switch (type)
        {
            case TmxTagGenerator.C_BOLD:
            case TmxTagGenerator.C_X_BOLD:
            case TmxTagGenerator.X_BOLD:
                strType = String.valueOf(TmxTagGenerator.BOLD);
                break;
            case TmxTagGenerator.C_STRONG:
            case TmxTagGenerator.C_X_STRONG:
            case TmxTagGenerator.X_STRONG:
                strType = String.valueOf(TmxTagGenerator.STRONG);
                break;
            case TmxTagGenerator.C_ITALIC:
            case TmxTagGenerator.C_X_ITALIC:
            case TmxTagGenerator.X_ITALIC:
                strType = String.valueOf(TmxTagGenerator.ITALIC);
                break;
            case TmxTagGenerator.C_UNDERLINE:
            case TmxTagGenerator.C_X_UNDERLINE:
            case TmxTagGenerator.X_UNDERLINE:
                strType = String.valueOf(TmxTagGenerator.UNDERLINE);
                break;
            case TmxTagGenerator.C_EM:
            case TmxTagGenerator.C_X_EM:
            case TmxTagGenerator.X_EM:
                strType = String.valueOf(TmxTagGenerator.EM);
                break;
            default:
                strType = String.valueOf(p_nType);
                break;
        }

        return strType;
    }

    public static String getType(int p_nType)
    {
        String strType = String.valueOf(p_nType);
        return getType(strType);
    }

    /**
     * <p>
     * Allocate the next internal match id (i) and push it on the context stack
     * for the tag p_strType.
     */
    protected int pushInternalId(String p_strType)
    {
        ++m_nNextInternalID;

        String strKey = getType(p_strType.toLowerCase());
        Stack s = (Stack) m_hLastInternalID.get(strKey);
        if (s == null)
        {
            s = new Stack();
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
     * <p>
     * Pop the internal match id (i) from the context stack of the tag
     * p_strType.
     */
    protected int popInternalId(String p_strType)
    {
        String strKey = getType(p_strType.toLowerCase());
        Stack s = (Stack) m_hLastInternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }

        Integer o_id = (Integer) s.pop();

        return o_id.intValue();
    }

    protected int pushExternalId(String p_strType)
    {
        ++m_nNextExternalID;

        String strKey = p_strType.toLowerCase();
        Stack s = (Stack) m_hLastExternalID.get(strKey);
        if (s == null)
        {
            s = new Stack();
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
        Stack s = (Stack) m_hLastExternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }
        Integer o_id = (Integer) s.pop();
        return o_id.intValue();
    }

    protected int peekExternalId(String p_strType)
    {
        String strKey = p_strType.toLowerCase();
        Stack s = (Stack) m_hLastExternalID.get(strKey);
        if (s == null || s.empty())
        {
            return -1;
        }
        Integer o_id = (Integer) s.peek();
        return o_id.intValue();
    }
}
