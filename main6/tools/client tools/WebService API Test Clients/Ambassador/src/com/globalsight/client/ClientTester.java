package com.globalsight.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.WebServiceException;


public class ClientTester
{
	private static int MAX_SEND_SIZE = 5 * 1000 * 1024;//5M
	private static String HOST_NAME = "localhost";
	private static String HOST_PORT = "8080";
	private static String userName = "york";
	private static String password = "password";
	private static boolean enableHttps = false;

    public static Ambassador getAmbassador() throws Exception
    {
        Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                HOST_NAME, HOST_PORT, userName, password, enableHttps);
        return ambassador;
    }

    public static void main(String[] args)
	{
		try
		{
			Ambassador ambassador = getAmbassador();
			String fullAccessToken = ambassador.login(userName, password);
			System.out.println("fullAccessToken : " + fullAccessToken);

//			testGetDownloadableJobs(ambassador, fullAccessToken);
//			testArchiveJobs(ambassador, fullAccessToken);

//			testGetTasksInJob(ambassador, fullAccessToken);
//			testAcceptTask(ambassador, fullAccessToken);
//			testCompleteTask(ambassador, fullAccessToken);
			
//			testGetImportExportStatus(ambassador, fullAccessToken);

			// GBS-3696 (8.5.8)
//            testGetWorkOfflineFiles(ambassador, fullAccessToken);
//
//            String identifyKey = testUploadWorkOfflineFiles(ambassador,
//                    fullAccessToken);
//            testImportWorkOfflineFiles(ambassador, fullAccessToken, identifyKey);
			
			testDownloadXliffOfflineFile(ambassador, fullAccessToken);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static void testGetTasksInJob(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String jobId = "394";
		String xml = ambassador.getTasksInJobs(accessToken, jobId, null);
		System.out.println(xml);
	}

	private static void testAcceptTask(Ambassador ambassador, String accessToken)
			throws Exception
	{
		String taskId = "7679";
		String result = ambassador.acceptTask(accessToken, taskId);
		System.out.println(result);
	}

	private static void testCompleteTask(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String taskId = "7679";
		String destinationArrow = "Action3";
		String result = ambassador.completeTask(accessToken, taskId,
				destinationArrow);
		System.out.println(result);
	}

    private static void testGetDownloadableJobs(Ambassador ambassador,
            String accessToken) throws Exception
    {
        String jobName = "ASTD_12-06-2012_04.37.18_9f350b7ef3d3e6";
        StringBuffer msg = new StringBuffer();
        msg.append("<jobs>\r\n");
        msg.append("  <job>\r\n");
        msg.append("    <name>").append(jobName).append("</name>\r\n");
        msg.append("    <status>unknown</status>\r\n");
        msg.append("  </job>\r\n");
        msg.append("</jobs>");

        String result = ambassador.getDownloadableJobs(accessToken,
                msg.toString());
        System.out.println(result);
    }

    private static void testArchiveJobs(Ambassador ambassador,
            String accessToken) throws Exception
    {
        String jobIds = "176";
        String result = ambassador.archiveJob(accessToken, jobIds);
        System.out.println(result);
    }

    /**
    private static void testUploadOriginalSourceFile(String p_accessToken) throws Exception
    {
    	File file = new File("C:\\Documents and Settings\\york\\Desktop\\974.txt");
		if (!file.exists())
		{
			throw new Exception("File(" + file.getPath() + ") is not exist");
		}

		// Init some parameters.
		String path = file.getAbsolutePath();
		String filePath = path.substring(path.indexOf(File.separator) + 1);
		int len = (int) file.length();
		BufferedInputStream inputStream = null;
		ArrayList fileByteList = new ArrayList();

		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			int size = len / MAX_SEND_SIZE;

			// Separates the file to several parts according to the size.
			for (int i = 0; i < size; i++)
			{
				byte[] fileBytes = new byte[MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}

			if (len % MAX_SEND_SIZE > 0)
			{
				byte[] fileBytes = new byte[len % MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}
			
			// Uploads all parts of files.
			Ambassador abmassador = getAmbassador();
			for (int i = 0; i < fileByteList.size(); i++)
			{				
				HashMap map = new HashMap();
				map.put("accessToken", p_accessToken);
				String jobName = "job_name_parameter";
				map.put("jobName", jobName);
				String targetLocale = "target_locale";
				map.put("targetLocale", targetLocale);
				map.put("fileName", "974.txt");
				map.put("bytes", fileByteList.get(i));
				abmassador.uploadOriginalSourceFile(map);
			}
		}
		catch (Exception e)
		{
            throw e;
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
    }
    */
    
    /**
    private static void testCreateEditionJob(String p_accessToken) throws Exception
    {
    	Ambassador abmassador = getAmbassador();
    	
    	HashMap paramMap = new HashMap();
    	paramMap.put("accessToken", p_accessToken);
        paramMap.put("jobName", "my_job_name_001");
        
        Vector filePaths = new Vector();
        filePaths.add("log\\index.html");
        paramMap.put("filePaths", filePaths);
        
        Vector targetLocales = new Vector();
        targetLocales.add("zh_CN");
        paramMap.put("targetLocales", targetLocales);
        
        Vector fileProfileIds = new Vector();
        fileProfileIds.add("1003");
        paramMap.put("fileProfileIds", fileProfileIds);
        
        paramMap.put("taskId", "123");
        paramMap.put("priority", "3");
        paramMap.put("wsdlUrl", "http://10.10.11.206:80/globalsight/services/AmbassadorWebService?wsdl");
        paramMap.put("userName", "qaadmin1");
        paramMap.put("password", "password1");
        
        Vector jobCommentVector = new Vector();
        String comment = "6666+_+create_date+_+qaadmin+_+comment text aaa+_+123+_+T+_+7777+_+null";
        jobCommentVector.add(comment);
        paramMap.put("jobComments", jobCommentVector);
        abmassador.createEditionJob(paramMap);
    }
    */
    
    /**
    private static void testStringSplit()
    {
    	String comment = "1000+_+create_date+_+qaadmin+_+comment text aaa+_+111+_+T+_+ ";
    	String[] strs = comment.split("\\+\\_\\+");
    	for (int i=0; i<strs.length; i++)
    	{
    		System.out.println(i + " :: " + strs[i]);
    	}    	
    }
    */
    
    private static void testUploadCommentReferenceFiles(String p_accessToken) throws Exception
    {
    	File file = new File("C:\\Documents and Settings\\york\\Desktop\\974.txt");
		if (!file.exists())
		{
			throw new Exception("File(" + file.getPath() + ") is not exist");
		}

		// Init some parameters.
		String path = file.getAbsolutePath();
		String filePath = path.substring(path.indexOf(File.separator) + 1);
		int len = (int) file.length();
		BufferedInputStream inputStream = null;
		ArrayList fileByteList = new ArrayList();

		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			int size = len / MAX_SEND_SIZE;

			// Separates the file to several parts according to the size.
			for (int i = 0; i < size; i++)
			{
				byte[] fileBytes = new byte[MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}

			if (len % MAX_SEND_SIZE > 0)
			{
				byte[] fileBytes = new byte[len % MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}
			
			// Uploads all parts of files.
			Ambassador abmassador = getAmbassador();
			for (int i = 0; i < fileByteList.size(); i++)
			{
				HashMap map = new HashMap();
				map.put("accessToken", p_accessToken);
				map.put("fileName", "974.txt");
				map.put("originalTaskId", "6666");
				map.put("wsdlUrl", "http://10.10.11.206:80/globalsight/services/AmbassadorWebService?wsdl");
				map.put("bytes", fileByteList.get(i));
				map.put("access", "Restricted");
				abmassador.uploadCommentReferenceFiles(map);
			}
		}
		catch (Exception e)
		{
            throw e;
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
    }

    private static boolean testGetImportExportStatus(Ambassador ambassador,
            String p_accessToken)
    {
        try
        {
            String status = ambassador.getImportExportStatus(p_accessToken);
            org.dom4j.Document dom = DocumentHelper.parseText(status);
            org.dom4j.Element root = dom.getRootElement();
            String creatingNum = root.element("jobsCreating").getText();
            String exportingNum = root.element("localesExporting").getText();
            int cNum = Integer.valueOf(creatingNum);
            int eNum = Integer.valueOf(exportingNum);
            if (cNum > 0 || eNum > 0)
                return true;
        }
        catch (Exception e)
        {

        }

        return false;
    }

    // GBS-3696 (8.5.8)
    private static File testGetWorkOfflineFiles(Ambassador ambassador,
            String p_accessToken) throws Exception
    {
        long taskId = 5084;
        int workOfflineFileType = 2;
        String result = ambassador.getWorkOfflineFiles(p_accessToken, taskId,
                workOfflineFileType);
        System.out.println(result);

        try
        {
            String path = result;
            path = path.replace("\\\\", "/");
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String urlDecode = URLDecoder.decode(path, "UTF-8").replace(" ", "%20");

            URL url = new URL(urlDecode);
            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
            hurl.connect();
            InputStream is = hurl.getInputStream();
            File localFile = new File("C:\\local", fileName);
            saveFile(is, localFile);
            System.out.println("Report is save to local :: "
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

    private static String testUploadWorkOfflineFiles(
            Ambassador ambassador, String p_accessToken) throws Exception
    {
        long taskId = 4383;
        int workOfflineFileType = 2;

        File uploadFile = new File("D:\\_tmp\\as_850129247.rtf");
        String fileName = uploadFile.getName();

        byte[] bytes = null;
        bytes = new byte[(int) uploadFile.length()];
        FileInputStream fin = new FileInputStream(uploadFile);
        fin.read(bytes, 0, (int) uploadFile.length());

        String identifyKey = ambassador.uploadWorkOfflineFiles(p_accessToken,
                taskId, workOfflineFileType, fileName, bytes);
        System.out.println(identifyKey);

        return identifyKey;
    }

    private static void testImportWorkOfflineFiles(
            Ambassador ambassador, String p_accessToken, String p_identifyKey)
            throws Exception
    {
        long taskId = 4383;
        int workOfflineFileType = 2;

        String result = ambassador.importWorkOfflineFiles(p_accessToken,
                taskId, p_identifyKey, workOfflineFileType);
        System.out.println(result);
    }

    private static void testDownloadXliffOfflineFile(Ambassador ambassador,
            String p_accessToken) throws Exception
    {
        String taskId = "5726";
        String lockedSegEditType = "4";
        String result = ambassador.downloadXliffOfflineFile(p_accessToken, taskId, lockedSegEditType);
        System.out.println(result);
    }
}
