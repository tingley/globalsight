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

package com.globalsight.webservices.coti;

import org.apache.ws.security.WSPasswordCallback;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;

/**
 * Authorize handler for COTI login service
 * @author Wayzou
 *
 */
public class AuthHandler implements CallbackHandler
{
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++)
        {
            WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
            String id = pwcb.getIdentifier();
            
            // id == null, check session id in next COTIApi
            if (null == id)
            {
                System.out.println("Authorization failed: user name is empty!");
                throw new UnsupportedCallbackException(pwcb, "Authorization failed: user name is empty!");
            }
            else
            {
                // get this user, and check password & permission
                User u = UserHandlerHelper.getUser(id);
                // check permission
                PermissionManager pm = Permission.getPermissionManager();
                PermissionSet ps = pm.getPermissionSetForUser(u.getUserId());
                if (!ps.getPermissionFor(Permission.COTI_JOB))
                {
                    throw new UnsupportedCallbackException(pwcb, "Authorization failed: permission!");
                }
                
                // check password
                String wssePassword = u.getWssePassword();
                if (wssePassword == null || "".equals(wssePassword))
                {
                    throw new UnsupportedCallbackException(pwcb, "Authorization failed: WSSE password not set!");
                }
                
                pwcb.setPassword(wssePassword);
            }
        }
    }
}