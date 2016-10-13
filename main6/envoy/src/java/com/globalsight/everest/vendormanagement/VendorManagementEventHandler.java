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

// globalsight
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Vendor
 */
public class VendorManagementEventHandler
{
    private static UserManager s_um = null;

    // for logging purposes
    private static final Logger c_logger = Logger
            .getLogger(VendorManagementEventHandler.class.getName());

    // contains the vendors that are in the process of being updated
    // this can be used by VendorManagement to check if a call to it
    // is recursive (and to ignore) or a brand new call.
    private static ArrayList m_vendorsUpdating = new ArrayList();

    VendorManagementEventHandler()
    {
    }

    public void dataUpdated(Vendor p_vendor, UpdatedDataEvent p_event)
            throws VendorException
    {
        try
        {
            // add to list - starting an event because the vendor's data changed
            synchronized (m_vendorsUpdating)
            {
                m_vendorsUpdating.add(p_vendor);
            }

            switch (p_event.getEventType())
            {
                case UpdatedDataEvent.CREATE_EVENT:
                case UpdatedDataEvent.UPDATE_EVENT:
                    createAndUpdateEvent(p_vendor, p_event);
                    break;

                case UpdatedDataEvent.DELETE_EVENT:
                    deleteEvent(p_vendor, p_event);
                    break;
                default:
                    // nothing
            }
        }
        finally
        {
            // done so remove from list
            synchronized (m_vendorsUpdating)
            {
                m_vendorsUpdating.remove(p_vendor);
            }
        }
    }

    // ================================= package methods
    // ===============================

    // only to be used by the VendorManagement package

