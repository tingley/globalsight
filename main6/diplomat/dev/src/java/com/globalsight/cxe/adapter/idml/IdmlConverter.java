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
package com.globalsight.cxe.adapter.idml;

import java.io.File;
import java.util.List;

import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

public class IdmlConverter
{
    static
    {
        java.util.logging.Logger jlog = java.util.logging.Logger
                .getLogger("org.artofsolving.jodconverter");
        jlog.setLevel(java.util.logging.Level.SEVERE);
    }

    public String convertIdmlToXml(String p_odFile, String p_dir) throws Exception
    {
        ZipIt.unpackZipPackage(p_odFile, p_dir);

        return p_dir;
    }

    public String convertXmlToIdml(String p_odFileName, String p_xmlDir) throws Exception
    {
        File xmlDir = new File(p_xmlDir);
        File parent = xmlDir.getParentFile();
        File zipFile = new File(parent, p_odFileName);
        if (zipFile.exists())
        {
            zipFile.delete();
        }

        // get all files
        List<File> fs = FileUtil.getAllFiles(xmlDir);
        File[] entryFiles = new File[fs.size()];
        entryFiles = fs.toArray(entryFiles);

        // create zip file
        zipFile.createNewFile();
        ZipIt.compress(xmlDir.getPath(), zipFile);

        return zipFile.getPath();
    }
}