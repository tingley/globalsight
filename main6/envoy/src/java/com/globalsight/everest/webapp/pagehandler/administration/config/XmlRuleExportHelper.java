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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports XML rules.
 */
public class XmlRuleExportHelper implements ConfigConstants
{
    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("XML Rules");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = XML_RULE_FILE_NAME + userName + "_" + sdf.format(new Date()) + ".xml";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets XML rule info.
     */
    public static File propertiesInputXR(File xrPropertyFile, Element xmlRoot, Document xmlDoc,
            String id)
    {
        FileOutputStream outStream = null;
        Element xmlRuleNode = new Element("XMLRule");
        try
        {
            XmlRuleFileImpl xmlRule = (XmlRuleFileImpl) ServerProxy
                    .getXmlRuleFilePersistenceManager().readXmlRuleFile(Long.parseLong(id));
            xmlRuleNode.addContent(new Element("ID").setText(String.valueOf(xmlRule.getId())));
            xmlRuleNode.addContent(new Element("NAME").setText(xmlRule.getName()));
            xmlRuleNode.addContent(new Element("COMPANY_ID").setText(String.valueOf(xmlRule
                    .getCompanyId())));
            xmlRuleNode.addContent(new Element("DESCRIPTION").setText(xmlRule.getDescription()));
            xmlRuleNode.addContent(new Element("RULE_TEXT").setText(xmlRule.getRuleText()));

            xmlRoot.addContent(xmlRuleNode);
            XMLOutputter XMLOut = new XMLOutputter();
            outStream = new FileOutputStream(xrPropertyFile);
            XMLOut.output(xmlDoc, outStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (outStream != null)
                    outStream.close();
            }
            catch (IOException e)
            {

            }
        }
        return xrPropertyFile;
    }
}
