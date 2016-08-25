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

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.restful.RestConstants;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceUtil;

@Path("/login-helper")
public class LoginResource extends RestResource implements RestConstants
{
    public static final String LOGIN = "login";

    /**
     * Authenticate user name and password to return access token and company information.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/login-helper
     * 
     * @return Access token and company information if user name and password are correct.
     * A sample is:
     * <p>
     *  {
     *     "companies": [
     *        {
     *            "companyID": 1,
     *            "companyName": "Welocalize"
     *        },
     *        {
     *            "companyID": 1000,
     *            "companyName": "York"
     *        }
     *     ],
     *    "accessToken": "x0xrLh4QLza2I2DA1E3KkA==<separator>xAlMOEje5NQ=+_+Welocalize"
     *  }
     * </p>
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
            accessToken = accessToken + "+_+" + user.getCompanyName();

            JSONObject json = new JSONObject();
            json.put("accessToken", accessToken);

            JSONArray comArray = new JSONArray();
            Company company = ServerProxy.getJobHandler().getCompany(user.getCompanyName());
            // return all companies if current user is super user
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(String.valueOf(company.getId())))
            {
                Collection<Company> companies = ServerProxy.getJobHandler().getAllCompanies();
                for (Company c : companies)
                {
                    JSONObject com = new JSONObject();
                    com.put(COMPANY_NAME, c.getCompanyName());
                    com.put(COMPANY_ID, c.getIdAsLong());
                    comArray.put(com);
                }
            }
            else
            {
                JSONObject com = new JSONObject();
                com.put(COMPANY_NAME, company.getCompanyName());
                com.put(COMPANY_ID, company.getIdAsLong());
                comArray.put(com);
            }

            json.put("companies", comArray);

            return Response.status(200).entity(json.toString()).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(LOGIN, e.getMessage()));
        }
    }
}
