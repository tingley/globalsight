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
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;

// java imports
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
                                                                                 
/**
 * This class represents the remote implementation of a VendorManagement that
 * manages the objects needed for managing vendors.
 */
public class VendorManagementWLRMIImpl 
    extends RemoteServer implements VendorManagementWLRemote
{
    // passes all calls off to the local instance (serves as a proxy)
    private VendorManagementLocal m_localInstance = null;

    /**
     * Construct a remote Vendor Management.
     *
     * @exception java.rmi.RemoteException Network related exception.
     */
    public VendorManagementWLRMIImpl() throws RemoteException, VendorException
    {
        super(VendorManagement.SERVICE_NAME); 
        m_localInstance = new VendorManagementLocal();        
    }
    
    /**
     * @see VendorManagement.addVendor(String, Vendor, FieldSecurity)
     */
    public void addVendor(User p_userPerformingAdd, Vendor p_newVendor,
                          FieldSecurity p_vendorFS)
        throws RemoteException, VendorException
    {
        m_localInstance.addVendor(p_userPerformingAdd, p_newVendor,
                                  p_vendorFS);
    }

    /**
     * @see VendorManagement.modifyVendor(String, Vendor, FieldSecurity)
     */
    public void modifyVendor(User p_userPerformingMod, Vendor p_modifiedVendor,
                             FieldSecurity p_vendorFS)
        throws RemoteException, VendorException
    {
        m_localInstance.modifyVendor(p_userPerformingMod, p_modifiedVendor,
                                     p_vendorFS);
    }

    /**
     * @see VendorManagement.findVendors(User, VendorSearchParameters)
     */
    public List findVendors(User p_userPerformingSearch, 
                            VendorSearchParameters p_searchParameters)
        throws RemoteException, VendorException
    {
        return m_localInstance.findVendors(
            p_userPerformingSearch, p_searchParameters);
    }


    /**
     * @see VendorManagement.removeVendor(String, long)
     */
    public void removeVendor(User p_userPerformingRemoval, long p_id)
        throws RemoteException, VendorException
    {
        m_localInstance.removeVendor(p_userPerformingRemoval, p_id);
    }

    /**
     * @see VendorManagement.removeVendor(String, String
     */
    public void removeVendor(User p_userPerformingRemoval, 
                             String p_customVendorId)
        throws RemoteException, VendorException
    {
        m_localInstance.removeVendor(p_userPerformingRemoval, 
                                     p_customVendorId);
    }

    /**
     * @see VendorManagement.getVendors(User)
     */
    public List getVendors(User p_userQuerying)
        throws RemoteException, VendorException
    {
        return m_localInstance.getVendors(p_userQuerying);
    }

    /**
     * @see VendorManagement.getVendorByCustomId(User, String)
     */
    public Vendor getVendorByCustomId(User p_userQuerying,
                                      String p_customVendorId)
        throws RemoteException, VendorException
    {
        return m_localInstance.getVendorByCustomId(p_userQuerying,
                                                   p_customVendorId);    
    }

    /** 
     * @see VendorManagement.getVendorById(long)
     */
    public Vendor getVendorById(long p_id)
        throws RemoteException, VendorException
    {
        return m_localInstance.getVendorById(p_id);
    }   

    /**
     * @see VendorManagement.getVendorByUserId(String)
     */
    public Vendor getVendorByUserId(String p_userId)
        throws RemoteException, VendorException
    {
        return m_localInstance.getVendorByUserId(p_userId);
    }

    /**
     * @see VendorManagement.getUserIdsOfVendors()
     */
    public List getUserIdsOfVendors()
        throws RemoteException, VendorException
    {
        return m_localInstance.getUserIdsOfVendors();
    }

    /**
     * @see VendorManagement.getVendorStatusList()
     */
    public String[] getVendorStatusList()
        throws RemoteException, VendorException
    {
        return m_localInstance.getVendorStatusList();
    }


    /**
     * @see VendorManagement.saveResumeFile(Vendor)
     */
    public void saveResumeFile(Vendor p_vendor)
        throws RemoteException, VendorException
    {
        m_localInstance.saveResumeFile(p_vendor);
    }

    /**
     * @see VendorManagement.getCompanyNames()
     */
    public ArrayList getCompanyNames()
        throws RemoteException, VendorException
    {
        return m_localInstance.getCompanyNames();
    }
    
    /**
     * @see VendorManagement.getPseudonyms
     */
    public ArrayList getPseudonyms()
        throws RemoteException, VendorException
    {
        return m_localInstance.getPseudonyms();
    }

    /**
     * @see VendorManagement.getCustomVendorIds
     */
    public ArrayList getCustomVendorIds()
        throws RemoteException, VendorException
    {
        return m_localInstance.getCustomVendorIds();
    }

    /**
     * @see VendorManagement.modifyVendorUserInfo(User)
     */
    public void modifyVendorUserInfo(User p_modifiedUser)
        throws RemoteException, VendorException
    {
        m_localInstance.modifyVendorUserInfo(p_modifiedUser);
    }

    /**
     * @see VendorManagement.deassociateUserFromVendor(User)
     */
    public void deassociateUserFromVendor(User p_user)
        throws RemoteException, VendorException
    {
        m_localInstance.deassociateUserFromVendor(p_user);
    }

    /** 
     * @see VendorManagement.addVendorsToProject(Project)
     */
    public void addVendorsToProject(Project p_proj)
        throws RemoteException, VendorException
    {
        m_localInstance.addVendorsToProject(p_proj);
    }

    /**
     * @see VendorManagement.removeVendorsFromProject(Project)
     */
    public void removeVendorsFromProject(Project p_proj)
        throws RemoteException, VendorException
    {
        m_localInstance.removeVendorsFromProject(p_proj);
    }


    // -------------------------------------------------------------
    //   Rating Methods
    // ------------------------------------------------------------
    /**
     * @see VendorManagement.addRating(String, Vendor, Rating)
     */
    public void addRating(User p_userAddingRate, 
                          Vendor p_vendor, 
                          Rating p_rating)
        throws RemoteException, VendorException
    {
        m_localInstance.addRating(
            p_userAddingRate, p_vendor, p_rating);
    }

    /**
     * @see VendorManagement.removeRating(String, Vendor, long)
     */
    public void removeRating(User p_userRemovingRate, 
                             Vendor p_vendor, 
                             long p_ratingId)
        throws RemoteException, VendorException
    {
        m_localInstance.removeRating(
            p_userRemovingRate, p_vendor, p_ratingId);
    }

    /**
     * @see VendorManagement.updateRating(String, Vendor, Rating)
     */
    public void updateRating(User p_userUpdatingRate, 
                             Vendor p_vendor, 
                             Rating p_rating)
        throws RemoteException, VendorException
    {
        m_localInstance.updateRating(
            p_userUpdatingRate, p_vendor, p_rating);
    }

    /**
     * @see VendorManagement.getRatingById(String, long)
     */
    public Rating getRatingById(User p_userQueryingRate, 
                                long p_ratingId)
        throws RemoteException, VendorException
    {
        return m_localInstance.getRatingById(
            p_userQueryingRate, p_ratingId);
    }

    /**
     * @see VendorManagement.getRatingsInTasks(String, long[])
     */
    public HashMap getRatingsInTasks(User p_userQueryingRate, 
                                     long[] p_taskIds)
        throws RemoteException, VendorException
    {
        return m_localInstance.getRatingsInTasks(
            p_userQueryingRate, p_taskIds);
    }

    // -------------------------------------------------------------------
    // Custom form and field methods
    // ----------------------------------------------------------------

    /**
     * @see VendorManagement.getCustomForm()
     */
    public CustomForm getCustomForm()
        throws RemoteException, VendorException
    {
        return m_localInstance.getCustomForm();
    }

    /**
     * @see VendorManagement.updateCustomForm(CustomForm)
     */
    public CustomForm updateCustomForm(CustomForm p_customForm,
                                       List p_removedFields)
        throws RemoteException, VendorException
    {
        return m_localInstance.updateCustomForm(p_customForm,
                                                p_removedFields);
    }

    /**
     * @see VendorManagement.removeCustomForm()
     */
    public void removeCustomForm()
        throws RemoteException, VendorException
    {
        m_localInstance.removeCustomForm();
    }
 }

