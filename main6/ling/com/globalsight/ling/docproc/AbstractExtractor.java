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
package com.globalsight.ling.docproc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.ling.StandardExtractor;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.Escaping;
import com.globalsight.cxe.entity.filterconfiguration.EscapingHelper;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.everest.util.comparator.PriorityComparator;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * <p>
 * The abstract class representing the required methods for any extractor.
 * </p>
 * 
 * <p>
 * All extractors in the framework must inherit from this class.
 * </p>
 */
public abstract class AbstractExtractor implements ExtractorInterface
{
    protected static final Logger CATEGORY = Logger
            .getLogger(AbstractExtractor.class);
    private EFInputData m_input = null;
    private Output m_output = null;
    private int m_iParentType = EFInputDataConstants.UNKNOWN;
    private boolean m_bEmbedded = false;
    private StringBuffer m_strEmbedded = null;
    private Object m_dynamicRules = null;

    // Flag for source page editing allowing caller to switch of
    // segment simplification in the HTML extractor.
    private boolean m_simplifySegments = true;
    // Flag for source page editing allowing caller to disallow
    // Javascript extractor to call HTML extractor (flag can be used
    // by other extractors as well).
    private boolean m_canCallOtherExtractor = true;

    private boolean m_bExtractUnmarked = true;
    private boolean m_bInExtractBlock = false;
    private boolean m_bInExcludeBlock = false;
    private boolean m_bExtractNext = false;
    private boolean m_bExcludeNext = false;

    // global extraction state for all extractors and all files
    private static boolean s_bExtractUnmarkedDefault = true;
    private static boolean s_bExtractEmbeddedUnmarkedDefault = true;
    private static boolean s_bExtractEmbeddedInherit = true;

    private static String s_excludeUnmarked = "GS_EXCLUDE_UNMARKED";
    private static String s_extractUnmarked = "GS_EXTRACT_UNMARKED";
    private static String s_beginExclude = "GS_BEGIN_EXCLUDE";
    private static String s_endExclude = "GS_END_EXCLUDE";
    private static String s_beginExtract = "GS_BEGIN_EXTRACT";
    private static String s_endExtract = "GS_END_EXTRACT";
    private static String s_extractNext = "GS_EXTRACT_NEXT";
    private static String s_excludeNext = "GS_EXCLUDE_NEXT";

    private ExtractorRegistry m_ExtractorRegistry = null;
    private Filter m_mainFilter = null;
    private BaseFilter mainBaseFilter = null;
    private boolean doSegBeforeInlText = true;

    private boolean m_preserveAllWhite = false;

