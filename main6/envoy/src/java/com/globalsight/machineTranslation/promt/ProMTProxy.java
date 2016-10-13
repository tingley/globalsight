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
package com.globalsight.machineTranslation.promt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.gxml.GxmlElement;

/**
 * Acts as a proxy to the free translation Machine Translation Service.
 */
public class ProMTProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger s_logger = Logger
            .getLogger(ProMTProxy.class);

    private DiplomatAPI m_diplomat = null;
    private int count = 0;
    
    public ProMTProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_PROMT;
    }

    /**
     * Returns true if the given locale pair is supported by MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        MachineTranslationExtentInfo ptsInfo = getProMTInfoBySrcTrgLocale(
                p_sourceLocale,
                p_targetLocale);
        if (ptsInfo == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * As PROMT does not support batch translation, this is the unique API to
     * translate text in PTS implementation.
     */
    protected String doTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
        if (p_string == null || "".equals(p_string.trim()))
        {
            return "";
        }

        String result = "";
        // get ptsUrlFlag

        // Translate via PTS9 APIs
        try
        {
            MachineTranslationExtentInfo ptsInfo = getProMTInfoBySrcTrgLocale(
                    p_sourceLocale, p_targetLocale);

            long dirId = -1;
            String topicTemplateId = "";
            if (ptsInfo != null)
            {
                dirId = ptsInfo.getLanguagePairCode();
                topicTemplateId = ptsInfo.getDomainCode();

                ProMtPts9Invoker invoker = getProMtPts9Invoker();
                int times = 0;
                String stringBak = new String(p_string);
                while (times < 2)
                {
                    // All segments from XLF file are re-wrapped,before send
                    // them to PTS9,need revert them.
                    Map paramMap = getMtParameterMap();
                    GxmlElement ge = null;
                    List<String> idList = null;
                    List<String> xList = null;
                    boolean isXlf = MTHelper.needSpecialProcessingXlfSegs(paramMap);
                    boolean containTags = isContainTags();
                    if (isXlf && containTags)
                    {
                        ge = getSourceGxmlElement(p_string);
                        if (ge != null)
                        {
                            idList = SegmentUtil2.getAttValuesByName(ge, "id");
                            xList = SegmentUtil2.getAttValuesByName(ge, "x");
                        }
                        String locale = p_sourceLocale.getLanguage() + "_"
                                + p_sourceLocale.getCountry();
                        stringBak = MTHelper.wrappText(p_string, locale);
                        stringBak = MTHelper.revertXlfSegment(stringBak, locale);
                        stringBak = MTHelper.encodeLtGtInGxmlAttributeValue(stringBak);
                    }
                    // Send to PTS9 for translation
                    result = invoker.translateText(dirId, topicTemplateId, stringBak);
                    if (result != null && !result.startsWith("-1 Error")
                            && isXlf && containTags)
                    {
                        result = result.replaceAll("_gt;_", ">");
                        /**
                            result = result.replaceAll("&#x9;", "&amp;#x9;");// while-space
                            result = result.replaceAll("&#xa;", "&amp;#xa;");// \r
                            result = result.replaceAll("&#xd;", "&amp;#xd;");// \n
                            // handle '<' and '"' in attribute value
                            result = encodeGxmlAttributeEntities2(result);
                            */
                        // handle single '&' in MT translation
                        result = MTHelper.encodeSeparatedAndChar(result);
                    }
                    // Parse the translation back
                    if (isXlf && containTags)
                    {
                        DiplomatAPI api = getDiplomatApi();
                        SegmentNode sn = SegmentUtil2.extractSegment(api,
                                result, "xlf", p_sourceLocale);
                        if (sn != null)
                        {
                            result = sn.getSegment();
                            // Handle entity
                            result = MTHelper.resetIdAndXAttributesValues(
                                    result, idList, xList);
                        }
                        else
                        {
                            result = null;
                        }
                    }
                    if (result != null && !"null".equalsIgnoreCase(result)
                            && !result.startsWith("-1"))
                    {
                        break;
                    }
                    times++;
                }

                if (result != null && result.startsWith("-1 Error:"))
                {
                    s_logger.error("Failed to get translation from PTS9 engine.");
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.error(result);
                    }
                }
                if (result == null || "null".equalsIgnoreCase(result)
                        || result.startsWith("-1"))
                {
                    result = "";
                }
            }
        }
        catch (Exception ex)
        {
//                s_logger.error(ex.getMessage(), ex);
        }

        return result;
    }

    /**
     * Get a PROMT invoker object for PTS version 8.
     * 
     * @return ProMtInvoker object
     */
    private ProMtInvoker getProMtInvoker()
    {
        ProMtInvoker invoker = null;

        HashMap paramMap = getMtParameterMap();
        String ptsurl = (String) paramMap.get(MachineTranslator.PROMT_PTSURL);
        String username = (String) paramMap
                .get(MachineTranslator.PROMT_USERNAME);
        String password = (String) paramMap
                .get(MachineTranslator.PROMT_PASSWORD);

        if (ptsurl != null && !"".equals(ptsurl.trim())
                && !"null".equals(ptsurl))
        {
            if (username != null)
            {
                invoker = new ProMtInvoker(ptsurl, username, password);
            }
            else
            {
                invoker = new ProMtInvoker(ptsurl);
            }
        }

        return invoker;
    }
    
    /**
     *  Get a PROMT invoker object for PTS version 9.
     *  
     * @return ProMtPts9Invoker object.
     */
    private ProMtPts9Invoker getProMtPts9Invoker()
    {
        ProMtPts9Invoker invoker = null;
        
        if (invoker == null)
        {
            HashMap paramMap = getMtParameterMap();
            String ptsurl = (String) paramMap.get(MachineTranslator.PROMT_PTSURL);
            String username = (String) paramMap
                    .get(MachineTranslator.PROMT_USERNAME);
            String password = (String) paramMap
                    .get(MachineTranslator.PROMT_PASSWORD);

            if (ptsurl != null && !"".equals(ptsurl.trim())
                    && !"null".equals(ptsurl))
            {
                if (username != null)
                {
                    invoker = new ProMtPts9Invoker(ptsurl, username, password);
                }
                else
                {
                    invoker = new ProMtPts9Invoker(ptsurl);
                }
            }            
        }

        return invoker;
    }
    
    /**
     * Try to find a matched PROMT setting for specified source and target
     * languages.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return
     */
    private MachineTranslationExtentInfo getProMTInfoBySrcTrgLocale(
            Locale p_sourceLocale,
            Locale p_targetLocale)
    {
        MachineTranslationExtentInfo result = null;
        String lpName = getLanguagePairName(p_sourceLocale, p_targetLocale);

        MachineTranslationProfile mtProfile = getMTProfile();
        if (mtProfile != null)
        {
            Set promtInfos = mtProfile.getExInfo();
            if (promtInfos != null && promtInfos.size() > 0)
            {
                Iterator promtInfoIter = promtInfos.iterator();
                while (promtInfoIter.hasNext())
                {
                    MachineTranslationExtentInfo ptsInfo = (MachineTranslationExtentInfo) promtInfoIter
                            .next();
                    String dirName = ptsInfo.getLanguagePairName();
                    if (dirName != null && dirName.equalsIgnoreCase(lpName))
                    {
                        result = ptsInfo;
                        break;
                    }
                }
            }
        }

        return result;
    }

    private MachineTranslationProfile getMTProfile()
    {
        MachineTranslationProfile result = null;

        HashMap paramMap = getMtParameterMap();
        String mtId = (String) paramMap.get(MachineTranslator.MT_PROFILE_ID);
        try
        {
            result = MTProfileHandlerHelper.getMTProfileById(mtId);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get translation memory profile for profile ID : "
                    + mtId);
        }

        return result;
    }

    /**
     * Get language pair name. Note that PROMT only support simplified Chinese.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return
     */
    private String getLanguagePairName(Locale p_sourceLocale,
            Locale p_targetLocale)
    {
        if (p_sourceLocale == null || p_targetLocale == null)
        {
            return null;
        }

        String srcLang = p_sourceLocale.getDisplayLanguage(Locale.ENGLISH);
        String srcCountry = p_sourceLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(srcLang) && "China".equals(srcCountry))
        {
            srcLang = "Chinese (Simplified)";
        }

        String trgLang = p_targetLocale.getDisplayLanguage(Locale.ENGLISH);
        String trgCountry = p_targetLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(trgLang) && "China".equals(trgCountry))
        {
            trgLang = "Chinese (Simplified)";
        }

        return (srcLang + "-" + trgLang);
    }
    
