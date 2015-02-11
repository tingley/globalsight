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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects.HtmlElement;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * Handler for the office embedded HTML contents.
 * 
 */
public class OfficeContentPostFilterHelper
{
    private ExtractionRules m_rules = null;

    private static final String NEWLINE = "\r\n";

    private Set<Integer> indexHasEmbeddedTag = new HashSet<Integer>();

    // used in gxml translatable element attribute, for export merge
    public static final String IS_FROM_OFFICE_CONTENT = " isFromOfficeContent=\"yes\"";
    // used in gxml skeleton element content, surround the tag that is from
    // office embedded content, for export merge
    public static final String SKELETON_OFFICE_CONTENT_START = "SKELETON-GS-OFFICE-CONTENT-START";
    public static final String SKELETON_OFFICE_CONTENT_END = "SKELETON-GS-OFFICE-CONTENT-END";

    public OfficeContentPostFilterHelper(ExtractionRules rules)
    {
        m_rules = rules;
    }

    /**
     * Walks through a segment list and mark each pairable tag without buddy as
     * isolated. The tags' boolean members m_bPaired and m_bIsolated are false
     * by default.
     */
    private void assignPairingStatus(List<HtmlObjects.HtmlElement> segments)
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>(
                segments);
        Object o1, o2;
        int i_start, i_end, i_max;
        int i_level, i_partner = 1;
        HtmlObjects.Tag t_start, t_tag;
        HtmlObjects.EndTag t_end;
        HtmlObjects.CFTag t_CFstart, t_CFtag;

