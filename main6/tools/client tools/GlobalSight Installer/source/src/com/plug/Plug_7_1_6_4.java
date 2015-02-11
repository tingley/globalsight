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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_7_1_6_4 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_6_4.class);

    private UI ui = UIFactory.getUI();
    private static final String L18N_BEGIN = "# The default UI locale.";
    private static final String L18N_END = "# Determines whether the OR/AND nodes should be visible";
    private static final String SUPPORT_LOCALE = "ui.locales";
    private static final String DEFAULT_LOCALE = "default.ui.locale=";
    private static final String DEFAULT_LOCALE_COMMENT = "# The default UI locale.  There should only be ONE locale"
            + FileUtil.lineSeparator
            + "# this option has been moved to the UI configuration in GlobalSight"
            + FileUtil.lineSeparator;
    private static final String SUPPORT_LOCALE_COMMENT = "# The supported UI locales (comma delimited)"
            + FileUtil.lineSeparator
            + "# NOTE: This can only list the SUPPORTED UI Locales: en_US, es_ES"
            + FileUtil.lineSeparator
            + "# this option has been moved to the UI configuration in GlobalSight"
            + FileUtil.lineSeparator;

    @Override
    public void run()
    {
        updateEnvoyProperties();
    }

    /**
     * For GBS-855, Support GlobalSight Internationalization (I18N) 
     */
    private void updateEnvoyProperties()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> properties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "envoy.properties".equalsIgnoreCase(pathname.getName());
            }
        });

        changeContents(properties);
    }

    public void changeContents(List<File> properties)
    {
        for (File f : properties)
        {
            try
            {
                changeContent(f);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                ui.error(e.getMessage());
                System.out.println();
            }
        }
    }

    private void changeContent(File file) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String s = in.readLine();
        StringBuilder content = new StringBuilder();
        boolean write = true;
        while (s != null)
        {
            if (s.startsWith(L18N_BEGIN))
            {
                write = false;
            }

            if (s.startsWith(L18N_END))
            {
                write = true;
            }

            if (!write)
            {
                if (s.indexOf(DEFAULT_LOCALE) > -1)
                {
                    content.append(DEFAULT_LOCALE_COMMENT);

                    if (!s.startsWith("#"))
                    {
                        content.append("# ");
                    }

                    content.append(s).append(FileUtil.lineSeparator).append(FileUtil.lineSeparator);
                }

                if (s.indexOf(SUPPORT_LOCALE) > -1
                        && s.indexOf("NOTE: NO blank here") < 0)
                {
                    content.append(SUPPORT_LOCALE_COMMENT);

                    if (!s.startsWith("#"))
                    {
                        content.append("# ");
                    }
                    
                    content.append(s).append(FileUtil.lineSeparator).append(FileUtil.lineSeparator);
                }
            }
            else
            {
                content.append(s).append(FileUtil.lineSeparator);
            }

            s = in.readLine();
        }
        in.close();

        FileWriter out = new FileWriter(file);
        out.write(content.toString());
        out.flush();
        out.close();
    }
}
