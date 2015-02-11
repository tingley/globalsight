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
package com.globalsight.diplomat.adapter.clearcase;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.adapter.CxeProcessor;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.ProcessRunner;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* The ClearCaseCheckOut is a pre processor that can be plugged into the
* FileSystemTargetAdapter to check out a target file from ClearCase
* after localization, or add the file to source control if it does not exist.
*/
public class ClearCaseCheckOut extends ClearCaseProcessor
{
    private static String OUTFILE="/debug/ClearCaseCheckOut.out";
    private static String ERRFILE="/debug/ClearCaseCheckOut.err";

    public ClearCaseCheckOut()
    {
        super();

        try
        {
            //set the log directory for the error files
            String logDirectory = SystemConfiguration.getInstance().
                                     getStringParameter(
                                         "system.logging.directory");
            //set the absolute path
            OUTFILE = logDirectory + OUTFILE;
            ERRFILE = logDirectory + ERRFILE;
        } catch (Exception e)
        {
            Logger.getLogger().println(Logger.WARNING,
                        "The log directory couldn't be found in the system configuration " +
                        " for ClearCase logging purposes.");
        }
    }

    /**
    * Checks the given file out from ClearCase, and creates any necessary projects
    */
    public CxeMessage process(CxeMessage p_msg)
    {
        m_cxeMessage = p_msg;
        try
        {
            //Assume the file will be written out to the docs directory in
            //in a locale specific directory.
            m_dirNames = new ArrayList();
            parseEventFlowXml();
            makeClearCaseActivity();
            makeClearCaseDirectoriesIfNeeded();
            checkOutFileIfElement();
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,"ClearCaseCheckOut: exception when processing. ", e);
        }

        //eventflowxml and content were not modified
        return m_cxeMessage;
    }


    /**
     * Creates the directory structure in clearcase if needed
     * 
     * @exception Exception
     */
    private void makeClearCaseDirectoriesIfNeeded()
    throws Exception
    {
        StringBuffer currentDir = new StringBuffer(m_docsDir);
        currentDir.append(File.separator);
        ArrayList checkedOutDirParents = new ArrayList();
        ArrayList checkedOutDirs = new ArrayList();

        for (int i=0; i < m_dirNames.size(); i++)
        {
            String dirName = (String)m_dirNames.get(i);
            StringBuffer command = new StringBuffer("CC_MkDir.bat \"");
            command.append(currentDir.toString());
            command.append("\" \"");
            command.append(dirName);
            command.append("\" ");
            command.append(m_activityName);
            command.append(" ");
            command.append(m_drive);


            //don't execute the command if the directory already exists
            File f = new File(currentDir.toString() + File.separator + dirName);
            if (f.exists() == false)
            {
                execute(command.toString(), OUTFILE, ERRFILE);
                checkedOutDirParents.add(currentDir.toString());
                checkedOutDirs.add(dirName);
            }
            
            currentDir.append(dirName);
            if (i + 1 < m_dirNames.size())
                currentDir.append(File.separator);
        }

        //add the docs directory and its parent to the list since the docs dir was checked out
        checkedOutDirs.add(m_docsDir);
        checkedOutDirParents.add(m_docsDirParent);

        //now check in the directories that were checked out
        for (int j=0; j < checkedOutDirs.size(); j++)
        {
            String parent = (String) checkedOutDirParents.get(j);
            String child = (String) checkedOutDirs.get(j);
            StringBuffer command = new StringBuffer("CC_CheckIn.bat \"");
            command.append(parent);
            command.append("\" \"");
            command.append(child);
            command.append("\" ");
            command.append(m_activityName);
            command.append(" ");
            command.append(m_drive);

            execute(command.toString(), OUTFILE, ERRFILE);
        }
    }

    /**
     * If the file exists, then it is assumed to be a clearcase
     * element, and it is checked out.
     * If the file does not exist, then nothing happens.
     * 
     * @exception Exception
     */
    private void checkOutFileIfElement()
    throws Exception
    {
        File f = new File (m_fullpath);
        StringBuffer command = null;
        if (f.exists() == false)
        {
            //add the element to source control so that it can be overwritten
            command = new StringBuffer("CC_MkElem.bat \"");            
        }
        else
        {
            command = new StringBuffer("CC_CheckOut.bat \"");            
        }
        
        command.append(m_realDirectory);
        command.append("\" \"");
        command.append(m_baseName);
        command.append("\" ");
        command.append(m_activityName);
        command.append(" ");
        command.append(m_drive);

        execute(command.toString(), OUTFILE,ERRFILE);
    }

    /**
     * Makes the activity (using the jobname) in ClearCase
     * Also sets that as the current activity
     * 
     * @exception Exception
     */
    private void makeClearCaseActivity()
    throws Exception
    {
        StringBuffer command = new StringBuffer("CC_MkActivity.bat \"");
        command.append(m_docsDir);
        command.append("\" ");
        command.append(m_activityName);
        command.append(" ");
        command.append(m_drive);
        execute(command.toString(), OUTFILE, ERRFILE);
    }
}

