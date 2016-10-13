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
package com.globalsight.diplomat.adapter.visualSourceSafe;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.ProcessRunner;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.cxe.adapter.CxeProcessor;
import com.globalsight.cxe.message.CxeMessage;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.net.URL;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
* The VssCheckOut is a pre processor that can be plugged into the
* FileSystemTargetAdapter to check out a target file from Visual Source Safe
* after l10n, and create any necessary projects in VSS
*/
public class VssCheckOut extends VssProcessor
{
    private static String OUTFILE="/VssCheckOut.out";
    private static String ERRFILE="/VssCheckOut.err";

    public VssCheckOut()
    {
        super();

        try
        {
            //set the log directory for the error files
            String logDirectory = SystemConfiguration.getInstance().
                                     getStringParameter(
                                         SystemConfigParamNames.CXE_LOGGING_DIRECTORY);
            //set the absolute path
            OUTFILE = logDirectory + OUTFILE;
            ERRFILE = logDirectory + ERRFILE;
        } catch (Exception e)
        {
            Logger.getLogger().println(Logger.WARNING,
                        "The log directory couldn't be found in the system configuration " +
                        " for VSS logging purposes.");
        }
    }


    /**
    * Checks the given file out from VSS, and creates any necessary projects
    */
    public CxeMessage process (CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        try
        {
            //Assume the file will be written out to the docs directory in
            //in a locale specific directory.
            m_dirNames = new Vector();
            parseEventFlowXml();
            makeVssProjects();
            checkOutFile();
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,"VssCheckOut: exception when processing. ", e);
        }
        
        //since no eventflowxml or content was modified, just return the same msg
        return m_cxeMessage;
    }

    /**For each relative directory name in m_dirNames, it will create a sub-project in VSS*/
    private void makeVssProjects()
    throws Exception
    {
        StringBuffer currentDir = new StringBuffer();

        for (int i=0; i < m_dirNames.size(); i++)
        {
            currentDir.append(m_dirNames.get(i));
            String command = "VssMkProj.bat " + currentDir.toString();
            execute(command, OUTFILE,ERRFILE);

            if (i + 1 < m_dirNames.size())
                currentDir.append("/");
        }

        m_relativeProject = currentDir.toString();
    }

    /** Actually checks the file out from VSS*/
    private void checkOutFile()
    throws Exception
    {
        StringBuffer command = new StringBuffer("VssCheckOut.bat ");
        command.append(m_relativeProject);
        command.append(" ");
        command.append(m_realDirectory);
        command.append(" ");
        command.append(m_baseName);
        Logger.getLogger().println(Logger.INFO,"VssCheckOut executing: " + command.toString());
        execute(command.toString(), OUTFILE,ERRFILE);
    }
}

