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
package com.globalsight.util;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.jobsAutoArchiver.Constants;

public class ConfigUtil
{
    public static ConfigBO getConfigBO()
    {
        String hostName;
        int port = 80;
        String userName;
        String password;
        int intervalTimeForArchive;
        int intervalTime;
        boolean isUseHTTPS = false;

        try
        {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new File(Constants.CONFIG_FILE_NAME));
            Node serverNode = document.selectSingleNode("/Configuration/server");

            hostName = serverNode.selectSingleNode("host").getText();
            port = Integer.valueOf(serverNode.selectSingleNode("port").getText());
            userName = serverNode.selectSingleNode("username").getText();
            password = serverNode.selectSingleNode("password").getText();
            isUseHTTPS = Boolean.valueOf(serverNode.selectSingleNode("https").getText());

            intervalTimeForArchive=Integer.valueOf(document.selectSingleNode("//intervalTimeForArchive").getText());
            intervalTime=Integer.valueOf(document.selectSingleNode("//intervalTime").getText());

            ConfigBO config = new ConfigBO(hostName, port, userName, password,
                    intervalTimeForArchive, intervalTime, isUseHTTPS);
            return config;
        }
        catch (DocumentException e)
        {
            LogUtil.info("Fail to read configuration info", e);
        }
        
        return null;
    }

    public static void main(String[] args)
    {
        getConfigBO();
    }
}
