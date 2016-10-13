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
package com.install;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.util.FileUtil;
import com.util.ServerUtil;
import com.util.UpgradeUtil;

public class JBossUpdater72 extends JBossUpdater
{
    private static Logger log = Logger.getLogger(JBossUpdater72.class);
    
    private Boolean isJboss72 = null;
    
    @Override
    protected boolean isUpdate()
    {
        if (isJboss72 == null)
        {
            File f = new File(ServerUtil.getPath() + "/jboss/server/version.txt");
            File f2 = new File(UpgradeUtil.newInstance().getPath() + "/jboss/server/version.txt");
            
            if (!f.exists() || !f2.exists())
            {
            	isJboss72 = false;
            	return isJboss72;
            }
            
            String content1 = FileUtil.readFile(f);
            String content2 = FileUtil.readFile(f2);
            
            isJboss72 = content1.contains(" 6.0.1.Alpha1") && content2.contains("6.4.0.GA");
        }
        
        return isJboss72;
    }

    private void backupProperties() throws Exception
    {
        String pPath = "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties";
        File pRoot = new File(ServerUtil.getPath() + pPath);
        List<File> pfiles = FileUtil.getAllFiles(pRoot, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                String path = pathname.getAbsolutePath().replace("\\", "/");
                
                if (path.endsWith("/AdobeAdapter.properties") || path.endsWith("/IdmlAdapter.properties"))
                {
                    String[] ns = path.split("/");
                    if (ns[ns.length - 3].equals("properties"))
                    {
                        // is company files
                        return true;
                    }
                }
                
                if (Upgrade.COPY_UNCOVER.contains(pathname.getName()))
                {
                	return true;
                }
                
                return false;
            }
        });
        
        for (File f : pfiles)
        {
            String path = f.getCanonicalPath().replace("\\", "/");
            String serverPath = ServerUtil.getPath().replace("\\", "/");
            String upgradePath = Upgrade.UPGRADE_UTIL.getPath().replace("\\", "/");
            
            path = path.replace(serverPath, upgradePath);
            
            FileUtil.copyFile(f, new File(path));
        }
    }
    
    private void backupInstallValues() throws Exception
    {
        File f = new File(ServerUtil.getPath() + "/install/data/installValues.properties");
        if (f.exists())
        {
            String path = f.getCanonicalPath().replace("\\", "/");
            String serverPath = ServerUtil.getPath().replace("\\", "/");
            String upgradePath = Upgrade.UPGRADE_UTIL.getPath().replace("\\", "/");
            
            path = path.replace(serverPath, upgradePath);
            
            FileUtil.copyFile(f, new File(path));
        }
        else
        {
            log.error("Can not find the file: " + f.getAbsolutePath());
        }
    }
    
    private void backupOtherfiles() throws Exception
    {
        List<String> paths = new ArrayList<String>();
//      paths.add("/jboss/server/standalone/deployments/globalsight.ear/lib/classes/com/globalsight/resources/messages");
      paths.add("/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/images");
      
      for (String bfile : paths)
      {
          File root = new File(ServerUtil.getPath() + bfile);
          if (!root.exists())
              continue;
          
          List<File> files = FileUtil.getAllFiles(root);
          
          for (File f : files)
          {
              String path = f.getCanonicalPath().replace("\\", "/");
              String serverPath = ServerUtil.getPath().replace("\\", "/");
              String upgradePath = Upgrade.UPGRADE_UTIL.getPath().replace("\\", "/");
              
              path = path.replace(serverPath, upgradePath);
              
              FileUtil.copyFile(f, new File(path));
          }
      }
    }
    
    /**
     * @see com.install.JbossUpdater#backup()
     */
    @Override
    protected void backup() throws Exception
    {
        backupProperties();
        backupInstallValues();
        backupOtherfiles();
    }

    /**
     * @see com.install.JbossUpdater#readOptions()
     */
    @Override
    public void readOptions()
    {
        String path = ServerUtil.getPath() + STANDALONE_CONF_BAT_PATH;
        
        if (ServerUtil.isInLinux())
        {
            path = ServerUtil.getPath() + STANDALONE_CONF_PATH;
        }
        
        File f = new File(path);
        
        if (!f.exists())
            return;
        
        String content = FileUtil.readFile(f);
        option = new Properties();
        
        Pattern p = Pattern.compile("-Xms([\\d]*[\\D])");
        Matcher m = p.matcher(content);
        
        if (m.find())
        {
            option.put("Xms", m.group(1));
        }
        
        p = Pattern.compile("-Xmx([\\d]*[\\D])");
        m = p.matcher(content);
        
        if (m.find())
        {
            option.put("Xmx", m.group(1));
        }
        
        p = Pattern.compile("-XX:MaxPermSize=([\\d]*[\\D])");
        m = p.matcher(content);
        
        if (m.find())
        {
            option.put("XX", m.group(1));
        }
        
        p = Pattern.compile("-Xss([\\d]*[\\D])");
        m = p.matcher(content);
        
        if (m.find())
        {
            option.put("Xss", m.group(1));
        }
        else
        {
            option.put("Xss", "512k");
        }
    }

}