    static
    {
        try
        {
            String value;

            ResourceBundle res = ResourceBundle.getBundle(
                    "properties/Diplomat", Locale.US);

            try
            {
                value = res.getString("CE_default_state");
                if (value != null)
                {
                    s_bExtractUnmarkedDefault = value
                            .equalsIgnoreCase("extract");
                }
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_embedded_default_state");
                if (value != null)
                {
                    s_bExtractEmbeddedInherit = value
                            .equalsIgnoreCase("inherit");
                    s_bExtractEmbeddedUnmarkedDefault = value
                            .equalsIgnoreCase("extract");
                }
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_exclude_unmarked");
                if (value != null)
                    s_excludeUnmarked = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_extract_unmarked");
                if (value != null)
                    s_extractUnmarked = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_begin_exclude");
                if (value != null)
                    s_beginExclude = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_end_exclude");
                if (value != null)
                    s_endExclude = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_begin_extract");
                if (value != null)
                    s_beginExtract = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_end_extract");
                if (value != null)
                    s_endExtract = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_exclude_next");
                if (value != null)
                    s_excludeNext = value;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                value = res.getString("CE_extract_next");
                if (value != null)
                    s_extractNext = value;
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
     * AbstractExtractor constructor.
     */
    public AbstractExtractor()
    {
        m_ExtractorRegistry = ExtractorRegistry.getObject();

        // set default extraction state
        m_bExtractUnmarked = s_bExtractUnmarkedDefault;
    }

    public void addToEmbeddedString(String p_strTmxString)
    {
        m_strEmbedded.append(p_strTmxString);
    }

    /**
     * <p>
     * Tests whether the next segment should be excluded from extraction.
     * </p>
     * 
     * <p>
     * Note: this predicate has side effects and can only be called <b>once</b>
     * in a "GS_EXTRACT_NEXT" or "GS_EXCLUDE_NEXT" context. Calling this
     * predicate resets the "exclude/extract next" flag.
     * </p>
     * 
     * @see #readMetaMarkup(String)
     */
    public boolean exclude()
    {
        if (m_bExtractNext)
        {
            m_bExtractNext = false;
            m_bExcludeNext = false;
            return false;
        }

        if (m_bExcludeNext)
        {
            m_bExcludeNext = false;
            return true;
        }

        if (m_bInExtractBlock)
            return false;

        if (m_bInExcludeBlock)
            return true;

        return !m_bExtractUnmarked;
    }

    protected String getEmbeddedString()
    {
        if (m_bEmbedded)
            return m_strEmbedded.toString();

        // should really throw an exception but this embedded string
        // is a hack anyway
        return "";
    }

    protected EFInputData getInput()
    {
        return m_input;
    }

    /**
     * Returns the primary extraction format.
     */
    public String getMainFormat()
    {
        return m_output.getDataFormat();
    }

    /**
     * Returns the current Output object, which holds the current output
     * results.
     */
    protected Output getOutput()
    {
        return m_output;
    }

    protected void setDynamicRules(Object p_rules)
    {
        m_dynamicRules = p_rules;
    }

    protected Object getDynamicRules()
    {
        return m_dynamicRules;
    }

    protected void setSimplifySegments(boolean p_flag)
    {
        m_simplifySegments = p_flag;
    }

    public boolean getSimplifySegments()
    {
        return m_simplifySegments;
    }

    protected void setCanCallOtherExtractor(boolean p_flag)
    {
        m_canCallOtherExtractor = p_flag;
    }

    public boolean getCanCallOtherExtractor()
    {
        return m_canCallOtherExtractor;
    }

    /**
     * Sets references to the input and putput objects.
     */
    public void init(EFInputData p_Input, Output p_Output)
            throws ExtractorException
    {
        m_input = p_Input;
        m_output = p_Output;

        // set the locale for the output

        // Thanks to a JDK bug we must post-process the locale string
        // so it ends up correctly in the Diplomat XML output.
        // THANK YOU EVER SO MUCH FOR MAKING ME WRITE CRAP LIKE THIS.
        String str_locale = p_Input.getLocale().toString();

        if (str_locale.startsWith("iw"))
            str_locale = "he" + str_locale.substring(2);
        else if (str_locale.startsWith("ji"))
            str_locale = "yi" + str_locale.substring(2);
        else if (str_locale.startsWith("in"))
            str_locale = "id" + str_locale.substring(2);

        m_output.setLocale(str_locale);
    }

    public boolean isEmbedded()
    {
        return m_bEmbedded;
    }

    /**
     * See if we have set the primary format
     */
    public boolean isSetMainFormat()
    {
        return m_output.getDataFormat() != null;
    }

    private AbstractExtractor makeExtractor(int p_iExtractorType)
            throws ExtractorException
    {
        AbstractExtractor result = null;

        if (p_iExtractorType == EFInputDataConstants.UNKNOWN)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_FILE_FORMAT);
        }

        try
        {
            result = (AbstractExtractor) Class.forName(
                    ExtractorRegistry.getObject().getExtractorClasspath(
                            p_iExtractorType)).newInstance();

            // propagate current extraction state
            if (s_bExtractEmbeddedInherit)
            {
                result.m_bExtractUnmarked = m_bExtractUnmarked;
                result.m_bInExtractBlock = m_bInExtractBlock;
                result.m_bInExcludeBlock = m_bInExcludeBlock;
            }
            else
            {
                result.m_bExtractUnmarked = s_bExtractEmbeddedUnmarkedDefault;
                result.m_bInExtractBlock = m_bInExtractBlock;
                result.m_bInExcludeBlock = m_bInExcludeBlock;
            }

            // propagate dynamic rules and other settings
            result.m_dynamicRules = m_dynamicRules;
            result.m_simplifySegments = m_simplifySegments;
            result.m_canCallOtherExtractor = m_canCallOtherExtractor;
        }
        catch (Exception e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }

        return result;
    }

    /**
     * <p>
     * Search for appropriate input type and make a Reader object to read it.
     * For byte array and URL input do code set conversion.
     * </p>
     */
    public Reader readInput() throws ExtractorException
    {
        return readInput(null);
    }

    public Reader readInput(BaseFilter p_baseFilter) throws ExtractorException
    {
        Reader input = getInputReader(p_baseFilter);

        try
        {
            // Guess encoding for files that definitely declare it.
            if (m_input.getType() == m_ExtractorRegistry
                    .getFormatId(IFormatNames.FORMAT_XPTAG))
            {
                String encoding = getQuarkEncoding(input);
                m_input.setCodeset(encoding);

                // Reset the stream by allocating a new reader.
                input.close();

                return getInputReader(p_baseFilter);
            }

            // throw away BOM. Usually BOM is not included in stream
            // when you read UTF-16 source. But UTF-8 signature will
            // be converted to U+FEFF and comes at the beginning of
            // the stream.
            if (input.read() != '\ufeff')
            {
                input.close();
                // We can't use Reader#reset, because some stream
                // doesn't support it.
                return getInputReader(p_baseFilter);
            }
        }
        catch (IOException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_SOURCE, e.toString());
        }

        return input;
    }

