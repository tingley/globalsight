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
package com.globalsight.persistence.workflow;

import java.io.Serializable;

import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * 
 *
 */
public class JbpmVariable implements Serializable
{

	
	private static final long serialVersionUID = -291309101079454832L;

	private long id;
	
	private String name;
	
	private String value;
	
	private String category;
	
	private TaskInstance taskInstance;

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public TaskInstance getTaskInstance()
	{
		return taskInstance;
	}

	public void setTaskInstance(TaskInstance taskInstance)
	{
		this.taskInstance = taskInstance;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	} 
	
}
