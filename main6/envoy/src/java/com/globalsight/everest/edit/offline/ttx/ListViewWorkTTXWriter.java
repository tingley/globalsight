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
package com.globalsight.everest.edit.offline.ttx;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.StringUtil;

public class ListViewWorkTTXWriter extends TTXWriterUnicode
{
    static private final Logger logger = Logger
            .getLogger(ListViewWorkTTXWriter.class);

    public ListViewWorkTTXWriter()
    {
    }

    /**
     * Writes the document front matter information.
     *@param DownloadParams
     */
    protected void writeTTXFrontMatter(DownloadParams p_downloadParams)
    	throws IOException, AmbassadorDwUpException
    {
    	m_outputStream.write("<FrontMatter>");
    	
    	Format format = new SimpleDateFormat("yyyyMMdd HHmmss");
    	String creationDate = format.format(new Date());
    	String creationTool = "GlobalSight";
    	String creationToolVersion = ServerUtil.getVersion();
        m_outputStream.write("<ToolSettings CreationDate="
                + str2DoubleQuotation(creationDate) + " CreationTool="
                + str2DoubleQuotation(creationTool) + " CreationToolVersion="
                + str2DoubleQuotation(creationToolVersion) + "/>");

        // set to "XML" despite of original format
        // TRADOS only recognizes "XML","HTML","PlugInVonverted","RTF" etc, and
        // they are case sensitive.
    	String dataType = "XML";
    	String page_encoding = m_page.getEncoding();
    	String settingName = "GS";
    	String settingsPath = "";
    	GlobalSightLocale srcGSL = p_downloadParams.getSourceLocale();
        String sourceLanguage = srcGSL.getLanguage().toUpperCase() + "-"
                + srcGSL.getCountry().toUpperCase();
    	GlobalSightLocale trgGSL = p_downloadParams.getTargetLocale();
        String targetLanguage = trgGSL.getLanguage().toUpperCase() + "-"
                + trgGSL.getCountry().toUpperCase();
    	String settingsRelativePath = "..\\help\\GS.ini";
    	
        m_outputStream.write("<UserSettings" + " DataType="
                + str2DoubleQuotation(dataType) + " O-Encoding="
                + str2DoubleQuotation(page_encoding) + " SettingsName="
                + str2DoubleQuotation(settingName) + " SettingsPath="
                + str2DoubleQuotation(settingsPath) + " SourceLanguage="
                + str2DoubleQuotation(sourceLanguage) + " TargetLanguage="
                + str2DoubleQuotation(targetLanguage)
                + " TargetDefaultFont=\"\" SourceDocumentPath=\"\""
                + " SettingsRelativePath=\"" + settingsRelativePath
                + "\" PlugInInfo=\"\"/>");
        m_outputStream.write("</FrontMatter>");
        m_outputStream.write("<Body>");
        m_outputStream.write("<Raw>");
    }
    
