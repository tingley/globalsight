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
package com.globalsight.mediasurface;

import java.io.Serializable;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class wraps the CMS user information.
 */
public class CmsUserInfo extends PersistentObject implements Serializable
{
	//
	// PUBLIC CONSTANTS FOR USE BY TOPLINK
	//
	public static final String M_USERID = "m_cmsUserId";

	public static final String M_PASSWORD = "m_cmsPassword";

	//
	// PRIVATE MEMBER VARIABLES
	//
	private String m_ambassadorUserId = null;

	private String m_cmsUserId = null;

	private String m_cmsPassword = null;

	/**
	 * Default constructor used by TopLink only
	 */
	public CmsUserInfo()
	{
		this(null, null, null);
	}

	/**
	 * Constructor that supplies all attributes for user parameter object.
	 * 
	 * @param p_ambassadorUserId
	 *            The GlobalSight user id
	 * @param p_cmsUserId
	 *            The user id used for CMS
	 * @param p_cmsPasswordId
	 *            The password used for CMS
	 */
	public CmsUserInfo(String p_ambassadorUserId, String p_cmsUserId,
			String p_cmsPassword)
	{
		super();

		m_ambassadorUserId = p_ambassadorUserId;
		m_cmsUserId = p_cmsUserId;
		m_cmsPassword = p_cmsPassword;
	}

	/**
	 * Get the GlobalSight user id.
	 */
	public String getAmbassadorUserId()
	{
		return m_ambassadorUserId;
	}

	/**
	 * Get the password used for CMS login
	 */
	public String getCmsPassword()
	{
		return m_cmsPassword;
	}

	/**
	 * Get the user id used for CMS login.
	 */
	public String getCmsUserId()
	{
		return m_cmsUserId;
	}

	/**
	 * Set the cms password to be the specified value.
	 */
	public void setCmsPassword(String p_password)
	{
		m_cmsPassword = p_password;
	}

	/**
	 * Set the cms user id to be the specified value.
	 */
	public void setCmsUserId(String p_cmsUserId)
	{
		m_cmsUserId = p_cmsUserId;
	}

	public void setAmbassadorUserId(String userId)
	{
		m_ambassadorUserId = userId;
	}
}
