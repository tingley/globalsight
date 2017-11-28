/**
 *  Copyright 2016 Welocalize, Inc. 
 *  
 */
package com.globalsight.ling.docproc.extractor.json;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.GlobalExclusionFilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.GlobalExclusionFilterSid;
import com.globalsight.cxe.entity.filterconfiguration.JsonFilter;
import com.globalsight.cxe.entity.filterconfiguration.SidFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.ling.common.PTEscapeSequence;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.extractor.plaintext.PTTmxController;
import com.globalsight.ling.docproc.extractor.plaintext.PTToken;
import com.globalsight.ling.docproc.extractor.plaintext.Parser;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

public class Extractor extends AbstractExtractor implements ExtractorExceptionConstants
{
    static private final Logger s_logger = Logger.getLogger(Extractor.class);

    private String REGEX_COLON = ":[\\s|\\t|\\r|\\n|\u00a0]*\"";
    private String REGEX_BRACKETS = ":[\\s|\\t|\\r|\\n|\u00a0]*\\[[\\s|\\t|\\r|\\n|\u00a0]*\"";
    private static String[] invalidHtmlTagCharacters = new String[]
    	    { "{", "}", "%", "^", "~", "!", "&", "*", "(", ")", "?" };
	private static final String PLACEHOLDER_LEFT_TAG = "GS_PLACEHOLDER_LEFT_TAG";
	private static final String PLACEHOLDER_RIGHT_TAG = "GS_PLACEHOLDER_RIGHT_TAG";
	private static final String PLACEHOLDER_LEFT_NATIVE = "GS_PLACEHOLDER_LEFT_NATIVE";
	private static final String PLACEHOLDER_RIGHT_NATIVE = "GS_PLACEHOLDER_RIGHT_NATIVE";
    private Output m_output = null;
    private Filter m_elementPostFilter = null;
    private String m_postFormat = null;
    
