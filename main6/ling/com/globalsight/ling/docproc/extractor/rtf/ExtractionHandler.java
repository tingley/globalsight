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

import com.globalsight.ling.docproc.extractor.rtf.Extractor;

import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfAnnotation;
import com.globalsight.ling.rtf.RtfAnnotationBookmark;
import com.globalsight.ling.rtf.RtfBookmark;
import com.globalsight.ling.rtf.RtfCell;
import com.globalsight.ling.rtf.RtfColorTable;
import com.globalsight.ling.rtf.RtfCompoundObject;
import com.globalsight.ling.rtf.RtfControl;
import com.globalsight.ling.rtf.RtfData;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.rtf.RtfFieldInstance;
import com.globalsight.ling.rtf.RtfFontTable;
import com.globalsight.ling.rtf.RtfFootnote;
import com.globalsight.ling.rtf.RtfHyperLink;
import com.globalsight.ling.rtf.RtfLineBreak;
import com.globalsight.ling.rtf.RtfMarker;
import com.globalsight.ling.rtf.RtfObject;
import com.globalsight.ling.rtf.RtfPageBreak;
import com.globalsight.ling.rtf.RtfParagraph;
import com.globalsight.ling.rtf.RtfPicture;
import com.globalsight.ling.rtf.RtfRow;
import com.globalsight.ling.rtf.RtfShape;
import com.globalsight.ling.rtf.RtfShapePicture;
import com.globalsight.ling.rtf.RtfShapeText;
import com.globalsight.ling.rtf.RtfStyleSheet;
import com.globalsight.ling.rtf.RtfSymbol;
import com.globalsight.ling.rtf.RtfTab;
import com.globalsight.ling.rtf.RtfText;
import com.globalsight.ling.rtf.RtfTextProperties;
import com.globalsight.ling.rtf.RtfVariable;
import com.globalsight.ling.rtf.RtfVariables;

