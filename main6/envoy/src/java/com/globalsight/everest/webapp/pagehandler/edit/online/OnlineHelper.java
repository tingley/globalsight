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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.HtmlEntities;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.ling.tw.PseudoBaseHandler;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoOverrideItemException;
import com.globalsight.ling.tw.PseudoParser;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.Tmx2HtmlPreviewHandler;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.XmlEntities;

/**
 * Provides access to PTag API for the editor. Just same as onlineApplet
 * <p>
 * 
 * In regards to the PTags , a simple session would progress be as follows:
 * <p>
 * 
 * 1) Set the input segment.
 * <p>
 * 2) Depending on the users request, get the verbose or compact PTag target
 * string and display it in the editor.
 * <p>
 * 3) When the user submits the translated target, call errorCheck().
 * <p>
 * 4) If the error checker returns a message, display it and abort. If no
 * message is returned, call getTargetDiplomat().
 * <p>
 * 5) Do whatever with the translated/verified diplomat string.
 * <p>
 * <p>
 */

public class OnlineHelper implements PseudoBaseHandler
{
    private String m_inputSegment = null;
    private String m_outputSegment = null;
    private String m_segmentFormat = null;
    private TmxPseudo m_converter = null;
    private PseudoData m_withPtags = null;
    private HtmlTableWriter m_HtmlTableWriter = null;
    private XmlEntities m_xmlCodec = null;
    private boolean m_bPTagResourcesInitialized = false;
    private StringBuffer m_coloredPtags = null;
    private PseudoParser m_ptagParser = null;

    private static final String PTAG_COLOR_START = "<SPAN DIR=ltr class=ptag UNSELECTABLE=on CONTENTEDITABLE=true>";
    private static final String PTAG_COLOR_END = "</SPAN>";
    private PseudoErrorChecker m_errChecker = null;
    private String m_internalErrMsg = "";


    /**
     * Returns information about this applet.
     * 
     * @return a string of information about this applet
     */
    public String getAppletInfo()
    {
        return "Copyright 2000-2004 GlobalSight Corporation";
    }
    
    

    public OnlineHelper()
    {
        super();
        init();
    }



    /**
     * Initializes the applet.
     * 
     * @see #start
     * @see #stop
     * @see #destroy
     */
    public void init()
    {
        m_coloredPtags = new StringBuffer();
        m_ptagParser = new PseudoParser(this);
    }

    /**
     * This method should always be called once before calling getVerbose() or
     * getCompact(). If you do not check for errors before calling getVerbose or
     * getCompact(), exceptions are likely to occur.
     * 
     * The method takes two boolean flags as parameters: 1) Verify leading
     * whitespace. (not implimented in this release) 2) Verify trailing
     * whitepsace. (not implimented in this release) NOTE: Neither are
     * implemented in this release.
     * 
     * @return String -
     * @param p_target
     *            java.lang.String
     * @throws Exception
     */
    public String errorCheck(String p_target) throws Exception
    {
        // We now allow for an empty target string.

        // If the user thinks a non-empty source should be translated
        // to empty, this is what (s)he will be allowed to do. But
        // only if the source has no-ptags-at-all or only deletable
        // ptags.

        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_target == null)
        {
            p_target = "";
        }

