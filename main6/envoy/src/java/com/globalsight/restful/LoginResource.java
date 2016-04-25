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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

/**
 * Return a basic authorization header for QA purpose only.
 */
@Path("/companies/{companyName}/login-helper")
public class LoginResource extends RestResource
{
    private static final Logger logger = Logger.getLogger(LoginResource.class);

    /**
     * Generate the authorization header so that QA can get this for testing. Will not be published.
     * 
     * @param p_userName
     * @param p_password
     * @return
     * @throws RestWebServiceException
     */
    @GET
    @Path("/")
    public Response getAuthorization(
            @QueryParam("userName") String p_userName,
            @QueryParam("password") String p_password) throws RestWebServiceException
    {
        String authorization = authorizationHeader(p_userName, p_password);
        return Response.status(200).entity(authorization).build();
    }

}