    public Extractor()
    {
        super();
    }

    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_JSON);
    }

    private SidFilter getRootJsonSidFilter()
    {
        SidFilter rootJsonSidFilter = null;
        Filter rootFilter = getRootFilter();
        if (rootFilter != null && rootFilter instanceof XMLRuleFilter)
        {
            XMLRuleFilter xmlFilter = (XMLRuleFilter) rootFilter;
            XmlFilterHelper xmlFilterHelper = new XmlFilterHelper(xmlFilter);
            try
            {
                xmlFilterHelper.init();
            }
            catch (Exception e)
            {
                s_logger.error(e);
                return rootJsonSidFilter;
            }
            
            long sidFilterId = xmlFilterHelper.getXmlFilterTags().getSidFilterId();
            long secondarySidFilter = Long.parseLong(xmlFilterHelper.getSecondarySidFilter());
            
            if (sidFilterId > 0)
            {
                SidFilter sf = HibernateUtil.get(SidFilter.class, sidFilterId);
                if (sf != null && sf.getType() == 4)
                {
                    rootJsonSidFilter = sf;
                }
            }
            
            if (rootJsonSidFilter == null && secondarySidFilter > 0)
            {
                SidFilter sf = HibernateUtil.get(SidFilter.class, secondarySidFilter);
                if (sf != null)
                {
                    rootJsonSidFilter = sf;
                }
            }
        }
        
        return rootJsonSidFilter;
    }
    
    public void extract() throws ExtractorException
    {
        this.setFormat();
        
        SidFilter rootJsonSidFilter = getRootJsonSidFilter();

        m_output = getOutput();
        try
        {
            Pattern colonP = Pattern.compile(REGEX_COLON);
            Pattern bracketsP = Pattern.compile(REGEX_BRACKETS);
            Filter mainFilter = getMainFilter();
            JsonFilter jsonFilter = null;
            if (mainFilter != null && mainFilter instanceof JsonFilter)
            {
                jsonFilter = (JsonFilter) mainFilter;
            }
            boolean isEnableSidSupport = false;
            long elementPostFilterId = -1;
            String elementPostFilterTableName = null;
            if (jsonFilter != null)
            {
                isEnableSidSupport = jsonFilter.isEnableSidSupport();
                if (rootJsonSidFilter != null)
                {
                    isEnableSidSupport = true;
                }
                
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
                
                SidFilter sidFilter = jsonFilter.getSidFilter();
                if (sidFilter != null)
                {
                    ArrayList<GlobalExclusionFilterSid> exclusionSids = GlobalExclusionFilterHelper.getAllEnabledGlobalExclusionFilters(sidFilter);
                    
                    if (rootJsonSidFilter != null)
                    {
                        exclusionSids.addAll(GlobalExclusionFilterHelper.getAllEnabledGlobalExclusionFilters(rootJsonSidFilter));
                    }
                    m_output.setExclusionFilterSids(exclusionSids);
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
                if (chr == ':' && !containBrackets)
                    containColon = true;
                
                if (tranChar && chr == '\\')
                {
                    countEscapes++;
                }

                //When value does not contain colons, go here
                if (containColon && !tranChar)
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
                //When the key value is an array, go here
                else if (containBrackets && !tranChar)
                {
                    if (chr == '"')
                    {
                        tranChar = true;
                        m_output.addSkeleton(skeletonBuffer.append(chr).toString());
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
                        //When value contains double quotes or escapes, go here
                        if ((countEscapes == 1 && chr != '\\') || (countEscapes > 1 && chr != '\\'))
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
                                    //When html filter parsing sentences wrong, go here
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
        XmlEntities entity = new XmlEntities();
        str = protectInvalidTags(str);
        str = replaceAllTagSlash(str);
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
                    extractString(new StringReader(chunk), true, sid);
                    break;

                case DocumentElement.SKELETON:
                    String skeleton = ((SkeletonElement) element).getSkeleton();
                    skeleton = entity.decodeStringBasic(skeleton);
                    if (skeleton.contains("\""))
                    {
                        StringBuffer strBuffer = new StringBuffer();
                        char[] chrArr = skeleton.toCharArray();
                        for (int i = 0; i < chrArr.length; i++)
                        {
                            if (chrArr[i] == '"')
                            {
                                if (i == 0 || (i > 0 && chrArr[i - 1] != '\\'))
                                {
                                    strBuffer.append("\\");
                                }
                            }
                            strBuffer.append(chrArr[i]);
                        }
                        skeleton = entity.encodeStringBasic(strBuffer.toString());
                    }
                    else
                    {
                        skeleton = entity.encodeStringBasic(skeleton);
                    }
                    m_output.addSkeletonTmx(skeleton);
                    break;
            }
        }
    }
    
    /**
     * 	0001845: Translatable attribute in the html post filter does not work for json filter
     * 	0002179: InActive start finished with does not work for JSON filter in some cases
     * */
    private String replaceAllTagSlash(String str){
		StringBuffer buffer = new StringBuffer();
    	Pattern startDecode = Pattern.compile("<[^>|^/]*>");
    	Matcher mDecode = startDecode.matcher(str);
		String str01 = "";
		String str02 = "";
		int i = 0;
		while (mDecode.find())
		{
			str01 = str.substring(i, mDecode.start());
			buffer.append(str01);
			str02 = str.substring(mDecode.start(),mDecode.end()).replace("\\\"", "\"");
			buffer.append(str02);
			i = mDecode.end();
		}
		
		if (i < str.length())
		{
			buffer.append(str.substring(i, str.length()));
		}
		if (buffer.toString() != null && buffer.length() > 0)
		{
			str = buffer.toString();
			buffer = new StringBuffer();
		}
		
		Pattern startEncode = Pattern.compile("&lt;[^>|^/]*&gt;");
		Matcher mEecode = startEncode.matcher(str);
		int j = 0;
		while (mEecode.find())
		{
			str01 = str.substring(j, mEecode.start());
			buffer.append(str01);
			str02 = str.substring(mEecode.start(),mEecode.end()).replace("\\\"", "\"");
			buffer.append(str02);
			j = mEecode.end();
		}
		if (j < str.length())
		{
			buffer.append(str.substring(j, str.length()));
		}
		
		if (buffer.toString() != null && buffer.length() > 0)
		{
			str = buffer.toString();
			buffer = new StringBuffer();
		}
		
		return str;
    }
    
    private void extractString(Reader p_input, boolean p_postFiltered, String sid)
    {
        Vector vTokenBuf = new Vector(200, 50);
        Parser parser = new Parser(p_input);
        PTToken token = parser.getNextToken();

        while (token.m_nType != PTToken.EOF)
        {
            vTokenBuf.add(token);
            token = parser.getNextToken();
        }

        addTokensToOutput(vTokenBuf, sid, p_postFiltered);
    }
   
    private void addTokensToOutput(Vector p_vTokens,String sid, boolean p_postFiltered)
    {
        PTTmxController TmxCtrl = new PTTmxController();
        PTEscapeSequence PTEsc = new PTEscapeSequence();

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
    
    @Override
    public void loadRules() throws ExtractorException
    {
        
    }

}
