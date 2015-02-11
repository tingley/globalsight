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
package com.globalsight.everest.webapp.pagehandler.terminology.maintenance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.TermEntryComparator;
import com.globalsight.everest.util.comparator.TermbaseInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.webservices.Ambassador;

public class TermbaseMaintenanceViaWebServicePageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
        		TermbaseMaintenanceViaWebServicePageHandler.class);

    static private ITermbaseManager s_manager = null;
	private Ambassador amb = new Ambassador();

    public TermbaseMaintenanceViaWebServicePageHandler()
    {
        super();

        if (s_manager == null) {
            try {
                s_manager = ServerProxy.getTermbaseManager();
            } catch (GeneralException ex) {
                // ignore.
            }
        }
    }

    /**
     * Invoke this PageHandler.
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
    	//get parameters
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = 
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        
        String action = (String)p_request.getParameter(TERMBASE_ACTION);
        String sourceLangName = (String) p_request.getParameter("sourcelocale");
        String targetLangName = (String) p_request.getParameter("targetlocale");
        String searchstr = (String) p_request.getParameter("searchstr");
        String matchtype = (String) p_request.getParameter("matchtype");
        String operation = (String) p_request.getParameter("operation");
        String sortBy = (String) p_request.getParameter("sortBy");
        String curPageNum = (String) p_request.getParameter("curPageNum");
        int intCurPageNum = 1;
        if ( operation != null && "search".equals(operation) ) {
        	intCurPageNum = 1;
        } else {
            if (curPageNum != null) {
            	intCurPageNum = (new Integer(curPageNum)).intValue();
            }
        }
        String num_on_per_page = 
            (String) p_request.getParameter("num_on_per_page");
        int intNum_on_per_page = 15;
        if ( num_on_per_page != null ) {
        	intNum_on_per_page = (new Integer(num_on_per_page)).intValue();
        }
        // all term-bases
        ArrayList allTBs = s_manager.getTermbaseList(uiLocale);

        // Selected TBs
        List listTBNames = new ArrayList();
        if ( operation != null && "sort".equals(operation)){
        	String selectedTBNames = p_request.getParameter("tbnames");
        	if (selectedTBNames != null){
        		StringTokenizer st = new StringTokenizer(selectedTBNames, ",");
        		while ( st.hasMoreTokens() ){
        			listTBNames.add(st.nextToken());
        		}
        	}
        } else {
        	String[] selectedTBNames = p_request.getParameterValues("tbnames");
            if ( selectedTBNames != null ){
            	for (int i=0; i<selectedTBNames.length; i++){
            		listTBNames.add(selectedTBNames[i]);
            	}
            }
        }

        // Sort by src_term ASC default
		int _sortBy = 5;
		try {
			_sortBy = Integer.valueOf(sortBy);
		} catch (Exception e){
			_sortBy = 5;
		}
		
		int totalNum = 0;
        if ( action.equals(WebAppConstants.TERMBASE_ACTION_SEARCH_TERM) ) 
        {
            //all records that match the search conditions
    		List allResultList = new ArrayList();
    		//records that match the requested current page
    		List neededResultList = new ArrayList();
        	//get the search results
        	if ( operation != null && "search".equals(operation) ) {
            	sessionMgr.removeElement("searchTBEntryResults");
                String sourcelocale = sourceLangName.substring(sourceLangName
                        .lastIndexOf("[") + 1, sourceLangName.length() - 1);
                String targetlocale = targetLangName.substring(targetLangName
                        .lastIndexOf("[") + 1, targetLangName.length() - 1);
            	try {
            		String results = "";
            		if ( listTBNames != null && listTBNames.size() > 0) {
            			for (int i=0; i<listTBNames.size(); i++){
           					try {
                                results = amb.searchTBEntries(
                                        getToken(p_request),
                                        (String) listTBNames.get(i), 
                                        searchstr.trim(), sourcelocale,
                                        targetlocale, new Double(matchtype)
                                                .doubleValue());
                                allResultList.addAll(convertTBSearchResults(results));
           					} catch (Exception ex) {}
            			}
            		}
            		sessionMgr.setAttribute("searchTBEntryResults", allResultList);
            	} catch (Exception ex){
            		CATEGORY.error(ex.getMessage());
            	}
        	} else {
        		allResultList = (List) sessionMgr.getAttribute("searchTBEntryResults");
        	}
        	totalNum = allResultList.size();
        	
            //sort the results
			Collections.sort(allResultList, new TermEntryComparator(_sortBy, uiLocale));
			//sub get the requested data
   			if ( allResultList != null && allResultList.size()>0 ) {
   				int firstInCurrPage = ( intCurPageNum - 1 ) * intNum_on_per_page;
   				int lastInCurrPage = intCurPageNum * intNum_on_per_page -1;
   				if (lastInCurrPage > allResultList.size() ) {
   					lastInCurrPage = allResultList.size()-1;
   				}
   				for (int i=firstInCurrPage; i<=lastInCurrPage; i++) {
   					try {
   						neededResultList.add( (HashMap)allResultList.get(i) );
   					} catch (Exception ex){}
   				}
   				
   		       	p_request.setAttribute("listResults", neededResultList);
   			}
        }

		p_request.setAttribute("totalNum", new Integer(totalNum));
        p_request.setAttribute("selectedTBNamesList", listTBNames);
        p_request.setAttribute("sourceLangName", sourceLangName);
        p_request.setAttribute("targetLangName", targetLangName);
        p_request.setAttribute("searchstr", searchstr);
        p_request.setAttribute("matchtype", matchtype);
        p_request.setAttribute("curPageNum", new Integer(intCurPageNum));
        p_request.setAttribute("sortBy", String.valueOf(_sortBy));
        p_request.setAttribute("action", String.valueOf(action));
        Collections.sort(allTBs, new TermbaseInfoComparator(0, uiLocale));
        p_request.setAttribute("namelist", allTBs);
        
        //set valid locales in request
        try {
        	setValidLocales(session, p_request);
        } catch (Exception ex) {
        	CATEGORY.error(ex.getMessage());
        }
        getTbListOfUser(p_request);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private String getToken(HttpServletRequest p_request){
    	HttpSession session = p_request.getSession();
    	String token = "";
    	try {
    	    String userId = getUser(session).getUserId();
    	    String pwd = getUser(session).getPassword();
    		token = amb.login(userId, pwd);
    	} catch (Exception ex){
    		CATEGORY.error(ex.getMessage());
    	}
		if ( token.indexOf("+_+") > -1 ) {
			token = removeCompanyName(token);
		}
		
		return token;
    }
	/**
	 * remove the company name including '+_+' in token
	 * @param fullToken
	 * @return realToken
	 */
	private String removeCompanyName(String fullToken){
		String realToken = fullToken;
		if (fullToken != null && !"".equals(fullToken.trim())){
			int index = fullToken.indexOf("+_+");
			if (index > 0){
				realToken = fullToken.substring(0,index);
			}
		}
		
		return realToken;
	}
    
    /**
     * Set valid locales in the request
     */
    @SuppressWarnings("unchecked")
    private void setValidLocales(HttpSession p_session,
            HttpServletRequest p_request) throws NamingException,
            RemoteException, GeneralException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAvailableLocales();
        Collections.sort(sources, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
                        .compareToIgnoreCase(
                                ((GlobalSightLocale) o2)
                                        .getDisplayName(Locale.US));
            }
        });
        p_request.setAttribute(LocalePairConstants.LOCALES, sources);
    }
    
    private List convertTBSearchResults(String resultsInXML) {
    	List rtnList = new ArrayList();
    	try {
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	DocumentBuilder db = dbf.newDocumentBuilder();
        	InputStream stream = new ByteArrayInputStream(resultsInXML
					.getBytes("UTF-8"));
			Document dc = db.parse(stream);
        	dc.normalize();
        	
        	NodeList nl_tbEntry = dc.getElementsByTagName("tbEntry");
        	NodeList nl_tbName = dc.getElementsByTagName("tbName");
        	NodeList nl_term = dc.getElementsByTagName("term");
        	NodeList nl_lang_name = dc.getElementsByTagName("lang_name");
        	NodeList nl_termContent = dc.getElementsByTagName("termContent");
        	
        	int entrySize = nl_tbEntry.getLength();
        	if ( entrySize > 0 ) {
        		for (int j=0; j<entrySize; j++){
        			HashMap map = new HashMap();
        			
            		String tbname = nl_tbName.item(j).getChildNodes().item(0).getNodeValue();
            		String lang_name1 = nl_lang_name.item(2*j).getChildNodes().item(0).getNodeValue();
            		String term_content1 = nl_termContent.item(2*j).getChildNodes().item(0).getNodeValue();
            		String issrc1 = null;
            		if (nl_term.item(2*j).getAttributes().item(0) != null) {
                		issrc1 = nl_term.item(2*j).getAttributes().item(0).getNodeValue();            			
            		}
            		String lang_name2 = nl_lang_name.item(2*j+1).getChildNodes().item(0).getNodeValue();
            		String term_content2 = nl_termContent.item(2*j+1).getChildNodes().item(0).getNodeValue();
//            		String issrc2 = nl_term.item(2*j+1).getAttributes().item(0).getNodeValue();
            		
            		String src_lang = "";
            		String src_term = "";
            		String target_lang = "";
            		String target_term = "";
            		if ( "true".equals(issrc1) || issrc1 == null ){
            			src_lang = lang_name1;
            			src_term = term_content1;
            			target_lang = lang_name2;
            			target_term = term_content2;
            		} else {
            			src_lang = lang_name2;
            			src_term = term_content2;
            			target_lang = lang_name1;
            			target_term = term_content1;
            		}
            		
            		map.put("tbname", tbname);
            		map.put("src_lang", src_lang);
            		map.put("src_term", src_term);
            		map.put("target_lang", target_lang);
            		map.put("target_term", target_term);
            		
            		rtnList.add(map);
        		}
        	}
    	} catch (Exception ex){
    		CATEGORY.error(ex.getMessage());
    	}
    	return rtnList;
    }
    /**
     * get tmList for this user
     * @param p_request
     * 
     * @author Leon Song
     * @since 8.0
     */
    private void getTbListOfUser(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) p_request.getSession()
                .getAttribute(SESSION_MANAGER);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper.getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany.getEnableTBAccessControl();
        
        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        
        ArrayList tbListOfUser = new ArrayList();
        if (enableTBAccessControl&&!isAdmin && !isSuperAdmin)
        {
            //TB Access Control is enable
            ProjectTMTBUsers ptbUsers = new ProjectTMTBUsers();
            List termbaseIds = ptbUsers.getTList(userId, "TB");
            Iterator it = termbaseIds.iterator();
            while (it.hasNext())
            {
                long id = ((BigInteger) it.next()).longValue();
                Termbase tb = TermbaseList.get(id);
                if (tb != null)
                {
                    tbListOfUser.add(id);
                }
            }
            sessionMgr.setAttribute("tbListOfUser", tbListOfUser);
        }
    }
}

