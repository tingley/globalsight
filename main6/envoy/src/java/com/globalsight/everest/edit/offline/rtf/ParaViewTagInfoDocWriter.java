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

package com.globalsight.everest.edit.offline.rtf;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tw.HtmlTableWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * <p>Generates an RTF file from an OfflinePageData object.</p>
 *
 * <p>The output is written as ASCII characters to the provided
 * OutputStream.  Unicode or double-byte characters are converted to
 * the proper RTF codes as necessary.  Note that RTF files are always
 * ASCII files.</p>
 *
 */
public class ParaViewTagInfoDocWriter
    extends RTFWriterUnicode
{
    static private final Logger c_logger =
        Logger.getLogger(
            ParaViewTagInfoDocWriter.class);

    //
    // Public Constants
    //

    /**
     * A unique string that identifies this file format.
     *
     * This string is stored in the RTF info section under "title".
     * This string is used by regular expressions to recognize our
     * files programatically.
     *
     * See UploadPageHandler where document recognition is
     * performed.
     */
    static public final String DOCUMENTTITLE =
        "GlobalSight Extracted Paragraph view - Static Tag Resources";


    //
    // Private & Protected Constants
    //

    static private final boolean _ENABLE_DEBUG_OF_DOWNLOAD_ABORT = false;


    //////////////////////////////////////
    // Constructors
    //////////////////////////////////////

    /**
     * Constructs an RTFWriterUnicode without setting the OfflinePageData.
     * You must call setOfflinePageData() before writing can begin.
     */
    public ParaViewTagInfoDocWriter()
    {
        super();
    }

    //////////////////////////////////////
    // Public Methods
    //////////////////////////////////////

    /**
     * Writes the main body of the document. Classes that extend
     * RTFWriterUnicode method should overide this method to create
     * the document of choice.
     */
    public void writeRTF_docBody()
        throws IOException
    {
        if (_ENABLE_DEBUG_OF_DOWNLOAD_ABORT)
        {
            // Used to test download abort messages:
            // Word limit is 16379 bookmarks. We allow up to 16369 to reserve a
            // few for runtime creation in word. The word limit for fields is
            // 32000.
            debug_writeRTF_dummyBookmarks(16369);
            debug_writeRTF_dummyFields(32000);
        }
        else
        {
            writeTagResourceSection();
        }
    }

    /**
     * Generates an RTF {\info} block with creation date, document
     * title etc.
     */
    public String makeRTF_infoSection()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{\\info");

        sb.append("{\\title ");
        sb.append(DOCUMENTTITLE);
        sb.append("}");
        sb.append(m_strEOL);

        sb.append("{\\author ");
        sb.append(PRODUCTNAME);
        sb.append("}");
        sb.append(m_strEOL);

        Calendar dt_creationDate = GregorianCalendar.getInstance();
        sb.append("{\\creatim ");
        sb.append("\\yr").append(dt_creationDate.get(Calendar.YEAR));
        sb.append("\\mo").append(dt_creationDate.get(Calendar.MONTH));
        sb.append("\\dy").append(dt_creationDate.get(Calendar.DAY_OF_MONTH));
        sb.append("\\hr").append(dt_creationDate.get(Calendar.HOUR_OF_DAY));
        sb.append("\\min").append(dt_creationDate.get(Calendar.MINUTE));
        sb.append("}");
        sb.append(m_strEOL);
        sb.append("}");

        return sb.toString();
    }

    /**
     * Creates the complete docment variables section.
     */
    public String makeRTF_documentVariables()
        throws AmbassadorDwUpException
    {
        StringBuffer sb = new StringBuffer();

        sb.append(makeRTF_docVar(DOCVAR_NAME_DOCVERSION, WC_VERSION));

        return sb.toString();
    }

    public String makeRTF_documentDefaults()
    {
        // get basic document defaults
        String commonDefaults = "";

        try
        {
            commonDefaults = m_resource.getString("DocumentDefaults");
        }
        catch (MissingResourceException ex)
        {
            c_logger.warn("Could not load RTF document default view options", ex);
        }

        StringBuffer sb = new StringBuffer();

        sb.append("\\margl720\\margr720");
        sb.append(commonDefaults);

        return sb.toString();
    }


    /**
     * Writes the document header - encoding, formats, languages etc.
     */
    public void writeRTF_docHeader()
        throws IOException, AmbassadorDwUpException
    {
        StringBuffer fldInst = new StringBuffer();

        m_outputStream.write("\\par { ");
        m_outputStream.write(m_strLabelAndHeaderTextStyle);
        m_outputStream.write("\\b========================\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("DO NOT ALTER THIS FILE\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("========================\\par");
        m_outputStream.write("}\\par ");
        m_outputStream.write(m_strEOL);
    }

    public String makeRTF_templateAttachment()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{\\*\\template ");
        sb.append(" " /*MSWORD_TRANS_TEMPLATE*/);
        sb.append("}");
        return sb.toString();
    }

    //////////////////////////////////////
    // Private Methods
    //////////////////////////////////////
    private void writeTagResourceSection()
        throws IOException
    {
        OfflineSegmentData offlineTu = null;
        String pTag = null;
        String nativeTag = null;

        m_outputStream.write("\\par {");
        m_outputStream.write(m_strLabelAndHeaderTextStyle);
        m_outputStream.write("\\b=================================================================\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("Static Tag Resources\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("=================================================================");
        m_outputStream.write("}\\par ");
        m_outputStream.write(m_strEOL);

        ListIterator segIt = m_page.getSegmentIterator();
        while (segIt.hasNext())
        {
            offlineTu = (OfflineSegmentData)segIt.next();

            m_outputStream.write("\\par {");
            m_outputStream.write(m_strLabelAndHeaderTextStyle);
            m_outputStream.write(makeBookmarkName(offlineTu, null)); // use bookmark name as a visual reference
            m_outputStream.write("}\\par ");

            // Tags
            if (!isMSWord2000BmLimitExceeded())
            {
                Map tagMap = offlineTu.getPTag2NativeMap();
                if (tagMap != null && tagMap.size() > 0)
                {
                    String keys[] = HtmlTableWriter.getSortedPtagKeys((Hashtable)tagMap);
                    if (keys.length > 0)
                    {
                        m_outputStream.write(makeRTF_bookmark(
                            true, makeBookmarkName(offlineTu, null)));
                        m_outputStream.write("{");

                        // add tag entries
                        for (int i = 0; i < keys.length; )
                        {
                            pTag = keys[i];
                            nativeTag = (String)tagMap.get(pTag);
                            m_outputStream.write(formatTagMap(pTag, nativeTag));
                            i++;
                            if (i < keys.length)
                            {
                                // special record delimiter that will not exist in content
                                m_outputStream.write("[*1]");
                            }
                            m_outputStream.write(m_strEOL);
                        }
                        m_outputStream.write("\\par }");
                        m_outputStream.write(makeRTF_bookmark(
                            false, makeBookmarkName(offlineTu, null)));
                    }
                }
            }
        }
    }

    private String formatTagMap(String p_pTag, String p_nativeTag)
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "{" + m_strSourceLcid + FONT_SOURCE + SIZE_12PT + COLOR_BLUE + " ");
        // special tag pair delimiter that will not exist in content
        sb.append(encodeText(p_pTag) + "[*2]" + encodeText(p_nativeTag));
        sb.append("}");

        return sb.toString();
    }
}


