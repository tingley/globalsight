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
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * To remove applet for pop edit, we add the online service for ajax.
 *
 */
public class OnlineService extends HttpServlet
{
    private static final Logger CATEGORY = Logger.getLogger(OnlineService.class);
    private static final long serialVersionUID = 1L;
    private HttpServletRequest request;
    private PrintWriter writer;
    private long companyId;
    String m_userId;
    
    private OnlineHelper helper = null;
    private SegmentView segmentView = null;
    private EditorState state = null;
    
    /**
     * The base method.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        boolean check = setCompanyId();
        HttpSession session = request.getSession(false);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        segmentView = (SegmentView) sessionMgr
                .getAttribute(WebAppConstants.SEGMENTVIEW);
        
        state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
        
        if (segmentView != null)
        {
            helper = new OnlineHelper();
            try
            {
                String target =  GxmlUtil.getInnerXml(segmentView.getTargetSegment());
                helper.setInputSegment(target, "", segmentView.getDataType());
                
                if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
                {
                    helper.getVerbose();
                }
                else
                {
                    helper.getCompact();
                }
                
                String seg = getSource();
                helper.setInputSegment(seg, "", segmentView.getDataType());
                
                if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
                {
                    helper.getVerbose();
                }
                else
                {
                    helper.getCompact();
                }
            }
            catch (Exception e1)
            {
                CATEGORY.error(e1);
            }
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
                OnlineService.class.getMethod(method).invoke(
                        OnlineService.this);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not invoke the method:" + method);
            CATEGORY.error(e);
        }
    }

    /**
     * Sets the company id.
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
     * Write the string.
     */
    private void writeString(String s)
    {
        writer.write(s);
        writer.close();
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
     * Get the source from the segment view.
     * 
     * @return
     */
    private String getSource()
    {
        return GxmlUtil.getInnerXml(segmentView.getSourceSegment());
    }
    
    /**
     * Gets the target diplomat and write back.
     * 
     * @throws PseudoParserException
     */
    public void getTargetDiplomat() throws Exception
    {
        String text = getTarget();
        String seg = helper.getTargetDiplomat(text);
        writeString(seg);
    }
    
    /**
     * Gets ptag to native map table and write back.
     * 
     * @throws Exception
     */
    public void getPtagToNativeMappingTable() throws Exception
    {
        String seg = helper.getPtagToNativeMappingTable();
        writeString(seg);
    }
    
    /**
     * Gets html segment and write back.
     * 
     * @throws Exception
     */
    public void getHtmlSegment() throws Exception
    {
        String text = getTarget();
        String isFromTarget = request.getParameter("isFromTarget");
        helper.setInputSegment(text, "", segmentView.getDataType(), Boolean.parseBoolean(isFromTarget));
        
        String result = text;
        if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
        {
            result = helper.makeVerboseColoredPtags(text);
        }
        else
        {
            result = helper.makeCompactColoredPtags(text);
        }

        writeString(result);
    }
    
    private String getTarget() throws UnsupportedEncodingException
    {
        String text = request.getParameter("text");
        return text;
    }
    
    /**
     * Init the target
     */
    public void initTarget() throws Exception
    {
        String text = getTarget();
        String result = text;
        
        OnlineTagHelper applet = new OnlineTagHelper();
        applet.setInputSegment(text, "", segmentView.getDataType());
        if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
        {
            result = applet.getVerbose();
            if (Boolean.parseBoolean(request.getParameter("colorPtags")))
            {
                result = applet.makeVerboseColoredPtags(text);
            }
        }
        else
        {
            result = applet.getCompact();
            if (Boolean.parseBoolean(request.getParameter("colorPtags")))
            {
                result = applet.makeCompactColoredPtags(text);
            }
        }
        
        writeString(result);
    }
    
    /**
     * Do error check and send the result back.
     * 
     * @throws Exception
     */
    public void doErrorCheck() throws Exception
    {
        String text = getTarget();
        Map<String, String> m = new HashMap<String, String>();
        
        helper.setUntranslateStyle(SegmentUtil2.getTAGS());
        String msg = helper.errorCheck(text, getSource(), 0, "UTF8", 0, "UTF8");
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
    
    /**
     * Do error check and send the result back. Note that this method only used
     * for save form richeditor with firefox. Because there are something wrong
     * with firefox and ajax.
     * 
     * @throws Exception
     */
    public void doErrorCheck2() throws Exception
    {
        String text = getTarget();
        Map<String, String> m = new HashMap<String, String>();
        
        helper.setUntranslateStyle(SegmentUtil2.getTAGS());
        String msg = helper.errorCheck(text, getSource(), 0, "UTF8", 0, "UTF8");
        String internalTagMsg = helper.getInternalErrMsg();
        String newTarget = "";
        if (msg == null || msg.length() == 0)
        {
            newTarget = helper.getNewPTagTargetString();
        }
        
        if (msg != null)
            m.put("msg", msg);
        
        if (internalTagMsg != null)
            m.put("internalTagMsg", internalTagMsg);
        
        if (newTarget == null || newTarget.length() == 0)
        {
            newTarget = helper.getTargetDiplomat(text);
        }
        m.put("newTarget", newTarget);
        
        writeString(JsonUtil.toJson(m));
    }
}
