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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.UTC;
import com.globalsight.util.edit.GxmlUtil;

public class TMSearchEditEntryHandlerHelper
{
    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    public static void setLable(HttpServletRequest request,
            ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_tm_name");
        setLableToJsp(request, bundle, "lb_ok");
        setLableToJsp(request, bundle, "lb_cancel");
        setLableToJsp(request, bundle, "lb_tm_search_tu_attributes");
        setLableToJsp(request, bundle, "lb_modified_on");
        setLableToJsp(request, bundle, "lb_modified_by");
        setLableToJsp(request, bundle, "lb_created_on");
        setLableToJsp(request, bundle, "lb_created_by");
        setLableToJsp(request, bundle, "lb_sid");
        setLableToJsp(request, bundle, "lb_tm_search_sys_attrs");
        setLableToJsp(request, bundle, "lb_source");
        setLableToJsp(request, bundle, "lb_target");
        setLableToJsp(request, bundle, "lb_tm_edit_entry");
        setLableToJsp(request, bundle, "lb_tm_edit_entry_description");
        setLableToJsp(request, bundle, "msg_tm_search_add_source_null");
        setLableToJsp(request, bundle, "msg_tm_search_add_target_null");
        setLableToJsp(request, bundle, "msg_tm_search_no_changed");
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private static void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

    /**
     * Save entry
     * 
     * @param request
     * @throws Exception
     */
    public static String saveEntries(HttpServletRequest request, String userId)
            throws Exception
    {
        // get the needed TUV info
        long tmId = Long.parseLong((String) request.getParameter("tmId"));
        long tuId = Long.parseLong((String) request.getParameter("tuId"));
        String sourceLocaleStr = (String) request.getParameter("sourceLocale");
        String targetLocaleStr = (String) request.getParameter("targetLocale");
        long sourceTuvId = Long.parseLong((String) request
                .getParameter("sourceTuvId"));
        long targetTuvId = Long.parseLong((String) request
                .getParameter("targetTuvId"));
        GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager()
                .getLocaleByString(sourceLocaleStr);
        GlobalSightLocale targetLocale = ServerProxy.getLocaleManager()
                .getLocaleByString(targetLocaleStr);
        String newSource = (String) request.getParameter("source");
        String newTarget = (String) request.getParameter("target");
        String newSid = (String) request.getParameter("sid");
        String sourceNoChanged = (String) request
                .getParameter("sourceNoChanged");
        String targetNoChanged = (String) request
                .getParameter("targetNoChanged");

        // Get TU, source TUV, target TUV
        ProjectHandler ph = ServerProxy.getProjectHandler();
        Tm tm = ph.getProjectTMById(tmId, false);
        SegmentTmTu tu = getTu(tm, tuId);
        SegmentTmTuv srcTuv = getTrgTuv(tu, sourceLocale, sourceTuvId);
        SegmentTmTuv trgTuv = getTrgTuv(tu, targetLocale, targetTuvId);

        // Use SegmentManager to check error
        String srcSegment = GxmlUtil.stripRootTag(srcTuv.getSegment());
        SegmentManager segmentManagerSource = new SegmentManager();
        segmentManagerSource.setInputSegment(srcSegment, "", "html");
        segmentManagerSource.getCompact();
        String sourceErrorCheck = segmentManagerSource.errorCheck(newSource,
                srcSegment, 0, "UTF8", 0, "UTF8");
        if (sourceErrorCheck != null)
        {
            sourceErrorCheck = sourceErrorCheck.replace(":",
                    " in source segment:");
            return sourceErrorCheck;
        }

        String trgSegment = GxmlUtil.stripRootTag(trgTuv.getSegment());
        SegmentManager segmentManagerTarget = new SegmentManager();
        segmentManagerTarget.setInputSegment(trgSegment, "", "html");
        segmentManagerTarget.getCompact();
        String targetErrorCheck = segmentManagerTarget.errorCheck(newTarget,
                trgSegment, 0, "UTF8", 0, "UTF8");
        if (targetErrorCheck != null)
        {
            targetErrorCheck = targetErrorCheck.replace(":",
                    " in target segment:");
            return targetErrorCheck;
        }

        String newSourceDiplomat = segmentManagerSource
                .getTargetDiplomat(newSource);
        String newTargetDiplomat = segmentManagerTarget
                .getTargetDiplomat(newTarget);

        boolean updateSrcTuv = "false".equals(sourceNoChanged);
        boolean updateTrgTuv = "false".equals(targetNoChanged);
        List<SegmentTmTuv> tuvList = new ArrayList<SegmentTmTuv>();
        if (updateSrcTuv)
        {
            long time = (new Date()).getTime();
            srcTuv.setModifyDate(new Timestamp(time));
            srcTuv.setModifyUser(userId);
            srcTuv.setSegment("<segment>" + newSourceDiplomat + "</segment>");
            tuvList.add(srcTuv);
        }
        if (updateTrgTuv)
        {
            long time = (new Date()).getTime();
            trgTuv.setModifyDate(new Timestamp(time));
            trgTuv.setModifyUser(userId);
            trgTuv.setSegment("<segment>" + newTargetDiplomat + "</segment>");
            tuvList.add(trgTuv);
        }

        if (tm.getTm3Id() != null)
        {
            // For TM3, TUV has no SID, the SID would be saved to TU
            tu.setSID(newSid);
        }
        else
        {
            trgTuv.setSid(newSid);
        }

        // Update TUV
        LingServerProxy.getTmCoreManager().updateSegmentTmTuvs(tm, tuvList);
        return "update";
    }

    /**
     * Edit Entries
     */
    public static void editEntries(HttpServletRequest request) throws Exception
    {
        // get the needed TUV info
        long tmId = Long.parseLong((String) request.getParameter("tmId"));
        long tuId = Long.parseLong((String) request.getParameter("tuId"));
        String sourceLocaleStr = (String) request.getParameter("sourceLocale");
        String targetLocaleStr = (String) request.getParameter("targetLocale");
        long sourceTuvId = Long.parseLong((String) request
                .getParameter("sourceTuvId"));
        long targetTuvId = Long.parseLong((String) request
                .getParameter("targetTuvId"));
        GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager()
                .getLocaleByString(sourceLocaleStr);
        GlobalSightLocale targetLocale = ServerProxy.getLocaleManager()
                .getLocaleByString(targetLocaleStr);
        request.setAttribute("tmId", tmId);
        request.setAttribute("tuId", tuId);
        request.setAttribute("sourceTuvId", sourceTuvId);
        request.setAttribute("targetTuvId", targetTuvId);

        ProjectHandler ph = ServerProxy.getProjectHandler();
        Tm tm = ph.getProjectTMById(tmId, false);
        SegmentTmTu tu = getTu(tm, tuId);
        SegmentTmTuv srcTuv = getTrgTuv(tu, sourceLocale, sourceTuvId);
        SegmentTmTuv trgTuv = getTrgTuv(tu, targetLocale, targetTuvId);

        request.setAttribute("sourceLocale", srcTuv.getLocale().toString());
        request.setAttribute("targetLocale", targetLocaleStr);

        String na = "N/A";
        Map<String, Object> entryInfo = new HashMap<String, Object>();
        // get all attributes
        List<String> tuAttributes = new ArrayList<String>();
        Collection<ProjectTmTuTProp> tuProps = tu.getProps();
        if (tuProps != null && tuProps.size() > 0)
        {
            Iterator<ProjectTmTuTProp> it = tu.getProps().iterator();
            while (it.hasNext())
            {
                ProjectTmTuTProp p = it.next();
                String propType = p.getPropType();
                propType = propType.substring(5);
                tuAttributes.add(propType + ":" + p.getPropValue());
            }
            entryInfo.put("tuAttributes", tuAttributes);
        }
        else
        {
            tuAttributes.add(na);
            entryInfo.put("tuAttributes", tuAttributes);
        }

        String sid = trgTuv.getSid();
        String createdBy = trgTuv.getCreationUser();
        String createdOn = UTC.valueOf(trgTuv.getCreationDate());
        String modifyUser = trgTuv.getModifyUser();
        String modifyDate = UTC.valueOf(trgTuv.getModifyDate());

        entryInfo.put("sid", sid);
        entryInfo.put("createdBy",
                createdBy == null ? na : UserUtil.getUserNameById(createdBy));
        entryInfo.put("createdOn", createdOn == null ? na : createdOn);
        entryInfo.put("modifiedBy",
                modifyUser == null ? na : UserUtil.getUserNameById(modifyUser));
        entryInfo.put("modifiedOn", modifyDate == null ? na : modifyDate);
        entryInfo.put("sourceLocale", srcTuv.getLocale().getDisplayName());
        entryInfo.put("targetLocale", targetLocale.getDisplayName());
        entryInfo.put("tmName", tm.getName());

        String srcSegment = GxmlUtil.stripRootTag(srcTuv.getSegment());
        SegmentManager segmentManagerSource = new SegmentManager();
        segmentManagerSource.setInputSegment(srcSegment, "", "html");
        segmentManagerSource.getCompact();
        String source = segmentManagerSource
                .makeCompactColoredPtags(srcSegment);
        source = source.replace("\"", "&quot;");
        source = source.replace("'", "&apos;");
        source = source.replace("\\", "\\\\");
        String ptagsSource = segmentManagerSource.getPtagString();
        entryInfo.put("source", source);
        entryInfo.put("ptagsSource", ptagsSource);

        String trgSegment = GxmlUtil.stripRootTag(trgTuv.getSegment());
        SegmentManager segmentManagerTarget = new SegmentManager();
        segmentManagerTarget.setInputSegment(trgSegment, "", "html");
        segmentManagerTarget.getCompact();
        String target = segmentManagerTarget
                .makeCompactColoredPtags(trgSegment);
        target = target.replace("\"", "&quot;");
        target = target.replace("'", "&apos;");
        target = target.replace("\\", "\\\\");
        String ptagsTarget = segmentManagerTarget.getPtagString();
        entryInfo.put("target", target);
        entryInfo.put("ptagsTarget", ptagsTarget);

        request.setAttribute("entryInfo", JsonUtil.toJson(entryInfo));
    }

    /**
     * Get TU by tmId, tuId
     * 
     * @param tmId
     * @param tuId
     * @return
     * @throws Exception
     */
    private static SegmentTmTu getTu(Tm tm, long tuId) throws Exception
    {
        SegmentTmTu tu = null;
        List<Long> tuIds = new ArrayList<Long>();
        tuIds.add(tuId);
        try
        {
            List<SegmentTmTu> tus = tm.getSegmentTmInfo().getSegmentsById(tm,
                    tuIds);
            tu = tus.get(0);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }

        return tu;
    }

    /**
     * Get target tuv by tu, targetLocale, tuvId
     * 
     * @param tu
     * @param targetLocaleStr
     * @param tuvId
     * @return
     * @throws Exception
     */
    private static SegmentTmTuv getTrgTuv(SegmentTmTu tu,
            GlobalSightLocale targetLocale, long tuvId) throws Exception
    {
        SegmentTmTuv trgTuv = null;
        Collection targetTuvs = tu.getTuvList(targetLocale);
        for (Iterator itTuvs = targetTuvs.iterator(); itTuvs.hasNext();)
        {
            SegmentTmTuv trgTuvTemp = (SegmentTmTuv) itTuvs.next();
            if (tuvId == trgTuvTemp.getId())
            {
                trgTuv = trgTuvTemp;
                break;
            }
        }
        return trgTuv;
    }
}
