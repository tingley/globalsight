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
package com.globalsight.everest.workflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.resourcebundle.LocaleWrapper;

/**
 * WorkflowHelper class is a superclass of WorkflowTemplateHelper and
 * WorkflowInstanceHelper and is responsible for getting the tasks of a template
 * or an instance.
 * <p>
 */

public class WorkflowHelper
{
	private static final Logger s_logger = Logger
			.getLogger(WorkflowHelper.class.getName());

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	public WorkflowHelper()
	{
	}

	// /////////////////////////////////////////////////////////
	// Begin: Public Methods
	// /////////////////////////////////////////////////////////

	/**
     * Return a string representation of the object appropriate for logging.
     * 
     * @return a string representation of the object appropriate for logging.
     */
	public static String toDebugString(Collection p_workflowObjects)
	{
		if (p_workflowObjects == null)
		{
			return "null";
		}
		Iterator it = p_workflowObjects.iterator();
		StringBuilder returnBuff = new StringBuilder(100);
		while (it.hasNext())
		{
			returnBuff.append(toDebugString(it.next()));
			returnBuff.append(GlobalSightCategory.getLineContinuation());
		}
		return returnBuff.toString();
	}

	/**
     * Return a string representation of the object appropriate for logging.
     * 
     * @return a string representation of the object appropriate for logging.
     */
	public static String toDebugString(Object p_workflowObject)
	{
		if (p_workflowObject == null)
		{
			return "null";
		}

		return p_workflowObject.toString();
	}

	/**
     * Return a string representation of the object appropriate for logging.
     * 
     * @return a string representation of the object appropriate for logging.
     */
	public static String toDebugStringWorkflowTaskInstanceState(
			int p_workflowTaskInstanceState)
	{
		switch (p_workflowTaskInstanceState)
		{
			case WorkflowConstants.TASK_ACTIVE:
				return "TASK_ACTIVE";
			case WorkflowConstants.TASK_DEACTIVE:
				return "TASK_DEACTIVE";
			case WorkflowConstants.TASK_ACCEPTED:
				return "TASK_ACCEPTED";
			case WorkflowConstants.TASK_DECLINED:
				return "TASK_DECLINED";
			case WorkflowConstants.TASK_COMPLETED:
				return "TASK_COMPLETED";
		}
		return "undefined " + Integer.toString(p_workflowTaskInstanceState);
	}

	/**
     * Return a string representation of the object appropriate for logging.
     * 
     * @return a string representation of the object appropriate for logging.
     */
	public static String toDebugStringNodeType(int p_nodeType)
	{
		switch (p_nodeType)
		{
			case WorkflowConstants.START:
				return "START";
			case WorkflowConstants.STOP:
				return "STOP";
			case WorkflowConstants.ACTIVITY:
				return "ACTIVITY";
			case WorkflowConstants.AND:
				return "AND";
			case WorkflowConstants.OR:
				return "OR";
			case WorkflowConstants.CONDITION:
				return "CONDITION";
		}
		return "undefined " + Integer.toString(p_nodeType);
	}

	// /////////////////////////////////////////////////////////
	// Begin: Package Methods
	// /////////////////////////////////////////////////////////

	/**
     * Convert the string representation of a locale to Locale object
     * 
     * @param p_localeAsString -
     *            locale as a string (i.e. en_US).
     * @return The Locale object based on the given string.
     */
	static Locale convertToLocale(String p_localeAsString)
	{
		return LocaleWrapper.getLocale(p_localeAsString);
	}

	/**
     * Get a source/target locale pair displayed based on the display locale.
     * 
     * @param p_sourceLocale -
     *            The source locale.
     * @param p_targetLocale -
     *            The target locale.
     * @param p_displayLocale -
     *            The locale used for displaying the locale pair.
     * @return The string representation of the locale pair based on the display
     *         locale (i.e. English (United States) / French (France))
     */
	static String localePair(String p_sourceLocale, String p_targetLocale,
			String p_displayLocale)
	{

		// convert string representation of a locale to Locale object
		Locale displayLocale = convertToLocale(p_displayLocale);
		Locale srcLocale = convertToLocale(p_sourceLocale);
		Locale trgtLocale = convertToLocale(p_targetLocale);

		StringBuilder sb = new StringBuilder();
		sb.append(srcLocale.getDisplayName(displayLocale));
		sb.append(" / ");
		sb.append(trgtLocale.getDisplayName(displayLocale));

		return sb.toString();
	}

	// ///////////////////////////////////////////////////////////////////////
	// End: Helper Methods
	// ///////////////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////////////////////
	// Begin: Local Methods
	// ///////////////////////////////////////////////////////////////////////

	/*
     * Mapping an integer-coded attribute to a timer action attribute name <p>
     * @param String The timer name @param int The attribute @return String The
     * timer action attribute name
     */
	static String getTimerActionAttributeName(String p_timerName, int p_attr)
	{
		String suffix = null;

		switch (p_attr)
		{
			case WorkflowConstants.MAIL_FROM:
				suffix = WorkflowConstants.MAIL_FROM_SUFFIX;
				break;
			case WorkflowConstants.MAIL_TO:
				suffix = WorkflowConstants.MAIL_TO_SUFFIX;
				break;
			case WorkflowConstants.MAIL_CC:
				suffix = WorkflowConstants.MAIL_CC_SUFFIX;
				break;
			case WorkflowConstants.MAIL_BCC:
				suffix = WorkflowConstants.MAIL_BCC_SUFFIX;
				break;
			case WorkflowConstants.MAIL_SUBJECT:
				suffix = WorkflowConstants.MAIL_SUBJECT_SUFFIX;
				break;
			case WorkflowConstants.MAIL_BODY:
				suffix = WorkflowConstants.MAIL_BODY_SUFFIX;
				break;
			default:
				break;
		}
		return p_timerName + suffix;
	}

	// Reset the data item names to it's corresponding integer constant
	// by basicially removing the timer names + sequence number and reading
	// the suffixes.
	//
	// When the workflow is saved appropriate prefix is attached at that time.
	//
	static String resetAttributeName(String p_attrName)
	{
		int lpos = p_attrName.lastIndexOf('_');
		if (lpos != -1)
		{
			String attr = p_attrName.substring(lpos);
			if (attr.equals(WorkflowConstants.MAIL_FROM_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_FROM);
			else if (attr.equals(WorkflowConstants.MAIL_TO_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_TO);
			else if (attr.equals(WorkflowConstants.MAIL_CC_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_CC);
			else if (attr.equals(WorkflowConstants.MAIL_BCC_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_BCC);
			else if (attr.equals(WorkflowConstants.MAIL_SUBJECT_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_SUBJECT);
			else if (attr.equals(WorkflowConstants.MAIL_BODY_SUFFIX))
				return String.valueOf(WorkflowConstants.MAIL_BODY);
			return p_attrName.substring(0, lpos);
		}

		return p_attrName;
	}

	// ///////////////////////////////////////////////////////////////////////
	// End: Local Methods
	// ///////////////////////////////////////////////////////////////////////
}
