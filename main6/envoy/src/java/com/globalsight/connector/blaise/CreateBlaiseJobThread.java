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
package com.globalsight.connector.blaise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.SaveCommentThread;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.RuntimeCache;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

public class CreateBlaiseJobThread  extends Thread
{
    static private final Logger logger = Logger.getLogger(CreateBlaiseJobThread.class);

    private User user;
    private String currentCompanyId;
    private BlaiseConnector connector;
    private CreateBlaiseJobForm blaiseForm;
    private File attachFile;
    private String attachFileName;
    private String uuid;
    List<JobAttribute> jobAttribtues = null;
    private List<TranslationInboxEntryVo> entries = new ArrayList<TranslationInboxEntryVo>();
    private List<FileProfile> fileProfiles = new ArrayList<FileProfile>();

    public CreateBlaiseJobThread(User user, String currentCompanyId, BlaiseConnector conn,
            CreateBlaiseJobForm blaiseForm, List<TranslationInboxEntryVo> entries,
            List<FileProfile> fileProfiles, File attachFile, String attachFileName, String uuid,
            List<JobAttribute> jobAttribtues)
    {
        super();

        this.user = user;
        this.currentCompanyId = currentCompanyId;
        this.connector = conn;
        this.blaiseForm = blaiseForm;
        this.attachFile = attachFile;
        this.attachFileName = attachFileName;
        this.uuid = uuid;
        this.jobAttribtues = jobAttribtues;
        this.entries = entries;
        this.fileProfiles = fileProfiles;
    }

    private void createJob() throws Exception
    {
        try
        {
            String jobName = decideJobName();
            String comment = blaiseForm.getComment();
            String priority = blaiseForm.getPriority();

            long l10Id = fileProfiles.get(0).getL10nProfileId();
            BasicL10nProfile l10Profile = HibernateUtil.get(BasicL10nProfile.class, l10Id);
            String sourceLocaleName = l10Profile.getSourceLocale().getLocaleCode();

            Locale trgLocale = entries.get(0).getTargetLocale();
            String targetLocale = trgLocale.getLanguage() + "_" + trgLocale.getCountry();
            targetLocale = BlaiseHelper.fixLocale(targetLocale);

            Job job = JobCreationMonitor.initializeJob(jobName, uuid, user.getUserId(), l10Id,
                    priority, Job.IN_QUEUE, Job.JOB_TYPE_BLAISE);

            // Initialize files and file profiles information
            List<String> descList = new ArrayList<String>();
            retrieveRealFileFromBlaiseServer(descList, job, entries, sourceLocaleName);

            Map<String, long[]> filesToFpId = FileProfileUtil.excuteScriptOfFileProfile(descList,
                    fileProfiles, job);

            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());

            // cache job attributes if there are any
            if (jobAttribtues != null && jobAttribtues.size() != 0)
            {
                RuntimeCache.addJobAtttibutesCache(uuid, jobAttribtues);
            }

            int count = 0;
            for (Iterator<String> i = fileNames.iterator(); i.hasNext();)
            {
                String fileName = i.next();
                long[] tmp = filesToFpId.get(fileName);
                String fileProfileId = String.valueOf(tmp[0]);
                int exitValue = (int) tmp[1];

                String key = jobName + fileName + ++count;
                CxeProxy.setTargetLocales(key, targetLocale);
                CxeProxy.importFromFileSystem(fileName, String.valueOf(job.getId()), jobName,
                        fileProfileId, pageCount, count, 1, 1, Boolean.TRUE, Boolean.FALSE,
                        CxeProxy.IMPORT_TYPE_L10N, exitValue, priority);
            }

            // save job attributes if there are any
            if (jobAttribtues != null)
            {
                saveAttributes(jobAttribtues, currentCompanyId, job);
            }

            // save job comment
            if (!StringUtils.isEmpty(comment) || !StringUtils.isEmpty(attachFileName))
            {
                StringBuilder dir = new StringBuilder();
                dir.append(convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath(currentCompanyId)));
                dir.append(File.separator).append("GlobalSight");
                dir.append(File.separator).append("CommentReference");
                dir.append(File.separator).append("tmp");
                dir.append(File.separator).append(uuid);
                File src = new File(dir.toString() + File.separator + attachFileName);
                if (attachFile != null)
                {
                    FileUtil.copyFile(attachFile, src);
                }

                SaveCommentThread sct = new SaveCommentThread(jobName, comment, attachFileName,
                        user.getUserId(), dir.toString());
                sct.start();
            }

