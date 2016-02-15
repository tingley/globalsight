/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.edit.offline.xliff.xliff20;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Data;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Ec;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.File;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Note;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Pc;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Ph;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Sc;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Segment;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Target;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Unit;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Xliff;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.YesNo;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideMapItem;
import com.globalsight.util.StringUtil;

/**
 * Parses standalone Diplomat strings using registered handler.
 */
public class Tmx2Xliff20
{
    private int maxLen;
    private int start;
    private int end;

    private StringBuffer tagName;
    boolean inSub = false;
    private StringBuffer attrName;
    private StringBuffer attrValue;
    private Properties attributes;
    private Tmx2Xliff20Handler handler;
    private static PseudoData data = new PseudoData();

    /**
     * Returns the input string with PTags inserted in place of TMX.
     */
    public List<Object> getResult()
    {
        return handler.getResult();
    }

    /**
     * @return the datas
     */
    public List<Data> getDatas()
    {
        return handler.getDatas();
    }

    /** Allocates needed resources. */
    private void init(String p_tmx)
    {
        if (tagName == null)
        {
            tagName = new StringBuffer(32);
        }

        if (attrName == null)
        {
            attrName = new StringBuffer(32);
        }

        if (attrValue == null)
        {
            attrValue = new StringBuffer(32);
        }

        if (attributes == null)
        {
            attributes = new Properties();
        }

        start = end = 0;

        maxLen = p_tmx.length();

        inSub = false;
    }

    /**
     * Frees used resources. Objects will be garbage-collected if this objectd
     * is free'd.
     */
    private void exit()
    {
        tagName.setLength(0);
        attrName.setLength(0);
        attrValue.setLength(0);
        attributes = new Properties();
    }

