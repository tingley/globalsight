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

package com.globalsight.everest.coti.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;
import com.globalsight.util.GlobalSightLocale;

/**
 * COTI.XML parser class
 * 
 * @author Wayzou
 * 
 */
public abstract class COTIXmlBase
{
    protected long companyId;
    protected Element root = null;

    public static COTIXmlBase getInstance(String cotiXml) throws Exception
    {
        String cotiXmlWithoutBom = null;
        for (int i = 0; i < cotiXml.length(); i++)
        {
            char c = cotiXml.charAt(i);
            
            if (c == '<')
            {
                cotiXmlWithoutBom = cotiXml.substring(i);
                break;
            }
        }
        
        StringReader sr = new StringReader(cotiXmlWithoutBom);
        InputSource is = new InputSource(sr);
        COTIXmlParseHandler h = new COTIXmlParseHandler();
        GsDOMParser parser = new GsDOMParser(
                "org.apache.xerces.jaxp.GSDocumentBuilderFactoryImpl");
        // don't read external DTDs
        parser.setEntityResolver(h);
        // provide detailed error report
        parser.setErrorHandler(h);
        parser.setValidating(false);

        // parse and create DOM tree
        Document document = parser.parse(is);
        Element root = document.getDocumentElement();
        Element transElement = getFirstElement(root, "translation");
        COTIXmlBase r = null;

        NodeList nl = transElement.getChildNodes();
        if (nl != null && nl.getLength() > 0)
        {
            for (int i = 0; i < nl.getLength(); i++)
            {
                Node n = nl.item(i);

                if (Node.ELEMENT_NODE == n.getNodeType())
                {
                    r = new COTIXmlSchemaST4(root);
                    break;
                }
            }
        }

        if (r == null)
        {
            r = new COTIXmlStandard(root);
        }

        return r;
    }

    /**
     * Create COTI package object from coti.xml
     * 
     * @return
     */
    public abstract COTIPackage createPackage();

    /**
     * Create COTI project object from coti.xml without package id
     * 
     * @return
     * @throws Exception
     */
    public abstract COTIProject createProject() throws Exception;

    /**
     * Create COTI document objects without project id.
     * 
     * @return
     */
    public abstract List<COTIDocument> createDocuments();

    public abstract String getCotiCreationDate();

    public abstract String getCotiVersion();

    public abstract String getCotiCreator();

    public abstract String getCotiLevel();

    public abstract String getProjectName();

    public abstract String getProjectId();

    public abstract String getSourceLang();

    public abstract String getTargetLang();

    public abstract String getSubject();

    public Element getFirstProjectElement()
    {
        return (Element) root.getElementsByTagName("project").item(0);
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public static Element getFirstElement(Element parent, String name)
    {
        return (Element) parent.getElementsByTagName(name).item(0);
    }
}
