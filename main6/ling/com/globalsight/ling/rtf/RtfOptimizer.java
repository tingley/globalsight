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
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//
package com.globalsight.ling.rtf;

import java.util.ArrayList;
import java.util.Iterator;

import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;

/**
 * Optimizes the paragraphs and text sequences by collapsing text runs
 * that share the same properties and removing unnecessary RTF controls.
 */
public class RtfOptimizer
{
    //
    // Private Members
    //
    private RtfDocument m_doc;
    private RtfStyleSheet m_stylesheet;

    private RtfTextProperties m_defaultParaProperties;

    private RtfParagraph m_paragraph;
    private RtfTextProperties m_paraProperties;
    private RtfTextProperties m_charProperties;

    private RtfTextPropertiesStack m_charPropertiesStack;

    private RtfCompoundObjectStack m_containerStack;
    private RtfCompoundObject m_container;

    private RtfObject m_current;

    private boolean m_debug = false;

    //
    // Constructors
    //

    /**
     * Default constructor: caller must call init(p_document) to use
     * this object.
     */
    public RtfOptimizer()
    {
    }

    /**
     * Constructor: caller can call optimize() on this object right
     * away and can reuse it by calling init(p_document).
     */
    public RtfOptimizer(RtfDocument p_document)
    {
        m_doc = p_document;

        m_charPropertiesStack = new RtfTextPropertiesStack();
        m_containerStack = new RtfCompoundObjectStack();
    }

    /**
     * Lifecycle management: initializes the object so it can be used.
     */
    public void init(RtfDocument p_document)
    {
        m_doc = p_document;
        m_stylesheet = null;

        // m_defaultParaProperties = null;

        m_paragraph = null;
        m_paraProperties = null;

        m_charProperties = null;
        m_charPropertiesStack = new RtfTextPropertiesStack();

        m_container = null;
        m_containerStack = new RtfCompoundObjectStack();
    }

    /**
     * Lifecycle management: frees all allocated resources. To reuse
     * this object, call init(p_document).
     */
    public void exit()
    {
        m_doc = null;
        m_stylesheet = null;

        // m_defaultParaProperties = null;

        m_paragraph = null;
        m_paraProperties = null;
        m_charProperties = null;
        m_charPropertiesStack = null;
        m_container = null;
        m_containerStack = null;
    }

    //
    // Public Methods
    //

    /**
     * Optimizes the paragraphs and text sequences by collapsing text
     * runs that share the same properties and removing unnecessary
     * RTF controls.
     */
    public RtfDocument optimize()
    {
        m_stylesheet = m_doc.getStyleSheet();

        // RTF files with Wordpad may not have a stylesheet.
        // m_defaultParaProperties = m_stylesheet.getStyle(0).getTextProperties();

        optimizeDocument(m_doc);

        return m_doc;
    }

    //
    // Private Methods
    //

    private void debug(String s)
    {
        System.err.println(s);
    }

    private void optimizeDocument(RtfDocument p_doc)
    {
        RtfCompoundObject container = p_doc;

        optimizeCompound(container);
    }

