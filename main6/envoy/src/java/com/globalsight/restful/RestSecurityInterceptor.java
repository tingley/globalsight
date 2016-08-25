/**
 * Copyright 2016 Welocalize, Inc.
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.naming.NamingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.util.Base64;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.util.StringUtil;

/**
 * This interceptor verify the access permissions for a user based on username
 * and password provided in request.
 *  
 */
@Provider
public class RestSecurityInterceptor extends RestResource implements ContainerRequestFilter,
        RestConstants
{
    private static final Logger logger = Logger.getLogger(RestSecurityInterceptor.class);

    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    private static final String AUTHENTICATION_SCHEME = "Basic";

    private static final ServerResponse ACCESS_DENIED = new ServerResponse(
            "Access denied for this resource", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_DENIED_EMPTY_USERNAME_PWD = new ServerResponse(
            "Empty username and password", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_DENIED_INVALID_USER = new ServerResponse(
            "Incorrect username", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_DENIED_INVALID_PASSWORD = new ServerResponse(
            "Incorrect password", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse(
            "Nobody can access this resource", 403, new Headers<Object>());

    private static final ServerResponse ACCESS_DENIED_EMPTY_ACCESS_TOKEN = new ServerResponse(
            "Empty access token", 401, new Headers<Object>());

    private static final ServerResponse ACCESS_DENIED_INVALID_ACCESS_TOKEN = new ServerResponse(
            "Invalid access token", 401, new Headers<Object>());

    private static final ServerResponse SERVER_ERROR = new ServerResponse("INTERNAL SERVER ERROR",
            500, new Headers<Object>());

    private static final String ERROR_MSG = "errorMsg";
    private static final String COMPANY = "company";

    /**
     * Filter requests:
     * 1. Validate basic authorization information.
     * 2. Validate "companyName" parameter.
     * 3. Other common validation such as "tmId" for TM resource.
     */
    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        setCharset(requestContext);

        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext
                .getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String companyID = pathParams.getFirst(COMPANY_ID);

        // Check "companyID" parameter
        HashMap<String, Object> checkRes = checkCompany(companyID);
        if (checkRes.get(ERROR_MSG) != null)
        {
            ServerResponse invalidCompany = new ServerResponse((String) checkRes.get(ERROR_MSG),
                    400, new Headers<Object>());
            requestContext.abortWith(invalidCompany);
            return;
        }
        // If error message is null, company must not be null.
        Company company = (Company) checkRes.get(COMPANY);

        boolean filterByToken = true;
        // "login" method is used to get access token, can not filter via token.
        if ("login".equals(method.getName()))
        {
            filterByToken = false;
        }

        // Access allowed for all
        if (!method.isAnnotationPresent(PermitAll.class))
        {
            // Access denied for all
            if (method.isAnnotationPresent(DenyAll.class))
            {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            // Get request headers
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();

            User user = null;
            ////// Authenticate via token
            if (filterByToken)
            {
                final List<String> gsAccessToken = headers.get(RestConstants.GLOBALSIGHT_ACCESS_TOKEN);
                if (gsAccessToken == null || gsAccessToken.isEmpty())
                {
                    requestContext.abortWith(ACCESS_DENIED_EMPTY_ACCESS_TOKEN);
                    return;
                }

                String accessToken = gsAccessToken.get(0);
                try
                {
                    checkAccess(accessToken);

                    user = getUser(getUserNameFromSession(accessToken));
                }
                catch (RestWebServiceException e)
                {
                    requestContext.abortWith(ACCESS_DENIED_INVALID_ACCESS_TOKEN);
                    return;
                }
            }
            ////// Basic authorization
            else
            {
                // Fetch authorization header
                final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
                // If no authorization information present; block access
                if (authorization == null || authorization.isEmpty())
                {
                    requestContext.abortWith(ACCESS_DENIED_EMPTY_USERNAME_PWD);
                    return;
                }

                // Get encoded username and password
                final String encodedUserPassword = authorization.get(0).replaceFirst(
                        AUTHENTICATION_SCHEME + " ", "");

                // Decode username and password
                String usernameAndPassword = null;
                try
                {
                    usernameAndPassword = new String(Base64.decode(encodedUserPassword));
                }
                catch (IOException e)
                {
                    requestContext.abortWith(SERVER_ERROR);
                    return;
                }

                // Split username and password tokens
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String username = tokenizer.nextToken();
                final String password = tokenizer.nextToken();
                // Verifying username and password
                try
                {
                    user = ServerProxy.getUserManager().getUserByName(username);
                    if (user == null)
                    {
                        user = ServerProxy.getUserManager().getUser(username);
                    }
                    if (user == null)
                    {
                        requestContext.abortWith(ACCESS_DENIED_INVALID_USER);
                        return;
                    }
                }
                catch (Exception e)
                {
                    requestContext.abortWith(ACCESS_DENIED_INVALID_USER);
                    return;
                }

                try
                {
                    UserLdapHelper.authenticate(password, user.getPassword());
                    logger.info("User '" + user.getUserName() + "' is requesting '" + method.getName()
                            + "' method with URL: " + uriInfo.getAbsolutePath());
                }
                catch (NamingException e)
                {
                    requestContext.abortWith(ACCESS_DENIED_INVALID_PASSWORD);
                    return;
                }
            }

            // User should belong to current company
            if (!user.getCompanyName().equalsIgnoreCase(company.getCompanyName()))
            {
                String msg = "User '" + user.getUserName() + "' does not belong to company '"
                        + company.getCompanyName() + "'.";
                ServerResponse fromDiffCompanies = new ServerResponse(msg, 400,
                        new Headers<Object>());
                requestContext.abortWith(fromDiffCompanies);
                return;
            }

            // Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class))
            {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

                // Is user valid?
                if (!isUserAllowed(user.getUserName(), user.getPassword(), rolesSet))
                {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }
            }

            // required !!!
            CompanyThreadLocal.getInstance().setValue(company.getCompanyName());
        }
    }

    private boolean isUserAllowed(final String username, final String password,
            final Set<String> rolesSet)
    {
        boolean isAllowed = false;

        // Step 1. Fetch password from database and match with password in
        // argument.
        // If both match then get the defined role for user from database and
        // continue; else return isAllowed [false].
        // Access the database and do this part yourself
        // String userRole = userMgr.getUserRole(username);
        String userRole = "ADMIN";

        // Step 2. Verify user role
        if (rolesSet.contains(userRole))
        {
            isAllowed = true;
        }
        return isAllowed;
    }

    // The returning includes 2 objects: a string message and the "Company" object.
    private HashMap<String, Object> checkCompany(String p_companyID)
    {
        String errorMsg = null;
        if (StringUtil.isEmpty(p_companyID))
        {
            errorMsg = "Empty company ID";
        }

        Company company = null;
        try
        {
            company = getCompanyById(p_companyID);
            if (company == null)
            {
                errorMsg = "Company does not exist for companyID: " + p_companyID;
            }
        }
        catch (Exception e)
        {
            errorMsg = "Invalid company ID: " + p_companyID;
        }

        HashMap<String, Object> res = new HashMap<String, Object>();
        res.put(ERROR_MSG, errorMsg);
        res.put(COMPANY, company);

        return res;
    }

    /**
     * Set "Accept-Charset" to "utf-8" as default to avoid corrupted words.
     */
    private void setCharset(ContainerRequestContext requestContext)
    {
        Object obj = requestContext.getProperty("RESTEASY_CHOSEN_ACCEPT");
        if (obj != null && obj instanceof MediaType)
        {
            MediaType type = (MediaType) obj;
            type = type.withCharset("utf-8");
            requestContext.setProperty("RESTEASY_CHOSEN_ACCEPT", type);
        }
    }
}