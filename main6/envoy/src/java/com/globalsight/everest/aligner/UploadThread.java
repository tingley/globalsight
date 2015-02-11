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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.aligner.AlignmentProject;
import com.globalsight.ling.aligner.io.AlignmentPackageReader;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.resourcebundle.LocaleWrapper;


/**
 * Run alignment package upload process in a thread.
 */
public class UploadThread extends MultiCompanySupportedThread
{
    static private final Logger c_logger =
        Logger.getLogger(
            UploadThread.class);

    private AlignerPackageUploadOptions m_alignerPackageUploadOptions;
    private String m_packageFile;
    private User m_user;


    UploadThread(AlignerPackageUploadOptions p_alignerPackageUploadOptions,
        String p_packageFile, User p_user)
    {
        super();
        m_alignerPackageUploadOptions = p_alignerPackageUploadOptions;
        m_packageFile = p_packageFile;
        m_user = p_user;
    }


    public void run()
    {
        //Set company id for current thread.
        //CompanyThreadLocal.getInstance().setValue(m_user.getCompanyName());
        super.run();
        
        // get project name from file name
        String projectName = getProjectName(
            m_alignerPackageUploadOptions.getFileName());

        try
        {
            c_logger.info(
                "Start uploading alignment package: " + projectName);

            // get TM id
            Tm tm = getTm(m_alignerPackageUploadOptions.getTmName());

            c_logger.debug("Saving alignment to TM: " + tm.getId());

            // get sync mode
            int tmSaveMode = getTmSaveMode(
                m_alignerPackageUploadOptions.getSyncMode());

            c_logger.debug("With save mode: " + tmSaveMode);

            AlignmentProject project =
                new AlignmentProject(projectName, tm, tmSaveMode);
            project.setUploadUser(m_user);

            AlignmentPackageReader packageReader = new AlignmentPackageReader(
                    m_packageFile, project.getProjectTmpDirectory());

            c_logger.debug("Start reading alignment package: " + projectName);
            packageReader.readAlignmentPackage();
            c_logger.debug(
                "Finished reading alignment package: " + projectName);

            c_logger.debug("Start saving alignment package: " + projectName);
            project.saveToTm();
            c_logger.debug(
                "Finished saving alignment package: " + projectName);

            // send email notification
            String[] args = {projectName};

            EmailNotification.sendNotification(
                m_user, EmailNotification.UPLOAD_COMPLETE_SUBJECT,
                EmailNotification.UPLOAD_COMPLETE_MESSAGE, args);

            c_logger.info(
                "Finished uploading alignment package: " + projectName);
        }
        catch (Exception ex)
        {
            c_logger.error("Alignment upload error:", ex);

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

            String[] args = {projectName, errorMessage};

            EmailNotification.sendNotification(
                m_user, EmailNotification.UPLOAD_FAILED_SUBJECT,
                EmailNotification.UPLOAD_FAILED_MESSAGE, args);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }


    private String getProjectName(String p_filename)
    {
        File file = new File(p_filename);
        String projectName = file.getName();
        if (projectName.length() == 0)
        {
            projectName = "project";
        }

        int idx = projectName.lastIndexOf('.');
        if (idx != -1)
        {
            projectName = projectName.substring(0, idx);
        }

        return projectName;
    }


    private Tm getTm(String p_tmName)
        throws Exception
    {
        ProjectHandler projectHandler = ServerProxy.getProjectHandler();
        ProjectTM tm = projectHandler.getProjectTMByName(p_tmName, false);

        return tm;
    }


    private int getTmSaveMode(String p_synchModeName)
    {
        int saveMode = TmCoreManager.SYNC_MERGE;

        if (p_synchModeName.equals(AlignerPackageUploadOptions.SYNC_OVERWRITE))
        {
            saveMode = TmCoreManager.SYNC_OVERWRITE;
        }
        else if (p_synchModeName.equals(AlignerPackageUploadOptions.SYNC_MERGE))
        {
            saveMode = TmCoreManager.SYNC_MERGE;
        }
        else if (p_synchModeName.equals(AlignerPackageUploadOptions.SYNC_DISCARD))
        {
            saveMode = TmCoreManager.SYNC_DISCARD;
        }

        return saveMode;
    }
}
