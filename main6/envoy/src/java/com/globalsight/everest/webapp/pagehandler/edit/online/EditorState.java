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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.globalsight.everest.edit.SynchronizationStatus;
import com.globalsight.everest.edit.online.CommentThreadView;
import com.globalsight.everest.edit.online.OnlineEditorConstants;
import com.globalsight.everest.edit.online.OnlineEditorManager;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * <p>A helper class that combines all state variables necessary for
 * the editor to do his work into one object.</p>
 *
 * @see EditorConstants
 */
public class EditorState extends PersistentObject implements EditorConstants
{
	private static final long serialVersionUID = -4984421987208974274L;

	//
    // Local Classes
    //
    public static class PagePair
    {
        public Long m_sourcePageId;
        public GlobalSightLocale m_sourceLocale;
        public String m_pageName;
        public boolean m_hasGsaTags;
        public String m_workflowState;

        private HashMap<String, Long> m_target = new HashMap<String, Long>();
        private HashMap<Long, GlobalSightLocale> m_targetIds = new HashMap<Long, GlobalSightLocale>();

        public PagePair (Long p_srcPageId, GlobalSightLocale p_srcLocale,
            String p_pageName, boolean p_hasGsaTags, String p_workflowState)
        {
            m_sourcePageId = p_srcPageId;
            m_sourceLocale = p_srcLocale;
            m_pageName = p_pageName;
            m_hasGsaTags = p_hasGsaTags;
            m_workflowState = p_workflowState;
        }

        public PagePair (long p_srcPageId, GlobalSightLocale p_srcLocale,
            String p_pageName, boolean p_hasGsaTags, String p_workflowState)
        {
            m_sourcePageId = new Long(p_srcPageId);
            m_sourceLocale = p_srcLocale;
            m_pageName = p_pageName;
            m_hasGsaTags = p_hasGsaTags;
            m_workflowState = p_workflowState;
        }

        public Long getSourcePageId()
        {
            return m_sourcePageId;
        }

        public String getPageName()
        {
            return m_pageName;
        }

        public boolean hasGsaTags()
        {
            return m_hasGsaTags;
        }

        public String getWorkflowState()
        {
            return m_workflowState;
        }

        public void putTargetPage(GlobalSightLocale p_trgLocale,
            Long p_trgPageId)
        {
            m_target.put(p_trgLocale.toString(), p_trgPageId);
            m_targetIds.put(p_trgPageId, p_trgLocale);
        }

        public Long getTargetPageId(GlobalSightLocale p_trgLocale)
        {
            return (Long)m_target.get(p_trgLocale.toString());
        }

        public GlobalSightLocale getTargetPageLocale(Long p_trgPageId)
        {
            return (GlobalSightLocale)(m_targetIds.get(p_trgPageId));
        }

        public String toString()
        {
            return "PagePair(" + m_pageName +
                " srcPageId=" + m_sourcePageId +
                " srcLocale=" + m_sourceLocale.toString() + " " +
                m_target.size() + " targetPages " + m_target.keySet() + ")";
        }
    }

    public static class LinkStyles
    {
        public String m_A_color;
        public String m_A_active;
        public String m_A_visited;

        public LinkStyles(String p_color, String p_active, String p_visited)
        {
            m_A_color = p_color;
            m_A_active = p_active;
            m_A_visited = p_visited;
        }
    }


    /**
     * These are user-settable options that are stored as UserParameters.
     */
    public static class Options
    {
        public boolean m_autosave = false;
        public boolean m_autounlock = false;
        public boolean m_autosync = true;  
        public boolean m_autoadjustws = false;
        public String m_layout = null;
        public String m_ptagmode = null;
        public boolean m_hilitePtags = true;
        public boolean m_showMt = false;
        public boolean m_iterateSubs = false;
        public int m_tmMatchingThreshold = 70;
        public int m_tbMatchingThreshold = 70;

        public Options()
        {
        }

        public void setAutoSave(boolean p_value)
        {
            m_autosave = p_value;
        }

        public boolean getAutoSave()
        {
            return m_autosave;
        }

        public void setAutoUnlock(boolean p_value)
        {
            m_autounlock = p_value;
        }

        public boolean getAutoUnlock()
        {
            return m_autounlock;
        }

        public void setAutoSync(boolean p_value)
        {
            m_autosync = p_value;
        }

