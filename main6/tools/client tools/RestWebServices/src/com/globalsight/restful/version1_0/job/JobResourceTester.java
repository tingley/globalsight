package com.globalsight.restful.version1_0.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;
import com.globalsight.restful.login.LoginResourceTester;
import com.globalsight.restful.util.FileUtil;
import com.globalsight.restful.util.URLEncoder;

public class JobResourceTester extends RestfulApiTestHelper
{
    private String accessToken = null;

    public JobResourceTester(String accessToken)
    {
        this.accessToken = accessToken;
    }

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/jobs/getUniqueJobName?jobName=ABC
	 */
	public void testGetUniqueJobName()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		try
		{
			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/jobs/getUniqueJobName");
			// required params
			url.append("?jobName").append(URLEncoder.encode("ABC"));

			HttpGet httpGet = getHttpGet(url.toString(), accessToken);

			httpResponse = httpClient.execute(httpGet);
			printHttpResponse(httpResponse);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consumeQuietly(httpResponse);
		}
	}

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/jobs/sourceFiles
	 */
	public void testUploadSourceFile()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		File file = new File("E:\\down\\test\\html\\work_with_legacy_tests.htm");
		BufferedInputStream inputStream = null;
		try
		{
			String jobName = "test_abc";
			String fileProfileId = "1065";

			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/jobs/sourceFiles");
			// required params
			url.append("?jobName=").append(URLEncoder.encode(jobName));
			url.append("&fileProfileId=").append(fileProfileId);

			// if tm file is large, we had better upload multiple times, 5M
			// every time.
			int len = (int) file.length();
			ArrayList<byte[]> fileByteList = new ArrayList<byte[]>();
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
			if (inputStream != null)
			{
				inputStream.close();
			}

			// Uploads all parts of files.
			for (int i = 0; i < fileByteList.size(); i++)
			{
				ByteArrayBody byteBody = new ByteArrayBody(fileByteList.get(i),
						file.getName());

				// if file is not big, can use FileBody to upload one time
				@SuppressWarnings("unused")
				FileBody fileBody = new FileBody(file);

				HttpEntity multiPartEntity = MultipartEntityBuilder.create()
						.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
						.addPart("anyNameIsOkay", byteBody).build();

				HttpPost httpPost = getHttpPost(url.toString(), accessToken);
				httpPost.setEntity(multiPartEntity);

				httpResponse = httpClient.execute(httpPost);

				printHttpResponse(httpResponse);

				consumeQuietly(httpResponse);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/jobs/createJob"
	 */
	public void testCreateJob()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		try
		{
			String jobId = "1471";
			String comment = "test_abc";
			String filePaths = "work_with_legacy_tests.htm";
			String p_fileProfileIds = "1065";
			String p_targetLocales = "fr_FR";
			String p_attributes = null;
			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/jobs/createJob");
			// required params
			url.append("?jobId=").append(jobId);
			url.append("&filePaths=").append(URLEncoder.encode(filePaths));
			url.append("&fileProfileIds=").append(URLEncoder.encode(p_fileProfileIds));
			url.append("&targetLocales=").append(URLEncoder.encode(p_targetLocales));
			// optional query params
			url.append("&comment=").append(URLEncoder.encode(comment));
			url.append("&attributes=").append(URLEncoder.encode(p_attributes));

			HttpPost httpPost = getHttpPost(url.toString(), accessToken);
			httpResponse = httpClient.execute(httpPost);

			printHttpResponse(httpResponse);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consumeQuietly(httpResponse);
		}
	}

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/jobs/{jobId}/status
	 */
	public String testGetJobStatus()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		try
		{
			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/jobs/1460/status");
			HttpGet httpGet = getHttpGet(url.toString(), accessToken);

			httpResponse = httpClient.execute(httpGet);

			return printHttpResponse(httpResponse);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consumeQuietly(httpResponse);
		}
		return null;
	}

	/**
	 * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/jobs/{jobIds}/targetFiles
	 */
	public void testGetJobExportFiles()
	{
		CloseableHttpClient httpClient = getHttpClient();
		HttpResponse httpResponse = null;
		try
		{
			StringBuffer url = new StringBuffer();
			url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/Allie/jobs/1460/targetFiles");
			HttpGet httpGet = getHttpGet(url.toString(), accessToken);
			httpResponse = httpClient.execute(httpGet);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			System.out.println("Status code: " + statusCode + "\r\n");

			String res = httpResponse.getStatusLine().toString();
			System.out.println("Status line: " + res + "\r\n");

			InputStream is = httpResponse.getEntity().getContent();

			BufferedInputStream inputStream = null;
			File file = new File("E:\\down\\test_abc.zip");
			if (file.exists())
			{
				file.delete();
			}
			try
			{
				inputStream = new BufferedInputStream(is);
				byte[] fileBytes = new byte[MAX_SEND_SIZE];
				int count = inputStream.read(fileBytes);

				while (count != -1 && count == MAX_SEND_SIZE)
				{
					FileUtil.writeFile(file, fileBytes, true);
					fileBytes = new byte[MAX_SEND_SIZE];
					count = inputStream.read(fileBytes);
				}

				byte[] fileBytes2 = new byte[count];
				for (int i = 0; i < count; i++)
				{
					fileBytes2[i] = fileBytes[i];
				}
				FileUtil.writeFile(file, fileBytes2, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (inputStream != null)
				{
					inputStream.close();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consumeQuietly(httpResponse);
		}
	}

	public static void main(String[] args)
	{
		JobResourceTester tester = null;
		try
		{
			LoginResourceTester loginTester = new LoginResourceTester();
			String accessToken = loginTester
					.testLogin("allieadmin", "password");
			System.out.println("access token: " + accessToken);

			tester = new JobResourceTester(accessToken);
			tester.testGetUniqueJobName();
			// tester.testUploadSourceFile();
			// tester.testCreateJob();
			// tester.testGetJobStatus();
			// tester.testGetJobExportFiles();
		}
		finally
		{
			tester.shutdownHttpClient();
		}
	}
}
