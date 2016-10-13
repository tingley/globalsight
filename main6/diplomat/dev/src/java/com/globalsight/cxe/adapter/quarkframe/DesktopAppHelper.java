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
package com.globalsight.cxe.adapter.quarkframe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.w3c.dom.Element;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;

/**
 * The DesktopAppHelper is an abstract base class intended to be subclassed to
 * handle converting native format files (Word, Quark, Frame, etc.) to XML and
 * back.
 */
public abstract class DesktopAppHelper
{
    /**
     * The name of the category in the event flow xml for any data specific to
     * the DesktopApplicationAdapter.
     */
    public static final String EFXML_DA_CATEGORY_NAME = "DesktopApplicationAdapter";
    private static final int BYTES_IN_100KB = 102400;
    // time to sleep between detection attempts
    private static final int SLEEP_TIME = 2000;
    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    /*****************************/
    /*** Private Member Data ***/
    /*****************************/
    private String m_eventFlowXml = null;
    private CxeMessage m_cxeMessage = null;
    private int m_originalFileSize = 0;
    private String m_workingDir = null;
    private String m_safeBaseFileName = null;
    protected EventFlowXmlParser m_parser = null;
    protected org.apache.log4j.Logger m_logger = null;
    // time in milliseconds to wait for conversion to finish
    private long m_conversionWaitTime = 0;
    // time in milliseconds to wait for conversion to finish
    private long m_sleepTime = SLEEP_TIME;

    // related to attempts to detect the end of the conversion process
    private int m_numTries = 0;
    private int m_maxTries = 0;

    private boolean m_isImport = true;
    private String m_saveDir = null;
    private String m_convDir = null;

    // formats to handle
    protected static final String FRAME5 = "frame5";
    protected static final String FRAME6 = "frame6";
    protected static final String FRAME7 = "frame7";
    protected static final String FRAME9 = "framemaker9";

    /**
     * Constructor for use by sub classes.
     * 
     * @param p_workingDir
     *            -- the main working directory where the conversion server
     *            looks for files
     * @param p_eventFlowXml
     *            -- the EventFlowXml
     * @param p_content
     *            -- the content (whether GXML or Native)
     */
    protected DesktopAppHelper(String p_workingDir, CxeMessage p_cxeMessage,
            org.apache.log4j.Logger p_logger)
    {
        m_logger = p_logger;
        m_workingDir = p_workingDir;
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = m_cxeMessage.getEventFlowXml();
        m_parser = new EventFlowXmlParser(m_eventFlowXml);
        readProperties();
    }

    /*******************************************************/
    /*** Public methods for a user of a DesktopAppHelper ***/
    /*******************************************************/

    /**
     * Gets the event flow XML. If called, after a call to convertNativeToXml()
     * or convertXmlToNative(), then the EventFlowXml may have been modified.
     * 
     * @return eventFlowXml
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * Converts the content in native format to XML.
     * 
     * @return MessageData
     * @throws DesktopApplicationAdapterException
     */
    public MessageData convert() throws DesktopApplicationAdapterException
    {
        try
        {
            m_isImport = true;
            parseEventFlowXml();
            setBasicParas();
            m_safeBaseFileName = createBaseFileNameToUseForConversion();
            String fname = getFormatName();

            if (FRAME9.equalsIgnoreCase(fname))
            {
                String fullSafeName = writeFileToSaveDir();

                m_logger.info("Converting: " + m_parser.getDisplayName()
                        + ", size: " + m_cxeMessage.getMessageData().getSize());

                convertWithFrameMaker();

                // Gather up the filenames.
                String expectedFileName = fullSafeName.substring(0,
                        fullSafeName.lastIndexOf(".")) + ".mif";

                m_logger.debug("Expected main file is " + expectedFileName);

                File expectedFile = new File(expectedFileName);
                if (expectedFile.exists())
                {
                    m_logger.debug("Expected main file exists!");
                }
                else
                {
                    throw new Exception(
                            "conversion failed to produce the mif file: "
                                    + expectedFileName);
                }

                modifyEventFlowXmlForImport(true);
                return importFiles(expectedFile);
            }
            else
            {
                writeContentToNativeInbox();
                modifyEventFlowXmlForImport(false);
                MessageData xmlOutput = readXmlOutput();
                Logger.writeDebugFile("desktop_conv.xml", xmlOutput);
                return xmlOutput;
            }
        }
        catch (DesktopApplicationAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            String[] errorArgs =
            { m_parser.getDisplayName() };
            throw new DesktopApplicationAdapterException("Import", errorArgs, e);
        }
    }