        public boolean getAutoSync()
        {
            return m_autosync;
        }

        public void setAutoAdjustWhitespace(boolean p_value)
        {
            m_autoadjustws = p_value;
        }

        public boolean getAutoAdjustWhitespace()
        {
            return m_autoadjustws;
        }

        public void setLayout(String p_value)
        {
            m_layout = p_value;
        }

        public String getLayout()
        {
            return m_layout;
        }

        public String getPTagMode()
        {
            return m_ptagmode;
        }

        public void setPTagMode(String p_value)
        {
            m_ptagmode = p_value;
        }

        public void setHilitePtags(boolean p_value)
        {
            m_hilitePtags = p_value;
        }

        public boolean getHilitePtags()
        {
            return m_hilitePtags;
        }

        public void setShowMt(boolean p_value)
        {
            m_showMt = p_value;
        }

        public boolean getShowMt()
        {
            return m_showMt;
        }

        public void setIterateSubs(boolean p_value)
        {
            m_iterateSubs = p_value;
        }

        public boolean getIterateSubs()
        {
            return m_iterateSubs;
        }

        public void setTmMatchingThreshold(int p_value)
        {
            m_tmMatchingThreshold = p_value;
        }

        public int getTmMatchingThreshold()
        {
            return m_tmMatchingThreshold;
        }

        public void setTbMatchingThreshold(int p_value)
        {
            m_tbMatchingThreshold = p_value;
        }

        public int getTbMatchingThreshold()
        {
            return m_tbMatchingThreshold;
        }
    }

    /**
     * Main Editor Layout Options.
     */
    public static class Layout
    {
        /**
         * Display single page or source and target together?  Default is
         * false.
         */
        private boolean m_singlePage = false;

        /**
         * Display source or target as single page?  Default is false.
         */
        private boolean m_singlePageIsSource = false;

        /**
         * Split 2 windows horizontally or vertically?  Default is vertical.
         */
        private boolean m_horizontal = false;

        /**
         * Current source page view mode (Preview, Text, Detail).
         * Default is VIEWMODE_DETAIL.
         */
        private int m_sourceViewMode = VIEWMODE_DETAIL;

        /**
         * Current target page view mode (Preview, Text, Detail).
         * Default is VIEWMODE_DETAIL.
         */
        private int m_targetViewMode = VIEWMODE_DETAIL;

        //
        // Constructor
        //
        public Layout()
        {
            // members have default values
        }

        public void setLayout(String p_layout)
        {
            if (p_layout != null)
            {
                if (p_layout.equals("source"))
                {
                    m_singlePage = true;
                    m_singlePageIsSource = true;
                }
                else if (p_layout.equals("target"))
                {
                    m_singlePage = true;
                    m_singlePageIsSource = false;
                }
                else if (p_layout.equals("source_target_vertical"))
                {
                    m_singlePage = false;
                    m_horizontal = false;
                }
                else if (p_layout.equals("source_target_horizontal"))
                {
                    m_singlePage = false;
                    m_horizontal = true;
                }
            }
        }

        public void setViewmode(int p_viewmode)
        {
            if (p_viewmode > 0 && p_viewmode <= VIEWMODE_MAX)
            {
                m_sourceViewMode = p_viewmode;
                m_targetViewMode = p_viewmode;
            }
        }

        //
        // Public Methods
        //

        public boolean isSinglePage()
        {
            return m_singlePage;
        }

        public void setSinglePage(boolean p_singlePage)
        {
            m_singlePage = p_singlePage;
        }

        public void setSinglePage(int p_singlePage)
        {
            m_singlePage = (p_singlePage == SINGLE_PAGE ? true : false);
        }

        public boolean singlePageIsSource()
        {
            return m_singlePageIsSource;
        }

        public void setSinglePageIsSource(boolean p_singlePageIsSource)
        {
            m_singlePageIsSource = p_singlePageIsSource;
        }

        public void setSinglePageIsSource(int p_singlePageIsSource)
        {
            m_singlePageIsSource =
                (p_singlePageIsSource == SINGLE_PAGE_IS_SOURCE ? true : false);
        }

        public boolean isHorizontal()
        {
            return m_horizontal;
        }

        public void setHorizontal(boolean p_horizontal)
        {
            m_horizontal = p_horizontal;
        }

        public void setHorizontal(int p_horizontal)
        {
            m_horizontal = (p_horizontal == SPLIT_HORIZONTALLY ? true : false);
        }