//    /**
//     * If the source page data type is XLF,need revert the segment content.
//     * 
//     * @return boolean
//     */
//    private boolean needRevertXlfSegment()
//    {
//        SourcePage sp = getSourcePage();
//        String spDataType = null;
//        if (sp != null)
//        {
//            ExtractedSourceFile esf = (ExtractedSourceFile) sp.getExtractedFile();
//            spDataType = esf.getDataType();
//        }
//        if (spDataType != null
//                && ("xlf".equalsIgnoreCase(spDataType) || "xliff"
//                        .equalsIgnoreCase(spDataType)))
//        {
//            return true;
//        }
//
//        return false;
//    }
    
//    private GlobalSightLocale getSourceLocale()
//    {
//        SourcePage sp = MTHelper.getSourcePage(getMtParameterMap());
//        GlobalSightLocale sourceLocale = null;
//        if (sp != null)
//        {
//            sourceLocale = sp.getGlobalSightLocale();
//        }
//        
//        return sourceLocale;
//    }
    
    /**
     * Retrieve source page by source page ID.
     * 
     * @return
     */
//    private SourcePage getSourcePage()
//    {
//        HashMap paramMap = getMtParameterMap();
//        Long sourcePageID = (Long) paramMap
//                .get(MachineTranslator.SOURCE_PAGE_ID);
//        SourcePage sp = null;
//        try
//        {
//            sp = ServerProxy.getPageManager().getSourcePage(sourcePageID);
//        }
//        catch (Exception e)
//        {
//            if (s_logger.isDebugEnabled())
//            {
//                s_logger.error("Failed to get source page by pageID : "
//                        + sourcePageID + ";" + e.getMessage());
//            }
//        }
//
//        return sp;
//    }

