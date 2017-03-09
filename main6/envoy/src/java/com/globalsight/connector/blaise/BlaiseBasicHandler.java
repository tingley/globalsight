/**
 * Copyright 2009 Welocalize, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalsight.connector.blaise;

import com.globalsight.connector.blaise.form.BlaiseConnectorAttribute;
import com.globalsight.connector.blaise.util.BlaiseAutoHelper;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import jodd.util.StringBand;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BlaiseBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(BlaiseBasicHandler.class);

    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.blaise.BlaiseConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try
        {
            BlaiseConnector blac = (BlaiseConnector) form;
            BlaiseHelper helper = new BlaiseHelper(blac);
            JSONObject json = new JSONObject();
            String errorMsg = helper.doTest();
            json.put("error", errorMsg);
            out.write(json.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            JSONObject json = new JSONObject();
            json.put("error", bundle.getString("error_blaise_connector"));
            out.write(json.toString().getBytes("UTF-8"));
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @ActionHandler(action = "getAttributes", formClass = "com.globalsight.cxe.entity.blaise.BlaiseConnector")
    public void getAttributes(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try
        {
            BlaiseConnector blac = (BlaiseConnector) form;
            long fpId = blac.getDefaultFileProfileId();
            BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();
            String data = autoHelper.getJobAttributes(fpId);
            out.write(data.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            JSONObject json = new JSONObject();
            json.put("error", "Error");
            out.write(json.toString().getBytes("UTF-8"));
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        String id = request.getParameter("id");
        BlaiseConnector connector = null;
        if (id != null && !"-1".equals(id))
        {
            connector = BlaiseManager
                    .getBlaiseConnectorById(Long.parseLong(id));
            request.setAttribute("blaise", connector);
        }

        long companyId = CompanyWrapper.getCurrentCompanyIdAsLong();
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        request.setAttribute("currentUsername", user.getUserName());
        List<String> extensions = new ArrayList<>(1);
        extensions.add("xlf");
        extensions.add("xliff");
        try
        {
            BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();
            ArrayList<FileProfileImpl> fps = autoHelper
                    .getAllXliff12FileProfile(companyId, user.getUserId());
            request.setAttribute("fileProfiles", fps);
        }
        catch (Exception e)
        {
            logger.error("Error found when get basic information of company " + companyId);
        }

        String names = "";
        StringBand urls = new StringBand(256);
        for (Object o : BlaiseManager.getAllConnectors())
        {
            BlaiseConnector mtc = (BlaiseConnector) o;
            if (id != null && id.equals("" + mtc.getId()))
            {
                continue;
            }
            names += "," + mtc.getName() + ",";
            if (mtc.isAutomatic())
                urls.append("$@$").append(mtc.getUrl()).append("$@$");
        }
        request.setAttribute("names", names);
        request.setAttribute("urls", urls.toString());

        List<BlaiseConnectorAttribute> typeAttributes = null;
        String attributeData = "";
        if (connector != null)
        {
            BlaiseHelper helper = new BlaiseHelper(connector);
            typeAttributes = helper.getConnectorAttributes(connector.getId());
            BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();
            attributeData = autoHelper.getJobAttributes(connector
                    .getDefaultFileProfileId());
        }
        request.setAttribute("typeAttributes", typeAttributes);
        request.setAttribute("attributeData", attributeData);

        request.setAttribute("tzs", CalendarHelper.getTimeZones(session));

        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {

    }
}
