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
import com.ui.UIFactory;

public class PatchUtil extends InstallUtil
{
    private static Logger log = Logger.getLogger(PatchUtil.class);
    
    private static String PATH = null;

    @Override
    public String getPath()
    {
        if (PATH == null)
        {
            File root = new File("./data");
            root.listFiles();
            try
            {
                PATH = root.listFiles()[0].getCanonicalPath();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return PATH;
    }

    /**
     * Validates the version for patch and server.
     * <p>
     * The following is the work flow.
     * <p>
     * 1. If the request version of the path is bigger than server version, it
     * means server need to update first, validation is failed.
     * <p>
     * 2. If the request version of the path is smaller than server version,
     * check the patch version is same as the server version?
     * <ul>
     * <li>Yes, it means the patch has been installed, if re-install is
     * allowed, let user make sure that is he want to re-install? exit syste if
     * answer is no. if re-install is not allowed, validation is failed.
     * <li>No, it means the server is more newer than the patch, validation is
     * failed.
     * </ul>
     * 
     * @throws Exception
     *             Throw out if validation failed.
     */
    public void validateVersion() throws Exception
    {
        int n = compare(getRequestedVersion(), ServerUtil.getVersion());
        log.debug("Comparing result: " + n);

        if (n > 0)
        {
            throw new Exception(MessageFormat.format(Resource
                    .get("version.needUpdate"), ServerUtil.getVersion(),
                    getRequestedVersion()));
        }

        n = compare(ServerUtil.getVersion(), getVersion());
        if (n == 0)
        {
            UIFactory.getUI().confirmUpgradeAgain();
        }
        else if (n > 0)
        {
            throw new Exception(MessageFormat.format(Resource
                    .get("version.patch.notSupport"), ServerUtil.getVersion(),
                    getVersion()));
        }
    }
}
