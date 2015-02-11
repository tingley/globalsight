/**
 *  Copyright 2010 Welocalize, Inc. 
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
package com.globalsight.ling.docproc.extractor.fm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.FMFilter;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.oroinc.text.regex.MalformedPatternException;
import com.oroinc.text.regex.MatchResult;
import com.oroinc.text.regex.Pattern;
import com.oroinc.text.regex.PatternCompiler;
import com.oroinc.text.regex.PatternMatcher;
import com.oroinc.text.regex.Perl5Compiler;
import com.oroinc.text.regex.Perl5Matcher;

public class Extractor extends AbstractExtractor
{
    private static final Logger logger = Logger.getLogger(Extractor.class);

    private static int index = 1;

    private static final boolean WITH_RETURN = true;
    private static final boolean NO_RETURN = false;

    private static final String regexp1 = "[\\w\\W]*[\\s]*(\\<[\\$]?[^<^>]*\\\\>)+[\\s]*[\\w\\W]*";
    private static final String regexp2 = "[A-Za-z]:[\\w\\W]*";
    private static final String continusInlineTag = "</ph><ph[^>]*>";

    /**
     * used to mark whether the previous line is string
     */
    private boolean inString = false;

    /**
     * used to mark whether it is "Callout"(Callout string should be extracted).
     */
    private boolean inCalloutPgfTag = false;

    /**
     * used to mark whether the current line is in font
     */
    private boolean inFontTag = false;
    /**
     * used to mark whether the current line is in xref
     */
    private boolean inXrefTag = false;
    private boolean inMarker = false;
    private boolean inPage = false;
    private boolean translatable = false;
    // in case of nested frames, the flag is a number.
    // As long as the number is bigger than 0, it means it's in a frame area.
    // When entering a frame, the tag plus 1. And when leaving, the tag minus 1.
    private int inFrame = 0;
    private boolean inTextRect = false;
    private boolean inGroup = false;
    private boolean inVariable = false;
    private boolean inConditional = false;

    private static Map<String, String> specialCharMap = new HashMap<String, String>();

    // contains the tags that are used for inline tags which will not be added
    // to skeleton.
    private static List<String> inlineTags = new ArrayList<String>();

    private StringBuffer storage = new StringBuffer();

    private XmlEntities m_xmlEncoder;

    public Extractor()
    {
        super();

        specialCharMap.put("EnDash", "\u2013");
        specialCharMap.put("EmDash", "\u2014");
        specialCharMap.put("NoHyphen", "\u200d");
        specialCharMap.put("Cent", "\u00a2");
        specialCharMap.put("Pound", "\u00a3");
        specialCharMap.put("Yen", "\u00a5");
        specialCharMap.put("Dagger", "\u2020");
        specialCharMap.put("DoubleDagger", "\u2021");
        specialCharMap.put("ThinSpace", "\u2009");
        specialCharMap.put("EnSpace", "\u2002");
        specialCharMap.put("EmSpace", "\u2003");
        specialCharMap.put("SoftHyphen", "");
        specialCharMap.put("NoHyphen", "");
        specialCharMap.put("DiscHyphen", "");
        specialCharMap.put("HardHyphen", "-");

        inlineTags.add("&lt;$chapnum\\&gt;");

        m_xmlEncoder = new XmlEntities();

    }

    /**
     * extract mif files
     */
    public void extract() throws ExtractorException
    {
        Output output = getOutput();
        setMainFormat(ExtractorRegistry.FORMAT_MIF);
        FMFilter filter = null;
        if (getMainFilter() != null)
        {
            filter = (FMFilter) getMainFilter();
        }

        if (logger.isDebugEnabled())
        {
            logger.info("freeMemory: " + Runtime.getRuntime().freeMemory());
            logger.info("maxMemory: " + Runtime.getRuntime().maxMemory());
            logger.info("totalMemory: " + Runtime.getRuntime().totalMemory());
        }
        long timeStarted = System.currentTimeMillis();

        extractWithReadLine(output, filter);
        // extractWithAllLines(output, filter);

        long timeFinished = System.currentTimeMillis();
        if (logger.isDebugEnabled())
        {
            logger.info("freeMemory: " + Runtime.getRuntime().freeMemory());
            logger.info("maxMemory: " + Runtime.getRuntime().maxMemory());
            logger.info("totalMemory: " + Runtime.getRuntime().totalMemory());
            logger.info("MIF extractor time: " + (timeFinished - timeStarted));
        }
    }

    private void extractWithReadLine(Output output, FMFilter filter)
    {
        StringBuffer tmpString = new StringBuffer("");
        String pageType = "";
        String frame_Id = "";

        // save all the translatable textrect in bodypages
        List<String> textRectListInBodyPage = new ArrayList<String>();

        // save all the translatable textrect in frames
        List<String> textRectListInFrame = new ArrayList<String>();

        // save all textrect in frames
        TextRectInFrame textRectInFrameMap = new TextRectInFrame();

        // used to store whether the table should be translated
        Map<String, Boolean> tableMap = new HashMap<String, Boolean>();

        // used to store whether the frame should be translated
        Map<String, Boolean> frameMap = new HashMap<String, Boolean>();

        // save all the translatable textrect for "Callout".
        List<String> textRectIdListForCallout = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(readInput());
        try
        {
            /*
             * the extractor needs 2 loops. In the 1st loop, find out
             * translatable tables, frames, bodypages, and textrects. In the 2nd
             * loop, parse the file.
             */
            firstLoop(reader, pageType, textRectListInBodyPage,
                    textRectListInFrame, tableMap, frameMap,
                    textRectInFrameMap, frame_Id, textRectIdListForCallout,
                    filter);

            reset();
            Marker marker = new Marker();

            // close first reader and open new one
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                logger.error("Close MIF file with exception", e);
            }
            reader = new BufferedReader(readInput());

            secondLoop(reader, output, frameMap, tableMap,
                    textRectListInBodyPage, textRectListInFrame, tmpString,
                    marker, textRectIdListForCallout);
        }
        catch (Exception ex)
        {
            throw new ExtractorException(ex);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    logger.error("Close MIF file with exception", e);
                }
            }
        }
    }

    private void firstLoop(BufferedReader reader, String pageType,
            List<String> textRectListInBodyPage,
            List<String> textRectListInFrame, Map<String, Boolean> tableMap,
            Map<String, Boolean> frameMap, TextRectInFrame textRectInFrameMap,
            String frame_Id, List<String> textRectIdListForCallout,
            FMFilter filter) throws NumberFormatException, IOException
    {
        String line = reader.readLine();
        for (; line != null; line = reader.readLine())
        {
            if (line.trim().startsWith(Tag.MIF_FILE_HEAD)) // <MIFFile
            {
                String mifVersion = Parser.getTagContent(line,
                        Tag.MIF_FILE_HEAD);
                double version = Double.parseDouble(mifVersion);
                if (version < 9)
                {
                    throw new ExtractorException(
                            ExtractorExceptionConstants.MIF_VERSION_ERROR);
                }
            }
            else if (line.trim().startsWith(Tag.PAGETYPE_HEAD)) // <PageType
            {
                inPage = true;
                pageType = Parser.getTagContent(line, Tag.PAGETYPE_HEAD);
            }
            // <ID, textrects in page tag
            else if (inPage && line.trim().startsWith(Tag.TEXT_RECT_ID))
            {
                if (pageType.equalsIgnoreCase("BodyPage"))
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("LeftMasterPage")
                        && filter != null && filter.isExposeLeftMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("RightMasterPage")
                        && filter != null && filter.isExposeRightMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("OtherMasterPage")
                        && filter != null && filter.isExposeOtherMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
            }
            else if (line.trim().equals(Tag.PAGE_END)) // # end of Page
            {
                inPage = false;
                translatable = false;
            }
            else if (line.trim().startsWith(Tag.TEXTRECTID_HEAD)) // <TextRectID
            {
                String rectID = Parser.getTagContent(line, Tag.TEXTRECTID_HEAD);
                if (textRectListInBodyPage.contains(rectID)
                        || textRectListInFrame.contains(rectID))
                {
                    translatable = true;
                }
                else
                {
                    translatable = false;
                }

                if (inCalloutPgfTag)
                {
                    translatable = true;
                    textRectIdListForCallout.add(rectID);
                }
            }
            else if (line.trim().startsWith(Tag.A_TABLE_ID)) // <ATbl
            {
                String referedTableId = Parser.getTagContent(line,
                        Tag.A_TABLE_ID);
                tableMap.put(referedTableId, translatable);
            }
            else if (line.trim().startsWith(Tag.A_FRAME_ID)
                    && !line.trim().equals("<AFrames")) // <AFrame
            {
                String referedFrameId = Parser.getTagContent(line,
                        Tag.A_FRAME_ID);
                frameMap.put(referedFrameId, translatable);
                List<String> textRectInFrame = textRectInFrameMap
                        .getTextRectInFrame(referedFrameId);
                if (translatable)
                {
                    textRectListInFrame.addAll(textRectInFrame);
                }
            }
            else if (line.trim().equals(Tag.FRAME_HEAD)) // <Frame
            {
                inFrame++;
            }
            else if (line.trim().equals(Tag.FRAME_END)) // > # end of Frame
            {
                inFrame--;
            }
            else if (line.trim().equals(Tag.TEXTRECT_HEAD)) // <TextRect
            {
                inTextRect = true;
            }
            else if (line.trim().equals(Tag.TEXTRECT_END)) // > # end of
                                                           // TextRect
            {
                inTextRect = false;
            }
            else if (line.trim().equals(Tag.GROUP_HEAD)) // <Group
            {
                inGroup = true;
            }
            else if (line.trim().equals(Tag.GROUP_END)) // # end of Group
            {
                inGroup = false;
            }
            else if (inFrame > 0 && inTextRect
                    && line.trim().startsWith(Tag.TEXT_RECT_ID)) // <ID
            {
                String textRectID = Parser
                        .getTagContent(line, Tag.TEXT_RECT_ID);
                textRectInFrameMap.saveRect(frame_Id, textRectID);
                textRectListInFrame.add(textRectID);
            }
            else if (inFrame > 0 && !inGroup && !inTextRect
                    && line.trim().startsWith(Tag.FRAME_ID)) // <ID
            {
                frame_Id = Parser.getTagContent(line, Tag.FRAME_ID);
            }
            else if (line.trim().equals(Tag.PGFTAG_CALLOUT))
            {
                inCalloutPgfTag = true;
            }
            else if (line.trim().equals(Tag.PARA_END))
            {
                // As callout pgfTag is always in "para", when para ends, set
                // this to false.
                inCalloutPgfTag = false;
            }
        }
    }

    private void secondLoop(BufferedReader reader, Output output,
            Map<String, Boolean> FrameMap, Map<String, Boolean> TableMap,
            List<String> textRectListInBodyPage,
            List<String> textRectListInFrame, StringBuffer tmpString,
            Marker marker, List<String> textRectIdListForCallout)
            throws IOException
    {
        Stack<String> tagLevel = new Stack<String>();
        // this is a flag, saving whether or not it's translatable when entering
        // a footnote area. When leaving footnote, translatable flag should
        // remain
        // as what it is when entering footnote.
        boolean translatableOutsideNote = false;

        String lineWithWhiteSpace = reader.readLine();
        for (; lineWithWhiteSpace != null; lineWithWhiteSpace = reader
                .readLine())
        {
            String lineWithoutWhiteSpace = lineWithWhiteSpace.trim();
            if (lineWithoutWhiteSpace.startsWith("<")
                    && !lineWithoutWhiteSpace.endsWith(">"))
            {
                tagLevel.push(lineWithoutWhiteSpace.substring(1));
            }
            else if (lineWithoutWhiteSpace.startsWith(">"))
            {
                if (lineWithoutWhiteSpace.equals(">") && !tagLevel.empty()
                        && translatable)
                {
                    String l = tagLevel.peek();
                    lineWithoutWhiteSpace += " # end of " + l;
                    lineWithWhiteSpace += " # end of " + l;
                }
                tagLevel.pop();
            }

            if (lineWithoutWhiteSpace.equals(Tag.FRAME_HEAD)) // <Frame
            {
                inFrame++;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (inFrame > 0 && !inGroup && !inTextRect
                    && lineWithoutWhiteSpace.startsWith(Tag.FRAME_ID)) // <ID
            {
                String frameId = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.FRAME_ID);
                translatable = FrameMap.get(frameId);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.FRAME_END)) // > # end of
                                                                  // Frame
            {
                inFrame--;
                // Nested "Frame":inner Frames have no "ID".
                if (inFrame == 0)
                {
                    translatable = false;
                }
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.GROUP_HEAD)) // <Group
            {
                inGroup = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.GROUP_END)) // # end of
                                                                  // Group
            {
                inGroup = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.TEXTRECT_HEAD)) // <TextRect
            {
                inTextRect = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.TEXTRECT_END)) // > # end
                                                                     // of
                                                                     // TextRect
            {
                inTextRect = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.startsWith(Tag.TABLE_ID)) // <TblID
            {
                String tableId = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.TABLE_ID);
                translatable = TableMap.get(tableId);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.TABLE_END)) // > # end of
                                                                  // Tbl
            {
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                translatable = false;
            }
            else if (lineWithoutWhiteSpace.equals(Tag.PARA_END)) // # end of
                                                                 // Para
            {
                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addSkeleton(output, Tag.PARALINE_END, WITH_RETURN);
                }
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                inString = false;
            }
            else if (lineWithoutWhiteSpace.startsWith(Tag.TEXTRECTID_HEAD)) // <TextRectID
            {
                String rectID = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.TEXTRECTID_HEAD);
                if (textRectListInBodyPage.contains(rectID)
                        || textRectListInFrame.contains(rectID))
                {
                    translatable = true;
                }
                else
                {
                    translatable = false;
                }
                if (inCalloutPgfTag
                        && textRectIdListForCallout.contains(rectID))
                {
                    translatable = true;
                }

                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.TEXTRECTID, true));
                    addTranslatableTmx(getStringPhStart());
                }
                else
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (translatable
                    && lineWithoutWhiteSpace.startsWith(Tag.XREFEND_HEAD)) // <XRefEnd
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.XREF, true));
                tmpString.setLength(0);
                inXrefTag = false;
            }
            else if (translatable
                    && (inXrefTag || lineWithoutWhiteSpace
                            .equals(Tag.XREF_HEAD))) // <XRef
            {
                endStringTag(output);
                inXrefTag = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (lineWithoutWhiteSpace.equals(Tag.PARALINE_HEAD)) // <ParaLine
            {
                if (!inString)
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (lineWithoutWhiteSpace.equals(Tag.PARALINE_END)) // # end of
                                                                     // ParaLine
            {
                if (!inString)
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (lineWithoutWhiteSpace.startsWith(Tag.STRING_HEAD)
                    && translatable) // <String
            {
                if (!inString)
                {
                    addTranslatableTmx(getStringPhStart());
                }
                inString = true;
                // output.addTranslatable(Parser.getStringContent(line));
                addTranslatable(Parser.getStringContent(lineWithWhiteSpace));
            }
            else if (translatable && lineWithoutWhiteSpace.equals(Tag.FONT_END)) // >
                                                                                 // #
                                                                                 // end
                                                                                 // of
            // Font
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.FONT, true));
                tmpString.setLength(0);
                inFontTag = false;
            }
            else if (translatable
                    && (inFontTag || lineWithoutWhiteSpace
                            .equals(Tag.FONT_HEAD))) // <Font
            {
                endStringTag(output);
                inFontTag = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (translatable
                    && lineWithoutWhiteSpace.equals(Tag.MARKER_END)) // > # end
                                                                     // of
            // Marker
            {
                addTranslatableTmx(handleMarkerTag(marker));
                inMarker = false;
            }
            else if (translatable
                    && (inMarker || lineWithoutWhiteSpace
                            .equals(Tag.MARKER_HEAD))) // <Marker
            {
                endStringTag(output);
                inMarker = true;
                if (lineWithoutWhiteSpace.startsWith(Tag.UNIQUE_HEAD)) // <Unique
                {
                    marker.setUnique(Parser.getTagContent(lineWithWhiteSpace,
                            Tag.UNIQUE_HEAD));
                }
                else if (lineWithoutWhiteSpace.startsWith(Tag.MTYPENAME_HEAD)) // <MTypeName
                {
                    marker.setMTypeName(Parser
                            .getStringContent(lineWithWhiteSpace));
                }
                else if (lineWithoutWhiteSpace.startsWith(Tag.MTYPE_HEAD)) // <MType
                {
                    marker.setMType(Parser.getTagContent(lineWithWhiteSpace,
                            Tag.MTYPE_HEAD));
                }
                else if (lineWithoutWhiteSpace.startsWith(Tag.MTEXT_HEAD)) // <MText
                {
                    marker.setMText(lineWithWhiteSpace.substring(
                            lineWithWhiteSpace.indexOf("`") + 1,
                            lineWithWhiteSpace.indexOf("'")));
                }
                else if (lineWithoutWhiteSpace.startsWith(Tag.MCURRPAGE_HEAD)) // <MCurrPage
                {
                    marker.setMCurrPage(Parser
                            .getStringContent(lineWithWhiteSpace));
                }
            }
            else if (inString
                    && lineWithoutWhiteSpace.startsWith(Tag.CHAR_HEAD)) // <Char
            {
                // String nextLine = lineList.get(lineNo + 1);
                addTranslatableTmx(handleChar(lineWithWhiteSpace));
            }
            else if (translatable
                    && lineWithoutWhiteSpace.equals(Tag.VARIABLE_END)) // > #
                                                                       // end
            // of
            // Variable
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.VARIABLE, true));
                tmpString.setLength(0);
                inVariable = false;
            }
            else if (translatable
                    && (inVariable || lineWithoutWhiteSpace
                            .equals(Tag.VARIABLE_HEAD))) // <Variable
            {
                endStringTag(output);
                inVariable = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (lineWithoutWhiteSpace.startsWith(Tag.VARIABLE_DEF_HEAD)
                    || lineWithoutWhiteSpace.startsWith(Tag.XREF_DEF_HEAD)) // <VariableDef
                                                                            // &
            // <XRefDef &
            {
                exposeDef(lineWithWhiteSpace, output, "text");
            }
            else if (lineWithoutWhiteSpace.startsWith(Tag.PGF_NUMBER_STRING)) // <PgfNumString
            {
                handleNumString(lineWithWhiteSpace, output);
            }
            else if (translatable
                    && lineWithoutWhiteSpace.equals(Tag.CONDITIONAL_END)) // > #
            // end of
            // Conditional
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.CONDITIONAL, true));
                tmpString.setLength(0);
                inConditional = false;
            }
            else if (translatable
                    && (inConditional || lineWithoutWhiteSpace
                            .startsWith(Tag.CONDITIONAL_HEAD))) // <Conditional
            {
                endStringTag(output);
                inConditional = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (translatable
                    && lineWithoutWhiteSpace.startsWith(Tag.UNCONDITIONAL)) // <Unconditional
            {
                endStringTag(output);
                addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                        Tag.CONDITIONAL, true));
            }
            else if (lineWithoutWhiteSpace.equals(Tag.NOTES_HEAD)) // <Notes
            {
                translatableOutsideNote = translatable;
                translatable = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.NOTES_END)) // > # end of
                                                                  // Notes
            {
                translatable = translatableOutsideNote;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (translatable
                    && lineWithoutWhiteSpace.startsWith(Tag.NOTES_REF)
                    && lineWithoutWhiteSpace.endsWith(">")) // <FNote
            {
                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.NOTES, true));
                    addTranslatableTmx(getStringPhStart());
                }
                else
                {
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.NOTES, true));
                }
            }
            else if (lineWithoutWhiteSpace.equals(Tag.PGFTAG_CALLOUT))
            {
                inCalloutPgfTag = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (lineWithoutWhiteSpace.equals(Tag.PARA_END))
            {
                // As callout pgfTag is always in "para", when para ends, set
                // this to false.
                inCalloutPgfTag = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else
            {
                endStringTag(output);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
        }
    }

    private void extractWithAllLines(Output output, FMFilter filter)
    {
        Parser parser = new Parser(readInput());
        // all the contents in mif file
        List<String> lineList = parser.getLineList();
        extractContent(lineList, output, filter);
    }

    private void extractContent(List<String> lineList, Output output,
            FMFilter filter) throws ExtractorException
    {
        StringBuffer tmpString = new StringBuffer("");
        String pageType = "";
        String frame_Id = "";

        // save all the translatable textrect in bodypages
        List<String> textRectListInBodyPage = new ArrayList<String>();

        // save all the translatable textrect in frames
        List<String> textRectListInFrame = new ArrayList<String>();

        // save all textrect in frames
        TextRectInFrame textRectInFrameMap = new TextRectInFrame();

        // used to store whether the table should be translated
        Map<String, Boolean> tableMap = new HashMap<String, Boolean>();

        // used to store whether the frame should be translated
        Map<String, Boolean> frameMap = new HashMap<String, Boolean>();

        // save all the translatable textrect for "Callout".
        List<String> textRectIdListForCallout = new ArrayList<String>();

        /*
         * the extractor needs 2 loops. In the 1st loop, find out translatable
         * tables, frames, bodypages, and textrects. In the 2nd loop, parse the
         * file.
         */
        firstLoop(lineList, pageType, textRectListInBodyPage,
                textRectListInFrame, tableMap, frameMap, textRectInFrameMap,
                frame_Id, textRectIdListForCallout, filter);

        reset();
        Marker marker = new Marker();

        secondLoop(lineList, output, frameMap, tableMap,
                textRectListInBodyPage, textRectListInFrame, tmpString, marker,
                textRectIdListForCallout);
    }

    private void firstLoop(List<String> lineList, String pageType,
            List<String> textRectListInBodyPage,
            List<String> textRectListInFrame, Map<String, Boolean> tableMap,
            Map<String, Boolean> frameMap, TextRectInFrame textRectInFrameMap,
            String frame_Id, List<String> textRectIdListForCallout,
            FMFilter filter)
    {
        for (int lineNo = 0; lineNo < lineList.size(); lineNo++)
        {
            String line = lineList.get(lineNo);
            if (line.trim().startsWith(Tag.MIF_FILE_HEAD)) // <MIFFile
            {
                String mifVersion = Parser.getTagContent(line,
                        Tag.MIF_FILE_HEAD);
                double version = Double.parseDouble(mifVersion);
                if (version < 9)
                {
                    throw new ExtractorException(
                            ExtractorExceptionConstants.MIF_VERSION_ERROR);
                }
            }
            else if (line.trim().startsWith(Tag.PAGETYPE_HEAD)) // <PageType
            {
                inPage = true;
                pageType = Parser.getTagContent(line, Tag.PAGETYPE_HEAD);
            }
            // <ID, textrects in page tag
            else if (inPage && line.trim().startsWith(Tag.TEXT_RECT_ID))
            {
                if (pageType.equalsIgnoreCase("BodyPage"))
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("LeftMasterPage")
                        && filter != null && filter.isExposeLeftMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("RightMasterPage")
                        && filter != null && filter.isExposeRightMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
                else if (pageType.equalsIgnoreCase("OtherMasterPage")
                        && filter != null && filter.isExposeOtherMasterPage())
                {
                    String textRectID = Parser.getTagContent(line,
                            Tag.TEXT_RECT_ID);
                    textRectListInBodyPage.add(textRectID);
                    translatable = true;
                }
            }
            else if (line.trim().equals(Tag.PAGE_END)) // # end of Page
            {
                inPage = false;
                translatable = false;
            }
            else if (line.trim().startsWith(Tag.TEXTRECTID_HEAD)) // <TextRectID
            {
                String rectID = Parser.getTagContent(line, Tag.TEXTRECTID_HEAD);
                if (textRectListInBodyPage.contains(rectID)
                        || textRectListInFrame.contains(rectID))
                {
                    translatable = true;
                }
                else
                {
                    translatable = false;
                }

                if (inCalloutPgfTag)
                {
                    translatable = true;
                    textRectIdListForCallout.add(rectID);
                }
            }
            else if (line.trim().startsWith(Tag.A_TABLE_ID)) // <ATbl
            {
                String referedTableId = Parser.getTagContent(line,
                        Tag.A_TABLE_ID);
                tableMap.put(referedTableId, translatable);
            }
            else if (line.trim().startsWith(Tag.A_FRAME_ID)
                    && !line.trim().equals("<AFrames")) // <AFrame
            {
                String referedFrameId = Parser.getTagContent(line,
                        Tag.A_FRAME_ID);
                frameMap.put(referedFrameId, translatable);
                List<String> textRectInFrame = textRectInFrameMap
                        .getTextRectInFrame(referedFrameId);
                if (translatable)
                {
                    textRectListInFrame.addAll(textRectInFrame);
                }
            }
            else if (line.trim().equals(Tag.FRAME_HEAD)) // <Frame
            {
                inFrame++;
            }
            else if (line.trim().equals(Tag.FRAME_END)) // > # end of Frame
            {
                inFrame--;
            }
            else if (line.trim().equals(Tag.TEXTRECT_HEAD)) // <TextRect
            {
                inTextRect = true;
            }
            else if (line.trim().equals(Tag.TEXTRECT_END)) // > # end of
                                                           // TextRect
            {
                inTextRect = false;
            }
            else if (line.trim().equals(Tag.GROUP_HEAD)) // <Group
            {
                inGroup = true;
            }
            else if (line.trim().equals(Tag.GROUP_END)) // # end of Group
            {
                inGroup = false;
            }
            else if (inFrame > 0 && inTextRect
                    && line.trim().startsWith(Tag.TEXT_RECT_ID)) // <ID
            {
                String textRectID = Parser
                        .getTagContent(line, Tag.TEXT_RECT_ID);
                textRectInFrameMap.saveRect(frame_Id, textRectID);
                textRectListInFrame.add(textRectID);
            }
            else if (inFrame > 0 && !inGroup && !inTextRect
                    && line.trim().startsWith(Tag.FRAME_ID)) // <ID
            {
                frame_Id = Parser.getTagContent(line, Tag.FRAME_ID);
            }
            else if (line.trim().equals(Tag.PGFTAG_CALLOUT))
            {
                inCalloutPgfTag = true;
            }
            else if (line.trim().equals(Tag.PARA_END))
            {
                // As callout pgfTag is always in "para", when para ends, set
                // this to false.
                inCalloutPgfTag = false;
            }
        }
    }

    private void secondLoop(List<String> lineList, Output output,
            Map<String, Boolean> FrameMap, Map<String, Boolean> TableMap,
            List<String> textRectListInBodyPage,
            List<String> textRectListInFrame, StringBuffer tmpString,
            Marker marker, List<String> textRectIdListForCallout)
    {
        Stack<String> tagLevel = new Stack<String>();
        // this is a flag, saving whether or not it's translatable when entering
        // a footnote area. When leaving footnote, translatable flag should
        // remain
        // as what it is when entering footnote.
        boolean translatableOutsideNote = false;

        for (int lineNo = 0; lineNo < lineList.size(); lineNo++)
        {
            String line = lineList.get(lineNo).trim();
            String lineWithWhiteSpace = lineList.get(lineNo);
            if (line.startsWith("<") && !line.endsWith(">") && lineNo > 0)
            {
                tagLevel.push(line.substring(1));
            }
            else if (line.startsWith(">"))
            {
                if (line.equals(">") && !tagLevel.empty() && translatable)
                {
                    String l = tagLevel.peek();
                    line += " # end of " + l;
                    lineWithWhiteSpace += " # end of " + l;
                }
                tagLevel.pop();
            }

            if (line.equals(Tag.FRAME_HEAD)) // <Frame
            {
                inFrame++;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (inFrame > 0 && !inGroup && !inTextRect
                    && line.startsWith(Tag.FRAME_ID)) // <ID
            {
                String frameId = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.FRAME_ID);
                translatable = FrameMap.get(frameId);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.FRAME_END)) // > # end of Frame
            {
                inFrame--;
                // Nested "Frame":inner Frames have no "ID".
                if (inFrame == 0)
                {
                    translatable = false;
                }
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.GROUP_HEAD)) // <Group
            {
                inGroup = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.GROUP_END)) // # end of Group
            {
                inGroup = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.TEXTRECT_HEAD)) // <TextRect
            {
                inTextRect = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.TEXTRECT_END)) // > # end of TextRect
            {
                inTextRect = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.startsWith(Tag.TABLE_ID)) // <TblID
            {
                String tableId = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.TABLE_ID);
                translatable = TableMap.get(tableId);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.TABLE_END)) // > # end of Tbl
            {
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                translatable = false;
            }
            else if (line.equals(Tag.PARA_END)) // # end of Para
            {
                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addSkeleton(output, Tag.PARALINE_END, WITH_RETURN);
                }
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                inString = false;
            }
            else if (line.startsWith(Tag.TEXTRECTID_HEAD)) // <TextRectID
            {
                String rectID = Parser.getTagContent(lineWithWhiteSpace,
                        Tag.TEXTRECTID_HEAD);
                if (textRectListInBodyPage.contains(rectID)
                        || textRectListInFrame.contains(rectID))
                {
                    translatable = true;
                }
                else
                {
                    translatable = false;
                }
                if (inCalloutPgfTag
                        && textRectIdListForCallout.contains(rectID))
                {
                    translatable = true;
                }

                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.TEXTRECTID, true));
                    addTranslatableTmx(getStringPhStart());
                }
                else
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (translatable && line.startsWith(Tag.XREFEND_HEAD)) // <XRefEnd
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.XREF, true));
                tmpString.setLength(0);
                inXrefTag = false;
            }
            else if (translatable && (inXrefTag || line.equals(Tag.XREF_HEAD))) // <XRef
            {
                endStringTag(output);
                inXrefTag = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (line.equals(Tag.PARALINE_HEAD)) // <ParaLine
            {
                if (!inString)
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (line.equals(Tag.PARALINE_END)) // # end of ParaLine
            {
                if (!inString)
                {
                    addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
                }
            }
            else if (line.startsWith(Tag.STRING_HEAD) && translatable) // <String
            {
                if (!inString)
                {
                    addTranslatableTmx(getStringPhStart());
                }
                inString = true;
                // output.addTranslatable(Parser.getStringContent(line));
                addTranslatable(Parser.getStringContent(lineWithWhiteSpace));
            }
            else if (translatable && line.equals(Tag.FONT_END)) // > # end of
                                                                // Font
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.FONT, true));
                tmpString.setLength(0);
                inFontTag = false;
            }
            else if (translatable && (inFontTag || line.equals(Tag.FONT_HEAD))) // <Font
            {
                endStringTag(output);
                inFontTag = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (translatable && line.equals(Tag.MARKER_END)) // > # end of
                                                                  // Marker
            {
                addTranslatableTmx(handleMarkerTag(marker));
                inMarker = false;
            }
            else if (translatable && (inMarker || line.equals(Tag.MARKER_HEAD))) // <Marker
            {
                endStringTag(output);
                inMarker = true;
                if (line.startsWith(Tag.UNIQUE_HEAD)) // <Unique
                {
                    marker.setUnique(Parser.getTagContent(lineWithWhiteSpace,
                            Tag.UNIQUE_HEAD));
                }
                else if (line.startsWith(Tag.MTYPENAME_HEAD)) // <MTypeName
                {
                    marker.setMTypeName(Parser
                            .getStringContent(lineWithWhiteSpace));
                }
                else if (line.startsWith(Tag.MTYPE_HEAD)) // <MType
                {
                    marker.setMType(Parser.getTagContent(lineWithWhiteSpace,
                            Tag.MTYPE_HEAD));
                }
                else if (line.startsWith(Tag.MTEXT_HEAD)) // <MText
                {
                    marker.setMText(lineWithWhiteSpace.substring(
                            lineWithWhiteSpace.indexOf("`") + 1,
                            lineWithWhiteSpace.indexOf("'")));
                }
                else if (line.startsWith(Tag.MCURRPAGE_HEAD)) // <MCurrPage
                {
                    marker.setMCurrPage(Parser
                            .getStringContent(lineWithWhiteSpace));
                }
            }
            else if (inString && line.startsWith(Tag.CHAR_HEAD)) // <Char
            {
                // String nextLine = lineList.get(lineNo + 1);
                addTranslatableTmx(handleChar(lineWithWhiteSpace));
            }
            else if (translatable && line.equals(Tag.VARIABLE_END)) // > # end
                                                                    // of
                                                                    // Variable
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.VARIABLE, true));
                tmpString.setLength(0);
                inVariable = false;
            }
            else if (translatable
                    && (inVariable || line.equals(Tag.VARIABLE_HEAD))) // <Variable
            {
                endStringTag(output);
                inVariable = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (line.startsWith(Tag.VARIABLE_DEF_HEAD)
                    || line.startsWith(Tag.XREF_DEF_HEAD)) // <VariableDef &
                                                           // <XRefDef &
            {
                exposeDef(lineWithWhiteSpace, output, "text");
            }
            // else if (line.startsWith(Tag.PGF_NUMBER_STRING)) // <PgfNumString
            // {
            // handleNumString(line, output);
            // }
            else if (translatable && line.equals(Tag.CONDITIONAL_END)) // > #
                                                                       // end of
                                                                       // Conditional
            {
                tmpString.append(lineWithWhiteSpace);
                addTranslatableTmx(handleInlineTag(tmpString.toString(),
                        Tag.CONDITIONAL, true));
                tmpString.setLength(0);
                inConditional = false;
            }
            else if (translatable
                    && (inConditional || line.startsWith(Tag.CONDITIONAL_HEAD))) // <Conditional
            {
                endStringTag(output);
                inConditional = true;
                tmpString.append(lineWithWhiteSpace).append("\n");
            }
            else if (translatable && line.startsWith(Tag.UNCONDITIONAL)) // <Unconditional
            {
                endStringTag(output);
                addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                        Tag.CONDITIONAL, true));
            }
            else if (line.equals(Tag.NOTES_HEAD)) // <Notes
            {
                translatableOutsideNote = translatable;
                translatable = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.NOTES_END)) // > # end of Notes
            {
                translatable = translatableOutsideNote;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (translatable && line.startsWith(Tag.NOTES_REF)
                    && line.endsWith(">")) // <FNote
            {
                if (inString)
                {
                    addTranslatableTmx(getStringPhEnd());
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.NOTES, true));
                    addTranslatableTmx(getStringPhStart());
                }
                else
                {
                    addTranslatableTmx(handleInlineTag(lineWithWhiteSpace,
                            Tag.NOTES, true));
                }
            }
            else if (line.equals(Tag.PGFTAG_CALLOUT))
            {
                inCalloutPgfTag = true;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else if (line.equals(Tag.PARA_END))
            {
                // As callout pgfTag is always in "para", when para ends, set
                // this to false.
                inCalloutPgfTag = false;
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
            else
            {
                endStringTag(output);
                addSkeleton(output, lineWithWhiteSpace, WITH_RETURN);
            }
        }
    }

    private void endStringTag(Output output)
    {
        if (inString)
        {
            addTranslatableTmx(getStringPhEnd());
            inString = false;
        }
    }

    private String getStringPhStart()
    {
        StringBuffer stuff = new StringBuffer();

        stuff.append("<ph type=\"" + Tag.STRING + "\" id=\"" + index
                + "\" x=\"" + index++ + "\">");
        stuff.append(m_xmlEncoder.encodeStringBasic(Tag.STRING_HEAD + " `"))
                .append("</ph>");

        return stuff.toString();
    }

    private String getStringPhEnd()
    {
        StringBuffer stuff = new StringBuffer();

        stuff.append("<ph type=\"" + Tag.STRING + "\" id=\"" + index
                + "\" x=\"" + index++ + "\">");
        stuff.append(m_xmlEncoder.encodeStringBasic(Tag.STRING_END))
                .append("\n").append("</ph>");

        return stuff.toString();
    }

    private void exposeDef(String line, Output output, String tagType)
    {
        int start = line.indexOf("`");
        int end = line.indexOf("'");
        if (start != -1 && end != -1)
        {
            String middle = line.substring(start + 1, end);
            String head = line.substring(0, start + 1);
            String tail = line.substring(end);

            addSkeleton(output, head, NO_RETURN);// add head as skeleton, with
                                                 // no return

            /*
             * pattern contains 4 kinds: 1: <$marker2\\> 2:
             * <Emphasis\\>A15-24<Default Font\\> 3: normal string We should 4:
             * a:Chapter <n=3\>< \>< \>< \>< \> check whether it matches all
             * patterns.
             */
            if (middle.matches(regexp2))
            {
                addSkeleton(output, middle.substring(0, 2), NO_RETURN);// add
                                                                       // the
                                                                       // front
                                                                       // 2
                                                                       // characters
                                                                       // to
                                                                       // skeleton
                middle = middle.substring(2); // remove the front 2 characters
            }

            String def = this.replaceSpecialCharactor(middle, true);
            if (def.length() == 0)
            {
                this.addSkeleton(output, tail, WITH_RETURN);
                return;
            }
            if (def.matches(regexp1))
            {
                String syntax;
                String inline = def;// String contains <ph>...</ph>

                try
                {
                    inline = replaceUnicodeCharacter(inline);
                    while ((syntax = getSyntax(inline, regexp1)) != null)
                    {
                        String inlineSyntax = this.handleInlineTag(syntax,
                                tagType, false);
                        start = inline.lastIndexOf(syntax);
                        inline = inline.substring(0, start) + inlineSyntax
                                + inline.substring(start + syntax.length());
                    }
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    this.addSkeleton(output, middle, NO_RETURN);
                    this.addSkeleton(output, tail, WITH_RETURN);
                    return;
                }

                inline = revertUnicodeCharacter(inline);
                String meaningful = getContentsBetweenPh(inline);
                if (meaningful.trim().matches("\\W*"))
                {
                    /*
                     * some variable should not be exposed for translation, for
                     * example -- <VariableDef
                     * `<$monthnum\>/<$daynum\>/<$shortyear\>'>, it will
                     * transfer to "[x1]/[x2]/[x3]", which has no meaning.
                     */
                    this.addSkeleton(output, middle, NO_RETURN);
                }
                else
                {
                    /*
                     * if the def contains charactors
                     */
                    String encodedMeaningful = encodeMeaningful(inline);
                    addTranslatableTmx(encodedMeaningful);
                }
            }
            else
            {
                addTranslatable(middle);
            }
            this.addSkeleton(output, tail, WITH_RETURN);// add tail as skeleton
        }
        else
        {
            this.addSkeleton(output, line, WITH_RETURN);// add whole as skeleton
        }
    }

    /**
     * Transfer \xnn and \\t into inline tags
     * 
     * @param content
     * @param protectDblQuotations
     *            - boolean: if there are unicode "double quotation marks", put
     *            them into PH tags.
     * @return
     */
    private String replaceSpecialCharactor(String content,
            boolean protectDblQuotations)
    {
        String regexp = "\\\\x[\\w]{2}[\\s]?";
        java.util.regex.Pattern PATTERN = java.util.regex.Pattern
                .compile(regexp);
        Matcher m = PATTERN.matcher(content);
        String result = null;
        List<String> transfered = new ArrayList<String>();
        while (m.find())
        {
            result = m.group(0);
            /*
             * Remember, transfered \xnn pattern string will still in the \xnn
             * pattern, so in order to avoid transfering again and again, we
             * should know whether the match has been transfered already.
             */
            if (!transfered.contains(result))
            {
                transfered.add(result);
                content = content.replace(result,
                        handleInlineTag(result, "text", false));
            }
        }

        String regexp1 = "[\\\\]{1,2}t";
        PATTERN = java.util.regex.Pattern.compile(regexp1);
        m = PATTERN.matcher(content);
        if (m.find())
        {
            result = m.group(0);
            content = content.replace(result,
                    handleInlineTag(result, "text", false));
        }

        if (protectDblQuotations)
        {
            // if there are unicode "double quotation marks", put them into PH
            // tags.
            boolean isInTag = false;
            StringBuilder newContent = new StringBuilder();
            char[] chars = content.toCharArray();
            for (int i = 0; i < chars.length; i++)
            {
                char ch = chars[i];
                if (!isInTag && (ch == '\u201c' || ch == '\u201d'))
                {
                    newContent.append(handleInlineTag(String.valueOf(ch),
                            "text", false));
                }
                else
                {
                    newContent.append(ch);
                    if (ch == '<')
                    {
                        isInTag = true;
                    }
                    else if (ch == '>')
                    {
                        isInTag = false;
                    }
                }
            }

            return newContent.toString();
        }
        else
        {
            return content;
        }
    }

    /**
     * Get last string which is in the regexp pattern
     * 
     * @param line
     * @param regexp
     * @return
     */
    private String getSyntax(String line, String regexp)
            throws ArrayIndexOutOfBoundsException
    {
        try
        {
            PatternCompiler compiler = new Perl5Compiler();
            Pattern pattern = compiler.compile(regexp,
                    Perl5Compiler.CASE_INSENSITIVE_MASK);

            PatternMatcher matcher = new Perl5Matcher();

            if (matcher.contains(line, pattern))
            {
                MatchResult result = matcher.getMatch();
                String reg = result.group(1);
                return reg;
            }
            else
            {
                return null;
            }
        }
        catch (MalformedPatternException e)
        {
            return null;
        }
    }

    /**
     * Transfer tags into inline tags with &lt;ph&gt;
     * 
     * @param string
     *            the String content of the file
     * @param tagType
     * @param newline
     *            whether the end contains a \n
     * @return
     */
    private String handleInlineTag(String string, String tagType,
            boolean newline)
    {
        StringBuffer stuff = new StringBuffer();
        stuff.append("<ph type=\"" + tagType + "\" id=\"" + index + "\" x=\""
                + index++ + "\">");
        stuff.append(m_xmlEncoder.encodeStringBasic(string));
        if (newline)
        {
            stuff.append("\n");
        }
        stuff.append("</ph>");
        return stuff.toString();
    }

    /**
     * Replace special charactors
     * 
     * @param line
     * @return
     */
    private String handleChar(String line)
    {
        String key = Parser.getTagContent(line, Tag.CHAR_HEAD);

        // hardreturn, Tab, HardSpace will convert to inline tags
        if (key.equalsIgnoreCase("HardReturn") || key.equalsIgnoreCase("Tab")
                || key.equalsIgnoreCase("HardSpace"))
        {
            if (!key.equalsIgnoreCase("HardReturn"))
            {
                inString = false;
            }
            StringBuffer stuff = new StringBuffer();

            stuff.append(getStringPhEnd()); // end upper string line
            stuff.append("<ph type=\"" + Tag.CHAR + "\" id=\"" + index
                    + "\" x=\"" + index++ + "\">");
            stuff.append(m_xmlEncoder.encodeStringBasic(line));

            stuff.append("\n");
            if (key.equalsIgnoreCase("HardReturn"))
            {
                // append the next piece of string
                stuff.append(m_xmlEncoder.encodeStringBasic(Tag.PARALINE_END))
                        .append("\n")
                        .append(m_xmlEncoder
                                .encodeStringBasic(Tag.PARALINE_HEAD))
                        .append("\n")
                        .append(m_xmlEncoder.encodeStringBasic(Tag.STRING_HEAD
                                + " `"));
            }
            stuff.append("</ph>");

            return stuff.toString();
        }

        String replace = specialCharMap.get(key);
        if (replace != null)
        {
            return replace;
        }
        else
        {
            return " ";
        }
    }

    /**
     * Add skeletons to output with line warpping
     * 
     * @param output
     * @param line
     */
    private void addSkeleton(Output output, String line, boolean newLine)
    {
        // add translatable segement first
        handleTranslatableSegements(output);

        // add skeleton segment
        output.addSkeleton(line);
        if (newLine)
        {
            output.addSkeleton("\n");
        }
    }

    /**
     * Add translatable segments to output
     * 
     * @param output
     */
    private void handleTranslatableSegements(Output output)
    {
        String trans = storage.toString();
        if (trans.length() != 0)
        {
            if (trans.trim().length() == 0)
            {
                output.addSkeleton(trans);
            }
            else if (trans.contains("<ph") && trans.contains("</ph>"))
            {
                trans = getContentsBetweenPh(trans);

                if (trans.trim().length() == 0)
                {
                    String cont = getContentsInPh(storage.toString());
                    output.addSkeleton(m_xmlEncoder.decodeStringBasic(cont));
                }
                else
                {
                    mergeContinuousInlineTags(output, storage.toString());
                }
            }
            else
            {
                output.addTranslatableTmx(storage.toString().trim());
            }
        }
        storage.setLength(0);
    }

    private void mergeContinuousInlineTags(Output output, String content)
    {
        // remove head ph
        String[] headAndContent = removeHeadingPh(content);
        if (!headAndContent[0].equals(""))
        {
            output.addSkeleton(m_xmlEncoder
                    .decodeStringBasic(headAndContent[0]));
        }
        // remove tail ph
        String[] tailAndContent = removeEndingPh(headAndContent[1]);
        // merge continuous inline tags
        content = tailAndContent[1].replaceAll(continusInlineTag, "");
        if (m_xmlEncoder.decodeStringBasic(content).matches("\\W*"))
        {
            content = m_xmlEncoder.decodeStringBasic(content);
            content = content.replace("\\", "\\\\").replace("'", "\\q")
                    .replace("`", "\\Q").replace("&gt;", "\\>")
                    .replace(">", "\\>");
            output.addSkeleton(content);
        }
        else
        {
            // GBS-2622 problem #3.
            content = changeTypeForSuperscript(content);
            output.addTranslatableTmx(content);
        }
        if (!tailAndContent[0].equals(""))
        {
            output.addSkeleton(m_xmlEncoder
                    .decodeStringBasic(tailAndContent[0]));
        }
    }

    /**
     * If the translatable content has "Superscript",change its default "type"
     * to "superscript".
     */
    private String changeTypeForSuperscript(String p_content)
    {
        if (p_content == null || p_content.indexOf("Superscript") == -1)
        {
            return p_content;
        }

        try
        {
            StringBuilder result = new StringBuilder();
            String strFirst, strMiddle, strLast;
            strLast = new String(p_content);
            int phStartIndex = -1;
            int phEndIndex = -1;
            while (strLast.indexOf("Superscript") > -1)
            {
                phStartIndex = strLast.indexOf("<ph");
                phEndIndex = strLast.indexOf("</ph>");

                strFirst = strLast.substring(0, phStartIndex);
                strMiddle = strLast.substring(phStartIndex, phEndIndex + 5);
                strLast = strLast.substring(phEndIndex + 5);

                result.append(strFirst);
                if (strMiddle.indexOf("Superscript") == -1)
                {
                    result.append(strMiddle);
                }
                else
                {
                    result.append(changeType(strMiddle));
                }

                phStartIndex = -1;
                phEndIndex = -1;
                strFirst = "";
                strMiddle = "";

                changeTypeForSuperscript(strLast);
            }

            if (strLast.length() > 0)
            {
                result.append(strLast);
            }

            return result.toString();
        }
        catch (Exception e)
        {
            logger.warn("Error when changeTypeForSuperscript() for "
                    + p_content);
            return p_content;
        }

    }

    private String changeType(String p_str)
    {
        return p_str.replaceFirst("type=\"text\"", "type=\"superscript\"");
    }

    /**
     * Remove heading inline tags
     * 
     * @param content
     * @return
     */
    private String[] removeHeadingPh(String content)
    {
        StringBuffer head = new StringBuffer("");
        while (content.trim().startsWith("<ph"))
        {
            int start = content.indexOf(">");
            int end = content.indexOf("</ph>");
            String firstInlineContent = content.substring(start + 1, end);

            if (inlineTags.contains(firstInlineContent)
                    || firstInlineContent.contains("<sub"))
            {
                break;
            }
            head.append(firstInlineContent);
            content = content.substring(end + "</ph>".length());
        }
        return new String[]
        { head.toString(), content };
    }

    /**
     * Remove ending inline tags
     * 
     * @param content
     * @return
     */
    private String[] removeEndingPh(String content)
    {
        String tail = "";
        while (content.trim().endsWith("</ph>"))
        {
            String tmp = content.substring(content.lastIndexOf("<ph"));
            if (tmp.contains("<sub"))
            {
                break;
            }
            String lastInlineContent = this.getContentsInPh(tmp);
            if (inlineTags.contains(lastInlineContent))
            {
                break;
            }
            tail = lastInlineContent + tail;
            content = content.substring(0, content.lastIndexOf(tmp));
        }
        return new String[]
        { tail, content };
    }

    /**
     * The method is used to encode meaningful part of VariableDef tag
     * 
     * @param content
     * @return
     */
    private String encodeMeaningful(String content)
    {
        StringBuffer result = new StringBuffer();
        while (content.indexOf("</ph>") != -1)
        {
            String phTag = content.substring(content.indexOf("<ph"),
                    content.indexOf("</ph>") + "</ph>".length());
            String beforePh = content.substring(0, content.indexOf("<ph"));
            result.append(m_xmlEncoder.encodeStringBasic(beforePh)).append(
                    phTag);
            content = content.substring(content.indexOf("</ph>")
                    + "</ph>".length());
        }
        result.append(m_xmlEncoder.encodeStringBasic(content));
        return result.toString();
    }

    /**
     * Get contents between inline tags. Example: content:abc<ph
     * type=...>def</ph>ghi return: abcghi return: abcghi
     * 
     * @param content
     * @return string
     */
    private String getContentsBetweenPh(String content)
    {
        while (content.contains("</ph>"))
        {
            int start = content.indexOf("<ph");
            int end = content.indexOf("</ph>");

            content = content.substring(0, start)
                    + content.substring(end + "</ph>".length());
        }

        return content;
    }

    /**
     * Get contents in inline tags Example:<ph type=...>abc</ph><ph
     * type=...>def</ph> Return:abcdef
     * 
     * @param content
     * @return
     */
    private String getContentsInPh(String content)
    {
        while (content.contains("</ph>"))
        {
            int start1 = content.indexOf("<ph");
            int start = content.indexOf(">", start1);
            int end = content.indexOf("</ph>");

            content = content.substring(0, start1)
                    + content.substring(start + 1, end)
                    + content.substring(end + "</ph>".length());
        }

        while (content.contains("</sub>"))
        {
            int start1 = content.indexOf("<sub");
            int start = content.indexOf(">", start1);
            int end = content.indexOf("</sub>");

            content = content.substring(0, start1)
                    + content.substring(start + 1, end)
                    + content.substring(end + "</sub>".length());
        }
        return content;
    }

    private void addTranslatableTmx(String content)
    {
        storage.append(content);
    }

    private void addTranslatable(String content)
    {
        String tmp = m_xmlEncoder.encodeStringBasic(content);
        tmp = this.replaceSpecialCharactor(tmp, false);
        addTranslatableTmx(tmp);
    }

    /**
     * Expose PgfNumString for translation Get string value and replace special
     * charactors. After this, check whether the content contains meaningful
     * charactors. If true, add the content to translatable, else add the
     * content to skeleton.
     * <p>
     * Because of GBS-3209, we found "&lt;PgfNumString `string'&gt;" needs to be
     * changed to "&lt;Pgf &lt;PgfNumFormat `string'&gt; &gt; # end of Pgf" so
     * that the translation can be updated to exported mif or fm file.
     */
    private void handleNumString(String line, Output output)
    {
        String content = Parser.getStringContent(line);
        int start = line.indexOf("`");
        int end = line.indexOf("'");
        boolean transform = false;
        if (start != -1 && end != -1)
        {
            String head = line.substring(0, start + 1);
            String tail = line.substring(end);

            if (content.length() > 0)
            {
                String tmp = getContentsBetweenPh(replaceSpecialCharactor(
                        content, false));
                if (tmp.trim().matches("\\W*"))
                {
                    this.addSkeleton(output, head, NO_RETURN);
                    // add content as skeleton, with no return
                    this.addSkeleton(output, content, NO_RETURN);
                }
                else
                {
                    transform = true;
                    this.addSkeleton(output, Tag.PGF_HEAD, WITH_RETURN);
                    this.addSkeleton(output, Tag.PGF_NUMBER_FORMAT + " `",
                            NO_RETURN);
                    this.addTranslatable(content);
                }
            }
            else
            {
                this.addSkeleton(output, head, NO_RETURN);
                this.addSkeleton(output, content, NO_RETURN);
            }
            addSkeleton(output, tail, WITH_RETURN);
            if (transform)
            {
                addSkeleton(output, Tag.PGF_END, WITH_RETURN);
            }
        }
    }

    /**
     * If the MTypeName of Marker is index, make MText subflow
     * 
     * @param marker
     * @return
     */
    private String handleMarkerTag(Marker marker)
    {
        StringBuffer stuff = new StringBuffer();

        stuff.append("<ph type=\"" + Tag.MARKER + "\" id=\"" + index
                + "\" x=\"" + index++ + "\">");
        stuff.append(m_xmlEncoder.encodeStringBasic("<Marker\n"));
        if (marker.getMType() != null)
        {
            stuff.append(m_xmlEncoder.encodeStringBasic("<MType "
                    + marker.getMType() + ">\n"));
        }
        if (marker.getMTypeName() != null)
        {
            stuff.append(m_xmlEncoder.encodeStringBasic("<MTypeName `"
                    + marker.getMTypeName() + "'>\n"));
        }
        if (marker.getMText() != null)
        {
            if (marker.getMTypeName() != null
                    && marker.getMTypeName().equals("Index"))
            {
                stuff.append(m_xmlEncoder.encodeStringBasic("<MText `"))
                        .append("<sub type=\"text\" locType=\"translatable\">")
                        .append(m_xmlEncoder.encodeStringBasic(marker
                                .getMText())).append("</sub>")
                        .append(m_xmlEncoder.encodeStringBasic("'>\n"));
            }
            else
            {
                stuff.append(m_xmlEncoder.encodeStringBasic("<MText `"
                        + marker.getMText() + "'>\n"));
            }
        }
        if (marker.getMCurrPage() != null)
        {
            stuff.append(m_xmlEncoder.encodeStringBasic("<MCurrPage `"
                    + marker.getMCurrPage() + "'>\n"));
        }
        if (marker.getUnique() != null)
        {
            stuff.append(m_xmlEncoder.encodeStringBasic("<Unique "
                    + marker.getUnique() + ">\n"));
        }
        stuff.append(m_xmlEncoder.encodeStringBasic("> # end of Marker"))
                .append("\n").append("</ph>");

        return stuff.toString();
    }

    private void reset()
    {
        inString = false;
        inFontTag = false;
        inXrefTag = false;
        inMarker = false;
        inPage = false;
        translatable = false;
        inFrame = 0;
        inTextRect = false;
        inGroup = false;
        inVariable = false;
        inConditional = false;
        inCalloutPgfTag = false;
    }

    private class Marker
    {
        String mTypeName;
        String mText;
        String unique;
        String mType;
        String mCurrPage;

        public String getMTypeName()
        {
            return mTypeName;
        }

        public void setMTypeName(String typeName)
        {
            mTypeName = typeName;
        }

        public String getMText()
        {
            return mText;
        }

        public void setMText(String text)
        {
            mText = text;
        }

        public String getUnique()
        {
            return unique;
        }

        public void setUnique(String unique)
        {
            this.unique = unique;
        }

        public String getMType()
        {
            return mType;
        }

        public void setMType(String type)
        {
            mType = type;
        }

        public String getMCurrPage()
        {
            return mCurrPage;
        }

        public void setMCurrPage(String currPage)
        {
            mCurrPage = currPage;
        }
    }

    private class TextRectInFrame
    {
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        public void saveRect(String frameId, String textRectId)
        {
            List<String> existList = map.get(frameId);
            if (existList == null)
            {
                existList = new ArrayList<String>();
            }
            existList.add(textRectId);
            map.put(frameId, existList);
        }

        public List<String> getTextRectInFrame(String frameId)
        {
            List<String> tmp = map.get(frameId);
            if (tmp == null)
            {
                return new ArrayList<String>();
            }
            return map.get(frameId);
        }
    }

    private String replaceUnicodeCharacter(String inline)
    {
        if (inline == null)
        {
            return null;
        }

        inline = inline.replace("\u201c", "_U201C_");
        inline = inline.replace("\u201d", "_U201D_");

        return inline;
    }

    private String revertUnicodeCharacter(String inline)
    {
        if (inline == null)
        {
            return null;
        }

        inline = inline.replace("_U201C_", "\u201c");
        inline = inline.replace("_U201D_", "\u201d");

        return inline;
    }

    public void loadRules() throws ExtractorException
    {
        // String str_rules = getInput().getRules();
        // m_rules.loadRules(str_rules);
        // m_rules.loadRules(getDynamicRules());
    }

}
