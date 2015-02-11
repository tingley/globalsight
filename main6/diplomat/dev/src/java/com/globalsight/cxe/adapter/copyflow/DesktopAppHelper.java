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
package com.globalsight.cxe.adapter.copyflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * This DesktopAppHelper converts Quark documents to Quark XPress Tags using the
 * CopyFlow converter on a server machine (Mac or Windows). See
 * http://www.napsys.com.
 */
public class DesktopAppHelper
{
    /**
     * The name of the category in the event flow xml for any data specific to
     * the DesktopApplicationAdapter.
     */
    public static final String EFXML_DA_CATEGORY_NAME = "DesktopApplicationAdapter";

    private static final String FORMAT = "CopyFlow";

    // Keep in sync with
    // ling2VOB/.../com/globalsight/ling/docproc/extractor/xptag/ExtractionHandler
    // method handleText().
    private static final String s_BOXAUTONAME_PREFIX = "quark_text_box_";

    //
    // Private classes
    //

    private static class ServerConfig
    {
        public String m_description;
        public String m_serverName;
        public int m_serverPort;
        public String m_workingDirRemote;
        public ArrayList m_locales;

        public ServerConfig(String p_description)
        {
            m_description = p_description;
        }

        public String toString()
        {
            return "GlobalSightXT config for " + m_description + " (name="
                    + m_serverName + ",port=" + m_serverPort + ",remote dir="
                    + m_workingDirRemote + ",locales=" + m_locales + ")";
        }
    }

    //
    // Private Members
    //

    private org.apache.log4j.Logger m_logger = null;

    protected EventFlowXmlParser m_parser = null;
    private String m_eventFlowXml = null;
    private CxeMessage m_cxeMessage = null;
    private int m_originalFileSize = 0;

    // File name with unique timestamp (without directory) for QXD->XTG
    private String m_safeBaseFileName = null;
    // File name with unique timestamp (without directory) for XTG->QXD
    private String m_safeBaseFileName2 = null;

    private String m_workingDirLocal;

    // List of ServerConfig objects.
    private ArrayList m_configs = new ArrayList();
    // Current config to use.
    private ServerConfig m_config;

    /**
     * Constructor.
     * 
     * @param p_eventFlowXml
     *            -- the EventFlowXml
     * @param p_content
     *            -- the content (whether GXML or Native)
     */
    public DesktopAppHelper(CxeMessage p_cxeMessage,
            org.apache.log4j.Logger p_logger)
    {
        m_logger = p_logger;
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = m_cxeMessage.getEventFlowXml();
        m_parser = new EventFlowXmlParser(m_eventFlowXml);

        readProperties();
    }

