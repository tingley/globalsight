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
package com.globalsight.everest.util.online;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineHelper;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoOverrideItemException;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * To remove applet for inline edit, we add the online service for ajax.
 *
 */
public class Online2Service extends HttpServlet
{
    private static final Logger CATEGORY = Logger.getLogger(Online2Service.class);
    private static final long serialVersionUID = 1L;
    private HttpServletRequest request;
    private SessionManager sessionMgr;
    private PrintWriter writer;
    private long companyId;
    
    private OnlineHelper helper = null;
    private OnlineHelper tmHelper = null;
    
    /**
     * The base method.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        boolean check = setCompanyId();
        HttpSession session = request.getSession(false);
        
        sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        helper = (OnlineHelper) sessionMgr.getAttribute("OnlineHelper");
        if (helper == null)
        {
            helper = new OnlineHelper();
            sessionMgr.setAttribute("OnlineHelper", helper);
        }
        
        response.setCharacterEncoding(request.getCharacterEncoding());
        String method = request.getParameter("action");
        try
        {
            writer = response.getWriter();
            if (!check)
            {
                writer.write("failed");
            }
            else
            {
                Online2Service.class.getMethod(method).invoke(
                        Online2Service.this);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not invoke the method:" + method);
            CATEGORY.error(e);
        }
    }

    /**
     * Sets company id.
     * @return
     */
    public boolean setCompanyId()
    {
        String companyName = UserUtil.getCurrentCompanyName(request);
        if (StringUtil.isNotEmpty(companyName))
        {
            try
            {
                companyId = ServerProxy.getJobHandler().getCompany(companyName)
                        .getIdAsLong();
                CompanyThreadLocal.getInstance().setIdValue("" + companyId);
                return true;
            }
            catch (Exception e)
            {
                CATEGORY.error("Can not get the Company!");
            }
        }
        return false;
    }
    
    /**
     * Write the string back.
     * 
     * @param s
     */
    private void writeString(String s)
    {
        writer.write(s);
        writer.close();
    }
    
    /**
     * Gets the parameter from request with specified name.
     * 
     * @param name
     * @return
     * @throws UnsupportedEncodingException 
     */
    private String get(String name) throws UnsupportedEncodingException
    {
        String text = request.getParameter(name);
        if (text == null)
            return null;
        
        return text;
    }
    
    /**
     * Inits the help with gxml.
     * 
     * @throws PseudoOverrideItemException
     */
    public void initGxml() throws Exception
    {
        String gxml = get("gxml");
        helper.setInputSegment(gxml, "", get("datatype"));   
    }
    
    /**
     * Init the tm helper.
     * 
     * @throws Exception
     */
    public void initTmHelper() throws Exception
    {
        tmHelper = (OnlineHelper) sessionMgr.getAttribute("tmOnlineHelper");
        if (tmHelper == null)
        {
            tmHelper = new OnlineHelper();
            sessionMgr.setAttribute("tmOnlineHelper", tmHelper);
        }
        
        String gxml = get("gxml");
        tmHelper.setInputSegment(gxml, "", get("datatype"));
        tmHelper.getCompact();
        
        writeString("end");
    }
    
    /**
     * Inits the helper with display html.
     * 
     * @throws Exception
     */
    public void initDisplayHtml() throws Exception
    {
        String gxml = get("gxml");
        initGxml();
        
        String result;
        if (Boolean.parseBoolean(get("ptagsVerbose")))
        {
            helper.getVerbose();
            result = helper.makeVerboseColoredPtags(gxml);
        }
        else
        {
            helper.getCompact();
            result = helper.makeCompactColoredPtags(gxml);
        }
        
        writeString(result);
    }
    
    /**
     * Gets target display html.
     * 
     * @throws Exception
     */
    public void getTargetDisplayHtml() throws Exception
    {
        getCompactColoredPtags();
    }
    
    /**
     * Gets target display html for preview.
     * 
     * @throws Exception
     */
    public void getTargetDisplayHtmlForPreview() throws Exception
    {
        String gxml = get("gxml");
        initGxml();  
        helper.getVerbose();
        String result = helper.makeInlineVerboseColoredPtags(gxml);
        
        writeString(result);
    }
    
    /**
     * Gets target display html for tm preview.
     * 
     * @throws Exception
     */
    public void getTargetDisplayHtmlForTmPreview() throws Exception
    {
        getCompactColoredPtags();
    }
    
    /**
     * Gets target display html for tm preview.
     * 
     * @throws Exception
     */
    public void getTargetDisplayHtmlForTmPreview2() throws Exception
    {
        String gxml = get("gxml");
        tmHelper.setInputSegment(gxml, "", get("datatype"));   
        
        String result;
        if (Boolean.parseBoolean(get("ptagsVerbose")))
        {
            tmHelper.getVerbose();
            result = tmHelper.makeVerboseColoredPtags(gxml);
        }
        else
        {
            tmHelper.getCompact();
            result = tmHelper.makeCompactColoredPtags(gxml);
        }
       
        writeString(result);
    }
    
    /**
     * Gets source display html.
     * 
     * @throws Exception
     */
    public void getSourceDisplayHtml() throws Exception
    {
        getCompactColoredPtags();
    }
    
    /**
     * Gets compact colored ptags.
     * 
     * @throws Exception
     */
    public void getCompactColoredPtags() throws Exception
    {
        String gxml = get("gxml");
        initGxml();
        helper.getCompact(); 
        String result = helper.makeCompactColoredPtags(gxml);
        
        writeString(result);
    }
    
    /**
     * Gets string with compact colored ptags.
     * 
     * @throws Exception
     */
    public void getPTagString() throws Exception
    {
        getCompactColoredPtags();
    }
    
    /**
     * Gets ptag string  with OnlineHelper.
     * 
     * @throws DiplomatBasicParserException
     */
    public void getPtagString() throws DiplomatBasicParserException
    {
        writeString(helper.getPtagString());
    }
    
    
    /**
     * Gets target diplomat.
     * 
     * @throws Exception
     */
    public void getTargetDiplomat() throws Exception
    {
        String gxml = get("gxml");
        String result = helper.getTargetDiplomat(gxml);
        
        writeString(result);
    }
    
    /**
     * Do error check and write back the result.
     * 
     * @throws Exception
     */
    public void doErrorCheck() throws Exception
    {
        String text = get("text");
        String source = get("source");
        
        Map<String, String> m = new HashMap<String, String>();
        
        helper.setUntranslateStyle(SegmentUtil2.getTAGS());
        String msg = helper.errorCheck(text, source, 0, "UTF8", 0, "UTF8");
        String internalTagMsg = helper.getInternalErrMsg();
        String newTarget = "";
        if (msg == null || msg.length() == 0)
        {
            newTarget = helper.getNewPTagTargetString();
            if (newTarget != null && newTarget.length() > 0)
            {
                newTarget = helper.getTargetDiplomat(newTarget);
            }
        }
        
        if (msg != null)
            m.put("msg", msg);
        
        if (internalTagMsg != null)
            m.put("internalTagMsg", internalTagMsg);
        
        if (newTarget != null)
            m.put("newTarget", newTarget);
        
        writeString(JsonUtil.toJson(m));
    }
    
    public void getPtagToNativeMappingTable() throws Exception
    {
        String seg = helper.getPtagToNativeMappingTable();
        writeString(seg);
    }
 }
