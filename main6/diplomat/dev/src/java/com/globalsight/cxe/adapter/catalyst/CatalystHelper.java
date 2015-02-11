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
package com.globalsight.cxe.adapter.catalyst;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Hashtable;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessRunner;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * The CatalystHelper is used to compile and decompile Translator Tool
 * Kit (TTK) files.  Many separately imported files in a batch go into
 * one TTK per target locale.
 *
 * This class makes use of the new EventFlowXml syntax to let CAP know
 * to use the alternate files for the target page of each workflow.
 */
public class CatalystHelper
{
    //CONSTANTS

    /**
     * The name of the category in the event flow xml for any data specific
     * to the Catalyst Adapter.
     */
    static public final String EFXML_DA_CATEGORY_NAME = "Catalyst";

    /*****************************/
    /*** Private Member Data ***/
    /*****************************/
    private String m_eventFlowXml = null;
    private CxeMessage m_cxeMessage = null;
    private long m_originalFileSize = 0;
    private String m_workingDir = null;
    private String m_fileName = null;
    private EventFlowXmlParser m_parser = null;
    private org.apache.log4j.Logger m_logger = null;
    private boolean m_sendTTkNow = false;
    private String[] m_targetLocales = null;
    private SystemConfiguration m_config = null;
    private File[] m_targetFiles = null;

    //hashtable of inprogress TTKs. Lost on restart.
    static private Hashtable s_inProgressTTKs = new Hashtable();

    //hashtable of Catalyst Locale mappings
    static private Hashtable s_catalystLocales = new Hashtable();

    public String getCatalystLocale(String l)
    {
        return (String)s_catalystLocales.get(l);
    }

    /**
     * Constructor
     *
     * @param p_workingDir -- the main working directory where the
     * conversion server looks for files
     * @param p_eventFlowXml -- the EventFlowXml
     * @param p_content -- the content (whether GXML or Native)
     */
    public CatalystHelper(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger,
        SystemConfiguration p_config)
        throws Exception
    {
        m_logger = p_logger;
        m_config= p_config;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_cxeMessage = p_cxeMessage;
        m_parser = new EventFlowXmlParser();
    }

    /*******************************************************/
    /*** Public methods for a user of a DesktopAppHelper ***/
    /*******************************************************/

    /**
     * Gets the event flow XML. If called, after a call to
     * convertNativeToXml() or convertXmlToNative(), then the
     * EventFlowXml may have been modified.
     *
     * @return eventFlowXml
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * Adds the file to the TTKs, or creates the TTKs if they do not exit.
     *
     * @return FileMessageData containing the word output
     * @throws CatalystAdapterException
     */
    public MessageData createTTKs()
        throws CatalystAdapterException
    {
        try
        {
            parseEventFlowXml();
            determineWorkingDir();
            determineBatchStatus();
            determineTargetLocales();
            writeContentToTmpDir();
            appendFileToTTKs();
            modifyEventFlowXmlForImport();

            if (m_sendTTkNow)
            {
                //replace the messagedata content with the source TTK file
                FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
                StringBuffer srcLangTTK = new StringBuffer(m_workingDir);
                srcLangTTK.append(m_targetLocales[0]);
                srcLangTTK.append(".ttk");
                fmd.copyFrom(new File(srcLangTTK.toString()));
                return fmd;
            }
            else
            {
                return null;
            }
        }
        catch (CatalystAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            m_logger.error("Problem creating TTK", e);
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new CatalystAdapterException("Import", errorArgs, e);
        }
    }

    /**
     * Extracts the TTK and then exports each file separately
     *
     * @return AdapterResult[] containing many results
     * @throws CatalystAdapterException
     */
    public AdapterResult[] extractTTK()
        throws CatalystAdapterException
    {
        AdapterResult[] results = null;

        try
        {
            parseEventFlowXml();
            determineWorkingDir();
            decompressTTK();
            results = exportExtractedFiles();
            return results;
        }
        catch (CatalystAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            m_logger.error("Problem extracting TTK", e);
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new CatalystAdapterException("Import", errorArgs, e);
        }
    }



    /***********************/
    /** Private Methods **/
    /***********************/