    //
    // Public methods
    //

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
    public MessageData convertNativeToXml()
            throws DesktopApplicationAdapterException
    {
        try
        {
            parseEventFlowXml();

            // The current restriction is that Quark Mac can only be used with
            // l10nprofiles that are 1->1 so there is only one target locale
            L10nProfile l10nProfile = ServerProxy.getProjectHandler()
                    .getL10nProfile(m_parser.getL10nProfileId());
            String srcLocale = l10nProfile.getSourceLocale().getLocale()
                    .toString();
            if (l10nProfile.getTargetLocales().length != 1)
                m_logger.warn("Quark(Mac) conversion can only be done with l10nprofiles that\r\nhave one target locale. Only the first target locale is used.");
            String trgLocale = l10nProfile.getTargetLocales()[0].getLocale()
                    .toString();

            m_config = findServerConfig(srcLocale, trgLocale);
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug("On import, found server config: "
                        + m_config.toString());                
            }

            m_safeBaseFileName = createBaseFileNameForConversion();

            writeContentToNativeInbox();

            modifyEventFlowXmlForImport();

            callConverterForNative();

            MessageData xmlOutput = readXmlOutput();

            Logger.writeDebugFile("copyflow_conv.xtg", xmlOutput);

            return xmlOutput;
        }
        catch (DesktopApplicationAdapterException ex)
        {
            throw ex;
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
    public MessageData convertXmlToNative()
            throws DesktopApplicationAdapterException
    {
        try
        {
            parseEventFlowXml();

            // Find server that writes TO target locale.
            m_config = findServerConfig(m_parser.getSourceLocale(),
                    m_parser.getTargetLocale());
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug("On export, found server config: "
                        + m_config.toString());                
            }

            modifyEventFlowXmlForExport();

            // The above call restored m_safeBaseFileName and
            // m_originalFileSize from the EFXML.

            m_safeBaseFileName2 = createBaseFileNameForBackConversion();

            callConverterForXml();

            return readNativeOutput();
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

    //
    // Protected Methods
    //

    /**
     * Writes the content to the native inbox. This means that a Quark document
     * is written to a directory on a Mac from which CopyFlow can read it.
     * 
     * @throws DesktopApplicationAdapterException
     */
    protected void writeContentToNativeInbox()
            throws DesktopApplicationAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(m_workingDirLocal);
            fileName.append(m_safeBaseFileName);

            String displayName = m_parser.getDisplayName();
            m_logger.info("Converting: " + displayName + ", size: "
                    + m_cxeMessage.getMessageData().getSize() + "b, tmp file: "
                    + fileName);

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
     * Writes the content to the xml inbox. This means that an XTG document is
     * written to a directory that is accessible to the CopyFlow server for
     * conversion back to Quark.
     * 
     * @throws DesktopApplicationAdapterException
     */
    protected void writeQxdToXmlInbox()
            throws DesktopApplicationAdapterException
    {
        try
        {
            // We copy the original (safe) file containing the story
            // names to a temporary copy into which we can import the
            // translated stories. After conversion, this temporary
            // copy is deleted.
            copyFile(m_safeBaseFileName, m_safeBaseFileName2);
        }
        catch (Exception e)
        {
            String[] errorArgs =
            { m_parser.getDisplayName() };
            throw new DesktopApplicationAdapterException("Export", errorArgs, e);
        }
    }

    /**
     * Writes the content to the xml inbox. This means that an XTG document is
     * written to a directory that is accessible to the CopyFlow server for
     * conversion back to Quark.
     * 
     * @throws DesktopApplicationAdapterException
     */
    protected void writeXtgToXmlInbox(String p_file)
            throws DesktopApplicationAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(m_workingDirLocal);
            fileName.append(p_file);

            String displayName = m_parser.getDisplayName();
            m_logger.info("Converting: " + displayName + ", size: "
                    + m_cxeMessage.getMessageData().getSize() + "b, tmp file: "
                    + fileName);

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
     * Calls CopyFlow to convert the native Quark document to Quark XPress Tags
     * (QXD->XTG).
     */
    private void callConverterForNative()
            throws DesktopApplicationAdapterException
    {
        String qxdFile = m_safeBaseFileName;
        String xtgFile = removeFileExtension(m_safeBaseFileName) + ".xtg";
        String directory = m_config.m_workingDirRemote;

        // Connect to CopyFlow on a Mac (may need to be read from prop file).
        CopyFlowSocket sock = new CopyFlowSocket(m_logger, true);

        try
        {
            sock.connect(m_config.m_serverName, m_config.m_serverPort);

            sock.openDocument(directory, qxdFile);

            sock.autoNameBoxes(s_BOXAUTONAME_PREFIX, xtgFile);
            sock.exportStory(directory, xtgFile);
            sock.saveDocument(directory, qxdFile);
            sock.closeDocument();
        }
        catch (UnknownHostException e)
        {
            String[] errorArgs =
            { "unknown host/port " + m_config.m_serverName + "/"
                    + m_config.m_serverPort };
            throw new DesktopApplicationAdapterException("Conversion",
                    errorArgs, e);
        }
        catch (IOException e)
        {
            String[] errorArgs =
            { e.getMessage() };
            throw new DesktopApplicationAdapterException("Conversion",
                    errorArgs, e);
        }
        finally
        {
            sock.close();
        }
    }

    /**
     * Calls CopyFlow to convert a translated XPress Tag document to native
     * Quark (XTG->QXD).
     */
    private void callConverterForXml()
            throws DesktopApplicationAdapterException
    {
        String qxdFile = m_safeBaseFileName2;
        String xtgFile = removeFileExtension(m_safeBaseFileName2) + ".xtg";
        String directory = m_config.m_workingDirRemote;

        // First create a copy of the original Quark document.
        writeQxdToXmlInbox();

        CopyFlowSocket sock = new CopyFlowSocket(m_logger, true);

        try
        {
            sock.connect(m_config.m_serverName, m_config.m_serverPort);

            sock.openDocument(directory, qxdFile);
            sock.autoNameBoxes(s_BOXAUTONAME_PREFIX, xtgFile);
            // Sometimes conversion works without this additional call
            // to export. It works all the time if it's left in. We
            // may revisit this again and comment out the next line.
            sock.exportStory(directory, xtgFile);

            writeXtgToXmlInbox(xtgFile);

            sock.importStory(directory, xtgFile);
            sock.saveDocument(directory, qxdFile);
            sock.closeDocument();
        }
        catch (DesktopApplicationAdapterException e)
        {
            throw e;
        }
        catch (UnknownHostException e)
        {
            String[] errorArgs =
            { "unknown host/port " + m_config.m_serverName + "/"
                    + m_config.m_serverPort };
            throw new DesktopApplicationAdapterException("Conversion",
                    errorArgs, e);
        }
        catch (IOException e)
        {
            String[] errorArgs =
            { e.getMessage() };
            throw new DesktopApplicationAdapterException("Conversion",
                    errorArgs, e);
        }
        finally
        {
            sock.close();
        }
    }

    /**
     * Reads in the XTG file.
     * 
     * @throws Exception
     **/
    protected MessageData readXmlOutput() throws Exception
    {
        StringBuffer fileName = new StringBuffer(m_workingDirLocal);
        fileName.append(removeFileExtension(m_safeBaseFileName));
        fileName.append(".xtg");

        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        File file = new File(fileName.toString());
        fmd.operatingSystemSafeCopyFrom(file);
        file.delete();
        return fmd;
    }

    /**
     * Reads in the QXD file.
     * 
     * @throws DesktopApplicationAdapterException
     **/
    protected MessageData readNativeOutput() throws IOException
    {
        // Read in the QXD file and delete it.
        StringBuffer fileName = new StringBuffer(m_workingDirLocal);
        fileName.append(m_safeBaseFileName2);

        FileMessageData fmd = MessageDataFactory.createFileMessageData();

        File file = new File(fileName.toString());
        fmd.operatingSystemSafeCopyFrom(file);
        file.delete();

        // Also delete the translated XTG file.
        fileName = new StringBuffer(m_workingDirLocal);
        fileName.append(removeFileExtension(m_safeBaseFileName2));
        fileName.append(".xtg");

        file = new File(fileName.toString());
        file.delete();

        return fmd;
    }

    /**
     * Creates a safe file name for writing the file out to a directory where
     * other similarly named files may be. For example, someone may import
     * new_files\foo.doc and foo\foo.doc and both will be written to the same
     * native inbox directory in the directory accessible to CopyFlow.
     * 
     * Note that the Macintosh has a 31 character limit on filenames.
     * 
     * @return a safe basefilename based off the display name
     */
    protected String createBaseFileNameForConversion()
    {
        String filename = m_parser.getDisplayName();

        // With so many different systems involved, the file may have
        // come from unix or NT.
        int forwardSlashIndex = filename.lastIndexOf('/');
        int backwardSlashIndex = filename.lastIndexOf('\\');
        int lastSeparatorindex = (forwardSlashIndex > backwardSlashIndex) ? forwardSlashIndex
                : backwardSlashIndex;

        String baseName = filename.substring(lastSeparatorindex + 1);

        int hash = baseName.hashCode();

        return System.currentTimeMillis() + (hash > 0 ? "-" : "") + hash
                + ".qxd";
    }

    /**
     * Creates another temporary name based on the previosly computed
     * safeBaseFileName for a conversion from XTG back to native QXD.
     * 
     * Note that the Macintosh has a 31 character limit on filenames.
     */
    protected String createBaseFileNameForBackConversion()
    {
        // Result contains timestamp, will be unique.
        return createBaseFileNameForConversion();
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
        FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
        fmd.operatingSystemSafeCopyTo(file);
    }

    /**
     * Modifies the EventFlowXml during import. This does the following changes:
     * 
     * 1) changes the postMergeEvent to be the value of this.getPostMergeEvent()
     * 2) changes the format type to xptag 3) saves all the above data in the
     * EventFlowXml as da/dv pairs
     * 
     * NOTE: The specified encoding in the EventFlowXml is left intact because
     * it will be auto-detected by the XPTag extractor (.xtg is
     * self-describing).
     * 
     * @throws Exception
     */
    protected void modifyEventFlowXmlForImport() throws Exception
    {
        // now save all the data to the EventFlowXml
        saveDataToEventFlowXml();

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
    private void saveDataToEventFlowXml() throws Exception
    {
        String originalPostMergeEvent = m_parser
                .setPostMergeEvent(getPostMergeEvent().getName());
        String originalFormat = m_parser.setSourceFormatType("xptag");

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

        // and the original file size
        m_originalFileSize = Integer.parseInt(m_parser.getCategoryDaValue(
                categoryElement, "originalFileSize")[0]);

        // For Quark files generated on the Mac that had no extension,
        // change the filename to have .qxd at the end so they can be
        // opened on the Mac without requiring proper resource forks.
        String filename = m_parser.getFilename();
        if (filename != null)
        {
            m_parser.setFilename(fixMacExtension(filename));
        }

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
        return m_workingDirLocal;
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
    public CxeMessageType getPostNativeToXmlConversionEvent()
    {
        return CxeMessageType
                .getCxeMessageType(CxeMessageType.XPTAG_IMPORTED_EVENT);
    }

    /**
     * Returns the event to publish after doing a conversion from XML to Native.
     * This should be an event that takes the content back to the data source
     * adapter.
     * 
     * @return something like "GlobalSight::FileSystemMergedEvent"
     */
    public CxeMessageType getPostXmlToNativeConversionEvent() throws Exception
    {
        String name = m_parser.getPostMergeEvent();
        return CxeMessageType.getCxeMessageType(name);
    }

    /**
     * Returns the format name that this desktop app helper supports.
     * 
     * @return String like "word", "frame", "quark"
     */
    public String getFormatName()
    {
        return FORMAT;
    }

    /**
     * Returns the event to use as the post merge event so that after the merger
     * merges the GXML to XML, the XML will come to the
     * DesktopApplicationAdapter
     * 
     * @return post merge event name
     */
    protected CxeMessageType getPostMergeEvent()
    {
        return CxeMessageType
                .getCxeMessageType(CxeMessageType.COPYFLOW_LOCALIZED_EVENT);
    }

    //
    // PRIVATE METHODS
    //

    private void copyFile(String p_from, String p_to) throws IOException
    {
        File from = new File(m_workingDirLocal + p_from);
        File to = new File(m_workingDirLocal + p_to);

        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("Copying file `" + m_workingDirLocal + p_from
                    + "' to `" + m_workingDirLocal + p_to + "'.");
        }

        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);

        byte[] buffer = new byte[32768];
        int len;

        while ((len = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * For Quark files generated on the Mac that had no extension, change the
     * filename to have .qxd at the end so they can be opened on the Mac without
     * requiring proper resource forks.
     * 
     * Make sure the filename has at most 31 chars. This will limit filenames to
     * 27 characters (unless they are already longer than 31 chars or already
     * have an extension).
     */
    private String fixMacExtension(String p_filename)
    {
        String extension = getExtension(p_filename);

        // If the file already has .QXD it's ok.
        if (extension.equalsIgnoreCase("qxd"))
        {
            return p_filename;
        }

        String dir = "";
        String filename = p_filename;
        int index;

        index = p_filename.lastIndexOf("\\");
        if (index < 0)
        {
            index = p_filename.lastIndexOf("/");
        }

        if (index >= 0)
        {
            dir = p_filename.substring(0, index + 1);
            filename = p_filename.substring(index + 1);
        }

        // If the file is already longer than 31 chars, it's not a MAC file.
        if (filename.length() > 31)
        {
            return dir + filename + ".qxd";
        }

        // Otherwise we truncate to max 31 chars including QXD extension.
        String baseName = filename
                .substring(0, Math.min(27, filename.length()));

        return dir + baseName + ".qxd";
    }

    private String getExtension(String p_filename)
    {
        int index = p_filename.lastIndexOf('.');

        if (index > 0)
        {
            return p_filename.substring(index + 1);
        }

        return "";
    }

    /**
     * Reads in the properties from file QuarkMacAdapter.properties for:
     * <ol>
     * <li>Local Working Directory</li>
     * <li>Remote Working Directory</li>
     * <li>Server Name</li>
     * <li>Server Port</li>
     * </ol>
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
                        + " not found.");
            }

            Properties props = new Properties();
            props.load(new FileInputStream(url.toURI().getPath()));

            m_workingDirLocal = props.getProperty("GlobalSight_Directory");

            int index = 1;

            while (true)
            {
                String server = props.getProperty("Server_" + index);
                if (server == null)
                {
                    break;
                }

                ServerConfig config = new ServerConfig(server);

                config.m_serverName = props.getProperty("ServerName_" + index);
                config.m_serverPort = Integer.parseInt(props
                        .getProperty("ServerPort_" + index));
                config.m_workingDirRemote = props
                        .getProperty("GlobalSightXT_Directory_" + index);
                config.m_locales = getLocales(props.getProperty("Locales_"
                        + index));

                m_configs.add(config);

                index++;
            }

            if (m_logger.isDebugEnabled())
            {
                m_logger.debug("GlobalSightXT Server Configuration:\n"
                        + m_configs);
            }
        }
        catch (Exception e)
        {
            m_logger.error("Problem reading properties from " + propertyFile
                    + ".", e);
        }
    }

    private ArrayList getLocales(String p_locales)
    {
        ArrayList result = new ArrayList();

        if (p_locales != null)
        {
            StringTokenizer tok = new StringTokenizer(p_locales, ",");

            while (tok.hasMoreTokens())
            {
                String locale = tok.nextToken().trim();

                if (locale.length() > 0)
                {
                    result.add(locale);
                }
            }
        }

        return result;
    }

    /**
     * Finds the best server configuration given the document's desired locale
     * (source for import, target for export). Full matches are preferred over
     * partial matches.
     */
    private ServerConfig findServerConfig(String p_srcLocale, String p_trgLocale)
            throws Exception
    {
        ServerConfig result = null;

        // First try full locales in all configs
        for (int i = 0, max = m_configs.size(); i < max; i++)
        {
            ServerConfig config = (ServerConfig) m_configs.get(i);

            if (findFullLocale(config.m_locales, p_srcLocale)
                    && (findFullLocale(config.m_locales, p_trgLocale) || findPartialLocale(
                            config.m_locales, p_trgLocale)))
            {
                return config;
            }
        }

        // Then try partial locales in all configs
        for (int i = 0, max = m_configs.size(); i < max; i++)
        {
            ServerConfig config = (ServerConfig) m_configs.get(i);

            if (findPartialLocale(config.m_locales, p_srcLocale)
                    && (findFullLocale(config.m_locales, p_trgLocale) || findPartialLocale(
                            config.m_locales, p_trgLocale)))
            {
                return config;
            }
        }

        throw new Exception("Requested locale pair `" + p_srcLocale + ","
                + p_trgLocale
                + "' not supported by any configured GlobalSightXT server.");
    }

    /**
     * Matches a long (untruncated) locale in a list of locales.
     */
    private boolean findFullLocale(ArrayList p_locales, String p_locale)
    {
        for (int i = 0, max = p_locales.size(); i < max; i++)
        {
            String locale = (String) p_locales.get(i);

            if (p_locale.equalsIgnoreCase(locale))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Matches a partial (truncated) locale in a list of locales.
     */
    private boolean findPartialLocale(ArrayList p_locales, String p_locale)
    {
        p_locale = p_locale.substring(0, 2);

        for (int i = 0, max = p_locales.size(); i < max; i++)
        {
            String locale = (String) p_locales.get(i);

            if (p_locale.equalsIgnoreCase(locale.substring(0, 2)))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the name of the property file for this helper.
     */
    protected String getPropertyFileName()
    {
        return "/properties/QuarkMacAdapter.properties";
    }
}
