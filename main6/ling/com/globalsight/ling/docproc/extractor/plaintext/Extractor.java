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
package com.globalsight.ling.docproc.extractor.plaintext;

import java.io.File;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.CustomTextRule;
import com.globalsight.cxe.entity.filterconfiguration.CustomTextRuleBase;
import com.globalsight.cxe.entity.filterconfiguration.CustomTextRuleHelper;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilter;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilterParser;
import com.globalsight.ling.common.PTEscapeSequence;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.LineIndex;
import com.globalsight.ling.docproc.LineString;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

/**
 * <p>
 * Plain text file extractor.
 * </p>
 *
 * <p>
 * <b>Note:</b> The conversion of certain format related characters into
 * diplomat tags is handled by a separate tag controller object - referred to as
 * the TmxController. The controller has static rules that govern the creation
 * of certain diplomat tags. The static rules are specific to this parser. They
 * can be changed by editing the flags in the TmxController class.
 * </p>
 */
public class Extractor extends AbstractExtractor implements
        ExtractorExceptionConstants, PTTmxControllerConstants
{
    static private final Logger s_logger = Logger.getLogger(Extractor.class);

    static public boolean s_breakOnSingleCR = false;
    static public boolean s_keepEmbeddedCR = true;

    private List<CustomTextRuleBase> m_customTextRules = null;
    private List<CustomTextRuleBase> m_customSidRules = null;
    // GBS-3881
    private Filter m_elementPostFilter = null;
    private String m_postFormat = null;
    private static String[] invalidHtmlTagCharacters = new String[]
    { "{", "}", "%", "^", "~", "!", "&", "*", "(", ")", "?" };
    private static final String PLACEHOLDER_LEFT_TAG = "GS_PLACEHOLDER_LEFT_TAG";
    private static final String PLACEHOLDER_RIGHT_TAG = "GS_PLACEHOLDER_RIGHT_TAG";
    private static final String PLACEHOLDER_LEFT_NATIVE = "GS_PLACEHOLDER_LEFT_NATIVE";
    private static final String PLACEHOLDER_RIGHT_NATIVE = "GS_PLACEHOLDER_RIGHT_NATIVE";

    static
    {
        try
        {
            ResourceBundle res = ResourceBundle.getBundle(
                    "properties/Diplomat", Locale.US);

            String value;

            try
            {
                value = res.getString("plaintext_break_on_eol");
                if (value.equalsIgnoreCase("true"))
                {
                    s_breakOnSingleCR = true;
                }
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("plaintext_keep_cr");
                if (value.equalsIgnoreCase("false"))
                {
                    s_keepEmbeddedCR = false;
                }
            }
            catch (MissingResourceException e)
            {
            }
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    /**
     * <p>
     * Line-segmenting flag. When true, this flag triggers "chunk level"
     * segmenting on a single carriage return. When false "chunk level"
     * segmenting is triggered on the double carriage return.
     * </p>
     */
    public boolean m_breakOnSingleCR = s_breakOnSingleCR;

    /**
     * <p>
     * When m_bBreakOnSingleCR is disabled, this flag controls what happens to
     * to embedded carriage returns. When this flag is true, carrage returns are
     * maintained. When set to false, each carriage return is converted to a
     * single space.
     * </p>
     */
    public boolean m_keepEmbeddedCR = s_keepEmbeddedCR;

    public Extractor()
    {
        super();
    }

    /**
     * <p>
     * Adds the tokens for a given translatable segment to the output object.
     * Escapes are encoded here after the extractor has made its determination
     * about embedded carriage returns and/or applied space substitution.
     * </p>
     *
     * <p>
     * Conversion of format related tokens to diplomat tags is handled by a
     * dynamically created tag controller object. The tag controller has static
     * rules that govern the creation of diplomat format tags. Since our format
     * tags borrow from the TMX specification the controller object was named
     * PTTmxController.
     * </p>
     *
     * @param p_vTokens
     *            - A vector of PTToken objects.
     */
    private void addTokensToOutput(Vector p_vTokens, boolean p_postFiltered)
    {
        PTTmxController TmxCtrl = new PTTmxController();
        PTEscapeSequence PTEsc = new PTEscapeSequence();

        // we must apply the rules to the tokens once before building
        // any tags
        TmxCtrl.applyRules(p_vTokens);

        Enumeration en = p_vTokens.elements();
        while (en.hasMoreElements())
        {
            PTToken Tok = (PTToken) en.nextElement();

            if (Tok.m_nType == PTToken.TEXT)
            {
                String content = Tok.m_strContent;
                if (p_postFiltered)
                {
                    content = StringUtil.replace(content, PLACEHOLDER_LEFT_TAG,
                            "<");
                    content = StringUtil.replace(content,
                            PLACEHOLDER_RIGHT_TAG, ">");
                    content = StringUtil.replace(content,
                            PLACEHOLDER_LEFT_NATIVE, "&lt;");
                    content = StringUtil.replace(content,
                            PLACEHOLDER_RIGHT_NATIVE, "&gt;");
                    getOutput().addTranslatableTmx(content, null, true,
                            m_postFormat);
                }
                else
                {
                    getOutput().addTranslatable(content);
                }
            }
            else
            {
                // returns false if there is no tagged version of the
                // character
                if (TmxCtrl.makeTmx(Tok.m_strContent.charAt(0), Tok.m_nPos))
                {
                    getOutput().addTranslatableTmx(TmxCtrl.getStart());
                    getOutput().addTranslatable(PTEsc.encode(Tok.m_strContent));
                    getOutput().addTranslatableTmx(TmxCtrl.getEnd());
                }
                else
                {
                    getOutput().addTranslatable(Tok.m_strContent);
                }
            }
        }

        p_vTokens.clear();

    }

    /**
     * <p>
     * This method is the top level entry point to start the extractor. The
     * output of the extraction process is stored in an Output object which was
     * passed to the extraction framework by the caller.
     * </p>
     *
     * @see com.globalsight.ling.docproc.AbstractExtractor
     *      docproc.AbstractExtractor
     * @see com.globalsight.ling.docproc.Output docproc.Output
     */
    public void extract() throws ExtractorException
    {
        try
        {
            getOutput().setDataFormat("plaintext");
            // GBS-3672
            Filter f = this.getMainFilter();
            PlainTextFilter pf = null;
            if (f != null && f instanceof PlainTextFilter)
            {
                pf = (PlainTextFilter) f;
            }

            if (pf != null)
            {
                PlainTextFilterParser parser = new PlainTextFilterParser(pf);
                parser.parserXml();
                m_customTextRules = parser.getCustomTextRules();
                m_customSidRules = parser.getCustomTextRuleSids();
                m_elementPostFilter = parser.getElementPostFilter();
                if (m_elementPostFilter != null)
                {
                    m_postFormat = FilterConstants.FILTER_TABLE_NAMES_FORMAT
                            .get(m_elementPostFilter.getFilterTableName());
                }
            }

            if (m_customTextRules != null && m_customTextRules.size() > 0)
            {
                boolean isMultiline = false;
                for (int i = 0; i < m_customTextRules.size(); i++)
                {
                    CustomTextRule rrr = (CustomTextRule) m_customTextRules
                            .get(i);

                    if (rrr.getIsMultiline())
                    {
                        isMultiline = true;
                        break;
                    }
                }

                String lineterminator = "\n";
                LineNumberReader lr = null;
                if (isMultiline)
                {
                    lr = new LineNumberReader(readInput());
                    StringBuffer allStr = new StringBuffer();
                    List<LineString> lines = new ArrayList<LineString>();
                    String line = lr.readLine();
                    int lineNumber = 1;

                    while (line != null)
                    {
                        lines.add(new LineString(line, lineNumber));
                        allStr.append(line);

                        line = lr.readLine();
                        ++lineNumber;

                        if (line != null)
                        {
                            allStr.append(lineterminator);
                        }
                    }

                    List<LineIndex> indexes = CustomTextRuleHelper
                            .extractLines(lines, allStr.length(),
                                    m_customTextRules, m_customSidRules);

                    if (indexes == null || indexes.size() == 0)
                    {
                        for (int i = 0; i < lines.size(); i++)
                        {
                            LineString lineStr = lines.get(i);

                            getOutput().addSkeleton(lineStr.getLine());
                            if (i != (lines.size() - 1))
                            {
                                getOutput().addSkeleton(lineterminator);
                            }
                        }
                    }
                    else
                    {
                        int start = 0;
                        for (int i = 0; i < indexes.size(); i++)
                        {
                            LineIndex lineIndex = indexes.get(i);

                            String s0 = allStr.substring(start,
                                    lineIndex.getContentStart());
                            String s1 = allStr.substring(
                                    lineIndex.getContentStart(),
                                    lineIndex.getContentEnd());
                            String sid = ((lineIndex.getSidStart() == -1) ? null
                                    : allStr.substring(lineIndex.getSidStart(),
                                            lineIndex.getSidEnd()));

                            if (s0 != null && s0.length() > 0)
                            {
                                getOutput().addSkeleton(s0);
                            }
                            if (s1 != null && s1.length() > 0)
                            {
                                if (m_elementPostFilter != null)
                                {
                                    gotoPostFilter(s1, sid);
                                }
                                else
                                {
                                    getOutput().addTranslatable(s1, sid);
                                }
                            }

                            start = lineIndex.getContentEnd();
                        }

                        if (start < allStr.length())
                        {
                            String endStr = allStr.substring(start,
                                    allStr.length());
                            getOutput().addSkeleton(endStr);
                        }
                    }
                }
                else
                {
                    // The Custom Text File filter should process files
                    // line-by-line.
                    lr = new LineNumberReader(readInput());
                    String line = lr.readLine();

                    while (line != null)
                    {
                        int[] index = CustomTextRuleHelper.extractOneLine(line,
                                m_customTextRules);
                        if (index == null)
                        {
                            getOutput().addSkeleton(line);
                        }
                        else if (index.length == 2 && index[0] < index[1])
                        {
                            String sid = null;
                            if (m_customSidRules != null
                                    && m_customSidRules.size() > 0)
                            {
                                int[] sidIndex = CustomTextRuleHelper
                                        .extractOneLine(line, m_customSidRules);
                                if (sidIndex != null && sidIndex.length == 2
                                        && sidIndex[0] < sidIndex[1])
                                {
                                    sid = line.substring(sidIndex[0],
                                            sidIndex[1]);
                                }
                            }

                            String s0 = line.substring(0, index[0]);
                            String s1 = line.substring(index[0], index[1]);
                            String s2 = line.substring(index[1]);
                            if (s0 != null && s0.length() > 0)
                            {
                                getOutput().addSkeleton(s0);
                            }
                            if (s1 != null && s1.length() > 0)
                            {
                                if (m_elementPostFilter != null)
                                {
                                    gotoPostFilter(s1, sid);
                                }
                                else
                                {
                                    getOutput().addTranslatable(s1, sid);
                                }
                            }
                            if (s2 != null && s2.length() > 0)
                            {
                                getOutput().addSkeleton(s2);
                            }
                        }
                        else
                        {
                            getOutput().addSkeleton(line);
                        }

                        line = lr.readLine();
                        if (line != null)
                        {
                            getOutput().addSkeleton(lineterminator);
                        }
                    }
                }

                if (lr != null)
                {
                    try
                    {
                        lr.close();
                    }
                    catch (Exception exxx)
                    {
                        // ignore
                    }
                }
            }
            else
            {
                if (m_elementPostFilter != null)
                {
                    gotoPostFilter();
                }
                else
                {
                    extractString(readInput(), false);
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(e);
            throw new ExtractorException(e);
        }
    }

    private void extractString(Reader p_input, boolean p_postFiltered)
    {
        int cVECTORSTART = 200;
        int cVECTORINC = 50;

        boolean bLeadingSequence = true;
        boolean bPrevWasCR = false;
        PTToken nextToken;
        Vector vTokenBuf = new Vector(cVECTORSTART, cVECTORINC);

        Parser parser = new Parser(p_input);
        PTToken token = parser.getNextToken();

        while (token.m_nType != PTToken.EOF)
        {
            if (token.m_nType == PTToken.TEXT)
            {
                // termination of leading whitespace
                bLeadingSequence = false;
            }
            else if ((token.m_nType == PTToken.LINEBREAK))
            {
                // termination of leading whitespace
                bLeadingSequence = false;

                if (m_breakOnSingleCR)
                {
                    // write vTokenBuf if not empty
                    addTokensToOutput(vTokenBuf, p_postFiltered);

                    // write CR as skel
                    getOutput().addSkeleton(token.m_strContent);
                    token = parser.getNextToken();
                    continue;
                }
                else
                {
                    if (bPrevWasCR)
                    {
                        // write vTokenBuf
                        addTokensToOutput(vTokenBuf, p_postFiltered);

                        // then read write CRs
                        // 1st - previous one
                        getOutput().addSkeleton(token.m_strContent);
                        // 2nd - current one
                        getOutput().addSkeleton(token.m_strContent);

                        // write additional trailing CRs
                        token = parser.getNextToken();
                        while (token.m_nType == PTToken.LINEBREAK)
                        {
                            getOutput().addSkeleton(token.m_strContent);
                            token = parser.getNextToken();
                        }
                        continue;
                    }
                    else
                    {
                        bPrevWasCR = true;

                        // What to do with embedded carriage returns?
                        nextToken = parser.getNextToken();

                        if ((nextToken.m_nType != PTToken.LINEBREAK)
                                && m_keepEmbeddedCR
                                && (token.m_nType != PTToken.EOF))
                        {
                            // keep it
                            vTokenBuf.add(token);
                        }
                        else if ((nextToken.m_nType != PTToken.LINEBREAK)
                                && !m_keepEmbeddedCR
                                && (token.m_nType != PTToken.EOF))
                        {
                            // convert it
                            vTokenBuf.add(new PTToken(PTToken.TEXT, " "));
                        }
                        token = nextToken;
                    }
                    continue;
                }
            }

            bPrevWasCR = false;
            vTokenBuf.add(token);
            token = parser.getNextToken();
        }

        addTokensToOutput(vTokenBuf, p_postFiltered);
    }

    /**
     * Goes to element post-filter for extraction with given string.
     * 
     * @since GBS-3881
     */
    private void gotoPostFilter(String str, String sid)
    {
        str = protectInvalidTags(str);
        Output output = switchExtractor(str, m_postFormat, m_elementPostFilter);
        Iterator it = output.documentElementIterator();
        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement) it.next();
            switch (element.type())
            {
                case DocumentElement.TRANSLATABLE:
                case DocumentElement.LOCALIZABLE:
                    Segmentable segmentableElement = (Segmentable) element;
                    String chunk = segmentableElement.getChunk();
                    chunk = StringUtil.replace(chunk, PLACEHOLDER_LEFT_NATIVE,
                            "&lt;");
                    chunk = StringUtil.replace(chunk, PLACEHOLDER_RIGHT_NATIVE,
                            "&gt;");
                    segmentableElement.setChunk(chunk);
                    if (sid != null && sid.length() > 0)
                    {
                        segmentableElement.setSid(sid);
                    }
                    getOutput().addDocumentElement(element, true);
                    break;

                case DocumentElement.SKELETON:
                    String skeleton = ((SkeletonElement) element).getSkeleton();
                    getOutput().addSkeletonTmx(skeleton);
                    break;
            }
        }
    }

    /**
     * Goes to element post-filter for extraction.
     * 
     * @since GBS-3881
     */
    private void gotoPostFilter() throws Exception
    {
        File f = getInput().getFile();
        if (f == null)
        {
            return;
        }
        String content = FileUtil.readFile(f, "utf-8");
        content = protectInvalidTags(content);
        Output output = switchExtractor(content, m_postFormat,
                m_elementPostFilter);
        Iterator it = output.documentElementIterator();
        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement) it.next();
            switch (element.type())
            {
                case DocumentElement.TRANSLATABLE:
                case DocumentElement.LOCALIZABLE:
                    Segmentable segmentableElement = (Segmentable) element;
                    String chunk = segmentableElement.getChunk();
                    // need to keep <bpt>, <ph>.. tag generated from post-filter
                    chunk = StringUtil
                            .replace(chunk, "<", PLACEHOLDER_LEFT_TAG);
                    chunk = StringUtil.replace(chunk, ">",
                            PLACEHOLDER_RIGHT_TAG);
                    extractString(new StringReader(chunk), true);
                    break;

                case DocumentElement.SKELETON:
                    String skeleton = ((SkeletonElement) element).getSkeleton();
                    getOutput().addSkeletonTmx(skeleton);
                    break;
            }
        }
    }

    /**
     * Protects invalid html tags, like <>, <{0}> before going into html
     * post-filter.
     * 
     * @since GBS-3881
     */
    private String protectInvalidTags(String content)
    {
        Pattern p = Pattern.compile("<([^>]*?)>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            boolean isInvalidTag = false;
            String tag = m.group(1);
            if (StringUtil.isEmpty(tag))
            {
                isInvalidTag = true;
            }
            else
            {
                for (int i = 0; i < tag.length(); i++)
                {
                    char c = tag.charAt(i);
                    if (StringUtil.isIncludedInArray(invalidHtmlTagCharacters,
                            String.valueOf(c)))
                    {
                        isInvalidTag = true;
                        break;
                    }
                }
            }
            if (isInvalidTag)
            {
                StringBuilder replaced = new StringBuilder();
                replaced.append(content.substring(0, m.start()));
                replaced.append(PLACEHOLDER_LEFT_NATIVE);
                replaced.append(tag);
                replaced.append(PLACEHOLDER_RIGHT_NATIVE);
                replaced.append(content.substring(m.end()));

                content = replaced.toString();
                m = p.matcher(content);
            }
        }
        return content;
    }

    /**
     * <p>
     * Part of Jim's rules engine idea. Still required by the
     * ExtractorInterface.
     * </p>
     */
    public void loadRules() throws ExtractorException
    {
    }
}
