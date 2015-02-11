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
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;

// java
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


/** 
 * VendorManagement is a new module that will manage all functionality 
 * specific to vendors.
 */
public interface VendorManagement
{
    public static final String SERVICE_NAME = "VendorManagement";
    public static final String VENDOR_RESUME_STORAGE = "GlobalSight" +
                                                        File.separator + "VendorResume";

    /**
     * Add the specified vendor.
     * If the status of the vendor is "APPROVED" and the flag "useInAmbassador" 
     * is set to true then call UserManager a message to create a user also.
     * 
     * @param p_userPerformingAdd     The user requesting to add a vendor.
     * @param p_newVendor             The new vendor to be added.
     * @param p_vendorFS              The new vendor's field security to be added.
     *                                If set to NULL then the default 
     *                                (all shared) will be set.
     */
    void addVendor(User p_userPerformingAdd, Vendor p_newVendor,
                   FieldSecurity p_vendorFS)
        throws RemoteException, VendorException;

    /**
     * Find vendors based on the specified search criteria parameters.
     *
     * @param p_idOfUserPerformingSearch - The user id of the person 
     *        performing the vendor search.
     * @param p_searchParameters - The vendor search parameter object with
     *        the set parameters.
     * @return A list of vendors that satisfy the provided search criteria.
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    List findVendors(User p_userPerformingSearch, 
                     VendorSearchParameters p_searchParameters)
        throws RemoteException, VendorException;

    /**
     * Modify the vendor passed in.  The vendor passed in contains all the necessary changes.
     * If the status has changed to 'APPROVED' and the flag useInAmbassador='true' then call
     * UserManager to create a new user.
     * If the status changes from 'APPROVED' to something else and a user exists for this vendor
     * then call UserManager to deactivate thhe user.
     * If a user exists for this vendor call UserManager to modify the user, since some of
     * the information stored in Vendor is also in thhe User object.
     *
     * @param p_userPerformingMod      The user requesting to modify the vendor.
     * @param p_modifiedVendor         The vendor will all modifications.
     * @param p_vendorFS               The vendor's updated Field Security.
     *                                 If set to NULL the field security will
     *                                 not be updated.
     */
    void modifyVendor(User p_userPerformingMod, Vendor p_modifiedVendor,
                      FieldSecurity p_vendorFS)
        throws RemoteException, VendorException;

    /**
     * Remove the vendor that is associated with the id.  
     * If a user has been created for this vendor call UserManager 
     * to remove the user.
     *
     * @param p_userPerformingRemoval   The user requesting to remove the vendor.
     * @param p_vendorId                The id of the vendor to remove
     */
    void removeVendor(User p_userPerformingRemoval, long p_vendorId)
        throws RemoteException, VendorException;

    /**
     * Remove the vendor that is associated with the specified custom vendor id.
     * If a user has been created for this vendor call UserManager to remove the user.
     *
     * @param p_userPerformingRemoval The user requesting to remove the vneodr.
     * @param p_customVendorId        The custom vendor id of the vendor to remove.
     */
    void removeVendor(User p_userPerformingRemoval, String p_customVendorId)
        throws RemoteException, VendorException;

    /**
     * Return a list of all vendors.  Verify that the user specified
     * has access to vendors.
     *
     * @param p_userQuerying  The user requesting all vendors.
     */
    List getVendors(User p_userQuerying)
        throws RemoteException, VendorException;

    /**
     * Return the vendor that is associated with the custom vendor id specified.
     * 
     * @param p_userQuering     There user querying for the vendor.
     * @param p_customVendorId  The custom vendor id of the vendor querying for.
     */
    Vendor getVendorByCustomId(User p_userQuerying,
                               String p_customVendorId)
        throws RemoteException, VendorException;

    /** 
     * Return the vendor that is associated with the vendor id specified.
     *
     * @param p_vendorId    The id of the vendor querying for.
     */
    Vendor getVendorById(long p_vendorId)
        throws RemoteException, VendorException;

    /**
     * Returns the vendor that is associated with the user id specified. 
     */
    Vendor getVendorByUserId(String p_userId)
        throws RemoteException, VendorException;

    /**
     * Returns a list of Strings - they are the User Ids the vendors
     * are associated with.
     */
    List getUserIdsOfVendors()
        throws RemoteException, VendorException;

    /**
     * Returns the list of possible statuses a Vendor's application can be in.
     */
    String[] getVendorStatusList()
        throws RemoteException, VendorException;


    /**
     * Saves out the resume file to the file directory.
     * This must be set up properly in the vendor object.
     */
    void saveResumeFile(Vendor p_vendor)
        throws RemoteException, VendorException;


    /**
     * Returns all the distinct company names that the vendors are associated with.
     */
    ArrayList getCompanyNames()
        throws RemoteException, VendorException;