        public int getSourceViewMode()
        {
            return m_sourceViewMode;
        }

        public void setSourceViewMode(int p_viewMode)
        {
            m_sourceViewMode = p_viewMode;
        }

        public int getTargetViewMode()
        {
            return m_targetViewMode;
        }

        public void setTargetViewMode(int p_viewMode)
        {
            m_targetViewMode = p_viewMode;
        }
    }

    //
    // Member Variables
    //

    /**
     * Flag indicating if the user is a project manager or
     * localization participant.
     */
    private boolean m_userIsPm = false;

    /**
     * Reference to our OnlineEditorManagerLocal instance holding
     * cached objects for better performance (pages, tus, tuvs etc).
     */
    private OnlineEditorManager m_editorManager = null;

    /**
     * User-definable editor options (user parameters).
     */
    private Options m_options = null;

    /**
     * Main Editor Layout Options.
     */
    private Layout m_layout = null;

    /**
     * uiMode, viewMode, editMode for page rendering.
     */
    private RenderingOptions m_renderOptions = null;


    /**
     * Show target page read-only or read-write?  Default is false.
     */
    private boolean m_readOnly = false;

    /**
     * Controls editing of snippets. Default is false.
     */
    private boolean m_canEditSnippets = false;

    /** Edit all segments or just the default segments? */
    private int m_editAll = EDIT_DEFAULT;

    /** Does the L10nProfile allow editing of locked segments? */
    private boolean m_canEditAll = true;

    /** Has the administrator allowed to show the MT button? */
    private boolean m_canShowMt = false;

    /**
     * PTag display mode: verbose or compact.
     * Default is PTAGS_COMPACT.
     */
    private String m_ptagFormat = PTAGS_COMPACT;

    /**
     * Need a variable holding the current excludedItemTypes.
     */
    private Vector<String> m_excludedItems = null;

    /**
     * Lists of pages (PagePair objects) the editor can show.  These
     * lists are merely stored here for perusal by the PageHandler.
     */
    private ArrayList<EditorState.PagePair> m_pages = null;

    /** Current page pair to show */
    private PagePair m_currentPage = null;

    /**
     * Flags indicating if the current page is the first or last page
     * in the page list.
     */
    private boolean m_isFirstPage = false;
    private boolean m_isLastPage = false;

    /** The page's original data format. */
    private String m_pageFormat = null;

    /** Array of HTML codes for source page views (a UI layer cache). */
    private ArrayList<String> m_sourcePageHtml = new ArrayList<String>(VIEWMODE_MAX);

    /** HTML code for target page view */
    private String m_targetPageHtml = null;

    /** PageInfo record (page name, data source type, etc). */
    private PageInfo m_pageInfo = null;

    /** SynchronizationStatus for notifications of offline uploads. */
    private SynchronizationStatus m_oldSyncStatus = null;
    private SynchronizationStatus m_newSyncStatus = null;

    private boolean m_isReviewActivity = false;

    /** Segment Comment Threads for review mode. */
    private CommentThreadView m_commentThreads = null;

    /**
     * List of TU ids in the current page pair (shared by source & target).
     */
    private ArrayList<Long> m_tuIds = null;

    /** Current TU being edited in Segment Editor */
    private long m_tuId = 0;

    /** Current TUV being edited in Segment Editor */
    private long m_tuvId = 0;

    /** Current sub segment being edited in Segment Editor */
    private long m_subId = 0;


    /**
     * Flag for selecting the segment editor or image editor (or
     * color, font editor etc) based on the current segment's type.
     */
    private int m_editorType = SE_SEGMENTEDITOR;

    /**
     * Flag indicating whether the editor is in view-mode.
     */
    private int m_editorMode = EDITORMODE_VIEWER;

    /**
     * The link colors to be put into the target page's stylesheet.
     */
    private LinkStyles m_linkStyles = null;

    /** SHOULD BE NULL OR INITIALIZED FROM A STATIC EMPTY VECTOR */
    private Vector<GlobalSightLocale> m_jobTargetLocales = new Vector<GlobalSightLocale>();

    /**
     * Stores the locale being viewed in the target page.
     */
    private GlobalSightLocale m_targetViewLocale = null;
    
    private GlobalSightLocale m_allTargetViewLocale = null;

