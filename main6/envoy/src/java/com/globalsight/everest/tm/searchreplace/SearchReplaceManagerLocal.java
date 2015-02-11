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
package com.globalsight.everest.tm.searchreplace;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManagerException;
import com.globalsight.everest.tm.TmManagerExceptionMessages;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TuQueryResult;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

public class SearchReplaceManagerLocal implements SearchReplaceManager,
		TmManagerExceptionMessages, Serializable
{
	static private final Logger CATEGORY = Logger
			.getLogger(SearchReplaceManagerLocal.class);

	private ArrayList<Tm> m_tms; // list of Tm objects

	private SessionInfo m_session;

	private static int searchCounter = 0;

	private static int replaceCounter = 0;

	private ProcessStatus m_listener;

	/** Read 10 TUs at a time. */
	private int m_pageSize = 10;

	//
	// Constructor
	//

	public void attachListener(IProcessStatusListener p_listener)
	{
		m_listener = (ProcessStatus) p_listener;
	}

	public void detachListener(IProcessStatusListener p_listener)
	{
		m_listener = null;
	}

	public SearchReplaceManagerLocal(ArrayList<Tm> p_tms, SessionInfo p_session)
	{
		m_tms = p_tms;
		m_session = p_session;
	}

	//
	// Interface Methods
	//

	public void search(String p_queryString, GlobalSightLocale p_sourceLocale,
			GlobalSightLocale p_targetLocale, boolean p_caseSensitiveSearch, 
			Map<Long, Integer> mapOfTmIdIndex)
			throws TmManagerException, RemoteException
	{
		doSearch(p_queryString, p_sourceLocale, p_targetLocale,
				p_caseSensitiveSearch, mapOfTmIdIndex);
	}

	private void doSearch(final String p_queryString,
			final GlobalSightLocale p_sourceLocale,
			final GlobalSightLocale p_targetLocale,
			final boolean p_caseSensitiveSearch, 
			final Map<Long, Integer> mapOfTmIdIndex)
	{
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					runSearch(p_queryString, p_sourceLocale, p_targetLocale,
							p_caseSensitiveSearch, mapOfTmIdIndex);
				}
				catch (Exception e)
				{
					CATEGORY.error("SearchReplaceManagerLocal::doSearch", e);
				}
			}
		};

		Thread t = new MultiCompanySupportedThread(runnable);
		t.setName("TMSEARCHER" + String.valueOf(searchCounter++));
		t.start();
	}

	private void runSearch(String p_queryString,
			GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
			boolean p_caseSensitiveSearch, Map<Long, Integer> mapOfTmIdIndex) throws TmManagerException,
			RemoteException
	{
		ArrayList targetLocales = new ArrayList();
		targetLocales.add(p_targetLocale);

		TmConcordanceResult result = null;

		Connection con = null;

		try
		{
			speak(25, 25, "lb_preparation_done", "Preparation Done", null);

			TmCoreManager mgr = LingServerProxy.getTmCoreManager();
			List<TMidTUid> queryResult = mgr.tmConcordanceQuery(m_tms, 
			                p_queryString, p_sourceLocale, p_targetLocale, 
			                convertMap(mapOfTmIdIndex));

	        speak(50, 50, "lb_query_done", "Query Done", null);

            result = new TmConcordanceResult(p_sourceLocale,
                    targetLocales, new TuQueryResult(queryResult, m_pageSize));
	        
			speak(75, 75, "lb_results_partially_retrieved", "Results Partially Retrieved", null);
			Map<Long,String> tmIdName = generateMapForIdName(m_tms);
			result.setMapIdName(tmIdName);
			m_listener.setResults(result);
			speak(100, 100, "lb_process_done", "Done", null);
		}
		catch (Exception e)
		{
			try
			{
			    e.printStackTrace();
				speak(100, 100, "lb_error_in_search",  "Error in Search Operation", e);
			}
			catch (IOException ie)
			{
				throw new TmManagerException(MSG_FAILED_TO_SEARCH, null, ie);
			}
		}
		finally
		{
			SqlUtil.fireConnection(con);
		}
	}

	private Map<Tm, Integer> convertMap(Map<Long, Integer> orig) {
	    if (orig == null) {
	        return null;
	    }
	    Map<Tm, Integer> m = new HashMap<Tm, Integer>();
        for (Map.Entry<Long, Integer> e : orig.entrySet()) {
	        // Gross
	        for (Tm tm : m_tms) {
	            if (tm.getId() == e.getKey()) {
	                m.put(tm, e.getValue());
	                break;
	            }
	        }
	    }
        return m;
	}
	
	private Map<Long, String> generateMapForIdName(ArrayList m_tms) {
		Map<Long, String> map = new HashMap<Long, String>();
		for(int i = 0; i < m_tms.size(); i++)
		{
			ProjectTM tm = (ProjectTM) m_tms.get(i);
			map.put(tm.getId(), tm.getName());
		}
		return map;
	}

	/**
	 * Remove this after making appropriate changes to
	 * BrowseCorpusMainHandler.java
	 * 
	 * This appears to be no longer used?
	 */
	@Deprecated
	public TmConcordanceResult searchIt(String p_queryString,
			GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
			boolean p_caseSensitiveSearch) throws TmManagerException,
			RemoteException
	{
		ArrayList targetLocales = new ArrayList();
		targetLocales.add(p_targetLocale);

		TmConcordanceResult result = null;
		

		Connection con = null;

		try
		{
		    TmCoreManager mgr = LingServerProxy.getTmCoreManager();
            List<TMidTUid> queryResult = mgr.tmConcordanceQuery(m_tms, 
                            p_queryString, p_sourceLocale, p_targetLocale, 
                            null);

            speak(50, 50, "lb_query_done", "Query Done", null);

            result = new TmConcordanceResult(p_sourceLocale,
                    targetLocales, new TuQueryResult(queryResult, m_pageSize));
		}
		catch (Exception e)
		{
			throw new TmManagerException(MSG_FAILED_TO_SEARCH, null, e);
		}
		finally
		{
			SqlUtil.fireConnection(con);
		}
		return result;
	}

	public ArrayList replace(String p_old, String p_new, ArrayList p_tuvs,
			boolean p_caseSensitiveSearch) throws GeneralException,
			RemoteException
	{
		ArrayList notReplaced = new ArrayList();
		ArrayList replaced = new ArrayList(p_tuvs.size());

		// all TUVs in p_tuvs should be the same locale
		Locale locale = null;
		if (p_tuvs.size() > 0)
		{
			locale = ((SegmentTmTuv) p_tuvs.get(0)).getLocale().getLocale();

			GxmlElementSubstringReplace substringReplacer = new GxmlElementSubstringReplace(
					p_old, p_new, p_caseSensitiveSearch, locale);

			for (int i = 0, max = p_tuvs.size(); i < max; i++)
			{
				SegmentTmTuv tuv = (SegmentTmTuv) p_tuvs.get(i);

				if (replaceSubstring(tuv, substringReplacer))
				{
					replaced.add(tuv);
				}
				else
				{
					notReplaced.add(tuv);
				}
			}

			// save Tuvs
			updateTuvs(replaced);
		}
		return replaced;
	}

	public ArrayList replace(String p_old, String p_new, ArrayList p_tuvs,
			boolean p_caseSensitiveSearch, String p_userId)
			throws GeneralException, RemoteException
	{
		ArrayList notReplaced = new ArrayList();
		ArrayList replaced = new ArrayList(p_tuvs.size());

		// all TUVs in p_tuvs should be the same locale
		Locale locale = null;
		if (p_tuvs.size() > 0)
		{
			locale = ((SegmentTmTuv) p_tuvs.get(0)).getLocale().getLocale();

			GxmlElementSubstringReplace substringReplacer = new GxmlElementSubstringReplace(
					p_old, p_new, p_caseSensitiveSearch, locale);

			for (int i = 0, max = p_tuvs.size(); i < max; i++)
			{
				SegmentTmTuv tuv = (SegmentTmTuv) p_tuvs.get(i);

				if (replaceSubstring(tuv, substringReplacer))
				{
					tuv.setModifyUser(p_userId);
					replaced.add(tuv);
				}
				else
				{
					notReplaced.add(tuv);
				}
			}

			// save Tuvs
			updateTuvs(replaced);
		}
		return replaced;
	}

	//
	// Private Methods
	//

	private boolean replaceSubstring(SegmentTmTuv p_tuv,
			GxmlElementSubstringReplace p_substringReplacer)
			throws GeneralException, RemoteException
	{
		String segment = p_tuv.getSegment();

		GxmlElement gxmlElement = getGxmlElement(segment);

		boolean replaced = p_substringReplacer.replace(gxmlElement);

		if (replaced)
		{
			p_tuv.setSegment(gxmlElement.toGxml());

			// update exact match key - TODO - maybe the backend tm2
			// code will perform more (all) necessary updates
			String exactMatchFormat = p_tuv.getExactMatchFormat();
			p_tuv.setExactMatchKey(GlobalSightCrc.calculate(exactMatchFormat));
		}

		return replaced;
	}

	private GxmlElement getGxmlElement(String p_segment) throws GxmlException
	{
		GxmlElement result = null;

		GxmlFragmentReader reader = GxmlFragmentReaderPool.instance()
				.getGxmlFragmentReader();

		try
		{
			result = reader.parseFragment(p_segment);
		}
		finally
		{
			GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
		}

		return result;
	}

	private void updateTuvs(ArrayList p_tuvs) throws GeneralException,
			RemoteException
	{
		// group tuvs by tm id
		HashMap tuvsByTmId = new HashMap();
		for (Iterator it = p_tuvs.iterator(); it.hasNext();)
		{
			SegmentTmTuv tuv = (SegmentTmTuv) it.next();
			Long tmId = new Long(tuv.getTu().getTmId());
			ArrayList tuvList = (ArrayList) tuvsByTmId.get(tmId);
			if (tuvList == null)
			{
				tuvList = new ArrayList();
				tuvsByTmId.put(tmId, tuvList);
			}
			tuvList.add(tuv);
		}

		ProjectHandler ph;
        try {
            ph = ServerProxy.getProjectHandler();
        } catch (NamingException e) {
            throw new GeneralException(e);
        }

		// call update method per tm
		for (Iterator it = tuvsByTmId.keySet().iterator(); it.hasNext();)
		{
			Long tmId = (Long) it.next();
			ArrayList tuvList = (ArrayList) tuvsByTmId.get(tmId);

			LingServerProxy.getTmCoreManager().updateSegmentTmTuvs(
					ph.getProjectTMById(tmId, true), tuvList);
		}
	}

	/** Notifies the event listener of the current import status. */
	private void speak(int p_entryCount, int p_percentage, String p_key, String p_defaultMessage, Exception ex)
			throws RemoteException, IOException
	{
		IProcessStatusListener listener = m_listener;

		if (listener != null)
		{
		    String msg = m_listener.getStringFromBundle(p_key, p_defaultMessage);
			listener.listen(p_entryCount, p_percentage, (ex == null)? msg : msg + ex.toString());
		}
		// try {
		// System.out.println("SLEEPING!!!!: " + p_percentage);
		// Thread.sleep(1000 * 5);
		// }
		// catch (Exception e){
		// }
	}

	/**
	 * Helper method to speak unconditionally so the web-client receives
	 * continues updates and has a chance to "cancel" by throwing an
	 * IOException.
	 */
	private void showProgress(int p_current, int p_expected, String p_key, String p_defaultMessage, Exception ex)
			throws IOException
	{
		int percentComplete = (int) ((p_current * 1.0 / p_expected * 1.0) * 100.0);

		if (percentComplete > 100)
		{
			percentComplete = 100;
		}

		speak(p_current, percentComplete, p_key, p_defaultMessage, ex);
	}

	/**
	 * Helper method to speak only when appropriate so the web-client is not
	 * flooded with traffic but still believes search has not died.
	 */
	private void showStatus(int p_current, int p_expected, String p_key, String p_defaultMessage, Exception ex)
			throws IOException
	{
		int percentComplete = (int) ((p_current * 1.0 / p_expected * 1.0) * 100.0);

		if (percentComplete > 100)
		{
			percentComplete = 100;
		}

		// Decide when to update the user's display.
		// With error message: always
		//
		// For 1- 10 expected entries, always update
		// For 11- 100 expected entries, update after every 5th entry
		// For 101-1000 expected entries, update after every 20th
		// For more than 1000 entries, update after every 50th
		//
		if ((p_defaultMessage != null && p_defaultMessage.length() > 0)
				|| (p_expected < 10)
				|| (p_expected >= 10 && p_expected < 100 && (p_current % 5 == 0))
				|| (p_expected >= 100 && p_expected < 1000 && (p_current % 20 == 0))
				|| (p_expected >= 1000 && (p_current % 50 == 0)))
		{
			speak(p_current, percentComplete, p_key, p_defaultMessage, ex);
		}
	}
	
	class CorpusTusComparatorByTm implements Comparator<SegmentTmTu>, Serializable
	{
        private static final long serialVersionUID = 1L;
        private Map<Long, Integer> mapOfTmIdIndex;
		public CorpusTusComparatorByTm(Map<Long, Integer> mapOfTmIdIndex) 
		{
			this.mapOfTmIdIndex = mapOfTmIdIndex;
		}

		public int compare(SegmentTmTu tu1, SegmentTmTu tu2) {
			if(mapOfTmIdIndex == null)
			{
				return 0;
			}
			int index1 = mapOfTmIdIndex.get(tu1.getTmId());
			int index2 = mapOfTmIdIndex.get(tu2.getTmId());
			return index1 - index2;
		}
		
	}
}
