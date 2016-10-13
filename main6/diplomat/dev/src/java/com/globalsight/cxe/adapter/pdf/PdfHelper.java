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
package com.globalsight.cxe.adapter.pdf;

//JDK
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.w3c.dom.Element;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.file.FileWaiter;

/** 
* The DesktopAppHelper is an abstract base class intended to be subclassed to
* handle converting native format files (Word, Quark, Frame, etc.) to XML and back.
*/
public class PdfHelper 
{
    //CONSTANTS
    /**
    * The name of the category in the event flow xml for any data specific
    * to the PdfAdapter.
    */
    public static final String EFXML_DA_CATEGORY_NAME = "PdfAdapter";

    /*****************************/
    /*** Private Member Data ***/
    /*****************************/
    private String m_eventFlowXml = null;
    private CxeMessage m_cxeMessage = null;
    private long m_originalFileSize = 0;
    private String m_workingDir = null;
    private String m_safeBaseFileName = null;
    protected EventFlowXmlParser m_parser = null;
    private org.apache.log4j.Logger m_logger = null;
    
    ///maximum time to wait for a conversion to complete in millisec
    private long m_maxTimeToWait = (long) (60 * 60 * 1000);
    ///time to sleep between detection attempts in millisec
    private static final long SLEEP_TIME = 2000; 


