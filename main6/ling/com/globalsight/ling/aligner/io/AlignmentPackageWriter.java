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

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import org.apache.tools.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;

/**
 * AlignmentPackageWriter reads files from a specified directory and
 * put it in an alignment package file.
 */

public class AlignmentPackageWriter
{
    static public final int BUFSIZE = 4096;

    // zip output stream
    private ZipOutputStream m_out;
    
    // project tmp file storage directory
    private File m_projectTmpDirectory;


    /**
     * Constructor. 
     *
     * @param p_out OutputStream of the package
     * @param p_projectTmpDirectory The alignment project temp file
     * storage directory. This directory contains files to add to the
     * package.
     */
    public AlignmentPackageWriter(
        OutputStream p_out, File p_projectTmpDirectory)
    {
        m_out = new ZipOutputStream(new BufferedOutputStream(p_out));
        m_projectTmpDirectory = p_projectTmpDirectory;
    }
    

    /**
     * Add a specified file to the package
     *
     * @param p_fileName File name to add. Only the file name, not the
     *                   path name.
     */
    public void addFileToPackage(String p_fileName)
        throws Exception
    {
        m_out.putNextEntry(new ZipEntry(p_fileName));
        
        BufferedInputStream in = new BufferedInputStream(
            new FileInputStream(
                new File(m_projectTmpDirectory, p_fileName)));

        byte[] buf = new byte[BUFSIZE];
        int readLen = 0;
        
        while((readLen = in.read(buf, 0, BUFSIZE)) != -1)
        {
            m_out.write(buf, 0, readLen);
        }
        in.close();

        m_out.closeEntry();
    }


    public void close()
        throws Exception
    {
        m_out.close();
    }
    
}