    /**
     * Writes the document header - encoding, formats, languages etc.
     * 
     * @param DownloadParams
     */
    protected void writeTTXHeaderInfo(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException
    {
        // Encoding
        m_outputStream.write("<ut DisplayText=\"GS:Encoding\">" + "Encoding: "
                + TTX_ENCODING + "</ut>");
        m_outputStream.write(m_strEOL);
        // DocumentFormat
        m_outputStream.write("<ut DisplayText=\"GS:DocumentFormat\">"
                + "Document Format: " + m_page.getDocumentFormat() + "</ut>");
        m_outputStream.write(m_strEOL);
        // PlaceholderFormat
        m_outputStream.write("<ut DisplayText=\"GS:PlaceholderFormat\">"
                + "Placeholder Format: " + m_page.getPlaceholderFormat()
                + "</ut>");
        m_outputStream.write(m_strEOL);
        // SourceLocale
        m_outputStream.write("<ut DisplayText=\"GS:SourceLocale\">"
                + "Source Locale: "
                + changeLocaleToTTXFormat(m_page.getSourceLocaleName())
                + "</ut>");
        m_outputStream.write(m_strEOL);
        // TargetLocale
        m_outputStream.write("<ut DisplayText=\"GS:TargetLocale\">"
                + "Target Locale: "
                + changeLocaleToTTXFormat(m_page.getTargetLocaleName())
                + "</ut>");
        m_outputStream.write(m_strEOL);
        // PageID
        m_outputStream.write("<ut DisplayText=\"GS:PageID\">" + "Page ID: "
                + m_page.getPageId() + "</ut>");
        m_outputStream.write(m_strEOL);
        // WorkflowID
        m_outputStream.write("<ut DisplayText=\"GS:WorkflowID\">"
                + "Workflow ID: " + m_page.getWorkflowId() + "</ut>");
        m_outputStream.write(m_strEOL);
        // TaskID
        m_outputStream.write("<ut DisplayText=\"GS:TaskID\">" + "Task ID: "
                + m_page.getTaskId() + "</ut>");
        m_outputStream.write(m_strEOL);
        //JobID
        m_outputStream.write("<ut DisplayText=\"GS:JobID\">" + "Job ID: "
                + m_page.getJobId() + "</ut>");
        m_outputStream.write(m_strEOL);
        // ExactMatchWordCount
        m_outputStream.write("<ut DisplayText=\"GS:ExactMatchWordCount\">"
                + "Exact Match word count: "
                + m_page.getExactMatchWordCountAsString() + "</ut>");
        m_outputStream.write(m_strEOL);
        // FuzzyMatchWordCount
        m_outputStream.write("<ut DisplayText=\"GS:FuzzyMatchWordCount\">"
                + "Fuzzy Match word count: "
                + m_page.getFuzzyMatchWordCountAsString() + "</ut>");
        m_outputStream.write(m_strEOL);
        // Populate100%TargetSegments
        m_outputStream.write("<ut DisplayText=\"GS:Populate100TargetSegments\">"
                + "Populate 100% Target Segments: "
                + (m_page.isPopulate100() ? "YES" : "NO") + "</ut>");
        m_outputStream.write(m_strEOL);
        // Server Instance ID
        if (m_page.getServerInstanceID() != null)
        {
            m_outputStream.write("<ut DisplayText=\"GS:InstanceID\">"
                    + AmbassadorDwUpConstants.LABEL_SERVER_INSTANCEID + ": "
                    + m_page.getServerInstanceID() + "</ut>");
            m_outputStream.write(m_strEOL);
        }
        // EditAll
        m_outputStream.write("<ut DisplayText=\"GS:EditAll\">" + "Edit all: "
                + m_page.getDisplayTMEditType() + "</ut>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
    }
    
    protected void writeTTXDocBody() throws AmbassadorDwUpException
    {
        OfflineSegmentData osd = null;

        for (ListIterator it = m_page.getSegmentIterator(); it.hasNext();)
        {
            osd = (OfflineSegmentData) it.next();

            try
            {
                writeTranslationUnit(osd, m_page);
            }
            catch (Exception ex)
            {
                throw new AmbassadorDwUpException(ex);
            }
        }
    }

    private void writeTranslationUnit(OfflineSegmentData p_osd,
            OfflinePageData m_page) throws IOException, RegExException
    {
        String srcSegment;
        String trgSegment;
        InternalTextUtil util = new InternalTextUtil(new XliffInternalTag());

        srcSegment = p_osd.getDisplaySourceTextWithNewLinebreaks(String
                .valueOf(TTXConstants.NORMALIZED_LINEBREAK));
        try
        {
            srcSegment = util.preProcessInternalText(srcSegment).getSegment();
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        trgSegment = p_osd.getDisplayTargetTextWithNewLineBreaks(String
                .valueOf(TTXConstants.NORMALIZED_LINEBREAK));
        try
        {
            trgSegment = util.preProcessInternalText(trgSegment).getSegment();
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        // Write "TuId" before Tu
        String tuId = p_osd.getDisplaySegmentID();
        m_outputStream.write("<ut DisplayText=\"TuId:" + tuId + "\">TuId:"
                + tuId + "</ut>");
        m_outputStream.write(m_strEOL);
        
        int TMEditType = m_downloadParams.getTMEditType();
        boolean writeTu = true;
		if (TMEditType != AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH) 
		{
			if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_100
					&& isInContextMatch(p_osd))
				writeTu = false;
			else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE
					&& isExactMatch(p_osd))
				writeTu = false;
			else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY
					&& (isExactMatch(p_osd) || isInContextMatch(p_osd)))
				writeTu = false;
		}

        if (writeTu)
        {
	        // Write Tu
	        float matchPercent = p_osd.getMatchValue();
	        String origin = "manual";
	        if (srcSegment.equals(trgSegment))
	        {
	            origin = TTXConstants.TTX_TU_ORIGIN_UNTRANSLATED;
	        }
			m_outputStream.write("<Tu Origin="
					+ str2DoubleQuotation(origin)
					+ " MatchPercent="
					+ str2DoubleQuotation(String.valueOf(
							Math.floor(matchPercent)).substring(
							0,
							String.valueOf(Math.floor(matchPercent)).indexOf(
									"."))) + ">");
	
	        // write Source
	        String srcLang = null;
	        
	        Tuv sTuv = p_osd.getSourceTuv();
	        if (sTuv != null)
	        {
	            long srcLocaleId = sTuv.getLocaleId();
	            GlobalSightLocale srcLocale = ServerProxy.getLocaleManager()
	                    .getLocaleById(srcLocaleId);
	            srcLang = srcLocale.getLanguage().toUpperCase() + "-"
	                    + srcLocale.getCountry().toUpperCase();
	        }
	        else
	        {
	            srcLang = changeLocaleToTTXFormat(m_page.getSourceLocaleName()).toUpperCase();
	        }
	        
	        m_outputStream.write("<Tuv Lang=" + str2DoubleQuotation(srcLang) + ">");
	        String srcSegment2 = handleTagsInSegment(srcSegment);
	        m_outputStream.write(srcSegment2);
	        m_outputStream.write("</Tuv>");
	
	        // write target
	        Tuv tTuv = p_osd.getTargetTuv();
	        String trgLang = null;
	        if (tTuv != null)
	        {
	            long trgLocaleId = tTuv.getLocaleId();
	            GlobalSightLocale trgLocale = ServerProxy.getLocaleManager()
	                    .getLocaleById(trgLocaleId);
	            trgLang = trgLocale.getLanguage().toUpperCase() + "-"
	                    + trgLocale.getCountry().toUpperCase();
	        }
	        else
	        {
	            trgLang = changeLocaleToTTXFormat(m_page.getTargetLocaleName()).toUpperCase();
	        }
	        
	        m_outputStream.write("<Tuv Lang=" + str2DoubleQuotation(trgLang) + ">");
	        String trgSegment2 = handleTagsInSegment(trgSegment);
	        m_outputStream.write(trgSegment2);
	        m_outputStream.write("</Tuv>");
	        m_outputStream.write("</Tu>");
        }
        else
        {
        	// write UT
        	m_outputStream.write("<ut DisplayText=\"");
        	m_outputStream.write(TTXConstants.GS_LOCKED_SEGMENT);
        	m_outputStream.write("\">");
            //String trgSegment2 = handleTagsInSegment(trgSegment);
            m_outputStream.write(trgSegment);
        	m_outputStream.write("</ut>");
        }
        
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
    }

    /**
     * Convert tags like "[x1]" into trados compatible format "ut"
     * 
     * @param p_segmentString
     * @return
     */
    private String handleTagsInSegment(String p_segmentString)
    {
        if (p_segmentString == null || p_segmentString.trim().length() == 0)
        {
            return p_segmentString;
        }

        StringBuffer result = new StringBuffer();

        String leftStr = null;
        String middleStr = null;
        String rightStr = null;
        String segment = handleSpecialChar(p_segmentString);

        boolean isContinue = true;
        while (isContinue)
        {
            int beginIndex = segment.indexOf("[");
            int endIndex = segment.indexOf("]");
            if (beginIndex == -1 || endIndex == -1)
            {
                result.append(segment);
                isContinue = false;
            }
            else if (beginIndex < endIndex)
            {
                leftStr = segment.substring(0, beginIndex);
                middleStr = segment.substring(beginIndex + 1, endIndex);
                rightStr = segment.substring(endIndex + 1);

                result.append(leftStr);//append left string
                // append middle string as tag
                if (middleStr != null && middleStr.trim().length() > 0
                        && middleStr.startsWith("/") == false)
                {
                    result.append("<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:"
                                    + middleStr + "\">[" + middleStr + "]</ut>");
                }
                else if (middleStr != null && middleStr.trim().length() == 0)
                {
                    result.append("<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:"
                            + " " + "\">[" + middleStr + "]</ut>");
                }
                else
                {
                    result.append("<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:"
                                    + middleStr.substring(1)
                                    + "\">[/"
                                    + middleStr.substring(1) + "]</ut>");
                }

                segment = rightStr;
            }
            else
            {
                result.append(segment.substring(0, beginIndex));//append left string
                middleStr = "";
                segment = segment.substring(beginIndex);
            }
        }

        String resultstr = result.toString();
        resultstr = resultstr.replace(TTXConstants.GS_DOUBLE_LEFT_BRACKETS, "[[");
        resultstr = resultstr.replace(TTXConstants.GS_DOUBLE_RIGHT_BRACKETS, "]");
        
        return resultstr;
    }

    private String handleSpecialChar(String p_segmentString)
    {
        int len = p_segmentString.length();
        StringBuffer result = new StringBuffer(len);
        StringBuffer temp = new StringBuffer();;
        boolean leftOccur = false;
        
        for(int i = 0; i < len; i++)
        {
            char c = p_segmentString.charAt(i);
            
            if (c == '[')
            {
                leftOccur = true;
                temp.append(c);
            }
            else if (c == ']')
            {
                temp.append(c);

                if (i + 1 < len)
                {
                    char nextc = p_segmentString.charAt(i + 1);
                    if (nextc == ']')
                    {
                        continue;
                    }
                }

                // if not continue
                leftOccur = false;
                String tempstr = temp.toString();
                // just handle [[[internal text]]
                if (tempstr.startsWith("[[[") && tempstr.endsWith("]]")
                        && !tempstr.startsWith("[[[[") && !tempstr.endsWith("]]]"))
                {
                    tempstr = tempstr.replace("[[[", "[" + TTXConstants.GS_DOUBLE_LEFT_BRACKETS);
                    tempstr = tempstr.replace("]]", TTXConstants.GS_DOUBLE_RIGHT_BRACKETS + "]");
                }
                result.append(tempstr);
                temp.setLength(0);
            }
            else if (leftOccur)
            {
                temp.append(c);
            }
            else
            {
                result.append(c);
            }
        }
        
        if (temp.length() != 0)
        {
            result.append(temp.toString());
        }
        
        String resultstr = result.toString();
        resultstr = resultstr.replace("[[", TTXConstants.GS_DOUBLE_LEFT_BRACKETS);
        
        return resultstr;
    }

    /**
     * parse string to "string"
     */
    private String str2DoubleQuotation(String str)
    {
        String result = null;
        result = new StringBuffer().append("\"").append(str).append("\"")
                .toString();
        return result;
    }

    public void write(OfflinePageData p_page, OutputStream p_outputStream,
            Locale p_uiLocale) throws IOException, AmbassadorDwUpException
    {

    }
    
    private boolean isExactMatch(OfflineSegmentData data)
    {
        return "DO NOT TRANSLATE OR MODIFY (Locked).".equals(data
                .getDisplayMatchType());
    }

    private boolean isInContextMatch(OfflineSegmentData data)
    {
        String matchType = data.getDisplayMatchType();
        if (matchType != null && matchType.startsWith("Context Exact Match"))
        {
            return true;
        }

        return false;
    }
}
