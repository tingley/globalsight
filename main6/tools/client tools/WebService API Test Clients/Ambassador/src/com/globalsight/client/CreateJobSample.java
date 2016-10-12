package com.globalsight.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import com.globalsight.www.webservices.Ambassador;

import org.apache.poi.POITextExtractor;  
import org.apache.poi.extractor.ExtractorFactory;  


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
			// login to get token which is required in any later invoking.
			String fullAccessToken = ambassador.login(userName, password);
			System.out.println("fullAccessToken : " + fullAccessToken);

			// First step : getUniqueJobName
			// GlobalSight requires job name unique
			testGetUniqueJobName(ambassador, fullAccessToken);

			// Second step : getFileProfileInfoEx, get all file profile.(If you
			// know the file profile, then the method can not be executed)
			testGetFileProfileInfoEx(ambassador, fullAccessToken);

			// Third step : uploadFile, upload files to the server.
			// For non-java client language.
			// Same as "uploadFileForInitial".
			testUploadFile(ambassador, fullAccessToken);

			// Third step : uploadFileForInitial,upload files to the server.
			// For Java
			testUploadFileForInitial(ambassador, fullAccessToken);

			// Fourth step : addJobComment,add comment for job.
			// Optional API
			testAddJobComment(ambassador, fullAccessToken);

			// Fifth step : createJob
			// For non-java client language.
			// Same as "createJobOnInitial"
			testCreateJob(ambassador, fullAccessToken);

			// Fifth step : createJobOnInitial
			// For java
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
		String jobName = "jobName";
		String uniqueJobName = ambassador
				.getUniqueJobName(accessToken, jobName);
		System.out.println(uniqueJobName);
	}
	
	private static void testGetFileProfileInfoEx(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String xml = ambassador.getFileProfileInfoEx(accessToken);
		System.out.println(xml);
	}
	
	/**
	 * Use this method to upload file contents to GlobalSight server. Generally,
	 * one call is for one file. For large file, it allows to be called multiple
	 * times to same file.
	 * 
	 * @param ambassador
	 * @param accessToken
	 * @throws Exception
	 */
	private static void testUploadFile(Ambassador ambassador, String accessToken)
			throws Exception
	{
		String uniqueJobName = "jobName_890581768";

		// If local path is wanted
		// String filePath = "Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		// String filePath = "accuracy_test_results.htm";
		// String filePath = "01 - Overview_Microsoft.docx";
		String filePath = "Welocalize_company.pptx";
		// String filePath = "tm_100_InContext_Fuzzy_NoMatch.xml";

		// String fileProfileId = "44";// htm
		String fileProfileId = "42";// office
		// String fileProfileId = "48";//xml

		// String fileStr = "C:/Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		String fileStr = "C:/Users/a/Desktop/down/test/pptx/Welocalize_Company.pptx";
		// String fileStr ="C:/Users/a/Desktop/down/test/xml/tm_100_InContext_Fuzzy_NoMatch.xml";

		String content = null;
		// If upload DOCX, XLSX, PPTX documents:
		if (filePath.toLowerCase().endsWith(".docx")
				|| filePath.toLowerCase().endsWith(".pptx")
				|| filePath.toLowerCase().endsWith(".xlsx"))
		{
			File uploadFile = new File(fileStr);
			POITextExtractor extractor = ExtractorFactory
					.createExtractor(uploadFile);
			content = extractor.getText();
		}
		else
		{
			 File uploadFile = new File(fileStr);
			 byte[] bytes = new byte[(int) uploadFile.length()];
			 FileInputStream fin = new FileInputStream(uploadFile);
			 fin.read(bytes, 0, (int) uploadFile.length());
			 fin.close();
			 content = new String(bytes);
		}

		ambassador.uploadFile(accessToken, uniqueJobName, filePath,
				fileProfileId, content);
	}

	/**
	 * Use this method to upload file contents to GlobalSight server. Generally,
	 * one call is for one file. For large file, it allows to be called multiple
	 * times to same file.
	 * 
	 * @param ambassador
	 * @param accessToken
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void testUploadFileForInitial(Ambassador ambassador,
			String accessToken) throws Exception
	{
		HashMap map = new HashMap();
		String uniqueJobName = "jobName_890581768";

		// String filePath = "accuracy_test_results.htm";
		// If local path is wanted
		// String filePath = "Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		String filePath = "9 - Safety and Security - Copy.docx";
		// String filePath = "tm_100_InContext_Fuzzy_NoMatch.xml";

		// String fileProfileId = "44";// htm
		String fileProfileId = "42";// office
		// String fileProfileId = "48";//xml

		// String fileStr = "C:/Users/a/Desktop/down/test/html/accuracy_test_results.htm";
		String fileStr = "C:/Users/a/Desktop/down/test/docx/19 - Safety and Security - Copy.docx";
		// String fileStr ="C:/Users/a/Desktop/down/test/xml/tm_100_InContext_Fuzzy_NoMatch.xml";

		byte[] bytes = null;
		// If upload DOCX, XLSX, PPTX documents:
		if (filePath.toLowerCase().endsWith(".docx")
				|| filePath.toLowerCase().endsWith(".pptx")
				|| filePath.toLowerCase().endsWith(".xlsx"))
		{
			File uploadFile = new File(fileStr);
			POITextExtractor extractor = ExtractorFactory
					.createExtractor(uploadFile);
			String content = extractor.getText();
			bytes = content.getBytes();
		}
		else
		{
			File file = new File(fileStr);
			InputStream is = new FileInputStream(file);
			bytes = new byte[(int) file.length()];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}
			is.close();
		}

		map.put("accessToken", accessToken);
		map.put("jobName", uniqueJobName);
		map.put("filePath", filePath);
		map.put("fileProfileId", fileProfileId);
		map.put("bytes", bytes);

		ambassador.uploadFileForInitial(map);
	}
	
	/**
	 * Add job comment and job reference file.
	 * 
	 * @param ambassador
	 * @param accessToken
	 * @throws Exception
	 */
	private static void testAddJobComment(Ambassador ambassador,
			String accessToken) throws Exception
	{
		String uniqueJobName = "jobName_890581768";
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

		String xml = ambassador.addJobComment(accessToken, uniqueJobName,
				userId, comment, bytes, fileName, access);
		System.out.println(xml);
	}
	
	private static void testCreateJob(Ambassador ambassador, String accessToken)
			throws Exception
	{
		String uniqueJobName = "jobName_890581768";
		String comment = "Test create job step.";

		// file pathnames are separated by "|"
		String filePaths = "accuracy_test_results.htm|19 - Safety and Security - Copy.docx|tm_100_InContext_Fuzzy_NoMatch.xml";
		// If local path is wanted:
//		String filePaths = "Users/a/Desktop/down/test/html/accuracy_test_results.htm|Users/a/Desktop/down/test/docx/01 - Overview_Microsoft.docx|Users/a/Desktop/down/test/xml/tm_100_InContext_Fuzzy_NoMatch.xml";

		// Separated by "|" with same sequence with files pathnames
		String fileProfileIds = "44|42|48";

		// this job has 3 target locales, every "fr_FR,de_DE,es_ES" is for one file 
		String targetLocales = "fr_FR,de_DE,es_ES|fr_FR,de_DE,es_ES|fr_FR,de_DE,es_ES";

		ambassador.createJob(accessToken, uniqueJobName, comment, filePaths,
				fileProfileIds, targetLocales);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testCreateJobOnInitial(Ambassador ambassador,
			String accessToken) throws Exception
	{
		HashMap map = new HashMap();
		Vector filePaths = new Vector();
		Vector fileProfileIds = new Vector();
		Vector targetLocales = new Vector();
		String jobId = "208";
		String comment = "Test create job step.";
		filePaths.add("accuracy_test_results.htm");
		filePaths.add("19 - Safety and Security - Copy.docx");
		filePaths.add("tm_100_InContext_Fuzzy_NoMatch.xml");

		// If local path is wanted:
		// filePaths.add("Users/a/Desktop/down/test/html/accuracy_test_results.htm");
		// filePaths.add("Users/a/Desktop/down/test/docx/01 - Overview_Microsoft.docx");
		// filePaths.add("Users/a/Desktop/down/test/xml/tm_100_InContext_Fuzzy_NoMatch.xml");

		fileProfileIds.add("44");
		fileProfileIds.add("42");
		fileProfileIds.add("48");

		targetLocales.add("fr_FR,de_DE,es_ES");// for first file
		targetLocales.add("fr_FR,de_DE,es_ES");// for second file
		targetLocales.add("fr_FR,de_DE,es_ES");// for third file

		map.put("accessToken", accessToken);
		map.put("jobId", jobId);
		map.put("comment", comment);
		map.put("filePaths", filePaths);
		map.put("fileProfileIds", fileProfileIds);
		map.put("targetLocales", targetLocales);

		ambassador.createJobOnInitial(map);
	}
}
