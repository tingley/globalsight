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

package com.globalsight.everest.usermgr;

import org.apache.log4j.Logger;

// globalsight
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.vendormanagement.UpdatedDataEvent;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;
import com.globalsight.everest.vendormanagement.VendorManagement;


// java
import java.rmi.RemoteException;
import java.util.Vector;



/**
 *  User Manager Event handler
 */
public class UserManagerEventHandler
{
    private static VendorManagement s_vm = null;

    //for logging purposes
    private static final Logger c_logger =
        Logger.getLogger(
            UserManagerEventHandler.class.getName());

    UserManagerEventHandler()
    {}
   
    public void dataUpdated(User p_user, UpdatedDataEvent p_event)
        throws UserManagerException
    {
        switch (p_event.getEventType())
        {
        case UpdatedDataEvent.CREATE_EVENT:
            // do nothing.
            break;

        case UpdatedDataEvent.UPDATE_EVENT:

            // first check if you can even get to VM - may not be installed
            if (getVM() != null)
            {
                //  If the user is associated with a vendor then modify the
                //  vendor too.
                //  make sure this is a request that started with user.
                try
                {
                    Vendor v = getVM().getVendorByUserId(p_user.getUserId());
                    if (v != null) 
                    {
                        getVM().modifyVendorUserInfo(p_user);
                    }
                }
                catch (Exception ve)
                {
                    // already logged out by VM
                }
            }
            break;

        case UpdatedDataEvent.DELETE_EVENT:

            // first check if you can even get to VM - may not be installed
            if (getVM() != null)
            {
                // If a vendor exists for this user set the "isAmbassadorUser" flag
                // to false to disassociate the vendor from the user.
                try
                {
                    Vendor v  = getVM().getVendorByUserId(p_user.getUserId());
                    if (v != null) 
                    {
                        getVM().deassociateUserFromVendor(p_user);
                    }
                }
                catch (Exception e)
                {
                    // already logged out by VM
                }
            }
            break;

        default:
            //nothing
        }            
    }

    //================================= private methods ============================================

     
    private VendorManagement getVM()
        throws UserManagerException
    {
        if (s_vm == null)
        {
            try
            {
                s_vm = ServerProxy.getVendorManagement();
            } catch (Exception e)
            {
                // just return NULL
            }
        }
        return s_vm;
    }
}