    private void optimizeCompound(RtfCompoundObject p_object)
    {
        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            RtfObject o = p_object.getObject(i);

            if (o instanceof RtfParagraph)
            {
                optimizeStyles((RtfParagraph)o);
                collapseTextRuns((RtfParagraph)o);
                collapseBookmarks((RtfParagraph)o);
            }
            else if (o instanceof RtfCompoundObject)
            {
                optimizeCompound((RtfCompoundObject)o);
            }
        }
    }

    /**
     * Computes the minimal styles that are in effect for each run of
     * RTF text.
     */
    private void optimizeStyles(RtfParagraph p_para)
    {
        m_paragraph = p_para;

        int style = m_paragraph.getProperties().getStyle();

        if (style >= 0)
        {
            m_paraProperties = m_stylesheet.getEffectiveTextProperties(style);
            // the merge removes superfluous default styles
            mergeProperties(m_paraProperties, new RtfTextProperties());
        }
        else
        {
            m_paraProperties = new RtfTextProperties();
        }

        RtfCompoundObject container = p_para;

        for (int i = 0, max = container.size(); i < max; i++)
        {
            RtfObject o = container.getObject(i);

            if (o instanceof RtfText)
            {
                optimizeStyles((RtfText)o);
            }
            else if (o instanceof RtfMarker)
            {
                optimizeStyles((RtfMarker)o);
            }
            else if (o instanceof RtfSymbol)
            {
                optimizeStyles((RtfSymbol)o);
            }
            else if (o instanceof RtfFootnote)
            {
                if (m_debug) debug("--> Starting footnote");

                RtfTextProperties temp = m_paraProperties;
                m_paraProperties = new RtfTextProperties();

                optimizeCompound((RtfCompoundObject)o);

                m_paraProperties = temp;

                if (m_debug) debug("<-- Ending footnote");
            }
        }
    }

    private void optimizeStyles(RtfText p_text)
    {
        m_charProperties = p_text.getProperties();

        if (m_debug)
        {
            debug("Optimizing Text: " + m_paraProperties +
                " in " + p_text.getData());
            debug("charprops_old: " + m_charProperties);
        }

        mergeProperties(m_charProperties, m_paraProperties);

        if (m_debug)
        {
            debug("charprops_new: " + m_charProperties);
        }
    }

    private void optimizeStyles(RtfMarker p_marker)
    {
        m_charProperties = p_marker.getProperties();

        if (m_debug)
        {
            debug("Optimizing Marker: " + m_paraProperties);
            debug("charprops_old: " + m_charProperties);
        }

        mergeProperties(m_charProperties, m_paraProperties);

        if (m_debug)
        {
            debug("charprops_new: " + m_charProperties);
        }
    }

    private void optimizeStyles(RtfSymbol p_symbol)
    {
        m_charProperties = p_symbol.getProperties();

        if (m_debug)
        {
            debug("Optimizing Symbol: " + m_paraProperties);
            debug("charprops_old: " + m_charProperties);
        }

        mergeProperties(m_charProperties, m_paraProperties);

        if (m_debug)
        {
            debug("charprops_new: " + m_charProperties);
        }
    }

    /**
     * Computes if props need to inherit or overwrite paragraph or
     * style properties. There is a matrix to do this:
     *
     *    |  -  b0  b1 -> para properties
     *====+===========
     * -  |  -  -   -
     * b0 |  -  -   b0
     * b1 |  b1 b1  -
     * |
     * v
     * char properties
     *
     * "-"  means a property is not set
     * "b0" means a property is set to false or the default
     * "b1" means a property is set to true or a non-default value
     */
    private void mergeProperties(RtfTextProperties p_char,
        RtfTextProperties p_para)
    {
        int value;

        // BOOLEAN PROPERTIES

        if (p_char.isBoldSet())
        {
            if (p_char.isBold() == false)
            {
                if (!p_para.isBoldSet() || p_para.isBold() == false)
                {
                    p_char.unsetBold();
                }
            }
            else
            {
                if (p_para.isBoldSet() && p_para.isBold() != false)
                {
                    p_char.unsetBold();
                }
            }
        }

        if (p_char.isItalicSet())
        {
            if (p_char.isItalic() == false)
            {
                if (!p_para.isItalicSet() || p_para.isItalic() == false)
                {
                    p_char.unsetItalic();
                }
            }
            else
            {
                if (p_para.isItalicSet() && p_para.isItalic() != false)
                {
                    p_char.unsetItalic();
                }
            }
        }

        if (p_char.isUnderlinedSet())
        {
            if (p_char.isUnderlined() == false)
            {
                if (!p_para.isUnderlinedSet() || p_para.isUnderlined() == false)
                {
                    p_char.unsetUnderlined();
                }
            }
            else
            {
                if (p_para.isUnderlinedSet() && p_para.isUnderlined() != false)
                {
                    p_char.unsetUnderlined();
                }
            }
        }

        if (p_char.isHiddenSet())
        {
            if (p_char.isHidden() == false)
            {
                if (!p_para.isHiddenSet() || p_para.isHidden() == false)
                {
                    p_char.unsetHidden();
                }
            }
            else
            {
                if (p_para.isHiddenSet() && p_para.isHidden() != false)
                {
                    p_char.unsetHidden();
                }
            }
        }

        if (p_char.isSubscriptSet())
        {
            if (p_char.isSubscript() == false)
            {
                if (!p_para.isSubscriptSet() || p_para.isSubscript() == false)
                {
                    p_char.unsetSubscript();
                }
            }
            else
            {
                if (p_para.isSubscriptSet() && p_para.isSubscript() != false)
                {
                    p_char.unsetSubscript();
                }
            }
        }

        if (p_char.isSuperscriptSet())
        {
            if (p_char.isSuperscript() == false)
            {
                if (!p_para.isSuperscriptSet() || p_para.isSuperscript() == false)
                {
                    p_char.unsetSuperscript();
                }
            }
            else
            {
                if (p_para.isSuperscriptSet() && p_para.isSuperscript() != false)
                {
                    p_char.unsetSuperscript();
                }
            }
        }

        // INTEGER PROPERTIES (with document defaults)
        value = m_doc.getColor();

        if (p_char.isColorSet())
        {
            if (p_char.getColor() == value)
            {
                if (!p_para.isColorSet() || p_para.getColor() == value)
                {
                    p_char.unsetColor();
                }
            }
            else
            {
                if (p_para.isColorSet() &&
                    p_char.getColor() == p_para.getColor())
                {
                    p_char.unsetColor();
                }
            }
        }

        value = m_doc.getBgColor();

        if (p_char.isBgColorSet())
        {
            if (p_char.getBgColor() == value)
            {
                if (!p_para.isBgColorSet() || p_para.getBgColor() == value)
                {
                    p_char.unsetBgColor();
                }
            }
            else
            {
                if (p_para.isBgColorSet() &&
                    p_char.getBgColor() == p_para.getBgColor())
                {
                    p_char.unsetBgColor();
                }
            }
        }

        value = 0 /*m_doc.getCharStyle()*/;

        if (p_char.isCharStyleSet())
        {
            if (p_char.getCharStyle() == value)
            {
                if (!p_para.isCharStyleSet() || p_para.getCharStyle() == value)
                {
                    p_char.unsetCharStyle();
                }
            }
            else
            {
                if (p_para.isCharStyleSet() &&
                    p_char.getCharStyle() == p_para.getCharStyle())
                {
                    p_char.unsetCharStyle();
                }
            }
        }

        value = m_doc.getFont();

        if (p_char.isFontSet())
        {
            if (p_char.getFont() == value)
            {
                if (!p_para.isFontSet() || p_para.getFont() == value)
                {
                    p_char.unsetFont();
                }
            }
            else
            {
                if (p_para.isFontSet() &&
                    p_char.getFont() == p_para.getFont())
                {
                    p_char.unsetFont();
                }
            }
        }

        value = m_doc.getFontSize();

        if (p_char.isFontSizeSet())
        {
            if (p_char.getFontSize() == value)
            {
                if (!p_para.isFontSizeSet() || p_para.getFontSize() == value)
                {
                    p_char.unsetFontSize();
                }
            }
            else
            {
                if (p_para.isFontSizeSet() &&
                    p_char.getFontSize() == p_para.getFontSize())
                {
                    p_char.unsetFontSize();
                }
            }
        }

        value = m_doc.getLang();

        if (p_char.isLangSet())
        {
            if (p_char.getLang() == value)
            {
                if (!p_para.isLangSet() || p_para.getLang() == value)
                {
                    p_char.unsetLang();
                }
            }
            else
            {
                if (p_para.isLangSet() &&
                    p_char.getLang() == p_para.getLang())
                {
                    p_char.unsetLang();
                }
            }
        }
    }

    /**
     * Collapses adjacent text runs with the same text properties into
     * a single text run, and removes empty runs.
     */
    private void collapseTextRuns(RtfParagraph p_para)
    {
        m_paragraph = p_para;

        int style = m_paragraph.getProperties().getStyle();

        if (style >= 0)
        {
            m_paraProperties = m_stylesheet.getEffectiveTextProperties(style);
            // the merge removes superfluous default styles
            mergeProperties(m_paraProperties, new RtfTextProperties());
        }
        else
        {
            m_paraProperties = new RtfTextProperties();
        }

        RtfCompoundObject container = p_para;

        RtfText curr = null;
        RtfText prev = null;

        for (Iterator it = container.getIterator(); it.hasNext(); )
        {
            RtfObject o = (RtfObject)it.next();

            if (o instanceof RtfText)
            {
                curr = (RtfText)o;
                RtfTextProperties properties = curr.getProperties();
                String data = curr.getData();
                if (properties.getColorName().equalsIgnoreCase("default")
                        && data.startsWith("#") && prev == null)
                {
                    curr.setData(data.replaceFirst("#", OfflineConstants.PONUD_SIGN));
                }
                if (prev != null &&
                    prev.getProperties().equals(curr.getProperties()))
                {
                    // merge the 2 and delete current node.
                    prev.setData(prev.getData() + curr.getData());

                    debug("RtfOptimizer: removing current text " +
                        curr.getData());

                    it.remove();
                    continue;
                }

                prev = curr;
            }
            else
            {
                prev = null;
            }
        }
    }

    /**
     * Scans for consecutive starting and ending bookmarks with no
     * intervening text and convertes them into a single location
     * bookmark.
     */
    private void collapseBookmarks(RtfParagraph p_para)
    {
        RtfCompoundObject container = p_para;

        RtfBookmark curr = null;
        RtfBookmark prev = null;

        for (Iterator it = container.getIterator(); it.hasNext(); )
        {
            RtfObject o = (RtfObject)it.next();

            if (o instanceof RtfBookmark)
            {
                curr = (RtfBookmark)o;

                if (prev != null && prev.isStart() && curr.isEnd() &&
                    prev.getName().equals(curr.getName()))
                {
                    // merge the 2 bookmarks
                    prev.setIsLocation();

                    it.remove();
                    continue;
                }

                prev = curr;
            }
            else
            {
                prev = null;
            }
        }
    }
}
