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
package com.globalsight.connector.blaise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.cognitran.translation.client.TranslationPageCommand;
import com.globalsight.connector.blaise.form.BlaiseConnectorAttribute;
import com.globalsight.connector.blaise.form.BlaiseConnectorFilter;
import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.BlaiseConnectorComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

import jodd.util.StringBand;

public class BlaiseMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger.getLogger(BlaiseMainHandler.class);

    private List<?> allConns = null;

    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.blaise.BlaiseConnector", loadFromDb = false)
    public void save(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        BlaiseConnector connector = (BlaiseConnector) form;
        String id = request.getParameter("companyId");
        if (StringUtil.isNotEmpty(id))
        {
            connector.setCompanyId(Long.parseLong(id));
        }

        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session.getAttribute(SESSION_MANAGER);
        User user = (User) sessionManager.getAttribute(USER);

        String tmp = request.getParameter("userTimeZone");
        connector.setUserCalendar(tmp);

        int hours = 0;
        Calendar systemCalendar = Calendar.getInstance();
        Calendar userCalendar = Calendar.getInstance(TimeZone.getTimeZone(tmp));
        if (systemCalendar.getTimeZone().getRawOffset() != userCalendar.getTimeZone()
                .getRawOffset())
        {
            // user set a different time zone from system time zone
            Calendar cal = Calendar.getInstance();
            cal.set(userCalendar.get(Calendar.YEAR), userCalendar.get(Calendar.MONTH),
                    userCalendar.get(Calendar.DATE), userCalendar.get(Calendar.HOUR_OF_DAY),
                    userCalendar.get(Calendar.MINUTE), userCalendar.get(Calendar.SECOND));
            long times = cal.getTimeInMillis() - systemCalendar.getTimeInMillis();
            hours = (int) times / 3600000;
        }

        String[] days = new String[]
        { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" };
        StringBand pullDays = new StringBand();
        ArrayList<Integer> pullDaysList = new ArrayList<>();
        for (String day : days)
        {
            tmp = request.getParameter(day);
            if (tmp != null)
            {
                pullDays.append(tmp).append(",");
                pullDaysList.add(Integer.parseInt(tmp));
            }
        }

        tmp = pullDays.toString();
        connector.setUserPullDays(tmp);
        connector.setPullDays(tmp);
        tmp = request.getParameter("pullHour");
        int tmpPullHour = Integer.parseInt(tmp);
        connector.setUserPullHour(tmpPullHour);
        connector.setPullHour(tmpPullHour);

        if (hours != 0)
        {
            // user set different time zone with server time zone
            int cz = 0;

            int iHours = tmpPullHour - hours;
            if (iHours < 0)
            {
                cz = -1;
                iHours = 24 + iHours;
            }
            else if (iHours >= 24)
            {
                cz = 1;
                iHours = iHours - 24;
            }
            connector.setPullHour(iHours);
            pullDays = new StringBand();
            for (Integer day : pullDaysList)
            {
                day = day + cz;
                day = day < 1 ? day = 7 : day;
                day = day > 7 ? day = 1 : day;
                pullDays.append(day).append(",");
            }
            connector.setPullDays(pullDays.toString());
        }

        connector.setUserPullHour(Integer.parseInt(tmp));
        tmp = request.getParameter("automatic");
        connector.setAutomatic("true".equals(tmp));
        tmp = request.getParameter("combined");
        connector.setCombined("true".equals(tmp));
        tmp = request.getParameter("checkDuration");
        if (StringUtil.isEmpty(tmp))
            connector.setCheckDuration(0);
        tmp = request.getParameter("qaCountString");
        if (StringUtil.isEmpty(tmp) || "all".equalsIgnoreCase(tmp))
            connector.setQaCount(0);
        else
            connector.setQaCount(Integer.parseInt(tmp));
        connector.setLoginUser(user.getUserId());

        boolean isNew = connector.getId() == -1;

        HibernateUtil.saveOrUpdate(connector);

        if (connector.isAutomatic())
        {
            saveAttributes(connector.getId(), request);
            if (isNew)
            {
                BlaiseAutoManager.startThread(connector);
            }
            else
                BlaiseAutoManager.resetThread(connector);
        }
        else
            BlaiseAutoManager.cancelThread(connector.getId());
    }

    private void saveAttributes(long connectorId, HttpServletRequest request)
    {
        List<BlaiseConnectorAttribute> attributes = new ArrayList<>();
        BlaiseConnectorAttribute attribute;
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements())
        {
            String param = names.nextElement();
            String value;
            attribute = new BlaiseConnectorAttribute();
            value = request.getParameter(param);
            attribute.setBlaiseConnectorId(connectorId);
            if (!param.startsWith("isheetAttr") && !param.startsWith("ownerManualAttr")
                    && !param.startsWith("serviceManualAttr") && !param.startsWith("edmAttr")
                    && !param.startsWith("hduMiscLiteratureAttr") && !param.startsWith("paAttr"))
                continue;
            if (param.startsWith("isheetAttr"))
            {
                param = param.substring("isheetAttr".length());
                attribute.setBlaiseJobType("I");
            }
            else if (param.startsWith("ownerManualAttr"))
            {
                param = param.substring("ownerManualAttr".length());
                attribute.setBlaiseJobType("O");
            }
            else if (param.startsWith("serviceManualAttr"))
            {
                param = param.substring("serviceManualAttr".length());
                attribute.setBlaiseJobType("S");
            }
            else if (param.startsWith("edmAttr"))
            {
                param = param.substring("edmAttr".length());
                attribute.setBlaiseJobType("E");
            }
            else if (param.startsWith("hduMiscLiteratureAttr"))
            {
                param = param.substring("hduMiscLiteratureAttr".length());
                attribute.setBlaiseJobType("H");
            }
            else if (param.startsWith("paAttr"))
            {
                param = param.substring("paAttr".length());
                attribute.setBlaiseJobType("P");
            }
            long attrId = Long.parseLong(param);
            Attribute attribute1 = HibernateUtil.get(Attribute.class, attrId);
            Condition condition = attribute1.getCondition();
            if (condition instanceof ListCondition)
                attribute.setAttributeType("choiceList");
            else if (condition instanceof TextCondition)
                attribute.setAttributeType("text");
            else if (condition instanceof IntCondition)
                attribute.setAttributeType("integer");
            else if (condition instanceof FloatCondition)
                attribute.setAttributeType("float");
            attribute.setAttributeId(attrId);
            attribute.setAttributeValue(value);
            attributes.add(attribute);
        }
        BlaiseHelper.saveConnectorAttributes(attributes);
    }

    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        String[] ids = request.getParameterValues("blaiseConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            BlaiseConnector c = BlaiseManager.getBlaiseConnectorById(cId);
            c.setIsActive(false);
            HibernateUtil.update(c);
        }
    }

    @ActionHandler(action = "demo", formClass = "")
    public void demo(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        String[] ids = request.getParameterValues("blaiseConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            BlaiseConnector blc = BlaiseManager.getBlaiseConnectorById(cId);
            BlaiseHelper helper = new BlaiseHelper(blc);

            // demo to fetch procedure entries to create a job
            TranslationPageCommand command = helper.initTranslationPageCommand(0, 10, null, "en_US",
                    "de_DE", BlaiseConstants.GS_TYPE_PROCEDURE, null, 0, false);
            List<TranslationInboxEntryVo> entries = helper.listInbox(command);
            logger.info("Fetch entries == " + (entries != null ? entries.size() : "0"));
            List<TranslationInboxEntryVo> iSheetEntries = new ArrayList<>();
            List<TranslationInboxEntryVo> ownerManualEntries = new ArrayList<>();
            List<TranslationInboxEntryVo> serviceManualEntries = new ArrayList<>();
            List<TranslationInboxEntryVo> edmManualEntries = new ArrayList<>();
            List<TranslationInboxEntryVo> hduMiscLiteratureEntries = new ArrayList<>();
            List<TranslationInboxEntryVo> paEntries = new ArrayList<>();
            if (entries != null)
            {
                for (TranslationInboxEntryVo vo : entries)
                {
                    if (helper.isCategoryISheet(vo))
                    {
                        iSheetEntries.add(vo);
                    }
                    else if (helper.isCategoryOwnerManual(vo))
                    {
                        ownerManualEntries.add(vo);
                    }
                    else if (helper.isCategoryServiceManual(vo))
                    {
                        serviceManualEntries.add(vo);
                    }
                    else if (helper.isCategoryEdmManual(vo))
                    {
                        edmManualEntries.add(vo);
                    }
                    else if (helper.isCategoryHduMiscLiterature(vo))
                    {
                        hduMiscLiteratureEntries.add(vo);
                    }
                    else if (helper.isCategoryPa(vo))
                    {
                        paEntries.add(vo);
                    }
                    else
                    {
                        ownerManualEntries.add(vo);
                    }
                    helper.claim(vo.getId());
                }
                ExecutorService pool = Executors.newFixedThreadPool(10);
                HttpSession session = request.getSession(false);
                SessionManager sessionMgr = (SessionManager) session
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
                String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
                List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
                FileProfile fp = HibernateUtil.get(FileProfileImpl.class,
                        blc.getDefaultFileProfileId());
                for (int i = 0; i < entries.size(); i++)
                    fileProfiles.add(fp);
                CreateBlaiseJobForm blaiseForm = new CreateBlaiseJobForm();
                blaiseForm.setBlaiseConnectorId(String.valueOf(cId));
                blaiseForm.setCombineByLangs("on");
                blaiseForm.setJobName(BlaiseHelper.getEntriesJobName(entries));
                blaiseForm.setUserName(user.getUserName());
                blaiseForm.setPriority("3");

                CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user, currentCompanyId,
                        blc, blaiseForm, entries, fileProfiles, null, null, null, null);
                Thread t = new MultiCompanySupportedThread(runnable);
                pool.execute(t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.blaise.form.BlaiseConnectorFilter")
    public void filter(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        allConns = BlaiseManager.getAllConnectors();
        BlaiseConnectorFilter filter = (BlaiseConnectorFilter) form;
        allConns = filter.filter((List<BlaiseConnector>) allConns);

        request.setAttribute("nameFilter", filter.getNameFilter());
        request.setAttribute("urlFilter", filter.getUrlFilter());
        request.setAttribute("usernameFilter", filter.getUsernameFilter());
        request.setAttribute("companyNameFilter", filter.getCompanyNameFilter());
    }

    @Override
    public void beforeAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, EnvoyServletException
    {
        allConns = null;
        clearSessionExceptTableInfo(request.getSession(false), "blaiseConnectorKey");

        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void afterAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, EnvoyServletException
    {
        dataForTable(request);
    }

    private void dataForTable(HttpServletRequest request) throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

        if (allConns == null)
        {
            allConns = BlaiseManager.getAllConnectors();
        }

        Integer orgSize = (Integer) session.getAttribute("blaiseConnectorPageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = Integer.MAX_VALUE;
            }

            session.setAttribute("blaiseConnectorPageSize", size);
        }

        setTableNavigation(request, session, allConns, new BlaiseConnectorComparator(uiLocale),
                size, "blaiseConnectorList", "blaiseConnectorKey");
    }
}
