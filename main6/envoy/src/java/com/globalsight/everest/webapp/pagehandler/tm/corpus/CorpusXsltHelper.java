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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

import org.apache.xalan.xpath.xdom.XercesLiaison;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTResultTarget;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.corpus.CorpusManagerLocal;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.log.GlobalSightCategory;

/**
 * Helper class for dealing with XSLT for corpus display
 */
public class CorpusXsltHelper implements Serializable
{
	private static final long serialVersionUID = -8034326484230535569L;

	private static final GlobalSightCategory c_logger = (GlobalSightCategory) GlobalSightCategory
			.getLogger(CorpusXsltHelper.class);

	// keep two separate XSLT processors so that source and target pages can be
	// processed
	// at the same time
	private XSLTProcessor m_xsltSource;

	private XSLTProcessor m_xsltTarget;

	private String m_partialHighlightUrl;

	private String m_fullHighlightUrl;

	private String m_urlPrefix;

	private String m_companyName;

	//
	// Constructor
	//
	public CorpusXsltHelper(HttpServletRequest p_request) throws Exception
	{
		m_xsltSource = XSLTProcessorFactory.getProcessor(new XercesLiaison());
		m_xsltTarget = XSLTProcessorFactory.getProcessor(new XercesLiaison());

		SystemConfiguration config = SystemConfiguration.getInstance();
		String port = config.getStringParameter(SystemConfigParamNames.CXE_NON_SSL_PORT);
		StringBuffer url = new StringBuffer("http://localhost:" + port);
		m_urlPrefix = url.toString();
		url.append("/globalsight/envoy/tm/corpus");
		m_partialHighlightUrl = url.toString() + "/partial_highlight.jsp?tuid=";
		m_fullHighlightUrl = url.toString() + "/full_highlight.jsp?tuid=";
		c_logger.debug("using url: " + m_partialHighlightUrl);
		m_companyName = UserUtil.getCurrentCompanyName(p_request);
	}

	/**
	 * Takes in a string of the partial context XML and a tuid to higlight
	 * 
	 * @param p_partialContextXml
	 * @param p_tuid
	 * @param p_isSource
	 *            true if source context
	 * @return String of HTML to use to display partial context
	 */
	public String partialHighlight(String p_partialContextXml, Long p_tuid,
			boolean p_isSource)
	{
		try
		{
			URL xsl = new URL(m_partialHighlightUrl + p_tuid);
			StringReader xmlReader = new StringReader(p_partialContextXml);
			XSLTInputSource xml = new XSLTInputSource(xmlReader);
			return process(xsl, xml, p_isSource);
		}
		catch (Exception e)
		{
			c_logger.error("Could not generate partial highlight.", e);
			return "";
		}
	}