    public void readMetaMarkup(String p_strComment) throws ExtractorException
    {
        // Wed Apr 04 00:07:35 2001: add -* for CF comments (<!---.--->)
        try
        {
            if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_excludeUnmarked, false) != null)
                m_bExtractUnmarked = false;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_extractUnmarked, false) != null)
                m_bExtractUnmarked = true;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_extractNext, false) != null)
                m_bExtractNext = true;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_excludeNext, false) != null)
                m_bExcludeNext = true;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_beginExclude, false) != null)
                m_bInExcludeBlock = true;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_endExclude, false) != null)
                m_bInExcludeBlock = false;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_beginExtract, false) != null)
                m_bInExtractBlock = true;
            else if (RegEx.matchSubstring(p_strComment, "^-*\\s*"
                    + s_endExtract, false) != null)
                m_bInExtractBlock = false;
        }
        catch (RegExException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.REGEX_ERROR, e.getMessage());
        }
    }

    /**
     * This method is invoked by AbstractExtractor framework. This must be
     * overridden by all extractors.
     */
    public abstract void loadRules() throws ExtractorException;

    /**
     * This method is invoked by AbstractExtractor framework. This should be
     * overridden by extractors using rules.
     */
    public void loadRules(String p_rules) throws ExtractorException
    {
    }

    /**
     * Set the filter for this format
     */
    public void setMainFilter(Filter p_filter)
    {
        m_mainFilter = p_filter;
    }

    /**
     * Get the filter for this format
     */
    public Filter getMainFilter()
    {
        return m_mainFilter;
    }

    protected void setEmbedded(boolean p_bEmbedded)
    {
        m_bEmbedded = p_bEmbedded;

        if (m_bEmbedded)
            m_strEmbedded = new StringBuffer();
    }

    /**
     * Set the main format if it hasn't been set before
     */
    public void setMainFormat(String p_format)
    {
        if (!isSetMainFormat())
            m_output.setDataFormat(p_format);
    }

    protected void setParentType(int p_iType)
    {
        m_iParentType = p_iType;
    }

    public int getParentType()
    {
        return m_iParentType;
    }

    /**
     * Calls a new sub-format extractor and afterwards returns control back to
     * the parent extractor.
     */
    public void switchExtractor(int p_iParentType, EFInputData p_Input,
            String... jsFunctionText) throws ExtractorException
    {
        AbstractExtractor ex;

        ex = makeExtractor(p_Input.getType());
        ex.init(p_Input, m_output);
        ex.setParentType(p_iParentType);
        ex.loadRules();
        setJsFunction(ex, jsFunctionText);

        ex.extract();
    }

    private void setJsFunction(AbstractExtractor ex, String... jsFunctionText)
    {
        if (ex instanceof com.globalsight.ling.docproc.extractor.javascript.Extractor)
        {
            if (jsFunctionText != null && jsFunctionText.length > 0)
            {
                ((com.globalsight.ling.docproc.extractor.javascript.Extractor) ex)
                        .setJsFilterRegex(jsFunctionText[0]);
                if (jsFunctionText.length == 2)
                {
                    ((com.globalsight.ling.docproc.extractor.javascript.Extractor) ex)
                            .setNoUseJsFunction(jsFunctionText[1]);
                }
            }
        }
    }

    /**
     * Calls a new sub-format extractor and afterwards returns control back to
     * the parent extractor.
     */
    public void switchExtractor(int p_iParentType, EFInputData p_Input,
            boolean p_bEmbedded) throws ExtractorException
    {
        AbstractExtractor ex;

        ex = makeExtractor(p_Input.getType());
        ex.init(p_Input, m_output);
        ex.setParentType(p_iParentType);
        ex.setEmbedded(p_bEmbedded);
        ex.loadRules();
        ex.extract();
    }

    public String switchExtractorEmbedded(int p_iParentType,
            EFInputData p_input, String... jsFunctionText)
            throws ExtractorException
    {
        AbstractExtractor ex;

        ex = makeExtractor(p_input.getType());
        ex.init(p_input, m_output);
        ex.setParentType(p_iParentType);
        ex.setEmbedded(true);
        ex.loadRules();
        setJsFunction(ex, jsFunctionText);
        ex.extract();
        return ex.getEmbeddedString();
    }

    public Output switchExtractor(String to_translate, String dataFormat)
            throws ExtractorException
    {
        return switchExtractor(to_translate, dataFormat, null, null);
    }

    public Output switchExtractor(String to_translate, String dataFormat,
            Filter filter) throws ExtractorException
    {
        return switchExtractor(to_translate, dataFormat, null, filter);
    }

    public Output switchExtractor(String to_translate, String dataFormat,
            String rules) throws ExtractorException
    {
        return switchExtractor(to_translate, dataFormat, rules, null);
    }

    public Output switchExtractor(String to_translate, String dataFormat,
            String rules, Filter filter) throws ExtractorException
    {
        // keep all whitespace from GBS-3663
        return switchExtractor(to_translate, dataFormat, rules, filter, true);
    }

    private Output switchExtractor(String to_translate, String dataFormat,
            String rules, Filter filter, boolean p_preserveAllWhite)
    {

        ExtractorRegistry reg = ExtractorRegistry.getObject();

        int formatId = -1;
        if (dataFormat != null)
        {
            formatId = reg.getFormatId(dataFormat);
        }

        if (formatId == -1)
        {
            String message = ExtractorExceptionConstants.UNKNOWN_FORMAT
                    + dataFormat;

            throw new ExtractorException(
                    ExtractorExceptionConstants.XML_EXTRACTOR_UNKNOWN_FORMAT,
                    message);
        }

        String content = to_translate;
        // html
        if (formatId == 1)
        {
            content = StandardExtractor.checkHtmlTags(content);
        }

        EFInputData currentInput = getInput();
        EFInputData newInput = new EFInputData();
        newInput.setType(formatId);
        newInput.setCodeset(currentInput.getCodeset());
        newInput.setLocale(currentInput.getLocale());
        newInput.setUnicodeInput(content);

        AbstractExtractor ex;
        Output out = new Output();

        ex = makeExtractor(newInput.getType());
        ex.init(newInput, out);
        ex.setMainBaseFilter(this.getMainBaseFilter());
        ex.setPreserveAllWhite(p_preserveAllWhite);

        if (filter != null)
        {
            ex.setMainFilter(filter);
            long filterId = filter.getId();
            String filterTableName = filter.getFilterTableName();
            if (FilterConstants.HTML_TABLENAME.equals(filterTableName)
                    && formatId == 1)
            {
                ((Extractor) ex).setRules(null, filterId);
                ((Extractor) ex).setFilterId(filterId);
                ((Extractor) ex).setFilterTableName(filterTableName);
            }
        }

        ex.loadRules(rules);
        ex.extract();
        return out;

    }

    private Reader getInputReader(BaseFilter p_baseFilter)
            throws ExtractorException
    {
        Reader input = null;
        ByteArrayInputStream bis;

        if (m_input.getInput() != null)
        {
            // Read from byte buffer
            try
            {
                bis = new ByteArrayInputStream(m_input.getInput());
                input = new InputStreamReader(bis, m_input.getCodeset());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ExtractorException(
                        ExtractorExceptionConstants.INVALID_ENCODING, e);
            }
            catch (IOException e)
            {
                throw new ExtractorException(
                        ExtractorExceptionConstants.INVALID_SOURCE, e);
            }
        }
        else if (m_input.getURL() != null)
        {
            // read from URL
            try
            {
                URL u = new URL(m_input.getURL());

                InputStream in = null;
                try
                {
                    in = u.openStream();
                }
                catch (Exception e)
                {
                    File f = m_input.getFile();
                    if (f != null)
                    {
                        in = new FileInputStream(f);
                    }
                }

                String encoding = null;
                try
                {
                    File f = m_input.getFile();
                    if (f != null)
                    {
                        encoding = FileUtil.guessEncoding(f);
                    }
                }
                catch (Exception e)
                {
                    // ignore
                }

                if (encoding == null)
                    encoding = m_input.getCodeset();

                if (m_input.getType() == m_ExtractorRegistry
                        .getFormatId(IFormatNames.FORMAT_XLIFF)
                        || m_input.getType() == m_ExtractorRegistry
                                .getFormatId(IFormatNames.FORMAT_XLIFF20))
                {
                    BufferedReader buffReader = new BufferedReader(
                            new InputStreamReader(in, encoding));

                    String tempString = buffReader.readLine();
                    StringBuffer newString = new StringBuffer();

                    while (tempString != null)
                    {
                        newString.append(tempString).append("\n");
                        tempString = buffReader.readLine();
                    }

                    String str = SegmentUtil
                            .protectEntity(newString.toString());
                    in = new ByteArrayInputStream(str.getBytes(encoding));
                }

                if (m_input.getType() == m_ExtractorRegistry
                        .getFormatId(IFormatNames.FORMAT_XML)
                        || m_input.getType() == m_ExtractorRegistry
                                .getFormatId(IFormatNames.FORMAT_IDML))
                {
                    BufferedReader buffReader = new BufferedReader(
                            new InputStreamReader(in, encoding));

                    String tempString = buffReader.readLine();
                    StringBuffer newString = new StringBuffer();

                    // get internal texts
                    List<Escaping> es = null;
                    try
                    {
                        es = BaseFilterManager.getEscapings(p_baseFilter);
                        SortUtil.sort(es, new PriorityComparator());
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }

                    while (tempString != null)
                    {
                        tempString = EscapingHelper.handleString4Import(
                                tempString, es, m_ExtractorRegistry
                                        .getFormatName(m_input.getType()),
                                true, null);
                        // keep empty cdata section
                        tempString = tempString
                                .replace("<![CDATA[]]>",
                                        "<![CDATA[_globalsight_cdata_empty_content_]]>");
                        newString.append(tempString).append("\n");
                        tempString = buffReader.readLine();
                    }

                    String str = SegmentUtil
                            .protectInvalidUnicodeChar(newString.toString());
                    in = new ByteArrayInputStream(str.getBytes(encoding));
                }

                input = new InputStreamReader(in, encoding);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ExtractorException(
                        ExtractorExceptionConstants.INVALID_ENCODING, e);
            }
            catch (MalformedURLException e)
            {
                throw new ExtractorException(
                        ExtractorExceptionConstants.INVALID_SOURCE, e);
            }
            catch (IOException e)
            {
                throw new ExtractorException(
                        ExtractorExceptionConstants.INVALID_SOURCE, e);
            }
        }
        else if (m_input.getUnicodeInput() != null)
        {
            // read from Unicode string
            if (m_input.getType() == m_ExtractorRegistry
                    .getFormatId(IFormatNames.FORMAT_XLIFF)
                    || m_input.getType() == m_ExtractorRegistry
                            .getFormatId(IFormatNames.FORMAT_XLIFF20))
            {
                String str = SegmentUtil.protectEntity(m_input
                        .getUnicodeInput());
                input = new StringReader(str);
            }
            else
            {
                input = new StringReader(m_input.getUnicodeInput());
            }
        }
        else
        {
            // Error: No input found
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_SOURCE,
                    "No input source");
        }

        return input;
    }

    /**
     * <p>
     * Creates the codec for a given format format.
     * </p>
     */
    public NativeEnDecoder getEncoder(int p_format) throws ExtractorException
    {
        NativeEnDecoder decoder = null;

        try
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();
            String str_class = er.getDecoderClasspath(p_format);

            decoder = (NativeEnDecoder) Class.forName(str_class).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (InstantiationException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
        catch (IllegalAccessException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }

        return decoder;
    }

    // encoding = <e0> Mac OS, <e1> Windows ANSI, <e2> ISO Latin 1
    private String getQuarkEncoding(Reader p_reader) throws IOException
    {
        LineNumberReader r = new LineNumberReader(p_reader);

        // The CopyFlow converter inserts box names ("#story_name_1")
        // into the file, so we must search in the first few lines for
        // the encoding marker.

        int i = 0;
        while (i++ < 10)
        {
            String line = r.readLine();

            if (line.indexOf("<e0>") >= 0)
                return "MacRoman";
            else if (line.indexOf("<e1>") >= 0)
                return "Cp1252";
            else if (line.indexOf("<e2>") >= 0)
                return "ISO8859_1";
        }

        // Need to return a default encoding but the choice is arbitrary.
        return "Cp1252";
    }

    public void setMainBaseFilter(BaseFilter mainBaseFilter)
    {
        this.mainBaseFilter = mainBaseFilter;
    }

    public BaseFilter getMainBaseFilter()
    {
        return mainBaseFilter;
    }

    /**
     * # GBS-2894 : do segmentation before internal text
     */
    public boolean isDoSegBeforeInlText()
    {
        return doSegBeforeInlText;
    }

    /**
     * # GBS-2894 : do segmentation before internal text. Set true to apply this
     * rule
     */
    public void setDoSegBeforeInlText(boolean doSegFirst)
    {
        this.doSegBeforeInlText = doSegFirst;
    }

    public boolean preserveAllWhite()
    {
        return m_preserveAllWhite;
    }

    public void setPreserveAllWhite(boolean p_preserveAllWhite)
    {
        this.m_preserveAllWhite = p_preserveAllWhite;
    }

    protected String getPrefixBlank(String str)
    {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer preBlank = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r')
            {
                preBlank.append(c);
            }
            else
            {
                break;
            }
        }
        return preBlank.toString();
    }

    protected String getSuffixBlank(String str)
    {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer suffixBlank = new StringBuffer();
        for (int i = str.length() - 1; i >= 0; i--)
        {
            char c = str.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r')
            {
                suffixBlank.insert(0, c);
            }
            else
            {
                break;
            }
        }
        return suffixBlank.toString();
    }
}
