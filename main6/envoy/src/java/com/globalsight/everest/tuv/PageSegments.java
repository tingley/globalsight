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

package com.globalsight.everest.tuv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.util.GlobalSightLocale;

/**
 * Holds source and specified target segments in a page and interprets the
 * splt/merge relationship between segments.
 * 
 * TU and TUV are essential objects in GlobalSight and they are used almost
 * everywhere. Adding split & merge function should have an minimal impact on
 * existing codes. Only the merge aware modules are affected. Those modules are
 * online and offline editors and TM population. The other modules should work
 * fine without modification even segments are merged.
 * 
 * NOTE: Split/Merge aware modules shouldn’t access TU and TUV directly. Instead
 * they should access to the segments via this class.
 * 
 */
public class PageSegments implements Serializable
{
    private static final long serialVersionUID = 2725476495071683806L;

    private static Logger s_logger = Logger.getLogger(PageSegments.class);

    // source page
    SourcePage m_sourcePage;

    // target locales
    Collection m_targetLocales;

    // list of TUs
    List m_tus;

    // key: GlobalSightLocale
    // value: List of SegmentPair objects
    Map m_segmentPairList;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////

    /** Creates a new instance of PageSegments. Package scope. */
    PageSegments(List p_tus, SourcePage p_sourcePage, Collection p_targetLocales)
    {
        m_tus = p_tus;
        m_sourcePage = p_sourcePage;
        m_targetLocales = p_targetLocales;

        m_segmentPairList = new HashMap();
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Gets a SegmentPair iterator for the given target locale.
     * 
     * @param p_trgLocale
     *            the target locale
     */
    public Iterator getSegmentPairIterator(GlobalSightLocale p_trgLocale)
            throws PageSegmentsException
    {
        List rslt = (List) m_segmentPairList.get(p_trgLocale);
        if (rslt == null)
        {
            rslt = createSegmentPairList(p_trgLocale);
            m_segmentPairList.put(p_trgLocale, rslt);
        }
        return rslt.iterator();
    }

    /**
     * Gets an iterator that iterates unmerged SegmentPairs for the given target
     * locale.
     * 
     * @param p_trgLocale
     *            the target locale
     */
    public Iterator getUnmergedSegmentPairIterator(GlobalSightLocale p_trgLocale)
            throws PageSegmentsException
    {
        return new UnmergedSegmentPairIterator(p_trgLocale);
    }

    /**
     * Gets SegmentPair object by TU id. The segment maybe the results of a
     * merge. To determine if the pair is merged, call
     * SegmentPair.isMergedSegment().
     */
    public SegmentPair getSegmentPairByTuId(long p_tuId,
            GlobalSightLocale p_trgLocale) throws PageSegmentsException
    {
        // TODO: lookup method too slow...

        SegmentPair rslt = null;
        Iterator it = getSegmentPairIterator(p_trgLocale);
        while (it.hasNext())
        {
            SegmentPair pair = (SegmentPair) it.next();
            if (pair.getTuId() == p_tuId)
            {
                rslt = pair;
                break;
            }
        }
        return rslt;
    }

    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }

    public Collection getTargetLocales()
    {
        return m_targetLocales;
    }

    /**
     * Returns a List of modified target TUVs in this PageSegments object. If
     * the merge state or segment text were changed, the segments are deemed to
     * be modified.
     * 
     * @param p_targetLocales
     *            target locale
     * @return List of TUVs that were changed
     */
    public List<TuvImplVo> getModifiedTuvs(GlobalSightLocale p_targetLocale)
            throws PageSegmentsException
    {
        List<TuvImplVo> modifiedTuvList = new ArrayList<TuvImplVo>();

        for (Iterator it = getSegmentPairIterator(p_targetLocale); it.hasNext();)
        {
            SegmentPairImpl segmentPair = (SegmentPairImpl) it.next();
            if (segmentPair.isModified())
            {
                modifiedTuvList.addAll(segmentPair.getTargetTuvs());
            }
        }

        return modifiedTuvList;
    }

