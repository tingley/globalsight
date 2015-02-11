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
package com.globalsight.cxe.adapter.msoffice;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;

/**
 * This is a util class that used to repair exporting office 2010 files.
 */
public class OfficeXmlRepairer
{
	static private final Logger logger = Logger
            .getLogger(OfficeXmlRepairer.class);
	
    private static String s_wpStartTag = "<w:p ";
    private static String s_wpEndTag = "</w:p>";
    private static String s_wpPrStartTag = "<w:pPr";
    private static String s_wpPrEndTag = "</w:pPr>";
    private static String s_wbidiTag = "<w:bidi/>";
	private static String s_gtMark = ">";
	
	private static String s_apStartTag = "<a:p ";
	private static String s_apStartTag2 = "<a:p>";
    private static String s_apEndTag = "</a:p>";
    private static String s_apPrRtl = "rtl=\"1\"";
    private static String s_apPrStartTag = "<a:pPr";
	
    private static String s_cellXfsStartTag = "<cellXfs ";
    private static String s_cellXfsEndTag = "</cellXfs>";
    private static String s_xfStartTag = "<xf ";
    private static String s_xfEndTag = "</xf>";
	
	public static void repair(String path)
	{
		try 
		{
			List<OfficeRepairer> repairers = new ArrayList<OfficeRepairer>();
			repairers.add(new WordRepairer(path));
			repairers.add(new PptxRepairer(path));
			repairers.add(new ExcelRepairer(path));
			
			for (OfficeRepairer repairer : repairers)
			{
				repairer.repairFiles();
			}
		} 
		catch (Exception e) 
		{
			logger.error(e);
		}
    }
	
	/**
	 * For Right to Left languages in office XML
	 * @param xmlContent
	 * @return
	 */
	public static String fixRtlLocale(String xmlContent)
	{
	    if (xmlContent == null || xmlContent.length() == 0)
	    {
	        return xmlContent;
	    }
	    
	    xmlContent = xmlContent.trim();
	    
	    if (xmlContent.endsWith("</w:document>"))
	    {
	        String result = fixRtlDocumentXmlWP(xmlContent);
	        //result = fixRtlDocumentXmlWR(result);
	        
	        return result;
	    }
	    else if (xmlContent.contains("</p:sld>"))
	    {
	        return fixRtlSlideXmlAP(xmlContent);
	    }
	    else if (xmlContent.contains("</styleSheet>"))
	    {
	        return fixRtlSheetStylesXml(xmlContent);
	    }
	    
	    return xmlContent;
	}

    private static String fixRtlDocumentXmlWP(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(src, 0, s_wpStartTag, s_wpEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            // add w:bidi for w:p in w:document XML
            if (v.contains(s_wbidiTag))
            {
                result.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0, s_wpPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    String sss = v.substring(0, 1);
                    tempSi = StringIndex.getValueBetween(temp, 0, sss, s_gtMark);
                    result.append(sss);
                    result.append(tempSi.value);
                    result.append(s_gtMark);
                    result.append(s_wpPrStartTag).append(s_gtMark);
                    result.append(s_wbidiTag).append(s_wpPrEndTag);
                    result.append(temp.substring(tempSi.end + 1));
                }
                else
                {
                    result.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        result.append(tempV.substring(0, tempV.length() - 1));
                        result.append(s_gtMark);
                        result.append(s_wbidiTag);
                        result.append(s_wpPrEndTag);
                    }
                    else
                    {
                        result.append(tempV);
                        result.append(s_gtMark);
                        result.append(s_wbidiTag);
                    }
                    result.append(temp.substring(tempSi.end + 1));
                }
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_wpStartTag, s_wpEndTag);
        }

        result.append(src);

        return result.toString();
    }
    
    private static String fixRtlSlideXmlAP(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();

        StringIndex si = StringIndex.getValueBetween(src, 0, s_apStartTag, s_apEndTag);
        if (si == null)
            si = StringIndex.getValueBetween(src, 0, s_apStartTag2, s_apEndTag);
        while (si != null)
        {
            String before = src.substring(0, si.start);
            String v = si.value;
            String after = src.substring(si.end);

            result.append(before);

            // add rtl="1" for a:p in p:sld XML
            if (v.contains(s_apPrRtl))
            {
                result.append(v);
            }
            else
            {
                StringBuffer temp = new StringBuffer(v);
                StringIndex tempSi = StringIndex.getValueBetween(temp, 0, s_apPrStartTag, s_gtMark);
                if (tempSi == null)
                {
                    if (before.endsWith(s_apStartTag2))
                    {
                        result.append("<a:pPr rtl=\"1\"/>");
                        result.append(temp);
                    }
                    else
                    {
                        String sss = v.substring(0, 1);
                        tempSi = StringIndex.getValueBetween(temp, 0, sss, s_gtMark);
                        result.append(sss);
                        result.append(tempSi.value);
                        result.append(s_gtMark);
                        result.append("<a:pPr rtl=\"1\"/>");
                        result.append(temp.substring(tempSi.end + 1));
                    }
                }
                else
                {
                    result.append(temp.substring(0, tempSi.start));
                    String tempV = tempSi.value;
                    if (tempV.endsWith("/"))
                    {
                        result.append(tempV.substring(0, tempV.length() - 1));
                        result.append(" rtl=\"1\"/");
                    }
                    else
                    {
                        result.append(tempV);
                        result.append(" rtl=\"1\"");
                    }
                    result.append(temp.substring(tempSi.end));
                }
            }

            src.delete(0, src.length());
            src.append(after);
            si = StringIndex.getValueBetween(src, 0, s_apStartTag, s_apEndTag);
            if (si == null)
                si = StringIndex.getValueBetween(src, 0, s_apStartTag2, s_apEndTag);
        }

        result.append(src);

        return result.toString();
    }
    
    private static String fixRtlSheetStylesXml(String xmlContent)
    {
        StringBuffer src = new StringBuffer(xmlContent);
        StringBuffer result = new StringBuffer();
        StringIndex si = StringIndex.getValueBetween(src, 0, s_cellXfsStartTag, s_cellXfsEndTag);
        String before = src.substring(0, si.start);
        String xfs = si.value;
        String after = src.substring(si.end);
        result.append(before);
        
        StringBuffer temp = new StringBuffer(xfs);
        si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        while (si != null)
        {
            String b = temp.substring(0, si.start);
            String v = si.value;
            String a = temp.substring(si.end);
            
            if (v.endsWith("/"))
            {
                result.append(b);
                result.append(v.substring(0, v.length() - 1));
                result.append("><alignment readingOrder=\"2\"/></xf");
            }
            else
            {
                si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_xfEndTag);
                b = temp.substring(0, si.start);
                v = si.value;
                a = temp.substring(si.end);
                result.append(b);

                if (v.contains(" readingOrder=\"2\""))
                {
                    result.append(v);
                }
                else if (v.contains(" readingOrder=\"1\""))
                {
                    result.append(v.replace(" readingOrder=\"1\"", " readingOrder=\"2\""));
                }
                else if (v.contains("<alignment "))
                {
                    result.append(v.replace("<alignment ", "<alignment readingOrder=\"2\" "));
                }
                else
                {
                    result.append(v);
                    result.append("<alignment readingOrder=\"2\"/>");
                }
            }

            temp.delete(0, temp.length());
            temp.append(a);
            si = StringIndex.getValueBetween(temp, 0, s_xfStartTag, s_gtMark);
        }

        result.append(temp);
        result.append(after);

        return result.toString();
    }
}
