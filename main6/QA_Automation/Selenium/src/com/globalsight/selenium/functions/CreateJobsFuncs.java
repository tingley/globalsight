package com.globalsight.selenium.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jodd.util.StringUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.testng.Reporter;

import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.thoughtworks.selenium.Selenium;

public class CreateJobsFuncs extends BasicFuncs
{
    /**
     * Create Job
     * 
     * @param jobName
     * @param filesStr
     * @param fileProfileNamesStr
     * @param targetLocales
     */
	
	private ArrayList<String> propertyNameArray = new ArrayList<String>();
	
    public String createJob(String jobName, String filesStr,
            String fileProfileNamesStr, String targetLocales)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmm");
    	if (StringUtil.isEmpty(jobName))
    		return null;
    	
    	String autoGenerateJobNameStr = ConfigUtil.getConfigData("autoGenerateJobName").trim();
    	boolean autoGenerateJobName = "true".equalsIgnoreCase(autoGenerateJobNameStr);
    	
    	//Generate a new job name suffix with time stamp for repeated test running
    	String newJobName = null;
        try
        {
            String[] jobFiles = filesStr.split(",");
            String[] fileProfileNames = fileProfileNamesStr.split(",");

            String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
                    + "/globalsight/services/AmbassadorWebService?wsdl";

            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
            String token = service.login(
                    ConfigUtil.getConfigData("pmName"),
                    ConfigUtil.getConfigData("pmPassword"));
            String profileInfoEx = service.getFileProfileInfoEx(token);
            // Get fileprofileIds by file profile name
            Map<String, String> fileProfileNameIdMap = getFileProfileNameIdMap(profileInfoEx);
            ArrayList<String> fileProfileIds = getFileProfileIds(
                    fileProfileNames, fileProfileNameIdMap);
            if ((fileProfileNames.length==1) && (jobFiles.length>1)){
            	int fileProfileNamesLength = fileProfileNames.length;
            	int jobFilesLength = jobFiles.length;
            	String fileProfileId_0 =  fileProfileIds.get(0);
            	for (int i = 0; i < jobFiles.length-1; i++) {
            		fileProfileIds.add(fileProfileId_0);
            	}
            }
 
            if (autoGenerateJobName)
                newJobName = jobName + sdf.format(Calendar.getInstance().getTime());
            else
                newJobName = jobName;
//            ArrayList<String> fileProfileIds2 = new ArrayList<String>();
//            fileProfileIds2.add("1179");
            // before creating jobs, files must be uploaded first.
            boolean uploaded = uploadFileToServer(service, token, newJobName,
                    jobFiles, fileProfileIds);
            if (uploaded)
            {
                try
                {
                    String basePath = ConfigUtil.getConfigData("Base_Path");
                    for(int i=0;i<jobFiles.length;i++)
                    {
                        jobFiles[i] = basePath + jobFiles[i];
                    }
                    // create job
                    service.createJob(token, newJobName, "",
                            deleteDir(getStrWithStrike(jobFiles)),
                            getStrWithStrike(fileProfileIds),
                            getStrWithStrike(jobFiles.length, targetLocales));
                    
                    CreatedJob.addCreatedJob(jobName, newJobName);
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
        
        return newJobName;
    }

    
    public String createJob(String username, String password, String jobName, String filesStr,
            String fileProfileNamesStr, String targetLocales)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmm");
    	if (StringUtil.isEmpty(jobName))
    		return null;
    	
    	String autoGenerateJobNameStr = ConfigUtil.getConfigData("autoGenerateJobName").trim();
    	boolean autoGenerateJobName = "true".equalsIgnoreCase(autoGenerateJobNameStr);
    	
    	//Generate a new job name suffix with time stamp for repeated test running
    	String newJobName = null;
        try
        {
            String[] jobFiles = filesStr.split(",");
            String[] fileProfileNames = fileProfileNamesStr.split(",");

            String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
                    + "/globalsight/services/AmbassadorWebService?wsdl";

            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
            String token = service.login(username, password);
                    
            String profileInfoEx = service.getFileProfileInfoEx(token);
            // Get fileprofileIds by file profile name
            Map<String, String> fileProfileNameIdMap = getFileProfileNameIdMap(profileInfoEx);
            ArrayList<String> fileProfileIds = getFileProfileIds(
                    fileProfileNames, fileProfileNameIdMap);
            if ((fileProfileNames.length==1) && (jobFiles.length>1)){
            	int fileProfileNamesLength = fileProfileNames.length;
            	int jobFilesLength = jobFiles.length;
            	String fileProfileId_0 =  fileProfileIds.get(0);
            	for (int i = 0; i < jobFiles.length-1; i++) {
            		fileProfileIds.add(fileProfileId_0);
            	}
            }
 
            if (autoGenerateJobName)
                newJobName = jobName + sdf.format(Calendar.getInstance().getTime());
            else
                newJobName = jobName;
//            ArrayList<String> fileProfileIds2 = new ArrayList<String>();
//            fileProfileIds2.add("1179");
            // before creating jobs, files must be uploaded first.
            boolean uploaded = uploadFileToServer(service, token, newJobName,
                    jobFiles, fileProfileIds);
            if (uploaded)
            {
                try
                {
                    String basePath = ConfigUtil.getConfigData("Base_Path");
                    for(int i=0;i<jobFiles.length;i++)
                    {
                        jobFiles[i] = basePath + jobFiles[i];
                    }
                    // create job
                    service.createJob(token, newJobName, "",
                            deleteDir(getStrWithStrike(jobFiles)),
                            getStrWithStrike(fileProfileIds),
                            getStrWithStrike(jobFiles.length, targetLocales));
                    
                    CreatedJob.addCreatedJob(jobName, newJobName);
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
        
        return newJobName;
    }
    
    
//    public void setup(ArrayList<String> propertyNameArray)
//    {
//        this.propertyNameArray = propertyNameArray;
//    }
//
//    /**
//     * Create new file profiles
//     * 
//     * @param selenium
//     * @param testCaseName
//     */
//    public String createJob(String jobName, 
//            String fileProfileNamesStr, String targetLocales)
//    {
//        String fileProfileNames = propertyNameArray.get(0);
//        String descriptions = propertyNameArray.get(1);
//        String localProfiles = propertyNameArray.get(2);
//        String sourceFormats = propertyNameArray.get(3);
//        String filters = propertyNameArray.get(4);
//        String qa_filter = propertyNameArray.get(5);
//        String extensions = propertyNameArray.get(6);
//        
//        
//
//        String[] fileProfileNameArray = fileProfileNames.split(",");
//        String[] descriptionArray = descriptions.split(",");
//        String[] localProfileArray = localProfiles.split(",");
//        String[] sourceFormatArray = sourceFormats.split(",");
//        String[] filterArray = filters.split(",");
//        String[] qa_filterArray = qa_filter.split(",");
//        String[] extensionArray = extensions.split(",");
//        
//    
//    	SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmm");
//    	if (StringUtil.isEmpty(jobName))
//    		return null;
//    	
//    	String autoGenerateJobNameStr = ConfigUtil.getConfigData("autoGenerateJobName").trim();
//    	boolean autoGenerateJobName = "true".equalsIgnoreCase(autoGenerateJobNameStr);
//    	
//    	//Generate a new job name suffix with time stamp for repeated test running
//    	String newJobName = null;
//        try
//        {
//            String[] jobFiles = filesStr.split(",");
//            String[] fileProfileNames = fileProfileNamesStr.split(",");
//
//            String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
//                    + "/globalsight/services/AmbassadorWebService?wsdl";
//
//            AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
//            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
//            String token = service.login(
//                    ConfigUtil.getConfigData("pmName"),
//                    ConfigUtil.getConfigData("pmPassword"));
//            String profileInfoEx = service.getFileProfileInfoEx(token);
//            // Get fileprofileIds by file profile name
//            Map<String, String> fileProfileNameIdMap = getFileProfileNameIdMap(profileInfoEx);
//            ArrayList<String> fileProfileIds = getFileProfileIds(
//                    fileProfileNames, fileProfileNameIdMap);
//            if ((fileProfileNames.length==1) && (jobFiles.length>1)){
//            	int fileProfileNamesLength = fileProfileNames.length;
//            	int jobFilesLength = jobFiles.length;
//            	String fileProfileId_0 =  fileProfileIds.get(0);
//            	for (int i = 0; i < jobFiles.length-1; i++) {
//            		fileProfileIds.add(fileProfileId_0);
//            	}
//            }
// 
//            if (autoGenerateJobName)
//                newJobName = jobName + sdf.format(Calendar.getInstance().getTime());
//            else
//                newJobName = jobName;
//            
//            // before creating jobs, files must be uploaded first.
//            boolean uploaded = uploadFileToServer(service, token, newJobName,
//                    jobFiles, fileProfileIds);
//            if (uploaded)
//            {
//                try
//                {
//                    String basePath = ConfigUtil.getConfigData("Base_Path");
//                    for(int i=0;i<jobFiles.length;i++)
//                    {
//                        jobFiles[i] = basePath + jobFiles[i];
//                    }
//                    // create job
//                    service.createJob(token, newJobName, "",
//                            deleteDir(getStrWithStrike(jobFiles)),
//                            getStrWithStrike(fileProfileIds),
//                            getStrWithStrike(jobFiles.length, targetLocales));
//                    
//                    CreatedJob.addCreatedJob(jobName, newJobName);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                    Reporter.log(e.getMessage());
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Reporter.log(e.getMessage());
//        }
//        
//        return newJobName;
//    }
    /**
     * Get needed parameter
     * 
     * @param num
     * @param str
     * @return
     */
    private String getStrWithStrike(int num, String str)
    {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < num; i++)
        {
            tmp.append(str).append("|");
        }
        if (tmp.length() > 0)
        {
            tmp.deleteCharAt(tmp.length() - 1);
        }
        return tmp.toString();
    }

    /**
     * Get needed parameter
     * 
     * @param strs
     * @return
     */
    private String getStrWithStrike(String[] strs)
    {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < strs.length; i++)
        {
            tmp.append(strs[i]).append("|");
        }
        if (tmp.length() > 0)
        {
            tmp.deleteCharAt(tmp.length() - 1);
        }
        return tmp.toString();
    }

    /**
     * Get needed parameter
     * 
     * @param strs
     * @return
     */
    private String getStrWithStrike(ArrayList<String> strs)
    {
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < strs.size(); i++)
        {
            tmp.append(strs.get(i)).append("|");
        }
        if (tmp.length() > 0)
        {
            tmp.deleteCharAt(tmp.length() - 1);
        }
        return tmp.toString();
    }

    /**
     * Upload file to Server
     * 
     * @param service
     * @param token
     * @param jobName
     * @param jobFiles
     * @param fileProfileIds
     * @return
     */
    private boolean uploadFileToServer(Ambassador service, String token,
            String jobName, String[] jobFiles, ArrayList<String> fileProfileIds)
    {
        boolean upload = false;
        String basePath = ConfigUtil.getConfigData("Base_Path");
        for (int i = 0; i < jobFiles.length; i++)
        {
            String filePath = basePath + jobFiles[i];
            String fileProfileId = fileProfileIds.get(i);
            try
            {
                File aFile = new File(filePath);
                byte[] content = getFileContent(aFile);
                service.uploadFile(token, jobName,
                        aFile.getPath().substring(2), fileProfileId, content);
                upload = true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return upload;
    }

    /**
     * Read a file content into a byte array.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] getFileContent(File file) throws IOException
    {
        InputStream in = null;
        byte[] content = new byte[(int) file.length()];
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
            if (in != null)
                in.close();
        }
        return content;
    }

    /**
     * Get File Profile name and id
     * 
     * @param profileEx
     * @return
     * @throws DocumentException
     */
    private Map<String, String> getFileProfileNameIdMap(String profileEx)
            throws DocumentException
    {
        Map<String, String> profileNameIdMap = new HashMap<String, String>();

        Document profileDoc = DocumentHelper.parseText(profileEx);
        List<Element> profileList = profileDoc
                .selectNodes("/fileProfileInfo/fileProfile");
        for (Element node : profileList)
        {
            String id = node.selectSingleNode("id").getText();
            String name = node.selectSingleNode("name").getText();
            profileNameIdMap.put(name, id);
        }
        return profileNameIdMap;
    }

    /**
     * Get a random number
     * 
     * @return
     */
    private int getRamdomNumber()
    {
        int random = (int) (Math.random() * 1000000000);
        return random;
    }

    /**
     * Get File Profile Ids
     * 
     * @param fileProfileNames
     * @param fileProfileNameIdMap
     * @return
     */
    private ArrayList<String> getFileProfileIds(String[] fileProfileNames,
            Map fileProfileNameIdMap)
    {
        ArrayList<String> fileProfileIds = new ArrayList<String>();

        for (int i = 0; i < fileProfileNames.length; i++)
        {
            String fileProfileName = fileProfileNames[i];
            String filrProfileId = (String) fileProfileNameIdMap
                    .get(fileProfileName);
            fileProfileIds.add(filrProfileId);
        }
        return fileProfileIds;
    }

    private String deleteDir(String path)
    {
        StringBuffer result = new StringBuffer();
        String[] paths = path.split("\\|");
        for (String tmp : paths)
        {
            result.append(tmp.substring(2)).append("|");
        }
        if (result.length() > 0)
        {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

}