    /**
     * Parses a TMX string and generates events for a DiplomatBasicHandler can
     * catch. Decoding the string (if it is in UTF-8, for instance) is not
     * implemented by this class but must be implemented by the handler.
     * 
     * @throws Exception
     * 
     * @see DiplomatBasicHandler
     */
    public void parse(String p_tmx) throws Exception
    {
        init(p_tmx);

        handler.handleStart();

        int i;

        try
        {
            boolean bInTag = false;
            boolean isExtracted = false;
            Stack<Boolean> isInPhStack = new Stack<Boolean>();
            Stack<Boolean> extractedStack = new Stack<Boolean>();
            Stack<String> tagStack = new Stack<String>();
            for (i = 0; i < maxLen; i++)
            {
                char ch = p_tmx.charAt(i);

                if (bInTag)
                {
                    if (ch == '>')
                    {
                        end = i + 1;
                        String subStr = p_tmx.substring(start, end);
                        isExtracted = subStr.indexOf("isTranslate=") != -1;

                        if (subStr.startsWith("<ph "))
                        {
                            int subi = 0;
                            char subCh = subStr.charAt(subi);
                            // read the name...
                            while (subCh != '>' && subCh != '/')
                            {
                                subCh = subStr.charAt(++subi);
                            }

                            if (subCh == '>')
                            {
                                isInPhStack.push(true);
                            }
                        }

                        if (subStr.indexOf("</ph>") != -1
                                && isInPhStack.size() > 0 && isInPhStack.peek())
                        {
                            isInPhStack.pop();
                        }

                        if (subStr.indexOf("<it") != -1)
                        {
                            tagStack.push("start");
                        }

                        if (tagStack.size() > 0
                                && subStr.indexOf("</it>") != -1)
                        {
                            tagStack.pop();
                        }

                        if (isExtracted)
                        {
                            if (subStr.indexOf("<bpt") != -1)
                            {
                                extractedStack.push(isExtracted);
                                tagStack.push("start");
                            }
                            else if (subStr.indexOf("</bpt>") != -1)
                            {
                                tagStack.push("end");
                            }
                        }
                        else
                        {
                            if (extractedStack.size() > 0
                                    && extractedStack.peek())
                            {
                                if (subStr.indexOf("<bpt") != -1)
                                {
                                    tagStack.push("start");
                                }
                                else if (subStr.indexOf("</bpt>") != -1)
                                {
                                    tagStack.push("end");
                                }

                                if (subStr.indexOf("</ept") != -1)
                                {
                                    extractedStack.pop();
                                }
                            }
                            if (tagStack.size() > 0
                                    && subStr.indexOf("<ept") != -1)
                            {
                                tagStack.pop();
                            }

                            if (tagStack.size() > 0
                                    && subStr.indexOf("</ept") != -1)
                            {
                                tagStack.pop();
                            }
                        }

                        processTag(p_tmx, start, end);

                        bInTag = false;
                        start = end = i + 1;
                    }
                }
                else if (ch == '<')
                {
                    end = i;

                    if (start < end)
                    {
                        boolean isInPh = (isInPhStack.size() > 0) ? isInPhStack
                                .peek() : false;
                        String subString = p_tmx.substring(start, end);
                        if (isInPh)
                        {
                            handler.handleText(subString);
                        }
                        else
                        {
                            isExtracted = (extractedStack.size() > 0) ? extractedStack
                                    .peek() : false;
                            String tag = (tagStack.size() > 0) ? tagStack
                                    .peek() : "start";

                            if (tagStack.size() == 0)
                            {
                                tag = "end";
                            }

                            if (isExtracted && "end".equals(tag))
                            {
                                handler.handleIsExtractedText(subString);
                            }
                            else
                            {
                                handler.handleText(subString);
                            }
                        }
                    }

                    bInTag = true;
                    start = end = i;
                }
            }

            if (i > start)
            {
                if (bInTag)
                {
                    throw new Exception("Invalid GXML string `" + p_tmx + "'");
                }
                else
                {
                    end = maxLen;
                    p_tmx = p_tmx.substring(start, end);
                    handler.handleText(p_tmx);
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new Exception("Error parsing GXML: " + e.toString());
        }

        handler.handleStop();

        exit();
    }

    /**
     * Skips over whitespace in the input string.
     * 
     * @return int first index of non-white space following the offset
     */
    private int eatWhitespaces(String p_tmx, int p_index)
    {
        int i = p_index;

        while (i < maxLen && Character.isWhitespace(p_tmx.charAt(i)))
        {
            i++;
        }

        return i;
    }

    /**
     * Reads a tag. Assumes that all atributes are correct. Input is the tag
     * including &lt; and &gt;.
     * 
     * @throws Exception
     */
    private void processTag(String p_tmx, int p_min, int p_max)
            throws Exception
    {
        int i = eatWhitespaces(p_tmx, p_min + 1);
        char ch = p_tmx.charAt(i);

        boolean bEndTag = ch == '/';
        if (bEndTag)
        {
            ch = p_tmx.charAt(++i);
        }

        // read the name...
        while (!Character.isWhitespace(ch) && ch != '>' && ch != '/')
        {
            tagName.append(ch);
            ch = p_tmx.charAt(++i);
        }

        boolean bEmptyTag = ch == '/';
        if (bEmptyTag)
        {
            ch = p_tmx.charAt(++i);
        }

        if (!(bEndTag || bEmptyTag))
        {
            attributes = new Properties();

            // read the attributes...
            while (i < p_max)
            {
                i = eatWhitespaces(p_tmx, i);
                ch = p_tmx.charAt(i);

                // we've consumed all the attributes - only whitespace left
                if (ch == '>' || i >= p_max)
                {
                    break;
                }

                // also break if the element is empty
                if (ch == '/')
                {
                    bEmptyTag = true;
                    ch = p_tmx.charAt(++i);
                    break;
                }

                // read the attribute name...
                while (!Character.isWhitespace(ch) && ch != '=')
                {
                    attrName.append(ch);
                    ch = p_tmx.charAt(++i);
                }

                i = eatWhitespaces(p_tmx, i);
                ch = p_tmx.charAt(i);

                // ... and attribute value
                if (ch == '=')
                {
                    while (ch != '"')
                    {
                        ch = p_tmx.charAt(++i);
                    }

                    ch = p_tmx.charAt(++i);

                    while (ch != '"')
                    {
                        attrValue.append(ch);
                        ch = p_tmx.charAt(++i);
                    }

                    ++i;
                }
                else
                {
                    throw new Exception("Invalid GXML string `" + p_tmx + "'");
                }

                attributes.put(attrName.toString(), attrValue.toString());

                attrName.setLength(0);
                attrValue.setLength(0);
            }
        }

        String tag = tagName.toString();
        tagName.setLength(0);

        if (bEndTag)
        {
            handler.handleEndTag(tag, p_tmx.substring(p_min, p_max));

            if (inSub && "sub".equals(tag))
            {
                inSub = false;
            }
        }
        else if (bEmptyTag)
        {
            // An empty is rare, so we can construct temp strings.
            handler.handleStartTag(tag, attributes,
                    p_tmx.substring(p_min, i - 1) + ">");

            handler.handleEndTag(tag, "</" + tag + ">");
        }
        else
        {
            handler.handleStartTag(tag, attributes,
                    p_tmx.substring(p_min, p_max));

            if ("sub".equals(tag))
            {
                inSub = true;
            }
        }
    }

    /**
     * @return the handler
     */
    public Tmx2Xliff20Handler getHandler()
    {
        return handler;
    }

    /**
     * @param handler
     *            the handler to set
     */
    public void setHandler(Tmx2Xliff20Handler handler)
    {
        this.handler = handler;
    }

    /**
     * Converts xliff 2.0 file to txt file.
     * 
     * The method is used in offline upload.
     * 
     * @param content
     * @return
     */
    public static String conveterToTxt(String content)
    {
        content = content.substring(1);
        Xliff xliff2 = XmlUtil.string2Object(Xliff.class, content);

        StringBuffer sb = new StringBuffer();
        File f = xliff2.getFile().get(0);
        List<Note> notes = f.getNotes().getNote();
        
        // remove the first \r\n in note
        String note = notes.get(0).getContent().substring(1);
        
        // remove content between # Activity Type and # Encoding
        int index = note.indexOf("# Activity Type:");
        int index2 = note.indexOf("# Encoding:");
        if (index > 0 && index2 > 0)
        {
            note = note.substring(0, index) + note.substring(index2);
        }
        
        sb.append(note);
        sb.append("\r\n");
        List<Object> us = f.getUnitOrGroup();
        for (Object o : us)
        {
            Unit u = (Unit) o;
            Segment seg = (Segment) u.getSegmentOrIgnorable().get(0);
            Target t = seg.getTarget();
            String id = seg.getId();
            id = StringUtil.replace(id, ":-", ":[");
            id = StringUtil.replace(id, "-:", "]:");
            sb.append("# ").append(id).append("\r\n");
            getContentAsString(t.getContent(), sb);
            sb.append("\r\n");
        }

        sb.append("\r\n# END GlobalSight Download File");
        return sb.toString();
    }

    /**
     * Changes all tags of xliff 2.0 to txt tag. For example, change &lt;pc&gt;
     * to [g1].
     * 
     * @param cs
     * @param sb
     */
    private static void getContentAsString(List<Object> cs, StringBuffer sb)
    {
        for (Object o : cs)
        {
            if (o instanceof String)
            {
                String s = (String) o;
                s = StringUtil.replace(s, "[", "[[");
                if (s.startsWith("#"))
                {
                    s = s.replaceFirst("#", OfflineConstants.PONUD_SIGN);
                }
                sb.append(s);
            }
            else if (o instanceof Pc)
            {
                Pc pc = (Pc) o;

                // is internal tag
                if (YesNo.NO.equals(pc.getCanDelete()))
                {
                    sb.append("[");
                    getContentAsString(pc.getContent(), sb);
                    sb.append("]");
                }
                else if (pc.getSubType() != null && pc.getSubType().length() > 0)
                {
                    sb.append("[").append(pc.getSubType()).append("]");
                    getContentAsString(pc.getContent(), sb);
                    sb.append("[/").append(pc.getSubType()).append("]");
                }
                else
                {
                    sb.append("[g").append(pc.getId()).append("]");
                    getContentAsString(pc.getContent(), sb);
                    sb.append("[/g").append(pc.getId()).append("]");
                }

            }
            else if (o instanceof Ph)
            {
                Ph ph = (Ph) o;
                sb.append("[x").append(ph.getId()).append("]");
            }
            else if (o instanceof Sc)
            {
                Sc sc = (Sc) o;
                sb.append("[x").append(sc.getId()).append("]");
            }
            else if (o instanceof Ec)
            {
                Ec ec = (Ec) o;
                sb.append("[x").append(ec.getId()).append("]");
            }
        }
    }

    private static String getBptId(Properties p_hAttributes)
    {
        String id = p_hAttributes.getProperty("i");
        
        if (id == null)
        {
            id = p_hAttributes.getProperty("id");
        }
        
        if (id == null)
        {
            id = p_hAttributes.getProperty("x");
        }
        
        return id;
    }
    
    /**
     * Changes gxml tag to txt tag.
     * 
     * The method is used in tag check during offline upload.
     * 
     * @param p_strTmxTagName
     * @param p_hAttributes
     * @param p_strOriginalString
     * @return
     */
    public static String getTag(String p_strTmxTagName,
            Properties p_hAttributes, String p_strOriginalString, Map<String, String> ept2bpt, Map<String, String> ept2bpt2)
    {
        String i = Tmx2Xliff20Handler.getId(p_hAttributes);
        
        if (p_strTmxTagName.equals("bpt"))
        {
            String type = p_hAttributes.getProperty("type");
            if (type != null)
            {
                PseudoOverrideMapItem item = data.getOverrideMapItem(type);
                if (item != null && !item.m_bNumbered)
                {
                    String bId = getBptId(p_hAttributes);
                    ept2bpt2.put(bId, type);
                    return type;
                }
            }
            
            String bId = getBptId(p_hAttributes);
            ept2bpt.put(bId, i);
            
            return "g" + i;
        }
        else if (p_strTmxTagName.equals("ept"))
        {
            if (ept2bpt.containsKey(i))
            {
                String bId = ept2bpt.get(i);
                ept2bpt.remove(i);
                return "/g" + bId;
            }
            
            if (ept2bpt2.containsKey(i))
            {
                return "/" + ept2bpt2.remove(i);
            }
            
            return "/g" + i;
        }
        else if (p_strTmxTagName.equals("ph"))
        {
            return "x" + i;
        }
        else
        {
            return "x" + i;
        }
    }
}
