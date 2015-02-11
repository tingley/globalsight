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
package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;

/**
 * Handler for the embedded HTML contents in office 2010.
 * 
 */
public class OfficeXmlContentPostFilter
{
    private HtmlFilter m_filter = null;
    private Map<String, Object> m_inlineTags = new HashMap<String, Object>();
    private Map<String, Object> m_pairedTags = new HashMap<String, Object>();
    private Map<String, Object> m_translatableAttrs = new HashMap<String, Object>();

    public static final String IS_FROM_OFFICE_CONTENT = OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT;
    public static final String SKELETON_OFFICE_CONTENT_START = OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_START;
    public static final String SKELETON_OFFICE_CONTENT_END = OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_END;

    public OfficeXmlContentPostFilter(HtmlFilter filter)
    {
        m_filter = filter;
        setConfiguration();
    }

    /**
     * Sets configurations for the filter settings.
     */
    private void setConfiguration()
    {
        fillEmbeddableTags();
        fillPairedTags();
        fillTranslatableAttrs();
    }

    private void fillBooleanMap(String value, Map<String, Object> map)
    {
        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String tag = tok.nextToken().trim().toLowerCase();
            map.put(tag, null);
        }
    }

    /**
     * Sets embeddable tags.
     */
    private void fillEmbeddableTags()
    {
        String value = m_filter.getEmbeddableTags();
        fillBooleanMap(value, m_inlineTags);
    }

    /**
     * Sets paired tags.
     */
    private void fillPairedTags()
    {
        String value = m_filter.getPairedTags();
        fillBooleanMap(value, m_pairedTags);
    }

    /**
     * Sets translatable attributes.
     */
    private void fillTranslatableAttrs()
    {
        String value = m_filter.getTranslatableAttributes();
        fillBooleanMap(value, m_translatableAttrs);
    }

    public final boolean isInlineTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return m_inlineTags.containsKey(key);
    }

    public final boolean isPairedTag(String p_tag)
    {
        String key = p_tag.toLowerCase();
        return m_pairedTags.containsKey(key);
    }

    public final boolean isTranslatableAttribute(String p_attr)
    {
        String key = p_attr.toLowerCase();
        return m_translatableAttrs.containsKey(key);
    }

    /**
     * Detects tags from the given content string.
     */
    public List<OfficeXmlContentTag> detectTags(String content)
    {
        String tmp = content;
        StringBuilder sb = new StringBuilder();
        List<OfficeXmlContentTag> tags = new ArrayList<OfficeXmlContentTag>();
        boolean hasTag = false;
        for (int i = 0; i < tmp.length(); i++)
        {
            char c = tmp.charAt(i);
            if (c == '<')
            {
                hasTag = true;
                sb.append(c);
            }
            else if (c == '>' && hasTag)
            {
                sb.append(c);
                OfficeXmlContentTag currentTag = new OfficeXmlContentTag(
                        sb.toString());
                if (currentTag.isEndTag())
                {
                    for (int m = tags.size() - 1; m >= 0; m--)
                    {
                        OfficeXmlContentTag existingTag = tags.get(m);
                        if (existingTag.isStartTag()
                                && !existingTag.isPaired()
                                && existingTag.getName().equalsIgnoreCase(
                                        currentTag.getName()))
                        {
                            existingTag.setPairedTag(currentTag);
                            currentTag.setPairedTag(existingTag);
                            break;
                        }
                    }
                }
                tags.add(currentTag);
                hasTag = false;
                sb = new StringBuilder();
            }
            else if (hasTag)
            {
                sb.append(c);
            }
        }

        return mergeTags(content, tags);
    }

    /**
     * Checks if they are all inline tags in the given content string.
     */
    public boolean isAllTags(String content, List<OfficeXmlContentTag> tags)
    {
        for (OfficeXmlContentTag tag : tags)
        {
            String tagName = tag.getName();
            if (!isInlineTag(tagName))
            {
                // the tag is the text, not a real inline tag
                return false;
            }
            else
            {
                int index = content.indexOf(tag.toString());
                String text = content.substring(0, index);
                content = content.substring(index + tag.toString().length());
                if (Text.isBlank(text))
                {
                    continue;
                }
                else
                {
                    return false;
                }
            }
        }

        return Text.isBlank(content);
    }

    /**
     * Merges the adjacent tags into one.
     */
    private List<OfficeXmlContentTag> mergeTags(String content,
            List<OfficeXmlContentTag> tags)
    {
        List<OfficeXmlContentTag> mergedTags = new ArrayList<OfficeXmlContentTag>(
                tags.size());
        for (ListIterator<OfficeXmlContentTag> it = tags.listIterator(); it
                .hasNext();)
        {
            OfficeXmlContentTag tag = it.next();
            StringBuilder mergedTag = new StringBuilder(tag.toString());
            String mergedTagName = tag.getName();
            boolean oneTag = true;
            if (!isInlineTag(tag.getName()) || hasAttributeToTranslate(tag))
            {
                mergedTags.add(tag);
                continue;
            }
            int tagIndex = content.indexOf(tag.toString());
            content = content.substring(tagIndex + tag.toString().length());

            while (it.hasNext())
            {
                OfficeXmlContentTag nextTag = it.next();
                if (!isInlineTag(nextTag.getName())
                        || hasAttributeToTranslate(nextTag))
                {
                    it.previous();
                    break;
                }
                int nextTagIndex = content.indexOf(nextTag.toString());
                String textBetweenTwoTags = content.substring(0, nextTagIndex);
                if (Text.isBlank(textBetweenTwoTags))
                {
                    // can be merged into one tag
                    mergedTag.append(textBetweenTwoTags);
                    mergedTag.append(nextTag.toString());
                    if (oneTag)
                    {
                        tag.reset();
                    }
                    nextTag.reset();
                    oneTag = false;
                    content = content.substring(nextTagIndex
                            + nextTag.toString().length());
                }
                else
                {
                    it.previous();
                    break;
                }
            }
            if (oneTag)
            {
                mergedTags.add(tag);
            }
            else
            {
                mergedTags.add(new OfficeXmlContentTag(mergedTag.toString(),
                        mergedTagName));
            }
        }

        return mergedTags;
    }

    /**
     * Checks if the given {@link OfficeXmlContentTag} has any attribute to
     * extract for translation.
     */
    private boolean hasAttributeToTranslate(OfficeXmlContentTag tag)
    {
        if (!isInlineTag(tag.getName()))
        {
            return false;
        }
        for (OfficeXmlContentTag.Attribute a : tag.getAttributeList())
        {
            if (isTranslatableAttribute(a.getName()) && a.hasValue())
            {
                return true;
            }
        }
        return false;
    }
}
