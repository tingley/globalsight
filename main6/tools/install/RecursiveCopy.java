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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;



public class RecursiveCopy
    extends installer.EventBroadcaster
{
    static private final int BUFFER_SIZE = 16384;

    private int m_fileNum = 0;
    
    public void copy(String p_sourceDir, String p_destDir)
        throws Exception
    {
        copy(p_sourceDir, p_destDir, new ArrayList());
    }

    public void copy(String p_sourceDir, String p_destDir,
                     ArrayList p_excludedDirs)
        throws Exception
    {
        File sourceDir = new File(p_sourceDir);
        File destDir = new File(p_destDir);
        
        if(!(sourceDir.isDirectory() && destDir.isDirectory()))
        {
            throw new Exception(p_sourceDir + " and/or " + p_destDir 
                                + " are not directories or don't exist");
        }
        
        File[] fileEntries = sourceDir.listFiles();

        m_fileNum = 0;
        System.out.println("Copying " + p_sourceDir + " -> " + p_destDir);
        doCopy(fileEntries, destDir, p_excludedDirs);
        System.out.println("\nDone. " + m_fileNum + " files.");
    }

    public void copyFile(String p_sourceFileName, String p_destDir)
        throws Exception
    {
        File f = new File(p_sourceFileName);
        File destDir = new File(p_destDir);
        File[] entries = {f};
        doCopy(entries, destDir, new ArrayList());
    }
    
    public void copyFile(String p_sourceFileName, String p_destDir, String p_destFileName)
            throws Exception
    {
        File f = new File(p_sourceFileName);
        File destDir = new File(p_destDir);
        
        copyFile(f, destDir, p_destFileName);
    }
    
    private void doCopy(File[] p_fileEntries, File p_destDir,
                        ArrayList p_excludeDirs)
        throws Exception
    {
        int size = p_fileEntries.length;
        for(int i = 0; i < size; i++)
        {
            if(p_fileEntries[i].isDirectory())
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
    
    private void copyFile(File p_sourceFile, File p_destDir) throws Exception
    {
        copyFile(p_sourceFile, p_destDir, p_sourceFile.getName());
    }
  
    private void copyFile(File p_sourceFile, File p_destDir, String p_destFileName)
        throws Exception
    {
    	// Notify listeners with the file name
        fireActionEvent(p_sourceFile.getName());

        File destFile = new File(p_destDir, p_destFileName);
        FileInputStream input = new FileInputStream(p_sourceFile);
        FileOutputStream output = new FileOutputStream(destFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;
        
        while((size = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, size);
        }
        input.close();
        output.close();
    

        if(++m_fileNum % 10 == 0)
        {
            if(m_fileNum % 100 == 0)
                System.out.print(":");
            else
                System.out.print(".");
        }
    }


    public int countFiles(String p_sourceDir,
                        ArrayList p_excludeDirs)
        throws Exception
    {
        File sourceDir = new File(p_sourceDir);
        if(!(sourceDir.isDirectory()))
        {
            throw new Exception(p_sourceDir + " is not a directory or doesn't exist");
        }
        
        File[] fileEntries = sourceDir.listFiles();

        int size = fileEntries.length;
        int totalSize = size;
        int subSize = 0;
        for(int i = 0; i < size; i++)
        {
            if(fileEntries[i].isDirectory())
            {
                --totalSize;
                if (notExcludedDirectory(fileEntries[i], p_excludeDirs))
                {
                    subSize += countFiles(fileEntries[i].getPath(), p_excludeDirs);
                }
            }
        }
        return totalSize + subSize;
    }

    static public void main(String[] args)
        throws Exception
    {
        RecursiveCopy copy = new RecursiveCopy();
        copy.copy(args[0], args[1]);
    }
}
