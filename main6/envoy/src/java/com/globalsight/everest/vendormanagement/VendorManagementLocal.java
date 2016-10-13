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
package com.globalsight.everest.vendormanagement;

// globalsight imports
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorFieldSecurity;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class represents the remote implementation of a VendorManagement that
 * manages the objects needed for managing vendors.
 */
public class VendorManagementLocal implements VendorManagement
{
    // for logging purposes
    private static final Logger c_logger = Logger
            .getLogger(VendorManagementLocal.class.getName());

    private static boolean c_isInstalled = false;

    private VendorManagementEventHandler m_eventHandler = null;

    /**
     * Return 'true' or 'false' depending on if Vendor Management is installed
     * or not.
     */
    public static boolean isInstalled()
    {
        return c_isInstalled;
    }

    /**
     * Set up the installation key if set properly.
     */
    public VendorManagementLocal()
    {
        m_eventHandler = new VendorManagementEventHandler();
    }

    /**
     * @see VendorManagement.addVendor(User, Vendor, FieldSecurity)
     */
    public void addVendor(User p_userPerformingAdd, Vendor p_newVendor,
            FieldSecurity p_vendorFS) throws RemoteException, VendorException
    {

        checkIfInstalled();
        checkIfAuthorized(p_userPerformingAdd.getUserId(),
                Permission.VENDORS_NEW);
        try
        {
            if (containsRequiredFields(p_newVendor))
            {
                // save the resume file out (if one was uploaded
                try
                {
                    saveResumeFile(p_newVendor);
                }
                catch (Exception e)
                {
                    // keep going and save the vendor
                }

                HibernateUtil.saveOrUpdate(p_newVendor);

                try
                {
                    if (p_vendorFS == null)
                    {
                        // not set so create the default one
                        p_vendorFS = new VendorFieldSecurity();
                    }
                    // now save out the field security
                    ServerProxy.getSecurityManager().setFieldSecurity(
                            p_userPerformingAdd, p_newVendor, p_vendorFS);
                }
                catch (Exception e)
                {
                    // failed to save the field security so roll back the vendor
                    try
                    {
                        HibernateUtil.delete(p_newVendor);
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }

                    c_logger.error(
                            "Failed to create the new vendor "
                                    + p_newVendor.getFirstName() + " "
                                    + p_newVendor.getLastName() + " because "
                                    + " the field security couldn't be saved.",
                            e);
                    String args[] =
                    { p_newVendor.getFirstName() + " "
                            + p_newVendor.getLastName() };
                    throw new VendorException(
                            VendorException.MSG_FAILED_TO_ADD_VENDOR_SECURITY,
                            args, e);
                }

                m_eventHandler.dataUpdated(p_newVendor, new UpdatedDataEvent(
                        UpdatedDataEvent.CREATE_EVENT, p_userPerformingAdd));

                c_logger.info("Vendor " + p_newVendor.getFullName()
                        + " added by user" + p_userPerformingAdd.getUserName());
            }
            else
            {
                String[] args =
                { p_newVendor.getCustomVendorId() };
                throw new VendorException(
                        VendorException.MSG_REQUIRED_FIELDS_MISSING, args, null);
            }
        }
        catch (PersistenceException pe)
        {
            // TBD - check if unique vendor id is duplicated
            // return different error message
            c_logger.error("Failed to add a new vendor with name "
                    + p_newVendor.getFullName(), pe);
            String[] args =
            { p_newVendor.getFullName() };
            throw new VendorException(VendorException.MSG_FAILED_TO_ADD_VENDOR,
                    args, pe);
        }
    }

