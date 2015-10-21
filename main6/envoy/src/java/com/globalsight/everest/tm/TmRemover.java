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

package com.globalsight.everest.tm;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentResultSet;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProcessMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * TmRemover class is responsible for deleting TM.
 */
public class TmRemover extends MultiCompanySupportedThread implements
        ProcessMonitor, ProgressReporter, Serializable
{
    private static final long serialVersionUID = -5050592130567158673L;

    static private final Logger c_logger = Logger.getLogger(TmRemover.class);

    // private long m_tmId;

    private ArrayList<String> tmIds = null;

    private int m_percentage = 0;

    private boolean m_done = false;

    private boolean m_error = false;

    private String m_message = "";

    private InterruptMonitor m_monitor = new InterruptMonitor();

    private boolean deleteLanguageFlag = false;
    
    private boolean deleteTUListingFlag = false;
    
    private File tmxFile = null;

    private long localeID;
    
    String m_userId;

    /**
     * TmRemover constructor.
     * @param m_userId 
     */
    public TmRemover(ArrayList<String> tmIds, String m_userId)
    {
        super();
        this.tmIds = tmIds;
        this.m_userId = m_userId;
    }

    public void run()
    {
        FileInputStream fileInputStream = null;
        try
        {
            super.run();

            setPercentage(0);
            ArrayList<String> dependencyTmpProfileNames = new ArrayList<String>();
            ArrayList<String> nonDependencyTms = new ArrayList<String>(tmIds);

            getDependentTmProfileNames(dependencyTmpProfileNames,
                    nonDependencyTms);

            if (dependencyTmpProfileNames.size() > 0)
                setDependencyError(dependencyTmpProfileNames);
            int size = nonDependencyTms.size();
            long tmId = -1l;
            if (size > 0)
            {
                int leftRound = 80;
                List<Long> tuIds = new ArrayList<Long>();
                if (deleteTUListingFlag)
                {
                    leftRound = 70;
                    fileInputStream = new FileInputStream(tmxFile);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(fileInputStream, "UTF-8"));

                    String line = reader.readLine();

                    while (line != null)
                    {
                        line = line.trim();
                        if (line.startsWith("<tu "))
                        {
                            StringIndex si = StringIndex.getValueBetween(line,
                                    0, "tuid=\"", "\"");

                            if (si != null)
                            {
                                Long tuId = Long.parseLong(si.value);

                                if (!tuIds.contains(tuId))
                                {
                                    tuIds.add(tuId);
                                }
                            }
                        }

                        line = reader.readLine();
                    }

                    setPercentage(10);
                }
                
                int round = Math.round(leftRound / size);
                int index = 1;
                Tm tm = null;
                GlobalSightLocale locale = null;
                TmCoreManager manager = LingServerProxy.getTmCoreManager();
                ProjectTMTBUsers ptUsers = new ProjectTMTBUsers();

                for (String tmpTmId : nonDependencyTms)
                {
                    tmId = Long.parseLong(tmpTmId);

                    tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                            true);

                    int convertedRate = tm.getConvertRate();
                    if (convertedRate > 0 && convertedRate < 100)
                    {
                        long tm2Id = tm.getConvertedTM3Id();
                        ProjectTM oriTm = ServerProxy.getProjectHandler()
                                .getProjectTMById(tm2Id, true);
                        if (oriTm != null)
                        {
                            oriTm.setConvertedTM3Id(-1);
                            oriTm.setLastTUId(-1);
                            HibernateUtil.save(oriTm);
                        }
                    }

                    if (deleteTUListingFlag)
                    {
                        String tmName = tm.getName();
                        // tm3
                        if (tm.getTm3Id() != null)
                        {
                            List<Long> tm3tuIds = new ArrayList<Long>();
                            TM3Tm<GSTuvData> tm3tm = (new Tm3SegmentTmInfo())
                                    .getTM3Tm(tm.getTm3Id());

                            List<TM3Tu<GSTuvData>> tus = tm3tm.getTu(tuIds);
                            Iterator<TM3Tu<GSTuvData>> tuIt = tus.iterator();

                            List<SegmentTmTu> resultList = new ArrayList<SegmentTmTu>();
                            while (tuIt.hasNext())
                            {
                                TM3Tu<GSTuvData> tm3tu = tuIt.next();

                                if (tm3tu.getTm().getId().equals(tm.getTm3Id()))
                                {
                                    TM3Attribute typeAttr = TM3Util
                                            .getAttr(tm3tm, TYPE);
                                    TM3Attribute formatAttr = TM3Util
                                            .getAttr(tm3tm, FORMAT);
                                    TM3Attribute sidAttr = TM3Util
                                            .getAttr(tm3tm, SID);
                                    TM3Attribute translatableAttr = TM3Util
                                            .getAttr(tm3tm, TRANSLATABLE);
                                    TM3Attribute fromWsAttr = TM3Util
                                            .getAttr(tm3tm, FROM_WORLDSERVER);
                                    TM3Attribute projectAttr = TM3Util
                                            .getAttr(tm3tm, UPDATED_BY_PROJECT);

                                    SegmentTmTu segmentTmTu = TM3Util
                                            .toSegmentTmTu(tm3tu, tm.getId(),
                                                    formatAttr, typeAttr,
                                                    sidAttr, fromWsAttr,
                                                    translatableAttr,
                                                    projectAttr);
                                    resultList.add(segmentTmTu);
                                    tm3tuIds.add(tm3tu.getId());
                                }
                            }

                            if (resultList.size() > 0)
                            {
                                manager.deleteSegmentTmTus(tm, resultList, false);
                                this.setMessageKey("", tmName + " - TUs ("
                                        + tm3tuIds
                                        + ") have been successfully removed.");
                            }
                            else
                            {
                                this.setMessageKey("", tmName
                                        + " - Nothing has been removed.");
                            }
                        }
                        // tm2 : do not testing as TM2 is hidden from 8.6.5
                        else
                        {
                            List<SegmentTmTu> tus = tm.getSegmentTmInfo()
                                    .getSegmentsById(tm, tuIds);

                            manager.deleteSegmentTmTus(tm, tus);
                            this.setMessageKey("", tmName + " - TUs (" + tuIds
                                    + ") has been successfully removed.");
                        }

                        OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                                OperationLog.COMPONET_TM, tm.getName());
                    }
                    else if (deleteLanguageFlag)
                    {
                        locale = ServerProxy.getLocaleManager().getLocaleById(
                                localeID);
                        manager.removeTmData(tm, locale, this, m_monitor);
                        OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                                OperationLog.COMPONET_TM, tm.getName());
                    }
                    else
                    {
                        manager.removeTmData(tm, this, m_monitor);
                        OperationLog.log(m_userId, OperationLog.EVENT_DELETE,
                                OperationLog.COMPONET_TM, tm.getName());
                        ptUsers.deleteAllUsers(String.valueOf(tmId), "TM");
                    }

                    setPercentage(index * round);
                }
            }
            setPercentage(100);
        }
        catch (Throwable ex)
        {
            if (ex instanceof InterruptException)
            {
                c_logger.info(m_message);
            }
            else
            {
                c_logger.error("An error occured while removing Tm", ex);

                synchronized (this)
                {
                    m_message = "&lt;font color='red'&gt;"
                            + getStringFromBundle("lb_tm_remove_error_occur",
                                    "An error occured while removing Tm")
                            + ": " + ex.getMessage() + "&lt;/font&gt;";
                    m_done = true;
                    m_error = true;
                }
            }
        }
        finally
        {
            m_done = true;

            try
            {
                fileInputStream.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    // ProcessMonitor methods

    /** Method for getting counter value. */
    public int getCounter()
    {
        return 0;
    }

    /** Method for getting percentage complete information. */
    public int getPercentage()
    {
        synchronized (this)
        {
            return m_percentage;
        }
    }

    /**
     * Method for getting a status if the process has finished (either
     * successfully, with error or canceled by user request)
     */
    public boolean hasFinished()
    {
        synchronized (this)
        {
            return m_done;
        }
    }

    /**
     * This must be called after setResourceBundle and before start to avoid a
     * window where getReplacingMessage returns null.
     */
    public void initReplacingMessage()
    {
        setMessageKey("", "");
    }

    /**
     * Method for getting a message that replaces an existing message in UI.
     * This is typically used to get a message that shows the current status
     */
    public String getReplacingMessage()
    {
        synchronized (this)
        {
            return m_message;
        }
    }

    /**
     * Returns true if an error has occured and the replacing message contains
     * error message.
     */
    public boolean isError()
    {
        synchronized (this)
        {
            // return m_error;
            return false;
        }
    }

    /**
     * Method for getting messages that are appended in UI. This is typically
     * used to get messages that cumulatively displayed e.g. items so far done.
     */
    public List getAppendingMessages()
    {
        return null;
    }

    /**
     * Method for setting flag to determine delete TM or TM's language
     */
    public void SetDeleteLanguageFlag(boolean flag)
    {
        deleteLanguageFlag = flag;
    }
    
    public void SetDeleteTUListingFlag(boolean flag)
    {
        deleteTUListingFlag = flag;
    }

    public void setTmxFile(File tmxFile)
    {
        this.tmxFile = tmxFile;
    }
    
    synchronized public void cancelProcess()
    {
        m_monitor.interrupt();
    }

    //
    // ProgressReporter
    //

    /**
     * Set the current progress message key. It is generally assumed that this
     * is a key into a localizable message bundle.
     * 
     * @param statusMessage
     *            progress message key
     */
    @Override
    public void setMessageKey(String messageKey, String defaultMessage)
    {
        synchronized (this)
        {
            String tmp = getStringFromBundle(messageKey, defaultMessage);
            if (!StringUtil.isEmpty(tmp))
                m_message += tmp + "&lt;br&gt;";
        }
    }

    /**
     * Set the current completion percentage.
     * 
     * @param percentage
     *            completion percentage
     */
    @Override
    public void setPercentage(int percentage)
    {
        synchronized (this)
        {
            m_percentage = percentage;
            if (m_percentage == 100)
                m_done = true;
        }
    }

    private void setDependencyError(ArrayList<String> p_tmProfileNames)
    {
        synchronized (this)
        {
            String tmpMsg = "Tm is refered to by the following Tm Profiles. "
                    + "Please deselect this Tm from the Tm Profiles before removing it.";
            m_message = "&lt;font color='red'&gt;"
                    + getStringFromBundle("lb_tm_remove_tm_deselect_tmp",
                            tmpMsg) + "&lt;br&gt;";

            for (Iterator<String> it = p_tmProfileNames.iterator(); it
                    .hasNext();)
                m_message += "&lt;b&gt;" + it.next() + "&lt;/b&gt;&lt;br&gt;";

            m_message += "&lt;/font&gt;";

            m_error = true;
        }
    }

    private void checkInterrupt() throws InterruptException
    {
        m_message = getStringFromBundle("lb_tm_remove_cancel_by_user",
                "Tm removal has been cancelled by user request.");
        m_done = true;

        throw new InterruptException();
    }

    private void getDependentTmProfileNames(
            ArrayList<String> dependencyTmpProfileNames,
            ArrayList<String> nonDependencyTms) throws Exception
    {
        Collection tmProfiles = ServerProxy.getProjectHandler()
                .getAllTMProfiles();

        String tmp = null;
        long tmpTmId = -1l;
        for (Iterator itTmProfiles = tmProfiles.iterator(); itTmProfiles.hasNext();)
        {
            TranslationMemoryProfile profile =
                    (TranslationMemoryProfile) itTmProfiles.next();
            tmpTmId = profile.getProjectTmIdForSave();
            // check with the Tm to save
            if (tmIds.contains(String.valueOf(tmpTmId)))
            {
                dependencyTmpProfileNames.add(profile.getName());
                nonDependencyTms.remove(String.valueOf(tmpTmId));
                continue;
            }

            // check with the Tm to leverage from
            Vector<LeverageProjectTM> tms = profile
                    .getProjectTMsToLeverageFrom();
            for (LeverageProjectTM lptm : tms)
            {
                tmp = String.valueOf(lptm.getProjectTmId());
                if (tmIds.contains(tmp))
                {
                    dependencyTmpProfileNames.add(profile.getName());
                    nonDependencyTms.remove(tmp);
                    break;
                }
            }
        }
    }

    private class InterruptException extends Exception
    {
    }

    public void setLocaleId(long cl)
    {
        localeID = cl;
    }
}