    /** Constructor for use by sub classes
    * <br>
    * @param p_workingDir -- the main working directory where the conversion server looks for files
    * @param p_eventFlowXml -- the EventFlowXml
    * @param p_content -- the content (whether GXML or Native)
    */
    public PdfHelper(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
    throws Exception
    {
        m_logger = p_logger;
        m_workingDir = SystemConfiguration.getInstance().getStringParameter(
            SystemConfigParamNames.PDF_CONV_DIR);
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_cxeMessage = p_cxeMessage;
        m_parser = new EventFlowXmlParser(m_eventFlowXml);
        readProperties();
    }

    /*******************************************************/
    /*** Public methods for a user of a DesktopAppHelper ***/
    /*******************************************************/

    /** Gets the event flow XML. If called, after a call to
    * convertNativeToXml() or convertXmlToNative(), then the
    * EventFlowXml may have been modified.
    * <br>
    * @return eventFlowXml
    */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /** Converts the content from PDF to Word
    * <br>
    * @return FileMessageData containing the word output
    * @throws PdfAdapterException
    */
    public MessageData convertPdfToWord() throws PdfAdapterException
    {
        String filename = null;
        try {
            parseEventFlowXml();
            m_safeBaseFileName = createBaseFileNameToUseForConversion();
            filename = writeContentToPdfInbox();
            modifyEventFlowXmlForImport();
            return readWordOutput();
        }
        catch (PdfAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new PdfAdapterException("Import", errorArgs,e);
        }
        finally
        {
            if (filename != null)
            {
                try {
                    File f = new File(filename);
                    f.delete();
                }
                catch (Exception e){}
            }
        }
    }

    /** Converts the content in Word to PDF
    * NOTE: Does nothing for now other than modifying the EventFlowXml
    * since conversion to PDF is not yet necessary
    * <br>
    * @return MessageData
    * @throws PdfAdapterException
    */
    public MessageData convertWordToPdf()
    throws PdfAdapterException
    {
        try {
            parseEventFlowXml();
            modifyEventFlowXmlForExport();
	    return m_cxeMessage.getMessageData();
        }
        catch (PdfAdapterException daae)
        {
            throw daae;
        }
        catch (Exception e)
        {
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new PdfAdapterException("Export", errorArgs,e);
        }
    }

    /***********************/
    /** Protected Methods **/
    /***********************/

    /**
    * Creates a safe file name for writing the file
    * out to a directory where other similarly named files may be.
    * For example, someone may import new_files\foo.doc and foo\foo.doc
    * and both will be written to the same native inbox directory in
    * the directory where Noonetime watches.
    * <br>
    * @returns a safe basefilename based off the display name
    */
    protected String createBaseFileNameToUseForConversion()
    {
	//with so many different systems involved, the file may have come from unix or NT
	String displayName = m_parser.getDisplayName();
	int forwardSlashIndex = m_parser.getDisplayName().lastIndexOf('/');
	int backwardSlashIndex = m_parser.getDisplayName().lastIndexOf('\\');
        int lastSeparatorindex = (forwardSlashIndex > backwardSlashIndex) ? forwardSlashIndex : backwardSlashIndex;
        String baseFileName = displayName.substring(lastSeparatorindex+1);

	//make sure each file written out is unique so that the OCR package will process it
	return System.currentTimeMillis() + baseFileName;
    }

    /** Returns the name of the directory where PDF files
    * should be written in order to be picked up for conversion to Word
    */
    protected String getPdfInbox()
    {
        StringBuffer ni = new StringBuffer(m_workingDir);
        ni.append(File.separator);
        ni.append(getFormatName());
        ni.append(File.separator);
        ni.append(m_parser.getSourceLocale());
       return ni.toString();
    }

    /** Returns the name of the directory where Word files
    * can be picked up after conversion from PDF
    * This is a directory under the src language directory*/
    protected String getWordOutbox()
    {
	//use the same outbox as inbox for now
	return getPdfInbox();
    }

    /**
    /**
    * Writes the content to the pdf inbox. This means
    * that a PDF file is written to a directory
    * that is monitored by the OCR package
    * <br>
    * @throws PdfAdapterException
    * @return the filename that was written out
    */
    protected String writeContentToPdfInbox()
    throws PdfAdapterException
    {
        try
        {
            StringBuffer fileName = new StringBuffer(getPdfInbox());
            File dir = new File(fileName.toString());
            dir.mkdirs();
            fileName.append(File.separator);
            fileName.append(m_safeBaseFileName);
            m_logger.info("Converting: " + m_parser.getDisplayName() + ", size: " +
			     m_cxeMessage.getMessageData().getSize() + ", tmp file: " + fileName.toString());
            FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
            File targetFile = new File (fileName.toString());
            fmd.copyTo(targetFile);
            return fileName.toString();
        }
        catch (Exception e)
        {
            m_logger.error("Failed to write pdf to inbox:",e);
            String[] errorArgs = { m_parser.getDisplayName() };
            throw new PdfAdapterException("Import", errorArgs,e);
        }
    }

    /**
    * Continually polls the WordOutbox for the converted file.
    *
    * This means waiting util the status file appears, and then if
    * the status of the conversion was successful, then reading in
    * the Word format file.
    * <br>
    * @throws PdfAdapterException    
    **/
    protected FileMessageData readWordOutput()
    throws PdfAdapterException
    {
        //figure out the filename of the status file
        StringBuffer fileNameNE = new StringBuffer(getWordOutbox());
        fileNameNE.append(File.separator);
        fileNameNE.append(removeFileExtension(m_safeBaseFileName));

	String statusFileName = fileNameNE.toString() + ".status";
	String docFileName = fileNameNE.toString() + ".doc";

	try {
	    FileWaiter waiter = new FileWaiter(SLEEP_TIME,m_maxTimeToWait,statusFileName);
	    waiter.waitForFile();
	    File file = new File(statusFileName);
	    BufferedReader statusFile = new BufferedReader(new FileReader(file));
	    String line = statusFile.readLine();
	    String msg = statusFile.readLine();
	    m_logger.info(msg);

	    String errorCodeString = line.substring(6);

	    //now delete the status file
	    statusFile.close();
	    file.delete();

	    int errorCode = Integer.parseInt(errorCodeString);
	    if (errorCode == 0)
            {
                File f = new File (docFileName);
                FileMessageData fmd = (FileMessageData) MessageDataFactory.createFileMessageData();
                fmd.copyFrom(f);
                f.delete(); //delete the original word file
                return fmd;
            }
	    else
		throw new Exception(msg);
	}
	catch (Exception e)
	{
            m_logger.error("Failed to read word output:",e);
	    String[] errorArgs = { m_parser.getDisplayName()};
	    throw new PdfAdapterException("Import",errorArgs,e);
	}
    }

    /**
    * Utility to return a filename without the file extension.
    * "foo" == removeFileExtension("foo.txt")
    * <br>
    * @param p_filename -- a filename
    * @return the filename without the file extension
    */
    protected String removeFileExtension(String p_filename)
    {
        int extIndex = p_filename.lastIndexOf('.');
        return p_filename.substring(0,extIndex);
    }

    /**
    * Modifies the EventFlowXml during import. This does the following changes
    * 1) changes the postMergeEvent to be the value of this.getPostMergeEvent()
    * 2) changes the format type to xml
    * 3) saves all the above data in the EventFlowXml as da/dv pairs
    * NOTE: The specified encoding in the EventFlowXml is left in tact (that is
    * the user can specify ISO,UTF8,etc. for the XML that Noonetime generates)
    * <br>
    * @throws Exception
    */
    protected void modifyEventFlowXmlForImport()
    throws Exception
    {

        //now save all the data to the EventFlowXml
        saveDataToEventFlowXml();

        //reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();

	Logger.writeDebugFile("pdf_ef.xml", m_eventFlowXml);
    }

    /**
    * Saves some data to the event flow xml by adding it as attribute/value
    * pairs to the category "PdfAdapter".
    * <br>
    * @throws Exception
    */
    private void saveDataToEventFlowXml()
    throws Exception
    {
        String originalPostMergeEvent = m_parser.setPostMergeEvent(getPostMergeEvent());
        String originalFormat = m_parser.setSourceFormatType("word-html");

        m_originalFileSize = m_cxeMessage.getMessageData().getSize();

        //<cateogory name="PdfAdapter">
        Element categoryElement = m_parser.addCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        String[] values = new String[1];

        //<da name="postMergeEvent><dv>GlobalSight::FileSystemMergedEvent</dv></da>
        values[0] = originalPostMergeEvent;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement("postMergeEvent", values));

        //<da name="formatType"><dv>word</dv></da>
        values[0] = originalFormat;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement("formatType", values));

