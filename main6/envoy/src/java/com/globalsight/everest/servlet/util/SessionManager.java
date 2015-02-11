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

package com.globalsight.everest.servlet.util;

import java.io.Serializable;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.log.GlobalSightCategory;

/**
 * SessionManager manages the session's activites in terms of storing/removing
 * state preservation objects that will be used by the client. After a session
 * expires, the content of this object will be set to null.
 * 
 * @author Tomy A. Doomany
 */
public class SessionManager implements HttpSessionBindingListener, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
			.getLogger(SessionManager.class);

	private String m_uid = null;

	private HashMap m_hashMap = null;
	
	// We cannot use m_hashMap for all reports, we set the data which are needed for report 
    // pagination in sessionManager, but the sesionManager will be cleaned when user perfome 
    // other operation (such as click other links). In this case, the 
    // report cannot perform pagination any more because the data has been cleaned.
    
    private HashMap r_hashMap = null;


	/**
	 * Constructor
	 */
	public SessionManager(String p_uid, HttpSession p_session)
	{
		m_uid = p_uid;
		m_hashMap = new HashMap(6); 
		r_hashMap = new HashMap(7); // Seven reports in all.
	}

	//
	// HttpSessionBindingListener Implementation
	//

	/**
	 * Notifies the object that it is being bound to a session and identifies
	 * the session.
	 * 
	 * @param p_event
	 *            HttpSesionBindingEvent that identifies the session.
	 */
	public void valueBound(HttpSessionBindingEvent p_event)
	{
		// DO NOTHING
	}

	/**
	 * Notifies the object that it is being unbound from a session and
	 * identifies the session.
	 * 
	 * @param p_event
	 *            HttpSessionBindingEvent that identifies the session.
	 */
	public void valueUnbound(HttpSessionBindingEvent p_event)
	{
		// get the session from the event
		HttpSession session = p_event.getSession();

		try
		{
			// log the user out
			User user = (User) getAttribute(WebAppConstants.USER);
			ServerProxy.getSecurityManager().logUserOut(user.getUserId());
			// calculate the users after logging out the system.
			ServerProxy.getUserManager().loggedOutUsers(user.getUserId(),
					session.getId());
		}
		catch (Exception e)
		{
			CATEGORY.error("error when closing workflow session", e);
		}

		try
		{
			AppletDirectory ad = AppletDirectory.getInstance();

			ad.removeSession(m_uid);
		}
		catch (Exception e)
		{
			CATEGORY.error("error when unregistering session from applet", e);
		}

		clearAll();
		m_uid = null;
	}

	//
	// Public Methods
	//

	/**
	 * Get the generated uid used by applets (for accessing the current
	 * session).
	 * 
	 * @return The generated uid.
	 */
	public String getUid()
	{
		return m_uid;
	}

	/**
	 * Store the state preservation objects based on a predefined key (based on
	 * module).
	 * 
	 * @param p_key -
	 *            The key used as an alias name for the object. This key is
	 *            predefined based for each module.
	 * @param p_value -
	 *            The object that preserves the state.
	 */
	public void setAttribute(String p_key, Object p_value)
	{
		m_hashMap.put(p_key, p_value);
	}

	/**
	 * Get a state preservation object based on a particular key.
	 * 
	 * @param p_key -
	 *            The key used as an alias name for the object.
	 * @return The requested object if it exists, otherwise return null;
	 */
	public Object getAttribute(String p_key)
	{
		return m_hashMap.get(p_key);
	}

	/**
	 * DEBUG: allow public access to the hash map for debugging purposes.
	 */
	public HashMap getMap()
	{
		return m_hashMap;
	}

	/**
	 * Clean up the SessionManager by removing all objects except for user
	 * object and user's permissions. This method is used by the servlet
	 * everytime a new link is clicked (activity links).
	 */
	public void clear()
	{
		// get the user and user's permissions
		Object user = getAttribute(WebAppConstants.USER);

		clearAll();

		setAttribute(WebAppConstants.USER, user);
	}

	/**
	 * Remove a particular object based on the key.
	 * 
	 * @param p_key -
	 *            The key used as an alias name for the object.
	 */
	public void removeElement(String p_key)
	{
		m_hashMap.remove(p_key);
	}

	/**
	 * Clears the hashtable so that it contains no keys.
	 */
	private void clearAll()
	{
		m_hashMap.clear();
	}

	/**
	 * Return a string representation of the object.
	 * 
	 * @return a string representation of the object.
	 */
	public String toString()
	{
		return super.toString() + " [m_hashMap="
				+ (m_hashMap != null ? m_hashMap.toString() : "null")
				+ " m_uid=" + (m_uid != null ? m_uid.toString() : "null") + "]";
	}
	/**
     * Just used by report part
     * Store the state preservation objects based on a predefined key
     * (based on module).
     * @param p_key - The key used as an alias name for the object.
     * This key is predefined based for each module.
     * @param p_value - The object that preserves the state.
     */
    public void setReportAttribute(String p_key, Object p_value)
    {
        r_hashMap.put(p_key, p_value);
    }

    /**
     * Just used by report part
     * Get a state preservation object based on a particular key.
     * @param p_key - The key used as an alias name for the object.
     * @return The requested object if it exists, otherwise return null;
     */
    public Object getReportAttribute(String p_key)
    {
        return r_hashMap.get(p_key);
    }
    
    /**
     * Just used by report part
     * Clears the hashtable so that it contains no keys.
     */
    public void clearReportMap()
    {
        r_hashMap.clear();
    }
    
    /**
     * Just used by report part
     * Remove a particular object based on the key.
     * @param p_key - The key used as an alias name for the object.
     */
    public void removeElementOfReportMap(String p_key)
    {
        r_hashMap.remove(p_key);
    }
    
}
