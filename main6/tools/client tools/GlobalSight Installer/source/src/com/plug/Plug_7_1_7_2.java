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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_7_1_7_2 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_7_2.class);

    private static final String DEPLOY_PATH = "/jboss/jboss_server/server/"
        + "default/deploy";
    private static final String ENVOY_GENERATED_PATH = DEPLOY_PATH
            + "/globalsight.ear/lib/classes/properties/envoy_generated.properties";
    
    private static List<String> AdobeXmpRuleAddContents = new ArrayList<String>();
    static
    {
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"InstanceID\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"DocumentID\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"OriginalDocumentID\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"CreateDate\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"ModifyDate\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"MetadataDate\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"CreatorTool\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"format\"]'/>");
        AdobeXmpRuleAddContents.add("<dont-translate path='//*[local-name()=\"DocChangeCount\"]'/>");
    }
    
    private static final String InddruleContent = "<schemarules>"
            + FileUtil.lineSeparator
            + "    <ruleset schema=\"Root\">"
            + FileUtil.lineSeparator
            + "        <translate path=\"/Root/Inddgsstory/Inddgsparagraph\"/>"            
            + FileUtil.lineSeparator + "    </ruleset>"
            + FileUtil.lineSeparator + "</schemarules>";

    @Override
    public void run()
    {
        updateEnvoyGenerated();
        updateAdobeXmpRule();
        updateInddrule();
    }

    private void updateEnvoyGenerated()
    {
        File file = new File(ServerUtil.getPath() + ENVOY_GENERATED_PATH);
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new FileReader(file));
            String s = in.readLine();
            StringBuilder content = new StringBuilder();

            while (s != null)
            {
                content.append(s).append(FileUtil.lineSeparator);
                s = in.readLine();
            }

            if (content.indexOf("RSSPersistenceManagerWLRMIImpl") < 0)
            {
                int i = content.indexOf("AlignerManagerWLRMIImpl");
                String cxe = "  com.globalsight.everest.webapp.pagehandler.rss.RSSPersistenceManagerWLRMIImpl,\\" + FileUtil.lineSeparator;

                content.insert(i + "AlignerManagerWLRMIImpl".length() + 3, cxe);
            }

            FileWriter out = new FileWriter(file, false);
            out.write(content.toString());
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private void updateAdobeXmpRule()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "AdobeXmpRule.properties".equalsIgnoreCase(pathname.getName());
            }
        });
        
        String endTag = "</ruleset>";
        for (File f : tagProperties)
        {
            StringBuilder content = FileUtil.readFileAsStringBuilder(f);
            int index = content.indexOf(endTag);
            for (String s : AdobeXmpRuleAddContents)
            {
                if (content.indexOf(s) < 0)
                {
                    content.insert(index, "    " + s + FileUtil.lineSeparator + "    ");
                    index = content.indexOf(endTag);
                }
            }
            
            try
            {
                FileUtil.writeFile(f, content.toString());
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private void updateInddrule()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "inddrule.properties".equalsIgnoreCase(pathname.getName());
            }
        });
        
        String start = "<!-- 1. attributes are in line -->";
        for (File f : tagProperties)
        {
            StringBuilder content = FileUtil.readFileAsStringBuilder(f);
            String deleted = "<translate path=\"/Root/Inddgsstory/Inddgsparagraph\" inline=\"yes\"/>";
            if (content.indexOf(deleted) > 0)
            {
                int begin = content.indexOf(start);
                int startIndex = content.indexOf("<schemarules>", begin);
                int endIndex = content.indexOf("</schemarules>", begin);
                content.delete(startIndex, endIndex + "</schemarules>".length());
                content.insert(startIndex, InddruleContent);
                
                try
                {
                    FileUtil.writeFile(f, content.toString());
                }
                catch (IOException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
