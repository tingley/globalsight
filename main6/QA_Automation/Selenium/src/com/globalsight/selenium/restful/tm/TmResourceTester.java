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

package com.globalsight.selenium.restful.tm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

import com.globalsight.selenium.restful.RestfulApiTestHelper;
import com.globalsight.selenium.restful.URLEncoder;

public class TmResourceTester extends RestfulApiTestHelper
{
    private String userName = null;
    private String password = null;

    public TmResourceTester(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    /**
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms
     */
    public void testGetTms()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms";

            HttpGet httpGet = getHttpGet(url, userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus
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
            url.append("http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/3/tus");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            url.append("&sourceSegment=").append(URLEncoder.encode(sourceSegment));
            url.append("&targetLocale=").append(targetLocale);
            url.append("&targetSegment=").append(URLEncoder.encode(targetSegment));
            // optional params
            url.append("&sid=").append(sid);
            url.append("&escapeString=").append(escapeString);

            HttpPost httpPost = getHttpPost(url.toString(), userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus/{id}
     */
    public String testGetTu()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            long id = 996201;
            String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/3/tus/" + id;

            HttpGet httpGet = getHttpGet(url, userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus
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
            url.append("http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/3/tus");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            // optional params
            url.append("&startId=").append(startId);
            url.append("&offset=").append(offset);
            url.append("&targetLocale=").append(targetLocale);

            HttpGet httpGet = getHttpGet(url.toString(), userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus
     */
    public void testEditTus(String tuXml)
    {
        String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/3/tus";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpPut httpPut = getHttpPut(url, userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus/{ids}
     */
    public void testDeleteTus()
    {
        String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/3/tus/996201";
//        String url = "http://localhost:8080/globalsight/restfulServices/companies/York/tms/5/tus/1,2-5,6-10";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpDelete httpDelete = getHttpDelete(url, userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/upload
     */
    public void testUploadTmxFile()
    {
        String strUrl = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/818/upload";

        File file = new File("E:\\Erica\\Personal\\My Note\\TestFiles\\tm_100_InContext_Fuzzy_NoMatch.xml");

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

                HttpPost httpPost = getHttpPost(strUrl, userName, password);
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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/import
     */
    public void testImportTmxFile()
    {
        String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/818/import";
        //optional params
        url += "?syncMode=merge";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpPost httpPost = getHttpPost(url, userName, password);

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
            String projectNames = ""; //"project_name_01, project_name_02";
            String exportedFileName = "abc_yyyymmdd";

            StringBuffer url = new StringBuffer();
            url.append("http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/818/export");
            // required params
            url.append("?startDate=").append(startDate);
            url.append("&exportFormat=").append(exportFormat);
            // optional params
            url.append("&languages=").append(languages);
            url.append("&finishDate=").append(finishDate);
            url.append("&projectNames=").append(projectNames);
            url.append("&exportedFileName=").append(exportedFileName);

            HttpGet httpGet = getHttpGet(url.toString(), userName, password);

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

    public void testGetTmExportFile(String identifyKey)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = "http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/818/export/" + identifyKey;

            HttpGet httpGet = getHttpGet(url, userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmIds}/fullTextSearch
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
            url.append("http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/818,3/fullTextSearch");
            // required params
            url.append("?sourceLocale=").append(sourceLocale);
            url.append("&targetLocale=").append(targetLocale);
            url.append("&searchText=").append(URLEncoder.encode(searchText));
            // optional query params
            url.append("&creationStartDate=").append(creationStartDate);
            url.append("&creationFinishDate=").append(creationFinishDate);
            url.append("&modifyStartDate=").append(modifyStartDate);
            url.append("&modifyFinishDate=").append(modifyFinishDate);

            HttpGet httpGet = getHttpGet(url.toString(), userName, password);

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
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/leverage?searchText=searchText&tmProfileName=tmpName&sourceLocale=en_US
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
            String tmProfileName = "tmp";
            String sourceLocale = "en_US";
            String escapeString = "true"; // or "false"

            StringBuffer url = new StringBuffer();
            url.append("http://10.10.213.20:8080/globalsight/restfulServices/companies/85/tms/leverage");
            // required params
            url.append("?searchText=").append(URLEncoder.encode(searchText));
            url.append("&tmProfileName=").append(tmProfileName);
            url.append("&sourceLocale=").append(sourceLocale);
            // optional query params
            url.append("&escapeString=").append(escapeString);

            HttpGet httpGet = getHttpGet(url.toString(), userName, password);

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
        TmResourceTester tester = new TmResourceTester("85admin", "password");

        try
        {
//            tester.testGetTms();

//            tester.testCreateTu();

//            tester.testGetTu();
//
//            String tuXml = tester.testGetTus();
//            tester.testEditTus(tuXml);
//
//            tester.testDeleteTus();
//
//            tester.testUploadTmxFile();
//            tester.testImportTmxFile();
//
            String identifyKey = "595777759";
            identifyKey = tester.testExportTM();
            if (identifyKey != null)
            {
                tester.testGetTmExportFile(identifyKey);
            }
//
//            tester.testFullTextSearch();
//
//            tester.testLeverageSegment();
        }
        finally
        {
            tester.shutdownHttpClient();
        }
    }
}