            // Record this
            for (TranslationInboxEntryVo entry : entries)
            {
                BlaiseConnectorJob bcj = new BlaiseConnectorJob();
                bcj.setBlaiseConnectorId(this.connector.getId());
                bcj.setBlaiseEntryId(entry.getId());
                bcj.setJobId(job.getId());
                HibernateUtil.saveOrUpdate(bcj);
            }
        }
        catch (FileNotFoundException ex)
        {
            logger.error("Cannot find the tmp uploaded files.", ex);
        }
        catch (Exception e)
        {
            logger.error("Create job failed.", e);
        }
    }

    private String decideJobName()
    {
        String jobName = null;
        String falconTargetValue = BlaiseHelper.findFalconTargetValue(jobAttribtues);
        if (falconTargetValue != null)
        {
            jobName = BlaiseHelper.getHarlyJobName(entries.get(0), falconTargetValue);
        }
        else if (entries.size() == 1)
        {
            jobName = BlaiseHelper.getEntryJobName(entries.get(0));
        }
        else
        {
            jobName = BlaiseHelper.getEntriesJobName(entries);
        }

        return addJobNameSuffix(jobName);        
    }

    /**
	 * The pathname is like "[source_locale]\[job_id]\blaise entry id
	 * [entryId]\[entry file name]". A sample is "en_US\96\blaise entry id
	 * 100\Blaise inbox entry - Markets - 1 - 32 - no_NO.xlf".
	 */
    private void retrieveRealFileFromBlaiseServer(List<String> descList, Job job,
            List<TranslationInboxEntryVo> entries, String sourceLocale)
    {
        for (TranslationInboxEntryVo curEntry : entries)
        {
            StringBuffer filePath = new StringBuffer();
            filePath.append(sourceLocale);
            filePath.append(File.separator);
            filePath.append(job.getId());
            filePath.append(File.separator);
            filePath.append(curEntry.getId());
            filePath.append(File.separator);
            filePath.append(BlaiseHelper.getEntryFileName(curEntry));
            String externalPageId = filePath.toString();
            descList.add(externalPageId);

            File srcFile = new File(AmbFileStoragePathUtils.getCxeDocDir(currentCompanyId)
                    + File.separator + externalPageId);
            if (!srcFile.exists())
            {
                srcFile.getParentFile().mkdirs();
            }
            BlaiseHelper helper = new BlaiseHelper(connector);
            logger.info("Downloading Blaise file " + srcFile.getAbsolutePath());
            helper.downloadXliff(curEntry, srcFile);
        }
    }

    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/", File.separator);
        }
        else
        {
            return "";
        }
    }

    /**
     * Save job attributes of the job
     * 
     * @param jobAttributeList
     * @param currentCompanyId
     * @param job
     */
    private void saveAttributes(List<JobAttribute> jobAttributeList, String currentCompanyId,
            Job job)
    {
        AddJobAttributeThread thread = new AddJobAttributeThread(((JobImpl) job).getUuid(),
                currentCompanyId);
        thread.setJobAttributes(jobAttributeList);
        thread.createJobAttributes();
    }

    private String addJobNameSuffix(String jobName)
    {
        String randomStr = String.valueOf((new Random()).nextInt(999999999));
        while (randomStr.length() < 9)
        {
            randomStr = "0" + randomStr;
        }
        return jobName + "_" + randomStr;
    }

    @Override
    public void run()
    {
        try
        {
            createJob();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}

