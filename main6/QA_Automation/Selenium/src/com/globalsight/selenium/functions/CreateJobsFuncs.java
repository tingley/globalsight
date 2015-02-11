package com.globalsight.selenium.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.testng.Reporter;

import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;

public class CreateJobsFuncs extends BasicFuncs
{
    
    public void create(String className)
    {
        try
        {
            String wsdlUrl = ConfigUtil.getConfigData("SERVER_URL") + "/globalsight/services/AmbassadorWebService?wsdl";
                              
            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
            String token = service.login(ConfigUtil.getConfigData("admin_login_name"),
                    ConfigUtil.getConfigData("admin_password"));
            String profileEx = service.getFileProfileInfoEx(token);
            Map fileProfileMap = this.getFileProfileMap(profileEx);
            
            /*
             * All test files are put under this directory, and
             * it contains sereval subfolders. The name of subfolders
             * are set to be the job name, and files under subfolders
             * are all in this single job.
             */
            String fileDir = ConfigUtil.getConfigData("Base_Path")
                    + "JobCreate\\";
            int i = 0;
            while (i > -1)
            {
                i++;
                String jobName = ConfigUtil.getDataInCase(className, "jobName"
                        + i);
                if (jobName == null)
                {
                    // All the jobs have been created
                    break;
                }
                String[] filesNamesInJob = ConfigUtil.getDataInCase(className,
                        "jobFiles" + i).split(",");

                ArrayList<File> filesInJob = new ArrayList<File>();
                ArrayList<String> filesFormat = new ArrayList<String>();
                for (String fileName : filesNamesInJob)
                {
                    if (fileName.indexOf("(office2010)") > 0)
                    {
                        fileName = fileName.substring(0,
                                fileName.indexOf("(office2010)"));
                        filesFormat.add("office2010");
                    }
                    else
                    {
                        filesFormat.add("");
                    }

                    filesInJob.add(new File(fileDir + fileName));
                }

                String[] tmp = getFilePathAndFileProfile(filesInJob,
                        fileProfileMap, filesFormat);

                String targetLocale = ConfigUtil
                        .getConfigData("TARGET_LOCALES");
                String locales = getTargetLocales(filesInJob.size(),
                        targetLocale);

                // before creating jobs, files must be uploaded first.
                boolean uploaded = uploadFileToServer(service, token, jobName,
                        tmp[0], tmp[1]);
                if (uploaded)
                {
                    try
                    {
                        String paths = deleteDir(tmp[0]);
                        // create job
                        service.createJob(token, jobName, "", paths, tmp[1],
                                locales);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Reporter.log(e.getMessage());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
    }
    
    //just create one job, using the jobName and jobFiles, for testing excel filter
    public void create(String jobName, String jobFiles, String dir)
    {
        try
        {
            String wsdlUrl = ConfigUtil.getConfigData("SERVER_URL") + "/globalsight/services/AmbassadorWebService?wsdl";
                              
            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
            String token = service.login(ConfigUtil.getConfigData("admin_login_name"),
                    ConfigUtil.getConfigData("admin_password"));
            String profileEx = service.getFileProfileInfoEx(token);
            Map fileProfileMap = this.getFileProfileMap(profileEx);
            
            /*
             * All test files are put under this directory, and
             * it contains sereval subfolders. The name of subfolders
             * are set to be the job name, and files under subfolders
             * are all in this single job.
             */
            String fileDir = ConfigUtil.getConfigData("Base_Path")
                    + dir;
            String[] filesNamesInJob = jobFiles.split(",");

            ArrayList<File> filesInJob = new ArrayList<File>();
            ArrayList<String> filesFormat = new ArrayList<String>();
            for (String fileName : filesNamesInJob)
            {
                if (fileName.indexOf("(office2010)") > 0)
                {
                    fileName = fileName.substring(0,
                            fileName.indexOf("(office2010)"));
                    filesFormat.add("office2010");
                }
                else
                {
                    filesFormat.add("");
                }

                filesInJob.add(new File(fileDir + fileName));
            }

            String[] tmp = getFilePathAndFileProfile(filesInJob,
                    fileProfileMap, filesFormat);

            String targetLocale = ConfigUtil
                    .getConfigData("TARGET_LOCALES");
            String locales = getTargetLocales(filesInJob.size(),
                    targetLocale);

            // before creating jobs, files must be uploaded first.
            boolean uploaded = uploadFileToServer(service, token, jobName,
                    tmp[0], tmp[1]);
            if (uploaded)
            {
                try
                {
                    String paths = deleteDir(tmp[0]);
                    // create job
                    service.createJob(token, jobName, "", paths, tmp[1],
                            locales);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Reporter.log(e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Reporter.log(e.getMessage());
        }
    }
    
    private String getTargetLocales(int fileNo, String targetLocale)
    {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < fileNo; i++)
        {
            tmp.append(targetLocale).append("|");
        }
        if (tmp.length() > 0)
        {
            tmp.deleteCharAt(tmp.length() - 1);
        }
        return tmp.toString();
    }
    
    private String deleteDir(String path)
    {
        StringBuffer result = new StringBuffer();
        String[] paths = path.split("\\|");
        for (String tmp : paths)
        {
            result.append(tmp.substring(3)).append("|");
        }
        if (result.length() > 0)
        {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }
    
    
    /**
     * Upload files to server to create jobs.
     * @param service 
     * @param token
     * @param jobName
     * @param filePaths
     * @param fileProfileIds
     */
    private boolean uploadFileToServer(Ambassador service, String token, String jobName,
            String filePath, String fileProfileId)
    {
        boolean upload = false;
        if (!filePath.equals("") && !fileProfileId.equals(""))
        {
            String[] filePaths = filePath.split("\\|");
            String[] fileProfiles = fileProfileId.split("\\|");
            for (int i = 0; i < filePaths.length; i++)
            {
                String path = filePaths[i];
                String fileProfile = fileProfiles[i];
                
                try
                {
                    File aFile = new File(path);
                    byte[] content = getFileContent(aFile);
                    service.uploadFile(token, jobName, aFile.getPath().substring(3), fileProfile, content);
                    upload = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return upload;
    }
    
    /**
     * Read a file content into a byte array.
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] getFileContent(File file) throws IOException
    {
        InputStream in = null;
        byte[] content = new byte[(int)file.length()];
        try
        {
            in = new FileInputStream(file);
            int i = 0;
            int tmp = 0;
            while ((tmp = in.read()) != -1)
            {
                content[i] = (byte) tmp;
                i++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (in != null) in.close();
        }
        return content;
    }
    
    /**
     * Restore all available file extensions and file profiles in a map.
     * The key of the map is extension name, and the value of map is
     * the file profile id.
     * @param profileInfo
     * @return
     * @throws DocumentException
     */
    @SuppressWarnings("unchecked")
    private Map getFileProfileMap(String profileEx) throws DocumentException
    {
        Map extensionAndProfileMap = new HashMap();
        
        Document profileDoc = DocumentHelper.parseText(profileEx);
        List<Element> profileList = profileDoc.selectNodes("/fileProfileInfo/fileProfile");
        for (Element node : profileList)
        {
            String id = node.selectSingleNode("id").getText();
            String sft = node.selectSingleNode("sourceFileFormat").getText();
            List<Element> extensionList = node.selectNodes("fileExtensionInfo/fileExtension");
            for (Element extension : extensionList)
            {
                String ext = extension.getText();
                Map tmp;
                if (extensionAndProfileMap.containsKey(ext))
                {
                    tmp = (Map) extensionAndProfileMap.get(ext);
                }
                else 
                {
                    tmp = new HashMap();
                }
                tmp.put(sft, id);
                extensionAndProfileMap.put(ext, tmp);
            }
        }
        return extensionAndProfileMap;
    }
    
    /**
     * Get a random number
     * @return
     */
    private int getRamdomNumber()
    {
        int random = (int) (Math.random() * 1000000000);
        return random;
    }
    
    @SuppressWarnings("unchecked")
    private String[] getFilePathAndFileProfile(ArrayList<File> filesInJob,
            Map fileProfileMap, ArrayList<String> filesFormat)
    {
        StringBuffer filePaths = new StringBuffer();
        StringBuffer fileProfiles = new StringBuffer();
        
        for (int i = 0; i < filesInJob.size(); i++)
        {
            File aFile = filesInJob.get(i);
            String fileName = aFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            Map sourceFormatAndFileProfileMap = (Map) fileProfileMap.get(extension);
            
            if (sourceFormatAndFileProfileMap == null)
            {
                continue;
            }
            String fileProfileId = null;
            if (sourceFormatAndFileProfileMap.keySet().size() == 1)
            {
                String[] tmp = new String[sourceFormatAndFileProfileMap.keySet().size()];
                sourceFormatAndFileProfileMap.values().toArray(tmp);
                fileProfileId = tmp[0];
            }
            else 
            {
                if (filesFormat.get(i).equals("office2010"))
                {
                    fileProfileId = (String) sourceFormatAndFileProfileMap.get("43");
                }
                else 
                {
                    Set keys = sourceFormatAndFileProfileMap.keySet();
                    for (Iterator iterator = keys.iterator(); iterator
                            .hasNext();)
                    {
                        String key = (String) iterator.next();
                        if (!key.equals("43"))
                        {
                            fileProfileId = (String) sourceFormatAndFileProfileMap.get(key);
                        }
                    }
                }
            }
            
            if (fileProfileId != null)
            {
                String path = aFile.getPath();
                filePaths.append(path).append("|");
                fileProfiles.append(fileProfileId).append("|");
            }
            else
            {
                Reporter.log(aFile.getPath() + " doesn't have a vaild fileprofile, ignore.");
                return null;
            }
        }
        
        if (filePaths.length() > 0)
        {
            filePaths.deleteCharAt(filePaths.length() - 1);
            fileProfiles.deleteCharAt(fileProfiles.length() - 1);
        }
        
        return new String[] {filePaths.toString(), fileProfiles.toString()};
    }
    
}
