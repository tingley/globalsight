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

package com.globalsight.persistence.hibernate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;

public class MappingConfigGenerator
{
    public static final String FOLDER_PATH = System.getProperty("user.dir")
            + "\\"
            + "envoy\\src\\java\\com\\globalsight\\persistence\\hibernate\\xml";

    public static final String TARGET_FILE = System.getProperty("user.dir")
            + "\\" + "envoy\\src\\java\\hibernate.cfg.xml";

    public static void main(String[] args) throws Exception
    {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(TARGET_FILE))));

        String[] xmlFiles = new File(FOLDER_PATH).list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".hbm.xml");
            }
        });
        
        out.write(XML_HEAD);

        for (int i = 0; i < xmlFiles.length; i++)
        {
            out.write(ITEM.replaceAll("FILE_NAME", xmlFiles[i]));
        }

        out.write(XML_TAIL);
        out.close();

        System.out.println("WRITING FILE DONE!");
    }

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String XML_HEAD = "<!DOCTYPE hibernate-configuration PUBLIC"
            + NEW_LINE
            + "    \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\""
            + NEW_LINE
            + "    \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">"
            + NEW_LINE
            + NEW_LINE
            + "<hibernate-configuration>"
            + NEW_LINE
            + "    <session-factory>" + NEW_LINE + NEW_LINE;

    private static final String ITEM = "        <mapping"
            + NEW_LINE
            + "            resource=\"com/globalsight/persistence/hibernate/xml/FILE_NAME\" />"
            + NEW_LINE;

    private static final String XML_TAIL = NEW_LINE + "    </session-factory>"
            + NEW_LINE + "</hibernate-configuration>";
}
