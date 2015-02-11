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
package com.globalsight.everest.aligner;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.aligner.AlignmentProject;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.company.MultiCompanySupportedThread;

import java.util.Iterator;
import java.util.Locale;


/**
 * Run a batch alignment process in a thread
 */
public class BatchAlignThread extends MultiCompanySupportedThread
{
    static private final Logger c_logger = Logger
            .getLogger(BatchAlignThread.class);

    private AlignerPackageOptions m_alignerPackageOptions;
    private User m_user;


    BatchAlignThread(
        AlignerPackageOptions p_alignerPackageOptions, User p_user)
    {
    	super();
        m_alignerPackageOptions = p_alignerPackageOptions;
        m_user = p_user;
    }


    public void run()
    {
    	super.run();        
        String packageName = m_alignerPackageOptions.getPackageName();

        try
        {
            c_logger.info("Start creating alignment package: " + packageName);

            // get KnownFormatType
            KnownFormatType knownFormatType =
                getKnownFormatType(m_alignerPackageOptions.getFormatType());

            // get XML rule file
            XmlRuleFile xmlRuleFile =
                getXmlRuleFile(m_alignerPackageOptions.getRules());

            // get source and target locale
            LocaleManager localeMgr = ServerProxy.getLocaleManager();
            GlobalSightLocale sourceLocale = localeMgr.getLocaleByString(
                m_alignerPackageOptions.getSourceLocale());
            GlobalSightLocale targetLocale = localeMgr.getLocaleByString(
                m_alignerPackageOptions.getTargetLocale());

            // create project
            AlignmentProject project = new AlignmentProject(
                packageName,
                sourceLocale, targetLocale, knownFormatType,
                m_alignerPackageOptions.getSourceEncoding(),
                m_alignerPackageOptions.getTargetEncoding(),
                xmlRuleFile);

            c_logger.debug("Start extracting files for alignment package: " +
                packageName);

            // extract source and target files and add AlignmentUnit objects
            Iterator it = m_alignerPackageOptions.getFilePairs().iterator();
            while (it.hasNext())
            {
                AlignerPackageOptions.FilePair filePair =
                    (AlignerPackageOptions.FilePair)it.next();

                project.addAlignmentUnit(
                    filePair.getSource(), filePair.getTarget());
            }

            c_logger.debug("Finished extracting files for alignment package: " +
                packageName);

            c_logger.debug("Start aligning for alignment package: " +
                packageName);

            // do batch alignment
            project.alignAll();

            c_logger.debug("Finished aligning for alignment package: " +
                packageName);

            // send email notification
            String[] args = {packageName};

            EmailNotification.sendNotification(
                m_user, EmailNotification.BATCH_COMPLETE_SUBJECT,
                EmailNotification.BATCH_COMPLETE_MESSAGE, args);

            c_logger.info(
                "Finished creating alignment package: " + packageName);
        }
        catch (Throwable ex)
        {
            c_logger.error("Batch alignment error:", ex);

            // send error email
            Locale userLocale = LocaleWrapper.getLocale(
                m_user.getDefaultUILocale());

            String errorMessage = null;
            if (ex instanceof GeneralException)
            {
                errorMessage = ((GeneralException)ex).getMessage(userLocale);
            }
            else
            {
                errorMessage = ex.getMessage();
            }

            String[] args = {packageName, errorMessage};

            EmailNotification.sendNotification(
                m_user, EmailNotification.BATCH_FAILED_SUBJECT,
                EmailNotification.BATCH_FAILED_MESSAGE, args);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private KnownFormatType getKnownFormatType(String p_formatType)
        throws Exception
    {
        return ServerProxy.getFileProfilePersistenceManager().
            queryKnownFormatType(Integer.parseInt(p_formatType));
    }


    private XmlRuleFile getXmlRuleFile(String p_xmlRuleFileName)
        throws Exception
    {
        // result is an ordered collection of XmlRuleFile objects
        Iterator it = ServerProxy.getXmlRuleFilePersistenceManager().
            getAllXmlRuleFiles().iterator();

        while (it.hasNext())
        {
            XmlRuleFile rule = (XmlRuleFile)it.next();

            if (rule.getName().equals(p_xmlRuleFileName))
            {
                return rule;
            }
        }

        return null;
    }
}
