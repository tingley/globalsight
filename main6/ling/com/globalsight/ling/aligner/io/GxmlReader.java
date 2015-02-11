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
package com.globalsight.ling.aligner.io;

import com.globalsight.ling.aligner.gxml.AlignmentPage;
import com.globalsight.ling.aligner.gxml.Skeleton;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.corpusinterface.TuvMapping;

import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlRootElement;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.tuv.TuType;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * GxmlReader reads GXML string and create AlignmentPage object.
 */

public class GxmlReader
{
    private String m_dataType;
    private GxmlRootElement m_gxmlRoot;
    
    public AlignmentPage createAlignmentPage(
        String p_gxml, GlobalSightLocale p_locale)
        throws Exception
    {
        m_gxmlRoot = parseGxml(p_gxml);
        m_dataType = getDataType(m_gxmlRoot);
        
        return createAlignmentPage(m_gxmlRoot, p_locale);
    }


    public String processGxmlForCorpus(String p_gxml, List p_corpusMappings)
        throws Exception
    {
        m_gxmlRoot = parseGxml(p_gxml);
        processGxml(p_corpusMappings);
        return m_gxmlRoot.toGxml();
    }
    

    public String getGxml()
    {
        return m_gxmlRoot.toGxml();
    }
    

    /**
     * This method parses the GXML and creates a DOM tree
     * @return GxmlRootElement - the root of the DOM tree.
     * @throws Exception
     */
    private GxmlRootElement parseGxml(String p_gxml)
        throws Exception
    {
        GxmlRootElement gxmlRootElement= null;
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            gxmlRootElement = reader.parse(p_gxml);
        }
        finally
        {
            if (reader != null)
            {
                GxmlFragmentReaderPool.instance()
                    .freeGxmlFragmentReader(reader);
            }
        }

        return gxmlRootElement;
    }



    private AlignmentPage createAlignmentPage(
        GxmlRootElement p_gxmlRoot, GlobalSightLocale p_locale)
    {
        AlignmentPage alignmentPage = new AlignmentPage(p_locale);
        // add an empty skeleton as the first skeleton in case there
        // is no skeleton before the first translatable (plain text
        // can be such a case).
        Skeleton currentSkeleton = new Skeleton(1, "");
        alignmentPage.addSkeleton(currentSkeleton);
        
        int skeletonId = 2;
        int segmentId = 1;
        
        List elements = p_gxmlRoot.getChildElements();
        int size = elements.size();

        for (int i = 0; i < size; i++)
        {
            GxmlElement elem = (GxmlElement)elements.get(i);

            switch (elem.getType())
            {
            case GxmlElement.TRANSLATABLE:
                List segments
                    = createTranslatableSegments(elem, segmentId, p_locale);
                segmentId += segments.size();
                
                Iterator itSeg = segments.iterator();
                while(itSeg.hasNext())
                {
                    BaseTmTuv tuv = (BaseTmTuv)itSeg.next();
                    alignmentPage.addSegment(currentSkeleton.getId(), tuv);
                }
                break;

            case GxmlElement.SKELETON:
                currentSkeleton = new Skeleton(skeletonId++, elem.toGxml());
                alignmentPage.addSkeleton(currentSkeleton);
                break;

            default:
                break;
            }
        }

        return alignmentPage;
    }
    
        
    private void processGxml(List p_corpusMappings)
    {
        // gather <segment> elements
        List segList = new ArrayList();
        
        List elements = m_gxmlRoot.getChildElements();
        int size = elements.size();

        for (int i = 0; i < size; i++)
        {
            GxmlElement elem = (GxmlElement)elements.get(i);

            switch (elem.getType())
            {
            case GxmlElement.TRANSLATABLE:
                Iterator itSeg = elem.getChildElements().iterator();
                while(itSeg.hasNext())
                {
                    segList.add(itSeg.next());
                }
                break;

            default:
                break;
            }
        }


        // add "tuid" for corpus mapping
        Iterator itMapping = p_corpusMappings.iterator();
        while(itMapping.hasNext())
        {
            TuvMapping tuvMapping = (TuvMapping)itMapping.next();
            long orgTuId = tuvMapping.getTuvId(); // tuv id that is compared

            Iterator itSeg = segList.iterator();
            while(itSeg.hasNext())
            {
                GxmlElement seg = (GxmlElement)itSeg.next();
                String orgIdStr = seg.getAttribute("org-tuid");
                if(orgIdStr != null)
                {
                    long orgId = Long.parseLong(orgIdStr);
                    if(orgId == orgTuId)
                    {
                        seg.setAttribute("tuid",
                            Long.toString(tuvMapping.getProjectTmTuId()));
                    }
                }
            }
        }
        
    }
    
        
    private String getDataType(GxmlRootElement p_gxmlRoot)
    {
        String tuDataType
            = p_gxmlRoot.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
        if (tuDataType == null)
        {
            // tuDataType shouldn't be null, but in case, default to html.
            tuDataType = "html";
        }
        
        return tuDataType;
    }
    
        
    private List createTranslatableSegments(
        GxmlElement p_elem, int p_segmentId, GlobalSightLocale p_locale)
    {
        List segments = new ArrayList();
        String str_tuType = p_elem.getAttribute(GxmlNames.TRANSLATABLE_TYPE);

        // set optional Gxml attribute "type" if not set
        if (str_tuType == null || str_tuType.length() == 0)
        {
            str_tuType = TuType.TEXT.getName();
        }

        String tuDataType =
            p_elem.getAttribute(GxmlNames.TRANSLATABLE_DATATYPE);

        if (tuDataType == null || tuDataType.length() == 0)
        {
            tuDataType = m_dataType;
        }

        Iterator itSeg = p_elem.getChildElements().iterator();
        while(itSeg.hasNext())
        {
            GxmlElement seg = (GxmlElement)itSeg.next();
            // add "org-tuid" attribute for using to map "tuid" later
            // for corpus mapping
            seg.setAttribute("org-tuid", Integer.toString(p_segmentId));
            
            PageTmTu tu
                = new PageTmTu(p_segmentId, 0, tuDataType, str_tuType, true);
            PageTmTuv tuv
                = new PageTmTuv(p_segmentId++, seg.toGxml(), p_locale);
            tu.addTuv(tuv);
            
            segments.add(tuv);
        }

        return segments;
    }

}
