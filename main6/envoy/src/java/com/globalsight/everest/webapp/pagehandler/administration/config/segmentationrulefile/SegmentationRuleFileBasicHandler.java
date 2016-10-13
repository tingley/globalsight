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
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileValidator;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReaderException;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class SegmentationRuleFileBasicHandler extends PageHandler
{
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
        if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            p_response
                    .sendRedirect("/globalsight/ControlServlet?activityName=segmentationrules");
            return;
        }
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        p_request.setCharacterEncoding("UTF-8");

        String action = p_request.getParameter("action");

        try
        {
            setCompanyNames(sessionMgr);
            setExistingRuleNames(p_request);
            setValidLocales(session, p_request);

            if (SegmentationRuleConstant.UPLOAD.equals(action))
            {
                writeUploadfileToRequest(p_request);
            }
            else if (SegmentationRuleConstant.DUPLICATE.equals(action))
            {
                String id = (String) p_request.getParameter(RADIO_BUTTON);
                SegmentationRuleFile segmentationRuleFile = ServerProxy
                        .getSegmentationRuleFilePersistenceManager()
                        .readSegmentationRuleFile(Long.parseLong(id));

                segmentationRuleFile.setName("new_segmentation_rule");

                sessionMgr.setAttribute(
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY,
                        segmentationRuleFile);
                p_request.setAttribute("dup", "true");
            }
            else if (SegmentationRuleConstant.EDIT.equals(action))
            {
                // Fetch the segmentationRuleFile to edit and store in session
                String id = (String) p_request.getParameter("segmentId");
                SegmentationRuleFile segmentationRuleFile = ServerProxy
                        .getSegmentationRuleFilePersistenceManager()
                        .readSegmentationRuleFile(Long.parseLong(id));

                sessionMgr.setAttribute(
                        SegmentationRuleConstant.SEGMENTATIONRULE_KEY,
                        segmentationRuleFile);
                sessionMgr.setAttribute("edit", "true");
            }
            else if (SegmentationRuleConstant.VALIDATE.equals(action))
            {
                validate(p_request);
                p_request.setAttribute("isValidate", true);
            }
            else if (SegmentationRuleConstant.TEST.equals(action))
            {
                test(p_request);
            }
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

    private void writeUploadfileToRequest(HttpServletRequest p_request)
    {
        try
        {
            MultipartFormDataReader reader = new MultipartFormDataReader();
            File tempFile = reader.uploadToTempFile(p_request);
            String encoding = p_request.getCharacterEncoding();
            FileInputStream fileInput = new FileInputStream(tempFile);
            String ruleString = FileUtils.read(fileInput, encoding);
            String ruleText;
            int ruleStart = ruleString.indexOf("<?");
            if (ruleStart != -1)
            {
                // Delete BOM mark in unicode encoding file header if any be
                // there.
                ruleText = ruleString.substring(ruleStart);
            }
            else
            {
                ruleText = ruleString;
            }
            p_request.setAttribute("ruleTextFromFile", ruleText);

            OfflineEditHelper.deleteFile(tempFile);
        }
        catch (MultipartFormDataReaderException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

    }

    /**
     * Test the Segmentation Rule
     */
    private void test(HttpServletRequest p_request)
    {
        try
        {
            SegmentationRuleFileImpl rule = new SegmentationRuleFileImpl();
            getParams(p_request, rule);

            String testText = p_request.getParameter("testField");
            String locale_id = p_request.getParameter("locale");
            GlobalSightLocale locale = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(locale_id));

            StringBuffer testResult = new StringBuffer();

            String testRule = rule.getRuleText();

            if ("default".equalsIgnoreCase(testRule))
            {
                String[] results = com.globalsight.everest.segmentationhelper.SegmentationHelper
                        .segmentWithDefault(locale.getLocale(), testText);

                for (int i = 0; i < results.length; i++)
                {
                    testResult.append(i + 1).append(". ");
                    testResult.append(results[i]);
                    testResult.append("\n");
                }
            }
            else
            {
                String msg = doValidator(p_request, rule);
                if (msg == null)
                {
                    String[] results = com.globalsight.everest.segmentationhelper.SegmentationHelper
                            .segment(testRule, locale.getLocale(),
                                    testText);
                    for (int i = 0; i < results.length; i++)
                    {
                        testResult.append(i + 1).append(". ");
                        testResult.append(results[i]);
                        testResult.append("\n");
                    }
                }
                else
                {
                    testResult.append("Validate Error. ");
                }
            }

            p_request.setAttribute("tmpRule", rule);
            p_request.setAttribute("selectedLocale", locale);
            p_request.setAttribute("testText", testText);
            p_request.setAttribute("testResult", testResult.toString());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Validate the segmentation. If invalid, set the error in the request.
     */
    private void validate(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        try
        {
            SegmentationRuleFileImpl rule = new SegmentationRuleFileImpl();
            getParams(p_request, rule);

            doValidator(p_request, rule);

            p_request.setAttribute("tmpRule", rule);

            // save test parameter in the web page
            String testText = p_request.getParameter("testField");
            String locale_id = p_request.getParameter("locale");
            GlobalSightLocale locale = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(locale_id));
            p_request.setAttribute("selectedLocale", locale);
            p_request.setAttribute("testText", testText);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private String doValidator(HttpServletRequest p_request,
            SegmentationRuleFileImpl rule) throws ExtractorException
    {
        String testRule = rule.getRuleText();

        if ("default".equalsIgnoreCase(testRule))
        {
            p_request.setAttribute("invalid", "");
            return null;
        }
        else
        {
            SegmentationRuleFileValidator validator = new SegmentationRuleFileValidator();
            if (!validator.validate(testRule, rule.getType()))
            {
                p_request.setAttribute("invalid", validator.getErrorMessage());
                return validator.getErrorMessage();
            }
            else
            {
                p_request.setAttribute("invalid", "");
                return null;
            }
        }
    }

    /**
     * Get list of all segmentationRuleFile names. Needed in jsp to determine
     * duplicate names.
     */
    private void setExistingRuleNames(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList) ServerProxy
                .getSegmentationRuleFilePersistenceManager()
                .getAllSegmentationRuleFiles();

        List<String> names = new ArrayList<String>();

        if (list != null)
        {
            for (int i = 0, max = list.size(); i < max; i++)
            {
                SegmentationRuleFileImpl rule = (SegmentationRuleFileImpl) list
                        .get(i);
                names.add(rule.getName());
            }
        }
        names.add("default");
        names.add("Default");

        p_request.setAttribute(SegmentationRuleConstant.NAMES, names);
    }

    /**
     * Set valid locales in the request
     */
    private void setValidLocales(HttpSession p_session,
            HttpServletRequest p_request) throws NamingException,
            RemoteException, GeneralException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAvailableLocales();

        // fix for GBS-1693
        SortUtil.sort(sources,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        p_request.setAttribute(LocalePairConstants.LOCALES, sources);
    }

    /**
     * 
     * @param sessionMgr
     * @throws EnvoyServletException
     */
    private void setCompanyNames(SessionManager sessionMgr)
            throws EnvoyServletException
    {
        String[] companies = UserHandlerHelper.getCompanyNames();
        sessionMgr.setAttribute("companyNames", companies);
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
}