    /**
     * Sets the list of target locales based on the l10nprofile.
     * The internal list of target locales is based on iso codes
     */
    private void determineTargetLocales()
        throws Exception
    {
        long id = Long.parseLong(m_parser.getL10nProfileId());
        L10nProfile l10nProfile = ServerProxy.getProjectHandler()
            .getL10nProfile(id);
        GlobalSightLocale[] locs = l10nProfile.getTargetLocales();
        m_targetLocales = new String[locs.length + 1];
        m_targetLocales[0] = m_parser.getSourceLocale();
        for (int i = 0; i < locs.length; i++)
        {
            GlobalSightLocale gsl = locs[i];
            m_targetLocales[i + 1] = gsl.getLocale().toString();
        }
    }


    /**
     * Based on the batch status, determines whether the TTK should be created or
     * merely appended to. Also determines whether it is time to send the TTK on.
     */
    private void determineBatchStatus()
    {
        //see if the TTK exists already
        String key = m_parser.getBatchId();
        Integer numFiles = (Integer) s_inProgressTTKs.get(key);

        if (numFiles == null)
        {
            int num = m_parser.getImportDocPageCount().intValue() - 1;
            if (num > 0)
            {
                s_inProgressTTKs.put(key, new Integer(num));
                m_sendTTkNow = false;
            }
            else
            {
                m_sendTTkNow = true;
            }
        }
        else
        {
            int num = numFiles.intValue() - 1;
            if (num == 0)
            {
                s_inProgressTTKs.remove(key);
                m_sendTTkNow = true;
            }
            else
            {
                m_sendTTkNow = false;
                s_inProgressTTKs.put(key, new Integer(num));
            }
        }

        m_logger.debug("m_sendTTkNow=" + m_sendTTkNow);
    }


    /**
     * Uses Catalyst to append the given file to each target locale TTK.
     * The TTK is created if it does not exist.
     */
    private void appendFileToTTKs()
        throws Exception
    {
        String catalystUser = m_config.getStringParameter("username");
        String fileListName = createFileList();

        for (int i = 0; i < m_targetLocales.length; i++)
        {
            StringBuffer cl = new StringBuffer("catalyst /M \"");
            cl.append(catalystUser);
            cl.append("\" TTKName:\"");
            cl.append(m_workingDir);
            cl.append(m_targetLocales[i]);
            cl.append(".ttk\" FileListFile:\"");
            cl.append(fileListName);
            cl.append("\" Sourcelang:\"");
            cl.append(getCatalystLocale(m_parser.getSourceLocale()));
            cl.append("\" Targetlang:\"");
            cl.append(getCatalystLocale(m_targetLocales[i]));
            cl.append("\"");

            execute(cl.toString());
        }
    }

