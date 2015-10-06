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
package com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class CreatePdfThread extends MultiCompanySupportedThread
{
    private Job job = null;
    private Logger logger = null;

    private boolean enableIndd = false;
    private boolean enableOffice = false;
    private boolean enableXML = false;

    public CreatePdfThread(Job job, Logger logger)
    {
        this.job = job;
        this.logger = logger;

        try
        {
            String companyId = "" + this.job.getCompanyId();

            enableIndd = PreviewPDFHelper.isInDesignEnabled(companyId);
            enableOffice = PreviewPDFHelper.isOfficeEnabled(companyId);
            enableXML = PreviewPDFHelper.isXMLEnabled(companyId);
        }
        catch (Exception ex)
        {
            // ignore
        }
    }

    @Override
    public void run()
    {
        try
        {
            Collection<SourcePage> sourcePages = job.getSourcePages();
            String userid = job.getCreateUserId();

            for (SourcePage sourcePage : sourcePages)
            {
                String pageName = sourcePage.getExternalPageId().toLowerCase();
                
                // no need to create PDF for (Adobe file information)
                if (pageName.startsWith("(adobe file information)"))
                {
                    continue;
                }
                
                if (pageName.endsWith(".indd") || pageName.endsWith(".idml")
                        || pageName.endsWith(".docx")
                        || pageName.endsWith(".pptx")
                        || pageName.endsWith(".xlsx")
                        || pageName.endsWith(".xml"))
                {
                    if (pageName.endsWith(".xml"))
                    {
                        if (!enableXML)
                        {
                            continue;
                        }
                        
                        long fpId = sourcePage.getRequest().getDataSourceId();
                        FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                                .readFileProfile(fpId);
                        
                        if (!FileProfileUtil.isXmlPreviewPDF(fp))
                        {
                            continue;
                        }
                    }

                    if ((pageName.endsWith(".indd")
                            || pageName.endsWith(".idml")) && !enableIndd)
                    {
                        continue;
                    }

                    if ((pageName.endsWith(".docx")
                            || pageName.endsWith(".pptx")
                            || pageName.endsWith(".xlsx")) && !enableOffice)
                    {
                        continue;
                    }

                    PreviewPDFHelper previewHelper = new PreviewPDFHelper();
                    File pdfFile = previewHelper.createPDF(sourcePage.getId(),
                            userid, false);

                    logger.debug("Create source PDF " + pdfFile
                            + " successfully for job : " + job);

                    // create target PDFs
                    Set<TargetPage> targetPages = sourcePage.getTargetPages();
                    for (TargetPage targetPage : targetPages)
                    {
                        PreviewPDFHelper previewHelperT = new PreviewPDFHelper();
                        File pdfFileT = previewHelperT
                                .createPDF(targetPage.getId(), userid, true);

                        logger.debug("Create target PDF " + pdfFileT
                                + " successfully for job : " + job);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Could not create In context preview PDF for job : " + job);
            throw new EnvoyServletException(e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
