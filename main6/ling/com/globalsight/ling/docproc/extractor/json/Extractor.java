/**
 *  Copyright 2016 Welocalize, Inc. 
 *  
 */
package com.globalsight.ling.docproc.extractor.json;

// Java
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.JsonFilter;
import com.globalsight.ling.common.PTEscapeSequence;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorInterface;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.extractor.plaintext.PTTmxController;
import com.globalsight.ling.docproc.extractor.plaintext.PTToken;
import com.globalsight.ling.docproc.extractor.plaintext.Parser;
import com.globalsight.util.StringUtil;


public class Extractor extends AbstractExtractor implements ExtractorInterface,
        EntityResolver, ExtractorExceptionConstants, ErrorHandler
{
    static private final Logger s_logger = Logger.getLogger(Extractor.class);
    private String REGEX_COLON = ":[\\s|\\t|\\r|\\n]*\"";
    private String REGEX_BRACKETS = ":[\\s|\\t|\\r|\\n]*\\[[\\s|\\t|\\r|\\n]*\"";
    private Output m_output = null;
    private EFInputData m_input = null;
    private Filter m_elementPostFilter = null;
    private String m_postFormat = null;
    private static String[] invalidHtmlTagCharacters = new String[]
    { "{", "}", "%", "^", "~", "!", "&", "*", "(", ")", "?" };
    private static final String PLACEHOLDER_LEFT_TAG = "GS_PLACEHOLDER_LEFT_TAG";
    private static final String PLACEHOLDER_RIGHT_TAG = "GS_PLACEHOLDER_RIGHT_TAG";
    private static final String PLACEHOLDER_LEFT_NATIVE = "GS_PLACEHOLDER_LEFT_NATIVE";
    private static final String PLACEHOLDER_RIGHT_NATIVE = "GS_PLACEHOLDER_RIGHT_NATIVE";

    public Extractor()
    {
        super();
    }

    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_JSON);
    }

    /**
     * Extracts the input document.
     * 
     * Parses the XML File into DOM using xerces.
     * 
     * Skips the external entity (DTD, etc) by providing a null byte array.
     * 
     * Then invokes domNodeVisitor for the Document 'Node' ('virtual root') to
     * traverse the DOM tree recursively, using the AbstractExtractor API to
     * write out skeleton and segments.
     */
    public void extract() throws ExtractorException
    {
        this.setFormat();
        m_input = getInput();
        m_output = getOutput();
        try
        {
            Pattern colonP = Pattern.compile(REGEX_COLON);
            Pattern bracketsP = Pattern.compile(REGEX_BRACKETS);
            Filter mainFilter = getMainFilter();
            JsonFilter jsonFilter = (mainFilter != null && mainFilter instanceof JsonFilter) ? (JsonFilter) mainFilter
                    : null;
            boolean isEnableSidSupport = false;
            long elementPostFilterId = -1;
            String elementPostFilterTableName = null;
            if (jsonFilter != null)
            {
                isEnableSidSupport = jsonFilter.isEnableSidSupport();
                elementPostFilterId = jsonFilter.getElementPostFilterId();
                elementPostFilterTableName = jsonFilter.getElementPostFilterTableName();
                if (elementPostFilterId >= 0 && elementPostFilterTableName != null)
                {
                    m_elementPostFilter = FilterHelper.getFilter(elementPostFilterTableName,
                            elementPostFilterId);
                    if (m_elementPostFilter != null)
                    {
                        m_postFormat = FilterConstants.FILTER_TABLE_NAMES_FORMAT
                                .get(m_elementPostFilter.getFilterTableName());
                    }
                }
            }
            Reader reader = readInput();
            BufferedReader bufferReader = new BufferedReader(reader);
            int tempchar;
            boolean containColon = false;
            boolean tranChar = false;
            boolean containBrackets = false;
            StringBuffer skeletonBuffer = new StringBuffer();
            StringBuffer colonBuffer = new StringBuffer();
            StringBuffer tranBuffer = new StringBuffer();
            String sid = null;
            int countEscapes = 0;
            while ((tempchar = bufferReader.read()) != -1)
            {
                char chr = (char) tempchar;
                if (chr == ':')
                    containColon = true;
                
                if (tranChar && chr == '\\')
                {
                    countEscapes++;
                }

                if (containColon)
                {
                    colonBuffer.append(chr);
                    if (colonP.matcher(colonBuffer.toString()).find())
                    {
                        skeletonBuffer.append(colonBuffer);
                        m_output.addSkeleton(skeletonBuffer.toString());
                        if (isEnableSidSupport)
                            sid = getSid(skeletonBuffer.toString());
                        
                        colonBuffer = new StringBuffer();
                        skeletonBuffer = new StringBuffer();
                        containColon = false;
                        tranChar = true;
                        continue;
                    }
                    else if (bracketsP.matcher(colonBuffer.toString()).find())
                    {
                        skeletonBuffer.append(colonBuffer);
                        m_output.addSkeleton(skeletonBuffer.toString());
                        if (isEnableSidSupport)
                            sid = getSid(skeletonBuffer.toString());
                        
                        colonBuffer = new StringBuffer();
                        skeletonBuffer = new StringBuffer();
                        containColon = false;
                        containBrackets = true;
                        tranChar = true;
                        continue;
                    }
                }
                else if (containBrackets && !tranChar)
                {
                    if (chr == '"')
                    {
                        tranChar = true;
                        m_output.addSkeleton(skeletonBuffer.toString());
                        skeletonBuffer = new StringBuffer();
                    }
                    else
                    {
                        skeletonBuffer.append(chr);
                        if (chr == ']')
                            containBrackets = false;
                    }
                }
                else if (tranChar)
                {
                    if (chr != '"' || (countEscapes > 0 && chr == '"'))
                    {
                        tranBuffer.append(chr);
                        if (countEscapes > 0 && chr == '"')
                            countEscapes = 0;
                        continue;
                    }
                    else
                    {
                        tranChar = false;
                        // if translate segment is empty
                        if (StringUtil.isEmpty(tranBuffer.toString().trim()))
                        {
                            skeletonBuffer.append(tranBuffer.toString()).append(chr);
                            tranBuffer = new StringBuffer();
                            continue;
                        }
                        else
                        {
                            // if translate segment is not empty
                            skeletonBuffer.append(chr);
                            if (m_elementPostFilter != null)
                            {
                                try
                                {
                                    gotoPostFilter(tranBuffer.toString(), sid);
                                }
                                catch (Exception e)
                                {
                                    m_output.addTranslatable(tranBuffer.toString(), sid);
                                }
                            }
                            else
                            {
                                m_output.addTranslatable(tranBuffer.toString(), sid);
                            }

                            tranBuffer = new StringBuffer();
                            continue;
                        }
                    }
                }
                else
                {
                    skeletonBuffer.append(chr);
                }
            }
            if (skeletonBuffer.toString() != null || colonBuffer.toString() != null)
            {
                skeletonBuffer.append(colonBuffer);
                m_output.addSkeleton(skeletonBuffer.toString());
            }
            bufferReader.close();
        }
        catch (Exception e)
        {
            s_logger.error(e);
            throw new ExtractorException(e);
        }
    }
    
    private String getSid(String skeleton)
    {
        char[] skeletonArr = skeleton.toCharArray();
        int firstIndex = -1;
        int lastIndex = -1;
        int count = 0;
        for (int i = skeletonArr.length - 1; i >= 0; i--)
        {
            if (skeletonArr[i] == '"')
            {
                count++;
                if (count == 2)
                {
                    lastIndex = i;
                }
                else if (count == 3)
                {
                    firstIndex = i;
                    break;
                }
            }
        }
        return skeleton.substring(firstIndex + 1, lastIndex);
    }

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
                    chunk = StringUtil.replace(chunk, "<", PLACEHOLDER_LEFT_TAG);
                    chunk = StringUtil.replace(chunk, ">", PLACEHOLDER_RIGHT_TAG);
                    // m_output.addTranslatableTmx(segmentableElement.getChunk(),
                    // sid, true,
                    // m_postFormat);
                    extractString(new StringReader(chunk), true,sid);
                    break;

                case DocumentElement.SKELETON:
                    String skeleton = ((SkeletonElement) element).getSkeleton();
                    m_output.addSkeletonTmx(skeleton);
                    break;
            }
        }
    }
    
    private void extractString(Reader p_input, boolean p_postFiltered, String sid)
    {
        int cVECTORSTART = 200;
        int cVECTORINC = 50;

        Vector vTokenBuf = new Vector(cVECTORSTART, cVECTORINC);

        Parser parser = new Parser(p_input);
        PTToken token = parser.getNextToken();

        while (token.m_nType != PTToken.EOF)
        {
            vTokenBuf.add(token);
            token = parser.getNextToken();
        }

        addTokensToOutput(vTokenBuf, sid,p_postFiltered);
    }
   
    private void addTokensToOutput(Vector p_vTokens,String sid, boolean p_postFiltered)
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
                    content = StringUtil.replace(content, PLACEHOLDER_LEFT_TAG, "<");
                    content = StringUtil.replace(content, PLACEHOLDER_RIGHT_TAG, ">");
                    content = StringUtil.replace(content, PLACEHOLDER_LEFT_NATIVE, "&lt;");
                    content = StringUtil.replace(content, PLACEHOLDER_RIGHT_NATIVE, "&gt;");
                    m_output.addTranslatableTmx(content, sid, true, m_postFormat);
                }
            }
            else
            {
                // returns false if there is no tagged version of the
                // character
                if (TmxCtrl.makeTmx(Tok.m_strContent.charAt(0), Tok.m_nPos))
                {
                    m_output.addTranslatableTmx(TmxCtrl.getStart(), sid);
                    m_output.addTranslatable(PTEsc.encode(Tok.m_strContent), sid);
                    m_output.addTranslatableTmx(TmxCtrl.getEnd(), sid);
                }
                else
                {
                    m_output.addTranslatable(Tok.m_strContent,sid);
                }
            }
        }

        p_vTokens.clear();

    }
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
     * This method is invoked by AbstractExractor framework. It is used to point
     * the XML Extracator to the file containing the XML extraction rules.
     * 
     * If the path to Extraction Rules is not specified via
     * Input.m_strProjectRules, it defaults to "file:/gsrules.xml". (CvdL: I
     * think it defaults to a null string.)
     */
    public void loadRules() throws ExtractorException
    {
    }

    /** Provide an alternate way to load rules */
    public void loadRules(String p_rules) throws ExtractorException
    {
    }

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to read Schemarules.dtd from resource and
     * feed it to the validating parser, but what it really does is returning a
     * null byte array to the XML parser.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    // ErrorHandler interface methods

    public void error(SAXParseException e) throws SAXException
    {
        String s = e.getMessage();
        // ignore below errors
        if (s.matches("Attribute .*? was already specified for element[\\s\\S]*"))
        {
            return;
        }

        throw new SAXException("XML parse error at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        System.err.println("XML parse warning at\n  line " + e.getLineNumber()
                + "\n  column " + e.getColumnNumber() + "\n  Message:"
                + e.getMessage());
    }
}
