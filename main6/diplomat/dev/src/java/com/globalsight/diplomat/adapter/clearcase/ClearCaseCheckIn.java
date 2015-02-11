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
* The ClearCaseCheckIn is a post processor that can be plugged into the
* FileSystemTargetAdapter to add or check in a localized target file
* in to a ClearCase Integration View
*/
public class ClearCaseCheckIn extends ClearCaseProcessor
{
    private static String OUTFILE="/debug/ClearCaseCheckIn.out";
    private static String ERRFILE="/debug/ClearCaseCheckIn.err";

    private String m_relativeProject = null;

    public ClearCaseCheckIn()
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
    * Adds the given file to ClearCase or checks it back in.
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
            checkInFile();
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,"ClearCaseCheckIn: exception when processing. ", e);
        }
        
        return m_cxeMessage;
    }

    /** Actually checks the file in to ClearCase or adds it to ClearCase*/
    private void checkInFile()
    throws Exception
    {
        StringBuffer command = new StringBuffer("CC_CheckIn.bat \"");
        command.append(m_realDirectory);
        command.append("\" \"");
        command.append(m_baseName);
        command.append("\" ");
        command.append(m_activityName);
        command.append(" ");
        command.append(m_drive);
        execute(command.toString(), OUTFILE, ERRFILE);
    }
}

