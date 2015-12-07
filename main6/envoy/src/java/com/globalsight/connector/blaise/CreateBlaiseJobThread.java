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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
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
	static private final Logger logger = Logger
			.getLogger(CreateBlaiseJobThread.class);

    private User user;
    private String currentCompanyId;
    private BlaiseConnector connector;
    private CreateBlaiseJobForm blaiseForm;
    private File attachFile;
    private String attachFileName;
    private String uuid;
    private TranslationInboxEntryVo curEntry = null;
    private FileProfile curFileProfile = null;

	public CreateBlaiseJobThread(User user, String currentCompanyId,
			BlaiseConnector conn, CreateBlaiseJobForm blaiseForm,
			TranslationInboxEntryVo curEntry, FileProfile curFileProfile,
			File attachFile, String attachFileName, String uuid)
    {
        super();

        this.user = user;
        this.currentCompanyId = currentCompanyId;
        this.connector = conn;
        this.blaiseForm = blaiseForm;
        this.attachFile = attachFile;
        this.attachFileName = attachFileName;
        this.uuid = uuid;
        this.curEntry = curEntry;
        this.curFileProfile = curFileProfile;
    }

    private void createJob() throws Exception
    {
        try
        {
            String jobName = BlaiseHelper.getEntryJobName(curEntry);
            jobName = addJobNameSuffix(jobName);
            String comment = blaiseForm.getComment();
            String priority = blaiseForm.getPriority();

            long l10Id = curFileProfile.getL10nProfileId();
            BasicL10nProfile l10Profile = HibernateUtil.get(
                    BasicL10nProfile.class, l10Id);
            String sourceLocaleName = l10Profile.getSourceLocale().getLocaleCode();

            Locale trgLocale = curEntry.getTargetLocale();
            String targetLocale = trgLocale.getLanguage() + "_" + trgLocale.getCountry();

			Job job = JobCreationMonitor.initializeJob(jobName, uuid,
					user.getUserId(), l10Id, priority, Job.IN_QUEUE,
					Job.JOB_TYPE_BLAISE);

            // init files and file profiles infomation
            List<String> descList = new ArrayList<String>();
			retrieveRealFileFromBlaiseServer(descList, job, curEntry,
					sourceLocaleName);

            List<FileProfile> fpList = new ArrayList<FileProfile>();
            fpList.add(curFileProfile);
			Map<String, long[]> filesToFpId = FileProfileUtil
					.excuteScriptOfFileProfile(descList, fpList, job);

            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());
            List<JobAttribute> jobAttribtues = getJobAttributes(
                    blaiseForm.getAttributeString(), l10Profile);
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
                CxeProxy.importFromFileSystem(fileName,
                        String.valueOf(job.getId()), jobName, fileProfileId,
                        pageCount, count, 1, 1, Boolean.TRUE, Boolean.FALSE,
                        CxeProxy.IMPORT_TYPE_L10N, exitValue, priority);
            }

            // save job attributes if there are any
            if (jobAttribtues != null)
            {
                saveAttributes(jobAttribtues, currentCompanyId, job);
            }

            // save job comment
            if (!StringUtils.isEmpty(comment)
                    || !StringUtils.isEmpty(attachFileName))
            {
                String dir = convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath(currentCompanyId))
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator + "tmp" + File.separator + uuid;
                File src = new File(dir + File.separator + attachFileName);
                if (attachFile != null)
                {
                    FileUtil.copyFile(attachFile, src);
                }

                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                		attachFileName, user.getUserId(), dir);
                sct.start();
            }

            // Record this
            BlaiseConnectorJob bcj = new BlaiseConnectorJob();
            bcj.setBlaiseConnectorId(this.connector.getId());
            bcj.setBlaiseEntryId(this.curEntry.getId());
            bcj.setJobId(job.getId());
            HibernateUtil.saveOrUpdate(bcj);
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

    /**
	 * The pathname is like "[source_locale]\[job_id]\blaise entry id
	 * [entryId]\[entry file name]". A sample is "en_US\96\blaise entry id
	 * 100\Blaise inbox entry - Markets - 1 - 32 - no_NO.xlf".
	 */
	private void retrieveRealFileFromBlaiseServer(List<String> descList,
			Job job, TranslationInboxEntryVo curEntry, String sourceLocale)
    {
		StringBuffer filePath = new StringBuffer();
		filePath.append(sourceLocale).append(File.separator)
				.append(job.getId()).append(File.separator)
				.append(curEntry.getId()).append(File.separator)
				.append(BlaiseHelper.getEntryFileName(curEntry));
		String externalPageId = filePath.toString();
		descList.add(externalPageId);

		File srcFile = new File(
				AmbFileStoragePathUtils.getCxeDocDir(currentCompanyId)
						+ File.separator + externalPageId);
        if (!srcFile.exists())
        {
        	srcFile.getParentFile().mkdirs();
        }
		BlaiseHelper helper = new BlaiseHelper(connector);
		helper.downloadXliff(curEntry, srcFile);
    }

    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/",
                    File.separator);
        }
        else
        {
            return "";
        }
    }

    /**
     * Save job attributes of the job
     * 
     * @param attributeString
     * @param job
     * @param currentCompanyId
     * @param l10Profile
     * @throws ParseException
     */
    private void saveAttributes(List<JobAttribute> jobAttributeList,
            String currentCompanyId, Job job)
    {
        AddJobAttributeThread thread = new AddJobAttributeThread(
                ((JobImpl) job).getUuid(), currentCompanyId);
        thread.setJobAttributes(jobAttributeList);
        thread.createJobAttributes();
    }

    private List<JobAttribute> getJobAttributes(String attributeString,
            BasicL10nProfile l10Profile)
    {
        List<JobAttribute> jobAttributeList = new ArrayList<JobAttribute>();

        if (l10Profile.getProject().getAttributeSet() == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(attributeString))
        {
            String[] attributes = attributeString.split(";.;");
            for (String ele : attributes)
            {
                try
                {
                    String attributeId = ele.substring(ele.indexOf(",.,") + 3,
                            ele.lastIndexOf(",.,"));
                    String attributeValue = ele.substring(ele
                            .lastIndexOf(",.,") + 3);

                    Attribute attribute = HibernateUtil.get(Attribute.class,
                            Long.parseLong(attributeId));
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(attribute.getCloneAttribute());
                    if (attribute != null
                            && StringUtils.isNotEmpty(attributeValue))
                    {
                        Condition condition = attribute.getCondition();
                        if (condition instanceof TextCondition)
                        {
                            jobAttribute.setStringValue(attributeValue);
                        }
                        else if (condition instanceof IntCondition)
                        {
                            jobAttribute.setIntegerValue(Integer
                                    .parseInt(attributeValue));
                        }
                        else if (condition instanceof FloatCondition)
                        {
                            jobAttribute.setFloatValue(Float
                                    .parseFloat(attributeValue));
                        }
                        else if (condition instanceof DateCondition)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    DateCondition.FORMAT);
                            jobAttribute
                                    .setDateValue(sdf.parse(attributeValue));
                        }
                        else if (condition instanceof ListCondition)
                        {
                            String[] options = attributeValue.split("#@#");
                            List<String> optionValues = Arrays.asList(options);
                            jobAttribute.setValue(optionValues, false);
                        }
                    }
                    jobAttributeList.add(jobAttribute);
                }
                catch (Exception e)
                {
                    logger.error("Failed to get job attributes", e);
                }
            }
        }
        else
        {
            List<Attribute> attsList = l10Profile.getProject()
                    .getAttributeSet().getAttributeAsList();
            for (Attribute att : attsList)
            {
                JobAttribute jobAttribute = new JobAttribute();
                jobAttribute.setAttribute(att.getCloneAttribute());
                jobAttributeList.add(jobAttribute);
            }
        }

        return jobAttributeList;
    }

    private String addJobNameSuffix(String jobName)
    {
        String randomStr = String.valueOf((new Random()).nextInt(999999999));
		while (randomStr.length() < 9) {
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
    }
}