    /**
     * Holds the name of the project's default termbase.
     */
    private String m_defaultTermbaseName = null;

    /**
     * Can access the TB or not.
     */
    private boolean canAccessTB = true;

    /**
     * Holds the id of the project's default termbase.
     */
    private long m_defaultTermbaseId = 0;

    /**
     * Holds the XML string of termbase names.
     *
     * See com.globalsight.terminology.ITermbaseManager.getTermbases().
     */
    private String m_termbaseNames = null;

    /**
     * List of TM names associated with this page's associated TM Profile
     */
    private String[] m_tmNames = null;

    /**
     * The related tmProfile
     */
    private TranslationMemoryProfile tmProfile;

    private String userName;
    
    /**
     * PaginateInfo for popup editor and inline editor
     */
    private PaginateInfo paginateInfo = null;
	private boolean isFirstBatch = true;
	private boolean isLastBatch = true;
	private String needUpdatePopUpEditor = null;

	private boolean needFindRepeatedSegments = false;
	private boolean needShowPTags = false;
	private String segmentFilter = OnlineEditorConstants.SEGMENT_FILTER_ALL;

	//
    // Constructors
    //
	public EditorState()
    {
        // The sourcePageHtml cache needs to be filled with dummies.
        for (int i = 0; i < VIEWMODE_MAX; ++i)
        {
            m_sourcePageHtml.add(null);
        }
    }

    //
    // Public methods
    //

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserIsPm(boolean p_flag)
    {
        m_userIsPm = p_flag;
    }

    public boolean getUserIsPm()
    {
        return m_userIsPm;
    }

    public void setEditorManager(OnlineEditorManager p_manager)
    {
        m_editorManager = p_manager;
    }

    public OnlineEditorManager getEditorManager()
    {
        return m_editorManager;
    }

    public void setOptions(Options p_options)
    {
        m_options = p_options;
    }

    public Options getOptions()
    {
        return m_options;
    }

    public void setLayout(Layout p_layout)
    {
        m_layout = p_layout;
    }

    public Layout getLayout()
    {
        return m_layout;
    }

    public void setRenderingOptions(RenderingOptions p_options)
    {
        m_renderOptions = p_options;
    }

    public RenderingOptions getRenderingOptions()
    {
        return m_renderOptions;
    }

    public void setEditorMode()
    {
        m_editorMode = EDITORMODE_EDITOR;
    }

    public void setViewerMode()
    {
        m_editorMode = EDITORMODE_VIEWER;
    }

    public void setReviewMode()
    {
        m_editorMode = EDITORMODE_REVIEW;
    }

    public boolean isEditorMode()
    {
        return m_editorMode == EDITORMODE_EDITOR;
    }

    public boolean isViewerMode()
    {
        return m_editorMode == EDITORMODE_VIEWER;
    }

    public boolean isReviewMode()
    {
        return m_editorMode == EDITORMODE_REVIEW;
    }

    public boolean canEditSnippets()
    {
        return m_canEditSnippets;
    }

    public void setAllowEditSnippets(boolean p_flag)
    {
        m_canEditSnippets = p_flag;
    }

    public boolean canEditAll()
    {
        return m_canEditAll;
    }

    public void setAllowEditAll(boolean p_flag)
    {
        m_canEditAll = p_flag;
    }

    public boolean isEditAll()
    {
        return m_editAll == EDIT_ALL;
    }

    public int getEditAllState()
    {
        return m_editAll;
    }

    public void setEditAllState(int p_state)
    {
        m_editAll = p_state;
    }

    public void setIsReviewActivity(boolean p_flag)
    {
        m_isReviewActivity = p_flag;
    }

    public boolean getIsReviewActivity()
    {
        return m_isReviewActivity;
    }

    public void setCanShowMt(boolean p_value)
    {
        m_canShowMt = p_value;
    }

    public boolean canShowMt()
    {
        return m_canShowMt;
    }

    public boolean isReadOnly()
    {
        return m_readOnly;
    }

    public void setReadOnly(boolean p_readOnly)
    {
        m_readOnly = p_readOnly;
    }

    public void setReadOnly(int p_readOnly)
    {
        m_readOnly = (p_readOnly == READ_ONLY ? true : false);
    }

    public GlobalSightLocale getTargetLocale()
    {
        return getTargetViewLocale();
    }

    public GlobalSightLocale getTargetViewLocale()
    {
        return m_targetViewLocale;
    }

