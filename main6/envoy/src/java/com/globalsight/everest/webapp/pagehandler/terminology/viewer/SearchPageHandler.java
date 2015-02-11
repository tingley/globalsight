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

package com.globalsight.everest.webapp.pagehandler.terminology.viewer;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.MTTranslationHandler;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseImpl;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

public class SearchPageHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(MTTranslationHandler.class);
    static final int maxHits = 20;

    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }
    
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }
    
    @ActionHandler(action = "searchTermHitList", formClass = "")
    public void searchTermHitList(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession();

        ITermbase termbase = (ITermbase) session
                .getAttribute(WebAppConstants.TERMBASE);
        
        String sourceLan = new String();
        String targetLan = new String();
        String query = new String();
        String type = new String();
        
        String pageDirection = request.getParameter("direction");

        String hitlist = new String();
        ServletOutputStream out = response.getOutputStream();
        Map<String, String> returnValue = new HashMap<String, String>();
        
        int currentPage = 0;
        
        if(pageDirection.equals("current")) {
            sourceLan = 
                (String) request.getParameter(WebAppConstants.TERMBASE_SOURCE);
            targetLan = (String) request.getParameter("target");
            query = 
                (String) request.getParameter(WebAppConstants.TERMBASE_QUERY);
            type = (String) request.getParameter("type");
            
            // Un-escape source language, target language and query.
            if (sourceLan != null && sourceLan.length() > 0) {
                sourceLan = EditUtil.unescape(sourceLan);
            }
            if (targetLan != null && targetLan.length() > 0) {
                targetLan = EditUtil.unescape(targetLan);
            }
            if (query != null && query.length() > 0) {
                query = EditUtil.unescape(query);
            }

            session.setAttribute("currentPage", 0);
            session.setAttribute("source", sourceLan);
            session.setAttribute("target", targetLan);
            session.setAttribute("query", query);
            session.setAttribute("type", type);
        }
        else {
            if(pageDirection.equals("next")){
                currentPage = (Integer)session.getAttribute("currentPage") + 1;
            }
            else if(pageDirection.equals("pre")) {
                currentPage = (Integer)session.getAttribute("currentPage") - 1;
            }
            
            sourceLan = (String) session.getAttribute("source");
            targetLan = (String) session.getAttribute("target");
            query = (String) session.getAttribute("query");
            type = (String) session.getAttribute("type");
        }
        
        try {
            if(currentPage < 0) {
                hitlist ="isFirst";
            }
            else {
                hitlist = termbase.search(sourceLan, targetLan, query, type,
                        maxHits, currentPage * maxHits);
                
                if(!hitlist.equals("isLast")) {
                    session.setAttribute("currentPage", currentPage);
                }
            }
        }
        catch(Exception e) {
         // Let client know about the exception
            hitlist = "<exception>" + EditUtil.encodeXmlEntities(e.getMessage() 
                + "@@@@@" + EditUtil.encodeXmlEntities(e.getMessage())) +
                "</exception>";
        }
        finally
        {
            returnValue.put("hitlist", hitlist);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "searchEntry", formClass = "")
    public void searchEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession();

        ITermbaseImpl termbase = (ITermbaseImpl) session
                .getAttribute(WebAppConstants.TERMBASE);
        String sourceLan = (String) request
                .getParameter(WebAppConstants.TERMBASE_SOURCE);
        // Un-escape source language, target language.
        if (sourceLan != null && sourceLan.length() > 0) {
            sourceLan = EditUtil.unescape(sourceLan);
        }
        String targetLan = (String) request.getParameter(WebAppConstants.TERMBASE_TARGET);
        if (targetLan != null && targetLan.length() > 0) {
            targetLan = EditUtil.unescape(targetLan);
        }
        
        String conceptId = (String)request.getParameter(
                WebAppConstants.TERMBASE_CONCEPTID);
        String termId = (String)request.getParameter(
                WebAppConstants.TERMBASE_TERMID);
        
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();

        // fix for the sort order for the entries, related to GBS-1693
        SessionInfo sessionInfo = termbase.getSession();
        sessionInfo.setSourceLan(sourceLan);
        sessionInfo.setTargetLan(targetLan);
        termbase.setSession(sessionInfo);
        try {
            result = termbase.getEntryForBrowser(Long.parseLong(conceptId));
        }
        catch(Exception e) {
            result = "<noresult></noresult>";
        }
        finally
        {
            returnValue.put("entry", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "addEntry", formClass = "")
    public void addEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession();
        ITermbase termbase = (ITermbase)session.getAttribute(
                WebAppConstants.TERMBASE);
         
        String entry = request.getParameter("entryXML");
        String isReIndex = request.getParameter("isReIndex");
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        String result = "";

        try {
            result = termbase.addEntry(entry) + "";
        }
        catch(Exception e) {
            result = "<exception></exception>";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
        
        if("true".equals(isReIndex)){
            IIndexManager indexer = termbase.getIndexer();
            indexer.doIndex();
        }
    }
    
    @ActionHandler(action = "updateEntry", formClass = "")
    public void updateEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession();
        ITermbase termbase = (ITermbase)session.getAttribute(
                WebAppConstants.TERMBASE);
         
        String entry = request.getParameter("entryXML");
        String lock = request.getParameter("lock");
        String isReIndex = request.getParameter("isReIndex");
        long conceptId = Long.parseLong(request.getParameter("conceptId"));
        
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        String result = "";

        try {
            termbase.updateEntry(conceptId, entry, lock);
            result = "sucess";
        }
        catch(Exception e) {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
        
        if("true".equals(isReIndex)){
            IIndexManager indexer = termbase.getIndexer();
            indexer.doIndex();
        }
    }
    
    @ActionHandler(action = "deleteEntry", formClass = "")
    public void DeleteEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession();
        ITermbase termbase = (ITermbase)session.getAttribute(
                WebAppConstants.TERMBASE);
        long conceptId = Long.parseLong(request.getParameter("conceptId"));
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        String result = "";
        
        try {
            termbase.deleteEntry(conceptId);
            result = "success";
        }
        catch(Exception e) {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "valadateEntry", formClass = "")
    public void valadateEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try
        {
            String entry = request.getParameter("entryXML");
            HttpSession session = request.getSession();
            ITermbase termbase = (ITermbase) session
                    .getAttribute(WebAppConstants.TERMBASE);
            result = termbase.validateEntry(entry);
        }
        catch (Exception e)
        {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "ReIndexEntry", formClass = "")
    public void ReIndexEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        HttpSession session = request.getSession();
        ITermbase termbase = (ITermbase) session
                .getAttribute(WebAppConstants.TERMBASE);
        IIndexManager indexer;
        try
        {
            try {
                indexer = termbase.getIndexer();
            }
            catch(Exception e) {
                result = "indexing";
                throw new Exception(e);
            }
            
            indexer.doIndex();
        }
        catch (Exception e)
        {
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "LockEntry", formClass = "")
    public void LockEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = "";
        long conceptId = Long.parseLong(request.getParameter("conceptId"));
        boolean steal = Boolean.parseBoolean(request.getParameter("steal"));
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try
        {
            HttpSession session = request.getSession();
            ITermbase termbase = (ITermbase) session
                    .getAttribute(WebAppConstants.TERMBASE);
            result = termbase.lockEntry(conceptId, steal);
            
            if (result.length() == 0)
            {
                result = termbase.getLockInfo(conceptId);
            }
        }
        catch (Exception e)
        {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "UnLockEntry", formClass = "")
    public void UnLockEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = "";
        long conceptId = Long.parseLong(request.getParameter("conceptId"));
        String lockStr = request.getParameter("lockStr");
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try
        {
            HttpSession session = request.getSession();
            ITermbase termbase = (ITermbase) session
                    .getAttribute(WebAppConstants.TERMBASE);
            termbase.unlockEntry(conceptId, lockStr);
            result = "sucess";
        }
        catch (Exception e)
        {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "GetStatistics", formClass = "")
    public void GetStatistics(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try
        {
            HttpSession session = request.getSession();
            ITermbase termbase = (ITermbase) session
                    .getAttribute(WebAppConstants.TERMBASE);
            result = termbase.getStatisticsWithoutIndexInfo();
        }
        catch (Exception e)
        {
            result = "error";
        }
        finally
        {
            returnValue.put("result", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "getDefaultModel", formClass = "")
    public void getDefaultModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            HttpSession session = request.getSession();
            ITermbase termbase = (ITermbase) session
                    .getAttribute(WebAppConstants.TERMBASE);
            IUserdataManager manager = termbase.getUserdataManager();
            String companyId = CompanyThreadLocal.getInstance().getValue();
            result = manager.getDefaultObject(companyId);
        }
        catch(Exception e) {
            result = "error";
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
}
