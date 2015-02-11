/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bussiness.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.FileUtil;
import com.globalsight.smartbox.util.LogUtil;

/**
 * Use case 02 post process
 * 
 * @author leon
 * 
 */
public class Usecase02PostProcess implements PostProcess
{

    @Override
    public boolean process(JobInfo jobInfo, CompanyConfiguration cpConfig)
    {
        // Get target files
        String targetFile = jobInfo.getTargetFiles();
        File originFile = new File(jobInfo.getOriginFile());
        String originFileName = originFile.getName();
        String format = originFileName.substring(
                originFileName.lastIndexOf(".") + 1, originFileName.length());
        format = format.toLowerCase();
        String outputFilePath = cpConfig.getTempBox() + File.separator
                + originFileName;
        File outputFile;

        try
        {
            LogUtil.info("Converting to " + format + "file: " + targetFile);
            outputFile = convertXMLToCSVTXT(format, targetFile, outputFilePath);
        }
        catch (Exception e)
        {
            String message = "Failed to convert to " + format + ": "
                    + originFile.getName();
            LogUtil.fail(message, e);
            return false;
        }

        // set final result file
        jobInfo.setFinalResultFile(outputFilePath);
        jobInfo.setTempFile(outputFilePath);
        return true;
    }

    /**
     * Convert xml to csv or txt file
     * 
     * @param format
     * @param originFile
     * @return
     * @throws Exception
     */
    private File convertXMLToCSVTXT(String format, String targetFile,
            String outputFilePath) throws Exception
    {

        File xmlFile = new File(targetFile);
        File outputFile = new File(outputFilePath);

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(xmlFile);
        Element aElement = document.getRootElement();
        String encoding = aElement.attributeValue("BomInfo");

        FileOutputStream fos = new FileOutputStream(outputFilePath);
        FileUtil.writeBom(fos, encoding);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,
                encoding));

        List<Element> rows = aElement.elements("row");
        for (Element row : rows)
        {
            List<String> rowStr = new ArrayList<String>();
            rowStr.add(row.elementText("sid"));
            rowStr.add(row.elementText("sourceLocaleName"));
            rowStr.add(row.elementText("sourceLocaleCode"));
            rowStr.add(row.elementText("unknown"));
            rowStr.add(row.elementText("translationSource"));

            rowStr.add(row.elementText("targetLocale"));
            rowStr.add(row.elementText("creationDate"));
            List<Element> segments = row.elements("segment");
            for (Element element : segments)
            {
                rowStr.add(element.getText());
            }

            StringBuffer sb = new StringBuffer();
            if ("csv".equals(format))
            {
                for (String str : rowStr)
                {
                    sb.append("\"").append(str).append("\"").append(",");
                }
            }
            else
            {
                for (String str : rowStr)
                {
                    sb.append(str).append("|");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            bw.write(sb.toString());
            bw.newLine();
        }
        bw.close();
        fos.close();

        return outputFile;
    }
}