    public void setTargetViewLocale(GlobalSightLocale p_targetViewLocale)
    {
        m_targetViewLocale = p_targetViewLocale;
    }

    public Long getSourcePageId()
    {
        return m_currentPage.getSourcePageId();
    }

    public String getSourcePageName()
    {
        return m_currentPage.getPageName();
    }
    
    /**
     * Returns the name of the source page. This is just the last name in the
     * pathname's name sequence. If the pathname's name sequence is empty, then
     * the empty string is returned.
     * 
     * @return The name of thesource page.
     */
    public String getSimpleSourcePageName()
    {
        String fileName = getSourcePageName();
        String subName = "";
        
        int index = fileName.indexOf(")");
        if (index > 0 && fileName.startsWith("("))
        {
            subName = fileName.substring(0, fileName.indexOf(")") + 1);
        }

        File file = new File(getSourcePageName());
        
        return file.getName() + subName;
    }

    public Long getTargetPageId()
    {
        return m_currentPage.getTargetPageId(getTargetViewLocale());
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_currentPage.m_sourceLocale;
    }

    public boolean hasGsaTags()
    {
        return m_currentPage.hasGsaTags();
    }

    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_currentPage.m_sourceLocale = p_locale;
    }

    public void setJobTargetLocales(Vector<GlobalSightLocale> p_locales)
    {
        m_jobTargetLocales = p_locales;
    }

    public Vector<GlobalSightLocale> getJobTargetLocales()
    {
        return m_jobTargetLocales;
    }

    public String getPageFormat()
    {
        return m_pageFormat;
    }

    public void setPageFormat(String p_pageFormat)
    {
        m_pageFormat = p_pageFormat;
    }

    public String getSourcePageHtml(int p_viewMode)
    {
        return (String)m_sourcePageHtml.get(p_viewMode - 1);
    }

    public void setSourcePageHtml(int p_viewMode, String p_html)
    {
        m_sourcePageHtml.set(p_viewMode - 1, p_html);
    }

    public void clearSourcePageHtml()
    {
        for (int i = 0; i < VIEWMODE_MAX; ++i)
        {
            m_sourcePageHtml.set(i, null);
        }
    }

    public String getTargetPageHtml()
    {
        return m_targetPageHtml;
    }

    public void setTargetPageHtml(String p_html)
    {
        m_targetPageHtml = p_html;
    }

    public void setPageInfo(PageInfo p_info)
    {
        m_pageInfo = p_info;
    }

    public PageInfo getPageInfo()
    {
        return m_pageInfo;
    }


    public void clearSynchronizationStatus()
    {
        m_oldSyncStatus = null;
        m_newSyncStatus = null;
    }

    public void setOldSynchronizationStatus(SynchronizationStatus p_arg)
    {
        m_oldSyncStatus = p_arg;
    }

    public void setNewSynchronizationStatus(SynchronizationStatus p_arg)
    {
        m_newSyncStatus = p_arg;
    }

    public SynchronizationStatus getOldSynchronizationStatus()
    {
        return m_oldSyncStatus;
    }

    public SynchronizationStatus getNewSynchronizationStatus()
    {
        return m_newSyncStatus;
    }

    public CommentThreadView getCommentThreads()
    {
        return m_commentThreads;
    }

    public void setCommentThreads(CommentThreadView p_arg)
    {
        m_commentThreads = p_arg;
    }

    public void setTuId(long p_tuId)
    {
        m_tuId = p_tuId;
    }

    public long getTuId()
    {
        return m_tuId;
    }

    public void setTuvId(long p_tuvId)
    {
        m_tuvId = p_tuvId;
    }

    public long getTuvId()
    {
        return m_tuvId;
    }

    public void setSubId(long p_subId)
    {
        m_subId = p_subId;
    }

    public long getSubId()
    {
        return m_subId;
    }

    public boolean isFirstPage()
    {
        return m_isFirstPage;
    }

    public void setIsFirstPage(boolean p_bool)
    {
        m_isFirstPage = p_bool;
    }

    public boolean isLastPage()
    {
        return m_isLastPage;
    }

    public void setIsLastPage(boolean p_bool)
    {
        m_isLastPage = p_bool;
    }

    public int getEditorType()
    {
        return m_editorType;
    }

    public void setEditorType(int p_type)
    {
        m_editorType = p_type;
    }

    public PagePair getCurrentPage()
    {
        return m_currentPage;
    }

    public void setCurrentPage(PagePair p_pair)
    {
        m_currentPage = p_pair;
        SourcePage sourcePage = 
            HibernateUtil.get(SourcePage.class, m_currentPage.getSourcePageId());
        if (sourcePage != null)
        {
            Vector<GlobalSightLocale> locales = new Vector<GlobalSightLocale>();
            for (TargetPage t : sourcePage.getTargetPages())
            {
                String wfState = (t.getWorkflowInstance() == null ? null : t
                        .getWorkflowInstance().getState());
                // Skip workflows that have failed or been canceled.
                // See "EditorHelper.setJobTargetLocales(...)".
                if (wfState == null || Workflow.CANCELLED.equals(wfState)
                        || Workflow.IMPORT_FAILED.equals(wfState))
                {
                    continue;
                }

                GlobalSightLocale locale = t.getGlobalSightLocale();
                locale.getLocale();
                locales.add(locale);
            }
            
            setJobTargetLocales(locales);
            
            if (locales.size() == 1)
            {
                setTargetViewLocale((GlobalSightLocale) locales.get(0));
            }
           
        }
    }

    public ArrayList<EditorState.PagePair> getPages()
    {
        return m_pages;
    }

    public void setPages(ArrayList<EditorState.PagePair> p_pages)
    {
        m_pages = p_pages;
    }

    public ArrayList<Long> getTuIds()
    {
        return m_tuIds;
    }

    public void setTuIds(ArrayList<Long> p_tuIds)
    {
        m_tuIds = p_tuIds;
    }

    public synchronized void setLinkStyles(String p_color, String p_active,
        String p_visited)
    {
        m_linkStyles = new LinkStyles(p_color, p_active, p_visited);
    }

    public synchronized void setLinkStyles(LinkStyles p_styles)
    {
        m_linkStyles = p_styles;
    }

    public synchronized LinkStyles getLinkStyles()
    {
        return m_linkStyles;
    }

    public void setExcludedItems(Vector<String> p)
    {
        m_excludedItems = p;
    }

    public Vector<String> getExcludedItems()
    {
        return m_excludedItems;
    }

    public String getPTagFormat()
    {
        return m_ptagFormat;
    }

    public void setPTagFormat(String p_format)
    {
        m_ptagFormat = p_format;
    }

    public String getDefaultTermbaseName()
    {
        return m_defaultTermbaseName;
    }

    public void setDefaultTermbaseName(String p_name)
    {
        m_defaultTermbaseName = p_name;
    }

    public boolean isCanAccessTB()
    {
        return canAccessTB;
    }

    public void setCanAccessTB(boolean canAccessTB)
    {
        this.canAccessTB = canAccessTB;
    }

    public long getDefaultTermbaseId()
    {
        return m_defaultTermbaseId;
    }

    public void setDefaultTermbaseId(long p_id)
    {
        m_defaultTermbaseId = p_id;
    }

    public String getTermbaseNames()
    {
        return m_termbaseNames;
    }

    public void setTermbaseNames(String p_names)
    {
        m_termbaseNames = p_names;
    }

    public String[] getTmNames()
    {
        return m_tmNames;
    }

    public void setTmNames(String[] p_tmNames)
    {
        m_tmNames = p_tmNames;
    }

    public TranslationMemoryProfile getTmProfile() {
        return tmProfile;
    }

    public void setTmProfile(TranslationMemoryProfile tmProfile) {
        this.tmProfile = tmProfile;
    }

	public PaginateInfo getPaginateInfo() {
		return paginateInfo;
	}

	public void setPaginateInfo(PaginateInfo paginateInfo) {
		this.paginateInfo = paginateInfo;
	}

	public boolean isFirstBatch() 
	{
		if ( paginateInfo.getCurrentPageNum() == 1) 
		{
			isFirstBatch = true;
		}
		else 
		{
			isFirstBatch = false;
		}
		
		return isFirstBatch;
	}
	
	public boolean isLastBatch() 
	{
		if (paginateInfo.getCurrentPageNum() == paginateInfo.getTotalPageNum()) 
		{
			isLastBatch = true;
		}
		else
		{
			isLastBatch = false;
		}
		
		return isLastBatch;
	}
	
    public String getNeedUpdatePopUpEditor() 
    {
		return needUpdatePopUpEditor;
	}

	public void setNeedUpdatePopUpEditor(String needUpdatePopUpEditor) 
	{
		this.needUpdatePopUpEditor = needUpdatePopUpEditor;
	}
	
	public boolean getNeedFindRepeatedSegments() {
	    return this.needFindRepeatedSegments;
	}
	
	public void setNeedFindRepeatedSegments(boolean p_needFindRepeatedSegments) {
	    this.needFindRepeatedSegments = p_needFindRepeatedSegments;
	}
	
    public GlobalSightLocale getAllTargetViewLocale()
    {
        return m_allTargetViewLocale;
    }

    public void setAllTargetViewLocale(GlobalSightLocale m_allTargetViewLocale)
    {
        this.m_allTargetViewLocale = m_allTargetViewLocale;
    }

    public boolean getNeedShowPTags()
    {
        return needShowPTags;
    }

    public void setNeedShowPTags(boolean needShowPTags)
    {
        this.needShowPTags = needShowPTags;
    }
    
    public String getSegmentFilter()
    {
        return segmentFilter;
    }

    public void setSegmentFilter(String segmentFilter)
    {
        this.segmentFilter = segmentFilter;
    }
    
    /**
     * Clone state object manually.
     * 
     * @param p_state
     * @return
     */
    public static EditorState cloneState(EditorState p_state)
    {
        EditorState result = new EditorState();
        if (p_state == null)
        {
            return null;
        }
        
        result.setUserIsPm(p_state.getUserIsPm());
        result.setEditorManager(p_state.getEditorManager());
        result.setOptions(p_state.getOptions());
        result.setLayout(p_state.getLayout());
        result.setRenderingOptions(p_state.getRenderingOptions());
        result.setReadOnly(p_state.isReadOnly());
        result.setAllowEditSnippets(p_state.canEditSnippets());
        result.setEditAllState(p_state.getEditAllState());
        result.setAllowEditAll(p_state.canEditAll());
        result.setCanShowMt(p_state.canShowMt());
        result.setPTagFormat(p_state.getPTagFormat());
        result.setExcludedItems(p_state.getExcludedItems());
        result.setPages(p_state.getPages());
        result.setCurrentPage(p_state.getCurrentPage());
        result.setIsFirstPage(p_state.isFirstPage());
        result.setIsLastPage(p_state.isLastPage());
        result.setPageFormat(p_state.getPageFormat());
        result.m_sourcePageHtml = p_state.m_sourcePageHtml;//
        result.setTargetPageHtml(p_state.getTargetPageHtml());
        result.setPageInfo(p_state.getPageInfo());
        result.setOldSynchronizationStatus(p_state.getOldSynchronizationStatus());
        result.setNewSynchronizationStatus(p_state.getNewSynchronizationStatus());
        result.setIsReviewActivity(p_state.getIsReviewActivity());
        result.setCommentThreads(p_state.getCommentThreads());
        result.setTuIds(p_state.getTuIds());
        result.setTuId(p_state.getTuId());
        result.setTuvId(p_state.getTuvId());
        result.setSubId(p_state.getSubId());
        result.setEditorType(p_state.getEditorType());
        result.m_editorMode = p_state.m_editorMode;//
        result.setLinkStyles(p_state.getLinkStyles());
        result.setJobTargetLocales(p_state.getJobTargetLocales());
        result.setTargetViewLocale(p_state.getTargetViewLocale());
        result.setDefaultTermbaseName(p_state.getDefaultTermbaseName());
        result.setDefaultTermbaseId(p_state.getDefaultTermbaseId());
        result.setTermbaseNames(p_state.getTermbaseNames());
        result.setTmNames(p_state.getTmNames());
        result.setTmProfile(p_state.getTmProfile());
        result.setUserName(p_state.getUserName());
        result.setPaginateInfo(p_state.getPaginateInfo());
        result.setNeedUpdatePopUpEditor(p_state.getNeedUpdatePopUpEditor());
        result.setNeedFindRepeatedSegments(p_state.getNeedFindRepeatedSegments());
        result.setNeedShowPTags(p_state.getNeedShowPTags());
        result.setSegmentFilter(p_state.getSegmentFilter());
        
        return result;
    }

    public long getJobId()
    {
        return BigTableUtil.getJobBySourcePageId(this.getSourcePageId()).getId();
    }
}
