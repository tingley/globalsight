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
package com.globalsight.everest.edit.online;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.cxe.adapter.adobe.InddTuMapping;
import com.globalsight.cxe.adapter.adobe.InddTuMappingHelper;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.edit.ImageHelper;
import com.globalsight.everest.edit.SegmentProtectionManager;
import com.globalsight.everest.edit.SegmentRepetitions;
import com.globalsight.everest.edit.offline.xliff.XLIFFStandardUtil;
import com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap;
import com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapPersistenceManager;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.SnippetPageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageupdate.GxmlPreviewer;
import com.globalsight.everest.page.pageupdate.PageUpdateApi;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvMerger;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.inprogresstm.DynamicLeveragedSegment;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TagNode;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.terminology.Hitlist.Hit;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.terminology.termleverager.TermLeverageMatchResultSet;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

/**
 * OnlineEditorManagerLocal implements the OnlineEditorManager server interface
 * that contains APIs to serve online Editor UI layer data needs.
 */
public class OnlineEditorManagerLocal implements OnlineEditorManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(OnlineEditorManagerLocal.class);

    /**
     * <p>
     * The maximum number of previous segment versions or TM matches shown in
     * the segment editor.
     * </p>
     * 
     * <p>
     * Should be more than 3 because we can have more than 3 stages.
     * </p>
     */
    public static final int MAX_NUM_VERSIONS = 10;

    /**
     * CSS class names that select the color of segments.
     */
    public static final String STYLE_EXACT_MATCH = "editorSegmentExact";
    public static final String STYLE_FUZZY_MATCH = "editorSegmentFuzzy";
    public static final String STYLE_NO_MATCH = "editorSegment";
    public static final String STYLE_UPDATED = "editorSegmentUpdated";
    public static final String STYLE_APPROVED = "editorSegmentApproved";
    public static final String STYLE_LOCKED = "editorSegmentLocked";
    public static final String STYLE_EXCLUDED = "editorSegmentExcluded";
    public static final String STYLE_MT = "editorSegmentMT";
    public static final String STYLE_UNVERIFIED = "editorSegmentUnverified";
    public static final String STYLE_REPETITION = "editorSegmentRepetition";
    public static final String STYLE_CONTEXT = "segmentContext";
    public static final String STYLE_CONTEXT_UNLOCK = "segmentContextUnlock";
    /**
     * CSS class names for new editor.
     */
    public static final String STYLE_SEGMENT = "segment";
    public static final String STYLE_SEGMENT_FUZZY = "segmentFuzzy";
    public static final String STYLE_SEGMENT_EXACT = "segmentExact";
    public static final String STYLE_SEGMENT_LOCKED = "segmentLocked";
    public static final String STYLE_SEGMENT_UPDATED = "segmentUpdated";
    public static final String STYLE_SEGMENT_UNVERIFIED = "segmentUnverified";
    public static final String STYLE_SEGMENT_REPETITION = "segmentRepetition";

    // Default Context (deprecated)
    private static int READY_CASE_1 = 1;
    // In-Context
    private static int READY_CASE_2 = 2;
    // No Match
    private static int READY_CASE_3 = 3;
    
    /** Sub id 0 represents the top-level segment. */
    public static final String DUMMY_SUBID = "0";

    protected PageManager m_pageManager;
    protected TuvManager m_tuvManager;
    protected TermLeverageManager m_termManager;
    protected LeverageMatchLingManager m_lingManager;
    protected ImageReplaceFileMapPersistenceManager m_imageManager;
    protected CommentManager m_commentManager;
    protected InProgressTmManager m_inprogressTmManager;
    protected EditorState editorState;
    protected HashMap<String, String> searchMap = null;
    /**
     * Data caching class to hold page-related data (source + target). Lock
     * objects apply to the members that follow them, until the next lock
     * object. See methods in the section "Caching-related helper methods".
     */
    static protected class PageCache
    {
        public Object m_pageLock = new Object();
        private SourcePage m_sourcePage = null;
        private TargetPage m_targetPage = null;

        public Object m_tusLock = new Object();
        private Collection<TuImpl> m_tus = null;
        private Map<Long, TuImpl> m_tusInMap = null;

        public Object m_sourceTuvsLock = new Object();
        private ArrayList<Tuv> m_sourceTuvs = null;
        private SegmentRepetitions m_repetitions = null;

        public Object m_targetTuvsLock = new Object();
        private List<Tuv> m_targetTuvs = null;

        public Object m_imageMapsLock = new Object();
        private ArrayList<ImageReplaceFileMap> m_imageMaps = null;

        public Object m_commentsLock = new Object();
        private ArrayList<Issue> m_comments = null;

        public Object m_matchTypesLock = new Object();
        private MatchTypeStatistics m_matchTypes = null;

        public Object m_templateLock = new Object();
        private PageTemplate m_sourceTemplate = null;
        private PageTemplate m_targetTemplate = null;
        private ArrayList<TemplatePart> m_sourceTemplateParts = null;
        private ArrayList<TemplatePart> m_targetTemplateParts = null;
        private HashSet m_interpretedTuIds = null;
        
        private List<Long> m_currentPageTuIDS = null;
        private List<Tu> m_tu_ICE = null;
        private List<Tu> m_tu_100 = null;
        private List<Tu> m_tu_REPEATED = null;
        private List<Tu> m_tu_REPETITIONS = null;
        private List<Tu> m_tu_MODIFIED = null;
        private List<Tu> m_tu_COMMENTED = null;

        /** Clear cache when switching to different source page. */
        public void clearAll()
        {
            m_sourcePage = null;
            m_targetPage = null;
            m_tus = null;
            m_tusInMap = null;
            m_sourceTuvs = null;
            m_targetTuvs = null;
            m_repetitions = null;
            m_imageMaps = null;
            m_comments = null;
            m_matchTypes = null;
            m_sourceTemplate = null;
            m_targetTemplate = null;
            m_sourceTemplateParts = null;
            m_targetTemplateParts = null;
            m_interpretedTuIds = null;
            
            m_currentPageTuIDS = null;
            m_tu_ICE = null;
            m_tu_100 = null;
            m_tu_REPEATED = null;
            m_tu_REPETITIONS = null;
            m_tu_MODIFIED = null;
            m_tu_COMMENTED = null;
        }

        /** Clear target cache when switching to different target page. */
        private void clearTarget()
        {
            m_targetPage = null;
            m_targetTuvs = null;
            m_imageMaps = null;
            m_comments = null;
            m_matchTypes = null;
            m_targetTemplate = null;
            m_targetTemplateParts = null;
            m_interpretedTuIds = null;

            m_currentPageTuIDS = null;
            m_tu_ICE = null;
            m_tu_100 = null;
            m_tu_REPEATED = null;
            m_tu_REPETITIONS = null;
            m_tu_MODIFIED = null;
            m_tu_COMMENTED = null;
        }

        public SourcePage getSourcePage()
        {
            return m_sourcePage;
        }

        public void setSourcePage(SourcePage p_page)
        {
            if (m_sourcePage != null && m_sourcePage.getId() != p_page.getId())
            {
                clearAll();
            }

            m_sourcePage = p_page;
        }

        public TargetPage getTargetPage()
        {
            return m_targetPage;
        }

        public void setTargetPage(TargetPage p_page)
        {
            if (m_targetPage != null && m_targetPage.getId() != p_page.getId())
            {
                clearTarget();
            }

            m_targetPage = p_page;
        }

        public Collection<TuImpl> getTus()
        {
            return m_tus;
        }

        public void setTus(Collection<TuImpl> p_tus)
        {
            m_tus = p_tus;
        }

        public ArrayList<Tuv> getSourceTuvs()
        {
            return m_sourceTuvs;
        }

        public void setSourceTuvs(ArrayList<Tuv> p_tuvs)
        {
            m_sourceTuvs = p_tuvs;
            m_repetitions = new SegmentRepetitions(m_sourceTuvs);
        }

        public List<Tuv> getTargetTuvs()
        {
            return m_targetTuvs;
        }

        public void setTargetTuvs(ArrayList<Tuv> p_tuvs)
        {
            m_targetTuvs = p_tuvs;
        }

        public void updateTuv(Tuv p_tuv)
        {
            int size = m_targetTuvs.size();
            for (int i = 0; i < size; ++i)
            {
                Tuv tuv = (Tuv) m_targetTuvs.get(i);

                if (tuv.getId() == p_tuv.getId())
                {
                    m_targetTuvs.set(i, p_tuv);
                    return;
                }
            }

            throw new RuntimeException("Page Cache: "
                    + "Target TUV object not found in list.");
        }

        // getter defined here, setter is in setSourceTuvs()
        public SegmentRepetitions getRepetitions()
        {
            return m_repetitions;
        }

        public ArrayList getImageMaps()
        {
            return m_imageMaps;
        }

        public void setImageMaps(ArrayList<ImageReplaceFileMap> p_maps)
        {
            m_imageMaps = p_maps;
        }

        public void updateImageMap(ImageReplaceFileMap p_map)
        {
            int size = m_imageMaps.size();
            for (int i = 0; i < size; ++i)
            {
                ImageReplaceFileMap map = (ImageReplaceFileMap) m_imageMaps
                        .get(i);

                if (map.getId() == p_map.getId())
                {
                    // if the source and temp are blanked out then
                    // the image has been removed. so remove from the map too
                    if (p_map.getRealSourceName() == null
                            && p_map.getTempSourceName() == null)
                    {
                        m_imageMaps.remove(map);
                    }
                    else
                    {
                        m_imageMaps.set(i, p_map);
                    }
                    return;
                }
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Page Cache: " + "Image map not found, adding: "
                        + p_map);
            }

            m_imageMaps.add(p_map);
        }

        public ArrayList getComments()
        {
            return m_comments;
        }

        public void setComments(ArrayList<Issue> p_comments)
        {
            m_comments = p_comments;
        }

        public void addComment(Issue p_comment)
        {
            m_comments.add(p_comment);
        }

        public void updateComment(Issue p_comment)
        {
            // TODO: organize m_comments as HashMap
            // String key = p_comment.getLogicalKey();

            for (int i = 0, max = m_comments.size(); i < max; i++)
            {
                Issue issue = (Issue) m_comments.get(i);

                if (issue.getId() == p_comment.getId())
                {
                    m_comments.set(i, p_comment);
                    return;
                }
            }
        }

        public MatchTypeStatistics getMatchTypes()
        {
            return m_matchTypes;
        }

        public void setMatchTypes(MatchTypeStatistics p_map)
        {
            m_matchTypes = p_map;
        }

        public PageTemplate getSourceTemplate()
        {
            return m_sourceTemplate;
        }

        // p_template can be a SnippetPageTemplate.
        public void setSourceTemplate(PageTemplate p_template)
        {
            if (p_template == null)
            {
                m_sourceTemplateParts = null;
            }
            else if (m_sourceTemplate != null
                    && m_sourceTemplate.getType() != p_template.getType())
            {
                if (m_targetTemplate != null
                        && m_targetTemplate.getType() == p_template.getType())
                {
                    m_sourceTemplateParts = m_targetTemplateParts;
                }
                else
                {
                    m_sourceTemplateParts = null;
                }
            }

            m_sourceTemplate = p_template;
        }

        public PageTemplate getTargetTemplate()
        {
            return m_targetTemplate;
        }

        // p_template can be a SnippetPageTemplate.
        public void setTargetTemplate(PageTemplate p_template)
        {
            if (p_template == null)
            {
                m_targetTemplateParts = null;
            }
            else if (m_targetTemplate != null
                    && m_targetTemplate.getType() != p_template.getType())
            {
                if (m_sourceTemplate != null
                        && m_sourceTemplate.getType() == p_template.getType())
                {
                    m_targetTemplateParts = m_sourceTemplateParts;
                }
                else
                {
                    m_targetTemplateParts = null;
                }
            }

            m_targetTemplate = p_template;
        }

        public ArrayList<TemplatePart> getSourceTemplateParts()
        {
            return m_sourceTemplateParts;
        }

        public void setSourceTemplateParts(ArrayList<TemplatePart> p_parts)
        {
            m_sourceTemplateParts = p_parts;
        }

        public ArrayList<TemplatePart> getTargetTemplateParts()
        {
            return m_targetTemplateParts;
        }

        public void setTargetTemplateParts(ArrayList<TemplatePart> p_parts)
        {
            m_targetTemplateParts = p_parts;
        }

        public HashSet getInterpretedTuIds()
        {
            return m_interpretedTuIds;
        }

        public void setInterpretedTuIds(HashSet p_set)
        {
            m_interpretedTuIds = p_set;
        }

        public List<Long> getCurrentPageTuIDS()
        {
            return m_currentPageTuIDS;
        }

        public void setCurrentPageTuIDS(List<Long> m_currentPageTuIDS)
        {
            this.m_currentPageTuIDS = m_currentPageTuIDS;
        }

        public List<Tu> getTuICE()
        {
            return m_tu_ICE;
        }

        public void setTuICE(List<Tu> p_tus)
        {
            this.m_tu_ICE = p_tus;
        }

        public List<Tu> getTu100()
        {
            return m_tu_100;
        }

        public void setTu100(List<Tu> p_tus)
        {
            this.m_tu_100 = p_tus;
        }

        public List<Tu> getTuRepeated()
        {
            return m_tu_REPEATED;
        }

        public void setTuRepeated(List<Tu> p_tus)
        {
            this.m_tu_REPEATED = p_tus;
        }

        public List<Tu> getTuRepeations()
        {
            return m_tu_REPETITIONS;
        }

        public void setTuRepeations(List<Tu> p_tus)
        {
            this.m_tu_REPETITIONS = p_tus;
        }

        public List<Tu> getTuModified()
        {
            return m_tu_MODIFIED;
        }

        public void setTuModified(List<Tu> p_tus)
        {
            this.m_tu_MODIFIED = p_tus;
        }

        public List<Tu> getTuCommonted()
        {
            return m_tu_COMMENTED;
        }

        public void setTuCommonted(List<Tu> p_tus)
        {
            this.m_tu_COMMENTED = p_tus;
        }
    }

    /** Cache object for increased parallel performance. */
    private PageCache m_pageCache = new PageCache();

    public OnlineEditorManager newInstance() throws OnlineEditorException,
            RemoteException
    {
        OnlineEditorManager lm = new OnlineEditorManagerLocal();
        return new OnlineEditorManagerWLRMIImpl(lm);
    }

    public OnlineEditorManagerLocal() throws OnlineEditorException
    {
        init();
    }

    /**
     * Initialize the object (only called from constructor).
     */
    private void init() throws OnlineEditorException
    {
        try
        {
            m_pageManager = ServerProxy.getPageManager();
            m_tuvManager = ServerProxy.getTuvManager();
            m_termManager = ServerProxy.getTermLeverageManager();
            m_lingManager = LingServerProxy.getLeverageMatchLingManager();
            m_imageManager = ServerProxy.getImageReplaceFileMapPersistenceManager();
            m_commentManager = ServerProxy.getCommentManager();
            m_inprogressTmManager = LingServerProxy.getInProgressTmManager();
        }
        catch (GeneralException ge)
        {
            String[] args =
            { "Couldn't find remote objects (PageManager, TuvManager, "
                    + "LeverageMatchLingManager, "
                    + "ImageReplaceFileMapPersistenceManager)" };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_INIT_SERVER, args, ge);
        }
    }

    /**
     * For source page editing: returns the GXML of the source page.
     */
    public String getSourcePageGxml(long p_srcPageId)
            throws OnlineEditorException, RemoteException
    {
        String result;

        // Clear cache to refresh all data from DB.
        m_pageCache.clearAll();

        try
        {
            SourcePage srcPage = getSourcePage(p_srcPageId);
            long jobId = srcPage.getJobId();

            String state = srcPage.getPageState();
            if (state.equals(PageState.UPDATING))
            {
                String[] args =
                { String.valueOf(p_srcPageId) };

                CATEGORY.info("Source page "
                        + srcPage.getExternalPageId()
                        + " (ID "
                        + p_srcPageId
                        + ") cannot be edited because it is already being updated.");

                throw new OnlineEditorException(
                        OnlineEditorException.MSG_SOURCEPAGE_NOT_EDITABLE,
                        args, null);
            }

            // Load the TUs into the Toplink cache to prevent called
            // code from loading each TU individually.
            getPageTus(srcPage);

            // get the Page Template
            PageTemplate template = getPageTemplate(UIConstants.VIEWMODE_GXML,
                    srcPage);

            if (template.getTemplateParts() == null
                    || template.getTemplateParts().size() == 0)
            {
                ArrayList<TemplatePart> parts = getSourceTemplateParts(
                        srcPage.getIdAsLong(), template.getTypeAsString());

                template.setTemplateParts(parts);
            }

            // get the Tuvs in the page
            List<Tuv> tuvs = getPageTuvs(srcPage);

            // inject them into the template
            for (int i = 0, max = tuvs.size(); i < max; i++)
            {
                Tuv srcTuv = (Tuv) tuvs.get(i);

                Long tuId = srcTuv.getTu(jobId).getIdAsLong();
                String tuvContent;

                if (srcTuv.getTu(jobId).isLocalizable())
                {
                    tuvContent = srcTuv.getGxmlExcludeTopTags();
                }
                else
                {
                    tuvContent = srcTuv.getGxml();
                }

                template.insertTuvContent(tuId, tuvContent);
            }

            result = template.getPageData();
        }
        catch (OnlineEditorException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            CATEGORY.error("The data for source page " + p_srcPageId
                    + " could not be loaded.", ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_LOAD_SOURCEPAGE, null,
                    ex);
        }

        return result;
    }

    /**
     * For source page editing: validates the new GXML of the source page.
     * 
     * @return list of error messages.
     */
    public ArrayList validateSourcePageGxml(String p_gxml)
            throws RemoteException
    {
        return PageUpdateApi.validateSourcePageGxml(p_gxml);
    }

    /**
     * For source page editing: updates the GXML of the source page. The GXML is
     * first validated and a list of error messages may be returned. If no
     * errors are returned, the source page will be updated in the background
     * and an email will be sent.
     * 
     * @return list of error messages.
     */
    public ArrayList updateSourcePageGxml(long p_srcPageId, String p_gxml)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            SourcePage srcPage = getSourcePage(p_srcPageId);

            // Whatever the outcome, totally invalidate the cache to
            // free resources.
            m_pageCache.clearAll();

            return PageUpdateApi.updateSourcePageGxml(srcPage, p_gxml);
        }
        catch (Exception ex)
        {
            String[] args =
            { "Failed to retrieve source page." };

            CATEGORY.error(args[0], ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW, args, ex);
        }
    }

    /**
     * For source page editing: validates the new GXML of the source page.
     * 
     * @return list of error messages.
     */
    public String getGxmlPreview(String p_gxml, String p_locale)
            throws Exception, RemoteException
    {
        GxmlPreviewer o = new GxmlPreviewer(p_gxml, p_locale);
        return o.getGxmlPreview();
    }

    public String getSourcePageView(long p_srcPageId,
            RenderingOptions p_options, GlobalSightLocale p_locale,
            boolean p_dirtyTemplate, PaginateInfo p_pi)
            throws OnlineEditorException, RemoteException
    {
        return getSourcePageView(p_srcPageId, p_options, p_locale,
                p_dirtyTemplate, p_pi, null);
    }

    /**
     * <p>
     * Returns HTML formatted output for source page view.
     * </p>
     * 
     * @param p_viewMode
     *            - The page view mode, available modes are defined by
     *            constants.
     * @param p_srcPage
     *            - The source page object to be viewed.
     * 
     * @return the page view as HTML String
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public String getSourcePageView(long p_srcPageId,
            RenderingOptions p_options, GlobalSightLocale p_locale,
            boolean p_dirtyTemplate, PaginateInfo p_pi, HashMap searchMap)
            throws OnlineEditorException, RemoteException
    {
        String result;

        if (p_dirtyTemplate)
        {
            invalidateCachedTemplates();
        }

        try
        {
            SourcePage srcPage = getSourcePage(p_srcPageId);
            File previewFile = new File(AmbFileStoragePathUtils.getCxeDocDirPath()  + "/" + srcPage.getExternalPageId() + ".preview.html");
            if (p_options.getViewMode() == VIEWMODE_PREVIEW && previewFile.exists())
            {
                result = GxmlUtil.cleanUpDisplayHtml(FileUtil.readFile(previewFile, "UTF-8"));
                return result;
            }
            // Load the TUs into the Toplink cache to prevent called
            // code from loading each TU individually.
            getPageTus(srcPage);

            // get the Page Template
            PageTemplate template = getPageTemplate(p_options.getViewMode(),
                    srcPage);

            // Sat Jun 08 16:27:59 2002 add/delete: if this page
            // contains add/delete instructions, turn the template
            // into its extended form. getPageData() will then execute
            // the GS instructions in the template.
            if (containGsTags(srcPage)
                    && !(template instanceof SnippetPageTemplate))
            {
                template = makeSnippetPageTemplate(template, p_locale, srcPage);
                m_pageCache.setSourceTemplate(template);
            }

            if (template.getTemplateParts() == null
                    || template.getTemplateParts().size() == 0)
            {
                ArrayList<TemplatePart> parts = getSourceTemplateParts(
                        srcPage.getIdAsLong(), template.getTypeAsString());

                // for snippets, computes positions
                template.setTemplateParts(parts);
            }

            // get the Tuvs in the page
            List<Tuv> tuvs = getPageTuvs(srcPage);

            List<Long> tuIdList = getCurrentTuIds(tuvs, p_pi);

            // insert all tuv content into template despite of current page num
            // believe this won't bring performance issue
            for (int i = 0, max = tuvs.size(); i < max; i++)
            {
                Tuv tuv = (Tuv) tuvs.get(i);
                String html = getSourceDisplayHtml(tuv,
                        p_options.getViewMode(), srcPage, searchMap, p_options);
                template.insertTuvContent(tuv.getTuId(), html);
            }

            p_options.setEditMode(EDITMODE_READ_ONLY);

            // for "text" and "list" view, return tuvs on current page.
            // otherwise, return all tuvs data as original.
            if (p_options.getViewMode() == EditorConstants.VIEWMODE_TEXT
                    || p_options.getViewMode() == EditorConstants.VIEWMODE_DETAIL)
            {
                result = template.getPageData(tuIdList);
            }
            else
            {
                result = template.getPageData(p_options);
            }
            // "text" view mode need "PRE" tag.
            if (p_options.getViewMode() == EditorConstants.VIEWMODE_TEXT)
            {
                if (!result.trim().startsWith(
                        "<PRE><SPAN CLASS=\"editorStandardText\">"))
                {
                    result = "<PRE><SPAN CLASS=\"editorStandardText\">"
                            + result.trim();
                }
                if (!result.trim().endsWith("</SPAN></PRE>"))
                {
                    result = result.trim() + "</SPAN></PRE>";
                }
            }
            // clear temporary data in template
            template.clearTuvContent();

            // If this is a Prs template, we must remove the dynamic
            // preview link and its associated data.

            if (srcPage.getDataSourceType().equals("db"))
            {
                result = removePreviewLink(result);
            }

            // Extra clean-up process for preview mode only: disables
            // active elements that would make the editor navigate.
            // Active elements are scripts, onXXX handlers, forms etc.
            if (p_options.getViewMode() == VIEWMODE_PREVIEW)
            {
                result = GxmlUtil.cleanUpDisplayHtml(result);
            }
        }
        catch (Exception ex)
        {
            String[] args =
            { "Failed to retrieve source page view." };

            CATEGORY.error(args[0], ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW, args, ex);
        }

        return result;
    }

    public String getTargetPageView(long p_trgPageId, EditorState p_state,
            Vector p_excludedItemTypes, boolean p_dirtyTemplate)
            throws OnlineEditorException, RemoteException
    {
        return getTargetPageView(p_trgPageId, p_state, p_excludedItemTypes,
                p_dirtyTemplate, null);
    }

    /**
     * @see OnlineEditorManager.getTargetPageView(long, EditorState, Vector,
     *      boolean, HashMap)
     */
    public String getTargetPageView(long p_trgPageId, EditorState p_state,
            Vector p_excludedItemTypes, boolean p_dirtyTemplate,
            HashMap p_searchMap) throws OnlineEditorException, RemoteException
    {
        String result = null;
        RenderingOptions options = p_state.getRenderingOptions();
        TranslationMemoryProfile tmProfile = p_state.getTmProfile();
        options.setTmProfile(tmProfile);
        options.setUserName(p_state.getUserName());

        // Old editor is template-based, new paragraph editor is not.
        // I wouldn't want to add another meaningless template
        // that would look like current list view (basically empty).
        boolean useTemplate = options.getUiMode() != UIMODE_PREVIEW_EDITOR;
        boolean reviewMode = options.getUiMode() == UIConstants.UIMODE_REVIEW;
        boolean reviewReadOnly = options.getUiMode() == UIConstants.UIMODE_REVIEW_READ_ONLY;

        try
        {
            if (p_dirtyTemplate)
            {
                invalidateCachedTemplates();
            }

            // The motherlode of all database queries
            TargetPage targetPage = getTargetPage(p_trgPageId);
            SourcePage sourcePage = targetPage.getSourcePage();
            m_pageCache.setSourcePage(sourcePage);
            long jobId = sourcePage.getJobId();

            String pageType = getExtractedSourceFile(sourcePage).getDataType();

            // Load the TUs into the Toplink cache to prevent called
            // code from loading each TU individually.
            getPageTus(sourcePage);

            PageTemplate template = null;

            if (useTemplate)
            {
                template = getPageTemplate(options.getViewMode(), targetPage);

                // Sat Jun 08 16:27:59 2002 add/delete: if this page
                // contains add/delete instructions, turn the template
                // into its extended form. getPageData() will then execute
                // the template.
                if (containGsTags(sourcePage)
                        && !(template instanceof SnippetPageTemplate))
                {
                    GlobalSightLocale locale = targetPage
                            .getGlobalSightLocale();
                    template = makeSnippetPageTemplate(template, locale,
                            sourcePage);
                    m_pageCache.setTargetTemplate(template);
                }

                if (template.getTemplateParts() == null
                        || template.getTemplateParts().size() == 0)
                {
                    List<TemplatePart> parts = getTargetTemplateParts(
                            sourcePage.getIdAsLong(),
                            template.getTypeAsString());

                    // for snippets, computes positions
                    template.setTemplateParts(parts);
                }
            }

            List<Tuv> sourceTuvs = getPageTuvs(sourcePage);
            ArrayList imageMaps = getImageMaps(targetPage);
            ArrayList<Issue> comments = null;
            SegmentRepetitions repetitions = getRepetitions(sourcePage);

            if (reviewMode || reviewReadOnly || SegmentFilter.isFilterSegment(p_state))
            {
                comments = getComments(targetPage);
            }

            Long targetLocaleId = targetPage.getGlobalSightLocale().getIdAsLong();
            MatchTypeStatistics tuvMatchTypes = null;
            if (sourceTuvs.size() > 0)
            {
                tuvMatchTypes = getMatchTypes(sourcePage.getIdAsLong(),
                        targetLocaleId);
            }
            
            List<Tuv> targetTuvs = getPageTuvs(targetPage);
            List<Tuv> targetTuvs2 = new ArrayList<Tuv>();
            if (targetTuvs != null && targetTuvs.size() > 0)
            {
                Iterator<Tuv> it = targetTuvs.iterator();
                while (it.hasNext())
                {
                    Tuv targetTuv = (Tuv) it.next();

                    boolean isWSXlf = false;
                    boolean isAutoCommit = false;
                    if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(targetTuv
                            .getTu(jobId).getGenerateFrom()))
                    {
                        isWSXlf = true;
                    }
                    if (targetTuv.getLastModifiedUser() != null
                            && targetTuv.getLastModifiedUser().indexOf("_MT") > -1)
                    // && targetTuv.getState().getValue() == TuvState.LOCALIZED
                    // .getValue())
                    {
                        isAutoCommit = true;
                    }
                    // Clone the targetTuv to avoid changes are stored into DB.
                    TuvImpl cloneTargetTuv = new TuvImpl((TuvImpl) targetTuv);
                    cloneTargetTuv.setId(targetTuv.getId());// tuvId also needed
                    // If WS XLF and auto-commit,should display source in
                    // pop-up editor.
                    if (isWSXlf && isAutoCommit)
                    {
                        Tuv sourceTuv = targetTuv.getTu(jobId).getTuv(
                                p_state.getSourceLocale().getId(), jobId);
                        cloneTargetTuv.setGxml(sourceTuv.getGxml());
                        cloneTargetTuv.setLastModifiedUser(null);
                        cloneTargetTuv.setState(TuvState.NOT_LOCALIZED);
                    }
                    targetTuvs2.add(cloneTargetTuv);
                }
            }
            targetTuvs = targetTuvs2;

            if (!useTemplate)
            {
                // Gets the filtered source and target tuvs, and reset PaginateInfo.
                Map<String, List<Tuv>> filterResult = SegmentFilter
                        .operateForSegmentFilter(m_pageCache, sourceTuvs,
                                targetTuvs, p_state, tuvMatchTypes, comments,
                                p_excludedItemTypes, p_trgPageId, jobId);
                if (filterResult == null)
                {
                    result = getTargetDisplayHtml2(sourceTuvs, targetTuvs, options,
                            p_excludedItemTypes, targetPage, tuvMatchTypes,
                            pageType, repetitions, p_state, p_searchMap, null);
                }
                else
                {
                    result = getTargetDisplayHtml2(sourceTuvs,
                            targetTuvs, options,
                            p_excludedItemTypes, targetPage, tuvMatchTypes,
                            pageType, repetitions, p_state, p_searchMap, filterResult);
                }
            }
            else
            {
                String html;
                
                // Gets the filtered source and target tuvs, and reset PaginateInfo.
                Map<String, List<Tuv>> filterResult = SegmentFilter
                        .operateForSegmentFilter(m_pageCache, sourceTuvs,
                                targetTuvs, p_state, tuvMatchTypes, comments,
                                p_excludedItemTypes, p_trgPageId, jobId);
                // Gets the Paginate info
                PaginateInfo pi = getPaginateInfo(p_state, sourceTuvs, filterResult);
                int segmentNumPerPage = pi.getSegmentNumPerPage();
                int currentPageNum = pi.getCurrentPageNum();
                int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
                
                // Gets term leverage matches from DB instead of dynamic leveraging.
                TermLeverageMatchResultSet termLMResultSet = m_termManager
                        .getTermMatchesForPage(sourcePage,
                                targetPage.getGlobalSightLocale());

                // Gets Display TU Id List.
                List<Long> displayTuIdList = getDisplayIdList(sourceTuvs,
                        targetTuvs, beginIndex, segmentNumPerPage,
                        filterResult, jobId);
                m_pageCache.setCurrentPageTuIDS(displayTuIdList);
                if (displayTuIdList == null || displayTuIdList.size() == 0
                		|| options.getViewMode() == VIEWMODE_LIST)
                    return "";

                // insert all tuv content into template despite of current page
                // num
                // believe this won't bring performance issue
                for (int i = 0, max = sourceTuvs.size(); i < max; i++)
                {
                    Tuv srcTuv = (Tuv) sourceTuvs.get(i);
                    Tuv trgTuv = (Tuv) targetTuvs.get(i);

                    if (LeverageUtil.isIncontextMatch(i, sourceTuvs,
                            targetTuvs, tuvMatchTypes, p_excludedItemTypes,
                            jobId))
                    {
                        html = getInContextTargetDisplayHtml(srcTuv, trgTuv,
                                options, termLMResultSet,
                                p_excludedItemTypes, targetPage, tuvMatchTypes,
                                imageMaps, comments, repetitions, p_searchMap,
                                p_state);
                        template.insertTuvContent(trgTuv.getTu(jobId)
                                .getIdAsLong(), html);
                    }
                    else
                    {
                        html = getTargetDisplayHtml(srcTuv, trgTuv, options,
                                termLMResultSet, p_excludedItemTypes,
                                targetPage, tuvMatchTypes, imageMaps, comments,
                                repetitions, p_searchMap, p_state);
                        template.insertTuvContent(trgTuv.getTu(jobId)
                                .getIdAsLong(), html);
                    }

                    String mergeState = trgTuv.getMergeState();
                    if (mergeState.equals(Tuv.MERGE_START))
                    {
                        do
                        {
                            i++;
                            if (i >= targetTuvs.size())
                            {
                                break;
                            }

                            trgTuv = (Tuv) targetTuvs.get(i);
                            html = getTargetDisplayHtmlMerged(trgTuv, options,
                                    comments);
                            template.insertTuvContent(trgTuv.getTu(jobId)
                                    .getIdAsLong(), html);
                            mergeState = trgTuv.getMergeState();
                        } while (!mergeState.equals(Tuv.MERGE_END));
                    }
                }

                // for "text" and "list" view, only return tuvs on current page.
                // otherwise, return all tuvs' data as original.
                if (options.getViewMode() == EditorConstants.VIEWMODE_TEXT
                        || options.getViewMode() == EditorConstants.VIEWMODE_DETAIL)
                {
                    result = template.getPageData(displayTuIdList);
                }
                else
                {
                    result = template.getPageData(options);
                }
                // "text" view mode need extra html tags.
                if (options.getViewMode() == EditorConstants.VIEWMODE_TEXT)
                {
                    if (!result.trim().startsWith(
                            "<PRE><SPAN CLASS=\"editorStandardText\">"))
                    {
                        result = "<PRE><SPAN CLASS=\"editorStandardText\">"
                                + result.trim();
                    }
                    if (!result.trim().endsWith("</SPAN></PRE>"))
                    {
                        result = result.trim() + "</SPAN></PRE>";
                    }
                }

                // clear temporary data in template
                template.clearTuvContent();

                // If this is a Prs template in text mode, we must insert
                // a dynamic preview link to /CapExportServlet and/or its
                // associated data.

                // There must be a good way to find out when to do this,
                // i.e, we don't want to do this for a large HTML page.
                if (targetPage.getDataSourceType().equals("db"))
                {
                    if (sourcePage.getRequest().isPageCxePreviewable())
                    {
                        result = insertTuvIds(result, targetTuvs);
                    }
                    else
                    {
                        result = removePreviewLink(result);
                    }
                }

                // Extra clean-up process for preview mode only: disables
                // active elements that would make the editor navigate.
                // Active elements are scripts, onXXX handlers, forms etc.
                if (options.getViewMode() == VIEWMODE_PREVIEW)
                {
                    result = GxmlUtil.cleanUpDisplayHtml(result);
                }
            }
        }
        catch (Exception ex)
        {
            String[] args =
            { "Failed to retrieve target page view." };

            CATEGORY.error(args[0], ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW, args, ex);
        }

        return result;
    }

    // Gets Display Tu Id List
    private List<Long> getDisplayIdList(List<Tuv> p_srcTuvs,
            List<Tuv> p_trgTuvs, int p_segmentIndex, int p_segmentNumPerPage,
            Map<String, List<Tuv>> p_filterResult, long p_jobId)
    {
        List<Long> result = new ArrayList<Long>();
        List<Tuv> srcTuvs = p_srcTuvs;
        List<Tuv> trgTuvs = p_trgTuvs;
        if (p_filterResult != null && p_filterResult.get(SegmentFilter.KEY_SOURCE) != null)
        {
            srcTuvs = (List<Tuv>) p_filterResult.get(SegmentFilter.KEY_SOURCE);
            trgTuvs = (List<Tuv>) p_filterResult.get(SegmentFilter.KEY_TARGET);
        }

        for (int i = p_segmentIndex, max = srcTuvs.size(), _count = 0; _count < p_segmentNumPerPage
                && i < max; i++)
        {
            _count++;

            Tuv trgTuv = (Tuv) trgTuvs.get(i);
            result.add(trgTuv.getTu(p_jobId).getIdAsLong());
        }

        return result;
    }

    // Recalculates the pagination Info.
    private PaginateInfo getPaginateInfo(EditorState p_state, List<Tuv> p_tuvs,
            Map<String, List<Tuv>> p_filterResult)
    {
        PaginateInfo pi = p_state.getPaginateInfo();
        List<Tuv> tuvs = p_tuvs;
        if (p_filterResult != null
                && p_filterResult.get(SegmentFilter.KEY_SOURCE) != null)
        {
            tuvs = (List<Tuv>) p_filterResult.get(SegmentFilter.KEY_SOURCE);
        }
        
        if (pi.getTotalSegmentNum() != tuvs.size())
        {
            pi = new PaginateInfo(tuvs.size(), pi.getSegmentNumPerPage(), 1);
            p_state.setPaginateInfo(pi);
        }
        
        return pi;
    }
    
    private String getInContextTargetDisplayHtml(Tuv p_srcTuv, Tuv p_targetTuv,
            RenderingOptions p_options,
            TermLeverageMatchResultSet p_termLMResultSet,
            Vector p_excludedItemTypes, TargetPage p_targetPage,
            MatchTypeStatistics p_matchTypes, Collection p_imageMaps,
            ArrayList p_comments, SegmentRepetitions p_repetitions,
            HashMap p_searchMap, EditorState p_editorState)
            throws PermissionException, RemoteException
    {
        long jobId = p_targetPage.getSourcePage().getJobId();
        p_options.setTmProfile(p_targetPage.getSourcePage().getRequest()
                .getJob().getL10nProfile().getTranslationMemoryProfile());

        Tu tu = p_targetTuv.getTu(jobId);
        long tuId = tu.getTuId();
        long tuvId = p_targetTuv.getId();
        String dataType = p_targetTuv.getDataType(jobId);
        GxmlElement elem = p_targetTuv.getGxmlElement();

        boolean reviewMode = p_options.getUiMode() == UIConstants.UIMODE_REVIEW;
        boolean reviewReadOnly = p_options.getUiMode() == UIConstants.UIMODE_REVIEW_READ_ONLY;
        boolean unlock = p_options.getEditMode() == EDITMODE_EDIT_ALL;
        // Localizables carry their own type attribute, whereas
        // segments inherit it from their parent translatable.
        String itemType = tu.getTuType();

        boolean isExcluded = SegmentProtectionManager.isTuvExcluded(elem,
                itemType, p_excludedItemTypes);

        // HTML class attribute that colors the segment in the editor
        String style = getMatchStyle(p_matchTypes, p_srcTuv, p_targetTuv,
                DUMMY_SUBID, isExcluded, unlock, p_repetitions, jobId);
        // For "localized" segment,if target is same with source,commonly
        // display as "no match" in blue color,for segment with sub,display
        // according to its LMs.
        String segment = GxmlUtil.getDisplayHtml(p_targetTuv.getGxmlElement(),
                dataType, p_options.getViewMode());
        String segmentSrc = GxmlUtil.getDisplayHtml(p_srcTuv.getGxmlElement(),
                dataType, p_options.getViewMode());

        boolean isReadOnly;
        if (STYLE_UPDATED.equals(style))
        {
            if (segment.trim().equals(segmentSrc.trim()))
            {
                List subFlows = p_targetTuv.getSubflowsAsGxmlElements();
                if (subFlows != null && subFlows.size() > 0)
                {
                    style = getMatchStyleByLM(p_matchTypes, p_srcTuv,
                            p_targetTuv, DUMMY_SUBID, unlock, p_repetitions,
                            jobId);
                }
                else
                {
                    style = STYLE_NO_MATCH;
                }
            }

            isReadOnly = false;
        }
        else
        {
            if (unlock)
            {
                style = STYLE_CONTEXT_UNLOCK;
            }
            else
            {
                style = STYLE_CONTEXT;
            }
            isReadOnly = !unlock;
        }

        if (!PageHandler.isInContextMatch(p_targetPage.getSourcePage()
                .getRequest().getJob()))
        {
            style = STYLE_EXACT_MATCH;
            isReadOnly = p_options.getEditMode() == EDITMODE_READ_ONLY
                    || (p_options.getEditMode() == EDITMODE_DEFAULT && EditHelper
                            .isTuvInProtectedState(p_targetTuv, jobId));
        }

        // Get the target page locale so we can set the DIR attribute
        // for right-to-left languages such as Hebrew and Arabic
        boolean b_rtlLocale = EditUtil.isRTLLocale(p_targetPage
                .getGlobalSightLocale());
        String dir = "";

        // Make the segment RTL if it's 1) Translatable 2) In an RTL
        // language and 3) it has bidi characters in it.
        if (b_rtlLocale && !p_targetTuv.isLocalizable(jobId)
                && Text.containsBidiChar(p_targetTuv.getGxml()))
        {
            dir = " DIR=rtl";
        }

        boolean isHighLight = isHighLight(p_searchMap, p_srcTuv, p_targetTuv);

        StringBuffer result = new StringBuffer(256);
        switch (p_options.getViewMode())
        {
            case VIEWMODE_LIST:
                // add javascript to synchronize scroll bars
                // by segment id in the pop-up editor
                if (isHighLight)
                {
                    result.append("<TD bgColor=\"yellow\" ID=seg");
                }
                else
                {
                    result.append("<TD ID=seg");
                }
                result.append(tuId);
                result.append(">");
                result.append("<Script Language=\"JavaScript\">");
                result.append("update_tr(\"");
                result.append("seg");
                result.append(tuId);
                result.append("\");");
                result.append("</Script>");

                result.append(tuId);
                result.append("</TD>\n");
                // Append "TD" for "Find Repeated Segments".
                if (p_editorState.getNeedFindRepeatedSegments())
                {
                    result.append(appendRepetitionTD(p_targetTuv));
                }

                if (reviewMode || reviewReadOnly)
                {
                    result.append("<TD>");
                    result.append(getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                            p_comments));
                    result.append("</TD>\n");

                    segment = highlightTerms(p_srcTuv, p_targetTuv, segment,
                            p_termLMResultSet, p_options.getViewMode());
                }

                if (isHighLight)
                {
                    result.append("<TD bgColor=\"yellow\" ID=seg");
                }
                else
                {
                    result.append("<TD ID=seg");
                }

                result.append(tuId);
                result.append('_');
                result.append(tuvId);
                result.append("_0");

                List subflows = p_targetTuv.getSubflowsAsGxmlElements(true);
                boolean b_subflows = (subflows != null && subflows.size() > 0);
                // b_subflows = hasSubflows(subflows);
                if (!b_subflows)
                {
                    result.append(dir);
                }

                result.append('>');

                segment = SegmentProtectionManager.handlePreserveWhiteSpace(
                        elem, segment, null, null);
                String seg = getEditorSegment(p_targetTuv,
                        EditorConstants.PTAGS_COMPACT, segment,
                        p_editorState.getNeedShowPTags(), jobId);
                if (!b_subflows) // No subflows
                {
                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnly || isExcluded))
                    {
                        result.append(getNonEditableCell(style, seg));
                    }
                    else
                    {
                        result.append(getEditableCell(style, tuId, tuvId,
                                DUMMY_SUBID, seg, false));
                    }
                }
                else
                // Subflows
                {
                    result.append("<TABLE WIDTH=100% CELLSPACING=0");
                    result.append(" CELLPADDING=2>\n");
                    result.append("<COL WIDTH=1%  VALIGN=TOP CLASS=editorId>");
                    if (reviewMode || reviewReadOnly)
                    {
                        result.append("<COL WIDTH=1%  VALIGN=TOP>");
                    }
                    result.append("<COL WIDTH=99% VALIGN=TOP>\n");
                    result.append("<TR><TD COLSPAN=");
                    result.append((reviewMode || reviewReadOnly) ? '3' : '2');
                    result.append(dir);
                    result.append('>');

                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnly || isExcluded))
                    {
                        result.append(getNonEditableCell(style, seg));
                    }
                    else
                    {
                        result.append(getEditableCell(style, tuId, tuvId,
                                DUMMY_SUBID, seg, false));
                    }

                    result.append("</TD></TR>\n");

                    // now process each subflow
                    List subflowsSRC = p_srcTuv.getSubflowsAsGxmlElements(true);
                    // hasSubflows(subflows);
                    // hasSubflows(subflowsSRC);
                    for (int i = 0; i < subflows.size(); i++)
                    {
                        GxmlElement subElmt = (GxmlElement) subflows.get(i);
                        GxmlElement subElmtSrc = (GxmlElement) subflowsSRC
                                .get(i);
                        String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
                        dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

                        // Inherit datatype from parent element...
                        if (dataType == null)
                        {
                            GxmlElement node = subElmt.getParent();

                            while (dataType == null && node != null)
                            {
                                dataType = node
                                        .getAttribute(GxmlNames.SUB_DATATYPE);
                                node = node.getParent();
                            }
                        }

                        // ... or from document if tuv inherits it.
                        if (dataType == null)
                        {
                            dataType = p_targetTuv.getDataType(jobId);
                        }

                        if (b_rtlLocale
                                && isTranslatableSub(subElmt)
                                && Text.containsBidiChar(subElmt.getTextValue()))
                        {
                            dir = " DIR=rtl";
                        }
                        else
                        {
                            dir = "";
                        }

                        isExcluded = SegmentProtectionManager.isTuvExcluded(
                                subElmt, itemType, p_excludedItemTypes);

                        // style = getMatchStyle(p_matchTypes, p_srcTuv,
                        // p_targetTuv,
                        // subId, isExcluded, unlock, p_repetitions);
                        style = STYLE_CONTEXT;
                        boolean isSubReadOnly = isReadOnly;
                        // if(!havePermission){
                        // style = STYLE_EXACT_MATCH;
                        // }
                        if (p_options.getTmProfile() != null
                                && !p_options.getTmProfile()
                                        .getIsContextMatchLeveraging())
                        {
                            style = STYLE_EXACT_MATCH;
                            isSubReadOnly = false;
                        }
                        segment = GxmlUtil.getDisplayHtml(subElmt, dataType,
                                p_options.getViewMode());

                        segmentSrc = GxmlUtil.getDisplayHtml(subElmtSrc,
                                dataType, p_options.getViewMode());
                        if (STYLE_UPDATED.equals(style)
                                && segment.trim().equals(segmentSrc.trim()))
                        {
                            style = STYLE_NO_MATCH;
                            isSubReadOnly = false;
                        }

                        result.append("<TR>");
                        result.append(getSubIdColumn(tuId, subId));

                        if (reviewMode || reviewReadOnly)
                        {
                            result.append("<TD>");
                            result.append(getCommentIcon(tuId, tuvId, subId,
                                    p_comments));
                            result.append("</TD>\n");

                            segment = highlightTerms(p_srcTuv, p_targetTuv,
                                    segment, p_termLMResultSet,
                                    p_options.getViewMode());
                        }

                        // If the TUV is read-only, or the sub is
                        // excluded, don't show the sub as editable.
                        if ((!reviewMode || reviewReadOnly)
                                && (isSubReadOnly || isExcluded))
                        {
                            result.append("<TD");
                            result.append(dir);
                            result.append('>');
                            result.append(getNonEditableCell(style, segment));
                            result.append("</TD>");
                        }
                        else
                        {
                            result.append("<TD");
                            result.append(dir);
                            result.append(" ID=seg");
                            result.append(tuId);
                            result.append('_');
                            result.append(tuvId);
                            result.append('_');
                            result.append(subId);
                            result.append('>');
                            result.append(getEditableCell(style, tuId, tuvId,
                                    subId, segment, false));
                            result.append("</TD>");
                        }

                        result.append("</TR>\n");
                    }

                    result.append("</TABLE>\n");
                }

                result.append("</TD>\n");
                break;

            case VIEWMODE_TEXT:
                if ((!reviewMode || reviewReadOnly)
                        && (isReadOnly || isExcluded))
                {
                    // Bug alert: this makes internal tags have the same
                    // color (blue) as the segment.
                    segment = p_targetTuv.getGxmlElement().getTotalTextValue();
                    segment = EditUtil.encodeHtmlEntities(segment);

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    result.append("<SPAN");
                    result.append(dir);
                    result.append(" ID=seg");
                    result.append(tuId);
                    result.append('_');
                    result.append(tuvId);
                    result.append("_0");
                    result.append('>');

                    // This makes the non-editable segment still have a color.
                    result.append(getNonEditableCell(style, segment));

                    result.append("</SPAN>");
                }
                else
                {
                    segment = GxmlUtil.getDisplayHtmlForText(elem, dataType,
                            tuId, tuvId, style, p_excludedItemTypes);

                    if (reviewMode || reviewReadOnly)
                    {
                        result.append(getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                                p_comments));

                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    result.append("<SPAN");
                    result.append(dir);
                    result.append(" ID=seg");
                    result.append(tuId);
                    result.append('_');
                    result.append(tuvId);
                    result.append("_0");
                    result.append('>');
                    result.append(segment);
                    result.append("</SPAN>");
                }

                break;

            case VIEWMODE_PREVIEW: // fall through
            default:
                // If this is a visible segment, make it a link if not
                // excluded; else output it as is.

                if ((elem.getType() == GxmlElement.SEGMENT)
                        && itemType.equals("text"))
                {
                    segment = GxmlUtil.getDisplayHtmlForPreview(elem, dataType,
                            p_targetPage, p_imageMaps,
                            p_targetTuv.getIdAsLong());

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnly || isExcluded))
                    {
                        result.append(getNonEditableCellForPreview(style,
                                segment, dir));
                    }
                    else
                    {
                        if (reviewMode || reviewReadOnly)
                        {
                            result.append(getCommentIcon(tuId, tuvId,
                                    DUMMY_SUBID, p_comments));
                        }

                        result.append(getEditableCellForPreview(style, tuId,
                                tuvId, DUMMY_SUBID, segment, true, dir));
                    }
                }
                else
                {
                    segment = GxmlUtil.getDisplayHtmlForPreview(elem, dataType,
                            p_targetPage, p_imageMaps,
                            p_targetTuv.getIdAsLong());

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    if (EditUtil.isHtmlDerivedFormat(dataType)
                            || dataType.equals("plaintext"))
                    {
                        segment = EditUtil.encodeHtmlEntities(segment);
                    }
                    else if (dataType.equals("javascript"))
                    {
                        segment = EditUtil.toJavascript(segment);
                    }

                    result.append(segment);
                }

                break;
        }

        return result.toString();
    }

    private String appendRepetitionTD(Tuv p_trgTuv)
    {
        StringBuffer result = new StringBuffer();

        if (p_trgTuv.isRepeated())
        {
            result.append("<TD bgColor=\"").append(UIConstants.COLOR_REPEATED)
                    .append("\" >");
        }
        else if (p_trgTuv.getRepetitionOfId() > 0)
        {
            result.append("<TD bgColor=\"")
                    .append(UIConstants.COLOR_REPETITION).append("\" >");
        }
        else
        {
            result.append("<TD>");
        }
        result.append("</TD>\n");

        return result.toString();
    }
    
    public SegmentView getSegmentView(long p_tuId, long p_tuvId,
            String p_subId, long p_trgPageId, long p_sourceLocaleId,
            long p_targetLocaleId, String[] p_tmNames, String p_termbase)
            throws OnlineEditorException, RemoteException
    {
        return getSegmentView(p_tuId, p_tuvId, p_subId, p_trgPageId,
                p_sourceLocaleId, p_targetLocaleId, p_tmNames, p_termbase, true);
    }

    /**
     * <p>
     * Returns HTML formatted output for segment editor. The output is wrapped
     * in a SegmentView object.
     * </p>
     * 
     * @param p_tuId
     *            - The tu ID of the segment being edited.
     * @param p_tuvId
     *            - The tuv ID of the segment being edited.
     * @param p_subId
     *            - The sub ID of the segment being edited.
     * @param p_targetPageId
     *            - The target page ID in which the TUV occurs.
     * @param p_sourceLocaleId
     *            - The source locale ID of the segment being edited.
     * @param p_targetLocaleId
     *            - The target locale ID of the segment being edited.
     * 
     * @return SegmentView object
     * 
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                - Network related exception.
     * @see SegmentView
     */
    public SegmentView getSegmentView(long p_tuId, long p_tuvId,
            String p_subId, long p_trgPageId, long p_sourceLocaleId,
            long p_targetLocaleId, String[] p_tmNames, String p_termbase,
            boolean isTarget)
            throws OnlineEditorException, RemoteException
    {
        SegmentView result = new SegmentView();

        String dataType;
        String itemType;
        boolean isLocalizable;

        Tuv sourceTuv = null;
        Tuv targetTuv = null;

        if (p_subId == null)
        {
            p_subId = DUMMY_SUBID;
        }

        TargetPage tp = getTargetPage(p_trgPageId);
        result.setPagePath(tp.getSourcePage().getExternalPageId());
        long jobId = tp.getSourcePage().getJobId();

        try
        {
        	result.setSubId(new Long(p_subId));
        	result.setTargetLocaleId(p_targetLocaleId);
        	
        	if (isTarget)
        	{
        	    targetTuv = m_tuvManager.getTuvForSegmentEditor(p_tuvId, jobId);
        	    
        	    String mergeState = targetTuv.getMergeState();
                if (mergeState.equals(Tuv.NOT_MERGED))
                {
                    sourceTuv = m_tuvManager.getTuvForSegmentEditor(p_tuId,
                            p_sourceLocaleId, jobId);
                }
                else
                {
                    sourceTuv = getMergedSourceTuvByTargetId(p_tuvId);
                }
        	}
        	else
            {
                sourceTuv = m_tuvManager.getTuvForSegmentEditor(p_tuvId, jobId);
                targetTuv = m_tuvManager.getTuvForSegmentEditor(p_tuId,
                        p_targetLocaleId, jobId);
            }

            Set<XliffAlt> xliffAltSet = targetTuv.getXliffAlt(true);
            if (xliffAltSet != null && !xliffAltSet.isEmpty())
            {
                for (Iterator iterator = xliffAltSet.iterator(); iterator
                        .hasNext();)
                {
                    XliffAlt xliffAlt = (XliffAlt) iterator.next();
                    String s = xliffAlt.getSourceSegment();
                    String t = xliffAlt.getSegment();
                    
                    String s2 = XLIFFStandardUtil.convertToTmx(s);
                    String t2 = XLIFFStandardUtil.convertToTmx(t);
                    
                    xliffAlt.setSegment(t2);
                    xliffAlt.setSourceSegment(s2);
                }
                
                result.setXliffAlt(xliffAltSet);
            }

            

            boolean isWSXlf = false;
            boolean isAutoCommit = false;
            if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(targetTuv.getTu(
                    jobId).getGenerateFrom()))
            {
                isWSXlf = true;
            }
            if (targetTuv.getLastModifiedUser() != null
                    && targetTuv.getLastModifiedUser().indexOf("_MT") > -1)
            // && targetTuv.getState().getValue() == TuvState.LOCALIZED
            // .getValue())
            {
                isAutoCommit = true;
            }
            TuvImpl cloneTargetTuv = new TuvImpl((TuvImpl) targetTuv);
            cloneTargetTuv.setId(targetTuv.getId());
            if (isWSXlf && isAutoCommit)
            {
                cloneTargetTuv.setGxml(sourceTuv.getGxml());
                result.setTargetTuv(cloneTargetTuv);
                cloneTargetTuv.setLastModifiedUser(null);
                cloneTargetTuv.setState(TuvState.NOT_LOCALIZED);
            }
            else
            {
                result.setTargetTuv(targetTuv);
            }

            // add the source segment as TMX string
            if (p_subId.equals(DUMMY_SUBID))
            {
                result.setSourceSegment(sourceTuv.getGxmlElement());
            }
            else
            {
                GxmlElement sub = sourceTuv.getSubflowAsGxmlElement(p_subId);
                result.setSourceSegment(sub);
            }

            // then add the target segment as TMX string
            // targetTuv = m_tuvManager.getTuvForSegmentEditor(p_tuId,
            // p_targetLocaleId);

            if (p_subId.equals(DUMMY_SUBID))
            {
                if (isWSXlf && isAutoCommit)
                {
                    result.setTargetSegment(cloneTargetTuv.getGxmlElement());
                }
                else
                {
                    result.setTargetSegment(targetTuv.getGxmlElement());
                }

                isLocalizable = sourceTuv.getTu(jobId).isLocalizable();
                itemType = targetTuv.getTu(jobId).getTuType();
                dataType = targetTuv.getDataType(jobId);

                // Note: only top-level segments have previous versions.

                // add the terminology match results
                result = addTerminologyMatches(result, sourceTuv, targetTuv);

                // add versions of this segment in previous tasks
                result = addSegmentVersions(result, targetTuv, p_subId, jobId);
            }
            else
            {
                GxmlElement sub = targetTuv.getSubflowAsGxmlElement(p_subId);
                result.setTargetSegment(sub);

                isLocalizable = !isTranslatableSub(sub);
                itemType = sub.getAttribute(GxmlNames.SUB_TYPE);
                dataType = sub.getAttribute(GxmlNames.SUB_DATATYPE);

                // Inherit datatype from parent element...
                if (dataType == null)
                {
                    GxmlElement node = sub.getParent();

                    while (dataType == null && node != null)
                    {
                        dataType = node.getAttribute(GxmlNames.SUB_DATATYPE);
                        node = node.getParent();
                    }
                }

                // ... or from document if tuv inherits it.
                if (dataType == null)
                {
                    dataType = targetTuv.getDataType(jobId);
                }
            }

            // Add the match results for top-level and sub segments
            result = addSegmentMatches(result, sourceTuv, targetTuv, p_subId,
                    p_tmNames);

            // Set the segment's or sub's data format and item type
            result.setDataType(dataType);
            result.setItemType(itemType);
            result.setIsLocalizable(isLocalizable);

            int wcount = sourceTuv.getWordCount();
            if (wcount >= 0)
            {
                result.setWordCount(sourceTuv.getWordCount());
            }

            if (ImageHelper.isImageItemType(itemType))
            {
                TargetPage page = getTargetPage(p_trgPageId);

                addImageInfo(result, page, p_tuvId, p_subId);
            }
        }
        catch (Exception ge)
        {
            String[] args =
            { "Failed to retrieve source or target tuv." };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_SEGMENTVIEW, args,
                    ge);
        }

        return result;
    }

    /**
     * Helper function that retrieves an existing image map for a image url and
     * sets the url to be displayed for the image in the browser.
     */
    private void addImageInfo(SegmentView p_result, TargetPage p_page,
            long p_tuvId, String p_subId) throws GeneralException,
            RemoteException
    {
        String trgUrl;

        // Assume this an extracted file
        ExtractedFile ef = getExtractedFile(p_page);
        String extUrl = ef.getExternalBaseHref();

		String srcUrl = ImageHelper.getDisplayImageUrl(p_result
				.getSourceSegment().getTotalTextValue(),
				WebAppConstants.VIRTUALDIR_CXEDOCS, ef.getInternalBaseHref(),
				extUrl);

        long subId = Long.parseLong(p_subId);

        ArrayList maps = getImageMaps(p_page);
        ImageReplaceFileMap map = GxmlUtil.getImageMap(maps, new Long(p_tuvId),
                new Long(subId));

        if (map == null)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Image has NOT been updated.");
            }

            trgUrl = ImageHelper.getDisplayImageUrl(p_result.getTargetSegment()
                    .getTotalTextValue(), WebAppConstants.VIRTUALDIR_CXEDOCS,
                    ef.getInternalBaseHref(), extUrl
            // GSDEF00010714: this breaks the online editor.
            // ef.getExternalBaseHref().substring(0,
            // ef.getExternalBaseHref().lastIndexOf("/"))
                    );
        }
        else
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Image has been updated:" + " temp=`"
                        + map.getTempSourceName() + "'" + " real=`"
                        + map.getRealSourceName() + "'");
            }

            // An image has been uploaded before. Editor must show
            // uploaded file on disk.
            trgUrl = ImageHelper.getDisplayImageUrl(map.getTempSourceName(),
                    WebAppConstants.VIRTUALDIR_IMAGE_REPLACE, "", "");
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Source=`"
                    + p_result.getSourceSegment().getTotalTextValue() + "'\n"
                    + "Target=`"
                    + p_result.getTargetSegment().getTotalTextValue() + "'\n"
                    + "source url=`" + srcUrl + "'\n" + "target url=" + trgUrl
                    + "'");
        }

        p_result.setImageMapExists(map != null);
        p_result.setSourceImageUrl(srcUrl);
        p_result.setTargetImageUrl(trgUrl);
    }

    /**
     * <p>
     * Helper method for {@see #getSegmentView(long,String,long,long)
     * getSegmentView()} that adds the TM matches for the current source segment
     * to the SegmentView.
     * </p>
     */
    private SegmentView addSegmentMatches(SegmentView p_view, Tuv p_srcTuv,
            Tuv p_targetTuv, String p_subId, String[] p_tmNames)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            // find matches created at import time

            // Returns all the fuzzy matches for the given
            // sourceTuvId. If it finds DEMOTED_EXACT_MATCH or
            // FUZZY_MATCH in LEVERAGE_MATCH table, it returns all
            // the matches it found. If it doesn't find fuzzy
            // matches, it means that the exact match for the
            // segment has already been copied into the target
            // TUV, or there is no match for the segment.
            long srcPageId = getCurrentSourcePage().getId();
            GlobalSightLocale p_targetLocale = p_targetTuv
                    .getGlobalSightLocale();
            SourcePage sourcePage = getSourcePage(srcPageId);
            long jobId = sourcePage.getJobId();

            LeverageOptions leverageOptions = m_inprogressTmManager
                    .getLeverageOptions(sourcePage, p_targetLocale);

            boolean isTmProcedence = leverageOptions.isTmProcedence();

            // Find matches created at import time
            SortedSet<LeverageMatch> staticMatches = m_lingManager
                    .getTuvMatches(p_srcTuv.getIdAsLong(), p_targetTuv
                            .getGlobalSightLocale().getIdAsLong(), p_subId,
                            isTmProcedence, jobId, null);

            // Re-leverage from in-progress and gold TMs.
            DynamicLeverageResults dynamicMatches;

            dynamicMatches = m_inprogressTmManager.leverage(p_srcTuv, p_subId,
                    p_targetTuv.getGlobalSightLocale(), srcPageId);

            dynamicMatches.setLeverageOptions(leverageOptions);
            // Should remove duplicate matches...
            dynamicMatches.mergeWithPreLeverage(staticMatches, isTmProcedence,
                    jobId);

            ArrayList<SegmentMatchResult> matchResults = new ArrayList<SegmentMatchResult>();

            for (int i = 0, max = dynamicMatches.size(); i < max
                    && i < leverageOptions.getNumberOfMatchesReturned(); i++)
            {
                DynamicLeveragedSegment match = dynamicMatches.get(i);

                String matchedSource = match.getMatchedSourceText();
                String matchedTarget = match.getMatchedTargetText();
                // Possibly empty leverage_match is saved,in this case,ignore it
                // for segment editor TM matches.
                // Ideally,this should not happen.
                if (matchedSource == null || "".equals(matchedSource.trim())
                        || matchedTarget == null
                        || "".equals(matchedTarget.trim()))
                {
                    continue;
                }
                String tmName = "";

                // A weird hack to deal with the overloaded sources over these
                // TUVs. Leverage info that is coming from a real TM may have a
                // SID; things that come from automated sources (or the
                // in-progress TM) will not.
                boolean mayHaveSid = false;
                // The save TM have the highest priority : -1
                if (match.getTmIndex() == Leverager.HIGHEST_PRIORTIY)
                {
                    ProjectTM ptm = ServerProxy
                            .getProjectHandler()
                            .getProjectTMById(leverageOptions.getSaveTmId(),
                                    false);
                    if (ptm != null)
                        tmName = ptm.getName();
                }
                else if (match.getTmIndex() == Leverager.MT_PRIORITY)
                {
                    tmName = match.getMtName();
                }
                else if (match.getTmIndex() == Leverager.XLIFF_PRIORITY)
                {
                    tmName = "xliff";
                }
                else if (match.getTmIndex() == Leverager.REMOTE_TM_PRIORITY)
                {
                    tmName = "Remote TM";
                }
                else if (match.getTmIndex() == Leverager.TDA_TM_PRIORITY)
                {
                    tmName = "TDA";
                }
                else if (match.getTmIndex() == Leverager.PO_TM_PRIORITY)
                {
                    tmName = IFormatNames.FORMAT_PO.toUpperCase();
                }
                else if (match.getTmIndex() == Leverager.IN_PROGRESS_TM_PRIORITY)
                {
                    tmName = "In Progress TM";
                }
                // Normal reference TM
                else
                {
                    ProjectTM ptm = ServerProxy.getProjectHandler()
                            .getProjectTMById(match.getTmId(), false);
                    if (ptm != null)
                        tmName = ptm.getName();
                    mayHaveSid = true;
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Dynamic match `" + matchedTarget + "'");
                }

                // Root tags have not been stripped yet (new TM)
                matchedSource = GxmlUtil.stripRootTag(matchedSource);
                matchedTarget = GxmlUtil.stripRootTag(matchedTarget);

                String matchTypeName = "";
                if (match.getMatchType() != null)
                {
                    matchTypeName = match.getMatchType().getName();
                }
                else if (match.getTmIndex() == Leverager.MT_PRIORITY)// mt used
                {
                    matchTypeName = "MT_EXACT_MATCH";
                }

                SegmentMatchResult matchResult = new SegmentMatchResult(
                        match.getMatchedTuvId(), matchedTarget,
                        match.getScore(), matchTypeName, match.getTmId(),
                        tmName, mayHaveSid);
                matchResult.setMatchContentSource(matchedSource);
                matchResult.setMatchedTuvBasicInfo(match
                        .getMatchedTuvBasicInfo());
                matchResult.setMatchedTuvJobName(match.getMatchedTuvJobName());
                matchResult.setSid(match.getSid());
                matchResults.add(matchResult);
            }

            p_view.setTmMatchResults(matchResults);
        }
        catch (GeneralException ge)
        {
            // CvdL thinks we shouldn't throw an exception here. Just eat it.

            String[] args =
            { "Failed to retrieve the segments matches." };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_SEGMENTVIEW, args,
                    ge);
        }
        catch (NamingException e)
        {
            CATEGORY.error(e.getMessage(), e);
        }

        return p_view;
    }

    /**
     * Set segment matches for "SegmentView" object.This method allows to re-set
     * TM matches separately.
     */
    public SegmentView addSegmentMatches(SegmentView p_view,
            EditorState p_state, long p_tuId, long p_tuvId, long p_subId,
            long p_sourceLocaleId, long p_targetLocaleId, long p_jobId)
    {
        SegmentView segmentView = null;

        try
        {
            Tuv sourceTuv;
            Tuv targetTuv;
            targetTuv = m_tuvManager.getTuvForSegmentEditor(p_tuvId, p_jobId);

            String mergeState = targetTuv.getMergeState();
            if (mergeState.equals(Tuv.NOT_MERGED))
            {
                sourceTuv = m_tuvManager.getTuvForSegmentEditor(p_tuId,
                        p_sourceLocaleId, p_jobId);
            }
            else
            {
                sourceTuv = getMergedSourceTuvByTargetId(p_tuvId);
            }

            segmentView = addSegmentMatches(p_view, sourceTuv, targetTuv,
                    String.valueOf(p_subId), p_state.getTmNames());
        }
        catch (Exception e)
        {
            // Trying to reset TM matches,this should not crash GS,so eat it.
            String arg = "Failed to reset TM matches for segmentView.";
            CATEGORY.error(arg, e);
        }

        return segmentView;
    }

    /**
     * Helper method for getSegmentView(...) that adds the terminology matches
     * for the current source segment to the SegmentView.
     */
    private SegmentView addTerminologyMatches(SegmentView p_view, Tuv p_srcTuv,
            Tuv p_trgTuv) throws OnlineEditorException, RemoteException
    {
        try
        {
            Collection<TermLeverageMatchResult> termMatches = null;
            // getTermMatchesForSegment accepts a subid but I don't think we
            // leverage subs, so pass in 0.
            termMatches = m_termManager.getTermMatchesForSegment(
                    p_srcTuv.getId(), 0L, p_trgTuv.getGlobalSightLocale());
            p_view.setTbMatchResults(termMatches);
        }
        catch (GeneralException ge)
        {
            // CvdL thinks we shouldn't throw an exception here. Just eat it.
            String[] args =
            { "Failed to retrieve the terminology matches." };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_SEGMENTVIEW, args,
                    ge);
        }

        return p_view;
    }

    /**
     * Helper method for getSegmentView(...) that adds the terminology matches
     * for the current source segment to the SegmentView.
     */
    private SegmentView addSegmentVersions(SegmentView p_view, Tuv p_targetTuv,
            String p_subId, long p_jobId) throws OnlineEditorException,
            RemoteException
    {
        try
        {
            List taskTuvs = m_tuvManager.getPreviousTaskTuvs(
                    p_targetTuv.getId/* AsLong */(), MAX_NUM_VERSIONS);

            if (taskTuvs != null)
            {
                int size = taskTuvs.size();
                ArrayList<SegmentVersion> versions = new ArrayList<SegmentVersion>(
                        size);

                for (int i = 0; i < size; i++)
                {
                    TaskTuv taskTuv = (TaskTuv) taskTuvs.get(i);
                    Tuv tuv = taskTuv.getTuv(p_jobId);
                    GxmlElement elem;
                    String gxml, stage;

                    if (tuv == null)
                    {
                        continue;
                    }

                    if (p_subId != null && (!p_subId.equals(DUMMY_SUBID)))
                    {
                        // if it is a subflow
                        elem = tuv.getSubflowAsGxmlElement(p_subId);
                    }
                    else
                    {
                        // otherwise, it is the whole segment
                        elem = tuv.getGxmlElement();
                    }

                    gxml = elem.toGxmlExcludeTopTags();
                    stage = taskTuv.getTaskName();

                    SegmentVersion version = new SegmentVersion(gxml, stage);
                    version.setLastModifyUser(tuv.getLastModifiedUser());

                    versions.add(version);
                }

                p_view.setSegmentVersions(versions);
            }
        }
        catch (GeneralException ge)
        {
            String[] args =
            { "Failed to retrieve previous taskTuvs." };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_SEGMENTVIEW, args,
                    ge);
        }

        return p_view;
    }

    /**
     * Retrieves the PageInfo data object: page name, page format, word count,
     * total segment count.
     */
    public PageInfo getPageInfo(long p_srcPageId) throws OnlineEditorException,
            RemoteException
    {
        PageInfo result = new PageInfo();

        try
        {
            SourcePage sourcePage = getSourcePage(p_srcPageId);

            result.setPageName(sourcePage.getExternalPageId());
            result.setPageFormat(getExtractedSourceFile(sourcePage)
                    .getDataType());
            result.setDataSourceType(sourcePage.getDataSourceType());
            result.setExternalBaseHref(getExtractedSourceFile(sourcePage)
                    .getExternalBaseHref());
            result.setWordCount(sourcePage.getWordCount());

            // TODO: need to ignore the segments that are excluded.
            Collection tus = getPageTus(sourcePage);
            result.setSegmentCount(tus == null ? 0 : tus.size());
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("Failed to get page info", ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEINFO, null, ge);
        }

        return result;
    }

    /**
     * Returns a list of TU ids (Long) for a source page. Tu IDs are cached by
     * the page handlers in EditorState, don't need to be cached here.
     */
    public ArrayList<Long> getTuIdsInPage(Long p_srcPageId)
            throws OnlineEditorException, RemoteException
    {
        ArrayList<Long> result = new ArrayList<Long>();

        try
        {
            SourcePage srcPage = getSourcePage(p_srcPageId.longValue());
            Collection<TuImpl> tus = getPageTus(srcPage);

            for (Iterator<TuImpl> it = tus.iterator(); it.hasNext();)
            {
                Tu tu = (Tu) it.next();
                result.add(tu.getIdAsLong());
            }
        }
        catch (GeneralException e)
        {
            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_TUIDS, null, e);
        }

        return result;
    }

    /**
     * Returns all segment comments for the target page.
     */
    public CommentThreadView getCommentThreads(long p_trgPageId)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            return new CommentThreadView(
                    getComments(getTargetPage(p_trgPageId)));
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(p_trgPageId), "0", "0", "0", ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_COMMENTVIEW, args,
                    ex);
        }
    }

    /**
     * <p>
     * Returns the segment comments for the selected segment in a CommentView
     * object.
     * </p>
     * 
     * @param p_tuId
     *            - The tu ID of the segment being commented.
     * @param p_tuvId
     *            - The tuv ID of the segment being commented.
     * @param p_subId
     *            - The sub ID of the segment being commented.
     * 
     * @return CommentView object
     * 
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                - Network related exception.
     * @see CommentView
     */
    public CommentView getCommentView(long p_commentId, long p_trgPageId,
            long p_tuId, long p_tuvId, long p_subId)
            throws OnlineEditorException, RemoteException
    {
        CommentView result = new CommentView(p_tuId, p_tuvId, p_subId);

        try
        {
            ArrayList<Issue> temp = getComments(getTargetPage(p_trgPageId));

            // TODO: manage comment list as map organized by logical key.

            if (p_commentId > 0)
            {
                for (int i = 0, max = temp.size(); i < max; i++)
                {
                    Issue issue = (Issue) temp.get(i);

                    if (issue.getId() == p_commentId)
                    {
                        result.setComment(issue);
                        return result;
                    }
                }
            }
            else
            {
                String key = CommentHelper.makeLogicalKey(
                        getCurrentTargetPage().getId(), p_tuId, p_tuvId,
                        p_subId);

                for (int i = 0, max = temp.size(); i < max; i++)
                {
                    Issue issue = (Issue) temp.get(i);

                    if (issue.getLogicalKey().equals(key))
                    {
                        result.setComment(issue);
                        return result;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(p_trgPageId), String.valueOf(p_tuId),
                    String.valueOf(p_tuvId), String.valueOf(p_subId),
                    ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_COMMENTVIEW, args,
                    ex);
        }

        // No existing comment found, return empty CommentView for UI.
        return result;
    }

    public void createComment(long p_tuId, long p_tuvId, long p_subId,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user, boolean share,
            boolean overwrite) throws OnlineEditorException, RemoteException
    {
        try
        {
            Issue issue = m_commentManager.addIssue(Issue.TYPE_SEGMENT,
                    p_tuvId, p_title, p_priority, p_status, p_category, p_user,
                    p_comment, CommentHelper.makeLogicalKey(
                            getCurrentTargetPage().getId(), p_tuId, p_tuvId,
                            p_subId), share, overwrite);

            m_pageCache.addComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_tuId), String.valueOf(p_tuvId),
                    String.valueOf(p_subId), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_CREATE_COMMENT, args,
                    ex);
        }
    }

    public void createComment(long p_tuId, long p_tuvId, long p_subId,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            Issue issue = m_commentManager.addIssue(Issue.TYPE_SEGMENT,
                    p_tuvId, p_title, p_priority, p_status, p_category, p_user,
                    p_comment, CommentHelper.makeLogicalKey(
                            getCurrentTargetPage().getId(), p_tuId, p_tuvId,
                            p_subId));

            m_pageCache.addComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_tuId), String.valueOf(p_tuvId),
                    String.valueOf(p_subId), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_CREATE_COMMENT, args,
                    ex);
        }
    }

    public void editComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user, boolean share, boolean overwrite)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            Issue issue = p_view.getComment();

            issue = m_commentManager.editIssue(issue.getId(), p_title,
                    p_priority, p_status, p_category, p_user, p_comment, share,
                    overwrite);

            m_pageCache.updateComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_view.getTuId()),
                    String.valueOf(p_view.getTuvId()),
                    String.valueOf(p_view.getSubId()), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_COMMENT, args,
                    ex);
        }
    }

    public void editComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user) throws OnlineEditorException,
            RemoteException
    {
        try
        {
            Issue issue = p_view.getComment();

            issue = m_commentManager.editIssue(issue.getId(), p_title,
                    p_priority, p_status, p_category, p_user, p_comment);

            m_pageCache.updateComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_view.getTuId()),
                    String.valueOf(p_view.getTuvId()),
                    String.valueOf(p_view.getSubId()), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_COMMENT, args,
                    ex);
        }
    }

    public void addComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user) throws OnlineEditorException,
            RemoteException
    {
        try
        {
            Issue issue = p_view.getComment();

            issue = m_commentManager.replyToIssue(issue.getId(), p_title,
                    p_priority, p_status, p_category, p_user, p_comment);

            m_pageCache.updateComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_view.getTuId()),
                    String.valueOf(p_view.getTuvId()),
                    String.valueOf(p_view.getSubId()), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_COMMENT, args,
                    ex);
        }
    }

    public void addComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user, boolean share, boolean overwrite)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            Issue issue = p_view.getComment();

            issue = m_commentManager.replyToIssue(issue.getId(), p_title,
                    p_priority, p_status, p_category, p_user, p_comment, share,
                    overwrite);

            m_pageCache.updateComment(issue);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(getCurrentTargetPage().getId()),
                    String.valueOf(p_view.getTuId()),
                    String.valueOf(p_view.getTuvId()),
                    String.valueOf(p_view.getSubId()), ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_COMMENT, args,
                    ex);
        }
    }

    public void closeAllComment(ArrayList p_issueList, String p_user)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            for (int i = 0; i < p_issueList.size(); i++)
            {
                Issue issue = (Issue) p_issueList.get(i);
                IssueHistory history = (IssueHistory) issue.getHistory().get(0);
                m_commentManager.editIssue(issue.getId(), issue.getTitle(),
                        issue.getPriority(), Issue.STATUS_CLOSED,
                        issue.getCategory(), p_user, history.getComment());
            }
        }
        catch (Exception ex)
        {
            String[] args =
            { "0", "0", "0", "0", ex.getMessage() };

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_COMMENT, args,
                    ex);
        }
    }

    /**
     * <p>
     * Updates the target segment content after being edited. If the p_subId is
     * a valid one (greater than 0), it means the target content to be updated
     * is the subflow content of the Tuv. Otherwise, if it's 0, it means the
     * target content to be updated is the content of the Tuv.
     * </p>
     * 
     * @param p_tuId
     *            - The tu ID of the segment being edited.
     * @param p_subId
     *            - The sub ID of the segment being edited.
     * @param p_newContent
     *            - The new content of the target segment.
     * 
     * @exception OnlineEditorManagerException
     *                - thrown when the target tuv could not be retrieved or
     *                updated.
     * @exception RemoteException
     *                - Network related exception.
     */
    public void updateTUV(long p_tuvId, String p_subId, String p_newContent,
            long p_jobId) throws OnlineEditorException, RemoteException
    {
        this.updateTUV(p_tuvId, p_subId, p_newContent, null, p_jobId);
    }

    public void updateTUV(long p_tuvId, String p_subId, String p_newContent,
            String p_userId, long p_jobId) throws OnlineEditorException,
            RemoteException
    {
        Tuv sourceTuv;
        Tuv targetTuv;

        try
        {
            SourcePage sp = getCurrentSourcePage();
            targetTuv = m_tuvManager.getTuvForSegmentEditor(p_tuvId, p_jobId);
            // get source tuv
            long sourcePageId = sp.getId();
            long sourceLocaleId = sp.getGlobalSightLocale().getId();

            String mergeState = targetTuv.getMergeState();
            if (mergeState.equals(Tuv.NOT_MERGED))
            {
                long tuId = targetTuv.getTuId();
                sourceTuv = m_tuvManager.getTuvForSegmentEditor(tuId,
                        sourceLocaleId, p_jobId);
            }
            else
            {
                sourceTuv = getMergedSourceTuvByTargetId(p_tuvId);
            }
            
            String dataType = sourceTuv.getDataType(p_jobId);
            if ("xlf".equals(dataType))
            {
                // revert sub [x3] tag
                PseudoData pTagData = new PseudoData();
                boolean isVerb = "verbose".equalsIgnoreCase(editorState.getPTagFormat());
                pTagData.setMode(isVerb ? PseudoConstants.PSEUDO_VERBOSE : PseudoConstants.PSEUDO_COMPACT);
                TmxPseudo convertor = new TmxPseudo();
                pTagData.setDataType("xlf");
                pTagData.setAddables("xlf");

                String oriSource = sourceTuv.getGxmlExcludeTopTags();
                convertor.tmx2Pseudo(oriSource, pTagData);

                Vector srcTagList = pTagData.getSrcCompleteTagList();

                if (srcTagList != null && srcTagList.size() > 0)
                {
                    Hashtable map = pTagData.getPseudo2TmxMap();
                    Enumeration srcEnumerator = srcTagList.elements();
                    while (srcEnumerator.hasMoreElements())
                    {
                        TagNode srcItem = (TagNode) srcEnumerator.nextElement();
                        String tagName = PseudoConstants.PSEUDO_OPEN_TAG
                                + srcItem.getPTagName()
                                + PseudoConstants.PSEUDO_CLOSE_TAG;

                        if (p_newContent.contains(tagName))
                        {
                            String oriTag = (String) map.get(srcItem.getPTagName());
                            p_newContent = p_newContent
                                    .replace(tagName, oriTag);
                        }
                    }
                }
            }
            
            // set new content to subflow or the Tuv itself
            if (p_subId != null && (!p_subId.equals(DUMMY_SUBID)))
            {
                targetTuv.setSubflowGxml(p_subId, p_newContent);
            }
            else
            {
                targetTuv.setGxmlExcludeTopTagsIgnoreSubflows(p_newContent,
                        p_jobId);
            }
            if (p_userId != null)
            {
                targetTuv.setLastModifiedUser(p_userId);
            }

            m_tuvManager.updateTuvToLocalizedState(targetTuv, p_jobId);

            // If database update succeeded, update cached copy.
            // Do we need to reload the TUV or can we use the in-mem copy?
            // tuv = m_tuvManager.getTuvForSegmentEditor(p_tuvId);
            m_pageCache.updateTuv(targetTuv);

            // Update in-progress TM.
            try
            {
                m_inprogressTmManager.save(sourceTuv, targetTuv, p_subId,
                        sourcePageId);
            }
            catch (Throwable ignore)
            {
                CATEGORY.error("cannot update in-progress TM", ignore);
            }
        }
        catch (Exception ge)
        {
            String[] args =
            { "TuvManager failed to update the Tuv." };

            CATEGORY.error(args[0], ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_SEGMENT, args,
                    ge);
        }
    }

    /**
     * Updates an existing ImageMap for the given target page, tuv and sub, or
     * creates a new ImageMap if it doesn't exist.
     * 
     * For updates, p_tempName can be null (the image name on disk) and only
     * p_realName gets overwritten (the segment's URL).
     */
    public void createImageMap(Long p_trgPageId, long p_tuvId, long p_subId,
            String p_tempName, String p_realName) throws OnlineEditorException,
            RemoteException
    {
        try
        {
            TargetPage targetPage = getTargetPage(p_trgPageId.longValue());
            ArrayList maps = getImageMaps(targetPage);
            ImageReplaceFileMap map = GxmlUtil.getImageMap(maps, new Long(
                    p_tuvId), new Long(p_subId));

            if (map != null)
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("updating image map\n" + "temp=`"
                            + p_tempName + "'\n" + "real=`" + p_realName + "'");
                }

                if (p_tempName != null)
                {
                    map.setTempSourceName(p_tempName);
                }

                if (p_realName == null && p_tempName == null)
                {
                    map.setTempSourceName(null);
                    map.setRealSourceName(null);
                    // remove the image from the map
                    // it has been replaced with text
                    // since what is passed are NULLs
                    m_imageManager.deleteImageReplaceFileMap(map);
                }
                else
                {
                    map.setRealSourceName(p_realName);
                    m_imageManager.updateImageReplaceFileMap(map);
                }
            }
            else
            {
                // the map couldn't be found.

                // no image to add so just return
                if (p_realName == null && p_tempName == null)
                {
                    return;
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("creating NEW image map\n" + "temp=`"
                            + p_tempName + "'\n" + "real=`" + p_realName + "'");
                }

                map = new ImageReplaceFileMap(p_trgPageId, p_tuvId, p_subId,
                        p_realName, p_tempName);

                m_imageManager.createImageReplaceFileMap(map);

                // object ID newly assigned, reload the object
                map = m_imageManager.getImageReplaceFileMap(p_trgPageId,
                        p_tuvId, p_subId);
            }

            m_pageCache.updateImageMap(map);
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("Failed to create or update image map", ge);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_UPDATE_IMAGE_MAP, null,
                    ge);
        }
    }

    /**
     * Returns a set of TU ids that are part of the page, i.e. ones that have
     * not been deleted using GSA delete tags. The caller must call this method
     * only on pages that satisfy page.containsGsTags().
     * 
     * Called from Segment Editor. Offline mode uses its own copy of this code.
     */
    public HashSet getInterpretedTuIds(long p_srcPageId,
            GlobalSightLocale p_locale) throws OnlineEditorException,
            RemoteException
    {
        try
        {
            return getInterpretedTuIds_1(p_srcPageId, p_locale);
        }
        catch (GeneralException e)
        {
            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_TUIDS, null, e);
        }
    }

    /**
     * Splits a range of merged segments at the "top" or "bottom". The split-off
     * segment is restored to its source. The remaining merged segments are
     * re-merged from their source to produce correct sub ids. Any existing
     * translations are lost.
     */
    public void splitSegments(long p_tuv1, long p_tuv2, String p_location,
            long p_jobId) throws OnlineEditorException, RemoteException
    {
        try
        {
            ArrayList<Tuv> splits = new ArrayList<Tuv>();
            ArrayList<Tuv> sourceSplits = new ArrayList<Tuv>();

            List<Tuv> sourceTuvs = getPageTuvs(getCurrentSourcePage());
            List targetTuvs = getPageTuvs(getCurrentTargetPage());

            // Collect TUVs to split: all from tuv1 to tuv2.
            boolean found = false;
            for (int i = 0, max = targetTuvs.size(); i < max; i++)
            {
                Tuv tuv = (Tuv) targetTuvs.get(i);

                if (tuv.getId() == p_tuv1)
                {
                    found = true;
                }

                if (found)
                {
                    splits.add(tuv);
                    sourceSplits.add(sourceTuvs.get(i));
                }

                if (tuv.getId() == p_tuv2)
                {
                    break;
                }
            }

            int start, end;

            // Split off the first or last TUV by setting merge state.
            if (p_location.equals("top"))
            {
                Tuv sourceTuv = (Tuv) sourceSplits.get(0);
                Tuv targetTuv = (Tuv) splits.get(0);
                targetTuv.setMergeState(Tuv.NOT_MERGED);
                targetTuv.setGxml(sourceTuv.getGxml());

                start = 1;
                end = splits.size();
            }
            else
            // if (p_location.equals("bottom"))
            {
                Tuv sourceTuv = (Tuv) sourceSplits.get(splits.size() - 1);
                Tuv targetTuv = (Tuv) splits.get(splits.size() - 1);
                targetTuv.setMergeState(Tuv.NOT_MERGED);
                targetTuv.setGxml(sourceTuv.getGxml());

                start = 0;
                end = splits.size() - 1;
            }

            // Restore source of the remaining TUVs and remerge.
            for (int i = start; i < end; i++)
            {
                Tuv sourceTuv = (Tuv) sourceSplits.get(i);
                Tuv targetTuv = (Tuv) splits.get(i);

                targetTuv.setGxml(sourceTuv.getGxml());
            }

            TuvMerger.mergeTuvs(splits.subList(start, end));

            // Update merge states
            if (start == end - 1)
            {
                ((Tuv) splits.get(start)).setMergeState(Tuv.NOT_MERGED);
            }
            else
            {
                ((Tuv) splits.get(start)).setMergeState(Tuv.MERGE_START);
                for (int i = start + 1, max = end - 1; i < max; i++)
                {
                    ((Tuv) splits.get(i)).setMergeState(Tuv.MERGE_MIDDLE);
                }
                ((Tuv) splits.get(end - 1)).setMergeState(Tuv.MERGE_END);
            }

            // Save tuvs and update page cache.
            for (int i = 0, max = splits.size(); i < max; i++)
            {
                Tuv tuv = (Tuv) splits.get(i);

                // TODO: the cached TUVs must be cloned before being
                // modified, then updated atomically in DB; only if
                // the update succeeds can the cache be updated.
                m_tuvManager.updateTuvToLocalizedState(tuv, p_jobId);
                m_pageCache.updateTuv(tuv);
            }
        }
        catch (/* General */Exception e)
        {
            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_SPLIT, null, e);
        }
    }

    /**
     * Merges a range of segments. The first segment receives the combined
     * target strings, all others will be set to empty.
     */
    public void mergeSegments(long p_tuv1, long p_tuv2, long p_jobId)
            throws OnlineEditorException, RemoteException
    {
        try
        {
            List<Tuv> merges = new ArrayList<Tuv>();
            List<Tuv> tuvs = getPageTuvs(getCurrentTargetPage());

            // Collect TUVs to merge: all from tuv1 to tuv2.
            boolean found = false;
            for (int i = 0, max = tuvs.size(); i < max; i++)
            {
                Tuv tuv = (Tuv) tuvs.get(i);

                if (tuv.getId() == p_tuv1)
                {
                    found = true;
                }

                if (found)
                {
                    merges.add(tuv);
                }

                if (tuv.getId() == p_tuv2)
                {
                    break;
                }
            }

            // First TUV now holds combined target text, all others are empty.
            TuvMerger.mergeTuvs(merges);

            ((Tuv) merges.get(0)).setMergeState(Tuv.MERGE_START);
            for (int i = 1, max = merges.size() - 1; i < max; i++)
            {
                ((Tuv) merges.get(i)).setMergeState(Tuv.MERGE_MIDDLE);
            }
            ((Tuv) merges.get(merges.size() - 1)).setMergeState(Tuv.MERGE_END);

            // Save tuvs and update page cache.
            for (int i = 0, max = merges.size(); i < max; i++)
            {
                Tuv tuv = (Tuv) merges.get(i);

                // TODO: the cached TUVs must be cloned before being
                // modified, then updated atomically in DB; only if
                // the update succeeds can the cache be updated.
                m_tuvManager.updateTuvToLocalizedState(tuv, p_jobId);
                m_pageCache.updateTuv(tuv);
            }
        }
        catch (Exception ex)
        {
            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_MERGE, null, ex);
        }
    }

    // Webex proposed reviewer view of term memory,
    // pass p_termbaseName for leveraging on which we
    // hightlight terms when reivewer reivews.
    /**
     * <p>
     * Returns the formatted Html display string for a Tuv. If the Tuv or a
     * subflow is excluded by types or p_excludedItemTypes, it is made readOnly.
     * </p>
     */
    private String getTargetDisplayHtml(Tuv p_srcTuv, Tuv p_targetTuv,
            RenderingOptions p_options,
            TermLeverageMatchResultSet p_termLMResultSet,
            Vector p_excludedItemTypes, TargetPage p_targetPage,
            MatchTypeStatistics p_matchTypes, Collection p_imageMaps,
            ArrayList p_comments, SegmentRepetitions p_repetitions,
            HashMap searchMap, EditorState p_editorState)
            throws OnlineEditorException, RemoteException
    {
        long jobId = p_targetPage.getSourcePage().getJobId();
        TuImpl tu = (TuImpl) p_targetTuv.getTu(jobId);
        long tuId = tu.getTuId();
        long tuvId = p_targetTuv.getId();
        String dataType = p_targetTuv.getDataType(jobId);
        GxmlElement elem = p_targetTuv.getGxmlElement();

        boolean reviewMode = p_options.getUiMode() == UIConstants.UIMODE_REVIEW;
        boolean reviewReadOnly = p_options.getUiMode() == UIConstants.UIMODE_REVIEW_READ_ONLY;
        boolean unlock = p_options.getEditMode() == EDITMODE_EDIT_ALL;
        // Localizables carry their own type attribute, whereas
        // segments inherit it from their parent translatable.
        String itemType = tu.getTuType();
        
        boolean isReadOnlyMode = p_options.getEditMode() == EDITMODE_READ_ONLY;
        boolean isRealExactLocalized = EditorHelper.isRealExactMatchLocalied(
                p_srcTuv, p_targetTuv, p_matchTypes, DUMMY_SUBID, jobId);
        boolean isReadOnly = isReadOnlyMode
                || (p_options.getEditMode() == EDITMODE_DEFAULT && EditHelper
                        .isTuvInProtectedState(p_targetTuv, jobId));
        boolean isExcluded = SegmentProtectionManager.isTuvExcluded(elem,
                itemType, p_excludedItemTypes);

        // HTML class attribute that colors the segment in the editor
        String style = getMatchStyle(p_matchTypes, p_srcTuv, p_targetTuv,
                DUMMY_SUBID, isExcluded, unlock, p_repetitions, jobId);

        // For "localized" segment,if target is same with source,commonly
        // display as "no match" in blue color,for segment with sub,display
        // according to its LMs.
        String segment = GxmlUtil.getDisplayHtml(p_targetTuv.getGxmlElement(),
                dataType, p_options.getViewMode());
        String segmentSrc = GxmlUtil.getDisplayHtml(p_srcTuv.getGxmlElement(),
                dataType, p_options.getViewMode());
        if (STYLE_UPDATED.equals(style)
                && segment.trim().equals(segmentSrc.trim()))
        {
            List subFlows = p_targetTuv.getSubflowsAsGxmlElements();
            if (subFlows != null && subFlows.size() > 0)
            {
                style = getMatchStyleByLM(p_matchTypes, p_srcTuv, p_targetTuv,
                        DUMMY_SUBID, unlock, p_repetitions, jobId);
            }
            else
            {
                style = STYLE_NO_MATCH;
            }
        }
        // Get the target page locale so we can set the DIR attribute
        // for right-to-left languages such as Hebrew and Arabic
        boolean b_rtlLocale = EditUtil.isRTLLocale(p_targetPage
                .getGlobalSightLocale());
        String dir = "";

        // Make the segment RTL if it's 1) Translatable 2) In an RTL
        // language and 3) it has bidi characters in it.
        if (b_rtlLocale && !p_targetTuv.isLocalizable(jobId)
                && Text.containsBidiChar(p_targetTuv.getGxml()))
        {
            dir = " DIR=rtl";
        }

        boolean isHighLight = isHighLight(searchMap, p_srcTuv, p_targetTuv);

        StringBuffer result = new StringBuffer(256);
        switch (p_options.getViewMode())
        {
            case VIEWMODE_LIST:
                // add javascript to synchronize scroll bars
                // by segment id in the pop-up editor
                if (isHighLight)
                {
                    result.append("<TD bgColor=\"yellow\" ID=seg");
                }
                else
                {
                    result.append("<TD ID=seg");
                }

                result.append(tuId);
                result.append(">");
                result.append("<Script Language=\"JavaScript\">");
                result.append("update_tr(\"");
                result.append("seg");
                result.append(tuId);
                result.append("\");");
                result.append("</Script>");

                result.append(tuId);
                result.append("</TD>\n");

                // Append extra "TD" for "Find Repeated Segments".
                if (p_editorState.getNeedFindRepeatedSegments())
                {
                    result.append(appendRepetitionTD(p_targetTuv));
                }

                // Append extra "TD" for comments
                if (reviewMode || reviewReadOnly)
                {
                    if (isHighLight)
                    {
                        result.append("<TD bgColor=\"yellow\" ");
                    }
                    else
                    {
                        result.append("<TD>");
                    }

                    result.append(getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                            p_comments));
                    result.append("</TD>\n");

                    segment = highlightTerms(p_srcTuv, p_targetTuv, segment,
                            p_termLMResultSet, p_options.getViewMode());
                }

                if (isHighLight)
                {
                    result.append("<TD bgColor=\"yellow\" ID=seg");
                }
                else
                {
                    result.append("<TD ID=seg");
                }

                result.append(tuId);
                result.append('_');
                result.append(tuvId);
                result.append("_0");

                List subflows = p_targetTuv.getSubflowsAsGxmlElements(true);
                boolean b_subflows = (subflows != null && subflows.size() > 0);
                // b_subflows = hasSubflows(subflows);
                if (!b_subflows)
                {
                    result.append(dir);
                }

                result.append('>');

                segment = SegmentProtectionManager.handlePreserveWhiteSpace(
                        elem, segment, null, null);
                String seg = getEditorSegment(p_targetTuv,
                        EditorConstants.PTAGS_COMPACT, segment,
                        p_editorState.getNeedShowPTags(), jobId);
                if (!b_subflows) // No subflows
                {
                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnly || isExcluded))
                    {
                        result.append(getNonEditableCell(style, seg));
                    }
                    else
                    {
                        result.append(getEditableCell(style, tuId, tuvId,
                                DUMMY_SUBID, seg, false));
                    }
                }
                else
                // Subflows
                {
                    result.append("<TABLE WIDTH=100% CELLSPACING=0");
                    result.append(" CELLPADDING=2>\n");
                    result.append("<COL WIDTH=1%  VALIGN=TOP CLASS=editorId>");
                    if (reviewMode || reviewReadOnly)
                    {
                        result.append("<COL WIDTH=1%  VALIGN=TOP>");
                    }
                    result.append("<COL WIDTH=99% VALIGN=TOP>\n");
                    result.append("<TR><TD COLSPAN=");
                    result.append((reviewMode || reviewReadOnly) ? '3' : '2');
                    result.append(dir);
                    result.append('>');

                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnlyMode
                                    || (isReadOnly && isRealExactLocalized) || isExcluded))
                    {
                        result.append(getNonEditableCell(style, seg));
                    }
                    else
                    {
                        result.append(getEditableCell(style, tuId, tuvId,
                                DUMMY_SUBID, seg, false));
                    }

                    result.append("</TD></TR>\n");

                    // now process each subflow
                    List subflowsSRC = p_srcTuv.getSubflowsAsGxmlElements(true);
                    // hasSubflows(subflowsSRC);
                    // hasSubflows(subflows);
                    for (int i = 0; i < subflows.size(); i++)
                    {
                        GxmlElement subElmt = (GxmlElement) subflows.get(i);
                        GxmlElement subElmtSrc = (GxmlElement) subflowsSRC
                                .get(i);
                        String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
                        dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

                        // Inherit datatype from parent element...
                        if (dataType == null)
                        {
                            GxmlElement node = subElmt.getParent();

                            while (dataType == null && node != null)
                            {
                                dataType = node
                                        .getAttribute(GxmlNames.SUB_DATATYPE);
                                node = node.getParent();
                            }
                        }

                        // ... or from document if tuv inherits it.
                        if (dataType == null)
                        {
                            dataType = p_targetTuv.getDataType(jobId);
                        }

                        if (b_rtlLocale
                                && isTranslatableSub(subElmt)
                                && Text.containsBidiChar(subElmt.getTextValue()))
                        {
                            dir = " DIR=rtl";
                        }
                        else
                        {
                            dir = "";
                        }

                        isExcluded = SegmentProtectionManager.isTuvExcluded(
                                subElmt, itemType, p_excludedItemTypes);

                        // For "localized" segment,if target is same with
                        // source,commonly display as "no match" in blue
                        // color,for segment with sub,display according to its
                        // LMs.
                        style = getMatchStyle(p_matchTypes, p_srcTuv,
                                p_targetTuv, subId, isExcluded, unlock,
                                p_repetitions, jobId);
                        segment = GxmlUtil.getDisplayHtml(subElmt, dataType,
                                p_options.getViewMode());
                        segmentSrc = GxmlUtil.getDisplayHtml(subElmtSrc,
                                dataType, p_options.getViewMode());
                        if (STYLE_UPDATED.equals(style)
                                && segment.trim().equals(segmentSrc.trim()))
                        {
                            List subFlows = p_targetTuv
                                    .getSubflowsAsGxmlElements();
                            if (subFlows != null && subFlows.size() > 0)
                            {
                                style = getMatchStyleByLM(p_matchTypes,
                                        p_srcTuv, p_targetTuv, subId, unlock,
                                        p_repetitions, jobId);
                            }
                            else
                            {
                                style = STYLE_NO_MATCH;
                            }
                        }

                        result.append("<TR>");
                        result.append(getSubIdColumn(tuId, subId));

                        if (reviewMode || reviewReadOnly)
                        {
                            result.append("<TD>");
                            result.append(getCommentIcon(tuId, tuvId, subId,
                                    p_comments));
                            result.append("</TD>\n");

                            segment = highlightTerms(p_srcTuv, p_targetTuv,
                                    segment, p_termLMResultSet,
                                    p_options.getViewMode());
                        }

                        boolean isSubExactLocalized = EditorHelper
                                .isRealExactMatchLocalied(p_srcTuv,
                                        p_targetTuv, p_matchTypes, subId, jobId);

                        // If the TUV is read-only, or the sub is
                        // excluded, don't show the sub as editable.
                        if ((!reviewMode || reviewReadOnly)
                                && (isReadOnlyMode
                                        || (isReadOnly && isSubExactLocalized) || isExcluded))
                        {
                            result.append("<TD");
                            result.append(dir);
                            result.append('>');
                            result.append(getNonEditableCell(style, segment));
                            result.append("</TD>");
                        }
                        else
                        {
                            result.append("<TD");
                            result.append(dir);
                            result.append(" ID=seg");
                            result.append(tuId);
                            result.append('_');
                            result.append(tuvId);
                            result.append('_');
                            result.append(subId);
                            result.append('>');
                            result.append(getEditableCell(style, tuId, tuvId,
                                    subId, segment, false));
                            result.append("</TD>");
                        }

                        result.append("</TR>\n");
                    }

                    result.append("</TABLE>\n");
                }

                result.append("</TD>\n");

                break;

            case VIEWMODE_TEXT:
                if ((!reviewMode || reviewReadOnly)
                        && (isReadOnly || isExcluded))
                {
                    // Bug alert: this makes internal tags have the same
                    // color (blue) as the segment.
                    segment = p_targetTuv.getGxmlElement().getTotalTextValue();
                    segment = EditUtil.encodeHtmlEntities(segment);

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    result.append("<SPAN");
                    result.append(dir);
                    result.append(" ID=seg");
                    result.append(tuId);
                    result.append('_');
                    result.append(tuvId);
                    result.append("_0");
                    result.append('>');

                    // This makes the non-editable segment still have a color.
                    result.append(getNonEditableCell(style, segment));

                    result.append("</SPAN>");
                }
                else
                {
                    segment = GxmlUtil.getDisplayHtmlForText(elem, dataType,
                            tuId, tuvId, style, p_excludedItemTypes);

                    if (reviewMode || reviewReadOnly)
                    {
                        result.append(getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                                p_comments));

                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    result.append("<SPAN");
                    result.append(dir);
                    result.append(" ID=seg");
                    result.append(tuId);
                    result.append('_');
                    result.append(tuvId);
                    result.append("_0");
                    result.append('>');
                    result.append(segment);
                    result.append("</SPAN>");
                }

                break;

            case VIEWMODE_PREVIEW: // fall through
            default:
                // If this is a visible segment, make it a link if not
                // excluded; else output it as is.

                if ((elem.getType() == GxmlElement.SEGMENT)
                        && itemType.equals("text"))
                {
                    segment = GxmlUtil.getDisplayHtmlForPreview(elem, dataType,
                            p_targetPage, p_imageMaps,
                            p_targetTuv.getIdAsLong());

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    if ((!reviewMode || reviewReadOnly)
                            && (isReadOnly || isExcluded))
                    {
                        result.append(getNonEditableCellForPreview(style,
                                segment, dir));
                    }
                    else
                    {
                        if (reviewMode || reviewReadOnly)
                        {
                            result.append(getCommentIcon(tuId, tuvId,
                                    DUMMY_SUBID, p_comments));
                        }

                        result.append(getEditableCellForPreview(style, tuId,
                                tuvId, DUMMY_SUBID, segment, true, dir));
                    }
                }
                else
                {
                    segment = GxmlUtil.getDisplayHtmlForPreview(elem, dataType,
                            p_targetPage, p_imageMaps,
                            p_targetTuv.getIdAsLong());

                    if (reviewMode || reviewReadOnly)
                    {
                        segment = highlightTerms(p_srcTuv, p_targetTuv,
                                segment, p_termLMResultSet,
                                p_options.getViewMode());
                    }

                    if (EditUtil.isHtmlDerivedFormat(dataType)
                            || dataType.equals("plaintext"))
                    {
                        segment = EditUtil.encodeHtmlEntities(segment);
                    }
                    else if (dataType.equals("javascript"))
                    {
                        segment = EditUtil.toJavascript(segment);
                    }

                    result.append(segment);
                }

                break;
        }

        return result.toString();
    }

    /**
     * Gets the segment with tags, such as the segment in segment editor.
     * 
     * @param p_targetTuv
     *            tuv data
     * @param p_ptagFlag
     *            pTag flag, such as verbose/compact
     * @param p_originalSegment
     *            original Segment without pTags
     * @param p_needShowPTags
     *            true, return the segment with tags false, return
     *            p_originalSegment
     */
    private String getEditorSegment(Tuv p_tuv, String p_ptagFlag,
            String p_originalSegment, boolean p_needShowPTags, long p_jobId)
    {
        if (!p_needShowPTags)
        {
            return p_originalSegment;
        }

        OnlineTagHelper applet = new OnlineTagHelper();
        try
        {
            String seg = GxmlUtil.getInnerXml(p_tuv.getGxmlElement());
            applet.setDataType(p_tuv.getDataType(p_jobId));
            applet.setInputSegment(seg, "", p_tuv.getDataType(p_jobId));
            if (EditorConstants.PTAGS_VERBOSE.equals(p_ptagFlag))
            {
                applet.getVerbose();
                seg = applet.makeVerboseColoredPtags(seg);
            }
            else
            {
                applet.getCompact();
                seg = applet.makeCompactColoredPtags(seg);
            }
            return seg;
        }
        catch (Exception e)
        {
            CATEGORY.info("getEditorSegment Error.", e);
        }

        return p_originalSegment;
    }

    // Webex proposed reviewer view of term memory,
    // main algorithm to highlight terms.
    private String highlightTerms(Tuv p_srcTuv, Tuv p_targetTuv,
            String p_segment, TermLeverageMatchResultSet p_termLMResultSet,
            int p_viewMode)
    {
        StringBuffer sb = null;

        if (p_termLMResultSet != null)
        {
            ArrayList<TermLeverageMatchResult> termLMResultList = p_termLMResultSet
                    .getLeverageMatches(p_srcTuv.getId(), 0L);
            if (termLMResultList != null)
            {
                sb = new StringBuffer(p_segment);
                String segmentSrc = p_srcTuv.getGxmlElement().getTextValue();
                String sourceTerm = null;
                String targetTerm = null;
                for (TermLeverageMatchResult termLMResult : termLMResultList)
                {
                    sourceTerm = termLMResult.getSourceTerm();
                    if (segmentSrc.indexOf(sourceTerm) < 0)
                    {
                        continue;
                    }
                    else
                    {
                        Iterator<Hit> it = termLMResult.getTargetHitIterator();
                        int startIndex = 0;
                        int indexInSegment = -1;
                        while(it.hasNext())
                        {
                            Hit hit = it.next();
                            targetTerm = hit.getTerm();
                            if (!StringUtil.isEmpty(targetTerm)
                                    && targetTerm.trim().length() > 1
                                    && targetTerm.indexOf("span") == -1
                                    && targetTerm.indexOf("style") == -1
                                    && targetTerm.indexOf("color") == -1
                                    && targetTerm.indexOf("chocolate") == -1)
                            {
                                while (true)
                                {
                                    indexInSegment = sb.indexOf(targetTerm,
                                            startIndex);

                                    if (indexInSegment >= 0)
                                    {
                                        // Now find a targetTerm in target
                                        // segment,
                                        // we add start tag of highlight color
                                        // before this term.
                                        sb.insert(indexInSegment,
                                                "<span style=\"color: chocolate\">");
                                        // And now add end tag of highlight
                                        // color after this term.
                                        sb.insert(
                                                indexInSegment
                                                        + "<span style=\"color: chocolate\">"
                                                                .length()
                                                        + targetTerm.length(),
                                                "</span>");
                                    }
                                    else
                                    {
                                        break;
                                    }
                                    startIndex = indexInSegment
                                            + "<span style=\"color: chocolate\">"
                                                    .length()
                                            + targetTerm.length()
                                            + "</span>".length();
                                }

                            }
                        }

                    }

                }
            }
        }

        return sb == null ? p_segment : sb.toString();
    }

    /**
     * <p>
     * Returns the formatted Html display string for a merged Tuv (for
     * subsequent tuvs, not the first one).
     * </p>
     */
    private String getTargetDisplayHtmlMerged(Tuv p_targetTuv,
            RenderingOptions p_options, List p_comments)
    {
        StringBuffer result = new StringBuffer(256);

        boolean reviewMode = p_options.getUiMode() == UIConstants.UIMODE_REVIEW;
        boolean reviewReadOnly = p_options.getUiMode() == UIConstants.UIMODE_REVIEW_READ_ONLY;

        long tuId = p_targetTuv.getTuId();
        long tuvId = p_targetTuv.getId();

        switch (p_options.getViewMode())
        {
            case VIEWMODE_LIST:
                result.append("<TD>");
                result.append(tuId);
                result.append("</TD>\n");

                if (reviewMode || reviewReadOnly)
                {
                    result.append("<TD>");
                    result.append(getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                            p_comments));
                    result.append("</TD>\n");
                }

                result.append("<TD ID=seg");
                result.append(tuId);
                result.append('_');
                result.append(tuvId);
                result.append("_0");
                result.append(" TITLE='merged segment'>");
                result.append("&nbsp;");
                result.append("</TD>\n");

                break;

            case VIEWMODE_TEXT:
                // Do not show anything, but show icon in review mode
                if (reviewMode || reviewReadOnly)
                {
                    String icon = getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                            p_comments);

                    if (icon.length() > 0)
                    {
                        result.append("<SPAN ID=seg");
                        result.append(tuId);
                        result.append('_');
                        result.append(tuvId);
                        result.append("_0");
                        result.append(" TITLE='merged segment'>");
                        result.append(icon);
                        result.append("</SPAN>");
                    }
                }
                break;

            case VIEWMODE_PREVIEW: // fall through
            default:
                // Do not show anything, but show icon in review mode
                if (reviewMode || reviewReadOnly)
                {
                    String icon = getCommentIcon(tuId, tuvId, DUMMY_SUBID,
                            p_comments);

                    if (icon.length() > 0)
                    {
                        result.append("<SPAN");
                        result.append(" ID=seg");
                        result.append(tuId);
                        result.append('_');
                        result.append(tuvId);
                        result.append("_0");
                        result.append(" TITLE='merged segment'>");
                        result.append(icon);
                        result.append("</SPAN>");
                    }
                }
                break;
        }

        return result.toString();
    }

    /**
     * <p>
     * Template-less UI mode: returns the formatted HTML display string for an
     * entire page. This is for the paragraph editor.
     * </p>
     */
    private String getTargetDisplayHtml2(List p_sourceTuvs, List p_targetTuvs,
            RenderingOptions p_options, Vector p_excludedItemTypes,
            TargetPage p_targetPage, MatchTypeStatistics p_matchTypes,
            String p_pageDataType, SegmentRepetitions p_repetitions,
            EditorState p_state, HashMap p_searchMap, Map<String, List<Tuv>> filterResult)
            throws Exception
    {
        long jobId = p_targetPage.getSourcePage().getJobId();
        StringBuffer result = new StringBuffer(256);

        long curPid = 0;
        long prevPid = 0;
        StringBuffer segmentPar = new StringBuffer();

        // Gets the Paginate info
        PaginateInfo pi = getPaginateInfo(p_state, p_sourceTuvs, filterResult);
        int segmentNumPerPage = pi.getSegmentNumPerPage();
        int currentPageNum = pi.getCurrentPageNum();
        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
        int tmpCount = 0;
        for (int i = beginIndex, max = p_sourceTuvs.size(); tmpCount < segmentNumPerPage
                && i < max; i++)
        {
            Tuv sourceTuv = (Tuv) p_sourceTuvs.get(i);
            Tuv targetTuv = (Tuv) p_targetTuvs.get(i);

            Tu tu = targetTuv.getTu(jobId);
            long tuId = tu.getTuId();
            long tuvId = targetTuv.getId();
            String dataType = targetTuv.getDataType(jobId);
            String itemType = tu.getTuType();
            boolean isLocalizable = tu.isLocalizable();
            GxmlElement srcElem = sourceTuv.getGxmlElement();
            GxmlElement trgElem = targetTuv.getGxmlElement();

            curPid = tu.getPid();

            // Skip localizables.
            if (isLocalizable)
            {
                prevPid = curPid;
                continue;
            }

            // Get the target page locale so we can set the DIR attribute
            // for right-to-left languages such as Hebrew and Arabic
            boolean b_rtlLocale = EditUtil.isRTLLocale(p_targetPage
                    .getGlobalSightLocale());
            String dir = "";

            p_options.setTmProfile(p_targetPage.getSourcePage().getRequest()
                    .getJob().getL10nProfile().getTranslationMemoryProfile());

            // Make the segment RTL if it's 1) Translatable 2) In an RTL
            // language
            if (b_rtlLocale && !targetTuv.isLocalizable(jobId))
            {
                dir = "DIR=rtl ";
            }

            String style;
            if (LeverageUtil.isIncontextMatch(i, p_sourceTuvs,
                    p_targetTuvs, p_matchTypes, p_excludedItemTypes, jobId))
            {
                style = STYLE_CONTEXT;
            }
            else
            {
                style = getMatchStyle2(p_matchTypes, sourceTuv, targetTuv,
                        p_repetitions, DUMMY_SUBID, jobId);
                if (STYLE_SEGMENT_UPDATED.equals(style)
                        && GxmlUtil
                                .getDisplayHtml2(srcElem)
                                .trim()
                                .equals(GxmlUtil.getDisplayHtml2(trgElem)
                                        .trim()))
                {
                    style = STYLE_SEGMENT;
                }
            }

            // TODO: we may have a source+target list view eventually.
            // TODO: display sub-segments in their own paragraphs.

            if(filterResult != null)
            {
                if(!filterResult.get(SegmentFilter.KEY_SOURCE).contains(sourceTuv))
                {
                    continue;
                }
            }
            
            if (curPid != prevPid)
            {
                result.append("<P style='display:block'></P>\n");
            }
            tmpCount++;
            String sourceGxml = EditUtil.encodeHtmlEntities(srcElem
                    .toGxmlExcludeTopTags());
            String targetGxml = EditUtil.encodeHtmlEntities(trgElem
                    .toGxmlExcludeTopTags());
            String mergeState = targetTuv.getMergeState();

            if (segmentPar.length() > 0)
            {
                segmentPar.delete(0, segmentPar.length());
            }
            // Judge if is search result by user or sid, if it is, nedd
            // highlight
            boolean isHighLight = isHighLight(p_searchMap, sourceTuv, targetTuv);

            boolean preserveWhitespace = SegmentProtectionManager
                    .isPreserveWhiteSpace(srcElem);
            
            if (isHighLight)
            {
                segmentPar.append(
                        "<P style=\"background-color:yellow\" ID='seg_")
                        .append(tuId);
            }
            else
            {
                segmentPar.append("<P ID='seg_").append(tuId);
            }

            segmentPar.append("_").append(tuvId);
            segmentPar.append("_0' ");
            segmentPar.append(dir).append("class='").append(style).append("' ");
            segmentPar.append("onclick='edit(this)' ");
            
            if (preserveWhitespace)
            {
                segmentPar.append("preserveWhitespace='yes' ");
            }
            if (!dataType.equals(p_pageDataType))
            {
                segmentPar.append("datatype='").append(dataType).append("' ");
            }
            if (!itemType.equals("text"))
            {
                segmentPar.append("itemtype='").append(itemType).append("' ");
            }
            if (mergeState.equals(Tuv.MERGE_START))
            {
                int count = getMergedTargetTuvCount(p_targetTuvs, i);
                List stuvs = getMergedSourceTuvs(p_sourceTuvs, i, count);

                String mergedSource = EditUtil.encodeHtmlEntities(GxmlUtil
                        .stripRootTag(TuvMerger.getMergedText(stuvs)));

                segmentPar.append("isMerged=\"").append(
                        mapMergeState(mergeState));
                segmentPar.append("\" ");
                segmentPar.append("\nsource=\"").append(mergedSource)
                        .append("\" ");
                segmentPar.append("\ntarget=\"").append(targetGxml)
                        .append("\" ");
            }
            else if (mergeState.equals(Tuv.MERGE_MIDDLE)
                    || mergeState.equals(Tuv.MERGE_END))
            {
                segmentPar.append("isMerged=\"").append(
                        mapMergeState(mergeState));
                segmentPar.append("\" ");
                segmentPar.append("\nsource=\"").append(sourceGxml)
                        .append("\" ");
                segmentPar.append("\ntarget=\"").append(targetGxml)
                        .append("\" ");
            }
            else
            {
                segmentPar.append("\nsource=\"").append(sourceGxml)
                        .append("\" ");
                segmentPar.append("\ntarget=\"").append(targetGxml)
                        .append("\" ");
            }
            segmentPar.append(">");
            // TODO: need to show B/I/U; client handles real ptag conversion.
            segmentPar.append(GxmlUtil.getDisplayHtml2(trgElem));
            segmentPar.append("</P>");
            // String segment =
            // SegmentProtectionManager.handlePreserveWhiteSpace(srcElem,
            // segmentPar, dir, style);
            result.append(segmentPar);
            // TODO: display sub-segments in their own paragraphs.

            List subflowsTarget = targetTuv.getSubflowsAsGxmlElements(true);
            boolean b_subflows = (subflowsTarget != null && subflowsTarget
                    .size() > 0);
            // b_subflows = hasSubflows(subflowsTarget);
            if (b_subflows) // Check Here can remove none-necessary invoke
            {
                result.append(getSubSegments(sourceTuv, targetTuv,
                        p_matchTypes, b_rtlLocale, p_pageDataType,
                        p_repetitions, jobId));
            }

            prevPid = curPid;
        }

        return result.toString();
    }

    /**
     * Check hasSubSegments and return subSegments for XML file
     * 
     */
    private String getSubSegments(Tuv sourceTuv, Tuv targetTuv,
            MatchTypeStatistics p_matchTypes, boolean b_rtlLocale,
            String p_pageDataType, SegmentRepetitions p_repetitions,
            long p_jobId) throws Exception
    {
        StringBuffer result = null;
        List subflowsTarget = targetTuv.getSubflowsAsGxmlElements(true);
        boolean b_subflows = (subflowsTarget != null && subflowsTarget.size() > 0);
        // b_subflows = hasSubflows(subflowsTarget);

        Tu tu = targetTuv.getTu(p_jobId);
        long tuId = tu.getTuId();
        long tuvId = targetTuv.getId();
        String itemType = tu.getTuType();
        String dataType;

        if (b_subflows) // check here is to protect again none-checked invoke
        {
            result = new StringBuffer(255);
            List subflowsSource = sourceTuv.getSubflowsAsGxmlElements(true);
            // hasSubflows(subflowsSource);
            for (int j = 0; j < subflowsTarget.size(); j++)
            {
                GxmlElement subElmtTarget = (GxmlElement) subflowsTarget.get(j);
                GxmlElement subEletSource = (GxmlElement) subflowsSource.get(j);

                String subId = subElmtTarget.getAttribute(GxmlNames.SUB_ID);
                dataType = subElmtTarget.getAttribute(GxmlNames.SUB_DATATYPE);

                // Inherit datatype from parent element...
                if (dataType == null)
                {
                    GxmlElement node = subElmtTarget.getParent();

                    while (dataType == null && node != null)
                    {
                        dataType = node.getAttribute(GxmlNames.SUB_DATATYPE);
                        node = node.getParent();
                    }
                }

                // ... or from document if tuv inherits it.
                if (dataType == null)
                {
                    dataType = targetTuv.getDataType(p_jobId);
                }
                String dir;
                if (b_rtlLocale && isTranslatableSub(subElmtTarget)
                        && Text.containsBidiChar(subElmtTarget.getTextValue()))
                {
                    dir = " DIR=rtl";
                }
                else
                {
                    dir = "";
                }
                String subSourceGxml = EditUtil
                        .encodeHtmlEntities(subEletSource
                                .toGxmlExcludeTopTags());
                String subTargetGxml = EditUtil
                        .encodeHtmlEntities(subElmtTarget
                                .toGxmlExcludeTopTags());
                String style = getMatchStyle2(p_matchTypes, sourceTuv,
                        targetTuv, p_repetitions, subId, p_jobId);
                if (STYLE_SEGMENT_UPDATED.equals(style)
                        && GxmlUtil
                                .getDisplayHtml2(subElmtTarget)
                                .trim()
                                .equals(GxmlUtil.getDisplayHtml2(subEletSource)
                                        .trim()))
                {
                    style = STYLE_SEGMENT;
                }
                String subMergeState = targetTuv.getMergeState();
                // result.append("<P style='display:block'></P>\n");
                result.append("<br />\n");
                result.append("<P ID='seg_").append(tuId);
                result.append("_").append(tuvId);
                result.append("_").append(subId).append("' ");
                result.append(dir).append("class='").append(style).append("' ");
                result.append("onclick='edit(this)' ");
                if (!dataType.equals(p_pageDataType))
                {
                    result.append("datatype='").append(dataType).append("' ");
                }
                if (!itemType.equals("text"))
                {
                    result.append("itemtype='").append(itemType).append("' ");
                }
                if (subMergeState.equals(Tuv.MERGE_START))
                {
                    int count = getMergedTargetTuvCount(
                            (ArrayList) subflowsTarget, j);
                    List stuvs = getMergedSourceTuvs(
                            (ArrayList) subflowsSource, j, count);

                    String mergedSource = EditUtil.encodeHtmlEntities(GxmlUtil
                            .stripRootTag(TuvMerger.getMergedText(stuvs)));

                    result.append("isMerged=\"").append(
                            mapMergeState(subMergeState));
                    result.append("\" ");
                    result.append("\nsource=\"").append(mergedSource)
                            .append("\" ");
                    result.append("\ntarget=\"").append(subTargetGxml)
                            .append("\" ");
                }
                else if (subMergeState.equals(Tuv.MERGE_MIDDLE)
                        || subMergeState.equals(Tuv.MERGE_END))
                {
                    result.append("isMerged=\"").append(
                            mapMergeState(subMergeState));
                    result.append("\" ");
                    result.append("\nsource=\"").append(subSourceGxml)
                            .append("\" ");
                    result.append("\ntarget=\"").append(subTargetGxml)
                            .append("\" ");
                }
                else
                {
                    result.append("\nsource=\"").append(subSourceGxml)
                            .append("\" ");
                    result.append("\ntarget=\"").append(subTargetGxml)
                            .append("\" ");
                }
                result.append(">\n");
                result.append(GxmlUtil.getDisplayHtml2(subElmtTarget)).append(
                        "\n");
                result.append("</P>\n");
            }
            result.append("<P style='display:block'></P>\n");
        }
        return (result == null || result.length() == 0) ? "" : result
                .toString();
    }

    /**
     * <p>
     * Returns a Html display string for a Tuv. If the view mode is PREVIEW, the
     * original segment is encoded according to its datatype.
     * </p>
     */
    private String getSourceDisplayHtml(Tuv p_srcTuv, int p_viewMode,
            SourcePage p_srcPage, HashMap searchMap, RenderingOptions p_options)
    {
        long jobId = p_srcPage.getJobId();
        long tuId = p_srcTuv.getTu(jobId).getTuId();
        String dataType = p_srcTuv.getDataType(jobId);
        // String itemType = p_srcTuv.getTu().getTuType();

        // Get the target page locale so we can set the DIR attribute
        // for right-to-left languages such as Hebrew and Arabic
        boolean b_rtlLocale = EditUtil.isRTLLocale(p_srcPage
                .getGlobalSightLocale());
        String dir = "";

        // Make the segment RTL if it's 1) Translatable 2) In an RTL
        // language and 3) it has bidi characters in it.
        if (b_rtlLocale && !p_srcTuv.isLocalizable(jobId)
                && Text.containsBidiChar(p_srcTuv.getGxml()))
        {
            dir = " DIR=rtl";
        }

        StringBuffer result = new StringBuffer(256);
        String segment;

        switch (p_viewMode)
        {
            case VIEWMODE_LIST:
                // add javascript to synchronize scroll bars
                // by segment id in the pop-up editor
                result.append("<TD ID=seg");

                result.append(tuId);
                result.append(">");
                result.append("<Script Language=\"JavaScript\">");
                result.append("update_tr(\"");
                result.append("seg");
                result.append(tuId);
                result.append("\");");
                result.append("</Script>");

                result.append(tuId);
                result.append("</TD>\n");
                result.append("<TD");

                List subflows = p_srcTuv.getSubflowsAsGxmlElements(true);
                boolean b_subflows = (subflows != null && subflows.size() > 0);
                // b_subflows = hasSubflows(subflows);
                if (!b_subflows)
                {
                    result.append(dir);
                }

                result.append('>');

                GxmlElement elem = p_srcTuv.getGxmlElement();
                segment = GxmlUtil.getDisplayHtml(elem, dataType, p_viewMode);
                segment = SegmentProtectionManager.handlePreserveWhiteSpace(
                        elem, segment, null, null);
                String seg = getEditorSegment(p_srcTuv,
                        EditorConstants.PTAGS_COMPACT, segment,
                        p_options.getNeedShowPTags(), jobId);
                if (!b_subflows)
                {
                    result.append(seg);
                }
                else
                {
                    result.append("<TABLE WIDTH=100% CELLSPACING=0");
                    result.append(" CELLPADDING=2>\n");
                    result.append("<COL WIDTH=1%  VALIGN=TOP CLASS=editorId>");
                    result.append("<COL WIDTH=99% VALIGN=TOP>\n");
                    result.append("<TR><TD COLSPAN=2");
                    result.append(dir);
                    result.append(" CLASS=editorText>");
                    result.append(seg);
                    result.append("</TD></TR>\n");

                    // process each subflow
                    for (int i = 0; i < subflows.size(); i++)
                    {
                        GxmlElement subElmt = (GxmlElement) subflows.get(i);
                        String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
                        dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

                        if (dataType == null)
                        {
                            dataType = p_srcTuv.getDataType(jobId);
                        }

                        if (b_rtlLocale
                                && isTranslatableSub(subElmt)
                                && Text.containsBidiChar(subElmt.getTextValue()))
                        {
                            dir = " DIR=rtl";
                        }
                        else
                        {
                            dir = "";
                        }

                        segment = GxmlUtil.getDisplayHtml(subElmt, dataType,
                                p_viewMode);

                        result.append("<TR>");
                        result.append(getSubIdColumn(tuId, subId));
                        result.append("<TD");
                        result.append(dir);
                        result.append(" CLASS=editorText>");
                        result.append(segment);
                        result.append("</TD>");
                        result.append("</TR>\n");
                    }

                    result.append("</TABLE>\n");
                }

                result.append("</TD>\n");

                break;

            case VIEWMODE_TEXT:
                segment = p_srcTuv.getGxmlElement().getTotalTextValue();

                segment = EditUtil.encodeHtmlEntities(segment);

                result.append(segment);
                break;

            case VIEWMODE_PREVIEW: // fall through
            default:
                // Assumption: PREVIEW MODE is only allowed for HTML and
                // derived formats, and plaintext files. Encode subs in
                // HTML according to their datatype.

                if (EditUtil.isHtmlDerivedFormat(dataType))
                {
                    // can only show images in HTML-derived files
                    segment = GxmlUtil.getOriginalTextInHtmlWithImages(
                            p_srcTuv.getGxmlElement(), p_srcPage);
                }
                else
                {
                    segment = GxmlUtil.getOriginalTextInHtml(p_srcTuv
                            .getGxmlElement());
                }

                result.append(segment);
                break;
        }

        return result.toString();
    }

    /**
     * Get the first column (id field) Html code in the subflow table.
     */
    private String getSubIdColumn(long p_tuId, String p_subId)
    {
        StringBuffer result = new StringBuffer();

        result.append("<TD STYLE=\"font-size: 10pt\" NOWRAP>");
        result.append(p_tuId);
        result.append('.');
        result.append(p_subId);
        result.append("</TD>");

        return result.toString();
    }

    /**
     * <p>
     * Returns Html output for an editable item cell.
     * </p>
     */
    private String getEditableCell(String p_style, long p_tuId, long p_tuvId,
            String p_subId, String p_text, boolean p_addBrackets)
    {
        return getEditableCell(p_style, p_tuId, p_tuvId, p_subId, p_text,
                p_addBrackets, false, "");
    }

    /**
     * <p>
     * Returns Html output for an editable item cell for Preview.
     * </p>
     */
    private String getEditableCellForPreview(String p_style, long p_tuId,
            long p_tuvId, String p_subId, String p_text, boolean p_addBrackets,
            String p_dir)
    {
        return getEditableCell(p_style, p_tuId, p_tuvId, p_subId, p_text,
                p_addBrackets, true, p_dir);
    }

    /**
     * Internal methods called from the other two getEditableCell() methods.
     * Don't call it directly.
     */
    private String getEditableCell(String p_style, long p_tuId, long p_tuvId,
            String p_subId, String p_text, boolean p_addBrackets,
            boolean p_addId, String p_dir)
    {
        StringBuffer result = new StringBuffer();

        result.append("<A CLASS=\"");
        result.append(p_style);
        result.append("\" HREF=\"javascript:SE(");
        result.append(p_tuId);
        result.append(",");
        result.append(p_tuvId);
        result.append(",");
        result.append(p_subId);
        result.append(")\"");
        result.append(p_dir);

        if (p_addId)
        {
            result.append(" id=seg");
            result.append(p_tuId);
            result.append('_');
            result.append(p_tuvId);
            result.append('_');
            result.append(p_subId);
        }

        result.append(">");

        if (p_addBrackets)
        {
            result.append('[');
        }

        result.append(p_text);

        if (p_addBrackets)
        {
            result.append(']');
        }

        result.append("</A>");

        return result.toString();
    }

    /**
     * <p>
     * Returns Html output for a non-editable item cell.
     * </p>
     */
    private String getNonEditableCell(String p_style, String p_text)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN CLASS=\"");
        result.append(p_style);
        result.append("\">");
        result.append(p_text);
        result.append("</SPAN>");

        return result.toString();
    }

    /**
     * <p>
     * Returns Html output for a non-editable item cell for preview.
     * </p>
     */
    private String getNonEditableCellForPreview(String p_style, String p_text,
            String p_dir)
    {
        StringBuffer result = new StringBuffer();

        // def7691
        // Adding <SPAN> to segments break original document's <SPAN>
        // when an opening <SPAN> begins before the current segment
        // and the corresponding closing </SPAN> is inside the
        // segment.
        //
        // <A> is safe to add because original <A> tags are modified
        // to some other form in order to disable the links.

        result.append("<A CLASS=\"");
        result.append(p_style);
        result.append("\"");
        result.append(p_dir);
        result.append(">");
        result.append(p_text);
        result.append("</A>");

        return result.toString();
    }

    private String getCommentIcon(long p_tuId, long p_tuvId, String p_subId,
            List p_comments)
    {
        if (haveCommentForSegment(p_tuId, p_tuvId, p_subId, p_comments))
        {
            StringBuffer result = new StringBuffer(
                    "<IMG class='editorComment' src='/globalsight/images/comment.gif' ");

            // Javascript SE() function is defined differently in review mode.
            result.append("onclick='SE(");
            result.append(p_tuId);
            result.append(",");
            result.append(p_tuvId);
            result.append(",");
            result.append(p_subId);
            result.append(")'>");

            return result.toString();
        }

        return "";
    }

    private boolean haveCommentForSegment(long p_tuId, long p_tuvId,
            String p_subId, List p_comments)
    {
        String key = CommentHelper.makeLogicalKey(getCurrentTargetPage()
                .getId(), p_tuId, p_tuvId, p_subId);

        for (int i = 0, max = p_comments.size(); i < max; i++)
        {
            Issue issue = (Issue) p_comments.get(i);

            if (issue.getLogicalKey().equals(key))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>
     * Returns a match style for a source/target Tuv pair.
     * </p>
     * 
     * @see LeverageMatchType
     */
    private String getMatchStyle(MatchTypeStatistics p_matchTypes,
            Tuv p_srcTuv, Tuv p_trgTuv, String p_subId, boolean p_isExcluded,
            boolean p_unlock, SegmentRepetitions p_repetitions, long p_jobId)
    {
        if (p_isExcluded)
        {
            return STYLE_EXCLUDED;
        }

        String modifiedUser = p_trgTuv.getLastModifiedUser();
        if (modifiedUser != null && modifiedUser.endsWith("_MT"))
        {
            return STYLE_MT;
        }

        // If the target tuv has been localized, i.e. previously been
        // overwritten by the user, don't color.
        if (p_trgTuv.isLocalized())
        {
            return STYLE_UPDATED;
        }
        
        if(p_trgTuv.getState().equals(TuvState.APPROVED))
        {
        	return STYLE_APPROVED;
        }

        return getMatchStyleByLM(p_matchTypes, p_srcTuv, p_trgTuv, p_subId,
                p_unlock, p_repetitions, p_jobId);
    }

    private String getMatchStyleByLM(MatchTypeStatistics p_matchTypes,
            Tuv p_srcTuv, Tuv p_trgTuv, String p_subId, boolean p_unlock,
            SegmentRepetitions p_repetitions, long p_jobId)
    {
        int state = p_matchTypes.getLingManagerMatchType(p_srcTuv.getId(),
                p_subId);

        switch (state)
        {
            case LeverageMatchLingManager.UNVERIFIED:
                return STYLE_UNVERIFIED;

            case LeverageMatchLingManager.EXACT:
                if (EditHelper.isTuvInProtectedState(p_trgTuv, p_jobId)
                        && !p_unlock)
                {
                    return STYLE_LOCKED;
                }

                return STYLE_EXACT_MATCH;

            case LeverageMatchLingManager.FUZZY:
                if (p_trgTuv.getRepetitionOfId() != 0)
                {
                    return STYLE_REPETITION;
                }
                return STYLE_FUZZY_MATCH;

            case LeverageMatchLingManager.STATISTICS:
                // A statistics match is a fuzzy match that fell below the
                // fuzzy matching threshold as defined in the TM profile.
                // So it gets colored as a no match. The segment editor
                // may still show fuzzy matches below the threshold.
            case LeverageMatchLingManager.NO_MATCH:
                if (p_trgTuv.getRepetitionOfId() != 0)
                {
                    return STYLE_REPETITION;
                }
                return STYLE_NO_MATCH;

            default:
                return STYLE_NO_MATCH;
        }
    }

    /**
     * <p>
     * Template-less UI mode: returns a match style for a source/target Tuv
     * pair.
     * </p>
     */
    private String getMatchStyle2(MatchTypeStatistics p_matchTypes,
            Tuv p_srcTuv, Tuv p_trgTuv, SegmentRepetitions p_repetitions,
            String p_subId, long p_jobId)
    {
        // Template-less UI mode shows only text segments, so nothing
        // needs to be excluded.

        // If the target tuv has been localized, i.e. previously been
        // overwritten by the user, don't color.
        if (p_trgTuv.isLocalized())
        {
            return STYLE_SEGMENT_UPDATED;
        }

        int state = p_matchTypes.getLingManagerMatchType(p_srcTuv.getId(),
                p_subId);

        switch (state)
        {
            case LeverageMatchLingManager.UNVERIFIED:
                return STYLE_SEGMENT_UNVERIFIED;
            case LeverageMatchLingManager.EXACT:
                if (EditHelper.isTuvInProtectedState(p_trgTuv, p_jobId))
                {
                    return STYLE_SEGMENT_LOCKED;
                }
                else
                {
                    return STYLE_SEGMENT_EXACT;
                }
            case LeverageMatchLingManager.FUZZY:
                if (p_trgTuv.getRepetitionOfId() != 0)
                {
                    return STYLE_SEGMENT_REPETITION;
                }
                else
                {
                    return STYLE_SEGMENT_FUZZY;
                }
            case LeverageMatchLingManager.STATISTICS:
                // A statistics match is a fuzzy match that fell below the
                // fuzzy matching threshold as defined in the TM profile.
                // So it gets colored as a no match. The segment editor
                // may still show fuzzy matches below the threshold.
            case LeverageMatchLingManager.NO_MATCH:
                if (p_trgTuv.getRepetitionOfId() != 0)
                {
                    return STYLE_SEGMENT_REPETITION;
                }
                else
                {
                    return STYLE_SEGMENT;
                }
            default:
                return STYLE_SEGMENT;
        }
    }

    /**
     * Maps a view mode to the corresponding template type.
     */
    private int mapViewmodeToTemplateType(int p_viewMode)
    {
        int result;

        switch (p_viewMode)
        {
            case VIEWMODE_GXML:
                result = PageTemplate.TYPE_EXPORT;
                break;
            case VIEWMODE_PREVIEW:
                result = PageTemplate.TYPE_PREVIEW;
                break;
            case VIEWMODE_TEXT:
                result = PageTemplate.TYPE_DETAIL;
                break;
            case VIEWMODE_LIST: // fall through
            default:
                result = PageTemplate.TYPE_STANDARD;
                break;
        }

        return result;
    }

    /**
     * Extracts the list of TUs &lt;&lt;TUS&lt;&lt;ID&gt;&gt;&gt;&gt; and
     * replaces &lt;&lt;TUVS&lt;&lt;ID&gt;&gt;&gt;&gt; with the target tuv ids.
     */
    private String insertTuvIds(String p_string, Collection p_tuvs)
    {
        Iterator it = p_tuvs.iterator();

        StringBuffer tuvIds = new StringBuffer(128);
        String tuIds;
        String regex;
        RegExMatchInterface match = null;

        for (int i = 0; /**/; ++i)
        {
            try
            {
                regex = "<<TUS" + i + " +([^>]*)>>";

                match = RegEx.matchSubstring(p_string, regex);

                p_string = RegEx.substituteAll(p_string, regex, "");
            }
            catch (RegExException ex)
            {
                CATEGORY.error("PILOT ERROR IN REGEXP", ex);
            }

            // While there are records in the html page
            if (match == null)
            {
                break;
            }

            // Extract the TU ID list, find the target tuvs
            tuIds = match.group(1);

            StringTokenizer tok = new StringTokenizer(tuIds, " ,");
            int count = tok.countTokens();

            // actually we count the number of tus in this record and
            // fetch the next n tuvs from the list of tuvs.
            for (int j = 0; j < count; j++)
            {
                Tuv tuv = (Tuv) it.next();

                tuvIds.append('&');
                tuvIds.append(ExportConstants.TUV_ID);
                tuvIds.append('=');
                tuvIds.append(tuv.getId());
            }

            // and insert the tuv ids in the javascript string
            try
            {
                p_string = RegEx.substituteAll(p_string, "<<TUVS" + i + ">>",
                        tuvIds.toString());
            }
            catch (RegExException ex)
            {
                CATEGORY.error("PILOT ERROR IN REGEXP", ex);
            }

            tuvIds.setLength(0);
        }

        return p_string;
    }

    /**
     * Removes the preview link from a DB template. Call this in case the link
     * must not be shown, i.e. when the request says isCxePreviewable = false.
     */
    private String removePreviewLink(String p_string)
    {
        try
        {
            String regex = "<!--Preview-->(.|[:space:])+?<!--EndPreview-->";

            p_string = RegEx
                    .substituteAll(p_string, regex,
                            "<IMG SRC='/globalsight/images/editor/spacer.gif' HEIGHT=1 WIDTH=50>");

            regex = "<<TUS+([^>]*)>>";

            p_string = RegEx.substituteAll(p_string, regex, "");
        }
        catch (RegExException ex)
        {
            CATEGORY.error("PILOT ERROR IN REGEXP", ex);
        }

        return p_string;
    }

    /**
     * Returns true if the gxml element representing a SUB has a locType of
     * "translatable".
     */
    private boolean isTranslatableSub(GxmlElement p_sub)
    {
        return p_sub.getAttribute(GxmlNames.SUB_LOCTYPE).equals(
                GxmlNames.TRANSLATABLE);
    }

    private SnippetPageTemplate makeSnippetPageTemplate(
            PageTemplate p_template, GlobalSightLocale p_locale,
            SourcePage p_srcPage)
    {
        String locale;

        if (p_locale != null)
        {
            locale = p_locale.toString();
        }
        else
        {
            locale = p_srcPage.getGlobalSightLocale().toString();
        }

        return new SnippetPageTemplate(p_template, locale);
    }

    // Mapping the state here provides compile-time safety for the UI.
    private String mapMergeState(String p_state)
    {
        if (p_state.equals(Tuv.MERGE_START))
        {
            return "start";
        }
        else if (p_state.equals(Tuv.MERGE_MIDDLE))
        {
            return "middle";
        }
        else if (p_state.equals(Tuv.MERGE_END))
        {
            return "end";
        }
        else
        {
            return "";
        }
    }

    private int getMergedTargetTuvCount(List p_targetTuvs, int p_start)
    {
        for (int i = p_start, max = p_targetTuvs.size(); i < max; i++)
        {
            Tuv tuv = (Tuv) p_targetTuvs.get(i);

            if (tuv.getMergeState().equals(Tuv.MERGE_END))
            {
                return i - p_start + 1;
            }
        }

        return 1;
    }

    private List getMergedSourceTuvs(List p_sourceTuvs, int p_start, int p_count)
    {
        ArrayList<Tuv> result = new ArrayList<Tuv>();

        for (int i = 0; i < p_count; i++)
        {
            Tuv tuv = (Tuv) p_sourceTuvs.get(p_start + i);

            result.add(tuv);
        }

        return result;
    }

    /**
     * Returns a cloned source tuv for segment editor when targets were merged.
     */
    private Tuv getMergedSourceTuvByTargetId(long p_tuvId) throws Exception,
            GeneralException, RemoteException
    {
        TuvImpl result = null;

        List sourceTuvs = getPageTuvs(getCurrentSourcePage());
        List targetTuvs = getPageTuvs(getCurrentTargetPage());

        // Can't use binary search since target tuv ids are NOT sorted.
        int index = 0;
        for (int i = 0, max = targetTuvs.size(); i < max; i++)
        {
            Tuv tuv = (Tuv) targetTuvs.get(i);

            if (tuv.getId() == p_tuvId)
            {
                index = i;
                break;
            }
        }

        TuvImpl srcTuv = (TuvImpl) sourceTuvs.get(index);

        // Should clone via TuvManager.
        result = new TuvImpl(srcTuv);
        result.setId(srcTuv.getId());

        // set combined text
        int count = getMergedTargetTuvCount(targetTuvs, index);
        List stuvs = getMergedSourceTuvs(sourceTuvs, index, count);
        String mergedSource = TuvMerger.getMergedText(stuvs);

        // Sub IDs have already been computed, TuvImpl.setGxml() and
        // TuvImpl.setSubIdAttributes() should not renumber them.
        result.setGxml/* WithSubIds */(mergedSource);

        return result;
    }

    //
    // Caching-related helper methods
    //
    /**
     * For online-offline synchronization: this method allows the online
     * pagehandler to invalidate target TUVs after an offline file upload.
     * Despite its name, this method invalidates ONLY target TUVs.
     */
    public void invalidateCache() throws OnlineEditorException, RemoteException
    {
        synchronized (m_pageCache.m_targetTuvsLock)
        {
            m_pageCache.setTargetTuvs(null);
        }
    }

    /**
     * Invalidates the cached Page Templates and Template Parts for source page
     * and target page.
     */
    public void invalidateCachedTemplates() throws OnlineEditorException,
            RemoteException
    {
        synchronized (m_pageCache.m_templateLock)
        {
            m_pageCache.setInterpretedTuIds(null);
            m_pageCache.setTargetTemplateParts(null);
            m_pageCache.setSourceTemplateParts(null);
            m_pageCache.setTargetTemplate(null);
            m_pageCache.setSourceTemplate(null);
        }
    }

    private SourcePage getSourcePage(long p_srcPageId) throws GeneralException,
            RemoteException
    {
        SourcePage result = m_pageManager.getSourcePage(p_srcPageId);
        m_pageCache.setSourcePage(result);
        /*
         * synchronized (m_pageCache.m_pageLock) { result =
         * m_pageCache.getSourcePage();
         * 
         * if (result == null || result.getId() != p_srcPageId) {
         * m_pageCache.setSourcePage(m_pageManager .getSourcePage(p_srcPageId));
         * 
         * result = m_pageCache.getSourcePage(); } }
         */

        return result;
    }

    private SourcePage getCurrentSourcePage() throws GeneralException,
            RemoteException
    {
        SourcePage result = null;

        synchronized (m_pageCache.m_pageLock)
        {
            result = m_pageCache.getSourcePage();
        }

        return result;
    }

    private TargetPage getTargetPage(long p_trgPageId) throws GeneralException,
            RemoteException
    {
        TargetPage result = m_pageManager.getTargetPage(p_trgPageId);
        result.setGlobalSightLocale(result.getGlobalSightLocale());
        m_pageCache.setTargetPage(result);

        return result;
    }

    private TargetPage getCurrentTargetPage()
    {
        TargetPage result = null;

        synchronized (m_pageCache.m_pageLock)
        {
            result = m_pageCache.getTargetPage();
        }

        return result;
    }

    private Collection<TuImpl> getPageTus(SourcePage p_page)
            throws GeneralException, RemoteException
    {
        Collection<TuImpl> result;

        synchronized (m_pageCache.m_tusLock)
        {
            result = m_pageCache.getTus();

            if (result == null)
            {
                m_pageCache.setTus(m_tuvManager.getTus(p_page.getIdAsLong()));

                result = m_pageCache.getTus();
                if (m_pageCache.m_tusInMap == null)
                {
                    m_pageCache.m_tusInMap = new HashMap<Long, TuImpl>();
                }
                for (TuImpl tu : result)
                {
                    m_pageCache.m_tusInMap.put(tu.getIdAsLong(), tu);
                }
            }
        }

        return result;
    }

    private List<Tuv> getPageTuvs(SourcePage p_page)
            throws GeneralException, RemoteException
    {
        ArrayList<Tuv> result;

        synchronized (m_pageCache.m_sourceTuvsLock)
        {
            result = m_pageCache.getSourceTuvs();

            if (result == null)
            {
                try
                {
                    m_pageCache.setSourceTuvs(SegmentTuvUtil.getSourceTuvs(p_page));
                    result = m_pageCache.getSourceTuvs();

                    for (Tuv srcTuv : result)
                    {
                        TuImpl myTu = m_pageCache.m_tusInMap.get(srcTuv.getTuId());
                        if (myTu != null)
                        {
                            srcTuv.setTu(myTu);
                            myTu.addTuv(srcTuv);
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new GeneralException(e);
                }
            }
        }

        return result;
    }

    private List<Tuv> getPageTuvs(TargetPage p_page) throws GeneralException,
            RemoteException
    {
        List<Tuv> result;

        synchronized (m_pageCache.m_targetTuvsLock)
        {
            result = m_pageCache.getTargetTuvs();

            if (result == null)
            {
                m_pageCache.setTargetTuvs(new ArrayList<Tuv>(m_tuvManager
                        .getTargetTuvsForStatistics(p_page)));

                result = m_pageCache.getTargetTuvs();
                for (Tuv trgTuv : result)
                {
                    TuImpl myTu = m_pageCache.m_tusInMap.get(trgTuv.getTuId());
                    if (myTu != null)
                    {
                        trgTuv.setTu(myTu);
                        myTu.addTuv(trgTuv);
                    }
                }
            }
        }

        return result;
    }

    private SegmentRepetitions getRepetitions(SourcePage p_page)
            throws GeneralException, RemoteException
    {
        SegmentRepetitions result;

        // Repetitions are a by-product of setting source tuvs in the
        // cache, so if repetitions are null, load the source tuvs.
        synchronized (m_pageCache.m_sourceTuvsLock)
        {
            result = m_pageCache.getRepetitions();

            if (result == null)
            {
                getPageTuvs(p_page);

                result = m_pageCache.getRepetitions();
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private ArrayList getImageMaps(TargetPage p_targetPage)
            throws GeneralException, RemoteException
    {
        ArrayList result;

        synchronized (m_pageCache.m_imageMapsLock)
        {
            result = m_pageCache.getImageMaps();

            if (result == null)
            {
                // TODO: make ImageManager return ArrayList or Vector.
                m_pageCache.setImageMaps(new ArrayList(m_imageManager
                        .getImageReplaceFileMapsForTargetPage(p_targetPage
                                .getIdAsLong())));

                result = m_pageCache.getImageMaps();
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Issue> getComments(TargetPage p_targetPage)
            throws GeneralException, RemoteException
    {
        ArrayList<Issue> result;

        synchronized (m_pageCache.m_commentsLock)
        {
            result = m_pageCache.getComments();

            if (result == null)
            {
                // TODO: make CommentManager return ArrayList or Vector.

                /*
                 * ArrayList temp = new ArrayList();
                 * 
                 * temp.add(new IssueImpl(Issue.TYPE_SEGMENT, 1125, "title 1",
                 * Issue.PRI_MEDIUM, Issue.STATUS_OPEN, "clove", "this is the
                 * latest comment", "1001_1001_1125_0")); temp.add(new
                 * IssueImpl(Issue.TYPE_SEGMENT, 1091, "title 2",
                 * Issue.PRI_MEDIUM, Issue.STATUS_OPEN, "cvdl", "this is a
                 * comment in the middle", "1001_1063_1091_0")); temp.add(new
                 * IssueImpl(Issue.TYPE_SEGMENT, 1115, "title 3",
                 * Issue.PRI_MEDIUM, Issue.STATUS_OPEN, "clove", "this comment
                 * started it all", "1001_1066_1115_0"));
                 * m_pageCache.setComments(temp);
                 */

                ArrayList<Issue> issues = new ArrayList<Issue>();
                issues.addAll(m_commentManager.getIssues(
                        Issue.TYPE_SEGMENT, p_targetPage.getId()));
                m_pageCache.setComments(issues);

                result = m_pageCache.getComments();
            }
        }

        return result;
    }

    public MatchTypeStatistics getMatchTypes(Long p_sourcePageId,
            Long p_targetLocaleId) throws GeneralException, RemoteException
    {
        MatchTypeStatistics result;

        synchronized (m_pageCache.m_matchTypesLock)
        {
            result = m_pageCache.getMatchTypes();

            if (result == null)
            {
                // Must return an object, not null.
                // If the source file is WorldServer XLF file,MT translations
                // should NOT impact the color in pop-up editor.
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(p_sourcePageId);
                if (isWSXlfSourceFile)
                {
                    m_lingManager.setIncludeMtMatches(false);
                }
                m_pageCache.setMatchTypes(m_lingManager
                        .getMatchTypesForStatistics(p_sourcePageId,
                                p_targetLocaleId, 0));

                result = m_pageCache.getMatchTypes();
            }
        }

        return result;
    }

    /**
     * <p>
     * Returns a cloned page template for a page. Page templates are identical
     * for source and target pages, so to ensure side-effect-free uses of
     * templates by source and target code, we clone them.
     * </p>
     */
    PageTemplate getPageTemplate(int p_viewMode, Page p_page)
    // throws OnlineEditorException
    {
        PageTemplate result;
        int templateType = mapViewmodeToTemplateType(p_viewMode);

        boolean b_source = true;
        if (p_page instanceof TargetPage)
        {
            b_source = false;
        }

        synchronized (m_pageCache.m_templateLock)
        {
            if (b_source)
            {
                result = m_pageCache.getSourceTemplate();
            }
            else
            {
                result = m_pageCache.getTargetTemplate();
            }

            // If no template yet, or cache out of date, refresh.
            if (result == null || result.getType() != templateType)
            {
                // I think we need to clone the template, or else
                // source and target calls may end up writing
                // different TUVs to the same template?!? How did
                // this ever work before?
                result = new PageTemplate(getExtractedFile(p_page)
                        .getPageTemplate(templateType));

                // setting the template invalidates the template parts
                if (b_source)
                {
                    m_pageCache.setSourceTemplate(result);
                }
                else
                {
                    m_pageCache.setTargetTemplate(result);
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<TemplatePart> getSourceTemplateParts(Long p_srcPageId,
            String p_templateType) throws GeneralException, RemoteException
    {
        ArrayList<TemplatePart> result;

        synchronized (m_pageCache.m_templateLock)
        {
            result = m_pageCache.getSourceTemplateParts();

            if (result == null)
            {
                m_pageCache.setSourceTemplateParts(new ArrayList<TemplatePart>(
                        m_pageManager.getTemplatePartsForSourcePage(
                                p_srcPageId, p_templateType)));

                result = m_pageCache.getSourceTemplateParts();
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<TemplatePart> getTargetTemplateParts(Long p_srcPageId,
            String p_templateType) throws GeneralException, RemoteException
    {
        List<TemplatePart> result = null;

        synchronized (m_pageCache.m_templateLock)
        {
            result = m_pageCache.getTargetTemplateParts();

            if (result == null)
            {
                m_pageCache.setTargetTemplateParts(new ArrayList<TemplatePart>(
                        m_pageManager.getTemplatePartsForSourcePage(
                                p_srcPageId, p_templateType)));

                result = m_pageCache.getTargetTemplateParts();
            }
        }

        return result;
    }

    private HashSet getInterpretedTuIds_1(long p_srcPageId,
            GlobalSightLocale p_locale) throws GeneralException,
            RemoteException
    {
        HashSet result;

        synchronized (m_pageCache.m_templateLock)
        {
            result = m_pageCache.getInterpretedTuIds();

            if (result == null)
            {
                SourcePage srcPage = getSourcePage(p_srcPageId);

                // Caller should check source page contains GS tags.
                if (!containGsTags(srcPage))
                {
                    return null;
                }

                // Load the TUs into the Toplink cache to prevent
                // called code from loading each TU individually.
                getPageTus(srcPage);

                // Get the Page Template - We should request our own
                // private copy to prevent the source template cache
                // from trashing, then load the template parts and run
                // it once, then cache the resulting interpreted ids.
                PageTemplate template = getPageTemplate(
                        PageTemplate.TYPE_DETAIL, srcPage);

                if (!(template instanceof SnippetPageTemplate))
                {
                    template = makeSnippetPageTemplate(template, p_locale,
                            srcPage);
                    m_pageCache.setSourceTemplate(template);
                }

                if (template.getTemplateParts() == null
                        || template.getTemplateParts().size() == 0)
                {
                    ArrayList<TemplatePart> parts = getSourceTemplateParts(
                            srcPage.getIdAsLong(), template.getTypeAsString());

                    // for snippets, computes positions
                    template.setTemplateParts(parts);
                }

                m_pageCache.setInterpretedTuIds(template.getInterpretedTuIds());

                result = m_pageCache.getInterpretedTuIds();
            }
        }

        return result;
    }

    /**
     * Returns true if the page contains GS tags and false if not.
     */
    private boolean containGsTags(SourcePage p_page)
    {
        return getExtractedSourceFile(p_page).containGsTags();
    }

    /**
     * Returns the extracted source file or NULL if there isn't one.
     */
    private ExtractedSourceFile getExtractedSourceFile(SourcePage p_page)
    {
        return (ExtractedSourceFile) getExtractedFile(p_page);
    }

    /**
     * Returns the extracted file or NULL if there isn't one.
     */
    private ExtractedFile getExtractedFile(Page p_page)
    {
        ExtractedFile ef = null;

        if (p_page.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
        {
            ef = (ExtractedFile) p_page.getPrimaryFile();
        }

        return ef;
    }

    @Override
    public ArrayList getPageLastModifyUserList(EditorState p_state)
            throws GeneralException, RemoteException
    {
        ArrayList<String> result = new ArrayList<String>();

        TargetPage targetPage = getTargetPage(p_state.getTargetPageId());
        List tuvs = getPageTuvs(targetPage);

        int segmentNumPerPage = p_state.getPaginateInfo()
                .getSegmentNumPerPage();
        int currentPageNum = p_state.getPaginateInfo().getCurrentPageNum();

        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
        int count = 0;

        // Find all sid on current page
        for (int i = beginIndex, max = tuvs.size(); count < segmentNumPerPage
                && i < max; i++)
        {
            count++;
            Tuv tuv = (Tuv) tuvs.get(i);

            String name = tuv.getLastModifiedUser();

            if (name != null && !name.equals("") && !name.equals("Xliff")
                    && !name.equals("po") && !name.equals("TDA")
                    & (name.indexOf("_MT") < 0))
            {
                if (!result.contains(name))
                {
                    result.add(name);
                }
            }
        }

        return result;
    }

    public ArrayList getPageSidList(EditorState p_state)
            throws GeneralException, RemoteException
    {
        ArrayList<String> result = new ArrayList<String>();

        SourcePage sourcePage = getSourcePage(p_state.getSourcePageId());
        List tuvs = getPageTuvs(sourcePage);

        int segmentNumPerPage = p_state.getPaginateInfo()
                .getSegmentNumPerPage();
        int currentPageNum = p_state.getPaginateInfo().getCurrentPageNum();

        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
        int count = 0;

        // Find all sid on current page
        for (int i = beginIndex, max = tuvs.size(); count < segmentNumPerPage
                && i < max; i++)
        {
            count++;
            Tuv tuv = (Tuv) tuvs.get(i);
            String sid = tuv.getSid();

            if (sid != null && !sid.equals(""))
            {
                if (!result.contains(sid))
                {
                    result.add(sid);
                }
            }
        }

        return result;
    }

    public boolean isHighLight(HashMap p_searchMap, Tuv p_srcTuv,
            Tuv p_targetTuv)
    {
        boolean isHighLight = false;

        try
        {
            if (p_searchMap != null)
            {
                Iterator it = p_searchMap.entrySet().iterator();

                while (it.hasNext())
                {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) it
                            .next();
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if (key.equals("userId"))
                    {
                        if (p_targetTuv.getLastModifiedUser() != null
                                && p_targetTuv.getLastModifiedUser().equals(
                                        value))
                        {
                            isHighLight = true;
                        }
                    }
                    else if (key.equals("sid"))
                    {
                        if (p_srcTuv.getSid() != null
                                && p_srcTuv.getSid().equals(value))
                        {
                            isHighLight = true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }

        return isHighLight;
    }

    private List<Long> getCurrentTuIds(List<Tuv> p_tuvs, PaginateInfo p_pi)
    {
        List<Long> tuIdList = m_pageCache.getCurrentPageTuIDS();
        if (tuIdList == null)
        {
            tuIdList = new ArrayList<Long>();
            int segmentNumPerPage = p_pi.getSegmentNumPerPage();
            int currentPageNum = p_pi.getCurrentPageNum();
            int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
            // Find all tuIds on current page
            for (int i = beginIndex, count = 0, max = p_tuvs.size(); count < segmentNumPerPage
                    && i < max; i++)
            {
                count++;
                Tuv tuv = (Tuv) p_tuvs.get(i);
                tuIdList.add(tuv.getTuId());
            }
        }

        return tuIdList;
    }

    
    @Override
    public String getSourceJsonData(EditorState p_state, boolean isAssignee)
    {
        return getSourceJsonData(p_state, isAssignee, false);
    }

    @Override
    public String getSourceJsonData(EditorState p_state, boolean isAssignee, boolean fromInCtxRv)
    {
        long srcPageId = p_state.getSourcePageId().longValue();

        GlobalSightLocale targetLocale = p_state.getTargetLocale();
        PaginateInfo paginateInfo = p_state.getPaginateInfo();
        RenderingOptions options = p_state.getRenderingOptions();
        JSONArray json = new JSONArray();
        try
        {
            json = getSourceJsonResult(srcPageId, targetLocale, paginateInfo,
                    options, fromInCtxRv);
        }
        catch (OnlineEditorException e)
        {
            CATEGORY.error(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.error(e);
        }
        return json.toString();
    }
    
    public JSONArray getSourceJsonResult(long p_srcPageId,
            GlobalSightLocale p_locale, PaginateInfo paginateInfo,
            RenderingOptions options) throws OnlineEditorException,
            RemoteException
    {
        return getSourceJsonResult(p_srcPageId, p_locale, paginateInfo, options, false);
    }

    public JSONArray getSourceJsonResult(long p_srcPageId,
            GlobalSightLocale p_locale, PaginateInfo paginateInfo,
            RenderingOptions options, boolean fromInCtxRv) throws OnlineEditorException,
            RemoteException
    {
        JSONArray jsonArr = new JSONArray();

        try
        {
            SourcePage srcPage = getSourcePage(p_srcPageId);
            long jobId = srcPage.getJobId();

            // Load the TUs into the Toplink cache to prevent called
            // code from loading each TU individually.
            getPageTus(srcPage);

            // get the Tuvs in the page
            List<Tuv> tuvs = getPageTuvs(srcPage);

//            List<Long> tuIdList = getCurrentTuIds(tuvs, paginateInfo);

            // insert all tuv content into template despite of current page num
            // believe this won't bring performance issue
            // String itemType = p_srcTuv.getTu().getTuType();

            // Get the target page locale so we can set the DIR attribute
            // for right-to-left languages such as Hebrew and Arabic
            boolean b_rtlLocale = EditUtil.isRTLLocale(srcPage
                    .getGlobalSightLocale());
            int segmentNumPerPage = paginateInfo.getSegmentNumPerPage();
            int currentPageNum = paginateInfo.getCurrentPageNum();
            int beginIndex = (currentPageNum - 1) * segmentNumPerPage;
            for (int i = beginIndex, max = tuvs.size(), _count = 0; _count < segmentNumPerPage
                    && i < max; i++)
            {
                _count++;
                Tuv tuv = (Tuv) tuvs.get(i);
                JSONObject jsonObj = getSourceJsonResult(b_rtlLocale, tuv,
                        true, jobId, fromInCtxRv);
                jsonArr.put(jsonObj);
                // template.insertTuvContent(tuv.getTuId(), html);
            }

        }
        catch (Exception ex)
        {
            String[] args =
            { "Failed to retrieve source page view." };

            CATEGORY.error(args[0], ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW, args, ex);
        }

        return jsonArr;
    }

	private JSONObject getSourceJsonResult(boolean b_rtlLocale, Tuv tuv,
			boolean ptag, long p_jobId, boolean fromInCtxRv) throws JSONException
	{
        JSONObject jsonObj = new JSONObject();
        String dataType = tuv.getDataType(p_jobId);
        Tu tu = tuv.getTu(p_jobId);
        long tuId = tu.getTuId();
        String segment = "";
        GxmlElement elem = tuv.getGxmlElement();
        segment = GxmlUtil.getDisplayHtml(elem, dataType, VIEWMODE_LIST);
        segment = SegmentProtectionManager.handlePreserveWhiteSpace(elem,
                segment, null, null);
        // TODO Ooptions.getNeedShowPTags() move to jquey
        segment = getEditorSegment(tuv, EditorConstants.PTAGS_COMPACT, segment,
                ptag, p_jobId);
        String segmentNoTag = elem.getTextValue().trim();
        String searchText = null;
		if (!editorState.getNeedShowPTags())
		{
			if (searchMap != null)
			{
				searchText = (String)searchMap.get("searchText");
			}
		}
		if (searchText != null)
		{
			segment = getNewSegement(segment, searchText,"source");
		}
        jsonObj.put("tuId", tuId);
        jsonObj.put("segment", segment);
        if (fromInCtxRv)
        {
            jsonObj.put("segmentNoTag", segmentNoTag);
        }
        if (fromInCtxRv)
        {
            jsonObj.put("tuvId", tuv.getId());
            jsonObj.put("subId", DUMMY_SUBID);
        }
        List subflows = tuv.getSubflowsAsGxmlElements(true);
        boolean b_subflows = (subflows != null && subflows.size() > 0);
        if (b_subflows)
        {
            JSONArray subArray = new JSONArray();

            for (int j = 0; j < subflows.size(); j++)
            {
                JSONObject subObj = new JSONObject();
                GxmlElement subElmt = (GxmlElement) subflows.get(j);
                String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
                dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

                if (dataType == null)
                {
                    dataType = tuv.getDataType(p_jobId);
                }

                if (b_rtlLocale && isTranslatableSub(subElmt)
                        && Text.containsBidiChar(subElmt.getTextValue()))
                {
                    subObj.put("subclass", "rtl");
                }

                segment = GxmlUtil.getDisplayHtml(subElmt, dataType, 3);
				if (!editorState.getNeedShowPTags())
				{
					if (searchText != null)
					{
						segment = getNewSegement(segment, searchText,"source");
					}
				}
                subObj.put("tuId", tuId);
                subObj.put("subId", subId);
                subObj.put("segment", segment);
                subArray.put(subObj);
            }
            jsonObj.put("subArray", subArray);
        }
        StringBuffer mainClass = fromInCtxRv ?  new StringBuffer("SE editorSegment ") : new StringBuffer();
        if (tuv.isRepeated())
        {
            mainClass.append(UIConstants.COLOR_REPEATED);
        }
        else if (tuv.getRepetitionOfId() > 0)
        {
            mainClass.append(UIConstants.COLOR_REPETITION);
        }
        jsonObj.put("mainstyle", mainClass);
        return jsonObj;
    }
	
	@Override
	public void updateApprovedTuvCache(List<Long> approvedTuvIds, Date modifiedDate, String user) 
	{
		for(Tuv tuv: m_pageCache.getTargetTuvs())
		{
			if(approvedTuvIds.contains(tuv.getId()))
			{
				tuv.setState(TuvState.APPROVED);
				tuv.setLastModified(modifiedDate);
				tuv.setLastModifiedUser(user);
			}
		}
	}
	
	@Override
	public void updateUnapprovedTuvCache(List<Long> unapprovedTuvIds,
			HashMap<Long, TuvState> originalStateMap, Date modifiedDate, String user)
	{
		for(Tuv tuv: m_pageCache.getTargetTuvs())
		{
			if(unapprovedTuvIds.contains(tuv.getId()))
			{
				tuv.setState(originalStateMap.get(tuv.getId()));
				tuv.setLastModified(modifiedDate);
				tuv.setLastModifiedUser(user);
			}
		}
	}
	
	@Override
	public void updateRevertTuvCache(List<Long> revertTuvIds,
			HashMap<Long, String> originalGxmlMap, Date modifiedDate, String user) 
	{
		for(Tuv tuv: m_pageCache.getTargetTuvs())
		{
			if(revertTuvIds.contains(tuv.getId()))
			{
				tuv.setGxml(originalGxmlMap.get(tuv.getId()));
				tuv.setState(TuvState.LOCALIZED);
				tuv.setLastModified(modifiedDate);
				tuv.setLastModifiedUser(user);
			}
		}
	}
	
	@Override
	public String getTargetJsonData(EditorState p_state, boolean isAssignee,
            HashMap<String, String> p_searchMap)
	{
	    return getTargetJsonData(p_state, isAssignee, p_searchMap, false);
	}
	
	@Override
    public String getTargetJsonData(EditorState p_state, boolean isAssignee,
            HashMap<String, String> p_searchMap, boolean fromInCtxRv)
    {
        return getTargetJsonObject(p_state, isAssignee, p_searchMap,
                fromInCtxRv).toString();
    }

    @Override
    public JSONObject getTargetJsonObject(EditorState p_state, boolean isAssignee,
            HashMap<String, String> p_searchMap, boolean fromInCtxRv)
    {
        JSONObject mainJson = new JSONObject();
        JSONArray targetjArray = new JSONArray();
        JSONArray sourcejArray = new JSONArray();
        JSONArray originalTargetjArray = new JSONArray();
        JSONArray approvejArray = new JSONArray();
        editorState = p_state;
        searchMap = p_searchMap;
        long p_trgPageId = p_state.getTargetPageId().longValue();
        
        boolean needOriginalTarget = false;
        if(p_searchMap.get("needOriginalTarget") != null 
        		&& p_searchMap.get("needOriginalTarget").equals("true"))
        {
        	needOriginalTarget = true;
        }

        RenderingOptions options = p_state.getRenderingOptions();
        Vector p_excludedItemTypes = p_state.getExcludedItems();

        TranslationMemoryProfile tmProfile = p_state.getTmProfile();
        options.setTmProfile(tmProfile);
        options.setUserName(p_state.getUserName());

        if (p_state.isReadOnly())
        {
            options.setEditMode(UIConstants.EDITMODE_READ_ONLY);
        }
        else if (p_state.isEditAll())
        {
            options.setEditMode(UIConstants.EDITMODE_EDIT_ALL);
        }
        else
        {
            options.setEditMode(UIConstants.EDITMODE_DEFAULT);
        }
        // update with the updated modes
        p_state.setRenderingOptions(options);

        try
        {
            TargetPage targetPage = getTargetPage(p_trgPageId);
            SourcePage sourcePage = targetPage.getSourcePage();
                
            long jobId = sourcePage.getJobId();

            List<Tuv> sourceTuvs = getPageTuvs(sourcePage);
            ArrayList imageMaps = getImageMaps(targetPage);
            ArrayList<Issue> comments = null;
            SegmentRepetitions repetitions = getRepetitions(sourcePage);

            comments = getComments(targetPage);

            Long targetLocaleId = targetPage.getGlobalSightLocale()
                    .getIdAsLong();
            MatchTypeStatistics tuvMatchTypes = null;
            if (sourceTuvs.size() > 0)
            {
                tuvMatchTypes = getMatchTypes(sourcePage.getIdAsLong(),
                        targetLocaleId);
            }

            List<Tuv> targetTuvs = getPageTuvs(targetPage);
            List<Tuv> targetTuvs2 = new ArrayList<Tuv>();
            if (targetTuvs != null && targetTuvs.size() > 0)
            {
                Iterator<Tuv> it = targetTuvs.iterator();
                while (it.hasNext())
                {
                    Tuv targetTuv = (Tuv) it.next();

                    boolean isWSXlf = false;
                    boolean isAutoCommit = false;
                    if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(targetTuv
                            .getTu(jobId).getGenerateFrom()))
                    {
                        isWSXlf = true;
                    }
                    if (targetTuv.getLastModifiedUser() != null
                            && targetTuv.getLastModifiedUser().indexOf("_MT") > -1)
                    // && targetTuv.getState().getValue() == TuvState.LOCALIZED
                    // .getValue())
                    {
                        isAutoCommit = true;
                    }
                    // Clone the targetTuv to avoid changes are stored into DB.
                    TuvImpl cloneTargetTuv = new TuvImpl((TuvImpl) targetTuv);
                    cloneTargetTuv.setId(targetTuv.getId());// tuvId also needed
                    // If WS XLF and auto-commit,should display source in
                    // pop-up editor.
                    if (isWSXlf && isAutoCommit)
                    {
                        Tuv sourceTuv = targetTuv.getTu(jobId).getTuv(
                                p_state.getSourceLocale().getId(), jobId);
                        cloneTargetTuv.setGxml(sourceTuv.getGxml());
                        cloneTargetTuv.setLastModifiedUser(null);
                        cloneTargetTuv.setState(TuvState.NOT_LOCALIZED);
                    }
                    targetTuvs2.add(cloneTargetTuv);
                }
            }
            targetTuvs = targetTuvs2;
            
            HashMap<Long, Tuv> originalTargetTuvMap = new HashMap<Long, Tuv>();
            if(needOriginalTarget)
            {
            	List<Tuv> allTargetTuvs = SegmentTuvUtil.getAllTargetTuvs(targetPage);
            	setOriginalTargetTuvMap(targetTuvs, allTargetTuvs, originalTargetTuvMap, targetLocaleId);
            }

            // Gets the filtered source and target tuvs, and reset
            // PaginateInfo.
            Map<String, List<Tuv>> filterResult = SegmentFilter
                    .operateForSegmentFilter(m_pageCache, sourceTuvs,
                            targetTuvs, p_state, tuvMatchTypes, comments,
                            p_excludedItemTypes, p_trgPageId, jobId);
            // Gets the Paginate info
            PaginateInfo pi = getPaginateInfo(p_state, sourceTuvs, filterResult);
            int segmentNumPerPage = pi.getSegmentNumPerPage();
            int currentPageNum = pi.getCurrentPageNum();
            int beginIndex = (currentPageNum - 1) * segmentNumPerPage;

            // Gets term leverage matches from DB instead of dynamic
            // leveraging.
            TermLeverageMatchResultSet termLMResultSet = m_termManager
                    .getTermMatchesForPage(sourcePage,
                            targetPage.getGlobalSightLocale());

            // Gets Display TU Id List.
            List<Long> displayTuIdList = getDisplayIdList(sourceTuvs,
                    targetTuvs, beginIndex, segmentNumPerPage, filterResult,
                    jobId);
            m_pageCache.setCurrentPageTuIDS(displayTuIdList);
            if (displayTuIdList == null || displayTuIdList.size() == 0)
                return new JSONObject();
            boolean b_rtlLocale = EditUtil.isRTLLocale(sourcePage
                    .getGlobalSightLocale());
            // insert all tuv content into template despite of current page
            // num
            // believe this won't bring performance issue
            List<Tuv> filteredSourceTuvs = new ArrayList<Tuv>();
            List<Tuv> filteredTargetTuvs = new ArrayList<Tuv>();
            
            if (filterResult != null
                    && filterResult.get(SegmentFilter.KEY_SOURCE) != null)
            {
                filteredSourceTuvs = (List<Tuv>) filterResult
                        .get(SegmentFilter.KEY_SOURCE);
                filteredTargetTuvs = (List<Tuv>) filterResult
                        .get(SegmentFilter.KEY_TARGET);
            }

            for (int i = beginIndex, max = sourceTuvs.size(), _count = 0; _count < segmentNumPerPage
                    && i < max; i++)
            {
                Tuv srcTuv = (Tuv) sourceTuvs.get(i);
                Tuv trgTuv = (Tuv) targetTuvs.get(i);
                if(filteredSourceTuvs.size() > 0)
                {
                    if(!filteredSourceTuvs.contains(srcTuv))
                    {
                        continue;
                    }
                }
                if(filteredTargetTuvs.size() > 0)
                {
                    if(!filteredTargetTuvs.contains(trgTuv))
                    {
                        continue;
                    }
                }
                _count++;
                int readyCase = READY_CASE_3;
                if (LeverageUtil.isIncontextMatch(i, sourceTuvs,
                        targetTuvs, tuvMatchTypes, p_excludedItemTypes, jobId))
                {
                    readyCase = READY_CASE_2;
                }
                JSONObject targetj = getTargetJsonResult(readyCase, srcTuv,
                        trgTuv, options, termLMResultSet, p_excludedItemTypes,
                        targetPage, tuvMatchTypes, imageMaps, comments,
                        repetitions, p_searchMap, p_state, fromInCtxRv);
                JSONObject sourcej = getSourceJsonResult(b_rtlLocale, srcTuv,
                        p_state.getNeedShowPTags(), jobId, fromInCtxRv);
                
                if (fromInCtxRv)
                {
                    InddTuMapping mp = InddTuMappingHelper.getMappingByTu(jobId, srcTuv.getTuId());

                    if (mp != null)
                    {
                        targetj.put("pageNum", mp.getPageNum());
                        sourcej.put("pageNum", mp.getPageNum());
                    }

                    targetj.put("format", p_state.getPageFormat());
                    sourcej.put("format", p_state.getPageFormat());
                }
                
                targetjArray.put(targetj);
                sourcejArray.put(sourcej);
                
                if(needOriginalTarget)
                {
                	JSONObject originalTargetj = new JSONObject();
                	StringBuffer mainClass = new StringBuffer();
                	String dir = "";
                	String temp = "";
					if (originalTargetTuvMap.size() > 0)
					{
                		Tuv tempTuv = originalTargetTuvMap.get(trgTuv.getId());
                		if(tempTuv != null)
                		{
                	        String dataType = tempTuv.getDataType(jobId);
                			String segment = GxmlUtil.getDisplayHtml(tempTuv.getGxmlElement(),
                	                dataType, options.getViewMode());
                			temp = getEditorSegment(tempTuv,
                	                EditorConstants.PTAGS_COMPACT, segment,
                	                editorState.getNeedShowPTags(), jobId);
                			boolean b_rtlOriginalLocale = EditUtil.isRTLLocale(tempTuv.getGlobalSightLocale());
                	        // Make the segment RTL if it's 1) Translatable 2) In an RTL
                	        // language and 3) it has bidi characters in it.
                	        if (b_rtlOriginalLocale && !tempTuv.isLocalizable(jobId)
                	                && Text.containsBidiChar(tempTuv.getGxml()))
                	        {
                	            dir = "rtl ";
                	        }
                			List subflows = tempTuv.getSubflowsAsGxmlElements(true);
                	        boolean b_subflows = (subflows != null && subflows.size() > 0);
                	        if (!b_subflows)
                	        {
                	        	mainClass.append(dir);
                	        }
                	        
                	        if (b_subflows)
            	            {
            	                JSONArray subArray = new JSONArray();
            	                for (int j = 0; j < subflows.size(); j++)
            	                {
            	                    JSONObject subObj = new JSONObject();
            	                    GxmlElement subElmt = (GxmlElement) subflows.get(j);
            	                    String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
            	                    dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

            	                    // Inherit datatype from parent element...
            	                    if (dataType == null)
            	                    {
            	                        GxmlElement node = subElmt.getParent();

            	                        while (dataType == null && node != null)
            	                        {
            	                            dataType = node.getAttribute(GxmlNames.SUB_DATATYPE);
            	                            node = node.getParent();
            	                        }
            	                    }

            	                    if (dataType == null)
            	                    {
            	                        dataType = trgTuv.getDataType(jobId);
            	                    }
            	                    
            	                    segment = GxmlUtil.getDisplayHtml(subElmt, dataType,
            	                            options.getViewMode());
            	                    subObj.put("subId", subId);
            	                    subObj.put("segment", segment);
            	                    subArray.put(subObj);
            	                }
            	                originalTargetj.put("subArray", subArray);
            	            }
                		}
                	}
                	originalTargetj.put("mainstyle", mainClass);
                	originalTargetj.put("tuId", trgTuv.getTuId());
                	originalTargetj.put("originalTarget", temp);
                	originalTargetjArray.put(originalTargetj);
                	
                	JSONObject approvej = new JSONObject();
                	if(trgTuv.getState().getValue() == TuvState.APPROVED.getValue())
                	{
                		approvej.put("approve","checked");
                	}
                	else
                	{
                		approvej.put("approve","");
                	}
                	approvejArray.put(approvej);
                }
                
            }
            
            mainJson.put("target", targetjArray);
            mainJson.put("source", sourcejArray);
            if(needOriginalTarget)
            {
            	mainJson.put("original", originalTargetjArray);
            	mainJson.put("approve", approvejArray);
            	mainJson.put("currentPageNum", pi.getCurrentPageNum());
            	mainJson.put("totalPageNum", pi.getTotalPageNum());
            	mainJson.put("isFirstBatch", p_state.isFirstBatch());
            	mainJson.put("isLastBatch", p_state.isLastBatch());
            }
        }
        catch (Exception ex)
        {
            String[] args =
            { "Failed to retrieve target page view." };

            CATEGORY.error(args[0], ex);

            throw new OnlineEditorException(
                    OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW, args, ex);
        }

        return mainJson;
    }
    
    private void setOriginalTargetTuvMap(List<Tuv> filterTargetTuvs, List<Tuv> allTargetTuvs, 
    		HashMap<Long, Tuv> setOriginalTargetTuvMap, long targetLocaleId)
    {
    	HashMap<Long, List<Tuv>> tempHashMap = new HashMap<Long, List<Tuv>>();
    	for(Tuv tuv: allTargetTuvs)
    	{
    		long tuId = tuv.getTuId();
    		if(tuv.getLocaleId() == targetLocaleId && tuv.getState().equals(TuvState.OUT_OF_DATE))
    		{
    			if(tempHashMap.get(tuId) == null)
    			{
    				List<Tuv> tempTuvList = new ArrayList<Tuv>();
    				tempTuvList.add(tuv);
    				tempHashMap.put(tuId, tempTuvList);
    			}
    			else
    			{
    				tempHashMap.get(tuId).add(tuv);
    			}
    		}
    	}
    	
    	for(Tuv tuv: filterTargetTuvs)
    	{
    		long tuId = tuv.getTuId();
    		List<Tuv> tempTuvList = tempHashMap.get(tuId);
    		if(tempTuvList != null)
    		{
    			sortById(tempTuvList);
    			for(Tuv tempTuv: tempTuvList)
    			{
    				if(!tempTuv.getGxml().equals(tuv.getGxml()))
    				{
    					setOriginalTargetTuvMap.put(tuv.getId(), tempTuv);
    					break;
    				}
    			}
    		}
    	}
    }
    
    private static void sortById(List<Tuv> tempTuvList)
    {
    	if(tempTuvList.size() > 1)
    	{
    		Collections.sort(tempTuvList, new Comparator<Tuv>() 
    		{  
                public int compare(Tuv arg0, Tuv arg1) 
                {  
                    long id0 = arg0.getId();
                    long id1 = arg1.getId();  
                    if (id1 > id0) 
                    {  
                        return 1;  
                    } 
                    else if (id1 == id0) 
                    {  
                        return 0;  
                    }
                    else
                    {
                        return -1;  
                    }  
                }  
            });
    	}
    }

    // THE KEY ONEreadyCase
    private JSONObject getTargetJsonResult(int readyCase, Tuv p_srcTuv,
            Tuv p_targetTuv, RenderingOptions p_options,
            TermLeverageMatchResultSet p_termLMResultSet,
            Vector p_excludedItemTypes, TargetPage p_targetPage,
            MatchTypeStatistics p_matchTypes, Collection p_imageMaps,
            ArrayList p_comments, SegmentRepetitions p_repetitions,
            HashMap p_searchMap, EditorState p_editorState, boolean fromInCtxRv)
            throws OnlineEditorException, RemoteException, JSONException
    {
        long jobId = p_targetPage.getSourcePage().getJobId();
        p_options.setTmProfile(p_targetPage.getSourcePage().getRequest()
                .getJob().getL10nProfile().getTranslationMemoryProfile());
        Tu tu = p_targetTuv.getTu(jobId);
        long tuId = tu.getTuId();
        long tuvId = p_targetTuv.getId();
        String dataType = p_targetTuv.getDataType(jobId);
        GxmlElement elem = p_targetTuv.getGxmlElement();

        boolean reviewMode = p_options.getUiMode() == UIConstants.UIMODE_REVIEW;
        boolean reviewReadOnly = p_options.getUiMode() == UIConstants.UIMODE_REVIEW_READ_ONLY;
        boolean unlock = p_options.getEditMode() == EDITMODE_EDIT_ALL;
        // Localizables carry their own type attribute, whereas
        // segments inherit it from their parent translatable.
        String itemType = tu.getTuType();

        boolean isExcluded = SegmentProtectionManager.isTuvExcluded(elem,
                itemType, p_excludedItemTypes);

        // HTML class attribute that colors the segment in the editor
        String style = getMatchStyle(p_matchTypes, p_srcTuv, p_targetTuv,
                DUMMY_SUBID, isExcluded, unlock, p_repetitions, jobId);
        // For "localized" segment,if target is same with source,commonly
        // display as "no match" in blue color,for segment with sub,display
        // according to its LMs.
        String segment = GxmlUtil.getDisplayHtml(p_targetTuv.getGxmlElement(),
                dataType, p_options.getViewMode());
        String segmentSrc = GxmlUtil.getDisplayHtml(p_srcTuv.getGxmlElement(),
                dataType, p_options.getViewMode());
        String segmentNoTag = p_targetTuv.getGxmlElement().getTextValue().trim();
        boolean isReadOnly = false;
        boolean isReadOnlyMode = p_options.getEditMode() == EDITMODE_READ_ONLY;
        boolean isRealExactLocalized = true;
        if (STYLE_UPDATED.equals(style))
        {
            if (segment.trim().equals(segmentSrc.trim()))
            {
                List subFlows = p_targetTuv.getSubflowsAsGxmlElements();
                if (subFlows != null && subFlows.size() > 0)
                {
                    style = getMatchStyleByLM(p_matchTypes, p_srcTuv,
                            p_targetTuv, DUMMY_SUBID, unlock, p_repetitions,
                            jobId);
                }
                else
                {
                    style = STYLE_NO_MATCH;
                }
            }
            isReadOnly = false;
        }
        else
        {
            switch (readyCase)
            {
//                case 1:
//                    style = STYLE_EXACT_MATCH;
//                    break;
                case 2:
                    if (unlock)
                    {
                        style = STYLE_CONTEXT_UNLOCK;
                    }
                    else
                    {
                        style = STYLE_CONTEXT;
                    }
                    isReadOnly = !unlock;
                    break;
                case 3:
                    // style = STYLE_NO_MATCH;
                    break;
                default:
                    break;
            }

        }

        switch (readyCase)
        {
//            case 1:
//                isReadOnly = isReadOnly(p_targetTuv, p_options, jobId);
//                if (!PageHandler.isDefaultContextMatch(p_targetPage))
//                {
//                    style = STYLE_EXACT_MATCH;
//                }
//                break;
            case 2:
                if (!PageHandler.isInContextMatch(p_targetPage.getSourcePage()
                        .getRequest().getJob()))
                {
                    style = STYLE_EXACT_MATCH;
                    isReadOnly = isReadOnly(p_targetTuv, p_options, jobId);
                }
                break;
            case 3:
                isRealExactLocalized = EditorHelper.isRealExactMatchLocalied(
                        p_srcTuv, p_targetTuv, p_matchTypes, DUMMY_SUBID, jobId);
                isReadOnly = isReadOnly(p_targetTuv, p_options, jobId);
                break;
            default:
                break;
        }
        // Get the target page locale so we can set the DIR attribute
        // for right-to-left languages such as Hebrew and Arabic
        boolean b_rtlLocale = EditUtil.isRTLLocale(p_targetPage
                .getGlobalSightLocale());
        String dir = "";

        // Make the segment RTL if it's 1) Translatable 2) In an RTL
        // language and 3) it has bidi characters in it.
        if (b_rtlLocale && !p_targetTuv.isLocalizable(jobId)
                && Text.containsBidiChar(p_targetTuv.getGxml()))
        {
            dir = "rtl ";
        }

        boolean isHighLight = isHighLight(p_searchMap, p_srcTuv, p_targetTuv);

        JSONObject jsonObj = new JSONObject();
        StringBuffer mainClass = new StringBuffer();

        // add javascript to synchronize scroll bars
        // by segment id in the pop-up editor
        if (isHighLight)
        {
            mainClass.append("isHighLight ");
        }
        jsonObj.put("tuId", tuId);

        if (haveCommentForSegment(tuId, tuvId, DUMMY_SUBID, p_comments))
        {
            mainClass.append("editorComment ");
        }

        if (reviewMode || reviewReadOnly)
        {
            segment = highlightTerms(p_srcTuv, p_targetTuv, segment,
                    p_termLMResultSet, p_options.getViewMode());

        }

        jsonObj.put("tuvId", tuvId);
        jsonObj.put("subId", DUMMY_SUBID);

        List subflows = p_targetTuv.getSubflowsAsGxmlElements(true);
        boolean b_subflows = (subflows != null && subflows.size() > 0);
        // b_subflows = hasSubflows(subflows);
        if (!b_subflows)
        {
            mainClass.append(dir);
        }

        segment = SegmentProtectionManager.handlePreserveWhiteSpace(elem,
                segment, null, null);
        // make jquery to do p_needShowPTags
        String seg = getEditorSegment(p_targetTuv,
                EditorConstants.PTAGS_COMPACT, segment,
                p_editorState.getNeedShowPTags(), jobId);
        String searchText = null;
		if (!p_editorState.getNeedShowPTags())
		{
			if (p_searchMap != null)
			{
				searchText = (String) p_searchMap.get("searchText");
			}
		}
		if (searchText != null)
		{
			seg = getNewSegement(seg,searchText,"target");
		}
        jsonObj.put("segment", seg);
        if (fromInCtxRv)
        {
            jsonObj.put("segmentNoTag", segmentNoTag);
        }
        mainClass.append(style + " ");

//        if ((!reviewMode || reviewReadOnly)
//                && (isReadOnlyMode || (isReadOnly && isRealExactLocalized) || isExcluded))
        if ((!reviewMode || reviewReadOnly) && (isReadOnly || isExcluded) && !fromInCtxRv)
        {
        }
        else
        {
            mainClass.append("SE ");
        }
        
        if (p_targetTuv.isRepeated())
        {
            mainClass.append("colorRepeated ");
            // .append(UIConstants.COLOR_REPEATED).append("\" >");#575757
        }
        else if (p_targetTuv.getRepetitionOfId() > 0)
        {
            mainClass.append("colorRepetition ");
            // .append(UIConstants.COLOR_REPETITION)#FF0000
        }
        if (b_subflows)
        // Subflows
        {
            JSONArray subArray = new JSONArray();

            if (reviewMode || reviewReadOnly)
            {
                mainClass.append("reviewMode ");
            }
            mainClass
                    .append((reviewMode || reviewReadOnly) ? "COL3 " : "COL2 ");

            // now process each subflow
            List subflowsSRC = p_srcTuv.getSubflowsAsGxmlElements(true);
            for (int i = 0; i < subflows.size(); i++)
            {
                JSONObject subObj = new JSONObject();
                StringBuffer subclass = new StringBuffer();
                GxmlElement subElmt = (GxmlElement) subflows.get(i);
                GxmlElement subElmtSrc = (GxmlElement) subflowsSRC.get(i);
                String subId = subElmt.getAttribute(GxmlNames.SUB_ID);
                dataType = subElmt.getAttribute(GxmlNames.SUB_DATATYPE);

                // Inherit datatype from parent element...
                if (dataType == null)
                {
                    GxmlElement node = subElmt.getParent();

                    while (dataType == null && node != null)
                    {
                        dataType = node.getAttribute(GxmlNames.SUB_DATATYPE);
                        node = node.getParent();
                    }
                }

                // ... or from document if tuv inherits it.
                if (dataType == null)
                {
                    dataType = p_targetTuv.getDataType(jobId);
                }

                if (b_rtlLocale && isTranslatableSub(subElmt)
                        && Text.containsBidiChar(subElmt.getTextValue()))
                {
                    dir = "rtl ";
                }
                else
                {
                    dir = "";
                }

                isExcluded = SegmentProtectionManager.isTuvExcluded(subElmt,
                        itemType, p_excludedItemTypes);
                
                style = getMatchStyle(p_matchTypes, p_srcTuv,
                        p_targetTuv, subId, isExcluded, unlock,
                        p_repetitions, jobId);
                
                if (readyCase != READY_CASE_3)
                {
                    // TODO readyCase
                    style = STYLE_CONTEXT;
                }
                boolean isSubReadOnly = isReadOnly;
                if (p_options.getTmProfile() != null
                        && !p_options.getTmProfile()
                                .getIsContextMatchLeveraging())
                {
                    style = STYLE_EXACT_MATCH;
                    if (readyCase == READY_CASE_2)
                        isSubReadOnly = false;
                }
                segment = GxmlUtil.getDisplayHtml(subElmt, dataType,
                        p_options.getViewMode());

                segmentSrc = GxmlUtil.getDisplayHtml(subElmtSrc, dataType,
                        p_options.getViewMode());
                if (STYLE_UPDATED.equals(style))
                {
                	if(segment.trim().equals(segmentSrc.trim()))
        			{                		
                		if (readyCase == READY_CASE_3)
                		{
                            style = getMatchStyleByLM(p_matchTypes, p_srcTuv,
                                    p_targetTuv, subId, unlock, p_repetitions,
                                    jobId);
                		}
                		else
                		{
                			style = STYLE_NO_MATCH;
                			if (readyCase == READY_CASE_2)
                				isSubReadOnly = false;
                		}
        			}
                }
                subclass.append(style + " ");
                subObj.put("subId", subId);
                // result.append(getSubIdColumn(tuId, subId));

                if (reviewMode || reviewReadOnly)
                {
                    subclass.append("editorComment ");

                    segment = highlightTerms(p_srcTuv, p_targetTuv, segment,
                            p_termLMResultSet, p_options.getViewMode());
                }
				if (!p_editorState.getNeedShowPTags())
				{
					if (searchText != null)
					{
						segment = getNewSegement(segment, searchText,"target");
					}
				}
                subObj.put("segment", segment);
                // If the TUV is read-only, or the sub is
                // excluded, don't show the sub as editable.
                if ((!reviewMode || reviewReadOnly)
                        && (isSubReadOnly || isExcluded))
                {
                }
                else
                {
                    subObj.put("tuvId", tuvId);
                }

                subObj.put("subclass", subclass);
                subArray.put(subObj);
            }
            jsonObj.put("subArray", subArray);
        }
        jsonObj.put("mainstyle", mainClass);
        return jsonObj;

    }

    private boolean isReadOnly(Tuv p_targetTuv, RenderingOptions p_options,
            long p_jobId)
    {
        return p_options.getEditMode() == EDITMODE_READ_ONLY
                || (p_options.getEditMode() == EDITMODE_DEFAULT && EditHelper
                        .isTuvInProtectedState(p_targetTuv, p_jobId));
    }

	private String highlightSearchText(String searchText, String segment,
			int startPos)
	{
		List<Integer> allIndexs = findAllIndex(searchText, segment, startPos);
		StringBuffer buffer = new StringBuffer();
		int fontIndex = 0;
		String begin = null;
		String end = null;
		String middle = null;
		for (int i = 0; i < allIndexs.size(); i++)
		{
			int index = allIndexs.get(i);
			if (i == 0)
			{
				begin = segment.substring(fontIndex, index);
				buffer.append(begin);
				middle = segment.substring(index, index + searchText.length());
				buffer.append("<span class=\"searchText\">" + middle
						+ "</span>");
				if (i == allIndexs.size() - 1)
				{
					end = segment.substring(index + searchText.length(),
							segment.length());
					buffer.append(end);
				}
			}
			else if (i > 0 && i == allIndexs.size() - 1)
			{
				begin = segment.substring(fontIndex + searchText.length(),
						index);
				middle = segment.substring(index, index + searchText.length());
				end = segment.substring(index + searchText.length(),
						segment.length());
				buffer.append(begin);
				buffer.append("<span class=\"searchText\">" + middle
						+ "</span>");
				buffer.append(end);
			}
			else
			{
				begin = segment.substring(fontIndex + searchText.length(),
						index);
				middle = segment.substring(index, index + searchText.length());
				buffer.append(begin);
				buffer.append("<span class=\"searchText\">" + middle
						+ "</span>");
			}
			fontIndex = index;
		}

		if (buffer.toString().length() > 0)
			return buffer.toString();
		return null;
	}

	private List<Integer> findAllIndex(String searchText, String segment,
			int startPos)
	{
		int foundPos = -1; // -1 represents not found.
		List<Integer> foundIndexs = new ArrayList<Integer>();
		do
		{
			foundPos = segment.toLowerCase().indexOf(searchText.toLowerCase(),
					startPos);
			if (foundPos > -1)
			{
				startPos = foundPos + 1;
				foundIndexs.add(foundPos);
			}
		}
		while (foundPos > -1 && startPos < segment.length());
		
		return foundIndexs;
	}

	private String getNewSegement(String segment, String searchText,
			String locale)
	{
		String beginSpan = "<span class=\"editorSegmentInternal\">";
		String endSpan = "</span>";
		if (segment.contains(beginSpan) && segment.contains(endSpan))
		{
			List<Integer> beginSpanIndexs = findAllIndex(beginSpan, segment, 0);
			List<Integer> endSpanIndexs = findAllIndex(endSpan, segment, 0);
			StringBuffer returnSeg = new StringBuffer();
			String beginSegment = null;
			String middleSegement = null;
			String endSegment = null;
			String begin = null;
			String end = null;
			String middle = null;
			int frontEndIndex = -1;
			for (int i = 0; i < beginSpanIndexs.size(); i++)
			{
				int beginSpanIndex = beginSpanIndexs.get(i);
				int endSpanIndex = endSpanIndexs.get(i);
				if (i == 0)
				{
					beginSegment = segment.substring(0, beginSpanIndex);
					begin = highlightSearchText(searchText, beginSegment, 0);
					if (begin == null)
					{
						returnSeg.append(beginSegment);
					}
					else
					{
						returnSeg.append(begin);
					}
					middleSegement = segment.substring(beginSpanIndex,
							endSpanIndex + endSpan.length());
					if (locale.equals("source"))
					{
						String middleStr = segment.substring(
								beginSpanIndex+beginSpan.length(), endSpanIndex);
						middle = highlightSearchText(searchText, middleStr, 0);
						if (middle != null)
						{
							returnSeg.append(middle);
						}
						else
						{
							returnSeg.append(middleSegement);
						}
					}
					else if (locale.equals("target"))
					{
						returnSeg.append(middleSegement);
					}

					if (i == beginSpanIndexs.size() - 1)
					{
						endSegment = segment.substring(
								endSpanIndex + endSpan.length(),
								segment.length());
						end = highlightSearchText(searchText, endSegment, 0);
						if (end == null)
						{
							returnSeg.append(endSegment);
						}
						else
						{
							returnSeg.append(end);
						}
					}
				}
				else if (i == beginSpanIndexs.size() - 1)
				{
					beginSegment = segment.substring(
							frontEndIndex + endSpan.length(), beginSpanIndex);
					begin = highlightSearchText(searchText, beginSegment, 0);
					if (begin == null)
					{
						returnSeg.append(beginSegment);
					}
					else
					{
						returnSeg.append(begin);
					}
					middleSegement = segment.substring(beginSpanIndex,
							endSpanIndex + endSpan.length());
					if (locale.equals("source"))
					{
						String middleStr = segment.substring(
								beginSpanIndex+beginSpan.length(), endSpanIndex);
						middle = highlightSearchText(searchText, middleStr, 0);
						if (middle != null)
						{
							returnSeg.append(middle);
						}
						else
						{
							returnSeg.append(middleSegement);
						}
					}
					else if (locale.equals("target"))
					{
						returnSeg.append(middleSegement);
					}
					endSegment = segment.substring(
							endSpanIndex + endSpan.length(), segment.length());
					end = highlightSearchText(searchText, endSegment, 0);
					if (end == null)
					{
						returnSeg.append(endSegment);
					}
					else
					{
						returnSeg.append(end);
					}
				}
				else
				{
					beginSegment = segment.substring(
							frontEndIndex + endSpan.length(), beginSpanIndex);
					begin = highlightSearchText(searchText, beginSegment, 0);
					if (begin == null)
					{
						returnSeg.append(beginSegment);
					}
					else
					{
						returnSeg.append(begin);
					}
					middleSegement = segment.substring(beginSpanIndex,
							endSpanIndex + endSpan.length());
					if (locale.equals("source"))
					{
						String middleStr = segment.substring(
								beginSpanIndex+beginSpan.length(), endSpanIndex);
						middle = highlightSearchText(searchText, middleStr, 0);
						if (middle != null)
						{
							returnSeg.append(middle);
						}
						else
						{
							returnSeg.append(middleSegement);
						}
					}
					else if (locale.equals("target"))
					{
						returnSeg.append(middleSegement);
					}
				}
				frontEndIndex = endSpanIndex;
			}

			return returnSeg.toString();
		}
		else
		{
			String returnStr = highlightSearchText(searchText, segment, 0);
			if (returnStr != null)
				return highlightSearchText(searchText, segment, 0);
			return segment;
		}
	}
}