import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.RtfEnDecoder;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TmxTagGenerator;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 */
public class ExtractionHandler
    implements IFormatNames
{
    //
    // Private Member Variables
    //
    static private boolean m_debug = true;

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;

    private XmlEntities m_xmlEncoder = new XmlEntities ();
    private RtfEnDecoder m_codec = new RtfEnDecoder();
    private Rtf2TmxMap m_tmxmap = new Rtf2TmxMap();

    private RtfDocument m_document = null;

    /** Flag when outputting in TRANSLATABLE section or SKELETON */
    private boolean b_inSkeleton = true;
    /** Flag when outputting in TMX tag section or TRANSLATABLE */
    private boolean b_inTag = false;

    //
    // Constructors
    //

    /**
     * <P>Returns a new handler that knows about its input data, the
     * output object, and the extractor that created it.<P>
     */
    ExtractionHandler (EFInputData p_input, Output p_output,
        Extractor p_extractor)
        throws ExtractorException
    {
        super();

        m_input = p_input;
        m_output = p_output;
        m_extractor = p_extractor;
    }

    public void extractDocument(RtfDocument p_doc)
        throws Exception
    {
        m_tmxmap.reset();
        m_document = p_doc;
        b_inSkeleton = true;

        extractHeader(p_doc);

        extractFonttable(p_doc);
        extractColortable(p_doc);
        extractStylesheet(p_doc);
        extractEntities(p_doc);

        // Other destinations before text.
        // ...

        extractGenerator(p_doc);
        extractInfo(p_doc);
        extractVariables(p_doc);

        extractParagraphs(p_doc);
        extractTrailer(p_doc);

        m_document = null;
    }

    //
    // Private Methods
    //

    protected void debug(String p_text)
    {
        System.err.println(p_text);
    }

    /**
     * Forces output to a (new) skeleton section.
     */
    protected void addSkeleton(String p_text)
    {
        m_output.addSkeleton(p_text);
        b_inSkeleton = true;
    }

    /**
     * Forces output to a (new) translatable.
     */
    protected void addTranslatable(String p_text)
    {
        m_output.addTranslatable(p_text);
        b_inSkeleton = false;
    }

    /**
     * Forces output to a (new) translatable, without converting
     * special XML chars (&lt;,&gt;,&amp;).
     */
    protected void addTranslatableTmx(String p_text)
    {
        m_output.addTranslatableTmx(p_text);
        b_inSkeleton = false;
    }

    /**
     * Outputs text to the current context - skeleton or translatable.
     */
    protected void addToCurrent(String p_text)
    {
        if (b_inSkeleton)
        {
            m_output.addSkeleton(p_text);
        }
        else
        {
            m_output.addTranslatable(p_text);
        }
    }

    //
    // Extraction Methods
    //

    /**
     * Recreates the necessary header information to re-create the
     * document in a new language: fonttable, stylesheet, colortable,
     * listtable, info.
     */
    private void extractHeader(RtfDocument p_doc)
        throws Exception
    {
        addSkeleton("{\\rtf1\\ansi\\ansicpg");
        addSkeleton(String.valueOf(p_doc.getCodepage()));
        addSkeleton("\\deflang");
        addSkeleton(String.valueOf(p_doc.getLang()));
        addSkeleton("\\deflangfe");
        addSkeleton(String.valueOf(p_doc.getFeLang()));
        addSkeleton("\\deff");
        addSkeleton(String.valueOf(p_doc.getFont()));
        // We write out Unicode chars with no ANSI replacements.
        addSkeleton("\\uc0");
        //addSkeleton(String.valueOf(p_doc.getSkipCount()));
        addSkeleton("\n");
    }

    private void extractFonttable(RtfDocument p_doc)
        throws Exception
    {
        m_output.addSkeleton("{\\fonttbl\n");

        for (Iterator it = p_doc.getFontTable().getFontDefinitions();
             it.hasNext(); )
        {
            RtfFontTable.FontDefinition font =
                (RtfFontTable.FontDefinition)it.next();

            addSkeleton("{\\f");
            addSkeleton(String.valueOf(font.getCode()));
            addSkeleton(font.getFontProperties().toRtf());
            addSkeleton("}\n");
        }

        addSkeleton("}\n");
    }

    private void extractColortable(RtfDocument p_doc)
        throws Exception
    {
        addSkeleton("{\\colortbl");

        int i = 0;
        for (Iterator it = p_doc.getColorTable().getColorDefinitions();
             it.hasNext(); i++)
        {
            RtfColorTable.ColorDefinition color =
                (RtfColorTable.ColorDefinition)it.next();

            addSkeleton(color.toRtf());

            if (i % 3 == 0)
            {
                addSkeleton("\n");
            }
        }

        addSkeleton("}\n");
    }

    private void extractStylesheet(RtfDocument p_doc)
        throws Exception
    {
        addSkeleton("{\\stylesheet\n");

        for (Iterator it = p_doc.getStyleSheet().getStyleDefinitions();
             it.hasNext(); )
        {
            RtfStyleSheet.StyleDefinition style =
                (RtfStyleSheet.StyleDefinition)it.next();

            addSkeleton("{");

            if (p_doc.getStyleSheet().isCharacterStyle(style))
            {
                addSkeleton("\\*\\cs");
                addSkeleton(String.valueOf(style.getCode()));

                if (style.isAdditive())
                {
                    addSkeleton(" \\additive ");
                }
            }
            else if (style.getCode() > 0)
            {
                addSkeleton("\\s");
                addSkeleton(String.valueOf(style.getCode()));
            }

            addSkeleton(style.getTextProperties().toRtf());

            if (style.getNext() >= 0)
            {
                addSkeleton("\\snext");
                addSkeleton(String.valueOf(style.getNext()));
            }

            if (style.getBasedOn() >= 0)
            {
                addSkeleton("\\sbasedon");
                addSkeleton(String.valueOf(style.getBasedOn()));
            }

            addSkeleton(" ");
            addSkeleton(style.getName());

            addSkeleton(";}\n");
        }

        addSkeleton("}\n");
    }

    private void extractEntities(RtfDocument p_doc)
        throws Exception
    {
        //addSkeleton("{\\entities@@@@@}\n");
    }

    /**
     * Leaves a note in the RTF file that it was automatically
     * generated by GlobalSight.
     */
    private void extractGenerator(RtfDocument p_doc)
        throws Exception
    {
        addSkeleton("{\\*\\generator ");
        addSkeleton(p_doc.VERSION_NAME);
        addSkeleton(" ");
        addSkeleton(p_doc.VERSION_NUMBER);
        addSkeleton(";}\n");
    }

    private void extractInfo(RtfDocument p_doc)
        throws Exception
    {
        if (p_doc.getInfo() != null)
        {
            addSkeleton("{\\info\n");
            addSkeleton(p_doc.getInfo().toRtf());
            addSkeleton("}\n");
        }
    }

    private void extractVariables(RtfDocument p_doc)
        throws Exception
    {
        RtfVariables vars = p_doc.getVariables();

        for (int i = 0, max = vars.count(); i < max; i++)
        {
            RtfVariable var = vars.getVariable(i);

            addSkeleton("{\\*\\docvar ");
            addSkeleton("{");
            addSkeleton(var.getName());
            addSkeleton("}");
            addSkeleton("{");
            addSkeleton(var.getValue());
            addSkeleton("}");
            addSkeleton("}\n");
        }
    }

    private void extractParagraphs(RtfDocument p_doc)
        throws Exception
    {
        for (int i = 0, max = p_doc.size(); i < max; i++)
        {
            extract(p_doc.getObject(i));

            /*
            RtfObject o = p_doc.getObject(i);

            if (o instanceof RtfControl)
            {
                extract((RtfControl)o);
            }
            else if (o instanceof RtfMarker)
            {
                extract((RtfMarker)o);
            }
            else if (o instanceof RtfParagraph)
            {
                extract((RtfParagraph)o);
            }
            else if (o instanceof RtfPageBreak)
            {
                extract((RtfPageBreak)o);
            }
            else if (o instanceof RtfCompoundObject)
            {
                extract((RtfCompoundObject)o);
            }
            else
            {
                debug("extractParagraphs(): unknown " + o);
                extract(o);
            }
            */
        }
    }

    private void extract(RtfObject p_object)
        throws Exception
    {
        if (p_object instanceof RtfFootnote)
        {
            extract((RtfFootnote)p_object);
        }
        else if (p_object instanceof RtfAnnotation)
        {
            extract((RtfAnnotation)p_object);
        }
        else if (p_object instanceof RtfMarker)
        {
            extract((RtfMarker)p_object);
        }
        else if (p_object instanceof RtfControl)
        {
            extract((RtfControl)p_object);
        }
        else if (p_object instanceof RtfText)
        {
            extract((RtfText)p_object);
        }
        else if (p_object instanceof RtfParagraph)
        {
            extract((RtfParagraph)p_object);
        }
        else if (p_object instanceof RtfData)
        {
            extract((RtfData)p_object);
        }
        else if (p_object instanceof RtfLineBreak)
        {
            extract((RtfLineBreak)p_object);
        }
        else if (p_object instanceof RtfPageBreak)
        {
            extract((RtfPageBreak)p_object);
        }
        else if (p_object instanceof RtfFieldInstance)
        {
            extract((RtfFieldInstance)p_object);
        }
        else if (p_object instanceof RtfSymbol)
        {
            extract((RtfSymbol)p_object);
        }
        else if (p_object instanceof RtfTab)
        {
            extract((RtfTab)p_object);
        }
        else if (p_object instanceof RtfHyperLink)
        {
            extract((RtfHyperLink)p_object);
        }
        else if (p_object instanceof RtfShape)
        {
            extract((RtfShape)p_object);
        }
        else if (p_object instanceof RtfShapeText)
        {
            extract((RtfShapeText)p_object);
        }
        else if (p_object instanceof RtfShapePicture)
        {
            extract((RtfShapePicture)p_object);
        }
        else if (p_object instanceof RtfPicture)
        {
            extract((RtfPicture)p_object);
        }
        else if (p_object instanceof RtfBookmark)
        {
            extract((RtfBookmark)p_object);
        }
        else if (p_object instanceof RtfAnnotationBookmark)
        {
            extract((RtfAnnotationBookmark)p_object);
        }
        else if (p_object instanceof RtfRow)
        {
            extract((RtfRow)p_object);
        }
        else if (p_object instanceof RtfCell)
        {
            extract((RtfCell)p_object);
        }
        else if (p_object instanceof RtfCompoundObject)
        {
            extract((RtfCompoundObject)p_object);
        }
        else
        {
            System.err.println("extract(RtfObject): unknown " + p_object);
        }
    }

    private void extract(RtfCompoundObject p_object)
        throws Exception
    {
        addToCurrent("{");

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            extract(p_object.getObject(i));
        }

        addToCurrent("}\n");
    }

    // Currently we extract row properties before *and* after the
    // cells as simple controls. We write them out as they come.
    private void extract(RtfRow p_object)
        throws Exception
    {
        //addToCurrent("{");
        addToCurrent(p_object.toRtf());
        addToCurrent("\n");

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            extract(p_object.getObject(i));
        }

        addToCurrent("\n\\row");
        //addToCurrent("}\n");
    }

    private void extract(RtfCell p_object)
        throws Exception
    {
        addToCurrent("{");

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            extract(p_object.getObject(i));
        }

        addToCurrent("\\cell}\n");
    }

    // Footnotes always appear inside paragraphs.
    private void extract(RtfFootnote p_object)
        throws Exception
    {
        //if (m_debug) { debug("Printing footnote " + p_object); }

        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("footnote");

        addTranslatableTmx(tg.getStart());
        addTranslatable("{\\footnote");
        if (p_object.isEndnote())
        {
            addTranslatable("\\ftnalt");
        }

        b_inTag = true;

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            extract(p_object.getObject(i));
        }

        b_inTag = false;

        addTranslatable("}");
        addTranslatableTmx(tg.getEnd());
    }

    private void extract(RtfControl p_object)
        throws Exception
    {
        addToCurrent(p_object.toRtf());
    }

    private void extract(RtfLineBreak p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            addToCurrent(p_object.toRtf());
        }
        else
        {
            TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("br");

            addTranslatableTmx(tg.getStart());

            addTranslatable(p_object.toRtf());

            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfPageBreak p_object)
        throws Exception
    {
        addSkeleton(p_object.toRtf());
    }

    private void extract(RtfFieldInstance p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            addToCurrent(p_object.toRtf());
        }
        else
        {
            TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("field");

            addTranslatableTmx(tg.getStart());

            addTranslatable(p_object.toRtf());

            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfSymbol p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            RtfTextProperties props = p_object.getProperties();
            // extractStyleStartTags(props);
            addToCurrent("{");
            addToCurrent(props.toRtf());

            addToCurrent(p_object.toRtf());

            //extractStyleEndTags(props);
            addToCurrent("}");
        }
        else
        {
            TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("symbol");

            addTranslatableTmx(tg.getStart());
            b_inTag = true;

            RtfTextProperties props = p_object.getProperties();
            extractStyleStartTags(props);

            addTranslatable(p_object.toRtf());

            extractStyleEndTags(props);

            b_inTag = false;
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfParagraph p_object)
        throws Exception
    {
        boolean inSkeleton = b_inSkeleton;

        if (inSkeleton)
        {
            m_tmxmap.resetCounter();
        }

        // Tue Sep 30 22:43:20 2003 Do not enclose paragraphs in { .. }
        addToCurrent("\n");
        addToCurrent(p_object.getProperties().toRtf());

        int style = p_object.getProperties().getStyle();
        RtfTextProperties props =
            m_document.getStyleSheet().getTextProperties(style);
        addToCurrent(props.toRtf());
        addToCurrent("\n");

        // A first attempt to optimize text output.
        if (inSkeleton &&
            p_object.size() == 1 && p_object.getObject(0) instanceof RtfText)
        {
            RtfText text = (RtfText)p_object.getObject(0);

            RtfTextProperties tprops = text.getProperties();

            addSkeleton("{");
            addSkeleton(tprops.toRtf());

            addTranslatable(text.getData());

            addSkeleton("}");
        }
        else
        {
            for (int i = 0, max = p_object.size(); i < max; i++)
            {
                extract(p_object.getObject(i));
            }
        }

        boolean par = p_object.getProperties().getPar();

        if (inSkeleton)
        {
            if (par)
            {
                addSkeleton("\\par");
            }
            addSkeleton("\n");
        }
        else
        {
            if (par)
            {
                addToCurrent("\\par");
            }
            addToCurrent("");
        }
    }

    private void extract(RtfText p_object)
        throws Exception
    {
        boolean inTag = b_inTag;

        if (inTag)
        {
            addTranslatableTmx("<sub type=\"text\" locType=\"translatable\">");
            b_inTag = false;
        }

        RtfTextProperties props = p_object.getProperties();

        extractStyleStartTags(props);

        addTranslatable(p_object.getData());

        extractStyleEndTags(props);

        if (inTag)
        {
            addTranslatableTmx("</sub>");
            b_inTag = true;
        }
    }

    private void extract(RtfMarker p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            RtfTextProperties props = p_object.getProperties();
            extractStyleStartTags(props);

            addToCurrent(p_object.toRtf());

            extractStyleEndTags(props);
        }
        else
        {
            TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("marker");

            addTranslatableTmx(tg.getStart());
            b_inTag = true;

            RtfTextProperties props = p_object.getProperties();
            extractStyleStartTags(props);

            addToCurrent(p_object.toRtf());

            extractStyleEndTags(props);

            b_inTag = false;
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extractStyleStartTags(RtfTextProperties p_props)
    {
        TmxTagGenerator tg = null;
        boolean output = false;

        if (p_props.isCharStyleSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\cs");
                addToCurrent(String.valueOf(p_props.getCharStyle()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cs", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\cs");
                addTranslatableTmx(String.valueOf(p_props.getCharStyle()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isFontSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\f");
                addToCurrent(String.valueOf(p_props.getFont()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("f", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\f");
                addTranslatableTmx(String.valueOf(p_props.getFont()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isFontSizeSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\fs");
                addToCurrent(String.valueOf(p_props.getFontSize()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("fs", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\fs");
                addTranslatableTmx(String.valueOf(p_props.getFontSize()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isLangSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\lang");
                addToCurrent(String.valueOf(p_props.getLang()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("lang", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\lang");
                addTranslatableTmx(String.valueOf(p_props.getLang()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }

        if (p_props.isBoldSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\b");
                addToCurrent(p_props.isBold() ? "" : "0");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("b", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\b");
                addTranslatableTmx(p_props.isBold() ? "" : "0");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isItalicSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\i");
                addToCurrent(p_props.isItalic() ? "" : "0");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("i", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\i");
                addTranslatableTmx(p_props.isItalic() ? "" : "0");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isUnderlinedSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\ul");
                addToCurrent(p_props.isUnderlined() ? "" : "0");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("ul", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\ul");
                addTranslatableTmx(p_props.isUnderlined() ? "" : "0");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isHiddenSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\v");
                addToCurrent(p_props.isHidden() ? "" : "0");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("v", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\v");
                addTranslatableTmx(p_props.isHidden() ? "" : "0");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isSubscriptSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\sub");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("sub", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\sub");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isSuperscriptSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\super");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("super", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\super");
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }

        if (p_props.isColorSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\cf");
                addToCurrent(String.valueOf(p_props.getColor()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cf", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\cf");
                addTranslatableTmx(String.valueOf(p_props.getColor()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }
        if (p_props.isBgColorSet())
        {
            if (b_inTag)
            {
                addToCurrent("{\\cb");
                addToCurrent(String.valueOf(p_props.getBgColor()));
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cb", true, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("{\\cb");
                addTranslatableTmx(String.valueOf(p_props.getBgColor()));
                addTranslatableTmx(tg.getEnd());
            }

            output = true;
        }

        if (output)
        {
            addTranslatableTmx(" ");
        }
   }

    private void extractStyleEndTags(RtfTextProperties p_props)
    {
        TmxTagGenerator tg = null;

        if (p_props.isBgColorSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cb", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isColorSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cf", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }

        if (p_props.isSuperscriptSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("super", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isSubscriptSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("sub", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isHiddenSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("v", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isUnderlinedSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("ul", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isItalicSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("i", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isBoldSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("b", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }

        if (p_props.isLangSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("lang", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isFontSizeSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("fs", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isFontSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("f", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
        if (p_props.isCharStyleSet())
        {
            if (b_inTag)
            {
                addToCurrent("}");
            }
            else
            {
                tg = m_tmxmap.getPairedTmxTag("cs", false, false);

                addTranslatableTmx(tg.getStart());
                addTranslatableTmx("}");
                addTranslatableTmx(tg.getEnd());
            }
        }
    }

    /**
     * Hyperlinks, which are really fields in RTF (placeholders) are
     * printed out as BPT/EPT like in HTML. The merger must recreate
     * the field.
     */
    private void extract(RtfHyperLink p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPairedTmxTag("link", true, false);

        addTranslatableTmx(tg.getStart());
        addTranslatable("{\\field{\\*\\fldinst{HYPERLINK \"");
        addTranslatable(p_object.getUrl());
        addTranslatable("\"");

        if (p_object.getRefid() != null)
        {
            addTranslatable(" \\\\l \"");
            addTranslatable(p_object.getRefid());
            addTranslatable("\"");
        }

        if (p_object.getAltText() != null)
        {
            addTranslatable(" \\\\o \"{");
            addTranslatableTmx("<sub locType=\"translatable\" ");
            addTranslatableTmx("type=\"alt\">");
            addTranslatable(p_object.getAltText());
            addTranslatableTmx("</sub>");
            addTranslatable("}\"");
        }

        addTranslatable("}}{\\fldrslt{");
        // TODO: style hack.
        addTranslatable("\\cf2\\ul ");
        addTranslatableTmx(tg.getEnd());

        addTranslatable(p_object.getText());

        tg = m_tmxmap.getPairedTmxTag("link", false, false);

        addTranslatableTmx(tg.getStart());
        addTranslatable("}}}");
        addTranslatableTmx(tg.getEnd());
    }

    /**
     * Shapes.
     */
    private void extract(RtfShape p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("shape");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        addToCurrent("{");
        addToCurrent(p_object.toRtf());

        extract((RtfCompoundObject)p_object);

        addToCurrent("}\n");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    /**
     * Shape paragraphs embedded in shapes.
     */
    private void extract(RtfShapeText p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("shape");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        addToCurrent("{");
        addToCurrent(p_object.toRtf());

        extract((RtfCompoundObject)p_object);

        addToCurrent("}\n");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    /**
     * Embedded Pictures. Merger must recreate {\*\shppict ...}.???????
     */
    private void extract(RtfShapePicture p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("img");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        extract((RtfCompoundObject)p_object);

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfPicture p_object)
        throws Exception
    {
        // \pict can appear as standalone pictures too (in test files)
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("img");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        addToCurrent("{\\pict");
        addToCurrent("\\");
        addToCurrent(p_object.getPictType());

        if (p_object.isPicwSet())
        {
            addToCurrent("\\picw");
            addToCurrent(String.valueOf(p_object.getPicw()));
        }
        if (p_object.isPichSet())
        {
            addToCurrent("\\pich");
            addToCurrent(String.valueOf(p_object.getPich()));
        }
        if (p_object.isPicwgoalSet())
        {
            addToCurrent("\\picwgoal");
            addToCurrent(String.valueOf(p_object.getPicwgoal()));
        }
        if (p_object.isPichgoalSet())
        {
            addToCurrent("\\pichgoal");
            addToCurrent(String.valueOf(p_object.getPichgoal()));
        }
        if (p_object.isPicscalexSet())
        {
            addToCurrent("\\picscalex");
            addToCurrent(String.valueOf(p_object.getPicscalex()));
        }
        if (p_object.isPicscaleySet())
        {
            addToCurrent("\\picscaley");
            addToCurrent(String.valueOf(p_object.getPicscaley()));
        }
        if (p_object.isPiccroptSet())
        {
            addToCurrent("\\piccropt");
            addToCurrent(String.valueOf(p_object.getPiccropt()));
        }
        if (p_object.isPiccropbSet())
        {
            addToCurrent("\\piccropb");
            addToCurrent(String.valueOf(p_object.getPiccropb()));
        }
        if (p_object.isPiccroprSet())
        {
            addToCurrent("\\piccropr");
            addToCurrent(String.valueOf(p_object.getPiccropr()));
        }
        if (p_object.isPiccroplSet())
        {
            addToCurrent("\\piccropl");
            addToCurrent(String.valueOf(p_object.getPiccropl()));
        }

        addToCurrent("\n");
        addToCurrent(p_object.getPictData());
        addToCurrent("}");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfBookmark p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("bookmark");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        addToCurrent(p_object.toRtf());

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfAnnotationBookmark p_object)
        throws Exception
    {
        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("annotationbookmark");

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getStart());
        }

        addToCurrent(p_object.toRtf());

        if (!(b_inSkeleton || b_inTag))
        {
            addTranslatableTmx(tg.getEnd());
        }
    }

    private void extract(RtfAnnotation p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            for (int i = 0, max = p_object.size(); i < max; i++)
            {
                extract(p_object.getObject(i));
            }

            return;
        }

        // Else if inside segment, extract like footnotes.

        TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("annotation");

        addTranslatableTmx(tg.getStart());
        addTranslatableTmx("{\\*\\annotation");

        if (p_object.getRef() != null)
        {
            addTranslatableTmx("{\\*\\atnref ");
            addTranslatableTmx(p_object.getRef());
            addTranslatableTmx("}");
        }

        b_inTag = true;

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            extract(p_object.getObject(i));
        }

        b_inTag = false;

        addTranslatable("}");
        addTranslatableTmx(tg.getEnd());
    }

    private void extract(RtfData p_object)
        throws Exception
    {
        addToCurrent(m_codec.encode(p_object.getData()));
    }

    private void extract(RtfTab p_object)
        throws Exception
    {
        if (b_inSkeleton || b_inTag)
        {
            addSkeleton("\\tab ");
        }
        else
        {
            // TAB should be a paragraph break.
            addSkeleton("\\tab ");
            /*
            TmxTagGenerator tg = m_tmxmap.getPlaceholderTmxTag("tab");

            addTranslatableTmx(tg.getStart());
            addTranslatable("\\tab ");
            addTranslatableTmx(tg.getEnd());
            */
        }
    }

    private void extractTrailer(RtfDocument p_doc)
        throws Exception
    {
        addSkeleton("}\n");
    }
}
