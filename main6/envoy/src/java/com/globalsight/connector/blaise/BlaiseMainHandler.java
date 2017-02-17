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

import com.cognitran.translation.client.TranslationPageCommand;
import com.globalsight.connector.blaise.form.BlaiseConnectorAttribute;
import com.globalsight.connector.blaise.form.BlaiseConnectorFilter;
import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.customAttribute.*;
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
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlaiseMainHandler extends PageActionHandler
{
	static private final Logger logger = Logger
			.getLogger(BlaiseMainHandler.class);

	private List<?> allConns = null;

    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.blaise.BlaiseConnector", loadFromDb = false)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
    	BlaiseConnector connector = (BlaiseConnector) form;
        String id = request.getParameter("companyId");
        if(StringUtil.isNotEmpty(id))
        {
        	connector.setCompanyId(Long.parseLong(id));
        }
        String[] days = new String[]{"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        StringBand pullDays = new StringBand();
        String tmp = null;
        for (String day : days)
        {
            tmp = request.getParameter(day);
            if (tmp != null)
                pullDays.append(tmp).append(",");
        }
        tmp = pullDays.toString();
        connector.setPullDays(tmp);
        tmp = request.getParameter("pullHour");
        connector.setPullHour(Integer.parseInt(tmp));
        tmp = request.getParameter("automatic");
        connector.setAutomatic("true".equals(tmp));
        tmp = request.getParameter("combined");
        connector.setCombined("true".equals(tmp));
        tmp = request.getParameter("qaCount");
        connector.setQaCount(Integer.parseInt(tmp));
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session.getAttribute(SESSION_MANAGER);
        User user = (User) sessionManager.getAttribute(USER);
        connector.setLoginUser(user.getUserId());

        boolean isNew = connector.getId() == -1;

        HibernateUtil.saveOrUpdate(connector);

        if (connector.isAutomatic())
        {
            saveAttributes(connector.getId(), request);
            if (isNew)
            {
                BlaiseAutoManager.startThread(connector);
            } else
                BlaiseAutoManager.resetThread(connector);
        } else
            BlaiseAutoManager.cancelThread(connector.getId());
    }

    private void saveAttributes(long connectorId, HttpServletRequest request)
    {
        List<BlaiseConnectorAttribute> attributes = new ArrayList<>();
        BlaiseConnectorAttribute attribute;
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String param = names.nextElement();
            String value;
            attribute = new BlaiseConnectorAttribute();
            value = request.getParameter(param);
            attribute.setBlaiseConnectorId(connectorId);
            if (!param.startsWith("anyAttr") && !param.startsWith("hduAttr")
                    && !param.startsWith("isheetAttr"))
                continue;
            if (param.startsWith("anyAttr")) {
                param = param.substring("anyAttr".length());
                attribute.setBlaiseJobType("A");
            } else if (param.startsWith("hduAttr")) {
                param = param.substring("hudAttr".length());
                attribute.setBlaiseJobType("H");
            } else if (param.startsWith("isheetAttr")) {
                param = param.substring("isheetAttr".length());
                attribute.setBlaiseJobType("I");
            }
            long attrId = Long.parseLong(param);
            Attribute attribute1 = HibernateUtil.get(Attribute.class, attrId);
            Condition condition = attribute1.getCondition();
            if (condition instanceof ListCondition)
                attribute.setAttributeType("choiceList");
            else if (condition instanceof TextCondition)
                attribute.setAttributeType("text");
            attribute.setAttributeId(attrId);
            attribute.setAttributeValue(value);
            attributes.add(attribute);
        }
        BlaiseHelper.saveConnectorAttributes(attributes);
    }

    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
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
    public void demo(HttpServletRequest request,
                       HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("blaiseConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            BlaiseConnector blc = BlaiseManager.getBlaiseConnectorById(cId);
            BlaiseHelper helper = new BlaiseHelper(blc);

            //demo to fetch procedure entries to create a job
            TranslationPageCommand command = helper.initTranslationPageCommand(0, 10,
                    null, "en_US", "de_DE",
                    BlaiseConstants.GS_TYPE_PROCEDURE, null, 0, false);
            List<TranslationInboxEntryVo> entries = helper.listInbox(command);
            logger.info("Fetch entries == " + (entries != null ? entries.size() : "0"));
            ArrayList<TranslationInboxEntryVo> hduEntries = new ArrayList<>();
            ArrayList<TranslationInboxEntryVo> edmEntries = new ArrayList<>();
            ArrayList<TranslationInboxEntryVo> otherEntries = new ArrayList<>();
            if (entries != null) {
                for (TranslationInboxEntryVo entry : entries) {
                    if (entry.isUsageOfHDU())
                        hduEntries.add(entry);
                    else if (entry.isUsageOfIsSheet())
                        edmEntries.add(entry);
                    else
                        otherEntries.add(entry);
                    helper.claim(entry.getId());
                }
                ExecutorService pool = Executors.newFixedThreadPool(10);
                HttpSession session = request.getSession(false);
                SessionManager sessionMgr =  (SessionManager) session
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
                String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
                List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
                FileProfile fp = HibernateUtil.get(FileProfileImpl.class, blc.getDefaultFileProfileId());
                for (int i=0;i<entries.size();i++)
                    fileProfiles.add(fp);
                CreateBlaiseJobForm blaiseForm = new CreateBlaiseJobForm();
                blaiseForm.setBlaiseConnectorId(String.valueOf(cId));
                blaiseForm.setCombineByLangs("on");
                blaiseForm.setJobName(BlaiseHelper.getEntriesJobName(entries));
                blaiseForm.setUserName(user.getUserName());
                blaiseForm.setPriority("3");

                CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user, currentCompanyId,
                        blc, blaiseForm, entries, fileProfiles, null,
                        null, null, null);
                Thread t = new MultiCompanySupportedThread(runnable);
                pool.execute(t);
            }
        }
    }

    @SuppressWarnings("unchecked")
	@ActionHandler(action = "filter", formClass = "com.globalsight.connector.blaise.form.BlaiseConnectorFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
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
	public void beforeAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
	{
        allConns = null;
        clearSessionExceptTableInfo(request.getSession(false),
                "blaiseConnectorKey");

        response.setCharacterEncoding("utf-8");
	}

	@Override
	public void afterAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
	{
		dataForTable(request);
	}

	private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

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

        setTableNavigation(request, session, allConns,
                new BlaiseConnectorComparator(uiLocale), size,
                "blaiseConnectorList", "blaiseConnectorKey");
    }
}
