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

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.SortUtil;

public class TMSearchAddEntryHandlerHelper
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
        setLableToJsp(request, bundle, "lb_tm");
        setLableToJsp(request, bundle, "lb_sid");
        setLableToJsp(request, bundle, "lb_target");

        setLableToJsp(request, bundle, "lb_source");
        setLableToJsp(request, bundle, "lb_source_locale");
        setLableToJsp(request, bundle, "lb_target_locale");
        setLableToJsp(request, bundle, "lb_tm_add_entry");
        setLableToJsp(request, bundle, "lb_save");
        setLableToJsp(request, bundle, "lb_close");

        setLableToJsp(request, bundle, "lb_tm_add_entry_description");
        setLableToJsp(request, bundle, "msg_tm_search_source");
        setLableToJsp(request, bundle, "msg_tm_search_target");
        setLableToJsp(request, bundle, "msg_tm_search_add_source_null");
        setLableToJsp(request, bundle, "msg_tm_search_add_target_null");
        setLableToJsp(request, bundle, "msg_tm_search_tm");
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
        long tmId = Long.parseLong((String) request.getParameter("tmId"));
        long sourceLocaleId = Long.parseLong((String) request
                .getParameter("sourceLocale"));
        long targetLocaleId = Long.parseLong((String) request
                .getParameter("targetLocale"));

        GlobalSightLocale sourceLocale = ServerProxy.getLocaleManager()
                .getLocaleById(sourceLocaleId);
        GlobalSightLocale targetLocale = ServerProxy.getLocaleManager()
                .getLocaleById(targetLocaleId);
        String source = (String) request.getParameter("source");
        String target = (String) request.getParameter("target");
        String sid = (String) request.getParameter("sid");
        if ("".equals(sid))
        {
            sid = null;
        }

        // Use SegmentTagManager to check source and target
        SegmentManager segmentManagerSource = new SegmentManager();
        segmentManagerSource.setInputSegment("", "", "html");
        segmentManagerSource.getCompact();
        String sourceErrorCheck = segmentManagerSource.errorCheck(source, "",
                0, "UTF8", 0, "UTF8");
        if (sourceErrorCheck != null)
        {
            sourceErrorCheck = sourceErrorCheck.replace(":",
                    " in source segment:");
            return sourceErrorCheck;
        }
        String sourceDiplomat = segmentManagerSource.getTargetDiplomat(source);

        SegmentManager segmentManagerTarget = new SegmentManager();
        segmentManagerTarget.setInputSegment("", "", "html");
        segmentManagerTarget.getCompact();
        String targetErrorCheck = segmentManagerTarget.errorCheck(target, "",
                0, "UTF8", 0, "UTF8");
        if (targetErrorCheck != null)
        {
            targetErrorCheck = targetErrorCheck.replace(":",
                    " in target segment:");
            return targetErrorCheck;
        }
        String targetDiplomat = segmentManagerTarget.getTargetDiplomat(target);

        // get TM
        ProjectHandler ph = ServerProxy.getProjectHandler();
        Tm tm = ph.getProjectTMById(tmId, false);
        // New a source TUV
        SegmentTmTuv srcTuv = new SegmentTmTuv();
        srcTuv.setSegment("<segment>" + sourceDiplomat + "</segment>");
        srcTuv.setLocale(sourceLocale);
        srcTuv.setCreationUser(userId);
        srcTuv.setModifyUser(userId);
        srcTuv.setSid(sid);
        srcTuv.setLastUsageDate(new Timestamp(System.currentTimeMillis()));

        // New a target TUV
        SegmentTmTuv trgTuv = new SegmentTmTuv();
        trgTuv.setSegment("<segment>" + targetDiplomat + "</segment>");
        trgTuv.setLocale(targetLocale);
        trgTuv.setCreationUser(userId);
        trgTuv.setModifyUser(userId);
        trgTuv.setSid(sid);
        trgTuv.setLastUsageDate(new Timestamp(System.currentTimeMillis()));

        // New a TU
        SegmentTmTu tu = new SegmentTmTu();
        tu.setTmId(tmId);
        tu.setTranslatable();
        tu.setType("text");
        tu.setSourceLocale(sourceLocale);
        tu.setFormat("plaintext");
        tu.setSourceTmName(tm.getName());
        if (tm.getTm3Id() != null)
        {
            tu.setSID(sid);
        }

        tu.addTuv(srcTuv);
        tu.addTuv(trgTuv);
        List<BaseTmTu> tus = new ArrayList<BaseTmTu>();
        tus.add(tu);

        Set<GlobalSightLocale> targetLocales = Collections
                .singleton(targetLocale);

        try
        {
            // Save the new TU
            tm.getSegmentTmInfo().saveToSegmentTm(tus, sourceLocale, tm,
                    targetLocales, TmCoreManager.SYNC_MERGE, false);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }

        return "save";
    }

    /**
     * Delete Entries
     */
    public static void addEntries(HttpServletRequest request, String userId)
            throws Exception
    {
        String sourceLocaleId = (String) request.getParameter("sourceLocaleId");
        String targetLocaleId = (String) request.getParameter("targetLocaleId");

        request.setAttribute("sourceLocaleId", sourceLocaleId);
        request.setAttribute("targetLocaleId", targetLocaleId);
        setLocales(request);
        TMSearchBroswerHandlerHelper.setTMs(request, userId);
    }

    /**
     * set locale
     * 
     * @param request
     * @throws LocaleManagerException
     * @throws RemoteException
     */
    public static void setLocales(HttpServletRequest request)
            throws LocaleManagerException, RemoteException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAvailableLocales();
        SortUtil.sort(sources,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        request.setAttribute(LocalePairConstants.LOCALES,
                JsonUtil.toJson(sources));
    }
}
