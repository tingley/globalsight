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
package com.globalsight.util.edit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.ImageHelper;
import com.globalsight.everest.edit.SegmentProtectionManager;
import com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.tw.internal.ColorInternalTag;
import com.globalsight.util.EmojiUtil;
import com.globalsight.util.Replacer;
import com.globalsight.util.StringUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.TextNode;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Various utility functions for converting GxmlElements to strings.
 */
public class GxmlUtil
{
    private static final Logger CATEGORY = Logger.getLogger(GxmlUtil.class
            .getName());

    static private final String HTML = "html";
    static private final String JHTML = "jhtml";
    static private final String CFM = "cfm";

    static private final String IMAGEPLACEHOLDER = "<img src='/globalsight/images/img.gif' width=30 height=16>";

    static private final Long LONG_ZERO = new Long(0);

    static private final HashMap s_GxmlToHtmlMap;

    private static final String MOVE_TAGS_REGEX_BEGIAN = "<bpt[^>]*i=\"([^\"]*)\"[^>]*>{0}</bpt>";
    private static final String MOVE_TAGS_REGEX_ALL = "<bpt[^>]*i=\"{0}\"[^>]*>{1}</bpt>(.*?)<ept[^>]*i=\"{0}\"[^>]*>{2}</ept>";
    private static Pattern O_PATTERN = Pattern.compile("o:gfxdata=\"[^\\s]+\"");

    static
    {
        s_GxmlToHtmlMap = new HashMap();

        // s_GxmlToHtmlMap.put("link", "U");
        s_GxmlToHtmlMap.put("bold", "b");
        s_GxmlToHtmlMap.put("x-bold", "b");
        s_GxmlToHtmlMap.put("italic", "i");
        s_GxmlToHtmlMap.put("x-italic", "i");
        s_GxmlToHtmlMap.put("ulined", "u");
        s_GxmlToHtmlMap.put("x-ulined", "u");

        // s_GxmlToHtmlMap.put("font", "FONT");

        s_GxmlToHtmlMap.put("x-abbr", "ABBR");
        s_GxmlToHtmlMap.put("x-acronym", "ACRONYM");
        // Should BDO be an erasable ptag?
        // s_GxmlToHtmlMap.put("x-bdo", "BDO");
        s_GxmlToHtmlMap.put("x-big", "BIG");
        s_GxmlToHtmlMap.put("x-blink", "BLINK");
        s_GxmlToHtmlMap.put("x-cite", "CITE");
        s_GxmlToHtmlMap.put("x-code", "CODE");
        s_GxmlToHtmlMap.put("x-dfn", "DFN");
        s_GxmlToHtmlMap.put("x-kbd", "KBD");
        s_GxmlToHtmlMap.put("x-nobr", "NOBR");
        s_GxmlToHtmlMap.put("x-q", "Q");
        s_GxmlToHtmlMap.put("x-s", "S");
        s_GxmlToHtmlMap.put("x-samp", "SAMP");
        s_GxmlToHtmlMap.put("x-small", "SMALL");
        // s_GxmlToHtmlMap.put("x-span", "SPAN");
        s_GxmlToHtmlMap.put("x-strike", "s");
        s_GxmlToHtmlMap.put("x-sub", "sub");
        s_GxmlToHtmlMap.put("x-super", "sup");
        s_GxmlToHtmlMap.put("x-tt", "TT");
        s_GxmlToHtmlMap.put("x-var", "VAR");

        s_GxmlToHtmlMap.put("strong", "strong");
        s_GxmlToHtmlMap.put("x-strong", "strong");
        s_GxmlToHtmlMap.put("em", "e");
        s_GxmlToHtmlMap.put("x-em", "e");

        s_GxmlToHtmlMap.put("c-bold", "B");
        s_GxmlToHtmlMap.put("c-x-bold", "B");
        s_GxmlToHtmlMap.put("c-strong", "STRONG");
        s_GxmlToHtmlMap.put("c-x-strong", "STRONG");
        s_GxmlToHtmlMap.put("c-italic", "I");
        s_GxmlToHtmlMap.put("c-x-italic", "I");
        s_GxmlToHtmlMap.put("c-ulined", "U");
        s_GxmlToHtmlMap.put("c-x-ulined", "U");
        s_GxmlToHtmlMap.put("c-strike", "S");
        s_GxmlToHtmlMap.put("c-sub", "SUB");
        s_GxmlToHtmlMap.put("c-super", "SUP");
        s_GxmlToHtmlMap.put("c-em", "E");
        s_GxmlToHtmlMap.put("c-x-em", "E");
    }

