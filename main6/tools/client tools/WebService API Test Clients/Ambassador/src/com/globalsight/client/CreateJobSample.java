package com.globalsight.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import com.globalsight.www.webservices.Ambassador;

public class CreateJobSample
{
	private static String HOST_NAME = "localhost";
	private static String HOST_PORT = "8080";
	private static String userName = "testadmin";
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

			// First step : getUniqueJobName, get a unique job name.(If you can
			// make sure that the job name of the sole, then the method can not
			// be executed)
			testGetUniqueJobName(ambassador, fullAccessToken);

			// Second step : getFileProfileInfoEx, get all file profile.(If you
			// know the file profile, then the method can not be executed)
			testGetFileProfileInfoEx(ambassador, fullAccessToken);

			// Third step : uploadFile,upload files to the server.
			testUploadFile(ambassador, fullAccessToken);

			// Third step : uploadFileForInitial,upload files to the
			// server.(uploadFileForInitial and uploadFile same function)
			testUploadFileForInitial(ambassador, fullAccessToken);

			// Fourth step : addJobComment,add comment for job.(If you do not
			// add a comment,then the method can not be executed)
			testAddJobComment(ambassador, fullAccessToken);

			// Fifth step : createJob
			testCreateJob(ambassador, fullAccessToken);

			// Fifth step : createJobOnInitial (createJob and createJobOnInitial
			// same function)
			testCreateJobOnInitial(ambassador, fullAccessToken);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void testGetUniqueJobName(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String jobName = "test";
		String xml = ambassador.getUniqueJobName(accessToken, jobName);
		System.out.println(xml);
	}
	
	private static void testGetFileProfileInfoEx(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String xml = ambassador.getFileProfileInfoEx(accessToken);
		System.out.println(xml);
	}
	
	private static void testUploadFile(Ambassador ambassador, String accessToken)
			throws Exception
	{
		String jobName = "test_068746575";
		String filePath = "accuracy_test_results.htm";
		String fileProfileId = "44";
		String fileStr = "C:/Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		FileReader fr = new FileReader(fileStr);
		BufferedReader br = new BufferedReader(fr);
		StringBuffer buffer = new StringBuffer();
		while (br.readLine() != null)
		{
			buffer.append(br.readLine());
		}
		br.close();
		String content = buffer.toString();

		ambassador.uploadFile(accessToken, jobName, filePath, fileProfileId,
				content);
	}
	
	private static void testUploadFileForInitial(Ambassador ambassador,
			String accessToken) throws Exception
	{
		HashMap map = new HashMap();
		String jobName = "test_068746575";
		String filePath = "accuracy_test_results.htm";
		String fileProfileId = "44";
		String fileStr = "C:/Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		File file = new File(fileStr);
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		{
			offset += numRead;
		}
		is.close();
		
		map.put("accessToken", accessToken);
		map.put("jobName", jobName);
		map.put("filePath", filePath);
		map.put("fileProfileId", fileProfileId);
		map.put("bytes", bytes);
		
		ambassador.uploadFileForInitial(map);
	}
	
	private static void testAddJobComment(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String jobName = "test_068746575";
		String userId = "1064";
		String fileName = "books.xml";
		String access = "General";
		String comment = "test add job comment";
		String fileStr = "C:/Users/a/Desktop/down/test/xml/books.xml";
		File file = new File(fileStr);
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		{
			offset += numRead;
		}
		is.close();

		String xml = ambassador.addJobComment(accessToken, jobName, userId,
				comment, bytes, fileName, access);
		System.out.println(xml);
	}
	
	private static void testCreateJob(Ambassador ambassador, String accessToken)
			throws Exception
	{
		String jobName = "test_068746575";
		String comment = "Test create job step.";
		String filePaths = "accuracy_test_results.htm";
		String fileProfileIds = "44";
		String targetLocales = "fr_FR";
		ambassador.createJob(accessToken, jobName, comment, filePaths,
				fileProfileIds, targetLocales);
	}
	
	private static void testCreateJobOnInitial(Ambassador ambassador,
			String accessToken) throws Exception
	{
		HashMap map = new HashMap();
		Vector filePaths = new Vector();
		Vector fileProfileIds = new Vector();
		Vector targetLocales = new Vector();
		String jobId = "215";
		String comment = "Test create job step.";
		filePaths.add("accuracy_test_results.htm");
		fileProfileIds.add("44");
		targetLocales.add("fr_FR");

		map.put("accessToken", accessToken);
		map.put("jobId", jobId);
		map.put("comment", comment);
		map.put("filePaths", filePaths);
		map.put("fileProfileIds", fileProfileIds);
		map.put("targetLocales", targetLocales);

		ambassador.createJobOnInitial(map);
	}
}
