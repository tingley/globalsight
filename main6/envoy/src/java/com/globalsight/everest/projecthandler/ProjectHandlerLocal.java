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
package com.globalsight.everest.projecthandler;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.BasicL10nProfileInfo;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.L10nProfileFactory;
import com.globalsight.everest.foundation.L10nProfileVersion;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfoKey;
import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileDescriptorModifier;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileForGUIQueryResultHandler;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileQueryResultHandler;
import com.globalsight.everest.persistence.l10nprofile.WorkflowTemplateInfoDescriptorModifier;
import com.globalsight.everest.persistence.project.ProjectDescriptorModifier;
import com.globalsight.everest.persistence.project.ProjectQueryResultHandler;
import com.globalsight.everest.persistence.project.ProjectUnnamedQueries;
import com.globalsight.everest.projecthandler.exporter.ExportManager;
import com.globalsight.everest.projecthandler.importer.ImportManager;
import com.globalsight.everest.request.WorkflowRequest;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.SetDefaultRoleUtil;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.WorkflowInfos;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;
import com.globalsight.exporter.ExporterException;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ArrayConverter;
import com.globalsight.util.CacheData;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;
import com.globalsight.util.system.LogManager;
import com.globalsight.util.system.LogType;

/**
 * This class is responsible for managing projects and localization profiles
 * which are assigned to projects.
 */
public class ProjectHandlerLocal implements ProjectHandler
{
    private static final Logger c_category = Logger
            .getLogger(ProjectHandlerLocal.class);

    public static final String ALL_TM_PROFILES_SQL = " select * from tm_profile "
            + " where company_id >= :"
            + CompanyWrapper.COPMANY_ID_START_ARG
            + " and company_id <= :" + CompanyWrapper.COPMANY_ID_END_ARG;

    private ResourceBundle p_resourceBundle = null;

    public ProjectHandlerLocal() throws ProjectHandlerException
    {
        super();
    }

