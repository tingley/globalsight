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

package com.globalsight.everest.webapp.pagehandler.administration.customer;

import java.io.Serializable;

/**
 * Holds object needed for View Files page.
 */
public class SourceFile implements Serializable
{
	public static final String FILE_LIST = "sourceFiles";

	public static final String FILE_KEY = "sourceFile";

	private String name;

	private String status;

	public SourceFile(String name, String status)
	{
		this.name = name;
		this.status = status;
	}

	public String getName()
	{
		return name;
	}

	public String getStatus()
	{
		return status;
	}
}