    /**
     * Converts the content in XML to native format.
     * 
     * @return MessageData
     * @throws DesktopApplicationAdapterException
     */
    public MessageData convertBack() throws DesktopApplicationAdapterException
    {
        try
        {
            m_isImport = false;
            parseEventFlowXml();
            setBasicParas();
            modifyEventFlowXmlForExport();
            String fname = getFormatName();

            if (FRAME9.equalsIgnoreCase(fname))
            {
                Element categoryElement = m_parser
                        .getCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
                String safeBaseFileName = m_parser.getCategoryDaValue(
                        categoryElement, "safeBaseFileName")[0];
                String relSafeName = FileUtils.getPrefix(safeBaseFileName)
                        + ".mif";
                String saveFile = m_saveDir + File.separator + relSafeName;

                m_logger.info("ReConverting: " + m_parser.getDisplayName()
                        + ", tmp file: " + saveFile);
                File saveFile_f = new File(saveFile);
                m_cxeMessage.getMessageData().copyTo(saveFile_f);

                String expectedFile = m_saveDir + File.separator
                        + safeBaseFileName;
                m_safeBaseFileName = safeBaseFileName;

                // Run the conversion process (from mif to fm).
                m_logger.info("Reconstructing: " + expectedFile);
                convertWithFrameMaker();

                FileMessageData fmd = (FileMessageData) MessageDataFactory
                        .createFileMessageData();
                fmd.copyFrom(new File(expectedFile));

                return fmd;
            }
            else
            {
                writeContentToXmlInbox();
                return readNativeOutput();
            }
        }
        catch (DesktopApplicationAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            String[] errorArgs =
            { m_parser.getDisplayName() };
            throw new DesktopApplicationAdapterException("Export", errorArgs, e);
        }
    }

    /**
     * Get the conversion dir for FrameMaker 9
     * 
     * @return
     * @throws Exception
     */
    public static String getConversionDir() throws Exception
    {
        StringBuffer convDir = new StringBuffer();
        convDir.append(m_sc.getStringParameter(
                SystemConfigParamNames.CXE_NTCS_DIR,
                CompanyWrapper.SUPER_COMPANY_ID));
        convDir.append(File.separator);
        convDir.append("FrameMaker9");

        return convDir.toString();
    }

    private MessageData importFiles(File fileToImport) throws Exception
    {
        FileMessageData fmd = (FileMessageData) MessageDataFactory
                .createFileMessageData();
        fmd.copyFrom(fileToImport);

        return fmd;
    }