    /**
     * Merge/split segments in this PageSegments object according to a merge
     * directive. SegmentPairs that have changed the merge state by this
     * operation will be marked as modified.
     * 
     * @param p_mergeDirective
     *            Merge directive Map: Key : Tu id of the top TUV of the merged
     *            segments (Long) Value: List of Tu ids of merged segments
     *            including top segment
     * @param p_trgLocale
     *            target locale
     */
    public void mergeByMergeDirective(Map p_mergeDirective,
            GlobalSightLocale p_trgLocale) throws PageSegmentsException
    {
        // collect current merged segments
        ArrayList currentMergedSegments = new ArrayList();
        for (Iterator it = getSegmentPairIterator(p_trgLocale); it.hasNext();)
        {
            SegmentPair segmentPair = (SegmentPair) it.next();
            if (segmentPair.isMergedSegment())
            {
                currentMergedSegments.add(segmentPair);
            }
        }

        // unmerge merged segments if they are different from the
        // merge directive
        for (Iterator it = currentMergedSegments.iterator(); it.hasNext();)
        {
            SegmentPair segmentPair = (SegmentPair) it.next();
            Long tuId = new Long(segmentPair.getTuId());
            List directiveTuIds = (List) p_mergeDirective.get(tuId);
            if (directiveTuIds == null
                    || !directiveTuIds.equals(segmentPair.getMergedTuIds()))
            {
                // keep splitting the segment until each TUV forms
                // separate segment
                while (segmentPair.isMergedSegment())
                {
                    segmentPair.splitTopSegment();
                }
            }
            else
            {
                // remove a merge directive if the merged segment
                // already exists
                p_mergeDirective.remove(tuId);
            }
        }

        // merge segments according to the merge directive (existing
        // merge directives have been removed)
        for (Iterator it = p_mergeDirective.keySet().iterator(); it.hasNext();)
        {
            Long topTuId = (Long) it.next();
            SegmentPair topSegmentPair = getSegmentPairByTuId(
                    topTuId.longValue(), p_trgLocale);

            ArrayList segmentPairToBeMerged = new ArrayList();
            List tuIdList = (List) p_mergeDirective.get(topTuId);
            // iterating from index 1 because index 0 is topSegmentPair
            for (int i = 1; i < tuIdList.size(); i++)
            {
                long tuId = ((Long) tuIdList.get(i)).longValue();
                SegmentPair segmentPair = getSegmentPairByTuId(tuId,
                        p_trgLocale);
                segmentPairToBeMerged.add(segmentPair);
            }

            topSegmentPair.mergeSegments(segmentPairToBeMerged);
        }
    }

    // ////////////////////////////////////
    // Private Methods //
    // ////////////////////////////////////

    // Creates SegmentPairs by interpreting merge flags
    private List createSegmentPairList(GlobalSightLocale p_trgLocale)
            throws PageSegmentsException
    {
        long jobId = m_sourcePage.getJobId();
        List segmentPairList = new ArrayList();

        // set the initial capacity to 1 to save memories
        List srcTuvList = new ArrayList(1);
        List trgTuvList = new ArrayList(1);
        debugPrintSize(m_tus, "size of m_tus is:");
        Iterator it = m_tus.iterator();
        while (it.hasNext())
        {
            Tu tu = (Tu) it.next();
            srcTuvList.add(tu.getTuv(m_sourcePage.getGlobalSightLocale()
                    .getId(), jobId));
            Tuv trgTuv = tu.getTuv(p_trgLocale.getId(), jobId);
            trgTuvList.add(trgTuv);

            String mergeState = trgTuv.getMergeState();
            if (mergeState.equals(Tuv.NOT_MERGED)
                    || mergeState.equals(Tuv.MERGE_END))
            {
                SegmentPair segmentPair = new SegmentPairImpl(srcTuvList,
                        trgTuvList, p_trgLocale);
                segmentPairList.add(segmentPair);

                // set the initial capacity to 1 to save memories
                srcTuvList = new ArrayList(1);
                trgTuvList = new ArrayList(1);
            }
        }

        debugPrintSize(segmentPairList, "size of segment pair list is:");
        return segmentPairList;
    }

    // /Prints out the size of the collection if debugging is on with
    // the given message
    private void debugPrintSize(Collection p_c, String p_msg)
    {
        if (s_logger.isDebugEnabled())
        {
            int size = (p_c == null) ? 0 : p_c.size();
            s_logger.debug(p_msg + size);
        }
    }

    /*
     * represents a source and a target segment pair. Merged segments can be
     * accessed seamlessly
     */
    class SegmentPairImpl implements Serializable, SegmentPair
    {
        private GlobalSightLocale m_trgLocale;

        private List m_srcTuvs;
        private List m_trgTuvs;

        private Tuv m_srcTuvClone;
        private boolean m_isMerged;
        private boolean m_isModified;

