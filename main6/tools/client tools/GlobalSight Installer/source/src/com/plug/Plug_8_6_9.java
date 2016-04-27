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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.util.ServerUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

public class Plug_8_6_9 implements Plug
{

    private static Logger log = Logger.getLogger(Plug_8_6_9.class);

    private static final String DEPLOY_PATH = "/jboss/server/standalone/deployments";

    public DbUtil dbUtil = DbUtilFactory.getDbUtil();

    @Override
    public void run()
    {
        removeOldVersionJars();
        handleDirtyDataFromLPfile();
    }
 
    // For GBS-4123: upgrade RestEasy bundled in Jboss.
    private void removeOldVersionJars()
    {
        List<String> paths = new ArrayList<String>();
        paths.add("javax/annotation/api/main/jboss-annotations-api_1.1_spec-1.0.1.Final-redhat-3.jar");
        paths.add("javax/ws/rs/api/main/jboss-jaxrs-api_1.1_spec-1.0.1.Final-redhat-3.jar");
        paths.add("org/codehaus/jackson/jackson-core-asl/main/jackson-core-asl-1.9.9.redhat-4.jar");
        paths.add("org/codehaus/jackson/jackson-jaxrs/main/jackson-jaxrs-1.9.9.redhat-4.jar");
        paths.add("org/codehaus/jackson/jackson-mapper-asl/main/jackson-mapper-asl-1.9.9.redhat-4.jar");
        paths.add("org/codehaus/jackson/jackson-xc/main/jackson-xc-1.9.9.redhat-4.jar");
        paths.add("org/codehaus/jettison/main/jettison-1.3.1.redhat-4.jar");
        paths.add("org/jboss/resteasy/resteasy-atom-provider/main/resteasy-atom-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-cdi/main/resteasy-cdi-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-hibernatevalidator-provider/main/resteasy-hibernatevalidator-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jackson-provider/main/resteasy-jackson-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jaxb-provider/main/resteasy-jaxb-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jaxrs/main/async-http-servlet-3.0-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jaxrs/main/resteasy-jaxrs-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jettison-provider/main/resteasy-jettison-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-jsapi/main/resteasy-jsapi-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-multipart-provider/main/resteasy-multipart-provider-2.3.10.Final-redhat-1.jar");
        paths.add("org/jboss/resteasy/resteasy-yaml-provider/main/resteasy-yaml-provider-2.3.10.Final-redhat-1.jar");

        File jarFile = null;
        for (String path : paths)
        {
            try
            {
                jarFile = new File(ServerUtil.getPath() + "/jboss/server/modules/system/layers/base/" + path);
                if (jarFile.exists() && jarFile.isFile())
                {
                    jarFile.delete();
                }
            }
            catch (Exception e)
            {
                log.error("Error to delete file: " + path, e);
            }
        }
    }

    private List<ArrayList> searchDirtyNameFromL10nProfile() throws SQLException
    {
        String sql = "SELECT companyId, NAME, COUNT(id) AS num FROM l10n_profile "
                + "WHERE is_active = 'Y' GROUP BY companyid, NAME HAVING num > 1";
        List<ArrayList> lists = dbUtil.query(sql);
        return lists;
    }

    private List<ArrayList> searchDirtyIdFromL10nProfile(long companyId, String name)
            throws SQLException
    {
        String sql = "SELECT id FROM l10n_profile WHERE NAME = '" + name + "' AND "
                + " companyid = " + companyId + " AND is_active = 'y' ORDER BY TIMESTAMP DESC";
        List<ArrayList> l10nProfileIdlist = dbUtil.query(sql);
        return l10nProfileIdlist;
    }

    public void handleDirtyDataFromLPfile()
    {
        List<ArrayList> namesList;
        try
        {
            namesList = searchDirtyNameFromL10nProfile();
            if (namesList.size() > 0)
            {
                for (ArrayList list0 : namesList)
                {
                    long companyId = (long) list0.get(0);
                    List<ArrayList> l10nProfileIdlist = searchDirtyIdFromL10nProfile(companyId,
                            (String) list0.get(1));
                    if (l10nProfileIdlist.size() > 0)
                    {
                        long latestLPId = (long) l10nProfileIdlist.get(0).get(0);
                        for (int i = 1; i < l10nProfileIdlist.size(); i++)
                        {
                            String sql1 = "UPDATE l10n_profile SET is_active= 'N' WHERE id = ?";
                            List sql1List = new ArrayList<>();
                            sql1List.add(l10nProfileIdlist.get(i).get(0));
                            dbUtil.execute(sql1, sql1List);
                            String sql2 = "UPDATE file_profile SET l10n_profile_id = ? WHERE l10n_profile_id = ?  AND IS_ACTIVE = 'Y' AND companyid = ?";
                            List sql2List = new ArrayList<>();
                            sql2List.add(latestLPId);
                            sql2List.add(l10nProfileIdlist.get(i).get(0));
                            sql2List.add(companyId);
                            dbUtil.execute(sql2, sql2List);
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            log.info("update table file_profile and l10n_profile fail ");
        }
    }
}