        if (m_bPTagResourcesInitialized)
        {
            m_withPtags.setPTagTargetString(p_target);
            String errors = m_errChecker.check(m_withPtags);
            m_internalErrMsg = m_errChecker.geStrInternalErrMsg();
            return errors;
        }
        else
        {
            throw new Exception(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Get the current source string with maximally compressed p-tags. This
     * string would then be displayed as the initial target string.
     */
    public String getCompact() throws DiplomatBasicParserException,
            PseudoParserException
    {
        if (m_bPTagResourcesInitialized)
        {
            convertToPtags(PseudoConstants.PSEUDO_COMPACT);
            return m_withPtags.getPTagSourceString();
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    public String getNewPTagTargetString() throws DiplomatBasicParserException,
            PseudoParserException
    {
        if (m_errChecker != null)
        {
            return m_errChecker.getNewTarget();
        }

        return "";
    }

    /**
     * Generate the HTML table (rows only) containing the p-tag to native map.
     */
    @SuppressWarnings("static-access")
    public String getPtagToNativeMappingTable()
            throws DiplomatBasicParserException
    {
        if (m_bPTagResourcesInitialized)
        {
            return m_HtmlTableWriter.getSortedHtmlRows(m_withPtags
                    .getPseudo2NativeMap());
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Generate a string of ptags separated by comma ("l1,/l1,g1,/g1").
     */
    @SuppressWarnings("static-access")
    public String getPtagString() throws DiplomatBasicParserException
    {
        if (m_bPTagResourcesInitialized)
        {
            return m_HtmlTableWriter.getPtagString(m_withPtags
                    .getPseudo2NativeMap());
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Get the current source string with descriptive p-tags. This string would
     * then be displayed as the initial target string.
     */
    public String getVerbose() throws DiplomatBasicParserException,
            PseudoParserException
    {
        if (m_bPTagResourcesInitialized)
        {
            convertToPtags(PseudoConstants.PSEUDO_VERBOSE);
            return m_withPtags.getPTagSourceString();
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Get the translated target string encoded with Diplomat tags.
     */
    @SuppressWarnings("static-access")
    public String getTargetDiplomat(String p_target)
            throws PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_target == null)
        {
            p_target = "";
        }

        // if (m_errChecker != null)
        // {
        // p_target = m_errChecker.revertInternalTags(p_target);
        // }

        if (m_bPTagResourcesInitialized)
        {
            m_withPtags.setPTagTargetString(p_target);
            m_outputSegment = m_converter.pseudo2Tmx(m_withPtags);
            return m_outputSegment;
        }
        else
        {
            throw new PseudoParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }

    }

    @SuppressWarnings("static-access")
    private void convertToPtags(int p_mode) throws DiplomatBasicParserException
    {
        m_withPtags.setMode(p_mode);
        m_converter.tmx2Pseudo(m_inputSegment, m_withPtags);
    }

    /*
     * Initializes PTag resources.<p>
     * 
     * The main purpose for creating this method was to catch exceptions that
     * could be thrown when the PseudoOverride rules are initialized within the
     * PseudoData constructor.<pp>
     * 
     * @throws PseudoOverrideItemException
     */
    private void initPTagResources() throws PseudoOverrideItemException
    {
        m_xmlCodec = new XmlEntities();
        m_converter = new TmxPseudo();
        m_withPtags = new PseudoData();
        m_HtmlTableWriter = new HtmlTableWriter();
        m_errChecker = new PseudoErrorChecker();
    }

    /*
     * Set the Diplomat input string and convert it to p-tags internally. After
     * setting the string, you can getCompact() or getVerbose().
     * 
     * @param p_source String
     * 
     * @param p_encoding String
     * 
     * @param p_segmentFormat String - an empty, null or otherwise incorrect
     * value disables addables.
     */
    public void setInputSegment(String p_source, String p_encoding,
            String p_segmentFormat, boolean p_isFromSourceTargetPanel)
            throws PseudoOverrideItemException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_source == null)
        {
            p_source = "";
        }

        // we must init things here because we cannot throw exceptions
        // from the applets init() method.
        if (!m_bPTagResourcesInitialized)
        {
            initPTagResources();
            m_bPTagResourcesInitialized = true;
        }

        m_inputSegment = p_source;
        m_segmentFormat = p_segmentFormat;

        m_withPtags.setAddables(p_segmentFormat);
        m_withPtags.setIsFromSourceTargetPanel(p_isFromSourceTargetPanel);
    }

    public void setInputSegment(String p_source, String p_encoding,
            String p_segmentFormat) throws PseudoOverrideItemException
    {
        setInputSegment(p_source, p_encoding, p_segmentFormat, true);
    }

    /*
     * Sets the locale (for error messages)
     * 
     * @param p_locale - language and country string, example: "en_US"
     */
    public void setLocale(String p_locale) throws PseudoOverrideItemException
    {
        // we must init things here because we cannot throw exceptions
        // from the applets init() method.
        if (!m_bPTagResourcesInitialized)
        {
            initPTagResources();
            m_bPTagResourcesInitialized = true;
        }

        m_withPtags.setLocale(p_locale);
    }

    /*
     * Sets the data type (for error check)
     * 
     * @param
     */
    public void setDataType(String p_dataType)
            throws PseudoOverrideItemException
    {
        // we must init things here because we cannot throw exceptions
        // from the applets init() method.
        if (!m_bPTagResourcesInitialized)
        {
            initPTagResources();
            m_bPTagResourcesInitialized = true;
        }

        m_withPtags.setDataType(p_dataType);
    }

    /**
     */
    public void processTag(String tagName, String originalString)
            throws PseudoParserException
    {
        m_coloredPtags.append(PTAG_COLOR_START + originalString
                + PTAG_COLOR_END);
    }

    /**
     */
    public void processText(String strText)
    {
        m_coloredPtags.append(strText);
    }

    /**
     */
    @SuppressWarnings("static-access")
    public String makeCompactColoredPtags(String p_diplomat)
            throws DiplomatBasicParserException, PseudoOverrideItemException,
            PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_diplomat == null)
        {
            p_diplomat = "";
        }

        TmxPseudo converter = new TmxPseudo();
        PseudoData withPtags = new PseudoData();

        withPtags.setMode(PseudoConstants.PSEUDO_COMPACT);
        withPtags.setAddables(m_segmentFormat);
        converter.tmx2Pseudo(p_diplomat, withPtags);

        return convertToColored(withPtags.getWrappedPTagSourceString());
    }

    /**
     */
    @SuppressWarnings("static-access")
    public String makeVerboseColoredPtags(String p_diplomat)
            throws DiplomatBasicParserException, PseudoOverrideItemException,
            PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_diplomat == null)
        {
            p_diplomat = "";
        }

        TmxPseudo converter = new TmxPseudo();
        PseudoData withPtags = new PseudoData();

        withPtags.setMode(PseudoConstants.PSEUDO_VERBOSE);
        withPtags.setAddables(m_segmentFormat);
        converter.tmx2Pseudo(p_diplomat, withPtags);

        return convertToColored(withPtags.getWrappedPTagSourceString());
    }

    @SuppressWarnings("static-access")
    public String makeInlineVerboseColoredPtags(String p_diplomat)
            throws DiplomatBasicParserException, PseudoOverrideItemException,
            PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_diplomat == null)
        {
            p_diplomat = "";
        }

        TmxPseudo converter = new TmxPseudo();
        PseudoData withPtags = new PseudoData();

        withPtags.setMode(PseudoConstants.PSEUDO_VERBOSE);
        withPtags.setAddables(m_segmentFormat);
        converter.tmx2Pseudo(p_diplomat, withPtags, TmxPseudo.ONLINE_EDIT);

        return convertToColored(withPtags.getWrappedPTagSourceString());
    }

