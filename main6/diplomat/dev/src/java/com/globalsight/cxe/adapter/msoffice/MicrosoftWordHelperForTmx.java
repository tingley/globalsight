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
package com.globalsight.cxe.adapter.msoffice;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.util.FileUtil;
import com.globalsight.util.file.FileWaiter;


import java.io.*;
import java.util.ArrayList;

/**
 * The MicrosoftAppHelper is an helper class to handle converting
 * native format files (Word, Quark, Frame, etc.) to XML and back.
 */
public class MicrosoftWordHelperForTmx
{
    //CONSTANTS

    //////////////////////////////////////////////////////////////
    //  Supported extensions for MS Office
    //////////////////////////////////////////////////////////////
    static private final String DOC = ".doc";
    static private final String PDF = ".pdf";
    static private final String RTF = ".rtf";
    static private final String XLS = ".xls";
    static private final String PPT = ".ppt";

    //the types of MS Office files (used to set m_type below)
    static private final int MS_DOC = 0;
    static private final int MS_RTF = 1;
    static private final int MS_PPT = 2;
    static private final int MS_XLS = 3;

    // max time to wait is 5 minutes?
    static private long s_MAXTTW = 5;

    //
    // Private Member Data
    //
    private Logger m_logger;

    private String m_originalFileName = null;
    private String m_safeBaseFileName = null;
    private String m_convDir = null;
    private String m_saveDir = null;

    /**
     * Constructor.
     *
     * @param p_logger logger to use
     */
    public MicrosoftWordHelperForTmx(Logger p_logger)
    {
        m_logger = p_logger;
    }

    //
    // Begin: Public Helper Methods
    //

    /**
     * Performs the conversion of an RTF file to HTML. The file must
     * have a unique name since it will not be copied.
     *
     * @exception MsOfficeAdapterException - When conversion to HTML
     * fails.
     */
    public String performConversion(String p_filename)
        throws MsOfficeAdapterException
    {
        try
        {
            m_logger.debug("Calling Word to convert: " + p_filename);

            m_originalFileName = p_filename;
            m_safeBaseFileName = m_originalFileName.substring(
                lastSeparatorIndex(m_originalFileName) + 1);

            setConversionDir();
            setSaveDirectory();

            moveFileToDir(m_originalFileName, m_convDir);

            convertWithOffice();

            //gather up the filenames
            String rtfFileName = m_convDir + File.separator +
                m_safeBaseFileName.substring(0,
                    m_safeBaseFileName.length() - 4) + ".rtf";

            String expectedHtmlFileName = m_convDir + File.separator +
                m_safeBaseFileName.substring(0,
                    m_safeBaseFileName.length() - 4) + ".html";

            String finalHtmlFileName = m_originalFileName.substring(0,
                m_originalFileName.length() - 4) + ".html";

            m_logger.debug("Expected main file is " + expectedHtmlFileName);

            File expectedHtmlFile = new File (expectedHtmlFileName);
            if (expectedHtmlFile.exists())
            {
                m_logger.debug("Expected main file exists!");
            }
            else
            {
                throw new Exception(
                    "conversion failed to produce the main HTML file: " +
                    expectedHtmlFileName);
            }

            moveFileToDir(expectedHtmlFileName, m_saveDir);
            new File(rtfFileName).delete();

            return finalHtmlFileName;
        }
        catch (Exception e)
        {
            m_logger.error("Could not perform conversion.", e);
            String[] errorArgs = { m_originalFileName };
            throw new MsOfficeAdapterException("Import", errorArgs, e);
        }
    }

    //
    // Begin: Private Methods
    //

    /**
     * Writes out the command file to invoke the appropriate MS Office
     * converter, and waits until the conversion has completed.
     */
    private void convertWithOffice()
        throws Exception
    {
        String commandFileName = null;

        try
        {
            //first create the command file
            String baseName = m_safeBaseFileName.substring(0,
                m_safeBaseFileName.lastIndexOf("."));

            StringBuffer commandFileNameBuffer = new StringBuffer(m_convDir);
            commandFileNameBuffer.append(File.separator).append(baseName);
            commandFileNameBuffer.append(".im_command");

            commandFileName = commandFileNameBuffer.toString();
            writeCommandFile(commandFileName);

            //now wait for status file
            StringBuffer statusFileName = new StringBuffer(m_convDir);
            statusFileName.append(File.separator).append(baseName);
            statusFileName.append(".status");

            long maxTimeToWait = s_MAXTTW * 60 * 1000;
            FileWaiter waiter = new FileWaiter(2000L, maxTimeToWait,
                statusFileName.toString());

            waiter.waitForFile();

            // conversion is done, but check the status to see if
            // there is an error
            File statusFile = new File (statusFileName.toString());
            BufferedReader reader = new BufferedReader(
                new FileReader(statusFile));

            String line = reader.readLine();
            String msg = reader.readLine();

            m_logger.info(msg);

            String errorCodeString = line.substring(6); //Error:1
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
                    File f = new File (commandFileName);
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
     * ConvertFrom=doc | rtf | html | ppt | xls
     * ConvertTo=doc | rtf | html | ppt | xls
     * AcceptChanges=true | false | NA
     */
    private void writeCommandFile(String p_commandFileName) throws Exception
    {
        String convertFrom = "ConvertFrom=rtf";
        String convertTo = "ConvertTo=html";
        String acceptChanges = "AcceptChanges=NA";

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");
        text.append(acceptChanges).append("\r\n");

        FileUtil.writeFileAtomically(
            new File(p_commandFileName), text.toString(), "US-ASCII");
    }

    private void moveFileToDir(String p_fileName, String p_directory)
        throws Exception
    {
        File dir = new File(p_directory);
        dir.mkdirs();

        File oldFile = new File(p_fileName);
        File newFile = new File(p_directory + File.separator +
            p_fileName.substring(lastSeparatorIndex(p_fileName) + 1));

        InputStream in = new BufferedInputStream(
            new FileInputStream(oldFile));
        OutputStream out = new BufferedOutputStream(
            new FileOutputStream(newFile));

        byte[] buffer = new byte[65536];
        int len;

        while ((len = in.read(buffer, 0, 65536)) >= 0)
        {
            out.write(buffer, 0, len);
        }

        out.close();
        in.close();

        oldFile.delete();
    }

    //
    // Mutators/Accessors for member data
    //

    /**
     * Determines and sets the locale specific save directory during
     * import/export process.
     */
    private void setSaveDirectory()
        throws Exception
    {
        m_saveDir = m_originalFileName.substring(0,
            lastSeparatorIndex(m_originalFileName));
    }

    /**
     * Determines and sets the content specific conversion directory,
     * for example: D:\WINFILES\word
     */
    private void setConversionDir() throws Exception
    {
        StringBuffer convDir = new StringBuffer(
            SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.MSOFFICE_CONV_DIR));

        convDir.append(File.separator);
        convDir.append("word");
        convDir.append(File.separator);
        convDir.append("en_US");

        m_convDir = convDir.toString();
    }

    /**
     * Helps in finding the last separator in the string. If the name
     * contains both / and \ then it returns the greatest index value.
     */
    private int lastSeparatorIndex(String p_name)
    {
        int forwardSlashIndex = p_name.lastIndexOf('\\');
        int backwardSlashIndex = p_name.lastIndexOf('/');
        int result = (forwardSlashIndex > backwardSlashIndex) ?
            forwardSlashIndex : backwardSlashIndex;
        return result;
    }
}
