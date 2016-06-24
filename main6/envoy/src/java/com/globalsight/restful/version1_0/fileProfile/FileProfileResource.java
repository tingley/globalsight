package com.globalsight.restful.version1_0.fileProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import com.globalsight.restful.version1_0.job.JobResource;
import com.globalsight.util.GlobalSightLocale;

@Path("/1.0/companies/{companyName}/fileProfiles")
public class FileProfileResource extends RestResource
{
    public static final String GET_FILE_PROFILES = "getFileProfiles";
    private static final Logger logger = Logger.getLogger(FileProfileResource.class);

    @GET
    @Path("/getFileProfiles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFileProfiles(@HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);
            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restStart = RestWebServiceLog.start(JobResource.class, GET_FILE_PROFILES, restArgs);

            Collection fileProfiles_all = null;
            Collection fileProfiles_filt = new ArrayList();
            FileProfilePersistenceManager fileProfileManager = ServerProxy
                    .getFileProfilePersistenceManager();
            fileProfiles_all = fileProfileManager.getAllFileProfiles();
            // filter by user
            if (!userName.equals(""))
            {
                User user = ServerProxy.getUserManager().getUserByName(userName);
                List uProjects = ServerProxy.getProjectHandler()
                        .getProjectsByUser(user.getUserId());

                for (Iterator ifp = fileProfiles_all.iterator(); ifp.hasNext();)
                {
                    FileProfile fp = (FileProfile) ifp.next();
                    Project fpProj = getProject(fp);
                    if (uProjects.contains(fpProj))
                    {
                        fileProfiles_filt.add(fp);
                    }
                }
            }
            else
            {
                fileProfiles_filt = fileProfiles_all;
            }

            List<GetFileProfileResponse> entities = new ArrayList<GetFileProfileResponse>();
            Iterator iter = fileProfiles_filt.iterator();
            while (iter.hasNext())
            {
                GetFileProfileResponse profile = new GetFileProfileResponse();
                FileProfile fileProfile = (FileProfile) iter.next();
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
                Vector fileExtensionIds = fileProfile.getFileExtensionIds();
                List<String> fileExtensionList = new ArrayList<String>();
                for (int i = 0; i < fileExtensionIds.size(); i++)
                {
                    Long fileExtensionId = (Long) fileExtensionIds.get(i);
                    FileExtension fileExtension = fileProfileManager
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
