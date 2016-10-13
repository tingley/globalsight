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
package com.globalsight.everest.webapp.pagehandler.administration.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.cvsconfig.CVSModule;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSUtil;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistentObjectNameComparator;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.rss.RSSUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.common.URLDecoder;


/**
 * Page handler used to map selected files to the file profiles to be used for importing.
 */
public class MapFileProfileToFileHandler 
    extends PageHandler 
{
    //
    // PUBLIC CONSTANTS
    //
      
    public static final String ANY_EXTENSION = "AnyExtension";

    // keys for the date stored in the session
    public static final String SELECTED_FILES = "selected_files";    
    public static final String FILE_PROFILES = "file_profiles";    
    public static final String DISPLAY_MAPPINGS = "displayMappings";
    // stores the information to pass on to the next screen
   // for importing - key = file name and value = file profile the
   // file is mapped to
    public static final String MAPPINGS = "mappings";
    public static final String DONE_MAPPING = "doneMapping";
    public static final String FIRST_SELECTED_FP = "firstSelecteFP";

    // values that pass the mapping information in the request object
    public static final String MAP_EXTENSION = "mappedExtension";
    public static final String MAP_FILE_PROFILE = "mappedFileProfile";
    public static final String MAP_FILE_NAMES = "mappedFileNames";
    
    //
    // PRIVATE STATIC VARIABLES
    //
    private static final Logger s_logger =
        Logger.
        getLogger(MapFileProfileToFileHandler.class.getName());

    /**
     * Invokes this PageHandler
     * <p>
     * @param p_thePageDescriptor the page descriptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_descriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        String jobType = (String)sessionMgr.getAttribute("jobType");
        if (jobType != null && !jobType.equals("")) {
            User userHeader = (User)sessionMgr.getAttribute(WebAppConstants.USER);
            String files = (String)p_request.getParameter("selectFiles");
            ArrayList value = null;
            if (files != null && !files.equals("")) {
                if (jobType.equals("cvsJob")) {
                    //For CVS job
                    
                    String jobName = (String)session.getAttribute("jobName");
                    String notes = (String)session.getAttribute("notes");
                    String projectId = (String)session.getAttribute("projectId");
                    String projectName = (String)session.getAttribute("projectName");
                    String sourceLocale = (String)session.getAttribute("sourceLocale");
                    
                    CVSServer server = (CVSServer)session.getAttribute("cvsServer");
                    CVSModule module = (CVSModule)session.getAttribute("cvsModule");
                    
                    value = CVSUtil.saveData(module, jobName, sourceLocale, projectId, projectName, notes, files, userHeader);
                } else if (jobType.equals("rssJob")) {
                    //For RSS job
                    String jobName = p_request.getParameter("jobName");
                    String notes = p_request.getParameter("notesField");
                    String project = p_request.getParameter("projects");
                    String[] projectInfos = project.split(",");
                    String projectId = projectInfos[0];
                    String projectName = projectInfos[1];
                    String sourceLocale = p_request.getParameter("srcLocales");

                    String rssItemID = (String)sessionMgr.getAttribute("RSS_ITEM_ID");
                    sessionMgr.setAttribute("projectId", projectId);
                    sessionMgr.setAttribute("jobName", jobName);
                    sessionMgr.setAttribute("notes", notes);
                    value = RSSUtil.saveData(jobName, sourceLocale, projectId, projectName, notes, rssItemID, userHeader);
                }
                String currentFolder = (String)value.get(0);
                ArrayList<String> results = (ArrayList<String>)value.get(1);
                sessionMgr.setAttribute("fileList", (HashSet<String>)value.get(2));
            }
        }
        performAppropriateOperation(p_request);
        dispatchJSP(p_descriptor, p_request, p_response, p_context);
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_descriptor,
                               HttpServletRequest p_request,
                               HttpServletResponse p_response,
                               ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_descriptor, p_request,
                                p_response,p_context);
    }

    private void performAppropriateOperation(HttpServletRequest p_request)
        throws EnvoyServletException
    {   
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);

        String pageAction= (String)p_request.getParameter("pageAction");

        // if the action is clearAll - clear the mappings
        if (pageAction != null && pageAction.equals(WebAppConstants.CLEAR_ALL))
        {
            // clear out the mappings
            sessionMgr.setAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS,
                                    new Hashtable());
            sessionMgr.setAttribute(MapFileProfileToFileHandler.MAPPINGS,
                                    new Hashtable());   

            // remove the setting of the first selected profile
            sessionMgr.removeElement(FIRST_SELECTED_FP);

            //reset the file profiles so they aren't filtered by the L10nProfile
            Hashtable files = 
                (Hashtable)sessionMgr.getAttribute(MapFileProfileToFileHandler.SELECTED_FILES);
            Hashtable fileProfiles = 
                (Hashtable)getFileProfiles(files.keySet() , p_request);
            sessionMgr.setAttribute(
                MapFileProfileToFileHandler.FILE_PROFILES, fileProfiles);  
        }
        else
        {
            String extension = (String)p_request.getParameter(MAP_EXTENSION);
       
            // if an extension was specified then a mapping is being passed in to be saved
            if (extension != null  && extension.length() > 0) 
            {
                String fpName = p_request.getParameter(MAP_FILE_PROFILE);
                // get the right one.
                FileProfile fp = findFileProfile(extension, fpName,
                                             sessionMgr);
                String fileNames = p_request.getParameter(MAP_FILE_NAMES);
                String[] fNamesArray = null;
                if (fileNames != null)
                {
                    fNamesArray = getFileNames(fileNames);
                    addOrUpdateMapping(extension, fp, fNamesArray, sessionMgr);
                }

                // Here we try to stop a refresh from setting old assignments by removing them
                p_request.removeAttribute(MAP_EXTENSION);
                p_request.removeAttribute(MAP_FILE_NAMES);
                p_request.removeAttribute(MAP_FILE_PROFILE);
            }
            else
            {
                // otherwise viewing the page so populate the data in the tables
                populateData(p_request);
            }

            setDoneMapping(sessionMgr);
        }
    }

    // set up all the data
    private void populateData(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);

        ArrayList files =
            new ArrayList((HashSet)sessionMgr.getAttribute(SelectFileHandler.FILE_LIST));
        
        // pass in the files and organize into lists by file extension
        Hashtable orgFiles = organizeFiles(files, p_request);
        sessionMgr.setAttribute(
            MapFileProfileToFileHandler.SELECTED_FILES, orgFiles);

        // get the file profiles organized by extensions
        Set extensions = orgFiles.keySet();
        Hashtable fileProfiles = getFileProfiles(extensions, p_request);
        sessionMgr.setAttribute(
            MapFileProfileToFileHandler.FILE_PROFILES, fileProfiles);  

        if (sessionMgr.getAttribute(MapFileProfileToFileHandler.FIRST_SELECTED_FP) != null)
        {
           fileProfiles = filterFileProfilesByFirstSelected(sessionMgr);
        }

        if (sessionMgr.getAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS) == null)
        {
            sessionMgr.setAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS, 
                                      new Hashtable());
        }
        else
        {
            // it wasn't null so coming in here from possibly the first page after
            // some mappings are already done.  Need to remove any mappings that
            // belong to files that were removed from the import list.
            verifyAndAdjustMappings(sessionMgr);
        }    
    }

    // group the files by extension
    private Hashtable organizeFiles(List p_fileNames,
                                    HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        // clear out the list and start with new since some files may have been removed
        Hashtable listByFileExt = getListByFileExt(sessionMgr);
        if (listByFileExt.size()  > 0)
        {
            sessionMgr.setAttribute(this.SELECTED_FILES, new Hashtable());
        }

        // loop through the file names and put them into the hashtable
        // according to their extension
        for (Iterator i = p_fileNames.iterator() ; i.hasNext() ; )
        {
            String fileName = (String)i.next();

            int index = fileName.lastIndexOf('.');
            // if an extension was even specified and something exists
            if (index > 0 && index < (fileName.length()-1))
            {
                String extension = fileName.substring(index+1).toLowerCase();
                String companyId = CompanyThreadLocal.getInstance().getValue();
                // create a file extension object just for checking if the
                // file extension list contains it
                FileExtensionImpl fe = new FileExtensionImpl(extension, companyId);
                List allExt = getFileExtensions();
                if (allExt.contains(fe))
                {   
                    addOrUpdateFileExt(extension, fileName, sessionMgr);    
                }   
                else
                {
                    addOrUpdateFileExt(ANY_EXTENSION, fileName, sessionMgr);
                } 
            }
            else  // no extension - add to "Any extension" list
            {
                addOrUpdateFileExt(ANY_EXTENSION, fileName, sessionMgr);    
            }
        }
        return getListByFileExt(sessionMgr);
    }           

    // add the file and the extension to the organized list of files.
    private void addOrUpdateFileExt(String p_extension, String p_fileName,
                                    SessionManager p_sessionMgr)
    {
        Hashtable listByFileExt = getListByFileExt(p_sessionMgr);
            
        if (listByFileExt.containsKey(p_extension))
        {
            Set files = (Set)listByFileExt.get(p_extension);
            files.add(p_fileName);
            listByFileExt.put(p_extension, files);
        }
        else
        {
            Set newFiles = new TreeSet();
            newFiles.add(p_fileName);
            listByFileExt.put(p_extension, newFiles);
        }

        p_sessionMgr.setAttribute(SELECTED_FILES, listByFileExt);

    }

    // add the file profile and the extension to the organized list of file profiles
    private void addOrUpdateFileProfileExt(String p_extension, FileProfile p_fp, 
                                           SessionManager p_sessionMgr)
    {
        Hashtable fileProfByExt = getFileProfByExt(p_sessionMgr);

        if (fileProfByExt.containsKey(p_extension))
        {
            Set fps = (Set)fileProfByExt.get(p_extension);
            fps.add(p_fp);
            fileProfByExt.put(p_extension, fps);
        }
        else
        {
            PersistentObjectNameComparator nc = new PersistentObjectNameComparator();
            Set newFps = new TreeSet(nc);
            newFps.add(p_fp);
            fileProfByExt.put(p_extension, newFps);
        }
        p_sessionMgr.setAttribute(MapFileProfileToFileHandler.FILE_PROFILES,
                                  fileProfByExt);
    }

     // add the file profile and the extension to the organized list of file profiles
    private void addOrUpdateMapping(String p_extension, FileProfile p_fp, 
                                    String[] p_files, SessionManager p_sessionMgr)
    {
        Hashtable mappedFiles = null;

        Hashtable mappingDisplay = 
            (Hashtable)p_sessionMgr.getAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS);
        Hashtable mappings = getMappingList(p_sessionMgr);

        if (mappingDisplay.containsKey(p_extension))
        {
            mappedFiles = (Hashtable)mappingDisplay.get(p_extension);
        }
        else
        {
            mappedFiles = new Hashtable();
        }
        
        String fileProfileName = p_fp.getName();
        for (int j = 0 ; j < p_files.length ; j++)
        {
            String filename = (String)p_files[j];

//            filename.replaceAll("%5C", "\\");
//            filename.replaceAll("%20", " ");
            
            mappedFiles.put(filename, fileProfileName);
            mappings.put(filename,p_fp);
        }
        mappingDisplay.put(p_extension, mappedFiles);

        p_sessionMgr.setAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS, 
                                  mappingDisplay);

        if (p_sessionMgr.getAttribute(FIRST_SELECTED_FP) == null)
        {
            p_sessionMgr.setAttribute(FIRST_SELECTED_FP, p_fp);
            filterFileProfilesByFirstSelected(p_sessionMgr);
        }
    }

    // add the file profile and file name string to the display hashtable by extension
    // and to the array.
    private void addOrUpdateFileProfilesExt(String p_extension, List p_fps,
                                            SessionManager p_sessionMgr)
    {
        Hashtable fileProfByExt = getFileProfByExt(p_sessionMgr);

        if (fileProfByExt.containsKey(p_extension))
        {
            Set fps = (Set)fileProfByExt.get(p_extension);
            fps.addAll(p_fps);
            fileProfByExt.put(p_extension, fps);
        }
        else
        {
            PersistentObjectNameComparator nc = new PersistentObjectNameComparator();
            Set newFps = new TreeSet(nc);
            newFps.addAll(p_fps);
            fileProfByExt.put(p_extension, newFps);
        }
        p_sessionMgr.setAttribute(MapFileProfileToFileHandler.FILE_PROFILES,
                                  fileProfByExt);
    }

    private FileProfile findFileProfile(String p_ext, String p_fpName,
                                        SessionManager p_sessionMgr)
    {
        Hashtable fileProfByExt = getFileProfByExt(p_sessionMgr);
        Set fps = (Set)fileProfByExt.get(p_ext);
        FileProfile foundFp = null;
        for (Iterator x = fps.iterator() ; foundFp == null && x.hasNext() ; )
        {
            FileProfile fp = (FileProfile)x.next();
            if (fp.getName().equals(p_fpName))
            {
                foundFp = fp;
            }
        }
        return foundFp;
    }
        
    // return all the possible file extensions the system is aware of
    private List getFileExtensions()
    {
        List fileExtensions = null;
        try
        {
            fileExtensions = 
                    (List)ServerProxy.getFileProfilePersistenceManager().getAllFileExtensions();
        }
        catch(Exception e)
        {
            s_logger.error("Failed to retrieve all the file extensions known to GlobalSight.",
                               e);
            // just create an empty sorted set
            fileExtensions = new ArrayList();
        }
        return fileExtensions;
    }

    /**
     * Filter the file profiles by the first selected.
     */
    private Hashtable filterFileProfilesByFirstSelected(SessionManager p_sessionMgr)
    {
        FileProfile selectedFP = 
            (FileProfile)p_sessionMgr.getAttribute(FIRST_SELECTED_FP);

        Hashtable fileProfList = getFileProfByExt(p_sessionMgr);
        if (selectedFP != null)
        {
            Hashtable filteredFPList = new Hashtable();
            Set exts = fileProfList.keySet();

            for (Iterator ie=exts.iterator() ; ie.hasNext() ; )
            {
                String ext = (String)ie.next();
                Set fps = (Set)fileProfList.get(ext);

                PersistentObjectNameComparator nc = new PersistentObjectNameComparator();
                Set newFps = new TreeSet(nc);
                for (Iterator ifps = fps.iterator() ; ifps.hasNext() ; )
                {
                    FileProfile fp = (FileProfile)ifps.next();
                    
                    // if the same l10nprofile then add to the new list
                    if (fp.getL10nProfileId() == selectedFP.getL10nProfileId())
                    {
                        newFps.add(fp);
                    }
                }
                filteredFPList.put(ext, newFps);
            }                          

            // set it with any of the changes
            fileProfList = filteredFPList;
            p_sessionMgr.setAttribute(MapFileProfileToFileHandler.FILE_PROFILES, fileProfList);
        }                         

        return fileProfList;
    }

    /**
     * Get the file profiles that cover the specified extensions.
     * This also includes the file profiles that are associated with Any or All extensions.  
     */
    private Hashtable getFileProfiles(Set p_extensionNames,
                                      HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        // start out with a new/clear list since files may be added or removed and
        // certain extensions/file profiles don't apply anymore.
        sessionMgr.setAttribute(MapFileProfileToFileHandler.FILE_PROFILES,
                                  new Hashtable());

        User u = getUser(session);

        // query for all file profiles
        PersistentObjectNameComparator nc = new PersistentObjectNameComparator();
        Set fileProfiles = new TreeSet(nc);
        try 
        {
            List<Project> uProjects = null;
            // if a project is specified in the session then use that project to 
            // filter file profiles
            String projectId = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_ID);
            if (projectId != null)
            {
                uProjects = new ArrayList<Project>();
                Project p = 
                    ServerProxy.getProjectHandler().getProjectById(Long.parseLong(projectId));
                uProjects.add(p);
            }
            else  // get all the projects the user has permission to access
            {
                uProjects = ServerProxy.getProjectHandler().getProjectsByUserPermission(u);
            }
            
            List exts = new ArrayList(p_extensionNames);
            List fps = (List)(ServerProxy.getFileProfilePersistenceManager().
                          getFileProfilesByExtension(exts));
            for (Iterator ifp = fps.iterator() ; ifp.hasNext() ; )
            {
                FileProfile fp = (FileProfile)ifp.next();
                Project fpProj = getProject(fp);

                // get the project and check if it is in the group of user's projects
                if (uProjects.contains(fpProj))
                {
                    fileProfiles.add(fp);
                }
            }
        }
        catch(Exception e)
        {
            s_logger.error("Failed to retrieve all the file profiles associated with extensions " +
                           p_extensionNames.toString(), e);
            throw new EnvoyServletException(e);
        }
        
        // organize the file profiles into what extensions they are associated with
        List allExtensionFP = new ArrayList();
        for (Iterator i = fileProfiles.iterator() ; i.hasNext() ; ) 
        {
            FileProfileImpl fp = (FileProfileImpl)i.next();
            try
            {
                Collection exts = 
                    ServerProxy.getFileProfilePersistenceManager().
                        getFileExtensionsByFileProfile(fp);
                // if there are no extensions then the file profile can be used
                // for all
                if (exts == null || exts.size() == 0) 
                {
                    // save as a FP that can be associated with any extension 
                    // to be handled later
                    allExtensionFP.add(fp);   
                }
                else
                {
                    String companyId = CompanyThreadLocal.getInstance().getValue();
                    for (Iterator ien = p_extensionNames.iterator() ; ien.hasNext(); )
                    {
                        String extension = (String)ien.next();
                        FileExtensionImpl fei = new FileExtensionImpl(extension, companyId);
                        if (exts.contains(fei))
                        {
                            addOrUpdateFileProfileExt(extension, fp, sessionMgr);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error("Failed to get the file extensions of file profile " + fp.toString());
                throw new EnvoyServletException(e);
            }

            // if there are file profiles set up to support all extensions then
            // add to each extension list.
            if (allExtensionFP.size() > 0)
            {
                for (Iterator eni = p_extensionNames.iterator() ; eni.hasNext() ; )
                {
                    String extension = (String)eni.next();
                    addOrUpdateFileProfilesExt(extension, allExtensionFP, sessionMgr);
                }
            }
        }
        
        return getFileProfByExt(sessionMgr); 
    }


    /** 
     * Verify that the mappings lists (both display and mappings) contain only 
     * mappings that are in the list of files - just in case some files were removed 
     * from the list.
     */
    void verifyAndAdjustMappings(SessionManager p_sessionMgr)
    {
        Hashtable files = getListByFileExt(p_sessionMgr);
        Hashtable dispMappings = 
            (Hashtable)p_sessionMgr.getAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS);
        Hashtable mappings = getMappingList(p_sessionMgr);

        Set mapKeys = dispMappings.keySet();
        for (Iterator imk = mapKeys.iterator() ;  imk.hasNext() ; )
        {
            Hashtable adjustedMapping = new Hashtable();
            String ext = (String)imk.next();
            Set fm = (Set)files.get(ext);
            if (fm != null)
            {
                // go through them and see if they are in the file list still
                Hashtable em = (Hashtable)dispMappings.get(ext);
                Set fileKeys = em.keySet();
                for (Iterator ifk = fileKeys.iterator() ; ifk.hasNext() ; )
                {
                    String fileName = (String)ifk.next(); 
                    // if the file is in the list add to the new display mapping list
                    if (fm.contains(fileName))
                    {
                        adjustedMapping.put(fileName, em.get(fileName));
                    }
                    else    //remove from the mapping list (and don't add to the new display list)
                    {
                        mappings.remove(fileName);
                    }
                }
            }
            // put in the newly adjusted mappings for the particular extension
            dispMappings.put(ext, adjustedMapping);
        }   

        p_sessionMgr.setAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS, dispMappings);
    }

    /**
     * Get the file profiles organizes by extension.
     * If it hasn't been set yet then return an empty hashtable.
     */
    private Hashtable getFileProfByExt(SessionManager p_sessionMgr)
    {
        Hashtable fileProfByExt = 
            (Hashtable)p_sessionMgr.getAttribute(MapFileProfileToFileHandler.FILE_PROFILES);
        if (fileProfByExt == null)
        {
            fileProfByExt = new Hashtable();
            p_sessionMgr.setAttribute(MapFileProfileToFileHandler.FILE_PROFILES,
                                      fileProfByExt);
        }
        return fileProfByExt;
    }

    /**
     * Get the list of selected files organized by extension.
     * If it hasn't been set yet then return an emtpy hashtable.
     */
    private Hashtable getListByFileExt(SessionManager p_sessionMgr)
    {
        Hashtable listByFileExt = 
            (Hashtable)p_sessionMgr.getAttribute(MapFileProfileToFileHandler.SELECTED_FILES);
        if (listByFileExt == null)
        {
            listByFileExt = new Hashtable();
            p_sessionMgr.setAttribute(MapFileProfileToFileHandler.SELECTED_FILES,
                                      listByFileExt);
        }
        return listByFileExt;
    }   


    /**
     * Return the list of mappings.  If not set yet then just return an emtpy Hashtable
     */
    private Hashtable getMappingList(SessionManager p_sessionMgr)
    {
        Hashtable mappings = 
            (Hashtable)p_sessionMgr.getAttribute(MapFileProfileToFileHandler.MAPPINGS);
        if (mappings == null)
        {
            mappings = new Hashtable();
            p_sessionMgr.setAttribute(MapFileProfileToFileHandler.MAPPINGS,
                                      mappings);
        }
        return mappings;
    }

    /**
     * Checks to see if all the mappings have been made.
     * If so sets the DONE_MAPPING boolean to TRUE, otherwise FALSE.
     */
    private void setDoneMapping(SessionManager p_sessionMgr)
    {
        ArrayList files =
            new ArrayList((HashSet)p_sessionMgr.getAttribute(SelectFileHandler.FILE_LIST));
        Hashtable mappings = getMappingList(p_sessionMgr);
        int mappingCount = 0;
        Set keys = mappings.keySet();

        // count up all the mappings
        for (Iterator i = keys.iterator() ; i.hasNext(); )
        {
            mappingCount++;
            i.next();
        }            

        // if the number of files and mappings are the same then all have been mapped
        if (files.size() == mappingCount)
        {
            p_sessionMgr.setAttribute(
                MapFileProfileToFileHandler.DONE_MAPPING, new Boolean (true));
        }
        else    // the sizes aren't the same so the mapping hasn't been done
        {
            p_sessionMgr.setAttribute(
                MapFileProfileToFileHandler.DONE_MAPPING, new Boolean (false));
        }
    }

    /**
     * Get the project that the file profile is associated with.
     */
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
            s_logger.error("Failed to get the project that file profile " + p_fp.toString() + " is associated with.",
                           e);
            // just leave and return NULL
        }
        return p;
    }

    /**
     * Decode the file names string from jsp page 
     * and return the file names String[]
     * 
     * @param fileNames
     * @return
     */
    String[] getFileNames(String fileNames)
    {
        String[] fNamesArray = null;
        fNamesArray = fileNames.split(",");
        for (int i = 0; i < fNamesArray.length; i++)
        {
            fNamesArray[i] = URLDecoder.decode(fNamesArray[i], "UTF-8");
        }
        return fNamesArray;
    }
}

