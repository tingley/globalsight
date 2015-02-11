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
package com.globalsight.ling.aligner;

import org.apache.log4j.Logger;

import com.globalsight.ling.aligner.AlignmentResult;
import com.globalsight.ling.aligner.engine.GxmlAligner;
import com.globalsight.ling.aligner.io.GxmlReader;
import com.globalsight.ling.aligner.io.TmxReader;
import com.globalsight.ling.aligner.io.TmxWriter;
import com.globalsight.ling.aligner.io.GamReader;
import com.globalsight.ling.aligner.io.GamWriter;
import com.globalsight.ling.aligner.gxml.AlignmentPage;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GeneralException;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.servlet.util.ServerProxy;


import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.PrintWriter;


/**
 * AlignmentUnit holds information for a source and target pair
 * alignment. The actual alignment result and TMX files are not stored
 * in this object. They are persisted elsewhere.
 */
public class AlignmentUnit
{
    private static final Logger c_logger =
        Logger.getLogger(
            AlignmentUnit.class.getName());

    static public final int BUFSIZE = 4096;

    public static final String EXTRACTED = "EXTRACTED";
    public static final String PRE_ALIGNED = "PRE-ALIGNED";
    public static final String COMPLETED = "COMPLETED";
    public static final String MISMATCH = "MISMATCH";
    public static final String ALIGN_FAILED = "ALIGN-FAILED";
    public static final String INITIALIZED = "INITIALIZED";

    // project temp directory
    private File m_projectTmpDir;

    // original source and target file names
    private String m_originalSourceFile;
    private String m_originalTargetFile;

    // source and target GXML file names
    private String m_sourceGxml;
    private String m_targetGxml;

    // source and target TMX file names
    private String m_sourceTmx;
    private String m_targetTmx;

    // source and target CorpusDoc objects
    private CorpusDoc m_sourceCorpusDoc;
    private CorpusDoc m_targetCorpusDoc;

    // GAM file name
    private String m_gam;

    // alignment state
    private String m_state = INITIALIZED;

    // error message
    private String m_errorMessage = null;


    public AlignmentUnit(String p_originalSourceFile,
        String p_originalTargetFile, File p_projectTmpDir)
    {
        m_originalSourceFile = p_originalSourceFile;
        m_originalTargetFile = p_originalTargetFile;

        m_projectTmpDir = p_projectTmpDir;
    }


    /**
     * Aligns the source and target file. This method does the following.
     * 1. Take translatable segments from those GXMLs and write source
     *    and target TMXs in the given directory.
     * 2. Align GXMLs and create GAM.
     * 3. Write the GAM to the given directory.
     *
     * The source and target GXML files must be stored in the project
     * temp directory prior to calling this method.
     *
     * @param p_sourceLocale Source locale
     * @param p_targetLocale Target locale
     * @param p_fileType File type of the original files (html, xml, doc, etc)
     * @param p_sourceFileEncoding Original source file encoding
     * @param p_targetFileEncoding Original target file encoding
     * @param p_xmlRule Xml rule file (can be null if the file type is not XML)
     */
    public void alignFiles(GlobalSightLocale p_sourceLocale,
        GlobalSightLocale p_targetLocale, KnownFormatType p_formatType,
        String p_sourceFileEncoding, String p_targetFileEncoding,
        XmlRuleFile p_xmlRule)
    {
        try
        {
            // read source and target GXML files from the disk
            File sourceGxmlFile = new File(m_projectTmpDir, m_sourceGxml);
            String sourceGxml = readGxml(sourceGxmlFile);

            File targetGxmlFile = new File(m_projectTmpDir, m_targetGxml);
            String targetGxml = readGxml(targetGxmlFile);

            // create AlignmentPage objects from GXMLs
            GxmlReader gxmlReader = new GxmlReader();
            AlignmentPage sourcePage
                = gxmlReader.createAlignmentPage(sourceGxml, p_sourceLocale);

            //overwrite the original src GXML
            ServerProxy.getNativeFileManager().save(
                gxmlReader.getGxml(), "UTF8",
                m_sourceCorpusDoc.getGxmlPath());

            AlignmentPage targetPage
                = gxmlReader.createAlignmentPage(targetGxml, p_targetLocale);

            //overwrite the original src GXML
            ServerProxy.getNativeFileManager().save(
                gxmlReader.getGxml(), "UTF8",
                m_targetCorpusDoc.getGxmlPath());

            // write TMX files
            File sourceTmxFile = createTmxFile(
                m_originalSourceFile, p_sourceLocale);
            writeTmx(sourcePage, sourceTmxFile,
                p_sourceLocale, p_formatType.getFormatType());
            setSourceTmxFileName(sourceTmxFile.getName());

            File targetTmxFile = createTmxFile(
                m_originalTargetFile, p_targetLocale);
            writeTmx(targetPage, targetTmxFile,
                p_targetLocale, p_formatType.getFormatType());
            setTargetTmxFileName(targetTmxFile.getName());

            // Align
            AlignmentResult alignmentResult
                = GxmlAligner.align(sourcePage, targetPage,
                    isBlockAlignmentNecessary(p_formatType));

            alignmentResult.setSourceTmxFileName(sourceTmxFile.getName());
            alignmentResult.setTargetTmxFileName(targetTmxFile.getName());

            // write GAM file
            File gamFile = createGamFile(m_originalSourceFile);
            writeGam(alignmentResult, gamFile);
            setGamFileName(gamFile.getName());

            // set state
            setState(PRE_ALIGNED);
        }
        catch(Exception e)
        {
            setState(ALIGN_FAILED);

            // log the error
            c_logger.error(e.getMessage(), e);

            // process error
            if(e instanceof GeneralException)
            {
                m_errorMessage
                    = ((GeneralException)e).getMessage(Locale.getDefault());
            }
            else
            {
                m_errorMessage = e.getMessage();
            }

            // exception is not re-throwed. The alignment must be continued.

        }
    }


