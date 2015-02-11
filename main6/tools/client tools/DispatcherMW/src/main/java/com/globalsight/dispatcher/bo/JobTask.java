/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.globalsight.dispatcher.bo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.globalsight.dispatcher.controller.TranslateXLFController;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslator;

public class JobTask implements Callable<JobBO>
{
    private static final Logger logger = Logger.getLogger(JobTask.class);
    JobBO job;

    public JobTask(JobBO p_job)
    {
        job = p_job;
    }

    public JobBO call() throws Exception
    {
        GlobalSightLocale srcLocale = CommonDAO.getGlobalSightLocaleByShortName(job.getSourceLanguage());
        GlobalSightLocale trgLocale = CommonDAO.getGlobalSightLocaleByShortName(job.getTargetLanguage());
        MTPLanguage mtpLanguge = DispatcherDAOFactory.getMTPLanguagesDAO().getMTPLanguage(srcLocale, trgLocale, job.getAccountId());
        MachineTranslationProfile mtProfile = mtpLanguge.getMtProfile();

        MachineTranslator translator = AbstractTranslator.initMachineTranslator(mtProfile.getMtEngine());
        translator.setMtParameterMap(mtProfile.getParamHM());
        String[] trgSegments = translator.translateBatchSegments(srcLocale.getLocale(), trgLocale.getLocale(), job.getSourceSegments(), true);
        operateTargetSegments(trgSegments);
        createTargetFile(job, trgSegments);
        job.setStatus(AppConstants.STATUS_COMPLETED);
        TranslateXLFController.setJobMap(job);
        return job;
    }

    private void operateTargetSegments(String[] p_targetSegments)
    {
        for(int i=0; i<p_targetSegments.length; i++)
        {
            String target = p_targetSegments[i];
            // Delete <segment> tag.
            if(target.startsWith("<segment>") && target.endsWith("</segment>"))
            {
                p_targetSegments[i] = target.substring(9, target.length()-10);
            }
        }
    }
    
    private void createTargetFile(JobBO p_job, String[] p_targetSegments) throws IOException
    {
        File fileStorage = CommonDAO.getFileStorage();
        File srcFile = p_job.getSrcFile();
        File trgDir = CommonDAO.getFolder(fileStorage, p_job.getAccountId() + File.separator 
                + p_job.getJobID() + File.separator + AppConstants.XLF_TARGET_FOLDER);
        File trgFile = new File(trgDir, srcFile.getName());
        FileUtils.copyFile(srcFile, trgFile);

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(p_job.getSrcFile());
            Element root = doc.getRootElement(); // Get root element
            Element fileElem = root.getChild("file");
            List<?> list = fileElem.getChild("body").getChildren("trans-unit");
            for (int i = 0; i < list.size(); i++)
            {
                Element e = (Element) list.get(i);
                e.getChild("target").setText(p_targetSegments[i]);
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trgFile),"UTF-8"));
            xmlOutput.output(doc, writer);
            p_job.setTrgFile(trgFile);
            logger.info("Create Target File: " + trgFile);
        }
        catch (JDOMException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }
}
