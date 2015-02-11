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
package com.globalsight.ling.aligner.engine;

import org.apache.log4j.Logger;

import com.globalsight.ling.aligner.AlignmentResult;
import com.globalsight.ling.aligner.gxml.AlignmentPage;
import com.globalsight.ling.aligner.gxml.Skeleton;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTuv;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 * GxmlAligner alignes source and target GXMLs and records the result
 * in AlignmentResult object.
 */

public class GxmlAligner
{
    private static final Logger c_logger =
        Logger.getLogger(
            GxmlAligner.class);

    private static final long MAX_CELL_SIZE = 8000000L;


    public static AlignmentResult align(
        AlignmentPage p_sourcePage, AlignmentPage p_targetPage,
        boolean p_performsBlockAlignment)
        throws Exception
    {
        AlignmentResult alignmentResult = null;

        if(p_performsBlockAlignment)
        {
            alignmentResult
                = alignWithSkeletonAlignment(p_sourcePage, p_targetPage);
        }
        else
        {
            alignmentResult
                = alignWithoutSkeletonAlignment(p_sourcePage, p_targetPage);
        }

        return alignmentResult;
    }


    private static AlignmentResult alignWithoutSkeletonAlignment(
        AlignmentPage p_sourcePage, AlignmentPage p_targetPage)
        throws Exception
    {
        AlignmentResult alignmentResult = new AlignmentResult();

        SegmentAlignmentFunction alignmentFunction
            = new SegmentAlignmentFunction(
                p_sourcePage.getLocale(), p_targetPage.getLocale());

        alignSegments(alignmentResult, p_sourcePage.getAllSegments(),
            p_targetPage.getAllSegments(), alignmentFunction);

        return alignmentResult;
    }


    private static AlignmentResult alignWithSkeletonAlignment(
        AlignmentPage p_sourcePage, AlignmentPage p_targetPage)
        throws Exception
    {
        AlignmentResult alignmentResult = new AlignmentResult();

        // align skeletons
        List sourceSkeletons = p_sourcePage.getAllSkeletons();
        List targetSkeletons = p_targetPage.getAllSkeletons();

        DpMatrix matrix = new DpMatrix(
            sourceSkeletons, targetSkeletons, new SkeletonAlignmentFunction());

        List result = matrix.align();
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("Skeleton Alignment: " + result);
        }

        // align segments
        SegmentAlignmentFunction alignmentFunction
            = new SegmentAlignmentFunction(
                p_sourcePage.getLocale(), p_targetPage.getLocale());

        List sourceSegments = new ArrayList();
        List targetSegments = new ArrayList();

        Iterator it = result.iterator();
        while(it.hasNext())
        {
            DpMatrixCell cell = (DpMatrixCell)it.next();
            if(cell.getState() == DpMatrixCell.DELETED)
            {
                Skeleton sourceSkeleton
                    = (Skeleton)matrix.getAlignmentElementX(cell.getXindex());

                sourceSegments.addAll(
                    p_sourcePage.getSegments(sourceSkeleton.getId()));
            }
            else if(cell.getState() == DpMatrixCell.INSERTED)
            {
                Skeleton targetSkeleton
                    = (Skeleton)matrix.getAlignmentElementY(cell.getYindex());

                targetSegments.addAll(
                    p_targetPage.getSegments(targetSkeleton.getId()));
            }
            else // MATCH
            {
                if(sourceSegments.size() != 0 || targetSegments.size() != 0)
                {
                    // align portion of segments
                    alignSegments(alignmentResult,
                        sourceSegments, targetSegments, alignmentFunction);
                }

                sourceSegments.clear();
                targetSegments.clear();

                Skeleton sourceSkeleton
                    = (Skeleton)matrix.getAlignmentElementX(cell.getXindex());

                sourceSegments.addAll(
                    p_sourcePage.getSegments(sourceSkeleton.getId()));

                Skeleton targetSkeleton
                    = (Skeleton)matrix.getAlignmentElementY(cell.getYindex());

                targetSegments.addAll(
                    p_targetPage.getSegments(targetSkeleton.getId()));
            }
        }

        // last segments to be aligned
        if(sourceSegments.size() != 0 || targetSegments.size() != 0)
        {
            // align portion of segments
            alignSegments(alignmentResult,
                sourceSegments, targetSegments, alignmentFunction);
        }

        return alignmentResult;
    }


    private static void alignSegments(AlignmentResult p_result,
        List p_soruceSegments, List p_targetSegments,
        SegmentAlignmentFunction p_alignmentFunction)
        throws Exception
    {
        // To prevent OutOfMemory exception, simply don't perform the
        // alignment for a block with a lot of segments. TEMPORARY FIX
        if(p_soruceSegments.size() * p_targetSegments.size() > MAX_CELL_SIZE)
        {
            return;
        }

        DpMatrix matrix = new DpMatrix(
            p_soruceSegments, p_targetSegments, p_alignmentFunction);

        List result = matrix.align();

//      if (c_logger.isDebugEnabled())
//      {
//         c_logger.debug("Segment Alignment Matrix: " + matrix);
//      }

        // record the result in AlignmentResult object
        Iterator it = result.iterator();
        while(it.hasNext())
        {
            DpMatrixCell cell = (DpMatrixCell)it.next();
            if(cell.getState() == DpMatrixCell.DELETED)
            {
                BaseTmTuv sourceTuv
                    = (BaseTmTuv)matrix.getAlignmentElementX(cell.getXindex());
                p_result.addSourceIsolatedSegment(sourceTuv);
            }
            else if(cell.getState() == DpMatrixCell.INSERTED)
            {
                BaseTmTuv targetTuv
                    = (BaseTmTuv)matrix.getAlignmentElementY(cell.getYindex());
                p_result.addTargetIsolatedSegment(targetTuv);
            }
            else if(cell.getState() == DpMatrixCell.MATCH)
            {
                BaseTmTuv sourceTuv
                    = (BaseTmTuv)matrix.getAlignmentElementX(cell.getXindex());

                BaseTmTuv targetTuv
                    = (BaseTmTuv)matrix.getAlignmentElementY(cell.getYindex());

                List sourceTuvs = new ArrayList();
                sourceTuvs.add(sourceTuv);

                List targetTuvs = new ArrayList();
                targetTuvs.add(targetTuv);

                p_result.addAlignedSegments(sourceTuvs, targetTuvs);
            }
            else if(cell.getState() == DpMatrixCell.MULTI_MATCH)
            {
                List sourceTuvs = matrix.getAlignmentElementsX(
                    cell.getMultiMatchXIndexBegin(),
                    cell.getMultiMatchXIndexEnd());

                List targetTuvs = matrix.getAlignmentElementsY(
                    cell.getMultiMatchYIndexBegin(),
                    cell.getMultiMatchYIndexEnd());

                p_result.addAlignedSegments(sourceTuvs, targetTuvs);
            }
        }
    }

}
