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
package com.globalsight.connector.eloqua;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.connector.eloqua.form.CreateEloquaForm;
import com.globalsight.connector.eloqua.models.Email;
import com.globalsight.connector.eloqua.models.LandingPage;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.SaveCommentThread;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

public class CreateJobThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(CreateJobThread.class);
    private User user;
    private String currentCompanyId;
    private EloquaConnector conn;
    private File attachment;
    private CreateEloquaForm eForm;
    private String[] targetLocales;
    private String attachmentName;
    private String attribute;
    private String uuid;
    
    public CreateJobThread(User user, String currentCompanyId,
            EloquaConnector conn, File attachment, CreateEloquaForm eForm,
            String[] targetLocales, String attachmentName, String attribute, String uuid)
    {
        super();
        this.user = user;
        this.currentCompanyId = currentCompanyId;
        this.conn = conn;
        this.attachment = attachment;
        this.eForm = eForm;
        this.targetLocales = targetLocales;
        this.attachmentName = attachmentName;
        this.attribute = attribute;
        this.uuid = uuid;
    }

    private void createJob() throws Exception
    {
        try
        {
            String jobName = eForm.getJobName();
            String comment = eForm.getComment();
            String priority = eForm.getPriority();
            
            String randomStr = String.valueOf((new Random()).nextInt(999999999));
            while (randomStr.length() < 9)
            {
                randomStr = "0" + randomStr;
            }

            jobName = jobName + "_" + randomStr;

            // The fileMapFileProfile should be like e11-1001, e12-1002
            String fileMapFileProfile = eForm.getFileMapFileProfile();
            List<String> files = new ArrayList<String>();
            List<String> fps = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            String[] ffs = fileMapFileProfile.split(",");
            for (String ff : ffs)
            {
                String[] f = ff.split("-");
                files.add(f[0]);
                fps.add(f[1]);
                fileProfileList.add(HibernateUtil.get(FileProfileImpl.class,
                        Long.parseLong(f[1])));
            }

            long l10Id = fileProfileList.get(0).getL10nProfileId();
            BasicL10nProfile l10Profile = HibernateUtil.get(
                    BasicL10nProfile.class, l10Id);
            // init files and file profiles infomation
            List<String> descList = new ArrayList<String>();

            // init target locale infomation
            String locs = this.initTargetLocale(targetLocales);
			Job job = JobCreationMonitor.initializeJob(jobName, uuid,
					user.getUserId(), l10Id, priority, Job.IN_QUEUE,
					Job.JOB_TYPE_ELOQUA);
            String sourceLocaleName = l10Profile.getSourceLocale()
                    .getLocaleCode();
            initDescAndFileProfile(descList, job, files, sourceLocaleName);
            Map<String, long[]> filesToFpId = FileProfileUtil.excuteScriptOfFileProfile(
                    descList, fileProfileList, job);
            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());
            List<JobAttribute> jobAttribtues = getJobAttributes(attribute,
                    l10Profile);
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
                CxeProxy.setTargetLocales(key, locs);
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
                    || !StringUtils.isEmpty(attachmentName))
            {
                String dir = convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath())
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator + "tmp" + File.separator + uuid;
                File src = new File(dir + File.separator + attachmentName);
                if (attachment != null)
                {
                    FileUtil.copyFile(attachment, src);
                    attachment.delete();
                }

                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                        attachmentName, user.getUserId(), dir);
                sct.start();
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

    /**
     * Replace "\" and "/" to file separator
     * 
     * @param path
     * @return
     */
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

    /**
     * Move tmp files to proper directory and save the paths and file profiles.
     * 
     * @param descList
     * @param fpIdList
     * @param filePaths
     * @param l10nAndfileProfiles
     * @param blp
     * @param tmpFolderName
     * @param jobName
     * @throws FileNotFoundException
     * @throws Exception
     */

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

    /**
     * According to the array input, return a string of locales. Locales are
     * seperated with commas.
     * 
     * @param targetLocales
     * @return
     * @throws LocaleManagerException
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws GeneralException
     */
    private String initTargetLocale(String[] targetLocales)
            throws LocaleManagerException, NumberFormatException,
            RemoteException, GeneralException
    {
        StringBuffer targetLocaleString = new StringBuffer();
        for (int i = 0; i < targetLocales.length; i++)
        {
            GlobalSightLocale gsl = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(targetLocales[i]));
            String loaleName = gsl.toString();
            if (targetLocaleString.length() != 0)
            {
                targetLocaleString.append(",");
            }
            targetLocaleString.append(loaleName);
        }
        return targetLocaleString.toString();
    }
    
    private String getFileName(String name)
    {
    	if (name.length() > 100)
     	   name = name.substring(0, 100);
    	
     	name = name.replace("\\", "");
     	name = name.replace("/", "");
     	name = name.replace(":", "");
     	name = name.replace("*", "");
     	name = name.replace("?", "");
     	name = name.replace("\"", "");
     	name = name.replace("<", "");
     	name = name.replace(">", "");
     	name = name.replace("|", "");
     	
     	return name;
    }

    private void initDescAndFileProfile(List<String> descList, Job job,
            List<String> files, String sorceLocale)
            throws FileNotFoundException, Exception
    {
        EloquaHelper eh = new EloquaHelper(conn);
        for (String f : files)
        {
            if (f.startsWith("e"))
            {
                f = f.substring(1);
                Email e = eh.getEmail(f);
                e.setConnect(conn);
                
                if (f.equals(e.getId()))
                {
                	// the name may be like ~!@#$%^&*()_+|}{":?"<>,./;'[]\\` 
                	String name = "(" + e.getId() + ")" +  e.getDisplayName();
                	name = getFileName(name);
                	
                    String file = sorceLocale + File.separator + job.getId()
                            + File.separator + name + ".email.html";
                    String file2 = sorceLocale + File.separator + job.getId()
                            + File.separator + name + ".obj";
                    File f1 = new File(AmbFileStoragePathUtils.getCxeDocDir()
                            + File.separator + file);
                    
                    File f2 = new File(AmbFileStoragePathUtils.getCxeDocDir()
                            + File.separator + file2);
                    
                    e.saveToFile(f1);
                    e.saveJsonToFile(f2);
                    descList.add(file);
                }
            }
            else if (f.startsWith("p"))
            {
                f = f.substring(1);
                LandingPage p = eh.getLandingPage(f);
                p.setConnect(conn);
                
                if (f.equals(p.getId()))
                {
                	String name = "(" + p.getId() + ")" +  p.getDisplayName();
                	name = getFileName(name);
                	
                    String file = sorceLocale + File.separator + job.getId()
                            + File.separator + name + ".landingPage.html";
                    String file2 = sorceLocale + File.separator + job.getId()
                            + File.separator + name + ".obj";
                    File f1 = new File(AmbFileStoragePathUtils.getCxeDocDir()
                            + File.separator + file);
                    File f2 = new File(AmbFileStoragePathUtils.getCxeDocDir()
                            + File.separator + file2);
                    
                    p.saveToFile(f1);
                    p.saveJsonToFile(f2);
                    descList.add(file);
                }
            }
            else 
            {
                logger.error("Don't know how to handle the eloqua file with id " + f);
            }
        }
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
