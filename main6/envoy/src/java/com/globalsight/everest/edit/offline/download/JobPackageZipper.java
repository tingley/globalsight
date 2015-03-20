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
package com.globalsight.everest.edit.offline.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.download.BinaryResource.ParaViewResWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.DownloadWriterInterface;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.TermHelpFactory;
import com.globalsight.everest.edit.offline.page.TerminologyHelp;
import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.edit.offline.rtf.ListViewWorkDocWriter;
import com.globalsight.everest.edit.offline.rtf.ParaViewSrcDocWriter;
import com.globalsight.everest.edit.offline.rtf.ParaViewTagInfoDocWriter;
import com.globalsight.everest.edit.offline.rtf.ParaViewTermDocWriter;
import com.globalsight.everest.edit.offline.rtf.ParaViewTmDocWriter;
import com.globalsight.everest.edit.offline.rtf.ParaViewWorkDocWriter;
import com.globalsight.everest.edit.offline.rtf.RTFWriterAnsi;
import com.globalsight.everest.edit.offline.rtf.RTFWriterUnicode;
import com.globalsight.everest.edit.offline.ttx.ListViewWorkTTXWriter;
import com.globalsight.everest.edit.offline.xliff.ListViewWorkXLIFFWriter;
import com.globalsight.everest.edit.offline.xliff.xliff20.ListViewWorkXLIFF20Writer;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.ling.common.RegExException;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Zips up all job resources and sends a zipped stream to disk.
 */
public class JobPackageZipper
{
    static private final Logger CATEGORY = Logger
            .getLogger(JobPackageZipper.class);

    public String m_lastIndex = null;
    private ZipOutputStream m_zipOutputStream = null;

    //
    // Constructor
    //

    public JobPackageZipper()
    {
        super();
    }

