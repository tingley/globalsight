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

package com.globalsight.restful.login;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;

@Path("/companies/{companyName}/login-helper")
public class LoginResource extends RestResource
{
    public static final String LOGIN = "login";

    private static final Logger logger = Logger.getLogger(LoginResource.class);

    /**
     * Authenticate user name and password to return an access token.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/login-helper
     * 
     * @param userName
     *            -- user name to login. Required.
     * @param password
     *            -- password to login. Required.
     * 
     * @return Access token if user name and password are correct.
     * 
     * @throws RestWebServiceException
     * 
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(
            @QueryParam("userName") String p_userName,
            @QueryParam("password") String p_password) throws RestWebServiceException
    {
        try
        {
            String accessToken = doLogin(p_userName, p_password);

            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            accessToken = accessToken + "+_+" + user.getCompanyName();

            return Response.status(200).entity(accessToken).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(LOGIN, e.getMessage()));
        }
    }
}
