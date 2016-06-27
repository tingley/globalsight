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
package com.globalsight.restful.version1_0.tmprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceLog;

@Path("/1.0/companies/{companyName}/tmprofiles")
public class TmProfileResource extends RestResource
{
    private static final Logger logger = Logger.getLogger(TmProfileResource.class);

    public static final String GET_TM_PROFILE = "getTmProfile";
    public static final String GET_ALL_TM_PROFILES = "getAllTmProfiles";

    /**
     * Get translation memory profile by ID.
     * 
     * Sample URL:
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tmprofiles/{id}
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     *            Required.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmProfileId
     *            -- translation memory profile ID. Required.
     * 
     * @return translation memory profile information in JSON.
     * A sample is:
     * {"id":1,"name":"tmprofile_1","description":"","storageTMName":"111","referenceTMGrp":[{"id":29,"referenceTM":"1628"},{"id":4,"referenceTM":"131030_w15OlhConsolidatedPlusNew"},{"id":28,"referenceTM":"111"}]}
     * 
     * @throws RestWebServiceException
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTmProfile(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyName") String p_companyName,
            @PathParam("id") long p_tmProfileId) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("id", p_tmProfileId);
            restStart = RestWebServiceLog.start(TmProfileResource.class, GET_TM_PROFILE, restArgs);

            checkPermission(userName, Permission.TMP_VIEW);

            TranslationMemoryProfile tmp = checkTmProfileId(p_tmProfileId, p_companyName);

            TmProfileEntity tmpResponse = tmProfile2Entity(tmp);

            return Response.status(200).entity(tmpResponse).build();
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new RestWebServiceException(makeErrorJson(GET_TM_PROFILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    /**
     * Get all translation memory profiles.
     * 
     * Sample URL:
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tmprofiles
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name.
     * 
     * @return translation memory profiles information in JSON.
     * A sample is:
     * [{"id":1,"name":"tmprofile_1","description":"","storageTMName":"111","referenceTMGrp":[{"id":28,"referenceTM":"111"},{"id":29,"referenceTM":"1628"},{"id":4,"referenceTM":"131030_w15OlhConsolidatedPlusNew"}]},
     *  {"id":2,"name":"d","description":"","storageTMName":"131030_w15OlhConsolidatedPlusNew","referenceTMGrp":[{"id":4,"referenceTM":"131030_w15OlhConsolidatedPlusNew"},{"id":7,"referenceTM":"Legacy OLH Optimized"}]}]
     * 
     * @throws RestWebServiceException
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTmProfiles(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyName") String p_companyName) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restStart = RestWebServiceLog.start(TmProfileResource.class, GET_ALL_TM_PROFILES,
                    restArgs);

            checkPermission(userName, Permission.TMP_VIEW);

            List<TmProfileEntity> entities = new ArrayList<TmProfileEntity>();
            try
            {
                List<TranslationMemoryProfile> allTMProfiles =
                        (List<TranslationMemoryProfile>) TMProfileHandlerHelper.getAllTMProfiles();
                for (TranslationMemoryProfile tmp : allTMProfiles)
                {
                    entities.add(tmProfile2Entity(tmp));
                }
                return Response.status(200).entity(entities).build();
            }
            catch (Exception e)
            {
                String message = "Unable to get all TM profiles.";
                logger.error(message, e);
                throw new RestWebServiceException(message);
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_ALL_TM_PROFILES, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    private TranslationMemoryProfile checkTmProfileId(long p_tmpId, String p_companyName)
            throws Exception
    {
        TranslationMemoryProfile tmp = TMProfileHandlerHelper.getTMProfileById(p_tmpId);
        if (tmp == null)
            throw new RestWebServiceException("Unable to find translation memory profile by ID: "
                    + p_tmpId);

        Company company = ServerProxy.getJobHandler().getCompany(p_companyName);
        if (company != null && company.getId() != tmp.getCompanyId())
        {
            String msg = "TM Profile " + p_tmpId + " does not belong to company "
                    + company.getName();
            throw new RestWebServiceException(msg);
        }
        return tmp;
    }

    private TmProfileEntity tmProfile2Entity(TranslationMemoryProfile tmp)
            throws RestWebServiceException
    {
        TmProfileEntity tmpEntity = new TmProfileEntity();
        tmpEntity.setId(tmp.getId());
        tmpEntity.setName(tmp.getName());
        tmpEntity.setDescription(tmp.getDescription());
        long storageTMId = tmp.getProjectTmIdForSave();
        try
        {
            ProjectTM tm = ServerProxy.getProjectHandler().getProjectTMById(storageTMId, false);
            tmpEntity.setStorageTMName(tm.getName());
        }
        catch (Exception ex)
        {
            String msg = "cannot get storage tm by id: " + storageTMId;
            logger.error(msg, ex);
            throw new RestWebServiceException(msg);
        }

        // add reference TMs
        Vector<LeverageProjectTM> tms = tmp.getProjectTMsToLeverageFrom();
        for (LeverageProjectTM lp_tm : tms)
        {
            try
            {
                long projectTmId = lp_tm.getProjectTmId();
                ProjectTM tm = ServerProxy.getProjectHandler().getProjectTMById(projectTmId, false);
                tmpEntity.addReferenceTM(projectTmId, tm.getName());
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
        
        return tmpEntity;
    }
}
