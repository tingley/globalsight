package com.globalsight.selenium.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
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

import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.globalsight.www.webservices.Ambassador4Falcon;
import com.globalsight.www.webservices.Ambassador4FalconServiceLocator;
import com.thoughtworks.selenium.Selenium;

public class OfflineDownloadUploadFuncs extends BasicFuncs
{
    /**
     * Offline Download and Upload
     * 
     * 
     */
	
	
	
	
    public void OfflineUploadImport(String username, String password, long taskId, int workOfflineFileType, 
    		String file)
    {
    	
    	File uploadFile = new File(file);
    	String fileName = uploadFile.getName();
    	
        try
        {
        	
        	byte[] bytes = null;
	        bytes = new byte[(int) uploadFile.length()];
	        FileInputStream fin = new FileInputStream(uploadFile);
	        fin.read(bytes, 0, (int) uploadFile.length());
	        fin.close();
	        
	        String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
                    + "/globalsight/services/AmbassadorWebService?wsdl";

	        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
            Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
            String token = service.login(username, password);
            
	        String identifyKey = service.uploadWorkOfflineFiles(token,
	                taskId, workOfflineFileType, fileName, bytes);
	        System.out.println(identifyKey);
	        
	        
	        
	        if (StringUtil.isNotEmpty(identifyKey)) {
	        	try
                {
	        		String result = service.importWorkOfflineFiles(token, 
	        				taskId, identifyKey, workOfflineFileType);
	        		if (StringUtil.isNotEmpty(result))
	        			System.out.println(result);
	        		else System.out.println("Finished offline importing " + fileName);
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
    
    public File OfflineDownload(String username, String password, Long taskId, int workOfflineFileType, 
    		String workofflineFileTypeOption, String file_location_path)
    {
    	if (workofflineFileTypeOption.equalsIgnoreCase("x")) 
    		workofflineFileTypeOption = "";
    	
	         try
            {
	        	 
	        	String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
	                     + "/globalsight/services/Ambassador4Falcon?wsdl";
	        	Ambassador4FalconServiceLocator loc = new Ambassador4FalconServiceLocator();
	        	Ambassador4Falcon service = loc.getAmbassador4Falcon(new URL(wsdlUrl));
		 	    String token = service.login(username, password);
	 	        String result = service.getWorkOfflineFiles(token, taskId, workOfflineFileType, 
	 	        		workofflineFileTypeOption);
	 	        System.out.println(result);
	 	        
                String path = result;
                path = path.replace("\\\\", "/");
                path = path.substring(path.indexOf("\":\"")+3, path.indexOf("\",\""));
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                String urlDecode = URLDecoder.decode(path, "UTF-8").replace(" ", "%20");

                URL url = new URL(urlDecode);
                HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
                hurl.connect();
                InputStream is = hurl.getInputStream();
                File localFile = new File(file_location_path, fileName);
//                File localFile = new File("C:\\GlobalSightAutomationFiles\\ResultFiles\\TestMatrix\\Others\\Longevity_OfflineDownload", fileName);
                saveFile(is, localFile);
                System.out.println("Offline is save to local :: "
                        + localFile.getAbsolutePath());
                return localFile;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    	
	private static void saveFile(InputStream is, File file) throws IOException,
	    FileNotFoundException
	{
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream outstream = new FileOutputStream(file);
		int c;
		while ((c = is.read()) != -1)
		{
		    outstream.write(c);
		}
		outstream.close();
		is.close();
		if (file.length() == 0)
		{
		    file.delete();
		}
	}
    
}

    