    /**
     * Get alignment result by reading TMX and GAM files.
     *
     * @param p_sourceFileEncoding Original source file encoding
     * @param p_targetFileEncoding Original target file encoding
     * @return AlignmentResult object
     */
    public AlignmentResult getAlignmentResult(
        GlobalSightLocale p_sourceFileEncoding,
        GlobalSightLocale p_targetFileEncoding)
        throws Exception
    {
        // read source TMX file
        TmxReader tmxReader = new TmxReader();
        File sourceTmxFile = new File(m_projectTmpDir, m_sourceTmx);
        Reader sourceTmxStream = new InputStreamReader(
            new FileInputStream(sourceTmxFile), "UTF-8");
        List sourceTuvs = tmxReader.read(
            sourceTmxStream, p_sourceFileEncoding, m_sourceTmx);
        sourceTmxStream.close();

        // read target TMX file
        File targetTmxFile = new File(m_projectTmpDir, m_targetTmx);
        Reader targetTmxStream = new InputStreamReader(
            new FileInputStream(targetTmxFile), "UTF-8");
        List targetTuvs = tmxReader.read(
            targetTmxStream, p_targetFileEncoding, m_targetTmx);
        targetTmxStream.close();

        // read GAM file
        GamReader gamReader = new GamReader();
        File gamFile = new File(m_projectTmpDir, m_gam);
        Reader gamStream
            = new InputStreamReader(new FileInputStream(gamFile), "UTF-8");
        AlignmentResult result
            = gamReader.read(gamStream, sourceTuvs, targetTuvs, m_gam);
        gamStream.close();

        return result;
    }


    public void setSourceGxml(
        String p_sourceGxml, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        File gxmlFile = createGxmlFile(
            m_originalSourceFile, p_sourceLocale);

        m_sourceGxml = gxmlFile.getName();

        writeGxml(gxmlFile, p_sourceGxml);
    }


    public void setTargetGxml(
        String p_targetGxml, GlobalSightLocale p_targetLocale)
        throws Exception
    {
        File gxmlFile = createGxmlFile(
            m_originalTargetFile, p_targetLocale);

        m_targetGxml = gxmlFile.getName();

        writeGxml(gxmlFile, p_targetGxml);
    }


    public void setSourceTmxFileName(String p_sourceTmx)
    {
        m_sourceTmx = p_sourceTmx;
    }


    public void setTargetTmxFileName(String p_targetTmx)
    {
        m_targetTmx = p_targetTmx;
    }


    public void setGamFileName(String p_gam)
    {
        m_gam = p_gam;
    }


    public void setState(String p_state)
    {
        m_state = p_state;
    }


    public void setSourceCorpusDoc(CorpusDoc p_corpusDoc)
    {
        m_sourceCorpusDoc = p_corpusDoc;
    }


    public void setTargetCorpusDoc(CorpusDoc p_corpusDoc)
    {
        m_targetCorpusDoc = p_corpusDoc;
    }


    public String getOriginalSourceFileName()
    {
        return m_originalSourceFile;
    }


    public String getOriginalTargetFileName()
    {
        return m_originalTargetFile;
    }


    public String getSourceTmxFileName()
    {
        return m_sourceTmx;
    }


    public String getTargetTmxFileName()
    {
        return m_targetTmx;
    }


