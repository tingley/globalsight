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
package com.globalsight.ling.docproc.merger.html;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.merger.plaintext.PlaintextPostMergeProcessor;

/**
 * This class post processes a merged HTML document.
 */
public class HtmlPostMergeProcessor implements PostMergeProcessor
{
    private static final String PROCESS_ERROR = "HTML post merge process error";
    private static Logger theLogger = Logger.getLogger(PlaintextPostMergeProcessor.class);

    private String m_content = null;

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String,
     *      java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding) throws DiplomatMergerException
    {
        m_content = p_content;

        if (findHtmlTag())
        {
            putHeadTag();
            putMetaTag();
            putTitleTag();

            rewriteMetaTag(p_IanaEncoding);

            // The title tag is an exception in that it does not allow
            // embedded HTML tags. So if the merger inserted <span
            // dir="rtl"> for a bidi language, we have to undo it here.
            rewriteTitleTag();
        }
        else
        {
            m_content = null;
        }

        return m_content;
    }

    /**
     * Looks for <HTML> tag. Returns true if found, else false.
     */
    private boolean findHtmlTag()
    {
        boolean found = true;

        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(m_content, "<HTML[^>]*", false);

            // no html element found
            if (match == null)
            {
                found = false;
            }
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }

        return found;
    }

    /**
     * Looks for <HEAD> tag. If it's not found, the method puts it as well as
     * <BODY> tag.
     */
    private void putHeadTag()
    {
        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(m_content, "<HEAD", false);

            // no head element found
            if (match == null)
            {
                match = RegEx.matchSubstring(m_content, "<HTML[^>]*>", false);
                int htmlTagEnd = match == null ? 0 : match.endOffset(0);
                match = RegEx.matchSubstring(m_content, "</HTML>", false);
                int htmlEndTagBegin = match == null ? m_content.length() : match.beginOffset(0);

                // match =
                // RegEx.matchSubstring(m_content,
                // "(.*<HTML[^>]*>)(.*)(</HTML>.*)",
                // false, true);

                String prologue = m_content.substring(0, htmlTagEnd);
                String content = m_content.substring(htmlTagEnd, htmlEndTagBegin);
                String epilogue = m_content.substring(htmlEndTagBegin);

                // Added by Vincent Yan, 2010/04/27
                // To test if the source contains BODY tag
                RegExMatchInterface match1 = RegEx.matchSubstring(content, "<BODY", false);
                if (match1 == null)
                {
                    // no body element found
                    m_content = prologue + "\n<HEAD>\n</HEAD>\n<BODY>" + content + "</BODY>\n"
                            + epilogue;
                }
                else
                {
                    m_content = prologue + "\n<HEAD>\n</HEAD>\n" + content + "\n" + epilogue;
                }
            }
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }
    }

    /**
     * Looks for <META> tag with charset attribute. If it's not found, the
     * method puts it.
     */
    private void putMetaTag()
    {
        try
        {
            RegExMatchInterface match = RegEx
                    .matchSubstring(
                            m_content,
                            "<META\\s+([^>]*HTTP-EQUIV\\s*=\\s*['\"]?CONTENT-TYPE['\"]?[^>]+CHARSET\\s*=|[^>]+CHARSET\\s*=[^>]+HTTP-EQUIV\\s*=\\s*['\"]?CONTENT-TYPE['\"]?)",
                            false);

            // no charset specifier
            if (match == null)
            {
                match = RegEx.matchSubstring(m_content, "<HEAD[^>]*>", false);
                int headContentBegin = match.endOffset(0);

                String bodyStart = m_content.substring(0, headContentBegin);
                String rest = m_content.substring(headContentBegin);

                m_content = bodyStart
                        + "\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=ISO-8859-1\">"
                        + rest;
            }
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }
    }

    /**
     * Looks for <TITLE> tag. If it's not found, the method adds it.
     */
    private void putTitleTag()
    {
        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(m_content, "<TITLE\\s*[^>]*?>", false);

            // no title element
            if (match == null)
            {
                // <head> is guaranteed to exist, we've just added it.
                match = RegEx.matchSubstring(m_content, "<HEAD[^>]*>", false);
                int headContentBegin = match.endOffset(0);

                String bodyStart = m_content.substring(0, headContentBegin);
                String rest = m_content.substring(headContentBegin);

                m_content = bodyStart + "\n<TITLE></TITLE>\n" + rest;
            }
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }
    }

    /**
     * Looks for <META> tag and rewrites charset part.
     */
    private void rewriteMetaTag(String p_encoding) throws DiplomatMergerException
    {
        try
        {
            RegExMatchInterface match = RegEx
                    .matchSubstring(
                            m_content,
                            "<META\\s+([^>]*HTTP-EQUIV\\s*=\\s*['\"]?CONTENT-TYPE['\"]?[^>]+CHARSET\\s*=[\"'\\s]*([^>\"' ]+)|[^>]+CHARSET\\s*=[\"'\\s]*([^>\"' ]+)[^>]+HTTP-EQUIV\\s*=\\s*['\"]?CONTENT-TYPE['\"]?)",
                            false);

            // no charset specifier
            if (match == null)
            {
                // some exception
                throw new DiplomatMergerException("MetaTagNotFound", null, null);
            }

            int charsetValueEnd;
            int charsetValueBegin = match.beginOffset(2);
            if (charsetValueBegin == -1)
            {
                charsetValueBegin = match.beginOffset(3);
                charsetValueEnd = match.endOffset(3);
            }
            else
            {
                charsetValueEnd = match.endOffset(2);
            }

            String firstHalf = m_content.substring(0, charsetValueBegin);
            String secondHalf = m_content.substring(charsetValueEnd);

            // Logical ordered Hebrew encoding must be labeled "ISO-8859-8-i"
            // See http://www.w3.org/TR/html401/struct/dirlang.html#bidi88598
            if (p_encoding.compareToIgnoreCase("ISO-8859-8") == 0)
            {
                p_encoding = "ISO-8859-8-i";
            }

            m_content = firstHalf + p_encoding + secondHalf;
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }
    }

    /**
     * Looks for {@code <TITLE>} tag and rewrites any {@code <span dir="rtl">}
     * inside to {@code <title dir="rtl">}.
     */
    private void rewriteTitleTag() throws DiplomatMergerException
    {
        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(m_content,
                    "<TITLE\\s*[^>]*?>(.*?)</TITLE\\s*>", false, true);

            // no title tag
            if (match == null)
            {
                // Hey, we just inserted a title tag, but the regex
                // may be incorrect. We should throw an exception but
                // I'm not trusting the regexp code so I won't.
                return;

                // throw new DiplomatMergerException("TitleTagNotFound",
                // null, null);
            }

            int titleBegin = match.beginOffset(0);
            int titleEnd = match.endOffset(0);

            String title = m_content.substring(titleBegin, titleEnd);

            // This regexp depends upon the string inserted in
            // DiplomatMerger.addSpanRtl().
            RegExMatchInterface match2 = RegEx.matchSubstring(title,
                    "<span dir=\"rtl\">(.*?)</span>", false);

            // perform replacement only if necessary
            if (match2 != null)
            {
                String firstHalf = m_content.substring(0, titleBegin);
                String secondHalf = m_content.substring(titleEnd);

                String realTitle = title.substring(match2.beginOffset(1), match2.endOffset(1));

                m_content = firstHalf + "<TITLE dir=\"rtl\">" + realTitle + "</TITLE>" + secondHalf;
            }
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            // theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
            theLogger.error(PROCESS_ERROR, e);
        }
    }
}
