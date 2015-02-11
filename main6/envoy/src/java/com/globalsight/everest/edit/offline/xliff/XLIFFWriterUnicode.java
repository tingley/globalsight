package com.globalsight.everest.edit.offline.xliff;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.download.WriterInterface;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public abstract class XLIFFWriterUnicode extends XLIFFUnicode implements
        WriterInterface
{
    static protected String XLIFF_ENCODING = FileUtil.UTF16LE;

    /**
     * A resource bundle that contains our XLF strings.
     */
    static protected ResourceBundle m_resource;
    static private Set m_resourceKeys;
    static
    {
        m_resource = ResourceBundle
                .getBundle("com.globalsight.everest.edit.offline.rtf.UnicodeRTF");
        m_resourceKeys = new HashSet();
        Enumeration e = m_resource.getKeys();
        while (e.hasMoreElements())
        {
            m_resourceKeys.add(e.nextElement());
        }
    }

    /**
     * A resource bundle that contains the UI related strings that appear in XLF
     * files.
     */
    static protected ResourceBundle m_localeRes;

    /**
     * The info section of the XLF header. Specifies document title, author,
     * creation and revision dates etc.
     */
    protected String m_strEOL = "\r\n";
    protected String m_space = "  ";

    /**
     * The input page object from which the XLF output is derived.
     */
    protected OfflinePageData m_page;

    /**
     * The ui locale. Used to write comments and other header text when
     * possible.
     */
    protected Locale m_uiLocale = GlobalSightLocale
            .makeLocaleFromString("en_US");

    /**
     * The output stream to which we write the XLF results.
     */
    protected OutputStreamWriter m_outputStream;

    public XLIFFWriterUnicode()
    {
    }

    protected abstract void writeXlfDocHeader(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException;

    protected abstract void writeXlfDocBody(boolean isTmx, int TMEditType,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException;

    protected void setOfflinePageData(OfflinePageData p_page, Locale p_uiLocale)
    {
        m_page = p_page;
    }

    protected void setDefaults()
    {
        m_localeRes = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }

    public void write(DownloadParams p_downloadParams, OfflinePageData p_page,
            OutputStream p_outputStream, Locale p_uiLocale) throws IOException,
            AmbassadorDwUpException
    {

        m_outputStream = new OutputStreamWriter(p_outputStream, XLIFF_ENCODING);
        FileUtil.writeBom(p_outputStream, XLIFF_ENCODING);
        m_page = p_page;
        m_uiLocale = p_uiLocale;

        writeXLF(p_downloadParams);

        m_outputStream.flush();
    }

    public void writeXLF(OutputStream p_outputStream, Locale p_uiLocale)
            throws IOException, AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter(p_outputStream, "ASCII");
        // writeXLF(true);
        m_outputStream.flush();
    }

    /**
     * Changes "_" to "-". For example, change "en_US" to "en-US".
     * 
     * @param locale
     * @return
     */
    protected String changeLocaleToXlfFormat(String locale)
    {
        if (locale == null)
            return locale;

        return locale.replace("_", "-");
    }

    private void writeXLF(DownloadParams p_downloadParams) throws IOException,
            AmbassadorDwUpException
    {
        boolean isTmx = true;
        int TMEditType = p_downloadParams.getTMEditType();
//        boolean editAll = p_downloadParams.getDownloadEditAllState() == AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_YES;
        if (p_downloadParams.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_ATNS
                || p_downloadParams.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH)
        {
            isTmx = false;
        }
        setDefaults();
        writeXlfHeader();
        writeXlfDocHeader(p_downloadParams);
        //writeXlfDocBody(isTmx, editAll, p_downloadParams);
        writeXlfDocBody(isTmx, TMEditType, p_downloadParams);
        writeXlfEnd();
    }

    private void writeXlfHeader() throws IOException, AmbassadorDwUpException
    {
        m_outputStream.write("<?xml version=\"1.0\" encoding=\""
                + XLIFF_ENCODING + "\"?>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<xliff version=\"1.2\">");
        m_outputStream.write(m_strEOL);
    }

    private void writeXlfEnd() throws IOException
    {
        m_outputStream.write("</body>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</file>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</xliff>");
    }
}
