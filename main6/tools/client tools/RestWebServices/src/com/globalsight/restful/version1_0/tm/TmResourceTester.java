/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.restful.version1_0.tm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;
import com.globalsight.restful.login.LoginResourceTester;
import com.globalsight.restful.util.FileUtil;
import com.globalsight.restful.util.URLEncoder;

public class TmResourceTester extends RestfulApiTestHelper
{
    private String accessToken = null;

    public TmResourceTester(String accessToken)
    {
        this.accessToken = accessToken;
    }

    /**
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms
     */
    public void testGetTms()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms";

            HttpGet httpGet = getHttpGet(url, accessToken);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/tus
     */
    public void testCreateTu()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String sourceLocale = "en_US";
            String sourceSegment = "source in English";
            String targetLocale = "fr_FR";
            String targetSegment = "target in French";
            String sid = "sidValue";
            String escapeString = "true"; // or "false"

            StringBuffer url = new StringBuffer();
            url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            url.append("&sourceSegment=").append(URLEncoder.encode(sourceSegment));
            url.append("&targetLocale=").append(targetLocale);
            url.append("&targetSegment=").append(URLEncoder.encode(targetSegment));
            // optional params
            url.append("&sid=").append(sid);
            url.append("&escapeString=").append(escapeString);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/tus/{id}
     */
    public String testGetTu()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            long id = 912902;
            String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus/" + id;

            HttpGet httpGet = getHttpGet(url, accessToken);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/tus
     */
    public String testGetTus()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String sourceLocale = "en_US";
            String targetLocale = "fr_FR";
            int startId = 0; // default 0
            int offset = 1; // default 1

