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

package com.globalsight.everest.edit.offline;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.PageSegments;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

/**
 * Offline Edit helper class.
 */
public class OfflineEditHelper
{
    static private final Logger s_category = Logger
            .getLogger(OfflineEditHelper.class);

    static public final String UPLOAD_FAIL_SUBJECT = "uploadFailedSubject";
    static public final String UPLOAD_FAIL_MESSAGE = "uploadFailedMessage";
    static public final String UPLOAD_SUCCESSFUL_SUBJECT = "uploadSuccessfulSubject";
    static public final String UPLOAD_SUCCESSFUL_MESSAGE = "uploadSuccessfulMessage";

    // determines whether the system-wide notification is enabled
    static private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /** Creates a new instance of OfflineEditHelper. */
    public OfflineEditHelper()
    {
    }

    /**
     * Notifies the user about the upload process.
     */
    static public void notifyUser(User p_user, String p_fileName,
            String p_localPair, String p_subjectKey, String p_messageKey,
            String p_compandIsStr)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            String capLoginUrl = config
                    .getStringParameter(SystemConfiguration.CAP_LOGIN_URL);

            String[] args =
            { p_fileName, capLoginUrl, p_localPair };

            ServerProxy.getMailer().sendMailFromAdmin(p_user, args,
                    p_subjectKey, p_messageKey, p_compandIsStr);
        }
        catch (Exception ex)
        {
            // do not throw an exception if email notification fails...
            s_category.error("failed to send upload e-mail: " + "p_user="
                    + p_user + ", p_fileName=" + p_fileName + ", p_localPair="
                    + p_localPair + ", p_subjectKey=" + p_subjectKey
                    + ", p_messageKey=" + p_messageKey, ex);
        }
    }

    /**
     * Get a source/target locale pair displayed based on the display locale.
     * 
     * @param p_sourceLocale
     *            - The source locale.
     * @param p_targetLocale
     *            - The target locale.
     * @param p_displayLocale
     *            - The locale used for displaying the locale pair.
     * @return The string representation of the locale pair based on the display
     *         locale (i.e. English (United States) / French (France))
     */
    static public String localePair(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, GlobalSightLocale p_displayLocale)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_sourceLocale.getDisplayName(p_displayLocale));
        sb.append(" / ");
        sb.append(p_targetLocale.getDisplayName(p_displayLocale));

        return sb.toString();
    }

    /**
     * Builds a subflow-root id. The caller must append a segment id to form a
     * full offline id.
     * 
     * @param p_tuIdAsStr
     *            the TU id of the target tuv.
     * @param m_parentOfSubTagName
     * @param p_delimter
     *            used to separate portions of the segments ID
     * @return the subflow root id as a string
     */
    static public String makeSubSegIdPrefix(String p_tuId,
            String m_parentOfSubTagName, char p_delimiter)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_tuId);
        if (m_parentOfSubTagName != null)
        {
            sb.append(p_delimiter);
            sb.append(m_parentOfSubTagName);
        }
        sb.append(p_delimiter);

        return sb.toString();
    }

    /**
     * Returns the ptag under which this subflow resides.
     * 
     * @param p_parentOfSub
     *            element used to create and insert the parent tag name. If
     *            null, the parent tag-name will not be included in the result.
     * @param p_placeholderFormatId
     *            (compact or verbose) See AmbassadorDwUpConstants
     * @return the subflow root id as a string
     */
    static public String getParentOfSubTagName(GxmlElement p_parentOfSub,
            int p_placeholderFormatId)
    {
        String displayParentTagName = null;
        StringBuffer sb = new StringBuffer();

        if (p_parentOfSub != null)
        {
            // try getting the full name of the parent placeholder
            try
            {
                String parentTagName = p_parentOfSub.getName();
                Hashtable attributes = new Hashtable();

                attributes.put("type", p_parentOfSub.getAttribute("type"));
                attributes.put("x", p_parentOfSub.getAttribute("x"));

                PseudoData PD = new PseudoData();
                PD.setMode(p_placeholderFormatId);
                displayParentTagName = PD.makePseudoTagName(parentTagName,
                        attributes, "");
            }
            catch (Exception ex)
            {
                // try getting the x attribute only
                try
                {
                    displayParentTagName = p_parentOfSub.getAttribute("x");
                }
                catch (Exception ex2)
                {
                    // drop it
                    displayParentTagName = null;
                }
            }
        }

        if (displayParentTagName != null && displayParentTagName.length() > 0)
        {
            sb.append(PseudoConstants.PSEUDO_OPEN_TAG);
            sb.append(displayParentTagName);
            sb.append(PseudoConstants.PSEUDO_CLOSE_TAG);
        }

        return sb.toString();
    }

    /**
     * Determines if a segment id is a subflow segment id.
     * 
     * @return true when the id is a subflow id. Otherwise false.
     */
    static public boolean isSubflowSegmentId(String p_segId)
    {
        // To cover both upload and download, we need to determine
        // this from the ID string itself. Because, on Upload, we
        // only have the string id to work with.
        return (p_segId.indexOf(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER) == -1 && p_segId
                .indexOf(AmbassadorDwUpConstants.BOOKMARK_SEG_ID_DELIM) == -1) ? false
                : true;
    }

    static public PageSegments getPageSegments(SourcePage m_srcPage,
            GlobalSightLocale p_targetLocale, Map p_mergeOverrideDirective,
            boolean p_isUpload, boolean p_mergeEnabled)
            throws OfflineEditorManagerException
    {
        PageSegments pageSegs = null;

        // note: offline works only with one target locale at a time
        ArrayList<GlobalSightLocale> trgLocales = new ArrayList<GlobalSightLocale>();
        trgLocales.add(p_targetLocale);

        try
        {
            // editable / non-editable Tuvs is no longer an issue
            // since TuvManager now uses JDBC.
            pageSegs = ServerProxy.getTuvManager().getPageSegments(m_srcPage,
                    trgLocales);

            if (p_isUpload)
            {
                // Reconfigure PageSegments merge order (according to
                // upload) - if there are merged segs
                if (p_mergeEnabled)
                {
                    pageSegs.mergeByMergeDirective(p_mergeOverrideDirective,
                            p_targetLocale);
                }
            }
        }
        catch (Exception ex)
        {
            String args[] =
            { m_srcPage.getName(), p_targetLocale.toString(),
                    p_mergeOverrideDirective.toString() };

            OfflineEditorManagerException ex2 = new OfflineEditorManagerException(
                    OfflineEditorManagerException.MSG_TO_GET_PAGE_SEGMENTS,
                    args, ex);

            // logged at a higher level
            throw ex2;
        }

        return pageSegs;
    }

    /**
     * Converts delimiters to internal delimiters.
     */
    static public String convertToInternalSegId(String p_segId)
    {
        return p_segId.trim().replace(
                AmbassadorDwUpConstants.BOOKMARK_SEG_ID_DELIM,
                AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER);
    }

    /**
     * Returns true if the download environment suppports split/merge, otherwise
     * false.
     */
    static public boolean isSplitMergeEnabledFormat(DownloadParams p_dldParams)
    {
        return p_dldParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE;
    }

    /**
     * Attempts to delete the specified file. If it cannot be deleted, it is
     * flagged as deleteOnExit.
     */
    static public void deleteFile(File p_tmpFile)
    {
        try
        {
            p_tmpFile.delete();
        }
        catch (Exception ex)
        {
            // ignore - tmp file may not be deletable on windows
        }
        finally
        {
            if (p_tmpFile.exists())
            {
                // Tell VM to delete file on exit when it still exists.
                p_tmpFile.deleteOnExit();
            }
        }
    }
}
