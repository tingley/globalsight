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
package com.globalsight.everest.projecthandler;

import com.globalsight.everest.persistence.PersistentObject;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 * 
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF GLOBALSIGHT
 * CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT IN CONFIDENCE.
 * INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED OR DISCLOSED IN WHOLE OR
 * IN PART EXCEPT AS PERMITTED BY WRITTEN AGREEMENT SIGNED BY AN OFFICER OF
 * GLOBALSIGHT CORPORATION.
 * 
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER SECTIONS 104
 * AND 408 OF TITLE 17 OF THE UNITED STATES CODE. UNAUTHORIZED USE, COPYING OR
 * OTHER REPRODUCTION IS PROHIBITED BY LAW.
 */

public class ProMTInfo extends PersistentObject
{
	private static final long serialVersionUID = 7490266421052480175L;
	
	private TranslationMemoryProfile tmProfile = null;
	private long m_dirId = -1;
	private String m_dirName = null;
	private String m_topicTemplateId = null;
	
    public long getDirId() {
		return this.m_dirId;
	}

	public void setDirId(long p_dirId) {
		this.m_dirId = p_dirId;
	}

	public String getDirName() {
		return this.m_dirName;
	}

	public void setDirName(String p_dirName) {
		m_dirName = p_dirName;
	}

	public String getTopicTemplateId() {
		return this.m_topicTemplateId;
	}

	public void setTopicTemplateId(String p_topicTemplateId) {
		m_topicTemplateId = p_topicTemplateId;
	}

	public void setTMProfile(TranslationMemoryProfile p_tmProfile) {
        tmProfile = p_tmProfile;
    }

    public TranslationMemoryProfile getTMProfile() {
        return tmProfile;
    }
}
