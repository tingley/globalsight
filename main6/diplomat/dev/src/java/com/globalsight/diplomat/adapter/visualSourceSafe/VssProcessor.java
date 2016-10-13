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
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.adapter.CxeProcessor;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
* The VssProcessor is a pre processor that can be plugged into the
* FileSystemTargetAdapter to check out a target file from Visual Source Safe
* after l10n, and create any necessary projects in VSS
*/
public abstract class VssProcessor implements CxeProcessor
{
    private static final String PROPERTY_FILE = "/properties/VssProcessor.properties";
    private static final String PROP_DOCSDIR="CxeDocsDir";

    private static final char UNIX_SEPARATOR = '/';
    private static final char WIN_SEPARATOR = '\\';

    protected CxeMessage m_cxeMessage = null;

    /** Construct and initialize a VssProcessor*/
    public VssProcessor()
    {
        initialize();
    }

    public abstract CxeMessage process (CxeMessage p_cxeMessage);

    /** Parses the EventFlowXml to determine the full path and list of subdirectories
    * in the relative path name that the file system target adapter used*/
    protected void parseEventFlowXml()
    throws Exception
    {
        StringReader sr = new StringReader(m_cxeMessage.getEventFlowXml());
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
        parser.parse(is);
        Element elem = parser.getDocument().getDocumentElement();
        String filename = readOriginalFilename(elem);
        String exportDirectory = findExportDirectory(elem);

        m_dirNames.add(exportDirectory);
        m_fullpath = m_docsDir + File.separator + exportDirectory + File.separator;

        //take the filename minus the leading directory name
        int separatorIndex = filename.indexOf(File.separator);
        String truncatedFilename = filename.substring(separatorIndex + 1);
        addAdditionalSubDirs(truncatedFilename);
        m_fullpath += truncatedFilename; //complete the full path file name

        setRealDirectoryAndBaseName();
        Logger.getLogger().println(Logger.INFO,"VssProcessor: FileSystemAdapter used fullpath:" + m_fullpath);
    }

    /**
     * Takes in the directory name and replaces "/" and "\" with the appropriate
            * File.separator for the current operating system. Also removes the leading
            * and trailing separators.
            */
    protected String makeDirectoryNameOperatingSystemSafe(String p_dirName)
    {
        String newName = p_dirName.replace(UNIX_SEPARATOR,File.separatorChar);
        newName = newName.replace(WIN_SEPARATOR,File.separatorChar);
        if (newName.startsWith(File.separator))
            newName = newName.substring(1);

        if (newName.endsWith(File.separator))
            newName = newName.substring(0,newName.length() - 1);

        return newName;
    }

    /** Adds each sub directory name to the vector m_dirNames */
    protected void addAdditionalSubDirs(String p_relativeFileName)
    throws Exception
    {
        StringTokenizer st = new StringTokenizer(p_relativeFileName,File.separator);
        while (st.hasMoreTokens())
        {
            String dirName = st.nextToken();
            if (st.hasMoreTokens())
            {
                //don't add the final basename of the file, only intermediate directories
                m_dirNames.add(dirName);
            }
        }
    }

    /** Searches through the EventFlowXml Element for the original file name*/
    private String readOriginalFilename(Element p_element)
    throws Exception
    {
        NodeList nl = p_element.getElementsByTagName("source");
        Element sourceElement = (Element) nl.item(0);
        NodeList attributeList = sourceElement.getElementsByTagName("da");
        String filename = null;

        for (int k=0; k < attributeList.getLength();k++)
        {
            Element attrElement = (Element)attributeList.item(k);
            String name = attrElement.getAttribute("name");
            if (name != null && name.equals("Filename"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                filename = valElement.getFirstChild().getNodeValue();
                Logger.getLogger().println(Logger.DEBUG_D, "VssProcessor: original file:" + filename);
                break;
            }
        }
        return filename;
    }

    /** Searches through the EventFlowXml Element for the export directory*/
    private String findExportDirectory(Element p_element)
    throws Exception
    {
        //get the target Element
        NodeList nl = p_element.getElementsByTagName("target");
        Element targetElement = (Element) nl.item(0);
        NodeList attributeList = targetElement.getElementsByTagName("da");
        String exportDirectory = null;

        for (int k=0;k<attributeList.getLength();k++)
        {
            Element attrElement = (Element)attributeList.item(k);
            String name = attrElement.getAttribute("name");
            if (name != null && name.equals("ExportDirectory"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                exportDirectory = makeDirectoryNameOperatingSystemSafe(valElement.getFirstChild().getNodeValue());
                Logger.getLogger().println(Logger.DEBUG_D, "export dir:" + exportDirectory);
                break;
            }
        }

        return exportDirectory;
    }


    /**
    * Gets the Property file path name as a System Resource
    * @param propertyFile basename of the property file
    * @throws FileNotFoundException
    * @return String -- propety file path name
     * @throws URISyntaxException 
    */
    protected String getPropertyFilePath(String p_propertyFile)
    throws FileNotFoundException, URISyntaxException
    {
        URL url = VssProcessor.class.getResource(p_propertyFile);
        if (url == null)
            throw new FileNotFoundException("Property file " + p_propertyFile + " not found");
        return url.toURI().getPath();
    }


    /** Initializes the processor by reading in some necessary properties to configure
    *the processor*/
    protected void initialize()
    {
        try
        {
            Properties props = new Properties();
            props.load(new FileInputStream(getPropertyFilePath(PROPERTY_FILE)));
            m_docsDir = props.getProperty(PROP_DOCSDIR);
        }
        catch (Exception e)
        {
            Logger.getLogger().printStackTrace(Logger.ERROR,"VssProcessor: failure to initialize from property file.",e);
        }
    }

    /**Execute the given command*/
    protected void execute (String p_command, String p_outFile, String p_errFile)
    throws Exception
    {
        Logger.getLogger().println(Logger.INFO, "Executing command: " + p_command);
        ProcessRunner pr = new ProcessRunner(p_command, p_outFile, p_errFile, true);
        Thread t = new Thread(pr);
        t.start();
        try
        {
            t.join();
        }
        catch (InterruptedException ie)
        {
        }
    }

    /** Sets the real directory to use and the basename of the file*/
    private void setRealDirectoryAndBaseName()
    {
        int separatorIndex = m_fullpath.lastIndexOf(File.separator);
        m_baseName = m_fullpath.substring(separatorIndex + 1);
        m_realDirectory = m_fullpath.substring(0,separatorIndex);
    }

    protected Vector m_dirNames = null;
    protected String m_fullpath = null;
    protected String m_relativeProject = null;
    protected String m_realDirectory = null;
    protected String m_baseName = null;
    protected String m_docsDir = null;
}

