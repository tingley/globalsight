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

import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.XmlTransformer;

/**
 * Helper class for dealing with XSLT for corpus display.
 */
public class CorpusXsltHelper implements Serializable
{
    private static final long serialVersionUID = -8034326484230535569L;

    private static final Logger c_logger = Logger
            .getLogger(CorpusXsltHelper.class);

    private String m_partialHighlightUrl;

    private String m_fullHighlightUrl;

    private String m_urlPrefix;

    private String m_companyName;

    public CorpusXsltHelper(HttpServletRequest p_request) throws Exception
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        String port = config
                .getStringParameter(SystemConfigParamNames.CXE_NON_SSL_PORT);
        StringBuffer url = new StringBuffer("http://localhost:" + port);
        m_urlPrefix = url.toString();
        url.append("/globalsight/envoy/tm/corpus");
        m_partialHighlightUrl = url.toString() + "/partial_highlight.jsp?tuid=";
        m_fullHighlightUrl = url.toString() + "/full_highlight.jsp?tuid=";
        c_logger.debug("using url: " + m_partialHighlightUrl);
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
    }

    /**
     * Takes in a string of the partial context XML and a tuid to highlight.
     * 
     * @param p_partialContextXml
     * @param p_tuid
     * @return String of HTML to use to display partial context
     */
    public String partialHighlight(String p_partialContextXml, Long p_tuid,
            boolean p_isSource)
    {
        try
        {
            URL xsl = new URL(m_partialHighlightUrl + p_tuid);
            StringReader xmlReader = new StringReader(p_partialContextXml);

            return XmlTransformer.transform(xsl.openStream(), xmlReader);
        }
        catch (Exception e)
        {
            c_logger.error("Could not generate partial highlight.", e);
            return "";
        }
    }

    /**
     * Returns the full highlight HTML.
     * 
     * @param p_xmlurl
     *            -- URL to full GXML
     * @param p_tuid
     * @return String of HTML to use to display full context
     */
    public String fullHighlight(String p_xmlUrl, Long p_tuid,
            boolean p_isSource, String p_viewMode)
    {
        try
        {
            StringBuilder xmlUrl = new StringBuilder();
            xmlUrl.append(m_urlPrefix.replaceAll(" ", "%20"));
            xmlUrl.append(p_xmlUrl.replaceAll(" ", "%20"));
            xmlUrl.append("?").append(CompanyWrapper.CURRENT_COMPANY_ID)
                    .append("=").append(m_companyName);
            URL xml = new URL(xmlUrl.toString());
            URL xsl = new URL(m_fullHighlightUrl + p_tuid);

            return XmlTransformer.transform(xsl.openStream(), xml.openStream());
        }
        catch (Exception e)
        {
            c_logger.error("Could not generate full highlight.", e);
            return "";
        }
    }
}
