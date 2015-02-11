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
package com.globalsight.cxe.servlet.cms.teamsite;

//globalsight
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.diplomat.javabeans.SortedNameValuePairsBean;
import com.globalsight.diplomat.util.JSPCallingUtility;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;

public class TranslateServlet extends HttpServlet implements SingleThreadModel
{
  private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory.getLogger("TeamSite");

  // error message displayed when there are selected files that are part of on-going jobs
  public static final String filesTranslatingMessage =  "<H1>Cannot Translate Selected Files</H1>\n" +
                                                        "<P> The following files are " +
                                                        "currently being translated.</P>\n" +
                                                        "<TABLE cellspacing=5><TR><TH>JobID</TH><TH ALIGN=Left>Filename</TH></TR>\n";
  public TranslateServlet()
  throws ServletException
  {
  }

  private void dumpRequestValues(HttpServletRequest p_request)
  {
      if (s_logger.isDebugEnabled())
      {
          s_logger.debug("\r\nDumping Request Attributes and Parameter Values from TranslateServlet");
          System.out.println("-------------------------------");
          System.out.println("HTTP request=" + p_request.toString());
          System.out.println("HTTP request URI=" + p_request.getRequestURI());
          System.out.println("HTTP content type=" + p_request.getContentType());

          Enumeration enumeration = p_request.getAttributeNames();
          while (enumeration.hasMoreElements()) {
              String name = (String)enumeration.nextElement();
              Object value = (Object)p_request.getAttribute(name);
              System.out.println("attribute="+name+", value= "+ value);
          }
          enumeration = p_request.getParameterNames();
          while (enumeration.hasMoreElements()) {
              String name = (String)enumeration.nextElement();
              String value = (String)p_request.getParameter(name);
              System.out.println("parameter="+name+", value= "+ value);
          }
      }
  }
  
