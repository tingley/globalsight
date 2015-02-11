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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.edit.EditUtil;

public class CustomizeReportsParamXmlHandler 
{
    static private final Logger s_logger =
        Logger.getLogger(
                CustomizeReportsParamXmlHandler.class);
    
    /** The JobInfoParam XML. **/
    static private String s_jobInfoParamXml = null;
    
    static private final String JOBINFOPARAM_XML_URL =
        "/globalsight/envoy/administration/reports/customizeReportsParamXml.jsp";
    
    /**
     * Returns the JobInfoParamXml which describes all the parameters
     * in the customize job report and their hierarchy.
     *
     * @return String of XML
     */
    static public synchronized String getJobInfoParamXml()
    {   
        if (s_jobInfoParamXml == null)
        {
            try
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();

                //http is ok for a reference to this host
                StringBuffer paramXmlUrl = new StringBuffer("http://");
                paramXmlUrl.append(sc.getStringParameter(
                    SystemConfigParamNames.SERVER_HOST));
                paramXmlUrl.append(":");
                paramXmlUrl.append(sc.getStringParameter(
                    SystemConfigParamNames.SERVER_PORT));
                paramXmlUrl.append(JOBINFOPARAM_XML_URL);

                URL u = new URL(paramXmlUrl.toString());
                InputStream is = u.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s = null;
                StringBuffer sb = new StringBuffer();

                while ((s = br.readLine()) != null)
                {
                    sb.append(s).append("\r\n");
                }
                br.close();

                s_jobInfoParamXml = sb.toString();
            }
            catch (Exception ex)
            {
                s_logger.error ("Failed to read customizeReportsJobInfoParam xml.", ex);
                s_jobInfoParamXml = null;
            }
        }

        return s_jobInfoParamXml;
    }

    public static String parseParamXml(ResourceBundle bundle, 
                                       String paramXml)
    {
        // Run xml through parser to look up the strings in the resource bundle
        Document doc = null;
        try
        {
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(new InputSource(new StringReader(paramXml)));
            doc = parser.getDocument();

            Element root = doc.getDocumentElement();
            NodeList categories = root.getElementsByTagName("category");
            for (int i=0; i < categories.getLength(); i++)
            {
                Element category = (Element)categories.item(i);
                String id = category.getAttributes().item(0).getNodeValue();
                category.setAttribute("label", bundle.getString(id));
            }
            NodeList params = root.getElementsByTagName("param");
            for (int i = 0; i < params.getLength(); i++)
            {
                Element param = (Element)params.item(i);
                String id = param.getAttributes().item(0).getNodeValue();
                param.setAttribute("label", bundle.getString(id));
                param.setAttribute("set", "false");
            }
            
            Source source = new DOMSource((Element) doc.getDocumentElement());
            StringWriter out = new StringWriter();
            StreamResult result = new StreamResult(out);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty("encoding", "UTF-8");
            xformer.setOutputProperty("indent", "no");
            xformer.transform(source, result);
            return EditUtil.toJavascript(result.getWriter().toString());
        }
        catch (Exception ex)
        {
            s_logger.error ("Failed to parse customizeReportsJobDetailParam xml.", ex);
            return null;
        }
    }
    
}
