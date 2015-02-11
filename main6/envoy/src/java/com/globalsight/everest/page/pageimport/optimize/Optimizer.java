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
package com.globalsight.everest.page.pageimport.optimize;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.page.pageimport.DocsTagUtil;
import com.globalsight.everest.tuv.RemovedPrefixTag;
import com.globalsight.everest.tuv.RemovedSuffixTag;
import com.globalsight.everest.tuv.RemovedTag;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.ling.docproc.extractor.xml.OfficeXmlContentPostFilter;

public abstract class Optimizer
{
    protected static String REGEX_BPT = "<bpt[^>]*i=\"([^\"]*)\"[^>]*>";
    protected static String REGEX_BPT_ALL = "(<bpt[^>]*i=\"{0}\"[^>]*>)[^>]*</bpt>[\\d\\D]*(<ept[^>]*i=\"{0}\"[^>]*>)[^>]*</ept>";
    protected static String REGEX_BPT_ALL2 = "<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>([\\d\\D]*)<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    protected static String REGEX_BPT_ALL3 = "(<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>)([\\d\\D]*)(<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>)";
    protected static String REGEX_BPT_ALL4 = "(<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>\\s*)([\\d\\D]*)(\\s*<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>)";
    protected static String REGEX_BPT_ALL_SPACE = "<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>([ ]*)<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    protected static String REGEX_SAME_TAG = "(<bpt[^>]*>)([^<]*)(</bpt>)([^<]*)(<ept[^>]*>)([^<]*)(</ept>)(<bpt[^>]*>)([^<]*)(</bpt>)([^<]*)(<ept[^>]*>)([^<]*)(</ept>)";
    protected static String REGEX_IT = "\\s*<[pi][^>]*>[^<]*</[pi][^>]*>\\s*";
    protected static String REGEX_IT2 = "(<it[^>]*>)([^<]*)(</it>)";
    protected static String REGEX_TAG = "(<[^be][^>]*>)([^<]*)(</[^>]*>)";
    protected static String REGEX_SEGMENT = "(<segment[^>]*>)([\\d\\D]*?)</segment>";
    protected static String REGEX_PH_AFTER = "(<[^>]*>)([^<]*)(</[^>]*>)<ph[^>]*>([^<]*)</ph>";
    protected static String REGEX_PH_BEFORE = "<ph[^>]*>([^<]*)</ph>(<[^>]*>)([^<]*)(</[^>]*>)";

    private static String REGEX_IT_END = "<it[^>]*pos=\"end\"[^>]*>([^<]*)</it>";
    private static String REGEX_IT_START = "<it[^>]*pos=\"begin\"[^>]*>[^<]*</it>";

    protected static String PRESERVE = "&lt;w:t xml:space=&quot;preserve&quot;&gt;";
    protected static String NO_PRESERVE = "&lt;w:t&gt;";
    protected static String RSIDRPR_REGEX = " w:rsidRPr=&quot;[^&]*&quot;";
    protected static String RSIDR_REGEX = " w:rsidR=&quot;[^&]*&quot;";

    protected abstract boolean accept(String tuDataType, String fileName,
            String pageDataType);

    protected abstract void setGxml(TuvImpl tuv, String gxml, long p_jobId);

    public boolean setGxml(TuvImpl tuv, String gxml, String tuDataType,
            String fileName, String pageDataType, long p_jobId)
    {
        if (accept(tuDataType, fileName, pageDataType))
        {
            setGxml(tuv, gxml, p_jobId);
            return true;
        }

        return false;
    }

    protected boolean isSpaceRemovable(String all, String nextBpt)
    {
        if (all.contains("internal=\"yes\"")
                || all.contains(OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT))
        {
            return false;
        }

        String ulineKey = " u=&quot;sng&quot; ";
        boolean cannotRemove = (nextBpt != null && nextBpt.contains(ulineKey) && !all
                .contains(ulineKey));

        return !cannotRemove;
    }

