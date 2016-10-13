package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

public class SelectRecentFiles extends PageHandler
{
    
    private static final Logger logger = Logger
            .getLogger(SelectRecentFiles.class);
    
    private Map<String, File> fileMap = null;
    
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        // permission check
        HttpSession session = request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.CREATE_JOB) &&
        		!userPerms.getPermissionFor(Permission.CREATE_JOB_NO_APPLET))
        {
            logger.error("User doesn't have the permission to see the page.");
            response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        
        String action = request.getParameter("action");
        if (action != null && action.equals("getSwitchFiles"))
        {
            this.getSwitchFiles(request, response);
            return;
        }
        else if (action != null && action.equals("deleteFile"))
        {
            this.deleteServerFiles(request, response);
        }
        
        fileMap = new HashMap<String, File>();
        // show words
        ResourceBundle bundle = PageHandler.getBundle(session);
        setLable(request, bundle);
        // get just uploaded files
        String currentFolderName = request.getParameter("currentFolderName");
        File current = new File(convertFilePath(AmbFileStoragePathUtils
                .getCxeDocDir().getPath())
                + File.separator
                + CreateJobsMainHandler.TMP_FOLDER_NAME
                + File.separator
                + currentFolderName);
        List<File> lastUploaded = null;
        if (current.exists())
        {
            lastUploaded = FileUtil.getAllFilesAndFolders(current, false);
        }
        // get all files
        String tmpPath = AmbFileStoragePathUtils.getCxeDocDir()
                + File.separator + CreateJobsMainHandler.TMP_FOLDER_NAME;
        request.setAttribute("currentFolderName", currentFolderName);
        request.setAttribute("path", request.getContextPath());
        File baseFolder = new File(tmpPath);
        if (baseFolder != null && baseFolder.exists())
        {
            // delete empty folders
            File[] tmp = baseFolder.listFiles();
            for (File t : tmp)
            {
                if (FileUtil.isEmpty(t))
                {
                    FileUtil.deleteFile(t);
                }
            }
            // files should not contain files uploaded on this page
            List<File> recentFiles = FileUtil.getAllFilesAndFolders(baseFolder,
                    false);
            if (lastUploaded != null)
            {
                recentFiles.removeAll(lastUploaded);
            }
            List<FileSystemEntity> files = new ArrayList<FileSystemEntity>();
            for (File aFile : recentFiles)
            {
                files.add(new FileSystemEntity(aFile, tmpPath));
                if (aFile.isFile())
                {
                    fileMap.put(FileUtil.getFileNo(aFile.getPath()), aFile);
                }
            }
            Collections.reverse(files);
            request.setAttribute("recentFiles", files);
        }
        
        super.invokePageHandler(pageDescriptor, request, response, context);
    }
    
    /**
     * Delete files that were uploaded ago.
     * @param request
     * @param response 
     * @throws IOException 
     */
    private void deleteServerFiles(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        try
        {
            String[] fileIds = request.getParameterValues("fileId");
            for (String fileId : fileIds)
            {
                File file = fileMap.get(fileId);
                if (file != null)
                {
                    FileUtil.deleteFile(file);
                    fileMap.remove(fileId);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Delete server files error.", e);
        }
    }

    /**
     * Query all files in selected folder. 
     * The return value is in xml pattern.
     * @param request
     * @param response
     * @throws IOException
     */
    private void getSwitchFiles(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();
        try
        {
            String files = request.getParameter("files");
            String[] fileIds = files.split("#@#");
            StringBuffer data = new StringBuffer("[");
            for (String fileId : fileIds)
            {
                File file = fileMap.get(fileId);
                if (file != null)
                {
                    if (data.length() > 1)
                    {
                        data.append(",");
                    }
                    data.append("{id:\"").append(fileId).append("\",path:\"")
                            .append(convertFilePath(file.getPath()).replace("\\", "\\\\"))
                            .append("\",name:\"").append(file.getName())
                            .append("\",size:\"").append(file.length())
                            .append("\"}");
                }
            }
            data.append("]");
            
            response.setContentType("text/html;charset=UTF-8");
            writer.write(data.toString());
        }
        catch (Exception e)
        {
            logger.error("Get switched files error.", e);
        }
        finally
        {
            writer.close();
        }
    }
    
    
    private void setLable(HttpServletRequest request, ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_uploaded_files");
        setLableToJsp(request, bundle, "msg_job_delete_confirm");
        setLableToJsp(request, bundle, "msg_job_delete_deny");
        setLableToJsp(request, bundle, "helper_text_recent_files");
        setLableToJsp(request, bundle, "lb_create_job_add_to_job");
        setLableToJsp(request, bundle, "lb_close");
    }
    
    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }
    
    /**
     * Replace "\" and "/" to file separator
     * @param path
     * @return
     */
    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/",
                    File.separator);
        }
        else 
        {
            return "";
        }
    }
}
