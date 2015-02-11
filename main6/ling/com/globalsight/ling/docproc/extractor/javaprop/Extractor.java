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
package com.globalsight.ling.docproc.extractor.javaprop;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.PropertiesInternalText;
import com.globalsight.ling.common.JPEscapeSequence;
import com.globalsight.ling.common.JPMFEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;

public class Extractor
    extends AbstractExtractor
    implements ExtractorExceptionConstants,
               IFormatNames
{
    static private final Logger logger = Logger
            .getLogger(Extractor.class);
    //
    // Constants
    //
    public static final String EXTRACT_STANDARD = "GS_EXTRACT_STANDARD";
    public static final String EXTRACT_MSGFORMAT = "GS_EXTRACT_AS_MSG";
    public static final String EXTRACT_HTML = "GS_EXTRACT_AS_HTML";

    //
    // Member variables
    //
    private int m_valueType = JPConstants.JAVAPROP_STANDARD;
    private char m_prevChar1 = '\0';
    private char m_prevChar2 = '\0';
    private char m_prevChar3 = '\0';
    private JPEscapeSequence m_jpCodec = new JPEscapeSequence();
    private JPMFEscapeSequence m_mfCodec = new JPMFEscapeSequence();
    //private PropertiesInternalText internalText = null;
    private List<InternalText> internalTexts = null;
    private boolean useBptTag = true;
    
    private List<InternalText> getInternalTexts() throws Exception
    {
        if (internalTexts == null)
        {
            Filter filter = getMainFilter();
            if (filter != null && filter instanceof JavaPropertiesFilter)
            {
                long filterId = filter.getId();
                String filterTableName = filter.getFilterTableName();
                BaseFilter bf = BaseFilterManager.getBaseFilterByMapping(filterId, filterTableName);
                internalTexts = BaseFilterManager.getInternalTexts(bf);
                
                JavaPropertiesFilter jf = (JavaPropertiesFilter) filter;
                long scid = jf.getSecondFilterId();
                String scTableName = jf.getSecondFilterTableName();
                useBptTag = !FilterHelper.isFilterExist(scTableName, scid);
            }
        }
        
        return internalTexts;
    }
    
    public void setInternalTexts(List<InternalText> p_internalTexts)
    {
        internalTexts =p_internalTexts;        
    }
    
    private String handleInternalText(String s)
    {
        List<InternalText> _internalTexts = null;
        try
        {
            if (isDoSegBeforeInlText())
            {
                _internalTexts = null;
            }
            else
            {
                _internalTexts = getInternalTexts();
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getInternalTexts. ", e);
        }

        return InternalTextHelper.handleString(s, _internalTexts,
                FORMAT_JAVAPROP, useBptTag);
    }

    //
    // Constructors
    //

    public Extractor()
    {
        super();
    }

    //
    // Will be overwritten in classes derived from javaprop extractor
    // (html, Msg).
    //
    public void setFormat()
    {
        setMainFormat(FORMAT_JAVAPROP);
    }

    //
    // Interface Implementation -- ExtractorInterface
    //

    /**
     * <p>Extract method is the top level entry point to start the
     * extractor.  The output of the extraction process is stored in
     * an Output object which was passed to the extraction framework
     * by the caller.  See the AbstractExtractor class.</p>
     *
     * @throws com.globalsight.ling.docproc.ExtractorException
     */
    public void extract()
        throws ExtractorException
    {
        // Set the main format depending on which (derived) class
        // we're called in.
        setFormat();

        Parser parser = new Parser(readInput());
        JPToken token = parser.getNextToken();

        while (token.m_nType != JPToken.EOF)
        {
            if (token.m_nType == JPToken.KEY_VALUE && !exclude())
            {
            	// if trimSegment is checked in javaProperties filter, then trim it.
//            	if (getTrimSegmentFlag()) {
//            		token.m_strContent = token.m_strContent.trim();
//            	}
            	
                // Value tokens are now sent to appropriate value handler
                switch (m_valueType)
                {
                case JPConstants.JAVAPROP_STANDARD:
                    standardValueHandler(token);
                    break;
                case JPConstants.JAVAPROP_MSGFORMAT:
                    msgFormatValueHandler(token);
                    break;
                case JPConstants.JAVAPROP_HTML:
                    htmlValueHandler(token);
                    break;
                default:
                    break;
                }
            }
            else
            {
                if (token.m_nType == JPToken.PROP_COMMENT)
                {
                    String str_comment = token.m_strContent.substring(1);
                    // First, read GS_EXCLUDE/GS_EXTRACT comments
                    readMetaMarkup(str_comment);
                    readExtractionDirectiveComment(str_comment);
                }

                getOutput().addSkeleton(token.m_strContent);
            }

            token = parser.getNextToken();
        }
    }


    /**
     * Part of Jim's rules engine idea.  Still required by the
     * ExtractorInterface.
     */
    public void loadRules()
        throws ExtractorException
    {
    }

    //
    // Other public methods
    //

    /** Sets the type of processing to be done on the property value. */
    public void setValueType(int p_type)
    {
        m_valueType = p_type;
    }

     /**
      * <p>Switches the extractor to HTML to handle a stretch of HTML
      * codes embedded in the input.
      *
      * @param p_input: the input string that the extractor will parse
      */
    private void switchToHtml(String p_input)
        throws ExtractorException
    {
        Output output = this.switchExtractor(p_input, FORMAT_HTML);
        Iterator it = output.documentElementIterator();

        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement)it.next();
            switch (element.type())
            {
            case DocumentElement.SKELETON:
                SkeletonElement s = (SkeletonElement)element;
                getOutput().addSkeletonTmx(s.getSkeleton());
                break;
            case DocumentElement.TRANSLATABLE: // fall through
            case DocumentElement.LOCALIZABLE:
                // Overwrite data type to be JP+HTML combined.
                // TODO: ignores other combinations for the time being.
                Segmentable o = (Segmentable)element;
                if (o.getDataType() == null ||
                  o.getDataType().equals(FORMAT_HTML))
                {
                    o.setDataType(FORMAT_JAVAPROP_HTML);
                }
                getOutput().addDocumentElement(element);
                break;
            default:
                getOutput().addDocumentElement(element);
                break;
            }
        }
    }

    /**
     * Process the value string as an HTML snippet.
     * @param p_token: the value portion of a key/value pair
     */
    private void htmlValueHandler(JPToken p_token)
        throws ExtractorException
    {
        String value;

        try
        {
            value = m_jpCodec.decode(p_token.m_strContent);
        }
        catch (NativeEnDecoderException e)
        {
            e = new NativeEnDecoderException("JP error on line " +
              p_token.m_nInputLineNumber + ", cannot decode value:\n" +
              p_token.m_strContent);
            throw new ExtractorException(JP_ESCAPE_DECODE_ERROR, e);
        }

        try
        {
            switchToHtml(value);
        }
        catch (ExtractorException e)
        {
            // If HTML parser fails, process as standard javaprop
            // value - and pass on the original token.

            // System.err.println("HTML failed with " + e.toString());

            standardValueHandler(p_token);
        }
    }

    /**
     * Process the value string as a standard property value.
     * @param p_token: the value portion of a key/value pair
     */
    private void standardValueHandler(JPToken p_token)
        throws ExtractorException
    {
        javaPropValueHandler(p_token, false);
    }

    /**
     * Process the value as a standard property value
     * that uses the java message format.
     *
     * @param p_token: the value portion of a key/value pair
     */
    private void msgFormatValueHandler(JPToken p_token)
        throws ExtractorException
    {
        javaPropValueHandler(p_token, true);
    }

    /**
     * Process the value string as either a standard property value or
     * as a java message format value.
     *
     * @param p_token: the value portion of a key/value pair
     * @param p_isMessageFormat boolean true enables special handling
     * for java message format.
     */
    private void javaPropValueHandler(JPToken p_token,
      boolean p_isMessageFormat)
        throws ExtractorException
    {
        int x = 0, len = 0, p = 0, l = 0;
        char aChar;
        String input = p_token.m_strContent;
        String result = null;

        // Configure Tmx behavior
        JPTmxEncoder Tmx = new JPTmxEncoder();
        Tmx.setErasable(JPConstants.ALL_TMX_NON_ERASABLE);
        Tmx.enableTmxOnLeadingSpaces(true);

        // Decode escapes for all properties files
        try
        {
            m_jpCodec.setIsJavaProperty(true);
            input = m_jpCodec.decode(input);
        }
        catch (NativeEnDecoderException e)
        {
            e = new NativeEnDecoderException("JP error on line " +
              p_token.m_nInputLineNumber + ", cannot decode value:\n" +
              input);
            throw new ExtractorException(JP_ESCAPE_DECODE_ERROR, e);
        }

        // now add tmx and nonTmx chunks

        // NOTE: Parser currently does not return a "tokenized" value.
        // Instead it returns the entire raw content of the value.
        // So we have to parse tmx out here.

        len = input.length();
        aChar = input.charAt(x++);

        // detect leading whitespace
        while (Character.isWhitespace(aChar))
        {
            // true means leading whitespace
            getOutput().addTranslatableTmx(Tmx.makeTmx(aChar, true));

            if (x < len)
            {
                m_prevChar3 = m_prevChar2;
                m_prevChar2 = m_prevChar1;
                m_prevChar1 = aChar;
                aChar = input.charAt(x++);
            }
            else
            {
                break; // done with leading whitespace
            }
        }
        --x; // backup one
        aChar = m_prevChar1;
        m_prevChar1 = m_prevChar2;
        m_prevChar2 = m_prevChar3;

        // process remainder of value
        StringBuffer buff = new StringBuffer(len);
        while (x < len)
        {
            p = 0;
            m_prevChar3 = m_prevChar2;
            m_prevChar2 = m_prevChar1;
            m_prevChar1 = aChar;
            aChar = input.charAt(x++);

            // Detect tmx chunks ==============

            // 1) If we are in message format, check if it's a message
            // placeholder to be wrapped in tmx ?
            if (p_isMessageFormat && aChar == '{' &&
              (l = isPlaceholderStart(input.substring(x-1))) > 0)
            {
                result = Tmx.makeTmxForMsgPlaceholder(
                  input.substring(x-1, (x-1)+l));
                x = (x + l) - 1;
                m_prevChar1 = input.charAt(x-1);
                m_prevChar2 = input.charAt(x-2);
                m_prevChar3 = input.charAt(x-3);
            }
            else
            {
                // 2) Is it a single character to be wrapped in tmx ?
                result = Tmx.makeTmx(aChar, false);
            }

            // 3) If the current character is not tmx, we continue,
            // accumulating a buffer until we hit text to be encoded
            // in tmx or the end of the line.
            if (result.equals("" + aChar))
            {
                while (result.equals("" + aChar) && p == 0)
                {
                    buff.append(result);
                    if (x < len)
                    {
                        m_prevChar3 = m_prevChar2;
                        m_prevChar2 = m_prevChar1;
                        m_prevChar1 = aChar;
                        aChar = input.charAt(x++);

                        // Next two calls are used simply to detect
                        // the end of a run of non-tmx encoded text
                        // Both return values are checked at the top
                        // of the while loop.
                        result = Tmx.makeTmx(aChar, false);
                        if (p_isMessageFormat && (aChar == '{'))
                        {
                            p = isPlaceholderStart(input.substring(x-1));
                        }
                    }
                    else
                    {
                        break;
                    }
                }

                if (!result.equals("" + aChar) || p != 0)
                {
                    // we stopped because we encountered something to
                    // be wrapped in tmx
                    --x; // backup one
                    aChar = m_prevChar1;
                    m_prevChar1 = m_prevChar2;
                    m_prevChar2 = m_prevChar3;
                }

                // add the unprotected text
                if (p_isMessageFormat)
                {
                    try
                    {
                        getOutput().addTranslatable(
                          m_mfCodec.decode(buff.toString()));
                        buff.setLength(0);
                    }
                    catch (NativeEnDecoderException e)
                    {
                        e = new NativeEnDecoderException(
                          "JP error on line " + p_token.m_nInputLineNumber +
                          ", cannot decode value:\n" + buff.toString());
                        throw new ExtractorException(JP_ESCAPE_DECODE_ERROR,e);
                    }

                }
                else
                {
                    // everything is already decoded for standard javaprop
                    String value = buff.toString();
                    value = Tmx.encode(value);
                    value = handleInternalText(value);
                    getOutput().addTranslatableTmx(value);
                    buff.setLength(0);
                }
            }
            else
            {
                // add a tmx chunk
                getOutput().addTranslatableTmx(result);
            }
        } // continue parsing remainder of string for tmx

        // Finally, set the correct datatype for this value
        try
        {
            getOutput().setTranslatableAttrs(
              p_isMessageFormat == true ?
              FORMAT_JAVAPROP_MSG : FORMAT_JAVAPROP, null);
        }
        catch (DocumentElementException ignore)
        {
        }
    }

    /**
     * Identifies the start of a message placeholder run.
     * @param p_sub: the text to search.
     * @return int: the length of the run or 0 if not a placeholder
     */
    private int isPlaceholderStart(String p_sub)
    {
        int i=0, subLen=0, placeholderLen=0, openBracketCnt=0;

        // has to be at least three characters long to be a placeholder
        if ((subLen = p_sub.length()) < 3)  return 0;

        char aChar = p_sub.charAt(i);

        // Is this the start of a placeholder?  Opening bracket can be
        // escaped by quoting it: '{', but it is not escaped by a
        // preceding even number of single quotes (''{ -> '{).
        // Note we only recognize 2 quotes instead of any even number.
        if (aChar == '{' &&
          (m_prevChar1 != '\'' ||
            (m_prevChar1 == '\'' && m_prevChar2 == '\'')))
        {
            openBracketCnt++;
            placeholderLen++;

            while ((i < subLen) && openBracketCnt != 0)
            {
                aChar = p_sub.charAt(++i) ;
                if      (aChar == '{') openBracketCnt++;
                else if (aChar == '}') openBracketCnt--;
                placeholderLen++;
            }

            // if it was a message format placeholder we should be on '}'
            if (aChar != '}' )
            {
                placeholderLen = 0;
            }
        }

        return placeholderLen;
    }

    private void readExtractionDirectiveComment(String p_comment)
    {
        p_comment = p_comment.trim();
        if (p_comment.startsWith(EXTRACT_STANDARD))
        {
            m_valueType = JPConstants.JAVAPROP_STANDARD;
        }
        else if (p_comment.startsWith(EXTRACT_MSGFORMAT))
        {
            m_valueType = JPConstants.JAVAPROP_MSGFORMAT;
        }
        else if (p_comment.startsWith(EXTRACT_HTML))
        {
            m_valueType = JPConstants.JAVAPROP_HTML;
        }
    }

}
