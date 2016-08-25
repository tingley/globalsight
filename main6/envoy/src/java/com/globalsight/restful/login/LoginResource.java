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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceUtil;

@Path("/companies/{companyID}/login-helper")
public class LoginResource extends RestResource
{
    public static final String LOGIN = "login";

    /**
     * Authenticate user name and password to return access token and companyID.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyID}/login-helper
     * 
     * @return Access token if user name and password are correct. A sample is:
     *      {"companyID":1000,"accessToken":"w4MHMYAqdnU=<separator>8Zs5rDHLrnU=+_+York"}
     * 
     * @throws RestWebServiceException
     * 
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @HeaderParam("Authorization") List<String> authorization)
            throws RestWebServiceException
    {
        try
        {
            String userName = RestWebServiceUtil
                    .getUserNameFromBasicAuthorizationHeader(authorization);
            User user = ServerProxy.getUserManager().getUserByName(userName);
            // Username and password have been verified in
            // "RestSecurityInterceptor", put the real password as parameter.
            String accessToken = doLogin(userName, user.getPassword());

            Company company = ServerProxy.getJobHandler().getCompany(user.getCompanyName());
            accessToken = accessToken + "+_+" + user.getCompanyName();

            JSONObject json = new JSONObject();
            json.put("companyID", company.getId());
            json.put("accessToken", accessToken);

            return Response.status(200).entity(json.toString()).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(LOGIN, e.getMessage()));
        }
    }
}