    boolean vendorUpdating(Vendor p_vendor)
    {
        synchronized (m_vendorsUpdating)
        {
            if (m_vendorsUpdating.contains(p_vendor))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    // ================================= private methods
    // ============================================

    /**
     * Handle the create and update vendor event.
     */
    private void createAndUpdateEvent(Vendor p_vendor, UpdatedDataEvent p_event)
            throws VendorException
    {
        // If the status has changed to "APPROVED" and the flag
        // "useInAmbassador"
        // is set to true then call UserManager to create a new user.
        if (p_vendor.getStatus().equals(Vendor.APPROVED_STATUS)
                && p_vendor.useInAmbassador() && p_vendor.getUser() == null)
        {
            createNewUser(p_event.getInitiator(), p_vendor);
        }
        else if (p_vendor.getUser() != null)
        {
            if (p_vendor.useInAmbassador())
            {

                // now update the status of the user
                if (!p_vendor.getStatus().equals(Vendor.APPROVED_STATUS)
                        && p_vendor.getUser().isActive())
                {
                    // If the status changes from "APPROVED" to something else
                    // and an active user exists for this vendor then call
                    // UserManager
                    // to deactivate the user.
                    try
                    {
                        User u = getUserManager().deactivateUser(
                                p_vendor.getUserId());
                        // set the updated/deactivated user
                        p_vendor.setUser(u);
                    }
                    catch (Exception e)
                    {
                        c_logger.error("Failed to deactivate the user when vendor "
                                + UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId())
                                + " was not APPROVED.");
                        String args[] =
                        {
                                UserUtil.getUserNameById(p_vendor.getUserId()),
                                UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId()) };
                        throw new VendorException(
                                VendorException.MSG_FAILED_TO_DEACTIVATE_USER,
                                args, e);
                    }
                }
                // if approved but the user they are pointing to is not active.
                // activate the user
                else if (p_vendor.getStatus().equals(Vendor.APPROVED_STATUS)
                        && !p_vendor.getUser().isActive())
                {
                    try
                    {
                        List projectIds = new ArrayList();
                        for (Iterator pi = p_vendor.getProjects().iterator(); pi
                                .hasNext();)
                        {
                            Project p = (Project) pi.next();
                            projectIds.add(p.getIdAsLong());
                        }

                        User u = getUserManager().activateUser(
                                p_vendor.getUserId(), projectIds);
                        // set the updated/activated user
                        p_vendor.setUser(u);
                        // modify the user with the vendor's detail
                        modifyUser(p_event.getInitiator(), p_vendor);
                    }
                    catch (Exception e)
                    {
                        c_logger.error("Failed to activate the user when vendor "
                                + UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId())
                                + " was APPROVED.");
                        String args[] =
                        {
                                UserUtil.getUserNameById(p_vendor.getUserId()),
                                UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId()) };
                        throw new VendorException(
                                VendorException.MSG_FAILED_TO_ACTIVATE_USER,
                                args, e);
                    }
                }
                else
                {
                    // modify the user with the vendor's detail
                    modifyUser(p_event.getInitiator(), p_vendor);
                }
            }
            else
            // !p_vendor.useInAmbassador()
            {
                if (p_vendor.getUser().isActive())
                {
                    // If the user changes from "useInAmbassador" to not
                    // and an active user exists for this vendor then call
                    // UserManager
                    // to deactivate the user.
                    try
                    {
                        User u = getUserManager().deactivateUser(
                                p_vendor.getUserId());
                        // set the updated/deactivated user
                        p_vendor.setUser(u);

                    }
                    catch (Exception e)
                    {
                        c_logger.error("Failed to deactivate the user when vendor "
                                + UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId())
                                + " was changed to not be a GlobalSight user.");
                        String args[] =
                        {
                                UserUtil.getUserNameById(p_vendor.getUserId()),
                                UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId()) };
                        throw new VendorException(
                                VendorException.MSG_FAILED_TO_DEACTIVATE_USER,
                                args, e);
                    }
                }
            }
        }
    }

    /**
     * Handle the vendor deleted event.
     */
    private void deleteEvent(Vendor p_vendor, UpdatedDataEvent p_event)
            throws VendorException
    {
        // If a user has been created for this vendor call UserManager
        // to remove the user.
        if (p_vendor.getUser() != null)
        {
            try
            {
                getUserManager().deactivateUser(p_vendor.getUserId());
                // don't call "removeUser" - in case this user wants to be used
                // again
                // also if "removeUser" is called it will call VendorManagment
                // again
                // with its event handler
            }
            catch (Exception e)
            {
                c_logger.error(
                        "Failed to remove user "
                                + UserUtil.getUserNameById(p_vendor.getUserId())
                                + " that is associated with vendor "
                                + UserUtil.getUserNameById(p_vendor
                                        .getCustomVendorId())
                                + " that has been removed.", e);
                String args[] =
                { UserUtil.getUserNameById(p_vendor.getUserId()),
                        UserUtil.getUserNameById(p_vendor.getCustomVendorId()) };
                throw new VendorException(
                        VendorException.MSG_FAILED_TO_REMOVE_USER, args, e);
            }
        }
    }

    private void createNewUser(User p_creator, Vendor p_vendor)
            throws VendorException
    {
        User u = null;
        try
        {
            u = getUserManager().createUser();
            setNewUserValues(u, p_vendor);
            getUserManager().addUser(p_creator, u,
                    createUserProjects(u, p_vendor), null,
                    createUserRoles(u, p_vendor));

            // Create a user calendar based on the system calendar.
            // FluxCalendar baseCal =
            // ServerProxy.getCalendarManager().findDefaultCalendar();
            String companyId = CompanyWrapper.getCompanyIdByName(u
                    .getCompanyName());
            FluxCalendar baseCal = ServerProxy.getCalendarManager()
                    .findDefaultCalendar(companyId);
            UserFluxCalendar cal = new UserFluxCalendar(baseCal.getId(),
                    p_vendor.getUserId(), baseCal.getTimeZoneId());
            cal = CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);

            ServerProxy.getCalendarManager().createUserCalendar(cal,
                    p_creator.getUserId());

        }
        catch (Exception e)
        {
            c_logger.error("Failed to add a user.", e);
            String args[] =
            { UserUtil.getUserNameById(p_vendor.getUserId()),
                    UserUtil.getUserNameById(p_vendor.getCustomVendorId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_CREATE_USER, args, e);
        }

        // put outside the try/catch because this method throws the correct
        // exception
        // about the user not being associated with the vendor. at this point
        // the
        // vendor has been successfully created
        setUserInVendor(p_vendor, u);

    }

    private void modifyUser(User p_modifier, Vendor p_vendor)
            throws VendorException
    {
        try
        {
            setUserValues(p_vendor.getUser(), p_vendor);
            // the field security and access groups are set to NULL since they
            // shouldn't
            // be modified.
            getUserManager().modifyUser(p_modifier, p_vendor.getUser(),
                    createUserProjects(p_vendor.getUser(), p_vendor), null,
                    createUserRoles(p_vendor.getUser(), p_vendor));

        }
        catch (Exception e)
        {
            c_logger.error("Failed to modify a user.", e);
            String args[] =
            { UserUtil.getUserNameById(p_vendor.getUserId()),
                    UserUtil.getUserNameById(p_vendor.getCustomVendorId()) };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_MODIFY_USER, args, e);
        }
    }

    /**
     * Set all the user values from the vendor for modifying a user.
     */
    private void setUserValues(User p_modUser, Vendor p_vendor)
            throws Exception
    {
        p_modUser.setFirstName(p_vendor.getFirstName());
        p_modUser.setLastName(p_vendor.getLastName());
        p_modUser.setEmail(p_vendor.getEmail());
        p_modUser.setAddress(p_vendor.getAddress());
        p_modUser.setTitle(p_vendor.getTitle());
        p_modUser.setCompanyName(p_vendor.getCompanyName());
        p_modUser.setDefaultUILocale(p_vendor.getDefaultUILocale());

        String workNumber = p_vendor
                .getPhoneNumber(CommunicationInfo.CommunicationType.WORK);
        if (workNumber != null)
        {
            p_modUser.setOfficePhoneNumber(workNumber);
        }

        String homeNumber = p_vendor
                .getPhoneNumber(CommunicationInfo.CommunicationType.HOME);
        if (homeNumber != null)
        {
            p_modUser.setHomePhoneNumber(homeNumber);
        }

        String cellNumber = p_vendor
                .getPhoneNumber(CommunicationInfo.CommunicationType.CELL);
        if (cellNumber != null)
        {
            p_modUser.setCellPhoneNumber(cellNumber);
        }

        String faxNumber = p_vendor
                .getPhoneNumber(CommunicationInfo.CommunicationType.FAX);
        if (faxNumber != null)
        {
            p_modUser.setFaxPhoneNumber(faxNumber);
        }

    }

    /**
     * Create user roles from the vendors roles passed in.
     */
    private List createUserRoles(User p_user, Vendor p_vendor) throws Exception
    {
        List uRoles = new ArrayList();
        // loop through the vendor roles and create user roles.
        Set vendorRoles = p_vendor.getRoles();
        for (Iterator ri = vendorRoles.iterator(); ri.hasNext();)
        {
            VendorRole vr = (VendorRole) ri.next();

            LocalePair lp = vr.getLocalePair();
            Activity act = vr.getActivity();

            // Get a new UserRole from the factory, and fill out its fields
            // with the data passed.
            UserRole userRole = getUserManager().createUserRole();
            ((Role) userRole).setActivity(act);
            ((Role) userRole).setSourceLocale(lp.getSource().toString());
            ((Role) userRole).setTargetLocale(lp.getTarget().toString());
            userRole.setUser(p_user.getUserId());
            Rate rate = null;
            // set the rate if there is one
            if (vr.getRate() != null)
            {
                long rateId = vr.getRate().getId();
                userRole.setRate(Long.toString(rateId));
                rate = ServerProxy.getCostingEngine().getRate(rateId);
                ((Role) userRole).addRate(rate);
            }
            uRoles.add(userRole);
        }
        return uRoles;
    }

    /**
     * Set all the user values for a new user from the vendor.
     */
    void setNewUserValues(User p_modUser, Vendor p_vendor) throws Exception
    {
        p_modUser.setUserName(p_vendor.getUserId());
        p_modUser.setPassword(p_vendor.getPassword());

        setUserValues(p_modUser, p_vendor);
    }

    /*
     * Create the user project (ids) from the vendor's projects that are
     * specified.
     */
    private List createUserProjects(User p_user, Vendor p_vendor)
    {
        p_user.isInAllProjects(p_vendor.isInAllProjects());
        List projectIds = new ArrayList();
        for (Iterator pi = p_vendor.getProjects().iterator(); pi.hasNext();)
        {
            Project p = (Project) pi.next();
            projectIds.add(p.getIdAsLong());
        }

        return projectIds;
    }

    /**
     * Sets the user in the vendor.
     */
    private void setUserInVendor(Vendor p_vendor, User p_user)
            throws VendorException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Vendor cloneV = (Vendor) session.get(Vendor.class,
                    p_vendor.getIdAsLong());
            cloneV.setUser(p_user);
            session.update(cloneV);
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            c_logger.error("Failed to associated user " + p_user.getUserName()
                    + " with vendor " + p_vendor.getCustomVendorId(), e);
            String args[] =
            { p_user.getUserName(), p_vendor.getCustomVendorId() };
            throw new VendorException(
                    VendorException.MSG_FAILED_TO_ASSOCIATE_USER, args, e);
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
     * Get the user manager.
     */
    private UserManager getUserManager() throws VendorException
    {
        if (s_um == null)
        {
            try
            {
                s_um = ServerProxy.getUserManager();
            }
            catch (Exception e)
            {
                throw new VendorException(
                        VendorException.MSG_FAILED_TO_GET_USERMANAGER, null, e);
            }
        }
        return s_um;
    }
}
