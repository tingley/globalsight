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
package com.globalsight.ling.aligner.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/**
 * AlignmentPackageReader reads an alignment package from an input
 * stream and write files contained in the package into a specified
 * directory.
 */

public class AlignmentPackageReader
{
    static public final int BUFSIZE = 4096;

    private ZipFile m_zipFile;

    // project tmp file storage directory
    private File m_projectTmpDirectory;


    /**
     * Constructor. 
     *
     * @param p_in InputStream of the package
     * @param p_projectTmpDirectory The alignment project temp file
     * storage directory. Files are written to this directory.
     */
    public AlignmentPackageReader(
        String filePath, File p_projectTmpDirectory)
        throws Exception
    {
        m_zipFile = new ZipFile(filePath);
        m_projectTmpDirectory = p_projectTmpDirectory;
    }

    public void readAlignmentPackage()
        throws Exception
    {
        Enumeration<?> enumeration = m_zipFile.getEntries();
        while (enumeration.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry)enumeration.nextElement();
        
            File outFile = new File(m_projectTmpDirectory, entry.getName());
            if(outFile.exists())
            {
                outFile.delete();
            }
            
            BufferedOutputStream out
                = new BufferedOutputStream(new FileOutputStream(outFile));

            BufferedInputStream in = new BufferedInputStream(m_zipFile
                    .getInputStream(entry));

            byte[] buf = new byte[BUFSIZE];
            int readLen = 0;
        
            while((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                out.write(buf, 0, readLen);
            }
            out.close();
        }
        m_zipFile.close();
    }
    
}
