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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.globalsight.dispatcher.controller.TranslateXLFController;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.util.FileUtil;
import com.globalsight.dispatcher.util.TranslateUtil;
import com.globalsight.machineTranslation.MachineTranslator;

public class JobTask implements Callable<JobBO>
{
    private static final Logger logger = Logger.getLogger(JobTask.class);
    JobBO job;

    public JobTask(JobBO p_job)
    {
        job = p_job;
    }

    public JobBO call()
    {
        GlobalSightLocale srcLocale = CommonDAO.getGlobalSightLocaleByShortName(job.getSourceLanguage());
        GlobalSightLocale trgLocale = CommonDAO.getGlobalSightLocaleByShortName(job.getTargetLanguage());
        MTPLanguage mtpLanguge = DispatcherDAOFactory.getMTPLanguagesDAO().getMTPLanguage(srcLocale, trgLocale, job.getAccountId());
        MachineTranslationProfile mtProfile = mtpLanguge.getMtProfile();
        job.setMtpLanguageID(mtpLanguge.getId());
        job.setStatus(AppConstants.STATUS_RUNNING);
        TranslateXLFController.updateJobMap(job);
        
        try
        {
            MachineTranslator translator = TranslateUtil.getMachineTranslator(mtProfile);
            String[] trgSegments = translator.translateBatchSegments(srcLocale.getLocale(), trgLocale.getLocale(), job.getSourceSegments(), true, false);
            trgSegments = operateTargetSegments(job, trgSegments);
            createTargetFile(job, trgSegments);
            job.setStatus(AppConstants.STATUS_COMPLETED);
            TranslateXLFController.updateJobMap(job);
        }
        catch (JobTaskException jobEx)
        {
            logger.error(jobEx.getInfo(), jobEx);
            job.setStatus(AppConstants.STATUS_FAILED);
            job.setSourceSegments(null);
            TranslateXLFController.updateJobMap(job);
        }
        catch (Exception mtEx)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Do Machine Translation Failed, By Job\n")
               .append("JobID:").append(job.getJobID()).append("\n")
               .append("SourceLanguage:").append(job.getSourceLanguage()).append("\n")
               .append("TargetLanguage:").append(job.getTargetLanguage()).append("\n")
               .append("SrcFile:").append(job.getSrcFile()).append("\n")
               .append("Language Name:").append(mtpLanguge.getName());
            logger.error(msg.toString(), mtEx);
            job.setStatus(AppConstants.STATUS_FAILED);
            job.setSourceSegments(null);
            TranslateXLFController.updateJobMap(job);
        }
        
        return job;
    }

    /**
     * Check MT Result and operate every target.
     * 
     * @param p_job
     *            DispatcherMW Job
     * @param p_targetSegments
     *            Machine Translation Result
     * @throws JobTaskException
     *            DispatcherMW Job Exception
     */
    private String[] operateTargetSegments(JobBO p_job, String[] p_targetSegments) throws JobTaskException
    {
        if (isEmpty(p_targetSegments))
            throw new JobTaskException("The MT Result is empty.", p_job);

        if (p_targetSegments.length != p_job.getSourceSegments().length)
            throw new JobTaskException("The MT Result is incorrect.", p_job);
        
        return p_targetSegments;
    }
    
    private boolean isEmpty(String[] p_targetSegments)
    {
        if (p_targetSegments == null || p_targetSegments.length == 0)
            return true;

        for (String target : p_targetSegments)
        {
            if (target != null && target.trim().length() > 0)
                return false;
        }
        return true;
    }
    
    private void createTargetFile(JobBO p_job, String[] p_targetSegments) throws IOException
    {
        OutputStream writer = null;
        File fileStorage = CommonDAO.getFileStorage();
        File srcFile = p_job.getSrcFile();
        Account account = DispatcherDAOFactory.getAccountDAO().getAccount(p_job.getAccountId());
        File trgDir = CommonDAO.getFolder(fileStorage, account.getAccountName() + File.separator + p_job.getJobID()
                + File.separator + AppConstants.XLF_TARGET_FOLDER);
        File trgFile = new File(trgDir, srcFile.getName());
        FileUtils.copyFile(srcFile, trgFile);
        String encoding = FileUtil.getEncodingOfXml(trgFile);

        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(p_job.getSrcFile());
            Element root = doc.getRootElement(); // Get root element
            Namespace namespace = root.getNamespace();
            Element fileElem = root.getChild("file", namespace);
            XPathFactory xFactory = XPathFactory.instance();
            XPathExpression<Element> expr = xFactory.compile("//trans-unit", Filters.element(), null, namespace);
            List<Element> tuList = expr.evaluate(fileElem.getChild("body", namespace));
            for (int tuIndex = 0, trgIndex = 0; tuIndex < tuList.size() && trgIndex < p_targetSegments.length; tuIndex++, trgIndex++)
            {
                if (p_targetSegments[trgIndex] == null)
                {
                    continue;
                }

                Element elem = (Element) tuList.get(tuIndex);
                Element srcElem = elem.getChild("source", namespace);
                Element trgElem = elem.getChild("target", namespace);
                if (srcElem == null || srcElem.getContentSize() == 0 )
                {
                    trgIndex--;
                    continue;
                }
                
                if (trgElem != null)
                {
                    setTargetSegment(trgElem, p_targetSegments[trgIndex], encoding);
                }
                else
                {
                    trgElem = new Element("target", namespace);
                    setTargetSegment(trgElem, p_targetSegments[trgIndex], encoding);
                    elem.addContent(trgElem);
                }

            }
            
            XMLOutputter xmlOutput = new XMLOutputter();
            Format format = Format.getRawFormat();
            format.setEncoding(encoding);
            writer = new FileOutputStream(trgFile);
            xmlOutput.setFormat(format);
            writeBOM(writer, format.getEncoding());
            xmlOutput.output(doc, writer);
            p_job.setTrgFile(trgFile);
            logger.info("Create Target File: " + trgFile);
        }
        catch (JDOMException e1)
        {
            logger.error("CreateTargetFile Error: ", e1);
        }
        catch (IOException e1)
        {
            logger.error("CreateTargetFile Error: ", e1);
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }
    
    // Write BOM Info for UTF-16BE/LE
    private void writeBOM(OutputStream p_writer, String p_encoding) throws IOException
    {
        if("UTF-16BE".equalsIgnoreCase(p_encoding))
        {
            p_writer.write(254);
            p_writer.write(255);            
        }
        else if("UTF-16LE".equalsIgnoreCase(p_encoding))
        {
            p_writer.write(255);
            p_writer.write(254);
        }
    }
    
    private void setTargetSegment(Element p_trgElement, String p_target, String p_encoding) throws UnsupportedEncodingException
    {
        if(p_target == null || p_target.trim().length() == 0)
            return;
        
        String target = new String(p_target.getBytes("UTF-8"), p_encoding);
        try
        {
            StringReader stringReader = new StringReader("<target>" + p_target + "</target>");
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(stringReader);
            Element elem = doc.getRootElement().clone().detach();
            setNamespace(elem, p_trgElement.getNamespace());
            //Delete Original Target Segment.
            p_trgElement.removeContent();
            for (int i = 0; i < elem.getContentSize(); i++)
            {
                p_trgElement.addContent(elem.getContent(i).clone().detach());
            }
        }
        catch (Exception e)
        {
            p_trgElement.setText(target);
        }
    }
    
    private void setNamespace(Element p_element, Namespace p_namespace)
    {
        p_element.setNamespace(p_namespace);
        for(Element child : p_element.getChildren())
        {
            setNamespace(child, p_namespace);
        }
    }
}
