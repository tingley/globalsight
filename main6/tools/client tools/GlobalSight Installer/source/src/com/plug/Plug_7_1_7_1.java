/**
 *  Copyright 2009 Welocalize, Inc. 
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
package com.plug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_7_1_7_1 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_7_1.class);

    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
        + "default/deploy";
    private static final String WEB_XML_PATH = DEPLOY_PATH
            + "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml";
    private static final String SERVER_XML_PATH = "/jboss/server.xml";

    @Override
    public void run()
    {
        updateXml();
        updateServerXml();
    }

    private void updateXml()
    {
        File webXml = new File(ServerUtil.getPath() + WEB_XML_PATH);
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new FileReader(webXml));
            String s = in.readLine();
            StringBuilder content = new StringBuilder();

            while (s != null)
            {
                content.append(s).append(FileUtil.lineSeparator);
                s = in.readLine();
            }

            if (content.indexOf("ImageFileServlet2") < 0)
            {

                int i = content.indexOf("ImageFileServlet");
                int a = content.lastIndexOf("<servlet>", i);
                int b = content.indexOf("<servlet>", i);
                
                String cxe = content.substring(a, b);
                cxe = cxe.replace("cxedocs<", "cxedocs2<");
                cxe = cxe.replace("ImageFileServlet<", "ImageFileServlet2<");

                int index = content.indexOf("<servlet>", i);
                if (index < 0)
                {
                    return;
                }

                content.insert(index, cxe);
            }
            
            if (content.indexOf("<url-pattern>/cxedocs2/*</url-pattern>") < 0)
            {

                int i = content.indexOf("<url-pattern>/cxedocs/*</url-pattern>");
                int a = content.lastIndexOf("<servlet-mapping>", i);
                int b = content.indexOf("<servlet-mapping>", i);
                
                String cxeMapping = content.substring(a, b);
                cxeMapping = cxeMapping.replace("cxedocs", "cxedocs2");

                int index = content.indexOf("<servlet-mapping>", i);
                if (index < 0)
                {
                    return;
                }

                content.insert(index, cxeMapping);
            }

            FileWriter out = new FileWriter(webXml, false);
            out.write(content.toString());
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void updateServerXml()
    {
        File webXml = new File(ServerUtil.getPath() + SERVER_XML_PATH);
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new FileReader(webXml));
            String s = in.readLine();
            StringBuilder content = new StringBuilder();

            while (s != null)
            {
                content.append(s).append(FileUtil.lineSeparator);
                s = in.readLine();
            }

            if (content.indexOf("maxHttpHeaderSize") < 0)
            {
                int index = content.indexOf("maxThreads=\"150\"");
                if (index > 0)
                {
                    content.insert(index, "maxHttpHeaderSize=\"8192\" ");
                }
            }

            FileWriter out = new FileWriter(webXml, false);
            out.write(content.toString());
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
