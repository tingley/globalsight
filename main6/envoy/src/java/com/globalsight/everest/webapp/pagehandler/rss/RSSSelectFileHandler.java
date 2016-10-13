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
package com.globalsight.everest.webapp.pagehandler.rss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.imp.ImportFileFilter;
import com.globalsight.everest.webapp.pagehandler.administration.imp.MapFileProfileToFileHandler;
import com.globalsight.everest.webapp.pagehandler.administration.imp.SetAttributeHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

// Create the File Selection screen for manual import of files
public class RSSSelectFileHandler extends PageHandler {
    // static variables

    private static final Logger CATEGORY = Logger
            .getLogger(RSSSelectFileHandler.class.getName());

    // values stored in the session to pass info between pages
    // or use in the jsp
    public static final String BATCH_CONDITION = "batchCondition";

    public static final String FILE_LIST = "fileList";

    public static final String ATTR_SUGGEST_JOB_NAME = "suggestJobName";

    public static final String FOLDER_SELECTED = "currentFolder";

    public static final String JOB_NAME = "jobName";

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_pageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException 
    {
        addSuggestJobNameAttribute(p_request);

        SessionManager sessionMgr = (SessionManager) p_request
                .getSession(false).getAttribute(SESSION_MANAGER);
        // Clicking on the folder sends the parameter via the request
        // object. Get it and put on the session.
        HttpSession session = p_request.getSession(true);

        //if it is a cvs job,the jobType will be "cvsJob",
        //Which needs to be exported automatically.
        /**
        String jobType = p_request.getParameter("jobType");
        if (jobType != null && !"".equals(jobType.trim())) {
        	sessionMgr.setAttribute("jobType", jobType);
        }
        */
        try {
//            String action = p_request.getParameter("pageAction");
//            if (action != null) {
//                if (CANCEL.equals(action)) {
//                    // clear out the session to start again
//                    sessionMgr.clear();
//                } else if (IMPORT.equals(action)) {
//                    // gather the data for importing  and perform the import
//                	p_request.setAttribute("nextPageFlag", 
//                              p_request.getParameter("nextPageFlag"));
//                    checkForFileImportData(p_request);
//                }
//            }
            
            String folderSelected = p_request.getParameter(FOLDER_SELECTED);
            if (folderSelected != null)
            {
                try
                {
                    folderSelected = URLDecoder.decode(folderSelected, "UTF-8");
                }
                catch(Exception e)
                {
                    CATEGORY.error("Failed to decode folderSelected: " + folderSelected, e);
                }
            }

            if (p_request.getParameter(FOLDER_SELECTED) != null) {
                session.setAttribute(FOLDER_SELECTED, folderSelected);
            }

            // set dispatch criteria to batch
            sessionMgr.setAttribute(BATCH_CONDITION, new Boolean(true));

            // the selected file listing.
            prepareFileList(p_request, sessionMgr, null);

        } catch (GeneralException ge) {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    public static String getRelativePath(File p_parent, File p_absolute) {
        String parent;

        if (p_parent.getPath().endsWith(File.separator))
            parent = p_parent.getPath();
        else
            parent = p_parent.getPath() + File.separator;

        String absolute = p_absolute.getPath();

        return absolute.substring(parent.length());
    }

    public static String getAbsolutePath(String p_absolute) {
        return RSSUtil.getStorageRoot() + p_absolute;
    }

    public static Vector getFileExtensionsByFileProfile(
            FileProfile p_fileProfile) throws EnvoyServletException {
        try {
            return new Vector(ServerProxy.getFileProfilePersistenceManager()
                    .getFileExtensionsByFileProfile(p_fileProfile));
        } catch (Exception e) {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

//    public static File getCXEBaseDir() {
//        try {
//            SystemConfiguration config = SystemConfiguration.getInstance();
//            return new File(config
//                    .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR));
//        } catch (Exception e) {
//            CATEGORY.error(e.getMessage(), e);
//            throw new RuntimeException(e.getMessage());
//        }
//    }
    
    public static File getFileStorageDir() 
    {
        return new File(AmbFileStoragePathUtils.getFileStorageDir().getAbsoluteFile() + File.separator + "RSS Docs" + File.separator);
    }

    private void recurseFileStructures(File p_baseFile,
            FileProfile p_fileProfile, SessionManager p_sessionMgr)
            throws EnvoyServletException {
        // we don't filter by file profiles anymore - so just replace by empty Vector.
        // will leave this call here in case there is some sort of filtering that is needed
        // in the near future
        File[] directoryContent = p_baseFile.listFiles(new ImportFileFilter(
                new Vector()));

        if (directoryContent != null) {
            for (int i = 0; i < directoryContent.length; i++) {
                File doc = (File) directoryContent[i];

                if (doc.isDirectory())
                {
                    recurseFileStructures(doc, p_fileProfile, p_sessionMgr);
                }
                else
                {
                    getFileList(p_sessionMgr).add(getRelativePath(
                           getFileStorageDir(), doc));
                }
            }
        }
    }
 
    // obtain the file list and add or remove the files selected from the selected list.
    private void prepareFileList(HttpServletRequest p_request, 
            SessionManager p_sessionMgr, FileProfile p_fileProfile)
            throws EnvoyServletException {
        HashSet fileList = getFileList(p_sessionMgr);
        String fileAction = (String) p_request.getParameter("fileAction");
        if (fileAction != null) {
            // All the checkboxes are named "file" so the "file" parameter value
            // comes back as an array.
            String files[] = p_request.getParameterValues("file");
            String file = null;

            if (fileAction.equals("add")) {
                // Add the selected files to the import list.
                for (int i = 0; i < files.length; i++) {
                    try
                    {
                        file = URLDecoder.decode(files[i], "UTF-8");
                    }
                    catch(Exception e)
                    {
                        CATEGORY.error("Failed to decode fileName: " + files[i], e);
                    }
                    File addFile = new File(getFileStorageDir(), file);
                    if (addFile.isDirectory()) {
                        recurseFileStructures(addFile, p_fileProfile,
                                p_sessionMgr);
                    } else {
                        fileList.add(file);
                    }
                }
            } else if (fileAction.equals("remove")) {
                // Remove files from the import list.
                for (int i = 0; i < files.length; i++) {
                    try
                    {
                        file = URLDecoder.decode(files[i].toString(), "UTF-8");
                    }
                    catch(Exception e)
                    {
                        CATEGORY.error("Failed to decode fileName: " + files[i], e);
                    }
                    fileList.remove(file);
                }
            }
        }
        ImportUtil.modifyFilesToUTF8(fileList);
    }


	// return the file list that is stored in the session
    // create it if it hasn't been created yet
    private HashSet getFileList(SessionManager p_sessionMgr) {
        HashSet fileList = (HashSet) p_sessionMgr.getAttribute(FILE_LIST);

        if (fileList == null)
            p_sessionMgr.setAttribute(FILE_LIST, new HashSet());

        return (HashSet) p_sessionMgr.getAttribute(FILE_LIST);
    }

    /**
     * Adds the envoy.properties value of import.suggestJobName
     * to the request
     *
     * @param p_request
     */
    private void addSuggestJobNameAttribute(HttpServletRequest p_request) {
        Boolean suggestJobName = Boolean.TRUE;
        try {
            SystemConfiguration config = SystemConfiguration.getInstance();
            String s = config
                    .getStringParameter(SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME);
            if ("false".equalsIgnoreCase(s)) {
                suggestJobName = Boolean.FALSE;
            }
        } catch (Exception e) {
            CATEGORY.warn("Could not read envoy.properties value for "
                    + SystemConfigParamNames.IMPORT_SUGGEST_JOB_NAME, e);
        }

        p_request.setAttribute(ATTR_SUGGEST_JOB_NAME, suggestJobName);
    }

    //////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  FILE IMPORT DATA RETRIEVAL  /////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private void checkForFileImportData(HttpServletRequest p_request)
            throws IOException, EnvoyServletException {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String jobType = (String) sessionMgr.getAttribute("jobType");
        sessionMgr.removeElement("jobType");
        
        //Allow use to select target locales while creating jobs
        String[] targetLocaleIds = p_request.getParameterValues("targetLocales");
        ArrayList targetLocalesList = new ArrayList();
        if (targetLocaleIds != null && targetLocaleIds.length > 0) {
        	for (int i = 0; i<targetLocaleIds.length; i++) {
        		GlobalSightLocale gsl = ServerProxy.getLocaleManager().getLocaleById(Long.parseLong(targetLocaleIds[i]));
        		String loaleName = gsl.getLocale().toString();
        		targetLocalesList.add(loaleName);
        	}
        }

        // 1. obtain the mapped file names to be imported.
        Hashtable fileList = (Hashtable) sessionMgr
                .getAttribute(MapFileProfileToFileHandler.MAPPINGS);
        
        // for linux path issue.
        Hashtable mappedFiles = new Hashtable();
        Set fileNames = fileList.keySet();
        Iterator iterator = fileNames.iterator();
        while (iterator.hasNext())
        {
            String fileName = (String) iterator.next();
            FileProfile fp = (FileProfile) fileList.get(fileName);
            
            fileName = fileName.replace('\\', File.separatorChar);
            mappedFiles.put(fileName, fp);
        }
        sessionMgr.setAttribute(MapFileProfileToFileHandler.MAPPINGS, mappedFiles);      
        
        // 2. job name
        String jobName = EditUtil.utf8ToUnicode(p_request
                .getParameter(JOB_NAME));
        
        String uuid = (String) sessionMgr.getAttribute("uuid");
        Map<String, JobAttribute> jobAttributes = (Map<String, JobAttribute>) sessionMgr
                .getAttribute(SetAttributeHandler.JOB_ATTRIBUTES);
        if (jobAttributes != null && uuid != null)
        {
            List<JobAttribute> jobAtts = new ArrayList<JobAttribute>();
            jobAtts.addAll(jobAttributes.values());
            RuntimeCache.addJobAtttibutesCache(uuid, jobAtts);
        }
        
        // 3. send off to be imported
//        sendToCxe(mappedFiles, jobName, user.getUserId());
        sendToCxe(mappedFiles, jobName, uuid, user.getUserId(), targetLocalesList, jobType);
        
        // 4.
        if (jobAttributes != null)
        {
            String companyId = CompanyThreadLocal.getInstance().getValue();
            AddJobAttributeThread thread = new AddJobAttributeThread(uuid,
                    companyId);
            List<JobAttribute> jobAtts = new ArrayList<JobAttribute>();
            jobAtts.addAll(jobAttributes.values());
            thread.setJobAttributes(jobAtts);
            thread.createJobAttributes();
        }
        
        // 5. clean up the session.
        sessionMgr.clear();
    }
    
    /**
     * Creates a batch for the given set of files and tells CXE
     * to import them in manual mode.
     *
     * @param p_mappedFiles  A hashtable with all the files to be imported mapped to the
     *                       file profile it should be associated with.
     *                       The key is the file name and the value is the file profile.
     * @param p_jobName  job name
     * @exception EnvoyServletException
     */
    private static void sendToCxe(Hashtable p_mappedFiles, String p_jobName, String uuid,
            String p_importInitiatorId, List p_targetLocalesList, String p_jobType) 
    	throws EnvoyServletException {

        try {
            Set fileNames = p_mappedFiles.keySet();
            Date d = new Date();
            String batchId = p_jobName + Long.toString(d.getTime());

            Hashtable files2FpId = new Hashtable(); //hold the scripted files to fileprofile ids.
            Hashtable files2ExitValue = new Hashtable(); // hold the scripted files to exit values.
            for (Iterator i = fileNames.iterator(); i.hasNext();) 
            {
                String fileName = (String) i.next();
                FileProfile fp = (FileProfile) p_mappedFiles.get(fileName);
                
                //For "Import&Export script" issue. 
                //Add a script process before sending the files to create a job.
                String scriptOnImport = fp.getScriptOnImport();
                int exitValue = 0;
                if (scriptOnImport != null && scriptOnImport.length() > 0)
                {
                	String scriptedDir = fileName.substring(0, fileName.lastIndexOf("."));
            		String scriptedFolderPath = getFileStorageDir().getAbsolutePath()
        					+ File.separator + scriptedDir;
            		File scriptedFolder = new File(scriptedFolderPath);
            		if (!scriptedFolder.exists())
            		{
            			File file = new File(fileName);
                    	String filePath = getFileStorageDir().getAbsolutePath() 
                    			+ File.separator + file.getParent();
                    	//Call the script on import to convert the file
                    	try
                		{
                    		String cmd = "cmd.exe /c " + scriptOnImport + " \"" + filePath + "\"";
                    		// If the script is Lexmark tool, another parameter -encoding is passed.
                    		if ("lexmarktool.bat".equalsIgnoreCase(new File(
									scriptOnImport).getName()))
							{
								cmd += " \"-encoding " + fp.getCodeSet() + "\"";
							}
    						Process process = Runtime.getRuntime().exec(cmd);
    						BufferedReader reader = new BufferedReader(
    								new InputStreamReader(process.getInputStream()));
    						String line = "";
    						while((line = reader.readLine()) != null)
    						{
    							//just read the output.
    						}
    						
    						BufferedReader error_reader = new BufferedReader(
    								new InputStreamReader(process.getErrorStream()));
    						String error_line = "";
    						while((error_line = error_reader.readLine())!=null)
    						{
    							//just read the output.
    						}
    						CATEGORY.info("Script on Import " + scriptOnImport + " was called: \n");
    						exitValue = process.exitValue();
                		}
                		catch (Exception e)
                        {
                			//Set exitValue to 1 if the file was not scripted correctly.
                			exitValue = 1;
                			CATEGORY.error("The script on import was not executed successfully.");
                        }
            		}
            		//Iterator the files converted by the script and import each one of them.
            		if (scriptedFolder.exists())
            		{
            			String scriptedFiles[] = scriptedFolder.list();
                		if (scriptedFiles != null && scriptedFiles.length > 0)
                		{
                			for (int j = 0; j < scriptedFiles.length; j++)
                			{
                				String scriptedFileName = scriptedFiles[j];
                				String fileProfileId = Long.toString(fp.getId());
                				String key_fileName = 
                					scriptedDir + File.separator + scriptedFileName;
                				files2FpId.put(key_fileName, fileProfileId);
                				files2ExitValue.put(key_fileName, new Integer(exitValue));
                			}
                		}
                		else //there are no scripted files in the folder
                		{
                			files2FpId.put(fileName, Long.toString(fp.getId()));
                			files2ExitValue.put(fileName, new Integer(exitValue));
                		}
            		}
            		else //the corresponding folder was not created by the script.
            		{
            			files2FpId.put(fileName, Long.toString(fp.getId()));
            			files2ExitValue.put(fileName, new Integer(exitValue));
            		}
                }
                else //there is no script on import for the file profile. normal case.
                {
                	files2FpId.put(fileName, Long.toString(fp.getId()));
                	files2ExitValue.put(fileName, new Integer(0));
                }
            }
            if (!files2FpId.isEmpty())
            {
            	sendToCxe2(files2FpId, files2ExitValue, 
            			p_jobName, uuid, batchId, p_importInitiatorId,
            			p_targetLocalesList, p_jobType);
            }
        } catch (Exception e) 
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Sends all the files including the scripted ones to Cxe for importing.
     *
     * @param p_files2FpId  A hashtable with all the files including scripted files to be imported.
     *                      The key is the file name and the value is the file profile id.
     * @param files2ExitValue A hashtable with all the files including scripted files to be imported.
     *                        The key is the file name and the value is the exit value of script result.
     * @param p_jobName  job name
     * @param p_batchId  job's batch id
     * @param p_initiatorId
     *                   The user id of the user who initiated the import for this file.
     * @throws Exception 
     * 
     */
	private static void sendToCxe2(Hashtable p_files2FpId,
			Hashtable p_files2ExitValue, String p_jobName, String uuid, String p_batchId,
			String p_initiatorId, List p_targetLocalesList, String p_jobType) throws Exception
	{
		CATEGORY.info("---Sending to CXE the following for job " + p_jobName
				+ " : " + p_files2FpId.toString() + "---");
		
		Set fileNames = p_files2FpId.keySet();
		Integer pageCount = new Integer(fileNames.size());

		int count = 0;
		for (Iterator i = fileNames.iterator(); i.hasNext();)
		{
			count++;
			String fileName = (String) i.next();
			String fileProfileId = (String) p_files2FpId.get(fileName);
			Integer exitValue = (Integer) p_files2ExitValue.get(fileName);
			
			String key = p_batchId + fileName + count;
			StringBuffer sb = new StringBuffer();
			
			for (int j=0; j<p_targetLocalesList.size(); j++) {
				String targetLocale = (String) p_targetLocalesList.get(j);
				if (sb.length() == 0) {
					sb.append(targetLocale);
				} else {
					sb.append("," + targetLocale);
				}
			}
			CxeProxy.setTargetLocales(key, sb.toString());
			
			boolean isAutoImport = false;
			if (p_jobType != null && "cvsJob".equalsIgnoreCase(p_jobType)) {
				isAutoImport = true;
			}
			
			CxeProxy.importFromFileSystem(fileName, p_jobName, uuid, p_batchId,
					fileProfileId, pageCount, new Integer(count),
					new Integer(1), new Integer(1), isAutoImport,
					CxeProxy.IMPORT_TYPE_L10N, p_initiatorId, exitValue);
		}
	}
}