	/**
	 * Returns the full highlight HTML
	 * 
	 * @param p_xmlurl --
	 *            URL to full GXML
	 * @param p_tuid
	 * @param p_isSource
	 *            true if source context
	 * @param p_viewMode
	 *            list or text
	 * @return String of HTML to use to display full context
	 */
	public String fullHighlight(String p_xmlUrl, Long p_tuid,
			boolean p_isSource, String p_viewMode)
	{
		try
		{			
			StringBuffer xmlUrl = new StringBuffer();
			xmlUrl.append(m_urlPrefix.replaceAll(" ", "%20"));
			xmlUrl.append(p_xmlUrl.replaceAll(" ", "%20"));
			xmlUrl.append("?").append(CompanyWrapper.CURRENT_COMPANY_ID).append("=").append(m_companyName);
			URL xmlurl = new URL(xmlUrl.toString());
			
			//read all content into one StringBuffer
			InputStream in = xmlurl.openStream();
			String encoding = CorpusManagerLocal.UTF8;
			BufferedReader br = new BufferedReader(new InputStreamReader(in,encoding));
			StringBuffer allContentSB = new StringBuffer();
			String line = br.readLine();
			while (line != null)
			{
				allContentSB.append(line).append("\r\n");
				line = br.readLine();
			}
			
			//
			StringBuffer outputSB = new StringBuffer();
			outputSB.append("<html>").append("\r\n");
			outputSB.append("<head>").append("\r\n");
			outputSB.append("    <META CONTENT=\"no-cache\" HTTP-EQUIV=\"Cache-Control\">").append("\r\n");
			outputSB.append("    <META CONTENT=\"0\" HTTP-EQUIV=\"Expires\">").append("\r\n");
			outputSB.append("    <META CONTENT=\"text/html; charset=UTF-8\" HTTP-EQUIV=\"Content-Type\">").append("\r\n");
			outputSB.append("</head>").append("\r\n");
			outputSB.append("<body>").append("\r\n");

			String all = allContentSB.toString();
			int startIndex = all.indexOf("<segment");
			int endIndex = all.indexOf("</segment>");
			if (startIndex == -1 || endIndex == -1) 
			{
				outputSB.append("<P>")
		                .append("<font color=\"red\">").append("There are no \"segment\" elements in gxml file.").append("</font>")
		                .append("</P>").append("\r\n");
			}
			while (startIndex > -1 && endIndex > -1 && startIndex < endIndex)
			{
//				String str1 = all.substring(0, startIndex);
				String str2 = all.substring(startIndex,endIndex+10);
				String str3 = all.substring(endIndex+10);
				
				int subStartIndex = str2.indexOf(">");
				int subEndIndex = str2.indexOf("</segment>");
//				String str2_1 = str2.substring(0, subStartIndex+1);
				String str2_2 = str2.substring(subStartIndex+1, subEndIndex);
//				String str2_3 = str2.substring(subEndIndex);
				
				str2_2 = removeContentEntity(str2_2);
				outputSB.append("<P>")
				        .append("<font color=\"black\">").append(str2_2).append("</font>")
				        .append("</P>").append("\r\n");
				
				all = str3;
				startIndex = all.indexOf("<segment");
				endIndex = all.indexOf("</segment>");
			}
			outputSB.append("</body>").append("\r\n");
			outputSB.append("</html>").append("\r\n");

//			ByteArrayInputStream byteIs = new ByteArrayInputStream(outputSB.toString().getBytes());
//			XSLTInputSource xml = new XSLTInputSource(byteIs);
//			
//			return process(xsl, xml, p_isSource);
			return outputSB.toString();
		}
		catch (Exception e)
		{
			c_logger.error("Could not generate full highlight.", e);
			return "";
		}
	}

	/**
	 * Calls the appropriate XSLT processor to generate highlight HTML
	 * 
	 * @param p_url
	 * @param p_xml
	 * @param p_isSource
	 * @return
	 * @exception Exception
	 */
	private String process(URL p_url, XSLTInputSource p_xml, boolean p_isSource)
			throws Exception
	{
		XSLTInputSource xsl = new XSLTInputSource(p_url.openStream());
		StringWriter sw = new StringWriter();
		XSLTResultTarget out = new XSLTResultTarget(sw);
		if (p_isSource)
			m_xsltSource.process(p_xml, xsl, out);
		else
			m_xsltTarget.process(p_xml, xsl, out);
		return sw.getBuffer().toString();
	}
	
	/**
	 * Remove all contents between "<",">" or "&lt;","&gt;".
	 * 
	 * @param p_string
	 * @return String
	 */
	private String removeContentEntity(String p_string)
	{
		if (p_string == null || "".equals(p_string.trim())) {
			return "";
		}
		
		String str = p_string.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		boolean hasLtGt = true;
		while (hasLtGt)
		{
			int start = str.indexOf("<");
			int end = str.indexOf(">");
			if (start > -1 && end > -1 && start < end)
			{
				String str1 = str.substring(0, start);
				String str2 = str.substring(end+1);
				str = str1 + str2;
			} 
			else 
			{
				hasLtGt = false;
			}	
		}
		
		return str;
	}
	
}
