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
package com.globalsight.everest.webapp.pagehandler.administration.config.segmentationrulefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.SegmentationRuleFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;

/**
 * SegmentationRuleFilePageHandler, A page handler to produce the entry page
 * (segmentationRuleFileMain.jsp) for SegmentationRuleFile management.
 */
public class SegmentationRuleFileMainHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(SendDownloadFileHelper.class);

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        p_request.setCharacterEncoding("UTF-8");
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        boolean noInvoke = false;

        try
        {
            if (SegmentationRuleConstant.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session,
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
            }
            else if (SegmentationRuleConstant.NEW.equals(action))// save new
                                                                 // SR
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=segmentationrules");
                    return;
                }
                createRule(p_request, session);
                clearSessionExceptTableInfo(session,
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
            }
            else if (SegmentationRuleConstant.EDIT.equals(action))// save
                                                                  // updated
                                                                  // SR
            {
                updateRule(p_request, session);
                clearSessionExceptTableInfo(session,
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
            }
            else if (SegmentationRuleConstant.REMOVE.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=segmentationrules");
                    return;
                }
                removeRule(p_request, session);
                clearSessionExceptTableInfo(session,
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
            }
            else if (SegmentationRuleConstant.EXPORT.equals(action))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=segmentationrules");
                    return;
                }
                exportRule(p_request, p_response, session);
                clearSessionExceptTableInfo(session,
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
                noInvoke = true;
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

        if (!noInvoke)
        {
            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }
    }

    /**
     * Create a new rule.
     */
    private void createRule(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        SegmentationRuleFileImpl ruleFile = new SegmentationRuleFileImpl();

        getParams(p_request, ruleFile);

        ServerProxy.getSegmentationRuleFilePersistenceManager()
                .createSegmentationRuleFile(ruleFile);
    }

    /**
     * Modify an existing rule.
     */
    private void updateRule(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        SegmentationRuleFileImpl ruleFile = (SegmentationRuleFileImpl) sessionMgr
                .getAttribute(SegmentationRuleConstant.SEGMENTATIONRULE_KEY);

        if (ruleFile == null)
        {
            /*
             * If the ruleFile is null, means the link from illegal request,
             * return the operation directly.
             */
            return;
        }

        getParams(p_request, ruleFile);

        ServerProxy.getSegmentationRuleFilePersistenceManager()
                .updateSegmentationRuleFile(ruleFile);
    }

    /**
     * Remove an existing rule. Check dependence first.
     */
    private void removeRule(HttpServletRequest p_request, HttpSession p_session)
            throws RemoteException, NamingException, GeneralException
    {
        String id = (String) p_request.getParameter(RADIO_BUTTON);
        StringBuffer invalid = new StringBuffer();
        // can not remove default rule
        if (id.equals("1"))
        {
            p_request.setAttribute("invalid",
                    "Can not remove default Segmentation Rule. ");
        }
        else
        {
            // check whether some tm profiles using it.
            String[] tmpids = ServerProxy
                    .getSegmentationRuleFilePersistenceManager()
                    .getTmpIdsBySegmentationRuleId(id);

            if (tmpids != null)
            {

                for (int i = 0; i < tmpids.length; i++)
                {
                    TranslationMemoryProfile tmp = TMProfileHandlerHelper
                            .getTMProfileById(Long.parseLong(tmpids[i]));

                    invalid.append(tmp.getName()).append(", ");
                }

                p_request.setAttribute("invalid",
                        "This Segmentation Rule is being used by these TM Profiles: "
                                + invalid.substring(0, invalid.length() - 2));
            }
            else
            {
                SegmentationRuleFile segmentationRuleFile = ServerProxy
                        .getSegmentationRuleFilePersistenceManager()
                        .readSegmentationRuleFile(Long.parseLong(id));

                ServerProxy.getSegmentationRuleFilePersistenceManager()
                        .deleteSegmentationRuleFile(segmentationRuleFile);
            }
        }
    }

    /**
     * Export an existing rule.
     * 
     * @param p_response
     */
    private void exportRule(HttpServletRequest p_request,
            HttpServletResponse p_response, HttpSession p_session)
            throws NamingException, GeneralException, IOException
    {
        String id = (String) p_request.getParameter(RADIO_BUTTON);
        SegmentationRuleFile segmentationRuleFile = ServerProxy
                .getSegmentationRuleFilePersistenceManager()
                .readSegmentationRuleFile(Long.parseLong(id));

        String name = segmentationRuleFile.getName();
        if (name.length() < 3)
        {
            name += "__";
        }
        File tmpDir = AmbFileStoragePathUtils.getCustomerDownloadDir();
        File tmpFile = File.createTempFile(name, ".xml", tmpDir);
        FileUtils.write(tmpFile, segmentationRuleFile.getRuleText(),
                p_request.getCharacterEncoding());

        p_response.setContentType("application/xml");
        p_response.setHeader("Content-Disposition", "attachment; filename=\""
                + tmpFile.getName() + "\";");
        p_response.setContentLength((int) tmpFile.length());

        byte[] inBuff = new byte[4096];
        FileInputStream fis = new FileInputStream(tmpFile);
        int bytesRead = 0;
        while ((bytesRead = fis.read(inBuff)) != -1)
        {
            p_response.getOutputStream().write(inBuff, 0, bytesRead);
        }

        if (bytesRead > 0)
        {
            p_response.getOutputStream().write(inBuff, 0, bytesRead);
        }

        fis.close();

        OfflineEditHelper.deleteFile(tmpFile);

        CATEGORY.debug("Sent a Segmentation Rule File "
                + segmentationRuleFile.getName() + " to "
                + p_request.getRemoteHost());
    }

    /**
     * Get request params and update rule.
     */
    private void getParams(HttpServletRequest p_request,
            SegmentationRuleFileImpl p_ruleFile)
    {
        String name = p_request.getParameter("saveRuleName");
        String desc = p_request.getParameter("descField");
        String rule = p_request.getParameter("textField");
        String type_str = p_request.getParameter("type");
        int type = Integer.parseInt(type_str);

        p_ruleFile.setName(name);
        p_ruleFile.setDescription(desc);
        p_ruleFile.setRuleText(rule);
        p_ruleFile.setType(type);

        // for super admin
        String companyName = p_request.getParameter("companyName");
        if (companyName != null)
        {
            String companyId = CompanyWrapper.getCompanyIdByName(companyName);
            p_ruleFile.setCompanyId(Long.parseLong(companyId));
        }
    }

    /**
     * Get list of all rules.
     */
    @SuppressWarnings("unchecked")
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        Collection segmentationrulefiles = ServerProxy
                .getSegmentationRuleFilePersistenceManager()
                .getAllSegmentationRuleFiles();

        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, new ArrayList(
                segmentationrulefiles), new SegmentationRuleFileComparator(
                uiLocale), 10, SegmentationRuleConstant.SEGMENTATIONRULE_LIST,
                SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
    }
}
