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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.config.properties.Resource;
import com.plug.Version_7_1_6_0.DbServer;
import com.plug.Version_7_1_6_0.FilterServer;
import com.plug.Version_7_1_6_0.InitialFilter;
import com.plug.Version_7_1_6_0.InitialFilterConfiguration;
import com.plug.Version_7_1_6_0.InitialHtmlFilter;
import com.plug.Version_7_1_6_0.InitialJavaPropertiesFilter;
import com.plug.Version_7_1_6_0.InitialJavaScriptFilter;
import com.plug.Version_7_1_6_0.InitialMSDocFilter;
import com.plug.Version_7_1_6_0.InitialXmlRuleFilter;
import com.plug.Version_7_1_6_0.UpdateFileProfile;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

public class Plug_7_1_6_0 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_6_0.class);

    private UI ui = UIFactory.getUI();
    private DbServer dbServer = new DbServer();

    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
            + "default/deploy";
    private static final String WEB_XML_PATH = DEPLOY_PATH
            + "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml";
    public final static String AXIS_WS4EE_JAR_PATH = DEPLOY_PATH
            + "/jboss-ws4ee.sar/axis-ws4ee.jar";
    private static final String ENVOY_PATH = DEPLOY_PATH
            + "/globalsight.ear/lib/classes/properties/envoy.properties";

    @Override
    public void run()
    {
        updateFilter();
        updateXml();
        updateHtmlFilter();
        removeAxisWs4eeJar();
        initRemoteIpFilter();
        editEnvoyProperties();
    }

    private void editEnvoyProperties()
    {
        log.debug("Editing envoy.properties...");
        File file = new File(ServerUtil.getPath() + ENVOY_PATH);
        String ips = PropertyUtil.get(file, "remote.ipAddressFilterWSvc");
        if (ips != null)
        {
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String s = in.readLine();
                StringBuilder content = new StringBuilder();

                while (s != null)
                {
                    content.append(s).append(FileUtil.lineSeparator);
                    s = in.readLine();
                }
                in.close();
                
                int firstIndex = content.indexOf("# Remote ip address filtering for webservices.");
                int endIndex = content.indexOf("# Determines whether MT translated segments should get committed to TM even if");
                if (firstIndex > 0 && endIndex > firstIndex)
                {
                    content.delete(firstIndex, endIndex);
                    FileWriter out = new FileWriter(file);
                    out.write(content.toString());
                    out.flush();
                    out.close();
                    
                }
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        log.debug("Finished");
    }
    
    private void initRemoteIpFilter()
    {
        log.info("Updating REMOTE_IP table...");
        DbServer server = new DbServer();
        String sql = "select * from REMOTE_IP";
        try
        {
            if (server.query(sql).size() == 0)
            {
                List<String> ips = getRemoteIps();
                if (ips.size() > 0)
                {
                    StringBuilder sql2 = new StringBuilder("insert into REMOTE_IP(ip) values");
                    for (String ip : ips)
                    {
                        sql2.append("('").append(ip).append("'),");
                    }
                    
                    sql2.deleteCharAt(sql2.length() - 1);
                    server.insert(sql2.toString());
                }
            }
            
            log.info("Successful");
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
            log.info("failed");
        }
    }

    private List<String> getRemoteIps()
    {
        List<String> ipList = new ArrayList<String>();
        File file = new File(ServerUtil.getPath() + ENVOY_PATH);
        String ips = PropertyUtil.get(file, "remote.ipAddressFilterWSvc");
        if (ips != null)
        {
            for (String ip : ips.split("\\,"))
            {
                ipList.add(ip);
            }
        }
        
        return ipList;
    }
    
    private String getAjaxServlet()
    {
        return FileUtil.lineSeparator
                + "<!-- start ajax service configuration-->"
                + FileUtil.lineSeparator
                + "<servlet>"
                + FileUtil.lineSeparator
                + "   <servlet-name>AjaxService</servlet-name>"
                + FileUtil.lineSeparator
                + "   <servlet-class>com.globalsight.everest.util.ajax.AjaxService</servlet-class>"
                + FileUtil.lineSeparator + "</servlet>"
                + FileUtil.lineSeparator + FileUtil.lineSeparator
                + "<servlet-mapping>" + FileUtil.lineSeparator
                + "   <servlet-name>AjaxService</servlet-name>"
                + FileUtil.lineSeparator
                + "   <url-pattern>/AjaxService</url-pattern>"
                + FileUtil.lineSeparator + "</servlet-mapping>"
                + FileUtil.lineSeparator
                + "<!-- end ajax service configuration-->"
                + FileUtil.lineSeparator + FileUtil.lineSeparator;
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

            if (content.indexOf("<!-- start ajax service configuration-->") < 0)
            {
                int index = content
                        .indexOf("end InetSoft Reports Configuration");
                if (index < 0)
                {
                    log.error("Can not find where to insert ajax service");
                    return;
                }

                content.insert(index
                        + "<!-- end InetSoft Reports Configuration -->"
                                .length(), getAjaxServlet());
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

    private void updateFilter()
    {
        log.debug("Init database for filters...");

        ArrayList<InitialFilter> filters = new ArrayList<InitialFilter>();
        filters.add(new InitialFilterConfiguration());
        filters.add(new InitialJavaPropertiesFilter());
        filters.add(new InitialJavaScriptFilter());
        filters.add(new InitialMSDocFilter());
        filters.add(new InitialXmlRuleFilter());

        for (int i = 0; i < filters.size(); i++)
        {
            InitialFilter filter = filters.get(i);
            try
            {
                filter.insert();
            }
            catch (SQLException e)
            {
                log.info("Failed");
                log.debug(e);
                ui.confirmContinue(MessageFormat.format(Resource
                        .get("msg.updateDatabase"), e.getMessage()));
            }
        }

        UpdateFileProfile profile = new UpdateFileProfile();
        try
        {
            log.info("Updating file profile...");
            profile.updateFilterIds(filters);
            log.info("Successful");
        }
        catch (SQLException e)
        {
            log.info("Failed");
            log.debug(e);
            ui.confirmContinue(MessageFormat.format(Resource
                    .get("msg.updateDatabase"), e.getMessage()));
        }
    }

    private void updateHtmlFilter()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "Tags.properties".equalsIgnoreCase(pathname.getName());
            }
        });

        for (File file : tagProperties)
        {
            File parentFile = file.getParentFile();
            String path = parentFile.getPath().replace("\\", "/");
            if (!path.endsWith("classes/properties"))
            {

                String name = parentFile.getName();
                long id = dbServer.getCompanyIdByName(name);
                if (id > -1)
                {
                    log.info("Init table html_filter for company: " + name
                            + "...");
                    InitialHtmlFilter filter = new InitialHtmlFilter(id, file);
                    try
                    {
                        FilterServer.insertFilterConfigur("HTML Filter", "|1|",
                                "html_filter", "The filter for HTML file.", id);
                        filter.insert();
                    }
                    catch (SQLException e)
                    {
                        log.info("Failed");
                        log.debug(e);
                        ui.confirmContinue(MessageFormat.format(Resource
                                .get("msg.updateDatabase"), e.getMessage()));
                    }
                    log.info("Successful");
                }
            }
        }
    }

    private void removeAxisWs4eeJar()
    {
        log.debug("Starting to deleting axis-ws4ee.jar");
        File jar = new File(ServerUtil.getPath() + AXIS_WS4EE_JAR_PATH);
        if (jar.exists())
        {
            if (!jar.delete())
            {
                log.warn("Failed to delete axis-ws4ee.jar");
            }
        }
        log.debug("Finished");
    }
}
