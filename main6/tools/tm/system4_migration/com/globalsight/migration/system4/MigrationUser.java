/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.migration.system4;

//import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerLocal;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.foundation.User;

import netscape.ldap.LDAPException;


/**
 * This class is responsible for creating a Migration User.
 */
public class MigrationUser
{
    private static final String USER_ID = "gsAdmin";
    
    /**
     * get a user to be used to create a migration project. If it
     * doesn't exist yet, the method creates a new one.
     * @return a migration user
     */
    public static User get()
        throws Exception
    {
        //        UserManager userManager = ServerProxy.getUserManager();
        UserManagerLocal userManager = new UserManagerLocal();
        User migrationUser = null;
        try
        {
            migrationUser = userManager.getUser(USER_ID);
        }
        catch(UserManagerException e)
        {
            Exception original = e.getOriginalException();
            if(!(original instanceof LDAPException 
                 && ((LDAPException)original).getLDAPResultCode()
                 == LDAPException.NO_SUCH_OBJECT))
            {
                throw e;
            }
        }
        
        if(migrationUser == null)
        {
            migrationUser = userManager.createUser();
            migrationUser.setDefaultUILocale("en_US");
            migrationUser.setUserName(USER_ID);
            migrationUser.setUserId(USER_ID);
            migrationUser.setFirstName("Migration");
            migrationUser.setLastName("Tool");
            migrationUser.setPassword("password");
            migrationUser.setEmail("yazawa@globalsight.com");
            migrationUser.setAddress("Boulder, CO");
            migrationUser.setPhoneNumber("(720)622-4000");
            userManager.addUser(migrationUser);
        }
        return migrationUser;
    }

}
