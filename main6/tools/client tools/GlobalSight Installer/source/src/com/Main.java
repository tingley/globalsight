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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.install.Update;
import com.install.Upgrade;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.InstallUtil;
import com.util.JarSignUtil;
import com.util.PatchUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;
import com.util.UIUtil;
import com.util.UpgradeUtil;

public class Main
{
	private static Logger log = Logger.getLogger(Main.class);
	
    private static boolean IS_PATCH = false;
    private static final String OPTION_PATCH = "-n";
    private static InstallUtil UTIL = null;
    
    private static String JKS = null;
    private static String keyPass = null;
    private static String keyAlias = null;

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
        
        boolean canSign = false;
        canSign = validateSetting();
        
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
        
        UI ui = UIFactory.getUI();
        if (canSign && JKS != null)
        {
        	
        	log.info("Signing applet jar.");
        	udpateJar();
        	log.info("Signing applet jar finished");
        }
        
        ui.finish();
    }
    
    public static boolean validateSetting()
    {
    	Properties p = PropertyUtil.getProperties(new File("../config.properties"));
        String gsHome = p.getProperty("gsHome");
        if (gsHome == null)
        {
        	log.info("The gsHome is not set");
        }
        else
        {
        	gsHome = gsHome.trim();
        	File f = new File(gsHome);
        	if (f.exists())
        	{
        		try 
        		{
					ServerUtil.setPATH(f.getCanonicalPath());
				} 
        		catch (IOException e) 
				{
					log.error(e);
					e.printStackTrace();
				}
        	}
        	else
        	{
        		log.info("The gsHome is wrong");
        	}
        }
        
        JKS = p.getProperty("JKS");
        if (JKS != null)
        {
        	UI ui = UIFactory.getUI();
        	JKS = JKS.trim();
        	File f = new File(JKS);
        	
        	if (!f.isFile())
        	{
        		f = new File("../" + JKS);
        	}
        	
        	if (!f.isFile())
        	{
        		ui.confirmContinue(MessageFormat.format(Resource
                        .get("msg.jarsign_no_file"), JKS));
        		JKS = null;
        	}
        	else
        	{
        		JKS = f.getAbsolutePath();
        		
        		keyPass = p.getProperty("keyPass");
            	keyAlias = p.getProperty("keyAlias");
            	
				if (keyPass == null || keyAlias == null)
            	{
					JKS = null;
            		ui.confirmContinue(Resource.get("msg.jarsign_pass_error"));
            	}
				else
				{
					keyPass = keyPass.trim();
					keyAlias = keyAlias.trim();
					if ( !JarSignUtil.validate(JKS, keyPass, keyAlias))
	            	{
						JKS = null;
	            		ui.confirmContinue(Resource.get("msg.jarsign_pass_error"));
	            	}
				}
        	}
        	
        	return true;
        }
        
        return false;
    }
    
    public static void udpateJar()
    {
		File root = new File(
				ServerUtil.getPath()
						+ "/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/applet/lib");
		JarSignUtil.updateJars(root, JKS, keyPass, keyAlias);
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
