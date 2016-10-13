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

package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.zip.ZipIt;

/**
 * Deals with some web requirements about access remote ip filter for
 * webservice.
 */
public class UILocaleUploaderHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(UILocaleUploaderHandler.class);
    public static String newline = System.getProperty("line.separator");

    @ActionHandler(action = UILocaleConstant.UPLOAD, formClass = "")
    public void upload(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	// gbs-1389: restrict direct access to upload resource
		// without "Upload Resource" permission
    	HttpSession session = request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.UILOCALE_UPLOAD_RES)) 
		{
			request.setAttribute("restricted_access", true);
			if (userPerms.getPermissionFor(Permission.UILOCALE_VIEW)) 
			{
				response
						.sendRedirect("/globalsight/ControlServlet?activityName=uiLocaleConfiguration");
			} 
			else 
			{
				response.sendRedirect(request.getContextPath());
			}
			return;
		}
        logger.debug("upload resource files...");
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        String contentType = request.getContentType();
        Boolean uploadResult = Boolean.FALSE;
        String uploadMsg = "";
        
        // handing uploaded data
        if (null != contentType
                && contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            try
            {
                Stack<File> srcFiles = new Stack<File>();
                Stack<File> tgtFiles = new Stack<File>();
                List<String> unknownFiles = null;
                FileUploader fp = new FileUploader();
                File tempFile = fp.upload(request);
                String filename = fp.getName();
                String filenameLowerCase = filename.toLowerCase();
                String fileencoding = fp.getFieldValue(UILocaleConstant.ENCODING);

                boolean isUnknownEncoding = (fileencoding == null || "".equals(fileencoding)) ? true
                        : !Charset.isSupported(fileencoding);

                if (isUnknownEncoding)
                {
                    request.setAttribute(UILocaleConstant.LAST_ENCODING, fileencoding);
                    uploadResult = Boolean.FALSE;
                    uploadMsg = bundle.getString("msg_upload_uilocale_encoding");
                }
                else
                {
                    if (filenameLowerCase.endsWith(".zip"))
                    {
                        File unzipRoot = new File(AmbFileStoragePathUtils.getTempFileDir(),
                                "gs_unzip_" + (new Date()).getTime());
                        unzipRoot.mkdirs();
                        ArrayList zipEntries = ZipIt.unpackZipPackage(tempFile.getPath(), unzipRoot
                                .getPath());
                        if (zipEntries != null && zipEntries.size() > 0)
                        {
                            for (Object obj : zipEntries)
                            {
                                String fname = obj.toString();
                                File srcFile = new File(unzipRoot, fname);
                                String proParent = UILocaleManager
                                        .getPropertiesFileParentPath(srcFile.getName());
                                File tgtFile = new File(proParent, srcFile.getName());

                                srcFiles.push(srcFile);
                                tgtFiles.push(tgtFile);
                            }
                        }
                    }
                    else if (filenameLowerCase.endsWith(".properties"))
                    {
                        srcFiles.push(tempFile);
                        String proParent = UILocaleManager.getPropertiesFileParentPath(filename);
                        tgtFiles.push(new File(proParent, filename));
                    }
                    else
                    {
                        unknownFiles = new ArrayList<String>();
                        unknownFiles.add(filename);
                    }

                    if (!srcFiles.empty())
                    {
                        unknownFiles = getAllUnknownFiles(srcFiles, tgtFiles);
                        uploadResult = (unknownFiles.size() == 0) ? Boolean.TRUE : Boolean.FALSE;
                    }
                    else
                    {
                        uploadResult = Boolean.FALSE;
                    }

                    if (uploadResult)
                    {
                        copyAllFiles(srcFiles, tgtFiles, fileencoding);
                    }
                    else
                    {
                        uploadMsg = bundle.getString("msg_upload_uilocale_failed")
                                + toHtmlList(unknownFiles);
                    }
                }
            }
            catch (Exception e)
            {
                uploadResult = Boolean.FALSE;
                uploadMsg = bundle.getString("msg_upload_uilocale_failed");
                logger.error("upload resource files failed. ", e);
            }
        }
        else
        {
            uploadResult = Boolean.FALSE;
            uploadMsg = bundle.getString("msg_upload_uilocale_form_type");
        }

        request.setAttribute(UILocaleConstant.UPLOAD_RESULT, uploadResult);
        request.setAttribute(UILocaleConstant.UPLOAD_MSG, uploadMsg);

        logger.debug("upload resource files finished");
    }

    private String toHtmlList(List<String> unknownFiles)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        
        for (String string : unknownFiles)
        {
            sb.append("<li>").append(string).append("</li>");
        }
        
        sb.append("</ul>");
        
        return sb.toString();
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        try
        {
            request.setAttribute("encodings", ServerProxy.getLocaleManager().getAllCodeSets());
        }
        catch (Exception e)
        {
            request.setAttribute("encodings", new Vector());
            logger.error("Get all codesets failed. ", e);
        }
    }
    
    private List<String> getAllUnknownFiles(Vector<File> srcFiles,
            Vector<File> tgtFiles) throws IOException
    {
        List<String> unknownFiles = new ArrayList<String>();
        
        for (File f : tgtFiles)
        {
            String filename = f.getName();

            if (isUnknownFileName(filename))
            {
                unknownFiles.add(filename);
            }
        }

        return unknownFiles;
    }

    private List<String> copyAllFiles(Stack<File> srcFiles, Stack<File> tgtFiles, String fileencoding)
            throws Exception
    {
        List<String> unknownFiles = new ArrayList<String>();
        
        while (!srcFiles.empty() && !tgtFiles.empty())
        {
            File srcFile = srcFiles.pop();
            File tgtFile = tgtFiles.pop();
            String filename = tgtFile.getName();
            
            if (isUnknownFileName(filename))
            {
                unknownFiles.add(filename);
            }
            else
            {
                String timekey = "" + (new Date()).getTime();
                if (tgtFile.exists())
                {
                    File bakFileOfOri = new File(tgtFile.getParent(), tgtFile.getName() + "." + timekey + ".bak");
                    FileUtil.copyFile(tgtFile, bakFileOfOri);
                }
                
                File bakFileOfUploaded = new File(tgtFile.getParent(), tgtFile.getName() + "." + timekey + ".uploaded.bak");
                FileUtil.copyFile(srcFile, bakFileOfUploaded);
                
                //FileUtil.copyFile(srcFile, tgtFile);
                runNative2Ascii(srcFile, tgtFile, fileencoding);
            }
        }
        
        return unknownFiles;
    }

    private void runNative2Ascii(File srcFile, File tgtFile, String fileencoding) throws Exception
    {
        String native2ascii = UILocaleManager.getJdkNative2Ascii();
        StringBuilder cmd = new StringBuilder();
        cmd.append(native2ascii).append(" ");
        cmd.append("-encoding ").append(fileencoding).append(" ");
        cmd.append(srcFile.getPath()).append(" ");
        cmd.append(tgtFile.getPath());

        Process process = Runtime.getRuntime().exec(cmd.toString());
        InputStream stderr = process.getErrorStream();
        String line = null;
        StringBuilder errors = new StringBuilder();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));
        while ((line = errorReader.readLine()) != null)
        {
            errors.append(line).append(newline);
        }
        errorReader.close();

        if (errors.length() != 0)
        {
            throw new Exception(errors.toString());
        }
    }

    private boolean isUnknownFileName(String filename)
    {        
        String fname = filename.toLowerCase();
        boolean isUnknown = true;
        List<String> locales = UILocaleManager.getSystemUILocaleStrings();
        for (String locale : locales)
        {
            String substr = ("_" + locale + ".properties").toLowerCase();
            if (fname.endsWith(substr))
            {
                isUnknown = false;
                break;
            }
        }
        
        return isUnknown;
    }
}