    private void convertWithFrameMaker() throws Exception
    {

        String commandFileName = null;

        try
        {
            // First create the command file.
            String baseName = m_safeBaseFileName.substring(0,
                    m_safeBaseFileName.lastIndexOf("."));
            StringBuffer commandFileNameBuffer = new StringBuffer(m_saveDir);
            commandFileNameBuffer.append(File.separator).append(baseName);
            commandFileNameBuffer.append(".fm_command");

            commandFileName = commandFileNameBuffer.toString();
            writeCommandFile(commandFileName);

            // Now wait for status file.
            StringBuffer statusFileName = new StringBuffer(m_saveDir);
            statusFileName.append(File.separator).append(baseName)
                    .append(".status");
            File statusFile = new File(statusFileName.toString());

            determineMaxTriesAndSleepTime();
            waitForFileToAppear(statusFile);

            // Conversion is done, but check the status to see if there is an
            // error.
            BufferedReader reader = new BufferedReader(new FileReader(
                    statusFile));
            String line = reader.readLine();
            String msg = reader.readLine();
            m_logger.info(msg);
            String errorCodeString = line.substring(6); // Error:1
            reader.close();
            statusFile.delete();
            int errorCode = Integer.parseInt(errorCodeString);
            if (errorCode > 0)
            {
                throw new Exception(msg);
            }
        }
        finally
        {
            if (commandFileName != null)
            {
                try
                {
                    File f = new File(commandFileName);
                    f.delete();
                }
                catch (Exception e)
                {
                }
            }
        }

    }

    /**
     * Actually writes out the command file. The format of the command file is:
     * ConvertFrom=fm | mif ConvertTo=mif | fm
     */
    private void writeCommandFile(String p_commandFileName) throws Exception
    {
        String convertFrom = "ConvertFrom=";
        String convertTo = "ConvertTo=";

        if (m_isImport)
        {
            convertTo += "mif";
            convertFrom += "fm";
        }
        else
        {
            convertTo += "fm";
            convertFrom += "mif";
        }

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");

        FileUtil.writeFileAtomically(new File(p_commandFileName),
                text.toString(), "US-ASCII");
    }

    /**
     * Write the convertion file to save directory for future reference.
     */
    private String writeFileToSaveDir() throws Exception
    {
        String fullSafeName = m_saveDir + File.separator + m_safeBaseFileName;

        m_logger.debug("Writing file to: " + fullSafeName);

        File file = new File(fullSafeName);
        m_cxeMessage.getMessageData().copyTo(file);

        return fullSafeName;
    }

    /**
     * Set the basic parameters for convertion
     * 
     * @throws Exception
     */
    private void setBasicParas() throws Exception
    {
        setConversionDir();
        setSaveDirectory();
    }

    /**
     * Set the save directory
     */
    private void setSaveDirectory()
    {
        // First save the file out to a temp location.
        StringBuffer saveDir = new StringBuffer(m_convDir);

        saveDir.append(File.separator);
        saveDir.append(m_isImport ? m_parser.getSourceLocale() : m_parser
                .getTargetLocale());
        File saveDirF = new File(saveDir.toString());
        saveDirF.mkdirs();

        m_saveDir = saveDir.toString();
    }

    /**
     * Set the conversion dir
     * 
     * @throws Exception
     */
    private void setConversionDir() throws Exception
    {
        m_convDir = getConversionDir();
    }

    /*******************************************************/
    /*** Protected methods for a user of a DesktopAppHelper ***/
    /*******************************************************/

    /**
     * Creates a safe file name for writing the file out to a directory where
     * other similarly named files may be. For example, someone may import
     * new_files\foo.doc and foo\foo.doc and both will be written to the same
     * native inbox directory in the directory where Noonetime watches.
     * 
     * @return a safe basefilename based off the display name
     */
    protected String createBaseFileNameToUseForConversion()
    {
        // with so many different systems involved, the file may have
        // come from unix or NT
        int forwardSlashIndex = m_parser.getDisplayName().lastIndexOf('/');
        int backwardSlashIndex = m_parser.getDisplayName().lastIndexOf('\\');
        int lastSeparatorindex = (forwardSlashIndex > backwardSlashIndex) ? forwardSlashIndex
                : backwardSlashIndex;

        String baseFileName = m_parser.getDisplayName().substring(
                lastSeparatorindex + 1);

        return System.currentTimeMillis() + baseFileName;
    }

    /**
     * Returns the name of the directory where native files should be written in
     * order to be picked up for conversion to XML This is a directory under the
     * src language directory.
     */
    protected String getNativeInbox()
    {
        StringBuffer result = new StringBuffer(m_workingDir);

        result.append(File.separator);
        result.append(getFormatName());
        result.append(File.separator);
        result.append(m_parser.getSourceLocale());
        result.append(File.separator);
        result.append(getFormatName());
        result.append("_inbox");

        return result.toString();
    }

