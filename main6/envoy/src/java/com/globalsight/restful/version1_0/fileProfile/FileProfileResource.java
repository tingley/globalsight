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

package com.globalsight.restful.version1_0.fileProfile;

import java.util.ArrayList;
import java.util.Collection;
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

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceLog;
import com.globalsight.util.GlobalSightLocale;

@Path("/1.0/companies/{companyID}/fileProfiles")
public class FileProfileResource extends RestResource
{
    private static final Logger logger = Logger.getLogger(FileProfileResource.class);

    public static final String GET_FILE_PROFILES = "getFileProfiles";

    /**
     * Get all of the File Profile information from GlobalSight side as JSON
     * 
     * @param p_companyID
     *            Company ID.
     * @return Return all file profiles information for JSON.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFileProfiles(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));
            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("p_companyID", p_companyID);
            restStart = RestWebServiceLog.start(FileProfileResource.class, GET_FILE_PROFILES, restArgs);

            Collection<FileProfile> filteredFileProfiles = new ArrayList<FileProfile>();
            FileProfilePersistenceManager fpManager = ServerProxy
                    .getFileProfilePersistenceManager();
            @SuppressWarnings("unchecked")
            Collection<FileProfile> allFileProfiles = fpManager.getAllFileProfiles();

            // filter by user
            if (userName != null && !"".equals(userName))
            {
                User user = this.getUser(userName);
                List<Project> uProjects = ServerProxy.getProjectHandler()
                        .getProjectsByUser(user.getUserId());

                for (FileProfile fp : allFileProfiles)
                {
                    Project fpProj = getProject(fp);
                    if (uProjects.contains(fpProj))
                    {
                        filteredFileProfiles.add(fp);
                    }
                }
            }
            else
            {
                filteredFileProfiles = allFileProfiles;
            }

            List<GetFileProfileResponse> entities = new ArrayList<GetFileProfileResponse>();
            for ( FileProfile fileProfile : filteredFileProfiles)
            {
                GetFileProfileResponse profile = new GetFileProfileResponse();
                // Add basic information for a specified file profile.
                profile.setId(fileProfile.getId());
                profile.setName(fileProfile.getName());
                profile.setL10nprofileId(fileProfile.getL10nProfileId());
                profile.setSourceFileFormat(fileProfile.getKnownFormatTypeId());
                if (fileProfile.getDescription() == null)
                {
                    profile.setDescription("N/A");
                }
                else
                {
                    profile.setDescription(fileProfile.getDescription());
                }
                // Add file extensions information for a specified file profile.
                Vector<Long> fileExtensionIds = fileProfile.getFileExtensionIds();
                List<String> fileExtensionList = new ArrayList<String>();
                for (int i = 0; i < fileExtensionIds.size(); i++)
                {
                    Long fileExtensionId = (Long) fileExtensionIds.get(i);
                    FileExtension fileExtension = fpManager
                            .readFileExtension(fileExtensionId.longValue());
                    fileExtensionList.add(fileExtension.getName());
                }
                profile.setFileExtension(fileExtensionList);

                // Add locales information for a specified file profile.
                long l10nProfileId = fileProfile.getL10nProfileId();
                ProjectHandler projectHandler = ServerProxy.getProjectHandler();
                L10nProfile l10nProfile = projectHandler.getL10nProfile(l10nProfileId);
                GlobalSightLocale sourceLocale = l10nProfile.getSourceLocale();
                GlobalSightLocale[] targetLocales = l10nProfile.getTargetLocales();
                List<String> targetLocaleList = new ArrayList<String>();
                for (int i = 0; i < targetLocales.length; i++)
                {
                    GlobalSightLocale targetLocale = targetLocales[i];
                    targetLocaleList.add(targetLocale.toString());
                }
                profile.addLocaleInfo(sourceLocale.toString(), targetLocaleList);

                entities.add(profile);

            }
            return Response.status(200).entity(entities).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_FILE_PROFILES, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error("Failed to get the project that file profile " + p_fp.toString()
                    + " is associated with.", e);
        }
        return p;
    }
}
