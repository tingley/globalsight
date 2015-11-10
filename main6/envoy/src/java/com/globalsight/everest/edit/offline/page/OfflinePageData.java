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
package com.globalsight.everest.edit.offline.page;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistoryImpl;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.download.omegat.OmegaTConst;
import com.globalsight.everest.edit.offline.rtf.ListViewOneWorkDocLoader;
import com.globalsight.everest.edit.offline.rtf.ParaViewOneWorkDocLoader;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tda.TdaHelper;
import com.globalsight.everest.tm.exporter.TmxChecker;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TagNode;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.offline.parser.AmbassadorDwUpEventHandlerInterface;
import com.globalsight.ling.tw.offline.parser.AmbassadorDwUpParser;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * OfflinePageData is used in several ways:
 * 
 * 1) As a container of all display data for download (target segs and
 * resources). 2) As a container of the in-memory representation of an extracted
 * uploaded file. 3) As a container of reference data (server view) which is
 * compared to another OfflinePageData obtained from #2 above (the client-view).
 * 
 * Note: In all cases, OfflinePageData always eventually holds formatted display
 * data. Note: In the case of download (#1 above), OfflinePageData can have a
 * higher number of OfflinePageSegements than are in the actual target file. In
 * this case, some will be unmerged original segment references that enable
 * split to occur in the client. Using getSegmentIterator(), you can iterate
 * over just the actual target segments.
 * 
 * The various loadXXX() methods can be used to initialize the object either by
 * reading an offline file from disk (upload-mode) or by reading data from the
 * database (download-mode).
 */
public class OfflinePageData implements AmbassadorDwUpEventHandlerInterface,
        Serializable
{
    private static final long serialVersionUID = -4415872671186138674L;

    static private final Logger CATEGORY = Logger
            .getLogger(OfflinePageData.class);

    private PseudoData tmxPTagData = new PseudoData();
    private PseudoData tuvPTagData = new PseudoData();
    private TmxPseudo convertor = new TmxPseudo();

    private ArrayList m_ref_allOSDUnmergedIds = new ArrayList();

    /**
     * An unmerged version of ALL OfflineSegmentData, referenced both by
     * resource and main page.
     */
    private HashMap m_ref_allOSDUnmerged = new HashMap();

    /**
     * Only the merged instances of OfflineSegmentData, referenced by main page.
     */
    private HashMap m_ref_allOSDMerged = new HashMap();

    /**
     * Holds references into both maps (m_ref_allOSDUnmerged and
     * m_ref_allOSDMerged), we use this list to build a target page.
     */
    private Vector m_segmentList = new Vector();

    private Vector<OfflineSegmentData> m_segmentListUnmerged = new Vector<OfflineSegmentData>();

    /** Key: a OfflineSegmentData's DisplaySegmentID; value: the OSD. */
    private HashMap m_segmentMap = new HashMap();

    /**
     * For upload: a map of all uploaded segment comments.
     * 
     * To determine if a comment is new, a reply, or an unmodified copy of an
     * existing comment it must be compared against the reference page data.
     * 
     * Key: String "TUID_SUBID", value: UploadIssue object.
     */
    private HashMap m_uploadedIssueMap = new HashMap();
    /**
     * For upload: the list of all uploaded issues that are new.
     */
    private ArrayList m_uploadedNewIssues = new ArrayList();
    /**
     * For upload: a map of uploaded issues that are replies, keyed by the ID of
     * the issue they reply to (as Long).
     * 
     * Key: issue ID as Long, value: UploadIssue object.
     */
    private HashMap m_uploadedReplyIssuesMap = new HashMap();

    /**
     * The list of original segment comments from the DB. Key: TUVID_SUBID,
     * value: Issue object.
     */
    private HashMap m_issueMap = new HashMap();

    private boolean m_isLoadedFromMergeEnabledFormat = false;

    private AmbassadorDwUpParser m_parser = null;
    private OfflineSegmentData m_curSegData = null;
    private long m_totalNumOfSegments = 0;
    private long m_exactWordCount = 0;
    private long m_fuzzyWordCount = 0;
    private long m_noMatchWordCount = 0;
    private long m_inContextMatchWordCount = 0;
    private boolean m_hasTmResources = false;
    private boolean m_hasTermResources = false;

    // Annotations can add a lot of bulk to an RTF file, sometimes
    // creating files over 3 or 4 megabytes in size. The default is
    // to not insert annotations and instead insert one single
    // hyperlink per segment. This value can be (and usually is)
    // overridden by the setting in OfflineEditorConfig.properties.
    private int m_annotationThreshold = 0;

    // This is the state of the download-edit-all button.
    private String m_displayDownloadEditAll = AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_UNAUTHORIZED;
    private int m_stateDownloadEditAll = AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED;

    private String displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_BOTH;
    private int TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH;

    private String m_startSignature = AmbassadorDwUpConstants.SIGNATURE;
    private String m_endSignature = AmbassadorDwUpConstants.END_SIGNATURE;
    private String m_bom;
    private String m_encoding;
    private String m_workflowID;
    private String m_pageId;
    private String m_fullPageName;
    private String m_pageName;
    private String m_pageUrlPrefix;
    private boolean m_canUseUrl;
    private String m_taskID;
    private String m_documentFormat;
    private String m_placeholderFormat;
    private String m_sourceLocaleName;
    private String m_targetLocaleName;
    private String m_loadConversionLineBreak = null;
    private long jobId = -1;
    private String jobName = null;
    private String m_instanceID = null;
    private boolean m_isOmegaT = false;
    private boolean m_isXliff = false;
    private boolean m_isXliff20 = false;
    private boolean populate100 = false;
    private boolean preserveSourceFolder = false;

    // **MUST** start false to properly load an upload file.
    private boolean m_isSource = false;

    private boolean m_isConsolated = false;

    private boolean m_isCombined = false;

    private boolean m_isConvertLf = false;

    private long m_companyId = -1;

    private List<Long> m_taskIds;

    private String m_allJobIds;

    private String m_alljobnames;

    private boolean m_isRepetitons = false;

    private Vector m_excludedItemTypes = new Vector();

    // For XLF/OmegaT translation kit, store its tuID to target "state"
    // attribute value.
    private HashMap<String, String> tuId2XlfTrgStateMap = new HashMap<String, String>();

    /**
     * Constructor.
     */
    public OfflinePageData()
    {
        super();

        clear();
    }

    /**
     * Clears all values.
     */
    public void clear()
    {
        m_segmentList.clear();
        m_segmentMap.clear(); // CvdL
        m_parser = null;
        m_curSegData = null;
        m_totalNumOfSegments = 0;
        m_bom = "";
        m_startSignature = AmbassadorDwUpConstants.SIGNATURE;
        m_encoding = "";
        m_workflowID = "";
        m_pageId = "";
        m_pageName = "";
        m_canUseUrl = false;
        m_pageUrlPrefix = "";
        m_taskID = "";
        m_documentFormat = "";
        m_placeholderFormat = "";
        m_sourceLocaleName = "";
        m_targetLocaleName = "";
        m_endSignature = AmbassadorDwUpConstants.END_SIGNATURE;
        m_issueMap.clear();
        m_uploadedIssueMap.clear();
        m_uploadedNewIssues.clear();
        m_uploadedReplyIssuesMap.clear();
        jobName = null;
        jobId = -1;
        m_instanceID = null;

        // NOTE: do not clear the following
        // m_loadConversionLineBreak - ;
    }

    public boolean isConvertLf()
    {
        return m_isConvertLf;
    }

    public void setIsConvertLf(boolean m_isConvertLf)
    {
        this.m_isConvertLf = m_isConvertLf;
    }

    public boolean isOmegaT()
    {
        return m_isOmegaT;
    }

    public void setIsOmegaT(boolean p_isO)
    {
        this.m_isOmegaT = p_isO;
    }

    public boolean isXliff()
    {
        return m_isXliff;
    }

    public void setIsXliff(boolean p_isXliff)
    {
        this.m_isXliff = p_isXliff;
    }

    public boolean isPopulate100()
    {
        return populate100;
    }

    public void setPopulate100(boolean populate100)
    {
        this.populate100 = populate100;
    }

    /**
     * Adds a OfflineSegmentData to the segment list.
     * 
     * This method should only be used by test classes and this class' own
     * internal text file parser which is invoked by loadOfflineTextFile().
     * 
     * Used by upload.
     * 
     * @param p_OSD
     *            the OfflineSegmentData to be added.
     */
    public void addSegment(OfflineSegmentData p_osd)
    {
        m_segmentList.add(p_osd);
        m_segmentMap.put(p_osd.getDisplaySegmentID(), p_osd);

        if (p_osd.isMerged())
        {
            m_ref_allOSDMerged.put(p_osd.getDisplaySegmentID(), p_osd);
        }

        m_totalNumOfSegments = m_segmentList.size();
        m_hasTmResources = m_hasTmResources || p_osd.hasTMMatches();
        m_hasTermResources = m_hasTermResources || p_osd.hasTerminology();
    }

    /**
     * Used during download to map a target segment to a pre-existing
     * OfflineSegmentData object which was added by either addUnmergedSegment()
     * or addMergedSegment().
     * 
     * @param p_segId
     *            the id of the segment to be written to the target document.
     * @return true if reference found and added, otherwise false.
     */
    public boolean mapSegmentToResource(String p_segId)
    {
        OfflineSegmentData osdRef = null;
        boolean result = false;

        if (p_segId != null)
        {
            // find the proper reference:
            // first, search in the merged resources
            osdRef = (OfflineSegmentData) m_ref_allOSDMerged.get(p_segId);

            if (osdRef == null)
            {
                // then search in the unmerged resources
                osdRef = (OfflineSegmentData) m_ref_allOSDUnmerged.get(p_segId);
            }

            // add the reference:
            if (osdRef != null)
            {
                addSegment(osdRef);
                result = true;
            }
        }

        return result;
    }

    /**
     * Adds a collection of unmerged segments to the unmerged segment list.
     * 
     * For download, all segments must be included in the unmerged segment list
     * so that they may be used as resources (for merged segs) as well as
     * document segments.
     * 
     * @param p_OSD
     *            the collection of OfflineSegmentData to be added.
     */
    public void addUnmergedSegmentResource(ArrayList p_osds)
    {
        if (p_osds == null)
        {
            return;
        }

        for (int i = 0, max = p_osds.size(); i < max; i++)
        {
            addUnmergedSegmentResource((OfflineSegmentData) p_osds.get(i));
        }
    }

    /**
     * Adds a single unmerged segment to the unmerged segment list.
     * 
     * For download, all segments must be included in the unmerged segment list
     * so that they may be used as resources (for merged segs) as well as
     * document segments.
     * 
     * @param p_OSD
     *            the OfflineSegmentData to be added.
     */
    public void addUnmergedSegmentResource(OfflineSegmentData p_osd)
    {
        if (p_osd == null)
        {
            return;
        }

        m_ref_allOSDUnmerged.put(p_osd.getDisplaySegmentID(), p_osd);
        m_ref_allOSDUnmergedIds.add(p_osd.getDisplaySegmentID());
    }

    /**
     * Adds a collection of merged segment to the merged segment list. Used
     * during download.
     * 
     * @param p_OSD
     *            the OfflineSegmentData to be added.
     */
    public void addMergedSegmentResource(ArrayList p_osds)
    {
        if (p_osds == null)
        {
            return;
        }

        for (int i = 0, max = p_osds.size(); i < max; i++)
        {
            addMergedSegmentResource((OfflineSegmentData) p_osds.get(i));
        }
    }

    /**
     * Adds a merged segment to the merged segment list. Used during download.
     * 
     * @param p_OSD
     *            the OfflineSegmentData to be added.
     */
    public void addMergedSegmentResource(OfflineSegmentData p_osd)
    {
        if (p_osd == null)
        {
            return;
        }

        m_ref_allOSDMerged.put(p_osd.getDisplaySegmentID(), p_osd);
    }

    /**
     * Sets the segment ID.
     * 
     * Note: you must use this method to change a display of a segment that has
     * already been added. This method will fixup various internal maps to the
     * segmemt.
     * 
     * @param p_oldID
     *            java.lang.String
     * @param p_newID
     *            java.lang.String
     */
    /*
     * public void changeDisplayID(String p_oldId, String p_newId) {
     * OfflineSegmentData OSD = (OfflineSegmentData)m_segmentMap.get(p_oldId);
     * OSD.setDisplayID(p_newId); // note: we are not recreating the segment so
     * its location in the ordered // m_segmentList is unchanged. We just need
     * to remap to it. m_segmentMap.remove(p_oldId);
     * m_segmentMap.put(OSD.getDisplaySegmentID(), OSD); }
     */

    /**
     * Creates the issue map from the Array of Issues. The issue map can be
     * retrieved with getIssueMap().
     * 
     * @param p_issues
     *            list of Issue objects.
     */
    public void setIssues(ArrayList p_issues)
    {
        if (p_issues == null)
        {
            return;
        }

        for (int i = 0, max = p_issues.size(); i < max; i++)
        {
            Issue issue = (Issue) p_issues.get(i);

            // The key is TUID_SUBID.
            String key = CommentHelper.getTuSubKey(issue.getLogicalKey());

            if (CATEGORY.isDebugEnabled())
            {
                System.out.println("OPD: adding issue with key " + key
                        + " (logical key = " + issue.getLogicalKey() + ")");
            }

            m_issueMap.put(key, issue);
        }
    }

    public HashMap getIssuesMap()
    {
        return m_issueMap;
    }

    /*
     * 
     */
    public HashMap<Long, HashMap> getTUVIssueMap(long p_jobId)
    {
        HashMap<Long, HashMap> newMap = new HashMap<Long, HashMap>();

        if (m_issueMap != null)
        {
            Iterator iter = m_issueMap.entrySet().iterator();

            while (iter.hasNext())
            {
                Map.Entry me = (Map.Entry) iter.next();
                IssueImpl issue = (IssueImpl) me.getValue();
                HashMap<String, Object> tempMap = new HashMap<String, Object>();
                tempMap.put("IssueID", issue.getId());
                tempMap.put("LevelObjectId", issue.getLevelObjectId());
                tempMap.put("LevelObjectType",
                        issue.getLevelObjectTypeAsString());
                tempMap.put("CreateDate", issue.getCreateDate());
                tempMap.put("CreatorId", issue.getCreatorId());
                tempMap.put("Title", issue.getComment());
                tempMap.put("Priority", issue.getPriority());
                tempMap.put("Status", issue.getStatus());
                tempMap.put("LogicalKey", issue.getLogicalKey());
                tempMap.put("Category", issue.getCategory());

                Vector<HashMap<String, Object>> historyVec = new Vector<HashMap<String, Object>>();

                for (int i = 0; i < issue.getHistory().size(); i++)
                {
                    HashMap<String, Object> hv = new HashMap<String, Object>();
                    IssueHistoryImpl history = (IssueHistoryImpl) issue
                            .getHistory().get(i);
                    hv.put("HistoryID", history.getDbId());
                    hv.put("IssueID", history.getIssue().getId());
                    hv.put("Timestamp", history.getTimestamp());
                    hv.put("ReportedBy", history.getReportedBy());
                    hv.put("Comment", history.getComment());
                    historyVec.add(hv);
                }

                tempMap.put("HistoryVec", historyVec);
                TuvImpl ti = null;
                try
                {
                    ti = SegmentTuvUtil.getTuvById(issue.getLevelObjectId(),
                            p_jobId);
                }
                catch (Exception e)
                {
                    CATEGORY.error(e.getMessage(), e);
                }
                tempMap.put("localeId", ti.getLocaleId());
                newMap.put(ti.getTuId(), tempMap);
            }
        }

        return newMap;
    }

    /*
     * Not needed but left in for reference. public void addUploadedIssue(String
     * p_segmentId, String p_title, String p_status, String p_priority, String
     * p_comment) { String[] tmp = p_segmentId.split("[_:]"); String tuId =
     * tmp[0]; String subId = tmp.length == 2 ? tmp[1] : "0";
     * 
     * String key = tuId + CommentHelper.SEPARATOR + subId;
     * 
     * m_uploadedIssueMap.put(key, new UploadIssue(p_segmentId,
     * Long.parseLong(tuId), Long.parseLong(subId), p_title, p_status,
     * p_priority, p_comment)); }
     */

    public void addUploadedIssue(UploadIssue p_issue)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_issue.getTuId());
        sb.append(CommentHelper.SEPARATOR);
        sb.append(p_issue.getSubId());

        String key = sb.toString();

        m_uploadedIssueMap.put(key, p_issue);
    }

    public HashMap getUploadedIssuesMap()
    {
        return m_uploadedIssueMap;
    }

    public void addUploadedNewIssue(UploadIssue p_issue)
    {
        m_uploadedNewIssues.add(p_issue);
    }

    public ArrayList getUploadedNewIssues()
    {
        return m_uploadedNewIssues;
    }

    public void addUploadedReplyIssue(Issue p_refIssue, UploadIssue p_issue)
    {
        m_uploadedReplyIssuesMap.put(new Long(p_refIssue.getId()), p_issue);
    }

    public HashMap getUploadedReplyIssuesMap()
    {
        return m_uploadedReplyIssuesMap;
    }

    /**
     * Sets a flag to indicate that at least one segment in the page has a
     * leveraged match.
     * 
     * @param p_hasTmRes
     *            set true if resources are available, otherwise false.
     */
    public void setHasTmResources(boolean p_hasTmRes)
    {
        m_hasTmResources = p_hasTmRes;
    }

    /**
     * Sets a flag to indicate that at least one segment in the page has a
     * terminology matches.
     * 
     * @param p_hasTermRes
     *            set true if resources are available, otherwise false.
     */
    public void setHasTermResources(boolean p_hasTermRes)
    {
        m_hasTermResources = p_hasTermRes;
    }

    /**
     * Sets the Byte Order Mark to be used to create an offline file.
     * 
     * @param p_newBom
     *            the byte order mark sequence.
     */
    public void setBom(String p_newBom)
    {
        m_bom = p_newBom;
    }

    /**
     * Sets the offline files end signature. This signature string identifies
     * our offline file format.
     * 
     * @param p_newEndSignature
     *            signature string
     */
    public void setEndSignature(String p_newEndSignature)
    {
        m_endSignature = p_newEndSignature;
    }

    /**
     * Sets the pages native document format. The format name should be one
     * retrieved from the extractor registry.
     * 
     * @param p_newFormat
     *            (html, xml, plaintext ...etc..)
     */
    public void setDocumentFormat(String p_newFormat)
    {
        m_documentFormat = p_newFormat;
    }

    /**
     * Sets the offline files start signature. This signature string identifies
     * our offline file format.
     * 
     * @param p_newStartSignature
     *            the start signature.
     */
    public void setStartSignature(String p_newStartSignature)
    {
        m_startSignature = p_newStartSignature;
    }

    /**
     * Sets the name of the codeset of the target language.
     */
    public void setEncoding(String p_newEncoding)
    {
        m_encoding = p_newEncoding;
    }

    /**
     * Sets the Workflow ID that this page belongs to.
     */
    public void setWorkflowId(String p_newWorkflowID)
    {
        m_workflowID = p_newWorkflowID;
    }

    /**
     * Sets the page's ID.
     */
    public void setPageId(String p_newPageID)
    {
        m_pageId = p_newPageID;
    }

    /**
     * Set the source locale name.
     */
    public void setSourceLocaleName(String p_newLocale)
    {
        m_sourceLocaleName = p_newLocale;
    }

    /**
     * Set the target locale name.
     */
    public void setTargetLocaleName(String p_newLocale)
    {
        m_targetLocaleName = p_newLocale;
    }

    /**
     * Sets the linebreak to be substituted for existing linebreaks when loading
     * from a file.
     */
    public void setLoadConversionLineBreak(String p_newLineBreak)
    {
        m_loadConversionLineBreak = p_newLineBreak;
    }

    /**
     * Sets the page's name.
     * 
     * This can either be a fully qualified path or just the file name. Both the
     * full name and shortened page name are derived from this setting. See
     * getFullPageName and getPageName().
     */
    public void setPageName(String p_newPageName)
    {
        m_fullPageName = p_newPageName;

        // make page name
        File f = new File(((p_newPageName == null) ? "" : p_newPageName));
        if (f.getName() == null || f.getName().length() <= 0)
        {
            m_pageName = "";
        }
        else
        {
            m_pageName = f.getName();
        }
    }

    /**
     * Sets the page's URL prefix.
     * 
     * @param p_pageUrlPrefix
     *            String
     */
    public void setPageUrlPrefix(String p_pageUrlPrefix)
    {
        m_pageUrlPrefix = p_pageUrlPrefix;
    }

    /**
     * Sets whether or not a page has a preview URL.
     * 
     * @param newCanUseUrl
     *            boolean
     */
    public void setCanUseUrl(boolean newCanUseUrl)
    {
        m_canUseUrl = newCanUseUrl;
    }

    /**
     * Sets the page TaskID.
     * 
     * @param p_newStageID
     *            String
     */
    public void setTaskId(String p_newStage)
    {
        m_taskID = p_newStage;
    }

    /**
     * Sets the pages placeholder format.
     * 
     * @param p_PlaceholderFormat
     *            String
     */
    public void setPlaceholderFormat(String p_placeholderFormat)
    {
        m_placeholderFormat = p_placeholderFormat;
    }

    /**
     * Sets the exact match word count.
     * 
     * @param p_exactMatchWordCount
     */
    public void setExactMatchWordCount(long p_exactMatchWordCount)
    {
        m_exactWordCount = p_exactMatchWordCount;
    }

    /**
     * Sets the fuzzy match word count.
     * 
     * @param p_fuzzyMatchWordCount
     */
    public void setFuzzyMatchWordCount(long p_fuzzyMatchWordCount)
    {
        m_fuzzyWordCount = p_fuzzyMatchWordCount;
    }

    /**
     * Sets the Nomatch word count.
     * 
     * @param p_noMatchWordCount
     */
    public void setNoMatchWordCount(long p_noMatchWordCount)
    {
        m_noMatchWordCount = p_noMatchWordCount;
    }

    /**
     * The state of the download-edit-all button which will be displayed in the
     * header of the download file. This is a tri-state button. If it is not
     * enabled for the user it is in the unauthorized-state. If it is enabled
     * for the user, they may choose to enable or disable the function.
     * 
     * NOTE: currently the download edit-all button is only used to determine
     * protection during the creation of the download file. On upload we ignore
     * the value in the header and use the Match type string on to determine
     * what was protected for download.
     * 
     * @param p_downloadEditAll
     *            - the tri-state of the users choice.
     */
    public void setDownloadEditAll(int p_stateDownloadEditAll)
    {
        m_stateDownloadEditAll = p_stateDownloadEditAll;

        // set display string.
        switch (p_stateDownloadEditAll)
        {
            case AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED:
                m_displayDownloadEditAll = AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_UNAUTHORIZED;
                break;
            case AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_YES:
                m_displayDownloadEditAll = AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_YES;
                break;
            case AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_NO:
                m_displayDownloadEditAll = AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_NO;
                break;
            default:
                // force to unauthorized state
                m_displayDownloadEditAll = AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_UNAUTHORIZED;
                m_stateDownloadEditAll = AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED;
                break;
        }
    }

    /**
     * The state of the download-edit-all button which will be displayed in the
     * header of the download file. This is a tri-state button. If it is not
     * enabled for the user it is in the unauthorized-state. If it is enabled
     * for the user, they may choose to enable or disable the function.
     * 
     * NOTE: currently the download edit-all button is only used to determine
     * protection during the creation of the download file. On upload we ignore
     * the value in the header and use the Match type string on to determine
     * what was protected for download.
     * 
     * @param p_TMEditType
     *            - the tri-state of the users choice.
     */
    public void setTMEditType(int p_TMEditType)
    {
        m_stateDownloadEditAll = p_TMEditType;
        TMEditType = p_TMEditType;

        // set display string.
        switch (p_TMEditType)
        {
            case AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH:
                displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_BOTH;
                break;
            case AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE:
                displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_ICE;
                break;
            case AmbassadorDwUpConstants.TM_EDIT_TYPE_100:
                displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_100;
                break;
            case AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY:
                displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_DENY;
                break;
            default:
                TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_NONE;
                displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_NONE;
                break;
        }
    }

    /**
     * Annotations can add a lot of bulk to an RTF file, sometimes creating
     * files over 3 or 4 megabytes in size. The default is to not insert
     * annotations and instead insert one single hyperlink (to the resource
     * pages) per segment.
     * 
     * The OfflinePageDataGenerator calls this method to set the threshold using
     * the value read from OfflineEditorConfig.properties.
     * 
     * @param p_threshold
     *            the max number of segments that can contain annotations. NOTE:
     *            This is NOT a segment Id! Just a number...
     */
    public void setAnnotationThreshold(int p_threshold)
    {
        m_annotationThreshold = p_threshold;
    }

    /**
     * Returns the annotation threshold.
     * 
     * @return the number of segments that can contain annotations.
     */
    public int getAnnotationThreshold()
    {
        return m_annotationThreshold;
    }

    /**
     * Returns the byte order mark last used to read/write from a text file.
     * 
     * @return the byte order mark as a String
     */
    public String getBom()
    {
        return m_bom;
    }

    /**
     * Gets the offline file's end signature.
     */
    public String getEndSignature()
    {
        return m_endSignature;
    }

    /**
     * Gets the pages native document format (html, plaintext, xml, etc...). The
     * format should match one listed in the extractor registry.
     */
    public String getDocumentFormat()
    {
        return m_documentFormat;
    }

    /**
     * Gets the page's placeholder display format (TAG_TYPE_PTAGV - verbose,
     * TAG_TYPE_PTAGC - compact).
     */
    public String getPlaceholderFormat()
    {
        return m_placeholderFormat;
    }

    /**
     * Gets the page's placeholder format ID (PSEUDO-VERBOSE, PSEUDO-COMPACT).
     */
    public int getPlaceholderFormatId()
    {
        if (m_placeholderFormat.equals(AmbassadorDwUpConstants.TAG_TYPE_PTAGV))
        {
            return PseudoConstants.PSEUDO_VERBOSE;
        }
        else if (m_placeholderFormat
                .equals(AmbassadorDwUpConstants.TAG_TYPE_PTAGC))
        {
            return PseudoConstants.PSEUDO_COMPACT;
        }

        return -1;
    }

    /**
     * Gets the offline files start signature.
     */
    public String getStartSignature()
    {
        return m_startSignature;
    }

    /**
     * Returns the name of the codeset for the target language.
     */
    public String getEncoding()
    {
        return m_encoding;
    }

    /**
     * Returns the page ID.
     */
    public String getPageId()
    {
        return m_pageId;
    }

    /**
     * Returns the source locale name.
     */
    public String getSourceLocaleName()
    {
        return m_sourceLocaleName;
    }

    /**
     * Returns the target locale name.
     */
    public String getTargetLocaleName()
    {
        return m_targetLocaleName;
    }

    /**
     * Returns the linebreak sequence that will be substituted when a file is
     * loaded using one of this classes load methods.
     */
    public String getLoadConversionLineBreak()
    {
        return m_loadConversionLineBreak;
    }

    /**
     * Returns an iterator over the internal segment list.
     */
    public ListIterator getSegmentIterator()
    {
        return m_segmentList.listIterator();
    }

    /**
     * Returns an iterator over the internal unmerged segment id list. This list
     * includes all original segment ids.
     */
    public Iterator getAllUnmergedSegmentIdIterator()
    {
        return m_ref_allOSDUnmergedIds.iterator();
    }

    /**
     * Returns an OfflineSegmentData object if found, otherwise null.
     */
    public OfflineSegmentData getSegmentByDisplayId(String p_displayId)
    {
        return (OfflineSegmentData) m_segmentMap.get(p_displayId);
    }

    /**
     * Returns an OfflineSegmentData object if found, otherwise null.
     */
    public OfflineSegmentData getResourceByDisplayId(String p_displayId)
    {
        return (OfflineSegmentData) m_ref_allOSDUnmerged.get(p_displayId);
    }

    /**
     * Returns the segment map - keyed by segment Id.
     */
    public HashMap getSegmentMap()
    {
        return m_segmentMap;
    }

    /**
     * Returns the page's segment merge map.
     * 
     * The map is keyed by the parent id under which the merge occured.
     * 
     * The value of each map entry is a list of ids in the same order as merged.
     * 
     * The first id of each record is also always the parent id under which the
     * merge occured.
     * 
     * @return HashMap of merge records - keyed by the parent id under which the
     *         merge occured. Null if no segments are merged.
     */
    public Map getSegmentMergeMap()
    {
        HashMap map = new HashMap();
        Set keys = m_ref_allOSDMerged.keySet();

        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            OfflineSegmentData OSD = (OfflineSegmentData) m_ref_allOSDMerged
                    .get(it.next());
            ArrayList l = OSD.getMergedIds();
            map.put((Long) l.get(0), l);
        }

        return map;
    }

    /**
     * Returns a HashMap of SubflowMergeInfo's for merged segments ONLY. This
     * map is used during upload to renumber subflows who's parent is merged.
     * 
     * The map is keyed by the TU ID. This is the id that the given SubInfo is
     * for. The value of each map entry is a SubflowMergeInfo object.
     * 
     * @return HashMap - keyed by TU Id, value is a SublflowMergeInfo.
     */
    public Map buildSubflowOffsetMap()
    {
        HashMap result = new HashMap();
        Map parentMergeMap = getSegmentMergeMap();

        if (parentMergeMap == null)
        {
            return result;
        }

        Set keys = parentMergeMap.keySet();

        for (Iterator keysIt = keys.iterator(); keysIt.hasNext();)
        {
            Long parentOfMerge = (Long) keysIt.next();
            ArrayList mergedParentSegIds = (ArrayList) parentMergeMap
                    .get(parentOfMerge);

            for (int cnt = 0; cnt < mergedParentSegIds.size(); cnt++)
            {
                Long subInfoOwner = ((Long) mergedParentSegIds.get(cnt));

                SubflowMergeInfo subInfo = new SubflowMergeInfo(
                        subInfoOwner.longValue(), parentOfMerge.longValue(),
                        cnt * AmbassadorDwUpConstants.SPLIT_MERGE_OFFSET_BASE);

                result.put(subInfoOwner, subInfo);
            }
        }

        return result;
    }

    /**
     * Returns the page's full name (including path if any).
     */
    public String getFullPageName()
    {
        return m_fullPageName;
    }

    /**
     * Returns the page's name only (excluding path if any) or empty string if
     * the page name is null.
     */
    public String getPageName()
    {
        return m_pageName;
    }

    /**
     * Gets the page ID.
     */
    public String getUrlPrefix()
    {
        return m_pageUrlPrefix;
    }

    /**
     * Gets the Workflow ID that this page belongs to.
     */
    public String getWorkflowId()
    {
        return m_workflowID;
    }

    /**
     * Gets the page's Task ID.
     */
    public String getTaskId()
    {
        return m_taskID;
    }

    /**
     * Gets the exact match word count.
     * 
     * @return m_exactMatchWordCount as long
     */
    public long getExactMatchWordCount()
    {
        return m_exactWordCount;
    }

    /**
     * Gets the exact match word count as a string.
     * 
     * @return m_exactMatchWordCount as string
     */
    public String getExactMatchWordCountAsString()
    {
        return Long.toString(m_exactWordCount);
    }

    /**
     * Gets the fuzzy match word count.
     * 
     * @return m_fuzzyMatchWordCount as long
     */
    public long getFuzzyMatchWordCount()
    {
        return m_fuzzyWordCount;
    }

    /**
     * Gets the fuzzy match word count as a string.
     * 
     * @return m_fuzzyMatchWordCount as string
     */
    public String getFuzzyMatchWordCountAsString()
    {
        return Long.toString(m_fuzzyWordCount);
    }

    /**
     * Gets the Nomatch word count.
     * 
     * @return m_noMatchWordCount as long
     */
    public long getNoMatchWordCount()
    {
        return m_noMatchWordCount;
    }

    /**
     * Gets the Nomatch word count as a string.
     * 
     * @return m_noMatchWordCount as string
     */
    public String getNoMatchWordCountAsString()
    {
        return Long.toString(m_noMatchWordCount);
    }

    /**
     * Returns true if the segment data is in GXML format.
     */
    public boolean isTagFormatGXML()
    {
        return m_placeholderFormat
                .equals(AmbassadorDwUpConstants.TAG_TYPE_GXML);
    }

    /**
     * Returns true if the segment data is in PTAG format.
     */
    public boolean isTagFormatPTAG()
    {
        return (m_placeholderFormat
                .equals(AmbassadorDwUpConstants.TAG_TYPE_PTAGC) || m_placeholderFormat
                .equals(AmbassadorDwUpConstants.TAG_TYPE_PTAGV));
    }

    public boolean isLoadedFromMergeEnabledClient()
    {
        return m_isLoadedFromMergeEnabledFormat;
    }

    /**
     * Returns true if at least one segment in the page has a leveraged match.
     */
    public boolean hasTmResources()
    {
        return m_hasTmResources;
    }

    /**
     * Returns true if at least one segment in the page has Terminology matches.
     */
    public boolean hasTermResources()
    {
        return m_hasTermResources;
    }

    /**
     * Returns true if there if a preview url has been defined for this page.
     */
    public boolean isCanUseUrl()
    {
        return m_canUseUrl;
    }

    /**
     * Determines and creates a java locale based on the target locale name.
     */
    public Locale determineTargetLocale()
    {
        Locale locale = null;

        if (m_targetLocaleName.length() == 2) // must be language only
        {
            locale = new Locale(m_targetLocaleName, "");
        }
        else if (m_targetLocaleName.length() == 5) // language plus country
        {
            locale = new Locale(m_targetLocaleName.substring(0, 2),
                    m_targetLocaleName.substring(3, 5));
        }
        else
        {
            // handle variants - use default locale for now
            locale = Locale.US;
        }

        return locale;
    }

    /**
     * Returns the total number of segments loaded.
     */
    public long getTotalNumOfSegments()
    {
        return m_totalNumOfSegments;
    }

    /**
     * Gets the display value of the download-edit-all option from the header of
     * the download file.
     * 
     * @return a display string indicating the choice made during download.
     */
    public String getDisplayDownloadEditAll()
    {
        return m_displayDownloadEditAll;
    }

    /**
     * Gets the state of the download-edit-all option for this page.
     * 
     * @return an integer representing the state (YES/NO/UNAUTHORIZED).
     */
    public int getStateDownloadEditAll()
    {
        return m_stateDownloadEditAll;
    }

    // =============================================================
    // Parser Event Handlers.
    // An implementation of the AmbassadorDwUpEventHandlerInterface.
    // The following events are fired by the offline parser when one
    // of the file-based loader methods is called.
    // =============================================================

    /**
     * Parser event method.
     * 
     * @param s
     *            - the file's byte order mark.
     */
    public void handleBom(String s)
    {
        m_bom = s;
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the target language codeset name.
     */
    public void handleEncoding(String s)
    {
        m_encoding = s.trim();
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the Workflow ID that this page belongs to.
     */
    public void handleWorkflowId(String s)
    {
        m_workflowID = s.trim();
    }

    /**
     * Parser event method.
     * 
     * Handles a segment ID.
     * 
     * @param s
     *            - the segment ID.
     */
    public void handleId(String s)
    {
        // Finish the segment we were working on last.
        if (m_curSegData != null)
        {
            // Remove the new lines that were added for formating
            String segment = m_curSegData.getDisplaySourceText();
            m_curSegData.setDisplaySourceText(removeLastNewline(segment));

            // Remove the new lines that were added for formating
            segment = m_curSegData.getDisplayTargetText();
            m_curSegData.setDisplayTargetText(removeLastNewline(segment));

            // finish the one we were working on last
            addSegment(m_curSegData);
        }

        m_curSegData = new OfflineSegmentData(s.trim());
    }

    /**
     * Parser event method.
     * 
     * Handles a subflow ID. Subflow Ids have the following form:
     * [parentSegmentId]:[parentPtagId]:[SubflowId] Where [parentPtagId]
     * indicates the numbered ptag in the parent segment under which the sublow
     * resides.
     * 
     * @param s
     *            - a three part id that identifies a subflow
     */
    public void handleSubflowId(String s)
    {
        // Finish the segment we were working on last.
        if (m_curSegData != null)
        {
            // Remove the new lines that were added for formating.
            String segment = m_curSegData.getDisplaySourceText();
            m_curSegData.setDisplaySourceText(removeLastNewline(segment));

            // Remove the new lines that were added for formating.
            segment = m_curSegData.getDisplayTargetText();
            m_curSegData.setDisplayTargetText(removeLastNewline(segment));

            addSegment(m_curSegData);
        }

        // Create new segment.
        m_curSegData = new OfflineSegmentData(s.trim());
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the page name.
     */
    public void handlePageName(String s)
    {
        m_pageName = s.trim();
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the page ID.
     */
    public void handlePageId(String s)
    {
        m_pageId = s.trim();
    }

    /**
     * Parser event method. Fired after reading the end of the file header.
     */
    public void handleEndHeader()
    {
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the file end signature.
     */
    public void handleEndSignature(String s)
    {
        m_endSignature = s.trim();
    }

    /**
     * Parser event method. Fired after reading the entire file.
     */
    public void handleFinish()
    {
        // Finish the segment we were working on last.
        if (m_curSegData != null)
        {
            // Remove new lines that was added for formatting.
            String segment = m_curSegData.getDisplaySourceText();
            m_curSegData.setDisplaySourceText(removeLastNewline(segment));

            // Remove new lines that was added for formatting.
            segment = m_curSegData.getDisplayTargetText();
            m_curSegData.setDisplayTargetText(removeLastNewline(segment));

            addSegment(m_curSegData);
        }

        m_curSegData = null;
    }

    /**
     * Parser event method.
     * 
     * Fired when the source label (which preceeds the source text in a format
     * one file) is encountered. Format-one is a legacy format from system3
     * which is now used to facilitate test code.
     * 
     * @param s
     *            - the source key.
     */
    public void handleSourceKey(String s)
    {
        // We now use static keys defined in AmbassadorDwUpConstants
        // None of the header keys had handlers anyway.

        m_isSource = true;

        // clear source text
        m_curSegData.setDisplaySourceText("");
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the source locale.
     */
    public void handleSourceLocale(String s)
    {
        m_sourceLocaleName = s.trim().replace("-", "_");
    }

    /**
     * Parser event method. Fired just before the parser begins reading the
     * offline file.
     */
    public void handleStart()
    {
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the start signature.
     */
    public void handleStartSignature(String s)
    {
        m_startSignature = s.trim();
    }

    /**
     * Parser event method.
     * 
     * Fired when the target label (which preceeds the target text in a format
     * one file) is encountered. Format-one is a legacy format from system3
     * which is now used to facilitate test code.
     * 
     * @param s
     *            - the target key.
     */
    public void handleTargetKey(String s)
    {
        // We now use static keys defined in AmbassadorDwUpConstants
        // None of the header keys had handlers anyway.

        m_isSource = false;

        // clear target text
        m_curSegData.setDisplayTargetText("");
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the target locale.
     */
    public void handleTargetLocale(String s)
    {
        m_targetLocaleName = s.trim().replace("-", "_");
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the segment format name.
     */
    public void handleSegmentFormat(String s)
    {
        m_curSegData.setDisplaySegmentFormat(s.trim());
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the match value.
     */
    public void handleMatchScore(String s)
    {
        float matchScore = 0.0f;

        try
        {
            matchScore = Float.parseFloat(s.trim());
        }
        catch (NumberFormatException ex)
        {
            matchScore = -1;

            CATEGORY.warn("Invalid match score: " + s);
        }

        m_curSegData.setMatchValue(matchScore);
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the Match type.
     */
    public void handleMatchType(String s)
    {
        m_curSegData.setDisplayMatchType(s.trim());
    }

    /**
     * Parser event method. handleNewLines receives newline characters.
     * <p>
     * 
     * Note: consecutive newlines appearing in the input stream will be handled
     * here as a single string of newlines.
     */
    public void handleSegmentNewLine(String p_newLineString)
    {
        if (m_loadConversionLineBreak != null)
        {
            int count = 0;
            int len = p_newLineString.length();
            char curChar, nextChar;
            StringBuffer sb = new StringBuffer();

            // Count newline sequences.
            // Note: this can be an assortment of several newlines in a row
            //
            // PLATFORM_UNIX : "\n";
            // PLATFORM_MAC : "\r";
            // PLATFORM_WIN32: "\r\n";
            //
            for (int i = 0; i < len; i++)
            {
                curChar = p_newLineString.charAt(i);

                if (curChar == '\n')
                {
                    count++;
                }
                else if (curChar == '\r')
                {
                    count++;

                    // if this is a \r\n sequence, then advance the index
                    if ((i + 1) < len)
                    {
                        nextChar = p_newLineString.charAt(i + 1);
                        if (nextChar == '\n')
                        {
                            i++;
                        }
                    }
                }
            }

            // build new newline sequence
            for (int i = 0; i < count; i++)
            {
                sb.append(m_loadConversionLineBreak);
            }

            p_newLineString = sb.toString();
        }

        // send converted string on to be appended
        handleSegmentText(p_newLineString);
    }

    /**
     * Parser event method.
     * 
     * The offline file parser is capable of reading two formats. In system3
     * these two formats were refered to as format 1 and format 2. Format 1
     * contains both # Src: and # Trg : keys and source and target text (this
     * was the connection to DB in system3, created by perl side) Format one is
     * now used for test cases to simulate a database connection. Format 2 is
     * the actual offline download format. There is one segment which is always
     * the target. In format 2, there is no key proceeding the target value.
     * 
     * @param s
     *            - the segment text,
     */
    public void handleSegmentText(String s)
    {
        // This method alone cannot detemine if the segment text is
        // source or target.

        // To achieve this, the handleSourceKey() and handleTargetKey()
        // methods set and clear an internal flag which determines the
        // nature of the segment text that will follow.
        if (m_isSource)
        {
            m_curSegData.appendDisplaySourceText(s);
        }
        else
        {
            m_curSegData.appendDisplayTargetText(s);
        }
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the exact match word count.
     */
    public void handleExactMatchWordCount(String s)
    {
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the fuzzy match word count.
     */
    public void handleFuzzyMatchWordCount(String s)
    {
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the no match word count
     */
    public void handleUnmatchedMatchWordCount(String s)
    {
    }

    /**
     * Parser event method. This method recieves the current mode of the Ptag
     * strings.
     * 
     * @param s
     *            - GXML, PTAG-VERBOSE or PTAG-COMPACT
     */
    public void handlePlaceholderFormat(String s)
    {
        m_placeholderFormat = s.trim();
    }

    /**
     * Parser event method.
     * 
     * @param s
     *            - the native document format of the page.
     */
    public void handleDocumentFormat(String s)
    {
        m_documentFormat = s.trim();
    }

    /**
     * Parser event method.
     * 
     * @params s - the TaskID from the header.
     */
    public void handleTaskId(String s)
    {
        m_taskID = s.trim();
    }

    /**
     * Parser event method.
     * 
     * Reads the downloadEditAll display value from the file and sets the state
     * accordingly.
     * 
     * @param s
     *            - the text following the EditAll label
     */
    public void handleEditAll(String s)
    {
        // m_displayDownloadEditAll = s.trim();
        //
        // if (m_displayDownloadEditAll
        // .equals(AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_YES))
        // {
        // m_stateDownloadEditAll =
        // AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_YES;
        // }
        // else if (m_displayDownloadEditAll
        // .equals(AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_NO))
        // {
        // m_stateDownloadEditAll =
        // AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_NO;
        // }
        // else
        // {
        // // force to unauthorized
        // m_stateDownloadEditAll =
        // AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED;
        // m_displayDownloadEditAll =
        // AmbassadorDwUpConstants.HEADER_EDITALL_VALUE_UNAUTHORIZED;
        // }

        // Redirect the calling to new method
        handleTMEditType(s);
    }

    /**
     * Parser TM edit type.
     * 
     * Reads the TMEditType display value from the file and sets the state
     * accordingly. The default is not to allow locked segments
     * 
     * @param s
     *            - the text following the TMEditType label
     */
    public void handleTMEditType(String s)
    {
        displayTMEditType = s.trim();

        if (AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_NONE
                .equals(displayTMEditType))
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_NONE;
        }
        else if (AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_BOTH
                .equals(displayTMEditType))
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH;
        }
        else if (AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_ICE
                .equals(displayTMEditType))
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE;
        }
        else if (AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_100
                .equals(displayTMEditType))
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_100;
        }
        else if (AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_DENY
                .equals(displayTMEditType))
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY;
        }
        else
        {
            TMEditType = AmbassadorDwUpConstants.TM_EDIT_TYPE_NONE;
            displayTMEditType = AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_NONE;
        }
    }

    public void handleJobID(String s)
    {
        if (s != null && s.trim().length() > 0)
            setJobId(Long.valueOf(s));
    }

    public void handleJobName(String s)
    {
        setJobName(s);
    }

    // ======================================
    // Page data read methods
    // ======================================

    /**
     * Initializes this object by reading in an offline List-View-1 type file.
     * 
     * @param p_rtfDoc
     *            the upload file as RtfDocument object as read by the RTF
     *            parser.
     */
    public void loadListViewOneWorkFile(RtfDocument p_rtfDoc) throws Exception
    {
        m_isLoadedFromMergeEnabledFormat = false;

        clear();

        ListViewOneWorkDocLoader loader = new ListViewOneWorkDocLoader(
                p_rtfDoc, this);
        loader.parse();
    }

    /**
     * Initializes this object by reading in an offline Paragragh-View-1 type
     * file.
     * 
     * @param p_rtfDoc
     *            the upload file as RtfDocument object as read by the RTF
     *            parser.
     */
    public void loadParaViewOneWorkFile(RtfDocument p_rtfDoc) throws Exception
    {
        m_isLoadedFromMergeEnabledFormat = true;

        clear();

        ParaViewOneWorkDocLoader loader = new ParaViewOneWorkDocLoader(
                p_rtfDoc, this);
        loader.parse();
    }

    /**
     * Initializes the object by reading in an offline file from a string.
     * 
     * @param p_input
     *            the input file as a string.
     */
    public void loadOfflineTextFile(String p_input) throws Throwable
    {
        clear();

        StringReader input = new StringReader(p_input);
        m_parser = new AmbassadorDwUpParser(input);
        m_parser.setHandler(this);
        m_parser.parse();
    }

    /**
     * Initializes the object by reading in an offline file from a reader.
     * 
     * @param p_input
     *            the file as a reader.
     * @param p_keepIssues
     *            when an OfflinePageData object is called <b>twice</b> to load
     *            data, this parameter allows to keep issues read in the first
     *            run (the second run normally clears the entire object). This
     *            is necessary for RTF list view which first parses the RTF,
     *            then loads the textual content as list view text file.
     */
    public void loadOfflineTextFile(Reader p_input, boolean p_keepIssues)
            throws Throwable
    {
        HashMap tmp = m_uploadedIssueMap;
        m_uploadedIssueMap = new HashMap();
        clear();
        m_uploadedIssueMap = tmp;

        m_parser = new AmbassadorDwUpParser(p_input);
        m_parser.setHandler(this);
        m_parser.parse();
    }

    /**
     * Initializes the object by reading in an offline file in the given
     * encoding.
     * 
     * @param p_File
     *            the file to be read.
     * @param p_Encoding
     *            the codeset of the file.
     */
    public void loadOfflineTextFile(File p_file, String p_encoding)
            throws Throwable
    {
        clear();

        FileInputStream fis = new FileInputStream(p_file);
        m_parser = new AmbassadorDwUpParser(new InputStreamReader(fis,
                p_encoding));
        m_parser.setHandler(this);
        m_parser.parse();
    }

    // ======================================
    // Page data write methods
    // ======================================

    /**
     * Creates a download text file for the translator.
     * 
     * @param p_outputStream
     *            - the output stream
     * @param p_params
     *            - the download parameters
     */
    public void writeOfflineTextFile(OutputStream p_outputStream,
            DownloadParams p_params) throws AmbassadorDwUpException
    {
        OutputStreamWriter w;

        if (p_outputStream == null)
        {
            CATEGORY.error("Invalid output stream (null).");

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME,
                    "Invalid output stream.");
        }

        try
        {
            w = new OutputStreamWriter(p_outputStream, p_params.getEncoding());
            writeOfflineTextFile(w, p_params.getPlatformLineBreak());
            w.flush();
        }
        catch (FileNotFoundException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME, ex);
        }
        catch (UnsupportedEncodingException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_ENCODING, ex);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME, ex);
        }
    }

    /**
     * Creates a download text file for the translator.
     * 
     * @param p_outputStream
     *            - the output stream
     * @param p_lineBreak
     *            - linebreak to be used to write file.
     */
    public void writeOfflineTextFile(OutputStreamWriter p_outputStream,
            String p_lineBreak) throws AmbassadorDwUpException
    {
        if (p_outputStream == null)
        {
            CATEGORY.error("Null output stream.");

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME,
                    "Null output stream.");
        }

        if (p_lineBreak == null)
        {
            CATEGORY.error("Platform line-break is null.");

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT,
                    "Platform line-break is null.");
        }

        verifyHeader();

        try
        {
            // java writes the BOM by itself
            p_outputStream.write(m_startSignature + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_ENCODING_KEY
                    + " " + m_encoding + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_ORIGFMT_KEY
                    + " " + m_documentFormat + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_CURFMT_KEY
                    + " " + m_placeholderFormat + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_SRCLOCALE_KEY
                    + " " + m_sourceLocaleName + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_TRGLOCALE_KEY
                    + " " + m_targetLocaleName + p_lineBreak);

            /*
             * DISABLED Page Name - it is optional and the upload parser
             * currently cannot handle extended characters or a simple
             * exclamation point like in Yahoo!.html
             * p_outputStream.write(AmbassadorDwUpConstants.HEADER_PAGENAME_KEY
             * + " " + (m_fullPageName==null ? "null": m_fullPageName) +
             * p_lineBreak);
             */

            p_outputStream.write(AmbassadorDwUpConstants.HEADER_PAGEID_KEY
                    + " " + m_pageId + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_JOBID_KEY + " "
                    + m_workflowID + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_STAGEID_KEY
                    + " " + m_taskID + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_EXACT_COUNT_KEY
                    + " " + m_exactWordCount + p_lineBreak);
            p_outputStream.write(AmbassadorDwUpConstants.HEADER_FUZZY_COUNT_KEY
                    + " " + m_fuzzyWordCount + p_lineBreak);
            p_outputStream
                    .write(AmbassadorDwUpConstants.HEADER_NOMATCH_COUNT_KEY
                            + " " + m_noMatchWordCount + p_lineBreak);

            if (!displayTMEditType
                    .equals(AmbassadorDwUpConstants.HEADER_TM_EDIT_TYPE_NONE))
            {
                p_outputStream.write(AmbassadorDwUpConstants.HEADER_EDITALL_KEY
                        + " " + displayTMEditType + p_lineBreak);
            }

            p_outputStream.write(p_lineBreak);

            // segments
            for (ListIterator it = m_segmentList.listIterator(); it.hasNext();)
            {
                OfflineSegmentData osd = (OfflineSegmentData) it.next();
                boolean id;

                if (id = (osd.getDisplaySegmentID().length() <= 0))
                {
                    p_outputStream.close();

                    osd = (OfflineSegmentData) it.previous();
                    String msg = "Cannot write a format two segment:\n"
                            + "The in-memory segment ID for the segment following "
                            + osd.getDisplaySegmentID() + " is empty.";

                    CATEGORY.error(msg);

                    throw new AmbassadorDwUpException(
                            AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT,
                            msg);
                }
                else
                {
                    p_outputStream.write(AmbassadorDwUpConstants.SEGMENT_ID_KEY
                            + osd.getDisplaySegmentID() + p_lineBreak);

                    // Indicate a segment native-format switch.
                    // NOTE: We decided to NOT show this on all
                    // segments. We only show this when the format
                    // differs from the doc format
                    if (!m_documentFormat.equals(osd.getDisplaySegmentFormat()))
                    {
                        p_outputStream
                                .write(AmbassadorDwUpConstants.SEGMENT_FORMAT_KEY
                                        + " "
                                        + osd.getDisplaySegmentFormat()
                                        + p_lineBreak);
                    }

                    // Indicate the match type and score for parent segments
                    // only.
                    String tmp = osd.getDisplayMatchType();
                    if (tmp != null && tmp.length() != 0)
                    {
                        p_outputStream
                                .write(AmbassadorDwUpConstants.SEGMENT_MATCH_TYPE_KEY
                                        + " " + tmp + p_lineBreak);
                    }

                    List notCountTags = osd.getNotCountTags();
                    if (notCountTags.size() > 0)
                    {
                        String tags = notCountTags.toString();
                        tags = tags.substring(1, tags.length() - 1);
                        String message = AmbassadorDwUpConstants.SEGMENT_NOT_COUNT_KEY;
                        message = MessageFormat.format(message, new String[]
                        { tags });
                        p_outputStream.write(message + p_lineBreak);
                    }

                    // Get the target text with lineBreaks converted
                    // for target platform.
                    // Note: The platform line-feed converisons are
                    // converted as we need them. In this case it is
                    // AFTER the string has been converted to ptag.
                    String trg = osd
                            .getDisplayTargetTextWithNewLineBreaks(p_lineBreak);

                    if (trg != null)
                    {
                        // When writting a download text file, we always
                        // add two linebreaks for proper formating.
                        trg += p_lineBreak + p_lineBreak;
                        p_outputStream.write(trg);
                    }
                }
            }

            // close format
            p_outputStream.write(m_endSignature);
            p_outputStream.flush();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_WRITE_ERROR, ex);
        }
    }

    public void writeOfflineTmxFile(OutputStream p_outputStream,
            DownloadParams p_params, int p_tmxLevel, int p_mode,
            boolean isPenaltyTmx)
    {
        OutputStreamWriter w;

        if (p_outputStream == null)
        {
            CATEGORY.error("Invalid output stream (null).");

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME,
                    "Invalid output stream.");
        }

        try
        {
            FileUtil.writeBom(p_outputStream, TmxUtil.TMX_ENCODING);
            w = new OutputStreamWriter(p_outputStream, TmxUtil.TMX_ENCODING);
            // writeOfflineTmxFile(w, p_params, p_tmxLevel, p_mode);
            writeOfflineTmxFile(w, p_params, p_tmxLevel, p_mode, isPenaltyTmx);
            w.flush();
        }
        catch (FileNotFoundException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME, ex);
        }
        catch (UnsupportedEncodingException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_ENCODING, ex);
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME, ex);
        }
    }

    private int getTmxLevel(DownloadParams p_params)
    {
        int tmxLevel = -1;
        if (p_params.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN)
        {
            tmxLevel = TmxUtil.TMX_LEVEL_ONE;
        }
        else if (p_params.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN)
        {
            tmxLevel = TmxUtil.TMX_LEVEL_TWO;
        }
        return tmxLevel;
    }

    /*
     * According to different TM, get the different source and target content.
     */
    public ArrayList<String> getSourceTargetText(OfflineSegmentData osd,
            LeverageMatch match, String sourceText, String targetText,
            String userId, boolean isFromXliff, String sourceLocal,
            String targetLocal, boolean changeCreationId, long p_jobId)
    {
        int altFlag = -100;
        targetText = match.getLeveragedTargetString();
        ArrayList<String> array = new ArrayList<String>();
        String tmpSourceText = "";

        try
        {
            if (match.getProjectTmIndex() != altFlag)
            {
                long originalSrcTuvId = match.getOriginalSourceTuvId();
                Tuv sourceTuv = ServerProxy.getTuvManager()
                        .getTuvForSegmentEditor(originalSrcTuvId, p_jobId);
                if (sourceTuv != null)
                {
                    sourceText = sourceTuv.getGxml();
                    sourceText = match.getLeveragedString(sourceText);
                }
            }

            // MT matches
            if (match.getProjectTmIndex() == Leverager.MT_PRIORITY)
            {
                userId = match.getMtName();
                if ("".equals(userId) || userId == null || changeCreationId)
                {
                    userId = "MT!";
                }

                if (isFromXliff)
                {
                    sourceText = SegmentUtil.restoreSegment(sourceText,
                            sourceLocal);
                    targetText = SegmentUtil.restoreSegment(targetText,
                            targetLocal);
                }
            }
            // Remote TM matches
            else if (match.getProjectTmIndex() == Leverager.REMOTE_TM_PRIORITY)
            {
                userId = "REMOTE_TM";

                tmpSourceText = getMatchedOriginalSource(match, sourceText);
                sourceText = GxmlUtil.stripRootTag(tmpSourceText);
            }
            // XLF matches
            else if (match.getProjectTmIndex() == Leverager.XLIFF_PRIORITY)
            {
                userId = "XLF";

                if (isFromXliff)
                {
                    Tuv tuvTemp = ServerProxy.getTuvManager()
                            .getTuvForSegmentEditor(osd.getTrgTuvId(), p_jobId);
                    String xliffTarget = tuvTemp.getTu(p_jobId)
                            .getXliffTargetGxml().getTextValue();

                    if (xliffTarget != null && Text.isBlank(xliffTarget))
                    {
                        // is populate from alt-trans
                        tmpSourceText = getMatchedOriginalSource(match,
                                sourceText);
                        sourceText = GxmlUtil.stripRootTag(tmpSourceText);
                        sourceText = EditUtil.decodeXmlEntities(sourceText);
                        targetText = EditUtil.decodeXmlEntities(targetText);
                    }
                    else
                    {
                        sourceText = SegmentUtil.restoreSegment(sourceText,
                                sourceLocal);
                        targetText = SegmentUtil.restoreSegment(targetText,
                                targetLocal);
                    }
                }

                // When the data is from MT
                try
                {
                    long tuId = osd.getTuIdAsLong();
                    TuImpl tu = (TuImpl) ServerProxy.getTuvManager()
                            .getTuForSegmentEditor(tuId, p_jobId);
                    if (tu != null && tu.isXliffTranslationMT())
                    {
                        if (changeCreationId)
                        {
                            userId = "MT!";
                        }
                        else
                        {
                            userId = Extractor.IWS_TRANSLATION_MT;
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
            // TDA matches
            else if (match.getProjectTmIndex() == Leverager.TDA_TM_PRIORITY)
            {
                userId = "TDA";
                sourceText = getMatchedOriginalSource(match, sourceText);
            }
            // PO matches
            else if (match.getProjectTmIndex() == Leverager.PO_TM_PRIORITY)
            {
                userId = "PO";
            }
            else if (match.getProjectTmIndex() == altFlag)
            {
                userId = "XLF Source";

                tmpSourceText = getMatchedOriginalSource(match, sourceText);
                sourceText = GxmlUtil.stripRootTag(tmpSourceText);
            }
            else if (match.getProjectTmIndex() == Leverager.IN_PROGRESS_TM_PRIORITY)
            {
                userId = "Job " + match.getTmId();
            }
            else
            {
                sourceText = getMatchedOriginalSource(match, sourceText);
                sourceText = match.getLeveragedString(sourceText);

                userId = match.getCreationUser();

                if (userId == null)
                {
                    userId = "";
                }
                // Tm data maybe is created from MT.
                if (changeCreationId
                        && isCreatedFromMTEngine(match.getProjectTmIndex()))
                {
                    userId = "MT!";
                }
            }
        }
        catch (Exception e)
        {
        }

        try
        {
            sourceText = getFixResultForTMX(sourceText);
            targetText = getFixResultForTMX(targetText);
        }
        catch (Exception e)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_GXML, getMsg(osd)
                            + ": " + e.getMessage());
        }

        array.add(sourceText);
        array.add(targetText);
        array.add(userId);

        return array;
    }

    private String getMsg(OfflineSegmentData p_OSD)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("SegmentID is ").append(p_OSD.getDisplaySegmentID())
                    .append("; source TUV ID is ")
                    .append(p_OSD.getSourceTuv().getId())
                    .append("; target TUV ID is ").append(p_OSD.getTrgTuvId());
            return sb.toString();
        }
        catch (Exception e)
        {
        }

        return "";
    }

    /**
     * Get matched segment's original source text (since 8.2.2 version), if
     * empty, return source text.
     * 
     * @param match
     *            Matched segments
     * @param sourceText
     *            Source text of current segment
     * @return java.lang.String Matched original source text
     */
    private String getMatchedOriginalSource(LeverageMatch match,
            String sourceText)
    {
        if (!StringUtil.isEmpty(match.getMatchedOriginalSource()))
            return match.getMatchedOriginalSource();
        else
            return sourceText;
    }

    /*
     * because the source or target tuv content maybe have illegal attribute to
     * TMX 1.4, so must check and remove them
     */
    private String getFixResultForTMX(String p_str)
    {
        TmxChecker tmxChecker = new TmxChecker();
        p_str = "<segment>" + p_str + "</segment>";
        p_str = tmxChecker.fixSegment(p_str);
        p_str = GxmlUtil.stripRootTag(p_str);

        return p_str;
    }

    private String convertLf(String s, int tmxLevel)
    {
        String replace = "";
        if (tmxLevel != TmxUtil.TMX_LEVEL_ONE)
        {
            replace = "<ph type=\"LF\">[LF]</ph>";
        }
        return s.replace("\n", replace);
    }

    public void writeOfflineTmxFile(OutputStreamWriter p_outputStream,
            DownloadParams p_params, int p_tmxLevel, int p_mode,
            boolean isPenaltyTmx)
    {
        if (p_outputStream == null)
        {
            CATEGORY.error("Null output stream.");

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_NAME,
                    "Null output stream.");
        }

        int omegaT = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT;
        boolean isOmegaT = (omegaT == p_params.getFileFormatId());

        try
        {
            int tmxLevel = p_tmxLevel;
            if (tmxLevel == -1)
            {
                tmxLevel = getTmxLevel(p_params);
            }
            // When offline download to get level 1 tmx,some element attributes
            // are still using level 2 style,only segment contents are
            // tag-stripped.When import such tmx files back system TM,they can't
            // pass tmx11.dtd check. So here use tmx14.dtd.
            TmxUtil.writeXmlHeader(p_outputStream, 2);
            TmxUtil.writeTmxOpenTag(p_outputStream, 2);
            TmxUtil.writeTmxHeader(m_sourceLocaleName, p_outputStream,
                    p_tmxLevel);
            TmxUtil.writeBodyOpenTag(p_outputStream);

            // Decide the job ID
            long jobId = -1;
            try
            {
                if (m_segmentList.size() > 0)
                {
                    long srcPageId = ((OfflineSegmentData) m_segmentList.get(0))
                            .getPageId();
                    jobId = BigTableUtil.getJobBySourcePageId(srcPageId)
                            .getId();
                }
            }
            catch (Exception e)
            {
                // this is not reliable compeletely.
                jobId = p_params.getRightJob().getId();
            }

            Set<Long> tpIdsTuvsAlreadyLoaded = new HashSet<Long>();
            if (p_mode != TmxUtil.TMX_MODE_ICE_ONLY)
            {
                for (ListIterator it = m_segmentList.listIterator(); it
                        .hasNext();)
                {
                    OfflineSegmentData osd = (OfflineSegmentData) it.next();
                    if (osd.getDisplaySegmentID().length() <= 0)
                    {
                        p_outputStream.close();

                        osd = (OfflineSegmentData) it.previous();
                        String msg = "Cannot write a format two segment:\n"
                                + "The in-memory segment ID for the segment following "
                                + osd.getDisplaySegmentID() + " is empty.";

                        CATEGORY.error(msg);

                        throw new AmbassadorDwUpException(
                                AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT,
                                msg);
                    }

                    // Find the orignal non-ice segment.
                    if (p_mode == TmxUtil.TMX_MODE_NON_ICE && isContinue(osd))
                    {
                        continue;
                    }

                    String sourceText = null;
                    String targetText = null;
                    String userId = null;
                    boolean changeCreationIdToMT = p_params
                            .getChangeCreationIdForMTSegments();
                    boolean isFromXliff = false;
                    String sourceLocale = new String();
                    String targetLocale = new String();

                    if (osd.getTargetTuv() != null)
                    {
                        sourceText = osd.getSourceTuv().getGxml();
                        targetText = osd.getTargetTuv().getGxml();

                        if (osd.getTargetTuv().getTu(jobId).getDataType()
                                .equals(IFormatNames.FORMAT_XLIFF))
                        {
                            isFromXliff = true;
                        }

                        sourceLocale = osd.getSourceTuv()
                                .getGlobalSightLocale().getLocaleCode();
                        targetLocale = osd.getTargetTuv()
                                .getGlobalSightLocale().getLocaleCode();
                    }

                    String translateTuString = null;
                    boolean isCreatedFromMT = false;
                    String[] translatedSrcTrgSegments = new String[2];
                    // ## Compose the translated segment into tmx string first,
                    // but write at last.
                    if (!isPenaltyTmx && osd.getTargetTuv() != null
                            && osd.getTargetTuv().isLocalized())
                    {
                        userId = osd.getTargetTuv().getLastModifiedUser();
                        isCreatedFromMT = isCreatedFromMTEngine(userId);
                        if (isAddLocalizedTargetAsTu(p_mode, isCreatedFromMT))
                        {
                            translateTuString = getTranslatedTuString(osd,
                                    sourceText, sourceLocale, targetText,
                                    targetLocale, isOmegaT, isFromXliff,
                                    p_tmxLevel, changeCreationIdToMT,
                                    translatedSrcTrgSegments);
                        }
                    }

                    // avoid to output two same tu for Machine Translate
                    boolean isAddTrasnlatedTU = true;
                    if (StringUtil.isEmpty(translateTuString))
                    {
                        isAddTrasnlatedTU = false;
                    }

                    // Write TM matches into tmx.
                    StringBuffer exactAndFuzzy = new StringBuffer();
                    List<LeverageMatch> allLMs = new ArrayList<LeverageMatch>();
                    boolean hasMatches = osd.hasTMMatches()
                            || osd.hasMTMatches();
                    if (isPenaltyTmx)
                    {
                        hasMatches = osd.hasRefTmsTMMatches()
                                || osd.hasRefTmsMTMatches();
                    }
                    if (hasMatches)
                    {
                        // Load Tuvs/XliffAlts of current page for performance.
                        loadCurrentTargetPageTuvsForPerformance(
                                tpIdsTuvsAlreadyLoaded, osd.getTrgTuvId(),
                                jobId);

                        allLMs = getAllLeverageMatches(osd, jobId, isPenaltyTmx);
                        Iterator<LeverageMatch> matches = allLMs.iterator();

                        while (matches.hasNext())
                        {
                            LeverageMatch match = matches.next();
                            // Set current user id as default create_id
                            userId = p_params.getUser().getUserId();
                            ArrayList<String> array = getSourceTargetText(osd,
                                    match, sourceText, targetText, userId,
                                    isFromXliff, sourceLocale, targetLocale,
                                    changeCreationIdToMT, jobId);
                            sourceText = array.get(0);
                            targetText = array.get(1);
                            userId = array.get(2);
                            isCreatedFromMT = isCreatedFromMTEngine(match
                                    .getProjectTmIndex());

                            if (isOmegaT)
                            {
                                sourceText = convertOmegaT(sourceText, osd);
                                targetText = convertOmegaT(targetText, osd);
                            }
                            else if (m_isConvertLf)
                            {
                                sourceText = convertLf(sourceText, p_tmxLevel);
                                targetText = convertLf(targetText, p_tmxLevel);
                            }

                            if (p_mode == TmxUtil.TMX_MODE_INC_ALL
                                    || (p_mode == TmxUtil.TMX_MODE_MT_ONLY && isCreatedFromMT)
                                    || (p_mode == TmxUtil.TMX_MODE_TM_ONLY && !isCreatedFromMT)
                                    || (p_mode == TmxUtil.TMX_MODE_NON_ICE && !isCreatedFromMT))
                            {
                                if (isSameAsLocalizedSegments(
                                        translatedSrcTrgSegments, sourceText,
                                        targetText))
                                {
                                    isAddTrasnlatedTU = false;
                                }

                                TmxUtil.TmxTuvInfo srcTuvInfo = new TmxUtil.TmxTuvInfo(
                                        sourceText, m_sourceLocaleName, null,
                                        null, null, null);
                                TmxUtil.TmxTuvInfo trgTuvInfo = new TmxUtil.TmxTuvInfo(
                                        targetText, m_targetLocaleName,
                                        "MT!".equals(userId) ? "MT!" : match
                                                .getCreationUser(),
                                        match.getCreationDate(),
                                        "MT!".equals(userId) ? "MT!" : match
                                                .getModifyUser(),
                                        match.getModifyDate());
                                exactAndFuzzy.append(TmxUtil.composeTu(
                                        srcTuvInfo, trgTuvInfo, p_tmxLevel,
                                        match.getSid(), isPenaltyTmx));
                            }
                        }

                        p_outputStream.write(exactAndFuzzy.toString());
                    }

                    if (isAddTrasnlatedTU)
                    {
                        p_outputStream.write(translateTuString);
                    }
                }
            }
            else if (m_segmentListUnmerged != null)
            {
                // write ice for omegat
                for (ListIterator it = m_segmentListUnmerged.listIterator(); it
                        .hasNext();)
                {
                    OfflineSegmentData osd = (OfflineSegmentData) it.next();
                    if (osd.getDisplaySegmentID().length() <= 0)
                    {
                        p_outputStream.close();

                        osd = (OfflineSegmentData) it.previous();
                        String msg = "Cannot write a format two segment:\n"
                                + "The in-memory segment ID for the segment following "
                                + osd.getDisplaySegmentID() + " is empty.";

                        CATEGORY.error(msg);

                        throw new AmbassadorDwUpException(
                                AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT,
                                msg);
                    }

                    String matchType = osd.getDisplayMatchType();
                    boolean isIce = matchType == null ? false : matchType
                            .contains("Context Exact Match");

                    if (isIce)
                    {
                        String userId = null;
                        String sourceText = null;
                        String targetText = null;
                        boolean isFromXliff = false;
                        String targetLocal = new String();
                        String sourceLocal = new String();
                        boolean isCreatedFromMT = false;
                        boolean changeCreationId = p_params
                                .getChangeCreationIdForMTSegments();
                        boolean addThis = false;
                        String sid = null;

                        if (osd.getTargetTuv() != null)
                        {
                            sourceText = osd.getSourceTuv().getGxml();
                            targetText = osd.getTargetTuv().getGxml();
                            sid = osd.getSourceTuv().getSid();

                            if (osd.getTargetTuv().getTu(jobId).getDataType()
                                    .equals(IFormatNames.FORMAT_XLIFF))
                            {
                                isFromXliff = true;
                            }

                            sourceLocal = osd.getSourceTuv()
                                    .getGlobalSightLocale().getLocaleCode();
                            targetLocal = osd.getTargetTuv()
                                    .getGlobalSightLocale().getLocaleCode();
                        }

                        List<LeverageMatch> allLMs = new ArrayList<LeverageMatch>();
                        boolean hasMatches = osd.hasTMMatches()
                                || osd.hasMTMatches();
                        if (isPenaltyTmx)
                        {
                            hasMatches = osd.hasRefTmsTMMatches()
                                    || osd.hasRefTmsMTMatches();
                        }
                        if (hasMatches)
                        {
                            // Load Tuvs/XliffAlts of current page for
                            // performance.
                            loadCurrentTargetPageTuvsForPerformance(
                                    tpIdsTuvsAlreadyLoaded, osd.getTrgTuvId(),
                                    jobId);

                            allLMs = getAllLeverageMatches(osd, jobId,
                                    isPenaltyTmx);
                        }

                        if (allLMs != null
                                && allLMs.size() > 0
                                && osd.getTargetTuv() != null
                                && osd.getTargetTuv().isExactMatchLocalized(
                                        jobId))
                        {
                            LeverageMatch match = allLMs.get(0);

                            if (sid != null && !sid.equals(match.getSid()))
                            {
                                for (int i = 1; i < allLMs.size(); i++)
                                {
                                    LeverageMatch match1 = allLMs.get(i);

                                    if (sid.equals(match1.getSid()))
                                    {
                                        match = match1;
                                        break;
                                    }
                                }
                            }

                            userId = match.getCreationUser();
                            ArrayList<String> array = getSourceTargetText(osd,
                                    match, sourceText, targetText, userId,
                                    isFromXliff, sourceLocal, targetLocal,
                                    changeCreationId, jobId);
                            sourceText = array.get(0);
                            targetText = array.get(1);
                            userId = array.get(2);
                            isCreatedFromMT = isCreatedFromMTEngine(match
                                    .getProjectTmIndex());

                            if (!isCreatedFromMT)
                            {
                                addThis = true;
                            }
                            else
                            {
                                addThis = false;
                            }

                            if (addThis)
                            {
                                String tempSource = new String();
                                String tempTarget = new String();

                                if (isFromXliff)
                                {
                                    tempSource = getFixResultForTMX(SegmentUtil
                                            .restoreSegment(sourceText,
                                                    sourceLocal));
                                    tempTarget = getFixResultForTMX(SegmentUtil
                                            .restoreSegment(targetText,
                                                    targetLocal));
                                }
                                else
                                {
                                    tempSource = getFixResultForTMX(sourceText);
                                    tempTarget = getFixResultForTMX(targetText);
                                }

                                if (isOmegaT)
                                {
                                    tempSource = convertOmegaT(tempSource, osd);
                                    tempTarget = convertOmegaT(tempTarget, osd);
                                }
                                else if (m_isConvertLf)
                                {
                                    tempSource = convertLf(tempSource,
                                            p_tmxLevel);
                                    tempTarget = convertLf(tempTarget,
                                            p_tmxLevel);
                                }

                                TmxUtil.TmxTuvInfo srcTuvInfo = new TmxUtil.TmxTuvInfo(
                                        tempSource, m_sourceLocaleName, null,
                                        null, null, null);
                                TmxUtil.TmxTuvInfo trgTuvInfo = new TmxUtil.TmxTuvInfo(
                                        tempTarget, m_targetLocaleName,
                                        "MT!".equals(userId) ? "MT!" : match
                                                .getCreationUser(),
                                        match.getCreationDate(),
                                        "MT!".equals(userId) ? "MT!" : match
                                                .getModifyUser(),
                                        match.getModifyDate());
                                String ttuString = TmxUtil
                                        .composeTu(srcTuvInfo, trgTuvInfo,
                                                p_tmxLevel, match.getSid(),
                                                isOmegaT,
                                                OmegaTConst.tu_type_ice,
                                                osd.getDisplaySegmentID(),
                                                isPenaltyTmx);
                                p_outputStream.write(ttuString.toString());
                            }
                        }
                    }
                }
            }

            TmxUtil.writeBodyCloseTag(p_outputStream);
            TmxUtil.writeTmxCloseTag(p_outputStream);
            p_outputStream.flush();
        }
        catch (IOException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_WRITE_ERROR, ex);
        }
    }

    // I am not sure what these codes mean actually, just move them into a
    // separate method to make it easier to read.
    private boolean isContinue(OfflineSegmentData osd)
    {
        String matchType = osd.getDisplayMatchType();
        boolean isIce = matchType == null ? false : matchType
                .contains("Context Exact Match");

        if (isIce && m_segmentListUnmerged != null
                && m_segmentListUnmerged.size() > 0)
        {
            int osdHash = osd.hashCode();
            for (ListIterator it2 = m_segmentListUnmerged.listIterator(); it2
                    .hasNext();)
            {
                OfflineSegmentData osd2 = (OfflineSegmentData) it2.next();
                String matchType2 = osd2.getDisplayMatchType();
                boolean isIce2 = matchType2 == null ? false : matchType2
                        .contains("Context Exact Match");
                if (!isIce2 && osd2.hashCode() == osdHash)
                {
                    osd = osd2;
                    break;
                }
            }
        }

        // if the segment is still ICE match, continue
        matchType = osd.getDisplayMatchType();
        isIce = matchType == null ? false : matchType
                .contains("Context Exact Match");

        return isIce;
    }

    private boolean isAddLocalizedTargetAsTu(int p_mode, boolean isCreatedFromMT)
    {
        boolean addThis = false;
        if (p_mode == TmxUtil.TMX_MODE_INC_ALL
                || (p_mode == TmxUtil.TMX_MODE_MT_ONLY && isCreatedFromMT)
                || (p_mode == TmxUtil.TMX_MODE_TM_ONLY && !isCreatedFromMT)
                || (p_mode == TmxUtil.TMX_MODE_NON_ICE && !isCreatedFromMT))
        {
            addThis = true;
        }
        else
        {
            addThis = false;
        }

        return addThis;
    }

    /**
     * Compose the tmx tu string for localized segment.
     */
    private String getTranslatedTuString(OfflineSegmentData osd,
            String sourceText, String sourceLocale, String targetText,
            String targetLocale, boolean isOmegaT, boolean isFromXliff,
            int p_tmxLevel, boolean changeCreationIdToMT,
            String[] translatedSrcTrgSegments)
    {
        String tempSource = new String();
        String tempTarget = new String();

        if (isFromXliff)
        {
            tempSource = getFixResultForTMX(SegmentUtil.restoreSegment(
                    sourceText, sourceLocale));
            tempTarget = getFixResultForTMX(SegmentUtil.restoreSegment(
                    targetText, targetLocale));
        }
        else
        {
            tempSource = getFixResultForTMX(GxmlUtil.stripRootTag(sourceText));
            tempTarget = getFixResultForTMX(GxmlUtil.stripRootTag(targetText));
        }

        if (isOmegaT)
        {
            tempSource = convertOmegaT(tempSource, osd);
            tempTarget = convertOmegaT(tempTarget, osd);
        }
        else if (m_isConvertLf)
        {
            tempSource = convertLf(tempSource, p_tmxLevel);
            tempTarget = convertLf(tempTarget, p_tmxLevel);
        }

        translatedSrcTrgSegments[0] = tempSource;
        translatedSrcTrgSegments[1] = tempTarget;

        String userId = osd.getTargetTuv().getLastModifiedUser();
        // If "Set Creation ID to 'MT!' for machine translated
        // segments" is checked, creationId should be changed to "MT!".
        boolean isCreatedFromMT = isCreatedFromMTEngine(userId);
        if (changeCreationIdToMT && isCreatedFromMT)
        {
            userId = "MT!";
        }

        Tuv sourceTuv = osd.getSourceTuv();
        Tuv targetTuv = osd.getTargetTuv();
        TmxUtil.TmxTuvInfo srcTuvInfo = new TmxUtil.TmxTuvInfo(tempSource,
                m_sourceLocaleName, sourceTuv.getCreatedUser(),
                sourceTuv.getCreatedDate(), sourceTuv.getLastModifiedUser(),
                sourceTuv.getLastModified());
        TmxUtil.TmxTuvInfo trgTuvInfo = new TmxUtil.TmxTuvInfo(tempTarget,
                m_targetLocaleName, "MT!".equals(userId) ? "MT!"
                        : targetTuv.getCreatedUser(),
                targetTuv.getCreatedDate(), "MT!".equals(userId) ? "MT!"
                        : targetTuv.getLastModifiedUser(),
                targetTuv.getLastModified());

        return TmxUtil.composeTu(srcTuvInfo, trgTuvInfo, p_tmxLevel, null,
                false);
    }

    private boolean isSameAsLocalizedSegments(
            String[] translatedSrcTrgSegments, String sourceText,
            String targetText)
    {
        if (translatedSrcTrgSegments == null
                || translatedSrcTrgSegments.length != 2)
            return false;

        boolean isSourceSame = sourceText != null
                && sourceText.equals(translatedSrcTrgSegments[0]);
        boolean isTargetSame = targetText != null
                && targetText.equals(translatedSrcTrgSegments[1]);
        if (isSourceSame && isTargetSame)
        {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private List<LeverageMatch> getAllLeverageMatches(OfflineSegmentData osd,
            long p_jobId, boolean isPenaltyTmx) throws TuvException,
            RemoteException, GeneralException
    {
        List<LeverageMatch> allLMs = new ArrayList<LeverageMatch>();

        // Matches from "leverage_match_xx" table
        if (isPenaltyTmx)
        {
            allLMs.addAll(osd.getOriginalFuzzyLeverageMatchRefTmsList());
        }
        else
        {
            allLMs.addAll(osd.getOriginalFuzzyLeverageMatchList());
        }

        Tuv currTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                osd.getTrgTuvId(), p_jobId);

        // Add all xliff alts into the match list
        List<LeverageMatch> lmFromXliffAlts = convertXliffAltToLeverageMatches(currTuv
                .getXliffAlt(true));
        allLMs.addAll(lmFromXliffAlts);

        LeverageMatch.orderMatchResult(allLMs);
        SortUtil.sort(allLMs, new GeneralComparatorBySID());

        return allLMs;
    }

    /**
     * Load Tuvs/XliffAlts of current page for performance.
     */
    private void loadCurrentTargetPageTuvsForPerformance(
            Set<Long> tpIdsAlreadyLoaded, long tuvId, long p_jobId)
    {
        try
        {
            Tuv currTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                    tuvId, p_jobId);
            TargetPage myTp = ((TuvImpl) currTuv).getTargetPage(p_jobId);
            if (!tpIdsAlreadyLoaded.contains(myTp.getIdAsLong()))
            {
                SegmentTuvUtil.getExportTuvs(myTp.getLocaleId(), myTp
                        .getSourcePage().getId());
                tpIdsAlreadyLoaded.add(myTp.getIdAsLong());
            }
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Need write matches from xliff file "alt-trans" into offline tmx, so
     * convert "XliffAlt" to "LeverageMatch" object.
     */
    private List<LeverageMatch> convertXliffAltToLeverageMatches(
            Set<XliffAlt> xliffAltSet)
    {
        List<LeverageMatch> lms = new ArrayList<LeverageMatch>();
        if (xliffAltSet != null && xliffAltSet.size() > 0)
        {
            for (XliffAlt alt : xliffAltSet)
            {
                LeverageMatch lm = new LeverageMatch();
                lm.setMatchedOriginalSource(EditUtil.decodeXmlEntities(alt
                        .getSourceSegment()));
                lm.setMatchedText(EditUtil.decodeXmlEntities(alt.getSegment()));
                float score = (float) TdaHelper
                        .PecentToDouble(alt.getQuality());
                lm.setScoreNum(score);
                lm.setProjectTmIndex(-100);
                lm.setOriginalSourceTuvId(0);
                lms.add(lm);
            }
        }

        return lms;
    }

    /**
     * Convert TMX tags to OmegaT tags
     * 
     * @param tmxContent
     * @return
     */
    private String convertOmegaT(String tmxContent, OfflineSegmentData osd)
    {
        if (tmxContent != null && tmxContent.contains("<")
                && tmxContent.contains("/>"))
        {
            String oriSource = osd.getDisplaySourceText();
            try
            {
                convertor.tmx2Pseudo(oriSource, tuvPTagData);
                convertor.tmx2Pseudo(tmxContent, tmxPTagData);
            }
            catch (Exception e)
            {
                CATEGORY.error("Cannot init tag list, use default tmx", e);
            }

            Hashtable tuvMap = tuvPTagData.getPseudo2TmxMap();
            Hashtable tmxMap = tmxPTagData.getPseudo2TmxMap();
            Hashtable tmxMissedMap = tmxPTagData.getMissedPseudo2TmxMap();

            // do not convert to long format for non extact match
            if (tuvMap == null
                    || tmxMap == null
                    || tuvPTagData.getSrcCompleteTagList().size() != tmxPTagData
                            .getSrcCompleteTagList().size())
            {
                return tmxContent;
            }

            // convert tmx tags to long format
            if (tuvMap != null && tmxMap != null && tuvMap.size() > 0)
            {
                Enumeration tuvKeys2 = tuvMap.keys();

                while (tuvKeys2.hasMoreElements())
                {
                    Object tuvKey2 = tuvKeys2.nextElement();
                    Object tmxKey = null;
                    int srcListIndex = -1;
                    if (tmxMap.containsKey(tuvKey2))
                    {
                        tmxKey = tuvKey2;
                    }
                    else
                    {
                        String tuvKeyStr = tuvKey2.toString();
                        // find tag index
                        Vector tuvTagList = tuvPTagData.getSrcCompleteTagList();
                        int index = -1;
                        if (tuvTagList != null && tuvTagList.size() > 0)
                        {
                            for (Object tagNodeObj : tuvTagList)
                            {
                                TagNode tuvTagNode = (TagNode) tagNodeObj;
                                if (tuvKeyStr.equals(tuvTagNode.getPTagName()))
                                {
                                    index = tuvTagNode.getSourceListIndex();
                                    srcListIndex = index;
                                    break;
                                }
                            }
                        }

                        // get tuv key with same index
                        if (index != -1)
                        {
                            TagNode tmxTagNode = tmxPTagData
                                    .getSrcTagItem(index);
                            if (tmxTagNode != null)
                            {
                                tmxKey = tmxTagNode.getPTagName();
                            }
                        }
                    }

                    String tuvTag = null;
                    String tmxTag = null;

                    if (tuvKey2 != null)
                    {
                        Object tuvValue = tuvMap.get(tuvKey2);
                        tuvTag = tuvValue.toString();
                    }

                    if (tmxKey != null && tmxMap.containsKey(tmxKey))
                    {
                        Object tmxValue = tmxMap.get(tmxKey);
                        tmxTag = tmxValue.toString();
                    }
                    else if (tmxMissedMap != null && tmxMissedMap.size() > 0)
                    {
                        tmxTag = (String) tmxMissedMap.get("" + srcListIndex);
                    }

                    if (tuvTag != null && tmxTag != null)
                    {
                        tmxContent = convertTmxToLongFormat(tmxContent, tuvTag,
                                tmxTag);
                    }
                }

            }
        }

        return tmxContent;
    }

    /**
     * Convert TMX tags to long format, sample: <bpt i="1" type="bold" x="1" />
     * to <bpt i="1" type="bold" x="1">&lt;b&gt;</bpt>
     * 
     * @return
     */
    private String convertTmxToLongFormat(String tmxContent, String tuvTag,
            String tmxTag)
    {
        StringIndex si = StringIndex.getValueBetween(new StringBuffer(tuvTag),
                0, ">", "<");
        if (si != null && si.value != null)
        {
            String tmx = tmxTag;
            int index = tmx.indexOf(">");
            String pre = tmx.substring(0, index);
            String end = tmx.substring(index + 1);
            String newTmx = pre + ">" + si.value + end;

            if (tmxContent.contains(tmx))
            {
                tmxContent = tmxContent.replace(tmx, newTmx);
            }
            else if (tmxContent.contains(pre + "/>"))
            {
                tmxContent = tmxContent.replace(pre + "/>", newTmx);
            }
        }
        return tmxContent;
    }

    /**
     * Make a format one file name using the current header information.
     */
    public String makeFormatOneFileName()
    {
        return m_workflowID + AmbassadorDwUpConstants.FILE_NAME_BREAK
                + m_taskID + AmbassadorDwUpConstants.FILE_NAME_BREAK + m_pageId
                + AmbassadorDwUpConstants.FILE_EXT_FMT1;
    }

    /**
     * Make a format two file name using the current header information.
     */
    public String makeFormatTwoTempFileName()
    {
        return m_workflowID + AmbassadorDwUpConstants.FILE_NAME_BREAK
                + m_taskID + AmbassadorDwUpConstants.FILE_NAME_BREAK + m_pageId
                + AmbassadorDwUpConstants.FILE_EXT_FMT2;
    }

    /**
     * Helps to determine and remove linebreaks that were part of the offline
     * file formatting and not part of the text.
     */
    private String removeLastNewline(String segment)
    {
        String newline = null;

        // determine newline type
        if (segment.endsWith("\r\n"))
        {
            newline = "\r\n";
        }
        else if (segment.endsWith("\r"))
        {
            newline = "\r";
        }
        else if (segment.endsWith("\n"))
        {
            newline = "\n";
        }

        if (newline != null)
        {
            // Two newlines were added to the end of every
            // segment during download to format the file.

            // take last one off - if exists
            if (segment.endsWith(newline))
            {
                segment = segment.substring(0,
                        segment.length() - newline.length());
            }

            // take one more off - if exists
            if (segment.endsWith(newline))
            {
                segment = segment.substring(0,
                        segment.length() - newline.length());
            }

            // The remaining ones are original or the user has added them.
        }

        return segment;
    }

    /**
     * Verifies that all information required to create a download file header
     * is present.
     */
    public void verifyHeader() throws AmbassadorDwUpException
    {
        if ((m_startSignature == null || m_startSignature.length() <= 0)
                || (m_encoding == null || m_encoding.length() <= 0)
                || (m_documentFormat == null || m_documentFormat.length() <= 0)
                || (m_placeholderFormat == null || m_placeholderFormat.length() <= 0)
                || (m_sourceLocaleName == null || m_sourceLocaleName.length() <= 0)
                || (m_targetLocaleName == null || m_targetLocaleName.length() <= 0)
                || (m_pageId == null || m_pageId.length() <= 0)
                || (m_workflowID == null || m_workflowID.length() <= 0)
                || ((m_taskID == null || m_taskID.length() <= 0) && (m_taskIds == null || m_taskIds
                        .size() == 0))
                || (m_endSignature == null || m_endSignature.length() <= 0))
        {
            String msg = "Invalid header information - parameters follow:"
                    + "\nStartSignature = " + m_startSignature
                    + "\nEncoding = " + m_encoding + "\nDocument Format = "
                    + m_documentFormat + "\nPlaceholder Format = "
                    + m_placeholderFormat + "\nSource Locale = "
                    + m_sourceLocaleName + "\nTarget Locale = "
                    + m_targetLocaleName + "\nPageId = " + m_pageId
                    + "\nWorkflowId = " + m_workflowID + "\nTaskId = "
                    + m_taskID + "\nEnd Signature = " + m_endSignature;

            CATEGORY.error(msg);

            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.INVALID_FILE_FORMAT, msg);
        }
    }

    public Vector getSegmentList()
    {
        return m_segmentList;
    }

    public void setSegmentList(Vector segmentList)
    {
        this.m_segmentList = segmentList;
    }

    public Vector<OfflineSegmentData> getSegmentListUnmerged()
    {
        return m_segmentListUnmerged;
    }

    public void setSegmentListUnmerged(Vector<OfflineSegmentData> segmentList)
    {
        this.m_segmentListUnmerged = segmentList;
    }

    class GeneralComparatorBySID implements Comparator
    {

        public int compare(Object o1, Object o2)
        {
            LeverageMatch match1 = (LeverageMatch) o1;
            LeverageMatch match2 = (LeverageMatch) o2;
            int result = (int) (match2.getScoreNum() - match2.getScoreNum());
            if (result == 0)
            {
                String sid1 = match1.getSid();
                String sid2 = match2.getSid();
                if (sid1 == null || sid2 == null)
                {
                    return result;
                }
                result = sid1.compareTo(sid2);
            }
            return result;
        }

    }

    /**
     * Judge if the userId is machine translation engine.
     * 
     * @param p_userId
     *            TU or Tuv CreationId
     * @return
     */
    private boolean isCreatedFromMTEngine(String p_userId)
    {
        boolean isCreatedFromMTEngine = false;
        if (p_userId == null)
        {
            return false;
        }

        if ("MT!".equals(p_userId))
        {
            return true;
        }

        String uid = p_userId.toLowerCase();
        String[] supportedMTEngines = MachineTranslator.gsSupportedMTEngines;
        for (int j = 0; j < supportedMTEngines.length; j++)
        {

            if (p_userId != null && uid.indexOf("_mt") > -1
                    && uid.indexOf(supportedMTEngines[j].toLowerCase()) > -1)
            {
                isCreatedFromMTEngine = true;
                break;
            }
        }

        return isCreatedFromMTEngine;
    }

    private boolean isCreatedFromMTEngine(int p_projectTmIndex)
    {

        if (p_projectTmIndex == Leverager.MT_PRIORITY)
        {
            return true;
        }

        return false;
    }

    public long getInContextMatchWordCount()
    {
        return m_inContextMatchWordCount;
    }

    public void setInContextMatchWordCount(long m_inContextMatchWordCount)
    {
        this.m_inContextMatchWordCount = m_inContextMatchWordCount;
    }

    public boolean isCombined()
    {
        return m_isCombined;
    }

    public void setCombined(boolean isCombined)
    {
        this.m_isCombined = isCombined;
    }

    public boolean isConsolated()
    {
        return m_isConsolated;
    }

    public void setConsolated(boolean m_isConsolated)
    {
        this.m_isConsolated = m_isConsolated;
    }

    public long getJobId()
    {
        return jobId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public void setCompanyId(long companyId)
    {
        m_companyId = companyId;
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setTaskIds(List<Long> allTaskIds)
    {
        m_taskIds = allTaskIds;
    }

    public List<Long> getTaskIds()
    {
        return m_taskIds;
    }

    public void setAllJobIds(String jobids)
    {
        m_allJobIds = jobids;
    }

    public String getAllJobIds()
    {
        return m_allJobIds;
    }

    public void setAllJobNames(String jobnames)
    {
        m_alljobnames = jobnames;
    }

    public String getAllJobNames()
    {
        return m_alljobnames;
    }

    public void setIsRepetitions(boolean isRepetitons)
    {
        m_isRepetitons = isRepetitons;
    }

    public boolean getIsRepetitons()
    {
        return m_isRepetitons;
    }

    public int getTMEditType()
    {
        return TMEditType;
    }

    public String getDisplayTMEditType()
    {
        return displayTMEditType;
    }

    public void setDisplayTMEditType(String displayTMEditType)
    {
        this.displayTMEditType = displayTMEditType;
    }

    public String getServerInstanceID()
    {
        return m_instanceID;
    }

    public void setServerInstanceID(String p_instanceID)
    {
        m_instanceID = p_instanceID;
    }

    public void addXlfTargetState(String tuId, String state)
    {
        tuId2XlfTrgStateMap.put(tuId, state);
    }

    public String getXlfTargetState(String tuId)
    {
        return tuId2XlfTrgStateMap.get(tuId);
    }

    public void setPreserveSourceFolder(boolean preserveSourceFolder)
    {
        this.preserveSourceFolder = preserveSourceFolder;
    }

    public boolean isPreserveSourceFolder()
    {
        return preserveSourceFolder;
    }

    /**
     * @return the m_isXliff20
     */
    public boolean isXliff20()
    {
        return m_isXliff20;
    }

    /**
     * @param m_isXliff20
     *            the m_isXliff20 to set
     */
    public void setIsXliff20(boolean m_isXliff20)
    {
        this.m_isXliff20 = m_isXliff20;
    }
}