    /**
     * This method attaches a zip stream to a file output stream and file.
     */
    public void createZipFile(File p_tmpFile) throws AmbassadorDwUpException
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(p_tmpFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
            m_zipOutputStream = new ZipOutputStream(bos);
            m_zipOutputStream.setComment("GlobalSight Download Archive");
            m_zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            m_zipOutputStream.setLevel(9);
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_WRITE_ERROR, ex);
        }
    }

    public void closeZipFile() throws AmbassadorDwUpException
    {
        try
        {
            m_zipOutputStream.close();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    public void writeHtmlPage(DownloadWriterInterface p_writer,
            String p_encoding) throws AmbassadorDwUpException
    {
        try
        {
            p_writer.write(m_zipOutputStream, p_encoding);
            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    public void writePath(String p_path) throws AmbassadorDwUpException
    {
        try
        {
            ZipEntry ze = new ZipEntry(p_path);
            m_zipOutputStream.putNextEntry(ze);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    public void writeTxtPage(OfflinePageData p_writer, DownloadParams p_params)
            throws AmbassadorDwUpException
    {
        try
        {
            p_writer.writeOfflineTextFile(m_zipOutputStream, p_params);
            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    public void writeTmxPage(OfflinePageData p_writer, DownloadParams p_params,
            int p_tmxLevel, boolean isConvertLf, int mode, boolean isPenaltyTmx)
            throws AmbassadorDwUpException
    {
        try
        {
            p_writer.setIsConvertLf(isConvertLf);
            p_writer.writeOfflineTmxFile(m_zipOutputStream, p_params,
                    p_tmxLevel, mode, isPenaltyTmx);
            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    public void writeTmxPage(OfflinePageData p_writer, DownloadParams p_params,
            int p_tmxLevel) throws AmbassadorDwUpException
    {
        writeTmxPage(p_writer, p_params, p_tmxLevel, false,
                TmxUtil.TMX_MODE_INC_ALL, false);
    }

    private List<TermLeverageMatchResult> getTermMatchs(OfflinePageData page)
    {
        List<TermLeverageMatchResult> matches = new ArrayList<TermLeverageMatchResult>();
        Vector<OfflineSegmentData> segments = page.getSegmentList();
        for (OfflineSegmentData segment : segments)
        {
            if (segment.getTermLeverageMatchList() != null)
            {
                matches.addAll(segment.getTermLeverageMatchList());
            }
        }

        return matches;
    }

    public void writeTermPage(OfflinePageData page,
            DownloadParams downloadParams)
    {
        String format = downloadParams.getTermFormat();
        if (!OfflineConstants.TERM_NONE.equals(format))
        {
            TerminologyHelp help = TermHelpFactory.newInstance(format);
            help.setUserName(downloadParams.getUser().getUserId());

            Locale srclocale = GlobalSightLocale.makeLocaleFromString(page
                    .getSourceLocaleName());
            Locale trglocale = GlobalSightLocale.makeLocaleFromString(page
                    .getTargetLocaleName());
            String content = help.convert(getTermMatchs(page), srclocale,
                    trglocale);

            try
            {
                FileUtil.writeBom(m_zipOutputStream, FileUtil.UTF8);
                OutputStreamWriter write = new OutputStreamWriter(
                        m_zipOutputStream, FileUtil.UTF8);
                write.write(content);
                write.flush();
                m_zipOutputStream.closeEntry();
            }
            catch (IOException e)
            {
                CATEGORY.error(e.getMessage(), e);
                throw new AmbassadorDwUpException(
                        AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, e);
            }
        }
    }

    public void writeAnsiRtfPage(OfflinePageData p_page,
            RTFWriterAnsi p_writer, DownloadParams p_downloadParams)
            throws AmbassadorDwUpException
    {
        try
        {
            p_writer.writeRTF(p_page, m_zipOutputStream,
                    p_downloadParams.isDownloadForTrados());
            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
        catch (RegExException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_FATAL_ERROR, ex);
        }
    }

    public void writeUnicodeRtfPage(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        try
        {
            ListViewWorkDocWriter writer = new ListViewWorkDocWriter();

            if (p_downloadParams.isDownloadForTrados())
            {
                writer.setTradosOutput();
            }
            if (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED)
            {
                writer.setTradosOutputOptimized();
            }
            writer.setResInsertOption(p_downloadParams.getResInsOption());
            writer.setUser(p_downloadParams.getUser());
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));
            // p_writer.writeRTF(p_page, m_zipOutputStream,
            // p_downloadParams.isDownloadForTrados(),
            // p_downloadParams.getResInsOption());
            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes the Work Document for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @param p_uniqueBinResFname
     *            the associated binary resource filename
     * @param p_uniqueResIdxFname
     *            the associated index filename
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewWorkDoc(OfflinePageData p_page,
            DownloadParams p_downloadParams, String p_uniqueBinResFname,
            String p_uniqueResIdxFname) throws AmbassadorDwUpException
    {
        ParaViewWorkDocWriter writer = new ParaViewWorkDocWriter(
                p_uniqueBinResFname, p_uniqueResIdxFname,
                p_downloadParams.getUser());

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));

            m_zipOutputStream.closeEntry();

            return getRtfWriterStatus(writer);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes the Source Document for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewSrcDoc(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        ParaViewSrcDocWriter writer = new ParaViewSrcDocWriter();

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));

            m_zipOutputStream.closeEntry();

            return getRtfWriterStatus(writer);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes the TM Document for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewTmDoc(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        ParaViewTmDocWriter writer = new ParaViewTmDocWriter();

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));

            m_zipOutputStream.closeEntry();

            return getRtfWriterStatus(writer);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes a binary resource for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @param p_trgFname
     *            name of the associated target file
     * @param p_idxFname
     *            name of the associated index file
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewBinaryRes(String p_wrkDocFname,
            OfflinePageData p_page, DownloadParams p_downloadParams,
            String p_resFname, String p_idxFname)
            throws AmbassadorDwUpException
    {
        ParaViewResWriter writer = new ParaViewResWriter(p_wrkDocFname,
                p_resFname, p_idxFname);

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));
            m_zipOutputStream.closeEntry();

            m_lastIndex = writer.getIndex();

            // Note: For the binary writer, we do not care about MS-Word
            // Field and Bookmark overload conditions since we are not
            // creating full RTF resource documents that have to be parsed
            // and loaded by word. Instead, we are creating RTF snippets
            // that are store in the binary file and used one at a time.

            return DownLoadApi.MSGID_FILE_ADD_OK;
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes a binary resource index for the last binary file written.
     * 
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewBinaryResIdx() throws AmbassadorDwUpException
    {
        // ParaViewResWriter writer = new ParaViewResWriter();

        try
        {
            OutputStreamWriter osw = new OutputStreamWriter(m_zipOutputStream,
                    "ASCII");

            osw.write(m_lastIndex);
            osw.flush();
            m_zipOutputStream.closeEntry();

            return DownLoadApi.MSGID_FILE_ADD_OK;
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            return DownLoadApi.MSGID_FAILED_TO_WRITE_INDEX;
        }
    }

    /**
     * Writes the TAG info Document for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewTagInfoDoc(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        ParaViewTagInfoDocWriter writer = new ParaViewTagInfoDocWriter();

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));
            m_zipOutputStream.closeEntry();
            return getRtfWriterStatus(writer);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Writes the Term Document for the msWord client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * @return a status ID (see downLoadApi MSGID_xxxxx)
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public int writeParaViewTermDoc(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        ParaViewTermDocWriter writer = new ParaViewTermDocWriter();

        try
        {
            setPreserveSourceFolder(p_downloadParams, p_page);
            writer.write(p_page, m_zipOutputStream, GlobalSightLocale
                    .makeLocaleFromString(p_downloadParams.getUiLocale()));
            m_zipOutputStream.closeEntry();
            return getRtfWriterStatus(writer);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    /**
     * Copies any file to the Zip output stream.
     * 
     * @param p_fis
     *            an input stream.
     * @exception AmbassadorDwUpException.
     */
    public void writeFile(InputStream p_input) throws AmbassadorDwUpException
    {
        try
        {
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Read/write chunks of bytes until we reach the end of the file .
            while ((bytesRead = p_input.read(buffer)) != -1)
            {
                m_zipOutputStream.write(buffer, 0, bytesRead);
            }

            m_zipOutputStream.closeEntry();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    private int getRtfWriterStatus(RTFWriterUnicode p_writer)
    {
        if (p_writer.isMSWord2000BmLimitExceeded())
        {
            return DownLoadApi.MSGID_WD2000_BM_CNT_EXCEEDED;
        }
        else if (p_writer.isMSWord2000FieldLimitExceeded())
        {
            return DownLoadApi.MSGID_WD2000_FLD_CNT_EXCEEDED;
        }
        else
        {
            return DownLoadApi.MSGID_FILE_ADD_OK;
        }
    }

    /**
     * Writes the xliff Document for the client environment.
     * 
     * @param p_page
     *            the Offline Page (OfflinePageData)
     * @param p_downloadParams
     *            the download parameters
     * 
     * @exception com.globalsight.everest.edit.offline.AmbassadorDwUpException
     */
    public void writeUnicodeXliffPage(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        try
        {
            if (p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF20)
            {
                ListViewWorkXLIFF20Writer writer = new ListViewWorkXLIFF20Writer();

                writer.write(p_downloadParams, p_page, m_zipOutputStream,
                        GlobalSightLocale.makeLocaleFromString(p_downloadParams
                                .getUiLocale()));
            }
            else
            {
                ListViewWorkXLIFFWriter writer = new ListViewWorkXLIFFWriter();

                writer.write(p_downloadParams, p_page, m_zipOutputStream,
                        GlobalSightLocale.makeLocaleFromString(p_downloadParams
                                .getUiLocale()));
            }
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
        finally
        {
            try
            {
                m_zipOutputStream.closeEntry();
            }
            catch (IOException e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }
    }

    public void writeUnicodeTTXPage(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        try
        {
            ListViewWorkTTXWriter writer = new ListViewWorkTTXWriter();

            writer.write(p_downloadParams, p_page, m_zipOutputStream,
                    GlobalSightLocale.makeLocaleFromString(p_downloadParams
                            .getUiLocale()));
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
        finally
        {
            try
            {
                m_zipOutputStream.closeEntry();
            }
            catch (IOException e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }
    }

    private void setPreserveSourceFolder(DownloadParams p_downloadParams,
            OfflinePageData p_page)
    {
        if (p_downloadParams.isPreserveSourceFolder())
        {
            p_page.setPreserveSourceFolder(true);
        }
    }
}
