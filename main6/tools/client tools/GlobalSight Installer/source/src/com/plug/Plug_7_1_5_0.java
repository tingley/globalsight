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
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

public class Plug_7_1_5_0 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_5_0.class);

    private UI ui = UIFactory.getUI();

    @Override
    public void run()
    {
        updateTagsProperties();
        updateWordcounterProperties();
    }
    
    private void updateTagsProperties()
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

        String ignoreComment = "# Ignore invalid html tags and continue to "
                + "proceed the rest of HTML. true | false. "
                + FileUtil.lineSeparator + "#Default value is false";
        String convertHtmlComment = "# whether convert html entity while "
                + "exporting, true or false.";

        try
        {
            PropertyUtil.appendProperties(tagProperties,
                    "IgnoreInvalidHtmlTags", "false", ignoreComment);
            PropertyUtil.appendProperties(tagProperties, "convertHtmlEntity",
                    "false", convertHtmlComment);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            ui.error(e.getMessage());
            System.out.println();
        }
    }
    
    private void updateWordcounterProperties()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "Wordcounter.properties".equalsIgnoreCase(pathname
                        .getName());
            }
        });

        String comment = "# wordplacehold: Define the  Placeholder variables that do not need be counted in documents on import, and you"
                + FileUtil.lineSeparator
                + "# can use it to split one segment to calculate the word count."
                + FileUtil.lineSeparator
                + "#"
                + FileUtil.lineSeparator
                + "# split the variables with \"\\n\""
                + FileUtil.lineSeparator
                + "#"
                + FileUtil.lineSeparator
                + "#Example: -"
                + FileUtil.lineSeparator
                + "#\\[HotelID\\]:\\s*\\d+ "
                + FileUtil.lineSeparator
                + "#could be splitted as "
                + FileUtil.lineSeparator
                + "#1) -"
                + FileUtil.lineSeparator
                + "#2) \\[HotelID\\]:\\s*\\d+" + FileUtil.lineSeparator + "# ";

        try
        {
            for (File f : tagProperties)
            {
                BufferedReader in = new BufferedReader(new FileReader(f));
                String s = in.readLine();
                StringBuilder content = new StringBuilder();

                while (s != null)
                {
                    content.append(s).append(FileUtil.lineSeparator);
                    s = in.readLine();
                }
                in.close();

                int start = content
                        .indexOf("# wordplacehold: Define the  Placeholder");
                int end = content
                        .indexOf("#wordcounter_count_placeholders=$(*),{*}");

                if (start > 0 && end > 0)
                {
                    end += "#wordcounter_count_placeholders=$(*),{*}".length();
                    content.replace(start, end, comment);
                    FileWriter out = new FileWriter(f, false);
                    out.write(content.toString());
                    out.write(FileUtil.lineSeparator);
                    out.flush();
                    out.close();
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            ui.error(e.getMessage());
            System.out.println();
        }
    }
}