        /**
         * Creates a new instance of SegmentPair. Private scope.
         * 
         * @param p_srcTuvs
         *            List of source TUVs
         * @param p_trgTuvs
         *            List of target TUVs
         * @param p_trgLocale
         *            target locale
         */
        private SegmentPairImpl(List p_srcTuvs, List p_trgTuvs,
                GlobalSightLocale p_trgLocale) throws PageSegmentsException
        {
            m_srcTuvs = p_srcTuvs;
            m_trgTuvs = p_trgTuvs;
            m_trgLocale = p_trgLocale;
            m_isModified = false;

            init();
        }

        // ////////////////////////////////////
        // Public Methods //
        // ////////////////////////////////////

        /**
         * gets the TU id of the segment pair
         */
        public long getTuId()
        {
            long jobId = m_sourcePage.getJobId();
            return ((Tuv) m_srcTuvs.get(0)).getTu(jobId).getId();
        }

        /**
         * gets a source TUV object. If the segments are merged, the returned
         * Tuv is a clone of the first source TUV of the merged segment that
         * contains an entire merged text.
         */
        public Tuv getSourceTuv()
        {
            return m_srcTuvClone;
        }

        /**
         * gets a target TUV object. If the segments are merged, the returned
         * Tuv is the first target TUV of the merged segment. The merged text
         * has been already set in this Tuv.
         */
        public Tuv getTargetTuv()
        {
            return (Tuv) m_trgTuvs.get(0);
        }

        /**
         * returns true if the segment is a merged segmnet.
         */
        public boolean isMergedSegment()
        {
            return m_isMerged;
        }

        /**
         * returns a list of original TU ids if the segment is merged, otherwise
         * null
         */
        public List<Long> getMergedTuIds()
        {
            List<Long> ids = null;
            if (m_isMerged)
            {
                ids = new ArrayList<Long>();
                long jobId = m_sourcePage.getJobId();
                Iterator it = m_srcTuvs.iterator();
                while (it.hasNext())
                {
                    Tu tu = ((Tuv) it.next()).getTu(jobId);
                    ids.add(tu.getIdAsLong());
                }
            }

            return ids;
        }

        /**
         * returns a list of original source TUV ids if the segment is merged,
         * otherwise null.
         */
        public List getMergedSourceTuvIds()
        {
            List ids = null;
            if (m_isMerged)
            {
                ids = new ArrayList();

                Iterator it = m_srcTuvs.iterator();
                while (it.hasNext())
                {
                    Tuv tuv = (Tuv) it.next();
                    ids.add(tuv.getIdAsLong());
                }
            }

            return ids;
        }

        /**
         * returns a list of original target TUV ids if the segment is merged,
         * otherwise null.
         */
        public List getMergedTargetTuvIds()
        {
            List ids = null;
            if (m_isMerged)
            {
                ids = new ArrayList();

                Iterator it = m_trgTuvs.iterator();
                while (it.hasNext())
                {
                    Tuv tuv = (Tuv) it.next();
                    ids.add(tuv.getIdAsLong());
                }
            }

            return ids;
        }

        /**
         * Merges the specified segments into this segment. This object must be
         * the first segment of the merged segment. The remaining objects must
         * be ordered as they apear in the document. If the segments cannot be
         * merged because the segments are not adjacent or run across the
         * paragraph boundary, an exception is throw that contains an
         * appropriate error message. The merged segment pair is marked as
         * modified.
         * 
         * @param p_segmentPairsToMerge
         *            the pairs to be merged under this segment. The
         *            SegmentPairs in this List must be in an ascending order.
         */
        public void mergeSegments(List p_segmentPairsToMerge)
                throws PageSegmentsException
        {
            // verify if the segment pairs can be merged
            verifyMergeable(p_segmentPairsToMerge);

            // add all TUVs in TUV list
            Iterator it = p_segmentPairsToMerge.iterator();
            while (it.hasNext())
            {
                SegmentPairImpl pair = (SegmentPairImpl) it.next();

                m_srcTuvs.addAll(pair.m_srcTuvs);
                m_trgTuvs.addAll(pair.m_trgTuvs);

                // remove merging SegmentPair from the list in PageSegments
                List list = (List) m_segmentPairList.get(m_trgLocale);
                if (list != null)
                {
                    list.remove(pair);
                }
            }

            // mark appropriate merge state in the target TUVs
            markMergeState();

            // set merged text
            m_srcTuvClone = createSrcClone();
            mergeTargetText();

            m_isMerged = true;
            m_isModified = true;
        }