    /**
     * Returns the name of the directory where XML files can be picked up after
     * conversion from Native format. This is a directory under the src language
     * directory.
     */
    protected String getXmlOutbox()
    {
        StringBuffer result = new StringBuffer(m_workingDir);

        result.append(File.separator);
        result.append(getFormatName());
        result.append(File.separator);
        result.append(m_parser.getSourceLocale());
        result.append(File.separator);
        result.append("xml_outbox");

        return result.toString();
    }

    /**
     * Returns the name of the directory where XML files should be written in
     * order to be picked up for conversion to native format. This is a
     * directory under the target language directory for a particular source
     * language.
     */
    protected String getXmlInbox()
    {
        StringBuffer result = new StringBuffer(m_workingDir);

        result.append(File.separator);
        result.append(getFormatName());
        result.append(File.separator);
        result.append(m_parser.getSourceLocale());
        result.append(File.separator);
        result.append(m_parser.getTargetLocale());
        result.append(File.separator);
        result.append("xml_inbox");

        return result.toString();
    }

    /**
     * Returns the name of the directory where XML files should be written in
     * order to be picked up for conversion to native format. This is a
     * directory under the target language directory for a particular source
     * language.
     */
    protected String getNativeOutbox()
    {
        StringBuffer result = new StringBuffer(m_workingDir);

        result.append(File.separator);
        result.append(getFormatName());
        result.append(File.separator);
        result.append(m_parser.getSourceLocale());
        result.append(File.separator);
        result.append(m_parser.getTargetLocale());
        result.append(File.separator);
        result.append(getFormatName());
        result.append("_outbox");

        return result.toString();
    }

    /**
     * Returns the amount of time to wait for the file conversion to happen
     * based on each 100KB of the original file's size.
     */
    protected long getConversionWaitTime()
    {
        return m_conversionWaitTime;
    }

    /**
     * Writes the content to the native inbox. This means that a Word, Quark,
     * Frame document is written to a directory that is monitored by the
     * conversion server.
     * 
     * @throws DesktopApplicationAdapterException
     */
    protected void writeContentToNativeInbox()
            throws DesktopApplicationAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(getNativeInbox());
            String displayName = m_parser.getDisplayName();
            fileName.append(File.separator);
            fileName.append(m_safeBaseFileName);

            m_logger.info("Converting: " + displayName + ", size: "
                    + m_cxeMessage.getMessageData().getSize() + "b, tmp file: "
                    + fileName.toString());