    // /////////////////////////////////////////////////////////////////////////
    // BEGIN: Localization Profiles /////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Add a localization profile to the system.
     * <p>
     * 
     * @param p_l10nProfile
     *            The localization profile to add.
     * @return long Return the unique id of the newly added oprofile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to add the profile; the cause is indicated by the
     *                exception code.
     */
    public long addL10nProfile(L10nProfile p_l10nProfile)
            throws RemoteException, ProjectHandlerException
    {
        BasicL10nProfile basicProfile = null;
        org.hibernate.Session session = null;
        Transaction transaction = null;

        if (!(p_l10nProfile instanceof BasicL10nProfile))
        {
            basicProfile = L10nProfileFactory
                    .makeBasicL10nProfile(p_l10nProfile);
        }
        else
        {
            basicProfile = (BasicL10nProfile) p_l10nProfile;
        }

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            basicProfile
                    .setTimestamp(new Timestamp(System.currentTimeMillis()));
            session.save(basicProfile);

            Set<WorkflowTemplateInfo> workflowTemplateInfos = basicProfile
                    .getWorkflowTemplates();
            for (Iterator it = workflowTemplateInfos.iterator(); it.hasNext();)
            {
                WorkflowTemplateInfo wfInfo = (WorkflowTemplateInfo) it.next();
                L10nProfileWFTemplateInfo lnWfInfo = new L10nProfileWFTemplateInfo();
                L10nProfileWFTemplateInfoKey key = new L10nProfileWFTemplateInfoKey();
                key.setL10nProfileId(basicProfile.getId());
                key.setWfTemplateId(wfInfo.getId());
                key.setMtProfileId(wfInfo.getMtProfileId());
                lnWfInfo.setKey(key);
                lnWfInfo.setIsActive(true);
                saveL10nProfileWfTemplateInfo(session, lnWfInfo);
            }

            transaction.commit();
            
            return basicProfile.getId();
        }
        catch (Exception pe)
        {
            c_category.error(p_l10nProfile.toString() + " " + pe.getMessage(),
                    pe);
            String[] args = new String[1];
            args[0] = p_l10nProfile.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ADD_PROFILE, args, pe);
        }
    }

    /**
     * Duplicate the specified localization profile.
     * <p>
     * 
     * @param p_profileId
     *            The primary ID of the localization profile.
     * @return The duplicate copy of the specified localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public void duplicateL10nProfile(long p_profileId, String p_newName,
            Collection p_localePairs, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException
    {
        org.hibernate.Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            BasicL10nProfile profileToBeDuplicated = (BasicL10nProfile) getL10nProfile(p_profileId);
            Collection workflowTemplateInfos = profileToBeDuplicated
                    .getWorkflowTemplateInfos();

            if (workflowTemplateInfos.size() > 1)
            {
                throw new ProjectHandlerException(
                        ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_PROFILE);
            }

            Iterator ite = workflowTemplateInfos.iterator();
            WorkflowTemplateInfo workflowTemplateInfo = null;

            if (ite.hasNext())
            {
                workflowTemplateInfo = (WorkflowTemplateInfo) ite.next();
            }

            WorkflowTemplate iflowTemplate = ServerProxy.getWorkflowServer()
                    .getWorkflowTemplateById(
                            workflowTemplateInfo.getWorkflowTemplateId());
            Iterator it = p_localePairs.iterator();

            while (it.hasNext())
            {
                LocalePair localePair = (LocalePair) it.next();
                BasicL10nProfile duplicatedProfile = L10nProfileFactory
                        .makeDuplicateL10nProfile(profileToBeDuplicated,
                                localePair.getSource());

                String autoName = generateAutoName(p_newName, localePair);
                c_category.info("The value of name is " + autoName);
                duplicatedProfile.setName(autoName);
                duplicatedProfile.setSourceLocale(localePair.getSource());
                WorkflowTemplateInfo wftInfo = duplicateWorkflowTemplate(
                        autoName, workflowTemplateInfo.getId(), localePair,
                        iflowTemplate, p_displayRoleName);

                Vector v = new Vector();
                v.add(wftInfo);
                duplicatedProfile.setWorkflowTemplateInfos(v);
                Set tmProfiles = new HashSet();
                tmProfiles.addAll(profileToBeDuplicated.getTmProfiles());
                duplicatedProfile.setTmProfiles(tmProfiles);

                session.save(duplicatedProfile);

                L10nProfileWFTemplateInfo lnWfInfo = new L10nProfileWFTemplateInfo();

                L10nProfileWFTemplateInfoKey key = new L10nProfileWFTemplateInfoKey();

                key.setL10nProfileId(duplicatedProfile.getId());
                key.setWfTemplateId(wftInfo.getId());
                lnWfInfo.setKey(key);
                lnWfInfo.setIsActive(wftInfo.isActive());
                saveL10nProfileWfTemplateInfo(session, lnWfInfo);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args = new String[1];
            args[0] = Long.toString(p_profileId);
            c_category.error(e.getMessage() + " " + Long.toString(p_profileId),
                    e);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_PROFILE,
                    args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Modify an existing localization profile in the system.
     * <p>
     * The original localization profile is left as is. Instead we create a new
     * localization profile which has a new profile sequence number (PK). This
     * will allow for executing references to original profile intact an
     * operational.
     * <p>
     * An entry in profile version table is made which contains a pair of
     * original and modified profile sequence number. This allows for building a
     * version tree in future.
     * <p>
     * 
     * @param p_l10nProfile
     *            The modified localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to modify the profile; the cause is indicated by
     *                the exception code.
     */
    public void modifyL10nProfile(L10nProfile p_l10nProfile,
            Vector<WorkflowInfos> workflowInfos, long originalLocId)
            throws RemoteException, ProjectHandlerException
    {
        // Clone the modified profile so that it can be inserted to
        // system. The modified profile is created with a new profile id.
        BasicL10nProfile modifiedProfile = null;
        // if this is a modify or copy/duplication
        BasicL10nProfile originalProfile = null;
        // "false" = copy/duplication

        org.hibernate.Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            modifiedProfile = (BasicL10nProfile) p_l10nProfile;

            if (c_category.isDebugEnabled())
            {
                c_category.debug("The value of tm choice in p_l10nProfile is "
                        + p_l10nProfile.getTmChoice());
                c_category.debug("The modified l10n profile is "
                        + modifiedProfile.toDebugString());
                c_category.debug("The value of tm choice is "
                        + modifiedProfile.getTmChoice());
            }

            modifiedProfile.setId(-1L);
            session.saveOrUpdate(modifiedProfile);

            // Now create a version node in profile version table
            if (c_category.isDebugEnabled())
            {
                c_category.debug("ProfileID Before = " + originalLocId
                        + " Modified == " + modifiedProfile.getId());
            }

            L10nProfileVersion versionInfo = new L10nProfileVersion(
                    originalLocId, modifiedProfile.getId());
            session.save(versionInfo);

            // delete the old version of it (logically deleted)
            // this is a modification so get the original profile.
            originalProfile = (BasicL10nProfile) session.get(
                    BasicL10nProfile.class, new Long(originalLocId));

            if (originalProfile != null)
            {
                originalProfile.deactivate();
                session.update(originalProfile);
            }

            // Update those objects dependant on a modified l10nProfile.
            String hql = "from FileProfileImpl f where f.isActive = 'Y' and "
                    + "f.l10nProfileId = :lId";
            Map<String, Long> map = new HashMap<String, Long>();
            map.put("lId", new Long(originalLocId));
            List fProfiles = HibernateUtil.search(hql, map);

            for (int i = 0; i < fProfiles.size(); i++)
            {
                FileProfileImpl fProfile = (FileProfileImpl) fProfiles.get(i);
                fProfile.setL10nProfileId(modifiedProfile.getId());
                // Need update the "L10N_PROFILE_ID" for reference file profile
                // of XLZ (in fact it is XLF file profile).
                if (fProfile.getReferenceFP() > 0)
                {
                    long refFPId = fProfile.getReferenceFP();
                    FileProfileImpl refXlzFP = HibernateUtil.get(
                            FileProfileImpl.class, refFPId, false);
                    refXlzFP.setL10nProfileId(modifiedProfile.getId());
                }
            }

            Iterator<?> iterator = fProfiles.iterator();
            while (iterator.hasNext())
            {
                session.update(iterator.next());
            }
            // HibernateUtil.update(fProfiles);

            hql = "from DatabaseProfileImpl d where d.l10nProfileId = :lId";
            List dProfiles = HibernateUtil.search(hql, map);

            for (int i = 0; i < dProfiles.size(); i++)
            {
                DatabaseProfileImpl dProfile = (DatabaseProfileImpl) dProfiles
                        .get(i);
                dProfile.setL10nProfileId(modifiedProfile.getId());
            }

            iterator = dProfiles.iterator();
            while (iterator.hasNext())
            {
                session.update(iterator.next());
            }

            // HibernateUtil.update(dProfiles);
            ProjectHandler ph = ServerProxy.getProjectHandler();

            for (int i = 0; i < workflowInfos.size(); i++)
            {
                L10nProfileWFTemplateInfo l10nProfileWFTemplateInfo = new L10nProfileWFTemplateInfo();
                L10nProfileWFTemplateInfoKey l10nProfileWFTemplateInfoKey = new L10nProfileWFTemplateInfoKey();
                l10nProfileWFTemplateInfoKey.setL10nProfileId(modifiedProfile
                        .getId());
                l10nProfileWFTemplateInfoKey.setWfTemplateId(workflowInfos.get(
                        i).getWfId());
                l10nProfileWFTemplateInfoKey.setMtProfileId(workflowInfos
                        .get(i).getMtProfileId());
                l10nProfileWFTemplateInfo.setKey(l10nProfileWFTemplateInfoKey);
                l10nProfileWFTemplateInfo.setIsActive(workflowInfos.get(i)
                        .isActive());

                if (ph.isPrimaryKeyExist(modifiedProfile.getId(), workflowInfos
                        .get(i).getWfId()))
                {
                    updateL10nProfileWfTemplateInfo(session,
                            l10nProfileWFTemplateInfo);
                }
                else
                {
                    saveL10nProfileWfTemplateInfo(session,
                            l10nProfileWFTemplateInfo);
                }
            }

            transaction.commit();
        }
        catch (Exception pe)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            c_category.error(p_l10nProfile.toString() + " " + pe.getMessage(),
                    pe);
            String[] args = new String[1];
            args[0] = Long.toString(originalLocId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_MODIFY_PROFILE, args,
                    pe);
        }
    }

    /**
     * Remove the localization profile from the system. This method assumes that
     * all dependencies have been checked and already taken care of by calling
     * the L10nProfileDependencyChecker.
     * <p>
     * 
     * @param p_l10nProfile
     *            The localization profile to remove.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to remove the profile; the cause is indicated by
     *                the exception code.
     */
    public void removeL10nProfile(L10nProfile p_l10nProfile)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            HibernateUtil.delete(p_l10nProfile);
        }
        catch (Exception pe)
        {
            c_category.error("Couldn't remove the L10nProfile", pe);
            String args[] =
            { Long.toString(p_l10nProfile.getId()) };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_PROFILE, args,
                    pe);
        }
    }

    /**
     * Returns all the localization profiles in the system.
     * <p>
     * 
     * @return Return all the localization profiles as a vector.
     * @exception RemoteException
     *                System or network related exception.
     * @exception GeneralException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public Collection getAllL10nProfiles() throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);

            return HibernateUtil.searchWithSql(
                    L10nProfileDescriptorModifier.CURRENT_PROFILES_SQL, map,
                    BasicL10nProfile.class);
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILES,
                    null, pe);
        }
    }
    
	@Override
	public Collection getL10ProfilesByProjectId(long projectId)
			throws RemoteException, ProjectHandlerException
	{
		try
		{
			String hql = "FROM BasicL10nProfile L1 where L1.isActive = 'Y' and L1.project.id="
					+ projectId + " order by L1.name";
			return HibernateUtil.search(hql);
		}
		catch (Exception pe)
		{
			throw new ProjectHandlerException(
					ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILES,
					null, pe);
		}
	}

    /**
     * Get both active and inactive L10nProfiles for current company.
     */
    public Collection getAllL10nProfilesData() throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            String hql = "FROM BasicL10nProfile L1 where L1.companyId="
                    + CompanyWrapper.getCurrentCompanyId()
                    + " order by L1.name";
            return HibernateUtil.search(hql);
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILES,
                    null, pe);
        }
    }

    /**
     * Returns all the localization profiles for GUI in the system.
     * <p>
     * 
     * @return Return all the localization profiles for GUI as a vector.
     * @exception RemoteException
     *                System or network related exception.
     * @exception GeneralException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public Vector getAllL10nProfilesForGUI() throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            Vector args = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
            HashMap map = new HashMap();
            map.put(CompanyWrapper.COPMANY_ID_START_ARG, args.get(0));
            map.put(CompanyWrapper.COPMANY_ID_END_ARG, args.get(1));

            ArrayList result = (ArrayList) HibernateUtil.searchWithSql(
                    L10nProfileDescriptorModifier.CURRENT_PROFILEINFOS_GUI_SQL,
                    map);
            return L10nProfileForGUIQueryResultHandler.handleResult(result);
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILES_FOR_GUI,
                    null, pe);
        }
    }

    public Vector getAllL10nProfilesForGUI(String[] filterParams,
            Locale uiLocale) throws RemoteException, ProjectHandlerException
    {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(getQueryL10nProfileInfoSql(filterParams));
            Vector v = new Vector();
            while (rs.next())
            {
                long id = Long.valueOf(rs.getString("id"));
                String name = rs.getString("name");
                String description = rs.getString("description");
                String companyId = rs.getString("companyid");
                String tmpName = rs.getString("tmpname");
                String projectName = rs.getString("project_name");
                char isAutoDispatch = rs.getString("is_auto_dispatch")
                        .toCharArray()[0];
                String srcLocaleId = rs.getString("source_locale_id");
                int wftCount = Integer.valueOf(rs.getString("countwft"));
                BasicL10nProfileInfo basicL10nProfileInfo = new BasicL10nProfileInfo(
                        id, name, description, companyId);
                basicL10nProfileInfo.setTmProfileName(tmpName);
                basicL10nProfileInfo.setProjectName(projectName);
                basicL10nProfileInfo.setIsAutoDispatch(isAutoDispatch);
                basicL10nProfileInfo.setSrcLocaleName(CacheData
                        .getLocaleDisplayNameById(srcLocaleId, uiLocale));
                basicL10nProfileInfo.setWFTCount(wftCount);
                v.add(basicL10nProfileInfo);
            }
            return v;
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILES_FOR_GUI,
                    null, pe);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(st);
            DbUtil.silentReturnConnection(conn);
        }
    }

    private String getQueryL10nProfileInfoSql(String[] filterParams)
    {
        Vector args = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
        StringBuffer sql = new StringBuffer(
                "select l10n.ID, l10n.NAME, l10n.DESCRIPTION,l10n.COMPANYID,tmp.NAME TMPNAME, p.PROJECT_NAME,l10n.IS_AUTO_DISPATCH,l10n.SOURCE_LOCALE_ID,count(lpwi.WF_TEMPLATE_ID) countwft");
        sql.append(" from l10n_profile l10n, l10n_profile_tm_profile lptp, tm_profile tmp, project p, company c,l10n_profile_wftemplate_info lpwi");
        sql.append(" where 1 = 1");
        sql.append(" and l10n.IS_ACTIVE = 'Y'");
        sql.append(" and lpwi.IS_ACTIVE = 'Y'");
        sql.append(" and l10n.ID = lpwi.L10N_PROFILE_ID");
        sql.append(" and l10n.ID =  (select max(l10n2.ID) from l10n_profile l10n2 where l10n2.NAME = l10n.NAME and l10n2.COMPANYID = l10n.COMPANYID)");
        sql.append(" and l10n.COMPANYID >= " + args.get(0));
        sql.append(" and l10n.COMPANYID <= " + args.get(1));
        if (filterParams[0] != null && filterParams[0].trim().length() > 0)
        {
            sql.append(" and l10n.NAME LIKE '%" + filterParams[0] + "%'");
        }
        sql.append(" and l10n.COMPANYID = c.ID");
        if (filterParams[1] != null && filterParams[1].trim().length() > 0)
        {
            sql.append(" and c.NAME LIKE '%" + filterParams[1] + "%'");
        }
        sql.append(" and l10n.ID = lptp.l10n_profile_id");
        sql.append(" and lptp.tm_profile_id = tmp.ID ");
        if (filterParams[2] != null && filterParams[2].trim().length() > 0)
        {
            sql.append(" and tmp.NAME LIKE '%" + filterParams[2] + "%'");
        }
        sql.append(" and l10n.PROJECT_ID = p.PROJECT_SEQ");
        if (filterParams[3] != null && filterParams[3].trim().length() > 0)
        {
            sql.append(" and p.PROJECT_NAME LIKE '%" + filterParams[3] + "%'");
        }
        sql.append(" group by l10n.ID, l10n.NAME, l10n.DESCRIPTION, "
                    + "l10n.COMPANYID, tmp.NAME, p.PROJECT_NAME, "
                    + "l10n.IS_AUTO_DISPATCH,l10n.SOURCE_LOCALE_ID");
        sql.append(" order by l10n.NAME ");
        return sql.toString();
    }

    /**
     * Get the names (and primary keys) of all the localization profiles. The
     * key in the hashtable is the primary key.
     * <p>
     * 
     * @return Return all the localization profiles names as a hashtable.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public Hashtable getAllL10nProfileNames() throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);

            String sql = L10nProfileDescriptorModifier.CURRENT_PROFILEINFOS_SQL;
            ArrayList result = (ArrayList) HibernateUtil
                    .searchWithSql(sql, map);
            return L10nProfileQueryResultHandler.handleResult(result);
        }
        catch (PersistenceException pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROFILE_NAMES,
                    null, pe);
        }
    }

    /**
     * Get a localization profile by its ID.
     * <p>
     * 
     * @param p_profileId
     *            The primary ID of the localization profile.
     * @return The localization profile.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public L10nProfile getL10nProfile(long p_profileId) throws RemoteException,
            ProjectHandlerException
    {
        // There should be one localization profile returned from the querey
        try
        {
            // Ignore the isActive is Y or N
            L10nProfile l10nProfile = (L10nProfile) HibernateUtil.getSession()
                    .get(BasicL10nProfile.class, p_profileId);

            if (l10nProfile != null)
            {
                return l10nProfile;
            }

            // throw exception
            c_category.error("Failed to find L10nProfile " + p_profileId);
            String[] args = new String[1];
            args[0] = Long.toString(p_profileId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_LOCALIZATION_PROFILE,
                    args, null);

        }
        catch (Exception pe)
        {
            c_category.error(
                    Long.toString(p_profileId) + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = Long.toString(p_profileId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_LOCALIZATION_PROFILE,
                    args, pe);
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // END: Localization Profiles ///////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////
    // BEGIN: Projects //////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Add a project to the system.
     * <p>
     * 
     * @param p_project
     *            The project to be added.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to add the project; the cause is indicated by the
     *                exception code.
     */
    public void addProject(Project p_project) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            HibernateUtil.save(p_project);

            SetDefaultRoleUtil.setUserDefaultRoleToProject(p_project);
        }
        catch (PersistenceException pe)
        {
            c_category.error(p_project.toString() + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = p_project.getName();
            // if a sql exception - duplicate value probably
            if (pe.getOriginalException().getClass() == java.sql.SQLException.class)
            {
                throw new ProjectHandlerException(
                        ProjectHandlerException.MSG_FAILED_TO_ADD_PROJECT_ALREADY_EXISTS,
                        args, pe);
            }

            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ADD_PROJECT, args, pe);

        }
        catch (Exception te)
        {
            c_category.error(p_project.toString() + " " + te.getMessage(), te);
            String[] args = new String[1];
            args[0] = p_project.getName();

            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ADD_PROJECT, args, te);

        }
        try
        {
            // add project to all vendors that are marked to be added
            // to any future projects
            ServerProxy.getVendorManagement().addVendorsToProject(p_project);
        }
        catch (Exception e)
        {
            String args[] =
            { p_project.getName() };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ADD_VENDORS_TO_PROJECT,
                    args, e);
        }
    }

    /**
     * Create a project.
     * <p>
     * 
     * @return Return the project that is created.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to create the project; the cause is indicated by
     *                the exception code.
     */
    public Project createProject() throws RemoteException,
            ProjectHandlerException
    {
        return new ProjectImpl();
    }

    /**
     * @see ProjectHandler.modifyProject(Project, String)
     */
    public void modifyProject(Project p_project, String p_modifierId)
            throws RemoteException, ProjectHandlerException
    {
        org.hibernate.Session session = null;
        Transaction transaction = null;
        ProjectImpl originalProject = (ProjectImpl) p_project;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            ProjectImpl clone = (ProjectImpl) session.get(ProjectImpl.class,
                    new Long(originalProject.getId()));
            String originalPm = clone.getProjectManagerId();

            // merge the changes in
            clone.setName(originalProject.getName());
            clone.setDescription(originalProject.getDescription());
            clone.setProjectManager(originalProject.getProjectManager());
            clone.setQuotePerson(null);
            clone.setTermbaseName(originalProject.getTermbaseName());
            clone.setUserIds(originalProject.getUserIds());
            clone.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                    .getValue()));
            clone.setAttributeSet(originalProject.getAttributeSet());
            clone.setPMCost(originalProject.getPMCost());
            clone.setPoRequired(originalProject.getPoRequired());
            clone.setAutoAcceptTrans(originalProject
            		.getAutoAcceptTrans());
            clone.setAutoSendTrans(originalProject.getAutoSendTrans());
            clone.setReviewOnlyAutoAccept(originalProject
                    .getReviewOnlyAutoAccept());
            clone.setReviewOnlyAutoSend(originalProject.getReviewOnlyAutoSend());
            clone.setReviewReportIncludeCompactTags(originalProject
                    .isReviewReportIncludeCompactTags());
            clone.setAutoAcceptPMTask(originalProject.getAutoAcceptPMTask());
            clone.setCheckUnTranslatedSegments(originalProject
                    .isCheckUnTranslatedSegments());
            clone.setSaveTranslationsEditReport(originalProject
                    .getSaveTranslationsEditReport());
            clone.setSaveReviewersCommentsReport(originalProject
                    .getSaveReviewersCommentsReport());
            clone.setSaveOfflineFiles(originalProject.getSaveOfflineFiles());
            clone.setAllowManualQAChecks(originalProject
                    .getAllowManualQAChecks());
            clone.setAutoAcceptQATask(originalProject.getAutoAcceptQATask());
            clone.setAutoSendQAReport(originalProject.getAutoSendQAReport());
            clone.setManualRunDitaChecks(originalProject
                    .getManualRunDitaChecks());
            clone.setAutoAcceptDitaQaTask(originalProject
                    .getAutoAcceptDitaQaTask());
            clone.setAutoSendDitaQaReport(originalProject
                    .getAutoSendDitaQaReport());

            String quotePersonId = originalProject.getQuotePersonId();
            if (quotePersonId != null && !"".equals(quotePersonId))
            {
                if ("0".equals(quotePersonId))
                {
                    clone.setQuotePerson("0");
                }
                else
                {
                    User quotePerson = null;
                    try
                    {
                        quotePerson = lookupUserManager()
                                .getUser(quotePersonId);
                    }
                    catch (UserManagerException ume)
                    {
                        // do nothing
                    }
                    if (quotePerson != null)
                    {
                        clone.setQuotePerson(quotePerson);
                    }
                    else
                    {
                        clone.setQuotePerson("0");
                    }
                }
            }
            session.update(clone);
            transaction.commit();

            SetDefaultRoleUtil.setUserDefaultRoleToProject(originalProject);

            if (!originalProject.getProjectManagerId().equals(originalPm))
            {
                Serializable msg = new ProjectUpdateMessage(p_modifierId,
                        originalPm, originalProject.getProjectManagerId(),
                        originalProject.getIdAsLong());
                try
                {
                    JmsHelper.sendMessageToQueue(msg,
                            JmsHelper.JMS_PROJECT_UPDATE_QUEUE);
                }
                catch (Exception e)
                {
                    c_category
                            .error("Failed to send JMS message for project update.",
                                    e);
                }
            }
        }
        catch (PersistenceException pe)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            c_category.error(p_project.toString() + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = Long.toString(p_project.getId());
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_MODIFY_PROJECT, args,
                    pe);
        }
    }

    /**
     * Delete a project from the system.
     * <p>
     * 
     * @param p_project
     *            The project to be deleted.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Failed to delete the project; the cause is indicated by
     *                the exception code.
     */
    public void deleteProject(Project p_project) throws RemoteException,
            ProjectHandlerException
    {
        if (c_category.isDebugEnabled())
        {
            c_category.debug("deleteProject "
                    + ((ProjectImpl) p_project).toDebugString());
        }

        List projects = new ArrayList();
        projects.add(p_project);
        Collection l10nProfiles = getL10nProfiles(projects);
        if (!l10nProfiles.isEmpty())
        {
            throw new ProjectHandlerDeleteConstrainedProjectException(
                    p_project.getName()
                            + " with id "
                            + ((ProjectImpl) p_project).getIdAsLong()
                                    .toString()
                            + " is associated with L10nProfile "
                            + l10nProfiles.toString());
        }

        try
        {
            // remove all vendors that are part of this project
            ServerProxy.getVendorManagement().addVendorsToProject(p_project);
            HibernateUtil.delete(p_project);
        }
        catch (Exception pe)
        {
            c_category.error(p_project.toString() + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = Long.toString(p_project.getId());
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_PROJECT, args,
                    pe);
        }
    }

    /**
     * Returns all the projects in the system.
     * <p>
     * 
     * @return Return all the projects in the system.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public Collection<Project> getAllProjects() throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            String hql = "from ProjectImpl project where project.isActive = 'Y' ";
            String currentId = CompanyThreadLocal.getInstance().getValue();

            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql = hql + " and project.companyId ="
                        + Long.parseLong(currentId);
            }

            List queryResult = HibernateUtil.search(hql);
            for (Iterator i = queryResult.iterator(); i.hasNext();)
            {
                Project project = (Project) i.next();
                User pm = null;
                try
                {
                    pm = lookupUserManager().getUser(
                            project.getProjectManagerId());
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (pm != null)
                {
                    project.setProjectManager(pm);
                }
                String quotePersonId = project.getQuotePersonId();
                if (quotePersonId != null && !"".equals(quotePersonId))
                {
                    if ("0".equals(quotePersonId))
                    {
                        project.setQuotePerson("0");
                    }
                    else
                    {
                        User quotePerson = null;
                        try
                        {
                            quotePerson = lookupUserManager().getUser(
                                    quotePersonId);
                        }
                        catch (UserManagerException ume)
                        {
                            // do nothing
                        }
                        if (quotePerson != null)
                        {
                            project.setQuotePerson(quotePerson);
                        }
                        else
                        {
                            project.setQuotePerson("0");
                        }
                    }
                }
            }
            return queryResult;
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROJECTS,
                    null, pe);
        }
    }

    /**
     * @see ProjectHandler.getAllProjectInfosForGUI.
     */
    public List<ProjectInfo> getAllProjectInfosForGUI() throws RemoteException,
            ProjectHandlerException
    {
        return getAllProjectInfosForGUIbyCondition("");
    }

    public List<ProjectInfo> getAllProjectInfosForGUIbyCondition(
            String condition) throws RemoteException, ProjectHandlerException
    {
        try
        {
            String hql = "select new com.globalsight.everest.projecthandler.ProjectInfo"
                    + "( p )"
                    + " from ProjectImpl p ,Company c where p.isActive = 'Y' and c.id=p.companyId";
            Session session = HibernateUtil.getSession();
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and p.companyId = " + Long.parseLong(currentId);
            }

            hql += condition;

            List<ProjectInfo> queryResult = session.createQuery(hql).list();
            List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
            List allUsers = (List) lookupUserManager().getUsers();
            HashMap<String, String> idViewName = new HashMap<String, String>();
            for (Iterator iter = allUsers.iterator(); iter.hasNext();)
            {
                User user = (User) iter.next();
                idViewName.put(user.getUserId(), user.getUserName());
            }
            for (int i = 0; i < queryResult.size(); i++)
            {
                ProjectInfo pi = (ProjectInfo) queryResult.get(i);

                if (pi != null && pi.getProjectManagerName() == null)
                {
                    String usrName = idViewName.get(pi.getProjectManagerId());

                    if (usrName != null)
                    {
                        pi.setProjectManagerName(usrName);
                    }
                    else
                    {
                        pi.setProjectManagerName("");
                    }
                    projectInfos.add(pi);
                }
            }

            return projectInfos;
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROJECTS_FOR_GUI,
                    null, pe);
        }
    }

    /**
     * Get the names (and primary keys) of all the projects. The key in the
     * hashtable is the primary key.
     * <p>
     * 
     * @return All the names and keys of all projects.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Miscellaneous exception, most likely occuring in the
     *                persistence component.
     */
    public Hashtable getAllProjectNames() throws RemoteException,
            ProjectHandlerException
    {
        Hashtable projectNames = new Hashtable();

        try
        {
            String hql = "select p.name,p.id from ProjectImpl p "
                    + " where p.isActive = 'Y' "
                    + " p.companyId >=:companyIdStart "
                    + " and p.companyId <=:companyIdEnd";
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    "companyIdStart", "companyIdEnd");
            ArrayList setOfProjectNames = (ArrayList) ProjectQueryResultHandler
                    .handleResult(HibernateUtil.search(hql, map));

            if (setOfProjectNames != null && setOfProjectNames.size() > 0)
                projectNames = (Hashtable) setOfProjectNames.get(0);

            if (c_category.isDebugEnabled())
            {
                c_category.debug("getAllProjectNames returns "
                        + projectNames.toString());
            }
            return projectNames;
        }
        catch (Exception pe)
        {
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_PROJECTS,
                    null, pe);
        }
    }

    /**
     * Get project by the project id.
     * <p>
     * 
     * @param p_id
     *            The project id.
     * @return Return the specified project.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    public Project getProjectById(long p_id) throws RemoteException,
            ProjectHandlerException
    {
        Project project = null;

        try
        {
            project = (Project) HibernateUtil.get(ProjectImpl.class, p_id);
            if (project == null)
            {
                return null;
            }

            User pm = null;
            try
            {
                pm = lookupUserManager().getUser(project.getProjectManagerId());
            }
            catch (UserManagerException ume)
            {
                // do nothing
            }
            if (pm != null)
            {
                project.setProjectManager(pm);
            }
            String quotePersonId = project.getQuotePersonId();
            if (quotePersonId != null && !"".equals(quotePersonId))
            {
                if ("0".equals(quotePersonId))
                {
                    project.setQuotePerson("0");
                }
                else
                {
                    User quotePerson = null;
                    try
                    {
                        quotePerson = lookupUserManager()
                                .getUser(quotePersonId);
                    }
                    catch (UserManagerException ume)
                    {
                        // do nothing
                    }
                    if (quotePerson != null)
                    {
                        project.setQuotePerson(quotePerson);
                    }
                    else
                    {
                        project.setQuotePerson("0");
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectById " + Long.toString(p_id)
                        + " returned "
                        + ((ProjectImpl) project).toDebugString());
            }

            return project;
        }
        catch (Exception pe)
        {
            c_category.error(Long.toString(p_id) + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = Long.toString(p_id);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_BY_ID,
                    args, pe);
        }
    }

    /**
     * Get project by the project name and company id.
     */
    public Project getProjectByNameAndCompanyId(String p_name, long companyId)
            throws RemoteException, ProjectHandlerException
    {
        Project project = null;

        try
        {
            String hql = "from ProjectImpl p where p.name = :name "
                    + "and p.companyId = :companyId";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", p_name);
            map.put("companyId", companyId);

            Collection result = HibernateUtil.search(hql, map);
            if (result == null || result.isEmpty())
            {
                return null;
            }
            // set the user object to the project through the user manager id
            project = (Project) result.iterator().next();
            User pm = null;
            try
            {
                pm = lookupUserManager().getUser(project.getProjectManagerId());
            }
            catch (UserManagerException ume)
            {
                // do nothing
            }
            if (pm != null)
            {
                project.setProjectManager(pm);
            }
            String quotePersonId = project.getQuotePersonId();
            if (quotePersonId != null && !"".equals(quotePersonId))
            {
                if ("0".equals(quotePersonId))
                {
                    project.setQuotePerson("0");
                }
                else
                {
                    User quotePerson = null;
                    try
                    {
                        quotePerson = lookupUserManager()
                                .getUser(quotePersonId);
                    }
                    catch (UserManagerException ume)
                    {
                        // do nothing
                    }
                    if (quotePerson != null)
                    {
                        project.setQuotePerson(quotePerson);
                    }
                    else
                    {
                        project.setQuotePerson("0");
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectByNameAndCompanyId " + p_name
                        + " returned "
                        + ((ProjectImpl) project).toDebugString());
            }

            return project;
        }
        catch (Exception pe)
        {
            c_category.error(p_name + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = p_name;
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_BY_NAME,
                    args, pe);
        }
    }

    /**
     * Get projects by company id.
     * 
     * @param companyId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Project> getProjectsByCompanyId(long companyId)
    {
        List<Project> projects = new ArrayList<Project>();

        try
        {
            String hsql = "from ProjectImpl p where p.isActive = 'Y'";
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(String
                    .valueOf(companyId)))
            {
                hsql += " and p.companyId =" + companyId;
            }

            projects = (List<Project>) HibernateUtil.search(hsql);
            return projects;
        }
        catch (Exception e)
        {
            c_category.error("Failed to query projects by company id.", e);
            return null;
        }
    }

    /**
     * @deprecated I don't think this method is reliable (York).
     * 
     *             Get project by the project name.
     *             <p>
     * 
     * @param p_name
     *            The project to be found under this name.
     * @return Return the specified project.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     * 
     */
    public Project getProjectByName(String p_name) throws RemoteException,
            ProjectHandlerException
    {
        Project project = null;

        try
        {
            String hql = "from ProjectImpl p where p.name = :name";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", p_name);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and p.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            Collection result = HibernateUtil.search(hql, map);

            if (result == null || result.isEmpty())
            {
                return null;
            }
            // set the user object to the project through the user manager id
            project = (Project) result.iterator().next();
            User pm = null;
            try
            {
                pm = lookupUserManager().getUser(project.getProjectManagerId());
            }
            catch (UserManagerException ume)
            {
                // do nothing
            }
            if (pm != null)
            {
                project.setProjectManager(pm);
            }
            String quotePersonId = project.getQuotePersonId();
            if (quotePersonId != null && !"".equals(quotePersonId))
            {
                if ("0".equals(quotePersonId))
                {
                    project.setQuotePerson("0");
                }
                else
                {
                    User quotePerson = null;
                    try
                    {
                        quotePerson = lookupUserManager()
                                .getUser(quotePersonId);
                    }
                    catch (UserManagerException ume)
                    {
                        // do nothing
                    }
                    if (quotePerson != null)
                    {
                        project.setQuotePerson(quotePerson);
                    }
                    else
                    {
                        project.setQuotePerson("0");
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectByName " + p_name + " returned "
                        + ((ProjectImpl) project).toDebugString());
            }

            return project;
        }
        catch (Exception pe)
        {
            c_category.error(p_name + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = p_name;
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_BY_NAME,
                    args, pe);
        }
    }

    /**
     * @see ProjectHandler.getProjectsByUserPermission(User)
     */
    public List<Project> getProjectsByUserPermission(User p_user)
            throws RemoteException, ProjectHandlerException
    {
        // use a set nad comparator off of the project name
        // so no duplicate projects are returned
        ProjectComparator pc = new ProjectComparator(new Locale(
                p_user.getDefaultUILocale()));
        Set<Project> projects = new TreeSet<Project>(pc);

        try
        {
            PermissionSet userPerms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());

            // if the user has permission to look at all projects then
            // return all
            if (userPerms.getPermissionFor(Permission.GET_ALL_PROJECTS))
            {
                // if PM isn't in all projects, then return the project which PM
                // belongs to.
                if (!p_user.isInAllProjects())
                {
                    projects.addAll(getProjectsByUser(p_user.getUserId()));
                }
                else
                {
                    projects.addAll(getAllProjects());
                }
            }
            else
            {
                // if the user manages projects and can get all projects they
                // manage
                if (userPerms.getPermissionFor(Permission.PROJECTS_MANAGE)
                        && userPerms
                                .getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
                {
                    projects.addAll(getProjectsManagedByUser(p_user,
                            Permission.GROUP_MODULE_GLOBALSIGHT));
                }
                // if the user can get the projects they belong to also
                if (userPerms
                        .getPermissionFor(Permission.GET_PROJECTS_I_BELONG))
                {
                    projects.addAll(getProjectsByUser(p_user.getUserId()));
                }
            }
        }
        catch (Exception e)
        {
            c_category
                    .error("Couldn't get the projects by permission for user "
                            + p_user.getUserName());
            String args[] =
            { p_user.getUserName() };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_PERMISSION,
                    args, e);
        }

        return new ArrayList<Project>(projects);
    }

    /**
     * @see ProjectHandler.getProjectInfosManagedByUser(User, String)
     */
    public List getProjectInfosManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException
    {
        List projectInfos = null;
        try
        {

            PermissionSet userPerms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());
            if (userPerms.getPermissionFor(Permission.GET_ALL_PROJECTS))
            {
                // get all project infos since the requestor can see all
                // projects
                projectInfos = getAllProjectInfosForGUI();
            }
            else if (userPerms
                    .getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
            {
                projectInfos = getProjectInfosByProjectManager(p_user);
            }
            else
            {
                projectInfos = getProjectInfosByUser(p_user.getUserId());
            }
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(e.getMessage());
        }
        return projectInfos;
    }

    /**
     * @see ProjectHandler.getProjectsManagedByUser(User, String)
     */
    public List<Project> getProjectsManagedByUser(User p_user, String p_module)
            throws RemoteException, ProjectHandlerException
    {
        List<Project> projects = new ArrayList<Project>();
        PermissionSet userPerms = null;
        try
        {
            userPerms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(e.getMessage());
        }

        if (userPerms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            // get all project since the requestor is an admin
            projects.addAll(getAllProjects());
        }
        else if (userPerms.getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
        {
            projects.addAll(getProjectsByProjectManager(p_user));
        }
        else
        {
            projects = getProjectsByUser(p_user.getUserId());
        }

        return projects;
    }

    /**
     * @see ProjectHandler.getProjectInfosByUser(String)
     */
    public List getProjectInfosByUser(String p_userId) throws RemoteException,
            ProjectHandlerException

    {
        try
        {
            List projectInfos = null;

            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            map.put("userId", p_userId);
            List list = HibernateUtil.searchWithSql(
                    ProjectDescriptorModifier.PROJECT_INFO_BY_USER_ID_SQL, map,
                    ProjectImpl.class);
            Collection queryResult = ProjectQueryResultHandler
                    .handleResultForGUI(list);
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectInfosByUser " + p_userId
                        + " returns " + queryResult.toString());
            }
            if (!queryResult.isEmpty())
            {
                projectInfos = new ArrayList(queryResult);
            }
            else
            {
                projectInfos = new ArrayList();
            }
            return projectInfos;
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to get all the project infos for a "
                    + " user " + p_userId, pe);
            String[] args = new String[1];
            args[0] = p_userId;
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_INFOS_BY_USER,
                    args, pe);
        }
    }

    /**
     * @see ProjectHandler.getProjectsByUser(String)
     */
    public List<Project> getProjectsByUser(String p_userId)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            List projects = null;
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            map.put(ProjectDescriptorModifier.USER_ID_ARG, p_userId);

            Collection queryResult = HibernateUtil.searchWithSql(
                    ProjectDescriptorModifier.PROJECTS_BY_USER_ID_SQL, map,
                    ProjectImpl.class);

            // setting the user object to the project through the user
            // manager id
            for (Iterator i = queryResult.iterator(); i.hasNext();)
            {
                Project project = (Project) i.next();
                User pm = null;
                try
                {
                    pm = lookupUserManager().getUser(
                            project.getProjectManagerId());
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (pm != null)
                {
                    project.setProjectManager(pm);
                }
                String quotePersonId = project.getQuotePersonId();
                if (quotePersonId != null && !"".equals(quotePersonId))
                {
                    if ("0".equals(quotePersonId))
                    {
                        project.setQuotePerson("0");
                    }
                    else
                    {
                        User quotePerson = null;
                        try
                        {
                            quotePerson = lookupUserManager().getUser(
                                    quotePersonId);
                        }
                        catch (UserManagerException ume)
                        {
                            // do nothing
                        }
                        if (quotePerson != null)
                        {
                            project.setQuotePerson(quotePerson);
                        }
                        else
                        {
                            project.setQuotePerson("0");
                        }
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectsByUser " + p_userId + " returns "
                        + queryResult.toString());
            }
            if (!queryResult.isEmpty())
            {
                projects = new ArrayList(queryResult);
            }
            else
            {
                projects = new ArrayList();
            }
            return projects;
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to get all the projects for a " + " user "
                    + p_userId, pe);
            String[] args = new String[1];
            args[0] = p_userId;
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_USER_ID,
                    args, pe);
        }
    }

    /**
     * @see ProjectHandler.getProjectsByVendor(long)
     */
    public List getProjectsByVendor(long p_vendorId) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            List projects = null;
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            map.put(ProjectDescriptorModifier.VENDOR_ID_ARG, new Long(
                    p_vendorId));

            Collection queryResult = HibernateUtil.searchWithSql(
                    ProjectDescriptorModifier.PROJECTS_BY_VENDOR_ID_SQL, map,
                    ProjectImpl.class);
            // setting the user object to the project through the user
            // manager id
            for (Iterator i = queryResult.iterator(); i.hasNext();)
            {
                Project project = (Project) i.next();
                User pm = null;
                try
                {
                    pm = lookupUserManager().getUser(
                            project.getProjectManagerId());
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (pm != null)
                {
                    project.setProjectManager(pm);
                }
                String quotePersonId = project.getQuotePersonId();
                if (quotePersonId != null && !"".equals(quotePersonId))
                {
                    if ("0".equals(quotePersonId))
                    {
                        project.setQuotePerson("0");
                    }
                    else
                    {
                        User quotePerson = null;
                        try
                        {
                            quotePerson = lookupUserManager().getUser(
                                    quotePersonId);
                        }
                        catch (UserManagerException ume)
                        {
                            // do nothing
                        }
                        if (quotePerson != null)
                        {
                            project.setQuotePerson(quotePerson);
                        }
                        else
                        {
                            project.setQuotePerson("0");
                        }
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectsByVendor " + p_vendorId
                        + " returns " + queryResult.toString());
            }
            if (!queryResult.isEmpty())
            {
                projects = new ArrayList(queryResult);
            }
            else
            {
                projects = new ArrayList();
            }

            return projects;
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to get all the projects for " + " vendor "
                    + p_vendorId, pe);
            String[] args = new String[1];
            args[0] = Long.toString(p_vendorId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_VENDOR_ID,
                    args, pe);
        }
    }

    /**
     * Get projects by the workflow instance ids.
     * <p>
     * 
     * @param p_workflowInstanceIds
     *            An array of workflow instance ids.
     * @return Return all the projects by the specified id.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    public Collection<Project> getProjectsByWorkflowInstanceId(
            long[] p_workflowInstanceIds) throws RemoteException,
            ProjectHandlerException
    {
        Project project = null;
        int size = p_workflowInstanceIds.length;
        StringBuffer querySBuffer = new StringBuffer(
                p_workflowInstanceIds.length * 5);
        querySBuffer.append("{");
        for (int i = 0; i < size; i++)
        {
            querySBuffer.append(p_workflowInstanceIds
                    + (i != size - 1 ? ", " : ""));
        }
        querySBuffer.append("}");

        try
        {
            String sql = "SELECT DISTINCT * FROM project WHERE project_seq IN "
                    + "(SELECT DISTINCT * FROM l10n_profile WHERE profile_seq IN "
                    + "(SELECT * FROM job WHERE job_seq IN "
                    + "(SELECT * FROM workflow WHERE workflow_id = :workflowIds";
            HashMap map = new HashMap();
            map.put("workflowIds", querySBuffer.toString());
            List resultSetObjects = HibernateUtil.searchWithSql(sql, map,
                    ProjectImpl.class);

            for (int i = 0; i < resultSetObjects.size(); i++)
            {
                project = (Project) resultSetObjects.get(i);
                User pm = null;
                try
                {
                    pm = lookupUserManager().getUser(
                            project.getProjectManagerId());
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (pm != null)
                {
                    project.setProjectManager(pm);
                }
                String quotePersonId = project.getQuotePersonId();
                if (quotePersonId != null && !"".equals(quotePersonId))
                {
                    if ("0".equals(quotePersonId))
                    {
                        project.setQuotePerson("0");
                    }
                    else
                    {
                        User quotePerson = null;
                        try
                        {
                            quotePerson = lookupUserManager().getUser(
                                    quotePersonId);
                        }
                        catch (UserManagerException ume)
                        {
                            // do nothing
                        }
                        if (quotePerson != null)
                        {
                            project.setQuotePerson(quotePerson);
                        }
                        else
                        {
                            project.setQuotePerson("0");
                        }
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectsByWorkflowInstanceId "
                        + ArrayConverter.asList(p_workflowInstanceIds)
                                .toString() + " returns "
                        + resultSetObjects.toString());
            }

            return resultSetObjects;
        }
        catch (PersistenceException pe)
        {
            c_category.error(querySBuffer.toString() + " " + pe.getMessage(),
                    pe);
            String[] args = new String[1];
            args[0] = querySBuffer.substring(1, querySBuffer.length() - 2);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_WFI_ID,
                    args, pe);
        }
    }

    /**
     * @see ProjectHandler.addUserToProjects(String, List)
     */
    public void addUserToProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException
    {
        StringBuffer projectIdList = new StringBuffer();
        Session session = null;
        Transaction tx = null;
        try
        {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            Set s = new TreeSet();
            s.add(p_userId);
            for (Iterator pi = p_projects.iterator(); pi.hasNext();)
            {
                Project p = (Project) pi.next();
                p.addUserIds(s);
                projectIdList.append(Long.toString(p.getId()));
                if (pi.hasNext())
                {
                    projectIdList.append(", ");
                }
                session.saveOrUpdate(p);
            }
            // commit all the user additions to projects
            tx.commit();
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to add user " + p_userId + " to projects "
                    + projectIdList.toString(), pe);
            String[] args = new String[2];
            args[0] = p_userId;
            args[1] = projectIdList.toString();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ADD_USER_TO_PROJECTS,
                    args, pe);
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * @see ProjectHandler.removeUserFromProjects(String, List)
     */
    public void removeUserFromProjects(String p_userId, List p_projects)
            throws RemoteException, ProjectHandlerException
    {
        StringBuffer projectIdList = new StringBuffer();
        Session session = null;
        Transaction tx = null;
        try
        {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            Set s = new TreeSet();
            s.add(p_userId);
            for (Iterator pi = p_projects.iterator(); pi.hasNext();)
            {
                Project p = (Project) pi.next();
                p.removeUserIds(s);
                projectIdList.append(Long.toString(p.getId()));
                if (pi.hasNext())
                {
                    projectIdList.append(", ");
                }
                session.update(p);
            }
            // commit all the user removals from projects
            tx.commit();
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to remove user " + p_userId
                    + " from projects " + projectIdList.toString(), pe);
            String[] args = new String[2];
            args[0] = p_userId;
            args[1] = projectIdList.toString();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_USER_FROM_PROJECTS,
                    args, pe);
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * @see ProjectHandler.associateUserWithProjectsById(String, List)
     */
    public void associateUserWithProjectsById(String p_userId, List p_projectIds)
            throws RemoteException, ProjectHandlerException
    {
        Session session = null;
        Transaction tx = null;
        try
        {
            // get all the current projects a user is in
            List projects = getProjectsByUser(p_userId);
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            for (Iterator i = projects.iterator(); i.hasNext();)
            {
                Project p = (Project) i.next();
                Long pId = p.getIdAsLong();
                if (!p_projectIds.contains(pId))
                {
                    // deleted user from project and remove from existing id
                    // list so it leaves only the NEW ones in the list
                    p.removeUserId(p_userId);
                    p_projectIds.remove(pId);
                    session.saveOrUpdate(p);
                }
            }

            // add the new projects to the user
            for (Iterator pi = p_projectIds.iterator(); pi.hasNext();)
            {
                long projectId = ((Long) pi.next()).longValue();
                Project p = getProjectById(projectId);
                p.addUserId(p_userId);
                session.saveOrUpdate(p);
            }
            tx.commit();
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to associate " + p_userId
                    + " with projects " + p_projectIds.toString(), pe);
            String[] args = new String[2];
            args[0] = p_userId;
            args[1] = p_projectIds.toString();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ASSOCIATE_USER_WITH_PROJECTS,
                    args, pe);
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * @see ProjectHandler.getAllPossibleUserInfos(User)
     */
    public List<UserInfo> getAllPossibleUserInfos(User p_manager)
            throws RemoteException, ProjectHandlerException
    {
        ArrayList userInfos = new ArrayList();
        try
        {
            // for NOW get all the information for all active users
            // and NOT just by project.
            // will change once the shared/locked/hidden concept has been added
            UserManager um = lookupUserManager();
            userInfos = new ArrayList(um.getUserInfos());
        }
        catch (UserManagerException ume)
        {
            c_category
                    .error("Failed to get all the users project manager "
                            + p_manager.getUserName()
                            + " can manage."
                            + "  Could not retrieve one or more users from UserManager.",
                            ume);
            String[] args =
            { p_manager.getUserName() };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_ALL_USERS_PM_MANAGES,
                    args, ume);
        }

        return userInfos;
    }

    /**
     * @see ProjectHandler.getUserIdsInSameProjects(User)
     */
    public List getUserIdsInSameProjects(User p_user) throws RemoteException,
            ProjectHandlerException
    {
        // Get all projects that the project manager manages
        List projects = getProjectsByUser(p_user.getUserId());

        // get all the DISTINCT users in the projects
        Set userIds = new TreeSet();
        for (Iterator pi = projects.iterator(); pi.hasNext();)
        {
            Project p = (Project) pi.next();
            Set pUserIds = p.getUserIds();
            for (Iterator i = pUserIds.iterator(); i.hasNext();)
            {
                // add the user id - if it already exists in the set
                // it'll just be a NOP
                userIds.add((String) i.next());
            }
        }
        return new ArrayList(userIds);
    }

    /**
     * @see ProjectHandler.getProjectsWithUsers(List)
     */
    public HashMap getProjectsWithUsers(List p_users) throws RemoteException,
            ProjectHandlerException
    {
        HashMap hm = new HashMap();
        if (p_users != null && p_users.size() > 0)
        {
            // query for all projects that contain the users ids passed in
            Vector userIds = new Vector();
            for (Iterator i = p_users.iterator(); i.hasNext();)
            {
                User u = (User) i.next();
                userIds.add(u.getUserId());
            }
            Collection ps = null;
            try
            {
                String sql = ProjectUnnamedQueries.PROJECTS_WITH_USERS_SQL
                        + ProjectUnnamedQueries.buildUserIdsInClause(userIds);
                ps = HibernateUtil.searchWithSql(sql, null, ProjectImpl.class);
            }
            catch (PersistenceException pe)
            {
                c_category
                        .error("Failed to query for all projects associated with a set of user ids.",
                                pe);
                String[] args =
                { userIds.toString() };
                throw new ProjectHandlerException(
                        ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_USER_IDS,
                        args, pe);
            }

            // now loop through them and create the hashmap
            // that contains the projects and the users that are in them
            for (Iterator pi = ps.iterator(); pi.hasNext();)
            {
                Project p = (Project) pi.next();
                List prjUsers = new ArrayList();
                for (Iterator ui = p_users.iterator(); ui.hasNext();)
                {
                    User u = (User) ui.next();
                    if (p.getUserIds().contains(u.getUserId()))
                    {
                        prjUsers.add(u);
                    }
                }
                hm.put(String.valueOf(p.getId()), prjUsers);
            }
        }
        return hm;
    }

    /**
     * Get the L10nProfiles associated with the projects.
     * 
     * @param p_projects
     *            Collection of Projects.
     * @returns List of L10nProfiles associated with the Projects.
     */
    public Collection<L10nProfile> getL10nProfiles(
            Collection<Project> p_projects) throws RemoteException,
            ProjectHandlerException
    {
        // This should be implemented with a query, not this way.
        Collection l10nProfiles = getAllL10nProfiles();
        List returnL10nProfiles = new ArrayList(l10nProfiles.size());
        Iterator l10nProfilesIt = l10nProfiles.iterator();
        while (l10nProfilesIt.hasNext())
        {
            L10nProfile l10nProfile = (L10nProfile) l10nProfilesIt.next();
            Project l10nProfileProject = l10nProfile.getProject();
            Iterator it = p_projects.iterator();
            while (it.hasNext())
            {
                Project project = (Project) it.next();
                if (project.equals(l10nProfileProject))
                {
                    returnL10nProfiles.add(l10nProfile);
                    break;
                }
            }
        }

        return returnL10nProfiles;
    }

    /**
     * Allocates a helper object to import project-related data into the
     * database.
     */
    public IImportManager getProjectDataImportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ImporterException
    {
        if (p_user == null)
        {
            // TODO: throw exception.
        }

        // check if user can import data for this project.

        Project project = getProjectById(p_projectId);

        return new ImportManager(project, new SessionInfo(p_user.getUserId(),
                ""));
    }

    /**
     * Allocates a helper object to export project-related data from the
     * database to files (CSV or XML).
     */
    public IExportManager getProjectDataExportManager(User p_user,
            long p_projectId) throws RemoteException, ProjectHandlerException,
            ExporterException
    {
        if (p_user == null)
        {
            // throw exception.
        }

        // check if user can export data for this project.

        Project project = getProjectById(p_projectId);

        return new ExportManager(project, new SessionInfo(p_user.getUserId(),
                ""));
    }

    /**
     * Get all the projects the specified project manager manages.
     * <p>
     * 
     * @param p_user
     *            The projects found under this user.
     * @return Return all the projects assigned to this user.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    public Collection<Project> getProjectsByProjectManager(User p_user)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            String hql = "from ProjectImpl p where p.managerUserId=:user_id";

            HashMap map = new HashMap();
            map.put("user_id", p_user.getUserId());
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql = hql + " and p.companyId=:company_id";
                map.put("company_id", Long.parseLong(currentCompanyId));
            }
            List queryResult = HibernateUtil.search(hql, map);

            for (Iterator i = queryResult.iterator(); i.hasNext();)
            {
                Project project = (Project) i.next();
                User pm = null;
                try
                {
                    pm = lookupUserManager().getUser(
                            project.getProjectManagerId());
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (pm != null)
                {
                    project.setProjectManager(pm);
                }
                String quotePersonId = project.getQuotePersonId();
                if (quotePersonId != null && !"".equals(quotePersonId))
                {
                    if ("0".equals(quotePersonId))
                    {
                        project.setQuotePerson("0");
                    }
                    else
                    {
                        User quotePerson = null;
                        try
                        {
                            quotePerson = lookupUserManager().getUser(
                                    quotePersonId);
                        }
                        catch (UserManagerException ume)
                        {
                            // do nothing
                        }
                        if (quotePerson != null)
                        {
                            project.setQuotePerson(quotePerson);
                        }
                        else
                        {
                            project.setQuotePerson("0");
                        }
                    }
                }
            }
            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectsByProjectManager "
                        + p_user.toString() + " returns "
                        + queryResult.toString());
            }
            return queryResult;
        }
        catch (Exception pe)
        {
            c_category.error(p_user.toString() + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = p_user.getUserName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_PM,
                    args, pe);
        }
    }

    /**
     * Get information about the projects a project manager manages (the user).
     * <p>
     * 
     * @param p_user
     *            The projects found under this user.
     * @return Return all the projects assigned to this user.
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     */
    private List getProjectInfosByProjectManager(User p_pm)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            List projectInfos = null;
            String hql = "select p.* from project p where p.manager_user_id=:user_id";

            HashMap map = new HashMap();
            map.put("user_id", p_pm.getUserId());
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql = hql + " and p.companyid=:company_id";
                map.put("company_id", currentCompanyId);
            }
            List list = HibernateUtil
                    .searchWithSql(hql, map, ProjectImpl.class);
            Collection queryResult = ProjectQueryResultHandler
                    .handleResultForGUI(list);

            if (!queryResult.isEmpty())
            {
                projectInfos = new ArrayList(queryResult);
            }
            else
            {
                projectInfos = new ArrayList();
            }

            if (c_category.isDebugEnabled())
            {
                c_category.debug("getProjectsByProjectManager "
                        + p_pm.getUserName() + " (" + p_pm.toString() + ") "
                        + "returns " + queryResult.toString());
            }

            return projectInfos;
        }
        catch (Exception pe)
        {
            c_category.error(p_pm.toString() + " " + pe.getMessage(), pe);
            String[] args = new String[1];
            args[0] = p_pm.getUserName();

            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECTS_BY_PM,
                    args, pe);
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // END: Projects ////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Workflow Template Info
    // ////////////////////////////////////////////////////////////////////
    /**
     * @see ProjectHandler.findWorkflowTemplates(WfTemplateSearchParameters)
     */
    public Collection findWorkflowTemplates(
            WfTemplateSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            WfTemplateSearchCriteria criteria = new WfTemplateSearchCriteria();
            return criteria.search(p_searchParameters);
        }
        catch (Exception e)
        {
            c_category
                    .error("Failed to get workflow templates by criteria.", e);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
    }

    /**
     * Create a workflow template info object (contains the workflow template
     * designed via the graphical workflow UI).
     * 
     * @param p_workflowTemplateInfo
     *            The workflow template info to be created.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void createWorkflowTemplateInfo(
            WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            HibernateUtil.save(p_workflowTemplateInfo);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = p_workflowTemplateInfo.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_CREATE_WFI, args, e);
        }
    }

    /**
     * Duplicate a workflow template info object based on the given new name
     * (contains the workflow template designed via the graphical workflow UI).
     * 
     * @param p_newName
     *            - The new name given to the duplicated workflow template info.
     * @param p_wfTemplateInfo
     *            - The workflow template info to be duplicated.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void duplicateWorkflowTemplates(long p_wfTemplateInfoId,
            String p_newName, Project project,
            WorkflowTemplate p_iflowTemplate, Collection p_localePairs,
            String p_displayRoleName) throws RemoteException,
            ProjectHandlerException
    {
        WorkflowTemplateInfo origWorkflowTemplateInfo = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        List<WorkflowTemplateInfo> listAll = WorkflowTemplateHandlerHelper
                .getAllWorkflowTemplateInfos();
        try
        {
            origWorkflowTemplateInfo = getWorkflowTemplateInfoById(p_wfTemplateInfoId);
            WorkflowTemplate workflowTemplate = p_iflowTemplate;
            Iterator it = p_localePairs.iterator();

            while (it.hasNext())
            {
                LocalePair localePair = (LocalePair) it.next();
                String name = generateAutoName(p_newName, localePair);

                Iterator<WorkflowTemplateInfo> it2 = listAll.iterator();
                while (it2.hasNext())
                {
                    WorkflowTemplateInfo wf = it2.next();
                    String nameWf = wf.getName();
                    if (nameWf.equals(name))
                        return;
                }
                c_category.info("The value of name is " + name);
                WorkflowTemplateInfo dupWorkflowTemplateInfo = createDuplicateWorkflowTemplateInfo(
                        name, project, localePair, origWorkflowTemplateInfo,
                        false);
                WorkflowTemplate iflowTempDup = createIFlowDuplicate(name,
                        workflowTemplate, localePair,
                        origWorkflowTemplateInfo.getProjectManagerId(),
                        origWorkflowTemplateInfo.getWorkflowManagerIds(),
                        p_displayRoleName, false);
                dupWorkflowTemplateInfo.setWorkflowTemplate(iflowTempDup);
                session.save(dupWorkflowTemplateInfo);
            }
            tx.commit();
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = origWorkflowTemplateInfo.getName();
            tx.rollback();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_WFI, args,
                    e);
        }
    }

    /**
     * Import a workflow template info object based on the given new name
     * 
     * @param p_newName
     *            - The new name given to the workflow template info.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void importWorkflowTemplates(Document doc, String p_newName,
            Collection p_localePairs, String p_displayRoleName, String projectId)
            throws RemoteException, ProjectHandlerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        WorkflowTemplate workflowTemplate = new WorkflowTemplate();
        try
        {
            Iterator it = p_localePairs.iterator();
            // UnitOfWork uow = PersistenceService.getInstance()
            // .acquireUnitOfWork();

            while (it.hasNext())
            {
                LocalePair localePair = (LocalePair) it.next();
                List rolesNodes = doc
                        .selectNodes("/process-definition/task-node/task/assignment/roles");
                GlobalSightLocale source = localePair.getSource();
                GlobalSightLocale target = localePair.getTarget();
                Iterator iter = rolesNodes.iterator();

                while (iter.hasNext())
                {
                    Element rolesElement = (Element) iter.next();
                    String content = rolesElement.getText();
                    content = content.substring(0,
                            content.indexOf(source.toString())
                                    + source.toString().length());
                    rolesElement.setText(content + " " + target.toString());
                }

                List rolesNameNodes = doc
                        .selectNodes("/process-definition/task-node/task/assignment/role_name");
                Iterator ite = rolesNameNodes.iterator();

                while (ite.hasNext())
                {
                    Element element = (Element) ite.next();

                    element.setText(p_displayRoleName);
                }

                List rolesTypeNodes = doc
                        .selectNodes("/process-definition/task-node/task/assignment/role_type");
                ite = rolesTypeNodes.iterator();

                while (ite.hasNext())
                {
                    Element element = (Element) ite.next();
                    element.setText("false");
                }

                String name = generateAutoName(p_newName, localePair);
                c_category.info("The value of name is " + name);
                Attribute attribute = (Attribute) doc
                        .selectSingleNode("/process-definition/@name");
                attribute.setValue(name);
                workflowTemplate.setName(name);
                WorkflowTemplateInfo workflowTemplateInfo = createNewWorkflowTemplateInfo(
                        name, localePair, projectId);

                WorkflowTemplate jbpmTemp = ServerProxy.getWorkflowServer()
                        .importWorkflowTemplate(workflowTemplate, doc);

                workflowTemplateInfo.setWorkflowTemplate(jbpmTemp);
                // uow.registerObject(dupWorkflowTemplateInfo);
                session.save(workflowTemplateInfo);
            }
            // uow.commit();
            tx.commit();
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = workflowTemplate.getName();
            tx.rollback();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_WFI, args,
                    e);

        }
    }

    /**
     * @see ProjectHandler.replaceWorkflowTemplateInL10nProfile(long, long)
     */
    public L10nProfile replaceWorkflowTemplateInL10nProfile(long p_profileId,
            long p_workflowTemplateInfoId) throws RemoteException,
            ProjectHandlerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            L10nProfile profile = (L10nProfile) session.get(
                    BasicL10nProfile.class, new Long(p_profileId));
            WorkflowTemplateInfo template = (WorkflowTemplateInfo) session.get(
                    WorkflowTemplateInfo.class, new Long(
                            p_workflowTemplateInfoId));
            GlobalSightLocale sourceLocale = profile.getSourceLocale();
            profile.setSourceLocale(sourceLocale);
            GlobalSightLocale targetLocale = template.getTargetLocale();
            template.setSourceLocale(sourceLocale);
            template.setTargetLocale(targetLocale);

            // finally replace the workflow in the specified l10nProfile.
            profile.putWorkflowTemplateInfo(template);
            session.saveOrUpdate(template);
            session.saveOrUpdate(profile);
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            c_category.error("Failed to replace workflow "
                    + p_workflowTemplateInfoId + " in L10nProfile "
                    + p_profileId, e);
            String args[] =
            { Long.toString(p_profileId),
                    Long.toString(p_workflowTemplateInfoId) };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REPLACE_WFI_IN_PROFILE,
                    args, e);
        }

        return getL10nProfile(p_profileId);
    }

    /**
     * Duplicate a workflow template info object based on the given new name
     * (contains the workflow template designed via the graphical workflow UI).
     * 
     * @param p_newName
     *            - The new name given to the duplicated workflow template info.
     * @param p_wfTemplateInfo
     *            - The workflow template info to be duplicated.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, LocalePair p_localePair,
            WorkflowTemplate p_iflowTemplate, String p_displayRoleName)
            throws RemoteException, ProjectHandlerException
    {
        WorkflowTemplateInfo origWorkflowTemplateInfo = null;
        WorkflowTemplateInfo dupWorkflowTemplateInfo = null;
        try
        {
            origWorkflowTemplateInfo = getWorkflowTemplateInfoById(p_wfTemplateInfoId);
            WorkflowTemplate workflowTemplate = p_iflowTemplate;
            dupWorkflowTemplateInfo = createDuplicateWorkflowTemplateInfo(
                    p_newName, null, p_localePair, origWorkflowTemplateInfo,
                    false);
            WorkflowTemplate iflowDuplicate = createIFlowDuplicate(p_newName,
                    workflowTemplate, p_localePair,
                    origWorkflowTemplateInfo.getProjectManagerId(),
                    origWorkflowTemplateInfo.getWorkflowManagerIds(),
                    p_displayRoleName, false);
            dupWorkflowTemplateInfo.setWorkflowTemplate(iflowDuplicate);
            HibernateUtil.save(dupWorkflowTemplateInfo);
        }
        catch (Exception e)
        {
            c_category.error("The exception while duplicating a template is "
                    + e);
            String[] args = new String[1];
            args[0] = origWorkflowTemplateInfo.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_WFI, args,
                    e);

        }
        return dupWorkflowTemplateInfo;
    }

    /**
     * @see ProjectHandler.duplicateWorkflowTemplate(String, long,
     *      WorkflowTemplate)
     */
    public WorkflowTemplateInfo duplicateWorkflowTemplate(String p_newName,
            long p_wfTemplateInfoId, WorkflowTemplate p_iflowTemplate)
            throws RemoteException, ProjectHandlerException
    {

        WorkflowTemplateInfo origWorkflowTemplateInfo = null;
        WorkflowTemplateInfo dupWorkflowTemplateInfo = null;
        try
        {
            origWorkflowTemplateInfo = getWorkflowTemplateInfoById(p_wfTemplateInfoId);
            LocalePair lp = ServerProxy.getLocaleManager()
                    .getLocalePairBySourceTargetIds(
                            origWorkflowTemplateInfo.getSourceLocale().getId(),
                            origWorkflowTemplateInfo.getTargetLocale().getId());
            dupWorkflowTemplateInfo = createDuplicateWorkflowTemplateInfo(
                    p_newName, null, lp, origWorkflowTemplateInfo, true);
            WorkflowTemplate iflowDuplicate = createIFlowDuplicate(p_newName,
                    p_iflowTemplate, lp,
                    origWorkflowTemplateInfo.getProjectManagerId(),
                    origWorkflowTemplateInfo.getWorkflowManagerIds(), "", true);
            dupWorkflowTemplateInfo.setWorkflowTemplate(iflowDuplicate);
            HibernateUtil.save(dupWorkflowTemplateInfo);
        }
        catch (Exception e)
        {
            c_category.error("The exception while duplicating a template is "
                    + e);
            String[] args = new String[1];
            args[0] = origWorkflowTemplateInfo.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_WFI, args,
                    e);

        }
        return dupWorkflowTemplateInfo;
    }

    /**
     * Get a workflow template info object for the given id.
     * 
     * @param p_wfTemplateInfoId
     *            - The id of the workflow to be queried.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public WorkflowTemplateInfo getWorkflowTemplateInfoById(
            long p_wfTemplateInfoId) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            return getWfTemplateById(new Long(p_wfTemplateInfoId), true);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_wfTemplateInfoId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFI, args, e);
        }
    }

    /**
     * Get a list of all active workflow template infos.
     * 
     * @return A list of workflow template info objects.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public Collection<WorkflowTemplateInfo> getAllWorkflowTemplateInfos()
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            String hql = "from WorkflowTemplateInfo w where w.isActive = 'Y'";
            HashMap map = new HashMap();
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql = hql + " and w.companyId=:companyId";
                map.put("companyId", Long.parseLong(currentCompanyId));
            }
            List queryResult = HibernateUtil.search(hql, map);
            return queryResult;
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
    }

    /**
     * Modify a workflow template info object (contains the workflow template
     * designed via the graphical workflow UI).
     * 
     * @param p_workflowTemplateInfo
     *            - The workflow template info to be updated.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void modifyWorkflowTemplate(
            WorkflowTemplateInfo p_workflowTemplateInfo)
            throws RemoteException, ProjectHandlerException
    {
        // For GBS-1652: Deletes unselected leverage locale
        try
        {
            long wftiID = p_workflowTemplateInfo.getId();
            String locale = "";
            GlobalSightLocale lls;
            Set<GlobalSightLocale> nowLeverageLocales = p_workflowTemplateInfo
                    .getLeveragingLocales();
            for (Iterator it = nowLeverageLocales.iterator(); it.hasNext();)
            {
                lls = (GlobalSightLocale) it.next();
                locale = locale + String.valueOf(lls.getIdAsLong()) + ",";
            }

            if (locale.length() > 0)
            {
                locale = locale.trim().substring(0, locale.length() - 1);
                locale = " AND LOCALE_ID NOT IN (" + locale + ")";

                String sql = "DELETE FROM LEVERAGE_LOCALES WHERE WORKFLOW_INFO_ID = "
                        + wftiID;
                sql = sql + locale;
                HibernateUtil.executeSql(sql);
            }

        }
        catch (Exception e)
        {
            c_category
                    .error("Exception: There is error when modifyWorkflowTemplate");
        }// GBS-1652

        for (Iterator it2 = p_workflowTemplateInfo.getLeveragingLocalesSet()
                .iterator(); it2.hasNext();)
        {
            LeverageLocales leverageLocales = (LeverageLocales) it2.next();
            leverageLocales.setBackPointer(p_workflowTemplateInfo);
        }
        HibernateUtil.update(p_workflowTemplateInfo);
    }

    /**
     * Remove the workflow template info object based on the given id. The
     * remove process will check for dependencies first. If there are no
     * dependencies, it'll deactivate the workflow template info. Otherwise, an
     * exception will be thrown indicating the dependencies.
     * 
     * @param p_wfTemplateInfoId
     *            - The id of the workflow template info to be removed.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void removeWorkflowTemplate(long p_wfTemplateInfoId)
            throws RemoteException, ProjectHandlerException
    {
        WorkflowTemplateInfo wfti = getWorkflowTemplateInfoById(p_wfTemplateInfoId);
        try
        {
            wfti.deactivate();
            HibernateUtil.saveOrUpdate(wfti);
        }
        catch (Exception pe)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_WFI);
        }
    }

    /**
     * @see ProjectHandler.removeWorkflowTemplatesByLocalePair
     */
    public void removeWorkflowTemplatesByLocalePair(LocalePair p_localPair)
            throws RemoteException, ProjectHandlerException
    {
        // tbd - To be implemented....
        // check that no l10nprofile points to it
        try
        {
            Collection templates = getAllWorkflowTemplateInfosByLocalePair(p_localPair);
            HibernateUtil.delete(templates);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(1);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_PROFILE, args,
                    e);
        }
    }

    // ===========================private methods=======================

    private String generateAutoName(String p_name, LocalePair p_localePair)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(p_name);
        sb.append("_");
        sb.append(p_localePair.getSource().toString());
        sb.append("_");
        sb.append(p_localePair.getTarget().toString());
        String name = sb.toString();

        return name;
    }

    /*
     * This duplicates the CAP Workflow Template Info Object in memory. No
     * interaction with database
     */
    private WorkflowTemplateInfo createDuplicateWorkflowTemplateInfo(
            String p_newName, Project project, LocalePair p_localePair,
            WorkflowTemplateInfo p_origWorkflowTemplateInfo,
            boolean p_exactDuplicate)
    {
        WorkflowTemplateInfo workflowTemplateInfo = new WorkflowTemplateInfo();
        workflowTemplateInfo.setName(p_newName);
        workflowTemplateInfo
                .setCodeSet(p_origWorkflowTemplateInfo.getCodeSet());
        workflowTemplateInfo.setDescription(p_origWorkflowTemplateInfo
                .getDescription());
        workflowTemplateInfo.setCompanyId(Long.parseLong(CompanyThreadLocal
                .getInstance().getValue()));
        LeverageLocales leverageLocales = null;

        Set<LeverageLocales> lls = new HashSet<LeverageLocales>();
        if (!p_exactDuplicate) // just create one for the main target
        {
            leverageLocales = new LeverageLocales(p_localePair.getTarget());
            leverageLocales.setBackPointer(workflowTemplateInfo);
            lls.add(leverageLocales);
        }
        else
        // copy all of them
        {
            Set<LeverageLocales> origLls = p_origWorkflowTemplateInfo
                    .getLeveragingLocalesSet();
            for (Iterator it = origLls.iterator(); it.hasNext();)
            {
                LeverageLocales l = ((LeverageLocales) it.next())
                        .cloneForInsert();
                l.setBackPointer(workflowTemplateInfo);
                lls.add(l);
            }
        }
        workflowTemplateInfo.setLeveragingLocalesSet(lls);
        workflowTemplateInfo.setSourceLocale(p_localePair.getSource());
        workflowTemplateInfo.setTargetLocale(p_localePair.getTarget());
        if (project == null)
        {
            workflowTemplateInfo.setProject(p_origWorkflowTemplateInfo
                    .getProject());
            List<String> wfManagerIds = new ArrayList<String>();
            wfManagerIds.addAll(p_origWorkflowTemplateInfo
                    .getWorkflowManagerIds());
            workflowTemplateInfo.setWorkflowManagerIds(wfManagerIds);
        }
        else
        {
            workflowTemplateInfo.setProject(project);
        }
        workflowTemplateInfo.notifyProjectManager(p_origWorkflowTemplateInfo
                .notifyProjectManager());
        workflowTemplateInfo.setWorkflowType(p_origWorkflowTemplateInfo
                .getWorkflowType());
        workflowTemplateInfo.setScorecardShowType(p_origWorkflowTemplateInfo
                .getScorecardShowType());
        return workflowTemplateInfo;
    }

    private WorkflowTemplateInfo createNewWorkflowTemplateInfo(String name,
            LocalePair localePair, String projectId)
    {
        // Sets leverage locale.
        Set<LeverageLocales> leveragingLocales = new HashSet<LeverageLocales>();
        LeverageLocales leverageLocale = new LeverageLocales(
                localePair.getTarget());
        leveragingLocales.add(leverageLocale);

        // Sets workflowtemplateInfo.
        Project project;
        project = ProjectHandlerHelper.getProjectById(Long.valueOf(projectId));
        WorkflowTemplateInfo wfti = new WorkflowTemplateInfo(name, "", project,
                true, null, localePair.getSource(), localePair.getTarget(),
                "UTF-8", leveragingLocales);

        wfti.setWorkflowType(WorkflowTypeConstants.TYPE_TRANSLATION);

        // Sets company.
        wfti.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));

        leverageLocale.setBackPointer(wfti);

        return wfti;
    }

    private WorkflowTemplate createIFlowDuplicate(String p_newName,
            WorkflowTemplate p_workflowTemplate, LocalePair p_localePair,
            String p_projectManagerId, List p_workflowManagerIds,
            String p_displayRoleName, boolean p_exactDuplicate)
            throws ProjectHandlerException
    {
        WorkflowTemplate workflowTemplate = new WorkflowTemplate();
        WorkflowTemplate iflowTemplate = null;
        long NO_RATE = -1;
        try
        {

            Vector tasks = p_workflowTemplate.getWorkflowTasks();
            Iterator it = tasks.iterator();
            while (it.hasNext())
            {
                WorkflowTask workflowTask = (WorkflowTask) it.next();
                if (workflowTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    // not an exact copy
                    if (!p_exactDuplicate)
                    {
                        String activityName = workflowTask.getActivityName();
                        Activity activity = ServerProxy.getJobHandler()
                                .getActivityByCompanyId(
                                        activityName,
                                        String.valueOf(p_localePair
                                                .getCompanyId()));
                        String[] containerRole =
                        { getContainerRole(activity, p_localePair.getSource()
                                .toString(), p_localePair.getTarget()
                                .toString()) };
                        workflowTask.setRoles(containerRole);
                        workflowTask.setRoleType(false);
                        workflowTask.setRevenueRateId(NO_RATE);
                        workflowTask.setExpenseRateId(NO_RATE);

                        workflowTask.setDisplayRoleName(p_displayRoleName);
                    }
                    else
                    {
                        // don't make any changes to it
                    }
                }
                workflowTemplate.addWorkflowTask(workflowTask);
            }
            workflowTemplate.setName(p_newName);
            String desc = "";
            workflowTemplate.setDescription(desc);
            String[] wfManagerIds = new String[p_workflowManagerIds.size()];
            wfManagerIds = (String[]) p_workflowManagerIds
                    .toArray(wfManagerIds);
            WorkflowOwners workflowOwners = new WorkflowOwners(
                    p_projectManagerId, wfManagerIds);
            iflowTemplate = ServerProxy.getWorkflowServer()
                    .createWorkflowTemplate(workflowTemplate, workflowOwners);
        }
        catch (Exception e)
        {
            c_category
                    .error("Unable to duplicate i flow plan for original plan name "
                            + p_workflowTemplate.getName());
            c_category.error("The exception is " + e);
            String[] args = new String[1];
            args[0] = p_workflowTemplate.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_DUPLICATE_IFLOW_PLAN,
                    args, e);

        }
        return iflowTemplate;
    }

    private String getContainerRole(Activity p_activity, String p_sourceLocale,
            String p_targetLocale) throws Exception
    {
        String roleName = "";
        try
        {

            ContainerRole cr = ServerProxy.getUserManager().getContainerRole(
                    p_activity, p_sourceLocale, p_targetLocale);
            if (cr == null)
            {
                roleName = "";
            }
            else
            {
                roleName = cr.getName();
            }
        }
        catch (Exception e)
        {
            c_category
                    .error("Unable to retrieve container role from User Manager"
                            + e);
            String[] args = new String[1];
            args[0] = p_activity.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ACCESS_USERMANAGER,
                    args, e);

        }
        return roleName;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Workflow Template Info
    // ////////////////////////////////////////////////////////////////////

    // get a workflow template info object based on the given id. If
    // the p_editable parameter is true, the returned object is
    // editable. Otherwise it's just a read-only object.
    private WorkflowTemplateInfo getWfTemplateById(Long p_wfTemplateInfoId,
            boolean p_editable) throws Exception
    {
        return (WorkflowTemplateInfo) HibernateUtil.get(
                WorkflowTemplateInfo.class, p_wfTemplateInfoId);
    }

    /**
     * @see ProjectHandler.getAllWorkflowTemplateInfosbyParameters(long, long,
     *      long)
     */
    public Collection getAllWorkflowTemplateInfosByParameters(
            long p_sourceLocaleId, long p_targetLocaleId, long p_projectId)
            throws RemoteException, ProjectHandlerException
    {
        Collection workflowTemplateInfos = null;
        try
        {
            HashMap map = new HashMap();
            map.put(WorkflowTemplateInfoDescriptorModifier.SOURCE_LOCALE_ID,
                    new Long(p_sourceLocaleId));
            map.put(WorkflowTemplateInfoDescriptorModifier.TARGET_LOCALE_ID,
                    new Long(p_targetLocaleId));
            map.put(WorkflowTemplateInfoDescriptorModifier.PROJECT_ID,
                    new Long(p_projectId));
            String sql = WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_PARAMETERS_SQL;
            workflowTemplateInfos = HibernateUtil.searchWithSql(sql, map,
                    WorkflowTemplateInfo.class);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }

        return workflowTemplateInfos;
    }

    public Collection getAllWorkflowTemplateInfosByL10nProfileId(Job p_job)
            throws RemoteException, ProjectHandlerException
    {
        Collection result = null;
        long l10nProfileId = p_job.getL10nProfileId();
        try
        {
            HashMap map = new HashMap();
            map.put(WorkflowTemplateInfoDescriptorModifier.L10N_PROFILE_ID,
                    new Long(l10nProfileId));
            result = HibernateUtil
                    .searchWithSql(
                            WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_L10PROFILE_ID_SQL,
                            map, WorkflowTemplateInfo.class);
            // Remove the templates that are contained in the current workflow.
            HashMap existingWorkflows = getWorkflows(p_job);
            Iterator it2 = result.iterator();
            boolean isWorkflowDisplay = false;
            while (it2.hasNext())
            {
                WorkflowTemplateInfo template = (WorkflowTemplateInfo) it2
                        .next();
                isWorkflowDisplay = false;
                isWorkflowDisplay = hasExistingLocale(template,
                        existingWorkflows);
                if (isWorkflowDisplay == true)
                {
                    it2.remove();
                }
            }
            Iterator it3 = p_job.getWorkflowRequestList().iterator();
            // A job can have the workflow templates already added so
            // by the "add" workflow feature.Lets prune the list further.
            while (it3.hasNext())
            {
                WorkflowRequest wr = (WorkflowRequest) it3.next();
                Iterator it4 = wr.getWorkflowTemplateList().iterator();
                while (it4.hasNext())
                {
                    WorkflowTemplateInfo template = (WorkflowTemplateInfo) it4
                            .next();
                    removeWorkflowTemplates(result, template, existingWorkflows);
                }
            }
        }
        catch (Exception e)
        {
            c_category
                    .error("Unable to retrieve workflow templates for given source locale and project manager id "
                            + e);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
        return result;
    }

    private void removeWorkflowTemplates(Collection p_templates,
            WorkflowTemplateInfo p_addedWFT, HashMap p_existingWorkflows)
    {
        Iterator it = p_templates.iterator();
        while (it.hasNext())
        {
            WorkflowTemplateInfo availableWFT = (WorkflowTemplateInfo) it
                    .next();
            String addedTargetLocale = p_addedWFT.getTargetLocale().toString();
            Workflow existingWorkflow = (Workflow) p_existingWorkflows
                    .get(addedTargetLocale);
            if (existingWorkflow != null)
            {
                if (availableWFT.getTargetLocale().toString()
                        .equals(addedTargetLocale)
                        && !existingWorkflow.getState().equals(
                                Workflow.CANCELLED))
                {
                    it.remove();
                }
            }
        }
    }

    private boolean hasExistingLocale(WorkflowTemplateInfo p_template,
            HashMap p_currentWorkflows)
    {

        String templateTargetLocale = p_template.getTargetLocale().toString();
        Workflow workflow = (Workflow) p_currentWorkflows
                .get(templateTargetLocale);
        if (workflow != null)
        {
            if (templateTargetLocale.equals(workflow.getTargetLocale()
                    .toString())
                    && !workflow.getState().equals(Workflow.CANCELLED))
            {
                return true;
            }
        }
        return false;
    }

    private HashMap getWorkflows(Job p_job)
    {
        HashMap map = new HashMap();
        Iterator it = p_job.getWorkflows().iterator();
        // key : target locale string
        // value : workflow
        while (it.hasNext())
        {
            Workflow workflow = (Workflow) it.next();
            if (!Workflow.CANCELLED.equals(workflow.getState()))
            {
                map.put(workflow.getTargetLocale().toString(), workflow);
            }
        }
        return map;
    }

    /**
     * Return all workflow template infos with a particular source and target
     * locale.
     */
    public Collection getAllWorkflowTemplateInfosByLocalePair(
            LocalePair p_localPair) throws RemoteException,
            ProjectHandlerException
    {
        Collection workflowTemplateInfos = null;
        try
        {
            HashMap map = new HashMap();
            map.put(WorkflowTemplateInfoDescriptorModifier.SOURCE_LOCALE_ID,
                    p_localPair.getSource().getIdAsLong());
            map.put(WorkflowTemplateInfoDescriptorModifier.TARGET_LOCALE_ID,
                    p_localPair.getTarget().getIdAsLong());
            map.put(WorkflowTemplateInfoDescriptorModifier.COMPANY_ID,
                    new Long(p_localPair.getCompanyId()));
            workflowTemplateInfos = HibernateUtil
                    .searchWithSql(
                            WorkflowTemplateInfoDescriptorModifier.TEMPLATES_BY_LOCALE_PAIR_SQL,
                            map, WorkflowTemplateInfo.class);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
        return workflowTemplateInfos;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Translation Memory Profile
    // ////////////////////////////////////////////////////////////////////

    /**
     * Create a translation memory profile object (contains the translation
     * memory profile designed via the GUI).
     * 
     * @param p_tmProfile
     *            - The translation memory profile to be created.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void createTranslationMemoryProfile(
            TranslationMemoryProfile p_tmProfile) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            p_tmProfile
                    .setAllLeverageProjectTMs(p_tmProfile.getNewProjectTMs());
            HibernateUtil.save(p_tmProfile);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = p_tmProfile.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_CREATE_TMP, args, e);
        }
    }

    /**
     * Get a Tm Profile object based on the given id. If the p_editable
     * parameter is true, the returned object is editable. Otherwise it's just a
     * read-only object.
     */
    public TranslationMemoryProfile getTMProfileById(long p_tmProfileId,
            boolean p_editable) throws RemoteException, ProjectHandlerException
    {
        TranslationMemoryProfile tmProfile = null;

        try
        {
            tmProfile = (TranslationMemoryProfile) HibernateUtil.get(
                    TranslationMemoryProfile.class, new Long(p_tmProfileId));
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = new Long(p_tmProfileId).toString();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_TM_PROFILE_BY_ID,
                    args, e);
        }

        return tmProfile;
    }

    /**
     * Create a translation memory profile object (contains the translation
     * memory profile designed via the GUI).
     * 
     * @param p_tmProfile
     *            - The translation memory profile to be created.
     * 
     * @exception RemoteException
     *                System or network related exception.
     * @exception ProjectHandlerException
     *                Component specific exception
     */
    public void modifyTranslationMemoryProfile(
            TranslationMemoryProfile p_tmProfile) throws RemoteException,
            ProjectHandlerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            session.update(p_tmProfile);
            Vector newProjectTMs = p_tmProfile.getNewProjectTMs();
            if (newProjectTMs != null && newProjectTMs.size() != 0)
            {
                Iterator it2 = newProjectTMs.iterator();

                TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) session
                        .get(TranslationMemoryProfile.class, new Long(
                                p_tmProfile.getId()));
                Iterator it = tmProfile.getProjectTMsToLeverageFrom()
                        .iterator();
                while (it.hasNext())
                {
                    LeverageProjectTM levProjTM = (LeverageProjectTM) it.next();
                    session.delete(levProjTM);
                }

                Vector v = new Vector();
                while (it2.hasNext())
                {
                    LeverageProjectTM levProjTM = (LeverageProjectTM) it2
                            .next();
                    levProjTM.setTMProfile(tmProfile);
                    v.add(levProjTM);
                }
                tmProfile.setAllLeverageProjectTMs(v);

                session.saveOrUpdate(tmProfile);
            }

            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args = new String[1];
            args[0] = p_tmProfile.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_MODIFY_TMP, args, e);
        }
    }

    public Collection getAllTMProfiles() throws RemoteException,
            ProjectHandlerException
    {
        Collection tmProfiles = null;

        try
        {
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            tmProfiles = HibernateUtil.searchWithSql(ALL_TM_PROFILES_SQL, map,
                    TranslationMemoryProfile.class);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_TM_PROFILES,
                    null, e);
        }

        return tmProfiles;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Project TMs
    // ////////////////////////////////////////////////////////////////////
    public Collection<ProjectTM> getAllProjectTMs() throws RemoteException,
            ProjectHandlerException
    {
        return getAllProjectTMs(null);
    }

    public Collection<ProjectTM> getAllProjectTMs(String cond)
            throws RemoteException, ProjectHandlerException
    {
        Collection<ProjectTM> projectTMs = null;
        try
        {
            String hql = "from ProjectTM pt where 1=1";

            HashMap map = new HashMap();
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql += " and pt.companyId=:company_id and pt.status=:status";
                map.put("company_id", Long.parseLong(currentCompanyId));
                map.put("status", "");
            }
            if (!StringUtil.isEmpty(cond))
                hql += " and " + cond;
            projectTMs = (Collection<ProjectTM>) HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TMS,
                    null, e);
        }

        return projectTMs;
    }

    public Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin)
            throws RemoteException, ProjectHandlerException
    {
        return getAllProjectTMs(isSuperAdmin, null);
    }

    public Collection<ProjectTM> getAllProjectTMs(boolean isSuperAdmin,
            String cond) throws RemoteException, ProjectHandlerException
    {
        if (!isSuperAdmin)
            return getAllProjectTMs();

        Collection<ProjectTM> projectTMs = null;
        try
        {
            String hql = "from ProjectTM pt";
            if (!StringUtil.isEmpty(cond))
                hql += " where " + cond;
            projectTMs = (Collection<ProjectTM>) HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TMS,
                    null, e);
        }

        return projectTMs;
    }

    public void createProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    CompanyWrapper.getCurrentCompanyIdAsLong());
            if (company.getTmVersion().equals(TmVersion.TM3))
            {
                // We need to create the tm3 storage. Use the shared TM pool for
                // this company.
                TM3Manager mgr = DefaultManager.create();
                TM3Tm<GSTuvData> tm3tm = mgr.createMultilingualSharedTm(
                        new GSDataFactory(),
                        SegmentTmAttribute.inlineAttributes(), company.getId());
                p_projectTM.setTm3Id(tm3tm.getId());
            }

            HibernateUtil.save(p_projectTM);
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_CREATE_PROJECT_TM,
                    null, e);
        }
    }

    public ProjectTM getProjectTMById(long p_projectTMId, boolean p_editable)
            throws RemoteException, ProjectHandlerException
    {
        ProjectTM projectTM = null;

        try
        {
            projectTM = (ProjectTM) HibernateUtil.get(ProjectTM.class,
                    new Long(p_projectTMId));
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(p_projectTMId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TM_BY_ID,
                    args, e);
        }

        return projectTM;
    }

    /**
     * Get a ProjectTM by Name
     * 
     * @param p_projectTmName
     * @param p_editable
     * @return
     * @exception RemoteException
     * @exception ProjectHandlerException
     */
    public ProjectTM getProjectTMByName(String p_projectTmName,
            boolean p_editable) throws RemoteException, ProjectHandlerException
    {
        ProjectTM projectTM = null;

        try
        {
            String hql = "from ProjectTM p where p.name = :name ";

            HashMap map = new HashMap();
            map.put("name", p_projectTmName);

            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql += " and p.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentCompanyId));
            }

            projectTM = (ProjectTM) HibernateUtil.getFirst(hql, map);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(p_projectTmName);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TM_BY_ID,
                    args, e);
        }

        return projectTM;
    }

    /**
     * Retrieves a TM by tm3 id.
     * 
     * @param p_id
     *            TM id.
     * @return Tm object, or null if the TM does not exist.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public ProjectTM getProjectTMByTm3id(long p_tm3id) throws RemoteException,
            ProjectHandlerException
    {
        ProjectTM tm = null;
        try
        {
            String sql = " select * from project_tm " + " where TM3_ID = ? ";

            tm = (ProjectTM) HibernateUtil.getFirstWithSql(ProjectTM.class,
                    sql, new Long(p_tm3id));
        }
        catch (Exception pe)
        {
            String[] args =
            { String.valueOf(p_tm3id) };
            c_category.error(pe.getMessage(), pe);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TM_BY_ID,
                    args, pe);
        }

        if (tm == null)
        {
            c_category.error("getTmByTm3id queryResult empty: " + p_tm3id);
            String[] args =
            { String.valueOf(p_tm3id) };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_PROJECT_TM_BY_ID,
                    args, null);
        }

        return tm;
    }

    public void modifyProjectTM(ProjectTM p_projectTM) throws RemoteException,
            ProjectHandlerException
    {
        try
        {
            ProjectTM clone = getProjectTMById(p_projectTM.getId(), true);
            clone.setName(p_projectTM.getName());
            clone.setDomain(p_projectTM.getDomain());
            clone.setDescription(p_projectTM.getDescription());
            clone.setOrganization(p_projectTM.getOrganization());
            clone.setCreationDate(p_projectTM.getCreationDate());
            clone.setCompanyId(p_projectTM.getCompanyId());
            clone.setIndexTarget(p_projectTM.isIndexTarget());
            clone.setIsRemoteTm(p_projectTM.getIsRemoteTm());
            clone.setGsEditionId(p_projectTM.getGsEditionId());
            clone.setRemoteTmProfileId(p_projectTM.getRemoteTmProfileId());
            clone.setRemoteTmProfileName(p_projectTM.getRemoteTmProfileName());

            List<TMAttribute> tmAttributes = clone.getAllTMAttributes();
            if (tmAttributes != null && tmAttributes.size() > 0)
            {
                for (TMAttribute tmAttribute : tmAttributes)
                {
                    clone.getAttributes().remove(tmAttribute);
                    HibernateUtil.delete(tmAttribute);
                }
            }

            clone.setAttributes(p_projectTM.getAttributes());
            HibernateUtil.update(clone);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_projectTM.getId());

            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_MODIFY_PROJECT_TM,
                    args, e);
        }
    }

    public void removeProjectTm(long p_tmId) throws RemoteException,
            ProjectHandlerException
    {
        ProjectTM tm = getProjectTMById(p_tmId, true);

        try
        {
        	Set<TMAttribute> attrs = tm.getAttributes();
            HibernateUtil.delete(tm);
        	HibernateUtil.delete(tm.getAttributes());
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(p_tmId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_PROJECT_TM,
                    args, e);
        }
    }

    // return the UserManager remote interface.
    private UserManager lookupUserManager() throws ProjectHandlerException
    {
        try
        {
            return ServerProxy.getUserManager();
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_ACCESS_USERMANAGER,
                    null, e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: FileProfiles Template Info

    public Collection findFileProfileTemplates(
            FileProfileSearchParameters p_searchParameters)
            throws RemoteException, ProjectHandlerException
    {
        try
        {
            FileProfileSearchCriteria criteria = new FileProfileSearchCriteria();
            return criteria.search(p_searchParameters);
        }
        catch (Exception e)
        {
            c_category.error(
                    "Failed to get fileprofile templates by criteria.", e);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_FPIS, null, e);
        }

    }

    // ////////////////////////////////////////////////////////////////////
    // End: FileProfiles Template Info

    @Override
    public L10nProfileWFTemplateInfo getL10nProfileWfTemplateInfo(
            long profileId, long workflowId)
    {
        String hql = "from L10nProfileWFTemplateInfo l where l.key.l10nProfileId = :ProfileId and l.key.wfTemplateId = :TemplateId";
        HashMap map = new HashMap();
        map.put("ProfileId", profileId);
        map.put("TemplateId", workflowId);
        List list = HibernateUtil.search(hql, map);
        if (list.size() > 0)
        {
            return (L10nProfileWFTemplateInfo) list.get(0);
        }
        return null;
    }

    public void saveL10nProfileWfTemplateInfo(org.hibernate.Session session,
            L10nProfileWFTemplateInfo profileWFTemplateInfo)
    {
        try
        {
            if (profileWFTemplateInfo.getIsActive())
            {
                L10nProfileWFTemplateInfo lpi = new L10nProfileWFTemplateInfo();
                lpi.setIsActive(profileWFTemplateInfo.getIsActive());
                lpi.setKey(profileWFTemplateInfo.getKey());
                session.save(lpi);
            }
        }
        catch (Exception ex)
        {
            c_category.error("database error", ex);
            throw new LingManagerException(ex);
        }
    }

    /*
     * @Override public void saveL10nProfileWfTemplateInfo(
     * L10nProfileWFTemplateInfo profileWFTemplateInfo) { try {
     * L10nProfileWFTemplateInfo lpi = new L10nProfileWFTemplateInfo();
     * lpi.setIsActive(profileWFTemplateInfo.getIsActive());
     * lpi.setKey(profileWFTemplateInfo.getKey()); HibernateUtil.save(lpi); }
     * catch (Exception ex) { c_category.error("database error", ex); throw new
     * LingManagerException(ex); } }
     */
    public void updateL10nProfileWfTemplateInfo(org.hibernate.Session session,
            L10nProfileWFTemplateInfo profileWFTemplateInfo)
    {
        L10nProfileWFTemplateInfo lpi = new L10nProfileWFTemplateInfo();
        lpi.setIsActive(profileWFTemplateInfo.getIsActive());
        lpi.setKey(profileWFTemplateInfo.getKey());

        try
        {
            session.update(lpi);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
    }

    /*
     * public void updateL10nProfileWfTemplateInfo( L10nProfileWFTemplateInfo
     * profileWFTemplateInfo) { L10nProfileWFTemplateInfo lpi = new
     * L10nProfileWFTemplateInfo();
     * lpi.setIsActive(profileWFTemplateInfo.getIsActive());
     * lpi.setKey(profileWFTemplateInfo.getKey());
     * 
     * try { HibernateUtil.update(lpi); } catch (Exception e) {
     * e.printStackTrace(); throw new LingManagerException(e); } }
     */

    @Override
    public boolean isPrimaryKeyExist(long lnprofileId, long workflowId)
    {
        String hql = "from L10nProfileWFTemplateInfo l where l.key.l10nProfileId="
                + lnprofileId + " and l.key.wfTemplateId=" + workflowId;
        List<L10nProfileWFTemplateInfo> list = (List<L10nProfileWFTemplateInfo>) HibernateUtil
                .search(hql);
        return list.size() != 0;
    }

    public void removeTmProfile(TranslationMemoryProfile tmprofile)
            throws RemoteException, ProjectHandlerException
    {
        Connection connection = null;
        PreparedStatement query = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            String sql = "delete from tm_profile_project_tm_info where tm_profile_id = "
                    + tmprofile.getId();
            query = connection.prepareStatement(sql);
            query.execute();

            sql = "delete from l10n_profile_tm_profile where tm_profile_id = "
                    + tmprofile.getId();
            query = connection.prepareStatement(sql);
            query.execute();
            connection.commit();

            HibernateUtil.delete(tmprofile);
            
            LogManager.log(LogType.TMProfile, LogManager.EVENT_TYPE_REMOVE,
                    tmprofile.getId(), "Delete Translation Memory Profile ["
                            + tmprofile.getName() + "]",
                    tmprofile.getCompanyId());

        }
        catch (ConnectionPoolException cpe)
        {
            c_category.error("Fail to get connection: " + cpe.getMessage());
        }
        catch (SQLException sqle)
        {
            c_category.error("SQL error: " + sqle.getMessage());
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(tmprofile.getId());
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_TM_PROFILE,
                    args, e);
        }
        finally
        {
            if (query != null)
            {
                try
                {
                    query.close();
                }
                catch (Exception e)
                {
                    c_category.error("Closing query error", e);
                }
            }
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (Exception cpe)
            {
            }
        }
    }

    public void setResourceBundle(ResourceBundle p_resourceBundle)
    {
        this.p_resourceBundle = p_resourceBundle;
    }

    public HashSet getFileProfilesByProject(Project project)
            throws RemoteException, ProjectHandlerException
    {
        ArrayList coll = new ArrayList();
        coll.add(project);
        Collection lions = getL10nProfiles(coll);
        HashSet allProfileOfProject = new HashSet();

        Iterator ite = lions.iterator();

        while (ite.hasNext())
        {
            L10nProfile l10nProfile = (L10nProfile) ite.next();
            Set fileprofiles = l10nProfile.getFileProfiles();
            Iterator fps = fileprofiles.iterator();

            while (fps.hasNext())
            {
                FileProfileImpl fp = (FileProfileImpl) fps.next();
                if (!allProfileOfProject.contains(fp) && fp.isActive())
                {
                    allProfileOfProject.add(fp);
                }
            }
        }

        return allProfileOfProject;
    }

    /**
     * Get FileProfiles whose "Terminology Approval" is "Yes" for specified
     * project.
     * 
     * @param project
     *            -- Project object.
     */
    public ArrayList<FileProfile> fileProfileListTerminology(Project project)
            throws RemoteException, ProjectHandlerException
    {
        HashSet allProfileOfProject = getFileProfilesByProject(project);
        Iterator ite = allProfileOfProject.iterator();
        ArrayList al = new ArrayList();

        while (ite.hasNext())
        {
            FileProfileImpl fp = (FileProfileImpl) ite.next();

            if (fp.getTerminologyApproval() == 1)
            {
                al.add(fp);
            }
        }

        return al;
    }

    public List<ProjectImpl> getProjectsByTermbaseDepended(String termbaseName,
            long companyId)
    {
        String hql = "from ProjectImpl p where p.termbase='" + termbaseName
                + "' and p.companyId=" + companyId;
        List<ProjectImpl> list = (List<ProjectImpl>) HibernateUtil.search(hql);

        return list;
    }
    
    public List<WorkflowStatePosts> getAllWorkflowStatePostProfie(
            String[] filterParams)
    {
        try
        {
            List<WorkflowStatePosts> queryResult = new ArrayList<WorkflowStatePosts>();
            List<WorkflowStatePosts> qureyList = getAllWorkflowStatePostInfos();
            for (WorkflowStatePosts wfStatePost : qureyList)
            {
                if (wfStatePost.getName().toLowerCase().indexOf(filterParams[0].toLowerCase()) != -1
                        && wfStatePost.getListenerURL().toLowerCase()
                                .indexOf(filterParams[1].toLowerCase()) != -1
                       && CompanyWrapper.getCompanyNameById(
                                wfStatePost.getCompanyId()).toLowerCase().indexOf(
                                filterParams[2].toLowerCase()) != -1)
                {
                    queryResult.add(wfStatePost);
                }

            }
            return queryResult;
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
    }

    @Override
    public List<WorkflowStatePosts> getAllWorkflowStatePostInfos()
    {
        try
        {
            String hql = "from WorkflowStatePosts wfs where 1 = 1";
            HashMap map = new HashMap();
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                hql = hql + " and wfs.companyId=:companyId";
                map.put("companyId", Long.parseLong(currentCompanyId));
            }
            List queryResult = HibernateUtil.search(hql, map);
            return queryResult;
        }
        catch (Exception e)
        {
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFIS, null, e);
        }
    }

    @Override
    public void createWfStatePostProfile(WorkflowStatePosts wfStatePost)
    {
        try
        {
            HibernateUtil.save(wfStatePost);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = wfStatePost.getName();
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_CREATE_WFI, args, e);
        }
    }

    @Override
    public WorkflowStatePosts getWfStatePostProfile(long wfStatePostId)
    {
        try
        {
            return getWfStatePostProfile(new Long(wfStatePostId), true);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(wfStatePostId);
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_GET_WFI, args, e);
        }
    }

    private WorkflowStatePosts getWfStatePostProfile(Long wfStatePostId,
            boolean editable)
    {
        return (WorkflowStatePosts) HibernateUtil.get(WorkflowStatePosts.class,
                wfStatePostId);
    }

    @Override
    public void modifyWfStatePostProfile(WorkflowStatePosts wfstaPosts)
    {
        HibernateUtil.update(wfstaPosts);
    }

    @Override
    public void removeWorkflowStatePost(WorkflowStatePosts wfstaPosts)
    {
        try
        {
            HibernateUtil.delete(wfstaPosts);
        }
        catch (Exception pe)
        {
            c_category.error("Couldn't remove the WorkflowStatePosts", pe);
            String args[] =
            { Long.toString(wfstaPosts.getId()) };
            throw new ProjectHandlerException(
                    ProjectHandlerException.MSG_FAILED_TO_REMOVE_WF_STATE_POST_PROFILE,
                    args, pe);
        }
    }
    
}
