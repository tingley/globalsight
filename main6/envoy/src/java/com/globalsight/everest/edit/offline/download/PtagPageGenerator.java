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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.edit.EditUtil;

/**
 * Responsible for converting the GXML source text to create PTag Jobs.
 */
public class PtagPageGenerator
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            PtagPageGenerator.class);

    /**
     * Constructor
     */
    public PtagPageGenerator()
    {
        super();
    }

    /**
     * Converts the source, target and all fuzzy matches from GXML to PTag.
     */
    public void convertAllGxmlToPtag(OfflinePageData p_PageData, DownloadParams params)
        throws AmbassadorDwUpException
    {
        // Update tag display ID
        if (params.getTagDisplayFormatID() == PseudoConstants.PSEUDO_VERBOSE)
        {
            p_PageData.setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_PTAGV);
        }
        else
        {
            p_PageData.setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_PTAGC);
        }

        // convert target segments
        Iterator it = p_PageData.getAllUnmergedSegmentIdIterator();
        while (it.hasNext())
        {
            String id = (String)it.next();
            // try to convert through the target segment list first
            // this covers all merged and some unmerged segments.
            OfflineSegmentData data =
                (OfflineSegmentData)p_PageData.getSegmentByDisplayId(id);

            if (data != null)
            {
                convertAll(p_PageData, data,  params);
            }

            // if not found, it means it is merged and therefore
            // should be listed as a resource
            if (data == null || data.isMerged())
            {
                data = (OfflineSegmentData)p_PageData.getResourceByDisplayId(id);
                if (data != null)
                {
                    convertAll(p_PageData, data, params);
                }
            }
        }
    }


    /**
     * Converts the target segment from GXML to PTAg.
     */
    public void convertTargetGxmlToPtag(OfflinePageData p_pageData, int p_PTagDisplayMode)
        throws AmbassadorDwUpException
    {
        // Create PTag resources
        TmxPseudo convertor = new TmxPseudo();
        PseudoData PTagData = new PseudoData();
        PTagData.setMode(p_PTagDisplayMode);

        // Update tag display ID
        if (p_PTagDisplayMode == PseudoConstants.PSEUDO_VERBOSE)
        {
            p_pageData.setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_PTAGV);
        }
        else
        {
            p_pageData.setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_PTAGC);
        }

        // convert page seg-by-seg
        for (ListIterator it = p_pageData.getSegmentIterator(); it.hasNext(); )
        {
            OfflineSegmentData segData = (OfflineSegmentData)it.next();

            try
            {
                // Convert the current gxml target to ptag
                PTagData.setAddables(segData.getDisplaySegmentFormat());
                convertor.tmx2Pseudo(segData.getDisplayTargetText(), PTagData);
                segData.setDisplayTargetText(PTagData.getPTagSourceString());
            }
            catch (DiplomatBasicParserException ex)
            {
                CATEGORY.error(ex.getMessage(), ex);
                throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_GXML, ex);
            }
        }
    }

    private void convertAll(OfflinePageData p_PageData, OfflineSegmentData p_OSD, DownloadParams params)
        throws AmbassadorDwUpException
    {
        PseudoData PTagData = null;
        TmxPseudo convertor = null;

        // Create PTag resources
        PTagData = new PseudoData();
        PTagData.setMode(params.getTagDisplayFormatID());
        convertor = new TmxPseudo();

        try
        {
            // configure addable ptags for this format
            PTagData.setAddables( p_OSD.getDisplaySegmentFormat() );

            // Preserve the source GXML - reused in fuzzy conversions below.
//            String srcGxml = p_OSD.getDisplaySourceText();

            // convert the current source text and
            // set the native map to represent source tags
            convertor.tmx2Pseudo(p_OSD.getDisplaySourceText(), PTagData);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("\n\nSourceConversion:\nGXML=" +
                    p_OSD.getDisplaySourceText() + "\nDisplayText=" +
                    EditUtil.decodeXmlEntities(PTagData.getPTagSourceString()));
            }

            if (params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF 
                    || params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF20 
                    || params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT ) {
				p_OSD.setDisplaySourceText(TmxUtil.convertXlfToTmxFormat(p_OSD.getDisplaySourceText()));
			} else if (params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TTX) {
				//for ttx format,need extra handling
				String pTagSourceString = EditUtil.encodeXmlEntities(PTagData.getPTagSourceString());
				p_OSD.setDisplaySourceText(pTagSourceString);
			} else {
				// GBS-539, avoid decoding for the second time here
				//p_OSD.setDisplaySourceText(EditUtil.decodeXmlEntities(p_OSD.getDisplaySourceText()));
				p_OSD.setDisplaySourceText(PTagData.getPTagSourceString());
			}

            // convert the current target text
            // NOTE: The use of getPTagSourceString() below is correct!!
            convertor.tmx2Pseudo(p_OSD.getDisplayTargetText(), PTagData); 

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("\nTargetConversion:\nTargetGXML=" +
                    p_OSD.getDisplayTargetText() + "\nDisplayText=" +
                    EditUtil.decodeXmlEntities(PTagData.getPTagSourceString()));
            }

            if (params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF 
                    || params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF20 
                    || params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT ) {
				p_OSD.setDisplayTargetText(TmxUtil.convertXlfToTmxFormat(p_OSD.getDisplayTargetText()));				
			} else if (params.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TTX) {
				//for ttx format,need extra handling (below codes seem wrong,but they are right!!!)
        		String pTagTargetString = EditUtil.encodeXmlEntities(PTagData.getPTagSourceString());
				p_OSD.setDisplayTargetText(pTagTargetString);
			} else {
				// GBS-539, avoid decoding for the second time here
				//p_OSD.setDisplayTargetText(EditUtil.decodeXmlEntities(p_OSD.getDisplayTargetText()));
				p_OSD.setDisplayTargetText(PTagData.getPTagSourceString());
			}

            // Convert all GXML fuzzy matches to ptag
            List orgList = p_OSD.getOriginalFuzzyLeverageMatchList();
            if (orgList != null)
            {
                ListIterator orgListIterator = orgList.listIterator();
                ArrayList ptagFuzzyMatchList = new ArrayList();

                while (orgListIterator.hasNext())
                {
                    LeverageMatch lm = (LeverageMatch) orgListIterator.next();

                    try
                    {
                        convertor.tmx2Pseudo(lm.getLeveragedTargetString(), PTagData);
                        ptagFuzzyMatchList.add(PTagData.getPTagSourceString());
                    }
                    catch (Exception ex)
                    {
                        CATEGORY.warn("unable to get/convert Fuzzy match : "
                                + getMsg(p_OSD), ex);
                        ptagFuzzyMatchList.add("warn: unable to get/convert fuzzy match. " +
                            "(See GlobalSight.log.)");
                    }
                }

                p_OSD.setDisplayFuzzyList(ptagFuzzyMatchList);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(getMsg(p_OSD), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_GXML, ex
                            + "\n\nSEGMENT ID for above exception: "
                            + getMsg(p_OSD));
        }
    }

    private String getMsg(OfflineSegmentData p_OSD)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SegmentID is ")
                    .append(p_OSD.getDisplaySegmentID())
                    .append("; source TUV ID is ")
                    .append(p_OSD.getSourceTuv().getId())
                    .append("; target TUV ID is ")
                    .append(p_OSD.getTrgTuvId());
            return sb.toString();
        }
        catch (Exception e)
        {
        }

        return "";
    }
}
