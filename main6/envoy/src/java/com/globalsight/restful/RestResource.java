/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.restful;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.webservices.WebServiceException;

public class RestResource
{
    private static final Logger logger = Logger.getLogger(RestResource.class);

    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

    protected String getUserNameFromRequest(List<String> authorization)
            throws RestWebServiceException
    {
        return RestWebServiceUtil.getUserNameFromRequest(authorization);
    }

    /**
     * Check if current user has the specified permission
     * 
     * @param userName
     * @param permission
     *            Permission information
     * @throws WebServiceException
     */
    protected void checkPermission(String userName, String permission)
            throws RestWebServiceException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(userName);
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(permission))
            {
                String msg = "User '" + userName + "' does not have permission '" + permission
                        + "' to execute current request.";
                throw new RestWebServiceException(msg);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new RestWebServiceException(e.getMessage());
        }
    }

    /**
     * Check if user belongs to same company.
     * 
     * @param p_userName
     *            -- user name
     * @param p_companyName
     *            -- company name
     * 
     * @throws RestWebServiceException
     */
    protected void checkUserCompanyConsistency(String p_userName, String p_companyName)
            throws RestWebServiceException
    {
        String msg = "User " + p_userName + " does not belong to company " + p_companyName;
        try
        {
            boolean flag = false;
            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            if (user != null)
            {
                flag = user.getCompanyName().equals(p_companyName);
            }

            if (!flag)
            {
                throw new RestWebServiceException(msg);
            }

            // required !!!
            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(msg);
        }
    }

    /**
     * Gets locale with specified name.
     * 
     * @param name
     *            Locale name
     * @return GlobalSightLocale Locale information with the specified name
     * 
     * @throws RestWebServiceException
     */
    protected GlobalSightLocale getLocaleByName(String name) throws RestWebServiceException
    {
        if (StringUtil.isEmpty(name))
            return null;

        try
        {
            name = ImportUtil.normalizeLocale(name.trim());
            return ImportUtil.getLocaleByName(name);
        }
        catch (Exception e)
        {
            logger.warn("getLocaleByName() : Fail to get GlobalSightLocale by locale name: " + name);
//            throw new RestWebServiceException("Unable to get locale by name: " + name);
            return null;
        }
    }

    protected final static String authorizationHeader(String username, String password)
    {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }

    protected String makeErrorJson(String p_method, String p_msg)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("method", p_method);
            json.put("message", p_msg);
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        return json.toString();
    }
}