    /**
     * Returns an HTML string ready to be displayed in a target preview frame.
     */
    public String getTargetPreview(String p_tmxString)
            throws DiplomatBasicParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_tmxString == null)
        {
            p_tmxString = "";
        }

        Tmx2HtmlPreviewHandler handler = new Tmx2HtmlPreviewHandler();
        DiplomatBasicParser parser = new DiplomatBasicParser(handler);

        parser.parse(p_tmxString);

        return handler.getResult();
    }

    /**
     * Returns an HTML color encoded ptag string.
     * <p>
     * 
     * Only the necessary HTML to color the tags is added and the necessary
     * escapes are encoded.
     */
    private String convertToColored(String p_ptag) throws PseudoParserException
    {
        HtmlEntities html = new HtmlEntities();

        m_coloredPtags.setLength(0);
        m_ptagParser.tokenize(html.encodeString(p_ptag));

        return m_coloredPtags.toString();
    }

    /**
     * Sets the maximum length allowed for the target GXML (not implemented).
     */
    public void setTargetNativeMaxLength(int p_maxLen)
    {
    }

    /**
     * This method should always be called once before calling getVerbose() or
     * getCompact(). If you do not check for errors before calling getVerbose or
     * getCompact(), exceptions are likely to occur.
     * 
     * The method takes two boolean flags as parameters: 1) Verify leading
     * whitespace. (not implimented in this release) 2) Verify trailing
     * whitepsace. (not implimented in this release) NOTE: Neither are
     * implemented in this release.
     * 
     * @return String empty string or error message
     * @param p_target
     *            the target string
     * @param p_sourceWithSubContent
     *            the source string with subs (editors map-segment).
     * @param p_gxmlMaxLen
     *            maximum length of the gxml string as stored in the system3
     *            database if set to zero, no checking is performed.
     * @param p_gxmlStorageEncoding
     *            the encoding of the gxml string as stored in system3 database
     *            if empty string or null, exception is thrown.
     * @param p_nativeContentMaxLen
     *            maximum length of native content as stored in the clients
     *            database if set to zero, no checking is performed.
     * @param p_nativeStorageEncoding
     *            the encoding of native content as stored in the clients
     *            database if empty string or null, exception is thrown.
     * @throws Exception
     */
    public String errorCheck(String p_target, String p_sourceWithSubContent,
            int p_gxmlMaxLen, String p_gxmlStorageEncoding,
            int p_nativeContentMaxLen, String p_nativeStorageEncoding)
            throws Exception
    {
        // We now allow for an empty target string. If the user
        // thinks a non-empty source should be translated to empty,
        // this is what (s)he will be allowed to do. But only if the
        // source has no-ptags-at-all or only deletable ptags.

        // Empty strings arrive as NULL pointer somehow, so fix that
        if (p_target == null)
        {
            p_target = "";
        }

        if (m_bPTagResourcesInitialized)
        {
            // To add length checking with the least amount of impact
            // on editor we use the m_withPtags (PseudoData) object as
            // we did before. This keeps the rules for using the API
            // the same. We also have to stay in sync with the
            // compact/verbose mode.
            m_withPtags.setPTagTargetString(p_target);

            String errors = m_errChecker.check(m_withPtags,
                    p_sourceWithSubContent, p_gxmlMaxLen,
                    p_gxmlStorageEncoding, p_nativeContentMaxLen,
                    p_nativeStorageEncoding);
            m_internalErrMsg = m_errChecker.geStrInternalErrMsg();
            return errors;
        }
        else
        {
            throw new Exception(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    public void setUntranslateStyle(String styles)
    {
        if (m_errChecker != null)
        {
            m_errChecker.setStyles(styles);
        }
    }

    public String getInternalErrMsg()
    {
        if (m_internalErrMsg != null && m_internalErrMsg.contains("&"))
        {
            return m_xmlCodec.decodeString(m_internalErrMsg);
        }
        else
        {
            return m_internalErrMsg;
        }
    }

    public void setInternalErrMsg(String internalErrMsg)
    {
        this.m_internalErrMsg = internalErrMsg;
    }
}
