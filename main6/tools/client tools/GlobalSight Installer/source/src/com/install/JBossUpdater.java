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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.util.FileUtil;
import com.util.ServerUtil;

public abstract class JBossUpdater
{
    private static Logger log = Logger.getLogger(JBossUpdater.class);
    
    protected abstract boolean isUpdate();
    protected abstract void backup() throws Exception;
    public abstract void readOptions();

    protected static final String WRAPPER = "/install/JavaServiceWrapper/conf/wrapper.conf";
    protected static final String BIN_PATH = "/jboss/server/bin";
    protected static final String STANDALONE_CONF_PATH = BIN_PATH + "/standalone.conf";
    protected static final String STANDALONE_CONF_BAT_PATH = BIN_PATH + "/standalone.conf.bat";
    protected static Properties option = null;
    
    public void backupJboss() throws Exception
    {
        if (!isUpdate())
            return;
        
        readOptions();
        backup();
    }
    
    public void updateJboss() throws Exception
    {
        if (!isUpdate())
            return;
        
        updateJavaOptions();
    }
    
    public static void updateJavaOptions() throws IOException
    {
        if (option != null)
        {
            String runConf = ServerUtil.getPath() + STANDALONE_CONF_BAT_PATH;
            File f = new File(runConf);
            
            String content = FileUtil.readFile(f);
            content = content
                    .replace(
                            "set \"JAVA_OPTS=-Xms1024m -Xmx1024m -XX:MaxPermSize=256m -Xss512k\"",
                            MessageFormat
                                    .format("set \"JAVA_OPTS=-Xms{0} -Xmx{1} -XX:MaxPermSize={2} -Xss{3}\"",
                                            option.get("Xms"),
                                            option.get("Xmx"),
                                            option.get("XX"),
                                            option.get("Xss")));
            FileUtil.writeFile(f, content);
            
            String path = ServerUtil.getPath() + STANDALONE_CONF_PATH;
            f = new File(path);
            content = FileUtil.readFile(f);
            content = content
                    .replace(
                            "JAVA_OPTS=\"-Xms1303m -Xmx1303m -XX:MaxPermSize=256m ",
                            MessageFormat
                                    .format("JAVA_OPTS=\"-Xms{0} -Xmx{1} -XX:MaxPermSize={2} ",
                                            option.get("Xms"),
                                            option.get("Xmx"),
                                            option.get("XX")));
            FileUtil.writeFile(f, content);
        }
    }
}
