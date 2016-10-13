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
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

public class Plug_7_1_4_1 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_4_1.class);

    private static String IGNORE_INVALID_TAGS = "IgnoreInvalidHtmlTags";
    private UI ui = UIFactory.getUI();

    @Override
    public void run()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "Tags.properties".equalsIgnoreCase(pathname.getName());
            }
        });

        for (File f : tagProperties)
        {
            if (PropertyUtil.get(f, IGNORE_INVALID_TAGS) == null)
            {
                String comment = "# Ignore invalid html tags and continue to "
                        + "proceed the rest of HTML. true | false. "
                        + FileUtil.lineSeparator + "#Default value is true";

                try
                {
                    appendAttribute(f, IGNORE_INVALID_TAGS, "true", comment);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    ui.error(e.getMessage());
                    System.out.println();
                }
            }
        }
    }

    private void appendAttribute(File f, String name, String value,
            String comment) throws Exception
    {
        FileWriter out = new FileWriter(f, true);
        out.write(FileUtil.lineSeparator);
        out.write(FileUtil.lineSeparator);
        out.write(comment);
        out.write(FileUtil.lineSeparator);
        out.write(name);
        out.write("=");
        out.write(value);
        out.flush();
        out.close();
    }
}
