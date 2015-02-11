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
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.Text;


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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * <p>Generates an RTF file from an OfflinePageData object.</p>
 *
 * <p>The output is written as ASCII characters to the provided OutputStream.  Unicode
 * or double-byte characters are converted to the proper RTF codes
 * as necessary.  Note that RTF files are always ASCII files.</p>
 *
 */
public class ParaViewSrcDocWriter extends RTFWriterUnicode
{
    //////////////////////////////////////
    // Public Constants                 //
    //////////////////////////////////////
    
    private static final boolean _ENABLE_DEBUG_OF_DOWNLOAD_ABORT = false;
    
    /** A unique string that identifies this file format.
        This string is stored in the RTF info section under "title". 
        This string is used regular expressions to recognize our files programatically. 
        See UploadPageHandler where document recognition is performed.**/    
    public static final String DOCUMENTTITLE = "GlobalSight Extracted Paragraph view - Static Source Resources";
     

    // RTF DOCUMENT VARIABLE NAMES
    public static final String DOCVAR_NAME_HIGHEST_TUID = "highestTuId";
    /** name of the variable that holds the lowest TuId value */    
    public static final String DOCVAR_NAME_LOWEST_TUID ="lowestTuId";
    /** name of the variable that holds the target LCID */
    public static final String DOCVAR_TRG_LCID = "trgLCID";
    /** name of the variable that holds the source LCID */
    public static final String DOCVAR_SRC_LCID = "srcLCID";    
        
    /* bookmark strings */
    /** Bookmark suffix for the source text */
    public static final String BM_SUFFIX_SOURCE = "_s";

    //////////////////////////////////////
    // Private & Protected Constants
    //////////////////////////////////////
    private static final Logger c_logger =
        Logger.getLogger(
            ParaViewSrcDocWriter.class);
        
   
    //////////////////////////////////////
    // Private Member Variables
    //////////////////////////////////////

    
    //////////////////////////////////////
    // Constructors
    //////////////////////////////////////

    /**
     * Constructs an RTFWriterUnicode without setting the OfflinePageData. 
     * You must call setOfflinePageData() before writing can begin.
     */
    public ParaViewSrcDocWriter()
    {
        super();
        
    }

    //////////////////////////////////////
    // Public Methods
    //////////////////////////////////////
     