    /**
     * Merges some tags to one bpt.
     * 
     * @param s
     *            the gxml to update
     * @return new gxml after merging.
     */
    protected String mergeOneBpt(String s)
    {
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile(MessageFormat.format(REGEX_BPT_ALL,
                    m.group(1)));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                RemovedTag tag = extractForOneBpt(all);
                if (tag != null && tag.getTagNum() > 2)
                {
                    String content = tag.getNewStrings().get(0);
                    String bpt = m2.group(1) + tag.getPrefixString() + "</bpt>";
                    String ept = m2.group(2) + tag.getSuffixString() + "</ept>";
                    String newString = bpt + content + ept;
                    s = s.replace(tag.getOrgStrings().get(0), newString);
                    m = p.matcher(s);
                }
            }
        }
        return s;
    }

    /**
     * Gets the content in the bpt string.
     * 
     * @param s
     *            the bpt string
     * @return the content
     */
    private String getContentInOneBpt(String s)
    {
        String regex = "</[^>]*>([^<]+)<[^/]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        List<String> contents = new ArrayList<String>();
        while (m.find())
        {
            contents.add(m.group(1));
        }

        String content = null;

        if (contents.size() == 0)
        {
            return null;
        }

        if (contents.size() == 1)
        {
            content = contents.get(0);
        }

        if (contents.size() > 1)
        {
            String first = contents.get(0);
            String end = contents.get(contents.size() - 1);

            int firstIndex = s.indexOf('>' + first + '<') + 1;
            int endIndex = s.lastIndexOf('>' + end + '<') + 1 + end.length();

            content = s.substring(firstIndex, endIndex);
            if (content.indexOf("<bpt") > 0 || content.indexOf("<ept") > 0)
            {
                content = null;
            }
        }

        return content;
    }

    private boolean isNotExtract(String tag)
    {
        return tag.contains("<sub")
                || tag.contains("internal=\"yes\"")
                || tag.contains(OfficeXmlContentPostFilter.IS_FROM_OFFICE_CONTENT);
    }

    protected RemovedTag extractForOneBpt(String s)
    {
        RemovedTag tag = null;

        String content = getContentInOneBpt(s);

        if (content != null)
        {
            tag = new RemovedTag();
            
            int i = -1;
            
            //for content is &lt;r&gt;
            int index = s.indexOf('>' + content + '<', i);
            while (index > 0 && s.indexOf('>' + content + "</", i) == index)
            {
                i = index + 1;
                index = s.indexOf('>' + content + '<', i);
            }
            
            index++;
            String start = s.substring(0, index);
            if (isNotExtract(start))
            {
                return null;
            }

            String end = s.substring(index + content.length());
            if (isNotExtract(end))
            {
                return null;
            }

            tag.addOrgString(s);
            tag.addNewString(content);
            tag.setPrefixString(getContent(start, tag));
            tag.setSuffixString(getContent(end, tag));
        }

        return tag;
    }

    /**
     * Extracts a removed tag from a bpt fragment.
     * 
     * @param s
     *            a bpt fragment
     * @return the extracted removed tag.
     */
    protected RemovedTag extract(String s)
    {
        RemovedTag tag = null;

        if (s.indexOf("&lt;w:br/&gt;") > 0)
        {
            return tag;
        }

        String regex = "</[^>]*>([^<]+)<[^/]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        List<String> contents = new ArrayList<String>();
        while (m.find())
        {
            contents.add(m.group(1));
        }

        if (contents.size() == 1)
        {
            tag = new RemovedTag();
            String content = contents.get(0);
            int index = s.indexOf('>' + content + '<') + 1;
            String start = s.substring(0, index);
            String end = s.substring(index + content.length());

            tag.addOrgString(s);
            tag.addNewString(content);
            tag.setPrefixString(getContent(start, tag));
            tag.setSuffixString(getContent(end, tag));
        }

        return tag;
    }

    protected String mergeMultiTags(String s)
    {
        String result = s;
        Pattern p = Pattern.compile("<[^>]*>[^<]*</[^>]*>");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String all = m.group();
            int index = result.indexOf(all);
            String temp = result.substring(index);

            String allTags = getAllLinkedTags(temp);
            String mergedTag = mergeLinkedTags(allTags);
            if (!mergedTag.equals(allTags))
            {
                result = result.replace(allTags, mergedTag);
                m = p.matcher(result);
            }
        }

        return result;
    }

    protected String getContent(String s)
    {
        String regex = "<[^>]*>([^<]*)</[^>]*>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        StringBuffer result = new StringBuffer();
        while (m.find())
        {
            result.append(m.group(1));
        }

        return result.toString();
    }

    protected boolean hasContent(String s)
    {
        if (isNotExtract(s))
        {
            return true;
        }

        String regex = "</[^>]*>([^<]+)<[^/]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.find();
    }

    protected boolean isAllTags(String s)
    {
        if (hasContent(s))
        {
            return false;
        }

        String temp = s;
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String i = m.group(1);
            String regex = MessageFormat.format(REGEX_BPT_ALL2, i);
            Pattern p2 = Pattern.compile(regex);
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                String s2 = m2.group(1);
                if (hasContent(all) || !isAllTags(s2))
                {
                    return false;
                }

                temp = temp.replace(all, "");
                m = p.matcher(temp);
            }
        }

        return true;
    }

    protected String getAllLinkedTags(String s)
    {
        StringBuffer sb = new StringBuffer();

        Pattern p = Pattern.compile("^" + REGEX_BPT);
        Pattern p2 = Pattern.compile("^<[^>]*>[^<]*</[^>]*>");

        while (true)
        {
            Matcher m = p.matcher(s);
            if (m.find())
            {
                String i = m.group(1);
                String regex = MessageFormat.format(REGEX_BPT_ALL, i);
                Pattern p3 = Pattern.compile(regex);
                Matcher m2 = p3.matcher(s);

                if (!m2.find())
                {
                    break;
                }

                String all = m2.group();
                if (!isAllTags(all))
                {
                    break;
                }

                sb.append(all);
                s = s.substring(all.length());
            }
            else if (s.startsWith("<ept"))
            {
                break;
            }
            else
            {
                Matcher m2 = p2.matcher(s);

                if (!m2.find())
                {
                    break;
                }

                String all = m2.group();
                if (isNotExtract(all))
                {
                    break;
                }

                sb.append(all);
                s = s.substring(all.length());
            }
        }

        return sb.toString();
    }

    /**
     * Gets a string that contains all content. The tagNum of the
     * <code>tag</code> will be updated.
     * 
     * @param s
     *            the gxml
     * @param tag
     *            the tagNum will be updated
     * @return the string string that contains all content.
     */
    protected String getContent(String s, RemovedTag tag)
    {
        String regex = "<[^>]*>([^<]*)</[^>]*>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        StringBuffer result = new StringBuffer();
        int i = tag.getTagNum();
        while (m.find())
        {
            result.append(m.group(1));
            i++;
        }

        tag.setTagNum(i);
        return result.toString();
    }

    protected String mergeLinkedTags(String s)
    {
        Pattern p = Pattern.compile(REGEX_IT2);
        Matcher m = p.matcher(s);
        if (m.find())
        {
            return m.group(1) + getContent(s) + m.group(3);
        }

        Pattern p2 = Pattern.compile(REGEX_TAG);
        Matcher m2 = p2.matcher(s);
        if (m2.find())
        {
            return m2.group(1) + getContent(s) + m2.group(3);
        }

        return s;
    }

    /**
     * Removes some tags and save the removed tags to database.
     * 
     * @param tuv
     *            the tuv to update
     * @param gxml
     *            the gxml of the tuv
     * @return new gxml of the tuv
     */
    protected String removeTags(TuvImpl tuv, String gxml, long p_jobId)
    {
        List<RemovedTag> removedTags = getTags(gxml);
        if (removedTags.size() > 0)
        {
            boolean flag = false;
            for (RemovedTag tag : removedTags)
            {
                if (!flag)
                {
                    flag = true;
                    TuImpl tu = (TuImpl) tuv.getTu(p_jobId);
                    tu.addRemoveTag(tag);
                    tag.setTu(tu);
                }

                for (int i = 0; i < tag.getOrgStrings().size(); i++)
                {
                    gxml = gxml.replace(tag.getOrgStrings().get(i), tag
                            .getNewStrings().get(i));
                }
            }
        }

        return gxml;
    }

    /**
     * Checks the bpt tag can be removed or not.
     * 
     * @param tag
     *            The tag that will be check.
     * @return
     */
    private boolean isNotRemoveBpt(String all)
    {
        return isNotExtract(all)
                || all.contains("&lt;t xml:space=&quot;preserve&quot;&gt;");
    }

    private boolean isEmbeddedBpt(String all, String s)
    {
        return s.indexOf("</bpt>" + all + "<ept") > 0;
    }

    /**
     * Gets all tags.
     */
    private Map<RemovedTag, Integer> getAllTags(String s)
    {
        Map<RemovedTag, Integer> map = new HashMap<RemovedTag, Integer>();

        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile(MessageFormat.format(REGEX_BPT_ALL,
                    m.group(1)));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                if (isNotRemoveBpt(all) || isEmbeddedBpt(all, s))
                {
                    continue;
                }

                RemovedTag tag = extract(all);

                if (tag != null)
                {
                    Integer size = map.get(tag);
                    if (size == null)
                    {
                        size = 0;
                    }
                    else
                    {
                        for (RemovedTag key : map.keySet())
                        {
                            if (key.equals(tag))
                            {
                                key.mergeString(tag);
                                break;
                            }
                        }
                    }

                    map.put(tag, size + 1);
                }
                s = s.replace(all, "");
                m = p.matcher(s);
            }
        }

        return map;
    }

    /**
     * Recounts the tags.
     * 
     * @param map
     */
    private void margeSameTags(Map<RemovedTag, Integer> map)
    {
        List<RemovedTag> tags = new ArrayList<RemovedTag>(map.keySet());
        for (int i = tags.size() - 1; i >= 0; i--)
        {
            RemovedTag tag = tags.get(i);

            for (int j = i - 1; j >= 0; j--)
            {
                RemovedTag tag2 = tags.get(j);

                if (tag2.sameAs(tag))
                {
                    int n = map.get(tag);
                    map.put(tag2, map.get(tag2) + n);
                    map.put(tag, 0);

                    break;
                }
            }
        }
    }

    /**
     * Picks up the removed tags.
     * 
     * @param map
     * @return List<RemovedTag>
     */
    private List<RemovedTag> pickUpRemovedTags(Map<RemovedTag, Integer> map)
    {
        List<RemovedTag> removedTags = new ArrayList<RemovedTag>();

        int n = 0;
        RemovedTag tagF = null;
        boolean hasSpecalTag = false;

        for (RemovedTag tag : map.keySet())
        {
            // handle special tag
            if (tagF == null)
            {
                n = map.get(tag);
                tagF = tag;
                hasSpecalTag = DocsTagUtil
                        .hasSpecialTag(tagF.getPrefixString());
                continue;
            }

            if (hasSpecalTag
                    && !DocsTagUtil.hasSpecialTag(tag.getPrefixString()))
            {
                n = map.get(tag);
                tagF = tag;
                hasSpecalTag = false;
                continue;
            }

            if (!hasSpecalTag
                    && DocsTagUtil.hasSpecialTag(tag.getPrefixString()))
            {
                continue;
            }

            // Pick up based on the count.
            int num = map.get(tag);

            if (num > n)
            {
                tagF = tag;
                n = map.get(tag);
            }
            else if (num == n && tagF != null)
            {
                int oNum = 0;
                int fNum = 0;

                for (String ss : tag.getOrgStrings())
                {
                    oNum += ss.length();
                }

                for (String ss : tagF.getOrgStrings())
                {
                    fNum += ss.length();
                }

                if (oNum > fNum)
                {
                    tagF = tag;
                }
            }
        }

        if (tagF != null)
        {
            boolean isPreserve = false;

            removedTags.add(tagF);
            for (RemovedTag tag : map.keySet())
            {
                if (tag.sameAs(tagF))
                {
                    removedTags.add(tag);

                    if (tag.getPrefixString().indexOf(PRESERVE) > 0)
                    {
                        isPreserve = true;
                    }
                }
            }

            if (isPreserve)
            {
                String ps = tagF.getPrefixString();
                ps = ps.replace(NO_PRESERVE, PRESERVE);
                tagF.setPrefixString(ps);
            }
        }

        return removedTags;
    }

    /**
     * Gets all tags that will be removed from the gxml.
     * 
     * @param s
     *            the gxml of a tuv.
     * @return ArrayList<RemovedTag>. All tags that will be removed.
     */
    protected List<RemovedTag> getTags(String s)
    {
        // for [it] start
        Pattern pItStart = Pattern.compile(REGEX_IT_START);
        Matcher mItStart = pItStart.matcher(s);

        if (mItStart.find())
        {
            String all = mItStart.group();
            int index = s.indexOf(all);
            String s1 = s.substring(0, index);
            return getTags(s1);
        }

        // for [it] end
        Pattern pIt = Pattern.compile(REGEX_IT_END);
        Matcher mIt = pIt.matcher(s);

        if (mIt.find())
        {
            String all = mIt.group();
            int index = s.indexOf(all);

            String s1 = s.substring(index + all.length());
            return getTags(s1);
        }

        Map<RemovedTag, Integer> map = getAllTags(s);
        margeSameTags(map);
        List<RemovedTag> removedTags = pickUpRemovedTags(map);

        return removedTags;
    }

    protected String removePrefixTag(TuvImpl tuv, String gxml, long p_jobId)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }

        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();

            if (temp.startsWith("<") && !temp.startsWith("<bpt"))
            {
                Pattern p2 = Pattern.compile("^" + REGEX_IT);
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String prefixTag = m2.group();

                    TuImpl tu = (TuImpl) tuv.getTu(p_jobId);
                    RemovedPrefixTag tag = tu.getPrefixTag();
                    if (tag == null)
                    {
                        tag = new RemovedPrefixTag();
                        tag.setTu(tu);
                        tu.setPrefixTag(tag);
                    }

                    String s = tag.getString();
                    if (s == null)
                        s = "";

                    tag.setString(s + prefixTag);
                    content = content.replaceFirst("^" + REGEX_IT, "");
                    gxml = segment + content + "</segment>";
                }
            }
        }

        return gxml;
    }

    protected String removeSuffixTag(TuvImpl tuv, String gxml, long p_jobId)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }

        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();

            if (temp.endsWith(">"))
            {
                Pattern p2 = Pattern.compile("(" + REGEX_IT + ")*$");
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String suffixTag = m2.group();
                    TuImpl tu = (TuImpl) tuv.getTu(p_jobId);

                    RemovedSuffixTag tag2 = tu.getSuffixTag();
                    if (tag2 == null)
                    {
                        tag2 = new RemovedSuffixTag();
                        tag2.setTu(tu);
                        tu.setSuffixTag(tag2);
                    }

                    String s2 = tag2.getString();
                    if (s2 == null)
                        s2 = "";

                    tag2.setString(suffixTag + s2);

                    content = content.replace(suffixTag, "");
                    gxml = segment + content + "</segment>";
                }
            }
        }

        return gxml;
    }

    protected String removePrefixAndSuffixSpace(TuvImpl tuv, String gxml,
            long p_jobId)
    {
        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            if (content.trim().length() != content.length())
            {
                Pattern p1 = Pattern.compile("^(\\s*)(.*?)(\\s*)$");
                Matcher m1 = p1.matcher(content);
                if (m1.find())
                {
                    TuImpl tu = (TuImpl) tuv.getTu(p_jobId);

                    String prifixSpace = m1.group(1);
                    String s = m1.group(2);
                    String suffixSpace = m1.group(3);

                    if (prifixSpace.length() > 0)
                    {
                        RemovedPrefixTag tag = new RemovedPrefixTag();
                        tag.setString(prifixSpace);
                        tu.setPrefixTag(tag);
                    }

                    if (suffixSpace.length() > 0)
                    {
                        RemovedSuffixTag tag = new RemovedSuffixTag();
                        tag.setString(suffixSpace);
                        tu.setSuffixTag(tag);
                    }

                    gxml = segment + s + "</segment>";
                }
            }
        }

        return gxml;
    }

    protected String removeAllPrefixAndSuffixTags(TuvImpl tuv, String g,
            long p_jobId)
    {
        String gxml = removePrefixTag(tuv, g, p_jobId);
        gxml = removeSuffixTag(tuv, gxml, p_jobId);
        gxml = removePrefixAndSuffixTags(tuv, gxml, p_jobId);

        if (!gxml.equals(g))
            return removeAllPrefixAndSuffixTags(tuv, gxml, p_jobId);

        return gxml;
    }

    private String setPrefixAndSuffixTags(Matcher m3, TuImpl tu, String segment)
    {
        String prefixString = m3.group(1);
        String suffixString = m3.group(3);
        String newContent = m3.group(2);

        String gxml = segment + newContent + "</segment>";

        RemovedPrefixTag tag = tu.getPrefixTag();
        if (tag == null)
        {
            tag = new RemovedPrefixTag();
            tag.setTu(tu);
            tu.setPrefixTag(tag);
        }

        String s = tag.getString();
        if (s == null)
            s = "";

        tag.setString(s + prefixString);

        RemovedSuffixTag tag2 = tu.getSuffixTag();
        if (tag2 == null)
        {
            tag2 = new RemovedSuffixTag();
            tag2.setTu(tu);
            tu.setSuffixTag(tag2);
        }

        String s2 = tag2.getString();
        if (s2 == null)
            s2 = "";

        tag2.setString(suffixString + s2);

        return gxml;
    }

    protected String removePrefixAndSuffixTags(TuvImpl tuv, String gxml,
            long p_jobId)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }

        TuImpl tu = (TuImpl) tuv.getTu(p_jobId);
        if (tu.getRemovedTag() != null)
            return gxml;

        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();
            if (temp.startsWith("<"))
            {
                Pattern p2 = Pattern.compile("^" + REGEX_BPT);
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String i = m2.group(1);
                    String m2content = m2.group();
                    if (isNotExtract(m2content))
                    {
                        return gxml;
                    }

                    String regex = MessageFormat.format("^" + REGEX_BPT_ALL4
                            + "$", i);
                    Pattern p3 = Pattern.compile(regex);
                    Matcher m3 = p3.matcher(temp);

                    if (m3.find())
                    {
                        gxml = setPrefixAndSuffixTags(m3, tu, segment);
                    }
                }
            }
        }

        return gxml;
    }

    protected String mergePh(String s)
    {
        s = mergePhAfter(s);
        s = mergePhBefore(s);

        return s;
    }

    protected String mergePhBefore(String s)
    {
        Pattern p = Pattern.compile(REGEX_PH_BEFORE);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String all = m.group();
            int index = s.indexOf(all);
            if (index > -1)
            {
                String content1 = m.group(1);
                String tagStart = m.group(2);
                String content2 = m.group(3);
                String tagEnd = m.group(4);

                String changed = tagStart + content1 + content2 + tagEnd;
                s = s.replace(all, changed);
                m = p.matcher(s);
            }
        }
        return s;
    }

    protected String mergePhAfter(String s)
    {
        Pattern p = Pattern.compile(REGEX_PH_AFTER);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String all = m.group();
            int index = s.indexOf(all);
            if (index > -1)
            {
                String tagStart = m.group(1);
                String content1 = m.group(2);
                String tagEnd = m.group(3);
                String content2 = m.group(4);

                String changed = tagStart + content1 + content2 + tagEnd;
                s = s.replace(all, changed);
                m = p.matcher(s);
            }
        }
        return s;
    }

    /**
     * Removes tags that only includes a space.
     * 
     * @param s
     *            the gxml to update
     * @return new gxml after removing
     */
    protected String removeTagForSpace(String s)
    {
        if (s.contains(" xml:space=&quot;preserve&quot;"))
        {
            return s;
        }

        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            int currentid = -1;
            try
            {
                currentid = Integer.parseInt(m.group(1));
            }
            catch (Exception e)
            {
            }

            String currentRe = MessageFormat.format(REGEX_BPT_ALL_SPACE,
                    currentid);
            Pattern p2 = Pattern.compile(currentRe);
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                // check the attribute from next
                String nextRe = MessageFormat.format(REGEX_BPT_ALL3,
                        currentid + 1);
                Pattern p3 = Pattern.compile(nextRe);
                Matcher m3 = p3.matcher(s);
                String nextBpt = null;
                if (m3.find())
                {
                    nextBpt = m3.group(1);
                }

                if (isSpaceRemovable(all, nextBpt))
                {
                    s = s.replace(all, m2.group(1));
                }
            }
        }

        return s;
    }
}
