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

import com.globalsight.cxe.entity.filterconfiguration.CustomTextRule;
import com.globalsight.cxe.entity.filterconfiguration.CustomTextRuleHelper;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilter;
import com.globalsight.cxe.entity.filterconfiguration.PlainTextFilterParser;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.PTEscapeSequence;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * <p>Plain text file extractor.</p>
 *
 * <p><b>Note:</b> The conversion of certain format related
 * characters into diplomat tags is handled by a separate tag
 * controller object - referred to as the TmxController.  The
 * controller has static rules that govern the creation of certain
 * diplomat tags. The static rules are specific to this parser. They
 * can be changed by editing the flags in the TmxController class.</p>
 */
public class Extractor
    extends AbstractExtractor
    implements ExtractorExceptionConstants,
               PTTmxControllerConstants
{
    static public boolean s_breakOnSingleCR = false;
    static public boolean s_keepEmbeddedCR = true;

    static {
        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle("properties/Diplomat", Locale.US);

            String value;

            try
            {
                value = res.getString("plaintext_break_on_eol");
                if (value.equalsIgnoreCase("true"))
                {
                    s_breakOnSingleCR = true;
                }
            }
            catch (MissingResourceException e) {}

            try
            {
                value = res.getString("plaintext_keep_cr");
                if (value.equalsIgnoreCase("false"))
                {
                    s_keepEmbeddedCR = false;
                }
            }
            catch (MissingResourceException e) {}
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    /**
     * <p>Line-segmenting flag.  When true, this flag triggers
     * "chunk level" segmenting on a single carriage return.  When
     * false "chunk level" segmenting is triggered on the double
     * carriage return.</p>
     */
    public boolean m_breakOnSingleCR = s_breakOnSingleCR;

    /**
     * <p>When m_bBreakOnSingleCR is disabled, this flag controls what
     * happens to to embedded carriage returns. When this flag is
     * true, carrage returns are maintained. When set to false, each
     * carriage return is converted to a single space.</p>
     */
    public boolean m_keepEmbeddedCR = s_keepEmbeddedCR;

    public Extractor()
    {
        super();
    }

    /**
     * <p>Adds the tokens for a given translatable segment to the
     * output object.  Escapes are encoded here after the extractor
     * has made its determination about embedded carriage returns
     * and/or applied space substitution.</p>
     *
     * <p>Conversion of format related tokens to diplomat tags is
     * handled by a dynamically created tag controller object. The tag
     * controller has static rules that govern the creation of
     * diplomat format tags. Since our format tags borrow from the TMX
     * specification the controller object was named
     * PTTmxController.</p>
     *
     * @param p_vTokens - A vector of PTToken objects.
     */
    private void addTokensToOutput(Vector p_vTokens)
    {
        PTTmxController TmxCtrl = new PTTmxController();
        PTEscapeSequence PTEsc = new PTEscapeSequence();

        // we must apply the rules to the tokens once before building
        // any tags
        TmxCtrl.applyRules(p_vTokens);

        Enumeration en = p_vTokens.elements();
        while (en.hasMoreElements())
        {
            PTToken Tok = (PTToken)en.nextElement();

            if (Tok.m_nType == PTToken.TEXT)
            {
                getOutput().addTranslatable(Tok.m_strContent);
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
     * <p>This method is the top level entry point to start the
     * extractor.  The output of the extraction process is stored in
     * an Output object which was passed to the extraction framework
     * by the caller.</p>
     *
     * @see com.globalsight.ling.docproc.AbstractExtractor
     *      docproc.AbstractExtractor
     * @see com.globalsight.ling.docproc.Output
     *      docproc.Output
     */
    public void extract()
        throws ExtractorException
    {
        getOutput().setDataFormat("plaintext");
        // GBS-3672
        Filter f = this.getMainFilter();
        PlainTextFilter pf = null;
        if (f != null && f instanceof PlainTextFilter)
        {
            pf = (PlainTextFilter) f;
        }

        List<CustomTextRule> customTextRules = null;
        if (pf != null)
        {
            try
            {
                PlainTextFilterParser parser = new PlainTextFilterParser(pf);
                parser.parserXml();
                customTextRules = parser.getCustomTextRules();
            }
            catch (Exception ex)
            {
                throw new ExtractorException(ex);
            }
        }

        if (customTextRules != null && customTextRules.size() > 0)
        {
            // The Custom Text File filter should process files line-by-line.
            LineNumberReader lr = new LineNumberReader(readInput());

            try
            {
                String lineterminator = "\n";
                String line = lr.readLine();

                while (line != null)
                {
                    int[] index = CustomTextRuleHelper.extractOneLine(line,
                            customTextRules);
                    if (index == null)
                    {
                        getOutput().addSkeleton(line);
                    }
                    else if (index.length == 2)
                    {
                        String s0 = line.substring(0, index[0]);
                        String s1 = line.substring(index[0], index[1]);
                        String s2 = line.substring(index[1]);
                        if (s0 != null && s0.length() > 0)
                        {
                            getOutput().addSkeleton(s0);
                        }
                        if (s1 != null && s1.length() > 0)
                        {
                            getOutput().addTranslatable(s1);
                        }
                        if (s2 != null && s2.length() > 0)
                        {
                            getOutput().addSkeleton(s2);
                        }
                    }

                    getOutput().addSkeleton(lineterminator);
                    line = lr.readLine();
                }
            }
            catch (Exception e)
            {
                throw new ExtractorException(e);
            }
        }
        // extract as before if no rule
        else
        {
            int cVECTORSTART = 200;
            int cVECTORINC = 50;

            boolean bLeadingSequence = true;
            boolean bPrevWasCR = false;
            PTToken nextToken;
            Vector vTokenBuf = new Vector(cVECTORSTART, cVECTORINC);

            Parser parser = new Parser(readInput());
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
                        addTokensToOutput(vTokenBuf);

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
                            addTokensToOutput(vTokenBuf);

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

            addTokensToOutput(vTokenBuf);
        }
    }

    /**
     * <p>Part of Jim's rules engine idea. Still required by the
     * ExtractorInterface.</p>
     */
    public void loadRules()
        throws ExtractorException
    {
    }
}
