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
import com.globalsight.everest.edit.offline.rtf.RTFEditor;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.Text;
import com.globalsight.util.StringUtil;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.StringBuffer;
import java.text.DecimalFormat;
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
public class ParaViewTmDocWriter extends RTFWriterUnicode
{
    //////////////////////////////////////
    // Public Constants                 //
    //////////////////////////////////////
    
    private static final boolean _ENABLE_DEBUG_OF_DOWNLOAD_ABORT = false;
    
    /** A unique string that identifies this file format.
        This string is stored in the RTF info section under "title". 
        This string is used regular expressions to recognize our files programatically. 
        See UploadPageHandler where document recognition is performed.**/    
    public static final String DOCUMENTTITLE = "GlobalSight Extracted Paragraph view - TM Resources";
     

    // RTF DOCUMENT VARIABLE NAMES
    /** suffix used to create the fuzzy count document variable */
    public static final String DOCVAR_SUFFIX_FCNT = "_fcnt";
    /** name of the variable that tracks that last accessed resource for an opened segment*/
    public static final String DOCVAR_NAME_RES_IDX = "eResIdx";
    /** name of the variable that holds the highest TuId value */
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
    /** Bookmark suffix for the current target text */
    public static final String BM_SUFFIX_CUR_DOCTEXT = "_c";
    /** Bookmark suffix for fuzzy match text */
    public static final String BM_SUFFIX_FUZZY = "_f";
    /** Bookmark suffix for the info text */
    public static final String BM_SUFFIX_INFO = "_i";

    //////////////////////////////////////
    // Private & Protected Constants
    //////////////////////////////////////
    private static final Logger c_logger =
        Logger.getLogger(
            ParaViewTmDocWriter.class);


    
    
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
    public ParaViewTmDocWriter()
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
            writeEditorResourceSection();        
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
        sb.append(makeRTF_docVar(DOCVAR_NAME_RES_IDX, "0"));  
        sb.append(makeRTF_docVar(DOCVAR_TRG_LCID, Integer.toString(m_targetLcid)));
        sb.append(makeRTF_docVar(DOCVAR_SRC_LCID, Integer.toString(m_sourceLcid)));
                       
        // create the fuzzy count document variable for each segment 
        List fuz = null;
        Iterator it = m_page.getSegmentIterator();
        while(it.hasNext())
        {
            OfflineSegmentData OSD = (OfflineSegmentData)it.next();
            thisTuId = Long.parseLong(OSD.getRootSegmentId());

            if(isFirstSeg)
            {
                highestTuId = thisTuId;
                lowestTuId = thisTuId;                
                isFirstSeg = false;
            }
            if( thisTuId > highestTuId )
            {
                highestTuId = thisTuId;
            }
            else if ( thisTuId < lowestTuId )
            {
                lowestTuId = thisTuId;
            }

            fuz = OSD.getDisplayFuzzyMatchList();
            if(fuz != null)
            {
                sb.append(
                    makeRTF_docVar(
                        OSD.getDisplaySegmentID() + DOCVAR_SUFFIX_FCNT, 
                          Integer.toString(fuz.size())));                        
            }
        }
        
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
            m_outputStream.write("========================\\par\\par");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("=================================================================\\par");
            m_outputStream.write(m_strEOL);        
            m_outputStream.write("Reusable editor formatting\\par");
            m_outputStream.write(m_strEOL);        
            m_outputStream.write("=================================================================\\b0");
            m_outputStream.write("}\\par "); 
            m_outputStream.write(m_strEOL);        

            // Editor formatting 
            RTFEditor rtf = new RTFEditor(m_targetIsRtlLang);
            m_outputStream.write(rtf.getCompleteEditorWithBookmarks());
            m_outputStream.write(m_strEOL);
            
