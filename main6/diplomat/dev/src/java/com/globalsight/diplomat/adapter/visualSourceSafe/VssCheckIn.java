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

import com.globalsight.cxe.adapter.CxeProcessor;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.ProcessRunner;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

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
* The VssCheckIn is a post processor that can be plugged into the
* FileSystemTargetAdapter to add or check in a localized target file
* to Visual Source Safe
*/
public class VssCheckIn extends VssProcessor
{
    private static String OUTFILE="/VssCheckIn.out";
    private static String ERRFILE="/VssCheckIn.err";

    public VssCheckIn()
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
    * Adds the given file to VSS or checks it back in.
    */
    public CxeMessage process(CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        try
        {
            //Assume the file will be written out to the docs directory in
            //in a locale specific directory.
            m_dirNames = new Vector();
            parseEventFlowXml();
            determineRelativeProject();
            checkInFile();
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,"VssCheckIn: exception when processing. ", e);
        }
        
        //since the eventflow xml and content was not modified, just
        //return the original message
        return m_cxeMessage;
    }

    /**Figure out the relativeProject*/
    private void determineRelativeProject()
    throws Exception
    {
        StringBuffer currentDir = new StringBuffer();

        for (int i=0; i < m_dirNames.size(); i++)
        {
            currentDir.append(m_dirNames.get(i));
            if (i + 1 < m_dirNames.size())
                currentDir.append("/");
        }
        m_relativeProject = currentDir.toString();
    }

    /** Actually checks the file in to VSS or adds it to VSS*/
    private void checkInFile()
    throws Exception
    {
        StringBuffer command = new StringBuffer("VssCheckIn.bat ");
        command.append(m_relativeProject);
        command.append(" ");
        command.append(m_realDirectory);
        command.append(" ");
        command.append(m_baseName);
        Logger.getLogger().println(Logger.INFO,"VssCheckIn executing: " + command.toString());
        execute(command.toString(), OUTFILE, ERRFILE);
    }
}

