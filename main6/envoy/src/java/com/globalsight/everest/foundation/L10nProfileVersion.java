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
package com.globalsight.everest.foundation;

/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

/**
 * This is a implementation for localization profile version node. All version
 * of localization profile is maintained in localization profile table,
 * including the modified and original versions of those modified profiles. The
 * original and its corresponding modified version is matched up using an
 * instance of this class. During persistence of modified profile, we create an
 * instance of L10nProfileVersion and persist it to table L10N_PROFILE_VERSION
 * in database. Each persisted L10nProfileVersion also includes, persistence
 * service generated, ID which can be used as order sequence when generating a
 * version tree.
 *
 * A version tree can be created by traversing an ordering list of
 * L10nProfileVersion objects by building a list of profile IDs using modified
 * profile ID as an original profile ID for subsequent search, until no more
 * modified IDs are defined.
 */

import com.globalsight.everest.persistence.PersistentObject;

public class L10nProfileVersion extends PersistentObject
{
	private static final long serialVersionUID = -920280655091455226L;

	private long m_originalProfileId;
	private long m_modifiedProfileId;

	/**
	 * Default constructor to be used by TopLink only. This is here solely
	 * because the persistence mechanism that persists instances of this class
	 * is using TopLink, and TopLink requires a public default constructor for
	 * all the classes that it handles persistence for.
	 */
	public L10nProfileVersion()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param long
	 *            orginialProfileId The ID for orginial profile
	 * @param long
	 *            modifiedProfileId The ID of modified profile
	 */
	public L10nProfileVersion(long originalProfileId, long modifiedProfileId)
	{
		m_originalProfileId = originalProfileId;
		m_modifiedProfileId = modifiedProfileId;
	}

	/**
	 * Get the original profile ID. This will be used as the key to get a
	 * corresponding localization profile from localization profile table.
	 * <p>
	 * 
	 * @return long The ID for original profile.
	 */
	public long getOriginalProfileId()
	{
		return m_originalProfileId;
	}

	public void setOriginalProfileId(long originalProfileId)
	{
		m_originalProfileId = originalProfileId;
	}

	/**
	 * Get the modified profile ID. This is the ID for modified profile, which
	 * has been versioned from the profile defined at original profile ID. This
	 * will be used as the key to get a corresponding localization profile from
	 * localization profile table.
	 * <p>
	 * 
	 * @return long The ID for corresponding modified profile.
	 */
	public long getModifiedProfileId()
	{
		return m_modifiedProfileId;
	}

	public void setModifiedProfileId(long modifiedProfileId)
	{
		m_modifiedProfileId = modifiedProfileId;
	}

	/**
	 * Get the internal version identification
	 * 
	 * @return The internal version identification
	 */
	public long getVersionId()
	{
		return getId();
	}

}
