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
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;

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
        setLableToJsp(request, bundle, "lb_save");
        setLableToJsp(request, bundle, "lb_close");
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
        setLableToJsp(request, bundle, "msg_internal_moved_continue");
        setLableToJsp(request, bundle, "job_id");
        setLableToJsp(request, bundle, "job_name");
        setLableToJsp(request, bundle, "lb_last_usage_date");
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
        newSid = EditUtil.decodeXmlEntities(newSid);
        String sourceNoChanged = (String) request
                .getParameter("sourceNoChanged");
        String targetNoChanged = (String) request
                .getParameter("targetNoChanged");

        // Get TU, source TUV, target TUV
        ProjectHandler ph = ServerProxy.getProjectHandler();
        Tm tm = ph.getProjectTMById(tmId, false);
        SegmentTmTu tu = getTu(tm, tuId);
        SegmentTmTuv srcTuv = getTuvById(tu, sourceLocale, sourceTuvId);
        SegmentTmTuv trgTuv = getTuvById(tu, targetLocale, targetTuvId);

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
        HashMap<String, String> sourceInternals = segmentManagerSource.getPseudoData().getInternalTexts();
        HashMap<String, String> targetInternals = segmentManagerTarget.getPseudoData().getInternalTexts();
        
        if (sourceInternals != null)
        {
            List<String> addedKeys = new ArrayList<String>();
            for (String sourceKey : sourceInternals.keySet())
            {
                if (!targetInternals.containsKey(sourceKey))
                {
                    addedKeys.add(sourceKey);
                }
            }

            if (addedKeys.size() > 0)
            {
                for (String sourceKey : addedKeys)
                {
                    segmentManagerTarget.getPseudoData().addInternalTags(
                            sourceKey, sourceInternals.get(sourceKey));
                }
            }
        }
        
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
            tu.setSID(newSid);
            srcTuv.setSid(newSid);
            trgTuv.setSid(newSid);
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
        SegmentTmTuv srcTuv = getTuvById(tu, sourceLocale, sourceTuvId);
        SegmentTmTuv trgTuv = getTuvById(tu, targetLocale, targetTuvId);

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
                tuAttributes.add(fixString(EditUtil.encodeXmlEntities(propType + ":" + p.getPropValue())));
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
        
		entryInfo.put("sid", sid == null ? na : fixString(EditUtil.encodeXmlEntities(sid)));
        String createdByName = UserUtil.getUserNameById(createdBy);
        entryInfo.put("createdBy", createdBy == null ? 
                na : fixString(EditUtil.encodeXmlEntities(createdByName)));
        entryInfo.put("createdOn", createdOn == null ? na : createdOn);
        String modifyUserName = UserUtil.getUserNameById(modifyUser);
        entryInfo.put("modifiedBy", modifyUser == null ?
                na : fixString(EditUtil.encodeXmlEntities(modifyUserName)));
        entryInfo.put("modifiedOn", modifyDate == null ? na : modifyDate);
        entryInfo.put("sourceLocale", srcTuv.getLocale().getDisplayName());
        entryInfo.put("targetLocale", targetLocale.getDisplayName());
        entryInfo.put("tmName", fixString(EditUtil.encodeXmlEntities(tm.getName())));
		entryInfo.put("lastUsageDate", trgTuv.getLastUsageDate() == null ? na
				: UTC.valueOf(trgTuv.getLastUsageDate()));
		entryInfo.put(
				"jobId",
				trgTuv.getJobId() == -1 ? na
						: String.valueOf(trgTuv.getJobId()));
		entryInfo.put("jobName",
				trgTuv.getJobName() == null
						|| trgTuv.getJobName().equalsIgnoreCase("null") ? na
						: trgTuv.getJobName());
        
        String srcSegment = GxmlUtil.stripRootTag(srcTuv.getSegment());
        SegmentManager segmentManagerSource = new SegmentManager();
        segmentManagerSource.setInputSegment(srcSegment, "", "html");
        segmentManagerSource.getCompact();
        String source = segmentManagerSource
                .makeCompactColoredPtags(srcSegment);
        source = source.replace("\"", "&quot;");
        source = source.replace("'", "&apos;");
        source = source.replace("\\", "\\\\");
        source = source.replace("\r", "\\r");
        source = source.replace("\n", "\\n");
        source = source.replace("\t", "\\t");
        source = getSegmentWithBR(source);
        String ptagsSource = segmentManagerSource.getPtagString();
        SegmentUtil sutil = new SegmentUtil(null);
        List<String> sourceInternals = sutil.getInternalWords(srcSegment);
        
        if (sourceInternals != null && sourceInternals.size() > 0)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < sourceInternals.size(); i++)
            {
                String internal = sourceInternals.get(i);

                sb.append("[").append(internal).append("]");

                if ((i + 1) < sourceInternals.size())
                {
                    sb.append("_g_s_");
                }
            }
            
            entryInfo.put("sourceInternals", sb.toString());
        }
        else
        {
            entryInfo.put("sourceInternals", "");
        }
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
        target = target.replace("\r", "\\r");
        target = target.replace("\n", "\\n");
        target = target.replace("\t", "\\t");
        target = getSegmentWithBR(target);
        String ptagsTarget = segmentManagerTarget.getPtagString();
        entryInfo.put("target", target);
        entryInfo.put("ptagsTarget", ptagsTarget);
        List<String> targetInternals = sutil.getInternalWords(trgSegment);

        if (targetInternals != null && targetInternals.size() > 0)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < targetInternals.size(); i++)
            {
                String internal = targetInternals.get(i);

                sb.append("[").append(internal).append("]");

                if ((i + 1) < targetInternals.size())
                {
                    sb.append("_g_s_");
                }
            }
            
            entryInfo.put("targetInternals", sb.toString());
        }
        else
        {
            entryInfo.put("targetInternals", "");
        }

        request.setAttribute("entryInfo", JsonUtil.toJson(entryInfo));
        request.setAttribute("srcSegment", srcSegment);
        request.setAttribute("trgSegment", trgSegment);
    }
    
	private static String getSegmentWithBR(String segment)
	{
		char[] crArr = segment.toCharArray();
		StringBuffer bufer = new StringBuffer();
		char cr;
		for (int i = 0; i < crArr.length; i++)
		{
			cr = crArr[i];
			bufer.append(cr);
			if (cr == '\\')
			{
				if (i < crArr.length - 1 && i > 0)
				{
					if (crArr[i + 1] == 'n' && crArr[i - 1] != '\\')
					{
						bufer.append(crArr[i + 1]).append("<br>");
						i++;
					}
				}
			}
		}
		return bufer.toString();
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
    private static SegmentTmTuv getTuvById(SegmentTmTu tu,
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
    
    private static String fixString(String str)
    {
        str = str.replace("\\", "\\\\");
        return str;
    }
}
