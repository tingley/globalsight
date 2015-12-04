package com.globalsight.connector.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.connector.git.form.CreateGitConnectorJobForm;
import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorJob;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.SaveCommentThread;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.RuntimeCache;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

public class CreateGitConnectorJobThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(CreateGitConnectorJobThread.class);

    private User user;
    private String currentCompanyId;
    private GitConnector conn;
    private File attachment;
    private CreateGitConnectorJobForm gcForm;
    private String[] targetLocales;
    private String attachmentName;
    private String uuid;
    private HashMap<String, String> FilePaths = new HashMap<String, String>();
    
    public CreateGitConnectorJobThread(User user, String currentCompanyId,
            GitConnector conn, CreateGitConnectorJobForm gcForm,
            String[] targetLocales, File attachment, String attachmentName,
            String uuid, String[] filePaths)
    {
        super();

        this.user = user;
        this.currentCompanyId = currentCompanyId;
        this.conn = conn;
        this.gcForm = gcForm;
        this.targetLocales = targetLocales;
        this.attachment = attachment;
        this.attachmentName = attachmentName;
        this.uuid = uuid;
        for(String filePath: filePaths)
        {
        	FilePaths.put(CreateJobUtil.getFileId(filePath), filePath);
        }
    }

    private void createJob() throws Exception
    {
        try
        {
            String jobName = gcForm.getJobName();
            String comment = gcForm.getComment();
            String priority = gcForm.getPriority();
            
            String randomStr = String.valueOf((new Random()).nextInt(999999999));
            while (randomStr.length() < 9)
            {
                randomStr = "0" + randomStr;
            }
            jobName = jobName + "_" + randomStr;

            List<String> files = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            String fileMapFileProfile = gcForm.getFileMapFileProfile();
            String[] ffs = fileMapFileProfile.split(" ");
            for (String ff : ffs)
            {
                String[] f = ff.split("-");
                files.add(FilePaths.get(f[0]));
                fileProfileList.add(HibernateUtil.get(FileProfileImpl.class,
                        Long.parseLong(f[1].substring(f[1].indexOf(",") + 1))));
            }

            long l10Id = fileProfileList.get(0).getL10nProfileId();
            BasicL10nProfile l10Profile = HibernateUtil.get(
                    BasicL10nProfile.class, l10Id);

            String locs = this.initTargetLocale(targetLocales);

			Job job = JobCreationMonitor.initializeJob(jobName, uuid,
					user.getUserId(), l10Id, priority, Job.IN_QUEUE,
					Job.JOB_TYPE_GIT);

            String sourceLocaleName = l10Profile.getSourceLocale()
                    .getLocaleCode();
            // init files and file profiles infomation
            List<String> descList = new ArrayList<String>();
            retrieveRealFilesFromGitFolder(descList, job, files, sourceLocaleName);
            Map<String, long[]> filesToFpId = FileProfileUtil
            		.excuteScriptOfFileProfile(descList, fileProfileList, job);

            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());
            List<JobAttribute> jobAttribtues = getJobAttributes(
            		gcForm.getAttributeString(), l10Profile);
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
                        .getFileStorageDirPath(currentCompanyId))
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
            
            //save git connector job
            GitConnectorJob gitConnectorJob = new GitConnectorJob();
            gitConnectorJob.setGitConnectorId(Long.parseLong(gcForm.getGitConnectorId()));
            gitConnectorJob.setJobId(job.getId());
            HibernateUtil.save(gitConnectorJob);
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
    
    private void retrieveRealFilesFromGitFolder(List<String> descList, Job job,
            List<String> files, String sourceLocale)
            throws FileNotFoundException, Exception
    {
        GitConnectorHelper helper = new GitConnectorHelper(conn);
        File gitFolder = helper.getGitFolder();
        for (String f : files)
        {
            String externalPageId = getFilePath(sourceLocale, job.getId(),
                    f.substring(gitFolder.getPath().length() + 1));
            String targetFilePath = AmbFileStoragePathUtils.getCxeDocDir() 
            		+ File.separator + externalPageId;
            FileUtil.copyFile(new File(f), new File(targetFilePath));
            descList.add(externalPageId); 
        }
    }

    private String getFilePath(String sourceLocale, long jobId,
            String file)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(sourceLocale).append(File.separator).append(jobId)
        		.append(File.separator).append(file);
        String str = filePath.toString().replace("\\", "/");

        return handleSpecialChars(str);
    }

    // The "pathName" uses "/" as separator.
    private String handleSpecialChars(String pathName)
    {
        StringBuffer sb = new StringBuffer();
        String[] arr = pathName.split("/");
        if (arr != null && arr.length > 0)
        {
            for (String str : arr)
            {
                str = str.replace("\\", "");
                str = str.replace("/", "");
                str = str.replace(":", "");
                str = str.replace("*", "");
                str = str.replace("?", "");
                str = str.replace("\"", "");
                str = str.replace("<", "");
                str = str.replace(">", "");
                str = str.replace("|", "");

                sb.append(str).append(File.separator);
            }
        }
        if (sb.toString().endsWith(File.separator))
        {
            return sb.substring(0, sb.length() - 1);
        }

        return sb.toString();
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

