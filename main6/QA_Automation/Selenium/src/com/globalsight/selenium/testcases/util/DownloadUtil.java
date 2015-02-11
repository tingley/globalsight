/**
 *  Copyright 2009, 2012 Welocalize, Inc. 
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

package com.globalsight.selenium.testcases.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;

/**
 * @author Vincent
 *
 */
public class DownloadUtil
{
    public static String download(String jobName) throws Exception
    {
        String wsdlUrl = ConfigUtil.getConfigData("serverUrl")
                + "/globalsight/services/AmbassadorWebService?wsdl";

        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
        String token = service.login(
                ConfigUtil.getConfigData("adminName"),
                ConfigUtil.getConfigData("adminPassword"));
        String fileXml = null;
        
        String waitTimeStr = ConfigUtil.getConfigData("middleWait");
        String checkTimesStr = ConfigUtil.getConfigData("checkTimes");
        int checkTimes = 30, times = 0;
        long waitTime = 60000;
        
        try {
            checkTimes = Integer.parseInt(checkTimesStr);
            waitTime = Long.parseLong(waitTimeStr);
        } catch (Exception e) {
            checkTimes = 30;
            waitTime = 60000;
        }
        
        while (times < checkTimes)
        {
            fileXml = service.getJobExportFiles(token, jobName);
            if (fileXml != null)
                break;
            else
            {
                Thread.sleep(waitTime);
                times++;
            }
        }

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new StringReader(fileXml));
        Element rootElement = document.getRootElement();
        String root = rootElement.elementText("root");
        ArrayList<Element> paths = (ArrayList<Element>)rootElement.elements("paths");
        for (Element element : paths)
        {
            String filePath = element.getText();
            
            File outputFile = downloadHttp(root + File.separator + filePath);
        }
        return null;
    }
    
    private static File downloadHttp(String downloadURL) throws Exception
    {
        String urlDecode = URLDecoder.decode(downloadURL, "UTF-8").replaceAll("\\\\", "/");
        urlDecode = urlDecode.replace(" ", "%20");
        
        URL url = new URL(urlDecode);
        HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
        hurl.connect();
        InputStream is = hurl.getInputStream();
        
        urlDecode = urlDecode.replace("%20", " ");
        String sourcePath = ConfigUtil.getConfigData("Base_Path");
        sourcePath = sourcePath.substring(2);
        sourcePath = sourcePath.replaceAll("\\\\", "/");
        int index = urlDecode.lastIndexOf(sourcePath);
        String jobFilePath = urlDecode.substring(index + sourcePath.length());
        String resultPath = ConfigUtil.getConfigData("Base_Path_Result") + jobFilePath;
        
        File file = new File(resultPath);
        saveFile(is, file);
        
        return file;
    }
    
    private static void saveFile(InputStream is, File file) throws IOException,
            FileNotFoundException
    {
        file.getParentFile().mkdirs();
        file.createNewFile();
        BufferedInputStream bin = new BufferedInputStream(is);
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buf = new byte[4096];
        int len = -1;
        while ((len = bin.read(buf)) != -1) {
            bout.write(buf, 0, len);
        }
        bout.close();
        bin.close();
        if (file.length() == 0)
        {
            file.delete();
        }
    }
}