        /**
         * Split a top segment from this segment. It returns SegmentPair object
         * that is the split top segment. If this segment is not merged,
         * PageSegmentsException is thrown. Both split segments are marked as
         * modified.
         * 
         * @return Top half SegmentPair object.
         */
        public SegmentPair splitTopSegment() throws PageSegmentsException
        {
            // verify if the segment is merged
            verifyMerged();

            List topSrcList = new ArrayList();
            topSrcList.add(m_srcTuvs.get(0));
            List topTrgList = new ArrayList();
            topTrgList.add(m_trgTuvs.get(0));

            SegmentPairImpl topSegment = new SegmentPairImpl(topSrcList,
                    topTrgList, m_trgLocale);
            topSegment.markMergeState();
            topSegment.m_isModified = true;

            m_srcTuvs.remove(0);
            m_trgTuvs.remove(0);
            markMergeState();
            m_isModified = true;
            init();

            // add topSegment to the SegmentPair list in PageSegments
            List list = (List) m_segmentPairList.get(m_trgLocale);
            if (list != null)
            {
                int idx = list.indexOf(this);
                if (idx != -1)
                {
                    list.add(idx, topSegment);
                }
            }

            return topSegment;
        }

        /**
         * Split a bottom segment from this segment. It returns SegmentPair
         * object that is the split bottom segment. If this segment is not
         * merged, PageSegmentsException is thrown. Both split segments are
         * marked as modified.
         * 
         * @return Bottom half SegmentPair object.
         */
        public SegmentPair splitBottomSegment() throws PageSegmentsException
        {
            // verify if the segment is merged
            verifyMerged();

            int tuvSize = m_srcTuvs.size();

            List bottomSrcList = new ArrayList();
            bottomSrcList.add(m_srcTuvs.get(tuvSize - 1));
            List bottomTrgList = new ArrayList();
            bottomTrgList.add(m_trgTuvs.get(tuvSize - 1));

            SegmentPairImpl bottomSegment = new SegmentPairImpl(bottomSrcList,
                    bottomTrgList, m_trgLocale);
            bottomSegment.markMergeState();
            bottomSegment.m_isModified = true;

            m_srcTuvs.remove(tuvSize - 1);
            m_trgTuvs.remove(tuvSize - 1);
            markMergeState();
            m_isModified = true;
            init();

            // add bottomSegment to the SegmentPair list in PageSegments
            List list = (List) m_segmentPairList.get(m_trgLocale);
            if (list != null)
            {
                int idx = list.indexOf(this);
                if (idx != -1)
                {
                    list.add(idx + 1, bottomSegment);
                }
            }

            return bottomSegment;
        }

        /**
         * Returns modified state.
         */
        public boolean isModified()
        {
            return m_isModified;
        }

        /**
         * Set modified flag to true.
         */
        public void setModified()
        {
            m_isModified = true;
        }

        // ////////////////////////////////////
        // Package Methods //
        // ////////////////////////////////////

        List getTargetTuvs()
        {
            return m_trgTuvs;
        }

        // ////////////////////////////////////
        // Private Methods //
        // ////////////////////////////////////

        /* initialize the object */
        private void init() throws PageSegmentsException
        {
            if (m_srcTuvs.size() > 1)
            {
                m_isMerged = true;
                m_srcTuvClone = createSrcClone();
            }
            else
            {
                m_isMerged = false;
                m_srcTuvClone = (Tuv) m_srcTuvs.get(0);
            }
        }

        /* veryfy if the merge is valid */
        private void verifyMergeable(List p_segmentPairsToMerge)
                throws PageSegmentsException
        {
            long jobId = m_sourcePage.getJobId();
            SegmentPairImpl prevPair = this;

            Iterator it = p_segmentPairsToMerge.iterator();
            while (it.hasNext())
            {
                SegmentPairImpl nextPair = (SegmentPairImpl) it.next();

                // Check paragraph boundary
                if (this.getSourceTuv().getTu(jobId).getPid() != nextPair
                        .getSourceTuv().getTu(jobId).getPid())
                {
                    String args[] =
                    { Long.toString(this.getTuId()),
                            Long.toString(nextPair.getTuId()) };
                    throw new PageSegmentsException(
                            PageSegmentsException.MSG_INVALID_MERGE_PARA_BOUNDARY,
                            args, null);
                }

                // Check translatable/localizable
                if (this.getSourceTuv().isLocalizable(jobId) != nextPair
                        .getSourceTuv().isLocalizable(jobId))
                {
                    String args[] =
                    { Long.toString(this.getTuId()),
                            Long.toString(nextPair.getTuId()) };
                    throw new PageSegmentsException(
                            PageSegmentsException.MSG_INVALID_MERGE_LOCALIZE_TYPE,
                            args, null);
                }

                // check that segments are adjacent to one another
                // note: assumes an ordered list of p_segmentPairsToBeMerged
                Tuv prevEndTuv = (Tuv) prevPair.m_srcTuvs
                        .get(prevPair.m_srcTuvs.size() - 1);
                Tuv nextEndTuv = (Tuv) nextPair.m_srcTuvs
                        .get(nextPair.m_srcTuvs.size() - 1);

                if (prevEndTuv.getOrder() != nextEndTuv.getOrder() - 1)
                {
                    String args[] =
                    { Long.toString(prevPair.getTuId()),
                            Long.toString(nextPair.getTuId()) };
                    throw new PageSegmentsException(
                            PageSegmentsException.MSG_INVALID_MERGE_NON_ADJ_SEGS,
                            args, null);
                }
                prevPair = nextPair;
            }
        }

