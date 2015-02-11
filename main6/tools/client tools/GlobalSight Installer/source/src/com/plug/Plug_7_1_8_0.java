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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.Main;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.ServerUtil;
import com.util.UpgradeUtil;

public class Plug_7_1_8_0 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_8_0.class);

    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
            + "default/deploy";
    private static final String WEB_TEMPLATE_PATH = DEPLOY_PATH
            + "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml.template";
    private static final String PROPERTIES_PATH = DEPLOY_PATH
            + "/globalsight.ear/lib/classes/properties/";

    @Override
    public void run()
    {
        parseTemplates();
        removeInstallProperties();
        addProperites();
    }
    
    private void parseTemplates()
    {
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + WEB_TEMPLATE_PATH));
        templates.add(new File(ServerUtil.getPath()
                + "/jboss/mail-service.xml.template"));
        Main.getInstallUtil().parseTemplates(templates);
    }

    public void removeInstallProperties()
    {
        List<String> paths = new ArrayList<String>();
        paths.add("installDefaultValues.properties");
        paths.add("installDefaultValuesNoUI.properties");
        paths.add("installDisplay.properties");
        paths.add("installDisplay_en_US.properties");
        paths.add("installOrder.properties");
        paths.add("installOrderUI.properties");
        paths.add("installValueTypes.properties");

        String root = ServerUtil.getPath() + "/install/data/";
        for (String path : paths)
        {
            File f = new File(root + path);
            if (f.exists())
            {
                f.delete();
            }
        }
    }
    
    private void addProperites()
    {
        String root = UpgradeUtil.newInstance().getPath() + PROPERTIES_PATH;
        String copyRoot = ServerUtil.getPath() + PROPERTIES_PATH;
        List<String> names = new ArrayList<String>();
        
        names.add("OdStylesXmlRule.properties");
        names.add("OdpXmlRule.properties");
        names.add("OdsXmlRule.properties");
        names.add("OdtXmlRule.properties");
        names.add("OpenOfficeAdapter.properties");
        
        File pRoot = new File (copyRoot);
        File[] files = pRoot.listFiles();
        List<String> companys = new ArrayList<String>();
        
        for (File f : files)
        {
            if (f.isDirectory())
            {
                String name = f.getName();
                if (!"tm".equals(name) && !"aligner".equals(name))
                {
                    companys.add(name);
                }
            }
        }
        
        for (String name : names)
        {
            File file = new File (root + name);
            try
            {
                FileUtil.copyFile(file, new File (copyRoot + name));
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                UI ui = UIFactory.getUI();
                ui.confirmContinue("Failed to copy " + name);
            }
            
            for (String company : companys)
            {
                try
                {
                    FileUtil.copyFile(file, new File (copyRoot + company + "/" + name));
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    UI ui = UIFactory.getUI();
                    ui.confirmContinue("Failed to copy " + name + " for company " + company);
                }
            }
        }
    }
}
