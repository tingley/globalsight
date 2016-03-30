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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;
import com.util.db.Execute;

public class Plug_8_6_8 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_8_6_8.class);

    private static final String DEPLOY_PATH = "/jboss/server/standalone/deployments";
    private static final String PROPERTIES_PATH = DEPLOY_PATH
            + "/globalsight.ear/lib/classes/properties/mt.config.properties";
    public static final String ENGINE_GOOGLE = "Google_Translate";
    public static final String ENGINE_PROMT = "ProMT";
    public static final String ENGINE_MSTRANSLATOR = "MS_Translator";
    public static final String ENGINE_ASIA_ONLINE = "Asia_Online";
    public static final String ENGINE_SAFABA = "Safaba";
    public static final String ENGINE_IPTRANSLATOR = "IPTranslator";
    public static final String ENGINE_DOMT = "DoMT";

    public static final String SPELL_CHECK_WAR = "/jboss/server/standalone/deployments/globalsight.ear/spellchecker.war";
    public static final String XDE_SPELL_CHECK_WAR = "/jboss/server/standalone/deployments/globalsight.ear/xdespellchecker.war";

    private static final String BLAISE_OLD_JAR_FILE = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.0.1.jar";
    private static final String BLAISE_OLD_JAR_FILE2 = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.0.1.jar";

    public DbUtil dbUtil = DbUtilFactory.getDbUtil();
    
    @Override
    public void run()
    {
        removeSpellCheck();

        // Delete old Blaise jar file
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE);
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE2);

        updateForMT();
    }

    private void updateForMT()
    {
        Properties properties = PropertyUtil.getProperties(new File(ServerUtil.getPath()
                + (PROPERTIES_PATH)));
        String isSafabaLog = properties.getProperty("safaba.log.detailed.info");
        if ("true".equalsIgnoreCase(isSafabaLog))
        {
            updateColum("Y", ENGINE_SAFABA);
        }
        else
        {
            updateColum("N", ENGINE_SAFABA);
        }
        String isDomtLog = properties.getProperty("domt.log.detailed.info");
        if ("true".equalsIgnoreCase(isDomtLog))
        {
            updateColum("Y", ENGINE_DOMT);
        }
        else
        {
            updateColum("N", ENGINE_DOMT);
        }
        String isIPLog = properties.getProperty("iptranslator.log.detailed.info");
        if ("true".equalsIgnoreCase(isIPLog))
        {
            updateColum("Y", ENGINE_IPTRANSLATOR);
        }
        else
        {
            updateColum("N", ENGINE_IPTRANSLATOR);
        }
        String isPromtLog = properties.getProperty("promt.log.detailed.info");
        if ("true".equalsIgnoreCase(isPromtLog))
        {
            updateColum("Y", ENGINE_PROMT);
        }
        else
        {
            updateColum("N", ENGINE_PROMT);
        }
        String isMSLog = properties.getProperty("ms_translator.log.detailed.info");
        if ("true".equalsIgnoreCase(isMSLog))
        {
            updateColum("Y", ENGINE_MSTRANSLATOR);
        }
        else
        {
            updateColum("N", ENGINE_MSTRANSLATOR);
        }
        String isAsiaLog = properties.getProperty("asia_online.log.detailed.info");
        if ("true".equalsIgnoreCase(isAsiaLog))
        {
            updateColum("Y", ENGINE_ASIA_ONLINE);
        }
        else
        {
            updateColum("N", ENGINE_ASIA_ONLINE);
        }
        String isGoogleLog = properties.getProperty("google_translate.log.detailed.info");
        if ("true".equalsIgnoreCase(isGoogleLog))
        {
            updateColum("Y", ENGINE_GOOGLE);
        }
        else
        {
            updateColum("N", ENGINE_GOOGLE);
        }
        String MSType = properties.getProperty("ms_translator.translate.type");
        String sql = "update mt_profile set MS_TRANS_TYPE = ? where MT_ENGINE = 'MS_Translator'";
        List args = new ArrayList<>();
        args.add(MSType);

        dbUtil.execute(sql, args);
    }

    private void updateColum(String isLogDebugInfo, String engineName)
    {
        String updateSql = "update mt_profile set LOG_DEBUG_INFO = ? where MT_ENGINE = ?";
        List args = new ArrayList<>();
        args.add(isLogDebugInfo);
        args.add(engineName);
        dbUtil.execute(updateSql, args);
    }

    private void deleteFiles(String path)
    {
        try
        {
            File f = new File(path);
            if (f.exists())
                FileUtil.deleteFile(f);
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }
    
    private void removeSpellCheck()
    {
        deleteFiles(ServerUtil.getPath() + SPELL_CHECK_WAR);
        deleteFiles(ServerUtil.getPath() + XDE_SPELL_CHECK_WAR);
    }
}
