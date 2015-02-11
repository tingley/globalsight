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
package com.globalsight.cxe.entity.exportlocation;
/*
 * Copyright (c) 2002 GlobalSight Corporation. All rights reserved.
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

/** Represents a CXE Export Location entity object. */
public interface ExportLocation
{
	/**
	** Return the id of the export location (cannot be set)
	** @return id as a long
	**/
	public long getId();

	/**
	 ** Return the name
	 **/
	public String getName();

	/**
	 ** Return the description
	 **/
	public String getDescription();

	/**
	 ** Return the location (directory)
	 **/
	public String getLocation();

	/**
	 ** Sets the name
	 **/
	public void setName(String p_name);

	/**
	 ** Sets the description
	 **/
	public void setDescription(String p_desc);

	/**
	 ** Sets the location (directory)
	 **/
	public void setLocation(String p_location);
	
	public String getCompanyId();
	
	public void setCompanyId(String id);
}

