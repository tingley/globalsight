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
package com.globalsight.everest.taskmanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskManagerLocal;

/**
 * It is slowly to get WFTask in
 * <code>com.globalsight.everest.taskmanager.TaskManagerLocal -getTasks(TaskSearchParameters  p_searchParameters)</code>,
 * especially in edit role. It affect the performance serious, so we use the pool to record the WFTask.
 */
public class WFTaskPool extends TimerTask
{
	private static Hashtable CURRENT_POOL = new Hashtable();
	private static Hashtable OLD_POOL = new Hashtable();
	private static final long SLEEP_TIME = 1000 * 60 * 60 * 10; // 10 minutes.

    private static final Logger CATEGORY =
        Logger.
        getLogger(WFTaskPool.class.getName());
	
	public WFTaskPool()
	{
		// Timer control cleaning pool.
		Timer timer = new Timer();
		timer.schedule(this, new Date(), SLEEP_TIME);
	}
	
	/**
	 * Updates pool. 
	 * 
	 * @param sessionId
	 * @param userId
	 */
	public static void readWFTasks(String sessionId, String userId)
	{
		String key = getKey(sessionId, userId);
		TaskManagerLocal taskManager = new TaskManagerLocal();
		
		try 
		{
			Map tasks = taskManager.getWFTasks(ServerProxy.getUserManager().getUser(userId),
					Task.STATE_ALL);
			if (tasks != null)
			{
				// Adds to pool.
				addWFTask(key, tasks);
			}
		} 
		catch (Exception e) 
		{
			CATEGORY.error("Gets WFTask failed. The args is ("
					+ sessionId + ", " + userId + ", " + Task.STATE_ALL
					+ ")", e);
		}
	}
	
	/**
	 * Gets tasks based on the given criteria.
	 * 
	 * @param sessionId
	 * @param userId
	 * @param state
	 * @return
	 */
	private static Map getWFTasks(String sessionId, String userId)
	{		
		String key = getKey(sessionId, userId);
		
		// Try to get tasks from current pool.
		Map tasks = (Map)CURRENT_POOL.get(key);
		if (tasks == null)
		{
			// Try to get task from old pool.
			tasks = (Map)OLD_POOL.get(key);
			
			if (tasks != null)
			{
				CURRENT_POOL.put(key, tasks);
			} 
			else
			{
				// Can't get tasks from pool, so read it from database.
				TaskManagerLocal taskManager = new TaskManagerLocal();
				try 
				{
					tasks = taskManager.getWFTasks(ServerProxy.getUserManager().getUser(userId),
							Task.STATE_ALL);
					if (tasks != null)
					{
						// Adds to pool.
						addWFTask(key, tasks);
					}
				} 
				catch (Exception e) 
				{
					tasks = new HashMap();
					
					CATEGORY.error("Gets WFTask failed. The args is ("
							+ sessionId + ", " + userId + ", " + Task.STATE_ALL
							+ ")", e);
				} 
			}
		}
		
		return tasks;
	}
	
	/**
	 * Gets the key in the pool according to the args.
	 * 
	 * @param sessionId
	 * @param userId
	 * @param state
	 * @return
	 */
	public static String getKey(String sessionId, String userId)
	{
		return sessionId + " " + userId;
	}
	
	/**
	 * Adds a task map to pool.
	 * 
	 * @param tasks
	 * @param sessionId
	 * @param userId
	 */
	public synchronized static void addWFTask(String key, Map tasks) 
	{
		CURRENT_POOL.put(key, tasks);
	}
	
	/**
	 * Clears maps in old pool, and put maps in new pool to old pool.
	 */
	public void run() 
	{
		OLD_POOL.clear();
		OLD_POOL = CURRENT_POOL;
		CURRENT_POOL.clear();
	}
	
	/**
	 * Gets tasks according to the search parameters.
	 * 
	 * Will try to get workflow task from pool.
	 * 
	 * @param p_searchParameters
	 * @return
	 */
	 public static Collection getTasks(TaskSearchParameters p_searchParameters) 
	 {
		Map params = p_searchParameters.getParameters();
		User user = (User) params.get(new Integer(TaskSearchParameters.USER));
		String sessionId = (String) params.get(new Integer(
				TaskSearchParameters.SESSION_ID));
		Map wfTasks = getWFTasks(sessionId, user.getUserId());
		List tasks = (List) TaskPersistenceAccessor.getTasks(p_searchParameters);
		TaskManagerLocal taskManager = new TaskManagerLocal();

		try 
		{
			taskManager.linkWorkflowTasksWithTaskInfos(wfTasks, tasks);
		} 
		catch (Exception e) 
		{
			CATEGORY.error("Link workflow tasks with task info failed with error",
							e);
			return new ArrayList();
		}

		return tasks;
	}
}
