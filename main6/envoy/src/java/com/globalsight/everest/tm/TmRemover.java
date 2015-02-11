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

package com.globalsight.everest.tm;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.corpus.CorpusManager;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProcessMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * TmRemover class is responsible for deleting TM.
 */
public class TmRemover extends MultiCompanySupportedThread implements
		ProcessMonitor, ProgressReporter, Serializable
{
	static private final Logger c_logger = Logger
			.getLogger(TmRemover.class);

	private Connection m_connection;

	//private long m_tmId;
	
	private ArrayList<String> tmIds = null;

	private int m_percentage = 0;

	private boolean m_done = false;

	private boolean m_error = false;

	private String m_message = "";

	private InterruptMonitor m_monitor = new InterruptMonitor();
	
	private boolean deleteLanguageFlag = false;
	
	private long localeID;

	/**
	 * TmRemover constructor.
	 * 
	 * @param p_tmId
	 *            TM id to be removed.
	 * @param p_companyId
	 *            ID of the company to which the TM belogs.
	 */
	public TmRemover(ArrayList<String> tmIds) throws TmManagerException
	{
		super();

		this.tmIds = tmIds;

		try
		{
			m_connection = DbUtil.getConnection();
		}
		catch (Exception ex)
		{
			c_logger.error("Failed to get connection.", ex);

			throw new TmManagerException(ex);
		}
	}

	public void run()
	{
		try
		{
			super.run();
			
            setPercentage(0);
            ArrayList<String> dependencyTms = new ArrayList<String>();
            ArrayList<String> nonDependencyTms = new ArrayList<String>(tmIds);
            
            getDependentTmProfileNames(dependencyTms, nonDependencyTms);

            if (dependencyTms.size() > 0)
                setDependencyError(dependencyTms);
            int size = nonDependencyTms.size();
            long tmId = -1l;
            if (size > 0) {
                int round = Math.round(80/size);
                int index = 1;
                Tm tm = null;
                GlobalSightLocale locale = null;
                TmCoreManager manager = LingServerProxy.getTmCoreManager();
                ProjectTMTBUsers ptUsers = new ProjectTMTBUsers();

                for (String tmpTmId : nonDependencyTms) {
                    tmId = Long.parseLong(tmpTmId);
                    
                    tm = ServerProxy.getProjectHandler().getProjectTMById(tmId, true);
                    
                    int convertedRate = tm.getConvertRate();
                    if (convertedRate > 0 && convertedRate < 100) {
                        long tm2Id = tm.getConvertedTM3Id();
                        ProjectTM oriTm = ServerProxy.getProjectHandler().getProjectTMById(tm2Id, true);
                        oriTm.setConvertedTM3Id(-1);
                        oriTm.setLastTUId(-1);
                        HibernateUtil.save(oriTm);
                    }

                    if (deleteLanguageFlag) {
                        locale = ServerProxy.getLocaleManager().getLocaleById(localeID);
                        manager.removeTmData(tm, locale, this, m_monitor);
                    }
                    else {
                        manager.removeTmData(tm, this, m_monitor);
                        ptUsers.deleteAllUsers(String.valueOf(tmId), "TM");
                    }
                    
                    setPercentage(index * round);
                }
            }
            setPercentage(100);
		}
		catch (Throwable ex)
		{
			try
			{
				// cancel the row deletion
				m_connection.rollback();
			}
			catch (SQLException ignore)
			{
			}

			if (ex instanceof InterruptException)
			{
				c_logger.info(m_message);
			}
			else
			{
				c_logger.error("An error occured while removing Tm", ex);

				synchronized (this)
				{
                    m_message = "&lt;font color='red'&gt;" + getStringFromBundle("lb_tm_remove_error_occur", "An error occured while removing Tm")
                            + ": " + ex.getMessage() + "&lt;/font&gt;";
					m_done = true;
					m_error = true;
				}
			}
		}
		finally
		{
			m_done = true;

			try
			{
				DbUtil.returnConnection(m_connection);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	// ProcessMonitor methods

	/** Method for getting counter value. */
	public int getCounter()
	{
		return 0;
	}

	/** Method for getting percentage complete information. */
	public int getPercentage()
	{
		synchronized (this)
		{
			return m_percentage;
		}
	}

	/**
	 * Method for getting a status if the process has finished (either
	 * successfully, with error or canceled by user request)
	 */
	public boolean hasFinished()
	{
		synchronized (this)
		{
			return m_done;
		}
	}

	/**
         * This must be called after setResourceBundle and before start to
         * avoid a window where getReplacingMessage returns null.
	 */
	public void initReplacingMessage()
	{
		setMessageKey("", "");
	}

	/**
	 * Method for getting a message that replaces an existing message in UI.
	 * This is typically used to get a message that shows the current status
	 */
	public String getReplacingMessage()
	{
		synchronized (this)
		{
			return m_message;
		}
	}

	/**
	 * Returns true if an error has occured and the replacing message contains
	 * error message.
	 */
	public boolean isError()
	{
		synchronized (this)
		{
			//return m_error;
		    return false;
		}
	}

	/**
	 * Method for getting messages that are appended in UI. This is typically
	 * used to get messages that cumulatively displayed e.g. items so far done.
	 */
	public List getAppendingMessages()
	{
		return null;
	}
	
	/**
     * Method for setting flag to determine delete TM or TM's language
     */
	public void SetDeleteLanguageFlag(boolean flag) {
	    deleteLanguageFlag = flag;
	}

	synchronized public void cancelProcess()
	{
		m_monitor.interrupt();
	}

	//
	// ProgressReporter
	//
	
    /**
     * Set the current progress message key.  It is generally assumed that
     * this is a key into a localizable message bundle.
     * @param statusMessage progress message key
     */
    @Override
    public void setMessageKey(String messageKey, String defaultMessage) {
	    synchronized (this) {
            String tmp = getStringFromBundle(messageKey, defaultMessage);
            if (!StringUtil.isEmpty(tmp))
                m_message += tmp + "&lt;br&gt;";
	    }
	}
    
    /**
     * Set the current completion percentage.  
     * @param percentage completion percentage
     */
	@Override
    public void setPercentage(int percentage) {
	    synchronized (this) {
	        m_percentage = percentage;
	        if (m_percentage == 100)
	            m_done = true;
	    }
    }
	
	private void setDependencyError(ArrayList<String> p_tmProfileNames)
	{
		synchronized (this)
		{
		    String tmpMsg = "Tm is refered to by the following Tm Profiles. "
                + "Please deselect this Tm from the Tm Profiles before removing it.";
			m_message = "&lt;font color='red'&gt;" + getStringFromBundle("lb_tm_remove_tm_deselect_tmp", tmpMsg) + "&lt;br&gt;";

			for (Iterator<String> it = p_tmProfileNames.iterator(); it.hasNext();)
				m_message += "&lt;b&gt;" + it.next() + "&lt;/b&gt;&lt;br&gt;";

			m_message += "&lt;/font&gt;";
			
			m_error = true;
		}
	}

	private void checkInterrupt() throws InterruptException
	{
		m_message = getStringFromBundle("lb_tm_remove_cancel_by_user", "Tm removal has been cancelled by user request.");
		m_done = true;

		throw new InterruptException();
	}

	private void getDependentTmProfileNames(ArrayList<String> dependencyTms, ArrayList<String> nonDependencyTms) throws Exception
	{
		Collection tmProfiles = ServerProxy.getProjectHandler()
				.getAllTMProfiles();

		String tmp = null;
		long tmpTmId = -1l;
		Tm tm = null;
		for (Iterator itTmProfiles = tmProfiles.iterator(); itTmProfiles
				.hasNext();)
		{
			TranslationMemoryProfile profile = (TranslationMemoryProfile) itTmProfiles
					.next();

			tmpTmId = profile.getProjectTmIdForSave();
			// check with the Tm to save
			if (tmIds.contains(String.valueOf(tmpTmId)))
			{
			    ProjectTM projectTM = ServerProxy.getProjectHandler().getProjectTMById(tmpTmId, false);
				dependencyTms.add(projectTM.getName());
				nonDependencyTms.remove(String.valueOf(tmpTmId));
				continue;
			}

			// check with the Tm to leverage from
			Collection tms = profile.getProjectTMsToLeverageFrom();
			for (Iterator itTms = tms.iterator(); itTms.hasNext();)
			{
				LeverageProjectTM lptm = (LeverageProjectTM) itTms.next();
				tmp = String.valueOf(lptm.getProjectTmId());
				if (tmIds.contains(tmp))
				{
					dependencyTms.add(profile.getName());
					nonDependencyTms.remove(tmp);
					break;
				}
			}
		}
	}

	private class InterruptException extends Exception
	{
	}
	
	public void setLocaleId(long cl) {
	    localeID = cl;
	}
}