    /**
     * Executes the command and writes output to CAP.log
     *
     * @param p_command command line
     * @exception Exception
     */
    protected void execute (String p_command)
        throws Exception
    {
        m_logger.debug("Executing command: " + p_command);

        String outFile = m_workingDir + "catalyst.out";
        String errFile = m_workingDir + "catalyst.err";
        PrintStream err = new PrintStream(System.err);
        PrintStream out = new PrintStream(System.out);
        ProcessRunner pr = new ProcessRunner(p_command, out, err);
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



    /**
     * Creates a file list containing the name of the content file
     *
     * @return name of the file list
     */
    private String createFileList()
        throws Exception
    {
        StringBuffer fileListName = new StringBuffer(m_workingDir);
        fileListName.append("fileList.txt");
        File fileList = new File(fileListName.toString());
        FileWriter fw = new FileWriter(fileList);
        String fileName = m_fileName + "\r\n";
        fw.write(fileName);
        fw.close();
        return fileListName.toString();
    }

    /**
     * Creates a full path name to use for the TTK creation based on the batchId
     * for this import batch. Uses that as the working dir.
     */
    private void determineWorkingDir()
        throws Exception
    {
        String sep = File.separator;
        String fileStoreDir = SystemConfiguration.getInstance().getStringParameter(
            SystemConfigParamNames.FILE_STORAGE_DIR);
        StringBuffer d = new StringBuffer(fileStoreDir);
        d.append(sep).append("Alchemy").append(sep);
        d.append(m_parser.getBatchId());
        d.append(sep);
        m_workingDir = d.toString();
        File f = new File(m_workingDir);
        if (f.exists() == false)
        {
            f.mkdirs();
        }
    }

    /**
     * Writes the content to the tmp dir
     */
    private void writeContentToTmpDir()
        throws CatalystAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(m_workingDir);
            fileName.append("content"); //put files in 'content' subdir
            File contentDir = new File(fileName.toString());
            contentDir.mkdirs();
            fileName.append(File.separator);
            fileName.append(baseName(m_parser.getDisplayName()));
            m_fileName = fileName.toString();
            m_logger.info("Writing content file out to: " + m_fileName);
            FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
            File targetFile = new File (m_fileName);
            fmd.copyTo(targetFile);
        }
        catch (Exception e)
        {
            m_logger.error("Failed to write content to workingdir:", e);
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new CatalystAdapterException("Import", errorArgs, e);
        }
    }

    private void decompressTTK()
        throws Exception
    {
        //first write the TTK out to a working dir
        StringBuffer s = new StringBuffer(m_workingDir);
        s.append("export");
        String exportDir = s.toString();
        s.append(File.separator);
        s.append("ttk");
        String ttkdir = s.toString();
        File td = new File(ttkdir);
        td.mkdirs();

        String locale = m_parser.getTargetLocale();
        StringBuffer exportTtk = new StringBuffer(exportDir);
        exportTtk.append(File.separator).append(locale).append(".ttk");
        m_cxeMessage.getMessageData().copyTo(new File(exportTtk.toString()));

        StringBuffer cl = new StringBuffer("catalyst /E \"");
        cl.append(exportTtk.toString());
        cl.append("\" \"");
        cl.append(ttkdir);
        cl.append("\"");

        execute(cl.toString());

        m_targetFiles = td.listFiles(new ExtractedFilenameFilter());
        m_logger.debug("There are " + m_targetFiles.length + " target files.");
    }

    private AdapterResult[] exportExtractedFiles()
        throws Exception
    {
        if (m_targetFiles == null)
        {
            return null;
        }

        AdapterResult[] results = new AdapterResult[m_targetFiles.length];
        for (int i = 0; i < m_targetFiles.length;i++)
        {
            modifyEventFlowXmlForExport(m_targetFiles[i].getAbsolutePath());
            String pme = m_parser.getPostMergeEvent();

            m_logger.debug("Making pme: " + pme + " for file: " +
                m_targetFiles[i].getAbsolutePath());

            CxeMessageType postMergeEvent = CxeMessageType.getCxeMessageType(pme);

            CxeMessage msg = new CxeMessage(postMergeEvent);
            msg.setEventFlowXml(m_eventFlowXml);
            msg.setParameters(m_cxeMessage.getParameters());
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(m_targetFiles[i]);
            msg.setMessageData(fmd);
            results[i] = new AdapterResult(msg);
        }

        return results;
    }

    /**
     * Returns the basename of the given filename
     *
     * @param p_filename
     * @return
     */
    private String baseName(String p_filename)
    {
        int forwardSlashIndex = p_filename.lastIndexOf('/');
        int backwardSlashIndex = p_filename.lastIndexOf('\\');
        int lastSeparatorindex = (forwardSlashIndex > backwardSlashIndex) ?
            forwardSlashIndex : backwardSlashIndex;
        String baseFileName = p_filename.substring(lastSeparatorindex + 1);
        return baseFileName;
    }

    /**
     * Modifies the EventFlowXml during import. This does the following changes
     * 1) changes the postMergeEvent to be the value of this.getPostMergeEvent()
     * 2) changes the format type to xml
     * 3) saves all the above data in the EventFlowXml as da/dv pairs
     * NOTE: The specified encoding in the EventFlowXml is left intact (that is
     * the user can specify ISO,UTF8,etc. for the XML that Noonetime generates)
     * <br>
     * @throws Exception
     */
    protected void modifyEventFlowXmlForImport()
        throws Exception
    {
        //now save all the data to the EventFlowXml
        saveDataToEventFlowXml();

        //set the page number and page count to 1
        m_parser.setImportPageCount("1");
        m_parser.setImportPageNumber("1");
        m_parser.setImportDocPageCount("1");
        m_parser.setImportDocPageNumber("1");

        //reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();

        Logger.writeDebugFile("catalyst_ef.xml", m_eventFlowXml);
    }

    /**
     * Saves some data to the event flow xml by adding it as attribute/value
     * pairs to the category "CatalystAdapter".
     * <br>
     * @throws Exception
     */
    private void saveDataToEventFlowXml()
        throws Exception
    {
        for (int i = 1; i < m_targetLocales.length; i++)
        {
            String pageName = m_workingDir + m_targetLocales[i] + ".ttk";
            m_parser.addAlternateTargetPage(m_targetLocales[i], pageName);
        }

        //modify the display name
        String oldDisplayName = m_parser.getDisplayName();
        String newDisplayName = m_parser.getJobName() + ".ttk";
        m_parser.setDisplayName(newDisplayName);

        //save the old display name
        Element category = m_parser.addCategory(EFXML_DA_CATEGORY_NAME);
        String values[] = {oldDisplayName};
        Element da = m_parser.makeEventFlowXmlDaElement("originalDisplayName", values);
        category.appendChild(da);
    }

    /**
     * Modifies the EventFlowXml during export. Also restores the value
     * of some internal data based off of values saved to the EFXML.
     * This restores the EventFlowXml to the way it was before coming
     * to the Catalyst source adapter.
     * <br>
     * @throws Exception
     */
    private void modifyEventFlowXmlForExport(String p_targetFile)
        throws Exception
    {
        String targetBaseName = this.baseName(p_targetFile);
        Element categoryElement = m_parser.getCategory(EFXML_DA_CATEGORY_NAME);
        String originalDisplayName = m_parser.getCategoryDaValue(
            categoryElement, "originalDisplayName")[0];
        int sidx = originalDisplayName.lastIndexOf("/");
        int bsidx = originalDisplayName.lastIndexOf("\\");
        int idx = (sidx > bsidx) ? sidx : bsidx;
        String origDir = originalDisplayName.substring(0, idx + 1);
        String newDisplayName = origDir + targetBaseName;
        m_logger.debug("Setting new dispname: " + newDisplayName);
        m_parser.setDisplayName(newDisplayName);

        //now set the target filename
        Element targetElement= m_parser.getSingleElement("target");
        NodeList nl = targetElement.getElementsByTagName("da");
        for (int i = 0; i < nl.getLength(); i++)
        {
            Element daElement = (Element) nl.item(i);
            if (daElement.getAttribute("name").equals("Filename"))
            {
                NodeList dvs = daElement.getElementsByTagName("dv");
                for (int j = 0; j < dvs.getLength(); j++)
                {
                    Element dv = (Element)dvs.item(j);
                    m_logger.debug("Replacing " + dv.getFirstChild().getNodeValue()
                        + " with " + targetBaseName);
                    dv.getFirstChild().setNodeValue(targetBaseName);
                }
                break;
            }
        }

        //reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();

        Logger.writeDebugFile("catm_ef.xml",m_eventFlowXml);
    }


    /**
     * Parses the EventFlowXml to set internal values
     * <br>
     * @throws CatalystAdapterException
     */
    private void parseEventFlowXml()
        throws CatalystAdapterException
    {
        try
        {
            m_parser.parse(m_eventFlowXml);
        }
        catch (Exception e)
        {
            m_logger.error("Unable to parse EventFlowXml. ", e);
            String[] errorArgs = null;
            throw new CatalystAdapterException("Unexpected", errorArgs, e);
        }
    }

    /******************************************/
    /*** Mutators/Accessors for member data ***/
    /******************************************/

    /**
     * Returns the working directory of the conversion server
     */
    protected String getWorkingDir()
    {
        return m_workingDir;
    }

    /**
     * Returns the EventFlowXml Parser
     */
    protected EventFlowXmlParser getEventFlowXmlParser()
    {
        return m_parser;
    }


    //load all the catalyst locales
    static
    {
        s_catalystLocales.put("en_US", "English (United States)");
        s_catalystLocales.put("fr_FR", "French (Swiss)");
        s_catalystLocales.put("es_ES", "Spanish (Mexican)");
        s_catalystLocales.put("de_DE", "German (Austrian)");
    }

    /**
     * Avoids text files created by catalyst when
     * extracting
     */
    private class ExtractedFilenameFilter
        implements FilenameFilter
    {
        public ExtractedFilenameFilter()
        {}

        public boolean accept(File a, String b)
        {
            if (b.endsWith(".txt"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
}