    /**
     * Converts a GXML fragment to an HTML display string. A XML declaration
     * will be automatically prepended to the fragment.
     */
    static public String getDisplayHtml(String p_gxml, String p_dataFormat,
            int p_viewMode)
    {
        String result;
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            result = getDisplayHtml(reader.parseFragment(p_gxml), p_dataFormat,
                    p_viewMode);

            return result;
        }
        catch (GxmlException e)
        {
            return "ERROR";
        }
        finally
        {
            if (reader != null)
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }
    }

    /**
     * <p>
     * Converts a GXML fragment to an HTML display string.
     * </p>
     * 
     * <p>
     * Apparently this is now the function for LIST view.
     * </p>
     * 
     * <p>
     * This is just a wrapper that sets up the recursion context for the worker
     * function.
     * </p>
     */
    static public String getDisplayHtml(GxmlElement p_node,
            String p_dataFormat, int p_viewMode)
    {
        StringBuffer result = new StringBuffer();
        HashMap context = new HashMap();

        getDisplayHtml(p_node, p_dataFormat, context, true, result);

        return result.toString();
    }

    private static boolean isInternalNode(GxmlElement node, HashMap context)
    {
        // type id bpt or ept
        if (node.getType() == GxmlElement.BPT)
        {
            if ("yes".equals(node.getAttribute("internal")))
            {
                String iAttribute = node.getAttribute("i");
                context.put(iAttribute, node);
                return true;
            }
        }
        else
        {
            String iAttribute = node.getAttribute("i");
            GxmlElement bptNode = (GxmlElement) context.get(iAttribute);
            if (bptNode == null)
                return false;

            return "yes".equals(bptNode.getAttribute("internal"));
        }

        return false;
    }

    private static String getInternalDisplayHtml(GxmlElement node)
    {
        // type id bpt or ept
        if (node.getType() == GxmlElement.BPT)
        {
            return ColorInternalTag.COLOR_TAG_PREFIX;
        }

        return ColorInternalTag.COLOR_TAG_SUFFIX;
    }

    /**
     * <p>
     * Converts a GXML fragment to an HTML display string.
     * </p>
     * 
     * <p>
     * This is the recursive worker function.
     * </p>
     */
    static private void getDisplayHtml(GxmlElement p_node, String p_dataFormat,
            HashMap p_context, boolean p_isRoot, StringBuffer p_res)
    {
        if (p_node == null)
        {
            return;
        }

        List childNodes;
        String itemType;
        String iAttribute;
        String htmlTag;
        int nodeType = p_node.getType();

        switch (nodeType)
        {
            case GxmlElement.LOCALIZABLE: // fall through, same as segment
            case GxmlElement.SEGMENT:
                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    Iterator it = childNodes.iterator();

                    while (it.hasNext())
                    {
                        GxmlElement child = (GxmlElement) it.next();
                        boolean isInternalNode = isInternalNode(child,
                                p_context);
                        if (isInternalNode
                                && child.getType() == GxmlElement.BPT)
                        {
                            p_res.append(getInternalDisplayHtml(child));
                        }

                        List childElements = child.getChildElements();
                        if (childElements.size() != 0)
                        {
                            boolean isExtract = false;
                            for (int i = 0; i < childElements.size(); i++)
                            {
                                GxmlElement childElement = (GxmlElement) childElements
                                        .get(i);
                                if (childElement.getChildElements().size() > 0)
                                {
                                    String s = childElement
                                            .getAttribute("isTranslate");
                                    if (s != null && Boolean.parseBoolean(s))
                                    {
                                        isExtract = true;
                                        getDisplayHtml(childElement,
                                                p_dataFormat, p_context, false,
                                                p_res);
                                    }
                                    else
                                    {
                                        isExtract = false;
                                    }
                                }
                                if (isExtract
                                        && childElement.getChildElements()
                                                .size() == 0)
                                {
                                    getDisplayHtml(childElement, p_dataFormat,
                                            p_context, false, p_res);
                                }
                            }
                        }
                        else
                        {
                            getDisplayHtml(child, p_dataFormat, p_context,
                                    false, p_res);
                        }

                        if (isInternalNode
                                && child.getType() == GxmlElement.EPT)
                        {
                            p_res.append(getInternalDisplayHtml(child));
                        }
                    }
                }

                break;

            case GxmlElement.TEXT_NODE:
                p_res.append(EditUtil.encodeHtmlEntities(p_node.getTextValue()));
                break;

            case GxmlElement.BPT:
                itemType = p_node.getAttribute("type");
                iAttribute = p_node.getAttribute("i");

                // store BPT for reference when handling corresponding EPT
                p_context.put(iAttribute, p_node);
                break;

            case GxmlElement.EPT:
                // retrieve corresponding BPT
                iAttribute = p_node.getAttribute("i");
                Object obj = p_context.get(iAttribute);
                if (obj != null)
                {
                    itemType = ((GxmlElement) obj).getAttribute("type");
                }

                break;

            case GxmlElement.PH:
                itemType = p_node.getAttribute("type");

                if (itemType != null)
                {
                    if (itemType.equals("x-nbspace"))
                    {
                        // just a display value, use normal space
                        p_res.append(" ");
                    }
                    else if (itemType.equals("x-img"))
                    {
                        // TODO: output images with correct url.
                        // Use placeholder for now.
                        p_res.append(IMAGEPLACEHOLDER);

                    }
                    else
                    {
                        if (p_dataFormat.equals(HTML))
                        {
                            p_res.append(p_node.getTotalTextValue());
                        }
                        else if (p_dataFormat.equals(JHTML)
                                || p_dataFormat.equals(CFM))
                        {
                            p_res.append(EditUtil.encodeHtmlEntities(p_node
                                    .getTextValue()));
                        }
                    }
                }

                break;

            case GxmlElement.IT:
                // String type = p_node.getAttribute("type");
                // String pos = p_node.getAttribute("pos");
                // p_res.append("<SPAN class=it>[IT]</SPAN>");
                break;

            case GxmlElement.UT:
                // p_res.append("<SPAN class=ut>[UT]</SPAN>");
                break;

            case GxmlElement.SUB:
                if (p_isRoot == true)
                {
                    childNodes = p_node.getChildElements();

                    if (childNodes != null)
                    {
                        Iterator it = childNodes.iterator();

                        while (it.hasNext())
                        {
                            GxmlElement child = (GxmlElement) it.next();

                            getDisplayHtml(child, p_dataFormat, p_context,
                                    false, p_res);
                        }
                    }
                }
                /*
                 * else { p_res.append("<SPAN class=sub>[SUB]</SPAN>"); }
                 */
                break;

            default:
                // could throw exception
                break;
        }
    }

    /**
     * <p>
     * For WB's new preview mode: converts a GXML fragment to an HTML display
     * string.
     * </p>
     * 
     * <p>
     * This is just a wrapper that sets up the recursion context for the worker
     * function.
     * </p>
     */
    static public String getDisplayHtml2(GxmlElement p_node)
    {
        StringBuffer result = new StringBuffer();
        HashMap context = new HashMap();

        getDisplayHtml2(p_node, context, true, result);

        return result.toString();
    }

    /**
     * <p>
     * For WB's new preview mode: converts a GXML fragment to an HTML display
     * string.
     * </p>
     * 
     * <p>
     * This is the recursive worker function.
     * </p>
     */
    static private void getDisplayHtml2(GxmlElement p_node, HashMap p_context,
            boolean p_isRoot, StringBuffer p_res)
    {
        if (p_node == null)
        {
            return;
        }

        List childNodes;
        String itemType;
        String iAttribute;
        String htmlTag;
        int nodeType = p_node.getType();

        switch (nodeType)
        {
            case GxmlElement.LOCALIZABLE: // fall through, same as segment
            case GxmlElement.SEGMENT:
                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    Iterator it = childNodes.iterator();

                    while (it.hasNext())
                    {
                        GxmlElement child = (GxmlElement) it.next();

                        List childElements = child.getChildElements();
                        if (childElements.size() != 0)
                        {
                            boolean isExtract = false;
                            for (int i = 0; i < childElements.size(); i++)
                            {
                                GxmlElement childElement = (GxmlElement) childElements
                                        .get(i);
                                if (childElement.getChildElements().size() > 0)
                                {
                                    String s = childElement
                                            .getAttribute("isTranslate");
                                    if (s != null && Boolean.parseBoolean(s))
                                    {
                                        isExtract = true;
                                        getDisplayHtml2(childElement,
                                                p_context, false, p_res);
                                    }
                                    else
                                    {
                                        isExtract = false;
                                    }
                                }
                                if (isExtract
                                        && childElement.getChildElements()
                                                .size() == 0)
                                {
                                    getDisplayHtml2(childElement, p_context,
                                            false, p_res);
                                }
                            }
                        }
                        else
                        {
                            getDisplayHtml2(child, p_context, false, p_res);
                        }
                    }
                }

                break;

            case GxmlElement.TEXT_NODE:
                p_res.append(EditUtil.encodeHtmlEntities(p_node.getTextValue()));
                break;

            case GxmlElement.BPT:
                itemType = p_node.getAttribute("type");
                iAttribute = p_node.getAttribute("i");

                // store BPT for reference when handling corresponding EPT
                p_context.put(iAttribute, p_node);

                htmlTag = (String) s_GxmlToHtmlMap.get(itemType);

                if (htmlTag != null)
                {
                    p_res.append('<');
                    p_res.append(htmlTag);
                    p_res.append('>');
                }

                break;

            case GxmlElement.EPT:
                // retrieve corresponding BPT
                iAttribute = p_node.getAttribute("i");
                Object obj = p_context.get(iAttribute);
                if (obj != null)
                {
                    itemType = ((GxmlElement) obj).getAttribute("type");

                    htmlTag = (String) s_GxmlToHtmlMap.get(itemType);

                    if (htmlTag != null)
                    {
                        p_res.append("</");
                        p_res.append(htmlTag);
                        p_res.append('>');
                    }
                }

                break;

            case GxmlElement.PH:
                itemType = p_node.getAttribute("type");

                if (itemType.equals("x-nbspace"))
                {
                    // just a display value (could use normal space)
                    p_res.append('\u00a0');
                }
                else if (itemType.equals("x-br"))
                {
                    // just a display value (could use normal space)
                    p_res.append("<BR>");
                }
                else if (p_node.getAttribute("type").equals("x-img"))
                {
                    // TODO: output images with correct url.
                    // Use placeholder for now.
                    p_res.append(IMAGEPLACEHOLDER);
                }

                break;

            case GxmlElement.IT:
                // String type = p_node.getAttribute("type");
                // String pos = p_node.getAttribute("pos");
                // p_res.append("<SPAN class='ptag'>[it]</SPAN>");
                break;

            case GxmlElement.UT:
                // p_res.append("<SPAN class='ptag'>[ut]</SPAN>");
                break;

            case GxmlElement.SUB:
                if (p_isRoot == true)
                {
                    childNodes = p_node.getChildElements();

                    if (childNodes != null)
                    {
                        Iterator it = childNodes.iterator();

                        while (it.hasNext())
                        {
                            GxmlElement child = (GxmlElement) it.next();

                            getDisplayHtml2(child, p_context, false, p_res);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * <p>
     * Converts a GXML fragment to an HTML display string for Preview Mode.
     * </p>
     * 
     * <p>
     * This is just a wrapper that sets up the recursion context for the worker
     * function.
     * </p>
     */
    static public String getDisplayHtmlForPreview(GxmlElement p_node,
            String p_dataFormat, TargetPage p_page, Collection p_imageMaps,
            Long p_tuvId)
    {
        StringBuffer result = new StringBuffer();
        HashMap context = new HashMap();

        getDisplayHtmlForPreview(p_node, p_dataFormat, p_page, p_imageMaps,
                p_tuvId, context, true, result);

        return result.toString();
    }

    /**
     * <p>
     * Converts a GXML fragment to an HTML display string for Preview Mode.
     * </p>
     * 
     * <p>
     * This is the recursive worker function.
     * </p>
     */
    static private void getDisplayHtmlForPreview(GxmlElement p_node,
            String p_dataFormat, TargetPage p_page, Collection p_imageMaps,
            Long p_tuvId, HashMap p_context, boolean p_isRoot,
            StringBuffer p_res)
    {
        if (p_node == null)
        {
            return;
        }

        List childNodes;
        String itemType;
        String iAttribute;
        String htmlTag;
        int nodeType = p_node.getType();

        switch (nodeType)
        {
            case GxmlElement.LOCALIZABLE: // fall through, same as segment
            case GxmlElement.SUB: // subs are embedded segments
            case GxmlElement.SEGMENT:
                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    for (Iterator it = childNodes.iterator(); it.hasNext();)
                    {
                        GxmlElement child = (GxmlElement) it.next();
                        boolean isInternalNode = isInternalNode(child,
                                p_context);
                        if (isInternalNode
                                && child.getType() == GxmlElement.BPT)
                        {
                            p_res.append(getInternalDisplayHtml(child));
                        }

                        List childElements = child.getChildElements();
                        if (childElements.size() > 0)
                        {
                            for (Iterator it1 = childElements.iterator(); it1
                                    .hasNext();)
                            {
                                GxmlElement childElement = (GxmlElement) it1
                                        .next();
                                getDisplayHtmlForPreview(childElement,
                                        p_dataFormat, p_page, p_imageMaps,
                                        p_tuvId, p_context, false, p_res);
                            }
                        }
                        else
                        {
                            getDisplayHtmlForPreview(child, p_dataFormat,
                                    p_page, p_imageMaps, p_tuvId, p_context,
                                    false, p_res);
                        }

                        if (isInternalNode
                                && child.getType() == GxmlElement.EPT)
                        {
                            p_res.append(getInternalDisplayHtml(child));
                        }
                    }
                }

                break;

            case GxmlElement.TEXT_NODE:
                GxmlElement parent = p_node.getParent();

                if (parent != null
                        && (parent.getType() == GxmlElement.LOCALIZABLE || parent
                                .getType() == GxmlElement.SUB)
                        && ImageHelper.isImageItemType(parent
                                .getAttribute("type")))
                {
                    Long subId = LONG_ZERO;

                    if (parent.getType() == GxmlElement.SUB)
                    {
                        subId = new Long(parent.getAttribute("id"));
                    }

                    ImageReplaceFileMap map = getImageMap(p_imageMaps, p_tuvId,
                            subId);

                    ExtractedFile ef = (ExtractedFile) p_page.getPrimaryFile();
                    if (map == null)
                    {
                        // use original image from docs directory
                        p_res.append(disableLinks(ImageHelper
                                .getDisplayImageUrl(p_node.getTextValue(),
                                        WebAppConstants.VIRTUALDIR_CXEDOCS,
                                        ef.getInternalBaseHref(),
                                        ef.getExternalBaseHref())));
                    }
                    else
                    {
                        // use uploaded file
                        p_res.append(disableLinks(ImageHelper.getDisplayImageUrl(
                                map.getTempSourceName(),
                                WebAppConstants.VIRTUALDIR_IMAGE_REPLACE, "",
                                "")));
                    }
                }
                else
                {
                    String nodeV;
                    if (parent != null && (isTagElement(parent)))
                    {
                        nodeV = p_node.getTextValue();
                    }
                    else
                    {
                        nodeV = EditUtil.encodeHtmlEntities(p_node
                                .getTextValue());
                    }

                    p_res.append(disableLinks(nodeV));
                }

                break;

            case GxmlElement.BPT: // fallthru
            case GxmlElement.EPT: // fallthru
            case GxmlElement.PH: // fallthru
            case GxmlElement.IT: // fallthru
            case GxmlElement.UT: // fallthru

                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    for (Iterator it = childNodes.iterator(); it.hasNext();)
                    {
                        GxmlElement child = (GxmlElement) it.next();

                        if (child.getType() == GxmlElement.TEXT_NODE)
                        {
                            p_res.append(disableLinks(child.getTextValue()));
                        }
                        else
                        {
                            getDisplayHtmlForPreview(child, p_dataFormat,
                                    p_page, p_imageMaps, p_tuvId, p_context,
                                    false, p_res);
                        }
                    }
                }

                break;

            default:
                // could throw exception
                break;
        }
    }

    private static boolean isTagElement(GxmlElement element)
    {
        return element.getType() == GxmlElement.BPT
                || element.getType() == GxmlElement.EPT
                || element.getType() == GxmlElement.PH
                || element.getType() == GxmlElement.IT
                || element.getType() == GxmlElement.UT;
    }

    /**
     * <p>
     * Converts a GXML segment (segment or localizable) to an HTML display
     * string for TEXT mode.
     * </p>
     * 
     * <p>
     * This is just a wrapper that sets up the recursion context for the worker
     * function.
     * </p>
     */
    static public String getDisplayHtmlForText(GxmlElement p_node,
            String p_dataFormat, long p_tuId, long p_tuvId,
            String p_matchStyle, Vector p_excludedItemTypes)
    {
        StringBuffer result = new StringBuffer();
        HashMap context = new HashMap();

        getDisplayHtmlForText(p_node, p_dataFormat, p_tuId, p_tuvId,
                p_matchStyle, p_excludedItemTypes, context, result);

        return result.toString();
    }

    /**
     * <p>
     * Converts a GXML segment (segment or localizable) to an HTML display
     * string for TEXT mode.
     * </p>
     * 
     * <p>
     * This is the recursive worker function.
     * </p>
     */
    static private void getDisplayHtmlForText(GxmlElement p_node,
            String p_dataFormat, long p_tuId, long p_tuvId,
            String p_matchStyle, Vector p_excludedItemTypes, HashMap p_context,
            StringBuffer p_res)
    {
        if (p_node == null)
        {
            return;
        }

        List childNodes;
        String subId;
        String itemType;
        int nodeType = p_node.getType();

        switch (nodeType)
        {
            case GxmlElement.LOCALIZABLE: // fall through, same as segment
            case GxmlElement.SEGMENT:
                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    if (nodeType == GxmlElement.SEGMENT)
                    {
                        p_res.append('[');
                    }

                    ListIterator it = childNodes.listIterator();
                    while (it.hasNext())
                    {
                        GxmlElement child = (GxmlElement) it.next();

                        if (child.getType() == GxmlElement.TEXT_NODE)
                        {
                            p_res.append("<A class='");
                            p_res.append(p_matchStyle);
                            p_res.append("' href='javascript:SE(");
                            p_res.append(p_tuId);
                            p_res.append(',');
                            p_res.append(p_tuvId);
                            p_res.append(",0)'>");

                            p_res.append(EditUtil.encodeHtmlEntities(child
                                    .getTextValue()));

                            p_res.append("</A>");
                        }
                        else
                        {
                            getDisplayHtmlForText(child, p_dataFormat, p_tuId,
                                    p_tuvId, p_matchStyle, p_excludedItemTypes,
                                    p_context, p_res);
                        }
                    }

                    if (nodeType == GxmlElement.SEGMENT)
                    {
                        p_res.append(']');
                    }

                }

                break;

            case GxmlElement.TEXT_NODE:
                p_res.append(EditUtil.encodeHtmlEntities(p_node.getTextValue()));
                break;

            case GxmlElement.BPT: // fallthru
            case GxmlElement.EPT: // fallthru
            case GxmlElement.PH: // fallthru
            case GxmlElement.IT: // fallthru
            case GxmlElement.UT: // fallthru

                // GBS-3997&GBS-4066
                if (GxmlElement.PH == nodeType)
                {
                    itemType = p_node.getAttribute(GxmlNames.PH_TYPE);
                    if (itemType != null
                            && itemType.startsWith(EmojiUtil.TYPE_EMOJI))
                    {
                        break;
                    }
                }
                childNodes = p_node.getChildElements();

                if (childNodes != null)
                {
                    ListIterator it = childNodes.listIterator();

                    while (it.hasNext())
                    {
                        GxmlElement child = (GxmlElement) it.next();

                        if (child.getType() == GxmlElement.TEXT_NODE)
                        {
                            p_res.append(EditUtil.encodeHtmlEntities(child
                                    .getTextValue()));
                        }
                        else
                        {
                            getDisplayHtmlForText(child, p_dataFormat, p_tuId,
                                    p_tuvId, p_matchStyle, p_excludedItemTypes,
                                    p_context, p_res);
                        }
                    }
                }

                break;

            case GxmlElement.SUB:
                childNodes = p_node.getChildElements();
                subId = p_node.getAttribute(GxmlNames.SUB_ID);
                itemType = p_node.getAttribute(GxmlNames.SUB_TYPE);

                boolean b_readOnly = SegmentProtectionManager.isTuvExcluded(
                        p_node, itemType, p_excludedItemTypes);

                if (childNodes != null)
                {
                    for (Iterator it = childNodes.iterator(); it.hasNext();)
                    {
                        GxmlElement child = (GxmlElement) it.next();

                        if (child.getType() == GxmlElement.TEXT_NODE)
                        {
                            // subs inherit highlighting from parent

                            if (b_readOnly)
                            {
                                p_res.append("<SPAN class='");
                                p_res.append(p_matchStyle);
                                p_res.append("'>");
                            }
                            else
                            {
                                p_res.append("<A class='");
                                p_res.append(p_matchStyle);
                                p_res.append("' href='javascript:SE(");
                                p_res.append(p_tuId);
                                p_res.append(',');
                                p_res.append(p_tuvId);
                                p_res.append(",");
                                p_res.append(subId);
                                p_res.append(")'>");
                            }

                            p_res.append(EditUtil.encodeHtmlEntities(child
                                    .getTextValue()));

                            if (b_readOnly)
                            {
                                p_res.append("</SPAN>");
                            }
                            else
                            {
                                p_res.append("</A>");
                            }
                        }
                        else
                        {
                            getDisplayHtmlForText(child, p_dataFormat, p_tuId,
                                    p_tuvId, p_matchStyle, p_excludedItemTypes,
                                    p_context, p_res);
                        }
                    }
                }

                break;

            default:
                // could throw exception
                break;
        }
    }

    /**
     * <P>
     * Converts a GxmlElement to its original format that can be displayed in an
     * HTML browser. All text-nodes inside segment/sub are HTML encoded, and all
     * text-nodes inside TMX elements are left as is.
     * </P>
     */
    static public String getOriginalTextInHtml(GxmlElement p_node)
    {
        StringBuffer result = new StringBuffer(128);

        getOriginalTextInHtml(p_node, GxmlElement.NONE, result);

        return result.toString();
    }

    /**
     * <P>
     * Recursive worker function that converts a GxmlElement to its original
     * format that can be displayed in an HTML browser.
     * </P>
     */
    static private void getOriginalTextInHtml(GxmlElement p_node, int p_parent,
            StringBuffer p_result)
    {
        if (p_node.getType() == GxmlElement.TEXT_NODE)
        {
            String value = p_node.getTextNodeValue();

            if (p_parent == GxmlElement.SEGMENT || p_parent == GxmlElement.SUB)
            {
                value = EditUtil.encodeHtmlEntities(value);
            }

            p_result.append(value);
        }
        else
        {
            ListIterator it = p_node.getChildElements().listIterator();
            int type = p_node.getType();

            while (it.hasNext())
            {
                GxmlElement child = (GxmlElement) it.next();

                getOriginalTextInHtml(child, type, p_result);
            }
        }
    }

    /**
     * <P>
     * Converts a GxmlElement to its original format that can be displayed in an
     * HTML browser. All text-nodes inside segment and sub are HTML encoded, and
     * all text-nodes inside TMX elements are left as is.
     * </P>
     * 
     * This version shows images for preview of HTML pages.
     */
    static public String getOriginalTextInHtmlWithImages(GxmlElement p_node,
            SourcePage p_srcPage)
    {
        StringBuffer result = new StringBuffer(128);
        ExtractedFile ef = (ExtractedFile) p_srcPage.getPrimaryFile();

        getOriginalTextInHtmlWithImages(p_node, GxmlElement.NONE,
                ef.getInternalBaseHref(), ef.getExternalBaseHref(),
                p_srcPage.getDataSourceType(), result);

        return result.toString();
    }

    /**
     * <P>
     * Recursive worker function that converts a GxmlElement to its original
     * format that can be displayed in an HTML browser.
     * </P>
     */
    static private void getOriginalTextInHtmlWithImages(GxmlElement p_node,
            int p_parent, String p_int, String p_ext, String p_dataSourceType,
            StringBuffer p_result)
    {
        if (p_node.getType() == GxmlElement.TEXT_NODE)
        {
            String value = p_node.getTextNodeValue();

            if (p_parent == GxmlElement.LOCALIZABLE
                    || p_parent == GxmlElement.SUB)
            {
                GxmlElement parent = p_node.getParent();

                if (parent != null)
                {
                    String itemType = parent.getAttribute("type");

                    if (ImageHelper.isImageItemType(itemType))
                    {
						value = ImageHelper.getDisplayImageUrl(value,
								WebAppConstants.VIRTUALDIR_CXEDOCS, p_int,
								p_ext);
                        p_result.append(value);

                        return;
                    }
                }

            }

            if (p_parent == GxmlElement.SEGMENT || p_parent == GxmlElement.SUB)
            {
                value = EditUtil.encodeHtmlEntities(value);
            }

            p_result.append(value);
        }
        else
        {
            Iterator it = p_node.getChildElements().iterator();
            int type = p_node.getType();

            while (it.hasNext())
            {
                GxmlElement child = (GxmlElement) it.next();

                getOriginalTextInHtmlWithImages(child, type, p_int, p_ext,
                        p_dataSourceType, p_result);
            }
        }
    }

    // Precompile the regular expressions.
    // Use them with RE re = new RE(re1.getProgram(), RE.MATCH_NORMAL);
    // or RE.MATCH_CASEINDEPENDENT.
    static private REProgram re1, re2, re3, re4, re5, re6, re7, re8, re9, re10,
            re11;
    static
    {
        try
        {
            RECompiler compiler = new RECompiler();

            re1 = compiler.compile("<!DOCTYPE\\s*[^>]*>");

            re2 = compiler
                    .compile("<HTML>\\s*(<HEAD>(.|[:space:])*?</HEAD>\\s*)?<BODY(\\s+[^>]*)?>");

            // Allow for tags with : and _ as in <gs:A_DISABLED>. Duh.
            re3 = compiler
                    .compile("(<[A-Z:_]+(\\s+[^>]*)?\\s+)on([A-Z]+=[^>]*>)");

            re4 = compiler.compile("(<IMG(\\s+[^>]*)?\\s+)usemap\\s*=");

            re5 = compiler.compile("<FORM(\\s+|>)");

            re6 = compiler
                    .compile("<SCRIPT(\\s+[^>]*)?>(.|[:space:])*?</SCRIPT>");

            re7 = compiler.compile("\\s+on[a-zA-Z]+\\s*=");

            // An A tag is either <A> or <A attributes...>
            re8 = compiler.compile("<a\\s");
            re9 = compiler.compile("<a>");
            re10 = compiler.compile("</a>");

            // these should be anchored at end
            re11 = compiler.compile("</BODY>|</HTML>");
        }
        catch (RESyntaxException e)
        {
            System.err.println("\n\nREGEX BOOBOO IN GxmlUtil");
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * <P>
     * Disables active elements in a HTML string that would make the editor
     * navigate. Active elements are scripts, onXXX handlers, forms etc.
     * </P>
     * 
     * <P>
     * NOTE: Disabling Links must be done at the segment level before the target
     * page is assembled in order to avoid disabling the segment editor links.
     * Active links are disabled by this.getDisplayHtmlForPreview() which calls
     * this.disableLinks()
     * </P>
     */
    static public String cleanUpDisplayHtml(String p_html)
    {
        String result = p_html;
        // Remove the o:gfxdata value that is very large which causes
        // StackOverFlow error during the match
        result = StringUtil.replaceWithRE(result, O_PATTERN, new Replacer()
        {
            @Override
            public String getReplaceString(Matcher m)
            {
                // TODO Auto-generated method stub
                return "o:gfxdata=\"\"";
            }
        });

        RE re = new RE();

        // me_target.jsp provides a HTML header, BODY and sets up a
        // TABLE context in which we output the page data. If the page
        // data is an entire html page, we need to remove the header
        // and the closing body/html tags at the end and then some
        // more data. We also need to disable links.

        // <!DOCTYPE...> --> ""
        re.setProgram(re1);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "");

        // <HTML>\\s*(<HEAD>.*?</HEAD>\\s*)?<BODY(\\s+[^>]*)?> --> ""
        re.setProgram(re2);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "");

        int index = 0;
        StringBuffer buf = new StringBuffer(result.length() + 256);

        // rename onXXX handlers (<[A-Z]+(\s+[^>]*)?\s+)on([A-Z]+\s*=[^>]*>)
        re.setProgram(re3);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        RE reEvents = new RE(re7, RE.MATCH_CASEINDEPENDENT);
        while (re.match(result, index))
        {
            /* This is wrong - it replaces <a onclick="alert('onto=' + onto)"> */
            buf.append(result.substring(index, re.getParenStart(0)));

            String tagWithEvent = re.getParen(0);
            tagWithEvent = reEvents.subst(tagWithEvent,
                    " _replacedEventHandler=");
            buf.append(tagWithEvent);
            index = re.getParenEnd(0);
        }

        if (buf.length() != 0)
        {
            buf.append(result.substring(index));
            result = buf.toString();
        }

        // rename usemap attribute <IMG usemap=xx>
        re.setProgram(re4);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        index = 0;
        buf.setLength(0);
        while (re.match(result, index))
        {
            buf.append(result.substring(index, re.getParenEnd(1)));
            buf.append("_nousemap=");
            index = re.getParenEnd(0);
        }
        if (buf.length() != 0)
        {
            buf.append(result.substring(index));
            result = buf.toString();
        }

        // add onsubmit=return false to forms (onXXX has been deleted before)
        re.setProgram(re5);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        index = 0;
        buf.setLength(0);
        while (re.match(result, index))
        {
            buf.append(result.substring(index, re.getParenStart(0)));
            buf.append("<FORM onsubmit='return false'");
            index = re.getParenStart(1);
        }
        if (buf.length() != 0)
        {
            buf.append(result.substring(index));
            result = buf.toString();
        }

        // remove all scripts from a page
        re.setProgram(re6);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "");

        // Task: Disable HREFs unless they are for segment links.
        // This must be done at the segment level.
        // See this.getDisplayHtmlForPreview() which calls
        // this.disableLinks().

        // </BODY>|</HTML> --> ""
        re.setProgram(re11);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "");

        return result;
    }

    /**
     * <P>
     * Converts a GxmlElement to a XML string like GxmlElement.toGxml(), but
     * does not add the tag for the parent element itself.
     * </P>
     */
    static public String getInnerXml(GxmlElement p_node)
    {
        StringBuffer result = new StringBuffer();
        List children = p_node.getChildElements();

        if (children != null)
        {
            ListIterator it = p_node.getChildElements().listIterator();

            while (it.hasNext())
            {
                GxmlElement child = (GxmlElement) it.next();

                result.append(child.toGxml());
            }

            return result.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Removes the outer tag from an XML string.
     */
    static public String stripRootTag(String p_xml)
    {
        if (p_xml.startsWith("<"))
        {
            int i_start = p_xml.indexOf('>');
            int i_end = p_xml.lastIndexOf('<');
            int xmlLen = p_xml.length();
            if (i_end == 0 && i_end < i_start && i_start != (xmlLen - 1))
            {
                // indicates this is not an empty tag, like <x1>segment content,
                // then return original.
                return p_xml;
            }

            if (i_end < i_start)
            {
                // indicates this is an empty tag, like <localizable xxxx />,
                // then return empty. GBS-1671.
                return "";
            }
            p_xml = p_xml.substring(i_start + 1, i_end);
        }

        return p_xml;
    }

    /**
     * Replace inner text without root tag change. I.E. <segment
     * segmentId="1">original</segment> to: <segment
     * segmentId="1">current</segment>
     * 
     * @param p_xml
     * @param p_replacing
     * @return
     */
    static public String resetInnerText(String p_xml, String p_replacing)
    {
        StringBuffer result = new StringBuffer();

        if (p_xml.startsWith("<"))
        {
            int i_start = p_xml.indexOf('>');
            int i_end = p_xml.lastIndexOf('<');
            if (i_end < i_start)
            {
                // indicates this is an empty tag, like <localizable xxxx />,
                // then return empty.
                return p_xml;
            }

            String startTag = p_xml.substring(0, i_start + 1);
            // String innerText = p_xml.substring(i_end + 1, i_start);
            String endTag = p_xml.substring(i_end);

            result = result.append(startTag).append(p_replacing).append(endTag);
        }

        return result.toString();
    }

    // Also used by OnlineEditorManagerLocal.
    static public ImageReplaceFileMap getImageMap(Collection p_imageMaps,
            Long p_tuvId, Long p_subId)
    {
        for (Iterator it = p_imageMaps.iterator(); it.hasNext();)
        {
            ImageReplaceFileMap m = (ImageReplaceFileMap) it.next();

            if (m.getTuvIdAsLong().equals(p_tuvId)
                    && m.getSubIdAsLong().equals(p_subId))
            {
                return m;
            }
        }

        return null;
    }

    /**
     * Called by getDisplayHtmlForPreview().
     * 
     * We use this method to disable any links that are in the Tuv content for
     * preview mode. We have to rename the element because otherwise we could
     * have links inside links, with the first closing tag closing the first
     * link (<A>...<A>...</A>...</A>).
     * 
     * The renamed element receives a namespace to make it a custom element with
     * display and behaviour in Internet Explorer.
     * 
     * Note: when the editor is rewritten to use <SPAN> around the segments,
     * this BS becomes irrelevant.
     */
    static private String disableLinks(String p_content)
    {
        String result = p_content;
        RE re = new RE();

        re.setProgram(re8);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "<gs:A_DISABLED ");

        re.setProgram(re9);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "<gs:A_DISABLED>");

        re.setProgram(re10);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        result = re.subst(result, "</gs:A_DISABLED>");

        return result;
    }

    /**
     * Moves all tags (<code>startTag</code> and <code>endTag</code>) from the
     * String <code>src</code>.
     * 
     * <p>
     * Notices that, the method only used to deal with the html that changed
     * from word. Because some other tags are added auto when file change from
     * word to html.
     * 
     * For example. A string <code>&lt;a&gt;only&lt;/a&gt; test</code> in word
     * may change to
     * <code>&lt;bpt i="5" type="x-span" x="5"&gt;&lt;a&gt;&lt;/bpt&gt;only&lt;ept i="5"&gt;&lt;/a&gt;&lt;/ept&gt; test</code>
     * after importing and changing to html. Some attribute(i etc.) may be
     * others. If you want to move &lt;a&gt;&lt;/a&gt; tags. you can use the
     * method as follows. <br>
     * htmlString = GxmlUtil.moveTages(htmlString,"&lt;a&gt;","&lt;/a&gt;");
     * 
     * <p>
     * The tags will be moved only when the <code>startTag</code> and
     * <code>endTag</code> all exist.
     * 
     * @param src
     *            The string changed from word.
     * @param startTag
     *            The start tag.
     * @param endTag
     *            The end tag.
     * @return
     */
    static public String moveTages(String src, String startTag, String endTag)
    {
        String matchText = MessageFormat.format(MOVE_TAGS_REGEX_BEGIAN,
                new String[]
                { startTag });
        Pattern p = Pattern.compile(matchText);
        Matcher m = p.matcher(src);
        HashSet ids = new HashSet();
        while (m.find())
        {
            ids.add(m.group(1));
        }

        Iterator iterator = ids.iterator();
        while (iterator.hasNext())
        {
            String id = (String) iterator.next();
            matchText = MessageFormat.format(MOVE_TAGS_REGEX_ALL, new String[]
            { id, startTag, endTag });
            p = Pattern.compile(matchText);

            src = StringUtil.replaceWithRE(src, Pattern.compile(matchText),
                    new Replacer()
                    {
                        @Override
                        public String getReplaceString(Matcher m)
                        {
                            return m.group(1);
                        }
                    });
        }

        return src;
    }

    /**
     * Check if the GxmlElement is completely composed of protection text
     * (internal text).
     * <p>
     * If yes, this segment does not need translate, in protection.
     * </p>
     * 
     * @param p_rootElement
     * @return true|false
     */
    public static boolean isEntireInternalText(GxmlElement p_rootElement)
    {
        if (p_rootElement == null)
        {
            return false;
        }

        List<GxmlElement> allNotInternalTextNodes = new ArrayList<GxmlElement>();
        // Immediate TextNode list for the root element
        allNotInternalTextNodes.addAll(p_rootElement
                .getTextNodeWithoutInternal());
        // Add immediate TextNode list for all sub GxmlElement.
        List<GxmlElement> subFlowList = p_rootElement
                .getDescendantElements(GxmlElement.SUB_TYPE);
        if (subFlowList != null && subFlowList.size() > 0)
        {
            for (GxmlElement subEle : subFlowList)
            {
                allNotInternalTextNodes.addAll(subEle
                        .getTextNodeWithoutInternal());
            }
        }

        if (allNotInternalTextNodes.size() == 0)
            return true;

        for (GxmlElement ele : allNotInternalTextNodes)
        {
            String textValue = ((TextNode) ele).getTextNodeValue();
            if (textValue != null && textValue.trim().length() > 0)
            {
                return false;
            }
        }

        return true;
    }

    //
    // Test code
    //

    /*
     * static public void main(String[] argv) throws Exception { if (argv.length
     * != 2) { System.err.println("GxmlUtil file java_encoding");
     * System.exit(1); }
     * 
     * String s = readString(argv[0], argv[1]);
     * 
     * String result = GxmlUtil.cleanUpDisplayHtml(s);
     * 
     * System.out.println("Result of cleanUpDisplayHtml:");
     * System.out.println(result);
     * 
     * System.exit(0); }
     * 
     * private static String readString(String p_fileName, String p_encoding)
     * throws IOException { File f = new File (p_fileName); byte[] a_bytes = new
     * byte [(int)f.length()]; FileInputStream r = new FileInputStream(f);
     * r.read(a_bytes, 0, a_bytes.length);
     * 
     * return new String (a_bytes, p_encoding); }
     */
}