  public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse)
    throws ServletException, IOException
  {
    dumpRequestValues(theRequest);
    // extract and validate IW user name
    String userName = theRequest.getParameter("user_name");
    if(userName == null)
    {
        String message = "Unknown GlobalSight username. Please add the TeamSite user you are logged in as to GlobalSight and try the import again.";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        return;
    }
    userName = userName.substring(userName.indexOf("\\")+1);
    UserManager USER_MANAGER = null;
    User aUser = null;
    try{
      USER_MANAGER = ServerProxy.getUserManager();
      aUser = USER_MANAGER.getUser(userName);
    }
    catch (Exception e)
    {
        s_logger.error(e);    
    }
    if (aUser == null)
    {
        String message = "Unknown GlobalSight username. Please add the TeamSite user you are logged in as to GlobalSight and try the import again.";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        return;
    }
    
    // For Multi Company
    boolean isSuperPm = false;
    try 
    {
        isSuperPm = ServerProxy.getUserManager()
            .containsPermissionGroup(aUser.getUserId(), WebAppConstants.SUPER_PM_NAME);
    } 
    catch (Exception e) 
    {
        String message = "Failed in user permission\n" + e.getMessage();
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
    } 
    
    String companyId = theRequest.getParameter("companySlct");
    if (companyId != null)
    {
        CompanyThreadLocal.getInstance().setIdValue(companyId);
    }
    else
    {
        CompanyThreadLocal.getInstance().setValue(aUser.getCompanyName());
    }
    
    /*
    * Removing this for now
    * Needs to be rewritten
    // verify access rights
    if(!theUser.hasTranslateAccessRights())
    {
        String message = "You have insufficient access privileges to translate files";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(context, theRequest, theResponse, message);
        return;
    }
    */
    // determine the stage parameter
    String stage = theRequest.getParameter("stage");
    // in the first invokation of the servlet stage is not defined
    if(stage == null)
    {
      try {
        selectTemplate(theRequest, theResponse, isSuperPm);
      } catch (EnvoyServletException ese) {
          String message = "Failed in selecting file profiles\n" + ese.getMessage();
          reportFailure(theRequest, theResponse, message);
          JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
      }
    }
    // the stage is "import"
    else if(stage.equalsIgnoreCase("import"))
    {
      try {
        importFiles(theRequest, theResponse);
      } catch (EnvoyServletException ese) {
          String message = "Failed in importing files\n"+ese;
          reportFailure(theRequest, theResponse, message);
          JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
      }
    }
    else
    { // this is an error
        String message = "Unknown stage specified for file translation";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
    }
  }


  public void doPost(HttpServletRequest theRequest, HttpServletResponse theResponse)
    throws ServletException, IOException
  {
    doGet(theRequest, theResponse);
  }

  /**
   * creates a page that allows the user to select a template file as part of the process of starting a job
   *
   * @param theRequest: the servlet request -- used for the invokation of the JSP used to return error messages
   * @param theResponse: the servlet response -- used for the invokation of the JSP used to return error messages
   *
   * @throws ServletException as a pass-through
   * @throws IOException as a pass-through
   */
  private void selectTemplate(HttpServletRequest theRequest, HttpServletResponse theResponse, boolean isSuperPm)
    throws ServletException, IOException, EnvoyServletException
  {
    String areaPath = theRequest.getParameter("area_path");
    if( areaPath == null || areaPath.indexOf("WORKAREA") < 0 )
    {
      // print error message if user selected "Translate" outside working area.
        String message = "<B>Translate Files</B> operation is only valid in a work area.";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        return;
    }

    // convert TeamSite's file paths to our paths and put them in the convertedFilePaths Vector
    // first, extract the original Teamsite file paths
    Vector originalFilePaths = extractFileProperties(theRequest, theResponse);

    Vector convertedFilePaths = new Vector();
    Enumeration en = originalFilePaths.elements();

    // print error message if the user did not select a file
    if(!en.hasMoreElements())
    {
        String message = "Please select files to translate.";
        reportFailure(theRequest, theResponse, message);
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        return;
    }

    // then, for each file path, convert it to the CAP docs root file path
    while(en.hasMoreElements())
    {
      String originalFilePath = (String) en.nextElement();
      // remove index
      int start = originalFilePath.indexOf("-_-_-");
      convertedFilePaths.add( convertOriginalTSPath(originalFilePath.substring(0,start), theRequest) );
    }


    // build the list of hidden fields and store it in a JavaBean
    SortedNameValuePairsBean hiddenFieldsBean = makeListOfHiddenFields(theRequest);

    // make a list of file profiles
    //Call server
    Vector fileSystemProfiles = null;
    fileSystemProfiles = getFileProfiles();

    /*
    try
    {
        fileSystemProfiles = new Vector (ServerProxy.getFileProfilePersistenceManager().getAllFileProfiles());
    }
    catch (Exception ne)
    {
        throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
    }
    */

    Vector jobNamePrompt = new Vector();
    try
    {
        Iterator it = fileSystemProfiles.iterator();
        while(it.hasNext())
        {
            FileProfile fileProfile = (FileProfile)it.next();
            L10nProfile l10nProfile = ServerProxy.getProjectHandler().getL10nProfile(fileProfile.getL10nProfileId());
            DispatchCriteria criteria = l10nProfile.getDispatchCriteria();
            jobNamePrompt.add(criteria.getCondition()
                              == DispatchCriteria.BATCH_CONDITION
                              ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    catch(Exception e)
    {
        throw new EnvoyServletException(e);
    }
        
    // invoke the JSP that will present the choose template page
    JSPCallingUtility.invokeImportJobJSP(getServletContext(), theRequest,
                                         theResponse,
                                         fileSystemProfiles,
                                         hiddenFieldsBean, jobNamePrompt);

  }
  
  private Vector getFileProfiles()
  throws ServletException, EnvoyServletException
  {
      try
      {
          // Store the companys which has Teamsite Server
          HashSet companyIdSet = new HashSet();
          Collection tss = ServerProxy.getTeamSiteServerPersistenceManager()
                                      .getAllTeamSiteServers();
          Iterator it = tss.iterator();
          while (it.hasNext())
          {
              companyIdSet.add(((TeamSiteServer)it.next()).getCompanyId());
          }

          // Select fileProfiles
          Collection fileProfiles = 
              ServerProxy.getFileProfilePersistenceManager()
                         .getAllFileProfiles();
          it = fileProfiles.iterator();
          while (it.hasNext())
          {
              String fpCompanyId = ((FileProfile)it.next()).getCompanyId();
              if (!companyIdSet.contains(fpCompanyId))
              {
                  it.remove();
              }
          }
          
          return new Vector(fileProfiles);
      }
      catch (Exception ne)
      {
          throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
      }
  }


  /**
   * Creates a JavaBean (SortedNameValuePairsBean) with all the name and values that will
   * go in the import page as hidden fields.
   */
  private SortedNameValuePairsBean makeListOfHiddenFields(HttpServletRequest theRequest)
    throws ServletException
  {
    SortedNameValuePairsBean hiddenFieldsBean = new SortedNameValuePairsBean();
    Enumeration e = theRequest.getParameterNames();
    while(e.hasMoreElements())
    {
      String parameterName = (String) e.nextElement();
      String parameterValue = theRequest.getParameter(parameterName);
      hiddenFieldsBean.addKeyValuePair(parameterName, parameterValue);
    }
    // add a last hidden field with a stage tag to indicate that we are done with the template
    // selection and we must perform the file import
    hiddenFieldsBean.addKeyValuePair("stage", "import");
    return hiddenFieldsBean;
  }


 /**
   * This method extracts all the IW file paths in the HttpServletRequest. If a one of the selections was
   * a directory, the method recursively collects all the files it contains.
   *
   * @theRequest the original HttpServlet request from TeamSite.
   * @theResponse the HttpServlet response.
   *
   * @returns a list of all the file paths submitted form the Teamsite GUI.
   */
  private Vector extractFileProperties(HttpServletRequest theRequest, HttpServletResponse theResponse)
    throws ServletException
  {
    // iterate over all the file names in the HttpServletRequest
    RE pathRegExp = null;
    try
    {
      pathRegExp = new RE("path_([0-9]+)");
    }
    catch(RESyntaxException exc)
    {
        s_logger.error(exc);
      // do something???? ********************************
    }

    // create the list of files with converted paths
    Vector convertedFilePathList = new Vector();
    Enumeration e = theRequest.getParameterNames();
    while(e.hasMoreElements())
    {
      String parameterName = (String) e.nextElement();
      String parameterValue = (String) theRequest.getParameter(parameterName);
      // look for an index in the parameter name
      boolean result = pathRegExp.match(parameterName);
      if(result)
      {
        // find the index for this path
        String index = pathRegExp.getParen(1);
        // post-pend index to end file after "-_-_-" to use for recovering filesize
        String fileName = parameterValue+"-_-_-"+index;
        convertedFilePathList.add(fileName);
      }
    }
    return convertedFilePathList;
  }

  /**
   * converts a file from the Teamsite file path to the GlobalSight file path
   *
   * @param tsFileName the absolute path of a teamsite file
   * @param theRequest the original HttpServletRequest received by this servlet
   *
   * @return the converted file path of the Teamsite file into the GlobalSight file path
   */
  private String convertOriginalTSPath(String tsFileName, HttpServletRequest theRequest)
    throws EnvoyServletException
  {
      String convertedFilePath = "";
      try
      {
          // The root path for the Teamsite file system
          String archivePath = theRequest.getParameter("archive_path");
          archivePath = decodeSpacesInPath(archivePath);
          // set up TSLink for GlobalSight docs directory
          String tsLink;
          int index = archivePath.indexOf("/");
          if (index == 0)
            tsLink = archivePath.substring(1);  // remove leading slash "/"
          else
            tsLink = archivePath;               // no leading slash "/"
          // the root path for the branch
          String iwDocRoot = decodeSpacesInPath(theRequest.getParameter("area_path"));

          s_logger.debug("tsFileName=" + tsFileName);
          s_logger.debug("TSLink=" + tsLink);
          s_logger.debug("archive path=" + archivePath);
          s_logger.debug("iwDocRoot=" + iwDocRoot);

          // remove the archive path from the teamsite file name
          String partialTSFilePath = tsFileName.substring( archivePath.length() );
          s_logger.debug("partialTSFilePath=" + partialTSFilePath);
          // prepare path relative to GlobalSight document directory
          convertedFilePath = File.separator + "TSLink" + partialTSFilePath;
          s_logger.debug("convertedFilePath=" + convertedFilePath);
      }
      catch(EnvoyServletException e)
      {
          s_logger.error(e);
          throw new EnvoyServletException(e);
      }
    return convertedFilePath;
  }

  /**
   * This method notifies an adapter about importing files
   */
  private void importFiles(HttpServletRequest theRequest, HttpServletResponse theResponse)
    throws ServletException, EnvoyServletException
  {
    // extract all the TeamSite's file paths from the HTTP request
    Vector TSFilePaths = extractFileProperties(theRequest, theResponse);

    String userName = theRequest.getParameter("user_name");
    s_logger.debug("Username from theRequest.getparameter() call: " + userName);

    // determine the job name from the Http request
    String jobName = EditUtil.utf8ToUnicode(theRequest.getParameter("jobName"));

    if(jobName == null)
    {
        try
        {
            String message = "The job name field is empty.";
            reportFailure(theRequest, theResponse, message);
            JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        }
        catch(IOException exc1)
        {s_logger.error(exc1);}
        return;
    }
    long fpId = 0;
    String fileProfileId = "";

    String autoImport = theRequest.getParameter("autoImport");
    if(autoImport != null)
    {
    if(autoImport.equals("autoImport"))
    {
        String fileProfileName = theRequest.getParameter("fileProfile");
        Vector fileSystemProfiles = getFileProfiles();
        try
        {
        Iterator it = fileSystemProfiles.iterator();
        boolean found = false;
        while(it.hasNext())
        {
            FileProfile fileProfile = (FileProfile)it.next();
            String fpName = fileProfile.getName();
            if(fileProfileName.equals(fpName))
            {
            fpId = fileProfile.getId();
            Long filePId = new Long(fpId);
            fileProfileId = filePId.toString();
            found = true;
            break;
            }
        }
        if(!found)
        {
            s_logger.debug("TranslateServlet::Error:invalid file profile name"
                    + fileProfileName);
        }
        }
        catch(Exception e)
        {
            s_logger.error(e);
            throw new EnvoyServletException(e);
        }
    }
    }
    else
    {
    // determine the file system profile ID
    fileProfileId = theRequest.getParameter("fileProfile");
    if(fileProfileId == null)
    {
        try
        {
                String message = "File profile selection is missing.";
                reportFailure(theRequest, theResponse, message);
                JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        }
        catch(IOException exc1)
        {s_logger.error(exc1);}
        return;
    }
    fpId = new Integer(fileProfileId).longValue();

    }
    // From file profile, determine a vector of valid file extensions
    Vector extensions = new Vector();
    Vector extensionIds = new Vector();
    try {
      FileProfile fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(fpId);
      extensionIds = fp.getFileExtensionIds();
      Long extensionId;
      Enumeration en = extensionIds.elements();
      while(en.hasMoreElements()) {
        extensionId = (Long) en.nextElement();
        long fextId = extensionId.longValue();
        FileExtension fext = ServerProxy.getFileProfilePersistenceManager().readFileExtension(fextId);
        extensions.add(fext.getName());
      }
        }
        catch (NamingException ne)
        {
            s_logger.error(ne);
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            s_logger.error(re);
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            s_logger.error(ge);
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }

    // XXX RGL - dummy data to be removed from publishEventToCxe
    String localizationProfileID = "6";
    String locale = "es_ES";
    String characterSet = "ISO-8859";

    // create a unique batch ID common for all the files
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyDDDkkmmssSSSzzz");
    // the unique ID depends on the current date/time/time zone
    String batchId = "batch_" + sdf.format(new java.util.Date());

    boolean nothingPublished = true;
    String MessageJSP_msg = "The selected files have been routed to Welocalize GlobalSight";
    // notify the Adapter object for each file sent for translation
    try
    {
      // count the number of files with the proper extensions <=> pageCount
      Enumeration en = TSFilePaths.elements();
      int pageNumber = 0;                     // pages are 1-based
      while(en.hasMoreElements())
      {
        // take each file path and convert it to the CAP file system
        String originalTSFilePath = (String) en.nextElement();
        originalTSFilePath = decodeSpacesInPath(originalTSFilePath);
        // remove index
        int start = originalTSFilePath.indexOf("-_-_-");
        String convertedFilePath = this.convertOriginalTSPath(originalTSFilePath.substring(0,start), theRequest);

        boolean properFileExtension = false;
        properFileExtension = compareToExtensions(originalTSFilePath.substring(0,start), extensions.elements());
        if (properFileExtension) pageNumber++;

      }

      en = TSFilePaths.elements();
      int pageCount = pageNumber;       // the total number of files being sent to CAP
      pageNumber = 1;               // pages are 1-based

      while(en.hasMoreElements())
      {
        // take each file path and convert it to the CAP file system
        s_logger.debug("TranslateServlet.importFiles() pageNumber " + pageNumber);
        String originalTSFilePath = (String) en.nextElement();
        int start = originalTSFilePath.indexOf("-_-_-");
        String fileSize = theRequest.getParameter("size_"+originalTSFilePath.substring(start+5));
        s_logger.debug("TranslateServlet.importFiles() fileSize "+fileSize);
        originalTSFilePath = decodeSpacesInPath(originalTSFilePath);
        s_logger.debug( "Original file path" + originalTSFilePath);
        // remove index
        start = originalTSFilePath.indexOf("-_-_-");
        String convertedFilePath = this.convertOriginalTSPath(originalTSFilePath.substring(0,start), theRequest);
        s_logger.debug( "converted file path " + convertedFilePath);

        boolean properFileExtension = false;
        properFileExtension = compareToExtensions(originalTSFilePath.substring(0,start), extensions.elements());
        s_logger.debug("is properFileExtension? " + properFileExtension);
        // find out the teamsite server name
        String serverName = theRequest.getParameter("teamsite_server");
        String storeName = theRequest.getParameter("archive_name");
        String taskId = theRequest.getParameter("task_id");
        String overwriteSource = theRequest.getParameter("overwriteSource");
        String callbackImmediately = theRequest.getParameter("callbackImmediately");

        s_logger.debug("TranslateServlet.importFiles() serverName "+ serverName);
        s_logger.debug("TranslateServlet.importFiles() storeName "+ storeName);
        s_logger.debug("TranslateServlet.importFiles() taskId "+ taskId);
        s_logger.debug("TranslateServlet.importFiles() overwriteSource "+ overwriteSource);
        s_logger.debug("TranslateServlet.importFiles() callbackImmediately "+ callbackImmediately);

        if (properFileExtension) {
          nothingPublished = false;
          publishEventToCxe(originalTSFilePath.substring(0,start),
                            convertedFilePath,
                            new Integer(fileSize).intValue(),
                            locale, characterSet,
                            batchId, pageNumber,
                            pageCount, fileProfileId,
                            localizationProfileID, jobName,
                            userName, serverName,
                            storeName, taskId,
                            overwriteSource, callbackImmediately);
          pageNumber++;
        }
      }

      if (nothingPublished) {
        try {
            String message = "No files selected have an extension allowed by the File Profile";
            reportFailure(theRequest, theResponse, message);
            JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, message);
        }
        catch(IOException exc1) {s_logger.error(exc1);}
        return;
      }
    }
    catch(Exception exc)
    {
        s_logger.error(exc);
      try
      {
          String message = "The selected files were not routed to Welocalize GlobalSight";
          reportFailure(theRequest, theResponse, message);
          JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse,message );
      }
      catch(IOException exc1)
      {s_logger.error(exc1);}
      return;
    }

    try
    {
        JSPCallingUtility.invokeMessageJSP(getServletContext(), theRequest, theResponse, MessageJSP_msg);
    }
    catch(IOException exc2)
    {s_logger.error(exc2);}
  }

  /**
   *  Compares a file extension with the allowed list from File System Profile
   */
  private boolean compareToExtensions(String filePath, Enumeration enp) {
      if (enp.hasMoreElements() == false)
          return true; //return true if there are no extensions for the file profile

    boolean properFileExtension = false;

    // check that file extension is in File system profile list of extensions
    String extension;
    String extensionU;
    String fileName = filePath.substring(filePath.lastIndexOf("/"));
    int indexOfExtension = fileName.lastIndexOf(".");
    s_logger.debug("indexOfExtension is " + indexOfExtension );
    if (indexOfExtension == -1) {
      properFileExtension = true;
    }
    else {
        while(enp.hasMoreElements()) {
          extension = (String) enp.nextElement();
          extensionU = extension.toUpperCase();
          if (fileName.toUpperCase().endsWith(extensionU)) {
            properFileExtension = true;
            break;
          }
        }
    }
    return properFileExtension;
  }


  /**
   *  Creates a TeamsiteSelectedFileEvent and publishes it to CXE
   */
  private void publishEventToCxe(String p_originalFilename,
                                    String p_convertedFileName,
                                    int p_fileSize,
                                    String p_locale,
                                    String p_charset,
                                    String p_batchId,
                                    int p_pageNum,
                                    int p_pageCount,
                                    String p_fileProfileId,
                                    String p_localizationProfile,
                                    String p_jobName,
                                    String p_userName,
                                    String p_serverName,
                                    String p_storeName,
                                    String p_taskId,
                                    String p_overwriteSource,
                                    String p_callbackImmediately
                                 ) throws Exception
  {
      s_logger.info("Requesting file from teamsite" + p_originalFilename);
      CxeProxy.importFromTeamSite(p_originalFilename,
                                  p_convertedFileName,
                                  p_fileSize,
                                  p_locale,
                                  p_charset,
                                  p_batchId,
                                  p_pageNum,
                                  p_pageCount,
          1,
          1,
                                  p_fileProfileId,
                                  p_localizationProfile,
                                  p_jobName,
                                  p_userName,
                                  p_serverName,
                                  p_storeName,
                                  p_taskId,
                                  p_overwriteSource,
                                  p_callbackImmediately,
                                  CxeProxy.IMPORT_TYPE_L10N
                                  );
  }
  private String decodeSpacesInPath(String p_areaPath)
      throws EnvoyServletException
  {
      String pattern = "%%space%%";
      String replacement = " ";
      String all = "";

      try
      {
          RE modify = new RE( pattern );
          all = modify.subst( p_areaPath, replacement );
      }
      catch (Exception e)
      {
          s_logger.error(e);
          throw new EnvoyServletException(e);
      }
      return all;
  }
  /**
   * In case of an error report it back to teamsite
   **/
  private void reportFailure(HttpServletRequest theRequest,
                             HttpServletResponse theResponse,
                             String p_message)
  {
      try
      {
          s_logger.debug("reportFailure message:" + p_message);
          String serverName = theRequest.getParameter("teamsite_server");
          String taskId = theRequest.getParameter("task_id");
          if(taskId != null )
          {
              Integer tid = new Integer(taskId);
              if(tid.intValue() > 0)
              {
                  String overwriteSource = theRequest.getParameter("overwriteSource");
                  String callbackImmediately = theRequest.getParameter("callbackImmediately");
                  TeamSiteServer ts = (TeamSiteServer)ServerProxy.
                      getTeamSiteServerPersistenceManager().getTeamSiteServerByName(serverName);

                  String session = TeamSiteExchange.getTeamsiteSession(ts);
                  if (session == null)
                  {
                     session = URLEncoder.encode("teamsite_session");
                  }
                  
                  URL tsURL = null;
                  if (ts.getOS().equalsIgnoreCase("nt"))
                  {
                      tsURL = new URL(TeamSiteExchange.TS_HTTP+ts.getName()+":"+
                                      ts.getExportPort()+TeamSiteExchange.TS_IMPORT_NT);
                  }
                  else if (ts.getOS().equalsIgnoreCase("unix"))
                  {
                      tsURL = new URL(TeamSiteExchange.TS_HTTP+ts.getName()+":"+
                                      ts.getExportPort()+TeamSiteExchange.TS_IMPORT_UNIX);
                  }

                  HttpURLConnection tsConnection = (HttpURLConnection)tsURL.openConnection();
                  tsConnection.setRequestMethod("POST");
                  tsConnection.setDoOutput(true);

                  PrintWriter out = new PrintWriter(
                      new OutputStreamWriter(tsConnection.getOutputStream()));
                  if(taskId != null && !taskId.equals("null") )
                  {
                      out.print("taskId=" + URLEncoder.encode(taskId) + "&");
                  }
                  if(callbackImmediately != null && !callbackImmediately.equals("null") )
                  {
                      out.print("callbackImmediately=" + URLEncoder.encode(callbackImmediately) + "&");
                  }
                  out.print("teamsiteJobState=" + URLEncoder.encode("IMPORT_FAILED") + "&");
                  out.print("teamsiteMessage=" + URLEncoder.encode(p_message) + "&");
                  out.print("session=" + session + "&");
                  out.print("isimport=" + URLEncoder.encode("2") + "&");
                  out.print("sourceLocale=" + URLEncoder.encode("en_US"));
                  out.print("\n\n");
                  out.close();
                  BufferedReader in = new BufferedReader (new InputStreamReader(
                                                                               tsConnection.getInputStream()));
                  String inputLine = null;
                  String lastLine = null;
                  StringTokenizer st;

                  while ((inputLine = in.readLine()) != null)
                  {
                      lastLine = inputLine;
                  }
                  //now check the LastLine to see what the returned status from the
                  //Teamsite Integration CGI script was
                  st = new StringTokenizer(lastLine,"=");
                  st.nextToken(); //skip status
                  String status = st.nextToken();
                  in.close();
              }
          }
          else
          {
              return;
          }
      }
      catch (Exception e)
      {
          s_logger.error(e);
          // nothing
      }
  }
}