        // verify if the segment is merged
        private void verifyMerged() throws PageSegmentsException
        {
            if (!m_isMerged)
            {
                String args[] =
                { Long.toString(getTuId()) };
                throw new PageSegmentsException(
                        PageSegmentsException.MSG_INVALID_SPLIT, args, null);
            }
        }

        private void markMergeState()
        {
            int size = m_trgTuvs.size();

            if (size > 1)
            {
                Tuv trgTuv = (Tuv) m_trgTuvs.get(0);
                trgTuv.setMergeState(Tuv.MERGE_START);

                for (int i = 1; i < (size - 1); i++)
                {
                    trgTuv = (Tuv) m_trgTuvs.get(i);
                    trgTuv.setMergeState(Tuv.MERGE_MIDDLE);
                }

                trgTuv = (Tuv) m_trgTuvs.get(size - 1);
                trgTuv.setMergeState(Tuv.MERGE_END);
            }
            else
            {
                Tuv trgTuv = (Tuv) m_trgTuvs.get(0);
                trgTuv.setMergeState(Tuv.NOT_MERGED);
            }

        }

        private void mergeTargetText() throws PageSegmentsException
        {
            try
            {
                TuvMerger.mergeTuvs(m_trgTuvs);
            }
            catch (Exception e)
            {
                throw new PageSegmentsException(e);
            }
        }

        private Tuv createSrcClone() throws PageSegmentsException
        {
            try
            {
                // create source tuv copy
                TuvImplVo orgTuv = (TuvImplVo) m_srcTuvs.get(0);
                TuvImplVo newTuv = new TuvImplVo(orgTuv);
                newTuv.setId(orgTuv.getId());

                // set combined text
                String tuvText = TuvMerger.getMergedText(m_srcTuvs);
                newTuv.setGxmlWithSubIds(tuvText);

                return newTuv;
            }
            catch (Exception e)
            {
                throw new PageSegmentsException(e);
            }
        }

    }

    // Iterator that returns an unmerged SegmentPair
    private class UnmergedSegmentPairIterator implements Iterator
    {
        GlobalSightLocale m_trgLocale;
        Iterator m_itTus;

        // constructor
        private UnmergedSegmentPairIterator(GlobalSightLocale p_trgLocale)
        {
            m_trgLocale = p_trgLocale;
            // iterator on List of TUs in PageSegments
            m_itTus = m_tus.iterator();
        }

        public boolean hasNext()
        {
            return m_itTus.hasNext();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object next()
        {
            long jobId = m_sourcePage.getJobId();
            Tu tu = (Tu) m_itTus.next();
            Tuv srcTuv = tu.getTuv(m_sourcePage.getGlobalSightLocale().getId(),
                    jobId);
            Tuv trgTuv = tu.getTuv(m_trgLocale.getId(), jobId);

            if (srcTuv == null || trgTuv == null)
            {
                throw new NoSuchElementException(
                        "Source or target TUV couldn't be found.");
            }

            ArrayList srcList = new ArrayList(1);
            srcList.add(srcTuv);

            ArrayList trgList = new ArrayList(1);
            trgList.add(trgTuv);

            SegmentPairImpl segmentPair = null;
            try
            {
                segmentPair = new SegmentPairImpl(srcList, trgList, m_trgLocale);
            }
            catch (PageSegmentsException e)
            {
                s_logger.error(e.getMessage(), e);

                throw new NoSuchElementException(e.getMessage());
            }

            return segmentPair;
        }

        public void remove()
        {
            // it shouldn't be called.
        }

    }

}