    /**
     * Writes the main body of the document. Classes that extend RTFWriterUnicode
     * method should overide this method to create the document of choice.
     */
    public void writeRTF_docBody()
        throws IOException
    {   
        if(_ENABLE_DEBUG_OF_DOWNLOAD_ABORT)
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
            writeSourceSection(); 
        }
    } 

    /**
     * Generates an RTF {\info} block with creation date, document
     * title etc.
     */
    public String makeRTF_infoSection()
    {
        StringBuffer str_result = new StringBuffer();

        str_result.append("{\\info");

        str_result.append("{\\title ");
        str_result.append(DOCUMENTTITLE);
        str_result.append("}");
        str_result.append("{\\author ");
        str_result.append(PRODUCTNAME);
        str_result.append("}");
        Calendar dt_creationDate = GregorianCalendar.getInstance();
        str_result.append("{\\creatim ");
        str_result.append("\\yr" +  dt_creationDate.get(Calendar.YEAR));
        str_result.append("\\mo" +  dt_creationDate.get(Calendar.MONTH));
        str_result.append("\\dy" +  dt_creationDate.get(Calendar.DAY_OF_MONTH));
        str_result.append("\\hr" +  dt_creationDate.get(Calendar.HOUR_OF_DAY));
        str_result.append("\\min" + dt_creationDate.get(Calendar.MINUTE));
        str_result.append("}");
        str_result.append("}");                   // close {\info

        return str_result.toString();
    }

    /**
     * Creates the complete docment variables section.
     */
    public String makeRTF_documentVariables()
        throws AmbassadorDwUpException
    {
        StringBuffer sb = new StringBuffer();
        long highestTuId = Long.MIN_VALUE; // start at
        long lowestTuId = Long.MAX_VALUE; // start at
        long thisTuId = 0;
        boolean isFirstSeg = true;
        
        // standard variables
        sb.append(makeRTF_docVar(DOCVAR_NAME_DOCVERSION, WC_VERSION));        
        sb.append(makeRTF_docVar(DOCVAR_TRG_LCID, Integer.toString(m_targetLcid)));
        sb.append(makeRTF_docVar(DOCVAR_SRC_LCID, Integer.toString(m_sourceLcid)));
                        
        // Create the TuId boundary variables.
        // These values are used to help optimize some VBA code where we get the list of TuIds
        // TuIds are not guarenteed to be consecutive.
        sb.append(makeRTF_docVar(DOCVAR_NAME_HIGHEST_TUID, Long.toString(highestTuId)));                        
        sb.append(makeRTF_docVar(DOCVAR_NAME_LOWEST_TUID, Long.toString(lowestTuId)));
        return sb.toString();       
    }
        
    public String makeRTF_templateAttachment()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("{\\*\\template ");        
        sb.append(" " /*MSWORD_TRANS_TEMPLATE*/);       
        sb.append("}");       
        return sb.toString();
    }

    public String makeRTF_documentDefaults()
    {        
        // get basic document defaults
        String str_commonDefaults = "";
        try
        {
            str_commonDefaults = m_resource.getString("DocumentDefaults");
        }
        catch (MissingResourceException ex)
        {
            c_logger.warn("Could not load RTF document default view options", ex);
        }

        StringBuffer sb = new StringBuffer();

        sb.append("\\margl720\\margr720");        
        sb.append(str_commonDefaults);       

        return sb.toString();
    }

    
    /**
     * Writes the document header - encoding, formats, languages etc.
     */
    public void writeRTF_docHeader()
        throws IOException, AmbassadorDwUpException
    {        
        if (!_ENABLE_DEBUG_OF_DOWNLOAD_ABORT)
        {
            StringBuffer fldInst = new StringBuffer();

            m_outputStream.write("\\par { "); 
            m_outputStream.write(m_strLabelAndHeaderTextStyle);         
            m_outputStream.write("\\b========================\\par");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("DO NOT ALTER THIS FILE\\par");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("========================}\\par\\par");
            m_outputStream.write(m_strEOL);
        }
    }

    //////////////////////////////////////
    // Private Methods
    //////////////////////////////////////
    
    private void writeSourceSection()
         throws IOException
    {
        ArrayList subflows = new ArrayList();
        OfflineSegmentData OSD = null;
        long paraCnt = 0;
        StringBuffer paraBkmName = new StringBuffer(BM_PREFIX_SRCPARA);

        m_outputStream.write("\\par {"); 
        m_outputStream.write(m_strLabelAndHeaderTextStyle);         
        m_outputStream.write("\\b=================================================================\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("Source segments \\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("=================================================================");
        m_outputStream.write("\\par} "); 
        m_outputStream.write(m_strEOL);                
        
        // write the text staging area - area used to clean up bookmarks before returning the text.
        m_outputStream.write("{{\\*\\bkmkstart stageOuter} {\\*\\bkmkstart stageInner}\\hich\\af0\\dbch\\af13\\loch\\f0 TextStagingAreaBookmark{\\*\\bkmkend stageInner} {\\*\\bkmkend stageOuter}\\par\\par\\par }"); 
        m_outputStream.write(m_strEOL);                        

        // first paragraph name
        paraBkmName.append(Long.toString(paraCnt));
        
        Iterator it = m_page.getAllUnmergedSegmentIdIterator();        
        while (it.hasNext())
        {   
            if (!isMSWord2000BmLimitExceeded())
            {
                OSD = m_page.getResourceByDisplayId( (String)it.next() );
                if( OSD.isSubflowSegment() )
                {
                    if (subflows == null)
                    {
                        subflows = new ArrayList();
                    }
                    subflows.add(OSD);            
                }
                else
                {
                    if( OSD.isStartOfNewPara() )
                    {
                        // close previous para bookmark
                        if( paraCnt > 0 )
                        {
                            m_outputStream.write(
                                makeRTF_bookmark(BM_CLOSE, paraBkmName.toString()));                        
                            m_outputStream.write("\\par\\par"); 
                            m_outputStream.write(m_strEOL);                        
                        }

                        // write subs for previous paragraph
                        if(subflows.size() > 0)
                        {
                            writeRTF_docSubflowsSource(subflows);
                        }

                        // open new new para bookmark
                        paraBkmName.delete(BM_PREFIX_SRCPARA.length(), paraBkmName.length());
                        paraBkmName.append(Long.toString(++paraCnt));                    
                        m_outputStream.write(
                            makeRTF_bookmark(BM_OPEN, paraBkmName.toString()));
                    }

                    write_docSourceSegment(OSD);        
                }
            }
        }
        
        // close last para bookmark
        m_outputStream.write(
            makeRTF_bookmark(BM_CLOSE, paraBkmName.toString()));
        m_outputStream.write("\\par\\par");         
       
        // write subs for last paragraph
        if(subflows.size() > 0)
        {
            writeRTF_docSubflowsSource(subflows);
        }

    }
    
    private void write_docSourceSegment(OfflineSegmentData p_OSD)
        throws IOException
    {
        boolean protect = false;
        String srcText = p_OSD.getDisplaySourceTextWithNewLinebreaks(
            "" + NORMALIZED_LINEBREAK);
              
        writeRTF_docSegment(makeBookmarkName(p_OSD, BM_SUFFIX_SOURCE),
            srcText, /*p_OSD.getMatchTypeId()==MATCH_TYPE_FUZZY ? 
                m_strSourceTextFuzzyStyle :*/ m_strSourceTextStyle, 
                    m_strDefaultPtagStyle, m_sourceIsRtlLang, true, p_OSD.getDisplaySegmentFormat(), true, null, null);
    }

    private void writeRTF_docSubflowsSource(Collection p_subflows)
        throws IOException
    {        
        Iterator it = null;
        OfflineSegmentData OSD = null;
        
        if (p_subflows != null)
        {       
            it = p_subflows.iterator();

            while(it.hasNext())
            {
                OSD = (OfflineSegmentData)it.next();
                write_docSourceSegment(OSD);
                m_outputStream.write("\\par");                                
                m_outputStream.write(m_strEOL);                                        
            }
            m_outputStream.write("\\par ");
            p_subflows.clear();            
        }
    }
 
}
