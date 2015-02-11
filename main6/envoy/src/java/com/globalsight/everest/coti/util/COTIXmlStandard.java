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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.util.GlobalSightLocale;

/**
 * COTI.XML parser class 
 * @author Wayzou
 *
 */
public class COTIXmlStandard extends COTIXmlBase
{
    public COTIXmlStandard(Element root)
    {
        this.root = root;
    }

    /**
     * Create COTI package object from coti.xml
     * 
     * @return
     */
    public COTIPackage createPackage()
    {
        // fileName : <project name>_timestamp.coti
        String projectName = getProjectName();
        String projectTimestamp = getCotiCreationDate();
        String fileName = projectName + "_" + projectTimestamp.replace(":", "")
                + ".coti";

        COTIPackage cp = new COTIPackage();
        cp.setCompanyId(companyId);
        cp.setCreationDate(new Date());
        cp.setFileName(fileName);
        cp.setCotiProjectName(projectName);
        cp.setCotiProjectTimestamp(projectTimestamp);

        return cp;
    }

    /**
     * Create COTI project object from coti.xml without package id
     * 
     * @return
     * @throws Exception
     */
    public COTIProject createProject() throws Exception
    {
        String pid = getProjectId();
        String srcL = getSourceLang();
        String tgtL = getTargetLang();
        // <project-ID>_<source-locale>_<target-locale> 4711_de-DE_en-GB
        String dirName = pid + "_" + srcL + "_" + tgtL;

        COTIProject cp = new COTIProject();
        cp.setDirName(dirName);
        cp.setCotiProjectId(pid);
        cp.setCotiProjectName(getProjectName());
        cp.setGlobalsightJobId(-1);

        GlobalSightLocale sgsl = null;
        GlobalSightLocale tgsl = null;

        LocaleManager lm = COTIUtilEnvoy.getLocaleManager();
        Locale sl = COTIUtilEnvoy.makeLocaleFromString(srcL);
        Locale tl = COTIUtilEnvoy.makeLocaleFromString(tgtL);
        sgsl = lm.getLocaleByString(sl.toString());
        tgsl = lm.getLocaleByString(tl.toString());

        if (sgsl == null || tgsl == null)
        {
            throw new Exception("Cannot find locales for " + srcL + " and "
                    + tgtL);
        }

        cp.setSourceLang("" + sgsl.getId());
        cp.setTargetLang("" + tgsl.getId());

        return cp;
    }

    /**
     * Create COTI document objects without project id.
     * 
     * @return
     */
    public List<COTIDocument> createDocuments()
    {
        List<COTIDocument> result = new ArrayList<COTIDocument>();

        Element projectE = getFirstProjectElement();
        Element files = getFirstElement(projectE, "files");
        NodeList nodelist = files.getChildNodes();

        if (nodelist != null && nodelist.getLength() > 0)
        {
            for (int i = 0; i < nodelist.getLength(); i++)
            {
                Node n = nodelist.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element file = (Element) n;

                    String localName = file.getLocalName();

                    COTIDocument cd = new COTIDocument();
                    boolean isTranslation = true;
                    if (localName.equals("translation-file"))
                    {
                        isTranslation = true;
                    }
                    else if (localName.equals("reference-file"))
                    {
                        isTranslation = false;
                    }

                    String fileRef = file.getAttribute("file-ref");
                    String encoding = file.getAttribute("encoding");
                    String mimeType = file.getAttribute("mime-type");
                    String fileType = isTranslation ? "" : file
                            .getAttribute("type");
                    String creationDate = isTranslation ? file
                            .getAttribute("creation-date") : "";
                    String description = isTranslation ? "" : file
                            .getAttribute("description");
                    boolean isExternal = isTranslation ? false : "yes"
                            .equals(file.getAttribute("external"));

                    cd.setCreationDate(creationDate);
                    cd.setDescription(description);
                    cd.setEncoding(encoding);
                    cd.setFileRef(fileRef);
                    cd.setFileType(fileType);
                    cd.setIsTranslation(isTranslation);
                    cd.setMimeType(mimeType);
                    cd.setIsExternal(isExternal);

                    result.add(cd);
                }
            }
        }

        return result;
    }

    public String getCotiCreationDate()
    {
        return root.getAttribute("creation-date");
    }

    public String getCotiVersion()
    {
        return root.getAttribute("version");
    }

    public String getCotiCreator()
    {
        return root.getAttribute("creator");
    }

    public String getCotiLevel()
    {
        return root.getAttribute("level");
    }

    public String getProjectName()
    {
        Element projectE = getFirstProjectElement();
        return projectE.getAttribute("name");
    }

    public String getProjectId()
    {
        Element projectE = getFirstProjectElement();
        return projectE.getAttribute("project-id");
    }

    public String getSourceLang()
    {
        Element projectE = getFirstProjectElement();
        Element translation = getFirstElement(projectE, "translation");
        return translation.getAttribute("source-language");
    }

    public String getTargetLang()
    {
        Element projectE = getFirstProjectElement();
        Element translation = getFirstElement(projectE, "translation");
        return translation.getAttribute("target-language");
    }

    public String getSubject()
    {
        Element projectE = getFirstProjectElement();
        Element subject = getFirstElement(projectE, "subject");
        return subject.getTextContent();
    }
}