            writeOutFileContents(fileName.toString());
        }
        catch (Exception e)
        {
            String[] errorArgs =
            { m_parser.getDisplayName() };
            throw new DesktopApplicationAdapterException("Import", errorArgs, e);
        }
    }

    /**
     * Writes the content to the xml inbox. This means that an XML document is
     * written to a directory that is monitored by the conversion server for
     * conversion back to Word, Frame, or Quark.
     * 
     * @throws DesktopApplicationAdapterException
     */
    protected void writeContentToXmlInbox()
            throws DesktopApplicationAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(getXmlInbox());
            String displayName = m_parser.getDisplayName();
            fileName.append(File.separator);
            fileName.append(removeFileExtension(m_safeBaseFileName));
            fileName.append(".xml");

            m_logger.info("Converting: " + displayName + ", size: "
                    + m_cxeMessage.getMessageData().getSize() + "b, tmp file: "
                    + fileName.toString());

            writeOutFileContents(fileName.toString());
        }
        catch (Exception e)
        {
            String[] errorArgs =
            { m_parser.getDisplayName() };
            throw new DesktopApplicationAdapterException("Export", errorArgs, e);
        }
    }

    /**
     * Continually polls the XmlOutbox for the converted file. This involves
     * sleeping until the file appears between attempts to verify the file's
     * existance. Once the file is known to exist, a round of looping and
     * sleeping follows to wait until the file is ready to be read in since the
     * conversion server slowly writes the file out.
     * 
     * @throws Exception
     **/
    protected MessageData readXmlOutput() throws Exception
    {
        // figure out what filename we're looking for
        StringBuffer fileName = new StringBuffer(getXmlOutbox());
        fileName.append(File.separator);
        fileName.append(removeFileExtension(m_safeBaseFileName));
        fileName.append(".xml");

        File file = new File(fileName.toString());
        determineMaxTriesAndSleepTime();
        waitForFileToAppear(file);
        waitForFileToBeWritten(file);
        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        fmd.copyFrom(file);

        file.delete();

        return fmd;
    }

    /**
     * Determines the max number of attempts to detect the converted file. This
     * is based on the conversionWaitSleepTime that is specified in the
     * properties.
     * 
     * <p>
     * The calculation used is:
     * <ul>
     * <li>max_sleep = (originalFileSize b / 102400 b) * conversionWaitSleepTime
     * s</li>
     * <li>max_tries = max_sleep / SLEEP_TIME</li>
     * Given that conversionWaitSleepTime is the amount to sleep per 100KB of
     * original file size. And that there is a 2 second sleep interval between
     * detection attempts. <br>
     */
    private void determineMaxTriesAndSleepTime()
    {
        m_numTries = 0;
        int max_sleep = (int) m_conversionWaitTime
                * (1 + m_originalFileSize / BYTES_IN_100KB);
        m_maxTries = 1 + (int) (max_sleep / m_sleepTime);
    }

    /**
     * Continually polls the NativeOutbox for the converted file. This involves
     * sleeping until the file appears between attempts to verify the file's
     * existance. Once the file is known to exist, a round of looping and
     * sleeping follows to wait until the file is ready to be read in since the
     * conversion server slowly writes the file out.
     * 
     * @throws DesktopApplicationAdapterException
     **/
    protected MessageData readNativeOutput()
            throws DesktopApplicationAdapterException, IOException
    {
        // figure out what filename we're looking for
        StringBuffer fileName = new StringBuffer(getNativeOutbox());
        fileName.append(File.separator);
        fileName.append(m_safeBaseFileName);

        File file = new File(fileName.toString());
        determineMaxTriesAndSleepTime();
        waitForFileToAppear(file);
        waitForFileToBeWritten(file);

        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        fmd.copyFrom(file);

        file.delete();

        return fmd;
    }

    /**
     * Waits until the file appears by sleeping
     * 
     * @throws DesktopApplicationAdapterException
     *             if the file does not appear after the appropriate number of
     *             attempts to see it.
     */
    protected void waitForFileToAppear(File p_file)
            throws DesktopApplicationAdapterException
    {
        while (m_numTries <= m_maxTries)
        {
            m_numTries++;
            m_logger.debug("Waiting for file " + p_file.getAbsolutePath()
                    + " to appear.");

            if (p_file.exists())
            {
                m_logger.debug("File appeared.");
                return;
            }
            else
            {
                sleep();
            }
        }

        String[] errorArgs =
        { p_file.getAbsolutePath() };
        throw new DesktopApplicationAdapterException("Timeout", errorArgs, null);
    }

    /**
     * Sleeps SLEEP_TIME seconds.
     */
    private void sleep()
    {
        try
        {
            Thread.sleep(m_sleepTime);
        }
        catch (InterruptedException ie)
        {
        }
    }

    /**
     * Waits until the file has been converted. The conversion server takes a
     * long time to write the file. This will make a number of attempts to read
     * the file, and if the file is not finished by the last attempt, then there
     * is a timeout.
     * 
     * @param p_file
     *            -- the file being converted
     * @throws DesktopApplicationAdapterException
     *             -- if there is a timeout
     */
    protected void waitForFileToBeWritten(File p_file)
            throws DesktopApplicationAdapterException
    {
        byte[] buffer = new byte[32];
        while (m_numTries < m_maxTries)
        {
            m_numTries++;
            try
            {
                m_logger.debug("Waiting for file " + p_file.getAbsolutePath()
                        + " to be written.");

                FileInputStream fis = new FileInputStream(p_file);
                int bytesRead = fis.read(buffer);
                fis.close();

                if (bytesRead == -1)
                {
                    // nothing in the file yet
                    m_logger.debug("No data yet in file "
                            + p_file.getAbsolutePath());
                }
                else
                {
                    m_logger.debug("Conversion for file "
                            + p_file.getAbsolutePath() + " is started.");

                    waitForConversionServerToStopWritingFile(p_file
                            .getAbsolutePath());
                    return;
                }
            }
            catch (FileNotFoundException fnfe)
            {
                // Since the file exists, such an exception is only
                // thrown if the conversion server is still writing
                // the file out on NT.
                m_logger.debug("(NT?) Permission denied: " + fnfe.getMessage());
            }
            catch (IOException ioe)
            {
                // Since the file exists, such an exception is only
                // thrown if the conversion server is still writing
                // the file out on UNIX.
                if (ioe.getMessage().indexOf("Permission denied") > -1)
                {
                    // Do nothing because the file is still being
                    // written (we're on UNIX looking at a file on NT
                    // over NFS.
                    m_logger.debug("(Solaris?) Permission denied: "
                            + ioe.getMessage());
                }
                else
                {
                    String[] errorArgs =
                    { p_file.getAbsolutePath() };
                    throw new DesktopApplicationAdapterException("IO",
                            errorArgs, ioe);
                }
            }

            sleep();
        }

        String[] errorArgs =
        { p_file.getAbsolutePath() };
        throw new DesktopApplicationAdapterException("Timeout", errorArgs, null);
    }

    /**
     * Since the conversion server apparently writes the file out in chunks,
     * reading from an InputStream might give the EOF value before the file is
     * really done. This causes only part of the file to be read in. This method
     * will check the file size periodically to determine when the file has
     * stopped increasing in size.
     * 
     * @param p_fileName
     *            -- the name of the file being written out
     * @throws DesktopApplicationAdapterException
     */
    protected void waitForConversionServerToStopWritingFile(String p_fileName)
            throws DesktopApplicationAdapterException
    {
        File lastFile = new File(p_fileName);
        long lastFileSize = lastFile.length();
        boolean stillGrowing = true;

        while (stillGrowing)
        {
            // wait for the file to grow
            sleep();

            // create a new file object just to be safe...
            File currentFile = new File(p_fileName);
            long currentFileSize = currentFile.length();
            if (lastFileSize == currentFileSize)
            {
                m_logger.debug("file has stopped growing, size: "
                        + lastFileSize);

                try
                {
                    if (p_fileName.endsWith(".xml"))
                    {
                        // to prevent reading incomplete XML files,
                        // check if the file is really done by reading
                        // it and looking for "</doc>" at the end
                        FileInputStream fis = new FileInputStream(currentFile);
                        byte buf[] = new byte[(int) currentFile.length()];
                        fis.read(buf);
                        fis.close();

                        String s = new String(buf, "UTF8");
                        if (s.indexOf("</DOC>") > -1)
                        {
                            stillGrowing = false;

                            m_logger.info("Really done converting file: "
                                    + m_parser.getDisplayName() + ", size: "
                                    + currentFileSize + "b");
                        }
                        else
                        {
                            lastFileSize = currentFileSize;
                        }

                        s = null;
                    }
                    else
                    {
                        // just assume the binary files are done since
                        // there is no easy way to check.
                        stillGrowing = false;

                        m_logger.info("Done converting file: "
                                + m_parser.getDisplayName() + ", size: "
                                + currentFileSize + "b");
                    }
                }
                catch (Exception e)
                {
                    m_logger.error(
                            "Could not read file to determine if it is done.",
                            e);
                    lastFileSize = currentFileSize;
                }
            }
            else
            {
                lastFileSize = currentFileSize;
                m_logger.debug("file is still growing. last size: "
                        + lastFileSize);
            }
        }
    }

    /**
     * Utility to return a filename without the file extension. "foo" ==
     * removeFileExtension("foo.txt")
     * 
     * @param p_filename
     *            -- a filename
     * @return the filename without the file extension
     */
    protected String removeFileExtension(String p_filename)
    {
        int extIndex = p_filename.lastIndexOf('.');
        return p_filename.substring(0, extIndex);
    }

    /**
     * Writes out the file contents to the specified filename, creating any
     * necessary sub-directories.
     * 
     * @param p_filename
     *            -- the name of the file to write to
     * @throws IOException
     */
    protected void writeOutFileContents(String p_filename) throws IOException
    {
        File file = new File(p_filename);
        file.getParentFile().mkdirs();
        m_cxeMessage.getMessageData().copyTo(file);
    }

    /**
     * Modifies the EventFlowXml during import. This does the following changes
     * 1) changes the postMergeEvent to be the value of this.getPostMergeEvent()
     * 2) changes the format type to xml 3) saves all the above data in the
     * EventFlowXml as da/dv pairs NOTE: The specified encoding in the
     * EventFlowXml is left intact (that is the user can specify ISO,UTF8,etc.
     * for the XML that Noonetime generates).
     * 
     * @throws Exception
     */
    protected void modifyEventFlowXmlForImport(boolean isFMtoMif)
            throws Exception
    {
        // now save all the data to the EventFlowXml
        saveDataToEventFlowXml(isFMtoMif);

        // reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();
    }

    /**
     * Saves some data to the event flow xml by adding it as attribute/ value
     * pairs to the category "DesktopApplicationAdapter".
     * 
     * @throws Exception
     */
    private void saveDataToEventFlowXml(boolean isFMtoMif) throws Exception
    {
        String originalPostMergeEvent = m_parser
                .setPostMergeEvent(getPostMergeEvent().getName());
        String originalFormat = m_parser.setSourceFormatType(isFMtoMif ? "mif"
                : "xml");

        m_originalFileSize = (int) m_cxeMessage.getMessageData().getSize();

        // <category name="DesktopApplicationAdapter">
        Element categoryElement = m_parser
                .addCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        String[] values = new String[1];

        // <da
        // name="postMergeEvent><dv>GlobalSight::FileSystemMergedEvent</dv></da>
        values[0] = originalPostMergeEvent;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement(
                "postMergeEvent", values));

        // <da name="formatType"><dv>word</dv></da>
        values[0] = originalFormat;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement(
                "formatType", values));

        // <da name="safeBaseFileName"><dv>12345test.doc</dv></da>
        values[0] = m_safeBaseFileName;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement(
                "safeBaseFileName", values));

        // <da name="originalFileSize"><dv>11574</dv></da>
        values[0] = Integer.toString(m_originalFileSize);
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement(
                "originalFileSize", values));
    }

    /**
     * Modifies the EventFlowXml during export. Also restores the value of some
     * internal data based off of values saved to the EFXML. This restores the
     * EventFlowXml to the way it was before coming to the DesktopApplication
     * source adapter.
     * 
     * @throws Exception
     */
    protected void modifyEventFlowXmlForExport() throws Exception
    {
        Element categoryElement = m_parser
                .getCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        String originalPostMergeEvent = m_parser.getCategoryDaValue(
                categoryElement, "postMergeEvent")[0];
        m_parser.setPostMergeEvent(originalPostMergeEvent);

        String originalFormat = m_parser.getCategoryDaValue(categoryElement,
                "formatType")[0];
        m_parser.setSourceFormatType(originalFormat);

        // re-set the safe base file name
        m_safeBaseFileName = m_parser.getCategoryDaValue(categoryElement,
                "safeBaseFileName")[0];
        m_originalFileSize = Integer.parseInt(m_parser.getCategoryDaValue(
                categoryElement, "originalFileSize")[0]);

        // reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();
    }

    //
    // Private Methods
    //

    /**
     * Parses the EventFlowXml to set internal values.
     * 
     * @throws DesktopApplicationAdapterException
     */
    private void parseEventFlowXml() throws DesktopApplicationAdapterException
    {
        try
        {
            m_parser.parse();
        }
        catch (Exception e)
        {
            m_logger.error("Unable to parse EventFlowXml. ", e);
            throw new DesktopApplicationAdapterException("Unexpected", null, e);
        }
    }

    //
    // Mutators/Accessors for member data
    //

    /**
     * Returns the working directory of the conversion server.
     */
    protected String getWorkingDir()
    {
        return m_workingDir;
    }

    /**
     * Returns the EventFlowXml Parser.
     */
    protected EventFlowXmlParser getEventFlowXmlParser()
    {
        return m_parser;
    }

    /**
     * Returns the event to publish after doing a conversion from Native to XML.
     * 
     * @return something like XML_IMPORTED_EVENT
     */
    public CxeMessageType getPostConversionEvent()
    {
        String fname = getFormatName();
        if (fname.equalsIgnoreCase(FRAME9))
        {
            return CxeMessageType
                    .getCxeMessageType(CxeMessageType.MIF_IMPORTED_EVENT);
        }
        else
        {
            return CxeMessageType
                    .getCxeMessageType(CxeMessageType.XML_IMPORTED_EVENT);
        }
    }

    /**
     * Returns the event to publish after doing a conversion from XML to Native.
     * This should be an event that takes the content back to the data source
     * adapter.
     * 
     * @return something like "GlobalSight::FileSystemMergedEvent"
     */
    public CxeMessageType getPostConversionBackEvent() throws Exception
    {
        String name = m_parser.getPostMergeEvent();
        return CxeMessageType.getCxeMessageType(name);
    }

    //
    // PROTECTED ABSTRACT METHODS
    //

    /**
     * Returns the format name that this desktop app helper supports.
     * 
     * @return String like "word", "frame", "quark"
     */
    public abstract String getFormatName();

    /**
     * Returns the event to use as the post merge event so that after the merger
     * merges the GXML to XML, the XML will come to the
     * DesktopApplicationAdapter
     * 
     * @return post merge event name
     */
    protected abstract CxeMessageType getPostMergeEvent();

    /***********************/
    /*** PRIVATE METHODS ***/
    /***********************/

    /**
     * Reads in the properties from file <formatName>Adapter.properties for:
     * <ol>
     * <li>conversion wait sleep time</li>
     * </ol>
     * <br>
     */
    private void readProperties()
    {
        String propertyFile = getPropertyFileName();
        try
        {
            URL url = DesktopAppHelper.class.getResource(propertyFile);
            if (url == null)
            {
                throw new FileNotFoundException("Property file " + propertyFile
                        + " not found");
            }

            Properties props = new Properties();
            props.load(new FileInputStream(url.toURI().getPath()));
            m_conversionWaitTime = Long.parseLong(props
                    .getProperty("conversionWaitTime")) * 1000;
            m_sleepTime = Long.parseLong(props.getProperty("sleepTime")) * 1000;
            m_logger.info("conversionWaitTime=" + m_conversionWaitTime);
            m_logger.info("sleepTime=" + m_sleepTime);
        }
        catch (Exception e)
        {
            m_logger.error("Problem reading properties from " + propertyFile
                    + ". Using default values.", e);

            m_conversionWaitTime = 4000;
            m_sleepTime = SLEEP_TIME;
        }
    }

    /**
     * Returns the name of the property file for this helper.
     */
    protected abstract String getPropertyFileName();
}
