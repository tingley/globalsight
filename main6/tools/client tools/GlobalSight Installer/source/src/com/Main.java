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
package com;

import com.install.Update;
import com.install.Upgrade;
import com.util.InstallUtil;
import com.util.PatchUtil;
import com.util.UIUtil;
import com.util.UpgradeUtil;

public class Main
{
    private static boolean IS_PATCH = true;
    private static final String OPTION_PATCH = "-n";
    private static InstallUtil UTIL = null;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        UIUtil.setLookAndFeel();
        
        for (String arg : args)
        {
            if (OPTION_PATCH.equalsIgnoreCase(arg))
            {
                IS_PATCH = false;
            }
        }
        
        if (isPatch())
        {
            Update install = new Update();
            install.doUpdate();
        }
        else
        {
            Upgrade upgrade = new Upgrade();
            upgrade.doUpgrade();
        }
        
    }
    
    public static InstallUtil getInstallUtil()
    {
        if (UTIL == null)
        {
            if (isPatch())
            {
                UTIL = new PatchUtil();
            }
            else
            {
                UTIL = UpgradeUtil.newInstance();
            }
        }
        return UTIL;
    }

    public static boolean isPatch()
    {
        return IS_PATCH;
    }
}
