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
package com.globalsight.everest.webapp.pagehandler.administration.mtprofile;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.EngineEnum;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class MTProfileHandlerHelper
{
    private static final Logger logger = Logger
            .getLogger(MTProfileHandlerHelper.class.getName());

    public static final String ALL_MT_PROFILES_SQL = new StringBuffer()
            .append("select * from mt_profile mtp ")
            .append("where mtp.COMPANY_ID >= :")
            .append(CompanyWrapper.COPMANY_ID_START_ARG)
            .append(" and COMPANY_ID <= :")
            .append(CompanyWrapper.COPMANY_ID_END_ARG).toString();

    public static void savemtProfile(MachineTranslationProfile mtProfile)
    {
        try
        {
            HibernateUtil.saveOrUpdate(mtProfile);
        }
        catch (Exception e)
        {
            logger.error("Fail to save machine translation profile.", e);
        }
    }

    public static MachineTranslationProfile getMtProfileBySourcePage(
            SourcePage p_sourcePage, GlobalSightLocale p_targetLocale)
    {
        Request request = p_sourcePage.getRequest();
        L10nProfile l10nProfile = request.getL10nProfile();
        WorkflowTemplateInfo workflowTemplateInfo = l10nProfile
                .getWorkflowTemplateInfo(p_targetLocale);
        if (workflowTemplateInfo.getTargetLocale().getId() != p_targetLocale
                .getId())
        {
            return null;
        }
        return getMTProfileByRelation(l10nProfile.getId(),
                workflowTemplateInfo.getId());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Collection<MachineTranslationProfile> getAllMTProfiles(
            String condition) throws RemoteException, ProjectHandlerException
    {
        Collection<MachineTranslationProfile> mtProfiles = null;

        try
        {
            String sql = ALL_MT_PROFILES_SQL + condition;
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            mtProfiles = HibernateUtil.searchWithSql(sql, map,
                    MachineTranslationProfile.class);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_MT_PROFILES,
                    null, e);
        }

        return mtProfiles;
    }

    public static MachineTranslationProfile getMTProfileById(String mtId)
    {
        return HibernateUtil.get(MachineTranslationProfile.class,
                Long.valueOf(mtId));
    }

    public static void removeMTProfile(MachineTranslationProfile mtProfile)
    {
        try
        {
            String sql = "UPDATE l10n_profile_wftemplate_info lwi SET lwi.MT_PROFILE_ID = -1 WHERE  lwi.MT_PROFILE_ID="
                    + mtProfile.getId();
            HibernateUtil.delete(mtProfile);
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            logger.error("Fail to remove machine translation profile.", e);
        }
    }

    public static void activeMTProfile(MachineTranslationProfile mtProfile)
    {
        mtProfile.setActive(!mtProfile.isActive());
        HibernateUtil.saveOrUpdate(mtProfile);
    }

    public static boolean isMtProfileExisted(MachineTranslationProfile mtProfile)
    {
        String condition = " AND mtp.MT_PROFILE_NAME ='"
                + mtProfile.getMtProfileName() + "' AND mtp.ID !="
                + mtProfile.getId();
        Collection<MachineTranslationProfile> mtProfiles;
        try
        {
            mtProfiles = getAllMTProfiles(condition);
            if (mtProfiles == null || mtProfiles.size() == 0)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }

        return true;
    }

    public static List<MachineTranslationProfile> getMTProfilesByEngine(
            List<EngineEnum> engines)
    {
        List<MachineTranslationProfile> mtProfiles = new ArrayList<MachineTranslationProfile>();
        try
        {
            int i = 0;
            StringBuffer condition = new StringBuffer(" AND (");
            for (EngineEnum engine : engines)
            {
                if (i > 0)
                {
                    condition.append(" OR ");
                }
                condition.append("mtp.MT_ENGINE='").append(engine).append("'");
                i++;
            }
            condition.append(")");

            mtProfiles = (List<MachineTranslationProfile>) getAllMTProfiles(condition
                    .toString());
        }
        catch (Exception e)
        {
            logger.error("Error when get machine translation profiles.", e);
        }

        return mtProfiles;
    }

    /**
     * When specify MT profile for workflow on L10nProfile edit/new UI, below MT
     * engines support all locale pair in theory, no need to filter first.
     * 
     * @return
     */
    public static List<MachineTranslationProfile> getSupportMTProfiles()
    {
        List<EngineEnum> mtEnginesSupportAll = new ArrayList<EngineEnum>();
        mtEnginesSupportAll.add(EngineEnum.MS_Translator);
        mtEnginesSupportAll.add(EngineEnum.Safaba);
        mtEnginesSupportAll.add(EngineEnum.DoMT);
        mtEnginesSupportAll.add(EngineEnum.Google_Translate);
        return getMTProfilesByEngine(mtEnginesSupportAll);
    }

    /**
     * When specify MT profile for workflow on L10nProfile new/edit UI, some MT
     * engines may not support all locale pairs, need filter in advance.
     * 
     * @param otherProfiles
     * @return
     */
    public static List<MachineTranslationProfile> getNotSureMTProfiles(
            List otherProfiles)
    {
        List<MachineTranslationProfile> mtProfiles = new ArrayList<MachineTranslationProfile>();
        try
        {
            mtProfiles = (List<MachineTranslationProfile>) getAllMTProfiles("");
            mtProfiles.removeAll(otherProfiles);
        }
        catch (Exception e)
        {
            logger.error("Error when get machine translation profiles.", e);
        }
        return mtProfiles;
    }

    public static long getMTProfileIdByRelation(long lpId, long wfId)
    {
        String sql="SELECT lpwi.MT_PROFILE_ID FROM l10n_profile_wftemplate_info lpwi "
                + " WHERE lpwi.L10N_PROFILE_ID=" + lpId
                + " AND lpwi.WF_TEMPLATE_ID=" + wfId;
        Session session = HibernateUtil.getSession();
        SQLQuery query=session.createSQLQuery(sql);
        if (query.list() == null || query.list().size() == 0)
        {
            return -1;
        }

        long mtId = ((BigInteger) session.createSQLQuery(sql).list().get(0))
                .longValue();
        return mtId;
    }

    public static MachineTranslationProfile getMTProfileByRelation(long lpId,
            long wfId)
    {
        long id = getMTProfileIdByRelation(lpId, wfId);
        if (id == -1)
        {
            return null;
        }
        else
        {
            return HibernateUtil.get(MachineTranslationProfile.class, id);
        }
    }

    public static MachineTranslationProfile getMtProfileBySourcePageId(
            long sourcePageId, GlobalSightLocale targetLocale)
    {
        MachineTranslationProfile machineTranslationProfile = null;
        try
        {
            SourcePage sourcePage = ServerProxy.getPageManager().getSourcePage(
                    sourcePageId);
            machineTranslationProfile = getMtProfileBySourcePage(sourcePage,
                    targetLocale);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return machineTranslationProfile;
    }

    public static String isAble2Delete(long id)
    {

        Session session = HibernateUtil.getSession();
        String sql = "SELECT lp.NAME FROM  l10n_profile lp "
                + " WHERE lp.ID IN(SELECT lpwi.L10N_PROFILE_ID FROM l10n_profile_wftemplate_info lpwi WHERE lpwi.MT_PROFILE_ID=" + id + " ) "
                + " AND lp.IS_ACTIVE='y' ";
        SQLQuery query = session.createSQLQuery(sql);
        if (query.list() == null || query.list().size() == 0)
        {
            return null;
        }
        else
        {
            StringBuffer lpnames = new StringBuffer();
            List<String> names = query.list();
            for(String name : names){
                lpnames.append("," + name);
            }
            return lpnames.toString();
        }


    }

}
