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

public class Plug_7_1_6_3 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_6_3.class);

    private UI ui = UIFactory.getUI();
    private static final String PARAGRAPH_BEGIN = "# unextractableWordParagraphStyles: This is a comma-separated list";
    private static final String PARAGRAPH = "unextractableWordParagraphStyles=";
    private static final String CHARACTER = "unextractableWordCharacterStyles=";

    private static final String REMOVE_COMMENT = "# this configuration has "
            + "been moved to the doc filter " + FileUtil.lineSeparator
            + "# in the globalsight UI. You can re-set "
            + "your styles in the filter." + FileUtil.lineSeparator;

    @Override
    public void run()
    {
        updateWordExtractorProperties();
    }

    private void updateWordExtractorProperties()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> properties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "WordExtractor.properties".equalsIgnoreCase(pathname
                        .getName());
            }
        });

        changeContents(properties);
    }

    private void changeContents(List<File> properties)
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
            if (s.startsWith(PARAGRAPH_BEGIN))
            {
                write = false;

            }

            if (!write)
            {
                if (s.startsWith(PARAGRAPH) || s.startsWith(CHARACTER))
                {
                    content.append(REMOVE_COMMENT);
                    content.append("# ").append(FileUtil.lineSeparator)
                            .append("# ");
                }
                else if (!s.startsWith("#") && s.trim().length() > 0)
                {
                    content.append("# ");
                }
            }

            content.append(s).append(FileUtil.lineSeparator);
            s = in.readLine();
        }
        in.close();

        FileWriter out = new FileWriter(file);
        out.write(content.toString());
        out.flush();
        out.close();
    }
}
