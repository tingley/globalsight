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

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports segmentation rules.
 */
public class SegmentationRuleExportHelper implements ConfigConstants
{
    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Segmentation Rules");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = SEGMENT_RULE_FILE_NAME + userName + "_" + sdf.format(new Date()) + ".xml";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets segmentation rule info.
     */
    public static File propertiesInputSR(File srxPropertyFile, Element segRoot, Document segDoc,
            String id)
    {
        FileOutputStream outStream = null;
        Element segRuleNode = new Element("SegmentationRule");
        try
        {
            SegmentationRuleFileImpl segRule = (SegmentationRuleFileImpl) ServerProxy
                    .getSegmentationRuleFilePersistenceManager().readSegmentationRuleFile(
                            Long.parseLong(id));
            segRuleNode.addContent(new Element("ID").setText(String.valueOf(segRule.getId())));
            segRuleNode.addContent(new Element("NAME").setText(segRule.getName()));
            segRuleNode.addContent(new Element("COMPANY_ID").setText(String.valueOf(segRule
                    .getCompanyId())));
            segRuleNode
                    .addContent(new Element("SR_TYPE").setText(String.valueOf(segRule.getType())));
            segRuleNode.addContent(new Element("DESCRIPTION").setText(segRule.getDescription()));
            segRuleNode.addContent(new Element("IS_ACTIVE").setText(String.valueOf(segRule
                    .getIsActive())));
            segRuleNode.addContent(new Element("IS_DEFAULT").setText(String.valueOf(segRule
                    .getIsActive())));
            segRuleNode.addContent(new Element("RULE_TEXT").setText(segRule.getRuleText()));

            segRoot.addContent(segRuleNode);
            XMLOutputter XMLOut = new XMLOutputter();
            outStream = new FileOutputStream(srxPropertyFile);
            XMLOut.output(segDoc, outStream);
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
        return srxPropertyFile;
    }
}
