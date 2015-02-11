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
package com.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.ui.UI;
import com.ui.UIFactory;

public class UpgradeUtil extends InstallUtil
{
    private static Logger log = Logger.getLogger(UpgradeUtil.class);

    private String path = null;
    private UI ui = UIFactory.getUI();

    private static UpgradeUtil UTIL = null;

    private UpgradeUtil()
    {

    }

    public static UpgradeUtil newInstance()
    {
        if (UTIL == null)
        {
            UTIL = new UpgradeUtil();
        }

        return UTIL;
    }

    @Override
    public String getPath()
    {
        if (path == null)
        {
            File root = new File("./../server").listFiles()[0];
            try
            {
                path = root.getCanonicalPath();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Can't find server path");
            }
        }

        return path;
    }

    /**
     * Validates the version.
     * 
     * @throws Exception
     *             Throw out if validation failed.
     */
    public void validateVersion() throws Exception
    {
        if (compare(getRequestedVersion(), ServerUtil.getVersion()) > 0)
        {
            throw new Exception(MessageFormat.format(Resource
                    .get("version.needUpdate"), ServerUtil.getVersion(),
                    getRequestedVersion()));
        }
        
        int result = compare(getVersion(), ServerUtil.getVersion());
        if (result < 0)
        {
            throw new Exception(MessageFormat.format(Resource
                    .get("version.upgrade.notSupport"), getVersion(),
                    ServerUtil.getVersion()));
        }
        else if (result == 0)
        {
            ui.confirmUpgradeAgain();
        }
    }

    public void validatePath() throws Exception
    {
        String oldPath = ServerUtil.getPath().replace('\\', '/');
        String newPath="";
        try
        {
            newPath = getPath().replace('\\', '/');
        }
        catch (Exception e)
        {
            throw new Exception(Resource.get("msg.warnInstallFileMissing"));
        }
       
        if (oldPath.equalsIgnoreCase(newPath))
        {
            throw new Exception(Resource.get("path.upgrade.same"));
        }
    }
}
