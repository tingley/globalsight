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
package com.globalsight.cxe.adapter.filesystem.autoImport;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.globalsight.util.file.DirectoryMonitor;
import com.globalsight.util.file.FileModificationTimeSnapshot;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
* Prints out a the saved state from automatic import.
*/
public class DirectoryMapPrinter
{
    /**
    * USAGE: DirectoryMapPrinter <pers_file>
    * <br>
    * @throws Exception
    */
    public static void main(String[] args)
    throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("USAGE: DirectoryMapPrinter <pers_file>");
            System.exit(1);
        }
        String persFile = args[0];
        DirectoryMapPrinter dmp = new DirectoryMapPrinter();
        dmp.print(persFile);
    }

    /**
    * Creates a DirectoryMapPrinter to write to the given stream
    * <br>
    * @param the stream to write to
    */
    public DirectoryMapPrinter(PrintStream p)
    {
        m_out = p;
    }
    
    /**
    * Creates a DirectoryMapPrinter to write to the stdout
    */
    public DirectoryMapPrinter()
    {
        m_out = System.out;
    }


    /**
    * Prints out the contents of the specified directory map.
    * <br>
    * @param filename
    * @throws Exception
    */
    public void print(String p_filename)
    throws Exception
    {
        m_out.println("Reading PERS file:  " + p_filename);
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(p_filename));
        m_directoryMap = (DirectoryMap) is.readObject();
        is.close();

        System.out.println("Map file is: " + m_directoryMap.getDirectoryMapFileName());
        java.net.URL url = DirectoryMap.class.getResource(
            m_directoryMap.getDirectoryMapFileName());
        System.out.println("URL is:  " + url);

        ArrayList entries = m_directoryMap.getDirectoryMapEntries();
        m_out.println("There are " + entries.size() + " entries in the directory map.");
        for (int i=0; i < entries.size(); i++)
        {
            printEntry((DirectoryMapEntry)entries.get(i));
        }
    }

    private void printEntry(DirectoryMapEntry p_dme)
    throws Exception
    {
        DirectoryMonitor monitor = p_dme.getDirectoryMonitor();
        m_out.println("----Directory " + monitor.getDirectoryName());
        Object[] currentFiles = monitor.getCurrentFiles().values().toArray();
        m_out.println("\tNumber of files in directory: " + currentFiles.length);

        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss.S z");
        for (int i=0; i < currentFiles.length; i++)
        {
            FileModificationTimeSnapshot snapshot = (FileModificationTimeSnapshot) currentFiles[i];
            Date d  = new Date (snapshot.timeLastModified());
            m_out.println("\t" + snapshot.fileName() + " (" + sdf.format(d) + ")");
        }
    }

    DirectoryMap m_directoryMap = null;
    PrintStream m_out = null;
}