    public CorpusDoc getSourceCorpusDoc()
    {
        return m_sourceCorpusDoc;
    }


    public CorpusDoc getTargetCorpusDoc()
    {
        return m_targetCorpusDoc;
    }


    public String getGamFileName()
    {
        return m_gam;
    }


    public String getState()
    {
        return m_state;
    }


    public void setErrorMessage(String p_errorMessage)
    {
        m_errorMessage = p_errorMessage;
    }

    public String getErrorMessage()
    {
        String errorMessage = null;

        if(m_errorMessage != null)
        {
            errorMessage = "Error occured while aligning "
                + m_originalSourceFile + " and " + m_originalTargetFile
                + ".\n" + m_errorMessage;
        }

        return errorMessage;
    }


    // private methods

    private File createGxmlFile(
        String p_originalFileName, GlobalSightLocale p_locale)
        throws Exception
    {
        String prefix = createFilePrefix(p_originalFileName);
        prefix = prefix + "_" + p_locale.toString();

        return File.createTempFile(prefix, ".gxml", m_projectTmpDir);
    }

    private File createTmxFile(
        String p_originalFileName, GlobalSightLocale p_locale)
        throws Exception
    {
        String prefix = createFilePrefix(p_originalFileName);
        prefix = prefix + "_" + p_locale.toString();

        return File.createTempFile(prefix, ".tmx", m_projectTmpDir);
    }

    private File createGamFile(String p_originalFileName)
        throws Exception
    {
        String prefix = createFilePrefix(p_originalFileName);
        // Believe it or not, the prefix must have 3 chars minimum.
        while (prefix.length() < 3)
        {
            prefix = prefix + "0";
        }
        return File.createTempFile(prefix, ".gam", m_projectTmpDir);
    }


    private String createFilePrefix(String p_originalFileName)
    {
        String asciiName = asciinizeFileName(p_originalFileName);

        File file = new File(asciiName);
        String fileName = file.getName();
        int idxExtension = fileName.indexOf('.');
        if(idxExtension != -1)
        {
            fileName = fileName.substring(0, idxExtension);
        }

        return fileName;
    }


    // converts non-ascii characters in a file name to ascii
    // representation for a safer IO. Non-ascii characters are
    // converted to hex number enclosed with square brackets such as
    // [45f6].
    private String asciinizeFileName(String p_fileName)
    {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < p_fileName.length(); i++)
        {
            char c = p_fileName.charAt(i);

            // pass through only ascii characters.
            if(c >= 0x80)
            {
                sb.append("[").append(Integer.toHexString(c)).append("]");
            }
            else
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }


    private void writeTmx(AlignmentPage p_page, File p_tmxFile,
        GlobalSightLocale p_sourceLocale, String p_fileType)
        throws Exception
    {
        PrintWriter output
            = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(p_tmxFile), "UTF-8")));

        List segments = p_page.getAllSegments();
        List tus = new ArrayList(segments.size());
        Iterator it = segments.iterator();
        while(it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv)it.next();
            tus.add(tuv.getTu());
        }

        TmxWriter tmxWriter = new TmxWriter(output);
        tmxWriter.write(tus, p_sourceLocale, p_fileType);

        output.close();
    }


    private void writeGam(AlignmentResult p_alignmentResult, File p_gamFile)
        throws Exception
    {
        PrintWriter output
            = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(p_gamFile), "UTF-8")));

        GamWriter gamWriter = new GamWriter(output);
        gamWriter.write(p_alignmentResult);

        output.close();
    }


    private String readGxml(File p_gxmlFile)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(p_gxmlFile), "UTF-8"));

        char[] buf = new char[BUFSIZE];
        int readLen = 0;

        while((readLen = reader.read(buf, 0, BUFSIZE)) != -1)
        {
            sb.append(buf, 0, readLen);
        }
        reader.close();

        return sb.toString();
    }

    private void writeGxml(File p_gxmlFile, String p_gxml)
        throws Exception
    {
        BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(p_gxmlFile), "UTF-8"));

        StringReader strReader = new StringReader(p_gxml);

        char[] buf = new char[BUFSIZE];
        int readLen = 0;

        while((readLen = strReader.read(buf, 0, BUFSIZE)) != -1)
        {
            writer.write(buf, 0, readLen);
        }

        writer.close();
    }

    private boolean isBlockAlignmentNecessary(KnownFormatType p_format)
    {
        boolean performsBlockAlignment = true;
        
        String formatType = p_format.getFormatType();
        if(formatType.equals("word-html")
            || formatType.equals("excel-html")
            || formatType.equals("powerpoint-html"))
        {
            performsBlockAlignment = false;
        }

        return performsBlockAlignment;
    }
    
}


