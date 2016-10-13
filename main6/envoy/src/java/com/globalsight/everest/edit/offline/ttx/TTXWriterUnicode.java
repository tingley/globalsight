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
package com.globalsight.everest.edit.offline.ttx;

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

public abstract class TTXWriterUnicode extends TTXUnicode implements WriterInterface
{
    static protected String TTX_ENCODING = FileUtil.UTF16LE;

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
    
    protected DownloadParams m_downloadParams = null;

    public TTXWriterUnicode()
    {
    }

    protected abstract void writeTTXFrontMatter(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException;
    
    protected abstract void writeTTXHeaderInfo(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException;

    protected abstract void writeTTXDocBody() throws AmbassadorDwUpException;

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
        m_downloadParams = p_downloadParams;
        m_outputStream = new OutputStreamWriter(p_outputStream, TTX_ENCODING);
        FileUtil.writeBom(p_outputStream, TTX_ENCODING);
        m_page = p_page;
        m_uiLocale = p_uiLocale;

        writeTTX(p_downloadParams);

        m_outputStream.flush();
    }

    public void writeTTX(OutputStream p_outputStream, Locale p_uiLocale)
            throws IOException, AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter(p_outputStream, "ASCII");
        m_outputStream.flush();
    }

    /**
     * Changes "_" to "-". For example, change "en_US" to "en-US".
     * 
     * @param locale
     * @return
     */
    protected String changeLocaleToTTXFormat(String locale)
    {
        if (locale == null)
            return locale;

        return locale.replace("_", "-");
    }

    private void writeTTX(DownloadParams p_downloadParams) throws IOException, AmbassadorDwUpException
    {
    	setDefaults();
    	writeTTXStart();
    	writeTTXFrontMatter(p_downloadParams);
    	writeTTXHeaderInfo(p_downloadParams);
    	writeTTXDocBody();
    	writeTTXEnd();
    }
    
    private void writeTTXStart() throws IOException, AmbassadorDwUpException
    {
        m_outputStream.write("<?xml version='1.0'?>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<TRADOStag Version=\"2.0\">");
    }
    
    private void writeTTXEnd() throws IOException
    {
    	m_outputStream.write("</Raw>");
        m_outputStream.write("</Body>");
        m_outputStream.write("</TRADOStag>");
        m_outputStream.write(m_strEOL);
    }
    
}
