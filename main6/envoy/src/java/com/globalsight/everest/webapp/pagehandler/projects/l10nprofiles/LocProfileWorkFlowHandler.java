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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfoKey;
import com.globalsight.everest.localemgr.CodeSetImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GlobalSightLocale;
/**
 * LocProfileWorkFlowHandler is the page handler used to interface between
 * the persistence layer and the UI pages that require information about
 * persistent objects.
 */

public class LocProfileWorkFlowHandler
    extends PageHandler
    implements LocProfileStateConstants
{
    //
    // PRIVATE CONSTANTS
    //
    private static final int NUM_PER_PAGE = 10;
    private static final int NEW = 1;
    private static final int MODIFY = 2;

    //
    // PRIVATE STATIC VARIABLES
    //
    private static final GlobalSightCategory s_logger =
        (GlobalSightCategory)GlobalSightCategory.
        getLogger(LocProfileWorkFlowHandler.class.getName());


        /**
         * Invokes this PageHandler
         * <p>
         * @param p_thePageDescriptor the page descriptor
         * @param p_theRequest the original request sent from the browser
         * @param p_theResponse the original response object
         * @param p_context context the Servlet context
         */
        public void invokePageHandler(WebPageDescriptor p_descriptor,
                                      HttpServletRequest p_request,
                                      HttpServletResponse p_response,
                                      ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
        {
        	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
    		{
    			p_response
    					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
    			return;
    		}
            HttpSession session = p_request.getSession();
            SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("LocProfileName " + 
                               sessionMgr.getAttribute(LOC_PROFILE_NAME));
                s_logger.debug("SourceLocaleId " +
                               sessionMgr.getAttribute(SOURCE_LOCALE_ID));
                s_logger.debug("LocProfileDescription " +
                               sessionMgr.getAttribute(LOC_PROFILE_DESCRIPTION));
                s_logger.debug("LocProfileProjectId " +
                               sessionMgr.getAttribute(LOC_PROFILE_PROJECT_ID));
                s_logger.debug("LocProfileTMUsageId " +
                               sessionMgr.getAttribute(LOC_PROFILE_TM_USAGE_ID));
                s_logger.debug("AutomaticDispatch " +
                               sessionMgr.getAttribute(AUTOMATIC_DISPATCH));
                s_logger.debug("LocProfileSQLScript " +
                               sessionMgr.getAttribute(LOC_PROFILE_SQL_SCRIPT));
                s_logger.debug("DispatchCondition " +
                               sessionMgr.getAttribute(DISPATCH_CONDITION));
            }
            dispatchJSP(p_descriptor, p_request, p_response, p_context);
        }

        /**
         * Invoke the correct JSP for this page
         */
        protected void dispatchJSP(WebPageDescriptor p_descriptor,
                                   HttpServletRequest p_request,
                                   HttpServletResponse p_response,
                                   ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
        {
            HttpSession session = p_request.getSession();
            SessionManager sessionMgr =
                (SessionManager) session.getAttribute(SESSION_MANAGER);

            linkNavigationBeans(p_descriptor, p_request);
            
            transferParams(p_request, sessionMgr);
            selectTemplatesForDisplay(p_request, session);
            sessionMgr.setAttribute(LocProfileStateConstants.TARGET_LOCALES, getTargetData(session, p_request));

            FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_LOCALIZATION_PROFILE);

            //Call parent invokePageHandler() to set link beans and invoke JSP
            super.invokePageHandler(p_descriptor, p_request,
                                    p_response,p_context);
        }

        private void linkNavigationBeans(WebPageDescriptor p_descriptor,
                                         HttpServletRequest p_request)
        {
            Enumeration en = p_descriptor.getLinkNames();
            boolean adding = false;
            while (en.hasMoreElements())
            {
                String linkName = (String) en.nextElement();
                String pageName = p_descriptor.getPageName();
                NavigationBean bean = new NavigationBean(linkName, pageName);
                p_request.setAttribute(linkName, bean);
            }
        }

        // create hashtable of target locale names and ids
        private Hashtable getTargetLocales(Locale p_uiLocale, long p_sourceLocaleId)
            throws EnvoyServletException
        {
            // gather target locale name id pairs for user to choose
            GlobalSightLocale source = (GlobalSightLocale)LocProfileHandlerHelper.getLocaleById(p_sourceLocaleId);
            Vector targetlocales = LocProfileHandlerHelper.getTargetLocales(source);
            //TomyD - use this code for sorting based on getDisplayName(uiLocale) by passing 2
            java.util.Collections.sort(targetlocales, new LocaleComparator(2, p_uiLocale));
            Hashtable targetLocale_pairs = new Hashtable();

            // for each target locale, get language to display
            long id = 0;
            GlobalSightLocale locale;         
            GlobalSightLocale localeToBeRemoved = null;
            Vector displayNames = new Vector();
            Vector targetIds = new Vector();
            for (int i = 0; i < targetlocales.size(); i++)
            {
                locale = (GlobalSightLocale)targetlocales.elementAt(i);
                id = locale.getId();
                if (id == p_sourceLocaleId)
                {
                    // remove this locale from target
                    localeToBeRemoved = locale;                
                }
                else
                {
                    displayNames.add(locale.getDisplayName(p_uiLocale));                
                    targetIds.add((new Long(id)).toString());
                }

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("LocProfileMainHandler: locale_id "+id+" lang "+
                                   locale.getDisplayLanguage()+" country "+locale.getDisplayCountry());
                }
            }
                    
            if (localeToBeRemoved != null)
            {
                targetlocales.removeElement(localeToBeRemoved);
            }
            targetLocale_pairs.put("object", targetIds);
            targetLocale_pairs.put("display", displayNames);
            return targetLocale_pairs;
        }

        // create hashtable of target locale names and ids according project
        private Hashtable getTargetLocalesByProject(Locale p_uiLocale, long p_sourceLocaleId, String p_project)
            throws EnvoyServletException
        {
            // gather target locale name id pairs for user to choose
            GlobalSightLocale source = (GlobalSightLocale)LocProfileHandlerHelper.getLocaleById(p_sourceLocaleId);
            Vector targetlocales = LocProfileHandlerHelper.getTargetLocalesByProject(source, p_project);
            //TomyD - use this code for sorting based on getDisplayName(uiLocale) by passing 2
            java.util.Collections.sort(targetlocales, new LocaleComparator(2, p_uiLocale));
            Hashtable targetLocale_pairs = new Hashtable();

            // for each target locale, get language to display
            long id = 0;
            GlobalSightLocale locale;         
            GlobalSightLocale localeToBeRemoved = null;
            Vector displayNames = new Vector();
            Vector targetIds = new Vector();
            for (int i = 0; i < targetlocales.size(); i++)
            {
                locale = (GlobalSightLocale)targetlocales.elementAt(i);
                id = locale.getId();
                if (id == p_sourceLocaleId)
                {
                    // remove this locale from target
                    localeToBeRemoved = locale;                
                }
                else
                {
                    displayNames.add(locale.getDisplayName(p_uiLocale));                
                    targetIds.add((new Long(id)).toString());
                }

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("LocProfileMainHandler: locale_id "+id+" lang "+
                                   locale.getDisplayLanguage()+" country "+locale.getDisplayCountry());
                }
            }
                    
            if (localeToBeRemoved != null)
            {
                targetlocales.removeElement(localeToBeRemoved);
            }
            targetLocale_pairs.put("object", targetIds);
            targetLocale_pairs.put("display", displayNames);
            return targetLocale_pairs;
        }

        // create hashtable of supported locale names and ids
        private Hashtable getSupportedLocales(Locale p_uiLocale)
            throws EnvoyServletException
        {
            // get all supported locales for user to choose
            Vector supportedLocales = LocProfileHandlerHelper.getSupportedLocales();
            //- use this code for sorting based on getDisplayName(uiLocale) by passing 2
            java.util.Collections.sort(supportedLocales, new LocaleComparator(2, p_uiLocale));
            Hashtable result = new Hashtable();

            // for each locale, get language to display
            long id = 0;
            GlobalSightLocale locale;         
            GlobalSightLocale localeToBeRemoved = null;
            Vector displayNames = new Vector();
            for (int i = 0; i < supportedLocales.size(); i++)
            {
                locale = (GlobalSightLocale)supportedLocales.elementAt(i);
                displayNames.add(locale.getDisplayName(p_uiLocale));                
            }
                    
            // need to send both the object and display name based on the locale (since display
            // name within applet will result in incorrect names for some locales due to
            // non-java 2 jvm)
            result.put("object", supportedLocales);
            result.put("display", displayNames);
            return result;
        }

        // create string of target encodings
        public String[] getTargetEncodings()
        throws EnvoyServletException
        {
            // gather target code sets for user to choose
            Vector codesets = LocProfileHandlerHelper.getAllCodeSets();

            int rowSize = codesets.size();
            String[] targetEncodings = new String[rowSize];
            CodeSetImpl codeset;
            for (int i=0; i < rowSize; i++)
            {
                codeset = (CodeSetImpl)codesets.elementAt(i);
                targetEncodings[i] = codeset.getCodeSet();
            }
            return targetEncodings;
        }

        //
        // PRIVATE SUPPORT METHODS
        //
        private String getTargetData(HttpSession p_session, HttpServletRequest p_request) throws EnvoyServletException
        {
            ResourceBundle bundle = getBundle(p_session);
            SessionManager sessionMgr = (SessionManager) p_session.getAttribute(SESSION_MANAGER);
            p_request.setAttribute("workflowInfos", sessionMgr.getAttribute("workflowInfos"));
            long sourceLocaleId = getSourceLocaleId(sessionMgr);
            Locale uiLocale = (Locale)p_session.getAttribute(UILOCALE);
            String projectID = (String)sessionMgr.getAttribute(LOC_PROFILE_PROJECT_ID);
            Hashtable targetlocale_pairs = getTargetLocalesByProject(uiLocale, sourceLocaleId, projectID);
            Vector targetLocales = (Vector)targetlocale_pairs.get("object");
            Vector targetLocaleInBox = new Vector();
    		for (int i = 0; i < targetLocales.size(); i++) {
    			targetLocaleInBox.addElement(LocProfileHandlerHelper
    					.getLocaleById(Long.parseLong((String) targetLocales
    							.elementAt(i))));
    		}
    		sessionMgr.setAttribute(WorkflowTemplateConstants.TARGET_LOCALE_IN_BOX,
    				targetLocaleInBox);
            Vector displayNames = (Vector)targetlocale_pairs.get("display");
            int pageNum = parseInt((String)p_request.getParameter(WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM));
            String targetId;
            if (pageNum > 0)
            {
                targetId =  (String)sessionMgr.getAttribute(WorkflowTemplateConstants.TARGET_LOCALE);
            }
            else 
            {
                targetId = (String)p_request.getParameter(WorkflowTemplateConstants.TARGET_LOCALE);
            }
            if(targetId != null)
            {
                sessionMgr.setAttribute(WorkflowTemplateConstants.TARGET_LOCALE, targetId);
            }
            String targets = "";
            if(targetId == null)
            {
                targets = "<OPTION VALUE='0' SELECTED>" +  bundle.getString("lb_choose") + 
                          "</option>";
            }
            else
            {
                targets = "<OPTION VALUE='0'>" +  bundle.getString("lb_choose") + 
                          "</option>";
            }
            String action = p_request.getParameter(LocProfileStateConstants.ACTION);
            int size = targetLocales.size();
            for(int i=0; i<size; i++)
            {
                String selected = "";
                if((targetId != null) && (targetId.equals((String)targetLocales.elementAt(i))))
                {
                    selected = "SELECTED";
                }
                targets += "<OPTION VALUE=" + targetLocales.elementAt(i) + " " + selected + ">" + displayNames.elementAt(i) + "</OPTION>";
            }
            targets += "</SELECT>";
            return targets;
        }
        private String getTargetLeverageData(HttpSession p_session) throws EnvoyServletException
        {
            Locale uiLocale = (Locale)p_session.getAttribute(UILOCALE);
            Hashtable leverageLocale_pairs = getSupportedLocales(uiLocale);
            Vector targetLeverageLocales = (Vector)leverageLocale_pairs.get("object");
            Vector displayNames = (Vector)leverageLocale_pairs.get("display");
            int size = targetLeverageLocales.size();
            String leverage = "<SELECT NAME='selectLeverageLocale' SIZE='3' MULTIPLE>";
            for(int i=0; i<size; i++)
            {
                leverage += "<OPTION VALUE=" + targetLeverageLocales.elementAt(i) + ">" + displayNames.elementAt(i) + "</OPTION>";
            }
            leverage += "</SELECT>";
            return leverage;
        }


        private void transferParams(HttpServletRequest p_request,
                                    SessionManager p_sessionMgr)
        {
            saveUTFParameterToSession(p_request, p_sessionMgr, LOC_PROFILE_NAME);
            saveUTFParameterToSession(p_request, p_sessionMgr, LOC_PROFILE_DESCRIPTION);
            saveParameterToSession(p_request, p_sessionMgr, LOC_PROFILE_SQL_SCRIPT);
            saveParameterToSession(p_request, p_sessionMgr, LOC_TM_PROFILE_ID);
            saveParameterToSession(p_request, p_sessionMgr, LOC_PROFILE_PROJECT_ID);
            saveParameterToSession(p_request, p_sessionMgr, JOB_PRIORITY);
            saveParameterToSession(p_request, p_sessionMgr, SOURCE_LOCALE_ID);
            saveParameterToSession(p_request, p_sessionMgr, LOC_PROFILE_TM_USAGE_ID);
            saveParameterToSession(p_request, p_sessionMgr, AUTOMATIC_DISPATCH);
        }
 
        // return the source locale id of this l10n profile.  In case of null, return 0.
        private long getSourceLocaleId(SessionManager p_sessionMgr)
        {
            long id = 0;
            try
            {
                id = Long.parseLong((String)p_sessionMgr.getAttribute(SOURCE_LOCALE_ID));
            }
            catch (NumberFormatException e)
            {
            }

            return id;
    }    
    // Select all workflow templates that should be displayed
    private void selectTemplatesForDisplay(HttpServletRequest p_request,
                                       HttpSession p_session)
        throws ServletException, IOException, EnvoyServletException

    {

        SessionManager sessionMgr = (SessionManager) p_session
            .getAttribute(SESSION_MANAGER);
        long sourceLocaleId = getSourceLocaleId(sessionMgr);
        String tempProjectId = (String)sessionMgr.getAttribute(
                                LOC_PROFILE_PROJECT_ID);
        long projectId = Long.parseLong(tempProjectId);
        
        // initially it'll be 1 since we're on the first page...
        int pageNum = parseInt((String)p_request.getParameter(
            WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM));

        String action = (String)p_request.getParameter(
            WorkflowTemplateConstants.ACTION);
        List templates = null;
        if(action!=null)
        {
            if((List)sessionMgr.getAttribute(WorkflowTemplateConstants
                                             .TEMPLATES) != null )
            {
                templates = (List)sessionMgr.getAttribute(
                    WorkflowTemplateConstants.TEMPLATES);
            }
            if(action.equals("removeCurrent"))
            {
            	long wfTemplateId = Long.parseLong(p_request.getParameter(WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID));
            	Vector<WorkflowInfos> workflowInfos = new Vector();
                if(sessionMgr.getAttribute("locprofile") == null)
                {
                	//new
                	workflowInfos = new Vector<WorkflowInfos>();
                }else{
                	long locProfileId = ((BasicL10nProfile)sessionMgr.getAttribute("locprofile")).getId();

                	WorkflowInfos workflowInfo = new WorkflowInfos(locProfileId, wfTemplateId, false);
                	workflowInfos = (Vector<WorkflowInfos>)sessionMgr.getAttribute(LocProfileStateConstants.WORKFLOW_INFOS);
                	for(int i = 0; i < workflowInfos.size(); i++)
                	{
                		WorkflowInfos wfi = workflowInfos.get(i);
                		if(workflowInfo.equals(wfi))
                		{
                			workflowInfo.setTargetLocale(wfi.getTargetLocale());
                			workflowInfos.set(i, workflowInfo);
                		}
                	}

                }
            	sessionMgr.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);
            	p_request.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);
            }
            if(action.equals(WorkflowTemplateConstants
                             .POPULATE_WORKFLOWS_ACTION) ||
                (action.equals(LocProfileStateConstants
                               .MODIFY_PROFILE_ACTION)) ||
                (action.equals(WorkflowTemplateConstants
                               .SAVE_WORKFLOW_TEMPLATE)))
            {
                long targetId = 0;
                if(p_request.getParameter(WorkflowTemplateConstants
                                          .TARGET_LOCALE) != null)
                {
                    sessionMgr.setAttribute(
                        WorkflowTemplateConstants.TARGET_LOCALE, p_request
                        .getParameter(WorkflowTemplateConstants.TARGET_LOCALE));
                    targetId = Long.parseLong((String)p_request.getParameter(
                        WorkflowTemplateConstants.TARGET_LOCALE));
                }
                else 
                {
                    if(sessionMgr.getAttribute(WorkflowTemplateConstants
                                               .TARGET_LOCALE) != null) 
                    {
                        targetId = Long.parseLong(
                            (String)sessionMgr.getAttribute(
                                WorkflowTemplateConstants.TARGET_LOCALE));
                    }
                }
                GlobalSightLocale target = null;
                if(targetId > 0)
                {
                    target = (GlobalSightLocale)LocProfileHandlerHelper
                        .getLocaleById(targetId);
                }
                // create an array to save translation workflow id and dtp workflow id
				String[] idArray = p_request
						.getParameterValues(WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID);
				List ids = (idArray == null ? new ArrayList(2) : Arrays
						.asList(idArray));
                Hashtable wftLocaleHash = new Hashtable();
                Vector<WorkflowInfos> workflowInfos = new Vector<WorkflowInfos>();
                wftLocaleHash = (Hashtable)sessionMgr.getAttribute(
                    WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH); 
                workflowInfos = (Vector<WorkflowInfos>)sessionMgr.getAttribute(
                		LocProfileStateConstants.WORKFLOW_INFOS);
                if(workflowInfos == null)
                {
                	workflowInfos = new Vector<WorkflowInfos>();
                }
                Boolean isSamePM = (Boolean)sessionMgr.getAttribute(
                    LocProfileStateConstants.IS_SAME_PROJECT_MANAGER);
                Boolean isSameProject = (Boolean)sessionMgr.getAttribute(
                    LocProfileStateConstants.IS_SAME_PROJECT);
                if(wftLocaleHash == null || 
                   (isSameProject != null && !isSameProject.booleanValue()) &&
                    (isSamePM != null && !isSamePM.booleanValue()))
                {
                    wftLocaleHash = new Hashtable();
                    workflowInfos = new Vector<WorkflowInfos>();
                }
                
                if (!ids.isEmpty() && target != null) {
                	long srcWfid = 0;
                	boolean isNew = false;
                	if(wftLocaleHash.get(target) != null)
                	{
                		srcWfid = Long.parseLong((String)((List) wftLocaleHash.get(target)).get(0));
                	}
                	else
                	{
                		isNew = true;
                	}
					wftLocaleHash.put(target, ids);
					
					if(isNew) {
    					if(sessionMgr.getAttribute("locprofile") != null)
    					{
    						long locProfileId = ((BasicL10nProfile)sessionMgr.getAttribute("locprofile")).getId();
    						workflowInfos.add(new WorkflowInfos(locProfileId,Long.parseLong(ids.get(0).toString()),true, target));
    					}
    					else
    					{
    						long locProfileId = -1L;
    						workflowInfos.add(new WorkflowInfos(locProfileId,Long.parseLong(ids.get(0).toString()),true, target));
    					}
					}
					else {
						long wfId = Long.parseLong((String)ids.get(0));
						BasicL10nProfile ln = (BasicL10nProfile) sessionMgr.getAttribute("locprofile");
						WorkflowInfos workflowInfo = new WorkflowInfos((ln == null)? 0 :ln.getId(), wfId, true);
						workflowInfo.setTargetLocale(target);

						for(int i = 0; i < workflowInfos.size(); i++)
						{
							if(target.equals(workflowInfos.get(i).getTargetLocale()))
							{
								workflowInfos.set(i, workflowInfo);
							}
						}
					}
	            	sessionMgr.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);
	            	p_request.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);

                    sessionMgr.setAttribute(WorkflowTemplateConstants
                                            .SELECTED_WORKFLOW, ids); 
                }
                sessionMgr.setAttribute(WorkflowTemplateConstants
                                        .LOCALE_WORKFLOW_HASH, 
                                        wftLocaleHash);
                templates = (List)populateWorkflowsForSelectedTarget(
                    target, sourceLocaleId, projectId);
            }
        }
        else
        {
            // remove target locale and workflow template Hashtable form session manager.
            // this will make sure that different project selections would not have conflict
            // with attached workflow template(s)
            sessionMgr.removeElement(WorkflowTemplateConstants.TARGET_LOCALE);
            sessionMgr.removeElement(WorkflowTemplateConstants.SELECTED_WORKFLOW); 
            sessionMgr.removeElement(WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH);
            sessionMgr.removeElement(WorkflowTemplateConstants.TEMPLATES);
        }
        
        int size = 0;
        List subList = new ArrayList();
        if (templates != null)
        {
            size = templates.size();
            subList = size > NUM_PER_PAGE ? 
                templates.subList(getStartIndex(pageNum, size),
                                  getEndingIndex(pageNum, size)) :
                templates;
        }
        
        sessionMgr.setAttribute(WorkflowTemplateConstants.TEMPLATES, subList);
        p_request.setAttribute(WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM, new Integer(pageNum));
        p_request.setAttribute("totalTemplates", new Integer(size));
        p_request.setAttribute("numOfPages", new Integer(getNumOfPages(size)));        
    }
    //////////////////////////////////////////////////////////////////////
    //  Begin: Paging Support
    //////////////////////////////////////////////////////////////////////
    // get total number of pages that can be displayed.
    private int getNumOfPages(int p_numOfTemplates)
    {
        //List of templates
        int remainder = p_numOfTemplates % NUM_PER_PAGE;

        return remainder == 0 ?
            (p_numOfTemplates / NUM_PER_PAGE) :
            ((p_numOfTemplates - remainder) / NUM_PER_PAGE) + 1;
    }

    // get the start index of the collection (this is inclusive)
    private int getStartIndex(int p_pageNum, int p_collectionSize)
    {
        int startIndex = (p_pageNum - 1) * NUM_PER_PAGE;
        return startIndex < p_collectionSize ? 
            startIndex :
            p_collectionSize;
    }

    // get the ending index for this page (this is exclusive).
    private int getEndingIndex(int p_pageNum, int p_collectionSize)
    {
        int endIndex = p_pageNum * NUM_PER_PAGE;
        return endIndex < p_collectionSize ?
            endIndex :
            p_collectionSize;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Paging Support
    //////////////////////////////////////////////////////////////////////

    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the default value instead (always 1) */
    private int parseInt(String p_string)
    {
        int intVal = 1;
        if (p_string != null)
        {
            try
            {
                intVal  = Integer.parseInt(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return intVal;
    }

    private List getWorkflowTemplateList( GlobalSightLocale target, 
                                          long p_sourceLocaleId, 
                                          long p_projectId)
    throws EnvoyServletException
    {
            List templates = null;
            GlobalSightLocale targetLocale = target;
            long p_targetLocaleId = 0;
            if(targetLocale != null)
            {
                p_targetLocaleId = targetLocale.getId();
                try
                {
                    templates =  new ArrayList(ServerProxy.getProjectHandler().
                                     getAllWorkflowTemplateInfosByParameters(p_sourceLocaleId,
                                                               p_targetLocaleId,
                                                               p_projectId));
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
            }
            return templates;
    }
    private List populateWorkflowsForSelectedTarget( GlobalSightLocale target, 
                                                     long p_sourceLocaleId, 
                                                     long p_projectId)
    throws EnvoyServletException
    {
        return getWorkflowTemplateList( target,p_sourceLocaleId,p_projectId);
    }
}

