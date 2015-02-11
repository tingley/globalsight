/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.welocalize.dispatcherMW.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.globalsight.dispatcher.bo.AppConstants;

/**
 * DispatcherMW client code, which is used for upload XLF file, and download the translated file.
 * 
 * The DispatcherMW Upload URL:
 * http://localhost:8888/dispatcherMW/translateXLF/upload/?securityCode={securityCode}
 * 
 * The DispatcherMW Check Job Status URL:
 * http://localhost:8888/dispatcherMW/translateXLF/checkStatus/?jobID={jobID}
 * 
 * The DispatcherMW Download URL:
 * http://localhost:8888/dispatcherMW/translateXLF/download/?jobID={jobID}&securityCode={securityCode}
 * 
 * @author Joey
 * @Date 2014-03-14
 *
 */
public class Main implements AppConstants
{
    static String basicURL = "http://localhost:8888/dispatcherMW/translateXLF/";
    
    static long waitTime = 1000 * 60 * 10;
    static final String TYPE_TRANSLATE = "translate";
    static final String TYPE_UPLOAD = "upload";
    static final String TYPE_CHECK_STATUS = "checkStatus";
    static final String TYPE_DOWNLOAD = "download";

    public static void main(String[] args) throws InterruptedException, IOException
    {
        if (args.length >= 3)
        {
            String type = args[0];
            if (TYPE_TRANSLATE.equalsIgnoreCase(type))
            {
                setbasicURl(args[1]);
                doJob(args[2], args[3]);
                return;
            }
            else if (TYPE_CHECK_STATUS.equalsIgnoreCase(type))
            {
                setbasicURl(args[1]);
                checkJobStaus(args[2]);
                return;
            }
            else if (TYPE_DOWNLOAD.equalsIgnoreCase(type))
            {
                setbasicURl(args[1]);
                downloadJob(args[2], args[3]);
                return;
            }
        }
        else if (args.length == 1)
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            String type = properties.getProperty("type");
            setbasicURl(properties.getProperty("URL"));
            String securityCode = properties.getProperty(JSONPN_SECURITY_CODE);
            String filePath = properties.getProperty("filePath");
            String jobID = properties.getProperty(JSONPN_JOBID);
            setWaitTime(properties.getProperty("waitTime"));
            if (TYPE_TRANSLATE.equalsIgnoreCase(type))
            {
                doJob(securityCode, filePath);
                return;
            }
            else if (TYPE_CHECK_STATUS.equalsIgnoreCase(type))
            {
                String status = checkJobStaus(jobID);
                System.out.println("The Status of Job:" + jobID + " is " + status + ". ");
                return;
            }
            else if (TYPE_DOWNLOAD.equalsIgnoreCase(type))
            {
                downloadJob(jobID, securityCode);
                System.out.println("Download Job:" + jobID);
                return;
            }
        }

