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
import com.util.ServerUtil;

public class Plug_7_1_8_1 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_8_1.class);

    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
            + "default/deploy";
    private static final String WEB_TEMPLATE_PATH = DEPLOY_PATH
            + "/globalsight.ear/globalsight-web.war/WEB-INF/web.xml.template";

    @Override
    public void run()
    {
        parseTemplates();
    }
    
    private void parseTemplates()
    {
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + WEB_TEMPLATE_PATH));
        Main.getInstallUtil().parseTemplates(templates);
    }
}
