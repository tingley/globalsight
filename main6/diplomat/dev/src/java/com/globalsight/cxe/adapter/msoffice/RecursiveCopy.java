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

package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * This class mimic the behavior of "cp sourceDir/. destDir" in UNIX
 * or "xcopy /e /i sourceDir destDir" in Windows.
 */

public class RecursiveCopy
{
    static private final int BUFFER_SIZE = 16384;

    private int m_fileNum = 0;

    public void copy(String p_sourceDir, String p_destDir)
        throws Exception
    {
        copy(p_sourceDir, p_destDir, new ArrayList());
    }

    /**
     * Copies the source directory contents to the destination directory.
     * Excludes any directories that are passed in the ArrayList.
     * This is just an array of strings that are a relative or
     * absolute path name.
     */
    public void copy(String p_sourceDir, String p_destDir,
        ArrayList p_excludedDirs)
        throws Exception
    {
        File sourceDir = new File(p_sourceDir);
        File destDir = new File(p_destDir);

        if (!(sourceDir.isDirectory() && destDir.isDirectory()))
        {
            throw new Exception(p_sourceDir + " and/or " + p_destDir +
                " are not directories or don't exist");
        }

        File[] fileEntries = sourceDir.listFiles();

        doCopy(fileEntries, destDir, p_excludedDirs);
    }

    private void doCopy(File[] p_fileEntries, File p_destDir,
        ArrayList p_excludeDirs)
        throws Exception
    {
        int size = p_fileEntries.length;
        for (int i = 0; i < size; i++)
        {
            if (p_fileEntries[i].isDirectory())
            {
                if (notExcludedDirectory(p_fileEntries[i], p_excludeDirs))
                {
                    File destDir = new File(p_destDir, p_fileEntries[i].getName());
                    destDir.mkdir();
                    File[] fileEntries = p_fileEntries[i].listFiles();
                    doCopy(fileEntries, destDir, p_excludeDirs);
                }
                // else  - do not copy
            }
            else
            {
                copyFile(p_fileEntries[i], p_destDir);
            }
        }
    }


    private boolean notExcludedDirectory(File p_file, ArrayList p_excludeDirs)
    {
        boolean notExcluded = true;

        if (p_excludeDirs != null && p_excludeDirs.size() > 0)
        {
            for (int i=0; i < p_excludeDirs.size()  && notExcluded; i++)
            {
                // check if this directory should be excluded
                // doesn't check if they are "equal" because the excludeDirs
                // may be relative and not absolute
                if (p_file.getAbsolutePath().indexOf((String)p_excludeDirs.get(i)) != -1)
                {
                    notExcluded = false;
                }
            }
        }

        return notExcluded;
    }

    private void copyFile(File p_sourceFile, File p_destDir)
        throws Exception
    {
        File destFile = new File(p_destDir, p_sourceFile.getName());
        FileInputStream input = new FileInputStream(p_sourceFile);
        FileOutputStream output = new FileOutputStream(destFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;

        while ((size = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, size);
        }
        input.close();
        output.close();
    }


    static public void main(String[] args)
        throws Exception
    {
        RecursiveCopy copy = new RecursiveCopy();
        copy.copy(args[0], args[1]);
    }
}