        i_start = 0;
        i_max = tags.size();
        outer: while (i_start < i_max)
        {
            o1 = tags.get(i_start);

            if (o1 instanceof HtmlObjects.Tag)
            {
                t_start = (HtmlObjects.Tag) o1;

                // don't consider tags that are already closed (<BR/>)
                if (t_start.isClosed)
                {
                    tags.remove(i_start);
                    --i_max;
                    continue outer;
                }

                // handle recursive tags
                i_level = 0;

                // see if the current opening tag has a closing tag
                for (i_end = i_start + 1; i_end < i_max; ++i_end)
                {
                    o2 = tags.get(i_end);

                    if (o2 instanceof HtmlObjects.Tag)
                    {
                        t_tag = (HtmlObjects.Tag) o2;

                        if (t_start.tag.equalsIgnoreCase(t_tag.tag)
                                && t_start.isFromOfficeContent == t_tag.isFromOfficeContent)
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof HtmlObjects.EndTag)
                    {
                        t_end = (HtmlObjects.EndTag) o2;

                        if (t_start.tag.equalsIgnoreCase(t_end.tag)
                                && t_start.isFromOfficeContent == t_end.isFromOfficeContent)
                        {
                            if (i_level > 0)
                            {
                                --i_level;
                                continue;
                            }

                            // found a matching buddy in this segment
                            t_start.isPaired = t_end.isPaired = true;
                            t_start.partnerId = t_end.partnerId = i_partner;
                            i_partner++;
                            tags.remove(i_end);
                            tags.remove(i_start);
                            i_max -= 2;
                            continue outer;
                        }
                    }
                }

                // tag with no buddy - if it requires one, mark as isolated
                if (t_start.isFromOfficeContent ? m_rules
                        .isContentPairedTag(t_start.tag) : m_rules
                        .isPairedTag(t_start.tag))
                {
                    t_start.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (o1 instanceof HtmlObjects.CFTag)
            {
                t_CFstart = (HtmlObjects.CFTag) o1;

                // don't consider tags that are already closed (<BR/>)
                if (t_CFstart.isClosed)
                {
                    tags.remove(i_start);
                    --i_max;
                    continue outer;
                }

                // handle recursive tags
                i_level = 0;

                // see if the current opening tag has a closing tag
                for (i_end = i_start + 1; i_end < i_max; ++i_end)
                {
                    o2 = tags.get(i_end);

                    if (o2 instanceof HtmlObjects.CFTag)
                    {
                        t_CFtag = (HtmlObjects.CFTag) o2;

                        if (t_CFstart.tag.equalsIgnoreCase(t_CFtag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof HtmlObjects.EndTag)
                    {
                        t_end = (HtmlObjects.EndTag) o2;

                        if (t_CFstart.tag.equalsIgnoreCase(t_end.tag))
                        {
                            if (i_level > 0)
                            {
                                --i_level;
                                continue;
                            }

                            // found a matching buddy in this segment
                            t_CFstart.isPaired = t_end.isPaired = true;
                            t_CFstart.partnerId = t_end.partnerId = i_partner;
                            i_partner++;
                            tags.remove(i_end);
                            tags.remove(i_start);
                            i_max -= 2;
                            continue outer;
                        }
                    }
                }

                // tag with no buddy - if it requires one, mark as isolated
                if (t_CFstart.isFromOfficeContent ? m_rules
                        .isContentPairedTag(t_CFstart.tag) : m_rules
                        .isPairedTag(t_CFstart.tag))
                {
                    t_CFstart.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (!(o1 instanceof HtmlObjects.EndTag))
            {
                // don't consider non-tag tags in the list
                tags.remove(i_start);
                --i_max;
                continue outer;
            }

            ++i_start;
        }

        // only isolated begin/end tags are left in the list
        for (i_start = 0; i_start < i_max; ++i_start)
        {
            HtmlObjects.HtmlElement t = (HtmlObjects.HtmlElement) tags
                    .get(i_start);

            t.isIsolated = true;
        }
    }

    /**
     * Clears the pairing status.
     */
    private void clearPairingStatus(List<HtmlObjects.HtmlElement> tags)
    {
        for (int i = 0, max = tags.size(); i < max; i++)
        {
            HtmlObjects.HtmlElement t = tags.get(i);

            t.isIsolated = false;
            t.isPaired = false;
            t.partnerId = -1;
        }
    }

    /**
     * Converts some special span tags to whitespaces.
     */
    private void convertSpecialSpanTags(List<HtmlObjects.HtmlElement> tags)
    {
        for (int i = 0; i < tags.size(); i++)
        {
            HtmlObjects.HtmlElement o = tags.get(i);
            int startIndex = i;
            if (isMsoSpaceRun(o) || isMsoTabCount(o))
            {
                // convert mso-spacerun span and mso-tab-count span to
                // whitespace
                HtmlObjects.Tag t = (HtmlObjects.Tag) o;
                while (i < tags.size())
                {
                    i++;
                    o = tags.get(i);
                    if (o instanceof HtmlObjects.EndTag)
                    {
                        HtmlObjects.EndTag e = (HtmlObjects.EndTag) o;

                        if (t.tag.equalsIgnoreCase(e.tag) && t.isPaired
                                && e.isPaired && t.partnerId == e.partnerId)
                        {
                            tags.subList(startIndex, i + 1).clear();
                            // give it 6 whitespace for the replacement
                            tags.add(startIndex, new HtmlObjects.Text("      "));
                            i = 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetches tags from the given content string.
     */
    private Map<Integer, String> fetchTags(String content)
    {
        String tmp = content;
        StringBuilder sb = new StringBuilder();
        Map<Integer, String> tags = new HashMap<Integer, String>();
        boolean hasTag = false;
        for (int i = 0; i < tmp.length(); i++)
        {
            char c = tmp.charAt(i);
            if (c == '<' && !indexHasEmbeddedTag.contains(i))
            {
                hasTag = true;
                sb.append(c);
            }
            else if (c == '>' && hasTag)
            {
                sb.append(c);
                int index = i + 1 - sb.toString().length();
                tags.put(index, sb.toString());
                hasTag = false;
                sb = new StringBuilder();
            }
            else if (hasTag)
            {
                sb.append(c);
            }
        }

        return tags;
    }

    /**
     * Finds internal text from the given text string and puts into new tag
     * list.
     */
    private void findInternalText(List<InternalText> internalTexts,
            String textString, List<HtmlObjects.HtmlElement> candidateTexts,
            List<HtmlElement> newTags)
    {
        String tmp = textString.replace(NEWLINE + "  ", NEWLINE)
                .replace(NEWLINE + " ", NEWLINE).replace(NEWLINE, " ");
        List<String> internalTextList = InternalTextHelper
                .handleStringWithListReturn(tmp, internalTexts, null);

        boolean hasInternalText = true;
        if (internalTextList.size() == 1 && internalTextList.get(0).equals(tmp))
        {
            // as the new line space issue, try again to get internal text
            tmp = textString.replace(NEWLINE + " ", "");
            internalTextList = InternalTextHelper.handleStringWithListReturn(
                    tmp, internalTexts, null);
            if (internalTextList.size() == 1
                    && internalTextList.get(0).equals(tmp))
            {
                // as the new line space issue, try a third time to get internal
                // text
                tmp = textString.replace(NEWLINE, "");
                internalTextList = InternalTextHelper
                        .handleStringWithListReturn(tmp, internalTexts, null);
                if (internalTextList.size() == 1
                        && internalTextList.get(0).equals(tmp))
                {
                    // finally does not get internal text
                    newTags.addAll(candidateTexts);
                    hasInternalText = false;
                }
            }
        }
        if (hasInternalText)
        {
            // internal text
            for (String text : internalTextList)
            {
                HtmlObjects.HtmlElement element;
                if (text.startsWith(InternalTextHelper.GS_INTERNALT_TAG_START))
                {
                    element = new HtmlObjects.InternalText(text);
                }
                else
                {
                    element = new HtmlObjects.Text(text);
                }
                element.isFromOfficeContent = true;

                newTags.add(element);
            }
        }
    }

    /**
     * Generates an {@link HtmlObjects.ExtendedAttributeList} from the given tag
     * string.
     */
    private HtmlObjects.ExtendedAttributeList generateAttributes(String tag)
    {
        HtmlObjects.ExtendedAttributeList attributeList = new HtmlObjects.ExtendedAttributeList();

        String tmp = tag.replace(NEWLINE, " ").replace("/>", "")
                .replace(">", "");

        String tagName = getTagName(tag);
        // remove the tag name
        tmp = tmp.substring(tmp.indexOf(tagName) + tagName.length());
        if (tmp.trim().isEmpty())
        {
            return attributeList;
        }

        if (!tmp.contains("="))
        {
            // <TAG NAME1 NAME2>
            String[] ss = tmp.split(" ");
            for (String s : ss)
            {
                attributeList.addAttribute(new HtmlObjects.Attribute(s));
            }
            return attributeList;
        }

        while (tmp.contains("="))
        {
            // <TAG NAME1="VALUE1" NAME2="VALUE2">
            int equalIndex = tmp.indexOf("=");
            int spaceIndex = tmp.indexOf(" ");
            if (spaceIndex > -1 && spaceIndex < equalIndex)
            {
                // found an attribute
                String name = tmp.substring(spaceIndex, equalIndex).trim();
                if (name.indexOf(" ") > -1)
                {
                    String[] names = name.split(" ");
                    for (int i = 0; i < names.length - 1; i++)
                    {
                        attributeList.addAttribute(new HtmlObjects.Attribute(
                                names[i]));
                    }
                    name = names[names.length - 1];
                }
                tmp = tmp.substring(equalIndex + 1).trim();
                spaceIndex = tmp.indexOf(" ");
                equalIndex = tmp.indexOf("=");
                String value = "";
                char c = 0;
                if (tmp.length() > 0)
                {
                    c = tmp.charAt(0);
                }
                if (c == '\"' || c == '\'')
                {
                    String s = tmp.substring(1);
                    char c1 = 0;
                    if (tmp.length() > 1)
                    {
                        c1 = tmp.charAt(1);
                    }
                    if (c1 == '\"' || c1 == '\'')
                    {
                        s = tmp.substring(2);
                        String cc = String.valueOf(c) + String.valueOf(c1);
                        if (s.indexOf(cc) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(cc) + 4).trim();
                        }
                        else if (s.indexOf(c) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(c) + 3).trim();
                        }
                        else if (spaceIndex > -1)
                        {
                            value = tmp.substring(0, spaceIndex).trim();
                        }
                        else
                        {
                            value = tmp.trim();
                        }
                    }
                    else
                    {
                        if (s.indexOf(c) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(c) + 2).trim();
                        }
                        else if (spaceIndex > -1)
                        {
                            value = tmp.substring(0, spaceIndex).trim();
                        }
                        else
                        {
                            value = tmp.trim();
                        }
                    }
                }
                else if (spaceIndex == -1 && equalIndex > -1)
                {
                    value = tmp.trim();
                }
                else if (spaceIndex > -1)
                {
                    value = tmp.substring(0, spaceIndex).trim();
                }
                else if (spaceIndex == -1 && equalIndex == -1)
                {
                    value = tmp.trim();
                }

                if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(value))
                {
                    attributeList.addAttribute(new HtmlObjects.Attribute(name,
                            value));
                }
                else if (StringUtil.isEmpty(value))
                {
                    name = name + "=";
                    attributeList.addAttribute(new HtmlObjects.Attribute(name));
                }

                tmp = tmp.substring(tmp.indexOf(value) + value.length());
                if (tmp.length() > 0 && !tmp.contains("="))
                {
                    String[] attrs = tmp.trim().split(" ");
                    for (String attr : attrs)
                    {
                        attributeList.addAttribute(new HtmlObjects.Attribute(
                                attr));
                    }
                }
            }
            else
            {
                // avoid dead loop issue
                break;
            }
        }

        return attributeList;
    }

    /**
     * Generates an {@link HtmlObjects.HtmlElement} object with the given tag
     * string.
     */
    private HtmlObjects.HtmlElement generateHtmlTag(String tag)
    {
        HtmlObjects.HtmlElement element;
        String tagName = getTagName(tag);
        if (tag.startsWith("</"))
        {
            element = new HtmlObjects.EndTag(tagName);
        }
        else
        {
            element = new HtmlObjects.Tag(tagName, generateAttributes(tag),
                    tag.endsWith("/>") ? true : false, tag, 0, 0);
        }
        element.isFromOfficeContent = true;

        return element;
    }

    /**
     * Generates an {@link HtmlObjects.Tag} object with the merged tags.
     */
    private HtmlObjects.Tag generateHtmlTag(String tagName, String mergedTag)
    {
        HtmlObjects.Tag t = new HtmlObjects.Tag(tagName,
                new HtmlObjects.ExtendedAttributeList(), false, mergedTag, 0, 0);

        t.isFromOfficeContent = true;
        t.isMerged = true;

        return t;
    }

    /**
     * Gets the tag name without the <> characters.
     */
    private String getTagName(String tag)
    {
        String tmp = tag.replace(NEWLINE, " ").replace("</", "")
                .replace("<", "").replace("/>", "").replace(">", "").trim();
        if (!tmp.contains(" "))
        {
            return tmp;
        }
        else
        {
            return tmp.substring(0, tmp.indexOf(" "));
        }
    }

    /**
     * Handles special raw HTML tags that are embedded in those from text
     * content.
     */
    private String handleEmbeddedHtmlTags(String content,
            Map<Integer, HtmlObjects.HtmlElement> rawHtmlTags)
    {
        // make sure the replacement has the same length with the original
        // string so that the tag index can be kept.
        String tmp = content;
        Set<Integer> indexes = rawHtmlTags.keySet();
        for (int index : indexes)
        {
            HtmlObjects.HtmlElement rawHtmlTag = rawHtmlTags.get(index);
            String textBeforeTag = content.substring(0, index);
            String textAfterTag = content.substring(index
                    + rawHtmlTag.toString().length());
            if ("<BR>".equalsIgnoreCase(rawHtmlTag.toString()))
            {
                if (isEmbeddedTag(textBeforeTag) > -1)
                {
                    tmp = textBeforeTag + NEWLINE + NEWLINE + textAfterTag;
                }
            }
            else
            {
                // other tags
                while (isEmbeddedTag(textBeforeTag) > -1)
                {
                    int i = isEmbeddedTag(textBeforeTag);
                    indexHasEmbeddedTag.add(i);
                    textBeforeTag = textBeforeTag.substring(0, i);
                }
            }
        }

        return tmp;
    }

    /**
     * Checks if the given {@link HtmlObjects.Tag} has any attribute to extract
     * for translation.
     */
    private boolean hasAttributeToTranslate(HtmlObjects.Tag t)
    {
        for (Iterator<?> it = t.attributes.iterator(); it.hasNext();)
        {
            Object o = it.next();
            if (o instanceof HtmlObjects.Attribute)
            {
                HtmlObjects.Attribute a = (HtmlObjects.Attribute) o;
                if (m_rules.isContentTranslatableAttribute(a.name)
                        && a.hasValue)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if current tag is embedded in another from text content.
     * <p>
     * Returns the index of the parent tag.
     */
    private int isEmbeddedTag(String textBeforeTag)
    {
        String tmp = textBeforeTag;
        for (int i = tmp.length() - 1; i >= 0; i--)
        {
            char c = tmp.charAt(i);
            if (c == '>')
            {
                return -1;
            }
            else if (c == '<')
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the given text is an empty string or string with all nbsp
     * characters.
     */
    private static boolean isEmptyString(String s)
    {
        String tmp = s.replace(NEWLINE, "").replace(" ", "");
        return Text.isBlank(tmp);
    }

    /**
     * Detects series of nbsp that Word converted from tabs during conversion to
     * HTML.
     * <p>
     * Looks like {@code <span style="mso-spacerun:yes"> </span>}.
     */
    private boolean isMsoSpaceRun(HtmlObjects.HtmlElement e)
    {
        if (e instanceof HtmlObjects.Tag)
        {
            HtmlObjects.Tag t = (HtmlObjects.Tag) e;
            if (t.isPaired
                    && !t.isIsolated
                    && t.tag.equalsIgnoreCase("SPAN")
                    && t.attributes.isDefined("style")
                    && t.attributes.getValue("style").startsWith(
                            "mso-spacerun:", 1))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects series of nbsp that Word converted from tabs during conversion to
     * HTML.
     * <p>
     * Looks like {@code <span style='mso-tab-count:1'> </span>}.
     */
    private boolean isMsoTabCount(HtmlObjects.HtmlElement e)
    {
        if (e instanceof HtmlObjects.Tag)
        {
            HtmlObjects.Tag t = (HtmlObjects.Tag) e;
            if (t.isPaired
                    && !t.isIsolated
                    && t.tag.equalsIgnoreCase("SPAN")
                    && t.attributes.isDefined("style")
                    && t.attributes.getValue("style").startsWith(
                            "mso-tab-count:", 1))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current tag belongs to raw HTML tag.
     * <p>
     * Returns the index of the raw HTML tag.
     */
    private int isRawHtmlTag(int index,
            Map<Integer, HtmlObjects.HtmlElement> rawHtmlTags)
    {
        if (rawHtmlTags.containsKey(index))
        {
            return index;
        }
        else
        {
            Set<Integer> keys = rawHtmlTags.keySet();
            for (int i : keys)
            {
                HtmlObjects.HtmlElement e = rawHtmlTags.get(i);
                if (index > i && index < i + e.toString().length())
                {
                    return i;
                }
            }
        }

        // indicates this is not a raw html tag
        return -1;
    }

    /**
     * Checks if the tag is spelling or grammar tag.
     * <p>
     * Looks like {@code <span class=SpellE> </span>}.
     */
    private boolean isSpellingGrammarTag(HtmlObjects.Tag t)
    {
        if (t.isPaired && !t.isIsolated && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");
            if ("SpellE".equals(clazz) || "GramE".equals(clazz))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Links the tags to a string content.
     */
    private List<Object> linkTagsToString(List<HtmlObjects.HtmlElement> tags)
    {
        Map<Integer, HtmlObjects.HtmlElement> rawHtmlTags = new HashMap<Integer, HtmlObjects.HtmlElement>();
        StringBuilder sb = new StringBuilder();
        List<Object> list = new ArrayList<Object>();
        for (ListIterator<HtmlObjects.HtmlElement> it = tags.listIterator(); it
                .hasNext();)
        {
            HtmlObjects.HtmlElement e = it.next();
            if (e instanceof HtmlObjects.Text
                    || e instanceof HtmlObjects.Newline)
            {
                sb.append(e.toString());
            }
            else
            {
                rawHtmlTags.put(sb.toString().length(), e);
                sb.append(e.toString());
            }
        }
        list.add(sb.toString()); // list.get(0)
        list.add(rawHtmlTags); // list.get(1)

        return list;
    }

    /**
     * Merges the adjacent tags into one.
     */
    private List<HtmlObjects.HtmlElement> mergeTags(
            List<HtmlObjects.HtmlElement> tags)
    {
        List<HtmlObjects.HtmlElement> mergedTags = new ArrayList<HtmlObjects.HtmlElement>();
        for (ListIterator<HtmlObjects.HtmlElement> it = tags.listIterator(); it
                .hasNext();)
        {
            HtmlObjects.HtmlElement e = it.next();
            if (e.isFromOfficeContent)
            {
                if (e instanceof HtmlObjects.Tag
                        || e instanceof HtmlObjects.EndTag)
                {
                    String mergedTagName = "";
                    StringBuilder sb = new StringBuilder();
                    boolean oneTag = true;
                    HtmlObjects.HtmlElement theOneTag = e;
                    sb.append(e.toString());
                    if (e instanceof HtmlObjects.Tag)
                    {
                        mergedTagName = ((HtmlObjects.Tag) e).tag;
                        if (hasAttributeToTranslate((HtmlObjects.Tag) e))
                        {
                            // do not check next tag to merge if current tag has
                            // attribute to translate with it
                            mergedTags.add(e);
                            continue;
                        }
                    }
                    if (e instanceof HtmlObjects.EndTag)
                    {
                        mergedTagName = ((HtmlObjects.EndTag) e).tag;
                    }
                    while (it.hasNext())
                    {
                        e = it.next();
                        if (e.isFromOfficeContent)
                        {
                            if (e instanceof HtmlObjects.Tag)
                            {
                                if (hasAttributeToTranslate((HtmlObjects.Tag) e))
                                {
                                    it.previous();
                                    break;
                                }
                                else
                                {
                                    sb.append(e.toString());
                                    oneTag = false;
                                }
                            }
                            else if (e instanceof HtmlObjects.EndTag)
                            {
                                sb.append(e.toString());
                                oneTag = false;
                            }
                            else if (e instanceof HtmlObjects.Text
                                    || e instanceof HtmlObjects.Newline)
                            {
                                if (isEmptyString(e.toString()))
                                {
                                    // has no text content in it, can be merged
                                    sb.append(e.toString());
                                    oneTag = false;
                                }
                                else
                                {
                                    it.previous();
                                    break;
                                }
                            }
                        }
                        else
                        {
                            // raw html tags, do not merge
                            it.previous();
                            break;
                        }
                    }
                    if (oneTag)
                    {
                        // no merge, only one tag
                        mergedTags.add(theOneTag);
                    }
                    else
                    {
                        mergedTags.add(generateHtmlTag(mergedTagName,
                                sb.toString()));
                    }
                }
                else if (e instanceof HtmlObjects.Text
                        || e instanceof HtmlObjects.Newline)
                {
                    mergedTags.add(e);
                }
            }
            else
            {
                // raw html tags or text
                mergedTags.add(e);
            }
        }

        reAssignPairingStatus(mergedTags);

        return mergedTags;
    }

    /**
     * Re-assigns the pairing status to the tag list.
     */
    private void reAssignPairingStatus(List<HtmlObjects.HtmlElement> tags)
    {
        clearPairingStatus(tags);
        assignPairingStatus(tags);
    }

    /**
     * Removes the spelling and grammar tags.
     */
    private void removeSpellingGrammarTags(List<HtmlObjects.HtmlElement> tags)
    {
        for (int i = 0; i < tags.size(); i++)
        {
            HtmlObjects.HtmlElement e = tags.get(i);
            if (e instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) e;
                if (isSpellingGrammarTag(t))
                {
                    tags.remove(i);
                    i--;
                    // go through to find the end tag
                    while (i < tags.size())
                    {
                        i++;
                        e = tags.get(i);
                        if (e instanceof HtmlObjects.EndTag)
                        {
                            HtmlObjects.EndTag et = (HtmlObjects.EndTag) e;

                            if (t.tag.equalsIgnoreCase(et.tag) && t.isPaired
                                    && et.isPaired
                                    && t.partnerId == et.partnerId)
                            {
                                tags.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the leading tags to put into skeleton.
     */
    List<HtmlObjects.HtmlElement> getLeadingTags(
            List<HtmlObjects.HtmlElement> tags)
    {
        List<HtmlObjects.HtmlElement> leadingTags = new ArrayList<HtmlObjects.HtmlElement>();
        while (tags.size() > 0)
        {
            HtmlObjects.HtmlElement e = tags.get(0);
            if (e.isFromOfficeContent)
            {
                if (e instanceof HtmlObjects.Tag
                        || e instanceof HtmlObjects.EndTag)
                {
                    if (!e.isPaired)
                    {
                        leadingTags.add(e);
                        tags.remove(0);
                    }
                    else
                    {
                        break;
                    }
                }
                else if (e instanceof HtmlObjects.Text)
                {
                    if (isEmptyString(e.toString()))
                    {
                        leadingTags.add(e);
                        tags.remove(0);
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return leadingTags;
    }

    /**
     * Gets the trailing tags to put into skeleton.
     */
    List<HtmlObjects.HtmlElement> getTrailingTags(
            List<HtmlObjects.HtmlElement> tags)
    {
        List<HtmlObjects.HtmlElement> trailingTags = new ArrayList<HtmlObjects.HtmlElement>();
        while (tags.size() > 0)
        {
            HtmlObjects.HtmlElement e = tags.get(tags.size() - 1);
            if (e.isFromOfficeContent)
            {
                if (e instanceof HtmlObjects.Tag
                        || e instanceof HtmlObjects.EndTag)
                {
                    if (!e.isPaired)
                    {
                        trailingTags.add(0, e);
                        tags.remove(tags.size() - 1);
                    }
                    else
                    {
                        break;
                    }
                }
                else if (e instanceof HtmlObjects.Text)
                {
                    if (OfficeContentPostFilterHelper.isEmptyString(e
                            .toString()))
                    {
                        trailingTags.add(0, e);
                        tags.remove(tags.size() - 1);
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return trailingTags;
    }

    /**
     * Handles inline tags in office embedded contents.
     */
    List<HtmlObjects.HtmlElement> handleTagsInContent(
            List<HtmlObjects.HtmlElement> tags)
    {
        removeSpellingGrammarTags(tags);
        convertSpecialSpanTags(tags);
        List<Object> list = linkTagsToString(tags);
        String oriString = (String) list.get(0);
        @SuppressWarnings("unchecked")
        Map<Integer, HtmlObjects.HtmlElement> rawHtmlTags = (Map<Integer, HtmlObjects.HtmlElement>) list
                .get(1);
        List<HtmlObjects.HtmlElement> newTagList = new ArrayList<HtmlObjects.HtmlElement>();

        String tmp = handleEmbeddedHtmlTags(oriString, rawHtmlTags);
        // 1. fetch tags from embedded HTML content
        Map<Integer, String> tagMap = fetchTags(tmp);
        List<Integer> tagIndexes = new ArrayList<Integer>();
        tagIndexes.addAll(tagMap.keySet());
        // order the indexes by increasement
        SortUtil.sort(tagIndexes);
        for (Iterator<Integer> it = tagIndexes.iterator(); it.hasNext();)
        {
            int index = it.next();
            String tag = tagMap.get(index);
            String tagName = getTagName(tag);
            if (isRawHtmlTag(index, rawHtmlTags) == -1
                    && !m_rules.isContentInlineTag(tagName))
            {
                // 2. filter out the tags that are set as embedded tag or raw
                // html tag
                it.remove();
                tagMap.remove(index);
            }
        }
        if (tagIndexes.size() > 0)
        {
            // 3. add tags as HtmlObjects related object to new tag list
            HtmlObjects.HtmlElement element;
            Set<Integer> addedRawHtmlTagIndexes = new HashSet<Integer>();
            StringBuilder usedString = new StringBuilder();
            for (int index : tagIndexes)
            {
                String inlineTag = tagMap.get(index);
                int currentIndex = index - usedString.toString().length();
                String textBeforeTag = tmp.substring(0, currentIndex);
                int rawHtmlTagIndex = isRawHtmlTag(index, rawHtmlTags);
                if (textBeforeTag.length() > 0
                        && !addedRawHtmlTagIndexes.contains(rawHtmlTagIndex))
                {
                    // 3.1 add ordinary text content before the tag wrapped
                    // as HtmlObjects.Text to new tag list
                    element = new HtmlObjects.Text(textBeforeTag);
                    element.isFromOfficeContent = true;
                    newTagList.add(element);
                }
                if (rawHtmlTagIndex != -1
                        && !addedRawHtmlTagIndexes.contains(rawHtmlTagIndex))
                {
                    // 3.2 add raw HTML tags back
                    newTagList.add(rawHtmlTags.get(rawHtmlTagIndex));
                    addedRawHtmlTagIndexes.add(rawHtmlTagIndex);
                }
                else if (rawHtmlTagIndex == -1)
                {
                    // 3.3 add the tag as HtmlObjects.EndTag or HtmlObjects.Tag
                    // to new tag list
                    newTagList.add(generateHtmlTag(inlineTag));
                }
                tmp = tmp.substring(currentIndex + inlineTag.length());
                usedString.append(textBeforeTag).append(inlineTag);
            }
            if (tmp.length() > 0)
            {
                // 3.4 add ordinary text content after the tags wrapped as
                // HtmlObjects.Text to new tag list
                element = new HtmlObjects.Text(tmp);
                element.isFromOfficeContent = true;
                newTagList.add(element);
            }
        }
        else
        {
            newTagList = tags;
        }

        reAssignPairingStatus(newTagList);

        return mergeTags(newTagList);
    }

    /**
     * Handles the internal text in office embedded contents.
     */
    List<HtmlObjects.HtmlElement> handleInternalText(
            List<HtmlObjects.HtmlElement> tags)
    {
        if (isAllTags(tags))
        {
            return tags;
        }
        List<InternalText> internalTexts = m_rules.getInternalTextList();

        List<HtmlObjects.HtmlElement> newTags = new ArrayList<HtmlObjects.HtmlElement>();
        List<HtmlObjects.HtmlElement> candidateTexts = new ArrayList<HtmlObjects.HtmlElement>();
        StringBuilder textString = new StringBuilder();

        for (Iterator<HtmlObjects.HtmlElement> it = tags.iterator(); it
                .hasNext();)
        {
            HtmlObjects.HtmlElement e = it.next();
            if (e instanceof HtmlObjects.Text
                    || e instanceof HtmlObjects.Newline)
            {
                candidateTexts.add(e);
                textString.append(e.toString());
            }
            else
            {
                if (candidateTexts.isEmpty())
                {
                    newTags.add(e);
                    continue;
                }
                findInternalText(internalTexts, textString.toString(),
                        candidateTexts, newTags);

                newTags.add(e);
                textString = new StringBuilder();
                candidateTexts = new ArrayList<HtmlObjects.HtmlElement>();
            }
            if (!it.hasNext() && candidateTexts.size() > 0)
            {
                findInternalText(internalTexts, textString.toString(),
                        candidateTexts, newTags);
            }
        }

        return newTags;
    }

    /**
     * Checks if the list of tags are all TAG objects without any text content.
     */
    static boolean isAllTags(List<HtmlObjects.HtmlElement> tags)
    {
        for (HtmlObjects.HtmlElement e : tags)
        {
            if (e instanceof HtmlObjects.Text
                    || e instanceof HtmlObjects.Newline)
            {
                if (!isEmptyString(e.toString()))
                {
                    return false;
                }
            }
            else if (e instanceof HtmlObjects.InternalText)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the tags among the text content to let post-filter handle the
     * content correctly.
     */
    void removeDepressingTags(List<HtmlObjects.HtmlElement> tags)
    {
        for (Iterator<HtmlObjects.HtmlElement> it = tags.iterator(); it
                .hasNext();)
        {
            HtmlObjects.HtmlElement e = it.next();
            if (e instanceof HtmlObjects.Tag || e instanceof HtmlObjects.EndTag)
            {
                it.remove();
            }
        }
    }

    /**
     * Encodes the tag between SKELETON-GS-OFFICE-CONTENT-START and
     * SKELETON-GS-OFFICE-CONTENT-END to make sure some tag is not displaying as
     * raw component.
     * <p>
     * For example, {@code <input type='submit'} may display as a button.
     */
    public static String fixHtmlForSkeleton(String html)
    {
        String start = SKELETON_OFFICE_CONTENT_START;
        String end = SKELETON_OFFICE_CONTENT_END;
        String newHtml = html;
        HtmlEntities converter = new HtmlEntities();
        while (newHtml.indexOf(start) > -1)
        {
            int startIndex = newHtml.indexOf(start);
            int endIndex = newHtml.indexOf(end);
            if (endIndex < 0)
            {
                newHtml = newHtml.substring(0, startIndex)
                        + newHtml.substring(startIndex + start.length());
                continue;
            }
            String encoded = converter.encodeStringBasic(newHtml.substring(
                    startIndex + start.length(), endIndex));
            newHtml = newHtml.substring(0, startIndex) + encoded
                    + newHtml.substring(endIndex + end.length());
        }

        return newHtml;
    }

    public static boolean isOfficeFormat(String mainFormat)
    {
        return IFormatNames.FORMAT_WORD_HTML.equals(mainFormat)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(mainFormat)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(mainFormat)
                || IFormatNames.FORMAT_OFFICE_XML.equals(mainFormat);
    }
}