    /**
     * Returns a list of all the pseudonym's used by vendors.
     */
    ArrayList getPseudonyms()
        throws RemoteException, VendorException;

    /**
     * Returns a list of all the custom vendors ids.
     */
    ArrayList getCustomVendorIds()
        throws RemoteException, VendorException;

    /** 
     * Adds all vendors marked to be added to all projects to the project
     * specified.  This happens when a new project is created.
     */
    void addVendorsToProject(Project p_proj)
        throws RemoteException, VendorException;

    /**
     * Remove all vendors marked to b @see VendorManagement.removeVendorsFromProject(Project)
     */
    void removeVendorsFromProject(Project p_proj)
        throws RemoteException, VendorException;

    // ------------------------------------------------------------------
    //    methods that have to do with a User that the Vendor is
    //   associated with.
    // -----------------------------------------------------------------

    /**
     * Modify the vendor according to the user and their information
     * passed in.
     *
     * @param p_modifiedUser       The user that was modified.  This user is
     *                             also a vendor so the shared/duplicated
     *                             information should be modified too.
     *                              
     */
    void modifyVendorUserInfo(User p_modifiedUser)
       throws RemoteException, VendorException;

    /**
     *  Remove the association between the user and vendor.
     *  This means removing the user information from the vendor
     *  and changing the "useInAmbassador" flag to false. 
     */
    void deassociateUserFromVendor(User p_user)
        throws RemoteException, VendorException;

    // -------------------------------------------------------------------
    //  Methods for custom form and fields.
    // -------------------------------------------------------------------

    /**
     * Retrieves the vendor management custom form.
     * This form contains any customized fields for vendor information.
     */
    public CustomForm getCustomForm()
        throws RemoteException, VendorException;

    /**
     * Add or update the vendor management custom form.
     * If any customized fields were removed from the form they
     * will also be removed from each of the vendors.
     *
     * @param p_customForm    The form to add or updated
     * @param p_removedFields The fields that have been removed from the form.
     *                        Can be NULL or empty if no removed fields or
     *                        if the form is new.
     */
    public CustomForm updateCustomForm(CustomForm p_customForm,
                                       List p_removedFields)
        throws RemoteException, VendorException;

    /**
     * Removes the vendor management custom form - so there
     * are no customized fields for any vendors.
     */
    public void removeCustomForm()
        throws RemoteException, VendorException; 

    // ---------------------------------------------------------------------
    // Methods for vendor ratings.
    // ---------------------------------------------------------------------

    /**
     * Add a new rating and associate it with the given vendor.
     *
     * @param p_userAddingRate - The user who's associating the
     *                           rating to the specified vendor.
     * @param p_vendor - The vendor whom the rating is associated with.
     * @param p_rating - The rating to be added and associated with the vendor.
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    void addRating(User p_userAddingRate, Vendor p_vendor, 
                   Rating p_rating)
        throws RemoteException, VendorException;

    /**
     * Removing a rating from the given vendor.
     *
     * @param p_userRemovingRate - the user who's performing the removal 
     *                     action. Only an admin or the original person 
     *                     who made the rating can remove it. 
     * @param p_vendor - The vendor whoes rating is being removed.
     * @param p_rating - The rating to be removed.
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    void removeRating(User p_userRemovingRate, Vendor p_vendor, 
                      long p_ratingId)
        throws RemoteException, VendorException;

    /**
     * Update an existing rating associated with the specified vendor.
     *
     * @param p_userUpdatingRate - the user who's updating the
     *              rating. Only an admin or the original person who made the 
     *              rating can update it.
     * @param p_vendor - The vendor whoes rating is being updated.
     * @param p_rating - The rating to be updated
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    void updateRating(User p_userUpdatingRate, Vendor p_vendor, 
                      Rating p_rating)
        throws RemoteException, VendorException;

    /**
     * Get a vendor rating by id.
     *
     * @param p_userQueryingRate - the user who's performing the query.
     * @param p_ratingId - The id of the rating to be searched.
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    Rating getRatingById(User p_userQueryingRate, long p_ratingId)
        throws RemoteException, VendorException;

    /**
     * Get the ratings that are associated with the specified tasks. 
     * The list will mainly just have one rating or none. However, a 
     * task can be rated by more than one person (PM, Admin and/or 
     * WorkflowManager).
     *
     * @param p_userQueryingRate - the user who's performing the query.
     * @param p_taskIds - A list of task ids for which the ratings should
     *                    be queried.
     *
     * @return  A Map of ratings where the key is the task id and the 
     *          value is a List that contains "Rating" objects.  
     *
     * @throws RemoteException Network related exception.
     * @throws VendorException Component related exception.
     */
    HashMap getRatingsInTasks(User p_userQueryingRate, long[] p_taskIds)
        throws RemoteException, VendorException;

};
