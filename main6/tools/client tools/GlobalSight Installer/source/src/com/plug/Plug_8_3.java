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

import com.Main;
import com.plug.Version_8_3_0.UserIdNameMigrator;
import com.util.ServerUtil;

public class Plug_8_3 implements Plug
{
    @Override
    public void run()
    {
        upgradeUserIdNames();
        updateProperties();
    }

    private void upgradeUserIdNames()
    {
        UserIdNameMigrator.update();
    }
    
    private void updateProperties()
    { 
        List<File> templates = new ArrayList<File>();
        templates.add(new File(ServerUtil.getPath() + "/install/data/mysql/create_cap_mysql.sql.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/mysql/drop_all_mysql.sql.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/mysql/insert_default_calendar_mysql.sql.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/openldap/globalsight.ldif.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/openldap/globalsight/globalsight.acl.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/openldap-linux/globalsight.ldif.template"));
        templates.add(new File(ServerUtil.getPath() + "/install/data/openldap-linux/globalsight/globalsight.acl.template"));
        Main.getInstallUtil().parseTemplates(templates);
    }
}