        // Print Help Message
        StringBuffer msg = new StringBuffer();
        msg.append("The Input is incorrect.").append("\n");
        msg.append("If you want to translate the XLF file, use this command:").append("\n");
        msg.append(" translate $URL $securityCode $filePath").append("\n");
        msg.append("If you only want to check job status, use this command:").append("\n");
        msg.append(" checkStatus $URL $jobID").append("\n");
        msg.append("If you only want to download the job file, use this command:").append("\n");
        msg.append(" download $URL $jobID $securityCode").append("\n");
        System.out.println(msg.toString());
    }

    // Do Machine Translation, include upload/download XLF file.
    public static void doJob(String p_securityCode, String p_filePath) throws InterruptedException
    {
        String jobIdStr = uploadXLF(p_filePath, p_securityCode);
        if (jobIdStr == null || jobIdStr.length() != 10)
        {
            System.out.println("Upload File Error!");
            return;
        }
        do
        {
            String status = checkJobStaus(jobIdStr);
            System.out.println("The Status of job:" + jobIdStr + " is " + status);

            if (STATUS_QUEUED.equals(status) || STATUS_RUNNING.equals(status))
            {
                Thread.sleep(waitTime);
            }
            else if (STATUS_COMPLETED.equals(status))
            {
                downloadJob(jobIdStr, p_securityCode);
                return;
            }
            else
            {
                System.out.println("The job status is error. Break!");
                return;
            }
        } while (true);
    }

    // Upload XLF file to DispatcherMW for Machine Translation
    public static String uploadXLF(String p_fileName, String p_securityCode)
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(getFunctinURL(TYPE_UPLOAD, p_securityCode, null));

        try
        {
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("fileName", new StringBody(p_fileName, ContentType.create("text/plain", Consts.UTF_8)))
                    .addPart("file", new FileBody(new File(p_fileName))).build();
            httpPost.setEntity(reqEntity);

            // send the http request and get the http response
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            String jobID = null;
            String msg = EntityUtils.toString(resEntity);
            if (msg.contains("\"jobID\":\""))
            {
                int startIndex = msg.indexOf("\"jobID\":\"");
                jobID = msg.substring(startIndex + 9, msg.indexOf(",", startIndex) - 1);
            }
            System.out.println("Create Job: " + jobID + ", wtih file:" + p_fileName);
            return jobID;
        }
        catch (Exception e)
        {
            System.out.println("testHTTPClient error. " + e);
        }
        finally
        {
            httpPost.releaseConnection();
        }

        return null;
    }

    // Check Job Status in DispatcherMW
    public static String checkJobStaus(String p_jobId)
    {
        String url = getFunctinURL(TYPE_CHECK_STATUS, null, p_jobId);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try
        {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity resEntity = response.getEntity();
            String msg = EntityUtils.toString(resEntity);
            if (msg.startsWith("{\"status\":\""))
            {
                return msg.substring(11, msg.indexOf(",") - 1);
            }
            else
            {
                System.out.println(msg);
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            httpget.releaseConnection();
        }

        return "";
    }

    // Download Translated XLF file from DispatcherMW
    public static void downloadJob(String p_jobId, String p_securityCode)
    {
        File parent = new File("target/" + p_jobId);
        String url = getFunctinURL(TYPE_DOWNLOAD, p_securityCode, p_jobId);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try
        {
            HttpResponse response = httpClient.execute(httpget);
            String fileName = response.getHeaders("Content-Disposition")[0].getValue().split("\"")[1];
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                if (!parent.exists())
                    parent.mkdirs();
                FileOutputStream fos = new FileOutputStream(parent.getAbsolutePath() + "/" + fileName);
                entity.writeTo(fos);
                fos.close();
            }

            System.out.println("Download file:" + parent.getAbsolutePath() + "/" + fileName);
        }
        catch (Exception e)
        {
            System.out.println("Download XLF file error:\n" + e);
        }
        finally
        {
            httpget.releaseConnection();
        }
    }

    private static void setbasicURl(String p_url)
    {
        if (p_url == null || !p_url.startsWith("http"))
            return;

        if (!p_url.endsWith("/"))
            p_url = p_url + "/";

        if (!p_url.endsWith("translateXLF/"))
            p_url = p_url + "translateXLF/";

        basicURL = p_url;
    }

    private static void setWaitTime(String p_min)
    {
        if (p_min == null || p_min.trim().length() == 0)
            return;

        try
        {
            waitTime = Long.valueOf(p_min) * 1000 * 60;
        }
        catch (Exception e)
        {

        }
    }
    
    private static String getFunctinURL(String p_type, String p_securityCode, String p_jobId)
    {
        String result = basicURL;
        if (TYPE_UPLOAD.equals(p_type))
        {
            result = result + "upload?securityCode=" + p_securityCode;
        }
        else if (TYPE_CHECK_STATUS.equals(p_type))
        {
            result = result + "checkStatus?jobID=" + p_jobId;
        }
        else if (TYPE_DOWNLOAD.equals(p_type))
        {
            result = result + "download?jobID=" + p_jobId + "&securityCode=" + p_securityCode;
        }

        return result;
    }
}