    /**
     * @see VendorManagement.findVendors(User, VendorSearchParameters)
     */
    public List findVendors(User p_userPerformingSearch,
            VendorSearchParameters p_searchParameters) throws RemoteException,
            VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userPerformingSearch.getUserId(),
                Permission.VENDORS_VIEW);
        List vendors = null;
        try
        {
            vendors = new VendorSearchCriteria().search(p_searchParameters);
        }
        catch (Exception e)
        {
            String[] args =
            {};
            throw new VendorException(VendorException.MSG_FAILED_TO_GET_VENDOR,
                    args, e);
        }

        List vendorInfos = new ArrayList(vendors.size());

        for (int i = 0; i < vendors.size(); i++)
        {
            Vendor vendor = (Vendor) vendors.get(i);
            VendorInfo vi = new VendorInfo(vendor.getId(),
                    vendor.getCustomVendorId(), vendor.getPseudonym(),
                    vendor.getFirstName(), vendor.getLastName(),
                    vendor.getCompanyName(), vendor.getUserId(),
                    vendor.getStatus());
            vendorInfos.add(vi);
        }

        return vendorInfos;
    }

    /**
     * @see VendorManagement.modifyVendor(User, Vendor, FieldSecurity)
     */
    public void modifyVendor(User p_userPerformingMod, Vendor p_modifiedVendor,
            FieldSecurity p_vendorFS) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userPerformingMod.getUserId(),
                Permission.VENDORS_EDIT);

        try
        {
            if (containsRequiredFields(p_modifiedVendor))
            {

                // save out the field security first
                try
                {
                    // only update if specified
                    if (p_vendorFS != null)
                    {
                        // now save out the field security
                        ServerProxy.getSecurityManager().setFieldSecurity(
                                p_userPerformingMod, p_modifiedVendor,
                                p_vendorFS);
                    }
                }
                catch (Exception e)
                {
                    // failed to save the field security so log this out
                    // and stop modification
                    c_logger.error("Failed to update the field security on "
                            + "the vendor " + p_modifiedVendor.getId(), e);
                    String args[] =
                    { Long.toString(p_modifiedVendor.getId()) };
                    throw new VendorException(
                            VendorException.MSG_FAILED_TO_MODIFY_VENDOR_SECURITY,
                            args, e);
                }

                // must use updateObject in order to take in to account the
                // resume
                HibernateUtil.update(p_modifiedVendor);

                m_eventHandler.dataUpdated(p_modifiedVendor,
                        new UpdatedDataEvent(UpdatedDataEvent.UPDATE_EVENT,
                                p_userPerformingMod));

                c_logger.info("Vendor " + p_modifiedVendor.getFullName()
                        + " modified by user "
                        + p_userPerformingMod.getUserName());
            }
            else
            {
                String[] args =
                { p_modifiedVendor.getCustomVendorId() };
                throw new VendorException(
                        VendorException.MSG_REQUIRED_FIELDS_MISSING, args, null);
            }
        }
        catch (PersistenceException pe)
        {
            c_logger.error(
                    "Failed to modify vendor. " + p_modifiedVendor.getId(), pe);
            String[] args =
            { Long.toString(p_modifiedVendor.getId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_MODIFY_VENDOR, args, pe);
        }
    }

    /**
     * @see VendorManagement.removeVendor(User, long)
     */
    public void removeVendor(User p_userPerformingRemoval, long p_id)
            throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userPerformingRemoval.getUserId(),
                Permission.VENDORS_REMOVE);

        Vendor vendor = getVendorById(p_id);
        removeVendor(p_userPerformingRemoval, vendor);

        c_logger.info("Vendor with id " + p_id + " removed by user "
                + p_userPerformingRemoval.getUserName());
    }

    /**
     * @see VendorManagement.removeVendor(User, String)
     */
    public void removeVendor(User p_userPerformingRemoval,
            String p_customVendorId) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userPerformingRemoval.getUserId(),
                Permission.VENDORS_REMOVE);

        Vendor vendor = getVendorByCustomId(p_customVendorId);
        removeVendor(p_userPerformingRemoval, vendor);

        c_logger.info("Vendor with custom vendor id " + p_customVendorId
                + " removed by user " + p_userPerformingRemoval.getUserName());
    }

    /**
     * @see VendorManagement.getVendors(User)
     */
    public List getVendors(User p_userQuerying) throws RemoteException,
            VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userQuerying.getUserId(), Permission.VENDORS_VIEW);

        String hql = "from Vendor v";
        List vendors = HibernateUtil.search(hql);
        List vendorInfos = new ArrayList(vendors.size());

        for (int i = 0; i < vendors.size(); i++)
        {
            Vendor vendor = (Vendor) vendors.get(i);
            VendorInfo vi = new VendorInfo(vendor.getId(),
                    vendor.getCustomVendorId(), vendor.getPseudonym(),
                    vendor.getFirstName(), vendor.getLastName(),
                    vendor.getCompanyName(), vendor.getUserId(),
                    vendor.getStatus());
            vendorInfos.add(vi);
        }

        return vendorInfos;
    }

    /**
     * @see VendorManagement.getVendorByCustomId(User, String)
     */
    public Vendor getVendorByCustomId(User p_userQuerying,
            String p_customVendorId) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userQuerying.getUserId(), Permission.VENDORS_VIEW);

        return getVendorByCustomId(p_customVendorId);
    }

    /**
     * @see VendorManagement.getVendorById(long)
     */
    public Vendor getVendorById(long p_id) throws RemoteException,
            VendorException
    {
        checkIfInstalled();

        try
        {
            Vendor v = (Vendor) HibernateUtil.get(Vendor.class, p_id);
            if (v != null)
            {
                setUpAmbassadorUser(v);
                setUpProjectManagers(v);
            }

            return v;
        }
        catch (Exception pe)
        {
            c_logger.error("Failed to get the vendor with id " + p_id, pe);
            String[] args =
            { Long.toString(p_id) };
            throw new VendorException(VendorException.MSG_FAILED_TO_GET_VENDOR,
                    args, pe);
        }
    }

    /**
     * @see VendorManagement.getVendorByUserId(String)
     */
    public Vendor getVendorByUserId(String p_userId) throws RemoteException,
            VendorException
    {
        checkIfInstalled();

        Vendor v = null;

        String hql = "from Vendor v where v.userId = :uId";
        Map map = new HashMap();
        map.put("uId", p_userId);
        List vendors = HibernateUtil.search(hql, map);
        if (vendors != null && vendors.size() > 0)
        {
            v = (Vendor) vendors.get(0);
            setUpAmbassadorUser(v);
            setUpProjectManagers(v);
        }

        return v;
    }

    /**
     * @see VendorManagement.getUserIdsOfVendors()
     */
    public List getUserIdsOfVendors() throws RemoteException, VendorException
    {
        checkIfInstalled();

        List userIds = new ArrayList();

        try
        {
            List vendors = HibernateUtil.search("from Vendor");
            for (int i = 0; i < vendors.size(); i++)
            {
                Vendor vendor = (Vendor) vendors.get(i);
                userIds.add(vendor.getUserId());
            }
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Failed to get the vendor user ids.", pe);
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_VENDOR_USER_IDS, null, pe);
        }

        return userIds;
    }

    /**
     * @see VendorManagement.getVendorStatusList()
     */
    public String[] getVendorStatusList() throws RemoteException,
            VendorException
    {
        checkIfInstalled();
        return new String[]
        { Vendor.PENDING_STATUS, Vendor.APPROVED_STATUS,
                Vendor.REJECTED_STATUS, Vendor.ON_HOLD_STATUS };
    }

    /**
     * @see VendorManagement.saveResumeFile(Vendor)
     */
    public void saveResumeFile(Vendor p_vendor) throws RemoteException,
            VendorException
    {
        String resumeFilename = p_vendor.getResumeFilename();

        if (resumeFilename != null && resumeFilename.length() > 0)
        {
            try
            {
                String content = new String(p_vendor.getResumeContentInBytes());
                ServerProxy.getNativeFileManager().save(content, null,
                        p_vendor.getResumePath());
            }
            catch (Exception e)
            {
                c_logger.error("Failed to save the resume for vendor "
                        + p_vendor.getCustomVendorId(), e);
                String args[] =
                { p_vendor.getCustomVendorId() };
                throw new VendorException(
                        VendorException.MSG_FAILED_TO_SAVE_RESUME, args, e);
            }
        }
    }

    /**
     * @see VendorManagement.getCompanyNames()
     */
    public ArrayList getCompanyNames() throws RemoteException, VendorException
    {
        checkIfInstalled();

        ArrayList cNames = new ArrayList();

        try
        {
            String sql = "select distinct(company) from vendor "
                    + "where company is not null";
            List names = HibernateUtil.searchWithSql(sql, null);
            cNames = new ArrayList(names);
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Failed to get the vendor company names.");
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_VENDOR_COMPANY_NAMES,
                    null, pe);
        }
        return cNames;
    }

    /**
     * @see VendorManagement.getPseudonyms
     */
    public ArrayList getPseudonyms() throws RemoteException, VendorException
    {
        checkIfInstalled();

        ArrayList pList = new ArrayList();

        try
        {
            String sql = "select pseudonym from vendor "
                    + "where pseudonym is not null";
            List pseudonyms = HibernateUtil.searchWithSql(sql, null);
            pList = new ArrayList(pseudonyms);
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Failed to get the vendor pseudonyms.");
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_VENDOR_PSEUDONYMS, null,
                    pe);
        }
        return pList;
    }

    /**
     * @see VendorManagement.getCustomVendorIds
     */
    public ArrayList getCustomVendorIds() throws RemoteException,
            VendorException
    {
        checkIfInstalled();

        ArrayList cviList = new ArrayList();

        try
        {
            String sql = "select custom_vendor_id from vendor";
            List pseudonyms = HibernateUtil.searchWithSql(sql, null);
            cviList = new ArrayList(pseudonyms);
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Failed to get the vendor custom ids.");
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_VENDOR_CUSTOM_IDS, null,
                    pe);
        }
        return cviList;
    }

    /**
     * @see VendorManagement.modifyVendorUserInfo(User
     */
    public void modifyVendorUserInfo(User p_modUser) throws RemoteException,
            VendorException
    {
        try
        {
            Vendor v = getVendorByUserId(p_modUser.getUserId());

            if (v != null && !m_eventHandler.vendorUpdating(v))
            {
                v.setFirstName(p_modUser.getFirstName());
                v.setLastName(p_modUser.getLastName());
                v.setEmail(p_modUser.getEmail());
                v.setAddress(p_modUser.getAddress());
                v.setTitle(p_modUser.getTitle());
                v.setCompanyName(p_modUser.getCompanyName());
                v.setDefaultUILocale(p_modUser.getDefaultUILocale());

                v.setPhoneNumber(CommunicationInfo.CommunicationType.WORK,
                        p_modUser.getOfficePhoneNumber());
                v.setPhoneNumber(CommunicationInfo.CommunicationType.HOME,
                        p_modUser.getHomePhoneNumber());
                v.setPhoneNumber(CommunicationInfo.CommunicationType.CELL,
                        p_modUser.getCellPhoneNumber());
                v.setPhoneNumber(CommunicationInfo.CommunicationType.FAX,
                        p_modUser.getFaxPhoneNumber());

                v.isInAllProjects(p_modUser.isInAllProjects());

                List projects = ServerProxy.getProjectHandler()
                        .getProjectsByUser(p_modUser.getUserId());

                v.setProjects(projects);

                // roles
                Collection userRoles = ServerProxy.getUserManager()
                        .getUserRoles(p_modUser);
                // create a new set to hold all the roles in - basically
                // deletes all the existing ones and adds the ones back in User
                // Roles
                // doesn't attempt to figure out the differences
                Set vendorRoles = new HashSet();
                for (Iterator ri = userRoles.iterator(); ri.hasNext();)
                {
                    UserRoleImpl r = (UserRoleImpl) ri.next();
                    Rate rate = null;

                    if (r.getRate() != null)
                    {
                        rate = ServerProxy.getCostingEngine().getRate(
                                Long.parseLong(r.getRate()));
                    }
                    Activity act = r.getActivity();
                    String srcString = r.getSourceLocale();
                    String trgString = r.getTargetLocale();
                    LocalePair lp = ServerProxy.getLocaleManager()
                            .getLocalePairBySourceTargetStrings(srcString,
                                    trgString);

                    VendorRole vr = new VendorRole(act, lp, rate);
                    vendorRoles.add(vr);
                }

                v.setRoles(vendorRoles);
                HibernateUtil.update(v);
            }
            else if (v == null)
            {
                // throw exception so no vendor to update
                c_logger.error("Failed to modify the vendor that is "
                        + "associated with updated user "
                        + p_modUser.getUserName()
                        + ". The vendor could not be found.");
                String args[] =
                { p_modUser.getUserName() };
                throw new VendorException(
                        VendorException.MSG_FAILED_TO_MODIFY_VENDOR_WITH_USERINFO,
                        args, null);

            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to modify the vendor that is associated "
                    + "with updated user " + p_modUser.getUserName(), e);
            String args[] =
            { p_modUser.getUserName() };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_MODIFY_VENDOR_WITH_USERINFO,
                    args, e);
        }
    }

    /**
     * @see VendorManagement.deassociateUserFromVendor(User)
     */
    public void deassociateUserFromVendor(User p_user) throws RemoteException,
            VendorException
    {
        try
        {
            Vendor v = getVendorByUserId(p_user.getUserId());
            if (v != null)
            {
                v.useInAmbassador(false);
                v.setUserId(null);
                v.setUser(null);
                HibernateUtil.update(v);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to deassociate the vendor with user "
                    + p_user.getUserName());
            String args[] =
            { p_user.getUserName() };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_DEASSOCIATE_USER_VENDOR,
                    args, e);
        }
    }

    /**
     * @see VendorManagement.addVendorsToProject(Project)
     */
    public void addVendorsToProject(Project p_proj) throws RemoteException,
            VendorException
    {
        // get all vendors that are marked to be in all projects
        try
        {

            String hql = "from Vendor v where v.isInAllProjects = 'Y'";
            Collection vendors = HibernateUtil.search(hql);
            if (vendors != null && vendors.size() > 0)
            {
                // Add the vendor to the project. Don't need to update user
                // this happens automatically when creating the project.
                for (Iterator vi = vendors.iterator(); vi.hasNext();)
                {
                    Vendor v = (Vendor) vi.next();
                    v.addToProject(p_proj);
                    HibernateUtil.update(v);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("The project was added, however it failed to "
                    + "add the vendors to the new project" + p_proj.getName(),
                    e);
            String errorArgs[] =
            { p_proj.getName() };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_ADD_VENDORS_TO_PROJECT,
                    errorArgs, e);
        }
    }

    /**
     * @see VendorManagement.removeVendorsFromProject(Project)
     */
    public void removeVendorsFromProject(Project p_proj)
            throws RemoteException, VendorException
    {
        // get all vendors that are in the specific project
        try
        {

            String sql = "select * from vendor where id in "
                    + "(select vendor_id from project_vendor "
                    + "where project_id = :pId";
            Map map = new HashMap();
            map.put("pId", p_proj.getIdAsLong());
            List vendors = HibernateUtil.searchWithSql(sql, map, Vendor.class);
            if (vendors != null && vendors.size() > 0)
            {
                // Remove the vendor from the project. Don't need to update
                // user this happens automatically when removing the project.
                for (Iterator vi = vendors.iterator(); vi.hasNext();)
                {
                    Vendor v = (Vendor) vi.next();
                    ProjectImpl project = (ProjectImpl) HibernateUtil.get(
                            ProjectImpl.class, p_proj.getId());
                    v.removeFromProject(project);
                    HibernateUtil.update(v);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to remove the vendors from project "
                    + p_proj.getName(), e);
            String args[] =
            { p_proj.getName() };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_REMOVE_VENDORS_FROM_PROJECT,
                    args, e);
        }
    }

    //
    // Rating Methods
    //
    /**
     * @see VendorManagement.addRating(String, Vendor, Rating)
     */
    public void addRating(User p_userAddingRate, Vendor p_vendor,
            Rating p_rating) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userAddingRate.getUserId(),
                Permission.VENDORS_RATING_NEW);

        try
        {
            Vendor clonedVendor = (Vendor) HibernateUtil.get(Vendor.class,
                    p_vendor.getId());
            clonedVendor.addRating(p_rating);

            HibernateUtil.update(clonedVendor);
        }
        catch (Exception e)
        {
            String args[] =
            { p_userAddingRate.getUserName(), p_vendor.getFullName() };
            throw new VendorException(VendorException.MSG_FAILED_TO_ADD_RATING,
                    args, e);
        }
    }

    /**
     * @see VendorManagement.getRatingById(String, long)
     */
    public Rating getRatingById(User p_userQueryingRate, long p_ratingId)
            throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userQueryingRate.getUserId(),
                Permission.VENDORS_RATING_VIEW);

        return getRatingById(p_ratingId);
    }

    /**
     * @see VendorManagement.getRatingsInTasks(String, long[])
     */
    public HashMap getRatingsInTasks(User p_userQueryingRate, long[] p_taskIds)
            throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userQueryingRate.getUserId(),
                Permission.VENDORS_RATING_VIEW);

        try
        {
            String sql = ratingsByTaskIds(p_taskIds);
            List col = HibernateUtil.searchWithSql(Rating.class, sql);

            HashMap hm = null;
            if (col.size() > 0)
            {
                hm = new HashMap();
                Iterator it = col.iterator();
                while (it.hasNext())
                {
                    Rating rating = (Rating) it.next();
                    Long taskId = new Long(rating.getTask().getId());
                    ArrayList ratings = (ArrayList) hm.get(taskId);

                    if (ratings == null)
                    {
                        ArrayList l = new ArrayList();
                        l.add(rating);
                        hm.put(taskId, l);
                    }
                    else
                    {
                        ratings.add(rating);
                    }
                }
            }

            return hm;
        }
        catch (Exception e)
        {
            String args[] =
            { p_userQueryingRate.getUserName() };

            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_RATINGS_BY_TASK_IDS,
                    args, e);
        }
    }

    /**
     * @see VendorManagement.removeRating(String, Vendor, long)
     */
    public void removeRating(User p_userRemovingRate, Vendor p_vendor,
            long p_ratingId) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userRemovingRate.getUserId(),
                Permission.VENDORS_RATING_REMOVE);

        Rating rating = getRatingById(p_ratingId);

        if (rating != null)
        {
            Task t = rating.getTask();
            // if associated with a task remove from the task too.
            if (t != null)
            {
                t.removeRating(rating);
                HibernateUtil.update(t);
            }

            try
            {
                HibernateUtil.delete(rating);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
                throw new RemoteException(e.getMessage());
            }
        }
    }

    /**
     * @see VendorManagement.updateRating(String, Vendor, Rating)
     */
    public void updateRating(User p_userUpdatingRate, Vendor p_vendor,
            Rating p_rating) throws RemoteException, VendorException
    {
        checkIfInstalled();
        checkIfAuthorized(p_userUpdatingRate.getUserId(),
                Permission.VENDORS_RATING_EDIT);
        try
        {

            Vendor clonedVendor = (Vendor) HibernateUtil.get(Vendor.class,
                    p_vendor.getId());
            List ratings = clonedVendor.getRatings();
            Rating clonedRating = (Rating) ratings.get(ratings
                    .indexOf(p_rating));
            clonedRating.updateRating(p_rating.getValue(),
                    p_rating.getComment(), p_userUpdatingRate.getUserId());

            HibernateUtil.update(clonedRating);
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_rating.getId()), p_vendor.getFullName() };

            throw new VendorException(
                    VendorException.MSG_FAILED_TO_UPDATE_RATING, args, e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Custom Design Form Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * @see VendorManagement.removeCustomForm()
     */
    public void removeCustomForm() throws RemoteException, VendorException
    {
        try
        {
            CustomForm customForm = getCustomForm();
            HibernateUtil.delete(customForm);
        }
        catch (Exception e)
        {
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_REMOVE_CUSTOM_FORM, null, e);
        }
    }

    /**
     * @see VendorManagement.getCustomForm()
     */
    public CustomForm getCustomForm() throws RemoteException, VendorException
    {
        Collection col = HibernateUtil.search("from CustomForm");
        return (col == null || col.size() == 0) ? null : (CustomForm) col
                .iterator().next();
    }

    /**
     * @see VendorManagement.updateCustomForm(CustomForm, List)
     */
    public CustomForm updateCustomForm(CustomForm p_customForm,
            List p_removedFields) throws RemoteException, VendorException
    {
        try
        {
            if (p_customForm == null)
            {
                return null;
            }
            else if (p_customForm.getId() > 0)
            {
                return updateForm(p_customForm, p_removedFields);
            }
            else
            {
                return createForm(p_customForm);
            }
        }
        catch (Exception e)
        {
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_UPDATE_CUSTOM_FORM, null, e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Custom Design Form Methods
    // ////////////////////////////////////////////////////////////////////

    // =================================private
    // methods==========================================

    private Vendor getVendorByCustomId(String p_customVendorId)
            throws VendorException
    {
        Vendor v = null;

        String hql = "from Vendor v where v.customVendorId = :cId";
        Map map = new HashMap();
        map.put("cId", p_customVendorId);
        List vendors = HibernateUtil.search(hql, map);
        if (vendors != null && vendors.size() > 0)
        {
            v = (Vendor) vendors.get(0);
            setUpAmbassadorUser(v);
            setUpProjectManagers(v);
        }

        return v;
    }

    private void removeVendor(User p_userPerformingRemoval, Vendor p_vendor)
            throws VendorException
    {
        try
        {
            // tbd - check the dependencies on vendor and projects before
            // removing
            if (p_vendor != null)
            {
                // remove the field security
                ServerProxy.getSecurityManager().removeFieldSecurity(
                        p_userPerformingRemoval, p_vendor);
                HibernateUtil.delete(p_vendor);
            }

            // tbd - will the vendor object still be valid?
            m_eventHandler.dataUpdated(p_vendor, new UpdatedDataEvent(
                    UpdatedDataEvent.DELETE_EVENT, p_userPerformingRemoval));
        }
        catch (Exception pe)
        {
            c_logger.error("Failed to delete vendor " + p_vendor.getId(), pe);
            String[] args =
            { Long.toString(p_vendor.getId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_REMOVE_VENDOR, args, pe);

        }
    }

    private void checkIfInstalled() throws VendorException
    {
        if (!isInstalled())
            throw new VendorException(VendorException.MSG_NOT_INSTALLED, null,
                    null);
    }

    /**
     * Checks if the given user has the given permission
     * 
     * @param p_username
     *            username
     * @param p_permission
     *            permission (@see Permission)
     * @exception VendorException
     */
    private void checkIfAuthorized(String p_username, String p_permission)
            throws VendorException
    {
        try
        {
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_username);
            if (ps.getPermissionFor(p_permission) == false)
            {
                String args[] =
                { p_username };
                throw new VendorException(VendorException.MSG_NOT_AUTHORIZED,
                        args, null);
            }
        }
        catch (VendorException ve)
        {
            throw ve;
        }
        catch (Exception e)
        {
            c_logger.error("Failed to find permission set for user "
                    + p_username, e);
            String args[] =
            { p_username };
            throw new VendorException(VendorException.MSG_NOT_AUTHORIZED, args,
                    null);
        }
    }

    private void setUpAmbassadorUser(Vendor p_vendor) throws VendorException
    {
        try
        {
            // if user id is set then populate the user pointer
            if (p_vendor.getUserId() != null
                    && p_vendor.getUserId().length() > 0)
            {
                User u = ServerProxy.getUserManager().getUser(
                        p_vendor.getUserId());
                p_vendor.setUser(u);
            }
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Failed to get the user "
                            + UserUtil.getUserNameById(p_vendor.getUserId())
                            + " that vendor "
                            + UserUtil.getUserNameById(p_vendor
                                    .getCustomVendorId())
                            + " is associated with.", e);
            String args[] =
            { UserUtil.getUserNameById(p_vendor.getUserId()),
                    UserUtil.getUserNameById(p_vendor.getCustomVendorId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_ASSOCIATE_USER, args, e);
        }
    }

    /**
     * Vendors contain projects which contain project managers. Projects are
     * retrieved through TOPLink - however the PM won't be instantiated since it
     * is in LDAP. So fill in all PM's in the projects this vendor is associated
     * with.
     */
    private void setUpProjectManagers(Vendor p_vendor) throws VendorException
    {
        try
        {
            for (Iterator i = p_vendor.getProjects().iterator(); i.hasNext();)
            {
                Project p = (Project) i.next();
                if (p.getProjectManager() == null)
                {
                    User u = ServerProxy.getUserManager().getUser(
                            p.getProjectManagerId());
                    p.setProjectManager(u);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to set up the project manager for a "
                    + "project that vendor " + p_vendor.getId()
                    + " is associated with.", e);
            String args[] =
            { UserUtil.getUserNameById(p_vendor.getUserId()),
                    UserUtil.getUserNameById(p_vendor.getCustomVendorId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_ASSOCIATE_USER, args, e);
        }
    }

    /**
     * Returns true if all required fields are specified.
     */
    private boolean containsRequiredFields(Vendor p_vendor)
    {
        // generate certain required fields if not specified
        if (p_vendor.getCustomVendorId() == null
                || p_vendor.getCustomVendorId().length() == 0)
        {
            // generate customer vendor id
            StringBuffer vendorId = new StringBuffer("VENDOR_");
            vendorId.append(Calendar.getInstance().getTimeInMillis());
            p_vendor.setCustomVendorId(vendorId.toString());
        }
        if (p_vendor.getPseudonym() == null
                || p_vendor.getPseudonym().length() == 0)
        {
            // if not specified use the custom vendor id as the alias
            p_vendor.setPseudonym(p_vendor.getCustomVendorId());
        }

        // check for other required fields
        if (p_vendor.getFirstName() == null
                || p_vendor.getFirstName().length() == 0)
        {
            return false;
        }
        if (p_vendor.getLastName() == null
                || p_vendor.getLastName().length() == 0)
        {
            return false;
        }
        // if approved certain fields must be specified
        if (p_vendor.getStatus().equals(Vendor.APPROVED_STATUS)
                && p_vendor.useInAmbassador())
        {
            // if neither of these specified
            if (p_vendor.getUserId() == null && p_vendor.getUser() == null)
            {
                return false;
            }
        }
        return true;
    }

    private Rating getRatingById(long p_ratingId) throws VendorException
    {
        try
        {
            return (Rating) HibernateUtil.get(Rating.class, p_ratingId);
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
            String args[] =
            { String.valueOf(p_ratingId) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_GET_RATING_BY_ID, args, e);
        }
    }

    /**
     * Create an sql statement to query a list of ratings for the given task
     * ids.
     */
    private String ratingsByTaskIds(long[] p_taskIds)
    {
        int size = p_taskIds.length;

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM VENDOR_RATING ");
        sb.append(" WHERE ");
        sb.append("TASK_ID in (");
        for (int i = 0; i < size; i++)
        {
            sb.append(p_taskIds[i]);
            if (i < size - 1)
            {
                sb.append(", ");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Update the custom form with the newly specified one.
     */
    private CustomForm updateForm(CustomForm p_customForm, List p_removedFields)
            throws Exception
    {
        HibernateUtil.update(p_customForm);

        if (p_removedFields != null && p_removedFields.size() > 0)
        {
            removeCustomFields(p_removedFields);
        }

        return getCustomForm();
    }

    /**
     * Add the newly created form.
     */
    private CustomForm createForm(CustomForm p_customForm) throws Exception
    {
        HibernateUtil.save(p_customForm);
        return getCustomForm();
    }

    /**
     * Remove the custom fields specified from the vendor's that they are
     * associated with. These fields are no longer being used.
     */
    private void removeCustomFields(List p_customFields)
    {
        // query for all vendors who have one or more of these custom fields
        // filled out
        /***********************************************************************
         * System.out.println("+++Going to remove fields " +
         * p_customFields.toString());
         * 
         * Vector args = new Vector(p_customFields); Collection c =
         * PersistenceService.getInstance().executeNamedQuery(
         * VendorQueryNames.VENDORS_WITH_CUSTOM_FIELDS, args, false);
         * 
         * if (c.size() > 0) { UnitOfWork uow =
         * PersistenceService.getInstance(). acquireUnitOfWork(); // if there
         * are some then register then and remove the // fields from the custom
         * field list. for (Iterator ci = c.iterator() ; ci.hasNext() ; ) {
         * Vendor v = (Vendor)ci.next(); } uow.commit(); }
         **********************************************************************/

    }
}
