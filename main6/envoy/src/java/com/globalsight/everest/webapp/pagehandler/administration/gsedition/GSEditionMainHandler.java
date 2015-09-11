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
package com.globalsight.everest.webapp.pagehandler.administration.gsedition;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.gsedition.GSEdition;
import com.globalsight.everest.gsedition.GSEditionException;
import com.globalsight.everest.gsedition.GSEditionManagerLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GSEditionComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

/**
 */
public class GSEditionMainHandler extends PageHandler
{
    private static final Logger logger = Logger
            .getLogger(GSEditionMainHandler.class.getName());

    private GSEditionManagerLocal gsEditionManager = new GSEditionManagerLocal();

    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (action != null)
            {
                if (action.equals("create"))
                {
                    createGSEdition(p_request);
                }
                else if (action.equals("modify"))
                {
                    editGSEdition(p_request);
                }
                else if (action.equals("remove"))
                {
                    removeGSEdition(p_request);
                }
                else if (action.equals("downloadSourceFile"))
                {
                    StringBuffer fileStorageRoot = new StringBuffer(
                            SystemConfiguration
                                    .getInstance()
                                    .getStringParameter(
                                            SystemConfigParamNames.FILE_STORAGE_DIR));
                    StringBuffer fileStorageRootBak = new StringBuffer(
                            fileStorageRoot.toString());
                    String jobName = p_request.getParameter("jobName");
                    String local = p_request.getParameter("local");
                    String fileName = p_request.getParameter("fileName");

                    fileStorageRoot = fileStorageRoot.append(File.separator)
                            .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                            .append(File.separator)
                            .append(WebAppConstants.ORIGINAL_SORUCE_FILE)
                            .append(File.separator).append(jobName)
                            .append(File.separator).append(local);

                    File file = new File(fileStorageRoot.toString()
                            + File.separator + fileName);
                    if (!file.exists() || !file.isFile())
                    {
                        JobHandler jobHandler = ServerProxy.getJobHandler();
                        Job job = jobHandler.getJobByJobName(jobName);
                        long companyId = job.getCompanyId();
                        String companyName = jobHandler.getCompanyById(
                                companyId).getCompanyName();
                        if (fileStorageRootBak.toString().endsWith(companyName) == false)
                        {
                            fileStorageRootBak.append(File.separator).append(
                                    companyName);
                            fileStorageRootBak = fileStorageRootBak
                                    .append(File.separator)
                                    .append(WebAppConstants.VIRTUALDIR_TOPLEVEL)
                                    .append(File.separator)
                                    .append(WebAppConstants.ORIGINAL_SORUCE_FILE)
                                    .append(File.separator).append(jobName)
                                    .append(File.separator).append(local);
                        }
                        file = new File(fileStorageRootBak.toString()
                                + File.separator + fileName);
                    }

                    if (file.exists() && file.isFile())
                    {
                        byte[] buf = new byte[1024];
                        int len = 0;
                        BufferedInputStream br = null;
                        OutputStream ut = null;
                        p_response.reset();
                        p_response.setContentType("application/x-msdownload");
                        if (p_request.isSecure())
                        {
                            setHeaderForHTTPSDownload(p_response);
                        }
                        p_response.setHeader("Content-Disposition",
                                "attachment; filename=" + fileName + ";");
                        br = new BufferedInputStream(new FileInputStream(file));
                        ut = p_response.getOutputStream();

                        while ((len = br.read(buf)) != -1)
                        {
                            ut.write(buf, 0, len);
                        }
                        br.close();
                    }

                    return;
                }
            }

            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        Vector activities = vectorizedCollection(gsEditionManager
                .getAllGSEdition());
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, activities,
                new GSEditionComparator(uiLocale), 10, "GSEditionList",
                "GSEditonKey");
    }

    private void createGSEdition(HttpServletRequest p_request)
            throws GSEditionException, RemoteException
    {
        GSEdition gsEdition = new GSEdition();

        String name = p_request.getParameter("name");
        String hostName = p_request.getParameter("hostName");
        String hostPort = p_request.getParameter("hostPort");
        String userName = p_request.getParameter("userName");
        String password = p_request.getParameter("password");
        String description = p_request.getParameter("description");
        String enableHttps = p_request.getParameter("enableHttps");
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (!gsEditionManager.isActionExist(name))
        {
            gsEdition.setName(name);
            gsEdition.setHostName(hostName);
            if (enableHttps == null || !"on".equals(enableHttps))
            {
                gsEdition.setEnableHttps(false);
            }
            else
            {
                gsEdition.setEnableHttps(true);
            }
            gsEdition.setHostPort(hostPort);
            gsEdition.setUserName(userName);
            gsEdition.setPassword(password);
            gsEdition.setDescription(description);
            gsEdition.setCompanyID(Long.parseLong(companyId));
        }

        try
        {
            p_request.setAttribute("newOrModify", "new");

            Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                    hostName, hostPort, userName, password,
                    gsEdition.getEnableHttps());
            String fullAccessToken = ambassador.login(userName, password);
            String realAccessToken = WebServiceClientHelper
                    .getRealAccessToken(fullAccessToken);

            String permissionStr = ambassador
                    .getAllPermissionsByUser(realAccessToken);

            if (permissionStr.indexOf(Permission.CUSTOMER_UPLOAD) > -1)
            {
                gsEditionManager.createAction(gsEdition);
            }
            else
            {
                p_request.setAttribute("infoType", "noPermission");
                p_request.setAttribute("GSEditionSession", gsEdition);
            }
        }
        catch (Exception e)
        {
            p_request.setAttribute("infoType", "wsError");
            String errorInfo = null;
            String msg = e.getMessage();
            if (msg != null
                    && (msg.indexOf("Connection timed out") > -1
                            || msg.indexOf("UnknownHostException") > -1 || msg
                            .indexOf("java.net.ConnectException") > -1))
            {
                errorInfo = "Can not connect to server. Please check host name or host port.";
            }
            else if (msg != null && msg.indexOf("port out of range") > -1)
            {
                errorInfo = "Host Port is out of range.";
            }
            else if (msg != null
                    && msg.indexOf("Illegal web service access attempt from IP address") > -1)
            {
                errorInfo = "User name or password is wrong. Or the IP is not allowed to access server.";
            }
            else if (msg != null
                    && msg.indexOf("The username or password may be incorrect") > -1)
            {
                errorInfo = "Can not connect to server. The username or password may be incorrect.";
            }
            else
            {
                errorInfo = "Can not connect to server.";
            }
            p_request.setAttribute("wsErrorInfo", errorInfo);
            p_request.setAttribute("GSEditionSession", gsEdition);
        }
    }

    private void editGSEdition(HttpServletRequest p_request)
            throws GSEditionException, RemoteException
    {
        if (p_request.getParameter("gsEditionID") != null)
        {
            long id = Integer.parseInt(p_request.getParameter("gsEditionID"));
            GSEdition gsEdition = gsEditionManager.getGSEditionByID(id);

            try
            {
                p_request.setAttribute("newOrModify", "edit");

                if (p_request.getParameter("name") != null)
                {
                    gsEdition.setName(p_request.getParameter("name"));
                }

                if (p_request.getParameter("hostName") != null)
                {
                    gsEdition.setHostName(p_request.getParameter("hostName"));
                }

                String enableHttps = p_request.getParameter("enableHttps");
                if (enableHttps != null && "on".equals(enableHttps))
                {
                    gsEdition.setEnableHttps(true);
                }

                if (p_request.getParameter("hostPort") != null)
                {
                    gsEdition.setHostPort(p_request.getParameter("hostPort"));
                }

                if (p_request.getParameter("userName") != null)
                {
                    gsEdition.setUserName(p_request.getParameter("userName"));
                }

                if (p_request.getParameter("password") != null)
                {
                    gsEdition.setPassword(p_request.getParameter("password"));
                }

                gsEdition.setDescription(p_request.getParameter("description"));

                Ambassador ambassador = WebServiceClientHelper
                        .getClientAmbassador(gsEdition.getHostName(),
                                gsEdition.getHostPort(),
                                gsEdition.getUserName(),
                                gsEdition.getPassword(),
                                gsEdition.getEnableHttps());
                String fullAccessToken = ambassador.login(
                        gsEdition.getUserName(), gsEdition.getPassword());
                String realAccessToken = WebServiceClientHelper
                        .getRealAccessToken(fullAccessToken);

                String permissionStr = ambassador
                        .getAllPermissionsByUser(realAccessToken);

                if (permissionStr.indexOf(Permission.CUSTOMER_UPLOAD) > -1)
                {
                    gsEditionManager.updateGSEdition(gsEdition);
                }
                else
                {
                    p_request.setAttribute("infoType", "noPermission");
                    p_request.setAttribute("GSEditionSession", gsEdition);
                }
            }
            catch (Exception e)
            {
                p_request.setAttribute("infoType", "wsError");
                String errorInfo = null;
                String msg = e.getMessage();
                if (msg != null
                        && (msg.indexOf("Connection timed out") > -1
                                || msg.indexOf("UnknownHostException") > -1 || msg
                                .indexOf("java.net.ConnectException") > -1))
                {
                    errorInfo = "Can not connect to server. Please check host name or host port.";
                }
                else if (msg != null && msg.indexOf("port out of range") > -1)
                {
                    errorInfo = "Host Port is out of range.";
                }
                else if (msg != null
                        && msg.indexOf("Illegal web service access attempt from IP address") > -1)
                {
                    errorInfo = "Incorrect user name or password, or web service access is not allowed for IP filter is enabled on server.";
                }
                else if (msg != null
                        && msg.indexOf("The username or password may be incorrect") > -1)
                {
                    errorInfo = "Unable to login user to GlobalSight. The username or password may be incorrect.";
                }
                else
                {
                    errorInfo = "Can not connect to server.";
                }
                p_request.setAttribute("wsErrorInfo", errorInfo);
                p_request.setAttribute("GSEditionSession", gsEdition);
            }
        }
    }

    /*
     * remove the auto action.
     */
    private void removeGSEdition(HttpServletRequest p_request)
            throws GSEditionException, RemoteException
    {

        if (p_request.getParameter("gsEditionID") != null)
        {
            long id = Integer.parseInt(p_request.getParameter("gsEditionID"));

            // Check if this gs edition is referred to by remote tm
            boolean canBeRemoved = true;
            try
            {
                Collection allProjectTms = ServerProxy.getProjectHandler()
                        .getAllProjectTMs();
                if (allProjectTms != null && allProjectTms.size() > 0)
                {
                    Iterator projectTmIter = allProjectTms.iterator();
                    while (projectTmIter.hasNext())
                    {
                        ProjectTM ptm = (ProjectTM) projectTmIter.next();
                        long gsEditionId = ptm.getGsEditionId();
                        if (gsEditionId == id)
                        {
                            canBeRemoved = false;
                        }
                    }
                }
            }
            catch (Exception e)
            {
            	logger.error(e);
            }

            if (!canBeRemoved)
            {
                p_request.setAttribute("canBeRemoved", "false");
                return;
            }

            gsEditionManager.removeAction(id);
        }
    }
}