//    public static String wrappText(String p_text, String locale)
//    {
//        if (p_text == null || p_text.trim().length() == 0)
//        {
//            return null;
//        }
//
//        StringBuffer sb = new StringBuffer();
//        sb.append("<?xml version=\"1.0\"?>");
//        sb.append("<diplomat locale=\"").append(locale).append("\" version=\"2.0\" datatype=\"xlf\">");
//        sb.append("<translatable>");
//        sb.append(p_text);
//        sb.append("</translatable>");
//        sb.append("</diplomat>");
//
//        return sb.toString();
//    }
    
//    public static String revertXlfSegment(String text, String locale)
//    {
//        String result = null;
//
//        try
//        {
//            DiplomatAPI diplomat = new DiplomatAPI();
//            diplomat.setFileProfileId("-1");
//            diplomat.setFilterId(-1);
//            diplomat.setFilterTableName(null);
//            diplomat.setTargetLocale(locale);
//            byte[] mergeResult = diplomat.merge(text, "UTF-8", false);
//            result = new String(mergeResult, "UTF-8");
//        }
//        catch (Exception e)
//        {
//            if (s_logger.isDebugEnabled())
//            {
//                s_logger.error("Failed to revert XLF segment : "
//                        + e.getMessage());
//            }
//        }
//
//        return result;
//    }
    
    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }
    
    private GxmlElement getSourceGxmlElement(String p_segString)
    {
        GxmlElement gxmlElement = null;
        try 
        {
            StringBuffer sb = new StringBuffer();
            sb.append("<segment>").append(p_segString).append("</segment>");
            gxmlElement = SegmentUtil2.getGxmlElement(sb.toString());
        }
        catch (Exception e)
        {
        }
        
        return gxmlElement;
    }
    
    /**
     * Indicate if the segments contain tags.
     * 
     * @return
     */
    private boolean isContainTags()
    {
        HashMap paramMap = getMtParameterMap();
        String containTags = (String) paramMap
                .get(MachineTranslator.CONTAIN_TAGS);
        if (containTags != null && "Y".equalsIgnoreCase(containTags))
        {
            return true;
        }

        return false;
    }
    
     /**
     * XML attribute value can't have '<' and '"', so find all "&lt;" and 
     * "&quot;" in attribute value and encode them again. 
     * So in exported file, there won't be '<' and '"' after decoded.
     * 
     * Not care '>' and '''.
     * 
     * @deprecated
     * @param segement
     * @return
     */
    private static String encodeGxmlAttributeEntities2(String segement)
    {
        // this flag for recording the xml element begin.
        boolean flagXML = false;
        // this flag for recording the attribute begin, because if in "<  >",
        // and begin as double quote, it will be a attribute.
        boolean flagQuote = false;
        
        StringBuffer sb = new StringBuffer();
        StringBuffer attributeSB = new StringBuffer();

        for (int i = 0; i < segement.length(); i++)
        {
            char c = segement.charAt(i);

            if (!flagXML && !flagQuote && c == '<')
            {
                flagXML = true;
            }
            else if (flagXML && !flagQuote && c == '"')
            {
                flagQuote = true;
            }
            else if (flagXML && !flagQuote && c == '>')
            {
                flagXML = false;
            }
            else if (flagXML && flagQuote && c == '"')
            {
                flagQuote = false;
            }
            
            // In element
            if (flagXML)
            {
                //attribute value
                if (flagQuote)
                {
                    //current char is START '"'.
                    if (c == '"')
                    {
                        sb.append(c);//append START '"'
                    }
                    //in attribute value
                    else
                    {
                        attributeSB.append(c);
                    }
                }
                else
                {
                    //current char is END '"'.
                    if (c == '"')
                    {
                        if (attributeSB != null && attributeSB.length() > 0)
                        {
                            String str = attributeSB.toString();
                            str = str.replaceAll("&lt;", "&amp;lt;");
                            str = str.replaceAll("&quot;", "&amp;quot;");
                            sb.append(str);
                            attributeSB = new StringBuffer();
                        }
                        
                        sb.append(c);//append END '"'
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }
            else
            {
                sb.append(c);
            }
            
        }

        return sb.toString();
    }
    
    public static void main(String[] args) throws Exception
    {
        /**
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<diplomat locale=\"en_US\" version=\"2.0\" datatype=\"xlf\">");
        sb.append("<translatable>");
        sb.append("<ph type=\"ph\" id=\"1\" x=\"1\">&lt;ph id=&quot;1&quot; x=&quot;&amp;lt;Fragment&gt;&quot;&gt;{1}&lt;/ph&gt;</ph>PayPal Revolving Credit<ph type=\"ph\" id=\"2\" x=\"2\">&lt;ph id=&quot;2&quot; x=&quot;&amp;lt;/Fragment&gt;&quot;&gt;{2}&lt;/ph&gt;</ph>");
        sb.append("<ph id=\"1\" x=\"&lt;Fragment>\">{1}</ph>PayPal Revolving Credit<ph id=\"2\" x=\"&lt;/Fragment>\">{2}</ph>");
        sb.append("</translatable>");
        sb.append("</diplomat>");
        String result = revertXlfSegment(sb.toString());
        System.out.println(result);
        */
        
        /**
        String p_segment = "<trans-unit><source><ph id=\"3\" x=\"&lt;Fragment>\">{3}</ph>credit1, simulation2, financing3, money4 needs<ph id=\"4\" x=\"&lt;/Fragment>\">{4}</ph></source></trans-unit>";
        String p_dataType = "xlf";
        GlobalSightLocale p_sourceLocale = new GlobalSightLocale("en", "US", true);
        try
        {
            SegmentNode segNode = extractSegment(p_segment, p_dataType,
                    p_sourceLocale);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */
        
        String segString = "<segment><ph type=\"ph\" id=\"1\" x=\"1\">&lt;ph id=\"5\" x=\"&lt;Fragment&gt;\"&gt;{5}&lt;/ph&gt;</ph>The PayPal revolving credit meets your money needs with simplicity.</segment>";
        GxmlElement gxmlElement = SegmentUtil2.getGxmlElement(segString);
        String str = gxmlElement.toGxml("xlf");
        System.out.println(str);
    }
    
}
