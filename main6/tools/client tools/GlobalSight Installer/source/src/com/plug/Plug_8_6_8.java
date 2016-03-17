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

import com.util.PropertyUtil;
import com.util.ServerUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;
import com.util.db.Execute;

public class Plug_8_6_8 implements Plug
{
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
    
    public DbUtil dbUtil = DbUtilFactory.getDbUtil();

    @Override
    public void run()
    {
        Properties properties = PropertyUtil.getProperties(new File(ServerUtil.getPath() + (PROPERTIES_PATH)));
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
        String isIPLog = properties
                .getProperty("iptranslator.log.detailed.info");
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
        String isMSLog = properties
                .getProperty("ms_translator.log.detailed.info");
        if ("true".equalsIgnoreCase(isMSLog))
        {
            updateColum("Y", ENGINE_MSTRANSLATOR);
        }
        else
        {
            updateColum("N", ENGINE_MSTRANSLATOR);
        }
        String isAsiaLog = properties
                .getProperty("asia_online.log.detailed.info");
        if ("true".equalsIgnoreCase(isAsiaLog))
        {
            updateColum("Y", ENGINE_ASIA_ONLINE);
        }
        else
        {
            updateColum("N", ENGINE_ASIA_ONLINE);
        }
        String isGoogleLog = properties
                .getProperty("google_translate.log.detailed.info");
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

}