            writeRTF_curDocTextDefault(); 
        }
    }

    //////////////////////////////////////
    // Private Methods
    //////////////////////////////////////
    private void writeEditorResourceSection()
         throws IOException
    {
        OfflineSegmentData offlineTu = null;
        
        m_outputStream.write("\\par {"); 
        m_outputStream.write(m_strLabelAndHeaderTextStyle);         
        m_outputStream.write("\\b=================================================================\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("Editor Resources\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("=================================================================");
        m_outputStream.write("}\\par "); 
        m_outputStream.write(m_strEOL);                

        // write the text staging area - area used to clean up bookmarks before returning the text.
        m_outputStream.write("{{\\*\\bkmkstart stageOuter} {\\*\\bkmkstart stageInner}\\hich\\af0\\dbch\\af13\\loch\\f0 TextStagingAreaBookmark{\\*\\bkmkend stageInner} {\\*\\bkmkend stageOuter}\\par\\par\\par }"); 
        m_outputStream.write(m_strEOL); 
        
        Iterator segIt = m_page.getAllUnmergedSegmentIdIterator();
        
        while (segIt.hasNext())
        {        
            
            offlineTu = m_page.getResourceByDisplayId( (String)segIt.next() );

            m_outputStream.write("\\par {");                                
            m_outputStream.write(m_strLabelAndHeaderTextStyle);                                                
            m_outputStream.write(makeBookmarkName(offlineTu, null)); // use bookmark name as a visual reference
            m_outputStream.write("}\\par ");                

            // Tm matches
            if( offlineTu.hasTMMatches())
            {
                if (isMSWord2000BmLimitExceeded())
                {
                    m_outputStream.write("{Has fuzzy match on server.}\\par ");                                   
                }
                else
                {
                    writeRTF_tmResources(offlineTu, m_page);
                }
            }
            
            // The segments own storage for the cur doc text
            // Note: client will detect and switch to shared storage location if unique storage exceeded
            if (!isMSWord2000BmLimitExceeded())
            { 
                writeRTF_curDocTextResource(offlineTu, m_page);                        
            }   
        }
    }
    
    

    private void write_docTargetSegment(OfflineSegmentData p_OSD)
        throws IOException
    {
        boolean protect = false;
        String trgText = p_OSD.getDisplayTargetTextWithNewLineBreaks(
            "" + NORMALIZED_LINEBREAK);
              
        if (p_OSD.isCopyOfSource())
        {
            // This is actually a copy of the source segment
            writeRTF_docSegment(makeBookmarkName(p_OSD, BM_SUFFIX_SOURCE),
                trgText, p_OSD.getMatchTypeId()==MATCH_TYPE_FUZZY ? 
                    m_strSourceTextFuzzyStyle : m_strSourceTextStyle, 
                        m_strDefaultPtagStyle, m_sourceIsRtlLang, true, p_OSD.getDisplaySegmentFormat(), true, null, null);
        }
        else
        {
            // Special treatment for exact matches (if requested )
            if (p_OSD.isWriteAsProtectedSegment())
            {
                writeRTF_docSegment(makeBookmarkName(p_OSD, BM_SUFFIX_SOURCE),
                    trgText, m_strExternalStyleWithTargetFont,
                        m_strDefaultPtagStyle, m_targetIsRtlLang, true, p_OSD.getDisplaySegmentFormat(), true, null, null);
            }
            else
            {
                String textStyle = null;
                switch (p_OSD.getMatchTypeId())
                {
                    case MATCH_TYPE_EXACT:
                        textStyle = m_strTargetTextExactStyle;
                        break;
                    case MATCH_TYPE_FUZZY:
                        textStyle = m_strTargetTextFuzzyStyle;
                        break;
                    default:
                        textStyle = m_strTargetTextStyle;
                        break;
                }                        

                writeRTF_docSegment(makeBookmarkName(p_OSD, BM_SUFFIX_SOURCE),
                    trgText, textStyle, m_strDefaultPtagStyle,
                        m_targetIsRtlLang, true, p_OSD.getDisplaySegmentFormat(),
                            true, null, null);
            }
        }
    }

    private void writeRTF_docSubflowsTarget(Collection p_subflows)
        throws IOException
    {        
        Iterator it = null;
        OfflineSegmentData OSD = null;
        
        if (p_subflows != null)
        {       
            it = p_subflows.iterator();
            if (it.hasNext() )
            {
                m_outputStream.write("\\par");                
                m_outputStream.write(m_strEOL);                                                    
            }

            while(it.hasNext())
            {
                OSD = (OfflineSegmentData)it.next();
                m_outputStream.write("\\par");                
                write_docTargetSegment(OSD);
                m_outputStream.write(m_strEOL);                                        
            }
        }
    }
    
    private void writeRTF_tmResources(OfflineSegmentData p_osd, OfflinePageData p_page)
        throws IOException
    {
        //Collection fuzForDisplay = p_osd.getDisplayFuzzyMatchList();
        Iterator fuzDisplayIt = p_osd.getDisplayFuzzyMatchList().iterator();                
        //List origLmList = p_osd.getOriginalFuzzyLeverageMatchList();               
        Iterator origLmIt = p_osd.getOriginalFuzzyLeverageMatchList().iterator();                
        LeverageMatch origLm = null;
        int totalNumFuz = p_osd.getDisplayFuzzyMatchList().size();
        int cnt = 0;
        StringBuffer infoBkmName = new StringBuffer();                    
        StringBuffer infoText = new StringBuffer();                            
        String fuzTextBkmName = "";        
        
        while(fuzDisplayIt.hasNext())
        {        
            cnt++;                

            // create bookmark names
            infoBkmName.append(makeBookmarkName(p_osd, BM_SUFFIX_FUZZY));
            infoBkmName.append(cnt);
            fuzTextBkmName = infoBkmName.toString();
            infoBkmName.append(BM_SUFFIX_INFO);

            // create info text       
            infoText.append("Fuzzy match ");
            infoText.append(Integer.toString(cnt));
            infoText.append(" of ");                    
            infoText.append(Integer.toString(totalNumFuz));                    
            infoText.append("      ");
            infoText.append("Score ");
            origLm = (LeverageMatch)origLmIt.next();
            infoText.append(origLm != null ? StringUtil.formatPercent(origLm.getScoreNum(), 2) : "");                    
            if(p_osd.hasTerminology())
            {
                infoText.append("       \\ul");
                infoText.append(makeRTF_field(false,false,false,true," MACROBUTTON getTerms Click here for Terms", null));                        
            }
            
            // show segment format if different from the original main doc format
            if( p_osd.getDisplaySegmentFormat().toLowerCase().equals("html") && 
                !p_osd.getSegmentType().equals("text"))
            {
                infoText.append(",    Item type="); 
                infoText.append(p_osd.getSegmentType()); // is item type
            }
            else if( !p_osd.getDisplaySegmentFormat().toLowerCase().equals(
                        m_page.getDocumentFormat()))
            {
                infoText.append(",    Embedded format="); 
                infoText.append(p_osd.getSegmentType()); // is format type
            }
            
            // write info entry
            // Example: Fuzzy match 1 of 3      Score 90%      Click here for Terms
            m_outputStream.write("{");                                        
            m_outputStream.write(makeRTF_bookmark(BM_OPEN, infoBkmName.toString()));
            m_outputStream.write(m_strEditorInfoTextStyle);                    
            m_outputStream.write(infoText.toString());                    
            m_outputStream.write(makeRTF_bookmark(BM_CLOSE, infoBkmName.toString()));                    
            m_outputStream.write("} \\par " ); 

            // write fuzzy match
            writeRTF_docSegment(fuzTextBkmName,(String)fuzDisplayIt.next(),
                m_strTargetTextStyle, m_strDefaultPtagStyle, m_targetIsRtlLang,
                    true, p_osd.getDisplaySegmentFormat(), true, null, null);
            m_outputStream.write("\\par " );                    
            m_outputStream.write(m_strEOL);                                    

            infoBkmName.delete(0, infoBkmName.length());
            infoText.delete(0, infoText.length());
            fuzTextBkmName = "";                            
        }

    }
    
    private void writeRTF_curDocTextResource(OfflineSegmentData p_osd, OfflinePageData m_page)
        throws IOException    
    {
        StringBuffer infoBkmName = new StringBuffer();                    
        StringBuffer infoText = new StringBuffer();                            
        String curTextBkmName = "";                
        String fuzCnt = "0";
        
        // create current document text info line
        infoBkmName.append(makeBookmarkName(p_osd, BM_SUFFIX_CUR_DOCTEXT));            
        curTextBkmName = infoBkmName.toString();            
        infoBkmName.append(BM_SUFFIX_INFO);

        // write current document text info line
        // Example: Current document text  Fuzzy matches (ALT+ N) = 3,  Click here for Terms 
        m_outputStream.write("{ ");                                        
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, infoBkmName.toString()));
        m_outputStream.write(m_strEditorInfoTextStyle);             
        infoText.append(p_osd.isWriteAsProtectedSegment() ? 
            " Locked Segment      " : " Current document text      ");        

        switch(p_osd.getMatchTypeId())
        {
            case MATCH_TYPE_EXACT:
                infoText.append("Exact Match");
                if ( p_osd.hasTMMatches() ) 
                {
                    fuzCnt = Integer.toString(p_osd.getOriginalFuzzyLeverageMatchList().size());
                }                
                infoText.append(" : Alternates (ALT+N)=");
                infoText.append(fuzCnt);
                break;
            case MATCH_TYPE_FUZZY:
                if ( p_osd.hasTMMatches() ) 
                {
                    fuzCnt = Integer.toString(p_osd.getOriginalFuzzyLeverageMatchList().size());
                }
                infoText.append("Fuzzy match (ALT+N)=");
                infoText.append(fuzCnt);
                break;
            default: // fall through                                            
            case MATCH_TYPE_NOMATCH:
                infoText.append("No Match");                
                break;
        }
        
        // show segment format if different from the original main doc format
        if( p_osd.getDisplaySegmentFormat().toLowerCase().equals("html") && 
            !p_osd.getSegmentType().equals("text"))
        {
            infoText.append(",    Item type=");             
            infoText.append(p_osd.getSegmentType()); // show item type instead
        }
        else if( !p_osd.getDisplaySegmentFormat().toLowerCase().equals(
                    m_page.getDocumentFormat()))
        {
            infoText.append(",    Item type=");             
            infoText.append(p_osd.getSegmentType());            
        }

        if(p_osd.hasTerminology())
        {
            infoText.append(",      \\ul");
            infoText.append(makeRTF_field(false,false,false, true," MACROBUTTON getTerms Click here for Terms", null));                        
        }            
        m_outputStream.write(infoText.toString());            
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, infoBkmName.toString()));            
        m_outputStream.write("}\\par " );            

        // write cur doc text
        String trg = p_osd.isWriteAsProtectedSegment() ? 
                        p_osd.getDisplayTargetTextWithNewLineBreaks(
                            "" + NORMALIZED_LINEBREAK) : WC_CUR_DOC_TEXT_DEFAULT;
        writeRTF_docSegment(curTextBkmName, trg, 
                m_strSourceTextStyle, m_strDefaultPtagStyle, m_sourceIsRtlLang,
                    true, p_osd.getDisplaySegmentFormat(), true, null, null);            
        m_outputStream.write("\\par " );                        
        m_outputStream.write(m_strEOL);                            

        infoBkmName.delete(0, infoBkmName.length());            
        infoText.delete(0, infoText.length());
        curTextBkmName = "";                    
    }        
  
    
     private void writeRTF_curDocTextDefault()
        throws IOException    
    {
        StringBuffer infoBkmName1 = new StringBuffer(); 
        StringBuffer infoBkmName2 = new StringBuffer();  
        StringBuffer infoBkmName3 = new StringBuffer();      
        StringBuffer infoText1 = new StringBuffer();     
        StringBuffer infoText2 = new StringBuffer();
        StringBuffer infoText3 = new StringBuffer();
        String curTextBkmName = "";                
        String fuzCnt = "0";
        
        // create bookmark names
        infoBkmName1.append("default");  // text must be synced with client(see editorLoad:noData)
        infoBkmName1.append(BM_SUFFIX_CUR_DOCTEXT);            
        curTextBkmName = infoBkmName1.toString();            
        infoBkmName1.append(BM_SUFFIX_INFO);
        
        infoBkmName2.append("default_locked"); // text must be synced with client (see editorLoad:noData)
        infoBkmName2.append(BM_SUFFIX_CUR_DOCTEXT);
        infoBkmName2.append(BM_SUFFIX_INFO);

        infoBkmName3.append("default_fuzzy"); // text must be synced with client(see editorLoad:noData)
        infoBkmName3.append(BM_SUFFIX_CUR_DOCTEXT);
        infoBkmName3.append(BM_SUFFIX_INFO);

        // Write the deafult current document text info line
        // This default line will only be used if the TM file overflows. 
        // Otherwise, the segments own info line will be used.
        m_outputStream.write("\\para{ ");                                        
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, infoBkmName1.toString()));
        m_outputStream.write(m_strEditorInfoTextStyle);             
        infoText1.append(" Current document text      ");    
        m_outputStream.write(infoText1.toString());            
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, infoBkmName1.toString()));            
        m_outputStream.write("}\\par " ); 
        m_outputStream.write(m_strEOL);
        
        m_outputStream.write("\\para{ ");                                        
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, infoBkmName2.toString()));
        m_outputStream.write(m_strEditorInfoTextStyle);             
        infoText2.append(" Locked      ");    
        m_outputStream.write(infoText2.toString());            
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, infoBkmName2.toString()));            
        m_outputStream.write("}\\par " ); 
        m_outputStream.write(m_strEOL);
        
        m_outputStream.write("\\para{ ");                                        
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, infoBkmName3.toString()));
        m_outputStream.write(m_strEditorInfoTextStyle);             
        infoText3.append(" Current document text      Fuzzy match (available online)");    
        m_outputStream.write(infoText3.toString());            
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, infoBkmName3.toString()));            
        m_outputStream.write("}\\par " ); 
        m_outputStream.write(m_strEOL);        
        
        // write cur doc text default 
        String trg = WC_CUR_DOC_TEXT_DEFAULT;
        writeRTF_docSegment(curTextBkmName, trg, 
                m_strSourceTextStyle, m_strDefaultPtagStyle, m_sourceIsRtlLang,
                    true, "--", true, null, null );            
        m_outputStream.write("\\par " );                        
        m_outputStream.write(m_strEOL);                            
                   
    }        
}