            StringBuffer url = new StringBuffer();
            url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            // optional params
            url.append("&startId=").append(startId);
            url.append("&offset=").append(offset);
            url.append("&targetLocale=").append(targetLocale);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/tus
     */
    public void testEditTus(String tuXml)
    {
        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpPut httpPut = getHttpPut(url, accessToken);

            // The TUs that will be edited are from the "tuXml".
            StringEntity reqEntity = new StringEntity(tuXml, "UTF-8");
            reqEntity.setContentType("application/xml; charset=UTF-8");
            httpPut.setEntity(reqEntity);

            httpResponse = httpClient.execute(httpPut);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/tus/{ids}
     */
    public void testDeleteTus()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus/1,2,3";
//        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/tus/1,2-5,6-10";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpDelete httpDelete = getHttpDelete(url, accessToken);

            httpResponse = httpClient.execute(httpDelete);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/upload
     */
    public void testUploadTmxFile()
    {
        String strUrl = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/upload";

        File file = new File("D:\\_Middle Files\\TM Data\\tm_export_4.xml");

        CloseableHttpClient httpClient = getHttpClient();
        try
        {
            // if tm file is large, we had better upload multiple times, 5M every time.
            int len = (int) file.length();
            BufferedInputStream inputStream = null;
            ArrayList<byte[]> fileByteList = new ArrayList<byte[]>();
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

            // Uploads all parts of files.
            for (int i = 0; i < fileByteList.size(); i++)
            {
                ByteArrayBody byteBody = new ByteArrayBody(fileByteList.get(i), file.getName());

                // if file is not big, can use FileBody to upload one time
                @SuppressWarnings("unused")
                FileBody fileBody = new FileBody(file);

                HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        .addPart("anyNameIsOkay", byteBody).build();

                HttpPost httpPost = getHttpPost(strUrl, accessToken);
                httpPost.setEntity(multiPartEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/import
     */
    public void testImportTmxFile()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/5/import";
        //optional params
        url += "?syncMode=merge";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpPost httpPost = getHttpPost(url, accessToken);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/export
     */
    public String testExportTM()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String startDate = "19991223"; // in "yyyyMMdd" format
            String exportFormat = "GMX"; // or "TMX1.4b"

            String languages = "de_DE,fr_FR";
            String finishDate = "20161231"; // in "yyyyMMdd" format
            String projectNames = "project_name_01, project_name_02";
            String exportedFileName = "abc_yyyymmdd.xml";

            StringBuffer url = new StringBuffer();
            url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/28/export");
            // required params
            url.append("?startDate=").append(startDate);
            url.append("&exportFormat=").append(exportFormat);
            // optional params
            url.append("&languages=").append(languages);
            url.append("&finishDate=").append(finishDate);
            url.append("&projectNames=").append(projectNames);
            url.append("&exportedFileName=").append(exportedFileName);

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
        return null;
    }

    /**
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmId}/export/{identifyKey}
     */
    public void testGetTmExportFile(String identifyKey)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/4/export/" + identifyKey;

            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpClient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("Status code: " + statusCode + "\r\n");

            String res = httpResponse.getStatusLine().toString();
            System.out.println("Status line: " + res + "\r\n");

            InputStream is = httpResponse.getEntity().getContent();
            
            BufferedInputStream inputStream = null;
            File file = new File("C:\\tmx.xml");
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

    /**
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/{tmIds}/fullTextSearch
     * ?searchText=searchText&sourceLocale=en_US&targetLocale=fr_FR
     * 
     * A sample returned xml:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <segments>
     *     <sourceLocale>English (United States) [en_US]</sourceLocale>
     *     <targetLocale>French (France) [fr_FR]</targetLocale>
     *     <segment>
     *         <sourceSegment><bpt i="1" type="bold" x="1">&lt;B&gt;</bpt><bpt internal="yes" i="2" x="2"/>Welocalize<ept i="2"/> - About Us<ept i="1">&lt;/B&gt;</ept></sourceSegment>
     *         <targetSegment><bpt i="1" type="bold" x="1">&lt;B&gt;</bpt><bpt internal="yes" i="2" x="2"/>Welocalize<ept i="2"/> - About Us<ept i="1">&lt;/B&gt;</ept></targetSegment>
     *         <sid>N/A</sid>
     *         <tuId>912801</tuId>
     *         <tmName>storage-tm</tmName>
     *     </segment>
     *     <segment>
     *     ...
     *     </segment>
     * </segments>
     * 
     */
    public void testFullTextSearch()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String sourceLocale = "en_US";
            String targetLocale = "fr_FR";
            String searchText = "Welocalize";

            String creationStartDate = "20160101";
            String creationFinishDate = "20161231";
            String modifyStartDate =  "20160101";
            String modifyFinishDate = "20161231";

            StringBuffer url = new StringBuffer();
            url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/1,5/fullTextSearch");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            url.append("&targetLocale=").append(targetLocale);
            url.append("&searchText=").append(URLEncoder.encode(searchText));
            // optional query params
            url.append("&creationStartDate=").append(creationStartDate);
            url.append("&creationFinishDate=").append(creationFinishDate);
            url.append("&modifyStartDate=").append(modifyStartDate);
            url.append("&modifyFinishDate=").append(modifyFinishDate);

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
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyID}/tms/leverage?searchText=searchText&tmProfileName=tmpName&sourceLocale=en_US
     * 
     * A sample returned xml:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <entries>
     *     <entry>
     *         <tm id="5">tm_100_InContext_Fuzzy_NoMatch</tm>
     *         <percentage>98%</percentage>
     *         <source>
     *             <locale>en_US</locale>
     *             <segment><bpt i="1" type="bold" x="1" erasable="no" movable="yes"></bpt>Welocalize - About Us<ept i="1"></ept></segment>
     *         </source>
     *         <target>
     *             <locale>fr_FR</locale>
     *             <segment><bpt i="1" type="bold" x="1" erasable="no" movable="yes"></bpt>Welocalize - qui sommes-nous<ept i="1"></ept></segment>
     *         </target>
     *     </entry>
     *     <entry>
     *     ...
     *     </entry>
     * </entries>
     */
    public void testLeverageSegment()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String searchText = "Welocalize - about us";
            String tmProfileName = "tmprofile_1";
            String sourceLocale = "en_US";
            String targetLocale = "fr_FR";
            String escapeString = "true"; // or "false"

            StringBuffer url = new StringBuffer();
            url.append("http://localhost:8080/globalsight/restfulServices/1.0/companies/1000/tms/leverage");
            // required params
            url.append("?searchText=").append(URLEncoder.encode(searchText));
            url.append("&tmProfileName=").append(tmProfileName);
            url.append("&sourceLocale=").append(sourceLocale);
            // optional query params
            url.append("&targetLocale=").append(targetLocale);
            url.append("&escapeString=").append(escapeString);

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

    public static void main(String[] args)
    {
        TmResourceTester tester = null;
        try
        {
            LoginResourceTester loginTester = new LoginResourceTester();
            String accessToken = loginTester.testLogin("york", "password");
            System.out.println("access token: " + accessToken);

            tester = new TmResourceTester(accessToken);
            tester.testGetTms();

            tester.testCreateTu();

            tester.testGetTu();

            String tuXml = tester.testGetTus();
            tester.testEditTus(tuXml);

            tester.testDeleteTus();

            tester.testUploadTmxFile();
            tester.testImportTmxFile();

            String identifyKey = "199098841";
            identifyKey = tester.testExportTM();
            if (identifyKey != null)
            {
                tester.testGetTmExportFile(identifyKey);
            }

            tester.testFullTextSearch();

            tester.testLeverageSegment();
        }
        finally
        {
            tester.shutdownHttpClient();
        }
    }
}