        //<da name="safeBaseFileName"><dv>12345test.doc</dv></da>
        values[0] = m_safeBaseFileName;
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement("safeBaseFileName", values));

        //<da name="originalFileSize"><dv>11574</dv></da>
        values[0] = Long.toString(m_originalFileSize);
        categoryElement.appendChild(m_parser.makeEventFlowXmlDaElement("originalFileSize", values));
    }

    /**
    * Modifies the EventFlowXml during export. Also restores the value
    * of some internal data based off of values saved to the EFXML.
    * This restores the EventFlowXml to the way it was before coming
    * to the Pdf source adapter.
    * <br>
    * @throws Exception
    */
    protected void modifyEventFlowXmlForExport()
    throws Exception
    {
        Element categoryElement = m_parser.getCategory(EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        String originalPostMergeEvent = m_parser.getCategoryDaValue(categoryElement, "postMergeEvent")[0];
        m_parser.setPostMergeEvent(originalPostMergeEvent);

        String originalFormat = m_parser.getCategoryDaValue(categoryElement, "formatType")[0];
        m_parser.setSourceFormatType(originalFormat);

        //re-set the safe base file name
        m_safeBaseFileName = m_parser.getCategoryDaValue(categoryElement, "safeBaseFileName")[0];
        m_originalFileSize = Long.parseLong(m_parser.getCategoryDaValue(categoryElement, "originalFileSize")[0]);

        //change the file extension if it's PDF on the target file
        if (originalFormat != null && originalFormat.equals("pdf"))
        {
            // change the target file name to a .doc externsion 
            // since it can't be saved back to a pdf
            String originalName = m_parser.getTargetFileName();
            String newName = originalName.substring(0,originalName.length() - 4) + ".doc";
            m_parser.setTargetFileName(newName);
        }

        //reconstruct the EventFlowXml String
        m_parser.reconstructEventFlowXmlStringFromDOM();
        m_eventFlowXml = m_parser.getEventFlowXml();
    }

    /***********************/
    /** Private Methods **/
    /***********************/

    /** Parses the EventFlowXml to set internal values
    * <br>
    * @throws PdfAdapterException
    */
    private void parseEventFlowXml()
    throws PdfAdapterException
    {
        try
        {
            m_parser.parse();
        }
        catch (Exception e)
        {
            m_logger.error("Unable to parse EventFlowXml. ", e);
            throw new PdfAdapterException("Unexpected", null, e);
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

    /**
    * Returns the event to publish after doing a conversion
    * from PdfToWord
    * <br>
    * @return CxeMessageType
    */
    public CxeMessageType getPostNativeConversionEvent()
    {
        return CxeMessageType.getCxeMessageType(CxeMessageType.MSOFFICE_IMPORTED_EVENT);
    }

    /**
    * Returns the event to publish after doing a conversion
    * from Word to PDF. This should be an event that
    * takes the content back to the data source adapter.
    * <br>
    * @return CxeMessageType
    */
    public CxeMessageType getPostWordToPdfConversionEvent()
    {
        return CxeMessageType.getCxeMessageType(m_parser.getPostMergeEvent());
    }

    /**
    * Returns the event to use as the post merge event
    * so that after the merger merges the GXML to XML,
    * the XML will come to the PdfAdapter
    * <br>
    * @return post merge event name
    */
    String getPostMergeEvent()
    {
	return CxeMessageType.getCxeMessageType(CxeMessageType.PDF_LOCALIZED_EVENT).getName();
    }

    /***********************/
    /*** PRIVATE METHODS ***/
    /***********************/

    /**
    * Reads in the properties from file <formatName>Adapter.properties for:
    * <ol>
    * <li>conversion wait sleep time</li>
    * </ol><br>
    */
    private void readProperties()
    {
    	String propertyFile = getPropertyFileName();
		try
		{
			Properties props = ((DynamicPropertiesSystemConfiguration) SystemConfiguration
					.getInstance(propertyFile)).getProperties();

			// convert the time to wait to milliseconds from minutes
			m_maxTimeToWait = (long) (Long.parseLong(props.getProperty("maxTimeToWait")) * 60 * 1000);
        }
        catch (Exception e)
        {
            m_logger.error( "Problem reading properties from " + propertyFile +
                                     ". Using default values.", e);

        }
    }


    /**
     * Returns the name of the property file for this helper
     * 
     * @return 
     */
    protected String getPropertyFileName()
    {
	return "/properties/PdfAdapter.properties";
    }

    public String getFormatName()
    {
	return "pdf";
    }
}

