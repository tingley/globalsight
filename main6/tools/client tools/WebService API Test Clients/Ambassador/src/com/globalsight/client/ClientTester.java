package com.globalsight.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
	private static String userName = "easyadmin";
	private static String password = "password";

    public static Ambassador getAmbassador() throws Exception
    {
        Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                HOST_NAME, HOST_PORT, userName, password, false);
        return ambassador;
    }

    public static Ambassador getAmbassador(String userName, String password)
            throws Exception
    {
        Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                HOST_NAME, HOST_PORT, userName, password, false);
        return ambassador;
    }
	
    public static void main(String[] args)
    {
    	try 
    	{
        	Ambassador ambassador = getAmbassador(userName, password);
        	String fullAccessToken = ambassador.login(userName, password);
        	System.out.println("fullAccessToken : " + fullAccessToken);

//        	testGetDownloadableJobs(ambassador, fullAccessToken);
        	
        	testArchiveJobs(ambassador, fullAccessToken);
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
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

}
